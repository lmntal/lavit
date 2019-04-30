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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import lavit.Env;
import lavit.localizedtext.MsgID;
import lavit.ui.PathInputField;

public final class LMNtalCompilerPathSetting
{
	private LMNtalCompilerPathSetting() { }

	public static void showDialog(JFrame owner)
	{
		ContentPanel panel = new ContentPanel();
		ModalSettingDialog dialog = ModalSettingDialog.createDialog(owner, panel);
		dialog.setDialogTitle("LMNtal Compiler");
		dialog.setHeadLineText("LMNtal Compiler Location");
		dialog.setDescriptionText("Set the location of the LMNtal Compiler you'd like to use.");
		dialog.setDialogIconImages(Env.getApplicationIcons());

		boolean approved = dialog.showDialog();
		if (approved)
		{
			Env.set("path.lmntalcompiler", panel.getInputPath());
		}
	}

	@SuppressWarnings("serial")
	private static class ContentPanel extends JPanel
	{
		private PathInputField pathInput;

		ContentPanel()
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(new FileFilter()
			{
				public String getDescription()
				{
					return "LMNtal Compiler (lmntal.jar)";
				}

				public boolean accept(File file)
				{
				        return file.isDirectory() || file.getName().equals("lmntal.jar") || file.getName().equals("lmntal");
				}
			});
			pathInput = new PathInputField(chooser, Env.getMsg(MsgID.button_browse), 20);
			if (System.getenv("LMNTAL_HOME") == null) {
			    pathInput.setPathText("lmntal/bin/lmntal.jar");
			} else {
			    pathInput.setPathText(System.getenv("LMNTAL_HOME") + "/bin/lmntal");
			}

			add(pathInput);
		}

		String getInputPath()
		{
			return pathInput.getPathText();
		}
	}
}
