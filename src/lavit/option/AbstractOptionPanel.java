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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lavit.Env;
import extgui.text.HintTextField;

/**
 * コマンドラインオプション設定パネルの処理を共通化した（対症療法的）抽象コンポーネント。
 * 継承の必然性はなく、コンポジットとファクトリによる設計に切り替える可能性もある。
 */
@SuppressWarnings("serial")
abstract class AbstractOptionPanel extends JPanel
{
	private String propertyName;
	private List<String> majorOptions = new ArrayList<String>();
	private JCheckBox[] optionBoxes;
	private JTextField optionField;

	/**
	 * @param title パネルのタイトル
	 * @param propertyName env.txt に保存する設定のキー名
	 * @param options チェックボックスを表示するオプション名の配列
	 */
	protected AbstractOptionPanel(String title, String propertyName, String[] options)
	{
		this.propertyName = propertyName;

		for (String option : options)
		{
			if (!option.isEmpty() && !majorOptions.contains(option))
			{
				majorOptions.add(option);
			}
		}

		setLayout(new BorderLayout());

		ChangeHandler handler = new ChangeHandler();
		JPanel checkBoxPanel = new JPanel(new GridLayout(0, 3));
		optionBoxes = new JCheckBox[majorOptions.size()];
		for (int i = 0; i < majorOptions.size(); i++)
		{
			String caption = majorOptions.get(i);
			optionBoxes[i] = new JCheckBox(caption);
			optionBoxes[i].setToolTipText(caption);
			optionBoxes[i].addActionListener(handler);
			checkBoxPanel.add(optionBoxes[i]);
		}
		add(checkBoxPanel, BorderLayout.CENTER);

		optionField = new HintTextField("", "<other options>");
		optionField.getDocument().addDocumentListener(handler);
		add(optionField, BorderLayout.SOUTH);

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
			for (int i = 0; i < majorOptions.size(); i++)
			{
				if (majorOptions.get(i).equals(o))
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
