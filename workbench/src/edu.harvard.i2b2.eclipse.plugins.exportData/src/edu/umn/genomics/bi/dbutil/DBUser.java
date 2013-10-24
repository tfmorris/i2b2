/*
 * @(#) $RCSfile: DBUser.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;

/**
 * Holds the parameters required for making a JDBC connection to a database.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBUser implements DBConnectParams, Serializable {
    String name;
    String user;
    String passwd;
    String url;
    String driverName;

    public DBUser(String user, String password, String url, String driverName) {
	this.user = user != null ? user.trim() : "";
	this.passwd = password != null ? password.trim() : "";
	this.url = url != null ? url.trim() : "";
	this.driverName = driverName;
	String loc = url == null ? "" : url.indexOf("@") >= 0 ? url
		.substring(url.indexOf("@") + 1) : url;
	this.name = loc.length() > 0 ? user + "@" + loc : "";
    }

    public DBUser(String name, String user, String password, String url,
	    String driverName) {
	this(user, password, url, driverName);
	if (name != null && name.length() > 0)
	    this.name = name;
    }

    public DBUser(DBConnectParams params) {
	this.name = params.getName();
	this.user = params.getUser();
	this.passwd = params.getPassword();
	this.url = params.getURL();
	this.driverName = params.getDriverName();
    }

    public String getName() {
	return name;
    }

    public String getUser() {
	return user;
    }

    public String getPassword() {
	return passwd;
    }

    public String getURL() {
	return url;
    }

    public String getDriverName() {
	return driverName;
    }

    void setUser(String val) {
	this.user = user;
    }

    void setPassword(String passwd) {
	this.passwd = passwd;
    }

    void setURL(String url) {
	this.url = url;
    }

    void setDriverName(String driverName) {
	this.driverName = driverName;
    }

    /**
     * Returns whether this DBConnectParams represents the same database user
     * account as the given dbConnectParams. The name given to the
     * DBConnectParams instances are ignored for this comparison.
     * 
     * @param user
     *            one of the DBConnectParams to compare
     * @param other
     *            the other DBConnectParams to compare
     * @return whether these represent the same database user.
     */
    public static boolean userEquals(DBConnectParams user, DBConnectParams other) {
	if (user == null)
	    return false;
	if (other != null) {
	    if (user == other)
		return true;
	    if (!user.getUser().equals(other.getUser()))
		return false;
	    if (!user.getPassword().equals(other.getPassword()))
		return false;
	    if (!user.getURL().equals(other.getURL()))
		return false;
	    return true;
	}
	return false;
    }

    /**
     * Returns whether this DBConnectParams represnets the same database user
     * account as the given dbConnectParams. The name given to the
     * DBConnectParams instances are ignored for this comparison.
     * 
     * @param dbConnectParams
     * @return whether these represent the same database user.
     */
    public boolean userEquals(DBConnectParams dbConnectParams) {
	if (super.equals(dbConnectParams))
	    return true;
	return userEquals(this, dbConnectParams);
    }

    @Override
    public boolean equals(Object obj) {
	if (super.equals(obj))
	    return true;
	if (obj != null && obj instanceof DBConnectParams) {
	    DBConnectParams other = (DBConnectParams) obj;
	    if (getName().equals(other.getName()) && userEquals(other))
		return true;
	}
	return false;
    }

    @Override
    public String toString() {
	return name;
    }
}
