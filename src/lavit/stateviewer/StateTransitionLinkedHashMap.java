package lavit.stateviewer;

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
