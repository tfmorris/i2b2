/*
 * @(#) $RCSfile: DBColumn.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 14:24:06 $ $Name: RELEASE_1_3_1_0001b $
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
 * DatabaseMetaData information about a database table column.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 14:24:06 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see java.sql.DatabaseMetaData#getColumns(String, String, String, String)
 */
public class DBColumn {
    Hashtable catalogueHash; // name -> Catalogue
    DBTable table; // TABLE_CAT TABLE_SCHEM TABLE_NAME
    String name; // COLUMN_NAME
    short dataType; // DATA_TYPE
    String typeName; // TYPE_NAME
    int columnSize; // COLUMN_SIZE
    // int bufferLength; // BUFFER_LENGTH
    int decimalDigits; // DECIMAL_DIGITS
    int numPrecRadix; // NUM_PREC_RADIX
    int nullable; // NULLABLE
    String remarks; // REMARKS
    String columnDef; // COLUMN_DEF
    // int sqlDataType; // SQL_DATA_TYPE
    // int sqlDateTimeSub; // SQL_DATETIME_SUB
    int charOctetLength; // CHAR_OCTET_LENGTH
    int ordinalPosition; // ORDINAL_POSITION
    String isNullable; // IS_NULLABLE

    /**
     * Return a list of DBColumn instances for the given table.
     * 
     * @param dbmd
     *            the meta data for the database.
     * @param table
     *            the table for which to retrive column information.
     * @return a list of DBColumn instances for the given table.
     * @throws NullPointerException
     *             If either dbmd or table are null.
     * @throws SQLException
     *             from java.sql.DatabaseMetaData.getColumns method
     */
    public static List getDBColumn(DatabaseMetaData dbmd, DBTable table)
	    throws NullPointerException, SQLException {
	if (table == null) {
	    throw new NullPointerException(
		    "DBColumn.getDBColumn(DatabaseMetaData, DBTable) DBTable can't be null");
	}
	if (dbmd == null) {
	    throw new NullPointerException(
		    "DBColumn.getDBColumn(DatabaseMetaData, DBTable) DatabaseMetaData can't be null");
	}
	ResultSet rs = dbmd.getColumns(table.getCatalogName(), table
		.getSchemaName(), table.getTableName(), null);
	Vector colList = new Vector();
	if (rs != null) {
	    while (rs.next()) {
		DBColumn dbcol = getDBColumn(table, rs);
		if (dbcol != null) {
		    colList.add(dbcol);
		}
	    }
	    rs.close();
	}
	return colList;
    }

    /**
     * Return a DBColumn instance for the current row in the ResultSet returned
     * from
     * {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)}
     * . This is typically called by
     * {@link #getDBColumn(DatabaseMetaData, DBTable)}.
     * 
     * @param table
     *            the table for which the column information was retrieved, if
     *            null a new DBTable instance will be created for this column.
     * @param rs
     *            the ResultSet of column information.
     * @return a DBColumn instance for this row in the ResultSet.
     * @throws SQLException
     *             from retrieving column data from the ResultSet
     */
    public static DBColumn getDBColumn(DBTable table, ResultSet rs)
	    throws SQLException {
	DBTable dbTable = table;
	if (dbTable == null) {
	    dbTable = new DBTable(rs.getString(1), rs.getString(2), rs
		    .getString(3));
	}
	String name = rs.getString(4);
	DBColumn col = new DBColumn(name, dbTable);
	col.setDataType(rs.getShort(5));
	col.setTypeName(rs.getString(6));
	col.setColumnSize(rs.getInt(7));
	// col.setBufferLength(rs.getInt(8));
	col.setDecimalDigits(rs.getInt(9));
	col.setNumPrecRadix(rs.getInt(10));
	col.setNullable(rs.getInt(11));
	col.setRemarks(rs.getString(12));
	col.setColumnDef(rs.getString(13));
	// col.setSqlDataType(rs.getInt(14));
	// col.setSqlDateTimeSub(rs.getInt(15));
	col.setCharOctetLength(rs.getInt(16));
	col.setOrdinalPosition(rs.getInt(17));
	col.setIsNullable(rs.getString(18));
	return col;
    }

    public DBColumn(String name, DBTable table) {
	this.name = name;
	this.table = table;
    }

    /**
     * Return this column's name in the table.
     * 
     * @return this column's name.
     */
    public String getColumnName() {
	return name;
    }

    /**
     * Return the SQL type from java.sql.Types for this column.
     * 
     * @return the SQL type from java.sql.Types
     */
    public short getDataType() {
	return dataType;
    }

    /**
     * Data source dependent type name,for a UDT the type name is fully
     * qualified
     * 
     * @return Data source dependent type name
     */
    public String getTypeName() {
	return typeName;
    }

