/*
 * @(#) $RCSfile: ProcessMatrix.java,v $ $Revision: 1.3 $ $Date: 2008/11/24 22:06:46 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.Arrays;

/**
 * ProcessMatrix provides methods for preprocessing a matrix prior to performing
 * cluster analysis on it. It provides methods for estimating missing values in
 * the matrix, and allows the matrix to be transformed to log ratios as well as
 * mean or median centering.
 * 
 * @see ClutoMatrix
 * @see TransParams
 * @author Shulan Tian
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/24 22:06:46 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class ProcessMatrix implements PreProcess {
    private final static int NO_VAL = -1;
    private final static int MAX_DIFFERENCE = 9999;
    private final static float ALMOST_ZERO = 0.0001f;
    protected ClutoMatrix clutoMatrix;
    protected TransParams tParam;

    /**
     * Constructor for ProcessMatrix.
     * 
     * @param cMat
     *            ClutoTableMatrix
     * @param tParam
     *            Parameters returned from TransMatrixPanel
     */
    public ProcessMatrix(ClutoMatrix cMat, TransParams tParam) {
	this.clutoMatrix = cMat;
	this.tParam = tParam;
    }

    /**
     * Get the original ClutoMatrix
     * 
     * @return the original ClutoMatrix
     * @see ClutoMatrix
     */
    public ClutoMatrix getMatrix() {
	return clutoMatrix;
    }

    /**
     * Get the matrix transformation parameters.
     * 
     * @return the matrix transformation parameters.
     * @see TransParams
     */
    public TransParams getParams() {
	return tParam;
    }

    /**
     * This function preprocess the matrix before clustering
     * 
     * @return a dense ClutoMatrix if an Estimation method is selected.
     */
    public ClutoMatrix getTransformedMatrix() {
	ClutoMatrix cMat = getMatrix();
	ClutoMatrix mycMat;
	// Replace missing values in matrix with estimated value
	switch (tParam.getEstMethod()) {
	case TransParams.Do_Nothing:
	    mycMat = cMat;
	    break;
	case TransParams.KNN:
	    mycMat = getKNNMatrix(tParam.getNumNeighbors(), cMat);
	    break;
	case TransParams.Row_Average:
	    mycMat = getRowAverageMatrix(cMat);
	    break;
	case TransParams.Column_Average:
	    mycMat = getColAverageMatrix(cMat);
	    break;
	case TransParams.Input_With_Zero:
	    mycMat = getZeroMatrix(cMat);
	    break;
	default:
	    mycMat = cMat;
	    break;
	}
	// Transform the matrix to log ratio values
	if (tParam.getLogTrans()) {
	    mycMat = getLogMatrix(mycMat);
	}
	// Transform the matrix by centering values on the mean value
	if (tParam.getMeanCenter()) {
	    mycMat = getMeanMatrix(mycMat);
	}
	// Transform the matrix by centering values on the median value
	if (tParam.getMedianCenter()) {
	    mycMat = getMedianMatrix(mycMat);
	}
	return mycMat;
    }

    /**
     * This function uses K nearest neighbors method to estimate the missing
     * values it uses Euclidean distance as similarity measurements and finds k
     * nearest neighbors that are subject to rules, then calculate the weighted
     * average distance and return a dense ClutoMatrix with all missing values
     * filled Any row with missing values won't be included as neighbors if
     * number of neighbors supplied is more than the number of rows in that
     * column, use as many rows as in ClutoMatrix
     * 
     * @param k
     *            Number of nearest Neighbors
     * @param cMat
     *            Supplied ClutoMatrix
     */
    public ClutoMatrix getKNNMatrix(int k, ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();

	float[] distances = new float[k];
	int[] neighbors = new int[k];
	float[] weights = new float[k];

	float dSum, newVal;
	float[] rowval = cMat.getAllValues();

	for (int i = 0; i < nrows; i++) {
	    if (!cMat.isRowComplete(i)) {

		for (int j = 0; j < ncols; j++) {
		    if (Float.isNaN(cMat.getValue(i, j))) {
			KNN(k, i, j, cMat, neighbors, distances);

			dSum = 0.0f;

			for (int a = 0; a < k; a++) {
			    if (distances[a] == 0) {
				distances[a] = ALMOST_ZERO;
			    }
			}

			for (int a = 0; a < k; a++) {
			    dSum += 1 / distances[a];
			}

			for (int a = 0; a < k; a++) {
			    weights[a] = 1 / (distances[a] * dSum);
			}

			newVal = 0.0f;
			for (int a = 0; (a < k) && (neighbors[a] != NO_VAL); a++) {
			    newVal += cMat.getValue(neighbors[a], j)
				    * weights[a];
			}
			rowval[i * ncols + j] = newVal;
		    }
		}
	    }
	}

	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function uses row average method to estimate the missing values it
     * returns a dense Matrix with all missing values filled
     * 
     * @param cMat
     *            supplied ClutoMatrix
     */
    public ClutoMatrix getRowAverageMatrix(ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();
	float newVal = 0.0f;
	float[] rowval = cMat.getAllValues();

	for (int i = 0; i < nrows; i++) {
	    if (!cMat.isRowComplete(i)) {
		for (int j = 0; j < ncols; j++) {
		    if (Float.isNaN(cMat.getValue(i, j))) {
			newVal = getRowAvg(i, cMat);
			rowval[i * ncols + j] = newVal;
		    }
		}
	    }
	}
	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function uses column average method to estimate the missing values
     * it returns a dense Matrix with all missing values filled
     * 
     * @param cMat
     *            supplied ClutoMatrix
     */
    public ClutoMatrix getColAverageMatrix(ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();
	float newVal = 0.0f;
	float[] rowval = cMat.getAllValues();

	for (int i = 0; i < nrows; i++) {
	    if (!cMat.isRowComplete(i)) {
		for (int j = 0; j < ncols; j++) {
		    if (Float.isNaN(cMat.getValue(i, j))) {
			newVal = getColAvg(j, cMat);
			rowval[i * ncols + j] = newVal;

		    }
		}
	    }
	}
	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function replaces the missing values with zeros it returns a dense
     * Matrix with all missing values filled
     * 
     * @param cMat
     *            supplied ClutoMatrix
     */

    public ClutoMatrix getZeroMatrix(ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();
	float newVal = 0.0f;
	float[] rowval = cMat.getAllValues();

	for (int i = 0; i < nrows; i++) {
	    if (!cMat.isRowComplete(i)) {
		for (int j = 0; j < ncols; j++) {
		    if (Float.isNaN(cMat.getValue(i, j))) {
			newVal = 0.0f;
			rowval[i * ncols + j] = newVal;
		    }
		}
	    }
	}
	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function subtract mean from the values row by row in supplied
     * matrix. it returns a Matrix with all meaned center values
     * 
     * @param cMat
     *            supplied ClutoMatrix
     */
    public ClutoMatrix getMeanMatrix(ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();
	float[] rowval = cMat.getAllValues();

	for (int i = 0, k = 0; i < nrows; i++) {
	    float[] rowArray = cMat.getRow(i);
	    float mean = mean(rowArray);
	    for (int j = 0; j < ncols; j++, k++) {
		rowval[k] = rowval[k] - mean;
	    }
	}

	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function subtract median from the values row by row in supplied
     * matrix. it returns a Matrix with all median centered values
     * 
     * @param cMat
     *            supplied ClutoMatrix
     */
    public ClutoMatrix getMedianMatrix(ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();
	float[] rowval = cMat.getAllValues();

	for (int i = 0, k = 0; i < nrows; i++) {
	    float[] rowArray = cMat.getRow(i);
	    float median = median(rowArray);
	    for (int j = 0; j < ncols; j++, k++) {
		rowval[k] = rowval[k] - median;
	    }
	}

	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function use logrithm values (base 2) to replace the actual values
     * in Matrix. it returns a Matrix with log transformed values
     * 
     * @param cMat
     *            supplied ClutoMatrix
     */
    public ClutoMatrix getLogMatrix(ClutoMatrix cMat) {
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();
	float[] rowval = cMat.getAllValues();

	for (int i = 0; i < rowval.length; i++) {
	    if (!Float.isNaN(rowval[i]) && !Float.isInfinite(rowval[i])) {
		if (rowval[i] > 0) {
		    rowval[i] = (float) (Math.log(rowval[i]))
			    / (float) (Math.log(2.0));
		} else if (rowval[i] < 0) {
		    rowval[i] = (float) (-Math.log(-rowval[i]))
			    / (float) (Math.log(2.0));
		} else {
		    rowval[i] = (float) (Math.log(rowval[i]))
			    / (float) (Math.log(2.0));
		}
	    }
	}
	ClutoTableMatrix nMat = new ClutoTableMatrix(nrows, ncols, null, null,
		rowval);
	if (cMat instanceof ClutoTableMatrix) {
	    for (int c = 0; c < ncols; c++) {
		nMat.setColumnName(c, ((ClutoTableMatrix) cMat)
			.getColumnName(c));
	    }
	}
	return nMat;
    }

    /**
     * this function finds mean of the row values
     * 
     * @param array
     *            is supplied array
     * @return the mean average of the values of the array
     */
    public float mean(float array[]) {
	float result = 0;
	if (array != null && array.length > 0) {
	    int n = 0; // number of non null values
	    for (int i = 0; i < array.length; i++) {
		if (!Float.isNaN(array[i]) && !Float.isInfinite(array[i])) {
		    result += array[i];
		    n++;
		}
	    }
	    if (n > 0) {
		result = result / n;
	    }
	}
	return result;
    }

    /**
     * this function finds median of the row values with quick sort algorithm.
     * 
     * @param array
     *            is supplied array
     */
    public float median(float array[]) {
	float median = 0.f;
	if (array != null && array.length > 0) {
	    int n = array.length;
	    Arrays.sort(array);
	    if (n % 2 == 0) {
		median = (array[n / 2] + array[n / 2 + 1]) / 2;
	    } else {
		median = array[n / 2 + 1];
	    }
	}
	return median;
    }

    /**
     * this function finds k nearest neighbores to the cell with missing value
     * 
     * there are two preliminary rules to eliminate a neighbor: 1. don't include
     * the row itself as a neighbor. 2. don't use a neighbor with missing values
     * at coordinate column it stores the indices and distances of nearest
     * neighbors in arrays
     * 
     * @param k
     *            number of nearest neighbors
     * @param rowIndex
     *            row index of the cell with missing value
     * @param colIndex
     *            column index of the cell with missing value
     * @param cMat
     *            supplied ClutoMatrix
     * @param neighbors
     *            integer array to store the indice of K nearest neighbors
     * @param distances
     *            float array to store the distances between the cell with
     *            missing value and K nearest neighbors
     */
    public void KNN(int k, int rowIndex, int colIndex, ClutoMatrix cMat,
	    int[] neighbors, float[] distances) {
	float dis = 0.0f;
	int nrows = cMat.getRowCount();
	int ncols = cMat.getColumnCount();

	for (int i = 0; i < k; i++) {
	    neighbors[i] = NO_VAL;
	    distances[i] = MAX_DIFFERENCE;
	}
	for (int i = 0; i < nrows; i++) {
	    if ((rowIndex != i) && (!Float.isNaN(cMat.getValue(i, colIndex)))) {
		dis = EuclidianDistance(cMat.getRow(rowIndex), cMat.getRow(i),
			ncols, true, 0);
		boolean newNeighbor = false;
		for (int j = 0; (j < k) && (!newNeighbor); j++) {
		    if (dis < distances[j]) {
			newNeighbor = true;
			if (j != k - 1) {
			    System.arraycopy(neighbors, j, neighbors, j + 1, (k
				    - j - 1));
			    System.arraycopy(distances, j, distances, j + 1, (k
				    - j - 1));
			}
			neighbors[j] = i;
			distances[j] = dis;
		    }
		}
	    }
	}
    }

    /**
     * this function finds the unconditional average of the row it only use
     * values that are not missing
     * 
     * @param rowIndex
     *            index of the row with missing value cell
     * @param cMat
     *            supplied ClutoMatrix
     */
    public float getRowAvg(int rowIndex, ClutoMatrix cMat) {
	int count = 0;
	float sum = 0.0f;
	float avg = 0.0f;
	int ncols = cMat.getColumnCount();
	for (int j = 0; j < ncols; j++) {
	    if (!Float.isNaN(cMat.getValue(rowIndex, j))) {
		sum += cMat.getValue(rowIndex, j);
		count++;
	    }
	}
	if (count != 0) {
	    avg = sum / count;
	} else {
	    avg = 0.0f;
	}
	return avg;
    }

    /**
     * this function finds the unconditional average of the column it only use
     * values that are not missing
     * 
     * @param colIndex
     *            index of the column with missing value cell
     * @param cMat
     *            supplied ClutoMatrix
     */

    public float getColAvg(int colIndex, ClutoMatrix cMat) {
	int count = 0;
	float sum = 0.0f;
	float avg = 0.0f;
	int nrows = cMat.getRowCount();
	for (int j = 0; j < nrows; j++) {
	    if (!Float.isNaN(cMat.getValue(j, colIndex))) {
		sum += cMat.getValue(j, colIndex);
		count++;
	    }
	}
	if (count != 0) {
	    avg = sum / count;
	} else {
	    avg = 0.0f;
	}
	return avg;
    }

    /**
     * The function calculate Euclidean distance between two vectors. if one of
     * the vector has a missing value (mvCheck is true), and will use mvVal as
     * the "distance" along that axis. it returns Euclidean distance between two
     * vectors
     * 
     * @param vec1
     *            vector with len-dimensional elements
     * @param vec2
     *            vector with len-dimensional elements
     * @param len
     *            dimensions of the vector
     * @param mvCheck
     *            is the vector has missing value?
     * @param mvVal
     *            the distance between the cell with missing value in one vector
     *            and its corresponding cellin another vector
     * @return EuclidianDistance between two vectors
     */
    public float EuclidianDistance(float[] vec1, float[] vec2, int len,
	    boolean mvCheck, int mvVal) {
	float dp = 0.0f;
	int goodValCt = 0;
	int i;
	if (mvCheck) {
	    for (i = 0; i < len; i++) {
		if ((!Float.isNaN(vec1[i])) && (!Float.isNaN(vec2[i]))) {
		    dp += Math.pow(vec1[i] - vec2[i], 2);
		    goodValCt++;
		} else {
		    dp += mvVal;
		}
	    }
	} else {
	    for (i = 0; i < len; i++) {
		dp += Math.pow(vec1[i] - vec2[i], 2);
	    }
	}
	return (float) Math.sqrt(dp / goodValCt);
    }
}
