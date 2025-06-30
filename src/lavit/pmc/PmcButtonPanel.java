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

    add(filePanel);
  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == saveButton) {
      pmcPanel.savePmcFiles();
    } else if (src == loadButton) {
      pmcPanel.loadPmcFiles();
    } else {
      System.err.println("Unknown action source: " + src);
    }
  }
}
