/*
 * @(#) $RCSfile: GraphDisplay.java,v $ $Revision: 1.4 $ $Date: 2008/10/30 18:02:40 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.graph.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import edu.umn.genomics.graph.*;

/**
 * A Graph displays items mapped to its axes.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/30 18:02:40 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see Graph
 * @see GraphItem
 * @see Axis
 */
@SuppressWarnings("serial")
public class GraphDisplay extends JComponent implements Graph {
    Vector graphItems = new Vector();
    Dimension layoutDims = null;
    Rectangle dataArea = null;
    Axis xAxis;
    Axis yAxis;
    boolean rubberbanding = false;

    boolean showGrid;
    Color gridColor;

    Point sPt = null;;
    Point ePt = null;;
    private MouseAdapter ma = new MouseAdapter() {
	@Override
	public void mousePressed(MouseEvent e) {
	    sPt = e.getPoint();
	    ePt = sPt;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    sPt = null;
	    ePt = null;
	    repaint();
	}
    };
    private MouseMotionAdapter mma = new MouseMotionAdapter() {
	@Override
	public void mouseDragged(MouseEvent e) {
	    ePt = e.getPoint();
	    repaint();
	}
    };

    private ComponentAdapter ca = new ComponentAdapter() {
	@Override
	public void componentResized(ComponentEvent e) {
	    Dimension dim = getSize();
	    Insets insets = getInsets();
	    Rectangle dataArea = new Rectangle(insets.left, insets.top,
		    dim.width - insets.left - insets.right - 1, dim.height
			    - insets.top - insets.bottom - 1);
	    if (xAxis.getSize() != dataArea.width - 2) {
		xAxis.setSize(dataArea.width);
	    }
	    if (yAxis.getSize() != dataArea.height - 2) {
		yAxis.setSize(dataArea.height);
	    }
	}
    };

    AxisListener axisListener = new AxisListener() {
	public void axisChanged(AxisEvent e) {
	    repaint();
	}
    };
    GraphItemListener graphItemListener = new GraphItemListener() {
	public void graphItemChanged(GraphItemEvent e) {
	    repaint();
	}
    };

    /**
     * Create a default graph display.
     */
    public GraphDisplay() {
	addComponentListener(ca);
	setXAxis(new LinearAxis());
	setYAxis(new LinearAxis());
	showRubberbanding(true);
    }

    /**
     * Set the axis for the X dimension.
     * 
     * @param xAxis
     */
    public void setXAxis(Axis xAxis) {
	if (this.xAxis != null)
	    this.xAxis.removeAxisListener(axisListener);
	this.xAxis = xAxis;
	this.xAxis.addAxisListener(axisListener);
	repaint();
    }

    /**
     * Return the axis for the X dimension.
     * 
     * @return the X Axis
     */
    public Axis getXAxis() {
	return xAxis;
    }

    /**
     * Set the axis for the Y dimension.
     * 
     * @param yAxis
     */
    public void setYAxis(Axis yAxis) {
	if (this.yAxis != null)
	    this.yAxis.removeAxisListener(axisListener);
	this.yAxis = yAxis;
	this.yAxis.addAxisListener(axisListener);
	repaint();
    }

    /**
     * Return the axis for the Y dimension.
     * 
     * @return the Y Axis
     */
    public Axis getYAxis() {
	return yAxis;
    }

    /**
     * Whether to draw grid lines on the graph corresponding to tickmarks on the
     * axes.
     * 
     * @param show
     *            if true draw grid lines.
     */
    public void showGrid(boolean show) {
	this.showGrid = show;
	repaint();
    }

    /**
     * Set the color for grid lines on the graph.
     * 
     * @param gridColor
     *            the color for grid lines on the graph.
     */
    public void setGridColor(Color gridColor) {
	this.gridColor = gridColor;
	repaint();
    }

    /**
     * Get the color for grid lines on the graph.
     * 
     * @return the color for grid lines on the graph.
     */
    public Color getGridColor() {
	return gridColor;
    }

    /**
     * Set whether to show the selecting rectangle on the display.
     * 
     * @param showRubberband
     *            whether to show the selecting rectangle on the display.
     */
    public void showRubberbanding(boolean showRubberband) {
	if (showRubberband && !rubberbanding) {
	    rubberbanding = true;
	    addMouseListener(ma);
	    addMouseMotionListener(mma);
	} else if (!showRubberband && rubberbanding) {
	    rubberbanding = false;
	    removeMouseListener(ma);
	    removeMouseMotionListener(mma);
	}
    }

