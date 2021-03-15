package main;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelChecker.Verifier;
import modelChecker.Witness;
import processCalculus.ExtendedProcess;
import utils.ConfigReader;
import utils.Parser;
import utils.StateProp;
import properties.TemporalFormula;

public class RunAll {

	public static void main(String[] args) throws IOException {
		// measure overall time
		long startTime = System.nanoTime();

		// start the program
		if (!ConfigReader.readConfig()) {
			// Could not find or read the config file
			System.err.println("Config file could not be read! Exiting...");
			System.exit(1);
		}
		// caching
		Map<StateProp, Witness> cache = new LinkedHashMap<StateProp, Witness>();
		String path = "C:/Users/kaiba/git/diss/dissertation_template/images/Protocols/";
		Parser parser;
		String[] procs = {"beresford", "freudiger", "gong", "xinxin", "mobimix", "privacygrid", "cliquecloak", "prive", "lee",
				"reversecloak", "casper", "l2p2", "xu", "feelingbased", "locationdiversity", "kato", "kido", "spotme", "sybilquery",
				"mobipriv", "assam", "hoh", "cap", "cadsa", "mobicrowd", "ghinita", "trustnoone", "mapir", "spacetwist"};
		String ext = ".lp3";
		for (int proto = 0; proto < 29; ++proto) {
			parser = new Parser(path+procs[proto]+ext);
			parser.parse();
			String procName = parser.getProcName();
			ExtendedProcess proc = parser.getProcess();
			System.out.println("The protocol " + procName + ": " + proc);
			List<TemporalFormula> properties = parser.getProperties();
			List<String> propNames = parser.getPropNames();
			for (int i = 0; i < propNames.size(); ++i) {
				System.out.println("The property " + propNames.get(i) + ": " + properties.get(i));
			}
			// verification
			Witness res;
			Verifier verif = new Verifier(proc, cache);
			// Verifier verif = new Verifier(proc); // version with no caching
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
		}
		// measure overall time
		long stopTime = System.nanoTime();
		System.out.println("Overall: " + (stopTime - startTime));
	}

}
