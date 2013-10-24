/*
 * @(#) $RCSfile: ParallelCoordinatePanel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.AxisDisplay;

/*
 Inspired by http://csgrad.cs.vt.edu/~agoel/parallel_coordinates/
 */

/**
 * A ParallelCoordinatePanel displays the values of a table as a parallel
 * coordinate display. Each column of a table is represented as a coordinate
 * line in the panel. The coordinate lines are drawn as parallel lines in the
 * display. Each column in the table is mapped to normalized values along a
 * parallel coordinate. Numeric values (derived from the Number class) are
 * mapped with the minimum value at the bottom of the coordinate line and the
 * maximum value at the top of the coordinate line. Other Object types are
 * mapped so that distinct values are evenly spaced along the parallel
 * coordinate for the column. Each row of the table is plotted as a polyline
 * across the coordinate lines. Rows of the table may be selected by taking a
 * range of values along any column coordinate line.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ParallelCoordinatePanel extends AbstractTableModelView implements
	Cloneable, Serializable {

    /**
     * This class manages the 2D array of Y values for the polylines. All
     * changes to the size of the array are done here. The dataPoints array has
     * an array of Y Values for row of the table. The X points are common for
     * each row and are in colPos array. To draw a polyline for table row r :
     * g.drawPolyline(colPos, dataPoints[r], colPos.length);
     * 
     */
    class PointArray {
	int nrows = 0;
	int ncols = 0;
	int[][] dataPoints = new int[nrows][ncols];

	/**
	 * Return the array of arrays of Y values
	 */
	private synchronized int[][] getArray() {
	    return dataPoints;
	}

	/**
	 * Create a new array
	 */
	private synchronized void createArray(int nrows, int ncols) {
	    this.nrows = nrows;
	    this.ncols = ncols;
	    dataPoints = new int[nrows][ncols];
	    for (int r = 0; r < nrows; r++) {
		dataPoints[r] = new int[ncols];
	    }
	    ;
	}

	/**
	 * Add rows to an existing array, inserting them if fromRow is less than
	 * then current array length, or else appending the new rows. The new
	 * length will be at least toRow+1 in length. Since array length can not
	 * be modified, we must create a new array and assign each of the row
	 * arrays to it.
	 */
	private synchronized void addRows(int fromRow, int toRow) {
	    // insert or append?
	    int cnt = toRow < nrows ? toRow - fromRow + 1 : toRow + 1 - nrows;
	    nrows += cnt;
	    int[][] dPnts = new int[nrows][ncols];
	    // copy the first contiguous rows, before the insertion point, to
	    // dPnts
	    if (fromRow > 0) {
		System.arraycopy(dataPoints, 0, dPnts, 0,
			fromRow < dataPoints.length ? fromRow
				: dataPoints.length);
	    }
	    // create and add the new rows in dPnts, start from the lesser of
	    // fromRow or dataPoints.length
	    for (int r = fromRow < dataPoints.length ? fromRow
		    : dataPoints.length; r <= toRow; r++) {
		dPnts[r] = new int[ncols];
	    }
	    // copy the last contiguous rows, after the insertion point, to
	    // dPnts
	    if (fromRow < dataPoints.length) {
		System.arraycopy(dataPoints, fromRow, dPnts, toRow + 1,
			dataPoints.length - fromRow);
	    }
	    dataPoints = dPnts;
	}

	/**
	 * Delete the range of rows, from fromRow to toRow inclusive, from the
	 * data array.
	 */
	private synchronized void delRows(int fromRow, int toRow) {
	    if (fromRow >= nrows)
		return;
	    int cnt = toRow < nrows ? toRow - fromRow + 1 : nrows - fromRow;
	    nrows -= cnt;
	    int[][] dPnts = new int[nrows][ncols];
	    // copy first contiguous rows to dPnts
	    if (fromRow > 0) {
		System.arraycopy(dataPoints, 0, dPnts, 0, fromRow);
	    }
	    // copy last contiguous rows to dPnts
	    if (toRow + 1 < dataPoints.length) {
		System.arraycopy(dataPoints, toRow + 1, dPnts, fromRow,
			dataPoints.length - (toRow + 1));
	    }
	    dataPoints = dPnts;
	}

	/**
	 * Add a column to the data array, inserting a column if colIdx is less
	 * than the current number of columns, else appending columns so that
	 * colIdx is the highest column.
	 */
	private synchronized void addCol(int colIdx) {
	    if (colIdx < 0)
		return;
	    ncols = colIdx < ncols ? ncols + 1 : colIdx + 1;
	    for (int r = 0; r < dataPoints.length; r++) {
		int[] row = dataPoints[r];
		dataPoints[r] = new int[ncols];
		// copy first contiguous cols
		if (colIdx > 0) {
		    System.arraycopy(row, 0, dataPoints[r], 0, colIdx);
		}
		// copy last contiguous cols
		if (colIdx < row.length) {
		    System.arraycopy(row, colIdx, dataPoints[r], colIdx + 1,
			    row.length - colIdx);
		}
	    }
	}

	/**
	 * Delete the column from the data array.
	 */
	private synchronized void delCol(int colIdx) {
	    if (colIdx < 0 || colIdx >= ncols)
		return;
	    ncols -= 1;
	    for (int r = 0; r < dataPoints.length; r++) {
		int[] row = dataPoints[r];
		dataPoints[r] = new int[ncols];
		// copy first contiguous cols
		if (colIdx > 0) {
		    System.arraycopy(row, 0, dataPoints[r], 0, colIdx);
		}
		// copy last contiguous cols
		if (colIdx < row.length) {
		    System.arraycopy(row, colIdx, dataPoints[r], colIdx + 1,
			    row.length - colIdx);
		}
	    }
	}

	/**
	 * Swap two columns in the dataPnts array.
	 */
	private synchronized void swapCols(int c1, int c2) {
	    int tmp;
	    for (int r = 0; r < dataPoints.length; r++) {
		tmp = dataPoints[r][c1];
		dataPoints[r][c1] = dataPoints[r][c2];
		dataPoints[r][c2] = tmp;
	    }
	}
    }

    /**
     * Draw the Column Name above each displayed column.
     */
    class ColLbl extends JPanel {
	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    int cPos[] = colPos;
	    int cIdx[] = colIdx;
	    if (cPos != null) {
		Color bg = getBackground();
		Color fg = getForeground();
		Color mg = new Color((bg.getRed() + fg.getRed()) / 2, (bg
			.getGreen() + fg.getGreen()) / 2, (bg.getBlue() + fg
			.getBlue()) / 2);
		FontMetrics fm = g.getFontMetrics();
		for (int c = 0; c < cPos.length; c++) {
		    String colName = tm.getColumnName(cIdx[c]);
		    if (colName == null) {
			colName = "";
		    }
		    int len = fm.stringWidth(colName);
		    int lm = c == 0 ? 0 : cPos[c] - (cPos[c] - cPos[c - 1]) / 2
			    + 2;
		    int rm = (c == cPos.length - 1 ? getWidth() : cPos[c]
			    + (cPos[c + 1] - cPos[c]) / 2) - 6;
		    int pos = cPos[c] - len / 2;
		    if (pos < lm) {
			pos = lm;
		    }
		    Rectangle clip = g.getClipBounds();
		    if (pos + len >= rm) {
			g.setClip(lm, 0, rm - lm + 6, getHeight());
			Color color = g.getColor();
			g.setColor(mg);
			g.drawString(colName, pos, (fm.getAscent() + 1));
			g.setColor(color);
		    }
		    g.setClip(lm, 0, rm - lm, getHeight());
		    g.drawString(colName, pos, (fm.getAscent() + 1));
		    g.setClip(clip);
		}
	    }
	}

	@Override
	public Dimension getPreferredSize() {
	    try {
		FontMetrics fm = getGraphics().getFontMetrics();
		return new Dimension(24, fm.getHeight() + 4);
	    } catch (Exception ex) {
		System.err.println("ColLbl.getPreferredSize " + ex);
	    }
	    return new Dimension(24, 24);
	}
    }

    /**
     * Draw a Shape at each displayed column.
     */
    class ColWgt extends JComponent {
	int[] xpnt = { 0, -arrowSize, arrowSize };
	int[] ypnt = { arrowSize, 1, 1 };
	Polygon p = new Polygon(xpnt, ypnt, xpnt.length);

	ColWgt() {
	    super();
	    setPreferredSize(new Dimension(arrowSize * 2 + 4, arrowSize + 2));
	}

	protected boolean isFilled(int c) {
	    return true;
	}

	protected Shape getShape(int c) {
	    return p;
	}

	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	    int cPos[] = colPos;
	    if (cPos != null) {
		FontMetrics fm = g2.getFontMetrics();
		for (int c = 0; c < cPos.length; c++) {
		    if (isSelectedColumn(c)) {
			g2.setColor(Color.black);
		    } else {
			g2.setColor(Color.gray);
		    }
		    int x = cPos[c];
		    g2.translate(x, 0);
		    g2.draw(getShape(c));
		    if (isFilled(c)) {
			g2.fill(getShape(c));
		    }
		    g2.translate(-x, 0);
		}
	    }
	}
    }

    /**
     * A widget that will be used to drag columns to new positions.
     */
    class ColMov extends ColWgt {
	int[] xpnt = { 0, -arrowSize, 0, arrowSize };
	int[] ypnt = { 1, arrowSize / 2, arrowSize, arrowSize / 2 };

	ColMov() {
	    super();
	    p = new Polygon(xpnt, ypnt, xpnt.length);
	}

	@Override
	protected boolean isFilled(int c) {
	    int tmColIdx = colIdx[c];
	    return clsm.isSelectedIndex(tmColIdx);
	}
    }

    /**
     * A widget that show whether the values are in ascending or descending
     * order.
     */
    class ColDir extends ColWgt {
	int[] xpntUp = { 0, -arrowSize / 2, arrowSize / 2 };
	int[] ypntUp = { 1, arrowSize, arrowSize };
	int[] xpntDn = { 0, -arrowSize / 2, arrowSize / 2 };
	int[] ypntDn = { arrowSize, 1, 1 };
	Polygon sUp = new Polygon(xpntUp, ypntUp, xpntUp.length);
	Polygon sDn = new Polygon(xpntDn, ypntDn, xpntDn.length);

	@Override
	protected boolean isFilled(int c) {
	    return isInverted(c);
	}

	@Override
	protected Shape getShape(int c) {
	    return isInverted(c) ? sUp : sDn;
	}
    }

    /**
     * Displays either the maximum or minimum value of each displayed column.
     */
    class ColVal extends JPanel {
	boolean isTop = false;

	ColVal(boolean isTop) {
	    super();
	    this.isTop = isTop;
	}

	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    int cPos[] = colPos;
	    int cIdx[] = colIdx;
	    if (cPos != null) {
		FontMetrics fm = g.getFontMetrics();
		Color bg = getBackground();
		Color fg = getForeground();
		Color mg = new Color((bg.getRed() + fg.getRed()) / 2, (bg
			.getGreen() + fg.getGreen()) / 2, (bg.getBlue() + fg
			.getBlue()) / 2);
		for (int c = 0; c < cPos.length; c++) {
		    g.setColor(Color.black);
		    ColumnMap colMap = ctx.getColumnMap(tm, cIdx[c]);
		    if (true && colMap != null) {
			Axis axis = getAxis(getColumnIndex(colMap));
			double val = isTop ? axis.getMin() : axis.getMax();
			Object valObj = colMap.getMappedValue(val, 0);
			String sval = valObj != null ? valObj.toString()
				: "NULL";
			int len = fm.stringWidth(sval);
			int lm = c == 0 ? 0 : cPos[c] - (cPos[c] - cPos[c - 1])
				/ 2 + 2;
			int rm = (c == cPos.length - 1 ? getWidth() : cPos[c]
				+ (cPos[c + 1] - cPos[c]) / 2) - 6;
			int pos = cPos[c] - len / 2;
			if (pos < lm) {
			    pos = lm;
			}
			Rectangle clip = g.getClipBounds();
			if (pos + len >= rm) {
			    g.setClip(lm, 0, rm - lm + 6, getHeight());
			    Color color = g.getColor();
			    g.setColor(mg);
			    g.drawString(sval, pos, (fm.getAscent() + 1));
			    g.setColor(color);
			}
			g.setClip(lm, 0, rm - lm, getHeight());
			g.drawString(sval, pos, (fm.getAscent() + 1));
			g.setClip(clip);
		    }
		}
	    }
	}

	@Override
	public Dimension getPreferredSize() {
	    try {
		FontMetrics fm = getGraphics().getFontMetrics();
		return new Dimension(fm.getHeight(), 24);
	    } catch (Exception ex) {
		System.err.println("ColVal.getPreferredSize " + ex);
	    }
	    return new Dimension(24, 24);
	}
    }

    /**
     * The Parallel Coordinate display that draws a polyline for each row of the
     * table.
     */
    class DispPC extends JPanel {
	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    int h = this.getHeight();
	    int cPos[] = colPos;
	    int cIdx[] = colIdx;
	    int[] row;
	    int dataPnts[][] = dPnts.getArray();
	    FontMetrics fm = g.getFontMetrics();
	    // draw vertical lines for each displayed column
	    if (cPos != null) {
		for (int c = 0; c < cPos.length; c++) {
		    if (isSelectedColumn(c)) {
			g.setColor(Color.black);
		    } else {
			g.setColor(Color.gray);
		    }
		    g.drawLine(cPos[c], 0, cPos[c], getHeight());
		}
	    }
	    // draw polylines
	    if (dataPnts != null && dataPnts.length > 0 && dataPnts[0] != null) {
		// Special case if there is only one displayed column.
		if (cPos.length == 1) {
		    // first pass: draw unselected lines
		    for (int r = 0; r < dataPnts.length; r++) {
			if (rsm != null && !rsm.isSelectedIndex(r))
			    continue;
			if (!lsm.isSelectedIndex(r)) {
			    try {
				g.setColor(getColor(r));
				g.drawLine(cPos[0], dataPnts[r][0],
					cPos[0] + 4, dataPnts[r][0]);
			    } catch (Exception ex) {
			    }
			}
		    }
		    // second pass: draw selected lines
		    if (!lsm.isSelectionEmpty()) {
			for (int r = lsm.getMinSelectionIndex(); r <= lsm
				.getMaxSelectionIndex(); r++) {
			    if (rsm != null && !rsm.isSelectedIndex(r))
				continue;
			    if (lsm.isSelectedIndex(r)) {
				try {
				    g.setColor(getColor(r));
				    g.drawLine(cPos[0], dataPnts[r][0],
					    cPos[0] + 4, dataPnts[r][0]);
				} catch (Exception ex) {
				}
			    }
			}
		    }
		} else {
		    int clen = cPos.length;
		    // first pass: draw unselected lines
		    for (int r = 0; r < dataPnts.length; r++) {
			if (rsm != null && !rsm.isSelectedIndex(r))
			    continue;
			row = dataPnts[r];
			if (!lsm.isSelectedIndex(r)) {
			    g.setColor(getColor(r));
			    g.drawPolyline(cPos, row,
				    row.length < clen ? row.length : clen);
			}
		    }
		    // second pass: draw selected lines
		    if (!lsm.isSelectionEmpty()) {
			int max = lsm.getMaxSelectionIndex();
			if (max >= dataPnts.length) {
			    max = dataPnts.length - 1;
			}
			for (int r = lsm.getMinSelectionIndex(); r <= max; r++) {
			    if (rsm != null && !rsm.isSelectedIndex(r))
				continue;
			    if (lsm.isSelectedIndex(r)) {
				row = dataPnts[r];
				g.setColor(getColor(r));
				g.drawPolyline(cPos, row,
					row.length < clen ? row.length : clen);
			    }
			}
		    }
		}
	    }
	    // draw the current selection range lines
	    if (selectPos != null && selectionColumn >= 0) {
		int x1 = selectionColumn == 0 ? 0 : cPos[selectionColumn]
			- (cPos[selectionColumn] - cPos[selectionColumn - 1])
			/ 2;
		int x2 = selectionColumn == cPos.length - 1 ? getWidth()
			: cPos[selectionColumn]
				+ (cPos[selectionColumn + 1] - cPos[selectionColumn])
				/ 2;
		g.setColor(Color.black);
		g.drawLine(x1, selectPos[0], x2, selectPos[0]);
		g.drawLine(x1, selectPos[1], x2, selectPos[1]);
	    }
	    // display the value string for the start selection point
	    if (valueFrom != null) {
		int x = pressLoc.x + hitWidth;
		int l = fm.stringWidth(valueFrom);
		if (x + l > getWidth()) {
		    x = getWidth() - l;
		}
		int y = pressLoc.y > fm.getAscent() ? pressLoc.y : fm
			.getAscent();
		g.setColor(Color.white);
		g.fillRect(x, y - fm.getMaxAscent(), l, fm.getHeight());
		g.setColor(Color.black);
		g.drawString(valueFrom, x, y);
	    }
	    // display the value string for the current point
	    if (valueAt != null) {
		int x = movedLoc.x + hitWidth;
		int l = fm.stringWidth(valueAt);
		if (x + l > getWidth()) {
		    x = getWidth() - l;
		}
		int y = movedLoc.y > fm.getAscent() ? movedLoc.y : fm
			.getAscent();
		g.setColor(Color.white);
		g.fillRect(x, y - fm.getMaxAscent(), l, fm.getHeight());
		g.setColor(Color.black);
		g.drawString(valueAt, x, y);
	    }
	}
    }

    /** Widget size in pixels */
    private int arrowSize = 6;
    /** A list model of table columns */
    DefaultListModel colLM = new DefaultListModel();
    /** A list of table columns */
    JList colList = new JList(colLM);
    /** Whether to change the display for changes to the colSelectList */
    boolean monitorColList = true;
    /** The columns to show when the table is mapped */
    int[] pendingCols = null;
    /**
     * A list of columns that need to be mapped to the dPnts array. Elements are
     * Integer.
     */
    private SortedSet columnsToMap = Collections
	    .synchronizedSortedSet(new TreeSet());
    /** Holds the Y coords for the polylines for each row of the table. */
    private PointArray dPnts = new PointArray();
    /** The X position of each displayed column (indexed by display column) */
    private int colPos[] = null;
    /**
     * The mapping of columns from the display position index to the table
     * column index.
     */
    private int colIdx[] = null;
    /**
     * The axis to use for mapping column values to the Y value in the display.
     * (indexed by table column)
     */
    Vector axisList = null;
    /** Partitions table columns for sharing common scales */
    PartitionIndexMap partitionMap = null;
    /** The column (position index) from which to map line colors. */
    private int selectedColumn = 0;
    DefaultListSelectionModel clsm = new DefaultListSelectionModel();
    /** The column (position index) being selected upon */
    private int selectionColumn = 0;
    /** The Set Operator for selection. */
    private int prevSetOp = -1;
    /** Point at which a mouse button was pressed. */
    private Point pressLoc = new Point();
    /** Point at which the mouse cursor currently points. */
    private Point movedLoc = new Point();
    /** An array containing the start and end y positions for a select. */
    private int selectPos[] = null;
    /** The Column Value at the current cursor position. */
    private String valueAt = null;
    /** The Column Value at the cursor position where a selection was started. */
    private String valueFrom = null;
    /** Format infinite values. */
    static DecimalFormat df = new DecimalFormat();
    /** A String representation of positive infinity. */
    static String posInfStr = df.format(Double.POSITIVE_INFINITY);
    /** A String representation of negative infinity. */
    static String negInfStr = df.format(Double.NEGATIVE_INFINITY);
    /** The proximity to the column in order to count it as being hit. */
    int hitWidth = 5;
    /** If not null, specifies which of the rows to display */
    ListSelectionModel rsm = null;

    /*
     * Panel layout:
     * 
     * +----+----+-----+-+------------------------------------+ C N N N | colLbl
     * column Names | C N N ttP +-+------------------------------------+ C N tP
     * N S | colTval max value labels | C N
     * +-----+-+------------------------------------+ C N S tbP N | colMov drag
     * widget | C +----+-----+-+------------------------------------+ C C | | C
     * mP C | dispPC parallel coordinates | C C | | C
     * +----+-----+-+------------------------------------+ C S N | colDir invert
     * widget | C S bP +-+------------------------------------+ C S S | colBval
     * min value labels |
     * +----+----+-----+-+------------------------------------+
     */

    ColLbl colLbl = new ColLbl(); // ColumnName
    ColVal colTval = new ColVal(true); // value
    ColMov colMov = new ColMov(); // <>
    DispPC dispPC = new DispPC(); // -data-
    ColDir colDir = new ColDir(); // ^
    ColVal colBval = new ColVal(false); // value

    JRadioButton allRows = new JRadioButton("All Rows");
    JRadioButton selRows = new JRadioButton("Selected Rows");
    JRadioButton curRows = new JRadioButton("Current Rows");

    JButton showBtn = new JButton("Show All");
    JButton hideBtn = new JButton("Hide");
    JButton groupBtn = new JButton("Group");
    JButton normBtn = new JButton("Normalize");
    JButton scaleBtn = new JButton("Scale");

    AxisDisplay axisDisplay = null;

    JPanel ttP = new JPanel(new BorderLayout());
    JPanel tbP = new JPanel(new BorderLayout());
    JPanel tP = new JPanel(new BorderLayout());
    JPanel bP = new JPanel(new BorderLayout());
    JPanel mP = new JPanel(new BorderLayout());

    // Thread to map data to this view
    class MapThread extends Thread {
	@Override
	public void run() {
	    while (columnsToMap != null && columnsToMap.size() > 0) {
		updateMapData();
		if (columnsToMap.size() > 0) {
		    try {
			Thread.sleep(500);
		    } catch (Exception ex) {
		    }
		}
	    }
	}
    };

    MapThread mapThread = null;

    /**
     * Update points whenever a Column finishing getting mapped.
     */
    CellMapListener cml = new CellMapListener() {
	public void cellMapChanged(CellMapEvent e) {
	    if (e.getMapState() == CellMap.INVALID) {
		mapColumns();
	    } else if (!e.mappingInProgress()) {
		ColumnMap colMap = (ColumnMap) e.getSource();
		initColumn(colMap);
	    }
	}
    };

    private void initColumn(ColumnMap colMap) {
	if (colMap != null) {
	    int c = getColumnIndex(colMap);
	    Axis axis = getAxis(c);
	    setAxis(colMap, axis, true);
	    int dispIdx = getDispIdxForTableColumn(c);
	    if (dispIdx >= 0) {
		mapData(dispIdx);
	    }
	}
    }

    /**
     * Update points whenever the size changes.
     */
    private ComponentAdapter ca = new ComponentAdapter() {
	@Override
	public void componentResized(ComponentEvent e) {
	    if (tm != null && colPos == null) {
		mapColumns();
	    }
	    positionColumns();
	    setAxisSize();
	    mapData(colPos);
	}

	@Override
	public void componentShown(ComponentEvent e) {
	    if (tm != null) {
		mapColumns();
	    }
	}
    };

    ListSelectionListener rsmListener = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (!e.getValueIsAdjusting()) {
		repaint();
	    }
	}
    };

    ListSelectionListener clsmListener = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (!e.getValueIsAdjusting()) {
		boolean enableBtn = !clsm.isSelectionEmpty();
		hideBtn.setEnabled(enableBtn);
		groupBtn.setEnabled(enableBtn);
		normBtn.setEnabled(enableBtn);
		scaleBtn.setEnabled(enableBtn);
	    }
	}
    };

    ListSelectionListener lsmListener = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (!e.getValueIsAdjusting()) {
		boolean enableBtn = !((ListSelectionModel) e.getSource())
			.isSelectionEmpty();
		curRows.setEnabled(enableBtn);
	    }
	}
    };

    /**
     * Drag columns on display.
     */
    MouseInputAdapter colMovListener = new MouseInputAdapter() {
	/** The column (position index) being selected upon */
	private int highlightColumn = -1;
	private boolean isDown = false;
	private boolean inComponent = false;

	@Override
	public void mousePressed(MouseEvent e) {
	    isDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    if (!inComponent) {
		highlightColumn = -1;
	    }
	    isDown = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	    Point p = e.getPoint();
	    int cPos[] = colPos;
	    if (cPos != null) {
		for (int c = 0; c < cPos.length; c++) {
		    if (cPos[c] >= p.x - hitWidth && cPos[c] <= p.x + hitWidth) {
			int tmColIdx = colIdx[c];
			if (e.getClickCount() > 1) {
			    clsm.setSelectionInterval(tmColIdx, tmColIdx);
			    setSelectedColumn(c);
			} else {
			    if (e.isShiftDown()) {
				if (clsm.isSelectionEmpty()) {
				    setSelectedColumn(c);
				    clsm.addSelectionInterval(tmColIdx,
					    tmColIdx);
				} else {
				    int sc = getSelectedColumn();
				    int d = c < sc ? 1 : -1;
				    clsm.setValueIsAdjusting(true);
				    for (int i = c; i != sc; i += d) {
					tmColIdx = colIdx[i];
					clsm.addSelectionInterval(tmColIdx,
						tmColIdx);
				    }
				    clsm.setValueIsAdjusting(false);
				}
			    } else if (e.isControlDown()) {
				if (clsm.isSelectedIndex(tmColIdx)) {
				    clsm.removeSelectionInterval(tmColIdx,
					    tmColIdx);
				} else {
				    if (clsm.isSelectionEmpty()) {
					setSelectedColumn(c);
				    }
				    clsm.addSelectionInterval(tmColIdx,
					    tmColIdx);
				}
			    } else {
				if (clsm.isSelectionEmpty()) {
				    setSelectedColumn(c);
				}
				clsm.addSelectionInterval(tmColIdx, tmColIdx);
			    }
			}
			repaint();
		    }
		}
	    }
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	    int cPos[] = colPos;
	    if (cPos != null) {
		if (highlightColumn >= 0 && highlightColumn < cPos.length) {
		    JComponent comp = (JComponent) e.getSource();
		    int w = comp.getWidth();
		    Point p = e.getPoint();
		    int cIdx[] = colIdx;
		    int x = p.x < 0 ? 0 : p.x >= w ? w - 1 : p.x;
		    if (x < cPos[highlightColumn]) {
			for (int c = highlightColumn - 1; c >= 0 && x < cPos[c]; c--) {
			    dataArraySwapCols(c, c + 1);
			    highlightColumn = c;
			}
			cPos[highlightColumn] = x;
			repaint();
		    } else if (x > cPos[highlightColumn]) {
			for (int c = highlightColumn + 1; c < cPos.length
				&& x > cPos[c]; c++) {
			    dataArraySwapCols(c - 1, c);
			    highlightColumn = c;
			}
			cPos[highlightColumn] = x;
			repaint();
		    }
		}
	    }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    Point p = e.getPoint();
	    int cPos[] = colPos;
	    if (cPos != null) {
		boolean onColumn = false;
		highlightColumn = -1;
		for (int c = 0; c < cPos.length; c++) {
		    if (cPos[c] >= p.x - hitWidth && cPos[c] <= p.x + hitWidth) {
			onColumn = true;
			highlightColumn = c;
			break;
		    }
		}
		setCursor(Cursor
			.getPredefinedCursor(onColumn ? Cursor.MOVE_CURSOR
				: Cursor.DEFAULT_CURSOR));
	    }
	    repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    if (!isDown) {
		highlightColumn = -1;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    }
	    inComponent = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	    inComponent = true;
	}
    };

    /**
     * Invert the direction the column is mapped.
     */
    MouseInputAdapter invertListener = new MouseInputAdapter() {
	/** The column (position index) being selected upon */
	private int highlightColumn = -1;
	boolean onColumn = false;

	@Override
	public void mouseClicked(MouseEvent e) {
	    if (onColumn) {
		invertColumn(highlightColumn);
	    }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    mouseMove(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	    mouseMove(e);
	}

	private void mouseMove(MouseEvent e) {
	    Point p = e.getPoint();
	    int cPos[] = colPos;
	    if (cPos != null) {
		onColumn = false;
		highlightColumn = -1;
		for (int c = 0; c < cPos.length; c++) {
		    if (cPos[c] >= p.x - hitWidth && cPos[c] <= p.x + hitWidth) {
			onColumn = true;
			highlightColumn = c;
			break;
		    }
		}
		setCursor(Cursor
			.getPredefinedCursor(onColumn ? Cursor.HAND_CURSOR
				: Cursor.DEFAULT_CURSOR));
	    }
	    repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
    };

    /**
     * Make selections on the parallel coords display.
     * 
     */
    MouseInputAdapter dataListener = new MouseInputAdapter() {
	private boolean selectRange = false;
	private boolean onColumn = false;

	private String getValueString(double dval, ColumnMap cmap) {
	    String sval = "";
	    if (Double.isNaN(dval)) {
		sval = "NULL";
	    } else if (Double.isInfinite(dval)) {
		if (dval == Double.NEGATIVE_INFINITY) {
		    sval = negInfStr;
		} else {
		    sval = posInfStr;
		}
	    } else {
		Object obj = cmap.getMappedValue(dval, 0);
		if (obj != null) {
		    sval = obj.toString();
		} else {
		    sval = "NULL";
		}
	    }
	    return sval;
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    valueAt = null;
	    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    Point p = e.getPoint();
	    int cPos[] = colPos;
	    int cIdx[] = colIdx;
	    if (selectionColumn >= 0) {
		if (ctx != null) {
		    prevSetOp = ctx.getSetOperator(tm).getSetOperator();
		    ctx.getSetOperator(tm).setFromInputEventMask(
			    e.getModifiers());
		    int c = selectionColumn;
		    double dval = getColumnValue(c, p.y);
		    valueFrom = getValueString(dval, ctx.getColumnMap(tm,
			    cIdx[c]));
		    pressLoc.y = p.y;
		    pressLoc.x = cPos[c];
		}
		selectRange = true;
		selectPos = new int[2];
		selectPos[0] = p.y;
		selectPos[1] = p.y;
	    }
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    try {
		JComponent comp = (JComponent) e.getSource();
		int w = comp.getWidth();
		int h = comp.getHeight();
		Point p = e.getPoint();
		valueFrom = null;
		if (selectRange) {
		    int cPos[] = colPos;
		    int cIdx[] = colIdx;
		    selectPos[1] = p.y < 0 ? 0 : p.y >= h ? h - 1 : p.y;
		    if (selectionColumn >= 0 && selectionColumn < cPos.length) {
			ColumnMap colMap = ctx.getColumnMap(tm,
				cIdx[selectionColumn]);
			String nullSelected = null;
			double from = getColumnValue(selectionColumn,
				selectPos[0]);
			double to = getColumnValue(selectionColumn,
				selectPos[1]);
			if (Double.isNaN(from) || Double.isNaN(to)) {
			    nullSelected = colMap.getName() + " = NULL "; // ?
			    // check
			    // SQL
			    // spec
			    // If either is non null, then we may need to swap
			    // to and from values
			    // in order for the ColumnMap to select the right
			    // range
			    // Find out if drag was in order of increasing or
			    // decreasing order
			    // Is NaN range next to PosInf or NegInf
			    int dy = selectPos[1] - selectPos[0];
			    double mid = getColumnValue(selectionColumn,
				    (selectPos[0] + selectPos[1]) / 2);
			    if (!Double.isNaN(from)) {
				if (Double.isInfinite(from)) {
				    if (Double.isInfinite(mid)
					    || Double.isNaN(mid)) {
					from = Double.NaN;
				    }
				} else if (mid < from) {
				    to = from;
				    from = Double.NaN;
				}
			    } else if (!Double.isNaN(to)) {
				if (Double.isInfinite(to)) {
				    if (Double.isInfinite(mid)
					    || Double.isNaN(mid)) {
					from = Double.NaN;
				    }
				} else if (mid > to) {
				    to = from;
				    from = Double.NaN;
				}
			    }

			}
			colMap.selectRange(from, to);
			if (!Double.isNaN(from) || !Double.isNaN(to)) {
			    double fv = from;
			    double tv = to;
			    if (nullSelected != null) {
				if (Double.isNaN(from))
				    fv = colMap.getMin();
				if (Double.isNaN(to))
				    tv = colMap.getMin();
			    } else {
				fv = Math.min(fv, tv);
				tv = Math.max(fv, tv);
				if (Double.isInfinite(fv))
				    fv = fv < 0. ? colMap.getMin() : fv;
				if (Double.isInfinite(tv))
				    tv = tv > 0. ? colMap.getMax() : tv;
			    }
			    // Haven't converted Date to SQL format yet.
			    Object fobj = colMap.getMappedValue(fv, -1);
			    Object tobj = colMap.getMappedValue(tv, 1);
			}
		    }
		    selectRange = false;
		    selectPos = null;
		    if (ctx != null) {
			ctx.getSetOperator(tm).setSetOperator(prevSetOp);
		    }
		}
	    } catch (Throwable t) {
		t.printStackTrace();
	    }
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	    JComponent comp = (JComponent) e.getSource();
	    int w = comp.getWidth();
	    int h = comp.getHeight();
	    Point p = e.getPoint();
	    int cPos[] = colPos;
	    int cIdx[] = colIdx;
	    if (selectionColumn >= 0 && selectionColumn < cPos.length) {
		int y = p.y < 0 ? 0 : p.y >= h ? h - 1 : p.y;
		if (selectRange) {
		    selectPos[1] = y;
		    if (ctx != null) {
			int c = selectionColumn;
			ColumnMap cmap = ctx.getColumnMap(tm, cIdx[c]);
			double dval = getColumnValue(selectionColumn, y);
			valueAt = getValueString(dval, cmap);
			movedLoc.y = y;
			movedLoc.x = cPos[c];
		    }
		    repaint();
		}
	    }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    JComponent comp = (JComponent) e.getSource();
	    int w = comp.getWidth();
	    int h = comp.getHeight();
	    int dataPnts[][] = dPnts.getArray();
	    int cPos[] = colPos;
	    int cIdx[] = colIdx;
	    Point p = e.getPoint();
	    int x = p.x < 0 ? 0 : p.x >= w ? w - 1 : p.x;
	    int y = p.y < 0 ? 0 : p.y >= h ? h - 1 : p.y;
	    valueAt = null;
	    if (cPos != null) {
		selectionColumn = -1;
		for (int c = 0; c < cPos.length; c++) {
		    if (cPos[c] >= p.x - hitWidth && cPos[c] <= p.x + hitWidth) {
			ColumnMap cmap = ctx.getColumnMap(tm, cIdx[c]);
			setCursor(Cursor
				.getPredefinedCursor(cmap.getState() == CellMap.MAPPED ? Cursor.CROSSHAIR_CURSOR
					: Cursor.WAIT_CURSOR));
			movedLoc = p;
			movedLoc.x = cPos[c] + hitWidth;
			selectionColumn = c;
			if (ctx.getSetOperator(tm).getSetOperator() == SetOperator.BRUSHOVER) {
			    int closestRow = 0;
			    int diff = h;
			    if (dataPnts != null) {
				valueAt = null;
				int hits = 0;
				for (int r = 0; r < dataPnts.length; r++) {
				    if (dataPnts[r][c] == p.y) {
					diff = 0;
					closestRow = r;
					if (ctx.getSetOperator(tm)
						.getSetOperator() == SetOperator.BRUSHOVER) {
					    if (hits++ == 0) {
						lsm.setSelectionInterval(r, r);
					    } else {
						lsm.addSelectionInterval(r, r);
					    }
					}
				    } else if (diff > 0) {
					int d = p.y > dataPnts[r][c] ? p.y
						- dataPnts[r][c]
						: dataPnts[r][c] - p.y;
					if (d < diff) {
					    closestRow = r;
					    diff = d;
					}
				    }
				}

				if (cmap.isNumber() || cmap.isDate()) {
				    double dval = getColumnValue(c, p.y);
				    valueAt = getValueString(dval, cmap);
				    movedLoc.y = y;
				} else {
				    Object obj = tm.getValueAt(closestRow,
					    cIdx[c]);
				    if (obj != null) {
					valueAt = tm.getValueAt(closestRow,
						cIdx[c]).toString();
					movedLoc.y = dataPnts[closestRow][c];
				    } else {
					valueAt = "NULL";
				    }
				}

			    }
			} else {
			    movedLoc.y = y;
			    double dval = getColumnValue(c, p.y);
			    valueAt = getValueString(dval, cmap);
			    repaint();
			    break;
			}
		    }
		}
		if (selectionColumn < 0) {
		    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		    if (valueAt != null) {
			valueAt = null;
			repaint();
		    }
		}
	    }
	}
    };

    /**
     * Attach all the Listeners.
     */
    private void initListeners() {
	mP.addComponentListener(ca);
	dispPC.addMouseMotionListener(dataListener);
	dispPC.addMouseListener(dataListener);
	colMov.addMouseMotionListener(colMovListener);
	colMov.addMouseListener(colMovListener);
	colDir.addMouseMotionListener(invertListener);
	colDir.addMouseListener(invertListener);
	clsm.addListSelectionListener(clsmListener);
	ListSelectionModel selModel = getSelectionModel();
	if (selModel != null) {
	    selModel.addListSelectionListener(lsmListener);
	}
    }

    /**
     * Initialize the display.
     */
    private void init() {
	colDir.setToolTipText("invert column");
	colMov.setToolTipText("select or drag column");
	setOpaque(true);
	dispPC.setBackground(Color.white);
	ttP.add(colLbl, BorderLayout.NORTH);
	ttP.add(colTval, BorderLayout.SOUTH);
	tbP.add(colMov, BorderLayout.NORTH);
	tP.add(ttP, BorderLayout.NORTH);
	tP.add(tbP, BorderLayout.SOUTH);
	bP.add(colDir, BorderLayout.NORTH);
	bP.add(colBval, BorderLayout.SOUTH);
	mP.add(tP, BorderLayout.NORTH);
	mP.add(bP, BorderLayout.SOUTH);
	mP.add(dispPC, BorderLayout.CENTER);
	// colList
	colList.setToolTipText("Select columns to compare");
	colList
		.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	colList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    int[] indices = colList.getSelectedIndices();
		    if (monitorColList) {
			showColumns(indices);
		    }
		    showBtn.setEnabled(indices.length < colList.getModel()
			    .getSize());
		}
	    }
	});
	// toolbar
	JToolBar tb = new JToolBar();

	showBtn.setEnabled(false);
	showBtn.setToolTipText("Display All columns");
	showBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		showAllColumns();
	    }
	});
	tb.add(showBtn);

	hideBtn.setEnabled(false);
	hideBtn.setToolTipText("Hide the selected columns");
	hideBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hideSelectedColumns();
	    }
	});
	tb.add(hideBtn);

	groupBtn.setEnabled(false);
	groupBtn.setToolTipText("Set selected columns to a common range scale");
	groupBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		groupSelectedColumns();
	    }
	});
	tb.add(groupBtn);
	normBtn.setEnabled(false);
	normBtn.setToolTipText("Normalize the scale of each selected column");
	normBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		ungroupSelectedColumns();
	    }
	});
	tb.add(normBtn);

	scaleBtn.setEnabled(false);
	scaleBtn.setToolTipText("");
	scaleBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int c = selectedColumn;
		Axis axis = getAxis(c);
		axisDisplay = new AxisDisplay(axis, AxisComponent.RIGHT);
		axisDisplay.setZoomable(true);
		// TODO
		// Display this along axis and respond to changes
	    }
	});
	// tb.add(scaleBtn);

	tb.addSeparator();

	ButtonGroup rowGrp = new ButtonGroup();

	rowGrp.add(allRows);
	allRows.setSelected(true);
	allRows.setToolTipText("Display all rows");
	allRows.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setRowDisplaySelection((ListSelectionModel) null);
	    }
	});
	tb.add(allRows);

	rowGrp.add(selRows);
	selRows.setToolTipText("Display rows as they are selected");
	selRows.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setRowDisplaySelection(getSelectionModel());
	    }
	});
	tb.add(selRows);

	rowGrp.add(curRows);
	curRows.setEnabled(false);
	curRows.setToolTipText("Display the currently selected rows");
	curRows.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		ListSelectionModel sm = getSelectionModel();
		if (sm instanceof DefaultListSelectionModel) {
		    try {
			setRowDisplaySelection((ListSelectionModel) ((DefaultListSelectionModel) getSelectionModel())
				.clone());
		    } catch (CloneNotSupportedException ex) {
		    }
		} else {
		    DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
		    int min = sm.getMinSelectionIndex();
		    int max = sm.getMaxSelectionIndex();
		    if (min >= 0) {
			for (int i = min; i <= max; i++) {
			    if (sm.isSelectedIndex(i)) {
				dlsm.addSelectionInterval(1, 1);
			    }
			}
			setRowDisplaySelection(dlsm);
		    }
		}
	    }
	});
	tb.add(curRows);

	JPanel pnl = new JPanel(new BorderLayout());
	// pnl.add(tb,BorderLayout.NORTH);
	pnl.add(mP, BorderLayout.CENTER);

	JScrollPane jsp = new JScrollPane(colList,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		true, jsp, pnl);
	splitPane.setOneTouchExpandable(true);
	splitPane.setDividerLocation(0);

	setLayout(new BorderLayout());
	add(tb, BorderLayout.NORTH);
	add(splitPane);
	initListeners();
	mapColumns();
    }

    /**
     * Constructs a Parallel Coordinate display. Nothing will be displayed until
     * a data model is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ParallelCoordinatePanel() {
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
    public ParallelCoordinatePanel(TableModel tableModel) {
	this();
	setTableModel(tableModel);
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
    public ParallelCoordinatePanel(TableModel tableModel, ListSelectionModel lsm) {
	this();
	setTableModel(tableModel);
	setSelectionModel(lsm);
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
	mapColumns();
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
	ListSelectionModel selModel = getSelectionModel();
	if (selModel != null) {
	    selModel.removeListSelectionListener(lsmListener);
	}
	super.setSelectionModel(newModel);
	selModel = getSelectionModel();
	if (selModel != null) {
	    selModel.addListSelectionListener(lsmListener);
	}
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return mP;
    }

    /**
     * The TableModelEvent should be constructed in the coordinate system of the
     * model, the appropriate mapping to the view coordinate system is performed
     * by the ParallelCoordinatePanel when it receives the event.
     * 
     * @param e
     *            the change to the data model
     */
    @Override
    public void tableChanged(TableModelEvent e) {
	if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
	    // The whole thing changed
	    // dPnts.create(tm.getRowCount(),tm.getColumnCount());
	    // lsm.clearSelection();
	    mapColumns();
	    repaint();
	    return;
	}
	int rs = e.getFirstRow();
	int re = e.getType() != TableModelEvent.UPDATE
		|| e.getLastRow() < ((TableModel) e.getSource()).getRowCount() ? e
		.getLastRow()
		: ((TableModel) e.getSource()).getRowCount() - 1;

	if (e.getType() == TableModelEvent.INSERT) {
	    // Synchronization of events make it difficult to rely on inserts
	    // dPnts.addRows(e.getFirstRow(), e.getLastRow());
	    // mapRowValues(dPnts.getArray(),e.getFirstRow(), e.getLastRow());
	    int nrows = tm.getRowCount();
	    if (dPnts.getArray().length < nrows) {
		dPnts.addRows(dPnts.getArray().length, nrows - 1);
	    }
	    mapData(colPos);
	    updateAxes();
	    repaint();
	    return;
	} else if (e.getType() == TableModelEvent.DELETE) {
	    dPnts.delRows(rs, re);
	    updateAxes();
	    repaint();
	    return;
	} else if (e.getType() == TableModelEvent.UPDATE) {
	    int nrows = tm.getRowCount();
	    if (dPnts.getArray().length < nrows) {
		dPnts.addRows(dPnts.getArray().length, nrows - 1);
		mapData(colPos);
	    } else {
		mapRowValues(dPnts.getArray(), rs, re);
	    }
	    updateAxes();
	    repaint();
	    return;
	}
	lsm.clearSelection();
	mapData(colPos);
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
     * Swap two columns in the dataPnts array.
     */
    private synchronized void dataArraySwapCols(int c1, int c2) {
	int cPos[] = colPos;
	int cIdx[] = colIdx;
	int tmp = 0;
	tmp = cIdx[c1];
	cIdx[c1] = cIdx[c2];
	cIdx[c2] = tmp;
	tmp = cPos[c1];
	cPos[c1] = cPos[c2];
	cPos[c2] = tmp;
	dPnts.swapCols(c1, c2);
    }

    /**
     * Return the display color for the given row.
     * 
     * @param r
     *            the row in the table
     * @return the color for the given table row
     */
    private Color getColor(int r) {
	float s;
	ColumnMap colMap = ctx.getColumnMap(tm, selectedColumn);
	if (colMap != null) {
	    Axis axis = getAxis(selectedColumn);
	    s = 1f - (float) axis.getRelPosition(colMap.getMapValue(r));
	} else {
	    return Color.gray;
	}
	if (rowSelected(r)) {
	    // blue - cyan
	    return Color.getHSBColor(.65f - s * .15f, 1f, 1f);
	}
	// black - red
	return Color.getHSBColor(1f, s, s);
    }

    /**
     * Map the data in the table to the parallel coordinate display.
     */
    private void mapColumns() {
	if (tm == null) {
	    return;
	}
	int ncols = tm.getColumnCount();
	if (ncols <= getSelectedColumn()) {
	    setSelectedColumn(0);
	}
	colLM.clear();
	int cPos[] = new int[ncols];
	int cIdx[] = new int[ncols];
	for (int c = 0; c < ncols; c++) {
	    colLM.addElement(tm.getColumnName(c));
	    ColumnMap colMap = ctx.getColumnMap(tm, c);
	    colMap.addCellMapListener(cml);
	    initColumn(colMap);
	    cIdx[c] = c;
	}
	colPos = cPos;
	colIdx = cIdx;
	int nrows = tm.getRowCount();
	dPnts.createArray(nrows, ncols);
	setAxisSize();
	positionColumns();
	int[] cols;
	if (pendingCols != null) {
	    monitorColList = false;
	    setColumns(pendingCols);
	    showColumns(pendingCols);
	    monitorColList = true;
	} else {
	    colList.setSelectionInterval(0, ncols - 1);
	}
    }

    private int[] getSelectIndices(ListSelectionModel aLsm) {
	int[] grp;
	if (!aLsm.isSelectionEmpty()) {
	    int d = aLsm.getMaxSelectionIndex() - aLsm.getMinSelectionIndex()
		    + 1;
	    int n = 0;
	    int[] tmp = new int[d];
	    for (int i = aLsm.getMinSelectionIndex(); i <= aLsm
		    .getMaxSelectionIndex(); i++) {
		if (aLsm.isSelectedIndex(i)) {
		    tmp[n++] = i;
		}
	    }
	    if (n == d) {
		grp = tmp;
	    } else {
		grp = new int[n];
		System.arraycopy(tmp, 0, grp, 0, n);
	    }
	} else {
	    grp = new int[0];
	}
	return grp;
    }

    private void showAllColumns() {
	colList.setSelectionInterval(0, colList.getModel().getSize() - 1);
    }

    private void hideSelectedColumns() {
	int[] grp = getSelectIndices(clsm);
	if (grp.length > 0) {
	    monitorColList = false;
	    colList.setValueIsAdjusting(true);
	    clsm.setValueIsAdjusting(true);
	    for (int i = 0; i < grp.length; i++) {
		clsm.removeSelectionInterval(grp[i], grp[i]);
		colList.removeSelectionInterval(grp[i], grp[i]);
	    }
	    clsm.setValueIsAdjusting(false);
	    colList.setValueIsAdjusting(false);
	    showColumns(colList.getSelectedIndices());
	    monitorColList = true;
	}
    }

    private void groupSelectedColumns() {
	int[] grp = getSelectIndices(clsm);
	if (grp.length > 0) {
	    groupColumns(grp);
	}
    }

    private void ungroupSelectedColumns() {
	int[] grp = getSelectIndices(clsm);
	if (grp.length > 0) {
	    int[] col = new int[1];
	    for (int i = 0; i < grp.length; i++) {
		col[0] = grp[i];
		groupColumns(col);
	    }
	}
    }

    private void setSelectedColumn(int colPos) {
	selectedColumn = colIdx != null && colPos >= 0
		&& colPos < colIdx.length ? colIdx[colPos] : 0;
    }

    /**
     * Returns the Column display index of the selectedColumn return the Column
     * display index of the selectedColumn.
     */
    private int getSelectedColumn() {
	int dispIdx = getDispIdxForTableColumn(selectedColumn);
	return dispIdx > 0 ? dispIdx : 0;
    }

    /**
     * Returns the Column display index of the given table column. return the
     * Column display index of the given table column.
     */
    private int getDispIdxForTableColumn(int tmColIdx) {
	int[] cIdx = colIdx;
	if (cIdx != null) {
	    for (int i = 0; i < cIdx.length; i++) {
		if (cIdx[i] == tmColIdx) {
		    return i;
		}
	    }
	}
	return -1;
    }

    private boolean isSelectedColumn(int colPos) {
	return getSelectedColumn() == colPos;
    }

    /**
     * Set the view to display the columns at the TableModel columns indices
     * (numbered from 0 to number of columns - 1).
     * 
     * @param columns
     *            the indices of the TableModel columns to display.
     */
    @Override
    public void setColumns(int[] columns) {
	pendingCols = columns;
	if (columns != null) {
	    if (tm != null && tm.getColumnCount() > 0) {
		monitorColList = false;
		colList.setSelectedIndices(columns);
		showColumns(columns);
		monitorColList = true;
	    }
	} else {
	    colList.clearSelection();
	}
    }

    /**
     * The columns should share a common range of values.
     * 
     * @param columns
     *            the indices of the columns that should be plotted in the same
     *            range.
     */
    public void groupColumns(int[] columns) {
	if (columns != null) {
	    setGroup(columns);
	    for (int i = 0; i < columns.length; i++) {
		int c = columns[i];
		ColumnMap colMap = ctx.getColumnMap(tm, c);
		Axis axis = getAxis(c);
		setAxis(colMap, axis, axis.getMin() > axis.getMax());
	    }
	    mapData(colPos);
	}
    }

    /**
     * Sets the table rows that will be displayed. If selectedRows is null, all
     * rows are displayed.
     * 
     * @param selectedRows
     *            if not null, only row indices in the array will be displayed.
     */
    public void setRowDisplaySelection(int[] selectedRows) {
	DefaultListSelectionModel dsm = null;
	if (selectedRows != null) {
	    dsm = new DefaultListSelectionModel();
	    for (int i = 0; i < selectedRows.length; i++) {
		dsm.addSelectionInterval(selectedRows[i], selectedRows[i]);
	    }
	}
	setRowDisplaySelection(dsm);
    }

    /**
     * Selects table rows that will be displayed. If the selection model is
     * null, all rows are displayed.
     * 
     * @param rowDisplaySelection
     *            if not null, only row indices that are selected will be
     *            displayed.
     */
    public void setRowDisplaySelection(ListSelectionModel rowDisplaySelection) {
	if (rsm != null) {
	    rsm.removeListSelectionListener(rsmListener);
	}
	this.rsm = rowDisplaySelection;
	if (rsm != null) {
	    rsm.addListSelectionListener(rsmListener);
	    if (rowDisplaySelection == getSelectionModel()) {
		selRows.setSelected(true);
	    } else {
		curRows.setSelected(true);
	    }
	} else {
	    allRows.setSelected(true);
	}
	repaint();
    }

    /**
     * Returns the selection model that determines which rows will be displayed.
     * If the selection model is null, all rows are displayed.
     * 
     * @return the selection model that determines which rows will be displayed.
     */
    public ListSelectionModel getRowDisplaySelection() {
	return rsm;
    }

    private void showColumns(int[] columns) {
	int ncols = columns.length <= tm.getColumnCount() ? columns.length : tm
		.getColumnCount();
	int cPos[] = new int[ncols];
	int cIdx[] = new int[ncols];
	for (int c = 0; c < ncols; c++) {
	    int i = columns[c];
	    ColumnMap colMap = ctx.getColumnMap(tm, c);
	    colMap.addCellMapListener(cml);
	    cIdx[c] = i;
	}
	colPos = cPos;
	colIdx = cIdx;
	int nrows = tm.getRowCount();
	dPnts.createArray(nrows, ncols);
	setAxisSize();
	positionColumns();
	mapData(colPos);
    }

    /**
     * Set the default X positions for each displayed column.
     */
    private void positionColumns() {
	int[] cPos = colPos;
	if (cPos != null) {
	    int ncols = cPos.length;
	    double colw = (double) mP.getWidth() / ncols; // space between
	    // column axes
	    for (int c = 0; c < ncols; c++) {
		cPos[c] = (int) (colw * c + colw * .5);
	    }
	}
    }

    /**
     * Set the size of each axis to the display height.
     */
    private void setAxisSize() {
	int h = dispPC.getHeight() - 1;
	for (int c = 0; tm != null && c < tm.getColumnCount(); c++) {
	    getAxis(c).setSize(h);
	}
    }

    /**
     * Get an Axis for the column.
     * 
     * @param c
     *            The table model index of the column.
     * @return The axis that maps column values to the display y coordinate.
     */
    private Axis getDefaultAxis(int c) {
	InfLinearAxis axis = new InfLinearAxis();
	ColumnMap colMap = ctx.getColumnMap(tm, c);
	if (colMap != null) {
	    setAxis(colMap, axis, true);
	}
	return axis;
    }

    private Axis getAxis(int columnIndex) {
	if (axisList == null) {
	    axisList = new Vector();
	}
	if (columnIndex >= axisList.size()) {
	    axisList.setSize(columnIndex + 1);
	}
	Axis axis = (Axis) axisList.get(columnIndex);
	if (axis == null) {
	    axis = getDefaultAxis(columnIndex);
	    axisList.set(columnIndex, axis);
	}
	return axis;
    }

    private PartitionIndexMap getPartitionMap() {
	if (partitionMap == null) {
	    int[] cpi = new int[tm.getColumnCount()];
	    for (int i = 0; i < cpi.length; i++) {
		cpi[i] = i;
	    }
	    partitionMap = new PartitionIndexMap(cpi);
	} else if (partitionMap.getSrcSize() < tm.getColumnCount()) {
	    int[] cpi = partitionMap.getIndex();
	    int maxIdx = 0;
	    for (int i = 0; i < cpi.length; i++) {
		maxIdx = Math.max(maxIdx, cpi[i]);
	    }
	    int[] ncpi = new int[tm.getColumnCount()];
	    System.arraycopy(cpi, 0, ncpi, 0, cpi.length);
	    for (int i = cpi.length; i < ncpi.length; i++) {
		ncpi[i] = ++maxIdx;
	    }
	    partitionMap.setIndex(ncpi);
	}
	return partitionMap;
    }

    private void setGroup(int[] tableColumnIndices) {
	if (tableColumnIndices != null) {
	    if (tableColumnIndices.length == 0) {
	    } else {
		PartitionIndexMap pim = getPartitionMap();
		int[] cpi = pim.getIndex();
		int maxIdx = 0;
		for (int i = 0; i < cpi.length; i++) {
		    maxIdx = Math.max(maxIdx, cpi[i]);
		}
		maxIdx++;
		for (int i = 0; i < tableColumnIndices.length; i++) {
		    cpi[tableColumnIndices[i]] = maxIdx;
		}
		pim.setIndex(cpi);
	    }
	} else {
	    partitionMap = null;
	}
    }

    private int[] getGroup(int tableColumnIndex) {
	PartitionIndexMap pim = getPartitionMap();
	return pim.getSrcs(pim.getDst(tableColumnIndex));
    }

    private void updateGroups() {
	PartitionIndexMap pim = getPartitionMap();
	int n = pim.getDstSize();
	for (int i = 0; i < n; i++) {
	}
    }

    /**
     * Set the axis range to that of the column range.
     * 
     * @param cmap
     *            The table column map.
     * @param axis
     *            The axis for table to display mapping.
     * @param invert
     *            Whether to invert the column in the display.
     */
    private void setAxis(ColumnMap cmap, Axis axis, boolean invert) {
	if (cmap.getState() == CellMap.MAPPED) {
	    double min = cmap.getMin();
	    double max = cmap.getMax();
	    int[] grp = getGroup(getColumnIndex(cmap));
	    if (grp != null) {
		for (int i = 0; i < grp.length; i++) {
		    min = Math.min(min, ctx.getColumnMap(tm, grp[i]).getMin());
		    max = Math.max(max, ctx.getColumnMap(tm, grp[i]).getMax());
		}
	    }
	    axis.setMin(invert ? max : min);
	    axis.setMax(invert ? min : max);
	}
    }

    /**
     * Update the min and max values for all axes.
     */
    private void updateAxes() {
	for (int i = 0; i < tm.getColumnCount(); i++) {
	    ColumnMap colMap = getTableContext().getColumnMap(tm, i);
	    int c = getColumnIndex(colMap);
	    Axis axis = getAxis(c);
	    setAxis(colMap, axis, axis.getMin() > axis.getMax());
	}
    }

    /**
     * Invert the display of a column
     * 
     * @param c
     *            The display position index for the column.
     */
    private void invertColumn(int c) {
	int tmColIdx = colIdx[c];
	Axis axis = getAxis(tmColIdx);
	double min = axis.getMin();
	double max = axis.getMax();
	axis.setMax(min);
	axis.setMin(max);
	int dataPnts[][] = dPnts.getArray();
	mapColumnValues(dataPnts, c);
	repaint();
    }

    /**
     * Is the column displayed in inverted order.
     * 
     * @param c
     *            The display position index for the column.
     * @return True if the column displayed in inverted order.
     */
    private boolean isInverted(int c) {
	try {
	    int tmColIdx = colIdx[c];
	    Axis axis = getAxis(tmColIdx);
	    return axis.getMin() > axis.getMax();
	} catch (Exception ex) {
	}
	return false;
    }

    /**
     * Map the values in the column to the display.
     * 
     * @param c
     *            The display position index for the column.
     */
    private synchronized void mapData(int cPos) {
	columnsToMap.add(new Integer(cPos));
	mapData();
    }

    /**
     * Map the values in each of the columns to the display.
     * 
     * @param c
     *            An array of display position indices for columns.
     */
    private synchronized void mapData(int[] cPos) {
	if (cPos != null) {
	    for (int c = 0; c < cPos.length; c++) {
		columnsToMap.add(new Integer(c));
	    }
	    mapData();
	}
    }

    /**
     * Starts the thread to map the data values for each row in the table to
     * polyline values for each column in the columnsToMap list.
     */
    private synchronized void mapData() {
	if (mapThread == null || !mapThread.isAlive()) {
	    mapThread = new MapThread();
	    mapThread
		    .setPriority((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 3);
	    mapThread.start();
	}
    }

    /**
     * Map the data values for each row in the table to polyline values for each
     * column in the columnsToMap list. The mapping depends on the size of the
     * display so this method should be called whenever the view size changes.
     */
    private void updateMapData() {
	try {
	    if (tm != null && tm.getColumnCount() > 0) {
		while (columnsToMap.size() > 0) {
		    Integer i = (Integer) columnsToMap.first();
		    columnsToMap.remove(i);
		    int c = i.intValue();
		    mapColumnValues(dPnts.getArray(), c);
		}
		repaint();
	    }
	} catch (Exception ex) {
	    System.err.println("updateMapData " + ex);
	    ex.printStackTrace();
	}
    }

    /**
     * Return the value from the axis for the given screen location.
     * 
     * @param col
     *            The display position index for the column.
     * @param loc
     *            The y coordinate of the display.
     * @return The axis value for that loc.
     */
    private double getColumnValue(int col, int loc) {
	try {
	    int tmColIdx = colIdx[col];
	    Axis axis = getAxis(tmColIdx);
	    return axis.getValue(loc);
	} catch (Exception ex) {
	}
	return Double.NaN;
    }

    /**
     * Map the column values into the dataPnts array.
     * 
     * @param dataPnts
     *            The array of polyline y points.
     * @param c
     *            The display position index for the column.
     */
    public void mapColumnValues(int[][] dataPnts, int c) {
	int tmColIdx = -1;
	try {
	    int nrows = tm.getRowCount();
	    if (nrows < 1 || colIdx == null || c >= colIdx.length) {
		return;
	    }
	    tmColIdx = colIdx[c];
	    if (tmColIdx >= tm.getColumnCount()) {
		return;
	    }
	    Axis axis = getAxis(tmColIdx);
	    ColumnMap colMap = ctx.getColumnMap(tm, tmColIdx);
	    if (colMap != null) {
		if (colMap.getState() != CellMap.MAPPED) {
		    colMap.addCellMapListener(cml);
		} else {
		    for (int r = 0; r < nrows && r < dataPnts.length; r++) {
			Thread.currentThread();
			if (Thread.interrupted())
			    return;
			dataPnts[r][c] = axis.getIntPosition(colMap
				.getMapValue(r));
		    }
		}
	    }
	} catch (Throwable ex) {
	    System.err.println("mapColumnValues for column " + c + "\t"
		    + tmColIdx + "\t" + ex);
	    System.err.println("mapColumnValues table rows " + tm.getRowCount()
		    + "\t data rows " + dataPnts.length);
	    ex.printStackTrace();
	}
    }

    /**
     * Map the column values into the dataPnts array for the given rows.
     * 
     * @param dataPnts
     *            The array of polyline y points.
     * @param fromRow
     *            The first row index to map
     * @param toRow
     *            The last row index to map
     */
    public void mapRowValues(int[][] dataPnts, int fromRow, int toRow) {
	if (dataPnts == null || colIdx == null) {
	    return;
	}
	int fR = fromRow < toRow ? fromRow : toRow;
	int tR = fromRow < toRow ? toRow : fromRow;
	for (int r = fR; r < tR && r < dataPnts.length; r++) {
	    int[] row = dataPnts[r];
	    for (int c = 0; c < row.length && c < colIdx.length; c++) {
		int tmColIdx = colIdx[c];
		Axis axis = getAxis(tmColIdx);
		ColumnMap colMap = ctx.getColumnMap(tm, tmColIdx);
		if (axis != null && colMap != null
			&& colMap.getState() == CellMap.MAPPED) {
		    row[c] = axis.getIntPosition(colMap.getMapValue(r));
		}
	    }
	}
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
	ParallelCoordinatePanel pcp = null;
	try {
	    pcp = new ParallelCoordinatePanel();
	    pcp.setTableContext(getTableContext());
	    pcp.setTableModel(getTableModel());
	    pcp.setSelectionModel(getSelectionModel());
	    pcp.setColumns(colIdx);
	    pcp.colPos = colPos.clone();
	    pcp.partitionMap = partitionMap != null ? (PartitionIndexMap) partitionMap
		    .clone()
		    : null;
	    pcp.axisList.clear();
	    for (int i = 0; i < axisList.size(); i++) {
		pcp.axisList.add(((Axis) axisList.get(i)).clone());
	    }
	    pcp.selectedColumn = selectedColumn;
	    pcp.selectionColumn = selectionColumn;
	    pcp.rsm = rsm != null && rsm instanceof DefaultListSelectionModel ? (DefaultListSelectionModel) ((DefaultListSelectionModel) rsm)
		    .clone()
		    : null;
	} catch (CloneNotSupportedException nsex) {
	    throw nsex;
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return pcp;
    }

    /**
     * Get an array of axis clones, one for each column in the TableModel.
     */
    private Axis[] getAxes() {
	TableModel tm = getTableModel();
	int ncol = tm.getColumnCount();
	Axis[] axes = new Axis[ncol];
	for (int c = 0; c < ncol; c++) {
	    axes[c] = (Axis) getAxis(c).clone();
	}
	return axes;
    }

    /**
     * Return a JTable that displays all the distinct values of the given table
     * column in the first column, and a ParallelCoordinates graph of the table
     * rows that match the respectitive value in the second column.
     * 
     * @param tc
     *            The index of the TableModel column on which this table will be
     *            built.
     * @return A JTable displaying graphs for each distinct value of the column
     *         in the table.
     */
    public JTable getTableDisplay(int tc) {
	TableModel tm = getTableModel();
	ColumnMap colMap = ctx.getColumnMap(tm, tc);
	ColumnPartitionTableModel cptm = new ColumnPartitionTableModel(colMap);
	JTable jt = new JTable(cptm);
	ParallelCoordDisp pcd = new ParallelCoordDisp(getTableContext(), tm,
		colIdx, getAxes());
	jt.getColumnModel().getColumn(1).setCellRenderer(pcd);
	jt.setRowHeight(64);
	return jt;
    }

    /**
     * Return an icon displaying the Parallel Coordinate graph.
     * 
     * @param width
     *            the width for the icon
     * @param height
     *            the height for the icon
     * @return An Icon displaying the Parallel Coordinate graph.
     */
    public Icon getIcon(int width, int height) {
	TableModel tm = getTableModel();
	ParallelCoordDisp pcd = new ParallelCoordDisp(getTableContext(), tm,
		colIdx, getAxes(), getRowDisplaySelection());
	pcd.setSize(width, height);
	pcd.setPreferredSize(pcd.getSize());
	return pcd;
    }

    /**
     * This can display a ParallelCoordinate graph as either an Icon or as a
     * TableCellRenderer.
     */
    class ParallelCoordDisp extends JComponent implements TableCellRenderer,
	    Icon {
	TableContext cxt = null;
	TableModel tm = null;
	int[] columns = null;
	Axis[] axis = null;
	ListSelectionModel rsm = null;
	int[] rows = null;

	ParallelCoordDisp(TableContext cxt, TableModel tm, int columns[],
		Axis axis[]) {
	    this.cxt = cxt;
	    this.tm = tm;
	    this.columns = columns;
	    this.axis = axis;
	    setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2,
		    Color.lightGray));
	}

	ParallelCoordDisp(TableContext cxt, TableModel tm, int columns[],
		Axis axis[], ListSelectionModel rsm) {
	    this(cxt, tm, columns, axis);
	    this.rsm = rsm;
	}

	public int getIconWidth() {
	    return getWidth();
	}

	public int getIconHeight() {
	    return getHeight();
	}

	public void paintIcon(Component c, Graphics g, int ix, int iy) {
	    g.translate(ix, iy);
	    paintComponent(g);
	    g.translate(-ix, -iy);
	}

	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {
	    rows = value != null ? (int[]) value : null;
	    return this;
	}

	@Override
	public void paintComponent(Graphics g) {
	    Color prevColor = g.getColor();
	    int ncol = columns != null ? columns.length : tm.getColumnCount();
	    int[] xPos = new int[ncol];
	    int[] yPos = new int[ncol];
	    Insets inset = getInsets();
	    int iW = getWidth() - inset.left - inset.right;
	    int iH = getHeight() - inset.top - inset.bottom;
	    double incr = (iW) / (ncol - 1.);
	    g.setColor(Color.lightGray);
	    for (int c = 0; c < ncol; c++) {
		xPos[c] = 1 + (int) (c * incr);
		int tc = columns != null ? columns[c] : c;
		axis[tc].setSize(iH);
		g.drawLine(xPos[c], inset.top, xPos[c], getHeight()
			- inset.bottom);
		double[] ticks = axis[tc].getTicks();
		for (int i = 0; i < ticks.length; i += 2) {
		    int y = (int) ticks[i] + inset.top;
		    g.drawLine(xPos[c] - 1, y, xPos[c] + 1, y);
		}
	    }
	    g.setColor(getForeground());
	    // set xPos
	    // set and draw yPos
	    if (rsm != null) {
		for (int r = rsm.getMinSelectionIndex(); r <= rsm
			.getMaxSelectionIndex(); r++) {
		    if (rsm.isSelectedIndex(r)) {
			drawRow(g, xPos, yPos, r, inset.top);
		    }
		}
	    } else if (rows != null) {
		for (int i = 0; i < rows.length; i++) {
		    int r = rows[i];
		    drawRow(g, xPos, yPos, r, inset.top);
		}
	    } else {
		for (int r = 0; r < tm.getRowCount(); r++) {
		    drawRow(g, xPos, yPos, r, inset.top);
		}
	    }
	    g.setColor(prevColor);
	}

	private void drawRow(Graphics g, int[] xPos, int[] yPos, int r, int yd) {
	    for (int c = 0; c < yPos.length; c++) {
		int tc = columns != null ? columns[c] : c;
		ColumnMap colMap = ctx.getColumnMap(tm, tc);
		yPos[c] = axis[tc].getIntPosition(colMap.getMapValue(r)) + yd;
	    }
	    g.setColor(getColor(r));
	    g.drawPolyline(xPos, yPos, xPos.length);
	}

	// NO-OP validate, revalidate, repaint, and firePropertyChange
	@Override
	public void validate() {
	}

	@Override
	public void revalidate() {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
		boolean newValue) {
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
		Object newValue) {
	}

    }

    class AxisRenderer extends AxisDisplay implements TableCellRenderer {
	/**
	 * Create an AxisRenderer with position defaulting to
	 * AxisComponent.BOTTOM.
	 */
	public AxisRenderer() {
	    setAxis(new LinearAxis());
	}

	/**
	 * Create an AxisRenderer with the given axis and default position of
	 * AxisComponent.BOTTOM.
	 * 
	 * @param axis
	 *            the axis to display
	 */
	public AxisRenderer(Axis axis) {
	    setAxis(axis);
	}

	/**
	 * Create an AxisRenderer with the given axis and default position of
	 * AxisComponent.BOTTOM.
	 * 
	 * @param axis
	 *            the axis to display
	 * @param position
	 *            position of the axis display releative to the graph.
	 */
	public AxisRenderer(Axis axis, int position) {
	    setPosition(position);
	    setAxis(axis);
	}

	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {
	    // getAxis().setMin();
	    // getAxis().setMax();
	    return this;
	}

	// NO-OP validate, revalidate, repaint, and firePropertyChange
	@Override
	public void validate() {
	}

	@Override
	public void revalidate() {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
		boolean newValue) {
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
		Object newValue) {
	}

    }

    /**
     * Display a parallel coordinate view of table of data. usage: java
     * edu.umn.genomics.table.ParallelCoordinatePanel filename The file should
     * have a format that the FileTableModel class can read. If there are no
     * arguments, random data will be generated.
     * 
     * @param args
     *            the filename of the table data
     * @see FileTableModel
     */
    public static void main(String[] args) {
	JFrame frame = new JFrame("Parallel Coordinates");
	TableModel tm = null;
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	if (args.length > 0) {
	    try {
		tm = new FileTableModel(args[0]);
	    } catch (Exception ex) {
		System.err.println(args[0] + " " + ex);
		System.exit(1);
	    }
	} else {
	    Vector columnNames = new Vector(3);
	    columnNames.addElement("NRSeq");
	    columnNames.addElement("Randon ints");
	    columnNames.addElement("NRRef");
	    columnNames.addElement("Count");
	    Vector data = new Vector();
	    Vector pfams = new Vector();
	    pfams.addElement("kinase");
	    pfams.addElement("signalpeptide");
	    pfams.addElement("srp");
	    pfams.addElement("snap");
	    pfams.addElement("actin");
	    for (int i = 0; i < 30; i++) {
		Vector v = new Vector(4);
		v.addElement("SWP:0000" + i);
		v.addElement(new Integer((int) (Math.random() * 78)));
		v.addElement(pfams.elementAt((int) (Math.random() * pfams
			.size())));
		v.addElement(new Double(Math.random() * 50.));
		data.addElement(v);
	    }
	    tm = new DefaultTableModel(data, columnNames);
	}
	ParallelCoordinatePanel pc = new ParallelCoordinatePanel(tm);
	// pc.setBorder(BorderFactory.createEmptyBorder(50,20,10,20));
	frame.getContentPane().add(pc, BorderLayout.CENTER);
	frame.setLocation(200, 200);
	frame.setSize(400, 400);
	frame.setVisible(true);
    }
}
