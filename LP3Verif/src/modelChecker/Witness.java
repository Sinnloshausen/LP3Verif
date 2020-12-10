package modelChecker;

import java.util.Set;

import processCalculus.ExtendedProcess;
import processCalculus.State;
import processCalculus.Trace;
import properties.StaticFormula;

public class Witness {
	
	// class fields
	private boolean bool;
	private String witness;
	private State state;
	private String trace;
	private StaticFormula formula;
	
	// full constructor
	public Witness(boolean bool, String witness, State state, String trace, StaticFormula formula) {
		this.bool = bool;
		this.witness = witness;
		this.state = state;
		this.trace = trace;
		this.formula = formula;
	}
	
	// witness free constructor
	public Witness(boolean bool, StaticFormula formula) {
		this(bool, null, null, null, formula);
	}
	
	// empty constructor
	public Witness(boolean bool) {
		this(bool, null, null, null, null);
	}

	// class methods
	public String makeQuery() {
		String w = "";
		if (!bool) {
			w = "Witness:\n";
			w += "  Representative User: " + witness.substring(1, 31) + "\n";
			w += "  Other User Location: " + witness.substring(32, 42) + "\n";
			w += "  Query1: " + witness.substring(42, witness.indexOf("(G2")) + "\n";
			w += "  Query2: " + witness.substring(witness.indexOf("(G2"), witness.length()-1) + "\n";
			w += "  Trace: " + trace + "\n";
			return w;
		}
		return w;
	}

	public String shortestTrace(Set<Trace> T) {
		// TODO returns the shortest trace to reach the state
		if (state == null) {
			return null;
		}
		// iterate traces
		String shortest = "";
		int current = 100;
		for (Trace t : T) {
			int i = 0;
			for (ExtendedProcess a : t.getStates()) {
				++i;
				State s = a.getSigma();
				if (s.equals(state) && i < current) {
					shortest = t.getSubset(0, i).toString();
					current = i;
					break;
				}
			}
		}
		return shortest;
	}
	
	// print
	@Override
	public String toString() {
		if (bool) {
			return "true";
		} else {
			return witness;
		}
	}

	// getter and setter
	public boolean getBool() {
		return bool;
	}
	
	public String getWitness() {
		return witness;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
	
	public void setTrace(String trace) {
		this.trace = trace;
	}
	
	public String getTrace() {
		return trace;
	}

}
