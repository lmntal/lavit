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
	public StateNode node;
	public LinkedHashSet<State3DTransition> toes = new LinkedHashSet<State3DTransition>();
	public LinkedHashSet<State3DTransition> froms = new LinkedHashSet<State3DTransition>();
	public TransformGroup tg = null;
	public double z,dz,ddz;
	public StateNode startNode;

	public State3DNode(StateNode node, StateGraphPanel stateGraphPanel){
		this.node = node;
		this.z = 10*(Math.random()-0.5)+node.getX()*node.getY()/1000;
		this.startNode = stateGraphPanel.getDrawNodes().getStartNodeOne();
	}

	public void updateShape(){

		tg = new TransformGroup();

		//removeAllChildren();

		// 色の設定
		Appearance ap = new Appearance();
		Material ma = new Material();
		Color color = node.getColor();
		ma.setDiffuseColor(new Color3f(color));
		ma.setEmissiveColor(new Color3f(color)); //発光
		ap.setMaterial(ma);

		// 物体を追加
		Sphere sphere = new Sphere(0.2f, Sphere.GENERATE_NORMALS, 50, ap);
		tg.addChild(sphere);

		// 平行移動を定義
		Transform3D transform = new Transform3D();
		transform.set(new Vector3d(getGraphPoint()));
		tg.setTransform(transform);
	}

	public TransformGroup getTransformGroup(){
		return tg;
	}

	public Collection<State3DNode> getToNodes(){
		ArrayList<State3DNode> toNodes = new ArrayList<State3DNode>();
		for(State3DTransition t : toes){
			toNodes.add(t.to);
		}
		return toNodes;
	}

	public Collection<State3DNode> getFromNodes(){
		ArrayList<State3DNode> fromNodes = new ArrayList<State3DNode>();
		for(State3DTransition t : froms){
			fromNodes.add(t.from);
		}
		return fromNodes;
	}

	public long getID(){
		return node.id;
	}

	public boolean isDummy(){
		return node.dummy;
	}

	public double distance(State3DNode node){
		return getPoint().distance(node.getPoint());
	}

	public double getX(){
		return this.node.getX();
	}

	public double getY(){
		return this.node.getY();
	}

	public double getZ(){
		return this.z;
	}

	public void setX(double x){
		this.node.setX(x);
	}

	public void addX(double dx){
		setX(getX()+dx);
	}

	public void setY(double y){
		this.node.setY(y);
	}

	public void addY(double dy){
		setY(getY()+dy);
	}

	public void setZ(double z){
		this.z = z;
	}

	public void addZ(double dz){
		setZ(getZ()+dz);
	}

	public double getDY(){
		return this.node.dy;
	}

	public double getDZ(){
		return this.dz;
	}

	public void setDY(double dy){
		this.node.dy = dy;
	}

	public void addDY(double ddy){
		setDY(getDY()+ddy);
	}

	public void setDZ(double dz){
		this.dz = dz;
	}

	public void addDZ(double ddz){
		setDZ(getDZ()+ddz);
	}

	public double getDDY(){
		return this.node.ddy;
	}

	public void addDDY(double dddy){
		setDDY(getDDY()+dddy);
	}

	public double getDDZ(){
		return this.ddz;
	}

	public void addDDZ(double dddz){
		setDDZ(getDDZ()+dddz);
	}

	public void setDDY(double ddy){
		this.node.ddy = ddy;
	}

	public void setDDZ(double ddz){
		this.ddz = ddz;
	}

	public void setPosition(double x, double y, double z){
		setX(x);
		setY(y);
		setZ(z);
	}

	public double getSpeed(){
		return Math.sqrt(getDY()*getDY()+getDZ()*getDZ());
	}

	public void move(int maxSpeed){
		addDY(getDDY());
		addDZ(getDDZ());
		double speed = getSpeed();
		if(speed>maxSpeed){
			setDY(getDY()*maxSpeed/speed);
			setDZ(getDZ()*maxSpeed/speed);
		}
		addY(getDY());
		addZ(getDZ());
		//System.out.println("id:"+getID()+",y:"+getY()+",dy:"+getDY()+",ddy:"+getDDY());
	}

	public double getDistance(State3DNode n){
		return n.getPoint().distance(getPoint());
	}

	public Point3d getPoint(){
		return new Point3d(getX(),getY(),getZ());
	}

	public Point3d getGraphPoint(){
		Point3d p = new Point3d((node.getX()-startNode.getX())*Env.getInt("SV3D_X_SCALE")/300, (node.getY()-startNode.getY())*Env.getInt("SV3D_Y_SCALE")/300, getZ()*Env.getInt("SV3D_Z_SCALE")/300);
		//(node.getX()-startNode.getX())*(node.getY()-startNode.getY())/1000
		return p;
	}

}
