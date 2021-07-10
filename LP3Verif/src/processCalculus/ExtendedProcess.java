package processCalculus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import utils.Rules;
import utils.Rules.RedRule;

public class ExtendedProcess implements Process {

	// enums
	public enum ProcessType {
		PLAINSTATE, FINREPL
	}

	// class fields
	private ProcessType type;
	private PlainProcess P;
	private ExtendedProcess A;
	private State sigma;
	
	// full constructor
	public ExtendedProcess(ProcessType type, PlainProcess P, ExtendedProcess A, State sigma) {
		this.type = type;
		this.P = P;
		this.A = A;
		this.sigma = sigma;
	}

	// PLAINSTATE constructor
	public ExtendedProcess(ProcessType type, PlainProcess P, State sigma) {
		this(type, P, null, sigma);
	}

	// FINREPL constructor
	public ExtendedProcess(ProcessType type, ExtendedProcess A) {
		this(type, null, A, null);
	}

	// class methods
	public Set<Trace> buildTraces(Trace T) {
		// build all possible traces starting from this process
		T.addState(this);
		if (this.isNullP()) {
			// end of a trace reached
			return Collections.singleton(T);
		}
		ExtendedProcess tmp = null;
		List<ExtendedProcess> Candidates = new ArrayList<ExtendedProcess>();
		Set<Trace> traces = new LinkedHashSet<Trace>();
		for (RedRule r : RedRule.values()) {
			tmp = Rules.transition(this, r);
			if (tmp != null) {
				Candidates.add(tmp);
			}
		}
		for (ExtendedProcess candidate : Candidates) {
			traces.addAll(candidate.buildTraces(new Trace(T)));
		}
		return traces;
	}

	public boolean isNullP() {
		// check if the process is the terminating null process
		if (type == ProcessType.PLAINSTATE) {
			if (P.getType() == PlainProcess.ProcessType.NULL) {
				return true;
			}
		}
		return false;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((A == null) ? 0 : A.hashCode());
		result = prime * result + ((P == null) ? 0 : P.hashCode());
		result = prime * result + ((sigma == null) ? 0 : sigma.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedProcess other = (ExtendedProcess) obj;
		if (A == null) {
			if (other.A != null)
				return false;
		} else if (!A.equals(other.A))
			return false;
		if (P == null) {
			if (other.P != null)
				return false;
		} else if (!P.equals(other.P))
			return false;
		if (sigma == null) {
			if (other.sigma != null)
				return false;
		} else if (!sigma.equals(other.sigma))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		String proc = "";
		switch (type) {
		case PLAINSTATE:
			proc = P + "|" + sigma;
			break;
		case FINREPL:
			proc = A + "." + A;
			break;
		default:
			break;
		}
		return proc;
	}

	// getter and setter
	public ProcessType getType() {
		return type;
	}

	public void setType(ProcessType type) {
		this.type = type;
	}

	public ExtendedProcess getA() {
		return A;
	}

	public void setA(ExtendedProcess a) {
		A = a;
	}

	public PlainProcess getP() {
		return P;
	}

	public void setP(PlainProcess p) {
		P = p;
	}

	public State getSigma() {
		if (type == ProcessType.FINREPL) {
			return A.getSigma();
		}
		return sigma;
	}

	public void setSigma(State sigma) {
		this.sigma = sigma;
	}
}
