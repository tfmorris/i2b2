/*
 * @(#) $RCSfile: AbstractAxis.java,v $ $Revision: 1.4 $ $Date: 2008/10/14 21:24:06 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.event.EventListenerList;

/*
 * Still need to consider:
 *   min == max
 *   min > max
 */

/**
 * The AbstractAxis class provides a transformation between a value plotted
 * along an axis to a pixel position.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/14 21:24:06 $ $Name: RELEASE_1_3_1_0001b $
 * @since AV1.0
 */
public class AbstractAxis implements Axis, Cloneable {
    protected EventListenerList listenerList = new EventListenerList();
    protected int size = 0;
    protected double defaultMin = -10.;
    protected double defaultMax = 10.;
    protected double min = defaultMin;
    protected double max = defaultMax;

    protected double tickIncrement = Double.NaN;
    double ticks[] = null;
    double values[] = null;
    int outOfRangeFactor = 5;

    /**
     * Create a AbstractAxis with default values: min = -10, max = 10, and size
     * = 0.
     */
    public AbstractAxis() {
    }

    /**
     * Create a AbstractAxis with default values of min = -10, max = 10 and set
     * the pixel size to the given size.
     * 
     * @param size
     *            the pixel size for the axis.
     */
    public AbstractAxis(int size) {
	this.size = size;
    }

    /**
     * Create a AbstractAxis with default values of min = -10, max = 10 and set
     * the pixel size to the given size.
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
    public AbstractAxis(int size, double min, double max) {
	this.size = size;
	this.min = min;
	this.max = max;
    }

    /**
     * Create a AbstractAxis with the same min, max, and size values. as the
     * given axis.
     * 
     * @param axis
     *            the axis from which to set min, max, and size values.
     */
    public AbstractAxis(Axis axis) {
	this.size = axis.getSize();
	this.min = axis.getMin();
	this.max = axis.getMax();
    }

    /**
     * Return a copy of this Axis.
     * 
     * @return A copy of this Axis.
     */
    @Override
    public Object clone() {
	AbstractAxis axis = null;
	try {
	    axis = (AbstractAxis) super.clone();
	    axis.values = null;
	    axis.ticks = null;
	} catch (CloneNotSupportedException nsex) {
	}
	return axis;
    }

    /**
     * Sets the size of the axis. The size dictates the number of values
     * calulated along the axis. This would likely be the number of pixels for
     * the length of the axis on a two dimensional rectangular grid.
     * 
     * @param size
     *            the length in pixels.
     */
    public void setSize(int size) {
	if (this.size != size) {
	    this.size = size > 0 ? size : 0;
	    ticks = null;
	    values = null;
	    // Notify Listeners
	    fireChangeEvent();
	}
    }

    /**
     * Returns the size of the axis.
     * 
     * @return the size of the axis.
     */
    public int getSize() {
	return size;
    }

    /**
     * Sets the minimum value at the start of the axis.
     * 
     * @param value
     *            the value at the start of the axis.
     */
    public void setMin(double value) {
	setRange(value, max);
    }

    /**
     * Returns the minimum value of the axis.
     * 
     * @return the minimum value of the axis.
     */
    public double getMin() {
	return min;
    }

    /**
     * Sets the maximum value at the end of the axis.
     * 
     * @param value
     *            the maximum value at the end of the axis.
     */
    public void setMax(double value) {
	setRange(min, value);
    }

    /**
     * Returns the maximum value of the axis.
     * 
     * @return the maximum value of the axis.
     */
    public double getMax() {
	return max;
    }

    /**
     * Sets the range of the Axis.
     * 
     * @param min
     *            the minimum value at the start of the axis.
     * @param max
     *            the maximum value at the end of the axis.
     */
    public void setRange(double min, double max) {
	if (this.min != min || this.max != max) {
	    this.min = min;
	    this.max = max;
	    ticks = null;
	    values = null;
	    // Notify Listeners
	    fireChangeEvent();
	}
    }

