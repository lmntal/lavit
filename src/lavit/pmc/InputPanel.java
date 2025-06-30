package lavit.pmc;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.undo.UndoManager;

import lavit.Env;
import lavit.util.CommonFontUser;
import lavit.util.SimpleUndoKeyListener;
import lavit.util.SimpleUndoableEditListener;

public class InputPanel extends JPanel implements CommonFontUser {
  public InputPanel inputPanel;
  private JTextArea weightsInputArea;
  private UndoManager weightsUndoManager = new UndoManager();

  public InputPanel(PmcPanel pmcPanel) {
    setBorder(new TitledBorder("Weights Settings"));
		setLayout(new BorderLayout());

    weightsInputArea = new JTextArea();
    weightsInputArea.getDocument().addUndoableEditListener(new SimpleUndoableEditListener(weightsUndoManager));
    weightsInputArea.addKeyListener(new SimpleUndoKeyListener(weightsUndoManager));
    add(new JScrollPane(weightsInputArea), BorderLayout.CENTER);

    loadFont();
  }

  public void loadFont()
	{
		Font font = Env.getEditorFont();
		weightsInputArea.setFont(font);
	}

  public void setWeightsText(String str) {
    weightsInputArea.setText(str);
    weightsUndoManager.discardAllEdits();
  }

  public String getWeightsText() {
    return weightsInputArea.getText();
  }
}
