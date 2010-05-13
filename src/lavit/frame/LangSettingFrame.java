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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import lavit.Env;
import lavit.FrontEnd;

//言語の設定がされていないときのみ使用
public class LangSettingFrame extends JFrame {
	private SelectPanel panel;

	public LangSettingFrame(){

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Language");
        setIconImage(Env.getImageOfFile("img/icon.gif"));
        setAlwaysOnTop(true);
        setResizable(false);

        panel = new SelectPanel(this);
        add(panel);

        addWindowListener(new ChildWindowListener(this));

        pack();
        setLocationRelativeTo(FrontEnd.mainFrame);
        setVisible(true);
	}

	private class SelectPanel extends JPanel implements ActionListener {
		private JFrame frame;

		private ButtonGroup group = new ButtonGroup();
		private String[] labels = {"English","日本語"};
		private String[] langs = {"en","jp"};
		private JRadioButton[] radios = new JRadioButton[labels.length];

		private JButton ok = new JButton("OK");

		SelectPanel(JFrame frame){
			this.frame = frame;

			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			JLabel label = new JLabel();
			label.setText("Please select your languare.");
			label.setPreferredSize(new Dimension(250, 40));
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(label);

			JPanel radioPanel = new JPanel();
			radioPanel.setLayout(new GridLayout(3,1));

			for(int i=0;i<labels.length;++i){
				radios[i] = new JRadioButton(labels[i]);
				radios[i].setMargin(new Insets(2,10,2,10));
				group.add(radios[i]);
				radioPanel.add(radios[i]);
			}
			radios[0].setSelected(true);

			add(radioPanel);

			JPanel buttonPanel = new JPanel();

			ok.addActionListener(this);
			buttonPanel.add(ok);

			add(buttonPanel);

		}

		public void actionPerformed(ActionEvent e) {
			for(int i=0;i<labels.length;++i){
				if(radios[i].isSelected()){
					Env.set("LANG",langs[i]);
					break;
				}
			}
			frame.dispose();
		}

	}

}
