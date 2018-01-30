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

package lavit.editor;

import java.awt.Color;

import javax.swing.event.*;
import javax.swing.text.*;

import lavit.FrontEnd;

public class AutoStyledDocument extends DefaultStyledDocument{
	WordColorChanger wcc;

	public AutoStyledDocument(){
		wcc = new WordColorChanger(this);
		wcc.start();
	}

	public void colorUpdate(){
		wcc.update();
	}

	public void colorChange(){
		wcc.change();
	}

	public void end(){
		wcc.end();
	}

	public String getPlainText(){
		try {
			return getText(0,getLength()).replaceAll("\r\n","\n");
		}catch (BadLocationException e){
			FrontEnd.printException(e);
		}
		return "";
	}

	public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
		if (length == 0) { return; }
		try {
			//writeLock();
			InsignificantDocumentEvent changes = new InsignificantDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
			buffer.change(offset, length, changes);
			AttributeSet sCopy = s.copyAttributes();
			int lastEnd = Integer.MAX_VALUE;
			for (int pos = offset; pos < (offset + length); pos = lastEnd) {
				Element run = getCharacterElement(pos);
				lastEnd = run.getEndOffset();
				if (pos == lastEnd) { break; }
				MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
				changes.addEdit(new InsignificantUndoableEdit(run, sCopy, replace));
				if (replace) { attr.removeAttributes(attr); }
				attr.addAttributes(s);
			}
			changes.end();
			fireChangedUpdate(changes);
			fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
		} finally {
			//writeUnlock();
		}
	}

	public void setColor(int offset, int length, Color c, boolean bold){
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		attribute.addAttribute(StyleConstants.Foreground, c);
		attribute.addAttribute(StyleConstants.FontConstants.Bold,bold);
		if(offset+length<=getLength()){
			setCharacterAttributes(offset,length,attribute,false);
		}
	}

	public void startStyleEdit(){
		writeLock();
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		setCharacterAttributes(0,getLength(),attribute,true);
	}

	public void endStyleEdit(){
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		setCharacterAttributes(getLength(),0,attribute,true);
		writeUnlock();
	}

	public class InsignificantDocumentEvent extends DefaultDocumentEvent{

		public InsignificantDocumentEvent(int offs, int len, EventType type) {
			super(offs, len, type);
		}

		public boolean isSignificant(){
			return false;
		}

	}

	public class InsignificantUndoableEdit extends AttributeUndoableEdit{

		public InsignificantUndoableEdit(Element element, AttributeSet newAttributes, boolean isReplacing) {
			super(element, newAttributes, isReplacing);
		}

		public boolean isSignificant() {
			return false;
		}

	}

	// -- タブに関する実装 --
	
	private static final class CustomTabSet extends TabSet
	{
		private static final long serialVersionUID = 1L;

		private int _width;

		public CustomTabSet(int width)
		{
			super(new TabStop[] { new TabStop(width) });
			_width = width;
		}

		@Override
		public TabStop getTabAfter(float x)
		{
			return new TabStop((int)Math.ceil(x / _width) * _width);
		}
	}

	public void setTabWidth(int width)
	{
		TabSet tabSet = new CustomTabSet(width);
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setTabSet(attr, tabSet);
		setParagraphAttributes(0, getLength(), attr, false);
	}
}
