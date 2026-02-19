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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ColonSeparatedTableReader
{
	private ColonSeparatedTableReader() { }

	public static Map<String, String> loadFromFile(String fileName)
	{
		Map<String, String> table = new HashMap<String, String>();
		loadFromFile(table, new File(fileName));
		return table;
	}

	public static Map<String, String> loadFromURL(String url, int timeoutSec)
	{
		Map<String, String> table = new HashMap<String, String>();
		try
		{
			loadFromURL(table, new URL(url), timeoutSec);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		return table;
	}

	public static void loadFromFile(Map<String, String> result, File file)
	{
		try
		{
			InputStream is = new FileInputStream(file);
			read(result, is);
			is.close();
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

	public static void loadFromURL(Map<String, String> result, URL url, int timeoutSec)
	{
		System.err.println("update check: connect - " + url);
		try
		{
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(timeoutSec * 1000);
			connection.connect();
			System.err.println("update check: connected.");
			InputStream is = connection.getInputStream();
			read(result, is);
			is.close();
		}
		catch (IOException e)
		{
			System.err.println("update check: connection failed - " + e.getMessage());
		}
	}

	public static void read(Map<String, String> result, InputStream is)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] kv = line.trim().split("\\s*:\\s*", 2);
				if (kv.length == 2)
				{
					result.put(kv[0], kv[1].replace("\\n", "\n"));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
