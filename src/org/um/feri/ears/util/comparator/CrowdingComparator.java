//  CrowdingComparator.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.um.feri.ears.util.comparator;

import java.util.Comparator;

import org.um.feri.ears.problems.moo.MOSolutionBase;

/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on the crowding distance, as in NSGA-II.
 */
public class CrowdingComparator<Type> implements Comparator<MOSolutionBase<Type>> {

	public class RankComparator<Type> implements Comparator<MOSolutionBase<Type>>{
		/**
		 * Compares two solutions.
		 * @param solution1 Object representing the first <code>Solution</code>.
		 * @param solution2 Object representing the second <code>Solution</code>.
		 * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
		 * respectively.
		 */
		public int compare(MOSolutionBase<Type> solution1, MOSolutionBase<Type> solution2) {

			if (solution1 == null)
				return 1;
			else if (solution2 == null)
				return -1;

			return Double.compare(solution1.getRank(), solution2.getRank());
		}
	}

	/**
	 * stores a comparator for check the rank of solutions
	 */
	private final Comparator<MOSolutionBase<Type>> rankComparator = new RankComparator<>();

	/**
	 * Compare two solutions.
	 * 
	 * @param solution1 Object representing the first <code>Solution</code>.
	 * @param solution2 Object representing the second <code>Solution</code>.
	 * @return -1, or 0, or 1 if solution1 is less than, equal, or greater than solution2, respectively.
	 */
	public int compare(MOSolutionBase<Type> solution1, MOSolutionBase<Type> solution2) {
		if (solution1 == null)
			return 1;
		else if (solution2 == null)
			return -1;

		int flagComparatorRank = rankComparator.compare(solution1, solution2);
		if (flagComparatorRank != 0)
			return flagComparatorRank;

		/* His rank is equal, then distance crowding comparator */
		double distance1 = solution1.getCrowdingDistance();
		double distance2 = solution2.getCrowdingDistance();
		return Double.compare(distance2, distance1);
	}
}
