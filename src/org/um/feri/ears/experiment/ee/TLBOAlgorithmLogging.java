package org.um.feri.ears.experiment.ee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.algorithms.AlgorithmBase;
import org.um.feri.ears.problems.DoubleSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.comparator.TaskComparator;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.algorithms.so.tlbo.*;

public class TLBOAlgorithmLogging extends Algorithm {
    int pop_size; // = 50000; //defaults from authors
    int max_gen; // = 500000; //defaults from authors
    Task task; // To calculate fitness
    int gen;
    int num_var = 5;
    // double pmutate = 0;
    DoubleSolution population[]; // pop_size X dimension
    // double eval[]; // pop_size

    // double averageCost; //for stat
    // double minCost; //for stat
    public static boolean removeDuplicates = true;
    public Statistic stat;
    public static boolean useTF = true;
    public static boolean useAll4Mean = true;// used for internal tests
    private double lowerLimit[];
    private double upperLimit[];
    private ArrayList<DoubleSolution> keepList;
    public static boolean test = false;

    public Statistic getStat() {
        return stat;
    }

    private int Keep = 0; // copy best from ex generation

    public TLBOAlgorithmLogging() {
        this(0, 20);
    }

    public TLBOAlgorithmLogging(int Keep, int pop_size) {
        this.Keep = Keep;
        this.pop_size = pop_size;
        au = new Author("matej", "matej.crepinsek@um.si");
        ai = new AlgorithmInfo(
                "TLBO", "Teaching Learning Based Optimization",
                "\\bibitem{Rao2011}\nR.V.~Rao, V.J.~Savsani, D.P.~Vakharia.\n\\newblock Teaching-learning-based optimization: A novel method for constrained mechanical design optimization problems.\n\\newblock \\emph{Computer-Aided Design}, 43(3):303--315, 2011."
        );
    }
    
    @Override
    public DoubleSolution execute(Task taskProblem) throws StopCriterionException {
        task = taskProblem;
        num_var = task.getNumberOfDimensions();
        // max_eval = task.getMaxEvaluations();
        stat = new Statistic();
        init();
        try {
        aTeacher();
        } catch(StopCriterionException e) {
            System.out.println("KDO?"+e);
        }
        return stat.getCurrent_g().best;
    }

    private double[] mean() {
        double d[] = new double[num_var];
        int min;
        if (useAll4Mean)
            min = 0;
        else
            min = (int) (pop_size * 0.075);
        int max = pop_size - min;
        for (int i = min; i < max; i++) {
            for (int j = 0; j < num_var; j++) {
                d[j] += population[i].getValue(j);
            }
        }
        for (int j = 0; j < num_var; j++) {
            d[j] = d[j] / (max - min);
        }
        return d;
    }

    /**
     * Implemented by code Close to, but not 100% duplicates clear
     * 
     * @throws StopCriterionException
     */
    private void clearDups() throws StopCriterionException {
        double tmp1[] = new double[num_var];
        double tmp2[] = new double[num_var];
        double tmp3[];
        for (int i = 0; i < pop_size; i++) {
            for (int j = i + 1; j < pop_size; j++) {
                if (task.isStopCriterion())
                    return; // end jump out
                System.arraycopy(population[i].getDoubleVariables(), 0, tmp1, 0, num_var);
                System.arraycopy(population[j].getDoubleVariables(), 0, tmp2, 0, num_var);
                Arrays.sort(tmp1);
                Arrays.sort(tmp2);
                if (Arrays.equals(tmp1, tmp2)) {
                	List<DoubleSolution> parents = new ArrayList<DoubleSolution>();
                    // on random place change value
                    stat.getCurrent_g().incDouple();
                    int pos = Util.rnd.nextInt(num_var);
                    tmp3 = population[j].getDoubleVariables();
                    
                    parents.add(population[j]);
                    tmp3[pos] = Util.nextDouble(lowerLimit[pos], upperLimit[pos]);
                    StopCriterionException.id =" 3";
                    population[j] = task.eval(tmp3, parents);
                   
                }
            }
        }
    }


    private void sortByFirstBetterCondition() {
    	TaskComparator s = new TaskComparator(task);
        Arrays.sort(population, s);
    }

    private void init() throws StopCriterionException {

        population = new DoubleSolution[pop_size];
        lowerLimit = task.getLowerLimit();
        upperLimit = task.getUpperLimit();
        for (int i = 0; i < pop_size; i++) {
            population[i] = task.getRandomEvaluatedSolution();
            if (task.isStopCriterion())
                break;
        }
        if (TLBOAlgorithmLogging.removeDuplicates) {
            clearDups(); // stop condition inside
        }
        // printAllPopulation();
        sortByFirstBetterCondition();
        stat.getCurrent_g().setBest(population[0]);
        keepList = new ArrayList<DoubleSolution>();
    }

