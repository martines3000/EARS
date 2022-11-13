package org.um.feri.ears.algorithms.so.jDElscop;
import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.problems.DoubleSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.algorithms.so.jDElscop.jDElscopSolution;


public class jDElscop extends Algorithm {
    /*
     * Recoded based on cpp code from Janez Brest jDElscop
     * http://sci2s.ugr.es/EAMHCO#Software
     */
    public enum Strategy {
		STRATEGY_JDE_BEST, STRATEGY_JDE_BIN, STRATEGY_JDE_EXP;
        public static int COUNT = Strategy.values().length;
    }

    private int popSize;
    private jDElscopSolution[] popX; // population
    private jDElscopSolution g; // global best
    private Task task; // set it in run

    private void initPopulation() throws StopCriterionException {
        popX = new jDElscopSolution[popSize];
        for (int i = 0; i < popSize; i++) {
            popX[i] = jDElscopSolution.setInitState(task.getRandomEvaluatedSolution());
            if (i == 0)
                g = popX[0];
            else if (task.isFirstBetter(popX[i], g))
                g = popX[i];
            if (task.isStopCriterion())
                break;
        }
    }

    public jDElscop() {
        this(100);
    }

    public jDElscop(int popSize) {
        super();
        debug = false;
        this.popSize = popSize;

        setDebug(debug); // EARS prints some debug info
        ai = new AlgorithmInfo(
                "jDElscop", "jDElscop",
                "@Article{Brest2010,author=\"Brest, Janez and Mau{\\v{c}}ec, Mirjam Sepesy\",title=\"Self-adaptive differential evolution algorithm using population size reduction and three strategies\",journal=\"Soft Computing\",year=\"2010\",volume=\"15\",number=\"11\",pages=\"2157--2174\",issn=\"1433-7479\",doi=\"10.1007/s00500-010-0644-5\",url=\"http://dx.doi.org/10.1007/s00500-010-0644-5\"}"
        );
        au = new Author("Matej", "matej.crepinsek@um.si");
    }

