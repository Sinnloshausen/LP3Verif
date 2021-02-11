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
		// measure overall time
		long startTime = System.nanoTime();

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

		long beforeParseTime = System.nanoTime();
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

		long beforeTraceTime = System.nanoTime();
		// verification
		Witness res;
		Verifier verif = new Verifier(proc);
		long beforeVerifTime = System.nanoTime();
		for (TemporalFormula phi : properties) {
			try {
				res = verif.verify(phi);
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
		// measure overall time
		long stopTime = System.nanoTime();
		System.out.println("Overall: " + (stopTime - startTime));
		System.out.println("Parsing: " + (beforeTraceTime - beforeParseTime));
		System.out.println("Traces: " + (beforeVerifTime - beforeTraceTime));
		System.out.println("Verification: " + (stopTime - beforeVerifTime));
	}

}
