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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.stateviewer.*;

public class StateTransitionRuleFrame extends JFrame {
	private SelectPanel panel;
	private boolean end;
	private StateGraphPanel graphPanel;
	private HashMap<String,ArrayList<StateTransition>> rules = new HashMap<String,ArrayList<StateTransition>>();

	public StateTransitionRuleFrame(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;

		for(StateTransition t : graphPanel.getDrawNodes().getAllTransition()){
			for(String r : t.getRules()){
				if(!rules.containsKey(r)){
					rules.put(r,new ArrayList<StateTransition>());
				}
				rules.get(r).add(t);
			}
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Rules");
        setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
        setAlwaysOnTop(true);
        setResizable(false);

        panel = new SelectPanel(this);
        add(panel);

        addWindowListener(new ChildWindowListener(this));

        pack();
        setLocationRelativeTo(FrontEnd.mainFrame);
        setVisible(true);

        end = false;
	}

	public boolean isEnd(){
		return end;
	}

	public void end(){
		end = true;
	}

	private class SelectPanel extends JPanel implements ActionListener {
		private JFrame frame;
		private JButton[] ruleButtons;

		SelectPanel(JFrame frame){
			this.frame = frame;

			//ルール名の集合
			ArrayList<String> rs = new ArrayList<String>(rules.keySet());
			Collections.sort(rs);

			setLayout(new GridLayout(rs.size(),2));

			ruleButtons = new JButton[rs.size()];
			for(int i=0;i<rs.size();++i){
				ruleButtons[i] = new JButton(rs.get(i));
				ruleButtons[i].addActionListener(this);
				add(ruleButtons[i]);

				int c = 0;
				for(StateTransition t : rules.get(rs.get(i))){
					if(!t.isToDummy()){ c++; }
				}

				String cs = ""+c;
				if(c<1000){ cs = " "+cs; }
				if(c<100 ){ cs = " "+cs; }
				if(c<10  ){ cs = " "+cs; }
				add(new JLabel(" : "+cs+" transition(s) "));

			}

		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			String rule = ((JButton)src).getText();
			ArrayList<StateTransition> trans = rules.get(rule);
			graphPanel.emTransitions(trans);
			frame.dispose();
		}

	}
}
