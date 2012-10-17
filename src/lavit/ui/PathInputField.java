package lavit.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class PathInputField extends JComponent
{
	private JTextField textInput;
	private JButton buttonBrowse;
	private JFileChooser fileChooser;

	private boolean enabled;
	private boolean readOnly;

	public PathInputField(JFileChooser fileChooser, String buttonCaption, int columns)
	{
		this.fileChooser = fileChooser;

		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);

		textInput = new JTextField(columns);
		add(textInput);

		buttonBrowse = new JButton(buttonCaption);
		buttonBrowse.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				browse();
			}
		});
		add(buttonBrowse);

		gl.setHorizontalGroup(gl.createSequentialGroup()
			.addComponent(textInput)
			.addComponent(buttonBrowse)
		);
		gl.setVerticalGroup(gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(textInput)
			.addComponent(buttonBrowse)
		);

		setEnabled(true);
		setReadOnly(false);
	}

	public int getBaseline(int width, int height)
	{
		return textInput.getBaseline(width, height);
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		updateState();
	}

	public boolean isReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
		updateState();
	}

	public String getPathText()
	{
		return textInput.getText();
	}

	public void setPathText(String s)
	{
		textInput.setText(s);
	}

	public void addChangeListener(final ChangeListener l)
	{
		textInput.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				l.stateChanged(new ChangeEvent(textInput));
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				l.stateChanged(new ChangeEvent(textInput));
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
			}
		});
	}

	private void updateState()
	{
		textInput.setEnabled(enabled);
		textInput.setEditable(!readOnly);
		buttonBrowse.setEnabled(enabled && !readOnly);
	}

	private void browse()
	{
		initCurrentDirectory();
		int ret = fileChooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
		{
			approved();
		}
	}

	private void approved()
	{
		textInput.setText(fileChooser.getSelectedFile().getAbsolutePath());
	}

	private void initCurrentDirectory()
	{
		File file = new File(getPathText());
		if (file.exists())
		{
			fileChooser.setSelectedFile(file);
		}
		else
		{
			while (!file.exists() && file.getParentFile() != null)
			{
				file = file.getParentFile();
			}
			if (!file.exists())
			{
				file = new File("").getAbsoluteFile();
			}
			fileChooser.setCurrentDirectory(file);
		}
	}
}
