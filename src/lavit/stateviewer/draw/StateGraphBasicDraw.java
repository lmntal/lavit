/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit.stateviewer.draw;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import lavit.Env;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;
import lavit.stateviewer.StateTransition;

public class StateGraphBasicDraw extends StateDraw {
	StateGraphPanel panel;

	Graphics2D g2;
	StateNodeSet drawNodes;
	private double zoom;

	public StateGraphBasicDraw(StateGraphPanel panel){
		this.panel = panel;
	}

	public void drawGraph(Graphics2D g2){
		this.g2 = g2;
		this.drawNodes = panel.getDrawNodes();
		this.zoom = panel.getZoom();

		//背景色
		g2.setColor(Color.white);
		g2.fillRect(0, 0, (int)(panel.getWidth()/zoom)+1, (int)(panel.getHeight()/zoom)+1);

		//描写対象の決定
		double minX=-10/zoom,maxX=(panel.getWidth()+10)/zoom;
		double minY=-10/zoom,maxY=(panel.getHeight()+10)/zoom;
		for(StateNode node : drawNodes.getAllNode()){
			node.setInFrame(false);
			if(minX<node.getX()&&node.getX()<maxX&&minY<node.getY()&&node.getY()<maxY){
				node.setInFrame(true);
			}
		}

		//初期状態の矢印の描写
		if(!Env.is("SV_SHOWOUTTRANS")||drawNodes.getAllOutTransition().size()==0){
			drawStartArrow();
		}

		//OUTTRANSの描画
		if(Env.is("SV_SHOWOUTTRANS")){
			drawOutTransition();
		}

		//線の描写
		boolean nocurve = Env.is("SV_NOCURVE");
		if(simpleMode){
			//すべて直線で描画
			for(StateTransition t : drawNodes.getAllTransition()){
				//if(Math.random()<0.5){ continue; }
				drawTransition(t, null);
			}
		}else{
			/*
			//ダミー以外を描画
			for(StateTransition t : drawNodes.getAllTransition()){
				if(t.from.dummy||t.to.dummy){ continue; }
				drawTransition(t, null);
				System.out.println(t);
			}
			System.out.println();
			//ダミーカーブ
			ArrayList<ArrayList<StateNode>> dummyGroups = getDummyGroups(drawNodes.getDepthNode());
			drawDummyCurve(dummyGroups, null);
			*/
			drawNodes.allNodeUnMark();
			for(StateTransition t : drawNodes.getAllTransition()){
				if(nocurve){
					drawTransition(t, null);
				}else if(t.from.dummy){
					drawDummyCurve(t.from, null);
				}else if(t.to.dummy){
					drawDummyCurve(t.to, null);
				}else{
					drawTransition(t, null);
				}
			}
		}

		//ノードの描写
		for(StateNode node : drawNodes.getAllNode()){
			//if(Math.random()<0.5){ continue; }
			drawNode(node, null, null);
		}

		//サイクルの優先描画
		drawNodes.allNodeUnMark();
		List<StateNode> cycleNode = drawNodes.getCycleNode();
		for(StateNode node : cycleNode){
			StateTransition t = node.getToCycleTransition();
			if(t!=null){
				if(simpleMode){
					drawTransition(t, null);
				}else if(t.from.dummy){
					drawDummyCurve(t.from, null);
				}else if(t.to.dummy){
					drawDummyCurve(t.to, null);
				}else{
					drawTransition(t, null);
				}
			}
		}
		for(StateNode node : cycleNode){
			drawNode(node, null, null);
		}

		//選択しているノードの描写
		for(StateNode node : panel.getSelectNodes()){
			drawSelectNode(node);
		}
		panel.updateNodeLabel();
		panel.validate();

		//選択トランジションの描画
		if(panel.getSelectTransition()!=null){
			drawSelectTransition(panel.getSelectTransition());
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
	}

	private void drawStartArrow(){
		if(drawNodes.getStartNode().size()!=1){ return; }

		StateNode node = drawNodes.getStartNodeOne();
		if(!node.isInFrame()){ return; }

		if(node.weak){
			g2.setColor(Color.lightGray);
		}else{
			g2.setColor(Color.black);
		}

		drawNodeArrow(node.getX()-30,node.getY(),node.getRadius(),node.getX()-7,node.getY(),node.getRadius(),5);
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

	private void drawTransition(StateTransition t, Color color){

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
						drawLine(from.getX(),from.getY(),to.getX(),to.getY());
					}else{
						drawNodeLine(from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),0);
					}
				}else{
					drawNodeArrow(from.getX(),from.getY(),from.getRadius(),to.getX(),to.getY(),to.getRadius(),5);
				}
			}else{
				drawSelfArrow(from);
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
				drawLine(from.getX(),from.getY(),to.getX(),to.getY());
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

	private void drawDummyCurve(ArrayList<ArrayList<StateNode>> dummyGroups,  Color color){
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
			drawDummyCurve(dummyGroup.get(0), null);
		}
	}

	//ダミーカーブの描画
	private void drawDummyCurve(StateNode dummy, Color color){
		//markされてる場合は描画しない
		if(dummy.isMarked()){ return; }

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
			n.mark();
			dummyGroup.add(n);
			n = n.getToNode();
		}

		//ダミーの中間点を作成
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		StateNode n0 = dummyGroup.get(0).getFromNode();
		boolean inFrame = n0.isInFrame();
		n = n0;
		for(StateNode node : dummyGroup){
			points.add(new Point2D.Double((n.getX()+node.getX())/2,(n.getY()+node.getY())/2));
			points.add(new Point2D.Double(node.getX(),node.getY()));
			inFrame |= node.isInFrame();
			n = node;
		}
		StateNode nN = n.getToNode();
		points.add(new Point2D.Double((n.getX()+nN.getX())/2,(n.getY()+nN.getY())/2));
		inFrame |= n.isInFrame();

		if(hideBackEdgeMode&&nN.depth<n0.depth){ return; }
		if(!inFrame){ return; }

		//パスの作成、直線、曲線描画
		GeneralPath p = new GeneralPath();
		Point2D fP = points.get(0);
		drawNodeLine(n0.getX(), n0.getY(), n0.getRadius(), fP.getX(), fP.getY(), 0);

		p.moveTo(fP.getX(), fP.getY());
		for(int i=1;(i+1)<points.size();i+=2){
			p.quadTo(points.get(i).getX(), points.get(i).getY(), points.get(i+1).getX(), points.get(i+1).getY());
		}
		g2.draw(p);

		//矢印の描画
		Point2D lP = points.get(points.size()-1);
		drawNodeArrow(lP.getX(), lP.getY(), 0, nN.getX(), nN.getY(), nN.getRadius(), 5);


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

	private void drawOutTransition(){
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
				drawNodeArrow(0,y,0,to.getX(),to.getY(),to.getRadius(),5);
			}
			i++;
		}
		i=2;
		for(StateNode to : outTo.keySet()){
			double y = (panel.getHeight()/zoom)*((double)i/(double)(outTo.size()+2));
			for(StateNode from : outTo.get(to)){
				drawNodeArrow(from.getX(),from.getY(),from.getRadius(),panel.getWidth()/zoom,y,0,5);
			}
			i++;
		}
	}

	private void drawNode(StateNode node, Color fillColor, Color drawColor){
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

	private void drawSelectNode(StateNode node){

		// 遷移元の描画
		drawNodes.allNodeUnMark();
		for(StateTransition f : node.getFromTransitions()){
			if(!f.from.dummy){
				drawTransition(f, Color.BLUE);
			}else{
				if(!simpleMode){
					drawDummyCurve(f.from, Color.BLUE);
				}else{
					while(f.from.dummy){
						f = f.from.getFromTransition();
						drawTransition(f, Color.BLUE);
					}
				}
			}
			if(node.dummy){
				drawTransition(f, Color.GRAY);
			}
		}

		// 遷移先の描画
		drawNodes.allNodeUnMark();
		for(StateTransition t : node.getToTransitions()){
			StateTransition f = t.to.getToTransition(node);
			if(!t.to.dummy){
				drawTransition(t,Color.RED);
			}else{
				if(!simpleMode){
					drawDummyCurve(t.to, Color.RED);
				}else{
					while(t.to.dummy){
						t = t.to.getToTransition();
						drawTransition(t,Color.RED);
					}
				}
			}
			if(node.dummy){
				drawTransition(t, Color.GRAY);
			}
			if(f!=null){
				drawTransition(f,Color.RED);
			}
		}

		// 状態の描画
		drawNode(node, node.getColor(), Color.RED);

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

	private void drawSelectTransition(StateTransition trans){

		drawTransition(trans, Color.RED);

		// 状態の描画
		drawNode(trans.from, trans.from.getColor(), Color.BLUE);
		drawNode(trans.to, trans.to.getColor(), Color.RED);
	}

	private void drawSelfArrow(StateNode node){
		double radius = node.getRadius();
		drawArc(node.getX()-radius*2+1,node.getY()-radius*2+1,radius*2-1,radius*2-1,0,270);
		drawLine(node.getX()-radius-1,node.getY(),node.getX()-radius-1,node.getY()-3);
		drawLine(node.getX()-radius-1,node.getY(),node.getX()-radius-3,node.getY()+1);
	}

	private void drawNodeArrow(double x1,double y1,double r1,double x2,double y2,double r2,double a){
		double theta = Math.atan2((double)(y2-y1),(double)(x2-x1));

		double cos = Math.cos(theta);
		double sin = Math.sin(theta);

		double startX = x1+(r1+1)*cos;
		double startY = y1+(r1+1)*sin;
		double endX = x2-(r2+1)*cos;
		double endY = y2-(r2+1)*sin;

		double dts = (2.0 * Math.PI / 360.0) * 30;

		drawLine(startX,startY,endX,endY);
		drawLine(endX,endY,endX-a*Math.cos(theta-dts),endY-a*Math.sin(theta-dts));
		drawLine(endX,endY,endX-a*Math.cos(theta+dts),endY-a*Math.sin(theta+dts));
	}

	private void drawNodeLine(double x1,double y1,double r1,double x2,double y2,double r2){
		double theta = Math.atan2((double)(y2-y1),(double)(x2-x1));

		double cos = Math.cos(theta);
		double sin = Math.sin(theta);

		double startX = x1+r1*cos;
		double startY = y1+r1*sin;
		double endX = x2-r2*cos;
		double endY = y2-r2*sin;

		drawLine(startX,startY,endX,endY);
	}

	private void drawLine(double x1,double y1,double x2,double y2){
		if(zoom>2.0){
			//doubleライン
			g2.draw(new Line2D.Double(x1,y1,x2,y2));
		}else{
			//intライン
			g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
		}
	}

	private void drawArc(double x,double y,double w,double h,double start,double extent){
		if(zoom>2.0){
			//doubleアーク
			g2.draw(new Arc2D.Double(x,y,w,h,start,extent,Arc2D.OPEN));
		}else{
			//intアーク
			g2.drawArc((int)x,(int)y,(int)w,(int)h,(int)start,(int)extent);
		}
	}

	public void setNodeLook(StateNode node){
		Shape shape;
		double radius;
		Color color;

		//色の決定
		double to = node.getToTransitions().size();
		double from = node.getFromTransitions().size();
		double r = 0;
		double g = 0;
		double b = 0;

		if(from<to){
			if(from*2<to){
				r = 0;
				g = 255*Math.sqrt(from*2/to);
				b = 255;
			}else if(from*2==to){
				r = 0;
				g = 255;
				b = 255;
			}else if(from*2>to){
				r = 0;
				g = 255;
				b = 255*Math.sqrt(to/from-1);
			}
		}else if(from==to){
			r = 0;
			g = 255;
			b = 0;
		}else if(from>to){
			if(from<to*2){
				r = 255*Math.sqrt(from/to-1);
				g = 255;
				b = 0;
			}else if(from==to*2){
				r = 255;
				g = 255;
				b = 0;
			}else if(from>to*2){
				r = 255;
				g = 255*Math.sqrt(to*2/from);
				b = 0;
			}
		}

		//色の設定
		if(node.dummy){
			color = Color.gray;
		}else{
			color = new Color((int)r,(int)g,(int)b);
		}

		//大きさの設定
		if(node.dummy){
			if(Env.is("SV_SHOW_DUMMY")){
				radius = 2.0;
			}else{
				radius = 0.0;
			}
		}else if(node.weak){
			radius = 3.0;
		}else{
			radius = 5.0;
		}

		//形の設定
		if(node.getChildSet()==null){
			shape = new RoundRectangle2D.Double(node.getX()-radius,node.getY()-radius,radius*2,radius*2,radius*2,radius*2);
		}else{
			shape = new RoundRectangle2D.Double(node.getX()-radius,node.getY()-radius,radius*2,radius*2,radius/2,radius/2);
		}

		node.setColor(color);
		node.setRadius(radius);
		node.setShape(shape);

		/*
		if(to<from){
			if(to*2<from){
				r = 255;
				g = 255*Math.sqrt(to*2/from);
			}else{
				g = 255;
				r = 255*Math.sqrt(to/from);
			}
		}else if(to>from){
			if(to>from*2){
				b = 255;
				g = 255*Math.sqrt(from*2/to);
			}else{
				g = 255;
				b = 255*Math.sqrt(from/to);
			}
		}else{
			g = 255;
		}
		 */

		/*
		if(from<to){
			r = 0;
			g = 255*Math.sqrt(from/to);
			b = 255*Math.sqrt(1-from/to);
		}else if(from==to){
			r = 0;
			g = 255;
			b = 0;
		}else if(from>to){
			r = 255*Math.sqrt(1-to/from);
			g = 255*Math.sqrt(to/from);
			b = 0;
		}
		 */

		/*
		switch(colorMode){
			case normal:
				radius = 5.0;
				fillColor = new Color((int)r,(int)g,(int)b);
				drawColor = Color.gray;
				break;
			case em:
				radius = 6.0;
				fillColor = new Color((int)r,(int)g,(int)b);
				drawColor = Color.black;
				break;
			case weak:
				radius = 4.0;
				fillColor = Color.white;
				drawColor = new Color((int)r,(int)g,(int)b);
				break;
			case dummy:
				radius = 0.0;
				fillColor = Color.black;
				drawColor = Color.black;
				break;
			case showdummy:
				radius = 3.0;
				fillColor = Color.white;
				drawColor = Color.gray;
				break;
		}
		 */
		/*
		if(toIds.size()==0){
			color = Color.red;
		}else if(toIds.size()==1){
			color = Color.blue;
		}else if(toIds.size()==2){
			color = Color.orange;
		}else if(toIds.size()==3){
			color = Color.green;
		}else if(toIds.size()==4){
			color = Color.yellow;
		}else{
			color = Color.cyan;
		}
		 */
	}

}
