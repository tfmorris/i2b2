/*
 * @(#) $RCSfile: JTableView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * JTableView presents the table as a JTable with the ability to sort the values
 * on a column. It also adds a ListSelectionView along the left side of the
 * JTable that displays the relative location of all selected rows in the table,
 * Clicking on the ListSelectView will scroll the JTable to that location.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.JTable
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see ListSelectionView
 */
public class JTableView extends AbstractTableModelView implements Serializable {
    /*
     * Use a VirtualTableModelProxy so we can sort on a column. Use
     * IndexMapSelection to map selections to the sorted row indexes.
     */
    /** Icon for displaying sorting info on the TableColumnHeaders. */
    SortArrow sortArrow = new SortArrow(false, false);
    RowSortArrow rowSortArrow = new RowSortArrow();

    /**
     * Column Header renderer class that has SortArrow icons.
     */
    class SortColumnRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {

	    Icon icon = sortArrow.get(true, false, Color.black);
	    int ci = table.convertColumnIndexToModel(column);
	    int ciDesc = -ci - 1;
	    for (int i = 0; sortColumns != null && i < sortColumns.length; i++) {
		float c = 1f - (1f / (i + 1));
		if (sortColumns[i] == ci) {
		    icon = sortArrow.get(true, true, new Color(c, c, c));
		    break;
		} else if (sortColumns[i] == ciDesc) {
		    icon = sortArrow.get(false, true, new Color(c, c, c));
		    break;
		}
	    }
	    setIcon(icon);
	    setHorizontalTextPosition(SwingConstants.RIGHT);
	    setHorizontalAlignment(SwingConstants.LEFT);
	    if (table != null) {
		JTableHeader header = table.getTableHeader();
		if (header != null) {
		    setForeground(header.getForeground());
		    setBackground(header.getBackground());
		    setFont(header.getFont());
		}
	    }
	    setText((value == null) ? "" : value.toString());
	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    return this;
	}
    }

    /**
     * Column Header renderer class that has SortArrow icons.
     */
    class NumberRenderer extends DefaultTableCellRenderer {
	NumberFormat nf = NumberFormat.getIntegerInstance();
	String pattern = "0.###E0";
	DecimalFormat df = new DecimalFormat(pattern);

	public NumberRenderer() {
	    super();
	    setHorizontalAlignment(SwingConstants.RIGHT);
	}

	/**
	 * Sets the <code>String</code> object for the cell being rendered to
	 * <code>value</code>.
	 * 
	 * @param value
	 *            the string value for this cell; if value is
	 *            <code>null</code> it sets the text value to an empty
	 *            string
	 * @see JLabel#setText
	 * 
	 */
	@Override
	protected void setValue(Object value) {
	    setText((value == null) ? "" : value instanceof Integer
		    || value instanceof Short || value instanceof Character
		    || value instanceof Byte ? nf.format(value)
		    : value instanceof Number ? df.format(value) : value
			    .toString());
	}
    }

    class BooleanRenderer extends JCheckBox implements TableCellRenderer {
	public BooleanRenderer() {
	    super();
	    setHorizontalAlignment(SwingConstants.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {
	    if (isSelected) {
		setForeground(table.getSelectionForeground());
		super.setBackground(table.getSelectionBackground());
	    } else {
		setForeground(table.getForeground());
		setBackground(table.getBackground());
	    }
	    setSelected((value != null && ((Boolean) value).booleanValue()));
	    setEnabled(value != null); // Added from the JTable BooleanRenderer
	    // version
	    return this;
	}
    }

    /**
     * Generates row numbers for a JList.
     */
    class RowNumListModel extends AbstractListModel {
	public void setSize(int size) {
	    fireContentsChanged(this, 0, size - 1);
	}

	public int getSize() {
	    return vtm != null ? vtm.getRowCount() : 0;
	}

	public Object getElementAt(int index) {
	    return new Integer((indexMap != null ? indexMap.getSrc(index)
		    : index) + 1);
	}
    }

    RowNumListModel rowNumLM = new RowNumListModel();
    JList rowNums = new JList(rowNumLM);
    JTable table = null;
    JScrollPane jsp = null;
    JToolBar toolbar = new JToolBar();
    /** a global selection view */
    ListSelectionView lsv = null;
    /** The actual table data. */
    TableModel tm = null;
    /** The row selection model for the actual data table. */
    ListSelectionModel lsm = null;
    /** The TableContext that registers tableModels and Row Selections. */
    TableContext ctx = null;
    /**
     * a virtual model on the real table so that we can sort rows or add
     * columns.
     */
    VirtualTableModelProxy vtm = new VirtualTableModelProxy();
    /** The row selection model for the virtual table model */
    DefaultListSelectionModel tlsm = new DefaultListSelectionModel();
    /** Maps row indices between the tm and vtm TableModels. */
    IndexMap indexMap = null;
    /**
     * Maps the row selections between the tm and vtm row SelectionModels lsm
     * and tlsm.
     */
    IndexMapSelection ims = null;
    /** The default cursor, saved when we show Wait cursor while sorting. */
    Cursor cursor = getCursor();
    boolean showRowNums = false;
    /** Columns to sort on */
    int[] sortColumns = null;
    int sortOrder = CellMap.ROWORDERSORT;
    boolean sortAscending = false;
    JTextArea textArea = new JTextArea();

    TableModelListener tml = new TableModelListener() {
	public void tableChanged(TableModelEvent e) {
	    if (e.getSource() != null) {
		try {
		    // System.err.println("TMEvt\t" +
		    // vtm.getTableModel().getRowCount() + "\t" + e.getType() +
		    // " : " + e.getFirstRow() + " - " + e.getLastRow() +
		    // e.getSource());
		    showRowNumbers(showRowNums);
		    if (lsv != null && table != null) {
			lsv.setListSize(table.getModel().getRowCount());
			lsv.setSelectionModel(table.getSelectionModel());
		    } else {
		    }
		    if (vtm.getIndexMap() != null) {
			if (vtm.getIndexMap() != indexMap
				|| vtm.getTableModel().getRowCount() != vtm
					.getIndexMap().getDstSize()) {
			    if (sortThread != null) {
				sortThread.reSort();
			    } else {
				sortThread = new SortThread(sortColumns);
				sortThread.start();
			    }
			}
		    }
		    rowNumLM.setSize(vtm.getRowCount());
		    // table.invalidate();
		    // jsp.validate();
		    repaint();
		} catch (Exception ex) {
		    System.err.println("lsv ex : " + ex);
		}
	    }
	}

	@Override
	public String toString() {
	    return lsv.hashCode() + " " + lsv.toString();
	}
    };

    /**
     * Thread class to sort the table.
     */
    class SortThread extends Thread {
	int[] column;
	boolean sortAgain = false;

	SortThread(int[] column) {
	    this.column = column;
	}

	public void reSort() {
	    sortAgain = true;
	}

	@Override
	public void run() {
	    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	    // System.err.println("Sort run \t" + Thread.currentThread());
	    do {
		sortAgain = false;
		try {
		    sortOnColumn(column);
		} catch (Exception iex) {
		    System.err.println("Interrupted\t" + Thread.currentThread()
			    + " " + iex);
		}
	    } while (sortAgain);
	    setCursor(cursor);
	    // System.err.println("Sort done\t" + Thread.currentThread());
	}
    }

    SortThread sortThread = null;

    /**
     * This removes this view as a listener to the TableModel and to the
     * ListSelectionModel. Classes overriding this method should call
     * super.cleanUp();
     */
    @Override
    public void cleanUp() {
	if (ims != null) {
	    ims.cleanUp();
	}
	super.cleanUp();
    }

    /**
     * Initialize this View.
     */
    private void init() {
	setLayout(new BorderLayout());
	if (table == null) {
	    table = new JTable(vtm);
	    table.getTableHeader().setDefaultRenderer(new SortColumnRenderer());
	    // Should make formatting be a user preference
	    table.setDefaultRenderer(java.sql.Time.class,
		    new DefaultTableCellRenderer());
	    table.setDefaultRenderer(java.sql.Timestamp.class,
		    new DefaultTableCellRenderer());
	    // table.setDefaultRenderer(java.lang.Number.class, new
	    // NumberRenderer());
	    // table.setDefaultRenderer(java.lang.Double.class, new
	    // NumberRenderer());
	    // table.setDefaultRenderer(java.lang.Float.class, new
	    // NumberRenderer());
	    // table.setDefaultRenderer(java.lang.Integer.class, new
	    // NumberRenderer());
	    table.setDefaultRenderer(java.lang.Boolean.class,
		    new BooleanRenderer());
	    //
	    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    if (lsm != null) {
		ims = new IndexMapSelection(lsm, tlsm, vtm.getIndexMap());
	    }
	    table.setSelectionModel(tlsm);
	    rowNums.setSelectionModel(tlsm);
	    if (true) { // Cells now handles sorting for JDK1.1 too.
		table.getTableHeader().addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
			int colIdx = table.getTableHeader().columnAtPoint(
				e.getPoint());
			Rectangle rect = table.getTableHeader().getHeaderRect(
				colIdx);
			if (colIdx >= 0
				&& e.getX() < rect.x + sortArrow.getIconWidth()) {
			    TableColumnModel columnModel = table
				    .getColumnModel();
			    int viewColumn = columnModel.getColumnIndexAtX(e
				    .getX());
			    int column = table
				    .convertColumnIndexToModel(viewColumn);
			    if (e.getClickCount() == 1 && column != -1) {
				if (e.isControlDown()) {
				    removeSortColumn(column);
				} else {
				    boolean ascending = e.getY() <= rect.y
					    + rect.height / 2 ? true : false;
				    int ci = ascending ? column : -column - 1;
				    setSortColumns(ci, e.isShiftDown());
				}
			    }
			    return;
			}
		    }
		});
	    }
	}
	jsp = new JScrollPane(table,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	rowNums.setFixedCellHeight(table.getRowHeight());
	rowNums.setBackground(table.getTableHeader().getBackground());
	jsp.setRowHeaderView(rowNums);
	rowNums.setToolTipText("Table Row");
	JLabel rowNumSortLbl = new JLabel(rowSortArrow);
	rowNumSortLbl.setToolTipText("Sort on Table Row");
	rowNumSortLbl.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	rowNumSortLbl.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (e.isControlDown()) {
		    removeSortColumn(Cells.ROWNUM_ASCENDING);
		} else {
		    boolean ascending = e.getY() <= ((Component) e.getSource())
			    .getSize().height / 2 ? true : false;
		    setSortColumns(ascending ? Cells.ROWNUM_ASCENDING
			    : Cells.ROWNUM_DESCENDING, e.isShiftDown());
		}
	    }
	});
	jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, rowNumSortLbl);

	// Add the Global list selection view widget.
	lsv = new ListSelectionView(tlsm, vtm.getRowCount());
	lsv.setSelectionModel(table.getSelectionModel());
	lsv.setListSize(table.getModel().getRowCount());
	lsv.setOpaque(true);
	lsv.setPreferredSize(new Dimension(8, 100));
	lsv.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		int nrows = table.getRowCount();
		table.scrollRectToVisible(table.getCellRect((int) Math
			.round((double) e.getY()
				/ ((Component) e.getSource()).getSize().height
				* nrows), 0, true));
	    }
	});
	jsp.getViewport().addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		if (lsv != null && table != null) {
		    int nrows = table.getRowCount();
		    int maxrow = nrows > 1 ? nrows - 1 : 1;
		    Rectangle vr = jsp.getViewport().getViewRect();
		    double tr = table.rowAtPoint(new Point(0, vr.y));
		    double br = table.rowAtPoint(new Point(0, vr.y + vr.height
			    - 1));
		    lsv.setWindow(tr / maxrow, br / maxrow);
		} else {
		}
	    }
	});

	JCheckBox jcbx;
	jcbx = new JCheckBox("Row Numbers", showRowNums);
	jcbx.setToolTipText("Show Row Numbers");
	jcbx.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    showRowNumbers(true);
		} else if (e.getStateChange() == ItemEvent.DESELECTED) {
		    showRowNumbers(false);
		}
	    }
	});
	// toolbar.add(jcbx);

	jcbx = new JCheckBox("Fit Columns", false);
	jcbx.setToolTipText("Show the Whole Table");
	jcbx.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		table
			.setAutoResizeMode(e.getStateChange() == ItemEvent.SELECTED ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
				: JTable.AUTO_RESIZE_OFF);
	    }
	});
	toolbar.add(jcbx);

	jcbx = new JCheckBox("Select Columns", false);
	jcbx.setToolTipText("Allow selection of columns as well as rows.");
	jcbx.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		table
			.setColumnSelectionAllowed(e.getStateChange() == ItemEvent.SELECTED ? true
				: false);
	    }
	});
	toolbar.add(jcbx);

	table.setDragEnabled(true);

	JButton jbtn = new JButton("Copy");
	jbtn.setToolTipText("Copies selected cells to ClipBoard.");
	jbtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		textArea.setText("");
		int[] selRows = table.getSelectedRows();
		int[] selCols = table.getSelectedColumns();
		int colCnt = table.getColumnCount();
		for (int ri = 0; ri < selRows.length; ri++) {
		    if (table.getColumnSelectionAllowed()) {
			for (int ci = 0; ci < selCols.length; ci++) {
			    if (ci > 0)
				textArea.append("\t");
			    Object val = table.getValueAt(selRows[ri],
				    selCols[ci]);
			    textArea.append(val != null ? val.toString() : "");
			}
		    } else {
			for (int ci = 0; ci < colCnt; ci++) {
			    if (ci > 0)
				textArea.append("\t");
			    Object val = table.getValueAt(selRows[ri], ci);
			    textArea.append(val != null ? val.toString() : "");
			}
		    }
		    textArea.append("\n");
		}
		textArea.selectAll();
		textArea.copy();
	    }
	});
	toolbar.add(jbtn);

	add(toolbar, BorderLayout.NORTH);
	JPanel lsvPnl = new JPanel(new BorderLayout());
	lsv.setBorder(BorderFactory.createLoweredBevelBorder());
	lsvPnl.setBorder(BorderFactory.createEtchedBorder());
	final JLabel lsvSort = new JLabel(new RowSortArrow(
		Cells.SELECTED_ROWS_FIRST, Cells.SELECTED_ROWS_LAST));
	lsvSort.setToolTipText("Sort by rows selected");
	lsvSort.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	lsvSort.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (e.isControlDown()) {
		    removeSortColumn(Cells.SELECTED_ROWS_FIRST);
		} else {
		    boolean ascending = e.getY() <= ((Component) e.getSource())
			    .getSize().height / 2 ? true : false;
		    setSortColumns(ascending ? Cells.SELECTED_ROWS_FIRST
			    : Cells.SELECTED_ROWS_LAST, e.isShiftDown());
		}
	    }
	});
	table.getTableHeader().addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		lsvSort.setPreferredSize(new Dimension(
			sortArrow.getIconWidth(), Math.max(sortArrow
				.getIconHeight(), table.getTableHeader()
				.getPreferredSize().height)));
	    }
	});
	lsv.setPreferredSize(lsvSort.getPreferredSize());
	lsvPnl.add(lsvSort, BorderLayout.NORTH);
	lsvPnl.add(lsv, BorderLayout.WEST);
	add(lsvPnl, BorderLayout.WEST);
	add(jsp, BorderLayout.CENTER);
    }

    private synchronized void removeSortColumn(int column) {
	if (sortColumns != null && sortColumns.length > 0) {
	    int ciAsc = column >= 0 ? column
		    : column == Cells.ROWNUM_DESCENDING ? Cells.ROWNUM_ASCENDING
			    : -column - 1;
	    int ciDesc = column < 0 ? column
		    : column == Cells.ROWNUM_ASCENDING ? Cells.ROWNUM_DESCENDING
			    : -column - 1;
	    for (int i = 0; i < sortColumns.length; i++) {
		if (sortColumns[i] == ciAsc || sortColumns[i] == ciDesc) {
		    if (sortColumns.length > 1) {
			int[] tmp = sortColumns;
			sortColumns = new int[tmp.length - 1];
			if (sortColumns.length > i) {
			    System.arraycopy(tmp, i + 1, sortColumns, i,
				    sortColumns.length - i);
			}
			System.arraycopy(tmp, 0, sortColumns, 0, i);
		    } else {
			sortColumns = null;
		    }
		    if (sortThread != null) {
			sortThread.interrupt();
			sortThread = null;
		    }
		    sortThread = new SortThread(sortColumns);
		    sortThread.start();
		    break;
		}
	    }
	}
    }

    private synchronized void setSortColumns(int column, boolean append) {
	boolean sortNeeded = true;
	boolean ascending = column >= 0;
	if (append && sortColumns != null && sortColumns.length > 0) {
	    int ciAsc = column == Cells.ROWNUM_DESCENDING ? Cells.ROWNUM_ASCENDING
		    : column == Cells.SELECTED_ROWS_LAST ? Cells.SELECTED_ROWS_FIRST
			    : column >= 0 ? column : -column - 1;
	    int ciDesc = column == Cells.ROWNUM_ASCENDING ? Cells.ROWNUM_DESCENDING
		    : column == Cells.SELECTED_ROWS_FIRST ? Cells.SELECTED_ROWS_LAST
			    : column < 0 ? column : -column - 1;
	    boolean inList = false;
	    for (int i = 0; i < sortColumns.length; i++) {
		if (sortColumns[i] == ciAsc || sortColumns[i] == ciDesc) {
		    inList = true;
		    if (ascending && sortColumns[i] != ciAsc) {
			sortColumns[i] = ciAsc;
		    } else if (!ascending && sortColumns[i] != ciDesc) {
			sortColumns[i] = ciDesc;
		    } else {
			if (ciAsc != Cells.SELECTED_ROWS_FIRST) {
			    sortNeeded = false;
			}
		    }
		    break;
		}
	    }
	    if (!inList) {
		int[] tmp = sortColumns;
		sortColumns = new int[tmp.length + 1];
		System.arraycopy(tmp, 0, sortColumns, 0, tmp.length);
		sortColumns[sortColumns.length - 1] = column;
	    }
	} else {
	    if (sortColumns != null && sortColumns.length == 1
		    && sortColumns[0] == column) {
		if (column != Cells.SELECTED_ROWS_FIRST
			&& column != Cells.SELECTED_ROWS_LAST) {
		    sortNeeded = false;
		}
	    } else {
		sortColumns = new int[1];
		sortColumns[0] = column;
	    }
	}
	if (sortNeeded) {
	    if (sortThread != null) {
		sortThread.interrupt();
		sortThread = null;
	    }
	    sortThread = new SortThread(sortColumns);
	    sortThread.start();
	}
    }

    /**
     * Sort selected rows either first or last.
     * 
     * @param selectedFirst
     *            If true, sort selected rows first, else last.
     */
    public void sortOnSelection(boolean selectedFirst) {
	int selLen = 0;
	int notLen = 0;
	if (tlsm != null && tlsm.getMinSelectionIndex() >= 0) {
	    IndexMap map = indexMap;
	    int si[] = new int[vtm.getRowCount()];
	    int ri = 0;
	    for (int i = 0; ri < si.length && i < si.length; i++) {
		if (tlsm.isSelectedIndex(i) == selectedFirst) {
		    si[ri++] = map != null ? map.getSrc(i) : i;
		}
	    }
	    for (int i = 0; ri < si.length && i < si.length; i++) {
		if (tlsm.isSelectedIndex(i) != selectedFirst) {
		    si[ri++] = map != null ? map.getSrc(i) : i;
		}
	    }
	    map = new OneToOneIndexMap(si);
	    if (ims != null) {
		ims.cleanUp();
		ims = null;
	    }
	    tlsm = new DefaultListSelectionModel();
	    if (lsm != null) {
		ims = new IndexMapSelection(lsm, tlsm, map);
	    }
	    setIndexMap(map);
	    table.setSelectionModel(tlsm);
	    rowNums.setSelectionModel(tlsm);
	    lsv.setSelectionModel(tlsm);
	    setCursor(cursor);
	    repaint();
	}
    }

    /**
     * Sort rows according by the columns in the column array. The column
     * indices are interpretted as: index >= 0 sort on this column in ascending
     * order index < 0 sort on column -index-1 in descending order index ==
     * Cells.ROWNUM_ASCENDING sort by table row order ascending index ==
     * Cells.ROWNUM_DESCENDING sort by table row order descending index ==
     * Cells.SELECTED_ROWS_FIRST sort selected table rows first index ==
     * Cells.SELECTED_ROWS_LAST sort selected table rows last If column is null,
     * don't sort the table. If column.length == 0, sort in reverse table order.
     * 
     * @param column
     *            An array of column indices which define the sort order.
     */
    private void sortOnColumn(int[] column) {
	// System.err.println(" sort start  " + column);
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	// Disconnect the mapped selection models
	if (ims != null) {
	    ims.cleanUp();
	    ims = null;
	}
	OneToOneIndexMap map = null;
	try {
	    if (column == null
		    || (column.length > 0 && column[0] == Cells.ROWNUM_ASCENDING)) {
	    } else if (column.length < 1
		    || (column.length > 0 && column[0] == Cells.ROWNUM_DESCENDING)) {
		int si[] = new int[vtm.getRowCount()];
		for (int i = 0, j = si.length - 1; i < si.length; i++, j--) {
		    si[i] = j;
		}
		map = new OneToOneIndexMap(si);
	    } else {
		// Need original unmapped order for sorting
		setIndexMap(null);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		int ci = column[0] >= 0 ? column[0] : -column[0] - 1;
		if (ci < vtm.getColumnCount()
			&& vtm.getColumnList().getElementAt(ci) instanceof RowNumColumn) {
		    if (column[0] < 0) {
			int si[] = new int[vtm.getRowCount()];
			for (int i = 0, j = si.length - 1; i < si.length; i++, j--) {
			    si[i] = j;
			}
			map = new OneToOneIndexMap(si);
		    }
		} else {
		    int si[] = Cells.getSortIndex(vtm, lsm, column);
		    map = new OneToOneIndexMap(si);
		}
	    }
	} catch (Exception ex) {
	}
	tlsm = new DefaultListSelectionModel();
	if (lsm != null) {
	    ims = new IndexMapSelection(lsm, tlsm, map);
	}
	setIndexMap(map);
	table.setSelectionModel(tlsm);
	rowNums.setSelectionModel(tlsm);
	lsv.setSelectionModel(tlsm);
	setCursor(cursor);
	repaint();
	// System.err.println(" sort done   " + column);
    }

    /**
     * Return the toolbar.
     */
    public JToolBar getToolbar() {
	return toolbar;
    }

    /**
     * Sets whether to display a row number column in the table.
     * 
     * @param showRowNumbers
     *            If true display a row number column.
     */
    public void showRowNumbers(boolean showRowNumbers) {
	showRowNums = showRowNumbers;
	if (vtm != null) {
	    int rowNumCol = -1;
	    ListModel cols = vtm.getColumnList();
	    if (cols != null) {
		for (int c = 0; c < cols.getSize(); c++) {
		    Object col = cols.getElementAt(c);
		    if (col != null && col instanceof RowNumColumn) {
			rowNumCol = c;
		    }
		}
	    }
	    if (showRowNumbers) {
		if (rowNumCol < 0) {
		    if (ims != null) {
			ims.cleanUp();
			ims = null;
		    }
		    vtm.addColumn(new RowNumColumn(), 0);
		    if (sortColumns != null) {
			for (int i = 0; i < sortColumns.length; i++) {
			    sortColumns[i] += sortColumns[i] >= 0 ? 1 : -1;
			}
		    }
		    // Handle Selection Model
		    // tlsm = new DefaultListSelectionModel();
		    if (lsm != null) {
			ims = new IndexMapSelection(lsm, tlsm, vtm
				.getIndexMap());
		    }
		    // table.setSelectionModel(tlsm);
		    // lsv.setSelectionModel(tlsm);
		}
	    } else {
		if (rowNumCol >= 0) {
		    if (ims != null) {
			ims.cleanUp();
			ims = null;
		    }
		    vtm.removeColumn(rowNumCol);
		    if (sortColumns != null) {
			for (int i = 0; i < sortColumns.length; i++) {
			    sortColumns[i] -= sortColumns[i] >= 0 ? 1 : -1;
			}
		    }
		    // Handle Selection Model
		    // tlsm = new DefaultListSelectionModel();
		    if (lsm != null) {
			ims = new IndexMapSelection(lsm, tlsm, vtm
				.getIndexMap());
		    }
		    // table.setSelectionModel(tlsm);
		    // lsv.setSelectionModel(tlsm);
		}
	    }
	}
    }

    /**
     * Constructs a JTableView display. Nothing will be displayed until a data
     * model is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public JTableView() {
	super();
	init();
    }

    /**
     * Constructs a JTableView diaplay which is initialized with tableModel as
     * the data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public JTableView(TableModel tableModel) {
	super();
	init();
	setTableModel(tableModel);
    }

    /**
     * Constructs a JTableView diaplay which is initialized with tableModel as
     * the data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public JTableView(TableModel tableModel, ListSelectionModel lsm) {
	super();
	init();
	setTableModel(tableModel);
	setSelectionModel(lsm);
    }

    /**
     * Sets tableModel as the data model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	tm = tableModel;
	if (vtm != null) {
	    vtm.setTableModel(tm);
	    table.setModel(vtm);
	    vtm.addTableModelListener(tml);
	    showRowNumbers(showRowNums);
	    ListSelectionModel _lsm = lsm;
	    if (_lsm != null) {
		lsm = null;
		setSelectionModel(_lsm);
	    } else {
		table.setSelectionModel(tlsm);
	    }
	    if (lsv != null) {
		lsv.setSelectionModel(table.getSelectionModel());
		lsv.setListSize(table.getModel().getRowCount());
	    }
	    rowNumLM.setSize(vtm.getRowCount());
	}
	repaint();
    }

    /**
     * Return the table model being displayed.
     * 
     * @return the table being displayed.
     */
    @Override
    public TableModel getTableModel() {
	return tm;
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
	if (newModel != null && newModel != lsm) {
	    lsm = newModel;
	    if (table != null && tm != null) {
		if (ims != null) {
		    ims.cleanUp();
		    ims = null;
		}
		tlsm = new DefaultListSelectionModel();
		if (lsm != null) {
		    ims = new IndexMapSelection(lsm, tlsm, vtm.getIndexMap());
		}
		table.setSelectionModel(tlsm);
		rowNums.setSelectionModel(tlsm);
	    }
	    if (lsv != null) {
		lsv.setSelectionModel(table.getSelectionModel());
	    }
	    repaint();
	}
    }

    /**
     * Returns the ListSelectionModel that is used to maintain row selection
     * state.
     * 
     * @return the object that provides row selection state.
     */
    @Override
    public ListSelectionModel getSelectionModel() {
	return table.getSelectionModel();
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return this.jsp;
    }

    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @param ctx
     *            The context to use for TableModels and Views.
     */
    @Override
    public void setTableContext(TableContext ctx) {
	this.ctx = ctx;
    }

    /**
     * Set the TableContext that manages TableModels and Views.
     * 
     * @return The context to use for TableModels and Views.
     */
    @Override
    public TableContext getTableContext() {
	return ctx;
    }

    /**
     * Set an IndexMap to provide a sorting on the table.
     * 
     * @param map
     *            A sort index.
     */
    private void setIndexMap(IndexMap map) {
	indexMap = map;
	((VirtualTableModelProxy) table.getModel()).setIndexMap(map);
    }

    class SortArrow implements Icon {
	int arrowSize = 3;
	int h = arrowSize * 4 + 4;
	int w = arrowSize * 2 + 4;

	int[] xpntU = { 0, -arrowSize, arrowSize };
	int[] ypntU = { 0, arrowSize * 2 - 2, arrowSize * 2 - 2 };

	int[] xpntD = { 0, -arrowSize, arrowSize };
	int[] ypntD = { arrowSize * 4 - 2, arrowSize * 2, arrowSize * 2 };

	int[] xpnt = xpntU;
	int[] ypnt = ypntU;

	boolean filled = false;
	boolean asc = true;
	Color fill = Color.black;

	public SortArrow() {
	}

	public SortArrow(boolean asc, boolean filled) {
	    this.asc = asc;
	    this.filled = filled;
	}

	public Icon get(boolean asc, boolean filled, Color color) {
	    this.asc = asc;
	    this.filled = filled;
	    this.fill = color;
	    return this;
	}

	public int getIconWidth() {
	    return w;
	}

	public int getIconHeight() {
	    return w;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	    Color prevColor = g.getColor();
	    int xt = x + w / 2;
	    int yt = y;
	    g.translate(xt, yt);
	    g.setColor(Color.white);
	    g.fillPolygon(xpnt, ypntU, xpnt.length);
	    g.fillPolygon(xpnt, ypntD, xpnt.length);
	    g.setColor(Color.black);
	    g.drawPolygon(xpnt, ypntU, xpnt.length);
	    g.drawPolygon(xpnt, ypntD, xpnt.length);
	    if (filled) {
		g.setColor(fill);
		if (asc) {
		    g.fillPolygon(xpnt, ypntU, xpnt.length);
		} else {
		    g.fillPolygon(xpnt, ypntD, xpnt.length);
		}
	    }
	    g.translate(-xt, -yt);
	    g.setColor(prevColor);
	}
    }

    class RowSortArrow extends SortArrow {
	int ascVal = Cells.ROWNUM_ASCENDING;
	int dscVal = Cells.ROWNUM_DESCENDING;

	public RowSortArrow() {
	}

	public RowSortArrow(int asc, int desc) {
	    this.ascVal = asc;
	    this.dscVal = desc;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
	    fill = Color.black;
	    filled = false;
	    asc = true;
	    if (sortColumns == null && ascVal == Cells.ROWNUM_ASCENDING) {
		filled = true;
		asc = true;
	    } else if (sortColumns != null && sortColumns.length < 1
		    && dscVal == Cells.ROWNUM_DESCENDING) {
		filled = true;
		asc = false;
	    } else if (sortColumns != null) {
		for (int i = 0; i < sortColumns.length; i++) {
		    if (sortColumns[i] == ascVal || sortColumns[i] == dscVal) {
			asc = sortColumns[i] >= 0 ? true : false;
			filled = true;
			float cv = 1f - (1f / (i + 1));
			fill = new Color(cv, cv, cv);
			break;
		    }
		}
	    }
	    super.paintIcon(c, g, x, y);
	}
    }

}