    private void aTeacher() throws StopCriterionException {
        int TF = 1;
        double M[];
        double tmpX[], tmpY[], tmpIsland[];
        double new_mean[] = new double[num_var];
        double Dif_mean[] = new double[num_var];
        double pop_tmp[][] = new double[pop_size][num_var];
        DoubleSolution eval_tmp[] = new DoubleSolution[pop_size];
        DoubleSolution Island_1[] = new DoubleSolution[pop_size];
        gen = 0;
        DoubleSolution bestEvalCond = stat.getBest();
        while (!task.isStopCriterion()) { // generation or evaluations
            stat.newGeneration(gen);
            M = mean();
            if (test)
                System.out.println("mean M=" + Arrays.toString(M));
            new_mean = population[0].getDoubleVariables();
            // Keep not in paper
            for (int k = 0; k < Keep; k++)
                keepList.add(new DoubleSolution(population[k]));
            // Teacher phase
            // For every dimension it calculates dif_mean
            for (int n = 0; n < num_var; n++) {
                if (useTF)
                    TF = Util.rnd.nextInt(2) + 1; // in source code is fix to 1
                Dif_mean[n] = Util.rnd.nextDouble() * (new_mean[n] - TF * M[n]);
            }
            if (test)
                System.out.println("Dif_mean Dif_mean=" + Arrays.toString(Dif_mean));
            for (int i = 0; i < pop_size; i++) {
            	List<DoubleSolution> parents = new ArrayList<DoubleSolution>();
                if (task.isStopCriterion())
                    break; // in loop after incEval
                tmpX = population[i].getDoubleVariables();
                for (int n = 0; n < num_var; n++) {
                    pop_tmp[i][n] = task.setFeasible(tmpX[n] + Dif_mean[n], n);
                }
                parents.add(population[i]);
                eval_tmp[i] = task.eval(pop_tmp[i], parents);
                if (task.isFirstBetter(eval_tmp[i], bestEvalCond))
                    bestEvalCond = eval_tmp[i]; // 4 stop condition
                stat.incEval();
                if (test)
                    System.out.println("Compare new=" + Arrays.toString(pop_tmp[i]));
                if (test)
                    System.out.println("Compare old=" + population[i]);
                if (task.isFirstBetter(eval_tmp[i], population[i])) {
                    stat.incUpdateByTeacher();
                    population[i] = eval_tmp[i];
                }
            }
            // Learner phase
            int ii = 0;
            int i_first = 0;
            for (; i_first < pop_size; i_first++) {
                if (task.isStopCriterion())
                    break; // in loop after incEval
                ii = Util.rnd.nextInt(pop_size);
                while (i_first == ii)
                    ii = Util.rnd.nextInt(pop_size); // select different pair i, ii
                double rand = Util.rnd.nextDouble();
                if (test)
                    System.out.println("Rand=" + rand);
                if (test)
                    System.out.println("\nBasic " + population[i_first]);
                if (test)
                    System.out.println("Learning partner " + population[ii]);
                tmpX = population[i_first].getDoubleVariables();
                tmpY = population[ii].getDoubleVariables();
                tmpIsland = new double[num_var];
                if (task.isFirstBetter(population[i_first], population[ii])) {
                    for (int n = 0; n < num_var; n++) {
                        tmpIsland[n] = task.setFeasible(tmpX[n] + rand * (tmpX[n] - tmpY[n]), n);
                    }
                } else {
                    for (int n = 0; n < num_var; n++) {
                        tmpIsland[n] = task.setFeasible(tmpX[n] + rand * (tmpY[n] - tmpX[n]), n);
                    }
                }
                if (test)
                    System.out.println("New " + Arrays.toString(tmpIsland));
                
                List<DoubleSolution> parents = new ArrayList<DoubleSolution>();
                parents.add(population[i_first]);
                parents.add(population[ii]);
                Island_1[i_first] = task.eval(tmpIsland, parents);
                if (task.isFirstBetter(Island_1[i_first], bestEvalCond))
                    bestEvalCond = Island_1[i_first]; // for stop condition
                                                      // ...epsilon no
                                                      // constraints checked
                stat.incEval();
            }
            // i_first instead pop_size because of possible eval stop condition!
            for (int i = 0; i < i_first; i++) { // copy best
                if (task.isFirstBetter(Island_1[i], population[i])) {
                    population[i] = Island_1[i];
                }
            }
            sortByFirstBetterCondition();
            // Keep back change worst chromosomes
            int back = pop_size - 1 - Keep;
            // int back= (int) (pop_size*0.9);
            for (int k = 0; k < Keep; k++)
                population[back + k] = keepList.get(k);
            keepList.clear();
            if (TLBOAlgorithmLogging.removeDuplicates) {
                clearDups(); // stop condition inside
            }
            sortByFirstBetterCondition();
            // averageCost = average(eval);
            // minCost = eval[0];
            // stat.getCurrent_g().setAvrEval(averageCost);
            stat.getCurrent_g().setBest(population[0]);
            gen++;
			task.incrementNumberOfIterations();
        }
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
        // it sets in init!
    }

    @Override
    public List<AlgorithmBase> getAlgorithmParameterTest(int dimension, int maxCombinations) {
        List<AlgorithmBase> alternative = new ArrayList<AlgorithmBase>();

        if (maxCombinations == 1) {
            alternative.add(this);
        } else {
            int paramCombinations[][] = { // {elite, pop_size}
            { 4, 5 + dimension * 2 }, { 0, 20 }, { 4, 20 }, { 0, 5 + dimension * 2 }, { 0, 50 }, { 4, 50 }, { 0, 100 }, { 4, 100 },
                    { 8, 100 } };
            int counter = 0;
            for (int i = 0; (i < paramCombinations.length) && (counter < maxCombinations); i++) {
                alternative.add(new TLBOAlgorithmLogging(paramCombinations[i][0], paramCombinations[i][1]));
                counter++;

            }
        }
        return alternative;
    }

}
