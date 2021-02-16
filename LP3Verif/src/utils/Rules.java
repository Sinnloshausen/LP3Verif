package utils;

import java.util.LinkedHashSet;
import java.util.Set;

import processCalculus.ExtendedProcess;
import processCalculus.ExtendedProcess.ProcessType;
import terms.Equation;
import processCalculus.PlainProcess;
import processCalculus.State;
import processCalculus.Trace;

public abstract class Rules {

	// enums
	public static enum RedRule {
		REPL, PARA1, PARA2, PARA3, PARA4, PARA5, PARA6, PARA7, PARA8, PARA9, PARA10, DO, END, THEN, ELSE, COMPN, COMPU, QURY, KQURY
	}

	// class methods
	public static ExtendedProcess transition(ExtendedProcess A, RedRule r) {
		ExtendedProcess p = null;
		switch (r) {
		case REPL:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.REPL) {
				p = new ExtendedProcess(ProcessType.FINREPL, new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), A.getSigma()));
			}
			break;
		case PARA1:
			if (A.getType() == ProcessType.FINREPL) {
				for (ExtendedProcess B : reduce(A.getA())) {
					// take the first option
					p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
					break;
				}
			}
			break;
		case PARA2:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take second option
					++i;
					if (i==2) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA3:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take third option
					++i;
					if (i==3) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA4:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take fourth option
					++i;
					if (i==4) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA5:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take fifth option
					++i;
					if (i==5) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA6:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take sixth option
					++i;
					if (i==6) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA7:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take seventh option
					++i;
					if (i==7) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA8:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take eighth option
					++i;
					if (i==8) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA9:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take ninth option
					++i;
					if (i==9) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case PARA10:
			if (A.getType() == ProcessType.FINREPL) {
				int i = 0;
				for (ExtendedProcess B : reduce(A.getA())) {
					// take tenth option
					++i;
					if (i==10) {
						p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getA().getP(), B.getSigma());
						break;
					}
				}
			}
			break;
		case THEN:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.CONDITION) {
				// check for proposition to add
				if (A.getP().getPredicate().isProp()) {
					p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), new State(A.getSigma(), A.getP().getPredicate()));
				} else {
					p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), new State(A.getSigma()));
				}
			}
			break;
		case ELSE:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.CONDITION) {
				p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getQ(), new State(A.getSigma()));
			}
			break;
		case DO:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.WHILE) {
				p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP().concat(A.getP().getQ()), new State(A.getSigma()));
			}
			break;
		case END:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.WHILE) {
				p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getQ(), new State(A.getSigma()));
			}
			break;
		case COMPN:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.COMP) {
				if (!A.getSigma().contains(A.getP().getName())) {
					p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), new State(A.getSigma(), new Equation(A.getP().getName(), A.getP().getT())));
				}
			}
			break;
		case COMPU:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.COMP) {
				if (A.getSigma().contains(A.getP().getName())) {
					p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), new State(A.getSigma(), new Equation(A.getP().getName(), A.getP().getT()), A.getP().getName()));
				}
			}
			break;
		case QURY:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.QUERY) {
				p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), new State(A.getSigma(), A.getP().getQuery(), true));
			}
			break;
		case KQURY:
			if (A.getType() == ProcessType.PLAINSTATE && A.getP().getType() == PlainProcess.ProcessType.KQUERY) {
				p = new ExtendedProcess(ProcessType.PLAINSTATE, A.getP().getP(), new State(A.getSigma(), A.getP().getMulti(), true));
			}
			break;
		}
		return p;
	}

	private static Set<ExtendedProcess> reduce(ExtendedProcess A) {
		// generate all traces and then return the final states
		Set<ExtendedProcess> finalStates = new LinkedHashSet<ExtendedProcess>();
		Set<Trace> traces = A.buildTraces(new Trace());
		for (Trace t : traces) {
			finalStates.add(t.getState(t.length()-1));
		}
		return finalStates;
	}

}
