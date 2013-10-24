/*
 * @(#) $RCSfile: DBTestConnection.java,v $ $Revision: 1.2 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * Test whether a given user can connect to a given data base via JDBC.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBTestConnection {
    /**
     * Test whether a connection can be established with the database.
     * 
     * @param dbuser
     *            JDBC connection parameters.
     * @return true if a connection could be established, otherwise false.
     * @throws Exception
     *             the exception that occurred when attempting this connection.
     */
    public static boolean testConnection(DBConnectParams dbuser)
	    throws Exception {
	return testConnection(dbuser.getUser(), dbuser.getPassword(), dbuser
		.getURL(), dbuser.getDriverName());
    }

    /**
     * Test whether a connection can be established with the database.
     * 
     * @param user
     *            the database user name.
     * @param password
     *            the database password for the user.
     * @param serverURL
     *            the URL for the database connection.
     * @param driverName
     *            the JDBC driver for the connection.
     * @return true if a connection could be established, otherwise false.
     * @throws Exception
     *             the exception that occurred when attempting this connection.
     */
    public static boolean testConnection(String user, String password,
	    String serverURL, String driverName) throws Exception {
	if (user == null)
	    throw new Exception("user name missing");
	if (password == null)
	    throw new Exception("user password missing");
	if (serverURL == null)
	    throw new Exception("data base URL missing");
	if (driverName != null && driverName.length() > 0) {
	    Class drvrClass = Class.forName(driverName);
	    if (drvrClass == null) {
		throw new Exception("JDBC Driver Class for " + driverName
			+ " not found");
	    } else {
		Driver drvr = (Driver) drvrClass.newInstance();
		DriverManager.registerDriver(drvr);
	    }
	}
	Connection tc = DriverManager.getConnection(serverURL, user, password);
	tc.close();
	return true;
    }
}
