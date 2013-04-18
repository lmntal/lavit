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

package extgui.fileview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import extgui.TitledPane;
import extgui.fileview.event.DirectoryChangeListener;
import extgui.fileview.event.FileSelectedListener;
import extgui.flatsplitpane.FlatSplitPane;

/**
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
public class FileViewPane extends FlatSplitPane
{
	private static final Icon ICON_HIDE = new ImageIcon(FileViewPane.class.getResource("hide.png"));
	private static final Icon ICON_REFRESH = new ImageIcon(FileViewPane.class.getResource("refresh.png"));
	private static final Icon ICON_UP = new ImageIcon(FileViewPane.class.getResource("up.png"));
	private static final Icon ICON_DOWN = new ImageIcon(FileViewPane.class.getResource("down.png"));

	private FileTree fileTree;
	private TitledPane fileTreeContainer;
	private JTextField pathInput;
	private int savedDividerSize = 3;
	private int savedDividerLocation = 200;

	public FileViewPane(JComponent contentComponent)
	{
		JPanel panel = new JPanel(new BorderLayout());
		fileTree = new FileTree();
		fileTree.addDirectoryChangeListener(new DirectoryChangeListener()
		{
			public void directoryChanged(File dir)
			{
				String s = dir.getAbsolutePath();
				pathInput.setText(s);
				pathInput.setCaretPosition(s.length());
			}
		});
		panel.add(createToolBar(), BorderLayout.NORTH);
		panel.add(fileTree, BorderLayout.CENTER);

		fileTreeContainer = new TitledPane("File System", panel);
		setLeftComponent(fileTreeContainer);
		setRightComponent(contentComponent);
		setContinuousLayout(true);

		setDividerSize(3);
		setResizeWeight(0);
	}

	private JToolBar createToolBar()
	{
		JToolBar toolBar = new FixedToolBar();
		toolBar.setFloatable(false);

		pathInput = new JTextField();
		pathInput.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File dir = new File(pathInput.getText());
				if (dir.exists() && dir.isDirectory())
				{
					fileTree.setBaseDirectory(dir);
					fileTree.refresh();
				}
			}
		});
		toolBar.add(pathInput);

		JButton buttonHide = new JButton(ICON_HIDE);
		buttonHide.setToolTipText("Minimize");
		buttonHide.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFileViewVisible(false);
			}
		});
		toolBar.add(buttonHide);

		JButton buttonRefresh = new JButton(ICON_REFRESH);
		buttonRefresh.setToolTipText("Refresh");
		buttonRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				refresh();
			}
		});
		toolBar.add(buttonRefresh);

		JButton buttonAscend = new JButton(ICON_UP);
		buttonAscend.setToolTipText("Go up to parent directory");
		buttonAscend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				goUpToParentDirectory();
			}
		});
		toolBar.add(buttonAscend);

		JButton buttonDescend = new JButton(ICON_DOWN);
		buttonDescend.setToolTipText("Go into a selected directory");
		buttonDescend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				goIntoSelectedDirectory();
			}
		});
		toolBar.add(buttonDescend);

		return toolBar;
	}

	public void addFileSelectedListener(FileSelectedListener l)
	{
		fileTree.addFileSelectedListener(l);
	}

	public void removeFileSelectedListener(FileSelectedListener l)
	{
		fileTree.removeFileSelectedListener(l);
	}

	public void setRootDirectory(File dir)
	{
		fileTree.setBaseDirectory(dir);
	}

	public void setFileFilter(FileFilter fileFilter)
	{
		fileTree.setFileFilter(fileFilter);
	}

	public void refresh()
	{
		fileTree.refresh();
	}

	public void setFileViewVisible(boolean b)
	{
		if (fileTreeContainer.isVisible() == b) return;

		fileTreeContainer.setVisible(b);
		if (b)
		{
			restoreDividerStatus();
		}
		else
		{
			saveDividerStatus();
			minimizeLeftArea();
		}
	}

	public boolean isFileViewVisible()
	{
		return fileTreeContainer.isVisible();
	}

	private void saveDividerStatus()
	{
		savedDividerSize = getDividerSize();
		savedDividerLocation = getDividerLocation();
	}

	private void restoreDividerStatus()
	{
		setDividerSize(savedDividerSize);
		setDividerLocation(savedDividerLocation);
	}

	private void minimizeLeftArea()
	{
		setDividerSize(0);
		setDividerLocation(0);
	}

	private void goUpToParentDirectory()
	{
		File parent = fileTree.getBaseDirectory().getParentFile();
		if (parent != null)
		{
			fileTree.setBaseDirectory(parent);
			fileTree.refresh();
		}
	}

	private void goIntoSelectedDirectory()
	{
		File dir = fileTree.getSelectedFile();
		if (dir != null && dir.isDirectory())
		{
			fileTree.setBaseDirectory(dir);
			fileTree.refresh();
		}
	}

	private static class FixedToolBar extends JToolBar
	{
		public void updateUI()
		{
			super.updateUI();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
	}
}
