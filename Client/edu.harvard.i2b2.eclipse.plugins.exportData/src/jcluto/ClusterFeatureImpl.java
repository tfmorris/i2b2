/*
 * @(#) $RCSfile: ClusterFeatureImpl.java,v $ $Revision: 1.3 $ $Date: 2008/11/18 16:49:18 $ $Name: RELEASE_1_3_1_0001b $
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
 * ClusterFeatures holds the set of features (i.e., columns of the matrix) that
 * are most descriptive of each cluster and the set of features that best
 * discriminate each cluster from the rest of the objects.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/18 16:49:18 $ $Name: RELEASE_1_3_1_0001b $
 */
public class ClusterFeatureImpl implements ClusterFeatures {
    ClutoSolution cs;
    int nfeatures;
    int nclusters;
    int[] internalids;
    float[] internalwgts;
    int[] externalids;
    float[] externalwgts;

    /**
     * Get the set of features (i.e., columns of the matrix) that are most
     * descriptive of each cluster and the set of features that best
     * discriminate each cluster from the rest of the objects.
     * 
     * @param clutoSolution
     *            from which to determine the features.
     * @param numFeatures
     *            The number of features to determine for each cluster.
     */
    public ClusterFeatureImpl(ClutoSolution clutoSolution, int numFeatures) {
	nfeatures = numFeatures;
	cs = clutoSolution;
	ClutoMatrix cMat = cs.getMatrix();
	ClutoParams cParam = cs.getParams();
	nclusters = cParam.getNumClusters();
	internalids = new int[nclusters * nfeatures];
	internalwgts = new float[nclusters * nfeatures];
	externalids = new int[nclusters * nfeatures];
	externalwgts = new float[nclusters * nfeatures];
	JClutoWrapper
		.V_GetClusterFeatures(cMat.getRowCount(),
			cMat.getColumnCount(), cMat.getRowPtr(), cMat
				.getRowInd(), cMat.getRowVal(), cParam
				.getSimFunc(), cParam.getRowModel(), cParam
				.getColModel(), cParam.getColPrune(),
			nclusters, cs.getParts(), nfeatures, internalids,
			internalwgts, externalids, externalwgts);
    }

    /**
     * Return the ClutoSolution that produced these features.
     * 
     * @return the ClutoSolution that produced these features.
     * @see ClutoSolution
     */
    public ClutoSolution getClutoSolution() {
	return cs;
    }

    /**
     * Return the number of clusters.
     * 
     * @return the number of clusters
     */
    public int getClusterCount() {
	return nclusters;
    }

    /**
     * The number of features for each cluster.
     * 
     * @return The number of features for each cluster.
     */
    public int getFeatureCount() {
	return nfeatures;
    }

    /**
     * Get the id (column index) of the feature (0 to getFeatureCount()-1, with
     * 0 being the most descriptive id) for the given cluster.
     * 
     * @return the id for the given descriptive feature of the cluster.
     */
    public int getInternalID(int cluster, int feature) {
	return internalids[cluster * nfeatures + feature];
    }

    /**
     * Get the id (column index) of the feature (0 to getFeatureCount()-1, with
     * 0 being the most discriminating id) for the given cluster.
     * 
     * @return the id for the given discriminating feature of the cluster.
     */
    public int getExternalID(int cluster, int feature) {
	return externalids[cluster * nfeatures + feature];
    }

    /**
     * Get the value of the feature (0 to getFeatureCount()-1, with 0 being the
     * most descriptive id) for the given cluster.
     * 
     * @return the value for the given descriptive feature of the cluster.
     */
    public float getInternalWgt(int cluster, int feature) {
	return internalwgts[cluster * nfeatures + feature];
    }

    /**
     * Get the value of the feature (0 to getFeatureCount()-1, with 0 being the
     * most discriminating id) for the given cluster.
     * 
     * @return the value for the given discriminating feature of the cluster.
     */
    public float getExternalWgt(int cluster, int feature) {
	return externalwgts[cluster * nfeatures + feature];
    }
}
