package properties;


public class StaticFormula extends Formula {

	// enum
	public enum FormulaType {
		KID, KLOC, KSERV, KT, NEGATION, DISJUNCTION, CONJUNCTION
	}

	// class fields
	private FormulaType type;
	private StaticFormula delta;
	private StaticFormula delta2;
	
	// full constructor (also DISJUNCTION)
	public StaticFormula(FormulaType type, StaticFormula delta, StaticFormula delta2) {
		this.type = type;
		this.delta = delta;
		this.delta2 = delta2;
	}
	
	// K constructor
	public StaticFormula(FormulaType type) {
		this(type, null, null);
	}
	
	// NEGATION constructor
	public StaticFormula(FormulaType type, StaticFormula delta) {
		this(type, delta, null);
	}

	// class methods
	public StaticFormula negate() {
		switch (type) {
		case CONJUNCTION:
			return new StaticFormula(FormulaType.DISJUNCTION, delta.negate(), delta2.negate());
		case DISJUNCTION:
			return new StaticFormula(FormulaType.CONJUNCTION, delta.negate(), delta2.negate());
		case KID:
			// fall through
		case KLOC:
			// fall through
		case KSERV:
			// fall through
		case KT:
			return new StaticFormula(FormulaType.NEGATION, this);
		case NEGATION:
			return delta;
		}
		return null;
	}
	
	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delta == null) ? 0 : delta.hashCode());
		result = prime * result + ((delta2 == null) ? 0 : delta2.hashCode());
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
		StaticFormula other = (StaticFormula) obj;
		if (delta == null) {
			if (other.delta != null)
				return false;
		} else if (!delta.equals(other.delta))
			return false;
		if (delta2 == null) {
			if (other.delta2 != null)
				return false;
		} else if (!delta2.equals(other.delta2))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	// print
	@Override
	public String toString() {
		String fml = "";
		switch (type) {
		case KID:
			fml = "K_ID";
			break;
		case KLOC:
			fml = "K_LOC";
			break;
		case KSERV:
			fml = "K_SERV";
			break;
		case KT:
			fml = "K_T";
			break;
		case NEGATION:
			fml = "\u00AC " + delta;
			break;
		case DISJUNCTION:
			fml = delta + " V " + delta2;
			break;
		case CONJUNCTION:
			fml = delta + " ^ " + delta2;
			break;
		}
		return fml;
	}

	// getter setter
	public FormulaType getType() {
		return type;
	}

	public void setType(FormulaType type) {
		this.type = type;
	}

	public StaticFormula getDelta() {
		return delta;
	}

	public void setDelta(StaticFormula delta) {
		this.delta = delta;
	}

	public StaticFormula getDelta2() {
		return delta2;
	}

	public void setDelta2(StaticFormula delta2) {
		this.delta2 = delta2;
	}
}
