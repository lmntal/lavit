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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransitionAbstraction;
import lavit.stateviewer.worker.StateGraphExchangeWorker;

public class StateButtonPanel extends JPanel implements ActionListener {
	private StatePanel statePanel;

	private JPanel resetPanel = new JPanel();
	private JButton posReset = new JButton("Position Reset");
	private JButton adjustReset = new JButton("Adjust Reset");
	private JButton adjust2Reset = new JButton("Adjust(Backedge) Reset");
	private JButton adjust3Reset = new JButton("Adjust(Find) Reset");
	private JButton dummyMixAdjust = new JButton("Dummy Mix Adjust");
	private JButton allReset = new JButton("All Reset");

	private JPanel crossPanel = new JPanel();
	private JButton crossInfo = new JButton("Cross Info");
	private JButton geneticAlgorithm = new JButton("Genetic Algorithm");
	private JButton exchangeReset = new JButton("Adjacent Exchange");
	public JCheckBox exchangeDummyOnly = new JCheckBox("Dummy Only");

	private JPanel dummyPanel = new JPanel();
	public JCheckBox dummy = new JCheckBox("Set Dummy");
	private JCheckBox showdummy = new JCheckBox("Show Dummy");
	private JButton dummyInfo = new JButton("Dummy Info");
	private JButton dummyCentering = new JButton("Dummy Centering");
	private JButton dummySmoothing = new JButton("Dummy Smoothing");

	private JPanel dynamicPanel = new JPanel();
	private JCheckBox dynamicModeling = new JCheckBox("Dynamic Modeling");
	private JButton stretchMove = new JButton("Stretch Move");
	private JButton autoCentering = new JButton("Auto Centering");

	private JPanel transitionPanel = new JPanel();
	private JButton transitionAbstraction = new JButton("Transition Abstraction");
	private JButton selectAbstraction = new JButton("Select Abstraction");

	private JPanel autoPanel = new JPanel();
	private JButton autoAdjust = new JButton("Auto Adjust");
	private JCheckBox startupAutoAdjust = new JCheckBox("Start up Auto Adjust");

	private JComponent comps[] = {
		autoAdjust,startupAutoAdjust,
		posReset,adjustReset,adjust2Reset,adjust3Reset,dummyMixAdjust,allReset,
		crossInfo,geneticAlgorithm,exchangeReset,exchangeDummyOnly,
		dummy,showdummy,dummyInfo,dummyCentering,dummySmoothing,
		dynamicModeling,stretchMove,autoCentering,
		transitionAbstraction,selectAbstraction
	};

	StateButtonPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		setLayout(new GridLayout(6,1));

		resetPanel.setLayout(new GridLayout(1,6));
		posReset.addActionListener(this);
		resetPanel.add(posReset);
		adjustReset.addActionListener(this);
		resetPanel.add(adjustReset);
		adjust2Reset.addActionListener(this);
		resetPanel.add(adjust2Reset);
		adjust3Reset.addActionListener(this);
		resetPanel.add(adjust3Reset);
		dummyMixAdjust.addActionListener(this);
		resetPanel.add(dummyMixAdjust);
		allReset.addActionListener(this);
		resetPanel.add(allReset);
		add(resetPanel);

		crossPanel.setLayout(new GridLayout(1,4));
		crossInfo.addActionListener(this);
		crossPanel.add(crossInfo);
		geneticAlgorithm.addActionListener(this);
		crossPanel.add(geneticAlgorithm);
		exchangeReset.addActionListener(this);
		crossPanel.add(exchangeReset);
		exchangeDummyOnly.addActionListener(this);
		exchangeDummyOnly.setSelected(Env.is("SV_CROSSREDUCTION_DUMMYONLY"));
		crossPanel.add(exchangeDummyOnly);
		add(crossPanel);

		dummyPanel.setLayout(new GridLayout(1,5));
		dummy.addActionListener(this);
		dummy.setSelected(Env.is("SV_DUMMY"));
		dummyPanel.add(dummy);
		showdummy.addActionListener(this);
		showdummy.setSelected(Env.is("SV_SHOW_DUMMY"));
		dummyPanel.add(showdummy);
		dummyInfo.addActionListener(this);
		dummyPanel.add(dummyInfo);
		dummyCentering.addActionListener(this);
		dummyPanel.add(dummyCentering);
		dummySmoothing.addActionListener(this);
		dummyPanel.add(dummySmoothing);
		add(dummyPanel);

		dynamicPanel.setLayout(new GridLayout(1,3));
		dynamicModeling.addActionListener(this);
		dynamicModeling.setSelected(Env.is("SV_DYNAMIC_MOVER"));
		dynamicPanel.add(dynamicModeling);
		stretchMove.addActionListener(this);
		dynamicPanel.add(stretchMove);
		autoCentering.addActionListener(this);
		dynamicPanel.add(autoCentering);
		add(dynamicPanel);

