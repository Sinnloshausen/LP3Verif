package processCalculus;

import terms.MultiQuery;
import terms.Name;
import terms.Predicate;
import terms.Query;
import terms.Term;

public class PlainProcess implements Process {

	// enum
	public enum ProcessType {
		NULL, REPL, CONDITION, WHILE, COMP, QUERY, KQUERY
	}

	// class fields
	private ProcessType type;
	private PlainProcess P;
	private PlainProcess Q;
	private Predicate p;
	private Name n;
	private Term T;
	private Query R;
	private MultiQuery M;

	// full constructor
	public PlainProcess(ProcessType type, PlainProcess P, PlainProcess Q, Predicate p, Name n, Term T, Query R, MultiQuery M) {
		this.type = type;
		this.P = P;
		this.Q = Q;
		this.p = p;
		this.n = n;
		this.T = T;
		this.R = R;
		this.M = M;
	}

	// NULL constructor
	public PlainProcess(ProcessType type) {
		this(type, null, null, null, null, null, null, null);
	}

	// REPL constructor
	public PlainProcess(ProcessType type, PlainProcess P) {
		this(type, P, null, null, null, null, null, null);
	}

	// CONDITION/WHILE constructor
	public PlainProcess(ProcessType type, Predicate p, PlainProcess P, PlainProcess Q) {
		this(type, P, Q, p, null, null, null, null);
	}

	// COMP constructor
	public PlainProcess(ProcessType type, Name n, Term T, PlainProcess P) {
		this(type, P, null, null, n, T, null, null);
	}

	// QUERY constructor
	public PlainProcess(ProcessType type, Query R, PlainProcess P) {
		this(type, P, null, null, null, null, R, null);
	}

	// KQUERY constructor
	public PlainProcess(ProcessType type, MultiQuery M, PlainProcess P) {
		this(type, P, null, null, null, null, null, M);
	}
	
	// copy constructor
	public PlainProcess(PlainProcess old) {
		this(old.getType(), new PlainProcess(old.getP()), new PlainProcess(old.getQ()), old.getPredicate(), old.getName(), old.getT(), old.getQuery(), old.getMulti());
	}

	// class methods
	
	// print
	@Override
	public String toString() {
		String proc = "";
		switch (type) {
		case NULL:
			proc = "0";
			break;
		case REPL:
			proc =  "!" + P;
			break;
		case CONDITION:
			proc = "if " + p + " then " + P + " else " + Q;
			break;
		case WHILE:
			proc = "while " + p + " do " + P + " then " + Q;
			break;
		case COMP:
			proc = "Compute(" + n + " = " + T + ")." + P;
			break;
		case QUERY:
			proc = "Query(" + R +  ")." + P;
			break;
		case KQUERY:
			proc = "kQuery(" + M +  ")." + P;
			break;
		default:
			break;
		}
		return proc;
	}

	// equals
	//TODO	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((M == null) ? 0 : M.hashCode());
		result = prime * result + ((P == null) ? 0 : P.hashCode());
		result = prime * result + ((Q == null) ? 0 : Q.hashCode());
		result = prime * result + ((R == null) ? 0 : R.hashCode());
		result = prime * result + ((T == null) ? 0 : T.hashCode());
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
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
		PlainProcess other = (PlainProcess) obj;
		if (M == null) {
			if (other.M != null)
				return false;
		} else if (!M.equals(other.M))
			return false;
		if (P == null) {
			if (other.P != null)
				return false;
		} else if (!P.equals(other.P))
			return false;
		if (Q == null) {
			if (other.Q != null)
				return false;
		} else if (!Q.equals(other.Q))
			return false;
		if (R == null) {
			if (other.R != null)
				return false;
		} else if (!R.equals(other.R))
			return false;
		if (T == null) {
			if (other.T != null)
				return false;
		} else if (!T.equals(other.T))
			return false;
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		if (p == null) {
			if (other.p != null)
				return false;
		} else if (!p.equals(other.p))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	public PlainProcess concat(PlainProcess tail) {
		// replace the null proc with "tail"
		PlainProcess p = null;
		switch (type) {
		case NULL:
			p = new PlainProcess(tail);
			break;
		case COMP:
			// fall through
		case KQUERY:
			// fall through
		case QUERY:
			// fall through
		case REPL:
			p = new PlainProcess(this);
			p.setP(p.getP().concat(tail));
			break;
		case WHILE:
			p = new PlainProcess(this);
			p.setQ(p.getQ().concat(tail));
			break;
		case CONDITION:
			p = new PlainProcess(this);
			p.setP(p.getP().concat(tail));
			p.setQ(p.getQ().concat(tail));
			break;
		}
		return p;
	}

	// getter setter
	public ProcessType getType() {
		return type;
	}

	public void setType(ProcessType type) {
		this.type = type;
	}

	public PlainProcess getP() {
		return P;
	}

	public void setP(PlainProcess p) {
		P = p;
	}

	public PlainProcess getQ() {
		return Q;
	}

	public void setQ(PlainProcess q) {
		Q = q;
	}

	public Name getName() {
		return n;
	}

	public void setN(Name n) {
		this.n = n;
	}

	public Term getT() {
		return T;
	}

	public void setT(Term T) {
		this.T = T;
	}
	
	public Predicate getPredicate() {
		return p;
	}

	public Query getQuery() {
		return R;
	}

	public MultiQuery getMulti() {
		return M;
	}

}
