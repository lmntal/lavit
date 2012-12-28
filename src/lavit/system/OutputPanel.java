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

package lavit.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lavit.Env;
import lavit.FrontEnd;
import lavit.runner.RunnerOutputGetter;
import lavit.util.CommonFontUser;

@SuppressWarnings("serial")
public class OutputPanel extends JPanel implements RunnerOutputGetter, CommonFontUser
{
	private DefaultStyledDocument doc;
	private JTextPane log;

	private JPanel findPanel = new JPanel();
	private JLabel findLabel = new JLabel();
	private JTextField findField = new JTextField();

	private int line = 0;
	private int maxLine = 0;

	private int findLength = 0;
	private int findCursorPos = 0;
	private List<Integer> findResults = new ArrayList<Integer>();

	private RightMenu rightMenu = new RightMenu();

	public OutputPanel()
	{
		doc = new DefaultStyledDocument();
		log = new JTextPane(doc);
		log.setEditable(false);
		log.addMouseListener(new MenuTrigger());

		JScrollPane jsp = new JScrollPane(log);
		jsp.getVerticalScrollBar().setUnitIncrement(15);

		setLayout(new BorderLayout());
		add(jsp, BorderLayout.CENTER);

		FindFieldAction findAction = new FindFieldAction();
		KeyHandler keyHandler = new KeyHandler();

		JButton findButton = new JButton("Find");
		findButton.addActionListener(findAction);
		findButton.addKeyListener(keyHandler);
		findField.addActionListener(findAction);
		findField.addKeyListener(keyHandler);

		findPanel.setLayout(new BorderLayout());
		findPanel.add(findLabel, BorderLayout.WEST);
		findPanel.add(findField, BorderLayout.CENTER);
		findPanel.add(findButton, BorderLayout.EAST);
		add(findPanel, BorderLayout.SOUTH);

		line = 0;
		maxLine = Env.getInt("SYSTEM_OUTPUT_MAXLINE");

		loadFont();
		FrontEnd.addFontUser(this);

		findPanel.setVisible(Env.is("SYSTEM_OUTPUT_FIND"));
	}

	public void loadFont()
	{
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		log.setFont(font);
		findField.setFont(font);
		revalidate();
	}

	public void println(String str)
	{
		println(str, Color.BLACK, Color.WHITE);
	}

	public void errPrintln(String str)
	{
		println(str, Color.RED, Color.WHITE);
	}

	public void printTitle(String str)
	{
		println(str, Color.WHITE, Color.BLUE);
	}

	private synchronized void println(final String str, final Color foreground, final Color background)
	{
		line++;
		if (line <= maxLine)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					appendString(doc, str + "\n", foreground, background);
					log.setCaretPosition(doc.getLength());
				}
			});
			if (line == maxLine)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						appendString(doc, "Output is full.\n", Color.RED, Color.WHITE);
						log.setCaretPosition(doc.getLength());
					}
				});
			}
		}
		else
		{
			System.out.println(str);
		}
	}

	private static void appendString(Document doc, String s, Color fg, Color bg)
	{
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setForeground(attr, fg);
		StyleConstants.setBackground(attr, bg);
		try
		{
			doc.insertString(doc.getLength(), s, attr);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void toggleFindVisible()
	{
		findPanel.setVisible(!findPanel.isVisible());
		Env.set("SYSTEM_OUTPUT_FIND", findPanel.isVisible());
		resetFindResult();
	}

	private void resetFindResult()
	{
		SimpleAttributeSet attribute = new SimpleAttributeSet();
		StyleConstants.setBackground(attribute, Color.WHITE);

		if (findLength > 0)
		{
			for (int pos : findResults)
			{
				doc.setCharacterAttributes(pos, findLength, attribute, true);
			}
		}

		findLabel.setText("");

		findLength = 0;
		findCursorPos = 0;
		findResults.clear();
	}

	private void findAll()
	{
		try
		{
			SimpleAttributeSet attribute = new SimpleAttributeSet();
			StyleConstants.setBackground(attribute, Color.WHITE);
			String text = doc.getText(0, doc.getLength()).replaceAll("\r\n", "\n");

			findLength = findField.getText().length();
			if (findLength > 0)
			{
				StyleConstants.setBackground(attribute, Color.YELLOW);
				int pos = 0;
				while ((pos = text.indexOf(findField.getText(), pos)) >= 0)
				{
					findResults.add(pos);
					doc.setCharacterAttributes(pos, findLength, attribute, true);
					pos += findLength;
				}
				updateSelect();
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void updateSelect()
	{
		if (findResults.isEmpty())
		{
			findLabel.setText(" 0 / 0 ");
			return;
		}

		int N = findResults.size();
		int backPos = ((findCursorPos - 1) + N) % N;
		int nextPos = (findCursorPos + 1) % N;
		findCursorPos = (findCursorPos + N) % N;

		SimpleAttributeSet attribute = new SimpleAttributeSet();
		StyleConstants.setBackground(attribute, Color.YELLOW);
		doc.setCharacterAttributes(findResults.get(backPos), findLength, attribute, true);
		doc.setCharacterAttributes(findResults.get(nextPos), findLength, attribute, true);

		StyleConstants.setBackground(attribute, new Color(200, 200, 0));
		doc.setCharacterAttributes(findResults.get(findCursorPos), findLength, attribute, true);
		log.setCaretPosition(findResults.get(findCursorPos));

		findLabel.setText(" " + (findCursorPos + 1) + " / " + N + " ");
	}

	public void outputStart(String command, String option, File target)
	{
		printTitle("> " + command + " " + option + " " + target.getName());
	}

	public void outputLine(String str)
	{
		println(str);
	}

	public void outputEnd()
	{
		println("");
	}

	private class FindFieldAction implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			resetFindResult();
			findAll();
		}
	}

	private class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_DOWN:
				findCursorPos++;
				updateSelect();
				break;
			case KeyEvent.VK_UP:
				findCursorPos--;
				updateSelect();
				break;
			}
		}
	}

	private class MenuTrigger extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			checkPopupTrigger(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			checkPopupTrigger(e);
		}

		private void checkPopupTrigger(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				rightMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private class RightMenu extends JPopupMenu
	{
		public RightMenu()
		{
			JMenuItem clear = new JMenuItem("Clear");
			clear.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					log.setText("");
					line = 0;
					findLength = 0;
					findCursorPos = 0;
					findResults.clear();
				}
			});
			add(clear);

			JCheckBoxMenuItem find = new JCheckBoxMenuItem("Find");
			find.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					toggleFindVisible();
				}
			});
			find.setSelected(Env.is("SYSTEM_OUTPUT_FIND"));
			add(find);
		}
	}
}
