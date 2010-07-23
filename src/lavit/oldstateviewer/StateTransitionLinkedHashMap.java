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

package lavit.oldstateviewer;

import java.util.*;

public class StateTransitionLinkedHashMap{
	LinkedHashMap<StateNode,LinkedHashMap<StateNode,StateTransition>> trans;

	public StateTransitionLinkedHashMap(){
		trans = new LinkedHashMap<StateNode,LinkedHashMap<StateNode,StateTransition>>();
	}

	int size(){
		int size = 0;
		for(LinkedHashMap<StateNode,StateTransition> subTrans :  trans.values()){
    		size += subTrans.size();
    	}
    	return size;
	}

    boolean isEmpty(){
    	return trans.isEmpty();
    }

    boolean containsKey(StateNode from, StateNode to){
    	if(trans.containsKey(from)){
    		return trans.get(from).containsKey(to);
    	}
    	return false;
    }

    boolean containsValue(StateTransition value){
    	if(trans.containsKey(value.from)){
    		return trans.get(value.from).containsValue(value);
    	}
    	return false;
    }

    StateTransition get(StateNode from, StateNode to){
    	if(trans.containsKey(from)){
    		return trans.get(from).get(to);
    	}
    	return null;
    }

    StateTransition put(StateNode from, StateNode to, StateTransition value){
    	if(!trans.containsKey(from)){
    		trans.put(from, new LinkedHashMap<StateNode,StateTransition>());
    	}
    	return trans.get(from).put(to, value);
    }

    StateTransition remove(StateNode from, StateNode to){
    	if(trans.containsKey(from)){
    		return trans.get(from).remove(to);
    	}
    	return null;
    }

    void clear(){
    	trans.clear();
    }

    Collection<StateTransition> values(){
    	LinkedList<StateTransition> values = new LinkedList<StateTransition>();
    	for(LinkedHashMap<StateNode,StateTransition> subTrans :  trans.values()){
    		values.addAll(subTrans.values());
    	}
    	return values;
    }

}
