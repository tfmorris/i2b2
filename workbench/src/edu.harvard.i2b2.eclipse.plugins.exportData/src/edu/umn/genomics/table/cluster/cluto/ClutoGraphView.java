/*
 * @(#) $RCSfile: ClutoGraphView.java,v $ $Revision: 1.3 $ $Date: 2008/11/11 19:05:37 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import edu.umn.genomics.component.*;
import edu.umn.genomics.table.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;
import jcluto.*;

/**
 * ClutoMatrixView displays a hierarchical clustering of rows from a table. The
 * clustering is displayed as a Dendogram which is drawn as line segments on a
 * graph widget. The axes of the graph are zoomable. The row selection of the
 * table is displayed on the dendogram. The user can trace out a rectangle on
 * the dendogram to edit the row selection set for the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/11 19:05:37 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClutoGraphView extends JPanel implements CleanUp, Serializable {
    IndexMapSelection ims = null;
    JTable jtable = null;

    // Presents the cluster numbers as a TableModel
    class ClusterTableModel extends AbstractTableModel {
	int rowCnt = 0;

	public ClusterTableModel(int nrows) {
	    rowCnt = nrows;
	}

	public int getRowCount() {
	    return rowCnt;
	}

	public int getColumnCount() {
	    return 1;
	}

	@Override
	public String getColumnName(int index) {
	    return "Cluster";
	}

	public Object getValueAt(int row, int column) {
	    return new Integer(row);
	}
    }

    /**
     * A view of the ClutoSolution that displays the rows of the matrix as line
     * graphs for each cluster.
     * 
     */
    public ClutoGraphView(ClutoSolution clutoSolution,
	    PartitionIndexMap partMap, ListSelectionModel lsm) {
	ClutoTableMatrix cMat = clutoSolution.getMatrix() instanceof ClutoTableMatrix ? (ClutoTableMatrix) clutoSolution
		.getMatrix()
		: new ClutoTableMatrix(clutoSolution.getMatrix());

	ClusterTableModel tm = new ClusterTableModel(clutoSolution.getParams()
		.getNumClusters());
	jtable = new JTable(tm);
	jtable.getColumnModel().getColumn(0).setPreferredWidth(30);
	ims = new IndexMapSelection(lsm, jtable.getSelectionModel(), partMap);

	// graphs
	final JPanel gpP = new JPanel(new GridLayout(0, 1, 2, 2));
	// colHeader AxisDisplay
	final JPanel chP = new JPanel(new BorderLayout());
	// axes displays
	final JPanel axP = new JPanel(new GridLayout(0, 1, 2, 2));
	axP.setPreferredSize(new Dimension(50, 50));

	final JPanel rhP = new JPanel(new BorderLayout());
	rhP.add(jtable, BorderLayout.WEST);
	rhP.add(axP, BorderLayout.EAST);

	ClusterGraph[] cg = new ClusterGraph[clutoSolution.getParams()
		.getNumClusters()];
	double minVal = Double.MAX_VALUE;
	double maxVal = Double.MIN_VALUE;
	boolean shareAxis = true;
	int fCnt = 5;
	for (int c = 0; c < cg.length; c++) {
	    cg[c] = new ClusterGraph(clutoSolution, c, fCnt, partMap.getSrcs(c));
	    cg[c].getGraph().showAxis(BorderLayout.SOUTH, false);
	    cg[c].getGraph().showAxis(BorderLayout.WEST, false);
	    if (c > 0) {
		// Have all graphs share the same axes
		cg[c].getGraph().setXAxis(cg[0].getGraph().getXAxis());
		if (shareAxis)
		    cg[c].getGraph().setYAxis(cg[0].getGraph().getYAxis());
	    }
	    double min = cg[c].getGraph().getYAxis().getMin();
	    if (min < minVal) {
		minVal = min;
	    }
	    double max = cg[c].getGraph().getYAxis().getMax();
	    if (max > maxVal) {
		maxVal = max;
	    }
	    gpP.add(cg[c]);
	    AxisDisplay yDisp = new AxisDisplay(cg[0].getGraph().getYAxis(),
		    AxisComponent.LEFT);
	    yDisp.setZoomable(true);
	    axP.add(yDisp);
	}

	ComponentAdapter jta = new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		jtable
			.setRowHeight(((JComponent) e.getSource()).getHeight() + 2);
	    }
	};
	cg[0].addComponentListener(jta);

	// Set the labeler on the bottom graph
	ColumnNameLabeler colLabeler = new ColumnNameLabeler(cMat);

	((LinearAxis) (cg[cg.length - 1].getGraph().getXAxis()))
		.setTickIncrement(1.);
	Axis xAxis = new LinearAxis(cg[0].getGraph().getXAxis());
	AxisDisplay xDisp = new AxisDisplay(cg[0].getGraph().getXAxis(),
		AxisComponent.TOP);
	xDisp.setZoomable(true);
	xDisp.setAxisLabeler(colLabeler);
	chP.add(xDisp, BorderLayout.NORTH);

	// Set the Y Axes the same
	double diff = maxVal - minVal;
	maxVal += diff * .1;
	minVal -= diff * .1;
	if (shareAxis) {
	    cg[0].getGraph().getYAxis().setMin(minVal);
	    cg[0].getGraph().getYAxis().setMax(maxVal);
	} else {
	    for (int c = 0; c < cg.length; c++) {
		cg[c].getGraph().getYAxis().setMin(minVal);
		cg[c].getGraph().getYAxis().setMax(maxVal);
	    }
	}

	final JScrollPane jsp = new JScrollPane(gpP);
	jsp.setRowHeaderView(rhP);
	jsp.setColumnHeaderView(chP);

	// dispSize
	try {
	    Class.forName("javax.swing.JSpinner");
	    JComponent dispSize = DoSpinner.getComponent(1., 1., 10., .5);
	    dispSize.setToolTipText("Set the Graph height");
	    ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    try {
			double scale = ((Number) DoSpinner.getValue(e
				.getSource())).doubleValue();
			Dimension dim = jsp.getViewport().getSize();
			dim.height = (int) (dim.height * scale);
			gpP.setPreferredSize(dim);
			dim.width = rhP.getPreferredSize().width;
			rhP.setPreferredSize(dim);
			gpP.invalidate();
			rhP.invalidate();
			jsp.invalidate();
			jsp.validate();
		    } catch (Exception ex) {
		    }
		}
	    };
	    DoSpinner.addChangeListener(dispSize, changeListener);
	    jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, dispSize);
	} catch (Exception ex) {
	}

	ComponentAdapter ca = new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		Dimension dim = rhP.getPreferredSize();
		dim.height = gpP.getPreferredSize().height;
		rhP.setPreferredSize(dim);
		rhP.validate();
		gpP.invalidate();
		rhP.invalidate();
	    }
	};
	gpP.addComponentListener(ca);

	ComponentAdapter vpa = new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		Dimension dim = jsp.getViewport().getSize();
		dim.height = gpP.getPreferredSize().height;
		gpP.setPreferredSize(dim);
		// gpP.validate();
		gpP.invalidate();
		rhP.invalidate();
		jsp.getViewport().validate();
	    }
	};
	jsp.addComponentListener(vpa);

	setLayout(new BorderLayout());
	add(jsp);
    }

    @Override
    public void addNotify() {
	super.addNotify();
    }

    @Override
    public void removeNotify() {
	super.removeNotify();
    }

    @Override
    protected void finalize() throws Throwable {
	cleanUp();
	super.finalize();
    }

    public void cleanUp() {
	if (ims != null) {
	    ims.cleanUp();
	    ims = null;
	}
    }

}
