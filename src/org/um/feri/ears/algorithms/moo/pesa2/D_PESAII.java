package org.um.feri.ears.algorithms.moo.pesa2;

import org.um.feri.ears.algorithms.moo.nsga2.NSGAII;
import org.um.feri.ears.operators.CrossoverOperator;
import org.um.feri.ears.operators.MutationOperator;
import org.um.feri.ears.operators.PolynomialMutation;
import org.um.feri.ears.operators.SBXCrossover;
import org.um.feri.ears.problems.DoubleMOTask;
import org.um.feri.ears.problems.StopCriteriaException;

public class D_PESAII extends PESAII<DoubleMOTask, Double> {
	
	public D_PESAII() {
		this(new SBXCrossover(0.9, 20.0), new PolynomialMutation(1.0 / 10, 20.0), 100, 100);
	}
	
	public D_PESAII(int populationSize) {
		this(new SBXCrossover(0.9, 20.0), new PolynomialMutation(1.0 / 10, 20.0), populationSize, populationSize);
	}
	
	public D_PESAII(int populationSize, int archiveSize) {
		this(new SBXCrossover(0.9, 20.0), new PolynomialMutation(1.0 / 10, 20.0), populationSize, archiveSize);
	}

	public D_PESAII(CrossoverOperator crossover, MutationOperator mutation, int populationSize, int archiveSize) {
		super(crossover, mutation, populationSize, archiveSize);
	}

	@Override
	public void start() throws StopCriteriaException {
		super.start();
		mut.setProbability(1.0 / num_var);
	}
	
}
