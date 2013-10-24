/*
 * @(#) $RCSfile: TransOptions.java,v $ $Revision: 1.3 $ $Date: 2008/11/26 14:59:01 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2002. The Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * see: http://www.gnu.org/copyleft/gpl.html
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package jcluto;

/**
 * Saves the options selected for matrix transformations and missing value
 * estimation.
 * 
 * @author Shulan Tian
 * @version $Revision: 1.3 $ $Date: 2008/11/26 14:59:01 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class TransOptions implements TransParams {
    int estMethod;
    int numNeighbors;
    boolean mean;
    boolean median;
    boolean log;
    int dbglvl;

    /**
     * Constructor of the class
     * 
     * @param source
     *            The matrix transformation options
     */
    public TransOptions(TransParams source) {
	setValues(source);
    }

    /**
     * Get the missing value estimating method.
     * 
     * @return the integer associated with method
     */
    public int getEstMethod() {
	return estMethod;
    }

    /**
     * Get the number of neighbors used when estimating missing values with the
     * K nearest neighbors.
     * 
     * @return number of neighbors
     */
    public int getNumNeighbors() {
	return numNeighbors;
    }

    /**
     * Return whether mean centering should be performed on the matrix values.
     * 
     * @return whether the mean centering option is selected.
     */
    public boolean getMeanCenter() {
	return mean;
    }

    /**
     * Return whether median centering should be performed on the matrix values.
     * 
     * @return whether the median centering option is selected.
     */
    public boolean getMedianCenter() {
	return median;
    }

    /**
     * Return whether a log transformation should be performed on the matrix
     * values. get log transformation option
     * 
     * @return whether the log transformation option is selected
     */
    public boolean getLogTrans() {
	return log;
    }

    /**
     * get debug option
     * 
     * @return debug option
     */
    public int getDbgLvl() {
	return dbglvl;
    }

    /**
     * Set the matrix transformation values to be the same as the given params.
     * 
     * @param params
     *            The
     */
    public void setValues(TransParams params) {
	estMethod = params.getEstMethod();
	numNeighbors = params.getNumNeighbors();
	mean = params.getMeanCenter();
	median = params.getMedianCenter();
	log = params.getLogTrans();
	dbglvl = params.getDbgLvl();
    }
}
