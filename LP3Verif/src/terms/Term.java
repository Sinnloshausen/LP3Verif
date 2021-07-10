package terms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Term {

	// enum
	public enum TermType {
		NAME, RESNAME, FUNC
	}

	// class fields
	private TermType type;
	private Name n;
	private ReservedName rn;
	private Function func;
	private List<Term> terms;

	// full constructor
	public Term(TermType type, Name n, ReservedName rn, Function func, List<Term> terms) {
		this.type = type;
		this.n = n;
		this.rn = rn;
		this.func = func;
		this.terms = terms;
	}

	// NAME constructor
	public Term(TermType type, Name n) {
		this(type, n, null, null, new ArrayList<Term>());
	}

	// RESNAME constructor
	public Term(TermType type, ReservedName rn) {
		this(type, null, rn, null, new ArrayList<Term>());
	}

	// FUNC constructor
	public Term(TermType type, Function func, List<Term> terms) {
		this(type, null, null, func, terms);
	}
	
	// copy constructor
	public Term(Term old) {
		this(old.getType(), old.getN(), old.getRn(), old.getFunc(), new ArrayList<>(old.getTerms()));
	}

	// class methods
	public Term update(Set<Equation> equations) {
		// replace all names in the term by the rhs of the corresponding equation
		Term res = new Term(this);
		switch (type) {
		case NAME:
			for (Equation e : equations) {
				if (e.getLhs().equals(n)) {
					res = new Term(e.getRhs());
					break;
				}
			}
			break;
		case FUNC:
			List<Term> L = new ArrayList<Term>();
			terms.forEach((t) -> L.add(t.update(equations)));
			res = new Term(type, func, L);
			break;
		case RESNAME:
			break;
		}
		return res;
	}

	public String makeSMT() {
		// recursively create smt-function syntax
		String smt = "";
		switch (type) {
		case FUNC:
			smt += "(" + func;
			for (Term t : terms) {
				smt +=  " " + t.makeSMTf();
			}
			smt += ")";
			break;
		case NAME:
			smt += this;
			break;
		case RESNAME:
			switch (rn.getRn()) {
			case PID:
				// fall through
			case LOC:
				// fall through
			case SERV:
				// fall through
			case T:
				smt += "(singleton " + this + ")";
				break;
			case LOCS:
				// fall through
			case TS:
				// fall through
			case PIDS:
				// fall through
			case LOCI:
				// fall through
			case PIDI:
				// fall through
			case SERVI:
				// fall through
			case TI:
				// fall through
			case SERVS:
				smt += this;
				break;
			}
			break;
		}
		return smt;
	}

	private String makeSMTf() {
		// only for function terms
		String smt = "";
		switch (type) {
		case FUNC:
			smt += "(" + func;
			for (Term t : terms) {
				smt +=  " " + t.makeSMTf();
			}
			smt += ")";
			break;
		case NAME:
			smt += this;
			break;
		case RESNAME:
			switch (rn.getRn()) {
			case PID:
				smt += "(singleton " + this + ")";
				break;
			case LOC:
				// fall through
			case SERV:
				// fall through
			case T:
				// fall through
			case LOCS:
				// fall through
			case TS:
				// fall through
			case PIDS:
				// fall through
			case LOCI:
				// fall through
			case PIDI:
				// fall through
			case SERVI:
				// fall through
			case TI:
				// fall through
			case SERVS:
				smt += this;
				break;
			}
			break;
		}
		return smt;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((func == null) ? 0 : func.hashCode());
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		result = prime * result + ((rn == null) ? 0 : rn.hashCode());
		result = prime * result + ((terms == null) ? 0 : terms.hashCode());
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
		Term other = (Term) obj;
		if (func == null) {
			if (other.func != null)
				return false;
		} else if (!func.equals(other.func))
			return false;
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		if (rn == null) {
			if (other.rn != null)
				return false;
		} else if (!rn.equals(other.rn))
			return false;
		if (terms == null) {
			if (other.terms != null)
				return false;
		} else if (!terms.equals(other.terms))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		String trm = "";
		switch (type) {
		case NAME:
			trm = n.toString();
			break;
		case RESNAME:
			trm = rn.toString();
			break;
		case FUNC:
			trm = func + "(";
			for (Term t : terms) {
				trm += t + ", ";
			}
			// replace last two chars with ')'
			trm = trm.substring(0, trm.length()-2) + ")";
			break;
		default:
			break;
		}
		return trm;
	}

	// getter setter
	public TermType getType() {
		return type;
	}
	
	public Name getN() {
		return n;
	}
	
	public ReservedName getRn() {
		return rn;
	}
	
	public Function getFunc() {
		return func;
	}
	
	public List<Term> getTerms() {
		return terms;
	}
}
