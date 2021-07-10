package terms;

public class Relation {
	
	// enum
	public enum RelationType {
		EQUAL, INT, SET
	}

	public enum IntRelation {
		LE, GE, LT, GT
	}

	public enum SetRelation {
		SUBE, SUPERE
	}
	
	// class fields
	private RelationType type;
	private IntRelation ir;
	private SetRelation sr;
	private Term t1;
	private Term t2;
	
	// full constructor
	public Relation(RelationType type, IntRelation ir, SetRelation sr, Term t1, Term t2) {
		this.type = type;
		this.ir = ir;
		this.sr = sr;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	// EQUAL constructor
	public Relation(RelationType type, Term t1, Term t2) {
		this(type, null, null, t1, t2);
	}
	
	// INT constructor
	public Relation(RelationType type, IntRelation ir, Term t1, Term t2) {
		this(type, ir, null, t1, t2);
	}
	
	// SET constructor
	public Relation(RelationType type, SetRelation sr, Term t1, Term t2) {
		this(type, null, sr, t1, t2);
	}

	// class methods
	public String makeSMT(int i) {
		String statement = "";
		switch (type) {
		case EQUAL:
			statement = "(= " + t1 + " " + t2 + ")";
			break;
		case INT:
			switch (ir) {
			case GE:
				statement = "(>= " + t1 + " " + t2 + ")";
				break;
			case GT:
				statement = "(> " + t1 + " " + t2 + ")";
				break;
			case LE:
				statement = "(<= " + t1 + " " + t2 + ")";
				break;
			case LT:
				statement = "(< " + t1 + " " + t2 + ")";
				break;
			}
			break;
		case SET:
			switch (sr) {
			case SUBE:
				statement = "(subset " + t1 + " " + t2 + ")";
				break;
			case SUPERE:
				statement = "(subset " + t2 + " " + t1 + ")";
				break;
			}
			break;
		}
		return statement;
	}

	// equal
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ir == null) ? 0 : ir.hashCode());
		result = prime * result + ((sr == null) ? 0 : sr.hashCode());
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
		Relation other = (Relation) obj;
		if (ir != other.ir)
			return false;
		if (sr != other.sr)
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
		case EQUAL:
			res = t1.toString() + " = " + t2;
			break;
		case INT:
			switch (ir) {
			case GE:
				res = t1.toString() + " >= " + t2;
				break;
			case GT:
				res = t1.toString() + " > " + t2;
				break;
			case LE:
				res = t1.toString() + " <= " + t2;
				break;
			case LT:
				res = t1.toString() + " < " + t2;
				break;
			}
			break;
		case SET:
			switch (sr) {
			case SUBE:
				res = t1.toString() + " \u2286 " + t2;
				break;
			case SUPERE:
				res = t1.toString() + " \u2287 " + t2;
				break;
			}
			break;
		}
		return res;
	}

}
