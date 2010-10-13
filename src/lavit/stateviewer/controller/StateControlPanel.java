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

package lavit.stateviewer.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.*;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;
import lavit.util.CommonFontUser;
import lavit.util.FixFlowLayout;

public class StateControlPanel extends JPanel implements ChangeListener {

	private StatePanel statePanel;

	/*
	private JPanel control = new JPanel();
	private JButton delete = new JButton("Delete");
    private JButton reset = new JButton("All Reset");
    */



    public StateControlTab stateControlTab;

    private JSlider zoomSlider = new JSlider(1,399);

    private JPanel infoPanel = new JPanel();
    private JLabel zoomNum = new JLabel();
	private JLabel stateNum = new JLabel();

	public StateControlPanel(StatePanel statePanel){

		this.statePanel = statePanel;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		stateControlTab = new StateControlTab(statePanel);
		add(stateControlTab);

		zoomSlider.addChangeListener(this);
		zoomSlider.setVisible(Env.is("SV_ZOOMSLIDER"));
		add(zoomSlider);

		infoPanel.setLayout(new GridLayout(1,2));
		zoomNum.setHorizontalAlignment(JLabel.LEFT);
		infoPanel.add(zoomNum);
		stateNum.setHorizontalAlignment(JLabel.RIGHT);
		infoPanel.add(stateNum);

		infoPanel.setVisible(Env.is("SV_INFO"));
		add(infoPanel);

		setEnabled(false);
		setDrawInfo(0,0);
		setStateInfo(0,0,0);
	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		stateControlTab.setEnabled(enabled);
		zoomSlider.setEnabled(enabled);
	}

	public void setSliderPos(double z){
		int pos = (int)(Math.sqrt(z*10000)*2-1);
		if(pos<1){ pos=1; }else if(pos>399){ pos=399; }
		zoomSlider.removeChangeListener(this);
		zoomSlider.setValue(pos);
		zoomSlider.addChangeListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		double z = (zoomSlider.getValue()+1)/2.0;
		statePanel.stateGraphPanel.setInnerZoom(z*z/10000.0);
		statePanel.stateGraphPanel.update();
	}

	public void updateInfo(){
		StateGraphPanel p = statePanel.stateGraphPanel;
		setDrawInfo(p.getZoom(),p.getDrawTime());
		setStateInfo(p.getDepth()-1,p.getAllNum(),p.getEndNum());
	}

	public void toggleZoomSliderVisible(){
		zoomSlider.setVisible(!zoomSlider.isVisible());
		Env.set("SV_ZOOMSLIDER",zoomSlider.isVisible());
	}

	public void toggleInfoVisible(){
		infoPanel.setVisible(!infoPanel.isVisible());
		Env.set("SV_INFO",infoPanel.isVisible());
	}

	private void setDrawInfo(double zoom,double drawTime){
		String z = ""+(int)(zoom*100);
		if(zoom<0.01){
			z = ""+(((int)(zoom*10000))/100.0);
		}
		zoomNum.setText(" Zoom : "+z+"%, DrawTime : "+(drawTime/1000)+"s");
	}

	private void setStateInfo(int depth,int num,int end){
		stateNum.setText(" Depth : "+depth+", State : "+num+", (End : "+end+") ");
	}

}
