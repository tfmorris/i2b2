/*
 * @(#) $RCSfile: DBTable.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * DatabaseMetaData information for a Table.
 * 
 * TABLE_CAT String => table catalog (may be null) TABLE_SCHEM String => table
 * schema (may be null) TABLE_NAME String => table name TABLE_TYPE String =>
 * table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE",
 * "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM". REMARKS String =>
 * explanatory comment on the table TYPE_CAT String => the types catalog (may be
 * null) TYPE_SCHEM String => the types schema (may be null) TYPE_NAME String =>
 * type name (may be null) SELF_REFERENCING_COL_NAME String => name of the
 * designated "identifier" column of a typed table (may be null) REF_GENERATION
 * String => specifies how values in SELF_REFERENCING_COL_NAME are created.
 * Values are "SYSTEM", "USER", "DERIVED". (may be null)
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see java.sql.DatabaseMetaData#getColumns(String, String, String, String)
 */
public class DBTable implements Comparable {
    String catalog;
    String schema;
    String table;
    String table_type;
    String remarks;
    String type_cat;
    String type_schem;
    String type_name;
    String self_ref_col;
    String ref_gen;
    DBColumn[] columns = null;
    int nrows = -1;

    /**
     * Construct a table reference from the arguments.
     * 
     * @param catalog
     *            the database catalog this table is in, (may be null).
     * @param schema
     *            the database schema this table is in, (may be null).
     * @param table
     *            the database name of this table.
     */
    public DBTable(String catalog, String schema, String table) {
	this.catalog = catalog;
	this.schema = schema;
	this.table = table;
    }

    /**
     * Construct a table reference from the arguments.
     * 
     * @param dbmd
     *            the database meta data to query for tables.
     * @param catalog
     *            the name pattern for the catalog (may be null).
     * @param schema
     *            the name pattern for the schema (may be null).
     * @param table
     *            the name pattern for the table name.
     * @param types
     *            the database table types to include.
     * @return a list of DBTable ojbects that describe tables matching the
     *         arguments.
     * @see java.sql.DatabaseMetaData#getColumns(String, String, String, String)
     * @throws NullPointerException
     *             if dbmd is null
     * @throws SQLException
     *             from retrieving table data from the ResultSet
     */
    public static List getDBTables(DatabaseMetaData dbmd, String catalog,
	    String schema, String table, String[] types)
	    throws NullPointerException, SQLException {
	if (dbmd == null) {
	    throw new NullPointerException(
		    "DBColumn.getDBColumn(DatabaseMetaData, DBTable) DatabaseMetaData can't be null");
	}
	ResultSet rs = dbmd.getTables(catalog, schema, table, types);
	Vector tableList = new Vector();
	if (rs != null) {
	    while (rs.next()) {
		DBTable dbTable = getDBTable(dbmd, rs);
		if (dbTable != null) {
		    tableList.add(dbTable);
		}
	    }
	    rs.close();
	}
	return tableList;
    }

    /**
     * Return a DBTable instance for the current row in the ResultSet returned
     * from
     * {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     * . This is typically called by
     * {@link #getDBTables(DatabaseMetaData, String, String, String, String[])}.
     * 
     * @param rs
     *            the ResultSet of table information.
     * @return a DBTable instance for this row in the ResultSet.
     * @throws SQLException
     *             from retrieving table data from the ResultSet
     */
    public static DBTable getDBTable(DatabaseMetaData dbmd, ResultSet rs)
	    throws SQLException {
	int cnt = rs.getMetaData().getColumnCount();
	DBTable dbTable = new DBTable(rs.getString(1), rs.getString(2), rs
		.getString(3));
	try {
	    if (cnt >= 4)
		dbTable.table_type = rs.getString(4);
	} catch (SQLException ex) {
	}
	try {
	    if (cnt >= 5)
		dbTable.remarks = rs.getString(5);
	} catch (SQLException ex) {
	}
	try {
	    if (cnt >= 6)
		dbTable.type_cat = rs.getString(6);
	} catch (SQLException ex) {
	}

	try {
	    if (cnt >= 7)
		dbTable.type_schem = rs.getString(7);
	} catch (SQLException ex) {
	}
	try {
	    if (cnt >= 8)
		dbTable.type_name = rs.getString(8);
	} catch (SQLException ex) {
	}
	try {
	    if (cnt >= 9)
		dbTable.self_ref_col = rs.getString(9);
	} catch (SQLException ex) {
	}
	try {
	    if (cnt >= 10)
		dbTable.ref_gen = rs.getString(10);
	} catch (SQLException ex) {
	}
	Vector colList = new Vector(DBColumn.getDBColumn(dbmd, dbTable));
	if (colList != null && colList.size() > 0) {
	    dbTable.columns = new DBColumn[colList.size()];
	    dbTable.columns = (DBColumn[]) colList.toArray(dbTable.columns);
	}
	// dbTable.getRowCount(dbmd.getConnection());
	return dbTable;
    }

