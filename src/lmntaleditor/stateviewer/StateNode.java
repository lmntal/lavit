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

package lmntaleditor.stateviewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import lmntaleditor.Env;
import lmntaleditor.FrontEnd;
import lmntaleditor.editor.AutoStyledDocument;
import lmntaleditor.editor.NoWrapEditorKit;
import lmntaleditor.frame.ChildWindowListener;
import lmntaleditor.runner.LmntalRunner;
import lmntaleditor.runner.UnyoRunner;

class StateNode implements Shape{
	public long id;
	public int no;
	public int depth = 0;
	public int nth = 0;
	public String state;
	boolean accept;
	boolean inCycle;

	boolean dummy;
	boolean weak;

	private ArrayList<StateTransition> toes;

	//private ArrayList<StateNode> toNodes;
	private ArrayList<StateNode> fromNodes;

	//private ArrayList<StateNode> emToNodes;

	private boolean marked;
	private boolean inFrame;

	private Shape shape;
	private double radius;
	private Color fillColor;
	private Color drawColor;
	//private ColorMode colorMode;
	//enum ColorMode { normal, em, weak };

	//物理モデル用変数
	double dy;
	double ddy;

	StateNode(long id){
		this.id = id;
	}

	void init(int no,String state,boolean accept,boolean inCycle){
		this.no = no;
		this.depth = 0;
		this.nth = 0;
		this.state = state;
		this.accept = accept;
		this.inCycle = inCycle;

		this.dummy = false;
		this.weak = false;

		//this.toNodes = new ArrayList<StateNode>();
		this.toes = new ArrayList<StateTransition>();
		this.fromNodes = new ArrayList<StateNode>();

		//this.emToNodes = new ArrayList<StateNode>();

		this.marked = false;
		this.shape = new RoundRectangle2D.Double(0,0,0,0,0,0);

		updateLooks();
	}

	void updateLooks(){

		//double to = toNodes.size();
		double to = toes.size();

		double from = fromNodes.size();

		double r = 0;
		double g = 0;
		double b = 0;

		/*
		if(to<from){
			if(to*2<from){
				r = 255;
				g = 255*Math.sqrt(to*2/from);
			}else{
				g = 255;
				r = 255*Math.sqrt(to/from);
			}
		}else if(to>from){
			if(to>from*2){
				b = 255;
				g = 255*Math.sqrt(from*2/to);
			}else{
				g = 255;
				b = 255*Math.sqrt(from/to);
			}
		}else{
			g = 255;
		}
		*/


		if(from<to){
			if(from*2<to){
				r = 0;
				g = 255*Math.sqrt(from*2/to);
				b = 255;
			}else if(from*2==to){
				r = 0;
				g = 255;
				b = 255;
			}else if(from*2>to){
				r = 0;
				g = 255;
				b = 255*Math.sqrt(to/from-1);
			}
		}else if(from==to){
			r = 0;
			g = 255;
			b = 0;
		}else if(from>to){
			if(from<to*2){
				r = 255*Math.sqrt(from/to-1);
				g = 255;
				b = 0;
			}else if(from==to*2){
				r = 255;
				g = 255;
				b = 0;
			}else if(from>to*2){
				r = 255;
				g = 255*Math.sqrt(to*2/from);
				b = 0;
			}
		}

/*
		if(from<to){
			r = 0;
			g = 255*Math.sqrt(from/to);
			b = 255*Math.sqrt(1-from/to);
		}else if(from==to){
			r = 0;
			g = 255;
			b = 0;
		}else if(from>to){
			r = 255*Math.sqrt(1-to/from);
			g = 255*Math.sqrt(to/from);
			b = 0;
		}
*/

		/*
		switch(colorMode){
			case normal:
				radius = 5.0;
				fillColor = new Color((int)r,(int)g,(int)b);
				drawColor = Color.gray;
				break;
			case em:
				radius = 6.0;
				fillColor = new Color((int)r,(int)g,(int)b);
				drawColor = Color.black;
				break;
			case weak:
				radius = 4.0;
				fillColor = Color.white;
				drawColor = new Color((int)r,(int)g,(int)b);
				break;
			case dummy:
				radius = 0.0;
				fillColor = Color.black;
				drawColor = Color.black;
				break;
			case showdummy:
				radius = 3.0;
				fillColor = Color.white;
				drawColor = Color.gray;
				break;
		}
		*/

		if(dummy){
			if(Env.is("SV_SHOW_DUMMY")){
				radius = 2.0;
			}else{
				radius = 0.0;
			}
			fillColor = Color.white;
			drawColor = Color.gray;
		}else if(weak){
			radius = 3.0;
			fillColor = Color.white;
			drawColor = new Color((int)r,(int)g,(int)b);
		}else{
			radius = 5.0;
			fillColor = new Color((int)r,(int)g,(int)b);
			drawColor = Color.gray;
		}

		shape = new RoundRectangle2D.Double(getX()-radius,getY()-radius,radius*2,radius*2,radius*2,radius*2);

		/*
		if(toIds.size()==0){
			color = Color.red;
		}else if(toIds.size()==1){
			color = Color.blue;
		}else if(toIds.size()==2){
			color = Color.orange;
		}else if(toIds.size()==3){
			color = Color.green;
		}else if(toIds.size()==4){
			color = Color.yellow;
		}else{
			color = Color.cyan;
		}
		*/
	}

