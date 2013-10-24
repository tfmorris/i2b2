/*
 * @(#) $RCSfile: GraphLine.java,v $ $Revision: 1.4 $ $Date: 2008/10/28 21:04:14 $ $Name: RELEASE_1_3_1_0001b $
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
 * GraphLine draws a line graph of the data values on a graph.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/28 21:04:14 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see DataModel
 */
public class GraphLine extends AbstractGraphItem {
    public int points[][] = new int[2][];

    /**
     * Create a line graph.
     */
    public GraphLine() {
    }

    /**
     * This method is typically called from the paint method of the graph to
     * draw the data points on the graph.
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
	if (dataModel == null) {
	    return;
	}
	if (color != null) {
	    g.setColor(color);
	}
	if (!r.equals(rect) || !xAxis.equals(axes[0]) || !yAxis.equals(axes[1])) {
	    rect = new Rectangle(r);
	    axes[0] = (Axis) xAxis.clone();
	    axes[1] = (Axis) yAxis.clone();
	    points = dataModel.getPoints(r.x, r.y, axes, points);
	}
	if (points != null && points.length == 2 && points[0] != null
		&& points[1] != null && points[0].length == points[1].length) {
	    if (indexedColor != null) {
		for (int i = 0; i < points[0].length - 1; i++) {
		    if (indexedColor != null) {
			Color iColor = indexedColor.getColorAt(i);
			if (iColor != null) {
			    g.setColor(iColor);
			} else if (color != null) {
			    g.setColor(color);
			}
			g.drawLine(points[0][i], points[1][i],
				points[0][i + 1], points[1][i + 1]);
		    }
		}
	    } else {
		g.drawPolyline(points[0], points[1], points[0].length);
	    }
	}
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
	if (points == null || !xAxis.equals(axes[0]) || !yAxis.equals(axes[1])) {
	    axes[0] = (Axis) xAxis.clone();
	    axes[1] = (Axis) yAxis.clone();
	    if (rect == null)
		return false;
	    points = dataModel.getPoints(rect.x, rect.y, axes, points);
	}
	if (points != null && points[0] != null && points[1] != null) {
	    int npts = points[0].length <= points[1].length ? points[0].length
		    : points[1].length;
	    int xr = r.x + r.width;
	    int yb = r.y + r.height;
	    // if a point is in the rectangle, return true
	    for (int i = 0; i < npts; i++) {
		if (points[0][i] >= r.x && points[0][i] < xr
			&& points[1][i] >= r.y && points[1][i] < yb) {
		    return true;
		}
	    }
	    // if a line crosses the rectangle, return true
	    npts -= 1;
	    for (int i = 0; i < npts; i++) {
		double m = (double) (points[1][i + 1] - points[1][i])
			/ (double) (points[0][i + 1] - points[0][i]);
		double dy;
		dy = m * (r.x - points[0][i]) + points[1][i];
		if (dy >= r.y && dy <= yb) {
		    return true;
		}
		dy = m * (xr - points[0][i]) + points[1][i];
		if (dy >= r.y && dy <= yb) {
		    return true;
		}
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
	if (points == null || !xAxis.equals(axes[0]) || !yAxis.equals(axes[1])) {
	    axes[0] = (Axis) xAxis.clone();
	    axes[1] = (Axis) yAxis.clone();
	    if (rect == null)
		return false;
	    points = dataModel.getPoints(rect.x, rect.y, axes, points);
	}
	if (points != null && points[0] != null && points[1] != null) {
	    int npts = points[0].length <= points[1].length ? points[0].length
		    : points[1].length;
	    // if the given point falls on a data point, return true
	    for (int i = 0; i < npts; i++) {
		if (points[0][i] == p.x && points[1][i] == p.y) {
		    return true;
		}
	    }
	    // if the given point falls on the line, return true
	    npts -= 1;
	    for (int i = 0; i < npts; i++) {
		if ((p.x > points[0][i] && p.x < points[0][i + 1] || p.x > points[0][i + 1]
			&& p.x < points[0][i])
			&& (p.y > points[1][i] && p.y < points[0][i + 1] || p.y > points[1][i + 1]
				&& p.y < points[0][i])) {
		    double m = (double) (points[1][i + 1] - points[1][i])
			    / (double) (points[0][i + 1] - points[0][i]);
		    double dy = m * (p.x - points[0][i]) + points[1][i];
		    if (Math.abs(p.y - dy) < 2.0) {
			return true;
		    }
		}
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
	if (points == null || !xAxis.equals(axes[0]) || !yAxis.equals(axes[1])) {
	    axes[0] = (Axis) xAxis.clone();
	    axes[1] = (Axis) yAxis.clone();
	    if (rect == null)
		return null;
	    points = dataModel.getPoints(rect.x, rect.y, axes, points);
	}
	if (points != null && points[0] != null && points[1] != null) {
	    int npts = points[0].length <= points[1].length ? points[0].length
		    : points[1].length;
	    int tmp[] = new int[npts];
	    int cnt = 0;
	    // if the given point falls on the line, return true
	    for (int i = 0; i < npts; i++) {
		if (points[0][i] == p.x && points[1][i] == p.y) {
		    tmp[cnt++] = i;
		} else if (i + 1 < npts) {
		    if ((p.x > points[0][i] && p.x < points[0][i + 1] || p.x > points[0][i + 1]
			    && p.x < points[0][i])
			    && (p.y > points[1][i] && p.y < points[0][i + 1] || p.y > points[1][i + 1]
				    && p.y < points[0][i])) {
			double m = (double) (points[1][i + 1] - points[1][i])
				/ (double) (points[0][i + 1] - points[0][i]);
			double dy = m * (p.x - points[0][i]) + points[1][i];
			if (Math.abs(p.y - dy) < 2.0) {
			    tmp[cnt++] = i;
			    tmp[cnt++] = ++i;
			}
		    }
		}
	    }
	    if (cnt > 0) {
		int ia[] = new int[cnt];
		System.arraycopy(tmp, 0, ia, 0, cnt);
		return ia;
	    } else {
		return null;
	    }
	}
	return null;
    }
}
