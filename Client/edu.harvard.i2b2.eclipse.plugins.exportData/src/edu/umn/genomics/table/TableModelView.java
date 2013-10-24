/*
 * @(#) $RCSfile: TableModelView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.Component;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A TableModelView displays the values of a table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public interface TableModelView {
    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @param ctx
     *            The context to use for TableModels and Views.
     */
    public void setTableContext(TableContext ctx);

    /**
     * Get the TableContext that manages TableModels and Views.
     * 
     * @return The context to use for TableModels and Views.
     */
    public TableContext getTableContext();

    /**
     * Sets tableModel as the data model for the view.
     * 
     * @param tableModel
     *            the data model for the view
     */
    public void setTableModel(TableModel tableModel);

    /**
     * Return the table model being displayed.
     * 
     * @return the table being displayed.
     */
    public TableModel getTableModel();

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
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    public Component getCanvas();

    /**
     * Set the view to display the columns at the TableModel columns indices
     * (numbered from 0 to number of columns - 1).
     * 
     * @param columns
     *            the indices of the columns to display.
     */
    public void setColumns(int[] columns);

    /**
     * Set the view to display the named columns of the TableModel.
     * 
     * @param columns
     *            the indices of the columns to display.
     */
    public void setColumns(String[] columns);

    /**
     * Return the column index in the TableModel that this ColumnMap represents.
     * 
     * @param columnMap
     *            The ColumnMap for which to find the TableModel column index.
     * @return The column index in the TableModel that ColumnMap represents.
     * @see edu.umn.genomics.table.ColumnMap
     */
    public int getColumnIndex(ColumnMap columnMap);
}