    /**
     * Sets the range of the Axis.
     * 
     * @param range
     *            an array of length 2 holding the minimum and Maximum values of
     *            the axis.
     * @throws NullPointerException
     *             if the range is null.
     * @throws IllegalArgumentException
     *             if the range is not of length 2.
     */
    public void setRange(double[] range) throws NullPointerException,
	    IllegalArgumentException {
	if (range == null) {
	    throw new NullPointerException("setRange: range cannot be null");
	} else if (range.length != 2) {
	    throw new IllegalArgumentException(
		    "setRange: range must be [min, max]");
	} else {
	    setRange(range[0], range[1]);
	}
    }

    /**
     * Returns the range of the Axis in an array: [min, max].
     * 
     * @return an array of length 2 holding the minimum and Maximum values of
     *         the axis.
     */
    public double[] getRange() {
	double[] range = new double[2];
	range[0] = min;
	range[1] = max;
	return range;
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
	    Axis a = (Axis) obj;
	    if (a.getSize() == getSize() && a.getMin() == getMin()
		    && a.getMax() == getMax()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns evenly spaced values along the axis. The array contains pairs of
     * numbers: the first is the pixel offset from the start of the axis, the
     * second is the value at that location.
     * 
     * @return tick marks along the axis.
     */
    public double[] getTicks() {
	if (ticks == null)
	    setTicks();
	return (ticks);
    }

    /**
     * Place tickmarks along the axis at thee given increment.
     * 
     * @param tickIncrement
     *            The increment at which to place the ticks.
     */
    public void setTickIncrement(double tickIncrement) {
	this.tickIncrement = tickIncrement;
	ticks = null;
    }

    public double getTickIncrement() {
	return tickIncrement;
    }

    /**
     * Place tickmarks along the axis.
     * 
     * @param tickValues
     *            The values at which to place the ticks.
     */
    public void setTicks(double[] tickValues) {
	ticks = new double[2 * tickValues.length];
	for (int i = 0, j = 0; i < tickValues.length; i++, j += 2) {
	    ticks[j] = getPosition(tickValues[i]);
	    ticks[j + 1] = tickValues[i];
	}
    }

    /**
     * Place tickmarks along the axis spaced by tickIncrement.
     * 
     * @param tickIncrement
     *            The values at which to place the ticks.
     */
    public void setTicksByIncr(double tickIncrement) {
	double diff = Math.abs(max - min);
	if (!(diff > 0.) || tickIncrement == 0.) {
	    ticks = new double[2];
	    ticks[0] = 0.;
	    ticks[1] = min;
	    return;
	}
	double smin = Math.ceil(min / tickIncrement) * tickIncrement;
	double smax = Math.floor(max / tickIncrement) * tickIncrement;
	int count = ((int) Math.abs((smax - smin) / tickIncrement) + 1) * 2;
	ticks = new double[count];
	for (int i = 0; i < count; i += 2) {
	    double val = smin + i / 2 * tickIncrement;
	    ticks[i] = getIntPosition(val);
	    ticks[i + 1] = val;
	}
    }

    /**
     * Calculate where to place tickmarks along the axis. Attemmpt to place the
     * ticks at major numbers, for example: for axis from 0 - 50, ticks would be
     * placed at multiples of 10. for axis from .05 - .55, ticks would be placed
     * at multiples of .1.
     */
    public void setTicks() {
	Double dti = new Double(tickIncrement);
	if (!Double.isNaN(tickIncrement)) {
	    setTicksByIncr(tickIncrement);
	    return;
	}
	double diff = Math.abs(max - min);
	if (!(diff > 0.)) {
	    ticks = new double[2];
	    ticks[0] = 0.;
	    ticks[1] = min;
	    return;
	}
	double exp = Math.log(diff) / Math.log(10.); // log10
	int expi = -(int) Math.floor(exp);
	double scale = Math.pow(10., expi);
	double smin = Math.floor(min * scale);
	double smax = Math.ceil(max * scale);
	int sdiff = (int) (smax - smin);
	if (sdiff > size / 2)
	    scale *= .5;
	else if (sdiff < 3)
	    scale *= 4;
	else if (sdiff < 5)
	    scale *= 2;
	double incr = 1. / scale;
	int count = 0;
	double sval = smin / scale;
	if (max > min) {
	    smin = Math.ceil(min * scale);
	    smax = Math.floor(max * scale) / scale + .2 * incr;
	    sval = smin / scale;
	    while (sval <= smax) {
		count += 2;
		sval += incr;
	    }
	} else {
	    incr *= -1.;
	    smin = Math.floor(min * scale);
	    smax = Math.ceil(max * scale) / scale + .2 * incr;
	    sval = smin / scale;
	    while (sval >= smax) {
		count += 2;
		sval += incr;
	    }
	}
	sval = smin / scale;
	ticks = new double[count];
	for (int i = 0; i < count; i += 2) {
	    ticks[i] = Math.round((size - 1) * ((sval - min) / (max - min)));
	    ticks[i + 1] = Math.round(sval * scale) / scale;
	    sval += incr;
	}
    }

    /**
     * Calculate the value at each position along the axis.
     */
    protected void setValues() {
	values = new double[size];
	double f = size > 1 ? (max - min) / (size - 1.) : 0.;
	for (int i = 0; i < size; i++) {
	    double v = min + i * f;
	    values[i] = v;
	}
    }

    /**
     * Returns an array of values for each integral location along the axis.
     * 
     * @return values along the axis.
     */
    public double[] getValues() {
	if (values == null)
	    setValues();
	return values;
    }

    /**
     * Returns the value at the position along the axis.
     * 
     * @param position
     *            the position along the axis for which to return a value.
     * @return value at this position on the axis.
     */
    public double getValue(int position) {
	if (values == null)
	    setValues();
	if (position < 0 || position >= size) {
	    return min + position * (size > 1 ? (max - min) / (size - 1.) : 0.);
	}
	return values[position];
    }

    /**
     * Returns the relative position (0. to 1.) along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the relative position on the axis.
     */
    public double getRelPosition(double value) {
	if (Double.isNaN(value) || Double.isInfinite(value)) {
	    // System.err.println("getRelPosition " + value);
	    return value;
	}
	return (value - min) / (max - min);
    }

    /**
     * Returns the pixel position along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the relative position on the axis.
     */
    public double getPosition(double value) {
	return getRelPosition(value) * size;
    }

    /**
     * Returns an integral position along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the position on the axis.
     */
    public int getIntPosition(double value) {
	if (Double.isNaN(value)) {
	    // System.err.println("getIntPosition " + value);
	    return -2 * outOfRangeFactor;
	} else if (Double.isInfinite(value)) {
	    // System.err.println("getIntPosition " + value);
	    if (value > 0.)
		return size + 1 * outOfRangeFactor;
	    else
		return -1 * outOfRangeFactor;
	}
	return (int) getPosition(value);
    }

    protected void printValues() {
	for (int i = 0; i < size; i++) {
	    System.err.print(" " + values[i]);
	    if (i % 4 == 3)
		System.err.print("\n");
	}
	System.err.print("\n");
    }

    protected void printTicks() {
	double[] _ticks = getTicks();
	for (int i = 0; i < _ticks.length; i += 2) {
	    System.err.println(" " + _ticks[i] + " :  " + _ticks[i + 1]);
	}
    }

    /**
     * Adds the listener to be notified of changes to the data source.
     * 
     * @param listener
     *            the AxisListener to add
     */
    public void addAxisListener(AxisListener listener) {
	listenerList.add(AxisListener.class, listener);
    }

    /**
     * Removes the listener from the notification list.
     * 
     * @param listener
     *            the AxisListener to remove
     */
    public void removeAxisListener(AxisListener listener) {
	listenerList.remove(AxisListener.class, listener);
    }

    /**
     * Notify Listeners of change.
     */
    protected void fireChangeEvent() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	AxisEvent axisEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == AxisListener.class) {
		// Lazily create the event:
		if (axisEvent == null)
		    axisEvent = new AxisEvent(this);
		((AxisListener) listeners[i + 1]).axisChanged(axisEvent);
	    }
	}
    }

}
