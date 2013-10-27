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

package util;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LazyHighlighter
{
	private static final AttributeSet PLAIN_ATTR = new SimpleAttributeSet();

	private final MutableAttributeSet highlightAttr = new SimpleAttributeSet();
	private JTextPane textPane;
	private CharCondition headTester = CharCondition.any();
	private CharCondition partTester = CharCondition.any();
	private Markers marker = Markers.create();
	private IntervalActivator updateMarkerActivator;
	private IntervalActivator highlightActivator;
	private boolean useCaret;
	private DocumentListener documentListener;
	private MouseMotionListener mouseMotionListener;
	private CaretListener caretListener;

	public LazyHighlighter(JTextPane textPane)
	{
		this.textPane = textPane;

		updateMarkerActivator = new IntervalActivator(new UpdateMarkerAction(), 200);
		highlightActivator = new IntervalActivator(new HighlightAction(), 200);

		documentListener = new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)
			{
				updateMarkerActivator.activate();
			}

			public void insertUpdate(DocumentEvent e)
			{
				updateMarkerActivator.activate();
			}

			public void changedUpdate(DocumentEvent e)
			{
			}
		};

		mouseMotionListener = new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				if (!useCaret)
				{
					highlightActivator.activate();
				}
			}
		};

		caretListener = new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				if (useCaret)
				{
					highlightActivator.activate();
				}
			}
		};

		textPane.getDocument().addDocumentListener(documentListener);
		textPane.addMouseMotionListener(mouseMotionListener);
		textPane.addCaretListener(caretListener);

		setHighlightBackground(Color.YELLOW);
	}

	public LazyHighlighter setHighlightForeground(Color c)
	{
		synchronized (highlightAttr)
		{
			StyleConstants.setForeground(highlightAttr, c);
		}
		return this;
	}

	public LazyHighlighter removeHighlightForeground()
	{
		synchronized (highlightAttr)
		{
			highlightAttr.removeAttribute(StyleConstants.Foreground);
		}
		return this;
	}

	public LazyHighlighter setHighlightBackground(Color c)
	{
		synchronized (highlightAttr)
		{
			StyleConstants.setBackground(highlightAttr, c);
		}
		return this;
	}

	public LazyHighlighter removeHighlightBackground()
	{
		synchronized (highlightAttr)
		{
			highlightAttr.removeAttribute(StyleConstants.Background);
		}
		return this;
	}

	public LazyHighlighter setUseCaret(boolean b)
	{
		this.useCaret = b;
		return this;
	}

	public LazyHighlighter setHeadCharCondition(CharCondition cond)
	{
		this.headTester = cond;
		return this;
	}

	public LazyHighlighter setPartCharCondition(CharCondition cond)
	{
		this.partTester = cond;
		return this;
	}

	public void startActivator()
	{
		updateMarkerActivator.startMonitoring();
		highlightActivator.startMonitoring();
	}

	public void stopActivator()
	{
		updateMarkerActivator.stopMonitoring();
		highlightActivator.stopMonitoring();
	}

	public void updateMarkers()
	{
		try
		{
			Document doc = textPane.getDocument();
			String s = doc.getText(0, doc.getLength());
			StringBuilder buf = new StringBuilder();
			marker.clear();
			for (int i = 0; i < s.length(); )
			{
				char c = s.charAt(i);
				if (headTester.test(c))
				{
					int start = i;
					i++;
					buf.setLength(0);
					buf.append(c);
					while (i < s.length() && partTester.test(s.charAt(i)))
					{
						buf.append(s.charAt(i));
						i++;
					}
					marker.addMarker(start, buf.toString());
				}
				else
				{
					i++;
				}
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private int getPosition()
	{
		if (useCaret)
		{
			return textPane.getCaretPosition();
		}
		else
		{
			Point p = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(p, textPane);
			return textPane.viewToModel(p);
		}
	}

	private void updateHighlight()
	{
		final StyledDocument doc = textPane.getStyledDocument();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				doc.setCharacterAttributes(0, doc.getLength(), PLAIN_ATTR, true);
			}
		});

		for (final String word : marker.getMarkedWord(getPosition()))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					synchronized (highlightAttr)
					{
						for (int j : marker.getPositions(word))
						{
							doc.setCharacterAttributes(j, word.length(), highlightAttr, true);
						}
					}
				}
			});
		}
	}

	private class UpdateMarkerAction implements Runnable
	{
		public void run()
		{
			updateMarkers();
			highlightActivator.activate();
		}
	}

	private class HighlightAction implements Runnable
	{
		public void run()
		{
			updateHighlight();
		}
	}
}
