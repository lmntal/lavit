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
import lavit.stateviewer.s3d.State3DNode;
import lavit.stateviewer.s3d.State3DPanel;

public class State3DDynamicMover extends Thread {
	private State3DPanel panel;
	private boolean active;

	private boolean physicsMode;

	private double k;
	private double nc;
	private double dc;
	private long interval;
	private int maxSpeed;

	public State3DDynamicMover(State3DPanel panel){
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

	public void setPhysicsMode(boolean physicsMode){
		this.physicsMode = physicsMode;
	}

	public void run(){
		long sleepTime = 0;
		while(true){
			try{

				if(active){
					for(State3DNode node : panel.getAllNode()){
						node.setDDY(0);
						node.setDDZ(0);
					}

					double springLength = 30;

					if(panel.getDepth()>=2){
						springLength = panel.getDepthNode().get(1).get(0).getX() - panel.getDepthNode().get(0).get(0).getX();
					}

					//ばね
					for(State3DNode node : panel.getAllNode()){
						for(State3DNode to : node.getToNodes()){
							double l = node.distance(to);
							if(l==0){ continue; }
							double f = -1.0 * (l-springLength)*k;
							double rateY = (node.getY()-to.getY())/l;
							node.addDDY(f*rateY);
							double rateZ = (node.getZ()-to.getZ())/l;
							node.addDDZ(f*rateZ);
						}
						for(State3DNode from : node.getFromNodes()){
							double l = node.distance(from);
							if(l==0){ continue; }
							double f = -1.0 * (l-springLength)*k;
							double rateY = (node.getY()-from.getY())/l;
							node.addDDY(f*rateY);
							double rateZ = (node.getZ()-from.getZ())/l;
							node.addDDZ(f*rateZ);
						}
					}

					//斥力
					/*
					int d = 10;
					for(ArrayList<State3DNode> nodes : panel.getDepthNode()){
						for(State3DNode node : nodes){
							for(State3DNode n : nodes){
								if(node.getID()==n.getID()) continue;
								double l = node.distance(n);
								if(l==0){ continue; }
								double ry = (node.getY()-n.getY())/l;
								if(ry!=0){
									if(0<ry&&ry<d){ ry=d; }
									if(-d<ry&&ry<0){ ry=-d; }
									double f;
									if(ry>0){
										f = ((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(ry*ry);
									}else{
										f = -((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(ry*ry);
									}
									node.addDDY(f);
								}
								double rz = (node.getZ()-n.getZ())/l;
								if(rz!=0){
									if(0<rz&&rz<d){ rz=d; }
									if(-d<rz&&rz<0){ rz=-d; }
									double f;
									if(rz>0){
										f = ((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(rz*rz);
									}else{
										f = -((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(rz*rz);
									}
									node.addDDZ(f);
								}
							}
						}
					}
					*/

					int d = 10;
					for(State3DNode node : panel.getAllNode()){
						for(State3DNode n : panel.getAllNode()){
							if(node.getID()==n.getID()) continue;
							double l = node.distance(n);
							if(l==0){ continue; }
							double ry = (node.getY()-n.getY())/l;
							if(ry!=0){
								if(0<ry&&ry<d){ ry=d; }
								if(-d<ry&&ry<0){ ry=-d; }
								double f;
								if(ry>0){
									f = ((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(ry*ry);
								}else{
									f = -((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(ry*ry);
								}
								node.addDDY(f);
							}
							double rz = (node.getZ()-n.getZ())/l;
							if(rz!=0){
								if(0<rz&&rz<d){ rz=d; }
								if(-d<rz&&rz<0){ rz=-d; }
								double f;
								if(rz>0){
									f = ((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(rz*rz);
								}else{
									f = -((node.isDummy()?dc:nc)+(n.isDummy()?dc:nc))/(rz*rz);
								}
								node.addDDZ(f);
							}
						}
					}

					//摩擦力
					for(State3DNode node : panel.getAllNode()){
						node.addDDY(-0.7 * (node.getDY()+node.getDDY()));
						node.addDDZ(-0.7 * (node.getDZ()+node.getDDZ()));
					}

					//移動
					for(State3DNode node : panel.getAllNode()){
						node.move(maxSpeed);
					}
					panel.statePanel.stateGraphPanel.repaint();
					while(System.currentTimeMillis()<sleepTime+interval){
						sleep(1);
					}
					sleepTime=System.currentTimeMillis();
				}else{
					sleep(300);
				}
			} catch (Exception e) {
				e.printStackTrace();
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