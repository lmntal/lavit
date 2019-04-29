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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import lavit.Env;
import lavit.FrontEnd;
import lavit.runner.LmntalRunner;
import lavit.stateviewer.draw.StateDraw;
import lavit.util.UtilTextDialog;

public class StateNode implements Shape {
	public long id;

	public StateNodeSet parentSet;
	public StateNodeSet childSet;

	public String state;
	public String label;
	boolean accept;
	public boolean cycle;

	public int depth;
	public int nth;

	public boolean dummy;
	public boolean backDummy; //dummy=trueの場合のみtrueになる可能性がある
	public boolean weak;

	private LinkedHashSet<StateTransition> toes = new LinkedHashSet<StateTransition>();
	private LinkedHashSet<StateTransition> froms = new LinkedHashSet<StateTransition>();

	private boolean marked;
	private boolean inFrame;

	private Shape shape;
	private double radius;
	private Color color;

	//物理モデル用変数
	public double dy;
	public double ddy;

	StateNode(long id, StateNodeSet parentSet){
		this.id = id;
		this.parentSet = parentSet;

		this.state = "";
		this.label = "";
		this.accept = false;
		this.cycle = false;

		this.depth = 0;
		this.nth = 0;

		this.dummy = false;
		this.backDummy = false;
		this.weak = false;

		this.toes = new LinkedHashSet<StateTransition>();
		this.froms = new LinkedHashSet<StateTransition>();

		this.marked = false;
		this.inFrame = false;

		this.shape = new RoundRectangle2D.Double(0,0,0,0,0,0);
		this.radius = 5.0;
		this.color = Color.black;

		//updateLooks();
	}

	/*
	void init(String state,String label,boolean accept,boolean inCycle){
		this.depth = 0;
		this.nth = 0;
		this.state = state;
		this.label = label;
		this.accept = accept;
		this.cycle = inCycle;

		this.dummy = false;
		this.weak = false;

		this.toes = new LinkedHashSet<StateTransition>();
		this.froms = new LinkedHashSet<StateTransition>();

		this.childSet = null;

		//this.toNodes = new ArrayList<StateNode>();
		//this.fromNodes = new LinkedHashSet<StateNode>();
		//this.emToNodes = new ArrayList<StateNode>();

		this.marked = false;
		this.shape = new RoundRectangle2D.Double(0,0,0,0,0,0);

		updateLooks();
	}
	 */

	void updateLooks(){
		StateGraphPanel panel = parentSet.panel;
		StateDraw draw = panel.getDraw();
		if(draw!=null){
			draw.setNodeLook(this);
		}
	}

	public void setShape(Shape shape){
		this.shape = shape;
	}

	public synchronized double getX(){
		return ((RectangularShape)shape).getCenterX();
	}

	public synchronized double getY(){
		return ((RectangularShape)shape).getCenterY();
	}

	public synchronized void setX(double x){
		setPosition(x,getY());
	}

	public synchronized void setY(double y){
		setPosition(getX(),y);
	}

	public synchronized void move(double dx, double dy){
		setPosition(getX()+dx, getY()+dy);
	}

	public synchronized void setPosition(double x,double y){
		if(!(-1000000000<y&&y<1000000000)){ y = 0; }
		((RectangularShape)shape).setFrame(x-radius, y-radius, radius*2, radius*2);
	}

	/*
	StateTransition addToNode(StateNode toNode,ArrayList<String> rules,boolean em){
		if(isToNode(toNode)) return null;

		StateTransition t = new StateTransition(this,toNode,em);
		t.addRules(rules);
		toes.add(t);
		return t;
	}
	 */

	public Collection<StateNode> getRuleNameGroupNodes(StateRule rule){
		HashSet<StateNode> nodes = new HashSet<StateNode>();
		for(StateTransition t : toes){
			if(t.getRules().contains(rule)){
				nodes.add(t.to);
			}
		}
		for(StateTransition f : froms){
			if(f.getRules().contains(rule)){
				nodes.add(f.from);
			}
		}
		return nodes;
	}

	public Collection<StateNode> getRuleNameGroupNodes(Collection<StateRule> rules){
		HashSet<StateNode> nodes = new HashSet<StateNode>();
		for(StateRule rule : rules){
			nodes.addAll(getRuleNameGroupNodes(rule));
		}
		return nodes;
	}


	/*
	 * to系メソッド
	 */

	StateTransition addToTransition(StateTransition t){
		if(isToNode(t.to)) return null;
		toes.add(t);
		return t;
	}

	StateTransition removeToTransition(StateTransition t){
		toes.remove(t);
		return t;
	}

	public Collection<StateTransition> getToTransitions(){
		return toes;
	}

