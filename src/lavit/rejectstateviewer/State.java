package lavit.rejectstateviewer;

import java.util.LinkedHashSet;

public class State extends Node {

	public String state;
	public String label;
	boolean accept;
	public boolean inCycle;

	private LinkedHashSet<Transition> toes;
	private LinkedHashSet<Transition> fromes;

	public GraphSet childSet;
	public GraphSet parentSet;

}
