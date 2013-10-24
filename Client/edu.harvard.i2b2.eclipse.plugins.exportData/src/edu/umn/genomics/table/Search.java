/*
 * @(#) $RCSfile: Search.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.*;
import javax.swing.table.*;

/**
 * SearchSelect sets a ListSelectionModel for items that match a Regular
 * Expression.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class Search {

    /**
     * Apply the given selection to the current selection lsm for this column,
     * using the set operation designated by the setOperator.
     * 
     * @param tableModel
     *            The table to search.
     * @param columnIndex
     *            The table column to search
     * @param selectionModel
     *            the ListSelectionModel to which selections will be recorded.
     * @param regex
     *            The regular expression to search for.
     * @return The selectionModel in which selections are recorded.
     */
    public static ListSelectionModel select(TableModel tableModel,
	    int columnIndex, ListSelectionModel selectionModel, String regex) {
	ListSelectionModel lsm = selectionModel != null ? selectionModel
		: new DefaultListSelectionModel();
	if (columnIndex < 0 || columnIndex >= tableModel.getColumnCount()) {
	} else {
	    lsm.setValueIsAdjusting(true);
	    for (int r = 0; r < tableModel.getRowCount(); r++) {
		boolean match = false;
		Object obj = tableModel.getValueAt(r, columnIndex);
		if (obj != null && regex != null) {
		    if (obj.toString().matches(regex)) {
			match = true;
		    }
		} else if (obj == null && regex == null) {
		    match = true;
		}
		if (match) {
		    lsm.addSelectionInterval(r, r);
		} else {
		    lsm.removeSelectionInterval(r, r);
		}
	    }
	    lsm.setValueIsAdjusting(false);
	}
	return lsm;
    }

    /**
     * Apply the given selection to the current selection lsm for this column,
     * using the set operation designated by the setOperator.
     * 
     * @param selectionModel
     *            the current ListSelectionModel to which the change will be
     *            applied.
     * @param selection
     *            the selected rows to combine with the current selection
     * @param setOperator
     *            The set operation used to combine the selections
     * @return The selectionModel parameter.
     */
    public static ListSelectionModel select(ListSelectionModel selectionModel,
	    ListSelectionModel selection, int setOperator) {
	ListSelectionModel lsm = selectionModel != null ? selectionModel
		: new DefaultListSelectionModel();
	if (selection != null) {
	    int min = selection.getMinSelectionIndex();
	    int max = selection.getMaxSelectionIndex();
	    lsm.setValueIsAdjusting(true);
	    switch (setOperator) {
	    case SetOperator.UNION:
		if (min < 0)
		    break;
		for (int r = min; r <= max; r++) {
		    if (selection.isSelectedIndex(r)) {
			lsm.addSelectionInterval(r, r);
		    }
		}
		break;
	    case SetOperator.DIFFERENCE:
		if (min < 0 || lsm.getMinSelectionIndex() < 0)
		    break;
		int cmax = lsm.getMaxSelectionIndex();
		for (int r = Math.max(min, lsm.getMinSelectionIndex()); r <= Math
			.min(max, cmax); r++) {
		    if (selection.isSelectedIndex(r) && lsm.isSelectedIndex(r)) {
			lsm.removeSelectionInterval(r, r);
		    }
		}
		break;
	    case SetOperator.INTERSECTION:
		if (lsm.getMinSelectionIndex() < 0)
		    break;
		min = lsm.getMinSelectionIndex();
		max = lsm.getMaxSelectionIndex();
		for (int r = min; r <= max; r++) {
		    if (!selection.isSelectedIndex(r)) {
			lsm.removeSelectionInterval(r, r);
		    }
		}
		break;
	    case SetOperator.XOR:
		if (lsm.getMinSelectionIndex() < 0) {
		    for (int r = min; r <= max; r++) {
			if (!selection.isSelectedIndex(r)) {
			    lsm.addSelectionInterval(r, r);
			}
		    }
		    break;
		}
		for (int r = min; r <= max; r++) {
		    if (selection.isSelectedIndex(r)) {
			if (lsm.isSelectedIndex(r)) {
			    lsm.removeSelectionInterval(r, r);
			} else {
			    lsm.addSelectionInterval(r, r);
			}
		    }
		}
		break;
	    case SetOperator.BRUSHOVER:
	    case SetOperator.REPLACE:
		lsm.clearSelection();
		if (min < 0)
		    break;
		for (int r = min; r <= max; r++) {
		    if (selection.isSelectedIndex(r)) {
			lsm.addSelectionInterval(r, r);
		    }
		}
		break;
	    }
	    lsm.setValueIsAdjusting(false);
	}
	return lsm;
    }

}
