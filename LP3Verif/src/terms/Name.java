package terms;

import terms.Term.TermType;

public class Name {
	
	// class fields
	private String name;
	
	// constructor
	public Name(String name) {
		this.setName(name);
	}

	// class methods
	public Term toTerm() {
		// create a term from this name
		return new Term(TermType.NAME, this);
	}

	// getter setter
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Name other = (Name) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		return name;
	}

}
