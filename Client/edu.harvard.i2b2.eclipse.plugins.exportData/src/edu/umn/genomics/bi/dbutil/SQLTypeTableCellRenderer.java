/*
 * @(#) $RCSfile: SQLTypeTableCellRenderer.java,v $ $Revision: 1.3 $ $Date: 2008/09/05 14:54:08 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.table.*;

/**
 * Displays the java.sql.Types name for a java.sql.Types value.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/05 14:54:08 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class SQLTypeTableCellRenderer extends DefaultTableCellRenderer {
    static SQLTypeNames sqlTypeName = SQLTypeNames.getSharedInstance();

    /**
     * Overrides the setValue method method to display the java.sql.Types field
     * name for the value argument.
     */
    @Override
    public void setValue(Object value) {
	try {
	    String s = null;
	    if (value != null && value instanceof Number) {
		s = sqlTypeName.get(((Number) value).intValue());
	    }
	    if (s != null) {
		super.setValue(s);
	    } else {
		super.setValue(value);
	    }
	} catch (Exception ex) {
	    System.err.println(this + " :  " + ex);
	}
    }
}
