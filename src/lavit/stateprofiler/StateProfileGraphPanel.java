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
package lavit.stateprofiler;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class StateProfileGraphPanel extends JPanel
{
	private final Color cState = new Color(0, 0, 255);
	private final Color unState = new Color(200, 200, 200);

	private StateProfilePanel profile;
	private boolean active;
	private Insets margin = new Insets(10, 50, 30, 10);
	private int maxX = 5;
	private int maxY = 5;

	public StateProfileGraphPanel(StateProfilePanel p)
	{
		profile = p;
	}

	public void setActive(boolean b)
	{
		active = b;
	}

	public void start()
	{
		setActive(true);
		maxX = maxY = 5;
	}

	public void end()
	{
		setActive(false);
	}

	protected void paintComponent(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		int gw = getWidth() - (margin.left + margin.right);
		int gh = getHeight() - (margin.top + margin.bottom);

		if (gw < 50 || gh < 50) return;

		int n = profile.timeLine.size();
		while (n > 0 && 10 * profile.timeLine.get(n - 1) > maxY * 9)
		{
			maxY *= 2;
		}
		while (n > maxX)
		{
			maxX += 5;
		}

		g.setColor(Color.BLACK);
		drawAxises(g, gw, gh);

		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('0');
		int charHeight = fm.getHeight();

		// Ticks
		Point o = toModel(0, 0);
		g.drawString("0", o.x - charWidth / 2 + 1, o.y + 3 + charHeight);
		for (int x = 1; x <= 5; ++x)
		{
			String str = Integer.toString(maxX * x / 5);
			Point xp = toModel(gw * x / 5, 0);
			g.drawLine(xp.x, xp.y + 2, xp.x, xp.y - 2);
			g.drawString(str, xp.x - charWidth * str.length() / 2 + 1, xp.y + 3 + charHeight);
		}
		for (int y = 1; y <= 5; ++y)
		{
			String str = Integer.toString(maxY * y / 5);
			Point yp = toModel(0, gh * y / 5);
			g.drawLine(yp.x + 2, yp.y, yp.x - 2, yp.y);
			g.drawString(str, yp.x - charWidth * str.length() - 3, yp.y + charHeight / 2);
		}

		if (n >= 2)
		{
			for (int i = 1; i < n; ++i)
			{
				int v0 = profile.timeLine.get(i - 1);
				int v1 = profile.timeLine.get(i);
				Point p0 = toModel((i - 1) * gw / maxX, v0 * gh / maxY);
				Point p1 = toModel(i * gw / maxX, v1 * gh / maxY);
				g.setColor(v0 < v1 ? cState : unState);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			if (n > 0)
			{
				int lastX = n - 1;
				int lastY = profile.timeLine.get(lastX);
				if (active)
				{
					Point p = toModel(lastX * gw / maxX, lastY * gh / maxY);
					g.fillOval(p.x - 3, p.y - 3, 6, 6);
					g.drawString("Searching...", p.x - 22, p.y - 2);
				}
			}
		}
	}

	private void drawAxises(Graphics g, int xMax, int yMax)
	{
		Point o = toModel(0, 0);
		Point p = toModel(xMax, yMax);
		g.drawLine(o.x, o.y, p.x, o.y);
		g.drawLine(o.x, o.y, o.x, p.y);
	}

	private Point toModel(int x, int y)
	{
		return new Point(margin.left + x, getHeight() - margin.bottom - y);
	}
}
