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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import lavit.Env;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateNodeSet;

public class StatePositionSet {

	private HashMap<Long,StatePosition> allNode = new HashMap<Long,StatePosition>();
	private ArrayList<ArrayList<StatePosition>> depthNode = new ArrayList<ArrayList<StatePosition>>();

	public StatePositionSet(StateNodeSet stateNodeSet){

		//位置情報の作成
		for(StateNode node : stateNodeSet.getAllNode()){
			StatePosition n = new StatePosition(node);
			allNode.put(node.id, n);
		}

		//深さ情報の作成
		for(int depth=0; depth<stateNodeSet.getDepth();++depth){
			ArrayList<StatePosition> dn = new ArrayList<StatePosition>();
			for(StateNode node : stateNodeSet.getDepthNode().get(depth)){
				dn.add(allNode.get(node.id));
			}
			depthNode.add(depth, dn);
		}

		//遷移先情報の追加
		for(StateNode node : stateNodeSet.getAllNode()){
			StatePosition n = allNode.get(node.id);
			for(StateNode to : node.getToNodes()){
				n.toNodes.add(allNode.get(to.id));
			}
			for(StateNode from : node.getFromNodes()){
				n.fromNodes.add(allNode.get(from.id));
			}
		}

	}

	public StatePositionSet(StatePositionSet statePositionSet){

		//位置情報の作成
		for(StatePosition node : statePositionSet.getAllNode()){
			StatePosition n = new StatePosition(node);
			allNode.put(node.id, n);
		}

		//深さ情報の作成
		for(int depth=0; depth<statePositionSet.getDepth(); ++depth){
			ArrayList<StatePosition> dn = new ArrayList<StatePosition>();
			for(StatePosition node : statePositionSet.getDepthNode().get(depth)){
				dn.add(allNode.get(node.id));
			}
			depthNode.add(depth, dn);
		}

		//遷移先情報の追加
		for(StatePosition node : statePositionSet.getAllNode()){
			StatePosition n = allNode.get(node.id);
			for(StatePosition to : node.toNodes){
				n.toNodes.add(allNode.get(to.id));
			}
			for(StatePosition from : node.fromNodes){
				n.fromNodes.add(allNode.get(from.id));
			}
		}

	}

	public int getDepth(){
		return depthNode.size();
	}

	public Collection<StatePosition> getAllNode(){
		return allNode.values();
	}

	public ArrayList<ArrayList<StatePosition>> getDepthNode(){
		return depthNode;
	}

	public double getY(long id){
		return allNode.get(id).y;
	}

	public int getRamdomDepth(){
		//ランダムに深さを決定
		ArrayList<Integer> depths = new ArrayList<Integer>();
		for(int i=0;i<depthNode.size();++i){
			depths.add(i);
		}
		Collections.shuffle(depths);
		for(int depth : depths){
			if(depthNode.get(depth).size()>=2){
				return depth;
			}
		}
		return -1;
	}

	public void mutation1(){

		//２個以上ノードがある深さをランダムに決定
		int depth = getRamdomDepth();
		if(depth==-1){ return; }

		//ランダムにノードを選択
		int index1=0;
		int index2=0;
		int size = depthNode.get(depth).size();
		while(index1==index2&&size>=2){
			index1 = (int)(Math.random()*size);
			index2 = (int)(Math.random()*size);
		}

		//場所を入れ替える
		swap(depth, index1, index2);
	}

	public void mutation2(){
		//全深さを見て、確率でランダムシャッフル
		for(int depth=1;depth<getDepth();++depth){
			if(Math.random()>0.5){ continue; }

			int size = depthNode.get(depth).size();
			if(size<2){ continue; }

			for(int i=0;i<size;++i){
				swap(depth, i, (int)(Math.random()*size));
			}
		}
	}

	public void swap(int depth, int index1, int index2){
		if(index1==index2){ return; }

		//場所を入れ替える
		ArrayList<StatePosition> nodes = depthNode.get(depth);

		StatePosition n1 = nodes.get(index1);
		StatePosition n2 = nodes.get(index2);

		if(Env.is("SV_CROSSREDUCTION_DUMMYONLY")&&!(n1.dummy&&n2.dummy)){ return; }

		double y1 = n1.y;
		n1.y = n2.y;
		n2.y = y1;

		nodes.set(index1, n2);
		nodes.set(index2, n1);
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(ArrayList<StatePosition> dn : depthNode){
			for(StatePosition sp : dn){
				sb.append(sp.id+",");
			}
		}
		return sb.toString();
	}

	public void randomMove(){
		int depth = (int)(Math.random()*depthNode.size());
		ArrayList<StatePosition> nodes = depthNode.get(depth);
		int index = (int)(Math.random()*nodes.size());
		nodes.get(index).y += Math.random()*100-50;
	}

	double transitionLength = -1;
	public double getTransitionLength(){
		if(transitionLength>=0){ return transitionLength; }
		double sum = 0;
		for(StatePosition p : getAllNode()){
			for(StatePosition to : p.toNodes){
				sum += (p.x-to.x)*(p.x-to.x) + (p.y-to.y)*(p.y-to.y);
			}
		}
		transitionLength = sum;
		return sum;
	}

