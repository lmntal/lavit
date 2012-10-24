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

/**
 * コマンドラインオプション設定パネルの処理を共通化した（対症療法的）抽象コンポーネント。
 * 継承の必然性はなく、コンポジットとファクトリによる設計に切り替える可能性もある。
 */
@SuppressWarnings("serial")
abstract class AbstractOptionPanel extends JPanel
{
	private String propertyName;
	private String[] majorOptions;
	private JCheckBox[] optionBoxes;
	private JTextField optionField;

	/**
	 * @param title パネルのタイトル
	 * @param propertyName env.txt に保存する設定のキー名
	 * @param options チェックボックスを表示するオプション名の配列
	 */
	protected AbstractOptionPanel(String title, String propertyName, String[] options)
	{
		majorOptions = options;
		this.propertyName = propertyName;

		setLayout(new FixFlowLayout());
		setBorder(new TitledBorder(title));

		ChangeHandler handler = new ChangeHandler();
		optionBoxes = new JCheckBox[majorOptions.length];
		for (int i = 0; i < majorOptions.length; i++)
		{
			optionBoxes[i] = new JCheckBox(majorOptions[i]);
			optionBoxes[i].addActionListener(handler);
			add(optionBoxes[i]);
		}

		optionField = new JTextField(16);
		optionField.getDocument().addDocumentListener(handler);
		add(optionField);

		initializeFields();
	}

	private void initializeFields()
	{
		for (JCheckBox checkBox : optionBoxes)
		{
			checkBox.setSelected(false);
		}

		String[] options = Env.get(propertyName).split("\\s+");
		String fieldText = "";
		for (String o : options)
		{
			boolean exist = false;
			for (int i = 0; i < majorOptions.length; i++)
			{
				if (majorOptions[i].equals(o))
				{
					optionBoxes[i].setSelected(true);
					exist = true;
				}
			}
			if (!exist)
			{
				if (!fieldText.isEmpty())
				{
					fieldText += " ";
				}
				fieldText += o;
			}
		}
		optionField.setText(fieldText);
	}

	private void applyChanges()
	{
		String newOptions = "";
		for (JCheckBox checkBox : optionBoxes)
		{
			if (checkBox.isSelected())
			{
				if (!newOptions.isEmpty())
				{
					newOptions += " ";
				}
				newOptions += checkBox.getText();
			}
		}
		String field = optionField.getText();
		if (!field.isEmpty())
		{
			if (!newOptions.isEmpty())
			{
				newOptions += " ";
			}
			newOptions += optionField.getText();
		}
		Env.set(propertyName, newOptions);
	}

	private class ChangeHandler implements ActionListener, DocumentListener
	{
		public void actionPerformed(ActionEvent e)
		{
			applyChanges();
		}

		public void changedUpdate(DocumentEvent e)
		{
			applyChanges();
		}

		public void insertUpdate(DocumentEvent e)
		{
			applyChanges();
		}

		public void removeUpdate(DocumentEvent e)
		{
			applyChanges();
		}
	}
}
