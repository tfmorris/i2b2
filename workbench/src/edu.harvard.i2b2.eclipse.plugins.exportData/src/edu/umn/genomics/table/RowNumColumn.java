/*
 * @(#) $RCSfile: RowNumColumn.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * RowNumColumn represents a column a TableModel. It may be used to index into a
 * Column in an underlying table, or as a formula column that generates its
 * values via a script.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see edu.umn.genomics.table.VirtualTableModel
 */
public class RowNumColumn implements VirtualColumn {
    String name = "Row";

    /**
     * Returns the most specific superclass for all the cell values in the
     * column. This is used by the <code>JTable</code> to set up a default
     * renderer and editor for the column.
     * 
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass() {
	return Integer.class;
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
	return false;
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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
	return new Integer(rowIndex + 1);
    }

    /**
     * Get a name for this cell.
     * 
     * @return The name given to this cell.
     */
    public String getName() {
	return name;
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
     * Get a type name for this cell.
     * 
     * @return The type name for this cell.
     */
    public String getType() {
	return "Row Number";
    }

    /**
     * Get a description of this VirtualCell.
     * 
     * @return a description of this VirtualCell.
     */
    public String getDescription() {
	return getType();
    }

}
