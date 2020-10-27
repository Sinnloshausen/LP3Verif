package terms;

public class Predicate {
	
	// enum
	public enum PredicateType {
		K_USERS, DUMMIES, L_DIVERS, S_DIVERS, RELATION
	}
	
	// class fields
	private PredicateType type;
	private Relation r;
	private Term t1;
	private Term t2;
	
	// RELATION constructor
	public Predicate(PredicateType type, Relation r, Term t1, Term t2) {
		this.type = type;
		this.r = r;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	// FLAG constructor
	public Predicate(PredicateType type) {
		this(type, null, null, null);
	}
	
	// class methods
	public boolean isProp() {
		return type != PredicateType.RELATION;
	}

	public String makeSMT(int i) {
		// TODO finish and test
		String ret = "";
		switch (type) {
		case DUMMIES:
			// the same dummies
			ret = "(and (= pid11 pid21) (= pid12 pid22) (= pid13 pid23) (= pid14 pid24))";
			break;
		case K_USERS:
			//TODO irgendwas zu asserten??
			ret = "true";
			break;
		case L_DIVERS:
			// region divers
			ret = "(> (card R) 1)";
			break;
		case RELATION:
			//TODO irgendwas zu asserten??
			ret = r.makeSMT(i);
			break;
		case S_DIVERS:
			// service divers
			ret = "(> (card S) 1)";
			break;
		}
		return ret;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((r == null) ? 0 : r.hashCode());
		result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
		result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
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
		Predicate other = (Predicate) obj;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		if (t1 == null) {
			if (other.t1 != null)
				return false;
		} else if (!t1.equals(other.t1))
			return false;
		if (t2 == null) {
			if (other.t2 != null)
				return false;
		} else if (!t2.equals(other.t2))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		String res = "";
		switch (type) {
		case DUMMIES:
			// fall through
		case K_USERS:
			// fall through
		case L_DIVERS:
			// fall through
		case S_DIVERS:
			res = type.name();
			break;
		case RELATION:
			res = r.toString();
			break;
		}
		return res;
	}
	
	// getter / setter
	public PredicateType getType() {
		return type;
	}
	
}
