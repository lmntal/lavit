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

package lavit.stateviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JPanel;

import lavit.*;

public class StatePanel extends JPanel{

	public StateGraphPanel stateGraphPanel;
	public StateControlPanel stateControlPanel;

	private String originalString;
	private boolean ltlMode;
	private StateNodeSet drawNodes;

	public StatePanel(){
		setLayout(new BorderLayout());

		stateGraphPanel = new StateGraphPanel(this);
		add(stateGraphPanel, BorderLayout.CENTER);

		stateControlPanel = new StateControlPanel(this);
		add(stateControlPanel, BorderLayout.SOUTH);
	}

	public void start(String str,boolean ltl){

		//System.out.println("SV  _START="+System.currentTimeMillis());

		originalString = str;
		ltlMode = ltl;
		drawNodes = new StateNodeSet();
		boolean res;
		if(ltlMode){
			res = drawNodes.setSlimLtlResult(str);
		}else{
			res = drawNodes.setSlimNdResult(str);
		}

		if(res){
			FrontEnd.println("(StateViewer) start! (state = "+drawNodes.size()+")");
			stateGraphPanel.init(drawNodes);
			FrontEnd.mainFrame.toolTab.setTab("StateViewer");
		}else{
			FrontEnd.println("(StateViewer) error.");
		}

	}

	public void reset(){
		start(originalString,ltlMode);
	}

	public void savaFile(File file){
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath())));
			writer.write(originalString);
			writer.close();
			FrontEnd.println("(StateViewer) save "+file.getName()+" ]");
		} catch (IOException e) {
			FrontEnd.printException(e);
		}
	}

	public void loadFile(File file){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringBuffer buf = new StringBuffer("");
			String strLine;
			while ((strLine = reader.readLine()) != null) {
				buf.append("\n" + strLine);
			}
			reader.close();
			FrontEnd.println("(StateViewer) load "+file.getName()+" ]");
			start(buf.toString(),false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isLtl(){
		return ltlMode;
	}

}
