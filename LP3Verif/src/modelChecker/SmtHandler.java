package modelChecker;

import processCalculus.State;
import properties.StaticFormula;
import terms.Query;

public class SmtHandler {
	
	/**
	 * Type of SMT command.
	 */
	private enum Command {
		// TODO more commands?
		DECLARE, DEFINE, ASSERT, CHECK, OPTION, POP, PUSH, UNSAT, VALUE
	}

	/**
	 * Type of variable.
	 */
	private enum VariableType {
		// TODO more types?
		BOOL, INT, EQ, VAR, REGION, LOCSPE, NULL, LOC, ID, GROUP, TIME, FRAME, SERV, SERVS, QUERY;

		@Override
		public String toString() {
			switch (this) {
			case BOOL:
				return "Bool";
			case INT:
				return "Int";
			case EQ:
				return "Equation";
			case VAR:
				return "Variable";
			case REGION:
				return "Region";
			case LOCSPE:
				return "Location Speed";
			case NULL:
				return "";
			case LOC:
				return "Location";
			case ID:
				return "ID";
			case GROUP:
				return "Group";
			case TIME:
				return "Time";
			case FRAME:
				return "Frame";
			case SERV:
				return "Service";
			case SERVS:
				return "Services";
			case QUERY:
				return "(g Group)(r Region)(f Frame)(s Service)";
			default:
				return this.name();
			}
		}
	}

	/**
	 * Type of SMT object, either function, sort or constant.
	 */
	private enum SmtType {
		FUN, SORT, CONST;
	}

	// class fields
	private String buffer;

	/**
	 * Constructor that initializes the buffer and already add the first lines of SMT
	 * code to declare necessary functions and variables.
	 */
	public SmtHandler() {
		// initialize buffer
		buffer = "";
		// set options
		addLineSmt(Command.OPTION, null, null, null, null, null);
		// declare all the necessary variables and functions
		declareSorts();
		declareFunctions();
		declareVariables();
		defineVars();
		defineFuncs();
	}

	/**
	 * Method that adds an SMT assertion to the existing file
	 * to check for satisfiabilty.
	 * @param property
	 *          the poperty to verify
	 * @return true if sat, false else
	 * @throws Exception 
	 */
	public Witness verify(State sigma, StaticFormula property) throws Exception {
		//TODO use push/pop for more efficiency
		int i = 1;
		for (Query q : sigma.getQueries()) {
			addLineSmt(Command.ASSERT, "QUERY" + i, null, q.makeSMT(i), null, null);
			addLineSmt(Command.ASSERT, "PROPOSITION" + i, null, q.makeSMTProp(i), null, null);
			++i;
		}
		// replace missing queries with "impossible" queries
		if (i == 1) {
			// no query has happened
			addLineSmt(Command.ASSERT, "QUERY" + i, null, makeQRY(i), null, null);
			++i;
			addLineSmt(Command.ASSERT, "QUERY" + i, null, makeQRY(i), null, null);
		} else if (i == 2) {
			addLineSmt(Command.ASSERT, "QUERY" + i, null, makeQRY(i), null, null);
		}
		// assert the negated property
		addLineSmt(Command.ASSERT, "PROPERTY", null, "(not " + makeSMT(property) + ")", null, null);
		
		// add the line for the satisfiability check
		addLineSmt(Command.CHECK, null, null, null, null, null);
		addLineSmt(Command.VALUE, null, null, null, null, null);

		SolverHandler solv = new SolverHandler();
		// return true or witness otherwise
		Witness tmp = solv.runSolver(buffer, property);
		tmp.setState(sigma);
		return tmp;
	}

	private String makeQRY(int i) {
		// TODO generate an impossible query, i.e., divers group. region. service, frame
		String tmp = " (insert 1 2 (singleton 3)))";
		String tmp_long = " (insert 1 2 3 4 5 6 7 8 (singleton 9)))";
		return "(and (= G" + i + tmp_long + " (= R" + i + tmp_long + " (= S" + i + tmp + " (= F" + i + tmp + ")";
	}

	private String makeSMT(StaticFormula property) {
		String ret = "";
		switch (property.getType()) {
		case KID:
			//TODO klappt das?
			ret = "(or (= 1 (card G1)) (= 1 (card (intersection G1 G2))))";
			break;
		case KLOC:
			//TODO richtig so?
			ret = "(or (= 1 (card R1)) (= 1 (card (intersection R1 (move R2)))) (= 1 (card (intersection R2 (move R1)))))";
			break;
		case KSERV:
			ret = "(= 1 (card S1))";
			break;
		case KT:
			ret = "(= 1 (card F1))";
			break;
		case DISJUNCTION:
			ret = "(or " + makeSMT(property.getDelta()) + " " + makeSMT(property.getDelta2()) + ")";
			break;
		case CONJUNCTION:
			ret = "(and " + makeSMT(property.getDelta()) + " " + makeSMT(property.getDelta2()) + ")";
			break;
		case NEGATION:
			ret = "(not " + makeSMT(property.getDelta()) + ")";
			break;
		}
		return ret;
	}

	/**
	 * Helper method to declare all necessary sorts (types).
	 */
	private void declareSorts() {
		// declare types
		addLineSmt(Command.DEFINE, "Location", SmtType.SORT, "Int", null, null);
		addLineSmt(Command.DEFINE, "Region", SmtType.SORT, "(Set Location)", null, null);
		addLineSmt(Command.DEFINE, "ID", SmtType.SORT, "Int", null, null);
		addLineSmt(Command.DEFINE, "Group", SmtType.SORT, "(Set ID)", null, null);
		addLineSmt(Command.DEFINE, "Time", SmtType.SORT, "Int", null, null);
		addLineSmt(Command.DEFINE, "Frame", SmtType.SORT, "(Set Time)", null, null);
		addLineSmt(Command.DEFINE, "Service", SmtType.SORT, "Int", null, null);
		addLineSmt(Command.DEFINE, "Services", SmtType.SORT, "(Set Service)", null, null);
		addLineSmt(Command.DEFINE, "Speed", SmtType.SORT, "Int", null, null);
	}

	/**
	 * Helper method to declare all necessary functions: has All/ONE/NONE, K, B for each
	 * component and additionally declare the necessary type equation.
	 */
	private void declareFunctions() {
		// declare the functions for locations and stuff
		addLineSmt(Command.DECLARE, "MBB", SmtType.FUN, null, VariableType.REGION, VariableType.REGION);
		addLineSmt(Command.DECLARE, "move", SmtType.FUN, null, VariableType.REGION, VariableType.REGION);
		addLineSmt(Command.DECLARE, "hash", SmtType.FUN, null, VariableType.GROUP, VariableType.GROUP);
		addLineSmt(Command.DECLARE, "rand", SmtType.FUN, null, VariableType.GROUP, VariableType.GROUP);
		addLineSmt(Command.DECLARE, "noise", SmtType.FUN, null, VariableType.LOC, VariableType.REGION);
		addLineSmt(Command.DECLARE, "noiset", SmtType.FUN, null, VariableType.TIME, VariableType.FRAME);
		addLineSmt(Command.DECLARE, "redund", SmtType.FUN, null, VariableType.LOC, VariableType.REGION);
		addLineSmt(Command.DECLARE, "swap", SmtType.FUN, null, VariableType.GROUP, VariableType.GROUP);
	}

