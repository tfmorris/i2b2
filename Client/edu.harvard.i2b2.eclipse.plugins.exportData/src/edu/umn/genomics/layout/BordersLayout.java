/*
 * @(#) $RCSfile: BordersLayout.java,v $ $Revision: 1.4 $ $Date: 2008/10/29 19:20:49 $ $Name: RELEASE_1_3_1_0001b $
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

/*
 * @(#)BordersLayout.java	1.42 99/06/08
 *
 * Copyright 1995-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package edu.umn.genomics.layout;

import java.awt.*;

/**
 * This is a modified version of java.awt.BorderLayout that adds corner
 * components. A border layout lays out a container, arranging and resizing its
 * components to fit in nine regions: north, south, east, west, northeast,
 * northwest, southeast, southwest, and center. Each region is identified by a
 * corresponding constant: <code>NORTH</code>, <code>SOUTH</code>,
 * <code>EAST</code>, <code>WEST</code>, <code>NORTHEAST</code>,
 * <code>NORTHWEST</code>, <code>SOUTHEAST</code>, <code>SOUTHWEST</code>, and
 * <code>CENTER</code>. When adding a * component to a container with a border
 * layout, use one of these nine constants, for example:
 * 
 * <pre>
 * Panel p = new Panel();
 * p.setLayout(new BordersLayout());
 * p.add(new Button(&quot;Okay&quot;), BordersLayout.SOUTH);
 * </pre>
 * 
 * As a convenience, BordersLayout interprets the absence of a string
 * specification the same as the constant <code>CENTER</code>:
 * 
 * <pre>
 * Panel p2 = new Panel();
 * p2.setLayout(new BordersLayout());
 * p2.add(new TextArea()); // Same as p.add(new TextArea(), BordersLayout.CENTER);
 * </pre>
 * <p>
 * The components are laid out according to their preferred sizes and the
 * constraints of the container's size. The <code>NORTH</code> and
 * <code>SOUTH</code> components may be stretched horizontally; the
 * <code>EAST</code> and <code>WEST</code> components may be stretched
 * vertically; the <code>CENTER</code> component may stretch both horizontally
 * and vertically to fill any space left over. Corners will match the dimension
 * of the adjoining border component.
 * <p>
 * 
 * @version $Revision: 1.4 $ $Date: 2008/10/29 19:20:49 $ $Name: RELEASE_1_3_1_0001b $
 * @author Arthur van Hoff
 * @see java.awt.Container#add(String, Component)
 * @see java.awt.ComponentOrientation
 * @since JDK1.0
 */
public class BordersLayout implements LayoutManager2, java.io.Serializable {
    /**
     * Constructs a border layout with the horizontal gaps between components.
     * The horizontal gap is specified by <code>hgap</code>.
     * 
     * @see #getHgap
     * @see #setHgap
     * 
     * @serial
     */
    int hgap;

    /**
     * Constructs a border layout with the vertical gaps between components. The
     * vertical gap is specified by <code>vgap</code>.
     * 
     * @see #getVgap
     * @see #setVgap
     * @serial
     */
    int vgap;

    /**
     * Constant to specify components location to be the north portion of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component north;
    /**
     * Constant to specify components location to be the west portion of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component west;
    /**
     * Constant to specify components location to be the east portion of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component east;
    /**
     * Constant to specify components location to be the south portion of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component south;
    /**
     * Constant to specify components location to be the northeast corner of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component northeast;
    /**
     * Constant to specify components location to be the northwest corner of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component northwest;
    /**
     * Constant to specify components location to be the southeast corner of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component southeast;
    /**
     * Constant to specify components location to be the southwest corner of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component southwest;

    /**
     * Constant to specify components location to be the center portion of the
     * border layout.
     * 
     * @serial
     * @see #addLayoutComponent
     * @see #getLayoutAlignmentX
     * @see #getLayoutAlignmentY
     * @see #removeLayoutComponent
     */
    Component center;

    /**
     * 
     * A relative positioning constant, that can be used instead of north,
     * south, east, west or center. mixing the two types of constants can lead
     * to unpredicable results. If you use both types, the relative constants
     * will take precedence. For example, if you add components using both the
     * <code>NORTH</code> and <code>BEFORE_FIRST_LINE</code> constants in a
     * container whose orientation is <code>LEFT_TO_RIGHT</code>, only the
     * <code>BEFORE_FIRST_LINE</code> will be layed out. This will be the same
     * for lastLine, firstItem, lastItem.
     * 
     * @serial
     */
    Component firstLine;
    /**
     * A relative positioning constant, that can be used instead of north,
     * south, east, west or center. Please read Description for firstLine.
     * 
     * @serial
     */
    Component lastLine;
    /**
     * A relative positioning constant, that can be used instead of north,
     * south, east, west or center. Please read Description for firstLine.
     * 
     * @serial
     */
    Component firstItem;
    /**
     * A relative positioning constant, that can be used instead of north,
     * south, east, west or center. Please read Description for firstLine.
     * 
     * @serial
     */
    Component lastItem;

