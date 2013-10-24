/*
 * @(#) $RCSfile: CompleteSimilarity.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
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
import edu.umn.genomics.table.cluster.*;
import cern.colt.matrix.*;

/**
 * CompleteSimilarity computes the greatest distance between any pairing of data
 * points from one cluster to another cluster.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class CompleteSimilarity extends AbstractSimilarity {
    /**
     * Get the distance between the two Clusters by traversing to the
     * RowClusters. Return the greatest of the leaf distances.
     * 
     * @param c1
     *            The first cluster of the pair.
     * @param c2
     *            The second cluster of the pair.
     * @param dm
     *            The distance matrix for RowClusters. return the distance
     *            between the clusters.
     */

    @Override
    public double distance(CompositeCluster c1, Cluster c2, DoubleMatrix2D dm) {
	double dist = 0.;
	for (Enumeration e1 = c1.depthFirstEnumeration(); e1.hasMoreElements();) {
	    double d;
	    Cluster n1 = (Cluster) e1.nextElement();
	    if (n1 != null && n1.isLeaf()) {
		// System.err.println(" n1  = " + n1);
		if (c2 instanceof CompositeCluster) {
		    for (Enumeration e2 = c2.depthFirstEnumeration(); e2
			    .hasMoreElements();) {
			Cluster n2 = (Cluster) e2.nextElement();
			if (n2 != null && n2.isLeaf()) {
			    // System.err.println(" n1  = " + n1 + "\t n2 = " +
			    // n2);
			    d = distance((RowCluster) n1, (RowCluster) n2, dm);
			    if (d > dist) {
				dist = d;
			    }
			}
		    }
		} else if (c2 != null) {
		    d = distance((RowCluster) n1, (RowCluster) c2, dm);
		    if (d > dist) {
			dist = d;
		    }
		}
	    }
	}
	return dist;
    }
}
