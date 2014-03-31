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

package lavit.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import lavit.util.Option;

@SuppressWarnings("serial")
public class FindReplaceDialog extends JDialog
{
	private static FindReplaceDialog instance;

	private JTextComponent targetTextComponent;

	private JTextField findText;
	private JTextField replaceText;
	private JButton findForward;
	private JButton findBackward;
	private JButton replace;
	private JButton replaceAll;
	private JCheckBox checkRegex;
	private JCheckBox checkWrap;
	private JLabel labelMessage;
	private JButton buttonClose;

	private boolean isQueryDirty = true;
	private Pattern queryPattern;

	private FindReplaceDialog()
	{
		setTitle("Find/Replace");

		JPanel contents = new JPanel();

		JLabel labelFind = new JLabel("Find:");
		JLabel labelReplace = new JLabel("Replace:");
		findText = new JTextField();
		findText.setColumns(20);
		findText.getDocument().addDocumentListener(new QueryDocumentListener());
		replaceText = new JTextField();
		replaceText.setColumns(20);
		findForward = new JButton("Find forward");
		findForward.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doFind(true, false);
			}
		});
		findBackward = new JButton("Find backward");
		findBackward.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doFind(false, false);
			}
		});
		replace = new JButton("Replace");
		replace.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doFind(true, true);
			}
		});
		replaceAll = new JButton("Replace all");
		replaceAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doReplaceAll();
			}
		});
		checkRegex = new JCheckBox("Regex");
		checkWrap = new JCheckBox("Wrap search");
		labelMessage = new JLabel("Ready.");

		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		checkBoxPanel.add(checkRegex);
		checkBoxPanel.add(checkWrap);

		GroupLayout gl = new GroupLayout(contents);
		contents.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.LEADING)
			.addGroup(gl.createSequentialGroup()
				.addGroup(gl.createParallelGroup()
					.addComponent(labelFind)
					.addComponent(labelReplace)
				)
				.addGroup(gl.createParallelGroup()
					.addComponent(findText)
					.addComponent(replaceText)
					.addComponent(checkBoxPanel)
				)
				.addGroup(gl.createParallelGroup()
					.addComponent(findForward)
					.addComponent(findBackward)
					.addComponent(replace)
					.addComponent(replaceAll)
				)
			)
			.addComponent(labelMessage)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(labelFind)
				.addComponent(findText)
				.addComponent(findForward)
			)
			.addComponent(findBackward)
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(labelReplace)
				.addComponent(replaceText)
				.addComponent(replace)
			)
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(replaceAll)
				.addComponent(checkBoxPanel)
			)
			.addComponent(labelMessage)
		);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
					BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)
				)
			)
		);
		buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				closeDialog();
			}
		});
		buttonPanel.add(buttonClose);

		fixButtonWidth(120, findForward, findBackward, replace, replaceAll, buttonClose);

		add(contents, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		pack();

		setAlwaysOnTop(true);
		setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	public void setTargetTextComponent(JTextComponent targetTextComponent)
	{
		this.targetTextComponent = targetTextComponent;
	}

	public void showDialog()
	{
		setVisible(true);
	}

	private void closeDialog()
	{
		setVisible(false);
	}

	private boolean isRegex()
	{
		return checkRegex.isSelected();
	}

	private boolean isWrapSearch()
	{
		return checkWrap.isSelected();
	}

	private void doFind(boolean forward, final boolean replace)
	{
		setMessage("");
		Option<Range> result;
		if (isRegex())
		{
			result = progressFindRegex(forward, isWrapSearch());
		}
		else
		{
			result = progressFind(forward, isWrapSearch());
		}
		result.accept(new Option.IVisitor<Range, Object>()
		{
			public Object visitSome(Range range)
			{
				setSelection(range.start, range.end);
				if (replace)
				{
					askReplace();
				}
				return null;
			}

			public Object visitNone()
			{
				setErrorMessage("text not found.");
				return null;
			}
		});
	}

	private void doReplaceAll()
	{
		executeReplaceAll(true, isWrapSearch());
	}

	private Option<Pattern> getQueryPattern(String patternText)
	{
		if (isQueryDirty)
		{
			try
			{
				queryPattern = Pattern.compile(patternText);
				isQueryDirty = false;
			}
			catch (PatternSyntaxException e)
			{
				return Option.none();
			}
		}
		return Option.some(queryPattern);
	}

	private String getText()
	{
		return targetTextComponent.getText().replace("\r\n", "\n");
	}

	private Option<Range> progressFind(boolean forward, boolean wrap)
	{
		return progressFind(new TextFinder(findText.getText()), forward, wrap);
	}

	private Option<Range> progressFindRegex(final boolean forward, final boolean wrap)
	{
		return getQueryPattern(findText.getText()).accept(new Option.IVisitor<Pattern, Option<Range>>()
		{
			public Option<Range> visitNone()
			{
				setErrorMessage("regular expression syntax error.");
				return Option.none();
			}

			public Option<Range> visitSome(Pattern pattern)
			{
				return progressFind(new RegexTextFinder(pattern), forward, wrap);
			}
		});
	}

	private Option<Range> progressFind(final ITextFinder finder, final boolean forward, final boolean wrap)
	{
		final String text = getText();
		Option<Range> result;
		if (forward)
		{
			result = finder.selectNextMatch(text, targetTextComponent.getSelectionEnd());
		}
		else
		{
			result = finder.selectPreviousMatch(text, targetTextComponent.getSelectionStart());
		}
		return result.accept(new Option.IVisitor<Range, Option<Range>>()
		{
			public Option<Range> visitNone()
			{
				if (wrap)
				{
					if (forward)
					{
						return finder.selectNextMatch(text, 0);
					}
					else
					{
						return finder.selectPreviousMatch(text, text.length());
					}
				}
				return Option.none();
			}

			public Option<Range> visitSome(Range item)
			{
				return Option.some(item);
			}
		});
	}

	private void askReplace()
	{
		String from = targetTextComponent.getSelectedText();
		String to = replaceText.getText();
		int ret = JOptionPane.showConfirmDialog(this, "Replace \"" + from + "\" to \"" + to + "\"?", "Replace", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION)
		{
			targetTextComponent.replaceSelection(replaceText.getText());
		}
	}

	private void executeReplaceAll(boolean forward, boolean wrap)
	{
		int count = stepReplaceAll(forward, wrap);
		setMessage("replaced " + count + " tokens.");
	}

	private int stepReplaceAll(final boolean forward, final boolean wrap)
	{
		return progressFindRegex(forward, wrap).accept(new Option.IVisitor<Range, Integer>()
		{
			public Integer visitSome(Range range)
			{
				setSelection(range.start, range.end);
				targetTextComponent.replaceSelection(replaceText.getText());
				return 1 + stepReplaceAll(forward, wrap);
			}

			public Integer visitNone()
			{
				return 0;
			}
		});
	}

	private void setSelection(int start, int end)
	{
		targetTextComponent.setSelectionStart(start);
		targetTextComponent.setSelectionEnd(end);
		targetTextComponent.requestFocusInWindow();
	}

	private void setMessage(String message)
	{
		labelMessage.setForeground(Color.BLACK);
		labelMessage.setText(message);
	}

	private void setErrorMessage(String message)
	{
		labelMessage.setForeground(Color.RED);
		labelMessage.setText(message);
	}

	public static FindReplaceDialog getDialog()
	{
		if (instance == null)
		{
			instance = new FindReplaceDialog();
		}
		return instance;
	}

	private static void fixButtonWidth(int minWidth, JButton ... buttons)
	{
		int w = minWidth;
		int h = 0;
		for (JButton b : buttons)
		{
			Dimension dim = b.getPreferredSize();
			w = Math.max(dim.width, w);
			h = Math.max(dim.height, h);
		}
		Dimension size = new Dimension(w, h);
		for (JButton b : buttons)
		{
			b.setPreferredSize(size);
			b.setMaximumSize(size);
			b.setMinimumSize(size);
		}
	}

	private class QueryDocumentListener implements DocumentListener
	{
		public void insertUpdate(DocumentEvent e)
		{
			isQueryDirty = true;
		}

		public void removeUpdate(DocumentEvent e)
		{
			isQueryDirty = true;
		}

		public void changedUpdate(DocumentEvent e)
		{
		}
	}

	private static class Range
	{
		public final int start, end;

		public Range(int i, int j)
		{
			this.start = Math.min(i, j);
			this.end = Math.max(i, j);
		}
	}

	private interface ITextFinder
	{
		public Option<Range> selectNextMatch(String text, int startIndex);
		public Option<Range> selectPreviousMatch(String text, int endIndex);
	}

	private static class TextFinder implements ITextFinder
	{
		private String query;

		public TextFinder(String query)
		{
			this.query = query;
		}

		public Option<Range> selectNextMatch(String text, int startIndex)
		{
			int i = text.indexOf(query, startIndex);
			if (i != -1)
			{
				return Option.some(new Range(i, i + query.length()));
			}
			return Option.none();
		}

		public Option<Range> selectPreviousMatch(String text, int endIndex)
		{
			int i = 0, j = -1;
			while (i < text.length())
			{
				i = text.indexOf(query, i);
				if (i == -1 || endIndex <= i)
				{
					break;
				}
				j = i;
				i += query.length();
			}
			if (j != -1)
			{
				return Option.some(new Range(j, j + query.length()));
			}
			return Option.none();
		}
	}

	private static class RegexTextFinder implements ITextFinder
	{
		private Pattern pattern;

		public RegexTextFinder(Pattern pattern)
		{
			this.pattern = pattern;
		}

		public Option<Range> selectNextMatch(String text, int startIndex)
		{
			Matcher m = pattern.matcher(text);
			if (m.find(startIndex))
			{
				return Option.some(new Range(m.start(), m.end()));
			}
			return Option.none();
		}

		public Option<Range> selectPreviousMatch(String text, int endIndex)
		{
			Matcher m = pattern.matcher(text);
			int start = -1, end = -1;
			while (m.find())
			{
				if (endIndex <= m.start())
				{
					break;
				}
				start = m.start();
				end = m.end();
			}
			if (start != -1)
			{
				return Option.some(new Range(start, end));
			}
			return Option.none();
		}
	}
}
