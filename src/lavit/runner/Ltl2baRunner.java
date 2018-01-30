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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import lavit.*;
import lavit.util.OuterRunner;

public class Ltl2baRunner implements OuterRunner {

	private ThreadRunner runner;
	private StringBuffer buffer;

	private String ltl;
	private boolean success;

	public Ltl2baRunner(String ltl){
		this.ltl = ltl.trim();
		this.runner = new ThreadRunner();
		this.buffer = new StringBuffer();
		this.success = false;
	}

	public void run() {
		runner.start();
	}

	public String getOutput(){
		return buffer.toString();
	}

	public boolean isRunning() {
		if(runner==null) return false;
		return true;
	}

	public void exit(){
		runner=null;
	}

	public boolean isSucceeded(){
		return success;
	}

	public void kill() {
		if (runner!=null) {
			runner.interrupt();
			runner=null;
		}
	}

	private class ThreadRunner extends Thread {

		private Process p;

		public void run() {
			try {

				ArrayList<String> cmdlist = new ArrayList<String>();
				cmdlist.add(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfLtl2ba()+File.separator+Env.getLtl2baBinaryName());
				cmdlist.add("-f");
				cmdlist.add("!("+ltl+")");

				ProcessBuilder pb = new ProcessBuilder(cmdlist);
				Env.setProcessEnvironment(pb.environment());
				pb.redirectErrorStream(true);
				p = pb.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

				String str;
				while ((str=in.readLine())!=null) {
					buffer.append(str+"\n");
				}

				in.close();
				p.waitFor();

				success = true;

			}catch(Exception e){

				StringWriter sw = new StringWriter();
			    e.printStackTrace(new PrintWriter(sw));
			    buffer.append(sw.toString());

			}finally{
				exit();
			}
		}

	}

}
