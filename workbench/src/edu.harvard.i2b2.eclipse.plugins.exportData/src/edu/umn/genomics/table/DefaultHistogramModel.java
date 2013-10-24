/*
 * @(#) $RCSfile: DefaultHistogramModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * BinModel.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 */
public class DefaultHistogramModel extends AbstractHistogramModel implements
	Runnable {
    protected int setOperator = SetOperator.REPLACE;
    public MultiDimIntArray binCounts = null;
    public MultiDimIntArray selCounts = null;
    Object dataModel = null;
    TableModelListener tml = new TableModelListener() {
	public void tableChanged(TableModelEvent e) {
	    setItemCount(((TableModel) e.getSource()).getRowCount());
	}
    };
    ListDataListener ldl = new ListDataListener() {
	public void intervalAdded(ListDataEvent e) {
	    setItemCount(((ListModel) e.getSource()).getSize());
	}

	public void intervalRemoved(ListDataEvent e) {
	    setItemCount(((ListModel) e.getSource()).getSize());
	}

	public void contentsChanged(ListDataEvent e) {
	    setItemCount(((ListModel) e.getSource()).getSize());
	}
    };
    ListSelectionModel lsm = null;
    ListSelectionListener lsl = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (!e.getValueIsAdjusting()) {
		recalcBinSizes();
	    }
	}
    };
    int itemCount = 0;
    int maxBinSize = -1;
    Thread calcThread = null;

    private void startCalc() {
	if (calcThread != null) {
	    calcThread.interrupt();
	}
	calcThread = new Thread(this);
	calcThread.start();
    }

    public void run() {
	updateBinSizes();
	calcThread = null;
    }

    /**
     * Recalculate bin sizes in a separate Thread.
     */
    private synchronized void recalcBinSizes() {
	startCalc();
    }

    /**
     * Create
     */
    public DefaultHistogramModel() {
    }

    /**
     * Set the number of items in the list or table.
     */
    private void setItemCount(int count) {
	if (itemCount != count) {
	    itemCount = count;
	    recalcBinSizes();
	}
    }

    /**
     * Get the number of items in the list or table.
     */
    @Override
    public int getItemCount() {
	return itemCount;
    }

    /**
     * Set the model that will be the data source for histograms.
     * 
     * @param model
     *            The data source.
     */
    public void setDataModel(TableModel model) {
	if (this.dataModel != null) {
	    if (this.dataModel instanceof TableModel) {
		((TableModel) this.dataModel).removeTableModelListener(tml);
	    } else if (this.dataModel instanceof ListModel) {
		((ListModel) this.dataModel).removeListDataListener(ldl);
	    }
	}
	this.dataModel = model;
	if (model != null) {
	    model.addTableModelListener(tml);
	    setItemCount(model.getRowCount());
	}
    }

    /**
     * Set the model that will be the data source for histograms.
     * 
     * @param model
     *            The data source.
     */
    public void setDataModel(ListModel model) {
	if (this.dataModel != null) {
	    if (this.dataModel instanceof TableModel) {
		((TableModel) this.dataModel).removeTableModelListener(tml);
	    } else if (this.dataModel instanceof ListModel) {
		((ListModel) this.dataModel).removeListDataListener(ldl);
	    }
	}
	this.dataModel = model;
	if (model != null) {
	    model.addListDataListener(ldl);
	    setItemCount(model.getSize());
	}
    }

    /**
     * Set the selection model for the data source.
     * 
     * @param lsm
     *            The selection model for the data source.
     */
    public void setListSelectionModel(ListSelectionModel lsm) {
	if (this.lsm != null) {
	    this.lsm.removeListSelectionListener(lsl);
	}
	this.lsm = lsm;
	if (lsm != null) {
	    lsm.addListSelectionListener(lsl);
	}
    }

    /**
     * Get the selection model for the data source.
     * 
     * @return The selection model for the data source.
     */
    public ListSelectionModel getListSelectionModel() {
	return lsm;
    }

    /**
     * Set the values to an initial state.
     */
    private void clear() {
	binCounts = null;
	selCounts = null;
	maxBinSize = -1;
    }

    public String arrayString(int[] arr) {
	String s = "[";
	if (arr != null && arr.length > 0) {
	    s += arr[0];
	    for (int i = 1; i < arr.length; i++) {
		s += "," + arr[i];
	    }
	}
	return s + "]";
    }

    /**
     * Update the count foreach bin.
     * 
     * @return whether counts are complete.
     */
    public synchronized boolean calculateBinSizes() {
	int[] dims = getDimensions();
	if (dims == null || dims.length < 1) {
	    return false;
	}
	MultiDimIntArray ca = new MultiDimIntArray(dims);
	MultiDimIntArray sa = new MultiDimIntArray(dims);
	int[] idx = new int[dims.length];
	for (int r = 0; r < itemCount; r++) {
	    try {
		boolean validIndex = true;
		for (int i = 0; i < dims.length; i++) {
		    if (Thread.currentThread().isInterrupted()) {
			// System.err.println("!!!! calculateBinSizes@" +
			// Integer.toHexString(hashCode()) );
			return false;
		    }
		    if (dims[i] > 0) {
			idx[i] = getBinModel(i).getBin(r);
			if (idx[i] < 0) {
			    validIndex = false;
			    break;
			}
		    }
		}
		if (validIndex) {
		    ca.incr(idx);
		    if (lsm != null && lsm.isSelectedIndex(r)) {
			sa.incr(idx);
		    }
		}
	    } catch (Exception ex) {
		System.err.println("Exception calculating bin for item " + r
			+ "  " + arrayString(idx) + " in " + arrayString(dims));
		ex.printStackTrace();
		return false;
	    }
	}
	// System.err.println(">>>  calculateBinSizes@" +
	// Integer.toHexString(hashCode()) );
	binCounts = ca;
	selCounts = sa;
	maxBinSize = binCounts.getMax();
	// System.err.println("<<<  calculateBinSizes@" +
	// Integer.toHexString(hashCode()) );
	return true;
    }

    /**
     * Update the count foreach bin, notifying listeners before the calculation
     * begins and after it is done.
     */
    public void updateBinSizes() {
	fireHistogramEvent(true);
	if (calculateBinSizes()) {
	    fireHistogramEvent(false);
	}
    }

    /**
     * Return the number of items in the bin located by the given indices.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @return the number of items in the bin.
     */
    @Override
    public int getBinCount(int[] indices) {
	if (binCounts == null) {
	    calculateBinSizes();
	}
	return binCounts.get(indices);
    }

    /**
     * Return the size of the bin with the most items.
     * 
     * @return the size of the bin with the most items.
     */
    @Override
    public int getMaxBinSize() {
	if (maxBinSize < 0 && binCounts != null) {
	    maxBinSize = binCounts.getMax();
	}
	return maxBinSize > 0 ? maxBinSize : 0;
    }

    /**
     * Return the number of items in each bin.
     * 
     * @return the number of items in each bin.
     */
    @Override
    public int[] getBinCounts() {
	if (binCounts == null) {
	    calculateBinSizes();
	}
	return binCounts != null ? binCounts.getValues() : new int[0];
    }

    /**
     * Return the number of selected items in each bin.
     * 
     * @return the number of selected items in each bin.
     */
    @Override
    public int[] getBinSelectCounts() {
	if (selCounts == null) {
	    calculateBinSizes();
	}
	return selCounts != null ? selCounts.getValues() : new int[0];
    }

    /**
     * return The indices of the bin for the item with the given index.
     * 
     * @param itemIndex
     *            The ordinal index of the item in the list or table.
     * @return The indices of the bin for the item with the given index.
     */
    public int[] getBin(int itemIndex) {
	// Should we throw exception if index out of range?
	int[] dims = getDimensions();
	int[] idx = new int[dims.length];
	for (int i = 0; i < dims.length; i++) {
	    if (Thread.interrupted()) {
		return null;
	    }
	    if (dims[i] > 0) {
		idx[i] = getBinModel(i).getBin(itemIndex);
	    }
	}
	return idx;
    }

    /**
     * Select items referenced by this bin and perform a set operation with the
     * items previously selected in the given listSelectModel.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @param listSelectModel
     *            The selection model to change with the bin selection, if null
     *            a new ListSelectionModel will be allocated.
     * @param setOperator
     *            The Set operation used when applying the new selection.
     * @return The ListSelectionModel resulting from the selection, this will be
     *         the listSelectModel parameter if that was not null;
     */
    public ListSelectionModel selectBins(int[] indices,
	    ListSelectionModel listSelectModel, int setOperator) {
	ListSelectionModel sm = listSelectModel != null ? listSelectModel
		: new DefaultListSelectionModel();
	sm.setValueIsAdjusting(true);
	int[] dims = getDimensions();
	for (int r = 0; r < itemCount; r++) {
	    try {
		int bi = MultiDimIntArray.getIndex(getBin(r), dims);
		boolean binSelected = false;
		for (int i = 0; i < indices.length; i++) {
		    if (bi == indices[i]) {
			binSelected = true;
			break;
		    }
		}
		if (binSelected) {
		    switch (setOperator) {
		    case SetOperator.DIFFERENCE:
			sm.removeSelectionInterval(r, r);
			break;
		    case SetOperator.INTERSECTION:
			if (sm.isSelectedIndex(r))
			    sm.addSelectionInterval(r, r);
			break;
		    case SetOperator.XOR:
			if (!sm.isSelectedIndex(r))
			    sm.addSelectionInterval(r, r);
			break;
		    case SetOperator.BRUSHOVER:
		    case SetOperator.REPLACE:
		    case SetOperator.UNION:
		    default:
			sm.addSelectionInterval(r, r);
			break;
		    }
		} else {
		    switch (setOperator) {
		    case SetOperator.UNION:
		    case SetOperator.DIFFERENCE:
			break;
		    case SetOperator.XOR:
			if (sm.isSelectedIndex(r))
			    sm.addSelectionInterval(r, r);
			break;
		    case SetOperator.BRUSHOVER:
		    case SetOperator.REPLACE:
		    case SetOperator.INTERSECTION:
		    default:
			sm.removeSelectionInterval(r, r);
			break;
		    }
		}
	    } catch (Exception ex) {
	    }
	}
	selCounts = null;
	sm.setValueIsAdjusting(false);
	// Invalidate the select Counts
	return sm;
    }

    int[] singleSelectIndex = new int[1];

    /**
     * Select items referenced by this bin and perform a set operation with the
     * items previously selected in the given listSelectModel.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @param listSelectModel
     *            The selection model to change with the bin selection, if null
     *            a new ListSelectionModel will be allocated.
     * @param setOperator
     *            The Set operation used when applying the new selection.
     * @return The ListSelectionModel resulting from the selection, this will be
     *         the listSelectModel parameter if that was not null;
     */
    @Override
    public ListSelectionModel selectBin(int[] indices,
	    ListSelectionModel listSelectModel, int setOperator) {
	singleSelectIndex[0] = MultiDimIntArray.getIndex(indices,
		getDimensions());
	return selectBins(singleSelectIndex, listSelectModel, setOperator);
    }

    /**
     * Select items referenced by this bin.
     * 
     * @param indices
     *            an array that locates a particular bin.
     */
    @Override
    public void selectBin(int[] indices) {
	selectBin(indices, lsm, setOperator);
    }

    /**
     * Select items referenced by this bin.
     * 
     * @param index
     *            locates a particular bin.
     */
    @Override
    public void selectBin(int index) {
	singleSelectIndex[0] = index;
	selectBins(singleSelectIndex, lsm, setOperator);
    }

    /**
     * Select items referenced by these bins.
     * 
     * @param indices
     *            an array that locates selected bins.
     */
    @Override
    public void selectBins(int[] indices) {
	selectBins(indices, lsm, setOperator);
    }

    /**
     * Get the number of selected items for the given bin.
     * 
     * @param indices
     *            The indices of the bin.
     * @return The number of items selected in the given bin.
     */
    @Override
    public int getBinSelectCount(int[] indices) {
	if (selCounts == null) {
	    calculateBinSizes();
	}
	return selCounts.get(indices);
    }

    /**
     * Update the bin sizes whenever a bin model changes.
     * 
     * @param e
     *            The event that triggered this call.
     */
    @Override
    protected void handleBinModelChange(BinModelEvent e) {
	recalcBinSizes();
    }

}
