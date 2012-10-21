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

package lavit.runner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lavit.Env;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.ui.ColoredLinePrinter;
import lavit.util.Cygpath;
import lavit.util.FileUtils;
import lavit.util.IntUtils;
import lavit.util.OuterRunner;
import lavit.util.StringUtils;

public class SlimInstaller implements OuterRunner
{
	private static final DateFormat HEAD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private static final String[] PROGRESS_MATCH_STRING =
	{
		"checking build system type",
		"checking lex output file root",
		"checking for main in -lrt",
		"checking whether the gcc linker",
		"checking for glob.h",
		"checking for backtrace in -lunwind",
		"configure: creating ./config.status",
		"config.status: creating src/genconfig",
		"LaViT: End - configure",
		"gcc: unrecognized option",
		"LaViT: End - make (",
		"Making install in doc",
		"LaViT: End - make install (",
	};

	private static Icon ICON_SUCCESS;
	private static Icon ICON_FAILED;

	private StreamReaderThread outputReader;
	private StreamReaderThread errorReader;
	private InstallWindow window;
	private ThreadRunner runner;
	private boolean success;
	private String slimSourceDir;
	private String slimInstallDir;
	private PrintWriter logWriter;
	private ActionListener finishListener; // TODO: Create unique listener interface instead of ActionListener.

	public SlimInstaller()
	{
		if (ICON_SUCCESS == null)
		{
			ICON_SUCCESS = new ImageIcon(Env.getImageOfFile("img/icon_success.png"));
		}

		if (ICON_FAILED == null)
		{
			ICON_FAILED = new ImageIcon(Env.getImageOfFile("img/icon_failed.png"));
		}

		this.runner = new ThreadRunner();
		this.success = false;
	}

	/**
	 * Set location of SLIM source directory.
	 * The default location is: './lmntal/slim-x.y.z'
	 */
	public void setSlimSourceDirectory(String dir)
	{
		slimSourceDir = dir;
	}

	/**
	 * Set location of SLIM install directory.
	 * The default location is: 'lmntal/installed'
	 */
	public void setSlimInstallDirectory(String dir)
	{
		slimInstallDir = dir;
	}

