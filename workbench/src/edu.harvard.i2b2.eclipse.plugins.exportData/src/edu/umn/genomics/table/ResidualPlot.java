/*
 * @(#) $RCSfile: ResidualPlot.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import javax.swing.event.*;
import edu.umn.genomics.graph.*;

/**
 * Display a ResidualPlot of the values from two ColumnMaps. Dragging out a
 * rectangle on the panel selects the rows in the TableModel that are mapped to
 * the data points in the rectangle.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 */
public class ResidualPlot extends ScatterPlot implements Serializable,
	ListSelectionListener, DataModel {

    /**
     * Return arrays of the x pixel location and the y pixel location.
     * 
     * @param x
     *            the x pixel offset
     * @param y
     *            the y pixel offset
     * @param axes
     *            the axes that transform the datapoints to the pixel area
     * @param points
     *            the array of points: xpoints, ypoints
     * @return the array of points: xpoints, ypoints
     */
    @Override
    public int[][] getPoints(int x, int y, Axis axes[], int points[][]) {
	int pnts[][] = points;
	if (xcol != null && ycol != null) {
	    int w = axes[0].getSize();
	    int h = axes[1].getSize();
	    rline = xcol.regressionLine(ycol);
	    int np = xcol.getCount();
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

	    for (int r = 0; r < np; r++) {
		pnts[0][r] = x + axes[0].getIntPosition(xcol.getMapValue(r));
		pnts[1][r] = yb
			- axes[1].getIntPosition(ycol.getMapValue(r)
				- rline.getY(xcol.getMapValue(r)));
	    }
	}
	return pnts;
    }

    @Override
    public double[] getYValues(int xi) {
	return null; // Should this be implemented?
    }

    /**
     * Construct a ResidualPlot for the given columns.
     * 
     * @param xColumn
     *            the column to map to the x axis.
     * @param yColumn
     *            the column to map to the y axis.
     */
    public ResidualPlot(ColumnMap xColumn, ColumnMap yColumn) {
	super(xColumn, yColumn);
    }

    /**
     * Construct a ResidualPlot for the given columns.
     * 
     * @param xColumn
     *            the column to map to the x axis.
     * @param yColumn
     *            the column to map to the y axis.
     * @param selectionModel
     */
    public ResidualPlot(ColumnMap xColumn, ColumnMap yColumn,
	    ListSelectionModel selectionModel) {
	super(xColumn, yColumn, selectionModel);
    }

    @Override
    public void setColumnMaps(ColumnMap xColumn, ColumnMap yColumn) {
	super.setColumnMaps(xColumn, yColumn);
	yTitle = "Residual " + ycol.getName();
	yLbl.setText(yTitle);
	int np = xcol.getCount();
	if (np > 0) {
	    rline = xcol.regressionLine(ycol);
	    double min = ycol.getMapValue(0) - rline.getY(xcol.getMapValue(0));
	    double max = min;
	    for (int r = 1; r < np; r++) {
		double y = ycol.getMapValue(r)
			- rline.getY(xcol.getMapValue(r));
		if (y < min)
		    min = y;
		else if (y > max)
		    max = y;
	    }
	    if (min == max) {
		min -= 1.;
		max += 1.;
	    }
	    System.err.println("residual  min = " + min + "   max = " + max);
	    yAxis.setMin(min);
	    yAxis.setMax(max);
	}
	repaint();
    }

}
