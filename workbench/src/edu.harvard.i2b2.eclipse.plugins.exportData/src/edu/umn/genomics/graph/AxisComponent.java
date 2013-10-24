/*
 * @(#) $RCSfile: AxisComponent.java,v $ $Revision: 1.3 $ $Date: 2008/10/17 19:00:57 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.event.*;

/**
 * An AxisComponent displays an axis that may be placed along the edge of a
 * Graph. The position of the axis component relative to the graph determines
 * where the tickmarks and labels are drawn.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/17 19:00:57 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface AxisComponent {
    /**
     * The axis component is positioned at the top of the graph.
     */
    public static final int TOP = 1;
    /**
     * The axis component is positioned at the left of the graph.
     */
    public static final int LEFT = 2;
    /**
     * The axis component is positioned at the bottom of the graph.
     */
    public static final int BOTTOM = 3;
    /**
     * The axis component is positioned at the rigt of the graph.
     */
    public static final int RIGHT = 4;

    /**
     * Set the axis for this display.
     * 
     * @param axis
     *            the axis to display.
     */
    public void setAxis(Axis axis);

    /**
     * Return the axis for this display.
     * 
     * @return the axis fo this display.
     */
    public Axis getAxis();

    /**
     * Set the position of this axis component relative to the graph component.
     * 
     * @param position
     *            the position relative to the graph component..
     */
    public void setPosition(int position);

    /**
     * Return the position of this axis component relative to the graph
     * component.
     * 
     * @return the position relative to the graph component..
     */
    public int getPosition();

    /**
     * Return the value of the axis at the given point on the axis component.
     * 
     * @param p
     *            the point on the axis component.
     * @return the value of the axis at the given point on the axis component.
     */
    public double getValueAt(Point p);

    /**
     * Return the value of the axis at the given point on the axis component.
     * 
     * @param x
     *            the x offsset on the axis component.
     * @param y
     *            the y offsset on the axis component.
     * @return the value of the axis at the given point on the axis component.
     */
    public double getValueAt(int x, int y);

    /**
     * Set the value formatter for the display.
     * 
     * @param axisLabeler
     *            formats labels for the values along the axis.
     */
    public void setAxisLabeler(AxisLabeler axisLabeler);

    /**
     * Return the value label formatter for the display.
     * 
     * @return the label formatter for the axis values.
     */
    public AxisLabeler getAxisLabeler();

    public final static int modifierMask = InputEvent.BUTTON1_MASK
	    | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK
	    | InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK
	    | InputEvent.ALT_MASK | InputEvent.META_MASK;

}
