/*
 * @(#) $RCSfile: AbstractClutoSolution.java,v $ $Revision: 1.2 $ $Date: 2008/11/18 15:33:28 $ $Name: RELEASE_1_3_1_0001b $
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
 * AbstractClutoSolution holds the result from executing a <b>CLUTO</b>
 * clustering method.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/11/18 15:33:28 $ $Name: RELEASE_1_3_1_0001b $
 */
public class AbstractClutoSolution implements ClutoSolution, Serializable {
    ClutoMatrix cMat;
    ClutoParams cParam;
    ClutoInternalTableMatrix internalMat;
    ClusterFeatures clusterFeatures;
    float solutionQuality;
    float totalEntropy;
    float totalPurity;
    float[] fScores;
    int[] pWgts;
    float[] cIntSim;
    float[] cIntSdev;
    float[] iZScores;
    float[] cExtSim;
    float[] cExtSdev;
    float[] eZScores;
    int[] part;
    int[] ptree;
    int[][] ftree;
    int[] tsize;
    float[] tsims;
    float[] gains;
    int[] rowMap;
    int[] colMap;
    float[] distMatrix;

    /**
     * Return the matrix for which this clustering solution was derived.
     * 
     * @return the matrix for which this clustering solution.
     * @see ClutoMatrix
     */
    public ClutoMatrix getMatrix() {
	return cMat;
    }

    /**
     * The parameter settings for the <b>CLUTO</b> clustering method that
     * produced this solution.
     * 
     * @return parameter settings for the <b>CLUTO</b> clustering method.
     * @see ClutoParams
     */
    public ClutoParams getParams() {
	return cParam;
    }

    /**
     * This function returns the value of the clustering criterion function of
     * the supplied clustering solution.
     * 
     * @return the value of the clustering criterion function.
     * @see ClutoParams#getCrFunc
     */
    public float getSolutionQuality() {
	return solutionQuality;
    }

    /**
     * Small entropy values and large purity values indicate good clustering
     * solutions.
     * 
     * @return the entropy value of the clustering solution.
     */
    public float getEntropy() {
	return totalEntropy;
    }

    /**
     * Small entropy values and large purity values indicate good clustering
     * solutions.
     * 
     * @return the purity value of the clustering solution.
     */
    public float getPurity() {
	return totalPurity;
    }

    /**
     * This is an array of size nrows that upon successful completion stores the
     * clustering vector of the matrix. The i th entry of this array stores the
     * cluster number that the i th row of the matrix belongs to. Note that the
     * numbering of the clusters starts from zero. Under certain circumstances,
     * <b>CLUTO</b> may not be able to assign a particular row to a cluster. In
     * this case, the getParts()[] entry of that particular row will be set to
     * -1.
     * 
     * @return the cluster number assignment for each row.
     */
    public int[] getParts() {
	return part;
    }

    /**
     * This is an array of size 2*nrows that upon successful completion stores
     * the parent array of the binary hierarchical tree. In this tree, each node
     * corresponds to a cluster. The leaf nodes are the original nrows objects,
     * and they are numbered from 0 to nrows-1. The internal nodes of the tree
     * are numbered from nrows to 2*nrows-2. The numbering of the internal nodes
     * is performed so that smaller numbers correspond to clusters obtained by
     * merging a pair of clusters earlier during the agglomeration process. The
     * root of the tree is numbered 2*nrows-2. The ith entry of the ptree array
     * stores the parent node of the i node of the tree. The ptree entry for the
     * root is set to -1.
     * 
     * @return the parent array for the clustering solution.
     */
    public int[] getPtree() {
	return ptree;
    }

    /**
     * This is an array of size 2*nrows that upon successful completion stores
     * the average similarity between every pair of siblings in the induced
     * tree. In particular, getTSims()[i] stores the average pairwise similarity
     * between the pair of clusters that are the children of the ith node of the
     * tree. Note that the first nrows entries of this vector are not defined
     * and are set to 0.0.
     * 
     * @return similarity between every pair of siblings in the induced tree.
     */
    public float[] getTSims() {
	return tsims;
    }

    /**
     * This is an array of size 2*nrows that upon successful completion stores
     * the gains in the value of the criterion function resulted by the merging
     * pairs of clusters. In particular, getGains()[i] stores the gain achieved
     * by merging the clusters that are the children of the ith node of the
     * tree. Note that the first nrows entries of this vector are not defined
     * and are set to 0.0.
     * 
     * @return the gains in the value of the criterion function.
     */
    public float[] getGains() {
	return gains;
    }

