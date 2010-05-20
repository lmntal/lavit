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

import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.EditorPanel;
import lavit.util.CommonFontUser;

public class MainFrame extends JFrame{

	public HashSet<Window> childFrames;

	public MainMenuBar mainMenuBar;
	public EditorPanel editorPanel;
	public JSplitPane jsp;
	public ToolTab toolTab;

	public MainFrame(){

		childFrames = new HashSet<Window>();

		setSize(Env.getInt("WINDOW_WIDTH"),Env.getInt("WINDOW_HEIGHT"));
        setLocation(new Point(Env.getInt("WINDOW_X"),Env.getInt("WINDOW_Y")));
        setTitle(Env.APP_NAME);
        setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        mainMenuBar = new MainMenuBar();
		setJMenuBar(mainMenuBar);

		editorPanel = new EditorPanel();

		toolTab = new ToolTab();

		double editerPer = Env.getPercentage("WINDOW_EDITER_PERCENTAGE",50);
		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, toolTab);
		jsp.setOneTouchExpandable(true);
        jsp.setResizeWeight(0.5);
        jsp.setDividerLocation((int)(Env.getInt("WINDOW_WIDTH")*editerPer));
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

	public void exit(){
		Env.set("WINDOW_X", getLocation().x);
		Env.set("WINDOW_Y", getLocation().y);
		Env.set("WINDOW_WIDTH", getWidth());
		Env.set("WINDOW_HEIGHT", getHeight());
		Env.setPercentage("WINDOW_EDITER_PERCENTAGE",(double)jsp.getDividerLocation()/(double)getWidth());
	}

}
