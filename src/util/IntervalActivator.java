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

package util;

public class IntervalActivator
{
	private final Object lock = new Object();

	private Thread thread;
	private Runnable action;
	private long interval;
	private volatile long startTime;
	private volatile boolean activated;

	public IntervalActivator(Runnable action, long interval)
	{
		this.action = action;
		this.interval = interval;
	}

	public void activate()
	{
		synchronized (lock)
		{
			startTime = System.currentTimeMillis();
			if (!activated)
			{
				activated = true;
				lock.notifyAll();
			}
		}
	}

	public void startMonitoring()
	{
		if (thread == null)
		{
			thread = new MonitorThread();
			thread.setDaemon(true);
			thread.start();
		}
		else
		{
			throw new IllegalStateException("Activator has been already started.");
		}
	}

	public void stopMonitoring()
	{
		if (thread != null)
		{
			thread.interrupt();
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class MonitorThread extends Thread
	{
		public void run()
		{
			try
			{
				while (!isInterrupted())
				{
					synchronized (lock)
					{
						activated = false;
						while (!activated)
						{
							lock.wait();
						}
					}
					while (System.currentTimeMillis() - startTime < interval)
					{
						sleep(60);
					}
					action.run();
				}
			}
			catch (InterruptedException e)
			{
			}
		}
	}
}
