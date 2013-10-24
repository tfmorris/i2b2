/*
 * @(#) $RCSfile: ClutoParams.java,v $ $Revision: 1.3 $ $Date: 2008/11/21 17:18:38 $ $Name: RELEASE_1_3_1_0001b $
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
 * ClutoParams supplies all the required parameters for executing a <b>CLUTO</b>
 * clustering method.
 * 
 * NOTE: Only the vector based clustering methods are currently supported,
 * support for <b>CLUTO</b>'s graph based clustering methods still needs to be
 * added.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/21 17:18:38 $ $Name: RELEASE_1_3_1_0001b $
 */
public interface ClutoParams {
    /**
     * In this method, the desired k-way clustering solution is computed by
     * simultaneously finding all <code>k</code> clusters. In general, computing
     * a k-way clustering directly is slower than clustering via repeated
     * bisections. In terms of quality, for reasonably small values of
     * <code>k</code> (usually less than 10 - 20), the direct approach leads to
     * better clusters than those obtained via repeated bisections. However, as
     * <code>k</code> increases, the repeated-bisecting approach tends to be
     * better than direct clustering.
     */
    public final static int VP_ClusterDirect = 0;

    /**
     * In this method, the desired k-way clustering solution is computed by
     * performing a sequence of k-1 repeated bisections. In this approach, the
     * matrix is first clustered into two groups, then one of these groups is
     * selected and bisected further. This process continuous until the desired
     * number of clusters is found. During each step, the cluster is bisected so
     * that the resulting 2-way clustering solution optimizes a particular
     * clustering criterion function: <code>getCrFunc()</code>. <br>
     * NOTE that this approach ensures that the criterion function is locally
     * optimized within each bisection, but in general is not globally
     * optimized. The cluster that is selected for further partitioning is
     * controlled by the parameter: <code>getCsType()</code>. <br>
     * VP_ClusterRB is considerably faster than VP_ClusterDirect and it should
     * be preferred if the number of desired clusters is quite large (e.g.,
     * greater than 20 30).
     */
    public final static int VP_ClusterRB = 1;

    /**
     * In this method, the desired k-way clustering solution is computed using
     * the agglomerative paradigm whose goal is to locally optimize (minimize or
     * maximize) a particular clustering criterion function,
     * <code>getCrFunc()</code>. The solution is obtained by stopping the
     * agglomeration process when k clusters are left. <br>
     * Note: Due to the high computational requirements of VA_Cluster, it should
     * only be used to cluster matrices that have fewer than 3,000 6,000 rows.
     */
    public final static int VA_Cluster = 2;

    /**
     * In this method, the desired k-way clustering solution is computed in a
     * fashion similar to the <code>VA_Cluster</code> method; however, the
     * agglomeration process is biased by a partitional clustering solution that
     * is initially computed on the dataset. <b>CLUTO</b> first computes a n-way
     * clustering solution using the VP_ClusterRB method, where n is the number
     * of objects to be clustered. Then, it augments the original feature space
     * by adding n new dimensions, one for each cluster. Each object is then
     * assigned a value to the dimension corresponding to its own cluster, and
     * this value is proportional to the similarity between that object and its
     * cluster-centroid. Now, given this augmented representation, the overall
     * clustering solution is obtained by using the traditional agglomerative
     * paradigm and the clustering criterion function. The solution is obtained
     * by stopping the agglomeration process when k clusters are left.
     * Experiments on document datasets have shown that this biased
     * agglomerative approach outperforms the traditional agglomerative
     * algorithms. <br>
     * Note: Due to the high computational requirements of VA_ClusterBiased, it
     * should only be used to cluster matrices that have fewer than 3,000 6,000
     * rows.
     */
    public final static int VA_ClusterBiased = 3;

    /**
     * In this method the desired k-way clustering solution is computed in a
     * fashion similar to the repeated-bisecting method but at the end, the
     * overall solution is globally optimized. Essentially, an initial
     * clustering solution is obtained using <code>VP_ClusterRB</code> method,
     * then attempts to further optimize the clustering criterion function.
     */
    public final static int VP_ClusterRBTree = 4;

