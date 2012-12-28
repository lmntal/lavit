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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateRule;
import lavit.stateviewer.StateTransition;

public class StateSimulationPanel extends JPanel {
	private StatePanel statePanel;
	private StateSimulationPanel simulationPanel;

	private ButtonPanel buttonPanel;
	private RulePanel rulePanel;
	private OutputPanel outputPanel;

	private AutoRunMover mover;
	private ArrayList<StateRule> priorityRules;
	private NumberingStateTransitionRuleFrame ruleWindow;

	private StateNode nowNode;
	private int counter;

	public StateSimulationPanel(StatePanel statePanel){
		this.statePanel = statePanel;
		this.simulationPanel = this;

		setLayout(new BorderLayout());

		buttonPanel = new ButtonPanel();
		add(buttonPanel, BorderLayout.NORTH);

		outputPanel = new OutputPanel();
		rulePanel = new RulePanel();
		JScrollPane rulePane = new JScrollPane(rulePanel);
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rulePane, outputPanel);
		jsp.setOneTouchExpandable(true);
		jsp.setResizeWeight(0.5);
		jsp.setDividerLocation(0.5);
		jsp.setPreferredSize(new Dimension(500,100));

		add(jsp, BorderLayout.CENTER);

		mover = new AutoRunMover(1000);
		mover.start();

		priorityRules = new ArrayList<StateRule>();
		ruleWindow = null;