	public double getFromBestLength(){
		double sum = 0;
		for(StatePosition p : getAllNode()){
			for(StatePosition to : p.toNodes){
				if(p.depth==to.depth){
					double l = 15;
					if(p.dummy) l -= 5;
					if(to.dummy) l -= 5;
					sum += Math.abs(Math.abs(p.y-to.y) - l);
				}else{
				//}else if(Math.abs(p.depth - to.depth)==1){
					//sum += Math.abs(Math.sqrt((p.x-to.x)*(p.x-to.x)+(p.y-to.y)*(p.y-to.y))-bestLength);
					sum += Math.sqrt((p.x-to.x)*(p.x-to.x)+(p.y-to.y)*(p.y-to.y));
				}
			}
		}
		for(ArrayList<StatePosition> dn : depthNode){
			for(StatePosition n1 : dn){
				for(StatePosition n2 : dn){
					if(n1.id<n2.id){
						double l = 15;
						if(n1.dummy) l -= 5;
						if(n2.dummy) l -= 5;
						double d = Math.abs(n1.y - n2.y);
						if(d<l){
							sum += (l - d)*(l - d);
						}
					}
				}
			}
		}
		return sum;
	}

	int allCross = -1;
	public int getAllCross(){
		if(allCross>=0){ return allCross; }
		int depth = depthNode.size();
		int cross = 0;
		for(int i=1;i<depth;++i){
			cross += getLayerCross(i-1,i);
			cross += getOneLayerCross(i);
		}
		allCross = cross;
		return cross;
	}

	private int getOneLayerCross(int layer){
		ArrayList<StatePosition> nodes = depthNode.get(layer);
		int cross = 0;
		for(StatePosition n : nodes){
			for(StatePosition to : n.toNodes){
				if(n.depth==to.depth&&n.id!=to.id){
					double b,t;
					if(n.y<to.y){
						b = n.y;
						t = to.y;
					}else{
						b = to.y;
						t = n.y;
					}
					for(StatePosition m : nodes){
						if(b<m.y&&m.y<t){
							cross++;
						}
					}
				}
			}
		}
		return cross;
	}

	private int getLayerCross(int layer1,int layer2){
		int cross = 0;

		/*
		ArrayList<StatePosition> nodes = new ArrayList<StatePosition>(depthNode.get(layer1));
		Collections.sort(nodes, new Comparator<StatePosition>() {
			public int compare(StatePosition n1, StatePosition n2) {
				if(n1.y<n2.y){
					return -1;
				}else if(n1.y>n2.y){
					return 1;
				}else{
					return 0;
				}
			}
		});
		for(int i=0;i<nodes.size();++i){
			for(StatePosition n1f : nodes.get(i).getLayerFlowNodes(layer2)){
				for(int j=i+1;j<nodes.size();++j){
					for(StatePosition n2f : nodes.get(j).getLayerFlowNodes(layer2)){
						if(n2f.y<n1f.y){
							cross++;
						}
					}
				}
			}
		}
*/

		ArrayList<StatePosition> nodes = depthNode.get(layer1);
		for(StatePosition n1 : nodes){
			for(StatePosition n1f : n1.getLayerFlowNodes(layer2)){
				for(StatePosition n2 : nodes){
					if(n1.y<n2.y){
						for(StatePosition n2f : n2.getLayerFlowNodes(layer2)){
							if(n2f.y<n1f.y){
								cross++;
							}
						}
					}
				}
			}
		}
		return cross;
	}

}

class StatePosition{
	StateNode node;
	long id;
	int depth;
	int nth;
	double x,y;
	boolean dummy;
	ArrayList<StatePosition> toNodes;
	ArrayList<StatePosition> fromNodes;

	StatePosition(StateNode node){
		this.node = node;
		this.id = node.id;
		this.depth = node.depth;
		this.nth = node.nth;
		this.x = node.getX();
		this.y = node.getY();
		this.dummy = node.dummy;
		this.toNodes = new ArrayList<StatePosition>();
		this.fromNodes = new ArrayList<StatePosition>();
	}

	StatePosition(StatePosition node){
		this.node = node.node;
		this.id = node.id;
		this.depth = node.depth;
		this.nth = node.nth;
		this.x = node.x;
		this.y = node.y;
		this.dummy = node.dummy;
		this.toNodes = new ArrayList<StatePosition>();
		this.fromNodes = new ArrayList<StatePosition>();
	}

	ArrayList<StatePosition> getLayerFlowNodes(int layer){
		ArrayList<StatePosition> backs = new ArrayList<StatePosition>();
		for(StatePosition from : fromNodes){
			if(from.depth==layer){
				backs.add(from);
			}
		}
		for(StatePosition to : toNodes){
			if(to.depth==layer){
				if(!backs.contains(to)){ backs.add(to); }
			}
		}
		return backs;
	}

}