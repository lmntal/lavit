package lavit.stateviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import lavit.Env;
import lavit.stateviewer.worker.StateGraphStretchMoveWorker;
import lavit.stateviewer.worker.StateTransitionAbstractionWorker;
import lavit.util.StateTransitionCatcher;

public class StateTransitionAbstraction implements StateTransitionCatcher {

	private StateGraphPanel graphPanel;

	public StateTransitionAbstraction(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;
	}

	@Override
	public void transitionCatch(Collection<String> rules, Collection<StateTransition> trans) {

		StateTransitionAbstractionWorker worker = new StateTransitionAbstractionWorker(graphPanel);
		if(trans.size()<500){
			worker.atomic(rules, trans);
		}else{
			worker.ready(rules, trans);
			worker.execute();
		}

	}

}
