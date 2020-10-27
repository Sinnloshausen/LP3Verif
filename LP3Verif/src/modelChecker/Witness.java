package modelChecker;

public class Witness {
	
	// class fields
	private boolean bool;
	private String witness;
	
	// full constructor
	public Witness(boolean bool, String witness) {
		this.bool = bool;
		this.witness = witness;
	}
	
	// witness free constructor
	public Witness(boolean bool) {
		this(bool, null);
	}

	// class methods
	public String makeQuery() {
		String w = "";
		if (!bool) {
			w = "Witness:\n";
			w += "Representative User: " + witness.substring(1, 31) + "\n";
			w += "Other User Location: " + witness.substring(32, 42) + "\n";
			w += "Query: " + witness.substring(42, witness.length()-1) + "\n";
			return w;
		}
		return w;
	}
	
	// print
	@Override
	public String toString() {
		if (bool) {
			return "true";
		} else {
			return witness;
		}
	}

	// getter and setter
	public boolean getBool() {
		return bool;
	}
	
	public String getWitness() {
		return witness;
	}

}
