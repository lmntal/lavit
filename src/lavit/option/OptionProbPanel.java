package lavit.option;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lavit.Env;

public class OptionProbPanel extends JPanel {
  public static final String PROB_TRANSLATOR_EXE_PATH = "PROB_TRANSLATOR_EXE_PATH";
  public static final String PRISM_EXE_PATH = "PRISM_EXE_PATH";
  public static final String PRISM_OPTIONS = "PRISM_OPTIONS";

  private JTextField probTranslatorPathTextField;
  private JTextField prismPathTextField;
  private JTextField prismOptionsTextField;

  public OptionProbPanel() {
    setLayout(new BorderLayout());

    // set PROB_TRANSLATOR_EXE_PATH
    JPanel probTranslatorPanel = new JPanel();
    probTranslatorPanel.setBorder(new TitledBorder("Probabilistic Translator Path (SLIM output to DTMC format)"));
    probTranslatorPathTextField = new JTextField(Env.get(PROB_TRANSLATOR_EXE_PATH, ""));
    probTranslatorPathTextField.getDocument().addDocumentListener(new ChangeHandler(probTranslatorPathTextField, PROB_TRANSLATOR_EXE_PATH));
    probTranslatorPanel.setLayout(new BorderLayout());
    probTranslatorPanel.add(probTranslatorPathTextField, BorderLayout.CENTER);
    add(probTranslatorPanel, BorderLayout.NORTH);

    // set PRISM_EXE_PATH
    JPanel prismPanel = new JPanel();
    prismPanel.setBorder(new TitledBorder("PRISM Executable Path"));
    prismPathTextField = new JTextField(Env.get(PRISM_EXE_PATH, ""));
    prismPathTextField.getDocument().addDocumentListener(new ChangeHandler(prismPathTextField, PRISM_EXE_PATH));
    prismPanel.setLayout(new BorderLayout());
    prismPanel.add(prismPathTextField, BorderLayout.CENTER);
    add(prismPanel, BorderLayout.CENTER);

    // set PRISM options
    JPanel prismOptionsPanel = new JPanel();
    prismOptionsPanel.setBorder(new TitledBorder("PRISM Options"));
    prismOptionsTextField = new JTextField(Env.get(PRISM_OPTIONS, ""));
    prismOptionsTextField.getDocument().addDocumentListener(new ChangeHandler(prismOptionsTextField, PRISM_OPTIONS));
    prismOptionsPanel.setLayout(new BorderLayout());
    prismOptionsPanel.add(prismOptionsTextField, BorderLayout.CENTER);
    add(prismOptionsPanel, BorderLayout.SOUTH);
  }

  private void applyChanges(JTextField optionField, String propertyName)
	{
		String newOptions = "";
		String field = optionField.getText();
		if (!field.isEmpty())
		{
			if (!newOptions.isEmpty())
			{
				newOptions += " ";
			}
			newOptions += optionField.getText();
		}
		Env.set(propertyName, newOptions);
	}

  private class ChangeHandler implements ActionListener, DocumentListener
	{
    private JTextField optionField;
    private String propertyName;

    public ChangeHandler(JTextField optionField, String propertyName)
		{
			this.optionField = optionField;
			this.propertyName = propertyName;
		}

		public void actionPerformed(ActionEvent e)
		{
			applyChanges(optionField, propertyName);
		}

		public void changedUpdate(DocumentEvent e)
		{
			applyChanges(optionField, propertyName);
		}

		public void insertUpdate(DocumentEvent e)
		{
			applyChanges(optionField, propertyName);
		}

		public void removeUpdate(DocumentEvent e)
		{
			applyChanges(optionField, propertyName);
		}
	}
}
