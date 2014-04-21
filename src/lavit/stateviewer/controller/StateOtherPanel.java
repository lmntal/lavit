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

package lavit.stateviewer.controller;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.worker.StateGraphExchangeWorker;

public class StateOtherPanel extends JPanel implements ActionListener {
	private StatePanel statePanel;

	private JPanel infoPanel = new JPanel();
	private JButton transInfo = new JButton("Transition Infomation");
	private JButton dummyInfo = new JButton("Dummy Infomation");
	private JButton crossInfo = new JButton("Cross Infomation");

	private JPanel graphViewPanel = new JPanel();
	private JCheckBox showdummy = new JCheckBox("Show Dummy");
	private JCheckBox backedge = new JCheckBox("Hide Backedge");
	private JCheckBox showId = new JCheckBox("Show ID");
	private JCheckBox showRule = new JCheckBox("Show Rule");
	private JCheckBox showNoNameRule = new JCheckBox("Show Rule (No Name)");
	private JCheckBox showOutTransition = new JCheckBox("Show Out Transition");
	private JCheckBox noCurve = new JCheckBox("No Curve Transition");
	private JCheckBox antialias = new JCheckBox("Antialias");

	private JPanel startupPanel = new JPanel();
	private JCheckBox startupSetBackDummy = new JCheckBox("Set Back Dummy");
	private JCheckBox startupAbstraction = new JCheckBox("Abstraction");

	private JPanel filePanel = new JPanel();
	private JButton dotFile = new JButton("Save Dot File");
	private JButton saveSlimOutput = new JButton("Save Slim Output File");

	private JPanel otherButtonPanel = new JPanel();
    private JButton searchShortCycle = new JButton("Search Short Cycle");
    private JButton updateDefaultYOrder = new JButton("Update Default Y Order");
    private JButton allDelete = new JButton("All Delete");

	private JComponent comps[] = {
			transInfo,dummyInfo,crossInfo,
			showdummy,backedge,showId,showRule,showNoNameRule,showOutTransition,noCurve,antialias,
			startupSetBackDummy,
			dotFile,saveSlimOutput,
			searchShortCycle,updateDefaultYOrder,allDelete
		};

	StateOtherPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		infoPanel.setLayout(new GridLayout(1,3));
		infoPanel.setBorder(new TitledBorder("Infomation"));
		infoPanel.setMaximumSize(new Dimension(1000,50));
		transInfo.addActionListener(this);
		infoPanel.add(transInfo);
		dummyInfo.addActionListener(this);
		infoPanel.add(dummyInfo);
		crossInfo.addActionListener(this);
		infoPanel.add(crossInfo);
		add(infoPanel);

		graphViewPanel.setLayout(new GridLayout(2,4));
		graphViewPanel.setBorder(new TitledBorder("Graph View"));
		graphViewPanel.setMaximumSize(new Dimension(1000,70));
		showdummy.addActionListener(this);
		showdummy.setSelected(Env.is("SV_SHOW_DUMMY"));
		graphViewPanel.add(showdummy);
		backedge.addActionListener(this);
		backedge.setSelected(Env.is("SV_HIDEBACKEDGE"));
		graphViewPanel.add(backedge);
		showId.addActionListener(this);
		showId.setSelected(Env.is("SV_SHOWID"));
		graphViewPanel.add(showId);
		showRule.addActionListener(this);
		showRule.setSelected(Env.is("SV_SHOWRULE"));
		graphViewPanel.add(showRule);
		showNoNameRule.addActionListener(this);
		showNoNameRule.setSelected(Env.is("SV_SHOWNONAMERULE"));
		graphViewPanel.add(showNoNameRule);
		showOutTransition.addActionListener(this);
		showOutTransition.setSelected(Env.is("SV_SHOWOUTTRANS"));
		graphViewPanel.add(showOutTransition);
		noCurve.addActionListener(this);
		noCurve.setSelected(Env.is("SV_NOCURVE"));
		graphViewPanel.add(noCurve);
		antialias.addActionListener(this);
		antialias.setSelected(Env.is("SV_ANTIALIAS"));
		graphViewPanel.add(antialias);
		add(graphViewPanel);

		startupPanel.setLayout(new GridLayout(1,3));
		startupPanel.setBorder(new TitledBorder("Startup"));
		startupPanel.setMaximumSize(new Dimension(1000,50));
		startupSetBackDummy.addActionListener(this);
		startupSetBackDummy.setSelected(Env.is("SV_STARTUP_SET_BACKDUMMY"));
		startupPanel.add(startupSetBackDummy);
		startupAbstraction.addActionListener(this);
		startupAbstraction.setSelected(Env.is("SV_STARTUP_ABSTRACTION"));
		startupPanel.add(startupAbstraction);
		add(startupPanel);

		filePanel.setLayout(new GridLayout(1,2));
		filePanel.setBorder(new TitledBorder("File"));
		filePanel.setMaximumSize(new Dimension(1000,50));
		dotFile.addActionListener(this);
		filePanel.add(dotFile);
		saveSlimOutput.addActionListener(this);
		filePanel.add(saveSlimOutput);
		add(filePanel);

