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
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.EditorPanel;
import lavit.runner.ILRunner;
import lavit.runner.PrintLineListener;
import lavit.ui.FlatSplitPaneUI;

@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	
	public HashSet<Window> childFrames;

	public MainMenuBar mainMenuBar;
	public EditorPanel editorPanel;
	public JSplitPane jsp;
	public ToolTab toolTab;
	
	private Dimension sizeSave;
	private Point locationSave;

	private ILRunner ilRunner;

	public MainFrame()
	{
		childFrames = new HashSet<Window>();
		
		sizeSave = new Dimension(Env.getInt("WINDOW_WIDTH"), Env.getInt("WINDOW_HEIGHT"));
		setSize(sizeSave);
		
		locationSave = new Point(Env.getInt("WINDOW_X"),Env.getInt("WINDOW_Y"));
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
					if (getX() > 0 && getY() > 0)
					{
						locationSave = getLocation();
					}
				}
			}
		});
		
        setTitle(Env.APP_NAME);
        setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        mainMenuBar = new MainMenuBar();
		setJMenuBar(mainMenuBar);

		editorPanel = new EditorPanel();

		toolTab = new ToolTab();

		double editerPer = Env.getPercentage("WINDOW_EDITER_PERCENTAGE",50);
		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, toolTab)
		{
			public void setBorder(Border b)
			{
			}
			public void updateUI()
			{
			}
		};
		jsp.setUI(new FlatSplitPaneUI());
		jsp.setOneTouchExpandable(true);
        jsp.setResizeWeight(0.5);
        //jsp.setDividerLocation((int)(Env.getInt("WINDOW_WIDTH")*editerPer));
        jsp.setDividerLocation((int)(getWidth()*editerPer));
        setContentPane(jsp);

        addWindowListener(new MainWindowListener(this));

        setVisible(true);

	}

	public void addChildWindow(Window window){
		childFrames.add(window);
	}

	public void removeChildWindow(Window window){
		childFrames.remove(window);
	}

	public void setAllChildWindowVisible(boolean visible){
		for(Window w : childFrames){
			w.setVisible(visible);
		}
	}

	public void dispose(){
		for(Window w : childFrames){
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

	public void runAsILCode()
	{
		if (ilRunner != null) return;
		
		if (editorPanel.isChanged())
		{
			editorPanel.fileSave();
		}
		FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.outputStart("lmntal --stdin-tal", Env.get("LMNTAL_OPTION"), editorPanel.getFile());
		String s = editorPanel.getSelectedEditor().getText();
		ILRunner runner = new ILRunner(Env.get("LMNTAL_OPTION"));
		runner.setStdoutListener(new PrintLineListener()
		{
			@Override
			public void println(String line)
			{
				FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.println(line);
			}
		});
		runner.setStderrListener(new PrintLineListener()
		{
			@Override
			public void println(String line)
			{
				FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.errPrintln(line);
			}
		});
		runner.exec(s);
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
		Env.setPercentage("WINDOW_EDITER_PERCENTAGE",(double)jsp.getDividerLocation()/(double)getWidth());
	}
}