	/*
	StateTransition removeToNode(StateNode toNode){
		StateTransition r = null;
		for(StateTransition t : toes){
			if(t.to==toNode){
				r = t;
				break;
			}
		}
		toes.remove(r);
		return r;
	}
	 */

	boolean isToNode(StateNode toNode){
		return getToNodes().contains(toNode);
	}

	public ArrayList<StateNode> getToNodes(){
		ArrayList<StateNode> toNodes = new ArrayList<StateNode>();
		for(StateTransition t : toes){
			toNodes.add(t.to);
		}
		return toNodes;
	}

	public StateNode getToNode(){
		for(StateTransition t : toes){
			return t.to;
		}
		return null;
	}

	public StateNode getToCycleNode(){
		for(StateTransition t : toes){
			if(t.cycle){
				return t.to;
			}
		}
		return null;
	}

	public StateTransition getToCycleTransition(){
		for(StateTransition t : toes){
			if(t.cycle){
				return t;
			}
		}
		return null;
	}

	public ArrayList<StateNode> getToNoWeakNodes(){
		ArrayList<StateNode> toNodes = new ArrayList<StateNode>();
		for(StateTransition t : toes){
			if(!t.to.weak&&t.cycle){
				toNodes.add(t.to);
			}
		}
		return toNodes;
	}

	/*
	ArrayList<String> getToRuleNames(StateNode toNode){
		for(StateTransition t : toes){
			if(t.to==toNode){
				return t.getRules();
			}
		}
		return null;
	}
	*/

	public StateTransition getToTransition(StateNode toNode){
		for(StateTransition t : toes){
			if(t.to==toNode){
				return t;
			}
		}
		return null;
	}

	public StateTransition getToTransition(){
		for(StateTransition t : toes){
			return t;
		}
		return null;
	}

	public void setToTransition(LinkedHashSet<StateTransition> toes){
		this.toes = toes;
	}

	public String getToRuleName(StateNode toNode){
		for(StateTransition t : toes){
			if(t.to==toNode){
				return t.getRuleNameString();
			}
		}
		return "";
	}

	/*
	 * from系メソッド
	 */

	StateTransition addFromTransition(StateTransition f){
		if(isFromNode(f.from)) return null;
		froms.add(f);
		return f;
	}

	StateTransition removeFromTransition(StateTransition f){
		froms.remove(f);
		return f;
	}

	public Collection<StateTransition> getFromTransitions(){
		return froms;
	}

	StateTransition getFromTransition(StateNode fromNode){
		for(StateTransition f : froms){
			if(f.from==fromNode){
				return f;
			}
		}
		return null;
	}

	public StateTransition getFromTransition(){
		for(StateTransition f : froms){
			return f;
		}
		return null;
	}

	/*
	StateTransition removeFromNode(StateNode fromNode){
		StateTransition r = null;
		for(StateTransition f : froms){
			if(f.from==fromNode){
				r = f;
				break;
			}
		}
		froms.remove(r);
		return r;
	}
	 */

	boolean isFromNode(StateNode fromNode){
		return getFromNodes().contains(fromNode);
	}

	public ArrayList<StateNode> getFromNodes(){
		ArrayList<StateNode> fromNodes = new ArrayList<StateNode>();
		for(StateTransition f : froms){
			fromNodes.add(f.from);
		}
		return fromNodes;
	}

	public StateNode getFromNode(){
		for(StateTransition f : froms){
			return f.from;
		}
		return null;
	}

	StateNode getFromNearNode(){
		double minDis = Double.MAX_VALUE;
		StateNode minNode = null;
		for(StateNode node : getFromNodes()){
			if(node.depth!=this.depth-1){ continue; }
			if(minNode==null){ minNode=node; continue; }
			double dis = Math.abs(getY()-node.getY());
			if(dis<minDis){
				minDis = dis;
				minNode = node;
			}
		}
		return minNode;
	}

	public ArrayList<StateNode> getFromNoWeakNodes(){
		ArrayList<StateNode> fNodes = new ArrayList<StateNode>();
		for(StateNode from : getFromNodes()){
			if(!from.weak&&from.getToTransition(this).cycle){
				fNodes.add(from);
			}
		}
		return fNodes;
	}

	public void resetFromTransition(){
		froms.clear();
	}

	/*
	void addFromNode(StateNode fromNode){
		if(!getFromNodes().contains(fromNode)){
			fromNodes.add(fromNode);
		}
	}

	void removeFromNode(StateNode fromNode){
		fromNodes.remove(fromNode);
	}

	boolean isFromNode(StateNode fromNode){
		return getFromNodes().contains(fromNode);
	}

	public Collection<StateNode> getFromNodes(){
		return fromNodes;
	}

	public StateNode getFromNode(){
		for(StateNode from : fromNodes){
			return from;
		}
		return null;
	}

	void setFromNode(LinkedHashSet<StateNode> fromNodes){
		this.fromNodes = fromNodes;
	}

	public ArrayList<StateNode> getFromNoWeakNodes(){
		ArrayList<StateNode> fNodes = new ArrayList<StateNode>();
		for(StateNode from : fromNodes){
			if(!from.weak&&from.getToTransition(this).em){
				fNodes.add(from);
			}
		}
		return fNodes;
	}
	 */


