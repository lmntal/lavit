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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import lavit.Env;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.util.OuterRunner;

public class SlimInstaller implements OuterRunner {

	private ThreadRunner runner;
	private boolean success;

	public SlimInstaller(){
		this.runner = new ThreadRunner();
		this.success = false;
	}

	public void run() {
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

	public boolean isSuccess(){
		return success;
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

				ProcessBuilder pb;
				String str;
				String shCmd = Env.getBinaryAbsolutePath("sh")+" configure --prefix="+Env.getSpaceEscape(Env.getSlimInstallLinuxPath()+"/")+" "+Env.get("SLIM_CONFIGURE_OPTION");
				String makeCmd = Env.getBinaryAbsolutePath("make");
				String makeInstallCmd = Env.getBinaryAbsolutePath("make")+" install";

				// sh configure起動
				window.println(shCmd);

				pb = new ProcessBuilder(strList(shCmd));
				Env.setProcessEnvironment(pb.environment());
				pb.directory(new File(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfSlim()));
				pb.redirectErrorStream(true);
				p = pb.start();
				in = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while ((str=in.readLine())!=null) {
					window.println(str);
				}
				in.close();
				p.waitFor();
				//if(p.exitValue()!=0) throw new Exception("configure error.");

				window.println("configure end. exit="+p.exitValue()+".\n");


				// make起動
				window.println(makeCmd);

				pb = new ProcessBuilder(strList(makeCmd));
				Env.setProcessEnvironment(pb.environment());
				pb.directory(new File(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfSlim()));
				pb.redirectErrorStream(true);
				p = pb.start();
				in = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while ((str=in.readLine())!=null) {
					window.println(str);
				}
				in.close();
				p.waitFor();

				window.println("make end. exit="+p.exitValue()+".\n");


				// make install起動
				window.println(makeInstallCmd);

				pb = new ProcessBuilder(strList(makeInstallCmd));
				Env.setProcessEnvironment(pb.environment());
				pb.directory(new File(Env.LMNTAL_LIBRARY_DIR+File.separator+Env.getDirNameOfSlim()));
				pb.redirectErrorStream(true);
				p = pb.start();
				in = new BufferedReader(new InputStreamReader(p.getInputStream()));

				while ((str=in.readLine())!=null) {
					window.println(str);
				}
				in.close();
				p.waitFor();

				window.println("make install end. exit="+p.exitValue()+".\n");


				// slim.exe が無かったら例外
				if(!(new File(Env.getSlimInstallPath()+File.separator+"bin"+File.separator+Env.getSlimBinaryName())).exists()) throw new Exception();

				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){JOptionPane.showMessageDialog(
						window,
						Lang.w[10],
						"SLIM INSTALL",
						JOptionPane.PLAIN_MESSAGE
				);}});

				success = true;

			}catch(Exception e){

				javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){JOptionPane.showMessageDialog(
						window,
						Lang.w[11],
						"SLIM INSTALL",
						JOptionPane.PLAIN_MESSAGE
				);}});

				StringWriter sw = new StringWriter();
			    e.printStackTrace(new PrintWriter(sw));
			    window.println(sw.toString());

			}finally{

				window.exit();
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

		private String[] progressMatchString = {
				"checking for a BSD-compatible install",
				"checking for style of include used by make",
				"checking for suffix of object files",
				"checking whether we are using the GNU C++ compiler",
				"checking lex library",
				"checking for C/C++ restrict keyword",
				"checking for egrep",
				"checking for memory.h",
				"checking for unistd.h",
				"checking for int64_t",
				"checking for uint16_t",
				"checking for void*",
				"checking for strchr",
				"config.status: creating src/Makefile",
				"config.status: executing depfiles commands",
				"configure end.",
				"gcc: unrecognized option",
				"make end.",
				"Making install in doc",
				"make install end"
		};

		InstallWindow(){

			ImageIcon image = new ImageIcon(Env.getImageOfFile("img/slim_c_s.png"));

			setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			setAlwaysOnTop(true);
			//setUndecorated(true);

		    JPanel panel = new JPanel();
		    panel.setBackground(new Color(255,255,255));
			panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			add(panel);


			JLabel icon = new JLabel(image);
			icon.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(icon);


			bar = new JProgressBar(0,100);
			//bar.setStringPainted(true);
			bar.setIndeterminate(true);
			panel.add(bar);

			text = new JTextArea();
			text.setEditable(false);
			text.setLineWrap(true);
			text.setFont(new Font(null,Font.PLAIN,11));
			JScrollPane textScrollPane = new JScrollPane(text);
			textScrollPane.setPreferredSize(new Dimension(image.getIconWidth(),image.getIconHeight()/2));
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
			//progress barの処理
			for(int i=0;i<progressMatchString.length;++i){
				if(str.startsWith(progressMatchString[i])){
					bar.setIndeterminate(false);
					final int progress = (i+1)*5;
					javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
						bar.setValue(progress);
					}});
				}
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			String date = dateFormat.format(new Date());
			if(str.length()>0){ str = "["+date+"] "+str; }

			text.append(str+"\n");
			text.setCaretPosition(text.getText().length()-1);

		}

		public void actionPerformed(ActionEvent e) {
			dispose();
		}

		public void exit(){
			javax.swing.SwingUtilities.invokeLater(new Runnable(){public void run(){
				//bar.setIndeterminate(false);
				bar.setValue(100);
				button.setEnabled(true);
			}});
		}

	}


}