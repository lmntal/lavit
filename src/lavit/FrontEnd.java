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

package lavit;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lavit.frame.CygwinPathSetting;
import lavit.frame.LaViTSplashWindow;
import lavit.frame.LanguageSetting;
import lavit.frame.MainFrame;
import lavit.frame.SlimPathSetting;
import lavit.runner.PrintLineListener;
import lavit.runner.ProcessFinishListener;
import lavit.runner.ProcessTask;
import lavit.runner.RebootRunner;
import lavit.system.versioncheck.UpdateChecker;
import lavit.util.CommonFontUser;
import lavit.util.LookAndFeelEntry;
import lavit.util.StringUtils;

public class FrontEnd
{
	public static MainFrame mainFrame;

	private static Set<CommonFontUser> fontUsers = new HashSet<CommonFontUser>();
	private static List<ProcessTask> processTasks = new ArrayList<ProcessTask>();

	private static void loadArgs(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].isEmpty() || args[i].charAt(0) != '-')
			{
				continue;
			}

			if (args[i].equals("--stateviewer"))
			{
				if (i + 1 < args.length)
				{
					File file = new File(args[i + 1]);
					if (file.exists())
					{
						//mainFrame.jsp.setDividerLocation(0);
						mainFrame.toolTab.statePanel.loadFile(file);
					}
				}
			}
			else
			{
				println("invalid option: " + args[i]);
			}
		}
	}

	public static void executeUnyo(File file)
	{
		println("(UNYO) executing...");

		List<String> args = new ArrayList<String>();
		args.addAll(Arrays.asList(Env.get("UNYO_OPTION").split("\\s+")));
		args.add(file.getAbsolutePath());

		final ProcessTask unyoTask = ProcessTask.createJarProcessTask("unyo.jar", args);
		unyoTask.setDirectory(Env.LMNTAL_LIBRARY_DIR + File.separator + Env.getDirNameOfUnyo());
		unyoTask.addProcessFinishListener(new ProcessFinishListener()
		{
			public void processFinished(int id, int exitCode, boolean isAborted)
			{
				printTerminationMessage("UNYO", id, unyoTask.getElapsedSeconds(), exitCode, isAborted);
			}
		});

		if (unyoTask.execute())
		{
			addProcessTask(unyoTask);
		}
	}

       private static Properties createProperties(String properties_path) {
	        Properties properties = new Properties();
		try {
		    InputStream is = new FileInputStream(properties_path);
		    properties.load(is);
		} catch (IOException e) {

		}
		return properties;
        }

	public static void executeGraphene(File file)
	{
		println("(Graphene) executing...");

		String properties_path = "lmntal" + File.separator + Env.getDirNameOfGraphene() + File.separator + "LMNtal.properties";
		Properties properties = createProperties(properties_path);
		properties.setProperty("additional_options", Env.get("SLIM_OPTION"));
		properties.setProperty("slim_path", Env.get("path.slim.exe"));
		properties.setProperty("lmntal_home", System.getenv("LMNTAL_HOME"));
		try {
		    FileOutputStream fos = new FileOutputStream(properties_path);
		    properties.store(fos, "from lavit");
		    fos.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		final List<String> args = Arrays.asList("--lmntal.file", file.getAbsolutePath());
		final ProcessTask grapheneTask = ProcessTask.createJarProcessTask("graphene.jar", args);
		grapheneTask.setDirectory(Env.LMNTAL_LIBRARY_DIR + File.separator + Env.getDirNameOfGraphene());
		grapheneTask.addProcessFinishListener(new ProcessFinishListener()
		{
			public void processFinished(int id, int exitCode, boolean isAborted)
			{
				printTerminationMessage("Graphene", id, grapheneTask.getElapsedSeconds(), exitCode, isAborted);
			}
		});

		if (grapheneTask.execute())
		{
			addProcessTask(grapheneTask);
		}
	}

	public static void executeILCodeInSLIM()
	{
		File file = mainFrame.editorPanel.getFile();
		String code = mainFrame.editorPanel.getSelectedEditor().getText();

		println("(SLIM) executing...");
		mainFrame.toolTab.systemPanel.outputPanel.outputStart("slim", Env.get("SLIM_OPTION"), file);

		List<String> args = new ArrayList<String>();
		args.addAll(Arrays.asList(Env.get("SLIM_OPTION").split("\\s+")));
		args.add("-");

		final ProcessTask slimTask = ProcessTask.createProcessTask(Env.get("SLIM_EXE_PATH"), args);
		slimTask.setDirectory(".");
		slimTask.setStandardInputData(code);

		slimTask.setStandardOutputListener(new PrintLineListener()
		{
			public void println(String line)
			{
				mainFrame.toolTab.systemPanel.outputPanel.println(line);
			}
		});

		slimTask.setStandardErrorListener(new PrintLineListener()
		{
			public void println(String line)
			{
				mainFrame.toolTab.systemPanel.outputPanel.errPrintln(line);
			}
		});

		slimTask.addProcessFinishListener(new ProcessFinishListener()
		{
			public void processFinished(int id, int exitCode, boolean isAborted)
			{
				printTerminationMessage("SLIM", id, slimTask.getElapsedSeconds(), exitCode, isAborted);
				mainFrame.toolTab.systemPanel.outputPanel.outputEnd();
			}
		});

		if (slimTask.execute())
		{
			addProcessTask(slimTask);
		}
	}

	/**
	 * 中間命令列ファイルをSLIMで実行
	 */
	public static void executeILFileInSLIM(File file)
	{
		println("(SLIM) executing...");
		mainFrame.toolTab.setTab("System");
		mainFrame.toolTab.systemPanel.outputPanel.outputStart("slim", Env.get("SLIM_OPTION"), file);

		List<String> args = new ArrayList<String>();
		args.addAll(StringUtils.splitToSet(Env.get("SLIM_OPTION"), "\\s+"));
		args.add(Env.getSpaceEscape(file.getAbsolutePath()));

		final ProcessTask slimTask = ProcessTask.createProcessTask(Env.get("SLIM_EXE_PATH"), args);
		slimTask.setDirectory(".");
		slimTask.setStandardInputData(Env.get("slim.stdin.str", ""));

		slimTask.setStandardOutputListener(new PrintLineListener()
		{
			public void println(String line)
			{
				mainFrame.toolTab.systemPanel.outputPanel.println(line);
			}
		});

		slimTask.setStandardErrorListener(new PrintLineListener()
		{
			public void println(String line)
			{
				mainFrame.toolTab.systemPanel.outputPanel.errPrintln(line);
			}
		});

		slimTask.addProcessFinishListener(new ProcessFinishListener()
		{
			public void processFinished(int id, int exitCode, boolean isAborted)
			{
				printTerminationMessage("SLIM", id, slimTask.getElapsedSeconds(), exitCode, isAborted);
				mainFrame.toolTab.systemPanel.outputPanel.outputEnd();
			}
		});

		if (slimTask.execute())
		{
			addProcessTask(slimTask);
		}
	}

	public static void addProcessTask(ProcessTask task)
	{
		cleanProcessTasks();
		synchronized (processTasks)
		{
			processTasks.add(task);
		}
		mainFrame.editorPanel.buttonPanel.killButton.setEnabled(true);
	}

	public static void detachProcessTask(ProcessTask task)
	{
		synchronized (processTasks)
		{
			processTasks.remove(task);
		}
	}

	public static void abortAllProcessTasks()
	{
		synchronized (processTasks)
		{
			for (ProcessTask task : processTasks)
			{
				if (!task.isTerminated())
				{
					task.abort();
				}
			}
			processTasks.clear();
		}
	}

	private static void cleanProcessTasks()
	{
		synchronized (processTasks)
		{
			Iterator<ProcessTask> it = processTasks.iterator();
			while (it.hasNext())
			{
				ProcessTask task = it.next();
				if (task.isTerminated())
				{
					it.remove();
				}
			}
		}
	}

	public static void reboot()
	{
		if (!mainFrame.editorPanel.askSaveAllChangedFiles())
		{
			return;
		}
		Env.saveOpenedFilePathes(mainFrame.editorPanel.getFiles());
		mainFrame.exit();
		Env.save();
		mainFrame.dispose();
		System.out.println("LaViT reboot.");

		RebootRunner rebootRunner = new RebootRunner("-Xms16M -Xmx" + Env.get("REBOOT_MAX_MEMORY"));
		rebootRunner.run();
		while (rebootRunner.isRunning())
		{
			sleep(200);
		}
		System.exit(0);
	}

	public static void exit()
	{
		if (!mainFrame.editorPanel.askSaveAllChangedFiles())
		{
			return;
		}
		Env.saveOpenedFilePathes(mainFrame.editorPanel.getFiles());
		mainFrame.exit();
		if (Env.is("WATCH_DUMP")) StopWatch.dumpWatch();
		Env.save();
		System.out.println("LaViT end.");
		System.exit(0);
	}

	public static void println(final String str)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainFrame.toolTab.systemPanel.logPanel.println(str);
			}
		});
	}

	public static void errPrintln(final String str)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainFrame.toolTab.systemPanel.logPanel.errPrintln(str);
			}
		});
	}

	public static void printException(final Exception e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainFrame.toolTab.systemPanel.logPanel.printException(e);
			}
		});
		e.printStackTrace();
	}

	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			printException(e);
		}
	}

	public static void setLookAndFeel(LookAndFeelEntry lafEntry)
	{
		Env.set("LookAndFeel", lafEntry.getName());

		final String className = lafEntry.getClassName();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(className);
					for (Window window : Window.getWindows())
					{
						SwingUtilities.updateComponentTreeUI(window);
					}
				}
				catch (Exception e)
				{
					printException(e);
				}
			}
		});
	}

	public static void addFontUser(CommonFontUser user)
	{
		fontUsers.add(user);
	}

	public static void removeFontUser(CommonFontUser user)
	{
		fontUsers.remove(user);
	}

	public static void loadAllFont()
	{
		for (CommonFontUser user : fontUsers)
		{
			user.loadFont();
		}
	}

	private static void printTerminationMessage(String label, int taskId, double seconds, int exitCode, boolean aborted)
	{
		String s = "($label) $text (Task[$id] $time [s])";
		s = s.replace("$label", label);
		s = s.replace("$id", String.valueOf(taskId));
		s = s.replace("$text", aborted ? "aborted" : "terminated");
		s = s.replace("$time", String.format("%.02f", seconds));

		if (aborted)
		{
			errPrintln(s);
		}
		else
		{
			println(s);
		}
		if (label.equals("Graphene") && exitCode == 3) {
		    errPrintln("SLIM NOT FOUND!");
		}
		if (exitCode != 0)
		{
			errPrintln("ExitCode = " + exitCode);
		}
	}

	private static void initialSetup()
	{
		if (!Env.isSet("LANG"))
		{
			if (!LanguageSetting.showDialog())
			{
				System.exit(0);
			}
		}
		Env.loadMsg();

		if (Env.isWindows() && !Env.isSet("WINDOWS_CYGWIN_DIR"))
		{
			CygwinPathSetting.showDialog();
		}

		File lmntal = new File(Env.LMNTAL_LIBRARY_DIR+File.separator+"bin"+File.separator+"lmntal");
		if (lmntal.exists() && !lmntal.canExecute())
		{
			lmntal.setExecutable(true);
		}

		if (StringUtils.nullOrEmpty(Env.get("SLIM_EXE_PATH")))
		{
			SlimPathSetting setting = new SlimPathSetting();
			if (setting.showDialog())
			{
				setting.waitFor();
			}
		}
	}

	private static void buildAndShowGUI(String[] args)
	{
		try
		{
			mainFrame = new MainFrame();
			mainFrame.setVisible(true);
			mainFrame.editorPanel.openInitialFiles();

			loadArgs(args); //起動オプションの読み込み

			println("(SYSTEM) Ready.");
		}
		catch (Exception e)
		{
			printException(e);
			e.printStackTrace();
		}

		if (Env.get("updatecheck.enabled", "false").equals("true"))
		{
			final String url = Env.get("updatecheck.url", "");
			if (!url.isEmpty())
			{
				Thread updateCheckThread = new Thread()
				{
					public void run()
					{
						UpdateChecker.checkVersion(mainFrame, Env.APP_VERSION, Env.APP_DATE, url);
					}
				};
				updateCheckThread.setDaemon(true);
				updateCheckThread.start();
			}
		}
	}

	/**
	 * The entry point of LaViT.
	 * @param args Command line arguments.
	 */
	public static void main(final String[] args)
	{
		LaViTSplashWindow.showSplash(2000);

		if (!Env.loadEnvironment())
		{
			System.err.println("Error: failed to load environment");
			return;
		}

		setLookAndFeel(LookAndFeelEntry.getLookAndFeelEntry(Env.get("LookAndFeel")));

		initialSetup();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				buildAndShowGUI(args);
			}
		});
	}
}
