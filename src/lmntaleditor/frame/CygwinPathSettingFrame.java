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

package lmntaleditor.frame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import lmntaleditor.Env;
import lmntaleditor.FrontEnd;
import lmntaleditor.Lang;
import lmntaleditor.runner.SlimInstaller;

public class CygwinPathSettingFrame extends JFrame  {
	private SelectPanel panel;

	public CygwinPathSettingFrame(){

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Cygwin");
		setIconImage(Env.getImageOfFile("img/icon.gif"));
        setAlwaysOnTop(true);
        setResizable(false);

		panel = new SelectPanel(this);
		add(panel);

		addWindowListener(new ChildWindowListener(this));

		pack();
		setLocationRelativeTo(FrontEnd.mainFrame);
		setVisible(true);

	}

	private class SelectPanel extends JPanel implements ActionListener {
		private JFrame frame;

		private JTextField pathInput = new JTextField();
		private JButton fileChooser = new JButton(Lang.w[0]);

		private JButton ok = new JButton(Lang.d[6]);
		private JButton cancel = new JButton(Lang.d[2]);

		SelectPanel(JFrame frame){
			this.frame = frame;

			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			JLabel label = new JLabel();
			label.setText(Lang.w[1]);
			label.setPreferredSize(new Dimension(350, 40));
			label.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(label);

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

			if(getCygwinPath().equals("")){
				pathInput.setText("C:\\cygwin");
			}else{
				pathInput.setText(getCygwinPath());
			}

		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==fileChooser){
				JFileChooser chooser;
				String path = pathInput.getText();
				if((new File(path)).exists()){
					chooser = new JFileChooser((new File(path)).getParent());
				}else{
					chooser = new JFileChooser((new File(".")));
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int res = chooser.showOpenDialog(this);
				if(res==JFileChooser.APPROVE_OPTION) {
					pathInput.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}else if(src==cancel){
				frame.dispose();
			}else if(src==ok){
				Env.set("WINDOWS_CYGWIN_DIR",pathInput.getText());
				frame.dispose();
			}
		}

		private String getCygwinPath(){
			String path = Env.get("WINDOWS_CYGWIN_DIR");
			if(path==null){
				return "";
			}else{
				return path;
			}
		}

	}
}
