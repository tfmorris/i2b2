/*
 * @(#) $RCSfile: PartitionIndexMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * Provides a mapping between the array indices of a list of elements, and a
 * partition on that list of elements. <br>
 * For example, given partition index array : <code>
 *  [ 1 2 2 0 1 0 5 3 1 5 ]
 * </code> in which each of the 10
 * elements of the list are assigned to one of 6 parts of the partition.
 * <ul>
 * <li>getDstSize() returns 6 The number of partitions (0,...,5)
 * <li>getDst(i) returns the partition the value at postion i is assaigned.
 * <li>getDst(0) returns 1
 * <li>getDst(1) returns 2
 * <li>getDst(2) returns 2
 * <li>getDst(3) returns 0
 * <li>getDst(4) returns 1
 * <li>getDst(5) returns 0
 * <li>getDst(6) returns 5
 * <li>getDst(7) returns 3
 * <li>getDst(8) returns 1
 * <li>getDst(9) returns 5
 * <li>getSrcSize() returns 10
 * <li>getSrcs(i) returns the array indices assigned to the i partition.
 * <li>getSrcs(0) returns [3 5]
 * <li>getSrcs(1) returns [0 4 8]
 * <li>getSrcs(2) returns [1 2]
 * <li>getSrcs(3) returns [7]
 * <li>getSrcs(4) returns []
 * <li>getSrcs(5) returns [6 9]
 * </ul>
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */

public class PartitionIndexMap implements IndexMap, Cloneable {
    /*
     * For effeciency sake, we sort the initial array and use a
     * OneToManyIndexMap to reference that sorted list, then we use a
     * OneToManyIndexMap to map from a partition into that sorted list.
     */
    /** A partition index that places each element into a numbered partition. */
    private int[] partition;
    /** Does this mapping represent a one to one mapping between indices. */
    private boolean one2one = false;
    /**
     * A OneToOneIndexMap to provide sorted access to the partition assignments.
     */
    private OneToOneIndexMap dstMap;
    /**
     * A OneToManyIndexMap correlating each array index to a partition index.
     * with the src representing the partition indices and the dst representing
     * the indices in the the array that was partitioned.
     */
    private OneToManyIndexMap prtMap;

    /**
     * Provides a mapping between the array indices of a list of elements, and a
     * partition on that list of elements.
     * 
     * @see #setIndex
     */
    public PartitionIndexMap() {
    }

    /**
     * Provides a mapping between the array indices of a list of elements, and a
     * partition on that list of elements using the given partition array.
     * 
     * @param partitionIndex
     *            The index mapping array.
     * @see #setIndex
     */
    public PartitionIndexMap(int partitionIndex[]) {
	setIndex(partitionIndex);
    }

    @Override
    public Object clone() {
	PartitionIndexMap pim = null;
	try {
	    pim = (PartitionIndexMap) super.clone();
	} catch (CloneNotSupportedException ex) {
	    pim = new PartitionIndexMap();
	    pim.one2one = one2one;
	}
	pim.partition = partition != null ? (int[]) partition.clone() : null;
	pim.dstMap = dstMap != null ? (OneToOneIndexMap) dstMap.clone() : null;
	pim.prtMap = prtMap != null ? (OneToManyIndexMap) prtMap.clone() : null;
	return pim;
    }

    /**
     * Return true if this is a one to one mapping of indices to partition
     * indices.
     * 
     * @return true if this is a one to one mapping, else false.
     */
    public boolean isOneToOne() {
	return one2one;
    }

    /**
     * Get the size of the source index, that is, the length of the array of
     * items that were partitioned.
     * 
     * @return the size of the source index.
     */
    public int getSrcSize() {
	return partition != null ? partition.length : 0;
    }

    /**
     * Get the size of the destination index, that is the number of partitions
     * into which the array of items were assigned.
     * 
     * @return the size of the destination index.
     */
    public int getDstSize() {
	return prtMap != null ? prtMap.getSrcSize() : 0;
    }