	/*
	 * Layer系メソッド
	 */


	private Double[] sameLayerYs;
	private Double[] nextLayerYs;
	private Double[] backLayerYs;

	public void makeLayerNodeList(){
		TreeSet<Double> sameYs = new TreeSet<Double>();
		TreeSet<Double> nextYs = new TreeSet<Double>();
		TreeSet<Double> backYs = new TreeSet<Double>();

		for(StateTransition trans : getToTransitions()){
			if(trans.to.depth==depth&&trans.to!=this){
				sameYs.add(trans.to.getY());
			}else if(trans.to.depth==depth+1){
				nextYs.add(trans.to.getY());
			}else if(trans.to.depth==depth-1){
				backYs.add(trans.to.getY());
			}
		}
		for(StateTransition trans : getFromTransitions()){
			if(trans.from.depth==depth&&trans.from!=this){
				sameYs.add(trans.from.getY());
			}else if(trans.from.depth==depth+1){
				nextYs.add(trans.from.getY());
			}else if(trans.from.depth==depth-1){
				backYs.add(trans.from.getY());
			}
		}

		this.sameLayerYs = (Double[])sameYs.toArray(new Double[0]);
		this.nextLayerYs = (Double[])nextYs.toArray(new Double[0]);
		this.backLayerYs = (Double[])backYs.toArray(new Double[0]);
	}

	public Double[] getSameLayerYs(){
		return sameLayerYs;
	}

	public Double[] getNextLayerYs(){
		return nextLayerYs;
	}

	public Double[] getBackLayerYs(){
		return backLayerYs;
	}

	public Double[] getLayerYs(int cmp){
		if(cmp==-1){
			return backLayerYs;
		}else if(cmp==0){
			return sameLayerYs;
		}else if(cmp==1){
			return nextLayerYs;
		}else{
			return new Double[0];
		}
	}

	public void clearLayerNodeList(){
		sameLayerYs=null;
		nextLayerYs=null;
		backLayerYs=null;
	}


	/*
	 * Subset系
	 */

	public boolean hasSubset(){
		if(childSet==null){
			return false;
		}else{
			return true;
		}
	}

	public StateNodeSet getChildSet(){
		return childSet;
	}

	void setChildSet(StateNodeSet subset){
		this.childSet = subset;
	}

	void setCycleToNode(StateNode toNode, boolean cycle){
		StateTransition t = getToTransition(toNode);
		if(t!=null){
			t.cycle = cycle;
		}
	}

	/*
	void addEmToNode(StateNode toNode){
		StateTransition t = getTransition(toNode);
		if(t!=null){
			t.em = true;
		}
	}

	void removeEmToNode(StateNode toNode){
		StateTransition t = getTransition(toNode);
		if(t!=null){
			t.em = false;
		}
	}

	boolean isEmToNode(StateNode toNode){
		StateTransition t = getToTransition(toNode);
		if(t!=null){
			return t.cycle;
		}
		return false;
	}

	void resetEmToNode(){
		for(StateTransition t : toes){
			t.cycle = false;
		}
	}
	*/

	public ArrayList<StateNode> getLayerFlowNodes(int layer){
		ArrayList<StateNode> backs = new ArrayList<StateNode>();
		for(StateNode from : getFromNodes()){
			if(from.depth==layer){
				backs.add(from);
			}
		}
		for(StateNode to : getToNodes()){
			if(to.depth==layer){
				if(!backs.contains(to)){ backs.add(to); }
			}
		}
		return backs;
	}

	public void setRadius(double radius){
		this.radius = radius;
	}

	public double getRadius(){
		return radius;
	}

	public void setColor(Color color){
		this.color = color;
	}

	public Color getColor(){
		return color;
	}

	public boolean isAccept(){
		return accept;
	}

	public void setAccept(boolean accept){
		this.accept = accept;
	}

	public boolean isInFrame(){
		return inFrame;
	}

	public void setInFrame(boolean inFrame){
		this.inFrame = inFrame;
	}

	public void mark(){
		marked = true;
	}

	public void unmark(){
		marked = false;
	}

	public boolean isMarked(){
		return marked;
	}

	void resetLocation(double xPosInterval,double[] yPosInterval){
		double x = (depth+1)*xPosInterval;
		double y = yPosInterval[0];
		if(depth<yPosInterval.length){
			y = (nth+1)*yPosInterval[depth];
		}
		setPosition(x,y);
	}

