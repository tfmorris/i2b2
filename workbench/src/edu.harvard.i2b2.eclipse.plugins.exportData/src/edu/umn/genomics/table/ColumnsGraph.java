/*
 * @(#) $RCSfile: ColumnsGraph.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;

/**
 * A ColumnsGraph displays the values of a table as a line graph, with one line
 * per column in the table. The horizontal axis represents the range of rows in
 * the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ColumnsGraph extends AbstractTableModelView implements
	Serializable {
    SimpleGraph graph;
    Font font = new Font("Serif", Font.PLAIN, 10);
    private int tabWidth = 10;
    Point anchor = null;
    private int prevSetOp = -1;
    private int setOp = SetOperator.REPLACE;
    DefaultListModel colLM = new DefaultListModel();
    JList colList = new JList(colLM);
    DefaultComboBoxModel labelModel = new DefaultComboBoxModel();
    JComboBox labelChoice = new JComboBox(labelModel);
    GraphItem colGraph[] = null;
    Color colColor[] = null;
    int labelColumn = -1;
    int[] pendingCols = null;

    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    if (e.getMapState() == CellMap.INVALID) {
	    } else if (!e.mappingInProgress()) {
		mapColumns();
	    }
	}
    };
    /**
     * Listener for labeler selection
     */
    private ItemListener labelListener = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	    setLabeler();
	}
    };
    AxisLabeler rowlabeler = new AxisLabeler() {
	public String getLabel(double value) {
	    return getRowLabel(value);
	}
    };

    /**
     * Selection of rows on a graph display.
     */
    private MouseAdapter ma = new MouseAdapter() {
	@Override
	public void mouseClicked(MouseEvent e) {
	    Point p = e.getPoint();
	    repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    Point p = e.getPoint();
	    anchor = p;
	    if (ctx != null) {
		prevSetOp = ctx.getSetOperator(tm).getSetOperator();
		ctx.getSetOperator(tm).setFromInputEventMask(e.getModifiers());
	    }
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    if (anchor != null) {
		Point p = e.getPoint();
		Component c = e.getComponent();
		GraphDisplay gd = (GraphDisplay) c;
		int si = (int) gd.getValueAt(anchor)[0];
		int ei = (int) gd.getValueAt(p)[0];
		int mn = si < ei ? si : ei;
		int mx = ei > si ? ei : si;
		if (mn < 0)
		    mn = 0;
		if (mx < 0)
		    mx = 0;
		int min;
		int max;
		if (lsm != null) {
		    lsm.setValueIsAdjusting(true);
		    if (ctx != null) {
			setOp = ctx.getSetOperator(tm).getSetOperator();
		    }
		    switch (setOp) {
		    case SetOperator.REPLACE:
		    case SetOperator.BRUSHOVER:
			lsm.clearSelection();
		    case SetOperator.UNION:
			lsm.addSelectionInterval(mn, mx);
			break;
		    case SetOperator.DIFFERENCE:
			lsm.removeSelectionInterval(mn, mx);
			break;
		    case SetOperator.INTERSECTION:
			if (mn > 0)
			    lsm.removeSelectionInterval(0, mn - 1);
			max = lsm.getMaxSelectionIndex();
			if (mx < max)
			    lsm.removeSelectionInterval(mx + 1, max);
			break;
		    case SetOperator.XOR:
			min = lsm.getMinSelectionIndex();
			max = lsm.getMaxSelectionIndex();
			if (mn < min) {
			    lsm.addSelectionInterval(mn, min - 1);
			    mn = min;
			}
			if (mx > max) {
			    lsm.addSelectionInterval(max + 1, mx);
			    mx = max;
			}
			for (int r = mn; r <= mx; r++) {
			    if (lsm.isSelectedIndex(r)) {
				lsm.removeSelectionInterval(r, r);
			    } else {
				lsm.addSelectionInterval(r, r);
			    }
			}
			break;
		    }
		    lsm.setValueIsAdjusting(false);
		}
	    }
	    anchor = null;
	    if (ctx != null) {
		ctx.getSetOperator(tm).setSetOperator(prevSetOp);
	    }
	}
    };
    /**
     * Selection of rows on a graph display.
     */
    private MouseMotionAdapter mma = new MouseMotionAdapter() {
	@Override
	public void mouseDragged(MouseEvent e) {
	    Point p = e.getPoint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    Point p = e.getPoint();
	}
    };

    /**
     * Return a label for the given point on the graph axis
     * 
     * @param value
     *            the value on the graph
     * @return the label for the given value
     */
    private String getRowLabel(double value) {
	String label = Double.toString(value);
	try {
	    int r = (int) value;
	    if (r < 0 || r >= tm.getRowCount()) {
		return "";
	    }
	    if (labelColumn >= 0 && labelColumn < tm.getColumnCount()) {
		Object o = tm.getValueAt(r, labelColumn);
		return o != null ? o.toString() : "";
	    }
	} catch (Exception ex) {
	}
	try {
	    label = Integer.toString((int) value);
	} catch (Exception ex) {
	}
	return label;
    }

    private void setLabeler() {
	int ncols = tm.getColumnCount();
	labelColumn = labelChoice.getSelectedIndex();
	repaint();
	if (labelColumn >= 0 && labelColumn < ncols) {
	    graph.getAxisDisplay(BorderLayout.NORTH).setAxisLabeler(rowlabeler);
	} else {
	    graph.getAxisDisplay(BorderLayout.NORTH).setAxisLabeler(
		    new DecimalLabeler("###"));
	}
    }

    private void initListeners() {
	// this.addComponentListener(ca);
	// this.addMouseListener(ma);
	// this.addMouseMotionListener(mma);

	graph = new SimpleGraph();
	graph.getGraphDisplay().setOpaque(true);
	graph.getGraphDisplay().setBackground(Color.white);
	graph.getGraphDisplay().setGridColor(new Color(220, 220, 220));
	graph.showGrid(true);
	graph.showAxis(BorderLayout.SOUTH, false);
	graph.showAxis(BorderLayout.NORTH, true);
	graph.getAxisDisplay(BorderLayout.WEST).setZoomable(true);
	graph.getAxisDisplay(BorderLayout.NORTH).setZoomable(true);
	graph.getGraphDisplay().addMouseListener(ma);
	graph.getGraphDisplay().addMouseMotionListener(mma);

	setOpaque(true);
	setBackground(Color.white);
	graph.getAxisDisplay(BorderLayout.NORTH).setAxisLabeler(
		new DecimalLabeler("###"));
	mapColumns();

	setLayout(new BorderLayout());
	colList.setToolTipText("Select columns to compare");
	colList
		.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	colList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting())
		    makeGraphs();
	    }
	});
	JScrollPane jsp = new JScrollPane(colList,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	if (true) {
	    labelChoice.setToolTipText("Label Row Axis by:");
	    labelChoice.addItemListener(labelListener);
	    JPanel cPanel = new JPanel(new BorderLayout());
	    cPanel.add(labelChoice, BorderLayout.NORTH);
	    cPanel.add(jsp, BorderLayout.CENTER);
	    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		    true, cPanel, graph);
	    split.setOneTouchExpandable(true);
	    add(split);

	} else {
	    add(jsp, BorderLayout.WEST);
	    add(graph);
	}
    }

    /**
     * Constructs a ColumnsGraph display. Nothing will be displayed until a data
     * model is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ColumnsGraph() {
	super();
	initListeners();
    }

    /**
     * Constructs a ColumnsGraph diaplay which is initialized with tableModel as
     * the data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ColumnsGraph(TableModel tableModel) {
	super(tableModel);
	initListeners();
    }

    /**
     * Constructs a ColumnsGraph diaplay which is initialized with tableModel as
     * the data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public ColumnsGraph(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
	initListeners();
    }

    /**
     * Sets tableModel as the data model for the view.
     * 
     * @param tableModel
     *            the data model for the view
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	if (tm != tableModel) {
	    super.setTableModel(tableModel);
	    mapColumns();
	}
    }

    /**
     * The TableModelEvent should be constructed in the coordinate system of the
     * model, the appropriate mapping to the view coordinate system is performed
     * by the ColumnsGraph when it receives the event.
     * 
     * @param e
     *            the change to the data model
     */
    @Override
    public void tableChanged(TableModelEvent e) {
	// if (tcm != null)
	// return;
	if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
	    // The whole thing changed
	    lsm.clearSelection();
	    mapColumns();
	    repaint();
	    return;
	}
	if (e.getType() == TableModelEvent.INSERT) {
	    lsm.clearSelection();
	    mapColumns();
	    repaint();
	    return;
	}
	if (e.getType() == TableModelEvent.DELETE) {
	    lsm.clearSelection();
	    mapColumns();
	    repaint();
	    return;
	}
	if (e.getType() == TableModelEvent.UPDATE) {
	    lsm.clearSelection();
	    mapColumns();
	    repaint();
	    return;
	}
	lsm.clearSelection();
	mapColumns();
	repaint();
    }

    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e
     *            the event that characterizes the change in selection.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
	repaint();
    }

    /**
     * Map the data in the table to the display.
     */
    private void mapColumns() {
	if (tm != null) {
	    int ncols = tm.getColumnCount();
	    int nrows = tm.getRowCount();
	    double rh = ncols > 0 ? 1. / ncols : 0.;
	    colGraph = new GraphItem[ncols];
	    colColor = new Color[ncols];
	    GraphItem gi[] = graph.getGraphItems();
	    for (int i = 0; i < gi.length; i++) {
		graph.removeGraphItem(gi[i]);
	    }
	    Axis xAxis = graph.getAxisDisplay(BorderLayout.NORTH).getAxis();
	    xAxis.setMin(0.);
	    xAxis.setMax(nrows - 1.);
	    colLM.clear();
	    labelModel.removeAllElements();
	    for (int c = 0; c < ncols; c++) {
		colColor[c] = Color.getHSBColor((float) (c * rh), 1f, .3f);
		colLM.addElement(tm.getColumnName(c));
		labelModel.addElement(tm.getColumnName(c));
	    }
	    labelModel.addElement("Row Numbers");
	    labelChoice.setSelectedIndex(labelChoice.getItemCount() - 1);
	    colList.setCellRenderer(new MyCellRenderer(colColor));
	    if (pendingCols != null) {
		setColumns(pendingCols);
	    }
	}
    }

    private void makeGraphs() {
	int ncols = tm.getColumnCount();
	int nrows = tm.getRowCount();
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;
	for (int c = 0; c < ncols; c++) {
	    if (colList.isSelectedIndex(c)) {
		ColumnMap colMap = ctx.getColumnMap(tm, c);
		if (colMap.getMin() < min)
		    min = colMap.getMin();
		if (colMap.getMax() > max)
		    max = colMap.getMax();
		if (colGraph[c] == null) {
		    if (colMap != null) {
			GraphLine gl = new GraphLine();
			gl.setData(colMap.getMapValues());
			gl.setColor(colColor[c]);
			graph.addGraphItem(gl);
			colGraph[c] = gl;
		    }
		}
	    } else {
		if (colGraph[c] != null) {
		    graph.removeGraphItem(colGraph[c]);
		    colGraph[c] = null;
		}
	    }
	}
	Axis yAxis = graph.getAxisDisplay(BorderLayout.WEST).getAxis();
	if (graph.getGraphItems().length == 0) {
	    min = 0.;
	    max = 10.;
	}
	yAxis.setMin(min);
	yAxis.setMax(max);
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
	if (columns != null) {
	    pendingCols = columns;
	    if (tm != null && tm.getColumnCount() > 0) {
		colList.setSelectedIndices(columns);
	    }
	}
    }

    /*
     * private void setTicks(Graphics g) { AxisDisplay xAxisDisp =
     * graph.getAxisDisplay(BorderLayout.NORTH); Axis xAxis =
     * xAxisDisp.getAxis(); double ti = ((LinearAxis)xAxis).getTickIncrement();
     * int cw = 12; // char width Font f = xAxisDisp.getFont(); if (f != null) {
     * FontMetrics fm = g.getFontMetrics(f); cw = fm.getMaxAdvance(); } int nrow
     * = tm.getRowCount(); // pixels per label / pixels per row double lw =
     * Math.ceil(Math.log(nrow) 0.43429448190325176) cw; double ppr =
     * (xAxisDisp.getWidth() / (double)nrow); double tn =
     * Math.ceil(Math.log(nrow) 0.43429448190325176) cw / (xAxisDisp.getWidth()
     * / (double)nrow); double nti = ti; int exp = (int)Math.floor(Math.log(tn)
     * 0.43429448190325176); char lc = '1'; if (exp < 0) { lc =
     * Double.toString(Math.pow(tn,exp)).charAt(0); } else { lc =
     * Double.toString(tn).charAt(0); } if (lc == '1') { nti = Math.pow(10,exp);
     * } else if (lc < '5') { nti = Math.pow(10,exp) 2; } else if (lc <= '9') {
     * nti = Math.pow(10,exp) 5; } if (nti < 1.) nti = 1.;
     * ((LinearAxis)xAxis).setTickIncrement(nti); //System.err.println("ti " +
     * lw + " " + ppr + " " + lc + " " + tn + " " + nti + " " + exp); }
     */

    /**
     * Paint the background.
     * 
     * @param g
     *            the graphics context.
     */
    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	g.setColor(getBackground());
	AxisDisplay xAxisDisp = graph.getAxisDisplay(BorderLayout.NORTH);
	Axis xAxis = xAxisDisp.getAxis();
	if (!lsm.isSelectionEmpty()) {
	    Color c = g.getColor();
	    Insets insets = getInsets();
	    int xoff = xAxisDisp.getLocationOnScreen().x
		    - getLocationOnScreen().x + xAxisDisp.getInsets().left;
	    int h = getHeight() - insets.top - insets.bottom;
	    int y = insets.top;
	    // g.setColor(getBackground().brighter());
	    Color sc = getBackground();
	    g.setColor(new Color(sc.getRed() - 20, sc.getGreen() - 20, sc
		    .getBlue() - 20));
	    int mn = lsm.getMinSelectionIndex();
	    int mx = lsm.getMaxSelectionIndex();
	    boolean sel = true;
	    for (int r = mn, a = mn; r <= mx; r++) {
		boolean rs = lsm.isSelectedIndex(r);
		if (sel && (!rs || r == mx)) {
		    int xl = xoff + xAxis.getIntPosition(a);
		    if (a == r) {
			g.drawLine(xl, y, xl, y + h);
		    } else {
			int xr = xoff + xAxis.getIntPosition(r);
			g.fillRect(xl, y, xr - xl, h);
		    }
		    sel = false;
		} else if (!sel && rs) {
		    a = r;
		    sel = true;
		}
	    }
	    g.setColor(c);
	}
    }
}

