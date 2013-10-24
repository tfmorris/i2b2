/*
 * @(#) $RCSfile: AbstractTableSource.java,v $ $Revision: 1.4 $ $Date: 2008/10/31 15:49:03 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Associates a named table data source and the TableModel interface to that
 * data source.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/31 15:49:03 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class AbstractTableSource extends JPanel implements TableSource {
    private EventListenerList listenerList = new EventListenerList();
    protected String tableSource;
    protected TableModel tableModel;

    /**
     * Set the source name for the given TableModel.
     * 
     * @param tableModel
     *            A TableModel interface to the named data source.
     * @param tableSource
     *            A name for this data source.
     */
    protected void setTableSource(TableModel tableModel, String tableSource) {
	this.tableModel = tableModel;
	this.tableSource = tableSource;
	fireChangeEvent();
    }

    /**
     * Return the name for the source of the data table.
     * 
     * @return The source of the data table
     */
    public String getTableSource() {
	return tableSource;
    }

    /**
     * Return a TableModel for the data source.
     * 
     * @return The TableModel for the data source, or null if not available.
     */
    public TableModel getTableModel() {
	return tableModel;
    }

    /**
     * Adds the listener to be notified of changes to the data source.
     * 
     * @param listener
     *            the ChangeListener to add
     */
    public void addChangeListener(ChangeListener listener) {
	listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes the listener from the notification list.
     * 
     * @param listener
     *            the ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener listener) {
	listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Notify Listeners of change.
     */
    protected void fireChangeEvent() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	ChangeEvent changeEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ChangeListener.class) {
		// Lazily create the event:
		if (changeEvent == null)
		    changeEvent = new ChangeEvent(this);
		((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
	    }
	}
    }
}
