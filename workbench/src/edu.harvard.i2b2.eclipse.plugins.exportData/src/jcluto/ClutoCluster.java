/*
 * @(#) $RCSfile: ClutoCluster.java,v $ $Revision: 1.3 $ $Date: 2008/11/18 21:42:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.Arrays;

/**
 * ClutoCluster generates a <b>CLUTO</b> clustering solution for a given matrix
 * and set of clustering parameters.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/18 21:42:22 $ $Name
 */
public class ClutoCluster extends AbstractClutoSolution {
    float clusterTime = Float.NaN;

    /**
     * Generate a clustering for the given matrix.
     * 
     * @param cMat
     *            the matrix to be clustered.
     * @param cParam
     *            the parameters that defien the clustering method.
     */
    public ClutoCluster(ClutoMatrix cMat, ClutoParams cParam) {
	this.cMat = cMat;
	this.cParam = cParam;
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
    @Override
    public ClusterFeatures getClusterFeatures(int featureCount) {
	return new ClusterFeatureImpl(this, featureCount);
    }

    /**
     * Return an index array that orders the columns of the matrix in the tree
     * order.
     * 
     * @return an index array that orders the columns of the matrix.
     */
    @Override
    public int[] getColTreeOrder() {
	if (colMap == null) {
	    try {
		clusterColumns(cMat, cParam, ptree, part);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	return colMap;
    }

    /**
     * Return a matrix with the values normalized.
     * 
     * @return a matrix with the values normalized.
     */
    @Override
    public ClutoInternalTableMatrix getInternalMatrix() {
	if (internalMat == null) {
	    internalMat = getInternalMatrix(getMatrix(), getParams(),
		    getParts());
	}
	return internalMat;
    }

    /**
     * Return an array of length cluster number squared that gives the distance
     * between each pair of clusters. The distance between cluster i and cluster
     * j of n clusters is at array index n * i + j.
     * 
     * @return the distance between each pair of clusters.
     */
    @Override
    public float[] getClusterDistances() {
	if (distMatrix == null) {
	    float[] distMatrix = new float[cParam.getNumClusters()
		    * cParam.getNumClusters()];
	    JClutoWrapper.V_GetClusterDistanceMatrix(cMat.getRowCount(), cMat
		    .getColumnCount(), cMat.getRowPtr(), cMat.getRowInd(), cMat
		    .getRowVal(), cParam.getSimFunc(),
		    // cParam.getRowModel(), cParam.getColModel(),
		    // cParam.getColPrune(),
		    1, 1, 1, cParam.getDbgLvl(), cParam.getNumClusters(), part,
		    distMatrix);
	}
	return distMatrix;
    }

    /**
     * Return the time in seconds to compute the clustering solution.
     * 
     * @return the time in seconds to compute the clustering solution.
     */
    public float getClusterTime() {
	return clusterTime;
    }

    /**
     * Execute the clustering method to generate a cluster solution.
     */
    public void execute() {
	try {
	    int nrows = cMat.getRowCount();
	    int nclusters = cMat.getRowCount() > cParam.getNumClusters() ? cParam
		    .getNumClusters()
		    : cMat.getRowCount();
	    float crvalue = 0f;
	    part = new int[nrows];
	    ptree = new int[nrows * 2];
	    tsims = new float[nrows * 2];
	    gains = new float[nrows * 2];
	    long t0 = 0, t1 = 0, t2 = 0;

	    switch (cParam.getClusterMethod()) {
	    // Agglomerative Clustering
	    case ClutoParams.VA_Cluster:
		try {
		    t0 = System.currentTimeMillis();
		    JClutoWrapper.VA_Cluster(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(), cMat.getRowVal(),
			    cParam.getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
			    cParam.getColPrune(), cParam.getDbgLvl(), cParam
				    .getNumClusters(), part, ptree, tsims,
			    gains);
		    t1 = System.currentTimeMillis();
		    crvalue = JClutoWrapper
			    .V_GetSolutionQuality(cMat.getRowCount(), cMat
				    .getColumnCount(), cMat.getRowPtr(), cMat
				    .getRowInd(), cMat.getRowVal(), cParam
				    .getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
				    cParam.getColPrune(), cParam
					    .getNumClusters(), part);
		} catch (Throwable t) {
		}
		break;

	    // Biased Agglomerative Clustering
	    case ClutoParams.VA_ClusterBiased:
		try {
		    t0 = System.currentTimeMillis();
		    JClutoWrapper.VA_ClusterBiased(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(), cMat.getRowVal(),
			    cParam.getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
			    cParam.getColPrune(), cParam.getDbgLvl(),
			    (int) Math.sqrt(nrows) / 2,
			    cParam.getNumClusters(), part, ptree, tsims, gains);
		    t1 = System.currentTimeMillis();
		    crvalue = JClutoWrapper
			    .V_GetSolutionQuality(cMat.getRowCount(), cMat
				    .getColumnCount(), cMat.getRowPtr(), cMat
				    .getRowInd(), cMat.getRowVal(), cParam
				    .getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
				    cParam.getColPrune(), cParam
					    .getNumClusters(), part);
		} catch (Throwable t) {
		}
		break;

	    // Direct k-way Clustering
	    case ClutoParams.VP_ClusterDirect:
		try {
		    int nparts = cParam.getAggloFrom() > cParam
			    .getNumClusters() ? cParam.getAggloFrom() : cParam
			    .getNumClusters();
		    if (nparts > cMat.getRowCount())
			nparts = cMat.getRowCount();
		    t0 = System.currentTimeMillis();
		    JClutoWrapper.VP_ClusterDirect(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(), cMat.getRowVal(),
			    cParam.getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
			    cParam.getColPrune(), cParam.getNumTrials(), cParam
				    .getNumIter(), cParam.getSeed(), cParam
				    .getDbgLvl(), nparts, part);
		    t1 = System.currentTimeMillis();
		    // System.err.println(" " + part.length);
		    if (cParam.getAggloFrom() > cParam.getNumClusters()) {
			agglomerateClusters(cMat, cParam, part);
		    }

		    JClutoWrapper.V_BuildTree(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(),
			    cMat.getRowVal(),
			    cParam.getSimFunc(),
			    cParam.getAggloCrFunc(), // ctrl.agglocrfun
			    cParam.getRowModel(), cParam.getColModel(), cParam
				    .getColPrune(),
			    JClutoWrapper.CLUTO_TREE_FULL, cParam.getDbgLvl(),
			    cParam.getNumClusters(), part, ptree, tsims, gains);

		    crvalue = JClutoWrapper
			    .V_GetSolutionQuality(cMat.getRowCount(), cMat
				    .getColumnCount(), cMat.getRowPtr(), cMat
				    .getRowInd(), cMat.getRowVal(), cParam
				    .getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
				    cParam.getColPrune(), cParam
					    .getNumClusters(), part);

		} catch (Throwable t) {
		}
		break;

	    // Repeated Bisection Clustering
	    case ClutoParams.VP_ClusterRB:
		try {
		    int nparts = cParam.getAggloFrom() > cParam
			    .getNumClusters() ? cParam.getAggloFrom() : cParam
			    .getNumClusters();
		    if (nparts > cMat.getRowCount())
			nparts = cMat.getRowCount();
		    t0 = System.currentTimeMillis();
		    JClutoWrapper.VP_ClusterRB(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(), cMat.getRowVal(),
			    cParam.getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
			    cParam.getColPrune(), cParam.getNumTrials(), cParam
				    .getNumIter(), cParam.getSeed(), cParam
				    .getCsType(), cParam.getKwayRefine(),
			    cParam.getDbgLvl(), nparts, part);
		    t1 = System.currentTimeMillis();

		    if (cParam.getAggloFrom() > cParam.getNumClusters()) {
			agglomerateClusters(cMat, cParam, part);
		    }

		    JClutoWrapper.V_BuildTree(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(),
			    cMat.getRowVal(),
			    cParam.getSimFunc(),
			    cParam.getAggloCrFunc(), // ctrl.agglocrfun
			    cParam.getRowModel(), cParam.getColModel(), cParam
				    .getColPrune(),
			    JClutoWrapper.CLUTO_TREE_FULL, cParam.getDbgLvl(),
			    cParam.getNumClusters(), part, ptree, tsims, gains);

		    crvalue = JClutoWrapper
			    .V_GetSolutionQuality(cMat.getRowCount(), cMat
				    .getColumnCount(), cMat.getRowPtr(), cMat
				    .getRowInd(), cMat.getRowVal(), cParam
				    .getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
				    cParam.getColPrune(), cParam
					    .getNumClusters(), part);

		} catch (Throwable t) {
		}
		break;

	    // Repeated bisecting hierarchical
	    case ClutoParams.VP_ClusterRBTree:
		try {
		    t0 = System.currentTimeMillis();
		    JClutoWrapper.VP_ClusterRBTree(cMat.getRowCount(), cMat
			    .getColumnCount(), cMat.getRowPtr(), cMat
			    .getRowInd(), cMat.getRowVal(),
			    cParam.getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
			    cParam.getColPrune(), cParam.getNumTrials(), cParam
				    .getNumIter(), cParam.getSeed(), cParam
				    .getDbgLvl(), cParam.getNumClusters(),
			    part, ptree);
		    t1 = System.currentTimeMillis();

		    crvalue = JClutoWrapper
			    .V_GetSolutionQuality(cMat.getRowCount(), cMat
				    .getColumnCount(), cMat.getRowPtr(), cMat
				    .getRowInd(), cMat.getRowVal(), cParam
				    .getSimFunc(), cParam.getCrFunc(), cParam
				    .getRowModel(), cParam.getColModel(),
				    cParam.getColPrune(), cParam
					    .getNumClusters(), part);
		} catch (Throwable t) {
		}
		break;
	    default:
		// Complain
		break;
	    }
	    clusterTime = (t1 - t0) * .001f;
	    solutionQuality = crvalue;

	    ftree = constructForwardTree(nrows, ptree);
	    JClutoWrapper.V_TreeReorder(cMat.getRowCount(), cMat
		    .getColumnCount(), cMat.getRowPtr(), cMat.getRowInd(), cMat
		    .getRowVal(), cParam.getSimFunc(), cParam.getRowModel(),
		    cParam.getColModel(), cParam.getColPrune(), cParam
			    .getDbgLvl(), ptree, ftree);
	    rowMap = getMapOrder(ftree, new int[nrows]);
	    int nnrows = cMat.getRowCount() * 2;
	    tsize = new int[nnrows];
	    Arrays.fill(tsize, 1);
	    for (int i = 0; i < nnrows - 2; i++) {
		tsize[ptree[i]] += tsize[i];
	    }
	    evaluateClusters(cMat, cParam, part, ptree, crvalue);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Create an agglomerate clustering, the part may be changed.
     * 
     * @param cMat
     *            The data matrix.
     * @param cParam
     *            The clustering parameters
     * @param part
     *            The index the specifies the part cluster each row is assigned
     *            to.
     */
    private void agglomerateClusters(ClutoMatrix cMat, ClutoParams cParam,
	    int[] part) {
	int agglofrom = cParam.getAggloFrom();
	int[] ppart = new int[agglofrom];
	int[] ptree = new int[agglofrom * 2];
	float[] tsims = new float[agglofrom * 2];
	float[] gains = new float[agglofrom * 2];

	// Get the tree of the clustering solution
	JClutoWrapper.V_BuildTree(cMat.getRowCount(), cMat.getColumnCount(),
		cMat.getRowPtr(), cMat.getRowInd(), cMat.getRowVal(), cParam
			.getSimFunc(), cParam.getAggloCrFunc(), //ctrl.agglocrfun
		cParam.getRowModel(), cParam.getColModel(), cParam
			.getColPrune(), JClutoWrapper.CLUTO_TREE_TOP, cParam
			.getDbgLvl(), agglofrom, // agglofrom
		part, ptree, tsims, gains);

	for (int i = 0; i < agglofrom; i++) {
	    ppart[i] = i;
	}
	induceClusteringFromHAC(agglofrom, cParam.getNumClusters(), ptree,
		ppart);
	for (int i = 0; i < cMat.getRowCount(); i++) {
	    if (part[i] != -1) {
		part[i] = ppart[part[i]];
	    }
	}
	JClutoWrapper.V_ReorderPartitions(cMat.getRowCount(), cMat
		.getColumnCount(), cMat.getRowPtr(), cMat.getRowInd(), cMat
		.getRowVal(), cParam.getSimFunc(), cParam.getRowModel(), cParam
		.getColModel(), cParam.getColPrune(), cParam.getNumClusters(), // agglofrom
		part);
    }

    /**
     * This function induces a k-way clustering of a collection of objects based
     * on the hac-tree.
     */
    public void induceClusteringFromHAC(int nrows, int nparts, int[] ptree,
	    int[] part) {
	int[] queue = new int[nparts];
	// Construct the forward tree
	int[][] ftree = constructForwardTree(nrows, ptree);
	// Go and find the root nodes of the nparts clusters
	queue[0] = 2 * nrows - 2;
	int qlen = 1;
	for (int i = 0, j = 0, k = 0; i < nparts - 1; i++) {
	    for (k = 0, j = 1; j < qlen; j++) {
		if (queue[j] > queue[k]) {
		    k = j;
		}
	    }
	    j = queue[k];
	    queue[k] = queue[--qlen];
	    queue[qlen++] = ftree[j][0];
	    queue[qlen++] = ftree[j][1];
	}
	//  
	// ASSERT(qlen == nparts);
	//  
	// Go and label the rows under each subtree with the appropriate partnum
	for (int i = 0; i < qlen; i++) {
	    labelSubTree(queue[i], ftree, part, i);
	}
    }

    /**
     * This is a recursive routine for labeling the nodes under the subtree
     */
    public void labelSubTree(int root, int[][] ftree, int[] part, int cid) {
	if (ftree[root][0] != -1) {
	    labelSubTree(ftree[root][0], ftree, part, cid);
	    labelSubTree(ftree[root][1], ftree, part, cid);
	} else {
	    part[root] = cid;
	}
    }

    /**
     * Construct the forward tree from the given ptree. Returns a 2 dimensional
     * array that gives the child nodes for each node in a hierarchical tree of
     * the agglomerative clustering for this matrix. Each tree node can have 2
     * child nodes, so [nrows - 2][0] and [nrows - 2][1] contain the child node
     * indices in the tree of the root node. A value of -1 represents the lack
     * of a child node. If nrows is the number of matrix rows, the array has a
     * length of 2 * nrows, with the root node at index 2 * nrows - 2. The first
     * nrows of the array represents the original rows of the matrix as leaf
     * nodes of the tree.
     * 
     * @param nnrows
     *            The number of rows from which the ptree was generated.
     * @param ptree
     *            An array of size 2*nrows that upon successful completion
     *            stores the parent array of the binary hierarchical tree. In
     *            this tree, each node corresponds to a cluster. The leaf nodes
     *            are the original nrows objects, and they are numbered from 0
     *            to nrows-1. The internal nodes of the tree are numbered from
     *            nrows to 2*nrows-2. The numbering of the internal nodes is
     *            performed so that smaller numbers correspond to clusters
     *            obtained by merging a pair of clusters earlier during the
     *            agglomeration process. The root of the tree is numbered
     *            2*nrows-2. The ith entry of the ptree array stores the parent
     *            node of the i node of the tree. The ptree entry for the root
     *            is set to -1.
     * @return The forward tree.
     */
    public static int[][] constructForwardTree(int nnrows, int[] ptree) {
	int[][] ftree = new int[2 * nnrows][2];
	for (int i = 0; i < ftree.length; i++) {
	    ftree[i] = new int[2];
	    ftree[i][0] = -1;
	    ftree[i][1] = -1;
	}
	for (int i = 0; i < ftree.length - 2; i++) {
	    if (ftree[ptree[i]][0] == -1)
		ftree[ptree[i]][0] = i;
	    else
		ftree[ptree[i]][1] = i;
	}
	return ftree;
    }

    /**
     * This function calculates the class distribution statistics for a
     * clustering solution.
     */
    public void evaluateClusters(ClutoMatrix cMat, ClutoParams cParam,
	    int[] part, int[] ptree, float crvalue) {
	int nparts = cParam.getNumClusters();
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();

	// For now assume each row is a distinct class
	int nclasses = nrows;
	int[] cclass = new int[nclasses];
	for (int i = 0; i < cclass.length; i++) {
	    cclass[i] = 1;
	}

	int[] pwgts = new int[nparts];
	float[] cintsim = new float[nparts];
	float[] cintsdev = new float[nparts];
	float[] izscores = new float[nrows];
	float[] cextsim = new float[nparts];
	float[] cextsdev = new float[nparts];
	float[] ezscores = new float[nrows];

	int[][] classdist = new int[nparts][nclasses];
	for (int i = 0; i < classdist.length; i++) {
	    classdist[i] = new int[nclasses];
	}
	int[] ocldist = new int[nclasses];
	float totalentropy = 0f;
	float totalpurity = 0f;
	float totalfscore = 0f;
	float[] entropies = new float[nparts];
	float[] purities = new float[nparts];
	float[] fscores = new float[nclasses];

	boolean showfscores = false;
	// showfscores = (ctrl->clmethod == CLMETHOD_AGGLO || ctrl->clmethod ==
	// CLMETHOD_RBTREE || ctrl->clmethod == CLMETHOD_BAGGLO ||
	// ctrl->fulltree);
	// showfscores = 0;
	// Get the information about the clusters
	JClutoWrapper.V_GetClusterStats(cMat.getRowCount(), cMat
		.getColumnCount(), cMat.getRowPtr(), cMat.getRowInd(), cMat
		.getRowVal(), cParam.getSimFunc(), cParam.getRowModel(), cParam
		.getColModel(), cParam.getColPrune(),
		cParam.getNumClusters(), // agglofrom
		part, pwgts, cintsim, cintsdev, izscores, cextsim, cextsdev,
		ezscores);

	pWgts = pwgts;
	cIntSim = cintsim;
	cIntSdev = cintsdev;
	cExtSim = cextsim;
	cExtSdev = cextsdev;

	int ncrows = 0;
	// Compute the class distribution information
	for (int i = 0, k = 0; i < nrows; i++) {
	    if ((k = part[i]) == -1)
		continue;
	    ncrows++;
	    classdist[k][cclass[i]]++;
	    ocldist[cclass[i]]++;
	}
	// Compute the entropies
	for (int i = 0; i < nparts; i++) {
	    entropies[i] = computeEntropy(nclasses, ncrows, ocldist, pwgts[i],
		    classdist[i]);
	    totalentropy += (entropies[i] * pwgts[i]) / ncrows;
	}
	totalEntropy = totalentropy;

	// Compute the purities
	for (int i = 0; i < nparts; i++) {
	    purities[i] = computePurity(nclasses, ncrows, ocldist, pwgts[i],
		    classdist[i]);
	    totalpurity += (purities[i] * pwgts[i]) / ncrows;
	}
	totalPurity = totalpurity;

	// Compute the fscores
	if (showfscores && nclasses > 1) {
	    computeFScores(nclasses, nrows, cclass, ocldist, ptree, fscores);
	    for (int i = 0; i < nclasses; i++)
		totalfscore += (fscores[i] * ocldist[i]) / ncrows;
	    fScores = fscores;
	}
    }

    /**
     * This function computes the relative entropy of a cluster
     */
    public float computeEntropy(int n, int tsize, int[] tdist, int size,
	    int[] dist) {
	double entropy = 0.0;

	for (int i = 0; i < n; i++)
	    entropy += (dist[i] > 0 ? -(1.0 * dist[i] / size)
		    * Math.log(1.0 * dist[i] / size) / Math.log(2.0) : 0.0);

	return (float) (entropy / (Math.log(n) / Math.log(2.0)));
    }

    /**
     * This function computes the relative entropy of a cluster
     */
    public float computePurity(int n, int tsize, int[] tdist, int size,
	    int[] dist) {
	int max = 0;
	// find the index of the max value in the array
	for (int i = 1; i < dist.length; i++) {
	    max = (dist[i] > dist[max] ? i : max);
	}
	return (float) 1.0 * dist[max] / size;
    }

    /**
     * This function computes the f-scores for a particular tree
     */
    private void computeFScores(int nclasses, int nrows, int[] cclass,
	    int[] ocldist, int[] ptree, float[] fscores) {
	int[][] tclassdist;

	tclassdist = new int[2 * nrows][nclasses];
	for (int i = 0; i < tclassdist.length; i++) {
	    tclassdist[i] = new int[nclasses];
	}

	/* build the class distribution arrays for each node of the tree */
	for (int i = 0; i < nrows; i++) {
	    if (ptree[i] != -1)
		tclassdist[i][cclass[i]] = 1;
	}
	for (int i = 0; i < 2 * nrows - 2; i++) {
	    if (ptree[i] != -1) {
		for (int j = 0; j < nclasses; j++)
		    tclassdist[ptree[i]][j] += tclassdist[i][j];
	    }
	}

	// Visit each tree node and compute the various f-scores and the overall
	// f-score
	for (int i = 0; i < 2 * nrows - 1; i++) {
	    int k = 0;
	    for (int l = 0; l < tclassdist[i].length; l++) {
		k += tclassdist[i][l];
	    }
	    if (k > 0) {
		for (int j = 0; j < nclasses; j++) {
		    float rec = (tclassdist[i][j] == 0 ? 0f : 1f
			    * tclassdist[i][j] / ocldist[j]);
		    float pre = (tclassdist[i][j] == 0 ? 0f : 1f
			    * tclassdist[i][j] / k);
		    float f1 = (tclassdist[i][j] == 0 ? 0f : 2f * pre * rec
			    / (pre + rec));
		    fscores[j] = Math.max(fscores[j], f1);
		}
	    }
	}

    }

    /**
     * Return a matrix with the values normalized.
     * 
     * @param cMat
     *            the source matrix
     * @param cParam
     *            the clustering params
     * @param part
     *            the cluster indices
     * @return a matrix with the values normalized.
     */
    private ClutoInternalTableMatrix getInternalMatrix(ClutoMatrix cMat,
	    ClutoParams cParam, int[] part) {
	int[] r_nrows = new int[1];
	int[] r_ncols = new int[1];
	int[][] r_rowptr = new int[1][];
	int[][] r_rowind = new int[1][];
	float[][] r_rowval = new float[1][];
	int[][] r_rimap = new int[1][];
	int[][] r_cimap = new int[1][];

	JClutoWrapper.InternalizeMatrix(cMat.getRowCount(), cMat
		.getColumnCount(), cMat.getRowPtr(), cMat.getRowInd(), cMat
		.getRowVal(), cParam.getSimFunc(), cParam.getRowModel(), cParam
		.getColModel(), cParam.getColPrune(), part, r_nrows, r_ncols,
		r_rowptr, r_rowind, r_rowval, r_rimap, r_cimap);

	// System.err.println("getInternalMatrix " +
	// "\tr_nrows " + r_nrows[0] + "\tr_ncols " + r_ncols[0] +
	// "\tr_rowptr: " + r_rowptr[0].length +
	// "\tr_rowind: " + r_rowind[0].length +
	// "\tr_rowval: " + r_rowval[0].length);

	return new ClutoIntTableMatrix(cMat, r_nrows[0], r_ncols[0],
		r_rowptr[0], r_rowind[0], r_rowval[0], r_rimap[0], r_cimap[0]);
    }

    /**
     * Cluster Columns
     */
    private void clusterColumns(ClutoMatrix cMat, ClutoParams cParam,
	    int[] ptree, int[] part) {
	ClutoInternalMatrix iMat = getInternalMatrix();
	int nrows = iMat.getRowCount();
	int ncols = iMat.getColumnCount();
	int[] rowptr = iMat.getRowPtr();
	int[] rowind = iMat.getRowInd();
	float[] rowval = iMat.getRowVal();
	// System.err.println("iMat rowptr: " + rowptr.length + "\trowind: " +
	// rowind.length + "\trowval: " + rowval.length);
	int[] colptr = new int[ncols + 1];
	int[] colind = new int[rowptr[nrows]];
	float[] colval = new float[rowptr[nrows]];
	;
	int[] cpart = new int[ncols];
	int[] cptree = new int[2 * ncols];
	float[] ctsims = new float[2 * ncols];
	float[] cgains = new float[2 * ncols];
	Arrays.fill(cpart, -1);
	Arrays.fill(cptree, -1);
	for (int i = 0; i < nrows; i++) {
	    for (int j = rowptr[i]; j < rowptr[i + 1]; j++)
		colptr[rowind[j]]++;
	}
	for (int i = 1; i < ncols; i++)
	    colptr[i] += colptr[i - 1];
	for (int i = ncols; i > 0; i--)
	    colptr[i] = colptr[i - 1];
	colptr[0] = 0;
	for (int i = 0; i < nrows; i++) {
	    for (int j = rowptr[i]; j < rowptr[i + 1]; j++) {
		int k = rowind[j];
		colind[colptr[k]] = i;
		colval[colptr[k]] = rowval[j];
		colptr[k]++;
	    }
	}
	for (int i = ncols; i > 0; i--)
	    colptr[i] = colptr[i - 1];
	colptr[0] = 0;
	JClutoWrapper.VA_Cluster(ncols, nrows, colptr, colind, colval,
		JClutoWrapper.CLUTO_SIM_COSINE, cParam.getCrFunc(),
		JClutoWrapper.CLUTO_ROWMODEL_NONE,
		JClutoWrapper.CLUTO_COLMODEL_NONE, 1.0f, 0, 1, cpart, cptree,
		ctsims, cgains);
	int[][] ftree = constructForwardTree(ncols, cptree);
	if (ncols < 5000) {
	    JClutoWrapper.V_TreeReorder(ncols, nrows, colptr, colind, colval,
		    JClutoWrapper.CLUTO_SIM_COSINE,
		    JClutoWrapper.CLUTO_ROWMODEL_NONE,
		    JClutoWrapper.CLUTO_COLMODEL_NONE, 1.0f, 0, cptree, ftree);
	}
	/* Compute the size of each subtree */
	float[] tsize = new float[2 * ncols];
	Arrays.fill(tsize, 0, ncols, 1f);
	Arrays.fill(tsize, ncols, tsize.length, 0f);
	for (int i = 0; i < 2 * ncols - 2; i++) {
	    tsize[cptree[i]] += tsize[i];
	}
	// Create the sorted column index based on depth first search
	colMap = getMapOrder(ftree, new int[ncols]);
    }

    /**
     * Create an index that sorts the row order according to the leaf nodes in
     * the forward tree.
     * 
     * @param ftree
     *            the forward tree
     * @param map
     *            the array for the row indices
     * @return the array of the row indices
     */
    public int[] getMapOrder(int[][] ftree, int[] map) {
	if (map == null) {
	    map = new int[ftree.length / 2];
	}
	int leafnodes = getMapOrder(ftree.length - 2, 0, ftree, map);
	return map;
    }

    /**
     * Recursively traverses the forward tree to create an sorting row index.
     * 
     * @param tn
     *            the current tree node index
     * @param ln
     *            the leaf node index into the map array.
     * @param ftree
     *            the forward tree
     * @param map
     *            the array for the row indices
     * @return the next leaf node index into the map array.
     */
    private int getMapOrder(int tn, int nleafs, int[][] ftree, int[] map) {
	int ln = nleafs;
	if (ftree[tn][0] < 0) {
	    map[ln++] = tn;
	} else {
	    ln = getMapOrder(ftree[tn][0], ln, ftree, map);
	    ln = getMapOrder(ftree[tn][1], ln, ftree, map);
	}
	return ln;
    }

}
