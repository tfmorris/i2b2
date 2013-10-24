/*
 * @(#) $RCSfile: MultiDimIntArray.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table;

import java.io.Serializable;
import java.util.*;

/**
 * Stores values for a multidimensional array in a one dimensional array.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 */
public class MultiDimIntArray implements Serializable {
    int[] dims;
    int[] vals;

    /**
     * Create a MultiDimIntArray for the given dimensions.
     * 
     * @param dims
     *            The dimensions for a multidimensional array.
     * @exception NullPointerException
     *                If dims are null.
     * @exception IllegalArgumentException
     *                If any of dims are < 1.
     */
    public MultiDimIntArray(int[] dims) throws NullPointerException,
	    IllegalArgumentException {
	// Check that dims != null and all dims > 0;
	if (dims == null) {
	    throw new NullPointerException(
		    "MultiDimIntArray must have dimensions.");
	}
	int tmp[] = new int[dims.length];
	int size = 1;
	for (int i = 0; i < dims.length; i++) {
	    if (dims[i] <= 0) {
		throw new IllegalArgumentException(
			"MultiDimIntArray dimensions must be positive.");
	    }
	    tmp[i] = dims[i];
	    size *= dims[i];
	}
	this.dims = tmp;
	vals = new int[size];
    }

    public int[] getDims() {
	return dims;
    };

    /**
     * Get the one dimensional index that corresponds to the location in the
     * multidimensional array.
     * 
     * @param loc
     *            The location indices in the multidimensional array.
     * @param dims
     *            The dimensions for a multidimensional array.
     * @return the one dimensional index corresponding to the given
     *         multidimensional location.
     * @exception NullPointerException
     *                If loc or dims are null.
     * @exception IllegalArgumentException
     *                If any of loc are < 0 or > dims.
     */
    public static int getIndex(int[] loc, int[] dims) {
	// Check that loc != null loc.length = dims.length and all loc > 0;
	if (loc == null) {
	    throw new NullPointerException(
		    "MultiDimIntArray location index array is null.");
	} else if (dims == null) {
	    throw new NullPointerException(
		    "MultiDimIntArray dimensions are not set");
	} else if (loc == null || dims == null || loc.length > dims.length) {
	    throw new IllegalArgumentException(
		    "getIndex: loc does not have the same dimensions "
			    + loc.length + " vs. " + dims.length);
	}
	int idx = 0;
	for (int i = loc.length - 1, j = 1; i >= 0; j *= dims[i], i--) {
	    if (loc[i] < 0) {
		throw new IllegalArgumentException(
			"MultiDimIntArray dimensions must be positive.");
	    } else if (loc[i] >= dims[i]) {
		throw new IllegalArgumentException("MultiDimIntArray index "
			+ i + ": " + loc[i] + " !< " + dims[i]);
	    }
	    idx += loc[i] * j;
	}
	return idx;
    }

    /*
     * Return a list of one-dimensional indices that correspond to the given
     * multidimensional location. The length of the loc array can be less than
     * the length of the dims array, thus a loc of [1,2] with dims of [2,3,4]
     * would return [20,21,22,23].
     * 
     * @param loc The location indices in the multidimensional array.
     * 
     * @param dims The dimensions for a multidimensional array.
     * 
     * @return an array of one dimensional index corresponding to the given
     * multidimensional location.
     */
    public static int[] getIndexArray(int[] loc, int[] dims) {
	int[] ia = null;
	if (loc == null || dims == null || loc.length < 1 || dims.length < 1) {
	    ia = new int[0];
	} else if (loc.length == dims.length) {
	    ia = new int[1];
	    ia[0] = getIndex(loc, dims);
	} else if (loc.length < dims.length) {
	    int n = 1;
	    for (int i = dims.length - 1; i >= loc.length; i--) {
		n *= dims[i];
	    }
	    ia = new int[n];
	    int[] idx = new int[dims.length];
	    System.arraycopy(loc, 0, idx, 0, loc.length);
	    for (int i = 0; i < n; i++) {
		ia[i] = getIndex(idx, dims);
		incrIndex(idx, dims);
	    }
	}
	return ia;
    }

    /**
     * Get the one dimensional index that corresponds to the location in the
     * multidimensional array.
     * 
     * @param loc
     *            The location indices in the multidimensional array.
     * @return the one dimensional index corresponding to the given
     *         multidimensional location.
     * @exception NullPointerException
     *                If loc or dims are null.
     * @exception IllegalArgumentException
     *                If any of loc are < 0 or > dims.
     */
    public int getIndex(int[] loc) {
	return getIndex(loc, getDims());
    }

    /**
     * Increment the mutlidimensional index array such that the indices will
     * choose the next element of the array or the first element if the given
     * indices were at the last element.
     * 
     * @param indices
     *            The indices to increment
     * @param dim
     *            The dimensions on the indices
     * @return the input param indices
     */
    public static int[] incrIndex(int[] indices, int[] dim) {
	if (indices.length <= dim.length) {
	    for (int i = indices.length - 1; i >= 0; i--) {
		if (indices[i] + 1 < dim[i]) {
		    indices[i]++;
		    break;
		} else {
		    indices[i] = 0;
		}
	    }
	}
	return indices;
    }

