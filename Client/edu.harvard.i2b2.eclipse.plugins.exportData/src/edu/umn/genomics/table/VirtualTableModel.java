/*
 * @(#) $RCSfile: VirtualTableModel.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.ListModel;
import javax.swing.table.TableModel;

/**
 * VirtualTableModel provides an interface to hide columns, reorder columns, or
 * add VirtualColumns of formulas in a TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
public interface VirtualTableModel extends TableModel {
    /**
     * Return the TableModel being referenced.
     * 
     * @return the table being displayed.
     */
    public TableModel getTableModel();

    /**
     * Give a name to this instance.
     * 
     * @param name
     *            a name for this VirtualTableModelProxy.
     * @see #getName()
     */
    public void setName(String name);

    /**
     * Give a name to this instance, which will be returned toString().
     * 
     * @see #setName(String name)
     */
    public String getName();

    /**
     * Add a formula column to this VirtualTableModel.
     * 
     * @param columnName
     *            the name given this column.
     * @param formula
     *            a formula to generate values for this column.
     */
    public void addColumn(String columnName, TableModelFormula formula);

    /**
     * Insert a formula column to this VirtualTableModel.
     * 
     * @param formula
     *            a formula to generate values for this column.
     * @param index
     *            at which to add the Column.
     */
    public void addColumn(TableModelFormula formula, int index);

    /**
     * Add a column of the source TableModel to this VirtualTableModel.
     * 
     * @param columnName
     *            the name given this column.
     * @param columnIndex
     *            the index of the TableModel to add.
     */
    public void addColumn(String columnName, int columnIndex);

    /**
     * Add a column of the source TableModel to this VirtualTableModel.
     * 
     * @param columnIndex
     *            the index of the TableModel to add.
     */
    public void addColumn(int columnIndex);

    /**
     * Add a column of the source TableModel to this VirtualTableModel.
     * 
     * @param col
     *            The column to add to the TableModel.
     */
    public void addColumn(VirtualColumn col);

    /**
     * Insert a column to this VirtualTableModel.
     * 
     * @param column
     *            the Column of the TableModel to add.
     * @param index
     *            at which to add the Column.
     */
    public void addColumn(VirtualColumn column, int index);

    /**
     * Remove the column of this VirtualTableModel at the index.
     * 
     * @param columnIndex
     *            the index of the column to remove.
     */
    public void removeColumn(int columnIndex);

    /**
     * Remove the columns of this VirtualTableModel at the indices.
     * 
     * @param columnIndices
     *            the index of the column to remove.
     */
    public void removeColumns(int[] columnIndices);

    /**
     * Returns a list of the VirtualColumns that comprise the TableModel view
     * presented by this VirtualTableModel.
     * 
     * @return a list of the VirtualColumns.
     */
    public ListModel getColumnList();

    /**
     * Returns a list of the VirtualColumns that comprise the TableModel
     * referenced by this VirtualTableModel.
     * 
     * @return a list of the VirtualColumns from the referenced TableModel.
     */
    public ListModel getDefaultColumnList();

    /**
     * Returns a list of the TableModelFormulas in this VirtualTableModel.
     * 
     * @return a list of the TableModelFormulas in this VirtualTableModel.
     */
    public ListModel getFormulaList();

    /**
     * Set the name of the column at <code>columnIndex</code>.
     * 
     * @param name
     *            the name of the column
     * @param columnIndex
     *            the index of the column
     */
    public void setColumnName(String name, int columnIndex);

}
