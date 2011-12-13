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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.multiedit.EditorPage;
import lavit.multiedit.coloring.lexer.TokenLabel;
import lavit.util.CommonFontUser;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel implements DocumentListener, KeyListener, CommonFontUser
{
	public EditorButtonPanel buttonPanel;

	private EditorPage editor;

	public EditorPanel()
	{
		setLayout(new BorderLayout());

		editor = new EditorPage();
		editor.addKeyListener(this);
		editor.setShowTabs(true);
		editor.setShowEols(true);

		loadFont();
		FrontEnd.addFontUser(this);

		add(editor, BorderLayout.CENTER);

		buttonPanel = new EditorButtonPanel(this);
		add(buttonPanel, BorderLayout.SOUTH);

		setMinimumSize(new Dimension(0, 0));
		
		updateHighlight();
	}
	
	public void updateHighlight()
	{
		editor.removeAllHighlights();
		
		String colortarget = Env.get("COLOR_TARGET");
		for (String target : colortarget.split("\\s+"))
		{
			if (target.equals("comment"))
			{
				editor.addHighlight(TokenLabel.COMMENT | TokenLabel.STRING);
			}
			else if (target.equals("symbol"))
			{
				editor.addHighlight(TokenLabel.OPERATOR);
			}
			else if (target.equals("reserved"))
			{
				editor.addHighlight(TokenLabel.KEYWORD);
			}
		}
		editor.updateHighlight();
	}

	public JTextPane getSelectedEditor()
	{
		return editor.getJTextPane();
	}

	public File getFile()
	{
		return editor.getFile();
	}

	/**
	 * first.lmn を開く
	 */
	public void firstFileOpen()
	{
		File first = new File("first.lmn");
		if (first.exists())
		{
			openInnerEditorFile(first);
			FrontEnd.mainFrame.toolTab.ltlPanel.loadFile("0");
		}
		else
		{
			try
			{
				first.createNewFile();
			}
			catch(Exception e)
			{
				FrontEnd.printException(e);
			}
			FrontEnd.println("(EDITOR) new file.");
		}
	}

	/**
	 * 新規作成
	 */
	public void newFileOpen()
	{
		if (!closeFile())
		{
			return;
		}
		
		editor.setText("");
		editor.clearUndo();
		editor.setFile(null);
		editor.setModified(false);

		FrontEnd.println("(EDITOR) new file.");
	}

	/**
	 * ファイルを開く
	 */
	public void fileOpen()
	{
		if (!closeFile())
		{
			return;
		}
		
		File file = chooseOpenFile();
		if (file != null)
		{
			openInnerEditorFile(file);
			FrontEnd.mainFrame.toolTab.ltlPanel.loadFile("0");
		}
	}

	private void openInnerEditorFile(File file)
	{
		try
		{
			String encoding = Env.get("EDITER_FILE_READ_ENCODING");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

			StringBuffer buf = new StringBuffer();
			String line;
			if((line = reader.readLine()) != null)
			{
				buf.append(line);
			}
			while ((line = reader.readLine()) != null)
			{
				buf.append("\r\n");
				buf.append(line);
			}
			reader.close();

			editor.setText(buf.toString());
			editor.clearUndo();
			editor.setCaretPosition(0);
			editor.setModified(false);
			editor.setFile(file);

			FrontEnd.println("(EDITOR) file open. [ " + file.getName() + " ]");
		}
		catch (Exception e)
		{
			FrontEnd.printException(e);
		}
	}

	/**
	 * 上書き保存
	 */
	public boolean fileSave()
	{
		try
		{
			if (editor.hasFile())
			{
				editorFileSave(editor.getFile());
				return true;
			}
			else
			{
				return fileSaveAs();
			}
		}
		catch (IOException e)
		{
			FrontEnd.printException(e);
		}
		return false;
	}

	/**
	 * 名前を付けて保存
	 */
	public boolean fileSaveAs()
	{
		File file = chooseWriteFile();
		if (file != null)
		{
			try
			{
				editorFileSave(file);
				return true;
			}
			catch (IOException e)
			{
				FrontEnd.printException(e);
			}
		}
		return false;
	}

	private void editorFileSave(File file) throws IOException
	{
		String encoding = Env.get("EDITER_FILE_WRITE_ENCODING");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), encoding));
		editor.write(writer);
		writer.close();
		editor.setFile(file);
		editor.setModified(false);
		FrontEnd.println("(EDITOR) file save. [ " + file.getName() + " ]");
	}

	public boolean closeFile()
	{
		if (editor.isModified())
		{
			String option[] = { Lang.d[0], Lang.d[1], Lang.d[2] };
			String title = editor.hasFile() ? editor.getFile().getName() : "untitled";
			String message = title + Lang.f[2];
			int r = JOptionPane.showOptionDialog(FrontEnd.mainFrame, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, option[0]);
			if (r == JOptionPane.YES_OPTION)
			{
				return fileSave();
			}
			else if (r == JOptionPane.CANCEL_OPTION || r == JOptionPane.CLOSED_OPTION)
			{
				return false;
			}
		}
		return true;
	}

	private File chooseOpenFile()
	{
		String chooser_dir = Env.get("EDITER_FILE_LAST_CHOOSER_DIR");
		if (chooser_dir == null)
		{
			chooser_dir=new File("demo").getAbsolutePath();
		}
		else if (!new File(chooser_dir).exists() && new File("demo").exists())
		{
			chooser_dir=new File("demo").getAbsolutePath();
		}

		JFileChooser jfc = new JFileChooser(chooser_dir);
		jfc.addChoosableFileFilter(new LmnFilter());
		int r = jfc.showOpenDialog(FrontEnd.mainFrame);
		if (r != JFileChooser.APPROVE_OPTION)
		{
			return null;
		}
		File file = jfc.getSelectedFile();
		Env.set("EDITER_FILE_LAST_CHOOSER_DIR",file.getParent());
		return file;
	}

	private File chooseWriteFile()
	{
		JFileChooser jfc = new JFileChooser(Env.get("EDITER_FILE_LAST_CHOOSER_DIR"));
		jfc.addChoosableFileFilter(new LmnFilter());
		File file = null;
		while (true)
		{
			int r = jfc.showSaveDialog(FrontEnd.mainFrame);
			if (r != JFileChooser.APPROVE_OPTION)
			{
				return null;
			}
			file = jfc.getSelectedFile();

			if (!file.exists() && !file.getAbsolutePath().endsWith(".lmn"))
			{
				file = new File(file.getAbsolutePath() + ".lmn");
			}

			if (file.exists())
			{
				String option[] = { Lang.d[0], Lang.d[1], Lang.d[2] };
				r = JOptionPane.showOptionDialog(FrontEnd.mainFrame,file.getName()+Lang.f[0],Lang.f[1],JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,option,option[0]);
				if (r == JOptionPane.YES_OPTION)
				{
					Env.set("EDITER_FILE_LAST_CHOOSER_DIR", file.getParent());
					return file;
				}
				else if (r == JOptionPane.CANCEL_OPTION || r == JOptionPane.CLOSED_OPTION)
				{
					return null;
				}
				else
				{
					/* もう一度選択 */
				}
			}
			else
			{
				Env.set("EDITER_FILE_LAST_CHOOSER_DIR", file.getParent());
				return file;
			}
		}
	}

	public void loadFont()
	{
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		editor.setFont(font);
		setTabWidth(Env.getInt("EDITER_TAB_SIZE"));
	}

	public boolean isChanged()
	{
		return editor.isModified();
	}

	String getFileName()
	{
		if (!editor.hasFile())
		{
			return "";
		}
		else
		{
			return editor.getFile().getName();
		}
	}

	private void setTabWidth(int charactersPerTab)
	{
		editor.setTabWidth(charactersPerTab);
	}

	/*
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
	*/

	/*
	private void commonUpdate(DocumentEvent e) {
		if (e.getDocument() == doc) {
			editorChange(true);
			if(!e.getClass().getName().equals("javax.swing.text.AbstractDocument$UndoRedoDocumentEvent")){
				doc.colorUpdate();
			}
		}
	}
	*/

	/*
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
	*/

	public void changedUpdate(DocumentEvent e) {
	}

	public void insertUpdate(DocumentEvent e) {
		//commonUpdate(e);
		//checkIndent(e);
	}

	public void removeUpdate(DocumentEvent e) {
		//commonUpdate(e);
	}

	public void editorUndo()
	{
		//editor.undo();
	}

	public void editorRedo()
	{
		//editor.redo();
	}

	public void redoundoUpdate(){
		//FrontEnd.mainFrame.mainMenuBar.updateUndoRedo(undoredoManager.canUndo(), undoredoManager.canRedo());
	}

	/**
	 * カーソルの移動を監視するクラス
	 */
	private class RowColumnListener implements CaretListener{
		public void caretUpdate(CaretEvent e) {
			/*
			try {
				int pos = editor.getCaretPosition();
				int line = getLineOfOffset(pos);
				buttonPanel.setRowColumn(line+1, pos-getLineStartOffset(line)+1);
			} catch (BadLocationException ex) {
				FrontEnd.printException(ex);
			}
			*/
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

	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.isControlDown()){
			switch(e.getKeyCode()){
			case KeyEvent.VK_SEMICOLON:
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_PLUS:
				for(int i=0;i<Env.FONT_SIZE_LIST.length;++i){
					if(Env.FONT_SIZE_LIST[i].equals(Env.get("EDITER_FONT_SIZE"))){
						i++;
						if(i>=Env.FONT_SIZE_LIST.length){ i = Env.FONT_SIZE_LIST.length-1; }
						Env.set("EDITER_FONT_SIZE",Env.FONT_SIZE_LIST[i]);
						FrontEnd.loadAllFont();
						break;
					}
				}
				break;
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_SUBTRACT:
				System.out.println("-");
				for(int i=0;i<Env.FONT_SIZE_LIST.length;++i){
					if(Env.FONT_SIZE_LIST[i].equals(Env.get("EDITER_FONT_SIZE"))){
						i--;
						if(i<0){ i=0; }
						Env.set("EDITER_FONT_SIZE",Env.FONT_SIZE_LIST[i]);
						FrontEnd.loadAllFont();
						break;
					}
				}
				break;
			case KeyEvent.VK_0:
				Env.set("EDITER_FONT_SIZE","14");
				FrontEnd.loadAllFont();
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}