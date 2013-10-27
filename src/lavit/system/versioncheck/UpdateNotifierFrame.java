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

package lavit.system.versioncheck;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class UpdateNotifierFrame extends JDialog
{
	private LinkLabel link;
	private JTextArea details;

	public UpdateNotifierFrame(Frame owner, String title, String headerText, String versionText, String releaseText)
	{
		super(owner, title);

		JLabel labelIcon = new JLabel();
		try
		{
			Image iconImage = ImageIO.read(UpdateNotifierFrame.class.getResource("icon_update_64.png"));
			labelIcon.setIcon(new ImageIcon(iconImage));
		}
		catch (Exception e)
		{
		}
		labelIcon.setOpaque(true);
		labelIcon.setBackground(Color.WHITE);
		labelIcon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		Font headFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
		Font textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

		JLabel labelHead = new JLabel(headerText);
		labelHead.setFont(textFont);
		labelHead.setOpaque(true);
		labelHead.setBackground(Color.WHITE);
		labelHead.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

		JPanel panelHead = new JPanel(new BorderLayout());
		panelHead.add(labelIcon, BorderLayout.WEST);
		panelHead.add(labelHead, BorderLayout.CENTER);
		add(panelHead, BorderLayout.NORTH);

		JLabel labelVersion = new JLabel("Version");
		labelVersion.setFont(headFont);
		JLabel labelRelease = new JLabel("Release");
		labelRelease.setFont(headFont);
		JLabel labelVersionValue = new JLabel(versionText);
		labelVersionValue.setFont(textFont);
		JLabel labelReleaseValue = new JLabel(releaseText);
		labelReleaseValue.setFont(textFont);
		link = new LinkLabel("Download Site");
		link.setFont(textFont);
		link.setHorizontalAlignment(SwingConstants.CENTER);

		final JLabel labelShowDetail = new JLabel("+ Show Description");
		labelShowDetail.setFont(textFont);

		details = new JTextArea(4, 20);
		details.setEditable(false);

		final JScrollPane detailJsp = new JScrollPane(details);
		detailJsp.setVisible(false);

		labelShowDetail.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						if (detailJsp.isVisible())
						{
							detailJsp.setVisible(false);
							labelShowDetail.setText("+ Show Description");
						}
						else
						{
							detailJsp.setVisible(true);
							labelShowDetail.setText("- Hide Description");
						}
						pack();
					}
				});
			}
		});

		JPanel panelCenter = new JPanel();
		GroupLayout gl = new GroupLayout(panelCenter);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.LEADING)
			.addGroup(gl.createSequentialGroup()
				.addGroup(gl.createParallelGroup(Alignment.TRAILING)
					.addComponent(labelVersion)
					.addComponent(labelRelease)
				)
				.addGroup(gl.createParallelGroup()
					.addComponent(labelVersionValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(labelReleaseValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
			)
			.addComponent(link, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addComponent(labelShowDetail)
			.addComponent(detailJsp)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(labelVersion)
				.addComponent(labelVersionValue)
			)
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(labelRelease)
				.addComponent(labelReleaseValue)
			)
			.addComponent(link)
			.addComponent(labelShowDetail)
			.addComponent(detailJsp)
		);
		panelCenter.setLayout(gl);
		add(panelCenter, BorderLayout.CENTER);

		JButton buttonClose = new JButton("OK");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		});
		ButtonPanel buttonPanel = new ButtonPanel(buttonClose);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
	}

	public void setLinkUrl(String url)
	{
		link.setURL(url);
	}

	public void setDescriptionText(String text)
	{
		details.setText(text);
	}

	private void close()
	{
		dispose();
	}
}

@SuppressWarnings("serial")
class ButtonPanel extends JPanel
{
	public ButtonPanel(JComponent ... components)
	{
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(4, 4, 4, 4),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)
			)
		));
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		for (JComponent c : components)
		{
			add(c);
		}
		fixWidth(100, 0, components);
	}

	private static void fixWidth(int minWidth, int minHeight, JComponent ... components)
	{
		Dimension size = new Dimension(minWidth, minHeight);
		for (JComponent c : components)
		{
			Dimension d = c.getPreferredSize();
			size.width = Math.max(size.width, d.width);
			size.height = Math.max(size.height, d.height);
		}
		for (JComponent c : components)
		{
			c.setPreferredSize(size);
		}
	}
}

@SuppressWarnings("serial")
class LinkLabel extends JLabel
{
	private String url;
	private boolean hover;

	public LinkLabel(String text)
	{
		this(text, "");
	}

	public LinkLabel(String text, String url)
	{
		super(text);
		addMouseListener(new MouseHandler());
		setForeground(Color.BLUE);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setURL(url);
	}

	public void setURL(String url)
	{
		this.url = url;
		setToolTipText(url);
	}

	protected void paintComponent(Graphics g)
	{
		if (hover)
		{
			FontMetrics fm = g.getFontMetrics();
			int w = fm.stringWidth(getText());
			int x = (getWidth() - w) / 2;
			int y = getBaseline(getWidth(), getHeight()) + 1;
			g.setColor(getForeground());
			g.drawLine(x, y, x + w, y);
		}
		super.paintComponent(g);
	}

	private void browseUrl()
	{
		if (url != null && !url.isEmpty())
		{
			try
			{
				Desktop.getDesktop().browse(new URI(url));
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class MouseHandler extends MouseAdapter
	{
		public void mouseEntered(MouseEvent e)
		{
			hover = true;
			repaint();
		}

		public void mouseExited(MouseEvent e)
		{
			hover = false;
			repaint();
		}

		public void mouseClicked(MouseEvent e)
		{
			browseUrl();
		}
	}
}
