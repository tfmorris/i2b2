/*
 * @(#) $RCSfile: AbstractHistogramModel.java,v $ $Revision: 1.4 $ $Date: 2008/10/28 21:04:40 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import javax.swing.event.EventListenerList;

/**
 * BinModel.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 */
public abstract class AbstractHistogramModel implements HistogramModel {
    protected EventListenerList listenerList = new EventListenerList();
    protected Vector models = new Vector();
    protected BinModelListener binModelListener = new BinModelListener() {
	public void binModelChanged(BinModelEvent e) {
	    handleBinModelChange(e);
	    fireHistogramEvent();
	}
    };

    /**
     * Get the number of BinModels.
     * 
     * @return the number of BinModels.
     */
    public int getModelCount() {
	return models.size();
    }

    /**
     * Return the BinModel at the given index.
     * 
     * @param index
     *            the index of BinModel.
     * @return the BinModel at the index.
     */
    public BinModel getBinModel(int index) {
	return (BinModel) models.get(index);
    }

    /**
     * Return a copy of the list of BinModels.
     * 
     * @return the list of BinModels.
     */
    public List getBinModelList() {
	return (List) models.clone();
    }

    /**
     * Return an array with the bin count of each BinModel.
     * 
     * @return an array with the bin count of each BinModel.
     */
    public int[] getDimensions() {
	int[] dims = new int[getModelCount()];
	for (int i = 0; i < dims.length; i++) {
	    BinModel m = getBinModel(i);
	    dims[i] = m != null ? m.getBinCount() : 0;
	}
	return dims;
    }

    /**
     * Return the bin count.
     * 
     * @return the bin count.
     */
    public int getBinCount() {
	int[] dims = getDimensions();
	int cnt = 0;
	for (int i = 0; i < dims.length; i++) {
	    if (dims[i] > 0) {
		cnt = cnt > 0 ? cnt * dims[i] : dims[i];
	    }
	}
	return cnt;
    }

    /**
     * Add a BinModel to this Histogram.
     * 
     * @param model
     *            BinModel to add.
     */
    public void addBinModel(BinModel model) {
	models.add(model);
	model.addBinModelListener(binModelListener);
	handleBinModelChange(null);
	BinModel[] binChanged = { model };
	fireHistogramEvent(null, binChanged, false);
    }

    /**
     * Set the BinModel of this Histogram at the index.
     * 
     * @param index
     *            the index for the model.
     * @param model
     *            BinModel to add.
     */
    public void setBinModel(int index, BinModel model) {
	BinModel[] binAdded = { model };
	BinModel[] binRemoved = null;
	if (index >= models.size()) {
	    models.setSize(index + 1);
	} else {
	    BinModel m = getBinModel(index);
	    if (m == model) {
		return;
	    }
	    if (m != null) {
		m.removeBinModelListener(binModelListener);
		binRemoved = new BinModel[1];
		binRemoved[0] = m;
	    }
	}
	models.set(index, model);
	model.addBinModelListener(binModelListener);
	handleBinModelChange(null);
	BinModel[] binChanged = { model };
	fireHistogramEvent(binRemoved, binAdded, false);
    }

    /**
     * Remove a BinModel from this Histogram.
     * 
     * @param model
     *            BinModel to remove.
     */
    public void removeBinModel(BinModel model) {
	model.removeBinModelListener(binModelListener);
	models.remove(model);
	handleBinModelChange(null);
	BinModel[] binChanged = { model };
	fireHistogramEvent(binChanged, null, false);
    }

    /**
     * Remove all BinModels from this Histogram.
     */
    public void removeBinModels() {
	if (models.size() > 0) {
	    BinModel[] binChanged = new BinModel[models.size()];
	    binChanged = (BinModel[]) models.toArray(binChanged);
	    for (int i = 0; i < binChanged.length; i++) {
		binChanged[i].removeBinModelListener(binModelListener);
	    }
	    models.clear();
	    handleBinModelChange(null);
	    fireHistogramEvent(binChanged, null, false);
	}
    }

    public abstract int getItemCount();

    /**
     * Return the number of items in the bin located by the given indices.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @return the number of items in the bin.
     */
    public abstract int getBinCount(int[] indices);

    /**
     * Return the number of selected items in the bin located by the given
     * indices.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @return the number of selected items in the bin.
     */
    public abstract int getBinSelectCount(int[] indices);

    /**
     * Return the size of the bin with the most items.
     * 
     * @return the size of the bin with the most items.
     */
    public abstract int getMaxBinSize();

    /**
     * Return the number of items in each bin.
     * 
     * @return the number of items in each bin.
     */
    public abstract int[] getBinCounts();

    /**
     * Return the number of selected items in each bin.
     * 
     * @return the number of selected items in each bin.
     */
    public abstract int[] getBinSelectCounts();

    /**
     * Select items referenced by this bin.
     * 
     * @param indices
     *            an array that locates a particular bin.
     */
    public abstract void selectBin(int[] indices);

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
    public abstract ListSelectionModel selectBin(int[] indices,
	    ListSelectionModel listSelectModel, int setOperator);

    /**
     * Select items referenced by this bin.
     * 
     * @param index
     *            locates a particular bin.
     */
    public abstract void selectBin(int index);

    /**
     * Select items referenced by these bins.
     * 
     * @param indices
     *            an array that locates selected bins.
     */
    public abstract void selectBins(int[] indices);

    /**
     * This is called whenever the BinModels have changed prior to firing a
     * HistogramEvent. This implementation does nothing, but the method may be
     * overidden by extending classes.
     * 
     * @param e
     *            The event from a BinModel or null if this resulted from a
     *            change in the collection of BinModels .
     */
    protected void handleBinModelChange(BinModelEvent e) {
    }

    /**
     * Add a Listener for changes to this BinModel.
     * 
     * @param l
     *            the Listener to add.
     */
    public void addHistogramListener(HistogramListener l) {
	listenerList.add(HistogramListener.class, l);
    }

    /**
     * Remove a Listener for changes from this BinModel.
     * 
     * @param l
     *            the Listener to remove.
     */
    public void removeHistogramListener(HistogramListener l) {
	listenerList.remove(HistogramListener.class, l);
    }

    /**
     * Notify Listeners of a change.
     */
    protected void fireHistogramEvent() {
	fireHistogramEvent(false);
    }

    /**
     * Notify Listeners of a change.
     * 
     * @param isAdjusting
     *            Whether this model is still in the process of changing.
     */
    protected void fireHistogramEvent(boolean isAdjusting) {
	fireHistogramEvent(null, null, isAdjusting);
    }

    /**
     * Notify Listeners of a change.
     * 
     * @param removed
     *            an array of BinModels that have been removed
     * @param added
     *            an array of BinModels that have been added
     * @param isAdjusting
     *            Whether this model is still in the process of changing.
     */
    protected void fireHistogramEvent(BinModel[] removed, BinModel[] added,
	    boolean isAdjusting) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	HistogramEvent histogramEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == HistogramListener.class) {
		// Lazily create the event:
		if (histogramEvent == null)
		    histogramEvent = new HistogramEvent(this, removed, added,
			    isAdjusting);
		((HistogramListener) listeners[i + 1])
			.histogramChanged(histogramEvent);
	    }
	}
    }
}
