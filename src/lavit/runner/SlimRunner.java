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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import lavit.*;
import lavit.system.OutputPanel;

public class SlimRunner {

	private ThreadRunner runner;
	private RunnerOutputGetter output;
	private StringBuffer buffer;

	private String option;
	private File targetFile;
	private File symbolFile;
	private File ncFile;
	private boolean success;

	private boolean quiet;

	public SlimRunner(String option){
		this(option,FrontEnd.mainFrame.editorPanel.getFile());
	}

	public SlimRunner(String option,File targetFile){
		this.runner = new ThreadRunner();
		this.output = null;
		this.buffer = null;

		this.option = option;
		this.targetFile = targetFile;
		this.symbolFile = null;
		this.ncFile = null;
		this.success = false;
		this.quiet = false;
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

	public void setSymbolFile(File symbolFile){
		this.symbolFile = symbolFile;
	}

	public void setNcFile(File ncFile){
		this.ncFile = ncFile;
	}

	public void setQuiet(boolean quiet){
		this.quiet = quiet;
	}

	public String getBufferString(){
		return buffer.toString();
	}

	public boolean isRunning() {
		if(runner==null) return false;
		return true;
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
		private Process p1;
		private Process p2;
		private String slim_path;

		ThreadRunner(){
			slim_path = Env.get("SLIM_EXE_PATH");
			if(slim_path==null||slim_path.equals("")){ slim_path = Env.LMNTAL_LIBRARY_DIR+File.separator+"bin"+File.separator+Env.getSlimBinaryName(); }
		}

		public void run() {
			try {

				//System.out.println("SLIM_START="+System.currentTimeMillis());

				// LMNtal起動
				String cmd1 = Env.getLmntalCmd()+" "+Env.get("SLIM_LMNTAL_COMPILE_OPTION")+" "+Env.getSpaceEscape(targetFile.getAbsolutePath());

				if(!quiet) FrontEnd.println("(SLIM) "+cmd1);

				ProcessBuilder pb = new ProcessBuilder(strList(cmd1));
				pb.directory(new File("."));
				Env.setProcessEnvironment(pb.environment());
				//pb.redirectErrorStream(redirectErrorStream);
				p1 = pb.start();
				BufferedInputStream in1 = new BufferedInputStream(p1.getInputStream());
				ErrorStreamPrinter err1 = new ErrorStreamPrinter(p1.getErrorStream());
				err1.start();

				// SLIM起動
				//String cmd2 = slim_path+" -Ilmntal"+File.separator+Env.getDirNameOfSlim()+File.separator+"lib"+File.separator+" "+option;
				//if(Env.is("SLIM_USE_LIBRARY")){
				//	cmd2 += " -I"+Env.getSlimInstallLibraryPath();
				//}
				String cmd2 = slim_path+" "+option;
				String view_option = option;

				if(symbolFile!=null){
					cmd2 += " --psym "+Env.getSpaceEscape(Env.getLinuxStylePath(symbolFile.getAbsolutePath()))+" ";
					view_option += " --psym "+symbolFile.getName();
				}
				if(ncFile!=null){
					cmd2 += " --nc "+Env.getSpaceEscape(Env.getLinuxStylePath(ncFile.getAbsolutePath()))+" ";
					view_option += " --nc "+ncFile.getName();
				}
				cmd2 += " -";
				if(!quiet) FrontEnd.println("(SLIM) "+cmd2);

				pb = new ProcessBuilder(strList(cmd2));
				pb.directory(new File("."));
				Env.setProcessEnvironment(pb.environment());
				p2 = pb.start();
				OutputStreamWriter out2 = new OutputStreamWriter(p2.getOutputStream());
				BufferedReader in2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
				ErrorStreamPrinter err2 = new ErrorStreamPrinter(p2.getErrorStream());

				// SLIMへ流し込む
				int b;
				while ((b = in1.read()) != -1) {
					out2.write(b);
				}
				try {
					out2.flush();
				}catch(Exception e){ if(!quiet) FrontEnd.printException(e); } //標準入力を待たずにSLIMが終了した場合

				out2.close();
				in1.close();
				err1.join();
				p1.waitFor();

				// SLIMの出力を得る
				if(buffer==null){
					if(output==null){
						output = FrontEnd.mainFrame.toolTab.systemPanel.outputPanel;
					}
					output.outputStart("slim", view_option, targetFile);
					err2.start();
					String str;
					while ((str=in2.readLine())!=null) {
						output.outputLine(str);
					}
					err2.join();
					output.outputEnd();
				}else{
					if(output==null){
						output = FrontEnd.mainFrame.toolTab.systemPanel.outputPanel;
					}
					err2.start();
					String str;
					while ((str=in2.readLine())!=null) {
						buffer.append(str+"\n");
					}
					err2.join();
				}

				in2.close();
				p2.waitFor();

				success = true;

				//System.out.println("SLIM_END  ="+System.currentTimeMillis());

				/*
				InputStream in2 = p2.getInputStream();
				output = FrontEnd.mainFrame.toolTab.systemPanel.outputPanel;
				output.outputStart("slim", option, targetFile);
				int str = 0;
				//while ((str=in2.read())!=-1) {
				//	output.outputLine(String.valueOf(str));
				//}
				while(true){
					output.outputLine(String.valueOf(in2.available()));
					if(str++>100){ break; }
					Thread.sleep(250);
				}
				output.outputEnd();
				*/


				/*
				String cmd1 = "java -DLMNTAL_HOME=lmntal -classpath lmntal"+File.separator+"bin"+File.separator+"lmntal.jar runtime.FrontEnd -O2 --interpret --slimcode \""+targetFile.getAbsolutePath()+"\"";
				FrontEnd.println("(SLIM) "+cmd1);

				ProcessBuilder pb = new ProcessBuilder(strList(cmd1));
				pb.redirectErrorStream(true);
				p1 = pb.start();
				BufferedInputStream in1 = new BufferedInputStream(p1.getInputStream());

				FileOutputStream fos = new FileOutputStream(targetFile.getAbsolutePath()+".slimcode");
				int b = -1;
				while ((b = in1.read()) != -1) {
					fos.write(b);
				}
				fos.flush();
				fos.close();
				in1.close();
				p1.waitFor();

				String cmd2 = Env.get("SLIM_EXE_PATH")+" "+option+" \""+targetFile.getAbsolutePath()+".slimcode\"";
				FrontEnd.println("(SLIM) "+cmd2);

				pb = new ProcessBuilder(strList(cmd2));
				pb.redirectErrorStream(true);
				p2 = pb.start();

				BufferedReader in2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
				if(buffer==null){
					if(output==null){
						output = FrontEnd.mainFrame.toolTab.systemPanel.outputPanel;
					}
					output.outputStart("slim", option, targetFile);
					String str;
					while ((str=in2.readLine())!=null) {
						output.outputLine(str);
					}
					output.outputEnd();
				}else{
					String str;
					while ((str=in2.readLine())!=null) {
						buffer.append(str+"\n");
					}
				}

				in2.close();
				p2.waitFor();

				exit();
				success = true;

				*/

			}catch(Exception e){
				if(!quiet) FrontEnd.printException(e);

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
			if(p1!=null) p1.destroy();
			if(p2!=null) p2.destroy();
		}

	}

	static public boolean checkRun(){
		File f = new File("temp.lmn");
		try {
			FileWriter fp = new FileWriter(f);
			fp.write("slimruncheckatom.");
            fp.close();
		} catch (IOException e) {}

		final SlimRunner slimRunner = new SlimRunner("",f);
		slimRunner.setBuffering(true);
		slimRunner.setQuiet(true);
		slimRunner.run();

		int count = 0;
		while(slimRunner.isRunning()){
			FrontEnd.sleep(200);
			if(count++>10){
				slimRunner.kill();
				return false;
			}
		}
		if(slimRunner.getBufferString().indexOf("slimruncheckatom")>=0){
			return true;
		}else{
			return false;
		}
	}

}
