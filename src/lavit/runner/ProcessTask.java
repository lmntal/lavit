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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lavit.Env;
import lavit.util.StringUtils;

/**
 * @author Yuuki.S
 */
public class ProcessTask
{
	private static int taskid = 0;

	private int id;
	private ProcessBuilder pb;
	private Process p;
	private String stdinData;
	private StreamReaderThread stdoutReader;
	private StreamReaderThread stderrReader;
	private PrintLineListener stdoutListener;
	private PrintLineListener stderrListener;
	private MonitorThread monitorThread;
	private long startTime;
	private long elapsedTime;
	private boolean terminated;
	private boolean aborted;
	private List<ProcessFinishListener> finishListeners = new ArrayList<ProcessFinishListener>();

	public ProcessTask(String ... commands)
	{
		this(Arrays.asList(commands));
	}

	public ProcessTask(List<String> commands)
	{
		pb = new ProcessBuilder(commands);
		Env.setProcessEnvironment(pb.environment());
		id = createID();
	}

	public int getTaskID()
	{
		return id;
	}

	public double getElapsedSeconds()
	{
		return elapsedTime / 1000.0;
	}

	public void setDirectory(String dirPath)
	{
		pb.directory(new File(dirPath).getAbsoluteFile());
	}

	public void setStandardInputData(String data)
	{
		stdinData = data;
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
		synchronized (finishListeners)
		{
			finishListeners.add(l);
		}
	}

	public void removeProcessFinishListener(ProcessFinishListener l)
	{
		synchronized (finishListeners)
		{
			finishListeners.remove(l);
		}
	}

	private void dispatchProcessFinishEvent(int exitCode)
	{
		synchronized (finishListeners)
		{
			for (ProcessFinishListener l : finishListeners)
			{
				l.processFinished(id, exitCode, aborted);
			}
		}
	}

	public synchronized boolean isTerminated()
	{
		return terminated;
	}

	private synchronized void setTerminated(boolean b)
	{
		terminated = b;
	}

	public boolean execute()
	{
		startTime = System.currentTimeMillis();

		try
		{
			p = pb.start();

			OutputStream os = p.getOutputStream();
			try
			{
				writeInputData(os);
				os.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			stdoutReader = new StreamReaderThread(p.getInputStream());
			stderrReader = new StreamReaderThread(p.getErrorStream());

			stdoutReader.setPrintLineListener(stdoutListener);
			stderrReader.setPrintLineListener(stderrListener);

			stdoutReader.start();
			stderrReader.start();

			monitorThread = new MonitorThread();
			monitorThread.setDaemon(true);
			monitorThread.start();

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

	private void writeInputData(OutputStream os) throws IOException
	{
		if (!StringUtils.nullOrEmpty(stdinData))
		{
			os.write(stdinData.getBytes());
			os.flush();
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

	public static ProcessTask createProcessTask(String path, List<String> args)
	{
		List<String> commands = new ArrayList<String>();
		commands.add(path);
		commands.addAll(args);
		return new ProcessTask(commands);
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

	private static synchronized int createID()
	{
		return taskid++;
	}

	private class MonitorThread extends Thread
	{
		public MonitorThread()
		{
			setName("MonitorThread");
		}

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
				waitReaderThreads();
				closeStreams();
				setTerminated(true);
				elapsedTime = System.currentTimeMillis() - startTime;
				dispatchProcessFinishEvent(p.exitValue());
			}
		}
	}
}
