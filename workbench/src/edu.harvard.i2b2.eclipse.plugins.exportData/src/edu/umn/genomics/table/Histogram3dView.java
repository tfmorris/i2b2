/*
 * @(#) $RCSfile: Histogram3dView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.border.*;

/**
 * Histogram3dView displays a scatter plot of the two selected columns of the
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
public class Histogram3dView extends AbstractTableModelView implements
	Serializable {

    DefaultHistogramModel hgm = new DefaultHistogramModel();
    BinModelEditorDialog editorDialog = null;
    DefaultComboBoxModel xModel = new DefaultComboBoxModel();
    DefaultComboBoxModel yModel = new DefaultComboBoxModel();
    JComboBox xChoice = new JComboBox(xModel);
    JComboBox yChoice = new JComboBox(yModel);
    JToolBar top = new JToolBar();
    JSlider tilt;
    JSlider rotation;
    JSpinner spinner;
    JCheckBox selectOnly;
    JPanel plotPanel = new JPanel(new BorderLayout());
    public Histogram3dDisplay canvas = null;
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
    public Histogram3dView() {
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
    public Histogram3dView(TableModel tableModel) {
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
    public Histogram3dView(TableModel tableModel, ListSelectionModel lsm) {
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
	if (hgm != null) {
	    hgm.setDataModel(tableModel);
	}
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
	if (hgm != null) {
	    hgm.setListSelectionModel(newModel);
	}
    }

    /**
     * Return the HistogramModel being displayed.
     * 
     * @return the HistogramModel being displayed.
     */
    public HistogramModel getHistogramModel() {
	return hgm;
    }

    /**
   *
   */
    private void init() {
	setLayout(new BorderLayout());

	canvas = ctx != null ? new Histogram3dDisplay(hgm, ctx
		.getSetOperator(tm)) : new Histogram3dDisplay(hgm);

	// top.add(new JLabel("X: "));
	xChoice.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Column X",
		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP));

	top.add(xChoice);
	yChoice.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Column Y",
		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP));
	top.add(yChoice);

	spinner = new JSpinner(new SpinnerNumberModel(canvas.getBarWidth(), .1,
		1., .1));
	spinner.setToolTipText("Adjust BarWidth");
	spinner.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		canvas.setBarWidth(((Number) spinner.getValue()).doubleValue());
	    }
	});
	JPanel spinPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
	spinPanel.setToolTipText("Adjust Bar Size");
	spinPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Bar Size",
		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP));
	spinPanel.add(spinner);
	top.add(spinPanel);
	selectOnly = new JCheckBox();
	selectOnly.setToolTipText("Show selected portion only");
	selectOnly.setSelected(canvas.getSelectBarsOnly());
	selectOnly.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		canvas
			.setSelectBarsOnly(e.getStateChange() == ItemEvent.SELECTED);
	    }
	});
	top.add(selectOnly);
	JButton btn = new JButton("Edit");
	btn.setToolTipText("Edit the Binning Models");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		editBinModels();
	    }
	});
	top.add(btn);

	btn = new JButton("Plot");
	btn.setToolTipText("Plot the selected columns");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setColumns();
	    }
	});
	top.add(btn);

	add(top, BorderLayout.NORTH);
	plotPanel.add(canvas);
	add(plotPanel);
	tilt = new JSlider(SwingConstants.VERTICAL, 0, 500, 500 - (int) (canvas
		.getTilt() * 1000));
	tilt.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		// if (!source.getValueIsAdjusting()) {
		int tilt = source.getValue();
		canvas.setTilt((500 - tilt) * .001);
		// }
	    }
	});
	rotation = new JSlider(SwingConstants.HORIZONTAL, -1000, 1000,
		(int) (canvas.getRotation() * 1000));
	rotation.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		// if (!source.getValueIsAdjusting()) {
		int rot = source.getValue();
		canvas.setRotation(rot * .001);
		// }
	    }
	});

	plotPanel.add(tilt, BorderLayout.EAST);
	plotPanel.add(rotation, BorderLayout.SOUTH);
	xChoice.setToolTipText("Select column for X axis");
	yChoice.setToolTipText("Select column for Y axis");
	updateColumns();
    }

    public void setTilt(double value) {
	canvas.setTilt(value);
	tilt.setValue((int) (canvas.getTilt() * 1000));
    }

    public double getTilt() {
	return canvas.getTilt();
    }

    public void setRotation(double value) {
	canvas.setRotation(value);
	rotation.setValue((int) (canvas.getRotation() * 1000));
    }

    public double getRotation() {
	return canvas.getRotation();
    }

    public void setBarWidth(double value) {
	canvas.setBarWidth(value);
	spinner.setValue(new Double(canvas.getBarWidth()));
    }

    public double getBarWidth() {
	return canvas.getBarWidth();
    }

    public void setSelectBarsOnly(boolean showSelectedOnly) {
	canvas.setSelectBarsOnly(showSelectedOnly);
	selectOnly.setSelected(showSelectedOnly);
    }

    public boolean getSelectBarsOnly() {
	return canvas.getSelectBarsOnly();
    }

    private void editBinModels() {
	if (editorDialog == null) {
	    editorDialog = new BinModelEditorDialog((JFrame) this
		    .getTopLevelAncestor(), hgm);
	}
	editorDialog.setVisible(true);
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
		    // plotPanel.removeAll();
		    plotPanel.invalidate();
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
		if (xMap != null) {
		    hgm.setBinModel(0, new ColumnMapBinModel(xMap));
		}
		ColumnMap yMap = ctx.getColumnMap(tm, yChoice
			.getSelectedIndex());
		if (yMap != null) {
		    hgm.setBinModel(1, new ColumnMapBinModel(yMap));
		}
	    } catch (Exception ex) {
		System.err.println("Histogram3dView.setColumns() " + ex);
		ex.printStackTrace();
	    }
	    repaint();
	}
    }

    /**
     * Set the columns to be viewed.
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
	if (xModel.getSize() > 0 && yModel.getSize() > 0) {
	    int x = columns != null && columns.length > 0 ? columns[0] : 0;
	    int y = columns != null && columns.length > 1 ? columns[1] : 1;
	    setColumns(x, y);
	}
    }

    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @param ctx
     *            The context to use for TableModels and Views.
     */
    @Override
    public void setTableContext(TableContext ctx) {
	super.setTableContext(ctx);
	if (canvas != null) {
	    canvas.setSetOperator(ctx.getSetOperator(tm));
	}
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return canvas;
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    public Histogram3dDisplay getDisplay() {
	return canvas;
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
	if (canvas != null) {
	    // canvas.cleanUp();
	}
	super.cleanUp();
    }

}
