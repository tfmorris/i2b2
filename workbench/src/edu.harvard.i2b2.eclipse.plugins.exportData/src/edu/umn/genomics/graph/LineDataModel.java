/*
 * @(#) $RCSfile: LineDataModel.java,v $ $Revision: 1.4 $ $Date: 2008/10/22 19:39:19 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;

/**
 * LineDataModel presents LineFormula lines as line segments within the graph
 * area.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/22 19:39:19 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see edu.umn.genomics.graph.LineFormula
 */
public class LineDataModel implements DataModel {
    Vector lines = new Vector();

    /**
     * Create a LineDataModel without any data.
     */
    public LineDataModel() {
    }

    /**
     * Create a LineDataModel from a LineFormula.
     * 
     * @param line
     *            a LineFormula.
     */
    public LineDataModel(LineFormula line) {
	lines.addElement(line);
    }

    /**
     * Inserts the line at the specified position in the line list.
     */
    public void add(int index, LineFormula line) {
	lines.add(index, line);
    }

    /**
     * Add a line to the end of the line list.
     */
    public boolean add(LineFormula line) {
	return lines.add(line);
    }

    /**
     * Add a line to the end of the line list.
     */
    public boolean remove(LineFormula line) {
	return lines.remove(line);
    }

    /**
     * Add a line to the end of the line list.
     */
    public void clear() {
	lines.clear();
    }

    /**
     * Return any y values at the given x index.
     * 
     * @param xi
     *            the x index into the array.
     * @return the y values at the given x index.
     */
    public double[] getYValues(int xi) {
	double yv[] = new double[lines.size()];
	for (int i = 0; i < lines.size(); i++) {
	    LineFormula line = (LineFormula) lines.elementAt(i);
	    yv[i] = line.getY(xi);
	}
	return yv;
    }

    /**
     * @param x
     *            the x pixel offset
     * @param y
     *            the y pixel offset
     * @param axes
     *            the axes that transform the datapoints to the pixel area
     * @param points
     *            the array of points: xpoints, ypoints return the array of
     *            points: xpoints, ypoints
     */
    public int[][] getPoints(int x, int y, Axis axes[], int points[][]) {
	int pnts[][] = points;
	int w = axes[0].getSize();
	int h = axes[1].getSize();
	int np = lines.size() * 2;
	if (pnts == null || pnts.length < 2) {
	    pnts = new int[2][];
	}
	if (pnts[0] == null || pnts[0].length != np) {
	    pnts[0] = new int[np];
	}
	if (pnts[1] == null || pnts[1].length != np) {
	    pnts[1] = new int[np];
	}
	int yb = y + h;
	for (int i = 0, j = 0; i < lines.size(); i++, j += 2) {
	    try {
		LineFormula line = (LineFormula) lines.elementAt(i);
		pnts[0][j] = x;
		pnts[1][j] = yb - (int) Math.round(line.getY(x));
		pnts[1][j] = yb
			- axes[1]
				.getIntPosition(line.getY(axes[0].getValue(x)));
		pnts[0][j + 1] = x + w;
		pnts[1][j + 1] = yb
			- axes[1].getIntPosition(line.getY(axes[0].getValue(x
				+ w)));

	    } catch (ClassCastException ccex) {
	    }
	}
	return pnts;
    }
}
