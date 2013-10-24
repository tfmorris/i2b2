/*
 * @(#) $RCSfile: DvWrapper.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import javax.swing.table.*;
import edu.umn.genomics.table.dv.*;

/**
 * DvWrapper provides common TableModel View elements for 3D viewing.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see Java3DView
 * @see Plot3DView
 * @see ColumnMap
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public abstract class DvWrapper extends AbstractTableModelView implements
	Serializable {
    DataView dv = null;
    DataMap dataMap = null;

    /**
     * Constructs a view display. Nothing will be displayed until a data model
     * is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public DvWrapper() {
	super();
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public DvWrapper(TableModel tableModel) {
	super(tableModel);
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
    public DvWrapper(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
    }

    /**
     * Adds a DataMap for this TableModel to the DataView.
     */
    protected void setDataMap() {
	if (dv != null) {
	    if (dataMap != null) {
		dv.deleteDataMap(dataMap);
		dataMap = null;
	    }
	    if (tm != null) {
		dataMap = dv.addDataMap(tm.toString(), tm);
		if (lsm != null) {
		    dataMap.setSelectionModel(lsm);
		}
	    }
	}
	repaint();
    }

    /**
     * Sets tableModel as the data model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	super.setTableModel(tableModel);
	setDataMap();
    }

    /**
     * Sets the row selection model for this table to newModel and registers
     * with for listener notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    @Override
    public void setSelectionModel(ListSelectionModel newModel) {
	super.setSelectionModel(newModel);
	if (dataMap != null) {
	    dataMap.setSelectionModel(newModel);
	}
    }

    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @param ctx
     *            The context to use for TableModels and Views.
     */
    @Override
    public void setTableContext(TableContext ctx) {
	super.setTableContext(ctx);
	if (ctx != null && dv != null) {
	    dv.setSetOperatorModel(ctx.getSetOperator(tm));
	}
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
    @Override
    public void setSetOperator(int setOperator) {
	super.setSetOperator(setOperator);
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return dv != null ? dv.getCanvas() : null;
    }

    /**
     * This removes this view as a listener to the TableModel and to the
     * ListSelectionModel. Classes overriding this method should call
     * super.cleanUp();
     */
    @Override
    public void cleanUp() {
	if (dataMap != null) {
	    dataMap.cleanUp();
	}
	super.cleanUp();
    }
}
