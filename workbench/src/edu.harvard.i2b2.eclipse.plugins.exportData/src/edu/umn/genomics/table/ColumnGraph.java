/*
 * @(#) $RCSfile: ColumnGraph.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import edu.umn.genomics.layout.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;

/**
 * A ColumnGraph displays the values of a table as a line graph, with one line
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
public class ColumnGraph extends AbstractTableModelView implements Serializable {
    public Axis xAxis = new LinearAxis();
    public AxisDisplay axisDisp = new AxisDisplay(xAxis, AxisComponent.TOP);
    public JPanel gpanel = new JPanel(new RelativeLayout());
    public JPanel tpanel = new JPanel(new RelativeLayout());
    public JScrollPane jsp;
    Font font = new Font("Serif", Font.PLAIN, 10);
    private int tabWidth = 10;
    Hashtable extents = new Hashtable();
    Vector gds = new Vector();
    Point anchor = null;
    JLabel value = new JLabel();
    private int prevSetOp = -1;
    private int setOp = SetOperator.REPLACE;
    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    if (e.getMapState() == CellMap.INVALID) {
	    } else if (!e.mappingInProgress()) {
		mapColumns();
	    }
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
	    Component c = e.getComponent();
	    GraphDisplay gd = (GraphDisplay) c;
	    double v[] = gd.getValueAt(p);
	    value.setText(((int) v[0])
		    + ","
		    + (ctx.getColumnMap(tm, gds.indexOf(gd)).getMappedValue(
			    v[1], 0)));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    Point p = e.getPoint();
	    Component c = e.getComponent();
	    GraphDisplay gd = (GraphDisplay) c;
	    double v[] = gd.getValueAt(p);
	    value.setText(((int) v[0])
		    + ","
		    + (ctx.getColumnMap(tm, gds.indexOf(gd)).getMappedValue(
			    v[1], 0)));
	    if (ctx.getSetOperator(tm).getSetOperator() == SetOperator.BRUSHOVER) {
		int ei = (int) v[0];
		lsm.clearSelection();
		lsm.addSelectionInterval(ei, ei);
	    }
	}
    };
    /**
     * Selection of a graph panel to drag.
     */
    private MouseAdapter pma = new MouseAdapter() {
	@Override
	public void mouseClicked(MouseEvent e) {
	    Point p = e.getPoint();
	    /*
	     * Component c = e.getComponent(); Extents ce =
	     * (Extents)extents.get(c); Component tc =
	     * (Component)extents.get(ce); gpanel.remove(tc); gpanel.add(tc,0);
	     * // need extents tpanel.remove(c); tpanel.add(c,0); // need
	     * extents
	     */
	    repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    Point p = e.getPoint();
	    anchor = p;
	    Component c = e.getComponent();
	    c.setForeground(Color.blue);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    Point p = e.getPoint();
	    anchor = null;
	    Component c = e.getComponent();
	    c.setForeground(Color.black);
	}
    };
    /**
     * Drag the graph panel.
     */
    private MouseMotionAdapter pmma = new MouseMotionAdapter() {
	@Override
	public void mouseDragged(MouseEvent e) {
	    Point p = e.getPoint();
	    int offset = p.y - anchor.y;
	    Component c = e.getComponent();
	    Rectangle cb = c.getBounds();
	    Dimension pd = c.getParent().getSize();
	    Extents ce = (Extents) extents.get(c);
	    Component tc = (Component) extents.get(ce);
	    Rectangle tcb = tc.getBounds();
	    cb.y += offset;
	    if (cb.y < 0)
		cb.y = 0;
	    else if (cb.y + cb.height > pd.height)
		cb.y = pd.height - cb.height;
	    tcb.y = cb.y;
	    c.setBounds(cb);
	    tc.setBounds(tcb);
	    ce.fromY = (double) cb.y / pd.height;
	    ce.toY = (double) (cb.y + cb.height) / pd.height;
	}
    };

    private void initListeners() {
	// this.addComponentListener(ca);
	// this.addMouseListener(ma);
	// this.addMouseMotionListener(mma);
	setOpaque(true);
	setBackground(Color.white);
	setLayout(new BorderLayout());
	// axisDisp.setBorder(BorderFactory.createEtchedBorder());

	((DecimalFormat) axisDisp.getAxisLabeler()).applyPattern("###");
	gpanel.setOpaque(false);
	tpanel.setOpaque(true);
	tpanel.setBackground(Color.lightGray);

	gpanel.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		Dimension d = gpanel.getSize();
		Dimension dt = tpanel.getSize();
		Dimension da = axisDisp.getSize();
		dt.height = d.height;
		tpanel.setSize(dt);
		da.width = d.width;
		Graphics g = axisDisp.getGraphics();
		if (g != null) {
		    setTicks(g);
		}
		axisDisp.setSize(da);
	    }
	});

	// ip.setBorder(BorderFactory.createEmptyBorder(0,tabWidth,0,0));
	value.setOpaque(true);
	add(value, BorderLayout.NORTH);
	jsp = new JScrollPane(gpanel);
	jsp.setRowHeaderView(tpanel);
	jsp.setColumnHeaderView(axisDisp);
	JLabel rowLbl = new JLabel("Row #", SwingConstants.RIGHT);
	jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, rowLbl);
	add(jsp, BorderLayout.CENTER);

	mapColumns();
	axisDisp.setZoomable(true);
    }

    /**
     * Constructs a ColumnGraph display. Nothing will be displayed until a data
     * model is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ColumnGraph() {
	super();
	initListeners();
    }

    /**
     * Constructs a ColumnGraph diaplay which is initialized with tableModel as
     * the data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ColumnGraph(TableModel tableModel) {
	super(tableModel);
	initListeners();
    }

    /**
     * Constructs a ColumnGraph diaplay which is initialized with tableModel as
     * the data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public ColumnGraph(TableModel tableModel, ListSelectionModel lsm) {
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
     * Return whether the given row is selected.
     * 
     * @param r
     *            the row being inquired about
     * @return true if the row is selected, else false
     */
    private boolean rowSelected(int r) {
	return lsm.isSelectedIndex(r);
    }

    /**
     * The TableModelEvent should be constructed in the coordinate system of the
     * model, the appropriate mapping to the view coordinate system is performed
     * by the ColumnGraph when it receives the event.
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
	gpanel.removeAll();
	tpanel.removeAll();
	extents.clear();
	gds.removeAllElements(); // gds.clear();
	if (tm != null) {
	    int ncols = tm.getColumnCount();
	    int nrows = tm.getRowCount();
	    xAxis.setMin(0.);
	    xAxis.setMax(nrows - 1.);
	    double rh = ncols > 0 ? 1. / ncols : 0.;
	    Insets ginsets = axisDisp.getInsets();
	    for (int c = 0; c < ncols; c++) {
		ColumnMap colMap = ctx.getColumnMap(tm, c);
		if (colMap != null) {
		    Axis yAxis = new LinearAxis();
		    yAxis.setMin(colMap.getMin());
		    yAxis.setMax(colMap.getMax());
		    GraphDisplay gd = new GraphDisplay();
		    /*
		     * gd.setBorder(BorderFactory.createTitledBorder(
		     * BorderFactory.createEtchedBorder(), tm.getColumnName(c),
		     * TitledBorder.DEFAULT_JUSTIFICATION,
		     * TitledBorder.BELOW_TOP, //ABOVE_TOP, font, Color.black
		     * ));
		     */

		    gd.setXAxis(xAxis);
		    gd.setYAxis(yAxis);
		    GraphLine gl = new GraphLine();
		    gl.setData(colMap.getMapValues());
		    gl.setColor(Color.getHSBColor((float) (c * rh), 1f, .3f));
		    gd.setForeground(Color.black);
		    gd.setOpaque(false);
		    gd.addGraphItem(gl);
		    gd.addMouseListener(ma);
		    gd.addMouseMotionListener(mma);
		    gds.addElement(gd);
		    Extents extent = new Extents(.0, c * rh, 1., (c + 1) * rh);
		    JPanel ip = new JPanel(new BorderLayout());
		    ip.setOpaque(false);
		    JLabel tab = new JLabel(tm.getColumnName(c));
		    ip.setBorder(BorderFactory.createEtchedBorder());
		    Insets insets = ip.getInsets();
		    gd.setBorder(BorderFactory.createEmptyBorder(insets.top,
			    ginsets.left, insets.bottom, ginsets.right));
		    ip.addMouseListener(pma);
		    ip.addMouseMotionListener(pmma);
		    ip.add(tab, BorderLayout.WEST);
		    AxisDisplay vaxis = new AxisDisplay(yAxis,
			    AxisComponent.LEFT);
		    ip.add(vaxis, BorderLayout.EAST);
		    extents.put(ip, extent);
		    extents.put(extent, gd);
		    gpanel.add(gd, extent);
		    tpanel.add(ip, extent);
		}
	    }
	}
	gpanel.invalidate();
	validate();
    }

    private void setTicks(Graphics g) {
	double ti = ((LinearAxis) xAxis).getTickIncrement();
	int cw = 12; // char width
	Font f = axisDisp.getFont();
	if (f != null) {
	    FontMetrics fm = g.getFontMetrics(f);
	    cw = fm.getMaxAdvance();
	}
	int nrow = tm.getRowCount();
	// pixels per label / pixels per row
	double lw = Math.ceil(Math.log(nrow) * 0.43429448190325176) * cw;
	double ppr = (axisDisp.getWidth() / (double) nrow);
	double tn = Math.ceil(Math.log(nrow) * 0.43429448190325176) * cw
		/ (axisDisp.getWidth() / (double) nrow);
	double nti = ti;
	int exp = (int) Math.floor(Math.log(tn) * 0.43429448190325176);
	char lc = '1';
	if (exp < 0) {
	    lc = Double.toString(Math.pow(tn, exp)).charAt(0);
	} else {
	    lc = Double.toString(tn).charAt(0);
	}
	if (lc == '1') {
	    nti = Math.pow(10, exp);
	} else if (lc < '5') {
	    nti = Math.pow(10, exp) * 2;
	} else if (lc <= '9') {
	    nti = Math.pow(10, exp) * 5;
	}
	if (nti < 1.)
	    nti = 1.;
	((LinearAxis) xAxis).setTickIncrement(nti);
	// System.err.println("ti " + lw + " " + ppr + " " + lc + " " + tn + " "
	// + nti + " " + exp);
    }

    /**
     * Paint the background.
     * 
     * @param g
     *            the graphics context.
     */
    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	if (!lsm.isSelectionEmpty()) {
	    Color c = g.getColor();
	    Insets insets = getInsets();
	    int xoff = axisDisp.getLocationOnScreen().x
		    - getLocationOnScreen().x + axisDisp.getInsets().left;
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
