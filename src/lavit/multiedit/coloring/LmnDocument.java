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

package lavit.multiedit.coloring;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.undo.UndoManager;

import lavit.multiedit.coloring.event.DirtyFlagChangeListener;
import lavit.multiedit.coloring.lexer.ColorLabel;
import lavit.multiedit.coloring.lexer.Lexer;
import lavit.multiedit.coloring.lexer.TokenLabel;

@SuppressWarnings("serial")
public class LmnDocument extends DefaultStyledDocument
{
	private TreeSet<ColorLabel> labels = new TreeSet<ColorLabel>();
	private List<Integer> tabs = new ArrayList<Integer>();
	private int[] parenPair = null;
	private int tabWidth = 8;

	private boolean dirty;
	private int hlFlags;
	private boolean showTabs;
	private boolean showEols;

	private UndoManager undo = new UndoManager();
	private DocumentListener updateObserver;

	public LmnDocument()
	{
		undo.setLimit(1000);
		addUndoableEditListener(undo);

		updateObserver = new DocumentUpdateObserver();
		addDocumentListener(updateObserver);
	}

	public void initializeText(String text)
	{
		removeDocumentListener(updateObserver);
		try
		{
			remove(0, getLength());
			insertString(0, text, null);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		finally
		{
			addDocumentListener(updateObserver);
		}
	}

	public String getRowText()
	{
		try
		{
			return getText(0, getLength());
		}
		catch (BadLocationException e)
		{
		}
		return null;
	}

	public boolean canUndo()
	{
		return undo.canUndo();
	}

	public boolean canRedo()
	{
		return undo.canRedo();
	}

	public void undo()
	{
		if (canUndo())
		{
			undo.undo();
		}
	}

	public void redo()
	{
		if (canRedo())
		{
			undo.redo();
		}
	}

	public void clearUndo()
	{
		undo.discardAllEdits();
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty(boolean b)
	{
		boolean changed = dirty != b;
		dirty = b;
		if (changed)
		{
			dispatchDirtyFlagChangeListener(dirty);
		}
	}

	public TreeSet<ColorLabel> getLabels()
	{
		return labels;
	}

	public List<Integer> getTabs()
	{
		return tabs;
	}

	public void setParenPair(int[] pair)
	{
		parenPair = pair;
	}

	public TreeSet<ColorLabel> getParenPairSet()
	{
		TreeSet<ColorLabel> set = new TreeSet<ColorLabel>();
		if (parenPair != null && parenPair.length == 2)
		{
			set.add(new ColorLabel(parenPair[0], 1, TokenLabel.OPERATOR));
			set.add(new ColorLabel(parenPair[1], 1, TokenLabel.OPERATOR));
		}
		return set;
	}

	public void setHighlightFlags(int flags)
	{
		hlFlags = flags;
	}

	public void addHighlight(int labelKind)
	{
		hlFlags |= labelKind;
	}

	public void removeHighlight(int labelKind)
	{
		hlFlags &= ~labelKind;
	}

	public boolean getShowTabs()
	{
		return showTabs;
	}

	public void setShowTabs(boolean b)
	{
		showTabs = b;
	}

	public boolean getShowEols()
	{
		return showEols;
	}

	public void setShowEols(boolean b)
	{
		showEols = b;
	}

	public void reparse()
	{
		try
		{
			String text = getText(0, getLength());
			Lexer lexer = new Lexer(text, hlFlags);

			labels = lexer.lex();

			tabs.clear();
			tabs.addAll(lexer.getTabs());
		}
		catch (BadLocationException e)
		{
		}
	}

	public void setTabWidth(int spaces)
	{
		tabWidth = spaces;
	}

	public int getTabWidth()
	{
		return tabWidth;
	}

	public void addDirtyFlagChangeListener(DirtyFlagChangeListener l)
	{
		listenerList.add(DirtyFlagChangeListener.class, l);
	}

	private void dispatchDirtyFlagChangeListener(boolean dirty)
	{
		for (DirtyFlagChangeListener l : listenerList.getListeners(DirtyFlagChangeListener.class))
		{
			l.dirtyFlagChanged(dirty);
		}
	}

	private void updateHighlight()
	{
		setDirty(true);
		reparse();
	}

	private class DocumentUpdateObserver implements DocumentListener
	{
		public void removeUpdate(DocumentEvent e)
		{
			updateHighlight();
		}

		public void insertUpdate(DocumentEvent e)
		{
			updateHighlight();
		}

		public void changedUpdate(DocumentEvent e)
		{
		}
	}
}
