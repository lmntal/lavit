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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import lavit.Env;
import lavit.Lang;
import lavit.runner.SlimInstaller;
import lavit.ui.PathInputField;
import lavit.util.FileUtil;

public final class SlimPathSetting
{
	public enum Result
	{
		USE_INSTALLED,
		INSTALL,
		USE_INCLUDED,
		USE_OTHER
	}

	private static SlimPathPanel sp;
	private static ModalSettingDialog dialog;

	private SlimPathSetting() { }

	public static void showDialog()
	{
		if (dialog == null)
		{
			sp = new SlimPathPanel();
			dialog = ModalSettingDialog.createDialog(sp);
			dialog.setDialogIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
			dialog.setDialogTitle("SLIM Setting");
			dialog.setHeadLineText("Setup SLIM path");
			dialog.setDescriptionText(Lang.w[8] + Env.getSlimBinaryName() + Lang.w[9]);
		}
		boolean approved = dialog.showDialog();
		if (approved)
		{
			if (sp.getResult() == Result.INSTALL)
			{
				installSlim(sp.getSourceDirectory(), sp.getInstallDirectory());
			}
			Env.set("SLIM_EXE_PATH", sp.getSLIMPath());
		}
	}

	private static void installSlim(String sourceDir, String installDir)
	{
		SlimInstaller slimInstaller = new SlimInstaller();
		slimInstaller.run();
	}
}

@SuppressWarnings("serial")
class SlimPathPanel extends JPanel
{
	private static final String SLIM_BIN = Env.getSlimBinaryName();

	private JRadioButton useInstalled;
	private JRadioButton install;
	private JRadioButton useIncluded;
	private JRadioButton useOther;

	private PathInputField pathSource;
	private PathInputField pathInstall;
	private PathInputField pathInput;

	//private String defaultInstallPath = Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfSlim()+File.separator+"src"+File.separator+Env.getSlimBinaryName();
	private String defaultInstallPath = Env.getSlimInstallPath() + File.separator + "bin" + File.separator + SLIM_BIN;
	private String defaultIncludePath = Env.LMNTAL_LIBRARY_DIR + File.separator + "bin" + File.separator + SLIM_BIN;

	public SlimPathPanel()
	{
		setLayout(new GridLayout(0, 1, 0, 4));

		initializeComponents();

		if (!FileUtil.exists(defaultInstallPath))
		{
			useInstalled.setEnabled(false);
		}

		if (getSlimPath().equals("") || getSlimPath().equals(defaultInstallPath))
		{
			if (FileUtil.exists(defaultInstallPath))
			{
				useInstalled.setSelected(true);
				useInstalled.requestFocus();
			}
			else
			{
				install.setSelected(true);
				install.requestFocus();
			}
			pathInput.setPathText(defaultInstallPath);
		}
		else if (getSlimPath().equals(defaultIncludePath))
		{
			useIncluded.setSelected(true);
			useIncluded.requestFocus();
			pathInput.setPathText(defaultIncludePath);
		}
		else
		{
			useOther.setSelected(true);
			useOther.requestFocus();
			pathInput.setPathText(getSlimPath());
		}

		updateState();
	}

	private void initializeComponents()
	{
		useInstalled = new JRadioButton(Lang.w[13] + SLIM_BIN + Lang.w[14]);
		add(useInstalled);

		install = new JRadioButton(Lang.w[2] + SLIM_BIN + Lang.w[3]);
		add(install);

		JLabel label1 = new JLabel("Source Location:");
		JLabel label2 = new JLabel("Install location:");
		JLabel label3 = new JLabel("SLIM location:");

		{
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			pathSource = new PathInputField(chooser, Lang.w[0], 25);
			panel.add(label1);
			panel.add(pathSource);
			add(panel);
		}

		{
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			pathInstall = new PathInputField(chooser, Lang.w[0], 25);
			panel.add(label2);
			panel.add(pathInstall);
			add(panel);
		}

		useIncluded = new JRadioButton(Lang.w[4] + SLIM_BIN + Lang.w[5]);
		add(useIncluded);

		useOther = new JRadioButton(Lang.w[6] + SLIM_BIN + Lang.w[7]);
		add(useOther);

		{
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			pathInput = new PathInputField(chooser, Lang.w[0], 25);
			panel.add(label3);
			panel.add(pathInput);
			add(panel);
		}

		fixLabels(100, 9, label1, label2, label3);

		UpdateAction l = new UpdateAction();
		useInstalled.addActionListener(l);
		install.addActionListener(l);
		useIncluded.addActionListener(l);
		useOther.addActionListener(l);

		ButtonGroup group = new ButtonGroup();
		group.add(useInstalled);
		group.add(install);
		group.add(useIncluded);
		group.add(useOther);
	}

	public String getSourceDirectory()
	{
		return pathSource.getPathText();
	}

	public String getInstallDirectory()
	{
		return pathInstall.getPathText();
	}

	public String getSLIMPath()
	{
		return pathInput.getPathText();
	}

	public SlimPathSetting.Result getResult()
	{
		if (useInstalled.isSelected())
		{
			return SlimPathSetting.Result.USE_INSTALLED;
		}
		else if (install.isSelected())
		{
			return SlimPathSetting.Result.INSTALL;
		}
		else if (useIncluded.isSelected())
		{
			return SlimPathSetting.Result.USE_INCLUDED;
		}
		else
		{
			return SlimPathSetting.Result.USE_OTHER;
		}
	}

	private void updateState()
	{
		pathSource.setEnabled(install.isSelected());
		pathInstall.setEnabled(install.isSelected());

		pathInput.setReadOnly(!useOther.isSelected());

		switch (getResult())
		{
		case USE_INSTALLED:
			pathInput.setPathText(defaultInstallPath);
			break;
		case USE_INCLUDED:
			pathInput.setPathText(defaultIncludePath);
			break;
		case INSTALL:
			pathInput.setPathText(defaultInstallPath);
			break;
		case USE_OTHER:
			pathInput.setPathText(getSlimPath());
			break;
		}
	}

	private String getSlimPath()
	{
		String path = Env.get("SLIM_EXE_PATH");
		return path != null ? path : "";
	}

	private static void fixLabels(int minWidth, int minHeight, JLabel ... labels)
	{
		int w = minWidth;
		for (JLabel l : labels)
		{
			w = Math.max(w, l.getPreferredSize().width);
		}
		for (JLabel l : labels)
		{
			Dimension size = l.getPreferredSize();
			size.width = w;
			size.height = Math.max(size.height, minHeight);
			l.setPreferredSize(size);
		}
	}

	private class UpdateAction implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			updateState();
		}
	}
}
