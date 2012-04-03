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

package lavit.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class FindReplaceDialog extends JDialog
{
	private JTextField findText;
	private JTextField replaceText;
	private JButton findForward;
	private JButton findBackward;
	private JButton replace;
	private JButton replaceAll;
	private JCheckBox useRegex;
	private JButton buttonClose;
	
	public FindReplaceDialog()
	{
		setTitle("Find/Replace");
		
		JPanel contents = new JPanel();
		
		JLabel labelFind = new JLabel("Find:");
		JLabel labelReplace = new JLabel("Replace:");
		findText = new JTextField();
		findText.setColumns(20);
		replaceText = new JTextField();
		replaceText.setColumns(20);
		findForward = new JButton("Find forward");
		findBackward = new JButton("Find backward");
		replace = new JButton("Replace");
		replaceAll = new JButton("Replace all");
		useRegex = new JCheckBox("Regex");

		Dimension dim = findForward.getPreferredSize();
		dim.width += 20;
		dim.height = 22;
		findForward.setPreferredSize(dim);
		findForward.setMinimumSize(dim);
		findForward.setMaximumSize(dim);
		findBackward.setPreferredSize(dim);
		findBackward.setMinimumSize(dim);
		findBackward.setMaximumSize(dim);
		replace.setPreferredSize(dim);
		replace.setMinimumSize(dim);
		replace.setMaximumSize(dim);
		replaceAll.setPreferredSize(dim);
		replaceAll.setMinimumSize(dim);
		replaceAll.setMaximumSize(dim);

		contents.add(labelFind);
		contents.add(labelReplace);
		contents.add(findText);
		contents.add(replaceText);
		contents.add(findForward);
		contents.add(findBackward);
		contents.add(replace);
		contents.add(replaceAll);
		contents.add(useRegex);

		GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(labelFind)
				.addComponent(labelReplace))
			.addGroup(layout.createParallelGroup()
				.addComponent(findText)
				.addComponent(replaceText)
				.addComponent(useRegex))
			.addGroup(layout.createParallelGroup()
				.addComponent(findForward)
				.addComponent(findBackward)
				.addComponent(replace)
				.addComponent(replaceAll))
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(labelFind)
				.addComponent(findText)
				.addComponent(findForward))
			.addComponent(findBackward)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(labelReplace)
				.addComponent(replaceText)
				.addComponent(replace))
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(replaceAll)
				.addComponent(useRegex))
		);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(
			BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
					BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE)
				)
			)
		);
		buttonClose = new JButton("Close");
		buttonClose.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		buttonPanel.add(buttonClose);
		
		add(contents, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}
}
