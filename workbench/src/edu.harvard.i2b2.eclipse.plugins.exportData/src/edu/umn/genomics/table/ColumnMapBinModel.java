/*
 * @(#) $RCSfile: ColumnMapBinModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2004. The Regents of the University of Minnesota
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
import javax.swing.*;
import javax.swing.event.*;

/**
 * ColumnMapBinModel partitions the values in a ColumnMap into bins to support a
 * histogram.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 * @see ColumnMap
 */
public class ColumnMapBinModel implements MutableBinModel {
    protected EventListenerList listenerList = new EventListenerList();
    int type = 1;
    int binCount = 10;
    double startingValue;
    double increment;
    double dividers[];
    double binDividers[];
    PartitionIndexMap partitionMap;
    ColumnMap cmap;
    int[] binCounts = null;
    double[] binMin = null;
    double[] binMax = null;
    BinLabeler defaultBinLabeler = new BinLabeler() {
	public String getLabel(int binIndex) {
	    int bsize = getBinSize(binIndex);
	    switch (type) {
	    case 0:
	    case 5:
		break;
	    default:
		double dmin = binMin != null && binIndex < binMin.length ? binMin[binIndex]
			: getBinMin(binIndex);
		double dmax = binMax != null && binIndex < binMax.length ? binMax[binIndex]
			: getBinMax(binIndex);
		Object min = cmap.getMappedValue(binMin != null
			&& binIndex < binMin.length ? binMin[binIndex]
			: getBinMin(binIndex), 1);
		Object max = cmap.getMappedValue(binMax != null
			&& binIndex < binMax.length ? binMax[binIndex]
			: getBinMax(binIndex), -1);
		// Object max = cmap.getMappedValue(getBinMax(binIndex),-1);
		Object minNext = binIndex + 1 < getBinCount() ? cmap
			.getMappedValue(
				binMin != null && binIndex + 1 < binMin.length ? binMin[binIndex + 1]
					: getBinMin(binIndex + 1), 1)
			: max;
		// System.err.println("lbl " + binIndex + "\t" + min + "\t" +
		// max + "\t" + dmin + "\t" + dmax + "\t" + binMin);
		if ((min != null && min.equals(max))
		// || (max != null && max.equals(minNext))
		) {
		    return min.toString();
		    // return binIndex >= getBinCount() - 1 ? max.toString() :
		    // min.toString();
		}
		return min + "-" + max;
	    }
	    return "" + (binIndex + 1);
	}
    };
    BinLabeler binLabeler = defaultBinLabeler;
    /** Listener for changes to ColumnMaps */
    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    dividers = null;
	    if (e.getMapState() == CellMap.INVALID) {
	    } else if (!e.mappingInProgress()) {
		binCount = Math.min(binCount, cmap.getDistinctCount());
		calculateBinCounts();
		fireBinModelEvent();
	    }
	}
    };

    /**
     * Create a BinModel of the given ColumnMap.
     * 
     * @param cmap
     *            the ColumnMap to partition into bins.
     */
    public ColumnMapBinModel(ColumnMap cmap) {
	this.cmap = cmap;
	cmap.addCellMapListener(cml);
	if (cmap.getState() == CellMap.MAPPED) {
	    binCount = Math.min(binCount, cmap.getDistinctCount());
	}
    }

    /**
     * Return the ColumnMap this partitions.
     * 
     * @return The ColumnMap being partitioned.
     */
    public ColumnMap getColumnMap() {
	return cmap;
    }

    public void setBins(int binCount) {
	type = 1;
	this.binCount = binCount;
	dividers = null;
	binCounts = null;
	fireBinModelEvent();
    }

    public void setBins(double startingValue, double increment) {
	type = 2;
	this.startingValue = startingValue;
	this.increment = increment;
	dividers = null;
	binCounts = null;
	fireBinModelEvent();
    }

    public void setBins(double startingValue, double increment, int binCount) {
	type = 3;
	this.startingValue = startingValue;
	this.increment = increment;
	this.binCount = binCount;
	dividers = null;
	binCounts = null;
	fireBinModelEvent();
    }

    public void setBins(double binDividers[]) {
	type = 4;
	this.binDividers = binDividers;
	dividers = null;
	binCounts = null;
	fireBinModelEvent();
    }

    public void setBins(PartitionIndexMap partitionMap) {
	type = 5;
	this.partitionMap = partitionMap;
	dividers = null;
	binCounts = null;
	fireBinModelEvent();
    }

    public void setBins(Partition partition) {
	setBins(partition.getPartitionIndexMap());
    }

    public int getBinCount() {
	int cnt = 0;
	switch (type) {
	case 0:
	    break;
	case 5:
	    cnt = partitionMap.getDstSize();
	    break;
	case 1:
	    if (dividers == null) {
		cnt = binCount;
		break;
	    }
	case 2:
	    if (dividers == null) {
		if (increment > 0)
		    cnt = (int) Math.ceil((cmap.getMax() - startingValue)
			    / increment);
		else if (increment < 0)
		    cnt = (int) Math.ceil((startingValue - cmap.getMin())
			    / increment);
		break;
	    }
	case 3:
	    if (dividers == null) {
		cnt = binCount;
		break;
	    }
	case 4:
	    if (dividers == null) {
		cnt = binDividers != null ? binDividers.length - 1 : 0;
		break;
	    }
	default:
	    cnt = getDividers().length - 1;
	    break;
	}
	return cnt;
    }

    /**
     * Return the minimum map value that can be placed in the bin with the given
     * index. All values palce in this bin are greater than or equal to this
     * value.
     * 
     * @param binIndex
     *            The index of the bin.
     * @return The minimum value bound of the bin at this index.
     */
    public double getBinMin(int binIndex) {
	switch (type) {
	case 0:
	    return binIndex;
	case 5:
	    return binMin != null && binIndex < binMin.length ? binMin[binIndex]
		    : binIndex + 0.;
	}
	return getDividers()[binIndex];
    }

    /**
     * Return the maximum map value that can be placed in the bin with the given
     * index. All values palce in this bin are less than or equal to this value.
     * 
     * @param binIndex
     *            The index of the bin.
     * @return The maximum value bound of the bin at this index.
     */
    public double getBinMax(int binIndex) {
	switch (type) {
	case 0:
	    return binIndex + 0.;
	case 5:
	    return binMax != null && binIndex < binMax.length ? binMax[binIndex]
		    : binIndex + 0.;
	}
	return getDividers()[binIndex + 1];
    }

    /**
     * Return the array of values that divide the bins. This will be null for a
     * PartitionIndexMap.
     * 
     * @return the dividers array separating the bins.
     */
    public double[] getDividers() {
	if (dividers == null) {
	    int cnt = getBinCount();
	    double divisions[] = null;
	    switch (type) {
	    case 0:
		break;
	    case 1:
	    case 2:
	    case 3:
		double start = type == 1 ? cmap.getMin() : startingValue;
		double incr = type == 1 ? cmap.isContinuous() ? (cmap.getMax() - cmap
			.getMin())
			/ cnt
			: (double) cmap.getDistinctCount() / cnt
			: increment;
		divisions = new double[cnt + 1];
		for (int i = 0; i < divisions.length; i++) {
		    divisions[i] = start + i * incr;
		}
		break;
	    case 4:
		divisions = binDividers;
		break;
	    case 5:
		break;
	    }
	    dividers = divisions;
	    // System.err.println("dividers " + arrayToString(dividers));
	}
	return dividers;
    }

    /**
     * Get the index of the bin for the items in the ColumnMap that are mapped
     * to the given value.
     * 
     * @param val
     *            The map value in the ColumnMap.
     * @return The the index of the bin that the mapped value belongs to.
     */
    public int getBin(double val) {
	int idx = -1;
	switch (type) {
	case 0:
	case 1:
	case 2:
	case 3:
	case 4:
	    double div[] = getDividers();
	    if (div != null) {
		int i = Arrays.binarySearch(div, val);
		idx = i < 0 ? Double.isNaN(val) ? -1 : -i - 2 : i;
	    }
	    break;
	case 5:
	    break;
	}
	return idx == getBinCount() ? idx - 1 : idx;
    }

    public int getBin(int rowIndex) {
	if (type == 5) {
	    return partitionMap.getDst(rowIndex);
	}
	int idx = getBin(cmap.getMapValue(rowIndex));
	int binCnt = getBinCount();
	return idx >= binCnt ? binCnt - 1 : idx;
    }

    private void calculateBinCounts() {
	int[] cnt;
	double[] bmin = null;
	double[] bmax = null;
	switch (type) {
	case 0:
	case 1:
	case 2:
	case 3:
	case 4:
	    double div[] = getDividers();
	    cnt = new int[div.length - 1];
	    bmin = new double[cnt.length];
	    bmax = new double[cnt.length];
	    for (int i = 0; i < cnt.length; i++) {
		bmin[i] = Double.POSITIVE_INFINITY;
		bmax[i] = Double.NEGATIVE_INFINITY;
	    }
	    for (int r = 0; r < cmap.getCount(); r++) {
		double v = cmap.getMapValue(r);
		if (Double.isNaN(v)) {
		    continue;
		}
		int i = Arrays.binarySearch(div, v);
		i = i < 0 ? -i - 2 : i == cnt.length ? i - 1 : i;
		if (i < cnt.length) {
		    cnt[i]++;
		    if (v < bmin[i]) {
			bmin[i] = v;
		    }
		    if (v > bmax[i]) {
			bmax[i] = v;
		    }
		}
	    }
	    for (int i = 0; i < cnt.length; i++) {
		if (bmin[i] > div[i + 1]) {
		    bmin[i] = div[i];
		}
		if (bmax[i] < div[i]) {
		    bmax[i] = div[i + 1];
		}
	    }
	    binCounts = cnt;
	    binMin = bmin;
	    binMax = bmax;
	    break;
	case 5:
	    cnt = new int[getBinCount()];
	    bmin = new double[cnt.length];
	    bmax = new double[cnt.length];
	    if (cnt.length > 0) {
		bmin[0] = cmap.getMax();
		bmax[0] = cmap.getMin();
		for (int i = 1; i < cnt.length; i++) {
		    bmin[i] = bmin[0];
		    bmax[i] = bmax[0];
		}
	    }
	    for (int i = 0; i < cnt.length; i++) {
		int si[] = partitionMap.getSrcs(i);
		cnt[i] = si != null ? si.length : 0;
		if (si != null) {
		    for (int j = 0; j < si.length; j++) {
			double v = cmap.getMapValue(si[j]);
			if (v < bmin[i]) {
			    bmin[i] = v;
			}
			if (v > bmax[i]) {
			    bmax[i] = v;
			}
		    }
		}
	    }
	    binCounts = cnt;
	    binMin = bmin;
	    binMax = bmax;
	    break;
	}
	// System.err.println(arrayToString(bmin));
	// System.err.println(arrayToString(bmax));
    }

    /**
     * Return the number of items in this bin.
     * 
     * @param binIndex
     *            the index of bin.
     * @return the number of items included in this bin.
     */
    public int getBinSize(int binIndex) {
	int[] binCnts = binCounts;
	if (binCnts == null) {
	    calculateBinCounts();
	    binCnts = binCounts;
	}
	return binCnts != null && binIndex >= 0 && binIndex < binCnts.length ? binCnts[binIndex]
		: 0;
    }

    /**
     * Select items referenced by this bin.
     * 
     * @param binIndex
     *            the index of bin.
     */
    public void selectBin(int binIndex) {
	switch (type) {
	case 0:
	case 1:
	case 2:
	case 3:
	case 4:
	    cmap.selectRange(getBinMin(binIndex), getBinMax(binIndex));
	    break;
	case 5:
	    int si[] = partitionMap.getSrcs(binIndex);
	    DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
	    for (int i = 0; i < si.length; i++) {
		dlsm.addSelectionInterval(si[i], si[i]);
	    }
	    cmap.selectValues(dlsm);
	    break;
	}
    }

    /**
     * Set a Labeler for the bins.
     * 
     * @param labeler
     *            a Labeler for the bins.
     */
    public void setBinLabeler(BinLabeler labeler) {
	binLabeler = labeler != null ? labeler : defaultBinLabeler;
    }

    /**
     * Return the Labeler for the bins.
     * 
     * @return the Labeler for the bins.
     */
    public BinLabeler getBinLabeler() {
	return binLabeler;
    }

    /**
     * Return a label for this bin.
     * 
     * @param binIndex
     *            the index of bin.
     * @return a label for this bin.
     */
    public String getBinLabel(int binIndex) {
	return "" + (binIndex + 1);
    }

    /**
     * Add a Listener for changes to this BinModel.
     * 
     * @param l
     *            the Listener to add.
     */
    public void addBinModelListener(BinModelListener l) {
	listenerList.add(BinModelListener.class, l);
    }

    /**
     * Remove a Listener for changes from this BinModel.
     * 
     * @param l
     *            the Listener to remove.
     */
    public void removeBinModelListener(BinModelListener l) {
	listenerList.remove(BinModelListener.class, l);
    }

    public String arrayToString(double[] a) {
	StringBuffer sb = new StringBuffer();
	sb.append("[");
	if (a != null) {
	    for (int i = 0; i < a.length; i++) {
		sb.append((i > 0 ? "," : "") + a[i]);
	    }
	}
	sb.append("]");
	return sb.toString();
    }

    /**
     * Notify Listeners of change.
     */
    protected void fireBinModelEvent() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	BinModelEvent event = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == BinModelListener.class) {
		// Lazily create the event:
		if (event == null)
		    event = new BinModelEvent(this);
		((BinModelListener) listeners[i + 1]).binModelChanged(event);
	    }
	}
    }

}
