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

import java.util.ArrayList;

import lavit.*;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;

public class StateDynamicMover extends Thread {
	private StateGraphPanel panel;
	private boolean active;

	private double k;
	private double nc;
	private double dc;
	private long interval;
	private int maxSpeed;

	public StateDynamicMover(StateGraphPanel panel){
		this.panel = panel;
		this.active = false;
	}

	public void setInnerSpring(int k){
		this.k = (double)k/100.0;
	}

	public void setInnerNodeRepulsion(int nc){
		this.nc = (double)nc*10.0;
	}

	public void setInnerDummyRepulsion(int dc){
		this.dc = (double)dc*10.0;
	}

	public void setInnerInterval(int interval){
		this.interval = interval;
	}

	public void setInnerMaxSpeed(int maxSpeed){
		this.maxSpeed = maxSpeed*10;
	}

	public void run(){
		while(true){
			StateNodeSet drawNodes = panel.getDrawNodes();
			if(active){
				double springLength = 30;
				if(drawNodes.getDepth()>=2){
					springLength = drawNodes.getDepthNode().get(1).get(0).getX() - drawNodes.getDepthNode().get(0).get(0).getX();
				}

				//ばね
				for(StateNode node : drawNodes.getAllNode()){
					for(StateNode to : node.getToNodes()){
						double l = Math.sqrt((node.getX()-to.getX())*(node.getX()-to.getX())+(node.getY()-to.getY())*(node.getY()-to.getY()));
						if(l==0){ continue; }
						double f = -1.0 * (l-springLength)*k;
						double rateY = (node.getY()-to.getY())/l;
						node.ddy += f*rateY;
					}
					for(StateNode from : node.getFromNodes()){
						double l = Math.sqrt((node.getX()-from.getX())*(node.getX()-from.getX())+(node.getY()-from.getY())*(node.getY()-from.getY()));
						if(l==0){ continue; }
						double f = -1.0 * (l-springLength)*k;
						double rateY = (node.getY()-from.getY())/l;
						node.ddy += f*rateY;
					}
				}

				//斥力
				int d = 10;
				for(ArrayList<StateNode> nodes : drawNodes.getDepthNode()){
					for(StateNode node : nodes){
						for(StateNode n : nodes){
							if(node.id==n.id) continue;
							double r = node.getY()-n.getY();
							if(r==0) continue;
							if(0<r&&r<d){ r=d; }
							if(-d<r&&r<0){ r=-d; }
							double f;
							if(r>0){
								f = ((node.dummy?dc:nc)+(n.dummy?dc:nc))/(r*r);
							}else{
								f = -((node.dummy?dc:nc)+(n.dummy?dc:nc))/(r*r);
							}
							/*
							r/=20;
							if(-1<r&&r<0){
								f = -1*(5*r*r*r/4-19*r*r/8+9/8);
							}else if(0<r&&r<1){
								f = 1*(5*r*r*r/4-19*r*r/8+9/8);
							}else{
								f = 0;
							}
							*/
							node.ddy += f;
						}
					}
				}

				//摩擦力
				for(StateNode node : drawNodes.getAllNode()){
					node.ddy += -0.5 * (node.dy+node.ddy);
				}

				//移動
				for(StateNode node : drawNodes.getAllNode()){
					if(panel.getSelectNodes().contains(node)&&panel.isDragg()) continue;
					node.dy += node.ddy;
					if(node.dy>maxSpeed){
						node.move(0,maxSpeed);
					}else if(node.dy<-maxSpeed){
						node.move(0,-maxSpeed);
					}else{
						//dyがおかしくなった場合は0にする
						if(!(-1000000000<node.dy&&node.dy<1000000000)){ node.dy=0; node.ddy=0; }
						node.move(0,node.dy);
					}
				}
				panel.repaint();

			}else{
				try {
					sleep(100);
				} catch (InterruptedException e) {
					FrontEnd.printException(e);
				}
			}

			if(interval>0){
				try {
					sleep(interval);
				} catch (InterruptedException e) {
					FrontEnd.printException(e);
				}
			}

		}
	}

	public void setActive(boolean active){
		this.active = active;
	}

	public boolean isActive(){
		return active;
	}

}