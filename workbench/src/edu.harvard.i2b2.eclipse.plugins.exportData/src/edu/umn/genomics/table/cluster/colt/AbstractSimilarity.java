/*
 * @(#) $RCSfile: AbstractSimilarity.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster.colt;

import java.util.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import edu.umn.genomics.table.*;
import edu.umn.genomics.table.cluster.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.doublealgo.*;

/**
 * AbstractSimilarity provides the basic operations for hierarchical clustering.
 * Extending classes need to define the distance calculation. NOTE: This
 * currently doesn't handle null values in table cells.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public abstract class AbstractSimilarity implements ClusterSimilarity {
    static String distFunctions[] = { "EUCLID", "MANHATTAN", "MAXIMUM",
	    "BRAY_CURTIS", "CANBERRA" };
    public int debug = 0;
    public TreeNode topNode = null;
    private Statistic.VectorVectorFunction distFunc = Statistic.EUCLID;
    private int remaining = 0;

    /**
     * Return the names for the available distance functions.
     * 
     * @return the available distance functions.
     */
    public static Object[] getDistanceFunctions() {
	return distFunctions;
    }

    /**
     * Set the distance function to use when calculating the distance matrix.
     * 
     * @param distanceFunction
     *            The name of the distance function.
     */
    public void setDistanceFunction(Object distanceFunction) {
	if (distanceFunction != null && distanceFunction instanceof String) {
	    String s = (String) distanceFunction;
	    if (s.equals(distFunctions[0])) {
		setDistanceFunction(Statistic.EUCLID);
	    } else if (s.equals(distFunctions[0])) {
		setDistanceFunction(Statistic.MANHATTAN);
	    } else if (s.equals(distFunctions[0])) {
		setDistanceFunction(Statistic.MAXIMUM);
	    } else if (s.equals(distFunctions[0])) {
		setDistanceFunction(Statistic.BRAY_CURTIS);
	    } else if (s.equals(distFunctions[0])) {
		setDistanceFunction(Statistic.CANBERRA);
	    }
	}
    }

    /**
     * Set the distance function to use when calculating the distance matrix.
     * 
     * @param distanceFunction
     *            The distance function.
     */
    public void setDistanceFunction(
	    Statistic.VectorVectorFunction distanceFunction) {
	this.distFunc = distanceFunction;
    }

    /**
     * Return the remaining number of nodes to cluster.
     * 
     * @return the nummber of nodes left to cluster.
     */
    public int getRemainingCount() {
	return remaining;
    }

    /**
     * Generate a distance matrix for the table, based on the values in the
     * given columns.
     * 
     * @param ctx
     *            The context from which to get ColumnMaps.
     * @param tm
     *            The table from which to get data values.
     * @param columns
     *            the column indexes to use for clustering.
     * @return the distance matrix
     */
    public DoubleMatrix2D getDistMat(TableContext ctx, TableModel tm,
	    int columns[]) {
	return getDistMat(ctx, tm, columns, distFunc);
    }

    /**
     * Generate a distance matrix for the table, based on the values in the
     * given columns.
     * 
     * @param ctx
     *            The context from which to get ColumnMaps.
     * @param tm
     *            The table from which to get data values.
     * @param columns
     *            the column indexes to use for clustering.
     * @param distanceFunc
     *            the distance function
     * @return the distance matrix
     */
    public DoubleMatrix2D getDistMat(TableContext ctx, TableModel tm,
	    int columns[], Statistic.VectorVectorFunction distanceFunc) {
	int ncol = columns.length;
	double[][] val = new double[ncol][];
	for (int c = 0; c < ncol; c++) {
	    ColumnMap cmap = ctx.getColumnMap(tm, columns[c]);
	    int num = cmap.getCount();
	    while (cmap.getState() == CellMap.MAPPING) {
		try {
		    Thread.sleep(100);
		} catch (Exception ex) {
		    break;
		}
	    }
	    val[c] = cmap.getMapValues();
	}
	DenseDoubleMatrix2D matrix = new DenseDoubleMatrix2D(val);
	val = null; // recover memory
	DoubleMatrix2D distMat = Statistic.distance(matrix, distanceFunc);
	// Now we should deal with NaN values which arrise from null cells
	double maxDist = 0.;
	int numNaNs = 0;

	for (int r = 0; r < distMat.rows(); r++) {
	    for (int c = r + 1; c < distMat.rows(); c++) {
		double dv = distMat.getQuick(r, c);
		if (Double.isNaN(dv)) {
		    DoubleMatrix1D rm = matrix.viewColumn(r);
		    DoubleMatrix1D cm = matrix.viewColumn(c);
		    int nonNaNs = 0;
		    for (int i = 0; i < rm.size(); i++) {
			if (!Double.isNaN(rm.get(i))
				&& !Double.isNaN(cm.get(i))) {
			    nonNaNs++;
			}
		    }
		    if (nonNaNs > 0) {
			DenseDoubleMatrix2D sm = new DenseDoubleMatrix2D(
				nonNaNs, 2);
			for (int i = 0, j = 0; i < rm.size(); i++) {
			    if (!Double.isNaN(rm.get(i))
				    && !Double.isNaN(cm.get(i))) {
				sm.setQuick(j, 0, rm.get(i));
				sm.setQuick(j, 1, cm.get(i));
				j++;
			    }
			}
			DoubleMatrix2D dm = Statistic
				.distance(sm, distanceFunc);
			dv = dm.getQuick(0, 1); // Assuming a symmetrical matrix
			distMat.setQuick(r, c, dv);
			distMat.setQuick(c, r, dv);
		    } else {
			numNaNs++;
		    }
		}
		if (Double.isNaN(dv) && dv > maxDist) {
		    maxDist = dv;
		}
	    }
	}
	// Not knowing what else to do with remaining NaNs,
	// I'll set them to 110% of the maxmum of any distance
	maxDist *= .1;
	for (int r = 0; r < distMat.rows() && numNaNs > 0; r++) {
	    for (int c = r + 1; c < distMat.rows(); c++) {
		double dv = distMat.getQuick(r, c);
		if (Double.isNaN(dv)) {
		    distMat.setQuick(r, c, maxDist);
		    distMat.setQuick(c, r, maxDist);
		    numNaNs--;
		}
	    }
	}
	return distMat;
    }

    /**
     * Generate a tree that represents a hierarchical clustering of the rows of
     * the table, based on the values in the given columns.
     * 
     * @param ctx
     *            The context from which to get ColumnMaps.
     * @param tm
     *            The table from which to get data values.
     * @param columns
     *            the column indexes to use for clustering.
     * @return the root node of a tree that represents a hierarchical clustering
     *         of the rows of the table.
     */
    public TreeNode cluster(TableContext ctx, TableModel tm, int columns[]) {
	remaining = tm.getRowCount();
	Vector v = new Vector();
	for (int r = 0; r < tm.getRowCount(); r++) {
	    v.add(new RowCluster(tm, r, columns));
	}
	DoubleMatrix2D dm = getDistMat(ctx, tm, columns);
	topNode = cluster(v, dm);
	return topNode;
    }

    /**
     * Generate a tree that represents a hierarchical clustering of the list of
     * RowClusters.
     * 
     * @param items
     *            The RowClusters to cluster.
     * @param dm
     *            The distance matrix for the table values.
     * @return the root node of a tree that represents a hierarchical clustering
     *         of the rows of the table.
     */
    protected TreeNode cluster(List items, DoubleMatrix2D dm) {
	Vector clusters = new Vector(items); // work list of cluster nodes
	Vector nearest = null; // list to hold nearest cluster node
	double[] da = null; // array of distances to nearest node
	Cluster rMin = null; // The first of Cluster nodes merged
	Cluster cMin = null; // The second of Cluster nodes merged
	CompositeCluster pn = null; // The parent node for the merged rMin and
				    // cMin
	int lrmin = 0; // The previous first node index, record for tracing
	int lcmin = 0; // The previous second node index, record for tracing
	// Loop through the clusters merging two each time into a parent cluster
	// Remove the two nodes from the clusters list, and add the parent
	while (clusters.size() > 1) {
	    if (Thread.interrupted()) {
		// Quit if we've been interrupted
		remaining = 0;
		return null;
	    }
	    double dist = Double.MAX_VALUE; // Initialize
	    int rmin = 0; // The index of the first node to merge.
	    int cmin = 0; // The index of the second node to merge.
	    int n = clusters.size();
	    if (nearest == null) { // nearest is null, so we need to initialize
		nearest = new Vector(n); // list to hold nearest cluster node
		nearest.setSize(n); // Initialize the size so we can just use
				    // set method;
		da = new double[n]; // array of distances to nearest node
		// Initially compare each cluster with all the others after it
		// in the list
		// record the cluster with the least distance in the neastest
		// list,
		// and put the distance in the da distance array
		for (int r = 0; r < n - 1; r++) { // loop until next to last
						  // node
		    int rcmin = 0;
		    double rdist = Double.MAX_VALUE;
		    // since pairwise distance are transitive, we only need to
		    // compare to the remaining nodes
		    for (int c = r + 1; c < n; c++) {
			if (debug > 4)
			    System.err.print("\rr = " + r + "\t c = " + c);
			double tmp = distance((Cluster) clusters.get(r),
				(Cluster) clusters.get(c), dm);
			if (Double.isNaN(tmp)) {
			    System.err.println("\tdistance NaN\t" + r + "\t"
				    + c + "\t");
			}
			if (debug > 2)
			    System.err.println("\tcluster comp \t" + r + "\t"
				    + c + "\t" + tmp);
			if (tmp < rdist) {
			    rdist = tmp;
			    rcmin = c;
			    if (debug > 1)
				System.err.println(r + "\tclusters found \t"
					+ r + "\t" + rcmin + "\t" + rdist);
			}
		    }
		    if (rcmin > 0) {
			Cluster nn = (Cluster) clusters.get(rcmin);
			nearest.set(r, nn);
			da[r] = rdist;
			if (debug > 0)
			    System.err.println(" " + r + "\tinitial" + "\t"
				    + rcmin + "\t\t" + rdist);
			if (rdist < dist) { // remember the nearest pair to
					    // merge next
			    dist = rdist;
			    rmin = r;
			    cmin = rcmin;
			}
		    } else {
			System.err.println("Cluster error " + r + " "
				+ clusters.get(r));
		    }
		}
	    } else { // Update the nearest list and the da distance array
		if (debug > 2) {
		    for (int r = 0; r < clusters.size(); r++) {
			Cluster nc = (Cluster) nearest.get(r);
			System.err.println(" > " + r + "\t"
				+ +clusters.indexOf(nc) + "\t" + da[r]);
		    }
		}
		// loop through the list and find new nearest
		for (int r = 0; r < clusters.size() - 1; r++) {
		    Cluster nc = (Cluster) nearest.get(r);
		    // if nearest is null or nearest is either of the last nodes
		    // we merged
		    if (nc == null || nc == cMin || nc == rMin) {
			int rcmin = 0;
			double rdist = Double.MAX_VALUE;
			// Search through the entire remining list looking for
			// the new nearest
			// with the least distance.
			for (int c = r + 1; c < n; c++) {
			    if (debug > 4)
				System.err.print("\rr = " + r + "\t c = " + c);
			    double tmp = distance((Cluster) clusters.get(r),
				    (Cluster) clusters.get(c), dm);
			    if (Double.isNaN(tmp)) {
				System.err.println("\tdistance NaN\t" + r
					+ "\t" + c + "\t");
			    }
			    if (tmp < rdist) { // this one is now the nearest
				rdist = tmp;
				rcmin = c;
				if (debug > 1)
				    System.err.println("\rclusters found \t"
					    + rmin + "\t" + rcmin + "\t"
					    + rdist);
			    }
			}
			if (rcmin > 0) {
			    Cluster nn = (Cluster) clusters.get(rcmin);
			    nearest.set(r, nn);
			    if (debug > 0)
				System.err.println(" "
					+ r
					+ "\t recalc "
					+ "\t"
					+ rcmin
					+ "\t"
					+ (nc == cMin ? "c " + lcmin
						: nc == rMin ? "r " + lrmin
							: "null") + "\t"
					+ rdist + "\t" + da[r]);
			    da[r] = rdist;
			} else {
			    System.err.println("Cluster error " + r + " "
				    + clusters.get(r));
			}
		    } else { // we only need to check the distance to the new
			     // parent node
			double tmp = distance((Cluster) clusters.get(r), pn, dm);
			if (Double.isNaN(tmp)) {
			    System.err.println("\tdistance NaN\t" + r + "\t"
				    + (clusters.size() - 1) + "\t");
			}
			if (tmp < da[r]) {
			    // The parent node is now the nearest
			    if (debug > 0)
				System.err.println(" " + r + "\t pn     "
					+ "\t" + (clusters.size() - 1) + "\t"
					+ clusters.indexOf(nc) + "\t" + tmp
					+ "\t" + da[r]);
			    da[r] = tmp;
			    nearest.set(r, pn);
			}
		    }
		    if (da[r] < dist) { // remember the nearest pair to merge
					// next
			rmin = r;
			cmin = clusters.indexOf(nearest.get(r));
			dist = da[r];
		    }
		}
	    }

	    // Get the list index for the second of the nodes to merge
	    cmin = clusters.indexOf(nearest.get(rmin));
	    if (cmin < 0) {
		System.err.println(clusters.size() + "\t" + rmin + "\t" + cmin
			+ "\t" + dist + "\n" + rMin + "\n" + cMin);
	    }

	    lrmin = rmin; // remember for tracing
	    lcmin = cmin; // remember for tracing
	    rMin = (Cluster) clusters.get(rmin);
	    cMin = (Cluster) nearest.get(rmin);
	    if (debug > 0)
		System.err.println(clusters.size() + "\tclusters merge \t"
			+ rmin + "\t" + cmin + "\t" + dist);

	    // merge: create parent, delete children from clusters, add parent
	    // to clusters
	    pn = new CompositeCluster(rMin, cMin);
	    pn.setSimilarity(dist);

	    // Adjust the da distance array to remove merged node entries
	    if (cmin - rmin > 1)
		System.arraycopy(da, rmin + 1, da, rmin, cmin - rmin - 1);
	    if (nearest.size() - cmin > 1)
		System.arraycopy(da, cmin + 1, da, cmin - 1, nearest.size()
			- cmin - 1);

	    // Remove merged nodes from the nearest list, then add null entry
	    // for parent
	    if (cmin < nearest.size())
		nearest.remove(cmin);
	    else
		System.err.println("nearest.remove " + cmin + " from "
			+ nearest.size());
	    if (rmin < nearest.size())
		nearest.remove(rmin);
	    nearest.add(null); // need a null entry to indicate that we need to
			       // search

	    // Remove merged nodes from the clusters list, then add parent
	    clusters.remove(rMin);
	    clusters.remove(cMin);
	    clusters.add(pn);

	    remaining = clusters.size(); // Set the number remaining to cluster

	}
	if (clusters.size() > 0) {
	    return (TreeNode) clusters.get(0);
	}
	return null;
    }

    /**
     * Get the distance between the two RowClusters by looking up the distance
     * in the distance matrix.
     * 
     * @param c1
     *            The first cluster of the pair.
     * @param c2
     *            The second cluster of the pair.
     * @param dm
     *            The distance matrix for RowClusters. return the distance
     *            between the clusters.
     */
    protected double distance(RowCluster c1, RowCluster c2, DoubleMatrix2D dm) {
	return dm.get(c1.getIndex(), c2.getIndex());
    }

    /**
     * Get the distance between the two Clusters by traversing to the
     * RowClusters. The extending classes determine how to accumulate the leaf
     * distances.
     * 
     * @param c1
     *            The first cluster of the pair.
     * @param c2
     *            The second cluster of the pair.
     * @param dm
     *            The distance matrix for RowClusters. return the distance
     *            between the clusters.
     */
    protected abstract double distance(CompositeCluster c1, Cluster c2,
	    DoubleMatrix2D dm);

    /**
     * Get the distance between the two Clusters by traversing to the
     * RowClusters.
     * 
     * @param c1
     *            The first cluster of the pair.
     * @param c2
     *            The second cluster of the pair.
     * @param dm
     *            The distance matrix for RowClusters. return the distance
     *            between the clusters.
     */
    public double distance(Cluster c1, Cluster c2, DoubleMatrix2D dm) {
	if (c1 instanceof CompositeCluster) {
	    return distance((CompositeCluster) c1, c2, dm);
	} else if (c2 instanceof CompositeCluster) {
	    return distance((CompositeCluster) c2, c1, dm);
	} else {
	    return distance((RowCluster) c2, (RowCluster) c1, dm);
	}
    }

}