	synchronized double getX(){
		return ((RectangularShape)shape).getCenterX();
	}

	synchronized double getY(){
		return ((RectangularShape)shape).getCenterY();
	}

	synchronized void setX(double x){
		setPosition(x,getY());
	}

	synchronized void setY(double y){
		setPosition(getX(),y);
	}

	synchronized void move(double dx, double dy){
		setPosition(getX()+dx, getY()+dy);
	}

	synchronized void setPosition(double x,double y){
		if(!(-1000000000<y&&y<1000000000)){ y = 0; }
		((RectangularShape)shape).setFrame(x-radius, y-radius, radius*2, radius*2);
	}

	StateTransition addToNode(StateNode toNode,ArrayList<String> rules){
		if(isToNode(toNode)) return null;

		StateTransition t = new StateTransition(this,toNode);
		t.addRules(rules);
		toes.add(t);
		return t;

	}

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

	boolean isToNode(StateNode toNode){
		return getToNodes().contains(toNode);
	}


	ArrayList<StateNode> getToNodes(){
		ArrayList<StateNode> toNodes = new ArrayList<StateNode>();
		for(StateTransition t : toes){
			toNodes.add(t.to);
		}
		return toNodes;
	}

	StateNode getEmToNode(){
		for(StateTransition t : toes){
			if(t.em){
				return t.to;
			}
		}
		return null;
	}

	StateNode getOneFromNode(){
		double minDis = Double.MAX_VALUE;
		StateNode minNode = null;
		for(StateNode node : fromNodes){
			if(node.depth!=this.depth-1){ continue; }
			double dis = Math.abs(getY()-node.getY());
			if(dis<minDis){
				minDis = dis;
				minNode = node;
			}
		}
		return minNode;
	}

	ArrayList<StateNode> getToNoWeakNodes(){
		ArrayList<StateNode> toNodes = new ArrayList<StateNode>();
		for(StateTransition t : toes){
			if(!t.to.weak&&t.em){
				toNodes.add(t.to);
			}
		}
		return toNodes;
	}

	ArrayList<String> getToRuleNames(StateNode toNode){
		for(StateTransition t : toes){
			if(t.to==toNode){
				return t.getRules();
			}
		}
		return null;
	}

	StateTransition getTransition(StateNode toNode){
		for(StateTransition t : toes){
			if(t.to==toNode){
				return t;
			}
		}
		return null;
	}

	ArrayList<StateTransition> getTransition(){
		return toes;
	}

	String getToRuleName(StateNode toNode){
		StringBuffer buf = new StringBuffer();
		for(StateTransition t : toes){
			if(t.to==toNode){
				return t.getRuleNameString();
			}
		}
		return "";
	}

	void addFromNode(StateNode fromNode){
		if(!fromNodes.contains(fromNode)){
			fromNodes.add(fromNode);
		}
	}

	void removeFromNode(StateNode fromNode){
		fromNodes.remove(fromNode);
	}

	boolean isFromNode(StateNode fromNode){
		return fromNodes.contains(fromNode);
	}

	ArrayList<StateNode> getFromNodes(){
		return fromNodes;
	}

