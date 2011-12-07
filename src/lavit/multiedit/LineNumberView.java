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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

/**
 * <p>行番号を表示するコンポーネント。</p>
 * <p>{@code JTextComponent} を監視し、行に関する情報を表示します。</p>
 * @since  December 2, 2011
 * @author Yuuki SHINOBU
 */
@SuppressWarnings("serial")
public class LineNumberView extends JComponent
{
	private static final Color DEFAULT_BACKGROUND            = new Color(220, 220, 240);
	private static final Color DEFAULT_FOREGROUND            = new Color(  0, 128, 192);
	private static final Color DEFAULT_HIGHLIGHT_FOREGROUND  = new Color(255, 255, 255);
	private static final Color DEFAULT_HIGHLIGHT_BACKGROUND  = new Color(100, 200, 255);
	private static final Color DEFAULT_LINE_COLOR            = new Color(  0, 128, 192);
	private static final Color DEFAULT_SHADOW_COLOR          = new Color(220, 220, 220);
	
	private JTextComponent _text;
	private int            _top;
	private int            _lineHeight;
	private int            _digitWidth;
	
	public LineNumberView(JTextComponent text)
	{
		_text = text;
		
		_text.getDocument().addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)
			{
				updateRange();
				repaint();
			}
			
			public void insertUpdate(DocumentEvent e)
			{
				updateRange();
				repaint();
			}
			
			public void changedUpdate(DocumentEvent e)
			{
			}
		});
		
		_text.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				updateSize();
				repaint();
			}
		});
		
		_text.addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				repaint();
			}
		});
		
		_text.addPropertyChangeListener("font", new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				updateRange();
				repaint();
			}
		});
		
		updateRange();
	}
	
	private void updateRange()
	{
		setFont(_text.getFont());
		
		_top = _text.getMargin().top;
		
		FontMetrics fm = getFontMetrics(_text.getFont());
		_lineHeight = fm.getHeight();
		
		_digitWidth = 0;
		for (int i = 0; i < 10; i++)
		{
			_digitWidth = Math.max(fm.charWidth((char)('0' + i)), _digitWidth);
		}
		
		updateSize();
	}
	
	private void updateSize()
	{
		Dimension size = new Dimension(5 * _digitWidth + 5, _lineHeight + _text.getHeight());
		setPreferredSize(size);
		setSize(size);
	}
	
	public void paintComponent(Graphics g)
	{
		Rectangle bounds = g.getClipBounds();
		Element rootElement = _text.getDocument().getDefaultRootElement();
		int caret = _text.getCaretPosition();
		int lines = rootElement.getElementCount();
		int curLine = rootElement.getElementIndex(caret);
		
		// background
		g.setColor(DEFAULT_BACKGROUND);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		
		// line
		g.setColor(DEFAULT_LINE_COLOR);
		g.drawLine(bounds.x + bounds.width - 2, bounds.y,
				bounds.x + bounds.width - 2, bounds.y + bounds.height);
		
		// shadow line
		g.setColor(DEFAULT_SHADOW_COLOR);
		g.drawLine(bounds.x + bounds.width - 1, bounds.y,
				bounds.x + bounds.width - 1, bounds.y + bounds.height);
		
		FontMetrics fm = g.getFontMetrics(_text.getFont());
		
		int snap = Math.max(bounds.y - Math.max(bounds.y - _top, 0) % _lineHeight, _top);
		int baseY = snap + _lineHeight - fm.getDescent();
		int endY = snap + bounds.height + _lineHeight;
		
		Graphics2D g2 = (Graphics2D)g;
		RenderingHints hints = g2.getRenderingHints();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 250);
		
		g.setFont(_text.getFont());
		
		// highlight current line
		g.setColor(DEFAULT_HIGHLIGHT_BACKGROUND);
		g.fillRect(bounds.x, _top + curLine * _lineHeight, bounds.width - 2, _lineHeight);
		g.setColor(DEFAULT_HIGHLIGHT_FOREGROUND);
		{
			String s = Integer.toString(curLine + 1);
			int w = fm.stringWidth(s);
			g.drawString(s, getWidth() - w - 5, _top + (curLine + 1) * _lineHeight - fm.getDescent());
		}
		
		// line numbers
		g.setColor(DEFAULT_FOREGROUND);
		
		int n = Math.max(bounds.y - _top, 0) / _lineHeight;
		for (int y = baseY; y < endY && n < lines; y += _lineHeight)
		{
			if (n != curLine)
			{
				String s = Integer.toString(n + 1);
				int w = fm.stringWidth(s);
				g.drawString(s, getWidth() - w - 5, y);
			}
			n++;
		}
		
		g2.setRenderingHints(hints);
	}
}