	String getStringTo(int no){
		String fillcolor = Integer.toHexString((getColor().getRGB())&0xffffff);
		while(fillcolor.length()<6){ fillcolor = "0"+fillcolor; }
		String str = id+" [fillcolor=\"#"+fillcolor+"\",label=\""+no+"\"];\n";
		for(StateNode to : getToNodes()){
			str += id+" -> "+to.id+";\n";
		}
		return str;
	}

	public void runUnyo3()
	{
		File tempFile = new File("temp.lmn");
		try
		{
			FileWriter writer = new FileWriter(tempFile);
			writer.write(state);
			writer.close();

			FrontEnd.executeUnyo(tempFile);
		}
		catch (IOException e)
		{
			FrontEnd.printException(e);
		}
	}

	public void runGraphene()
	{
		File tempFile = new File("temp.lmn");
		try
		{
			FileWriter writer = new FileWriter(tempFile);
			writer.write(state);
			writer.close();

			FrontEnd.executeGraphene(tempFile);
		}
		catch (IOException e)
		{
			FrontEnd.printException(e);
		}
	}

	boolean isMatch(String str){
		if(dummy) return false;
		if(childSet==null){
			if(toString().lastIndexOf(str)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			for(StateNode node : childSet.getAllNode()){
				if(node.isMatch(str)){
					return true;
				}
			}
			return false;
		}
	}

	void doubleClick(StateGraphPanel graphPanel)
	{
		if (childSet == null)
		{
			UtilTextDialog.showDialog(String.valueOf(id), state);
		}
		else
		{
			graphPanel.init(childSet);
		}
	}

	void debugFrame(StateGraphPanel graphPanel){

		StringBuffer buf = new StringBuffer();
		buf.append("id:"+id+"\n");
		buf.append("state:"+toString()+"\n");
		buf.append("label:"+label+"\n");
		buf.append("accept:"+accept+"\n");
		buf.append("inCycle:"+cycle+"\n");
		buf.append("depth:"+depth+"\n");
		buf.append("nth:"+nth+"\n");
		buf.append("dummy:"+dummy+"\n");
		buf.append("weak:"+weak+"\n");
		buf.append("x:"+getX()+"\n");
		buf.append("y:"+getY()+"\n");

		buf.append("childSet:"+(childSet==null?"null":childSet.size())+"\n");
		buf.append("parentSet:"+(parentSet==null?"null":parentSet.size())+"\n");

		buf.append("to:");
		for(StateNode n : getToNodes()){ buf.append(n.id+", "); }
		buf.append("\n");
		buf.append("from:");
		for(StateNode n : getFromNodes()){ buf.append(n.id+", "); }
		buf.append("\n");

		UtilTextDialog.showDialog(String.valueOf(id), buf.toString());
	}

	public String toString(){
		if(state!=null&&state.length()>0){
			return state;
		}
		if(childSet!=null&&childSet.size()>0){
			return childSet.getRepresentationNode().toString();
		}
		return "";
	}

	@Override
	public boolean contains(Point2D p) {
		return shape.contains(p);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return shape.contains(r);
	}

	@Override
	public boolean contains(double x, double y) {
		return shape.contains(x,y);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return shape.contains(x,y,w,h);
	}

	@Override
	public Rectangle2D getBounds2D() {
		return shape.getBounds2D();
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return shape.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return shape.getPathIterator(at,flatness);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return shape.intersects(r);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return shape.intersects(x,y,w,h);
	}

	@Override
	public Rectangle getBounds() {
		return shape.getBounds();
	}

/*
	boolean isLmntalMatch(String head,String guard){
		File f = new File("temp.lmn");
		try {
			FileWriter fp = new FileWriter(f);
			fp.write("sVr_nomatch,"+state+"\n");
			fp.write("sVr_nomatch,"+head+" :- ");
			if(!guard.equals("")){ fp.write(guard+" | "); }
			fp.write(head+".");
            fp.close();
		} catch (IOException e) {
			FrontEnd.printException(e);
		}

		final LmntalRunner lr = new LmntalRunner("",f);
		lr.setBuffering(true);
		lr.run();

		while(lr.isRunning()){
			FrontEnd.sleep(200);
		}
		if(lr.isSuccess()){
			String str = lr.getBufferString();
			if(str.indexOf("sVr_nomatch")==-1){
				return true;
			}
		}
		return false;
	}
	*/

	/*
	private class TextDialog extends JDialog{

		TextDialog(String title,String state){

			super(FrontEnd.mainFrame);

			setTitle(title);

			JTextArea text = new JTextArea(state);
			text.setLineWrap(true);
			text.setFont(new Font(Env.get("EDITOR_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITOR_FONT_SIZE")));

		    add(text);

		    setVisible(true);

		}
	}
	 */

}
