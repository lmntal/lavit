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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import javax.swing.filechooser.FileFilter;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.event.TabChangeListener;
import lavit.multiedit.EditorPage;
import lavit.multiedit.TabView;
import lavit.multiedit.coloring.lexer.TokenLabel;
import lavit.system.FileHistory;
import lavit.util.CommonFontUser;
import extgui.filedrop.event.FileDropListener;
import extgui.fileview.FileViewPane;
import extgui.fileview.event.FileSelectedListener;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel implements CommonFontUser
{
	public EditorButtonPanel buttonPanel;

	private FileViewPane fileView;
	private TabView tabView;
	private int hlFlags;
	private FileDropListener fileDropListener = new FileDropAction();

	public EditorPanel()
	{
		setLayout(new BorderLayout());

		tabView = new TabView();

		loadFont();
		FrontEnd.addFontUser(this);

		fileView = new FileViewPane(tabView);
		fileView.addFileSelectedListener(new FileSelectedListener()
		{
			public void fileSelected(File selectedFile)
			{
				openFile(selectedFile);
			}
		});
		add(fileView, BorderLayout.CENTER);

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
		hlFlags = 0;
		for (String target : colortarget.split("\\s+"))
		{
			if (target.equals("comment"))
			{
				hlFlags |= TokenLabel.COMMENT;
				hlFlags |= TokenLabel.STRING;
			}
			else if (target.equals("symbol"))
			{
				hlFlags |= TokenLabel.OPERATOR;
			}
			else if (target.equals("reserved"))
			{
				hlFlags |= TokenLabel.KEYWORD;
			}
		}
		for (EditorPage page : tabView.getPages())
		{
			page.removeAllHighlights();
			page.addHighlight(hlFlags);
			page.setShowTabs(Env.is("SHOW_TABS"));
			page.setShowEols(Env.is("SHOW_LINE_DELIMITERS"));
		}
		repaint();
	}

	public JTextPane getSelectedEditor()
	{
		return tabView.getSelectedPage().getJTextPane();
	}

	public File getFile()
	{
		return tabView.getSelectedPage().getFile();
	}

	public List<File> getFiles()
	{
		List<File> files = new ArrayList<File>();
		EditorPage[] pages = tabView.getPages();
		for (EditorPage page : pages)
		{
			if (page.hasFile())
			{
				files.add(page.getFile());
			}
		}
		return files;
	}

	/**
	 * Returns a number of opend tabs.
	 */
	public int getTabCount()
	{
		return tabView.getTabCount();
	}

	public void addTabChangeListener(TabChangeListener l)
	{
		tabView.addTabChangeListener(l);
	}

	public void setFileViewVisible(boolean b)
	{
		fileView.setFileViewVisible(b);
	}

	public boolean isFileViewVisible()
	{
		return fileView.isFileViewVisible();
	}

	public void setFileViewDividerLocation(int location)
	{
		fileView.setDividerLocation(location);
	}

	public int getFileViewDividerLocation()
	{
		return fileView.getDividerLocation();
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
		openFile(first);
		//FrontEnd.mainFrame.toolTab.ltlPanel.loadFile("0");
	}

	/**
	 * 前回開いていたファイルを開く
	 */
	public void openInitialFiles()
	{
		List<File> files = Env.loadLastFiles();
		if (!files.isEmpty())
		{
			openFiles(files);
			tabView.setSelectedIndex(0);
		}
		else
		{
			firstFileOpen();
		}
	}

	/**
	 * 新規作成
	 */
	public void newFileOpen()
	{
		EditorPage page = createEmptyPage();
		tabView.addPage(page, "untitled", "untitled");
		FrontEnd.println("(EDITOR) new file.");
	}

	/**
	 * 開くファイルを選択する。
	 */
	 //TODO: このパネルの仕事ではない。
	public void fileOpen()
	{
		File file = chooseOpenFile();
		if (file != null)
		{
			openFile(file);
		}
	}

	/**
	 * ファイルを開く。
	 */
	public void openFile(File file)
	{
		try
		{
			String encoding = Env.get("EDITER_FILE_READ_ENCODING");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

			StringBuilder buf = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) buf.append(Character.toChars(c));
			reader.close();

			EditorPage page = createPage(buf.toString());
			page.setFile(file);

			// タブ変更イベントとの関係上、タブの設定終了後に追加する
			tabView.addPage(page, file.getName(), file.getAbsolutePath());

			FrontEnd.println("(EDITOR) file open. [ " + file.getName() + " ]");
		}
		catch (Exception e)
		{
			FrontEnd.printException(e);
		}

		FileHistory.get().add(file);
	}

	/**
	 * 上書き保存
	 */
	public boolean fileSave()
	{
		EditorPage page = tabView.getSelectedPage();
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

	private void openFiles(Collection<File> files)
	{
		for (File file : files)
		{
			if (file.exists() && file.isFile())
			{
				openFile(file);
			}
		}
	}

	private EditorPage createPage(String text)
	{
		EditorPage page = createEmptyPage();
		page.setText(text);
		page.setCaretPosition(0);
		page.setModified(false);
		page.clearUndo();
		return page;
	}

	private EditorPage createEmptyPage()
	{
		EditorPage page = new EditorPage();
		page.addFileDropListener(fileDropListener);
		page.addHighlight(hlFlags);
		page.setShowTabs(Env.is("SHOW_TABS"));
		page.setShowEols(Env.is("SHOW_LINE_DELIMITERS"));
		return page;
	}

	private void editorFileSave(File file) throws IOException
	{
		EditorPage page = tabView.getSelectedPage();
		String encoding = Env.get("EDITER_FILE_WRITE_ENCODING");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), encoding));
		page.write(writer);
		writer.close();
		page.setFile(file);
		page.setModified(false);
		tabView.setTitle(page, file.getName(), file.getAbsolutePath());
		FrontEnd.println("(EDITOR) file save. [ " + file.getName() + " ]");
	}

	/**
	 * 変更のあるすべてのページについて保存について聞く。
	 * キャンセルが選択された場合はページは1つも閉じられない。
	 * @return すべての確認で「はい」か「いいえ」が選択された場合は {@code true}、ある確認について「キャンセル」が選択された場合は {@code false}
	 */
	public boolean askSaveAllChangedFiles()
	{
		boolean approved = true;
		for (int i = 0; i < tabView.getTabCount(); i++)
		{
			if (isChanged(i))
			{
				tabView.setSelectedPage(i);
				int ret = askSaveChangedFile();
				if (ret == JOptionPane.CANCEL_OPTION)
				{
					approved = false;
					break;
				}
				else if (ret == JOptionPane.YES_OPTION)
				{
					fileSave();
				}
			}
		}
		return approved;
	}

	/**
	 * すべて閉じる
	 */
	public void closeAllPages()
	{
		if (askSaveAllChangedFiles())
		{
			while (0 < tabView.getTabCount())
			{
				tabView.setSelectedPage(0);
				closeSelectedPageDiscardChanges();
			}
		}
	}

	/**
	 * 選択しているページを閉じる
	 */
	public boolean closeSelectedPage()
	{
		EditorPage page = tabView.getSelectedPage();
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
			closeSelectedPageDiscardChanges();
		}
		return ret;
	}

	/**
	 * 選択しているページを閉じる。
	 * このメソッドを直接呼んだ場合、閉じられるページの変更内容は破棄される。
	 */
	public void closeSelectedPageDiscardChanges()
	{
		tabView.closeSelectedPage();
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
		jfc.addChoosableFileFilter(LMNtalFileFilter.getInstance());
		jfc.addChoosableFileFilter(ILCodeFileFilter.getInstance());
		jfc.setFileFilter(LMNtalFileFilter.getInstance());
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
		jfc.addChoosableFileFilter(LMNtalFileFilter.getInstance());
		jfc.addChoosableFileFilter(ILCodeFileFilter.getInstance());
		jfc.setFileFilter(LMNtalFileFilter.getInstance());
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
				if (!file.getName().endsWith(".lmn") && jfc.getFileFilter() == LMNtalFileFilter.getInstance())
				{
					file = new File(file.getAbsolutePath() + ".lmn");
				}
				else if (!file.getName().endsWith(".il") && !file.getName().endsWith(".tal") && jfc.getFileFilter() == ILCodeFileFilter.getInstance())
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

	/**
	 * 変更を保存するか聞く。
	 */
	private int askSaveChangedFile()
	{
		EditorPage page = tabView.getSelectedPage();
		String option[] = { Lang.d[0], Lang.d[1], Lang.d[2] };
		String title = page.hasFile() ? page.getFile().getName() : "untitled";
		String message = title + Lang.f[2];
		return JOptionPane.showOptionDialog(FrontEnd.mainFrame,
			message, title,
			JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
			null, option, option[0]);
	}

	public void loadFont()
	{
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		tabView.setFont(font);
		setTabWidth(Env.getInt("EDITER_TAB_SIZE"));
	}

	public boolean isChanged()
	{
		return isChanged(tabView.getSelectedIndex());
	}

	public boolean isChanged(int pageIndex)
	{
		EditorPage[] pages = tabView.getPages();
		if (pageIndex < 0 || pages.length < pageIndex)
		{
			return false;
		}
		EditorPage page = pages[pageIndex];
		return !page.hasFile() || page.isModified();
	}

	String getFileName()
	{
		if (tabView.getTabCount() == 0)
		{
			return "";
		}

		if (!tabView.getSelectedPage().hasFile())
		{
			return "";
		}
		else
		{
			return tabView.getSelectedPage().getFile().getName();
		}
	}

	private void setTabWidth(int charactersPerTab)
	{
		for (EditorPage page : tabView.getPages())
		{
			page.setTabWidth(charactersPerTab);
		}
	}

	public boolean canUndo()
	{
		if (tabView.getTabCount() > 0)
		{
			return tabView.getSelectedPage().canUndo();
		}
		return false;
	}

	public boolean canRedo()
	{
		if (tabView.getTabCount() > 0)
		{
			return tabView.getSelectedPage().canRedo();
		}
		return false;
	}

	public void editorUndo()
	{
		tabView.getSelectedPage().undo();
	}

	public void editorRedo()
	{
		tabView.getSelectedPage().redo();
	}

	public void redoundoUpdate()
	{
		//FrontEnd.mainFrame.mainMenuBar.updateUndoRedo(undoredoManager.canUndo(), undoredoManager.canRedo());
	}

	/**
	 * カーソルの移動を監視するクラス
	 */
	private class RowColumnListener implements CaretListener
	{
		public void caretUpdate(CaretEvent e)
		{
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

	private class FileDropAction implements FileDropListener
	{
		public void filesDropped(List<File> files)
		{
			openFiles(files);
		}
	}

	/**
	 * <p>File name filter for LMNtal source file.</p>
	 * <p>This class is designed as singleton.</p>
	 */
	private static final class LMNtalFileFilter extends FileFilter
	{
		private static LMNtalFileFilter instance;

		private LMNtalFileFilter() { }

		public boolean accept(File f)
		{
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".lmn");
		}

		public String getDescription()
		{
			return "LMNtal " + Lang.d[5] + " (*.lmn)";
		}

		public static LMNtalFileFilter getInstance()
		{
			if (instance == null) instance = new LMNtalFileFilter();
			return instance;
		}
	}

	/**
	 * <p>File name filter for LMNtal intermediate code file.</p>
	 * <p>This class is designed as singleton.</p>
	 */
	private static final class ILCodeFileFilter extends FileFilter
	{
		private static ILCodeFileFilter instance;

		private ILCodeFileFilter() { }

		public boolean accept(File f)
		{
			String name = f.getName().toLowerCase();
			return f.isDirectory() || name.endsWith(".il") || name.endsWith(".tal");
		}

		public String getDescription()
		{
			return Lang.d[9] + " (*.il, *.tal)";
		}

		public static ILCodeFileFilter getInstance()
		{
			if (instance == null)
			{
				instance = new ILCodeFileFilter();
			}
			return instance;
		}
	}
}
