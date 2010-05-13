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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.util.FixFlowLayout;

public class GeneralSettingFrame extends JFrame {
	JPanel panel;
	//SlimLibSettingPanel slimLibSettingPanel;
	EditorColorPanel editorColorPanel;
	FontSettingPanel fontSettingPanel;
	EncodingSettingPanel encodingSettingPanel;
	ViewSettingPanel viewSettingPanel;

	GeneralSettingFrame(){
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("LaViT Reboot");
		setIconImage(Env.getImageOfFile("img/icon.gif"));
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(500, 400));
        //setResizable(false);

        setLayout(new GridLayout(4,1));

        //slimLibSettingPanel = new SlimLibSettingPanel();
        //add(slimLibSettingPanel);

        editorColorPanel = new EditorColorPanel();
        add(editorColorPanel);

        fontSettingPanel = new FontSettingPanel();
        add(fontSettingPanel);

        encodingSettingPanel = new EncodingSettingPanel();
        add(encodingSettingPanel);

        viewSettingPanel = new ViewSettingPanel();
        add(viewSettingPanel);

		addWindowListener(new ChildWindowListener(this));

		pack();
		setLocationRelativeTo(FrontEnd.mainFrame);
		setVisible(true);

	}

	private class EditorColorPanel extends JPanel implements ActionListener{
		String majorOption[] = {"comment","symbol","reserved"};
		JCheckBox optionCheckBox[] = new JCheckBox[majorOption.length];

		EditorColorPanel(){

			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("Color"));

			for(int i=0;i<majorOption.length;++i){
				optionCheckBox[i] = new JCheckBox(majorOption[i]);
				add(optionCheckBox[i]);
			}
			settingInit();

			for(int i=0;i<majorOption.length;++i){
				optionCheckBox[i].addActionListener(this);
			}
		}

		void settingInit(){
			for(int i=0;i<majorOption.length;++i){
				optionCheckBox[i].setSelected(false);
			}
			String[] options = Env.get("COLOR_TARGET").split(" ");
			for(String o : options){
				for(int i=0;i<majorOption.length;++i){
					if(majorOption[i].equals(o)){
						optionCheckBox[i].setSelected(true);
					}
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			String newOptions = "";
			for(int i=0;i<majorOption.length;++i){
				if(optionCheckBox[i].isSelected()){
					if(newOptions.length()==0){
						newOptions += optionCheckBox[i].getText();
					}else{
						newOptions += " " + optionCheckBox[i].getText();
					}
				}
			}
			Env.set("COLOR_TARGET",newOptions);
			FrontEnd.mainFrame.editorPanel.doc.colorUpdate();
		}
	}

	public class FontSettingPanel extends JPanel implements ActionListener{

		String fontFamilyList[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		String fontSizeList[] = {"8","9","10","11","12","14","16","18","20","24","28","32","36","40","44","48","54","60","66","72","80","88","96","106"};
		String tabSizeList[] = {"1","2","3","4","5","6","7","8","9","10"};


		JComboBox fontFamilyComboBox;
		JComboBox fontSizeComboBox;
		JComboBox tabSizeComboBox;

		FontSettingPanel(){

			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("Font & Language"));

			add(new JLabel("FontFamily"));
			fontFamilyComboBox = new JComboBox(fontFamilyList);
			add(fontFamilyComboBox);

			add(new JLabel("FontSize"));
			fontSizeComboBox = new JComboBox(fontSizeList);
			add(fontSizeComboBox);

			add(new JLabel("TabSize"));
			tabSizeComboBox = new JComboBox(tabSizeList);
			add(tabSizeComboBox);

			settingInit();

			fontFamilyComboBox.addActionListener(this);
			fontSizeComboBox.addActionListener(this);
			tabSizeComboBox.addActionListener(this);

		}

		void settingInit(){
			for(String str : fontFamilyList){
				if(str.equals(Env.get("EDITER_FONT_FAMILY"))){
					fontFamilyComboBox.setSelectedItem(str);
					break;
				}
			}
			for(String str : fontSizeList){
				if(str.equals(Env.get("EDITER_FONT_SIZE"))){
					fontSizeComboBox.setSelectedItem(str);
					break;
				}
			}
			for(String str : tabSizeList){
				if(str.equals(Env.get("EDITER_TAB_SIZE"))){
					tabSizeComboBox.setSelectedItem(str);
					break;
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			Env.set("EDITER_FONT_FAMILY",(String)fontFamilyComboBox.getSelectedItem());
			Env.set("EDITER_FONT_SIZE",(String)fontSizeComboBox.getSelectedItem());
			Env.set("EDITER_TAB_SIZE",(String)tabSizeComboBox.getSelectedItem());
			/*
			FrontEnd.mainFrame.editorPanel.loadFont();
			FrontEnd.mainFrame.toolTab.systemPanel.logPanel.loadFont();
			FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.loadFont();
			FrontEnd.mainFrame.toolTab.statePanel.stateGraphPanel.loadFont();
			*/
			FrontEnd.loadAllFont();

		}

	}


	public class EncodingSettingPanel extends JPanel implements ActionListener{

		String encodingList[] = {"SJIS", "EUC_JP","ISO2022JP", "UTF8"};

		JComboBox readComboBox;
		JComboBox writeComboBox;

		EncodingSettingPanel(){

			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("File Encoding"));

			add(new JLabel("READ"));
			readComboBox = new JComboBox(encodingList);
			add(readComboBox);

			add(new JLabel("WRITE"));
			writeComboBox = new JComboBox(encodingList);
			add(writeComboBox);

			settingInit();

			readComboBox.addActionListener(this);
			writeComboBox.addActionListener(this);

		}

		void settingInit(){
			for(String str : encodingList){
				if(str.equals(Env.get("EDITER_FILE_READ_ENCODING"))){
					readComboBox.setSelectedItem(str);
					break;
				}
			}
			for(String str : encodingList){
				if(str.equals(Env.get("EDITER_FILE_WRITE_ENCODING"))){
					writeComboBox.setSelectedItem(str);
					break;
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			Env.set("EDITER_FILE_READ_ENCODING",(String)readComboBox.getSelectedItem());
			Env.set("EDITER_FILE_WRITE_ENCODING",(String)writeComboBox.getSelectedItem());
		}

	}

	public class SlimLibSettingPanel extends JPanel implements ActionListener{
		JCheckBox useCheckBox;

		SlimLibSettingPanel(){

			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("SLIM LIBRARY"));

			useCheckBox = new JCheckBox("Use slim library");
			useCheckBox.addActionListener(this);
			useCheckBox.setSelected(Env.is("SLIM_USE_LIBRARY"));
			add(useCheckBox);

		}

		public void actionPerformed(ActionEvent e) {
			Env.set("SLIM_USE_LIBRARY",useCheckBox.isSelected());
		}
	}

	public class ViewSettingPanel extends JPanel implements ActionListener{

		String langList[] = {"jp","en"};
		String lookAndFeelList[] = {"Metal","Motif","Windows","WindowsClassic","SystemDefault"};

		JComboBox lookAndFeelComboBox;
		JComboBox langComboBox;

		ViewSettingPanel(){

			setLayout(new FixFlowLayout());
			setBorder(new TitledBorder("View"));

			add(new JLabel("Lang"));
			langComboBox = new JComboBox(langList);
			add(langComboBox);

			add(new JLabel("LookAndFeel"));
			lookAndFeelComboBox = new JComboBox(lookAndFeelList);
			add(lookAndFeelComboBox);

			settingInit();

			lookAndFeelComboBox.addActionListener(this);
			langComboBox.addActionListener(this);

		}

		void settingInit(){
			for(String str : lookAndFeelList){
				if(str.equals(Env.get("LookAndFeel"))){
					lookAndFeelComboBox.setSelectedItem(str);
					break;
				}
			}
			for(String str : langList){
				if(str.equals(Env.get("LANG"))){
					langComboBox.setSelectedItem(str);
					break;
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==langComboBox){
				Env.set("LANG",(String)langComboBox.getSelectedItem());
				JOptionPane.showMessageDialog(FrontEnd.mainFrame,Lang.f[4],"Change Language",JOptionPane.PLAIN_MESSAGE);
			}else{
				Env.set("LookAndFeel",(String)lookAndFeelComboBox.getSelectedItem());
				FrontEnd.updateLookAndFeel();
				SwingUtilities.updateComponentTreeUI(FrontEnd.mainFrame);
			}
		}

	}


}
