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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lavit.util.FileUtils;
import lavit.util.StringUtils;

public class UpdateChecker
{
	public static void checkVersion(final Frame owner, String versionText, String dateText, String remoteUrl)
	{
		Map<String, String> table = ColonSeparatedTableReader.loadFromURL(remoteUrl, 3);
		String remoteVersionText = table.get("version");
		String remoteDateText = table.get("release-date");

		final VersionInfo currentVersion = VersionInfo.create(versionText, dateText);
		final VersionInfo releaseVersion = VersionInfo.create(remoteVersionText, remoteDateText);
		if (currentVersion != null && releaseVersion != null)
		{
			if (releaseVersion.isNewer(currentVersion))
			{
				final String downloadUrl = getDefault(table, "download-url", "");
				final String desc = getDefault(table, "description", "");
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						showDialog(owner, releaseVersion, downloadUrl, desc);
					}
				});
			}
		}
	}

	private static void showDialog(Frame owner, VersionInfo v, String url, String desc)
	{
		if (StringUtils.nullOrEmpty(desc))
		{
			desc = "(no description)";
		}

		UpdateNotifierFrame frame = new UpdateNotifierFrame(owner, "Update", "Newer LaViT is now available.", v.getVersionText(), v.getDateText());
		frame.setDescriptionText(desc);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		if (frame.isApproved())
		{
			if (!FileUtils.exists("updater.jar"))
			{
				extractUpdater();
			}
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "updater.jar", url);
			try
			{
				pb.start();
				System.exit(0);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
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

	private static void extractUpdater()
	{
		BufferedInputStream is = new BufferedInputStream(UpdateChecker.class.getResourceAsStream("updater.jar"));
		try
		{
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream("updater.jar"));
			byte[] buf = new byte[4096];
			int n;
			while ((n = is.read(buf)) != -1)
			{
				os.write(buf, 0, n);
			}
			is.close();
			os.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