    /**
     * An array of size nclusters that returns the sizes of the different
     * clusters. In particular, the size of the ith cluster is returned in
     * getPartWeights()[i].
     * 
     * @return the sizes of the different clusters.
     */
    public int[] getPartWeights() {
	return pWgts;
    }

    /**
     * An array of size nclusters that returns the average similarity between
     * the objects assigned to each cluster. In particular, the average
     * similarity between the objects of the ith cluster is returned in
     * getIntSim()[i].
     * 
     * @return the average similarity between the objects assigned to each
     *         cluster.
     */
    public float[] getIntSim() {
	return cIntSim;
    }

    /**
     * An array of size nclusters that returns the standard deviation of the
     * average similarity between each object and the other objects in its own
     * cluster. In particular, the standard deviation of the ith cluster is
     * returned in getIntStdDev()[i].
     * 
     * @return the standard deviation of the average similarity.
     */
    public float[] getIntStdDev() {
	return cIntSdev;
    }

    /**
     * An array of size nrows that returns the internal z-scores of each object.
     * The internal z-score of the ith object is returned in getIntZScores()[i].
     * The internal z-score of each object is described in the discussion of the
     * -zscores option of vcluster.
     * 
     * @return the internal z-scores of each object.
     */
    public float[] getIntZScores() {
	return iZScores;
    }

    /**
     * An array of size nclusters that returns the average similarity between
     * the objects of each cluster and the remaining objects. In particular, the
     * average external similarity of the objects of the ith cluster is returned
     * in getExtSim()[i].
     * 
     * @return the average similarity between the objects of each cluster and
     *         the remaining objects.
     */
    public float[] getExtSim() {
	return cIntSim;
    }

    /**
     * An array of size nclusters that returns the standard deviation of the
     * average external similarities of each object. In particular, the external
     * standard deviation of the objects of the ith cluster is returned in
     * getExtStdDev()[i].
     * 
     * @return the standard deviation of the average external similarities
     */
    public float[] getExtStdDev() {
	return cExtSdev;
    }

    /**
     * An array of size nrows that returns the external z-scores of each object.
     * The external z-score of the ith object is returned in getExtZScores()[i].
     * The external z-score of each object is described in the discussion of the
     * -zscores option of vcluster. The application is responsible for
     * allocating the memory for this array.
     * 
     * @return the external z-scores of each object.
     */
    public float[] getExtZScores() {
	return eZScores;
    }

    /**
     * Return the featureCount number of descriptive and discriminating features
     * for this clustering solution.
     * 
     * @param featureCount
     *            The number of features to identify for each cluster.
     * @return The descriptive and discriminating features for this clustering
     *         solution.
     * @see ClusterFeatures
     */
    public ClusterFeatures getClusterFeatures(int featureCount) {
	return clusterFeatures;
    }

    /**
     * Returns a 2 dimensional array that gives the child nodes for each node in
     * a hierarchical tree of the agglomerative clustering for this matrix. Each
     * tree node can have 2 child nodes, so getForwardTree()[nrows - 2][0] and
     * getForwardTree()[nrows - 2][1] contain the child node indices in the tree
     * of the root node. A value of -1 represents the lack of a child node. If
     * nrows is the number of matrix rows (getMatrix().getRowCount()), the array
     * has a length of 2 * nrows - 2, with the root node at index 2 * nrows - 2.
     * The first nrows of the array represents the original rows of the matrix
     * as leaf nodes of the tree.
     * 
     * @return A tree representation of the clustering of the matrix.
     * @see #getTreeCounts
     */
    public int[][] getForwardTree() {
	return ftree;
    }

    /**
     * Return an array the gives the number of child nodes for each node of the
     * tree returned by getForwardTree().
     * 
     * @return the number of child nodes for each node of the tree.
     * @see #getForwardTree
     */
    public int[] getTreeCounts() {
	return tsize;
    }

    /**
     * Return an index array that orders the rows of the matrix in the tree
     * order.
     * 
     * @return an index array that orders the rows of the matrix.
     */
    public int[] getRowTreeOrder() {
	return rowMap;
    }

    /**
     * Return an index array that orders the columns of the matrix in the tree
     * order.
     * 
     * @return an index array that orders the columns of the matrix.
     */
    public int[] getColTreeOrder() {
	return colMap;
    }

    /**
     * Return a matrix with the values normalized.
     * 
     * @return a matrix with the values normalized.
     */
    public ClutoInternalTableMatrix getInternalMatrix() {
	return internalMat;
    }

    /**
     * Return an array of length cluster number squared that gives the distance
     * between each pair of clusters. The distance between cluster i and cluster
     * j of n clusters is at array index n * i + j.
     * 
     * @return the distance between each pair of clusters.
     */
    public float[] getClusterDistances() {
	return distMatrix;
    }

}
