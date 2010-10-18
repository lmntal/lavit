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

package lavit.util;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import lavit.Env;
import lavit.stateviewer.StateNode;

public abstract class StateDraw {
	protected boolean simpleMode;
	protected boolean hideBackEdgeMode;
	protected boolean showIdMode;
	protected boolean showRuleMode;
	protected boolean showNoNameRuleMode;
	protected boolean showDummyMode;
	protected boolean cycleMode;
	protected boolean searchMode;

	public StateDraw(){
		if(Env.get("SV_SIMPLE_MODE").equals("auto")){
			this.simpleMode = false;
		}else{
			this.simpleMode = Env.is("SV_SIMPLE_MODE");
		}
		this.hideBackEdgeMode = Env.is("SV_HIDEBACKEDGE");
		this.showIdMode = Env.is("SV_SHOWID");
		this.showRuleMode = Env.is("SV_SHOWRULE");
		this.showNoNameRuleMode = Env.is("SV_SHOWNONAMERULE");
		this.showDummyMode = Env.is("SV_SHOW_DUMMY");

		//this.cycleMode = false;
		this.searchMode = false;
	}

	public void setCycleMode(boolean cycleMode){
		this.cycleMode = cycleMode;
	}

	public boolean isCycleMode(){
		return this.cycleMode;
	}

	public void setSearchMode(boolean searchMode){
		this.searchMode = searchMode;
	}

	public boolean isSearchMode(){
		return this.searchMode;
	}

	public void setSimpleMode(boolean simpleMode){
		this.simpleMode = simpleMode;
	}

	public boolean isSimpleMode(){
		return this.simpleMode;
	}

	public void setHideBackEdgeMode(boolean hideBackEdgeMode){
		this.hideBackEdgeMode = hideBackEdgeMode;
	}

	public boolean isHideBackEdgeMode(){
		return this.hideBackEdgeMode;
	}

	public void setShowIdMode(boolean showIdMode){
		this.showIdMode = showIdMode;
	}

	public void setShowRuleMode(boolean showRuleMode){
		this.showRuleMode = showRuleMode;
	}

	public void setShowNoNameRuleMode(boolean showNoNameRuleMode){
		this.showNoNameRuleMode = showNoNameRuleMode;
	}

	public void setShowDummyMode(boolean showDummyMode){
		this.showDummyMode = showDummyMode;
	}

	public boolean isShowDummyMode(){
		return this.showDummyMode;
	}

	public abstract void drawGraph(Graphics2D g2);
	public abstract void setNodeLook(StateNode node);
}
