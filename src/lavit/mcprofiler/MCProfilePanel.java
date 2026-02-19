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
 */package lavit.mcprofiler;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.JPanel;

import lavit.runner.RunnerOutputGetter;

@SuppressWarnings("serial")
public class MCProfilePanel extends JPanel implements RunnerOutputGetter {

	private MCProfileGraphPanel graphPanel;
	private MCProfileLabelPanel label;
	private Timer clock;

	int hashConflict;
	ArrayList<State> allState;
	TreeMap<Long, ArrayList<State>> hashes;
	TreeMap<Integer, ArrayList<State>> suces;

	ArrayList<Integer[]> timeLine;

	public MCProfilePanel() {
		setLayout(new BorderLayout());

		allState = new ArrayList<State>();
		hashes = new TreeMap<Long, ArrayList<State>>();
		suces = new TreeMap<Integer, ArrayList<State>>();

		timeLine = new ArrayList<Integer[]>();

		graphPanel = new MCProfileGraphPanel(this);
		add(graphPanel, BorderLayout.CENTER);

		label = new MCProfileLabelPanel(this);
		add(label, BorderLayout.SOUTH);
	}

	void line(String str) {
		String ss[] = str.split(":", 4);
		if (ss.length < 4) {
			return;
		}

		long id = Long.parseLong(ss[0]);
		long hash = Long.parseLong(ss[1]);
		int successor = Integer.parseInt(ss[2]);
		// String s = ss[3];

		State state = new State(id, hash, successor, null);
		allState.add(state);

		// ハッシュ
		if (hashes.containsKey(hash)) {
			hashConflict++;
			hashes.get(hash).add(state);
		} else {
			ArrayList<State> l = new ArrayList<State>();
			l.add(state);
			hashes.put(hash, l);
		}

		// 遷移先数
		if (suces.containsKey(successor)) {
			suces.get(successor).add(state);
		} else {
			ArrayList<State> l = new ArrayList<State>();
			l.add(state);
			suces.put(successor, l);
		}
	}

	public void outputStart(String command, String option, File target) {
		hashConflict = 0;
		allState.clear();
		hashes.clear();
		suces.clear();

		timeLine.clear();
		timeLine.add(new Integer[] { 0, 0, 0 });

		graphPanel.start();
		label.update();
		repaint();

		clock = new Timer();
		clock.schedule(new Clock(), 1000, 1000);
	}

	public void outputLine(String str) {
		line(str);
	}

	public void outputEnd() {
		timeLine.add(new Integer[] { allState.size(), hashes.size(), hashConflict });
		clock.cancel();
		label.update();
		graphPanel.end();
		repaint();
	}

	class State {
		long id;
		long hash;
		int successor;
		String str;

		State(long id, long hash, int successor, String str) {
			this.id = id;
			this.hash = hash;
			this.successor = successor;
			this.str = str;
		}
	}

	class Clock extends TimerTask {
		public void run() {
			timeLine.add(new Integer[] { allState.size(), hashes.size(), hashConflict });
			label.update();
			repaint();
		}
	}

}
