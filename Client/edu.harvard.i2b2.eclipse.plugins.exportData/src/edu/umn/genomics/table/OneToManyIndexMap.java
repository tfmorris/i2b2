/*
 * @(#) $RCSfile: OneToManyIndexMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * Provides a mapping between two sets of indices that are in sequential order.
 * The source indices map to any number of destination indices. But each
 * destination index can map to one source index at most. The mapping is
 * provided by the srcToDstIndex array. The srcToDstIndex has a length that is 1
 * greater than the length of the source index. <br>
 * A source index <code><b>i</b></code> maps to destination indices
 * <code>srcToDst[<b>i</b>]</code> to <code>srcToDst[<b>i</b>+1]</code>. The
 * number of destination indices equals
 * <code>srcToDst[<b>i</b>+1] - srcToDst[<b>i</b>]</code>. <br>
 * <br>
 * For example given srcToDstIndex array: <code> [0 2 5 6 6 8] </code> the
 * following shows the mapping from the 5 elements in the src to the 8 elements
 * in the dst index.
 * 
 * <pre>
 * &lt;br&gt;src      [0 1 2 3 4]
 * &lt;br&gt;srcToDst [0 2 5 6 6 8]  
 * &lt;br&gt;dst      [0 0 1 1 1 2 4 4]  (shows the src index mapped to)
 * </pre>
 * <ul>
 * <li>getSrcSize() returns 5
 * <li>getDstSize() returns 8
 * <li>getDsts(1) returns [2 3 4] (indices starting from 2 but less than 5)
 * <li>getDst(1) returns 2 (the first index mapped to)
 * <li>getDsts(3) returns [ ]
 * <li>getDst(1) returns -1 (indicating NO mapping og this index)
 * <li>getSrc(5) returns 2
 * <li>getSrcs(5) returns [2]
 * </ul>
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class OneToManyIndexMap implements IndexMap, Cloneable {
    public int srcToDst[];

    /**
     * Provide a mapping between two sets of indices.
     * 
     * @see #setIndex
     */
    public OneToManyIndexMap() {
    }

    /**
     * Provide a mapping between two sets of indices.
     * 
     * @param srcToDstIndex
     *            The index mapping array.
     * @see #setIndex
     */
    public OneToManyIndexMap(int srcToDstIndex[]) {
	setIndex(srcToDstIndex);
    }

    @Override
    public Object clone() {
	OneToManyIndexMap im = null;
	try {
	    im = (OneToManyIndexMap) super.clone();
	} catch (CloneNotSupportedException ex) {
	    im = new OneToManyIndexMap();
	}
	im.srcToDst = srcToDst != null ? (int[]) srcToDst.clone() : null;
	return im;
    }

    /**
     * Return true if this is a one to one mapping.
     * 
     * @return true if this is a one to one mapping, else efalse.
     */
    public boolean isOneToOne() {
	return false;
    }

    /**
     * Get the size of the source index.
     * 
     * @return the size of the source index.
     */
    public int getSrcSize() {
	return srcToDst.length - 1;
    }

    /**
     * Get the size of the destination index.
     * 
     * @return the size of the destination index.
     */
    public int getDstSize() {
	return srcToDst == null || srcToDst.length < 1 ? 0
		: srcToDst[srcToDst.length - 1];
    }

    /**
     * Return the source index for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source index mapped from the destination index.
     */
    public int getSrc(int dstIndex) {
	int[] indices = getSrcs(dstIndex);
	return indices.length > 0 ? indices[0] : -1;
    }

    /**
     * Return the destination index for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination index mapped from the source index.
     */
    public int getDst(int srcIndex) {
	int[] indices = getDsts(srcIndex);
	return indices.length > 0 ? indices[0] : -1;
    }

    /**
     * Return the source indices for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source indices mapped from the destination index.
     */
    public int[] getSrcs(int dstIndex) { // throw range error?
	if (dstIndex >= 0 && dstIndex < srcToDst[srcToDst.length - 1]) {
	    int fi = 0;
	    int ti = srcToDst.length - 1;
	    while (ti - fi > 1) {
		if (dstIndex == srcToDst[fi]) {
		    break;
		}
		int mi = fi + (ti - fi) / 2;
		if (dstIndex < srcToDst[mi]) {
		    ti = mi;
		} else if (dstIndex > srcToDst[mi]) {
		    fi = mi;
		} else {
		    fi = mi;
		    while (++mi < ti && dstIndex == srcToDst[mi]) {
			fi = mi;
		    }
		    break;
		}
	    }
	    int retVal[];
	    if (true || dstIndex == srcToDst[fi]) {
		retVal = new int[1];
		retVal[0] = fi;
	    } else {
		retVal = new int[0];
	    }
	    return retVal;
	}
	return new int[0];
    }

    /**
     * Return the destination indices for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination indices mapped from the source index.
     */
    public int[] getDsts(int srcIndex) { // throw range error?
	if (srcIndex < 0 || srcIndex >= srcToDst.length - 1) {
	    return new int[0];
	}
	int len = srcToDst[srcIndex + 1] - srcToDst[srcIndex];
	int retVal[] = new int[len];
	for (int i = 0; i < len; i++) {
	    retVal[i] = srcToDst[srcIndex] + i;
	}
	return retVal;
    }

    /**
     * The srcToDstIndex provides a mapping between two sets of indices that are
     * in sequential order. The source indices map to any number of destination
     * indices. But each each destination index can map to one source index at
     * most. The srcToDstIndex length is 1 greater than then length of the
     * source index. A source index i maps to destination indices
     * srcToDstIndex[i] to srcToDstIndex[i+1]. The number of destination indices
     * equals srcToDstIndex[i+1] - srcToDstIndex[i].
     * 
     * @param srcToDstIndex
     *            The index mapping array.
     */
    public void setIndex(int srcToDstIndex[]) {
	srcToDst = srcToDstIndex;
    }

    /**
     * Return the index mapping array.
     * 
     * @return The index mapping array.
     */
    public int[] getIndex() {
	return srcToDst;
    }

}
