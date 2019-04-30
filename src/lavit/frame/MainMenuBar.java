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

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import lavit.Env;
import lavit.FrontEnd;
import lavit.localizedtext.MsgID;
import lavit.system.FileHistory;

@SuppressWarnings("serial")
public class MainMenuBar extends JMenuBar implements ActionListener {
	private JMenu file;
	private JMenuItem iNew;
	private JMenuItem iOpen;
	private JMenuItem iSave;
	private JMenuItem iSaveAs;
	private JMenuItem iClose;
	private JMenuItem iCloseAll;
	private JMenuItem iSVOpen;
	private JMenu menuRecentFiles;
	private JMenuItem iExit;

	private JMenu edit;
	private JMenuItem iCopy;
	private JMenuItem iCut;
	private JMenuItem iPaste;
	public JMenuItem iUndo;
	public JMenuItem iRedo;

	private JMenu run;
	private JMenuItem iLMNtal;
	private JMenuItem iUNYO;
	private JMenuItem iGraphene;
	private JMenuItem iSLIM;
	private JMenuItem iILJavaRun;
	private JMenuItem iILSlimRun;
	private JMenuItem iSViewer;
	private JMenuItem iStateProfiler;
	private JMenuItem iKill;
	private JMenuItem iReboot;

	private JMenu setting;
	private JMenuItem iCygwinPath;
	private JMenuItem iLMNtalCompilerPath;
	private JMenuItem iSlimPath;
	private JMenuItem iGeneral;

	private JMenu help;
	private JMenuItem iVersion;
	private JMenuItem iRuntime;
	private JMenuItem iBrowse;

	public MainMenuBar() {
		file = new JMenu(Env.getMsg(MsgID.menu_file));
		add(file);
		file.setMnemonic(KeyEvent.VK_F);

		iNew = new JMenuItem(Env.getMsg(MsgID.menu_new));
		file.add(iNew);
		iNew.addActionListener(this);
		iNew.setMnemonic(KeyEvent.VK_N);

		iOpen = new JMenuItem(Env.getMsg(MsgID.menu_open));
		file.add(iOpen);
		iOpen.addActionListener(this);
		iOpen.setMnemonic(KeyEvent.VK_O);

		iSave = new JMenuItem(Env.getMsg(MsgID.menu_save));
		file.add(iSave);
		iSave.addActionListener(this);
		iSave.setMnemonic(KeyEvent.VK_S);

		iSaveAs = new JMenuItem(Env.getMsg(MsgID.menu_saveas));
		file.add(iSaveAs);
		iSaveAs.addActionListener(this);
		iSaveAs.setMnemonic(KeyEvent.VK_A);

		file.addSeparator();

		iClose = new JMenuItem(Env.getMsg(MsgID.menu_close));
		file.add(iClose);
		iClose.addActionListener(this);
		iClose.setMnemonic(KeyEvent.VK_C);

		iCloseAll = new JMenuItem(Env.getMsg(MsgID.menu_closeall));
		file.add(iCloseAll);
		iCloseAll.addActionListener(this);
		iCloseAll.setMnemonic(KeyEvent.VK_L);

		file.addSeparator();

		iSVOpen = new JMenuItem(Env.getMsg(MsgID.menu_open_sv_file));
		file.add(iSVOpen);
		iSVOpen.addActionListener(this);

		file.addSeparator();

		menuRecentFiles = createRecentFilesMenu(file);
		file.add(menuRecentFiles);

		file.addSeparator();

		iExit = new JMenuItem(Env.getMsg(MsgID.menu_exit));
		file.add(iExit);
		iExit.addActionListener(this);
		iExit.setMnemonic(KeyEvent.VK_X);

		edit = new JMenu(Env.getMsg(MsgID.menu_edit));
		add(edit);
		edit.setMnemonic(KeyEvent.VK_E);

		iUndo = new JMenuItem(Env.getMsg(MsgID.menu_undo));
		edit.add(iUndo);
		iUndo.setEnabled(false);
		iUndo.addActionListener(this);
		iUndo.setMnemonic(KeyEvent.VK_U);

		iRedo = new JMenuItem(Env.getMsg(MsgID.menu_redo));
		edit.add(iRedo);
		iRedo.setEnabled(false);
		iRedo.addActionListener(this);
		iRedo.setMnemonic(KeyEvent.VK_R);

		edit.addSeparator();

		iCopy = new JMenuItem(Env.getMsg(MsgID.menu_copy));
		edit.add(iCopy);
		iCopy.addActionListener(this);
		iCopy.setMnemonic(KeyEvent.VK_C);

		iCut = new JMenuItem(Env.getMsg(MsgID.menu_cut));
		edit.add(iCut);
		iCut.addActionListener(this);
		iCut.setMnemonic(KeyEvent.VK_T);

		iPaste = new JMenuItem(Env.getMsg(MsgID.menu_paste));
		edit.add(iPaste);
		iPaste.addActionListener(this);
		iPaste.setMnemonic(KeyEvent.VK_P);

		edit.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				iUndo.setEnabled(FrontEnd.mainFrame.editorPanel.canUndo());
				iRedo.setEnabled(FrontEnd.mainFrame.editorPanel.canRedo());
			}

