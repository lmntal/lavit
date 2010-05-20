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

package lavit.stateviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.frame.ChildWindowListener;

public class StateGraphStretchMoveWorker extends SwingWorker<Object,Integer>{
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;

	private ProgressFrame frame;

	public StateGraphStretchMoveWorker(StateGraphPanel panel){
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
	}

	public void ready(){
		frame = new ProgressFrame();
		frame.lastParam = (new StateGraphExchangeWorker(panel)).getAllCross();
	}

	public void done() {
		frame.dispose();
	}

	@Override
	protected Object doInBackground(){
		int i = 0;
		while(true){
			if(i<10){
				drawNodes.allScaleCenterMove(1/0.8, 0.9);
			}else if(i<20){
				drawNodes.allScaleCenterMove(0.8, 1/0.8);
			}else if(i<30){
				drawNodes.allScaleCenterMove(1/0.9, 0.9);
			}else if(i<40){
				drawNodes.allScaleCenterMove(0.9, 1/0.8);
			}else if(i==40){
				panel.autoCentering();
				sleep(5000);
			}else if(drawNodes.getHeight()*30<drawNodes.getNodesDimension().getHeight()&&i<200){
				drawNodes.allScaleCenterMove(1, 0.95);
			}else{
				drawNodes.allScaleCenterMove(1.1, 1);
				panel.autoCentering();
				publish((new StateGraphExchangeWorker(panel)).getAllCross());
				sleep(5000);
				i=-1;
				if(isCancelled()){ break; }
			}
			i++;
			panel.update();
			sleep(50);
		}
		panel.autoCentering();
		frame.end();
		return null;
	}

	private void sleep(int msec){
		if(isCancelled()) return;
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			FrontEnd.printException(e);
		}
	}

	@Override
    protected void process(List<Integer> chunks) {
        for (int number : chunks) {
            frame.setParam(number);
        }
    }

	private class ProgressFrame extends JDialog implements ActionListener {
		private JPanel panel;
		private JLabel label;
		private JButton end;

		private int paramNum = 0;
		private int lastParam = -1;
		private ArrayList<Integer> params = new ArrayList<Integer>();

		private ProgressFrame(){
			panel = new JPanel();

			panel.setLayout(new BorderLayout());

			label = new JLabel("");
			label.setHorizontalAlignment(JLabel.CENTER);
			panel.add(label, BorderLayout.NORTH);

			end = new JButton(Lang.d[7]);
			end.addActionListener(this);
			panel.add(end, BorderLayout.SOUTH);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Stretch Move");
			setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
			setAlwaysOnTop(true);
			setResizable(false);

			label.setText(0+" : cross = "+0);
			label.setPreferredSize(new Dimension(160,20));

	        pack();
	        setLocationRelativeTo(panel);
	        addWindowListener(new ChildWindowListener(this));
	        setVisible(true);
		}

		public void end(){
		}

		public void setParam(int param){
			paramNum++;
			params.add(param);
			int s = param-lastParam;
			label.setText(paramNum+" : cross = "+param+(lastParam>=0?" ("+(s>=0?"+":"")+s+")":""));
			lastParam = param;
			repaint();
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==end){
				if(!isDone()){
					cancel(true);
				}
			}
		}
	}
}
