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
import java.util.LinkedList;

public class StateTransitionEm implements StateTransitionCatcher {
	private StateGraphPanel graphPanel;

	public StateTransitionEm(StateGraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}

	@Override
	public void transitionCatch(Collection<StateRule> rules, Collection<StateTransition> trans) {
		/*
		 * StateNodeSet drawNodes = graphPanel.getDrawNodes(); ArrayList<StateNode>
		 * weaks = new ArrayList<StateNode>(drawNodes.getAllNode());
		 * 
		 * for(StateTransition t : trans){ t.from.inCycle = true; weaks.remove(t.from);
		 * 
		 * t.to.inCycle = true; weaks.remove(t.to);
		 * 
		 * t.from.setEmToNode(t.to, true); }
		 * 
		 * for(StateNode node : weaks){ node.weak = true; node.updateLooks(); }
		 * graphPanel.update();
		 */

		StateNodeSet drawNodes = graphPanel.getDrawNodes();
		ArrayList<StateNode> weakNodes = new ArrayList<StateNode>(drawNodes.getAllNode());
		ArrayList<StateTransition> weakTransitions = new ArrayList<StateTransition>(drawNodes.getAllTransition());

		for (StateTransition t : trans) {
			drawNodes.setLastOrder(t);
			weakNodes.remove(t.from);
			weakNodes.remove(t.to);
			weakTransitions.remove(t);
		}

		for (StateNode node : weakNodes) {
			node.weak = true;
		}
		for (StateTransition t : weakTransitions) {
			t.weak = true;
		}

		graphPanel.getDrawNodes().updateNodeLooks();
		graphPanel.getDraw().setSearchMode(true);
		graphPanel.update();
	}
}
