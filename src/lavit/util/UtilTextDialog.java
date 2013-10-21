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

package lavit.util;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lavit.Env;
import lavit.FrontEnd;
import lavit.editor.AutoStyledDocument;
import lavit.frame.ChildWindowListener;

@SuppressWarnings("serial")
public class UtilTextDialog extends JDialog
{
	private static final Object LOCK = new Object();
	private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	private static Point initialLocation = null;
	private static Point location = null;

	public UtilTextDialog(String title, String str)
	{
		super(FrontEnd.mainFrame, title);

		setSize(300, 200);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		AutoStyledDocument doc = new AutoStyledDocument();
		JTextPane editor = new JTextPane();
		editor.setDocument(doc);
		editor.setFont(Env.getEditorFont());
		editor.setText(str);
		doc.colorChange();
		doc.end();

		add(new JScrollPane(editor));

		addWindowListener(new ChildWindowListener(this));

		initLocation();
	}

	private void initLocation()
	{
		synchronized (LOCK)
		{
			if (location == null)
			{
				initialLocation = getLocation();
				location = getLocation();
			}
			else
			{
				location.translate(20, 20);
				if (SCREEN_SIZE.height <= location.y + getHeight())
				{
					location.y = initialLocation.y;
				}
				if (SCREEN_SIZE.width <= location.x + getWidth())
				{
					location.x = initialLocation.x;
				}
				setLocation(location);
			}
		}
	}

	public static void showDialog(final String title, final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				UtilTextDialog dialog = new UtilTextDialog(title, text);
				dialog.setVisible(true);
			}
		});
	}
}
