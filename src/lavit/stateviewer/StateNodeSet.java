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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import lavit.Env;
import lavit.FrontEnd;

public class StateNodeSet {

	public boolean global;

	private HashMap<Long,StateNode> allNode = new HashMap<Long,StateNode>();
	private ArrayList<StateTransition> allTransition = new ArrayList<StateTransition>();

	private StateNode startNode;
	private ArrayList<StateNode> endNode =  new ArrayList<StateNode>();

	private ArrayList<ArrayList<StateNode>> depthNode = new ArrayList<ArrayList<StateNode>>();
	private ArrayList<StateNode> drawNodeOrder = new ArrayList<StateNode>();
	private ArrayList<StateTransition> drawTransitionOrder = new ArrayList<StateTransition>();
	private ArrayList<StateNode> dummyNode = new ArrayList<StateNode>();

	final String stateMarkString = "States";
	final String graphMarkString = "Transitions";
	final String initStartMarkString = "init:";
	final String labelMarkString = "Labels";

	public boolean setSlimNdResult(String str){
		this.global = true;

		HashMap<Long,TempNode> temps = new HashMap<Long,TempNode>();
		int line=0;
		TempNode startNode = null;

		String[] strs = str.split("\n");

		// States探し
		for(;line<strs.length;++line){
			if(strs[line].equals(stateMarkString)) break;
		}
		if(line>=strs.length) return false; //エラー

		int no = 0;
		for(line++;line<strs.length;++line){
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = Long.parseLong(ss[0]);
			TempNode node = new TempNode(id,no++,ss[1]);
			temps.put(id,node);
		}


		// Transitions探し
		for(;line<strs.length;++line){
			if(strs[line].equals(graphMarkString)) break;
		}
		if(line>=strs.length) return false; //エラー

		// init
		line++;
		if(strs[line].startsWith(initStartMarkString)){
			long id = Long.parseLong(strs[line].substring(initStartMarkString.length()));
			startNode = temps.get(id);
		}else{
			 return false; //エラー
		}

		for(line++;line<strs.length;++line){
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = Long.parseLong(ss[0]);
			TempNode node = temps.get(id);
			if(node==null){ break; }
			node.setToId(ss[1]);
		}

		//エラー
		if(startNode==null) return false;

		setNodeFrom(temps);

		// temp -> state
		makeAllFromTemps(temps,startNode);

		setTreeDepth();

		resetOrder();

		if(Env.is("SV_DUMMY")){
			setFirstPositionReset();
			setDummy();
		}

		updateNodeLooks();

		return true;
	}

	public boolean setSlimLtlResult(String str){
		this.global = true;

		HashMap<Long,TempNode> temps = new HashMap<Long,TempNode>();
		int line=0;
		TempNode startNode = null;

		ArrayList<Long> cycles = new ArrayList<Long>();

		String[] strs = str.split("\n");

		// States探し
		int findCycle = 0;
		for(;line<strs.length;++line){

			String ss[] = strs[line].split(":",3);

			//cycleの状態を取得
			if(findCycle<=1&&ss.length==3&&ss[0].length()*ss[1].length()*ss[2].length()>0){
				long id = Long.parseLong(ss[2].trim());
				cycles.add(id);
				findCycle=1;
			}else if(findCycle==1){
				findCycle=2;
			}

			if(strs[line].equals(stateMarkString)) break;
		}

		//エラー
		if(line>=strs.length) return false;

		int no = 0;
		for(line++;line<strs.length;++line){
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = Long.parseLong(ss[0]);
			TempNode node = new TempNode(id,no++,ss[1]);
			temps.put(id,node);
		}


		// Transitions探し
		for(;line<strs.length;++line){
			if(strs[line].equals(graphMarkString)) break;
		}
		if(line>=strs.length) return false; //エラー

		// init
		line++;
		if(strs[line].startsWith(initStartMarkString)){
			long id = Long.parseLong(strs[line].substring(initStartMarkString.length()));
			startNode = temps.get(id);
		}else{
			 return false; //エラー
		}

		for(line++;line<strs.length;++line){
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = Long.parseLong(ss[0]);
			TempNode node = temps.get(id);
			if(node==null){ break; }
			node.setToId(ss[1]);
		}


		// Labels探し
		for(;line<strs.length;++line){
			if(strs[line].equals(labelMarkString)) break;
		}
		if(line>=strs.length) return false; //エラー

		for(line++;line<strs.length;++line){
			//状態名を解析
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = Long.parseLong(ss[0]);
			TempNode node = temps.get(id);
			if(node==null){ break; }
			node.setLabel(ss[1]);
		}

		// incycle
		for(long id : cycles){
			TempNode node = temps.get(id);
			if(node==null){ break; }
			node.inCycle = true;
		}

		//エラー
		if(startNode==null) return false;

		setNodeFrom(temps);

		for(int i=0;i<cycles.size()-1;++i){
			TempNode node = temps.get(cycles.get(i));
			node.setEmToId(cycles.get(i+1));
		}

		for(int i=cycles.size()-2;i>=0;--i){
			TempNode endNode = temps.get(cycles.get(cycles.size()-1));
			long nodeId = cycles.get(i);
			if(endNode.isToNodeId(nodeId)){
				endNode.setEmToId(nodeId);
				break;
			}
		}

		// temp -> state
		makeAllFromTemps(temps,startNode);

		setTreeDepth();

		resetOrder();

		if(Env.is("SV_DUMMY")){
			setFirstPositionReset();
			setDummy();
		}

		for(StateNode node : getAllNode()){
			if(node.inCycle){
				node.weak = false;
			}else{
				node.weak = true;
			}
		}

		Collections.reverse(cycles);

		for(Long id : cycles){
			setOrderEnd(allNode.get(id));
		}

		updateNodeLooks();

		return true;
	}

