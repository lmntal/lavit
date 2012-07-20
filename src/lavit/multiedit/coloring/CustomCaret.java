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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * <p>行全体が選択されたときにコンポーネントの端までハイライトされるように拡張したキャレットです。</p>
 * @author Yuuki SHINOBU
 */
@SuppressWarnings("serial")
public class CustomCaret extends DefaultCaret
{
	private static final class SelectionPainter implements Highlighter.HighlightPainter
	{
		public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c)
		{
			Rectangle alloc = bounds.getBounds();
			try
			{
				TextUI mapper = c.getUI();
				Rectangle p0 = mapper.modelToView(c, offs0);
				Rectangle p1 = mapper.modelToView(c, offs1);

				g.setColor(c.getSelectionColor());

				if (p0.y == p1.y) // 単一の行
				{
					Rectangle r = p0.union(p1);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
				else // 複数行
				{
					// 開始行：選択範囲の始点から行末まで
					int p0ToRight = alloc.x + alloc.width - p0.x;
					g.fillRect(p0.x, p0.y, p0ToRight, p0.height);

					// 全体が選択されている行
					if ((p0.y + p0.height) != p1.y)
					{
						g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y
								- (p0.y + p0.height));
					}

					// 最終行：行頭から選択範囲の終点まで
					g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
				}
			}
			catch (BadLocationException e)
			{
			}
		}
	}

	private static SelectionPainter painter;

	protected Highlighter.HighlightPainter getSelectionPainter()
	{
		return painter != null ? painter : (painter = new SelectionPainter());
	}

	protected synchronized void damage(Rectangle r)
	{
		if (r != null)
		{
			JTextComponent t = getComponent();
			x = 0;
			y = r.y;
			width = t.getWidth();
			height = r.height;
			t.repaint();
		}
	}
}
