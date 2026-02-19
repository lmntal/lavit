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
 */package lavit.mcprofiler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class MCProfileStatusPanel extends JPanel {

	MCProfileGraphPanel graphPanel;

	JPanel left = new JPanel();
	JPanel right = new JPanel();

	private JLabel lState;
	private JLabel lHashSize;
	private JLabel lHashConflict;

	MCProfileStatusPanel(MCProfileGraphPanel p) {
		graphPanel = p;

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new TitledBorder("Status"));

		left.setLayout(new GridLayout(3, 1));
		right.setLayout(new GridLayout(3, 1));
		JLabel l;

		l = new JLabel("State : ");
		l.setHorizontalAlignment(JLabel.RIGHT);
		left.add(l);

		l = new JLabel("Hash Size : ");
		l.setHorizontalAlignment(JLabel.RIGHT);
		left.add(l);

		l = new JLabel("Hash Conflict : ");
		l.setHorizontalAlignment(JLabel.RIGHT);
		left.add(l);

		lState = new JLabel("0");
		lState.setHorizontalAlignment(JLabel.RIGHT);
		lState.setForeground(graphPanel.cState);
		right.add(lState);

		lHashSize = new JLabel("0");
		lHashSize.setHorizontalAlignment(JLabel.RIGHT);
		lHashSize.setForeground(graphPanel.cHashSize);
		right.add(lHashSize);

		lHashConflict = new JLabel("0");
		lHashConflict.setHorizontalAlignment(JLabel.RIGHT);
		lHashConflict.setForeground(graphPanel.cHashConflict);
		right.add(lHashConflict);

		add(left);
		add(right);

		setBackgroundColor(new Color(255, 255, 255, 200));
	}

	public void setBackgroundColor(Color bg) {
		setBackground(bg);
		left.setBackground(bg);
		right.setBackground(bg);
	}

	public void paintComponent(Graphics g) {
		lState.setText("" + graphPanel.profile.allState.size());
		lHashSize.setText("" + graphPanel.profile.hashes.size());
		lHashConflict.setText("" + graphPanel.profile.hashConflict);
	}
}
