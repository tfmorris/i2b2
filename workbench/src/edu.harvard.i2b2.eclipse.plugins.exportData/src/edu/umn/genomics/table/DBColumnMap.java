/*
 * @(#) $RCSfile: DBColumnMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;
import java.sql.*;
import java.io.*;
import javax.swing.table.TableModel;
import javax.swing.ListSelectionModel;

/**
 * Presents a JDBC obtained database table as a TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBColumnMap extends CacheColumnMap {
    int sqlType;
    int[] nanos = new int[0];

    public DBColumnMap(TableModel tm, ListSelectionModel lsm, String name,
	    int colIndex, Class colClass, int sqlType) {
	super(tm, lsm, name, colIndex, colClass);
	this.sqlType = sqlType;
	switch (sqlType) {
	case java.sql.Types.INTEGER:
	case java.sql.Types.TINYINT:
	case java.sql.Types.SMALLINT:
	case java.sql.Types.BIGINT:
	case java.sql.Types.FLOAT:
	case java.sql.Types.DOUBLE:
	case java.sql.Types.DECIMAL:
	case java.sql.Types.NUMERIC:
	    isNumber = true;
	    break;
	case java.sql.Types.REAL:
	case java.sql.Types.DATE:
	case java.sql.Types.TIME:
	case java.sql.Types.TIMESTAMP:
	    isDate = true;
	    break;
	case java.sql.Types.LONGVARCHAR:
	case java.sql.Types.ARRAY:
	case java.sql.Types.BINARY:
	case java.sql.Types.VARBINARY:
	case java.sql.Types.LONGVARBINARY:
	case java.sql.Types.BIT:
	case java.sql.Types.BLOB:
	case java.sql.Types.CLOB:
	case java.sql.Types.DISTINCT:
	case java.sql.Types.NULL:
	case java.sql.Types.REF:
	case java.sql.Types.STRUCT:
	case java.sql.Types.JAVA_OBJECT:
	case java.sql.Types.OTHER:
	    break;
	default:
	    if (System.getProperty("java.specification.version").compareTo(
		    "1.4") >= 0) {
		switch (sqlType) {
		case java.sql.Types.BOOLEAN:
		case java.sql.Types.DATALINK:
		default:
		}
	    }
	}
	createDataArray(sqlType);
	colTyped = true;
    }

    /**
     * Return the element that is mapped nearest to the the mapValue in the
     * given direction.
     * 
     * @param mapValue
     *            the relative position on the map
     * @param dir
     *            negative means round down, positive mean round up, 0 rounds to
     *            closest.. return the element that is mapped nearest to the the
     *            mapValue.
     */
    @Override
    public Object getMappedValue(double mapValue, int dir) {
	if (isNumber) {
	    return super.getMappedValue(mapValue, dir);
	} else if (isDate) {
	    return new java.sql.Date((long) mapValue);
	} else {
	    return super.getMappedValue(mapValue, dir);
	}
    }

    /**
     * Return a java Class for a given SQL Type. This is for JDBC 1.1 only, for
     * JDBC 1.2 and later use ResultSetMetaData.getColumnClassName(int column)
     * 
     */
    public static Class getClassForSqlType(int sqlType) {
	switch (sqlType) {
	case java.sql.Types.CHAR:
	case java.sql.Types.VARCHAR:
	case java.sql.Types.CLOB:
	    return String.class;
	case java.sql.Types.INTEGER:
	    return Integer.class;
	case java.sql.Types.TINYINT:
	    return Byte.class;
	case java.sql.Types.SMALLINT:
	    return Short.class;
	case java.sql.Types.BIGINT:
	    return Long.class;
	case java.sql.Types.FLOAT:
	    return Float.class;
	case java.sql.Types.DOUBLE:
	case java.sql.Types.DECIMAL:
	case java.sql.Types.NUMERIC:
	case java.sql.Types.REAL:
	    return Double.class;
	case java.sql.Types.DATE:
	case java.sql.Types.TIME:
	case java.sql.Types.TIMESTAMP:
	    return java.util.Date.class;
	case java.sql.Types.BIT:
	case java.sql.Types.BOOLEAN:
	    return java.lang.Boolean.class;
	case java.sql.Types.LONGVARCHAR:
	case java.sql.Types.ARRAY:
	case java.sql.Types.BINARY:
	case java.sql.Types.VARBINARY:
	case java.sql.Types.LONGVARBINARY:
	case java.sql.Types.BLOB:
	case java.sql.Types.DISTINCT:
	case java.sql.Types.NULL:
	case java.sql.Types.REF:
	case java.sql.Types.STRUCT:
	case java.sql.Types.JAVA_OBJECT:
	case java.sql.Types.OTHER:
	default:
	    break;
	}
	return Object.class;
    }

    synchronized private void createDataArray(int sqlType) {
	switch (sqlType) {
	case java.sql.Types.CHAR:
	case java.sql.Types.VARCHAR:
	    objList = new Vector();
	    data = java.lang.reflect.Array.newInstance(Integer.TYPE, bufIncr);
	    break;

	case java.sql.Types.INTEGER:
	    data = java.lang.reflect.Array.newInstance(Integer.TYPE, bufIncr);
	    break;
	case java.sql.Types.TINYINT:
	    data = java.lang.reflect.Array.newInstance(Byte.TYPE, bufIncr);
	    break;
	case java.sql.Types.SMALLINT:
	    data = java.lang.reflect.Array.newInstance(Short.TYPE, bufIncr);
	    break;
	case java.sql.Types.BIGINT:
	    data = java.lang.reflect.Array.newInstance(Long.TYPE, bufIncr);
	    break;

	case java.sql.Types.FLOAT:
	    data = java.lang.reflect.Array.newInstance(Float.TYPE, bufIncr);
	    break;
	case java.sql.Types.DOUBLE:
	case java.sql.Types.DECIMAL:
	case java.sql.Types.NUMERIC:
	case java.sql.Types.REAL:
	    data = java.lang.reflect.Array.newInstance(Double.TYPE, bufIncr);
	    break;

	case java.sql.Types.DATE:
	case java.sql.Types.TIME:
	    data = java.lang.reflect.Array.newInstance(Long.TYPE, bufIncr);
	    break;
	case java.sql.Types.TIMESTAMP: // This should be an array of
	    // java.sql.Types.TIMESTAMP
	    data = java.lang.reflect.Array.newInstance(Long.TYPE, bufIncr);
	    break;

	case java.sql.Types.BIT:
	case java.sql.Types.BOOLEAN:
	    data = new BitSet();
	    break;

	case java.sql.Types.ARRAY:
	case java.sql.Types.BINARY:
	case java.sql.Types.VARBINARY:
	case java.sql.Types.LONGVARBINARY:

	case java.sql.Types.LONGVARCHAR:
	case java.sql.Types.BLOB:
	case java.sql.Types.CLOB:

	case java.sql.Types.DISTINCT:
	case java.sql.Types.NULL:
	case java.sql.Types.REF:
	case java.sql.Types.STRUCT:
	case java.sql.Types.JAVA_OBJECT:
	case java.sql.Types.OTHER:
	    objList = new Vector();
	    data = java.lang.reflect.Array.newInstance(Integer.TYPE, bufIncr);
	    break;
	default:
	    if (System.getProperty("java.specification.version").compareTo(
		    "1.4") >= 0) {
		switch (sqlType) {
		case java.sql.Types.BOOLEAN:
		    // Maybe use a BitSet here
		case java.sql.Types.DATALINK:
		default:
		    objList = new Vector();
		    data = java.lang.reflect.Array.newInstance(Integer.TYPE,
			    bufIncr);
		    break;
		}
	    }
	    break;
	}
    }

    /**
     * Sets the value in the cell at rowIndex freom the ResultSet rs.
     * 
     * @param rs
     *            - the ResultSet which must be at the row corresponding to
     *            rowIndexcurrent row
     * @rowIndex - the row whose value is to be set
     */
    synchronized void setValueAt(ResultSet rs, int rowIndex)
	    throws SQLException {
	checkArraySize(rowIndex + 1);
	switch (sqlType) {
	case java.sql.Types.CHAR:
	case java.sql.Types.VARCHAR: {
	    String val = rs.getString(colIndex + 1);
	    ((int[]) data)[rowIndex] = mapObject(val);
	}
	    break;

	case java.sql.Types.INTEGER: {
	    int val = rs.getInt(colIndex + 1);
	    ((int[]) data)[rowIndex] = val;
	    if (val < min) {
		min = val;
	    } else if (val > max) {
		max = val;
	    }
	}
	    break;
	case java.sql.Types.TINYINT: {
	    byte val = rs.getByte(colIndex + 1);
	    ((byte[]) data)[rowIndex] = val;
	    if (val < min) {
		min = val;
	    } else if (val > max) {
		max = val;
	    }
	}
	    break;
	case java.sql.Types.SMALLINT: {
	    short val = rs.getShort(colIndex + 1);
	    ((short[]) data)[rowIndex] = val;
	    if (val < min) {
		min = val;
	    } else if (val > max) {
		max = val;
	    }
	}
	    break;
	case java.sql.Types.BIGINT: {
	    long val = rs.getLong(colIndex + 1);
	    ((long[]) data)[rowIndex] = val;
	    if (val < min) {
		min = val;
	    } else if (val > max) {
		max = val;
	    }
	}
	    break;

	case java.sql.Types.FLOAT: {
	    float val = rs.getFloat(colIndex + 1);
	    ((float[]) data)[rowIndex] = val;
	    if (val < min) {
		min = val;
	    } else if (val > max) {
		max = val;
	    }
	}
	    break;
	case java.sql.Types.DOUBLE:
	case java.sql.Types.DECIMAL:
	case java.sql.Types.NUMERIC:
	case java.sql.Types.REAL: {
	    double val = rs.getDouble(colIndex + 1);
	    ((double[]) data)[rowIndex] = val;
	    if (val < min) {
		min = val;
	    } else if (val > max) {
		max = val;
	    }
	}
	    break;

	case java.sql.Types.DATE:
	case java.sql.Types.TIME: {
	    java.util.Date date = (java.util.Date) rs.getObject(colIndex + 1);
	    if (date != null) {
		long val = date.getTime();
		((long[]) data)[rowIndex] = val;
		if (val < min) {
		    min = val;
		} else if (val > max) {
		    max = val;
		}
	    } else {
		long val = Long.MAX_VALUE;
	    }
	}
	    break;
	case java.sql.Types.TIMESTAMP: {
	    Timestamp date = (Timestamp) rs.getObject(colIndex + 1);
	    if (date != null) {
		long val = date.getTime();
		((long[]) data)[rowIndex] = val;
		if (val < min) {
		    min = val;
		} else if (val > max) {
		    max = val;
		}
		nanos = (int[]) checkArraySize(nanos, rowIndex + 1);
		nanos[rowIndex] = date.getNanos();
	    } else {
		long val = Long.MAX_VALUE;
	    }
	}
	    break;

	case java.sql.Types.BIT:
	case java.sql.Types.BOOLEAN:
	    // This should be a BitSet
	    if (data instanceof BitSet) {
		((BitSet) data).set(rowIndex, rs.getBoolean(colIndex + 1));
	    } else {
		super.setValueAt(rs.getObject(colIndex + 1), rowIndex);
	    }
	    break;

	case java.sql.Types.ARRAY:
	    break;

	case java.sql.Types.BINARY:
	case java.sql.Types.VARBINARY:
	case java.sql.Types.LONGVARBINARY:
	case java.sql.Types.BLOB:
	    // Compressed?
	    // Image?
	    break;
	case java.sql.Types.LONGVARCHAR: {
	    String val = null;
	    try {
		int cbuflen = 4096;
		char[] cbuf = new char[4096];
		StringBuffer sb = new StringBuffer();
		BufferedReader rdr = new BufferedReader(rs
			.getCharacterStream(colIndex + 1));
		for (int len = rdr.read(cbuf, 0, cbuf.length); len >= 0; len = rdr
			.read(cbuf, 0, cbuf.length)) {
		    sb.append(cbuf, 0, len);
		}
		val = sb.toString();
	    } catch (IOException ioex) {
		System.err.println("setValueAt(" + rowIndex + "," + colIndex
			+ ") " + ioex);
	    }
	    ((int[]) data)[rowIndex] = mapObject(val);
	}
	    break;
	case java.sql.Types.CLOB: {
	    String val = null;
	    try {
		Clob clob = rs.getClob(colIndex + 1);
		int cbuflen = 4096;
		long clen = clob.length();
		if (clen <= cbuflen) {
		    val = clob.getSubString(1, (int) clen); // first character
		    // is at position 1
		} else {
		    char[] cbuf = new char[4096];
		    StringBuffer sb = new StringBuffer();
		    BufferedReader rdr = new BufferedReader(clob
			    .getCharacterStream());
		    for (int len = rdr.read(cbuf, 0, cbuf.length); len >= 0; len = rdr
			    .read(cbuf, 0, cbuf.length)) {
			sb.append(cbuf, 0, len);
		    }
		    val = sb.toString();
		}
	    } catch (IOException ioex) {
		System.err.println("setValueAt(" + rowIndex + "," + colIndex
			+ ") " + ioex);
	    }
	    ((int[]) data)[rowIndex] = mapObject(val);
	}
	    break;
	case java.sql.Types.DISTINCT:
	case java.sql.Types.NULL:
	case java.sql.Types.REF:
	case java.sql.Types.STRUCT:
	case java.sql.Types.JAVA_OBJECT:
	case java.sql.Types.OTHER: {
	    ((int[]) data)[rowIndex] = mapObject(rs.getObject(colIndex + 1));
	}
	    break;
	default:
	    if (System.getProperty("java.specification.version").compareTo(
		    "1.4") >= 0) {
		switch (sqlType) {
		case java.sql.Types.BOOLEAN:
		    // Maybe use a BitSet here
		case java.sql.Types.DATALINK:
		default: {
		    String val = rs.getString(colIndex + 1);
		    ((int[]) data)[rowIndex] = mapObject(val);
		}
		    break;
		}
	    }
	    // System.err.print(" NEED TO DEFINE " + sqlType + " for column " +
	    // colIndex);
	    break;
	}
    }

    @Override
    public Object getValueAt(int rowIndex) {
	Object val = super.getValueAt(rowIndex);
	switch (sqlType) {
	/*
	 * case java.sql.Types.INTEGER : case java.sql.Types.TINYINT : case
	 * java.sql.Types.SMALLINT : case java.sql.Types.BIGINT : case
	 * java.sql.Types.FLOAT : case java.sql.Types.DOUBLE : case
	 * java.sql.Types.DECIMAL : case java.sql.Types.NUMERIC : break; case
	 * java.sql.Types.REAL : break; case java.sql.Types.DATE : break;
	 */
	case java.sql.Types.TIME:
	    Time time = new Time(((java.util.Date) val).getTime());
	    val = time;
	    break;
	case java.sql.Types.TIMESTAMP:
	    Timestamp ts = new Timestamp(((java.util.Date) val).getTime());
	    if (nanos != null && rowIndex < nanos.length) {
		ts.setNanos(nanos[rowIndex]);
	    }
	    val = ts;
	    break;
	/*
	 * case java.sql.Types.BIT : case java.sql.Types.BOOLEAN : case
	 * java.sql.Types.LONGVARCHAR : case java.sql.Types.ARRAY : case
	 * java.sql.Types.BINARY : case java.sql.Types.VARBINARY : case
	 * java.sql.Types.LONGVARBINARY : case java.sql.Types.BLOB : case
	 * java.sql.Types.CLOB : case java.sql.Types.DISTINCT : case
	 * java.sql.Types.NULL : case java.sql.Types.REF : case
	 * java.sql.Types.STRUCT : case java.sql.Types.JAVA_OBJECT : case
	 * java.sql.Types.OTHER : default: break;
	 */
	}
	return val;
    }

}
