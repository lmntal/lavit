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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;

public class MainMenuBar extends JMenuBar implements ActionListener {

	private JMenu file;
    private JMenuItem iNew;
    private JMenuItem iOpen;
    private JMenuItem iSave;
    private JMenuItem iSaveAs;
    private JMenuItem iClose;
    private JMenuItem iCloseAll;
    private JMenuItem iSVOpen;
    private JMenuItem iExit;

    private JMenu edit;
    private JMenuItem iCopy;
    private JMenuItem iCut;
    private JMenuItem iPaste;
    public  JMenuItem iUndo;
    public  JMenuItem iRedo;
    private JMenuItem iTemplate;

    private JMenu run;
    private JMenuItem iLMNtal;
    private JMenuItem iLMNtalg;
    private JMenuItem iUNYO;
    private JMenuItem iSLIM;
    private JMenuItem iILJavaRun;
    private JMenuItem iILSlimRun;
    private JMenuItem iSViewer;
    private JMenuItem iStateProfiler;
    private JMenuItem iKill;
    private JMenuItem iReboot;

    private JMenu setting;
    private JMenuItem iCygwinPath;
    private JMenuItem iSlimPath;
    private JMenuItem iGeneral;

    private JMenu help;
    private JMenuItem iVersion;
    private JMenuItem iRuntime;
    private JMenuItem iBrowse;

