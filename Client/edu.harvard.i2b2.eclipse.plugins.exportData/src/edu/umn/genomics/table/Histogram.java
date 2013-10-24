/*
 * @(#) $RCSfile: Histogram.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import edu.umn.genomics.graph.*;

/**
 * Display a hisotgram of the values.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class Histogram extends JComponent implements Serializable {
    int cnts[]; // count for each bucket of histogram

    Axis vaxis = null; // vertical axis for scaling counts

    boolean doSelectCounts = false;

    boolean selecting = false; // whether selecting

    Point start = null; // the starting point for selecting

    Point current = null; // the current point while selecting

    Color defaultColor = Color.blue;

    Color selectColor = Color.cyan;

    private MouseAdapter ma = new MouseAdapter() {
	@Override
	public void mousePressed(MouseEvent e) {
	    start = e.getPoint();
	    current = e.getPoint();
	    selecting = true;
	    repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    selecting = false;
	    current = e.getPoint();
	    repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	    start = e.getPoint();
	    current = e.getPoint();
	    repaint();
	}
    };

    private MouseMotionAdapter mma = new MouseMotionAdapter() {
	@Override
	public void mouseDragged(MouseEvent e) {
	    current = e.getPoint();
	    repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
    };

    /**
     * Construct a histogram display.
     */
    public Histogram() {
	addMouseListener(ma);
	addMouseMotionListener(mma);
    }

    /**
     * Set the histogram data to display.
     * 
     * @param counts
     *            the number of elements in each bucket of the histogram
     * @param withSelectCounts
     *            the counts include selected number in each bucket
     */
    public void setData(int counts[], boolean withSelectCounts) {
	this.cnts = counts;
	this.doSelectCounts = withSelectCounts;
	repaint();
    }

    /**
     * Set a vertical axis for scaling count bars.
     * 
     * @param axis
     *            Vertical axis for scaling bars
     */
    public void setVerticalAxis(Axis axis) {
	vaxis = axis;
    }

    /**
     * Return the vertical axis for scaling count bars.
     * 
     * @return Vertical axis for scaling bars
     */
    public Axis getVerticalAxis(Axis axis) {
	return vaxis;
    }

    /**
     * Return the range selected in relative values from 0 - 1.
     * 
     * @return the start and end positions of the selected range.
     */
    public double[] getSelectionRange() {
	Insets insets = getInsets();
	double sel[] = new double[2];
	double w = getWidth() - insets.left - insets.right;
	if (w >= 1. && start != null && current != null) {
	    sel[0] = (start.x - insets.left) / w;
	    sel[1] = (current.x - insets.left) / w;
	}
	return sel;
    }

    /**
     * Draw the histogram.
     * 
     * @param g
     *            the graphics context.
     */
    @Override
    public void paintComponent(Graphics g) {
	Insets insets = getInsets();
	int xl = insets.left;
	int xr = getWidth() - insets.right;
	int yt = insets.top;
	int yb = getHeight() - insets.bottom;
	int w = getWidth() - insets.left - insets.right;
	int h = getHeight() - insets.top - insets.bottom;
	if (isOpaque()) {
	    g.setColor(getBackground());
	    g.fillRect(xl, yt, w, h);
	}
	// draw axis
	g.setColor(Color.black);
	g.drawLine(xl, yb, xr, yb);
	int offset = doSelectCounts ? 2 : 1;
	if (cnts != null) {
	    double max = 0.; // the max count for any bucket
	    double min = 0.;
	    if (vaxis != null) {
		max = vaxis.getMax();
		min = vaxis.getMin();
	    } else {
		for (int i = 0; i < cnts.length; i += offset) {
		    if (cnts[i] > max) {
			max = cnts[i];
		    }
		}
	    }
	    double range = max - min;
	    // amount to scale the height of the bars
	    double scale = range > 0 ? (h - 2) / range : 1.;
	    // scale factor along the x axis
	    double xs = cnts.length > 1 ? (double) w / (cnts.length / offset)
		    : 0;
	    // draw axis
	    g.setColor(Color.black);
	    g.drawLine(xl, yb, xr, yb);
	    // draw histogram bars
	    for (int i = 0; i < cnts.length; i += offset) {
		int b = i / offset;
		if (cnts[i] > 0) {
		    int x1 = xl + (int) (b * xs); // left edge of bar
		    int x2 = xl + (int) ((b + 1) * xs) - 1;// right edge of bar
		    int y0 = yb - 1;
		    int y1 = y0 - (int) (min + cnts[i] * scale);
		    int y2 = y1;
		    if (doSelectCounts) {
			if (cnts[i] > 0) {
			    y1 = y0
				    - (int) (min + cnts[i]
					    * scale
					    * (1. - (double) cnts[i + 1]
						    / (double) cnts[i]));
			}
		    }
		    if (x2 - x1 < 3) { // represent as a line
			// non selected
			g.setColor(defaultColor);
			g.drawLine(x1, y0, x1, y1);
			// selected
			if (y1 != y2) {
			    g.setColor(selectColor);
			    g.drawLine(x1, y1, x1, y2);
			}
		    } else { // use a rectagle as the histogram bar
			// non selected
			g.setColor(defaultColor);
			g.fillRect(x1, y1, x2 - x1, yb - y1);
			if (y1 != y2) {
			    // selected
			    g.setColor(selectColor);
			    g.fillRect(x1, y2, x2 - x1, y1 - y2);
			}
		    }
		}
	    }
	}
	// draw lines showing the selection range
	if (selecting) {
	    if (start != null) {
		g.setColor(Color.black);
		g.drawLine(start.x, yt, start.x, yb);
	    }
	    if (current != null) {
		g.setColor(Color.black);
		g.drawLine(current.x, yt, current.x, yb);
	    }
	}
    }
}
