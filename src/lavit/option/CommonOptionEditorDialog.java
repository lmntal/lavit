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

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lavit.Env;

/**
 * タブ中に並べるオプションスイッチをとりあえず編集する用
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
class CommonOptionEditorDialog extends JDialog
{
	private static String[] visibleOptionKeys =
	{
		"options.lmntal",
		"options.unyo",
		"options.lmntal_slim",
		"options.slim",
		"options.stateviewer",
		"options.ltl",
	};

	private JComboBox comboKeys;
	private JTextArea edit;
	private JButton buttonApply;
	private JButton buttonClose;

	public CommonOptionEditorDialog()
	{
		setTitle("Displaying Options");

		JLabel labelDesc = new JLabel("Edit the list of option switches to display in Option tab.");
		labelDesc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		comboKeys = new JComboBox();
		for (String key : visibleOptionKeys)
		{
			comboKeys.addItem(key);
		}
		comboKeys.setEditable(false);
		comboKeys.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				load();
			}
		});
		add(comboKeys);

		edit = new JTextArea();
		JScrollPane editJsp = new JScrollPane(edit);
		add(editJsp);

		buttonApply = new JButton("Apply");
		buttonApply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				store();
			}
		});
		add(buttonApply);

		buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		add(buttonClose);

		GroupLayout gl = new GroupLayout(getContentPane());
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.TRAILING)
			.addComponent(labelDesc)
			.addComponent(comboKeys)
			.addComponent(editJsp)
			.addGroup(gl.createSequentialGroup()
				.addComponent(buttonApply)
				.addComponent(buttonClose)
			)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addComponent(labelDesc)
			.addComponent(comboKeys, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(editJsp, 0, 200, Short.MAX_VALUE)
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(buttonApply)
				.addComponent(buttonClose)
			)
		);
		getContentPane().setLayout(gl);

		load();
	}

	private void load()
	{
		String key = (String)comboKeys.getSelectedItem();
		edit.setText(Env.get(key, "").replaceAll("\\s+", "\n"));
	}

	private void store()
	{
		String key = (String)comboKeys.getSelectedItem();
		String value = edit.getText().replaceAll("\\s+", " ");
		Env.set(key, value);
		JOptionPane.showMessageDialog(this, "To apply this change, please restart LaViT.", "Apply", JOptionPane.INFORMATION_MESSAGE);
	}
}
