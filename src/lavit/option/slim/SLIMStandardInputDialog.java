/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
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

package lavit.option.slim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import lavit.option.AbstractConfigEdit;

@SuppressWarnings("serial")
public class SLIMStandardInputDialog extends JDialog
{
	private JFileChooser fileChooser;
	private JTextArea textArea;
	private AbstractConfigEdit<String> edit;

	public SLIMStandardInputDialog(Window owner, AbstractConfigEdit<String> edit)
	{
		super(owner, "Standard Input for SLIM");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.edit = edit;

		fileChooser = new JFileChooser("./");
		fileChooser.setMultiSelectionEnabled(false);

		JPanel panelCenter = new JPanel();
		textArea = new JTextArea(6, 30);
		textArea.setText(edit.get());
		JScrollPane textAreaSp = new JScrollPane(textArea);
		JButton buttonLoad = new JButton("Load from a file...");
		buttonLoad.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				loadFromFile();
			}
		});

		GroupLayout gl = new GroupLayout(panelCenter);
		panelCenter.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.TRAILING)
			.addComponent(textAreaSp)
			.addComponent(buttonLoad)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addComponent(textAreaSp)
			.addComponent(buttonLoad)
		);

		add(panelCenter, BorderLayout.CENTER);

		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panelButtons.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(4, 4, 4, 4),
			BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE))));
		JButton buttonApply = new JButton("Apply");
		buttonApply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onApply();
			}
		});
		JButton buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onReset();
			}
		});
		JButton buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
			}
		});
		fixButtons(90, 24, buttonApply, buttonReset, buttonClose);
		panelButtons.add(buttonApply);
		panelButtons.add(buttonReset);
		panelButtons.add(buttonClose);

		add(panelButtons, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(buttonApply);

		pack();
	}

	private void loadFromFile()
	{
		int ret = fileChooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			StringBuilder buf = new StringBuilder();
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				while ((line = reader.readLine()) != null)
				{
					buf.append(line).append('\n');
				}
				reader.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			textArea.setText(buf.toString());
		}
	}

	private void onApply()
	{
		edit.set(textArea.getText());
	}

	private void onReset()
	{
		edit.revert();
		textArea.setText(edit.get());
	}

	private void close()
	{
		dispose();
	}

	private static void fixButtons(int minWidth, int minHeight, JButton ... buttons)
	{
		int w = minWidth, h = minHeight;
		for (JButton button : buttons)
		{
			Dimension size = button.getPreferredSize();
			w = Math.max(w, size.width);
			h = Math.max(h, size.height);
		}
		for (JButton button : buttons)
		{
			button.setPreferredSize(new Dimension(w, h));
		}
	}
}
