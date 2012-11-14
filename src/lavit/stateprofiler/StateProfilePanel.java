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
 */package lavit.stateprofiler;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lavit.Env;
import lavit.runner.RunnerOutputGetter;

@SuppressWarnings("serial")
public class StateProfilePanel extends JPanel implements RunnerOutputGetter
{
	private StateProfileGraphPanel graphPanel;
	private StateProfileLabelPanel label;
	private JTextField lastState;
	private Timer paintClock;
	private Timer labelClock;

	private boolean end;

	private int hashConflict;
	private List<State> allState = new ArrayList<State>();
	List<Integer> timeLine = new ArrayList<Integer>();

	public StateProfilePanel()
	{
		setLayout(new BorderLayout());

		graphPanel = new StateProfileGraphPanel(this);
		add(graphPanel, BorderLayout.CENTER);

		label = new StateProfileLabelPanel();
		add(label, BorderLayout.SOUTH);

		lastState = new JTextField();
		lastState.setFont(new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, 9));
		add(lastState, BorderLayout.NORTH);
	}

	private void line(String str)
	{
		String[] ss = str.split("::", 2);
		if (ss.length < 2)
		{
			return;
		}

		long id = Long.parseLong(ss[0]);
		//long hash = Long.parseLong(ss[1]);
		//int successor = Integer.parseInt(ss[2]);
		String s = ss[1];

		State state = new State(id, s);
		allState.add(state);
	}

	public void outputStart(String command, String option, File target)
	{
		hashConflict = 0;
		allState.clear();

		timeLine.clear();
		timeLine.add(0);

		graphPanel.start();
		label.setStatus(0, 0, 0);
		repaint();

		if (paintClock != null)
		{
			paintClock.cancel();
		}
		paintClock = new Timer();
		paintClock.schedule(new PaintTask(), 1000, 1000);

		if (labelClock != null)
		{
			labelClock.cancel();
		}
		labelClock = new Timer();
		labelClock.schedule(new LabelTask(), 200, 200);

		end = false;
	}

	public void outputLine(String str)
	{
		if (str.equals("States")) return;
		if (str.equals("Transitions"))
		{
			end = true;
			return;
		}
		if (end) return;
		line(str);
	}

	public void outputEnd()
	{
		timeLine.add(allState.size());
		int n = timeLine.size();
		if (n >= 2)
		{
			if (timeLine.get(n - 2) == timeLine.get(n - 1))
			{
				timeLine.remove(n - 1);
			}
		}
		paintClock.cancel();
		labelClock.cancel();
		updateStatusLabel();
		graphPanel.end();
		updateLastState();
		repaint();
	}

	private void updateLastState()
	{
		if (!allState.isEmpty())
		{
			final String s = allState.get(allState.size() - 1).str;
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					lastState.setText(s);
				}
			});
		}
	}

	private void updateStatusLabel()
	{
		int states = 0;
		if (timeLine.size() > 1)
		{
			states = allState.size();
		}
		int time = 0;
		if (timeLine.size() > 1)
		{
			time = timeLine.size() - 1;
		}
		int rate = 0;
		if (time > 0)
		{
			rate = states / time;
		}
		label.setStatus(states, time, rate);
	}

	private static class State
	{
		public final long id;
		public final String str;

		public State(long id, String str)
		{
			this.id = id;
			this.str = str;
		}
	}

	private class PaintTask extends TimerTask
	{
		public void run()
		{
			int nowCount = allState.size();
			timeLine.add(nowCount);

			//同じ値が続いたときの補正
			int size = timeLine.size();
			if (size >= 2)
			{
				int oldCount = timeLine.get(size - 2);
				if (nowCount > oldCount)
				{
					int offsetIndex = size - 3;
					for (; offsetIndex >= 0; --offsetIndex)
					{
						if (timeLine.get(offsetIndex) < oldCount)
						{
							break;
						}
					}
					offsetIndex++;
					if (offsetIndex < size - 2)
					{
						int c = size - 1 - offsetIndex;
						int i = 1;
						for (offsetIndex++; offsetIndex < size - 1; offsetIndex++)
						{
							timeLine.set(offsetIndex, oldCount + (nowCount - oldCount) * i / c);
							i++;
						}
					}
				}
			}
			updateLastState();
			repaint();
		}
	}

	private class LabelTask extends TimerTask
	{
		public void run()
		{
			updateStatusLabel();
			repaint();
		}
	}
}
