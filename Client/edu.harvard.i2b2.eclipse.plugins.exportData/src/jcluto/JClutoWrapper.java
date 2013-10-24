/*
 * @(#) $RCSfile: JClutoWrapper.java,v $ $Revision: 1.2 $ $Date: 2008/11/24 17:41:20 $ $Name: RELEASE_1_3_1_0001b $
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

import java.net.URL;

/**
 * Provides the java native interface to the <b>CLUTO</b> clustering library.
 * The fields and methods are primarily a direct translation from the cluto.h
 * include file of the cluto library. Most of the documentation is taken from
 * the manual provided with the <b>CLUTO</b> download, and which is referenced
 * below.
 * 
 * <p>
 * The complexity of CLUTOs clustering algorithms.<br>
 * The meaning of the various quantities are as follows:<br>
 * <ul>
 * <li><span style="font-weight: bold;">n</span> is the number of objects to be
 * clustered,</li>
 * <li><span style="font-weight: bold;">m</span> is the number of dimensions,</li>
 * <li><span style="font-weight: bold;">NNZ</span> is the number of non-zeros in
 * the input matrix or similarity matrix,</li>
 * <li><span style="font-weight: bold;">NNbrs</span> is the number of neighbors
 * in the nearest-neighbor graph.</li>
 * </ul>
 * <br>
 * <br>
 * <table cellpadding="2" cellspacing="2" border="1" * style="width: 100%; text-align: left;">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;">Clustering Algorithm</td>
 * <td style="vertical-align: top;">Method</td>
 * <td style="vertical-align: top;">Function<br>
 * </td>
 * <td style="vertical-align: top;">Time Complexity<br>
 * </td>
 * <td style="vertical-align: top;">Space Complexity<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">K-way repeated bisection</td>
 * <td style="vertical-align: top;">{@link #VP_ClusterRB}<br>
 * {@link #SP_ClusterRB}</td>
 * <td style="vertical-align: top;">{@link #CLUTO_SIM_COSINE}<br>
 * </td>
 * <td style="vertical-align: top;">O(NNZ*log(k))&nbsp;<br>
 * </td>
 * <td style="vertical-align: top;">O(NNZ)</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">K-way repeated bisection</td>
 * <td style="vertical-align: top;">{@link #VP_ClusterRB}<br>
 * {@link #SP_ClusterRB}</td>
 * <td style="vertical-align: top;">{@link #CLUTO_SIM_CORRCOEF}<br>
 * </td>
 * <td style="vertical-align: top;">O(n*m*log(k))&nbsp;<br>
 * </td>
 * <td style="vertical-align: top;">O(n*m)</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">K-means</td>
 * <td style="vertical-align: top;">{@link #VP_ClusterDirect}<br>
 * {@link #SP_ClusterDirect}</td>
 * <td style="vertical-align: top;">{@link #CLUTO_SIM_COSINE}<br>
 * </td>
 * <td style="vertical-align: top;">O(NNZ*k+m*k)&nbsp;<br>
 * </td>
 * <td style="vertical-align: top;">O(NNZ+m*k)</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">K-means</td>
 * <td style="vertical-align: top;">{@link #VP_ClusterDirect}<br>
 * {@link #SP_ClusterDirect}</td>
 * <td style="vertical-align: top;">{@link #CLUTO_SIM_CORRCOEF}<br>
 * </td>
 * <td style="vertical-align: top;">O(n*m*k)&nbsp;<br>
 * </td>
 * <td style="vertical-align: top;">O(n*m+m*k)</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">Hierarchical Agglomerative</td>
 * <td style="vertical-align: top;">{@link #VA_Cluster}<br>
 * {@link #SA_Cluster}</td>
 * <td style="vertical-align: top;">{@link #CLUTO_SIM_COSINE}</td>
 * <td style="vertical-align: top;">O(n2*log(n))&nbsp;<br>
 * </td>
 * <td style="vertical-align: top;">O(n2)</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">Hierarchical Agglomerative</td>
 * <td style="vertical-align: top;">{@link #VA_Cluster}<br>
 * {@link #SA_Cluster}</td>
 * <td style="vertical-align: top;">{@link #CLUTO_CLFUN_I1},
 * {@link #CLUTO_CLFUN_I2}<br>
 * </td>
 * <td style="vertical-align: top;">O(n3)<br>
 * </td>
 * <td style="vertical-align: top;">O(n2)<br>
 * </td>
 * </tr>
 * 
 * </tbody>
 * </table>
 * <br>
 * 
 * <p>
 * References:
 * <ul>
 * <li><a href="http://www-users.cs.umn.edu/~karypis/cluto/files/manual.pdf">
 * http://www-users.cs.umn.edu/~karypis/cluto/files/manual.pdf </a>
 * <li><a href="http://www-users.cs.umn.edu/~karypis/cluto/">
 * http://www-users.cs.umn.edu/~karypis/cluto/ </a>
 * <li><a
 * href="http://www-users.cs.umn.edu/~karypis/publications/vscluster.pdf">
 * http://www-users.cs.umn.edu/~karypis/publications/vscluster.pdf </a>
 * </ul>
 * 
 */
public class JClutoWrapper {
    private static boolean clutoLibLoaded = false;
    static {
	String libName = "JCluto";
	try {
	    // System.loadLibrary(libName);
	    clutoLibLoaded = true;
	} catch (Throwable ex) {
	    System.err.println(libName + "\t" + ex);
	    System.err.println("java.library.path :  "
		    + System.getProperty("java.library.path"));
	    try {
		String fileName = System.mapLibraryName(libName);
		System.err.println("libName fileName " + fileName);
		URL libURL = JClutoWrapper.class.getResource(fileName);
		System.err.println("libName libURL " + libURL);
		// System.loadLibrary();
		clutoLibLoaded = true;
	    } catch (Throwable t) {
		System.err.println(libName + "\t" + t);
	    }
	}
    }

    /** <b>CLUTO</b> copyright */
    public static final String copyright = "CLUTO 2.1 Copyright 2001-02, Regents of the University of Minnesota";

    /*
     * ------------------------------------------------------------------------
     * Constant definitions
     * -------------------------------------------------------------------------
     */

    /* Different choices for RowModel */
    /** The columns of each row are not scaled and used as supplied */
    public static final int CLUTO_ROWMODEL_NONE = 1;
    /**
     * The columns of each row are scaled so their values are between 0.5 and
     * 1.0. TF = .5 + .5(TF/max(|TF|))
     */
    public static final int CLUTO_ROWMODEL_MAXTF = 2;
    /**
     * The columns of each row are scaled to be equal to the square root of
     * their actual values. TF = 1+sign(TF, sqrt(|TF|))
     */
    public static final int CLUTO_ROWMODEL_SQRT = 3;
    /**
     * The columns of each row are scaled to be equal to the log of their actual
     * values. TF = 1+sign(TF, log2(|TF|))
     */
    public static final int CLUTO_ROWMODEL_LOG = 4;

    /* Different choices for ColModel */
    /**
     * The columns of the matrix are not globally scaled and they are used as
     * is.
     */
    public static final int CLUTO_COLMODEL_NONE = 1;
    /**
     * The columns of the matrix are scaled according to the inverse document
     * frequency paradigm (IDF), used in information retrieval. In particular,
     * if rf<i>i</i> is the number of rows that the ith column belongs to, then
     * each entry of the <i>i</i>th column is scaled by log 2 (rf<i>i</i> /n).
     * The effect of this scaling is to de-emphasize columns that appear in many
     * rows. This is the default setting used by vcluster when the cosine
     * similarity function is used.
     */
    public static final int CLUTO_COLMODEL_IDF = 2;

