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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.*;
import lavit.stateviewer.worker.StatePositionSet;
import lavit.util.NodeYComparator;

public class StateNodeSet {

	public StateNode parentNode;
	public int generation;

	public long maxNodeId;
	private LinkedHashMap<Long,Long> mem2id = new LinkedHashMap<Long,Long>();

	private LinkedHashMap<Long,StateNode> allNode = new LinkedHashMap<Long,StateNode>();
	private LinkedHashSet<StateTransition> allTransition = new LinkedHashSet<StateTransition>();
	private LinkedHashSet<StateTransition> outTransition = new LinkedHashSet<StateTransition>();

	private ArrayList<StateNode> cycleNode =  new ArrayList<StateNode>();

	private LinkedHashSet<StateNode> startNode =  new LinkedHashSet<StateNode>();
	private LinkedHashSet<StateNode> endNode =  new LinkedHashSet<StateNode>();

	private ArrayList<ArrayList<StateNode>> depthNode = new ArrayList<ArrayList<StateNode>>();

	final String cycleMarkString = "cycle(or error) found:";
	final String cycleEndMarkString = "no cycles found";
	final String stateMarkString = "States";
	final String graphMarkString = "Transitions";
	final String initStartMarkString = "init:";
	final String labelMarkString = "Labels";

	public StateNodeSet(){
		this.parentNode = null;
		this.generation = 0;
	}

	public StateNodeSet(StateNode parent){
		this.parentNode = parent;
		this.generation = parent.parentSet.generation+1;
	}

	private StateNode getNodeInMaking(Long id){
		StateNode node = allNode.get(id);
		if(node==null){
			node = new StateNode(id, this);
			addNode(node);
		}
		return node;
	}

	private Long getIdFromMemString(String mem){
		Long memlong = Long.parseLong(mem.trim());
		Long id = mem2id.get(memlong);
		if(id!=null) return id;
		id = publishNodeId();
		mem2id.put(memlong, id);
		return id;
	}

	public Long publishNodeId(){
		return (++getRootStateNodeSet().maxNodeId);
	}

	long getMaxNodeId(){
		return getRootStateNodeSet().maxNodeId;
	}

