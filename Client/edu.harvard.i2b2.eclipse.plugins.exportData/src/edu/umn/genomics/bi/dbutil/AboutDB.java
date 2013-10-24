/*
 * @(#) $RCSfile: AboutDB.java,v $ $Revision: 1.3 $ $Date: 2008/09/03 18:02:08 $ $Name: RELEASE_1_3_1_0001b $
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
import java.lang.reflect.*;
import javax.swing.table.*;

/**
 * Return information about a database using DatabaseMetaData returned from a
 * connection.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/03 18:02:08 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see java.sql.DatabaseMetaData
 */
public class AboutDB {
    /**
     * Return as a TableModel the method names and the values they return for
     * methods about Database and JDBC Driver versions.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return a TableModel of versions.
     */
    public static TableModel getVersions(DatabaseMetaData dbmd) {
	Vector colNames = new Vector(2);
	colNames.add("Name");
	colNames.add("Version");
	Vector rows = new Vector();
	Vector cols;
	Method[] m = dbmd.getClass().getMethods(); // introspect on
						   // DataBaseMetaData methods
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getName().startsWith("getDatabase")
			&& m[i].getParameterTypes().length == 0) {
		    cols = new Vector(2);
		    cols.add(m[i].getName());
		    cols.add(m[i].invoke(dbmd, null));
		    rows.add(cols);
		}
	    } catch (Throwable ex) {
	    }
	}
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getName().startsWith("getDriver")
			&& m[i].getParameterTypes().length == 0) {
		    cols = new Vector(2);
		    cols.add(m[i].getName());
		    cols.add(m[i].invoke(dbmd, null));
		    rows.add(cols);
		}
	    } catch (Throwable ex) {
	    }
	}
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getName().startsWith("getJDBC")
			&& m[i].getParameterTypes().length == 0) {
		    cols = new Vector(2);
		    cols.add(m[i].getName());
		    cols.add(m[i].invoke(dbmd, null));
		    rows.add(cols);
		}
	    } catch (Throwable ex) {
	    }
	}
	return new DefaultTableModel(rows, colNames);
    }

    /**
     * Return as a TableModel the method names and the values they return for
     * methods that have boolean return values.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return a TableModel of method names and returned values.
     */
    public static TableModel getCapabilities(DatabaseMetaData dbmd) {
	Vector colNames = new Vector(2);
	colNames.add("Capability Question");
	colNames.add("Answer");
	Vector rows = new Vector();
	Vector cols;
	Method[] m = dbmd.getClass().getMethods(); // introspect on
						   // DataBaseMetaData methods
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getReturnType().toString().equals("boolean")
			&& m[i].getParameterTypes().length == 0) {
		    cols = new Vector(2);
		    cols.add(m[i].getName());
		    cols.add(m[i].invoke(dbmd, null));
		    rows.add(cols);
		}
	    } catch (Throwable ex) {
	    }
	}
	return new DefaultTableModel(rows, colNames);
    }

    /**
     * Return as a TableModel the method names and the values they return for
     * methods that have int return values and no input parameters.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return a TableModel of method names and returned values.
     */
    public static TableModel getLimits(DatabaseMetaData dbmd) {
	Vector colNames = new Vector(2);
	colNames.add("Name");
	colNames.add("Value");
	Vector rows = new Vector();
	Vector cols;
	Method[] m = dbmd.getClass().getMethods(); // introspect on
						   // DataBaseMetaData methods
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getReturnType().toString().equals("int")
			&& m[i].getName().startsWith("get")
			&& !m[i].getName().startsWith("getDatabase")
			&& !m[i].getName().startsWith("getDriver")
			&& !m[i].getName().startsWith("getJDBC")
			&& m[i].getParameterTypes().length == 0) {
		    cols = new Vector(2);
		    cols.add(m[i].getName());
		    cols.add(m[i].invoke(dbmd, null));
		    rows.add(cols);
		}
	    } catch (Throwable ex) {
	    }
	}
	return new DefaultTableModel(rows, colNames);
    }

    /**
     * Return as a TableModel the method names and the values they return for
     * methods that have String return values and no input parameters.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return a TableModel of method names and returned values.
     */
    public static TableModel getStringValues(DatabaseMetaData dbmd) {
	Vector colNames = new Vector(2);
	colNames.add("Name");
	colNames.add("Value");
	Vector rows = new Vector();
	Vector cols;
	Method[] m = dbmd.getClass().getMethods(); // introspect on
						   // DataBaseMetaData methods
	for (int i = 0; i < m.length; i++) {
	    try {
		if ((m[i].getReturnType().toString().indexOf("String") >= 0)
			&& !m[i].getName().startsWith("getDatabase")
			&& !m[i].getName().startsWith("getDriver")
			&& !m[i].getName().startsWith("getJDBC")
			&& !m[i].getName().endsWith("Functions")
			&& m[i].getParameterTypes().length == 0) {
		    cols = new Vector(2);
		    cols.add(m[i].getName());
		    cols.add(m[i].invoke(dbmd, null));
		    rows.add(cols);
		}
	    } catch (Throwable ex) {
	    }
	}
	return new DefaultTableModel(rows, colNames);
    }

    /**
     * Return as a TableModel the the ResultSet returned for the
     * DatabaseMetaData.getSQLKeywords method.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return a TableModel of method names and returned values.
     */
    public static TableModel getSQLKeywords(DatabaseMetaData dbmd) {
	Vector colNames = new Vector(1);
	colNames.add("Keyword");
	Vector rows = new Vector();
	Vector cols;
	try {
	    String kw = dbmd.getSQLKeywords();
	    for (StringTokenizer st = new StringTokenizer(kw, ","); st != null
		    && st.hasMoreTokens();) {
		cols = new Vector(1);
		cols.add(st.nextToken());
		rows.add(cols);
	    }
	} catch (Throwable ex) {
	}
	return new DefaultTableModel(rows, colNames);
    }

    /**
     * Return as a TableModel the Function type: String, Numeric, or Date, and
     * the function name.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return a TableModel of method names and returned values.
     */
    public static TableModel getFunctions(DatabaseMetaData dbmd) {
	Vector colNames = new Vector(2);
	colNames.add("Type");
	colNames.add("Function Name");
	Vector rows = new Vector();
	Vector cols;
	Method[] m = dbmd.getClass().getMethods(); // introspect on
						   // DataBaseMetaData methods
	for (int i = 0; i < m.length; i++) {
	    try {
		if ((m[i].getReturnType().toString().indexOf("String") >= 0)
			&& !m[i].getName().startsWith("getDatabase")
			&& !m[i].getName().startsWith("getDriver")
			&& !m[i].getName().startsWith("getJDBC")
			&& m[i].getName().endsWith("Functions")
			&& m[i].getParameterTypes().length == 0) {
		    String name = m[i].getName();
		    if (name != null && name.length() > 3) {
			name = name.substring(3, name.length() - 1);
		    }
		    String vals = (String) m[i].invoke(dbmd, null);
		    for (StringTokenizer st = new StringTokenizer(vals, ","); st != null
			    && st.hasMoreTokens();) {
			cols = new Vector(2);
			cols.add(name);
			cols.add(st.nextToken());
			rows.add(cols);
		    }
		}
	    } catch (Throwable ex) {
	    }
	}
	return new DefaultTableModel(rows, colNames);
    }

    /**
     * Return as text the method names and the values they return for methods in
     * the DatabaseMetaData interface that don't require input parameters.
     * 
     * @param dbmd
     *            the DatabaseMetaData for the Database
     * @return method names and returned values for those methods.
     */
    public static String aboutDB(DatabaseMetaData dbmd) {
	StringBuffer sb = new StringBuffer();
	Method[] m = dbmd.getClass().getMethods(); // introspect on
						   // DataBaseMetaData methods
	int nl = 0;
	for (int i = 0; i < m.length; i++) {
	    if (m[i].getName().length() > nl)
		nl = m[i].getName().length();
	}
	nl++;
	String fill = "                                                                       ";
	sb.append("Print version info");
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getName().startsWith("getDatabase")
			&& m[i].getParameterTypes().length == 0) {
		    sb.append(" " + m[i].getName()
			    + fill.substring(0, nl - m[i].getName().length())
			    + "\t" + m[i].invoke(dbmd, null));
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getName().startsWith("getDriver")
			&& m[i].getParameterTypes().length == 0) {
		    sb.append(" " + m[i].getName()
			    + fill.substring(0, nl - m[i].getName().length())
			    + "\t" + m[i].invoke(dbmd, null));
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getName().startsWith("getJDBC")
			&& m[i].getParameterTypes().length == 0) {
		    sb.append(" " + m[i].getName()
			    + fill.substring(0, nl - m[i].getName().length())
			    + "\t" + m[i].invoke(dbmd, null));
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	sb.append("Capabilities");
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getReturnType().toString().equals("boolean")
			&& m[i].getParameterTypes().length == 0) {
		    sb.append(" " + m[i].getName()
			    + fill.substring(0, nl - m[i].getName().length())
			    + "\t" + m[i].invoke(dbmd, null));
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	sb.append("\n");
	for (int i = 0; i < m.length; i++) {
	    try {
		if (m[i].getReturnType().toString().equals("int")
			&& !m[i].getName().startsWith("getDatabase")
			&& !m[i].getName().startsWith("getDriver")
			&& !m[i].getName().startsWith("getJDBC")
			&& m[i].getParameterTypes().length == 0) {
		    sb.append(" " + m[i].getName()
			    + fill.substring(0, nl - m[i].getName().length())
			    + "\t" + m[i].invoke(dbmd, null));
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	sb.append("\n");
	for (int i = 0; i < m.length; i++) {
	    try {
		if ((m[i].getReturnType().toString().indexOf("String") >= 0)
			&& !m[i].getName().startsWith("getDatabase")
			&& !m[i].getName().startsWith("getDriver")
			&& !m[i].getName().startsWith("getJDBC")
			&& m[i].getParameterTypes().length == 0) {
		    sb.append(" " + m[i].getName()
			    + fill.substring(0, nl - m[i].getName().length())
			    + "\t" + m[i].invoke(dbmd, null));
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	sb.append("\n");
	for (int i = 0; i < m.length; i++) {
	    try {
		if ((m[i].getReturnType().toString().indexOf("ResultSet") >= 0)
			&& m[i].getParameterTypes().length == 0) {
		    try {
			ResultSet rs = (ResultSet) m[i].invoke(dbmd, null);
			sb.append("\n");
			sb.append(m[i].getName() + ":");
			sb.append(printResultSet(rs)).append("\n");
		    } catch (Exception ex) {
		    }
		}
	    } catch (Exception ex) {
		sb.append(" " + m[i].getName()
			+ fill.substring(0, nl - m[i].getName().length())
			+ "\t" + ex);
	    }
	}
	return sb.toString();
    }

    /**
     * Return as a TableModel view of the ResultSet.
     * 
     * @param rs
     *            the table data for the TableModel
     * @return a TableModel of method names and returned values.
     */
    public static TableModel getTableModel(ResultSet rs) {
	try {
	    return new ResultTableModel(rs);
	} catch (SQLException ex) {
	    for (SQLException sqlex = ex; sqlex != null; sqlex = sqlex
		    .getNextException()) {
		System.err.println(sqlex.toString());
	    }
	    ex.printStackTrace();
	} catch (Exception ex) {
	    System.err.println("AboutDB.getTableModel " + ex);
	    ex.printStackTrace();
	} catch (Throwable t) {
	    System.err.println("AboutDB.getTableModel " + t);
	}
	return new DefaultTableModel();
    }

    /**
     * Return a text representation of a data table, with Column Headings from
     * the ResultSetMetaData. Fields are separated by the TAB character.
     * 
     * @param rs
     *            the table data for the TableModel
     * @return a text String representation of the table.
     */
    public static String printResultSet(ResultSet rs) {
	StringBuffer sb = new StringBuffer();
	try {
	    // Get the metadata so we know number, name, and type of columns
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int numberOfColumns = rsmd.getColumnCount();
	    // Print out the column names
	    for (int column = 0; column < numberOfColumns; column++) {
		sb.append((column == 0 ? "\n" : "\t")
			+ rsmd.getColumnLabel(column + 1));
	    }
	    // Print out each returned query row
	    while (rs.next()) {
		// Print out each column in the row
		try {
		    for (int column = 0; column < numberOfColumns; column++) {
			sb.append((column == 0 ? "\n" : "\t")
				+ rs.getObject(column + 1));
		    }
		} catch (SQLException sqlex) {
		    System.err.println("\nException: " + sqlex + "\n");
		}
	    }
	} catch (Exception ex) {
	    System.err.println("\nException: " + ex + "\n");
	}
	return sb.toString();
    }
}
