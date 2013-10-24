/*
 * @(#) $RCSfile: ScatterPlotIcons.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import edu.umn.genomics.graph.LinearAxis;

/**
 * Display scatterplots of the values of pairs of columns of a table. Each pair
 * of columns is represented by a checkbox button containing an icon of the
 * scatterplot for the pair. Selecting the button displays the scatterplot in
 * the drawing region of the panel. Deselecting the button removes the
 * scatterplot from the drawing region.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ScatterPlotIcons extends AbstractTableModelView implements
	Serializable {
    Hashtable btorc = new Hashtable();// button to row/column
    JButton[][] btns = null;
    JScrollPane jsp = null;
    JPanel iconPanel = new JPanel(); // panel of icons for scatterplots
    JPanel leftPanel = new JPanel(new GridLayout(0, 1)); // column labels
    JPanel topPanel = new JPanel(new GridLayout(1, 0)); // column labels
    boolean useBufferedImage = false; // is JDK1.2 BufferedImage class available
    int iw = 20; // Icon width
    int ih = iw; // Icon height
    int bw = 2; // borderwidth
    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    // System.err.println(((ColumnMap)e.getSource()).getName() + " " +
	    // e.getMapState());
	    if (e.getMapState() == CellMap.INVALID) {
	    } else if (!e.mappingInProgress()) {
		if (e.getSource() instanceof ColumnMap) {
		    int ci = getColumnIndex((ColumnMap) e.getSource());
		    if (ci < btns.length) {
			for (int i = 0; i < btns[ci].length; i++) {
			    addIconUpdate(btns[ci][i]);
			}
		    }
		    for (int i = 0; i < btns.length; i++) {
			if (i != ci && ci < btns[i].length) {
			    addIconUpdate(btns[i][ci]);
			}
		    }
		}
	    }
	}
    };

    // ActionListener for the Icon Buttons
    ActionListener showPlot = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    int[] ia = (int[]) btorc.get(e.getSource());
	    if (ia != null && ia.length >= 2) {
		try {
		    String viewName = "ScatterPlot";
		    ScatterPlotView view = (ScatterPlotView) ctx.getView(tm,
			    viewName);
		    view.setColumns(ia[0], ia[1]);
		    JFrame frame = ctx.addView(tm, viewName, view);
		    frame.setVisible(true);
		} catch (Exception ex) {
		    ScatterPlotView view = new ScatterPlotView(tm, lsm);
		    view.setColumns(ia[0], ia[1]);
		    JFrame frame = new JFrame("ScatterPlot");
		    frame.getContentPane().add(view);
		    DefaultTableContext.setViewToolBar(frame, view.getCanvas());
		    frame.pack();
		    frame.setVisible(true);
		}
	    }
	}
    };
    // Queue of icons to make [r,c]
    Set iconQueue = Collections.synchronizedSet(new HashSet());

    // Thread to make icons
    class MapThread extends Thread {
	@Override
	public void run() {
	    updateIcons();
	}
    };

    MapThread mapThread = null;

    private synchronized void addIconUpdate(JButton b) {
	iconQueue.add(b);
	if (mapThread == null) {
	    mapThread = new MapThread();
	    mapThread
		    .setPriority((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 3);
	    mapThread.start();
	}
    }

    // Use PixIcon for JDK1.1, since there is no BufferedImage class
    class PixIcon implements Icon {
	int iw;
	int ih;
	int pixels[];

	PixIcon(int w, int h, int pix[]) {
	    iw = w;
	    ih = h;
	    pixels = pix;
	}

	public int getIconWidth() {
	    return iw;
	}

	public int getIconHeight() {
	    return ih;
	}

	public void paintIcon(Component c, Graphics g, int ix, int iy) {
	    for (int x = 0; x < iw; x++) {
		for (int y = 0; y < ih; y++) {
		    int i = y * iw + x;
		    if (i < pixels.length)
			g.setColor(new Color(pixels[i]));
		    g.drawLine(ix + x, iy + y, ix + x, iy + y);
		}
	    }
	}
    };

    private void init() {
	try {
	    Font font = new Font("Serif", Font.PLAIN, 8);
	    // setFont(font);
	} catch (Exception ex) {
	    System.err.println(ex);
	}
	JPanel iP1 = new JPanel(new BorderLayout());
	iP1.add(iconPanel, BorderLayout.NORTH);
	JPanel iP = new JPanel(new BorderLayout());
	iP.add(iP1, BorderLayout.WEST);
	JPanel lP = new JPanel(new BorderLayout());
	lP.add(leftPanel, BorderLayout.NORTH);
	JPanel tP = new JPanel(new BorderLayout());
	tP.add(topPanel, BorderLayout.WEST);
	setLayout(new BorderLayout());
	jsp = new JScrollPane(iP,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	jsp.setColumnHeaderView(tP);
	jsp.setRowHeaderView(lP);
	add(jsp);
	try {
	    Class.forName("java.awt.image.BufferedImage");
	    useBufferedImage = true;
	} catch (ClassNotFoundException ex) {
	    useBufferedImage = false;
	}
	addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentShown(ComponentEvent e) {
		if (tm != null) {
		    mapColumns();
		}
	    }
	});
	mapColumns();
    }

    /**
     * Create an empty ScatterPlotIcons Panel.
     */
    public ScatterPlotIcons() {
	super();
	init();
    }

    /**
     * Create an ScatterPlotIcons Panel with an icon for each pair of columns in
     * the TableModel. The data in the TableModel will be mapped by ColumnMaps
     * provided by the TableContext. The ScatterPlotIcons uses the
     * ListSelectionModel from the TableContext.
     * 
     * @param tableModel
     *            the data.
     */
    public ScatterPlotIcons(TableModel tableModel) {
	super(tableModel);
	init();
    }

    /**
     * Create an ScatterPlotIcons Panel with an icon for each pair of columns in
     * the TableModel. The data in the TableModel will be mapped by ColumnMaps
     * provided by the TableContext. The ScatterPlotIcons uses the given
     * ListSelectionModel.
     * 
     * @param tableModel
     *            the data.
     * @param lsm
     *            the row selection model for the data table.
     */
    public ScatterPlotIcons(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
	init();
    }

    /**
     * Sets tableModel as the data model for the view.
     * 
     * @param tableModel
     *            the data model for the view
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	if (tableModel != tm) {
	    super.setTableModel(tableModel);
	    mapColumns();
	}
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
	repaint();
    }

    /**
     * Create an iconic button for each pair of columns in the data model.
     */
    private void mapColumns() {
	if (tm != null) {
	    int ncols = tm.getColumnCount();
	    btns = new JButton[ncols][ncols];
	    for (int r = 0; r < ncols; r++) {
		btns[r] = new JButton[ncols];
	    }
	    iconPanel.removeAll();
	    btorc.clear();
	    Font lblFont = new Font("monospaced", Font.PLAIN, 8);
	    JLabel jl;
	    leftPanel.removeAll();
	    topPanel.removeAll();
	    iconPanel.removeAll();
	    iconPanel.setLayout(new GridLayout(ncols, ncols));
	    for (int r = 0; r < ncols; r++) {
		JButton b = null;
		for (int c = 0; c < ncols; c++) {
		    Icon icon = makeIcon(null, null, iw, ih);
		    b = new JButton(icon);
		    b.setMargin(new Insets(0, 0, 0, 0));
		    b.setHorizontalAlignment(SwingConstants.CENTER);
		    b.setBorder(BorderFactory.createLineBorder(Color.lightGray,
			    bw));
		    b.setToolTipText(tm.getColumnName(r) + ","
			    + tm.getColumnName(c));

		    int ia[] = new int[2];
		    ia[0] = r;
		    ia[1] = c;
		    btorc.put(b, ia);
		    b.addActionListener(showPlot);
		    iconPanel.add(b);
		    btns[r][c] = b;
		}
		JLabel lbl;
		String tip = tm.getColumnName(r) + "  mean: "
			+ ctx.getColumnMap(tm, r).getAvg() + " stddev: "
			+ ctx.getColumnMap(tm, r).getStdDev();
		lbl = new LeftLabel(tm.getColumnName(r),
			b.getPreferredSize().height);
		lbl.setToolTipText(tip);
		leftPanel.add(lbl);
		lbl = new TopLabel(tm.getColumnName(r),
			b.getPreferredSize().width);
		lbl.setToolTipText(tip);
		topPanel.add(lbl);
	    }
	    // Queue button to makeIcon
	    for (int r = 0; r < ncols; r++) {
		ColumnMap cm = ctx.getColumnMap(tm, r);
		cm.addCellMapListener(cml);
		for (int c = 0; c < ncols; c++) {
		    addIconUpdate(btns[r][c]);
		}
	    }
	    iconPanel.invalidate();
	    validate();
	    repaint();
	}
    }

    private synchronized void updateIcons() {
	while (iconQueue != null && iconQueue.size() > 0) {
	    try {
		for (Iterator i = iconQueue.iterator(); i.hasNext();) {
		    JButton b = (JButton) i.next();
		    if (updateIcon(b))
			i.remove();
		}
	    } catch (ConcurrentModificationException ex) {
	    }
	    if (iconQueue.size() > 0) {
		Thread.yield();
	    }
	}
	mapThread = null;
    }

    private boolean updateIcon(JButton b) {
	int ia[] = (int[]) btorc.get(b);
	int r = ia[0]; // row
	int c = ia[1]; // column
	ColumnMap cm1 = ctx.getColumnMap(tm, r);
	ColumnMap cm2 = ctx.getColumnMap(tm, c);
	if (cm1.getState() != CellMap.MAPPED
		|| cm1.getState() != CellMap.MAPPED)
	    return false;
	b.setIcon(makeIcon(cm1, cm2, iw, ih));
	b.setMargin(new Insets(0, 0, 0, 0));
	float corr = (float) cm1.correlation(cm2);
	float blue = Float.isNaN(corr) || Float.isInfinite(corr) ? 0f
		: (corr + 1f) / 2f;
	blue = blue < 0f ? 0f : blue > 1f ? 1f : blue;
	float red = Float.isNaN(corr) || Float.isInfinite(corr) ? 0f
		: 1f - blue;
	red = red < 0f ? 0f : red > 1f ? 1f : red;
	float green = Float.isNaN(corr) || Float.isInfinite(corr) ? 0f
		: .5f - Math.abs(corr) / 2f;
	green = green < 0f ? 0f : green > 1f ? 1f : green;
	b.setBorder(BorderFactory.createLineBorder(new Color(red, green, blue),
		bw));
	b.setToolTipText(tm.getColumnName(r) + "," + tm.getColumnName(c)
		+ " correlation: " + corr);
	return true;
    }

    // may want to add params for background and point color
    /**
     * Return an icon that pictures the scatterplot for a pair of columns.
     * 
     * @param xColumn
     *            data column for the x axis value
     * @param yColumn
     *            data column for the y axis value
     * @param w
     *            the width for the icon
     * @param h
     *            the height for the icon
     */
    public Icon makeIcon(ColumnMap xColumn, ColumnMap yColumn, int w, int h) {
	int pix[] = new int[w * h]; // pixel array
	// initialize pixels to a color
	for (int i = 0; i < pix.length; i++) {
	    pix[i] = 0xffffff; // white background for now
	}
	if (xColumn != null && yColumn != null) {
	    int nrows = xColumn.getCount();
	    LinearAxis xAxis = new LinearAxis(); // axes to map x values to
	    // pixel offsett
	    LinearAxis yAxis = new LinearAxis(); // axes to map y values to
	    // pixel offsett
	    xAxis.setSize(w - 1); // range from 0 - w-1
	    xAxis.setMin(xColumn.getMin());
	    xAxis.setMax(xColumn.getMax());
	    yAxis.setSize(h - 1); // range from 0 - h-1
	    yAxis.setMin(yColumn.getMin());
	    yAxis.setMax(yColumn.getMax());
	    int bl = h - 1; // baseline for y values;
	    // map each data row to a pixel location
	    for (int r = 0; r < nrows; r++) {
		int x = xAxis.getIntPosition(xColumn.getMapValue(r));
		int y = bl - yAxis.getIntPosition(yColumn.getMapValue(r));
		int i = y * w + x; // pixel offset
		try {
		    // accumulate the points that map to this pixel
		    // this should be more general
		    pix[i] = 0x8888ff - ((pix[i] / 2) & 0xffff00);
		} catch (Exception ex) {
		}
	    }
	}
	if (useBufferedImage) {
	    BufferedImage bimg = new BufferedImage(w, h,
		    BufferedImage.TYPE_INT_RGB);
	    bimg.setRGB(0, 0, w, h, pix, 0, w);
	    return new ImageIcon(bimg);
	} else {
	    return new PixIcon(w, h, pix);
	}
    }
}

