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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import lavit.*;
import lavit.system.OutputPanel;
import lavit.util.OuterRunner;

public class LmntalRunner implements OuterRunner {

	private ThreadRunner runner;
	private RunnerOutputGetter output;
	private StringBuffer buffer;

	private String option;
	private File targetFile;
	private boolean success;

	private long time;

	public LmntalRunner(String option){
		this(option,FrontEnd.mainFrame.editorPanel.getFile());
	}

	public LmntalRunner(String option,File targetFile){
		this.option = option;
		this.targetFile = targetFile;
		runner = new ThreadRunner();
		output = null;
		buffer = null;
		success = false;
	}

	public void run() {
		runner.start();
	}

	public void setOutputGetter(RunnerOutputGetter output){
		this.output = output;
	}

	public void setBuffering(boolean b){
		if(b){
			buffer = new StringBuffer();
		}else{
			buffer = null;
		}
	}

	public String getBufferString(){
		return buffer.toString();
	}

	public boolean isRunning() {
		if(runner==null) return false;
		return true;
	}

	public long getTime(){
		return time;
	}

	public void kill() {
		if (runner!=null) {
			runner.kill();
			runner.interrupt();
			runner=null;
		}
	}

	public void exit(){
		runner=null;
	}

	public boolean isSuccess(){
		return success;
	}

	private class ThreadRunner extends Thread {

		private Process p;

		public void run() {
			try {

				//計測開始
				long startTimeMillis = System.currentTimeMillis();

				//オプション
				String cmd = Env.getLmntalCmd()+option+" "+Env.getSpaceEscape(targetFile.getAbsolutePath());

				FrontEnd.println("(LMNtal) "+cmd);

				ProcessBuilder pb = new ProcessBuilder(strList(cmd));
				Env.setProcessEnvironment(pb.environment());
				//pb.redirectErrorStream(true);
				p = pb.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				ErrorStreamPrinter err = new ErrorStreamPrinter(p.getErrorStream());

				if(buffer==null){
					if(output==null){
						output = FrontEnd.mainFrame.toolTab.systemPanel.outputPanel;
					}
					output.outputStart("lmntal", option, targetFile);
					err.start();
					String str;
					while ((str=in.readLine())!=null) {
						output.outputLine(str);
					}
					err.join();
					output.outputEnd();
				}else{
					String str;
					while ((str=in.readLine())!=null) {
						buffer.append(str+"\n");
					}
				}

				in.close();
				p.waitFor();

				time = System.currentTimeMillis() - startTimeMillis;
				success = true;

			}catch(Exception e){
				FrontEnd.printException(e);

			}finally{
				exit();
			}
		}

		ArrayList<String> strList(String str){
			ArrayList<String> cmdList = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(str);
			while(st.hasMoreTokens()){
				String s = st.nextToken();
				if(s.length()>=2&&s.charAt(0)=='"'&&s.charAt(s.length()-1)=='"'){
					s = s.substring(1,s.length()-1);
				}
				cmdList.add(s);
			}
			return cmdList;
		}

		private void kill() {
			if(p!=null){
				p.destroy();
			}
		}

	}

}
