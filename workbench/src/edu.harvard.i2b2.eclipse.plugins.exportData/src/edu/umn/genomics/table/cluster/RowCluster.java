/*
 * @(#) $RCSfile: RowCluster.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 18:06:11 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster;

import javax.swing.table.*;

/**
 * A RowCluster is a leaf node that points a row in the data table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 18:06:11 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class RowCluster extends Cluster {
    TableModel tm = null;
    int rowIndex = 0;
    int cols[] = null;

    public RowCluster(TableModel tm, int rowIndex, int columnIndices[]) {
	this.tm = tm;
	this.rowIndex = rowIndex;
	this.cols = columnIndices;
	setAllowsChildren(false);
    }

    public int getIndex() {
	return rowIndex;
    }

    @Override
    public double[] getValues() {
	double vals[] = new double[cols.length];
	for (int i = 0; i < cols.length; i++) {
	    Object o = tm.getValueAt(rowIndex, cols[i]);
	    if (o instanceof Number) {
		vals[i] = ((Number) o).doubleValue();
	    }
	}
	return vals;
    }

    @Override
    public String toString() {
	return "" + rowIndex;
    }
}
