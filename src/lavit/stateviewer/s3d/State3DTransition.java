package lavit.stateviewer.s3d;

import java.awt.Color;

import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.PositionInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import lavit.stateviewer.StateNode;
import lavit.stateviewer.StateTransition;

import com.sun.j3d.utils.geometry.Cone;

public class State3DTransition{
	public StateTransition trans;
	public State3DNode from;
	public State3DNode to;
	public TransformGroup tg = null;

	public State3DTransition(StateTransition trans){
		this.trans = trans;
	}

	public void updateShape(){
		if(to==from){ return; }

		//removeAllChildren();

		tg = new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		double dis = from.getGraphPoint().distance(to.getGraphPoint());
		double length = dis-0.4;
		double dx = to.getGraphPoint().x-from.getGraphPoint().x;
		double dy = to.getGraphPoint().y-from.getGraphPoint().y;
		double dz = to.getGraphPoint().z-from.getGraphPoint().z;

		// 色の設定
		Appearance ap = new Appearance();
		Material ma = new Material();
		Color color = Color.WHITE;
		ma.setDiffuseColor(new Color3f(color));
		ma.setEmissiveColor(new Color3f(color)); //発光
		ap.setMaterial(ma);

		// 円錐を追加
		Cone cone = new Cone( 0.05f, 0.3f, Cone.GENERATE_NORMALS, 20, 1, ap);
		tg.addChild(cone);

		//ラインを追加
		Point3d[] vertex = new Point3d[2];
		vertex[0] = new Point3d(0.0, 0.1, 0.0);
		vertex[1] = new Point3d(0.0, -length, 0.0);
		LineArray geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(color));
		geometry.setColor(1, new Color3f(color));
		Shape3D shape = new Shape3D(geometry);
		tg.addChild(shape);


		//平行移動を定義
		Transform3D transform = new Transform3D();
		transform.set(new Vector3d(to.getGraphPoint().x - 0.3*dx/dis, to.getGraphPoint().y - 0.3*dy/dis, to.getGraphPoint().z - 0.3*dz/dis));

		//回転を定義
		Transform3D rotate = new Transform3D();
		if(dx>=0){
			rotate.rotZ(-Math.PI / 2);
		}else{
			rotate.rotZ(Math.PI / 2);
		}
		Transform3D t3dx = new Transform3D();
		if(dy!=0){
			t3dx.rotX(Math.atan(dz/dy));
		}else if(dz>0){
			t3dx.rotX(-Math.PI / 2);
		}else if(dz<0){
			t3dx.rotX(Math.PI / 2);
		}
		Transform3D t3dz = new Transform3D();
		if(dx!=0){
			if(dy>0){
				t3dz.rotZ(Math.atan(Math.sqrt(dy*dy+dz*dz)/dx));
			}else{
				t3dz.rotZ(Math.atan(-Math.sqrt(dy*dy+dz*dz)/dx));
			}
		}else if(dy>0){
			t3dz.rotZ(Math.PI / 2);
		}else if(dy<0){
			t3dz.rotZ(-Math.PI / 2);
		}

		t3dz.mul(rotate);
		t3dx.mul(t3dz);
		transform.mul(t3dx);
		tg.setTransform(transform);
	}

	public TransformGroup getTransformGroup(){
		return tg;
	}

	/*
	public void makeChild_s(){
		double dis = from.p.distance(to.p);
		if(dis==0){ return; }

		double dx = to.p.x-from.p.x;
		double dy = to.p.y-from.p.y;
		double dz = to.p.z-from.p.z;

		//ラインを追加
		Point3d[] vertex;
		LineArray geometry;

		vertex = new Point3d[2];
		vertex[0] = from.p;
		vertex[1] = to.p;
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.WHITE));
		geometry.setColor(1, new Color3f(Color.WHITE));
		addChild(new Shape3D(geometry));

		vertex = new Point3d[2];
		vertex[0] = new Point3d(to.p.x-0.2*dx/dis-0.2*dx/dis, to.p.y-0.2*dy/dis, to.p.z-0.2*dz/dis);
		vertex[1] = new Point3d(to.p.x-0.2*dx/dis, to.p.y-0.2*dy/dis, to.p.z-0.2*dz/dis);
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.WHITE));
		geometry.setColor(1, new Color3f(Color.WHITE));
		addChild(new Shape3D(geometry));

		vertex = new Point3d[2];
		vertex[0] = new Point3d(to.p.x-0.2*dx/dis, to.p.y-0.2*dy/dis-0.2*dy/dis, to.p.z-0.2*dz/dis);
		vertex[1] = new Point3d(to.p.x-0.2*dx/dis, to.p.y-0.2*dy/dis, to.p.z-0.2*dz/dis);
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.WHITE));
		geometry.setColor(1, new Color3f(Color.WHITE));
		addChild(new Shape3D(geometry));

		vertex = new Point3d[2];
		vertex[0] = new Point3d(to.p.x-0.2*dx/dis, to.p.y-0.2*dy/dis, to.p.z-0.2*dz/dis-0.2*dz/dis);
		vertex[1] = new Point3d(to.p.x-0.2*dx/dis, to.p.y-0.2*dy/dis, to.p.z-0.2*dz/dis);
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.WHITE));
		geometry.setColor(1, new Color3f(Color.WHITE));
		addChild(new Shape3D(geometry));
	}
	*/

	double at(double d1, double d2){
		if(d2!=0){
			return Math.atan(d1/d2);
		}else if(d1>0){
			return Math.PI / 2;
		}else{
			return -Math.PI / 2;
		}
	}

}