	public boolean setTempNode(HashMap<Long,TempNode> temps,TempNode startNode){
		this.global = false;

		setNodeFrom(temps);

		// temp -> state
		makeAllFromTemps(temps,startNode);

		setTreeDepth();

		resetOrder();

		if(Env.is("SV_DUMMY")){
			setFirstPositionReset();
			setDummy();
		}

		updateNodeLooks();

		return true;
	}

	public String getDotString(){
		StringBuffer str = new StringBuffer();
		int no=0;
		for(ArrayList<StateNode> sameDepth : depthNode){
			for(StateNode node : sameDepth){
				str.append(node.getStringTo(++no));
			}
		}
		return str.toString();
	}

	public String getRankString(){
		StringBuffer str = new StringBuffer();
		for(ArrayList<StateNode> sameDepth : depthNode){
			str.append("{rank = same");
			for(StateNode node : sameDepth){
				str.append(";"+node.id);
			}
			str.append("}\n");
		}
		return str.toString();
	}

	public String getMatchFileString(String head,String guard){
		StringBuffer str = new StringBuffer();
		for(StateNode node : getAllNode()){
			if(node.dummy) continue;
			str.append("{ sVr_id("+node.id+"),"+node.state+" },\n");
		}
		str.append("sVr_matches{}.\n");
		str.append("sVr_matches{$ids},{sVr_id($sVr_n),"+head+"}\n");
		str.append(" :- int($sVr_n)");
		if(!guard.equals("")){ str.append(","+guard); }
		str.append("\n |");
		str.append("sVr_matches{$ids,id($sVr_n)},{"+head+"}\n");
		return str.toString();
	}

	/*
	public void reduction(){
		StateNode removenode;
		while((removenode=getToOneFromOne())!=null){
			innerRemove(removenode);
		}
		updateNodeLooks();
	}
	*/

	public void remove(StateNode node){
		innerRemove(node);
		updateNodeLooks();
	}

	public void remove(Long id){
		innerRemove(allNode.get(id));
		updateNodeLooks();
	}

	/*
	public boolean isFlow(StateNode n1,StateNode n2){
		if(cycleNode.size()==0||n1==n2) return true;
		for(int i=0;i<cycleNode.size();++i){
			if(cycleNode.get(i)==n1){
				if(i>0&&cycleNode.get(i-1)==n2) return true;
				if(i<(cycleNode.size()-1)&&cycleNode.get(i+1)==n2) return true;
			}
			if(cycleNode.get(i)==n2){
				if(i>0&&cycleNode.get(i-1)==n1) return true;
				if(i<(cycleNode.size()-1)&&cycleNode.get(i+1)==n1) return true;
			}
		}
		if(n1==acceptStartNode){
			if(n2==cycleNode.get(cycleNode.size()-1)) return true;
		}else if(n2==acceptStartNode){
			if(n1==cycleNode.get(cycleNode.size()-1)) return true;
		}

		return false;
	}
	*/

	public StateNode get(long id){
		return allNode.get(id);
	}

