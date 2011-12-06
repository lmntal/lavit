package lavit.multiedit.coloring;

import java.util.ArrayList;
import java.util.List;
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

import lavit.multiedit.coloring.lexer.Lexer;
import lavit.multiedit.coloring.lexer.TokenLabel;

@SuppressWarnings("serial")
public class LmnDocument extends DefaultStyledDocument
{
	private TreeSet<ColorLabel> _labels = new TreeSet<ColorLabel>();
	private List<Integer> _tabs = new ArrayList<Integer>();
	private int[] _parenPair = null;
	
	public LmnDocument()
	{
		addDocumentListener(new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)
			{
				reparse();
			}
			public void insertUpdate(DocumentEvent e)
			{
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
	
	private void reparse()
	{
		try
		{
			String text = getText(0, getLength()).replace("\r\n", "\n");
			Lexer lexer = new Lexer(text);
			
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
