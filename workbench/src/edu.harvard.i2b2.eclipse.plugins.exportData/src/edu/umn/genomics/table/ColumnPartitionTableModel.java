/*
 * @(#) $RCSfile: ColumnPartitionTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2003. The Regents of the University of Minnesota  
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

import java.util.*;
import javax.swing.table.*;

/**
 * Presents the distinct values in a ColumnMap as a TableModel with column 0
 * containing the Object and column 1 containing the array of row indices that
 * contain that value.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
public class ColumnPartitionTableModel extends AbstractTableModel {
    static final int[] dummy = new int[0];
    public ColumnMap cmap = null;
    public PartitionIndexMap pim = null;

    public ColumnPartitionTableModel(ColumnMap colMap) {
	cmap = colMap;
	pim = cmap.getPartitionIndexMap();
    }

    @Override
    public String getColumnName(int columnIndex) {
	if (columnIndex == 0) {
	    return "VALUE";
	} else if (columnIndex == 1) {
	    return "ROWS";
	} else if (columnIndex == 2) {
	    return "LIST";
	}
	return "";
    }

    @Override
    public Class getColumnClass(int columnIndex) {
	if (columnIndex == 1) {
	    // return Class.forName("[I");
	    return dummy.getClass();
	}
	return java.lang.Object.class;
    }

    public int getColumnCount() {
	return 2;
    }

    public int getRowCount() {
	return pim != null ? pim.getDstSize() : 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
	if (rowIndex >= 0 && rowIndex < getRowCount()) {
	    int[] ia = pim.getSrcs(rowIndex);
	    if (ia != null) {
		if (columnIndex == 0) {
		    return cmap.getValueAt(ia[0]);
		} else if (columnIndex == 1) {
		    return ia;
		} else {
		    Vector v = new Vector(ia.length);
		    for (int i = 0; i < ia.length; i++) {
			v.add(new Integer(ia[i]));
		    }
		    return v;
		}
	    }
	}
	return null;
    }
}
