package main;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import modelChecker.Verifier;
import modelChecker.Witness;
import processCalculus.ExtendedProcess;
import utils.ConfigReader;
import utils.Parser;
import properties.TemporalFormula;

public class Test {

	public static void main(String[] args) throws IOException {

		// start the program
		if (!ConfigReader.readConfig()) {
			// Could not find or read the config file
			System.err.println("Config file could not be read! Exiting...");
			System.exit(1);
		}

		// check at start whether arguments with path to file exist
		String path = "";
		Parser parser;
		if (args.length > 1) {
			if (args[0].equals("-lp3")) {
				path = args[1];
				System.out.println("Parsing .lp3 file...");
			}
		} else {
			System.out.println("Provide a full path to the .lp3 file you want to verify");
			Scanner scanIn = new Scanner(System.in);
			path = scanIn.nextLine();
			scanIn.close();
		}

		parser = new Parser(path);
		parser.parse();
		String procName = parser.getProcName();
		ExtendedProcess proc = parser.getProcess();
		System.out.println("The protocol " + procName + ": " + proc);
		List<TemporalFormula> properties = parser.getProperties();
		List<String> propNames = parser.getPropNames();
		for (int i = 0; i < propNames.size(); ++i) {
			System.out.println("The property " + propNames.get(i) + ": " + properties.get(i));
		}


//		//-------------- Alice Protocol #1 -------------------
//		// protocol
//		Predicate p = new Predicate(PredicateType.K_USERS);
//		Name R = new Name("R");
//		Function MBB = new Function(FunctionType.LOC, LocFunction.MBB);
//		List<Term> locs = Collections.singletonList(new Term(TermType.RESNAME, new ReservedName(ResNames.LOCS)));
//		Term T1 = new Term(TermType.FUNC, MBB, locs);
//		Name h = new Name("h");
//		Function hash = new Function(FunctionType.INT, IntFunction.HASH);
//		List<Term> ids = Collections.singletonList(new Term(TermType.RESNAME, new ReservedName(ResNames.PID)));
//		Term T2 = new Term(TermType.FUNC, hash, ids);
//		Term serv = new Term(TermType.RESNAME, new ReservedName(ResNames.SERV));
//		Term t = new Term(TermType.RESNAME, new ReservedName(ResNames.T));
//		Term region = new Term(TermType.NAME, R);
//		Term id = new Term(TermType.NAME, h);
//		Query query = new Query(id, region, serv, t);
//		PlainProcess P0 = new PlainProcess(ProcessType.NULL);
//		PlainProcess Q = new PlainProcess(ProcessType.QUERY, query, P0);
//		PlainProcess P = new PlainProcess(ProcessType.COMP, h, T2, Q);
//		PlainProcess P1 = new PlainProcess(ProcessType.COMP, R, T1, P);
//		PlainProcess P2 = new PlainProcess(ProcessType.CONDITION, p, P1, P0);
//		PlainProcess P3 = new PlainProcess(ProcessType.REPL, P2);
//		State init = new State();
//		ExtendedProcess A = new ExtendedProcess(ExtendedProcess.ProcessType.PLAINSTATE, P3, init);
//		// properties
//		StaticFormula Kloc = new StaticFormula(StaticFormula.FormulaType.KLOC);
//		StaticFormula Kt = new StaticFormula(StaticFormula.FormulaType.KT);
//		StaticFormula delta1 = new StaticFormula(StaticFormula.FormulaType.NEGATION, Kloc);
//		StaticFormula delta2 = new StaticFormula(StaticFormula.FormulaType.NEGATION, Kt);
//		EpistemicFormula stat1 = new EpistemicFormula(FormulaType.STATIC, delta1);
//		EpistemicFormula stat2 = new EpistemicFormula(FormulaType.STATIC, delta2);
//		EpistemicFormula or = new EpistemicFormula(FormulaType.DISJUNCTION, stat1, stat2);
//		EpistemicFormula phi = new EpistemicFormula(FormulaType.GLOBAL, or);

		//		//-------------------- Alice Protocol #2 ---------------------------
		//		Predicate q = new Predicate(PredicateType.L_DIVERS); PlainProcess P4 = new
		//				PlainProcess(ProcessType.CONDITION, q, P1, P0); PlainProcess P5 = new
		//				PlainProcess(ProcessType.CONDITION, p, P4, P0); PlainProcess P6 = new
		//				PlainProcess(ProcessType.REPL, P5); ExtendedProcess A1 = new
		//				ExtendedProcess(ExtendedProcess.ProcessType.PLAINSTATE, P6, init);


		// verification
		Witness res;
		for (TemporalFormula phi : properties) {
			try {
				res = Verifier.verify(phi, proc);
				//DEBUG
				if (res.getBool()) {
					System.out.println("The protocol " + procName + " satisfies the property " + phi);
				} else {
					System.out.println("The protocol " + procName + " does not satisfy the property " + phi);
					System.out.println(res.makeQuery());
					// grid
					System.out.println("Grid:");
					System.out.println("+-----+");
					System.out.println("|1 2 3|");
					System.out.println("|4 5 6|");
					System.out.println("|7 8 9|");
					System.out.println("+-----+");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
