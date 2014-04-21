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

package extgui.collapsiblepane;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

/**
 * 折り畳み、展開可能な複数のグループを保持するコンポーネント。
 * 現状では、各グループの内部プロパティへは一意な識別文字列を介してアクセスする。
 * @author Yuuki.S
 */
@SuppressWarnings("serial")
public class CollapsiblePane extends JPanel
{
	private Map<String, CollapsibleItemPanel> items = new HashMap<String, CollapsibleItemPanel>();
	private GroupLayout.Group hGroup;
	private GroupLayout.Group vGroup;

	public CollapsiblePane()
	{
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		hGroup = gl.createParallelGroup(GroupLayout.Alignment.LEADING);
		vGroup = gl.createSequentialGroup();
		gl.setHorizontalGroup(hGroup);
		gl.setVerticalGroup(vGroup);
	}

	public CollapsiblePane addPage(String key, String caption, Component content)
	{
		if (items.containsKey(key))
		{
			throw new RuntimeException("CollapsiblePane: key " + key + " already exists.");
		}

		CollapsibleItemPanel p = new CollapsibleItemPanel(caption, content);
		hGroup.addComponent(p, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(p);
		items.put(key, p);
		return this;
	}

	public Set<String> getKeys()
	{
		return items.keySet();
	}

	public boolean containsKey(String key)
	{
		return items.containsKey(key);
	}

	public void setExpanded(String key, boolean b)
	{
		setExpanded(key, b, false);
	}

	public void setExpanded(String key, boolean b, boolean scrollToVisible)
	{
		items.get(key).setExpanded(b, scrollToVisible);
	}

	public boolean isExpanded(String key)
	{
		return items.get(key).isExpanded();
	}
}
