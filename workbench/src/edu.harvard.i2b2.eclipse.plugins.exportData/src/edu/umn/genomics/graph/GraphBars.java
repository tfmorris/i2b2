/*
 * @(#) $RCSfile: GraphBars.java,v $ $Revision: 1.4 $ $Date: 2008/10/27 20:18:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * GraphBars draws a bar graph of the data values on a graph.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/27 20:18:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see DataModel
 */
public class GraphBars extends AbstractGraphItem {
    int points[][] = new int[2][];
    double barWidth = Double.NaN;
    int bw = 2;
    int xi[] = null;
    AxisLabeler labeler;

    /**
     * Create a bar graph.
     */
    public GraphBars() {
    }

    /**
     * Set the preferred bar width in axis space.
     * 
     * @param barWidth
     *            the preferred bar width in axis space.
     */
    public void setBarWidth(double barWidth) {
	this.barWidth = barWidth;
	if (axes[0] != null) {
	    bw = Math.abs(axes[0].getIntPosition(barWidth)
		    - axes[0].getIntPosition(0.));
	}
	fireChangeEvent();
    }

    /**
     * Get the preferred bar width in pixels.
     * 
     * @return the preferred bar width in pixels.
     */
    public double getBarWidth() {
	return barWidth;
    }

    /**
     * Set the labeler to generate labels over each bar.
     * 
     * @param barLabeler
     *            the labeler to generate labels over each bar.
     */
    public void setBarLabeler(AxisLabeler barLabeler) {
	this.labeler = barLabeler;
    }

    /**
     * Get the labeler for generating labels over each bar.
     * 
     * @return the labeler for generating labels over each bar.
     */
    public AxisLabeler getBarLabeler() {
	return labeler;
    }

    /**
     * The values in the index array ia are sorted by the values in the given
     * array a.
     * 
     * @param a
     *            the array for which to generate a sorted index.
     * @param ia
     *            the sorted index generated for array a.
     * @param lo0
     *            the lo index of the range to sort.
     * @param hi0
     *            the high index of the range to sort.
     */
    private static void qsort(int a[], int ia[], int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	int mid;
	if (hi0 > lo0) {
	    /*
	     * Arbitrarily establishing partition element as the midpoint of the
	     * array.
	     */
	    mid = (lo0 + hi0) / 2;
	    int mo = a[ia[mid]];
	    // loop through the array until indices cross
	    while (lo <= hi) {
		/*
		 * find the first element that is greater than or equal to the
		 * partition element starting from the left Index.
		 */
		while ((lo < hi0) && (a[ia[lo]] < mo))
		    ++lo;
		/*
		 * find an element that is smaller than or equal to the
		 * partition element starting from the right Index.
		 */
		while ((hi > lo0) && (a[ia[hi]] > mo))
		    --hi;
		if (lo <= hi) {
		    // if the indexes have not crossed, swap
		    if (lo < hi) {
			int tmp = ia[lo];
			ia[lo] = ia[hi];
			ia[hi] = tmp;
		    }
		    ++lo;
		    --hi;
		}
	    }
	    /*
	     * If the right index has not reached the left side of array must
	     * now sort the left partition.
	     */
	    if (lo0 < hi)
		qsort(a, ia, lo0, hi);
	    /*
	     * If the left index has not reached the right side of array must
	     * now sort the right partition.
	     */
	    if (lo < hi0)
		qsort(a, ia, lo, hi0);
	}
    }

    /**
     * Return a sorted index for the x axis positions of the bars.
     * 
     * @return a sorted index for the x axis positions of the bars.
     */
    public int[] getSortIndex() {
	int a[] = points[0];
	int idx[] = new int[a.length];
	for (int i = 0; i < idx.length; i++)
	    idx[i] = i;
	qsort(a, idx, 0, a.length - 1);
	return idx;
    }

    /**
     * Return a barwidth that it is not larger than the preferred bar width and
     * that has a space between each bar if possible.
     * 
     * @return a barwidth.
     */
    private int calcBarWidth() {
	int bw = 0;
	if (points != null && points.length >= 1 && points[0] != null
		&& points[0].length > 0) {
	    if (points[0].length == 1) {
		if (axes[0] != null) {
		    bw = (int) (Math.min((double) axes[0].getSize()
			    - points[0][0], points[0][0]) * .8);
		}
	    } else {
		if (axes[0] != null) {
		    bw = (int) (axes[0].getSize() * .8 / points[0].length);
		}
		int xl;
		int xr;
		if (xi == null) {
		    for (int i = 0; i < points[0].length; i++) {
			xl = (i > 0 ? (points[0][i] - points[0][i - 1]) / 2
				: (points[0][i + 1] - points[0][i]) / 2);
			xr = (i < points[0].length - 1 ? (points[0][i + 1] - points[0][i]) / 2
				: (points[0][i] - points[0][i - 1]) / 2);
			if (xl + xr <= bw)
			    bw = xl + xr - 1;
		    }
		} else {
		    for (int i = 0; i < points[0].length; i++) {
			xl = (i > 0 ? (points[0][xi[i]] - points[0][xi[i - 1]]) / 2
				: (points[0][xi[i + 1]] - points[0][xi[i]]) / 2);
			xr = (i < points[0].length - 1 ? (points[0][xi[i + 1]] - points[0][xi[i]]) / 2
				: (points[0][xi[i]] - points[0][xi[i - 1]]) / 2);
			if (xl + xr <= bw)
			    bw = xl + xr - 1;
		    }
		}
	    }
	}
	return (int) (bw * .8);
    }

