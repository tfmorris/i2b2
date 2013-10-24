/*
 * @(#) $RCSfile: CacheColumnMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;
import java.lang.reflect.*;
import javax.swing.table.TableModel;
import javax.swing.ListSelectionModel;

/**
 * This ColumnMap is designed to be used as a data cache for a column in a
 * TableModel. It stores numerical and Date data in primitive arrays.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
@SuppressWarnings("serial")
public class CacheColumnMap extends BaseColumnMap {
    int bufIncr = 1000;

    int floatCnt = 0; // The count of numbers with floating point remainders

    BitSet nullCells = null;

    // Array of Number primitives, Date as long, index for Char types
    Object data = null;

    // Potential cached store
    // data - easiest with Numbers and Dates
    // ?? mapped file in j2se1.4
    // OR
    // data -> dataseg[0] -> SoftReference -> prim[]
    // +-> dataseg[1] -> SoftReference -> prim[]
    // +-> dataseg[2] -> SoftReference -> prim[]
    // lastdata -> prim[] to keep last accessed from being collected
    // writedata -> prim[] to keep buffer currently being written from being
    // collected
    //
    // colHash
    // objList
    // sortIndex
    //
    Object minObj;

    Object maxObj;

    public Object getMinObj() {
	return minObj;
    }

    public void setMinObj(Object minObj) {
	this.minObj = minObj;
    }

    public Object getMaxObj() {
	return maxObj;
    }

    public void setMaxObj(Object maxObj) {
	this.maxObj = maxObj;
    }

    public CacheColumnMap(TableModel tm, ListSelectionModel lsm, String name,
	    int colIndex, Class colClass) {
	super(tm, colIndex);
	this.lsm = lsm;
	this.name = name;
	this.colClass = colClass;
	if (colClass != null) {
	    if (java.lang.Number.class.isAssignableFrom(colClass)) {
		isNumber = true;
	    } else if (java.util.Date.class.isAssignableFrom(colClass)) {
		isDate = true;
	    } else if (java.lang.Boolean.class.isAssignableFrom(colClass)) {
		isBoolean = true;
	    }
	    createDataArray(colClass);
	    colTyped = true;
	}
    }

    // Inherit javadoc
    @Override
    public Class getColumnClass() {
	if (colClass != null && colClass == java.lang.Number.class
		&& data != null) {
	    if (data instanceof BitSet) {
		return Boolean.class;
	    }
	    Class dtype = data.getClass().getComponentType();
	    if (java.lang.Double.TYPE == dtype) {
		return java.lang.Double.class;
	    } else if (java.lang.Long.TYPE == dtype) {
		return java.lang.Long.class;
	    } else if (java.lang.Integer.TYPE == dtype) {
		return java.lang.Integer.class;
	    } else if (java.lang.Float.TYPE == dtype) {
		return java.lang.Float.class;
	    } else if (java.lang.Short.TYPE == dtype) {
		return java.lang.Short.class;
	    } else if (java.lang.Byte.TYPE == dtype) {
		return java.lang.Byte.class;
	    }
	}
	return super.getColumnClass();
    }

    // CellMap

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
	if (data != null && data instanceof BitSet) {
	    return Math.abs(FALSE_VALUE - mapValue) < Math.abs(TRUE_VALUE
		    - mapValue) ? new Boolean(false) : new Boolean(true);
	} else if (isNumber) {
	    if (data == null) {
		return new Double(mapValue);
	    } else if (data.getClass().getComponentType() == java.lang.Double.TYPE) {
		return new Double(mapValue);
	    } else if (data.getClass().getComponentType() == java.lang.Long.TYPE) {
		return new Long((long) (dir < 0 ? Math.floor(mapValue)
			: dir > 0 ? Math.ceil(mapValue) : mapValue));
	    } else if (data.getClass().getComponentType() == java.lang.Integer.TYPE) {
		return new Integer((int) (dir < 0 ? Math.floor(mapValue)
			: dir > 0 ? Math.ceil(mapValue) : mapValue));
	    } else if (data.getClass().getComponentType() == java.lang.Float.TYPE) {
		return new Float((float) mapValue);
	    } else if (data.getClass().getComponentType() == java.lang.Short.TYPE) {
		return new Short((short) (dir < 0 ? Math.floor(mapValue)
			: dir > 0 ? Math.ceil(mapValue) : mapValue));
	    } else if (data.getClass().getComponentType() == java.lang.Byte.TYPE) {
		return new Byte((byte) (dir < 0 ? Math.floor(mapValue)
			: dir > 0 ? Math.ceil(mapValue) : mapValue));
	    }
	} else if (isDate) {
	    return new Date((long) mapValue);
	} else {
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
	}
	return null;
    }

    /**
     * Return the mapped value for the given table model row.
     * 
     * @param rowIndex
     *            the row in the table return the mapped value of the row
     */
    @Override
    public double getMapValue(int rowIndex) {
	if (rowIndex < 0)
	    return NULL_VALUE;

	if (data != null && data instanceof BitSet) {
	    Object obj = getValueAt(rowIndex);
	    return obj instanceof Boolean ? ((Boolean) obj).booleanValue() == true ? TRUE_VALUE
		    : FALSE_VALUE
		    : NULL_VALUE;
	} else if (isNumber) {
	    if (data == null || rowIndex >= Array.getLength(data)) {
		Object obj = getValueAt(rowIndex);
		if (obj != null && obj instanceof Number) {
		    return ((Number) getValueAt(rowIndex)).doubleValue();
		}
		return NULL_VALUE;
	    } else if (data.getClass().getComponentType() == java.lang.Double.TYPE) {
		return (((double[]) data)[rowIndex]);
	    } else if (data.getClass().getComponentType() == java.lang.Long.TYPE) {
		return (((long[]) data)[rowIndex]);
	    } else if (data.getClass().getComponentType() == java.lang.Integer.TYPE) {
		return (((int[]) data)[rowIndex]);
	    } else if (data.getClass().getComponentType() == java.lang.Float.TYPE) {
		return (((float[]) data)[rowIndex]);
	    } else if (data.getClass().getComponentType() == java.lang.Short.TYPE) {
		return (((short[]) data)[rowIndex]);
	    } else if (data.getClass().getComponentType() == java.lang.Byte.TYPE) {
		return (((byte[]) data)[rowIndex]);
	    }
	} else if (isDate) {
	    if (data == null || rowIndex >= Array.getLength(data)) {
		Object obj = getValueAt(rowIndex);
		if (obj != null && obj instanceof Number) {
		    return ((Number) getValueAt(rowIndex)).doubleValue();
		}
		return NULL_VALUE;
	    }
	    return ((long[]) data)[rowIndex] != Long.MAX_VALUE ? (double) (((long[]) data)[rowIndex])
		    : NULL_VALUE;
	} else {
	    int i = getMappedIndex(rowIndex);
	    if (sortIndex != null) {
		// System.err.println( " gmv " + rowIndex +"\t" + i
		// +"\t"+sortIndex.getDst(i));
		i = sortIndex.getDst(i);
	    }
	    if (i >= 0) {
		return i;
	    }
	}
	return NULL_VALUE;
    }

    /**
     * Return the mapped value for the given table model row. return the mapped
     * value of each row element in the column.
     */
    @Override
    public double[] getMapValues() {
	double dm[] = null;
	if (data != null) {
	    Object da = data;
	    int nrow = getCount();
	    if (data instanceof BitSet) {
		BitSet bs = (BitSet) data;
		dm = new double[nrow];
		if (nullCells != null) {
		    for (int i = 0; i < dm.length; i++)
			dm[i] = nullCells.get(i) ? NULL_VALUE
				: bs.get(i) ? TRUE_VALUE : FALSE_VALUE;
		} else {
		    for (int i = 0; i < dm.length; i++)
			dm[i] = bs.get(i) ? TRUE_VALUE : FALSE_VALUE;
		}
	    } else if (isNumber) {
		if (data.getClass().getComponentType() == java.lang.Double.TYPE) {
		    dm = new double[Math.min(nrow, ((double[]) da).length)];
		    System.arraycopy(da, 0, dm, 0, dm.length);
		} else if (data.getClass().getComponentType() == java.lang.Float.TYPE) {
		    dm = new double[Math.min(nrow, ((float[]) da).length)];
		    float fa[] = (float[]) data;
		    for (int i = 0; i < dm.length; i++)
			dm[i] = fa[i];
		} else if (data.getClass().getComponentType() == java.lang.Long.TYPE) {
		    dm = new double[Math.min(nrow, ((long[]) da).length)];
		    long la[] = (long[]) data;
		    if (nullCells != null) {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = nullCells.get(i) ? NULL_VALUE : la[i];
		    } else {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = la[i];
		    }
		} else if (data.getClass().getComponentType() == java.lang.Integer.TYPE) {
		    dm = new double[Math.min(nrow, ((int[]) da).length)];
		    int ia[] = (int[]) data;
		    if (nullCells != null) {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = nullCells.get(i) ? NULL_VALUE : ia[i];
		    } else {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = ia[i];
		    }
		} else if (data.getClass().getComponentType() == java.lang.Short.TYPE) {
		    dm = new double[Math.min(nrow, ((short[]) da).length)];
		    short sa[] = (short[]) data;
		    if (nullCells != null) {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = nullCells.get(i) ? NULL_VALUE : sa[i];
		    } else {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = sa[i];
		    }
		} else if (data.getClass().getComponentType() == java.lang.Byte.TYPE) {
		    dm = new double[Math.min(nrow, ((byte[]) da).length)];
		    byte ba[] = (byte[]) data;
		    if (nullCells != null) {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = nullCells.get(i) ? NULL_VALUE : ba[i];
		    } else {
			for (int i = 0; i < dm.length; i++)
			    dm[i] = ba[i];
		    }
		}
	    } else if (isDate) {
		dm = new double[Math.min(nrow, ((long[]) da).length)];
		long la[] = (long[]) data;
		for (int i = 0; i < dm.length; i++)
		    dm[i] = la[i] != Long.MAX_VALUE ? la[i] : NULL_VALUE;
	    } else {
		dm = new double[Math.min(nrow, ((int[]) da).length)];
		if (sortIndex != null) {
		    for (int i = 0; i < dm.length; i++) {
			dm[i] = sortIndex.getDst(getMappedIndex(i));
		    }
		} else {
		    for (int i = 0; i < dm.length; i++) {
			dm[i] = getMappedIndex(i);
		    }
		}
	    }
	}
	return dm;
    }

    public int getMappedIndex(int rowIndex) {
	if (rowIndex >= 0 && data != null
		&& java.lang.reflect.Array.getLength(data) > rowIndex) {
	    int i = java.lang.reflect.Array.getInt(data, rowIndex);
	    return i;
	}
	return -1;
    }

    public Object getMappedObject(int rowIndex) {
	if (objList != null) {
	    int i = getMappedIndex(rowIndex);
	    if (i >= 0 && i < objList.size()) {
		return objList.get(i);
	    }
	}
	return null;
    }

    @Override
    public Object getValueAt(int rowIndex) {
	if (data != null) {
	    if (data instanceof BitSet) {
		if (nullCells != null && nullCells.get(rowIndex)) {
		    return null;
		}
		return rowIndex >= 0 ? new Boolean(((BitSet) data)
			.get(rowIndex)) : null;
	    } else if (isNumber) {
		if (nullCells != null && nullCells.get(rowIndex)) {
		    return null;
		}
		return Array.getLength(data) > rowIndex ? Array.get(data,
			rowIndex) : null;
	    } else if (isDate) {
		return ((long[]) data).length > rowIndex
			&& ((long[]) data)[rowIndex] != Long.MAX_VALUE ? new Date(
			((long[]) data)[rowIndex])
			: null;
	    }
	    // Object
	    return getMappedObject(rowIndex);
	}
	return null;
    }

    /**
	 */

    protected void checkArraySize(int size) {
	data = checkArraySize(data, size);
    }

    protected Object checkArraySize(Object dataArray, int size) {
	if (dataArray instanceof BitSet) {
	    return dataArray;
	}
	Object array = dataArray;
	int oldLength = array != null ? java.lang.reflect.Array
		.getLength(array) : 0;
	if (oldLength < size) {
	    int newLength = (size / bufIncr + 1) * bufIncr;
	    array = setArraySize(array, newLength);
	}
	return array;
    }

    protected void setArraySize(int size) {
	data = setArraySize(data, size);
    }

    protected Object setArraySize(Object dataArray, int size) {
	if (dataArray instanceof BitSet) {
	    return dataArray;
	}
	Object array = dataArray;
	try {
	    int oldLength = array != null ? java.lang.reflect.Array
		    .getLength(array) : 0;
	    if (oldLength != size) {
		Object tmp = java.lang.reflect.Array.newInstance(array
			.getClass().getComponentType(), size);
		if (size > oldLength) {
		    if (array.getClass().getComponentType() == Double.TYPE) {
			Arrays.fill((double[]) tmp,
				oldLength > 0 ? oldLength - 1 : 0, size - 1,
				Double.NaN);
		    } else if (array.getClass().getComponentType() == Float.TYPE) {
			Arrays.fill((float[]) tmp,
				oldLength > 0 ? oldLength - 1 : 0, size - 1,
				Float.NaN);
		    }
		}
		System.arraycopy(array, 0, tmp, 0, oldLength < size ? oldLength
			: size);
		array = tmp;
	    }
	} catch (Throwable t) {
	    System.err.println(this + "\t" + size + "\t" + t);
	}
	return array;
    }

    synchronized private void createDataArray(Class columnClass) {
	if (java.lang.Boolean.class.isAssignableFrom(columnClass)) {
	    data = new BitSet();
	    return;
	} else if (java.lang.Number.class.isAssignableFrom(columnClass)) {
	    data = allocNumArray(columnClass, bufIncr);
	    // System.err.println("COLUMN " + colIndex + " " + colClass + " " +
	    // data.getClass().getComponentType());
	    return;
	} else if (java.util.Date.class.isAssignableFrom(columnClass)) {
	    data = java.lang.reflect.Array.newInstance(Long.TYPE, bufIncr);
	    return;
	}
	objList = new Vector();
	data = java.lang.reflect.Array.newInstance(Integer.TYPE, bufIncr);
	return;
    }

    private Object allocNumArray(Class columnClass, int size) {
	Object dataArray = null;
	if (java.lang.Number.class.isAssignableFrom(columnClass)) {
	    if (java.lang.Double.class == colClass) {
		dataArray = java.lang.reflect.Array.newInstance(Double.TYPE,
			size);
	    } else if (java.lang.Long.class == colClass) {
		dataArray = java.lang.reflect.Array
			.newInstance(Long.TYPE, size);
	    } else if (java.lang.Integer.class == colClass) {
		dataArray = java.lang.reflect.Array.newInstance(Integer.TYPE,
			size);
	    } else if (java.lang.Float.class == colClass) {
		dataArray = java.lang.reflect.Array.newInstance(Float.TYPE,
			size);
	    } else if (java.lang.Short.class == colClass) {
		dataArray = java.lang.reflect.Array.newInstance(Short.TYPE,
			size);
	    } else if (java.lang.Byte.class == colClass) {
		dataArray = java.lang.reflect.Array
			.newInstance(Byte.TYPE, size);
	    } else if (java.math.BigDecimal.class == colClass) {
		dataArray = java.lang.reflect.Array.newInstance(Double.TYPE,
			size);
	    } else if (java.math.BigInteger.class == colClass) {
		dataArray = java.lang.reflect.Array
			.newInstance(Long.TYPE, size);
	    } else {
		dataArray = java.lang.reflect.Array
			.newInstance(Byte.TYPE, size);
	    }
	}
	return dataArray;
    }

    private void checkNumDataType(Number num, double dval, int ri) {
	Class prevType = data.getClass().getComponentType();
	if (data.getClass().getComponentType() == java.lang.Double.TYPE) {
	    return;
	} else if (data.getClass().getComponentType() == java.lang.Float.TYPE) {
	    if (dval < Float.MIN_VALUE || dval > Float.MIN_VALUE) {
		float[] oldArr = (float[]) data;
		double newArr[] = new double[oldArr.length];
		for (int i = 0; i < oldArr.length; i++) {
		    newArr[i] = oldArr[i];
		}
		data = newArr;
	    }
	} else {
	    boolean isFloat = num == null
		    || Math.abs(dval - num.longValue()) > 0.;
	    if (data.getClass().getComponentType() == java.lang.Long.TYPE) {
		if (isFloat) {
		    long[] oldArr = (long[]) data;
		    double newArr[] = new double[oldArr.length];
		    for (int i = 0; i < oldArr.length; i++) {
			newArr[i] = nullCells != null && nullCells.get(i) ? Double.NaN
				: (double) oldArr[i];
		    }
		    data = newArr;
		    nullCells = null;
		}
	    } else if (data.getClass().getComponentType() == java.lang.Integer.TYPE) {
		if (isFloat) {
		    int[] oldArr = (int[]) data;
		    if (Double.isInfinite(dval) || dval < Float.MIN_VALUE
			    || dval > Float.MIN_VALUE) {
			double newArr[] = new double[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = nullCells != null && nullCells.get(i) ? Double.NaN
				    : (double) oldArr[i];
			}
			data = newArr;
		    } else {
			float newArr[] = new float[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = nullCells != null && nullCells.get(i) ? Float.NaN
				    : (float) oldArr[i];
			}
			data = newArr;
		    }
		    nullCells = null;
		} else if (dval >= Integer.MIN_VALUE
			&& dval <= Integer.MAX_VALUE) {
		    return;
		} else {
		    int[] oldArr = (int[]) data;
		    // Convert to long
		    long[] newArr = new long[oldArr.length];
		    for (int i = 0; i < oldArr.length; i++) {
			newArr[i] = oldArr[i];
		    }
		    data = newArr;
		}
	    } else if (data.getClass().getComponentType() == java.lang.Short.TYPE) {
		if (isFloat) {
		    short[] oldArr = (short[]) data;
		    if (Double.isInfinite(dval) || dval < Float.MIN_VALUE
			    || dval > Float.MIN_VALUE) {
			double newArr[] = new double[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = nullCells != null && nullCells.get(i) ? Double.NaN
				    : (double) oldArr[i];
			}
			data = newArr;
		    } else {
			float newArr[] = new float[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = nullCells != null && nullCells.get(i) ? Float.NaN
				    : (float) oldArr[i];
			}
			data = newArr;
		    }
		    nullCells = null;
		} else if (dval >= Short.MIN_VALUE && dval <= Short.MAX_VALUE) {
		    return;
		} else {
		    short[] oldArr = (short[]) data;
		    if (dval >= Integer.MIN_VALUE && dval <= Integer.MAX_VALUE) {
			// Convert to integer
			int[] newArr = new int[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = oldArr[i];
			}
			data = newArr;
		    } else {
			// Convert to long
			long[] newArr = new long[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = oldArr[i];
			}
			data = newArr;
		    }
		}
	    } else if (data.getClass().getComponentType() == java.lang.Byte.TYPE) {
		if (isFloat) {
		    byte[] oldArr = (byte[]) data;
		    if (Double.isInfinite(dval) || dval < Float.MIN_VALUE
			    || dval > Float.MIN_VALUE) {
			double newArr[] = new double[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = nullCells != null && nullCells.get(i) ? Double.NaN
				    : (double) oldArr[i];
			}
			data = newArr;
		    } else {
			float newArr[] = new float[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = nullCells != null && nullCells.get(i) ? Float.NaN
				    : (float) oldArr[i];
			}
			data = newArr;
		    }
		    nullCells = null;
		} else if (dval >= Byte.MIN_VALUE && dval <= Byte.MAX_VALUE) {
		    return;
		} else {
		    byte[] oldArr = (byte[]) data;
		    if (dval >= Short.MIN_VALUE && dval <= Short.MAX_VALUE) {
			// Convert to short
			short[] newArr = new short[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = oldArr[i];
			}
			data = newArr;
		    } else if (dval >= Integer.MIN_VALUE
			    && dval <= Integer.MAX_VALUE) {
			// Convert to integer
			int[] newArr = new int[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = oldArr[i];
			}
			data = newArr;
		    } else {
			// Convert to long
			long[] newArr = new long[oldArr.length];
			for (int i = 0; i < oldArr.length; i++) {
			    newArr[i] = oldArr[i];
			}
			data = newArr;
		    }
		}
	    }
	}
	// System.err.println("COLUMN " + colIndex + " " + colClass + "\t" +
	// prevType + "\t -> " + data.getClass().getComponentType() + "\t" +
	// ri);
    }

    /**
     * Sets the value in the cell at rowIndex to obj.
     * 
     * @param obj
     *            - the new value
     * @rowIndex - the row whose value is to be set
     */
    synchronized void setValueAt(Object obj, int rowIndex) {
	checkArraySize(rowIndex + 1);
	if (isDate) {
	    if (obj != null && obj instanceof Date) {
		long val = ((Date) obj).getTime();
		// Check if this is replacing an existing value,
		// which is currently the min or max value.
		long prevVal = ((long[]) data)[rowIndex];
		((long[]) data)[rowIndex] = val;
		if (val < min) {
		    min = val;
		}
		if (val > max) {
		    max = val;
		}
	    } else {
		long val = Long.MAX_VALUE;
	    }
	} else if (isNumber) {
	    Number num = obj != null && obj instanceof Number ? (Number) obj
		    : null;
	    double val = num == null ? NULL_VALUE : num.doubleValue();
	    // Check if this is replacing an existing value
	    // which is currently the min or max value.
	    if (!Double.isNaN(val)) {
		if (val < min) {
		    min = val;
		    if (colClass == java.lang.Number.class) {
			checkNumDataType(num, val, rowIndex);
		    }
		}
		if (val > max) {
		    max = val;
		    if (colClass == java.lang.Number.class) {
			checkNumDataType(num, val, rowIndex);
		    }
		}
		if (rowIndex == 0) {
		    if (colClass == java.lang.Number.class) {
			checkNumDataType(num, val, rowIndex);
		    }
		}
	    }
	    if (data.getClass().getComponentType() == java.lang.Double.TYPE) {
		Array.setDouble(data, rowIndex, val);
	    } else if (data.getClass().getComponentType() == java.lang.Long.TYPE) {
		setNullBit(rowIndex, num == null);
		Array.setLong(data, rowIndex, (long) val);
	    } else if (data.getClass().getComponentType() == java.lang.Integer.TYPE) {
		setNullBit(rowIndex, num == null);
		Array.setInt(data, rowIndex, (int) val);
	    } else if (data.getClass().getComponentType() == java.lang.Float.TYPE) {
		Array.setFloat(data, rowIndex, (float) val);
	    } else if (data.getClass().getComponentType() == java.lang.Short.TYPE) {
		setNullBit(rowIndex, num == null);
		Array.setShort(data, rowIndex, (short) val);
	    } else if (data.getClass().getComponentType() == java.lang.Byte.TYPE) {
		setNullBit(rowIndex, num == null);
		Array.setByte(data, rowIndex, (byte) val);
	    }
	} else if (data != null && data instanceof BitSet) {
	    setNullBit(rowIndex, obj == null);
	    boolean val = false;
	    if (obj instanceof Boolean) {
		val = ((Boolean) obj).booleanValue();
	    } else if (obj instanceof Number) {
		val = ((Number) obj).intValue() > 0;
	    } else if (obj instanceof String) {
		val = (new Boolean((String) obj)).booleanValue();
	    }
	    ((BitSet) data).set(rowIndex, val);
	} else {
	    ((int[]) data)[rowIndex] = mapObject(obj);
	}
    }

    private void setNullBit(int rowIndex, boolean isNull) {
	if (isNull) {
	    if (nullCells == null) {
		nullCells = new BitSet(rowIndex + 1);
	    }
	    nullCells.set(rowIndex);
	} else if (nullCells != null) {
	    nullCells.clear(rowIndex);
	}
    }

    /**
     * Return the sorting order for mapping this column.
     * 
     * @param sortOrder
     *            the sorting order for mapping this column.
     */
    @Override
    public void setSortOrder(int sortOrder) {
	if (sortby != sortOrder) {
	    super.setSortOrder(sortOrder);
	    sortColumn();
	}
    }

    // Override since we are controlled by this tablemodel
    @Override
    public void cleanUp() {
    }

    // SelectableCellMap
}
