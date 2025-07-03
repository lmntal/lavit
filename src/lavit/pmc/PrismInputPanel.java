package lavit.pmc;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.undo.UndoManager;

import lavit.Env;
import lavit.util.CommonFontUser;
import lavit.util.SimpleUndoKeyListener;
import lavit.util.SimpleUndoableEditListener;

public class PrismInputPanel extends JPanel implements CommonFontUser {
  private JTextArea predicatesInputArea;
  private JTextArea transitionInputArea;
  private JTextArea labelsInputArea;
  private JTextArea stateRewardsInputArea;

  private UndoManager predicatesUndoManager = new UndoManager();
  private UndoManager transitionUndoManager = new UndoManager();
  private UndoManager labelsUndoManager = new UndoManager();
  private UndoManager stateRewardsUndoManager = new UndoManager();

  public PrismInputPanel(PmcPanel pmcPanel) {
    setBorder(new TitledBorder("PRISM Inputs"));
		setLayout(new BorderLayout());

    predicatesInputArea = new JTextArea();
    predicatesInputArea.getDocument().addUndoableEditListener(new SimpleUndoableEditListener(predicatesUndoManager));
    predicatesInputArea.addKeyListener(new SimpleUndoKeyListener(predicatesUndoManager));

    transitionInputArea = new JTextArea();
    transitionInputArea.getDocument().addUndoableEditListener(new SimpleUndoableEditListener(transitionUndoManager));
    transitionInputArea.addKeyListener(new SimpleUndoKeyListener(transitionUndoManager));

    labelsInputArea = new JTextArea();
    labelsInputArea.getDocument().addUndoableEditListener(new SimpleUndoableEditListener(labelsUndoManager));
    labelsInputArea.addKeyListener(new SimpleUndoKeyListener(labelsUndoManager));

    stateRewardsInputArea = new JTextArea();
    stateRewardsInputArea.getDocument().addUndoableEditListener(new SimpleUndoableEditListener(stateRewardsUndoManager));
    stateRewardsInputArea.addKeyListener(new SimpleUndoKeyListener(stateRewardsUndoManager));

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Predicates", new JScrollPane(predicatesInputArea));
    tabbedPane.addTab("Transitions", new JScrollPane(transitionInputArea));
    tabbedPane.addTab("Labels", new JScrollPane(labelsInputArea));
    tabbedPane.addTab("State Rewards", new JScrollPane(stateRewardsInputArea));
    add(tabbedPane, BorderLayout.CENTER);

    loadFont();
  }

  public void loadFont()
	{
		Font font = Env.getEditorFont();
    predicatesInputArea.setFont(font);
    transitionInputArea.setFont(font);
    labelsInputArea.setFont(font);
    stateRewardsInputArea.setFont(font);
	}

  public void setPredicatesText(String str) {
    predicatesInputArea.setText(str);
    predicatesUndoManager.discardAllEdits();
  }

  public String getPredicatesText() {
    return predicatesInputArea.getText();
  }
  
  public void setTransitionText(String str) {
    transitionInputArea.setText(str);
    transitionUndoManager.discardAllEdits();
  }

  public String getTransitionText() {
    return transitionInputArea.getText();
  }

  public void setLabelsText(String str) {
    labelsInputArea.setText(str);
    labelsUndoManager.discardAllEdits();
  }

  public String getLabelsText() {
    return labelsInputArea.getText();
  }

  public void setStateRewardsText(String str) {
    stateRewardsInputArea.setText(str);
    stateRewardsUndoManager.discardAllEdits();
  }

  public String getStateRewardsText() {
    return stateRewardsInputArea.getText();
  }
}
