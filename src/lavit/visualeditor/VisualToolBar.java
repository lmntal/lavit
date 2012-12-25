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

package lavit.visualeditor;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class VisualToolBar extends JToolBar implements ActionListener {
	VisualPanel visualPanel;

	private ArrayList<JToggleButton> buttons;

	JToggleButton selected;

	JToggleButton selectButton;
	JToggleButton atomButton;
	JToggleButton linkButton;
	JToggleButton memButton;
	JToggleButton ruleButton;

	VisualToolBar(VisualPanel visualPanel){
		this.visualPanel = visualPanel;

		buttons = new ArrayList<JToggleButton>();

		selectButton = new JToggleButton("選択");
		selectButton.addActionListener(this);
		selectButton.setFocusable(false);
		selectButton.setMargin(new Insets(10, 10, 10, 10));
		buttons.add(selectButton);
		add(selectButton);

		addSeparator();

		atomButton = new JToggleButton("アトム");
		atomButton.addActionListener(this);
		atomButton.setFocusable(false);
		atomButton.setMargin(new Insets(10, 10, 10, 10));
		buttons.add(atomButton);
		add(atomButton);

		linkButton = new JToggleButton("リンク");
		linkButton.addActionListener(this);
		linkButton.setFocusable(false);
		linkButton.setMargin(new Insets(10, 10, 10, 10));
		buttons.add(linkButton);
		add(linkButton);

		memButton = new JToggleButton("膜");
		memButton.addActionListener(this);
		memButton.setFocusable(false);
		memButton.setMargin(new Insets(10, 10, 10, 10));
		buttons.add(memButton);
		add(memButton);

		ruleButton = new JToggleButton("ルール");
		ruleButton.addActionListener(this);
		ruleButton.setFocusable(false);
		ruleButton.setMargin(new Insets(10, 10, 10, 10));
		buttons.add(ruleButton);
		add(ruleButton);

		selectButton.setSelected(true);
		selected = selectButton;

	}

	public void allButtonSetEnabled(boolean enabled){
		selectButton.setEnabled(enabled);
		atomButton.setEnabled(enabled);
		linkButton.setEnabled(enabled);
		memButton.setEnabled(enabled);
		ruleButton.setEnabled(enabled);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		int select = 0;
		for(JToggleButton b : buttons){
			if(b.isSelected()){ ++select; }
			b.setSelected(false);
		}
		if(select==0){
			selected.setSelected(true);
		}else{
			JToggleButton btn = (JToggleButton)src;
			btn.setSelected(true);
			selected = btn;
		}

		visualPanel.drawPanel.update();
	}

}
