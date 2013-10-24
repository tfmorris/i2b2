/*
 * @(#) $RCSfile: SortIndex.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * SortIndex generates an index array that will access the values of the given
 * array in sorted order.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class SortIndex {

    /**
     * Swaps the index positions ia[a] with ia[b].
     */
    private static void swap(int ia[], int a, int b) {
	int i = ia[a];
	ia[a] = ia[b];
	ia[b] = i;
    }

    /**
     * qsort an array of int by generating a corresponding index array
     */
    private static void qsort(int a[], int ia[], int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	int mid;
	if (hi0 > lo0) {
	    /*
	     * Arbitrarily establishing partition element as the midpoint of the
	     * array.
	     */
	    mid = (lo0 + hi0) / 2;
	    int mval = a[ia[mid]];
	    // loop through the array until indices cross
	    while (lo <= hi) {
		/*
		 * find the first element that is greater than or equal to the
		 * partition element starting from the left Index.
		 */
		while ((lo < hi0) && (a[ia[lo]] < mval))
		    ++lo;
		/*
		 * find an element that is smaller than or equal to the
		 * partition element starting from the right Index.
		 */
		while ((hi > lo0) && (a[ia[hi]] > mval))
		    --hi;
		// if the indexes have not crossed, swap
		if (lo <= hi) {
		    if (lo < hi)
			swap(ia, lo, hi);
		    ++lo;
		    --hi;
		}
	    }
	    /*
	     * If the right index has not reached the left side of array must
	     * now sort the left partition.
	     */
	    if (lo0 < hi)
		qsort(a, ia, lo0, hi);
	    /*
	     * If the left index has not reached the right side of array must
	     * now sort the right partition.
	     */
	    if (lo < hi0)
		qsort(a, ia, lo, hi0);
	}
    }

    /**
     * Return an index array that provides access to the given array in sorted
     * order.
     * 
     * @param array
     *            An array of ints for which to generate an index.
     * @return he index that will access the array in sorted order.
     */
    public static int[] getSortIndex(int[] array) {
	if (array == null) {
	    // throw IllegalArgumentException
	    return null;
	}
	int[] si = new int[array.length];
	for (int i = 0; i < si.length; i++) {
	    si[i] = i;
	}
	qsort(array, si, 0, array.length - 1);
	return si;
    }
}
