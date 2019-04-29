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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.frame.ChildWindowListener;
import lavit.localizedtext.MsgID;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNodeSet;

public class StateGraphGeneticAlgorithmWorker extends SwingWorker<Object, Double> {
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;
	private boolean changeActive;

	private ProgressFrame frame;

	private boolean cross = true;

	public StateGraphGeneticAlgorithmWorker(StateGraphPanel panel) {
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
		this.changeActive = true;
	}

	public void ready() {
		if (changeActive)
			panel.setActive(false);
		frame = new ProgressFrame();
	}

	public void end() {
		panel.autoCentering();
		if (changeActive)
			panel.setActive(true);
		frame.dispose();
	}

	@Override
	protected Object doInBackground() {

		int size = 100;
		ArrayList<StatePositionSet> sets = new ArrayList<StatePositionSet>();
		StatePositionSet best = new StatePositionSet(drawNodes);
		sets.add(best);

		// 初期遺伝子は次々と突然変異させて準備
		for (int i = 1; i < size; ++i) {
			best = new StatePositionSet(best);
			best.mutation2();
			sets.add(best);
		}
		best = sortSets(sets);
		publish(getParam(best));

		while (!isCancelled()) {

			// 交叉
			ArrayList<StatePositionSet> children = new ArrayList<StatePositionSet>();
			for (int i = 0; i < size / 2; ++i) {
				int index1 = 0;
				int index2 = 0;
				while (index1 == index2 && sets.size() >= 2) {
					index1 = (int) (Math.random() * sets.size());
					index2 = (int) (Math.random() * sets.size());
				}
				ArrayList<StatePositionSet> cs = hybridization(sets.get(index1), sets.get(index2));
				for (StatePositionSet c : cs) {
					children.add(c);
				}
			}

			// 突然変異
			double mutation1 = 0.1;
			double mutation2 = 0.1;
			for (StatePositionSet s : sets) {
				if (Math.random() < mutation1) {
					StatePositionSet m = new StatePositionSet(s);
					m.mutation1();
					children.add(m);
				}
				if (Math.random() < mutation2) {
					StatePositionSet m = new StatePositionSet(s);
					m.mutation2();
					children.add(m);
				}
			}

			// 子供の追加
			for (StatePositionSet c : children) {
				sets.add(c);
			}

			// 重複を許さない
			HashSet<String> strset = new HashSet<String>();
			for (int i = 0; i < sets.size();) {
				String s = sets.get(i).toString();
				if (strset.contains(s)) {
					sets.remove(i);
				} else {
					strset.add(s);
					++i;
				}
			}

			// ソート
			best = sortSets(sets);

			// 淘汰開始
			ArrayList<StatePositionSet> selection = new ArrayList<StatePositionSet>();

			// エリート選択
			for (int i = 0; i < size / 2; ++i) {
				if (sets.size() > 0) {
					selection.add(sets.get(0));
					sets.remove(0);
				} else {
					break;
				}
			}

			// ルーレット選択
			double sum = 0;
			for (StatePositionSet s : sets) {
				sum += 100 / getParam(s);
			}
			for (int i = 0; i < size / 2; ++i) {
				if (sets.size() > 0 && sum >= 1) {
					double tempsum = (100 / sum) * Math.random();
					int ti = 0;
					while (tempsum > 0) {
						if (ti >= sets.size()) {
							break;
						}
						tempsum -= getParam(sets.get(ti));
						ti++;
					}
					if (ti >= sets.size()) {
						break;
					}

					sum -= 100 / getParam(sets.get(ti));
					selection.add(sets.get(ti));
					sets.remove(ti);

				} else {
					break;
				}
			}

			// 次世代の作成
			sets.clear();
			for (StatePositionSet s : selection) {
				sets.add(s);
			}

			/*
			 * for(StatePositionSet s : selection){ System.out.print(s.getAllCross()+","); }
			 * System.out.println("\n");
			 */

			publish(getParam(best));
			if (getParam(best) <= 0) {
				break;
			}
		}

		drawNodes.updatePosition(best);

		frame.end();
		end();
		return null;
	}

	private double getParam(StatePositionSet s) {
		if (cross) {
			return (double) s.getAllCross();
		} else {
			return s.getTransitionLength();
		}
	}

