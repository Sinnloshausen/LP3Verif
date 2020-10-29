package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import processCalculus.PlainProcess;
import processCalculus.PlainProcess.ProcessType;
import properties.EpistemicFormula;
import terms.Name;
import terms.Predicate;
import terms.Predicate.PredicateType;
import terms.Term;

public class Parser {

	//TODO parse a .lp3 file into a process and a set of properties

	// class fields
	private String filePath;
	private String fileName;
	private PlainProcess process;
	private List<EpistemicFormula> properties;

	public Parser(String filePath, String fileName) {
		this.fileName = fileName;
		this.filePath = filePath;
	}

	public void parse() throws IOException {
		//TODO use a proper grammar/parser?

		// a bit hacky
		// Read data from file
		try (BufferedReader br = new BufferedReader(new FileReader(filePath+fileName))) {

			// Protocol to collect
			PlainProcess proc = null;
			String procName = "";
			// List of properties to collect
			List<EpistemicFormula> props = new ArrayList<EpistemicFormula>();
			
			// tmp 0 proc to be later replaced
			PlainProcess p0 = new PlainProcess(ProcessType.NULL);
			// work list with processes for each line
			List<PlainProcess> procs = new ArrayList<PlainProcess>();

			// Read file line by line
			String line = "";
			while ((line = br.readLine()) != null) {
				// Parse line to identify process
				line = line.trim();
				if (line.substring(0, 6).equals("process")) {
					// process key word found
					procName = line.substring(7);
					//TODO do more?
					continue;
				}
				// for each line, create a process with the 0 process as placeholder sequences
				if (line.equals("!")) {
					// replication key word found
					PlainProcess p_rep = new PlainProcess(ProcessType.REPL, p0);
					procs.add(p_rep);
				}
				if (line.substring(0, 1).equals("if")) {
					// if key word found
					Predicate pred = null;
					String p = line.substring(2);
					for (PredicateType type : PredicateType.values()) {
						if (type.name().equals(p)) {
							// boolean flag found
							pred = new Predicate(type);
							break;
						}
					}
					if (pred == null) {
						// relation found
						//TODO parse into relation object
					}
					PlainProcess p_if = new PlainProcess(ProcessType.CONDITION, pred, p0, p0);
					procs.add(p_if);
				}
				if (line.substring(0, 4).equals("while")) {
					// while key word found
					Predicate pred = null;
					String p = line.substring(2);
					for (PredicateType type : PredicateType.values()) {
						if (type.name().equals(p)) {
							// boolean flag found
							pred = new Predicate(type);
							break;
						}
					}
					if (pred == null) {
						// relation found
						//TODO parse into relation object
					}
					PlainProcess p_while = new PlainProcess(ProcessType.WHILE, pred, p0, p0);
					procs.add(p_while);
				}
				if (line.substring(0, 6).equals("Compute")) {
					// compute key word found
					Name name = null;
					Term term = null;
					String[] parts = line.split("=");
					String n = parts[0];
					String t = parts[1];
					name = new Name(n);
					term = parseTerm(t);

					PlainProcess p_comp = new PlainProcess(ProcessType.COMP, name, term, p0);
					procs.add(p_comp);
				}
				//TODO all possible keywords for lines
			}

			// TODO finalize the process and properties
		}
	}

	private Term parseTerm(String t) {
		// TODO Auto-generated method stub
		return null;
	}

	//getter and setter
	public PlainProcess getProcess() {
		return process;
	}

	public List<EpistemicFormula> getProperties() {
		return properties;
	}
}
