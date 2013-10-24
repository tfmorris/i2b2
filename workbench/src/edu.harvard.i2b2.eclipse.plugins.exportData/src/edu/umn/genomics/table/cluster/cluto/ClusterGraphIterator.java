/*
 * @(#) $RCSfile: ClusterGraphIterator.java,v $ $Revision: 1.3 $ $Date: 2008/11/07 18:52:53 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster.cluto;

import java.awt.geom.*;
import edu.umn.genomics.graph.*;
import jcluto.*;

/**
 * ClusterGraphIterator displays a hierarchical clustering of rows from a table.
 * The clustering is displayed as a Dendogram which is drawn as line segments on
 * a graph widget. The axes of the graph are zoomable. The row selection of the
 * table is displayed on the dendogram. The user can trace out a rectangle on
 * the dendogram to edit the row selection set for the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/07 18:52:53 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClusterGraphIterator implements PathIterator {

    int ri = 0;
    int ci = 0;
    int ncol = 0;
    ClutoMatrix matrix;
    int[] rows = null;
    Axis xAxis;
    Axis yAxis;

    public ClusterGraphIterator(ClutoMatrix matrix, int[] rows, Axis xAxis,
	    Axis yAxis) {
	ncol = matrix.getColumnCount();
	this.rows = rows;
	this.matrix = matrix;
	this.xAxis = xAxis;
	this.yAxis = yAxis;
    }

    public int getWindingRule() {
	return PathIterator.WIND_EVEN_ODD;
    }

    public boolean isDone() {
	if (ri >= rows.length) {
	    return true;
	}
	return false;
    }

    public void next() {
	ci++;
	if (ci >= ncol) {
	    ri++;
	    ci = 0;
	}
    }

    public int currentSegment(float[] coords) {
	float val = matrix.getValue(rows[ri], ci);
	coords[0] = (float) xAxis.getPosition(ci);
	coords[1] = (float) (yAxis.getSize() - yAxis.getPosition(val));
	return ci == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
    }

    public int currentSegment(double[] coords) {
	float val = matrix.getValue(rows[ri], ci);
	coords[0] = xAxis.getPosition(ci);
	coords[1] = (yAxis.getSize() - yAxis.getPosition(val));
	return ci == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
    }

}
