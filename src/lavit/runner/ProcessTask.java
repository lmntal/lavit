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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lavit.Env;

/**
 * TODO: 起動プロセスの標準入力への書き込みに対応する。
 */
public class ProcessTask
{
	private static int taskid = 0;

	private int id;
	private ProcessBuilder pb;
	private Process p;
	private StreamReaderThread stdoutReader;
	private StreamReaderThread stderrReader;
	private PrintLineListener stdoutListener;
	private PrintLineListener stderrListener;
	private MonitorThread monitorThread;
	private boolean terminated;
	private boolean aborted;
	private List<ProcessFinishListener> finishListeners = new ArrayList<ProcessFinishListener>();

	public ProcessTask(List<String> commands)
	{
		pb = new ProcessBuilder(commands);
		Env.setProcessEnvironment(pb.environment());
		id = createID();
	}

	public void setDirectory(String dirPath)
	{
		pb.directory(new File(dirPath).getAbsoluteFile());
	}

	public void setStandardOutputListener(PrintLineListener l)
	{
		stdoutListener = l;
	}

	public void setStandardErrorListener(PrintLineListener l)
	{
		stderrListener = l;
	}

	public void addProcessFinishListener(ProcessFinishListener l)
	{
		finishListeners.add(l);
	}

	public void removeProcessFinishListener(ProcessFinishListener l)
	{
		finishListeners.remove(l);
	}

	private void deliverProcessFinishEvent(int exitCode)
	{
		for (ProcessFinishListener l : finishListeners)
		{
			l.processFinished(id, exitCode, aborted);
		}
	}

	public boolean isTerminated()
	{
		return terminated;
	}

	public boolean execute()
	{
		try
		{
			p = pb.start();
			monitorThread = new MonitorThread();
			monitorThread.setDaemon(true);
			monitorThread.start();

			p.getOutputStream().close();

			stdoutReader = new StreamReaderThread(p.getInputStream());
			stderrReader = new StreamReaderThread(p.getErrorStream());

			stdoutReader.setPrintLineListener(stdoutListener);
			stderrReader.setPrintLineListener(stderrListener);

			stdoutReader.start();
			stderrReader.start();

			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void abort()
	{
		if (p == null || isTerminated())
		{
			return;
		}

		aborted = true;
		p.destroy();
		monitorThread.interrupt();
		try
		{
			monitorThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void closeStreams()
	{
		try
		{
			p.getOutputStream().close();
			p.getInputStream().close();
			p.getErrorStream().close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void waitReaderThreads()
	{
		try
		{
			stdoutReader.join();
			stderrReader.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static ProcessTask createJarProcessTask(String jarName, List<String> args)
	{
		List<String> commands = new ArrayList<String>();
		commands.add("java");
		commands.add("-jar");
		commands.add(jarName);
		commands.addAll(args);
		return new ProcessTask(commands);
	}

	private static int createID()
	{
		return taskid++;
	}

	private class MonitorThread extends Thread
	{
		public void run()
		{
			try
			{
				p.waitFor();
			}
			catch (InterruptedException e)
			{
			}
			finally
			{
				closeStreams();
				waitReaderThreads();
				terminated = true;
				deliverProcessFinishEvent(p.exitValue());
			}
		}
	}
}