    /*
     * The complexity of CLUTOs clustering algorithms. The meaning of the
     * various quantities are as follows: n is the number of objects to be
     * clustered, m is the number of dimensions, NNZ is the number of non-zeros
     * in the input matrix or similarity matrix, NNbrs is the number of
     * neighbors in the nearest-neighbor graph.
     * 
     * Algorithm Time Complexity Space Complexity VP_ClusterRB, -sim=cos O(NNZ
     * log(k)) O(NNZ) VP_ClusterRB, -sim=corr O(n m log(k)) O(n m)
     * VP_ClusterDirect, -sim=cos O(NNZ k + m k) O(NNZ + m k) VP_ClusterDirect,
     * -sim=corr O(n m k) O(n m + m k) VA_Cluster, O(n2 log(n)) O(n2)
     * VA_Cluster, -crfun=[
     */

    /* Different cluster criterion functions */
    /** The I1 from the manual */
    public static final int CLUTO_CLFUN_I1 = 1;
    /** The I2 from the manual */
    public static final int CLUTO_CLFUN_I2 = 2;
    /** The E1 from the manual */
    public static final int CLUTO_CLFUN_E1 = 3;
    /** The G1 from the manual */
    public static final int CLUTO_CLFUN_G1 = 4;
    /** The G1' from the manual */
    public static final int CLUTO_CLFUN_G1P = 5;
    /** The H1 from the manual */
    public static final int CLUTO_CLFUN_H1 = 6;
    /** The H2 from the manual */
    public static final int CLUTO_CLFUN_H2 = 7;
    /**
     * The traditional single-link method (can only be used within the context
     * of agglomerative clustering)
     */
    public static final int CLUTO_CLFUN_SLINK = 8;
    /**
     * The traditional single-link / MST method, cluster size weighted (can only
     * be used within the context of agglomerative clustering)
     */
    public static final int CLUTO_CLFUN_SLINK_W = 9;
    /**
     * The traditional complete-link method (can only be used within the context
     * of agglomerative clustering)
     */
    public static final int CLUTO_CLFUN_CLINK = 10;
    /**
     * The traditional complete-link method, cluster size weighted (can only be
     * used within the context of agglomerative clustering)
     */
    public static final int CLUTO_CLFUN_CLINK_W = 11;
    /**
     * The traditional UPGMA method (can only be used within the context of
     * agglomerative clustering)
     */
    public static final int CLUTO_CLFUN_UPGMA = 12;
    /**
     * The traditional weighted UPGMA method (can only be used within the
     * context of agglomerative clustering)
     */
    public static final int CLUTO_CLFUN_UPGMA_W = 13;

    /* The following are criterion functions for graph-based clustering */
    /** Edge-cut based ( graph-based clustering ) */
    public static final int CLUTO_CLFUN_CUT = 15;
    /** Ratio cut ( graph-based clustering ) */
    public static final int CLUTO_CLFUN_RCUT = 16;
    /** Normalized cut ( graph-based clustering ) */
    public static final int CLUTO_CLFUN_NCUT = 17;
    /** Min-Max cut ( graph-based clustering ) */
    public static final int CLUTO_CLFUN_MMCUT = 18;

    /* Different cluster selection schemes for RB */
    /** Select the largest cluster to bisect */
    public static final int CLUTO_CSTYPE_LARGEFIRST = 1;
    /** Select the cluster that leads the best value of the criterion function */
    public static final int CLUTO_CSTYPE_BESTFIRST = 2;
    /** Selects the cluster that leads to the largest subspace reduction */
    public static final int CLUTO_CSTYPE_LARGESUBSPACEFIRST = 3;

    /* Different dbglvl options */
    /** Show simple progress statistics */
    public static final int CLUTO_DBG_PROGRESS = 1;
    /** Show simple progress statistics during refinement */
    public static final int CLUTO_DBG_RPROGRESS = 2;
    /** Show progress during the agglomeration */
    public static final int CLUTO_DBG_APROGRESS = 4;
    /** Show progress statistics during coarsening */
    public static final int CLUTO_DBG_CPROGRESS = 8;
    /** Show vertex movement information during refinement */
    public static final int CLUTO_DBG_MPROGRESS = 16;
    /** Show stats during cc elimination */
    public static final int CLUTO_DBG_CCMPSTAT = 32;

    /* Different option for memory re-use for the SA-routines */
    /** Preserves the supplied information */
    public static final int CLUTO_MEM_NOREUSE = 1;
    /** Does not preserve the supplied information */
    public static final int CLUTO_MEM_REUSE = 2;

    /*
     * Different types of trees that <b>CLUTO</b> can build on top of the
     * clustering solution
     */
    /** Builds a tree on top of the supplied clustering */
    public static final int CLUTO_TREE_TOP = 1;
    /** Builds the entire tree that preserves the clustering */
    public static final int CLUTO_TREE_FULL = 2;

    /* Different similarity functions that <b>CLUTO</b> supports */
    /** Similarity is measured using the cosine function */
    public static final int CLUTO_SIM_COSINE = 1;
    /** Similarity is measured using Pearson's correlation coefficient */
    public static final int CLUTO_SIM_CORRCOEF = 2;
    /** Similarity is measured using the negative Euclidean distance */
    public static final int CLUTO_SIM_EDISTANCE = 3;
    /** Similarity is measured using the extended Jaccard */
    public static final int CLUTO_SIM_EJACCARD = 4;

    /* Different types of optimizers implemeted by <b>CLUTO</b> */
    /** Traditional single-level optimizer */
    public static final int CLUTO_OPTIMIZER_SINGLELEVEL = 1;
    /** Better multi-level optimizer */
    public static final int CLUTO_OPTIMIZER_MULTILEVEL = 2;

    /* Different ways for performing the graph coarsening */
    /** Heavy-edge matching */
    public static final int CLUTO_MTYPE_HEDGE = 1;
    /** Heavy-star matching */
    public static final int CLUTO_MTYPE_HSTAR = 2;
    /** Heavy-star matching */
    public static final int CLUTO_MTYPE_HSTAR2 = 3;

    /* Different type of neighborhood graph models */
    /** Computes similarity exactly, and includes edges for all of them */
    public static final int CLUTO_GRMODEL_EXACT_ASYMETRIC_DIRECT = 1;
    /** Computes similarity exactly, and includes edges only if they are shared */
    public static final int CLUTO_GRMODEL_EXACT_SYMETRIC_DIRECT = 2;
    /**
     * Computes most similar vertices inexactly, and includes edges for all of
     * them
     */
    public static final int CLUTO_GRMODEL_INEXACT_ASYMETRIC_DIRECT = 3;
    /**
     * Computes most similar vertices inexactly, includes edges only if they are
     * shared
     */
    public static final int CLUTO_GRMODEL_INEXACT_SYMETRIC_DIRECT = 4;
    /** Computes similarity exactly, and includes edges for all of them */
    public static final int CLUTO_GRMODEL_EXACT_ASYMETRIC_LINKS = 5;
    /** Computes similarity exactly, and includes edges only if they are shared */
    public static final int CLUTO_GRMODEL_EXACT_SYMETRIC_LINKS = 6;
    /**
     * Computes most similar vertices inexactly, and includes edges for all of
     * them
     */
    public static final int CLUTO_GRMODEL_INEXACT_ASYMETRIC_LINKS = 7;
    /**
     * Computes most similar vertices inexactly, includes edges only if they are
     * shared
     */
    public static final int CLUTO_GRMODEL_INEXACT_SYMETRIC_LINKS = 8;

    /**
     * An edge between two vertices u and v is included as long as one of them
     * is in the nearest neighbor list of the other. The weight of this edge is
     * set equal to the similarity of the objects.
     */
    public static final int CLUTO_GRMODEL_ASYMETRIC_DIRECT = CLUTO_GRMODEL_EXACT_ASYMETRIC_DIRECT;

