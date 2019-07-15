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

package lavit.editor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lavit.Env;
import lavit.FrontEnd;
import lavit.localizedtext.MsgID;
import lavit.runner.*;
import lavit.util.FileUtils;
import lavit.util.StringUtils;

@SuppressWarnings("serial")
public class EditorButtonPanel extends JPanel implements ActionListener {
	private EditorPanel editorPanel;

	private LmntalRunner lmntalRunner;
	private SlimRunner slimRunner;

	private JPanel buttonPanel;
	public JButton lmntalButton;
	public JButton unyoButton;
	public JButton grapheneButton;
	public JButton stateProfilerButton;
	public JButton slimButton;
	public JButton sviewerButton;
	public JButton svporButton;
	public JButton nullButton;
	public JButton killButton;

	public EditorButtonPanel(EditorPanel editorPanel) {
		this.editorPanel = editorPanel;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// ボタン列
		buttonPanel = new JPanel(new GridLayout(2, 4));

		lmntalButton = new JButton("Compile");
		lmntalButton.addActionListener(this);
		buttonPanel.add(lmntalButton);

		unyoButton = new JButton(Env.getMsg(MsgID.button_unyo));
		unyoButton.addActionListener(this);
		buttonPanel.add(unyoButton);

		grapheneButton = new JButton(Env.getMsg(MsgID.button_graphene));
		grapheneButton.addActionListener(this);
		buttonPanel.add(grapheneButton);

		stateProfilerButton = new JButton(Env.getMsg(MsgID.button_stateprofiler));
		stateProfilerButton.addActionListener(this);
		buttonPanel.add(stateProfilerButton);

		slimButton = new JButton(Env.getMsg(MsgID.button_slim));
		slimButton.addActionListener(this);
		buttonPanel.add(slimButton);

		sviewerButton = new JButton(Env.getMsg(MsgID.button_stateviewer));
		sviewerButton.addActionListener(this);
		buttonPanel.add(sviewerButton);

		// svporButton = new JButton("(POR)"+Lang.m[15]);
		svporButton = new JButton("");
		svporButton.addActionListener(this);
		buttonPanel.add(svporButton);

		/*
		 * sviewerlButton = new JButton("(LTL)"+Lang.m[15]);
		 * sviewerlButton.addActionListener(this); buttonPanel.add(sviewerlButton);
		 */
		// nullButton = new JButton();
		// buttonPanel.add(nullButton);

		killButton = new JButton(Env.getMsg(MsgID.button_kill));
		killButton.addActionListener(this);
		buttonPanel.add(killButton);

		setAllEnable(true);
		add(buttonPanel);
	}

	public void setAllEnable(boolean enable) {
		// nullButton.setEnabled(enable);

		lmntalButton.setEnabled(enable);
		unyoButton.setEnabled(enable);
		grapheneButton.setEnabled(enable);
		slimButton.setEnabled(enable);
		sviewerButton.setEnabled(enable);
		svporButton.setEnabled(enable);
		stateProfilerButton.setEnabled(enable);

		killButton.setEnabled(!enable);
	}

	private void setButtonEnable(boolean enable) {
		setAllEnable(enable);
		FrontEnd.mainFrame.toolTab.ltlPanel.setButtonsEnabled(enable);
	}