    /**
     * Add the graph item to the graph.
     * 
     * @param graphItem
     *            Item to be added to the graph.
     */
    public void addGraphItem(GraphItem graphItem) {
	if (graphItem != null) {
	    graphItems.addElement(graphItem);
	    graphItem.addGraphItemListener(graphItemListener);
	}
	repaint();
    }

    /**
     * Remove the graph item from the graph.
     * 
     * @param graphItem
     *            Item to be removed from the graph.
     */
    public void removeGraphItem(GraphItem graphItem) {
	graphItems.removeElement(graphItem);
	graphItem.removeGraphItemListener(graphItemListener);
	repaint();
    }

    /**
     * Return the graph items for this graph.
     * 
     * @return the graph items for this graph.
     */
    public GraphItem[] getGraphItems() {
	GraphItem gia[] = new GraphItem[graphItems.size()];
	for (int i = 0; i < graphItems.size(); i++) {
	    gia[i] = (GraphItem) graphItems.elementAt(i);
	}
	return gia;
    }

    /**
     * Return the axis values for the given pixel location on this graph.
     * 
     * @param p
     *            The point location on the graph.
     * @return the x and y values for the given pixel location.
     */
    public double[] getValueAt(Point p) {
	return getValueAt(p.x, p.y);
    }

    /**
     * Return the axis values for the given pixel location on this graph.
     * 
     * @param x
     *            The x pixel location on the graph.
     * @param y
     *            The y pixel location on the graph.
     * @return the x and y values for the given pixel location.
     */
    public double[] getValueAt(int x, int y) {
	Insets insets = getInsets();
	double v[] = new double[2];
	v[0] = xAxis != null ? xAxis.getValue(x - insets.left) : 0.;
	v[1] = yAxis != null ? yAxis.getValue(getHeight() - y - insets.bottom)
		: 0.;
	return v;
    }

    /**
     * Return the axis values for the given pixel location on this graph.
     * 
     * @param x
     *            The x pixel location on the graph.
     * @param y
     *            The y pixel location on the graph.
     * @return the x and y values for the given pixel location.
     */
    public double[] getValueAt(double x, double y) {
	return getValueAt((int) x, (int) y);
    }

    /**
     * Called by the paint method to draw the graph and its graph items.
     * 
     * @param g
     *            the graphics context.
     */
    @Override
    public void paintComponent(Graphics g) {

	Dimension dim = getSize();
	Insets insets = getInsets();
	dataArea = new Rectangle(insets.left, insets.top, dim.width
		- insets.left - insets.right - 1, dim.height - insets.top
		- insets.bottom - 1);
	// background
	if (isOpaque()) {
	    g.setColor(getBackground());
	    g.fillRect(0, 0, dim.width, dim.height);
	}
	g.setColor(getForeground());
	// get axis tickmarks
	double xticks[] = xAxis.getTicks();
	double yticks[] = yAxis.getTicks();
	int yb = dataArea.y + dataArea.height;
	// draw grid
	if (showGrid) {
	    g
		    .setColor(gridColor != null ? gridColor : getBackground()
			    .darker());
	    // vertical x grid lines
	    for (int i = 0; i < xticks.length; i += 2) {
		int x = dataArea.x + (int) Math.round(xticks[i]);
		g.drawLine(x, dataArea.y, x, dataArea.y + dataArea.height);
	    }
	    // horizontal y grid lines
	    for (int i = 0; i < yticks.length; i += 2) {
		int y = yb - (int) Math.round(yticks[i]);
		g.drawLine(dataArea.x, y, dataArea.x + dataArea.width, y);
	    }
	}
	for (int i = 0; i < graphItems.size(); i++) {
	    ((GraphItem) graphItems.elementAt(i)).draw(this, g, dataArea,
		    xAxis, yAxis);
	}
	if (sPt != null && ePt != null) {
	    g.setColor(getForeground());
	    g.drawRect(Math.min(sPt.x, ePt.x), Math.min(sPt.y, ePt.y), Math
		    .abs(ePt.x - sPt.x), Math.abs(ePt.y - sPt.y));
	}
    }
}
