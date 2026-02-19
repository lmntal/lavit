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
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import lavit.Env;

@SuppressWarnings("serial")
public class LaViTSplashWindow extends JWindow
{
	private LaViTSplashWindow()
	{
		setAlwaysOnTop(true);
		setIconImages(Env.getApplicationIcons());

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		panel.setOpaque(true);
		panel.setBackground(Color.WHITE);
		add(panel);

		JLabel icon = new JLabel(new ImageIcon(Env.getImageOfFile("img/logo.png")));
		panel.add(icon, BorderLayout.CENTER);

		JLabel label = new JLabel("VERSION " + Env.APP_VERSION);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		label.setForeground(Color.GRAY);
		panel.add(label, BorderLayout.SOUTH);

		pack();
	}

	public static void showSplash(final long millis)
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					buildAndShow(millis);
				}
			});
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	private static void buildAndShow(final long millis)
	{
		final JWindow w = new LaViTSplashWindow();
		w.setLocationRelativeTo(null);
		w.setVisible(true);
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					sleep(millis);
				}
				catch (InterruptedException e)
				{
				}
				finally
				{
					w.dispose();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}
}
