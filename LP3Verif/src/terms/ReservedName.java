package terms;

public class ReservedName {
	
	// enum
	public enum ResNames {
		PID {
			@Override
			public String toString() {
				return "pid";
			}
		}, PIDS {
			@Override
			public String toString() {
				return "pids";
			}
		}, LOC {
			@Override
			public String toString() {
				return "loc";
			}
		}, SERV {
			@Override
			public String toString() {
				return "serv";
			}
		}, T {
			@Override
			public String toString() {
				return "t";
			}
		}, TS {
			@Override
			public String toString() {
				return "ts";
			}
		}, LOCS {
			@Override
			public String toString() {
				return "locs";
			}
		}, SERVS {
			@Override
			public String toString() {
				return "servs";
			}
		}, PIDI {
			@Override
			public String toString() {
				return "pid_i";
			}
		}, LOCI {
			@Override
			public String toString() {
				return "loc_i";
			}
		}, SERVI {
			@Override
			public String toString() {
				return "serv_i";
			}
		}, TI {
			@Override
			public String toString() {
				return "t_i";
			}
		}
	}
	
	// class fields
	private ResNames rn;

	public ReservedName(ResNames rn) {
		this.rn = rn;
	}
	
	// equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rn == null) ? 0 : rn.hashCode());
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
		ReservedName other = (ReservedName) obj;
		if (rn != other.rn)
			return false;
		return true;
	}
	
	// print
	@Override
	public String toString() {
		return rn.toString();
	}

	// getter and setter
	public ResNames getRn() {
		return rn;
	}
}
