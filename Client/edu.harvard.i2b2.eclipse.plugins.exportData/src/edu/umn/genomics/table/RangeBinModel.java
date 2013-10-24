/*
 * @(#) $RCSfile: RangeBinModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * Divides a range of values into bins.
 * 
 * @author J Johnson
 * @version %I%, %G%
 * @since 1.0
 */
public interface RangeBinModel extends BinModel {
    /**
     * Set the number of bins and evenly divide up the range into the bins.
     * 
     * @param binCount
     *            the number of bins.
     */
    public void setBins(int binCount);

    /**
     * Divide the range into bins starting from the startingValue with other
     * dividers being multiples of the increment from the startingValue.
     * 
     * @param startingValue
     *            the reference value for a bin.
     * @param increment
     *            the increment to the start of the next bin.
     */
    public void setBins(double startingValue, double increment);

    /**
     * Divide the range into bins starting from the startingValue with other
     * dividers being multiples of the increment from the startingValue Limit
     * the bins to the given binCount. Set the number of bins and evenly divide
     * up the range into the bins.
     * 
     * @param startingValue
     *            the reference value for a bin.
     * @param increment
     *            the increment to the start of the next bin.
     * @param binCount
     *            the number of bins.
     */
    public void setBins(double startingValue, double increment, int binCount);

    /**
     * Set the bin divider locations.
     * 
     * @param dividers
     *            the dividers between bins, with bins containing values:
     *            dividers[0] <= value < dividers[n+1] and the last bin
     *            containing values: dividers[n-1] <= value < dividers[n]
     */
    public void setBins(double dividers[]);

    /**
     * Return the minimum value included in this bin.
     * 
     * @param binIndex
     *            the index of bin.
     * @return the minimum value included in this bin.
     */
    public double getBinMin(int binIndex);

    /**
     * Return the maximum value included in this bin.
     * 
     * @param binIndex
     *            the index of bin.
     * @return the maximum value included in this bin.
     */
    public double getBinMax(int binIndex);

    /**
     * Return the array of values that divide the bins. This will be the
     * integral values for a PartitionIndexMap.
     * 
     * @return the dividers array separating the bins.
     */
    public double[] getDividers();

    /**
     * Return the index of the bin in which a value falls.
     * 
     * @param value
     *            the value to place in a bin.
     * @return the index of the bin into which the value would be placed.
     */
    public int getBin(double value);

}
