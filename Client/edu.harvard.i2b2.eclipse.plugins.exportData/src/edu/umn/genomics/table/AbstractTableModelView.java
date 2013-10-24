/*
 * @(#) $RCSfile: AbstractTableModelView.java,v $ $Revision: 1.4 $ $Date: 2008/10/29 20:58:46 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * AbstractTableModelView provides common TableModel View elements.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/29 20:58:46 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class AbstractTableModelView extends JPanel implements Serializable,
	TableModelView, CleanUp, TableModelListener, ListSelectionListener {
    protected TableContext ctx;
    protected TableModel tm = null;
    protected ListSelectionModel lsm = new DefaultListSelectionModel();

    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	}
    };

    // remap the data points on a resize.
    private ComponentAdapter ca = new ComponentAdapter() {
	// public void componentResized(ComponentEvent e) {
	// mapData();
	// }
	@Override
	public void componentShown(ComponentEvent e) {
	    addListeners();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	    cleanUp();
	}
    };

    /**
     * This adds this view as a listener to the TableModel and to the
     * ListSelectionModel. Classes overriding this method should call
     * super.addListeners();
     */
    protected void addListeners() {
	if (tm != null) {
	    tm.addTableModelListener(this);
	}
	if (lsm != null) {
	    lsm.addListSelectionListener(this);
	}
    }

    /**
     * This removes this view as a listener to the TableModel and to the
     * ListSelectionModel. Classes overriding this method should call
     * super.cleanUp();
     */
    public void cleanUp() {
	if (tm != null) {
	    tm.removeTableModelListener(this);
	}
	if (lsm != null) {
	    lsm.removeListSelectionListener(this);
	}
    }

    /**
     * Constructs a view display. Nothing will be displayed until a data model
     * is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public AbstractTableModelView() {
	this.addComponentListener(ca);
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public AbstractTableModelView(TableModel tableModel) {
	this();
	setTableModel(tableModel);
	setSelectionModel(lsm);
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public AbstractTableModelView(TableModel tableModel, ListSelectionModel lsm) {
	this();
	setSelectionModel(lsm);
	setTableModel(tableModel);
    }

    /**
     * Sets tableModel as the data model for the view.
     * 
     * @param tableModel
     *            the data model for the view
     */
    public void setTableModel(TableModel tableModel) {
	if (tm != null) {
	    tm.removeTableModelListener(this);
	}
	tm = tableModel;
	tm.addTableModelListener(this);
	repaint();
    }

    /**
     * Return the TableModel being displayed.
     * 
     * @return the table being displayed.
     * @see #setTableModel(TableModel tableModel)
     */
    public TableModel getTableModel() {
	return tm;
    }

    /**
     * Sets the row selection model for this table to newModel and registers
     * with for listener notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    public void setSelectionModel(ListSelectionModel newModel) {
	if (newModel != null && newModel != lsm) {
	    lsm = newModel;
	    lsm.addListSelectionListener(this);
	}
    }

    /**
     * Returns the ListSelectionModel that is used to maintain row selection
     * state.
     * 
     * @return the object that provides row selection state.
     */
    public ListSelectionModel getSelectionModel() {
	return lsm;
    }

    /**
     * Set the selection set operator to use.
     * 
     * @param setOperator
     *            the selection set operator to use.
     * @see SetOperator#REPLACE
     * @see SetOperator#BRUSHOVER
     * @see SetOperator#UNION
     * @see SetOperator#INTERSECTION
     * @see SetOperator#DIFFERENCE
     * @see SetOperator#XOR
     */
    public void setSetOperator(int setOperator) {
	ctx.getSetOperator(tm).setSetOperator(setOperator);
    }

    /**
     * Return the selection set operator being used.
     */
    public int getSetOperator() {
	return ctx.getSetOperator(tm).getSetOperator();
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    public Component getCanvas() {
	return this;
    }

    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @param ctx
     *            The context to use for TableModels and Views.
     */
    public void setTableContext(TableContext ctx) {
	this.ctx = ctx;
    }

    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @return The context to use for TableModels and Views.
     */
    public TableContext getTableContext() {
	return ctx;
    }

    /**
     * Called whenever the TableModel structure or data changes, this method
     * does nothing and should be overridden by extending classes that wish to
     * receive these events. The TableModelEvent should be constructed in the
     * coordinate system of the model.
     * 
     * @param e
     *            the change to the data model
     */
    public void tableChanged(TableModelEvent e) {
    }

    /**
     * Called whenever the value of the selection changes, this method does
     * nothing and should be overridden by extending classes that wish to
     * receive these events.
     * 
     * @param e
     *            the event that characterizes the change in selection.
     */
    public void valueChanged(ListSelectionEvent e) {
    }

    /**
     * Set the view to display the columns at the TableModel columns indices
     * (numbered from 0 to number of columns - 1). This implementation does
     * nothing, extending classes should override this method.
     * 
     * @param columns
     *            the indices of the columns to display.
     */
    public void setColumns(int[] columns) {
    }

    /**
     * Set the view to display the named columns of the TableModel. This
     * implementation uses TableModel.getColumnName(int columnIndex) to find the
     * column index for each name, then calls setColumns(int[] columns).
     * 
     * @param columns
     *            the indices of the columns to display.
     */
    public void setColumns(String[] columns) {
	if (columns != null) {
	    int[] colIndex = new int[columns.length];
	    for (int i = 0; i < columns.length; i++) {
		colIndex[i] = -1;
		if (columns[i] != null) {
		    for (int c = 0; c < tm.getColumnCount(); c++) {
			if (columns[i].equals(tm.getColumnName(c))) {
			    colIndex[i] = c;
			    break;
			}
		    }
		}
	    }
	    setColumns(colIndex);
	}
    }

    /**
     * Return the column index in the TableModel that this ColumnMap represents.
     * 
     * @param columnMap
     *            The ColumnMap for which to find the TableModel column index.
     * @return The column index in the TableModel that ColumnMap represents.
     * @see edu.umn.genomics.table.ColumnMap
     */
    public int getColumnIndex(ColumnMap columnMap) {
	if (ctx != null) {
	    return ctx.getColumnIndex(tm, columnMap);
	}
	return -1;
    }

}
