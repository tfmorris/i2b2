/*
 * @(#) $RCSfile: SimpleIndexMap.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * Provides a mapping between two sets of indices.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class SimpleIndexMap implements IndexMap {
    int srcPerDst = 1;
    int dstPerSrc = 1;
    int dstOffset = 0;
    int srcSize = 0;
    int dstSize = 0;

    public SimpleIndexMap() {
    }

    public SimpleIndexMap(int srcPerDst, int dstPerSrc, int dstOffset,
	    int srcSize, int dstSize) {
	this.srcPerDst = srcPerDst;
	this.dstPerSrc = dstPerSrc;
	this.dstOffset = dstOffset;
	this.srcSize = srcSize;
	this.dstSize = dstSize;
    }

    /**
     * Return true if this is a one to one mapping.
     * 
     * @return true if this is a one to one mapping, else efalse.
     */
    public boolean isOneToOne() {
	return true;
    }

    /**
     * Set the size of the source index.
     * 
     * @param srcSize
     *            the size of the source index.
     */
    public void setSrcSize(int srcSize) {
	this.srcSize = srcSize;
    }

    /**
     * Get the size of the source index.
     * 
     * @return the size of the source index.
     */
    public int getSrcSize() {
	return srcSize;
    }

    /**
     * Set the size of the destination index.
     * 
     * @param dstSize
     *            the size of the destination index.
     */
    public void setDstSize(int dstSize) {
	this.dstSize = dstSize;
    }

    /**
     * Get the size of the destination index.
     * 
     * @return the size of the destination index.
     */
    public int getDstSize() {
	return dstSize;
    }

    /**
     * Return the source indices for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source indices mapped from the destination index.
     */
    public int getSrc(int dstIndex) {
	return dstIndex * srcPerDst / dstPerSrc - dstOffset;
    }

    /**
     * Return the destination indices for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination indices mapped from the source index.
     */
    public int getDst(int srcIndex) {
	return srcIndex * dstPerSrc / srcPerDst + dstOffset;
    }

    /**
     * Return the source indices for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source indices mapped from the destination index.
     */
    public int[] getSrcs(int dstIndex) {
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
    public int[] getDsts(int srcIndex) {
	int retVal[] = new int[1];
	retVal[0] = getDst(srcIndex);
	return retVal;
    }
}
