/*
 * @(#) $RCSfile: ClusterFeatureTable.java,v $ $Revision: 1.3 $ $Date: 2008/11/18 17:57:56 $ $Name: RELEASE_1_3_1_0001b $
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

package jcluto;

import javax.swing.table.*;

/**
 * ClusterFeatureTable presents a ClusterFeatures instance of a ClutoSolution as
 * a TableModel so that it can be displayed in a JTable.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/18 17:57:56 $ $Name
 */
public class ClusterFeatureTable extends AbstractTableModel {
    ClusterFeatures cf;
    // Columns
    // Cluster#
    // InternalID
    // InternalWgt
    // ...
    // ExternalID
    // ExternalWgt
    // ...
    /** The column names of the cluster features table. */
    protected String[] colNames;
    /**
     * Whether to return the name of the column, or the number index of the
     * column.
     */
    protected boolean useColumnNames = true;

    /**
     * Represent the ClusterFeatures as a TableModel.
     * 
     * @param clusterFeaures
     */
    public ClusterFeatureTable(ClusterFeatures clusterFeaures) {
	cf = clusterFeaures;
	int cnt = cf.getFeatureCount();
	colNames = new String[1 + cnt * 4];
	int ci = 0;
	colNames[ci++] = "Cluster";
	for (int i = 0; i < cnt; i++) {
	    colNames[ci++] = "Descriptive " + (i + 1);
	    colNames[ci++] = "Descriptive " + (i + 1) + "%";
	}
	for (int i = 0; i < cnt; i++) {
	    colNames[ci++] = "Discriminating " + (i + 1);
	    colNames[ci++] = "Discriminating " + (i + 1) + "%";
	}
    }

    /**
     * Represent the ClusterFeatures as a TableModel.
     * 
     * @param clusterFeaures
     *            the features determining by clustering
     * @param useColumnNames
     *            if true represent features by column name, else by column
     *            index.
     */
    public ClusterFeatureTable(ClusterFeatures clusterFeaures,
	    boolean useColumnNames) {
	this(clusterFeaures);
	showColumnNames(useColumnNames);
    }

    /**
     * Set whether the name of the column, or the number index of the column is
     * returned in the table.
     * 
     * @param useColumnNames
     *            if true represent features by column name, else the column
     *            index.
     */
    public void showColumnNames(boolean useColumnNames) {
	this.useColumnNames = useColumnNames;
    }

    /**
     * Return whether to represent features by column name or by column index.
     * 
     * @return if true features represented by column name, else by column
     *         index.
     */
    public boolean showColumnNames() {
	return useColumnNames;
    }

    /**
     * Returns the number of rows in this model.
     * 
     * @return the number of rows in this model
     * @see #getColumnCount
     */
    public int getRowCount() {
	return cf.getClusterCount();
    }

    /**
     * Returns the number of columns in this model.
     * 
     * @return the number of columns in this model
     * @see #getRowCount
     */
    public int getColumnCount() {
	return cf.getFeatureCount() * 4 + 1;
    }

    /**
     * Returns the most specific superclass for all the cell values in the
     * column.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
	if (columnIndex == 0) {
	    return java.lang.String.class;
	} else if (columnIndex % 2 == 1) {
	    if (useColumnNames) {
		return java.lang.String.class;
	    }
	    return java.lang.Integer.class;
	}
	return java.lang.Float.class;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>. This is used
     * to initialize the table's column header name. Note: this name does not
     * need to be unique; two columns in a table can have the same name.
     * 
     * @param columnIndex
     *            the index of the column
     * @return the name of the column
     */
    @Override
    public String getColumnName(int columnIndex) {
	if (columnIndex >= 0 && columnIndex < colNames.length) {
	    return colNames[columnIndex];
	}
	return "col_" + columnIndex;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     * 
     * @param rowIndex
     *            the row whose value is to be queried
     * @param columnIndex
     *            the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
	if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0
		&& columnIndex < getColumnCount()) {
	    if (columnIndex == 0) {
		return "" + rowIndex;
	    } else {
		try {
		    int fn = (columnIndex - 1) % cf.getFeatureCount();
		    if (columnIndex <= cf.getFeatureCount()) {
			if (columnIndex % 2 == 1) {
			    int id = cf.getInternalID(rowIndex, fn);
			    if (useColumnNames) {
				if (cf.getClutoSolution().getMatrix() instanceof TableModel) {
				    String name = ((TableModel) cf
					    .getClutoSolution().getMatrix())
					    .getColumnName(id);
				    if (name != null) {
					return name;
				    }
				}
			    }
			    return new Integer(id);
			} else {
			    return new Float(100f * cf.getInternalWgt(rowIndex,
				    fn));
			}
		    } else if (columnIndex < getColumnCount()) {
			if (columnIndex % 2 == 1) {
			    int id = cf.getExternalID(rowIndex, fn);
			    if (useColumnNames) {
				if (cf.getClutoSolution().getMatrix() instanceof TableModel) {
				    String name = ((TableModel) cf
					    .getClutoSolution().getMatrix())
					    .getColumnName(id);
				    if (name != null) {
					return name;
				    }
				}
			    }
			    return new Integer(id);
			} else {
			    return new Float(100f * cf.getExternalWgt(rowIndex,
				    fn));
			}
		    }
		} catch (Exception ex) {
		}
	    }
	}
	return null;
    }
}
