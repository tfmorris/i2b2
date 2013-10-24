/*
 * @(#) $RCSfile: DataModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/17 19:00:57 $ $Name: RELEASE_1_3_1_0001b $
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
 * Return arrays of the x pixel location and the y pixel location for the data
 * values given the axes transformations.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/17 19:00:57 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface DataModel {
    /**
     * Return arrays of the x pixel location and the y pixel location.
     * 
     * @param x
     *            the x pixel offset
     * @param y
     *            the y pixel offset
     * @param axes
     *            the axes that transform the datapoints to the pixel area
     * @param points
     *            the array of points: xpoints, ypoints
     * @return the array of points: xpoints, ypoints
     */
    public int[][] getPoints(int x, int y, Axis axes[], int points[][]);

    /**
     * Return any y values at the given x index.
     * 
     * @param xi
     *            the x index into the array.
     * @return the y values at the given x index.
     */
    public double[] getYValues(int xi);

    /*
     * // Some possible methods for future consideration: public int[][]
     * getDistributionPolyline(int x, int y, Axis axes[], double distribution[],
     * int points[][]); public int[][] getMedianPolyline(int x, int y, Axis
     * axes[], int points[][]); public int[][] getSampledPolyline(int x, int y,
     * Axis axes[], int points[][]); public int[][] getMeanPolyline(int x, int
     * y, Axis axes[], int points[][]);
     */

}
