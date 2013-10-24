/*
 * @(#) $RCSfile: DBConnectParams.java,v $ $Revision: 1.2 $ $Date: 2008/09/04 18:39:02 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * An interface for holding the parameters required for making a JDBC connection
 * to a data base.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/09/04 18:39:02 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface DBConnectParams {
    /**
     * Return the name for this connection.
     * 
     * @return the for this connection.
     */
    public String getName();

    /**
     * Return the user account name.
     * 
     * @return the user account name.
     */
    public String getUser();

    /**
     * Return the user password.
     * 
     * @return the user password.
     */
    public String getPassword();

    /**
     * Return the data base URL.
     * 
     * @return the data base URL.
     */
    public String getURL();

    /**
     * Return the class name for the JDBC Driver Class.
     * 
     * @return the class name for the JDBC Driver Class.
     */
    public String getDriverName();

    /**
     * Returns whether this DBConnectParams represents the same database user
     * account as the given dbConnectParams. The name given to the
     * DBConnectParams instances are ignored for this comparison.
     * 
     * @param dbConnectParams
     * @return whether these represent the same database user.
     */
    public boolean userEquals(DBConnectParams dbConnectParams);
}