    public boolean debug = false;

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
	if (dataModel == null || g == null) {
	    return;
	}
	long t[] = new long[10];
	int w = 0;

	if (color != null) {
	    g.setColor(color);
	}
	t[w++] = System.currentTimeMillis();
	if (points == null || !r.equals(rect) || !xAxis.equals(axes[0])
		|| !yAxis.equals(axes[1])) {
	    rect = new Rectangle(r);
	    axes[0] = (Axis) xAxis.clone();
	    axes[1] = (Axis) yAxis.clone();
	    points = dataModel.getPoints(r.x, r.y, axes, points);
	    xi = null;
	    if (points != null && points.length >= 2 && points[0] != null
		    && points[1] != null && points[0].length > 0
		    && points[0].length == points[1].length) {
		for (int n = points[0][0], i = 1; i < points[0].length; n = points[0][i], i++) {
		    if (n > points[0][i]) {
			xi = getSortIndex();
			break;
		    }
		}
	    }
	    t[w++] = System.currentTimeMillis();
	    if (Double.isNaN(barWidth)) {
		bw = calcBarWidth();
	    } else {
		bw = Math.abs(xAxis.getIntPosition(barWidth)
			- xAxis.getIntPosition(0.));
	    }
	    t[w++] = System.currentTimeMillis();
	}
	if (points != null && points.length >= 2 && points[0] != null
		&& points[1] != null && points[0].length == points[1].length) {
	    int yb = r.y + r.height;
	    int bl = bw / 2;
	    for (int i = 0; i < points[0].length; i++) {
		if (indexedColor != null) {
		    Color iColor = indexedColor.getColorAt(i);
		    if (iColor != null) {
			g.setColor(iColor);
		    } else if (color != null) {
			g.setColor(color);
		    }
		}
		if (bw < 2) {
		    g.drawLine(points[0][i], points[1][i], points[0][i], yb);
		} else if (bw < 3) {
		    g.drawRect(points[0][i] - bl, points[1][i], bw - 1, yb
			    - points[1][i]);
		} else {
		    g.fillRect(points[0][i] - bl, points[1][i], bw, yb
			    - points[1][i] + 1);
		}
	    }
	    t[w++] = System.currentTimeMillis();
	    if (labeler != null) {
		FontMetrics fm = g.getFontMetrics();
		int yd = fm.getMaxDescent();
		for (int i = 0; i < points[0].length; i++) {
		    double yv[] = dataModel.getYValues(i);
		    if (yv != null && yv.length > 0) {
			String lbl = labeler.getLabel(yv[0]);
			int xl = points[0][i] - fm.stringWidth(lbl) / 2;
			g.drawString(lbl, xl, points[1][i] - yd);
		    }
		}
	    }
	}
	t[w++] = System.currentTimeMillis();
	if (debug) {
	    for (int i = 1; i < w; i++)
		System.err.println(" " + i + ":\t" + (t[i] - t[i - 1]) + "\t"
			+ (t[i] - t[0]));
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
	    int yb = rect.y + rect.height - 1;
	    int bl = bw / 2;
	    int rr = r.x + r.width;
	    int rb = r.y + r.height;
	    for (int i = 0; i < npts; i++) {
		if (points[0][i] - bl <= rr && points[0][i] + bl >= r.x
			&& r.y <= yb && rb >= points[1][i]) {
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
	    int yb = rect.y + rect.height - 1;
	    int bl = bw / 2;
	    for (int i = 0; i < npts; i++) {
		if (p.x >= points[0][i] - bl && p.x <= points[0][i] + bl
			&& p.y <= yb && p.y >= points[1][i]) {
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
	    int yb = rect.y + rect.height - 1;
	    int bl = bw / 2;
	    for (int i = 0; i < npts; i++) {
		if (p.x >= points[0][i] - bl && p.x <= points[0][i] + bl
			&& p.y <= yb && p.y >= points[1][i]) {
		    tmp[cnt++] = i;
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
