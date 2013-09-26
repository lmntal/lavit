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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import lavit.Env;
import lavit.FrontEnd;
import lavit.ui.ColoredLinePrinter;
import lavit.util.CommonFontUser;

@SuppressWarnings("serial")
public class LogPanel extends JPanel implements CommonFontUser
{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

	private ColoredLinePrinter log;

	public LogPanel()
	{
		setLayout(new BorderLayout());

		log = new ColoredLinePrinter();
		log.setEditable(false);
		log.addMouseListener(new PopupMenuTrigger(new RightMenu()));

		JScrollPane jsp = new JScrollPane(log);
		jsp.getVerticalScrollBar().setUnitIncrement(15);
		add(jsp, BorderLayout.CENTER);

		loadFont();
		FrontEnd.addFontUser(this);
	}

	public void loadFont()
	{
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));
		log.setFont(font);
	}

	public void printException(Exception e)
	{
		StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    errPrintln(sw.toString());
	}

	public void println(String str)
	{
		println(str, Color.BLACK, Color.WHITE);
	}

	public void errPrintln(String str)
	{
		println(str, Color.RED, Color.WHITE);
	}

	private void println(String str, Color fg, Color bg)
	{
		log.appendLine("[" + DATE_FORMAT.format(new Date()) + "] " + str, fg, bg);
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
					log.clear();
				}
			});
			add(clear);
		}
	}
}