	public boolean setSlimResult(String str, boolean ltlMode){

		int line=0;
		String[] strs = str.split("\n");

		// cycle parse
		if(ltlMode){
			// cycle探し
			for(;line<strs.length;++line){
				if(strs[line].equals(cycleMarkString)) break;
				if(strs[line].equals(cycleEndMarkString)) break;
			}
			if(line>=strs.length) return false; //エラー

			// cycle解析
			for(line++;line<strs.length;++line){
				String ss[] = strs[line].split(":",3);
				if(ss.length<3){ break; }

				long id = getIdFromMemString(ss[2]);
				StateNode node = getNodeInMaking(id);
				node.cycle = true;
				cycleNode.add(node);
			}
		}

		// States探し
		for(;line<strs.length;++line){
			if(strs[line].equals(stateMarkString)) break;
		}
		if(line>=strs.length) return false; //エラー

		// States解析
		for(line++;line<strs.length;++line){
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = getIdFromMemString(ss[0]);
			StateNode node = getNodeInMaking(id);
			node.state = ss[1];
		}


		// Transitions探し
		for(;line<strs.length;++line){
			if(strs[line].equals(graphMarkString)) break;
		}
		if(line>=strs.length) return false; //エラー

		// init解析
		line++;
		if(strs[line].startsWith(initStartMarkString)){
			long id = getIdFromMemString(strs[line].substring(initStartMarkString.length()));
			StateNode node = getNodeInMaking(id);
			startNode.add(node);
		}else{
			return false; //エラー
		}

		// Transitions解析
		for(line++;line<strs.length;++line){
			String ss[] = strs[line].split("::",2);
			if(ss.length<2){ break; }

			long id = getIdFromMemString(ss[0]);
			StateNode from = getNodeInMaking(id);
			String toIdsStr = ss[1];

			try{
				if(toIdsStr.length()>0){
					String[] toStrs = toIdsStr.split(",");
					for(String toIdStr : toStrs){
						StateNode to = null;
						Long toId;
						String rules = "";

						int leftBracketIndex = toIdStr.indexOf("(");
						int rightBracketIndex = -1;
						if(leftBracketIndex>0){ rightBracketIndex = toIdStr.indexOf(")",leftBracketIndex); }
						if(rightBracketIndex>0){
							toId = getIdFromMemString(toIdStr.substring(0,leftBracketIndex));
							rules = toIdStr.substring(leftBracketIndex+1,rightBracketIndex);
						}else{
							toId = getIdFromMemString(toIdStr);
						}
						to = getNodeInMaking(toId);

						StateTransition t = new StateTransition();
						t.from = from;
						t.to = to;
						t.addRules(rules);

						t.from.addToTransition(t);
						t.to.addFromTransition(t);
						allTransition.add(t);
					}
				}
			}catch(NumberFormatException e){
			}
		}

		// Labels
		if(ltlMode){
			// Labels探し
			for(;line<strs.length;++line){
				if(strs[line].equals(labelMarkString)) break;
			}
			if(line>=strs.length) return false; //エラー

			// Label解析
			for(line++;line<strs.length;++line){
				String ss[] = strs[line].split("::",2);
				if(ss.length<2){ break; }

				long id = getIdFromMemString(ss[0]);
				StateNode node = getNodeInMaking(id);
				node.label = ss[1];
				if(node.label.toLowerCase().indexOf("accept")!=-1){
					node.accept = true;
				}
			}
		}

		//parse終了

		//endNodeの登録
		for(StateNode node : allNode.values()){
			if(node.getToTransitions().size()==0){
				endNode.add(node);
			}
		}

		//cycle transitionの登録
		for(int i=0;i<cycleNode.size()-1;++i){
			StateNode from = cycleNode.get(i);
			StateNode to = cycleNode.get(i+1);
			StateTransition t = from.getToTransition(to);
			t.cycle = true;
		}
		//cycleのループの戻りを探す
		if(cycleNode.size()>0){
			StateNode end = cycleNode.get(cycleNode.size()-1);
			for(int i = cycleNode.size()-2;i>=0;i--){
				StateTransition t = end.getToTransition(cycleNode.get(i));
				if(t!=null){
					t.cycle = true;
					break;
				}
			}
		}

		setTreeDepth();

		resetOrder();
		positionReset();

		if(Env.is("SV_STARTUP_SET_BACKDUMMY")){
			setBackDummy();
			dummyCentering();
		}

		updateNodeLooks();

		return true;
	}

