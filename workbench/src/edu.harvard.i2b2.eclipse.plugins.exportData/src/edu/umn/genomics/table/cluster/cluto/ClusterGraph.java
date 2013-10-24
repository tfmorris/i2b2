/*
 * @(#) $RCSfile: ClusterGraph.java,v $ $Revision: 1.3 $ $Date: 2008/11/07 18:52:53 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;
import javax.swing.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;
import jcluto.*;

/**
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/07 18:52:53 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClusterGraph extends JPanel {

    ClutoTableMatrix ctm;
    int[] rows = null;
    double[] mean = null;
    double[] stddev = null;
    double minVal = Double.MAX_VALUE;
    double maxVal = Double.MIN_VALUE;

    SimpleGraph graph;

    public ClusterGraph(ClutoSolution clutoSolution, int cluster, int fCnt,
	    int[] rows) {
	ClutoMatrix matrix = clutoSolution.getMatrix();
	ctm = matrix instanceof ClutoTableMatrix ? (ClutoTableMatrix) matrix
		: new ClutoTableMatrix(matrix);
	this.rows = rows;
	calcStats();

	graph = new SimpleGraph();
	graph.getGraphDisplay().setOpaque(true);
	graph.getGraphDisplay().setBackground(Color.white);
	graph.getGraphDisplay().setGridColor(new Color(220, 220, 220));
	graph.showGrid(false);
	graph.showAxis(BorderLayout.WEST, true);
	graph.getAxisDisplay(BorderLayout.WEST).setZoomable(true);
	graph.getAxisDisplay(BorderLayout.SOUTH).setZoomable(true);

	graph.getXAxis().setMin(-.5);
	graph.getXAxis().setMax(ctm.getColumnCount() - .5);
	graph.getYAxis().setMin(minVal);
	graph.getYAxis().setMax(maxVal);

	if (fCnt > 0) {
	    ClusterFeatures cf = clutoSolution.getClusterFeatures(fCnt);
	    IndexedColor idx;
	    FeatureGraphItem gf;
	    int cnt = cf.getFeatureCount();
	    int[] fcol1 = new int[cnt];
	    int[] fcol2 = new int[cnt];
	    final Color[] fcolor1 = new Color[cnt];
	    final Color[] fcolor2 = new Color[cnt];

	    for (int i = 0; i < cnt; i++) {
		// 
		fcol2[i] = cf.getInternalID(cluster, i);
		float fwgt = cf.getInternalWgt(cluster, i) * fCnt / 2f;
		if (fwgt > 1f) {
		    fwgt = 1f;
		} else if (fwgt < .1f) {
		    fwgt = .1f;
		}
		fcolor2[i] = new Color(1f - fwgt, 1f, 1f - fwgt);
		//
		fcol1[i] = cf.getExternalID(cluster, i);
		fwgt = cf.getExternalWgt(cluster, i) * fCnt / 2f;
		if (fwgt > 1f) {
		    fwgt = 1f;
		} else if (fwgt < .1f) {
		    fwgt = .1f;
		}
		fcolor1[i] = new Color(1f - fwgt, 1f - fwgt, 1f);
	    }

	    idx = new IndexedColor() {
		Color color[] = fcolor1;

		public int getSize() {
		    return color != null ? color.length : 0;
		}

		public Color getColorAt(int index) {
		    return color != null && index >= 0 && index < color.length ? color[index]
			    : Color.white;
		}
	    };
	    gf = new FeatureGraphItem(fcol1, 0., .1);
	    gf.setIndexedColor(idx);
	    graph.addGraphItem(gf);

	    idx = new IndexedColor() {
		Color color[] = fcolor2;

		public int getSize() {
		    return color != null ? color.length : 0;
		}

		public Color getColorAt(int index) {
		    return color != null && index >= 0 && index < color.length ? color[index]
			    : Color.white;
		}
	    };
	    gf = new FeatureGraphItem(fcol2, .9, .1);
	    gf.setIndexedColor(idx);
	    graph.addGraphItem(gf);

	}

	ClusterGraphItem cg = new ClusterGraphItem(ctm, rows);
	cg.setColor(Color.darkGray);
	graph.addGraphItem(cg);

	double[] stddevPos = new double[stddev.length];
	for (int i = 0; i < stddev.length; i++) {
	    stddevPos[i] = mean[i] + stddev[i];
	}
	GraphLine gp = new GraphLine();
	gp.setData(stddevPos);
	gp.setColor(Color.yellow);
	graph.addGraphItem(gp);

	double[] stddevNeg = new double[stddev.length];
	for (int i = 0; i < stddev.length; i++) {
	    stddevNeg[i] = mean[i] - stddev[i];
	}
	GraphLine gn = new GraphLine();
	gn.setData(stddevNeg);
	gn.setColor(Color.yellow);
	graph.addGraphItem(gn);

	GraphLine gm = new GraphLine();
	gm.setData(mean);
	gm.setColor(Color.green);
	graph.addGraphItem(gm);

	setLayout(new BorderLayout());
	add(graph);
    }

    public SimpleGraph getGraph() {
	return graph;
    }

    private void calcStats() {
	int ncol = ctm.getColumnCount();
	mean = new double[ncol];
	stddev = new double[ncol];
	for (int c = 0; c < ncol; c++) {
	    int numNull = 0;
	    double avg = 0.;
	    int n = rows.length;
	    if (n > 0) {
		for (int i = 0; i < rows.length; i++) {
		    double val = ctm.getValue(rows[i], c);
		    if (!Double.isNaN(val)) {
			avg += val / n;
			if (val < minVal) {
			    minVal = val;
			}
			if (val > maxVal) {
			    maxVal = val;
			}
		    } else {
			numNull++;
		    }
		}
		if (numNull > 0 && numNull < rows.length) {
		    avg *= n / (n - numNull);
		}
	    }
	    // variance
	    double variance = 0.;
	    double n1 = n - numNull - 1;
	    if (n1 > 0) {
		for (int i = 0; i < rows.length; i++) {
		    double val = ctm.getValue(rows[i], c);
		    if (!Double.isNaN(val)) {
			variance += Math.pow(val - avg, 2.) / n1;
		    }
		}
	    }
	    // stddev
	    mean[c] = avg;
	    stddev[c] = Math.sqrt(variance);
	}

    }

}
