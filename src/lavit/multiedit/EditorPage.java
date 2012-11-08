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

import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import lavit.multiedit.coloring.LmnTextPane;
import lavit.multiedit.coloring.lexer.TokenLabel;

@SuppressWarnings("serial")
public class EditorPage extends JScrollPane
{
	private LmnTextPane text;
	private File file;

	public EditorPage()
	{
		text = new LmnTextPane();

		setRowHeaderView(new LineNumberView(text));
		setViewportView(text);

		getVerticalScrollBar().setUnitIncrement(15);

		addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				text.requestFocus();
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
	}

	public String getText()
	{
		return text.getText();
	}

	public void setText(String t)
	{
		text.setText(t);
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
		text.removeHighlight(
				  TokenLabel.KEYWORD
				| TokenLabel.COMMENT
				| TokenLabel.STRING
				| TokenLabel.OPERATOR);
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
}