	int getDepth(){
		return depthNode.size();
	}

	public int getSameDepthSize(int depth){
		return depthNode.get(depth).size();
	}

	public int getHeight(){
		int max = 0;
		for(int depth=0;depth<getDepth();++depth){
			int d = getSameDepthSize(depth);
			if(max<d){
				max = d;
			}
		}
		return max;
	}

	public int size(){
		return allNode.size();
	}

	public int getDummySize(){
		int dummy = 0;
		for(StateNode node : getAllNode()){
			if(node.dummy){
				dummy++;
			}
		}
		return dummy;
	}

	public Collection<StateNode> getAllNode(){
		return allNode.values();
	}

	public Collection<StateNode> getAllNodeDrawOrder(){
		return drawNodeOrder;
	}

	public StateNode getStartNode(){
		return startNode;
	}

	public ArrayList<StateNode> getEndNode(){
		return endNode;
	}

	public ArrayList<ArrayList<StateNode>> getDepthNode(){
		return depthNode;
	}

	public ArrayList<StateTransition> getAllTransition(){
		return allTransition;
	}

	public Collection<StateTransition> getAllTransitionDrawOrder(){
		return drawTransitionOrder;
	}

	public void resetOrder(){
		drawNodeOrder.clear();
		for(StateNode node : getAllNode()){
			drawNodeOrder.add(node);
		}
		for(StateNode node : endNode){
			setOrderEnd(node);
		}

		drawTransitionOrder.clear();
		for(StateNode node : drawNodeOrder){
			for(StateTransition t : node.getTransition()){
				drawTransitionOrder.add(t);
			}
		}
	}

	public void setOrderEnd(StateNode node){
		drawNodeOrder.remove(node);
		drawNodeOrder.add(node);
		for(StateTransition t : node.getTransition()){
			if(t.em){
				drawTransitionOrder.remove(t);
				drawTransitionOrder.add(t);
			}
		}
	}

	/*
	private StateNode getToOneFromOne(){
	    if(allNode.size()<=1) return null;
		for(StateNode node : getAllNode()){
	    	ArrayList<StateNode> toes = node.getToNodes();
			if(toes.size()==1){
				ArrayList<StateNode> froms = node.getFromNodes();
				if(froms.size()==1){
					if(froms.get(0).getToNodes().size()==1){
						return node;
					}
				}
			}
		}
	    return null;
	}
	*/

	private void innerRemove(StateNode removenode){

		for(StateNode to : removenode.getToNodes()){
			if(to==removenode){ continue; }
			to.removeFromNode(removenode);
			for(StateNode from : removenode.getFromNodes()){
				if(from==removenode){ continue; }
				to.addFromNode(from);
			}
		}

		for(StateNode from : removenode.getFromNodes()){
			if(from==removenode){ continue; }
			ArrayList<String> ruleNames = from.getToRuleNames(removenode);
			removeTransition(from.removeToNode(removenode));
			for(StateNode to : removenode.getToNodes()){
				if(to==removenode){ continue; }
				addTransition(from.addToNode(to,ruleNames,removenode.isEmToNode(to)));
			}
		}

		if(removenode==startNode){ startNode = null; }
		endNode.remove(removenode);

		allNode.remove(removenode.id);
		drawNodeOrder.remove(removenode);
		for(StateTransition t : removenode.getTransition()){
			removeTransition(t);
		}
		dummyNode.remove(removenode);

		ArrayList<StateNode> sameDepth = depthNode.get(removenode.depth);
		sameDepth.remove(removenode);
		for(StateNode node : sameDepth){
			if(node.nth>removenode.nth){
				node.nth--;
			}
		}

		if(sameDepth.size()==0){
			depthNode.remove(removenode.depth);
			for(StateNode node : getAllNode()){
				if(node.depth>removenode.depth){
					node.depth--;
				}
			}
		}
	}

