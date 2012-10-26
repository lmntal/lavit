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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lavit.frame.CygwinPathSetting;
import lavit.frame.LanguageSetting;
import lavit.frame.LookAndFeelEntry;
import lavit.frame.MainFrame;
import lavit.frame.SlimPathSetting;
import lavit.frame.StartupFrame;
import lavit.runner.ProcessFinishListener;
import lavit.runner.ProcessTask;
import lavit.runner.RebootRunner;
import lavit.util.CommonFontUser;
import lavit.util.StringUtils;

public class FrontEnd
{
	public static MainFrame mainFrame;

	public static Set<CommonFontUser> fontUsers = new HashSet<CommonFontUser>();

	private static List<ProcessTask> processTasks = new ArrayList<ProcessTask>();

	public FrontEnd(String[] args)
	{
		mainFrame   = new MainFrame();
        mainFrame.editorPanel.openInitialFiles();

        sleep(500);

        loadArgs(args); //起動オプションの読み込み

        println("(SYSTEM) Ready.");
	}

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
		FrontEnd.println("(UNYO) executing...");

		List<String> args = new ArrayList<String>();
		args.addAll(Arrays.asList(Env.get("UNYO_OPTION").split("\\s+")));
		args.add(file.getAbsolutePath());
		ProcessTask unyoTask = ProcessTask.createJarProcessTask("unyo.jar", args);
		unyoTask.setDirectory(Env.LMNTAL_LIBRARY_DIR + File.separator + Env.getDirNameOfUnyo());
		unyoTask.addProcessFinishListener(new ProcessFinishListener()
		{
			public void processFinished(int id, int exitCode, boolean isAborted)
			{
				if (isAborted)
				{
					FrontEnd.println(String.format("(UNYO) terminated. (Task[%d])", id));
				}
				else
				{
					FrontEnd.errPrintln(String.format("(UNYO) aborted. (Task[%d])", id));
				}
			}
		});
		if (unyoTask.execute())
		{
			FrontEnd.addProcessTask(unyoTask);
		}
	}

	public static void addProcessTask(final ProcessTask task)
	{
		task.addProcessFinishListener(new ProcessFinishListener()
		{
			public void processFinished(int id, int exitCode, boolean isAborted)
			{
				if (!isAborted)
				{
					cleanProcessTasks();
				}
			}
		});
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
			FrontEnd.sleep(200);
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
		if (Env.is("WATCH_DUMP")) Env.dumpWatch();
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
			FrontEnd.printException(e);
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
					FrontEnd.printException(e);
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

	private static void initialSetup()
	{
		if (!Env.isSet("LANG"))
		{
			Lang.set("en");
			if (!LanguageSetting.showDialog())
			{
				System.exit(0);
			}
		}
		Lang.set(Env.get("LANG"));

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

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		new Env();

		setLookAndFeel(LookAndFeelEntry.getLookAndFeelEntry(Env.get("LookAndFeel")));

		final StartupFrame sf = new StartupFrame();
		sf.setLocationRelativeTo(null);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				sf.setVisible(true);
			}
		});

		initialSetup();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					new FrontEnd(args);
					sf.setVisible(false);
				}
				catch (Exception e)
				{
					FrontEnd.printException(e);
					e.printStackTrace();
				}
			}
		});
	}
}