		//otherButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		otherButtonPanel.setLayout(new GridLayout(1,3));
		searchShortCycle.addActionListener(this);
		otherButtonPanel.add(searchShortCycle);
		updateDefaultYOrder.addActionListener(this);
		otherButtonPanel.add(updateDefaultYOrder);
		allDelete.addActionListener(this);
		otherButtonPanel.add(allDelete);
		add(otherButtonPanel);
	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for(JComponent comp : comps){
			comp.setEnabled(enabled);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		StateGraphPanel graphPanel = statePanel.stateGraphPanel;

		if(src==transInfo){
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"Transition : "+graphPanel.getDrawNodes().getAllTransition().size(),
					"Transition Info",
					JOptionPane.PLAIN_MESSAGE
			);
		}else if(src==dummyInfo){
			int size = graphPanel.getDrawNodes().size();
			int dummy = graphPanel.getDrawNodes().getDummySize();
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"All Node : "+size+"\n"+
					"State : "+(size-dummy)+"\n"+
					"Dummy : "+dummy,
					"Dummy Info",
					JOptionPane.PLAIN_MESSAGE
			);
		}else if(src==crossInfo){
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"Cross : "+(new StateGraphExchangeWorker(statePanel.stateGraphPanel)).getAllCross(),
					"Cross Info",
					JOptionPane.PLAIN_MESSAGE
			);
		}else if(src==showdummy){
			Env.set("SV_SHOW_DUMMY",!Env.is("SV_SHOW_DUMMY"));
			graphPanel.getDraw().setShowDummyMode(Env.is("SV_SHOW_DUMMY"));
			graphPanel.getDrawNodes().updateNodeLooks();
			graphPanel.update();
		}else if(src==backedge){
			Env.set("SV_HIDEBACKEDGE",!Env.is("SV_HIDEBACKEDGE"));
			graphPanel.getDraw().setHideBackEdgeMode(Env.is("SV_HIDEBACKEDGE"));
			graphPanel.update();
		}else if(src==showId){
			Env.set("SV_SHOWID",!Env.is("SV_SHOWID"));
			graphPanel.getDraw().setShowIdMode(Env.is("SV_SHOWID"));
			graphPanel.update();
		}else if(src==showRule){
			Env.set("SV_SHOWRULE",!Env.is("SV_SHOWRULE"));
			graphPanel.getDraw().setShowRuleMode(Env.is("SV_SHOWRULE"));
			graphPanel.update();
		}else if(src==showNoNameRule){
			Env.set("SV_SHOWNONAMERULE",!Env.is("SV_SHOWNONAMERULE"));
			graphPanel.getDraw().setShowNoNameRuleMode(Env.is("SV_SHOWNONAMERULE"));
			graphPanel.update();
		}else if(src==showOutTransition){
			Env.set("SV_SHOWOUTTRANS",!Env.is("SV_SHOWOUTTRANS"));
			graphPanel.update();
		}else if(src==noCurve){
			Env.set("SV_NOCURVE",!Env.is("SV_NOCURVE"));
			graphPanel.update();
		}else if(src==antialias){
			Env.set("SV_ANTIALIAS",!Env.is("SV_ANTIALIAS"));
			graphPanel.update();
		}else if(src==startupSetBackDummy){
			Env.set("SV_STARTUP_SET_BACKDUMMY",!Env.is("SV_STARTUP_SET_BACKDUMMY"));
		}else if(src==startupAbstraction){
			Env.set("SV_STARTUP_ABSTRACTION",!Env.is("SV_STARTUP_ABSTRACTION"));
		}else if(src==dotFile){
			dotfileSave();
			FrontEnd.mainFrame.toolTab.setTab("System");
		}else if(src==saveSlimOutput){
			File file = chooseOpenFile();
			if(file!=null){
				graphPanel.statePanel.savaFile(file);
			}
		}else if(src==searchShortCycle){
			graphPanel.searchShortCycle();
		}else if(src==allDelete){
			graphPanel.allDelete();
		}
	}

	private boolean dotfileSave(){

		File lmn = FrontEnd.mainFrame.editorPanel.getFile();
		File file = new File(lmn.getAbsolutePath()+".dot");
		if (file!=null) {
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath())));
				writer.write("digraph sample {\n");
				writer.write("graph [rankdir = LR];\n");
				writer.write("node [style=filled,color=\"#336666\"];\n");
				writer.write(statePanel.stateGraphPanel.getDrawNodes().getDotString());
				writer.write(statePanel.stateGraphPanel.getDrawNodes().getRankString());
				writer.write("}\n");
				writer.close();
            	FrontEnd.println("(StateViewer) dot file save. [ "+file.getName()+" ]");
            	FrontEnd.println("(StateViewer) example:\ndot -Tgif \""+file.getAbsolutePath()+"\" -o \""+(new File(lmn.getAbsolutePath()+".gif")).getAbsolutePath()+"\"");
            	return true;
			} catch (IOException e) {
				FrontEnd.printException(e);
			}
		}

		return false;
	}

	private File chooseOpenFile(){
		String chooser_dir = Env.get("SV_FILE_LAST_CHOOSER_DIR");
		if(chooser_dir==null){
			chooser_dir=new File("demo").getAbsolutePath();
		}else if(!new File(chooser_dir).exists()&&new File("demo").exists()){
			chooser_dir=new File("demo").getAbsolutePath();
		}
		JFileChooser jfc = new JFileChooser(chooser_dir);
		int r = jfc.showOpenDialog(FrontEnd.mainFrame);
		if (r != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = jfc.getSelectedFile();
		Env.set("SV_FILE_LAST_CHOOSER_DIR",file.getParent());
		return file;
	}

}
