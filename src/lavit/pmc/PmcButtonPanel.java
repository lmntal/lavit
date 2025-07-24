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
  private JButton prismRunButton;
  private JButton prismKillButton;
  private JButton stateViewerButton;

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
    translatorPanel.setBorder(new TitledBorder("Translator"));

    translatorRunButton = new JButton("Run Translator");
    translatorRunButton.addActionListener(this);
    translatorPanel.add(translatorRunButton);

    translatorKillButton = new JButton("Kill");
    translatorKillButton.addActionListener(this);
    translatorPanel.add(translatorKillButton);

    JPanel prismPanel = new JPanel();
    prismPanel.setBorder(new TitledBorder("PRISM"));
    prismRunButton = new JButton("Run PRISM");
    prismRunButton.addActionListener(this);
    prismPanel.add(prismRunButton);

    prismKillButton = new JButton("Kill");
    prismKillButton.addActionListener(this);
    prismPanel.add(prismKillButton);

    JPanel stateViewerPanel = new JPanel();
    stateViewerPanel.setBorder(new TitledBorder("State Viewer"));
    stateViewerButton = new JButton("Open State Viewer");
    stateViewerButton.addActionListener(this);
    stateViewerPanel.add(stateViewerButton);

    add(filePanel);
    add(translatorPanel);
    add(prismPanel);
    add(stateViewerPanel);
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
    } else if (src == prismRunButton) {
      pmcPanel.runPrism();
    } else if (src == prismKillButton) {
      pmcPanel.killPrism();
    } else if (src == stateViewerButton) {
      pmcPanel.openStateViewer();
    } else {
      System.err.println("Unknown action source: " + src);
    }
  }
}
