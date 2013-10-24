/*
 * @(#) $RCSfile: ValidateClutoParams.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
 * Validates a given set of <b>CLUTO</b> parameters to prevent the <b>CLUTO</b> 
 * native library from exiting on incompatible parameter settings.
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 */
public class ValidateClutoParams {
  /**
   * Validates a given set of <b>CLUTO</b> parameters.
   * @param cMat The matrix.
   * @param tParam The matrix transformation parameters.
   * @param cParam The Cluto clustering parameters.
   * @return true if the parameters are valid values, else false.
   */
  public static boolean isValid(ClutoMatrix cMat, TransParams tParam, ClutoParams cParam) 
    throws IllegalArgumentException,NullPointerException {
    String err = null;
    // Check if matrix is given
    if (cMat == null || cParam == null) {
      throw new NullPointerException();
    }
    // Check that matrix has data
    if (cMat.getValueCount() < 1) {
      throw new IllegalArgumentException(
        "Empty Matrix");
    }
    // Check that number of clusters < matrix row count
    if (cParam.getNumClusters() >= cMat.getRowCount()) {
      throw new IllegalArgumentException(
        "Number of Clusters must be less than the number of rows in the matrix");
    }

    // Check that Pearson Correlation has more than 2 columns
    if (cParam.getSimFunc() == JClutoWrapper.CLUTO_SIM_CORRCOEF && 
        cMat.getColumnCount() < 3) {
      throw new IllegalArgumentException(
        "Similarity based on correlation coefficient requires more than two dimensions");
    }

    // Check that AggloFrom number of clusters < matrix row count
    switch(cParam.getClusterMethod()) {
    case ClutoParams.VP_ClusterDirect:
    case ClutoParams.VP_ClusterRB:
      // Check that number of clusters <= matrix row count
      if (cParam.getAggloFrom() >= cMat.getRowCount()) {
        throw new IllegalArgumentException(
          "Number of Clusters must be less than the number of rows in the matrix");
      }
    }

    boolean isSparse = cMat.getMissingValueCount() > 0 && 
                         (tParam == null || tParam.getEstMethod() == TransParams.Do_Nothing);
    switch(cParam.getClusterMethod()) {
    case ClutoParams.VA_Cluster:
    case ClutoParams.VA_ClusterBiased:
    case ClutoParams.VP_ClusterDirect:
    case ClutoParams.VP_ClusterRB:
    case ClutoParams.VP_ClusterRBTree:
    default:
    }
    return true;
  }
}