    /**
     * Set the parameter option parameters to be the same as the given params.
     * 
     * @param params
     *            The <b>CLUTO</b> parameters to duplicate.
     */
    public void setValues(ClutoParams params);

    /**
     * Return the selected <b>CLUTO</b> clustering method.
     * 
     * @return the selected <b>CLUTO</b> clustering method.
     * @see ClutoParams#VP_ClusterDirect
     * @see ClutoParams#VP_ClusterRB
     * @see ClutoParams#VA_Cluster
     * @see ClutoParams#VA_ClusterBiased
     * @see ClutoParams#VP_ClusterRBTree
     */
    public int getClusterMethod();

    /**
     * Return the selected <b>CLUTO</b> clustering similarity function. The
     * runtime may increase for <code>CLUTO_SIM_CORRCOEF</code>, as it needs to
     * store and operate on the dense <code>n × m</code> matrix.
     * 
     * @return the selected <b>CLUTO</b> clustering similarity function.
     * @see JClutoWrapper#CLUTO_SIM_COSINE
     * @see JClutoWrapper#CLUTO_SIM_CORRCOEF
     * @see JClutoWrapper#CLUTO_SIM_EDISTANCE
     * @see JClutoWrapper#CLUTO_SIM_EJACCARD
     */
    public int getSimFunc();

    /**
     * Return the selected <b>CLUTO</b> clustering criterion function.
     * <code>CLUTO_CLFUN_I2</code> is the default criterion function for
     * <code>VP_ClusterRB</code> and <code>VP_ClusterDirect</code>
     * <code>CLUTO_CLFUN_UPGMA</code> is the default criterion function for
     * <code>VA_Cluster</code> and <code>VA_ClusterBiased</code>
     * 
     * @return the selected <b>CLUTO</b> clustering criterion function.
     * @see JClutoWrapper#CLUTO_CLFUN_I1
     * @see JClutoWrapper#CLUTO_CLFUN_I2
     * @see JClutoWrapper#CLUTO_CLFUN_E1
     * @see JClutoWrapper#CLUTO_CLFUN_G1
     * @see JClutoWrapper#CLUTO_CLFUN_G1P
     * @see JClutoWrapper#CLUTO_CLFUN_H1
     * @see JClutoWrapper#CLUTO_CLFUN_H2
     * @see JClutoWrapper#CLUTO_CLFUN_SLINK
     * @see JClutoWrapper#CLUTO_CLFUN_SLINK_W
     * @see JClutoWrapper#CLUTO_CLFUN_CLINK
     * @see JClutoWrapper#CLUTO_CLFUN_CLINK_W
     * @see JClutoWrapper#CLUTO_CLFUN_UPGMA
     * @see JClutoWrapper#CLUTO_CLFUN_UPGMA_W
     */
    public int getCrFunc();

    /**
     * Return the selected <b>CLUTO</b> cluster selection scheme for Repeated
     * Bisection methods.
     * 
     * @return the selected <b>CLUTO</b> cluster selection scheme for bisection.
     * @see JClutoWrapper#CLUTO_CSTYPE_LARGEFIRST
     * @see JClutoWrapper#CLUTO_CSTYPE_BESTFIRST
     * @see JClutoWrapper#CLUTO_CSTYPE_LARGESUBSPACEFIRST
     */
    public int getCsType();

    /**
     * Return the selected <b>CLUTO</b> row model transformation model to be
     * used to scale the various columns of each row.
     * <code>CLUTO_ROWMODEL_MAXTF</code>, <code>CLUTO_ROWMODEL_SQRT</code>, and
     * <code>CLUTO_ROWMODEL_LOG</code> are primarily used to smooth large values
     * in certain columns (i.e., dimensions) of each vector.
     * 
     * @return the selected <b>CLUTO</b> row model transformation.
     * @see JClutoWrapper#CLUTO_ROWMODEL_NONE
     * @see JClutoWrapper#CLUTO_ROWMODEL_MAXTF
     * @see JClutoWrapper#CLUTO_ROWMODEL_SQRT
     * @see JClutoWrapper#CLUTO_ROWMODEL_LOG
     * @see #getColModel
     */
    public int getRowModel();

