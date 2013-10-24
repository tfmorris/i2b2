/*
 * @(#) $RCSfile: GraphItem.java,v $ $Revision: 1.4 $ $Date: 2008/10/28 21:04:14 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;

/**
 * An item to be displayed on a graph.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/28 21:04:14 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see Graph
 */
public interface GraphItem {
    /**
     * Set the color of the graph item.
     * 
     * @param color
     *            for this graph item.
     */
    public void setColor(Color color);

    /**
     * Return the color of the graph item.
     * 
     * @return color of the graph item.
     */
    public Color getColor();

    /**
     * Set the label of the graph item.
     * 
     * @param label
     *            for this graph item.
     */
    /**
     * Set the color for each drawable of the graph item.
     * 
     * @param indexedColor
     *            color for each drawable of this graph item.
     */
    public void setIndexedColor(IndexedColor indexedColor);

    /**
     * Return the color for each drawable of the graph item.
     * 
     * @return the color for each drawable of the graph item.
     */
    public IndexedColor getIndexedColor();

    public void setLabel(String label);

    /**
     * Return the label of the graph item.
     * 
     * @return label of the graph item.
     */
    public String getLabel();

    /**
     * Add a Listener to be notified of changes.
     * 
     * @param l
     *            the listener to be added.
     */
    public void addGraphItemListener(GraphItemListener l);

    /**
     * Remove the listener from the notification list.
     * 
     * @param l
     *            the listener to be removed.
     */
    public void removeGraphItemListener(GraphItemListener l);

    /**
     * Draw the graph item on the graph display.
     * 
     * @param c
     *            the component upon which to draw.
     * @param g
     *            the graphics context.
     * @param r
     *            the area of the graph within the component.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     */
    public void draw(Component c, Graphics g, Rectangle r, Axis xAxis,
	    Axis yAxis);

    /**
     * Returns whether this graph item intersects the given rectangle.
     * 
     * @param r
     *            the area to test for intersection.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     * @return true if the item intersects the given rectangle, else false.
     */
    public boolean intersects(Rectangle r, Axis xAxis, Axis yAxis);

    /**
     * Returns whether this graph item intersects the given point.
     * 
     * @param p
     *            the point to test for intersection.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     * @return true if the item intersects the given point, else false.
     */
    public boolean intersects(Point p, Axis xAxis, Axis yAxis);

    /**
     * Returns whether this graph item intersects the given point.
     * 
     * @param p
     *            the point to test for intersection.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     * @return indices of datapoints that intersects the given point, else null.
     */
    public int[] getIndicesAt(Point p, Axis xAxis, Axis yAxis);
}
