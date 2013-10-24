/*
 * @(#) $RCSfile: ColumnNameLabeler.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;
import javax.swing.table.TableModel;
import edu.umn.genomics.graph.*;

/**
 * ColumnNameLabeler provides a formatted label for the column names of a
 * TableModel for positions along an axis.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class ColumnNameLabeler implements AxisLabeler, Serializable {
    TableModel tm;
    int maxLen = Integer.MAX_VALUE;

    /**
     * Label integral positions along a graph axis with TableModel column names.
     * 
     * @param tableModel
     *            The TableModel from which to take the column names for axis
     *            labels.
     */
    public ColumnNameLabeler(TableModel tableModel) {
	this.tm = tableModel;
    }

    /**
     * Label integral positions along a graph axis with TableModel column names.
     * 
     * @param tableModel
     *            The TableModel from which to take the column names for axis
     *            labels.
     * @param maxLen
     *            maxLen The maximum character length for the labels returned.
     */
    public ColumnNameLabeler(TableModel tableModel, int maxLen) {
	this.tm = tableModel;
	this.maxLen = maxLen;
    }

    /**
     * Return a label for the given value along an axis.
     * 
     * @param value
     *            the value on the axis.
     * @return a formatted label to display for the given value.
     */
    public String getLabel(double value) {
	Object obj = tm.getColumnName((int) Math.round(value));
	if (obj != null) {
	    String s = obj.toString();
	    return s.length() < maxLen ? s : s.substring(0, maxLen);
	}
	return "";
    }
}
