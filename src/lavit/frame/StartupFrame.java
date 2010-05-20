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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.runner.Ltl2baInstaller;
import lavit.runner.SlimInstaller;

public class StartupFrame extends JFrame {

	public StartupFrame(){

		ImageIcon image = new ImageIcon(Env.getImageOfFile("img/logo.png"));

		//setSize(image.getIconWidth()+30, image.getIconHeight()+30);
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    setUndecorated(true);
	    setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));

	    JPanel panel = new JPanel();
		//panel.setBorder(new LineBorder(new Color(200,200,200), 2, true));
		panel.setBackground(new Color(255,255,255));
		panel.setLayout(new BorderLayout());
		add(panel);

		JLabel icon = new JLabel(image);
		panel.add(icon,BorderLayout.CENTER);

		JTextField text = new JTextField("Version "+Env.APP_VERSION);
		text.setHorizontalAlignment(JTextField.CENTER);
		text.setBackground(new Color(255,255,255));
		text.setEditable(false);
	    text.setBorder(null);
	    panel.add(text,BorderLayout.SOUTH);

	    pack();
	    setLocationRelativeTo(null);
	    setVisible(true);

	}

	LangSettingFrame langFrame;
	SlimPathSettingFrame slimFrame;
	CygwinPathSettingFrame cygwinFrame;

	public void startEnvSet(){

		if((Env.get("LANG")==null||Env.get("LANG").equals(""))){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
				langFrame = new LangSettingFrame();
			}});
			while(langFrame==null||langFrame.isDisplayable()){
				FrontEnd.sleep(200);
			}
		}

		Lang.set(Env.get("LANG"));

		if(Env.isWindows()&&!Env.isSet("WINDOWS_CYGWIN_DIR")){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
				cygwinFrame = new CygwinPathSettingFrame();
			}});
			while(cygwinFrame==null||cygwinFrame.isDisplayable()){
				FrontEnd.sleep(200);
			}
		}

		File lmntal = new File(Env.LMNTAL_LIBRARY_DIR+File.separator+"bin"+File.separator+"lmntal");
		if(lmntal.exists()&&!lmntal.canExecute()){
			lmntal.setExecutable(true);
		}

		if(Env.get("SLIM_EXE_PATH")==null||Env.get("SLIM_EXE_PATH").equals("")){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
				slimFrame = new SlimPathSettingFrame();
			}});
			while(slimFrame==null||!slimFrame.isEnd()){
				FrontEnd.sleep(200);
			}
		}
	}

}