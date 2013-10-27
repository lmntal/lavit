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

import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class UpdateChecker
{
	public static void checkVersion(Frame owner, String versionText, String dateText, String remoteUrl)
	{
		Map<String, String> table = ColonSeparatedTableReader.loadFromURL(remoteUrl, 3);
		String remoteVersionText = table.get("version");
		String remoteDateText = table.get("release-date");
		String downloadUrl = getDefault(table, "download-url", "");
		String desc = getDefault(table, "description", "(no description)");

		VersionInfo currentVersion = VersionInfo.create(versionText, dateText);
		VersionInfo releaseVersion = VersionInfo.create(remoteVersionText, remoteDateText);
		if (currentVersion != null && releaseVersion != null)
		{
			if (releaseVersion.isNewer(currentVersion))
			{
				showDialog(owner, releaseVersion, downloadUrl, desc);
			}
			else
			{
				System.err.println("update check: this is the latest version.");
			}
		}
	}

	private static void showDialog(final Frame owner, final VersionInfo v, final String url, final String desc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				UpdateNotifierFrame frame = new UpdateNotifierFrame(owner, "Update", "Newer LaViT is now available.", v.getVersionText(), v.getDateText());
				frame.setLinkUrl(url);
				frame.setDescriptionText(desc);
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	private static String getDefault(Map<String, String> map, String key, String defval)
	{
		String value = map.get(key);
		if (value == null)
		{
			value = defval;
		}
		return value;
	}
}
