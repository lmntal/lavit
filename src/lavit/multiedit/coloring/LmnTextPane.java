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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;

import lavit.multiedit.coloring.event.DirtyFlagChangeListener;

@SuppressWarnings("serial")
public class LmnTextPane extends JTextPane
{
	private final Document blankDocument = new DefaultStyledDocument();
	private boolean autoIndentEnabled = true;
	private boolean autoAlignEnabled = true;

	public LmnTextPane()
	{
		setEditorKit(new LmnEditorKit());

		addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				String text = getLMNDocument().getRowText();
				int caretPos = getCaretPosition();
				int pos = findMatchingParenIndex(text, caretPos);
				if (pos == -1)
				{
					--caretPos;
					pos = findMatchingParenIndex(text, caretPos);
				}
				if (pos != -1)
				{
					getLMNDocument().setParenPair(new int[] { caretPos, pos });
				}
				else
				{
					getLMNDocument().setParenPair(null);
				}
				repaint();
			}
		});

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_SLASH &&
					(e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				{
					if (comment())
					{
						e.consume();
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_TAB)
				{
					if (indent(e.getModifiersEx())){
						e.consume();
					}
				}
				else if (autoIndentEnabled && e.getKeyCode() == KeyEvent.VK_ENTER)
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
				getLMNDocument().undo();
			}
		});
		am.put("redo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				getLMNDocument().redo();
			}
		});
	}

	public void updateUI()
	{
		super.updateUI();
		setUI(new LineHighlightTextPaneUI());
		setBackground(Color.WHITE);
	}

	public void setText(String text)
	{
		LmnDocument doc = getLMNDocument();
		setDocument(blankDocument);
		text = setEOLStringPropertyAndReplace(doc, text);
		doc.initializeText(text);
		setDocument(doc);
		updateHighlight();
	}

	private String setEOLStringPropertyAndReplace(Document doc, String text)
	{
		boolean isCR = false;
		boolean isLF = false;
		boolean isCRLF = false;
		boolean prevCR = false;
		for (int i = 0, limit = Math.min(text.length(), 4096); i < limit; i++)
		{
			switch (text.charAt(i))
			{
			case '\r':
				if (prevCR)
				{
					isCR = true;
				}
				else
				{
					prevCR = true;
				}
				break;
			case '\n':
				if (prevCR)
				{
					isCRLF = true;
				}
				else
				{
					isLF = true;
				}
				break;
			}
			if (isCR || isLF || isCRLF)
			{
				break;
			}
		}
		if (isCR)
		{
			text = text.replace('\r', '\n');
			doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\r");
		}
		else if (isCRLF)
		{
			text = text.replace("\r\n", "\n");
			doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\r\n");
		}
		else if (isLF)
		{
			doc.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
		}
		return text;
	}

	public LmnDocument getLMNDocument()
	{
		Document doc = getDocument();

		if(doc instanceof LmnDocument){
			return (LmnDocument) doc;
		}else {
			LmnDocument lmndoc = new LmnDocument();
			try {
				lmndoc.initializeText(doc.getText(0, doc.getLength()));
				// 0からdocのlengthまでを取ってるだけでBadLocationExceptionは起きないはずなので握り潰す
			} catch (BadLocationException e){
				System.err.println(e);
			}

			// なぜかハイライトがされなくなったのでLmnDocument.updateHighlight()をコピペ
			lmndoc.setDirty(true);
			lmndoc.reparse();
			return lmndoc;
		}
	}

	public void setTabWidth(int spaces)
	{
		getLMNDocument().setTabWidth(spaces);
	}

	public int getTabWidth()
	{
		return getLMNDocument().getTabWidth();
	}

	public boolean canUndo()
	{
		return getLMNDocument().canUndo();
	}

	public boolean canRedo()
	{
		return getLMNDocument().canRedo();
	}

	public void undo()
	{
		getLMNDocument().undo();
	}

	public void redo()
	{
		getLMNDocument().redo();
	}

	public void clearUndo()
	{
		getLMNDocument().clearUndo();
	}

	public boolean isModified()
	{
		return getLMNDocument().isDirty();
	}

	public void setModified(boolean b)
	{
		getLMNDocument().setDirty(b);
	}

	public void updateHighlight()
	{
		getLMNDocument().reparse();
		repaint();
	}

	public void clearHighlightFlags()
	{
		getLMNDocument().setHighlightFlags(0);
	}

	public void addHighlight(int labelKind)
	{
		getLMNDocument().addHighlight(labelKind);
	}

	public void removeHighlight(int labelKind)
	{
		getLMNDocument().removeHighlight(labelKind);
	}

	public void setShowTabs(boolean b)
	{
		getLMNDocument().setShowTabs(b);
	}

	public void setShowEols(boolean b)
	{
		getLMNDocument().setShowEols(b);
	}

	public void addDirtyFlagChangeListener(DirtyFlagChangeListener l)
	{
		getLMNDocument().addDirtyFlagChangeListener(l);
	}

	// 右端で折り返さないようにする
	public boolean getScrollableTracksViewportWidth()
	{
		return getUI().getPreferredSize(this).width < getParent().getWidth();
	}

	private static int findMatchingParenIndex(String s, int p0)
	{
		if (0 <= p0 && p0 < s.length())
		{
			char c1 = s.charAt(p0);
			char c2 = getPairParenChar(c1);
			if (c2 != 0)
			{
				int dir = isOpenParen(c1) ? 1 : -1;
				int level = 0;
				for (int p1 = p0 + dir; 0 <= p1 && p1 < s.length(); p1 += dir)
				{
					char c = s.charAt(p1);
					if (c == c2)
					{
						if (level == 0)
						{
							return p1;
						}
						else
						{
							--level;
						}
					}
					else if (c == c1)
					{
						++level;
					}
				}
			}
		}
		return -1;
	}

	private boolean autoIndent()
	{
		LmnDocument doc = getLMNDocument();
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
		LmnDocument doc = getLMNDocument();
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

	/**
	 * If selection is empty, comment out the current line
	 * otherwise, comment out the selected lines
	 * @return true if the operation was successful
	 */
	private boolean comment()
	{
		LmnDocument doc = getLMNDocument();
		int pos = getCaretPosition();
		int start = getSelectionStart();
		int end = getSelectionEnd();
		int new_start = start;
		int new_end = end;

		if (start == end)
		{
			// no selection, comment out the current line
			int index = doc.getDefaultRootElement().getElementIndex(pos);
			Element elem = doc.getDefaultRootElement().getElement(index);
			start = elem.getStartOffset();
			end = elem.getEndOffset() - 1;
		}
		else
		{
			// fix up the selection to be line-based
			int startLine = doc.getDefaultRootElement().getElementIndex(start);
			int endLine = doc.getDefaultRootElement().getElementIndex(end);
			Element startElem = doc.getDefaultRootElement().getElement(startLine);
			Element endElem = doc.getDefaultRootElement().getElement(endLine);
			start = startElem.getStartOffset();
			end = endElem.getEndOffset() - 1;
		}

		try
		{
			String text = getText(start, end - start);
			boolean lastNewline = text.endsWith("\n");
			String[] lines = text.split("\n");
			String comment = "//";
			boolean commented = lines[0].startsWith(comment);
			StringBuilder sb = new StringBuilder();
			for (String line : lines)
			{
				if (commented)
				{
					if (line.startsWith(comment))
					{
						sb.append(line.substring(comment.length()));
					}
					else
					{
						sb.append(line);
					}
				}
				else
				{
					sb.append(comment).append(line);
				}
				sb.append("\n");
			}

			if (!lastNewline)
			{
				sb.deleteCharAt(sb.length() - 1);
			}

			doc.replace(start, end - start, sb.toString(), null);

			new_start += 2 * (commented ? -1 : 1);
			new_end += 2 * (lines.length) * (commented ? -1 : 1);

			setSelectionStart(new_start);
			setSelectionEnd(new_end);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean indent(int keyModifiers)
	{
		LmnDocument doc = getLMNDocument();
		int start = getSelectionStart();
		int end = getSelectionEnd();
		int new_start = start;
		int new_end = end;
		boolean shift = (keyModifiers & KeyEvent.SHIFT_DOWN_MASK) != 0;

		// no selection, do nothing
		if (start == end) return false;
		
		// fix up the selection to be line-based
		int startLine = doc.getDefaultRootElement().getElementIndex(start);
		int endLine = doc.getDefaultRootElement().getElementIndex(end);
		Element startElem = doc.getDefaultRootElement().getElement(startLine);
		Element endElem = doc.getDefaultRootElement().getElement(endLine);
		start = startElem.getStartOffset();
		end = endElem.getEndOffset() - 1;

		try
		{
			String text = getText(start, end - start);
			String[] lines = text.split("\n");
			StringBuilder sb = new StringBuilder();
			for (String line : lines)
			{
				if (shift)
				{
					if (line.startsWith("\t"))
					{
						sb.append(line.substring(1));
					}
					else if (line.startsWith("  "))
					{
						sb.append(line.substring(1));
					}
					else
					{
						sb.append(line);
					}
				}
				else
				{
					sb.append("\t").append(line);
				}
				sb.append("\n");
			}

			doc.replace(start, end - start, sb.toString(), null);

			new_start += shift ? -1 : 1;
			new_end += lines.length * (shift ? -1 : 1);

			setSelectionStart(new_start);
			setSelectionEnd(new_end);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
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

	private static char getPairParenChar(char c)
	{
		switch (c)
		{
			case '(': return ')';
			case '{': return '}';
			case '[': return ']';
			case ')': return '(';
			case '}': return '{';
			case ']': return '[';
		}
		return 0;
	}

	private static boolean isOpenParen(char c)
	{
		return c == '(' || c == '{' || c == '[';
	}
}
