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
//import java.util.HashMap;
//import java.util.Map;
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
import lavit.frame.ChildWindowListener;
import lavit.localizedtext.MsgID;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateRule;
import lavit.stateviewer.StateTransition;

@SuppressWarnings("serial")
public class StateSimulationPanel extends JPanel {
	private StatePanel statePanel;

	private ButtonPanel buttonPanel;
	private RulePanel rulePanel;
	private OutputPanel outputPanel;

	private AutoRunMover mover;
	private ArrayList<StateRule> priorityRules;
	private NumberingStateTransitionRuleFrame ruleWindow;

	private StateNode nowNode;
	private int counter;

	public StateSimulationPanel(StatePanel statePanel) {
		this.statePanel = statePanel;

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
		jsp.setPreferredSize(new Dimension(500, 100));

		add(jsp, BorderLayout.CENTER);

		mover = new AutoRunMover(1000);
		mover.start();

		priorityRules = new ArrayList<StateRule>();
		ruleWindow = null;

		init();

	}

	public void init() {
		this.counter = 0;
		this.nowNode = null;
		outputPanel.clear();
		rulePanel.setRules();
		buttonPanel.startButton.setEnabled(true);
		buttonPanel.endButton.setEnabled(false);
		mover.setActive(false);
		priorityRules.clear();
		if (ruleWindow != null) {
			ruleWindow.dispose();
			ruleWindow = null;
		}
	}

	public void applyNode(StateNode node, StateTransition trans) {
		++counter;
		this.nowNode = node;
		statePanel.stateGraphPanel.setSelectNode(node);
		statePanel.stateGraphPanel.update();
		if (trans != null) {
			outputPanel.println("[" + counter + "] " + node.toString() + " (" + trans.getRuleNameString() + ")");
		} else {
			outputPanel.println("[" + counter + "] " + node.toString());
		}
		rulePanel.setRules();
	}

	class ButtonPanel extends JPanel implements ActionListener, ChangeListener {
		JButton startButton = new JButton("Start");
		JButton endButton = new JButton("End");
		JCheckBox autoRun = new JCheckBox("Auto Run");
		//JButton abstractButton = new JButton("Abstract");
		JSlider intervalSlider = new JSlider(10, 190);
		JButton ruleWindowButton = new JButton("Rule Priority");

		ButtonPanel() {

			// setLayout(new FlowLayout(FlowLayout.LEFT));
			setLayout(new GridLayout(1, 5));

			startButton.addActionListener(this);
			add(startButton);

			endButton.addActionListener(this);
			add(endButton);

			// 初期状態でチェックする
			autoRun.setSelected(true);
			autoRun.addActionListener(this);
			add(autoRun);

			//abstractButton.addActionListener(this);
			//add(abstractButton);

			intervalSlider.addChangeListener(this);
			add(intervalSlider);

			ruleWindowButton.addActionListener(this);
			add(ruleWindowButton);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if (src == startButton) {
				counter = 0;
				outputPanel.clear();
				mover.resetClock();

				StateNode startNode = null;
				StateNodeSet drawNodes = statePanel.stateGraphPanel.getDrawNodes();
				ArrayList<StateNode> selectNodes = statePanel.stateGraphPanel.getSelectNodes();
				if (selectNodes.size() >= 1) {
					startNode = selectNodes.get(0);
				} else {
					startNode = drawNodes.getStartNodeOne();
				}
				applyNode(startNode, null);
				updateMover();
				endButton.setEnabled(true);
			} else if (src == endButton) {
				counter = 0;
				nowNode = null;
				statePanel.stateGraphPanel.selectClear();
				statePanel.stateGraphPanel.update();
				outputPanel.println("[End]\n");
				rulePanel.setRules();
				updateMover();
				buttonPanel.endButton.setEnabled(false);
			} else if (src == autoRun) {
				updateMover();
			//} else if (src == abstractButton) {
			//	simulatAbstract();
			} else if (src == ruleWindowButton) {
				if (ruleWindow == null) {
					ruleWindow = new NumberingStateTransitionRuleFrame();
				}
			}
		}

		public void updateMover() {
			if (autoRun.isSelected()) {
				rulePanel.setEnabled(false);
				mover.setActive(true);
			} else {
				rulePanel.setEnabled(true);
				mover.setActive(false);
			}
		}

