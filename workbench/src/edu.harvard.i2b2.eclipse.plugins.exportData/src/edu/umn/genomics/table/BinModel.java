/*
 * @(#) $RCSfile: BinModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * BinModel categorizes an indexed list of items into a number of bins. This can
 * be used to generate histograms on the list.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 */
public interface BinModel {
    /**
     * Get the number of bins.
     * 
     * @return the number of bins.
     */
    public int getBinCount();

    /**
     * Return the number of items in this bin.
     * 
     * @param binIndex
     *            the index of bin.
     * @return the number of items included in this bin.
     */
    public int getBinSize(int binIndex);

    /**
     * Return the bin in which the value at the itemIndex belongs.
     * 
     * @param itemIndex
     *            the index of value to assign to a bin.
     * @return the index of bin.
     */
    public int getBin(int itemIndex);

    /**
     * Return a label for this bin.
     * 
     * @param binIndex
     *            the index of bin.
     * @return a label for this bin.
     */
    public String getBinLabel(int binIndex);

    /**
     * Set a Labeler for the bins.
     * 
     * @param labeler
     *            a Labeler for the bins.
     */
    public void setBinLabeler(BinLabeler labeler);

    /**
     * Return the Labeler for the bins.
     * 
     * @return the Labeler for the bins.
     */
    public BinLabeler getBinLabeler();

    /**
     * Select items referenced by this bin.
     * 
     * @param binIndex
     *            the index of bin.
     */
    public void selectBin(int binIndex);

    /**
     * Add a Listener for changes to this BinModel.
     * 
     * @param l
     *            the Listener to add.
     */
    public void addBinModelListener(BinModelListener l);

    /**
     * Remove a Listener for changes from this BinModel.
     * 
     * @param l
     *            the Listener to remove.
     */
    public void removeBinModelListener(BinModelListener l);
}