		init();

	}

	public void init(){
		this.counter = 0;
		this.nowNode = null;
		outputPanel.clear();
		rulePanel.setRules();
		buttonPanel.startButton.setEnabled(true);
		buttonPanel.endButton.setEnabled(false);
		mover.setActive(false);
		priorityRules.clear();
		if(ruleWindow!=null){
			ruleWindow.dispose();
			ruleWindow=null;
		}
	}

	public void applyNode(StateNode node, StateTransition trans){
		++counter;
		this.nowNode = node;
		statePanel.stateGraphPanel.setSelectNode(node);
		statePanel.stateGraphPanel.update();
		if(trans!=null){
			outputPanel.println("["+counter+"] "+node.toString()+" ("+trans.getRuleNameString()+")");
		}else{
			outputPanel.println("["+counter+"] "+node.toString());
		}
		rulePanel.setRules();
	}

	class ButtonPanel extends JPanel implements ActionListener,ChangeListener {
		JButton startButton = new JButton("Start");
		JButton endButton = new JButton("End");
		JCheckBox autoRun = new JCheckBox("Auto Run");
		JSlider intervalSlider = new JSlider(10, 190);
		JButton ruleWindowButton = new JButton("Rule Priority");

		ButtonPanel(){

			//setLayout(new FlowLayout(FlowLayout.LEFT));
			setLayout(new GridLayout(1,5));

			startButton.addActionListener(this);
			add(startButton);

			endButton.addActionListener(this);
			add(endButton);

			autoRun.addActionListener(this);
			add(autoRun);

			intervalSlider.addChangeListener(this);
			add(intervalSlider);

			ruleWindowButton.addActionListener(this);
			add(ruleWindowButton);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if(src==startButton){
				counter = 0;
				outputPanel.clear();
				mover.resetClock();

				StateNode startNode = null;
				StateNodeSet drawNodes = statePanel.stateGraphPanel.getDrawNodes();
				ArrayList<StateNode> selectNodes = statePanel.stateGraphPanel.getSelectNodes();
				if(selectNodes.size()>=1){
					startNode = selectNodes.get(0);
				}else{
					startNode = drawNodes.getStartNodeOne();
				}
				applyNode(startNode,null);
				updateMover();
				endButton.setEnabled(true);
			}else if(src==endButton){
				counter = 0;
				nowNode = null;
				statePanel.stateGraphPanel.selectClear();
				statePanel.stateGraphPanel.update();
				outputPanel.println("[End]\n");
				rulePanel.setRules();
				updateMover();
				buttonPanel.endButton.setEnabled(false);
			}else if(src==autoRun){
				updateMover();
			}else if(src==ruleWindowButton){
				if(ruleWindow==null){
					ruleWindow = new NumberingStateTransitionRuleFrame();
				}
			}
		}

		public void updateMover(){
			if(autoRun.isSelected()){
				rulePanel.setEnabled(false);
				mover.setActive(true);
			}else{
				rulePanel.setEnabled(true);
				mover.setActive(false);
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
			mover.setInterval(intervalSlider.getValue()*10);
		}
	}

	class RulePanel extends JPanel {
		ArrayList<RuleLinePanel> ruleLinePanels;

		RulePanel(){
			setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
			ruleLinePanels = new ArrayList<RuleLinePanel>();
		}

		void setRules(){
			removeAll();
			ruleLinePanels.clear();
			if(nowNode!=null){
				ArrayList<StateTransition> trans = new ArrayList<StateTransition>(nowNode.getToTransitions());

				//遷移先を見てソートする
				Collections.sort(trans, new Comparator<StateTransition>() {
					public int compare(StateTransition t1, StateTransition t2) {
						StateNode n1 = t1.to;
						StateNode n2 = t2.to;
						while(n1.dummy){
							n1 = n1.getToNode();
						}
						while(n2.dummy){
							n2 = n2.getToNode();
						}

						if(n1.depth<n2.depth){
							return 1;
						}else if(n1.depth>n2.depth){
							return -1;
						}else{
							if(n1.getY()<n2.getY()){
								return -1;
							}else if(n1.getY()>n2.getY()){
								return 1;
							}else{
								return 0;
							}
						}
					}
				});

				//ラベルをつけながら表示
				boolean forwardLabel=false,backLabel = false;
				for(StateTransition t : trans){
					//System.out.println(t.to.depth+" : "+t.to);
					if(!forwardLabel&&t.from.depth<t.to.depth){
						forwardLabel = true;
						JLabel l = new JLabel("forward");
						l.setHorizontalAlignment(JLabel.LEFT);
						add(l);
					}
					if(!backLabel&&t.from.depth>=t.to.depth){
						backLabel = true;
						JLabel l = new JLabel("back");
						l.setHorizontalAlignment(JLabel.LEFT);
						add(l);
					}
					RuleLinePanel rlp = new RuleLinePanel(t);
					add(rlp);
					ruleLinePanels.add(rlp);
				}
			}
			if(buttonPanel.autoRun.isSelected()){
				rulePanel.setEnabled(false);
			}else{
				rulePanel.setEnabled(true);
			}
			updateUI();
			//simulationPanel.validate();
			//simulationPanel.updateUI();
		}

		public void setEnabled(boolean enabled){
			super.setEnabled(enabled);
			for(RuleLinePanel rlp : ruleLinePanels){
				rlp.setEnabled(enabled);
			}
		}
	}

	class RuleLinePanel extends JPanel implements ActionListener {
		private JTextField state;
		private JLabel rule;
		private JButton btn;

		private StateTransition trans;
		private StateNode node;

		RuleLinePanel(StateTransition trans){
			this.trans = trans;
			this.node = trans.to;
			while(node.dummy){
				node = node.getToNode();
			}

			setLayout(new BorderLayout());
			setMaximumSize(new Dimension(10000,28));

			state = new JTextField(node.toString());
			state.setBackground(Color.white);
			add(state, BorderLayout.CENTER);

			btn = new JButton("Apply ("+trans.getRuleNameString()+")");
			btn.addActionListener(this);
			add(btn, BorderLayout.WEST);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if(src==btn){
				applyNode(node,trans);
			}
		}

		public void setEnabled(boolean enabled){
			super.setEnabled(enabled);
			btn.setEnabled(enabled);
		}

		public void doClick(){
			applyNode(node,trans);
		}
	}

	class OutputPanel extends JPanel {
		private DefaultStyledDocument doc;
		private JScrollPane jsp;
		private JTextPane log;

		OutputPanel(){
			doc = new DefaultStyledDocument();
			log = new JTextPane(doc);
			log.setEditable(false);
			jsp = new JScrollPane(log);
			jsp.getVerticalScrollBar().setUnitIncrement(15);

			setLayout(new BorderLayout());
			add(jsp,BorderLayout.CENTER);
		}

		public void println(String str){
			SimpleAttributeSet attribute = new SimpleAttributeSet();
			attribute.addAttribute(StyleConstants.Foreground, Color.BLACK);
			println(str,attribute);
		}

		public void errPrintln(String str){
			SimpleAttributeSet attribute = new SimpleAttributeSet();
			attribute.addAttribute(StyleConstants.Foreground, Color.RED);
			println(str,attribute);
		}

		//スレッドセーフ
		public void println(final String str,final SimpleAttributeSet attribute){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
				try {
					doc.insertString(doc.getLength(),str+"\n", attribute);
					log.setCaretPosition(doc.getLength());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}});
		}

		public void clear(){
			log.setText("");
		}
	}

	class AutoRunMover extends Thread {
		private long interval;
		private boolean active;
		long sleepTime = 0;

		AutoRunMover(int interval){
			this.interval = interval;
		}

		public void setInterval(int interval){
			this.interval = interval;
		}

		public void setActive(boolean active){
			this.active = active;
			sleepTime=System.currentTimeMillis();
		}

		public boolean isActive(){
			return active;
		}

		public void resetClock(){
			sleepTime=System.currentTimeMillis();
		}

		public void run(){
			while(true){
				try{
					if(active){
						selectRule();
					}
					while(System.currentTimeMillis()<sleepTime+interval){
						sleep(50);
					}
					sleepTime=System.currentTimeMillis();
				} catch (Exception e) {
				}
			}

		}

		void selectRule(){
			for(StateRule rule : priorityRules){
				for(RuleLinePanel rlp : rulePanel.ruleLinePanels){
					if(rlp.trans.getRules().contains(rule)){
						rlp.doClick();
						return;
					}
				}
			}
			for(RuleLinePanel rlp : rulePanel.ruleLinePanels){
				rlp.doClick();
				return;
			}
		}

	}

	class NumberingStateTransitionRuleFrame extends JFrame implements ActionListener {

		private JPanel panel;
		private SelectPanel rulePanel;

		private JPanel btnPanel;
		private JButton ok;
		private JButton cancel;

		public NumberingStateTransitionRuleFrame(){

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Rules");
	        setIconImages(Env.getApplicationIcons());
	        setAlwaysOnTop(true);
	        setResizable(false);

	        panel = new JPanel();
	        panel.setLayout(new BorderLayout());

	        rulePanel = new SelectPanel();
	        panel.add(rulePanel, BorderLayout.CENTER);


	        btnPanel =  new JPanel();
	        btnPanel.setLayout(new GridLayout(1,2));

	        ok = new JButton(Lang.d[6]);
	        ok.addActionListener(this);
	        btnPanel.add(ok);

	        cancel = new JButton(Lang.d[2]);
	        cancel.addActionListener(this);
	        btnPanel.add(cancel);

	        panel.add(btnPanel, BorderLayout.SOUTH);

	        add(panel);

	        addWindowListener(new ChildWindowListener(this));

	        pack();
	        setLocationRelativeTo(FrontEnd.mainFrame);
	        setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==ok){
				priorityRules = rulePanel.getSortedRules();
				if(ruleWindow!=null){
					ruleWindow.dispose();
					ruleWindow=null;
				}
			}else if(src==cancel){
				if(ruleWindow!=null){
					ruleWindow.dispose();
					ruleWindow=null;
				}
			}
		}

		private class SelectPanel extends JPanel {
			ArrayList<RuleLine> ruleLines;

			SelectPanel(){
				ruleLines = new ArrayList<RuleLine>();
				Set<StateRule> rules = statePanel.stateGraphPanel.getDrawNodes().getRules();
				setLayout(new GridLayout(rules.size(),1));
				for(StateRule r : rules){
					RuleLine rl = new RuleLine(r,rules.size());
					ruleLines.add(rl);
					add(rl);
				}
				for(int i=0;i<priorityRules.size();++i){
					for(RuleLine rl : ruleLines){
						if(priorityRules.get(i)==rl.rule){
							rl.setRank(priorityRules.size()-i);
						}
					}
				}
			}

			ArrayList<StateRule> getSortedRules(){
				Collections.sort(ruleLines, new Comparator<RuleLine>() {
					public int compare(RuleLine t1, RuleLine t2) {
						if(t1.getRank()<t2.getRank()){
							return 1;
						}else if(t1.getRank()>t2.getRank()){
							return -1;
						}else{
							return 0;
						}
					}
				});
				ArrayList<StateRule> sortedRules = new ArrayList<StateRule>();
				for(RuleLine rl : ruleLines){
					if(rl.getRank()>0){
						sortedRules.add(rl.rule);
					}
				}
				return sortedRules;
			}

		}

		private class RuleLine extends JPanel{
			StateRule rule;
			JComboBox box = new JComboBox();

			RuleLine(StateRule rule, int ruleCount){
				this.rule = rule;

				setLayout(new GridLayout(1,2));

				JLabel label = new JLabel(rule.getName());
				add(label);

				box = new JComboBox();
				for(int i=0;i<ruleCount;++i){
					box.addItem(""+i);
				}
				add(box);
			}

			void setRank(int r){
				box.setSelectedItem(""+r);
			}

			int getRank(){
				return Integer.parseInt(box.getSelectedItem().toString());
			}
		}

	}

}
