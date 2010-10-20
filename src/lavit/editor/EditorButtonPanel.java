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
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lavit.*;
import lavit.ltl.LtlButtonPanel;
import lavit.runner.*;
import lavit.util.TarGetter;

public class EditorButtonPanel extends JPanel implements ActionListener {

	private EditorPanel editorPanel;

	private LmntalRunner lmntalRunner;
	private SlimRunner slimRunner;
	private UnyoRunner unyoRunner;

	private JPanel buttonPanel;
	public JButton lmntalButton;
	public JButton lmntalgButton;
	public JButton unyoButton;
	public JButton stateProfilerButton;
	public JButton slimButton;
	public JButton sviewerButton;
	public JButton svporButton;
	public JButton nullButton;
	public JButton killButton;

	private JPanel labelPanel;
	private JLabel rowColumnStatus;
	private JLabel fileStatus;

	private int textRow;
	private int textColumn;

	public EditorButtonPanel(EditorPanel editorPanel){
		this.editorPanel = editorPanel;

		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		//ボタン列
		buttonPanel = new JPanel(new GridLayout(2,4));

		lmntalButton = new JButton(Lang.m[11]);
		lmntalButton.addActionListener(this);
		buttonPanel.add(lmntalButton);

		lmntalgButton = new JButton(Lang.m[12]);
		lmntalgButton.addActionListener(this);
		buttonPanel.add(lmntalgButton);

		unyoButton = new JButton(Lang.m[13]);
		unyoButton.addActionListener(this);
		buttonPanel.add(unyoButton);

		stateProfilerButton = new JButton(Lang.m[21]);
		stateProfilerButton.addActionListener(this);
		buttonPanel.add(stateProfilerButton);

		slimButton = new JButton(Lang.m[14]);
		slimButton.addActionListener(this);
		buttonPanel.add(slimButton);

		sviewerButton = new JButton(Lang.m[15]);
		sviewerButton.addActionListener(this);
		buttonPanel.add(sviewerButton);

		//svporButton = new JButton("(POR)"+Lang.m[15]);
		svporButton = new JButton("");
		svporButton.addActionListener(this);
		buttonPanel.add(svporButton);

		/*
		sviewerlButton = new JButton("(LTL)"+Lang.m[15]);
		sviewerlButton.addActionListener(this);
		buttonPanel.add(sviewerlButton);
		*/
		//nullButton = new JButton();
		//buttonPanel.add(nullButton);

		killButton = new JButton(Lang.m[20]);
		killButton.addActionListener(this);
		buttonPanel.add(killButton);

		setAllEnable(true);
		add(buttonPanel);

		//ラベル列
		labelPanel = new JPanel(new GridLayout(1,2));

		fileStatus = new JLabel();
		updateFileStatus();
		labelPanel.add(fileStatus);

		rowColumnStatus = new JLabel();
		rowColumnStatus.setHorizontalAlignment(JLabel.RIGHT);
		setRowColumn(1,1);
		labelPanel.add(rowColumnStatus);

		add(labelPanel);

	}

	public void updateFileStatus(){
		String fileMark = "";
		if(editorPanel.isChanged()){
			fileMark = "["+Lang.f[5]+"]";
		}
		fileStatus.setText(" "+fileMark+" "+editorPanel.getFileName());
	}

	public void setRowColumn(int r, int c){
		this.textRow=r;
		this.textColumn=c;
		String rowColumn = textRow + ":" + textColumn;
		rowColumnStatus.setText(rowColumn);
	}

	public void setAllEnable(boolean enable){
		//nullButton.setEnabled(enable);

		lmntalButton.setEnabled(enable);
		lmntalgButton.setEnabled(enable);
		unyoButton.setEnabled(enable);
		slimButton.setEnabled(enable);
		sviewerButton.setEnabled(enable);
		svporButton.setEnabled(enable);
		stateProfilerButton.setEnabled(enable);

		killButton.setEnabled(!enable);
	}

	private void setButtonEnable(boolean enable){
		setAllEnable(enable);
		FrontEnd.mainFrame.toolTab.ltlPanel.ltlButtonPanel.setAllEnable(enable);
	}

	public void actionPerformed(ActionEvent e) {
		JButton src = (JButton)e.getSource();

		if (src == lmntalButton) {

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(LMNtal) Doing...");
			lmntalRunner = new LmntalRunner(Env.get("LMNTAL_OPTION"));
			lmntalRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(lmntalRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(LMNtal) Done! ["+(lmntalRunner.getTime()/1000.0)+"s]");
				lmntalRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}else if (src == lmntalgButton) {

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(LMNtal) Doing...");
			lmntalRunner = new LmntalRunner("-g "+Env.get("UNYO_OPTION"));
			lmntalRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(lmntalRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(LMNtal) Done!");
				lmntalRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}else if (src == unyoButton) {

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(UNYO) Doing...");
			unyoRunner = new UnyoRunner(Env.get("UNYO_OPTION"));
			unyoRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(unyoRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(UNYO) Done!");
				unyoRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}else if (src == slimButton) {

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(SLIM) Doing...");
			slimRunner = new SlimRunner(Env.get("SLIM_OPTION"));
			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(SLIM) Done! ["+(slimRunner.getTime()/1000.0)+"s]");
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}else if (src == sviewerButton) {

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(StateViewer) Doing...");
			String opt = "--nd "+Env.get("SV_OPTION");
			if(!Env.get("SV_DEPTH_LIMIT").equals("unset")){
				opt += " --bfs_depth "+Env.get("SV_DEPTH_LIMIT");
			}
			slimRunner = new SlimRunner(opt);
			slimRunner.setBuffering(true);
			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(SLIM) Done! ["+(slimRunner.getTime()/1000.0)+"s]");
				if(slimRunner.isSuccess()){
					FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString(),false);
				}
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}else if (src == svporButton) {

			/*
			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(StateViewer) Doing...");
			slimRunner = new SlimRunner("--por "+Env.get("SV_OPTION"));
			slimRunner.setBuffering(true);
			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				if(slimRunner.isSuccess()){
					FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString(),false);
				}
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();
			*/


			//FrontEnd.println(SlimRunner.checkRun()?"ok":"ng");
			//FrontEnd.reboot();


/*
		}else if (src == sviewerlButton) {

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(StateViewer) Doing...");
			slimRunner = new SlimRunner("--ltl_nd --hideruleset");
			slimRunner.setBuffering(true);
			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				if(slimRunner.isSuccess()){
					FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString());
				}
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();
*/

		}else if (src == stateProfilerButton) {


			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("StateProfiler");

			FrontEnd.println("(StateProfiler) Doing...");
			slimRunner = new SlimRunner("--nd_dump --hideruleset");
			slimRunner.setOutputGetter(FrontEnd.mainFrame.toolTab.stateProfilePanel);

			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(StateProfiler) Done!");
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();


		}else if (src == killButton) {

			if(lmntalRunner!=null) lmntalRunner.kill();
			if(slimRunner!=null) slimRunner.kill();
			if(unyoRunner!=null) unyoRunner.kill();
			FrontEnd.mainFrame.toolTab.ltlPanel.ltlButtonPanel.runnerKill();
			FrontEnd.errPrintln("Kill");

		}
	}

}
