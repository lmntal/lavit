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

package lavit.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.runner.SlimInstaller;

public class SlimPathSettingFrame extends JFrame {
	private SelectPanel panel;
	private boolean end;

	public SlimPathSettingFrame(){

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("SLIM");
        setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
        setAlwaysOnTop(true);
        setResizable(false);

        panel = new SelectPanel(this);
        add(panel);

        addWindowListener(new ChildWindowListener(this));

        pack();
        setLocationRelativeTo(FrontEnd.mainFrame);
        setVisible(true);

        end = false;
	}

	public boolean isEnd(){
		return end;
	}

	public void end(){
		end = true;
	}

	private class SelectPanel extends JPanel implements ActionListener {
		private JFrame frame;

		private ButtonGroup group = new ButtonGroup();
		private JRadioButton installed = new JRadioButton(Lang.w[13]+Env.getSlimBinaryName()+Lang.w[14]);
		private JRadioButton install = new JRadioButton(Lang.w[2]+Env.getSlimBinaryName()+Lang.w[3]);
		private JRadioButton include = new JRadioButton(Lang.w[4]+Env.getSlimBinaryName()+Lang.w[5]);
		private JRadioButton input = new JRadioButton(Lang.w[6]+Env.getSlimBinaryName()+Lang.w[7]);

		private JTextField pathInput = new JTextField();
		private JButton fileChooser = new JButton(Lang.w[0]);

		private JButton ok = new JButton(Lang.d[6]);
		private JButton cancel = new JButton(Lang.d[2]);

		//private String defaultInstallPath = Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfSlim()+File.separator+"src"+File.separator+Env.getSlimBinaryName();
		private String defaultInstallPath = Env.getSlimInstallPath()+File.separator+"bin"+File.separator+Env.getSlimBinaryName();
		private String defaultIncludePath = Env.LMNTAL_LIBRARY_DIR+File.separator+"bin"+File.separator+Env.getSlimBinaryName();

		SelectPanel(JFrame frame){
			this.frame = frame;

			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			JLabel label = new JLabel();
			label.setText(Lang.w[8]+Env.getSlimBinaryName()+Lang.w[9]);
			label.setPreferredSize(new Dimension(250, 40));
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(label);

			JPanel radioPanel = new JPanel();
			radioPanel.setLayout(new GridLayout(4,1));


			installed.addActionListener(this);
			installed.setMargin(new Insets(2,10,2,10));
			group.add(installed);
			radioPanel.add(installed);
			if(!(new File(defaultInstallPath)).exists()){
				installed.setEnabled(false);
			}

			install.addActionListener(this);
			install.setMargin(new Insets(2,10,2,10));
			group.add(install);
			radioPanel.add(install);

			include.addActionListener(this);
			include.setMargin(new Insets(2,10,2,10));
			group.add(include);
			radioPanel.add(include);

			input.addActionListener(this);
			input.setMargin(new Insets(2,10,2,10));
			group.add(input);
			radioPanel.add(input);

			add(radioPanel);


			JPanel inputPanel = new JPanel();

			pathInput.setPreferredSize(new Dimension(200,20));
			inputPanel.add(pathInput);

			fileChooser.addActionListener(this);
			inputPanel.add(fileChooser);

			add(inputPanel);


			JPanel buttonPanel = new JPanel();

			ok.addActionListener(this);
			buttonPanel.add(ok);

			cancel.addActionListener(this);
			buttonPanel.add(cancel);

			add(buttonPanel);


			if(getSlimPath().equals("")||getSlimPath().equals(defaultInstallPath)){
				if((new File(defaultInstallPath)).exists()){
					installed.setSelected(true);
					installed.requestFocus();
				}else{
					install.setSelected(true);
					install.requestFocus();
				}
				pathInput.setEditable(false);
				pathInput.setText(defaultInstallPath);
				fileChooser.setEnabled(false);
			}else if(getSlimPath().equals(defaultIncludePath)){
				include.setSelected(true);
				include.requestFocus();
				pathInput.setEditable(false);
				pathInput.setText(defaultIncludePath);
				fileChooser.setEnabled(false);
			}else{
				input.setSelected(true);
				input.requestFocus();
				pathInput.setEditable(true);
				pathInput.setText(getSlimPath());
				fileChooser.setEnabled(true);
			}

		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==installed||src==install){
				pathInput.setEditable(false);
				pathInput.setText(defaultInstallPath);
				fileChooser.setEnabled(false);
			}else if(src==include){
				pathInput.setEditable(false);
				pathInput.setText(defaultIncludePath);
				fileChooser.setEnabled(false);
			}else if(src==input){
				pathInput.setEditable(true);
				pathInput.setText(getSlimPath());
				fileChooser.setEnabled(true);
			}else if(src==fileChooser){
				JFileChooser chooser;
				String path = pathInput.getText();
				if((new File(path)).exists()){
					chooser = new JFileChooser((new File(path)).getParent());
				}else{
					chooser = new JFileChooser((new File(".")));
				}
				int res = chooser.showOpenDialog(this);
				if(res==JFileChooser.APPROVE_OPTION) {
					pathInput.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}else if(src==cancel){
				frame.dispose();
				end();

			}else if(src==ok){

				if(installed.isSelected()){
					Env.set("SLIM_EXE_PATH",defaultInstallPath);
					frame.dispose();
					end();
				}
				if(install.isSelected()){
					frame.dispose();
					final SlimInstaller slimInstaller = new SlimInstaller();
					slimInstaller.run();
					(new Thread(new Runnable() { public void run() {
						while(slimInstaller.isRunning()){
							FrontEnd.sleep(200);
						}
						if(slimInstaller.isSuccess()){
							Env.set("SLIM_EXE_PATH",defaultInstallPath);
						}
						end();
					}})).start();

				}
				if(include.isSelected()){
					Env.set("SLIM_EXE_PATH",defaultIncludePath);
					frame.dispose();
					end();
				}
				if(input.isSelected()){
					Env.set("SLIM_EXE_PATH",pathInput.getText());
					frame.dispose();
					end();
				}
			}
		}

		private String getSlimPath(){
			String path = Env.get("SLIM_EXE_PATH");
			if(path==null){
				return "";
			}else{
				return path;
			}
		}

	}

}