class MyCellRenderer extends JLabel implements ListCellRenderer {
    // final static ImageIcon longIcon = new ImageIcon("long.gif");
    // final static ImageIcon shortIcon = new ImageIcon("short.gif");
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.
    Color color[] = null;
    boolean bg = !true;

    MyCellRenderer(Color c[]) {
	color = c;
    }

    public Component getListCellRendererComponent(JList list, Object value, // value
	    // to
	    // display
	    int index, // cell index
	    boolean isSelected, // is the cell selected
	    boolean cellHasFocus) // the list and the cell have the focus
    {
	String s = value.toString();
	setText(s);
	// setIcon((s.length() > 10) ? longIcon : shortIcon);
	if (isSelected) {

	    if (bg && color != null && index < color.length
		    && color[index] != null)
		setBackground(color[index].brighter());
	    else
		setBackground(list.getSelectionBackground());
	    if (!bg && color != null && index < color.length
		    && color[index] != null)
		setForeground(color[index]);
	    else
		setForeground(list.getSelectionForeground());
	} else {
	    setBackground(list.getBackground());
	    // if (color != null && index < color.length && color[index] !=
	    // null)
	    // setForeground(color[index]);
	    // else
	    setForeground(list.getForeground());
	}
	setEnabled(list.isEnabled());
	setFont(list.getFont());
	setOpaque(true);
	return this;
    }
}
