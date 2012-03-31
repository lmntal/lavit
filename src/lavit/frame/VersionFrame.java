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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import lavit.Env;

@SuppressWarnings("serial")
public class VersionFrame extends JDialog
{
	private JButton button;
	private String strTable[] =
	{
		"LaViT",
		"Version : " + Env.APP_VERSION,
		"Date : " + Env.APP_DATE,
		"",
		Env.LMNTAL_VERSION,
		Env.SLIM_VERSION,
		Env.UNYO_VERSION
	};

	public VersionFrame()
	{
		ImageIcon image = new ImageIcon(Env.getImageOfFile("img/logo.png"));

		setTitle("Version information");
		setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
		setResizable(false);
		setAlwaysOnTop(true);

	    JPanel panel = new JPanel();
	    panel.setBackground(new Color(255,255,255));
	    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		add(panel);

		JLabel icon = new JLabel(image);
		icon.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(icon);

		JPanel lPanel = new JPanel();
		lPanel.setBorder(new LineBorder(Color.WHITE , 10));
		lPanel.setBackground(Color.WHITE);
		lPanel.setLayout(new GridLayout((int)(strTable.length/1),1));
		for(String s : strTable){
			lPanel.add(new JLabel(s));
		}
		panel.add(lPanel);

		button = new JButton("OK");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(button);

		addWindowListener(new ChildWindowListener(this));

		pack();
		setLocationRelativeTo(null);
	 	setVisible(true);
	}
}
