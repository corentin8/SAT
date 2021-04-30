package fr.uga.pddl4j.tutorial.satplanner;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.statespace.AbstractStateSpacePlanner;
import fr.uga.pddl4j.planners.statespace.StateSpacePlanner;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.util.SequentialPlan;
import fr.uga.pddl4j.planners.Statistics;
import fr.uga.pddl4j.heuristics.relaxation.FastForward;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

/**
 * This class implements a simple SAT planner based on SAT4J.
 *
 * @author H. Fiorino
 * @version 1.0 - 29.03.2021
 */
public final class SATPlanner extends AbstractStateSpacePlanner {

    /*
     * The arguments of the planner.
     */
    private Properties arguments;


    /**
     * Creates a new SAT planner with the default parameters.
     *
     * @param arguments the arguments of the planner.
     */
    public SATPlanner(final Properties arguments) {
        super();
        this.arguments = arguments;
    }

    /**
     * Solves the planning problem and returns the first solution found.
     *
     * @param problem the problem to be solved.
     * @return a solution search or null if it does not exist.
     */
    @Override
    public Plan search(final CodedProblem problem) throws OutOfMemoryError {
        // The solution plan is sequential
        final Plan plan = new SequentialPlan();
        // We get the initial state from the planning problem
        final BitState init = new BitState(problem.getInit());
        // We get the goal from the planning problem
        final BitState goal = new BitState(problem.getGoal());
        // Nothing to do, goal is already satisfied by the initial state
        long spentTime, startTime;
        long begin;
        FastForward ff = new FastForward(problem);
        int minSteps = ff.estimate(new BitState(problem.getInit()), problem.getGoal());

        if (init.satisfy(problem.getGoal())) {
            return plan;
        }
        // Otherwise, we start the search
        else {
            boolean erreuresat;
            // SAT solver timeout
            final int timeout = ((int) this.arguments.get(Planner.TIMEOUT));
            // SAT solver max number of var
            final int MAXVAR = 1000000;
            // SAT solver max number of clauses
            final int NBCLAUSES = 100000000;

            ISolver solver = SolverFactory.newDefault();
            solver.setTimeout(timeout);
            ModelIterator mi = new ModelIterator(solver);

            startTime = System.currentTimeMillis();

            begin = System.currentTimeMillis();

            SATEncoding encode=new SATEncoding(problem,NBCLAUSES);
            for(int t=0;t<minSteps;t++){
                encode.next();
            }
            getStatistics().setTimeToEncode(System.currentTimeMillis() - begin);

            begin = System.currentTimeMillis();
            // Prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
            solver.newVar(MAXVAR);
            solver.setExpectedNumberOfClauses(NBCLAUSES);

            // SAT Encoding starts here!
            final int steps = (int) arguments.get("steps");

            // Feed the solver using Dimacs format, using arrays of int
            Boolean trouver=true;
            int [][]g;
            ArrayList<int[]>tmp;
            IProblem ip;
            while(trouver /*&& encode.getsteps()<steps*/) {
                // the clause should not contain a 0, only integer (positive or negative)
                // with absolute values less or equal to MAXVAR
                // e.g. int [] clause = {1, -3, 7}; is fine
                // while int [] clause = {1, -3, 7, 0}; is not fine
                erreuresat=false;

                getStatistics().setTimeToSearch(getStatistics().getTimeToSearch()+System.currentTimeMillis() - begin);
                begin = System.currentTimeMillis();
                tmp=encode.next();
                if(tmp==null){
                    return plan;
                }

                for(int []r :tmp){
                    try {
                        solver.addClause(new VecInt(r)); // adapt Array to IVecInt
                    } catch (ContradictionException e){
                        //System.out.println("SAT encoding failure!");
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
                g=encode.goal(problem);
                for(int [] r : g){
                    try {
                        if(r[0]!=0) {
                            solver.addClause(new VecInt(r)); // adapt Array to IVecInt
                        }
                    } catch (ContradictionException e){
                        //si le but est inateniable avec la profondeur actuellement tester la clause vide est
                        //crée, il fautessayer avec une profondeur plus grande
                        //e.printStackTrace();
                        erreuresat=true;
                    }
                }
                getStatistics().setTimeToEncode(getStatistics().getTimeToEncode()+System.currentTimeMillis() - begin);
                // We are done. Working now on the IProblem interface
                begin = System.currentTimeMillis();
                ip = solver;
                try {
                    if (!erreuresat && ip.isSatisfiable()) {
                        int [] mod=ip.findModel();
                        encode.decode(mod,problem,plan);
                        trouver=false;
                    } else {
                        solver = SolverFactory.newDefault();
                        solver.setTimeout(timeout);
                        solver.newVar(MAXVAR);
                        solver.setExpectedNumberOfClauses(NBCLAUSES);
                    }
                } catch (TimeoutException e){
                    //System.out.println("Timeout! No solution found!");
                    return plan;
                    //System.exit(0);
                }

                spentTime = (System.currentTimeMillis() - startTime) / 1000;
                if (spentTime > timeout) {
                    // System.out.println("time limit exceeded");
                    return null;
                }

            }
            getStatistics().setTimeToSearch(getStatistics().getTimeToSearch()+System.currentTimeMillis() - begin);

            // Finally, we return the solution plan or null otherwise
            return plan;
        }
    }

    /**
     * Print the usage of the SAT planner.
     */
    private static void printUsage() {
        final StringBuilder strb = new StringBuilder();
        strb.append("\nusage of PDDL4J:\n")
                .append("OPTIONS   DESCRIPTIONS\n")
                .append("-o <str>    operator file name\n")
                .append("-f <str>    fact file name\n")
                .append("-t <num>    SAT solver timeout in seconds\n")
                .append("-n <num>    Max number of steps\n")
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
                if (!new File(args[i + 1]).exists()) return null;
                arguments.put(Planner.DOMAIN, new File(args[i + 1]));
            } else if ("-f".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                if (!new File(args[i + 1]).exists()) return null;
                arguments.put(Planner.PROBLEM, new File(args[i + 1]));
            } else if ("-t".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                final int timeout = Integer.parseInt(args[i + 1]);
                if (timeout < 0) return null;
                arguments.put(Planner.TIMEOUT, timeout);
            } else if ("-n".equalsIgnoreCase(args[i]) && ((i + 1) < args.length)) {
                final int steps = Integer.parseInt(args[i + 1]);
                if (steps > 0)
                    arguments.put("steps", steps);
                else
                    return null;
            } else {
                return null;
            }
        }
        // Return null if the domain or the problem was not specified
        return (arguments.get(Planner.DOMAIN) == null
                || arguments.get(Planner.PROBLEM) == null) ? null : arguments;
    }

