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

package lavit.stateviewer.worker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.StopWatch;
import lavit.frame.ChildWindowListener;
import lavit.localizedtext.MsgID;
import lavit.stateviewer.StateAbstractionMaker;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;
import lavit.stateviewer.StateRule;
import lavit.stateviewer.StateTransition;

public class StateTransitionAbstractionWorker extends SwingWorker<Object, Object> {
	private StateGraphPanel panel;
	private boolean endFlag;
	private boolean changeActive;

	private ProgressFrame frame;

	private StateAbstractionMaker maker;
	private Collection<StateRule> rules;
	private Collection<StateTransition> trans;

	public StateTransitionAbstractionWorker(StateGraphPanel panel) {
		this.panel = panel;
		this.endFlag = false;
		this.changeActive = true;
	}

	public void waitExecute(Collection<StateRule> rules) {
		this.changeActive = false;
		selectExecute(rules);
		while (!endFlag) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	public void selectExecute(Collection<StateRule> rules) {
		if (panel.getDrawNodes().size() < 1000) {
			atomic(rules);
		} else {
			ready(rules);
			execute();
		}
	}

	public void atomic(Collection<StateRule> rules) {
		ready(rules, false);
		doInBackground();
		done();
	}

	public void ready(Collection<StateRule> rules) {
		ready(rules, true);
	}

	public void ready(Collection<StateRule> rules, boolean open) {
		if (changeActive)
			panel.setActive(false);

		if (open) {
			frame = new ProgressFrame();
			addPropertyChangeListener(frame);
		}

		this.maker = new StateAbstractionMaker(panel);
		this.rules = rules;
		this.trans = new LinkedHashSet<StateTransition>();

		// ダミーが含まれるため再構築
		for (StateTransition t : panel.getDrawNodes().getAllTransition()) {
			rule: for (StateRule r : t.getRules()) {
				if (rules.contains(r)) {
					trans.add(t);
					break rule;
				}
			}
		}
	}

	public void end() {
		maker.end();
		if (changeActive)
			panel.setActive(true);
		if (frame != null)
			frame.dispose();
		this.endFlag = true;
	}

	@Override
	protected Object doInBackground() {

		StateNodeSet drawNodes = panel.getDrawNodes();

		drawNodes.allNodeUnMark();
		LinkedHashSet<StateNode> nodes = new LinkedHashSet<StateNode>();
		for (StateTransition t : trans) {
			if (!t.from.isMarked()) {
				nodes.add(t.from);
				t.from.mark();
			}
			if (!t.to.isMarked()) {
				nodes.add(t.to);
				t.to.mark();
			}
		}

		int allNum = nodes.size();
		drawNodes.allNodeUnMark();

		while (true) {

			StopWatch.startWatch("Worker[1]");

			LinkedHashSet<StateNode> transitionGroup = new LinkedHashSet<StateNode>();
			while (nodes.size() > 0) {

				transitionGroup.clear();
				LinkedList<StateNode> queue = new LinkedList<StateNode>();

				StateNode firstNode = null;
				for (StateNode node : nodes) {
					firstNode = node;
					break;
				}

				queue.add(firstNode);
				transitionGroup.add(firstNode);
				firstNode.mark();
				nodes.remove(firstNode);

				while (!queue.isEmpty()) {

					StateNode node = queue.remove();

					StopWatch.startWatch("Worker[1-1]");
					Collection<StateNode> ns = node.getRuleNameGroupNodes(rules);
					StopWatch.stopWatch("Worker[1-1]");

					for (StateNode n : ns) {
						if (n.isMarked()) {
							continue;
						}
						queue.add(n);
						transitionGroup.add(n);
						n.mark();
						nodes.remove(n);
					}
				}

				if (transitionGroup.size() >= 2) {
					break;
				}
			}

			if (transitionGroup.size() <= 1) {
				break;
			}

			StopWatch.stopWatch("Worker[1]");
			StopWatch.startWatch("Worker[2]");

			maker.makeNode(transitionGroup);

			StopWatch.stopWatch("Worker[2]");

			setProgress((int) (100 * (1 - ((double) nodes.size()) / ((double) allNum))));
			if (isCancelled()) {
				end();
				return null;
			}

		}

		if (frame != null)
			frame.end();
		end();
		return null;
	}

	StateTransition getInTransition(ArrayList<StateTransition> toes, StateNode toNode) {
		for (StateTransition trans : toes) {
			if (trans.to == toNode) {
				return trans;
			}
		}
		return null;
	}

	@SuppressWarnings("serial")
	private class ProgressFrame extends JDialog implements PropertyChangeListener, ActionListener {
		private JPanel panel;
		private JProgressBar bar;
		private JButton cancel;

		private ProgressFrame() {
			panel = new JPanel();

			bar = new JProgressBar(0, 100);
			bar.setStringPainted(true);
			panel.add(bar);

			cancel = new JButton(Env.getMsg(MsgID.text_cancel));
			cancel.addActionListener(this);
			panel.add(cancel);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Transition Abstraction");
			setIconImages(Env.getApplicationIcons());
			setAlwaysOnTop(true);
			setResizable(false);

			pack();
			setLocationRelativeTo(panel);
			addWindowListener(new ChildWindowListener(this));
			setVisible(true);
		}

		public void end() {
			bar.setValue(100);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress".equals(evt.getPropertyName())) {
				bar.setValue((Integer) evt.getNewValue());
			}
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == cancel) {
				if (!isDone()) {
					cancel(false);
				}
			}
		}
	}

}
