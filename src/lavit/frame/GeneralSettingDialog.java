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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LookAndFeel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;

import lavit.Env;
import lavit.FrontEnd;
import lavit.frame.propertyeditor.PropertyEditorDialog;
import lavit.localizedtext.MsgID;
import lavit.util.FixFlowLayout;
import lavit.util.LookAndFeelEntry;
import lavit.util.StringUtils;
import lavit.util.TextAntialias;

@SuppressWarnings("serial")
public class GeneralSettingDialog extends JDialog {
	private static LookAndFeel laf;
	private static GeneralSettingDialog instance;

	private EditorColorPanel panelEditor;
	private FontSettingPanel panelFont;
	private UIFontSizeSettingPanel panelUIFont;

	private PropertyEditorDialog propertyDialog;

	private GeneralSettingDialog(Window owner) {
		super(owner);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Preferences");
		setIconImages(Env.getApplicationIcons());
		setAlwaysOnTop(true);

		JPanel panel = new JPanel();

		// panel.add(new SlimLibSettingPanel());
		panelEditor = new EditorColorPanel();
		panelFont = new FontSettingPanel();
		JPanel panelEncoding = new EncodingSettingPanel();
		JPanel panelView = new ViewSettingPanel();
		panelUIFont = new UIFontSizeSettingPanel();

		JButton buttonEditProperty = new JButton("Edit properties (env.txt)");
		buttonEditProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPropertyEditDialog();
			}
		});

		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.CENTER).addComponent(panelEditor).addComponent(panelFont)
				.addGroup(gl.createSequentialGroup().addComponent(panelEncoding).addComponent(panelView))
				.addGroup(gl.createSequentialGroup().addComponent(panelUIFont).addComponent(buttonEditProperty)));
		gl.setVerticalGroup(gl.createSequentialGroup().addComponent(panelEditor).addComponent(panelFont)
				.addGroup(gl.createParallelGroup(Alignment.LEADING).addComponent(panelEncoding).addComponent(panelView))
				.addGroup(gl.createParallelGroup(Alignment.CENTER).addComponent(panelUIFont).addComponent(buttonEditProperty)));

		add(panel, BorderLayout.CENTER);

		JButton buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		Dimension dim = buttonClose.getPreferredSize();
		dim.width = Math.max(dim.width, 100);
		buttonClose.setPreferredSize(dim);

		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panelButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
						BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE))));
		panelButtons.add(buttonClose);
		add(panelButtons, BorderLayout.SOUTH);

		addWindowListener(new ChildWindowListener(this));
	}

	public void initialize() {
		panelEditor.initialize();
		panelFont.initialize();
	}

	public static synchronized void showDialog() {
		LookAndFeel currentLaf = UIManager.getLookAndFeel();
		if (instance == null || !currentLaf.equals(laf)) {
			laf = currentLaf;
			instance = new GeneralSettingDialog(FrontEnd.mainFrame);
			instance.pack();
			instance.setLocationRelativeTo(null);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				instance.initialize();
				instance.pack();
				instance.setVisible(true);
			}
		});
	}

	private void showPropertyEditDialog() {
		final Window owner = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (propertyDialog == null) {
					propertyDialog = new PropertyEditorDialog(owner);
					propertyDialog.setLocationRelativeTo(owner);
				}
				propertyDialog.setVisible(true);
			}
		});
	}

	private void close() {
		setVisible(false);
	}

	private static class EditorColorPanel extends JPanel {
		private JCheckBox checkComment;
		private JCheckBox checkSymbol;
		private JCheckBox checkKeyword;
		private JCheckBox checkRulename;
		private JCheckBox optShowEols;
		private JCheckBox optShowTabs;

		public EditorColorPanel() {
			setBorder(new TitledBorder("Color"));

			checkComment = new JCheckBox("Comment");
			checkSymbol = new JCheckBox("Symbol");
			checkKeyword = new JCheckBox("Keyword");
			checkRulename = new JCheckBox("Rule Name");

			ActionListener l = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateColorSettings();
				}
			};
			checkComment.addActionListener(l);
			checkSymbol.addActionListener(l);
			checkKeyword.addActionListener(l);
			checkRulename.addActionListener(l);

			optShowEols = new JCheckBox("Show Line Delimiters");
			optShowEols.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Env.set("SHOW_LINE_DELIMITERS", optShowEols.isSelected());
					FrontEnd.mainFrame.editorPanel.updateHighlight();
				}
			});

			optShowTabs = new JCheckBox("Show Tabs");
			optShowTabs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Env.set("SHOW_TABS", optShowTabs.isSelected());
					FrontEnd.mainFrame.editorPanel.updateHighlight();
				}
			});

			Component pad1 = Box.createHorizontalGlue();
			Component pad2 = Box.createHorizontalGlue();

			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			gl.setAutoCreateGaps(true);
			gl.setAutoCreateContainerGaps(true);
			gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(pad1)
					.addGroup(gl.createParallelGroup()
							.addGroup(gl.createSequentialGroup().addComponent(checkComment).addComponent(checkSymbol)
									.addComponent(checkKeyword).addComponent(checkRulename))
							.addGroup(gl.createSequentialGroup().addComponent(optShowEols).addComponent(optShowTabs)))
					.addComponent(pad2));
			gl.setVerticalGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(pad1)
					.addGroup(gl.createSequentialGroup().addComponent(checkComment).addComponent(optShowEols))
					.addGroup(gl.createSequentialGroup().addComponent(checkSymbol).addComponent(optShowTabs))
					.addComponent(checkKeyword).addComponent(checkRulename).addComponent(pad2));

			initialize();
		}

		public void initialize() {
			Set<String> options = new HashSet<String>(Arrays.asList(Env.get("COLOR_TARGET").split("\\s+")));
			checkComment.setSelected(options.contains("comment"));
			checkSymbol.setSelected(options.contains("symbol"));
			checkKeyword.setSelected(options.contains("reserved"));
			checkRulename.setSelected(options.contains("rulename"));
			optShowEols.setSelected(Env.is("SHOW_LINE_DELIMITERS"));
			optShowTabs.setSelected(Env.is("SHOW_TABS"));
		}

		private void updateColorSettings() {
			Set<String> options = new HashSet<String>();
			if (checkComment.isSelected()) {
				options.add("comment");
			}
			if (checkSymbol.isSelected()) {
				options.add("symbol");
			}
			if (checkKeyword.isSelected()) {
				options.add("reserved");
			}
			if (checkRulename.isSelected()) {
				options.add("rulename");
			}
			Env.set("COLOR_TARGET", StringUtils.join(options, " "));
			FrontEnd.mainFrame.editorPanel.updateHighlight();
		}
	}

	private static class FontSettingPanel extends JPanel {
		private JComboBox<String> fontFamilyComboBox;
		private JSpinner fontSizeController;
		private JComboBox<String> tabSizeComboBox;

		public FontSettingPanel() {
			setBorder(new TitledBorder("Editor Font"));

			JLabel labelFamily = new JLabel("Family:");

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			fontFamilyComboBox = new JComboBox<String>(ge.getAvailableFontFamilyNames());

			JLabel labelSize = new JLabel("Size:");

			fontSizeController = new JSpinner(new SpinnerNumberModel(12, 1, 120, 1));

			JLabel labelTabWidth = new JLabel("Tab width:");

			tabSizeComboBox = new JComboBox<String>();
			for (int tabWidth = 1; tabWidth <= 10; tabWidth++) {
				tabSizeComboBox.addItem(String.valueOf(tabWidth));
			}

			JLabel labelTextAA = new JLabel("Antialias:");
			final JComboBox<TextAntialias> comboTextAA = new JComboBox<TextAntialias>();
			for (TextAntialias item : TextAntialias.values()) {
				comboTextAA.addItem(item);
			}
			TextAntialias textaa = TextAntialias.of(Env.get("editor.antialias", ""));
			if (textaa != null) {
				comboTextAA.setSelectedItem(textaa);
			}

			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			gl.setAutoCreateGaps(true);
			gl.setAutoCreateContainerGaps(true);
			gl.setHorizontalGroup(gl.createParallelGroup()
					.addGroup(gl.createSequentialGroup().addComponent(labelFamily).addComponent(fontFamilyComboBox)
							.addComponent(labelSize).addComponent(fontSizeController).addComponent(labelTabWidth)
							.addComponent(tabSizeComboBox))
					.addGroup(gl.createSequentialGroup().addComponent(labelTextAA).addComponent(comboTextAA,
							GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
			gl.setVerticalGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(labelFamily)
							.addComponent(fontFamilyComboBox).addComponent(labelSize).addComponent(fontSizeController)
							.addComponent(labelTabWidth).addComponent(tabSizeComboBox))
					.addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(labelTextAA).addComponent(comboTextAA)));

			fontFamilyComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Env.set("EDITOR_FONT_FAMILY", (String) fontFamilyComboBox.getSelectedItem());
					FrontEnd.loadAllFont();
				}
			});
			fontSizeController.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int fontSize = (Integer) fontSizeController.getValue();
					Env.set("EDITOR_FONT_SIZE", fontSize);
					FrontEnd.loadAllFont();
				}
			});
			tabSizeComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Env.set("EDITOR_TAB_SIZE", (String) tabSizeComboBox.getSelectedItem());
					FrontEnd.loadAllFont();
				}
			});
			comboTextAA.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TextAntialias textaa = (TextAntialias) comboTextAA.getSelectedItem();
					Env.set("editor.antialias", textaa.name);
					FrontEnd.mainFrame.repaint();
				}
			});

			initialize();
		}

		public void initialize() {
			fontFamilyComboBox.setSelectedItem(Env.get("EDITOR_FONT_FAMILY"));
			fontSizeController.setValue(Env.getInt("EDITOR_FONT_SIZE", 12));
			tabSizeComboBox.setSelectedItem(Env.get("EDITOR_TAB_SIZE"));
		}
	}

	private static class EncodingSettingPanel extends JPanel {
		private String[] encodingList = { "SJIS", "EUC_JP", "ISO2022JP", "UTF8" };

		private JComboBox<String> readComboBox;
		private JComboBox<String> writeComboBox;

		public EncodingSettingPanel() {
			setBorder(new TitledBorder("File Encoding"));

			JLabel labelRead = new JLabel("Read as:");
			readComboBox = new JComboBox<String>(encodingList);
			readComboBox.setSelectedItem(Env.get("EDITER_FILE_READ_ENCODING"));
			readComboBox.setMaximumSize(readComboBox.getPreferredSize());
			readComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSelectedReadEncoding();
				}
			});

			JLabel labelWrite = new JLabel("Write as:");
			writeComboBox = new JComboBox<String>(encodingList);
			writeComboBox.setSelectedItem(Env.get("EDITER_FILE_WRITE_ENCODING"));
			writeComboBox.setMaximumSize(writeComboBox.getPreferredSize());
			writeComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSelectedWriteEncoding();
				}
			});

			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			gl.setAutoCreateGaps(true);
			gl.setAutoCreateContainerGaps(true);
			gl.setHorizontalGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup(Alignment.TRAILING).addComponent(labelRead).addComponent(labelWrite))
					.addGroup(gl.createParallelGroup().addComponent(readComboBox).addComponent(writeComboBox)));
			gl.setVerticalGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(labelRead).addComponent(readComboBox))
					.addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(labelWrite).addComponent(writeComboBox)));
		}

		private void setSelectedReadEncoding() {
			Env.set("EDITOR_FILE_READ_ENCODING", (String) readComboBox.getSelectedItem());
		}

		private void setSelectedWriteEncoding() {
			Env.set("EDITOR_FILE_WRITE_ENCODING", (String) writeComboBox.getSelectedItem());
		}
	}

	@SuppressWarnings("unused")
	private class SlimLibSettingPanel extends JPanel implements ActionListener {
		private JCheckBox useCheckBox;

		public SlimLibSettingPanel() {
			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("SLIM LIBRARY"));

			useCheckBox = new JCheckBox("Use slim library");
			useCheckBox.addActionListener(this);
			useCheckBox.setSelected(Env.is("SLIM_USE_LIBRARY"));
			add(useCheckBox);
		}

		public void actionPerformed(ActionEvent e) {
			Env.set("SLIM_USE_LIBRARY", useCheckBox.isSelected());
		}
	}

	private static class ViewSettingPanel extends JPanel {
		private static class Item {
			public final String id;
			public final String desc;

			public Item(String id, String desc) {
				this.id = id;
				this.desc = desc;
			}

			public String toString() {
				return desc;
			}
		}

		private JComboBox<Item> langComboBox;
		private JComboBox<LookAndFeelEntry> lookAndFeelComboBox;

		public ViewSettingPanel() {
			setBorder(new TitledBorder("View"));

			JLabel labelLanguage = new JLabel("Language:");
			langComboBox = new JComboBox<Item>();
			langComboBox.addItem(new Item("ja", "Japanese (ja)"));
			langComboBox.addItem(new Item("en", "English (en)"));
			String langCode = Env.get("LANG");
			for (int i = 0; i < langComboBox.getItemCount(); i++) {
				Item item = (Item) langComboBox.getItemAt(i);
				if (item.id.equals(langCode)) {
					langComboBox.setSelectedItem(item);
				}
			}
			langComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					changeLanguage();
				}
			});

			JLabel labelLaf = new JLabel("LookAndFeel:");
			String lafName = Env.get("LookAndFeel");
			lookAndFeelComboBox = new JComboBox<LookAndFeelEntry>();
			for (LookAndFeelEntry ent : LookAndFeelEntry.getSupportedLookAndFeelEntries()) {
				lookAndFeelComboBox.addItem(ent);
				if (lafName.equals(ent.getName())) {
					lookAndFeelComboBox.setSelectedItem(ent);
				}
			}
			lookAndFeelComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					changeLAF();
				}
			});

			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			gl.setAutoCreateGaps(true);
			gl.setAutoCreateContainerGaps(true);
			gl.setHorizontalGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup(Alignment.TRAILING).addComponent(labelLanguage).addComponent(labelLaf))
					.addGroup(
							gl.createParallelGroup(Alignment.LEADING).addComponent(langComboBox).addComponent(lookAndFeelComboBox)));
			gl.setVerticalGroup(gl.createSequentialGroup()
					.addGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(labelLanguage).addComponent(langComboBox))
					.addGroup(
							gl.createParallelGroup(Alignment.BASELINE).addComponent(labelLaf).addComponent(lookAndFeelComboBox)));
		}

		private void changeLanguage() {
			Item item = (Item) langComboBox.getSelectedItem();
			Env.set("LANG", item.id);

			final JComponent parent = this;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(parent, Env.getMsg(MsgID.text_please_reboot), "Reboot required",
							JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}

		private void changeLAF() {
			LookAndFeelEntry lafEntry = (LookAndFeelEntry) lookAndFeelComboBox.getSelectedItem();
			FrontEnd.setLookAndFeel(lafEntry);
		}
	}

	private static class UIFontSizeSettingPanel extends JPanel {
		public UIFontSizeSettingPanel() {
			setBorder(BorderFactory.createTitledBorder("UI Font Size"));

			JButton buttonInc = new JButton("+");
			buttonInc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addToFontSizeAll(1);
				}
			});

			JButton buttonDec = new JButton("-");
			buttonDec.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addToFontSizeAll(-1);
				}
			});

			Dimension dim1 = buttonInc.getPreferredSize();
			Dimension dim2 = buttonDec.getPreferredSize();
			dim1.width = Math.max(Math.max(dim1.width, dim2.width), 30);
			buttonInc.setPreferredSize(dim1);
			buttonInc.setMaximumSize(dim1);
			buttonInc.setMinimumSize(dim1);
			buttonDec.setPreferredSize(dim1);
			buttonDec.setMaximumSize(dim1);
			buttonDec.setMinimumSize(dim1);

			Component padLeft = Box.createHorizontalGlue();
			Component padRight = Box.createHorizontalGlue();

			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			gl.setAutoCreateContainerGaps(true);
			gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(padLeft).addComponent(buttonInc)
					.addComponent(buttonDec).addComponent(padRight));
			gl.setVerticalGroup(gl.createParallelGroup(Alignment.BASELINE).addComponent(padLeft).addComponent(buttonInc)
					.addComponent(buttonDec).addComponent(padRight));
		}

		private void addToFontSizeAll(int a) {
			// update env
			Env.set("UI_FONT_SIZE_DIFF", Env.getInt("UI_FONT_SIZE_DIFF", 0) + a);

			// set all
			for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
				String key = entry.getKey().toString();
				if (key.toLowerCase().endsWith("font")) {
					Font font = UIManager.getFont(key);
					float size = font.getSize() + a;
					if (size > 0) {
						UIManager.put(key, new FontUIResource(font.deriveFont(size)));
					}
				}
			}

			// fire updates
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						for (Window window : Window.getWindows()) {
							SwingUtilities.updateComponentTreeUI(window);
						}
					} catch (Exception e) {
						FrontEnd.printException(e);
					}
				}
			});
		}
	}

	// // under developed
	// @SuppressWarnings("unused")
	// private class UISettingPanel extends JPanel {
	// private JList uiFontList;
	// private JComboBox uiFonts;
	// private JSpinner uiFontSize;
	// private JButton buttonApply;
	// private JCheckBox isBold;
	// private JCheckBox isItalic;

	// public UISettingPanel() {
	// DefaultListModel listModel = new DefaultListModel();
	// List<String> keys = new ArrayList<String>();
	// for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
	// String key = entry.getKey().toString();
	// if (key.toLowerCase().endsWith("font")) {
	// keys.add(key);
	// }
	// }
	// Collections.sort(keys);
	// for (String item : keys)
	// listModel.addElement(item);

	// uiFontList = new JList(listModel);
	// uiFontList.setVisibleRowCount(5);
	// uiFontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// uiFontList.addListSelectionListener(new ListSelectionListener() {
	// @Override
	// public void valueChanged(ListSelectionEvent e) {
	// setSelectedFont();
	// }
	// });
	// add(new JScrollPane(uiFontList));

	// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	// uiFonts = new JComboBox(ge.getAvailableFontFamilyNames());
	// add(uiFonts);

	// isBold = new JCheckBox("Bold");
	// add(isBold);

	// isItalic = new JCheckBox("Italic");
	// add(isItalic);

	// uiFontSize = new JSpinner(new SpinnerNumberModel(12, 8, 96, 1));
	// add(uiFontSize);

	// buttonApply = new JButton("Apply");
	// buttonApply.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// apply();
	// }
	// });
	// add(buttonApply);
	// }

	// private void setSelectedFont() {
	// String key = (String) uiFontList.getSelectedValue();
	// Font font = UIManager.getFont(key);
	// if (font != null) {
	// uiFonts.setSelectedItem(font.getName());
	// uiFontSize.setValue(font.getSize());
	// isBold.setSelected(font.isBold());
	// isItalic.setSelected(font.isItalic());
	// }
	// }

	// private void apply() {
	// String key = (String) uiFontList.getSelectedValue();

	// String fontName = (String) uiFonts.getSelectedItem();
	// int fontSize = (Integer) uiFontSize.getValue();
	// int style = Font.PLAIN;
	// if (isBold.isSelected())
	// style |= Font.BOLD;
	// if (isItalic.isSelected())
	// style |= Font.ITALIC;

	// Font font = new Font(fontName, style, fontSize);

	// UIManager.put(key, new FontUIResource(font));
	// SwingUtilities.invokeLater(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// for (Window window : Window.getWindows()) {
	// SwingUtilities.updateComponentTreeUI(window);
	// }
	// } catch (Exception e) {
	// FrontEnd.printException(e);
	// }
	// }
	// });
	// }
	// }
}
