package processCalculus;

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
	
	// full constructor
	public State(Set<Query> queries, Set<Equation> equations, Set<Predicate> propositions) {
		this.queries = queries;
		this.equations = equations;
		this.propositions = propositions;
	}
	
	// copy constructor
	public State(State old) {
		this(new LinkedHashSet<>(old.getQueries()), new LinkedHashSet<>(old.getEquations()), new LinkedHashSet<>(old.getProps()));
	}
	
	// modified copy constructor prop
	public State(State old, Predicate prop) {
		this(new LinkedHashSet<>(old.getQueries()), new LinkedHashSet<>(old.getEquations()), Sets.addElem(old.getProps(), prop));
	}
	
	// modified copy constructor equation
	public State(State old, Equation e) {
		this(new LinkedHashSet<>(old.getQueries()), Sets.addElem(old.getEquations(), e), new LinkedHashSet<>(old.getProps()));
	}

	// modified copy constructor update equation 
	public State(State old, Equation e, Name n) {
		this(new LinkedHashSet<>(old.getQueries()), replEqu(old.getEquations(), e, n), new LinkedHashSet<>(old.getProps()));
	}

	// modified copy constructor query
	public State(State old, Query query) {
		this(Sets.addElem(old.getQueries(), query.update(old.getEquations(), old.getProps())), new LinkedHashSet<Equation>(), new LinkedHashSet<Predicate>());
	}

	// modified copy constructor multi query
	public State(State old, MultiQuery multi) {
		this(Sets.union(old.getQueries(), multi.update(old.getEquations(), old.getProps())), new LinkedHashSet<Equation>(), new LinkedHashSet<Predicate>());
	}
	
	// empty constructor
	public State() {
		this(new LinkedHashSet<Query>(), new LinkedHashSet<Equation>(), new LinkedHashSet<Predicate>());
	}

	private static Set<Equation> replEqu(Set<Equation> E, Equation eq, Name n) {
		// TODO replace the one equation with lhf = n
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
		// TODO returns true if the equations already contain the name
		for (Equation e : equations) {
			if (e.getLhs().equals(name)) {
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
		result = prime * result + ((equations == null) ? 0 : equations.hashCode());
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

	public Set<Equation> getEquations() {
		return equations;
	}

}
