/*
 * @(#) $RCSfile: Graph.java,v $ $Revision: 1.3 $ $Date: 2008/10/21 20:09:07 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.Point;

/**
 * A Graph displays items mapped to its axes.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/21 20:09:07 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see GraphItem
 * @see Axis
 */
public interface Graph {

    /**
     * Set the axis for the X dimension.
     * 
     * @param xAxis
     */
    public void setXAxis(Axis xAxis);

    /**
     * Return the axis for the X dimension.
     * 
     * @return the X Axis
     */
    public Axis getXAxis();

    /**
     * Set the axis for the Y dimension.
     * 
     * @param yAxis
     */
    public void setYAxis(Axis yAxis);

    /**
     * Return the axis for the Y dimension.
     * 
     * @return the Y Axis
     */
    public Axis getYAxis();

    /**
     * Add the graph item to the graph.
     * 
     * @param graphItem
     *            Item to be added to the graph.
     */
    public void addGraphItem(GraphItem graphItem);

    /**
     * Remove the graph item from the graph.
     * 
     * @param graphItem
     *            Item to be removed from the graph.
     */
    public void removeGraphItem(GraphItem graphItem);

    /**
     * Return the graph items for this graph.
     * 
     * @return the graph items for this graph.
     */
    public GraphItem[] getGraphItems();

    /**
     * Return the axis values for the given pixel location on this graph.
     * 
     * @param p
     *            The point location on the graph.
     * @return the x and y values for the given pixel location.
     */
    public double[] getValueAt(Point p);

    /**
     * Return the axis values for the given pixel location on this graph.
     * 
     * @param x
     *            The x pixel location on the graph.
     * @param y
     *            The y pixel location on the graph.
     * @return the x and y values for the given pixel location.
     */
    public double[] getValueAt(int x, int y);

    /**
     * Whether to draw grid lines on the graph corresponding to tickmarks on the
     * axes.
     * 
     * @param show
     *            if true draw grid lines.
     */
    public void showGrid(boolean show);
}