    /**
     * The north layout constraint (top of container).
     */
    public static final String NORTH = java.awt.BorderLayout.NORTH;

    /**
     * The south layout constraint (bottom of container).
     */
    public static final String SOUTH = java.awt.BorderLayout.SOUTH;

    /**
     * The east layout constraint (right side of container).
     */
    public static final String EAST = java.awt.BorderLayout.EAST;

    /**
     * The west layout constraint (left side of container).
     */
    public static final String WEST = java.awt.BorderLayout.WEST;

    /**
     * The northeast layout constraint (top left of container).
     */
    public static final String NORTHEAST = "NORTHEAST";

    /**
     * The northwest layout constraint (top right of container).
     */
    public static final String NORTHWEST = "NORTHWEST";

    /**
     * The southeast layout constraint (bottom left of container).
     */
    public static final String SOUTHEAST = "SOUTHEAST";

    /**
     * The southwest layout constraint (bottom right of container).
     */
    public static final String SOUTHWEST = "SOUTHWEST";

    /**
     * The center layout constraint (middle of container).
     */
    public static final String CENTER = "Center";

    /**
     * The component comes before the first line of the layout's content. For
     * Western, top-to-bottom, left-to-right orientations, this is equivalent to
     * NORTH.
     * 
     * @see java.awt.Component#getComponentOrientation
     * @since 1.2
     */
    public static final String BEFORE_FIRST_LINE = "First";

    /**
     * The component comes after the last line of the layout's content. For
     * Western, top-to-bottom, left-to-right orientations, this is equivalent to
     * SOUTH.
     * 
     * @see java.awt.Component#getComponentOrientation
     * @since 1.2
     */
    public static final String AFTER_LAST_LINE = "Last";

    /**
     * The component goes at the beginning of the line direction for the layout.
     * For Western, top-to-bottom, left-to-right orientations, this is
     * equivalent to WEST.
     * 
     * @see java.awt.Component#getComponentOrientation
     * @since 1.2
     */
    public static final String BEFORE_LINE_BEGINS = "Before";

    /**
     * The component goes at the end of the line direction for the layout. For
     * Western, top-to-bottom, left-to-right orientations, this is equivalent to
     * EAST.
     * 
     * @see java.awt.Component#getComponentOrientation
     * @since 1.2
     */
    public static final String AFTER_LINE_ENDS = "After";

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -8658291919501921765L;

    /**
     * Constructs a new border layout with no gaps between components.
     */
    public BordersLayout() {
	this(0, 0);
    }

    /**
     * Constructs a border layout with the specified gaps between components.
     * The horizontal gap is specified by <code>hgap</code> and the vertical gap
     * is specified by <code>vgap</code>.
     * 
     * @param hgap
     *            the horizontal gap.
     * @param vgap
     *            the vertical gap.
     */
    public BordersLayout(int hgap, int vgap) {
	this.hgap = hgap;
	this.vgap = vgap;
    }

    /**
     * Returns the horizontal gap between components.
     * 
     * @since JDK1.1
     */
    public int getHgap() {
	return hgap;
    }

    /**
     * Sets the horizontal gap between components.
     * 
     * @param hgap
     *            the horizontal gap between components
     * @since JDK1.1
     */
    public void setHgap(int hgap) {
	this.hgap = hgap;
    }

    /**
     * Returns the vertical gap between components.
     * 
     * @since JDK1.1
     */
    public int getVgap() {
	return vgap;
    }

