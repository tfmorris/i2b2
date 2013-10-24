/*
 * @(#) $RCSfile: DBPage.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.sql.*;

/**
 * Make queries that page through a results table. This will use SQL99 LIMIT and
 * OFFSET syntax when it is available.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBPage {
    /**
     * Try to modify the given query so that it will provide the row LIMIT and
     * OFFSET.
     * 
     * @param dbmd
     *            The metadata for the database connection.
     * @param query
     *            The query to modify.
     * @param pageSize
     *            The number of rows the query should attempt to return.
     * @param page
     *            The page of rows to return, ( OFFSET = pageSize * (page - 1) )
     * @return A modified query that will provide the row LIMIT and OFFSET, else
     *         null if this was unable to build such a query for this database.
     */
    public static String getPageQuery(DatabaseMetaData dbmd, String query,
	    int pageSize, int page) {
	boolean supportsLimit = false;
	boolean supportsOffset = false;
	boolean supportsRowNum = false;
	String orderTerm = null;
	String sql = null;
	String dbname = "";
	int dbMajorVersion = -1;
	int dbMinorVersion = -1;
	String dbProductVersion = "";
	int nrow = pageSize;
	int offset = page > 0 ? pageSize * (page - 1) : 0;
	try {
	    dbname = dbmd.getDatabaseProductName();
	} catch (SQLException sqlexd) {
	    for (SQLException ex = sqlexd; ex != null; ex = ex
		    .getNextException()) {
		System.err.println("getPageQuery: " + ex);
	    }
	}
	try {
	    dbMajorVersion = dbmd.getDatabaseMajorVersion();
	    dbMinorVersion = dbmd.getDatabaseMinorVersion();
	} catch (AbstractMethodError amerr) {
	    System.err.println("getPageQuery: " + amerr);
	} catch (SQLException sqlexd) {
	    for (SQLException ex = sqlexd; ex != null; ex = ex
		    .getNextException()) {
		System.err.println("getPageQuery: " + ex);
	    }
	}
	try {
	    dbProductVersion = dbmd.getDatabaseProductVersion();
	} catch (AbstractMethodError amerr) {
	    System.err.println("getPageQuery: " + amerr);
	} catch (SQLException sqlexd) {
	    for (SQLException ex = sqlexd; ex != null; ex = ex
		    .getNextException()) {
		System.err.println("getPageQuery: " + ex);
	    }
	}
	if (dbname != null && dbname.equalsIgnoreCase("Oracle")) {
	    supportsRowNum = true;
	} else if (dbname != null && dbname.equalsIgnoreCase("PostGreSQL")) {
	    supportsLimit = true;
	    supportsOffset = true;
	    orderTerm = "oid";
	} else if (dbname != null && dbname.equalsIgnoreCase("MySQL")) {
	    supportsLimit = true;
	    if (dbMajorVersion > 4) {
		supportsOffset = true;
	    } else if (dbMajorVersion == 4) {
		if (dbMinorVersion < 1
			&& dbProductVersion.compareTo("4.0.6") < 0) {
		    supportsOffset = false;
		} else {
		    supportsOffset = true;
		}
	    }
	}
	if (supportsLimit && supportsOffset) {
	    // LIMIT offset , nrows
	    // LIMIT nrows
	    // LIMIT nrows OFFSET offset // 4.1 and later
	    String qu = query.toUpperCase();
	    String limitKw = "LIMIT";
	    String offsetKw = "OFFSET";
	    int lsi = qu.lastIndexOf(limitKw);
	    int osi = qu.lastIndexOf(offsetKw);
	    if (lsi >= 0) {
		int lei = lsi + limitKw.length();
		sql = query.substring(0, lsi + limitKw.length());
		// Now find the first occurance of any character that is not 0-9
		// or comma
		for (lei = lsi + limitKw.length(); lei < query.length(); lei++) {
		    char ch = query.charAt(lei);
		    if (Character.isDigit(ch) || Character.isWhitespace(ch)
			    || ch == ',') {
			continue;
		    }
		    break;
		}
		String sb = query.substring(lsi + limitKw.length(), lei);
		if (osi >= 0) {
		    int oei = osi + offsetKw.length();
		    // Now find the first occurance of any character that is not
		    // 0-9 or whitespace
		    for (oei = lsi + limitKw.length(); oei < query.length(); oei++) {
			char ch = query.charAt(oei);
			if (Character.isDigit(ch) || Character.isWhitespace(ch)) {
			    continue;
			}
			break;
		    }
		    if (lsi < osi) {
			sql = query.substring(0, lsi)
				+ "\n"
				+ limitKw
				+ " "
				+ nrow
				+ " "
				+ offsetKw
				+ " "
				+ offset
				+ (oei < query.length() ? query.substring(oei)
					: "");
		    } else {
			sql = query.substring(0, osi)
				+ "\n"
				+ limitKw
				+ " "
				+ nrow
				+ " "
				+ offsetKw
				+ " "
				+ offset
				+ (lei < query.length() ? query.substring(lei)
					: "");
		    }
		} else {
		    sql = query.substring(0, lsi)
			    + "\n"
			    + limitKw
			    + " "
			    + (offset > 0 ? (offset + "," + nrow) : ("" + nrow))
			    + (lei < query.length() ? query.substring(lei) : "");
		}
	    } else {
		sql = query + "\n" + limitKw + " " + nrow + " " + offsetKw
			+ " " + offset;
	    }
	} else if (supportsLimit) {
	    // LIMIT offset , nrows
	    // LIMIT nrows
	    // LIMIT nrows OFFSET offset // 4.1 and later
	    String qu = query.toUpperCase();
	    String limitKw = "LIMIT";
	    int lsi = qu.indexOf(limitKw);
	    if (lsi >= 0) {
		int lei = lsi + limitKw.length();
		sql = query.substring(0, lsi + limitKw.length());
		// Now find the first occurance of any character that is not 0-9
		// or comma
		for (lei = lsi + limitKw.length(); lei < query.length(); lei++) {
		    char ch = query.charAt(lei);
		    if (Character.isDigit(ch) || Character.isWhitespace(ch)
			    || ch == ',') {
			continue;
		    }
		    break;
		}
		String sb = query.substring(lsi + limitKw.length(), lei);
		sql = query.substring(0, lsi) + "\n" + limitKw + " "
			+ (offset > 0 ? (offset + "," + nrow) : ("" + nrow))
			+ (lei < query.length() ? query.substring(lei) : "");
	    } else {
		sql = query + "\n" + limitKw + " "
			+ (offset > 0 ? (offset + "," + nrow) : ("" + nrow));
	    }
	} else if (supportsRowNum) {
	    /*
	     * select from select my_tmp, ROWNUM SBQ_ROW_NUM where RN >
	     */
	} else {
	    sql = null;
	}
	if (!true)
	    System.err.println("getPageQuery " + sql);
	return sql;
    }
}