    /**
     * An edge between two vertices u and v is included as long as one of them
     * is in the nearestneighbor list of the other. The weight of this edge was
     * set equal to the number of neighbors that vertices u and v have in
     * common.
     */
    public static final int CLUTO_GRMODEL_ASYMETRIC_LINKS = CLUTO_GRMODEL_EXACT_ASYMETRIC_LINKS;

    /**
     * An edge between two vertices u and v is included if and only if they are
     * in the nearestneighbor list of each other. The weight of this edge is set
     * equal to the similarity of the objects.
     */
    public static final int CLUTO_GRMODEL_SYMETRIC_DIRECT = CLUTO_GRMODEL_EXACT_SYMETRIC_DIRECT;

    /**
     * An edge between two vertices u and v is included if and only if they are
     * in the nearestneighbor list of each other. The weight of this edge was
     * set equal to the number of neighbors that vertices u and v have in
     * common.
     */
    public static final int CLUTO_GRMODEL_SYMETRIC_LINKS = CLUTO_GRMODEL_EXACT_SYMETRIC_LINKS;

    /** The supplied graph is used as is. */
    public static final int CLUTO_GRMODEL_NONE = 9;

    /* Summary Types */
    /** Find maximal feature cliques */
    public static final int CLUTO_SUMMTYPE_MAXCLIQUES = 1;
    /** Find maximal itemset cliques */
    public static final int CLUTO_SUMMTYPE_MAXITEMSETS = 2;

    /* <b>CLUTO</b>'s version number */
    /** <b>CLUTO</b>'s major version number */
    public static final int CLUTO_VER_MAJOR = 2;
    /** <b>CLUTO</b>'s minor version number */
    public static final int CLUTO_VER_MINOR = 1;
    /** <b>CLUTO</b>'s sub minor version number */
    public static final int CLUTO_VER_SUBMINOR = 0;

    // These were not in cluto.h, but were added here
    /** No global optimization of the clustering solution. */
    public static final int CLUTO_GLOBAL_OPTIMIZIMATION_NONE = 0;
    /** Optimize the clustering solution globally. */
    public static final int CLUTO_GLOBAL_OPTIMIZIMATION_KWAYREFINE = 1;

    /**
     * the desired k-way clustering solution is computed by simultaneously
     * finding all k clusters.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param cstype
     *            The method to be used for selecting the next cluster to be
     *            bisected.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param vtxprune
     *            The vertex pruning threshold, a vertex will be eliminated if
     *            its degree is less than vtxprune * nnbrs.
     * @param edgeprune
     *            The edge pruning threshold, an edge (u,v) will be eliminated
     *            if and only if link(u,v) < edgeprune * nnbrs, where link u v)
     *            is the graph-partitioning based clustering algorithm.
     * @param mincmp
     *            The size of the minimum connect component that will be pruned
     *            prior to clustering.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param niter
     *            the maximum number of iterations ((> 0) that are performed
     *            during each refinement cycle.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param kwayrefine
     *            whether or not the clustering solution will be globally
     *            optimized at the end by performing a series of k-way
     *            refinement iterations. This can significantly increase the
     *            amount of time required to perform the clustering.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param tsims
     *            an array of size 2*nrows that upon successful completion
     *            stores the average similarity between every pair of siblings
     *            in the induced tree. In particular, tsims[i] stores the
     *            average pairwise similarity between the pair of clusters that
     *            are the children of the ith node of the tree. Note that the
     *            first nrows entries of this vector are not defined and are set
     *            to 0.0. The application is responsible for allocating the
     *            memory for this array.
     * @param gains
     *            This is an array of size 2*nrows that upon successful
     *            completion stores the gains in the value of the criterion
     *            function resulted by the merging pairs of clusters. In
     *            particular, gains[i] stores the gain achieved by merging the
     *            clusters that are the children of the ith node of the tree.
     *            Note that the first nrows entries of this vector are not
     *            defined and are set to 0.0. The application is responsible for
     *            allocating the memory for this array.
     * @param r_crvalue
     *            This is a variable that upon returns stores the edge-cut of
     *            the clustering solution.
     * @return the number of clusters that it found. This number will be equal
     *         to the number of desired clusters plus the number of connected
     *         components in the graph.
     */

    /**
     * Cluster a matrix into a specified (k) number of clusters using a
     * partitional clustering algorithm that computes the k-way clustering
     * directly.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param niter
     *            the maximum number of iterations ((> 0) that are performed
     *            during each refinement cycle.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     */
    public static native void VP_ClusterDirect(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun, int crfun,
	    int rowmodel, int colmodel, float colprune, int ntrials, int niter,
	    int seed, int dbglvl, int nparts, int[] part);

    /**
     * Cluster a matrix into a specified (k) number of clusters using a
     * partitional clustering algorithm that computes the k-way by performing a
     * sequence of repeated bisections. NOTE: VP ClusterRB is considerably
     * faster than VP_ClusterDirect and it should be preferred if the number of
     * desired clusters is quite large (e.g., greater than 20 30).
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param niter
     *            the maximum number of iterations ((> 0) that are performed
     *            during each refinement cycle.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param rbtype
     *            The method to be used for selecting the next cluster to be
     *            bisected.
     * @param kwayrefine
     *            whether or not the clustering solution will be globally
     *            optimized at the end by performing a series of k-way
     *            refinement iterations. This can significantly increase the
     *            amount of time required to perform the clustering.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     */
    public static native void VP_ClusterRB(int nrows, int ncols, int[] rowptr,
	    int[] rowind, float[] rowval, int simfun, int crfun, int rowmodel,
	    int colmodel, float colprune, int ntrials, int niter, int seed,
	    int rbtype, int kwayrefine, int dbglvl, int nparts, int[] part);

    /**
     * Cluster a matrix hierarchically using a partitional clustering algorithm
     * that computes the hierarchical tree by performing a sequence of repeated
     * bisections.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param niter
     *            the maximum number of iterations ((> 0) that are performed
     *            during each refinement cycle.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     */
    public static native void VP_ClusterRBTree(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun, int crfun,
	    int rowmodel, int colmodel, float colprune, int ntrials, int niter,
	    int seed, int dbglvl, int nparts, int[] part, int[] ptree);

    /**
     * Cluster a matrix into a specified (k) number of clusters using a
     * hierarchical agglomerative clustering algorithm. Note Due to the high
     * computational requirements of CLUTO VA Cluster, it should only be used to
     * cluster matrices that have fewer than 3,000 6,000 rows.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param tsims
     *            an array of size 2*nrows that upon successful completion
     *            stores the average similarity between every pair of siblings
     *            in the induced tree. In particular, tsims[i] stores the
     *            average pairwise similarity between the pair of clusters that
     *            are the children of the ith node of the tree. Note that the
     *            first nrows entries of this vector are not defined and are set
     *            to 0.0. The application is responsible for allocating the
     *            memory for this array.
     * @param gains
     *            This is an array of size 2*nrows that upon successful
     *            completion stores the gains in the value of the criterion
     *            function resulted by the merging pairs of clusters. In
     *            particular, gains[i] stores the gain achieved by merging the
     *            clusters that are the children of the ith node of the tree.
     *            Note that the first nrows entries of this vector are not
     *            defined and are set to 0.0. The application is responsible for
     *            allocating the memory for this array.
     */
    public static native void VA_Cluster(int nrows, int ncols, int[] rowptr,
	    int[] rowind, float[] rowval, int simfun, int crfun, int rowmodel,
	    int colmodel, float colprune, int dbglvl, int nparts, int[] part,
	    int[] ptree, float[] tsims, float[] gains);

