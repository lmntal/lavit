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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.util.FixFlowLayout;

@SuppressWarnings("serial")
public class GeneralSettingFrame extends JDialog
{
	public GeneralSettingFrame()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Preferences");
		setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
		setAlwaysOnTop(true);
		setPreferredSize(new Dimension(500, 400));

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		//panel.add(new SlimLibSettingPanel());
		panel.add(new EditorColorPanel());
		panel.add(new FontSettingPanel());
		panel.add(new EncodingSettingPanel());
		panel.add(new ViewSettingPanel());
		panel.add(new UIFontSizeSettingPanel());

		add(panel, BorderLayout.CENTER);

		addWindowListener(new ChildWindowListener(this));

		pack();
		setLocationRelativeTo(FrontEnd.mainFrame);
		setVisible(true);
	}

	private static class EditorColorPanel extends JPanel implements ActionListener
	{
		private final String majorOption[] = { "comment", "symbol", "reserved" };
		private JCheckBox optionCheckBox[] = new JCheckBox[majorOption.length];
		private JCheckBox optShowEols;
		private JCheckBox optShowTabs;

		public EditorColorPanel()
		{
			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("Color"));

			for (int i = 0; i < majorOption.length; ++i)
			{
				optionCheckBox[i] = new JCheckBox(majorOption[i]);
				add(optionCheckBox[i]);
			}
			settingInit();

			for (int i = 0; i < majorOption.length; ++i)
			{
				optionCheckBox[i].addActionListener(this);
			}

			optShowEols = new JCheckBox("Show Line Delimiters");
			optShowEols.setSelected(Env.is("SHOW_LINE_DELIMITERS"));
			add(optShowEols);
			optShowEols.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Env.set("SHOW_LINE_DELIMITERS", optShowEols.isSelected());
					FrontEnd.mainFrame.editorPanel.updateHighlight();
				}
			});

			optShowTabs = new JCheckBox("Show Tabs");
			optShowTabs.setSelected(Env.is("SHOW_TABS"));
			add(optShowTabs);
			optShowTabs.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Env.set("SHOW_TABS", optShowTabs.isSelected());
					FrontEnd.mainFrame.editorPanel.updateHighlight();
				}
			});
		}

		private void settingInit()
		{
			for (int i = 0; i < majorOption.length; ++i)
			{
				optionCheckBox[i].setSelected(false);
			}
			String[] options = Env.get("COLOR_TARGET").split(" ");
			for (String o : options)
			{
				for (int i = 0; i < majorOption.length; ++i)
				{
					if (majorOption[i].equals(o))
					{
						optionCheckBox[i].setSelected(true);
					}
				}
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			String newOptions = "";
			for (int i = 0; i < majorOption.length; ++i)
			{
				if (optionCheckBox[i].isSelected())
				{
					if (newOptions.length() == 0)
					{
						newOptions += optionCheckBox[i].getText();
					}
					else
					{
						newOptions += " " + optionCheckBox[i].getText();
					}
				}
			}
			Env.set("COLOR_TARGET", newOptions);
			FrontEnd.mainFrame.editorPanel.updateHighlight();
		}
	}

	private class FontSettingPanel extends JPanel implements ActionListener
	{
		private String tabSizeList[] = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };

		private JComboBox fontFamilyComboBox;
		private JComboBox fontSizeComboBox;
		private JComboBox tabSizeComboBox;

		public FontSettingPanel()
		{
			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("Editor Font"));

			add(new JLabel("FontFamily"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			fontFamilyComboBox = new JComboBox(ge.getAvailableFontFamilyNames());
			add(fontFamilyComboBox);

			add(new JLabel("FontSize"));
			fontSizeComboBox = new JComboBox(Env.FONT_SIZE_LIST);
			add(fontSizeComboBox);

			add(new JLabel("TabSize"));
			tabSizeComboBox = new JComboBox(tabSizeList);
			add(tabSizeComboBox);

			settingInit();

			fontFamilyComboBox.addActionListener(this);
			fontSizeComboBox.addActionListener(this);
			tabSizeComboBox.addActionListener(this);
		}

		private void settingInit()
		{
			fontFamilyComboBox.setSelectedItem(Env.get("EDITER_FONT_FAMILY"));
			fontSizeComboBox.setSelectedItem(Env.get("EDITER_FONT_SIZE"));
			tabSizeComboBox.setSelectedItem(Env.get("EDITER_TAB_SIZE"));
		}

		public void actionPerformed(ActionEvent e)
		{
			Env.set("EDITER_FONT_FAMILY", (String)fontFamilyComboBox.getSelectedItem());
			Env.set("EDITER_FONT_SIZE", (String)fontSizeComboBox.getSelectedItem());
			Env.set("EDITER_TAB_SIZE", (String)tabSizeComboBox.getSelectedItem());
			/*
			FrontEnd.mainFrame.editorPanel.loadFont();
			FrontEnd.mainFrame.toolTab.systemPanel.logPanel.loadFont();
			FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.loadFont();
			FrontEnd.mainFrame.toolTab.statePanel.stateGraphPanel.loadFont();
			*/
			FrontEnd.loadAllFont();

		}

	}

	private class EncodingSettingPanel extends JPanel implements ActionListener
	{
		private String encodingList[] = { "SJIS", "EUC_JP", "ISO2022JP", "UTF8" };

		private JComboBox readComboBox;
		private JComboBox writeComboBox;

		public EncodingSettingPanel()
		{
			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("File Encoding"));

			add(new JLabel("READ"));
			readComboBox = new JComboBox(encodingList);
			add(readComboBox);

			add(new JLabel("WRITE"));
			writeComboBox = new JComboBox(encodingList);
			add(writeComboBox);

			settingInit();

			readComboBox.addActionListener(this);
			writeComboBox.addActionListener(this);
		}

		private void settingInit()
		{
			readComboBox.setSelectedItem(Env.get("EDITER_FILE_READ_ENCODING"));
			writeComboBox.setSelectedItem(Env.get("EDITER_FILE_WRITE_ENCODING"));
		}

		public void actionPerformed(ActionEvent e)
		{
			Env.set("EDITER_FILE_READ_ENCODING", (String)readComboBox.getSelectedItem());
			Env.set("EDITER_FILE_WRITE_ENCODING", (String)writeComboBox.getSelectedItem());
		}
	}

	private class SlimLibSettingPanel extends JPanel implements ActionListener
	{
		private JCheckBox useCheckBox;

		public SlimLibSettingPanel()
		{
			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("SLIM LIBRARY"));

			useCheckBox = new JCheckBox("Use slim library");
			useCheckBox.addActionListener(this);
			useCheckBox.setSelected(Env.is("SLIM_USE_LIBRARY"));
			add(useCheckBox);
		}

		public void actionPerformed(ActionEvent e)
		{
			Env.set("SLIM_USE_LIBRARY", useCheckBox.isSelected());
		}
	}

	private class ViewSettingPanel extends JPanel
	{
		private String langList[] = { "jp", "en" };

		private JComboBox langComboBox;
		private JComboBox lookAndFeelComboBox;

		public ViewSettingPanel()
		{
			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("View"));

			add(new JLabel("Lang"));
			langComboBox = new JComboBox(langList);
			langComboBox.setSelectedItem(Env.get("LANG"));
			langComboBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Env.set("LANG", (String)langComboBox.getSelectedItem());
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							JOptionPane.showMessageDialog(
								GeneralSettingFrame.this, Lang.f[4],
								"Change Language", JOptionPane.PLAIN_MESSAGE);
						}
					});
				}
			});
			add(langComboBox);

			add(new JLabel("LookAndFeel"));
			String lafName = Env.get("LookAndFeel");
			lookAndFeelComboBox = new JComboBox();
			for (LookAndFeelEntry ent : LookAndFeelEntry.getSupportedLookAndFeelEntries())
			{
				lookAndFeelComboBox.addItem(ent);
				if (lafName.equals(ent.getName()))
				{
					lookAndFeelComboBox.setSelectedItem(ent);
				}
			}
			lookAndFeelComboBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					LookAndFeelEntry lafEntry = (LookAndFeelEntry)lookAndFeelComboBox.getSelectedItem();
					FrontEnd.setLookAndFeel(lafEntry);
				}
			});
			add(lookAndFeelComboBox);
		}
	}

	private class UIFontSizeSettingPanel extends JPanel
	{
		private JButton buttonInc;
		private JButton buttonDec;

		public UIFontSizeSettingPanel()
		{
			setBorder(BorderFactory.createTitledBorder("UI Font Size"));

			buttonInc = new JButton("+");
			buttonInc.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					addToFontSizeAll(1);
				}
			});

			buttonDec = new JButton("-");
			buttonDec.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					addToFontSizeAll(-1);
				}
			});

			add(buttonInc);
			add(buttonDec);
		}

		private void addToFontSizeAll(int a)
		{
			// set all
			for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet())
			{
				String key = entry.getKey().toString();
				if (key.toLowerCase().endsWith("font"))
				{
					Font font = UIManager.getFont(key);
					float size = font.getSize() + a;
					if (size > 0)
					{
						UIManager.put(key,
							new FontUIResource(font.deriveFont(size)));
					}
				}
			}

			// fire updates
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						for (Window window : Window.getWindows())
						{
							SwingUtilities.updateComponentTreeUI(window);
						}
					}
					catch (Exception e)
					{
						FrontEnd.printException(e);
					}
				}
			});
		}
	}

	// under developed
	private class UISettingPanel extends JPanel
	{
		private JList uiFontList;
		private JComboBox uiFonts;
		private JSpinner uiFontSize;
		private JButton buttonApply;
		private JCheckBox isBold;
		private JCheckBox isItalic;

		public UISettingPanel()
		{
			DefaultListModel listModel = new DefaultListModel();
			List<String> keys = new ArrayList<String>();
			for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet())
			{
				String key = entry.getKey().toString();
				if (key.toLowerCase().endsWith("font"))
				{
					keys.add(key);
				}
			}
			Collections.sort(keys);
			for (String item : keys) listModel.addElement(item);

			uiFontList = new JList(listModel);
			uiFontList.setVisibleRowCount(5);
			uiFontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			uiFontList.addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					setSelectedFont();
				}
			});
			add(new JScrollPane(uiFontList));

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			uiFonts = new JComboBox(ge.getAvailableFontFamilyNames());
			add(uiFonts);

			isBold = new JCheckBox("Bold");
			add(isBold);

			isItalic = new JCheckBox("Italic");
			add(isItalic);

			uiFontSize = new JSpinner(new SpinnerNumberModel(12, 8, 96, 1));
			add(uiFontSize);

			buttonApply = new JButton("Apply");
			buttonApply.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					apply();
				}
			});
			add(buttonApply);
		}

		private void setSelectedFont()
		{
			String key = (String)uiFontList.getSelectedValue();
			Font font = UIManager.getFont(key);
			if (font != null)
			{
				uiFonts.setSelectedItem(font.getName());
				uiFontSize.setValue(font.getSize());
				isBold.setSelected(font.isBold());
				isItalic.setSelected(font.isItalic());
			}
		}

		private void apply()
		{
			String key = (String)uiFontList.getSelectedValue();

			String fontName = (String)uiFonts.getSelectedItem();
			int fontSize = (Integer)uiFontSize.getValue();
			int style = Font.PLAIN;
			if (isBold.isSelected()) style |= Font.BOLD;
			if (isItalic.isSelected()) style |= Font.ITALIC;

			Font font = new Font(fontName, style, fontSize);

			UIManager.put(key, new FontUIResource(font));
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						for (Window window : Window.getWindows())
						{
							SwingUtilities.updateComponentTreeUI(window);
						}
					}
					catch (Exception e)
					{
						FrontEnd.printException(e);
					}
				}
			});
		}
	}
}
