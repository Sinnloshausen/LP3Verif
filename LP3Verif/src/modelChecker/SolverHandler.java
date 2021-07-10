package modelChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import properties.StaticFormula;
import utils.ConfigReader;

/**
 * Class that takes care of the running of the solver.
 */
public class SolverHandler {

	// constants
	//private static final String SPATH = "C:/cvc4/";
	private static final String SPATH = ConfigReader.solverPath;
	//private static final String SNAME = "cvc4.exe";
	private static final String SNAME = ConfigReader.solverName;
	//private static final String FPATH = "C:/cvc4/";
	private static final String FPATH = ConfigReader.filePath;
	//private static final String FNAME = "test.smt2";
	private static final String FNAME = ConfigReader.fileName;

	// class fields
	private String solverPath;
	private String solverName;
	private String filePath;
	private String fileName;

	/**
	 * The full constructor with all parameters
	 * that is is typically only called by the empty constructor in this class.
	 * 
	 * @param solverPath
	 *          the path where the solver (mathsat) is to find
	 * @param solverName
	 *          the name of the solver to exec
	 * @param filePath
	 *          the path where the SMT2 file should be saved
	 * @param fileName
	 *          the name of the file to save as
	 */
	public SolverHandler(String solverPath, String solverName, String filePath, String fileName) {
		this.solverPath = solverPath;
		this.solverName = solverName;
		this.filePath = filePath;
		this.fileName = fileName;
	}

	/**
	 * Empty constructor with default values from config file.
	 */
	public SolverHandler() {
		this(SPATH, SNAME, FPATH, FNAME);
	}

	/**
	 * Method that runs the solver on the content of a string.
	 * 
	 * @param buffer
	 *          the string containing valid SMT lines
	 * @param property
	 *          the property that is verified by this run
	 * @return true, if successful
	 */
	public Witness runSolver(String buffer, StaticFormula property) throws Exception {
		// create file handler
		FileHandler file = new FileHandler(filePath, fileName);
		byte[] lines = (buffer).getBytes();

		// write into the file
		if (!file.writeFile(lines)) {
			throw new Exception("Could not write into file!");
		}

		String line = "";
		String lastLine = "";
		String secondLastLine = "";
		List<String> history = new ArrayList<String>();
		// run the solver on the file
		try {
			Process cvc4;
			cvc4 = Runtime.getRuntime().exec(
					solverPath + solverName + " " + filePath + fileName);
			cvc4.waitFor();
			BufferedReader bufReader = new BufferedReader(
					new InputStreamReader(cvc4.getInputStream()));
			while ((line = bufReader.readLine()) != null) {
				// go through all lines
				history.add(line);
				secondLastLine = lastLine;
				lastLine = line;
			}
			if (secondLastLine.equals("sat") || secondLastLine.equals("unknown")) {
				return new Witness(false, lastLine, null, null, property);
			} else if (secondLastLine.equals("unsat")) {
				// Trace: pass on output
				return new Witness(true, property);
			} else {
				System.out.println("Unexpected return from SMT solver:");
				System.out.println(secondLastLine);
				System.out.println(lastLine);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		throw new Exception("Something unexpected happened...");
	}

	// getter and setter methods
	public String getSolverPath() {
		return solverPath;
	}

	public void setSolverPath(String solverPath) {
		this.solverPath = solverPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
