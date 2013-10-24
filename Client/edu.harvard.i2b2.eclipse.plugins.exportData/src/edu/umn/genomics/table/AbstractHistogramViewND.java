/*
 * @(#) $RCSfile: AbstractHistogramViewND.java,v $ $Revision: 1.4 $ $Date: 2008/10/29 20:58:46 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * AbstractHistogramViewND displays a scatter plot of the two selected columns
 * of the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/29 20:58:46 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public abstract class AbstractHistogramViewND extends AbstractTableModelView
	implements Serializable {

    DefaultHistogramModel hgm = new DefaultHistogramModel();
    BinModelEditorDialog editorDialog = null;
    JMenu addMenu = new JMenu("Add");
    JMenu delMenu = new JMenu("Delete");
    JMenu setMenu = new JMenu("Set");
    JMenu monthByYearMenu = new JMenu("Compare Months from Year to Year");
    JMenu quarterByYearMenu = new JMenu("Compare Quarters from Year to Year");
    ActionListener monthByYearListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    JMenuItem mi = (JMenuItem) e.getSource();
	    String colName = mi.getText();
	    int idx = Integer.parseInt(mi.getActionCommand());
	    setYearToYearMonthly(idx);
	}
    };

    ActionListener quarterByYearListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    JMenuItem mi = (JMenuItem) e.getSource();
	    String colName = mi.getText();
	    int idx = Integer.parseInt(mi.getActionCommand());
	    setYearToYearQuarterly(idx);
	}
    };

    Hashtable menuItemToBinModel = new Hashtable();

    JToolBar top = new JToolBar();
    JPanel plotPanel = new JPanel(new BorderLayout());
    JComponent canvas;
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

    ActionListener colAddListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    JMenuItem mi = (JMenuItem) e.getSource();
	    String colName = mi.getText();
	    int idx = Integer.parseInt(mi.getActionCommand());
	    addColumn(idx);
	}
    };

    ActionListener colDelListener = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    JMenuItem mi = (JMenuItem) e.getSource();
	    BinModel bm = (BinModel) menuItemToBinModel.get(mi);
	    hgm.removeBinModel(bm);
	    menuItemToBinModel.remove(bm);
	    delMenu.remove(mi);
	}
    };

    private void addDelItem(String name, ColumnMapBinModel bm) {
	JMenuItem mi = delMenu.add(name);
	menuItemToBinModel.put(mi, bm);
	mi.addActionListener(colDelListener);
    }

    private void addColumn(int idx) {
	try {
	    ColumnMap cMap = ctx.getColumnMap(tm, idx);
	    if (cMap != null) {
		String name = cMap.getName();
		ColumnMapBinModel bm = new ColumnMapBinModel(cMap);
		hgm.addBinModel(bm);
		addDelItem(name, bm);
	    }
	} catch (Exception ex) {
	    System.err.println(this.getClass() + ".addColumn(" + idx + ") "
		    + ex);
	}
    }

    private int[] getColumnIndices(int newIdx) {
	int cnt = hgm != null ? hgm.getModelCount() : 0;
	int[] idx = new int[cnt + (newIdx >= 0 ? 1 : 0)];
	for (int i = 0; i < cnt; i++) {
	    idx[i] = ((ColumnMapBinModel) hgm.getBinModel(i)).getColumnMap()
		    .getColumnIndex();
	}
	if (newIdx >= 0) {
	    idx[cnt] = newIdx;
	}
	return idx;
    }

    /**
     * Constructs a view display. Nothing will be displayed until a data model
     * is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public AbstractHistogramViewND() {
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
    public AbstractHistogramViewND(TableModel tableModel) {
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
    public AbstractHistogramViewND(TableModel tableModel, ListSelectionModel lsm) {
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

    protected abstract JComponent createCanvas();

    /**
   *
   */
    private void init() {
	setLayout(new BorderLayout());
	JMenuBar mb = new JMenuBar();
	mb.add(addMenu);
	mb.add(delMenu);
	setMenu.add(monthByYearMenu);
	setMenu.add(quarterByYearMenu);
	mb.add(setMenu);
	top.add(mb);
	JButton btn = new JButton("Edit");
	btn.setToolTipText("Open an editor to change the histogram");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		editBinModels();
	    }
	});
	top.add(btn);

	add(top, BorderLayout.NORTH);
	canvas = createCanvas();
	plotPanel.add(canvas);
	add(plotPanel);
	updateColumns();
    }

    private void editBinModels() {
	if (editorDialog == null) {
	    editorDialog = new BinModelEditorDialog((JFrame) this
		    .getTopLevelAncestor(), hgm);
	}
	editorDialog.setVisible(true);
    }

    private Vector getTableColumnNames() {
	Vector colNames = new Vector();
	for (int i = 0; i < tm.getColumnCount(); i++) {
	    colNames.add(tm.getColumnName(i));
	}
	return colNames;
    }

    private synchronized void updateColumns() {
	if (tm != null) {
	    boolean needUpdate = addMenu.getItemCount() != tm.getColumnCount();
	    if (!needUpdate) {
		int ncols = tm.getColumnCount();
		for (int c = 0; !needUpdate && c < ncols; c++) {
		    String name = tm.getColumnName(c);
		    String lbl = addMenu.getItem(c).getText();
		    if (name != null) {
			if (!name.equals(lbl)) {
			    needUpdate = true;
			}
		    } else if (lbl != null) {
			needUpdate = true;
		    }
		}
	    }
	    if (needUpdate) {
		try {
		    addMenu.removeAll();
		    int ncols = tm.getColumnCount();
		    for (int c = 0; c < ncols; c++) {
			String name = tm.getColumnName(c);
			JMenuItem mi = addMenu.add(name);
			mi.setActionCommand("" + c);
			mi.addActionListener(colAddListener);
			ColumnMap cMap = ctx.getColumnMap(tm, c);
			if (cMap.isDate()) {
			    // monthByYearMenu
			    mi = monthByYearMenu.add(name);
			    mi.setActionCommand("" + c);
			    mi.addActionListener(monthByYearListener);
			    // quarterByYearMenu
			    mi = quarterByYearMenu.add(name);
			    mi.setActionCommand("" + c);
			    mi.addActionListener(quarterByYearListener);
			}
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
	setColumns(getColumnIndices(-1));
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
	if (ctx != null && plotPanel != null) {
	    java.util.List binModelList = hgm.getBinModelList();
	    Vector cmapList = new Vector();
	    // Remove models that aren't in list
	    for (int i = binModelList.size() - 1; i >= 0; i--) {
		ColumnMapBinModel bm = (ColumnMapBinModel) binModelList.get(i);
		ColumnMap cmap = bm.getColumnMap();
		boolean isSelected = false;
		for (int ci = 0; ci < columns.length; ci++) {
		    ColumnMap xMap = ctx.getColumnMap(tm, columns[ci]);
		    if (xMap == cmap) {
			isSelected = true;
			break;
		    }
		}
		if (isSelected) {
		    cmapList.add(cmap);
		} else {
		    hgm.removeBinModel(bm);
		}
	    }
	    // Add new models
	    for (int i = 0; i < columns.length; i++) {
		try {
		    ColumnMap xMap = ctx.getColumnMap(tm, columns[i]);
		    if (xMap != null && !cmapList.contains(xMap)) {
			hgm.addBinModel(new ColumnMapBinModel(xMap));
		    }
		} catch (Exception ex) {
		    System.err.println(this.getClass() + ".setColumns() " + ex);
		    ex.printStackTrace();
		}
	    }
	    // Update delMenu
	    delMenu.removeAll();
	    binModelList = hgm.getBinModelList();
	    for (int i = 0; i < binModelList.size(); i++) {
		ColumnMapBinModel bm = (ColumnMapBinModel) binModelList.get(i);
		ColumnMap cmap = bm.getColumnMap();
		addDelItem(cmap.getName(), bm);
	    }
	}
	repaint();
    }

    public void setYearToYearMonthly(int column) {
	ColumnMap cmap = ctx.getColumnMap(tm, column);
	if (cmap.isDate()) {
	    Object obj = cmap.getMappedValue(cmap.getMin(), 0);
	    Date date = obj instanceof Date ? (Date) obj : new Date();
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    int year = cal.get(Calendar.YEAR);
	    cal.clear();
	    cal.set(Calendar.YEAR, year);
	    setDateView(cmap, Calendar.MONTH, null, cal.getTime(),
		    Calendar.YEAR, 1);
	}
    }

    public void setYearToYearQuarterly(int column) {
	ColumnMap cmap = ctx.getColumnMap(tm, column);
	if (cmap.isDate()) {
	    Object obj = cmap.getMappedValue(cmap.getMin(), 0);
	    Date date = obj instanceof Date ? (Date) obj : new Date();
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    int year = cal.get(Calendar.YEAR);
	    cal.clear();
	    cal.set(Calendar.YEAR, year);
	    String[] labels = { "Q1", "Q2", "Q3", "Q4" };
	    setDateView(cmap, Calendar.MONTH, labels, cal.getTime(),
		    Calendar.YEAR, 1);
	}
    }

    public void setDateView(ColumnMap cmap, int calendarPartition,
	    String[] labels, Date startDate, int calendarRange, int incr) {
	if (cmap.isDate()) {
	    ColumnMapBinModel bmp = new ColumnMapBinModel(cmap);
	    BinModelEditor.setDatePartition(bmp, cmap, calendarPartition,
		    labels);
	    ColumnMapBinModel bmr = new ColumnMapBinModel(cmap);
	    BinModelEditor.setDateRange(bmr, cmap, startDate, calendarRange,
		    incr);
	    hgm.removeBinModels();
	    hgm.addBinModel(bmp);
	    hgm.addBinModel(bmr);
	    // Update delMenu
	    delMenu.removeAll();
	    addDelItem(cmap.getName(), bmp);
	    addDelItem(cmap.getName(), bmr);
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
     * The TableModelEvent should be constructed in the coordinate system of the
     * model.
     * 
     * @param e
     *            the change to the data model
     */
    @Override
    public void tableChanged(TableModelEvent e) {
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
	if (canvas != null && canvas instanceof CleanUp) {
	    ((CleanUp) canvas).cleanUp();
	}
	super.cleanUp();
    }

}
