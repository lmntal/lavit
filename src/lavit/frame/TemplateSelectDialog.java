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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import lavit.Env;

class TemplateEntry {
	private final File file;

	public TemplateEntry(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		String name = file.getName();
		int i = name.lastIndexOf('.');
		if (i != -1) {
			name = name.substring(0, i);
		}
		return name;
	}

	public String toString() {
		return getName();
	}
}

@SuppressWarnings("serial")
public class TemplateSelectDialog extends JDialog {
	private static TemplateSelectDialog dialogInstance;

	private File templatesDir = new File("./templates");

	private JComboBox<TemplateEntry> templates;
	private JTextArea quickView;
	private JButton buttonOK;
	private JButton buttonCancel;
	private boolean accepted;

	public TemplateSelectDialog(Window owner) {
		super(owner);

		setIconImages(Env.getApplicationIcons());
		setTitle("Load template");
		setModalityType(ModalityType.APPLICATION_MODAL);

		JPanel contents = new JPanel();
		GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);

		JLabel label = new JLabel("Template file:");
		contents.add(label);

		//
		// templates
		//
		templates = new JComboBox<TemplateEntry>();
		Dimension dim = templates.getPreferredSize();
		dim.width = 100;
		templates.setPreferredSize(dim);
		List<TemplateEntry> entries = getEntries(templatesDir);
		for (TemplateEntry item : entries) {
			templates.addItem(item);
		}
		templates.setEnabled(templates.getItemCount() > 0);
		templates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TemplateEntry entry = (TemplateEntry) templates.getSelectedItem();
				viewTemplate(entry);
			}
		});
		contents.add(templates);

		//
		// quickView
		//
		quickView = new JTextArea();
		quickView.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
		quickView.setEditable(false);
		quickView.setRows(5);
		JScrollPane jsp = new JScrollPane(quickView);
		contents.add(jsp);

		//
		// GroupLayout
		//
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(label).addComponent(templates)).addComponent(jsp));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(templates))
				.addComponent(jsp));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		Dimension buttonSize = new Dimension(90, 22);
		buttonOK = new JButton("OK");
		buttonOK.setPreferredSize(buttonSize);
		buttonOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				accepted = true;
				dispose();
			}
		});
		buttonPanel.add(buttonOK);

		buttonCancel = new JButton("Cancel");
		buttonCancel.setPreferredSize(buttonSize);
		buttonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(buttonCancel);

		JLabel headerLabel = new JLabel("Select a template located on the directory \"./templates.\"");
		headerLabel.setOpaque(true);
		headerLabel.setBackground(Color.WHITE);
		headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(headerLabel, BorderLayout.NORTH);
		add(contents, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				accepted = false;
			}
		});

		pack();
		setLocationRelativeTo(owner);

		if (templates.isEnabled()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					templates.setSelectedIndex(0);
				}
			});
		}
	}

	private void viewTemplate(TemplateEntry entry) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				quickView.setText("");
			}
		});
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entry.getFile())));
			final StringBuilder buf = new StringBuilder();
			String line;
			if ((line = reader.readLine()) != null) {
				buf.append(line);
				while ((line = reader.readLine()) != null) {
					buf.append('\n');
					buf.append(line);
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					quickView.setText(buf.toString());
					quickView.setCaretPosition(0);
				}
			});
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<TemplateEntry> getEntries(File dir) {
		List<TemplateEntry> entries = new ArrayList<TemplateEntry>();
		if (dir.exists() && dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				if (child.isFile()) {
					entries.add(new TemplateEntry(child));
				}
			}
		}
		return entries;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public String getTemplateContents() {
		return quickView.getText();
	}

	public static TemplateSelectDialog create(Window owner) {
		if (dialogInstance == null) {
			dialogInstance = new TemplateSelectDialog(owner);
		}
		return dialogInstance;
	}
}
