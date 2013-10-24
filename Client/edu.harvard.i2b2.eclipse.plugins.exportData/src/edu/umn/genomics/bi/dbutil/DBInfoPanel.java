/*
 * @(#) $RCSfile: DBInfoPanel.java,v $ $Revision: 1.4 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Displays information about a database using DatabaseMetaData returned from a
 * connection.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBInfoPanel extends JPanel {
    JTabbedPane tabP;

    /**
     * Construct a display panel for database metadata information.
     */
    public DBInfoPanel() {
	tabP = new JTabbedPane();
	setLayout(new BorderLayout());
	add(tabP);
    }

    /**
     * Construct a display panel for database metadata information.
     * 
     * @param dbmd
     *            the metadata source for the display.
     */
    public DBInfoPanel(DatabaseMetaData dbmd) {
	this();
	setMetaData(dbmd);
    }

    /**
     * Display database information from this database metadata source.
     * 
     * @param dbmd
     *            the metadata source for the display.
     */
    public void setMetaData(DatabaseMetaData dbmd) {
	tabP.removeAll();
	if (dbmd == null)
	    return;
	JTable jt;

	jt = new JTable(AboutDB.getVersions(dbmd));
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tabP.addTab("Versions", null, new JScrollPane(jt),
		"Database and driver versions");

	jt = new JTable(AboutDB.getCapabilities(dbmd));
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tabP.addTab("Capabilities", null, new JScrollPane(jt),
		"Database Capabilities Supported");

	jt = new JTable(AboutDB.getLimits(dbmd));
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tabP.addTab("Limits", null, new JScrollPane(jt), "Database Limits");

	jt = new JTable(AboutDB.getStringValues(dbmd));
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tabP.addTab("Values", null, new JScrollPane(jt), "Database Terms");

	jt = new JTable(AboutDB.getSQLKeywords(dbmd));
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tabP.addTab("SQL Keywords", null, new JScrollPane(jt),
		"Database SQLKeywords");

	jt = new JTable(AboutDB.getFunctions(dbmd));
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	tabP.addTab("Functions", null, new JScrollPane(jt),
		"Database Functions");

	try {
	    TableModel tm = AboutDB.getTableModel(dbmd.getTypeInfo());
	    if (tm instanceof ResultTableModel) {
		((ResultTableModel) tm).allowEditing(false);
	    }
	    jt = new JTable(tm);
	    jt.getColumnModel().getColumn(1).setCellRenderer(
		    new SQLTypeTableCellRenderer());
	    jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tabP.addTab("SQL Types", null, new JScrollPane(jt),
		    "Database Supported SQL Type Information");
	} catch (Exception ex) {
	}
    }
}
