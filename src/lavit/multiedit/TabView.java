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

package lavit.multiedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import lavit.Env;
import lavit.ui.FlatButton;

@SuppressWarnings("serial")
public class TabView extends JTabbedPane
{
	public TabView()
	{
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		for (int i = 0; i < getTabCount(); i++)
		{
			getComponentAt(i).setFont(font);
		}
	}

	public void addPage(EditorPage page, String title, String toolTip)
	{
		page.setFont(getFont());
		page.setTabWidth(4);

		addTab(title, null, page, toolTip);
		setTitle(page, title, toolTip);

		setSelectedPage(getTabCount() - 1);
	}

	public void setTitle(final EditorPage page, String title, String toolTip)
	{
		JPanel hp = new JPanel(new BorderLayout());
		hp.setOpaque(false);

		// fix header width
		JLabel header = new JLabel(title);
		Dimension dim = header.getPreferredSize();
		dim.width = Math.max(dim.width, 100);
		header.setPreferredSize(dim);
		hp.add(header, BorderLayout.CENTER);

		Icon icon;
		if (title.endsWith(".lmn"))
		{
			icon = new ImageIcon(Env.getImageOfFile("img/tab_icon_lmn.png"));
		}
		else if (title.endsWith(".il") || title.endsWith(".tal"))
		{
			icon = new ImageIcon(Env.getImageOfFile("img/tab_icon_il.png"));
		}
		else
		{
			icon = new ImageIcon(Env.getImageOfFile("img/tab_icon.png"));
		}
		JLabel iconLabel = new JLabel(icon);
		iconLabel.setPreferredSize(new Dimension(16, 16));
		hp.add(iconLabel, BorderLayout.WEST);

		JButton closeButton = new FlatButton();
		closeButton.setIcon(new ImageIcon(Env.getImageOfFile("img/tab_close_cold.png")));
		closeButton.setRolloverIcon(new ImageIcon(Env.getImageOfFile("img/tab_close_hot.png")));
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				closePage(indexOfComponent(page));
			}
		});
		closeButton.setPreferredSize(new Dimension(16, 16));
		hp.add(closeButton, BorderLayout.EAST);

		setTabComponentAt(indexOfComponent(page), hp);
	}

	public EditorPage getSelectedPage()
	{
		int index = getSelectedIndex();
		return (EditorPage)getComponentAt(index);
	}

	public void setSelectedPage(int index)
	{
		setSelectedIndex(index);
		getSelectedPage().requestFocus();
	}

	public void closePage(int index)
	{
		removeTabAt(index);
	}

	public void closeSelectedPage()
	{
		closePage(getSelectedIndex());
	}

	public EditorPage[] getPages()
	{
		EditorPage[] pages = new EditorPage[getTabCount()];
		for (int i = 0; i < getTabCount(); i++)
		{
			pages[i] = (EditorPage)getComponentAt(i);
		}
		return pages;
	}
}
