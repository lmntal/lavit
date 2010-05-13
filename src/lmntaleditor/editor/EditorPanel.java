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

package lmntaleditor.editor;

import lmntaleditor.*;
import lmntaleditor.util.CommonFontUser;

import java.io.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.filechooser.FileFilter;

public class EditorPanel extends JPanel implements DocumentListener,CommonFontUser {

	public AutoStyledDocument doc;
	public JTextPane editor;
	public UndoManager undoredoManager;

	private JScrollPane scrollPane;

	public EditorButtonPanel buttonPanel;

	private EditerLineComponent eLine;
	//private Font font;

	private File editorFile;
	private boolean changed;

	public EditorPanel(){

		setLayout(new BorderLayout());

		doc = new AutoStyledDocument();

		editor = new JTextPane();
		editor.setEditorKit(new NoWrapEditorKit());
		editor.setDocument(doc);
		editor.addCaretListener(new RowColumnListener());

		eLine = new EditerLineComponent(editor.getSize().height);

		loadFont();
		FrontEnd.addFontUser(this);

		undoredoManager = new UndoManager();
		undoredoManager.setLimit(1000);

		doc.addDocumentListener(this);
		doc.addUndoableEditListener(new RedoUndoListener());

		scrollPane = new JScrollPane(editor);
		scrollPane.setRowHeaderView(eLine);
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		add(scrollPane, BorderLayout.CENTER);

		buttonPanel = new EditorButtonPanel(this);
		add(buttonPanel, BorderLayout.SOUTH);

		setMinimumSize(new Dimension(0,0));

	}

	public File getFile(){
		return editorFile;
	}

	public void firstFileOpen(){
		File first = new File("first.lmn");
		if(first.exists()){
			editorFile = first;
			openInnerEditorFile();
			FrontEnd.mainFrame.toolTab.ltlPanel.loadFile("0");
		}else{
			editorFile = new File("first.lmn");
			try{
				editorFile.createNewFile();
			}catch(Exception e){
				FrontEnd.printException(e);
			}
			editorChange(false);
			FrontEnd.println("(EDITOR) new file.");
		}
	}

	public void newFileOpen(){
		if(!closeFile()){return;}
		doc.removeDocumentListener(this);
		editor.setText("");
		doc.addDocumentListener(this);
		eLine.setHeightByLines(getLineCount());
		doc.colorChange();
		undoredoManager.discardAllEdits();

		File newfile;
	    for(int i=0;(newfile = new File("untitled"+i+".lmn")).exists();i++);

		editorFile = newfile;
		editorChange(false);
		FrontEnd.println("(EDITOR) new file.");
	}

	public void fileOpen(){
		if(!closeFile()){return;}
		File file = chooseOpenFile();
		if (file!=null) {
			editorFile = file;
			openInnerEditorFile();
			FrontEnd.mainFrame.toolTab.ltlPanel.loadFile("0");
		}
	}

	public void openInnerEditorFile(){
		try {
			String encoding = Env.get("EDITER_FILE_READ_ENCODING");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(editorFile), encoding));

			StringBuffer buf = new StringBuffer("");
			String strLine;
			if((strLine = reader.readLine()) != null){
				buf.append(strLine);
			}
			while ((strLine = reader.readLine()) != null) {
				buf.append("\r\n" + strLine);
			}
			reader.close();

			doc.removeDocumentListener(this);
			editor.setText(buf.toString());
			doc.addDocumentListener(this);
			eLine.setHeightByLines(getLineCount());
			doc.colorChange();
			undoredoManager.discardAllEdits();

			editorChange(false);

