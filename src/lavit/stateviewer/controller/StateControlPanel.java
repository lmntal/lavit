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

public class StateControlPanel extends JPanel implements ChangeListener,ActionListener,CommonFontUser {

	private StatePanel statePanel;

	/*
	private JPanel control = new JPanel();
	private JButton delete = new JButton("Delete");
    private JButton reset = new JButton("All Reset");
    */

	private JPanel matchPanel = new JPanel();
	private JLabel matchLabel = new JLabel();
	private JPanel matchHeadPanel = new JPanel();
	private JTextField matchHeadField = new JTextField();
	private JPanel matchGuardPanel = new JPanel();
	private JTextField matchGuardField = new JTextField();
    private JButton matchButton = new JButton("Match");

	private JPanel findPanel = new JPanel();
	private JLabel findLabel = new JLabel();
	private JTextField findField = new JTextField();
    private JButton findButton = new JButton("Find");

    private StateDynamicControlPanel dynamicPanel;

    public StateButtonPanel buttonPanel;

    private JSlider zoomSlider = new JSlider(1,399);

    private JPanel infoPanel = new JPanel();
    private JLabel zoomNum = new JLabel();
	private JLabel stateNum = new JLabel();

	public StateControlPanel(StatePanel statePanel){

		this.statePanel = statePanel;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		/*
		delete.setFocusable(false);
		delete.addActionListener(this);
		control.add(delete);

		dot.setFocusable(false);
		dot.addActionListener(this);
		control.add(dot);

		reset.setFocusable(false);
		reset.addActionListener(this);
		control.add(reset);

		add(control);
		*/

		matchButton.addActionListener(this);
		matchHeadField.addActionListener(this);
		matchGuardField.addActionListener(this);
		matchHeadPanel.setLayout(new BorderLayout());
		matchHeadPanel.add(new JLabel(" Head:"),BorderLayout.WEST);
		matchHeadPanel.add(matchHeadField, BorderLayout.CENTER);
		matchGuardPanel.setLayout(new BorderLayout());
		matchGuardPanel.add(new JLabel(" Guard:"),BorderLayout.WEST);
		matchGuardPanel.add(matchGuardField, BorderLayout.CENTER);
		JPanel l = new JPanel(new GridLayout(1,2));
		l.add(matchHeadPanel);
		l.add(matchGuardPanel);
		matchPanel.setLayout(new BorderLayout());
		matchPanel.add(matchLabel,BorderLayout.WEST);
		matchPanel.add(l, BorderLayout.CENTER);
		matchPanel.add(matchButton, BorderLayout.EAST);
		matchPanel.setVisible(Env.is("SV_MATCH"));
		add(matchPanel);


		findButton.addActionListener(this);
		findField.addActionListener(this);
		findPanel.setLayout(new BorderLayout());
		findPanel.add(findLabel,BorderLayout.WEST);
		findPanel.add(findField, BorderLayout.CENTER);
		findPanel.add(findButton, BorderLayout.EAST);
		findPanel.setVisible(Env.is("SV_FIND"));
		add(findPanel);

		dynamicPanel = new StateDynamicControlPanel(statePanel);
		dynamicPanel.setVisible(Env.is("SV_DYNAMIC_CONTROL"));
		add(dynamicPanel);

		buttonPanel = new StateButtonPanel(statePanel);
		buttonPanel.setVisible(Env.is("SV_BUTTON"));
		add(buttonPanel);

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

		allButtonSetEnabled(false);
		setDrawInfo(0,0);
		setStateInfo(0,0,0);

		loadFont();
		FrontEnd.addFontUser(this);
	}

	public void loadFont(){
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		matchHeadField.setFont(font);
		matchGuardField.setFont(font);
		findField.setFont(font);
		revalidate();
	}

	public void allButtonSetEnabled(boolean enabled){
		matchHeadField.setEnabled(enabled);
		matchGuardField.setEnabled(enabled);
		matchButton.setEnabled(enabled);
		findField.setEnabled(enabled);
		findButton.setEnabled(enabled);
		dynamicPanel.allSetEnabled(enabled);
		buttonPanel.allSetEnabled(enabled);
		zoomSlider.setEnabled(enabled);
	}

	public void toggleMatchVisible(){
		matchPanel.setVisible(!matchPanel.isVisible());
		Env.set("SV_MATCH",matchPanel.isVisible());
	}

	public void toggleFindVisible(){
		findPanel.setVisible(!findPanel.isVisible());
		Env.set("SV_FIND",findPanel.isVisible());
	}

	public void toggleDynamicVisible(){
		dynamicPanel.setVisible(!dynamicPanel.isVisible());
		Env.set("SV_DYNAMIC_CONTROL",dynamicPanel.isVisible());
	}

	public void toggleButtonVisible(){
		buttonPanel.setVisible(!buttonPanel.isVisible());
		Env.set("SV_BUTTON",buttonPanel.isVisible());
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

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==matchButton||src==matchHeadField||src==matchGuardField){
			matchLabel.setText("matching...");
			(new Thread(new Runnable() { public void run() {
				final int match = statePanel.stateGraphPanel.stateMatch(matchHeadField.getText(),matchGuardField.getText());
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					if(match>=0){
						matchLabel.setText(" match : "+match+" state ");
					}else{
						matchLabel.setText(" error! ");
					}
				}});
			}})).start();
		}else if(src==findButton||src==findField){
			findLabel.setText("finding...");
			(new Thread(new Runnable() { public void run() {
				final int match = statePanel.stateGraphPanel.stateFind(findField.getText());
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					findLabel.setText(" \""+findField.getText()+"\" match : "+match+" state ");
				}});
			}})).start();
		}
	}

}
