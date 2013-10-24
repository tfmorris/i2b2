/*
 * @(#) $RCSfile: IndexSelectDrawable.java,v $ $Revision: 1.3 $ $Date: 2008/10/29 20:59:03 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.graph.swing;

import javax.swing.ListSelectionModel;
import edu.umn.genomics.graph.Drawable;
import edu.umn.genomics.graph.IndexedDrawable;

/**
 * Associates a Drawable for each index value based on whether the index is
 * selected in a ListSelectionModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/29 20:59:03 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class IndexSelectDrawable implements IndexedDrawable {
    Drawable selectDrawable;
    ListSelectionModel lsm;
    IndexedDrawable indexedDrawable;

    public IndexSelectDrawable() {
    }

    public IndexSelectDrawable(Drawable selectDrawable,
	    IndexedDrawable indexedDrawable, ListSelectionModel lsm) {
	setSelectDrawable(selectDrawable);
	setSelectionModel(lsm);
	setIndexedDrawable(indexedDrawable);
    }

    /**
     * Set the Drawable to return for a selected index.
     * 
     * @param drawable
     *            for selected indices.
     */
    public void setSelectDrawable(Drawable drawable) {
	this.selectDrawable = drawable;
    }

    /**
     * Return the color of the graph item.
     * 
     * @return drawable of the graph item.
     */
    public Drawable getSelectDrawable() {
	return selectDrawable;
    }

    /**
     * Sets the selection model newModel and registers with for listener
     * notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    public void setSelectionModel(ListSelectionModel newModel) {
	lsm = newModel;
    }

    /**
     * Returns the ListSelectionModel that is used to maintain the selection
     * state.
     * 
     * @return the object that provides row selection state.
     */
    public ListSelectionModel getSelectionModel() {
	return lsm;
    }

    /**
     * Set the indexed drawable of the graph item, which provides a drawable at
     * each data point in this graph item.
     * 
     * @param indexedDrawable
     *            drawable for this graph item.
     */
    public void setIndexedDrawable(IndexedDrawable indexedDrawable) {
	this.indexedDrawable = indexedDrawable;
    }

    /**
     * Return the indexed drawable of the graph item.
     * 
     * @return drawable of the graph item.
     */
    public IndexedDrawable getIndexedDrawable() {
	return indexedDrawable;
    }

    /**
     * Returns the drawable at the specified index.
     * 
     * @param index
     *            the index into the drawable list.
     * @return the drawable at the specified index.
     */
    public Drawable get(int index) {
	if (lsm != null && selectDrawable != null) {
	    if (lsm.isSelectedIndex(index)) {
		return selectDrawable;
	    }
	}
	if (indexedDrawable != null) {
	    return indexedDrawable.get(index);
	}
	return null;
    }
}
