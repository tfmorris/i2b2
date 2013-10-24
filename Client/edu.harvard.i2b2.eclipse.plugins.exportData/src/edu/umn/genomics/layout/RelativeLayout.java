/*
 * @(#) $RCSfile: RelativeLayout.java,v $ $Revision: 1.3 $ $Date: 2008/10/29 19:20:49 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.layout;

import java.awt.*;
import java.util.Hashtable;

/**
 * The <code>RelativeLayout</code> class is a layout manager that lays out a
 * container's components relative to the container's size.
 * <p>
 * <code><pre>
 * import java.awt.*;
 * import java.applet.Applet;
 * public class ButtonGrid extends JApplet {
 *     public void init() {
 *         setLayout(new RelativeLayout());
 *         JLabel label1 = new JLabel("Narrow");
 *         add(label1, new Extents(.0,0.,.2,1.));
 *         JLabel label2 = new JLabel("Wide");
 *         add(label2, new Extents(.2,0.,1.,1.));
 *     }
 * }
 * </pre></code>
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/29 19:20:49 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class RelativeLayout implements LayoutManager2 {
    Hashtable compTable; // constraints (Extents)

    /**
     * Creates a layout, components need to setConstraints.
     * 
     * @see #setConstraints
     */
    public RelativeLayout() {
	compTable = new Hashtable();
    }

    /**
     *
     */
    public void setConstraints(Component comp, Extents extents) {
	compTable.put(comp, extents);
    }

    /**
     * Adds the specified component with the specified name to the layout. This
     * does nothing in RelativeLayout, since constraints are required.
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * Removes the specified component from the layout.
     * 
     * @param comp
     *            the component to be removed
     */
    public void removeLayoutComponent(Component comp) {
	compTable.remove(comp);
    }

    /**
     * Calculates the preferred size dimensions for the specified panel given
     * the components in the specified parent container.
     * 
     * @param parent
     *            the component to be laid out
     * 
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent) {
	return getLayoutSize(parent, true);
    }

    /**
     * Calculates the minimum size dimensions for the specified panel given the
     * components in the specified parent container.
     * 
     * @param parent
     *            the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent) {
	return getLayoutSize(parent, false);
    }

    /**
     * Algorithm for calculating layout size (minimum or preferred).
     * 
     * @param parent
     *            the container in which to do the layout.
     * @param isPreferred
     *            true for calculating preferred size, false for calculating
     *            minimum size.
     * @return the dimensions to lay out the subcomponents of the specified
     *         container.
     */
    protected Dimension getLayoutSize(Container parent, boolean isPreferred) {
	int w = 0;
	int h = 0;
	Component[] ca = parent.getComponents();
	for (int i = 0; i < ca.length; i++) {
	    Dimension cd;
	    Extents e = (Extents) compTable.get(ca[i]);
	    if (e == null) {
		continue;
	    }
	    if (isPreferred) {
		cd = ca[i].getPreferredSize();
	    } else {
		cd = ca[i].getMinimumSize();
	    }
	    if (cd != null) {
		if (e.getWidth() != 0.) {
		    int cw = (int) (cd.width / e.getWidth());
		    if (w < cw) {
			w = cw;
		    }
		}
		if (e.getHeight() != 0.) {
		    int ch = (int) (cd.height / e.getHeight());
		    if (h < ch) {
			h = ch;
		    }
		}
	    }
	}
	return new Dimension(w, h);
    }

    /**
     * Lays out the container in the specified container.
     * 
     * @param parent
     *            the component which needs to be laid out
     */
    public void layoutContainer(Container parent) {
	synchronized (parent.getTreeLock()) {
	    Insets insets = parent.getInsets();
	    int ncomponents = parent.getComponentCount();

	    if (ncomponents == 0) {
		return;
	    }

	    // Total parent dimensions
	    Dimension size = parent.getSize();
	    int totalW = size.width - (insets.left + insets.right);
	    int totalH = size.height - (insets.top + insets.bottom);

	    for (int i = 0; i < ncomponents; i++) {
		Component c = parent.getComponent(i);
		Extents e = (Extents) compTable.get(c);
		if (e != null) {
		    int x = insets.left + (int) (totalW * e.getX());
		    int y = insets.top + (int) (totalH * e.getY());
		    int w = (int) (totalW * e.getWidth());
		    int h = (int) (totalH * e.getHeight());
		    c.setBounds(x, y, w, h);
		}
	    }
	}
    }

    // LayoutManager2 /////////////////////////////////////////////////////////

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     * 
     * @param comp
     *            the component to be added
     * @param constraints
     *            where/how the component is added to the layout.
     */
    public void addLayoutComponent(Component comp, Object constraints) {
	if (constraints instanceof Extents) {
	    setConstraints(comp, (Extents) constraints);
	} else if (constraints != null) {
	    throw new IllegalArgumentException(
		    "cannot add to layout: constraint must be Extents");
	}
    }

    /**
     * Returns the maximum size of this component.
     * 
     * @see java.awt.Component#getMinimumSize()
     * @see java.awt.Component#getPreferredSize()
     * @see LayoutManager
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
    public float getLayoutAlignmentX(Container target) {
	return 0.5f;
    }

    /**
     * Returns the alignment along the y axis. This specifies how the component
     * would like to be aligned relative to other components. The value should
     * be a number between 0 and 1 where 0 represents alignment along the
     * origin, 1 is aligned the furthest away from the origin, 0.5 is centered,
     * etc.
     */
    public float getLayoutAlignmentY(Container target) {
	return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached
     * information it should be discarded.
     */
    public void invalidateLayout(Container target) {
	// Do nothing
    }
}
