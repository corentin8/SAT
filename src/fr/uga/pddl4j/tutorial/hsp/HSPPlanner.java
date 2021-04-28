package fr.uga.pddl4j.tutorial.HSPPlanner;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.hsp.HSP;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.parser.ErrorManager;

import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.planners.statespace.search.strategy.StateSpaceStrategy;

import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.planners.statespace.search.strategy.Node;
import fr.uga.pddl4j.planners.statespace.search.strategy.StateSpaceStrategy;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Properties;

public final class HSPPlanner {

	/*
	 * The arguments of the planner.
	 */
	private Properties arguments;

    /**
     * Computation timeout.
     */
    private static final int TIMEOUT = 30;

    /**
     * Default Heuristic Type.
     */
    private static final Heuristic.Type HEURISTIC_TYPE = Heuristic.Type.FAST_FORWARD;

    /**
     * Default Heuristic Weight.
     */
    private static final double HEURISTIC_WEIGHT = 1.0;

    /**
     * Default Trace level.
     */
    private static final int TRACE_LEVEL = 0;

    /**
     * Default statistics computation.
     */
    private static final boolean STATISTICS = false;

	/**
	 * Creates a new HSPPlanner planner with the default parameters.
	 *
	 * @param arguments the arguments of the planner.
	 */
	public HSPPlanner(final Properties arguments) {
		super();
		this.arguments = arguments;
	}


	/**
	 * The main method of the <code>HSPPlanner</code> example. The command line syntax is
	 * as follow:
	 * <p>
	 *
	 * <pre>
	 * usage of HSPPlanner:
	 *
	 * OPTIONS   DESCRIPTIONS
	 *
	 * -o <i>str</i>   operator file name
	 * -f <i>str</i>   fact file name
	 * -t <i>num</i>   specifies the maximum CPU-time in seconds (preset: 300)
	 * -h              print this message
	 *
	 * </pre>
	 * </p>
	 *
	 * @param args the arguments of the command line.
	 */

	/**
	 * Print the usage of the HSPPlanner planner.
	 */
	private static void printUsage() {
		final StringBuilder strb = new StringBuilder();
		strb.append("\nusage of PDDL4J:\n").append("OPTIONS   DESCRIPTIONS\n")
				.append("-o <str>    operator file name\n").append("-f <str>    fact file name\n")
				.append("-t <num>    specifies the maximum CPU-time in seconds (preset: 300)\n")
				.append("-h          print this message\n\n");
		Planner.getLogger().trace(strb.toString());
	}

	/**
	 * Parse the command line and return the planner's arguments.
	 *
	 * @param args the command line.
	 * @return the planner arguments or null if an invalid argument is encountered.
	 */
	private static Properties parseCommandLine(String[] args) {

		// Get the default arguments from the super class
		final Properties arguments = StateSpacePlanner.getDefaultArguments();

		// Parse the command line and update the default argument value
		for (int i = 0; i < args.length; i += 2) {
			if ("-o".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
				if (!new File(args[i + 1]).exists())
					return null;
				arguments.put(Planner.DOMAIN, new File(args[i + 1]));
			} else if ("-f".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
				if (!new File(args[i + 1]).exists())
					return null;
				arguments.put(Planner.PROBLEM, new File(args[i + 1]));
			} else if ("-t".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
				final int timeout = Integer.parseInt(args[i + 1]) * 1000;
				if (timeout < 0)
					return null;
				arguments.put(Planner.TIMEOUT, timeout);
			} else {
				return null;
			}
		}
		// Return null if the domain or the problem was not specified
		return (arguments.get(Planner.DOMAIN) == null || arguments.get(Planner.PROBLEM) == null) ? null : arguments;
	}

	public static void main(String[] args) {
		// To be completed
		final Properties arguments = HSPPlanner.parseCommandLine(args);
		if (arguments == null) {
			HSPPlanner.printUsage();
			System.exit(0);
		}

		final ProblemFactory factory = ProblemFactory.getInstance();

		File domain = (File) arguments.get(Planner.DOMAIN);
		File problem = (File) arguments.get(Planner.PROBLEM);
		ErrorManager errorManager = null;
		try {
			errorManager = factory.parse(domain, problem);
		} catch (IOException e) {
			Planner.getLogger().trace("\nunexpected error when parsing the PDDL planning problem description.");
			System.exit(0);
		}

		if (!errorManager.isEmpty()) {
			errorManager.printAll();
			System.exit(0);
		}

		final CodedProblem pb = factory.encode();

        long begin = System.currentTimeMillis();
        begin = System.currentTimeMillis();
        HSP planner = new HSP(TIMEOUT * 1000, HEURISTIC_TYPE, HEURISTIC_WEIGHT, STATISTICS, TRACE_LEVEL);
        final Plan plan = planner.search(pb);

        if (plan != null) {
            System.out.println((System.currentTimeMillis()-begin)/1000.0+"  "+plan.size());
            return;
        }

        System.out.println("* A* failed");

	}
}
