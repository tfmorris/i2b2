/*
 * @(#) $RCSfile: DBPreferences.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.prefs.*;
import java.io.*;
import edu.umn.genomics.file.OpenInputSource;

/**
 * Maintain database account preference data.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see java.util.prefs.Preferences
 */
public class DBPreferences {
    private static final String NAME = "name";
    private static final String USER = "user";
    private static final String PASSWD = "passwd";
    private static final String URL = "url";
    private static final String DRIVER = "driver";

    /**
     * Return Database account preferences.
     * 
     * @return an array of Database account connection parameters.
     */
    public static DBConnectParams[] getDBAccounts() {
	DBConnectParams[] dbpa = null;
	try {
	    Preferences prefs = Preferences
		    .userNodeForPackage(edu.umn.genomics.bi.dbutil.DBPreferences.class);
	    String db[] = prefs.childrenNames();
	    dbpa = new DBConnectParams[db.length];
	    for (int i = 0; i < db.length; i++) {
		Preferences pref = prefs.node(db[i]);
		String name = pref.get(NAME, "");
		String user = pref.get(USER, System.getProperty("user.name"));
		String passwd = pref.get(PASSWD, "");
		String url = pref.get(URL,
			"jdbc:oracle:thin:@localhost:1521:ORA");
		String driver = pref.get(DRIVER,
			"oracle.jdbc.driver.OracleDriver");
		dbpa[i] = new DBUser(name, user, passwd, url, driver);
	    }
	} catch (Exception ex) {
	    System.err.println(" DBPreferences " + ex);
	}
	return dbpa;
    }

    public static DBConnectParams getDatabaseAccount(String accountName) {
	Preferences prefs = Preferences
		.userNodeForPackage(edu.umn.genomics.bi.dbutil.DBPreferences.class);
	try {
	    if (prefs.nodeExists(accountName)) {
		Preferences pref = prefs.node(accountName);
		String name = pref.get(NAME, "");
		String user = pref.get(USER, System.getProperty("user.name"));
		String passwd = pref.get(PASSWD, "");
		String url = pref.get(URL, "");
		String driver = pref.get(DRIVER, "");
		return new DBUser(name, user, passwd, url, driver);
	    }
	} catch (Exception ex) {
	    System.err.println(" DBPreferences " + ex);
	}
	return null;
    }

    /**
     * Save the Database account connection parameters in the user preferences.
     * 
     * @param dbconnections
     *            an array of Database account connection parameters.
     */
    public static void saveDBAccounts(DBConnectParams[] dbconnections) {
	for (int i = 0; i < dbconnections.length; i++) {
	    saveDBAccount(dbconnections[i]);
	}
    }

    /**
     * Save the Database account connection parameters in the user preferences.
     * 
     * @param dbconnection
     *            Database account connection parameters.
     */
    public static void saveDBAccount(DBConnectParams dbconnection) {
	Preferences prefs = Preferences
		.userNodeForPackage(edu.umn.genomics.bi.dbutil.DBPreferences.class);
	Preferences pref = prefs.node(dbconnection.getName());
	pref.put(NAME, dbconnection.getName());
	pref.put(USER, dbconnection.getUser());
	pref.put(PASSWD, dbconnection.getPassword());
	pref.put(URL, dbconnection.getURL());
	pref.put(DRIVER, dbconnection.getDriverName());
    }

    /**
     * Delete the Database account connection parameters from the user
     * preferences.
     * 
     * @param dbconnection
     *            Database account connection parameters.
     */
    public static void deleteDBAccount(DBConnectParams dbconnection) {
	deleteDBAccount(dbconnection.getName());
    }

    /**
     * Delete the Database account connection parameters from the user
     * preferences.
     * 
     * @param name
     *            the name of a Database account.
     */
    public static void deleteDBAccount(String name) {
	try {
	    Preferences prefs = Preferences
		    .userNodeForPackage(edu.umn.genomics.bi.dbutil.DBPreferences.class);
	    if (prefs.nodeExists(name)) {
		prefs.node(name).removeNode();
	    }
	} catch (Exception ex) {
	    System.err.println(" DBPreferences " + ex);
	}
    }

    /**
     * Import Database account connection parameters from the preferences
     * source.
     * 
     * @param source
     *            the URL or pathname to a preferences file.
     */
    public static void importPreferences(String source)
	    throws NullPointerException, SecurityException, IOException,
	    InvalidPreferencesFormatException {
	InputStream is = OpenInputSource.getInputStream(source);
	importPreferences(is);
    }

    /**
     * Import Database account connection parameters from the preferences
     * source.
     * 
     * @param is
     *            an open input stream to a preferences source.
     */
    public static void importPreferences(InputStream is)
	    throws NullPointerException, SecurityException, IOException,
	    InvalidPreferencesFormatException {
	Preferences prefs = Preferences
		.userNodeForPackage(edu.umn.genomics.bi.dbutil.DBPreferences.class);
	Preferences.importPreferences(is);
    }

    /**
     * Export Database account connection parameters to the given filename.
     * 
     * @param filename
     *            a preferences file.
     * @param name
     *            If not null, only export the connection parameters for this
     *            account, otherwise export all account parameters.
     */
    public static void exportPreferences(String filename, String name)
	    throws NullPointerException, SecurityException, IOException,
	    BackingStoreException, IllegalStateException {
	exportPreferences(new FileOutputStream(filename), name);
    }

    /**
     * Export Database account connection parameters to the given filename.
     * 
     * @param os
     *            an open output stream to a preferences file.
     * @param name
     *            If not null, only export the connection parameters for this
     *            account, otherwise export all account parameters.
     */
    public static void exportPreferences(OutputStream os, String name)
	    throws NullPointerException, SecurityException, IOException,
	    BackingStoreException, IllegalStateException {
	Preferences prefs = Preferences
		.userNodeForPackage(edu.umn.genomics.bi.dbutil.DBPreferences.class);
	if (name != null) {
	    if (prefs.nodeExists(name)) {
		Preferences pref = prefs.node(name);
		pref.exportSubtree(os);
	    }
	} else {
	    prefs.exportSubtree(os);
	}
    }
}