	private void setTreeDepth(){
		allNodeUnMark();
		depthNode.clear();

		LinkedList<StateNode> queue = new LinkedList<StateNode>();

		startNode.mark();
		startNode.depth=0;
		startNode.nth=0;
		depthNode.add(0,new ArrayList<StateNode>());
		depthNode.get(0).add(startNode);
		queue.add(startNode);

		while(!queue.isEmpty()){
			StateNode node = queue.remove();
			for(StateNode child : node.getToNodes()){
				if(child.isMarked()){continue;}
				child.mark();
				/*
				child.depth = node.depth+1;
				if(depthNode.size()<=child.depth){
					depthNode.add(child.depth,new ArrayList<StateNode>());
				}
				ArrayList<StateNode> dnodes = depthNode.get(child.depth);
				if(dnodes==null){
					dnodes = new ArrayList<StateNode>();
					depthNode.add(child.depth,dnodes);
				}
				child.nth=dnodes.size();
				dnodes.add(child);
				*/
				insertDepthNode(child,node.depth+1);
				queue.add(child);
			}
		}
	}

	private void insertDepthNode(StateNode node,int depth){
		while(depthNode.size()<=depth){
			depthNode.add(depth,new ArrayList<StateNode>());
		}
		ArrayList<StateNode> dnodes= depthNode.get(depth);

		int i;
		for(i=0;i<dnodes.size();++i){
			if(dnodes.get(i).getY()>node.getY()){
				break;
			}
		}

		dnodes.add(i,node);
		node.depth = depth;
		node.nth = i;
		for(;i<dnodes.size();++i){
			dnodes.get(i).nth = i;
		}
	}


	public void setDummy(){

		int no = allNode.size();
		ArrayList<StateNode> newNode = new ArrayList<StateNode>();

		for(StateNode node : getAllNode()){
			ArrayList<StateNode> addToNodes = new ArrayList<StateNode>();
			ArrayList<ArrayList<String>> addToRuleNames = new ArrayList<ArrayList<String>>();
			ArrayList<StateNode> addEmToNodes = new ArrayList<StateNode>();
			ArrayList<StateNode> removeToNodes = new ArrayList<StateNode>();
			for(StateNode to : node.getToNodes()){
				ArrayList<String> ruleNames = node.getToRuleNames(to);
				if(node.depth-to.depth>=2){
					int dummyDepth = node.depth-1;
					StateNode from = node;
					boolean inCycle = false;
					if(node.isEmToNode(to)){
						inCycle = true;
					}
					double startY = node.getY(), endY = to.getY();
					int startDepth = node.depth, endDepth = to.depth;
					while(dummyDepth>to.depth){
						double y = ((startY-endY)/(double)(startDepth-endDepth))*(double)(dummyDepth-endDepth)+endY;
						StateNode dummy = makeDummyNode(++no,dummyDepth,node.label,node.accept,inCycle,y,node.weak);
						newNode.add(dummy);

						addTransition(dummy.addToNode(to,ruleNames,from.isEmToNode(to)));
						dummy.addFromNode(from);
						if(from==node){
							addToNodes.add(dummy);
							if(from.isEmToNode(to)){ addEmToNodes.add(dummy); }
							addToRuleNames.add(ruleNames);
							removeToNodes.add(to);
						}else{
							addTransition(from.addToNode(dummy,ruleNames,from.isEmToNode(to)));
							removeTransition(from.removeToNode(to));
						}
						to.addFromNode(dummy);
						to.removeFromNode(from);

						/*
						if(from.isEmToNode(to)){
							from.removeEmToNode(to);
							from.addEmToNode(dummy);
							dummy.addEmToNode(to);
						}
						*/
						/*
						if(from.isEmToNode(to)||(from==node&&addEmToNodes.contains(to))){
							dummy.addEmToNode(to);
							if(from==node){
								removeEmToNodes.add(to);
								addEmToNodes.add(dummy);
							}else{
								from.removeEmToNode(to);
								from.addEmToNode(dummy);
							}
						}
						*/

						//次のループの準備
						from = dummy;
						dummyDepth--;
					}
				}
			}
			for(int i=0;i<addToNodes.size();++i){
				addTransition(node.addToNode(addToNodes.get(i),addToRuleNames.get(i),addEmToNodes.contains(addToNodes.get(i))));
			}
			for(StateNode to : removeToNodes){
				removeTransition(node.removeToNode(to));
			}
		}
		for(StateNode dummy : newNode){
			allNode.put(dummy.id,dummy);
			drawNodeOrder.add(dummy);
			dummyNode.add(dummy);
		}

	}

	private void addTransition(StateTransition trans){
		if(trans!=null){
			allTransition.add(trans);
			drawTransitionOrder.add(trans);
		}
	}

	private void removeTransition(StateTransition trans){
		if(trans!=null){
			allTransition.remove(trans);
			drawTransitionOrder.remove(trans);
		}
	}

