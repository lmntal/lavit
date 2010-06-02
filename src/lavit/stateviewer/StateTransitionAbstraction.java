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
		StateAbstractionMaker maker = new StateAbstractionMaker(graphPanel);
		StateNodeSet drawNodes = graphPanel.getDrawNodes();

		while(true){

			drawNodes.allNodeUnMark();
			LinkedList<StateNode> nodes = new LinkedList<StateNode>(drawNodes.getAllNode());
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
					ArrayList<StateNode> ns = node.getRuleNameGroupNodes(ruleName);

					for(StateNode n : ns){
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

			maker.makeNode(transitionGroup);

		}

		maker.end();
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
