/*
 * @(#) $RCSfile: ColRef.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.text.*;

/**
 * ColRef provides a reference to a column in a TableModel. It allows a
 * VirtualTableModelProxy to hide and rearrange the columns of the TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ColRef implements VirtualColumn {
    String name = null;
    TableModel tm;
    int c;

    /**
     * Create a column reference with the given name that indexes into the
     * columnIndex of the tableModel.
     * 
     * @param name
     *            A name for this column reference.
     * @param tableModel
     *            the tableModel to reference.
     * @param columnIndex
     *            the column index into the tableModel.
     */
    public ColRef(String name, TableModel tableModel, int columnIndex) {
	this.tm = tableModel;
	this.c = columnIndex;
	setName(name);
    }

    /**
     * Create a column reference that indexes into the columnIndex of the
     * tableModel.
     * 
     * @param tableModel
     * @param columnIndex
     */
    public ColRef(TableModel tableModel, int columnIndex) {
	this.tm = tableModel;
	this.c = columnIndex;
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
	return tm.getValueAt(rowIndex, c);
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	Object o = tm.getValueAt(rowIndex, c);
	if (o != null && o instanceof Number && aValue instanceof String) {
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
	tm.setValueAt(aValue, rowIndex, c);
    }

    /**
     * Get a name for this cell.
     * 
     * @return The name given to this cell.
     */
    public String getName() {
	return name != null ? name : tm.getColumnName(c);
    }

    /**
     * Set the name for this cell.
     * 
     * @param name
     *            The name given to this cell.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column.
     * 
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass() {
	return tm.getColumnClass(c);
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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return tm.isCellEditable(rowIndex, c);
    }

    /**
     * Return the column index that this reference points to in the tableModel.
     * 
     * @return the column index that this reference points to in the tableModel.
     */
    public int getColumnIndex() {
	return c;
    }

    /**
     * Return a String representation of this instance.
     * 
     * @return a String representation of this instance.
     */
    @Override
    public String toString() {
	return getName();
    }

    /**
     * Get a type name for this cell.
     * 
     * @return The type name for this cell.
     */
    public String getType() {
	return "Table Column";
    }

    /**
     * Get a description of this VirtualCell.
     * 
     * @return a description of this VirtualCell.
     */
    public String getDescription() {
	return tm.getColumnName(c);
    }

}
