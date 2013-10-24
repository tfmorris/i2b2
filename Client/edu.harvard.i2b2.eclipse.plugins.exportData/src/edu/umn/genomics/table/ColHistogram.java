/*
 * @(#) $RCSfile: ColHistogram.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.image.BufferedImage;
import java.util.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;
import edu.umn.genomics.layout.BordersLayout;

/**
 * Display a histogram of the values for each column of a table. The display may
 * be used to select rows of the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ColHistogram extends AbstractTableModelView implements
	Serializable {
    JPanel iconPanel = new JPanel(); // panel of icons for Histograms

    JPanel histPanel = new JPanel(); // panel of Histograms

    JCheckBox btns[] = null;

    Hashtable ctoh = new Hashtable(); // column to Histogram JPanel;

    int iw = 20; // Icon width

    int ih = 12; // Icon height

    Histogram hist[];

    AxisDisplay haxis[];

    AxisDisplay vaxis[];

    boolean doSelectCounts = true;

    boolean dataNeeded = true;

    // listen for changes to selection range.
    int prevSetOp = -1;

    int[] pendingCols = null;

    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    if (e.getMapState() == CellMap.INVALID) {
	    } else if (!e.mappingInProgress()) {
		if (e.getSource() instanceof ColumnMap) {
		    addUpdate((ColumnMap) e.getSource());
		}
	    }
	}
    };

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
	    Histogram h = (Histogram) e.getSource();
	    double sr[] = h.getSelectionRange();
	    for (int c = 0; c < hist.length; c++) {
		if (hist[c] == h) {
		    ColumnMap cmap = ctx.getColumnMap(tm, c);
		    double cr = cmap.getMax() - cmap.getMin();
		    double from = cmap.getMin()
			    + (sr[0] < sr[1] ? sr[0] : sr[1]) * cr;
		    double to = cmap.getMin() + (sr[0] < sr[1] ? sr[1] : sr[0])
			    * cr;
		    cmap.selectRange(from, to);
		    break;
		}
	    }
	    if (ctx != null) {
		ctx.getSetOperator(tm).setSetOperator(prevSetOp);
	    }
	}
    };

    // ItemListener for the CheckBox Icon Buttons
    ItemListener showPlot = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	    int c = Integer.parseInt(((JCheckBox) e.getSource())
		    .getActionCommand());
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		((JComponent) e.getSource()).setBackground(Color.black);
		showHistogram(c);
	    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
		((JComponent) e.getSource()).setBackground(Color.lightGray);
		Integer key = new Integer(c);
		Component hp = (Component) ctoh.get(key);
		if (hp != null) {
		    histPanel.remove(hp);
		    ctoh.remove(key);
		    if (hist != null && c < hist.length)
			hist[c] = null;
		}
	    }
	    histPanel.invalidate();
	    validate();
	    setAll();
	    repaint();
	}
    };

    // Queue of icons to make
    Set mapQueue = Collections.synchronizedSet(new HashSet());

    // Thread to make icons
    class MapThread extends Thread {
	@Override
	public void run() {
	    updateMaps();
	}
    };

    MapThread mapThread = null;

    private synchronized void addUpdate(ColumnMap cmap) {
	mapQueue.add(cmap);
	if (mapThread == null) {
	    mapThread = new MapThread();
	    mapThread
		    .setPriority((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 3);
	    mapThread.start();
	}
    }

    private synchronized void updateMaps() {
	while (mapQueue != null && mapQueue.size() > 0) {
	    try {
		for (Iterator i = mapQueue.iterator(); i.hasNext();) {
		    ColumnMap cmap = (ColumnMap) i.next();
		    if (updateMap(cmap))
			i.remove();
		}
	    } catch (ConcurrentModificationException ex) {
	    }
	    if (mapQueue.size() > 0) {
		Thread.yield();
	    }
	}
	mapThread = null;
    }

    private boolean updateMap(ColumnMap cmap) {
	if (cmap == null || cmap.getState() != CellMap.MAPPED)
	    return false;
	int ci = getColumnIndex(cmap);
	// Update Button Icon
	if (btns != null && btns.length > ci) {
	    btns[ci].setIcon(makeIcon(cmap, iw, ih));
	}
	// If Histogram, update
	if (hist != null && hist.length > ci && hist[ci] != null) {
	    // Set Axis?
	    setHistogramData(hist[ci], cmap);
	}
	return true;
    }

    private void init() {
	histPanel.setLayout(new GridLayout(0, 1));
	histPanel.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		dataNeeded = true;
	    }

	    @Override
	    public void componentShown(ComponentEvent e) {
		if (tm != null) {
		    mapColumns();
		}
	    }
	});
	setLayout(new BorderLayout());
	add(histPanel, BorderLayout.CENTER);
	JScrollPane sp = new JScrollPane(iconPanel,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	JToolBar tb = new JToolBar();
	tb.add(sp);
	add(tb, BorderLayout.NORTH);
	mapColumns();
    }

    /**
     * Create a panel that can display histograms of the data in a table.
     */
    public ColHistogram() {
	super();
	init();
    }

    /**
     * Create a panel that displays a histogram for each column in the
     * TableModel, and uses a default selection model.
     * 
     * @param tableModel
     *            the data model
     */
    public ColHistogram(TableModel tableModel) {
	super(tableModel);
	init();
    }

    /**
     * Create a panel that displays a histogram for each column in the
     * TableModel, and uses selectionven selection model.
     * 
     * @param tableModel
     *            contains the table data
     * @param lsm
     *            is the row selection model
     */
    public ColHistogram(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
	init();
    }

    // may want to add params for background and point color
    /**
     * Return an icon that pictures the histogram of the column
     * 
     * @param c
     *            data column for the histogram.
     * @param w
     *            the width for the icon
     * @param h
     *            the height for the icon
     */
    public static Icon makeIcon(ColumnMap c, int w, int h) {
	boolean showSelection = false;
	Histogram hg = new Histogram();
	hg.setSize(w, h);
	hg.setOpaque(true);
	hg.setBackground(Color.white);
	if (c.getState() == CellMap.MAPPED) {
	    int cnts[] = c.getCounts(c.isNumber() || c.isDate() ? w : 0,
		    showSelection);
	    hg.setData(cnts, showSelection);
	}
	BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	hg.paintComponent(bimg.createGraphics());
	return new ImageIcon(bimg);
    }

    private void setIcons() {
	Font lblFont = new Font("monospaced", Font.PLAIN, 10);
	iconPanel.removeAll();
	int ncols = tm.getColumnCount();
	btns = new JCheckBox[ncols];
	for (int c = 0; c < ncols; c++) {
	    ColumnMap cmap = ctx.getColumnMap(tm, c);
	    Icon icon = makeIcon(cmap, iw, ih);
	    JCheckBox b = new JCheckBox(icon);
	    btns[c] = b;
	    b.setHorizontalAlignment(SwingConstants.CENTER);
	    b.setBackground(Color.lightGray);
	    b.setToolTipText(tm.getColumnName(c));
	    b.addItemListener(showPlot);
	    b.setActionCommand("" + c);
	    int len;
	    JPanel jp = new JPanel(new BorderLayout());
	    len = tm.getColumnName(c).length();
	    JLabel jl = new JLabel(tm.getColumnName(c).substring(0,
		    len < 5 ? len : 5));
	    jl.setFont(lblFont);
	    jp.add(jl, BorderLayout.NORTH);
	    jp.add(b, BorderLayout.WEST);
	    iconPanel.add(jp);
	    cmap.addCellMapListener(cml);
	}
	iconPanel.validate();
	iconPanel.repaint();
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
	if (!e.getValueIsAdjusting()) {
	    dataNeeded = true;
	    repaint();
	}
    }

    private void setAll() {
	if (hist == null) {
	    mapColumns();
	}
	if (hist != null && hist.length > 0) {
	    dataNeeded = false;
	    int ncols = hist.length < tm.getColumnCount() ? hist.length : tm
		    .getColumnCount();
	    for (int c = 0; c < ncols; c++) {
		if (hist != null && hist.length > c && hist[c] != null) {
		    Insets insets = hist[c].getInsets();
		    int w = hist[c].getWidth() - insets.left - insets.right;
		    int h = hist[c].getHeight() - insets.top - insets.bottom;
		    haxis[c].getAxis().setSize(w);
		    vaxis[c].getAxis().setSize(h);
		    setHistogramData(hist[c], ctx.getColumnMap(tm, c));
		}
	    }
	}
    }

    /**
     * Supply the histogram data to the Histogram panel.
     */
    private void setHistogramData(Histogram h, ColumnMap c) {
	if (h != null && c != null) {
	    Insets inset = getInsets();
	    int w = getWidth() - inset.left - inset.right - 2;
	    int cnts[] = c.getCounts(c.isNumber() || c.isDate() ? w : 0,
		    doSelectCounts);
	    int ci = getColumnIndex(c);
	    haxis[ci].getAxis().setMin(c.getMin());
	    if (c.isNumber() || c.isDate()) {
		haxis[ci].getAxis().setMax(c.getMax());
	    } else {
		haxis[ci].getAxis().setMax(c.getMax() + 1);
		double[] ticks = new double[(int) c.getMax() + 1];
		for (int i = 0; i < ticks.length; i++) {
		    ticks[i] = i;
		}
		haxis[ci].getAxis().setTicks(ticks);
	    }
	    int max = 0; // the max count for any bucket
	    int offset = doSelectCounts ? 2 : 1;
	    if (cnts != null) {
		for (int i = 0; i < cnts.length; i += offset) {
		    if (cnts[i] > max) {
			max = cnts[i];
		    }
		}
	    }
	    // log10 [ 1/Math.log(10.) ] to get number of places.
	    int d = 1 + (int) Math.floor(Math.log(max) * 0.43429448190325176);
	    d = d < 1 ? 1 : d;
	    double scale = Math.pow(10., d - 1);
	    int vmax = (int) Math.ceil((max + 1) / scale);
	    vaxis[ci].getAxis().setMax(vmax * scale);
	    if (vmax > 5) {
		double[] ticks = new double[vmax / 2 + 1];
		for (int i = 0; i < ticks.length; i++) {
		    ticks[i] = (double) i * 2 * scale;
		}
		vaxis[ci].getAxis().setTicks(ticks);
	    } else {
		double[] ticks = new double[vmax + 1];
		for (int i = 0; i < ticks.length; i++) {
		    ticks[i] = i * scale;
		}
		vaxis[ci].getAxis().setTicks(ticks);
	    }
	    h.setVerticalAxis(vaxis[ci].getAxis());
	    String pat = "#############";
	    ((DecimalFormat) vaxis[ci].getAxisLabeler()).applyPattern(pat
		    .substring(0, d + 1));
	    h.setData(cnts, doSelectCounts);
	}
    }

    @Override
    public void paintComponent(Graphics g) {
	if (isVisible()) {
	    if (dataNeeded) {
		setAll();
	    }
	    super.paintComponent(g);
	}
    }

    /**
     * Create a Histogram panel for each column in the TableModel.
     */
    private void showHistogram(int c) {
	if (tm == null)
	    return;
	if (c < 0 || c >= tm.getColumnCount() || hist == null
		|| c >= hist.length)
	    return;
	Font font = new Font("Serif", Font.PLAIN, 8);
	if (hist[c] == null) {
	    hist[c] = new Histogram();
	    hist[c].addMouseListener(ma);
	    hist[c].setOpaque(true);
	    hist[c].setBackground(Color.white);
	}
	LinearAxis xAxis = new LinearAxis();
	haxis[c] = new AxisDisplay(xAxis, AxisComponent.BOTTOM);
	haxis[c].setAxisLabeler(new ColumnMapLabeler(ctx.getColumnMap(tm, c),
		20));
	LinearAxis yAxis = new LinearAxis();
	yAxis.setMin(0);
	vaxis[c] = new AxisDisplay(yAxis, AxisComponent.LEFT);
	((DecimalFormat) vaxis[c].getAxisLabeler()).applyPattern("###");
	JPanel jp = new JPanel();
	jp.setLayout(new BordersLayout());
	jp.add(vaxis[c], BorderLayout.WEST);
	jp.add(haxis[c], BorderLayout.SOUTH);
	jp.add(hist[c]);
	jp.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), tm.getColumnName(c),
		TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP, // ABOVE_TOP
		// ,
		font, Color.black));
	ctoh.put(new Integer(c), jp);
	histPanel.add(jp);
	validate();
	dataNeeded = true;
	repaint();
    }

    /**
     * Create a Histogram panel for each column in the TableModel.
     */
    private void mapColumns() {
	if (tm == null)
	    return;
	int ncols = tm.getColumnCount();
	setIcons();
	histPanel.removeAll();
	if (hist == null) {
	    hist = new Histogram[ncols];
	    haxis = new AxisDisplay[ncols];
	    vaxis = new AxisDisplay[ncols];
	} else if (hist.length != ncols) {
	    hist = new Histogram[ncols];
	    haxis = new AxisDisplay[ncols];
	    vaxis = new AxisDisplay[ncols];
	}
	validate();
	dataNeeded = true;
	if (pendingCols != null) {
	    setColumns(pendingCols);
	}
	repaint();
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
		for (int i = 0; i < columns.length; i++) {
		    showHistogram(columns[i]);
		}
	    }
	}
    }

    /**
     * Display a histogram for each column of data in tthe table. usage: java
     * edu.umn.genomics.table.ColHistogram filename The file should have a
     * format that the FileTableModel class can read. If there are no arguments,
     * random data will be generated.
     * 
     * @param args
     *            the filename of the table data
     * @see FileTableModel
     */
    public static void main(String[] args) {
    }
}
