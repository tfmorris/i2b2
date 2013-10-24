/*
 * @(#) $RCSfile: ClutoMatrix.java,v $ $Revision: 1.3 $ $Date: 2008/11/20 17:35:19 $ $Name: RELEASE_1_3_1_0001b $
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
 * ClutoMatrix represents a matrix in a form that can be used in calls to the
 * <b>CLUTO</b> clustering package.
 * 
 * <p>
 * Excerps From the <b>CLUTO</b> manual:
 * <p>
 * Most of the routines in <b>CLUTO</b>'s library take, as input, the objects to
 * be clustered in the form of a matrix. For some routines this matrix
 * corresponds to the feature-space representation of the objects, that is, the
 * rows are the objects and the columns are the features (just like the
 * matrix-file for the <b>CLUTO</b> vcluster program). Whereas for some other
 * routines, this matrix corresponds to the adjacency matrix of the similarity
 * graph between the objects, that is, both the rows and the columns of the
 * matrix correspond to the vertices in the graph (just like the graph-file for
 * the <b>CLUTO</b> scluster program). Even though these two type of matrices
 * represent entirely different information, they are provided to <b>CLUTO</b>'s
 * routines using the same data structure. This is primarily because the
 * adjacency matrix of a graph is, after all, a matrix which just happens to
 * have the same number of rows and columns. <b>CLUTO</b>'s routines support
 * both sparse and dense matrices using the same set of data structures.
 * <p>
 * <b>Dense Matrix Data Structure.</b> <br>
 * A dense matrix is supplied to <b>CLUTO</b>'s routines by using only the
 * rowval array and setting the rowptr and rowind arrays to NULL. In fact,
 * <b>CLUTO</b>'s routines determine the input matrix format by checking to see
 * if rowptr is NULL or not. A dense matrix with n rows and m columns is passed
 * to <b>CLUTO</b> by supplying in rowval the n ×m values of the matrix, in
 * row-major order format. That is, the m values of the ith row (where i takes
 * values from 0 ...n 1) is stored starting at location rowval[i*m] and ending
 * at (but not including) rowval[(i+1)*m].
 * 
 * <p>
 * <b>Sparse Matrix and Graph Data Structure.</b> <br>
 * A sparse matrix is supplied to <b>CLUTO</b>'s routines using a row-based
 * compressed storage format (CSR). The CSR format is a widely used scheme for
 * storing sparse matrices. In this format a matrix with n rows, m columns, and
 * nnz non-zero entries is represented using three arrays that are called
 * rowptr, rowind, and rowval. The array rowptr is of size n +1 whereas the
 * arrays rowind and rowval are of size nnz. The array rowind stores the
 * column-indices of the non-zero entries in the matrix, and the array rowval
 * stores their corresponding values. In particular, the array rowind stores the
 * column-indices of the first row, followed by the column-indices of the second
 * row, and so on. Similarly, the array rowval stores the corresponding values
 * of the non-zero entries of the first row, followed by the corresponding
 * values of the non-zero entries of the second row, and so on. The array rowptr
 * is used to determine where the storage of a row starts and ends in the
 * arrays, rowind and rowval. In particular, the column-indices of the ith row
 * are stored starting at rowind[rowptr[i]] and ending at (but not including)
 * rowind[rowptr[i+1]]. Similarly, the values of the non-zero entries of the ith
 * row are stored starting at rowval[rowptr[i]] and ending at (but not
 * including) rowval[rowptr[i+1]]. Also note that the number of non-zero entries
 * of the ith row is simply rowptr[i+1]-rowptr[i].
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/20 17:35:19 $ $Name: RELEASE_1_3_1_0001b $
 */
public interface ClutoMatrix {

    /**
     * Return whether this matrix represents a the edge weights of a connected
     * graph.
     * 
     * @return whether this matrix represents a graph
     */
    public boolean getIsGraph();

    /**
     * Returns the number of rows in the matrix.
     * 
     * @return The number of rows in the matrix.
     */
    public int getRowCount();

    /**
     * Returns the number of columns in the matrix.
     * 
     * @return The number of columns in the matrix.
     */
    public int getColumnCount();

    /**
     * Returns the number of matrix cells that have a value, that is, the
     * numbers of values in a sparse matrix.
     * 
     * @return The number of cells in the matrix that have a value.
     */
    public int getValueCount();

    /**
     * Returns the number of cells missing from the matrix, if this is dense
     * array this value will be zero, and getRowPtr() and getRowInd() will
     * return null.
     * 
     * @return The number of cells missing in the matrix.
     */
    public int getMissingValueCount();

    /**
     * If this is a sparse matrix, this will return an array that provides the
     * starting and ending index into the arrays returned by getRowInd() and
     * getRowVal() for each row in the matrix. This will return null if this is
     * a dense array.
     * 
     * @return null if this is a dense array, else the indices into the
     *         getRowInd() and getRowVal() arrays.
     * @see ClutoMatrix
     */
    public int[] getRowPtr();

    /**
     * If this is a sparse matrix, this will return an array that provides the
     * column index for this value in the matrix. This will return null if this
     * is a dense array.
     * 
     * @return null if this is a dense array, else the column indices of the
     *         values in the getRowVal() array.
     * @see ClutoMatrix
     */
    public int[] getRowInd();

    /**
     * Return the values of the cells as row-ordered array. If this is a dense
     * Matrix this array will be getRowCount() * getColumnCount() in length. If
     * this is a sparse matrix, the index of a cell is determined using the
     * arrays return by getRowPtr() and getRowInd().
     * 
     * @return The values of the cells as row-ordered array.
     * @see ClutoMatrix
     */
    public float[] getRowVal();

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
    public float getValue(int rowIndex, int columnIndex);

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
    public float[] getAllValues();

    /**
     * Return the values for the given row of the matrix.
     * 
     * @param rowIndex
     *            the row index of the matrix.
     * @return The values in the given row of the matrix.
     */
    public float[] getRow(int rowIndex);

    /**
     * Return whether every column in the given row of the matrix has a value.
     * 
     * @param rowIndex
     *            the row index of the matrix.
     * @return true if every column in the given row of the matrix has a value,
     *         else false.
     */
    public boolean isRowComplete(int rowIndex);
}