    /**
     * Cluster a matrix into a specified (k) number of clusters using a
     * hierarchical agglomerative clustering algorithm that is biased by a
     * partitionally computed clustering solution. Note Due to the high
     * computational requirements of CLUTO VA ClusterBiased, it should only be
     * used to cluster matrices that have fewer than 3,000 6,000 rows.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param dbglvl
     *            The debugging parameter.
     * @param pnparts
     *            The number of clusters for which the partitional clustering
     *            solution will be computed. The sqrt(nrows) is a reasonable
     *            default.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param tsims
     *            an array of size 2*nrows that upon successful completion
     *            stores the average similarity between every pair of siblings
     *            in the induced tree. In particular, tsims[i] stores the
     *            average pairwise similarity between the pair of clusters that
     *            are the children of the ith node of the tree. Note that the
     *            first nrows entries of this vector are not defined and are set
     *            to 0.0. The application is responsible for allocating the
     *            memory for this array.
     * @param gains
     *            This is an array of size 2*nrows that upon successful
     *            completion stores the gains in the value of the criterion
     *            function resulted by the merging pairs of clusters. In
     *            particular, gains[i] stores the gain achieved by merging the
     *            clusters that are the children of the ith node of the tree.
     *            Note that the first nrows entries of this vector are not
     *            defined and are set to 0.0. The application is responsible for
     *            allocating the memory for this array.
     */
    public static native void VA_ClusterBiased(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun, int crfun,
	    int rowmodel, int colmodel, float colprune, int dbglvl,
	    int pnparts, int nparts, int[] part, int[] ptree, float[] tsims,
	    float[] gains);

    /**
     * Cluster a matrix into a specified (k) number of clusters using a
     * graph-partitioning-based clustering algorithm that computes the k-way by
     * performing a sequence of repeated min-cut bisections.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param grmodel
     *            The type of k-nearest neighbor graph that will be built by
     *            CLUTO s graph-partitioning based clustering algorithms.
     * @param nnbrs
     *            The number of neighbors of each object that will be used to
     *            create the nearest neighbor graph.
     * @param edgeprune
     *            The edge pruning threshold, an edge (u,v) will be eliminated
     *            if and only if link(u,v) < edgeprune * nnbrs, where link u v)
     *            is the graph-partitioning based clustering algorithm.
     * @param vtxprune
     *            The vertex pruning threshold, a vertex will be eliminated if
     *            its degree is less than vtxprune * nnbrs.
     * @param mincmp
     *            The size of the minimum connect component that will be pruned
     *            prior to clustering.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param rbtype
     *            The method to be used for selecting the next cluster to be
     *            bisected.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param r_crvalue
     *            This is a variable that upon returns stores the edge-cut of
     *            the clustering solution.
     * @return the number of clusters that it found. This number will be equal
     *         to the number of desired clusters plus the number of connected
     *         components in the graph.
     */
    public static native int VP_GraphClusterRB(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int grmodel, int nnbrs,
	    float edgeprune, float vtxprune, int mincmp, int ntrials, int seed,
	    int rbtype, int dbglvl, int nparts, int[] part, float[] r_crvalue);

    /**
     * Cluster a matrix into a specified (k) number of clusters using a
     * partitional clustering algorithm that computes the k-way by performing a
     * sequence of repeated bisections. NOTE: CLUTO SP ClusterRB is considerably
     * faster than CLUTO SP ClusterDirect and it should be preferred if the
     * number of desired clusters is quite large (e.g., greater than 20 30).
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param niter
     *            the maximum number of iterations ((> 0) that are performed
     *            during each refinement cycle.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param cstype
     *            The method to be used for selecting the next cluster to be
     *            bisected.
     * @param kwayrefine
     *            whether or not the clustering solution will be globally
     *            optimized at the end by performing a series of k-way
     *            refinement iterations. This can significantly increase the
     *            amount of time required to perform the clustering.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     */
    public static native void SP_ClusterRB(int nvtxs, int[] xadj, int[] adjncy,
	    float[] adjwgt, int crfun, int ntrials, int niter, int seed,
	    int cstype, int kwayrefine, int dbglvl, int nparts, int[] part);

    /**
     * Cluster a graph into a specified (k) number of clusters using a
     * partitional clustering algorithm that computes the k-way clustering
     * directly.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param niter
     *            the maximum number of iterations ((> 0) that are performed
     *            during each refinement cycle.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     */

    public static native void SP_ClusterDirect(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int crfun, int ntrials, int niter,
	    int seed, int dbglvl, int nparts, int[] part);

    /**
     * Cluster a graph into a specified (k) number of clusters using a
     * hierarchical agglomerative clustering algorithm. NOTE: Due to the high
     * computational requirements of CLUTO SA Cluster, it should only be used to
     * cluster matrices that have fewer than 3,000 6,000 rows.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param tsims
     *            an array of size 2*nrows that upon successful completion
     *            stores the average similarity between every pair of siblings
     *            in the induced tree. In particular, tsims[i] stores the
     *            average pairwise similarity between the pair of clusters that
     *            are the children of the ith node of the tree. Note that the
     *            first nrows entries of this vector are not defined and are set
     *            to 0.0. The application is responsible for allocating the
     *            memory for this array.
     * @param gains
     *            This is an array of size 2*nrows that upon successful
     *            completion stores the gains in the value of the criterion
     *            function resulted by the merging pairs of clusters. In
     *            particular, gains[i] stores the gain achieved by merging the
     *            clusters that are the children of the ith node of the tree.
     *            Note that the first nrows entries of this vector are not
     *            defined and are set to 0.0. The application is responsible for
     *            allocating the memory for this array.
     */
    public static native void SA_Cluster(int nvtxs, int[] xadj, int[] adjncy,
	    float[] adjwgt, int crfun, int dbglvl, int nparts, int[] part,
	    int[] ptree, float[] tsims, float[] gains);

    /**
     * Cluster a matrix into into a specified (k) number of clusters using a
     * graph-partitioning-based clustering algorithm that computes the k-way by
     * performing a sequence of repeated min-cut bisections.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param nnbrs
     *            The number of neighbors of each object that will be used to
     *            create the nearest neighbor graph.
     * @param edgeprune
     *            The edge pruning threshold, an edge (u,v) will be eliminated
     *            if and only if link(u,v) < edgeprune * nnbrs, where link u v)
     *            is the graph-partitioning based clustering algorithm.
     * @param vtxprune
     *            The vertex pruning threshold, a vertex will be eliminated if
     *            its degree is less than vtxprune * nnbrs.
     * @param mincmp
     *            The size of the minimum connect component that will be pruned
     *            prior to clustering.
     * @param ntrials
     *            the number (> 0) of different clustering solutions to be
     *            computed, the one that achieves the best value of the
     *            criterion function is returned.
     * @param seed
     *            The seed to be used by the random number generator.
     * @param cstype
     *            The method to be used for selecting the next cluster to be
     *            bisected.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param r_crvalue
     *            This is a variable that upon returns stores the edge-cut of
     *            the clustering solution.
     * @return the number of clusters that it found. This number will be equal
     *         to the number of desired clusters plus the number of connected
     *         components in the graph.
     */
    public static native int SP_GraphClusterRB(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int nnbrs, float edgeprune,
	    float vtxprune, int mincmp, int ntrials, int seed, int cstype,
	    int dbglvl, int nparts, int[] part, float[] r_crvalue);

