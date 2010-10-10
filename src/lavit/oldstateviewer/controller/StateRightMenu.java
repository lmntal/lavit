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

package lavit.oldstateviewer.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import lavit.Env;
import lavit.FrontEnd;
import lavit.oldstateviewer.StateGraphPanel;
import lavit.oldstateviewer.StateNode;
import lavit.oldstateviewer.StateTransitionEm;
import lavit.oldstateviewer.worker.StateGraphExchangeWorker;

public class StateRightMenu extends JPopupMenu implements ActionListener{
	private StateGraphPanel graphPanel;

	private JMenu nodeSubmenu = new JMenu("Node");
	private JMenuItem remove = new JMenuItem("Remove");
	private JMenuItem unyo2 = new JMenuItem("Unyo(2G)");
	private JMenuItem unyo3 = new JMenuItem("Unyo(3G)");
	private JMenuItem add = new JMenuItem("add Editor");

	private JMenu transitionSearchSubmenu = new JMenu("Transition Search");
	private JMenuItem transInfo = new JMenuItem("Transition Info");
	private JMenuItem ruleWindow = new JMenuItem("Rule Name");
	private JMenuItem backNs = new JMenuItem("Back Nodes Search");
	private JMenuItem nextNs = new JMenuItem("Next Nodes Search");
	private JMenuItem fromNs = new JMenuItem("From Nodes Search");
	private JMenuItem toNs = new JMenuItem("To Nodes Search");
	private JMenuItem searchShortCycle = new JMenuItem("Search Short Cycle");
	private JMenuItem searchReset = new JMenuItem("Search Reset");

	/*
	private JMenu resetSubmenu = new JMenu("Graph Reset");
	private JMenuItem posReset = new JMenuItem("Position Reset");
	private JMenuItem adjustReset = new JMenuItem("Adjust Reset");
	private JMenuItem adjust2Reset = new JMenuItem("Adjust(Backedge) Reset");

	private JMenu crossSubmenu = new JMenu("Cross Reduction");
	private JMenuItem crossInfo = new JMenuItem("Cross Info");
	private JMenuItem geneticAlgorithm = new JMenuItem("Genetic Algorithm");
	private JMenuItem exchangeReset = new JMenuItem("Adjacent Exchange");
	private JCheckBoxMenuItem exchangeDummyOnly = new JCheckBoxMenuItem("Dummy Only");

	private JMenu dummySubmenu = new JMenu("Dummy Control");
	private JCheckBoxMenuItem dummy = new JCheckBoxMenuItem("Set Dummy");
	private JCheckBoxMenuItem showdummy = new JCheckBoxMenuItem("Show Dummy");
	private JMenuItem dummyInfo = new JMenuItem("Dummy Info");
	private JMenuItem dummyCentering = new JMenuItem("Dummy Centering");
	private JMenuItem dummySmoothing = new JMenuItem("Dummy Smoothing");
	*/

	private JMenu graphViewSubmenu = new JMenu("Graph View");
	private JCheckBoxMenuItem backedge = new JCheckBoxMenuItem("Hide Backedge");
	private JCheckBoxMenuItem showId = new JCheckBoxMenuItem("Show ID");
	private JCheckBoxMenuItem showRule = new JCheckBoxMenuItem("Show Rule");
	private JCheckBoxMenuItem showNoNameRule = new JCheckBoxMenuItem("Show No Name Rule");
	private JCheckBoxMenuItem showOutTransition = new JCheckBoxMenuItem("Show Out Transition");

	private JMenuItem autoCentering = new JMenuItem("Auto Centering");
	private JMenuItem stretchMove = new JMenuItem("Stretch Move");
	private JMenuItem shakeMove = new JMenuItem("Shake Move");
	//private JMenuItem groupMove = new JMenuItem("Group Move");


	private JCheckBoxMenuItem dynamicModeling = new JCheckBoxMenuItem("Dynamic Modeling");

	private JMenu controlViewSubmenu = new JMenu("Control View");
	private JCheckBoxMenuItem match = new JCheckBoxMenuItem("Match State");
	private JCheckBoxMenuItem find = new JCheckBoxMenuItem("Find State");
	private JCheckBoxMenuItem dynamicControl = new  JCheckBoxMenuItem("Dynamic Control");
	private JCheckBoxMenuItem shortcut = new JCheckBoxMenuItem("Control Button");
	private JCheckBoxMenuItem zoomSlider = new  JCheckBoxMenuItem("Show Zoom Slider");
	private JCheckBoxMenuItem info = new JCheckBoxMenuItem("Show Info");

	private JMenu fileSubmenu = new JMenu("File");
	private JMenuItem dotFile = new JMenuItem("Save Dot File");
	private JMenuItem saveSlimOutput = new JMenuItem("Save Slim Output File");
	//private JMenuItem loadSlimOutput = new JMenuItem("Load Slim Output File");

