/*
 * @(#) $RCSfile: SimpleGraph.java,v $ $Revision: 1.4 $ $Date: 2008/10/30 18:02:40 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.layout.BordersLayout;

/**
 * A graph container containing a Graph display and optional axis displays along
 * any border.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/30 18:02:40 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see GraphItem
 * @see Axis
 */
public class SimpleGraph extends JPanel implements Graph {
    GraphDisplay graph;
    Axis axisX;
    Axis axisY;
    AxisDisplay laxis;
    AxisDisplay raxis;
    AxisDisplay taxis;
    AxisDisplay baxis;

    private MouseAdapter gma = new MouseAdapter() {
	@Override
	public void mouseEntered(MouseEvent e) {
	    e.getComponent().requestFocus();
	}
    };

    private final static String logAxisToggle = "logAxisToggle";
    private final static String invertAxisToggle = "invertAxisToggle";
    // toggle between log and linear axis
    private AbstractAction logToggleAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    AxisDisplay axisDisplay = (AxisDisplay) e.getSource();
	    Axis axis = axisDisplay.getAxis();
	    if (axisDisplay == laxis || axisDisplay == raxis) {
		if (axis instanceof LinearAxis) {
		    setYAxis(new LogAxis(axis));
		} else {
		    setYAxis(new LinearAxis(axis));
		}
	    } else if (axisDisplay == taxis || axisDisplay == baxis) {
		if (axis instanceof LinearAxis) {
		    setXAxis(new LogAxis(axis));
		} else {
		    setXAxis(new LinearAxis(axis));
		}
	    }
	    repaint();
	}
    };
    private AbstractAction invertAxisAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    AxisDisplay axisDisplay = (AxisDisplay) e.getSource();
	    Axis axis = axisDisplay.getAxis();
	    Axis newAxis = (Axis) axis.clone();
	    newAxis.setMin(axis.getMax());
	    newAxis.setMax(axis.getMin());
	    if (axisDisplay == laxis || axisDisplay == raxis) {
		setYAxis(newAxis);
	    } else if (axisDisplay == taxis || axisDisplay == baxis) {
		setXAxis(newAxis);
	    }
	    repaint();
	}
    };

    private void addLogToggle(AxisDisplay axisD) {
	axisD.getInputMap().put(
		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
		logAxisToggle);
	axisD.getActionMap().put(logAxisToggle, logToggleAction);
	axisD.getInputMap().put(
		KeyStroke.getKeyStroke(KeyEvent.VK_I, 0, false),
		invertAxisToggle);
	axisD.getActionMap().put(invertAxisToggle, invertAxisAction);
    }

    public final static String ZOOM_IN = AxisDisplay.ZOOM_IN;
    public final static String ZOOM_OUT = AxisDisplay.ZOOM_OUT;
    public final static String PAN_UP_OR_LEFT = AxisDisplay.PAN_UP_OR_LEFT;
    public final static String PAN_DOWN_OR_RIGHT = AxisDisplay.PAN_DOWN_OR_RIGHT;

    public void alterView(String command) {
	int p1 = 0, p2 = 0;
	double aspect = 1.;
	if (command == ZOOM_IN) {
	    p1 = 1;
	    p2 = -2;
	    aspect = (double) graph.getWidth() / (double) graph.getHeight();
	} else if (command == ZOOM_OUT) {
	    p1 = -1;
	    p2 = 0;
	    aspect = (double) graph.getWidth() / (double) graph.getHeight();
	} else if (command == PAN_UP_OR_LEFT) {
	    p1 = -1;
	    p2 = -2;
	} else if (command == PAN_DOWN_OR_RIGHT) {
	    p1 = 1;
	    p2 = 0;
	} else {
	    return;
	}
	for (int i = 0; i < 4; i++) {
	    AxisDisplay axisD = i == 0 ? laxis : i == 1 ? raxis
		    : i == 2 ? taxis : i == 3 ? baxis : null;
	    if (axisD != null && axisD.getZoomable()) {
		double adj = i < 2 ? 1. : aspect;
		Axis axis = axisD.getAxis();
		double min = axis.getValue(0);
		double max = axis.getValue(axis.getSize() - 1);
		double dmin = min - axis.getValue(p1);
		double dmax = max - axis.getValue(axis.getSize() + p2);
		axis.setMin(min - dmin * adj);
		axis.setMax(max - dmax * adj);
	    }
	}
	repaint();
    }

    // move one pixel up or left
    private AbstractAction panUpLeftAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    alterView(PAN_UP_OR_LEFT);
	}
    };
    // move one pixel down or right
    private AbstractAction panDownRightAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    alterView(PAN_DOWN_OR_RIGHT);
	}
    };
    // zoom in one pixel on each edge
    private AbstractAction zoominAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    alterView(ZOOM_IN);
	}
    };
    // zoom out one pixel on each edge
    private AbstractAction zoomoutAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    alterView(ZOOM_OUT);
	}
    };

    private void addGraphKeys(GraphDisplay graph) {
	graph.addMouseListener(gma);
	graph.getInputMap().put(
		KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), ZOOM_OUT);
	graph.getActionMap().put(ZOOM_OUT, zoomoutAction);
	graph.getInputMap().put(
		KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), ZOOM_IN);
	graph.getActionMap().put(ZOOM_IN, zoominAction);
	graph.getInputMap().put(
		KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false),
		PAN_UP_OR_LEFT);
	graph.getActionMap().put(PAN_UP_OR_LEFT, panUpLeftAction);
	graph.getInputMap().put(
		KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false),
		PAN_DOWN_OR_RIGHT);
	graph.getActionMap().put(PAN_DOWN_OR_RIGHT, panDownRightAction);
    }

    /**
     * Create a graph with a graph display and axis displays along the left and
     * bottom borders.
     */
    public SimpleGraph() {
	setLayout(new BordersLayout());
	graph = new GraphDisplay();
	addGraphKeys(graph);
	axisX = graph.getXAxis();
	axisY = graph.getYAxis();
	add(graph, BordersLayout.CENTER);
	showAxis(BordersLayout.WEST, true);
	showAxis(BordersLayout.SOUTH, true);
    }

    /**
     * Whether to display an axis along the given border.
     * 
     * @param borderPosition
     *            the border along the graph display
     * @param show
     *            whether the axis should be displayed at the given position.
     * @see edu.umn.genomics.layout.BordersLayout#NORTH
     * @see edu.umn.genomics.layout.BordersLayout#SOUTH
     * @see edu.umn.genomics.layout.BordersLayout#EAST
     * @see edu.umn.genomics.layout.BordersLayout#WEST
     */
    public void showAxis(String borderPosition, boolean show) {
	if (BordersLayout.WEST.equals(borderPosition)) {
	    if (show) {
		laxis = new AxisDisplay(axisY, AxisComponent.LEFT);
		addLogToggle(laxis);
		add(laxis, BordersLayout.WEST);
		validate();
	    } else {
		if (laxis != null) {
		    remove(laxis);
		    laxis = null;
		    validate();
		}
	    }
	} else if (BordersLayout.SOUTH.equals(borderPosition)) {
	    if (show) {
		baxis = new AxisDisplay(axisX, AxisComponent.BOTTOM);
		addLogToggle(baxis);
		add(baxis, BordersLayout.SOUTH);
		validate();
	    } else {
		if (baxis != null) {
		    remove(baxis);
		    baxis = null;
		    validate();
		}
	    }
	} else if (BordersLayout.EAST.equals(borderPosition)) {
	    if (show) {
		raxis = new AxisDisplay(axisY, AxisComponent.RIGHT);
		addLogToggle(raxis);
		add(raxis, BordersLayout.EAST);
		validate();
	    } else {
		if (raxis != null) {
		    remove(raxis);
		    raxis = null;
		    validate();
		}
	    }
	} else if (BordersLayout.NORTH.equals(borderPosition)) {
	    if (show) {
		taxis = new AxisDisplay(axisX, AxisComponent.TOP);
		addLogToggle(taxis);
		add(taxis, BordersLayout.NORTH);
		validate();
	    } else {
		if (taxis != null) {
		    remove(taxis);
		    taxis = null;
		    validate();
		}
	    }
	}
    }

    /**
     * Return the AxisDisplay at the given position.
     * 
     * @param borderPosition
     *            the border along the graph display
     * @return the AxisDisplay at the given position.
     * @see edu.umn.genomics.layout.BordersLayout#NORTH
     * @see edu.umn.genomics.layout.BordersLayout#SOUTH
     * @see edu.umn.genomics.layout.BordersLayout#EAST
     * @see edu.umn.genomics.layout.BordersLayout#WEST
     */
    public AxisDisplay getAxisDisplay(String borderPosition) {
	if (BordersLayout.WEST.equals(borderPosition)) {
	    return laxis;
	} else if (BordersLayout.SOUTH.equals(borderPosition)) {
	    return baxis;
	} else if (BordersLayout.EAST.equals(borderPosition)) {
	    return raxis;
	} else if (BordersLayout.NORTH.equals(borderPosition)) {
	    return taxis;
	}
	return null;
    }

    /**
     * Return the graph component.
     * 
     * @return the graph component.
     */
    public GraphDisplay getGraphDisplay() {
	return graph;
    }

    /**
     * Set the axis for the X dimension.
     * 
     * @param axisX
     */
    public void setXAxis(Axis axisX) {
	this.axisX = axisX;
	graph.setXAxis(axisX);
	if (baxis != null)
	    baxis.setAxis(axisX);
	if (taxis != null)
	    taxis.setAxis(axisX);
	repaint();
    }

    /**
     * Return the axis for the X dimension.
     * 
     * @return the X Axis
     */
    public Axis getXAxis() {
	return axisX;
    }

    /**
     * Set the axis for the Y dimension.
     * 
     * @param axisY
     */
    public void setYAxis(Axis axisY) {
	this.axisY = axisY;
	graph.setYAxis(axisY);
	if (laxis != null)
	    laxis.setAxis(axisY);
	if (raxis != null)
	    raxis.setAxis(axisY);
	repaint();
    }

    /**
     * Return the axis for the Y dimension.
     * 
     * @return the Y Axis
     */
    public Axis getYAxis() {
	return axisY;
    }

    /**
     * Add the graph item to the graph.
     * 
     * @param graphItem
     *            Item to be added to the graph.
     */
    public void addGraphItem(GraphItem graphItem) {
	graph.addGraphItem(graphItem);
    }

    /**
     * Remove the graph item from the graph.
     * 
     * @param graphItem
     *            Item to be removed from the graph.
     */
    public void removeGraphItem(GraphItem graphItem) {
	graph.removeGraphItem(graphItem);
    }

    /**
     * Return the graph items for this graph.
     * 
     * @return the graph items for this graph.
     */
    public GraphItem[] getGraphItems() {
	return graph.getGraphItems();
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
	Rectangle r = graph.getBounds();
	return graph.getValueAt(x - r.x, y - r.y);
    }

    /**
     * Whether to draw grid lines on the graph corresponding to tickmarks on the
     * axes.
     * 
     * @param show
     *            if true draw grid lines.
     */
    public void showGrid(boolean show) {
	graph.showGrid(show);
    }
}
