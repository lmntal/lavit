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

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;

@SuppressWarnings("serial")
public class StateUnderInfoPanel extends JPanel {

	public StatePanel statePanel;

	private JLabel zoomNum = new JLabel();
	private JLabel stateNum = new JLabel();

	StateUnderInfoPanel(StatePanel statePanel) {
		this.statePanel = statePanel;

		setLayout(new GridLayout(1, 2));

		zoomNum.setHorizontalAlignment(JLabel.LEFT);
		add(zoomNum);

		stateNum.setHorizontalAlignment(JLabel.RIGHT);
		add(stateNum);

		setDrawInfo(0, 0);
		setStateInfo(0, 0, 0);
	}

	public void updateInfo() {
		StateGraphPanel p = statePanel.stateGraphPanel;
		setDrawInfo(p.getZoom(), p.getDrawTime());
		setStateInfo(p.getDepth() - 1, p.getAllNum(), p.getEndNum());
	}

	private void setDrawInfo(double zoom, double drawTime) {
		String z = "" + (int) (zoom * 100);
		if (zoom < 0.01) {
			z = "" + (((int) (zoom * 10000)) / 100.0);
		}
		zoomNum.setText(" Zoom : " + z + "%, DrawTime : " + (drawTime / 1000) + "s");
	}

	private void setStateInfo(int depth, int num, int end) {
		stateNum.setText(" Depth : " + depth + ", State : " + num + ", (End : " + end + ") ");
	}

}
