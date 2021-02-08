package modelChecker;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import processCalculus.ExtendedProcess;
import processCalculus.State;
import processCalculus.Trace;
import properties.TemporalFormula;
import properties.StaticFormula;

public abstract class Verifier {
	
	// class methods
	public static Witness verify(TemporalFormula phi, ExtendedProcess A) throws Exception {
		// build all traces from P
		Set<Trace> T = A.buildTraces(new Trace());
		// collect all static formulas from phi
		//TODO normalize the formula
		TemporalFormula phi_norm = phi.normalize();
		Set<StaticFormula> F = phi_norm.getStatic();
		// F.addAll(phi_norm.getNegatedStatic());
		// collect all states
		Set<State> S = new LinkedHashSet<State>();
		for (Trace t : T) {
			S.addAll(t.getSigmas());
		}
		// create array for static properties
		Witness[][] B = new Witness[S.size()][F.size()];
		// temporary Lists
		List<State> lS = new ArrayList<State>(S);
		List<StaticFormula> lF = new ArrayList<StaticFormula>(F);
		// go through all states and formulas
		for (State s : lS) {
			for (StaticFormula f : lF) {
				Witness tmp = checkStatic(f, s);
				if (!tmp.getBool()) {
					String shortest = tmp.shortestTrace(T);
					tmp.setTrace(shortest);
				}
				B[lS.indexOf(s)][lF.indexOf(f)] = tmp;
			}
		}
		// check the phi for all traces
		for (Trace t : T) {
			Witness tmp = checkTemporal(T, B, phi_norm, t, 0, lS, lF);
			if (!tmp.getBool()) {
				return tmp;
			}
		}
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
			tmp1 = B[lS.indexOf(t0.getState(i).getSigma())][lF.indexOf(phi.getDelta().negate())];
			if (tmp1.getBool()) {
				// property does not hold locally
				return tmp1;
			}
			q = t0.getState(i).getSigma().getQueries().size();
			tmp2 = new Witness(false);
			for (int j = 0; j < i; j++) {
				if (t0.getState(j).getSigma().getQueries().size() < q) {
					tmp2 = B[lS.indexOf(t0.getState(j).getSigma())][lF.indexOf(phi.getDelta().negate())];
					if (!tmp2.getBool()) {
						// property holds for local state and some prior state j
						return tmp2;
					}
				}
			}
			// property did not hold in the past
			return tmp2;
		}
		return tmp1;
	}

}
