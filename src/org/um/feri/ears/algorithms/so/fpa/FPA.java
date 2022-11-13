/*
 * Flower Pollination Algorithm by Xin-She Yang in Java.
 */
package org.um.feri.ears.algorithms.so.fpa;

import org.apache.commons.math3.special.Gamma;
import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.problems.DoubleSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.comparator.TaskComparator;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.algorithms.so.fpa.Distribution;


import java.util.ArrayList;

public class FPA extends Algorithm {

    @AlgorithmParameter(name = "population size")
    private int popSize;
    @AlgorithmParameter
    private double lambda;
    @AlgorithmParameter(name = "switch probability")
    private double switchProbability;
    // ND: Normal distribution
    private static final double meanND = 0.0;
    private static final double stdDevND = 1.0;

    private Task task;
    private DoubleSolution best;
    private ArrayList<DoubleSolution> population;

    public FPA() {
        this(20, 1.5, 0.8);
    }

    public FPA(int popSize, double lambda, double switchProbability) {
        super();
        this.popSize = popSize;
        this.lambda = lambda;
        this.switchProbability = switchProbability;

        au = new Author("miha", "miha.ravber@um.si");
        ai = new AlgorithmInfo("FPA", "Flower Pollination Algorithm",
                "@inproceedings{yang2012flower,"
                        + "title={Flower pollination algorithm for global optimization},"
                        + "author={Yang, Xin-She},"
                        + "booktitle={International Conference on Unconventional Computing and Natural Computation},"
                        + "pages={240--249},"
                        + "year={2012},"
                        + "organization={Springer}}"
        );
    }

    @Override
    public DoubleSolution execute(Task task) throws StopCriterionException {
        this.task = task;
        initPopulation();

        double[] candidate;
        double[] levy = new double[this.task.getNumberOfDimensions()];

        int rand1, rand2;
        double epsilon;
        while (!this.task.isStopCriterion()) {

            for (int i = 0; i < popSize; i++) {

                candidate = new double[this.task.getNumberOfDimensions()];
                if (Util.nextDouble() > switchProbability) {
                    /* Global Pollination */
                    levy = levy();

                    for (int j = 0; j < this.task.getNumberOfDimensions(); j++) {
                        candidate[j] = population.get(i).getValue(j) + levy[j] * (best.getValue(j) - population.get(i).getValue(j));
                    }
                } else {
                    /* Local Pollination */
                    epsilon = Util.nextDouble();

                    do {
                        rand1 = Util.nextInt(popSize);
                    } while (rand1 == i);
                    do {
                        rand2 = Util.nextInt(popSize);
                    } while (rand2 == rand1);

                    for (int j = 0; j < this.task.getNumberOfDimensions(); j++)
                        candidate[j] += epsilon * (population.get(rand1).getValue(j) - population.get(rand2).getValue(j));
                }

                // Check bounds
                candidate = this.task.setFeasible(candidate);

                // Evaluate new solution
                if (this.task.isStopCriterion())
                    break;
                DoubleSolution newSolution = this.task.eval(candidate);

                // If the new solution is better: Replace
                if (this.task.isFirstBetter(newSolution, population.get(i))) {
                    population.set(i, newSolution);
                }

                // Update best solution
                if (this.task.isFirstBetter(newSolution, best)) {
                    best = new DoubleSolution(newSolution);
                }

            }

            this.task.incrementNumberOfIterations();
        }

        return best;
    }

    /**
     * creates Levy flight samples
     */
    private double[] levy() {
        double[] step = new double[task.getNumberOfDimensions()];

        double sigma = Math.pow(Gamma.gamma(1 + lambda) * Math.sin(Math.PI * lambda / 2)
                / (Gamma.gamma((1 + lambda) / 2) * lambda * Math.pow(2, (lambda - 1) / 2)), (1 / lambda));

        for (int i = 0; i < task.getNumberOfDimensions(); i++) {

            double u = Distribution.normal(Util.rnd, meanND, stdDevND) * sigma;
            double v = Distribution.normal(Util.rnd, meanND, stdDevND);

            step[i] = 0.01 * u / (Math.pow(Math.abs(v), (1 - lambda)));
        }
        return step;
    }

    private void initPopulation() throws StopCriterionException {
        population = new ArrayList<DoubleSolution>();

        for (int i = 0; i < popSize; i++) {
            if (task.isStopCriterion())
                break;
            DoubleSolution newSolution = task.getRandomEvaluatedSolution();
            population.add(newSolution);
        }

        population.sort(new TaskComparator(task));
        best = new DoubleSolution(population.get(0));
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
    }
}
