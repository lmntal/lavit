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

package lavit.visualeditor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.Env;
import lavit.stateviewer.StateGraphPanel;

public class VisualControlPanel extends JPanel implements ChangeListener,ActionListener{
	VisualPanel visualPanel;

	private JSlider zoomSlider = new JSlider(1,39);

	VisualControlPanel(VisualPanel visualPanel){
		this.visualPanel = visualPanel;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		zoomSlider.addChangeListener(this);
		add(zoomSlider);
	}

	public void allButtonSetEnabled(boolean enabled){
		zoomSlider.setEnabled(enabled);
	}

	public void setSliderPos(double z){
		int pos = (int)(Math.sqrt(z*100)*2-1);
		if(pos<1){ pos=1; }else if(pos>39){ pos=39; }
		zoomSlider.removeChangeListener(this);
		zoomSlider.setValue(pos);
		zoomSlider.addChangeListener(this);
	}
	public void toggleZoomSliderVisible(){
		zoomSlider.setVisible(!zoomSlider.isVisible());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		double z = (zoomSlider.getValue()+1)/2.0;
		visualPanel.drawPanel.setZoom(z*z/100.0);
		visualPanel.drawPanel.update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
