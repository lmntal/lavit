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
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
public class ColoredLinePrinter extends JTextPane
{
	private static final int DEFALT_LIMIT_LINES = 1000;

	private final Object lock = new Object();

	private int limitLines = DEFALT_LIMIT_LINES;
	private int lines;
	private Document doc;

	public ColoredLinePrinter()
	{
		doc = getDocument();
	}

	public void setMaximumNumberOfLines(int n)
	{
		if (n > 0)
		{
			limitLines = n;
		}
	}

	public void clear()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				setText("");
				setLinesZero();
			}
		});
	}

	private void setLinesZero()
	{
		synchronized (lock)
		{
			lines = 0;
		}
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
		SimpleAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setForeground(a, fg);
		StyleConstants.setBackground(a, bg);
		appendLine(line, a);
	}

	public void appendLine(final String line, final AttributeSet attr)
	{
		synchronized (lock)
		{
			if (lines < limitLines)
			{
				++lines;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						appendString(line, attr);
						appendLineFeed();
						moveCaretToEnd();
					}
				});
				if (lines == limitLines)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							SimpleAttributeSet a = new SimpleAttributeSet();
							StyleConstants.setBackground(a, Color.WHITE);
							StyleConstants.setForeground(a, Color.RED);
							appendString("Output is full.", a);
							appendLineFeed();
							moveCaretToEnd();
						}
					});
				}
			}
		}
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
		SimpleAttributeSet a = new SimpleAttributeSet();
		StyleConstants.setForeground(a, fg);
		StyleConstants.setBackground(a, bg);
		append(s, a);
	}

	public synchronized void append(final String s, final AttributeSet attr)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				appendString(s, attr);
				moveCaretToEnd();
			}
		});
	}

	public synchronized void lineFeed()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				appendLineFeed();
				moveCaretToEnd();
			}
		});
	}

	//
	// INTERNAL METHODS
	//

	/** [NOTE] This method must be called in EDT. */
	private void appendLineFeed()
	{
		appendString("\n", null);
	}

	/** [NOTE] This method must be called in EDT. */
	private void appendString(String s, AttributeSet attr)
	{
		try
		{
			doc.insertString(doc.getLength(), s, attr);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	/** [NOTE] This method must be called in EDT. */
	private void moveCaretToEnd()
	{
		Element root = doc.getDefaultRootElement();
		int offset = root.getElement(root.getElementCount() - 1).getStartOffset();
		try
		{
			setCaretPosition(offset);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
}
