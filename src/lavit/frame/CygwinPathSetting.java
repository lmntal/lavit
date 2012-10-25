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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lavit.Env;
import lavit.Lang;
import lavit.util.StringUtils;

public final class CygwinPathSetting
{
	private static SelectPanel sp;
	private static ModalSettingDialog dialog;

	private CygwinPathSetting() { }

	public static void showDialog()
	{
		if (dialog == null)
		{
			sp = new SelectPanel();
			dialog = ModalSettingDialog.createDialog(sp);
			dialog.setDialogTitle("Cygwin Path Setting");
			dialog.setHeadLineText("Cygwin path setting");
			dialog.setDescriptionText(Lang.w[1]);
			dialog.setDialogIconImages(Env.getApplicationIcons());
			dialog.setDialogResizable(false);
		}

		String path = Env.get("WINDOWS_CYGWIN_DIR");
		if (StringUtils.nullOrEmpty(path))
		{
			path = "C:\\cygwin";
		}
		sp.setPathString(path);

		boolean approved = dialog.showDialog();
		if (approved)
		{
			Env.set("WINDOWS_CYGWIN_DIR", sp.getPathString());
		}
	}
}

@SuppressWarnings("serial")
class SelectPanel extends JPanel
{
	private JTextField textPath;
	private JFileChooser fileChooser;

	public SelectPanel()
	{
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);

		JLabel label = new JLabel("Cygwin path:");
		add(label);

		textPath = new JTextField(20);
		textPath.setColumns(20);
		add(textPath);

		JButton buttonBrowse = new JButton(Lang.w[0]);
		buttonBrowse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				browse();
			}
		});
		add(buttonBrowse);

		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.LEADING)
			.addComponent(label)
			.addGroup(gl.createSequentialGroup()
				.addComponent(textPath)
				.addComponent(buttonBrowse)
			)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addComponent(label)
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(textPath)
				.addComponent(buttonBrowse)
			)
		);
	}

	public String getPathString()
	{
		return textPath.getText();
	}

	public void setPathString(String s)
	{
		textPath.setText(s);
	}

	private void browse()
	{
		File file = new File(getPathString());
		if (file.exists() && file.getParentFile() != null)
		{
			file = file.getParentFile();
		}
		else
		{
			file = new File(".");
		}
		JFileChooser chooser = getFileChooser();
		chooser.setCurrentDirectory(file);
		int res = chooser.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION)
		{
			setPathString(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private JFileChooser getFileChooser()
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		return fileChooser;
	}
}
