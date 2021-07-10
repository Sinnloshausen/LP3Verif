package processCalculus;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import terms.Equation;
import terms.MultiQuery;
import terms.Name;
import terms.Predicate;
import terms.Query;
import utils.Sets;

public class State {
	
	// class fields
	private Set<Query> queries;
	private Set<Equation> equations;
	private Set<Predicate> propositions;
	private int index;
	private Query last;
	
	// full constructor
	public State(Set<Query> queries, Set<Equation> equations, Set<Predicate> propositions, int index, Query last) {
		this.queries = queries;
		this.equations = equations;
		this.propositions = propositions;
		this.index = index;
		this.last = last;
	}
	
	// copy constructor
	public State(State old) {
		this(new LinkedHashSet<>(old.getQueries()), new LinkedHashSet<>(old.getEquations()), new LinkedHashSet<>(old.getProps()), old.getIndex(), old.getLast());
	}
	
	// copy constructor with only one query
	public State(State old, Query query) {
		this(new LinkedHashSet<>(Arrays.asList(query)), new LinkedHashSet<>(old.getEquations()), new LinkedHashSet<>(old.getProps()), old.getIndex(), query);
	}
	
	// modified copy constructor prop
	public State(State old, Predicate prop) {
		this(new LinkedHashSet<>(old.getQueries()), new LinkedHashSet<>(old.getEquations()), Sets.addElem(old.getProps(), prop), old.getIndex(), old.getLast());
	}
	
	// modified copy constructor equation
	public State(State old, Equation e) {
		this(new LinkedHashSet<>(old.getQueries()), Sets.addElem(old.getEquations(), e), new LinkedHashSet<>(old.getProps()), old.getIndex(), old.getLast());
	}

	// modified copy constructor update equation 
	public State(State old, Equation e, Name n) {
		this(new LinkedHashSet<>(old.getQueries()), replEqu(old.getEquations(), e, n), new LinkedHashSet<>(old.getProps()), old.getIndex(), old.getLast());
	}

	// modified copy constructor query
	public State(State old, Query query, boolean increment) {
		this(Sets.addElem(old.getQueries(), query.update(old.getEquations(), old.getProps())), new LinkedHashSet<Equation>(), new LinkedHashSet<Predicate>(), increment ? old.getIndex()+1 : old.getIndex(), query.update(old.getEquations(), old.getProps()));
	}

	// modified copy constructor multi query
	public State(State old, MultiQuery multi, boolean increment) {
		this(Sets.union(old.getQueries(), multi.update(old.getEquations(), old.getProps())), new LinkedHashSet<Equation>(), new LinkedHashSet<Predicate>(), increment ? old.getIndex()+1 : old.getIndex(), old.getLast());
	}
	
	// empty constructor
	public State() {
		this(new LinkedHashSet<Query>(), new LinkedHashSet<Equation>(), new LinkedHashSet<Predicate>(), 0, null);
	}

	private static Set<Equation> replEqu(Set<Equation> E, Equation eq, Name n) {
		// replace the one equation with lhf = n
		Set<Equation> S = null;
		for (Equation e : E) {
			if (e.getLhs().equals(n)) {
				S = new LinkedHashSet<>(E);
				S.remove(e);
				S.add(eq);
				break;
			}
		}
		return S;
	}

	// class methods
	public boolean contains(Name name) {
		// returns true if the equations already contain the name
		for (Equation e : equations) {
			if (e.getLhs().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((equations == null) ? 0 : equations.hashCode());
		result = prime * result + index;
		result = prime * result + ((last == null) ? 0 : last.hashCode());
		result = prime * result + ((propositions == null) ? 0 : propositions.hashCode());
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
		State other = (State) obj;
		if (equations == null) {
			if (other.equations != null)
				return false;
		} else if (!equations.equals(other.equations))
			return false;
		if (index != other.index)
			return false;
		if (last == null) {
			if (other.last != null)
				return false;
		} else if (!last.equals(other.last))
			return false;
		if (propositions == null) {
			if (other.propositions != null)
				return false;
		} else if (!propositions.equals(other.propositions))
			return false;
		if (queries == null) {
			if (other.queries != null)
				return false;
		} else if (!queries.equals(other.queries))
			return false;
		return true;
	}

	// equals version with partial-order reduction
	//@Override
	public boolean equalsPO(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
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
		return "(" + queries + ", " + equations + ", " + propositions + ")";
	}

	// getter and setter
	public Set<Predicate> getProps() {
		return propositions;
	}

	public Set<Query> getQueries() {
		return queries;
	}
	
	public void setQueries(Set<Query> queries) {
		this.queries = queries;
	}

	public Set<Equation> getEquations() {
		return equations;
	}

	public int getIndex() {
		return index;
	}
	
	public Query getLast() {
		return last;
	}
}