	/**
	 * Helper method to declare all necessary variables and also declare all necessary
	 * equations.
	 */
	private void declareVariables() {
		//TODO more
		addLineSmt(Command.DECLARE, "R1", SmtType.FUN, null, VariableType.NULL, VariableType.REGION);
		addLineSmt(Command.DECLARE, "R2", SmtType.FUN, null, VariableType.NULL, VariableType.REGION);
		addLineSmt(Command.DECLARE, "loc", SmtType.FUN, null, VariableType.NULL, VariableType.LOC);
		addLineSmt(Command.DECLARE, "pid", SmtType.FUN, null, VariableType.NULL, VariableType.ID);
		addLineSmt(Command.DECLARE, "loc_i", SmtType.FUN, null, VariableType.NULL, VariableType.REGION);
		addLineSmt(Command.DECLARE, "pid_i", SmtType.FUN, null, VariableType.NULL, VariableType.GROUP);
		addLineSmt(Command.DECLARE, "G1", SmtType.FUN, null, VariableType.NULL, VariableType.GROUP);
		addLineSmt(Command.DECLARE, "G2", SmtType.FUN, null, VariableType.NULL, VariableType.GROUP);
		addLineSmt(Command.DECLARE, "t", SmtType.FUN, null, VariableType.NULL, VariableType.TIME);
		addLineSmt(Command.DECLARE, "t_i", SmtType.FUN, null, VariableType.NULL, VariableType.FRAME);
		addLineSmt(Command.DECLARE, "F1", SmtType.FUN, null, VariableType.NULL, VariableType.FRAME);
		addLineSmt(Command.DECLARE, "F2", SmtType.FUN, null, VariableType.NULL, VariableType.FRAME);
		addLineSmt(Command.DECLARE, "serv", SmtType.FUN, null, VariableType.NULL, VariableType.SERV);
		addLineSmt(Command.DECLARE, "serv_i", SmtType.FUN, null, VariableType.NULL, VariableType.SERVS);
		addLineSmt(Command.DECLARE, "S1", SmtType.FUN, null, VariableType.NULL, VariableType.SERVS);
		addLineSmt(Command.DECLARE, "S2", SmtType.FUN, null, VariableType.NULL, VariableType.SERVS);
		addLineSmt(Command.DECLARE, "locs", SmtType.FUN, null, VariableType.NULL, VariableType.REGION);
		addLineSmt(Command.DECLARE, "locs2", SmtType.FUN, null, VariableType.NULL, VariableType.REGION);
		addLineSmt(Command.DECLARE, "servs", SmtType.FUN, null, VariableType.NULL, VariableType.SERVS);
		addLineSmt(Command.DECLARE, "pids", SmtType.FUN, null, VariableType.NULL, VariableType.GROUP);
		addLineSmt(Command.DECLARE, "ts", SmtType.FUN, null, VariableType.NULL, VariableType.FRAME);
		addLineSmt(Command.DECLARE, "loc_1", SmtType.FUN, null, VariableType.NULL, VariableType.LOC);
		addLineSmt(Command.DECLARE, "loc2", SmtType.FUN, null, VariableType.NULL, VariableType.LOC);
		addLineSmt(Command.DECLARE, "loc_12", SmtType.FUN, null, VariableType.NULL, VariableType.LOC);
		addLineSmt(Command.DECLARE, "pid_1", SmtType.FUN, null, VariableType.NULL, VariableType.ID);
	}

	private void defineVars() {
		// TODO assert all fixed values and dependencies for variables
		addLineSmt(Command.ASSERT, "LOC", null, "(and (> loc 0) (< loc 10))", null, null);
		addLineSmt(Command.ASSERT, "LOC1", null, "(and (> loc_1 0) (< loc_1 10))", null, null);
		addLineSmt(Command.ASSERT, "LOC2", null, "(and (> loc2 0) (< loc2 10))", null, null);
		addLineSmt(Command.ASSERT, "LOC12", null, "(and (> loc_12 0) (< loc_12 10))", null, null);
		addLineSmt(Command.ASSERT, "LOCS", null, "(= locs (insert loc_1 (singleton loc)))", null, null);
		addLineSmt(Command.ASSERT, "LOCS2", null, "(= locs2 (insert loc_12 (singleton loc2)))", null, null);
		addLineSmt(Command.ASSERT, "PIDS", null, "(= pids (insert pid_1 (singleton pid)))", null, null);
		addLineSmt(Command.ASSERT, "SERV", null, "(and (> serv 0) (< serv 10))", null, null);
		addLineSmt(Command.ASSERT, "PID1", null, "(and (> pid 0) (< pid 10))", null, null);
		addLineSmt(Command.ASSERT, "PID2", null, "(and (> pid_1 0) (< pid_1 10))", null, null);
		addLineSmt(Command.ASSERT, "PIDUNIQ", null, "(not (= pid pid_1))", null, null);
		addLineSmt(Command.ASSERT, "Gr1", null, "(> (card G1) 0)", null, null);
		addLineSmt(Command.ASSERT, "Re1", null, "(> (card R1) 0)", null, null);
		addLineSmt(Command.ASSERT, "Se1", null, "(> (card S1) 0)", null, null);
		addLineSmt(Command.ASSERT, "Fr1", null, "(> (card F1) 0)", null, null);
		addLineSmt(Command.ASSERT, "Gr2", null, "(> (card G2) 0)", null, null);
		addLineSmt(Command.ASSERT, "Re2", null, "(> (card R2) 0)", null, null);
		addLineSmt(Command.ASSERT, "Se2", null, "(> (card S2) 0)", null, null);
		addLineSmt(Command.ASSERT, "Fr2", null, "(> (card F2) 0)", null, null);
		addLineSmt(Command.ASSERT, "LOCi", null, "(= loc_i locs)", null, null);
		addLineSmt(Command.ASSERT, "PIDi", null, "(= pid_i pids)", null, null);
		addLineSmt(Command.ASSERT, "SERVi", null, "(= serv_i servs)", null, null);
		addLineSmt(Command.ASSERT, "Ti", null, "(= t_i ts)", null, null);
	}

