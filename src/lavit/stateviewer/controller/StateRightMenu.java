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
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateTransitionEm;
import lavit.stateviewer.worker.StateGraphExchangeWorker;

public class StateRightMenu extends JPopupMenu implements ActionListener{
	private StateGraphPanel graphPanel;

	private JMenu nodeSubmenu = new JMenu("Node");
	private JMenuItem remove = new JMenuItem("Remove");
	private JMenuItem unyo3 = new JMenuItem("Unyo(3G)");
	private JMenuItem graphene = new JMenuItem("Graphene");
	private JMenuItem add = new JMenuItem("add Editor");

	private JMenu transitionSearchSubmenu = new JMenu("Transition Search");
	private JMenuItem backNs = new JMenuItem("Back Nodes Search");
	private JMenuItem nextNs = new JMenuItem("Next Nodes Search");
	private JMenuItem fromNs = new JMenuItem("From Nodes Search");
	private JMenuItem toNs = new JMenuItem("To Nodes Search");

	private JMenu controlViewSubmenu = new JMenu("Control View");
	private JCheckBoxMenuItem controller = new  JCheckBoxMenuItem("Show Controller");
	private JCheckBoxMenuItem zoomSlider = new  JCheckBoxMenuItem("Show Zoom Slider");
	private JCheckBoxMenuItem info = new JCheckBoxMenuItem("Show Info");

	public StateRightMenu(StateGraphPanel graphPanel){
		this.graphPanel = graphPanel;

		//Node

		remove.addActionListener(this);
		nodeSubmenu.add(remove);

		unyo3.addActionListener(this);
		nodeSubmenu.add(unyo3);

		graphene.addActionListener(this);
		nodeSubmenu.add(graphene);

		add.addActionListener(this);
		nodeSubmenu.add(add);

		add(nodeSubmenu);


		//Transition

		backNs.addActionListener(this);
		transitionSearchSubmenu.add(backNs);

		nextNs.addActionListener(this);
		transitionSearchSubmenu.add(nextNs);

		fromNs.addActionListener(this);
		transitionSearchSubmenu.add(fromNs);

		toNs.addActionListener(this);
		transitionSearchSubmenu.add(toNs);

		add(transitionSearchSubmenu);

		addSeparator();


		//view

		controller.addActionListener(this);
		controller.setSelected(Env.is("SV_CONTROLLER"));
		controlViewSubmenu.add(controller);

		zoomSlider.addActionListener(this);
		zoomSlider.setSelected(Env.is("SV_ZOOMSLIDER"));
		controlViewSubmenu.add(zoomSlider);

		info.addActionListener(this);
		info.setSelected(Env.is("SV_INFO"));
		controlViewSubmenu.add(info);

		add(controlViewSubmenu);

		updateEnabled();
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem src = (JMenuItem)e.getSource();
		if(src==remove){
			graphPanel.getDrawNodes().remove(graphPanel.getSelectNodes());
			graphPanel.getSelectNodes().clear();
			graphPanel.repaint();
		}else if(src==unyo3){
			for(StateNode node : graphPanel.getSelectNodes()){
				node.runUnyo3();
			}
		}else if(src==graphene){
			for(StateNode node : graphPanel.getSelectNodes()){
				node.runGraphene();
			}
		}else if(src==add){
			for(StateNode node : graphPanel.getSelectNodes()){
				FrontEnd.mainFrame.editorPanel.getSelectedEditor().replaceSelection(node.state);
			}
		}else if(src==backNs){
			graphPanel.emBackNodes(graphPanel.getSelectNodes());
		}else if(src==nextNs){
			graphPanel.emNextNodes(graphPanel.getSelectNodes());
		}else if(src==fromNs){
			graphPanel.emFromNodes(graphPanel.getSelectNodes());
		}else if(src==toNs){
			graphPanel.emToNodes(graphPanel.getSelectNodes());
		}else if(src==controller){
			graphPanel.statePanel.stateControlPanel.toggleControllerVisible();
		}else if(src==zoomSlider){
			graphPanel.statePanel.stateControlPanel.toggleZoomSliderVisible();
		}else if(src==info){
			graphPanel.statePanel.stateControlPanel.toggleInfoVisible();
		}
	}

	private void updateEnabled(){
		remove.setEnabled(true);
		unyo3.setEnabled(true);
		graphene.setEnabled(true);
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
			unyo3.setEnabled(false);
			graphene.setEnabled(false);
			add.setEnabled(false);
		}

	}


}