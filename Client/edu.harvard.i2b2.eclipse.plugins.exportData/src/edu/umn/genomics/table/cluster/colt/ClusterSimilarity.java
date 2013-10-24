/*
 * @(#) $RCSfile: ClusterSimilarity.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
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

import edu.umn.genomics.table.cluster.*;
import cern.colt.matrix.*;

/**
 * AbstractSimilarity.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface ClusterSimilarity {
    /**
     * Get the distance between the two Clusters by traversing to the
     * RowClusters. The implementing classes determine how to accumulate the
     * leaf distances.
     * 
     * @param c1
     *            The first cluster of the pair.
     * @param c2
     *            The second cluster of the pair.
     * @param dm
     *            The distance matrix for RowClusters. return the distance
     *            between the clusters.
     */
    public double distance(Cluster c1, Cluster c2, DoubleMatrix2D dm);
}
