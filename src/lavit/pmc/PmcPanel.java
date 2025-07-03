/*
 *   Copyright (c) 2025, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
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

package lavit.pmc;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import lavit.Env;
import lavit.FrontEnd;
import lavit.runner.ProbabilisticTranslatorRunner;
import lavit.util.FileUtils;

// TODO: 4. predicates.pctl 等の作成
// パネル内で，PCTL 等を入力できるようにする．
// predicates.pctl に保存する．
// その他，ラベル付けファイルや，状態への報酬設定など．

// TODO: 5. PRISM 実行
// e.g. prism -importmodel '<model_name>_dtmc.tra,srew' predicates.pctl
// 実行結果を表示する．

// TODO: 6. State Viewer
// StateViewer 上で各遷移について実際に割り当てられる確率を表示できるようにする．

public class PmcPanel extends JPanel {
  private File targetLMNtalFile;
  private File targetSlimDumpFile;
  private File targetWeightsFile;
  private File targetPredicatesFile;
  private File targetTransitionFile;
  private File targetLabelsFile;
  private File targetStateRewardsFile;

  private String targetBasePath;

  private SlimButtonPanel slimButtonPanel;
  private WeightsInputPanel weightsInputPanel;
  private PrismInputPanel prismInputPanel;
  private PmcButtonPanel pmcButtonPanel;

  private ProbabilisticTranslatorRunner translatorRunner;

  public PmcPanel() {
    setLayout(new BorderLayout());

    slimButtonPanel = new SlimButtonPanel(this);
    weightsInputPanel = new WeightsInputPanel(this);
    prismInputPanel = new PrismInputPanel(this);
    pmcButtonPanel = new PmcButtonPanel(this);

    add(slimButtonPanel, BorderLayout.NORTH);

    JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, weightsInputPanel, prismInputPanel);
    jsp.setResizeWeight(0.2);
    add(jsp, BorderLayout.CENTER);

    add(pmcButtonPanel, BorderLayout.SOUTH);
  }

  public void setTargetLMNtalFile(File lmntalFile) {
    targetLMNtalFile = lmntalFile;
    if( targetLMNtalFile != null) {
      targetBasePath = FileUtils.removeExtension(targetLMNtalFile.getAbsolutePath());
    }
    setInputFiles();
  }

  public void setInputFiles() {
    if(targetLMNtalFile != null) {
      targetWeightsFile = new File(targetBasePath + "_weights.json");
      targetPredicatesFile = new File(targetBasePath + "_dtmc.pctl");
      targetTransitionFile = new File(targetBasePath + "_dtmc.tra");
      targetLabelsFile = new File(targetBasePath + "_dtmc.lab");
      targetStateRewardsFile = new File(targetBasePath + "_dtmc.srew");
    }
  }

  public void loadInputFiles() {  
    if (targetWeightsFile != null && targetWeightsFile.exists()) {
      weightsInputPanel.setWeightsText(openFile(targetWeightsFile));
    } else {
      weightsInputPanel.setWeightsText("");
    }

    if (targetPredicatesFile != null && targetPredicatesFile.exists()) {
      prismInputPanel.setPredicatesText(openFile(targetPredicatesFile));
    } else {
      prismInputPanel.setPredicatesText("");
    }

    if (targetTransitionFile != null && targetTransitionFile.exists()) {
      prismInputPanel.setTransitionText(openFile(targetTransitionFile));
    } else {
      prismInputPanel.setTransitionText("");
    }

    if (targetLabelsFile != null && targetLabelsFile.exists()) {
      prismInputPanel.setLabelsText(openFile(targetLabelsFile));
    } else {
      prismInputPanel.setLabelsText("");
    }

    if (targetStateRewardsFile != null && targetStateRewardsFile.exists()) {
      prismInputPanel.setStateRewardsText(openFile(targetStateRewardsFile));
    } else {
      prismInputPanel.setStateRewardsText("");
    }
  }

  public void unloadInputFiles() {
    targetWeightsFile = null;
    weightsInputPanel.setWeightsText("");
    targetPredicatesFile = null;
    prismInputPanel.setPredicatesText("");
    targetTransitionFile = null;
    prismInputPanel.setTransitionText("");
    targetLabelsFile = null;
    prismInputPanel.setLabelsText("");
    targetStateRewardsFile = null;
    prismInputPanel.setStateRewardsText("");
  }

  public void saveInputFiles() {
    if (targetWeightsFile != null) {
      writeFile(targetWeightsFile, weightsInputPanel.getWeightsText());
    }
    if (targetPredicatesFile != null) {
      writeFile(targetPredicatesFile, prismInputPanel.getPredicatesText());
    }
    if (targetTransitionFile != null) {
      writeFile(targetTransitionFile, prismInputPanel.getTransitionText());
    }
    if (targetLabelsFile != null) {
      writeFile(targetLabelsFile, prismInputPanel.getLabelsText());
    }
    if (targetStateRewardsFile != null) {
      writeFile(targetStateRewardsFile, prismInputPanel.getStateRewardsText());
    }
  }

  private static String openFile(File file) {
    if (!file.exists()) {
      return "";
    }

    try {
      String encoding = Env.get("EDITOR_FILE_READ_ENCODING");
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(file), encoding));

      StringBuilder buf = new StringBuilder();
      String line;
      if ((line = reader.readLine()) != null) {
        buf.append(line);
      }
      while ((line = reader.readLine()) != null) {
        buf.append("\r\n" + line);
      }
      reader.close();

      return buf.toString();
    } catch (Exception e) {
      FrontEnd.printException(e);
    }
    return "";
  }

  private static void writeFile(File file, String str)
	{
		try
		{
			String encoding = Env.get("EDITOR_FILE_WRITE_ENCODING");
			BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), encoding));
			writer.write(str + "\r\n");
			writer.close();
		}
		catch (IOException e)
		{
			FrontEnd.printException(e);
		}
	}

  // run translator
  public void runTranslator() 
  {
    FrontEnd.mainFrame.toolTab.setTab("System");
    FrontEnd.println("Running translator...");

    targetSlimDumpFile = new File(targetBasePath + "_slim.txt");
    if (!targetSlimDumpFile.exists()) {
      FrontEnd.println("SLIM dump file does not exist: " + targetSlimDumpFile.getAbsolutePath());
      return;
    }

    if (targetWeightsFile == null || !targetWeightsFile.exists()) {
      FrontEnd.println("Weights file does not exist: " + (targetWeightsFile != null ? targetWeightsFile.getAbsolutePath() : "null"));
      return;
    }

    translatorRunner = new ProbabilisticTranslatorRunner(targetSlimDumpFile, targetWeightsFile);
    translatorRunner.run();

    new Thread() {
      public void run() {
        while (translatorRunner.isRunning()) {
          FrontEnd.sleep(200);
        }
        if (translatorRunner.isSucceeded()) {
          FrontEnd.println("Translation completed successfully.");
        } else {
          FrontEnd.println("Translator run failed.");
        }
        translatorRunner = null;
      }
    }.start();
  }

  public void killTranslator() {
    if (translatorRunner != null) {
      translatorRunner.kill();
      translatorRunner = null;
    }
  }
}
