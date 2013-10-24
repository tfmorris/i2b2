/*
 * @(#) $RCSfile: ClutoTableMatrix.java,v $ $Revision: 1.3 $ $Date: 2008/11/24 17:41:20 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 * ClutoTableMatrix encapsulates a portion of a TableModel as a ClutoMatrix, and
 * also presents the ClutoMatrix as a TableModel.
 * 
 * @author J Johnson
 * @author Shlan Tian
 * @version $Revision: 1.3 $ $Date: 2008/11/24 17:41:20 $ $Name: RELEASE_1_3_1_0001b $
 * @see javax.swing.table.AbstractTableModel
 * @see ClutoMatrix
 */
public class ClutoTableMatrix extends AbstractTableModel implements
	MutableClutoMatrix, Cloneable, Serializable {
    /** whether this matrix represents a the edge weights of a connected graph. */
    private boolean isGraph = false;
    /** The number of rows in the matrix */
    protected int rowCnt = 0;
    /** The number of columns in the matrix */
    protected int colCnt = 0;
    /**
     * If this is a dense matrix, this will be null, else if this is a sparse
     * matrix, this array has the starting and ending index into the
     * {@link #rowInd} array. This array will be of length: {@link #rowCnt} + 1.
     * The indices to values in the rowVal array for row <B>i</B> will be
     * rowInd[rowPtr[<B>i</B>]] to rowInd[rowPtr[<B>i</B>+1]].
     */
    protected int[] rowPtr = null;
    /**
     * If this is a dense matrix, this will be null, else if this is a sparse
     * matrix, this array contains indices into the {@link #rowVal} array. This
     * array will be of length: the value in {@link #rowPtr}[{@link #rowCnt}+1].
     */
    protected int[] rowInd = null;
    /**
     * The values for cells in the matrix. If this is a dense matrix, the length
     * will be {@link #rowCnt} {@link #colCnt}, else if this is a sparse matrix,
     * the length will be the same as {@link #rowInd}.
     */
    protected float[] rowVal = null;
    /** Names of the columns in the matrix. */
    protected Vector colNames = null;

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
    public ClutoTableMatrix(int nrows, int ncols, int[] rowptr, int[] rowind,
	    float[] rowval) {
	// Check that these values are consistent
	// 
	// throw IllegalArgumentException
	//
	rowCnt = nrows;
	colCnt = ncols;
	rowPtr = rowptr;
	rowInd = rowind;
	rowVal = rowval;
	colNames = new Vector(colCnt);
	colNames.setSize(colCnt);
    }

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
     * @param isGraph
     *            Whether this matrix represents a the edge weights of a
     *            connected graph.
     * @see ClutoMatrix
     */
    public ClutoTableMatrix(int nrows, int ncols, int[] rowptr, int[] rowind,
	    float[] rowval, boolean isGraph) {
	// Check that these values are consistent
	// 
	// throw IllegalArgumentException
	//
	rowCnt = nrows;
	colCnt = ncols;
	rowPtr = rowptr;
	rowInd = rowind;
	rowVal = rowval;
	colNames = new Vector(colCnt);
	colNames.setSize(colCnt);
	setIsGraph(isGraph);
    }

    /**
     * Construct an ClutoTableMatrix from the ClutoMatrix. This will reference
     * the array data in the given matrix, not copy it.
     */
    public ClutoTableMatrix(ClutoMatrix matrix) {
	rowCnt = matrix.getRowCount();
	colCnt = matrix.getColumnCount();
	rowPtr = matrix.getRowPtr();
	rowInd = matrix.getRowInd();
	rowVal = matrix.getRowVal();
	isGraph = matrix.getIsGraph();
	if (matrix instanceof ClutoTableMatrix) {
	    for (int c = 0; c < colCnt; c++) {
		setColumnName(c, ((ClutoTableMatrix) matrix).getColumnName(c));
	    }
	}
    }

    /**
     * Construct an empty cluto matrix for derived classes.
     */
    protected ClutoTableMatrix() {
    }

    /**
     * Returns a clone of this Matrix that is a deep copy, that is, the cloned
     * copy will have references to clones of the orginal internal data, and any
     * changes to the cloned matrix will not affect the original.
     * 
     * @return a clone of this matrix.
     */
    @Override
    public Object clone() {
	// I'm assuming that Listeners should NOT be copied to the new matrix.
	ClutoTableMatrix cMat = new ClutoTableMatrix();
	cMat.isGraph = isGraph;
	cMat.rowCnt = rowCnt;
	cMat.colCnt = colCnt;
	if (rowPtr != null)
	    cMat.rowPtr = rowPtr.clone();
	if (rowInd != null)
	    cMat.rowInd = rowInd.clone();
	if (rowVal != null)
	    cMat.rowVal = rowVal.clone();
	if (colNames != null)
	    cMat.colNames = (Vector) colNames.clone();
	return cMat;
    }

    public boolean getIsGraph() {
	return isGraph;
    }

    public void setIsGraph(boolean isGraph) {
	// Should verify dense matrix is square
	this.isGraph = isGraph;
    }

    /**
     * If this is a sparse matrix, this will return an array that provides the
     * starting and ending index into the arrays returned by getRowInd() and
     * getRowVal() for each row in the matrix. This will return null if this is
     * a dense array.
     * 
     * @return null if this is a dense array, else the indices into the
     *         getRowInd() and getRowVal() arrays.
     * 
     *         NOTE: This is a reference to an internal array; the elements of
     *         this array should not be changed.
     * 
     * @see ClutoMatrix
     */
    public int[] getRowPtr() {
	return rowPtr;
    }

    /**
     * If this is a sparse matrix, this will return an array that provides the
     * column index for this value in the matrix. This will return null if this
     * is a dense array.
     * 
     * @return null if this is a dense array, else the column indices of the
     *         values in the getRowVal() array.
     * 
     *         NOTE: This is a reference to an internal array; the elements of
     *         this array should not be changed.
     * 
     * @see ClutoMatrix
     */
    public int[] getRowInd() {
	return rowInd;
    }

    /**
     * Return the values of the cells as row-ordered array. If this is a dense
     * Matrix this array will be getRowCount() * getColumnCount() in length. If
     * this is a sparse matrix, the index of a cell is determined using the
     * arrays return by getRowPtr() and getRowInd().
     * 
     * NOTE: This is a reference to an internal array; the elements of this
     * array should not be changed.
     * 
     * @return The values of the cells as row-ordered array.
     * @see ClutoMatrix
     */
    public float[] getRowVal() {
	return rowVal;
    }

    /**
     * Get the value of the given cell in the matrix.
     * 
     * @param rowIndex
     *            The row index of the cell in the matrix.
     * @param columnIndex
     *            The column index of the cell in the matrix.
     * @return The value of the given cell in the matrix, or Float.NaN if this
     *         cell is missing in a sparse matrix.
     */
    public float getValue(int rowIndex, int columnIndex) {
	if (rowIndex >= rowCnt || columnIndex >= colCnt) {
	    return Float.NaN;
	}
	if (rowPtr != null && rowInd != null) {
	    for (int i = rowPtr[rowIndex]; i < rowPtr[rowIndex + 1]; i++) {
		if (rowInd[i] == columnIndex) {
		    return rowVal[i];
		}
	    }
	    return Float.NaN;
	} else {
	    return rowVal[rowIndex * colCnt + columnIndex];
	}
    }

    /**
     * Return the values for the given row of the matrix.
     * 
     * @param rowIndex
     *            the row index of the matrix.
     * @return The values in the given row of the matrix.
     */
    public float[] getRow(int rowIndex) {
	float[] entry = new float[colCnt];
	for (int i = 0; i < colCnt; i++)
	    entry[i] = getValue(rowIndex, i);
	return entry;
    }

    /**
     * Return whether every column in the given row of the matrix has a value.
     * 
     * @param rowIndex
     *            the row index of the matrix.
     * @return true if every column in the given row of the matrix has a value,
     *         else false.
     */
    public boolean isRowComplete(int rowIndex) {
	for (int i = 0; i < colCnt; i++) {
	    if (Float.isNaN(getValue(rowIndex, i))) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Return a copy of the values of all cells as row-ordered array. Changing
     * the value of the returned array will not alter this matrix. This is a
     * dense Matrix array * getRowCount() * getColumnCount() in length. To
     * access the value at row 3, column 4: getAllValues()[3 * getColumnCount()
     * + 4]. Missing values of a sparse matrix are assigned the value Float.NaN.
     * 
     * @return The values of the cells as row-ordered array.
     * @see ClutoMatrix
     */
    public float[] getAllValues() {
	float[] rv = new float[rowCnt * colCnt];
	if (getMissingValueCount() == 0) {
	    System.arraycopy(rowVal, 0, rv, 0, rowVal.length);
	} else {
	    for (int ri = 0, ii = 0; ri < rowCnt; ri++) {
		for (int ci = 0; ci < colCnt; ci++) {
		    rv[ii++] = getValue(ri, ci);
		}
	    }
	}
	return rv;
    }

    /**
     * Set the value of a cell in the matrix. This may change the matrix from a
     * sparse to dense matrix or vice versa.
     * 
     * @param value
     *            The value for the matrix cell.
     * @param rowIndex
     *            the row index of the matrix cell.
     * @param columnIndex
     *            the column index of the matrix cell.
     */
    public synchronized void setValue(float value, int rowIndex, int columnIndex) {
	if (rowIndex >= rowCnt || columnIndex >= colCnt) {
	    return;
	}
	if (Float.isNaN(value) && !Float.isNaN(getValue(rowIndex, columnIndex))) {
	    // this will now be a sparse matrix
	    int ri = rowIndex * colCnt + columnIndex; // this is for a dense
						      // matrix
	    if (rowPtr != null && rowInd != null) { // already a sparse matrix
		// find the array offset in the sparse matrix
		for (ri = rowPtr[rowIndex]; ri < rowPtr[rowIndex + 1]
			&& rowInd[ri] < columnIndex; ri++)
		    ;
		// adjust row offsets in rowPtr
		for (int i = rowIndex + 1; i < rowPtr.length; i++) {
		    rowPtr[i]--;
		}
		int[] rI = rowInd;
		rowInd = new int[rI.length - 1];
		System.arraycopy(rI, 0, rowInd, 0, ri); // copy the start of the
							// array
		System.arraycopy(rI, ri + 1, rowInd, ri, rI.length - ri - 1); // copy
									      // the
									      // end
									      // of
									      // the
									      // array
	    } else {
		rowPtr = new int[rowCnt + 1];
		for (int i = 0; i < rowPtr.length; i++) {
		    rowPtr[i] = i * colCnt + (i <= rowIndex ? 0 : -1);
		}
		rowInd = new int[rowCnt * colCnt - 1];
		for (int row = 0, ii = 0; row < rowCnt; row++) {
		    for (int col = 0; col < rowCnt; col++) {
			if (row == rowIndex && col == columnIndex)
			    continue;
			rowInd[ii++] = col;
		    }
		}
	    }
	    float[] rV = rowVal;
	    rowVal = new float[rV.length - 1];
	    System.arraycopy(rV, 0, rowVal, 0, ri); // copy the start of the
						    // array
	    System.arraycopy(rV, ri + 1, rowVal, ri, rV.length - ri - 1); // copy
									  // the
									  // end
									  // of
									  // the
									  // array

	} else {
	    if (rowPtr != null && rowInd != null) { // sparse matrix
		int ri = -1;
		// find the array offset in the sparse matrix
		for (ri = rowPtr[rowIndex]; ri < rowPtr[rowIndex + 1]
			&& rowInd[ri] < columnIndex; ri++)
		    ;
		if (rowInd[ri] != columnIndex) { // cell not currently in
						 // matrix, adjust arrays
		    int nnz = rowVal.length + 1;
		    float[] rV = rowVal;
		    rowVal = new float[nnz];
		    System.arraycopy(rV, 0, rowVal, 0, ri); // copy the start of
							    // the array
		    System.arraycopy(rV, ri, rowVal, ri + 1, rV.length - ri); // copy
									      // the
									      // end
									      // of
									      // the
									      // array
		    // if this is now a dense matrix, set rowPtr and rowInd to
		    // null
		    if (nnz == rowCnt * colCnt) {
			rowPtr = null;
			rowInd = null;
		    } else {
			// add columnIndex to rowInd
			int[] rI = rowInd;
			rowInd = new int[nnz];
			System.arraycopy(rI, 0, rowInd, 0, ri); // copy the
								// start of the
								// array
			System
				.arraycopy(rI, ri, rowInd, ri + 1, rI.length
					- ri); // copy the end of the array
			rowInd[ri] = columnIndex;
			// adjust row offsets in rowPtr
			for (int i = rowIndex + 1; i < rowPtr.length; i++) {
			    rowPtr[i]++;
			}
		    }
		}
		rowVal[ri] = value;
	    } else {
		rowVal[rowIndex * colCnt + columnIndex] = value;
	    }
	}
	fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Returns the number of matrix cells that have a value, that is the numbers
     * of values in a sparse matrix.
     * 
     * @return The number of cells in the matrix that have a value.
     */
    public int getValueCount() {
	if (rowPtr != null) {
	    return rowPtr[rowPtr.length - 1];
	}
	return rowCnt * colCnt;
    }

    /**
     * Returns the number of cells missing from the matrix.
     * 
     * @return The number of cells missing in the matrix.
     */
    public int getMissingValueCount() {
	return rowCnt * colCnt - getValueCount();
    }

    /**
     * Returns the number of rows in the matrix.
     * 
     * @return the number of rows in the matrix
     * @see #getColumnCount
     */
    public int getRowCount() {
	return rowCnt;
    }

    /**
     * Returns the number of columns in the matrix.
     * 
     * @return the number of columns in the matrix
     * @see #getRowCount
     */
    public int getColumnCount() {
	return colCnt;
    }

    /**
     * Returns the value for the matrix cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     * 
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (rowIndex < rowCnt && columnIndex < colCnt && rowIndex >= 0
		&& columnIndex >= 0) {
	    return new Float(getValue(rowIndex, columnIndex));
	}
	return null;
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and
     * <code>rowIndex</code> to <code>aValue</code>.
     * 
     * @param value
     *            the new value
     * @param rowIndex
     *            the row whose value is to be changed
     * @param columnIndex
     *            the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
	if (value == null) {
	    setValue(Float.NaN, rowIndex, columnIndex);
	} else if (value instanceof Number) {
	    setValue(((Number) value).floatValue(), rowIndex, columnIndex);
	} else if (value instanceof String) {
	    try {
		setValue(Float.parseFloat((String) value), rowIndex,
			columnIndex);
	    } catch (Exception ex) {
	    }
	}
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code> is editable. Otherwise, <code>setValueAt</code>
     * on the cell will not change the value of that cell.
     * 
     * @param rowIndex
     *            the row whose value to be queried
     * @param columnIndex
     *            the column whose value to be queried
     * @return true if the cell is editable
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return true;
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column, which is java/lang.Float for a matrix.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < colCnt)
	    return java.lang.Float.class;
	return java.lang.Float.class;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>. This is used
     * to initialize the table's column header name. Note: this name does not
     * need to be unique; two columns in a table can have the same name.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the name of the column
     */
    @Override
    public String getColumnName(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < colCnt && colNames != null
		&& columnIndex < colNames.size()) {
	    String name = (String) colNames.get(columnIndex);
	    if (name != null) {
		return name;
	    }
	}
	String name = "";
	for (int c = columnIndex % 26, n = (columnIndex - c) / 26;; c = n % 26, n = (n - c) / 26) {
	    if (columnIndex >= 26 && n <= 0) {
		c--;
	    }
	    name = Character.toString((char) ('A' + c)) + name;
	    if (n <= 0) {
		break;
	    }
	}
	return name;
    }

    /**
     * Sets the name of the column at <code>columnIndex</code>. This is used to
     * initialize the table's column header name. Note: this name does not need
     * to be unique; two columns in a table can have the same name.
     * 
     * @param columnIndex
     *            the index of the column
     * @param name
     *            the name of the column
     */
    public void setColumnName(int columnIndex, String name) {
	if (columnIndex >= 0 && columnIndex < colCnt) {
	    if (colNames == null) {
		colNames = new Vector(colCnt);
	    }
	    if (colNames.size() != colCnt) {
		colNames.setSize(colCnt);
	    }
	    colNames.set(columnIndex, name);
	}
    }
}
