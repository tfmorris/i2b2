/*
 * @(#) $RCSfile: CellMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import edu.umn.genomics.graph.LineFormula;

/**
 * CellMap maps the values of a Collection of values to a numeric range. If the
 * common Class type of the Collection is Number or Date, or if all of the *
 * values of the Collection are Strings that can be parsed as class Number or
 * Date, * the range will be from the minimum to the maximum of the values in
 * the Collection. If any value can not be parsed as class Number or Date, the
 * values will be mapped to integral values from 0 to the number of distinct
 * values - 1, (distinct values are determined by the value's equals method.)
 * The CellMap can also be used to select a range of mapped values from the
 * Collection and indicate the selected indices in a ListSelectionModel. A new
 * selection is be specified as a subrange of values between the the minimum and
 * maximum values of the collection. New selections may be combined with the
 * previous selections using standard set operators.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public interface CellMap {
    /**
     * Sort the cells by their natural order, e.g. alphabetically for text
     * strings, numerically for numbers.
     */
    public static final int NATURALSORT = 0;
    /**
     * Sort the cells by comparing their respective alphabetic and numeric
     * parts, e.g. page2b, page10a, page10c.
     */
    public static final int ALPHANUMSORT = 1;
    /**
     * Discreet value are mapped in the order they appear in the list.
     */
    public static final int ROWORDERSORT = 2;

    /**
     * The TableModel values haven't been mapped.
     */
    public static final int UNMAPPED = 0;
    /**
     * The TableModel values are in the process of being mapped.
     */
    public static final int MAPPING = 1;
    /**
     * The TableModel values have been mapped.
     */
    public static final int MAPPED = 2;
    /**
     * The TableModel changed and this map is no longer valid for this
     * TableModel.
     */
    public static final int INVALID = -1;

    /**
     * Add a listener for changes to the CellMap.
     * 
     * @param l
     *            The listener
     */
    public void addCellMapListener(CellMapListener l);

    /**
     * Remove a listener from the CellMap.
     * 
     * @param l
     *            The listener
     */
    public void removeCellMapListener(CellMapListener l);

    /**
     * Return the state of mapping.
     * 
     * @return The state of this map.
     * @see #UNMAPPED
     * @see #MAPPING
     * @see #MAPPED
     * @see #INVALID
     */
    public int getState();

    /**
     * Return the name given this map.
     * 
     * @return the name given this map.
     */
    public String getName();

    /**
     * Set the name for this map.
     * 
     * @param name
     *            the name for this map.
     */
    public void setName(String name);

    public int getDistinctCount();

    public int getNullCount();

    public double getMedian(); // median value

    // public double getPercentile(double percentile); // value at Percentile
    public double getAvg();

    public double getStdDev(); // standard deviation value

    public double getVariance(); // standard deviation value

    public double[] stdvals();

    public double covariance(CellMap map);

    public double correlation(CellMap map);

    public void setCovariance(CellMap map, double covariance);

    public LineFormula regressionLine(CellMap ymap);

    /**
     * Return a count of the number of values mapped.
     * 
     * @return a count of the number of values mapped.
     */
    public int getCount();

    /**
     * Return the minimum mapped value of this collection.
     * 
     * @return the minimum mapped value of this collection.
     */
    public double getMin();

    /**
     * Return the maximum mapped value of this collection.
     * 
     * @return the maximum mapped value of this collection.
     */
    public double getMax();

    /**
     * Set whether to map Number and Dates values on a continuum or as a set of
     * discreet elements.
     */
    public void setMapping(boolean continuous);

    /**
     * Return whether Number and Dates values are mapped on a continuum or as a
     * set of discreet elements.
     */
    public boolean getMapping();

    /**
     * Set the default sorting order for this CellMap Class. The implementing
     * class needs to store this in a static variable.
     * 
     * @param sortOrder
     *            the default sorting order for this CellMap Class.
     */
    public void setDefaultSortOrder(int sortOrder);

    /**
     * Return the default sorting order for this CellMap Class.
     * 
     * @return the default sorting order for this CellMap Class.
     */
    public int getDefaultSortOrder();

    /**
     * Set the sorting order for mapping this collection.
     * 
     * @param sortOrder
     *            the sorting order for mapping this collection.
     */
    public void setSortOrder(int sortOrder);

    /**
     * Return the sorting order for mapping this collection.
     * 
     * @return the sorting order for mapping this collection.
     */
    public int getSortOrder();

    /**
     * Get a mapping from a sorted list of distinct values to the table rows
     * that contain that value. PartitionIndexMap.getSrcs(0) will return the
     * indices of the table rows that contain that contain the first value in
     * order.
     * 
     * @return a mapping from a sorted list of distinct values to the table rows
     */
    public PartitionIndexMap getPartitionIndexMap();

    /**
     * Return the mapped value for each element. return the mapped value for
     * each element.
     */
    public double[] getMapValues();

    /**
     * Return the mapped value for the given index.
     * 
     * @param index
     *            the index into the list of elements being mapped. return the
     *            mapped value of the element at the given index.
     */
    public double getMapValue(int index);

    /**
     * For mapped set elements, return the element that is mapped nearest to the
     * the mapValue in the given direction. For Numbers or Dates mapped along a
     * continuum, return the value (Number or Date) that the given mapPosition
     * represents along the continuum (dir is ignored).
     * 
     * @param mapValue
     *            the relative position on the map from minimum to maximum.
     * @param dir
     *            negative means round down, positive mean round up, 0 rounds to
     *            closest element. return the element that is mapped nearest to
     *            the the mapValue.
     */
    public Object getMappedValue(double mapValue, int dir);

    /**
     * Construct a histogram returning the count of elements in each subrange
     * along the CellMap.
     * 
     * @param buckets
     *            the number of divisions of the range of values. If the value
     *            is zero, the default numbers of subdivisions be returned: 10
     *            if the collection is numeric, else the number of discrete
     *            elements.
     * @param withSelectCounts
     * @return an array of count of elements in each range.
     */
    public int[] getCounts(int buckets, boolean withSelectCounts);

    /**
     * Return whether values in this collection are mapped to a continuous range
     * or as discreet elements of a set.
     * 
     * @return true if values in this collection are mapped to a continuous
     *         range.
     */
    public boolean isContinuous();

    /**
     * Return whether all values in this collection are number values.
     * 
     * @return true if all values in this collection are number values, else
     *         false.
     */
    public boolean isNumber();

    /**
     * Return whether all values in this collection are Dates.
     * 
     * @return true if all values in this collection are Dates, else false.
     */
    public boolean isDate();

    /**
     * Return whether all values in this collection are Boolean.
     * 
     * @return true if all values in this collection are Boolean, else false.
     */
    public boolean isBoolean();

    /**
     * Free resources.
     */
    public void cleanUp();
}