	private JMenuItem updateDefaultYOrder = new JMenuItem("Update Default Y Order");
	private JMenuItem allReset = new JMenuItem("All Reset");
	private JMenuItem allDelete = new JMenuItem("All Delete");

	private JMenuItem test = new JMenuItem("Test");


	public StateRightMenu(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;

		//Node

		remove.addActionListener(this);
		nodeSubmenu.add(remove);

		unyo2.addActionListener(this);
		nodeSubmenu.add(unyo2);

		unyo3.addActionListener(this);
		nodeSubmenu.add(unyo3);

		add.addActionListener(this);
		nodeSubmenu.add(add);

		add(nodeSubmenu);


		//Transition

		transInfo.addActionListener(this);
		transitionSearchSubmenu.add(transInfo);

		transitionSearchSubmenu.addSeparator();

		ruleWindow.addActionListener(this);
		transitionSearchSubmenu.add(ruleWindow);

		backNs.addActionListener(this);
		transitionSearchSubmenu.add(backNs);

		nextNs.addActionListener(this);
		transitionSearchSubmenu.add(nextNs);

		fromNs.addActionListener(this);
		transitionSearchSubmenu.add(fromNs);

		toNs.addActionListener(this);
		transitionSearchSubmenu.add(toNs);

		transitionSearchSubmenu.addSeparator();

		searchShortCycle.addActionListener(this);
		transitionSearchSubmenu.add(searchShortCycle);

		transitionSearchSubmenu.addSeparator();

		searchReset.addActionListener(this);
		transitionSearchSubmenu.add(searchReset);

		add(transitionSearchSubmenu);

		addSeparator();


/*
		autoCentering.addActionListener(this);
		add(autoCentering);

		stretchMove.addActionListener(this);
		add(stretchMove);


		//reset

		posReset.addActionListener(this);
		resetSubmenu.add(posReset);

		adjustReset.addActionListener(this);
		resetSubmenu.add(adjustReset);

		adjust2Reset.addActionListener(this);
		resetSubmenu.add(adjust2Reset);

		add(resetSubmenu);

		//cross

		crossInfo.addActionListener(this);
		crossSubmenu.add(crossInfo);

		crossSubmenu.addSeparator();

		geneticAlgorithm.addActionListener(this);
		crossSubmenu.add(geneticAlgorithm);

		exchangeReset.addActionListener(this);
		crossSubmenu.add(exchangeReset);

		crossSubmenu.addSeparator();

		exchangeDummyOnly.addActionListener(this);
		exchangeDummyOnly.setSelected(Env.is("SV_CROSSREDUCTION_DUMMYONLY"));
		crossSubmenu.add(exchangeDummyOnly);

		add(crossSubmenu);


		//dummy

		dummy.addActionListener(this);
		dummy.setSelected(Env.is("SV_DUMMY"));
		dummySubmenu.add(dummy);

		showdummy.addActionListener(this);
		showdummy.setSelected(Env.is("SV_SHOW_DUMMY"));
		dummySubmenu.add(showdummy);

		dummySubmenu.addSeparator();

		dummyInfo.addActionListener(this);
		dummySubmenu.add(dummyInfo);

		dummySubmenu.addSeparator();

		dummyCentering.addActionListener(this);
		dummySubmenu.add(dummyCentering);

		dummySmoothing.addActionListener(this);
		dummySubmenu.add(dummySmoothing);

		add(dummySubmenu);

		addSeparator();


		dynamicModeling.addActionListener(this);
		dynamicModeling.setSelected(Env.is("SV_DYNAMIC_MOVER"));
		add(dynamicModeling);
*/

		backedge.addActionListener(this);
		backedge.setSelected(Env.is("SV_HIDEBACKEDGE"));
		graphViewSubmenu.add(backedge);

		showId.addActionListener(this);
		showId.setSelected(Env.is("SV_SHOWID"));
		graphViewSubmenu.add(showId);

		showRule.addActionListener(this);
		showRule.setSelected(Env.is("SV_SHOWRULE"));
		graphViewSubmenu.add(showRule);

		showNoNameRule.addActionListener(this);
		showNoNameRule.setSelected(Env.is("SV_SHOWNONAMERULE"));
		graphViewSubmenu.add(showNoNameRule);

		showOutTransition.addActionListener(this);
		showOutTransition.setSelected(Env.is("SV_SHOWOUTTRANS"));
		graphViewSubmenu.add(showOutTransition);

		add(graphViewSubmenu);


		//view

		match.addActionListener(this);
		match.setSelected(Env.is("SV_MATCH"));
		controlViewSubmenu.add(match);

		find.addActionListener(this);
		find.setSelected(Env.is("SV_FIND"));
		controlViewSubmenu.add(find);

		dynamicControl.addActionListener(this);
		dynamicControl.setSelected(Env.is("SV_DYNAMIC_CONTROL"));
		controlViewSubmenu.add(dynamicControl);

		shortcut.addActionListener(this);
		shortcut.setSelected(Env.is("SV_BUTTON"));
		controlViewSubmenu.add(shortcut);

		zoomSlider.addActionListener(this);
		zoomSlider.setSelected(Env.is("SV_ZOOMSLIDER"));
		controlViewSubmenu.add(zoomSlider);

		info.addActionListener(this);
		info.setSelected(Env.is("SV_INFO"));
		controlViewSubmenu.add(info);

		add(controlViewSubmenu);


		//file

		dotFile.addActionListener(this);
		fileSubmenu.add(dotFile);

		saveSlimOutput.addActionListener(this);
		fileSubmenu.add(saveSlimOutput);

		//loadSlimOutput.addActionListener(this);
		//fileSubmenu.add(loadSlimOutput);

		add(fileSubmenu);
		addSeparator();


		updateDefaultYOrder.addActionListener(this);
		add(updateDefaultYOrder);

		//allReset.addActionListener(this);
		//add(allReset);

		allDelete.addActionListener(this);
		add(allDelete);

		updateEnabled();


		//test.addActionListener(this);
		//add(test);
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem src = (JMenuItem)e.getSource();
		if(src==remove){
			//for(StateNode node : graphPanel.getSelectNodes()){
			//	graphPanel.getDrawNodes().remove(node);
			//}
			graphPanel.getDrawNodes().remove(graphPanel.getSelectNodes());
			graphPanel.getSelectNodes().clear();
			graphPanel.repaint();
		}else if(src==unyo2){
			for(StateNode node : graphPanel.getSelectNodes()){
				node.runUnyo2();
			}
		}else if(src==unyo3){
			for(StateNode node : graphPanel.getSelectNodes()){
				node.runUnyo3();
			}
		}else if(src==add){
			for(StateNode node : graphPanel.getSelectNodes()){
				FrontEnd.mainFrame.editorPanel.editor.replaceSelection(node.state);
			}
		}else if(src==transInfo){
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"Transition : "+graphPanel.getDrawNodes().getAllTransition().size(),
					"Transition Info",
					JOptionPane.PLAIN_MESSAGE
			);
		}else if(src==ruleWindow){
			new SelectStateTransitionRuleFrame(graphPanel,new StateTransitionEm(graphPanel));
		}else if(src==backNs){
			graphPanel.emBackNodes(graphPanel.getSelectNodes());
		}else if(src==nextNs){
			graphPanel.emNextNodes(graphPanel.getSelectNodes());
		}else if(src==fromNs){
			graphPanel.emFromNodes(graphPanel.getSelectNodes());
		}else if(src==toNs){
			graphPanel.emToNodes(graphPanel.getSelectNodes());
		}else if(src==searchShortCycle){
			graphPanel.searchShortCycle();
		}else if(src==searchReset){
			graphPanel.searchReset();
/*
		}else if(src==posReset){
			graphPanel.positionReset();
			graphPanel.autoCentering();
		}else if(src==adjustReset){
			graphPanel.adjustReset();
		}else if(src==adjust2Reset){
			graphPanel.adjust2Reset();
		}else if(src==exchangeReset){
			graphPanel.exchangeReset();
		}else if(src==geneticAlgorithm){
			graphPanel.geneticAlgorithmLength();
		}else if(src==exchangeDummyOnly){
			Env.set("SV_CROSSREDUCTION_DUMMYONLY",!Env.is("SV_CROSSREDUCTION_DUMMYONLY"));
		}else if(src==autoCentering){
			graphPanel.autoCentering();
		}else if(src==stretchMove){
			graphPanel.stretchMove();
		}else if(src==dummyCentering){
			//graphPanel.dummyCentering();
		}else if(src==dummySmoothing){
			graphPanel.dummySmoothing();
		}else if(src==shakeMove){
			//graphPanel.shakeMove();
			graphPanel.randomMove();
		}else if(src==updateDefaultYOrder){
			graphPanel.getDrawNodes().updateDefaultYOrder();
		//}else if(src==groupMove){
		//	graphPanel.groupMove();
		}else if(src==dummy){
			Env.set("SV_DUMMY",!Env.is("SV_DUMMY"));
			if(Env.is("SV_DUMMY")){
				graphPanel.getDrawNodes().setDummy();
				graphPanel.getDrawNodes().dummyCentering();
				if(graphPanel.statePanel.isLtl()){
					for(StateNode node : graphPanel.getDrawNodes().getAllNode()){
						if(node.cycle){
							node.weak = false;
							graphPanel.getDrawNodes().setLastOrder(node);
						}else{
							node.weak = true;
						}
					}
				}
				graphPanel.getDrawNodes().updateNodeLooks();
			}else{
				//graphPanel.statePanel.reset();
				graphPanel.getDrawNodes().removeDummy();
			}
			graphPanel.repaint();
		}else if(src==backedge){
			Env.set("SV_HIDEBACKEDGE",!Env.is("SV_HIDEBACKEDGE"));
			graphPanel.repaint();
		}else if(src==showId){
			Env.set("SV_SHOWID",!Env.is("SV_SHOWID"));
			graphPanel.repaint();
		}else if(src==showRule){
			Env.set("SV_SHOWRULE",!Env.is("SV_SHOWRULE"));
			graphPanel.repaint();
		}else if(src==showNoNameRule){
			Env.set("SV_SHOWNONAMERULE",!Env.is("SV_SHOWNONAMERULE"));
			graphPanel.repaint();
		}else if(src==showOutTransition){
			Env.set("SV_SHOWOUTTRANS",!Env.is("SV_SHOWOUTTRANS"));
			graphPanel.repaint();
		}else if(src==dynamicModeling){
			Env.set("SV_DYNAMIC_MOVER",!Env.is("SV_DYNAMIC_MOVER"));
			graphPanel.setDynamicMoverActive(Env.is("SV_DYNAMIC_MOVER"));
		}else if(src==showdummy){
			Env.set("SV_SHOW_DUMMY",!Env.is("SV_SHOW_DUMMY"));
			graphPanel.getDrawNodes().updateNodeLooks();
			graphPanel.repaint();
		}else if(src==info){
			graphPanel.statePanel.stateControlPanel.toggleInfoVisible();
		}else if(src==shortcut){
			graphPanel.statePanel.stateControlPanel.toggleButtonVisible();
		}else if(src==match){
			graphPanel.statePanel.stateControlPanel.toggleMatchVisible();
		}else if(src==find){
			graphPanel.statePanel.stateControlPanel.toggleFindVisible();
		}else if(src==dynamicControl){
			graphPanel.statePanel.stateControlPanel.toggleDynamicVisible();
		}else if(src==zoomSlider){
			graphPanel.statePanel.stateControlPanel.toggleZoomSliderVisible();
		//}else if(src==reduction){
		//	graphPanel.reduction();
		}else if(src==crossInfo){
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"Cross : "+(new StateGraphExchangeWorker(graphPanel)).getAllCross(),
					"Cross Info",
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
			*/
		}else if(src==dotFile){
			dotfileSave();
			FrontEnd.mainFrame.toolTab.setTab("System");
		}else if(src==saveSlimOutput){
			File file = chooseOpenFile();
			if(file!=null){
				graphPanel.statePanel.savaFile(file);
			}
		}else if(src==allReset){
			graphPanel.statePanel.reset();
		}else if(src==allDelete){
			graphPanel.allDelete();
		}else if(src==test){
			/*
			ArrayList<ArrayList<StateNode>> depthNode = graphPanel.getDrawNodes().getDepthNode();
			for(int i=1;i<depthNode.size()-1;i++){
				for(int j=depthNode.get(i).size()-2;j>=1;){
					StateNode node = depthNode.get(i).get(j);
					if(node.getToNodes().size()>0){
						graphPanel.getDrawNodes().remove(node);
						j-=2;
					}else{
						j-=1;
					}
				}
			}
			graphPanel.getSelectNodes().clear();
			graphPanel.repaint();
			*/
			graphPanel.startMover();
		}
		/*
		else if(src==output){
			int w = (int)(panel.getWidth()/panel.getZoom());
			int h = (int)(panel.getHeight()/panel.getZoom());
			BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D)bi.getGraphics();
			g2.setColor(Color.white);
			g2.fillRect(0, 0, w, h);
			drawGraph(g2,w,h,false);
			try {
			    ImageIO.write(bi, "png", new File("output.png"));
			} catch (Exception ex) {
			    FrontEnd.printException(ex);
			}
		}
*/
	}

	private void updateEnabled(){
		remove.setEnabled(true);
		unyo2.setEnabled(true);
		unyo3.setEnabled(true);
		add.setEnabled(true);

		backNs.setEnabled(true);
		nextNs.setEnabled(true);
		fromNs.setEnabled(true);
		toNs.setEnabled(true);

		if(graphPanel.getSelectNodes().size()==0){
			remove.setEnabled(false);
			backNs.setEnabled(false);
			nextNs.setEnabled(false);
			fromNs.setEnabled(false);
			toNs.setEnabled(false);
		}
		if(graphPanel.getSelectNodes().size()!=1){
			unyo2.setEnabled(false);
			unyo3.setEnabled(false);
			add.setEnabled(false);
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
				writer.write(graphPanel.getDrawNodes().getDotString());
				writer.write(graphPanel.getDrawNodes().getRankString());
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