	private ArrayList<StatePositionSet> hybridization(StatePositionSet p1, StatePositionSet p2) {
		StatePositionSet c1 = new StatePositionSet(p1);
		StatePositionSet c2 = new StatePositionSet(p2);
		ArrayList<StatePositionSet> children = new ArrayList<StatePositionSet>();
		children.add(c1);
		children.add(c2);

		// ランダムで深さを決定
		// int depth = c1.getRamdomDepth();
		// if(depth==-1) return children;

		for (int depth = 0; depth < p1.getDepth(); ++depth) {

			ArrayList<StatePosition> nodes1 = c1.getDepthNode().get(depth);
			ArrayList<StatePosition> nodes2 = c2.getDepthNode().get(depth);

			// 同じ深さのノード数が違うとエラーで強制終了
			if (nodes1.size() != nodes2.size())
				return children;

			// IDリストを準備
			ArrayList<Long> ids1 = new ArrayList<Long>();
			for (StatePosition s : nodes1) {
				ids1.add(s.id);
			}
			ArrayList<Long> ids2 = new ArrayList<Long>();
			for (StatePosition s : nodes2) {
				ids2.add(s.id);
			}

			// 循環交叉
			int initialIndex = (int) (Math.random() * ids1.size());
			int nextIndex = initialIndex;
			while (true) {
				long id2 = ids2.get(nextIndex);
				ids2.set(nextIndex, ids1.get(nextIndex));
				ids1.set(nextIndex, id2);

				for (int ni = 0; ni < ids1.size(); ++ni) {
					if (ni == nextIndex) {
						continue;
					}
					if (ids1.get(ni) == id2) {
						nextIndex = ni;
						break;
					}
				}
				if (initialIndex == nextIndex) {
					break;
				}
			}

			// IDリストを実ノードに反映
			for (int i = 0; i < ids1.size(); ++i) {
				if (nodes1.get(i).id != ids1.get(i)) {
					for (int j = i + 1; j < ids1.size(); ++j) {
						if (ids1.get(i) == nodes1.get(j).id) {
							c1.swap(depth, i, j);
						}
					}
				}
			}
			for (int i = 0; i < ids2.size(); ++i) {
				if (nodes2.get(i).id != ids2.get(i)) {
					for (int j = i + 1; j < ids2.size(); ++j) {
						if (ids2.get(i) == nodes2.get(j).id) {
							c2.swap(depth, i, j);
						}
					}
				}
			}

		}

		return children;
	}

	private StatePositionSet sortSets(ArrayList<StatePositionSet> sets) {
		Collections.sort(sets, new Comparator<StatePositionSet>() {
			public int compare(StatePositionSet n1, StatePositionSet n2) {

				double tl1 = getParam(n1);
				double tl2 = getParam(n2);
				if (tl1 < tl2) {
					return -1;
				} else if (tl1 > tl2) {
					return 1;
				} else {
					return 0;
				}

				/*
				 * double tl1 = n1.getAllCross(); double tl2 = n2.getAllCross(); if(tl1<tl2){
				 * return -1; }else if(tl1>tl2){ return 1; }else{ double t1 =
				 * n1.getTransitionLength(); double t2 = n2.getTransitionLength(); if(t1<t2){
				 * return -1; }else if(t1>t2){ return 1; }else{ return 0; } }
				 */
			}
		});
		return sets.get(0);
	}

	@Override
	protected void process(List<Double> chunks) {
		for (double number : chunks) {
			frame.setParam(number);
		}
	}

	private class ProgressFrame extends JDialog implements ActionListener {
		private JPanel panel;
		private JLabel label;
		private GraphPanel graph;
		private JButton end;
		private GraphPainter painter;

		private int paramNum = 0;
		private double lastParam = -1;
		private ArrayList<Double> params = new ArrayList<Double>();

		private ProgressFrame() {
			panel = new JPanel();

			panel.setLayout(new BorderLayout());

			label = new JLabel("");
			label.setHorizontalAlignment(JLabel.CENTER);
			panel.add(label, BorderLayout.NORTH);

			graph = new GraphPanel();
			panel.add(graph, BorderLayout.CENTER);

			end = new JButton(Env.getMsg(MsgID.text_end));
			end.addActionListener(this);
			panel.add(end, BorderLayout.SOUTH);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Genetic Algorithm");
			setIconImages(Env.getApplicationIcons());
			setAlwaysOnTop(true);
			setResizable(false);

			label.setText(0 + " : cross = " + 0);

			pack();
			setLocationRelativeTo(panel);
			addWindowListener(new ChildWindowListener(this));
			setVisible(true);

			painter = new GraphPainter();
			painter.start();
		}

		public void end() {
			if (painter != null) {
				painter.interrupt();
				painter = null;
			}
			// System.out.println(paramNum+" : cross = "+(new
			// DecimalFormat("#.###")).format(lastParam));
		}

		public void setParam(double param) {
			paramNum++;
			label.setText(paramNum + " : cross = " + (new DecimalFormat("#.###")).format(param));
			lastParam = param;
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if (src == end) {
				if (!isDone()) {
					cancel(false);
				}
			}
		}

		private class GraphPanel extends JPanel {

			private GraphPanel() {
				setPreferredSize(new Dimension(300, 100));
			}

			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;

				// フレームの初期化
				g2.setColor(Color.white);
				g2.fillRect(0, 0, getWidth(), getHeight());

				g2.setColor(Color.black);
				if (params.size() >= 2) {
					double pw = params.size();
					double base = params.get(0);
					double ph = Math.abs(params.get(params.size() - 1) - params.get(0));
					double h = 1.0;
					double w = 20.0;
					int margin = 5;
					while (w < pw)
						w *= 1.5;
					while (h < ph)
						h *= 1.5;
					for (int x = 0; x < params.size() - 1; ++x) {
						g2.drawLine((int) (x * (getWidth() - margin * 2) / w) + margin,
								(int) (Math.abs(params.get(x) - base) * (getHeight() - margin * 2) / h) + margin,
								(int) ((x + 1) * (getWidth() - margin * 2) / w) + margin,
								(int) (Math.abs(params.get(x + 1) - base) * (getHeight() - margin * 2) / h) + margin);
					}
				}
			}

		}

		private class GraphPainter extends Thread {
			public void run() {
				while (true) {
					if (lastParam >= 0) {
						params.add(lastParam);
					}
					repaint();
					try {
						sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
}
