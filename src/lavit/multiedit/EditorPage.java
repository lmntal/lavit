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
	private LmnTextPane _text;
	private File        _file;

	public EditorPage()
	{
		_text = new LmnTextPane();
		_text.setTransferHandler(null);

		setRowHeaderView(new LineNumberView(_text));
		setViewportView(_text);

		getVerticalScrollBar().setUnitIncrement(15);

		addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				_text.requestFocus();
			}
		});
	}

	public boolean hasFile()
	{
		return _file != null;
	}

	public File getFile()
	{
		return _file;
	}

	public void setFile(File file)
	{
		this._file = file;
	}

	public String getText()
	{
		return _text.getText();
	}

	public void setText(String t)
	{
		_text.setText(t);
	}

	public boolean isModified()
	{
		return _text.isModified();
	}

	public void setModified(boolean b)
	{
		_text.setModified(b);
	}

	public boolean canUndo()
	{
		return _text.canUndo();
	}
	
	public boolean canRedo()
	{
		return _text.canRedo();
	}
	
	public void undo()
	{
		_text.undo();
	}
	
	public void redo()
	{
		_text.redo();
	}
	
	public void clearUndo()
	{
		_text.clearUndo();
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		if (_text != null)
		{
			_text.setFont(font);
		}
	}

	public void setTabWidth(int numSpace)
	{
		_text.setTabWidth(numSpace);
	}

	public void setCaretPosition(int position)
	{
		_text.setCaretPosition(position);
	}

	public JTextPane getJTextPane()
	{
		return _text;
	}

	public void write(Writer out) throws IOException
	{
		_text.write(out);
	}

	public void removeAllHighlights()
	{
		_text.removeHighlight(
				  TokenLabel.KEYWORD
				| TokenLabel.COMMENT
				| TokenLabel.STRING
				| TokenLabel.OPERATOR);
	}

	public void addHighlight(int kind)
	{
		_text.addHighlight(kind);
	}
	
	/**
	 * <p>タブ文字の可視性を設定します。</p>
	 * @param b 可視にする場合は {@code true}
	 */
	public void setShowTabs(boolean b)
	{
		_text.setShowTabs(b);
	}
	
	/**
	 * <p>改行文字の可視性を設定します。</p>
	 * @param b 可視にする場合は {@code true}
	 */
	public void setShowEols(boolean b)
	{
		_text.setShowEols(b);
	}

	public void updateHighlight()
	{
		_text.updateHighlight();
	}
	
	public void copy()
	{
		_text.copy();
	}
	
	public void cut()
	{
		_text.cut();
	}
	
	public void paste()
	{
		_text.paste();
	}
}
