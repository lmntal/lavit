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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.Env;
import lavit.stateviewer.*;
import lavit.stateviewer.s3d.*;
import lavit.stateviewer.worker.State3DDynamicMover;

public class State3DControlPanel extends JPanel implements ChangeListener,ActionListener {
	private StatePanel statePanel;

	private JPanel dynamicPanel = new JPanel();
	private JCheckBox activeCheckBox = new JCheckBox("3D Modeling");
	private JCheckBox dynamicModeling = new JCheckBox("Dynamic 3D Modeling");
	private JButton updateButton = new JButton("Update");
	private JButton resetButton = new JButton("Reset");

	private JPanel parameterPanel = new JPanel();
	private JLabel xLabel = new JLabel();
	private JSlider xSlider = new JSlider(1,50);
	private JLabel yLabel = new JLabel();
	private JSlider ySlider = new JSlider(1,50);
	private JLabel zLabel = new JLabel();
	private JSlider zSlider = new JSlider(1,50);

	private JLabel labels[] = {xLabel,yLabel,zLabel};
	private JSlider sliders[] = {xSlider,ySlider,zSlider};

	private JPanel optionPanel = new JPanel();
	private JCheckBox drawAxis = new JCheckBox("Draw Axis");
	private JCheckBox startupReset = new JCheckBox("Startup 3D Reset");

	State3DControlPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		dynamicPanel.setLayout(new GridLayout(1,4));
		dynamicPanel.setMaximumSize(new Dimension(1000,30));
		dynamicPanel.setBorder(new EmptyBorder(2,5,2,5));

		activeCheckBox.addActionListener(this);
		activeCheckBox.setSelected(Env.is("SV3D"));
		dynamicPanel.add(activeCheckBox);

		dynamicModeling.addActionListener(this);
		dynamicModeling.setEnabled(activeCheckBox.isSelected());
		dynamicModeling.setSelected(false);
		dynamicPanel.add(dynamicModeling);

		updateButton.addActionListener(this);
		dynamicPanel.add(updateButton);

		resetButton.addActionListener(this);
		dynamicPanel.add(resetButton);

		xSlider.setValue(Env.getInt("SV3D_X_SCALE"));
		ySlider.setValue(Env.getInt("SV3D_Y_SCALE"));
		zSlider.setValue(Env.getInt("SV3D_Z_SCALE"));

		setSubEnabled(Env.is("SV3D"));
		add(dynamicPanel);

		parameterPanel.setLayout(new GridLayout(labels.length, 2));
		parameterPanel.setMaximumSize(new Dimension(1000,140));
		parameterPanel.setBorder(new TitledBorder("Parameter"));
		for(int i=0;i<labels.length;++i){
			parameterPanel.add(labels[i]);
			sliders[i].addChangeListener(this);
			parameterPanel.add(sliders[i]);
		}
		add(parameterPanel);
		stateUpdate();

		optionPanel.setLayout(new GridLayout(1,2));
		optionPanel.setMaximumSize(new Dimension(1000,30));
		optionPanel.setBorder(new EmptyBorder(2,5,2,5));

		startupReset.addActionListener(this);
		startupReset.setSelected(Env.is("SV3D_AUTO_RESET"));
		optionPanel.add(startupReset);

		drawAxis.addActionListener(this);
		drawAxis.setSelected(Env.is("SV3D_DRAW_AXIS"));
		optionPanel.add(drawAxis);

		add(optionPanel);

	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		activeCheckBox.setEnabled(enabled);
		setSubEnabled(enabled);
	}

	public void setSubEnabled(boolean enabled){
		updateButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		State3DPanel state3DPanel = statePanel.state3DPanel;

		if(src==activeCheckBox){
			statePanel.toggle3D();
			Env.set("SV3D",!Env.is("SV3D"));
			setSubEnabled(Env.is("SV3D"));
			dynamicModeling.setEnabled(true);
		}else if(src==updateButton){
			state3DPanel.updateGraph();
		}else if(src==resetButton){
			state3DPanel.createGraph();
		}else if(src==dynamicModeling){
			statePanel.state3DPanel.setDynamicMoverActive(dynamicModeling.isSelected());
		}else if(src==startupReset){
			Env.set("SV3D_AUTO_RESET",!Env.is("SV3D_AUTO_RESET"));
		}else if(src==drawAxis){
			Env.set("SV3D_DRAW_AXIS",!Env.is("SV3D_DRAW_AXIS"));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Env.set("SV3D_X_SCALE", xSlider.getValue());
		Env.set("SV3D_Y_SCALE", ySlider.getValue());
		Env.set("SV3D_Z_SCALE", zSlider.getValue());
		stateUpdate();
	}

	public void stateUpdate(){
		xLabel.setText(" x scale : "+xSlider.getValue());
		yLabel.setText(" y scale : "+ySlider.getValue());
		zLabel.setText(" z scale : "+zSlider.getValue());
	}

}