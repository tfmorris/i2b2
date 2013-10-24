/*
 * @(#) $RCSfile: CoordinateMap.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * CoordinateMap provides a mapping of coordinates to an index.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface CoordinateMap {
    /**
     * @return the all coordinate values.
     */
    public double[] getCoordinates();

    /**
     * @param row
     *            the row index in the table
     * @param coordinate
     *            if non-null returned with coordinate values filled in.
     * @return the coordinate values at the given row index.
     */
    public double[] getCoordinateAt(int row, double coordinate[]);

    /**
     * @param coordinateIndex
     * @return the row index for the given coordinate index.
     */
    public int getRowAt(int coordinateIndex);

    /**
     * @param row
     * @return the coordinate index for the given row index.
     */
    public int getCoordinateIndex(int row);

    /**
     * @return the number of coordinate points represented.
     */
    public int getCoordinateCount();

    /**
     * @return the number of dimensions in a coordinate point.
     */
    public int getDim();
}
