/*
 * @(#) $RCSfile: Axis.java,v $ $Revision: 1.3 $ $Date: 2008/10/14 21:42:57 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.graph;

/*
 * Still need to consider:
 *   min == max
 *   min > max
 */

/**
 * The Axis class provides a transformation between values along an axis and
 * linear screen position.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/14 21:42:57 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface Axis {

    /**
   *
   */
    public Object clone();

    /**
     * Sets the size of the axis. The size dictates the number of values
     * calulated along the axis. This would likely be the number of pixels for
     * the length of the axis on a two dimensional rectangular grid.
     * 
     * @param size
     *            the length in pixels.
     */
    public void setSize(int size);

    /**
     * Returns the size of the axis.
     * 
     * @return the size of the axis.
     */
    public int getSize();

    /**
     * Sets the minimum value at the start of the axis.
     * 
     * @param value
     *            the value at the start of the axis.
     */
    public void setMin(double value);

    /**
     * Returns the minimum value of the axis.
     * 
     * @return the minimum value of the axis.
     */
    public double getMin();

    /**
     * Sets the maximum value at the end of the axis.
     * 
     * @param value
     *            the maximum value at the end of the axis.
     */
    public void setMax(double value);

    /**
     * Returns the maximum value of the axis.
     * 
     * @return the maximum value of the axis.
     */
    public double getMax();

    /**
     * Return whether the given Axis has the same min, max, and size values.
     * 
     * @param obj
     *            the Axis to compare
     * @return true if the given object is an Axis with the same min, max, and
     *         size values.
     */
    public boolean equals(Object obj);

    /**
     * Calculate where to place tickmarks along the axis. Attempts to place the
     * ticks at major numbers, for example: for axis from 0 - 50, ticks would be
     * placed at multiples of 10. for axis from .05 - .55, ticks would be placed
     * at multiples of .1.
     */
    public void setTicks();

    /**
     * Place tickmarks along the axis.
     * 
     * @param tickValues
     *            The values at which to place the ticks.
     */
    public void setTicks(double[] tickValues);

    /**
     * Returns evenly spaced values along the axis. The array contains pairs of
     * numbers: the first is the pixel offset from the start of the axis, the
     * second is the value at that location.
     * 
     * @return tick marks along the axis.
     */
    public double[] getTicks();

    /**
     * Returns an array of values for each integral location along the axis.
     * 
     * @return values along the axis.
     */
    public double[] getValues();

    /**
     * Returns the value at the position along the axis.
     * 
     * @param position
     *            the position along the axis for which to return a value.
     * @return value at this position on the axis.
     */
    public double getValue(int position);

    /**
     * Returns the relative position (0. to 1.) along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the relative position on the axis.
     */
    public double getRelPosition(double value);

    /**
     * Returns the pixel position along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the relative position on the axis.
     */
    public double getPosition(double value);

    /**
     * Returns an integral position along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the position on the axis.
     */
    public int getIntPosition(double value);

    /*
     * public void setDefaultRange(double min, double max); public void
     * setRange(double min, double max); public void setRange(double[] range);
     * public double[] getRange();
     */

    public void addAxisListener(AxisListener listener);

    public void removeAxisListener(AxisListener listener);

}
