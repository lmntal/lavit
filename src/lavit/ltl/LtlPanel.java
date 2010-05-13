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

package lavit.ltl;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import lavit.Env;
import lavit.FrontEnd;


public class LtlPanel extends JPanel {

	private LtlSymbolPanel ltlSymbolPanel;
	private LtlNcPanel ltlNcPanel;
	public LtlButtonPanel ltlButtonPanel;

	public LtlPanel(){
		setLayout(new BorderLayout());

		ltlSymbolPanel = new LtlSymbolPanel();
		ltlNcPanel = new LtlNcPanel();
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,ltlSymbolPanel,ltlNcPanel);
        jsp.setResizeWeight(0.5);
		add(jsp, BorderLayout.CENTER);

		ltlButtonPanel = new LtlButtonPanel(this);
		add(ltlButtonPanel, BorderLayout.SOUTH);

	}

	public File getSymbolFile(String no){
		return new File(getFilename()+no+".psym");
	}

	public File getNcFile(String no){
		return new File(getFilename()+no+".nc");
	}

	public void saveFile(String no){

		writeFile(getSymbolFile(no),ltlSymbolPanel.getText());
		writeFile(getNcFile(no),ltlNcPanel.getText());

	}

	public void loadFile(String no){

	    ltlSymbolPanel.setText(openFile(getSymbolFile(no)));
	    ltlNcPanel.setText(openFile(getNcFile(no)));
	    ltlButtonPanel.setSelected(no);

	}

	private String getFilename(){
		File file = FrontEnd.mainFrame.editorPanel.getFile().getAbsoluteFile();
		String name = file.getName();

		//拡張子を取り除く
		int point = name.lastIndexOf(".");
	    if (point != -1) name = name.substring(0, point);

	    return file.getParent()+File.separator+name;

	}

	private String openFile(File file){
		if(!file.exists()) return "";
		try {
			String encoding = Env.get("EDITER_FILE_READ_ENCODING");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

			StringBuffer buf = new StringBuffer("");
			String strLine;
			if((strLine = reader.readLine()) != null){
				buf.append(strLine);
			}
			while ((strLine = reader.readLine()) != null) {
				buf.append("\r\n" + strLine);
			}
			reader.close();

			return buf.toString();
		}catch (Exception e) {
			FrontEnd.printException(e);
		}
		return "";
	}

	private void writeFile(File file,String str){
		try {
			String encoding = Env.get("EDITER_FILE_WRITE_ENCODING");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
			writer.write(str+"\r\n");
			writer.close();
		} catch (IOException e) {
			FrontEnd.printException(e);
		}

	}

}
