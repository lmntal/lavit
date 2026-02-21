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
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Position.Bias;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.WrappedPlainView;

import lavit.Env;
import lavit.multiedit.coloring.lexer.ColorLabel;
import lavit.multiedit.coloring.lexer.TokenLabel;
import lavit.util.TextAntialias;

public class LmnContext implements ViewFactory
{
	private boolean lineWrap = false;

	public boolean isLineWrap()
	{
		return lineWrap;
	}

	public void setLineWrap(boolean wrap)
	{
		lineWrap = wrap;
	}

	public View create(Element e)
	{
		return lineWrap ? new LmnWrappedView(e) : new LmnView(e);
	}
}

class LmnView extends PlainView
{
	private Map<Integer, Color> colors = new HashMap<Integer, Color>();
	private int marginLeft;
	private int drawingTranslateX = 0;
	private Color ruleNameBackground = new Color(255, 200, 200);

	public LmnView(Element e)
	{
		super(e);
		colors.put(TokenLabel.COMMENT , new Color(  0, 128,   0));
		colors.put(TokenLabel.STRING  , new Color(128,   0,   0));
		colors.put(TokenLabel.OPERATOR, new Color(255,   0,   0));
		colors.put(TokenLabel.KEYWORD , new Color(  0,   0, 255));
	}

	public void paint(Graphics g, Shape alloc)
	{
		marginLeft = alloc.getBounds().x;

		drawParenPair(g, alloc);

		if (getLMNDocument().getShowTabs())
		{
			drawTabs(g, alloc);
		}

		Graphics2D g2 = (Graphics2D)g;
		RenderingHints hints = g2.getRenderingHints();

		TextAntialias textaa = TextAntialias.of(Env.get("editor.antialias", ""));
		if (textaa == null)
		{
			textaa = TextAntialias.DEFAULT;
		}
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textaa.value);

