package org.um.feri.ears.algorithms.so.abc;

import java.util.ArrayList;

import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.problems.DoubleSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.algorithms.so.abc.ABCSolution;

public class ABC extends Algorithm {

    @AlgorithmParameter(name = "population size")
    private int popSize; // The colony size (employed bees + onlooker bees)
    private int foodNumber;
    @AlgorithmParameter(description = "maximum cycle number")
    private int limit;

    private Task task;
    private ABCSolution best;
    protected ArrayList<ABCSolution> population;

    public ABC() {
        this(60);
    }

    public ABC(int popSize) {
        super();
        this.popSize = popSize;
        this.foodNumber = popSize / 2;

        au = new Author("miha", "miha.ravber@um.si");
        ai = new AlgorithmInfo("ABC", "Artificial Bee Colony",
                "@article{karaboga2007powerful,"
                        + "title={A powerful and efficient algorithm for numerical function optimization: artificial bee colony (ABC) algorithm},"
                        + "author={Karaboga, Dervis and Basturk, Bahriye},"
                        + "journal={Journal of global optimization},"
                        + "volume={39},"
                        + "number={3},"
                        + "pages={459--471},"
                        + "year={2007},"
                );
    }

    @Override
    public DoubleSolution execute(Task taskProblem) throws StopCriterionException {
        task = taskProblem;
        limit = (popSize * task.getNumberOfDimensions()) / 2;
        initPopulation();

        while (!task.isStopCriterion()) {

            sendEmployedBees();
            calculateProbabilities();
            sendOnlookerBees();
            memorizeBestSource();
            sendScoutBees();

            task.incrementNumberOfIterations();
        }
        return best;
    }

    private void sendScoutBees() throws StopCriterionException {

        for (int i = 0; i < foodNumber; i++) {
            if (population.get(i).trials >= limit) {
                if (task.isStopCriterion())
                    return;
                ABCSolution newBee = new ABCSolution(task.getRandomEvaluatedSolution());
                population.set(i, newBee);
            }
        }

    }

    private void memorizeBestSource() {

        for (ABCSolution bee : population) {
            if (task.isFirstBetter(bee, best)) {
                best = new ABCSolution(bee);
            }
        }

    }

    private void sendOnlookerBees() throws StopCriterionException {

        int neighbour, param2change;
        double phi, newValue;
        int t = 0, i = 0;

        while (t < foodNumber) {
            if (Util.nextDouble() < population.get(i).getProb()) {
                t++;

                //A randomly chosen solution is used in producing a mutant solution of the solution i
                neighbour = Util.nextInt(foodNumber);
                while (neighbour == i) //Randomly selected solution must be different from the solution i
                    neighbour = Util.nextInt(foodNumber);

                //The parameter to be changed is determined randomly
                param2change = Util.nextInt(task.getNumberOfDimensions());

                phi = Util.nextDouble(-1, 1);

                newValue = population.get(i).getValue(param2change) + (population.get(i).getValue(param2change) - population.get(neighbour).getValue(param2change)) * (phi - 0.5) * 2;
                newValue = task.setFeasible(newValue, param2change);

                ABCSolution newBee = new ABCSolution(population.get(i));
                newBee.setValue(param2change, newValue);

                if (task.isStopCriterion())
                    return;

                task.setFeasible(newBee);
                task.eval(newBee);

                if (newBee.getABCEval() > population.get(i).getABCEval()) {
                    newBee.trials = 0;
                    population.set(i, newBee);
                } else {
                    population.get(i).trials++;
                }

            }
            i++;

            if (i == foodNumber)
                i = 0;
        }
    }

    private void calculateProbabilities() {
        double maxFit;
        maxFit = population.get(0).getABCEval();

        for (int i = 1; i < foodNumber; i++) {

            if (maxFit < population.get(i).getABCEval()) {
                maxFit = population.get(i).getABCEval();
            }
        }

        for (ABCSolution bee : population) {
            bee.setProb((0.9 * (bee.getABCEval() / maxFit)) + 0.1);
        }
    }

    private void sendEmployedBees() throws StopCriterionException {
        int neighbour, param2change;
        double phi, newValue;

        for (int i = 0; i < foodNumber; i++) {

            //A randomly chosen solution is used in producing a mutant solution of the solution i
            neighbour = Util.nextInt(foodNumber);
            while (neighbour == i) //Randomly selected solution must be different from the solution i
                neighbour = Util.nextInt(foodNumber);

            //The parameter to be changed is determined randomly
            param2change = Util.nextInt(task.getNumberOfDimensions());

            phi = Util.nextDouble(-1, 1);

            //TODO pomno�i samo z phi
            newValue = population.get(i).getValue(param2change) + (population.get(i).getValue(param2change) - population.get(neighbour).getValue(param2change)) * (phi - 0.5) * 2;
            newValue = task.setFeasible(newValue, param2change);

            ABCSolution newBee = new ABCSolution(population.get(i));
            newBee.setValue(param2change, newValue);

            if (task.isStopCriterion())
                return;

            task.eval(newBee);

            if (newBee.getABCEval() > population.get(i).getABCEval()) {
                newBee.trials = 0;
                population.set(i, newBee);
            } else {
                population.get(i).trials++;
            }
        }
    }

    private void initPopulation() throws StopCriterionException {
        population = new ArrayList<>();
        ABCSolution bee = new ABCSolution(task.getRandomEvaluatedSolution());
        population.add(bee);
        best = new ABCSolution(bee);
        for (int i = 0; i < foodNumber - 1; i++) {
            ABCSolution newBee = new ABCSolution(task.getRandomEvaluatedSolution());
            population.add(newBee);
            if (task.isFirstBetter(newBee, best))
                best = new ABCSolution(newBee);
            if (task.isStopCriterion())
                break;
        }
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
    }
}
