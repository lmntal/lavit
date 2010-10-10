package lavit.util;

import java.util.Comparator;

import lavit.stateviewer.StateNode;

public class NodeYComparator implements Comparator<StateNode> {
	public int compare(StateNode n1, StateNode n2) {
		double n1y = n1.getY();
		double n2y = n2.getY();
		if(n1y<n2y){
			return -1;
		}else if(n1y>n2y){
			return 1;
		}else{
			if(n1.id<n2.id){
				return -1;
			}else if(n1.id>n2.id){
				return 1;
			}else{
				return 0;
			}
		}
	}
}