	@Override
	public void run()
	{
		window = new InstallWindow(PROGRESS_MATCH_STRING.length - 1);
		window.addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				runner.start();
			}
		});
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				window.setLocationRelativeTo(null);
				window.setVisible(true);
			}
		});
	}

	public void waitFor()
	{
		try
		{
			runner.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void setFinishListener(ActionListener l)
	{
		finishListener = l;
	}

	@Override
	public boolean isRunning()
	{
		if(runner==null) return false;
		return true;
	}

	@Override
	public void kill()
	{
		if (runner!=null)
		{
			runner.kill();
			runner.interrupt();
			runner = null;
		}
	}

	public void exit()
	{
		runner = null;
	}

	public boolean isSucceeded()
	{
		return success;
	}

	private String getAbsolutePath(String path)
	{
		return new File(path).getAbsolutePath();
	}

	private String getSlimSourcePathName()
	{
		if (StringUtils.nullOrEmpty(slimSourceDir))
		{
			slimSourceDir = Env.LMNTAL_LIBRARY_DIR + File.separator + Env.getDirNameOfSlim();
		}
		return getAbsolutePath(slimSourceDir);
	}

	private String getSlimInstallPathName()
	{
		if (StringUtils.nullOrEmpty(slimInstallDir))
		{
			slimInstallDir = Env.getSlimInstallPath();
		}
		return getAbsolutePath(slimInstallDir);
	}

	private String getLinuxStyleSlimInstallPathName()
	{
		if (Env.isWindows())
		{
			return Cygpath.toLinuxStyle(getSlimInstallPathName());
		}
		else
		{
			return getSlimInstallPathName();
		}
	}

	private static String format(String str)
	{
		return String.format("[%s] %s", LOG_DATE_FORMAT.format(new Date()), str);
	}

	private void logStart(String fileName)
	{
		try
		{
			logWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
			logWriter.println("LaViT SLIM Installer Log");
			logWriter.println("Date: " + HEAD_DATE_FORMAT.format(new Date()));
			logWriter.println("------------------------------------");
		}
		catch (FileNotFoundException e)
		{
			logWriter = null;
		}
	}

	private void logEnd()
	{
		if (logWriter != null)
		{
			logWriter.close();
		}
	}

	private void logPrintLine(String str)
	{
		logPrint(str, false);
	}

	private void logPrintError(String str)
	{
		logPrint(str, true);
	}

	private void logPrint(String str, boolean error)
	{
		String s = format(str);
		if (window != null)
		{
			for (int i = 0; i < PROGRESS_MATCH_STRING.length; i++)
			{
				if (str.startsWith(PROGRESS_MATCH_STRING[i]))
				{
					window.setProgressValue(i);
					break;
				}
			}

			if (error)
			{
				window.printError(s);
			}
			else
			{
				window.println(s);
			}
		}
		if (logWriter != null)
		{
			if (error)
			{
				logWriter.println("! " + s);
			}
			else
			{
				logWriter.println("  " + s);
			}
		}
	}

	private class ThreadRunner extends Thread
	{
		private Process p;

		private int execCommand(String cmd)
		{
			try
			{
				ProcessBuilder pb = new ProcessBuilder(strList(cmd));
				Env.setProcessEnvironment(pb.environment());
				pb.directory(new File(getSlimSourcePathName()));
				p = pb.start();

				outputReader = new StreamReaderThread(p.getInputStream());
				outputReader.setPrintLineListener(new PrintLineListener()
				{
					public void println(String line)
					{
						logPrintLine(line);
					}
				});
				outputReader.start();

				errorReader = new StreamReaderThread(p.getErrorStream());
				errorReader.setPrintLineListener(new PrintLineListener()
				{
					public void println(String line)
					{
						logPrintError(line);
					}
				});
				errorReader.start();

				p.waitFor();
				return p.exitValue();
			}
			catch (Exception e)
			{
				PrintWriter writer = new PrintWriter(new StringWriter());
				e.printStackTrace(writer);
				logPrintError(writer.toString());
			}
			return 1;
		}

		@Override
		public void run()
		{
			String shCmd = Env.getBinaryAbsolutePath("sh") + " configure --prefix=" + getLinuxStyleSlimInstallPathName() + " " + Env.get("SLIM_CONFIGURE_OPTION");
			String makeCmd = Env.getBinaryAbsolutePath("make");
			String makeInstallCmd = Env.getBinaryAbsolutePath("make") + " install";
			boolean succeeded = true;
			int ret;

			logStart("slim_install_log.txt");

			// sh configure起動
			logPrintLine("LaViT: Execute - " + shCmd);
			ret = execCommand(shCmd);
			logPrintLine("LaViT: End - configure (Exit Code = " + ret + ")\n");
			succeeded = (ret == 0);

			// make起動
			if (succeeded)
			{
				logPrintLine("LaViT: Execute - " + makeCmd);
				ret = execCommand(makeCmd);
				logPrintLine("LaViT: End - make (Exit Code = " + ret + ")\n");
				succeeded = (ret == 0);
			}

			// make install起動
			if (succeeded)
			{
				logPrintLine("LaViT: Execute - " + makeInstallCmd);
				ret = execCommand(makeInstallCmd);
				logPrintLine("LaViT: End - make install (Exit Code = " + ret + ")\n");
				succeeded = (ret == 0);
			}

			// slim.exe が無かったら失敗
			if (succeeded)
			{
				String slimPath = getSlimInstallPathName() + File.separator + "bin" + File.separator + Env.getSlimBinaryName();
				succeeded = FileUtils.exists(slimPath);
			}
			success = succeeded;

			if (isSucceeded())
			{
				logPrintLine("== SLIM INSTALL SUCCEEDED ==");
				logPrintLine("slim is in " + getSlimInstallPathName());
				JOptionPane.showMessageDialog(
					window, Lang.w[10], "SLIM INSTALL",
					JOptionPane.PLAIN_MESSAGE, ICON_SUCCESS);
			}
			else
			{
				logPrintLine("== SLIM INSTALL FAILED ==");
				JOptionPane.showMessageDialog(
					window, Lang.w[11], "SLIM INSTALL",
					JOptionPane.PLAIN_MESSAGE, ICON_FAILED);
			}

			window.exit();
			exit();

			logEnd();

			if (finishListener != null)
			{
				finishListener.actionPerformed(null);
			}
		}

		private List<String> strList(String str)
		{
			List<String> cmdList = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(str);
			while (st.hasMoreTokens())
			{
				String s = st.nextToken();
				if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"')
				{
					s = s.substring(1, s.length() - 1);
				}
				cmdList.add(s);
			}
			return cmdList;
		}

		private void kill()
		{
			if (p != null) p.destroy();
		}
	}
}

@SuppressWarnings("serial")
class InstallWindow extends JFrame
{
	private JProgressBar bar;
	private ColoredLinePrinter text;
	private JButton button;

	public InstallWindow(int progressMax)
	{
		ImageIcon image = new ImageIcon(Env.getImageOfFile("img/slim_c_s.png"));

		setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Installing SLIM...");

	    JPanel panel = new JPanel();
	    panel.setBackground(Color.WHITE);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel);

		JLabel icon = new JLabel(image);
		icon.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(icon);

		bar = new JProgressBar(0, progressMax);
		bar.setIndeterminate(true);
		panel.add(bar);

		text = new ColoredLinePrinter();
		text.setEditable(false);
		text.setBackground(Color.BLACK);
		text.setForeground(Color.WHITE);
		text.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
		JScrollPane textScrollPane = new JScrollPane(text);
		textScrollPane.setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight() / 2));
		panel.add(textScrollPane);

		button = new JButton("OK");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		button.setEnabled(false);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(button);

		addWindowListener(new ChildWindowListener(this));

		pack();
	}

	public void setProgressValue(int value)
	{
		if (bar.isIndeterminate())
		{
			bar.setIndeterminate(false);
		}
		bar.setValue(IntUtils.clamp(value, bar.getMinimum(), bar.getMaximum()));
	}

	public void println(String str)
	{
		printColoredLine(str, Color.WHITE);
	}

	public void printError(String str)
	{
		printColoredLine(str, Color.GRAY);
	}

	private void printColoredLine(final String str, final Color c)
	{
		if (!str.isEmpty())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					text.appendLine(str, c);
				}
			});
		}
	}

	public void exit()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				bar.setValue(bar.getMaximum());
				button.setEnabled(true);
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			}
		});
	}
}