    /**
     * Return the first source index for the given partition index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source index mapped from the destination index, or -1 if the
     *         dstIndex is not mapped to a source index.
     */
    public int getSrc(int dstIndex) {
	int[] indices = getSrcs(dstIndex);
	return indices.length > 0 ? indices[0] : -1;
    }

    /**
     * Return the partition index for the given source array index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination index mapped from the source index.
     */
    public int getDst(int srcIndex) {
	if (dstMap != null) {
	    return prtMap.getSrc(dstMap.getDst(srcIndex));
	} else {
	    return prtMap.getSrc(srcIndex);
	}
    }

    /**
     * Return the source indices for the given partition index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source indices mapped from the destination index.
     */
    public int[] getSrcs(int dstIndex) { // throw range error?
	int src[] = prtMap.getDsts(dstIndex);
	if (dstMap != null) {
	    for (int i = 0; i < src.length; i++) {
		if (src[i] >= 0 && src[i] < partition.length) {
		    src[i] = dstMap.getSrc(src[i]);
		}
	    }
	}
	return src;
    }

    /**
     * Return the partition indices for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination indices mapped from the source index.
     */
    public int[] getDsts(int srcIndex) { // throw range error?
	if (dstMap != null) {
	    return prtMap.getSrcs(dstMap.getDst(srcIndex));
	} else {
	    return prtMap.getSrcs(srcIndex);
	}
    }

    /**
     * The partitionIndex provides a mapping between the array indices of a list
     * of elements, and a partition on that list of elements. The partitionIndex
     * array is the same length as the size of the list it partitions. The value
     * at partitionIndex[<b>i</b>] is the part that element <b>i</b> in the list
     * is assigned to.
     * 
     * @param partitionIndex
     *            The index mapping array.
     */
    public void setIndex(int partitionIndex[]) {
	partition = partitionIndex;
	dstMap = null;
	prtMap = null;
	boolean asc = true;
	one2one = true;
	if (partition == null || partition.length < 1) {
	    return;
	}
	int[] si = partition;
	for (int i = 0, last = -1; i < si.length; i++) {
	    if (si[i] < last) {
		asc = false;
		break;
	    }
	    last = si[i];
	}
	if (!asc) { // get sortIndex
	    si = SortIndex.getSortIndex(partition);
	    // for (int i = 0; i < si.length; i++) {
	    // System.err.println("\t" + i + "\t" + partition[i] + "\t" +
	    // si[i]);
	    // }
	}
	for (int i = 0; i < si.length; i++) {
	    if (si[i] != i) {
		one2one = false;
		break;
	    }
	}
	if (!asc) {
	    dstMap = new OneToOneIndexMap(si);
	    int pmax = partition[si[si.length - 1]];
	    int[] p = new int[pmax + 2];
	    int pi = 0;
	    p[0] = 0;
	    p[p.length - 1] = si.length;
	    for (int i = 0, cnt = 0, last = 0; i < si.length; i++) {
		int ii = partition[si[i]];
		if (ii == last) {
		    cnt++;
		} else {
		    last = ii;
		    p[++pi] = i;
		    while (pi < last) {
			p[pi + 1] = p[pi];
			pi++;
		    }
		    cnt = 0;
		}
	    }
	    prtMap = new OneToManyIndexMap(p);
	} else {
	    int pmax = si[si.length - 1];
	    int[] p = new int[pmax + 2];
	    int pi = 0;
	    p[0] = 0;
	    p[p.length - 1] = si.length;
	    for (int i = 0, cnt = 0, last = 0; i < si.length; i++) {
		int ii = si[i];
		if (ii == last) {
		    cnt++;
		} else {
		    last = ii;
		    p[++pi] = i;
		    cnt = 0;
		    while (pi < last) {
			p[pi + 1] = p[pi];
			pi++;
		    }
		}
	    }
	    prtMap = new OneToManyIndexMap(p);
	}
    }

    /**
     * Return the partition index mapping array.
     * 
     * @return The partition index mapping array.
     */
    public int[] getIndex() {
	return partition;
    }
}