	public void updateShortCycle(){
		if(cycleNode.size()==0) return;

		StateNode loopStartNode = cycleNode.get(cycleNode.size()-1).getToCycleNode();

		//loop部とstraight部にわける
		ArrayList<StateNode> cycleStraightNode =  new ArrayList<StateNode>();
		ArrayList<StateNode> cycleLoopNode =  new ArrayList<StateNode>();
		if(loopStartNode==null){
			for(int i=0;i<cycleNode.size()-1;++i){
				cycleStraightNode.add(cycleNode.get(i));
			}
			//最後のノードだけループに入れる
			cycleLoopNode.add(cycleNode.get(cycleNode.size()-1));
		}else{
			boolean onLoop = false;
			for(StateNode n : cycleNode){
				if(!onLoop){
					if(n==loopStartNode){
						onLoop = true;
						cycleLoopNode.add(n);
					}else{
						cycleStraightNode.add(n);
					}
				}else{
					cycleLoopNode.add(n);
				}
			}
		}

		//cycleのリセット
		for(StateTransition t : getAllTransition()){
			t.cycle = false;
		}
		for(StateNode node : getAllNode()){
			node.cycle = false;
		}
		cycleNode.clear();


		//構築
		ArrayList<StateNode> newCycleStraightNode =  new ArrayList<StateNode>();
		ArrayList<StateNode> newCycleLoopNode =  new ArrayList<StateNode>();

		//直線部の構築
		{
			//サイクル中から一番深さが小さい物を探す
			StateNode node = null;
			int minDepth = Integer.MAX_VALUE;
			for(StateNode n : cycleLoopNode){
				if(n.depth<minDepth){
					minDepth = n.depth;
					node = n;
				}
			}

			//入口の更新
			if(loopStartNode!=null){
				loopStartNode = node;
			}

			//一番深さが小さいものから最短をサイクルに入れる
			while((node = node.getFromNearNode())!=null){
				newCycleStraightNode.add(node);
			}
			Collections.reverse(newCycleStraightNode);
		}

		//ループ部の構築
		if(loopStartNode==null){
			newCycleLoopNode.add(cycleLoopNode.get(0));
		}else{
			//ループの開始ノードからの一周にする
			int start;
			for(start = 0;start<cycleLoopNode.size();++start){
				if(cycleLoopNode.get(start)==loopStartNode){
					break;
				}
			}
			for(int i=start;i<cycleLoopNode.size();++i){
				newCycleLoopNode.add(cycleLoopNode.get(i));
			}
			for(int i=0;i<start;++i){
				newCycleLoopNode.add(cycleLoopNode.get(i));
			}
		}


		//cycle化
		for(StateNode node : newCycleStraightNode){
			node.cycle = true;
			cycleNode.add(node);
		}
		for(StateNode node : newCycleLoopNode){
			node.cycle = true;
			cycleNode.add(node);
		}

		//cycle transitionの登録
		for(int i=0;i<cycleNode.size()-1;++i){
			StateNode from = cycleNode.get(i);
			StateNode to = cycleNode.get(i+1);
			StateTransition t = from.getToTransition(to);
			t.cycle = true;
		}
		//cycleのループの戻りを探す
		if(cycleNode.size()>0){
			StateNode end = cycleNode.get(cycleNode.size()-1);
			for(int i = cycleNode.size()-2;i>=0;i--){
				StateTransition t = end.getToTransition(cycleNode.get(i));
				if(t!=null){
					t.cycle = true;
					break;
				}
			}
		}
		resetOrder();
	}

