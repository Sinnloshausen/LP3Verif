package modelChecker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import processCalculus.ExtendedProcess;
import processCalculus.State;
import processCalculus.Trace;
import properties.TemporalFormula;
import utils.StateProp;
import properties.StaticFormula;

public class Verifier {

	// class fields
	private Map<StateProp, Witness> cache;
	private Set<Trace> T;
	private Set<State> S;

	// Constructor
	public Verifier(ExtendedProcess A, Map<StateProp, Witness> cache) {
		this.cache = cache;
		// build all traces from Process A
		T = A.buildTraces(new Trace());
		// collect all states
		S = new LinkedHashSet<State>();
		for (Trace t : T) {
			S.addAll(t.getSigmas());
		}
	}
	
	// small Constructor
	public Verifier(ExtendedProcess A) {
		this(A, new LinkedHashMap<StateProp, Witness>());
	}

	// class methods
	public Witness verify(TemporalFormula phi) throws Exception {
		// measure time
		long startTime = System.nanoTime();
		// normalize the formula
		TemporalFormula phi_norm = phi.notNormalize(); // version without normalizing
		//TemporalFormula phi_norm = phi.normalize();
		// collect all static formulas from phi
		Set<StaticFormula> F = phi_norm.getStatic();
		// F.addAll(phi_norm.getNegatedStatic());
		// measure time
		long afterGetStaticTime = System.nanoTime();
		// create array for static properties
		Witness[][] B = new Witness[S.size()][F.size()];
		// temporary Lists
		List<State> lS = new ArrayList<State>(S);
		List<StaticFormula> lF = new ArrayList<StaticFormula>(F);
		// measure time
		long beforeStaticTime = System.nanoTime();
		// go through all states and formulas
		for (State s : lS) {
			for (StaticFormula f : lF) {
				Witness tmp;
				// check if static check is already cached
				StateProp pair = new StateProp(s,f);
				if (!cache.containsKey(pair)) {
					tmp = checkStatic(f, s);
					if (!tmp.getBool()) {
						String shortest = tmp.shortestTrace(T);
						tmp.setTrace(shortest);
					}
					// put result in cache
					cache.put(pair, tmp);
				} else {
					tmp = cache.get(pair);
				}
				B[lS.indexOf(s)][lF.indexOf(f)] = tmp;
			}
		}
		// measure time
		long afterStaticTime = System.nanoTime();
		// check the phi for all traces
		long stopTime;
		for (Trace t : T) {
			Witness tmp = checkTemporal(T, B, phi_norm, t, 0, lS, lF);
			if (!tmp.getBool()) {
				stopTime = System.nanoTime();
				System.out.println("Overall: " + (stopTime - startTime));
				System.out.println("GetStatic: " + (afterGetStaticTime - startTime));
				System.out.println("CheckStatic: " + (afterStaticTime - beforeStaticTime));
				System.out.println("CheckTemporal: " + (stopTime - afterStaticTime));
				return tmp;
			}
		}
		// measure time
		stopTime = System.nanoTime();
		System.out.println("Overall: " + (stopTime - startTime));
		System.out.println("GetStatic: " + (afterGetStaticTime - startTime));
		System.out.println("CheckStatic: " + (afterStaticTime - beforeStaticTime));
		System.out.println("CheckTemporal: " + (stopTime - afterStaticTime));
		return new Witness(true);
	}

	private static Witness checkStatic(StaticFormula delta, State s) throws Exception {
		//TODO test
		SmtHandler smt = new SmtHandler();
		return smt.verify(s, delta);
	}

	private static Witness checkTemporal(Set<Trace> T, Witness[][] B, TemporalFormula phi, Trace t0, int i, List<State> lS, List<StaticFormula> lF) {
		//TODO test
		Witness tmp1 = new Witness(false);
		Witness tmp2 = new Witness(false);
		int q = 0;
		switch (phi.getType()) {
		case STATIC:
			return B[lS.indexOf(t0.getState(i).getSigma())][lF.indexOf(phi.getDelta())];
		case CONT:
			tmp1 = B[lS.indexOf(t0.getState(i).getSigma())][lF.indexOf(phi.getDelta())];
			if (!tmp1.getBool()) {
				// property does not hold locally
				return tmp1;
			}
			q = t0.getState(i).getSigma().getQueries().size();
			tmp2 = new Witness(false);
			for (int j = 0; j < i; j++) {
				if (t0.getState(j).getSigma().getQueries().size() < q) {
					tmp2 = B[lS.indexOf(t0.getState(j).getSigma())][lF.indexOf(phi.getDelta())];
					if (tmp2.getBool()) {
						// property holds for local state and some prior state j
						return tmp2;
					}
				}
			}
			// property did not hold in the past
			return tmp2;
		case GLOBAL:
			for (int j = i; j < t0.length(); j++) {
				tmp1 = checkTemporal(T, B, phi.getPhi(), t0, j, lS, lF);
				if (!tmp1.getBool()) {
					// witness found
					return tmp1;
				}
			}
			// no witness
			return new Witness(true);
		case NEGATION:
			return checkTemporal(T, B, phi.getPhi().negate(), t0, i, lS, lF);
		case DISJUNCTION:
			tmp1 = checkTemporal(T, B, phi.getPhi(), t0, i, lS, lF);
			tmp2 = checkTemporal(T, B, phi.getPhi2(), t0, i, lS, lF);
			if (tmp1.getBool()) {
				return tmp1;
			} else if (tmp2.getBool()) {
				return tmp2;
			}
			return tmp1;
		case CONJUNCTION:
			tmp1 = checkTemporal(T, B, phi.getPhi(), t0, i, lS, lF);
			tmp2 = checkTemporal(T, B, phi.getPhi2(), t0, i, lS, lF);
			if (tmp1.getBool() && tmp2.getBool()) {
				return tmp1;
			} else if (!tmp2.getBool()) {
				return tmp2;
			}
			return tmp1;
		case FUTURE:
			for (int j = i; j < t0.length(); j++) {
				tmp1 = checkTemporal(T, B, phi.getPhi(), t0, j, lS, lF);
				if (tmp1.getBool()) {
					// property holds
					return tmp1;
				}
			}
			// witness
			return tmp1;
		case CONTINV:
			// negation-structure formula not for normal use
			tmp1 = new Witness(true);
			if (t0.getState(i).getSigma().getLast() == null) {
				return tmp1;
			}
			State tmp = new State(t0.getState(i).getSigma(), t0.getState(i).getSigma().getLast());
			// check the state with only the most recent query
			Witness tmp3 = B[lS.indexOf(tmp)][lF.indexOf(phi.getDelta().negate())];
			if (tmp3.getBool()) {
				// property does not hold locally
				return tmp3;
			}
			q = t0.getState(i).getSigma().getIndex();
			tmp2 = new Witness(false);
			for (int j = 0; j < i; j++) {
				// consider only states with an index lesser than q
				if (t0.getState(j).getSigma().getIndex() >= q) {
					return tmp1;
				}
				tmp2 = B[lS.indexOf(t0.getState(j).getSigma())][lF.indexOf(phi.getDelta().negate())];
				if (!tmp2.getBool()) {
					// property holds for local state and some prior state j
					return tmp2;
				}
			}
			// property did not hold in the past
			return tmp1;
		}
		return tmp1;
	}

}