    /**
     * Return the selected <b>CLUTO</b> column model transformation model to be
     * used to scale the various columns globally across all the rows. The
     * global scaling of the columns occurs after the per-row column scaling
     * selected by the <code>getRowModel()</code> parameter has been performed.
     * The choice of the options for both <code>getRowModel()</code> and
     * <code>getColModel()</code> were motivated by the clustering requirements
     * of high-dimensional datasets arising in document and commercial datasets.
     * However, for other domains the provided options may not be sufficient. In
     * such domains, the data should be pre-processed to apply the desired
     * row/column model before supplying them to CLUTO. In that case
     * <code>CLUTO_ROWMODEL_NONE</code> and <code>CLUTO_COLMODEL_NONE</code>
     * should probably be used.
     * 
     * @return the selected <b>CLUTO</b> column model transformation.
     * @see JClutoWrapper#CLUTO_COLMODEL_NONE
     * @see JClutoWrapper#CLUTO_COLMODEL_IDF
     * @see #getRowModel
     */
    public int getColModel();

    /**
     * Return the <b>CLUTO</b> column pruning <code>threshold</code>,
     * <code>( 0. < threshold <= 1. )</code> Threshold is a number between 0.0
     * and 1.0 and indicates the fraction of the overall similarity that the
     * retained columns must account for. For example, if <code>threshold</code>
     * = 0.9, the clustering method first determines how much each column
     * contributes to the overall pairwise similarity between the rows, and then
     * selects as many of the highest contributing columns as required to
     * account for 90% of the similarity. Reasonable values are within the range
     * of <code>(0.8 ...1.0)</code>, and the default value is <code>1.0</code>,
     * indicating that no columns will be pruned. In general, this parameter
     * leads to a substantial reduction of the number of columns (i.e.,
     * dimensions) without seriously affecting the overall clustering quality.
     * 
     * @return the column pruning threshold.
     */
    public float getColPrune();

    /**
     * Returns the number of different clustering solutions to be computed by
     * the various partitional algorithms. This number of clustering solutions
     * is computed (each one of them starting with a different set of seed
     * objects), and the solution that has the best value of the criterion
     * function will be reported the the clustering method.
     * 
     * @return the selected number of different clustering solutions to be
     *         computed
     */
    public int getNumTrials();

    /**
     * Return the maximum number of refinement iterations to be performed,
     * within each clustering step. Reasonable values for this parameter are
     * usually in the range of 5 - 20. This parameter applies only to the
     * partitional clustering algorithms.
     * 
     * @return the selected number of refinement iterations.
     */
    public int getNumIter();

    /**
     * Returns the seed for the random number generator.
     * 
     * @return the seed for the random number generator.
     */
    public int getSeed();

    /**
     * Return whether or not the clustering solution will be globally optimized
     * at the end by performing a series of k-way refinement iterations. The
     * global optimization of the clustering solution can significantly increase
     * the amount of time required to perform the clustering.
     * 
     * @see JClutoWrapper#CLUTO_GLOBAL_OPTIMIZIMATION_NONE
     * @see JClutoWrapper#CLUTO_GLOBAL_OPTIMIZIMATION_KWAYREFINE
     * @return the the whether globally optimizimation should be performed.
     */
    public int getKwayRefine();

    /**
     * Return the number of nearest neighbors of each object that will be used
     * in creating the nearest neighbor graph that is used by the
     * graph-partitioning based clustering algorithm.
     * 
     * @return the number of nearest neighbors for the nearest neighbor graph.
     */
    public int getNumNeighbors();

