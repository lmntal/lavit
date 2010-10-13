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

package lavit.stateviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import lavit.*;
import lavit.runner.LmntalRunner;
import lavit.stateviewer.controller.StateGenerationControlPanel;
import lavit.stateviewer.controller.StateNodeLabel;
import lavit.stateviewer.controller.StateRightMenu;
import lavit.stateviewer.worker.StateDynamicMover;
import lavit.stateviewer.worker.StateGraphAdjust2Worker;
import lavit.stateviewer.worker.StateGraphAdjust3Worker;
import lavit.stateviewer.worker.StateGraphAdjustWorker;
import lavit.stateviewer.worker.StateGraphDummyMixAdjustWorker;
import lavit.stateviewer.worker.StateGraphDummySmoothingWorker;
import lavit.stateviewer.worker.StateGraphExchangeWorker;
import lavit.stateviewer.worker.StateGraphGeneticAlgorithmWorker;
import lavit.stateviewer.worker.StateGraphRandomMoveWorker;
import lavit.stateviewer.worker.StateGraphSimpleMixAdjustWorker;
import lavit.stateviewer.worker.StateGraphStretchMoveWorker;
import lavit.stateviewer.worker.StatePainter;
import lavit.util.CommonFontUser;
import lavit.util.NodeYComparator;

public class StateGraphPanel extends JPanel implements MouseInputListener,MouseWheelListener,KeyListener,CommonFontUser{

	public StatePanel statePanel;

	private StateNodeSet drawNodes;
	private StateNodeSet rootDrawNodes;

	private double zoom;
	private double drawTime;

	private boolean simpleMode;
	private boolean hideBackEdgeMode;
	private boolean showIdMode;
	private boolean showRuleMode;
	private boolean showNoNameRuleMode;
	private boolean showDummyMode;

	private boolean cycleMode;
	private boolean searchMode;

	private ArrayList<StateNode> selectNodes;
	private boolean nodeSelected;
	private StateTransition selectTransition;

	private Point lastPoint;
	private Point startPoint;
	private boolean selectSquare;

	private StatePainter painter;
	private StateDynamicMover mover;

	private StateGenerationControlPanel generalControlPanel;
	private StateNodeLabel nodeLabel;

	private Font font;

	public StateGraphPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		setFocusable(true);
		setLayout(new BorderLayout());

		generalControlPanel = new StateGenerationControlPanel(this);
		generalControlPanel.setVisible(false);
		add(generalControlPanel, BorderLayout.NORTH);

		nodeLabel = new StateNodeLabel();
		nodeLabel.setVisible(false);
		add(nodeLabel, BorderLayout.SOUTH);

		selectNodes  = new ArrayList<StateNode>();

		painter = new StatePainter(this);
		painter.start();

		mover = new StateDynamicMover(this);
		mover.start();

