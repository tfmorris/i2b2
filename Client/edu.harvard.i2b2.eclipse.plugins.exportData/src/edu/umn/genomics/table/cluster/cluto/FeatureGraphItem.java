/*
 * @(#) $RCSfile: FeatureGraphItem.java,v $ $Revision: 1.3 $ $Date: 2008/11/13 18:06:50 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster.cluto;

import java.awt.*;
import java.awt.geom.*;
import edu.umn.genomics.graph.*;

/**
 * Draws bars on the Cluster graph which represent columns that are identified
 * as defining or discrimating features by a cluto clustering solution.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/13 18:06:50 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class FeatureGraphItem extends AbstractGraphItem {
    int[] cols = null;
    Color color = Color.black;
    double stripWidth = 1.;
    double barOffset = 0.;
    double barHeight = 1.;

    /**
     * Graph bars for featured columns.
     * 
     * @param cols
     *            an array of column indices
     */
    public FeatureGraphItem(int[] cols) {
	this.cols = cols;
    }

    /**
     * Graph bars for featured columns.
     * 
     * @param cols
     *            an array of column indices
     * @param barOffset
     *            the amount (0 - 1) to offset the bar from the baseline
     * @param barHeight
     *            the size of the bar (0 - 1) relative to the height of the
     *            graph
     */
    public FeatureGraphItem(int[] cols, double barOffset, double barHeight) {
	this.barOffset = barOffset;
	this.barHeight = barHeight;
	this.cols = cols;
    }

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
    @Override
    public void draw(Component c, Graphics g, Rectangle r, Axis xAxis,
	    Axis yAxis) {
	Color prevColor = g.getColor();
	g.setColor(color);
	double w = stripWidth;
	double h = r.height * barHeight;
	double x = 0.;
	double y = r.height - h - (barOffset * r.height);
	Rectangle2D.Double rect = new Rectangle2D.Double();
	for (int i = 0; i < cols.length - 1; i++) {
	    x = xAxis.getPosition(cols[i] - stripWidth * .5);
	    w = xAxis.getPosition(cols[i] + stripWidth * .5)
		    - xAxis.getPosition(cols[i] - stripWidth * .5);
	    rect.setRect(x, y, w, h);
	    if (indexedColor != null) {
		g.setColor(indexedColor.getColorAt(i));
	    }
	    ((Graphics2D) g).fill(rect);
	}
	g.setColor(prevColor);
    }

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
    @Override
    public boolean intersects(Rectangle r, Axis xAxis, Axis yAxis) {
	double w = xAxis.getPosition(stripWidth) - xAxis.getPosition(0.);
	double h = r.height * barHeight;
	double x = 0.;
	double y = r.height - h - (barOffset * r.height);
	Rectangle2D.Double rect = new Rectangle2D.Double();
	for (int i = 0; i < cols.length - 1; i++) {
	    x = xAxis.getPosition(cols[i] - stripWidth * .5);
	    w = xAxis.getPosition(cols[i] + stripWidth * .5)
		    - xAxis.getPosition(cols[i] - stripWidth * .5);
	    rect.setRect(x, y, w, h);
	    if (rect.intersects(r)) {
		return true;
	    }
	}
	return false;
    }

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
    @Override
    public boolean intersects(Point p, Axis xAxis, Axis yAxis) {
	for (int i = 0; i < cols.length - 1; i++) {
	    double x = xAxis.getPosition(cols[i] - stripWidth * .5);
	    double x1 = xAxis.getPosition(cols[i] + stripWidth * .5);
	    if (p.x >= x && p.x <= x1) {
		return true;
	    }
	}
	return false;
    }

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
    @Override
    public int[] getIndicesAt(Point p, Axis xAxis, Axis yAxis) {
	for (int i = 0; i < cols.length - 1; i++) {
	    double x = xAxis.getPosition(cols[i] - stripWidth * .5);
	    double x1 = xAxis.getPosition(cols[i] + stripWidth * .5);
	    if (p.x >= x && p.x <= x1) {
		int[] hit = new int[1];
		hit[0] = i;
		return hit;
	    }
	}
	return new int[0];
    }

}
