package properties;

import java.util.LinkedHashSet;
import java.util.Set;

public class TemporalFormula extends Formula {

	// enum
	public enum FormulaType {
		STATIC, CONT, NEGATION, DISJUNCTION, CONJUNCTION, GLOBAL, FUTURE, CONTINV
	}
	
	// class fields
	private FormulaType type;
	private StaticFormula delta;
	private TemporalFormula phi;
	private TemporalFormula phi2;
	
	// full constructor
	public TemporalFormula(FormulaType type, StaticFormula delta, TemporalFormula phi, TemporalFormula phi2) {
		this.type = type;
		this.delta = delta;
		this.phi = phi;
		this.phi2 = phi2;
	}
	
	// STATIC/CONT constructor
	public TemporalFormula(FormulaType type, StaticFormula delta) {
		this(type, delta, null, null);
	}
	
	// NEGATION/GLOBAL constructor
	public TemporalFormula(FormulaType type, TemporalFormula phi) {
		this(type, null, phi, null);
	}
	
	// DISJUNCTION/CONJUNCTION constructor
	public TemporalFormula(FormulaType type, TemporalFormula phi, TemporalFormula phi2) {
		this(type, null, phi, phi2);
	}
	
	// class methods
	public Set<StaticFormula> getStatic() {
		Set<StaticFormula> F = new LinkedHashSet<StaticFormula>();
		switch (type) {
		case CONT:
			F.add(delta);
			F.add(delta.negate());
			break;
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

	public TemporalFormula negate() {
		switch (type) {
		case CONJUNCTION:
			return new TemporalFormula(FormulaType.DISJUNCTION, phi.negate(), phi2.negate());
		case DISJUNCTION:
			return new TemporalFormula(FormulaType.CONJUNCTION, phi.negate(), phi2.negate());
		case NEGATION:
			return phi;
		case STATIC:
			return new TemporalFormula(FormulaType.STATIC, delta.negate());
		case CONT:
			return new TemporalFormula(FormulaType.CONTINV, delta);
		case GLOBAL:
			return new TemporalFormula(FormulaType.FUTURE, phi.negate());
		case FUTURE:
			return new TemporalFormula(FormulaType.GLOBAL, phi.negate());
		case CONTINV:
			break;
		}
		return null;
	}

	public TemporalFormula normalize() {
		// this method returns an equivalent formula in normal form
		TemporalFormula norm = this;
		switch(this.type) {
		case FUTURE:
			// fall through
		case GLOBAL:
			return new TemporalFormula(this.getType(), phi.normalize());
		case NEGATION:
			if (this.getPhi().getType().equals(FormulaType.STATIC)) {
				StaticFormula a;
				StaticFormula b;
				TemporalFormula tmp1;
				TemporalFormula tmp2;
				switch(this.getPhi().getDelta().getType()) {
				case CONJUNCTION:
					a = this.getPhi().getDelta().getDelta().negate();
					b = this.getPhi().getDelta().getDelta2().negate();
					tmp1 = new TemporalFormula(FormulaType.STATIC, a);
					tmp2 = new TemporalFormula(FormulaType.STATIC, b);
					return new TemporalFormula(FormulaType.DISJUNCTION, tmp1, tmp2);
				case DISJUNCTION:
					a = this.getPhi().getDelta().getDelta().negate();
					b = this.getPhi().getDelta().getDelta2().negate();
					tmp1 = new TemporalFormula(FormulaType.STATIC, a);
					tmp2 = new TemporalFormula(FormulaType.STATIC, b);
					return new TemporalFormula(FormulaType.CONJUNCTION, tmp1, tmp2);
				case KID:
					// fall through
				case KLOC:
					// fall through
				case KSERV:
					// fall through
				case KT:
					return new TemporalFormula(FormulaType.STATIC, this.getPhi().getDelta().negate());
				default:
					break;
				}
			}
			return new TemporalFormula(this.getType(), phi.normalize());
		default:
			break;
		}
		return norm;
	}

	public TemporalFormula notNormalize() {
		// this method returns an equivalent formula not in normal form
		switch (type) {
		case DISJUNCTION:
			return new TemporalFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.DISJUNCTION, phi.notNormalize().getDelta(), phi2.notNormalize().getDelta()));
		case CONJUNCTION:
			return new TemporalFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.CONJUNCTION, phi.notNormalize().getDelta(), phi2.notNormalize().getDelta()));
		case NEGATION:
			return new TemporalFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.NEGATION, phi.notNormalize().getDelta()));
		case FUTURE:
			// fall through
		case GLOBAL:
			return new TemporalFormula(type, phi.notNormalize());
		case STATIC:
			break;
		case CONT:
			break;
		case CONTINV:
			break;
		}
		return this;
	}

	// getter setter
	public FormulaType getType() {
		return type;
	}
	
	public StaticFormula getDelta() {
		return delta;
	}
	
	public TemporalFormula getPhi() {
		return phi;
	}
	
	public TemporalFormula getPhi2() {
		return phi2;
	}

	public void setPhi(TemporalFormula phi) {
		this.phi = phi;
	}

	public void setPhi2(TemporalFormula phi2) {
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
		TemporalFormula other = (TemporalFormula) obj;
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
