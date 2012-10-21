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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.Env;
import lavit.Lang;
import lavit.frame.ModalSettingDialog.ClosingEvent;
import lavit.runner.SlimInstaller;
import lavit.ui.PathInputField;
import lavit.util.FileUtils;
import lavit.util.StringUtils;

public final class SlimPathSetting
{
	enum Result
	{
		USE_INSTALLED,
		INSTALL,
		USE_INCLUDED,
		USE_OTHER
	}

	private SlimPathPanel sp;
	private ModalSettingDialog dialog;
	private SlimInstaller installer;

	public SlimPathSetting()
	{
	}

	public boolean showDialog()
	{
		if (dialog == null)
		{
			sp = new SlimPathPanel();
			dialog = ModalSettingDialog.createDialog(sp);
			dialog.setDialogIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
			dialog.setDialogTitle("SLIM Setting");
			dialog.setHeadLineText("Setup SLIM path");
			dialog.setDescriptionText(Lang.w[8] + Env.getSlimBinaryName() + Lang.w[9]);
			dialog.setClosingListener(new ModalSettingDialog.ClosingListener()
			{
				public void dialogClosing(ClosingEvent e)
				{
					if (e.isApproved() && !sp.verifyConfiguration())
					{
						e.setCanceled(true);
					}
				}
			});
		}
		boolean approved = dialog.showDialog();
		if (approved)
		{
			if (sp.getResult() == Result.INSTALL)
			{
				installSlim(sp.getSourceDirectory(), sp.getInstallDirectory());
			}
			else
			{
				Env.set("SLIM_EXE_PATH", sp.getSlimBinaryPath());
				Env.set("path.slim.exe", sp.getSlimBinaryPath());
				Env.set("version.slim", getSlimVersion(Env.get("path.slim.exe")));
			}
		}
		return approved;
	}

	public void waitFor()
	{
		installer.waitFor();
	}

	private void installSlim(final String sourceDir, final String installDir)
	{
		File installDirFile = new File(installDir);
		if (!installDirFile.exists())
		{
			installDirFile.mkdir();
		}

		installer = new SlimInstaller();
		installer.setSlimSourceDirectory(sourceDir);
		installer.setSlimInstallDirectory(installDir);
		installer.setFinishListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (installer.isSucceeded())
				{
					String slimPath = sp.getSlimBinaryPath();
					Env.set("path.slim.source", sourceDir);
					Env.set("path.slim.install", installDir);
					Env.set("path.slim.exe", slimPath);
					Env.set("SLIM_EXE_PATH", slimPath);
					Env.set("version.slim", getSlimVersion(slimPath));
				}
			}
		});
		installer.run();
	}

	/**
	 * Gets SLIM version by executing {@code slim --version}.
	 */
	private static String getSlimVersion(String slimPath)
	{
		if (StringUtils.nullOrEmpty(slimPath))
		{
			return "";
		}

		ProcessBuilder pb = new ProcessBuilder(slimPath, "--version");
		pb.redirectErrorStream(true);

		String version = "";
		try
		{
			Process p = pb.start();
			p.getOutputStream().close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			p.getInputStream().close();
			p.getErrorStream().close();
			p.waitFor();

			Pattern pat = Pattern.compile("\\d+\\.\\d+\\.\\d+");
			Matcher m = pat.matcher(line);
			if (m.find())
			{
				version = m.group();
			}
		}
		catch (IOException e)
		{
		}
		catch (InterruptedException e)
		{
		}
		return version;
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
	private PathInputField pathSlim;

	private final String defaultIncludeDir = Env.LMNTAL_LIBRARY_DIR;
	private final String defaultIncludePath = defaultIncludeDir + File.separator + "bin" + File.separator + SLIM_BIN;

	public SlimPathPanel()
	{
		setLayout(new GridLayout(0, 1, 0, 4));

		initializeComponents();
		initializeFields();
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
			pathInstall.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					pathSlim.setPathText(createSlimPath());
				}
			});
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
			pathSlim = new PathInputField(chooser, Lang.w[0], 25);
			panel.add(label3);
			panel.add(pathSlim);
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

	/**
	 * 初期の入力状態を構成します。
	 */
	private void initializeFields()
	{
		String installedSlimPath = Env.get("SLIM_EXE_PATH");
		if (!StringUtils.nullOrEmpty(installedSlimPath) && FileUtils.exists(installedSlimPath))
		{
			// インストール済みのSLIMを選択
			useInstalled.setSelected(true);
			pathSlim.setPathText(installedSlimPath);
		}
		else
		{
			// SLIMのインストールを選択
			useInstalled.setEnabled(false);
			install.setSelected(true);
		}
		updateState();
	}

	/**
	 * SLIMソースコードディレクトリのパス名を取得します。
	 */
	public String getSourceDirectory()
	{
		return pathSource.getPathText();
	}

	/**
	 * SLIMのインストール先ディレクトリのパス名を取得します。
	 */
	public String getInstallDirectory()
	{
		return pathInstall.getPathText();
	}

	/**
	 * SLIMバイナリのパス名を取得します。
	 */
	public String getSlimBinaryPath()
	{
		return pathSlim.getPathText();
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

	/**
	 * 設定項目が適切であるか検査します。
	 */
	public boolean verifyConfiguration()
	{
		switch (getResult())
		{
		case USE_INSTALLED:
			if (!FileUtils.exists(getSlimBinaryPath()))
			{
				showError("file \"" + getSlimBinaryPath() + "\" is not found.");
				return false;
			}
			break;
		case INSTALL:
			if (!FileUtils.exists(getSourceDirectory()))
			{
				showError("directory \"" + getSourceDirectory() + "\" is not found.");
				return false;
			}
			else if (!new File(getSourceDirectory()).isDirectory())
			{
				showError("\"" + getSourceDirectory() + "\" is not a directory.");
				return false;
			}
			break;
		case USE_INCLUDED:
			break;
		case USE_OTHER:
			break;
		}
		return true;
	}

	private void updateState()
	{
		pathSource.setEnabled(install.isSelected());
		pathInstall.setEnabled(install.isSelected());

		pathSlim.setReadOnly(!useOther.isSelected());

		switch (getResult())
		{
		case USE_INSTALLED:
			pathSource.setPathText("");
			pathInstall.setPathText("");
			pathSlim.setPathText(Env.get("SLIM_EXE_PATH"));
			break;
		case USE_INCLUDED:
			pathSource.setPathText("");
			pathInstall.setPathText(defaultIncludeDir);
			pathSlim.setPathText(defaultIncludePath);
			break;
		case INSTALL:
			pathSource.setPathText(Env.estimateSlimSourcePath());
			pathInstall.setPathText(Env.getSlimInstallPath());
			pathSlim.setPathText(createSlimPath());
			break;
		case USE_OTHER:
			pathSource.setPathText("");
			pathInstall.setPathText("");
			pathSlim.setPathText("");
			break;
		}
	}

	/**
	 * 指定されたインストール先パス名からSLIMバイナリのパス名を作成する。
	 */
	private String createSlimPath()
	{
		return getInstallDirectory() + File.separator + "bin" + File.separator + SLIM_BIN;
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

	private void showError(String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private class UpdateAction implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			updateState();
		}
	}
}
