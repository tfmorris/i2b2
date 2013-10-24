/*
 * @(#) $RCSfile: AxisDisplay.java,v $ $Revision: 1.4 $ $Date: 2008/10/30 18:02:40 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * An AxisComponent displays an axis that may be placed along the edge of a
 * Graph. The position of the axis component relative to the graph determines
 * where the tickmarks and labels are drawn.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/30 18:02:40 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class AxisDisplay extends JComponent implements AxisComponent {
    Axis axis;
    int position = BOTTOM;
    Dimension layoutDims = null;
    Rectangle dataArea = null;
    String jver = System.getProperty("java.version");
    String pattern = jver.indexOf("1.1") > 0 ? "" : ".####";
    AxisLabeler labeler = new DecimalLabeler(pattern);
    int tickLen = 4;
    Point p1, p2;
    boolean iszoomable = false;
    final static int ZOOMNOT = 0;
    final static int ZOOMIN = 1;
    final static int ZOOMPAN = 2;
    final static int ZOOMOUT = 3;
    int zoomOp = 0;
    AxisListener listener = new AxisListener() {
	public void axisChanged(AxisEvent e) {
	    repaint();
	}
    };

    private MouseMotionAdapter mma = new MouseMotionAdapter() {
	@Override
	public void mouseDragged(MouseEvent e) {
	    p2 = e.getPoint();
	    boolean hrz = position == BOTTOM || position == TOP;
	    int h = getHeight();
	    int v1 = hrz ? p1.x : h - p1.y;
	    int v2 = hrz ? p2.x : h - p2.y;
	    if (v2 != v1) {
		double mn = getAxis().getValue(Math.min(v1, v2));
		double mx = getAxis().getValue(Math.max(v1, v2));
		int evtMask = e.getModifiers() & modifierMask;
		// Btn2 or ShiftBtn1
		if ((evtMask == InputEvent.BUTTON2_MASK)
			|| (evtMask == (InputEvent.SHIFT_MASK | InputEvent.BUTTON1_MASK))
			|| (evtMask == InputEvent.SHIFT_MASK)) { // Mac
		    // OneButtonMouse
		    double diff = (getAxis().getValue(v1) - getAxis().getValue(
			    v2));
		    getAxis().setMin(getAxis().getMin() + diff);
		    getAxis().setMax(getAxis().getMax() + diff);
		    p1 = p2;
		}
	    }
	    repaint();
	}
    };
    private MouseAdapter ma = new MouseAdapter() {
	@Override
	public void mouseEntered(MouseEvent e) {
	    requestFocus();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    p1 = e.getPoint();
	    p2 = p1;
	    int evtMask = e.getModifiers() & modifierMask;
	    if ((evtMask == InputEvent.BUTTON1_MASK) || evtMask == 0) { // Mac
		// OneButtonMouse
		zoomOp = ZOOMIN;
	    } else if ((evtMask == InputEvent.BUTTON2_MASK)
		    || (evtMask == (InputEvent.SHIFT_MASK | InputEvent.BUTTON1_MASK))
		    || (evtMask == InputEvent.SHIFT_MASK)) { // Mac
		// OneButtonMouse
		zoomOp = ZOOMPAN;
	    } else if ((evtMask == InputEvent.BUTTON3_MASK)
		    || (evtMask == (InputEvent.CTRL_MASK | InputEvent.BUTTON1_MASK))
		    || (evtMask == (InputEvent.CTRL_MASK | InputEvent.BUTTON2_MASK))
		    || // Mac
		    (evtMask == InputEvent.CTRL_MASK || evtMask == InputEvent.META_MASK)) { // Mac
		// OneButtonMouse
		zoomOp = ZOOMOUT;
	    }
	    repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    p2 = e.getPoint();
	    boolean hrz = position == BOTTOM || position == TOP;
	    int h = getHeight();
	    int v1 = hrz ? p1.x : h - p1.y;
	    int v2 = hrz ? p2.x : h - p2.y;
	    if (v2 != v1) {
		double mn = getAxis().getValue(Math.min(v1, v2));
		double mx = getAxis().getValue(Math.max(v1, v2));
		int evtMask = e.getModifiers() & modifierMask;
		if ((evtMask == InputEvent.BUTTON1_MASK) || evtMask == 0) { // Mac
		    // OneButtonMouse
		    getAxis().setMin(mn);
		    getAxis().setMax(mx);
		} else if ((evtMask == InputEvent.BUTTON2_MASK)
			|| (evtMask == (InputEvent.SHIFT_MASK | InputEvent.BUTTON1_MASK))
			|| (evtMask == InputEvent.SHIFT_MASK)) { // Mac
		    // OneButtonMouse
		    double diff = (getAxis().getValue(v1) - getAxis().getValue(
			    v2));
		    getAxis().setMin(getAxis().getMin() + diff);
		    getAxis().setMax(getAxis().getMax() + diff);
		} else if ((evtMask == InputEvent.BUTTON3_MASK)
			|| (evtMask == (InputEvent.CTRL_MASK | InputEvent.BUTTON1_MASK))
			|| (evtMask == (InputEvent.CTRL_MASK | InputEvent.BUTTON2_MASK))
			|| // Mac
			(evtMask == InputEvent.CTRL_MASK || evtMask == InputEvent.META_MASK)) { // Mac
		    // OneButtonMouse
		    double aMin = getAxis().getMin();
		    double aMax = getAxis().getMax();
		    double f = (aMax - aMin) / (mx - mn);
		    getAxis().setMin(aMin - (mn - aMin) * f);
		    getAxis().setMax(aMax + (aMax - mx) * f);
		} else {
		    if (v2 < v1) {
		    } else {
		    }
		}
	    }
	    zoomOp = ZOOMNOT;
	    repaint();
	}
    };

    public final static String ZOOM_IN = "zoom in";
    public final static String ZOOM_OUT = "zoom out";
    public final static String PAN_UP_OR_LEFT = "pan up left";
    public final static String PAN_DOWN_OR_RIGHT = "pan down right";

    public void alterView(String command) {
	int p1 = 0, p2 = 0;
	if (command == ZOOM_IN) {
	    p1 = 1;
	    p2 = -2;
	} else if (command == ZOOM_OUT) {
	    p1 = -1;
	    p2 = 0;
	} else if (command == PAN_UP_OR_LEFT) {
	    p1 = -1;
	    p2 = -2;
	} else if (command == PAN_DOWN_OR_RIGHT) {
	    p1 = 1;
	    p2 = 0;
	} else {
	    return;
	}
	Axis axis = getAxis();
	double min = axis.getValue(p1);
	double max = axis.getValue(axis.getSize() + p2);
	axis.setMin(min);
	axis.setMax(max);
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

    /**
     * Create an AxisDisplay with position defaulting to AxisComponent.BOTTOM.
     */
    public AxisDisplay() {
	setAxis(new LinearAxis());
    }

    /**
     * Create an AxisDisplay with the given axis and default position of
     * AxisComponent.BOTTOM.
     * 
     * @param axis
     *            the axis to display
     */
    public AxisDisplay(Axis axis) {
	setAxis(axis);
    }

    /**
     * Create an AxisDisplay with the given axis and default position of
     * AxisComponent.BOTTOM.
     * 
     * @param axis
     *            the axis to display
     * @param position
     *            position of the axis display releative to the graph.
     */
    public AxisDisplay(Axis axis, int position) {
	setPosition(position);
	setAxis(axis);
    }

    /**
     * Set the axis for this display.
     * 
     * @param axis
     *            the axis to display.
     */
    public void setAxis(Axis axis) {
	if (this.axis != null)
	    this.axis.removeAxisListener(listener);
	this.axis = axis;
	this.axis.addAxisListener(listener);
	repaint();
    }

    /**
     * Return the axis for this display.
     * 
     * @return the axis fo this display.
     */
    public Axis getAxis() {
	return axis;
    }

    /**
     * Set whether to allow zooming and panning on this axis.
     * 
     * @param zoom
     *            if true allow zooming else axis is fixed.
     */
    public void setZoomable(boolean zoom) {
	if (zoom && !iszoomable) {
	    addMouseListener(ma);
	    addMouseMotionListener(mma);
	    getInputMap().put(
		    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
		    ZOOM_OUT);
	    getActionMap().put(ZOOM_OUT, zoomoutAction);
	    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
		    ZOOM_IN);
	    getActionMap().put(ZOOM_IN, zoominAction);
	    getInputMap().put(
		    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false),
		    PAN_UP_OR_LEFT);
	    getActionMap().put(PAN_UP_OR_LEFT, panUpLeftAction);
	    getInputMap().put(
		    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false),
		    PAN_DOWN_OR_RIGHT);
	    getActionMap().put(PAN_DOWN_OR_RIGHT, panDownRightAction);
	    iszoomable = true;
	} else if (iszoomable) {
	    removeMouseListener(ma);
	    removeMouseMotionListener(mma);
	    getActionMap().clear();
	    getInputMap().clear();
	    iszoomable = false;
	}
    }

    /**
     * Return whether zooming and panning is allowed on this axis.
     * 
     * @return if true allow zooming else axis is fixed.
     */
    public boolean getZoomable() {
	return iszoomable;
    }

    /**
     * Set the position of this axis component relative to the graph component.
     * 
     * @param position
     *            the position relative to the graph component..
     * @see AxisComponent#TOP
     * @see AxisComponent#BOTTOM
     * @see AxisComponent#LEFT
     * @see AxisComponent#RIGHT
     */
    public void setPosition(int position) {
	this.position = position;
	repaint();
    }

    /**
     * Return the position of this axis component relative to the graph
     * component.
     * 
     * @return the position relative to the graph component..
     */
    public int getPosition() {
	return position;
    }

    /**
     * Set the value formatter for the display.
     * 
     * @param axisLabeler
     *            formats labels for the values along the axis.
     */
    public void setAxisLabeler(AxisLabeler axisLabeler) {
	labeler = axisLabeler;
    }

    /**
     * Return the value label formatter for the display.
     * 
     * @return the label formatter for the axis values.
     */
    public AxisLabeler getAxisLabeler() {
	return labeler;
    }

    /**
     * Return the value of the axis at the given point on the axis component.
     * 
     * @param p
     *            the point on the axis component.
     * @return the value of the axis at the given point on the axis component.
     */
    public double getValueAt(Point p) {
	return getValueAt(p.x, p.y);
    }

    /**
     * Return the value of the axis at the given point on the axis component.
     * 
     * @param x
     *            the x offsset on the axis component.
     * @param y
     *            the y offsset on the axis component.
     * @return the value of the axis at the given point on the axis component.
     */
    public double getValueAt(int x, int y) {
	return getValueAt((double) x, (double) y);
    }

    /**
     * Return the value of the axis at the given point on the axis component.
     * 
     * @param x
     *            the x offsset on the axis component.
     * @param y
     *            the y offsset on the axis component.
     * @return the value of the axis at the given point on the axis component.
     */
    public double getValueAt(double x, double y) {
	double v;
	double p = position == LEFT || position == RIGHT ? getSize().height
		- getInsets().bottom - y : x;
	v = axis != null ? axis.getValue((int) p) : 0.;
	return v;
    }

    /**
     * Gets the preferred size of this component.
     * 
     * @return A dimension object indicating this component's preferred size.
     */
    @Override
    public Dimension getPreferredSize() {
	Dimension d = new Dimension();
	Insets insets = getInsets();
	if (axis != null) {
	    int gap = 2;
	    double ticks[] = axis.getTicks();
	    String max = "-9"; // an arbitrary digit
	    for (int i = 0; i < ticks.length; i += 2) {
		String tag = labeler.getLabel(ticks[i + 1]);
		if (tag.length() > max.length())
		    max = tag;
	    }
	    int tagwidth = 14 * max.length();
	    int tagheight = 14;
	    Font f = getFont();
	    if (f != null) {
		FontMetrics fm = getFontMetrics(f);
		if (fm != null) {
		    tagwidth = fm.stringWidth(max + "-");
		    tagheight = fm.getAscent() + fm.getDescent();
		}
	    }
	    switch (position) {
	    case TOP:
	    case BOTTOM:
		d.width = axis.getSize() + 2 + insets.left + insets.right;
		d.height = tagheight + tickLen + gap + insets.top
			+ insets.bottom;
		break;
	    case LEFT:
	    case RIGHT:
		d.width = tagwidth + tickLen + gap * 2 + insets.left
			+ insets.right;
		d.height = axis.getSize() + 2 + insets.top + insets.bottom;
		break;
	    }
	}
	return d;
    }

    /**
     * Paints this component.
     * 
     * @param g
     *            the graphics context.
     */
    @Override
    public void paintComponent(Graphics g) {
	// let super handle background
	super.paintComponent(g);
	Font font = g.getFont();
	FontMetrics fm = g.getFontMetrics(font);
	Dimension dim = getSize();
	Insets insets = getInsets();
	dataArea = new Rectangle(insets.left, insets.top, dim.width
		- insets.left - insets.right - 1, dim.height - insets.top
		- insets.bottom - 1);
	g.setColor(getForeground());
	// get axis tickmarks
	double ticks[] = axis.getTicks();
	// format axis labels
	// draw axis
	int xl = dataArea.x;
	int yt = dataArea.y;
	int xr = dataArea.x + dataArea.width;
	int yb = dataArea.y + dataArea.height;

	if (position == BOTTOM) { // xaxis
	    int sl = -1;
	    g.drawLine(xl, yt, xr, yt);
	    for (int i = 0; i < ticks.length; i += 2) {
		int x = xl + (int) Math.round(ticks[i]);
		g.drawLine(x, yt, x, yt + tickLen / 2);
		if (x > sl) {
		    String tag = labeler.getLabel(ticks[i + 1]);
		    sl = x + fm.stringWidth(tag);
		    if (sl < xr) {
			g.drawLine(x, yt, x, yt + tickLen);
			g.drawString(tag, x, yt + fm.getAscent() + tickLen);
		    }
		}
	    }
	} else if (position == LEFT) { // yaxis
	    int sl = yb + 1;
	    g.drawLine(xr, yt, xr, yb);
	    for (int i = 0; i < ticks.length; i += 2) {
		int y = yb - (int) Math.round(ticks[i]);
		g.drawLine(xr - tickLen / 2, y, xr, y);
		if (y < sl && y - fm.getAscent() >= 0) {
		    g.drawLine(xr - tickLen, y, xr, y);
		    String tag = labeler.getLabel(ticks[i + 1]);
		    sl = y - fm.getAscent() - fm.getDescent();
		    g.drawString(tag, xr - fm.stringWidth(tag) - tickLen, y);
		}
	    }
	} else if (position == TOP) { // xaxis
	    int sl = -1;
	    g.drawLine(xl, yb, xr, yb);
	    for (int i = 0; i < ticks.length; i += 2) {
		int x = xl + (int) Math.round(ticks[i]);
		g.drawLine(x, yb - tickLen / 2, x, yb);
		if (x > sl) {
		    String tag = labeler.getLabel(ticks[i + 1]);
		    sl = x + fm.stringWidth(tag);
		    if (sl < xr) {
			g.drawLine(x, yb - tickLen, x, yb);
			g.drawString(tag, x, yb - fm.getDescent() - tickLen);
		    }
		}
	    }
	} else if (position == RIGHT) { // yaxis
	    int sl = yb + 1;
	    g.drawLine(xl, yt, xl, yb);
	    for (int i = 0; i < ticks.length; i += 2) {
		int y = yb - (int) Math.round(ticks[i]);
		g.drawLine(xl, y, xl + tickLen / 2, y);
		if (y < sl && y - fm.getAscent() >= 0) {
		    String tag = labeler.getLabel(ticks[i + 1]);
		    sl = y - fm.getAscent() - fm.getDescent();
		    g.drawLine(xl, y, xl + tickLen, y);
		    g.drawString(tag, xl + tickLen, y);
		}
	    }
	}
	if (iszoomable && p1 != null && p2 != null) {
	    int mn;
	    int mx;
	    int zln;
	    if (position == TOP || position == BOTTOM) {
		mn = Math.min(p1.x, p2.x);
		mx = Math.max(p1.x, p2.x);
		zln = position == TOP ? yt + 3 : yb - 3;
		switch (zoomOp) {
		case ZOOMIN:
		    g.drawLine(xl, zln, xl + 3, zln + 3);
		    g.drawLine(xl, zln, xl + 3, zln - 3);
		    g.drawLine(xl, zln, mn, zln);
		    g.drawLine(mn, zln - 3, mn, zln + 3);
		    g.drawLine(xr, zln, xr - 3, zln + 3);
		    g.drawLine(xr, zln, xr - 3, zln - 3);
		    g.drawLine(xr, zln, mx, zln);
		    g.drawLine(mx, zln - 3, mx, zln + 3);
		    break;
		case ZOOMPAN:
		    g.drawLine(mx - 10, zln, mx - 10 + 3, zln + 3);
		    g.drawLine(mx - 10, zln, mx - 10 + 3, zln - 3);
		    g.drawLine(mx - 10, zln, mx + 10, zln);
		    g.drawLine(mx + 10, zln, mx + 10 - 3, zln + 3);
		    g.drawLine(mx + 10, zln, mx + 10 - 3, zln - 3);
		    break;
		case ZOOMOUT:
		    g.drawLine(xl, zln - 3, xl, zln + 3);
		    g.drawLine(xl, zln, mn, zln);
		    g.drawLine(mn, zln, mn - 3, zln + 3);
		    g.drawLine(mn, zln, mn - 3, zln - 3);
		    g.drawLine(xr, zln - 3, xr, zln + 3);
		    g.drawLine(xr, zln, mx, zln);
		    g.drawLine(mx, zln, mx + 3, zln + 3);
		    g.drawLine(mx, zln, mx + 3, zln - 3);
		    break;
		default:
		}
	    } else if (position == LEFT || position == RIGHT) {
		mn = Math.min(p1.y, p2.y);
		mx = Math.max(p1.y, p2.y);
		zln = position == LEFT ? xl + 3 : xr - 3;
		switch (zoomOp) {
		case ZOOMIN:
		    g.drawLine(zln - 3, yt + 3, zln, yt);
		    g.drawLine(zln + 3, yt + 3, zln, yt);
		    g.drawLine(zln, yt, zln, mn);
		    g.drawLine(zln - 3, mn, zln + 3, mn);
		    g.drawLine(zln - 3, yb - 3, zln, yb);
		    g.drawLine(zln + 3, yb - 3, zln, yb);
		    g.drawLine(zln, yb, zln, mx);
		    g.drawLine(zln - 3, mx, zln + 3, mx);
		    break;
		case ZOOMPAN:
		    g.drawLine(zln - 3, mx - 10 + 3, zln, mx - 10);
		    g.drawLine(zln + 3, mx - 10 + 3, zln, mx - 10);
		    g.drawLine(zln, mx - 10, zln, mx + 10);
		    g.drawLine(zln - 3, mx + 10 - 3, zln, mx + 10);
		    g.drawLine(zln + 3, mx + 10 - 3, zln, mx + 10);
		    g.drawLine(zln, mx + 10, zln, mx);
		    break;
		case ZOOMOUT:
		    g.drawLine(zln - 3, yt, zln + 3, yt);
		    g.drawLine(zln, yt, zln, mn);
		    g.drawLine(zln - 3, mn - 3, zln, mn);
		    g.drawLine(zln + 3, mn - 3, zln, mn);
		    g.drawLine(zln - 3, yb, zln + 3, yb);
		    g.drawLine(zln, yb, zln, mx);
		    g.drawLine(zln - 3, mx + 3, zln, mx);
		    g.drawLine(zln + 3, mx + 3, zln, mx);
		    break;
		default:
		}
	    }
	}
    }

}
