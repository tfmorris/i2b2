/*
 * @(#) $RCSfile: SQLTypeNames.java,v $ $Revision: 1.3 $ $Date: 2008/09/05 21:19:13 $ $Name: RELEASE_1_3_1_0001b $
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
import java.lang.reflect.*;

/**
 * A Hashtable to retrieve a java.sql.Types name from the value of that
 * java.sql.Types static field. The names are determined by using reflection on
 * the fields of the java.sql.Types class.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/05 21:19:13 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class SQLTypeNames extends Hashtable {
    static SQLTypeNames sharedInstance = new SQLTypeNames();

    /**
     * Construct a Hashtable of java.sql.Types field names using the value as
     * the key and the field name as the value.
     */
    public SQLTypeNames() {
	try {
	    Field[] fld = java.sql.Types.class.getFields();
	    for (int i = 0; i < fld.length; i++) {
		super.put(fld[i].get(null), fld[i].getName());
	    }
	} catch (Exception ex) {
	    System.err.println("sqlTypeName " + ex);
	}
    }

    /**
     * Return the field name for the java.sql.Types value.
     * 
     * @param value
     *            the java.sql.Types value.
     * @return the field name for the java.sql.Types value, or null if not a
     *         defined value.
     */
    public String get(int value) {
	return (String) get(new Integer(value));
    }

    /**
     * Return a shared instance of this class.
     * 
     * @return an instance of this class.
     */
    public static SQLTypeNames getSharedInstance() {
	return sharedInstance;
    }
}
