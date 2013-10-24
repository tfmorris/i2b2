/*
 * @(#) $RCSfile: OneToOneIndexMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * Provides a one to one mapping between two sets of indices, with an array of
 * indices that maps each element in the source list to an element in the
 * destination list. This provides a way to access a list or table in a certain
 * order without actually sorting the elements of the list or the rows of the
 * table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class OneToOneIndexMap implements IndexMap, Cloneable {
    int srcToDst[] = null;
    int dstToSrc[] = null;

    /**
     * Create a one to one mapping between two sets of indices.
     * 
     * @see #setIndex
     */
    public OneToOneIndexMap() {
    }

    /**
     * Create a one to one mapping between two sets of indices.
     * 
     * @param index
     *            the array of indices that each element in the source list will
     *            map to in the destination list.
     */
    public OneToOneIndexMap(int index[]) {
	setIndex(index);
    }

    @Override
    public Object clone() {
	OneToOneIndexMap im = null;
	try {
	    im = (OneToOneIndexMap) super.clone();
	} catch (CloneNotSupportedException ex) {
	    im = new OneToOneIndexMap();
	}
	im.srcToDst = srcToDst != null ? (int[]) srcToDst.clone() : null;
	im.dstToSrc = dstToSrc != null ? (int[]) dstToSrc.clone() : null;
	return im;
    }

    /**
     * Return true if this is a one to one mapping.
     * 
     * @return true if this is a one to one mapping, else false.
     */
    public boolean isOneToOne() {
	return true;
    }

    /**
     * Get the size of the source index.
     * 
     * @return the size of the source index.
     */
    public int getSrcSize() {
	return srcToDst != null ? srcToDst.length : 0;
    }

    /**
     * Get the size of the destination index.
     * 
     * @return the size of the destination index.
     */
    public int getDstSize() {
	return srcToDst != null ? srcToDst.length : 0;
    }

    /**
     * Return the source index for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source index mapped from the destination index.
     */
    public int getSrc(int dstIndex) { // throw range error?
	if (dstIndex < 0 || srcToDst == null || dstIndex >= srcToDst.length)
	    return -1;
	return srcToDst[dstIndex];
    }

    /**
     * Return the destination index for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination index mapped from the source index.
     */
    public int getDst(int srcIndex) { // throw range error?
	if (srcIndex < 0 || srcToDst == null || srcIndex >= srcToDst.length)
	    return -1;
	if (dstToSrc == null) {
	    dstToSrc = new int[srcToDst.length];
	    for (int i = 0; i < srcToDst.length; i++) {
		dstToSrc[srcToDst[i]] = i;
	    }
	}
	return dstToSrc[srcIndex];
    }

    /**
     * Return the source indices for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source indices mapped from the destination index.
     */
    public int[] getSrcs(int dstIndex) { // throw range error?
	int retVal[] = new int[1];
	retVal[0] = getSrc(dstIndex);
	return retVal;
    }

    /**
     * Return the destination indices for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination indices mapped from the source index.
     */
    public int[] getDsts(int srcIndex) { // throw range error?
	int retVal[] = new int[1];
	retVal[0] = getDst(srcIndex);
	return retVal;
    }

    /**
     * Set the array of indices that each element in the source list will map to
     * in the destination list.
     * 
     * @param index
     *            the array of indices that each element in the source list will
     *            map to in the destination list.
     */
    public void setIndex(int index[]) {
	srcToDst = index;
	dstToSrc = null;
    }

    /**
     * Get the array of indices that each element in the source list will map to
     * in the destination list.
     * 
     * @return the array of indices that each element in the source list will
     *         map to in the destination list.
     */
    public int[] getIndex() {
	return srcToDst;
    }

}