    /**
     * Return the minimum number of components required for a cluster. This
     * parameter is used to eliminate small connected components from the
     * nearest-neighbor graph prior to clustering. In general, if the edge- and
     * vertex-pruning options are used, the resulting graph may have a large
     * number of small connect components (in addition to larger ones). By
     * eliminating (i.e., not clustering) the smaller components eliminates some
     * of the clutter in the resulting clustering solution, and it removes some
     * additional outliers. The default value for this parameter is set to five.
     * NOTE that this parameter is used only by the graph-partitioning based
     * clustering algorithm.
     * 
     * @return the minimum number of components required for a cluster.
     */
    public int getMinCmp();

    /**
     * Return the desired number of clusters.
     * 
     * @return the desired number of clusters.
     */
    public int getNumClusters();

    /**
     * Return the number of initial clusters to compute using a partitional
     * clustering method. This is used when cluto compute a clustering by
     * combining both the partitional and agglomerative methods. In this
     * approach, the desired k-way clustering solution is computed by first
     * clustering the dataset into m clusters (m >k), and then the final k-way
     * clustering solution is obtained by merging some of these clusters using
     * an agglomerative algorithm. This approach was motivated by the two-phase
     * clustering approach of the CHAMELEON algorithm [2], and was designed to
     * allow the user to compute a clustering solution that uses a different
     * clustering criterion function for the partitioning phase from that used
     * for the agglomeration phase. An application of such an approach is to
     * allow the clustering algorithm to find non-globular clusters. In this
     * case, the partitional clustering solution can be computed using a
     * criterion function that favors globular clusters (e.g., CLUTO_CLFUN_I2),
     * and then combine these clusters using a single-link approach (e.g.,
     * CLUTO_CLFUN_SLINK_W ) to find non-globular but well-connected clusters.
     * 
     * @return the number of initial clusters to compute using a partitional
     *         clustering.
     */
    public int getAggloFrom();

    /**
     * Return the selected <b>CLUTO</b> Agglomerative clustering criterion
     * function. This parameter controls the criterion function that is used
     * during the agglomeration when using combined partitional and
     * agglomerative methods.
     * 
     * @return the selected <b>CLUTO</b> agglomerative clustering criterion
     *         function
     * @see JClutoWrapper#CLUTO_CLFUN_I1
     * @see JClutoWrapper#CLUTO_CLFUN_I2
     * @see JClutoWrapper#CLUTO_CLFUN_E1
     * @see JClutoWrapper#CLUTO_CLFUN_G1
     * @see JClutoWrapper#CLUTO_CLFUN_G1P
     * @see JClutoWrapper#CLUTO_CLFUN_H1
     * @see JClutoWrapper#CLUTO_CLFUN_H2
     * @see JClutoWrapper#CLUTO_CLFUN_SLINK
     * @see JClutoWrapper#CLUTO_CLFUN_SLINK_W
     * @see JClutoWrapper#CLUTO_CLFUN_CLINK
     * @see JClutoWrapper#CLUTO_CLFUN_CLINK_W
     * @see JClutoWrapper#CLUTO_CLFUN_UPGMA
     * @see JClutoWrapper#CLUTO_CLFUN_UPGMA_W
     */
    public int getAggloCrFunc();

    /**
     * Return the debug level
     * 
     * @return the debug level
     * @see JClutoWrapper#CLUTO_DBG_PROGRESS
     * @see JClutoWrapper#CLUTO_DBG_RPROGRESS
     * @see JClutoWrapper#CLUTO_DBG_APROGRESS
     * @see JClutoWrapper#CLUTO_DBG_CPROGRESS
     * @see JClutoWrapper#CLUTO_DBG_MPROGRESS
     * @see JClutoWrapper#CLUTO_DBG_CCMPSTAT
     */
    public int getDbgLvl();

    /**
     * Return whether agglomerative parameters should be displayed. This is
     * purely for the user interface.
     * 
     * @return whether agglomerative parameters should be displayed.
     */
    public int showAgglomerativeParameters();
}