    /**
     * Get the database catalog containing this table,(may be null).
     * 
     * @return the database catalog containing this table.
     */
    public String getCatalogName() {
	return catalog;
    }

    /**
     * Get the database schema containing this table,(may be null).
     * 
     * @return the database schema containing this table.
     */
    public String getSchemaName() {
	return schema;
    }

    /**
     * Get the database name for this table.
     * 
     * @return the database name for this table.
     */
    public String getTableName() {
	return table;
    }

    public void setType(String type) {
	this.table_type = type;
    }

    public String getType() {
	return table_type;
    }

    public void setRemarks(String remarks) {
	this.remarks = remarks;
    }

    public String getRemarks() {
	return remarks;
    }

    public int getColumnCount() {
	return columns != null ? columns.length : 0;

    }

    public DBColumn[] getColumns() {
	return columns;
    }

    public int getRowCount() {
	return nrows;
    }

    public DBColumn getColumn(int index) {
	if (index < 0 || index >= columns.length) {
	    return null;
	}
	return columns[index];
    }

    public DBColumn getColumn(String name) {
	if (name == null) {
	    throw new NullPointerException("Column name can't be null.");
	}
	for (int i = 0; i < getColumnCount(); i++) {
	    if (columns[i].getColumnName().equals(name)
		    || columns[i].getQualifiedName().equals(name)) {
		return columns[i];
	    }
	}
	return null;
    }

    public Collection getColumnNames() {
	Vector v = new Vector(getColumnCount());
	for (int i = 0; i < getColumnCount(); i++) {
	    v.add(columns[i].getColumnName());
	}
	return v;
    }

    public int getRowCount(Connection conn) {
	String sql = "select count(*) from " + getQualifiedName();
	int count = -1;
	Statement stmt = null;
	ResultSet rs = null;
	System.err.println(sql);
	try {
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery(sql);
	} catch (Exception e) {
	    System.err.println(e);
	}
	if (rs != null) {
	    try {
		if (rs.next()) {
		    count = rs.getInt(1);
		}
	    } catch (Exception e) {
		System.err.println(e);
	    }
	    try {
		stmt.close();
	    } catch (Exception e) {
		System.err.println(e);
	    }
	}
	System.err.println(sql + " rows = " + count);
	return count;
    }

    /*
     * getPrimaryKeys getImportedKeys getExportedKeys getCrossReference public
     * TableModel getIndexInfo() { }
     */

    public String getQualifiedName() {
	return (catalog != null ? catalog + "." : "")
		+ (schema != null ? schema + "." : "") + table;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj != null && obj instanceof DBTable) {
	    return this.getQualifiedName().equals(
		    ((DBTable) obj).getQualifiedName());
	}
	return false;
    }

    public int compareTo(Object obj) {
	if (obj == null) {
	    throw new NullPointerException(
		    "Can only compare DBTable to another DBTable.");
	} else if (obj instanceof DBTable) {
	    return this.getQualifiedName().compareTo(
		    ((DBTable) obj).getQualifiedName());
	}
	throw new ClassCastException(
		"Can only compare DBTable to another DBTable.");
    }

    @Override
    public String toString() {
	return getTableName();
    }
}