			FrontEnd.println("(EDITOR) file open. [ "+editorFile.getName()+" ]");
		}catch (Exception e) {
			FrontEnd.printException(e);
		}
	}

	public boolean fileSave(){
		try {
			editorFileSave();
			return true;
		} catch (IOException e) {
			FrontEnd.printException(e);
		}
		return false;
	}

	public boolean fileSaveAs(){
		File file = chooseWriteFile();
		if (file!=null) {
			editorFile = file;
			try {
				editorFileSave();
				return true;
			} catch (IOException e) {
				FrontEnd.printException(e);
			}
		}
		return false;
	}

	private void editorFileSave() throws IOException{
		String encoding = Env.get("EDITER_FILE_WRITE_ENCODING");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(editorFile.getAbsolutePath()), encoding));
		editor.write(writer);
		writer.close();
		editorChange(false);
		FrontEnd.println("(EDITOR) file save. [ "+editorFile.getName()+" ]");
	}

	public boolean closeFile(){
		if (isChanged()) {
			String option[] = { Lang.d[0], Lang.d[1], Lang.d[2] };
			int r = JOptionPane.showOptionDialog(FrontEnd.mainFrame,editorFile.getName()+Lang.f[2],editorFile.getName(),JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,option,option[0]);
			if (r == JOptionPane.YES_OPTION) {
				return fileSave();
			} else if (r == JOptionPane.CANCEL_OPTION || r == JOptionPane.CLOSED_OPTION) {
				return false;
			}
		}
		return true;
	}

	private File chooseOpenFile(){
		String chooser_dir = Env.get("EDITER_FILE_LAST_CHOOSER_DIR");
		if(chooser_dir==null){
			chooser_dir=new File("demo").getAbsolutePath();
		}else if(!new File(chooser_dir).exists()&&new File("demo").exists()){
			chooser_dir=new File("demo").getAbsolutePath();
		}

		JFileChooser jfc = new JFileChooser(chooser_dir);
		jfc.addChoosableFileFilter(new LmnFilter());
		int r = jfc.showOpenDialog(FrontEnd.mainFrame);
		if (r != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = jfc.getSelectedFile();
		Env.set("EDITER_FILE_LAST_CHOOSER_DIR",file.getParent());
		return file;
	}

	private File chooseWriteFile(){
		JFileChooser jfc = new JFileChooser(Env.get("EDITER_FILE_LAST_CHOOSER_DIR"));
		jfc.addChoosableFileFilter(new LmnFilter());
		File file = null;
		while(true) {
			int r = jfc.showSaveDialog(FrontEnd.mainFrame);
			if (r != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			file = jfc.getSelectedFile();

			if(!file.exists()&&!file.getAbsolutePath().endsWith(".lmn")){
				file = new File(file.getAbsolutePath()+".lmn");
			}

			if(file.exists()){
				String option[] = { Lang.d[0], Lang.d[1], Lang.d[2] };
				r = JOptionPane.showOptionDialog(FrontEnd.mainFrame,file.getName()+Lang.f[0],Lang.f[1],JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,option,option[0]);
				if (r == JOptionPane.YES_OPTION) {
					Env.set("EDITER_FILE_LAST_CHOOSER_DIR",file.getParent());
					return file;
				} else if (r == JOptionPane.CANCEL_OPTION || r == JOptionPane.CLOSED_OPTION) {
					return null;
				} else {
					/* もう一度選択 */
				}
			}else{
				Env.set("EDITER_FILE_LAST_CHOOSER_DIR",file.getParent());
				return file;
			}
		}
	}

	public void loadFont(){
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		editor.setFont(font);
		if(eLine!=null){
			eLine.setFont(font);
			eLine.setEditorMargin(editor.getMargin());
			eLine.setHeightByLines(getLineCount());
			eLine.repaint();
		}
		setTabWidth(editor,Env.getInt("EDITER_TAB_SIZE"));
	}

	public boolean isChanged(){
		return changed;
	}

	String getFileName(){
		if(editorFile==null){
			return "";
		}else{
			return editorFile.getName();
		}
	}

	private void editorChange(boolean change){
		this.changed = change;
		buttonPanel.updateFileStatus();
	}

	private void setTabWidth(JTextPane textPane, int charactersPerTab){
		FontMetrics fm = textPane.getFontMetrics( textPane.getFont() );
		int charWidth = fm.charWidth(' ');
		int tabWidth = charWidth * charactersPerTab;
		TabStop[] tabs = new TabStop[20];
		for (int j=0;j<tabs.length;j++){
			int tab = j + 1;
			tabs[j] = new TabStop( tab * tabWidth );
		}
		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		int length = textPane.getDocument().getLength();
		textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
	}

	private int getLineOfOffset(int offset) throws BadLocationException {
		if (offset < 0) {
			throw new BadLocationException("Can't translate offset to line", -1);
		} else if (offset > doc.getLength()) {
			throw new BadLocationException("Can't translate offset to line", doc.getLength()+1);
		} else {
			Element map = doc.getDefaultRootElement();
			return map.getElementIndex(offset);
		}
	}

	private int getLineStartOffset(int line) throws BadLocationException {
		int lineCount = getLineCount();
		if (line < 0) {
			throw new BadLocationException("Negative line", -1);
		} else if (line >= lineCount) {
			throw new BadLocationException("No such line", doc.getLength()+1);
		} else {
			Element map = doc.getDefaultRootElement();
			Element lineElem = map.getElement(line);
			return lineElem.getStartOffset();
		}
	}

	private int getLineCount() {
		Element map = doc.getDefaultRootElement();
		return map.getElementCount();
	}

	private void commonUpdate(DocumentEvent e) {
		if (e.getDocument() == doc) {
			editorChange(true);
			eLine.setHeightByLines(getLineCount());
			if(!e.getClass().getName().equals("javax.swing.text.AbstractDocument$UndoRedoDocumentEvent")){
				doc.colorUpdate();
			}
		}
	}

	private void checkIndent(DocumentEvent e){
		try {
			if(doc.getText(e.getOffset(),e.getLength()).endsWith("\n")){
				String text = doc.getText(0,e.getOffset());
				int pos = text.lastIndexOf("\n");
				if(pos>=0){
					text = text.substring(pos+1);
				}

				String indentString = "";
				for(int i=0;i<text.length();++i){
					if(text.charAt(i)==' '){
						indentString+=" ";
					}else if(text.charAt(i)=='\t'){
						indentString+="\t";
					}else{
						break;
					}
				}
				final String findentString = indentString;
				final int insertPos = e.getOffset()+e.getLength();

				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						try {
							doc.insertString(insertPos,findentString,null);
						} catch (BadLocationException e) {
							FrontEnd.printException(e);
						}
					}
				});

			}
		} catch (BadLocationException ex) {
			FrontEnd.printException(ex);
		}
	}

	public void changedUpdate(DocumentEvent e) {
	}

	public void insertUpdate(DocumentEvent e) {
		commonUpdate(e);
		checkIndent(e);
	}

	public void removeUpdate(DocumentEvent e) {
		commonUpdate(e);
	}

	public void editorUndo(){
		try{
			undoredoManager.undo();
		} catch (CannotUndoException e) {
			FrontEnd.printException(e);
		}
		redoundoUpdate();
	}

	public void editorRedo(){
		try{
			undoredoManager.redo();
		} catch (CannotUndoException e) {
			FrontEnd.printException(e);
		}
		redoundoUpdate();
	}

	public void redoundoUpdate(){
		FrontEnd.mainFrame.mainMenuBar.updateUndoRedo(undoredoManager.canUndo(), undoredoManager.canRedo());
	}

	/**
	 * カーソルの移動を監視するクラス
	 */
	private class RowColumnListener implements CaretListener{
		public void caretUpdate(CaretEvent e) {
			try {
				int pos = editor.getCaretPosition();
				int line = getLineOfOffset(pos);
				buttonPanel.setRowColumn(line+1, pos-getLineStartOffset(line)+1);
				eLine.updateCaretPos(line+1);
			} catch (BadLocationException ex) {
				FrontEnd.printException(ex);
			}
		}
	}

	/**
	 * 元に戻す・やり直しのためのリスナー
	 */
	private class RedoUndoListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undoredoManager.addEdit(e.getEdit());
			redoundoUpdate();
		}
	}

	private class LmnFilter extends FileFilter{

		public boolean accept(File f){
			if (f.isDirectory()){
				return true;
			}
			if (f.getName().toLowerCase().endsWith(".lmn")){
				return true;
			}else{
				return false;
			}
		}

		public String getDescription(){
			return "LMNtal "+Lang.d[5]+" (*.lmn)";
		}
	}

}