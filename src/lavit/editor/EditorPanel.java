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
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.multiedit.EditorPage;
import lavit.multiedit.TabView;
import lavit.multiedit.coloring.lexer.TokenLabel;
import lavit.util.CommonFontUser;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel implements DocumentListener, CommonFontUser
{
	public EditorButtonPanel buttonPanel;

	private TabView _tabview;
	private int _hlFlags;

	public EditorPanel()
	{
		setLayout(new BorderLayout());

		_tabview = new TabView();
		
		loadFont();
		FrontEnd.addFontUser(this);

		add(_tabview, BorderLayout.CENTER);

		buttonPanel = new EditorButtonPanel(this);
		add(buttonPanel, BorderLayout.SOUTH);

		setMinimumSize(new Dimension(0, 0));

		updateHighlight();

		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK), "mag_font");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), "mag_font");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK), "mag_font");

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), "min_font");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), "min_font");

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), "def_font");

		ActionMap am = getActionMap();
		am.put("mag_font", getScaleUpAction());
		am.put("min_font", getScaleDownAction());
		am.put("def_font", getScaleDefaultAction());
	}

	public void updateHighlight()
	{
		String colortarget = Env.get("COLOR_TARGET");
		_hlFlags = 0;
		for (String target : colortarget.split("\\s+"))
		{
			if (target.equals("comment"))
			{
				_hlFlags |= TokenLabel.COMMENT;
				_hlFlags |= TokenLabel.STRING;
			}
			else if (target.equals("symbol"))
			{
				_hlFlags |= TokenLabel.OPERATOR;
			}
			else if (target.equals("reserved"))
			{
				_hlFlags |= TokenLabel.KEYWORD;
			}
		}
		for (EditorPage page : _tabview.getPages())
		{
			page.removeAllHighlights();
			page.addHighlight(_hlFlags);
		}
	}

	public JTextPane getSelectedEditor()
	{
		return _tabview.getSelectedPage().getJTextPane();
	}

	public File getFile()
	{
		return _tabview.getSelectedPage().getFile();
	}

	/**
	 * first.lmn を開く
	 */
	public void firstFileOpen()
	{
		File first = new File("first.lmn");
		if (!first.exists())
		{
			try
			{
				first.createNewFile();
				FrontEnd.println("(EDITOR) new file.");
			}
			catch (Exception e)
			{
				FrontEnd.printException(e);
			}
		}
		openInnerEditorFile(first);
		FrontEnd.mainFrame.toolTab.ltlPanel.loadFile("0");
	}

	/**
	 * 新規作成
	 */
	public void newFileOpen()
	{
		EditorPage page = new EditorPage();
		_tabview.addPage(page, "untitled", "untitled");
		page.addHighlight(_hlFlags);
		page.setShowTabs(true);
		page.setShowEols(true);
		page.setText("");
		page.clearUndo();
		page.setFile(null);
		page.setModified(false);

		FrontEnd.println("(EDITOR) new file.");
	}

	/**
	 * ファイルを開く
	 */
	public void fileOpen()
	{
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

			StringBuilder buf = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) buf.append(Character.toChars(c));
			reader.close();

			EditorPage page = new EditorPage();
			_tabview.addPage(page, file.getName(), file.getAbsolutePath());
			page.addHighlight(_hlFlags);
			page.setShowTabs(true);
			page.setShowEols(true);
			page.setText(buf.toString());
			page.setCaretPosition(0);
			page.setModified(false);
			page.setFile(file);
			page.clearUndo();

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
		EditorPage page = _tabview.getSelectedPage();
		try
		{
			if (page.hasFile())
			{
				editorFileSave(page.getFile());
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
		EditorPage page = _tabview.getSelectedPage();
		String encoding = Env.get("EDITER_FILE_WRITE_ENCODING");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), encoding));
		page.write(writer);
		writer.close();
		page.setFile(file);
		page.setModified(false);
		_tabview.setTitle(page, file.getName(), file.getAbsolutePath());
		FrontEnd.println("(EDITOR) file save. [ " + file.getName() + " ]");
	}

	/**
	 * すべて閉じる
	 */
	public boolean closeFile()
	{
		/*
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
		*/
		
		boolean exit_success = true;
		while (0 < _tabview.getTabCount())
		{
			_tabview.setSelectedPage(0);
			boolean ret = closeSelectedPage();
			exit_success = exit_success && ret;
		}
		return exit_success;
	}
	
	/**
	 * 選択しているページを閉じる
	 */
	public boolean closeSelectedPage()
	{
		EditorPage page = _tabview.getSelectedPage();
		boolean ret = true;
		if (page.isModified())
		{
			String option[] = { Lang.d[0], Lang.d[1], Lang.d[2] };
			String title = page.hasFile() ? page.getFile().getName() : "untitled";
			String message = title + Lang.f[2];
			int r = JOptionPane.showOptionDialog(FrontEnd.mainFrame, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, option[0]);
			if (r == JOptionPane.YES_OPTION)
			{
				ret = fileSave();
			}
			else if (r == JOptionPane.CANCEL_OPTION || r == JOptionPane.CLOSED_OPTION)
			{
				ret = false;
			}
		}
		if (ret)
		{
			_tabview.closeSelectedPage();
		}
		return ret;
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
		jfc.addChoosableFileFilter(LmnFilter.getInstance());
		jfc.addChoosableFileFilter(FilterILCode.getInstance());
		jfc.setFileFilter(LmnFilter.getInstance());
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
		jfc.addChoosableFileFilter(LmnFilter.getInstance());
		jfc.addChoosableFileFilter(FilterILCode.getInstance());
		jfc.setFileFilter(LmnFilter.getInstance());
		File file = null;
		while (true)
		{
			int r = jfc.showSaveDialog(FrontEnd.mainFrame);
			if (r != JFileChooser.APPROVE_OPTION)
			{
				return null;
			}
			file = jfc.getSelectedFile();

			if (!file.exists())
			{
				if (!file.getName().endsWith(".lmn") && jfc.getFileFilter() == LmnFilter.getInstance())
				{
					file = new File(file.getAbsolutePath() + ".lmn");
				}
				else if (!file.getName().endsWith(".il") && !file.getName().endsWith(".tal") && jfc.getFileFilter() == FilterILCode.getInstance())
				{
					file = new File(file.getAbsolutePath() + ".il");
				}
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
		_tabview.setFont(font);
		setTabWidth(Env.getInt("EDITER_TAB_SIZE"));
	}

	public boolean isChanged()
	{
		if (_tabview.getTabCount() == 0)
		{
			return false;
		}
		return !_tabview.getSelectedPage().hasFile() || _tabview.getSelectedPage().isModified();
	}

	String getFileName()
	{
		if (_tabview.getTabCount() == 0)
		{
			return "";
		}

		if (!_tabview.getSelectedPage().hasFile())
		{
			return "";
		}
		else
		{
			return _tabview.getSelectedPage().getFile().getName();
		}
	}

	private void setTabWidth(int charactersPerTab)
	{
		for (EditorPage page : _tabview.getPages())
		{
			page.setTabWidth(charactersPerTab);
		}
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

	public boolean canUndo()
	{
		if (_tabview.getTabCount() > 0)
		{
			return _tabview.getSelectedPage().canUndo();
		}
		return false;
	}

	public boolean canRedo()
	{
		if (_tabview.getTabCount() > 0)
		{
			return _tabview.getSelectedPage().canRedo();
		}
		return false;
	}

	public void editorUndo()
	{
		_tabview.getSelectedPage().undo();
	}

	public void editorRedo()
	{
		_tabview.getSelectedPage().redo();
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

	private void scaleUp()
	{
		for (int i = 0; i < Env.FONT_SIZE_LIST.length; i++)
		{
			if (Env.FONT_SIZE_LIST[i].equals(Env.get("EDITER_FONT_SIZE")))
			{
				i++;
				if (i >= Env.FONT_SIZE_LIST.length)
				{
					i = Env.FONT_SIZE_LIST.length - 1;
				}
				Env.set("EDITER_FONT_SIZE",Env.FONT_SIZE_LIST[i]);
				FrontEnd.loadAllFont();
				break;
			}
		}
	}

	private void scaleDown()
	{
		for (int i = 0; i < Env.FONT_SIZE_LIST.length; i++)
		{
			if (Env.FONT_SIZE_LIST[i].equals(Env.get("EDITER_FONT_SIZE")))
			{
				i--;
				if (i < 0)
				{
					i = 0;
				}
				Env.set("EDITER_FONT_SIZE",Env.FONT_SIZE_LIST[i]);
				FrontEnd.loadAllFont();
				break;
			}
		}
	}

	private void scaleDefault()
	{
		Env.set("EDITER_FONT_SIZE","14");
		FrontEnd.loadAllFont();
	}

	private Action getScaleUpAction()
	{
		return new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				scaleUp();
			}
		};
	}

	private Action getScaleDownAction()
	{
		return new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				scaleDown();
			}
		};
	}

	private Action getScaleDefaultAction()
	{
		return new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				scaleDefault();
			}
		};
	}
}

/**
 * <p>File name filter for LMNtal source file.</p>
 * <p>This class is designed as singleton.</p>
 */
final class LmnFilter extends FileFilter
{
	private static LmnFilter instance;

	private LmnFilter() { }

	public boolean accept(File f)
	{
		return f.isDirectory() || f.getName().toLowerCase().endsWith(".lmn");
	}

	public String getDescription()
	{
		return "LMNtal " + Lang.d[5] + " (*.lmn)";
	}

	public static LmnFilter getInstance()
	{
		if (instance == null) instance = new LmnFilter();
		return instance;
	}
}

/**
 * <p>File name filter for LMNtal intermediate code file.</p>
 * <p>This class is designed as singleton.</p>
 */
final class FilterILCode extends FileFilter
{
	private static FilterILCode instance;

	private FilterILCode() { }

	public boolean accept(File f)
	{
		String name = f.getName().toLowerCase();
		return f.isDirectory() || name.endsWith(".il") || name.endsWith(".tal");
	}

	public String getDescription()
	{
		return Lang.d[9] + " (*.il, *.tal)";
	}

	public static FilterILCode getInstance()
	{
		if (instance == null)
		{
			instance = new FilterILCode();
		}
		return instance;
	}
}