	public void setSubNode(Collection<StateNode> nodes, Collection<StateTransition> allTrans, Collection<StateTransition> outTrans){

		//nodeの登録
		for(StateNode node : nodes){
			addNode(node);
			node.parentSet = this;
		}

		//allTransitionの登録
		for(StateTransition trans : allTrans){
			allTransition.add(trans);
		}

		//outTransitionの登録
		for(StateTransition trans : outTrans){
			outTransition.add(trans);
		}

		//startNode と endNodeの登録
		for(StateNode node : nodes){
			if(node.getFromNodes().size()==0){
				startNode.add(node);
			}
			if(node.getToNodes().size()==0){
				endNode.add(node);
			}
		}

		//startNodeがない場合は深さで決める
		if(startNode.size()==0){
			StateNode minNode = null;
			for(StateNode node : nodes){
				if(minNode==null){ minNode=node; }
				if(node.depth<minNode.depth){
					minNode = node;
				}
			}
			startNode.add(minNode);
		}

		//startNodeから全て遷移できるかチェック
		ArrayList<StateNode> aN = new ArrayList<StateNode>(getAllNode());
		while(aN.size()>0){
			LinkedList<StateNode> queue = new LinkedList<StateNode>();
			allNodeUnMark();
			for(StateNode node : startNode){
				node.mark();
				aN.remove(node);
				queue.add(node);
			}
			while(!queue.isEmpty()){
				StateNode node = queue.remove();
				for(StateNode child : node.getToNodes()){
					if(child.isMarked()){continue;}
					child.mark();
					aN.remove(child);
					queue.add(child);
				}
			}
			if(aN.size()>0){
				StateNode maxToNode = aN.get(0);
				for(StateNode n : aN){
					if(n.getToTransitions().size()>maxToNode.getToTransitions().size()){
						maxToNode = n;
					}
				}
				startNode.add(maxToNode);
			}
		}

		setTreeDepth();

		resetOrder();
		positionReset();

		if(Env.is("SV_STARTUP_SET_BACKDUMMY")){
			setBackDummy();
			dummyCentering();
		}

		updateNodeLooks();

		/*
		//抽象化状態へのtransitionは削除
		for(StateTransition t : new LinkedList<StateTransition>(outTrans)){
			if(t.from.childSet!=null||t.to.childSet!=null){
				outTrans.remove(t);
			}
		}
		this.outTransition = outTrans;


		//nodeの登録
		for(StateNode node : nodes){
			allNode.put(node.id, node);
			node.parentSet = this;
		}

		//transitionの登録,内でないtransitionを削除
		for(StateNode node : nodes){
			// to
			for(StateTransition t : new ArrayList<StateTransition>(node.getToTransitions())){
				if(allNode.containsKey(t.to.id)){
					addTransition(t);
				}else{
					node.removeToTransition(t);
				}
			}
			// from
			for(StateTransition f : new ArrayList<StateTransition>(node.getFromTransitions())){
				if(allNode.containsKey(f.from.id)){
					addTransition(f);
				}else{
					node.removeFromTransition(f);
				}
			}
		}

		//fromの登録
		for(StateNode node : nodes){
			node.resetFromTransition();
			for(StateTransition t : node.getToTransitions()){
				t.to.addFromTransition(t);
			}
		}

		//startNode と endNodeの登録
		for(StateNode node : nodes){
			if(node.getFromNodes().size()==0){
				startNode.add(node);
			}
			if(node.getToNodes().size()==0){
				endNode.add(node);
			}
		}


		//for(StateTransition t : outTrans){
		//	if(!nodes.contains(t.from)&&nodes.contains(t.to)){
		//		startNode.add(t.to);
		//	}
		//}


		//startNodeから全て遷移できるかチェック
		ArrayList<StateNode> aN = new ArrayList<StateNode>(getAllNode());
		while(aN.size()>0){
			LinkedList<StateNode> queue = new LinkedList<StateNode>();
			allNodeUnMark();
			for(StateNode node : startNode){
				node.mark();
				aN.remove(node);
				queue.add(node);
			}
			while(!queue.isEmpty()){
				StateNode node = queue.remove();
				for(StateNode child : node.getToNodes()){
					if(child.isMarked()){continue;}
					child.mark();
					aN.remove(child);
					queue.add(child);
				}
			}
			if(aN.size()>0){
				StateNode maxToNode = aN.get(0);
				for(StateNode n : aN){
					if(n.getToTransitions().size()>maxToNode.getToTransitions().size()){
						maxToNode = n;
					}
				}
				startNode.add(maxToNode);
			}
		}

		setTreeDepth();

		resetOrder();
		positionReset();

		if(Env.is("SV_STARTUP_SET_BACKDUMMY")){
			setDummy();
			dummyCentering();
		}

		updateNodeLooks();

		return true;
		 */
	}

	public void positionReset(){
		double w = FrontEnd.mainFrame.toolTab.statePanel.stateGraphPanel.getWidth();
		double h = FrontEnd.mainFrame.toolTab.statePanel.stateGraphPanel.getHeight();
		double z;
		double minLength = w/(getDepth()+1);
		for(int i=0;i<getDepth();++i){
			double d = h/(getSizeOfDepth(i)+1);
			if(d<minLength) minLength = d;
		}
		if(minLength/30>1){
			z = minLength/30;
		}else{
			z = 1.0;
		}

		double xPosInterval;
		xPosInterval = w/(getDepth()+1);
		xPosInterval /= z;

		double[] yPosInterval = new double[getDepth()];
		for(int i=0;i<getDepth();++i){
			yPosInterval[i] = h/(getSizeOfDepth(i)+1);
			yPosInterval[i] /= z;
		}

		for(StateNode node : getAllNode()){
			node.resetLocation(xPosInterval,yPosInterval);
		}
	}

