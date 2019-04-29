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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lavit.Env;
import lavit.util.StringUtils;

/**
 * タブ中に並べるオプションスイッチをとりあえず編集する用
 *
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
class CommonOptionEditorDialog extends JDialog {
	private static String[] visibleOptionKeys = { "options.lmntal", "options.unyo", "options.lmntal_slim", "options.slim",
			"options.stateviewer", "options.ltl", };

	private JComboBox<String> comboKeys;
	private JTextArea edit;
	private JButton buttonApply;
	private JButton buttonClose;
	private CheckBoxPanel checkBoxPanel;
	private Map<String, String> optionSettings = new HashMap<String, String>();

	public CommonOptionEditorDialog() {
		setTitle("Displaying Options");
		setLayout(new BorderLayout());

		JLabel labelDesc = new JLabel("Edit option switches in Option tab.");
		labelDesc.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		labelDesc.setBackground(Color.WHITE);
		labelDesc.setOpaque(true);
		add(labelDesc, BorderLayout.NORTH);

		initContentPanel();
		initButtonPanel();

		loadEnvironment();
		load();

		pack();
	}

	private void initContentPanel() {
		JPanel p = new JPanel();

		comboKeys = new JComboBox<String>();
		for (String key : visibleOptionKeys) {
			comboKeys.addItem(key);
		}
		comboKeys.setEditable(false);
		comboKeys.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				switch (e.getStateChange()) {
				case ItemEvent.DESELECTED:
					update((String) e.getItem());
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

		checkBoxPanel = new CheckBoxPanel();
		checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Open on startup"));
		checkBoxPanel.addItem("lmntal", "LMNtal Options");
		checkBoxPanel.addItem("unyo", "UNYO Options");
		checkBoxPanel.addItem("slim-compile", "SLIM Compile Options");
		checkBoxPanel.addItem("slim", "SLIM Options");
		checkBoxPanel.addItem("sv", "StateViewer Options");
		checkBoxPanel.addItem("ltl", "LTL Model Check Options");

		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.LEADING).addComponent(comboKeys).addComponent(editJsp)
				.addComponent(checkBoxPanel));
		gl.setVerticalGroup(gl.createSequentialGroup()
				.addComponent(comboKeys, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(editJsp).addComponent(checkBoxPanel));
		p.setLayout(gl);

		add(p, BorderLayout.CENTER);
	}

	private void initButtonPanel() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
						BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE))));

		buttonApply = new JButton("Apply");
		buttonApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveEnvironment();
			}
		});
		p.add(buttonApply);

		buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

	private void load() {
		String key = (String) comboKeys.getSelectedItem();
		edit.setText(optionSettings.get(key).replaceAll("\\s+", "\n"));
		edit.setCaretPosition(0);
	}

	private void update() {
		update((String) comboKeys.getSelectedItem());
	}

	private void update(String key) {
		optionSettings.put(key, edit.getText().replaceAll("\\s+", " "));
	}

	private void loadEnvironment() {
		for (String key : visibleOptionKeys) {
			optionSettings.put(key, Env.get(key, ""));
		}

		String values = Env.get("window.controls.switches.expanded", "");
		checkBoxPanel.setSelectedKeys(StringUtils.splitToSet(values, "\\s+"));
	}

	private void saveEnvironment() {
		update();

		for (Map.Entry<String, String> entry : optionSettings.entrySet()) {
			Env.set(entry.getKey(), entry.getValue());
		}

		Set<String> openOptions = new LinkedHashSet<String>();
		for (String key : checkBoxPanel.getSelectedKeys()) {
			openOptions.add(key);
		}
		Env.set("window.controls.switches.expanded", StringUtils.join(openOptions, " "));

		JOptionPane.showMessageDialog(this, "Please restart LaViT to apply the changes.", "Apply Changes",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void close() {
		dispose();
	}
}

@SuppressWarnings("serial")
class CheckBoxPanel extends JPanel {
	private Map<String, JCheckBox> checks = new HashMap<String, JCheckBox>();

	public CheckBoxPanel() {
		setLayout(new GridLayout(0, 3));
	}

	public void addItem(String key, String text) {
		if (checks.containsKey(key)) {
			throw new RuntimeException("key " + key + " already exists");
		}
		JCheckBox checkBox = new JCheckBox(text);
		checks.put(key, checkBox);
		add(checkBox);
	}

	public void setSelectedKeys(Set<String> keys) {
		for (Map.Entry<String, JCheckBox> entry : checks.entrySet()) {
			entry.getValue().setSelected(keys.contains(entry.getKey()));
		}
	}

	public Set<String> getSelectedKeys() {
		Set<String> keys = new LinkedHashSet<String>();
		for (Map.Entry<String, JCheckBox> entry : checks.entrySet()) {
			if (entry.getValue().isSelected()) {
				keys.add(entry.getKey());
			}
		}
		return keys;
	}
}
