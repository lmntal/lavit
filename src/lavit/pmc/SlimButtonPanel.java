package lavit.pmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lavit.FrontEnd;
import lavit.editor.EditorPanel;
import lavit.runner.SlimRunner;
import lavit.util.FixFlowLayout;

// TODO: slim 実行結果を <model_name>_slim.txt に保存する．

public class SlimButtonPanel extends JPanel implements ActionListener {
  public PmcPanel pmcPanel;

  private SlimRunner slimRunner;

  private JButton slimButton;

  public SlimButtonPanel(PmcPanel pmcPanel) {
    this.pmcPanel = pmcPanel;

    setLayout(new FixFlowLayout());

    setBorder(new javax.swing.border.TitledBorder("Generate State Transition System from LMNtal model"));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

    slimButton = new JButton("RUN SLIM");
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
      slimRunner.run();
      new Thread() {
				public void run() {
					while (slimRunner.isRunning()) {
						FrontEnd.sleep(200);
					}
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
