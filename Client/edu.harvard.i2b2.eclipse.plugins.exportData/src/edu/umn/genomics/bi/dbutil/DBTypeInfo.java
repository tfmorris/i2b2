/*
 * @(#) $RCSfile: DBTypeInfo.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;

/**
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBTypeInfo implements Comparable {
    public String type_name;
    public short data_type;
    public int precision;
    public String literal_prefix;
    public String literal_suffix;
    public String create_params;
    public short nullable;
    public boolean case_sensitive;
    public short searchable;
    public boolean unsigned_attribute;
    public boolean fixed_prec_scale;
    public boolean auto_increment;
    public String local_type_name;
    public short minimum_scale;
    public short maximum_scale;
    public int sql_data_type;
    public int sql_datetime_sub;
    public int num_prec_radix;

    /**
     * Creates DBTypeInfo instance for the current row in the resultset.
     */
    public DBTypeInfo(ResultSet rs) {
	try {
	    type_name = rs.getString(1);
	    data_type = rs.getShort(2);
	    precision = rs.getInt(3);
	    literal_prefix = rs.getString(4);
	    literal_suffix = rs.getString(5);
	    create_params = rs.getString(6);
	    nullable = rs.getShort(7);
	    case_sensitive = rs.getBoolean(8);
	    searchable = rs.getShort(9);
	    unsigned_attribute = rs.getBoolean(10);
	    fixed_prec_scale = rs.getBoolean(11);
	    auto_increment = rs.getBoolean(12);
	    local_type_name = rs.getString(13);
	    minimum_scale = rs.getShort(14);
	    maximum_scale = rs.getShort(15);
	    sql_data_type = rs.getInt(16);
	    sql_datetime_sub = rs.getInt(17);
	    num_prec_radix = rs.getInt(18);
	} catch (SQLException sqlex) {
	}
    }

    public DBTypeInfo makeDBTypeInfo(ResultSet rs) {
	return null;
    }

    public HashSet makeDBTypeInfoHash(ResultSet rs) {
	return null;
    }

    @Override
    public String toString() {
	return type_name + (create_params != null ? create_params : "");
    }

    public int compareTo(Object o) {
	return compareTo(((DBTypeInfo) o));
    }

    public int compareTo(DBTypeInfo o) {
	int c = -1;
	c = type_name.compareTo(o.type_name);
	if (c == 0)
	    c = precision - o.precision;
	if (c == 0)
	    c = data_type - o.data_type;
	return c;
    }

}
