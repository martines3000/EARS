/* Copyright 2009-2015 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.um.feri.ears.operators;
import org.um.feri.ears.problems.moo.MOSolutionBase;
import org.um.feri.ears.problems.moo.ParetoSolution;
import org.um.feri.ears.util.comparator.ObjectiveComparator;
import org.um.feri.ears.util.Util;

public class VEGASelection<Type extends Number> {
	
	private final ObjectiveComparator<Type> comparator;
	
	/**
	 * The tournament size. This is the number of solutions sampled from which
	 * the tournament winner is selected.
	 */
	private int size;

	/**
	 * Constructs a binary tournament selection operator using the specified
	 * dominance comparator.
	 * 
	 * @param comparator the comparator used to determine the tournament winner
	 */
	public VEGASelection(ObjectiveComparator<Type>  comparator) {
		this(2, comparator);
	}

	/**
	 * Constructs a tournament selection operator of the specified size and
	 * using the specified dominance comparator.
	 * 
	 * @param size the tournament size
	 * @param comparator the comparator used to determine the tournament winner
	 */
	public VEGASelection(int size, ObjectiveComparator<Type>  comparator) {
		this.size = size;
		this.comparator = comparator;
	}
	
	/**
	 * Performs deterministic tournament selection with the specified
	 * population, returning the tournament winner. If more than one solution is
	 * a winner, one of the winners is returned with equal probability.
	 * 
	 * @param coralReef the population from which candidate solutions are
	 *        selected
	 * @return the winner of tournament selection
	 */
	public MOSolutionBase<Type> execute(Object object) {
		ParetoSolution<Type> population = (ParetoSolution<Type>) object;
		MOSolutionBase<Type> winner = population.get(Util.nextInt(population.size()));

		for (int i = 1; i < size; i++) {
			MOSolutionBase<Type> candidate = population
					.get(Util.nextInt(population.size()));

			int flag = comparator.compare(winner, candidate);

			if (flag > 0) {
				winner = candidate;
			}
		}

		return winner;
	}
	

}
