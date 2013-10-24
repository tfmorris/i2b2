/*
 * @(#) $RCSfile: ResultTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/09/05 21:19:13 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.bi.dbutil;

import java.sql.*;
import java.util.*;
import javax.swing.table.*;

/**
 * Represents a ResultSet as a TableModel
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/05 21:19:13 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class ResultTableModel extends DefaultTableModel {
    Class columnClass[] = null;
    boolean editingAllowed = true;

    Comparator classComparator = new Comparator() {
	public int compare(Object o1, Object o2) throws ClassCastException {
	    if (!(o1 instanceof Class)) {
	    } else if (!(o2 instanceof Class)) {
	    }
	    Class c1 = (Class) o1;
	    Class c2 = (Class) o2;
	    if (c1.equals(c2)) {
		return 0;
	    }
	    if (c1.isAssignableFrom(c2)) {
		return 1;
	    }
	    if (c2.isAssignableFrom(c1)) {
		return -1;
	    }
	    return 0;
	}

	@Override
	public boolean equals(Object obj) {
	    return obj == this;
	}
    };

    public ResultTableModel() {
	super();
    }

    public ResultTableModel(ResultSet rs) throws SQLException {
	this();
	set(rs);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
	if (editingAllowed) {
	    return super.isCellEditable(row, column);
	}
	return false;
    }

    /**
     * Set whether to allow any cell editing.
     * 
     * @param allowEdit
     *            whether to allow any cell editing.
     */
    public void allowEditing(boolean allowEdit) {
	editingAllowed = allowEdit;
    }

    public Class getCommonClass(Class[] classes) {
	HashSet classSet = null;
	if (classes == null || classes.length < 1) {
	    return java.lang.Object.class;
	}
	if (classes.length == 1) {
	    return classes[0];
	}
	for (int i = 0; i < classes.length; i++) {
	    HashSet cl = new HashSet(Arrays.asList(classes[i].getClasses()));
	    cl.addAll(Arrays.asList(classes[i].getInterfaces()));
	    if (classSet != null) {
		classSet.retainAll(cl);
	    } else {
		classSet = cl;
	    }
	}
	if (classSet != null && classSet.size() > 0) {
	    TreeSet ts = new TreeSet(classComparator);
	    ts.addAll(classSet);
	    if (ts.size() > 0) {
		return (Class) ts.first();
	    }
	}
	return java.lang.Object.class;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
	if (columnClass != null && columnIndex >= 0
		&& columnIndex < columnClass.length) {
	    return columnClass[columnIndex];
	}
	return java.lang.Object.class;
    }

    public void set(ResultSet rs) throws SQLException {
	HashSet colClass[];
	Vector columnNames;
	Vector dataRows;
	ResultSetMetaData rsmd = rs.getMetaData();
	int ncols = rsmd.getColumnCount();
	columnNames = new Vector(ncols);
	colClass = new HashSet[ncols];
	columnClass = new Class[ncols];
	for (int c = 0; c < ncols; c++) {
	    columnNames.add(rsmd.getColumnLabel(c + 1));
	    colClass[c] = new HashSet();
	}
	dataRows = new Vector();
	for (int r = 0; rs.next(); r++) {
	    Vector row = new Vector(ncols);
	    for (int c = 0; c < ncols; c++) {
		Object obj = rs.getObject(c + 1);
		row.add(obj);
		if (obj != null) {
		    colClass[c].add(obj.getClass());
		}
	    }
	    dataRows.add(row);
	}
	setDataVector(dataRows, columnNames);

	for (int c = 0; c < ncols; c++) {
	    columnClass[c] = colClass[c].size() == 1 ? (Class) (new Vector(
		    colClass[c])).get(0)
		    : colClass[c].size() > 1 ? getCommonClass((Class[]) colClass[c]
			    .toArray(new Class[colClass[c].size()]))
			    : java.lang.Object.class;
	}
    }

    public void add(ResultSet rs) throws SQLException, Exception {
	if (getColumnCount() < 1) {
	    set(rs);
	    return;
	}
	ResultSetMetaData rsmd = rs.getMetaData();
	int ncols = rsmd.getColumnCount();
	if (ncols != getColumnCount()) {
	    throw new Exception("Table columns count mismatch "
		    + getColumnCount() + " vs " + ncols);
	}
	for (int c = 0; c < ncols; c++) {
	    String colClass = rsmd.getColumnClassName(c + 1);
	    if (!columnClass[c].isAssignableFrom(Class.forName(colClass))) {
		throw new Exception("Table column " + (c + 1)
			+ " class mismatch " + columnClass[c] + " vs "
			+ colClass);
	    }
	}
	for (int r = 0; rs.next(); r++) {
	    Vector row = new Vector(ncols);
	    for (int c = 0; c < ncols; c++) {
		Object obj = rs.getObject(c + 1);
		row.add(obj);
	    }
	    addRow(row);
	}
    }

}
