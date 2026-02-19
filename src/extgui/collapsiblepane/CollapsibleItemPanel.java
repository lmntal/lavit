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

package extgui.collapsiblepane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
class CollapsibleItemPanel extends JPanel
{
	private static final Color COLOR_PLAIN = new Color(160, 160, 220);
	private static final Color COLOR_ROLLOVER = Color.WHITE;
	private static final Color COLOR_BORDER = new Color(220, 220, 255);
	private static final Color COLOR_GRAD1 = new Color(220, 220, 240);
	private static final Color COLOR_GRAD2 = new Color(220, 220, 240, 48);
	private static final Stroke STROKE_FOCUSRECT = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 5.0F, new float[] { 1, 1 }, 0);

	private JPanel contentContainer;

	public CollapsibleItemPanel(String caption, Component content)
	{
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

		CaptionLabel captionLabel = new CaptionLabel(caption);
		add(captionLabel, BorderLayout.NORTH);

		contentContainer = new JPanel(new BorderLayout());
		contentContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentContainer.setVisible(false);
		add(contentContainer, BorderLayout.CENTER);

		setContentComponent(content);
	}

	public void setContentComponent(Component content)
	{
		contentContainer.add(content, BorderLayout.CENTER);
	}

	public void setExpanded(boolean b, boolean scrollToVisible)
	{
		if (b)
		{
			Dimension size = contentContainer.getPreferredSize();
			size.width = getPreferredSize().width;
			contentContainer.setPreferredSize(size);
		}
		contentContainer.setVisible(b);
		revalidate();
		if (scrollToVisible)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					scrollRectToVisible(getBounds());
				}
			});
		}
	}

	public boolean isExpanded()
	{
		return contentContainer.isVisible();
	}

	public Dimension getMaximumSize()
	{
		Dimension dim = super.getMaximumSize();
		dim.height = getPreferredSize().height;
		return dim;
	}

	private void toggleExpansion()
	{
		setExpanded(!isExpanded(), true);
	}

	private class CaptionLabel extends JLabel
	{
		public CaptionLabel(String text)
		{
			super(text);
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			setForeground(COLOR_PLAIN);
			setFocusable(true);

			addMouseListener(new MouseAdapter()
			{
				public void mousePressed(MouseEvent e)
				{
					toggleExpansion();
					requestFocusInWindow();
				}

				public void mouseExited(MouseEvent e)
				{
					setForeground(COLOR_PLAIN);
				}

				public void mouseEntered(MouseEvent e)
				{
					setForeground(COLOR_ROLLOVER);
				}
			});
			addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						toggleExpansion();
					}
				}
			});
			addFocusListener(new FocusListener()
			{
				public void focusLost(FocusEvent e)
				{
					repaint();
				}

				public void focusGained(FocusEvent e)
				{
					repaint();
				}
			});
		}

		private void draw(Graphics2D g)
		{
			g.setPaint(new GradientPaint(0, 0, COLOR_GRAD1, getWidth(), 0, COLOR_GRAD2));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setPaint(null);

			FontMetrics fm = g.getFontMetrics();
			int right = fm.stringWidth(getText()) + 5;
			int r = 3;
			int cy = getHeight() / 2;
			int cx = getWidth() - (cy - r) - r;
			if (right < cx - r)
			{
				drawMark(g, cx, cy, r, getForeground());
			}
		}

		private void drawMark(Graphics2D g, int cx, int cy, int r, Color color)
		{
			g.setColor(color);
			g.drawLine(cx - r, cy, cx + r, cy);
			if (!isExpanded())
			{
				g.drawLine(cx, cy - r, cx, cy + r);
			}
		}

		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			draw(g2);
			super.paintComponent(g);

			if (hasFocus())
			{
				g2.setStroke(STROKE_FOCUSRECT);
				g2.setColor(Color.LIGHT_GRAY);
				g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
			}
		}
	}
}