    /**
     * Create a nearest-neighbor graph of the set of objects. This is graph can
     * be used as input to the graph partitioning based clustering algorithm
     * (SP_GraphClusterRB).
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param grmodel
     *            The type of k-nearest neighbor graph that will be built by
     *            CLUTO s graph-partitioning based clustering algorithms.
     * @param nnbrs
     *            The number of neighbors of each object that will be used to
     *            create the nearest neighbor graph.
     * @param dbglvl
     *            The debugging parameter.
     * @param r_xadj
     *            the starting and ending index into the <i>rowind</i>array for
     *            each row in the matrix, {@link ClutoMatrix#getRowPtr()}.
     * @param r_adjncy
     *            the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param r_adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     */
    public static native void V_GetGraph(int nrows, int ncols, int[] rowptr,
	    int[] rowind, float[] rowval, int simfun, int rowmodel,
	    int colmodel, float colprune, int grmodel, int nnbrs, int dbglvl,
	    int[][] r_xadj, int[][] r_adjncy, float[][] r_adjwgt);

    /**
     * Create a nearest-neighbor graph of the set of objects. This is graph can
     * be used as input to the graph partitioning based clustering algorithm
     * (SP_GraphClusterRB).
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param grmodel
     *            The type of k-nearest neighbor graph that will be built by
     *            CLUTO s graph-partitioning based clustering algorithms.
     * @param nnbrs
     *            The number of neighbors of each object that will be used to
     *            create the nearest neighbor graph.
     * @param dbglvl
     *            The debugging parameter.
     * @param r_xadj
     *            the starting and ending index into the <i>rowind</i>array for
     *            each row in the matrix, {@link ClutoMatrix#getRowPtr()}.
     * @param r_adjncy
     *            the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param r_adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     */
    public static native void S_GetGraph(int nvtxs, int[] xadj, int[] adjncy,
	    float[] adjwgt, int grmodel, int nnbrs, int dbglvl, int[][] r_xadj,
	    int[][] r_adjncy, float[][] r_adjwgt);

    /**
     * Returns the value of a particular criterion function for a given
     * clustering solution.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @return the value of a particular criterion function.
     */
    public static native float V_GetSolutionQuality(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun, int crfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part);

    /**
     * Returns the value of a particular criterion function for a given
     * clustering solution.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @return the value of a particular criterion function.
     */
    public static native float S_GetSolutionQuality(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int crfun, int nparts, int[] part);

    /**
     * Returns a number of statistics about a given clustering solution.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param pwgts
     *            An array of size nclusters that returns the sizes of the
     *            different clusters. In particular, the size of the ith cluster
     *            is returned in pwgts[i ]. The application is responsible for
     *            allocating the memory for this array.
     * @param cintsim
     *            An array of size nclusters that returns the average similarity
     *            between the objects assigned to each cluster. In particular,
     *            the average similarity between the objects of the ith cluster
     *            is returned in cintsim[i ]. The application is responsible for
     *            allocating the memory for this array.
     * @param cintsdev
     *            An array of size nclusters that returns the standard deviation
     *            of the average similarity between each object and the other
     *            objects in its own cluster. In particular, the standard
     *            deviation of the ith cluster is returned in cintsdev[i ]. The
     *            application is responsible for allocating the memory for this
     *            array.
     * @param izscores
     *            An array of size nrows that returns the internal z-scores of
     *            each object. The internal z-score of the ith object is
     *            returned in izscores[i ]. The internal z-score of each object
     *            is described in the discussion of the -zscores option of
     *            vcluster. The application is responsible for allocating the
     *            memory for this array.
     * @param cextsim
     *            An array of size nclusters that returns the average similarity
     *            between the objects of each cluster and the remaining objects.
     *            In particular, the average external similarity of the objects
     *            of the ith cluster is returned in cextsim[i ]. The application
     *            is responsible for allocating the memory for this array.
     * @param cextsdev
     *            An array of size nclusters that returns the standard deviation
     *            of the average external similarities of each object. In
     *            particular, the external standard deviation of the objects of
     *            the ith cluster is returned in cextsdev[i ]. The application
     *            is responsible for allocating the memory for this array.
     * @param ezscores
     *            An array of size nrows that returns the external z-scores of
     *            each object. The external z-score of the ith object is
     *            returned in ezscores[i ]. The external z-score of each object
     *            is described in the discussion of the -zscores option of
     *            vcluster. The application is responsible for allocating the
     *            memory for this array.
     */
    public static native void V_GetClusterStats(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part,
	    int[] pwgts, float[] cintsim, float[] cintsdev, float[] izscores,
	    float[] cextsim, float[] cextsdev, float[] ezscores);

    /**
     * Returns a number of statistics about a given clustering solution.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param pwgts
     *            An array of size nclusters that returns the sizes of the
     *            different clusters. In particular, the size of the ith cluster
     *            is returned in pwgts[i ]. The application is responsible for
     *            allocating the memory for this array.
     * @param cintsim
     *            An array of size nclusters that returns the average similarity
     *            between the objects assigned to each cluster. In particular,
     *            the average similarity between the objects of the ith cluster
     *            is returned in cintsim[i ]. The application is responsible for
     *            allocating the memory for this array.
     * @param cintsdev
     *            An array of size nclusters that returns the standard deviation
     *            of the average similarity between each object and the other
     *            objects in its own cluster. In particular, the standard
     *            deviation of the ith cluster is returned in cintsdev[i ]. The
     *            application is responsible for allocating the memory for this
     *            array.
     * @param izscores
     *            An array of size nrows that returns the internal z-scores of
     *            each object. The internal z-score of the ith object is
     *            returned in izscores[i ]. The internal z-score of each object
     *            is described in the discussion of the -zscores option of
     *            vcluster. The application is responsible for allocating the
     *            memory for this array.
     * @param cextsim
     *            An array of size nclusters that returns the average similarity
     *            between the objects of each cluster and the remaining objects.
     *            In particular, the average external similarity of the objects
     *            of the ith cluster is returned in cextsim[i ]. The application
     *            is responsible for allocating the memory for this array.
     * @param cextsdev
     *            An array of size nclusters that returns the standard deviation
     *            of the average external similarities of each object. In
     *            particular, the external standard deviation of the objects of
     *            the ith cluster is returned in cextsdev[i ]. The application
     *            is responsible for allocating the memory for this array.
     * @param ezscores
     *            An array of size nrows that returns the external z-scores of
     *            each object. The external z-score of the ith object is
     *            returned in ezscores[i ]. The external z-score of each object
     *            is described in the discussion of the -zscores option of
     *            vcluster. The application is responsible for allocating the
     *            memory for this array.
     */
    public static native void S_GetClusterStats(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int nparts, int[] part, int[] pwgts,
	    float[] cintsim, float[] cintsdev, float[] izscores,
	    float[] cextsim, float[] cextsdev, float[] ezscores);

    /**
     * Returns the set of features that best describe and discriminate each one
     * of the clusters of a given clustering solution.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param nfeatures
     *            The number of descriptive and discriminating features that is
     *            desired.
     * @param internalids
     *            An array of size nclusters*nfeatures that returns the column
     *            numbers of the descriptive features. The set of features of
     *            the ith cluster are stored in the internalids array starting
     *            at location i nfeatures up to location (but excluding) (i + 1)
     *            nfeatures. The set of features for each cluster are returned
     *            in decreasing importance order. The numbering of the returned
     *            columns starts from zero. The application is responsible for
     *            allocating the memory for this array.
     * @param internalwgts
     *            An array of size nclusters*nfeatures that returns the weight
     *            of each one of the descriptive features returned in the
     *            internalids array. The weight of the features stored in the
     *            ith location of the internalids array is returned in the ith
     *            location of the internalwgts array. The weights are numbers
     *            between 0.0 and 1.0 and represent the fraction of the within
     *            cluster similarity that each particular feature is responsible
     *            for. The application is responsible for allocating the memory
     *            for this array.
     * @param externalids
     *            an array of size nclusters*nfeatures that returns the column
     *            numbers of the discriminating features. The set of features of
     *            the ith cluster are stored in the externalids array starting
     *            at location i nfeatures up to location (but excluding) (i +1)
     *            nfeatures. The set of features for each cluster are returned
     *            in decreasing importance order. The numbering of the returned
     *            columns starts from zero. The application is responsible for
     *            allocating the memory for this array.
     * @param externalwgts
     *            an array of size nclusters*nfeatures that returns the weight
     *            of each one of the discriminating features returned in the
     *            externalids array. The weight of the features stored in the
     *            ith location of the externalids array is returned in the ith
     *            location of the externalwgts array. The weights are numbers
     *            between 0.0 and 1.0 and represent the fraction of the
     *            dissimilarity between the cluster and the rest of the objects
     *            that each particular feature is responsible for. The
     *            application is responsible for allocating the memory for this
     *            array.
     */
    public static native void V_GetClusterFeatures(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part,
	    int nfeatures, int[] internalids, float[] internalwgts,
	    int[] externalids, float[] externalwgts);

