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

package lavit.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.util.StringUtils;

@SuppressWarnings("serial")
public class VersionDialog extends JDialog
{
	private static VersionDialog instance;

	private JTextArea infoText;

	private VersionDialog(Window owner)
	{
		super(owner);

		Icon image = new ImageIcon(Env.getImageOfFile("img/logo.png"));

		setTitle("Version information");
		setIconImages(Env.getApplicationIcons());
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel);

		JLabel icon = new JLabel(image);
		icon.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(icon);

		infoText = new JTextArea();
		infoText.setAlignmentX(Component.CENTER_ALIGNMENT);
		infoText.setEditable(false);
		infoText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		panel.add(new JScrollPane(infoText));

		JButton buttonOK = new JButton("OK");
		buttonOK.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		Dimension size = buttonOK.getPreferredSize();
		size.width = Math.max(size.width, 100);
		buttonOK.setPreferredSize(size);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setOpaque(false);
		buttonPanel.add(buttonOK);
		panel.add(buttonPanel);

		getRootPane().setDefaultButton(buttonOK);

		addWindowListener(new ChildWindowListener(this));
	}

	private void updateInformationText()
	{
		String lmntalVersion = Env.getLMNtalVersion();
		String slimVersion = Env.getSlimVersion();
		if (StringUtils.nullOrEmpty(slimVersion))
		{
			slimVersion = Lang.w[15];
		}
		String info =
			"LaViT\n" +
			"Version : " + Env.APP_VERSION + "\n" +
			"Date : " + Env.APP_DATE + "\n\n" +
			lmntalVersion + "\n" +
			"SLIM : " + slimVersion + "\n" +
			Env.UNYO_VERSION;
		infoText.setText(info);
	}

	public static void showDialog()
	{
		if (instance == null)
		{
			instance = new VersionDialog(FrontEnd.mainFrame);
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				instance.updateInformationText();
				instance.pack();
				instance.setLocationRelativeTo(null);
				instance.setVisible(true);
			}
		});
	}
}
