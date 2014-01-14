package org.def.turing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Machine {
	private Tape tape;
	private String state;
	private String name;

	private String[] symbols;
	private String[] states;
	private String blankSymbol;

	private Map<String, Object> rules;

	public Machine(String name) {
		super();
		this.name = name;
	}

	public void initMachine(Map<String, Object> rules, Tape tape,
			String startState) {
		this.tape = tape;
		this.rules = rules;
		Set<String> symbols = new HashSet<String>();
		Set<String> states = new HashSet<String>();
		for (Map.Entry<String, Object> e : rules.entrySet()) {
			symbols.add(e.getKey());
			Map<String, Object> statesR = (Map<String, Object>) e.getValue();
			for (Map.Entry<String, Object> en : statesR.entrySet()) {
				states.add(en.getKey());
			}
		}
		this.symbols = symbols.toArray(new String[] {});
		this.states = states.toArray(new String[] {});
		this.blankSymbol = (String) tape.getDefaultValue();
		this.state = startState;
	}

	public String getFullMachineDescription() {
		StringBuffer b = new StringBuffer();
		b.append("Machine: " + getName() + "\n");
		b.append("Symbols: " + Arrays.toString(symbols) + "\n");
		b.append("States: " + Arrays.toString(states) + "\n");
		b.append("Start State: " + state + "\n");
		b.append("Blank symbol: " + blankSymbol + "\n");
		b.append("Rules table:\n+--------+");

		for (String state : states) {
			b.append("---------------+");
		}
		b.append("\n|        |");
		for (String state : states) {
			b.append(pad(state, 15) + "|");
		}
		b.append("\n+--------+");
		for (String state : states) {
			b.append("---------------+");
		}

		for (String symbol : symbols) {
			b.append("\n|" + pad(symbol, 8) + "|");

			for (String state : states) {
				Rule r = (Rule) ((Map<String, Object>) rules.get(symbol))
						.get(state);
				String rule = String.format(" %s, %s, %s", r.simbol,
						(r.direction > 0 ? "R" : r.direction < 0 ? "L" : "N"),
						r.nextState);
				b.append(pad(rule, 15) + "|");
			}
			b.append("\n");
			b.append("+--------+");
			for (String state : states) {
				b.append("---------------+");
			}
		}

		return b.toString();
	}

	private String pad(String str, int len) {
		String m = "";
		int n = len - str.length();
		while (n-- > 0) {
			m += " ";
		}
		return m + str;
	}

	public String getName() {
		return name;
	}

	public static Machine buildMachine(String fileName) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName)));

		String name = reader.readLine().trim();
		String infite = reader.readLine();
		infite = infite.substring(
				infite.indexOf("INFINITE_TAPE:") + "INFINITE_TAPE:".length())
				.trim();
		String tape = reader.readLine();
		tape = tape.substring(tape.indexOf("TAPE:") + "TAPE:".length()).trim();
		String defaultTapeSymbol = reader.readLine();
		defaultTapeSymbol = defaultTapeSymbol.substring(
				defaultTapeSymbol.indexOf("DEAFULT_TAPE_SYMBOL:")
						+ "DEAFULT_TAPE_SYMBOL:".length()).trim();

		Tape t = new Tape(Boolean.parseBoolean(infite), defaultTapeSymbol);
		if (!tape.equals("NONE")) {
			String[] ts = tape.split("\\|");
			for (String symbol : ts) {
				t.addToTape(symbol.trim());
			}
		}

		String tapePosition = reader.readLine();
		tapePosition = tapePosition.substring(tapePosition
				.indexOf("TAPE_POSITION:") + "TAPE_POSITION:".length());
		int n = Integer.parseInt(tapePosition.trim());
		while (n-- > 0) {
			t.moveRight();
		}
		String startState = reader.readLine();
		startState = startState.substring(
				startState.indexOf("START_STATE:") + "START_STATE:".length())
				.trim();

		if (Boolean.parseBoolean(infite)) {
			t.getCurrentValue(); // force to produce a cell if No cells in tape
									// and we have an infinite tape
		}

		Map<String, Object> rules = new HashMap<String, Object>();

		String line = null;

		while ((line = reader.readLine()) != null) {
			String symbol = line.split("=")[0];
			String[] states = line.split("=")[1].trim().split("\\|");
			Map<String, Object> statesRules = new HashMap<String, Object>();
			rules.put(symbol, statesRules);
			for (String state : states) {
				String stateName = state.substring(0, state.indexOf(":"))
						.trim();
				String[] r = state.substring(state.indexOf(":") + 1).trim()
						.split(",");
				Rule rule = new Rule();
				String direction = r[1].trim().toLowerCase();

				if ("l".equals(direction)) {
					rule.direction = -1;
				} else if ("r".equals(direction)) {
					rule.direction = 1;
				} else if ("n".equals(direction)) {
					rule.direction = 0;
				} else {
					rule.direction = Integer.parseInt(direction);
				}
				rule.nextState = r[2].trim();
				rule.simbol = r[0].trim();
				statesRules.put(stateName, rule);
			}
		}

		Machine m = new Machine(name);
		m.initMachine(rules, t, startState);
		return m;
	}

	public void step() throws Exception {
		// read tape first
		String symbol = (String) tape.getCurrentValue();
		// get the rule now . . .
		Rule rule = (Rule) ((Map<String, Object>) rules.get(symbol)).get(state);
		if (rule.nextState.equals("HALT")) {
			throw new Exception("NORMAL HALT");
		}

		// Write symbol
		tape.getCurrent().value = rule.simbol;
		try {
			switch (rule.direction) {
			case 1:
				tape.moveRight();
				break;
			case -1:
				tape.moveLeft();
				break;
			}
		} catch (Exception e) {
			if (e.getMessage().equals("LIST_UNDER")
					|| e.getMessage().equals("LIST_OVER")) {
				throw new Exception("Machine halted - move out of tape!");
			}
			throw e;
		}
		this.state = rule.nextState;

	}

	public static void main(String[] args) throws Exception {

		String usage = "turing <filename> [number of computations]";
		if (args.length == 0) {
			System.out.println(usage);
			return;
		}
		if ("--help".equals(args[0].trim())) {
			System.out.println(usage);
			return;
		}
		String fileName = args[0];
		int calculations = -1;
		if (args.length > 1) {
			calculations = Integer.parseInt(args[1].trim());
		}
		Machine machine = Machine.buildMachine(fileName);
		System.out.println(machine.getFullMachineDescription());
		int count = 1;
		String f = "%" + (calculations > 0 ? (calculations + "").length() : "")
				+ "d. %s";
		boolean normalExit = true;
		try {
			System.out.println("\nInitial state: \n0. " + machine);
			System.out.println("====== COMPUTATION BEGIN ========");
			while (true) {
				if (calculations > 0 && count > calculations)
					break;
				machine.step();
				System.out.println(String.format(f, count, machine));
				Thread.sleep(10);
				count++;

			}
			System.out.println("======  COMPUTATION END  ========");
		} catch (Exception e) {
			System.out.println("Computation ended. (" + e.getMessage() + ")");
			if (!e.getMessage().equals("NORMAL HALT"))
				normalExit = false;
		}
		if (normalExit)
			System.out.println("Computation complete. . .");
		else
			System.out.println("Unexpected computation end!");
	}

	public String toString() {
		return "(" + state + ") " + tape;
	}

}

class Rule {
	String simbol;
	int direction;
	String nextState;
}
