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

package lmntaleditor.stateviewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import lmntaleditor.Env;
import lmntaleditor.FrontEnd;
import lmntaleditor.Lang;
import lmntaleditor.frame.ChildWindowListener;

public class StateGraphDummySmoothingWorker extends SwingWorker<Object,Object>{
	private StateGraphPanel panel;
	private StateNodeSet drawNodes;

	private ProgressFrame frame;

	public StateGraphDummySmoothingWorker(StateGraphPanel panel){
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

		boolean move = true;
		while(move){
			move = false;
			ArrayList<ArrayList<StateNode>> depthNode = drawNodes.getDepthNode();
			for(ArrayList<StateNode> nodes : depthNode){
				for(StateNode node : nodes){
					if(node.dummy){
						double y = node.getY();
						double ay = (node.getFromNodes().get(0).getY()+node.getToNodes().get(0).getY())/2;
						node.setY(ay);
						if(rideOtherNode(nodes,node)){
							if(ay>y){
								ay = y+1;
							}else{
								ay = y-1;
							}
							node.setY(ay);
							if(rideOtherNode(nodes,node)){
								node.setY(y);
							}
						}
						if(Math.abs(node.getY()-y)>0.9){
							move = true;
						}
					}
					if(isCancelled()){ return null; }
				}
			}
		}

		return null;
	}

	boolean rideOtherNode(ArrayList<StateNode> nodes,StateNode node){
		for(StateNode n : nodes){
			if(n==node) continue;
			double dy = node.getY()-n.getY();
			if(dy<0){ dy *= -1; }
			double r = node.getRadius()+n.getRadius()+7;
			if(dy<r){
				return true;
			}
		}
		return false;
	}

	private class ProgressFrame extends JDialog implements ActionListener {
		private JPanel panel;
		private JProgressBar bar;
		private JButton cancel;

		private ProgressFrame(){
			panel = new JPanel();

			bar = new JProgressBar(0,100);
			bar.setIndeterminate(true);
			panel.add(bar);

			cancel = new JButton(Lang.d[2]);
			cancel.addActionListener(this);
			panel.add(cancel);

			add(panel);

			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setTitle("Dummy Smoothing");
			setIconImage(Env.getImageOfFile("img/icon.gif"));
			setAlwaysOnTop(true);
			setResizable(false);

	        pack();
	        setLocationRelativeTo(panel);
	        addWindowListener(new ChildWindowListener(this));
	        setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==cancel){
				if(!isDone()){
					cancel(true);
				}
			}
		}
	}


}
