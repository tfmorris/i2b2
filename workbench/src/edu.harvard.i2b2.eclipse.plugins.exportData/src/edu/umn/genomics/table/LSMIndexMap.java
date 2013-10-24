/*
 * @(#) $RCSfile: LSMIndexMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * Provides a mapping between two sets of indices, where the destination index
 * is a subset of the source index as determined by a ListSelectionModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class LSMIndexMap implements IndexMap, ListSelectionListener, CleanUp {
    ListSelectionModel lsm;
    int srcToDst[] = null;
    int dstToSrc[] = null;
    int srcSize = 0;
    int dstSize = 0;
    boolean useSelected = true;

    /**
     * Create a mapping to a subset of a list with the ListSelectionModel
     * determining which elements are in the subset.
     * 
     * @param lsm
     *            The ListSelectionModel that determines which elements are in
     *            the subset.
     * @param useSelected
     *            whether the subset is the selected or unselected elements.
     * @param trackSelectionModel
     *            whether to change the mapping in response to changes in the
     *            ListSelectionModel
     */
    public LSMIndexMap(ListSelectionModel lsm, boolean useSelected,
	    boolean trackSelectionModel) {
	this.lsm = lsm;
	this.useSelected = useSelected;
	makeIndex();
	if (trackSelectionModel)
	    lsm.addListSelectionListener(this);
    }

    /**
     * Set whether the mapping is to selected or unselected elements.
     * 
     * @param useSelected
     *            whether the subset is the selected or unselected elements.
     */
    public void setSelectionMode(boolean useSelected) {
	if (this.useSelected != useSelected) {
	    this.useSelected = useSelected;
	    makeIndex();
	}
    }

    /**
     * Get whether the mapping is to selected or unselected elements.
     * 
     * @return whether the subset is the selected or unselected elements.
     */
    public boolean getSelectionMode() {
	return useSelected;
    }

    private void makeIndex() {
	int mn = lsm.getMinSelectionIndex();
	int mx = lsm.getMaxSelectionIndex();
	if (srcToDst == null || srcToDst.length != srcSize) {
	    srcToDst = new int[srcSize];
	}
	int dst[] = new int[srcSize];
	dstSize = 0;
	for (int i = 0; i < srcSize; i++) {
	    if (lsm.isSelectedIndex(i) == useSelected) {
		srcToDst[i] = dstSize;
		dst[dstSize] = i;
		dstSize++;
	    } else {
		srcToDst[i] = -1;
	    }
	}
	if (dstSize == srcSize) {
	    dstToSrc = dst;
	} else {
	    dstToSrc = new int[dstSize];
	    System.arraycopy(dst, 0, dstToSrc, 0, dstSize);
	}
    }

    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e
     *            the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
	if (!e.getValueIsAdjusting() && e.getSource() == lsm) {
	    makeIndex();
	}
    }

    @Override
    public void finalize() throws Throwable {
	cleanUp();
	super.finalize();
    }

    public void cleanUp() {
	if (lsm != null) {
	    lsm.removeListSelectionListener(this);
	    lsm = null;
	}
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
    public void setSize(int srcSize) {
	if (this.srcSize != srcSize) {
	    this.srcSize = srcSize;
	    makeIndex();
	}
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
	return dstToSrc != null && dstIndex < dstToSrc.length ? dstToSrc[dstIndex]
		: -1;
    }

    /**
     * Return the destination indices for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination indices mapped from the source index.
     */
    public int getDst(int srcIndex) {
	return srcToDst != null && srcIndex < srcToDst.length ? srcToDst[srcIndex]
		: -1;
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