		//public void simulatAbstract() {
			//String log = outputPanel.getText();
			//String[] steps = log.split("\n");
			//for (int i = 0; i < steps.length; ++i) {
			//	// もしstepが[End]なら
			//	if (steps[i].equals("[End]")) {
			//		break;
			//	}
			//	// stepの最初の[数字]を取り除く
			//	steps[i] = steps[i].substring(steps[i].indexOf("]") + 2);
			//	// stepの最後の(ルール名)を取り除く　ルール名がない場合は何もしない
			//	if (! steps[i].endsWith(". ")){
			//		steps[i] = steps[i].substring(0, steps[i].lastIndexOf("(") - 1);
			//	}
			//}
			//// step[i]がz(0.1). z(0.1). z(0.1) で、step[i+1]がz(0.2). z(0.1). だったら
			//// (z(0.1). z(0.1).) z(0.1). にする
			//// 最終的に step[0]が((z(0.1). z(0.1)) z(0.1).) z(0.1). のようになり、これだけを表示する
			//// 後ろから見ていって、このルールに従う
			//// for文でstepsの有効なindexを取得
			//int index = 0;
			//for (int i = steps.length - 1; i >= 0; i--) {
			//	if (!steps[i].equals("[End]")) {
			//		index = i;
			//		break;
			//	}
			//}
			//outputPanel.clear();
			////String[] stateStep2 = steps[index].split(" ");
			//String[] abstract_results = new String[index + 1];
			//for (int i = index; i > 0; i--) {
			//	String[] stateStep2 = steps[i].split(" ");
			//	String[] stateStep1 = steps[i - 1].split(" ");
			//	// stateStep2とstateStep1のdiffをとる
			//	// 差分を取得(順番は関係ないので、2重ループで全探索)
			//	for (int k = 0; k < stateStep2.length; k++) {
			//		for (int l = 0; l < stateStep1.length; l++) {
			//			if (stateStep2[k].equals(stateStep1[l])) {
			//				stateStep2 = remove(stateStep2, k);
			//				stateStep1 = remove(stateStep1, l);
			//				k--;
			//				break;
			//			}
			//		}
			//	}
//
			//	System.out.println("check point 3" + i);
			//	String[] stateStep1_all = steps[i-1].split(" ");
			//	// stateStep1_allの要素と、count、true,falseを持つ構造体にする　Hashmapは使えない
			//	// Booleanとcountを持つ構造体を作る
			//	Map<String, Map<Integer, Boolean>> stateStep1_all_map = new HashMap<String, Map<Integer, Boolean>>();
			//	//Map<Integer, Boolean> index_map = new HashMap<Integer, Boolean>();
			//	// stateStep1_allの要素をmapに入れる
			//	for (int k = 0; k < stateStep1_all.length; k++) {
			//		// もし、要素がmapにあれば、countを増やす
			//		if (stateStep1_all_map.containsKey(stateStep1_all[k])) {
			//			Map<Integer, Boolean> index_map = stateStep1_all_map.get(stateStep1_all[k]);
			//			// index_mapのintegerの最大値を取得
			//			int max = 0;
			//			for (int key : index_map.keySet()) {
			//				if (max < key) {
			//					max = key;
			//				}
			//			}
			//			// index_mapの最大値に1を足して、mapに追加
			//			index_map.put(max + 1, false);
			//			stateStep1_all_map.put(stateStep1_all[k], index_map);
			//		} else {
			//			// もし、要素がmapになければ、countを1にして、mapに追加
			//			Map<Integer, Boolean> index_map = new HashMap<Integer, Boolean>();
			//			index_map.put(1, false);
			//			stateStep1_all_map.put(stateStep1_all[k], index_map);
			//		}
			//	}// ここまで【】
//
			//	// stateStep1に残ったものが、stateStep2にないもの
			//	// これをsteps[i]において()で囲む
			//	// stateStep1_allの要素から、stateStep1の要素を探す（1つのみ）
			//	// あれば、mapを更新
			//	for (int k = 0; k < stateStep1_all.length; k++) {
			//		for (int l = 0; l < stateStep1.length; l++) {
			//			if (stateStep1_all[k].equals(stateStep1[l])) {
			//				// mapを更新
			//				stateStep1_all_map.put(stateStep1_all[k], true);
			//				stateStep1 = remove(stateStep1, l);
			//				break;
			//			}
			//		}
			//	}
//
			//	// 2重ループで全探索
			//	//for (int k = 0; k < stateStep1_all.length; k++) {
			//	//	for (int l = 0; l < stateStep1.length; l++) {
			//	//		if (stateStep1_all[k].equals(stateStep1[l])) {
			//	//			// mapを更新
			//	//			stateStep1_all_map.put(stateStep1_all[k], true);
			//	//		}
			//	//	}
			//	//}
			//	System.out.println("check point 4");
			//	// mapを見て、bolleanがtrueのものを取り出す
			//	String stateStep1_all_true = "";
			//	String stateStep1_all_false = "";
			//	for (int k = 0; k < stateStep1_all.length; k++) {
			//		if (stateStep1_all_map.get(stateStep1_all[k])) {
			//			stateStep1_all_true += stateStep1_all[k] + " ";
			//		} else {
			//			stateStep1_all_false += stateStep1_all[k] + " ";
			//		}
			//	}
			//	// abstract_result stateStep2_tmp_trueを()で囲む
			//	abstract_results[i] = "(" + stateStep1_all_true + ")";
			//	// abstract_resultにstateStep2_tmp_falseを足す
			//	abstract_results[i] += stateStep1_all_false;
			//	outputPanel.println(abstract_results[i]);
			//	//stateStep2 = abstract_result.split(" ");
			//	System.out.println("check point 5");
//
			//}
			//System.out.println("check point 6");
			//
			////outputPanel.clear();
			////for (int i = 0; i < steps.length; ++i) {
			////	outputPanel.println(abstract_results[i]);
			////}
		//}

