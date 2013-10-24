/*
 * @(#) $RCSfile: MergeTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2003. The Regents of the University of Minnesota  
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
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Presents a set of TableModels which have identical columnClasses as a single
 * TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
public class MergeTableModel extends AbstractTableModel implements
	TableModelListener {
    /** Add an initial column that identifies the table this row is from. */
    static String TABLE_COLUMN_NAME = "TableName";
    /** A count of added initial columns. */
    static int PSEUDO_COLUMNS = 1;
    /** A list of table that comprise this set. */
    Vector table = new Vector();
    /**
     * The offset to the first row of each table, allows for tables of differing
     * row counts.
     */
    int[] tableRowOffsets = null; // length is the number of tables + 1

    /**
     * Construct a TableModel that consolidates like TableModels.
     */
    public MergeTableModel() {
	super();
    }

    /**
     * Add a TableModel to this set.
     * 
     * @param tableModel
     *            The table to add.
     * @exception NullPointerException
     *                If tableModel is null.
     * @exception IndexOutOfBoundsException
     *                If the column count of the tableModel does not match the
     *                column count of the existing tables.
     * @exception ClassCastException
     *                If the column classes of the tableModel do not match the
     *                column classes of the existing tables.
     */
    public void addTableModel(TableModel tableModel)
	    throws NullPointerException, IndexOutOfBoundsException,
	    ClassCastException {
	// check for null
	if (tableModel == null) {
	    throw new NullPointerException(
		    "Attempted to add a null TableModel to MergeTableModel");
	}
	if (getTableCount() > 0) {
	    // check if this tableModel is already in list
	    if (table.contains(tableModel)) {
		return;
	    }
	    // check that this matches the prototype table
	    matchColumns(getTableAt(0), tableModel);
	}
	int tblIndex = table.size();
	// add to table vector
	table.add(tableModel);
	// add listener
	tableModel.addTableModelListener(this);
	// update table row counts
	updateRowOffsets();
	int tmp[] = tableRowOffsets;
	int firstRow = tmp[tblIndex];
	int lastRow = tmp[tblIndex + 1] - 1;
	fireTableRowsInserted(firstRow, lastRow);
    }

    /**
     * Remove the TableModel from this set.
     * 
     * @param tableModel
     *            The table to remove.
     */
    public void removeTableModel(TableModel tableModel) {
	int tmp[] = tableRowOffsets;
	int tblIndex = table.indexOf(tableModel);
	if (tblIndex >= 0) {
	    int firstRow = tmp[tblIndex];
	    int lastRow = tmp[tblIndex + 1] - 1;
	    // remove listener
	    tableModel.removeTableModelListener(this);
	    // remove from table vector
	    table.remove(tblIndex);
	    // update table row counts
	    updateRowOffsets();
	    // fireTableRowsDeleted(int firstRow, int lastRow);
	    fireTableRowsDeleted(firstRow, lastRow);
	}
    }

    /**
     * Return the number of tables this consolidates.
     * 
     * @return the number of tables in this set.
     */
    public int getTableCount() {
	return table != null ? table.size() : 0;
    }

    /**
     * Return a list of TableModels that this consolidates.
     * 
     * @return the list of tables in this set.
     */
    public List getTableList() {
	return table;
    }

    /**
     * Return an array of TableModels that this consolidates.
     * 
     * @return the array of tables in this set.
     */
    public TableModel[] getTables() {
	return (TableModel[]) table.toArray(new TableModel[table.size()]);
    }

    /**
     * Return the TableModel at the index.
     * 
     * @return the table at the index, or null if the index is out of range.
     */
    public TableModel getTableAt(int tableIndex) {
	return (TableModel) (table != null ? table.get(tableIndex) : null);
    }

    /**
     * Return the display name for the TableModel at the index.
     * 
     * @return the display name for the table at the index.
     */
    public String getTableNameAt(int tableIndex) {
	return "table_" + (tableIndex + 1);
    }

    /**
     * Returns whether the cell in the referenced table at the given row and
     * column is editable.
     * 
     * @param tableIndex
     *            the index of the referenced table
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return whether the cell in the table at the given row and column is
     *         editable. see #setValueAt
     */
    public boolean isCellEditable(int tableIndex, int rowIndex, int columnIndex) {
	TableModel tm = getTableAt(tableIndex);
	if (tm != null) {
	    return tm.isCellEditable(rowIndex, columnIndex);
	}
	return false;
    }

    /**
     * Sets the value of the cell at the given row and column of the referenced
     * table.
     * 
     * @param aValue
     *            the new value for the cell
     * @param tableIndex
     *            the index of the referenced table
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried see
     *            #isCellEditable(int,int,int) see #getValueAt(int,int,int)
     */
    public void setValueAt(Object aValue, int tableIndex, int rowIndex,
	    int columnIndex) {
	TableModel tm = getTableAt(tableIndex);
	if (tm != null && tm.isCellEditable(rowIndex, columnIndex)) {
	    tm.setValueAt(aValue, rowIndex, columnIndex);
	}
    }

    /**
     * Gets the value of the cell at the given row and column of the referenced
     * table.
     * 
     * @param tableIndex
     *            the index of the referenced table
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return The value of the cell in the referenced table at the given row
     *         and column. see #setValueAt(Object,int,int,int)
     */
    public Object getValueAt(int tableIndex, int rowIndex, int columnIndex) {
	TableModel tm = getTableAt(tableIndex);
	if (tm != null) {
	    return tm.getValueAt(rowIndex, columnIndex);
	}
	return null;
    }

    private void updateRowOffsets() {
	int[] tmp = new int[getTableCount() + 1];
	tmp[0] = 0;
	for (int i = 1, n = 0; i < tmp.length; i++) {
	    n += getTableAt(i - 1).getRowCount();
	    tmp[i] = n;
	}
	tableRowOffsets = tmp;
    }

    private int getTableForRow(int rowIndex) {
	int[] tmp = tableRowOffsets;
	if (tmp != null && rowIndex >= 0 && rowIndex < tmp[tmp.length - 1]) {
	    for (int i = 0, j = tmp.length - 1; i < j;) {
		int mid = i + (j - i) / 2;
		if (rowIndex >= tmp[mid]) {
		    if (rowIndex < tmp[mid + 1]) {
			return mid;
		    }
		    i = mid;
		} else {
		    j = mid;
		}
	    }
	}
	return -1;
    }

    private int getTableRow(int rowIndex) {
	int[] tmp = tableRowOffsets;
	int tbl = getTableForRow(rowIndex);
	if (tbl >= 0 && tbl < tmp.length - 1) {
	    return rowIndex - tmp[tbl];
	}
	return -1;
    }

    private int getTableCol(int columnIndex) {
	return columnIndex - PSEUDO_COLUMNS;
    }

    private boolean matchColumns(TableModel tm1, TableModel tm2)
	    throws NullPointerException, IndexOutOfBoundsException,
	    ClassCastException {
	// check for null;
	if (tm1 == null || tm2 == null) {
	    throw new NullPointerException("Can'y match " + tm1 + " to " + tm2);
	}
	// column counts
	if (tm1.getColumnCount() != tm2.getColumnCount()) {
	    throw new IndexOutOfBoundsException(
		    "Tables have different number of columns: "
			    + tm1.getColumnCount() + " != "
			    + tm2.getColumnCount());
	}
	// column classes
	for (int ci = 0; ci < tm1.getColumnCount(); ci++) {
	    if (java.lang.Number.class.isAssignableFrom(tm1.getColumnClass(ci))
		    && java.lang.Number.class.isAssignableFrom(tm2
			    .getColumnClass(ci))) {
		continue;
	    }
	    if (!tm1.getColumnClass(ci)
		    .isAssignableFrom(tm2.getColumnClass(ci))) {
		throw new ClassCastException("Table Columns " + ci
			+ " don't match: " + tm1.getColumnClass(ci) + " vs "
			+ tm2.getColumnClass(ci));
	    }
	}
	return true;
    }

    // TableModelListener Interface
    // inherit javadoc from super
    public void tableChanged(TableModelEvent e) {
	int tblIndex = table.indexOf(e.getSource());
	if (tblIndex >= 0) {
	    int tmp[] = tableRowOffsets;
	    if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
		if (getTableCount() < 2) {
		    updateRowOffsets();
		    fireTableStructureChanged();
		} else {
		    int ti = tblIndex > 0 ? 0 : 1;
		    try {
			matchColumns(getTableAt(ti), getTableAt(tblIndex));
		    } catch (Exception ex) {
			removeTableModel((TableModel) e.getSource());
		    }
		}
		return;
	    }
	    int firstRow = tmp[tblIndex] + e.getFirstRow();
	    int lastRow = tmp[tblIndex]
		    + (e.getType() != TableModelEvent.UPDATE
			    || e.getLastRow() < ((TableModel) e.getSource())
				    .getRowCount() ? e.getLastRow()
			    : ((TableModel) e.getSource()).getRowCount() - 1);
	    updateRowOffsets();
	    if (e.getType() == TableModelEvent.INSERT) {
		fireTableRowsInserted(firstRow, lastRow);
		return;
	    } else if (e.getType() == TableModelEvent.DELETE) {
		fireTableRowsDeleted(firstRow, lastRow);
		return;
	    } else if (e.getType() == TableModelEvent.UPDATE) {
		fireTableRowsUpdated(firstRow, lastRow);
		return;
	    }
	}
    }

    // TableModel Interface
    // inherit javadoc from super
    public int getRowCount() {
	int count = 0;
	if (getTableCount() > 0) {
	    int tmp[] = tableRowOffsets;
	    count = tmp[tmp.length - 1];
	}
	return count;
    }

    // inherit javadoc from super
    public int getColumnCount() {
	if (getTableCount() > 0) {
	    return ((TableModel) table.get(0)).getColumnCount()
		    + PSEUDO_COLUMNS;
	}
	return 0;
    }

    // inherit javadoc from super
    @Override
    public String getColumnName(int columnIndex) {
	if (getTableCount() > 0) {
	    if (columnIndex == 0) {
		return TABLE_COLUMN_NAME;
	    } else {
		return getTableAt(0).getColumnName(getTableCol(columnIndex));
	    }
	}
	return "";
    }

    // inherit javadoc from super
    @Override
    public int findColumn(String columnName) {
	if (columnName != null && getTableCount() > 0) {
	    if (TABLE_COLUMN_NAME.equals(columnName)) {
		return 0;
	    }
	    for (int ci = 0; ci < getTableAt(0).getColumnCount(); ci++) {
		if (columnName.equals(getTableAt(0).getColumnName(ci))) {
		    return ci + PSEUDO_COLUMNS;
		}
	    }
	}
	return -1;
    }

    // inherit javadoc from super
    @Override
    public Class getColumnClass(int columnIndex) {
	if (getTableCount() > 0) {
	    if (columnIndex == 0) {
		return TABLE_COLUMN_NAME.getClass();
	    } else {
		Class colClass = getTableAt(0).getColumnClass(
			getTableCol(columnIndex));
		if (java.lang.Number.class.isAssignableFrom(colClass)) {
		    for (int i = 1; i < getTableCount(); i++) {
			Class nextClass = getTableAt(i).getColumnClass(
				getTableCol(columnIndex));
			if (!colClass.isAssignableFrom(nextClass)) {
			    colClass = nextClass;
			}
		    }
		}
		return colClass;
	    }
	}
	return null;
    }

    // inherit javadoc from super
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	if (getTableCount() > 0) {
	    if (columnIndex == 0) {
		return false;
	    } else {
		return isCellEditable(getTableForRow(rowIndex),
			getTableRow(rowIndex), getTableCol(columnIndex));
	    }
	}
	return false;
    }

    // inherit javadoc from super
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (getTableCount() > 0) {
	    if (columnIndex == 0) {
		return getTableAt(getTableForRow(rowIndex));
	    } else {
		return getValueAt(getTableForRow(rowIndex),
			getTableRow(rowIndex), getTableCol(columnIndex));
	    }
	}
	return null;
    }

    // inherit javadoc from super
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (getTableCount() > 0) {
	    if (columnIndex == 0) {
	    } else {
		setValueAt(aValue, getTableForRow(rowIndex),
			getTableRow(rowIndex), getTableCol(columnIndex));
	    }
	}
    }
}
