/*
 * @(#) $RCSfile: LogAxis.java,v $ $Revision: 1.4 $ $Date: 2008/10/22 19:39:19 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * The LogAxis class provides a transformation between a value plotted along an
 * axis to a pixel position.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/22 19:39:19 $ $Name: RELEASE_1_3_1_0001b $
 * @since AV1.0
 */
public class LogAxis extends AbstractAxis implements Cloneable {
    double base = 10.;

    /**
     * Create a LogAxis with default values: min = -10, max = 10, and size = 0.
     */
    public LogAxis() {
	super();
    }

    /**
     * Create a LogAxis with default values of min = -10, max = 10 and set the
     * pixel size to the given size.
     * 
     * @param size
     *            the pixel size for the axis.
     */
    public LogAxis(int size) {
	super(size);
    }

    /**
     * Create a LogAxis with default values of min = -10, max = 10 and set the
     * pixel size to the given size.
     * 
     * @param size
     *            the pixel size for the axis.
     * @param min
     *            the minimum value of the axis corresponding to the zero pixel
     *            position.
     * @param max
     *            the maximum value of the axis corresponding to the pixel
     *            position at the size.
     */
    public LogAxis(int size, double min, double max) {
	super(size, min, max);
    }

    /**
     * Create a LogAxis with the same min, max, and size values. as the given
     * axis.
     * 
     * @param axis
     *            the axis from which to set min, max, and size values.
     */
    public LogAxis(Axis axis) {
	super(axis);
    }

    /**
     * Sets the logarithmic base.
     * 
     * @param base
     *            the logarithmic base for calculating positions.
     * @throws IllegalArgumentException
     *             If base <= 0.
     */
    public void setBase(double base) throws IllegalArgumentException {
	if (base <= 0.) {
	    throw new IllegalArgumentException("base value " + base
		    + " is not greater than 0.");
	}
	this.base = base;
    }

    /**
     * Returns the logarithmic base.
     * 
     * @return the logarithmic base for calculating positions.
     */
    public double getBase() {
	return base;
    }

    /**
     * Return whether the given Axis has the same min, max, and size values.
     * 
     * @param obj
     *            the Axis to compare
     * @return true if the given object is an Axis with the same min, max, and
     *         size values.
     */
    @Override
    public boolean equals(Object obj) {
	if (obj != null && obj.getClass() == this.getClass()) {
	    LogAxis a = (LogAxis) obj;
	    if (a.getSize() == getSize() && a.getMin() == getMin()
		    && a.getMax() == getMax() && a.getBase() == getBase()) {
		return true;
	    }
	}
	return false;
    }

    protected double valueAt(int position) {
	return min
		+ (max - min)
		* .1
		* (Math.exp((double) position / (double) size * Math.log(base)) - 1)
		* (base / (base - 1));
    }

    /**
     * Returns the relative position (0. to 1.) along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the relative position on the axis.
     */
    @Override
    public double getRelPosition(double value) {
	if (Double.isNaN(value) || Double.isInfinite(value)) {
	    // System.err.println("getRelPosition " + value);
	    return value;
	}
	return Math.log(1 + ((value - min) / (max - min) * (base - 1)))
		/ Math.log(base);
    }

}
