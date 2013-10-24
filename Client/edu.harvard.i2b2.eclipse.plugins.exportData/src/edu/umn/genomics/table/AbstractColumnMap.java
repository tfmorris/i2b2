/*
 * @(#) $RCSfile: AbstractColumnMap.java,v $ $Revision: 1.5 $ $Date: 2008/10/31 15:49:03 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import edu.umn.genomics.graph.LineFormula;

/**
 * AbstractColumnMap maps the values of a TableModel column to a numeric range.
 * If Class type of the column is Number, or if all of the values of the column
 * are Strings that can be parsed as class Number, the range will be from the
 * minimum to the maximun of the values in the column. If any value can not be
 * parsed as class Number, the values will be mapped to integral values from 0
 * to the number of distinct values - 1, (distinct values are determined by the
 * value's equals method.) The AbstractColumnMap can also be used to select a
 * range of mapped values from the column and indicate the selected rows in a
 * ListSelectionModel. A new selection is be specified as a subrange of values
 * between the the minimum and maximum values of the column. New selections may
 * be combined with the previous selections using standard set operators.
 * 
 * @author J Johnson
 * @version $Revision: 1.5 $ $Date: 2008/10/31 15:49:03 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public abstract class AbstractColumnMap implements Serializable, ColumnMap,
	CleanUp {
    protected static final double TRUE_VALUE = 1.;
    protected static final double FALSE_VALUE = 0.;
    protected static final double NULL_VALUE = Double.NaN;
    protected TableModel tm = null;
    protected ListSelectionModel lsm = null;
    protected EventListenerList listenerList = new EventListenerList();
    protected int setOperator = SetOperator.REPLACE;
    protected int colIndex = 0;
    protected boolean tryNumber = true; // try to map as Number, else map as
    // Object
    protected boolean isNumber = false; // whether all elements are of type
    // Number
    protected boolean isDate = false; // whether all elements are Dates
    protected boolean isBoolean = false; // whether all elements are Boolean
    protected boolean colTyped = false; // the type of the elememts has been
    // determined.
    protected Class colClass = null;
    protected int colType = 0;
    protected static int defaultsortby = NATURALSORT;
    protected int sortby = defaultsortby;
    // stats
    protected double min = Double.MAX_VALUE;
    protected double max = Double.MIN_VALUE;
    protected double median = Double.NaN;
    protected double q1 = Double.NaN;
    protected double q3 = Double.NaN;
    protected double avg = Double.NaN;
    protected double variance = Double.NaN;
    protected double stdDev = Double.NaN;
    protected int nullCount = -1;
    protected int infiniteCount = -1;
    protected int distinctCount = -1;

    protected boolean minSet = false;
    protected boolean maxSet = false;
    protected boolean medianSet = false;
    protected boolean q1Set = false;
    protected boolean q3Set = false;
    protected boolean avgSet = false;
    protected boolean varianceSet = false;
    protected boolean stdDevSet = false;
    protected boolean nullCountSet = false;
    protected boolean infiniteCountSet = false;
    protected boolean distinctCountSet = false;

    protected static final Object nullTag = new Object() {
	@Override
	public String toString() {
	    return "\"\"";
	}
    };
    protected String name = null;
    protected int mapState = CellMap.UNMAPPED;

    /**
     * Map the rows in the given column of the table to numeric values.
     * 
     * @param tableModel
     *            the table containing the column of values
     * @param column
     *            the index of the column in the TableModel
     */
    public AbstractColumnMap(TableModel tableModel, int column) {
	this.colIndex = column;
	setTableModel(tableModel);
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
    public AbstractColumnMap(TableModel tableModel, int column,
	    boolean continuous) {
	this.colIndex = column;
	setMapping(continuous);
	setTableModel(tableModel);
    }

    /**
     * Return the state of mapping.
     * 
     * @return The state of this map.
     * @see CellMap#UNMAPPED
     * @see CellMap#MAPPING
     * @see CellMap#MAPPED
     * @see CellMap#INVALID
     */
    public int getState() {
	return mapState;
    }

    /**
     * Set the state of mapping.
     * 
     * @param mapState
     *            The mapping state of this map.
     * @see CellMap#UNMAPPED
     * @see CellMap#MAPPING
     * @see CellMap#MAPPED
     * @see CellMap#INVALID
     */
    protected void setState(int mapState) {
	if (this.mapState != mapState) {
	    this.mapState = mapState;
	    fireColumnMapChanged(mapState);
	}
    }

    /**
     * Set the default sorting order for this CellMap Class. The implementing
     * class needs to store this in a static variable.
     * 
     * @param sortOrder
     *            the default sorting order for this CellMap Class.
     */
    public void setDefaultSortOrder(int sortOrder) {
	defaultsortby = sortOrder;
    }

    /**
     * Return the default sorting order for this CellMap Class.
     * 
     * @return the default sorting order for this CellMap Class.
     */
    public int getDefaultSortOrder() {
	return defaultsortby;
    }

    /**
     * Set whether to map Number and Dates values on a continuum or as a set of
     * discreet elements.
     */
    public void setMapping(boolean continuous) {
	tryNumber = continuous;
    }

    /**
     * Return whether Number and Dates values are mapped on a continuum or as a
     * set of discreet elements.
     */
    public boolean getMapping() {
	return tryNumber;
    }

    /**
     * Sets tableModel as the data model for the column being mapped.
     * 
     * @param tableModel
     *            the data model
     */
    public void setTableModel(TableModel tableModel) {
	tm = tableModel;
    }

    /**
     * Return the table model being displayed.
     * 
     * @return the table being displayed.
     */
    public TableModel getTableModel() {
	return tm;
    }

    /**
     * Return the column index that is be mapped.
     * 
     * @return the index of the column of the TableModel this maps.
     */
    public int getColumnIndex() {
	return colIndex;
    }

    /**
     * Return the Class of the column in the TableModel.
     * 
     * @return the Class of the column in the TableModel.
     */
    public Class getColumnClass() {
	if (colClass != null) {
	    return colClass;
	}
	if (tm != null && colIndex < tm.getColumnCount()) {
	    return tm.getColumnClass(colIndex);
	}
	return java.lang.Object.class;
    }

    /**
     * Return the value for this column in the TableModel at the given rowIndex.
     * This is a convenience method for TableModel.getValueAt(int rowIndex, int
     * columnIndex), however, an implementation may choose to store the values
     * outside of the table.
     * 
     * @param rowIndex
     *            the row index in the TableModel.
     * @return the value for this column in the TableModel at the given
     *         rowIndex.
     */
    public Object getValueAt(int rowIndex) {
	if (tm != null) {
	    return tm.getValueAt(rowIndex, colIndex);
	}
	return null;
    }

    /**
     * Return a count of the rows in the TableModel for this ColumnMap.
     * 
     * @return a count of the rows in the TableModel.
     */
    public int getCount() {
	return tm != null ? tm.getRowCount() : 0;
    }

    /**
     * Return the name given this column of the TableModel.
     * 
     * @return the name given this column of the TableModel
     */
    public String getName() {
	if (name != null) {
	    return name;
	}
	if (tm != null && colIndex < tm.getColumnCount()) {
	    return tm.getColumnName(colIndex);
	}
	return null;
    }

    /**
     * Set the name for this map.
     * 
     * @param name
     *            the name for this map.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Sets the row selection model for this table to newModel and registers
     * with for listener notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    public void setSelectionModel(ListSelectionModel newModel) {
	lsm = newModel;
    }

    /**
     * Returns the ListSelectionModel that is used to maintain row selection
     * state.
     * 
     * @return the object that provides row selection state.
     */
    public ListSelectionModel getSelectionModel() {
	return lsm;
    }

    /**
     * Return the sorting order for mapping this column.
     * 
     * @return the sorting order for mapping this column.
     */
    public int getSortOrder() {
	return sortby;
    }

    /**
     * Return the sorting order for mapping this column.
     * 
     * @param sortOrder
     *            the sorting order for mapping this column.
     */
    public void setSortOrder(int sortOrder) {
	if (sortOrder != sortby && sortOrder >= NATURALSORT
		&& sortOrder <= ROWORDERSORT) {
	    sortby = sortOrder;
	}
    }

    /**
     * Return the num of null values in this column.
     * 
     * @return the num of null values in this column.
     */
    public int getNullCount() {
	return nullCount;
    }

    /**
     * Return the num of infinite values in this column.
     * 
     * @return the num of infinite values in this column.
     */
    public int getInfiniteCount() {
	return infiniteCount;
    }

    /**
     * Return the num of distinct values in this column.
     * 
     * @return the num of distinct values in this column.
     */
    public int getDistinctCount() {
	return distinctCount;
    }

    /**
     * Return the median value in this column in this column.
     * 
     * @return the median value in this column in this column.
     */
    public double getMedian() { // median value
	return median;
    }

    /**
     * Return the first quartile value in this column in this column.
     * 
     * @return the first quartile value in this column in this column.
     */
    public double getQ1() { // first quartile value
	return q1;
    }

    /**
     * Return the third quartile value in this column in this column.
     * 
     * @return the third quartile value in this column in this column.
     */
    public double getQ3() { // third quartile value
	return q3;
    }

    /**
     * Return the avg value in this column in this column. This is only
     * meaningful for continuously mapped columns.
     * 
     * @return the avg value in this column in this column.
     */
    public double getAvg() {
	return avg;
    }

    /**
     * Return the variance of the values in this column in this column. This is
     * only meaningful for continuously mapped columns.
     * 
     * @return the variance of the values in this column in this column.
     */
    public double getVariance() { // statistical variance value
	return variance;
    }

    /**
     * Return the standard deviation of the values in this column in this
     * column. This is only meaningful for continuously mapped columns.
     * 
     * @return the standard deviation of the values in this column in this
     *         column.
     */
    public double getStdDev() { // standard deviation value
	return stdDev;
    }

    /**
     * Return the minimum mapped value of this column.
     * 
     * @return the minimum mapped value of this column.
     */
    public double getMin() {
	return min;
    }

    /**
     * Return the maximum mapped value of this column.
     * 
     * @return the maximum mapped value of this column.
     */
    public double getMax() {
	return max;
    }

    /**
     * Set the number of null values in this column.
     * 
     * @param nullCount
     *            the number of null values in this column.
     */
    public void setNullCount(int nullCount) {
	this.nullCount = nullCount;
	nullCountSet = true;
    }

    /**
     * Set the number of infinite values in this column.
     * 
     * @param infiniteCount
     *            the number of infinite values in this column.
     */
    public void setInfiniteCount(int infiniteCount) {
	this.infiniteCount = infiniteCount;
	infiniteCountSet = true;
    }

    /**
     * Set the number of distinct values in this column.
     * 
     * @param distinctCount
     *            the number of distinct values in this column.
     */
    public void setDistinctCount(int distinctCount) {
	this.distinctCount = distinctCount;
	distinctCountSet = true;
    }

    /**
     * Set the median value in this column in this column.
     * 
     * @param median
     *            the median value in this column in this column.
     */
    public void setMedian(double median) { // median value
	this.median = median;
	medianSet = true;
    }

    /**
     * Set the first quartile value in this column in this column.
     * 
     * @param q1
     *            the first quartile value in this column.
     */
    public void setQ1(double q1) { // first quartile value
	this.q1 = q1;
	q1Set = true;
    }

    /**
     * Set the third quartile value in this column in this column.
     * 
     * @param q3
     *            the third quartile value in this column.
     */
    public void setQ3(double q3) { // third quartile value
	this.q3 = q3;
	q3Set = true;
    }

    /**
     * Set the mean avaerage value in this column in this column. This is only
     * meaningful for continuously mapped columns.
     * 
     * @param avg
     *            the mean avaerage value in this column in this column.
     */
    public void setAvg(double avg) {
	this.avg = avg;
	avgSet = true;
    }

    /**
     * Set the variance of the values in this column in this column. This is
     * only meaningful for continuously mapped columns.
     * 
     * @param variance
     *            the variance of the values in this column in this column.
     */
    public void setVariance(double variance) { // statistical variance value
	this.variance = variance;
	varianceSet = true;
    }

    /**
     * Set the standard deviation of the values in this column in this column.
     * This is only meaningful for continuously mapped columns.
     * 
     * @param stdDev
     *            the standard deviation of the values in this column in this
     *            column.
     */
    public void setStdDev(double stdDev) { // standard deviation value
	this.stdDev = stdDev;
	stdDevSet = true;
    }

    /**
     * Set the minimum mapped value of this column.
     * 
     * @param min
     *            the minimum mapped value of this column.
     */
    public void setMin(double min) {
	this.min = min;
	minSet = true;
    }

    /**
     * Set the maximum mapped value of this column.
     * 
     * @param max
     *            the maximum mapped value of this column.
     */
    public void setMax(double max) {
	this.max = max;
	maxSet = true;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isMinSet() {
	return minSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isMaxSet() {
	return maxSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isMedianSet() {
	return medianSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isQ1Set() {
	return q1Set;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isQ3Set() {
	return q3Set;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isAvgSet() {
	return avgSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isVarianceSet() {
	return varianceSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isStddevSet() {
	return stdDevSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    public boolean isNullCountSet() {
	return nullCountSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    protected boolean isNumInfiniteSet() {
	return infiniteCountSet;
    }

    /**
     * Return whether this value has been set.
     * 
     * @return whether this value has been set.
     */
    protected boolean isDistinctCountSet() {
	return distinctCountSet;
    }

    /**
     * Return the mapped value for the given table model row. return the mapped
     * value of each row element in the column.
     */
    public abstract double[] getMapValues();

    /**
     * Return the mapped value for the given table model row.
     * 
     * @param row
     *            the row in the table return the mapped value of the row
     */
    public abstract double getMapValue(int row);

    /**
     * Return the element that is mapped nearest to the mapValue in the given
     * direction.
     * 
     * @param mapValue
     *            the relative position on the map
     * @param dir
     *            negative means round down, positive mean round up, 0 rounds to
     *            closest.. return the element that is mapped nearest to the
     *            mapValue.
     */
    public abstract Object getMappedValue(double mapValue, int dir);

    /**
     * Get a mapping from a sorted list of distinct values to the table rows
     * that contain that value. PartitionIndexMap.getSrcs(0) will return the
     * indices of the table rows that contain that contain the first value in
     * order.
     * 
     * @return a mapping from a sorted list of distinct values to the table rows
     */
    public PartitionIndexMap getPartitionIndexMap() {
	double[] vals = getMapValues();
	int[] ia = new int[vals.length];
	for (int i = 0; i < ia.length; i++) {
	    ia[i] = i;
	}
	Cells.sort(vals, ia);
	int[] pi = new int[vals.length];
	if (pi.length > 0) {
	    pi[0] = 0;
	    for (int i = 1, n = 0; i < vals.length; i++) {
		pi[ia[i]] = vals[i] > vals[i - 1] ? ++n : n;
	    }
	}
	vals = null;
	ia = null;
	return new PartitionIndexMap(pi);
    }

    /**
     * Construct a histogram returning the count of elements in each subrange
     * along the ColumnMap.
     * 
     * @param buckets
     *            the number of divisions of the range of values. If the value
     *            is zero, the default numbers of subdivisions be returned: 10
     *            if the column is numeric, else the number of discrete
     *            elements.
     * @param withSelectCounts
     * @return an array of count of elements in each range.
     */
    public abstract int[] getCounts(int buckets, boolean withSelectCounts);

    public boolean isContinuous() {
	return isNumber() || isDate();
    }

    /**
     * Return whether all values in this column are number values.
     * 
     * @return true if all values in this column are number values, else false.
     */
    public boolean isNumber() {
	return isNumber;
    }

    /**
     * Return whether all values in this column are Dates.
     * 
     * @return true if all values in this column are Dates, else false.
     */
    public boolean isDate() {
	return isDate;
    }

    /**
     * Return whether all values in this column are Boolean.
     * 
     * @return true if all values in this column are Boolean, else false.
     */
    public boolean isBoolean() {
	return isBoolean
		|| java.lang.Boolean.class.isAssignableFrom(tm
			.getColumnClass(colIndex));
    }

    /**
     * Call to release resources associated with this map.
     */
    public void cleanUp() {
	setState(CellMap.INVALID);
	fireColumnMapChanged(CellMap.INVALID);
    }

    /**
     * Set the selection set operator to use when combining the current set of
     * selected values with the previous set of selected values.
     * 
     * @see SetOperator#REPLACE
     * @see SetOperator#BRUSHOVER
     * @see SetOperator#UNION
     * @see SetOperator#INTERSECTION
     * @see SetOperator#DIFFERENCE
     * @see SetOperator#XOR
     */
    public void setSetOperator(int setOperator) {
	this.setOperator = setOperator;
    }

    /**
     * Return the selection set operator in effect for combining a new set of
     * selected values with the previous set of selected values.
     * 
     * @return the current selection set operator being used
     */
    public int getSetOperator() {
	return setOperator;
    }

    /**
     * Select the rows of the table model that are mapped in the given range of
     * this ColumnMap. The selected rows are set in the given ListSelection
     * Model.
     * 
     * @param from
     *            the start of the selected range
     * @param to
     *            the end of the selected range
     * @param sm
     *            the ListSelectionModel in which to set the rows indices.
     */
    public void selectRange(double from, double to, ListSelectionModel sm) {
	// System.err.println(this + "\tselectRange\t" + from + "\t" + to + "\t"
	// + sm);
	int nrows = tm.getRowCount();
	double cmin, cmax;
	boolean doNull = Double.isNaN(from) || Double.isNaN(to);
	boolean doInf = Double.isInfinite(from) || Double.isInfinite(to);
	boolean doPos = doInf && from > 0. || to > 0.;
	boolean doNeg = doInf && from < 0. || to < 0.;
	if (doNull) {
	    if (Double.isNaN(from) && Double.isNaN(to)) {
		cmin = Double.NEGATIVE_INFINITY;
		cmax = Double.NEGATIVE_INFINITY;
	    } else {
		cmin = Double.isNaN(from) ? Double.NEGATIVE_INFINITY : from;
		cmax = Double.isNaN(to) ? Double.POSITIVE_INFINITY : to;
	    }
	} else if (from < to) {
	    cmin = from;
	    cmax = to;
	} else {
	    cmin = to;
	    cmax = from;
	}
	sm.clearSelection();
	sm.setValueIsAdjusting(true);
	for (int r = 0; r < nrows; r++) {
	    double v = getMapValue(r);
	    if ((v >= cmin && v <= cmax)
		    || (doNull && Double.isNaN(v))
		    || (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
		sm.addSelectionInterval(r, r);
	    }
	}
	sm.setValueIsAdjusting(false);
    }

    /**
     * Select the rows of the table model that are mapped in the given range of
     * this ColumnMap. The selected rows are set in the ListSelection Model that
     * was set for this ColumnMap.
     * 
     * @param from
     *            the start of the selected range
     * @param to
     *            the end of the selected range
     * @see #setSetOperator
     * @see #setSelectionModel
     */
    public void selectRange(double from, double to) {
	// System.err.println(this + "\tselectRange\t" + from + "\t" + to);
	double cmin, cmax;
	boolean doNull = Double.isNaN(from) || Double.isNaN(to);
	boolean doInf = Double.isInfinite(from) || Double.isInfinite(to);
	boolean doPos = doInf && from > 0. || to > 0.;
	boolean doNeg = doInf && from < 0. || to < 0.;
	if (lsm == null) {
	    return;
	}
	if (doNull) {
	    if (Double.isNaN(from) && Double.isNaN(to)) {
		cmin = Double.NEGATIVE_INFINITY;
		cmax = Double.NEGATIVE_INFINITY;
	    } else {
		cmin = Double.isNaN(from) ? Double.NEGATIVE_INFINITY : from;
		cmax = Double.isNaN(to) ? Double.POSITIVE_INFINITY : to;
	    }
	} else if (from < to) {
	    cmin = from;
	    cmax = to;
	} else {
	    cmin = to;
	    cmax = from;
	}
	ListSelectionModel lsmOld;
	int nrows = tm.getRowCount();
	lsm.setValueIsAdjusting(true);
	switch (setOperator) {
	case SetOperator.UNION:
	    for (int r = 0; r < nrows; r++) {
		double v = getMapValue(r);
		if ((v >= cmin && v <= cmax)
			|| (doNull && Double.isNaN(v))
			|| (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
		    lsm.addSelectionInterval(r, r);
		}
	    }
	    break;
	case SetOperator.DIFFERENCE:
	    for (int r = 0; r < nrows; r++) {
		double v = getMapValue(r);
		if ((v >= cmin && v <= cmax)
			|| (doNull && Double.isNaN(v))
			|| (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
		    lsm.removeSelectionInterval(r, r);
		}
	    }
	    break;
	case SetOperator.INTERSECTION:
	    if (lsm.getMinSelectionIndex() < 0) {
		break;
	    }
	    try {
		lsmOld = (DefaultListSelectionModel) ((DefaultListSelectionModel) lsm)
			.clone();
	    } catch (Exception ex) {
		int min = lsm.getMinSelectionIndex();
		int max = lsm.getMaxSelectionIndex();
		lsmOld = new DefaultListSelectionModel();
		if (min >= 0) {
		    for (int i = min; i <= max; i++) {
			if (lsm.isSelectedIndex(i)) {
			    lsmOld.addSelectionInterval(i, i);
			}
		    }
		}
	    }
	    lsm.clearSelection();
	    for (int r = 0; r < nrows; r++) {
		double v = getMapValue(r);
		if ((v >= cmin && v <= cmax)
			|| (doNull && Double.isNaN(v))
			|| (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
		    if (lsmOld.isSelectedIndex(r)) {
			lsm.addSelectionInterval(r, r);
		    }
		}
	    }
	    break;
	case SetOperator.XOR:
	    if (lsm.getMinSelectionIndex() < 0) {
		for (int r = 0; r < nrows; r++) {
		    double v = getMapValue(r);
		    if ((v >= cmin && v <= cmax)
			    || (doNull && Double.isNaN(v))
			    || (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
			lsm.addSelectionInterval(r, r);
		    }
		}
		break;
	    }
	    try {
		lsmOld = (DefaultListSelectionModel) ((DefaultListSelectionModel) lsm)
			.clone();
	    } catch (Exception ex) {
		int min = lsm.getMinSelectionIndex();
		int max = lsm.getMaxSelectionIndex();
		lsmOld = new DefaultListSelectionModel();
		if (min >= 0) {
		    for (int i = min; i <= max; i++) {
			if (lsm.isSelectedIndex(i)) {
			    lsmOld.addSelectionInterval(i, i);
			}
		    }
		}
	    }
	    for (int r = 0; r < nrows; r++) {
		double v = getMapValue(r);
		if ((v >= cmin && v <= cmax)
			|| (doNull && Double.isNaN(v))
			|| (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
		    if (lsmOld.isSelectedIndex(r)) {
			lsm.removeSelectionInterval(r, r);
		    } else {
			lsm.addSelectionInterval(r, r);
		    }
		}
	    }
	    break;
	case SetOperator.BRUSHOVER:
	case SetOperator.REPLACE:
	    lsm.clearSelection();
	    for (int r = 0; r < nrows; r++) {
		double v = getMapValue(r);
		if ((v >= cmin && v <= cmax)
			|| (doNull && Double.isNaN(v))
			|| (doInf && Double.isInfinite(v) && ((doPos && v > 0.) || (doNeg && v < 0.)))) {
		    lsm.addSelectionInterval(r, r);
		}
	    }
	    break;
	}
	lsm.setValueIsAdjusting(false);
    }

    /**
     * Apply the given selection to the current selection for this column, using
     * the setOperator currently in effect.
     * 
     * @param selection
     *            the selected rows to combine with the previous selection
     * @see #setSetOperator
     * @see #setSelectionModel
     */
    public void selectValues(ListSelectionModel selection) {
	double cmin, cmax;
	if (lsm == null || selection == null) {
	    return;
	}
	int nrows = tm.getRowCount();
	int min = selection.getMinSelectionIndex();
	int max = selection.getMaxSelectionIndex();
	lsm.setValueIsAdjusting(true);
	switch (setOperator) {
	case SetOperator.UNION:
	    if (min < 0)
		break;
	    for (int r = min; r <= max; r++) {
		if (selection.isSelectedIndex(r)) {
		    lsm.addSelectionInterval(r, r);
		}
	    }
	    break;
	case SetOperator.DIFFERENCE:
	    if (min < 0)
		break;
	    for (int r = min; r <= max; r++) {
		if (selection.isSelectedIndex(r)) {
		    lsm.removeSelectionInterval(r, r);
		}
	    }
	    break;
	case SetOperator.INTERSECTION:
	    if (lsm.getMinSelectionIndex() < 0)
		break;
	    min = lsm.getMinSelectionIndex();
	    max = lsm.getMaxSelectionIndex();
	    for (int r = min; r <= max; r++) {
		if (!selection.isSelectedIndex(r)) {
		    lsm.removeSelectionInterval(r, r);
		}
	    }
	    break;
	case SetOperator.XOR:
	    if (lsm.getMinSelectionIndex() < 0) {
		for (int r = min; r <= max; r++) {
		    if (!selection.isSelectedIndex(r)) {
			lsm.addSelectionInterval(r, r);
		    }
		}
		break;
	    }
	    for (int r = min; r <= max; r++) {
		if (selection.isSelectedIndex(r)) {
		    if (lsm.isSelectedIndex(r)) {
			lsm.removeSelectionInterval(r, r);
		    } else {
			lsm.addSelectionInterval(r, r);
		    }
		}
	    }
	    break;
	case SetOperator.BRUSHOVER:
	case SetOperator.REPLACE:
	    lsm.clearSelection();
	    if (min < 0)
		break;
	    for (int r = min; r <= max; r++) {
		if (selection.isSelectedIndex(r)) {
		    lsm.addSelectionInterval(r, r);
		}
	    }
	    break;
	}
	lsm.setValueIsAdjusting(false);
    }

    protected void collectStats(double[] dvals) {
	if (dvals == null) {
	    return;
	}
	double _median = getMedian();
	double _q1 = getQ1();
	double _q3 = getQ3();
	double _avg = getAvg();
	double _variance = getVariance();
	double _stdDev = getStdDev();
	int _nullCount = getNullCount();
	int _infiniteCount = getInfiniteCount();
	int _distinctCount = getDistinctCount();
	double vals[] = new double[dvals.length];
	System.arraycopy(dvals, 0, vals, 0, vals.length);
	if (!isMinSet() || !isMinSet() || !isNullCountSet()
		|| !isDistinctCountSet() || !isMedianSet()) {
	    Arrays.sort(vals); // JDK1.2
	    if (!isMinSet()) {
		for (int i = 0; i < vals.length; i++) {
		    if (!Double.isNaN(vals[i])) {
			setMin(vals[i]);
			break;
		    }
		}
	    }
	    if (!isMaxSet()) {
		for (int i = vals.length - 1; i >= 0; i--) {
		    if (!Double.isNaN(vals[i])) {
			setMax(vals[i]);
			break;
		    }
		}
	    }
	    // num null
	    if (!isNullCountSet()) {
		_nullCount = 0;
		_infiniteCount = 0;
		for (int i = 0; i < vals.length; i++) {
		    if (Double.isNaN(vals[i])) {
			_nullCount++;
		    }
		    if (Double.isInfinite(vals[i])) {
			_infiniteCount++;
		    }
		}
	    }
	    // distinct
	    if (!isDistinctCountSet()) {
		_distinctCount = vals.length - _nullCount > 0 ? 1 : 0;
		for (int i = 1; i < vals.length; i++) {
		    if (vals[i] != vals[i - 1])
			_distinctCount++;
		}
	    }
	    // median and quartiles
	    // What about NaN INFINITY? Should this exclude NaN INFINITY?
	    if (!isMedianSet() && vals.length > 0) {
		double[] vv = vals;
		if (_nullCount > 0 || _infiniteCount > 0) {
		    vv = new double[vals.length - _nullCount - _infiniteCount];
		    for (int i = 0, j = 0; i < vals.length && j < vv.length; i++) {
			if (!Double.isNaN(vals[i])
				&& !Double.isInfinite(vals[i])) {
			    vv[j++] = vals[i];
			}
		    }
		}
		if (vv.length > 0) {
		    if (vv.length % 2 == 0) {
			_median = (vv[vv.length / 2 - 1] + vv[vv.length / 2]) / 2.;
			if (vv.length < 4) {
			    _q1 = (vv[0] + _median) / .2;
			    _q3 = _median + (vv[vv.length - 1] - _median) / .2;
			} else if (vv.length / 2 % 2 == 0) {
			    _q1 = (vv[vv.length / 4 - 1] + vv[vv.length / 4]) / 2.;
			    _q3 = (vv[vv.length * 3 / 4 - 1] + vv[vv.length * 3 / 4]) / 2.;
			} else {
			    _q1 = vv[vv.length / 4];
			    _q3 = vv[vv.length * 3 / 4];
			}
		    } else {
			_median = vv[vv.length / 2];
			if (vv.length < 4) {
			    _q1 = (vv[0] + _median) / .2;
			    _q3 = _median + (vv[vv.length - 1] - _median) / .2;
			} else if (vv.length / 2 % 2 == 0) {
			    _q1 = (vv[vv.length / 4 - 1] + vv[vv.length / 4]) / 2.;
			    _q3 = (vv[vv.length * 3 / 4 - 1] + vv[vv.length * 3 / 4]) / 2.;
			} else {
			    _q1 = vv[vv.length / 4];
			    _q3 = vv[vv.length * 3 / 4];
			}
		    }
		}
	    }
	}
	// avg
	if (!isAvgSet()) {
	    _avg = 0.;
	    int n = vals.length - _nullCount;
	    for (int i = 0; i < vals.length; i++) {
		if (!Double.isNaN(vals[i]) && !Double.isInfinite(vals[i])) {
		    _avg += vals[i] / n;
		}
	    }
	}
	if (!isVarianceSet()) {
	    // variance
	    _variance = 0.;
	    _stdDev = 0.;
	    double n1 = vals.length - _nullCount - 1;
	    if (n1 > 0) {
		for (int i = 0; i < vals.length; i++) {
		    if (!Double.isNaN(vals[i]) && !Double.isInfinite(vals[i])) {
			_variance += Math.pow(vals[i] - _avg, 2.) / n1;
		    }
		}
	    }
	    // stdDev
	    _stdDev = Math.sqrt(_variance);
	}
	setNullCount(_nullCount);
	setInfiniteCount(_infiniteCount);
	setDistinctCount(_distinctCount);
	setMedian(_median);
	setQ1(_q1);
	setQ3(_q3);
	setAvg(_avg);
	setVariance(_variance);
	setStdDev(_stdDev);
    }

    /** 
   *  
   */
    // Now this has reference to the other CellMap,
    // should this use WeakHashMap or Hashtable?
    WeakHashMap covariances = new WeakHashMap();

    /**
     * Set the covariance between this column and the given column.
     * 
     * @param map
     *            the other column
     * @param covariance
     *            the covariance value for the pair of columns.
     */
    public void setCovariance(CellMap map, double covariance) {
	covariances.put(map, new Double(covariance));
    }

    /**
     * Get the covariance between this column and the given column.
     * 
     * @param map
     *            the other column
     * @return the covariance value for the pair of columns.
     */
    public double covariance(CellMap map) {
	double covariance = Double.NaN;
	Double covObj = (Double) covariances.get(map);
	if (covObj == null) {
	    covariance = covariance(this, map);
	    this.setCovariance(map, covariance);
	    map.setCovariance(this, covariance);
	} else {
	    covariance = covObj.doubleValue();
	}
	return covariance;
    }

    /**
     * Get the correlation between this column and the given column.
     * 
     * @param map
     *            the other column
     * @return the correlation value for the pair of columns.
     */
    public double correlation(CellMap map) {
	return covariance(map) / (getStdDev() * map.getStdDev());
    }

    /**
     * For each value in the column return its distance from the mean in
     * standard deviations.
     * 
     * @return an array with the standard distance from the mean for each value.
     */
    public double[] stdvals() {
	double vals[] = null;
	double dval[] = getMapValues();
	int cnt = dval.length;
	double mean = getAvg();
	double stdDev = getStdDev();
	if (stdDev != 0 && !Double.isNaN(mean) && !Double.isNaN(stdDev)) {
	    vals = new double[cnt];
	    if (stdDev == 0) {
		Arrays.fill(vals, 0.);
	    } else {
		for (int i = 0; i < vals.length; i++) {
		    if (!Double.isNaN(dval[i])) {
			vals[i] = (dval[i] - mean) / stdDev;
		    } else {
			vals[i] = Double.NaN;
		    }
		}
	    }
	}
	return vals;
    }

    /**
     * Get the correlation between the given columns.
     * 
     * @param xmap
     *            a column
     * @param ymap
     *            the other column
     * @return the correlation value for the pair of columns.
     */
    public static double correlation(CellMap xmap, CellMap ymap) {
	// correlation
	// r = (1/(n-1)) * sum( ((x[i] - mean) / stdvals[i])*((y[i] - mean) /
	// stdvals[i]))
	// covariance(xmap, ymap) / (xmap.getStdDev() * ymap.getStdDev())
	double cov = xmap.covariance(ymap);
	return cov / (xmap.getStdDev() * ymap.getStdDev());
    }

    /**
     * Get the covariance between the given columns.
     * 
     * @param xmap
     *            a column
     * @param ymap
     *            the other column
     * @return the covariance value for the pair of columns.
     */
    public static double covariance(CellMap xmap, CellMap ymap) {
	double cov = Double.NaN;
	double xd[] = xmap.getMapValues();
	double yd[] = ymap.getMapValues();
	if (xd != null && yd != null && xd.length > 0 && xd.length == yd.length) {
	    double sumx = xd[0], sumy = yd[0], Sxy = 0;
	    for (int i = 1; i < xd.length; i++) {
		double x = xd[i];
		double y = yd[i];
		sumx += x;
		Sxy += (x - sumx / (i + 1)) * (y - sumy / i);
		sumy += y;
	    }
	    cov = Sxy / xd.length;
	    if (xd.length > 1) {
		cov *= xd.length / (xd.length - 1.);
	    }
	    // System.err.println(xmap + " " + ymap + " " + cov);
	}
	return cov;
    }

    /**
     * Get the regression line for the correlation of this column to the given
     * column.
     * 
     * @param ymap
     *            the other column
     * @return the regression line for the pair of columns.
     */
    public LineFormula regressionLine(CellMap ymap) {
	return regressionLine(this, ymap);
    }

    /**
     * Get the regression line for the correlation of one column to the other
     * column.
     * 
     * @param xmap
     *            a column
     * @param ymap
     *            the other column
     * @return the regression line for the pair of columns.
     */
    public static LineFormula regressionLine(CellMap xmap, CellMap ymap) {
	// least squares regression
	// Returns line class that returns a y val for a given x
	double meanx = xmap.getAvg();
	double meany = ymap.getAvg();
	double sx = xmap.getStdDev();
	double sy = ymap.getStdDev();
	double r = xmap.correlation(ymap);
	double m = r * sy / sx;
	double i = meany - m * meanx;
	return new LineFormula(m, i);
    }

    /*
     * public static Vector residuals(Vector xcells, Vector ycells) { return
     * residuals(xcells, ycells, regressionLine(xcells, ycells)); }
     * 
     * public static Vector residuals(Vector xcells, Vector ycells, LineFormula
     * regressionLine) { if (xcells == null || ycells == null || regressionLine
     * == null) return null; Vector r = new Vector(xcells.size()); for (int i =
     * 0; i < xcells.size(); i++) { double rv = Double.NaN; Object xo =
     * xcells.elementAt(i); if (xo != null && xo instanceof Number) { double x =
     * ((Number)xo).doubleValue(); if (!Double.isNaN(x)) { Object yo =
     * ycells.elementAt(i); if (yo != null && yo instanceof Number) { double y =
     * ((Number)yo).doubleValue(); double ry = regressionLine.getY(x); rv = y -
     * ry; } } } r.addElement(new Double(rv)); } return r; }
     */

    public PartitionIndexMap getRegexPartition(String[] regex)
	    throws NullPointerException {
	return getRegexPartition(this, regex);
    }

    public static PartitionIndexMap getRegexPartition(ColumnMap cmap,
	    String[] regex) throws NullPointerException {
	if (cmap == null) {
	    throw new NullPointerException("No ColumnMap given.");
	}
	if (regex == null) {
	    throw new NullPointerException("No Regular Expressions given.");
	}
	int[] pmap = new int[cmap.getTableModel().getRowCount()];
	for (int ri = 0; ri < pmap.length; ri++) {
	    pmap[ri] = regex.length; // Other
	    Object obj = cmap.getValueAt(ri);
	    if (obj != null) {
		for (int i = 0; i < regex.length; i++) {
		    if (regex[i] != null) {
			if (obj.toString().matches(regex[i])) {
			    pmap[ri] = i;
			    break;
			}
		    }
		}
	    } else {
		for (int i = 0; i < regex.length; i++) {
		    if (regex[i] == null) {
			pmap[ri] = i;
			break;
		    }
		}
	    }
	}
	return new PartitionIndexMap(pmap);
    }

    public PartitionIndexMap getDatePartition(int calendarField)
	    throws NullPointerException, IllegalArgumentException {
	return getDatePartition(this, calendarField);
    }

    public static PartitionIndexMap getDatePartition(ColumnMap cmap,
	    int calendarField) {
	return getDatePartition(cmap, calendarField, 0);
    }

    public static PartitionIndexMap getDatePartition(ColumnMap cmap,
	    int calendarField, int modulus) throws NullPointerException,
	    IllegalArgumentException {
	if (cmap == null) {
	    throw new NullPointerException("ColumnMap");
	}
	if (cmap.getColumnClass() != null
		&& !java.util.Date.class
			.isAssignableFrom(cmap.getColumnClass())) {
	    throw new IllegalArgumentException(
		    "ColumnMap is not an instance of java.util.Date");
	}
	int calFld = calendarField;
	switch (calendarField) {
	case Calendar.AM_PM:
	case Calendar.DAY_OF_MONTH: // Calendar.DATE:
	case Calendar.DAY_OF_WEEK:
	case Calendar.DAY_OF_YEAR:
	case Calendar.DAY_OF_WEEK_IN_MONTH:
	case Calendar.MONTH:
	case Calendar.WEEK_OF_MONTH:
	case Calendar.WEEK_OF_YEAR:
	case Calendar.HOUR:
	case Calendar.HOUR_OF_DAY:
	case Calendar.MINUTE:
	case Calendar.SECOND:
	case Calendar.MILLISECOND:
	    break;
	case Calendar.YEAR:
	    // restrict to actual range of years in data
	case Calendar.ERA:
	default:
	    throw new IllegalArgumentException(
		    "ColumnMap is not an instance of java.util.Date");
	}
	int[] pmap = new int[cmap.getTableModel().getRowCount()];
	GregorianCalendar cal = new GregorianCalendar();
	for (int ri = 0; ri < pmap.length; ri++) {
	    Object obj = cmap.getValueAt(ri);
	    int val = -1;
	    if (obj != null && obj instanceof java.util.Date) {
		try {
		    cal.setTime((Date) obj);
		    val = cal.get(calFld);
		} catch (Exception ex) {
		    System.err.println("getDatePartition(" + obj + ") " + ex);
		}
		if (calFld == Calendar.DAY_OF_WEEK && pmap[ri] == 0) {
		    System.err.println(ri + " getDatePartition(" + obj + ") "
			    + cal.getTime() + "\t" + cal.get(calFld));
		}
	    } else {
		val = cal.getMaximum(calFld) + 1;
	    }
	    pmap[ri] = modulus > 0 ? val % modulus : val;
	}
	return new PartitionIndexMap(pmap);
    }

    /**
     * Add a listener to this map.
     * 
     * @param l
     *            the listener to add.
     */
    public void addCellMapListener(CellMapListener l) {
	EventListener[] listeners = listenerList
		.getListeners(CellMapListener.class);
	for (int i = 0; i < listeners.length; i++) {
	    if (listeners[i] == l)
		return;
	}
	listenerList.add(CellMapListener.class, l);
    }

    /**
     * Remove a listener from this map.
     * 
     * @param l
     *            the listener to remove.
     */
    public void removeCellMapListener(CellMapListener l) {
	listenerList.remove(CellMapListener.class, l);
    }

    /**
     * Notify listeners of a change in state for this map.
     * 
     * @param mapState
     *            the mapState of this map.
     */
    protected void fireColumnMapChanged(int mapState) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	CellMapEvent columnMapEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == CellMapListener.class) {
		// Lazily create the event:
		if (columnMapEvent == null)
		    columnMapEvent = new CellMapEvent(this, mapState);
		((CellMapListener) listeners[i + 1])
			.cellMapChanged(columnMapEvent);
	    }
	}
    }
}
