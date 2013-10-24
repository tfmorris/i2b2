/*
 * @(#) $RCSfile: InfLinearAxis.java,v $ $Revision: 1.4 $ $Date: 2008/10/24 16:35:20 $ $Name: RELEASE_1_3_1_0001b $
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
 * The InfLinearAxis class provides a transformation between a value plotted
 * along an axis to a pixel position.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/24 16:35:20 $ $Name: RELEASE_1_3_1_0001b $
 * @since AV1.0
 */
public class InfLinearAxis extends LinearAxis {
    int negW = 3; // Band of pixels for Negative Infinity values
    int posW = 3; // Band of pixels for Positive Infinity values
    int nanW = 3; // Band of pixels for null values

    /**
     * Create a InfLinearAxis with default values: min = -10, max = 10, and size
     * = 0.
     */
    public InfLinearAxis() {
    }

    /**
     * Create a InfLinearAxis with default values of min = -10, max = 10 and set
     * the pixel size to the given size.
     * 
     * @param size
     *            the pixel size for the axis.
     */
    public InfLinearAxis(int size) {
	super(size);
    }

    /**
     * Create a InfLinearAxis with default values of min = -10, max = 10 and set
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
    public InfLinearAxis(int size, double min, double max) {
	super(size, min, max);
    }

    /**
     * Create a LinearAxis with the same min, max, and size values. as the given
     * axis.
     * 
     * @param axis
     *            the axis from which to set min, max, and size values.
     */
    public InfLinearAxis(Axis axis) {
	super(axis);
    }

    /**
     * Calculate where to place tickmarks along the axis. Attemmpt to place the
     * ticks at major numbers, for example: for axis from 0 - 50, ticks would be
     * placed at multiples of 10. for axis from .05 - .55, ticks would be placed
     * at multiples of .1.
     */
    public void _setTicks() {
	if (!Double.isNaN(tickIncrement)) {
	    setTicksByIncr(tickIncrement);
	    return;
	}
	double diff = Math.abs(max - min);
	if (!(diff > 0.)) {
	    int tn = 2;
	    tn += nanW > 0 ? 2 : 0;
	    tn += negW > 0 ? 2 : 0;
	    tn += nanW > 0 || negW > 0 ? 2 : 0;
	    tn += posW > 0 ? 4 : 0;
	    ticks = new double[tn];
	    int i = 0;
	    if (nanW > 0) {
		ticks[i++] = 0.;
		ticks[i++] = Double.NaN;
		if (!(negW > 0)) {
		    ticks[i++] = 0. + nanW;
		    ticks[i++] = Double.NaN;
		}
	    }
	    if (negW > 0) {
		ticks[i++] = 0. + nanW;
		ticks[i++] = Double.NEGATIVE_INFINITY;
		ticks[i++] = 0. + nanW + negW;
		ticks[i++] = Double.NEGATIVE_INFINITY;
	    }
	    ticks[i++] = 0. + nanW + posW;
	    ticks[i++] = min;
	    if (posW > 0) {
		ticks[i++] = size - posW + 1;
		ticks[i++] = Double.POSITIVE_INFINITY;
		ticks[i++] = size - 1;
		ticks[i++] = Double.POSITIVE_INFINITY;
	    }
	    return;
	}
	double exp = Math.log(diff) * 0.43429448190325176; // log10
	int expi = -(int) Math.floor(exp);
	double scale = Math.pow(10., expi);
	double smin = Math.floor(min * scale);
	double smax = Math.ceil(max * scale);
	int siz = size - nanW - negW - nanW;
	int sdiff = (int) (smax - smin);
	if (sdiff > siz / 2)
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
	int tn = count;
	tn += nanW > 0 ? 2 : 0;
	tn += negW > 0 ? 2 : 0;
	tn += nanW > 0 || negW > 0 ? 2 : 0;
	tn += posW > 0 ? 4 : 0;
	sval = smin / scale;
	ticks = new double[tn];
	int pi = 0; // pixelIndex
	if (nanW > 0 && pi < tn) {
	    ticks[pi++] = 0.;
	    ticks[pi++] = Double.NaN;
	    if (!(negW > 0)) {
		ticks[pi++] = 0. + nanW;
		ticks[pi++] = Double.NaN;
	    }
	}
	if (negW > 0 && pi < tn) {
	    ticks[pi++] = 0. + nanW;
	    ticks[pi++] = Double.NEGATIVE_INFINITY;
	    ticks[pi++] = 0. + nanW + negW;
	    ticks[pi++] = Double.NEGATIVE_INFINITY;
	}
	for (int i = 0; i < count && pi < tn; i += 2) {
	    ticks[pi++] = nanW + negW + (siz - 1)
		    * ((sval - min) / (max - min));
	    ticks[pi++] = Math.round(sval * scale) / scale;
	    sval += incr;
	}
	if (posW > 0 && pi < tn) {
	    ticks[pi++] = size - posW + 1;
	    ticks[pi++] = Double.POSITIVE_INFINITY;
	    ticks[pi++] = size - 1;
	    ticks[pi++] = Double.POSITIVE_INFINITY;
	}
    }

    /**
     * Calculate the value at each position along the axis.
     */
    @Override
    protected void setValues() {
	values = new double[size];
	int pi = 0; // pixelIndex
	int siz = size - nanW - negW - posW;
	int biw = min < max ? negW : posW;
	int tiw = min < max ? posW : negW;
	double bv = min < max ? Double.NEGATIVE_INFINITY
		: Double.POSITIVE_INFINITY;
	double tv = min < max ? Double.POSITIVE_INFINITY
		: Double.NEGATIVE_INFINITY;
	double f = siz > 1 ? (max - min) / (siz - 1.) : 0.;
	for (int i = 0; i < biw && pi < size; i++, pi++) {
	    values[pi] = bv;
	}
	for (int i = 0; i < siz && pi < size; i++, pi++) {
	    double v = min + i * f;
	    values[pi] = v;
	}
	for (int i = 0; i < tiw && pi < size; i++, pi++) {
	    values[pi] = tv;
	}
	for (int i = 0; i < nanW && pi < size; i++, pi++) {
	    values[pi] = Double.NaN;
	}
    }

    /**
     * Returns the pixel position along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the relative position on the axis.
     */
    @Override
    public double getPosition(double value) {
	return getRelPosition(value) * (size - nanW - negW - posW)
		+ (min < max ? negW : posW);
    }

    /**
     * Returns an integral position along the axis for the value.
     * 
     * @param value
     *            the value to map to a position along the axis.
     * @return the position on the axis.
     */
    @Override
    public int getIntPosition(double value) {
	if (Double.isNaN(value)) {
	    return size - nanW / 2;
	} else if (Double.isInfinite(value)) {
	    if (value > 0.)
		return min < max ? size - nanW - posW / 2 : posW / 2;
	    else
		return min < max ? negW / 2 : size - nanW - negW / 2;
	}
	return (int) getPosition(value);
    }

    @Override
    protected void printValues() {
	for (int i = 0; i < size; i++) {
	    System.err.print(" " + values[i]);
	    if (i % 4 == 3)
		System.err.print("\n");
	}
	System.err.print("\n");
    }

    @Override
    protected void printTicks() {
	double[] _ticks = getTicks();
	for (int i = 0; i < _ticks.length; i += 2) {
	    System.err.println(" " + _ticks[i] + " :  " + _ticks[i + 1]);
	}
    }

}
