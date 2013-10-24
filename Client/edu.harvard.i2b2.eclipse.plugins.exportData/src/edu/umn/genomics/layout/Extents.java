/*
 * @(#) $RCSfile: Extents.java,v $ $Revision: 1.4 $ $Date: 2008/10/29 19:20:49 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.layout;

/**
 * Extents defines a double precision rectangle by specifying two opposite
 * corners.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/29 19:20:49 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class Extents {
    public double fromX = 0.;
    public double fromY = 0.;
    public double toX = 1.;
    public double toY = 1.;

    /**
     * Constructor that sets the default extents 0.,0. t0 1.,1.
     */
    public Extents() {
    }

    /**
     * Constructor that sets the extents to the given values.
     * 
     * @param fromX
     *            the left x position.
     * @param fromY
     *            the top y position.
     * @param toX
     *            the right x position.
     * @param toY
     *            the bottom y position.
     */
    public Extents(double fromX, double fromY, double toX, double toY) {
	this.fromX = fromX;
	this.fromY = fromY;
	this.toX = toX;
	this.toY = toY;
    }

    /**
     * Return the width of this Extent.
     * 
     * @return the width of this Extent.
     */
    public double getWidth() {
	return toX - fromX;
    }

    /**
     * Return the height of this Extent.
     * 
     * @return the height of this Extent.
     */
    public double getHeight() {
	return toY - fromY;
    }

    /**
     * Return the x origin of this Extent.
     * 
     * @return the x origin of this Extent.
     */
    public double getX() {
	return fromX;
    }

    /**
     * Return the y origin of this Extent.
     * 
     * @return the y origin of this Extent.
     */
    public double getY() {
	return fromY;
    }

    /**
     * Return a String representation of this Extent.
     * 
     * @return a representation of this Extent.
     */
    @Override
    public String toString() {
	return "[" + fromX + "," + fromY + "," + toX + "," + toY + "]";
    }
}
