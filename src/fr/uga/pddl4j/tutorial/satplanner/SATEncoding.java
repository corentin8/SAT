package fr.uga.pddl4j.tutorial.satplanner;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.BitVector;
import fr.uga.pddl4j.util.Plan;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * This class implements a planning problem/domain encoding into DIMACS
 *
 * @author H. Fiorino
 * @version 1.0 - 30.03.2021
 */
public final class SATEncoding {
    /*
     * A SAT problem in dimacs format is a list of int list a.k.a clauses
     */
    private ArrayList<int []> dimacs;

    /*
     * Current number of steps of the SAT encoding
     */
    private int steps;
    private int tailleobj;

    /**
     *
     * @param
     */
    public SATEncoding(final CodedProblem problem, final int steps) {
        super();
        dimacs= new ArrayList<int[]>();
        this.steps = steps;
        // We get the initial state from the planning problem
        int res=0;
        BitVector init = problem.getInit().getPositive();
        tailleobj=init.size();
        int [] a;
        for(int ini=0; ini<tailleobj;ini++){
            a= new int[1];
            res=pair(ini+1,steps);
            if (init.get(ini)){
                a[0]=res;
                dimacs.add(a);
            }
            else{
                a[0]=-res;
                dimacs.add(a);
            }
        }


        // Encoding of init
        // Each fact is a unit clause


        // We get the operators of the problem

    }

    /*
     * SAT encoding for next step
     */
    public ArrayList<int[]> next(final CodedProblem problem) {
        int codea;
        int tailleobj=problem.getInit().getNegative().size();
        Boolean [][] nap=new Boolean[tailleobj][problem.getOperators().size()];
        Boolean [][] pan=new Boolean[tailleobj][problem.getOperators().size()];
        int [] comptenap=new int[tailleobj];
        int [] comptepan=new int[tailleobj];
        int[] neg;
        int[] pos;
        int[] pre;

        //initialisation de variable pour mémoriser les State transitions
        for (int r=0;r<tailleobj;r++){
            comptenap[r]=0;
            comptepan[r]=0;
            for(int o=0;o<problem.getOperators().size();o++){
                nap[r][o]=false;
                pan[r][o]=false;
            }
        }
        int [] exclusion;
        for (int i = 0; i < problem.getOperators().size(); i++) {
            for (int j = 0; j < problem.getOperators().size(); j++) {
                if(i!=j){
                    exclusion=new int[2];
                    exclusion[0]=-pair(i+1+tailleobj,steps);
                    exclusion[1]=-pair(j+1+tailleobj,steps);
                    dimacs.add(exclusion);
                }
            }
        }

        //parcour de toutes opération possible
        for (int i = 0; i < problem.getOperators().size(); i++) {
            final BitOp a = problem.getOperators().get(i);
            codea=pair(i+1+tailleobj,steps);
            //encodage des préconditions des actions
            final BitVector precond = a.getPreconditions().getPositive();
            for (int p=0; p<precond.size();p++){
                if(precond.get(p)) {
                    pre = new int[2];
                    pre[1] = -codea;
                    pre[0] =pair(p+1,steps);
                    dimacs.add(pre);
                }
            }
            final BitVector positive = a.getUnconditionalEffects().getPositive();
            final BitVector negative = a.getUnconditionalEffects().getNegative();
            //encodage des effet positife des actions
            for (int p=0; p<positive.size();p++) {
                if (positive.get(p)) {
                    pos = new int[2];
                    pos[0] = -codea;
                    pos[1] = pair(p + 1, steps + 1);
                    dimacs.add(pos);
                    nap[p][i] = true;
                    comptenap[p]++;
                }
            }
            //encodage des effet négatife des actions
            for (int p=0; p<negative.size();p++) {
                if(negative.get(p)) {
                    neg = new int[2];
                    neg[0] =-codea;
                    neg[1] =-pair(p+1,steps+1);
                    dimacs.add(neg);
                    pan[p][i]=true;
                    comptepan[p]++;

                }

            }
        }

        int indicel1;
        int indicel2;
        for (int r=0;r<tailleobj;r++) {
            indicel1=0;
            indicel2=0;
            //encodage des State transitions pour le passage de négatife a positife
            if(comptenap[r]>0){
                int []l1=new int [comptenap[r]+2];
                l1[0]=pair(r+1,steps);
                l1[1]=-pair(r+1,steps+1);
                for(int y=0;y<problem.getOperators().size();y++) {
                    if (nap[r][y]) {
                        l1[indicel1 + 2] = pair(y + 1 + tailleobj, steps);
                        indicel1++;
                    }
                }
                dimacs.add(l1);
            }
            //encodage des State transitions pour le passage de positife a négatife
            if(comptepan[r]>0){
                int []l2=new int [comptepan[r]+2];
                l2[0]=-pair(r+1,steps);
                l2[1]=pair(r+1,steps+1);
                for(int y=0;y<problem.getOperators().size();y++){
                    if (pan[r][y]) {
                        l2[indicel2 + 2] = pair(y+1+tailleobj, steps);
                        indicel2++;
                    }
                }
                dimacs.add(l2);
            }
        }
        steps++;
        return dimacs;
    }

    public int[][] goal(final CodedProblem problem){
        // We get the goal from the planning problem
        BitVector goalp = problem.getGoal().getPositive();
        BitVector goaln = problem.getGoal().getNegative();
        int code;
        int taille=goalp.size();
        int [][] res=new int[taille][1];
        for(int t=0;t<taille;t++){
            res[t][0]=0;
        }
        for(int go=0; go<taille;go++){
            if (goalp.get(go)){
                code=pair(go+1,steps);
                res[go][0]=code;
            }/*
            else if(goaln.get(go)){
                res[go][0]=-code;
            }*/
        }
        return res;
    }


    public void decode(int [] resultat, final CodedProblem problem, Plan plan){
        int a[];
        for (int h=0;h<steps;h++) {
            for (int i : resultat) {
                if (i > 0) {
                    a = unpair(i);
                    if (a[0] > tailleobj && a[1] == h) {
                        plan.add(h,problem.getOperators().get(a[0]-1-tailleobj));
                    } /*else {
                        //dans se else nous pouvons afficher l'état de chaque objet selon la profondeur
                        if (a[1] == h) {
                            System.out.println(problem.toString(problem.getRelevantFacts().get(a[0] - 1)) + " a l'etape " + a[1]);
                        }
                    }*/
                }
            }
            //System.out.println("--------------------------------------------------------");
        }
    }

    private static int pair(int a, int b) {
        return (((a+b)*(a+b+1))/2)+b;
    }


    private static int[] unpair(int c) {
        double t = (double) (Math.floor((Math.sqrt(8 * c + 1) - 1) / 2));
        double x = t * (t + 3) / 2 - c;
        double y = c - t * (t + 1) / 2;
        return new int[]{(int)x, (int)y};
    }

    public int  getsteps() {
        return steps;
    }
}