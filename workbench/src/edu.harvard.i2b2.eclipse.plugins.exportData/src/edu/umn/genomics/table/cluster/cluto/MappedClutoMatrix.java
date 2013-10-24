/*
 * @(#) $RCSfile: MappedClutoMatrix.java,v $ $Revision: 1.3 $ $Date: 2008/12/02 15:55:53 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster.cluto;

import java.io.Serializable;
import jcluto.*;
import edu.umn.genomics.table.*;

/**
 * MappedClutoMatrix encapsulates a portion of a TableModel as a ClutoMatrix,
 * and also presents the ClutoMatrix as a TableModel.
 * 
 * @author J Johnson
 * @author Shlan Tian
 * @version $Revision: 1.3 $ $Date: 2008/12/02 15:55:53 $ $Name: RELEASE_1_3_1_0001b $
 * @see javax.swing.table.AbstractTableModel
 * @see ClutoMatrix
 */
public class MappedClutoMatrix extends ClutoTableMatrix implements
	TableColumnMap, Cloneable, Serializable {
    ClutoColumnMap[] colMap = null;;

    /**
     * Construct a cluto matrix. The arguments rowptr and rowind are null for a
     * dense matrix.
     * 
     * @param nrows
     *            The number of rows in the matrix.
     * @param ncols
     *            The number of columns in the matrix.
     * @param rowptr
     *            The row offsets into the rowind and rowval arrays.
     * @param rowind
     *            The column indices for the values.
     * @param rowval
     *            The values of the matrix
     * @see ClutoMatrix
     */
    public MappedClutoMatrix(int nrows, int ncols, int[] rowptr, int[] rowind,
	    float[] rowval) {
	super(nrows, ncols, rowptr, rowind, rowval);
    }

    /**
     * Construct an MappedClutoMatrix from the ClutoMatrix.
     */
    public MappedClutoMatrix(ClutoMatrix matrix) {
	super(matrix);
    }

    /**
     * Construct an empty cluto matrix for derived classes.
     */
    protected MappedClutoMatrix() {
	super();
    }

    /**
     * Return a ColumnMap for the column in the TableModel at columnIndex.
     * 
     * @param columnIndex
     *            the index of the TableModel column.
     * @return a ColumnMap for the TableModel column at columnIndex.
     */
    public ColumnMap getColumnMap(int columnIndex) {
	if (colMap == null) {
	    colMap = new ClutoColumnMap[getColumnCount()];
	}
	if (columnIndex >= 0 && columnIndex < colMap.length) {
	    if (colMap[columnIndex] == null) {
		colMap[columnIndex] = new ClutoColumnMap(this, columnIndex);
	    }
	    return colMap[columnIndex];
	}
	return null;
    }
}
