package terms;

public class Function {
	
	// enum
	public enum FunctionType {
		SET, LOC, INT
	}

	public enum LocFunction {
		MBB, DIST, MOVE
	}

	public enum SetFunction {
		UNION, INTER
	}

	public enum IntFunction {
		HASH, RAND
	}
	
	// class fields
	private FunctionType type;
	private SetFunction sf;
	private LocFunction lf;
	private IntFunction inf;
	
	// full constructor
	public Function(FunctionType type, SetFunction sf, LocFunction lf, IntFunction inf) {
		this.type = type;
		this.sf = sf;
		this.lf = lf;
		this.inf = inf;
	}
	
	// SET constructor
	public Function(FunctionType type, SetFunction sf) {
		this(type, sf, null, null);
	}
	
	// LOC constructor
	public Function(FunctionType type, LocFunction lf) {
		this(type, null, lf, null);
	}
	
	// INT constructor
	public Function(FunctionType type, IntFunction inf) {
		this(type, null, null, inf);
	}

	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lf == null) ? 0 : lf.hashCode());
		result = prime * result + ((sf == null) ? 0 : sf.hashCode());
		result = prime * result + ((inf == null) ? 0 : inf.hashCode());
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
		Function other = (Function) obj;
		if (lf != other.lf)
			return false;
		if (sf != other.sf)
			return false;
		if (inf != other.inf)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	// to string
	@Override
	public String toString() {
		String name = "";
		switch (type) {
		case LOC:
			name = lf.name();
			break;
		case SET:
			name = sf.name();
			break;
		case INT:
			name = inf.name();
			break;
		}
		return name;
	}

}
