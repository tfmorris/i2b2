/*
 * @(#) $RCSfile: BaseColumnMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.text.DecimalFormat;
import java.text.ParsePosition;
import javax.swing.table.*;

/**
 * BaseColumnMap maps the values of a TableModel column to a numeric range. If
 * Class type of the column is Number, or if all of the values of the column are
 * Strings that can be parsed as class Number, the range will be from the
 * minimum to the maximun of the values in the column. If any value can not be
 * parsed as class Number, the values will be mapped to integral values from 0
 * to the number of distinct values - 1, (distinct values are determined by the
 * value's equals method.) The BaseColumnMap can also be used to select a range
 * of mapped values from the column and indicate the selected rows in a
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
@SuppressWarnings("serial")
public class BaseColumnMap extends AbstractColumnMap implements Serializable {
    class Elem { // class to hold info in colHash
	int index = 0; // ordinal for distinct values in this column
	int count = 0; // count of rows that have this value

	Elem(int index) {
	    this.index = index;
	}

	@Override
	public String toString() {
	    return this + "[index=" + index + ",count=" + count + "]";
	}
    }

    // Non Number or Date Values - Char VarChar
    Hashtable colHash = null; // map cell value Object to an Elem Object
    Vector objList = null; // sequential list of distinct cell value Objects
    // This should probably be Object[] and String[]
    OneToOneIndexMap sortIndex = null; // alternative sorting for Objects

    double dvals[] = null;
    boolean remap = false;
    boolean recalc = true;

    /**
     * Map the rows in the given column of the table to numeric values.
     * 
     * @param tableModel
     *            the table containing the column of values
     * @param column
     *            the index of the column in the TableModel
     */
    public BaseColumnMap(TableModel tableModel, int column) {
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
    public BaseColumnMap(TableModel tableModel, int column, boolean continuous) {
	super(tableModel, column, continuous);
    }

    protected void collectStats() {
	Hashtable colH = colHash;
	if (isBoolean()) { // discreet values
	    setMin(FALSE_VALUE);
	    setMax(TRUE_VALUE);
	    setInfiniteCount(0);
	    setDistinctCount(2);
	    setMedian(Double.NaN);
	    setQ1(Double.NaN);
	    setQ3(Double.NaN);
	    setAvg(Double.NaN);
	    setVariance(Double.NaN);
	    setStdDev(Double.NaN);
	    int _nullCnt = 0;
	    int nrow = getCount();
	    for (int r = 0; r < nrow; r++) {
		if (Double.isNaN(getMapValue(r))) {
		    _nullCnt++;
		}
	    }
	    setNullCount(_nullCnt);
	} else if (colH != null) { // discreet values
	    collectStats(colH);
	} else {
	    collectStats(getMapValues());
	}
    }

    protected void collectStats(Hashtable colH) {
	if (colH == null) { // discreet values
	    return;
	}
	int nrow = getCount();
	if (nrow < 1) {
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
	// num null
	Elem elem = (Elem) colH.get(nullTag);
	_nullCount = elem != null ? elem.count : 0;
	// distinct
	_distinctCount = objList != null ? objList.size() : 0;
	// median and quartiles
	double _mv = (nrow - 1) / 2.;
	double _qv1 = (Math.floor(_mv) - 1) / 2.;
	double _qv3 = nrow - 1 - _q1;
	for (int i = 0, c = 0, nc = 0; i < objList.size(); i++, c = nc) {
	    elem = (Elem) colH.get(objList.get(sortIndex != null ? sortIndex
		    .getSrc(i) : i));
	    nc = c + elem.count;
	    if (c < _mv && nc + 1 > _mv) {
		if (nc > _mv)
		    _median = elem.index;
		else
		    _median = elem.index + .5;
	    }
	    if (c < _qv1 && nc + 1 > _qv1) {
		if (nc > _qv1)
		    _q1 = elem.index;
		else
		    _q1 = elem.index + .5;
	    }
	    if (c < _qv3 && nc + 1 > _qv3) {
		if (nc > _qv3)
		    _q3 = elem.index;
		else
		    _q3 = elem.index + .5;
	    }
	}
	// avg
	_avg = 0.;
	for (int i = 0; i < objList.size(); i++) {
	    elem = (Elem) colH.get(objList.get(i));
	    _avg += elem.index * elem.count / nrow;
	}
	int n1 = nrow - 1;
	// variance
	for (int i = 0; i < objList.size(); i++) {
	    elem = (Elem) colH.get(objList.get(i));
	    _variance += (Math.pow(elem.index, 2.) / n1) * elem.count;
	}
	// stdDev
	_stdDev = 0.;
	if (!Double.isNaN(variance) && !Double.isInfinite(variance)) {
	    _stdDev = Math.sqrt(variance);
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
     * Return the mapped value for the given table model row. return the mapped
     * value of each row element in the column.
     */
    @Override
    public double[] getMapValues() {
	Hashtable colH = colHash;
	if (colH != null) { // discreet values
	    if (dvals == null) {
		dvals = new double[getCount()];
		for (int r = 0; r < dvals.length; r++) {
		    dvals[r] = getMapValue(r);
		}
	    }
	}
	double[] vals = new double[getCount()];
	if (dvals != null) {
	    System.arraycopy(dvals, 0, vals, 0, dvals.length);
	} else {
	    for (int r = 0; r < vals.length; r++) {
		vals[r] = getMapValue(r);
	    }
	}
	return vals;
    }

    /**
     * Return the mapped value for the given table model row.
     * 
     * @param row
     *            the row in the table return the mapped value of the row
     */
    @Override
    public double getMapValue(int row) {
	try {
	    if (row < 0 || row >= getCount())
		return NULL_VALUE;
	    if (getState() != CellMap.MAPPED)
		return NULL_VALUE;
	    if (isBoolean()) {
		Object obj = tm.getValueAt(row, colIndex);
		if (obj == null) {
		    return NULL_VALUE;
		} else if (obj instanceof Boolean) {
		    return ((Boolean) obj).booleanValue() ? TRUE_VALUE : 0;
		} else {
		    String s = obj.toString();
		    return s.equalsIgnoreCase("true") ? TRUE_VALUE : s
			    .equalsIgnoreCase("false") ? FALSE_VALUE
			    : NULL_VALUE;
		}
	    }
	    Hashtable colH = colHash;
	    if (colH != null) { // discreet values
		Object obj = tm.getValueAt(row, colIndex);
		if (obj == null) {
		    obj = nullTag;
		}
		try {
		    int i = ((Elem) colH.get(obj)).index;
		    if (sortIndex != null) {
			i = sortIndex.getDst(i);
		    }
		    if (i >= 0) {
			return i;
		    }
		} catch (Exception ex) {
		    System.err.println("err at (" + row + "," + colIndex
			    + ")  of " + tm.getRowCount() + " map " + obj
			    + " to " + colH.get(obj) + "  mappingstate: "
			    + getState());
		    ex.printStackTrace();
		    return NULL_VALUE;
		}
	    } else { // a range of numeric values
		if (dvals != null) {
		    if (row < dvals.length)
			return dvals[row];
		    return NULL_VALUE;
		}
		try {
		    return ((Number) tm.getValueAt(row, colIndex))
			    .doubleValue();
		} catch (Throwable ex) {
		    System.err.println("getMapValue ex= " + ex);
		}
		return NULL_VALUE;
	    }
	} catch (Throwable t) {
	    System.err.println("getMapValue t= " + t);
	    t.printStackTrace();
	}
	return NULL_VALUE;
    }

    /**
     * Return the element that is mapped nearest to the the mapValue in the
     * given direction.
     * 
     * @param mapValue
     *            the relative position on the map
     * @param dir
     *            negative means round down, positive mean round up, 0 rounds to
     *            closest.. return the element that is mapped nearest to the the
     *            mapValue.
     */
    @Override
    public Object getMappedValue(double mapValue, int dir) {
	Hashtable colH = colHash;
	if (isBoolean) { // boolean values
	    return Math.abs(FALSE_VALUE - mapValue) < Math.abs(TRUE_VALUE
		    - mapValue) ? new Boolean(false) : new Boolean(true);
	} else if (colH != null) { // discreet values
	    if (objList != null && objList.size() > 0) {
		int i = -1;
		if (!Double.isNaN(mapValue)) {
		    i = dir < 0 ? (int) Math.floor(mapValue)
			    : dir > 0 ? (int) Math.ceil(mapValue) : (int) Math
				    .round(mapValue);
		}
		if (i < 0) {
		    i = 0;
		} else if (i >= objList.size()) {
		    i = objList.size() - 1;
		}
		if (sortIndex != null) {
		    i = sortIndex.getSrc(i);
		    if (i < 0) {
			i = 0;
		    } else if (i >= objList.size()) {
			i = objList.size() - 1;
		    }
		}
		Object obj = objList.get(i);
		return obj != nullTag ? obj : null;
	    }
	} else { // a range of numeric values
	    if (isNumber) {
		DecimalFormat df = new DecimalFormat();
		ParsePosition pp = new ParsePosition(0);
		Number n = df.parse((new Double(mapValue)).toString(), pp);
		return n;
	    } else if (isDate) {
		return new Date((long) mapValue);
	    }
	}
	return null;
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
    @Override
    public int[] getCounts(int buckets, boolean withSelectCounts) {
	if (colIndex >= tm.getColumnCount()) {
	    return new int[0];
	}
	boolean selectCount = withSelectCounts && lsm != null;
	int offset = selectCount ? 2 : 1;
	int b[] = null;
	Hashtable colH = colHash;
	if (isBoolean) {
	    int len = 2 * offset;
	    b = new int[len];
	    int nrows = tm.getRowCount();
	    for (int r = 0; r < nrows; r++) {
		double val = getMapValue(r);
		if (Double.isNaN(val) || Double.isInfinite(val))
		    continue;
		int i = (int) val;
		i = i < 0 ? 0 : i > 1 ? 1 : i;
		b[i * offset]++;
		if (selectCount) {
		    if (lsm.isSelectedIndex(r)) {
			b[i * offset + 1]++;
		    }
		}
	    }
	} else if (colH != null) {
	    if (buckets < 1) {
		int len = colH.size() * offset;
		b = new int[len];
		for (Enumeration e = colH.elements(); e.hasMoreElements();) {
		    Elem el = (Elem) e.nextElement();
		    int i = offset
			    * (sortIndex != null ? sortIndex.getDst(el.index)
				    : el.index);
		    b[i] = el.count;
		}
		if (selectCount) {
		    int nrows = tm.getRowCount();
		    for (int r = 0; r < nrows; r++) {
			if (lsm.isSelectedIndex(r)) {
			    Object obj = getValueAt(r);
			    if (obj != null) {
				int i = offset
					* (sortIndex != null ? sortIndex
						.getDst(((Elem) colH.get(obj)).index)
						: ((Elem) colH.get(obj)).index);
				b[i + 1]++;
			    }
			}
		    }
		}
	    } else {
		// not implemented yet, so default to:
		return getCounts(0, withSelectCounts);
	    }
	} else {
	    double minval = getMin();
	    double maxval = getMax();
	    int len = buckets > 0 ? buckets : 10;
	    double incr = (maxval - minval) / len;
	    b = new int[len * offset];
	    int nrows = dvals != null ? dvals.length : tm.getRowCount();
	    double val = NULL_VALUE;
	    for (int r = 0; r < nrows; r++) {
		if (dvals != null) {
		    val = dvals[r];
		} else {
		    Object o = tm.getValueAt(r, colIndex);
		    if (o instanceof Number) {
			val = ((Number) o).doubleValue();
		    } else {
			val = Double.NaN;
		    }
		}
		if (Double.isNaN(val) || Double.isInfinite(val))
		    continue;
		int i = (int) ((val - minval) / incr);
		if (i >= buckets)
		    i = buckets - 1;
		if (i < 0)
		    i = 0;
		b[i * offset]++;
		if (selectCount) {
		    if (lsm.isSelectedIndex(r)) {
			b[i * offset + 1]++;
		    }
		}
	    }
	}
	return b;
    }

    protected synchronized int mapObject(Object val) {
	Hashtable colH = colHash;
	Vector objL = objList;
	if (colH == null || objL == null) {
	    colH = new Hashtable();
	    objL = new Vector();
	    colHash = colH;
	    objList = objL;
	    setMin(0.);
	    setMax(0.);
	}
	if (val == null) {
	    val = nullTag;
	}
	Elem el = (Elem) colH.get(val);
	if (el == null) {
	    int idx = objL.size();
	    if (getMax() < idx) {
		setMax(idx);
	    }
	    el = new Elem(idx);
	    objL.add(val);
	    colH.put(val, el);
	    if (distinctCount < ++idx) {
		distinctCount = idx;
	    }
	}
	el.count++;
	return el.index;
    }

    /**
     * If the given column of elements in the table is not numeric, map the
     * objects to integral values from 0 to 1 less than the number of distinct
     * elements in the column(as determined by the equals method of the object
     * class of the column.)
     */
    protected synchronized void mapObjects(int from, int to) {
	int nrows = tm.getRowCount();
	if (nrows < from) {
	    return;
	}
	for (int r = from; r <= to; r++) {
	    if (Thread.currentThread().isInterrupted()) {
		return;
	    }
	    Object val = tm.getValueAt(r, colIndex);
	    int idx = mapObject(val);
	}
	// sort the values
	if (Thread.currentThread().isInterrupted()) {
	    return;
	}
	sortColumn();
    }

    /**
     * Sort the value of the column according to the SortOrder set.
     * 
     * @see CacheColumnMap#setSortOrder
     */
    void sortColumn() {
	if (objList == null || objList.size() < 1)
	    return;
	setState(CellMap.MAPPING);
	// sort the values
	if (sortby != ROWORDERSORT) {
	    switch (sortby) {
	    case ALPHANUMSORT:
		sortIndex = new OneToOneIndexMap(Cells.getSortIndex(objList,
			Cells.alphaNumericComparator));
		break;
	    default:
		sortIndex = new OneToOneIndexMap(Cells.getSortIndex(objList));
		break;
	    }
	    if (!true) {
		for (int i = 0; i < objList.size(); i++) {
		    System.err.println(i + "\t" + objList.get(i) + "\t"
			    + sortIndex.getSrc(i) + "\t" + sortIndex.getDst(i));
		}
	    }
	} else {
	    sortIndex = null;
	}
	setState(CellMap.MAPPED);
    }

    @Override
    public void cleanUp() {
	setState(CellMap.INVALID);
	objList = null;
	colHash = null;
	dvals = null;
	tm = null;
	lsm = null;
    }

}
