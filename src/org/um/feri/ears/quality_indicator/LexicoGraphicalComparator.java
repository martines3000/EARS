//  LexicoGraphicalComparator.java
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

package org.um.feri.ears.quality_indicator;

import java.util.Comparator;


/**
 * This class implements the <code>Comparator</code> interface. It is used
 * to compare points given as <code>double</code>.
 * The order used is the lexicograhphical.
 */
public class LexicoGraphicalComparator implements Comparator<double[]> {

    /**
     * The compare method compare the objects o1 and o2.
     *
     * @param o1 An object that reference a double[]
     * @param o2 An object that reference a double[]
     * @return -1 if o1 < o1, 1 if o1 > o2 or 0 in other case.
     */
    public int compare(double[] o1, double[] o2) {

        //To determine the first i, that pointOne[i] != pointTwo[i];
        int index = 0;
        while ((index < o1.length) && (index < o2.length) &&
                o1[index] == o2[index]) {
            index++;
        }
        if ((index >= o1.length) || (index >= o2.length)) {
            return 0;
        } else if (o1[index] < o2[index]) {
            return -1;
        } else if (o1[index] > o2[index]) {
            return 1;
        }
        return 0;
    }
}
