/*
 * @(#) $RCSfile: SelectableCellMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * SelectableCellMap maps the values of a Collection of values to a numeric
 * range. If the common Class type of the Collection is Number or Date, or if
 * all of the * values of the Collection are Strings that can be parsed as class
 * Number or Date, * the range will be from the minimum to the maximum of the
 * values in the Collection. If any value can not be parsed as class Number or
 * Date, the values will be mapped to integral values from 0 to the number of
 * distinct values - 1, (distinct values are determined by the value's equals
 * method.) The CellMap can also be used to select a range of mapped values from
 * the Collection and indicate the selected indices in a ListSelectionModel. A
 * new selection is be specified as a subrange of values between the the minimum
 * and maximum values of the collection. New selections may be combined with the
 * previous selections using standard set operators.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public interface SelectableCellMap extends CellMap {
    /**
     * Sets the row selection model for this table to newModel and registers
     * with for listener notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    public void setSelectionModel(ListSelectionModel newModel);

    /**
     * Returns the ListSelectionModel that is used to maintain row selection
     * state.
     * 
     * @return the object that provides row selection state.
     */
    public ListSelectionModel getSelectionModel();

    /**
     * Set the selection set operator to use when combining the current set of
     * selected values with the previous set of selected values.
     * 
     * @see SetOperator#REPLACE
     * @see SetOperator#BRUSHOVER
     * @see SetOperator#UNION
     * @see SetOperator#INTERSECTION
     * @see SetOperator#DIFFERENCE
     * @see SetOperator#XOR
     */
    public void setSetOperator(int setOperator);

    /**
     * Return the selection set operator in effect for combining a new set of
     * selected values with the previous set of selected values.
     * 
     * @return the current selection set operator being used
     */
    public int getSetOperator();

    /**
     * Select the indices that are mapped in the given range of this map. The
     * selected indices are set in the given ListSelectionModel.
     * 
     * @param from
     *            the start of the selected range
     * @param to
     *            the end of the selected range
     * @param sm
     *            the ListSelectionModel in which to set the rows indices.
     */
    public void selectRange(double from, double to, ListSelectionModel sm);

    /**
     * Select the indices that are mapped in the given range of this map. The
     * selected rows are set in the ListSelection Model that was set for this
     * map.
     * 
     * @param from
     *            the start of the selected range
     * @param to
     *            the end of the selected range
     * @see #setSetOperator
     * @see #setSelectionModel
     */
    public void selectRange(double from, double to);

    /**
     * Apply the given selection to the current selection for this collection,
     * using the setOperator currently in effect.
     * 
     * @param selection
     *            the selected rows to combine with the previous selection
     * @see #setSetOperator
     * @see #setSelectionModel
     */
    public void selectValues(ListSelectionModel selection);
}
