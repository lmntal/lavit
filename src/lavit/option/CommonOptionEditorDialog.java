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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
	private Map<String, String> optionSettings = new HashMap<String, String>();

	public CommonOptionEditorDialog()
	{
		setTitle("Displaying Options");
		setLayout(new BorderLayout());

		JLabel labelDesc = new JLabel("Edit option switches in Option tab.");
		labelDesc.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		labelDesc.setBackground(Color.WHITE);
		labelDesc.setOpaque(true);
		add(labelDesc, BorderLayout.NORTH);

		initContentPanel();
		initButtonPanel();
		load();

		pack();
	}

	private void initContentPanel()
	{
		JPanel p = new JPanel();

		comboKeys = new JComboBox();
		for (String key : visibleOptionKeys)
		{
			optionSettings.put(key, Env.get(key, ""));
			comboKeys.addItem(key);
		}
		comboKeys.setEditable(false);
		comboKeys.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				switch (e.getStateChange())
				{
				case ItemEvent.DESELECTED:
					update();
					break;
				case ItemEvent.SELECTED:
					load();
					break;
				}
			}
		});

		edit = new JTextArea();
		JScrollPane editJsp = new JScrollPane(edit);
		editJsp.setPreferredSize(new Dimension(300, 150));

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.LEADING)
			.addComponent(comboKeys)
			.addComponent(editJsp)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addComponent(comboKeys, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(editJsp)
		);
		p.setLayout(gl);

		add(p, BorderLayout.CENTER);
	}

	private void initButtonPanel()
	{
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(5, 5, 5, 5),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)
			)
		));

		buttonApply = new JButton("Apply");
		buttonApply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				store();
			}
		});
		p.add(buttonApply);

		buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		});
		p.add(buttonClose);

		Dimension size = buttonApply.getPreferredSize();
		size.width = Math.max(size.width, buttonClose.getPreferredSize().width);
		size.width = Math.max(size.width, 100);
		buttonApply.setPreferredSize(size);
		buttonClose.setPreferredSize(size);

		add(p, BorderLayout.SOUTH);
	}

	private void load()
	{
		String key = (String)comboKeys.getSelectedItem();
		edit.setText(optionSettings.get(key).replaceAll("\\s+", "\n"));
		edit.setCaretPosition(0);
	}

	private void update()
	{
		String key = (String)comboKeys.getSelectedItem();
		optionSettings.put(key, edit.getText().replaceAll("\\s+", " "));
	}

	private void store()
	{
		update();
		for (Map.Entry<String, String> entry : optionSettings.entrySet())
		{
			Env.set(entry.getKey(), entry.getValue());
		}
		JOptionPane.showMessageDialog(this, "Please restart LaViT to apply the changes.", "Apply Changes", JOptionPane.INFORMATION_MESSAGE);
	}

	private void close()
	{
		dispose();
	}
}
