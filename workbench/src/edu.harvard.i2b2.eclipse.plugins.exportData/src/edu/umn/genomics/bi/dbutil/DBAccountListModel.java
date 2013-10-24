/*
 * @(#) $RCSfile: DBAccountListModel.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 14:23:48 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;
import java.util.prefs.*;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import javax.swing.*;
import edu.umn.genomics.file.OpenInputSource;

/**
 * Manages a user's JDBC Database account preferences and provides a
 * ComboBoxModel of the list of accounts in the user's preferences.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 14:23:48 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.ComboBoxModel
 * @see javax.swing.AbstractListModel
 * @see java.util.prefs.Preferences
 * @see java.sql.DriverManager
 */
public class DBAccountListModel extends AbstractListModel implements
	ComboBoxModel {
    // Key names for the account properties
    private static final String USER = "user";
    private static final String PASSWD = "passwd";
    private static final String URL = "url";
    private static final String DRIVER = "driver";
    private static final String QUERY_HISTORY = "QueryHistory";
    // A list of account names
    Vector acctList = new Vector();
    // The account selected for the ComboBox
    Object selectedItem = null;
    // The preference node that is the parent node for all the individual
    // account names
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    // Preference Listeners
    PreferenceChangeListener pcl = new PreferenceChangeListener() {
	public void preferenceChange(PreferenceChangeEvent evt) {
	    Preferences pref = evt.getNode();
	    update(pref);
	}
    };
    NodeChangeListener ncl = new NodeChangeListener() {
	public void childAdded(NodeChangeEvent evt) {
	    Preferences pref = evt.getChild();
	    pref.addPreferenceChangeListener(pcl);
	    add(pref);
	}

	public void childRemoved(NodeChangeEvent evt) {
	    Preferences pref = evt.getChild();
	    remove(pref);
	}
    };

    /**
     * Construct a DBAccountListModel of the user's database account
     * preferences.
     * 
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public DBAccountListModel() throws BackingStoreException {
	try {
	    Class prefclass = this.getClass();
	    prefs = Preferences.userNodeForPackage(prefclass);
	} catch (Exception ex) {
	    System.err.println("prefclass " + ex);
	}
	prefs.addNodeChangeListener(ncl);
	String[] accnts = getAccountNames();
	Arrays.sort(accnts);
	for (int i = 0; i < accnts.length; i++) {
	    Preferences pref = prefs.node(accnts[i]);
	    pref.addPreferenceChangeListener(pcl);
	}
	acctList.addAll(Arrays.asList(accnts));
    }

    /**
     * Return the number of accounts in the list.
     * 
     * @return the number of accounts in the list.
     */
    public int getSize() {
	return acctList.size();
    }

    /**
     * Returns the account name at the specified index int the list.
     * 
     * @param index
     *            the index of account name in the list.
     * @return the account name at the specified index.
     */
    public Object getElementAt(int index) {
	return acctList.get(index);
    }

    /**
     * Set the selected accountName. The selected accountName may be null.
     * 
     * @param accountName
     *            the account name of the selected database account.
     */
    public void setSelectedItem(Object accountName) {
	selectedItem = accountName;
    }

    /**
     * Return the name of the selected database account.
     * 
     * @return the name of the selected database account.
     */
    public Object getSelectedItem() {
	return acctList.contains(selectedItem) ? selectedItem : null;
    }

    /**
     * Return an array of all database account names.
     * 
     * @return an array of all database account names.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String[] getAccountNames() throws BackingStoreException {
	return prefs.childrenNames();
    }

    /**
     * Add a database account preference node to the list, and notify listeners
     * of the change.
     * 
     * @param pref
     *            the database account preference node.
     */
    private void add(Preferences pref) {
	String acct = pref.name();
	int idx = acctList.indexOf(acct);
	if (idx < 0) {
	    for (ListIterator iter = acctList.listIterator(); iter.hasNext();) {
		String item = (String) iter.next();
		if (acct.compareToIgnoreCase(item) < 0) {
		    idx = iter.previousIndex();
		    break;
		}
	    }
	    if (idx < 0) {
		idx = acctList.size();
		acctList.add(acct);
	    } else {
		acctList.add(idx, acct);
	    }
	    if (acctList.size() == 1 && selectedItem == null && acct != null) {
		setSelectedItem(acct);
	    }
	    fireIntervalAdded(this, idx, idx);
	} else {
	    update(pref);
	}
    }

    /**
     * Remove a database account preference node from the list, and notify
     * listeners of the change.
     * 
     * @param pref
     *            the database account preference node to remove.
     */
    private void remove(Preferences pref) {
	String acct = pref.name();
	int idx = acctList.indexOf(acct);
	if (acct != null && acct.equals(selectedItem)) {
	    if (idx > 0) {
		setSelectedItem(getElementAt(idx - 1));
	    } else {
		setSelectedItem(getSize() == 1 ? null : getElementAt(idx + 1));
	    }
	}
	if (idx >= 0) {
	    acctList.remove(idx);
	    fireIntervalRemoved(this, idx, idx);
	}
    }

    /**
     * Notify listeners that a database account preference node has changed.
     * 
     * @param pref
     *            the database account preference node that changed.
     */
    private void update(Preferences pref) {
	String acct = pref.name();
	int idx = acctList.indexOf(acct);
	if (idx >= 0) {
	    fireContentsChanged(this, idx, idx);
	}
    }

    /**
     * Get the value of a preference node property.
     * 
     * @param accountName
     *            the name of the account from which to retrieve the property.
     * @param key
     *            the name of the property.
     * @param def
     *            the default value for the property.
     * @return the value of the property.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    private String getValue(String accountName, String key, String def)
	    throws BackingStoreException {
	if (prefs.nodeExists(accountName)) {
	    return prefs.node(accountName).get(key, def);
	}
	return def;
    }

    /**
     * Set the value of a preference node property.
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param key
     *            the name of the property.
     * @param val
     *            the value for the property.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    private void setValue(String accountName, String key, String val)
	    throws BackingStoreException {
	if (prefs.nodeExists(accountName)) {
	    prefs.node(accountName).put(key, val);
	    prefs.sync();
	}
    }

    /**
     * Get Database Connection Parameters as values stored in a Properties
     * instance. This will return the following properties: "account" "url"
     * "driver" "user" "password"
     * 
     * @param properties
     *            if not null, properties will be set in this instance.
     * @param accountName
     *            the account name for which to set properties, if null,
     *            properties are set for the selected item, and if no item is
     *            selected properties are set for the first item.
     * @return the Connection properties.
     */
    public Properties getProperties(Properties properties, String accountName) {
	Properties prop = properties != null ? properties : new Properties();
	String acctName = accountName != null ? accountName
		: (String) getSelectedItem();
	if (acctName == null && getSize() > 0) {
	    acctName = (String) getElementAt(0);
	}
	if (acctName != null) {
	    try {
		prop.setProperty("account", acctName);
		prop.setProperty("user", getUser(acctName));
		prop.setProperty("password", getPassword(acctName));
		prop.setProperty("url", getURL(acctName));
		prop.setProperty("driver", getDriver(acctName));
	    } catch (BackingStoreException bsex) {
		System.err.println(this.getClass() + " getProperties: " + bsex);
		return null;
	    }
	}
	return prop;
    }

    /**
     * Get the database server URL for the specified database account.
     * 
     * @param accountName
     *            the name of the account from which to retrieve the property.
     * @return the database server URL for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String getURL(String accountName) throws BackingStoreException {
	return getValue(accountName, URL, "");
    }

    /**
     * Get the database user name for the specified database account.
     * 
     * @param accountName
     *            the name of the account from which to retrieve the property.
     * @return the database user name for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String getUser(String accountName) throws BackingStoreException {
	return getValue(accountName, USER, System.getProperty("user.name"));
    }

    /**
     * Get the database user password for the specified database account.
     * 
     * @param accountName
     *            the name of the account from which to retrieve the property.
     * @return the database user password for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String getPassword(String accountName) throws BackingStoreException {
	return getValue(accountName, PASSWD, "");
    }

    /**
     * Get the JDBC driver class name for the specified database account.
     * 
     * @param accountName
     *            the name of the account from which to retrieve the property.
     * @return the JDBC driver class name for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String getDriver(String accountName) throws BackingStoreException {
	return getValue(accountName, DRIVER, "");
    }

    /**
     * Set the database server URL for the specified database account.
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param url
     *            the database server URL for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void setURL(String accountName, String url)
	    throws BackingStoreException {
	setValue(accountName, URL, url);
    }

    /**
     * Set the database user name for the specified database account.
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param user
     *            the database user name for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void setUser(String accountName, String user)
	    throws BackingStoreException {
	setValue(accountName, USER, user);
    }

    /**
     * Set the database user password for the specified database account.
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param password
     *            the database user password for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void setPassword(String accountName, String password)
	    throws BackingStoreException {
	setValue(accountName, PASSWD, password);
    }

    /**
     * Set the JDBC driver class name for the specified database account.
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param driver
     *            the JDBC driver class name for the specified database account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void setDriver(String accountName, String driver)
	    throws BackingStoreException {
	setValue(accountName, DRIVER, driver);
    }

    /**
     * Add a database account preference.
     * 
     * @param accountName
     *            the name of the account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     * @see #setURL
     * @see #setUser
     * @see #setPassword
     * @see #setDriver
     */
    public void addAccount(String accountName) throws BackingStoreException {
	prefs.node(accountName);
	prefs.sync();
    }

    /**
     * Remove a database account preference.
     * 
     * @param accountName
     *            the name of the account.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void removeAccount(String accountName) throws BackingStoreException {
	prefs.node(accountName).removeNode();
	prefs.sync();
    }

    /**
     * Establish a Connection to the given database account.
     * 
     * @param accountName
     *            the name of the account.
     * @return a connection to the database account.
     * @throws NullPointerException
     *             if the given accountName is null
     * @throws SQLException
     *             if there was an error connecting to the database
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public Connection getConnection(String accountName)
	    throws NullPointerException, SQLException, BackingStoreException {
	if (accountName == null) {
	    throw new NullPointerException("accountName can't be null");
	} else if (prefs.nodeExists(accountName)) {
	    try {
		Class.forName(getDriver(accountName));
	    } catch (ClassNotFoundException ex) {
	    }
	    return DriverManager.getConnection(getURL(accountName),
		    getUser(accountName), getPassword(accountName));
	}
	return null;
    }

    /**
     * Return an array of all the unique database server URLs in the user's
     * preferences.
     * 
     * @return an array of all the unique database server URLs.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String[] getKnownURLs() throws BackingStoreException {
	TreeSet list = new TreeSet();
	String[] accnts = getAccountNames();
	for (int i = 0; i < accnts.length; i++) {
	    list.add(getURL(accnts[i]));
	}
	return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Return an array of all the unique JDBC driver class names that occur in
     * the user's preferences or that are listed in the jdbc.drivers property.
     * 
     * @return an array of all the unique database server URLs.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     * @see java.sql.DriverManager
     */
    public String[] getKnownDrivers() throws BackingStoreException {
	TreeSet list = new TreeSet();
	String[] accnts = getAccountNames();
	for (int i = 0; i < accnts.length; i++) {
	    list.add(getDriver(accnts[i]));
	}
	String drivers = System.getProperty("jdbc.drivers");
	if (drivers != null) {
	    for (StringTokenizer st = new StringTokenizer(drivers, ":"); st
		    .hasMoreTokens();) {
		list.add(st.nextToken());
	    }
	}
	return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Return an array of all the unique database user names in the user's
     * preferences.
     * 
     * @return an array of all the unique database user names.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String[] getKnownUsers() throws BackingStoreException {
	TreeSet list = new TreeSet();
	String[] accnts = getAccountNames();
	for (int i = 0; i < accnts.length; i++) {
	    list.add(getUser(accnts[i]));
	}
	return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param query
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void addQuery(String accountName, String query)
	    throws BackingStoreException {
	if (accountName != null && query != null) {
	    if (prefs.nodeExists(accountName)) {
		Preferences pref = prefs.node(accountName).node(QUERY_HISTORY);
		pref.put(query, (new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss"))
			.format(new Date()).toString());
	    }
	}
    }

    /**
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @param query
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void removeQuery(String accountName, String query)
	    throws BackingStoreException {
	if (accountName != null && query != null) {
	    if (prefs.nodeExists(accountName)) {
		Preferences pref = prefs.node(accountName).node(QUERY_HISTORY);
		pref.remove(query);
	    }
	}
    }

    /**
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public void clearQueries(String accountName) throws BackingStoreException {
	if (accountName != null) {
	    if (prefs.nodeExists(accountName)) {
		Preferences pref = prefs.node(accountName).node(QUERY_HISTORY);
		pref.clear();
	    }
	}
    }

    /**
     * 
     * @param accountName
     *            the name of the account for which to set the property.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     */
    public String[] getQueries(String accountName) throws BackingStoreException {
	if (accountName != null) {
	    if (prefs.nodeExists(accountName)) {
		Preferences pref = prefs.node(accountName).node(QUERY_HISTORY);
		return pref.keys();
	    }
	}
	return new String[0];
    }

    /**
     * Import Database account connection parameters from the preferences
     * source.
     * 
     * @param source
     *            the URL or pathname to a preferences file.
     * @throws NullPointerException
     *             if source is null.
     * @throws SecurityException
     *             if a security manager is present and it denies
     *             RuntimePermission("preferences").
     * @throws IOException
     *             if reading from the specified input stream results in an
     *             IOException.
     * @throws InvalidPreferencesFormatException
     *             Data on input stream does not constitute a valid XML document
     *             with the mandated document type.
     */
    public void importPreferences(String source) throws NullPointerException,
	    SecurityException, IOException, InvalidPreferencesFormatException {
	InputStream is = OpenInputSource.getInputStream(source);
	importPreferences(is);
    }

    /**
     * Import Database account connection parameters from the preferences
     * source.
     * 
     * @param is
     *            an open input stream to a preferences source.
     * @throws NullPointerException
     *             if is is null.
     * @throws SecurityException
     *             if a security manager is present and it denies
     *             RuntimePermission("preferences").
     * @throws IOException
     *             if reading from the specified input stream results in an
     *             IOException.
     * @throws InvalidPreferencesFormatException
     *             Data on input stream does not constitute a valid XML document
     *             with the mandated document type.
     */
    public void importPreferences(InputStream is) throws NullPointerException,
	    SecurityException, IOException, InvalidPreferencesFormatException {
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
     * @throws NullPointerException
     * @throws SecurityException
     *             if a security manager is present and it denies
     *             RuntimePermission("preferences").
     * @throws IOException
     *             if writing to the specified output stream results in an
     *             IOException.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     * @throws IllegalStateException
     *             if this node (or an ancestor) has been removed.
     */
    public void exportPreferences(String filename, String name)
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
     * @throws NullPointerException
     * @throws SecurityException
     *             if a security manager is present and it denies
     *             RuntimePermission("preferences").
     * @throws IOException
     *             if writing to the specified output stream results in an
     *             IOException.
     * @throws BackingStoreException
     *             if there was an error retrieving preference information
     * @throws IllegalStateException
     *             if this node (or an ancestor) has been removed.
     */
    public void exportPreferences(OutputStream os, String name)
	    throws NullPointerException, SecurityException, IOException,
	    BackingStoreException, IllegalStateException {
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