	/**
	 * LMNtalソースコードをコンパイルし、中間命令列を開く
	 */
	private void compileLMNtal() {
		if (editorPanel.isChanged()) {
			if (!editorPanel.fileSave()) {
				return;
			}
		}

		File file = editorPanel.getFile();
		final File outputFile = createILCodeFile(file);
		try {
			compile(file, outputFile, new ProcessFinishListener() {
				public void processFinished(int id, int exitCode, boolean isAborted) {
					if (exitCode == 0) {
						if (outputFile.exists()) {
							editorPanel.openFile(outputFile);
						} else {
							logError("(compile[" + id + "]) output file does not exists.");
						}
					} else {
						logError("(compile[" + id + "]) failed.");
					}
					logInfo("compile finished. [" + id + "]");
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@code file}をコンパイルし、生成された中間命令列ファイルを開く
	 */
	private void compile(File file, File outputFile, ProcessFinishListener onFinished) throws FileNotFoundException {
		final PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));

		List<String> javaArgs = new ArrayList<String>();
		javaArgs.add("-DLMNTAL_HOME=lmntal");

		Set<String> options = StringUtils.splitToSet(Env.get("LMNTAL_OPTION"), "\\s+");
		options.add("--slimcode");
		List<String> args = new ArrayList<String>();
		args.addAll(options);
		args.add(Env.getSpaceEscape(file.getAbsolutePath()));

		final String sep = File.separator;
		ProcessTask task;
		String compiler_path = Env.get("path.lmntalcompiler");
		if (compiler_path.contains(".jar"))
		{
		    task = ProcessTask.createJarProcessTask(compiler_path, javaArgs, args);
		}
		else
		{
		    task = ProcessTask.createProcessTask(compiler_path, args);
		}
		Env.setProcessEnvironment(task.getEnvironment());

		FrontEnd.mainFrame.toolTab.setTab("System");
		FrontEnd.println("(compile[" + task.getTaskID() + "]) " + task.getCommand());

		task.setStandardErrorListener(new PrintLineListener() {
			public void println(String line) {
				FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.errPrintln(line);
			}
		});
		task.setStandardOutputListener(new PrintLineListener() {
			public void println(String line) {
				writer.println(line);
			}
		});
		task.addProcessFinishListener(new ProcessFinishListener() {
			public void processFinished(int id, int exitCode, boolean isAborted) {
				writer.close();
			}
		});
		task.addProcessFinishListener(onFinished);
		task.execute();
	}

	/**
	 * LMNtalソースコードのファイル名から中間命令列のファイル名を生成する。 典型的には、拡張子を.ilにしたものを返す。
	 */
	private File createILCodeFile(File lmntalFile) {
		String dir = lmntalFile.getParent();
		String name = FileUtils.removeExtension(lmntalFile.getName()) + ".il";
		return new File(dir + File.separator + name);
	}

	private void runSlim(SlimCallback callbackOnProcessFinish) {
		if (editorPanel.isChanged()) {
			if (!editorPanel.fileSave()) {
				return;
			}
		}

		File file = editorPanel.getFile();
		final File outputFile = createILCodeFile(file);

		try {
			compile(file, outputFile, new ProcessFinishListener() {
				public void processFinished(int id, int exitCode, boolean isAborted) {
					if (exitCode == 0) {
						if (outputFile.exists()) {
							FrontEnd.executeILFileInSLIM(outputFile, callbackOnProcessFinish);
						} else {
							logError("(compile[" + id + "]) output file does not exists.");
							setButtonEnable(true);
						}
					} else {
						logError("(compile[" + id + "]) failed.");
						setButtonEnable(true);
					}
					logInfo("compile finished. [" + id + "]");
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (editorPanel.isChanged()) {
			if (!editorPanel.fileSave()) {
				return;
			}
		}
		if (src == lmntalButton) {
			File file = editorPanel.getFile();
			if (file != null && file.getName().endsWith(".lmn")) {
				compileLMNtal();
			}
		} else if (src == unyoButton) {
			FrontEnd.mainFrame.toolTab.setTab("System");
			FrontEnd.executeUnyo(FrontEnd.mainFrame.editorPanel.getFile());
		} else if (src == grapheneButton) {
			FrontEnd.mainFrame.toolTab.setTab("System");
			FrontEnd.executeGraphene(FrontEnd.mainFrame.editorPanel.getFile());
		} else if (src == slimButton) {
			setButtonEnable(false);
			SlimCallback callback = new SlimCallback() {
				@Override
				public void apply() {
					setButtonEnable(true);
				}
			};

			File file = editorPanel.getFile();
			if (file.getName().endsWith(".il")) {
				FrontEnd.executeILFileInSLIM(file, callback);
			} else {
				runSlim(callback);
			}
		} else if (src == sviewerButton) {
			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(StateViewer) Doing...");
			String opt = "";
			if (Env.is("SLIM2")) {
				opt = "--nd -t --dump-lavit " + Env.get("SV_OPTION");
			} else {
				opt = "--nd " + Env.get("SV_OPTION");
				if (!Env.get("SV_DEPTH_LIMIT").equals("unset")) {
					opt += " --bfs_depth " + Env.get("SV_DEPTH_LIMIT");
				}
			}
			boolean runOnlySlim = editorPanel.getFileName().endsWith(".il");
			slimRunner = new SlimRunner(opt, runOnlySlim);
			slimRunner.setBuffering(true);
			slimRunner.run();
			new Thread() {
				public void run() {
					while (slimRunner.isRunning()) {
						FrontEnd.sleep(200);
					}
					FrontEnd.println("(SLIM) Done! [" + (slimRunner.getTime() / 1000.0) + "s]");
					if (slimRunner.isSucceeded()) {
						FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString(), false);
					}
					slimRunner = null;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setButtonEnable(true);
						}
					});
				}
			}.start();
		} else if (src == svporButton) {
			/*
			 * if(editorPanel.isChanged()){ editorPanel.fileSave(); }
			 * 
			 * setButtonEnable(false);
			 * 
			 * FrontEnd.mainFrame.toolTab.setTab("System");
			 * 
			 * FrontEnd.println("(StateViewer) Doing..."); slimRunner = new
			 * SlimRunner("--por "+Env.get("SV_OPTION")); slimRunner.setBuffering(true);
			 * slimRunner.run(); (new Thread(new Runnable() { public void run() {
			 * while(slimRunner.isRunning()){ FrontEnd.sleep(200); }
			 * if(slimRunner.isSuccess()){
			 * FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString(),
			 * false); } slimRunner = null; javax.swing.SwingUtilities.invokeLater(new
			 * Runnable(){public void run() { setButtonEnable(true); }}); }})).start();
			 */

			// FrontEnd.println(SlimRunner.checkRun()?"ok":"ng");
			// FrontEnd.reboot();


			/*
			 * }else if (src == sviewerlButton) {
			 * 
			 * if(editorPanel.isChanged()){ editorPanel.fileSave(); }
			 * 
			 * setButtonEnable(false);
			 * 
			 * FrontEnd.mainFrame.toolTab.setTab("System");
			 * 
			 * FrontEnd.println("(StateViewer) Doing..."); slimRunner = new
			 * SlimRunner("--ltl_nd --hideruleset"); slimRunner.setBuffering(true);
			 * slimRunner.run(); (new Thread(new Runnable() { public void run() {
			 * while(slimRunner.isRunning()){ FrontEnd.sleep(200); }
			 * if(slimRunner.isSuccess()){
			 * FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString()); }
			 * slimRunner = null; javax.swing.SwingUtilities.invokeLater(new
			 * Runnable(){public void run() { setButtonEnable(true); }}); }})).start();
			 */

		} else if (src == stateProfilerButton) {
			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("StateProfiler");

			FrontEnd.println("(StateProfiler) Doing...");
			if (Env.is("SLIM2")) {
				slimRunner = new SlimRunner("--nd --dump-inc --dump-lavit");
			} else {
				slimRunner = new SlimRunner("--nd_dump --hideruleset");
			}
			slimRunner.setOutputGetter(FrontEnd.mainFrame.toolTab.stateProfilePanel);

			slimRunner.run();
			new Thread() {
				public void run() {
					while (slimRunner.isRunning()) {
						FrontEnd.sleep(200);
					}
					FrontEnd.println("(StateProfiler) Done!");
					slimRunner = null;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setButtonEnable(true);
						}
					});
				}
			}.start();
		} else if (src == killButton) {
			if (lmntalRunner != null)
				lmntalRunner.kill();
			if (slimRunner != null)
				slimRunner.kill();
			FrontEnd.mainFrame.killILRunner();
			FrontEnd.mainFrame.toolTab.ltlPanel.killLtlSlimRunner();
			FrontEnd.abortAllProcessTasks();
			FrontEnd.errPrintln("Kill");
			setButtonEnable(true);
		}
	}

	private static void logInfo(String message) {
		FrontEnd.println(message);
	}

	private static void logError(String message) {
		FrontEnd.errPrintln(message);
	}
}
