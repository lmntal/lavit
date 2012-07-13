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
import java.io.File;
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
import lavit.util.OuterRunner;

public class SlimInstaller implements OuterRunner
{
	private static Icon ICON_SUCCESS;
	private static Icon ICON_FAILED;

	private StreamReaderThread outputReader;
	private StreamReaderThread errorReader;
	private ThreadRunner runner;
	private boolean success;
	private File slimSourceDir;

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

	@Override
	public void run()
	{
		runner.start();
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

	public boolean isSuccess()
	{
		return success;
	}

	private File getSlimSourceDir()
	{
		if (slimSourceDir == null)
		{
			slimSourceDir = new File(Env.LMNTAL_LIBRARY_DIR + File.separator + Env.getDirNameOfSlim());
		}
		return slimSourceDir;
	}

	private class ThreadRunner extends Thread
	{
		private Process p;
		private InstallWindow window;

		public ThreadRunner()
		{
			window = new InstallWindow();
		}

		private int execCommand(String cmd)
		{
			try
			{
				ProcessBuilder pb = new ProcessBuilder(strList(cmd));
				Env.setProcessEnvironment(pb.environment());
				pb.directory(getSlimSourceDir());
				p = pb.start();
				
				outputReader = new StreamReaderThread(p.getInputStream());
				outputReader.setPrintLineListener(new PrintLineListener()
				{
					public void println(String line)
					{
						window.println(line);
					}
				});
				outputReader.start();

				errorReader = new StreamReaderThread(p.getErrorStream());
				errorReader.setPrintLineListener(new PrintLineListener()
				{
					public void println(String line)
					{
						window.printError(line);
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
				window.printError(writer.toString());
			}
			return 1;
		}

		@Override
		public void run()
		{
			String shCmd = Env.getBinaryAbsolutePath("sh") + " configure --prefix=" + Env.getSpaceEscape(Env.getSlimInstallLinuxPath() + "/") + " " + Env.get("SLIM_CONFIGURE_OPTION");
			String makeCmd = Env.getBinaryAbsolutePath("make");
			String makeInstallCmd = Env.getBinaryAbsolutePath("make") + " install";
			boolean succeeded = true;
			int ret;

			// sh configure起動
			window.println(shCmd);
			ret = execCommand(shCmd);
			window.println("configure end. exit=" + ret + ".\n");
			succeeded = (ret == 0);

			// make起動
			if (succeeded)
			{
				window.println(makeCmd);
				ret = execCommand(makeCmd);
				window.println("make end. exit=" + ret + ".\n");
				succeeded = (ret == 0);
			}

			// make install起動
			if (succeeded)
			{
				window.println(makeInstallCmd);
				ret = execCommand(makeInstallCmd);
				window.println("make install end. exit=" + ret + ".\n");
				succeeded = (ret == 0);
			}

			// slim.exe が無かったら失敗
			if (succeeded)
			{
				File slimBin = new File(Env.getSlimInstallPath() + File.separator + "bin" + File.separator + Env.getSlimBinaryName());
				succeeded = slimBin.exists();
			}

			if (succeeded)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						JOptionPane.showMessageDialog(
							window, Lang.w[10], "SLIM INSTALL",
							JOptionPane.PLAIN_MESSAGE, ICON_SUCCESS);
					}
				});
			}
			else
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						JOptionPane.showMessageDialog(
							window, Lang.w[11], "SLIM INSTALL",
							JOptionPane.PLAIN_MESSAGE, ICON_FAILED);
					}
				});
			}

			success = succeeded;
			window.exit();
			exit();
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

	@SuppressWarnings("serial")
	private static class InstallWindow extends JFrame
	{
		private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

		private JProgressBar bar;
		private ColoredLinePrinter text;
		private JButton button;

		private String[] progressMatchString =
		{
				"checking for a BSD-compatible install",
				"checking for style of include used by make",
				"checking for suffix of object files",
				"checking whether we are using the GNU C++ compiler",
				"checking lex library",
				"checking for C/C++ restrict keyword",
				"checking for egrep",
				"checking for memory.h",
				"checking for unistd.h",
				"checking for int64_t",
				"checking for uint16_t",
				"checking for void*",
				"checking for strchr",
				"config.status: creating src/Makefile",
				"config.status: executing depfiles commands",
				"configure end.",
				"gcc: unrecognized option",
				"make end.",
				"Making install in doc",
				"make install end"
		};

		public InstallWindow()
		{
			ImageIcon image = new ImageIcon(Env.getImageOfFile("img/slim_c_s.png"));

			setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Installing SLIM...");

		    JPanel panel = new JPanel();
		    panel.setBackground(new Color(255,255,255));
			panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			add(panel);

			JLabel icon = new JLabel(image);
			icon.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(icon);

			bar = new JProgressBar(0,100);
			//bar.setStringPainted(true);
			bar.setIndeterminate(true);
			panel.add(bar);

			text = new ColoredLinePrinter();
			text.setEditable(false);
			//text.setLineWrap(false);
			text.setBackground(Color.BLACK);
			text.setForeground(Color.WHITE);
			text.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
			JScrollPane textScrollPane = new JScrollPane(text);
			textScrollPane.setPreferredSize(new Dimension(image.getIconWidth(),image.getIconHeight()/2));
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
			setLocationRelativeTo(null);
		    setVisible(true);
		}
		
		private void progress(String s)
		{
			//progress barの処理
			for(int i=0;i<progressMatchString.length;++i){
				if(s.startsWith(progressMatchString[i])){
					bar.setIndeterminate(false);
					final int progress = (i+1)*5;
					SwingUtilities.invokeLater(new Runnable(){public void run(){
						if(bar.getValue()<progress){
							bar.setValue(progress);
						}
					}});
				}
			}
		}

		private void println(String str)
		{
			if (str.length() > 0)
			{
				progress(str);
				printColoredLine(str, Color.WHITE);
			}
		}

		private void printError(String str)
		{
			if (str.length() > 0)
			{
				progress(str);
				printColoredLine(str, Color.GRAY);
			}
		}
		
		private void printColoredLine(String s, Color c)
		{
			text.appendLine(String.format("[%s] %s", DATE_FORMAT.format(new Date()), s), c);
		}

		public void exit()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					//bar.setIndeterminate(false);
					bar.setValue(100);
					button.setEnabled(true);
					setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				}
			});
		}
	}
}
