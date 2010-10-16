package lavit.stateviewer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

import lavit.Env;
import lavit.util.*;

public class StateGraphBasicDrawer extends StateDraw {
	StateGraphPanel panel;

	Graphics2D g2;
	StateNodeSet drawNodes;
	private double zoom;

	StateGraphBasicDrawer(StateGraphPanel panel){
		this.panel = panel;
	}

	public void drawGraph(Graphics g){
		this.g2 = (Graphics2D)g;
		this.drawNodes = panel.getDrawNodes();
		this.zoom = panel.getZoom();

		g2.setFont(font);

		//フレームの初期化
		g2.setColor(Color.white);
		g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

		if(!panel.isActive()){ return; }

		double startTime = System.currentTimeMillis();
		//this.simpleMode = zoom<=0.7;

		//描写対象の決定
		double minX=-10/zoom,maxX=(panel.getWidth()+10)/zoom;
		double minY=-10/zoom,maxY=(panel.getHeight()+10)/zoom;
		for(StateNode node : drawNodes.getAllNode()){
			node.setInFrame(false);
			if(minX<node.getX()&&node.getX()<maxX&&minY<node.getY()&&node.getY()<maxY){
				node.setInFrame(true);
			}
		}

		if(Env.is("SV_VERTICAL_VIEW")){ g2.rotate(90 * Math.PI/180,panel.getWidth()/2, panel.getHeight()/2); }
		g2.scale(zoom,zoom);

		//初期状態の矢印の描写
		if(!Env.is("SV_SHOWOUTTRANS")||drawNodes.getAllOutTransition().size()==0){
			drawStartArrow(g2);
		}

		//OUTTRANSの描画
		if(Env.is("SV_SHOWOUTTRANS")){
			g2.setColor(Color.gray);
			//drawNodes.updateOutTransition();

			Collection<StateNode> all = drawNodes.getAllNode();
			HashMap<StateNode,LinkedHashSet<StateNode>> outFrom = new HashMap<StateNode,LinkedHashSet<StateNode>>();
			HashMap<StateNode,LinkedHashSet<StateNode>> outTo = new HashMap<StateNode,LinkedHashSet<StateNode>>();
			for(StateTransition t : drawNodes.getAllOutTransition()){
				if(!all.contains(t.from)&&all.contains(t.to)){
					if(!outFrom.containsKey(t.from)){
						outFrom.put(t.from, new LinkedHashSet<StateNode>());
					}
					outFrom.get(t.from).add(t.to);
				}
				if(all.contains(t.from)&&!all.contains(t.to)){
					if(!outTo.containsKey(t.to)){
						outTo.put(t.to, new LinkedHashSet<StateNode>());
					}
					outTo.get(t.to).add(t.from);
				}
			}
			int i=2;
			for(StateNode from : outFrom.keySet()){
				double y = (panel.getHeight()/zoom)*((double)i/(double)(outFrom.size()+2));
				for(StateNode to : outFrom.get(from)){
					drawNodeArrow(g2,0,y,0,to.getX(),to.getY(),to.getRadius(),5);
				}
				i++;
			}
			i=2;
			for(StateNode to : outTo.keySet()){
				double y = (panel.getHeight()/zoom)*((double)i/(double)(outTo.size()+2));
				for(StateNode from : outTo.get(to)){
					drawNodeArrow(g2,from.getX(),from.getY(),from.getRadius(),panel.getWidth()/zoom,y,0,5);
				}
				i++;
			}

		}

		//線の描写
		if(simpleMode){
			//すべて直線で描画
			for(StateTransition t : drawNodes.getAllTransition()){
				drawTransition(g2, t, null);
			}
		}else{
			//ダミー以外を描画
			for(StateTransition t : drawNodes.getAllTransition()){
				if(t.from.dummy||t.to.dummy){ continue; }
				drawTransition(g2, t, null);
			}
			//ダミーカーブ
			ArrayList<ArrayList<StateNode>> dummyGroups = getDummyGroups(drawNodes.getDepthNode());
			drawDummyCurve(g2, dummyGroups, null);
		}

		//ノードの描写
		for(StateNode node : drawNodes.getAllNode()){
			drawNode(g2, node, null, null);
		}

		//サイクルの優先描画
		ArrayList<StateNode> cycleNode = drawNodes.getCycleNode();
		for(StateNode node : cycleNode){
			StateTransition t = node.getToCycleTransition();
			if(t!=null){
				if(simpleMode){
					drawTransition(g2, t, null);
				}else if(t.from.dummy){
					drawDummyCurve(g2, t.from, null);
				}else if(t.to.dummy){
					drawDummyCurve(g2, t.to, null);
				}else{
					drawTransition(g2, t, null);
				}
			}
		}
		for(StateNode node : cycleNode){
			drawNode(g2, node, null, null);
		}

		//選択しているノードの描写
		for(StateNode node : panel.getSelectNodes()){
			drawSelectNode(g2,node);
		}
		panel.updateNodeLabel();
		panel.validate();

		//選択トランジションの描画
		if(panel.getSelectTransition()!=null){
			drawSelectTransition(g2, panel.getSelectTransition());
		}

		//debug:トランジション選択範囲の描画
		/*
		g2.setColor(Color.LIGHT_GRAY);
		for(StateTransition t : drawNodes.getAllTransition()){
			t.draw(g2);
		}
		if(selectTransition!=null){
			g2.setColor(Color.RED);
			selectTransition.draw(g2);
		}
		*/

		//選択時の四角の表示
		if(panel.isSelectSquare()&&panel.getLastPoint()!=null&&panel.getStartPoint()!=null){
			Point p1 = new Point((int)((double)panel.getLastPoint().getX()/zoom), (int)((double)panel.getLastPoint().getY()/zoom));
			Point p2 = new Point((int)((double)panel.getStartPoint().getX()/zoom), (int)((double)panel.getStartPoint().getY()/zoom));

			g2.setColor(Color.RED);
			g2.drawRect(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.max(p1.x, p2.x)-Math.min(p1.x, p2.x), Math.max(p1.y, p2.y)-Math.min(p1.y, p2.y));
		}

		g2.scale(1.0/zoom, 1.0/zoom);
		if(Env.is("SV_VERTICAL_VIEW")){ g2.rotate(-90 * Math.PI/180, panel.getWidth()/2, panel.getHeight()/2); }

		panel.setDrawTime(System.currentTimeMillis()-startTime);
	}

