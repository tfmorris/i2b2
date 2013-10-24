/*
 * @(#) $RCSfile: DefaultColumnMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * DefaultColumnMap maps the values of a TableModel column to a numeric range.
 * If Class type of the column is Number, or if all of the values of the column
 * are Strings that can be parsed as class Number, the range will be from the
 * minimum to the maximun of the values in the column. If any value can not be
 * parsed as class Number, the values will be mapped to integral values from 0
 * to the number of distinct values - 1, (distinct values are determined by the
 * value's equals method.) The DefaultColumnMap can also be used to select a
 * range of mapped values from the column and indicate the selected rows in a
 * ListSelectionModel. A new selection is be specified as a subrange of values
 * between the the minimum and maximum values of the column. New selections may
 * be combined with the previous selections using standard set operators.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class DefaultColumnMap extends BaseColumnMap implements Serializable,
	TableModelListener {
    static boolean async = true;
    static boolean canRef = false;
    static {
	try {
	    Class.forName("java.lang.ref.SoftReference");
	    // canRef = true;
	} catch (ClassNotFoundException cnfe) {
	}
    }
    int rowCount = 0;
    boolean remap = true;
    boolean recalc = true;

    class MapThread extends Thread {
	@Override
	public void run() {
	    mapTheColumn();
	}
    }

    MapThread mapThread = null;

    private synchronized void needsRemap() {
	// System.err.println(colIndex + " CHECK remap ");
	if (remap) {
	    remap = false;
	    // System.err.println(colIndex + " start remap ");
	    mapColumn();
	    // System.err.println(colIndex + " finish remap ");
	}
    }

    private synchronized void remapNeeded() {
	System.err.println(colIndex + " needs  remap ");
	if (!remap) {
	    remap = true;
	    // setState(CellMap.MAPPING);
	}
    }

    private synchronized void calcStats() {
	if (recalc && getState() == CellMap.MAPPED) {
	    recalc = false;
	    medianSet = false;
	    q1Set = false;
	    q3Set = false;
	    avgSet = false;
	    varianceSet = false;
	    stdDevSet = false;
	    nullCountSet = false;
	    infiniteCountSet = false;
	    distinctCountSet = false;
	    collectStats();
	}
    }

    private synchronized void statsNeeded() {
	recalc = true;
    }

    /**
     * Map the rows in the given column of the table to numeric values.
     * 
     * @param tableModel
     *            the table containing the column of values
     * @param column
     *            the index of the column in the TableModel
     */
    public DefaultColumnMap(TableModel tableModel, int column) {
	super(tableModel, column);
    }

    /**
     * Map the rows in the given column of the table to numeric values. If
     * mapNumbers is false, the distinct values of the column will by mapped as
     * discreet set elements as if they were not numeric.
     * 
     * @param tableModel
     *            the table containing the column of values
     * @param column
     *            the column in the table to map.
     * @param continuous
     *            if true map numeric and date values as a real number in the
     *            range of values from the column, if false treat numbers as
     *            elements of a set.
     */
    public DefaultColumnMap(TableModel tableModel, int column,
	    boolean continuous) {
	super(tableModel, column, continuous);
    }

    /**
     * Sets tableModel as the data model for the column being mapped.
     * 
     * @param tableModel
     *            the data model
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	TableModel tm = getTableModel();
	if (tm != null) {
	    tm.removeTableModelListener(this);
	}
	super.setTableModel(tableModel);
	tableModel.addTableModelListener(this);
	mapColumn();
    }

    /**
     * Return a count of the rows in the TableModel for this ColumnMap.
     * 
     * @return a count of the rows in the TableModel.
     */
    @Override
    public int getCount() {
	needsRemap();
	return rowCount;
    }

    /**
     * Return the sorting order for mapping this column.
     * 
     * @param sortOrder
     *            the sorting order for mapping this column.
     */
    @Override
    public void setSortOrder(int sortOrder) {
	if (sortOrder != sortby && sortOrder >= NATURALSORT
		&& sortOrder <= ROWORDERSORT) {
	    sortby = sortOrder;
	    mapColumn();
	}
    }

    /**
     * Return the num of null values in this column.
     * 
     * @return the num of null values in this column.
     */
    @Override
    public int getNullCount() {
	needsRemap();
	Hashtable colH = colHash;
	if (colH != null) { // discreet values
	    Elem e = (Elem) colH.get(nullTag);
	    return e != null ? e.count : 0;
	}
	calcStats();
	return nullCount;
    }

    /**
     * Return the num of distinct values in this column.
     * 
     * @return the num of distinct values in this column.
     */
    @Override
    public int getDistinctCount() {
	needsRemap();
	Hashtable colH = colHash;
	if (colH != null) { // discreet values
	    return objList != null ? objList.size() : 0;
	}
	calcStats();
	return distinctCount;
    }

    /**
     * Return the median value in this column in this column.
     * 
     * @return the median value in this column in this column.
     */
    @Override
    public double getMedian() { // median value
	needsRemap();
	calcStats();
	return median;
    }

    /**
     * Return the mean value in this column in this column. This is only
     * meaningful for continuously mapped columns.
     * 
     * @return the mean value in this column in this column.
     */
    @Override
    public double getAvg() {
	needsRemap();
	calcStats();
	return avg;
    }

    /**
     * Return the variance of the values in this column in this column. This is
     * only meaningful for continuously mapped columns.
     * 
     * @return the variance of the values in this column in this column.
     */
    @Override
    public double getVariance() { // statistical variance value
	needsRemap();
	calcStats();
	return variance;
    }

    /**
     * Return the standard deviation of the values in this column in this
     * column. This is only meaningful for continuously mapped columns.
     * 
     * @return the standard deviation of the values in this column in this
     *         column.
     */
    @Override
    public double getStdDev() { // standard deviation value
	needsRemap();
	calcStats();
	return stdDev;
    }

    /**
     * Return the minimum mapped value of this column.
     * 
     * @return the minimum mapped value of this column.
     */
    @Override
    public double getMin() {
	needsRemap();
	return min;
    }

    /**
     * Return the maximum mapped value of this column.
     * 
     * @return the maximum mapped value of this column.
     */
    @Override
    public double getMax() {
	needsRemap();
	return max;
    }

    /**
     * Return the mapped value for the given table model row. return the mapped
     * value of each row element in the column.
     */
    @Override
    public double[] getMapValues() {
	needsRemap();
	return super.getMapValues();
    }

    /**
     * Return the mapped value for the given table model row.
     * 
     * @param row
     *            the row in the table return the mapped value of the row
     */
    @Override
    public double getMapValue(int row) {
	if (row < 0 || row >= getCount())
	    return Double.NaN;
	needsRemap();
	return super.getMapValue(row);
    }

    /**
     * Return whether all values in this column are number values.
     * 
     * @return the number of rows that are Number values, or -1 if there is a
     *         non-number.
     */
    private int parseNumbers(int fi, int ti, double nv[][]) {
	int nrows = tm.getRowCount();
	double dv[] = new double[ti - fi + 1];
	double dr[] = new double[2];
	dr[0] = Double.POSITIVE_INFINITY;
	dr[1] = Double.NEGATIVE_INFINITY;
	int nums = 0;
	for (int r = fi, i = 0; r <= ti; r++, i++) {
	    if (Thread.currentThread().isInterrupted()) {
		return -2;
	    }
	    Object o = tm.getValueAt(r, colIndex);
	    if (o == null) {
		dv[i] = Double.NaN;
		continue;
	    }
	    if (o instanceof Number) {
		nums++;
		double val = ((Number) o).doubleValue();
		dv[i] = val;
		if (val > dr[1]) {
		    dr[1] = val;
		}
		if (val < dr[0]) {
		    dr[0] = val;
		}
	    } else {
		return -1;
	    }
	}
	nv[0] = dv;
	nv[1] = dr;
	return nums;
    }

    /**
     * Return whether all values in this column are Date values.
     * 
     * @return the number of rows that are Date values, or -1 if there is a
     *         non-date.
     */
    private int parseDates(int fi, int ti, double nv[][]) {
	int nrows = tm.getRowCount();
	double dv[] = new double[ti - fi + 1];
	double dr[] = new double[2];
	dr[0] = Double.POSITIVE_INFINITY;
	dr[1] = Double.NEGATIVE_INFINITY;
	int nums = 0;
	for (int r = fi, i = 0; r <= ti; r++, i++) {
	    if (Thread.currentThread().isInterrupted()) {
		return -2;
	    }
	    Object o = tm.getValueAt(r, colIndex);
	    if (o == null) {
		dv[i] = Double.NaN;
		continue;
	    }
	    if (o instanceof Calendar) {
		o = ((Calendar) o).getTime();
	    }
	    if (o instanceof Date) {
		nums++;
		double val = ((Date) o).getTime();
		dv[i] = val;
		if (val > dr[1]) {
		    dr[1] = val;
		} else if (val < dr[0]) {
		    dr[0] = val;
		}
		//System.err.println("Date "+(Date)o+" "+((Date)o).getTime()+" "
		// +val );
	    } else {
		return -1;
	    }
	}
	nv[0] = dv;
	nv[1] = dr;
	return nums;
    }

    /**
     * Return whether all values in this column are boolean values.
     * 
     * @return the number of rows that are boolean values, or -1 if there is a
     *         non-boolean.
     */
    private int parseBooleans(int fi, int ti) {
	int nrows = tm.getRowCount();
	int numTrue = 0;
	int numFalse = 0;
	int numNull = 0;
	for (int r = fi, i = 0; r <= ti; r++, i++) {
	    if (Thread.currentThread().isInterrupted()) {
		return -2;
	    }
	    Object o = tm.getValueAt(r, colIndex);
	    if (o == null) {
		numNull++;
		continue;
	    }
	    if (o instanceof Boolean) {
		if (((Boolean) o).booleanValue()) {
		    numTrue++;
		} else {
		    numFalse++;
		}
	    } else if (o instanceof String) {
		String s = o.toString();
		if (s.equalsIgnoreCase("true")) {
		    numTrue++;
		} else if (s.equalsIgnoreCase("false")) {
		    numFalse++;
		} else if (s.equalsIgnoreCase("null")) {
		    numNull++;
		} else {
		    return -1;
		}
	    } else {
		return -1;
	    }
	}
	return numTrue + numFalse;
    }

    /**
     * Return whether all values in this column are number values.
     * 
     * @return true if all values in this column are number values, else false.
     */
    @Override
    public boolean isNumber() {
	needsRemap();
	return super.isNumber();
    }

    /**
     * Return whether all values in this column are Dates.
     * 
     * @return true if all values in this column are Dates, else false.
     */
    @Override
    public boolean isDate() {
	needsRemap();
	return super.isDate();
    }

    /**
     * If the given column of elements in the table is not numeric, map the
     * objects to integral values from 0 to 1 less than the number of distinct
     * elements in the column(as determined by the equals method of the object
     * class of the column.)
     */
    private synchronized void mapColumn() {
	if (tm == null) {
	    return;
	}
	int nrows = tm.getRowCount();
	if (nrows < 1) {
	    return;
	}
	if (async && mapThread != null) {
	    mapThread.interrupt();
	    mapThread = null;
	    Thread.yield();
	}
	if (tm != null && tm.getRowCount() > 0) {
	    if (async) {
		mapThread = new MapThread();
		mapThread
			.setPriority((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 3);
		mapThread.start();
	    } else {
		mapTheColumn();
	    }
	}
    }

    private synchronized void mapTheColumn() {
	long t0 = System.currentTimeMillis();
	// System.err.println("mapTheColumn " + colIndex + "\tstart");
	if (tm == null) {
	    return;
	}
	int nrows = tm.getRowCount();
	if (nrows < 1) {
	    return;
	}
	isNumber = false;
	isDate = false;
	isBoolean = false;
	dvals = null;
	colHash = null;
	objList = null;
	if (tm != null && tm.getRowCount() > 0) {
	    setState(CellMap.MAPPING);
	    double nv[][] = new double[2][];
	    int nums = -1;
	    BitSet vBits = new BitSet();
	    BitSet nBits = new BitSet();
	    // Check if all values are boolean
	    nums = parseBooleans(0, nrows - 1);
	    if (nums > 0) {
		colTyped = true;
		isBoolean = true;
		isNumber = false;
		isDate = false;
		dvals = null;
		setMin(FALSE_VALUE);
		setMax(TRUE_VALUE);
		rowCount = nrows;
	    } else if (tryNumber) {
		nums = parseDates(0, nrows - 1, nv);
		if (nums > 0) { // any not null value is a Date
		    colTyped = true;
		    isNumber = false;
		    isDate = true;
		    dvals = nv[0];
		    min = nv[1][0];
		    max = nv[1][1];
		    rowCount = dvals.length;
		} else {
		    nums = parseNumbers(0, nrows - 1, nv);
		    if (nums > 0) { // any not null value is a number
			colTyped = true;
			isNumber = true;
			isDate = false;
			dvals = nv[0];
			min = nv[1][0];
			max = nv[1][1];
			rowCount = dvals.length;
		    }
		}
	    }
	    if (Thread.currentThread().isInterrupted()) {
		return;
	    }
	    if (nums < 0) { // at least one not null value was not a number
		colTyped = true;
		isNumber = false;
		isDate = false;
		dvals = null;
		mapObjects(0, nrows - 1);
		rowCount = nrows;
	    } else if (nums == 0) { // all null values
		colTyped = false;
		isNumber = false;
		isDate = false;
		mapObject(nullTag); // create a single NULL entry and then
		// adjust count
		try {
		    Elem el = (Elem) colHash.get(nullTag);
		    if (el != null) {
			el.count = nrows;
		    }
		} catch (Exception ex) {
		    ex.printStackTrace();
		    System.err.println("colHash " + colHash);
		    System.err.println("nullTag " + nullTag);
		}
		rowCount = nrows;
	    }
	    long t1 = System.currentTimeMillis();
	    // System.err.println("mapTheColumn " + colIndex + "\tdone " +
	    // (t1-t0));
	    setState(CellMap.MAPPED);
	}
	mapThread = null;
    }

    private void setMinMax() {
	double[] dv = dvals;
	if (dv != null && dv.length > 0) {
	    int nonNulls = 0;
	    double mn = Double.NaN;
	    double mx = Double.NaN;
	    for (int i = 0; i < dv.length; i++) {
		if (!Double.isNaN(dv[i])) {
		    if (nonNulls++ > 0) {
			if (dv[i] < mn) {
			    mn = dv[i];
			}
			if (dv[i] > mx) {
			    mx = dv[i];
			}
		    } else {
			mn = dv[i];
			mx = dv[i];
		    }
		}
	    }
	    // System.err.println("setMinMax " + min + " " + max + " \t" + mn +
	    // " " + mx);
	    setMin(mn);
	    setMax(mx);
	}
    }

    /**
     * The TableModelEvent should be constructed in the coordinate system of the
     * table model.
     * 
     * @param e
     *            the change to the data model
     */
    public void tableChanged(TableModelEvent e) {
	if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
	    // The whole thing changed, the class including this should handle
	    // this
	    // by disposing this ColumnMap and creating new ones.
	    // System.err.println("\n" + colIndex + "HEADER_ROW ");
	    cleanUp();
	    return;
	} else if (e.getColumn() == colIndex
		|| e.getColumn() == TableModelEvent.ALL_COLUMNS) {
	    int rs = e.getFirstRow();
	    int re = e.getType() != TableModelEvent.UPDATE
		    || e.getLastRow() < ((TableModel) e.getSource())
			    .getRowCount() ? e.getLastRow() : ((TableModel) e
		    .getSource()).getRowCount() - 1;
	    switch (e.getType()) {
	    case TableModelEvent.UPDATE:
		// System.err.println("\n" +colIndex + "UPDATE: " + rs + "-" +
		// re);
		// todo see if changed row is consistent with numeric/date
		// determination
		// if numeric/date compare to min and max.
		// if non numeric check hashed values, set min max accordingly
		if (!colTyped) {
		    remapNeeded();
		} else if (isNumber || isDate) {
		    double nv[][] = new double[2][];
		    if ((isDate && (parseDates(rs, re, nv) >= 0))
			    || (isNumber && (parseNumbers(rs, re, nv) >= 0))) {
			double dv[] = nv[0];
			double dr[] = nv[1];
			if (dvals != null && dv != null && re < dvals.length) {
			    setState(CellMap.MAPPING);
			    System.arraycopy(dv, 0, dvals, rs, re - rs + 1);
			    setMinMax();
			    statsNeeded();
			    setState(CellMap.MAPPED);
			    break;
			}
		    } else {
			remapNeeded();
		    }
		    // if non numeric check hashed values, set min max
		    // accordingly
		} else {
		}
		remapNeeded();
		break;
	    case TableModelEvent.INSERT:
		// System.err.println("\n" +colIndex + "INSERT: " + rs + "-" +
		// re);
		// todo see if changed row is consistent with numeric
		// determination
		// if numeric compare to min and max.
		if (!colTyped) {
		    remapNeeded();
		} else if (isNumber || isDate) {
		    double nv[][] = new double[2][];
		    if ((isDate && (parseDates(rs, re, nv) >= 0))
			    || (isNumber && (parseNumbers(rs, re, nv) >= 0))) {
			double dv[] = nv[0];
			double dr[] = nv[1];
			if (dv != null) {
			    setState(CellMap.MAPPING);
			    if (dvals == null && rs == 0) {
				dvals = dv;
			    } else {
				// save prev values;
				double tmp[] = dvals;
				// allocate array
				dvals = new double[(re - rs + 1)
					+ (tmp != null ? tmp.length : 0)];
				if (tmp != null) {
				    // copy part before rs
				    System.arraycopy(tmp, 0, dvals, 0, rs);
				    // copy part after re
				    System.arraycopy(tmp, rs, dvals, re + 1,
					    tmp.length - rs);
				}
				// copy insert
				System.arraycopy(dv, 0, dvals, rs, re - rs + 1);
			    }
			    if (dr != null) {
				if (dr[0] < min) {
				    setMin(dr[0]);
				}
				if (dr[1] > max) {
				    setMax(dr[1]);
				}
			    }
			    rowCount = dvals.length;
			    statsNeeded();
			    setState(CellMap.MAPPED);
			    break;
			}
		    } else {
			remapNeeded();
		    }
		    // if non numeric check hashed values, set min max
		    // accordingly
		} else {
		    mapObjects(rs, re);
		}
		break;
	    case TableModelEvent.DELETE:
		// System.err.println("\n" +colIndex + "DELETE: " + rs + "-" +
		// re);
		// if non numeric, see if this eliminates a distinct value in
		// Hashtable
		// see if change to min or max
		// for now just remap for everything
		if (colTyped && (isNumber || isDate)) {
		    double dv[] = dvals;
		    if (dv != null) {
			setState(CellMap.MAPPING);
			int len = dv.length - (re - rs + 1);
			dvals = new double[len];
			rowCount = dvals.length;
			if (rs > 0) {
			    // System.err.println("System.arraycopy("+ dv +
			    // ",0,"+dvals+",0,"+rs+")" );
			    System.arraycopy(dv, 0, dvals, 0, rs);
			}
			if (re < dv.length - 1) {
			    System.arraycopy(dv, re + 1, dvals, rs, dv.length
				    - 1 - re);
			}
			setMinMax();
			statsNeeded();
			setState(CellMap.MAPPED);
			break;
		    }
		}
		remapNeeded();
		break;
	    }
	    needsRemap();
	}
    }

    @Override
    public void cleanUp() {
	TableModel tm = getTableModel();
	if (tm != null)
	    tm.removeTableModelListener(this);
	super.cleanUp();
    }

}
