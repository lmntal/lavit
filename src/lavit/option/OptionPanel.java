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

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import lavit.Env;

@SuppressWarnings("serial")
public class OptionPanel extends JPanel
{
	private OptionLMNtalPanel optionLMNtalPanel;
	private OptionUnyoPanel optionUnyoPanel;
	private OptionCompilePanel optionCompilePanel;
	private OptionSlimPanel optionSlimPanel;
	private OptionSVPanel optionSVPanel;
	private OptionSVDepthPanel optionSVDepthPanel;
	private OptionLtlPanel optionLtlPanel;

	public OptionPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		optionLMNtalPanel = new OptionLMNtalPanel(readOptions("options.lmntal"));
		add(optionLMNtalPanel);

		optionUnyoPanel = new OptionUnyoPanel(readOptions("options.unyo"));
		add(optionUnyoPanel);

		optionCompilePanel = new OptionCompilePanel(readOptions("options.lmntal_slim"));
		add(optionCompilePanel);

		optionSlimPanel = new OptionSlimPanel(readOptions("options.slim"));
		add(optionSlimPanel);

		//optionSVDepthPanel = new OptionSVDepthPanel();
		//add(optionSVDepthPanel);

		optionSVPanel = new OptionSVPanel(readOptions("options.stateviewer"));
		add(optionSVPanel);

		optionLtlPanel = new OptionLtlPanel(readOptions("options.ltl"));
		add(optionLtlPanel);

		//fontSettingPanel = new FontSettingPanel();
		//add(fontSettingPanel);

		//encodingSettingPanel = new EncodingSettingPanel();
		//add(encodingSettingPanel);

		//editorColorPanel = new EditorColorPanel();
		//add(editorColorPanel);

		//generalSettingPanel = new GeneralSettingPanel();
		//add(generalSettingPanel);
	}

	private static String[] readOptions(String key)
	{
		return Env.get(key, "").split("\\s+");
	}
}
