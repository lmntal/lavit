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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import lavit.Env;

@SuppressWarnings("serial")
public class UpdateNotifierFrame extends JDialog
{
	private JTextArea details;
	private boolean approved;

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

		final JCheckBox checkDisable = new JCheckBox("Disable update checking");
		checkDisable.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Env.set("updatecheck.enabled", !checkDisable.isSelected());
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
			.addComponent(labelShowDetail)
			.addComponent(detailJsp)
			.addComponent(checkDisable)
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
			.addComponent(labelShowDetail)
			.addComponent(detailJsp)
			.addComponent(checkDisable)
		);
		panelCenter.setLayout(gl);
		add(panelCenter, BorderLayout.CENTER);

		JButton buttonUpdate = new JButton("Update Now");
		buttonUpdate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				approved = true;
				close();
			}
		});
		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				approved = false;
				close();
			}
		});
		ButtonPanel buttonPanel = new ButtonPanel(buttonUpdate, buttonCancel);
		add(buttonPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(buttonUpdate);

		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);

		pack();
	}

	public boolean isApproved()
	{
		return approved;
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