	private StateNode makeDummyNode(int no,int depth,String label,boolean accept,boolean inCycle,double y,boolean weak){
		long id;
		for(id = no;true;++id){
			if(!allNode.containsKey(id)) break;
		}

		StateNode dummy = new StateNode(id);
		dummy.init(no,"",label,accept,inCycle);
		dummy.dummy = true;
		dummy.weak = weak;

		// 位置の設定
		dummy.setX(depthNode.get(depth).get(0).getX());
		dummy.setY(y);

		// depth, nthの設定
		insertDepthNode(dummy,depth);

		return dummy;
	}

	public void removeDummy(){
		while(dummyNode.size()>0){
			innerRemove(dummyNode.get(0));
		}
	}

	public Rectangle2D.Double getNodesDimension(){
		double minX=Double.MAX_VALUE,minY=Double.MAX_VALUE;
		double maxX=Double.MIN_VALUE,maxY=Double.MIN_VALUE;

		for(StateNode node : getAllNode()){
			if(node.getX()<minX){ minX = node.getX(); }
			if(node.getY()<minY){ minY = node.getY(); }
			if(maxX<node.getX()){ maxX = node.getX(); }
			if(maxY<node.getY()){ maxY = node.getY(); }
		}

		minX -= 28.0;
		minY -= 14.0;
		maxX += 14.0;
		maxY += 14.0;

		return new Rectangle2D.Double(minX,minY,maxX-minX,maxY-minY);
	}

	void resetMovePosition(){
		Rectangle2D.Double d = getNodesDimension();
		allMove(-1.0*d.x,-1.0*d.y);
	}

	void allScaleMove(double scaleX, double scaleY, double pX, double pY){
		if(scaleX<0.5){ scaleX=0.5; }else if(scaleX>2){ scaleX=2; }
		if(scaleY<0.5){ scaleY=0.5; }else if(scaleY>2){ scaleY=2; }
		for(StateNode node : getAllNode()){
			double x = (node.getX()-pX)*scaleX+pX;
			double y = (node.getY()-pY)*scaleY+pY;
			node.setPosition(x, y);
		}
	}

	void allScaleCenterMove(double scaleX,double scaleY){
		Rectangle2D.Double d = getNodesDimension();
		allScaleMove(scaleX,scaleY,d.getCenterX(),d.getCenterY());
	}

	void allMove(double dx, double dy){
		for(StateNode node : getAllNode()){
			node.move(dx, dy);
		}
	}

	double getMinLength(long id,double x,double y){
		double minLength2 = 100000000;
		for(StateNode node : getAllNode()){
			if(node.id!=id){
				double dx = x-node.getX();
				double dy = y-node.getY();
				double l = dx*dx+dy*dy;
				if(l<minLength2){
					minLength2 = l;
				}
			}
		}
		return Math.sqrt(minLength2);
	}

	double getLinkLength(StateNode node,double x,double y){
		double sumLength = 0;
		for(StateNode to : node.getToNodes()){
			double dx = x-to.getX();
			double dy = y-to.getY();
			sumLength += dx*dx+dy*dy;
		}
		for(StateNode from : node.getFromNodes()){
			double dx = x-from.getX();
			double dy = y-from.getY();
			sumLength += dx*dx+dy*dy;
		}
		return sumLength;
	}

	void updateDefaultYOrder(){
		int no=0;
		for(ArrayList<StateNode> dn : depthNode){
			Collections.sort(dn, new Comparator<StateNode>() {
				public int compare(StateNode n1, StateNode n2) {
					double y1 = n1.getY();
					double y2 = n2.getY();
					if(y1<y2){
						return -1;
					}else if(y1>y2){
						return 1;
					}else{
						return 0;
					}

				}
			});
			for(int i=0;i<dn.size();++i){
				dn.get(i).no = no;
				dn.get(i).nth = i;
				no++;
			}
		}
	}

/*
	double getSumLength(int id,double x,double y){
		double sumLength = 0;
		for(StateNode node : getAllNode()){
			if(node.id!=id){
				double dx = x-node.getX();
				double dy = y-node.getY();
				sumLength += dx*dx+dy*dy;
			}
		}
		return sumLength;
	}
*/
	private void setNodeFrom(HashMap<Long,TempNode> temps){
		for(TempNode node : temps.values()){
			for(TempTransition to : node.toes){
				temps.get(to.node).setFromId(node.id);
			}
		}
	}

