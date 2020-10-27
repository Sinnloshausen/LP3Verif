package processCalculus;

import java.util.ArrayList;
import java.util.List;

public class Trace {

	// class fields
	private List<ExtendedProcess> states;
	
	// full constructur
	public Trace(List<ExtendedProcess> states) {
		this.states = states;
	}
	
	// copy constructor
	public Trace(Trace old) {
		this(new ArrayList<>(old.getStates()));
	}
	
	// empty constructor
	public Trace() {
		this(new ArrayList<ExtendedProcess>());
	}

	//class methods
	public int length() {
		return states.size();
	}
	
	public List<State> getSigmas() {
		ArrayList<State> sigmas = new ArrayList<State>();
		for (ExtendedProcess A : states) {
			sigmas.add(A.getSigma());
		}
		return sigmas;
	}
	
	// getter and setter
	public List<ExtendedProcess> getStates() {
		return states;
	}
	
	public ExtendedProcess getState(int index) {
		return states.get(index);
	}
	
	public Trace getSubset(int start, int end) {
		return new Trace(states.subList(start, end));
	}
	
	public void addState(ExtendedProcess state) {
		states.add(state);
	}

	// print
	@Override
	public String toString() {
		String trc = "";
		if (states.isEmpty()) {
			return states.toString();
		}
		for (int i = 0; i < states.size()-1; i++) {
			trc += states.get(i) + " --> ";
		}
		trc += states.get(states.size()-1);
		return trc;
	}
}
