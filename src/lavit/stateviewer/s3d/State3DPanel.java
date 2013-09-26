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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Light;
import javax.media.j3d.LineArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import lavit.Env;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransition;
import lavit.stateviewer.worker.State3DDynamicMover;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

public class State3DPanel extends JPanel {
	private LinkedHashMap<StateNode,State3DNode> all3DNode = new LinkedHashMap<StateNode,State3DNode>();
	private LinkedHashSet<State3DTransition> all3DTransition = new LinkedHashSet<State3DTransition>();
	private State3DNode startNode;
	private State3DNode selectNode;
	private List<List<State3DNode>> depthNode = new ArrayList<List<State3DNode>>();
	public StatePanel statePanel;

	public Canvas3D canvas;
	public SimpleUniverse universe;
	public BranchGroup rootBranchGroup;
	public BranchGroup axisBranchGroup;

	public State3DDynamicMover mover;

	public State3DPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		setLayout(new BorderLayout());

		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		canvas = new Canvas3D(config);
		canvas.addMouseListener(new MListener());
		add(canvas, BorderLayout.CENTER);

		universe = new SimpleUniverse(canvas);

		setCamera();
		setOrbitBehavior();

		mover = new State3DDynamicMover(this);
		mover.start();
	}

	public void setDynamicMoverActive(boolean active){
		mover.setActive(active);
		if(active){
			mover.setInnerSpring(Env.getInt("SV_DYNAMIC_SPRING"));
			mover.setInnerNodeRepulsion(Env.getInt("SV_DYNAMIC_NODE_REPULSION"));
			mover.setInnerDummyRepulsion(Env.getInt("SV_DYNAMIC_DUMMY_REPULSION"));
			mover.setInnerInterval(Env.getInt("SV_DYNAMIC_INTERVAL"));
			mover.setInnerMaxSpeed(Env.getInt("SV_DYNAMIC_MAXSPEED"));
		}
	}

	public Collection<State3DNode> getAllNode(){
		return all3DNode.values();
	}

	public List<List<State3DNode>> getDepthNode(){
		return depthNode;
	}

	public int getDepth(){
		return depthNode.size();
	}

	public void createGraph(){

		if(rootBranchGroup!=null){
			deleteGraph();
		}

		//ダミーを抜く
		statePanel.stateGraphPanel.getDrawNodes().removeDummy();
		statePanel.stateGraphPanel.getDrawNodes().updateNodeLooks();
		statePanel.stateGraphPanel.selectClear();
		statePanel.stateGraphPanel.update();

		//3Dノードとトランジションの生成
		for(StateTransition trans : statePanel.stateGraphPanel.getDrawNodes().getAllTransition()){
			State3DNode from = all3DNode.get(trans.from);
			if(from==null){ from = new State3DNode(trans.from, statePanel); all3DNode.put(trans.from, from); }
			State3DNode to = all3DNode.get(trans.to);
			if(to==null){ to = new State3DNode(trans.to, statePanel); all3DNode.put(trans.to, to); }
			State3DTransition s3dtrans = new State3DTransition(trans, statePanel);
			from.toes.add(s3dtrans);
			to.froms.add(s3dtrans);
			s3dtrans.from = from;
			s3dtrans.to = to;
			all3DTransition.add(s3dtrans);
		}

		//スタートノード
		startNode = all3DNode.get(statePanel.stateGraphPanel.getDrawNodes().getStartNodeOne());
		for(State3DNode node : getAllNode()){
			node.setStartNode(startNode);
		}

		//深さの決定
		for(List<StateNode> nodes : statePanel.stateGraphPanel.getDrawNodes().getDepthNode()){
			List<State3DNode> ns = new ArrayList<State3DNode>();
			for(StateNode node : nodes){
				ns.add(all3DNode.get(node));
			}
			depthNode.add(ns);
		}

		if(Env.is("SV3D_AUTO_RESET")){
			reset3dPosition();
		}
		updateGraph();
	}

	public void updateGraph(){
		hideGraph();

		if(Env.is("SV3D_DRAW_AXIS")){
			setLine();
		}else{
			removeLine();
		}

		rootBranchGroup = new BranchGroup();
		rootBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);

		for(State3DNode node : all3DNode.values()){
			node.updateShape();
			rootBranchGroup.addChild(node.getTransformGroup());
		}
		for(State3DTransition trans : all3DTransition){
			trans.updateShape();
			rootBranchGroup.addChild(trans.getTransformGroup());
		}

		// 光源を設置
		rootBranchGroup.addChild(createDirectionalLight());
		rootBranchGroup.addChild(createAmbientLight());
		rootBranchGroup.compile();
		universe.addBranchGraph(rootBranchGroup);
	}

	public void hideGraph(){
		if(rootBranchGroup==null){ return; }
		universe.getLocale().removeBranchGraph(rootBranchGroup);
		rootBranchGroup = null;
	}

	public void deleteGraph(){
		hideGraph();

		all3DNode.clear();
		all3DTransition.clear();
	}

	public void reset3dPosition(){
		int d = 0;
		for(List<State3DNode> nodes : depthNode){
			for(State3DNode node : nodes){
				//fromが2つ以上ある場合は平均座標に配置
				int fromSize=node.getFromBackNodes().size();
				if(fromSize>=2){
					double y=0,z=0;
					for(State3DNode from : node.getFromBackNodes()){
						y += from.getY();
						z += from.getZ();
					}
					node.setY(y/fromSize);
					node.setZ(z/fromSize);
				}

				//toがある場合は円形に配置
				int toSize=node.getToNextNodes().size();
				if(toSize>=1){
					double r = 10*Math.sqrt(toSize-1);
					int i=0;
					for(State3DNode to : node.getToNextNodes()){
						to.setY(r*Math.cos(d+i*2*Math.PI/toSize)+node.getY());
						to.setZ(r*Math.sin(d+i*2*Math.PI/toSize)+node.getZ());
						i++;
					}
				}
				d+=Math.PI/2;
			}
		}
	}

	public void removeLine(){
		if(axisBranchGroup!=null){
			universe.getLocale().removeBranchGraph(axisBranchGroup);
		}
	}

	public void setLine(){
		removeLine();

		axisBranchGroup = new BranchGroup();
		axisBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);

		TransformGroup tg = new TransformGroup();

		//ラインを追加
		Point3d[] vertex;
		LineArray geometry;

		vertex = new Point3d[2];
		vertex[0] = new Point3d(100,0,0);
		vertex[1] = new Point3d(-100,0,0);
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.RED));
		geometry.setColor(1, new Color3f(Color.RED));
		tg.addChild(new Shape3D(geometry));

		vertex = new Point3d[2];
		vertex[0] = new Point3d(0,100,0);
		vertex[1] = new Point3d(0,-100,0);
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.GREEN));
		geometry.setColor(1, new Color3f(Color.GREEN));
		tg.addChild(new Shape3D(geometry));

		vertex = new Point3d[2];
		vertex[0] = new Point3d(0,0,100);
		vertex[1] = new Point3d(0,0,-100);
		geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);
		geometry.setColor(0, new Color3f(Color.BLUE));
		geometry.setColor(1, new Color3f(Color.BLUE));
		tg.addChild(new Shape3D(geometry));

		axisBranchGroup.addChild(tg);

		universe.addBranchGraph(axisBranchGroup);
	}

	public void setCamera(){

		//視点についてのハードウェア情報を取得。
		ViewingPlatform vp = universe.getViewingPlatform();

		//視点のための座標変換クラスを用意
		TransformGroup camera = vp.getViewPlatformTransform();

		//vp.setNominalViewingTransform();

		double d = 0.1D;
		Transform3D transform3d = new Transform3D();
		double d1 = 1.0D / Math.tan(d / 2D);
		transform3d.set(new Vector3d(0.0D, 0.0D, d1));
		camera.setTransform(transform3d);

		//キーボードジェスチャ
		BoundingSphere bounds = new BoundingSphere( new Point3d(), 100.0 );
		KeyNavigatorBehavior keybehavior = new KeyNavigatorBehavior(camera);
		keybehavior.setSchedulingBounds(bounds);
		PlatformGeometry vg = new PlatformGeometry();
		vg.addChild(keybehavior);
		universe.getViewingPlatform().setPlatformGeometry(vg);

		/*
		//カメラの位置ベクトル
		Transform3D view_pos = new Transform3D();

		//カメラの位置を決める
		Vector3f pos_vec = new Vector3f(10f,10f,10f);

		//カメラの位置について、座標変換実行
		view_pos.setTranslation(pos_vec);

		//カメラの向きを示すベクトル
		Transform3D view_dir = new Transform3D();
		Transform3D view_dir2 = new Transform3D();

		//カメラの向きを決める
		view_dir.rotY(Math.PI/4);
		view_dir2.rotX(-Math.PI/4);
		view_dir.mul(view_dir2);

		//カメラの位置およびカメラの向きを統合
		view_pos.mul(view_dir);

		//カメラの位置情報を登録
		camera.setTransform(view_pos);
		 */
	}

	public void setOrbitBehavior(){
		//OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
		OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
		orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100.0));
		universe.getViewingPlatform().setViewPlatformBehavior(orbit);
	}

	public BranchGroup createSceneGraph2() {
		BranchGroup objRoot = new BranchGroup();

		Point3d[] vertex = new Point3d[2];

		vertex[0] = new Point3d(-0.5, 0.0, 0.0);
		vertex[1] = new Point3d(0.5, 0.0, 0.0);

		LineArray geometry = new LineArray(vertex.length, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		geometry.setCoordinates(0, vertex);

		geometry.setColor(0, new Color3f(Color.red));
		geometry.setColor(1, new Color3f(Color.red));

		Shape3D shape = new Shape3D(geometry);
		objRoot.addChild(shape);
		objRoot.compile();

		return objRoot;
	}

	/*
	public BranchGroup createSceneGraph3() {
		BranchGroup objRoot = new BranchGroup();

		// 光源を設置する
		objRoot.addChild(createDirectionalLight());
		objRoot.addChild(createAmbientLight());

		Appearance ap = new Appearance();
		Material ma = new Material();
		ma.setDiffuseColor(1.0f, 1.0f, 1.0f);
		ma.setEmissiveColor(new Color3f(1.0f, 1.0f, 1.0f)); //発光
		ap.setMaterial(ma);

		Cone cone = new Cone( 0.05f, 0.3f, Cone.GENERATE_NORMALS, 20, 1, ap);

		Transform3D t3dy = new Transform3D();
		//t3dy.rotY(Math.PI / 6);
		Transform3D t3dx = new Transform3D();
		//t3dx.rotX(Math.PI / 2);
		Transform3D t3dz = new Transform3D();
		t3dz.rotZ(Math.PI / 2);

		Transform3D t3d = new Transform3D();
		t3d.mul(t3dy);
		t3d.mul(t3dx);
		t3d.mul(t3dz);

		TransformGroup objTrans = new TransformGroup(t3d);
		objRoot.addChild(objTrans);

		objTrans.addChild(cone);
		objRoot.compile();

		return objRoot;
	}
	 */

	public BranchGroup createObjects() {

		BranchGroup root = new BranchGroup();

		// 平行移動を定義
		TransformGroup transRoot = new TransformGroup();
		Transform3D transform = new Transform3D();

		// x方向に1.0m移動
		transform.set(new Vector3d(1.0, 0, 0));
		transRoot.setTransform(transform);

		// 色つき立方体の作成
		transRoot.addChild(new ColorCube(0.4));
		root.addChild(transRoot);
		root.compile();

		return root;
	}

	private Light createDirectionalLight(){
		DirectionalLight light = new DirectionalLight( true,
				new Color3f(1.0f, 1.0f, 1.0f),
				new Vector3f(1.0f, 1.0f, 1.0f));
		light.setInfluencingBounds(new BoundingSphere(new Point3d(), 100.0));
		return light;
	}

	private Light createAmbientLight(){
		AmbientLight light = new AmbientLight(new Color3f(0.3f, 0.3f, 0.3f));
		light.setInfluencingBounds(new BoundingSphere(new Point3d(), 100.0));
		return light;
	}

	public void rotationYZ(double alpha){
		for(State3DNode node : all3DNode.values()){
			double y1 = node.getY(), z1 = node.getZ();
			double y2 = y1 * Math.cos(alpha) - z1 * Math.sin(alpha);
			double z2 = y1 * Math.sin(alpha) + z1 * Math.cos(alpha);
			node.setY(y2);
			node.setZ(z2);
		}
	}


	class MListener extends MouseAdapter{

		public void mousePressed(MouseEvent e){
			if(rootBranchGroup==null){ return; }
			PickCanvas pickCanvas = new PickCanvas(canvas, rootBranchGroup);
			pickCanvas.setMode(PickTool.BOUNDS);
			pickCanvas.setTolerance(4.0f);
			pickCanvas.setShapeLocation(e);
			PickResult[] results = pickCanvas.pickAll();
			selectNode = null;
			if(results!=null){
				for(PickResult res : results){
					Primitive p = (Primitive)res.getNode(PickResult.PRIMITIVE);
					if(p==null){ continue; }
					if(p instanceof Sphere){
						Sphere s = (Sphere)p;
						for(State3DNode node : all3DNode.values()){
							if(node.getSphere()==s){
								selectNode = node;
							}
						}
					}
				}
			}
			statePanel.stateControlPanel.stateControllerTab.s3dPanel.node3DLabel.setNode(selectNode);
			if(selectNode!=null){
				statePanel.stateGraphPanel.setSelectNode(selectNode.node);
			}
		}

	}

}
