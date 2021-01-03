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

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.EditorPanel;
import lavit.event.TabChangeListener;
import lavit.runner.ILRunner;
import lavit.runner.PrintLineListener;
import lavit.util.IntUtils;
import extgui.flatsplitpane.FlatSplitPane;

@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	public EditorPanel editorPanel;
	public ToolTab toolTab;

	private JSplitPane editorSplit;

	private Set<Window> childFrames = new HashSet<Window>();
	private Dimension sizeSave;
	private Point locationSave;

	private ILRunner ilRunner;

	public MainFrame()
	{
		int uiFontSizeDiff = Env.getInt("UI_FONT_SIZE_DIFF", 0);
		for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet())
		{
			String key = entry.getKey().toString();
			if (key.toLowerCase().endsWith("font"))
			{
				Font font = UIManager.getFont(key);
				float size = font.getSize() + uiFontSizeDiff;
				if (size > 0)
				{
					UIManager.put(key, new FontUIResource(font.deriveFont(size)));
				}
			}
		}

		sizeSave = new Dimension(Env.getInt("WINDOW_WIDTH"), Env.getInt("WINDOW_HEIGHT"));
		setSize(sizeSave);

		locationSave = new Point(Env.getInt("WINDOW_X"), Env.getInt("WINDOW_Y"));
		setLocation(locationSave);

		String state = Env.get("WINDOW_STATE");
		if (state != null && state.equalsIgnoreCase("maximized"))
		{
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				if (getExtendedState() == JFrame.NORMAL)
				{
					sizeSave = getSize();
				}
			}

			public void componentMoved(ComponentEvent e)
			{
				if (getExtendedState() == JFrame.NORMAL)
				{
					if (getX() >= 0 && getY() >= 0)
					{
						locationSave = getLocation();
					}
				}
			}
		});

		setTitle(Env.APP_NAME);
		setIconImages(Env.getApplicationIcons());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setJMenuBar(new MainMenuBar());

		editorPanel = new EditorPanel();
		editorPanel.setFileViewVisible(Env.is("window.fileview.visible"));
		editorPanel.setFileViewExtensionFilterText(Env.get("window.fileview.filter", ""));
		editorPanel.addTabChangeListener(new TabChangeListener()
		{
			public void tabChanged()
			{
				if (editorPanel.getTabCount() > 0)
				{
					loadLTLFile(editorPanel.getFile());
				}
				else
				{
					unloadLTLFile();
				}
			}
		});

		toolTab = new ToolTab();

		final int fileViewDividerLocation = Env.getInt("window.fileview.divider", 25);
		final double editorDividerRatio = IntUtils.clamp(Env.getInt("window.divider_location", 50), 0, 100) / 100.0;
		editorSplit = new FlatSplitPane();
		editorSplit.setLeftComponent(editorPanel);
		editorSplit.setRightComponent(toolTab);
		editorSplit.setOneTouchExpandable(true);
		editorSplit.setResizeWeight(0.5);
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						editorPanel.setFileViewDividerLocation(fileViewDividerLocation);
						editorSplit.setDividerLocation((int)Math.round(getWidth() * editorDividerRatio));
					}
				});
			}
		});
		setContentPane(editorSplit);

		addWindowListener(new MainWindowListener(this));

		// command-Qなどでアプリケーションを閉じるときもenv.txtを保存するようhook登録
		Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					exit();
				}
		});
	}

	public void addChildWindow(Window window)
	{
		childFrames.add(window);
	}

	public void removeChildWindow(Window window)
	{
		childFrames.remove(window);
	}

	public void setAllChildWindowVisible(boolean visible)
	{
		for (Window w : childFrames)
		{
			w.setVisible(visible);
		}
	}

	public void setFileViewVisible(boolean b)
	{
		editorPanel.setFileViewVisible(b);
	}

	public boolean isFileViewVisible()
	{
		return editorPanel.isFileViewVisible();
	}

	public void dispose()
	{
		for (Window w : childFrames)
		{
			w.dispose();
		}
		super.dispose();
	}

	public void loadTemplate()
	{
		TemplateSelectDialog dialog = TemplateSelectDialog.create(this);
		dialog.setVisible(true);
		if (dialog.isAccepted())
		{
			String contents = dialog.getTemplateContents();
			JTextPane textPane = editorPanel.getSelectedEditor();
			try
			{
				textPane.getDocument().insertString(textPane.getCaretPosition(), contents, null);
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void runILCodeOnLMNtalJava()
	{
		if (ilRunner != null) return;

		if (editorPanel.isChanged())
		{
			editorPanel.fileSave();
		}
		toolTab.systemPanel.outputPanel.outputStart("lmntal --stdin-tal", Env.get("LMNTAL_OPTION"), editorPanel.getFile());
		String s = editorPanel.getSelectedEditor().getText();
		ILRunner runner = new ILRunner(Env.get("LMNTAL_OPTION"));
		runner.setStdoutListener(new PrintLineListener()
		{
			@Override
			public void println(String line)
			{
				toolTab.systemPanel.outputPanel.println(line);
			}
		});
		runner.setStderrListener(new PrintLineListener()
		{
			@Override
			public void println(String line)
			{
				toolTab.systemPanel.outputPanel.errPrintln(line);
			}
		});
		runner.exec(s);
	}

	public void runILCodeOnSLIM()
	{
		if (editorPanel.isChanged())
		{
			editorPanel.fileSave();
		}
		toolTab.setTab("System");
		FrontEnd.executeILCodeInSLIM();
	}

	public void killILRunner()
	{
		if (ilRunner != null)
		{
			ilRunner.kill();
		}
	}

	public void exit()
	{
		Env.set("WINDOW_X", locationSave.x);
		Env.set("WINDOW_Y", locationSave.y);
		Env.set("WINDOW_WIDTH", sizeSave.width);
		Env.set("WINDOW_HEIGHT", sizeSave.height);
		Env.set("WINDOW_STATE", getExtendedState() == JFrame.MAXIMIZED_BOTH ? "maximized" : "normal");
		Env.set("window.divider_location", (int)Math.round(100.0 * editorSplit.getDividerLocation() / getWidth()));
		Env.set("window.fileview.visible", editorPanel.isFileViewVisible());
		Env.set("window.fileview.divider", editorPanel.getFileViewDividerLocation());
		Env.set("window.fileview.filter", editorPanel.getFileViewExtensionFilterText());
		Env.save();
	}

	private void loadLTLFile(File lmntalFile)
	{
		toolTab.ltlPanel.setTargetLMNtalFile(lmntalFile);
		toolTab.ltlPanel.setSelectedSuffix("0");
		toolTab.ltlPanel.loadFiles();
	}

	private void unloadLTLFile()
	{
		toolTab.ltlPanel.unloadFiles();
	}
}
