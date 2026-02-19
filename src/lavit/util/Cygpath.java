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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lavit.Env;
import lavit.runner.PrintLineListener;
import lavit.runner.StreamReaderThread;

public final class Cygpath
{
	private Cygpath() { }

	public static String toLinuxStyle(String path)
	{
		return execCygpath("--unix", path);
	}

	public static String toWindowsStyle(String path)
	{
		return execCygpath("--windows", path);
	}

	private static String execCygpath(String option, String path)
	{
		String cygpath = Env.getBinaryAbsolutePath("cygpath");
		String translatedPath = "";

		try
		{
			Process process = Runtime.getRuntime().exec(cygpath + " " + option + " \"" + path + "\"");
			process.getOutputStream().close();

			InputStream stderr = process.getErrorStream();
			StreamReaderThread errReader = new StreamReaderThread(stderr);
			errReader.setPrintLineListener(new PrintLineListener()
			{
				public void println(String line)
				{
					System.err.println("cygpath: " + line);
				}
			});
			errReader.start();

			InputStream stdout = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			translatedPath = reader.readLine();

			try
			{
				process.waitFor();
				errReader.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			stdout.close();
			stderr.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return translatedPath;
	}
}
