/*
 * @(#) $RCSfile: ClutoOptions.java,v $ $Revision: 1.3 $ $Date: 2008/11/20 17:35:19 $ $Name: RELEASE_1_3_1_0001b $
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

/* RULES:
 IDF column model only applies to sparse matrices and non-correlation-coefficient similarities!
 Similarity based on correlation coefficient requires more than two dimensions!
 */

/**
 * <b>CLUTO</b> clustering parameter options supply all the required parameters
 * for executing a <b>CLUTO</b> clustering method.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/20 17:35:19 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see JClutoWrapper
 */
public class ClutoOptions implements ClutoParams {
    int clusterMethod;
    int simFunc;
    int crFunc;
    int csType;
    int rowModel;
    int colModel;
    float colPrune;
    int numTrials;
    int numIter;
    int seed;
    int kwayRefine;
    int numNeighbors;
    int minCmp;
    int numClusters;
    int aggloBox;
    int aggloFrom;
    int aggloCrFunc;
    int dbglvl;

    /**
   * 
   */
    public ClutoOptions(ClutoParams source) {
	setValues(source);
    }

    // ClutoParams Interface

    /**
     * Return the selected <b>CLUTO</b> clustering similarity function.
     * 
     * @return the selected <b>CLUTO</b> clustering similarity function.
     * @see JClutoWrapper#CLUTO_SIM_COSINE
     * @see JClutoWrapper#CLUTO_SIM_CORRCOEF
     * @see JClutoWrapper#CLUTO_SIM_EDISTANCE
     * @see JClutoWrapper#CLUTO_SIM_EJACCARD
     */
    public int getClusterMethod() {
	return clusterMethod;
    }

    public int getSimFunc() {
	return simFunc;
    }

    public int getCrFunc() {
	return crFunc;
    }

    public int getCsType() {
	return csType;
    }

    public int getRowModel() {
	return rowModel;
    }

    public int getColModel() {
	return colModel;
    }

    public float getColPrune() {
	return colPrune;
    }

    public int getNumTrials() {
	return numTrials;
    }

    public int getNumIter() {
	return numIter;
    }

    public int getSeed() {
	return seed;
    }

    public int getKwayRefine() {
	return kwayRefine;
    }

    public int getNumNeighbors() {
	return numNeighbors;
    }

    public int getMinCmp() {
	return minCmp;
    }

    public int getNumClusters() {
	return numClusters;
    }

    public int showAgglomerativeParameters() {
	return aggloBox;
    }

    public int getAggloFrom() {
	return aggloFrom;
    }

    public int getAggloCrFunc() {
	return aggloCrFunc;
    }

    public int getDbgLvl() {
	return dbglvl;
    }

    public void setValues(ClutoParams params) {
	clusterMethod = params.getClusterMethod();
	simFunc = params.getSimFunc();
	crFunc = params.getCrFunc();
	csType = params.getCsType();
	rowModel = params.getRowModel();
	colModel = params.getColModel();
	colPrune = params.getColPrune();
	numTrials = params.getNumTrials();
	numIter = params.getNumIter();
	seed = params.getSeed();
	kwayRefine = params.getKwayRefine();
	numNeighbors = params.getNumNeighbors();
	minCmp = params.getMinCmp();
	numClusters = params.getNumClusters();
	aggloBox = params.showAgglomerativeParameters();
	aggloFrom = params.getAggloFrom();
	aggloCrFunc = params.getAggloCrFunc();
	dbglvl = params.getDbgLvl();
    }
}