			public void menuDeselected(MenuEvent e) {
			}

			public void menuCanceled(MenuEvent e) {
			}
		});

		edit.addSeparator();

		JMenuItem itemFind = new JMenuItem(Env.getMsg(MsgID.menu_find_replace));
		itemFind.setMnemonic(KeyEvent.VK_F);
		itemFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FindReplaceDialog.getDialog().showDialog();
			}
		});
		edit.add(itemFind);

		/*
		 * edit.addSeparator();
		 * 
		 * iTemplate = new JMenuItem("Insert template...");
		 * iTemplate.setMnemonic(KeyEvent.VK_T); iTemplate.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent e) {
		 * FrontEnd.mainFrame.loadTemplate(); } }); edit.add(iTemplate);
		 */

		JMenu menuView = new JMenu(Env.getMsg(MsgID.menu_group_view));
		add(menuView);
		final JCheckBoxMenuItem itemShowFileView = new JCheckBoxMenuItem(Env.getMsg(MsgID.menu_item_filesystem));
		itemShowFileView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrontEnd.mainFrame.setFileViewVisible(itemShowFileView.isSelected());
			}
		});
		menuView.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				itemShowFileView.setSelected(FrontEnd.mainFrame.isFileViewVisible());
			}

			public void menuDeselected(MenuEvent e) {
			}

			public void menuCanceled(MenuEvent e) {
			}
		});
		menuView.add(itemShowFileView);

		run = new JMenu(Env.getMsg(MsgID.menu_run));
		add(run);
		run.setMnemonic(KeyEvent.VK_R);

		iLMNtal = new JMenuItem(Env.getMsg(MsgID.button_lmntal_java));
		run.add(iLMNtal);
		iLMNtal.addActionListener(this);
		iLMNtal.setMnemonic(KeyEvent.VK_L);

		iUNYO = new JMenuItem(Env.getMsg(MsgID.button_unyo_3g));
		run.add(iUNYO);
		iUNYO.addActionListener(this);
		iUNYO.setMnemonic(KeyEvent.VK_U);

		iGraphene = new JMenuItem(Env.getMsg(MsgID.button_graphene));
		run.add(iGraphene);
		iGraphene.addActionListener(this);
		iGraphene.setMnemonic(KeyEvent.VK_G);

		iSLIM = new JMenuItem(Env.getMsg(MsgID.button_slim));
		run.add(iSLIM);
		iSLIM.addActionListener(this);
		iSLIM.setMnemonic(KeyEvent.VK_S);

		run.addSeparator();

		iILJavaRun = new JMenuItem(Env.getMsg(MsgID.menu_run_il_java));
		run.add(iILJavaRun);
		iILJavaRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrontEnd.mainFrame.runILCodeOnLMNtalJava();
			}
		});
		iILJavaRun.setMnemonic(KeyEvent.VK_I);

		iILSlimRun = new JMenuItem(Env.getMsg(MsgID.menu_item_run_slim));
		run.add(iILSlimRun);
		iILSlimRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FrontEnd.mainFrame.runILCodeOnSLIM();
			}
		});
		iILSlimRun.setMnemonic(KeyEvent.VK_M);

		run.addSeparator();

		iSViewer = new JMenuItem(Env.getMsg(MsgID.button_stateviewer));
		run.add(iSViewer);
		iSViewer.addActionListener(this);
		iSViewer.setMnemonic(KeyEvent.VK_V);

		iStateProfiler = new JMenuItem(Env.getMsg(MsgID.button_stateprofiler));
		run.add(iStateProfiler);
		iStateProfiler.addActionListener(this);
		iStateProfiler.setMnemonic(KeyEvent.VK_P);

		iKill = new JMenuItem(Env.getMsg(MsgID.button_kill));
		run.add(iKill);
		iKill.addActionListener(this);
		iKill.setMnemonic(KeyEvent.VK_K);

		run.addSeparator();

		iReboot = new JMenuItem(Env.getMsg(MsgID.menu_reboot));
		run.add(iReboot);
		iReboot.addActionListener(this);

		setting = new JMenu(Env.getMsg(MsgID.menu_setting));
		add(setting);
		setting.setMnemonic(KeyEvent.VK_S);

		iCygwinPath = new JMenuItem(Env.getMsg(MsgID.menu_cygwin_path));
		setting.add(iCygwinPath);
		iCygwinPath.addActionListener(this);

		iLMNtalCompilerPath = new JMenuItem("LMNtal Compiler Path...");
		setting.add(iLMNtalCompilerPath);
		iLMNtalCompilerPath.addActionListener(this);

		iSlimPath = new JMenuItem(Env.getMsg(MsgID.menu_slim_path));
		setting.add(iSlimPath);
		iSlimPath.addActionListener(this);

		iGeneral = new JMenuItem(Env.getMsg(MsgID.menu_general_setting));
		setting.add(iGeneral);
		iGeneral.addActionListener(this);

		help = new JMenu(Env.getMsg(MsgID.menu_help));
		add(help);
		help.setMnemonic(KeyEvent.VK_H);

		iVersion = new JMenuItem(Env.getMsg(MsgID.menu_version));
		help.add(iVersion);
		iVersion.addActionListener(this);

		iRuntime = new JMenuItem(Env.getMsg(MsgID.menu_jre_info));
		help.add(iRuntime);
		iRuntime.addActionListener(this);

		iBrowse = new JMenuItem(Env.getMsg(MsgID.menu_webpage));
		help.add(iBrowse);
		iBrowse.addActionListener(this);

		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		setAccelerator(iNew, KeyEvent.VK_N, mask);
		setAccelerator(iOpen, KeyEvent.VK_O, mask);
		setAccelerator(iSave, KeyEvent.VK_S, mask);
		setAccelerator(iClose, KeyEvent.VK_W, mask);
		setAccelerator(iUndo, KeyEvent.VK_Z, mask);
		setAccelerator(iRedo, KeyEvent.VK_Y, mask);
		setAccelerator(iCopy, KeyEvent.VK_C, mask);
		setAccelerator(iCut, KeyEvent.VK_X, mask);
		setAccelerator(iPaste, KeyEvent.VK_V, mask);
		setAccelerator(itemFind, KeyEvent.VK_F, mask);
		setAccelerator(iLMNtal, KeyEvent.VK_F1, 0);
		setAccelerator(iUNYO, KeyEvent.VK_F2, 0);
		setAccelerator(iGraphene, KeyEvent.VK_F3, 0);
		setAccelerator(iSLIM, KeyEvent.VK_F4, 0);
		setAccelerator(iSViewer, KeyEvent.VK_F5, 0);
		setAccelerator(iKill, KeyEvent.VK_ESCAPE, 0);
	}

	public void updateUndoRedo(boolean undo, boolean redo) {
		iUndo.setEnabled(undo);
		iRedo.setEnabled(redo);
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem src = (JMenuItem) e.getSource();

		if (src == iNew) {
			FrontEnd.mainFrame.editorPanel.newFileOpen();
		} else if (src == iOpen) {
			FrontEnd.mainFrame.editorPanel.fileOpen();
		} else if (src == iSave) {
			FrontEnd.mainFrame.editorPanel.fileSave();
		} else if (src == iSaveAs) {
			FrontEnd.mainFrame.editorPanel.fileSaveAs();
		} else if (src == iClose) {
			FrontEnd.mainFrame.editorPanel.closeSelectedPage();
		} else if (src == iCloseAll) {
			FrontEnd.mainFrame.editorPanel.closeAllPages();
		} else if (src == iSVOpen) {
			FrontEnd.mainFrame.toolTab.statePanel.loadFile();
		} else if (src == iExit) {
			FrontEnd.exit();
		} else if (src == iCopy) {
			FrontEnd.mainFrame.editorPanel.getSelectedEditor().copy();
		} else if (src == iCut) {
			FrontEnd.mainFrame.editorPanel.getSelectedEditor().cut();
		} else if (src == iPaste) {
			FrontEnd.mainFrame.editorPanel.getSelectedEditor().paste();
		} else if (src == iUndo) {
			FrontEnd.mainFrame.editorPanel.editorUndo();
		} else if (src == iRedo) {
			FrontEnd.mainFrame.editorPanel.editorRedo();
		} else if (src == iLMNtal) {
			FrontEnd.mainFrame.editorPanel.buttonPanel.lmntalButton.doClick();
		} else if (src == iUNYO) {
			FrontEnd.mainFrame.editorPanel.buttonPanel.unyoButton.doClick();
		} else if (src == iGraphene) {
			FrontEnd.mainFrame.editorPanel.buttonPanel.grapheneButton.doClick();
		} else if (src == iSLIM) {
			FrontEnd.mainFrame.editorPanel.buttonPanel.slimButton.doClick();
		} else if (src == iSViewer) {
			FrontEnd.mainFrame.editorPanel.buttonPanel.sviewerButton.doClick();
		} else if (src == iKill) {
			FrontEnd.mainFrame.editorPanel.buttonPanel.killButton.doClick();
		} else if (src == iReboot) {
			new RebootFrame();
		} else if (src == iCygwinPath) {
			CygwinPathSetting.showDialog();
		} else if (src == iLMNtalCompilerPath) {
			LMNtalCompilerPathSetting.showDialog(FrontEnd.mainFrame);
		} else if (src == iSlimPath) {
			SlimPathSetting slimPathSetting = new SlimPathSetting();
			slimPathSetting.showDialog();
		} else if (src == iGeneral) {
			GeneralSettingDialog.showDialog();
		} else if (src == iVersion) {
			VersionDialog.showDialog();
		} else if (src == iRuntime) {
			showRuntimeInformation();
		} else if (src == iBrowse) {
			showInDefaultBrowser(Env.APP_HREF);
		}
	}

	private static void setAccelerator(JMenuItem item, int keyCode, int modifier) {
		item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifier));
	}

	private static JMenu createRecentFilesMenu(JMenu owner) {
		final JMenuItem miClearRecentFiles = new JMenuItem(Env.getMsg(MsgID.menu_clear_recent));
		miClearRecentFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileHistory.get().clear();
			}
		});

		final JMenuItem miEmpty = new JMenuItem(Env.getMsg(MsgID.menu_recent_empty));
		miEmpty.setEnabled(false);

		final JMenu menu = new JMenu(Env.getMsg(MsgID.menu_recent));
		owner.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				menu.removeAll();
				List<File> recentFiles = FileHistory.get().getFiles();
				if (recentFiles.isEmpty()) {
					menu.add(miEmpty);
				} else {
					for (final File file : recentFiles) {
						JMenuItem item = new JMenuItem(trimFilePath(file));
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								FrontEnd.mainFrame.editorPanel.openFile(file);
							}
						});
						menu.add(item);
					}
				}
				menu.addSeparator();
				menu.add(miClearRecentFiles);
			}

			public void menuDeselected(MenuEvent e) {
			}

			public void menuCanceled(MenuEvent e) {
			}
		});
		return menu;
	}

	private static String trimFilePath(File file) {
		final int LIMIT = 20;
		final String ELLIPSIS = "...";

		String path = file.getAbsolutePath();
		int i = path.lastIndexOf(File.separatorChar);
		if (i != -1) {
			String head = path.substring(0, i);
			String name = path.substring(i);
			if (LIMIT < head.length()) {
				head = ELLIPSIS + head.substring(head.length() - LIMIT + ELLIPSIS.length(), head.length());
			}
			return head + name;
		} else {
			return file.getName();
		}
	}

	// TODO: ブラウザ起動のサポートは事前に調べ、サポートされない場合はメニューを表示しないように変更する。
	private static void showInDefaultBrowser(String uri) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(new URI(uri));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("showInDefaultBrowser failed: Desktop does not support \"browse\" action.");
			}
		} else {
			System.err.println("showInDefaultBrowser failed: Desktop is not supported.");
		}
	}

	private static void showRuntimeInformation() {
		Runtime rt = Runtime.getRuntime();
		final int maxMem = (int) (rt.maxMemory() / 1024 / 1024);
		final int useMem = (int) (rt.totalMemory() / 1024 / 1024);
		final int procs = rt.availableProcessors();
		final String javaVersion = System.getProperty("java.version");
		final String javaRuntimeVersion = System.getProperty("java.runtime.version");
		final String jvmVersion = System.getProperty("java.vm.version");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(FrontEnd.mainFrame,
						"Max Memory : " + maxMem + " MB\n" + "Use Memory : " + useMem + " MB\n" + "Available Processors : " + procs
								+ " \n" + "Java Version : " + javaVersion + " \n" + "Java Runtime Version : " + javaRuntimeVersion
								+ " \n" + "Java VM Version : " + jvmVersion + " \n",
						"Java Runtime Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
