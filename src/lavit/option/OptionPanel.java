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
import java.awt.Dialog.ModalityType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import lavit.Env;
import extgui.collapsiblepane.CollapsiblePane;

@SuppressWarnings("serial")
public class OptionPanel extends JPanel
{
	public OptionPanel()
	{
		setLayout(new BorderLayout());

		CollapsiblePane cp = new CollapsiblePane()
			.addPage("lmntal", "LMNtal Options", new OptionLMNtalPanel(readOptions("options.lmntal")))
			.addPage("unyo", "UNYO Options", new OptionUnyoPanel(readOptions("options.unyo")))
			.addPage("slim-compile", "SLIM Compile Options", new OptionCompilePanel(readOptions("options.lmntal_slim")))
			.addPage("slim", "SLIM Options", new OptionSlimPanel(readOptions("options.slim")))
			.addPage("sv", "StateViewer SLIM Options", new OptionSVPanel(readOptions("options.stateviewer")))
			.addPage("ltl", "LTL Model Check SLIM Options", new OptionLtlPanel(readOptions("options.ltl")))
		;

		for (String key : readOptions("window.controls.switches.expanded"))
		{
			if (cp.containsKey(key))
			{
				cp.setExpanded(key, true);
			}
		}

		JScrollPane jsp = new JScrollPane(cp);
		jsp.getVerticalScrollBar().setUnitIncrement(10);
		add(jsp, BorderLayout.CENTER);

		cp.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.isControlDown() && SwingUtilities.isRightMouseButton(e))
				{
					JDialog dialog = new CommonOptionEditorDialog();
					dialog.setModalityType(ModalityType.APPLICATION_MODAL);
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
				}
			}
		});
	}

	private static String[] readOptions(String key)
	{
		return Env.get(key, "").split("\\s+");
	}
}
