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

public class StateGraphRandomMoveWorker extends SwingWorker<Object,Double>{
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;

	private ProgressFrame frame;

	public StateGraphRandomMoveWorker(StateGraphPanel panel){
		this.panel = panel;
		this.drawNodes = panel.getDrawNodes();
	}

	public void ready(){
		panel.setActive(false);
		frame = new ProgressFrame();
	}

	public void done() {
		panel.autoCentering();
		panel.setActive(true);
		frame.dispose();
	}

	@Override
	protected Object doInBackground(){

		int size = 10;
		ArrayList<StatePositionSet> sets = new ArrayList<StatePositionSet>(size);
		StatePositionSet best = new StatePositionSet(drawNodes);
		publish(best.getFromBestLength());

		while(!isCancelled()){
			sets.clear();
			sets.add(best);
			for(int i=1;i<size;++i){
				best = new StatePositionSet(best);
				best.mutation2();
				best.randomMove();
				sets.add(best);
			}
			best = sortSets(sets);
			publish(best.getFromBestLength());
		}

		drawNodes.updatePosition(best);

		double w = (double)panel.getWidth();
		double h = (double)panel.getHeight();
		double xInterval = w/(drawNodes.getDepth()+1);
		double yInterval = h/(drawNodes.getHeight()+1);
		if(xInterval>30){ xInterval=30; }else if(xInterval<10){ xInterval=10; }
		if(yInterval>30){ yInterval=30; }else if(yInterval<10){ yInterval=10; }

		//等間隔x配置
		for(StateNode node : drawNodes.getAllNode()){
			node.setX((node.depth+1)*xInterval);
		}

		while(true){
			Rectangle2D.Double d = drawNodes.getNodesDimension();
			if(d.getHeight()<drawNodes.getHeight()*yInterval){
				drawNodes.allScaleCenterMove(1, 1.1);
			}else{
				break;
			}
		}
		while(true){
			Rectangle2D.Double d = drawNodes.getNodesDimension();
			if(d.getHeight()>drawNodes.getHeight()*yInterval){
				drawNodes.allScaleCenterMove(1, 0.9);
			}else{
				break;
			}
		}

		panel.autoCentering();
		frame.end();
		return null;
	}

	private StatePositionSet sortSets(ArrayList<StatePositionSet> sets){
		Collections.sort(sets, new Comparator<StatePositionSet>() {
			public int compare(StatePositionSet n1, StatePositionSet n2) {
				double tl1 = n1.getFromBestLength();
				double tl2 = n2.getFromBestLength();
				if(tl1<tl2){
					return -1;
				}else if(tl1>tl2){
					return 1;
				}else{
					return 0;
				}
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

		private ProgressFrame(){
			panel = new JPanel();

			panel.setLayout(new BorderLayout());

			label = new JLabel("");
			label.setHorizontalAlignment(JLabel.CENTER);
			panel.add(label, BorderLayout.NORTH);

			graph = new GraphPanel();
			panel.add(graph, BorderLayout.CENTER);

			end = new JButton(Lang.d[7]);
			end.addActionListener(this);
			panel.add(end, BorderLayout.SOUTH);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Genetic Algorithm");
			setIconImage(Env.getImageOfFile("img/icon.gif"));
			setAlwaysOnTop(true);
			setResizable(false);

	        pack();
	        setLocationRelativeTo(panel);
	        addWindowListener(new ChildWindowListener(this));
	        setVisible(true);

	        painter = new GraphPainter();
	        painter.start();
		}

		public void end(){
			if(painter!=null){
				painter.interrupt();
				painter = null;
			}
		}

		public void setParam(double param){
			paramNum++;
			lastParam = param;
			label.setText(paramNum+" : "+(new DecimalFormat("#.###")).format(param));
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==end){
				if(!isDone()){
					cancel(true);
				}
			}
		}

		private class GraphPanel extends JPanel {

			private GraphPanel(){
				setPreferredSize(new Dimension(300, 100));
			}

			public void paintComponent(Graphics g){
				Graphics2D g2 = (Graphics2D)g;

				//フレームの初期化
				g2.setColor(Color.white);
				g2.fillRect(0, 0, getWidth(), getHeight());

				g2.setColor(Color.black);
				if(params.size()>=2){
					double pw = params.size();
					double base = params.get(0);
					double ph = Math.abs(params.get(params.size()-1)-params.get(0));
					double h = 1.0;
					double w = 20.0;
					int margin = 5;
					while(w<pw) w *= 1.5;
					while(h<ph) h *= 1.5;
					for(int x=0;x<params.size()-1;++x){
						g2.drawLine((int)(x*(getWidth()-margin*2)/w)+margin, (int)(Math.abs(params.get(x)-base)*(getHeight()-margin*2)/h)+margin, (int)((x+1)*(getWidth()-margin*2)/w)+margin, (int)(Math.abs(params.get(x+1)-base)*(getHeight()-margin*2)/h)+margin);
					}
				}
			}

		}

		private class GraphPainter extends Thread {
			public void run(){
				while(true){
					if(lastParam>=0){
						params.add(lastParam);
					}
					repaint();
					try {
						sleep(200);
					} catch (InterruptedException e) {
						FrontEnd.printException(e);
					}
				}
			}
		}
	}
}
