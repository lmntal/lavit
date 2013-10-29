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

package lavit.multiedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Element;

import lavit.Env;
import lavit.multiedit.coloring.LmnTextPane;
import lavit.multiedit.coloring.event.DirtyFlagChangeListener;
import lavit.ui.FlatButton;
import extgui.filedrop.FileDropTransferHandler;
import extgui.filedrop.event.FileDropListener;

@SuppressWarnings("serial")
public class EditorPage extends JScrollPane
{
	private static Icon ICON_TAB_LMN;
	private static Icon ICON_TAB_IL;
	private static Icon ICON_TAB_OTHERS;
	private static Icon ICON_TAB_CLOSE_COOL;
	private static Icon ICON_TAB_CLOSE_HOT;

	private TabView host;
	private HeaderComponent header;
	private LineNumberView lineNumberView;

	private LmnTextPane text;
	private File file;
	private FileDropTransferHandler fileDropHandler;

	EditorPage(TabView hostTabView)
	{
		host = hostTabView;

		header = new HeaderComponent();

		text = new LmnTextPane();
		fileDropHandler = new FileDropTransferHandler(text.getTransferHandler());
		text.setTransferHandler(fileDropHandler);

		lineNumberView = new LineNumberView(text);
		setRowHeaderView(lineNumberView);
		setViewportView(text);

		getVerticalScrollBar().setUnitIncrement(15);

		addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				text.requestFocus();
			}
		});

		text.addDirtyFlagChangeListener(new DirtyFlagChangeListener()
		{
			public void dirtyFlagChanged(boolean dirty)
			{
				updateTitle();
			}
		});
	}

	public boolean hasFile()
	{
		return file != null;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
		updateTitle();
	}

	public String getTitle()
	{
		return hasFile() ? getFile().getName() : "untitled";
	}

	public String getText()
	{
		return text.getText();
	}

	public void setText(String t)
	{
		text.setText(t);
		lineNumberView.updateUI();
	}

	public boolean isModified()
	{
		return text.isModified();
	}

	public void setModified(boolean b)
	{
		text.setModified(b);
	}

	public boolean canUndo()
	{
		return text.canUndo();
	}

	public boolean canRedo()
	{
		return text.canRedo();
	}

	public void undo()
	{
		text.undo();
	}

	public void redo()
	{
		text.redo();
	}

	public void clearUndo()
	{
		text.clearUndo();
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		if (text != null)
		{
			text.setFont(font);
		}
	}

	public void setTabWidth(int numSpace)
	{
		text.setTabWidth(numSpace);
	}

	public void setCaretPosition(int position)
	{
		text.setCaretPosition(position);
	}

	public LineColumn getLineColumn()
	{
		int caretpos = text.getCaretPosition();
		Element root = text.getDocument().getDefaultRootElement();
		int index = root.getElementIndex(caretpos);
		int line = index + 1;
		int column = caretpos - root.getElement(index).getStartOffset() + 1;
		return new LineColumn(caretpos, line, column);
	}

	public JTextPane getJTextPane()
	{
		return text;
	}

	public void write(Writer out) throws IOException
	{
		text.write(out);
	}

	public void removeAllHighlights()
	{
		text.clearHighlightFlags();
	}

	public void addHighlight(int kind)
	{
		text.addHighlight(kind);
	}

	/**
	 * <p>タブ文字の可視性を設定します。</p>
	 * @param b 可視にする場合は {@code true}
	 */
	public void setShowTabs(boolean b)
	{
		text.setShowTabs(b);
	}

	/**
	 * <p>改行文字の可視性を設定します。</p>
	 * @param b 可視にする場合は {@code true}
	 */
	public void setShowEols(boolean b)
	{
		text.setShowEols(b);
	}

	public void updateHighlight()
	{
		text.updateHighlight();
	}

	public void copy()
	{
		text.copy();
	}

	public void cut()
	{
		text.cut();
	}

	public void paste()
	{
		text.paste();
	}

	public void addFileDropListener(FileDropListener listener)
	{
		fileDropHandler.addFileDropListener(listener);
	}

	public void removeFileDropListener(FileDropListener listener)
	{
		fileDropHandler.removeFileDropListener(listener);
	}

	JComponent getHeaderComponent()
	{
		return header;
	}

	private void updateTitle()
	{
		String s = getTitle();
		if (text.isModified())
		{
			s = "*" + s;
		}
		header.setTitleText(s);
		host.setToolTipTextAt(host.indexOfComponent(this), getDescription());
	}

	private String getDescription()
	{
		return hasFile() ? getFile().getAbsolutePath() : "untitled";
	}

	private void onTabCloseButtonClicked()
	{
		host.onTabCloseButtonClicked(this);
	}

	private static Icon getFileIcon(String title)
	{
		if (title.endsWith(".lmn"))
		{
			if (ICON_TAB_LMN == null)
			{
				ICON_TAB_LMN = new ImageIcon(Env.getImageOfFile("img/tab_icon_lmn.png"));
			}
			return ICON_TAB_LMN;
		}
		else if (title.endsWith(".il") || title.endsWith(".tal"))
		{
			if (ICON_TAB_IL == null)
			{
				ICON_TAB_IL = new ImageIcon(Env.getImageOfFile("img/tab_icon_il.png"));
			}
			return ICON_TAB_IL;
		}
		return getDefaultFileIcon();
	}

	private static Icon getDefaultFileIcon()
	{
		if (ICON_TAB_OTHERS == null)
		{
			ICON_TAB_OTHERS = new ImageIcon(Env.getImageOfFile("img/tab_icon.png"));
		}
		return ICON_TAB_OTHERS;
	}

	private static Icon getTabCloseCoolIcon()
	{
		if (ICON_TAB_CLOSE_COOL == null)
		{
			ICON_TAB_CLOSE_COOL = new ImageIcon(Env.getImageOfFile("img/tab_close_cold.png"));
		}
		return ICON_TAB_CLOSE_COOL;
	}

	private static Icon getTabCloseHotIcon()
	{
		if (ICON_TAB_CLOSE_HOT == null)
		{
			ICON_TAB_CLOSE_HOT = new ImageIcon(Env.getImageOfFile("img/tab_close_hot.png"));
		}
		return ICON_TAB_CLOSE_HOT;
	}

	private class HeaderComponent extends JPanel
	{
		private JLabel titleLabel;
		private JLabel iconLabel;

		public HeaderComponent()
		{
			setLayout(new BorderLayout());
			setOpaque(false);

			// fix header width
			titleLabel = new JLabel(getTitle());
			Dimension dim = titleLabel.getPreferredSize();
			dim.width = 100;
			titleLabel.setPreferredSize(dim);
			add(titleLabel, BorderLayout.CENTER);

			Icon icon = getFileIcon(getTitle());
			iconLabel = new JLabel(icon);
			iconLabel.setPreferredSize(new Dimension(16, 16));
			add(iconLabel, BorderLayout.WEST);

			JButton closeButton = new FlatButton();
			closeButton.setIcon(getTabCloseCoolIcon());
			closeButton.setRolloverIcon(getTabCloseHotIcon());
			closeButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					onTabCloseButtonClicked();
				}
			});
			closeButton.setPreferredSize(new Dimension(16, 16));
			add(closeButton, BorderLayout.EAST);
		}

		public void setTitleText(String s)
		{
			titleLabel.setText(s);
			iconLabel.setIcon(getFileIcon(s));
		}
	}
}
