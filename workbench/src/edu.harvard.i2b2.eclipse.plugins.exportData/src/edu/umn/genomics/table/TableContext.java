/*
 * @(#) $RCSfile: TableContext.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * A TableContext manages TableModels and any subtables, views, or selections
 * related to those tables. The managed objects are maintained in a TreeModel
 * that may be viewed by JTree.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.tree.TreeModel
 * @see javax.swing.ListSelectionModel
 */
public interface TableContext {

    /**
     * Get a list of all registered view class names.
     * 
     * @return A list of all registered view class names.
     */
    public String[] getViewNames();

    /**
     * Add a TableModel to the tree of managed tables.
     * 
     * @param tm
     *            The TableModel to add.
     */
    public void addTableModel(TableModel tm);

    /**
     * Remove a TableModel from the tree of managed tables.
     * 
     * @param tm
     *            The TableModel to remove.
     */
    public void removeTableModel(TableModel tm);

    /**
     * Get a VirtualTableModel for the given TableModel. If the given TableModel
     * is a VirtualTableModel, it will be registered and returned. This routine
     * is used to guarantee that all references to a TableModel are the same.
     * 
     * @param tm
     *            the TableModel for which to return a VirtualTableModel.
     * @return The VirtualTableModel for the given tm.
     */
    public VirtualTableModel getVirtualTableModel(TableModel tm);

    /**
     * Get a new TableModel that contains the selected rows in the given
     * TableModel. The selections will be mapped so that the selection of rows
     * for one TableModel will be reflected in the other TableModel. The new
     * TableModel is added as a child tree node to the given TableModel.
     * 
     * @param tm
     *            the TableModel from which to derive a new TableModel
     * @param rows
     *            the selected rows of the given TableModel to include in the
     *            new TableModel
     * @return The TableModel derived from the selected rows of the given
     *         TableModel.
     */
    public TableModel getTableModel(TableModel tm, ListSelectionModel rows);

    // Other derived TableModels that might be useful:
    // public TableModel getTableModel(TableModel tm, Rectangle rect);
    // public TableModel getTableModel(TableModel tm, ListSelectionModel rows,
    // ListSelectionModel cols);

    // Other possibly useful TabelModel creation methods:
    // public TableModel getRotatedTableModel(TableModel);
    // public TableModel getTableModel(Collection collection);

    /**
     * Display and manage a view of the given TableModel. This does a
     * combination of getView and addView.
     * 
     * @param tm
     * @param viewName
     *            The name of the type of view to create.
     * @return The view display component.
     */
    public JFrame getTableModelView(TableModel tm, String viewName);

    /**
     * Create a view for the given TableModel.
     * 
     * @param tm
     *            The TableModel to view.
     * @param viewName
     *            The name of the type of view to create.
     * @return A view compenent for the given TableModel.
     */
    public JComponent getView(TableModel tm, String viewName);

    /**
     * Display and manage the view component. The component is added to a Frame.
     * The display frame is added as a child tree node of the TableModel.
     * 
     * @param tm
     *            The TableModel for the view
     * @param viewName
     *            The name of the type of view to create.
     * @param jc
     *            The view compenent for the given TableModel.
     * @return The view display component.
     */
    public JFrame addView(TableModel tm, String viewName, JComponent jc);

    /**
     * Get an editor for a TableModel.
     * 
     * @param tm
     *            The TableModel to edit.
     * @return A component that displays the editor.
     */
    public JFrame getEditorFrame(TableModel tm);

    /**
     * Get the row selection model for the given TableModel.
     * 
     * @param tm
     *            The TableModel for which to get the selection model.
     * @return The selection model for this TableModel.
     */
    public ListSelectionModel getRowSelectionModel(TableModel tm);

    /**
     * Get the column selection model for the given TableModel.
     * 
     * @param tm
     *            The TableModel for which to get the selection model.
     * @return The selection model for this TableModel.
     */
    public ListSelectionModel getColumnSelectionModel(TableModel tm);

    /**
     * Return a ColumnMap for the given TableModel column.
     * 
     * @param tm
     *            The TableModel for which to get the map.
     * @param columnIndex
     *            The index of the column to map.
     * @return The ColumnMap for the given column of the TableModel
     * @see edu.umn.genomics.table.ColumnMap
     */
    public ColumnMap getColumnMap(TableModel tm, int columnIndex);

    /**
     * Return the column index in the given TableModel that this ColumnMap
     * represents.
     * 
     * @param tm
     *            The TableModel for which to get the index.
     * @param columnMap
     *            The ColumnMap for which to find the TableModel column index.
     * @return The column index in the TableModel that ColumnMap represents.
     * @see edu.umn.genomics.table.ColumnMap
     */
    public int getColumnIndex(TableModel tm, ColumnMap columnMap);

    // Other potentially useful mapping methods to consider:
    // public CellMap getMap(Collection);
    // public CellMap getMap(TableModel, Rectangle);
    // public CellMap getMap(TableModel, ListSelectionModel rows,
    // ListSelectionModel cols);
    // public RowMap getRowMap(TableModel tm, int rowIndex);

    /**
     * Get a TreeModel representation of the TableModels and views managed.
     * 
     * @return A tree of the managed TableModels and views.
     */
    public TreeModel getTreeModel();

    /**
     * Get an array of the TableModels being managed.
     * 
     * @return An array of the managed TableModels.
     */
    public TableModel[] getTableModels();

    /**
     * Get the Set of the TableModels, including VirtualTableModels, that are
     * being managed.
     * 
     * @return A Set of the managed TableModels.
     */
    public Set getTableModelList();

    /**
     * Tests if the given TableModel is being managed.
     * 
     * @return Whether the given TableModel is being managed.
     */
    public boolean hasTableModel(TableModel tm);

    /**
     * Return the SetOperator context for set operations on selections.
     * 
     * @param tm
     *            The TableModel for which to retrive the SetOperator.
     * @return The selection SetOperator context for the TableModel.
     */
    public SetOperator getSetOperator(TableModel tm);

}
