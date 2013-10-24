/*
 * @(#) $RCSfile: DBBrowser.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import edu.umn.genomics.bi.dbutil.*;
import edu.umn.genomics.component.DoSpinner;

/**
 * Browse the contents of a database, and supply a TabelModel for the table
 * selected.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBBrowser extends AbstractTableSource {
    // 
    int loginTimeout = 10;
    // known database accounts
    DBAccountListModel dbmodel;
    JComboBox dbChooser;
    DBConnectParams dbuser;

    Connection conn;
    Statement stmt;
    DatabaseMetaData dbmd;
    JFrame frame;
    // displays query row count
    JLabel rowLabel = new JLabel("Rows     ");
    // connection status
    JTextField status;
    // query status
    JTextField queryStatus;
    // split panes
    JSplitPane spltc;
    JSplitPane splts;
    JSplitPane splt;
    JSplitPane spltq;
    JSplitPane spltr;
    TreeSet dbDataTypes = new TreeSet();
    // Catalogs
    DefaultListModel catalogModel;
    JList catalogList;
    JScrollPane gjsp;
    // Schemas
    DefaultListModel schemaModel;
    JList schemaList;
    JScrollPane sjsp;
    // Tables
    DefaultListModel tableModel;
    JList tableList;
    JScrollPane tjsp;

    // columns
    DefaultListModel colListModel;
    JList colList;

    // columns info
    DefaultTableModel colModel;
    JTable colTable;
    JScrollPane cjsp;

    // Primary Key Info
    ResultTableModel pKeyModel;
    JTable pKeyTable;
    JScrollPane pKeyjsp;

    // Foreign Key Info
    ResultTableModel fKeyModel;
    JTable fKeyTable;
    JScrollPane fKeyjsp;

    // Exported Key Info
    ResultTableModel xKeyModel;
    JTable xKeyTable;
    JScrollPane xKeyjsp;

    // index info
    DefaultTableModel idxModel;
    JTable idxTable;
    JScrollPane idxjsp;

    // TabbedPane for Columns, Database Info, etc.
    JTabbedPane cPnl;
    // Database MetaData Info Panel
    DBInfoPanel dbinfoPanel = new DBInfoPanel();
    // SQL query
    JTextArea queryText;
    JScrollPane qjsp;
    // query results table
    JDBCTableModel rowModel;
    JTableView rowTable;
    JScrollPane rjsp;
    //
    JButton submitBtn = new JButton("submit");
    JButton stopBtn = new JButton("stop");

    // Paging through data
    Box pgBox = new Box(BoxLayout.X_AXIS);
    JLabel rowsLbl = new JLabel("Rows:");
    JLabel pageLbl = new JLabel("Page:");
    JCheckBox usePaging = new JCheckBox("Paging?");
    JComponent pageSize = DoSpinner.getComponent(100, 1, 1000000, 1);
    JComponent curPage = DoSpinner.getComponent(1, 1, 1000000, 1);

    // Launch tTableView with ResultTable
    TableView tableView = null;
    JButton tableViewBtn = new JButton("TableView");

    StatusListener statusListener = new StatusListener() {
	public void statusChanged(StatusEvent e) {
	    setQueryStatus(e.getStatus());
	}
    };

    static Hashtable sqlTypeName = SQLTypeNames.getSharedInstance();

    private static String getSqlTypeFor(Class jc) {
	try {
	    if (java.lang.Double.class.isAssignableFrom(jc)) {
		return "NUMBER";
	    } else if (java.lang.Integer.class.isAssignableFrom(jc)) {
		return "INTEGER";
	    } else if (java.lang.Number.class.isAssignableFrom(jc)) {
		return "NUMBER";
	    } else if (java.util.Date.class.isAssignableFrom(jc)) {
		return "DATE";
	    } else {
		return "VARCHAR(32)";
	    }
	} catch (Exception ex) {
	    System.err.println("getSqlTypeFor " + ex);
	}
	return null;
    }

    private Connection getConnection() {
	try {
	    try {
		if (conn != null || !conn.isClosed()) {
		    return conn;
		}
	    } catch (Exception ex1) {
		conn = null;
	    }
	    if (dbuser == null) {
		dbuser = getDBParams();
	    }
	    if (dbuser == null) {
		throw new Exception("No database account selected");
	    }
	    String usr = dbuser.getUser();
	    String pw = dbuser.getPassword();
	    String url = dbuser.getURL();
	    String driver = dbuser.getDriverName();
	    Class.forName(driver);
	    DriverManager.setLoginTimeout(loginTimeout);
	    conn = DriverManager.getConnection(url, usr, pw);
	    int idx = url.indexOf('@');
	    status.setText("connected to "
		    + url.substring(idx > 0 ? idx + 1 : 0));
	    dbmd = conn.getMetaData();
	    dbinfoPanel.setMetaData(dbmd);
	    cPnl.setEnabledAt(2, true);
	    // System.err.println("dbmd = " + dbmd);
	    ResultSet rs = dbmd.getTypeInfo();
	    if (rs != null) {
		dbDataTypes.clear();
		while (rs.next()) {
		    DBTypeInfo dbti = new DBTypeInfo(rs);
		    dbDataTypes.add(dbti);
		}
	    }
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	    JOptionPane.showMessageDialog(frame, ex,
		    "Data base connection failed", JOptionPane.ERROR_MESSAGE);
	}
	return conn;
    }

    private DatabaseMetaData getDBMetaData() {
	if (dbmd == null) {
	    getConnection();
	}
	return dbmd;
    }

    private void setCatalogs() {
	try {
	    DatabaseMetaData dbmd = getDBMetaData();
	    ResultSet rs = dbmd.getCatalogs();
	    if (rs != null) {
		catalogModel.clear();
		while (rs.next()) {
		    String s = rs.getString(1);
		    catalogModel.addElement(s);
		}
	    }
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	}
    }

    private void setSchemas() {
	try {
	    DatabaseMetaData dbmd = getDBMetaData();
	    ResultSet rs = dbmd.getSchemas();
	    if (rs != null) {
		schemaModel.clear();
		while (rs.next()) {
		    String s = rs.getString(1);
		    schemaModel.addElement(s);
		}
	    }
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	}
    }

    private void setTables(Object[] catalog, Object[] schema) {
	tableModel.clear();
	if (catalog != null) {
	    for (int i = 0; i < catalog.length; i++) {
		if (schema != null) {
		    for (int j = 0; j < schema.length; j++) {
			setTables((String) catalog[i], (String) schema[j]);
		    }
		} else {
		    setTables((String) catalog[i], null);
		}
	    }
	}
	if (schema != null) {
	    for (int j = 0; j < schema.length; j++) {
		setTables(null, (String) schema[j]);
	    }
	} else {
	}
    }

    private void setTables(String catalog, String schema) {
	try {
	    DatabaseMetaData dbmd = getDBMetaData();
	    // ResultSet rs = dbmd.getTables(catalog,schema,null,null);
	    // if (rs != null) {
	    // while(rs.next()) {
	    // String s = rs.getString(3);
	    // tableModel.addElement(new
	    // DBTable(rs.getString(1),rs.getString(2),rs.getString(3)));
	    // }
	    // }
	    java.util.List tblList = DBTable.getDBTables(dbmd, catalog, schema,
		    null, null);
	    for (Iterator iter = tblList.listIterator(); iter.hasNext();) {
		tableModel.addElement(iter.next());
	    }
	    // splt.resetToPreferredSizes();
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	    if (ex instanceof SQLException) {
		for (SQLException sqlex = (SQLException) ex; sqlex != null; sqlex = sqlex
			.getNextException()) {
		    System.err.println(sqlex.toString());
		}
	    }
	    ex.printStackTrace();
	}
    }

    private void setColumns(Object[] tables) {
	Vector cols = new Vector();
	Vector rows = new Vector();
	for (int i = 0; i < tables.length; i++) {
	    DBTable tbl = (DBTable) tables[i];
	    setColumns(tbl, cols, rows);
	}
	setColumns(cols, rows);
    }

    private void setColumns(DBTable dbTable, Vector cols, Vector rows) {
	// System.err.println(">>> setColumns");
	try {
	    if (cols != null && cols.size() == 0) {
		// cols.add("Show");
		cols.add("NAME");
		cols.add("JDBCTYPE");
		cols.add("DBTYPE");
		cols.add("SIZE");
		cols.add("NULLABLE");
	    }
	    DBColumn[] tblCol = dbTable.getColumns();
	    for (int i = 0; i < tblCol.length; i++) {
		Vector row = new Vector(5);
		row.add(tblCol[i]);
		row.add(new Short(tblCol[i].getDataType()));
		row.add(tblCol[i].getTypeName());
		row.add(new Integer(tblCol[i].getColumnSize()));
		row.add(tblCol[i].getIsNullable());
		rows.add(row);
	    }
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	}
	// System.err.println("<<< setColumns");
    }

    private void setColumns(Vector cols, Vector rows) {
	cPnl.setEnabledAt(0, rows.size() > 0);
	cPnl.setEnabledAt(1, rows.size() > 0);
	if (rows.size() < 1) {
	    colModel.setRowCount(0);
	    return;
	}
	colListModel.removeAllElements();
	for (int i = 0; i < rows.size(); i++) {
	    colListModel.addElement(((Vector) rows.get(i)).get(0));
	}
	colModel.setDataVector(rows, cols);
	JComboBox sqlTypes = new JComboBox();
	for (Iterator i = sqlTypeName.keySet().iterator(); i.hasNext();) {
	    sqlTypes.addItem(i.next());
	}
	sqlTypes.setRenderer(new SQLTypeListRenderer());
	JComboBox colTypes = new JComboBox();
	for (Iterator i = dbDataTypes.iterator(); i.hasNext();) {
	    colTypes.addItem(i.next());
	}
	JComboBox nullTypes = new JComboBox();
	nullTypes.addItem("YES");
	nullTypes.addItem("NO");
	nullTypes.addItem("");
	TableColumn col;
	// col = colTable.getColumnModel().getColumn(0);
	// col.setCellRenderer(new ShowColumnRenderer());
	// col.setCellEditor(new ShowColumnEditor());
	col = colTable.getColumnModel().getColumn(1);
	col.setCellRenderer(new SQLTypeTableCellRenderer());
	col.setCellEditor(new DefaultCellEditor(sqlTypes));
	col = colTable.getColumnModel().getColumn(2);
	col.setCellEditor(new DefaultCellEditor(colTypes));
	col = colTable.getColumnModel().getColumn(4);
	col.setCellEditor(new DefaultCellEditor(nullTypes));
    }

    private void setPrimaryKeyInfo(Object[] tables) {
	Vector cols = new Vector();
	Vector rows = new Vector();
	// ResultTableModel pKeyModel
	if (tables != null && tables.length > 0) {
	    for (int i = 0; i < tables.length; i++) {
		DBTable tbl = (DBTable) tables[i];
		try {
		    if (i < 1) {
			pKeyModel.set(dbmd.getPrimaryKeys(tbl.getCatalogName(),
				tbl.getSchemaName(), tbl.getTableName()));
		    } else {
			pKeyModel.add(dbmd.getPrimaryKeys(tbl.getCatalogName(),
				tbl.getSchemaName(), tbl.getTableName()));
		    }
		} catch (Exception ex) {
		}
	    }
	} else {
	    Object[] cl = catalogList.getSelectedValues();
	    Object[] sl = schemaList.getSelectedValues();
	    int n = 0;
	    for (int i = 0; i < cl.length; i++) {
		try {
		    if (n < 1) {
			pKeyModel.set(dbmd.getPrimaryKeys((String) cl[i], null,
				null));
		    } else {
			pKeyModel.add(dbmd.getPrimaryKeys((String) cl[i], null,
				null));
		    }
		    n++;
		} catch (Exception ex) {
		}
	    }
	    for (int i = 0; i < sl.length; i++) {
		try {
		    if (n < 1) {
			pKeyModel.set(dbmd.getPrimaryKeys(null, (String) sl[i],
				null));
		    } else {
			pKeyModel.add(dbmd.getPrimaryKeys(null, (String) sl[i],
				null));
		    }
		    n++;
		} catch (Exception ex) {
		}
	    }
	}
    }

    private void setForeignKeyInfo(Object[] tables) {
	Vector cols = new Vector();
	Vector rows = new Vector();
	// ResultTableModel fKeyModel
	if (tables != null && tables.length > 0) {
	    for (int i = 0; i < tables.length; i++) {
		DBTable tbl = (DBTable) tables[i];
		try {
		    if (i < 1) {
			fKeyModel.set(dbmd.getImportedKeys(
				tbl.getCatalogName(), tbl.getSchemaName(), tbl
					.getTableName()));
		    } else {
			fKeyModel.add(dbmd.getImportedKeys(
				tbl.getCatalogName(), tbl.getSchemaName(), tbl
					.getTableName()));
		    }
		} catch (Exception ex) {
		}
	    }
	} else {
	    Object[] cl = catalogList.getSelectedValues();
	    Object[] sl = schemaList.getSelectedValues();
	    int n = 0;
	    for (int i = 0; i < cl.length; i++) {
		try {
		    if (n < 1) {
			fKeyModel.set(dbmd.getImportedKeys((String) cl[i],
				null, null));
		    } else {
			fKeyModel.add(dbmd.getImportedKeys((String) cl[i],
				null, null));
		    }
		    n++;
		} catch (Exception ex) {
		}
	    }
	    for (int i = 0; i < sl.length; i++) {
		try {
		    if (n < 1) {
			fKeyModel.set(dbmd.getImportedKeys(null,
				(String) sl[i], null));
		    } else {
			fKeyModel.add(dbmd.getImportedKeys(null,
				(String) sl[i], null));
		    }
		    n++;
		} catch (Exception ex) {
		}
	    }
	}
    }

    private void setExportedKeyInfo(Object[] tables) {
	Vector cols = new Vector();
	Vector rows = new Vector();
	// ResultTableModel xKeyModel
	if (tables != null && tables.length > 0) {
	    for (int i = 0; i < tables.length; i++) {
		DBTable tbl = (DBTable) tables[i];
		try {
		    if (i < 1) {
			xKeyModel.set(dbmd.getExportedKeys(
				tbl.getCatalogName(), tbl.getSchemaName(), tbl
					.getTableName()));
		    } else {
			xKeyModel.add(dbmd.getExportedKeys(
				tbl.getCatalogName(), tbl.getSchemaName(), tbl
					.getTableName()));
		    }
		} catch (Exception ex) {
		}
	    }
	} else {
	    Object[] cl = catalogList.getSelectedValues();
	    Object[] sl = schemaList.getSelectedValues();
	    int n = 0;
	    for (int i = 0; i < cl.length; i++) {
		try {
		    if (n < 1) {
			xKeyModel.set(dbmd.getExportedKeys((String) cl[i],
				null, null));
		    } else {
			xKeyModel.add(dbmd.getExportedKeys((String) cl[i],
				null, null));
		    }
		    n++;
		} catch (Exception ex) {
		}
	    }
	    for (int i = 0; i < sl.length; i++) {
		try {
		    if (n < 1) {
			xKeyModel.set(dbmd.getExportedKeys(null,
				(String) sl[i], null));
		    } else {
			xKeyModel.add(dbmd.getExportedKeys(null,
				(String) sl[i], null));
		    }
		    n++;
		} catch (Exception ex) {
		}
	    }
	}
    }

    private void setIndexInfo(Object[] tables) {
	Vector cols = new Vector();
	Vector rows = new Vector();
	for (int i = 0; i < tables.length; i++) {
	    DBTable tbl = (DBTable) tables[i];
	    setIndexInfo(tbl, cols, rows);
	}
	setDefaultModel(idxModel, cols, rows);
    }

    private void setIndexInfo(DBTable dbTable, Vector cols, Vector rows) {
	System.err.println(">>> setIndexInfo");
	try {
	    TableModel itm = AboutDB.getTableModel(dbmd.getIndexInfo(dbTable
		    .getCatalogName(), dbTable.getSchemaName(), dbTable
		    .getTableName(), false, true));
	    if (cols != null && cols.size() == 0) {
		cols.add("TABLE_CAT");
		cols.add("TABLE_SCHEM");
		cols.add("TABLE_NAME");
		cols.add("NON_UNIQUE");
		cols.add("INDEX_QUALIFIER");
		cols.add("INDEX_NAME");
		cols.add("TYPE");
		cols.add("ORDINAL_POSITION");
		cols.add("COLUMN_NAME");
		cols.add("ASC_OR_DESC");
		cols.add("CARDINALITY");
		cols.add("PAGES");
		cols.add("FILTER_CONDITION");
	    }
	    for (int r = 0; r < itm.getRowCount(); r++) {
		Vector row = new Vector(13);
		for (int c = 0; c < itm.getColumnCount(); c++) {
		    row.add(itm.getValueAt(r, c));
		}
		rows.add(row);
	    }
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	}
	System.err.println("<<< setIndexInfo");
    }

    private void setDefaultModel(DefaultTableModel model, Vector cols,
	    Vector rows) {
	if (rows == null || rows.size() < 1) {
	    model.setRowCount(0);
	    return;
	}
	model.setDataVector(rows, cols);
    }

    private void setRowCount() {
	int cnt = rowModel != null ? rowModel.getRowCount() : -1;
	// System.err.println("setRowCount" + cnt);
	rowLabel.setText("Rows" + (cnt < 0 ? "" : (" " + cnt)));
    }

    private void setRows(Object[] table, String columns) {
	if (table == null || table.length < 1) {
	    return;
	}
	String tables = "";
	for (int i = 0; i < table.length; i++) {
	    tables += (i > 0 ? ", " : "")
		    + (table[i] instanceof DBTable ? ((DBTable) table[i])
			    .getQualifiedName() : table[i].toString());
	}
	String sql = "select " + columns + " \nfrom " + tables;
	setQueryText(sql);
    }

    private void setRows(String catalog, String schema, String table,
	    String columns) {
	// System.err.println(">>> setRows");
	if (table == null || table.length() < 1)
	    return;
	String loc = catalog != null && catalog.length() > 0 ? catalog + "."
		: "";
	loc += schema != null && schema.length() > 0 ? schema + "." : "";
	String sql = "select " + columns + " \nfrom " + loc + table;
	setQueryText(sql);
	// System.err.println("<<< setRows");
    }

    private void setQueryStatus(String msg) {
	queryStatus.setText(msg);
    }

    private void setQueryText(String text) {
	boolean curPageEnabled = curPage.isEnabled();
	if (curPageEnabled) {
	    curPage.setEnabled(false);
	}
	DoSpinner.setValue(curPage, new Integer(1));
	queryText.setText(text);
	if (curPageEnabled) {
	    curPage.setEnabled(curPageEnabled);
	}
    }

    public String getQuery() {
	return queryText.getText().trim();
    }

    private void submitQuery() {
	String sql = getQuery();
	if (dbuser == null) {
	    setQueryStatus("Not connected to a database");
	    return;
	}
	if (sql == null || sql.length() < 1) {
	    setQueryStatus("No query entered");
	    return;
	}
	if (sql.endsWith(";")) {
	    sql = sql.substring(0, sql.lastIndexOf(";"));
	}
	// if (rowModel != null && sql.equals(rowModel.getQuery())) {
	// return;
	// }
	try {
	    // Save query history to user preferences
	    // dbmodel.addQuery(dbuser.getName(),"\n"+sql.trim()+"\n");
	} catch (Exception ex) {
	}
	rowLabel.setText("Rows  ?");
	try {
	    if (rowModel != null) {
		// rowModel.cancelQuery();
	    }
	    setQueryStatus("Executing query");
	    if (rowModel == null) {
		rowModel = new JDBCTableModel(dbuser);
		rowModel.addStatusListener(statusListener);
		rowModel.addTableModelListener(new TableModelListener() {
		    public void tableChanged(TableModelEvent e) {
			setRowCount();
		    }
		});
		rowTable.setTableModel(new VirtualTableModelProxy(rowModel));
	    }
	} catch (Exception ex) {
	    System.err.println("DBBrowser.submitQuery() " + ex);
	    ex.printStackTrace();
	}
	if (getRowLimit() < 0) {
	    rowModel.setQuery(sql);
	} else {
	    rowModel.setQuery(sql, getRowLimit(), getPage());
	}
	setRowCount();
	setTableSource(rowModel, (dbuser != null ? dbuser + " : " : "") + sql);
    }

    private DBUser getDBParams() {
	DBUser dbuser = null;
	Properties dbprops = dbmodel.getProperties(null, null);
	if (dbprops != null) {
	    dbuser = new DBUser(dbprops.getProperty("account"), dbprops
		    .getProperty("user"), dbprops.getProperty("password"),
		    dbprops.getProperty("url"), dbprops.getProperty("driver"));
	}
	return dbuser;
    }

    public void connectToDatabase() {
	DBUser newdbuser = getDBParams();
	try {
	    String usr = newdbuser.getUser();
	    String pw = newdbuser.getPassword();
	    String url = newdbuser.getURL();
	    String driver = newdbuser.getDriverName();
	    DBTestConnection.testConnection(usr, pw, url, driver);
	    if (conn != null) {
		if (!conn.isClosed()) {
		    conn.close();
		}
		conn = null;
	    }
	    dbuser = newdbuser;
	    dbmd = null;
	    catalogModel.clear();
	    catalogList.clearSelection();
	    schemaModel.clear();
	    schemaList.clearSelection();
	    tableModel.clear();
	    tableList.clearSelection();
	    colModel.setRowCount(0);
	    setQueryText("");
	    cPnl.setEnabledAt(2, false);
	    cPnl.setEnabledAt(1, false);
	    cPnl.setEnabledAt(0, false);
	    if (rowModel != null) {
		// rowModel.cancelQuery();
	    }
	    rowModel = new JDBCTableModel(dbuser);
	    rowModel.addStatusListener(statusListener);
	    rowModel.addTableModelListener(new TableModelListener() {
		public void tableChanged(TableModelEvent e) {
		    setRowCount();
		}
	    });
	    rowTable.setTableModel(new VirtualTableModelProxy(rowModel));
	    setRowCount();
	    status.setText("connected to " + url);
	    dbmd = getDBMetaData();
	    if (!dbmd.supportsCatalogsInDataManipulation()
		    && !dbmd.supportsSchemasInDataManipulation()) {
		tableModel.clear();
		setTables(null, usr);
	    } else {
		if (dbmd.supportsCatalogsInDataManipulation()) {
		    setCatalogs();
		}
		if (dbmd.supportsSchemasInDataManipulation()) {
		    setSchemas();
		}
	    }
	    splt.resetToPreferredSizes();
	    splts.resetToPreferredSizes();
	    spltc.resetToPreferredSizes();
	    if (!dbmd.supportsCatalogsInDataManipulation()) {
		splt.setDividerLocation(.0);
		splt.validate();
	    }
	    if (!dbmd.supportsSchemasInDataManipulation()) {
		splts.setDividerLocation(.0);
		splts.validate();
	    }
	    int i = schemaModel.indexOf(usr);
	    if (i > -1) {
		schemaList.setSelectedIndex(i);
	    }
	} catch (Exception ex) {
	    status.setText("DB connection failed " + ex);
	    JOptionPane.showMessageDialog(frame, ex,
		    "Data base connection failed", JOptionPane.ERROR_MESSAGE);
	    System.err.println("DB connection failed " + ex);
	}
    }

    public DBBrowser() {
	JLabel label;
	try {
	    dbmodel = new DBAccountListModel();
	    if (dbmodel.getSize() > 0) {
		dbmodel.setSelectedItem(dbmodel.getElementAt(0));
	    }
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(frame, ex,
		    "Unable to display Database Account Preferences",
		    JOptionPane.ERROR_MESSAGE);
	}

	JButton dbServices = new JButton("Edit Connections");
	dbServices.setToolTipText("Edit Database Account Preferences");
	dbServices.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    (new DatabaseAccountEditor(new DBAccountListModel()))
			    .show((Window) getTopLevelAncestor());
		} catch (Exception ex) {
		    JOptionPane.showMessageDialog(frame, ex,
			    "Unable to display Database Account Preferences",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	});

	dbChooser = new JComboBox(dbmodel);
	dbChooser.setToolTipText("Select a database account");
	JButton connBtn = new JButton("connect");
	connBtn
		.setToolTipText("Establish a connection to the selected database");
	connBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		connectToDatabase();
	    }
	});

	JPanel connectionPanel = new JPanel(new BorderLayout());

	JPanel connChoicePanel = new JPanel();
	JPanel connBtnPanel = new JPanel(new BorderLayout());
	connChoicePanel.setLayout(new BoxLayout(connChoicePanel,
		BoxLayout.X_AXIS));

	connChoicePanel.add(dbServices);
	connChoicePanel.add(dbChooser);
	connBtnPanel.add(connBtn, BorderLayout.WEST);
	status = new JTextField("Not connected to a database");
	status.setBackground(null);
	status.setToolTipText("Status of database connection");
	connBtnPanel.add(status);

	connectionPanel.add(connChoicePanel, BorderLayout.NORTH);
	connectionPanel.add(connBtnPanel, BorderLayout.SOUTH);

	// Catalogs
	catalogModel = new DefaultListModel();
	catalogList = new JList(catalogModel);
	catalogList.setVisibleRowCount(8);
	catalogList.setToolTipText("Select a database Catalog");
	gjsp = new JScrollPane(catalogList);

	catalogList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		setTables((Object[]) (catalogList.getSelectedValues()), null);
		colModel.setRowCount(0);
		setPrimaryKeyInfo(tableList.getSelectedValues());
		setForeignKeyInfo(tableList.getSelectedValues());
		setExportedKeyInfo(tableList.getSelectedValues());
	    }
	});

	// Schemas
	schemaModel = new DefaultListModel();
	schemaList = new JList(schemaModel);
	schemaList.setToolTipText("Select a database Schema");
	schemaList.setVisibleRowCount(8);
	sjsp = new JScrollPane(schemaList);

	schemaList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		setTables(null, (Object[]) (schemaList.getSelectedValues()));
		colModel.setRowCount(0);
		setPrimaryKeyInfo(tableList.getSelectedValues());
		setForeignKeyInfo(tableList.getSelectedValues());
		setExportedKeyInfo(tableList.getSelectedValues());
	    }
	});

	// Tables
	tableModel = new DefaultListModel();
	tableList = new JList(tableModel);
	tableList.setToolTipText("Select a database Table");
	tableList.setVisibleRowCount(8);
	tjsp = new JScrollPane(tableList);

	tableList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    setColumns(tableList.getSelectedValues());
		    setRows(tableList.getSelectedValues(), "*");
		    setPrimaryKeyInfo(tableList.getSelectedValues());
		    setForeignKeyInfo(tableList.getSelectedValues());
		    setExportedKeyInfo(tableList.getSelectedValues());
		    // setIndexInfo(tableList.getSelectedValues());
		}
	    }
	});

	// columns
	colModel = new DefaultTableModel(1, 1);
	colTable = new JTable(colModel);
	cjsp = new JScrollPane(colTable);

	colListModel = new DefaultListModel();
	colList = new JList(colListModel);
	colList.setToolTipText("Select Columns for query");
	JScrollPane cljsp = new JScrollPane(colList);

	colList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    setRows(tableList.getSelectedValues(), getRowColumns());
		}
	    }
	});

	// Primary Keys
	// dbmd.getPrimaryKeys(null,"PUB_SPRUCEDB",null);
	pKeyModel = new ResultTableModel();
	pKeyTable = new JTable(pKeyModel);
	pKeyjsp = new JScrollPane(pKeyTable);

	// Foreign Keys
	// dbmd.getImportedKeys(null,"PUB_SPRUCEDB",null);
	fKeyModel = new ResultTableModel();
	fKeyTable = new JTable(fKeyModel);
	fKeyjsp = new JScrollPane(fKeyTable);

	// Exported Foreign Keys
	// dbmd.getImportedKeys(null,"PUB_SPRUCEDB",null);
	xKeyModel = new ResultTableModel();
	xKeyTable = new JTable(xKeyModel);
	xKeyjsp = new JScrollPane(xKeyTable);

	// Index Info
	// dbmd.getIndexInfo(null,"bionobody","<table>",false,true)
	idxModel = new DefaultTableModel(1, 1);
	idxTable = new JTable(idxModel);
	idxjsp = new JScrollPane(idxTable);

	// rows
	rowTable = new JTableView();
	// rowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	// rjsp = new JScrollPane(rowTable);

	// query
	setPaging(true);
	usePaging.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		setPagingState(usePaging.isSelected());
	    }
	});

	submitBtn.setToolTipText("Start the query to view the table");
	submitBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		submitQuery();
		stopBtn.setEnabled(true);
	    }
	});

	stopBtn.setEnabled(false);
	stopBtn.setToolTipText("Stop the current query");
	stopBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (rowModel != null) {
		    rowModel.stopQuery();
		}
		if (!usePaging.isSelected()) {
		    usePaging.doClick();
		}
		if (rowModel != null) {
		    DoSpinner.setValue(pageSize, new Integer(rowModel
			    .getRowCount()));
		}
		((JComponent) e.getSource()).setEnabled(false);
		try {
		    // since this can change the preferredsize of the JTable...
		    rowTable.validate();
		} catch (Exception ex) {
		}
	    }
	});

	queryStatus = new JTextField();
	queryStatus.setBackground(null);
	queryStatus.setToolTipText("Status of database query");

	queryText = new JTextArea(5, 80);
	qjsp = new JScrollPane(queryText);

	// Initialize button to launch TableView
	tableViewBtn.setToolTipText("View with TableView");
	tableViewBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		launchTableView();
	    }
	});

	JPanel gPnl = new JPanel();
	gPnl.setLayout(new BoxLayout(gPnl, BoxLayout.Y_AXIS));
	label = new JLabel("Catalogs");
	label.setToolTipText("Select a database Catalog");
	gPnl.add(label);
	gPnl.add(gjsp);

	JPanel sPnl = new JPanel();
	sPnl.setLayout(new BoxLayout(sPnl, BoxLayout.Y_AXIS));
	label = new JLabel("Schemas");
	label.setToolTipText("Select a database Schema");
	sPnl.add(label);
	sPnl.add(sjsp);

	JPanel tPnl = new JPanel();
	tPnl.setLayout(new BoxLayout(tPnl, BoxLayout.Y_AXIS));
	label = new JLabel("Tables");
	label.setToolTipText("Select a database Table");
	tPnl.add(label);
	tPnl.add(tjsp);

	cPnl = new JTabbedPane();
	cPnl.addTab("Select Columns", null, cljsp,
		"Select which database Columns to query");
	cPnl.addTab("Column Info", null, cjsp, "Column Information");
	cPnl.addTab("Primary Key", null, pKeyjsp, "Declared Primary Key");
	cPnl.addTab("Imported Foreign Key", null, fKeyjsp,
		"Declared Imported Foreign Keys");
	cPnl.addTab("Exported Foreign Key", null, xKeyjsp,
		"Declared Exported Foreign Keys");
	// cPnl.addTab("Index Info", null, idxjsp, "Index Information");
	cPnl.addTab("Database Info", null, dbinfoPanel, "About this Database");
	cPnl.setEnabledAt(2, false);
	cPnl.setEnabledAt(1, false);
	cPnl.setEnabledAt(0, false);

	JPanel qPnl = new JPanel(new BorderLayout());
	JPanel sbPnl = new JPanel(new BorderLayout());

	Box sbBox = new Box(BoxLayout.X_AXIS);
	usePaging.setToolTipText("Enable paging through rows of the table.");
	usePaging.setHorizontalTextPosition(SwingConstants.LEADING);
	sbBox.add(usePaging);

	rowsLbl.setToolTipText("Set the number of rows per page.");
	pageSize.setToolTipText("Set the number of rows per page.");
	pgBox.add(rowsLbl);
	pgBox.add(pageSize);
	pageLbl.setToolTipText("Choose the page of table rows.");
	curPage.setToolTipText("Choose the page of table rows.");
	pgBox.add(pageLbl);
	pgBox.add(curPage);
	sbBox.add(pgBox);

	sbBox.add(submitBtn);
	sbBox.add(stopBtn);
	sbPnl.add(sbBox, BorderLayout.WEST);
	sbPnl.add(queryStatus);

	label = new JLabel("Query");
	label.setToolTipText("You may edit this query");
	label.setAlignmentX(Component.LEFT_ALIGNMENT);
	JButton qClearBtn = new JButton("Clear");
	qClearBtn.setToolTipText("Clear the query window");
	qClearBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		queryText.setText("");
	    }
	});

	JPanel qlPnl = new JPanel(new BorderLayout());
	qlPnl.add(label, BorderLayout.WEST);
	Box qhBox = new Box(BoxLayout.X_AXIS);
	qhBox.add(qClearBtn);
	qhBox.setAlignmentX(Component.LEFT_ALIGNMENT);
	qlPnl.add(qhBox, BorderLayout.EAST);

	qPnl.add(qlPnl, BorderLayout.NORTH);
	qPnl.add(sbPnl, BorderLayout.SOUTH);
	qPnl.add(qjsp);

	JPanel rPnl = new JPanel();
	rPnl.setLayout(new BoxLayout(rPnl, BoxLayout.Y_AXIS));
	rPnl.add(rowLabel);
	// rPnl.add(rjsp);
	rPnl.add(rowTable);

	spltc = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tPnl, cPnl);
	spltc.setOneTouchExpandable(true);

	splts = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sPnl, spltc);
	splts.setOneTouchExpandable(true);

	splt = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gPnl, splts);
	splt.setOneTouchExpandable(true);

	spltq = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splt, qPnl);
	spltq.setOneTouchExpandable(true);

	spltr = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spltq, rPnl);
	spltr.setOneTouchExpandable(true);

	setLayout(new BorderLayout());
	// status = new JTextField();
	add(connectionPanel, BorderLayout.NORTH);
	add(spltr, BorderLayout.CENTER);
	// add(status,BorderLayout.SOUTH);
	spltr.setDividerLocation(.7);
	spltq.setDividerLocation(.7);
	spltr.setResizeWeight(.5);
	spltq.setResizeWeight(.5);

	((JSpinner) curPage).addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		if (usePaging.isSelected()) {
		    submitQuery();
		}
	    }
	});
    }

    public void setPaging(boolean enable) {
	usePaging.getModel().setSelected(enable);
	setPagingState(enable);
    }

    private void setPagingState(boolean enable) {
	pageSize.setEnabled(enable);
	curPage.setEnabled(enable);
	pgBox.setForeground(enable ? Color.black : Color.gray);
	rowsLbl.setForeground(enable ? Color.black : Color.gray);
	pageLbl.setForeground(enable ? Color.black : Color.gray);
    }

    public TableModel getTableModel() {
	TableModel tableModel = rowModel;
	rowModel = null;
	return tableModel;
    }

    private int getRowLimit() {
	if (usePaging.isSelected()) {
	    return ((Number) DoSpinner.getValue(pageSize)).intValue();
	}
	return -1;
    }

    private int getPage() {
	if (usePaging.isSelected()) {
	    return ((Number) DoSpinner.getValue(curPage)).intValue();
	}
	return 0;
    }

    private void parseArgs(String args[]) {
	String dbname = null;
	for (int i = 0; i < args.length; i++) {
	    if (args[i].startsWith("-")) {
		if (args[i].equals("-preferences")) {
		    String source = args[++i];
		    try {
			dbmodel.importPreferences(source);
		    } catch (Exception ex) {
			System.err.println("Unable to set preferences from "
				+ source + "  " + ex);
		    }
		} else if (args[i].equals("-dbname")) {
		    dbname = args[++i];
		}
	    }
	}
	if (dbname != null) {
	    setDatabase(dbname);
	}
    }

    public void launchTableView() {
	if (tableView == null) {
	    tableView = new TableView();
	    JFrame frame = new JFrame("TableView");
	    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	    frame.getContentPane().add(tableView, BorderLayout.CENTER);
	    frame.setLocationRelativeTo(this);
	    frame.pack();
	} else if (!tableView.getTopLevelAncestor().isDisplayable()) {
	    // ((JFrame)tableView.getTopLevelAncestor()).pack();
	}
	TableModel tm = rowModel;
	if (tm != null && !tableView.getTableContext().hasTableModel(tm)) {
	    tableView.setTableModel(tm, getTableSource());
	}
	if (!tableView.getTopLevelAncestor().isVisible()) {
	    tableView.getTopLevelAncestor().setVisible(true);
	}
	((Window) tableView.getTopLevelAncestor()).toFront();
    }

    public void enableTableViewLaunch(boolean b) {
	if (b) {
	    rowTable.getToolbar().add(tableViewBtn);
	} else {
	    rowTable.getToolbar().remove(tableViewBtn);
	}
    }

    public void setDatabase(String dbname) {
	if (dbname != null) {
	    for (int i = 0; i < dbmodel.getSize(); i++) {
		Object o = dbmodel.getElementAt(i);
		if (o != null && dbname.equals(o.toString())) {
		    dbChooser.setSelectedIndex(i);
		    connectToDatabase();
		    break;
		}
	    }
	}
    }

    public static void main(String args[]) {
	DBBrowser dbp = new DBBrowser();
	dbp.enableTableViewLaunch(true);
	dbp.parseArgs(args);
	JFrame frame = new JFrame("Browse Database Tables");
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }

	    public void windowClosed(WindowEvent e) {
		System.exit(0);
	    }
	});
	frame.getContentPane().add(dbp);
	JButton closeBtn = new JButton("Close");
	closeBtn.setToolTipText("Close this window");
	closeBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    ((Window) ((JComponent) e.getSource())
			    .getTopLevelAncestor()).dispose();
		} catch (Exception ex) {
		}
	    }
	});
	JToolBar tb = new JToolBar();
	tb.add(closeBtn);
	frame.getContentPane().add(tb, BorderLayout.NORTH);

	frame.pack();
	Dimension dim = frame.getSize();
	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	dim.width = dim.width < screen.width - 50 ? dim.width
		: screen.width - 50;
	dim.height = dim.height < screen.height - 50 ? dim.height
		: screen.height - 50;
	frame.setSize(dim);
	frame.setVisible(true);
    }

    private String getRowColumns() {
	if (colList != null) {
	    Object[] selCols = colList.getSelectedValues();
	    if (selCols != null && selCols.length > 0) {
		String cols = "";
		for (int i = 0, n = 0; i < selCols.length; i++) {
		    if (selCols[i] != null) {
			cols += (n++ > 0 ? ", " : "")
				+ (selCols[i] instanceof DBColumn ? ((DBColumn) selCols[i])
					.getQualifiedName()
					: selCols[i].toString());
		    }
		}
		if (cols.length() > 0) {
		    return cols;
		}
	    } else {
		// System.err.println("getRowColumns " + selCols);
	    }
	}
	return "*";
    }

    class ShowColumnEditor extends DefaultCellEditor {
	ShowColumnEditor() {
	    super(new JCheckBox());
	}

	public boolean stopCellEditing() {
	    boolean done = super.stopCellEditing();
	    setRows(tableList.getSelectedValues(), getRowColumns());
	    return done;
	}
    }

    class ShowColumnRenderer extends JCheckBox implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {
	    try {
		setSelected(((Boolean) value).booleanValue());
	    } catch (Exception ex) {
	    }
	    return this;
	}

	public void validate() {
	}

	public void revalidate() {
	}

	public void repaint(long tm, int x, int y, int width, int height) {
	}

	public void repaint(Rectangle r) {
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
		Object newValue) {
	}

	public void firePropertyChange(String propertyName, boolean oldValue,
		boolean newValue) {
	}
    }
}
