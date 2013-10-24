/*
 * @(#) $RCSfile: ClutoSolutionValues.java,v $ $Revision: 1.2 $ $Date: 2008/11/24 17:41:20 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;

/**
 * ClutoSolutionValues holds the result from executing a <b>CLUTO</b> clustering
 * method.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/11/24 17:41:20 $ $Name: RELEASE_1_3_1_0001b $
 */
public class ClutoSolutionValues extends AbstractClutoSolution implements
	Serializable {

    public ClutoSolutionValues(ClutoSolution cs, int featureCount,
	    boolean includeMatrix) {
	if (includeMatrix)
	    cMat = cs.getMatrix();
	cParam = cs.getParams();
	internalMat = cs.getInternalMatrix();
	solutionQuality = cs.getSolutionQuality();
	totalEntropy = cs.getEntropy();
	totalPurity = cs.getPurity();
	pWgts = cs.getPartWeights();
	cIntSim = cs.getIntSim();
	cIntSdev = cs.getIntStdDev();
	iZScores = cs.getIntZScores();
	cExtSim = cs.getExtSim();
	cExtSdev = cs.getExtStdDev();
	eZScores = cs.getExtZScores();
	part = cs.getParts();
	ptree = cs.getPtree();
	ftree = cs.getForwardTree();
	tsize = cs.getTreeCounts();
	tsims = cs.getTSims();
	gains = cs.getGains();
	rowMap = cs.getRowTreeOrder();
	colMap = cs.getColTreeOrder();
	clusterFeatures = cs.getClusterFeatures(featureCount);
	distMatrix = cs.getClusterDistances();
    }

    public void setMatrix(ClutoMatrix matrix) {
	cMat = matrix;
    }

}
