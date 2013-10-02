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

package lavit.system.versioncheck;

import java.awt.Component;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class UpdateChecker
{
	public static void checkVersion(Component parent, String versionText, String dateText, String remoteUrl)
	{
		Map<String, String> table = ColonSeparatedTableReader.loadFromURL(remoteUrl, 3);
		String remoteVersionText = table.get("version");
		String remoteDateText = table.get("release-date");
		VersionInfo currentVersion = VersionInfo.create(versionText, dateText);
		VersionInfo releaseVersion = VersionInfo.create(remoteVersionText, remoteDateText);
		if (currentVersion != null && releaseVersion != null)
		{
			if (releaseVersion.isNewer(currentVersion))
			{
				showDialog(parent, releaseVersion);
			}
			else
			{
				System.err.println("update check: this is the latest version.");
			}
		}
	}

	private static void showDialog(final Component parent, VersionInfo v)
	{
		final String msg = "Newer version " + v.getVersionText() + " (" + v.getDateText() + ") is available.";
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JOptionPane.showMessageDialog(parent, msg, "New version", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
