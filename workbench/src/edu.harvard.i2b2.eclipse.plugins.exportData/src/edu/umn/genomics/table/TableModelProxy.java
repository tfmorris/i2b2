/*
 * @(#) $RCSfile: TableModelProxy.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.event.*;
import javax.swing.table.*;
import java.text.*;

/**
 * A TableModelProxy object provides a proxy to a TableModel. This allows
 * objects to have a constant reference to a TableModel instance, but have the
 * actual TableModel supplying the data to change.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
public class TableModelProxy implements Serializable, TableModel,
	TableModelListener {
    TableModel tm = null;
    protected EventListenerList listenerList = new EventListenerList();

    public TableModelProxy() {
    }

    public TableModelProxy(TableModel tableModel) {
	setTableModel(tableModel);
    }

    public void setTableModel(TableModel tableModel) {
	if (tm != null) {
	    tm.removeTableModelListener(this);
	}
	this.tm = tableModel;
	tm.addTableModelListener(this);
	tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    public TableModel getTableModel() {
	return tm;
    }

    public int getRowCount() {
	if (tm == null)
	    return 0;
	return tm.getRowCount();
    }

    public int getColumnCount() {
	if (tm == null)
	    return 0;
	return tm.getColumnCount();
    }

    public String getColumnName(int columnIndex) {
	if (tm == null)
	    return null;
	return tm.getColumnName(columnIndex);
    }

    public Class getColumnClass(int columnIndex) {
	if (tm == null)
	    return null;
	return tm.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (tm == null)
	    return false;
	return tm.isCellEditable(rowIndex, columnIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
	if (tm == null)
	    return null;
	return tm.getValueAt(rowIndex, columnIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (tm == null)
	    return;
	Object o = tm.getValueAt(rowIndex, columnIndex);
	if (o instanceof Number && aValue instanceof String) {
	    DecimalFormat df = new DecimalFormat();
	    ParsePosition pp = new ParsePosition(0);
	    // DecimalFormat needs uppercase E for exponent
	    String ns = ((String) aValue).replace('e', 'E');
	    Number n = df.parse(ns, pp);
	    // DecimalFormat doesn't accept E+2 it must be E2
	    if (pp.getIndex() < ns.length()
		    && ns.regionMatches(pp.getIndex(), "E+", 0, 2)) {
		ns = ns.substring(0, pp.getIndex() + 1)
			+ ns.substring(pp.getIndex() + 2);
		pp.setIndex(0);
		n = df.parse(ns, pp);
	    }
	    // Now check if entire token could be parsed as a Number
	    if (pp.getIndex() >= ns.length()) {
		tm.setValueAt(n, rowIndex, columnIndex);
		return;
	    }
	}
	tm.setValueAt(aValue, rowIndex, columnIndex);
    }

    public void addTableModelListener(TableModelListener l) {
	listenerList.add(TableModelListener.class, l);
    }

    public void removeTableModelListener(TableModelListener l) {
	listenerList.remove(TableModelListener.class, l);
    }

    public void tableChanged(TableModelEvent e) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == TableModelListener.class) {
		((TableModelListener) listeners[i + 1]).tableChanged(e);
	    }
	}
    }
}
