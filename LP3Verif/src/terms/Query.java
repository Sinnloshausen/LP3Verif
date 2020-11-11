package terms;

import java.util.LinkedHashSet;
import java.util.Set;

public class Query {
	
	// class fields
	private Term G;
	private Term R;
	private Term S;
	private Term F;
	private Set<Predicate> props;
	
	// constructor
	public Query(Term G, Term R, Term S, Term F, Set<Predicate> props) {
		this.G = G;
		this.R = R;
		this.S = S;
		this.F = F;
		this.props = props;
	}
	
	// short constructor
	public Query(Term G, Term R, Term S, Term F) {
		this(G, R, S, F, new LinkedHashSet<Predicate>());
	}
	
	// class methods
	public Query update(Set<Equation> equations, Set<Predicate> props) {
		// update the individual query fields
		Query q = new Query(G.update(equations), R.update(equations), S.update(equations), F.update(equations), props);
		return q;
	}

	public String makeSMTProp(int i) {
		// TODO more to do?
		String statement = "true";
		if (props.size() == 1) {
			for (Predicate p : props) {
				switch (p.getType()) {
				case DUMMIES:
					break;
				case K_USERS:
					break;
				case L_DIVERS:
					statement = "(> (card R" + i + ") 1)";
					break;
				case RELATION:
					statement = p.makeSMT(i);
					break;
				case S_DIVERS:
					statement = "(> (card S" + i + ") 1)";
					break;
				}
			}
		} else if (props.size() >= 2) {
			statement = "(and";
			for (Predicate p : props) {
				switch (p.getType()) {
				case DUMMIES:
					break;
				case K_USERS:
					break;
				case L_DIVERS:
					statement += " (> (card R" + i + ") 1)";
					break;
				case RELATION:
					statement = p.makeSMT(i);
					break;
				case S_DIVERS:
					statement += " (> (card S" + i + ") 1)";
					break;
				}
			}
			statement += ")";
		}
		return statement;
	}

	public String makeSMT(int i) {
		String statement = "(and (= G" + i + " ";
		statement += G.makeSMT() + ") ";
		statement += "(= R" + i + " " + R.makeSMT() + ") ";
		statement += "(= S" + i + " " + S.makeSMT() + ") ";
		statement += "(= F" + i + " " + F.makeSMT() + "))";
		return statement;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((F == null) ? 0 : F.hashCode());
		result = prime * result + ((G == null) ? 0 : G.hashCode());
		result = prime * result + ((R == null) ? 0 : R.hashCode());
		result = prime * result + ((S == null) ? 0 : S.hashCode());
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
		Query other = (Query) obj;
		if (F == null) {
			if (other.F != null)
				return false;
		} else if (!F.equals(other.F))
			return false;
		if (G == null) {
			if (other.G != null)
				return false;
		} else if (!G.equals(other.G))
			return false;
		if (R == null) {
			if (other.R != null)
				return false;
		} else if (!R.equals(other.R))
			return false;
		if (S == null) {
			if (other.S != null)
				return false;
		} else if (!S.equals(other.S))
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		return "(" + G + ", " + R + ", " + S + ", " + F + ")";
	}
	
}