	private void drawStartArrow(Graphics2D g2){
		if(drawNodes.getStartNode().size()!=1){ return; }

		StateNode node = drawNodes.getStartNodeOne();
		if(!node.isInFrame()){ return; }

		if(node.weak){
			g2.setColor(Color.lightGray);
		}else{
			g2.setColor(Color.black);
		}

		drawNodeArrow(g2,node.getX()-30,node.getY(),node.getRadius(),node.getX()-7,node.getY(),node.getRadius(),5);
	}

	/*
	private void drawArrow(Graphics2D g2,StateNode node){

		//遷移先への矢印を表示
		for(StateNode to : node.getToNodes()){
			if(hideBackEdgeMode&&to.depth<node.depth){ continue; }
			if(!node.isInFrame()&&!to.isInFrame()){ continue; }
			if(node.weak||to.weak){
				g2.setColor(Color.lightGray);
			}else if(!node.isEmToNode(to)&&(node.inCycle||to.inCycle)){
				g2.setColor(Color.lightGray);
			}else{
				g2.setColor(Color.black);
			}
			if(!simpleMode){
				if(to!=node){
					if(to.dummy){
						drawLine(g2,node.getX(),node.getY(),to.getX(),to.getY());
					}else{
						drawNodeArrow(g2,node.getX(),node.getY(),node.getRadius(),to.getX(),to.getY(),to.getRadius(),5);
					}
				}else{
					drawSelfArrow(g2,node);
				}

				if((showRuleMode||showNoNameRuleMode)&&!node.dummy){
					String str = node.getToRuleName(to);
					if(str.length()>0){
						if((!str.substring(0, 1).equals("_")&&showRuleMode)||(str.substring(0, 1).equals("_")&&showNoNameRuleMode)){
							FontMetrics fm = g2.getFontMetrics();
							int h = 0;
							if(node.depth>to.depth){
								h = fm.getHeight();
							}
							g2.drawString(str,(int)((node.getX()+to.getX())/2)-fm.stringWidth(str)/2,(int)((node.getY()+to.getY())/2)+h);
						}
					}
				}

			}else{
				if(to!=node){
					drawLine(g2,node.getX(),node.getY(),to.getX(),to.getY());
				}
			}
		}
	}
	*/