	private void defineFuncs() {
		// TODO assert all function facts
		// MBB explicitly
		addLineSmt(Command.ASSERT, "MBB1", null, "(= (MBB (singleton 1)) (singleton 1))", null, null); // one element
		addLineSmt(Command.ASSERT, "MBB2", null, "(= (MBB (singleton 2)) (singleton 2))", null, null);
		addLineSmt(Command.ASSERT, "MBB3", null, "(= (MBB (singleton 3)) (singleton 3))", null, null);
		addLineSmt(Command.ASSERT, "MBB4", null, "(= (MBB (singleton 4)) (singleton 4))", null, null);
		addLineSmt(Command.ASSERT, "MBB5", null, "(= (MBB (singleton 5)) (singleton 5))", null, null);
		addLineSmt(Command.ASSERT, "MBB6", null, "(= (MBB (singleton 6)) (singleton 6))", null, null);
		addLineSmt(Command.ASSERT, "MBB7", null, "(= (MBB (singleton 7)) (singleton 7))", null, null);
		addLineSmt(Command.ASSERT, "MBB8", null, "(= (MBB (singleton 8)) (singleton 8))", null, null);
		addLineSmt(Command.ASSERT, "MBB9", null, "(= (MBB (singleton 9)) (singleton 9))", null, null);
		addLineSmt(Command.ASSERT, "MBB11", null, "(= (MBB (insert 1 (singleton 2))) (insert 1 (singleton 2)))", null, null); // two elements
		addLineSmt(Command.ASSERT, "MBB12", null, "(= (MBB (insert 1 (singleton 3))) (insert 1 2 (singleton 3)))", null, null);
		addLineSmt(Command.ASSERT, "MBB13", null, "(= (MBB (insert 1 (singleton 4))) (insert 1 (singleton 4)))", null, null);
		addLineSmt(Command.ASSERT, "MBB14", null, "(= (MBB (insert 1 (singleton 5))) (insert 1 2 4 (singleton 5)))", null, null);
		addLineSmt(Command.ASSERT, "MBB15", null, "(= (MBB (insert 1 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB16", null, "(= (MBB (insert 1 (singleton 7))) (insert 1 4 (singleton 7)))", null, null);
		addLineSmt(Command.ASSERT, "MBB17", null, "(= (MBB (insert 1 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB18", null, "(= (MBB (insert 1 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB19", null, "(= (MBB (insert 2 (singleton 3))) (insert 2 (singleton 3)))", null, null);
		addLineSmt(Command.ASSERT, "MBB20", null, "(= (MBB (insert 2 (singleton 4))) (insert 1 2 4 (singleton 5)))", null, null);
		addLineSmt(Command.ASSERT, "MBB21", null, "(= (MBB (insert 2 (singleton 5))) (insert 2 (singleton 5)))", null, null);
		addLineSmt(Command.ASSERT, "MBB22", null, "(= (MBB (insert 2 (singleton 6))) (insert 2 3 5 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB23", null, "(= (MBB (insert 2 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB24", null, "(= (MBB (insert 2 (singleton 8))) (insert 2 5 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB25", null, "(= (MBB (insert 2 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB26", null, "(= (MBB (insert 3 (singleton 4))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB27", null, "(= (MBB (insert 3 (singleton 5))) (insert 2 3 5 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB28", null, "(= (MBB (insert 3 (singleton 6))) (insert 3 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB29", null, "(= (MBB (insert 3 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB30", null, "(= (MBB (insert 3 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB31", null, "(= (MBB (insert 3 (singleton 9))) (insert 3 6 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB32", null, "(= (MBB (insert 4 (singleton 5))) (insert 4 (singleton 5)))", null, null);
		addLineSmt(Command.ASSERT, "MBB33", null, "(= (MBB (insert 4 (singleton 6))) (insert 4 5 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB34", null, "(= (MBB (insert 4 (singleton 7))) (insert 4 (singleton 7)))", null, null);
		addLineSmt(Command.ASSERT, "MBB35", null, "(= (MBB (insert 4 (singleton 8))) (insert 4 5 7 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB36", null, "(= (MBB (insert 4 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB37", null, "(= (MBB (insert 5 (singleton 6))) (insert 5 (singleton 6)))", null, null);
		addLineSmt(Command.ASSERT, "MBB38", null, "(= (MBB (insert 5 (singleton 7))) (insert 4 5 7 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB39", null, "(= (MBB (insert 5 (singleton 8))) (insert 5 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB40", null, "(= (MBB (insert 5 (singleton 9))) (insert 5 6 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB41", null, "(= (MBB (insert 6 (singleton 7))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB42", null, "(= (MBB (insert 6 (singleton 8))) (insert 5 6 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB43", null, "(= (MBB (insert 6 (singleton 9))) (insert 6 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB44", null, "(= (MBB (insert 7 (singleton 8))) (insert 7 (singleton 8)))", null, null);
		addLineSmt(Command.ASSERT, "MBB45", null, "(= (MBB (insert 7 (singleton 9))) (insert 7 8 (singleton 9)))", null, null);
		addLineSmt(Command.ASSERT, "MBB46", null, "(= (MBB (insert 8 (singleton 9))) (insert 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB47", null, "(= (MBB (insert 1 2 (singleton 3))) (insert 1 2 (singleton 3)))", null, null); // three elements
//		addLineSmt(Command.ASSERT, "MBB48", null, "(= (MBB (insert 1 2 (singleton 4))) (insert 1 2 4 (singleton 5)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB49", null, "(= (MBB (insert 1 2 (singleton 5))) (insert 1 2 4 (singleton 5)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB50", null, "(= (MBB (insert 1 2 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB51", null, "(= (MBB (insert 1 2 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB52", null, "(= (MBB (insert 1 2 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB53", null, "(= (MBB (insert 1 2 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB54", null, "(= (MBB (insert 1 3 (singleton 4))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB55", null, "(= (MBB (insert 1 3 (singleton 5))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB56", null, "(= (MBB (insert 1 3 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB57", null, "(= (MBB (insert 1 3 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB58", null, "(= (MBB (insert 1 3 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB59", null, "(= (MBB (insert 1 3 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB60", null, "(= (MBB (insert 1 4 (singleton 5))) (insert 1 2 4 (singleton 5)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB61", null, "(= (MBB (insert 1 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB62", null, "(= (MBB (insert 1 4 (singleton 7))) (insert 1 4 (singleton 7)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB63", null, "(= (MBB (insert 1 4 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB64", null, "(= (MBB (insert 1 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB65", null, "(= (MBB (insert 1 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB66", null, "(= (MBB (insert 1 5 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB67", null, "(= (MBB (insert 1 5 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB68", null, "(= (MBB (insert 1 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB69", null, "(= (MBB (insert 1 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB70", null, "(= (MBB (insert 1 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB71", null, "(= (MBB (insert 1 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB72", null, "(= (MBB (insert 1 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB73", null, "(= (MBB (insert 1 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB74", null, "(= (MBB (insert 1 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB75", null, "(= (MBB (insert 2 3 (singleton 4))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB76", null, "(= (MBB (insert 2 3 (singleton 5))) (insert 2 3 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB77", null, "(= (MBB (insert 2 3 (singleton 6))) (insert 2 3 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB78", null, "(= (MBB (insert 2 3 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB79", null, "(= (MBB (insert 2 3 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB80", null, "(= (MBB (insert 2 3 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB81", null, "(= (MBB (insert 2 4 (singleton 5))) (insert 1 2 4 (singleton 5)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB82", null, "(= (MBB (insert 2 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB83", null, "(= (MBB (insert 2 4 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB84", null, "(= (MBB (insert 2 4 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB85", null, "(= (MBB (insert 2 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB86", null, "(= (MBB (insert 2 5 (singleton 6))) (insert 2 3 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB87", null, "(= (MBB (insert 2 5 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB88", null, "(= (MBB (insert 2 5 (singleton 8))) (insert 2 5 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB89", null, "(= (MBB (insert 2 5 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB90", null, "(= (MBB (insert 2 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB91", null, "(= (MBB (insert 2 6 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB92", null, "(= (MBB (insert 2 6 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB93", null, "(= (MBB (insert 2 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB94", null, "(= (MBB (insert 2 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB95", null, "(= (MBB (insert 2 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB96", null, "(= (MBB (insert 3 4 (singleton 5))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB97", null, "(= (MBB (insert 3 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB98", null, "(= (MBB (insert 3 4 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB99", null, "(= (MBB (insert 3 4 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB100", null, "(= (MBB (insert 3 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB101", null, "(= (MBB (insert 3 5 (singleton 6))) (insert 2 3 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB102", null, "(= (MBB (insert 3 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB103", null, "(= (MBB (insert 3 5 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB104", null, "(= (MBB (insert 3 5 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB105", null, "(= (MBB (insert 3 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB106", null, "(= (MBB (insert 3 6 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB107", null, "(= (MBB (insert 3 6 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB108", null, "(= (MBB (insert 3 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB109", null, "(= (MBB (insert 3 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB110", null, "(= (MBB (insert 3 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB111", null, "(= (MBB (insert 4 5 (singleton 6))) (insert 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB112", null, "(= (MBB (insert 4 5 (singleton 7))) (insert 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB113", null, "(= (MBB (insert 4 5 (singleton 8))) (insert 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB114", null, "(= (MBB (insert 4 5 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB115", null, "(= (MBB (insert 4 6 (singleton 7))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB116", null, "(= (MBB (insert 4 6 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB117", null, "(= (MBB (insert 4 6 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB118", null, "(= (MBB (insert 4 7 (singleton 8))) (insert 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB119", null, "(= (MBB (insert 4 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB120", null, "(= (MBB (insert 4 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB121", null, "(= (MBB (insert 5 6 (singleton 7))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB122", null, "(= (MBB (insert 5 6 (singleton 8))) (insert 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB123", null, "(= (MBB (insert 5 6 (singleton 9))) (insert 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB124", null, "(= (MBB (insert 5 7 (singleton 8))) (insert 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB125", null, "(= (MBB (insert 5 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB126", null, "(= (MBB (insert 5 8 (singleton 9))) (insert 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB127", null, "(= (MBB (insert 6 7 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB128", null, "(= (MBB (insert 6 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB129", null, "(= (MBB (insert 6 8 (singleton 9))) (insert 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB130", null, "(= (MBB (insert 7 8 (singleton 9))) (insert 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB131", null, "(= (MBB (insert 1 2 3 (singleton 4))) (insert 1 2 3 4 5 (singleton 6)))", null, null); // four elements
//		addLineSmt(Command.ASSERT, "MBB132", null, "(= (MBB (insert 1 2 3 (singleton 5))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB133", null, "(= (MBB (insert 1 2 3 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB134", null, "(= (MBB (insert 1 2 3 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB135", null, "(= (MBB (insert 1 2 3 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB136", null, "(= (MBB (insert 1 2 3 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB137", null, "(= (MBB (insert 1 2 4 (singleton 5))) (insert 1 2 4 (singleton 5)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB138", null, "(= (MBB (insert 1 2 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB139", null, "(= (MBB (insert 1 2 4 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB140", null, "(= (MBB (insert 1 2 4 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB141", null, "(= (MBB (insert 1 2 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB142", null, "(= (MBB (insert 1 2 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB143", null, "(= (MBB (insert 1 2 5 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB144", null, "(= (MBB (insert 1 2 5 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB145", null, "(= (MBB (insert 1 2 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB146", null, "(= (MBB (insert 1 2 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB147", null, "(= (MBB (insert 1 2 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB148", null, "(= (MBB (insert 1 2 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB149", null, "(= (MBB (insert 1 2 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB150", null, "(= (MBB (insert 1 2 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB151", null, "(= (MBB (insert 1 2 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB152", null, "(= (MBB (insert 1 3 4 (singleton 5))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB153", null, "(= (MBB (insert 1 3 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB154", null, "(= (MBB (insert 1 3 4 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB155", null, "(= (MBB (insert 1 3 4 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB156", null, "(= (MBB (insert 1 3 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB157", null, "(= (MBB (insert 1 3 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB158", null, "(= (MBB (insert 1 3 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB159", null, "(= (MBB (insert 1 3 5 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB160", null, "(= (MBB (insert 1 3 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB161", null, "(= (MBB (insert 1 3 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB162", null, "(= (MBB (insert 1 3 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB163", null, "(= (MBB (insert 1 3 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB164", null, "(= (MBB (insert 1 3 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB165", null, "(= (MBB (insert 1 3 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB166", null, "(= (MBB (insert 1 3 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB167", null, "(= (MBB (insert 1 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB168", null, "(= (MBB (insert 1 4 5 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB169", null, "(= (MBB (insert 1 4 5 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB170", null, "(= (MBB (insert 1 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB171", null, "(= (MBB (insert 1 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB172", null, "(= (MBB (insert 1 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB173", null, "(= (MBB (insert 1 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB174", null, "(= (MBB (insert 1 4 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB175", null, "(= (MBB (insert 1 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB176", null, "(= (MBB (insert 1 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB177", null, "(= (MBB (insert 1 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB178", null, "(= (MBB (insert 1 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB179", null, "(= (MBB (insert 1 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB180a", null, "(= (MBB (insert 1 5 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB181a", null, "(= (MBB (insert 1 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB182a", null, "(= (MBB (insert 1 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB180", null, "(= (MBB (insert 1 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB181", null, "(= (MBB (insert 1 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB182", null, "(= (MBB (insert 1 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB183", null, "(= (MBB (insert 1 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB184", null, "(= (MBB (insert 2 3 4 (singleton 5))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB185", null, "(= (MBB (insert 2 3 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB186", null, "(= (MBB (insert 2 3 4 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB187", null, "(= (MBB (insert 2 3 4 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB188", null, "(= (MBB (insert 2 3 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB189", null, "(= (MBB (insert 2 3 5 (singleton 6))) (insert 2 3 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB190", null, "(= (MBB (insert 2 3 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB191", null, "(= (MBB (insert 2 3 5 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB192", null, "(= (MBB (insert 2 3 5 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB193", null, "(= (MBB (insert 2 3 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB194", null, "(= (MBB (insert 2 3 6 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB195", null, "(= (MBB (insert 2 3 6 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB196", null, "(= (MBB (insert 2 3 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB197", null, "(= (MBB (insert 2 3 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB198", null, "(= (MBB (insert 2 3 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB199", null, "(= (MBB (insert 2 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB200", null, "(= (MBB (insert 2 4 5 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB201", null, "(= (MBB (insert 2 4 5 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB202", null, "(= (MBB (insert 2 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB203", null, "(= (MBB (insert 2 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB204", null, "(= (MBB (insert 2 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB205", null, "(= (MBB (insert 2 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB206", null, "(= (MBB (insert 2 4 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB207", null, "(= (MBB (insert 2 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB208", null, "(= (MBB (insert 2 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB209", null, "(= (MBB (insert 2 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB210", null, "(= (MBB (insert 2 5 6 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB211", null, "(= (MBB (insert 2 5 6 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB212", null, "(= (MBB (insert 2 5 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB213", null, "(= (MBB (insert 2 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB214", null, "(= (MBB (insert 2 5 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB215", null, "(= (MBB (insert 2 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB216", null, "(= (MBB (insert 2 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB217", null, "(= (MBB (insert 2 6 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB218", null, "(= (MBB (insert 2 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB219", null, "(= (MBB (insert 3 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB220", null, "(= (MBB (insert 3 4 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB221", null, "(= (MBB (insert 3 4 5 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB222", null, "(= (MBB (insert 3 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB223", null, "(= (MBB (insert 3 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB224", null, "(= (MBB (insert 3 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB225", null, "(= (MBB (insert 3 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB226", null, "(= (MBB (insert 3 4 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB227", null, "(= (MBB (insert 3 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB228", null, "(= (MBB (insert 3 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB229", null, "(= (MBB (insert 3 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB230", null, "(= (MBB (insert 3 5 6 (singleton 8))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB231", null, "(= (MBB (insert 3 5 6 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB232", null, "(= (MBB (insert 3 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB233", null, "(= (MBB (insert 3 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB234", null, "(= (MBB (insert 3 5 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB235", null, "(= (MBB (insert 3 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB236", null, "(= (MBB (insert 3 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB237", null, "(= (MBB (insert 3 6 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB238", null, "(= (MBB (insert 3 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB239", null, "(= (MBB (insert 4 5 6 (singleton 7))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB240", null, "(= (MBB (insert 4 5 6 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB241", null, "(= (MBB (insert 4 5 6 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB242", null, "(= (MBB (insert 4 5 7 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB243", null, "(= (MBB (insert 4 5 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB244", null, "(= (MBB (insert 4 5 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB245", null, "(= (MBB (insert 4 6 7 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB246", null, "(= (MBB (insert 4 6 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB247", null, "(= (MBB (insert 4 6 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB248", null, "(= (MBB (insert 4 7 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB249", null, "(= (MBB (insert 5 6 7 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB250", null, "(= (MBB (insert 5 6 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB251", null, "(= (MBB (insert 5 6 8 (singleton 9))) (insert 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB252", null, "(= (MBB (insert 5 7 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB253", null, "(= (MBB (insert 6 7 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB254", null, "(= (MBB (insert 1 2 3 4 (singleton 5))) (insert 1 2 3 4 5 (singleton 6)))", null, null); // five elements
//		addLineSmt(Command.ASSERT, "MBB255", null, "(= (MBB (insert 1 2 3 4 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB256", null, "(= (MBB (insert 1 2 3 4 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB257", null, "(= (MBB (insert 1 2 3 4 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB258", null, "(= (MBB (insert 1 2 3 4 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB259", null, "(= (MBB (insert 1 2 3 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB260", null, "(= (MBB (insert 1 2 3 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB261", null, "(= (MBB (insert 1 2 3 5 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB262", null, "(= (MBB (insert 1 2 3 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB263", null, "(= (MBB (insert 1 2 3 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB264", null, "(= (MBB (insert 1 2 3 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB265", null, "(= (MBB (insert 1 2 3 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB266", null, "(= (MBB (insert 1 2 3 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB267", null, "(= (MBB (insert 1 2 3 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB268", null, "(= (MBB (insert 1 2 3 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB269", null, "(= (MBB (insert 1 2 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB270", null, "(= (MBB (insert 1 2 4 5 (singleton 7))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB271", null, "(= (MBB (insert 1 2 4 5 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB272", null, "(= (MBB (insert 1 2 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB273", null, "(= (MBB (insert 1 2 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB274", null, "(= (MBB (insert 1 2 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB275", null, "(= (MBB (insert 1 2 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB276", null, "(= (MBB (insert 1 2 4 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB277", null, "(= (MBB (insert 1 2 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB278", null, "(= (MBB (insert 1 2 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB279", null, "(= (MBB (insert 1 2 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB280", null, "(= (MBB (insert 1 2 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB281", null, "(= (MBB (insert 1 2 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB282", null, "(= (MBB (insert 1 2 5 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB283", null, "(= (MBB (insert 1 2 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB284", null, "(= (MBB (insert 1 2 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB285", null, "(= (MBB (insert 1 2 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB286", null, "(= (MBB (insert 1 2 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB287", null, "(= (MBB (insert 1 2 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB288", null, "(= (MBB (insert 1 2 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB289", null, "(= (MBB (insert 1 3 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB290", null, "(= (MBB (insert 1 3 4 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB291", null, "(= (MBB (insert 1 3 4 5 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB292", null, "(= (MBB (insert 1 3 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB293", null, "(= (MBB (insert 1 3 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB294", null, "(= (MBB (insert 1 3 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB295", null, "(= (MBB (insert 1 3 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB296", null, "(= (MBB (insert 1 3 4 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB297", null, "(= (MBB (insert 1 3 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB298", null, "(= (MBB (insert 1 3 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB299", null, "(= (MBB (insert 1 3 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB300", null, "(= (MBB (insert 1 3 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB301", null, "(= (MBB (insert 1 3 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB302", null, "(= (MBB (insert 1 3 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB303", null, "(= (MBB (insert 1 3 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB304", null, "(= (MBB (insert 1 3 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB305", null, "(= (MBB (insert 1 3 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB306", null, "(= (MBB (insert 1 3 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB307", null, "(= (MBB (insert 1 3 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB308", null, "(= (MBB (insert 1 3 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB309", null, "(= (MBB (insert 1 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB310", null, "(= (MBB (insert 1 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB311", null, "(= (MBB (insert 1 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB312", null, "(= (MBB (insert 1 4 5 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB313", null, "(= (MBB (insert 1 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB314", null, "(= (MBB (insert 1 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB315", null, "(= (MBB (insert 1 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB316", null, "(= (MBB (insert 1 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB317", null, "(= (MBB (insert 1 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB318", null, "(= (MBB (insert 1 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB319", null, "(= (MBB (insert 1 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB320", null, "(= (MBB (insert 1 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB321", null, "(= (MBB (insert 1 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB322", null, "(= (MBB (insert 1 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB323", null, "(= (MBB (insert 1 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB324", null, "(= (MBB (insert 2 3 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB325", null, "(= (MBB (insert 2 3 4 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB326", null, "(= (MBB (insert 2 3 4 5 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB327", null, "(= (MBB (insert 2 3 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB328", null, "(= (MBB (insert 2 3 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB329", null, "(= (MBB (insert 2 3 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB330", null, "(= (MBB (insert 2 3 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB331", null, "(= (MBB (insert 2 3 4 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB332", null, "(= (MBB (insert 2 3 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB333", null, "(= (MBB (insert 2 3 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB334", null, "(= (MBB (insert 2 3 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB335", null, "(= (MBB (insert 2 3 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB336", null, "(= (MBB (insert 2 3 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB337", null, "(= (MBB (insert 2 3 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB338", null, "(= (MBB (insert 2 3 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB339", null, "(= (MBB (insert 2 3 5 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB340", null, "(= (MBB (insert 2 3 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB341", null, "(= (MBB (insert 2 3 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB342", null, "(= (MBB (insert 2 3 6 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB343", null, "(= (MBB (insert 2 3 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB344", null, "(= (MBB (insert 2 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB345", null, "(= (MBB (insert 2 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB346", null, "(= (MBB (insert 2 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB347", null, "(= (MBB (insert 2 4 5 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB348", null, "(= (MBB (insert 2 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB349", null, "(= (MBB (insert 2 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB350", null, "(= (MBB (insert 2 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB351", null, "(= (MBB (insert 2 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB352", null, "(= (MBB (insert 2 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB353", null, "(= (MBB (insert 2 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB354", null, "(= (MBB (insert 2 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB355", null, "(= (MBB (insert 2 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB356", null, "(= (MBB (insert 2 5 6 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB357", null, "(= (MBB (insert 2 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB358", null, "(= (MBB (insert 2 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB359", null, "(= (MBB (insert 3 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB360", null, "(= (MBB (insert 3 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB361", null, "(= (MBB (insert 3 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB362", null, "(= (MBB (insert 3 4 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB363", null, "(= (MBB (insert 3 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB364", null, "(= (MBB (insert 3 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB365", null, "(= (MBB (insert 3 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB366", null, "(= (MBB (insert 3 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB367", null, "(= (MBB (insert 3 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB368", null, "(= (MBB (insert 3 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB369", null, "(= (MBB (insert 3 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB370", null, "(= (MBB (insert 3 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB371", null, "(= (MBB (insert 3 5 6 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB372", null, "(= (MBB (insert 3 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB373", null, "(= (MBB (insert 3 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB374", null, "(= (MBB (insert 4 5 6 7 (singleton 8))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB375", null, "(= (MBB (insert 4 5 6 7 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB376", null, "(= (MBB (insert 4 5 6 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB377", null, "(= (MBB (insert 4 5 7 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB378", null, "(= (MBB (insert 4 6 7 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB379", null, "(= (MBB (insert 5 6 7 8 (singleton 9))) (insert 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB380", null, "(= (MBB (insert 1 2 3 4 5 (singleton 6))) (insert 1 2 3 4 5 (singleton 6)))", null, null); // six elements
//		addLineSmt(Command.ASSERT, "MBB381", null, "(= (MBB (insert 1 2 3 4 5 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB382", null, "(= (MBB (insert 1 2 3 4 5 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB383", null, "(= (MBB (insert 1 2 3 4 5 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB384", null, "(= (MBB (insert 1 2 3 4 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB385", null, "(= (MBB (insert 1 2 3 4 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB386", null, "(= (MBB (insert 1 2 3 4 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB387", null, "(= (MBB (insert 1 2 3 4 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB388", null, "(= (MBB (insert 1 2 3 4 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB389", null, "(= (MBB (insert 1 2 3 4 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB390", null, "(= (MBB (insert 1 2 3 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB391", null, "(= (MBB (insert 1 2 3 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB392", null, "(= (MBB (insert 1 2 3 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB393", null, "(= (MBB (insert 1 2 3 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB394", null, "(= (MBB (insert 1 2 3 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB395", null, "(= (MBB (insert 1 2 3 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB396", null, "(= (MBB (insert 1 2 3 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB397", null, "(= (MBB (insert 1 2 3 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB398", null, "(= (MBB (insert 1 2 3 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB399", null, "(= (MBB (insert 1 2 3 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB400", null, "(= (MBB (insert 1 2 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB401", null, "(= (MBB (insert 1 2 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB402", null, "(= (MBB (insert 1 2 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB403", null, "(= (MBB (insert 1 2 4 5 7 (singleton 8))) (insert 1 2 4 5 7 (singleton 8)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB404", null, "(= (MBB (insert 1 2 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB405", null, "(= (MBB (insert 1 2 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB406", null, "(= (MBB (insert 1 2 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB407", null, "(= (MBB (insert 1 2 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB408", null, "(= (MBB (insert 1 2 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB409", null, "(= (MBB (insert 1 2 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB410", null, "(= (MBB (insert 1 2 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB411", null, "(= (MBB (insert 1 2 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB412", null, "(= (MBB (insert 1 2 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB413", null, "(= (MBB (insert 1 2 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB414", null, "(= (MBB (insert 1 2 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB415", null, "(= (MBB (insert 1 3 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB416", null, "(= (MBB (insert 1 3 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB417", null, "(= (MBB (insert 1 3 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB418", null, "(= (MBB (insert 1 3 4 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB419", null, "(= (MBB (insert 1 3 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB420", null, "(= (MBB (insert 1 3 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB421", null, "(= (MBB (insert 1 3 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB422", null, "(= (MBB (insert 1 3 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB423", null, "(= (MBB (insert 1 3 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB424", null, "(= (MBB (insert 1 3 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB425", null, "(= (MBB (insert 1 3 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB426", null, "(= (MBB (insert 1 3 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB427", null, "(= (MBB (insert 1 3 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB428", null, "(= (MBB (insert 1 3 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB429", null, "(= (MBB (insert 1 3 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB430", null, "(= (MBB (insert 1 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB431", null, "(= (MBB (insert 1 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB432", null, "(= (MBB (insert 1 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB433", null, "(= (MBB (insert 1 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB434", null, "(= (MBB (insert 1 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB435", null, "(= (MBB (insert 1 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB436", null, "(= (MBB (insert 2 3 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB437", null, "(= (MBB (insert 2 3 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB438", null, "(= (MBB (insert 2 3 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB439", null, "(= (MBB (insert 2 3 4 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB440", null, "(= (MBB (insert 2 3 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB441", null, "(= (MBB (insert 2 3 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB442", null, "(= (MBB (insert 2 3 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB443", null, "(= (MBB (insert 2 3 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB444", null, "(= (MBB (insert 2 3 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB445", null, "(= (MBB (insert 2 3 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB446", null, "(= (MBB (insert 2 3 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB447", null, "(= (MBB (insert 2 3 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB448", null, "(= (MBB (insert 2 3 5 6 8 (singleton 9))) (insert 2 3 5 6 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB449", null, "(= (MBB (insert 2 3 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB450", null, "(= (MBB (insert 2 3 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB451", null, "(= (MBB (insert 2 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB452", null, "(= (MBB (insert 2 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB453", null, "(= (MBB (insert 2 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB454", null, "(= (MBB (insert 2 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB455", null, "(= (MBB (insert 2 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB456", null, "(= (MBB (insert 2 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB457", null, "(= (MBB (insert 3 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB458", null, "(= (MBB (insert 3 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB459", null, "(= (MBB (insert 3 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB460", null, "(= (MBB (insert 3 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB461", null, "(= (MBB (insert 3 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB462", null, "(= (MBB (insert 3 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB463", null, "(= (MBB (insert 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB464", null, "(= (MBB (insert 1 2 3 4 5 6 (singleton 7))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null); // seven elements
//		addLineSmt(Command.ASSERT, "MBB465", null, "(= (MBB (insert 1 2 3 4 5 6 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB466", null, "(= (MBB (insert 1 2 3 4 5 6 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB467", null, "(= (MBB (insert 1 2 3 4 5 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB468", null, "(= (MBB (insert 1 2 3 4 5 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB469", null, "(= (MBB (insert 1 2 3 4 5 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB470", null, "(= (MBB (insert 1 2 3 4 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB471", null, "(= (MBB (insert 1 2 3 4 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB472", null, "(= (MBB (insert 1 2 3 4 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB473", null, "(= (MBB (insert 1 2 3 4 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB474", null, "(= (MBB (insert 1 2 3 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB475", null, "(= (MBB (insert 1 2 3 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB476", null, "(= (MBB (insert 1 2 3 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB477", null, "(= (MBB (insert 1 2 3 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB478", null, "(= (MBB (insert 1 2 3 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB479", null, "(= (MBB (insert 1 2 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB480", null, "(= (MBB (insert 1 2 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB481", null, "(= (MBB (insert 1 2 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB482", null, "(= (MBB (insert 1 2 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB483", null, "(= (MBB (insert 1 2 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB484", null, "(= (MBB (insert 1 2 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB485", null, "(= (MBB (insert 1 3 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB486", null, "(= (MBB (insert 1 3 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB487", null, "(= (MBB (insert 1 3 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB488", null, "(= (MBB (insert 1 3 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB489", null, "(= (MBB (insert 1 3 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB490", null, "(= (MBB (insert 1 3 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB491", null, "(= (MBB (insert 1 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB492", null, "(= (MBB (insert 2 3 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB493", null, "(= (MBB (insert 2 3 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB494", null, "(= (MBB (insert 2 3 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB495", null, "(= (MBB (insert 2 3 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB496", null, "(= (MBB (insert 2 3 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB497", null, "(= (MBB (insert 2 3 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB498", null, "(= (MBB (insert 2 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB499", null, "(= (MBB (insert 3 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB500", null, "(= (MBB (insert 1 2 3 4 5 6 7 (singleton 8))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null); // eight elements
//		addLineSmt(Command.ASSERT, "MBB501", null, "(= (MBB (insert 1 2 3 4 5 6 7 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB502", null, "(= (MBB (insert 1 2 3 4 5 6 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB503", null, "(= (MBB (insert 1 2 3 4 5 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB504", null, "(= (MBB (insert 1 2 3 4 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB505", null, "(= (MBB (insert 1 2 3 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB506", null, "(= (MBB (insert 1 2 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB507", null, "(= (MBB (insert 1 3 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB508", null, "(= (MBB (insert 2 3 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null);
//		addLineSmt(Command.ASSERT, "MBB509", null, "(= (MBB (insert 1 2 3 4 5 6 7 8 (singleton 9))) (insert 1 2 3 4 5 6 7 8 (singleton 9)))", null, null); // nine elements
		// move explicitly
		addLineSmt(Command.ASSERT, "MV1", null, "(= (move (singleton 1)) (insert 5 4 2 (singleton 1)))", null, null); // one element
		addLineSmt(Command.ASSERT, "MV2", null, "(= (move (singleton 2)) (insert 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV3", null, "(= (move (singleton 3)) (insert 6 5 3 (singleton 2)))", null, null);
		addLineSmt(Command.ASSERT, "MV4", null, "(= (move (singleton 4)) (insert 8 7 5 4 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV5", null, "(= (move (singleton 5)) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV6", null, "(= (move (singleton 6)) (insert 9 8 6 5 3 (singleton 2)))", null, null);
		addLineSmt(Command.ASSERT, "MV7", null, "(= (move (singleton 7)) (insert 8 7 5 (singleton 4)))", null, null);
		addLineSmt(Command.ASSERT, "MV8", null, "(= (move (singleton 8)) (insert 9 8 7 6 5 (singleton 4)))", null, null);
		addLineSmt(Command.ASSERT, "MV9", null, "(= (move (singleton 9)) (insert 9 8 6 (singleton 5)))", null, null);
		addLineSmt(Command.ASSERT, "MV10", null, "(= (move (insert 2 (singleton 1))) (insert 6 5 4 3 2 (singleton 1)))", null, null); // leading 1
		addLineSmt(Command.ASSERT, "MV11", null, "(= (move (insert 4 (singleton 1))) (insert 8 7 5 4 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV12", null, "(= (move (insert 3 2 (singleton 1))) (insert 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV13", null, "(= (move (insert 7 4 (singleton 1))) (insert 8 7 5 4 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV14", null, "(= (move (insert 5 4 2 (singleton 1))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV15", null, "(= (move (insert 6 5 4 3 2 (singleton 1))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV16", null, "(= (move (insert 8 7 5 4 2 (singleton 1))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV17", null, "(= (move (insert 9 8 7 6 5 4 3 2 (singleton 1))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV18", null, "(= (move (insert 3 (singleton 2))) (insert 6 5 4 3 2 (singleton 1)))", null, null); // leading 2
		addLineSmt(Command.ASSERT, "MV19", null, "(= (move (insert 5 (singleton 2))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV19a", null, "(= (move (insert 8 5 (singleton 2))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV20", null, "(= (move (insert 6 5 3 (singleton 2))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV21", null, "(= (move (insert 9 8 6 5 3 (singleton 2))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV22", null, "(= (move (insert 6 (singleton 3))) (insert 9 8 6 5 3 (singleton 2)))", null, null); // leading 3
		addLineSmt(Command.ASSERT, "MV23", null, "(= (move (insert 9 6 (singleton 3))) (insert 9 8 6 5 3 (singleton 2)))", null, null);
		addLineSmt(Command.ASSERT, "MV24", null, "(= (move (insert 5 (singleton 4))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null); // leading 4
		addLineSmt(Command.ASSERT, "MV24a", null, "(= (move (insert 7 (singleton 4))) (insert 8 7 5 4 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV25", null, "(= (move (insert 6 5 (singleton 4))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV26", null, "(= (move (insert 8 7 5 (singleton 4))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV27", null, "(= (move (insert 9 8 7 6 5 (singleton 4))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV28", null, "(= (move (insert 6 (singleton 5))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null); // leading 5
		addLineSmt(Command.ASSERT, "MV29", null, "(= (move (insert 8 (singleton 5))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV30", null, "(= (move (insert 9 8 6 (singleton 5))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MV31", null, "(= (move (insert 9 (singleton 6))) (insert 9 8 6 5 3 (singleton 2)))", null, null); // leading 6
		addLineSmt(Command.ASSERT, "MV32", null, "(= (move (insert 8 (singleton 7))) (insert 9 8 7 6 5 (singleton 4)))", null, null); // leading 7
		addLineSmt(Command.ASSERT, "MV33", null, "(= (move (insert 9 8 (singleton 7))) (insert 9 8 7 6 5 (singleton 4)))", null, null);
		addLineSmt(Command.ASSERT, "MV34", null, "(= (move (insert 9 (singleton 8))) (insert 9 8 7 6 5 (singleton 4)))", null, null); // leading 8
		addLineSmt(Command.ASSERT, "MV35", null, "(= (move (insert 9 8 7 6 5 4 3 2 (singleton 1))) (insert 9 8 7 6 5 4 3 2 (singleton 1)))", null, null); // whole region
		// move for disconnected regions
		addLineSmt(Command.ASSERT, "MVDIS1", null, "(= (move (insert 9 5 (singleton 1))) (insert 9 8 6 5 4 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MVDIS2", null, "(= (move (insert 8 (singleton 2))) (insert 9 8 7 5 3 2 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "MVDIS3", null, "(= (move (insert 7 (singleton 3))) (insert 8 7 6 4 3 (singleton 2)))", null, null);
		addLineSmt(Command.ASSERT, "MVDIS4", null, "(= (move (insert 6 (singleton 4))) (insert 9 6 5 4 3 (singleton 1)))", null, null);
		
		// for sets with more elements
		addLineSmt(Command.ASSERT, "MBBall", null, "(forall ((r Region)) (>= (card (MBB r)) (card r))) ", null, null); // MBB greater equal to region
		addLineSmt(Command.ASSERT, "MVall", null, "(forall ((r Region)) (>= (card (move r)) (card r))) ", null, null); // move greater equal to region
		// hash and rand
		addLineSmt(Command.ASSERT, "HASHall", null, "(forall ((g Group)) (>= (card (hash g)) 2)) ", null, null);
		addLineSmt(Command.ASSERT, "RANDall", null, "(forall ((g Group)) (= (rand g) (insert 3 2 (singleton 1)))) ", null, null);
		// redund
		addLineSmt(Command.ASSERT, "RED1", null, "(= (redund 1) (insert 9 5 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "RED2", null, "(= (redund 2) (insert 8 (singleton 2)))", null, null);
		addLineSmt(Command.ASSERT, "RED3", null, "(= (redund 3) (insert 7 (singleton 3)))", null, null);
		addLineSmt(Command.ASSERT, "RED4", null, "(= (redund 4) (insert 6 (singleton 4)))", null, null);
		addLineSmt(Command.ASSERT, "RED5", null, "(= (redund 5) (insert 9 5 (singleton 1)))", null, null);
		addLineSmt(Command.ASSERT, "RED6", null, "(= (redund 6) (insert 6 (singleton 4)))", null, null);
		addLineSmt(Command.ASSERT, "RED7", null, "(= (redund 7) (insert 7 (singleton 3)))", null, null);
		addLineSmt(Command.ASSERT, "RED8", null, "(= (redund 8) (insert 8 (singleton 2)))", null, null);
		addLineSmt(Command.ASSERT, "RED9", null, "(= (redund 9) (insert 9 5 (singleton 1)))", null, null);
		// noise
		addLineSmt(Command.ASSERT, "NOISEall", null, "(forall ((l Location)) (= (noise l) (move (singleton l)))) ", null, null);
		addLineSmt(Command.ASSERT, "NOISETall", null, "(forall ((t Time)) (= (noiset t) (insert 3 2 1 (singleton t)))) ", null, null);
		addLineSmt(Command.ASSERT, "SWAPall", null, "(forall ((g Group)) (= (swap g) (insert 3 2 (singleton 1)))) ", null, null);
	}

	/**
	 * Method to write an SMT line into the buffer.
	 * 
	 * @param cmd
	 *          the type of SMT command
	 * @param varName
	 *          the name of the variable to declare
	 * @param type
	 *          the type of variable to declare
	 * @param expression
	 *          the expression for a define
	 * @param in
	 *          the input type
	 * @param out
	 *          the output type
	 */
	private void addLineSmt(Command cmd, String varName, SmtType type, String expression,
			VariableType in, VariableType out) {
		// Method used to add a line to the buffer
		switch (cmd) {
		case DECLARE:
			// declare based on the type
			switch (type) {
			case CONST:
				addBuffer("( declare-const " + varName + " " + out.toString() + " )" + System.lineSeparator());
				break;
			case FUN:
				addBuffer("( declare-fun " + varName + " (" + in.toString() + ") "
						+ out.toString() + " )" + System.lineSeparator());
				break;
			case SORT:
				addBuffer("( declare-sort " + varName + " 0 )" + System.lineSeparator());
				break;
			default:
				break;
			}
			break;
		case DEFINE:
			// define a variables based in its type
			switch (type) {
			case FUN:
				addBuffer("( define-fun " + varName + " (" + in.toString() + ") "
						+ out.toString() + " " + expression + " )" + System.lineSeparator());
				break;
			case SORT:
				addBuffer("( define-sort " + varName + " () " + expression
						+ " )" + System.lineSeparator());
				break;
			default:
				break;
			}
			break;
		case ASSERT:
			if (expression == null) {
				addBuffer("( assert " + varName + " )" + System.lineSeparator());
			} else {
				addBuffer("( assert (! " + expression + " :named " + varName
						+ ") )" + System.lineSeparator());
			}
			break;
		case CHECK:
			// TODO more options?
			addBuffer("( check-sat )" + System.lineSeparator());
			break;
		case UNSAT:
			addBuffer("( get-unsat-core )" + System.lineSeparator());
			break;
		case OPTION:
			// TODO different options
			addBuffer("( set-logic UFLIAFS )" + System.lineSeparator());
			addBuffer("( set-option :produce-models true )" + System.lineSeparator());
			addBuffer("( set-option :produce-unsat-cores true )" + System.lineSeparator());
			break;
		case PUSH:
			addBuffer("( push 1 )" + System.lineSeparator());
			break;
		case POP:
			addBuffer("( pop 1 )" + System.lineSeparator());
			break;
		case VALUE:
			addBuffer("( get-value (pid loc serv t loc_1 G1 R1 S1 F1 G2 R2 S2 F2) )" + System.lineSeparator());
		}
	}

	private void addBuffer(String buffer) {
		this.buffer += buffer;
	}

	public String getBuffer() {
		return buffer;
	}

}
