/*
 * @(#) $RCSfile: GraphText.java,v $ $Revision: 1.4 $ $Date: 2008/10/29 15:21:58 $ $Name: RELEASE_1_3_1_0001b $
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
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/29 15:21:58 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public abstract class GraphText extends AbstractGraphItem {
    Color color;
    String label;
    DataModel dataModel;
    String text;
    double pos[];
    Font font = null;
    FontMetrics fm = null;

    @Override
    public void setColor(Color color) {
	this.color = color;
	fireChangeEvent();
    }

    @Override
    public Color getColor() {
	return color;
    }

    @Override
    public void setLabel(String label) {
	this.label = label;
	fireChangeEvent();
    }

    @Override
    public String getLabel() {
	return label;
    }

    /** 
   */
    public void setText(String s, double x, double y) {
	this.text = s;
	fireChangeEvent();
    }

    public String getText() {
	return text;
    }

    @Override
    public void draw(Component c, Graphics g, Rectangle r, Axis xAxis,
	    Axis yAxis) {
	Font f = font != null ? font : c.getFont() != null ? c.getFont() : g
		.getFont();

	g.setFont(f);
	fm = g.getFontMetrics(f);
	int x = xAxis.getIntPosition(pos[0]);
	int y = xAxis.getIntPosition(pos[1]);
	g.drawString(text, x, y - fm.getAscent());
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

	if (fm != null && text != null) {
	    int xr = r.x + r.width;
	    int yb = r.y + r.height;
	    int x = xAxis.getIntPosition(pos[0]);
	    int y = xAxis.getIntPosition(pos[1]);
	    int w = x + fm.stringWidth(text);
	    int t = y - fm.getAscent();
	    int b = y + fm.getDescent();
	    if ((x <= xr && w >= r.x) && (t <= yb && b >= r.y)) {
		return true;
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
	if (fm != null && text != null) {
	    int x = xAxis.getIntPosition(pos[0]);
	    int y = xAxis.getIntPosition(pos[1]);
	    int w = x + fm.stringWidth(text);
	    int t = y - fm.getAscent();
	    int b = y + fm.getDescent();
	    if (p.x >= x && p.x <= w && p.y >= t && p.y <= b) {
		return true;
	    }
	}
	return false;
    }
}
