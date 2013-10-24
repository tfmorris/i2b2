/*
 * @(#) $RCSfile: IndexSelectColor.java,v $ $Revision: 1.3 $ $Date: 2008/10/29 20:59:03 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.Color;
import javax.swing.ListSelectionModel;
import edu.umn.genomics.graph.IndexedColor;

/**
 * Associates a Color value for each index value based on whether the index is
 * selected in a ListSelectionModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/29 20:59:03 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.ListSelectionModel
 */
public class IndexSelectColor implements IndexedColor {
    Color selectColor;
    ListSelectionModel lsm;
    IndexedColor indexedColor;

    public IndexSelectColor() {
    }

    public IndexSelectColor(Color selectColor, IndexedColor indexedColor,
	    ListSelectionModel lsm) {
	setSelectColor(selectColor);
	setSelectionModel(lsm);
	setIndexedColor(indexedColor);
    }

    /**
     * Set the Color to return for a selected index.
     * 
     * @param color
     *            for selected indices.
     */
    public void setSelectColor(Color color) {
	this.selectColor = color;
    }

    /**
     * Return the color of the graph item.
     * 
     * @return color of the graph item.
     */
    public Color getSelectColor() {
	return selectColor;
    }

    /**
     * Returns the length of the list.
     */
    public int getSize() {
	return Integer.MAX_VALUE;
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
     * Set the indexed color of the graph item, which will provide a color for
     * each drawable in this graph item.
     * 
     * @param indexedColor
     *            color for this graph item.
     */
    public void setIndexedColor(IndexedColor indexedColor) {
	this.indexedColor = indexedColor;
    }

    /**
     * Return the indexed color of the graph item.
     * 
     * @return color of the graph item.
     */
    public IndexedColor getIndexedColor() {
	return indexedColor;
    }

    /**
     * Returns the color at the specified index.
     * 
     * @param index
     *            the index into the color list.
     * @return the color at the specified index.
     */
    public Color getColorAt(int index) {
	if (lsm != null && selectColor != null) {
	    if (lsm.isSelectedIndex(index)) {
		return selectColor;
	    }
	}
	if (indexedColor != null) {
	    return indexedColor.getColorAt(index);
	}
	return null;
    }
}