	StateNode pickANode(Point p){
		StateNode pick = null;
		for(StateNode node : drawNodeOrder){
			if(node == null){ continue; }
			if(node.contains(p)){
				pick = node;
			}
		}
		return pick;
	}

	boolean rideOtherNode(StateNode node){
		for(StateNode n : getAllNode()){
			if(n==node) continue;
			double dx = node.getX()-n.getX();
			double dy = node.getY()-n.getY();
			double r = node.getRadius()+n.getRadius()+5;
			if(dx*dx+dy*dy<r*r){
				return true;
			}
		}
		return false;
	}

	private void makeAllFromTemps(HashMap<Long,TempNode> temps,TempNode startNode){
		for(TempNode temp: temps.values()){
			StateNode node = getNewNode(temp.id);
			node.init(temp.no, temp.state, temp.label, temp.accept, temp.inCycle);

			for(TempTransition to : temp.toes){
				addTransition(node.addToNode(getNewNode(to.node),to.rules,to.em));
			}
			for(Long from : temp.fromIds){
				node.addFromNode(getNewNode(from));
			}
			if(temp.toes.size()==0){
				endNode.add(node);
			}

		}
		this.startNode = getNewNode(startNode.id);
	}

	private StateNode getNewNode(long id){
		StateNode node = allNode.get(id);
		if(node==null){
			node = new StateNode(id);
			allNode.put(id,node);
		}
		return node;
	}

	public void updatePosition(StatePositionSet statePositionSet){
		for(StateNode node : getAllNode()){
			node.setY(statePositionSet.getY(node.id));
		}
	}

	public void updateNodeLooks(){
		for(StateNode node : getAllNode()){
			node.updateLooks();
		}
	}

	public void allNodeUnMark(){
		for(StateNode node : getAllNode()){
			node.unmark();
		}
	}

	private void setFirstPositionReset(){
		double w = 100.0;
		double h = 100.0;

		double minLength = w/(getDepth()+1);
		for(int i=0;i<getDepth();++i){
			double d = h/(getSameDepthSize(i)+1);
			if(d<minLength) minLength = d;
		}

		double xPosInterval = w/(getDepth()+1);
		double[] yPosInterval = new double[getDepth()];
		for(int i=0;i<getDepth();++i){
			yPosInterval[i] = h/(getSameDepthSize(i)+1);
		}

		for(StateNode node : getAllNode()){
			node.resetLocation(xPosInterval,yPosInterval);
		}
	}

	class TempNode{
		long id = 0;
		int no = 0;
		String state = "";
		String label = "";
		boolean accept = false;
		boolean inCycle = false;
		public ArrayList<TempTransition> toes = new ArrayList<TempTransition>();
		public ArrayList<Long> fromIds = new ArrayList<Long>();

		TempNode(long id,int no,String state){
			this.id = id;
			this.no = no;
			this.state = state;
		}

		void setToId(String toIdsStr){
			try{
				if(toIdsStr.length()>0){
					String[] toStrs = toIdsStr.split(",");
					for(String toIdStr : toStrs){
						TempTransition to = new TempTransition();

						int leftBracketIndex = toIdStr.indexOf("(");
						int rightBracketIndex = -1;
						if(leftBracketIndex>0){ rightBracketIndex = toIdStr.indexOf(")",leftBracketIndex); }
						if(rightBracketIndex>0){
							to.node = Long.parseLong(toIdStr.substring(0,leftBracketIndex));
							for(String s : toIdStr.substring(leftBracketIndex+1,rightBracketIndex).split(" ")){
								to.rules.add(s);
							}
						}else{
							to.node = Long.parseLong(toIdStr);
						}
						toes.add(to);
					}
				}
			}catch(NumberFormatException e){
			}
		}

		void setFromId(long id){
			if(!fromIds.contains(id)) fromIds.add(id);
		}

		void setLabel(String label){
			this.label = label;
			if(label.toLowerCase().indexOf("accept")!=-1){
				this.accept = true;
			}
		}

		void setEmToId(long toId){
			for(TempTransition t : toes){
				if(t.node==toId){
					t.em = true;
				}
			}
		}

		boolean isToNodeId(long toId){
			for(TempTransition t : toes){
				if(t.node==toId){
					return true;
				}
			}
			return false;
		}

	}

	class TempTransition{
		Long node;
		boolean em = false;
		private ArrayList<String> rules = new ArrayList<String>();
	}


}
