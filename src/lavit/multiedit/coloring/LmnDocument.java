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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.undo.UndoManager;

import lavit.multiedit.coloring.lexer.ColorLabel;
import lavit.multiedit.coloring.lexer.Lexer;
import lavit.multiedit.coloring.lexer.TokenLabel;

@SuppressWarnings("serial")
public class LmnDocument extends DefaultStyledDocument
{
	private TreeSet<ColorLabel> _labels = new TreeSet<ColorLabel>();
	private List<Integer> _tabs = new ArrayList<Integer>();
	private int[] _parenPair = null;
	
	private boolean _modified;
	private int _hlFlags;
	
	private UndoManager _undo = new UndoManager();
	
	public LmnDocument()
	{
		_undo.setLimit(1000);
		addUndoableEditListener(_undo);
		
		addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)
			{
				setModified(true);
				reparse();
			}
			public void insertUpdate(DocumentEvent e)
			{
				setModified(true);
				try
				{
					String ins = getText(e.getOffset(), e.getLength());
					if (ins.endsWith("\n"))
					{
						Element elem = getParagraphElement(e.getOffset() - 1);
						String prevLine = getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
						String indent = "";
						for (int i = 0; i < prevLine.length(); i++)
						{
							char c = prevLine.charAt(i);
							if (c != ' ' && c != '\t') break;
							indent += c;
						}
						
						final String fs = indent;
						final int pos = e.getOffset() + e.getLength();
						
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								try
								{
									insertString(pos, fs, null);
								}
								catch (BadLocationException e)
								{
									e.printStackTrace();
								}
							}
						});
					}
				}
				catch (BadLocationException e1)
				{
					e1.printStackTrace();
				}
				reparse();
			}
			public void changedUpdate(DocumentEvent e) { }
		});
	}
	
	public boolean canUndo()
	{
		return _undo.canUndo();
	}
	
	public boolean canRedo()
	{
		return _undo.canRedo();
	}
	
	public void undo()
	{
		if (canUndo())
		{
			_undo.undo();
		}
	}
	
	public void redo()
	{
		if (canRedo())
		{
			_undo.redo();
		}
	}
	
	public void clearUndo()
	{
		_undo.discardAllEdits();
	}
	
	public boolean isModified()
	{
		return _modified;
	}
	
	public void setModified(boolean b)
	{
		_modified = b;
	}
	
	public TreeSet<ColorLabel> getLabels()
	{
		return _labels;
	}
	
	public List<Integer> getTabs()
	{
		return _tabs;
	}
	
	public void setParenPair(int[] pair)
	{
		_parenPair = pair;
	}
	
	public TreeSet<ColorLabel> getParenPairSet()
	{
		TreeSet<ColorLabel> set = new TreeSet<ColorLabel>();
		if (_parenPair != null && _parenPair.length == 2)
		{
			set.add(new ColorLabel(_parenPair[0], 1, TokenLabel.OPERATOR));
			set.add(new ColorLabel(_parenPair[1], 1, TokenLabel.OPERATOR));
		}
		return set;
	}
	
	public void addHighlight(int labelKind)
	{
		_hlFlags |= labelKind;
	}
	
	public void removeHighlight(int labelKind)
	{
		_hlFlags &= ~labelKind;
	}
	
	public void reparse()
	{
		try
		{
			String text = getText(0, getLength()).replace("\r\n", "\n");
			Lexer lexer = new Lexer(text, _hlFlags);
			
			_labels = lexer.lex();
			
			_tabs.clear();
			_tabs.addAll(lexer.getTabs());
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}
	
	// Implementation of tab setting.
	
	private static final class CustomTabSet extends TabSet
	{
		private int _width;

		public CustomTabSet(int width)
		{
			super(new TabStop[] { new TabStop(width) });
			_width = width;
		}

		@Override
		public TabStop getTabAfter(float x)
		{
			return new TabStop((int)Math.ceil(x / _width) * _width);
		}
	}

	public void setTabWidth(int width)
	{
		TabSet tabSet = new CustomTabSet(width);
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setTabSet(attr, tabSet);
		setParagraphAttributes(0, getLength(), attr, false);
	}
}