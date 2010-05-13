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

package lavit.option;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.util.FixFlowLayout;

public class ViewSettingPanel  extends JPanel implements ActionListener{

	String langList[] = {"jp","en"};
	String lookAndFeelList[] = {"Metal","Motif","Windows","WindowsClassic","SystemDefault"};

	JComboBox lookAndFeelComboBox;
	JComboBox langComboBox;

	ViewSettingPanel(){

		setLayout(new FixFlowLayout());
		setBorder(new TitledBorder("View"));

		add(new JLabel("Lang"));
		langComboBox = new JComboBox(langList);
		add(langComboBox);

		add(new JLabel("LookAndFeel"));
		lookAndFeelComboBox = new JComboBox(lookAndFeelList);
		add(lookAndFeelComboBox);

		settingInit();

		lookAndFeelComboBox.addActionListener(this);
		langComboBox.addActionListener(this);

	}

	void settingInit(){
		for(String str : lookAndFeelList){
			if(str.equals(Env.get("LookAndFeel"))){
				lookAndFeelComboBox.setSelectedItem(str);
				break;
			}
		}
		for(String str : langList){
			if(str.equals(Env.get("LANG"))){
				langComboBox.setSelectedItem(str);
				break;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==langComboBox){
			Env.set("LANG",(String)langComboBox.getSelectedItem());
			JOptionPane.showMessageDialog(FrontEnd.mainFrame,Lang.f[4],"Change Language",JOptionPane.PLAIN_MESSAGE);
		}else{
			Env.set("LookAndFeel",(String)lookAndFeelComboBox.getSelectedItem());
			FrontEnd.updateLookAndFeel();
			SwingUtilities.updateComponentTreeUI(FrontEnd.mainFrame);
		}
	}

}
