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
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.util.StringUtils;

public class StartupFrame extends JWindow
{
	private static final long serialVersionUID = 1L;

	public StartupFrame()
	{
		ImageIcon image = new ImageIcon(Env.getImageOfFile("img/logo.png"));

		setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		panel.setBackground(new Color(255,255,255));
		panel.setLayout(new BorderLayout());
		add(panel);

		JLabel icon = new JLabel(image);
		panel.add(icon,BorderLayout.CENTER);

		JLabel text = new JLabel("Version "+Env.APP_VERSION);
		text.setHorizontalAlignment(SwingConstants.CENTER);
		text.setBackground(new Color(255,255,255));
		panel.add(text, BorderLayout.SOUTH);

		pack();
	}

	// TODO: [refactor] initial settings
	private LangSettingFrame langFrame;
	private SlimPathSettingFrame slimFrame;
	private CygwinPathSettingFrame cygwinFrame;

	public void startEnvSet()
	{
		if (StringUtils.nullOrEmpty(Env.get("LANG")))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					langFrame = new LangSettingFrame();
				}
			});
			while (langFrame == null || langFrame.isDisplayable())
			{
				FrontEnd.sleep(200);
			}
		}

		Lang.set(Env.get("LANG"));

		if (Env.isWindows() && !Env.isSet("WINDOWS_CYGWIN_DIR"))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					cygwinFrame = new CygwinPathSettingFrame();
				}
			});
			while (cygwinFrame == null || cygwinFrame.isDisplayable())
			{
				FrontEnd.sleep(200);
			}
		}

		File lmntal = new File(Env.LMNTAL_LIBRARY_DIR+File.separator+"bin"+File.separator+"lmntal");
		if (lmntal.exists() && !lmntal.canExecute())
		{
			lmntal.setExecutable(true);
		}

		if (StringUtils.nullOrEmpty(Env.get("SLIM_EXE_PATH")))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					slimFrame = new SlimPathSettingFrame();
				}
			});
			while (slimFrame == null || !slimFrame.isEnd())
			{
				FrontEnd.sleep(200);
			}
		}
	}
}
