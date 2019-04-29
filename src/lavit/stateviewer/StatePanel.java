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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.controller.StateControlPanel;
import lavit.stateviewer.s3d.State3DPanel;

public class StatePanel extends JPanel {
	public StateGraphPanel stateGraphPanel;
	public State3DPanel state3DPanel = null;
	public boolean view3D = false;
	public boolean created3D = false;

	public StateControlPanel stateControlPanel;

	private String originalString;
	private boolean ltlMode;
	private StateNodeSet drawNodes;

	public StatePanel() {

		setLayout(new BorderLayout());

		stateGraphPanel = new StateGraphPanel(this);
		add(stateGraphPanel, BorderLayout.CENTER);

		stateControlPanel = new StateControlPanel(this);
		add(stateControlPanel, BorderLayout.SOUTH);

		/*
		 * setLayout(new BorderLayout());
		 *
		 * stateGraphPanel = new StateGraphPanel(this); stateControlPanel = new
		 * StateControlPanel(this);
		 *
		 * JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stateGraphPanel,
		 * stateControlPanel); jsp.setOneTouchExpandable(true);
		 * jsp.setResizeWeight(0.5); jsp.setDividerLocation(0.9);
		 *
		 * add(jsp, BorderLayout.CENTER);
		 */
	}

	public void start(String str, boolean ltlMode) {

		this.originalString = str;
		this.ltlMode = ltlMode;
		this.drawNodes = new StateNodeSet(stateGraphPanel);

		FrontEnd.println("(StateViewer) parsing.");
		boolean res = false;
		try {
			res = drawNodes.setSlimResult(str, ltlMode);
		} catch (NumberFormatException e) {
			FrontEnd.printException(e);
		}

		if (res) {
			FrontEnd.println("(StateViewer) start! (state = " + drawNodes.size() + ")");
			stateGraphPanel.init(drawNodes);
			stateGraphPanel.getDraw().setCycleMode(ltlMode);
			FrontEnd.mainFrame.toolTab.setTab("StateViewer");

			created3D = false;
			if (Env.is("SV3D")) {
				start3D();
				stateControlPanel.stateControllerTab.setTab("3D");
			}
		} else {
			FrontEnd.println("(StateViewer) error.");
		}

	}

	public void reset() {
		start(originalString, ltlMode);
	}

	public void savaFile(File file) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath())));
			writer.write(originalString);
			writer.close();
			FrontEnd.println("(StateViewer) save [ " + file.getName() + " ]");
		} catch (IOException e) {
			FrontEnd.printException(e);
		}
	}

	public void loadFile(File file) {
		try {
			FrontEnd.println("(StateViewer) load [ " + file.getName() + " ]");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringBuffer buf = new StringBuffer("");
			String strLine;
			while ((strLine = reader.readLine()) != null) {
				buf.append("\n" + strLine);
			}
			reader.close();

			String str = buf.toString();
			if (str.indexOf("no cycles found") >= 0) {
				start(str, true);
			} else {
				start(str, false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void loadFile() {
		final File file = chooseOpenFile();
		if (file != null) {
			(new Thread(new Runnable() {
				public void run() {
					loadFile(file);
				}
			})).start();
		}
	}

	private File chooseOpenFile() {
		String chooser_dir = Env.get("SV_FILE_LAST_CHOOSER_DIR");
		if (chooser_dir == null) {
			chooser_dir = new File("demo").getAbsolutePath();
		} else if (!new File(chooser_dir).exists() && new File("demo").exists()) {
			chooser_dir = new File("demo").getAbsolutePath();
		}
		JFileChooser jfc = new JFileChooser(chooser_dir);
		int r = jfc.showOpenDialog(FrontEnd.mainFrame);
		if (r != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = jfc.getSelectedFile();
		Env.set("SV_FILE_LAST_CHOOSER_DIR", file.getParent());
		return file;
	}

	public boolean isLtl() {
		return ltlMode;
	}

	public void toggle3D() {
		if (view3D) {
			stop3D();
		} else {
			start3D();
		}
	}

	public void start3D() {
		if (state3DPanel == null) {
			state3DPanel = new State3DPanel(this);
		}
		if (!created3D) {
			state3DPanel.createGraph();
			created3D = true;
		}

		remove(stateGraphPanel);
		add(state3DPanel, BorderLayout.CENTER);
		revalidate();

		view3D = true;
	}

	public void stop3D() {
		remove(state3DPanel);
		add(stateGraphPanel, BorderLayout.CENTER);
		revalidate();

		view3D = false;
	}

}