    /**
     * Sets the vertical gap between components.
     * 
     * @param vgap
     *            the vertical gap between components
     * @since JDK1.1
     */
    public void setVgap(int vgap) {
	this.vgap = vgap;
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object. For border layouts, the constraint must be one of the
     * following constants: <code>NORTH</code>, <code>SOUTH</code>,
     * <code>EAST</code>, <code>WEST</code>, or <code>CENTER</code>.
     * <p>
     * Most applications do not call this method directly. This method is called
     * when a component is added to a container using the
     * <code>Container.add</code> method with the same argument types.
     * 
     * @param comp
     *            the component to be added.
     * @param constraints
     *            an object that specifies how and where the component is added
     *            to the layout.
     * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
     * @exception IllegalArgumentException
     *                if the constraint object is not a string, or if it not one
     *                of the five specified constants.
     * @since JDK1.1
     */
    public void addLayoutComponent(Component comp, Object constraints) {
	synchronized (comp.getTreeLock()) {
	    if ((constraints == null) || (constraints instanceof String)) {
		addLayoutComponent((String) constraints, comp);
	    } else {
		throw new IllegalArgumentException(
			"cannot add to layout: constraint must be a string (or null)");
	    }
	}
    }

    /**
     * @deprecated replaced by
     *             <code>addLayoutComponent(Component, Object)</code>.
     */
    @Deprecated
    public void addLayoutComponent(String name, Component comp) {
	synchronized (comp.getTreeLock()) {
	    /* Special case: treat null the same as "Center". */
	    if (name == null) {
		name = "Center";
	    }

	    /*
	     * Assign the component to one of the known regions of the layout.
	     */
	    if (CENTER.equalsIgnoreCase(name)) {
		center = comp;
	    } else if (NORTH.equalsIgnoreCase(name)) {
		north = comp;
	    } else if (SOUTH.equalsIgnoreCase(name)) {
		south = comp;
	    } else if (EAST.equalsIgnoreCase(name)) {
		east = comp;
	    } else if (WEST.equalsIgnoreCase(name)) {
		west = comp;
	    } else if (NORTHEAST.equalsIgnoreCase(name)) {
		northeast = comp;
	    } else if (NORTHWEST.equalsIgnoreCase(name)) {
		northwest = comp;
	    } else if (SOUTHEAST.equalsIgnoreCase(name)) {
		southeast = comp;
	    } else if (SOUTHWEST.equalsIgnoreCase(name)) {
		southwest = comp;
	    } else if (BEFORE_FIRST_LINE.equals(name)) {
		firstLine = comp;
	    } else if (AFTER_LAST_LINE.equals(name)) {
		lastLine = comp;
	    } else if (BEFORE_LINE_BEGINS.equals(name)) {
		firstItem = comp;
	    } else if (AFTER_LINE_ENDS.equals(name)) {
		lastItem = comp;
	    } else {
		throw new IllegalArgumentException(
			"cannot add to layout: unknown constraint: " + name);
	    }
	}
    }

    /**
     * Removes the specified component from this border layout. This method is
     * called when a container calls its <code>remove</code> or
     * <code>removeAll</code> methods. Most applications do not call this method
     * directly.
     * 
     * @param comp
     *            the component to be removed.
     * @see java.awt.Container#remove(java.awt.Component)
     * @see java.awt.Container#removeAll()
     */
    public void removeLayoutComponent(Component comp) {
	synchronized (comp.getTreeLock()) {
	    if (comp == center) {
		center = null;
	    } else if (comp == north) {
		north = null;
	    } else if (comp == south) {
		south = null;
	    } else if (comp == east) {
		east = null;
	    } else if (comp == west) {
		west = null;
	    } else if (comp == northeast) {
		northeast = null;
	    } else if (comp == northwest) {
		northwest = null;
	    } else if (comp == southwest) {
		southwest = null;
	    } else if (comp == southeast) {
		southeast = null;
	    }
	    if (comp == firstLine) {
		firstLine = null;
	    } else if (comp == lastLine) {
		lastLine = null;
	    } else if (comp == firstItem) {
		firstItem = null;
	    } else if (comp == lastItem) {
		lastItem = null;
	    }
	}
    }

    /**
     * Determines the minimum size of the <code>target</code> container using
     * this layout manager.
     * <p>
     * This method is called when a container calls its
     * <code>getMinimumSize</code> method. Most applications do not call this
     * method directly.
     * 
     * @param target
     *            the container in which to do the layout.
     * @return the minimum dimensions needed to lay out the subcomponents of the
     *         specified container.
     * @see java.awt.Container
     * @see #preferredLayoutSize(Container)
     * @see java.awt.Container#getMinimumSize()
     */
    public Dimension minimumLayoutSize(Container target) {
	synchronized (target.getTreeLock()) {
	    Dimension dim = new Dimension(0, 0);

	    boolean ltr = true; //target.getComponentOrientation().isLeftToRight
	    // ();
	    Component c = null;

	    if ((c = getChild(EAST, ltr)) != null) {
		Dimension d = c.getMinimumSize();
		dim.width += d.width + hgap;
		dim.height = Math.max(d.height, dim.height);
	    }
	    if ((c = getChild(WEST, ltr)) != null) {
		Dimension d = c.getMinimumSize();
		dim.width += d.width + hgap;
		dim.height = Math.max(d.height, dim.height);
	    }
	    if ((c = getChild(CENTER, ltr)) != null) {
		Dimension d = c.getMinimumSize();
		dim.width += d.width;
		dim.height = Math.max(d.height, dim.height);
	    }
	    if ((c = getChild(NORTH, ltr)) != null) {
		Dimension d = c.getMinimumSize();
		dim.width = Math.max(d.width, dim.width);
		dim.height += d.height + vgap;
	    }
	    if ((c = getChild(SOUTH, ltr)) != null) {
		Dimension d = c.getMinimumSize();
		dim.width = Math.max(d.width, dim.width);
		dim.height += d.height + vgap;
	    }

	    Insets insets = target.getInsets();
	    dim.width += insets.left + insets.right;
	    dim.height += insets.top + insets.bottom;

	    return dim;
	}
    }

    /**
     * Determines the preferred size of the <code>target</code> container using
     * this layout manager, based on the components in the container.
     * <p>
     * Most applications do not call this method directly. This method is called
     * when a container calls its <code>getPreferredSize</code> method.
     * 
     * @param target
     *            the container in which to do the layout.
     * @return the preferred dimensions to lay out the subcomponents of the
     *         specified container.
     * @see java.awt.Container
     * @see #minimumLayoutSize(Container)
     * @see java.awt.Container#getPreferredSize()
     */
    public Dimension preferredLayoutSize(Container target) {
	synchronized (target.getTreeLock()) {
	    Dimension dim = new Dimension(0, 0);

	    boolean ltr = true; //target.getComponentOrientation().isLeftToRight
	    // ();
	    Component c = null;

	    if ((c = getChild(EAST, ltr)) != null) {
		Dimension d = c.getPreferredSize();
		dim.width += d.width + hgap;
		dim.height = Math.max(d.height, dim.height);
	    }
	    if ((c = getChild(WEST, ltr)) != null) {
		Dimension d = c.getPreferredSize();
		dim.width += d.width + hgap;
		dim.height = Math.max(d.height, dim.height);
	    }
	    if ((c = getChild(CENTER, ltr)) != null) {
		Dimension d = c.getPreferredSize();
		dim.width += d.width;
		dim.height = Math.max(d.height, dim.height);
	    }
	    if ((c = getChild(NORTH, ltr)) != null) {
		Dimension d = c.getPreferredSize();
		dim.width = Math.max(d.width, dim.width);
		dim.height += d.height + vgap;
	    }
	    if ((c = getChild(SOUTH, ltr)) != null) {
		Dimension d = c.getPreferredSize();
		dim.width = Math.max(d.width, dim.width);
		dim.height += d.height + vgap;
	    }

	    Insets insets = target.getInsets();
	    dim.width += insets.left + insets.right;
	    dim.height += insets.top + insets.bottom;

	    return dim;
	}
    }

    /**
     * Returns the maximum dimensions for this layout given the components in
     * the specified target container.
     * 
     * @param target
     *            the component which needs to be laid out
     * @see Container
     * @see #minimumLayoutSize
     * @see #preferredLayoutSize
     */
    public Dimension maximumLayoutSize(Container target) {
	return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the alignment along the x axis. This specifies how the component
     * would like to be aligned relative to other components. The value should
     * be a number between 0 and 1 where 0 represents alignment along the
     * origin, 1 is aligned the furthest away from the origin, 0.5 is centered,
     * etc.
     */
    public float getLayoutAlignmentX(Container parent) {
	return 0.5f;
    }

    /**
     * Returns the alignment along the y axis. This specifies how the component
     * would like to be aligned relative to other components. The value should
     * be a number between 0 and 1 where 0 represents alignment along the
     * origin, 1 is aligned the furthest away from the origin, 0.5 is centered,
     * etc.
     */
    public float getLayoutAlignmentY(Container parent) {
	return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached
     * information it should be discarded.
     */
    public void invalidateLayout(Container target) {
    }

    /**
     * Lays out the container argument using this border layout.
     * <p>
     * This method actually reshapes the components in the specified container
     * in order to satisfy the constraints of this <code>BordersLayout</code>
     * object. The <code>NORTH</code> and <code>SOUTH</code> components, if any,
     * are placed at the top and bottom of the container, respectively. The
     * <code>WEST</code> and <code>EAST</code> components are then placed on the
     * left and right, respectively. Finally, the <code>CENTER</code> object is
     * placed in any remaining space in the middle.
     * <p>
     * Most applications do not call this method directly. This method is called
     * when a container calls its <code>doLayout</code> method.
     * 
     * @param target
     *            the container in which to do the layout.
     * @see java.awt.Container
     * @see java.awt.Container#doLayout()
     */
    public void layoutContainer(Container target) {
	synchronized (target.getTreeLock()) {
	    Insets insets = target.getInsets();
	    Dimension targetSize = target.getSize();
	    int top = insets.top;
	    int bottom = targetSize.height - insets.bottom;
	    int left = insets.left;
	    int right = targetSize.width - insets.right;

	    boolean ltr = true; //target.getComponentOrientation().isLeftToRight
	    // ();
	    Component c = null;

	    Rectangle rt = new Rectangle();
	    Rectangle rb = new Rectangle();
	    Rectangle re = new Rectangle();
	    Rectangle rw = new Rectangle();
	    if ((c = getChild(NORTH, ltr)) != null) {
		c.setSize(right - left, c.getSize().height);
		Dimension d = c.getPreferredSize();
		rt = new Rectangle(left, top, right - left, d.height);
		c.setBounds(rt);
		top += d.height + vgap;
	    }

	    if ((c = getChild(SOUTH, ltr)) != null) {
		c.setSize(right - left, c.getSize().height);
		Dimension d = c.getPreferredSize();
		rb = new Rectangle(left, bottom - d.height, right - left,
			d.height);
		c.setBounds(rb);
		bottom -= d.height + vgap;
	    }
	    if ((c = getChild(EAST, ltr)) != null) {
		c.setSize(c.getSize().width, bottom - top);
		Dimension d = c.getPreferredSize();
		re = new Rectangle(right - d.width, top, d.width, bottom - top);
		c.setBounds(re);
		right -= d.width + hgap;
	    }
	    if ((c = getChild(WEST, ltr)) != null) {
		c.setSize(c.getSize().width, bottom - top);
		Dimension d = c.getPreferredSize();
		rw = new Rectangle(left, top, d.width, bottom - top);
		c.setBounds(rw);
		left += d.width + hgap;
	    }
	    if ((c = getChild(CENTER, ltr)) != null) {
		c.setBounds(left, top, right - left, bottom - top);
	    }

	    if ((c = getChild(NORTH, ltr)) != null) {
		rt = c.getBounds();
		rt.x = left;
		rt.width = right - left;
		c.setBounds(rt);
	    }

	    if ((c = getChild(SOUTH, ltr)) != null) {
		rb = c.getBounds();
		rb.x = left;
		rb.width = right - left;
		c.setBounds(rb);
	    }

	    if ((c = getChild(NORTHEAST, ltr)) != null) {
		c.setBounds(re.x, rt.y, re.width, rt.height);
	    }
	    if ((c = getChild(NORTHWEST, ltr)) != null) {
		c.setBounds(rw.x, rt.y, rw.width, rt.height);
	    }
	    if ((c = getChild(SOUTHEAST, ltr)) != null) {
		c.setBounds(re.x, rb.y, re.width, rb.height);
	    }
	    if ((c = getChild(SOUTHWEST, ltr)) != null) {
		c.setBounds(rw.x, rb.y, rw.width, rb.height);
	    }

	}
    }

    /**
     * Get the component that corresponds to the given constraint location
     * 
     * @param key
     *            The desired absolute position, either NORTH, SOUTH, EAST, or
     *            WEST.
     * @param ltr
     *            Is the component line direction left-to-right?
     */
    private Component getChild(String key, boolean ltr) {
	Component result = null;

	if (key == NORTH) {
	    result = (firstLine != null) ? firstLine : north;
	} else if (key == SOUTH) {
	    result = (lastLine != null) ? lastLine : south;
	} else if (key == WEST) {
	    result = ltr ? firstItem : lastItem;
	    if (result == null) {
		result = west;
	    }
	} else if (key == EAST) {
	    result = ltr ? lastItem : firstItem;
	    if (result == null) {
		result = east;
	    }
	} else if (key == NORTHEAST) {
	    result = northeast;
	} else if (key == NORTHWEST) {
	    result = northwest;
	} else if (key == SOUTHEAST) {
	    result = southeast;
	} else if (key == SOUTHWEST) {
	    result = southwest;
	} else if (key == CENTER) {
	    result = center;
	}
	if (result != null && !result.isVisible()) {
	    result = null;
	}
	return result;
    }

    /**
     * Returns a string representation of the state of this border layout.
     * 
     * @return a string representation of this border layout.
     */
    @Override
    public String toString() {
	return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
    }
}