class LeftLabel extends JLabel {
    int pw = 20;

    public LeftLabel(String text, int w) {
	super(text);
	pw = w;
    }

    @Override
    public Dimension getPreferredSize() {
	Dimension dim = super.getPreferredSize();
	dim.height = pw;
	return dim;
    }
}

class TopLabel extends JLabel {
    int pw = 20;

    public TopLabel(String text, int w) {
	super(text);
	pw = w;
    }

    @Override
    public Dimension getPreferredSize() {
	Dimension dim = super.getPreferredSize();
	dim.width = pw;
	return dim;
    }
}

/*
 * class TopLabel extends JComponent { int pw = 20; String text = "hello";
 * public TopLabel(String text, int w) { //super(text); //setMargin(new
 * Insets(1,1,1,1)); this.text = text; pw = w; } public String getText() {
 * return text; } public void paintComponent(Graphics g) {
 * g.setColor(Color.yellow); g.fillRect(1,1,5,5); if (g instanceof Graphics2D) {
 * g.setColor(Color.cyan); g.fillRect(10,10,5,5); g.setColor(Color.magenta);
 * g.drawString(getText(),10, 10); g.drawLine(2,getWidth() / 2,0 , getWidth() /
 * 2); Graphics2D g2 = (Graphics2D)g; g2.setColor(Color.black);
 * g.drawString(getText(),getHeight()-2,getWidth() / 2);
 * g.drawLine(getHeight()-2,getWidth() / 2,0 , getWidth() / 2); AffineTransform
 * saveXform = g2.getTransform(); AffineTransform rotXform = new
 * AffineTransform(); rotXform.rotate(Math.toRadians(90));
 * g2.setTransform(rotXform); //super.paintComponent(g2);
 * g2.setColor(Color.orange); g.drawLine(getHeight()-2,getWidth() / 2,0 ,
 * getWidth() / 2); g2.setColor(getForeground()); g2.setColor(Color.black);
 * g2.drawString(getText(),10,10); g2.setTransform(saveXform); } } public
 * Dimension getPreferredSize() { Dimension dim = super.getPreferredSize();
 * dim.height = 40; FontMetrics fm = getFontMetrics(getFont()); dim.height =
 * fm.stringWidth(getText()); dim.width = pw; return dim; } }
 */