    /**
     * The main method of the <code>SATPlanner</code> example. The command line syntax is as
     * follow:
     * <p>
     * <pre>
     * usage of SATPlanner:
     *
     * OPTIONS   DESCRIPTIONS
     *
     * -o <i>str</i>   operator file name
     * -f <i>str</i>   fact file name
     * -t <i>num</i>   specifies the maximum CPU-time in seconds
     * -n <i>num</i>   specifies the maximum number of steps
     * -h              print this message
     *
     * </pre>
     * </p>
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        final Properties arguments = SATPlanner.parseCommandLine(args);
        if (arguments == null) {
            SATPlanner.printUsage();
            System.exit(0);
        }

        final SATPlanner planner = new SATPlanner(arguments);
        final ProblemFactory factory = ProblemFactory.getInstance();
        Statistics info = planner.getStatistics();

        File domain = (File) arguments.get(Planner.DOMAIN);
        File problem = (File) arguments.get(Planner.PROBLEM);
        ErrorManager errorManager = null;
        try {
            errorManager = factory.parse(domain, problem);
        } catch (IOException e) {
            Planner.getLogger().trace("\nUnexpected error when parsing the PDDL files.");
            System.exit(0);
        }

        if (!errorManager.isEmpty()) {
            errorManager.printAll();
            System.exit(0);
        } /*else {
            Planner.getLogger().trace("\nParsing domain file: successfully done");
            Planner.getLogger().trace("\nParsing problem file: successfully done\n");
        }*/

        long begin = System.currentTimeMillis();
        final CodedProblem pb = factory.encode();
        planner.getStatistics().setTimeToParse(System.currentTimeMillis() - begin);

       // Planner.getLogger().trace("\nGrounding: successfully done ("
        //        + pb.getOperators().size() + " ops, "
        //        + pb.getRelevantFacts().size() + " facts)\n");

        if (!pb.isSolvable()) {
            //Planner.getLogger().trace(String.format("Goal can be simplified to FALSE."
            //        +  "No search will solve it%n%n"));
            System.exit(0);
        }


        begin = System.currentTimeMillis();
        Plan plan = null;
        try {
            plan = planner.search(pb);

        } catch (OutOfMemoryError e) {
            // System.out.println("heap memory exceeded");
        }

        if (plan == null || plan.size() == 0) {
            System.out.println("None");
            return;
        }

        System.out.println((System.currentTimeMillis()-begin)/1000.0+" "+plan.size());


/*
        Planner.getLogger().trace(String.format("%nfound plan as follows:%n%n" + pb.toString(plan)));
        Planner.getLogger().trace(String.format("%nplan total cost: %4.2f%n%n", plan.cost()));

        long time = info.getTimeToParse() +  info.getTimeToEncode() + info.getTimeToSearch();
        Planner.getLogger().trace(String.format("%ntime spent:   %8.2f seconds parsing %n", info.getTimeToParse()/1000.0));
        Planner.getLogger().trace(String.format("              %8.2f seconds encoding %n", info.getTimeToEncode()/1000.0));
        Planner.getLogger().trace(String.format("              %8.2f seconds searching%n", info.getTimeToSearch()/1000.0));
        Planner.getLogger().trace(String.format("              %8.2f seconds total time%n", time/1000.0));
*/
    }
}

