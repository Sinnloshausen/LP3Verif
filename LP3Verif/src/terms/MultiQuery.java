package terms;

import java.util.HashSet;
import java.util.Set;

public class MultiQuery {
	
	// class fields
	private Set<Query> queries;
	
	public MultiQuery(Set<Query> queries) {
		this.queries = queries;
	}

	// class methods
	public Set<Query> update(Set<Equation> equations, Set<Predicate> props) {
		// update all queries
		Set<Query> M = new HashSet<Query>();
		for (Query q : queries) {
			M.add(q.update(equations, props));
		}
		return M;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queries == null) ? 0 : queries.hashCode());
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
		MultiQuery other = (MultiQuery) obj;
		if (queries == null) {
			if (other.queries != null)
				return false;
		} else if (!queries.equals(other.queries))
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		return "{" + queries + "}";
	}
	
}
