package lavit.pmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lavit.util.FixFlowLayout;

public class PmcButtonPanel extends JPanel implements ActionListener {
  public PmcPanel pmcPanel;

  private JButton saveButton;
  private JButton loadButton;
  private JButton translatorRunButton;
  private JButton translatorKillButton;

  public PmcButtonPanel(PmcPanel pmcPanel) {
    this.pmcPanel = pmcPanel;

    setLayout(new FixFlowLayout());

    JPanel filePanel = new JPanel();
    filePanel.setBorder(new TitledBorder("Input Files"));

    loadButton = new JButton("Load");
    loadButton.addActionListener(this);
    filePanel.add(loadButton);

    saveButton = new JButton("Save");
    saveButton.addActionListener(this);
    filePanel.add(saveButton);

    JPanel translatorPanel = new JPanel();
    translatorPanel.setBorder(new TitledBorder("Translator(slim dump to dtmc)"));

    translatorRunButton = new JButton("Run Translator");
    translatorRunButton.addActionListener(this);
    translatorPanel.add(translatorRunButton);

    translatorKillButton = new JButton("Kill");
    translatorKillButton.addActionListener(this);
    translatorPanel.add(translatorKillButton);

    add(filePanel);
    add(translatorPanel);
  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == saveButton) {
      pmcPanel.saveInputFiles();
    } else if (src == loadButton) {
      pmcPanel.loadInputFiles();
    } else if (src == translatorRunButton) {
      pmcPanel.runTranslator();
    } else if (src == translatorKillButton) {
      pmcPanel.killTranslator();
    } else {
      System.err.println("Unknown action source: " + src);
    }
  }
}
