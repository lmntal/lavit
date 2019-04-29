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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lavit.Env;
import lavit.stateviewer.StatePanel;

@SuppressWarnings("serial")
public class StateBetaPanel extends JPanel {
	private StatePanel statePanel;

	private SimpleModeChanger simpleModeChanger;
	private StartupResetChanger startupResetChanger;
	private GraphDrawChanger graphDrawChanger;

	StateBetaPanel(StatePanel statePanel) {
		this.statePanel = statePanel;

		simpleModeChanger = new SimpleModeChanger();
		add(simpleModeChanger);

		startupResetChanger = new StartupResetChanger();
		add(startupResetChanger);

		graphDrawChanger = new GraphDrawChanger();
		add(graphDrawChanger);

	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		simpleModeChanger.setEnabled(enabled);
		startupResetChanger.setEnabled(enabled);
	}

	class SimpleModeChanger extends JPanel implements ActionListener {
		private JComboBox<String> box;
		private String[] boxItems = { "auto", "true", "false" };

		SimpleModeChanger() {
			add(new JLabel("Simple Mode:"));
			box = new JComboBox<String>(boxItems);
			box.addActionListener(this);
			add(box);
			for (String str : boxItems) {
				if (str.equals(Env.get("SV_SIMPLE_MODE"))) {
					box.setSelectedItem(str);
					break;
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if (src == box) {
				Env.set("SV_SIMPLE_MODE", (String) box.getSelectedItem());
				statePanel.stateGraphPanel.updateSimpleMode();
				statePanel.stateGraphPanel.update();
			}
		}

		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			box.setEnabled(enabled);
		}
	}

	class StartupResetChanger extends JPanel implements ActionListener {
		private JComboBox<String> box;
		private String[] boxItems = { "none", "PositionReset", "AdjustReset", "AdjustBackReset", "AdjustFindReset",
				"SimpleMixAdjust", "DummyMixAdjust" };

		StartupResetChanger() {
			add(new JLabel("Startup Reset:"));
			box = new JComboBox<String>(boxItems);
			box.addActionListener(this);
			add(box);
			for (String str : boxItems) {
				if (str.equals(Env.get("SV_STARTUP_RESET_TYPE"))) {
					box.setSelectedItem(str);
					break;
				}
			}
		}

		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			box.setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if (src == box) {
				Env.set("SV_STARTUP_RESET_TYPE", (String) box.getSelectedItem());
			}
		}
	}

	class GraphDrawChanger extends JPanel implements ActionListener {
		private JComboBox<String> box;
		private String[] boxItems = { "BASIC", "FLOWNODE", "ATOMCOLOR", "BONE" };

		GraphDrawChanger() {
			add(new JLabel("Graph Draw Mode:"));
			box = new JComboBox<String>(boxItems);
			box.addActionListener(this);
			add(box);
			for (String str : boxItems) {
				if (str.equals(Env.get("SV_GRAPH_DRAW"))) {
					box.setSelectedItem(str);
					break;
				}
			}
		}

		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			box.setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if (src == box) {
				Env.set("SV_GRAPH_DRAW", (String) box.getSelectedItem());
				statePanel.stateGraphPanel.updateDraw();
				statePanel.stateGraphPanel.update();
			}
		}
	}

}
