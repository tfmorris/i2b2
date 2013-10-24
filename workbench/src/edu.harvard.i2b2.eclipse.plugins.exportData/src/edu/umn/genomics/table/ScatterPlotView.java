/*
 * @(#) $RCSfile: ScatterPlotView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * ScatterPlotView displays a scatter plot of the two selected columns of the
 * table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ScatterPlotView extends AbstractTableModelView implements
	Serializable {

    DefaultComboBoxModel xModel = new DefaultComboBoxModel();
    DefaultComboBoxModel yModel = new DefaultComboBoxModel();
    JComboBox xChoice = new JComboBox(xModel);
    JComboBox yChoice = new JComboBox(yModel);
    JToolBar top = new JToolBar();
    JPanel plotPanel = new JPanel(new BorderLayout());
    ScatterPlot plot = null;
    boolean residuals = false;
    int prevSetOp = -1;
    int[] pendingCols = null;

    private MouseAdapter ma = new MouseAdapter() {
	@Override
	public void mousePressed(MouseEvent e) {
	    if (ctx != null) {
		prevSetOp = ctx.getSetOperator(tm).getSetOperator();
		ctx.getSetOperator(tm).setFromInputEventMask(e.getModifiers());
	    }
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    if (ctx != null) {
		ctx.getSetOperator(tm).setSetOperator(prevSetOp);
	    }
	}
    };

    /**
     * Constructs a view display. Nothing will be displayed until a data model
     * is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ScatterPlotView() {
	super();
	init();
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ScatterPlotView(TableModel tableModel) {
	super(tableModel);
	init();
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public ScatterPlotView(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
	init();
    }

    /**
     * Sets tableModel as the data model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	super.setTableModel(tableModel);
	updateColumns();
    }

    /**
     * Sets the row selection model for this table to newModel and registers
     * with for listener notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    @Override
    public void setSelectionModel(ListSelectionModel newModel) {
	super.setSelectionModel(newModel);
	if (plot != null) {
	    plot.setSelectionModel(newModel);
	}
    }

    /**
   *
   */
    private void init() {
	setLayout(new BorderLayout());

	top.add(new JLabel("X: "));
	top.add(xChoice);
	top.add(new JLabel("Y: "));
	top.add(yChoice);
	/*
	 * JCheckBox resbtn = new JCheckBox("Residuals",residuals);
	 * resbtn.addItemListener(new ItemListener() { public void
	 * itemStateChanged(ItemEvent e) { residuals = e.getStateChange() ==
	 * ItemEvent.SELECTED; } }); top.add(resbtn);
	 */
	JButton btn = new JButton("Plot");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setColumns();
	    }
	});
	top.add(btn);
	add(top, BorderLayout.NORTH);
	add(plotPanel);
	xChoice.setToolTipText("Select column for X axis");
	yChoice.setToolTipText("Select column for Y axis");
	btn.setToolTipText("Plot the selected columns");
	updateColumns();
    }

    private synchronized void updateColumns() {
	if (tm != null) {
	    boolean needUpdate = xModel.getSize() != tm.getColumnCount();
	    if (!needUpdate) {
		int ncols = tm.getColumnCount();
		for (int c = 0; !needUpdate && c < ncols; c++) {
		    String name = tm.getColumnName(c);
		    String lbl = (String) xModel.getElementAt(c);
		    if (name != null) {
			if (name.equals(lbl)) {
			    needUpdate = true;
			}
		    } else if (lbl != null) {
			needUpdate = true;
		    }
		}
	    }
	    if (needUpdate) {
		try {
		    xModel.removeAllElements();
		    yModel.removeAllElements();
		    int ncols = tm.getColumnCount();
		    for (int c = 0; c < ncols; c++) {
			String name = tm.getColumnName(c);
			xModel.addElement(name);
			yModel.addElement(name);
		    }
		    plotPanel.removeAll();
		    plotPanel.invalidate();
		    plot = null;
		    if (pendingCols != null) {
			setColumns(pendingCols);
		    }
		    validate();
		    repaint();
		} catch (Exception ex) {
		}
	    }
	}
    }

    private void setColumns() {
	if (ctx != null && plotPanel != null) {
	    try {
		ColumnMap xMap = ctx.getColumnMap(tm, xChoice
			.getSelectedIndex());
		ColumnMap yMap = ctx.getColumnMap(tm, yChoice
			.getSelectedIndex());
		if (xMap != null && yMap != null) {
		    if (plot == null
			    || (residuals && !(plot instanceof ResidualPlot))
			    || (!residuals && (plot instanceof ResidualPlot))) {
			plot = residuals ? new ResidualPlot(xMap, yMap)
				: new ScatterPlot(xMap, yMap);
			plot.setSelectionModel(ctx.getRowSelectionModel(tm));
			plot.getPlotComponent().addMouseListener(ma);
			plotPanel.add(plot);
			validate();
		    } else {
			plot.setColumnMaps(xMap, yMap);
		    }
		}
	    } catch (Exception ex) {
		System.err.println("ScatterPlotView.setColumns() " + ex);
		ex.printStackTrace();
	    }
	    repaint();
	}
    }

    /**
     * Set the columns to be view as a scatterplot.
     * 
     * @param x
     *            the index of the column for the x axis.
     * @param y
     *            the index of the column for the y axis.
     */
    public void setColumns(int x, int y) {
	xModel.setSelectedItem(xModel.getElementAt(x));
	yModel.setSelectedItem(yModel.getElementAt(y));
	setColumns();
    }

    /**
     * Set the view to display the columns at the TableModel columns indices
     * (numbered from 0 to number of columns - 1).
     * 
     * @param columns
     *            the indices of the columns to display.
     */
    @Override
    public void setColumns(int[] columns) {
	pendingCols = columns;
	if (xModel.getSize() > 0) {
	    int x = columns != null && columns.length > 0 ? columns[0] : 0;
	    int y = columns != null && columns.length > 1 ? columns[1] : 1;
	    setColumns(x, y);
	}
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return plotPanel;
    }

    /**
     * The TableModelEvent should be constructed in the coordinate system of the
     * model.
     * 
     * @param e
     *            the change to the data model
     */
    @Override
    public void tableChanged(TableModelEvent e) {
	// if (tcm == null)
	// return;
	if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
	    updateColumns();
	}
    }

    /**
     * Returns the preferred size of this view.
     * 
     * @return an instance of Dimension that represents the preferred size of
     *         this view.
     */
    @Override
    public Dimension getPreferredSize() {
	Dimension dim = super.getPreferredSize();
	Dimension topdim = top.getPreferredSize();
	dim.height = topdim.height + dim.width;
	return dim;
    }

    @Override
    protected void finalize() throws Throwable {
	cleanUp();
	super.finalize();
    }

    /**
     * This removes this view as a listener to the TableModel and to the
     * ListSelectionModel. Classes overriding this method should call
     * super.cleanUp();
     */
    @Override
    public void cleanUp() {
	if (plot != null) {
	    plot.cleanUp();
	}
	super.cleanUp();
    }

}