    /**
     * Builds a hierarchical agglomerative tree that preserves the clustering
     * solution supplied in the part array. It can build two types of trees. The
     * first type is a tree built on top of a particular clustering solution,
     * such that the leaves of the tree correspond to the different clusters.
     * The second type of tree is a complete agglomerative tree that preserves
     * the clustering. The hierarchical agglomerative tree is build so that it
     * optimizes a particular clustering criterion function.
     * 
     * NOTE: In order for this routine to get the accurate set of features for a
     * particular clustering solution, the values for the rowmodel, colmodel,
     * and colprune, nclusters, part, and ptree parameters should be identical
     * to those used to compute the clustering solution and build the
     * hierarchical agglomerative tree.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param treetype
     *            Specifies the type of tree that needs to be built. The
     *            possible values for this parameter are: CLUTO_TREE_TOP Builds
     *            a tree whose leaves correspond to the different clusters.
     *            CLUTO_TREE_FULL Builds a complete tree that preserves the
     *            clustering solution.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param ptree
     *            An array whose size depends on the type of tree that is
     *            requested. If treetype==CLUTO TREE TOP, then it is of size
     *            2*nclusters that upon successful completion stores the parent
     *            array of the binary hierarchical tree. In this tree, each node
     *            corresponds to a cluster. The leaf nodes are the original
     *            nclusters clusters supplied via the part array, and they are
     *            numbered from 0 to nclusters-1. The internal nodes of the tree
     *            are numbered from nclusters to 2*nclusters-2. The root of the
     *            tree is numbered 2*nclusters-2. If treetype==CLUTO TREE FULL,
     *            then it is of size 2*nrows that upon successful completion
     *            stores the parent array of the binary hierarchical tree. In
     *            this tree, each node corresponds to a cluster. The leaf nodes
     *            are the original rows of the matrix, and they are numbered
     *            from 0 to nrows-1. The internal nodes of the tree are numbered
     *            from nrows to 2*nrows-2. The root of the tree is numbered
     *            2*nrows- 2. The numbering of the internal nodes is done in
     *            such a fashion so that smaller numbers correspond to clusters
     *            obtained by merging a pair of clusters earlier during the
     *            agglomeration process. The ith entry of the ptree array stores
     *            the parent node of the i node of the tree. The ptree entry for
     *            the root is set to -1. The application is responsible for
     *            allocating the memory for this array.
     * @param tsims
     *            An array whose size depends on the type of tree that is
     *            requested. If treetype==CLUTO TREE TOP, then it is of size
     *            2*nclusters and if treetype==CLUTO TREE FULL then it is of
     *            size 2*nrows. Upon successful completion stores the average
     *            similarity between every pair of siblings in the induced tree.
     *            In particular, tsims[i] stores the average pairwise similarity
     *            between the pair of clusters that are the children of the ith
     *            node of the tree. Note that the first nclusters or nrows
     *            entries of this vector are not defined and are set to 0.0. The
     *            application is responsible for allocating the memory for this
     *            array.
     * @param gains
     *            An array whose size depends on the type of tree that is
     *            requested. If treetype==CLUTO TREE TOP, then it is of size
     *            2*nclusters and if treetype==CLUTO TREE FULL then it is of
     *            size 2*nrows. Upon successful completion stores the gains in
     *            the value of the criterion function resulted by the merging
     *            pairs of clusters. In particular, gains[i] stores the gain
     *            achieved by merging the clusters that are the children of the
     *            ith node of the tree. Note that the first nclusters or nrows
     *            entries of this vector are not defined and are set to 0.0. The
     *            application is responsible for allocating the memory for this
     *            array.
     */
    public static native void V_BuildTree(int nrows, int ncols, int[] rowptr,
	    int[] rowind, float[] rowval, int simfun, int crfun, int rowmodel,
	    int colmodel, float colprune, int treetype, int dbglvl, int nparts,
	    int[] part, int[] ptree, float[] tsims, float[] gains);

    /**
     * Builds a hierarchical agglomerative tree that preserves the clustering
     * solution supplied in the part array. It can build two types of trees. The
     * first type is a tree built on top of a particular clustering solution,
     * such that the leaves of the tree correspond to the different clusters.
     * The second type of tree is a complete agglomerative tree that preserves
     * the clustering. The hierarchical agglomerative tree is build so that it
     * optimizes a particular clustering criterion function.
     * 
     * NOTE: In order for this routine to get the accurate set of features for a
     * particular clustering solution, the values for the rowmodel, colmodel,
     * and colprune, nclusters, part, and ptree parameters should be identical
     * to those used to compute the clustering solution and build the
     * hierarchical agglomerative tree.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param crfun
     *            The clustering criterion function to be used in finding the
     *            clusters.
     * @param treetype
     *            Specifies the type of tree that needs to be built. The
     *            possible values for this parameter are: CLUTO_TREE_TOP Builds
     *            a tree whose leaves correspond to the different clusters.
     *            CLUTO_TREE_FULL Builds a complete tree that preserves the
     *            clustering solution.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param ptree
     *            An array whose size depends on the type of tree that is
     *            requested. If treetype==CLUTO TREE TOP, then it is of size
     *            2*nclusters that upon successful completion stores the parent
     *            array of the binary hierarchical tree. In this tree, each node
     *            corresponds to a cluster. The leaf nodes are the original
     *            nclusters clusters supplied via the part array, and they are
     *            numbered from 0 to nclusters-1. The internal nodes of the tree
     *            are numbered from nclusters to 2*nclusters-2. The root of the
     *            tree is numbered 2*nclusters-2. If treetype==CLUTO TREE FULL,
     *            then it is of size 2*nrows that upon successful completion
     *            stores the parent array of the binary hierarchical tree. In
     *            this tree, each node corresponds to a cluster. The leaf nodes
     *            are the original rows of the matrix, and they are numbered
     *            from 0 to nrows-1. The internal nodes of the tree are numbered
     *            from nrows to 2*nrows-2. The root of the tree is numbered
     *            2*nrows- 2. The numbering of the internal nodes is done in
     *            such a fashion so that smaller numbers correspond to clusters
     *            obtained by merging a pair of clusters earlier during the
     *            agglomeration process. The ith entry of the ptree array stores
     *            the parent node of the i node of the tree. The ptree entry for
     *            the root is set to -1. The application is responsible for
     *            allocating the memory for this array.
     * @param tsims
     *            An array whose size depends on the type of tree that is
     *            requested. If treetype==CLUTO TREE TOP, then it is of size
     *            2*nclusters and if treetype==CLUTO TREE FULL then it is of
     *            size 2*nrows. Upon successful completion stores the average
     *            similarity between every pair of siblings in the induced tree.
     *            In particular, tsims[i] stores the average pairwise similarity
     *            between the pair of clusters that are the children of the ith
     *            node of the tree. Note that the first nclusters or nrows
     *            entries of this vector are not defined and are set to 0.0. The
     *            application is responsible for allocating the memory for this
     *            array.
     * @param gains
     *            An array whose size depends on the type of tree that is
     *            requested. If treetype==CLUTO TREE TOP, then it is of size
     *            2*nclusters and if treetype==CLUTO TREE FULL then it is of
     *            size 2*nrows. Upon successful completion stores the gains in
     *            the value of the criterion function resulted by the merging
     *            pairs of clusters. In particular, gains[i] stores the gain
     *            achieved by merging the clusters that are the children of the
     *            ith node of the tree. Note that the first nclusters or nrows
     *            entries of this vector are not defined and are set to 0.0. The
     *            application is responsible for allocating the memory for this
     *            array.
     */
    public static native void S_BuildTree(int nvtxs, int[] xadj, int[] adjncy,
	    float[] adjwgt, int crfun, int treetype, int dbglvl, int nparts,
	    int[] part, int[] ptree, float[] tsims, float[] gains);

