package lavit.stateviewer;

import java.util.ArrayList;
import java.util.LinkedList;

import lavit.Env;
import lavit.util.StateTransitionCatcher;

public class StateTransitionAbstraction implements StateTransitionCatcher {

	private StateGraphPanel graphPanel;

	public StateTransitionAbstraction(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;
	}

	@Override
	public void transitionCatch(String ruleName,ArrayList<StateTransition> trans) {
		StateNodeSet drawNodes = graphPanel.getDrawNodes();
		graphPanel.selectNodeClear();

		boolean dummy = Env.is("SV_DUMMY");
		Env.set("SV_DUMMY",false);
		drawNodes.removeDummy();

		while(true){

			drawNodes.allNodeUnMark();
			ArrayList<StateNode> nodes = new ArrayList<StateNode>(drawNodes.getAllNode());
			ArrayList<StateNode> transitionGroup = new ArrayList<StateNode>();
			while(nodes.size()>0){
				transitionGroup.clear();
				LinkedList<StateNode> queue = new LinkedList<StateNode>();

				queue.add(nodes.get(0));
				transitionGroup.add(nodes.get(0));
				nodes.get(0).mark();
				nodes.remove(nodes.get(0));

				while(!queue.isEmpty()){
					StateNode node = queue.remove();
					for(StateNode n : node.getRuleNameGroupNodes(ruleName)){
						if(n.isMarked()){continue;}
						queue.add(n);
						transitionGroup.add(n);
						n.mark();
						nodes.remove(n);
					}
				}
				if(transitionGroup.size()>=2){
					break;
				}
			}

			if(transitionGroup.size()<=1){ break; }

			long maxId = Long.MIN_VALUE;
			for(StateNode node : drawNodes.getAllNode()){
				if(node.id>maxId){
					maxId = node.id;
				}
			}

			long id = ++maxId;
			boolean accept = false;
			boolean inCycle = false;
			boolean start = false;
			ArrayList<StateTransition> toes = new ArrayList<StateTransition>();
			ArrayList<StateNode> fromNodes = new ArrayList<StateNode>();

			StateNode newNode = new StateNode(id);
			for(StateNode node : transitionGroup){
				if(node.accept){ accept = true; }
				if(node.inCycle){ inCycle = true; }
				if(node.depth==0){ start = true; }
				ArrayList<StateNode> removeToes = new ArrayList<StateNode>();
				ArrayList<StateNode> removeFroms = new ArrayList<StateNode>();

				for(StateTransition t : node.getTransition()){
					if(!transitionGroup.contains(t.to)){
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
					drawNodes.removeTransition(t);
				}
				for(StateNode f : node.getFromNodes()){
					StateTransition t =  f.getTransition(node);
					if(!transitionGroup.contains(f)){
						//新しいfromを追加
						if(!fromNodes.contains(f)){
							fromNodes.add(f);
						}
						drawNodes.addTransition(f.addToNode(newNode, t.getRules(), t.em));
						//削除
						removeFroms.add(f);
						f.removeToNode(node);
					}
					drawNodes.removeTransition(t);
				}
				for(StateNode to : removeToes){
					node.removeToNode(to);
				}
				for(StateNode from : removeFroms){
					node.removeFromNode(from);
				}
				drawNodes.removeInnerNodeData(node);
			}

			newNode.init("","", accept, inCycle);

			//新しい位置の決定
			double x=0,y=0;
			for(StateNode node : transitionGroup){
				x+=node.getX();
				y+=node.getY();
			}
			x/=(double)(transitionGroup.size());
			y/=(double)(transitionGroup.size());
			double minD = Double.MAX_VALUE;
			double bestX = transitionGroup.get(0).getX();
			for(StateNode node : transitionGroup){
				double d = Math.abs(node.getX()-x);
				if(d<minD){
					bestX = node.getX();
					minD = d;
				}
			}
			newNode.setPosition(bestX, y);
			newNode.setSubset(new StateNodeSet());
			newNode.getSubset().setSubNode(newNode, transitionGroup);
			newNode.setTransition(toes);
			newNode.setFromNode(fromNodes);

			drawNodes.addNode(newNode);
			if(start){ drawNodes.setStartNode(newNode); }
		}


		drawNodes.setTreeDepth();
		drawNodes.resetOrder();

		Env.set("SV_DUMMY",dummy);
		if(Env.is("SV_DUMMY")){
			drawNodes.setDummy();
		}

		drawNodes.updateNodeLooks();

		graphPanel.update();
	}

	StateTransition getInTransition(ArrayList<StateTransition> toes,StateNode toNode){
		for(StateTransition trans : toes){
			if(trans.to==toNode){
				return trans;
			}
		}
		return null;
	}

}
