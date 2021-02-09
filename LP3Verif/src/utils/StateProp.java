package utils;

import processCalculus.State;
import properties.StaticFormula;

public class StateProp {

	// class fields
	private State state;
	private StaticFormula property;
	
	// constructor
	public StateProp(State state, StaticFormula property) {
		this.state = state;
		this.property = property;
	}

	// print
	@Override
	public String toString() {
		return "<" + state + ", " + property + ">";
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		StateProp other = (StateProp) obj;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	// getter / setter
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public StaticFormula getProperty() {
		return property;
	}

	public void setProperty(StaticFormula property) {
		this.property = property;
	}
}
