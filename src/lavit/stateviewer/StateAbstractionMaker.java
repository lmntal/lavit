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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import lavit.Env;

public class StateAbstractionMaker {

	private StateGraphPanel graphPanel;
	StateNodeSet drawNodes;
	boolean dummy;
	long newId;

	public StateAbstractionMaker(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;
		this.drawNodes = graphPanel.getDrawNodes();
		this.dummy = Env.is("SV_DUMMY");

		Env.set("SV_DUMMY",false);
		drawNodes.removeDummy();
		graphPanel.selectNodeClear();

		newId = drawNodes.getMaxNodeId();
	}

	public void makeNode(Collection<StateNode> groupNodes){

		long id = ++newId;
		boolean accept = false;
		boolean inCycle = false;
		boolean start = false;
		LinkedHashSet<StateTransition> toes = new LinkedHashSet<StateTransition>();
		LinkedHashSet<StateNode> fromNodes = new LinkedHashSet<StateNode>();
		LinkedHashSet<StateTransition> removeTrans = new LinkedHashSet<StateTransition>();

		StateNode newNode = new StateNode(id, drawNodes);
		for(StateNode node : groupNodes){
			if(node.dummy){ continue; }

			if(node.accept){ accept = true; }
			if(node.inCycle){ inCycle = true; }
			if(node.depth==0){ start = true; }
			LinkedList<StateNode> removeToes = new LinkedList<StateNode>();
			LinkedList<StateNode> removeFroms = new LinkedList<StateNode>();

			Env.startWatch("Worker[2-1]");

			for(StateTransition t : node.getTransition()){
				if(!groupNodes.contains(t.to)){

					//新しいtransを追加
					StateTransition existTrans = getInTransition(toes, t.to);
					if(existTrans==null){
						StateTransition newTrans = new StateTransition(newNode, t.to, t.em);
						newTrans.addRules(t.getRules());
						toes.add(newTrans);
						drawNodes.addTransition(newTrans);
					}else{
						if(t.em){ existTrans.em = true; }
						for(String r : t.getRules()){
							if(!existTrans.getRules().contains(r)){
								existTrans.getRules().add(r);
							}
						}
					}
					t.to.addFromNode(newNode);
					//削除
					removeToes.add(t.to);
					t.to.removeFromNode(node);
				}
				removeTrans.add(t);
			}

			Env.stopWatch("Worker[2-1]");
			Env.startWatch("Worker[2-2]");

			for(StateNode f : node.getFromNodes()){

				StateTransition t =  f.getTransition(node);
				if(!groupNodes.contains(f)){
					//新しいfromを追加
					if(!fromNodes.contains(f)){
						fromNodes.add(f);
					}
					drawNodes.addTransition(f.addToNode(newNode, t.getRules(), t.em));
					//削除
					removeFroms.add(f);
					f.removeToNode(node);
				}
				removeTrans.add(t);
			}

			Env.stopWatch("Worker[2-2]");
			Env.startWatch("Worker[2-3]");

			for(StateNode to : removeToes){
				node.removeToNode(to);
			}
			for(StateNode from : removeFroms){
				node.removeFromNode(from);
			}
			drawNodes.removeInnerNodeData(node);

			Env.stopWatch("Worker[2-3]");
		}

		drawNodes.removeTransitions(removeTrans);

		newNode.init("","", accept, inCycle);

		//新しい位置の決定
		double x=0,y=0;
		for(StateNode node : groupNodes){
			x+=node.getX();
			y+=node.getY();
		}
		x/=(double)(groupNodes.size());
		y/=(double)(groupNodes.size());
		double minD = Double.MAX_VALUE;
		double bestX = 0;
		for(StateNode node : groupNodes){
			double d = Math.abs(node.getX()-x);
			if(d<minD){
				bestX = node.getX();
				minD = d;
			}
		}

		StateNodeSet child = new StateNodeSet(newNode);
		child.setSubNode(groupNodes, removeTrans);
		newNode.setChildSet(child);

		newNode.setPosition(bestX, y);
		newNode.setTransition(toes);
		newNode.setFromNode(fromNodes);

		drawNodes.addNode(newNode);
		if(start){ drawNodes.setStartNode(newNode); }
		if(newNode.getToNodes().size()==0){ drawNodes.addEndNode(newNode); }
	}

	public void end(){
		drawNodes.setTreeDepth();
		drawNodes.resetOrder();
		drawNodes.updateMaxNodeId();

		Env.set("SV_DUMMY",dummy);
		if(Env.is("SV_DUMMY")){
			drawNodes.setDummy();
		}
		drawNodes.updateNodeLooks();

		graphPanel.update();
	}

	StateTransition getInTransition(LinkedHashSet<StateTransition> toes,StateNode toNode){
		for(StateTransition trans : toes){
			if(trans.to==toNode){
				return trans;
			}
		}
		return null;
	}

}