	private void drawTransition(Graphics2D g2, StateTransition t, Color color){

		StateNode from = t.from;
		StateNode to = t.to;

		if(hideBackEdgeMode&&to.depth<from.depth){ return; }
		if(!from.isInFrame()&&!to.isInFrame()){ return; }

		if(color==null){
			if(searchMode&&t.weak||!searchMode&&cycleMode&&!t.cycle){
				color = Color.lightGray;
			}else{
				color = Color.black;
			}
		}
		g2.setColor(color);

		if(!simpleMode){
			//矢印の表示
			if(to!=from){
				if(to.dummy){
					if(to.dummy&&from.dummy){
						drawLine(g2,from.getX(),from.getY(),to.getX(),to.getY());
					}else{
						drawNodeLine(g2,from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),0);
					}
				}else{
					drawNodeArrow(g2,from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),to.getRadius(),5);
				}
			}else{
				drawSelfArrow(g2,from);
			}
			//ルール名の表示
			if((showRuleMode||showNoNameRuleMode)&&!from.dummy){
				String str = from.getToRuleName(to);
				if(str.length()>0){
					if((!str.substring(0, 1).equals("_")&&showRuleMode)||(str.substring(0, 1).equals("_")&&showNoNameRuleMode)){
						FontMetrics fm = g2.getFontMetrics();
						int h = 0;
						if(from.depth>to.depth){
							h = fm.getHeight();
						}
						g2.drawString(str,(int)((from.getX()+to.getX())/2)-fm.stringWidth(str)/2,(int)((from.getY()+to.getY())/2)+h);
					}
				}
			}
		}else{
			if(to!=from){
				drawLine(g2,from.getX(),from.getY(),to.getX(),to.getY());
			}
		}
	}

	private ArrayList<ArrayList<StateNode>> getDummyGroups(ArrayList<ArrayList<StateNode>> depthNode){
		ArrayList<ArrayList<StateNode>> dummyGroups = new ArrayList<ArrayList<StateNode>>();
		LinkedHashSet<StateNode> dummys = new LinkedHashSet<StateNode>();
		for(ArrayList<StateNode> nodes : depthNode){
			for(StateNode node : nodes){
				if(node.dummy){
					dummys.add(node);
				}
			}
		}
		while(dummys.size()>0){
			ArrayList<StateNode> dummyGroup = new ArrayList<StateNode>();
			StateNode node = null;
			for(StateNode tempNode : dummys){
				node = tempNode;
				break;
			}
			while(node.dummy){
				dummyGroup.add(node);
				dummys.remove(node);
				node = node.getFromNodes().get(0);
			}
			dummyGroups.add(dummyGroup);
		}

		return dummyGroups;
	}

	private void drawDummyCurve(Graphics2D g2, ArrayList<ArrayList<StateNode>> dummyGroups,  Color color){
		for(ArrayList<StateNode> dummyGroup : dummyGroups){
			/*
			StateNode sN = dummyGroup.get(0).getToNodes().get(0);
			StateNode n1 = dummyGroup.get(0);
			StateNode n2 = n1.getFromNodes().get(0);
			if(color==null){
				if(searchMode&&n1.weak||!searchMode&&cycleMode&&!n1.cycle){
					color = Color.lightGray;
				}else{
					color = Color.black;
				}
			}
			dummyGroup.remove(n1);
			dummyGroup.remove(n2);

			GeneralPath p = new GeneralPath();
			p.moveTo(sN.getX(), sN.getY());

			while(dummyGroup.size()>=1){
				p.quadTo(n1.getX(), n1.getY(), n2.getX(), n2.getY());
				n1 = dummyGroup.get(0);
				n2 = n1.getFromNodes().get(0);
				dummyGroup.remove(n1);
				dummyGroup.remove(n2);
			}

			if(!n2.dummy){
				p.quadTo(n1.getX(), n1.getY(), n2.getX(), n2.getY());
			}else{
				StateNode n3 = n2.getFromNodes().get(0);
				p.curveTo(n1.getX(), n1.getY(), n2.getX(), n2.getY(), n3.getX(), n3.getY());
			}
			g2.draw(p);
			*/
/*
			ArrayList<Point2D> points = new ArrayList<Point2D>();
			StateNode n0 = dummyGroup.get(0).getToNodes().get(0);
			StateNode n = n0;
			for(StateNode node : dummyGroup){
				points.add(new Point2D.Double((n.getX()+node.getX())/2,(n.getY()+node.getY())/2));
				points.add(new Point2D.Double(node.getX(),node.getY()));
				n = node;
			}
			StateNode nN = n.getFromNodes().get(0);
			points.add(new Point2D.Double((n.getX()+nN.getX())/2,(n.getY()+nN.getY())/2));

			GeneralPath p = new GeneralPath();
			p.moveTo(n0.getX(), n0.getY());
			p.lineTo(points.get(0).getX(), points.get(0).getY());
			for(int i=1;(i+1)<points.size();i+=2){
				p.quadTo(points.get(i).getX(), points.get(i).getY(), points.get(i+1).getX(), points.get(i+1).getY());
			}
			p.lineTo(nN.getX(), nN.getY());
			g2.draw(p);
			*/
			drawDummyCurve(g2, dummyGroup.get(0), null);
		}
	}

	//ダミーカーブの描画
	private void drawDummyCurve(Graphics2D g2, StateNode dummy, Color color){
		if(color==null){
			if(searchMode&&dummy.weak||!searchMode&&cycleMode&&!dummy.cycle){
				color = Color.lightGray;
			}else{
				color = Color.black;
			}
		}
		g2.setColor(color);

		//ダミーのリストの作成
		ArrayList<StateNode> dummyGroup = new ArrayList<StateNode>();
		StateNode n = dummy;
		while(n.getFromNode().dummy){
			n = n.getFromNode();
		}
		while(n.dummy){
			dummyGroup.add(n);
			n = n.getToNode();
		}

		//ダミーの中間点を作成
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		StateNode n0 = dummyGroup.get(0).getFromNode();
		n = n0;
		for(StateNode node : dummyGroup){
			points.add(new Point2D.Double((n.getX()+node.getX())/2,(n.getY()+node.getY())/2));
			points.add(new Point2D.Double(node.getX(),node.getY()));
			n = node;
		}
		StateNode nN = n.getToNode();
		points.add(new Point2D.Double((n.getX()+nN.getX())/2,(n.getY()+nN.getY())/2));

		if(hideBackEdgeMode&&nN.depth<n0.depth){ return; }
		if(!n0.isInFrame()&&!nN.isInFrame()){ return; }

		//パスの作成、直線、曲線描画
		GeneralPath p = new GeneralPath();
		Point2D fP = points.get(0);
		drawNodeLine(g2,n0.getX(), n0.getY(), n0.getRadius(), fP.getX(), fP.getY(), 0);

		p.moveTo(fP.getX(), fP.getY());
		for(int i=1;(i+1)<points.size();i+=2){
			p.quadTo(points.get(i).getX(), points.get(i).getY(), points.get(i+1).getX(), points.get(i+1).getY());
		}
		g2.draw(p);

		//矢印の描画
		Point2D lP = points.get(points.size()-1);
		drawNodeArrow(g2, lP.getX(), lP.getY(), 0, nN.getX(), nN.getY(), nN.getRadius(), 5);


		//ルール名の表示
		if(showRuleMode||showNoNameRuleMode){
			String str = n0.getToRuleName(dummyGroup.get(0));
			if(str.length()>0){
				if((!str.substring(0, 1).equals("_")&&showRuleMode)||(str.substring(0, 1).equals("_")&&showNoNameRuleMode)){
					FontMetrics fm = g2.getFontMetrics();
					int h = 0;
					if(n0.depth>dummyGroup.get(0).depth){
						h = fm.getHeight();
					}
					g2.drawString(str,(int)((n0.getX()+dummyGroup.get(0).getX())/2)-fm.stringWidth(str)/2,(int)((n0.getY()+dummyGroup.get(0).getY())/2)+h);
				}
			}
		}
	}

	private void drawNode(Graphics2D g2, StateNode node, Color fillColor, Color drawColor){
		if(!node.isInFrame()){ return; }

		if(hideBackEdgeMode&&node.backDummy||!showDummyMode&&node.dummy){ return; }

		if(fillColor==null||drawColor==null){
			if(searchMode&&node.weak||!searchMode&&cycleMode&&!node.cycle){
				fillColor = Color.white;
				drawColor = node.getColor();
			}else{
				fillColor = node.getColor();
				drawColor = Color.black;
			}
		}

		g2.setColor(fillColor);
		g2.fill(node);

		if(!simpleMode){
			g2.setColor(drawColor);
			g2.draw(node);
			if(node.isAccept()&&!node.dummy){
				double r = node.getRadius()-2.0;
				g2.draw(new RoundRectangle2D.Double(node.getX()-r,node.getY()-r,r*2,r*2,r*2,r*2));
			}
			if(showIdMode){
				g2.drawString(node.id+"",(int)(node.getX()),(int)(node.getY()));
			}
		}
	}

	private void drawSelectNode(Graphics2D g2, StateNode node){

		// 遷移元の描画
		for(StateTransition f : node.getFromTransitions()){
			if(!f.from.dummy){
				drawTransition(g2, f, Color.BLUE);
			}else{
				if(!simpleMode){
					drawDummyCurve(g2, f.from, Color.BLUE);
				}else{
					while(f.from.dummy){
						f = f.from.getFromTransition();
						drawTransition(g2, f, Color.BLUE);
					}
				}
			}
			if(node.dummy){
				drawTransition(g2, f, Color.GRAY);
			}
		}

		// 遷移先の描画
		for(StateTransition t : node.getToTransitions()){
			StateTransition f = t.to.getToTransition(node);
			if(!t.to.dummy){
				drawTransition(g2,t,Color.RED);
			}else{
				if(!simpleMode){
					drawDummyCurve(g2, t.to, Color.RED);
				}else{
					while(t.to.dummy){
						t = t.to.getToTransition();
						drawTransition(g2,t,Color.RED);
					}
				}
			}
			if(node.dummy){
				drawTransition(g2, t, Color.GRAY);
			}
			if(f!=null){
				drawTransition(g2,f,Color.RED);
			}
		}

		// 状態の描画
		drawNode(g2, node, node.getColor(), Color.RED);

		/*
		// 遷移元の表示
		g2.setColor(Color.BLUE);
		//for(StateNode from : node.getFromNodes()){
		//	drawTransition(g2,from.getTransition(node),Color.BLUE);
		//}

		for(StateNode from : node.getFromNodes()){
			StateNode to = node;
			if(from==to){ continue; }
			if(from.dummy){
				while(from.dummy){
					to = from;
					from = from.getFromNodes().get(0);
				}
				while(to.dummy){
					drawNodeLine(g2,from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),to.getRadius());
					from = to;
					to = to.getToNodes().get(0);
				}
			}
			if(!simpleMode){
				drawNodeArrow(g2,from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),to.getRadius(),5);
			}else{
				drawLine(g2,from.getX(),from.getY(),to.getX(),to.getY());
			}
		}


		// 遷移先の表示
		g2.setColor(Color.RED);
		//for(StateTransition t : node.getTransition()){
		//	drawTransition(g2,t,Color.RED);
		//}

		for(StateNode to : node.getToNodes()){
			StateNode from = node;
			if(to.dummy){
				while(to.dummy){
					drawNodeLine(g2,from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),to.getRadius());
					from = to;
					to = to.getToNodes().get(0);
				}
			}
			if(to==from){
				drawSelfArrow(g2,from);
			}else{
				if(!simpleMode){
					drawNodeArrow(g2,from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),to.getRadius(),5);
				}else{
					drawLine(g2,from.getX(),from.getY(),to.getX(),to.getY());
				}
				//戻り矢印があるかチェック
				for(StateNode t : to.getToNodes()){
					if(t==from){
						if(!simpleMode){
							drawNodeArrow(g2,to.getX(),to.getY(),to.getRadius(),from.getX(),from.getY(),from.getRadius(),5);
						}else{
							drawLine(g2,to.getX(),to.getY(),from.getX(),from.getY());
						}
						break;
					}
				}
			}

		}

		g2.setColor(node.getColor());
		g2.fill(node);
		g2.setColor(Color.RED);
		g2.draw(node);
		*/
	}

	private void drawSelectTransition(Graphics2D g2, StateTransition trans){

		drawTransition(g2, trans, Color.RED);

		// 状態の描画
		drawNode(g2, trans.from, trans.from.getColor(), Color.BLUE);
		drawNode(g2, trans.to, trans.to.getColor(), Color.RED);
	}

	private void drawSelfArrow(Graphics2D g2,StateNode node){
		double radius = node.getRadius();
		drawArc(g2,node.getX()-radius*2+1,node.getY()-radius*2+1,radius*2-1,radius*2-1,0,270);
		drawLine(g2,node.getX()-radius-1,node.getY(),node.getX()-radius-1,node.getY()-3);
		drawLine(g2,node.getX()-radius-1,node.getY(),node.getX()-radius-3,node.getY()+1);
	}

	private void drawNodeArrow(Graphics2D g2,double x1,double y1,double r1,double x2,double y2,double r2,double a){
		double theta = Math.atan2((double)(y2-y1),(double)(x2-x1));

		double cos = Math.cos(theta);
		double sin = Math.sin(theta);

		double startX = x1+(r1+1)*cos;
		double startY = y1+(r1+1)*sin;
		double endX = x2-(r2+1)*cos;
		double endY = y2-(r2+1)*sin;

		double dts = (2.0 * Math.PI / 360.0) * 30;

		drawLine(g2,startX,startY,endX,endY);
		drawLine(g2,endX,endY,endX-a*Math.cos(theta-dts),endY-a*Math.sin(theta-dts));
		drawLine(g2,endX,endY,endX-a*Math.cos(theta+dts),endY-a*Math.sin(theta+dts));
	}

	private void drawNodeLine(Graphics2D g2,double x1,double y1,double r1,double x2,double y2,double r2){
		double theta = Math.atan2((double)(y2-y1),(double)(x2-x1));

		double cos = Math.cos(theta);
		double sin = Math.sin(theta);

		double startX = x1+r1*cos;
		double startY = y1+r1*sin;
		double endX = x2-r2*cos;
		double endY = y2-r2*sin;

		drawLine(g2,startX,startY,endX,endY);
	}

	private void drawLine(Graphics2D g2,double x1,double y1,double x2,double y2){
		if(zoom>2.0){
			//doubleライン
			g2.draw(new Line2D.Double(x1,y1,x2,y2));
		}else{
			//intライン
			g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
		}
	}

	private void drawArc(Graphics2D g2,double x,double y,double w,double h,double start,double extent){
		if(zoom>2.0){
			//doubleアーク
			g2.draw(new Arc2D.Double(x,y,w,h,start,extent,Arc2D.OPEN));
		}else{
			//intアーク
			g2.drawArc((int)x,(int)y,(int)w,(int)h,(int)start,(int)extent);
		}
	}

}
