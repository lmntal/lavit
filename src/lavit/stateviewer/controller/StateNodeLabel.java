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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.StateNode;
import lavit.util.CommonFontUser;
import lavit.util.FixFlowLayout;

public class StateNodeLabel extends JPanel implements CommonFontUser {
	private JPanel stateStatus;
	private JLabel stateNodenum;
	private JLabel stateLabel;

	private JTextField stateTextField;

	public StateNodeLabel(){

		setLayout(new BorderLayout());
		setOpaque(false);

		stateStatus = new JPanel();
		stateStatus.setLayout(new GridLayout(1,2));
		stateStatus.setOpaque(false);
		stateNodenum = new JLabel();
		stateNodenum.setOpaque(false);
		stateNodenum.setHorizontalAlignment(JLabel.LEFT);
		stateStatus.add(stateNodenum);
		stateLabel = new JLabel();
		stateLabel.setOpaque(false);
		stateLabel.setHorizontalAlignment(JLabel.RIGHT);
		stateStatus.add(stateLabel);
		add(stateStatus, BorderLayout.NORTH);

		stateTextField = new JTextField();
		add(stateTextField, BorderLayout.CENTER);

		loadFont();
		FrontEnd.addFontUser(this);

	}

	public void loadFont(){
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		stateTextField.setFont(font);
		revalidate();
	}

	public void setNode(StateNode node){
		if(node.label.length()>0){
			stateLabel.setText("Label: "+node.label);
		}else{
			stateLabel.setText("");
		}
		if(node.hasSubset()){
			stateNodenum.setText("Node: "+node.getSubset().size());
		}else{
			stateNodenum.setText("");
		}
		stateTextField.setText(node.toString());
	}

	public void setNode(ArrayList<StateNode> nodes){
		if(nodes.size()==1){
			StateNode node = nodes.get(0);
			if(node.hasSubset()){
				stateNodenum.setText("Node: "+node.getSubset().size());
			}else{
				stateNodenum.setText("");
			}
			if(node.label.length()>0){
				stateLabel.setText("Label: "+node.label);
			}else{
				stateLabel.setText("");
			}
			stateTextField.setText(node.toString());
			stateTextField.setVisible(true);
			setVisible(true);
		}else if(nodes.size()>1){
			stateNodenum.setText("Node: "+nodes.size());
			stateLabel.setText("");
			stateTextField.setText("");
			stateTextField.setVisible(false);
			setVisible(true);
		}else{
			setVisible(false);
		}
	}

}