    /**
     * Returns the set of features that best describe and discriminate each one
     * of the clusters of a given clustering solution.
     * 
     * NOTE: In order for this routine to get the accurate set of features for a
     * particular clustering solution, the values for the rowmodel, colmodel,
     * and colprune, nclusters, part, and ptree parameters should be identical
     * to those used to compute the clustering solution and build the
     * hierarchical agglomerative tree.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param pwgts
     *            An array of size nclusters that returns the sizes of the
     *            different clusters. In particular, the size of the ith cluster
     *            is returned in pwgts[i ]. The application is responsible for
     *            allocating the memory for this array.
     * @param cintsim
     *            An array of size nclusters that returns the average similarity
     *            between the objects assigned to each cluster. In particular,
     *            the average similarity between the objects of the ith cluster
     *            is returned in cintsim[i ]. The application is responsible for
     *            allocating the memory for this array.
     * @param cextsim
     *            An array of size nclusters that returns the average similarity
     *            between the objects of each cluster and the remaining objects.
     *            In particular, the average external similarity of the objects
     *            of the ith cluster is returned in cextsim[i ]. The application
     *            is responsible for allocating the memory for this array.
     */
    public static native void V_GetTreeStats(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part,
	    int[] ptree, int[] pwgts, float[] cintsim, float[] cextsim);

    /**
     * Returns the set of features that best describe and discriminate each one
     * of the clusters of a given clustering solution.
     * 
     * NOTE: In order for this routine to get the accurate set of features for a
     * particular clustering solution, the values for the rowmodel, colmodel,
     * and colprune, nclusters, part, and ptree parameters should be identical
     * to those used to compute the clustering solution and build the
     * hierarchical agglomerative tree.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param nfeatures
     *            The number of descriptive and discriminating features that is
     *            desired.
     * @param internalids
     *            An array of size nclusters*nfeatures that returns the column
     *            numbers of the descriptive features. The set of features of
     *            the ith cluster are stored in the internalids array starting
     *            at location i nfeatures up to location (but excluding) (i + 1)
     *            nfeatures. The set of features for each cluster are returned
     *            in decreasing importance order. The numbering of the returned
     *            columns starts from zero. The application is responsible for
     *            allocating the memory for this array.
     * @param internalwgts
     *            An array of size nclusters*nfeatures that returns the weight
     *            of each one of the descriptive features returned in the
     *            internalids array. The weight of the features stored in the
     *            ith location of the internalids array is returned in the ith
     *            location of the internalwgts array. The weights are numbers
     *            between 0.0 and 1.0 and represent the fraction of the within
     *            cluster similarity that each particular feature is responsible
     *            for. The application is responsible for allocating the memory
     *            for this array.
     * @param externalids
     *            an array of size nclusters*nfeatures that returns the column
     *            numbers of the discriminating features. The set of features of
     *            the ith cluster are stored in the externalids array starting
     *            at location i nfeatures up to location (but excluding) (i +1)
     *            nfeatures. The set of features for each cluster are returned
     *            in decreasing importance order. The numbering of the returned
     *            columns starts from zero. The application is responsible for
     *            allocating the memory for this array.
     * @param externalwgts
     *            an array of size nclusters*nfeatures that returns the weight
     *            of each one of the discriminating features returned in the
     *            externalids array. The weight of the features stored in the
     *            ith location of the externalids array is returned in the ith
     *            location of the externalwgts array. The weights are numbers
     *            between 0.0 and 1.0 and represent the fraction of the
     *            dissimilarity between the cluster and the rest of the objects
     *            that each particular feature is responsible for. The
     *            application is responsible for allocating the memory for this
     *            array.
     */
    public static native void V_GetTreeFeatures(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part,
	    int[] ptree, int nfeatures, int[] internalids,
	    float[] internalwgts, int[] externalids, float[] externalwgts);

    /**
     * Return a matrix with the values normalized. Given a matrix in parameters:
     * nrows, ncols, rowptr, rowind, and rowval, this applies scaling and
     * pruning specified by the simfun, rowmodel, colmodel, and colprune
     * parameters, then eliminates any rows unassigned to a cluster. The new
     * marix is returned in parameters: r_nrows, r_ncols, r_rowptr, r_rowind,
     * r_rowval. The mapping to old rows and cols is returned in parameters:
     * r_rimap and r_cimap.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param r_nrows
     *            An array of length 1, the number of rows in the output matrix
     *            is returned in r_nrows[0].
     * @param r_ncols
     *            An array of length 1, the number of columns of the output
     *            matrix is returned in r_ncols[0].
     * @param r_rowptr
     *            An array of length 1, the row pointer for the output matrix is
     *            returned as an array in r_rowptr[0].
     * @param r_rowind
     *            An array of length 1, the row index for the output matrix is
     *            returned as an array in r_rowind[0].
     * @param r_rowval
     *            An array of length 1, the row values for the output matrix is
     *            returned as an array in r_rowval[0].
     * @param r_rimap
     *            An array of length 1, the row index mapping from the output
     *            matrix to the input matrix is returned as an array in
     *            r_rimap[0]. The value of r_rimap[0][0] equals the index of the
     *            row in the input matrix corresponding to the first row in the
     *            output matrix.
     * @param r_cimap
     *            An array of length 1, the column index mapping from the output
     *            matrix to the input matrix is returned as an array in
     *            r_cimap[0]. The value of r_cimap[0][0] equals the index of the
     *            column in the input matrix corresponding to the first column
     *            in the output matrix.
     */
    public static native void InternalizeMatrix(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int[] part,
	    int[] r_nrows, int[] r_ncols, int[][] r_rowptr, int[][] r_rowind,
	    float[][] r_rowval, int[][] r_rimap, int[][] r_cimap);

    /*
     * public static native void S_TreeReorderInternal(int nrows, int[] rwgts,
     * float[] smat, int memflag, int dbglvl, int[] ptree, int[][] ftree);
     */

    /**
     * Reorders the forward tree and returns the result in the ftree array.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param dbglvl
     *            The debugging parameter.
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
     * @param ftree
     *            a 2 dimensional array that gives the child nodes for each node
     *            in a hierarchical tree of the agglomerative clustering for
     *            this matrix. Each tree node can have 2 child nodes, so [nrows
     *            - 2][0] and [nrows - 2][1] contain the child node indices in
     *            the tree of the root node. A value of -1 represents the lack
     *            of a child node. If nrows is the number of matrix rows, the
     *            array has a length of 2 * nrows - 1, with the root node at
     *            index 2 * nrows - 2. The first nrows of the array represents
     *            the original rows of the matrix as leaf nodes of the tree.
     * @see #constructForwardTree(int, int[])
     */
    public static native void V_TreeReorder(int nrows, int ncols, int[] rowptr,
	    int[] rowind, float[] rowval, int simfun, int rowmodel,
	    int colmodel, float colprune, int dbglvl, int[] ptree, int[][] ftree);

