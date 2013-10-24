/*
 * @(#) $RCSfile: ClutoColumnMapMatrix.java,v $ $Revision: 1.2 $ $Date: 2008/11/10 15:48:49 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.Vector;
import edu.umn.genomics.table.*;
import jcluto.ClutoTableMatrix;

/**
 * ClutoTableMatrix encapsulates a portion of a TableModel as a ClutoMatrix, and
 * also presents the ClutoMatrix as a TableModel.
 * 
 * @author J Johnson
 * @author Shlan Tian
 * @version $Revision: 1.2 $ $Date: 2008/11/10 15:48:49 $ $Name: RELEASE_1_3_1_0001b $
 * @see javax.swing.table.AbstractTableModel
 * @see jcluto.ClutoMatrix
 */
public class ClutoColumnMapMatrix extends ClutoTableMatrix implements
	Serializable {
    ColumnMap[] cmap = null;
    /**
     * Update points whenever a Column finishing getting mapped.
     */
    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    if (e.getMapState() == CellMap.INVALID) {
	    } else if (!e.mappingInProgress()) {
		makeMatrix(cmap);
	    }
	}
    };

    /**
     * Create a ClutoMatrix from the table columns represented by the
     * ColumnMaps. The ColumnMap will provide a number representation for each
     * value in the TableModel.
     * 
     * @param cmap
     *            an array of ColumnMap from a TableModel that should comprise
     *            the ClutoMarix.
     */
    public ClutoColumnMapMatrix(ColumnMap[] cmap) {
	this.cmap = cmap;
	for (int c = 0; c < cmap.length; c++) {
	    cmap[c].addCellMapListener(cml);
	}
	makeMatrix(cmap);
    }

    public boolean isMapped() {
	for (int c = 0; c < cmap.length; c++) {
	    if (cmap[c].getState() != CellMap.MAPPED) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Create a ClutoMatrix from the table columns represented by the
     * ColumnMaps. The ColumnMap will provide a number representation for each
     * value in the TableModel.
     * 
     * @param cmap
     *            an array of ColumnMaps from which to build the ClutoMatrix
     */
    private synchronized void makeMatrix(ColumnMap[] cmap) {
	if (cmap == null || cmap.length < 1)
	    return;
	// only proceed if all of the columns are mapped
	for (int c = 0; c < cmap.length; c++) {
	    if (cmap[c].getState() != CellMap.MAPPED) {
		return;
	    }
	}
	int[] rowptr = null;
	int[] rowind = null;
	float[] rowval = null;
	int ncol = cmap.length;
	int nrow = cmap[0].getCount();
	colNames = new Vector(ncol);
	colNames.setSize(ncol);
	int nullCnt = 0;
	// Check if there are any NULL values
	for (int c = 0; c < cmap.length; c++) {
	    nullCnt += cmap[c].getNullCount();
	    colNames.set(c, cmap[c].getName());
	}

	// If null values, create sparse matrix
	if (nullCnt > 0) {
	    // Create rowval array
	    rowval = new float[nrow * ncol - nullCnt];
	    rowptr = new int[nrow + 1];
	    rowind = new int[nrow * ncol - nullCnt];
	    int rii = 0;
	    int rvi = 0;
	    for (int r = 0; r < nrow; r++) {
		rowptr[r] = rii;
		for (int c = 0; c < ncol; c++) {
		    float val = (float) cmap[c].getMapValue(r);
		    if (!Float.isNaN(val)) {
			rowind[rii++] = c;
			rowval[rvi++] = val;
		    }
		}
	    }
	    rowptr[nrow] = rii;
	    // else dense matrix
	} else {
	    // Create rowval array
	    rowval = new float[nrow * ncol];
	    int rvi = 0;
	    for (int r = 0; r < nrow; r++) {
		for (int c = 0; c < ncol; c++) {
		    rowval[rvi++] = (float) cmap[c].getMapValue(r);
		}
	    }
	}
	rowPtr = rowptr;
	rowInd = rowind;
	rowVal = rowval;
	rowCnt = nrow;
	colCnt = ncol;
    }
}
