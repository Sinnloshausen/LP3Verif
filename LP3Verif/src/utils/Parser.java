package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import processCalculus.ExtendedProcess;
import processCalculus.PlainProcess;
import processCalculus.PlainProcess.ProcessType;
import processCalculus.State;
import properties.EpistemicFormula;
import properties.EpistemicFormula.FormulaType;
import properties.StaticFormula;
import terms.Function;
import terms.Function.FunctionType;
import terms.Function.IntFunction;
import terms.Function.LocFunction;
import terms.Function.SetFunction;
import terms.Name;
import terms.Predicate;
import terms.Query;
import terms.Predicate.PredicateType;
import terms.ReservedName;
import terms.ReservedName.ResNames;
import terms.Term;
import terms.Term.TermType;

public class Parser {

	//TODO parse a .lp3 file into a process and a set of properties

	// class fields
	private String filePath;
	private ExtendedProcess process;
	private String procName;
	private List<EpistemicFormula> properties;
	private List<String> propNames;

	public Parser(String filePath) {
		this.filePath = filePath;
		properties = new LinkedList<EpistemicFormula>();
		propNames = new LinkedList<String>();
	}

	public void parse() throws IOException {
		//TODO use a proper grammar/parser?

		// a bit hacky
		// Read data from file
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			// tmp 0 proc to be later replaced
			PlainProcess p0 = new PlainProcess(ProcessType.NULL);
			// work list with processes for each line
			List<PlainProcess> procs = new LinkedList<PlainProcess>();
			List<PlainProcess> finished = new LinkedList<PlainProcess>();

			// Read file line by line
			String line = "";
			while ((line = br.readLine()) != null) {
				// Parse line to identify process
				line = line.trim();
				if (line.equals("")) {
					// skip empty lines
					continue;
				}
				// for each line, create a process with the 0 process as placeholder sequences
				if (line.equals("!")) {
					// replication key word found
					PlainProcess p_rep = new PlainProcess(ProcessType.REPL, p0);
					procs.add(p_rep);
					continue;
				}
				if (line.substring(0, 2).equals("if")) {
					// if key word found
					Predicate pred = null;
					String p = line.substring(3).trim();
					for (PredicateType type : PredicateType.values()) {
						if (type.toString().equals(p)) {
							// boolean flag found
							pred = new Predicate(type);
							break;
						}
					}
					if (pred == null) {
						// relation found
						//TODO parse into relation object
						pred = parseRelation(p);
					}
					PlainProcess p_if = new PlainProcess(ProcessType.CONDITION, pred, p0, p0);
					procs.add(p_if);
					continue;
				}
				if (line.substring(0, 3).equals("end")) {
					// end key word found
					//TODO close the last opened construct
					PlainProcess last = p0;
					PlainProcess current = null;
					if (finished.containsAll(procs)) {
						// process end
						process = parseProc(finished.get(0));
						continue;
					}
					while (!procs.isEmpty()) {
						current = procs.get(procs.size()-1);
						if (finished.contains(current)) {
							// already finished construct
							finished.remove(finished.size()-1);
						} else if (current.getType() == ProcessType.COMP || current.getType() == ProcessType.QUERY) {
							current.setP(last);
						} else {
							if (current.getType() == ProcessType.REPL || current.getType() == ProcessType.CONDITION) {
								if (!current.getP().equals(p0)) {
									current.setQ(last);
								} else {
									current.setP(last);
								}
								procs.remove(procs.size()-1);
								break;
							}
						}
						last = current;
						procs.remove(procs.size()-1);
					}
					// end of construct
					finished.add(current);
					procs.add(current);
					continue;
				}
				if (line.substring(0, 4).equals("else")) {
					// else key word found
					PlainProcess last = p0;
					PlainProcess current = null;
					while (!procs.isEmpty()) {
						current = procs.get(procs.size()-1);
						if (finished.contains(current)) {
							// already finished construct
							finished.remove(finished.size()-1);
						} else if (current.getType() == ProcessType.COMP || current.getType() == ProcessType.QUERY) {
							current.setP(last);
						} else {
							if (current.getType() == ProcessType.REPL || current.getType() == ProcessType.CONDITION) {
								current.setP(last);
								procs.remove(procs.size()-1);
								break;
							}
						}
						last = current;
						procs.remove(procs.size()-1);
					}
					// end of construct
					//finished.add(current);
					procs.add(current);
					continue;
				}
				if (line.substring(0, 5).equals("while")) {
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
					continue;
				}
				if (line.substring(0, 5).equals("Query")) {
					// query key word found
					Term G = null;
					Term R = null;
					Term S = null;
					Term F = null;
					String[] parts = line.substring(6, line.length()-1).split(",");
					G = parseTerm(parts[0].trim());
					R = parseTerm(parts[1].trim());
					S = parseTerm(parts[2].trim());
					F = parseTerm(parts[3].trim());
					Query q = new Query(G, R, S, F);

					PlainProcess p_qry = new PlainProcess(ProcessType.QUERY, q, p0);
					procs.add(p_qry);
					continue;
				}
				if (line.substring(0, 7).equals("process")) {
					// process key word found
					procName = line.substring(7).trim();
					process = new ExtendedProcess(ExtendedProcess.ProcessType.PLAINSTATE, p0, new State());
					continue;
				}
				if (line.substring(0, 7).equals("Compute")) {
					// compute key word found
					Name name = null;
					Term term = null;
					String[] parts = line.substring(8, line.length()-1).split("=");
					String n = parts[0].trim();
					String t = parts[1].trim();
					name = new Name(n);
					term = parseTerm(t);

					PlainProcess p_comp = new PlainProcess(ProcessType.COMP, name, term, p0);
					procs.add(p_comp);
					continue;
				}
				if (line.substring(0, 8).equals("property")) {
					//TODO what about parentheses and precedence?
					// property key word found
					propNames.add(line.substring(9).trim());
					List<EpistemicFormula> props = new LinkedList<EpistemicFormula>();
					EpistemicFormula f1 = null;
					StaticFormula f0 = null;
					// read next line with property
					line = br.readLine();
					line = line.trim();
					String[] parts = line.split(" ");
					for (String s : parts) {
						// identify all parts
						if (s.startsWith("(")) {
							// dirty Parentheses
							int index = Arrays.asList(parts).indexOf(s)+1;
							String s1 = s.substring(1);
							String s2 = parts[index+1].substring(0,parts[index+1].length()-1);
							if (parts[index].equals("and")) {
								props.add(new EpistemicFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.CONJUNCTION, toStatic(s1), toStatic(s2))));
							} else if (parts[index].equals("or")) {
								props.add(new EpistemicFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.DISJUNCTION, toStatic(s1), toStatic(s2))));
							}
							break;
						}
						if (s.equals("G")) {
							props.add(new EpistemicFormula(FormulaType.GLOBAL, f1));
						} else if (s.equals("F")) {
							props.add(new EpistemicFormula(FormulaType.FUTURE, f1));
						} else if (s.equals("not")) {
							props.add(new EpistemicFormula(FormulaType.NEGATION, f1));
						} else if (s.equals("Cont")) {
							props.add(new EpistemicFormula(FormulaType.CONT, f0));
						} else if (s.equals("or")) {
							props.add(new EpistemicFormula(FormulaType.DISJUNCTION, f1, f1));
						} else if (s.equals("and")) {
							props.add(new EpistemicFormula(FormulaType.CONJUNCTION, f1, f1));
						} else if (s.equals("K_id")) {
							props.add(new EpistemicFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.KID)));
						} else if (s.equals("K_loc")) {
							props.add(new EpistemicFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.KLOC)));
						} else if (s.equals("K_serv")) {
							props.add(new EpistemicFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.KSERV)));
						} else if (s.equals("K_t")) {
							props.add(new EpistemicFormula(FormulaType.STATIC, new StaticFormula(StaticFormula.FormulaType.KT)));
						}
					}
					// now combine to one formula
					EpistemicFormula current = null;
					EpistemicFormula last = null;
					for (int i = props.size()-1; i>=0; --i) {
						current = props.get(i);
						// break if already filled
						if (current.getType() == FormulaType.STATIC && (current.getDelta().getType() == StaticFormula.FormulaType.DISJUNCTION || current.getDelta().getType() == StaticFormula.FormulaType.DISJUNCTION)) {
							last = current;
							continue;
						}
						switch (current.getType()) {
						case STATIC:
							break;
						case CONT:
							current.setDelta(last.getDelta());
							break;
						case FUTURE:
							// fall through
						case GLOBAL:
							// fall through
						case NEGATION:
							current.setPhi(last);
							break;
						case CONJUNCTION:
							// fall through
						case DISJUNCTION:
							current.setPhi2(last);
							current.setPhi(props.get(i-1));
						case CONTINV:
							// should not happen!
							break;
						}
						last = current;
					}
					properties.add(current);
					continue;
				}
				//TODO else
			}
		}
	}

	private StaticFormula toStatic(String s) {
		// create static epistemic property
		switch (s) {
		case "K_id":
			return new StaticFormula(StaticFormula.FormulaType.KID);
		case "K_loc":
			return new StaticFormula(StaticFormula.FormulaType.KLOC);
		case "K_serv":
			return new StaticFormula(StaticFormula.FormulaType.KSERV);
		case "K_t":
			return new StaticFormula(StaticFormula.FormulaType.KT);
		}
		return null;
	}

	private ExtendedProcess parseProc(PlainProcess finished) {
		// go through list and build extended process from plain processes
		return new ExtendedProcess(ExtendedProcess.ProcessType.PLAINSTATE, finished, new State());
	}

	private Predicate parseRelation(String p) {
		// TODO not high priority right now
		return null;
	}

	private Term parseTerm(String t) {
		// TODO test
		if (t.contains("(")) {
			// function detected
			Function fun = null;
			String[] parts = t.split("\\(");
			for (LocFunction f : LocFunction.values()) {
				if (parts[0].trim().equals(f.toString())) {
					fun = new Function(FunctionType.LOC, f);
					break;
				}
			}
			for (IntFunction f : IntFunction.values()) {
				if (parts[0].trim().equals(f.toString())) {
					fun = new Function(FunctionType.INT, f);
					break;
				}
			}
			for (SetFunction f : SetFunction.values()) {
				if (parts[0].trim().equals(f.toString())) {
					fun = new Function(FunctionType.SET, f);
					break;
				}
			}
			String[] terms = parts[1].trim().substring(0,parts[1].trim().length()-1).split(",");
			List<Term> tList = new ArrayList<Term>();
			for (String term : terms) {
				tList.add(parseTerm(term.trim()));
			}
			return new Term(TermType.FUNC, fun, tList);
		} else {
			// simple term
			for (ResNames rn : ResNames.values()) {
				if (t.equals(rn.toString())) {
					// reserved name
					return new Term(TermType.RESNAME, new ReservedName(rn));
				}
			}
			// name
			return new Term(TermType.NAME, new Name(t));
		}
	}

	//getter and setter
	public ExtendedProcess getProcess() {
		return process;
	}

	public List<EpistemicFormula> getProperties() {
		return properties;
	}

	public List<String> getPropNames() {
		return propNames;
	}

	public String getProcName() {
		return procName;
	}
}