		g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 250);

		super.paint(g, alloc);

		g2.setRenderingHints(hints);
	}

	public float nextTabStop(float x, int tabOffset)
	{
		Graphics g = getGraphics();
		if (g != null)
		{
			FontMetrics fm = getGraphics().getFontMetrics();
			int width = fm.charWidth(' ') * getLMNDocument().getTabWidth();
			if (drawingTranslateX != 0)
			{
				// x is in translated coordinates (0 == left margin).
				// floor(x/width)+1 correctly advances to the NEXT multiple of
				// width from 0, even when x lands exactly on a tab stop.
				return (int)(Math.floor(x / width) + 1) * width;
			}
			// Non-translated path: preserve the original formula so that
			// existing tab-stop alignment (relative to marginLeft) is unchanged.
			return marginLeft + (int)Math.ceil(x / width) * width;
		}
		return 0;
	}

	protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f)
	{
		super.updateDamage(changes, a, f);
		Component comp = getContainer();
		comp.repaint();
	}

	protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
	{
		y += 1;

		// Translate the graphics origin to x=0 so that internal drawing
		// coordinates stay small and never overflow Short.MAX_VALUE on any
		// platform/rendering pipeline (fixes display corruption on long lines).
		final int translateX = x;
		drawingTranslateX = translateX;
		((Graphics2D) g).translate(translateX, 0);
		x = 0;

		try
		{
		LmnDocument doc = getLMNDocument();
		Segment lb = getLineBuffer();
		int pos = p0;

		// draw all color parts
		for (ColorLabel label : doc.getLabels())
		{
			if (label.getEnd() < p0) continue;

			if (p1 <= Math.max(pos, label.getStart())) break;

			// before
			if (pos < label.getStart())
			{
				g.setColor(getContainer().getForeground());
				doc.getText(pos, label.getStart() - pos, lb);
				x = Utilities.drawTabbedText(lb, x, y, g, this, 0);
				pos = label.getStart();
			}

			// color part
			int len = label.getLength();
			if (label.getStart() < pos)
			{
				len -= pos - label.getStart();
			}
			if (pos + len >= p1)
			{
				len = p1 - pos;
			}
			doc.getText(pos, len, lb);
			if (label.getLabel() == TokenLabel.RULENAME)
			{
				FontMetrics fm = g.getFontMetrics();
				int w = Utilities.getTabbedTextWidth(lb, fm, x, this, 0);
				g.setColor(ruleNameBackground);
				g.fillRect(x, y - fm.getAscent(), w, fm.getHeight());
				g.setColor(getContainer().getForeground());
			}
			else
			{
				g.setColor(colors.get(label.getLabel()));
			}
			x = Utilities.drawTabbedText(lb, x, y, g, this, 0);
			pos += len;
		}

		// remaining
		if (pos < p1)
		{
			g.setColor(getContainer().getForeground());
			doc.getText(pos, p1 - pos, lb);
			x = Utilities.drawTabbedText(lb, x, y, g, this, 0);
		}

		if (getLMNDocument().getShowEols() && p0 < p1)
		{
			String eol = doc.getText(p1 - 1, 1);
			if (eol.charAt(0) == '\n')
			{
				drawCRLF(g, x, y);
			}
		}
		return x + translateX;
		}
		finally
		{
			((Graphics2D) g).translate(-translateX, 0);
			drawingTranslateX = 0;
		}
	}

	protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
	{
		final int translateX = x;
		drawingTranslateX = translateX;
		((Graphics2D) g).translate(translateX, 0);
		x = 0;
		try
		{
			g.setColor(getContainer().getForeground());
			Segment seg = getLineBuffer();
			getDocument().getText(p0, p1 - p0, seg);
			x = Utilities.drawTabbedText(seg, x, y + 1, g, this, 0);
			return x + translateX;
		}
		finally
		{
			((Graphics2D) g).translate(-translateX, 0);
			drawingTranslateX = 0;
		}
	}
	
	private void drawParenPair(Graphics g, Shape alloc)
	{
		LmnDocument doc = (LmnDocument)getDocument();
		TreeSet<ColorLabel> parens = doc.getParenPairSet();
		FontMetrics fm = g.getFontMetrics();
		int w = fm.charWidth('(');
		int h = fm.getHeight();
		try
		{
			g.setColor(new Color(255, 200, 100));
			for (ColorLabel c : parens)
			{
				Shape s = modelToView(c.getStart(), alloc, Bias.Backward);
				Rectangle rc = s.getBounds();
				g.fillRect(rc.x, rc.y, w, h);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void drawTabs(Graphics g, Shape alloc)
	{
		int h = g.getFontMetrics().getHeight() - 8;

		if (h <= 0) return;

		LmnDocument doc = (LmnDocument)getDocument();
		g.setColor(Color.LIGHT_GRAY);
		try
		{
			for (int p : doc.getTabs())
			{
				Shape s = modelToView(p, alloc, Bias.Backward);
				Rectangle r = s.getBounds();
				drawTabCharacter(g, r, h);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void drawTabCharacter(Graphics g, Rectangle r, int height)
	{
		int sy = r.y + (r.height - height) / 2;
		int h = height / 2;
		g.drawLine(r.x + 3, sy        , r.x + 3 + h, sy + h);
		g.drawLine(r.x + 3, sy + 2 * h, r.x + 3 + h, sy + h);
	}

	private void drawCRLF(Graphics g, int x, int y)
	{
		FontMetrics fm = g.getFontMetrics();
		int w = fm.charWidth('x');
		int h = fm.getAscent();

		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(x + (w - 1), y - h + 1, x + (w - 1), y - 1    );
		g.drawLine(x + 1      , y - 1    , x + (w - 1), y - 1    );
		g.drawLine(x + 1      , y - 1    , x + 3      , y + 2 - 1);
		g.drawLine(x + 1      , y - 1    , x + 3      , y - 2 - 1);
	}

	/*
	private void drawLF(Graphics g, int x, int y)
	{
		FontMetrics fm = g.getFontMetrics();
		int w = fm.charWidth('x');
		int h = fm.getAscent();
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(x + w / 2, y - h + 1, x + w / 2    , y - 1    );
		g.drawLine(x + w / 2, y - 1    , x + w / 2 + 2, y - 1 - 2);
		g.drawLine(x + w / 2, y - 1    , x + w / 2 - 2, y - 1 - 2);
	}

	private void drawCR(Graphics g, int x, int y)
	{
		FontMetrics fm = g.getFontMetrics();
		int w = fm.charWidth('x');
		int h = fm.getAscent();
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(x + 1, y - h / 2, x + w - 1, y - h / 2    );
		g.drawLine(x + 1, y - h / 2, x + 1 + 2, y - h / 2 - 2);
		g.drawLine(x + 1, y - h / 2, x + 1 + 2, y - h / 2 + 2);
	}
	*/

	private LmnDocument getLMNDocument()
	{
		return (LmnDocument)getDocument();
	}
}

/**
 * A {@link WrappedPlainView}-based view that applies the same syntax
 * colouring as {@link LmnView} while also visually wrapping long lines.
 * The translate trick used in {@link LmnView} is applied here too so that
 * rendering of very long un-wrappable tokens is also safe.
 */
class LmnWrappedView extends WrappedPlainView
{
	private Map<Integer, Color> colors = new HashMap<Integer, Color>();
	private Color ruleNameBackground = new Color(255, 200, 200);
	private int drawingTranslateX = 0;
	private Segment wrappedLineBuffer;

	LmnWrappedView(Element e)
	{
		// wordWrap=false: break at any character boundary so that no single
		// visual row can exceed the viewport width.  This guarantees that the
		// coordinate overflow that affects plain (non-wrapped) rendering cannot
		// occur.  Word-boundary wrapping (wordWrap=true) would leave very long
		// tokens unbroken and therefore does not fully protect against overflow.
		super(e, false);
		colors.put(TokenLabel.COMMENT , new Color(  0, 128,   0));
		colors.put(TokenLabel.STRING  , new Color(128,   0,   0));
		colors.put(TokenLabel.OPERATOR, new Color(255,   0,   0));
		colors.put(TokenLabel.KEYWORD , new Color(  0,   0, 255));
	}

	private Segment getWrappedLineBuffer()
	{
		if (wrappedLineBuffer == null)
		{
			wrappedLineBuffer = new Segment();
		}
		return wrappedLineBuffer;
	}

	public void paint(Graphics g, Shape alloc)
	{
		drawParenPair(g, alloc);

		if (getLMNDocument().getShowTabs())
		{
			drawTabs(g, alloc);
		}

		Graphics2D g2 = (Graphics2D)g;
		RenderingHints hints = g2.getRenderingHints();

		TextAntialias textaa = TextAntialias.of(Env.get("editor.antialias", ""));
		if (textaa == null)
		{
			textaa = TextAntialias.DEFAULT;
		}
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textaa.value);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 250);

		super.paint(g, alloc);

		g2.setRenderingHints(hints);
	}

	/** Tab stops at regular multiples of tab width from x = 0. */
	public float nextTabStop(float x, int tabOffset)
	{
		Component c = getContainer();
		if (c != null)
		{
			Graphics g = c.getGraphics();
			if (g != null)
			{
				FontMetrics fm = g.getFontMetrics();
				int width = fm.charWidth(' ') * getLMNDocument().getTabWidth();
				if (width > 0)
				{
					return (int)(Math.floor(x / width) + 1) * width;
				}
			}
		}
		return x;
	}

	protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
	{
		y += 1;

		final int translateX = x;
		drawingTranslateX = translateX;
		((Graphics2D) g).translate(translateX, 0);
		x = 0;

		try
		{
		LmnDocument doc = getLMNDocument();
		Segment lb = getWrappedLineBuffer();
		int pos = p0;

		for (ColorLabel label : doc.getLabels())
		{
			if (label.getEnd() < p0) continue;
			if (p1 <= Math.max(pos, label.getStart())) break;

			if (pos < label.getStart())
			{
				g.setColor(getContainer().getForeground());
				doc.getText(pos, label.getStart() - pos, lb);
				x = Utilities.drawTabbedText(lb, x, y, g, this, 0);
				pos = label.getStart();
			}

			int len = label.getLength();
			if (label.getStart() < pos)
			{
				len -= pos - label.getStart();
			}
			if (pos + len >= p1)
			{
				len = p1 - pos;
			}
			doc.getText(pos, len, lb);
			if (label.getLabel() == TokenLabel.RULENAME)
			{
				FontMetrics fm = g.getFontMetrics();
				int w = Utilities.getTabbedTextWidth(lb, fm, x, this, 0);
				g.setColor(ruleNameBackground);
				g.fillRect(x, y - fm.getAscent(), w, fm.getHeight());
				g.setColor(getContainer().getForeground());
			}
			else
			{
				g.setColor(colors.get(label.getLabel()));
			}
			x = Utilities.drawTabbedText(lb, x, y, g, this, 0);
			pos += len;
		}

		if (pos < p1)
		{
			g.setColor(getContainer().getForeground());
			doc.getText(pos, p1 - pos, lb);
			x = Utilities.drawTabbedText(lb, x, y, g, this, 0);
		}

		if (getLMNDocument().getShowEols() && p0 < p1)
		{
			String eol = doc.getText(p1 - 1, 1);
			if (eol.charAt(0) == '\n')
			{
				drawCRLF(g, x, y);
			}
		}
		return x + translateX;
		}
		finally
		{
			((Graphics2D) g).translate(-translateX, 0);
			drawingTranslateX = 0;
		}
	}

	protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
	{
		final int translateX = x;
		drawingTranslateX = translateX;
		((Graphics2D) g).translate(translateX, 0);
		x = 0;
		try
		{
			g.setColor(getContainer().getForeground());
			Segment seg = getWrappedLineBuffer();
			getDocument().getText(p0, p1 - p0, seg);
			x = Utilities.drawTabbedText(seg, x, y + 1, g, this, 0);
			return x + translateX;
		}
		finally
		{
			((Graphics2D) g).translate(-translateX, 0);
			drawingTranslateX = 0;
		}
	}

	private void drawParenPair(Graphics g, Shape alloc)
	{
		LmnDocument doc = getLMNDocument();
		TreeSet<ColorLabel> parens = doc.getParenPairSet();
		FontMetrics fm = g.getFontMetrics();
		int w = fm.charWidth('(');
		int h = fm.getHeight();
		try
		{
			g.setColor(new Color(255, 200, 100));
			for (ColorLabel c : parens)
			{
				Shape s = modelToView(c.getStart(), alloc, Bias.Backward);
				Rectangle rc = s.getBounds();
				g.fillRect(rc.x, rc.y, w, h);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void drawTabs(Graphics g, Shape alloc)
	{
		int h = g.getFontMetrics().getHeight() - 8;
		if (h <= 0) return;

		LmnDocument doc = getLMNDocument();
		g.setColor(Color.LIGHT_GRAY);
		try
		{
			for (int p : doc.getTabs())
			{
				Shape s = modelToView(p, alloc, Bias.Backward);
				Rectangle r = s.getBounds();
				drawTabCharacter(g, r, h);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void drawTabCharacter(Graphics g, Rectangle r, int height)
	{
		int sy = r.y + (r.height - height) / 2;
		int h = height / 2;
		g.drawLine(r.x + 3, sy        , r.x + 3 + h, sy + h);
		g.drawLine(r.x + 3, sy + 2 * h, r.x + 3 + h, sy + h);
	}

	private void drawCRLF(Graphics g, int x, int y)
	{
		FontMetrics fm = g.getFontMetrics();
		int w = fm.charWidth('x');
		int h = fm.getAscent();

		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(x + (w - 1), y - h + 1, x + (w - 1), y - 1    );
		g.drawLine(x + 1      , y - 1    , x + (w - 1), y - 1    );
		g.drawLine(x + 1      , y - 1    , x + 3      , y + 2 - 1);
		g.drawLine(x + 1      , y - 1    , x + 3      , y - 2 - 1);
	}

	private LmnDocument getLMNDocument()
	{
		return (LmnDocument)getDocument();
	}
}
