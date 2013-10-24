/*
 * @(#) $RCSfile: HistogramModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * A HistogramModel contains a collection of BinModels that define a histogram
 * that can have multiple dimensions.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 */
public interface HistogramModel {
    /**
     * Get the number of BinModels.
     * 
     * @return the number of BinModels.
     */
    public int getModelCount();

    /**
     * Return the BinModel at the given index.
     * 
     * @param index
     *            the index of BinModel.
     * @return the BinModel at the index.
     */
    public BinModel getBinModel(int index);

    /**
     * Return a copy of the list of BinModels.
     * 
     * @return the list of BinModels.
     */
    public List getBinModelList();

    /**
     * Return the bin count.
     * 
     * @return the bin count.
     */
    public int getBinCount();

    /**
     * Return an array with the bin count of each BinModel.
     * 
     * @return an array with the bin count of each BinModel.
     */
    public int[] getDimensions();

    /**
     * Return the number of items in the histogram.
     * 
     * @return the number of items in the histogram.
     */
    public int getItemCount();

    /**
     * Return the number of items in the bin located by the given indices.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @return the number of items in the bin.
     */
    public int getBinCount(int[] indices);

    /**
     * Return the number of selected items in the bin located by the given
     * indices.
     * 
     * @param indices
     *            an array that locates a particular bin.
     * @return the number of selected items in the bin.
     */
    public int getBinSelectCount(int[] indices);

    /**
     * Return the size of the bin with the most items.
     * 
     * @return the size of the bin with the most items.
     */
    public int getMaxBinSize();

    /**
     * Return the number of items in each bin.
     * 
     * @return the number of items in each bin.
     */
    public int[] getBinCounts();

    /**
     * Return the number of selected items in each bin.
     * 
     * @return the number of selected items in each bin.
     */
    public int[] getBinSelectCounts();

    /**
     * Select items referenced by this bin.
     * 
     * @param indices
     *            an array that locates a particular bin.
     */
    public void selectBin(int[] indices);

    /**
     * Select items referenced by this bin.
     * 
     * @param index
     *            locates a particular bin.
     */
    public void selectBin(int index);

    /**
     * Select items referenced by these bins.
     * 
     * @param indices
     *            an array that locates selected bins.
     */
    public void selectBins(int[] indices);

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
    public ListSelectionModel selectBin(int[] indices,
	    ListSelectionModel listSelectModel, int setOperator);

    /**
     * Select items referenced by these bins.
     * 
     * @param indices
     *            an array that locates selected bins.
     * @param listSelectModel
     *            The selection model to change with the bin selection, if null
     *            a new ListSelectionModel will be allocated.
     * @param setOperator
     *            The Set operation used when applying the new selection.
     * @return The ListSelectionModel resulting from the selection, this will be
     *         the listSelectModel parameter if that was not null;
     */
    public ListSelectionModel selectBins(int[] indices,
	    ListSelectionModel listSelectModel, int setOperator);

    /**
     * Get the selection model for the data source.
     * 
     * @return The selection model for the data source.
     */
    public ListSelectionModel getListSelectionModel();

    /**
     * Add a Listener for changes to this BinModel.
     * 
     * @param l
     *            the Listener to add.
     */
    public void addHistogramListener(HistogramListener l);

    /**
     * Remove a Listener for changes from this BinModel.
     * 
     * @param l
     *            the Listener to remove.
     */
    public void removeHistogramListener(HistogramListener l);

}
