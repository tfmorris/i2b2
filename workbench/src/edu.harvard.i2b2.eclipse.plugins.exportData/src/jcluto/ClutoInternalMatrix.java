/*
 * @(#) $RCSfile: ClutoInternalMatrix.java,v $ $Revision: 1.3 $ $Date: 2008/11/19 16:20:05 $ $Name: RELEASE_1_3_1_0001b $
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
 * ClutoInternalMatrix is a ClutoMatrix that has normalized values, and that may
 * have rows and columns removed. The methods getRowIndexMap() and
 * getColIndexMap() return arrays that give the corresponding indexes to the
 * rows and columns of the original source matrix.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/19 16:20:05 $ $Name
 */
public interface ClutoInternalMatrix extends ClutoMatrix {

    /**
     * Return the source matrix from which this matrix was derived.
     * 
     * @return the source matrix from which this matrix was derived.
     */
    public ClutoMatrix getSourceMatrix();

    /**
     * Return an index that gives the corresponding row index in the source
     * matrix for each row in this matrix.
     * 
     * @return the row mapping index.
     */
    public int[] getRowIndexMap();

    /**
     * Return an index that gives the corresponding column index in the source
     * matrix for each column in this matrix.
     * 
     * @return the column mapping index.
     */
    public int[] getColIndexMap();

    /**
     * Return the column index in the source matrix for the given columnIndex in
     * this matrix.
     * 
     * @param columnIndex
     *            the column index in this matrix.
     * @return the corresponding column index in the source matrix.
     */
    public int getSourceColumnIndex(int columnIndex);

    /**
     * Return the row index in the source matrix for the given rowIndex in this
     * matrix.
     * 
     * @param rowIndex
     *            the row index in this matrix.
     * @return the corresponding row index in the source matrix.
     */
    public int getSourceRowIndex(int rowIndex);

}
