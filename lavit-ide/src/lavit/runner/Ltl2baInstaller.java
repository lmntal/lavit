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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lavit.Env;
import lavit.FrontEnd;
import lavit.frame.ChildWindowListener;
import lavit.localizedtext.MsgID;
import lavit.util.OuterRunner;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public class Ltl2baInstaller implements OuterRunner  {

	private ThreadRunner runner;
	private boolean success;
	private String tarDir = Env.LMNTAL_LIBRARY_DIR+File.separator;

	public Ltl2baInstaller(){
		this.runner = null;
		this.success = false;
	}

	public boolean isNeedInstall(){
		File exe = new File(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfLtl2ba()+File.separator+Env.getLtl2baBinaryName());
		return !exe.exists();
	}

	public boolean isInstallable(){
		return dirFile().exists()||tarFile().exists();
	}

	File tarFile(){
		return new File(tarDir+Env.get("FILE_NAME_LTL2BA_PACK"));
	}

	File dirFile(){
		return new File(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfLtl2ba());
	}

	public void run() {
		runner = new ThreadRunner();
		runner.start();
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

	public boolean isSucceeded(){
		return success;
	}

	public ArrayList<String> unTar() throws IOException{

		ArrayList<String> names = new ArrayList<String>();
		TarInputStream tar = new TarInputStream(new GZIPInputStream(new FileInputStream(tarFile())));
		TarEntry tarEnt = tar.getNextEntry();
		while (tarEnt != null) {

			String name = tarEnt.getName();
			names.add(name);

			if(name.endsWith("/")){
				(new File(tarDir+name)).mkdir();
			}else{
				ByteArrayOutputStream bos = new ByteArrayOutputStream((int)tarEnt.getSize());
				tar.copyEntryContents(bos);
				InputStream in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
				FileOutputStream out = new FileOutputStream(tarDir+name);
				int size;
				byte bytes[] = new byte[1024];
				while((size = in.read(bytes)) >= 0) {
					out.write(bytes,0,size);
				}
				out.close();
				in.close();
			}
			tarEnt = tar.getNextEntry();
		}
		tar.close();
		return names;
	}

	private class ThreadRunner extends Thread {
		private Process p;
		private BufferedReader in;
		private InstallWindow window;

		ThreadRunner(){
			window = new InstallWindow();
			//new JDialog(window);
		}

		public void run() {
			try {

				if(tarFile().exists()){
					if(dirFile().exists()) dirFile().delete();
					ArrayList<String> names = unTar();
					window.println("unpacking: "+tarFile().getName());
					for(String name : names){
						window.println(name);
					}
				}

				ProcessBuilder pb;
				String str;
				String makeCmd = Env.getBinaryAbsolutePath("make");

				// make起動
				window.println(makeCmd);

				pb = new ProcessBuilder(strList(makeCmd));
				Env.setProcessEnvironment(pb.environment());
				pb.directory(new File(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfLtl2ba()));
				pb.redirectErrorStream(true);
				p = pb.start();
				in = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while ((str=in.readLine())!=null) {
					window.println(str);
				}
				in.close();
				p.waitFor();

				window.println("make end. exit="+p.exitValue()+".\n");

				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){JOptionPane.showMessageDialog(
						FrontEnd.mainFrame,
						Env.getMsg(MsgID.text_install_completed),
						"LTL2BA INSTALL",
						JOptionPane.PLAIN_MESSAGE
				);}});

				window.dispose();
				success = true;

			}catch(Exception e){

				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){JOptionPane.showMessageDialog(
						FrontEnd.mainFrame,
						Env.getMsg(MsgID.text_install_failed),
						"LTL2BA INSTALL",
						JOptionPane.PLAIN_MESSAGE
				);}});

				StringWriter sw = new StringWriter();
			    e.printStackTrace(new PrintWriter(sw));
			    window.println(sw.toString());
			    window.exit();

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
			if(p!=null) p.destroy();
		}

	}

	private class InstallWindow extends JFrame implements ActionListener{

		private JProgressBar bar;
		private JTextArea text;
		private JButton button;

		InstallWindow(){

			setIconImages(Env.getApplicationIcons());
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setAlwaysOnTop(true);
			//setUndecorated(true);

		    JPanel panel = new JPanel();
		    panel.setBackground(new Color(255,255,255));
			panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			add(panel);


			bar = new JProgressBar();
			bar.setIndeterminate(true);
			panel.add(bar);

			text = new JTextArea();
			text.setEditable(false);
			text.setLineWrap(true);
			text.setFont(new Font(null,Font.PLAIN,11));
			JScrollPane textScrollPane = new JScrollPane(text);
			textScrollPane.setPreferredSize(new Dimension(200,100));
			panel.add(textScrollPane);

			button = new JButton("OK");
			button.addActionListener(this);
			button.setEnabled(false);
			button.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(button);

			addWindowListener(new ChildWindowListener(this));

			pack();
			setLocationRelativeTo(null);
		    setVisible(true);

		}

		private void println(String str){
			text.append(str+"\n");
			text.setCaretPosition(text.getText().length()-1);
		}

		public void actionPerformed(ActionEvent e) {
			dispose();
		}

		public void exit(){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
				bar.setIndeterminate(false);
				button.setEnabled(true);
			}});
		}

	}


}