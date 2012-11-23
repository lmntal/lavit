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

package extgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
public class TitledPane extends JPanel
{
	private JLabel titleLabel;

	public TitledPane()
	{
		this("");
	}

	public TitledPane(String title)
	{
		this(title, new JLabel("content"));
	}

	public TitledPane(String title, JComponent contentView)
	{
		setLayout(new BorderLayout());

		Border border = BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(2, 2, 2, 2),
			BorderFactory.createLineBorder(Color.GRAY));
		setBorder(border);

		titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		JPanel panel = new GradientPanel();
		panel.setLayout(new BorderLayout());
		panel.add(titleLabel);

		add(panel, BorderLayout.NORTH);

		setContentView(contentView);
	}

	public void setContentView(JComponent comp)
	{
		add(comp, BorderLayout.CENTER);
		validate();
	}

	public void setTitle(String title)
	{
		titleLabel.setText(title);
	}

	public String getTitle()
	{
		return titleLabel.getText();
	}

	private static final class GradientPanel extends JPanel
	{
		protected void paintComponent(Graphics g)
		{
			Rectangle bounds = getBounds();
			int y1 = bounds.y;
			int y2 = bounds.y + bounds.height;
			Color c1 = new Color(240, 240, 255);
			Color c2 = c1.darker();

			Graphics2D g2 = (Graphics2D)g;
			g2.setPaint(new GradientPaint(0, y1, c1, 0, y2, c2));
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}
