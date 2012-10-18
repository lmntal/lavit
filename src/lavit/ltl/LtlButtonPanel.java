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

package lavit.ltl;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.EditorButtonPanel;
import lavit.editor.EditorPanel;
import lavit.runner.SlimRunner;
import lavit.util.FixFlowLayout;

public class LtlButtonPanel extends JPanel implements ActionListener {

	public LtlPanel ltlPanel;

	private SlimRunner slimRunner;

	private JLabel label;
	private JComboBox combo;
	private String fileNumberList[] = {"0","1","2","3","4","5","6","7","8","9"};
	private JButton saveButton;
	private JButton loadButton;

	private JButton ltlButton;
	private JButton ltlallButton;
	private JButton ltlndButton;
	private JButton ltlsvButton;

	LtlButtonPanel(LtlPanel ltlPanel){

		this.ltlPanel = ltlPanel;

		setLayout(new FixFlowLayout());

		JPanel filePanel = new JPanel();
		filePanel.setBorder(new TitledBorder("LTL File"));

		label = new JLabel("File No.");
		filePanel.add(label);
		combo = new JComboBox(fileNumberList);
		Dimension dim = combo.getPreferredSize();
		combo.setPreferredSize(new Dimension(dim.width+10, dim.height));
		filePanel.add(combo);

		loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		filePanel.add(loadButton);

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		filePanel.add(saveButton);
		add(filePanel);


		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
		buttonPanel.setBorder(new TitledBorder("LTL Model Check"));

		ltlButton = new JButton("slim --ltl");
		ltlButton.addActionListener(this);
		buttonPanel.add(ltlButton);

		ltlallButton = new JButton("slim --ltl-all");
		ltlallButton.addActionListener(this);
		buttonPanel.add(ltlallButton);

		/*
		ltlndButton = new JButton("slim --ltl_nd");
		ltlndButton.addActionListener(this);
		buttonPanel.add(ltlndButton);
		 */

		ltlsvButton = new JButton("LTL StateViewer");
		ltlsvButton.addActionListener(this);
		buttonPanel.add(ltlsvButton);

		setAllEnable(true);

		add(buttonPanel);

	}

	public void setAllEnable(boolean enable){
		ltlButton.setEnabled(enable);
		ltlallButton.setEnabled(enable);
		//ltlndButton.setEnabled(enable);
		ltlsvButton.setEnabled(enable);
	}

	private void setButtonEnable(boolean enable){
		setAllEnable(enable);
		FrontEnd.mainFrame.editorPanel.buttonPanel.setAllEnable(enable);
	}

	public void setSelected(String no){
		combo.setSelectedItem(no);
	}

	public String getSelectedNo(){
		return (String)combo.getSelectedItem();
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==loadButton){
			ltlPanel.loadFile(getSelectedNo());
		}else if(src==saveButton){
			ltlPanel.saveFile(getSelectedNo());
		}else if(src==ltlButton||src==ltlallButton||src==ltlndButton){

			EditorPanel editorPanel = FrontEnd.mainFrame.editorPanel;

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			ltlPanel.saveFile(getSelectedNo());

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(SLIM) Doing...");

			String option = "";
			if(Env.is("SLIM2")){
				if(src==ltlButton){
					option = "--ltl "+Env.get("LTL_OPTION");
				}else if(src==ltlallButton){
					option = "--ltl-all "+Env.get("LTL_OPTION");
				}
			}else{
				if(src==ltlButton){
					option = "--ltl "+Env.get("LTL_OPTION");
				}else if(src==ltlallButton){
					option = "--ltl_all "+Env.get("LTL_OPTION");
				}else if(src==ltlndButton){
					option = "--ltl_nd "+Env.get("LTL_OPTION");
				}
			}

			slimRunner = new SlimRunner(option);
			slimRunner.setSymbolFile(ltlPanel.getSymbolFile(getSelectedNo()));
			slimRunner.setNcFile(ltlPanel.getNcFile(getSelectedNo()));
			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				FrontEnd.println("(SLIM) Done!");
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}else if(src==ltlsvButton){

			EditorPanel editorPanel = FrontEnd.mainFrame.editorPanel;

			if(editorPanel.isChanged()){
				editorPanel.fileSave();
			}

			ltlPanel.saveFile(getSelectedNo());

			setButtonEnable(false);

			FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(StateViewer) Doing...");

			if(Env.is("SLIM2")){
				slimRunner = new SlimRunner("--ltl-all -t --dump-lavit "+Env.get("LTL_OPTION"));
			}else{
				slimRunner = new SlimRunner("--ltl_nd "+Env.get("LTL_OPTION"));
			}
			slimRunner.setSymbolFile(ltlPanel.getSymbolFile(getSelectedNo()));
			slimRunner.setNcFile(ltlPanel.getNcFile(getSelectedNo()));
			slimRunner.setBuffering(true);
			slimRunner.run();
			(new Thread(new Runnable() { public void run() {
				while(slimRunner.isRunning()){
					FrontEnd.sleep(200);
				}
				if(slimRunner.isSucceeded()){
					FrontEnd.mainFrame.toolTab.statePanel.start(slimRunner.getBufferString(),true);
				}
				slimRunner = null;
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					setButtonEnable(true);
				}});
			}})).start();

		}
	}

	public void runnerKill(){
		if(slimRunner!=null) slimRunner.kill();
	}

}
