/*
 * @(#) $RCSfile: VirtualTableModelProxy.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * A VirtualTableModelProxy object provides a proxy to a TableModel, allowing a
 * view of that TableModel with reordered rows and columns, and hidden or added
 * columns. This also allows objects to have a constant reference to a
 * TableModel instance, but have the actual TableModel supplying the data to
 * change.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
public class VirtualTableModelProxy extends AbstractTableModel implements
	Serializable, VirtualTableModel, TableColumnMap {
    TableModel tm = null;
    DefaultListModel columns = new DefaultListModel();
    DefaultListModel tmColumns = new DefaultListModel();
    DefaultListModel formulas = new DefaultListModel();
    IndexMap rowIdxMap = null;
    IndexMap colIdxMap = null;
    boolean notify = true; // notify listeners of change.
    String name = null;

    TableModelListener tml = new TableModelListener() {
	// need to adjust for columns
	public void tableChanged(TableModelEvent e) {
	    if (e != null && e.getFirstRow() == TableModelEvent.HEADER_ROW) {
		getTmColumns();
		setDefaultColumns();
		fireTableStructureChanged();
	    } else {
		if (recalculateFormulas()) {
		    fireTableDataChanged();
		} else {
		    int col = e.getColumn();
		    int num = 0;
		    if (col != TableModelEvent.ALL_COLUMNS) {
			for (int i = 0; i < columns.size(); i++) {
			    VirtualCell cell = (VirtualCell) columns
				    .getElementAt(i);
			    if (cell instanceof ColRef
				    && ((ColRef) cell).getColumnIndex() == e
					    .getColumn()) {
				fireTableChanged(new TableModelEvent(
					getThisSource(), e.getFirstRow(), e
						.getLastRow(), i, e.getType()));
			    }
			}
		    } else {
			fireTableChanged(new TableModelEvent(getThisSource(), e
				.getFirstRow(), e.getLastRow(), e.getColumn(),
				e.getType()));
		    }
		}
	    }
	}
    };

    private TableModel getThisSource() {
	return this;
    }

    /**
     * Give a name to this instance.
     * 
     * @param name
     *            a name for this VirtualTableModelProxy.
     * @see #getName()
     * @see #toString()
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Give a name to this instance, which will be returned toString().
     * 
     * @see #setName(String name)
     * @see #toString()
     */
    public String getName() {
	return name;
    }

    /**
     * Return a String representation for this instance, which will be the name
     * if it has been given.
     * 
     * @see #setName(String name)
     * @see #getName()
     */
    @Override
    public String toString() {
	if (name != null)
	    return name;
	return super.toString();
    }

    /**
     * Create a new instance of VirtualTableModelProxy, call setTableModel set
     * the TableModel which this instance will reference.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public VirtualTableModelProxy() {
	columns.addListDataListener(new ListDataListener() {
	    public void intervalAdded(ListDataEvent e) {
		if (notify) {
		    recalculateFormulas();
		    fireTableStructureChanged();
		}
	    }

	    public void intervalRemoved(ListDataEvent e) {
		if (notify) {
		    recalculateFormulas();
		    fireTableStructureChanged();
		}
	    }

	    public void contentsChanged(ListDataEvent e) {
		if (notify) {
		    recalculateFormulas();
		    fireTableStructureChanged();
		}
	    }
	});
    }

    /**
     * Create a new instance of VirtualTableModelProxy references the given
     * TableModel.
     * 
     * @param tableModel
     *            the TableModel which this instance will reference.
     * @see #setTableModel(TableModel tableModel)
     */
    public VirtualTableModelProxy(TableModel tableModel) {
	this();
	setTableModel(tableModel);
    }

    /**
     * Create a new instance of VirtualTableModelProxy references the given
     * TableModel.
     * 
     * @param tableModel
     *            the TableModel which this instance will reference.
     * @param name
     *            a name for this VirtualTableModelProxy.
     * @see #setTableModel(TableModel tableModel)
     * @see #setName(String name)
     */
    public VirtualTableModelProxy(TableModel tableModel, String name) {
	this(tableModel);
	setName(name);
    }

    /**
     * Sets the TableModel to reference.
     * 
     * @param tableModel
     *            the data model for the view
     */
    public void setTableModel(TableModel tableModel) {
	if (tm != null) {
	    tm.removeTableModelListener(tml);
	}
	this.tm = tableModel;
	tm.addTableModelListener(tml);
	getTmColumns();
	setDefaultColumns();
	// fireTableStructureChanged(); // already notified from
	// setDefaultColumns();
    }

    /**
     * Return the TableModel being referenced.
     * 
     * @return the table being displayed.
     * @see #setTableModel(TableModel tableModel)
     */
    public TableModel getTableModel() {
	return tm;
    }

    /**
     * Update the tmColumns list for the referenced TableModel.
     */
    private void getTmColumns() {
	tmColumns.clear();
	for (int i = 0; i < tm.getColumnCount(); i++) {
	    tmColumns.add(i, new ColRef(tm, i));
	}
    }

    /**
     * Set the columns to be those of the referenced TableModel.
     */
    public void setDefaultColumns() {
	if (tm != null) {
	    notify = false; // don't notify listeners until all changes made.
	    columns.clear();
	    for (int i = 0; i < tm.getColumnCount(); i++) {
		ColRef cref = new ColRef(tm, i);
		columns.addElement(cref);
	    }
	    notify = true;
	}
	fireTableStructureChanged(); // now notify listeners about changes
    }

    /**
     * Add a formula column to this VirtualTableModel.
     * 
     * @param name
     *            the name given this column.
     * @param formula
     *            a formula to generate values for this column.
     */
    public void addColumn(String name, TableModelFormula formula) {
	formulas.addElement(formula);
	if (name != null) {
	    formula.setName(name);
	}
	columns.addElement(formula);
    }

    /**
     * Insert a formula column to this VirtualTableModel.
     * 
     * @param formula
     *            a formula to generate values for this column.
     * @param index
     *            at which to add the Column.
     */
    public void addColumn(TableModelFormula formula, int index) {
	formulas.addElement(formula);
	columns.add(index, formula);
    }

    /**
     * Add a column of the source TableModel to this VirtualTableModel.
     * 
     * @param name
     *            the name given this column.
     * @param columnIndex
     *            the index of the TableModel to add.
     */
    public void addColumn(String name, int columnIndex) {
	ColRef cref = new ColRef(name, tm, columnIndex);
	columns.addElement(cref);
    }

    /**
     * Add a column of the source TableModel to this VirtualTableModel.
     * 
     * @param columnIndex
     *            the index of the TableModel to add.
     */
    public void addColumn(int columnIndex) {
	ColRef cref = new ColRef(tm, columnIndex);
	columns.addElement(cref);
    }

    /**
     * Add a column of the source TableModel to this VirtualTableModel.
     * 
     * @param col
     *            The column to add to the TableModel.
     */
    public void addColumn(VirtualColumn col) {
	columns.addElement(col);
    }

    /**
     * Insert a column to this VirtualTableModel.
     * 
     * @param column
     *            the Column of the TableModel to add.
     * @param index
     *            at which to add the Column.
     */
    public void addColumn(VirtualColumn column, int index) {
	columns.add(index, column);
    }

    /**
     * Remove the column of this VirtualTableModel at the index.
     * 
     * @param columnIndex
     *            the index of the column to remove.
     */
    public void removeColumn(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < columns.size()) {
	    columns.removeElementAt(columnIndex);
	}
    }

    /**
     * Remove the columns of this VirtualTableModel at the indices.
     * 
     * @param columnIndices
     *            the index of the column to remove.
     */
    public void removeColumns(int[] columnIndices) {
	if (columnIndices != null) {
	    notify = false;
	    Arrays.sort(columnIndices);
	    for (int i = columnIndices.length - 1; i >= 0; i--) {
		if (columnIndices[i] >= 0 && columnIndices[i] < columns.size()) {
		    columns.removeElementAt(columnIndices[i]);
		}
	    }
	    notify = true;
	    fireTableStructureChanged(); // now notify listeners about changes
	}
    }

    /**
     * Returns a list of the VirtualColumns that comprise the TableModel view
     * presented by this VirtualTableModel.
     * 
     * @return a list of the VirtualColumns.
     */
    public ListModel getColumnList() {
	return columns;
    }

    /**
     * Returns a list of the VirtualColumns that comprise the TableModel
     * referenced by this VirtualTableModel.
     * 
     * @return a list of the VirtualColumns from the referenced TableModel.
     */
    public ListModel getDefaultColumnList() {
	return tmColumns;
    }

    /**
     * Returns a list of the TableModelFormulas in this VirtualTableModel.
     * 
     * @return a list of the TableModelFormulas in this VirtualTableModel.
     */
    public ListModel getFormulaList() {
	return formulas;
    }

    /**
     * Set an IndexMap that will allow the VirtualTableModel to present a view
     * of the referenced TableModel with a different row ordering.
     * 
     * @param indexMap
     *            the IndexMap that reorders the rows of the TableModel
     * @see #getIndexMap()
     */
    public void setIndexMap(IndexMap indexMap) {
	this.rowIdxMap = indexMap;
	fireTableDataChanged();
    }

    /**
     * Get an IndexMap that will allow the VirtualTableModel to present a view
     * of the referenced TableModel with a different row ordering.
     * 
     * @see #setIndexMap(IndexMap indexMap)
     */
    public IndexMap getIndexMap() {
	return rowIdxMap;
    }

    /**
     * Set an IndexMap that will allow the VirtualTableModel to present a view
     * of the referenced TableModel with a different column ordering.
     * 
     * @param indexMap
     *            the IndexMap that reorders the columns of the TableModel
     * @see #getColumnIndexMap()
     */
    public void setColumnIndexMap(IndexMap indexMap) {
	this.colIdxMap = indexMap;
	fireTableStructureChanged();
    }

    /**
     * set an IndexMap that will allow the VirtualTableModel to present a view
     * of the referenced TableModel with a different column ordering.
     * 
     * @see #setColumnIndexMap(IndexMap indexMap)
     */
    public IndexMap getColumnIndexMap() {
	return colIdxMap;
    }

    private int getRowIndex(int rowIndex) {
	if (rowIdxMap != null) {
	    return rowIdxMap.getSrc(rowIndex);
	}
	return rowIndex;
    }

    private int getColIndex(int columnIndex) {
	if (colIdxMap != null && columnIndex < colIdxMap.getSrcSize()) {
	    return colIdxMap.getDst(columnIndex);
	}
	return columnIndex;
    }

    /**
     * Returns the number of rows in the model.
     * 
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
	if (rowIdxMap != null)
	    return rowIdxMap.getDstSize();
	if (tm == null)
	    return 0;
	return tm.getRowCount();
    }

    /**
     * Returns the number of columns in the model.
     * 
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
	if (tm == null)
	    return 0;
	return columns.size();
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the name of the column
     */
    @Override
    public String getColumnName(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < columns.size()) {
	    return ((VirtualCell) columns.elementAt(columnIndex)).getName();
	}
	return null;
    }

    /**
     * Set the name of the column at <code>columnIndex</code>.
     * 
     * @param name
     *            the name of the column
     * @param columnIndex
     *            the index of the column
     */
    public void setColumnName(String name, int columnIndex) {
	if (columnIndex >= 0 && columnIndex < columns.size()) {
	    ((VirtualCell) columns.elementAt(columnIndex)).setName(name);
	}
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < columns.size()) {
	    if (columns.elementAt(columnIndex) instanceof ColRef) {
		return ((ColRef) columns.elementAt(columnIndex))
			.getColumnClass();
	    } else if (columns.elementAt(columnIndex) instanceof VirtualColumn) {
		return ((VirtualColumn) columns.elementAt(columnIndex))
			.getColumnClass();
	    }
	    return (new Object()).getClass();
	}
	return null;
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code> is editable. Otherwise, <code>setValueAt</code>
     * on the cell will not change the value of that cell.
     * 
     * @param rowIndex
     *            the row whose value to be queried
     * @param columnIndex
     *            the column whose value to be queried
     * @return true if the cell is editable
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	int ci = getColIndex(columnIndex);
	if (ci >= 0 && ci < columns.size()) {
	    int ri = getRowIndex(rowIndex);
	    return ((VirtualCell) columns.elementAt(ci)).isCellEditable(ri, ci);
	}
	return false;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     * 
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (columnIndex >= 0 && columnIndex < columns.size()) {
	    int ri = rowIndex;
	    if (rowIdxMap != null) {
		ri = getRowIndex(rowIndex);
		if (ri < 0 || ri >= tm.getRowCount())
		    return null;
	    }
	    return ((VirtualCell) columns.elementAt(columnIndex)).getValueAt(
		    ri, getColIndex(columnIndex));
	}
	return null;
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and
     * <code>rowIndex</code> to <code>aValue</code>.
     * 
     * @param aValue
     *            the new value
     * @param rowIndex
     *            the row whose value is to be changed
     * @param columnIndex
     *            the column whose value is to be changed
     * @see #getValueAt
     * @see #isCellEditable
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (isCellEditable(rowIndex, columnIndex)) {
	    int ci = getColIndex(columnIndex);
	    if (ci >= 0 && ci < columns.size()) {
		VirtualCell cell = (VirtualCell) columns.elementAt(ci);
		int ri = rowIndex;
		if (rowIdxMap != null) {
		    ri = getRowIndex(rowIndex);
		    if (ri < 0 || ri >= tm.getRowCount())
			return;
		}
		cell.setValueAt(aValue, ri, ci);
		if (!(cell instanceof ColRef)) { // ColRef let underlying table
		    // notify
		    if (recalculateFormulas()) {
			fireTableDataChanged(); // Don't know which cells might
			// be affected
		    } else {
			fireTableCellUpdated(rowIndex, columnIndex);
		    }
		}
	    }
	}
    }

    public boolean recalculateFormulas() {
	boolean hasFormulas = false;
	for (Enumeration e = columns.elements(); e.hasMoreElements();) {
	    VirtualCell cell = (VirtualCell) e.nextElement();
	    if (cell instanceof TableModelFormula) {
		TableModelFormula formula = (TableModelFormula) cell;
		formula.recalculate();
		hasFormulas = true;
	    }
	}
	return hasFormulas;
    }

    /**
     * Return a ColumnMap for the TableModel column at the columnIndex.
     * 
     * @param columnIndex
     *            The index of the column to map.
     * @return The ColumnMap for the given column
     * @see edu.umn.genomics.table.ColumnMap
     */
    public ColumnMap getColumnMap(int columnIndex) {
	ColumnMap cmap = null;
	if (tm instanceof TableColumnMap && rowIdxMap == null) {
	    int ci = getColIndex(columnIndex);
	    if (ci >= 0 && ci < columns.size()) {
		VirtualCell cell = (VirtualCell) columns.elementAt(ci);
		if (cell instanceof ColRef) {
		    cmap = ((TableColumnMap) tm).getColumnMap(((ColRef) cell)
			    .getColumnIndex());
		}
	    }
	}
	return cmap;
    }

    /*
     * For debugging Listeners public void
     * addTableModelListener(TableModelListener l) {
     * System.err.println("vtm add " + l.hashCode() + " " +
     * l.getClass().getName()); if (l instanceof
     * edu.umn.genomics.table.VirtualTableModelProxy) { String s = ""; s = null;
     * try { s.equals(l); } catch (Throwable t) { t.printStackTrace(); } }
     * super.addTableModelListener(l); try { TableModelListener tl[] =
     * getTableModelListeners(); for (int i = 0; i < tl.length; i++)
     * System.err.println("vtm a " + i + " " + tl[i].hashCode() + " " +
     * tl[i].getClass().getName()); } catch (Throwable t) { } }
     * 
     * public void removeTableModelListener(TableModelListener l) {
     * System.err.println("vtm rmv " + l.hashCode() + " " +
     * l.getClass().getName()); super.removeTableModelListener(l); try {
     * TableModelListener tl[] = getTableModelListeners(); for (int i = 0; i <
     * tl.length; i++) System.err.println("vtm r " + i + " " + tl[i].hashCode()
     * + " " + tl[i].getClass().getName()); } catch (Throwable t) { } }
     */

}
