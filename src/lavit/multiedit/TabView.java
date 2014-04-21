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

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.event.TabChangeListener;
import lavit.multiedit.event.CaretPositionChangeListener;
import lavit.multiedit.event.TabButtonListener;

@SuppressWarnings("serial")
public class TabView extends JTabbedPane
{
	private List<TabChangeListener> tabChangeListeners = new ArrayList<TabChangeListener>();

	public TabView()
	{
		addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				dispatchTabChangeEvent();
				if (getTabCount() > 0)
				{
					dispatchCaretPositionChangeEvent(getSelectedPage().getLineColumn());
				}
			}
		});

		int ctrlMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ctrlMask), "next");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ctrlMask | InputEvent.SHIFT_DOWN_MASK), "prev");

		ActionMap am = getActionMap();
		am.put("next", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (getTabCount() != 0)
				{
					int index = (getSelectedIndex() + 1) % getTabCount();
					setSelectedPage(index);
				}
			}
		});
		am.put("prev", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (getTabCount() != 0)
				{
					int index = (getSelectedIndex() + getTabCount() - 1) % getTabCount();
					setSelectedPage(index);
				}
			}
		});
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		for (int i = 0; i < getTabCount(); i++)
		{
			getComponentAt(i).setFont(font);
		}
	}

	public EditorPage createEmptyPage()
	{
		EditorPage page = new EditorPage(this);
		page.setFont(getFont());
		page.setTabWidth(4);
		page.addCaretPositionChangeListener(new CaretPositionChangeListener()
		{
			public void caretPositionChanged(LineColumn lineColumn)
			{
				dispatchCaretPositionChangeEvent(lineColumn);
			}
		});
		addTab(null, page);
		setTabComponentAt(indexOfComponent(page), page.getHeaderComponent());
		return page;
	}

	/**
	 * 選択されたページが存在しない場合は{@code null}を返す。
	 */
	public EditorPage getSelectedPage()
	{
		return (EditorPage)getSelectedComponent();
	}

	public void setSelectedPage(int index)
	{
		setSelectedIndex(index);
		EditorPage page = getSelectedPage();
		if (page != null)
		{
			page.requestFocus();
		}
	}

	public void setSelectedPage(EditorPage page)
	{
		setSelectedComponent(page);
		page.requestFocus();
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

	public void addTabButtonListener(TabButtonListener l)
	{
		listenerList.add(TabButtonListener.class, l);
	}

	public void addTabChangeListener(TabChangeListener l)
	{
		tabChangeListeners.add(l);
	}

	public void removeTabChangeListener(TabChangeListener listener)
	{
		tabChangeListeners.remove(listener);
	}

	public void addCaretPositionChangeListener(CaretPositionChangeListener listener)
	{
		listenerList.add(CaretPositionChangeListener.class, listener);
	}

	public void removeCaretPositionChangeListener(CaretPositionChangeListener listener)
	{
		listenerList.remove(CaretPositionChangeListener.class, listener);
	}

	private void dispatchCaretPositionChangeEvent(LineColumn lineColumn)
	{
		for (CaretPositionChangeListener l : listenerList.getListeners(CaretPositionChangeListener.class))
		{
			l.caretPositionChanged(lineColumn);
		}
	}

	void onTabCloseButtonClicked(EditorPage page)
	{
		int index = indexOfComponent(page);
		if (index != -1)
		{
			for (TabButtonListener l : listenerList.getListeners(TabButtonListener.class))
			{
				l.closeButtonClicked(index);
			}
		}
	}

	private void dispatchTabChangeEvent()
	{
		for (TabChangeListener l : tabChangeListeners)
		{
			l.tabChanged();
		}
	}
}
