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

package lavit.stateviewer.s3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import lavit.Env;
import lavit.stateviewer.*;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.geometry.Sphere;

public class State3DNode {
	private StatePanel panel;

	public StateNode node;
	public LinkedHashSet<State3DTransition> toes = new LinkedHashSet<State3DTransition>();
	public LinkedHashSet<State3DTransition> froms = new LinkedHashSet<State3DTransition>();

	public TransformGroup tg = null;
	public Sphere sphere = null;

	public double z, dz, ddz;
	public State3DNode startNode;

	public State3DNode(StateNode node, StatePanel panel) {
		this.panel = panel;
		this.node = node;
		this.z = 0;
	}

	public void updateShape() {

		tg = new TransformGroup();

		// removeAllChildren();

		// 色の設定
		Appearance ap = new Appearance();
		Material ma = new Material();
		boolean searchMode = panel.stateGraphPanel.getDraw().isSearchMode();
		boolean cycleMode = panel.stateGraphPanel.getDraw().isCycleMode();
		if (searchMode && node.weak || !searchMode && cycleMode && !node.cycle) {
			ma.setDiffuseColor(new Color3f(Color.GRAY));
		} else {
			Color color = node.getColor();
			ma.setDiffuseColor(new Color3f(color));
			ma.setEmissiveColor(new Color3f(color)); // 発光
		}
		ap.setMaterial(ma);

		// 物体を追加
		sphere = new Sphere(0.2f, Sphere.GENERATE_NORMALS, 50, ap);
		tg.addChild(sphere);

		// 平行移動を定義
		Transform3D transform = new Transform3D();
		transform.set(new Vector3d(getGraphPoint()));
		tg.setTransform(transform);
	}

	public Sphere getSphere() {
		return sphere;
	}

	public void setStartNode(State3DNode startNode) {
		this.startNode = startNode;
	}

	public TransformGroup getTransformGroup() {
		return tg;
	}

	public Collection<State3DNode> getToNodes() {
		ArrayList<State3DNode> toNodes = new ArrayList<State3DNode>();
		for (State3DTransition t : toes) {
			toNodes.add(t.to);
		}
		return toNodes;
	}

	public Collection<State3DNode> getFromNodes() {
		ArrayList<State3DNode> fromNodes = new ArrayList<State3DNode>();
		for (State3DTransition t : froms) {
			fromNodes.add(t.from);
		}
		return fromNodes;
	}

	public Collection<State3DNode> getToNextNodes() {
		ArrayList<State3DNode> toNodes = new ArrayList<State3DNode>();
		for (State3DTransition t : toes) {
			if (t.to.node.depth != node.depth + 1) {
				continue;
			}
			toNodes.add(t.to);
		}
		return toNodes;
	}

	public Collection<State3DNode> getFromBackNodes() {
		ArrayList<State3DNode> fromNodes = new ArrayList<State3DNode>();
		for (State3DTransition t : froms) {
			if (t.from.node.depth != node.depth - 1) {
				continue;
			}
			fromNodes.add(t.from);
		}
		return fromNodes;
	}

	public long getID() {
		return node.id;
	}

	public boolean isDummy() {
		return node.dummy;
	}

	public double distance(State3DNode node) {
		return getPoint().distance(node.getPoint());
	}

	public double getX() {
		return this.node.getX();
	}

	public double getY() {
		return this.node.getY();
	}

	public double getZ() {
		return this.z;
	}

	public void setX(double x) {
		this.node.setX(conv(x));
	}

	public void addX(double dx) {
		setX(getX() + dx);
	}

	public void setY(double y) {
		this.node.setY(conv(y));
	}

	public void addY(double dy) {
		setY(getY() + dy);
	}

	public void setZ(double z) {
		this.z = conv(z);
	}

	public void addZ(double dz) {
		setZ(getZ() + dz);
	}

	public double getDY() {
		return this.node.dy;
	}

	public double getDZ() {
		return this.dz;
	}

	public void setDY(double dy) {
		this.node.dy = conv(dy);
	}

	public void addDY(double ddy) {
		setDY(getDY() + ddy);
	}

	public void setDZ(double dz) {
		this.dz = conv(dz);
	}

	public void addDZ(double ddz) {
		setDZ(getDZ() + ddz);
	}

	public double getDDY() {
		return this.node.ddy;
	}

	public void addDDY(double dddy) {
		setDDY(getDDY() + dddy);
	}

	public double getDDZ() {
		return this.ddz;
	}

	public void addDDZ(double dddz) {
		setDDZ(getDDZ() + dddz);
	}

	public void setDDY(double ddy) {
		this.node.ddy = conv(ddy);
	}

	public void setDDZ(double ddz) {
		this.ddz = conv(ddz);
	}

	private double conv(double x) {
		if (!(-1000000000 < x && x < 1000000000)) {
			return 0;
		}
		return x;
	}

	public void setPosition(double x, double y, double z) {
		setX(x);
		setY(y);
		setZ(z);
	}

	public double getSpeed() {
		return Math.sqrt(getDY() * getDY() + getDZ() * getDZ());
	}

	public void move(int maxSpeed) {
		addDY(getDDY());
		addDZ(getDDZ());
		double speed = getSpeed();
		if (speed > maxSpeed) {
			setDY(getDY() * maxSpeed / speed);
			setDZ(getDZ() * maxSpeed / speed);
		}
		addY(getDY());
		addZ(getDZ());
		// System.out.println("id:"+getID()+",y:"+getY()+",dy:"+getDY()+",ddy:"+getDDY());
	}

	public double getDistance(State3DNode n) {
		return n.getPoint().distance(getPoint());
	}

	public Point3d getPoint() {
		return new Point3d(getX(), getY(), getZ());
	}

	public Point3d getGraphPoint() {
		double x = getX() - startNode.getX();
		double y = getY() - startNode.getY();
		double z = getZ() - startNode.getZ();
		Point3d p = new Point3d(x * Env.getInt("SV3D_X_SCALE") / 300, y * Env.getInt("SV3D_Y_SCALE") / 300,
				z * Env.getInt("SV3D_Z_SCALE") / 300);
		// System.out.println(p);
		// (node.getX()-startNode.getX())*(node.getY()-startNode.getY())/1000
		return p;
	}

}
