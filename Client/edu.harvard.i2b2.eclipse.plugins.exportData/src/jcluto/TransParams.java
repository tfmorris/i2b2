/*
 * @(#) $RCSfile: TransParams.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
 * Provides the options selected for matrix transformations and 
 * missing value estimation.
 * the function supplies all the required parameters for 
 * executing a estimating method
 *
 * @author       Shulan Tian
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 * @since        1.0
 */

public interface TransParams {
  /** Leave the missing values in the ClutoMatrix*/
  public final static int Do_Nothing = 0;
  /** Replace Missing values using the K nearest neighbors method*/
  public final static int KNN = 1;
  /** Replace Missing values using the row average method */
  public final static int Row_Average = 2;
  /** Replace Missing values using the column average method */
  public final static int Column_Average = 3;
  /** Replace Missing values with zero */
  public final static int Input_With_Zero = 4;
  
  /**
   * Set the matrix transformation values to be the same as the given params.
   * @param params The matrix transformation options
   */
  public void setValues(TransParams params);

  /**
   * Get the missing value estimating method.
   * @return the integer associated with method
   * @see #Do_Nothing
   * @see #KNN
   * @see #Row_Average
   * @see #Column_Average
   * @see #Input_With_Zero
   */
  public int getEstMethod();

  /**
   * Get the number of neighbors used when estimating missing values 
   * with the K nearest neighbors.
   * @return number of neighbors
   */
  public int getNumNeighbors();
 
  /**
   * Return whether mean centering should be performed on the matrix values.
   * @return whether the mean centering option is selected.
   */
  public boolean getMeanCenter();

  /**
   * Return whether median centering should be performed on the matrix values.
   * @return whether the median centering option is selected.
   */
  public boolean getMedianCenter();

  /**
   * Return whether a log transformation should be performed on the matrix values.
   * get log transformation option
   * @return whether the log transformation option is selected
   */
  public boolean getLogTrans();

  /**
   * get debugging option 
   * @return debugging option, 1 if debug, else 0
   */
  public int getDbgLvl();
}