    /**
     * Return the column size. For char or date types this is the maximum number
     * of characters, for numeric or decimal types this is precision.
     * 
     * @return the column size.
     */
    public int getColumnSize() {
	return columnSize;
    }

    // /**
    // * Return the buffer length.
    // * @return the buffer length.
    // */
    // public int getBufferLength () {
    // return bufferLength;
    // }

    /**
     * Return the number of fractional digits.
     * 
     * @return the number of fractional digits.
     */
    public int getDecimalDigits() {
	return decimalDigits;
    }

    /**
     * Return the Radix (typically either 10 or 2).
     * 
     * @return the Radix.
     */
    public int getNumPrecRadix() {
	return numPrecRadix;
    }

    /**
     * Return whether NULL is allowed. columnNoNulls - might not allow NULL
     * values columnNullable - definitely allows NULL values
     * columnNullableUnknown - nullability unknown
     * 
     * @return the column size.
     */
    public int getNullable() {
	return nullable;
    }

    /**
     * Return comment describing column.
     * 
     * @return comment describing column.
     */
    public String getRemarks() {
	return remarks;
    }

    /**
     * Return the default value, (may be null).
     * 
     * @return the default value.
     */
    public String getColumnDef() {
	return columnDef;
    }

    // /**
    // * Return the SQL Data type.
    // * @return the SQL Data type.
    // */
    // public int getSqlDataType () {
    // return sqlDataType;
    // }

    // /**
    // * Return the SQL_DATETIME_SUB.
    // * @return the SQL_DATETIME_SUB.
    // */
    // public int getSqlDateTimeSub () {
    // return sqlDateTimeSub;
    // }

    /**
     * Return for char types, the maximum number of bytes in the column.
     * 
     * @return for char types, the maximum number of bytes in the column.
     */
    public int getCharOctetLength() {
	return charOctetLength;
    }

    /**
     * Return the index of column in table (starting at 1).
     * 
     * @return the index of column in table (starting at 1).
     */
    public int getOrdinalPosition() {
	return ordinalPosition;
    }

    /**
     * Return whether column allows NULL values: "NO" means column definitely
     * does not allow NULL values; "YES" means the column might allow NULL
     * values. An empty string means nobody knows.
     * 
     * @return whether column allows NULL values.
     */
    public String getIsNullable() {
	return isNullable;
    }

    public void setDataType(short dataType) {
	this.dataType = dataType;
    }

    public void setTypeName(String typeName) {
	this.typeName = typeName;
    }

    public void setColumnSize(int columnSize) {
	this.columnSize = columnSize;
    }

    // public void setBufferLength(int bufferLength) {
    // this.bufferLength = bufferLength;
    // }

    public void setDecimalDigits(int decimalDigits) {
	this.decimalDigits = decimalDigits;
    }

    public void setNumPrecRadix(int numPrecRadix) {
	this.numPrecRadix = numPrecRadix;
    }

    public void setNullable(int nullable) {
	this.nullable = nullable;
    }

    public void setRemarks(String remarks) {
	this.remarks = remarks;
    }

    public void setColumnDef(String columnDef) {
	this.columnDef = columnDef;
    }

    // public void setSqlDataType(int sqlDataType) {
    // this.sqlDataType = sqlDataType;
    // }

    // public void setSqlDateTimeSub(int sqlDateTimeSub) {
    // this.sqlDateTimeSub = sqlDateTimeSub;
    // }

    public void setCharOctetLength(int charOctetLength) {
	this.charOctetLength = charOctetLength;
    }

    public void setOrdinalPosition(int ordinalPosition) {
	this.ordinalPosition = ordinalPosition;
    }

    public void setIsNullable(String isNullable) {
	this.isNullable = isNullable;
    }

    /**
     * Return a name for this column that includes the catalog, schema, and
     * table names if appropriate for this database.
     * 
     * @return the fully qualified column name
     */
    public String getQualifiedName() {
	return (table != null ? table.getQualifiedName() + "." : "")
		+ getColumnName();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj != null && obj instanceof DBColumn) {
	    return this.getQualifiedName().equals(
		    ((DBColumn) obj).getQualifiedName());
	}
	return false;
    }

    public int compareTo(Object obj) {
	if (obj != null) {
	    throw new NullPointerException(
		    "Can only compare DBColumn to another DBColumn.");
	} else if (obj instanceof DBColumn) {
	    return this.getQualifiedName().compareTo(
		    ((DBColumn) obj).getQualifiedName());
	}
	throw new ClassCastException(
		"Can only compare DBColumn to another DBColumn.");
    }

    @Override
    public String toString() {
	return getColumnName();
    }

}
