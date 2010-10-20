package lavit.stateviewer.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lavit.Env;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransition;

public class StateSimulationPanel extends JPanel {
	private StatePanel statePanel;
	private StateSimulationPanel simulationPanel;

	private ButtonPanel buttonPanel;
	private RulePanel rulePanel;
	private OutputPanel outputPanel;

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

		init();

	}

	public void init(){
		this.counter = 0;
		this.nowNode = null;
		outputPanel.clear();
		rulePanel.setRules();
		buttonPanel.startButton.setEnabled(true);
		buttonPanel.endButton.setEnabled(false);
	}

	public void applyNode(StateNode node){
		++counter;
		this.nowNode = node;
		statePanel.stateGraphPanel.setSelectNode(node);
		statePanel.stateGraphPanel.update();
		outputPanel.println("["+counter+"] "+node.state);
		rulePanel.setRules();
	}

	class ButtonPanel extends JPanel implements ActionListener{
		JButton startButton = new JButton("Simulation Start");
		JButton endButton = new JButton("Simulation End");

		ButtonPanel(){

			setLayout(new FlowLayout(FlowLayout.LEFT));

			startButton.addActionListener(this);
			add(startButton);

			endButton.addActionListener(this);
			add(endButton);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if(src==startButton){
				counter = 0;
				outputPanel.clear();

				StateNode startNode = null;
				StateNodeSet drawNodes = statePanel.stateGraphPanel.getDrawNodes();
				ArrayList<StateNode> selectNodes = statePanel.stateGraphPanel.getSelectNodes();
				if(selectNodes.size()>=1){
					startNode = selectNodes.get(0);
				}else{
					startNode = drawNodes.getStartNodeOne();
				}
				applyNode(startNode);
				endButton.setEnabled(true);
			}else if(src==endButton){
				counter = 0;
				nowNode = null;
				statePanel.stateGraphPanel.selectClear();
				statePanel.stateGraphPanel.update();
				outputPanel.println("[End]\n");
				rulePanel.setRules();
				buttonPanel.endButton.setEnabled(false);
			}
		}
	}

	class RulePanel extends JPanel {
		RulePanel(){
			setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		}

		void setRules(){
			removeAll();
			if(nowNode!=null){
				ArrayList<StateTransition> trans = new ArrayList<StateTransition>(nowNode.getToTransitions());

				//遷移先を見てソートする
				Collections.sort(trans, new Comparator<StateTransition>() {
					public int compare(StateTransition t1, StateTransition t2) {
						if(t1.to.depth<t2.to.depth){
							return 1;
						}else if(t1.to.depth>t2.to.depth){
							return -1;
						}else{
							if(t1.to.getY()<t2.to.getY()){
								return -1;
							}else if(t1.to.getY()>t2.to.getY()){
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
					add(new RuleLinePanel(t));
				}
			}
			updateUI();
			//simulationPanel.validate();
			//simulationPanel.updateUI();
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

				state = new JTextField(node.state);
				state.setBackground(Color.white);
				add(state, BorderLayout.CENTER);

				btn = new JButton("Apply ("+trans.getRuleNameString()+")");
				btn.addActionListener(this);
				add(btn, BorderLayout.WEST);
			}

			public void actionPerformed(ActionEvent e) {
				Object src = e.getSource();

				if(src==btn){
					applyNode(node);
				}
			}
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

}
