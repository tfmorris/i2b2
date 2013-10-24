/*
 * @(#) $RCSfile: JDBCTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import edu.umn.genomics.bi.dbutil.*;

/**
 * Presents a JDBC obtained database table as a TableModel.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class JDBCTableModel extends AbstractTableModel implements
	TableColumnMap {
    /* 
*/
    ListSelectionModel lsm = null;
    // DB info
    DBConnectParams dbparams = null;
    DatabaseMetaData dbmd = null;
    Vector activeStatements = new Vector();
    // Table info
    String query = null;
    // ResultSet rs = null;
    int rowCount = 0;
    int rowsRead = 0;
    // Column info
    DBColumnMap colMap[] = null;
    // Threads for collecting column statistics for each column
    Hashtable statThreads = new Hashtable(); // ColumnMap -> Thread

    // Statement List
    private Vector stmtv = new Vector();
    // ResultSet List
    private Vector rsltv = new Vector();

    // Count Thread
    Thread countThread = null;
    Statement countStmt = null;
    // Data Thread
    Thread dataThread = null;
    // Column Stats Thread

    // Listeners
    private EventListenerList listenerList = new EventListenerList();

    int queryCount = -1;
    boolean stopped = false;
    boolean skipOffset = false;
    int rowLimit = -1;
    int rowOffset = 0;
    int updateIncr = 1000;
    int debug = 0;

    private void setRowLimit(int rowLimit) {
	this.rowLimit = rowLimit;
    }

    private int getRowLimit() {
	return rowLimit;
    }

    private void setRowOffset(int rowOffset) {
	this.rowOffset = rowOffset;
    }

    private int getRowOffset() {
	return rowOffset;
    }

    /**
     * Create a JDBCTableModel, the database account must be supplied by the
     * setUser method.
     */
    public JDBCTableModel() {
    }

    /**
     * Create a JDBCTableModel with the given database account.
     */
    public JDBCTableModel(DBConnectParams dbparams) throws SQLException,
	    ClassNotFoundException {
	setUser(dbparams);
    }

    // interface QueryTableModel
    /**
     * Set the database user account.
     * 
     * @param dbparams
     *            the database user account information.
     */
    public void setUser(DBConnectParams dbparams)
	    throws ClassNotFoundException, SQLException {
	if (debug > 0)
	    System.err.println("setUser " + "\t" + dbparams);
	if (dbparams.getDriverName() != null
		&& dbparams.getDriverName().length() > 0) {
	    Class.forName(dbparams.getDriverName());
	    Driver driver = DriverManager.getDriver(dbparams.getURL());
	}
	Connection conn = DriverManager.getConnection(dbparams.getURL(),
		dbparams.getUser(), dbparams.getPassword());
	if (dbmd != null) {
	    try {
		Connection dbmdConn = dbmd.getConnection();
		if (dbmdConn != null) {
		    dbmdConn.close();
		}
	    } catch (SQLException sqlex) {
	    }
	}
	dbmd = conn.getMetaData();
	if (this.dbparams != null && !this.dbparams.userEquals(dbparams)) {
	    cancelQuery();
	}
	this.dbparams = dbparams;
    }

    /**
     * Return the database user account.
     * 
     * @return the database user account information.
     */
    public DBConnectParams getUser() {
	return dbparams;
    }

    synchronized public int getCountForQuery(String query) {
	if (query != null) {
	    if (debug > 1)
		System.err.println("getCountForQuery:\t" + query);
	    int count = -1;
	    try {
		ResultSet rs = execQuery(query, true);
		try {
		    if (rs != null && rs.next()) {
			Object co = rs.getObject(1);
			count = ((Number) co).intValue();
		    }
		} catch (SQLException sqlex) {
		    System.err.println("getCountForQuery\t" + query + "\t"
			    + sqlex);
		} finally {
		    rs.close();
		}
	    } catch (Exception ex) {
		System.err.println("getCountForQuery\t" + query + "\t" + ex);
	    }
	    return count;
	}
	return -1;
    }

    synchronized public int getCountForQuery() {
	if (queryCount < 0) {
	    String q = getCountQuery();
	    queryCount = getCountForQuery(q);
	    setRowCount(queryCount);
	    fireStatusEvent("", rowsRead, getRowLimit(), queryCount, null);
	}
	return queryCount;
    }

    /**
     * Set the query that will be executed to provide data for the TableModel.
     * 
     * @param query
     *            The database query.
     * @param pageSize
     *            The number of rows to retrieve. (The SQL99 LIMIT parameter)
     * @param page
     *            The page of rows to retrieve (starting from page 1). (The
     *            SQL99 OFFSET parameter = pageSize * (page-1) ).
     */
    synchronized public void setQuery(String query, int pageSize, int page) {
	String sql = query != null ? query.trim() : null;
	if (sql != null && sql.endsWith(";")) {
	    sql = sql.substring(0, sql.lastIndexOf(";"));
	}
	if (this.query == null || !this.query.equals(sql)) {
	    cancelQuery();
	    this.query = sql;
	    setCountQuery();
	}
	setRowLimit(pageSize);
	setRowOffset(pageSize * (page - 1));
	String pageSQL = DBPage.getPageQuery(dbmd, sql, pageSize, page);
	if (pageSQL != null) {
	    skipOffset = false;
	    setDataQuery(pageSQL);
	} else {
	    skipOffset = true;
	    setDataQuery(sql);
	}
    }

    /**
     * Set the query that will be executed to provide data for the TableModel.
     * 
     * @param query
     *            The database query.
     */
    synchronized public void setQuery(String query) {
	String sql = query != null ? query.trim() : null;
	if (sql != null && sql.endsWith(";")) {
	    sql = sql.substring(0, sql.lastIndexOf(";"));
	}
	if (this.query == null || !this.query.equals(sql)) {
	    cancelQuery();
	    this.query = sql;
	    setCountQuery();
	}
	setRowLimit(-1);
	setRowOffset(0);
	skipOffset = false;
	setDataQuery(sql);
    }

    /**
     * Set the query that will be executed to provide data for the TableModel.
     * 
     * @param query
     *            The database query.
     */
    synchronized private void setDataQuery(String query) {
	if (debug > 0)
	    System.err.println("setQuery " + "\t" + query);
	setReadCount(0);
	// If dbparams && connection
	if (dbparams != null) {
	    cancelQueries();
	    fireStatusEvent("Executing query", null);
	    // Start Data Thread
	    final String q = query;
	    dataThread = new Thread() {
		public void run() {
		    try {
			if (debug > 0)
			    System.err.println("dataThread " + "\t"
				    + getQuery());
			readData(execQuery(q, false));
		    } catch (Exception ex) {
			fireStatusEvent(null, ex);
			System.err.println(this + " readData " + ex);
			ex.printStackTrace();
		    } catch (OutOfMemoryError err) {
			fireStatusEvent(err.toString(), null);
			throw (err);
		    }
		}
	    };
	    dataThread.start();
	}
    }

    /**
     * Set the query that will be executed to provide data for the TableModel.
     * 
     * @param query
     *            The database query.
     */
    synchronized private void setCountQuery() {
	if (debug > 0)
	    System.err.println("setQuery " + "\t" + query);
	// If dbparams && connection
	if (dbparams != null) {
	    // Start Count Thread
	    if (this.query != null) {
		countThread = new Thread() {
		    public void run() {
			int cnt = getCountForQuery();
			countThread = null;
		    }
		};
		countThread.start();
	    }
	}
    }

    /**
     * Return the database query.
     * 
     * @return the database query.
     */
    public String getQuery() {
	return query;
    }

    /**
     * Cancel any outstanding database queries.
     */
    synchronized public void cancelQuery() {
	cancelCount();
	cancelQueries();
    }

    private void cancelQueries() {
	if (dataThread != null) {
	    dataThread.interrupt();
	}
	// cancel all statements
	try {
	    cancelStatements();
	} catch (Exception ex) {
	}
	try {
	    cancelResultSets();
	} catch (Exception ex) {
	}
	// stop all threads
	// delete all column Maps

	DBColumnMap cMap[] = colMap;
	colMap = null;
	rowCount = 0;
	if (cMap != null) {
	    for (int c = 0; c < cMap.length; c++) {
		// cMap[c].setState(CellMap.INVALID);
		Thread t = (Thread) statThreads.get(cMap[c]);
		if (t != null) {
		    t.interrupt();
		}
	    }
	}
    }

    private void cancelCount() {
	if (countStmt != null) {
	    try {
		countStmt.cancel();
		countStmt.close();
	    } catch (SQLException sqlex) {
		for (SQLException ex = sqlex; ex != null; ex = ex
			.getNextException()) {
		    System.err.println("cancelCount: " + ex);
		}
	    }
	}
	if (countThread != null) {
	    try {
		countThread.interrupt();
	    } catch (Exception ex) {
	    }
	    countThread = null;
	}
	queryCount = -1;
    }

    /**
     * Cancel any outstanding database queries.
     */
    synchronized public void stopQuery() {
	setRowLimit(rowsRead);
	fireStatusEvent("Stopped: ", rowsRead, rowCount, queryCount, null);
    }

    private void addStatement(Statement stmt) {
	stmtv.addElement(stmt);
	try {
	    if (debug > 3)
		System.err.println("stmtv add: " + stmt.getConnection() + " "
			+ stmtv);
	} catch (Exception ex) {
	    if (debug > 3)
		System.err.println("stmtv add: " + ex);
	}
    }

    private void removeStatement(Statement stmt) {
	stmtv.removeElement(stmt);
	if (debug > 3)
	    System.err.println("stmtv rmv: " + stmtv);
    }

    private void cancelStatements() throws SQLException {
	for (Enumeration e = stmtv.elements(); e.hasMoreElements();) {
	    Statement stmt = (Statement) e.nextElement();
	    if (stmt != null) {
		stmt.cancel();
		stmt.close();
		removeStatement(stmt);
	    }
	}
    }

    private void addResultSet(ResultSet rslt) {
	rsltv.addElement(rslt);
	try {
	    if (debug > 3)
		System.err.println("rsltv add: "
			+ rslt.getStatement().getConnection() + " " + rsltv);
	} catch (Exception ex) {
	    if (debug > 3)
		System.err.println("rsltv add: " + ex);
	}
    }

    private void removeResultSet(ResultSet rslt) {
	try {
	    rslt.getStatement().cancel();
	    rslt.getStatement().getConnection().close();
	    rslt.getStatement().close();
	    rslt.close();
	} catch (Exception ex) {
	    System.err.println(" removeResultSet " + ex);
	}
	rsltv.removeElement(rslt);
	if (debug > 3)
	    System.err.println("rsltv rmv: " + rsltv);
    }

    private void cancelResultSets() throws SQLException {
	for (Enumeration e = rsltv.elements(); e.hasMoreElements();) {
	    ResultSet rslt = (ResultSet) e.nextElement();
	    if (rslt != null) {
		removeResultSet(rslt);
	    }
	}
    }

    /**
     * Execute a query using this database account.
     * 
     * @param sql
     *            the query to execute.
     * @return the ResultSet for the query.
     */
    public ResultSet execQuery(String sql, boolean isCount) throws SQLException {
	if (debug > 1)
	    System.err.println("execQuery " + "\t" + sql);
	DBConnectParams dbusr = getUser();
	Connection conn = DriverManager.getConnection(dbusr.getURL(), dbusr
		.getUser(), dbusr.getPassword());
	Statement stmt = conn.createStatement();
	if (!isCount) {
	    addStatement(stmt);
	} else {
	    countStmt = stmt;
	}
	if (debug > 4)
	    System.err.println("execQuery " + "\t" + stmt);
	ResultSet rs = stmt.executeQuery(sql);
	if (debug > 4)
	    System.err.println("execQuery " + "\t" + rs);
	if (!isCount) {
	    addResultSet(rs);
	    removeStatement(stmt);
	}
	return rs;
    }

    synchronized private void setRowCount(int cnt) {
	if (cnt > rowCount)
	    rowCount = cnt;
    }

    private void setReadCount(int cnt) {
	rowsRead = cnt;
	setRowCount(cnt);
    }

    /**
     * Return a query the will get the row count for the query.
     * 
     * @return the SQL query.
     */
    public String getCountQuery() {
	if (!supportsSelectInFrom()) {
	    String q = getQuery();
	    if (q == null) {
		return null;
	    }
	    String qu = q.toUpperCase();
	    String sel = "SELECT";
	    String frm = "FROM";
	    int si = qu.indexOf(sel);
	    int fi = qu.indexOf(frm);
	    if (si >= 0 && fi > 0) {
		String selClause = qu.substring(0, fi);
		if (selClause.indexOf("COUNT") >= 0) {
		    return null;
		}
	    }
	}
	return getStatQuery("count(*)", null);
    }

    /**
     * Return a query the will get the min and max values for the given column
     * for the query.
     * 
     * @param colName
     *            The name of the column from the query.
     * @return the SQL query.
     */
    public String getMinMaxQuery(String colName) {
	return getStatQuery("min(" + colName + "), max(" + colName + ")",
	// colName + " is NOT NULL");
		null);
    }

    /**
     * Return a query the will get the variance for the given column for the
     * query.
     * 
     * @param colName
     *            The name of the column from the query.
     * @return the SQL query.
     */
    public String getVarQuery(String colName) {
	return getStatQuery("variance(" + colName + ")", null);
    }

    /**
     * Return a query the will get the average for the given column for the
     * query.
     * 
     * @param colName
     *            The name of the column from the query.
     * @return the SQL query.
     */
    public String getAvgQuery(String colName) {
	return getStatQuery("avg(" + colName + ")", null);
    }

    /**
     * Return a query the will get the count of distinct values for the given
     * column for the query.
     * 
     * @param colName
     *            The name of the column from the query.
     * @return the SQL query.
     */
    public String getDistinctQuery(String colName) {
	return getStatQuery("count(distinct " + colName + ")", null);
    }

    /**
     * Return a query the will get the count of null values for the given column
     * for the query.
     * 
     * @param colName
     *            The name of the column from the query.
     * @return the SQL query.
     */
    public String getNullCountQuery(String colName) {
	return getStatQuery("count(*)", colName + " is null");
    }

    private String getStatQuery(String selectClause, String whereClause) {
	String sql = null;
	String dbname = "";
	try {
	    dbname = dbmd.getDatabaseProductName();
	} catch (SQLException sqlexd) {
	    for (SQLException ex = sqlexd; ex != null; ex = ex
		    .getNextException()) {
		System.err.println("getStatQuery: " + ex);
	    }
	}
	if (dbname != null && dbname.equalsIgnoreCase("Oracle")) {
	    sql = "select " + selectClause + " from (" + getQuery() + ")";
	    if (whereClause != null) {
		sql += " where " + whereClause;
	    }
	} else if (dbname != null && dbname.equalsIgnoreCase("PostGreSQL")) {
	    sql = "select " + selectClause + " from (" + getQuery()
		    + ") derivedtable";
	    if (whereClause != null) {
		sql += " where " + whereClause;
	    }
	} else {
	    String qu = query.toUpperCase();
	    String sel = "SELECT";
	    String frm = "FROM";
	    int si = qu.indexOf(sel);
	    int fi = qu.indexOf(frm);
	    sql = query.substring(0, si + sel.length());
	    sql += " " + selectClause + " ";
	    sql += query.substring(fi);
	    if (whereClause != null) {
		String whr = "WHERE";
		int wi = qu.lastIndexOf(whr);
		if (wi < 0) {
		    sql += " where " + whereClause;
		} else {
		    sql += " and " + whereClause;
		}
	    }

	    if (debug > 2)
		System.err.println("getStatQuery " + sql);
	}
	return sql;
    }

    /**
     * Create a Thread to collect stats for the ColumnMap.
     * 
     * @param cmap
     *            The ColumnMap
     */
    private Thread collectStatsThread(final DBColumnMap cMap) {
	Thread statsThread = new Thread() {
	    final DBColumnMap cmap = cMap;

	    public void run() {
		try {
		    if (debug > 3)
			System.err.println("statsThread " + "\t"
				+ cmap.getName());
		    collectStats(cmap);
		} catch (Exception ex) {
		    System.err.println(this + " statsThread " + ex);
		    // ex.printStackTrace();
		}
	    }
	};
	statThreads.put(cMap, statsThread);
	statsThread.start();
	return statsThread;
    }

    private boolean supportsSelectInFrom() {
	try {
	    String dbname = dbmd.getDatabaseProductName();
	    if (dbname != null) {
		if (dbname.equalsIgnoreCase("Oracle")) {
		    return true;
		} else if (dbname.equalsIgnoreCase("PostGreSQL")) {
		    return true;
		} else if (dbname.equalsIgnoreCase("MySQL")) {
		    if (dbmd.getDatabaseMajorVersion() >= 5) {
			return true;
		    } else if (dbmd.getDatabaseMajorVersion() == 4
			    && dbmd.getDatabaseMinorVersion() >= 1) {
			return true;
		    }
		}
	    }
	} catch (SQLException sqlexd) {
	    for (SQLException ex = sqlexd; ex != null; ex = ex
		    .getNextException()) {
		System.err.println("supportsSelectInFrom: " + ex);
	    }
	} catch (Exception ex) {
	    System.err.println("supportsSelectInFrom: " + ex);
	    ex.printStackTrace();
	}
	return false;
    }

    /**
     * Generate the stats for this columnmap.
     * 
     * @param cmap
     *            The ColumnMap
     */
    private void collectStats(DBColumnMap cmap) {
	String colName = cmap.getName();
	try {
	    String dbname = dbmd.getDatabaseProductName();
	    if (!supportsSelectInFrom()) {
		String qu = query.toUpperCase();
		String sel = "SELECT";
		String frm = "FROM";
		int si = qu.indexOf(sel);
		int fi = qu.indexOf(frm);
		String fields = query.substring(si + sel.length(), fi).trim();
		if (fields.equals("*")) {
		} else {
		    // Java1.4 use split
		    Vector fv = new Vector();
		    for (StringTokenizer st = new StringTokenizer(fields, ","); st
			    .hasMoreTokens();) {
			fv.add(st.nextToken());
		    }
		    String field = ((String) fv.get(cmap.getColumnIndex()))
			    .trim();
		    if (field.indexOf(' ') > 0) {
			field = field.substring(0, field.lastIndexOf(' '))
				.trim();
		    }
		    int bp = field.lastIndexOf('(');
		    if (bp > 0) {
			int ep = field.indexOf(')', bp);
			if (ep > bp) {
			    field = field.substring(bp + 1, ep).trim();
			}
			return; // Forget about stats on columns with functions
		    }
		    colName = field;
		}
	    }
	} catch (SQLException sqlexd) {
	    for (SQLException ex = sqlexd; ex != null; ex = ex
		    .getNextException()) {
		System.err.println("collectStats: " + ex);
	    }
	} catch (Exception ex) {
	    System.err.println("collectStats: " + ex);
	}

	String q = getMinMaxQuery(colName);

	try {
	    if (debug > 1)
		System.err.println("collectStats:\t" + q);
	    ResultSet rs = execQuery(q, false);
	    try {
		if (rs != null && rs.next()) {
		    cmap.setMinObj(rs.getObject(1));
		    cmap.setMaxObj(rs.getObject(2));
		    if (cmap.isNumber()) {
			cmap.min = ((Number) cmap.minObj).doubleValue();
			cmap.max = ((Number) cmap.maxObj).doubleValue();
		    } else if (cmap.isDate()) {
			cmap.min = (double) ((java.util.Date) cmap.minObj)
				.getTime();
			cmap.max = (double) ((java.util.Date) cmap.maxObj)
				.getTime();
		    } else {
			cmap.min = 0.;
		    }
		}
	    } catch (SQLException sqlex) {
		System.err.println("getMinMaxQuery\t" + q + "\t" + sqlex);
	    } finally {
		removeResultSet(rs);
		rs.close();
	    }
	} catch (Exception ex) {
	    System.err.println("collectStats\t" + q + "\t" + ex);
	}

	if (Thread.interrupted())
	    return;

	if (cmap.isNumber()) {
	    q = getAvgQuery(colName);
	    try {
		if (debug > 2)
		    System.err.println("collectStats:\t" + q);
		ResultSet rs = execQuery(q, false);
		try {
		    if (rs != null && rs.next()) {
			cmap.setAvg(((Number) rs.getObject(1)).doubleValue());
		    }
		} catch (SQLException sqlex) {
		    System.err.println("getAvgQuery\t" + q + "\t" + sqlex);
		} finally {
		    removeResultSet(rs);
		    rs.close();
		}
	    } catch (Exception ex) {
		System.err.println("getAvgQuery\t" + q + "\t" + ex);
	    }
	}

	if (Thread.interrupted())
	    return;

	if (true) {
	    q = getDistinctQuery(colName);
	    try {
		if (debug > 2)
		    System.err.println("collectStats:\t" + q);
		ResultSet rs = execQuery(q, false);
		try {
		    if (rs != null && rs.next()) {
			cmap.setDistinctCount(((Number) rs.getObject(1))
				.intValue());
			if (!cmap.isNumber() && !cmap.isDate()) {
			    if (cmap.getDistinctCount() > cmap.getMax())
				cmap.setMax(cmap.getDistinctCount() - 1);
			}
		    }
		} catch (SQLException sqlex) {
		    System.err.println("getDistinctQuery\t" + q + "\t" + sqlex);
		} finally {
		    removeResultSet(rs);
		    rs.close();
		}
	    } catch (Exception ex) {
		System.err.println("getDistinctQuery\t" + q + "\t" + ex);
	    }
	}

	if (Thread.interrupted())
	    return;

	if (true) {
	    q = getNullCountQuery(colName);
	    try {
		if (debug > 2)
		    System.err.println("collectStats:\t" + q);
		ResultSet rs = execQuery(q, false);
		try {
		    if (rs != null && rs.next()) {
			cmap
				.setNullCount(((Number) rs.getObject(1))
					.intValue());
		    }
		} catch (SQLException sqlex) {
		    System.err
			    .println("getNullCountQuery\t" + q + "\t" + sqlex);
		} finally {
		    removeResultSet(rs);
		    rs.close();
		}
	    } catch (Exception ex) {
		System.err.println("getNullCountQuery\t" + q + "\t" + ex);
	    }
	}

    }

    /**
     * Read the ResultSet data for the query, creating columnMaps to hold the
     * values for each column.
     */
    private void readData(ResultSet rs) throws SQLException {
	// determine columns classes
	ResultSetMetaData rsmd = rs.getMetaData();
	int ncol = rsmd.getColumnCount();
	if (debug > 2)
	    System.err.println("Number of Columns: " + ncol);
	// create ColumnMaps
	DBColumnMap cMap[] = new DBColumnMap[ncol];
	for (int c = 0; c < ncol; c++) {
	    String name = rsmd.getColumnLabel(c + 1);
	    if (debug > 2)
		System.err.println("  Column " + c + "\t" + name);
	    int sqlType = rsmd.getColumnType(c + 1);
	    // determine columns classes
	    Class colClass = null;
	    try { // JDBC 1.2
		String ctn = rsmd.getColumnClassName(c + 1);
		colClass = Class.forName(ctn);
	    } catch (Exception ex) {
		colClass = DBColumnMap.getClassForSqlType(sqlType);
	    }
	    cMap[c] = new DBColumnMap(this, lsm, name, c, colClass, sqlType);
	    cMap[c].setState(CellMap.MAPPING);
	    // collectStatsThread(cMap[c]);
	}

	boolean structureChanged = false;
	if (colMap != null && colMap.length == cMap.length) {
	    for (int c = 0; c < ncol; c++) {
		if (colMap[c].getColumnClass() != cMap[c].getColumnClass()
			|| !colMap[c].getName().equals(cMap[c].getName())) {
		    structureChanged = true;
		    break;
		}
	    }
	} else {
	    structureChanged = true;
	}
	if (structureChanged) {
	    colMap = cMap;
	    fireTableStructureChanged();
	} else {
	    fireTableDataChanged();
	}
	// If count has been done, let listeners know total number of rows
	if (rowCount > 0) {
	    fireTableRowsInserted(0, getRowCount());
	}
	try {
	    if (skipOffset) {
		int offset = getRowOffset();
		for (int r = 0; r < offset && rs.next(); r++) {
		}
	    }
	    int lastCnt = 0;
	    for (int r = 0; (rowLimit < 0 || r < rowLimit) && rs.next(); r++) {
		if (Thread.interrupted()) {
		    throw new InterruptedException("Query Interrupted");
		}
		for (int c = 0; c < ncol; c++) {
		    cMap[c].setValueAt(rs, r);
		}
		int rcnt = r + 1;
		setReadCount(rcnt);
		if (rcnt % updateIncr == 0) {
		    if (r + 1 == rowCount) {
			fireTableRowsUpdated(lastCnt, r);
			fireStatusEvent("", rcnt, rowLimit < 0 ? -1 : rowLimit,
				queryCount, null);
		    } else {
			fireTableRowsInserted(lastCnt, r);
			fireStatusEvent("", rcnt, rowLimit < 0 ? rowCount
				: rowLimit, queryCount, null);
		    }
		    lastCnt = rcnt;
		}
	    }
	    int nrow = getRowCount();
	    if (lastCnt < nrow) {
		fireTableRowsUpdated(lastCnt, nrow - 1);
	    } else if (lastCnt > nrow) {
		fireTableRowsDeleted(lastCnt + 1, nrow - 1);
	    }
	    fireStatusEvent("", rowsRead, rowLimit < 0 ? rowCount : rowLimit,
		    queryCount, null);
	} catch (InterruptedException iex) {
	    fireStatusEvent("Interrupted: ", rowsRead, rowCount, queryCount,
		    null);
	}
	for (int c = 0; c < ncol; c++) {
	    cMap[c].setSortOrder(CellMap.ALPHANUMSORT);
	    cMap[c].sortColumn();
	    cMap[c].collectStats();
	}
	for (int c = 0; c < ncol; c++) {
	    cMap[c].setState(CellMap.MAPPED);
	    if (debug > 3)
		System.err.println(c + " " + cMap[c].getName() + " "
			+ cMap[c].getState() + " " + cMap[c].getMin() + " to "
			+ cMap[c].getMax());
	}
    }

    // interface TableModel
    /**
     * Returns the number of rows in the model. A <code>JTable</code> uses this
     * method to determine how many rows it should display. This method should
     * be quick, as it is called frequently during rendering.
     * 
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
	return rowLimit < 0 || rowCount < rowLimit ? rowCount : rowLimit;
    }

    /**
     * Returns the number of columns in the model. A <code>JTable</code> uses
     * this method to determine how many columns it should create and display by
     * default.
     * 
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount() {
	DBColumnMap cMap[] = colMap;
	if (cMap != null) {
	    return cMap.length;
	}
	return 0;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>. This is used
     * to initialize the table's column header name. Note: this name does not
     * need to be unique; two columns in a table can have the same name.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the name of the column
     */
    public String getColumnName(int columnIndex)
	    throws ArrayIndexOutOfBoundsException {
	DBColumnMap cMap[] = colMap;
	if (cMap == null || columnIndex < 0 || columnIndex >= cMap.length) {
	    throw new ArrayIndexOutOfBoundsException(columnIndex
		    + " is not a valid columnIndex in a table with "
		    + (cMap == null || cMap.length < 1 ? "no" : ""
			    + cMap.length) + " columns.");
	}
	return cMap[columnIndex].getName();
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column. This is used by the <code>JTable</code> to set up a default
     * renderer and editor for the column.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass(int columnIndex)
	    throws ArrayIndexOutOfBoundsException {
	DBColumnMap cMap[] = colMap;
	if (cMap == null || columnIndex < 0 || columnIndex >= cMap.length) {
	    throw new ArrayIndexOutOfBoundsException(columnIndex
		    + " is not a valid columnIndex in a table with "
		    + (cMap == null || cMap.length < 1 ? "no" : ""
			    + cMap.length) + " columns.");
	}
	return cMap[columnIndex].getColumnClass();
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
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     * 
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex)
	    throws ArrayIndexOutOfBoundsException {
	DBColumnMap cMap[] = colMap;
	if (rowIndex < 0 || rowIndex > rowCount) {
	    throw new ArrayIndexOutOfBoundsException(rowIndex
		    + " is not a valid rowIndex in a table with " + rowCount
		    + " rows.");
	}
	if (cMap == null || columnIndex < 0 || columnIndex >= cMap.length) {
	    throw new ArrayIndexOutOfBoundsException(columnIndex
		    + " is not a valid columnIndex in a table with "
		    + (cMap == null || cMap.length < 1 ? "no" : ""
			    + cMap.length) + " columns.");
	}
	return cMap[columnIndex].getValueAt(rowIndex);
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
    // public void setValueAt(Object aValue, int rowIndex, int columnIndex);
    // interface TableColumnMap
    /**
     * Return a ColumnMap for the column in the TableModel at columnIndex.
     * 
     * @param columnIndex
     *            the index of the TableModel column.
     * @return a ColumnMap for the TableModel column at columnIndex.
     */
    public ColumnMap getColumnMap(int columnIndex) {
	DBColumnMap cMap[] = colMap;
	if (cMap != null && columnIndex >= 0 && columnIndex < cMap.length) {
	    return cMap[columnIndex];
	}
	return null;
    }

    /**
     * Sets the row selection model for this table to newModel and registers
     * with for listener notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    public void setSelectionModel(ListSelectionModel newModel) {
	this.lsm = newModel;
	DBColumnMap cMap[] = colMap;
	if (cMap != null) {
	    for (int i = 0; i < cMap.length; i++) {
		cMap[i].setSelectionModel(lsm);
	    }
	}
    }

    /**
     * Returns the ListSelectionModel that is used to maintain row selection
     * state.
     * 
     * @return the object that provides row selection state.
     */
    public ListSelectionModel getSelectionModel() {
	return lsm;
    }

    // Status Listener

    /**
     * Adds the listener to be notified of status changes of db connection.
     * 
     * @param listener
     *            the StatusListener to add
     */
    public void addStatusListener(StatusListener listener) {
	listenerList.add(StatusListener.class, listener);
    }

    /**
     * Removes the listener from the notification list.
     * 
     * @param listener
     *            the StatusListener to remove
     */
    public void removeStatusListener(StatusListener listener) {
	listenerList.add(StatusListener.class, listener);
    }

    /**
   *
   */
    protected void fireStatusEvent(String message, int rowsRead, int rowCount,
	    int totalCount, Exception ex) {
	int offset = getRowOffset();
	String msg = (message != null ? message : "")
		+ (rowsRead < 0 ? ""
			: (" Read rows " + (offset + 1) + " to " + (offset + rowsRead)))
		+ (rowCount < 0 ? "" : (" of " + rowCount + " requested "))
		+ (totalCount > 0 ? (" from " + totalCount + " rows in table")
			: "");
	fireStatusEvent(msg, null);
    }

    /**
     * Notify Listeners of change.
     */
    protected void fireStatusEvent(String msg, Exception ex) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	StatusEvent statusEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == StatusListener.class) {
		// Lazily create the event:
		if (statusEvent == null)
		    statusEvent = new StatusEvent(this, msg, ex);
		((StatusListener) listeners[i + 1]).statusChanged(statusEvent);
	    }
	}
    }

}
