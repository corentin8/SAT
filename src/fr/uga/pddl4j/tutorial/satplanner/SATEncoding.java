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
    //les valeur a encoder d'une étape a une autre eront toujours les même et seront stocker dans les variable etape*
    private ArrayList<int []> etapeTransitionP;
    private ArrayList<int []> etapeTransitionN;
    private ArrayList<int []> etapeExclusion;
    private ArrayList<int []> etapePrecond;
    private ArrayList<int []> etapeP;
    private ArrayList<int []> etapeN;

    /*
     * Current number of steps of the SAT encoding
     */
    private int steps;
    private int tailleobj;
    private int maxclause;

    /**
     *
     * @param
     */
    public SATEncoding(final CodedProblem problem, int maxclause) {
        super();
        this.maxclause=maxclause;
        dimacs= new ArrayList<int[]>();

        etapeTransitionP= new ArrayList<int[]>();
        etapeTransitionN= new ArrayList<int[]>();
        etapeExclusion= new ArrayList<int[]>();
        etapePrecond= new ArrayList<int[]>();
        etapeP= new ArrayList<int[]>();
        etapeN= new ArrayList<int[]>();

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
        etapeSup(problem);


        // Encoding of init
        // Each fact is a unit clause


        // We get the operators of the problem

    }

    /*
     * SAT encoding for next step
     */
    public void etapeSup(final CodedProblem problem) {
        int codea;
        ArrayList<ArrayList<Integer>> transitionP= new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> transitionN= new ArrayList<ArrayList<Integer>>();
        int[] neg;
        int[] pos;
        int[] pre;


        //faire une action exclu de faire une autre action a cette étape A->!B <=> !A || !B
        int [] exclusion;
        for (int i = 0; i < problem.getOperators().size(); i++) {
            for (int j = i; j < problem.getOperators().size(); j++) {
                if(i!=j){
                    exclusion=new int[2];
                    //exclusion[0]=-pair(i+1+tailleobj,steps);
                    //exclusion[1]=-pair(j+1+tailleobj,steps);

                    exclusion[0]=i+1;
                    exclusion[1]=j+1;
                    etapeExclusion.add(exclusion);
                }
            }
        }

        //parcour de toutes opération possible
        for (int i = 0; i < problem.getOperators().size(); i++) {
            final BitOp a = problem.getOperators().get(i);
           // codea=pair(i+1+tailleobj,steps);
            codea=i+1;
            //récupération des préconditions des actions A->P <=> !A || P
            final BitVector precond = a.getPreconditions().getPositive();
            for (int p=0; p<precond.size();p++){
                if(precond.get(p)) {
                    pre = new int[2];
//                    pre[1] = -codea;
//                    pre[0] =pair(p+1,steps);
                    pre[1] =codea;
                    pre[0] =p+1;
                    etapePrecond.add(pre);
                }
            }
            final BitVector positive = a.getUnconditionalEffects().getPositive();
            final BitVector negative = a.getUnconditionalEffects().getNegative();
            //récupération des effet positife des actions   A(i)->P(i+1) <=> !A(i) || P(i+1)
            for (int p=0; p<positive.size();p++) {
                if (positive.get(p)) {
                    pos = new int[2];
//                    pos[0] = -codea;
//                    pos[1] = pair(p + 1, steps + 1);
                    pos[0] = codea;
                    pos[1] = p + 1;
                    etapeP.add(pos);
                    while(transitionP.size()<=p){
                        transitionP.add(new ArrayList<Integer>());
                    }
                    transitionP.get(p).add(i);
//                    nap[p][i] = true;
//                    comptenap[p]++;
                }
            }
            //récupération des effet négatife des actions A(i)->N(i+1) <=> !A(i) || N(i+1) N est négatife
            for (int p=0; p<negative.size();p++) {
                if(negative.get(p)) {
                    neg = new int[2];
//                    neg[0] =-codea;
//                    neg[1] =-pair(p+1,steps+1);
                    neg[0] =codea;
                    neg[1] =p+1;
                    etapeN.add(neg);
                    while(transitionN.size()<=p){
                        transitionN.add(new ArrayList<Integer>());
                    }
                    transitionN.get(p).add(i);
//                    pan[p][i]=true;
//                    comptepan[p]++;

                }

            }
        }

        int indicel1;
        int indicel2;
        if(tailleobj<transitionP.size()){
            tailleobj=transitionP.size();
        }
        for (int r=0;r<transitionP.size();r++) {
            indicel1 = 2;
            //récupération des State transitions pour le passage de négatife a positife
            if (transitionP.get(r).size() > 0) {
                int[] l1 = new int[transitionP.get(r).size() + 2];
//                l1[0]=pair(r+1,steps);
//                l1[1]=-pair(r+1,steps+1);
                l1[0] = r + 1;
                l1[1] = r + 1;
                for (int val : (transitionP.get(r))) {
                    l1[indicel1] = val + 1;
                    indicel1++;
                }
                etapeTransitionP.add(l1);
            }
        }
        if(tailleobj<transitionN.size()){
            tailleobj=transitionN.size();
        }
        for (int r=0;r<transitionN.size();r++) {
            indicel2 = 2;
            //récupération des State transitions pour le passage de positife a négatife
            if(transitionN.get(r).size()>0){
                int []l2=new int [transitionN.get(r).size()+2];
//                l2[0]=-pair(r+1,steps);
//                l2[1]=pair(r+1,steps+1);
                l2[0]=r+1;
                l2[1]=r+1;
                for(int val : (transitionN.get(r))){
                    l2[indicel2] = val+1;
                    indicel2++;
                }
                etapeTransitionN.add(l2);
            }
        }
    }

    public ArrayList<int[]> next(){

        if(maxclause<dimacs.size()+etapeTransitionP.size()+etapeTransitionN.size()+
                etapeExclusion.size()+etapePrecond.size()+etapeP.size()+etapeN.size()){
            return null;
        }
        int res[];
        for (int[] tmp : etapeExclusion){
            res= new int[2];
            res[0]=-pair(tmp[0]+tailleobj,steps);
            res[1]=-pair(tmp[1]+tailleobj,steps);
            dimacs.add(res);
        }

        for (int[] tmp : etapePrecond){
            res= new int[2];
            res[0]=-pair(tmp[1]+tailleobj,steps);
            res[1]=pair(tmp[0],steps);
            dimacs.add(res);
        }

        for (int[] tmp : etapeP){
            res= new int[2];
            res[0]=-pair(tmp[0]+tailleobj,steps);
            res[1]=pair(tmp[1],steps+1);
            dimacs.add(res);
        }

        for (int[] tmp : etapeN){
            res= new int[2];
            res[0]=-pair(tmp[0]+tailleobj,steps);
            res[1]=-pair(tmp[1],steps+1);
            dimacs.add(res);
        }

        for (int[] tmp : etapeTransitionP){
            res= new int[tmp.length];
            res[0]=pair(tmp[0],steps);
            res[1]=-pair(tmp[1],steps+1);
            for(int u=2;u<tmp.length;u++){
                res[u]=pair(tmp[u]+tailleobj,steps);
            }
            dimacs.add(res);
        }

        for (int[] tmp : etapeTransitionN){
            res= new int[tmp.length];
            res[0]=-pair(tmp[0],steps);
            res[1]=pair(tmp[1],steps+1);
            for(int u=2;u<tmp.length;u++){
                res[u]=pair(tmp[u]+tailleobj,steps);
            }
            dimacs.add(res);
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
            }
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