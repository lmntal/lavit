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

package lavit.multiedit.coloring;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

@SuppressWarnings("serial")
public class LmnTextPane extends JTextPane
{
	private LmnDocument doc;
	private boolean autoIndentEnabled = true;
	private boolean autoAlignEnabled = true;

	public LmnTextPane()
	{
		setEditorKit(new LmnEditorKit());

		doc = new LmnDocument();
		setDocument(doc);

		setOpaque(false);

		addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				int[] pospair = findParenPair(getCaretPosition());
				doc.setParenPair(pospair);
				repaint();
			}
		});

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (autoIndentEnabled && e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if (autoIndent())
					{
						e.consume();
					}
				}
				else if (autoAlignEnabled && e.getKeyCode() == KeyEvent.VK_TAB)
				{
					if (autoAlign())
					{
						e.consume();
					}
				}
			}
		});

		CustomCaret caret = new CustomCaret();
		caret.setBlinkRate(getCaret().getBlinkRate());
		setCaret(caret);

		InputMap im = getInputMap();
		im.put(KeyStroke.getKeyStroke("control Y"), "redo");
		im.put(KeyStroke.getKeyStroke("control Z"), "undo");

		ActionMap am = getActionMap();
		am.put("undo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				doc.undo();
			}
		});
		am.put("redo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				doc.redo();
			}
		});
	}

	public void setTabWidth(int spaces)
	{
		doc.setTabWidth(spaces);
	}

	public int getTabWidth()
	{
		return doc.getTabWidth();
	}

	public boolean canUndo()
	{
		return doc.canUndo();
	}

	public boolean canRedo()
	{
		return doc.canRedo();
	}

	public void undo()
	{
		doc.undo();
	}

	public void redo()
	{
		doc.redo();
	}

	public void clearUndo()
	{
		doc.clearUndo();
	}

	public boolean isModified()
	{
		return doc.isModified();
	}

	public void setModified(boolean b)
	{
		doc.setModified(b);
	}

	public void updateHighlight()
	{
		doc.reparse();
		repaint();
	}

	public void addHighlight(int labelKind)
	{
		doc.addHighlight(labelKind);
	}

	public void removeHighlight(int labelKind)
	{
		doc.removeHighlight(labelKind);
	}

	public void setShowTabs(boolean b)
	{
		doc.setShowTabs(b);
	}

	public void setShowEols(boolean b)
	{
		doc.setShowEols(b);
	}

	// 右端で折り返さないようにする
	public boolean getScrollableTracksViewportWidth()
	{
		Container container = getParent();
		TextUI ui = getUI();
		return ui.getPreferredSize(this).width < container.getWidth();
	}

	protected void paintComponent(Graphics g)
	{
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.WHITE);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

		// paint line highlight
		try
		{
			Rectangle rc0 = modelToView(0);
			Rectangle rc = modelToView(getCaretPosition());

			if (rc0 == null || rc == null) return;

			int x = rc0.x;
			int y = rc.y;
			int w = getWidth();
			int h = rc.height;

			g.setColor(new Color(240, 240, 255));
			g.fillRect(x, y, w, h);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		super.paintComponent(g);
	}

	private int[] findParenPair(int caretPos)
	{
		String text = getText().replace("\r\n", "\n");
		int p0 = caretPos - 1, chpos;

		if (p0 < 0 || text.length() <= p0) return null;

		char c1 = text.charAt(p0);
		if ((chpos = "({[)}]".indexOf(text.charAt(p0))) != -1)
		{
			char c2 = ")}]({[".charAt(chpos);
			int dir = chpos < 3 ? 1 : -1;

			int p1 = p0 + dir;
			int level = 0;

			while (0 <= p1 && p1 < text.length())
			{
				char c = text.charAt(p1);
				if (c == c2)
				{
					if (level == 0)
					{
						return new int[] { p1, p0 };
					}
					else
					{
						level--;
					}
				}
				else if (c == c1)
				{
					level++;
				}
				p1 += dir;
			}
		}
		return null;
	}

	private boolean autoIndent()
	{
		final int pos = getCaretPosition();
		int lineIndex = doc.getDefaultRootElement().getElementIndex(pos);
		Element elemLine = doc.getDefaultRootElement().getElement(lineIndex);
		int start = elemLine.getStartOffset(), end = elemLine.getEndOffset();
		try
		{
			int localPos = pos - start;
			String line = getText(start, end - start);

			int spaces = getLeadingWhitespaceCount(line.substring(localPos));
			doc.replace(pos, spaces, "", null);

			String indent = getLeadingWhitespaces(line);
			doc.insertString(pos, "\n" + indent, null);
			return true;
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private boolean autoAlign()
	{
		int pos = getCaretPosition();
		int index = doc.getDefaultRootElement().getElementIndex(pos);
		if (index == 0)
		{
			return false;
		}
		Element elem = doc.getDefaultRootElement().getElement(index);
		int start = elem.getStartOffset(), end = elem.getEndOffset();
		try
		{
			String line = getText(start, end - start);
			String head = getText(start, pos - start);
			if (isWhitespaces(head))
			{
				Element prev = doc.getDefaultRootElement().getElement(index - 1);
				String pline = getText(prev.getStartOffset(), prev.getEndOffset() - prev.getStartOffset());
				String plws = getLeadingWhitespaces(pline);
				if (getColumnLength(plws) <= getColumnLength(head))
				{
					return false;
				}
				String clws = getLeadingWhitespaces(line);
				doc.replace(start, clws.length(), plws, null);
				return true;
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private int getColumnLength(String s)
	{
		int n = 0;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c == '\t')
			{
				n += getTabWidth();
			}
			else if (c <= 0x7F)
			{
				n++;
			}
			else
			{
				n += 2;
			}
		}
		return n;
	}

	private static boolean isWhitespaces(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			if (!Character.isWhitespace(s.charAt(i))) return false;
		}
		return true;
	}

	private static String getLeadingWhitespaces(String s)
	{
		String t = "";
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c != ' ' && c != '\t') break;
			t += c;
		}
		return t;
	}

	private static int getLeadingWhitespaceCount(String s)
	{
		int i = 0;
		for (; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c != ' ' && c != '\t') break;
		}
		return i;
	}
}
