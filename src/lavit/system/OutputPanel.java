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

package lavit.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.AutoStyledDocument;
import lavit.runner.RunnerOutputGetter;
import lavit.util.CommonFontUser;

public class OutputPanel extends JPanel implements MouseInputListener,RunnerOutputGetter,ActionListener,KeyListener,CommonFontUser {

	private DefaultStyledDocument doc;
	private JScrollPane jsp;
	private JTextPane log;

	private JPanel findPanel = new JPanel();
	private JLabel findLabel = new JLabel();
	private JTextField findField = new JTextField();
    private JButton findButton = new JButton("Find");

    private int line = 0;
    private int maxLine = 0;

    private int findLength = 0;
    private int findCursorPos = 0;
    private ArrayList<Integer> findResults = new ArrayList<Integer>();

	private RightMenu rightMenu = new RightMenu();

	public OutputPanel(){

		doc = new DefaultStyledDocument();
		log = new JTextPane(doc);
		log.setEditable(false);
		log.addMouseListener(this);
		jsp = new JScrollPane(log);
		jsp.getVerticalScrollBar().setUnitIncrement(15);

		setLayout(new BorderLayout());
		add(jsp,BorderLayout.CENTER);

		findButton.addActionListener(this);
		findButton.addKeyListener(this);
		findField.addActionListener(this);
		findField.addKeyListener(this);
		findPanel.setLayout(new BorderLayout());
		findPanel.add(findLabel,BorderLayout.WEST);
		findPanel.add(findField, BorderLayout.CENTER);
		findPanel.add(findButton, BorderLayout.EAST);
		add(findPanel,BorderLayout.SOUTH);

		line = 0;
		maxLine = Env.getInt("SYSTEM_OUTPUT_MAXLINE");

		loadFont();
		FrontEnd.addFontUser(this);

		findPanel.setVisible(Env.is("SYSTEM_OUTPUT_FIND"));

	}

	public void loadFont(){
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		log.setFont(font);
		findField.setFont(font);
		revalidate();
	}

	public void println(String str){
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		attribute.addAttribute(StyleConstants.Foreground, Color.BLACK);
		println(str,attribute);
	}

	public void errPrintln(String str){
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		attribute.addAttribute(StyleConstants.Foreground, Color.RED);
		println(str,attribute);
	}

	public void printTitle(String str){
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		attribute.addAttribute(StyleConstants.Foreground, Color.WHITE);
		attribute.addAttribute(StyleConstants.Background, Color.BLUE);
		println(str,attribute);
	}

	//スレッドセーフ
	public void println(final String str,final SimpleAttributeSet attribute){
		line++;
		if(line<=maxLine){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
				try {
					doc.insertString(doc.getLength(),str+"\n", attribute);
					log.setCaretPosition(doc.getLength());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}});
			if(line==maxLine){
				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run() {
					try {
						attribute.addAttribute(StyleConstants.Foreground, Color.RED);
						doc.insertString(doc.getLength(),"Output is full.\n", attribute);
						log.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}});
			}
		}else{
			System.out.println(str);
		}
	}

	private void toggleFindVisible(){
		findPanel.setVisible(!findPanel.isVisible());
		Env.set("SYSTEM_OUTPUT_FIND",findPanel.isVisible());
		resetFindResult();
	}

	private void resetFindResult(){
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		attribute.addAttribute(StyleConstants.Background, Color.white);

		if(findLength>0){
			for(int pos : findResults){
				doc.setCharacterAttributes(pos,findLength,attribute,true);
			}
		}

		findLabel.setText("");

		findLength=0;
		findCursorPos=0;
		findResults.clear();
	}

	private void findAll(){
		try {
			SimpleAttributeSet attribute = new SimpleAttributeSet();
			attribute.addAttribute(StyleConstants.Background, Color.white);
			String text = doc.getText(0,doc.getLength()).replaceAll("\r\n","\n");

			findLength = findField.getText().length();
			if(findLength>0){
				attribute.addAttribute(StyleConstants.Background, Color.yellow);
				int pos = 0;
				while((pos=text.indexOf(findField.getText(),pos))>=0){
					findResults.add(pos);
					doc.setCharacterAttributes(pos,findLength,attribute,true);
					pos += findLength;
				}
				updateSelect();
			}
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}

	private void updateSelect(){
		if(findResults.size()==0){
			findLabel.setText(" 0 / 0 ");
			return;
		}

		int backPos = ((findCursorPos-1)+findResults.size())%findResults.size();
		int nextPos = (findCursorPos+1)%findResults.size();
		findCursorPos = (findCursorPos+findResults.size())%findResults.size();

		SimpleAttributeSet attribute = new SimpleAttributeSet();
		attribute.addAttribute(StyleConstants.Background, Color.yellow);
		doc.setCharacterAttributes(findResults.get(backPos),findLength,attribute,true);
		doc.setCharacterAttributes(findResults.get(nextPos),findLength,attribute,true);

		attribute.addAttribute(StyleConstants.Background, new Color(200,200,0));
		doc.setCharacterAttributes(findResults.get(findCursorPos),findLength,attribute,true);
		log.setCaretPosition(findResults.get(findCursorPos));

		findLabel.setText(" "+(findCursorPos+1)+" / "+findResults.size()+" ");
	}

	public void mousePressed(MouseEvent e) {
		if(SwingUtilities.isRightMouseButton(e)){
			rightMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	private class RightMenu extends JPopupMenu implements ActionListener{
		private JMenuItem clear = new JMenuItem("Clear");
		private JCheckBoxMenuItem find = new JCheckBoxMenuItem("Find");

		RightMenu(){
			clear.addActionListener(this);
			add(clear);

			find.addActionListener(this);
			find.setSelected(Env.is("SYSTEM_OUTPUT_FIND"));
			add(find);
		}

		public void actionPerformed(ActionEvent e) {
			JMenuItem src = (JMenuItem)e.getSource();
			if(src==clear){
				log.setText("");
				line = 0;
				findLength = 0;
				findCursorPos = 0;
				findResults.clear();
			}else if(src==find){
				toggleFindVisible();
			}
		}


	}

	public void outputStart(String command,String option, File target) {
		printTitle("> "+command+" "+option+" "+target.getName());
	}

	public void outputLine(String str) {
		println(str);
	}

	public void outputEnd() {
		println("");
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==findButton||src==findField){
			resetFindResult();
			findAll();
		}
	}

	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()){
		case KeyEvent.VK_DOWN:
			++findCursorPos;
			updateSelect();
			break;
		case KeyEvent.VK_UP:
			--findCursorPos;
			updateSelect();
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
