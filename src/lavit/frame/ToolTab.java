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

package lavit.frame;

import java.awt.Dimension;

import javax.swing.JTabbedPane;

import lavit.ltl.LtlPanel;
import lavit.oldstateviewer.StatePanel;
import lavit.option.OptionPanel;
import lavit.stateprofiler.StateProfilePanel;
import lavit.system.SystemPanel;
import lavit.visualeditor.VisualPanel;

public class ToolTab extends JTabbedPane {

	public SystemPanel systemPanel;
	public VisualPanel visualPanel;
	public LtlPanel ltlPanel;
	public StatePanel statePanel;
	public StateProfilePanel stateProfilePanel;
	public OptionPanel optionPanel;

	public ToolTab(){
		setMinimumSize(new Dimension(0,0));
		setFocusable(false);

		systemPanel = new SystemPanel();
		addTab("System", systemPanel);

		//visualPanel = new VisualPanel();
		//addTab("Visual", visualPanel);

		ltlPanel = new LtlPanel();
		addTab("LTL Model Check", ltlPanel);

		statePanel = new StatePanel();
		addTab("StateViewer", statePanel);

		stateProfilePanel = new StateProfilePanel();
		addTab("StateProfiler", stateProfilePanel);

		optionPanel = new OptionPanel();
		addTab("Option", optionPanel);

	}

	public void setTab(String tab){
		if(tab.equals("System")){
			setSelectedComponent(systemPanel);
		}else if(tab.equals("StateViewer")){
			setSelectedComponent(statePanel);
		}else if(tab.equals("StateProfiler")){
			setSelectedComponent(stateProfilePanel);
		}
	}

}
