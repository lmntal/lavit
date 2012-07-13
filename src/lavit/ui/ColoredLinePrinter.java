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

package lavit.ui;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ColoredLinePrinter extends JTextPane
{
	private static final long serialVersionUID = 1L;

	private static final AttributeSet ATTRIBUTE_PLAIN = new SimpleAttributeSet();

	private int limitLines;
	private Document doc;

	public ColoredLinePrinter()
	{
		doc = getDocument();
	}

	public void setLimitLines(int n)
	{
		limitLines = n;
	}

	public void appendLine(String line)
	{
		appendLine(line, getForeground());
	}

	public void appendLine(String line, Color fg)
	{
		appendLine(line, fg, getBackground());
	}

	public void appendLine(String line, Color fg, Color bg)
	{
		appendText(line, fg, bg);
		lineFeed();
		moveCaret();
	}

	public void append(String s)
	{
		append(s, getForeground());
	}

	public void append(String s, Color fg)
	{
		append(s, fg, getBackground());
	}

	public void append(String s, Color fg, Color bg)
	{
		appendText(s, fg, bg);
		moveCaret();
	}

	public void lineFeed()
	{
		try
		{
			doc.insertString(doc.getLength(), "\n", ATTRIBUTE_PLAIN);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}

		Element elem = doc.getDefaultRootElement();

		if (limitLines > 0)
		{
			int lines = elem.getElementCount();
			if (lines > limitLines)
			{
				try
				{
					doc.remove(0, getText().indexOf('\n'));
				}
				catch (BadLocationException e)
				{
					e.printStackTrace();
				}
			}
		}
		moveCaret();
	}

	private void appendText(String s, Color fg, Color bg)
	{
		SimpleAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setForeground(a, fg);
		StyleConstants.setBackground(a, bg);
		try
		{
			doc.insertString(doc.getLength(), s, a);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void moveCaret()
	{
		Element elem = doc.getDefaultRootElement();
		int offset = elem.getElement(elem.getElementCount() - 1).getStartOffset();
		setCaretPosition(offset - 1);
	}
}
