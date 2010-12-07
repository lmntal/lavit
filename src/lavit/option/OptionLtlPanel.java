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

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lavit.Env;
import lavit.util.FixFlowLayout;

public class OptionLtlPanel extends JPanel implements ActionListener,DocumentListener{

	String majorOption[] = {"--hideruleset","--hide-ruleset","-t","--dump-lavit","--mem-enc","--show-transition"};
	JCheckBox optionCheckBox[] = new JCheckBox[majorOption.length];
	JTextField optionField = new JTextField(15);

	OptionLtlPanel(){

		setLayout(new FixFlowLayout());
		setBorder(new TitledBorder("LTL Model Check SLIM Option"));

		for(int i=0;i<majorOption.length;++i){
			optionCheckBox[i] = new JCheckBox(majorOption[i]);
			add(optionCheckBox[i]);
		}
		add(optionField);

		settingInit();

		for(int i=0;i<majorOption.length;++i){
			optionCheckBox[i].addActionListener(this);
		}
		optionField.getDocument().addDocumentListener(this);

	}

	void settingInit(){
		for(int i=0;i<majorOption.length;++i){
			optionCheckBox[i].setSelected(false);
		}
		optionField.setText("");

		String[] options = Env.get("LTL_OPTION").split(" ");
		for(String o : options){
			boolean exist = false;
			for(int i=0;i<majorOption.length;++i){
				if(majorOption[i].equals(o)){
					optionCheckBox[i].setSelected(true);
					exist = true;
				}
			}
			if(!exist){
				String text = optionField.getText();
				if(text.length()==0){
					optionField.setText(o);
				}else{
					optionField.setText(text+" "+o);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String newOptions = "";
		for(int i=0;i<majorOption.length;++i){
			if(optionCheckBox[i].isSelected()){
				if(newOptions.length()==0){
					newOptions += optionCheckBox[i].getText();
				}else{
					newOptions += " " + optionCheckBox[i].getText();
				}
			}
		}
		String field = optionField.getText();
		if(field.length()>0){
			newOptions += " " + optionField.getText();
		}
		Env.set("LTL_OPTION",newOptions);
	}

	public void changedUpdate(DocumentEvent e) {
		actionPerformed(null);
	}

	public void insertUpdate(DocumentEvent e) {
		actionPerformed(null);
	}

	public void removeUpdate(DocumentEvent e) {
		actionPerformed(null);
	}

}