		loadFont();
		FrontEnd.addFontUser(this);
	}

	public void init(StateNodeSet nodes){

		this.drawNodes = nodes;
		if(nodes.generation==0){
			this.rootDrawNodes = nodes;
		}

		this.selectNodes.clear();
		this.nodeSelected = false;
		this.selectTransition = null;

		this.zoom = 1.0;
		this.drawTime = 0.0;

		if(Env.get("SV_SIMPLE_MODE").equals("auto")){
			this.simpleMode = false;
		}else{
			this.simpleMode = Env.is("SV_SIMPLE_MODE");
		}
		this.hideBackEdgeMode = Env.is("SV_HIDEBACKEDGE");
		this.showIdMode = Env.is("SV_SHOWID");
		this.showRuleMode = Env.is("SV_SHOWRULE");
		this.showNoNameRuleMode = Env.is("SV_SHOWNONAMERULE");
		this.showDummyMode = Env.is("SV_SHOW_DUMMY");

		//this.cycleMode = false;
		this.searchMode = false;

		this.lastPoint = null;

		if(nodes.parentNode==null){
			generalControlPanel.setVisible(false);
		}else{
			generalControlPanel.setVisible(true);
			generalControlPanel.updateLabel(nodes);
		}

		autoCentering();
		setActive(true);

		/*
		if(Env.is("SV_AUTO_ADJUST_STARTUP")){
			if(drawNodes.size()<=Env.getInt("SV_AUTO_ADJUST_STARTUP_LIMIT")){
				FrontEnd.println("(StateViewer) Auto Adjust");
				simpleMixAdjust();
			}else{
				FrontEnd.println("(StateViewer) "+drawNodes.size()+" state > Auto Adjust Limit ("+Env.getInt("SV_AUTO_ADJUST_STARTUP_LIMIT")+" state)");
				FrontEnd.sleep(300);
			}
		}
		*/
	}

	public void init(StateNodeSet nodes, StateNode selectNode){
		init(nodes);
		if(nodes.getAllNode().contains(selectNode)){
			selectNodes.add(selectNode);
		}
	}

	public void generationReset(){
		init(rootDrawNodes);
	}

	public void generationUp(){
		if(drawNodes.parentNode!=null){
			init(drawNodes.parentNode.parentSet, drawNodes.parentNode);
		}
	}

	public void update(){
		painter.update();
	}

	public void setActive(boolean active){
		if(painter.isActive()==active){ return; }
		if(active){
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			addKeyListener(this);
		}else{
			removeMouseListener(this);
			removeMouseMotionListener(this);
			removeMouseWheelListener(this);
			removeKeyListener(this);
		}
		painter.setActive(active);
		mover.setActive(active&& Env.is("SV_DYNAMIC_MOVER"));

		statePanel.stateControlPanel.setEnabled(active);
		if(active){
			update();
		}else{
			repaint();
		}
	}

	public void setDynamicMoverActive(boolean active){
		mover.setActive(active);
	}

	public StateDynamicMover getDynamicMover(){
		return mover;
	}

	public void loadFont(){
		font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE")-4);
		update();
		revalidate();
	}

	public void setInnerZoom(double zoom){
		if(zoom<0.0001){ zoom=0.0001; }else if(zoom>4.0){ zoom=4.0; }

		double w = (double)getWidth() / 2;
		double h = (double)getHeight() / 2;
		double newW = w / zoom;
		double newH = h / zoom;
		double oldW = w / this.zoom;
		double oldH = h / this.zoom;

		drawNodes.allMove(newW-oldW,newH-oldH);

		this.zoom = zoom;
		if(Env.get("SV_SIMPLE_MODE").equals("auto")){
			this.simpleMode = zoom<=0.7;
		}
	}

	public void changeZoom(double dz){
		double z = Math.sqrt(zoom*10000);
		z -= dz;
		if(z<0){ z=0; }
		setZoom(z*z/10000.0);
		update();
	}

	public void setZoom(double zoom){
		setInnerZoom(zoom);
		statePanel.stateControlPanel.setSliderPos(zoom);
	}

	public double getZoom(){
		return this.zoom;
	}

	public double getDrawTime(){
		return this.drawTime;
	}

	public int getDepth(){
		if(drawNodes==null) return 0;
		return drawNodes.getDepth();
	}

	public int getAllNum(){
		if(drawNodes==null) return 0;
		return drawNodes.size()-drawNodes.getDummySize();
	}

	public int getEndNum(){
		if(drawNodes==null) return 0;
		return drawNodes.getEndNode().size();
	}

	public ArrayList<StateNode> getSelectNodes(){
		return selectNodes;
	}

	public StateNodeSet getDrawNodes(){
		return drawNodes;
	}

	public void setCycleMode(boolean cycleMode){
		this.cycleMode = cycleMode;
	}

	public void setSearchMode(boolean searchMode){
		this.searchMode = searchMode;
	}

	public void setSimpleMode(boolean simpleMode){
		this.simpleMode = simpleMode;
	}

	public void setHideBackEdgeMode(boolean hideBackEdgeMode){
		this.hideBackEdgeMode = hideBackEdgeMode;
	}

	public void setShowIdMode(boolean showIdMode){
		this.showIdMode = showIdMode;
	}

	public void setShowRuleMode(boolean showRuleMode){
		this.showRuleMode = showRuleMode;
	}

	public void setShowNoNameRuleMode(boolean showNoNameRuleMode){
		this.showNoNameRuleMode = showNoNameRuleMode;
	}

	public void setShowDummyMode(boolean showDummyMode){
		this.showDummyMode = showDummyMode;
	}

	/*
	void oldPositionReset(){
		double w = (double)getWidth();
		double h = (double)getHeight();

		setCenterZoom(1.0);
		xPosInterval = w/(drawNodes.getDepth()+1);
		yPosInterval = new double[drawNodes.getDepth()];
		for(int i=0;i<drawNodes.getDepth();++i){
			yPosInterval[i] = h/(drawNodes.getSameDepthSize(i)+1);
		}

		for(StateNode node : drawNodes.getAllNode()){
			node.resetLocation(xPosInterval,yPosInterval);
		}
		update();
	}

	void oldLengthReset(){
		double w = (double)getWidth();
		double h = (double)getHeight();

		double minLength = w/(drawNodes.getDepth()+1);
		for(int i=0;i<drawNodes.getDepth();++i){
			double d = h/(drawNodes.getSameDepthSize(i)+1);
			if(d<minLength) minLength = d;
		}
		setCenterZoom(minLength/30);
		xPosInterval = w/(drawNodes.getDepth()+1);
		xPosInterval /= getZoom();
		yPosInterval = new double[drawNodes.getDepth()];
		for(int i=0;i<drawNodes.getDepth();++i){
			yPosInterval[i] = h/(drawNodes.getSameDepthSize(i)+1);
			yPosInterval[i] /= getZoom();
		}

		for(StateNode node : drawNodes.getAllNode()){
			node.resetLocation(xPosInterval,yPosInterval);
		}
		update();
	}
	*/

	public void dummyMixAdjust(){
		(new StateGraphDummyMixAdjustWorker(this)).selectExecute();
	}

	public void simpleMixAdjust(){
		(new StateGraphSimpleMixAdjustWorker(this)).selectExecute();
	}

	public void positionReset(){
		drawNodes.positionReset();
	}

	public void adjustReset(){
		(new StateGraphAdjustWorker(this)).selectExecute();
	}

	public void adjust2Reset(){
		(new StateGraphAdjust2Worker(this)).selectExecute();
	}

	public void adjust3Reset(){
		(new StateGraphAdjust3Worker(this)).selectExecute();
	}

	public void dummySmoothing(){
		(new StateGraphDummySmoothingWorker(this)).selectExecute();
	}

	public void exchangeReset(){
		(new StateGraphExchangeWorker(this)).selectExecute();
	}

	public void geneticAlgorithmLength(){
		StateGraphGeneticAlgorithmWorker worker = new StateGraphGeneticAlgorithmWorker(this);
		worker.ready();
		worker.execute();
	}

	public void stretchMove(){
		StateGraphStretchMoveWorker worker = new StateGraphStretchMoveWorker(this);
		worker.ready();
		worker.execute();
	}

	public void randomMove(){
		StateGraphRandomMoveWorker worker = new StateGraphRandomMoveWorker(this);
		worker.ready();
		worker.execute();
	}

	void groupMove(){
		for(int i=0;i<5;++i){
			for(StateNode node : drawNodes.getAllNode()){
				double y=0;
				int count=0;
				for(StateNode to : node.getToNodes()){
					y+=to.getY();
					++count;
				}
				for(StateNode from : node.getFromNodes()){
					y+=from.getY();
					++count;
				}
				node.setPosition(node.getX(),y/count);
			}
		}
		update();
	}

	public void startMover(){
		mover.setActive(!mover.isActive());
	}

	public void autoCentering(){
		zoomCentering();
		moveCentering();
		update();
	}

	private void zoomCentering(){
		Rectangle2D.Double d = drawNodes.getNodesDimension();

		double zoomX = getWidth()/d.width;
		double zoomY = getHeight()/d.height;

		if(zoomX<zoomY){
			setZoom(zoomX);
		}else{
			setZoom(zoomY);
		}
	}

	private void moveCentering(){
		drawNodes.resetMovePosition();
		Rectangle2D.Double d = getNodesWindowDimension();
		drawNodes.allMove(((getWidth()-d.width)/2.0)/zoom,((getHeight()-d.height)/2.0)/zoom);
	}


	/*
	public void dummyCentering(){
		ArrayList<ArrayList<StateNode>> depthNode = drawNodes.getDepthNode();
		for(ArrayList<StateNode> nodes : depthNode){
			ArrayList<StateNode> ns = new ArrayList<StateNode>();
			StateNode startNode = null;

			nodes = new ArrayList<StateNode>(nodes);
			Collections.sort(nodes, new NodeYComparator());

			for(StateNode node : nodes){
				if(node.dummy){
					ns.add(node);
				}else{
					if(ns.size()>0){
						if(startNode==null){
							double startY = node.getY()-node.getRadius();
							double intarval = 15;
							for(int i=0;i<ns.size();++i){
								ns.get(i).setY(startY+intarval*(i-ns.size()));
							}
						}else{
							double startY = startNode.getY()+startNode.getRadius();
							double endY = node.getY()-node.getRadius();
							double intarval = (endY-startY)/(double)(ns.size()+1);
							for(int i=0;i<ns.size();++i){
								ns.get(i).setY(startY+intarval*(i+1));
							}
						}
						ns.clear();
					}
					startNode = node;
				}
			}

			if(startNode!=null){
				double startY = startNode.getY()+startNode.getRadius();
				double intarval = 15;
				for(int i=0;i<ns.size();++i){
					ns.get(i).setY(startY+intarval*(i+1));
				}
			}

		}
		update();
	}
	*/

	public Rectangle2D.Double getNodesWindowDimension(){
		Rectangle2D.Double d = drawNodes.getNodesDimension();
		d.x *= zoom;
		d.y *= zoom;
		d.width *= zoom;
		d.height *= zoom;
		return d;
	}

	public int stateFind(String str){
		selectClear();
		drawNodes.setAllWeak(true);

		if(str.equals("")){
			searchReset();
			return 0;
		}

		HashSet<StateNode> hits = new HashSet<StateNode>();

		//検索
		for(StateNode node : new LinkedList<StateNode>(drawNodes.getAllNode())){
			if(node.isMatch(str)){
				hits.add(node);
			}
		}

		//処理
		for(StateNode node : hits){
			drawNodes.setLastOrder(node);
			node.weak = false;
			for(StateTransition t : node.getToTransitions()){
				if(hits.contains(t.to)){
					drawNodes.setLastOrder(t);
					t.weak = false;
				}
			}
		}

		drawNodes.updateNodeLooks();
		setSearchMode(true);
		update();
		return hits.size();
	}

	public int stateMatch(String head,String guard){
		selectClear();
		drawNodes.setAllWeak(true);

		if(head.equals("")){
			searchReset();
			return 0;
		}

		HashSet<StateNode> hits = new HashSet<StateNode>();

		File f = new File("temp.lmn");
		try {
			FileWriter fp = new FileWriter(f);
			fp.write(drawNodes.getMatchFileString(head,guard));
            fp.close();
		} catch (IOException e) {
			FrontEnd.printException(e);
		}

		final LmntalRunner lr = new LmntalRunner("",f);
		lr.setBuffering(true);
		lr.run();

		while(lr.isRunning()){
			FrontEnd.sleep(200);
		}
		if(lr.isSuccess()){
			String str = lr.getBufferString();
			int sp=str.indexOf("sVr_matches{");
			if(sp==-1){ return -1; }
			int ep=str.indexOf("}",sp+12);
			String ids = str.substring(sp+12,ep);
			if(ids.length()>0){
				String[] strs = ids.split(",");
				for(String s: strs){
					try{
						long id = Long.parseLong(s.substring(s.indexOf("(")+1,s.indexOf(")")));
						StateNode node = drawNodes.get(id);
						hits.add(node);
					}catch(Exception e){
						searchReset();
						return -1;
					}
				}
			}
		}else{
			searchReset();
			return -1;
		}

		//処理
		for(StateNode node : hits){
			drawNodes.setLastOrder(node);
			node.weak = false;
			for(StateTransition t : node.getToTransitions()){
				if(hits.contains(t.to)){
					drawNodes.setLastOrder(t);
					t.weak = false;
				}
			}
		}

		drawNodes.updateNodeLooks();
		setSearchMode(true);
		update();
		return hits.size();
	}

	public void searchShortCycle(){
		drawNodes.updateShortCycle();
		update();
	}

	/*
	public void searchShortCycle(){


		ArrayList<StateNode> cycles = new ArrayList<StateNode>();
		StateNode node = drawNodes.getStartNodeOne();
		for(StateNode sn : drawNodes.getStartNode()){
			if(sn.cycle){
				node = sn;
				break;
			}
		}
		StateNode cyclestart = null;
		boolean loop = false;
		while(node != null){
			if(cycles.contains(node)){
				cyclestart = node;
				loop = true;
				break;
			}
			if(node.cycle){ cycles.add(node); }
			node = node.getEmToNode();
		}
		if(cycles.size()==0){ return; }
		if(cyclestart==null){
			cyclestart = cycles.get(cycles.size()-1);
		}

		//ループしているノードの探索
		ArrayList<StateNode> loopNodes = new ArrayList<StateNode>();
		if(loop){
			node = cyclestart.getEmToNode();
			while(node != cyclestart){
				loopNodes.add(node);
				node = node.getEmToNode();
			}
		}
		loopNodes.add(cyclestart);


		if(loop){
			//ループを縮小
			while(true){
				ArrayList<StateNode> newLoopNodes = new ArrayList<StateNode>();
				for(StateNode n : loopNodes){ newLoopNodes.add(n); }
				m : for(int i=0;i<newLoopNodes.size();++i){
					StateNode n = newLoopNodes.get(i);
					int size = newLoopNodes.size();
					for(int j=(i+2)%size;j!=i;){
						if(n.getToNodes().contains(newLoopNodes.get(j))){
							ArrayList<StateNode> removeLoopNodes = new ArrayList<StateNode>();
							for(int k=(i+1)%size;k!=j;){
								removeLoopNodes.add(newLoopNodes.get(k));
								k = (k+1)%size;
							}
							for(StateNode m : removeLoopNodes){
								newLoopNodes.remove(m);
							}
							break m;
						}
						j = (j+1)%size;
					}
				}
				if(loopNodes.size()==newLoopNodes.size()){ break; }
				boolean hit = false;
				for(StateNode n : newLoopNodes){
					if(n.accept){
						hit = true;
						break;
					}
				}
				if(hit){
					loopNodes = newLoopNodes;
				}else{
					break;
				}
			}

			//ループ内の強調を更新
			for(int i=0;i<loopNodes.size();++i){
				StateNode n = loopNodes.get(i);
				StateNode t = loopNodes.get((i+1)%loopNodes.size());
				n.resetEmToNode();
				n.setCycleToNode(t,true);
			}

			//ループの入り口を更新
			for(StateNode n : loopNodes){
				if(n.depth<cyclestart.depth){
					cyclestart = n;
				}
			}
		}

		//ループ中以外は不受理化
		for(StateNode n : new LinkedList<StateNode>(drawNodes.getAllNode())){
			if(loopNodes.contains(n)){
				n.cycle = true;
				n.weak = false;
				drawNodes.setLastOrder(n);
			}else{
				n.cycle = false;
				n.weak = true;
				n.resetEmToNode();
			}
			n.updateLooks();
		}

		//ループの入口から初期状態まで最短で受理化
		StateNode tn = cyclestart;
		node = cyclestart.getOneFromNode();
		while(node!=null){
			node.cycle = true;
			node.weak = false;
			node.setCycleToNode(tn,true);
			node.updateLooks();
			drawNodes.setLastOrder(node);
			tn = node;
			node = node.getOneFromNode();
		}

		update();

	}
	*/

	public void searchReset(){
		drawNodes.setAllWeak(false);
		drawNodes.updateNodeLooks();
		setSearchMode(false);
		update();
	}

	public void emBackNodes(ArrayList<StateNode> nodes){
		ArrayList<StateNode> weaks = new ArrayList<StateNode>(drawNodes.getAllNode());
		drawNodes.allNodeUnMark();

		LinkedList<StateNode> queue = new LinkedList<StateNode>();

		for(StateNode n : nodes){
			n.mark();
			queue.add(n);
			weaks.remove(n);
		}

		while(!queue.isEmpty()){
			StateNode node = queue.remove();
			for(StateNode n : node.getFromNodes()){
				if(n.isMarked()||n.depth!=node.depth-1){continue;}
				n.mark();
				queue.add(n);
				weaks.remove(n);
			}
		}
		for(StateNode node : weaks){ node.weak = true; node.updateLooks(); }
		update();
	}

	public void emFromNodes(ArrayList<StateNode> nodes){
		ArrayList<StateNode> weaks = new ArrayList<StateNode>(drawNodes.getAllNode());
		drawNodes.allNodeUnMark();

		LinkedList<StateNode> queue = new LinkedList<StateNode>();

		for(StateNode n : nodes){
			n.mark();
			queue.add(n);
			weaks.remove(n);
		}

		while(!queue.isEmpty()){
			StateNode node = queue.remove();
			for(StateNode n : node.getFromNodes()){
				if(n.isMarked()){continue;}
				n.mark();
				queue.add(n);
				weaks.remove(n);
			}
		}
		for(StateNode node : weaks){ node.weak = true; node.updateLooks(); }
		update();
	}

	public void emNextNodes(ArrayList<StateNode> nodes){
		ArrayList<StateNode> weaks = new ArrayList<StateNode>(drawNodes.getAllNode());
		drawNodes.allNodeUnMark();

		LinkedList<StateNode> queue = new LinkedList<StateNode>();

		for(StateNode n : nodes){
			n.mark();
			queue.add(n);
			weaks.remove(n);
		}

		while(!queue.isEmpty()){
			StateNode node = queue.remove();
			for(StateNode n : node.getToNodes()){
				if(n.isMarked()||node.depth+1!=n.depth){continue;}
				n.mark();
				queue.add(n);
				weaks.remove(n);
			}
		}
		for(StateNode node : weaks){ node.weak = true; node.updateLooks(); }
		update();
	}

	public void emToNodes(ArrayList<StateNode> nodes){
		ArrayList<StateNode> weaks = new ArrayList<StateNode>(drawNodes.getAllNode());
		drawNodes.allNodeUnMark();

		LinkedList<StateNode> queue = new LinkedList<StateNode>();

		for(StateNode n : nodes){
			n.mark();
			queue.add(n);
			weaks.remove(n);
		}

		while(!queue.isEmpty()){
			StateNode node = queue.remove();
			for(StateNode n : node.getToNodes()){
				if(n.isMarked()){continue;}
				n.mark();
				queue.add(n);
				weaks.remove(n);
			}
		}
		for(StateNode node : weaks){ node.weak = true; node.updateLooks(); }
		update();
	}

	public void allDelete(){
		setActive(false);
		statePanel.stateControlPanel.updateInfo();
		repaint();
	}

	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(font);

		//フレームの初期化
		g2.setColor(Color.white);
		g2.fillRect(0, 0, getWidth(), getHeight());

		if(!painter.isActive()){ return; }

		double startTime = System.currentTimeMillis();
		//this.simpleMode = zoom<=0.7;

		//描写対象の決定
		double minX=-10/zoom,maxX=(getWidth()+10)/zoom;
		double minY=-10/zoom,maxY=(getHeight()+10)/zoom;
		for(StateNode node : drawNodes.getAllNode()){
			node.setInFrame(false);
			if(minX<node.getX()&&node.getX()<maxX&&minY<node.getY()&&node.getY()<maxY){
				node.setInFrame(true);
			}
		}

		if(Env.is("SV_VERTICAL_VIEW")){ g2.rotate(90 * Math.PI/180,getWidth()/2, getHeight()/2); }
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
				double y = (getHeight()/zoom)*((double)i/(double)(outFrom.size()+2));
				for(StateNode to : outFrom.get(from)){
					drawNodeArrow(g2,0,y,0,to.getX(),to.getY(),to.getRadius(),5);
				}
				i++;
			}
			i=2;
			for(StateNode to : outTo.keySet()){
				double y = (getHeight()/zoom)*((double)i/(double)(outTo.size()+2));
				for(StateNode from : outTo.get(to)){
					drawNodeArrow(g2,from.getX(),from.getY(),from.getRadius(),getWidth()/zoom,y,0,5);
				}
				i++;
			}

		}


		//for(StateNode node : drawNodes.getAllNodeDrawOrder()){
		//	drawArrow(g2,node);
		//}

		/*
		{
			LinkedList<StateNode> queue = new LinkedList<StateNode>();
			drawNodes.allNodeUnMark();

			queue.add(drawNodes.getStartNode());
			drawNodes.getStartNode().mark();

			while(!queue.isEmpty()){

			}
		}
		*/

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
		for(StateNode node : selectNodes){
			drawSelectNode(g2,node);
		}
		nodeLabel.setNode(selectNodes);
		validate();

		//選択トランジションの描画
		if(selectTransition!=null){
			drawSelectTransition(g2, selectTransition);
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
		if(selectSquare&&lastPoint!=null&&startPoint!=null){
			Point p1 = new Point((int)((double)lastPoint.getX()/zoom), (int)((double)lastPoint.getY()/zoom));
			Point p2 = new Point((int)((double)startPoint.getX()/zoom), (int)((double)startPoint.getY()/zoom));

			g2.setColor(Color.RED);
			g2.drawRect(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.max(p1.x, p2.x)-Math.min(p1.x, p2.x), Math.max(p1.y, p2.y)-Math.min(p1.y, p2.y));
		}

		g2.scale(1.0/zoom, 1.0/zoom);
		if(Env.is("SV_VERTICAL_VIEW")){ g2.rotate(-90 * Math.PI/180,getWidth()/2, getHeight()/2); }

		drawTime = System.currentTimeMillis()-startTime;
		statePanel.stateControlPanel.updateInfo();

		//System.out.println("SV  _PAINT="+System.currentTimeMillis());

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

	/*
	void reduction(){
		drawNodes.reduction();
		update();
	}
	*/

	public void shakeMove(){
		for(int i=0;i<50;++i){
			for(StateNode node : drawNodes.getAllNode()){
				double newX = (Math.random()-0.5)*20.0 + node.getX();
				double newY = (Math.random()-0.5)*20.0 + node.getY();
				double nowMinLength = drawNodes.getMinLength(node.id,node.getX(),node.getY());
				double newMinLength = drawNodes.getMinLength(node.id,newX,newY);
				if(nowMinLength<24&&nowMinLength<newMinLength){
					node.setPosition(newX, newY);
				}else if(newMinLength>=24){
					double nowLinkLength = drawNodes.getLinkLength(node,node.getX(),node.getY());
					double newLinkLength = drawNodes.getLinkLength(node,newX,newY);
					if(newLinkLength<nowLinkLength){
						node.setPosition(newX, newY);
					}
				}
			}
		}
		autoCentering();
	}

	public void selectNodeAbstraction(){
		if(selectNodes.size()<=1){ return; }
		ArrayList<StateNode> groupNodes = new ArrayList<StateNode>();
		for(StateNode node : selectNodes){
			if(!node.dummy){ groupNodes.add(node); }
		}

		StateAbstractionMaker maker = new StateAbstractionMaker(this);
		maker.makeNode(groupNodes);
		maker.end();
	}

	public void selectClear(){
		selectNodes.clear();
		nodeSelected = false;
		selectTransition = null;
	}

	public boolean isDragg(){
		if(lastPoint==null){
			return false;
		}else{
			return true;
		}
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		requestFocus();

		lastPoint = e.getPoint();
		startPoint = e.getPoint();

		Point p = new Point((int)((double)e.getX()/zoom), (int)((double)e.getY()/zoom));


		//ノードの選択

		//ある程度拡大していないと選択できない
		StateNode selectNode = null;
		if(zoom>0.3){
			selectNode = drawNodes.pickANode(p);
		}

		if(e.isControlDown()){
			if(selectNode!=null){
				if(!selectNodes.contains(selectNode)){
					selectNodes.add(selectNode);
					nodeSelected = true;
				}else{
					selectNodes.remove(selectNode);
					nodeSelected = false;
				}
			}else{
				nodeSelected = false;
			}
		}else{
			if(selectNode!=null){
				if(!selectNodes.contains(selectNode)){
					selectNodes.clear();
					selectNodes.add(selectNode);
				}else if(e.getClickCount()==2){
					if(e.isShiftDown()){
						selectNode.debugFrame(this);
					}else{
						selectNode.doubleClick(this);
					}
				}
				nodeSelected = true;
			}else{
				selectNodes.clear();
				nodeSelected = false;
			}
		}


		//トランジションの選択
		if(selectNode==null&&selectNodes.size()==0){
			if(zoom>0.3){
				selectTransition = drawNodes.pickATransition(p);
			}else{
				selectTransition = null;
			}
		}else{
			selectTransition = null;
		}

		//右クリック制御
		if(SwingUtilities.isRightMouseButton(e)){
			(new StateRightMenu(this)).show(e.getComponent(), e.getX(), e.getY());
		}
		update();
	}

	public void mouseReleased(MouseEvent e) {
		lastPoint = null;
		startPoint = null;
		selectSquare = false;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		update();
	}

	public void mouseDragged(MouseEvent e) {
		if(lastPoint == null){
			lastPoint = e.getPoint();
			return;
		}
		if(startPoint == null){
			startPoint = e.getPoint();
			selectSquare = false;
			return;
		}
		double dx = (double)(e.getX() - lastPoint.x) / zoom;
		double dy = (double)(e.getY() - lastPoint.y) / zoom;

		if(nodeSelected){
			if(!e.isAltDown()){ dx = 0; }
			if(e.isAltDown()&&e.isControlDown()&&selectNodes.size()==1){
				for(StateNode node : selectNodes){
					accompany_move(node,dx,dy);
				}
			}else{
				for(StateNode node : selectNodes){
					node.move(dx, dy);
				}
			}
		}else if(selectTransition!=null){
			if(!e.isAltDown()){ dx = 0; }
			selectTransition.from.move(dx, dy);
			if(selectTransition.from!=selectTransition.to){
				selectTransition.to.move(dx, dy);
			}
		}else{

			if(e.isControlDown()){
				Rectangle2D.Double d = getNodesWindowDimension();

/*
				double posX,posY;
				if(e.getX()<d.getCenterX()){
					if(e.getY()<d.getCenterY()){
						posX = d.getMaxX();
						posY = d.getMaxY();
					}else{
						posX = d.getMaxX();
						posY = d.getMinY();
					}
				}else{
					if(e.getY()<d.getCenterY()){
						posX = d.getMinX();
						posY = d.getMaxY();
					}else{
						posX = d.getMinX();
						posY = d.getMinY();
					}
				}
				double scaleX = Math.abs(((double)e.getX()-posX)/((double)lastPoint.x-posX));
				double scaleY = Math.abs(((double)e.getY()-posY)/((double)lastPoint.y-posY));
*/

				double posX,posY;
				if(startPoint.x<d.getCenterX()){
					if(startPoint.y<d.getCenterY()){
						posX = d.getMaxX();
						posY = d.getMaxY();
					}else{
						posX = d.getMaxX();
						posY = d.getMinY();
					}
				}else{
					if(startPoint.y<d.getCenterY()){
						posX = d.getMinX();
						posY = d.getMaxY();
					}else{
						posX = d.getMinX();
						posY = d.getMinY();
					}
				}
				double scaleX = Math.abs(((double)e.getX()-posX)/((double)lastPoint.x-posX));
				double scaleY = Math.abs(((double)e.getY()-posY)/((double)lastPoint.y-posY));

				drawNodes.allScaleCenterMove(scaleX,scaleY);

				/*
				double posX,posY,scaleX,scaleY;
				if(lastPoint.x<d.getMinX()){
					if(lastPoint.y<d.getMinY()){
						posX = d.getMaxX();
						posY = d.getMaxY();
						scaleX = ((double)e.getX()-posX)/((double)lastPoint.x-posX);
						scaleY = ((double)e.getY()-posY)/((double)lastPoint.y-posY);
					}else{
						posX = d.getMaxX();
						posY = d.getMinY();
						scaleX = ((double)e.getX()-posX)/((double)lastPoint.x-posX);
						scaleY = ((double)e.getY()-posY)/((double)lastPoint.y-posY);
					}
				}else{
					if(lastPoint.y<d.getMinY()){
						posX = d.getMinX();
						posY = d.getMaxY();
						scaleX = ((double)e.getX()-posX)/((double)lastPoint.x-posX);
						scaleY = ((double)e.getY()-posY)/((double)lastPoint.y-posY);
					}else{
						posX = d.getMinX();
						posY = d.getMinY();
						scaleX = ((double)e.getX()-posX)/((double)lastPoint.x-posX);
						scaleY = ((double)e.getY()-posY)/((double)lastPoint.y-posY);
					}
				}
				scaleX = Math.abs(scaleX);
				scaleY = Math.abs(scaleY);
				*/

			}else if(e.isShiftDown()){
				selectSquare = true;
				Point p1 = new Point((int)((double)lastPoint.getX()/zoom), (int)((double)lastPoint.getY()/zoom));
				Point p2 = new Point((int)((double)startPoint.getX()/zoom), (int)((double)startPoint.getY()/zoom));

				double minX = Math.min(p1.x, p2.x);
				double minY = Math.min(p1.y, p2.y);
				double maxX = Math.max(p1.x, p2.x);
				double maxY = Math.max(p1.y, p2.y);
				selectNodes.clear();
				for(StateNode node : drawNodes.getAllNode()){
					if(!showDummyMode&&node.dummy){ continue; }
					if(minX<node.getX()&&node.getX()<maxX&&minY<node.getY()&&node.getY()<maxY){
						selectNodes.add(node);
					}
				}

			}else{
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				drawNodes.allMove(dx, dy);
			}
		}
		lastPoint = e.getPoint();
		update();
	}

	private void accompany_move(StateNode node,double dx,double dy){
		drawNodes.allNodeUnMark();

		LinkedList<StateNode> now_queue = new LinkedList<StateNode>();
		LinkedList<StateNode> next_queue = new LinkedList<StateNode>();

		next_queue.add(node);

		int count=0;
		while(!next_queue.isEmpty()){
			now_queue.clear();
			for(StateNode n : next_queue){
				now_queue.add(n);
			}
			next_queue.clear();

			for(StateNode n : now_queue){
				if(n.isMarked()){ continue; }
				n.mark();
				n.move(dx, dy);

				for(StateNode to : n.getToNodes()){
					next_queue.add(to);
				}
				for(StateNode from : n.getFromNodes()){
					next_queue.add(from);
				}
			}
			dx /= 2;
			dy /= 2;
			count++;
			if(count>10){ break; }
		}
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		//double oldZoom = zoom;
		double dz = (double)e.getWheelRotation()*5;
		if(e.isControlDown()){
			dz /= 5;
		}
		changeZoom(dz);

		//double dx = (double)(e.getX() - getWidth()/2) / zoom;
		//double dy = (double)(e.getY() - getHeight()/2) / zoom;
		//drawNodes.allMove((oldZoom-zoom)*dx/2, (oldZoom-zoom)*dy/2);
	}


	public void keyPressed(KeyEvent e) {
		boolean isUpdate = false;
		double d = 5;
		double s = 1.1;
		if(e.isControlDown()){
			d /= 5;
			s -= 1.0;
			s /= 5;
			s += 1.0;
		}

		switch(e.getKeyCode()){
		case KeyEvent.VK_LEFT:
			if(selectNodes.size()==0){
				if(e.isControlDown()){
					drawNodes.allScaleCenterMove(1.0/s,1);
				}else{
					drawNodes.allMove(-d/zoom,0);
				}
			}else{
				for(StateNode node : selectNodes){
					node.move(-d/zoom,0);
				}
			}
			isUpdate = true;
			break;
		case KeyEvent.VK_RIGHT:
			if(selectNodes.size()==0){
				if(e.isControlDown()){
					drawNodes.allScaleCenterMove(s,1);
				}else{
					drawNodes.allMove(d/zoom,0);
				}
			}else{
				for(StateNode node : selectNodes){
					node.move(d/zoom,0);
				}
			}
			isUpdate = true;
			break;
		case KeyEvent.VK_DOWN:
			if(selectNodes.size()==0){
				if(e.isControlDown()){
					drawNodes.allScaleCenterMove(1,s);
				}else{
					drawNodes.allMove(0,d/zoom);
				}
			}else{
				for(StateNode node : selectNodes){
					node.move(0,d/zoom);
				}
			}
			isUpdate = true;
			break;
		case KeyEvent.VK_UP:
			if(selectNodes.size()==0){
				if(e.isControlDown()){
					drawNodes.allScaleCenterMove(1,1/s);
				}else{
					drawNodes.allMove(0,-d/zoom);
				}
			}else{
				for(StateNode node : selectNodes){
					node.move(0,-d/zoom);
				}
			}
			isUpdate = true;
			break;
		case KeyEvent.VK_DELETE:
			//for(StateNode node : selectNodes){
			//	drawNodes.remove(node);
			//}
			drawNodes.remove(selectNodes);
			selectClear();
			update();
			isUpdate = true;
			break;
		case KeyEvent.VK_SEMICOLON:
		case KeyEvent.VK_ADD:
		case KeyEvent.VK_PLUS:
			if(e.isControlDown()){
			changeZoom(-1);
			}
			break;
		case KeyEvent.VK_MINUS:
		case KeyEvent.VK_SUBTRACT:
			if(e.isControlDown()){
				changeZoom(1);
			}
			break;
		case KeyEvent.VK_0:
			if(e.isControlDown()){
				setZoom(1);
				update();
			}
			break;
		case KeyEvent.VK_SPACE:
			if(selectTransition!=null){
				selectTransition.separateTransition(drawNodes);
				selectTransition = null;
				update();
			}
			break;
		}
		if(isUpdate) update();
	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}


}
