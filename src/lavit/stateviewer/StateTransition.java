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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lavit.util.UtilTextDialog;
import lavit.stateviewer.controller.StateNodeLabel;

public class StateTransition {
	public StateNode from;
	public StateNode to;
	public boolean cycle;
	public boolean weak;

	private ArrayList<StateRule> rules = new ArrayList<StateRule>();

	Color color;

	StateTransition() {
		this.cycle = false;
		this.weak = false;
	}

	StateTransition(StateNode from, StateNode to, boolean cycle, boolean weak) {
		this.from = from;
		this.to = to;
		this.cycle = cycle;
		this.weak = weak;
	}

	/*
	 * StateTransition(StateNode from,StateNode to,boolean cycle){ this.from = from;
	 * this.to = to; this.cycle = cycle; }
	 */

	public String getRuleNameString() {
		StringBuffer buf = new StringBuffer();
		for (StateRule r : rules) {
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(r.getName());
		}
		return buf.toString();
	}

	public ArrayList<StateRule> getRules() {
		return rules;
	}

	public boolean isToDummy() {
		return to.dummy;
	}

	void addRules(StateRule rule) {
		if (rule == null) {
			return;
		}
		if (rules.contains(rule)) {
			return;
		}
		rules.add(rule);
	}

	void addRules(ArrayList<StateRule> rs) {
		if (rs == null) {
			return;
		}
		for (StateRule r : rs) {
			addRules(r);
		}
	}

	void doubleClick(StateGraphPanel graphPanel) {
		UtilTextDialog.showDialog(from.id + " -> " + to.id,
				from.state + "\n\n-> (" + getRuleNameString() + ")\n\n" + to.state);
	}

	public String unpack(StateNodeLabel nodeLabel) {
		return "【" + from.id + "】" + from.state + "->"+ "(" + getRuleNameString() + ")【"  + to.id + "】" + to.state ;
	}

	public String diff_unpack(StateNodeLabel nodeLabel) {
		String diff_from = "";
		String diff_to = "";
		// from.stateとto.stateの差分を取得
		String[] from_state = from.state.split(" ");
		String[] to_state = to.state.split(" ");
		// 差分を取得(順番は関係ないので、2重ループで全探索)
		for (int i = 0; i < from_state.length; i++) {
			for (int j = 0; j < to_state.length; j++) {
				if (from_state[i].equals(to_state[j])) {
					// 一致した場合は、to_stateから削除
					to_state = remove(to_state, j);
					break;
				} else if (j == to_state.length - 1) {
					diff_from += from_state[i] ;
				}
			}
		}
		diff_to = String.join(".", to_state);
		return "【" + from.id + "】" + diff_from + "->"+ "(" + getRuleNameString() + ")【"  + to.id + "】" + diff_to ;
	}
	private static String[] remove(String[] arr, int index) {
        if (arr == null || index < 0 || index >= arr.length) {
            return arr;
        }

        List<String> result = new ArrayList<>(Arrays.asList(arr));
        result.remove(index);
        return result.toArray(new String[0]);
    }

	public String struct_diff_unpack(StateNodeLabel nodeLabel) {
		String diff_from = "";
		String diff_to = "";
		// from.stateとto.stateの差分を取得
		String[] from_state = from.state.split(" ");
		String[] to_state = to.state.split(" ");
		// 差分を取得(順番は関係ないので、2重ループで全探索)
		for (int i = 0; i < from_state.length; i++) {
			for (int j = 0; j < to_state.length; j++) {
				if (from_state[i].equals(to_state[j])) {
					// 一致した場合は、to_stateから削除
					to_state = remove(to_state, j);
					break;
				} else if (j == to_state.length - 1) {
					// from_stateとto_stateは一致しないが、その中身の構造を比較
					//String diff_struct = "";
					//String from_struct = from_state[i].split("\\(")[0];
					//String to_struct = to_state[j].split("\\(")[0];
					//if (from_struct.equals(to_struct)) {
					//	diff_struct = from_struct + "(";
					diff_from += from_state[i] + " ";
				}
			}
		}
		diff_to = String.join(" ", to_state);
		return "【" + from.id + "】" + diff_from + "->"+ "(" + getRuleNameString() + ")【"  + to.id + "】" + diff_to ;
	}

	public String toString() {
		return from.id + " -> " + to.id + " (" + getRuleNameString() + ")";
	}

	private Shape getShape() {
		if (to == from) {
			RoundRectangle2D.Double shape = new RoundRectangle2D.Double(to.getX() - 10, to.getY() - 10, 10, 10, 10, 10);
			return shape;
		} else {
			Polygon shape = new Polygon();
			if (Math.abs(to.getX() - from.getX()) > Math.abs(to.getY() - from.getY())) {
				shape.addPoint((int) to.getX(), (int) to.getY() + 2);
				shape.addPoint((int) to.getX(), (int) to.getY() - 2);
				shape.addPoint((int) from.getX(), (int) from.getY() - 2);
				shape.addPoint((int) from.getX(), (int) from.getY() + 2);
			} else {
				shape.addPoint((int) to.getX() + 2, (int) to.getY());
				shape.addPoint((int) to.getX() - 2, (int) to.getY());
				shape.addPoint((int) from.getX() - 2, (int) from.getY());
				shape.addPoint((int) from.getX() + 2, (int) from.getY());
			}
			return shape;
		}
	}

	void separateTransition(StateNodeSet drawNodes) {
		if (to == from) {
			StateNode dummy1 = drawNodes.makeDummyFromTransition(this);
			StateNode dummy2 = drawNodes.makeDummyFromTransition(dummy1.getFromTransition());
			dummy1.setX(to.getX() - 10);
			dummy1.setY(to.getY() - 20);
			dummy2.setX(to.getX() + 10);
			dummy2.setY(to.getY() - 20);
		} else {
			drawNodes.makeDummyFromTransition(this);
		}
		/*
		 * StateNodeSet nodeSet = graphPanel.getDrawNodes();
		 *
		 * long id = nodeSet.publishNodeId(); double x = ((from.getX()+to.getX())/2);
		 * double y = ((from.getY()+to.getY())/2); StateNode dummy = new StateNode(id,
		 * nodeSet); dummy.setPosition(x, y); dummy.dummy = true;
		 * if(from.depth>to.depth){ dummy.backDummy = true; } dummy.depth = to.depth;
		 * nodeSet.addNode(dummy); dummy.updateLooks();
		 *
		 * StateTransition t1 = new StateTransition(from, dummy, cycle, weak);
		 * t1.addRules(getRules()); t1.from.addToTransition(t1);
		 * t1.to.addFromTransition(t1); nodeSet.addTransition(t1);
		 *
		 * StateTransition t2 = new StateTransition(dummy, to, cycle, weak);
		 * t2.addRules(getRules()); t2.from.addToTransition(t2);
		 * t2.to.addFromTransition(t2); nodeSet.addTransition(t2);
		 *
		 * from.removeToTransition(this); to.removeFromTransition(this);
		 * nodeSet.removeTransition(this);
		 *
		 * nodeSet.setTreeDepth();
		 */
	}

	public boolean contains(Point2D p) {
		return getShape().contains(p);
	}

	public void draw(Graphics2D g2) {
		g2.draw(getShape());
	}

}