    @Override
    public DoubleSolution execute(Task task) throws StopCriterionException {
        this.task = task; // used in functions
        initPopulation();
        // int iteration=0;
        Strategy strategy; // =StrategyDE.STRATEGY_jDEbest;
        int MaxFES = task.getMaxEvaluations();
        int numReduce = 3; // 7.2.2010 pmax := numReduce+1 ==> pmax = 3+1 = 4
        int variablePopSize = this.popSize; // variable population sizebased on
        // numReduce
        int r1, r2, r3;
        double tau1 = 0.1;
        double tau2 = 0.1;
        double F, CR;
        int L, n;
        double[] tmp;
        double[] tmpPar;
        int offset;
        int D = task.getNumberOfDimensions();
        jDElscopSolution[] popTmp = new jDElscopSolution[variablePopSize];
        while (!task.isStopCriterion()) {
            // iteration++;
            for (int i = 0; i < variablePopSize; i++) {
                if (Util.rnd.nextDouble() < 0.1
                        && task.getNumberOfEvaluations() > MaxFES / 2)
                    strategy = Strategy.STRATEGY_JDE_BEST;
                else if (i < variablePopSize / 2)
                    strategy = Strategy.STRATEGY_JDE_BIN;
                else
                    strategy = Strategy.STRATEGY_JDE_EXP;
                // jDe
                tmp = popX[i].getDoubleVariables();
                tmpPar = popX[i].getNewPara();
                do {
                    r1 = Util.rnd.nextInt(variablePopSize);
                } while (r1 == i);
                do {
                    r2 = Util.rnd.nextInt(variablePopSize);
                } while (r2 == i || r2 == r1);
                do {
                    r3 = Util.rnd.nextInt(variablePopSize);
                } while (r3 == i || r3 == r1 || r3 == r2);
                offset = strategy.ordinal() * 2; // 0, 2, 4
                n = Util.rnd.nextInt(D);
                double F_l = 0.1 + Math.sqrt(1.0 / variablePopSize); // for small NP
                double CR_l = 0;
                double CR_u = 0.95;
                switch (strategy) {
                    case STRATEGY_JDE_BEST:
                        CR_l = 0.00;
                        CR_u = 1.0;
                        break;
                    case STRATEGY_JDE_EXP:
                        CR_l = 0.3;
                        CR_u = 1.0;
                        F_l = 0.5;
                        break;
                    case STRATEGY_JDE_BIN:
                        F_l = 0.4;
                        CR_l = 0.7;
                }// jDEbest
                // *** LOWER and UPPER values for strategies ***
                if (Util.rnd.nextDouble() < tau1) {
                    tmpPar[offset] = F = F_l + Util.rnd.nextDouble()
                            * (1. - F_l);
                } else {
                    F = tmpPar[offset];
                }
                if (Util.rnd.nextDouble() < tau2) {
                    tmpPar[1 + offset] = CR = CR_l + Util.rnd.nextDouble()
                            * (CR_u - CR_l);
                } else {
                    CR = tmpPar[1 + offset];
                }
                switch (strategy) {
                    case STRATEGY_JDE_BEST:
                        for (L = 0; L < D; L++) /* perform D binomial trials */ {
                            if ((Util.rnd.nextDouble() < CR) || L == (D - 1)) {
                                tmp[n] = F
                                        * (g.getValue(n))
                                        + F
                                        * (popX[r2].getValue(n) - popX[r3].getValue(n));
                            }
                            n = (n + 1) % D;
                        }
                        break;
                    case STRATEGY_JDE_EXP:
                        if (Util.rnd.nextDouble() < 0.75
                                && task.isFirstBetter(popX[r3], popX[r2]))
                            F = -F;

                        L = 0;
                        // F = 0.5; CR=0.9;
                        do {
                            tmp[n] = popX[r1].getValue(n) + F
                                    * (popX[r2].getValue(n) - popX[r3].getValue(n));
                            n = (n + 1) % D;
                            L++;
                        } while ((Util.rnd.nextDouble() < CR) && (L < D));

                        break;
                    case STRATEGY_JDE_BIN:
                        if (Util.rnd.nextDouble() < 0.75
                                && task.isFirstBetter(popX[r3], popX[r2]))
                            F = -F;

                        for (L = 0; L < D; L++) /* perform D binomial trials */ {
                            if ((Util.rnd.nextDouble() < CR) || L == (D - 1)) {
                                tmp[n] = popX[r1].getValue(n)
                                        + F
                                        * (popX[r2].getValue(n) - popX[r3].getValue(n));
                            }
                            n = (n + 1) % D;
                        }

                }
                for (int j = 0; j < D; j++) {
                    tmp[j] = task.setFeasible(tmp[j], j); // in bounds
                }
                DoubleSolution tmpI = task.eval(tmp);
                if (task.isFirstBetter(popX[i], tmpI)) { // old is better
                    popTmp[i] = popX[i];
                } else {
                    popTmp[i] = jDElscopSolution.setParamState(tmpI, tmpPar);
                    if (task.isFirstBetter(popTmp[i], g)) {
                        g = popTmp[i];
                        if (debug) {
                            System.out.println("time:"
                                    + task.getNumberOfEvaluations() + " " + g);
                        }
                    }
                }
                if (task.isStopCriterion())
                    break;
            }
            /*
             * if (numReduce > 0 && task.getNumberOfEvaluations() % (MaxFES /
             * (numReduce + 1)) == (MaxFES / (numReduce + 1) - 1) && variablePopSize >
             * 10 && task.getNumberOfEvaluations() < MaxFES - 1) {
             */
            if (numReduce > 0
                    && (task.getNumberOfEvaluations()
                    % (MaxFES / (numReduce + 1)) == 0) && variablePopSize > 10) {
                /*
                 * ASK Not used imin ASK numReduce--? if (imin < variablePopSize / 2)
                 * imin = imin / 2; else imin = (imin - NP / 2) / 2;
                 */
                for (int ii = 0; ii < variablePopSize / 2; ii++) { // take best
                    // half!
                    if (task.isFirstBetter(popTmp[variablePopSize / 2 + ii],
                            popTmp[ii])) {
                        popX[ii] = popTmp[variablePopSize / 2 + ii];
                    } else {
                        popX[ii] = popTmp[ii];
                    }

                }
                variablePopSize = (variablePopSize + 1) / 2; // NP = popReduceS2(newPop,
                // extDim, NP, cost);
                System.out.println("Reduce from:" + this.popSize + " to "
                        + variablePopSize + "  time:" + task.getNumberOfEvaluations());

            } else {
				// take best half!
				if (variablePopSize >= 0) System.arraycopy(popTmp, 0, popX, 0, variablePopSize);
            }
            task.incrementNumberOfIterations();
        }
        return g;
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
    }
}
