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

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;

import lavit.Env;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransition;
import lavit.stateviewer.worker.State3DDynamicMover;
import lavit.stateviewer.worker.StateDynamicMover;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.picking.PickObject;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Cone;
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
	private ArrayList<ArrayList<State3DNode>> depthNode = new ArrayList<ArrayList<State3DNode>>();
	public StatePanel statePanel;

	public Canvas3D canvas;
	public SimpleUniverse universe;
	public BranchGroup rootBranchGroup;

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
		setLine();

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

	public ArrayList<ArrayList<State3DNode>> getDepthNode(){
		return depthNode;
	}

	public int getDepth(){
		return depthNode.size();
	}

	public void createGraph(){

		if(rootBranchGroup!=null){
			deleteGraph();
		}

		//3Dノードとトランジションの生成
		for(StateTransition trans : statePanel.stateGraphPanel.getDrawNodes().getAllTransition()){
			State3DNode from = all3DNode.get(trans.from);
			if(from==null){ from = new State3DNode(trans.from, statePanel.stateGraphPanel); all3DNode.put(trans.from, from); }
			State3DNode to = all3DNode.get(trans.to);
			if(to==null){ to = new State3DNode(trans.to, statePanel.stateGraphPanel); all3DNode.put(trans.to, to); }
			State3DTransition s3dtrans = new State3DTransition(trans);
			from.toes.add(s3dtrans);
			to.froms.add(s3dtrans);
			s3dtrans.from = from;
			s3dtrans.to = to;
			all3DTransition.add(s3dtrans);
		}

		//深さの決定
		for(ArrayList<StateNode> nodes : statePanel.stateGraphPanel.getDrawNodes().getDepthNode()){
			ArrayList<State3DNode> ns = new ArrayList<State3DNode>();
			for(StateNode node : nodes){
				ns.add(all3DNode.get(node));
			}
			depthNode.add(ns);
		}

		updateGraph();
	}

	public void updateGraph(){
		hideGraph();

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

	public void setLine(){
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

		BranchGroup bg = new BranchGroup();
		bg.addChild(tg);

		universe.addBranchGraph(bg);
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


	class MListener extends MouseAdapter{

	    public void mousePressed(MouseEvent e){
	        if(rootBranchGroup==null){ return; }
	    	PickCanvas pickCanvas = new PickCanvas(canvas, rootBranchGroup);
	        pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
	        pickCanvas.setTolerance(4.0f);
	        pickCanvas.setShapeLocation(e);
	        PickResult[] results = pickCanvas.pickAll();
	        if(results!=null){
	        	System.out.println("results.length="+results.length);
	        }

	        //rootBranchGroup.pickAll(pickshape)
	    }

	}

}
