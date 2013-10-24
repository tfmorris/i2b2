/*
 * @(#) $RCSfile: ColumnMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.table.*;

/**
 * ColumnMap maps the values of a column in a TableModel to a numeric range. If
 * the common Class type of the Column is Number or Date, or if all of the *
 * values of the Column are Strings that can be parsed as class Number or Date,
 * * the range will be from the minimum to the maximum of the values in the
 * Column. If any value can not be parsed as class Number or Date, the values
 * will be mapped to integral values from 0 to the number of distinct values -
 * 1, (distinct values are determined by the value's equals method.) The CellMap
 * can also be used to select a range of mapped values from the Column and
 * indicate the selected indices in a ListSelectionModel. A new selection is be
 * specified as a subrange of values between the the minimum and maximum values
 * of the column. New selections may be combined with the previous selections
 * using standard set operators.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public interface ColumnMap extends SelectableCellMap {
    /**
     * Sets tableModel as the data model for the column being mapped.
     * 
     * @param tableModel
     *            the data model
     */
    public void setTableModel(TableModel tableModel);

    /**
     * Return the table model being displayed.
     * 
     * @return the table being displayed.
     */
    public TableModel getTableModel();

    /**
     * Return the column index that is be mapped.
     * 
     * @return the index of the column of the TableModel this maps.
     */
    public int getColumnIndex();

    /**
     * Return the Class of the column in the TableModel.
     * 
     * @return the Class of the column in the TableModel.
     */
    public Class getColumnClass();

    /**
     * Return the value for this column in the TableModel at the given rowIndex.
     * This is a convenience method for TableModel.getValueAt(int rowIndex, int
     * columnIndex), however, an implementation may choose to store the values
     * outside of the table.
     * 
     * @param rowIndex
     *            the row index in the TableModel.
     * @return the value for this column in the TableModel at the given
     *         rowIndex.
     */
    public Object getValueAt(int rowIndex);

}
