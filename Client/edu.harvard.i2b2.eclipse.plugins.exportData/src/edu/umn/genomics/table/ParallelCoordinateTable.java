/*
 * @(#) $RCSfile: ParallelCoordinateTable.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
 * ParallelCoordinateTable
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ParallelCoordinateTable extends AbstractTableModelView implements
	Serializable, TableModelView, CleanUp, TableModelListener,
	ListSelectionListener {

    JTabbedPane tabPane = new JTabbedPane();

    ParallelCoordinatePanel pcp = new ParallelCoordinatePanel();
    JPanel graphPanel = new JPanel(new BorderLayout());
    JPanel tablePanel = new JPanel(new BorderLayout());
    JComboBox selColumn = new JComboBox();
    // JComboBox colorColumn = new JComboBox();
    JSpinner rowHeight = new JSpinner(new SpinnerNumberModel(64, 8, 800, 1));
    JTable jt = null;

    TableModelListener tml = new TableModelListener() {
	public void tableChanged(TableModelEvent e) {
	    if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
		mapColumns();
		repaint();
		return;
	    }
	}
    };

    /**
     * Initializes a view display.
     */
    private void init() {
	selColumn.setToolTipText("Select Column for graphs.");
	// colorColumn.setToolTipText("Select Column for color.");
	rowHeight.setToolTipText("Set Height of Graphs.");
	selColumn.setBorder(BorderFactory.createTitledBorder("Graph Column"));
	//colorColumn.setBorder(BorderFactory.createTitledBorder("Color Column")
	// );
	rowHeight.setBorder(BorderFactory.createTitledBorder("Graph Height"));

	JToolBar tb = new JToolBar();
	tb.add(selColumn);
	// tb.add(colorColumn);

	tb.add(rowHeight);
	JButton update = new JButton("Update");
	update.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		showTable();
	    }
	});
	// tb.add(update);
	graphPanel.add(tb, BorderLayout.NORTH);
	graphPanel.add(tablePanel);
	tabPane.addTab("Row Graphs", null, graphPanel,
		"Display A Row Graph for each value of the Column.");
	tabPane.addTab("Graph Prototype", null, pcp,
		"Sets the appearance of the graph.");

	tabPane.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		if (tabPane.getSelectedIndex() == 0) {
		    showTable();
		}
	    }
	});

	selColumn.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    showTable();
		}
	    }
	});

	selColumn.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    // showTable();
		}
	    }
	});

	rowHeight.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		if (jt != null) {
		    jt.setRowHeight(((Number) rowHeight.getValue()).intValue());
		    repaint();
		}
	    }
	});

	setLayout(new BorderLayout());
	add(tabPane);
    }

    /**
     * Constructs a view display. Nothing will be displayed until a data model
     * is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ParallelCoordinateTable() {
	super();
	init();
    }

    /**
     * Constructs a Parallel Coordinate diaplay which is initialized with
     * tableModel as the data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ParallelCoordinateTable(TableModel tableModel) {
	super(tableModel);
	init();
    }

    /**
     * Constructs a Parallel Coordinate diaplay which is initialized with
     * tableModel as the data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public ParallelCoordinateTable(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
	init();
    }

    /**
     * Sets tableModel as the data model for the column being mapped.
     * 
     * @param tableModel
     *            the data model
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	super.setTableModel(tableModel);
	pcp.setTableModel(tableModel);
	mapColumns();
	showTable();
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
	pcp.setSelectionModel(newModel);
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
	pcp.setTableContext(ctx);
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return jt;
    }

    /**
     * Map the data in the table to the parallel coordinate display.
     */
    private void mapColumns() {
	selColumn.removeAllItems();
	// colorColumn.removeAllItems();
	if (tm == null) {
	    return;
	}
	for (int colIndex = 0; colIndex < tm.getColumnCount(); colIndex++) {
	    selColumn.addItem(tm.getColumnName(colIndex));
	    // colorColumn.addItem(tm.getColumnName(colIndex));
	}
    }

    public void launch(String value, int[] rowIndices) {
	try {
	    ParallelCoordinatePanel newPcp = (ParallelCoordinatePanel) pcp
		    .clone();
	    newPcp.setRowDisplaySelection(rowIndices);
	    getTableContext().addView(newPcp.getTableModel(),
		    "Compare Rows" + (value != null ? " of " + value : ""),
		    newPcp);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private void showTable() {
	if (tm != null) {
	    int colIndex = selColumn.getSelectedIndex();
	    if (colIndex >= 0 && colIndex < tm.getColumnCount()) {
		tablePanel.removeAll();
		int widths[] = null;
		if (jt != null) {
		    widths = new int[jt.getModel().getColumnCount()];
		    for (int i = 0; i < widths.length; i++) {
			widths[i] = jt.getColumnModel().getColumn(i).getWidth();
		    }
		}
		jt = pcp.getTableDisplay(colIndex);
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jt.setRowHeight(((Number) rowHeight.getValue()).intValue());
		if (widths != null) {
		    for (int i = 0; i < widths.length; i++) {
			jt.getTableHeader().setResizingColumn(
				jt.getColumnModel().getColumn(i));
			jt.getColumnModel().getColumn(i).setWidth(widths[i]);
			jt.getTableHeader().setResizingColumn(null);
		    }
		}
		jt.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
			    JTable tbl = (JTable) e.getSource();
			    int ci = tbl.convertColumnIndexToModel(tbl
				    .columnAtPoint(e.getPoint()));
			    if (ci == 1) {
				int ri = tbl.rowAtPoint(e.getPoint());
				if (ri >= 0) {
				    int[] indices = (int[]) tbl.getModel()
					    .getValueAt(ri, ci);
				    if (indices != null) {
					Object obj = tbl.getModel().getValueAt(
						ri, 0);
					String val = obj != null ? obj
						.toString() : null;
					launch(val, indices);
				    }
				}
			    }
			}
		    }
		});
		tablePanel.add(new JScrollPane(jt));
		validate();
		repaint();
	    }
	}
    }
}
