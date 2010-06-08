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

import java.io.File;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lavit.frame.*;
import lavit.runner.RebootRunner;
import lavit.util.CommonFontUser;

public class FrontEnd {

	static public FrontEnd frontEnd;
	static public MainFrame mainFrame;
	static public StartupFrame sf;

	static public HashSet<CommonFontUser> fontUsers = new HashSet<CommonFontUser>();

	public FrontEnd(String[] args){

		frontEnd    = this;

		mainFrame   = new MainFrame();
        mainFrame.editorPanel.firstFileOpen();

        sleep(500);

        loadArgs(args); //起動オプションの読み込み

        println("(SYSTEM) Ready.");
	}

	void loadArgs(String[] args){
		for(int i=0;i<args.length;i++){
			if(args[i].length()==0){ continue; }
			if(args[i].charAt(0)!='-'){ continue; }

			if (args[i].equals("--stateviewer")) {
				if(i+1<args.length){
					File file = new File(args[i+1]);
					if(file.exists()){
						//mainFrame.jsp.setDividerLocation(0);
						mainFrame.toolTab.statePanel.loadFile(file);
					}
				}
			}else{
				println("invalid option: " + args[i]);
			}
		}
	}

	static public void reboot(){
		if(!mainFrame.editorPanel.closeFile()){return;}
		mainFrame.exit();
		Env.save();
		mainFrame.dispose();
		System.out.println("LaViT reboot.");

		RebootRunner rebootRunner = new RebootRunner("-Xms16M -Xmx"+Env.get("REBOOT_MAX_MEMORY"));
		rebootRunner.run();
		while(rebootRunner.isRunning()){
			FrontEnd.sleep(200);
		}
		System.exit(0);
	}

	static public void exit(){
		if(!mainFrame.editorPanel.closeFile()){return;}
		mainFrame.exit();
		if(Env.is("WATCH_DUMP")) Env.dumpWatch();
		Env.save();
		System.out.println("LaViT end.");
		System.exit(0);
	}

	static public void println(final String str){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
			mainFrame.toolTab.systemPanel.logPanel.println(str);
		}});
	}

	static public void errPrintln(final String str){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
			mainFrame.toolTab.systemPanel.logPanel.errPrintln(str);
		}});
	}

	static public void printException(final Exception e){
		javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
			mainFrame.toolTab.systemPanel.logPanel.printException(e);
		}});
		e.printStackTrace();
	}

	static public void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			FrontEnd.printException(e);
		}
	}

	static public void updateLookAndFeel(){
		try{
			if(Env.get("LookAndFeel").equals("Metal")){
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			}else if(Env.get("LookAndFeel").equals("Motif")){
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			}else if(Env.get("LookAndFeel").equals("Windows")){
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}else if(Env.get("LookAndFeel").equals("WindowsClassic")){
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			}else if(Env.get("LookAndFeel").equals("SystemDefault")){
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		}catch(Exception e){
			FrontEnd.printException(e);
		}

	}

	static public void addFontUser(CommonFontUser user){
		fontUsers.add(user);
	}

	static public void removeFontUser(CommonFontUser user){
		fontUsers.remove(user);
	}

	static public void loadAllFont(){
		for(CommonFontUser user : fontUsers){
			user.loadFont();
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		new Env();

		//システムのLookAndFeelのエラーに対応
		FrontEnd.updateLookAndFeel();
		try{
			FrontEnd.sf = new StartupFrame();
		}catch(Exception e){
			Env.set("LookAndFeel","Metal");
			FrontEnd.updateLookAndFeel();
			FrontEnd.sf = new StartupFrame();
		}
		FrontEnd.sf.startEnvSet();

		javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
			try{
				new FrontEnd(args);
				FrontEnd.sf.setVisible(false);
			}catch(Exception e){
				FrontEnd.printException(e);
				e.printStackTrace();
			}
		}});
	}

}