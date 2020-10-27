package utils;

import java.util.LinkedHashSet;
import java.util.Set;

import processCalculus.ExtendedProcess;
import processCalculus.Trace;

public abstract class Traces {
	
	// static fields
	
	// static methods
	public static Set<ExtendedProcess> getStates(Set<Trace> T) {
		Set<ExtendedProcess> S = new LinkedHashSet<ExtendedProcess>();
		for (Trace t : T) {
			for (ExtendedProcess s : t.getStates()) {
				S.add(s);
			}
		}
		return S;
	}

}
