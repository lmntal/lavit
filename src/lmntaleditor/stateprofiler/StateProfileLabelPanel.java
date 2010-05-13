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
 */package lmntaleditor.stateprofiler;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StateProfileLabelPanel extends JPanel{

	StateProfilePanel profile;
	private JLabel all;
	private JLabel speed;
	private JLabel clock;

	StateProfileLabelPanel(StateProfilePanel p){
		profile = p;

		setLayout(new GridLayout(1,2));

		all = new JLabel();
		all.setHorizontalAlignment(JLabel.RIGHT);
		add(all);

		clock = new JLabel();
		clock.setHorizontalAlignment(JLabel.RIGHT);
		add(clock);

		speed = new JLabel();
		speed.setHorizontalAlignment(JLabel.RIGHT);
		add(speed);

		update();

	}

	//スレッドセーフ
	public void update(){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
			int r0 = 0;
			if(profile.timeLine.size()>1){
				r0 = profile.allState.size();
			}
			int r1 = 0;
			if(profile.timeLine.size()>1){
				r1 = profile.timeLine.size()-1;
			}
			int r2 = 0;
			if(r1>0){
				r2 = r0/r1;
			}
			all.setText(" State : "+r0+" state ");
			clock.setText(" Clock : "+r1+" sec ");
			speed.setText(" Speed : "+r2+" state/sec ");
		}});
	}

}