		transitionPanel.setLayout(new GridLayout(1,2));
		transitionAbstraction.addActionListener(this);
		transitionPanel.add(transitionAbstraction);
		selectAbstraction.addActionListener(this);
		transitionPanel.add(selectAbstraction);
		add(transitionPanel);

		autoPanel.setLayout(new GridLayout(1,2));
		autoAdjust.addActionListener(this);
		autoPanel.add(autoAdjust);
		startupAutoAdjust.addActionListener(this);
		startupAutoAdjust.setSelected(Env.is("SV_AUTO_ADJUST_STARTUP"));
		autoPanel.add(startupAutoAdjust);
		add(autoPanel);

	}

	public void allSetEnabled(boolean enabled){
		for(JComponent comp : comps){
			comp.setEnabled(enabled);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==autoAdjust){
			statePanel.stateGraphPanel.autoAdjust();
		}else if(src==startupAutoAdjust){
			Env.set("SV_AUTO_ADJUST_STARTUP",!Env.is("SV_AUTO_ADJUST_STARTUP"));
		}else if(src==posReset){
			statePanel.stateGraphPanel.positionReset();
			statePanel.stateGraphPanel.autoCentering();
		}else if(src==adjustReset){
			statePanel.stateGraphPanel.adjustReset();
		}else if(src==adjust2Reset){
			statePanel.stateGraphPanel.adjust2Reset();
		}else if(src==adjust3Reset){
			statePanel.stateGraphPanel.adjust3Reset();
		}else if(src==dummyMixAdjust){
			statePanel.stateGraphPanel.dummyMixAdjust();
		}else if(src==allReset){
			statePanel.reset();
		}
		else if(src==crossInfo){
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"Cross : "+(new StateGraphExchangeWorker(statePanel.stateGraphPanel)).getAllCross(),
					"Cross Info",
					JOptionPane.PLAIN_MESSAGE
			);
		}else if(src==geneticAlgorithm){
			statePanel.stateGraphPanel.geneticAlgorithmLength();
		}else if(src==exchangeReset){
			statePanel.stateGraphPanel.exchangeReset();
		}else if(src==exchangeDummyOnly){
			Env.set("SV_CROSSREDUCTION_DUMMYONLY",!Env.is("SV_CROSSREDUCTION_DUMMYONLY"));
		}else if(src==dummy){
			Env.set("SV_DUMMY",!Env.is("SV_DUMMY"));
			if(Env.is("SV_DUMMY")){
				statePanel.stateGraphPanel.getDrawNodes().setDummy();
				statePanel.stateGraphPanel.getDrawNodes().dummyCentering();
			}else{
				statePanel.stateGraphPanel.getDrawNodes().removeDummy();
			}
			statePanel.stateGraphPanel.repaint();
		}else if(src==showdummy){
			Env.set("SV_SHOW_DUMMY",!Env.is("SV_SHOW_DUMMY"));
			statePanel.stateGraphPanel.getDrawNodes().updateNodeLooks();
			statePanel.stateGraphPanel.repaint();
		}else if(src==dummyInfo){
			int size = statePanel.stateGraphPanel.getDrawNodes().size();
			int dummy = statePanel.stateGraphPanel.getDrawNodes().getDummySize();
			JOptionPane.showMessageDialog(
					FrontEnd.mainFrame,
					"All Node : "+size+"\n"+
					"State : "+(size-dummy)+"\n"+
					"Dummy : "+dummy,
					"Dummy Info",
					JOptionPane.PLAIN_MESSAGE
			);
		}else if(src==dummyCentering){
			statePanel.stateGraphPanel.getDrawNodes().dummyCentering();
		}else if(src==dummySmoothing){
			statePanel.stateGraphPanel.dummySmoothing();
		}else if(src==dynamicModeling){
			Env.set("SV_DYNAMIC_MOVER",!Env.is("SV_DYNAMIC_MOVER"));
			statePanel.stateGraphPanel.setDynamicMoverActive(Env.is("SV_DYNAMIC_MOVER"));
		}else if(src==stretchMove){
			statePanel.stateGraphPanel.stretchMove();
		}else if(src==autoCentering){
			statePanel.stateGraphPanel.autoCentering();
		}else if(src==transitionAbstraction){
			StateGraphPanel p = statePanel.stateGraphPanel;
			new SelectStateTransitionRuleFrame(p,new StateTransitionAbstraction(p));
		}else if(src==selectAbstraction){
			statePanel.stateGraphPanel.selectNodeAbstraction();
		}
	}

}
