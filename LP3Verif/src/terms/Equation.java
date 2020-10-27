package terms;

public class Equation {
	
	// class fields
	private Name lhs;
	private Term rhs;
	
	// constructor
	public Equation(Name lhs, Term rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
		Equation other = (Equation) obj;
		if (lhs == null) {
			if (other.lhs != null)
				return false;
		} else if (!lhs.equals(other.lhs))
			return false;
		if (rhs == null) {
			if (other.rhs != null)
				return false;
		} else if (!rhs.equals(other.rhs))
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		return "" + lhs + " = " + rhs;
	}

	// getter and setter
	public Name getLhs() {
		return lhs;
	}

	public Term getRhs() {
		return rhs;
	}

}
