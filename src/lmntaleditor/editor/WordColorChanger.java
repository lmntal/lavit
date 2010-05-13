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

package lmntaleditor.editor;

import java.awt.Color;
import javax.swing.text.BadLocationException;
import lmntaleditor.*;

public class WordColorChanger extends Thread {
	private AutoStyledDocument doc;
	private String text;
	private boolean updated;
	private boolean end;

	public WordColorChanger(AutoStyledDocument doc){
		this.doc = doc;
		this.text = "";
		this.updated = false;
		this.end = false;
	}

	public void run(){
		while(true){
			if(updated){
				change();
			}else{
				try {
					sleep(500);
				} catch (InterruptedException e) {
					FrontEnd.printException(e);
				}
			}
			if(end){ break; }
		}
	}

	public void change(){
		updated = false;
		javax.swing.SwingUtilities.invokeLater(new Runnable(){ public void run(){
			innerChange();
		}});
	}

	public void update(){
		updated = true;
	}

	public void end(){
		end = true;
	}

	private void innerChange(){
		if(Env.get("COLOR_TARGET").length()<=1) return;

		doc.startStyleEdit();

		text = doc.getPlainText();

		if(Env.get("COLOR_TARGET").indexOf("symbol")>=0){
			Color symbol = getColor("COLOR_SYMBOL");
			if(symbol==null) symbol = new Color(127,0,75);

			markUp(":-",symbol,true);

		}

		if(Env.get("COLOR_TARGET").indexOf("reserved")>=0){
			Color reserved = getColor("COLOR_RESERVED");
			if(reserved==null) reserved = new Color(127,0,75);

			markUp("ground",reserved,true);
			markUp("unary",reserved,true);
			markUp("int",reserved,true);
			markUp("float",reserved,true);
			markUp("string",reserved,true);
			markUp("class",reserved,true);
			markUp("uniq",reserved,true);
			//markUp("use",reserved,true);
			//markUp("module",reserved,true);
		}

		if(Env.get("COLOR_TARGET").indexOf("comment")>=0){
			Color comment = getColor("COLOR_COMMENT");
			if(comment==null) comment = new Color(90,167,127);

			markUp("//","\n",comment,false);
			markUp("%","\n",comment,false);
			markUp("/*","*/",comment,false);
		}
		doc.endStyleEdit();
	}

	private void markUp(String target,Color color,boolean bold){
		int pos = 0;
		while((pos=text.indexOf(target,pos))>=0){
			doc.setColor(pos,target.length(),color,bold);
			pos++;
		}
	}

	private void markUp(String start,String end,Color color,boolean bold){
		int sp = 0;
		while((sp=text.indexOf(start,sp))>=0){
			int ep;
			if((ep=text.indexOf(end,sp+start.length()))>=0){
				doc.setColor(sp,ep-sp+end.length(),color,bold);
				sp = ep+end.length();
			}else{
				doc.setColor(sp,doc.getLength()-sp,color,bold);
				break;
			}
		}
	}

	private Color getColor(String name){
		try{
			return Color.decode(Env.get(name));
		}catch(Exception e){
			FrontEnd.printException(e);
		}
		return null;
	}

}
