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

package lavit.runner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import lavit.FrontEnd;

public class ILRunner
{
	private String option = "";
	private Process process;
	private PrintLineListener stdoutListener;
	private PrintLineListener stderrListener;
	private StreamReaderThread stdoutReader;
	private StreamReaderThread stderrReader;
	private Thread thread;

	public ILRunner(String option)
	{
		this.option = option;
	}

	public void setStdoutListener(PrintLineListener listener)
	{
		stdoutListener = listener;
	}

	public void setStderrListener(PrintLineListener listener)
	{
		stderrListener = listener;
	}

	public void exec(final CharSequence cs)
	{
		thread = new Thread()
		{
			public void run()
			{
				try
				{
					ProcessBuilder pb = new ProcessBuilder("java", "-jar", "lmntal/bin/lmntal.jar", "--stdin-tal", option);
					process = pb.start();

					BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
					stdin.append(cs);
					stdin.close();

					stdoutReader = new StreamReaderThread(process.getInputStream());
					stdoutReader.setPrintLineListener(stdoutListener);
					stderrReader = new StreamReaderThread(process.getErrorStream());
					stderrReader.setPrintLineListener(stderrListener);

					stdoutReader.start();
					stderrReader.start();

					stdoutReader.join();
					stderrReader.join();

					process.waitFor();
					FrontEnd.mainFrame.toolTab.systemPanel.outputPanel.outputEnd();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	public void kill()
	{
		if (process != null)
		{
			try
			{
				process.getInputStream().close();
				process.getErrorStream().close();
				process.getOutputStream().close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			process.destroy();
		}
	}
}
