/*
 * @(#) $RCSfile: ClusterFeatures.java,v $ $Revision: 1.3 $ $Date: 2008/11/18 17:57:56 $ $Name: RELEASE_1_3_1_0001b $
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
 * discriminate each cluster from the rest of the objects. The set of
 * descriptive features is determined by selecting the columns that contribute
 * the most to the average similarity between the objects of each cluster. On
 * the other hand, the set of discriminating features is determined by selecting
 * the columns that are more prevalent in the cluster compared to the rest of
 * the objects. In general, there will be a large overlap between the
 * descriptive and discriminating features. However, in some cases there may be
 * certain differences, especially when the column model = none. This analysis
 * can only be performed when the similarity between objects is computed using
 * the cosine or correlation coefficient.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/18 17:57:56 $ $Name: RELEASE_1_3_1_0001b $
 */
public interface ClusterFeatures {
    /**
     * Return the ClutoSolution that produced these features.
     * 
     * @return the ClutoSolution that produced these features.
     * @see ClutoSolution
     */
    public ClutoSolution getClutoSolution();

    /**
     * Return the number of clusters.
     * 
     * @return the number of clusters
     */
    public int getClusterCount();

    /**
     * The number of features for each cluster.
     * 
     * @return The number of features for each cluster.
     */
    public int getFeatureCount();

    /**
     * Get the id (column index) of the feature (0 to getFeatureCount()-1, with
     * 0 being the most descriptive id) for the given cluster.
     * 
     * @return the id for the given descriptive feature of the cluster.
     */
    public int getInternalID(int cluster, int feature);

    /**
     * Get the id (column index) of the feature (0 to getFeatureCount()-1, with
     * 0 being the most discriminating id) for the given cluster.
     * 
     * @return the id for the given discriminating feature of the cluster.
     */
    public int getExternalID(int cluster, int feature);

    /**
     * Get the value of the feature (0 to getFeatureCount()-1, with 0 being the
     * most descriptive id) for the given cluster.
     * 
     * @return the value for the given descriptive feature of the cluster.
     */
    public float getInternalWgt(int cluster, int feature);

    /**
     * Get the value of the feature (0 to getFeatureCount()-1, with 0 being the
     * most discriminating id) for the given cluster.
     * 
     * @return the value for the given discriminating feature of the cluster.
     */
    public float getExternalWgt(int cluster, int feature);
}
