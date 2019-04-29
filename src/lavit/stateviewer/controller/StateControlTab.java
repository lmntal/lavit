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

package lavit.stateviewer.controller;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.Env;
import lavit.stateviewer.StatePanel;

public class StateControlTab extends JTabbedPane {

	public StatePanel statePanel;

	public StateButtonPanel buttonPanel;
	public StateSimulationPanel simulationPanel;
	public StateSearchPanel searchPanel;
	public StateDynamicControlPanel dynamicPanel;
	public State3DControlPanel s3dPanel;
	public StateOtherPanel otherPanel;
	public StateBetaPanel betaPanel;

	StateControlTab(StatePanel statePanel) {
		this.statePanel = statePanel;

		buttonPanel = new StateButtonPanel(statePanel);
		addTab("Control Button", buttonPanel);

		simulationPanel = new StateSimulationPanel(statePanel);
		addTab("Simulation", simulationPanel);

		searchPanel = new StateSearchPanel(statePanel);
		addTab("Search", searchPanel);

		dynamicPanel = new StateDynamicControlPanel(statePanel);
		addTab("Dynamic", dynamicPanel);

		s3dPanel = new State3DControlPanel(statePanel);
		addTab("3D", s3dPanel);

		otherPanel = new StateOtherPanel(statePanel);
		addTab("Other", otherPanel);

		betaPanel = new StateBetaPanel(statePanel);
		addTab("Beta", betaPanel);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		dynamicPanel.setEnabled(enabled);
		searchPanel.setEnabled(enabled);
		buttonPanel.setEnabled(enabled);
		s3dPanel.setEnabled(enabled);
		otherPanel.setEnabled(enabled);
		betaPanel.setEnabled(enabled);
	}

	public void setTab(String tab) {
		if (tab.equals("3D")) {
			setSelectedComponent(s3dPanel);
		}
	}
}