	ArrayList<StateNode> getFromNoWeakNodes(){
		ArrayList<StateNode> fNodes = new ArrayList<StateNode>();
		for(StateNode from : fromNodes){
			if(!from.weak&&from.getTransition(this).em){
				fNodes.add(from);
			}
		}
		return fNodes;
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
	*/

	void setEmToNode(StateNode toNode,boolean em){
		StateTransition t = getTransition(toNode);
		if(t!=null){
			t.em = em;
		}
	}

	boolean isEmToNode(StateNode toNode){
		StateTransition t = getTransition(toNode);
		if(t!=null){
			return t.em;
		}
		return false;
	}

	void resetEmToNode(){
		for(StateTransition t : toes){
			t.em = false;
		}
	}

	ArrayList<StateNode> getLayerFlowNodes(int layer){
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

	double getRadius(){
		return radius;
	}

	Color getDrawColor(){
		return drawColor;
	}

	Color getFillColor(){
		return fillColor;
	}

	boolean isAccept(){
		return accept;
	}

	void setAccept(boolean accept){
		this.accept = accept;
	}

	boolean isInFrame(){
		return inFrame;
	}

	void setInFrame(boolean inFrame){
		this.inFrame = inFrame;
	}

	void mark(){
		marked = true;
	}

	void unmark(){
		marked = false;
	}

	boolean isMarked(){
		return marked;
	}

	void resetLocation(double xPosInterval,double[] yPosInterval){
		double x = (depth+1)*xPosInterval;
		double y = (nth+1)*yPosInterval[depth];
		setPosition(x,y);
	}

	String getStringTo(int no){
		String fillcolor = Integer.toHexString((getFillColor().getRGB())&0xffffff);
		while(fillcolor.length()<6){ fillcolor = "0"+fillcolor; }
		String str = id+" [fillcolor=\"#"+fillcolor+"\",label=\""+no+"\"];\n";
		for(StateNode to : getToNodes()){
			str += id+" -> "+to.id+";\n";
		}
		return str;
	}

	void runUnyo2(){
		File f = new File("temp.lmn");
		try {
			FileWriter fp = new FileWriter(f);
			fp.write(state);
            fp.close();
		} catch (IOException e) {
			FrontEnd.printException(e);
		}
		(new LmntalRunner("-g "+Env.get("UNYO_OPTION"),f)).run();
	}

	void runUnyo3(){
		File f = new File("temp.lmn");
		try {
			FileWriter fp = new FileWriter(f);
			fp.write(state);
            fp.close();
		} catch (IOException e) {
			FrontEnd.printException(e);
		}
		(new UnyoRunner(Env.get("UNYO_OPTION"),f)).run();
	}

	boolean isMatch(String str){
		if(dummy) return false;
		if(state.lastIndexOf(str)>=0){
			return true;
		}else{
			return false;
		}
	}

	void showFrame(int x, int y){
		TextFrame frame = new TextFrame(""+id,state);
		//frame.setBounds(x,y,300,100);
	}

	private class TextFrame extends JFrame{

		TextFrame(String title,String state){

			int width = FrontEnd.mainFrame.getWidth()/2;
			int height = FrontEnd.mainFrame.getHeight()/2;
			int x = FrontEnd.mainFrame.getX();
			int y = FrontEnd.mainFrame.getY();

			setSize(width,height);
			setLocation(x,y);
			setTitle(title);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			/*
			JTextArea text = new JTextArea(state);
			text.setLineWrap(true);
			text.setFont(new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE")));
			*/
			AutoStyledDocument doc = new AutoStyledDocument();
			JTextPane editor = new JTextPane();
			//editor.setEditorKit(new NoWrapEditorKit());
			editor.setDocument(doc);
			editor.setFont(new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE")));
			editor.setText(state);
			doc.colorChange();
			doc.end();

		    add(new JScrollPane(editor));

		    addWindowListener(new ChildWindowListener(this));

		    setVisible(true);

		}

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
	private class TextDialog extends JDialog{

		TextDialog(String title,String state){

			super(FrontEnd.mainFrame);

			setTitle(title);

			JTextArea text = new JTextArea(state);
			text.setLineWrap(true);
			text.setFont(new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE")));

		    add(text);

		    setVisible(true);

		}
	}
	*/

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

}
