/*
 * @(#) $RCSfile: LinearAxis.java,v $ $Revision: 1.4 $ $Date: 2008/10/24 16:35:20 $ $Name: RELEASE_1_3_1_0001b $
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
 * The LinearAxis class provides a transformation between a value plotted along
 * an axis to a pixel position.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/24 16:35:20 $ $Name: RELEASE_1_3_1_0001b $
 * @since AV1.0
 */
public class LinearAxis extends AbstractAxis implements Cloneable {
    /**
     * Create a LinearAxis with default values: min = -10, max = 10, and size =
     * 0.
     */
    public LinearAxis() {
	super();
    }

    /**
     * Create a LinearAxis with default values of min = -10, max = 10 and set
     * the pixel size to the given size.
     * 
     * @param size
     *            the pixel size for the axis.
     */
    public LinearAxis(int size) {
	super(size);
    }

    /**
     * Create a LinearAxis with default values of min = -10, max = 10 and set
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
    public LinearAxis(int size, double min, double max) {
	super(size, min, max);
    }

    /**
     * Create a LinearAxis with the same min, max, and size values. as the given
     * axis.
     * 
     * @param axis
     *            the axis from which to set min, max, and size values.
     */
    public LinearAxis(Axis axis) {
	super(axis);
    }
}