    public MainMenuBar(){

    	file = new JMenu(Lang.m[0]);
        add(file);
        file.setMnemonic(KeyEvent.VK_F);

        iNew     = new JMenuItem(Lang.m[1]);
        file.add(iNew);
        iNew.addActionListener(this);
        iNew.setMnemonic(KeyEvent.VK_N);
        iNew.setAccelerator(KeyStroke.getKeyStroke("control N"));

        iOpen    = new JMenuItem(Lang.m[2]);
        file.add(iOpen);
        iOpen.addActionListener(this);
        iOpen.setMnemonic(KeyEvent.VK_O);
        iOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));

        iSave    = new JMenuItem(Lang.m[3]);
        file.add(iSave);
        iSave.addActionListener(this);
        iSave.setMnemonic(KeyEvent.VK_S);
        iSave.setAccelerator(KeyStroke.getKeyStroke("control S"));

        iSaveAs  = new JMenuItem(Lang.m[4]);
        file.add(iSaveAs);
        iSaveAs.addActionListener(this);
        iSaveAs.setMnemonic(KeyEvent.VK_A);
        
        file.addSeparator();
        
        iClose = new JMenuItem(Lang.m[31]);
        file.add(iClose);
        iClose.addActionListener(this);
        iClose.setMnemonic(KeyEvent.VK_C);

        iCloseAll = new JMenuItem(Lang.m[32]);
        file.add(iCloseAll);
        iCloseAll.addActionListener(this);
        iCloseAll.setMnemonic(KeyEvent.VK_L);

        file.addSeparator();

        iSVOpen  = new JMenuItem(Lang.m[30]);
        file.add(iSVOpen);
        iSVOpen.addActionListener(this);

        file.addSeparator();

        iExit    = new JMenuItem(Lang.m[5]);
        file.add(iExit);
        iExit.addActionListener(this);
        iExit.setMnemonic(KeyEvent.VK_X);


        edit = new JMenu(Lang.m[6]);
        add(edit);
        edit.setMnemonic(KeyEvent.VK_E);

        iUndo = new JMenuItem(Lang.m[16]);
        edit.add(iUndo);
        iUndo.setEnabled(false);
        iUndo.addActionListener(this);
        iUndo.setMnemonic(KeyEvent.VK_U);
        iUndo.setAccelerator(KeyStroke.getKeyStroke("control Z"));

        iRedo = new JMenuItem(Lang.m[17]);
        edit.add(iRedo);
        iRedo.setEnabled(false);
        iRedo.addActionListener(this);
        iRedo.setMnemonic(KeyEvent.VK_R);
        iRedo.setAccelerator(KeyStroke.getKeyStroke("control Y"));

        edit.addSeparator();

        iCopy = new JMenuItem(Lang.m[7]);
        edit.add(iCopy);
        iCopy.addActionListener(this);
        iCopy.setMnemonic(KeyEvent.VK_C);
        iCopy.setAccelerator(KeyStroke.getKeyStroke("control C"));

        iCut = new JMenuItem(Lang.m[8]);
        edit.add(iCut);
        iCut.addActionListener(this);
        iCut.setMnemonic(KeyEvent.VK_T);
        iCut.setAccelerator(KeyStroke.getKeyStroke("control X"));

        iPaste = new JMenuItem(Lang.m[9]);
        edit.add(iPaste);
        iPaste.addActionListener(this);
        iPaste.setMnemonic(KeyEvent.VK_P);
        iPaste.setAccelerator(KeyStroke.getKeyStroke("control V"));
        
        edit.addMenuListener(new MenuListener()
		{
			@Override
			public void menuSelected(MenuEvent e)
			{
				iUndo.setEnabled(FrontEnd.mainFrame.editorPanel.canUndo());
				iRedo.setEnabled(FrontEnd.mainFrame.editorPanel.canRedo());
			}
			
			@Override
			public void menuDeselected(MenuEvent e)
			{
			}
			
			@Override
			public void menuCanceled(MenuEvent e)
			{
			}
		});
        
        /*
        edit.addSeparator();
        
        iTemplate = new JMenuItem("Insert template...");
        iTemplate.setMnemonic(KeyEvent.VK_T);
        iTemplate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				FrontEnd.mainFrame.loadTemplate();
			}
		});
        edit.add(iTemplate);
		*/

        run = new JMenu(Lang.m[10]);
        add(run);
        run.setMnemonic(KeyEvent.VK_R);

        iLMNtal = new JMenuItem(Lang.m[11]);
        run.add(iLMNtal);
        iLMNtal.addActionListener(this);
        iLMNtal.setMnemonic(KeyEvent.VK_L);
        iLMNtal.setAccelerator(KeyStroke.getKeyStroke("F1"));

        iLMNtalg = new JMenuItem(Lang.m[12]);
        run.add(iLMNtalg);
        iLMNtalg.addActionListener(this);
        iLMNtalg.setMnemonic(KeyEvent.VK_G);
        iLMNtalg.setAccelerator(KeyStroke.getKeyStroke("F2"));

        iUNYO = new JMenuItem(Lang.m[13]);
        run.add(iUNYO);
        iUNYO.addActionListener(this);
        iUNYO.setMnemonic(KeyEvent.VK_U);
        iUNYO.setAccelerator(KeyStroke.getKeyStroke("F3"));

        iSLIM = new JMenuItem(Lang.m[14]);
        run.add(iSLIM);
        iSLIM.addActionListener(this);
        iSLIM.setMnemonic(KeyEvent.VK_S);
        iSLIM.setAccelerator(KeyStroke.getKeyStroke("F4"));
        
        run.addSeparator();

        iILJavaRun = new JMenuItem(Lang.m[33]);
        run.add(iILJavaRun);
        iILJavaRun.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				FrontEnd.mainFrame.runAsILCode();
			}
		});
        iILJavaRun.setMnemonic(KeyEvent.VK_I);
        
        /*
        iILSlimRun = new JMenuItem("中間命令列をSLIMで実行(M)");
        run.add(iILSlimRun);
        iILSlimRun.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				FrontEnd.mainFrame.runAsILCode();
			}
		});
        iILSlimRun.setMnemonic(KeyEvent.VK_I);
        */
        
        run.addSeparator();

        iSViewer = new JMenuItem(Lang.m[15]);
        run.add(iSViewer);
        iSViewer.addActionListener(this);
        iSViewer.setMnemonic(KeyEvent.VK_V);
        iSViewer.setAccelerator(KeyStroke.getKeyStroke("F5"));

        iStateProfiler = new JMenuItem(Lang.m[21]);
        run.add(iStateProfiler);
        iStateProfiler.addActionListener(this);
        iStateProfiler.setMnemonic(KeyEvent.VK_P);

        iKill = new JMenuItem(Lang.m[20]);
        run.add(iKill);
        iKill.addActionListener(this);
        iKill.setMnemonic(KeyEvent.VK_K);
        iKill.setAccelerator(KeyStroke.getKeyStroke("ESCAPE"));

        run.addSeparator();

        iReboot = new JMenuItem(Lang.m[27]);
        run.add(iReboot);
        iReboot.addActionListener(this);


        setting = new JMenu(Lang.m[22]);
        add(setting);
        setting.setMnemonic(KeyEvent.VK_S);

        iCygwinPath = new JMenuItem(Lang.m[23]);
        setting.add(iCygwinPath);
        iCygwinPath.addActionListener(this);

        iSlimPath = new JMenuItem(Lang.m[24]);
        setting.add(iSlimPath);
        iSlimPath.addActionListener(this);

        iGeneral = new JMenuItem(Lang.m[29]);
        setting.add(iGeneral);
        iGeneral.addActionListener(this);


        help = new JMenu(Lang.m[18]);
        add(help);
        help.setMnemonic(KeyEvent.VK_H);

        iVersion = new JMenuItem(Lang.m[19]);
        help.add(iVersion);
        iVersion.addActionListener(this);

        iRuntime = new JMenuItem(Lang.m[26]);
        help.add(iRuntime);
        iRuntime.addActionListener(this);

        iBrowse = new JMenuItem(Lang.m[28]);
        help.add(iBrowse);
        iBrowse.addActionListener(this);

    }

    public void updateUndoRedo(boolean undo,boolean redo){
    	iUndo.setEnabled(undo);
		iRedo.setEnabled(redo);
    }

	public void actionPerformed(ActionEvent e) {
		JMenuItem src = (JMenuItem)e.getSource();

		if (src == iNew) {
			FrontEnd.mainFrame.editorPanel.newFileOpen();
        }else if(src == iOpen) {
        	FrontEnd.mainFrame.editorPanel.fileOpen();
		}else if(src == iSave) {
        	FrontEnd.mainFrame.editorPanel.fileSave();
        }else if(src == iSaveAs) {
        	FrontEnd.mainFrame.editorPanel.fileSaveAs();
        }else if(src == iClose) {
        	FrontEnd.mainFrame.editorPanel.closeSelectedPage();
        }else if(src == iCloseAll) {
        	FrontEnd.mainFrame.editorPanel.closeFile();
        }else if(src == iSVOpen){
        	FrontEnd.mainFrame.toolTab.statePanel.loadFile();
        }else if(src == iExit) {
        	FrontEnd.frontEnd.exit();
        }else if(src == iCopy) {
        	FrontEnd.mainFrame.editorPanel.getSelectedEditor().copy();
        }else if(src == iCut) {
        	FrontEnd.mainFrame.editorPanel.getSelectedEditor().cut();
        }else if(src == iPaste) {
        	FrontEnd.mainFrame.editorPanel.getSelectedEditor().paste();
        }else if(src == iUndo) {
        	FrontEnd.mainFrame.editorPanel.editorUndo();
        }else if(src == iRedo) {
        	FrontEnd.mainFrame.editorPanel.editorRedo();
        }else if(src == iLMNtal) {
        	FrontEnd.mainFrame.editorPanel.buttonPanel.lmntalButton.doClick();
        }else if(src == iLMNtalg) {
        	FrontEnd.mainFrame.editorPanel.buttonPanel.lmntalgButton.doClick();
        }else if(src == iUNYO) {
        	FrontEnd.mainFrame.editorPanel.buttonPanel.unyoButton.doClick();
        }else if(src == iSLIM) {
        	FrontEnd.mainFrame.editorPanel.buttonPanel.slimButton.doClick();
        }else if(src == iSViewer) {
        	FrontEnd.mainFrame.editorPanel.buttonPanel.sviewerButton.doClick();
        }else if(src == iKill) {
        	FrontEnd.mainFrame.editorPanel.buttonPanel.killButton.doClick();
        }else if(src == iReboot) {
        	new RebootFrame();
        }else if(src == iCygwinPath) {
        	CygwinPathSetting.showDialog();
        }else if(src == iSlimPath) {
        	SlimPathSetting.showDialog();
        }else if(src == iGeneral) {
        	new GeneralSettingFrame();
        }else if(src == iVersion) {
        	VersionFrame.showDialog();
        }else if(src == iRuntime) {
        	JOptionPane.showMessageDialog(
        			FrontEnd.mainFrame,
        			"Max Memory : "+(int)(Runtime.getRuntime().maxMemory()/(1024*1024))+" MB\n"+
        			"Use Memory : "+(int)(Runtime.getRuntime().totalMemory()/(1024*1024))+" MB\n"+
        			"Available Processors : "+Runtime.getRuntime().availableProcessors()+" \n"+
        			"Java Version : "+System.getProperty("java.version")+" \n"+
        			"Java Runtime Version : "+System.getProperty("java.runtime.version")+" \n"+
        			"Java VM Version : "+System.getProperty("java.vm.version")+" \n",
        			"Java Runtime Info",
        		JOptionPane.PLAIN_MESSAGE
        	);
        }else if(src == iBrowse){
        	if(!Desktop.isDesktopSupported()) return;
        	try{
        	  Desktop.getDesktop().browse(new URI(Env.APP_HREF));
        	}catch(IOException ioe) {
        	  ioe.printStackTrace();
        	}catch(URISyntaxException use) {
        	  use.printStackTrace();
        	}
        }

	}

}
