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

package lavit.option;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lavit.Env;
import lavit.option.slim.SLIMStandardInputDialog;

@SuppressWarnings("serial")
class OptionSlimPanel extends JPanel
{
	private AbstractOptionPanel panel;

	public OptionSlimPanel(String[] options)
	{
		setLayout(new BorderLayout(0, 8));

		panel = new AbstractOptionPanel("SLIM Option", "SLIM_OPTION", options);
		add(panel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton("Standard Input");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Window owner = SwingUtilities.getWindowAncestor(OptionSlimPanel.this);
				SLIMStandardInputDialog dialog = new SLIMStandardInputDialog(owner, new AbstractConfigEdit<String>()
				{
					public void set(String value)
					{
						value = value.replace("\n", "\\n");
						Env.set("slim.stdin.str", value);
					}

					public String get()
					{
						String s = Env.get("slim.stdin.str", "");
						return s.replace("\\n", "\n");
					}
				});
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			}
		});
		bottomPanel.add(button);
		add(bottomPanel, BorderLayout.SOUTH);
	}
}