		//// this fuction is used in simulatAbstract()
		//private static String[] remove(String[] arr, int index) {
		//	if (arr == null || index < 0 || index >= arr.length) {
		//		return arr;
		//	}
		//	ArrayList<String> result = new ArrayList<>();
		//	for (int i = 0; i < arr.length; i++) {
		//		if (i != index) {
		//			result.add(arr[i]);
		//		}
		//	}
		//	return result.toArray(new String[0]);
		//}

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
			mover.setInterval(intervalSlider.getValue() * 10);
		}
	}

	class RulePanel extends JPanel {
		ArrayList<RuleLinePanel> ruleLinePanels;

		RulePanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			ruleLinePanels = new ArrayList<RuleLinePanel>();
		}

		void setRules() {
			removeAll();
			ruleLinePanels.clear();
			if (nowNode != null) {
				ArrayList<StateTransition> trans = new ArrayList<StateTransition>(nowNode.getToTransitions());

				// 遷移先を見てソートする
				Collections.sort(trans, new Comparator<StateTransition>() {
					public int compare(StateTransition t1, StateTransition t2) {
						StateNode n1 = t1.to;
						StateNode n2 = t2.to;
						while (n1.dummy) {
							n1 = n1.getToNode();
						}
						while (n2.dummy) {
							n2 = n2.getToNode();
						}

						if (n1.depth < n2.depth) {
							return 1;
						} else if (n1.depth > n2.depth) {
							return -1;
						} else {
							if (n1.getY() < n2.getY()) {
								return -1;
							} else if (n1.getY() > n2.getY()) {
								return 1;
							} else {
								return 0;
							}
						}
					}
				});

				// ラベルをつけながら表示
				boolean forwardLabel = false, backLabel = false;
				for (StateTransition t : trans) {
					// System.out.println(t.to.depth+" : "+t.to);
					if (!forwardLabel && t.from.depth < t.to.depth) {
						forwardLabel = true;
						JLabel l = new JLabel("forward");
						l.setHorizontalAlignment(JLabel.LEFT);
						add(l);
					}
					if (!backLabel && t.from.depth >= t.to.depth) {
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
			if (buttonPanel.autoRun.isSelected()) {
				rulePanel.setEnabled(false);
			} else {
				rulePanel.setEnabled(true);
			}
			updateUI();
			// validate();
		}

		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			for (RuleLinePanel rlp : ruleLinePanels) {
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

		RuleLinePanel(StateTransition trans) {
			this.trans = trans;
			this.node = trans.to;
			while (node.dummy) {
				node = node.getToNode();
			}

			setLayout(new BorderLayout());
			setMaximumSize(new Dimension(10000, 28));

			state = new JTextField(node.toString());
			state.setBackground(Color.white);
			add(state, BorderLayout.CENTER);

			btn = new JButton("Apply (" + trans.getRuleNameString() + ")");
			btn.addActionListener(this);
			add(btn, BorderLayout.WEST);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();

			if (src == btn) {
				applyNode(node, trans);
			}
		}

		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			btn.setEnabled(enabled);
		}

		public void doClick() {
			applyNode(node, trans);
		}
	}

	class OutputPanel extends JPanel {
		private DefaultStyledDocument doc;
		private JScrollPane jsp;
		private JTextPane log;

		OutputPanel() {
			doc = new DefaultStyledDocument();
			log = new JTextPane(doc);
			log.setEditable(false);
			jsp = new JScrollPane(log);
			jsp.getVerticalScrollBar().setUnitIncrement(15);

			setLayout(new BorderLayout());
			add(jsp, BorderLayout.CENTER);
		}

		public void println(String str) {
			SimpleAttributeSet attribute = new SimpleAttributeSet();
			attribute.addAttribute(StyleConstants.Foreground, Color.BLACK);
			println(str, attribute);
		}

		public void errPrintln(String str) {
			SimpleAttributeSet attribute = new SimpleAttributeSet();
			attribute.addAttribute(StyleConstants.Foreground, Color.RED);
			println(str, attribute);
		}

		// スレッドセーフ
		public void println(final String str, final SimpleAttributeSet attribute) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						doc.insertString(doc.getLength(), str + "\n", attribute);
						log.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});
		}

		// logのテキストを取得
		public String getText() {
			return log.getText();
		}

		public void clear() {
			log.setText("");
		}
	}

	class AutoRunMover extends Thread {
		private long interval;
		private boolean active;
		long sleepTime = 0;

		AutoRunMover(int interval) {
			this.interval = interval;
		}

		public void setInterval(int interval) {
			this.interval = interval;
		}

		public void setActive(boolean active) {
			this.active = active;
			sleepTime = System.currentTimeMillis();
		}

		public boolean isActive() {
			return active;
		}

		public void resetClock() {
			sleepTime = System.currentTimeMillis();
		}

		public void run() {
			while (true) {
				try {
					if (active) {
						selectRule();
					}
					while (System.currentTimeMillis() < sleepTime + interval) {
						sleep(50);
					}
					sleepTime = System.currentTimeMillis();
				} catch (Exception e) {
				}
			}

		}

		void selectRule() {
			for (StateRule rule : priorityRules) {
				for (RuleLinePanel rlp : rulePanel.ruleLinePanels) {
					if (rlp.trans.getRules().contains(rule)) {
						rlp.doClick();
						return;
					}
				}
			}
			for (RuleLinePanel rlp : rulePanel.ruleLinePanels) {
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

		public NumberingStateTransitionRuleFrame() {

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Rules");
			setIconImages(Env.getApplicationIcons());
			setAlwaysOnTop(true);
			setResizable(false);

			panel = new JPanel();
			panel.setLayout(new BorderLayout());

			rulePanel = new SelectPanel();
			panel.add(rulePanel, BorderLayout.CENTER);

			btnPanel = new JPanel();
			btnPanel.setLayout(new GridLayout(1, 2));

			ok = new JButton(Env.getMsg(MsgID.text_ok));
			ok.addActionListener(this);
			btnPanel.add(ok);

			cancel = new JButton(Env.getMsg(MsgID.text_cancel));
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
			if (src == ok) {
				priorityRules = rulePanel.getSortedRules();
				if (ruleWindow != null) {
					ruleWindow.dispose();
					ruleWindow = null;
				}
			} else if (src == cancel) {
				if (ruleWindow != null) {
					ruleWindow.dispose();
					ruleWindow = null;
				}
			}
		}

		private class SelectPanel extends JPanel {
			ArrayList<RuleLine> ruleLines;

			SelectPanel() {
				ruleLines = new ArrayList<RuleLine>();
				Set<StateRule> rules = statePanel.stateGraphPanel.getDrawNodes().getRules();
				setLayout(new GridLayout(rules.size(), 1));
				for (StateRule r : rules) {
					RuleLine rl = new RuleLine(r, rules.size());
					ruleLines.add(rl);
					add(rl);
				}
				for (int i = 0; i < priorityRules.size(); ++i) {
					for (RuleLine rl : ruleLines) {
						if (priorityRules.get(i) == rl.rule) {
							rl.setRank(priorityRules.size() - i);
						}
					}
				}
			}

			ArrayList<StateRule> getSortedRules() {
				Collections.sort(ruleLines, new Comparator<RuleLine>() {
					public int compare(RuleLine t1, RuleLine t2) {
						if (t1.getRank() < t2.getRank()) {
							return 1;
						} else if (t1.getRank() > t2.getRank()) {
							return -1;
						} else {
							return 0;
						}
					}
				});
				ArrayList<StateRule> sortedRules = new ArrayList<StateRule>();
				for (RuleLine rl : ruleLines) {
					if (rl.getRank() > 0) {
						sortedRules.add(rl.rule);
					}
				}
				return sortedRules;
			}

		}

		private class RuleLine extends JPanel {
			StateRule rule;
			JComboBox<String> box = new JComboBox<String>();

			RuleLine(StateRule rule, int ruleCount) {
				this.rule = rule;

				setLayout(new GridLayout(1, 2));

				JLabel label = new JLabel(rule.getName());
				add(label);

				box = new JComboBox<String>();
				for (int i = 0; i < ruleCount; ++i) {
					box.addItem("" + i);
				}
				add(box);
			}

			void setRank(int r) {
				box.setSelectedItem("" + r);
			}

			int getRank() {
				return Integer.parseInt(box.getSelectedItem().toString());
			}
		}

	}

}
