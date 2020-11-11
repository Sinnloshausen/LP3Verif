package properties;

import java.util.LinkedHashSet;
import java.util.Set;

public class EpistemicFormula extends Formula {

	// enum
	public enum FormulaType {
		STATIC, CONT, NEGATION, DISJUNCTION, CONJUNCTION, GLOBAL, FUTURE, CONTINV
	}
	
	// class fields
	private FormulaType type;
	private StaticFormula delta;
	private EpistemicFormula phi;
	private EpistemicFormula phi2;
	
	// full constructor
	public EpistemicFormula(FormulaType type, StaticFormula delta, EpistemicFormula phi, EpistemicFormula phi2) {
		this.type = type;
		this.delta = delta;
		this.phi = phi;
		this.phi2 = phi2;
	}
	
	// STATIC/CONT constructor
	public EpistemicFormula(FormulaType type, StaticFormula delta) {
		this(type, delta, null, null);
	}
	
	// NEGATION/GLOBAL constructor
	public EpistemicFormula(FormulaType type, EpistemicFormula phi) {
		this(type, null, phi, null);
	}
	
	// DISJUNCTION/CONJUNCTION constructor
	public EpistemicFormula(FormulaType type, EpistemicFormula phi, EpistemicFormula phi2) {
		this(type, null, phi, phi2);
	}
	
	// class methods
	public Set<StaticFormula> getStatic() {
		Set<StaticFormula> F = new LinkedHashSet<StaticFormula>();
		switch (type) {
		case CONT:
			// fall through
		case CONTINV:
			// fall through
		case STATIC:
			F.add(delta);
			break;
		case GLOBAL:
			// fall through
		case FUTURE:
			// fall through
		case NEGATION:
			F.addAll(phi.getStatic());
			break;
		case CONJUNCTION:
			// fall through
		case DISJUNCTION:
			F.addAll(phi.getStatic());
			F.addAll(phi2.getStatic());
			break;
		}
		return F;
	}

	public Set<StaticFormula> getNegatedStatic() {
		Set<StaticFormula> F = new LinkedHashSet<StaticFormula>();
		switch (type) {
		case CONT:
			// fall through
		case CONTINV:
			// fall through
		case STATIC:
			F.add(delta.negate());
			break;
		case GLOBAL:
			// fall through
		case FUTURE:
			// fall through
		case NEGATION:
			F.addAll(phi.getNegatedStatic());
			break;
		case CONJUNCTION:
			// fall through
		case DISJUNCTION:
			F.addAll(phi.getNegatedStatic());
			F.addAll(phi2.getNegatedStatic());
			break;
		}
		return F;
	}

	public EpistemicFormula negate() {
		// TODO test
		switch (type) {
		case CONJUNCTION:
			return new EpistemicFormula(FormulaType.DISJUNCTION, phi.negate(), phi2.negate());
		case DISJUNCTION:
			return new EpistemicFormula(FormulaType.CONJUNCTION, phi.negate(), phi2.negate());
		case NEGATION:
			return phi;
		case STATIC:
			return new EpistemicFormula(FormulaType.STATIC, delta.negate());
		case CONT:
			return new EpistemicFormula(FormulaType.CONTINV, delta);
		case GLOBAL:
			return new EpistemicFormula(FormulaType.FUTURE, phi.negate());
		case FUTURE:
			return new EpistemicFormula(FormulaType.GLOBAL, phi.negate());
		case CONTINV:
			break;
		}
		return null;
	}
	
	// getter setter
	public FormulaType getType() {
		return type;
	}
	
	public StaticFormula getDelta() {
		return delta;
	}
	
	public EpistemicFormula getPhi() {
		return phi;
	}
	
	public EpistemicFormula getPhi2() {
		return phi2;
	}

	public void setPhi(EpistemicFormula phi) {
		this.phi = phi;
	}

	public void setPhi2(EpistemicFormula phi2) {
		this.phi2 = phi2;
	}

	public void setDelta(StaticFormula delta) {
		this.delta = delta;
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delta == null) ? 0 : delta.hashCode());
		result = prime * result + ((phi == null) ? 0 : phi.hashCode());
		result = prime * result + ((phi2 == null) ? 0 : phi2.hashCode());
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
		EpistemicFormula other = (EpistemicFormula) obj;
		if (delta == null) {
			if (other.delta != null)
				return false;
		} else if (!delta.equals(other.delta))
			return false;
		if (phi == null) {
			if (other.phi != null)
				return false;
		} else if (!phi.equals(other.phi))
			return false;
		if (phi2 == null) {
			if (other.phi2 != null)
				return false;
		} else if (!phi2.equals(other.phi2))
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
		case STATIC:
			fml = delta.toString();
			break;
		case CONT:
			fml = "Cont " + delta;
			break;
		case NEGATION:
			fml = "\u00AC " + phi;
			break;
		case DISJUNCTION:
			fml = phi + " V " + phi2;
			break;
		case GLOBAL:
			fml = "G " + phi;
			break;
		case FUTURE:
			fml = "F " + phi;
			break;
		case CONJUNCTION:
			fml = phi + " ^ " + phi2;
			break;
		case CONTINV:
			break;
		}
		return fml;
	}
}
