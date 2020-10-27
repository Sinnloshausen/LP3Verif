package main;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import locationsAndStuff.Query;
import modelChecker.Verifier;
import modelChecker.Witness;
import processCalculus.ExtendedProcess;
import processCalculus.PlainProcess;
import processCalculus.PlainProcess.ProcessType;
import terms.Function;
import terms.Name;
import terms.Predicate;
import terms.ReservedName;
import terms.Term;
import terms.Function.FunctionType;
import terms.Function.IntFunction;
import terms.Function.LocFunction;
import terms.Predicate.PredicateType;
import terms.ReservedName.ResNames;
import terms.Term.TermType;
import utils.ConfigReader;
import processCalculus.State;
import properties.EpistemicFormula;
import properties.EpistemicFormula.FormulaType;
import properties.StaticFormula;

public class Test {

	public static void main(String[] args) throws IOException {

		// start the program
		if (!ConfigReader.readConfig()) {
			// Could not find or read the config file
			System.err.println("Config file could not be read! Exiting...");
			System.exit(1);
		}

		System.out.println("Grid:");
		System.out.println("+-----+");
		System.out.println("|1 2 3|");
		System.out.println("|4 5 6|");
		System.out.println("|7 8 9|");
		System.out.println("+-----+");
		//-------------- Alice Protocol #1 -------------------
		// protocol
		Predicate p = new Predicate(PredicateType.K_USERS);
		Name R = new Name("R");
		Function MBB = new Function(FunctionType.LOC, LocFunction.MBB);
		List<Term> locs = Collections.singletonList(new Term(TermType.RESNAME, new ReservedName(ResNames.LOCS)));
		Term T1 = new Term(TermType.FUNC, MBB, locs);
		Name h = new Name("h");
		Function hash = new Function(FunctionType.INT, IntFunction.HASH);
		List<Term> ids = Collections.singletonList(new Term(TermType.RESNAME, new ReservedName(ResNames.PID)));
		Term T2 = new Term(TermType.FUNC, hash, ids);
		Term serv = new Term(TermType.RESNAME, new ReservedName(ResNames.SERV));
		Term t = new Term(TermType.RESNAME, new ReservedName(ResNames.T));
		Term region = new Term(TermType.NAME, R);
		Term id = new Term(TermType.NAME, h);
		Query query = new Query(id, region, serv, t);
		PlainProcess P0 = new PlainProcess(ProcessType.NULL);
		PlainProcess Q = new PlainProcess(ProcessType.QUERY, query, P0);
		PlainProcess P = new PlainProcess(ProcessType.COMP, h, T2, Q);
		PlainProcess P1 = new PlainProcess(ProcessType.COMP, R, T1, P);
		PlainProcess P2 = new PlainProcess(ProcessType.CONDITION, p, P1, P0);
		PlainProcess P3 = new PlainProcess(ProcessType.REPL, P2);
		State init = new State();
		ExtendedProcess A = new ExtendedProcess(ExtendedProcess.ProcessType.PLAINSTATE, P3, init);
		// properties
		StaticFormula Kloc = new StaticFormula(StaticFormula.FormulaType.KLOC);
		StaticFormula Kt = new StaticFormula(StaticFormula.FormulaType.KT);
		StaticFormula delta1 = new StaticFormula(StaticFormula.FormulaType.NEGATION, Kloc);
		StaticFormula delta2 = new StaticFormula(StaticFormula.FormulaType.NEGATION, Kt);
		EpistemicFormula stat1 = new EpistemicFormula(FormulaType.STATIC, delta1);
		EpistemicFormula stat2 = new EpistemicFormula(FormulaType.STATIC, delta2);
		EpistemicFormula or = new EpistemicFormula(FormulaType.DISJUNCTION, stat1, stat2);
		EpistemicFormula phi = new EpistemicFormula(FormulaType.GLOBAL, or);

		//-------------------- Alice Protocol #2 ---------------------------
		Predicate q = new Predicate(PredicateType.L_DIVERS);
		PlainProcess P4 = new PlainProcess(ProcessType.CONDITION, q, P1, P0);
		PlainProcess P5 = new PlainProcess(ProcessType.CONDITION, p, P4, P0);
		PlainProcess P6 = new PlainProcess(ProcessType.REPL, P5);
		ExtendedProcess A1 = new ExtendedProcess(ExtendedProcess.ProcessType.PLAINSTATE, P6, init);


		// Choice of Example
		System.out.println("Choose example protocol from paper ('1' for Alice's original design and '2' for her improved protocol): ");
		String choice;
		Scanner scanIn = new Scanner(System.in);
		choice = scanIn.nextLine();
		scanIn.close();
		System.out.println("You chose: example " + choice);
		if (choice.equals("1")) {
			// verify
			Witness res;
			try {
				res = Verifier.verify(phi, A);
				//DEBUG
				if (res.getBool()) {
					System.out.println("The protocol " + P3 + " satisfies the property " + phi);
				} else {
					System.out.println("The protocol " + P3 + " does not satisfy the property " + phi);
					System.out.println(res.makeQuery());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (choice.equals("2")) {
			// verify
			Witness res1;
			try {
				res1 = Verifier.verify(phi, A1);
				//DEBUG
				if (res1.getBool()) {
					System.out.println("The protocol " + P6 + " satisfies the property " + phi);
				} else {
					System.out.println("The protocol " + P6 + " does not satisfy the property " + phi);
					System.out.println(res1.makeQuery());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//TODO DEBUG
			System.out.println("no valid option, terminating");
			System.exit(1);
		}

	}

}
