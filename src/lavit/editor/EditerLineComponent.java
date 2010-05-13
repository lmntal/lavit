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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * 行番号を表示するコンポーネント
 */
public class EditerLineComponent extends JComponent{
	private int digit;
	private int ascent;
	private int areaTop;
	private int fontWidth;
	private int lineHeight;
	private int caretPos;

	private Font font;

	EditerLineComponent(int editerHeight) {
		digit = 2;
		caretPos = 1;
	}

	public void setFont(Font font){
		this.font = font;
	}

	public void setEditorMargin(Insets editorMargin){
		FontMetrics fm = getFontMetrics(font);
		ascent = fm.getAscent();
		areaTop = editorMargin.top;
		fontWidth = fm.stringWidth("0");
		lineHeight = ascent + fm.getDescent();
	}

	void setHeightByLines(int lines) {
		digit = Integer.toString(lines).length();
		if(digit<2){
			digit = 2;
		}
		int height = ascent + (lines * lineHeight);
		setPreferredSize(new Dimension(fontWidth*digit+10, areaTop+height));
		revalidate();
	}

	void updateCaretPos(int pos){
		caretPos = pos;
		//revalidate();
		repaint();
	}

	public void paintComponent(Graphics g) {
		Rectangle bounds = g.getClipBounds();
		g.setColor(Color.white);
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
		g.setColor(new Color(220,220,240));
		g.fillRect(bounds.x, bounds.y, bounds.width-5, bounds.height);
		g.setColor(Color.black);
		g.drawLine(bounds.x+bounds.width-5, bounds.y, bounds.x+bounds.width-5, bounds.y+bounds.height);
		g.setFont(font);
		int startY = (bounds.y/lineHeight)*lineHeight + ascent + areaTop;
		int endY = (((bounds.y+bounds.height)/lineHeight)+1)*lineHeight+ascent + areaTop;
		int n = (int)Math.floor(bounds.y/lineHeight)+1;
		for (int y=startY;y<endY;y+=lineHeight) {
			String nstr = Integer.toString(n);
			if(n==caretPos){
				g.setColor(new Color(100,100,200));
				g.fillRect(bounds.x,y-ascent,bounds.width-5,lineHeight);
				g.setColor(Color.white);
			}else{
				g.setColor(Color.black);
			}
			g.drawString(nstr, 2+fontWidth*(digit-nstr.length()), y);
			n++;
		}
	}
}
