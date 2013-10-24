/*
 * @(#) $RCSfile: ClusterGraphItem.java,v $ $Revision: 1.3 $ $Date: 2008/11/07 18:52:53 $ $Name: RELEASE_1_3_1_0001b $
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
import jcluto.*;

/**
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/07 18:52:53 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClusterGraphItem extends AbstractGraphItem {

    ClutoMatrix matrix;
    int[] rows = null;
    Color color = Color.black;
    IndexedColor indexedColor = null;
    String label = null;

    public ClusterGraphItem(ClutoMatrix matrix, int[] rows) {
	this.rows = rows;
	this.matrix = matrix;
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
	ClusterGraphIterator pathIter = new ClusterGraphIterator(matrix, rows,
		xAxis, yAxis);
	GeneralPath genPath = new GeneralPath();
	genPath.append(pathIter, false);
	Color prevColor = g.getColor();
	g.setColor(color);
	((Graphics2D) g).draw(genPath);
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
	Line2D line = new Line2D.Float();
	ClusterGraphIterator pathIter = new ClusterGraphIterator(matrix, rows,
		xAxis, yAxis);
	float[] coord = new float[2];
	float x1 = 0f;
	float y1 = 0f;
	for (; !pathIter.isDone(); pathIter.next()) {
	    int move = pathIter.currentSegment(coord);
	    if (move == PathIterator.SEG_MOVETO) {
		x1 = coord[0];
		y1 = coord[1];
	    } else {
		line.setLine(x1, y1, coord[0], coord[1]);
		if (line.intersects(r)) {
		    return true;
		}
		x1 = coord[0];
		y1 = coord[1];
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
	Rectangle r = new Rectangle(p.x, p.y, 1, 1);
	Line2D line = new Line2D.Float();
	ClusterGraphIterator pathIter = new ClusterGraphIterator(matrix, rows,
		xAxis, yAxis);
	float[] coord = new float[2];
	float x1 = 0f;
	float y1 = 0f;
	for (; !pathIter.isDone(); pathIter.next()) {
	    int move = pathIter.currentSegment(coord);
	    if (move == PathIterator.SEG_MOVETO) {
		x1 = coord[0];
		y1 = coord[1];
	    } else {
		line.setLine(x1, y1, coord[0], coord[1]);
		if (line.intersects(r)) {
		    return true;
		}
		x1 = coord[0];
		y1 = coord[1];
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
	Rectangle r = new Rectangle(p.x, p.y, 1, 1);
	int[] rowhits = new int[rows.length];
	int cnt = 0;
	int ri = -1;
	Line2D line = new Line2D.Float();
	ClusterGraphIterator pathIter = new ClusterGraphIterator(matrix, rows,
		xAxis, yAxis);
	float[] coord = new float[2];
	float x1 = 0f;
	float y1 = 0f;
	for (; !pathIter.isDone(); pathIter.next()) {
	    int move = pathIter.currentSegment(coord);
	    if (move == PathIterator.SEG_MOVETO) {
		ri++;
		x1 = coord[0];
		y1 = coord[1];
	    } else {
		line.setLine(x1, y1, coord[0], coord[1]);
		if (line.intersects(r)) {
		    rowhits[cnt++] = ri;
		}
		x1 = coord[0];
		y1 = coord[1];
	    }
	}
	if (cnt < rows.length) {
	    int[] tmp = rowhits;
	    rowhits = new int[cnt];
	    if (cnt > 0) {
		System.arraycopy(tmp, 0, rowhits, 0, cnt);
	    }
	}
	return rowhits;
    }
}
