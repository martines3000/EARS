package org.um.feri.ears.problems.moo;

import java.util.ArrayList;
import java.util.List;

import org.um.feri.ears.quality_indicator.QualityIndicator;
import org.um.feri.ears.util.Util;

public abstract class DoubleMOProblem extends MOProblemBase<Double> {

    //protected List<Objective> functions = new ArrayList<Objective>();

    public DoubleMOProblem(int numberOfDimensions, int numberOfConstraints, int numberOfObjectives) {
        super(numberOfDimensions, numberOfConstraints, numberOfObjectives);
    }

    public void evaluate(MOSolutionBase<Double> solution) {
        double[] obj = evaluate(solution.getVariables());
        solution.setObjectives(obj);
    }

    @Override
    public boolean areDimensionsInFeasableInterval(ParetoSolution<Double> ps) {

        for (MOSolutionBase<Double> sol : ps) {
            for (int i = 0; i < numberOfDimensions; i++) {
                if (sol.getValue(i) < lowerLimit.get(i))
                    return false;
                if (sol.getValue(i) > upperLimit.get(i))
                    return false;
            }
        }
        return true;
    }

    @Override
    public double[] evaluate(Double[] ds) {
        double[] x = new double[numberOfDimensions];
        for (int i = 0; i < numberOfDimensions; i++)
            x[i] = ds[i];
        double[] obj = new double[functions.size()];
        for (int i = 0; i < obj.length; i++) {
            obj[i] = functions.get(i).eval(x);
        }
        return obj;
    }

    private double[] evaluate(List<Double> variables) {
        double[] x = new double[numberOfDimensions];
        for (int i = 0; i < numberOfDimensions; i++)
            x[i] = variables.get(i);
        double[] obj = new double[functions.size()];
        for (int i = 0; i < obj.length; i++) {
            obj[i] = functions.get(i).eval(x);
        }
        return obj;
    }

    @Override
    public MOSolutionBase<Double> getRandomSolution() {

        List<Double> var = new ArrayList<Double>(numberOfDimensions);
        //Double[] var = new Double[numberOfDimensions];
        for (int j = 0; j < numberOfDimensions; j++) {
            var.add(j, Util.nextDouble(lowerLimit.get(j), upperLimit.get(j)));
        }
        MOSolutionBase<Double> sol = new MOSolutionBase<Double>(var, evaluate(var), upperLimit, lowerLimit);
        evaluateConstraints(sol);

        return sol;
    }

    public abstract void evaluateConstraints(MOSolutionBase<Double> solution);

    public boolean isFirstBetter(ParetoSolution<Double> x, ParetoSolution<Double> y, QualityIndicator<Double> qi) {
        return x.isFirstBetter(y, qi);
    }
}
