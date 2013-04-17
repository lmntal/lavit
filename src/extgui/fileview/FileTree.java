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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lavit.util.FileUtils;
import extgui.fileview.event.DirectoryChangeListener;
import extgui.fileview.event.FileSelectedListener;

/**
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
public class FileTree extends JComponent
{
	private static final TreeItem PENDING = TreeItem.label("<Loading...>");
	private static final TreeItem EMPTY = TreeItem.label("<Empty>");
	private static final Icon ICON_LEAF = new ImageIcon(FileTree.class.getResource("leaf.png"));

	private DefaultTreeModel model;
	private JTree tree;
	private File baseDir = new File(".");
	private FileFilter fileFilter = new DefaultFileFilter();

	public FileTree()
	{
		setLayout(new BorderLayout());

		model = new DefaultTreeModel(null, true);
		tree = new NullBorderJTree(model);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.addTreeExpansionListener(new TreeExpansionListener()
		{
			public void treeExpanded(TreeExpansionEvent e)
			{
				expandPath(e.getPath());
			}

			public void treeCollapsed(TreeExpansionEvent e)
			{
			}
		});
		tree.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				onNodeClicked(e);
			}
		});
		setTreeCellRenderer();

		add(new NullBorderJScrollPane(tree));

		setBaseDirectory(new File("."));
		refresh();
	}

	public File getBaseDirectory()
	{
		return baseDir;
	}

	public void setBaseDirectory(File dir)
	{
		baseDir = dir.getAbsoluteFile();
		try
		{
			baseDir = baseDir.getCanonicalFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		dispatchDirectoryChangeListener(baseDir);
	}

	public void setFileFilter(FileFilter filter)
	{
		this.fileFilter = filter;
	}

	public File getSelectedFile()
	{
		TreePath path = tree.getSelectionPath();
		if (path != null)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			if (!node.isRoot())
			{
				TreeItem item = (TreeItem)node.getUserObject();
				if (item.hasFile())
				{
					return item.getFile();
				}
			}
		}
		return null;
	}

	public void refresh()
	{
		DefaultMutableTreeNode tempRoot = new DefaultMutableTreeNode(TreeItem.file(baseDir), true);
		tempRoot.add(new DefaultMutableTreeNode(PENDING, false));
		setRootNode(tempRoot);
		doInBackground(new Runnable()
		{
			public void run()
			{
				TreeNode root = createDirectoryNode(baseDir);
				setRootNode(root);
			}
		});
	}

	public void updateUI()
	{
		super.updateUI();
		setTreeCellRenderer();
	}

	private void setTreeCellRenderer()
	{
		FileTreeCellRenderer renderer = new FileTreeCellRenderer();
		renderer.setLeafIcon(ICON_LEAF);
		tree.setCellRenderer(renderer);
	}

	private void setRootNode(final TreeNode root)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				model.setRoot(root);
			}
		});
	}

	private void dispatchFileSelectedListener(File file)
	{
		for (FileSelectedListener l : listenerList.getListeners(FileSelectedListener.class))
		{
			l.fileSelected(file);
		}
	}

	private void expandPath(final TreePath path)
	{
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		if (isPending(node))
		{
			doInBackground(new Runnable()
			{
				public void run()
				{
					addChildren(node);
					notifyTreeModelChanged(node);
				}
			});
		}
	}

	public void addFileSelectedListener(FileSelectedListener l)
	{
		listenerList.add(FileSelectedListener.class, l);
	}

	public void removeFileSelectedListener(FileSelectedListener l)
	{
		listenerList.remove(FileSelectedListener.class, l);
	}

	public void addDirectoryChangeListener(DirectoryChangeListener l)
	{
		listenerList.add(DirectoryChangeListener.class, l);
	}

	public void removeDirectoryChangeListener(DirectoryChangeListener l)
	{
		listenerList.remove(DirectoryChangeListener.class, l);
	}

	private void dispatchDirectoryChangeListener(File dir)
	{
		for (DirectoryChangeListener l : listenerList.getListeners(DirectoryChangeListener.class))
		{
			l.directoryChanged(dir);
		}
	}

	private static boolean isPending(TreeNode node)
	{
		for (int i = 0; i < node.getChildCount(); i++)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
			if (child.getUserObject().equals(PENDING))
			{
				return true;
			}
		}
		return false;
	}

	private void onNodeClicked(MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
		{
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path != null)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				if (node.isLeaf())
				{
					TreeItem item = (TreeItem)node.getUserObject();
					if (item.hasFile())
					{
						dispatchFileSelectedListener(item.getFile());
					}
				}
			}
		}
	}

	private void notifyTreeModelChanged(final TreeNode node)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				model.nodeStructureChanged(node);
			}
		});
	}

	private void addChildren(DefaultMutableTreeNode root)
	{
		TreeItem item = (TreeItem)root.getUserObject();
		File dir = item.getFile();
		if (dir.exists() && dir.isDirectory())
		{
			root.removeAllChildren();
			for (File file : FileUtils.enumFiles(dir, fileFilter))
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(TreeItem.file(file), false);
				if (file.isDirectory())
				{
					node.setAllowsChildren(true);
					node.add(new DefaultMutableTreeNode(PENDING, false));
				}
				root.add(node);
			}
			if (root.getChildCount() == 0)
			{
				root.add(new DefaultMutableTreeNode(EMPTY, false));
			}
		}
	}

	private TreeNode createDirectoryNode(File dir)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(TreeItem.file(dir), true);
		addChildren(root);
		return root;
	}

	private static void doInBackground(Runnable task)
	{
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private static final Stroke STROKE_FOCUSRECT = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[] { 1, 1 }, 0);

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (value instanceof DefaultMutableTreeNode)
			{
				TreeItem item = (TreeItem)((DefaultMutableTreeNode)value).getUserObject();
				if (item.hasFile())
				{
					textSelectionColor = Color.BLACK;
					textNonSelectionColor = Color.BLACK;
				}
				else
				{
					selected = false;
					hasFocus = false;
					textSelectionColor = Color.LIGHT_GRAY;
					textNonSelectionColor = Color.LIGHT_GRAY;
				}
			}
			return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}

		public void paint(Graphics g)
		{
			((Graphics2D)g).setStroke(STROKE_FOCUSRECT);
			super.paint(g);
		}
	}

	private static class NullBorderJTree extends JTree
	{
		private static final long serialVersionUID = 1L;

		public NullBorderJTree(TreeModel model)
		{
			super(model);
		}

		public void updateUI()
		{
			super.updateUI();
			setBorder(null);
		}
	}

	private static class NullBorderJScrollPane extends JScrollPane
	{
		private static final long serialVersionUID = 1L;

		public NullBorderJScrollPane(Component view)
		{
			super(view);
		}

		public void updateUI()
		{
			super.updateUI();
			setBorder(null);
		}
	}

	private static class DefaultFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			return true;
		}
	}
}

abstract class TreeItem
{
	public boolean hasFile()
	{
		return false;
	}

	public File getFile()
	{
		return null;
	}

	public static TreeItem file(final File file)
	{
		return new TreeItem()
		{
			public boolean hasFile()
			{
				return true;
			}

			public File getFile()
			{
				return file;
			}

			public String toString()
			{
				return file.getName();
			}
		};
	}

	public static TreeItem label(final String label)
	{
		return new TreeItem()
		{
			public String toString()
			{
				return label;
			}
		};
	}
}