    /**
     * Decrement the mutlidimensional index array such that the indices will
     * choose the previous element of the array or the last element if the given
     * indices were at the first element.
     * 
     * @param indices
     *            The indices to decrement
     * @param dim
     *            The dimensions on the indices
     * @return the input param indices
     */
    public static int[] decrIndex(int[] indices, int[] dim) {
	if (indices.length <= dim.length) {
	    for (int i = indices.length - 1; i >= 0; i--) {
		if (indices[i] > 0) {
		    indices[i]--;
		    break;
		} else {
		    indices[i] = dim[i] - 1;
		}
	    }
	}
	return indices;
    }

    /**
     * Increment the mutlidimensional index array starting with the leftmost
     * index such that the indices will choose the next element of the array or
     * the first element if the given indices were at the last element.
     * 
     * @param indices
     *            The indices to increment
     * @param dim
     *            The dimensions on the indices
     * @return the input param indices
     */
    public static int[] incrMajorIndex(int[] indices, int[] dim) {
	if (indices.length <= dim.length) {
	    for (int i = 0; i < indices.length; i++) {
		if (indices[i] + 1 < dim[i]) {
		    indices[i]++;
		    break;
		} else {
		    indices[i] = 0;
		}
	    }
	}
	return indices;
    }

    /**
     * Decrement the mutlidimensional index array starting with the leftmost
     * index such that the indices will choose the previous element of the array
     * or the last element if the given indices were at the first element.
     * 
     * @param indices
     *            The indices to decrement
     * @param dim
     *            The dimensions on the indices
     * @return the input param indices
     */
    public static int[] decrMajorIndex(int[] indices, int[] dim) {
	if (indices.length <= dim.length) {
	    for (int i = 0; i < indices.length; i++) {
		if (indices[i] > 0) {
		    indices[i]--;
		    break;
		} else {
		    indices[i] = dim[i] - 1;
		}
	    }
	}
	return indices;
    }

    /**
     * Increment the mutlidimensional index array such that the indices will
     * choose the next element of the array or the first element if the given
     * indices were at the last element.
     * 
     * @param indices
     *            The indices to increment
     * @return the input param indices
     */
    public int[] incrIndex(int[] indices) {
	return incrIndex(indices, getDims());
    }

    /**
     * Get the mutlidimensional index array that corresponds to the one
     * dimensional index.
     * 
     * @param index
     *            The one dimensional index
     * @param dim
     *            The dimensions on the indices
     * @return the multidimensional indices
     * @exception ArrayIndexOutOfBoundsException
     *                If index < 0 or index > array designated by dim.
     */
    public static int[] getIndices(int index, int[] dim)
	    throws ArrayIndexOutOfBoundsException {
	if (index < 0) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	int idx = index;
	int[] indices = new int[dim.length];
	indices[dim.length - 1] = 1;
	for (int i = dim.length - 2; i >= 0; i--) {
	    indices[i] = dim[i + 1] * indices[i + 1];
	}
	for (int i = 0; i < dim.length; i++) {
	    int d = idx > 0 ? idx / indices[i] : 0;
	    idx -= d * indices[i];
	    indices[i] = d;
	}
	if (indices[0] >= dim[0]) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	return indices;
    }

    /**
     * Get the mutlidimensional index array that corresponds to the one
     * dimensional index.
     * 
     * @param index
     *            The one dimensional index
     * @return the multidimensional indices
     * @exception ArrayIndexOutOfBoundsException
     *                If index < 0 or index > array designated by dim.
     */
    public int[] getIndices(int index) throws ArrayIndexOutOfBoundsException {
	return getIndices(index, getDims());
    }

    /**
     * Reset all array values to 0.
     */
    public void reset() {
	Arrays.fill(vals, 0);
    }

    /**
     * Get the value at loc in the array.
     * 
     * @param loc
     *            The indices of the value in the array.
     * @return The value of the array at the given indices.
     */
    public int get(int[] loc) {
	return vals[getIndex(loc)];
    }

    /**
     * Set the value at loc in the array.
     * 
     * @param loc
     *            The indices of the value in the array.
     * @param val
     *            The value for the given location in the array.
     */
    public void set(int[] loc, int val) {
	vals[getIndex(loc)] = val;
    }

    /**
     * Increment the value at loc in the array.
     * 
     * @param loc
     *            The indices of the value in the array.
     * @return The new value of the array at the given indices.
     */
    public int incr(int[] loc) {
	int i = getIndex(loc);
	vals[i] += 1;
	return vals[i];
    }

    /**
     * Get the value at loc in the array.
     * 
     * @param loc
     *            The index of the value in the array.
     * @return The value of the array at the given index.
     */
    public int get(int loc) {
	return vals[loc];
    }

    /**
     * Set the value at loc in the array.
     * 
     * @param loc
     *            The index of the value in the array.
     * @param val
     *            The value for the given location in the array.
     */
    public void set(int loc, int val) {
	vals[loc] = val;
    }

    /**
     * Increment the value at loc in the array.
     * 
     * @param loc
     *            The index of the value in the array.
     * @return The new value of the array at the given index.
     */
    public int incr(int loc) {
	vals[loc] += 1;
	return vals[loc];
    }

    /**
     * Get the maximum value of the array.
     * 
     * @return The maximum value of the array.
     */
    public int getMax() {
	int max = 0;
	if (vals != null) {
	    for (int i = 0; i < vals.length; i++) {
		if (max < vals[i])
		    max = vals[i];
	    }
	}
	return max;
    }

    /**
     * Get the values of the array.
     * 
     * @return The values of the array.
     */
    public int[] getValues() {
	return vals;
    }
}