	public void dummyCentering(){
		ArrayList<ArrayList<StateNode>> depthNode = getDepthNode();
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
			str.append("{ sVr_id("+node.id+"),"+node.toString()+" },\n");
		}
		str.append("sVr_matches{}.\n");
		str.append("sVr_matches{$ids},{sVr_id($sVr_n),"+head+"}\n");
		str.append(" :- int($sVr_n)");
		if(!guard.equals("")){ str.append(","+guard); }
		str.append("\n |");
		str.append("sVr_matches{$ids,id($sVr_n)},{"+head+"}\n");
		return str.toString();
	}

	public StateNode get(long id){
		return allNode.get(id);
	}

	public int getDepth(){
		return depthNode.size();
	}

	public int getSizeOfDepth(int depth){
		return depthNode.get(depth).size();
	}

	public int getHeight(){
		int max = 0;
		for(int depth=0;depth<getDepth();++depth){
			int d = getSizeOfDepth(depth);
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

	public void addNode(StateNode node){
		allNode.put(node.id, node);
	}

	public Collection<StateNode> getAllNode(){
		return allNode.values();
	}

	public ArrayList<StateNode> getCycleNode(){
		return cycleNode;
	}

	public Collection<StateNode> getStartNode(){
		return startNode;
	}

	public StateNode getStartNodeOne(){
		for(StateNode node : startNode){
			return node;
		}
		return null;
	}

	public void setStartNode(StateNode sn){
		startNode.clear();
		startNode.add(sn);
	}

	public void addStartNode(StateNode sn){
		startNode.add(sn);
	}

	public Collection<StateNode> getEndNode(){
		return endNode;
	}

	public void addEndNode(StateNode en){
		endNode.add(en);
	}

	public ArrayList<ArrayList<StateNode>> getDepthNode(){
		return depthNode;
	}

	public Collection<StateTransition> getAllTransition(){
		return allTransition;
	}

	public Collection<StateTransition> getAllOutTransition(){
		return outTransition;
	}

	public void updateOutTransition(){
		Collection<StateNode> all = allNode.values();
		boolean check = true;
		while(check){
			check = false;
			for(StateTransition t : new LinkedList<StateTransition>(outTransition)){
				if(!all.contains(t.from)&&all.contains(t.to)){
					if(this.generation-1!=t.from.parentSet.generation){
						if(t.from.parentSet.parentNode!=null){
							t.from = t.from.parentSet.parentNode;
							check = true;
						}else{
							outTransition.remove(t);
						}
					}
				}
				if(all.contains(t.from)&&!all.contains(t.to)){
					if(this.generation-1!=t.to.parentSet.generation){
						if(t.to.parentSet.parentNode!=null){
							t.to = t.to.parentSet.parentNode;
							check = true;
						}else{
							outTransition.remove(t);
						}
					}
				}
			}
		}
	}

	public StateNode getRepresentationNode(){
		for(StateNode node : endNode){
			return node;
		}
		for(StateNode node : startNode){
			return node;
		}
		return allNode.get(0);
	}

	public void resetOrder(){
		for(StateNode node : new LinkedList<StateNode>(getAllNode())){
			setLastOrder(node);
		}
		for(StateTransition t : new LinkedList<StateTransition>(allTransition)){
			setLastOrder(t);
		}

		//最終状態を上に表示
		for(StateNode node : endNode){
			setLastOrderNodeAndTrans(node);
		}

		//cycleを上に表示
		for(StateNode node : cycleNode){
			setLastOrder(node);
			for(StateTransition t : node.getToTransitions()){
				if(t.cycle){
					setLastOrder(t);
				}
			}
		}
	}

	public void setLastOrderNodeAndTrans(StateNode node){
		setLastOrder(node);
		for(StateTransition t : node.getToTransitions()){
			setLastOrder(t);
		}
		for(StateTransition t : node.getFromTransitions()){
			setLastOrder(t);
		}
	}

	public void setLastOrder(StateNode node){
		allNode.remove(node.id);
		allNode.put(node.id, node);
	}

	public void setLastOrder(StateTransition t){
		allTransition.remove(t);
		allTransition.add(t);
	}

	StateNodeSet getRootStateNodeSet(){
		StateNodeSet root = this;
		while(root.generation!=0){
			root = root.getParentNode().parentSet;
		}
		return root;
	}

	StateNode getParentNode(){
		return parentNode;
	}

	/*
	public void remove(StateNode node){
		removeInnerTransitionData(node);
		removeInnerNodeData(node);

		ArrayList<StateNode> sameDepth = depthNode.get(node.depth);
		sameDepth.remove(node);
		for(StateNode n : sameDepth){
			if(n.nth>node.nth){
				n.nth--;
			}
		}

		if(sameDepth.size()==0){
			depthNode.remove(node.depth);
			for(StateNode n : getAllNode()){
				if(n.depth>node.depth){
					n.depth--;
				}
			}
		}
		updateNodeLooks();
	}
	 */

	public void remove(Collection<StateNode> nodes){
		for(StateNode node : nodes){
			removeInnerTransitionData(node);
			removeInnerNodeData(node);
		}
		setTreeDepth();
		updateNodeLooks();
	}

	public void remove(Long id){
		remove(allNode.get(id));
	}

	public void remove(StateNode node){
		ArrayList<StateNode> nodes = new ArrayList<StateNode>();
		nodes.add(node);
		remove(nodes);
	}

	public void removeDummy(){
		for(StateNode node : new LinkedList<StateNode>(getAllNode())){
			if(node.dummy){
				removeInnerTransitionData(node);
				removeInnerNodeData(node);
			}
		}
		setTreeDepth();
		updateNodeLooks();
	}


	void removeInnerTransitionData(StateNode node){
		//セルフループをまず削除
		for(StateTransition t : new ArrayList<StateTransition>(node.getToTransitions())){
			if(t.to==node&&t.from==node){
				node.removeToTransition(t);
				node.removeFromTransition(t);
				removeTransition(t);
			}
		}

		ArrayList<StateTransition> ft = new ArrayList<StateTransition>(node.getFromTransitions());
		ArrayList<StateTransition> tt = new ArrayList<StateTransition>(node.getToTransitions());

		//from削除
		for(StateTransition f : ft){
			f.from.removeToTransition(f);
			node.removeFromTransition(f);
			removeTransition(f);

			//fromをtoに結び付ける
			for(StateTransition t : tt){
				if(f.from==t.to){ continue; } //セルフループは作らせない
				if(f.from.getToTransition(t.to)!=null){ continue; } //既に遷移がある場合作らせない

				StateTransition newTrans = new StateTransition(f.from, t.to, f.cycle && t.cycle, f.weak && t.weak);

				ArrayList<String> rules = new ArrayList<String>();
				for(String fr : f.getRules()){
					for(String tr : t.getRules()){
						if(fr.equals(tr)){
							rules.add(tr);
						}
					}
				}
				newTrans.addRules(rules);

				newTrans.from.addToTransition(newTrans);
				newTrans.to.addFromTransition(newTrans);
				addTransition(newTrans);
			}

		}

		//to削除
		for(StateTransition t : tt){
			node.removeToTransition(t);
			t.to.removeFromTransition(t);
			removeTransition(t);
		}

		/*
		//to:1,from:1の場合は結びつける
		if(ft.size()==1&&tt.size()==1){
			StateTransition f = ft.get(0);
			StateTransition t = tt.get(0);

			StateTransition newTrans = new StateTransition();
			newTrans.from = f.from;
			newTrans.to = t.to;
			newTrans.cycle = f.cycle && t.cycle;
			newTrans.weak = f.weak && t.weak;

			ArrayList<String> rules = new ArrayList<String>();
			for(String fr : f.getRules()){
				for(String tr : t.getRules()){
					if(fr.equals(tr)){
						rules.add(tr);
					}
				}
			}
			newTrans.addRules(rules);

			newTrans.from.addToTransition(newTrans);
			newTrans.to.addFromTransition(newTrans);
			addTransition(newTrans);
		}*/

		/*
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

		for(StateTransition t : removenode.getToTransition()){
			removeTransition(t);
		}*/
	}

	void removeInnerNodeData(StateNode removenode){
		allNode.remove(removenode.id);
		cycleNode.remove(removenode);
		startNode.remove(removenode);
		endNode.remove(removenode);
	}

	void setTreeDepth(){
		allNodeUnMark();
		depthNode.clear();

		LinkedList<StateNode> queue = new LinkedList<StateNode>();

		for(StateNode node : startNode){
			node.mark();
			insertDepthNode(node, 0);
			queue.add(node);
		}

		while(!queue.isEmpty()){
			StateNode node = queue.remove();
			for(StateNode child : node.getToNodes()){
				if(child.isMarked()){continue;}
				child.mark();
				if(child.backDummy){
					int d = node.depth-1;
					if(d<1){ d=1; }
					insertDepthNode(child, d);
				}else{
					insertDepthNode(child, node.depth+1);
				}
				queue.add(child);
			}
		}

	}

	private void insertDepthNode(StateNode node, int depth){
		while(depthNode.size()<=depth){
			depthNode.add(depthNode.size(),new ArrayList<StateNode>());
		}
		ArrayList<StateNode> dnodes= depthNode.get(depth);

		node.depth = depth;
		node.nth = dnodes.size();
		dnodes.add(node);
	}

	public StateNode makeDummyFromTransition(StateTransition trans){
		StateNode from=trans.from, to=trans.to;

		long id = publishNodeId();
		double x = ((from.getX()+to.getX())/2);
		double y = ((from.getY()+to.getY())/2);
		StateNode dummy = new StateNode(id, this);
		dummy.setPosition(x, y);
		dummy.dummy = true;
		if(from.depth>to.depth){
			dummy.backDummy = true;
		}
		dummy.depth = to.depth;
		addNode(dummy);
		dummy.updateLooks();

		StateTransition t1 = new StateTransition(from, dummy, trans.cycle, trans.weak);
		t1.addRules(trans.getRules());
		t1.from.addToTransition(t1);
		t1.to.addFromTransition(t1);
		addTransition(t1);

		StateTransition t2 = new StateTransition(dummy, to, trans.cycle, trans.weak);
		t2.addRules(trans.getRules());
		t2.from.addToTransition(t2);
		t2.to.addFromTransition(t2);
		addTransition(t2);

		from.removeToTransition(trans);
		to.removeFromTransition(trans);
		removeTransition(trans);

		setTreeDepth();

		return dummy;
	}

	public void setBackDummy(){

		for(StateTransition trans : new ArrayList<StateTransition>(getAllTransition())){
			if(!(trans.from.depth-1>trans.to.depth)){ continue; }

			double startX = trans.from.getX(), endX = trans.to.getX();
			double startY = trans.from.getY(), endY = trans.to.getY();
			int startDepth = trans.from.depth, endDepth = trans.to.depth;

			StateNode fromDummy = trans.from;
			for(int dummyDepth=trans.from.depth-1;dummyDepth>trans.to.depth;dummyDepth--){
				long id = publishNodeId();
				double x = ((startX-endX)/(double)(startDepth-endDepth))*(double)(dummyDepth-endDepth)+endX;
				double y = ((startY-endY)/(double)(startDepth-endDepth))*(double)(dummyDepth-endDepth)+endY;
				StateNode toDummy = new StateNode(id, this);
				toDummy.setPosition(x, y);
				toDummy.dummy = true;
				toDummy.backDummy = true;
				toDummy.depth = dummyDepth;
				addNode(toDummy);

				StateTransition t = new StateTransition(fromDummy, toDummy, trans.cycle, trans.weak);
				t.addRules(trans.getRules());
				t.from.addToTransition(t);
				t.to.addFromTransition(t);
				addTransition(t);

				fromDummy = toDummy;
			}

			StateTransition t = new StateTransition(fromDummy, trans.to, trans.cycle, trans.weak);
			t.addRules(trans.getRules());
			t.from.addToTransition(t);
			t.to.addFromTransition(t);
			addTransition(t);

			trans.from.removeToTransition(trans);
			trans.to.removeFromTransition(trans);
			removeTransition(trans);
		}

		setTreeDepth();
		updateNodeLooks();


		/*
		long newId = getMaxNodeId();

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
						StateNode dummy = makeDummyNode(++newId,dummyDepth,node.label,node.accept,inCycle,y,node.weak);
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
			allNode.put(dummy.id, dummy);
		}
		for(ArrayList<StateNode> dnodes : depthNode){
			Collections.sort(dnodes, new NodeYComparator());
			for(int i=0;i<dnodes.size();++i){
				dnodes.get(i).nth = i;
			}
		}
		updateMaxNodeId();

		if(FrontEnd.mainFrame.toolTab.statePanel.isLtl()){
			for(StateNode node : new LinkedList<StateNode>(getAllNode())){
				if(node.inCycle){
					node.weak = false;
					setLastOrder(node);
				}else{
					node.weak = true;
				}
			}
		}
		updateNodeLooks();
		 */
	}

	void addTransition(StateTransition trans){
		if(trans!=null){
			allTransition.add(trans);
		}
	}

	void removeTransition(StateTransition trans){
		if(trans!=null){
			allTransition.remove(trans);
		}
	}

	void removeTransitions(Collection<StateTransition> transes){
		if(transes!=null){
			allTransition.removeAll(transes);
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

	public void allScaleCenterMove(double scaleX,double scaleY){
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

	public void updateDefaultYOrder(){
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
				dn.get(i).nth = i;
				no++;
			}
		}
	}

	StateNode pickANode(Point p){
		StateNode pick = null;
		for(StateNode node : getAllNode()){
			if(node == null){ continue; }
			if(node.contains(p)){
				pick = node;
			}
		}
		return pick;
	}

	StateTransition pickATransition(Point p){
		StateTransition pick = null;
		for(StateTransition trans : getAllTransition()){
			if(trans == null){ continue; }
			if(trans.contains(p)){
				pick = trans;
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

	public void setAllWeak(boolean weak){
		for(StateNode node : getAllNode()){ node.weak = weak; }
		for(StateTransition t : getAllTransition()){ t.weak = weak; }
	}

	public void allNodeUnMark(){
		for(StateNode node : getAllNode()){
			node.unmark();
		}
	}

	/*
	public boolean setSlimNdResult(String str){
		Env.startWatch("parsing[1]");

		HashMap<Long,TempNode> temps = new HashMap<Long,TempNode>();
		TempNode startNode = null;
		int line=0;

		String[] strs = str.split("\n");

		Env.stopWatch("parsing[1]");
		Env.startWatch("parsing[2]");

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


		Env.stopWatch("parsing[2]");
		Env.startWatch("buliding");


		setNodeFrom(temps);

		// temp -> state
		makeAllFromTemps(temps,startNode);

		setTreeDepth();

		resetOrder();
		positionReset();

		if(Env.is("SV_STARTUP_SET_BACKDUMMY")){
			setDummy();
		}

		updateNodeLooks();


		Env.stopWatch("buliding");


		return true;
	}

	public boolean setSlimLtlResult(String str){
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
		positionReset();

		if(Env.is("SV_STARTUP_SET_BACKDUMMY")){
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
			StateNode node = allNode.get(id);
			setLastOrder(node);
		}


		updateNodeLooks();

		return true;
	}
	 */

	/*
	private StateNode makeDummyNode(long id,int depth,String label,boolean accept,boolean inCycle,double y,boolean weak){

		StateNode dummy = new StateNode(id, this);
		dummy.init("",label,accept,inCycle);
		dummy.dummy = true;
		dummy.weak = weak;

		// 位置の設定
		dummy.setX(depthNode.get(depth).get(0).getX());
		dummy.setY(y);

		// depth, nthの設定
		insertDepthNode(dummy,depth);

		return dummy;
	}

	private StateNode getNewNode(long id){
		StateNode node = allNode.get(id);
		if(node==null){
			node = new StateNode(id,this);
			allNode.put(id,node);
		}
		return node;
	}

	private void setNodeFrom(HashMap<Long,TempNode> temps){
		for(TempNode node : temps.values()){
			for(TempTransition to : node.toes){
				temps.get(to.node).setFromId(node.id);
			}
		}
	}

	private void makeAllFromTemps(HashMap<Long,TempNode> temps,TempNode startNode){
		for(TempNode temp: temps.values()){
			StateNode node = getNewNode(temp.id);
			node.init(temp.state, temp.label, temp.accept, temp.inCycle);

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
		this.startNode.add(getNewNode(startNode.id));
		updateMaxNodeId();
	}

	 */

	/*
	public void reduction(){
		StateNode removenode;
		while((removenode=getToOneFromOne())!=null){
			innerRemove(removenode);
		}
		updateNodeLooks();
	}
	 */

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

	/*
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
	 */

}