    /**
     * Reorders the forward tree and returns the result in the ftree array.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param dbglvl
     *            The debugging parameter.
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
     * @param ftree
     *            a 2 dimensional array that gives the child nodes for each node
     *            in a hierarchical tree of the agglomerative clustering for
     *            this matrix. Each tree node can have 2 child nodes, so [nrows
     *            - 2][0] and [nrows - 2][1] contain the child node indices in
     *            the tree of the root node. A value of -1 represents the lack
     *            of a child node. If nrows is the number of matrix rows, the
     *            array has a length of 2 * nrows - 1, with the root node at
     *            index 2 * nrows - 2. The first nrows of the array represents
     *            the original rows of the matrix as leaf nodes of the tree.
     * @see #constructForwardTree(int, int[])
     */
    public static native void S_TreeReorder(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int dbglvl, int[] ptree, int[][] ftree);

    /**
     * Reorders the forward tree and returns the result in the ftree array.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param ftree
     *            a 2 dimensional array that gives the child nodes for each node
     *            in a hierarchical tree of the agglomerative clustering for
     *            this matrix. Each tree node can have 2 child nodes, so [nrows
     *            - 2][0] and [nrows - 2][1] contain the child node indices in
     *            the tree of the root node. A value of -1 represents the lack
     *            of a child node. If nrows is the number of matrix rows, the
     *            array has a length of 2 * nrows - 1, with the root node at
     *            index 2 * nrows - 2. The first nrows of the array represents
     *            the original rows of the matrix as leaf nodes of the tree.
     * @see #constructForwardTree(int, int[])
     */
    public static native void V_ClusterTreeReorder(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int dbglvl, int nparts,
	    int[] part, int[] ptree, int[][] ftree);

    /**
     * Reorders the forward tree and returns the result in the ftree array.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
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
     * @param ftree
     *            a 2 dimensional array that gives the child nodes for each node
     *            in a hierarchical tree of the agglomerative clustering for
     *            this matrix. Each tree node can have 2 child nodes, so [nrows
     *            - 2][0] and [nrows - 2][1] contain the child node indices in
     *            the tree of the root node. A value of -1 represents the lack
     *            of a child node. If nrows is the number of matrix rows, the
     *            array has a length of 2 * nrows - 1, with the root node at
     *            index 2 * nrows - 2. The first nrows of the array represents
     *            the original rows of the matrix as leaf nodes of the tree.
     * @see #constructForwardTree(int, int[])
     */
    public static native void S_ClusterTreeReorder(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int dbglvl, int nparts, int[] part,
	    int[] ptree, int[][] ftree);

    /**
     * Reorders the partitions and returns the order in the part array.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     */
    public static native void V_ReorderPartitions(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part);

    /**
     * Reorders the partitions and returns the order in the part array.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     */

    public static native void S_ReorderPartitions(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int nparts, int[] part);

    /**
     * Compute the distances between every pair of clusters.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param distmat
     *            An array of size nparts squared that will contain the
     *            distances between each pair of clusters. distmat[i*nparts+j]
     *            contains the distance between cluster part i and j.
     */
    public static native void V_GetClusterDistanceMatrix(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int dbglvl, int nparts,
	    int[] part, float[] distmat);

    /**
     * Compute the distances between every pair of clusters.
     * 
     * @param nvtxs
     *            The number of rows of the input adjacency matrix whose rows
     *            store the adjacency structure of the between-object similarity
     *            graph.
     * @param xadj
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param adjncy
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param adjwgt
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param dbglvl
     *            The debugging parameter.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param distmat
     *            An array of size nparts squared that will contain the
     *            distances between each pair of clusters. distmat[i*nparts+j]
     *            contains the distance between cluster part i and j.
     */
    public static native void S_GetClusterDistanceMatrix(int nvtxs, int[] xadj,
	    int[] adjncy, float[] adjwgt, int dbglvl, int nparts, int[] part,
	    float[] distmat);

    /**
     * Returns sets of features that frequently co-occur within the objects of
     * each cluster. NOTE: This routine will produce meaningful results only for
     * sparse and high-dimensional datasets.
     * 
     * @param nrows
     *            The number of rows in the input matrix.
     * @param ncols
     *            The number of columns of the input matrix.
     * @param rowptr
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the starting and ending index into the
     *            <i>rowind</i>array for each row in the matrix,
     *            {@link ClutoMatrix#getRowPtr()}.
     * @param rowind
     *            if a dense matrix null, if a sparse matrix an array that
     *            provides the column index for this value in the matrix,
     *            {@link ClutoMatrix#getRowInd()}.
     * @param rowval
     *            the values of the cells as row-ordered array,
     *            {@link ClutoMatrix#getRowVal()}.
     * @param simfun
     *            The similarity function.
     * @param rowmodel
     *            The row scaling method.
     * @param colmodel
     *            The column scaling method.
     * @param colprune
     *            The column pruning threshold.
     * @param nparts
     *            The number of desired clusters.
     * @param part
     *            an array of size nrows that upon successful completion stores
     *            the clustering vector of the matrix.
     * @param sumtype
     *            Specifies the type of summaries that needs to be computed. The
     *            possible values for this parameter are: CLUTO SUMTYPE
     *            MAXCLIQUES Returns the features that form maximal cliques in
     *            the feature-to-feature co-occurrence graph. CLUTO SUMTYPE
     *            MAXITEMSETS Returns the features that occur frequently in the
     *            objects of each cluster. A frequent itemset is returned if it
     *            is maximal or if its frequency is much higher than the
     *            frequency of its maximal itemsets.
     * @param nfeatures
     *            The number of descriptive and discriminating features that is
     *            desired.
     * @param r_nsum
     * @param r_spid
     * @param r_swgt
     * @param r_sumptr
     * @param r_sumind
     */
    public static native void V_GetClusterSummaries(int nrows, int ncols,
	    int[] rowptr, int[] rowind, float[] rowval, int simfun,
	    int rowmodel, int colmodel, float colprune, int nparts, int[] part,
	    int sumtype, int nfeatures, int[] r_nsum, int[][] r_spid,
	    float[][] r_swgt, int[][] r_sumptr, int[][] r_sumind);

    /**
     * A convenience method to construct the forward tree from the given ptree.
     * Returns a 2 dimensional array that gives the child nodes for each node in
     * a hierarchical tree of the agglomerative clustering for a matrix. Each
     * tree node can have 2 child nodes, so [nrows - 2][0] and [nrows - 2][1]
     * contain the child node indices in the tree of the root node. A value of
     * -1 represents the lack of a child node. If nrows is the number of matrix
     * rows, the array has a length of 2 * nrows, with the root node at index 2
     * * nrows - 2. The first nrows of the array represents the original rows of
     * the matrix as leaf nodes of the tree.
     * 
     * @param nnrows
     *            The number of rows in the matrix from which the ptree was
     *            generated.
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
     * Return the <b>CLUTO</b> copyright information
     * 
     * @return the <b>CLUTO</b> copyright information
     */
    public static String getCopyright() {
	return copyright;
    }

    /**
     * Return whether the <b>CLUTO</b> native library is available.
     * 
     * @return whether the <b>CLUTO</b> native library is available.
     */
    public static boolean isClutoAvailable() {
	return clutoLibLoaded;
    }

}
