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

package lavit.ltl;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.undo.UndoManager;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.runner.Ltl2baInstaller;
import lavit.runner.Ltl2baRunner;
import lavit.util.CommonFontUser;
import lavit.util.SimpleUndoKeyListener;
import lavit.util.SimpleUndoableEditListener;

@SuppressWarnings("serial")
class LtlNcPanel extends JPanel implements ActionListener, CommonFontUser
{
	private JPanel panel;
	private JTextField ltlText;
	private UndoManager ltlUndoManager = new UndoManager();
	private JButton ltlButton;

	private JTextArea ncArea;

	public LtlNcPanel()
	{
		setBorder(new TitledBorder("Never Claims"));
		setLayout(new BorderLayout());

		panel = new JPanel(new BorderLayout());
		panel.setBorder(new TitledBorder("LTL formula"));
		ltlText = new JTextField();
		ltlText.addActionListener(this);
		ltlText.getDocument().addUndoableEditListener(new SimpleUndoableEditListener(ltlUndoManager));
		ltlText.addKeyListener(new SimpleUndoKeyListener(ltlUndoManager));
		panel.add(ltlText, BorderLayout.CENTER);
		ltlButton = new JButton("Translate");
		ltlButton.addActionListener(this);
		panel.add(ltlButton, BorderLayout.EAST);
		add(panel,BorderLayout.NORTH);

		ncArea = new JTextArea();
		add(new JScrollPane(ncArea), BorderLayout.CENTER);

		loadFont();
		FrontEnd.addFontUser(this);
	}

	public void loadFont()
	{
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		ncArea.setFont(font);
		ltlText.setFont(font);
		revalidate();
	}

	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if (src == ltlButton || src == ltlText)
		{
			final Ltl2baInstaller ltl2baInstaller = new Ltl2baInstaller();
			if (ltl2baInstaller.isNeedInstall())
			{
				if (ltl2baInstaller.isInstallable())
				{
					ltl2baInstaller.run();

					new Thread()
					{
						public void run()
						{
							while (ltl2baInstaller.isRunning())
							{
								FrontEnd.sleep(200);
							}
							if (ltl2baInstaller.isSucceeded())
							{
								ltlButton.doClick();
							}
						}
					}.start();
				}
				else
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							JOptionPane.showMessageDialog(
								FrontEnd.mainFrame,
								Lang.w[12]+" (lmntal/"+Env.getDirNameOfLtl2ba()+")",
								"LTL2BA INSTALL",
								JOptionPane.PLAIN_MESSAGE);
						}
					});
				}
				return;
			}

			final Ltl2baRunner runner = new Ltl2baRunner(ltlText.getText());
			runner.run();
			new Thread()
			{
				public void run()
				{
					while (runner.isRunning())
					{
						FrontEnd.sleep(200);
					}
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							ncArea.setText(runner.getOutput());
						}
					});
				}
			}.start();
		}
	}

	public void setText(String str)
	{
		ncArea.setText(str);

		int l = str.indexOf("/*");
		int r = str.indexOf("*/");
		if (!(l > 0 && r > 0 && l < r)) return;

		String ltl = str.substring(l + 2, r).trim();

		if (ltl.startsWith("!(") && ltl.endsWith(")"))
		{
			ltlText.setText(ltl.substring(2, ltl.length() - 1).replaceAll("!!", ""));
		}
		else
		{
			ltlText.setText(("!" + ltl).replaceAll("!!", ""));
		}

		ltlUndoManager.discardAllEdits();
	}

	public void clearText()
	{
		ltlText.setText("");
		ncArea.setText("");
	}

	public String getText()
	{
		return ncArea.getText();
	}
}
