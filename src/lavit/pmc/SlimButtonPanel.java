package lavit.pmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lavit.FrontEnd;
import lavit.editor.EditorPanel;
import lavit.runner.SlimRunner;
import lavit.util.FixFlowLayout;

public class SlimButtonPanel extends JPanel implements ActionListener {
  public PmcPanel pmcPanel;

  private SlimRunner slimRunner;
  private JButton slimButton;

  public SlimButtonPanel(PmcPanel pmcPanel) {
    this.pmcPanel = pmcPanel;

    setLayout(new FixFlowLayout());

    JPanel buttonPanel = new JPanel();
    
    slimButton = new JButton("Generate State Transition System from LMNtal model (SLIM)");
    slimButton.addActionListener(this);
    buttonPanel.add(slimButton);

    add(buttonPanel);
  }

  private void setAllEnable(boolean enable) {
    slimButton.setEnabled(enable);
    // Add other buttons here if needed
  }

  private void setButtonEnable(boolean enable) {
    setAllEnable(enable);
    FrontEnd.mainFrame.editorPanel.buttonPanel.setAllEnable(enable);
  }

  private String getModelNameFromFile(File file) {
    if (file == null) {
      return null;
    }
    String fileName = file.getName();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      return fileName.substring(0, dotIndex);
    } else {
      return fileName; // No extension found
    }
  }

  private String getModelDirectoryFromFile(File file) {
    if (file == null) {
      return null;
    }
    String filePath = file.getAbsolutePath();
    int lastSlashIndex = filePath.lastIndexOf(File.separator);
    if (lastSlashIndex > 0) {
      return filePath.substring(0, lastSlashIndex);
    } else {
      return "."; // Current directory if no path found
    }
  }

  private void saveSlimOutputToFile(String slimOutput) {
    File targetFile = FrontEnd.mainFrame.editorPanel.getFile();
    if (targetFile == null) {
      FrontEnd.println("(SLIM) No file to save results.");
      return;
    }

    String modelName = getModelNameFromFile(targetFile);
    String modelDirectory = getModelDirectoryFromFile(targetFile);
    if (modelName == null || modelDirectory == null) {
      FrontEnd.println("(SLIM) Invalid model name or directory.");
      return;
    }
    String slimOutputFilePath = modelDirectory + File.separator + modelName + "_slim.txt";
    try {
      java.nio.file.Files.write(java.nio.file.Paths.get(slimOutputFilePath), slimOutput.getBytes());
      FrontEnd.println("(SLIM) Results saved to: " + slimOutputFilePath);
    } catch (java.io.IOException ex) {
      FrontEnd.println("(SLIM) Error saving results: " + ex.getMessage());
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == slimButton) {
      EditorPanel editorPanel = FrontEnd.mainFrame.editorPanel;

      if (editorPanel.isChanged()) {
        editorPanel.fileSave();
      }

      setButtonEnable(false);

      FrontEnd.mainFrame.toolTab.setTab("System");

			FrontEnd.println("(SLIM) Doing...");

			String option = "-t --nd --hl --use-builtin-rule --show-transition";

      slimRunner = new SlimRunner(option);
      slimRunner.setBuffering(true);
      slimRunner.run();
      new Thread() {
				public void run() {
					while (slimRunner.isRunning()) {
						FrontEnd.sleep(200);
					}

          saveSlimOutputToFile(slimRunner.getBufferString());

					FrontEnd.println("(SLIM) Done!");
					slimRunner = null;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setButtonEnable(true);
						}
					});
				}
			}.start();
    } 
  }
}
