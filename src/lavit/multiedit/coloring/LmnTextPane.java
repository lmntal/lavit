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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;

@SuppressWarnings("serial")
public class LmnTextPane extends JTextPane
{
	private LmnDocument _doc;
	
	public LmnTextPane()
	{
		setEditorKit(new LmnEditorKit());
		
		_doc = new LmnDocument();
		setDocument(_doc);
		
		setOpaque(false);
		
		addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				int[] pospair = findParenPair(getCaretPosition());
				_doc.setParenPair(pospair);
				repaint();
			}
		});
		
		InputMap im = getInputMap();
		im.put(KeyStroke.getKeyStroke("control Z"), "undo");
		im.put(KeyStroke.getKeyStroke("control Y"), "redo");
		
		ActionMap am = getActionMap();
		am.put("undo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				_doc.undo();
			}
		});
		am.put("redo", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				_doc.redo();
			}
		});
	}
	
	public void setTabWidth(int spaces)
	{
		FontMetrics fm = getFontMetrics(getFont());
		int width = fm.charWidth('m') * spaces;
		_doc.setTabWidth(width);
	}
	
	public void undo()
	{
		_doc.undo();
	}
	
	public void redo()
	{
		_doc.redo();
	}
	
	public void clearUndo()
	{
		_doc.clearUndo();
	}
	
	public boolean isModified()
	{
		return _doc.isModified();
	}
	
	public void setModified(boolean b)
	{
		_doc.setModified(b);
	}
	
	public void updateHighlight()
	{
		_doc.reparse();
		repaint();
	}
	
	public void addHighlight(int labelKind)
	{
		_doc.addHighlight(labelKind);
	}
	
	public void removeHighlight(int labelKind)
	{
		_doc.removeHighlight(labelKind);
	}
	
	// ��ü���ޤ��֤��ʤ��褦�ˤ���
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		Container container = getParent();
		TextUI ui = getUI();
		return ui.getPreferredSize(this).width < container.getWidth();
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.WHITE);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
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
}