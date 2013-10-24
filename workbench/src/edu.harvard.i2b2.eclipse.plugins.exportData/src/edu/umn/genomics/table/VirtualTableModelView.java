/*
 * @(#) $RCSfile: VirtualTableModelView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.lang.reflect.InvocationTargetException;

/**
 * VirtualTableModel
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see JSFormula
 * @see javax.swing.table.TableModel
 */
/*
 * Base table column objects field method methods params
 * 
 * remove | add delete | add delete -------------------+------------+-----------
 * virtualColumns | addColumns | scripts name | VirtualCell |
 * 
 * 
 * script defined vars table - the TableModel for which this script is invoked
 * row - the cell row for which this script is invoked col - the cell column for
 * which this script is invoked example script: table.getValueAt(row,0) -
 * table.getValueAt(row,1)
 */

public class VirtualTableModelView extends JPanel {
    String[] columnTypes = { "Table Column", "Text", "Integer", "Number",
	    "Date", "Boolean",
	    // "Image",
	    // "Color",
	    "BeanShell", "JavaScript", };

    Class[] columnTypeClass = { edu.umn.genomics.table.ColRef.class,
	    java.lang.String.class, java.lang.Integer.class,
	    java.lang.Double.class,
	    java.util.Date.class,
	    java.lang.Boolean.class,
	    // java.awt.Image.class,
	    // java.awt.Color.class,
	    edu.umn.genomics.table.BshFormula.class,
	    edu.umn.genomics.table.JSFormula.class, };

    Object[][] columnTypeInfo = { // Name, Class, VirtualCell
	    { "Table Column", edu.umn.genomics.table.ColRef.class,
		    edu.umn.genomics.table.ColRef.class },
	    { "Text", java.lang.String.class,
		    edu.umn.genomics.table.VectorColumn.class },
	    { "Integer", java.lang.Integer.class,
		    edu.umn.genomics.table.VectorColumn.class },
	    { "Number", java.lang.Double.class,
		    edu.umn.genomics.table.VectorColumn.class },
	    { "Date", java.util.Date.class,
		    edu.umn.genomics.table.VectorColumn.class },
	    { "Boolean", java.lang.Boolean.class,
		    edu.umn.genomics.table.VectorColumn.class },
	    //{"Image",java.awt.Image.class,edu.umn.genomics.table.VectorColumn.
	    // class},
	    //{"Color",java.awt.Color.class,edu.umn.genomics.table.VectorColumn.
	    // class},
	    { "BeanShell", edu.umn.genomics.table.BshFormula.class,
		    edu.umn.genomics.table.BshFormula.class },
	    { "JavaScript", edu.umn.genomics.table.JSFormula.class,
		    edu.umn.genomics.table.JSFormula.class }, };

    class ColumnTableModel extends AbstractTableModel {
	// columns name type class format
	// Name Table Reference String
	// Age Integer
	// BDay Date
	// Methods
	// Name getName(int) setName(int)
	// Class getColumnClass setColumnClass()
	// Type
	// Describe
	VirtualTableModel vtm = null;
	String[] columnNames = { "Name", "Class", "Type", "Description" };
	TableModelListener tml = new TableModelListener() {
	    public void tableChanged(TableModelEvent e) {
		if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
		    fireTableDataChanged();
		}
	    }
	};

	public ColumnTableModel(VirtualTableModel vtm) {
	    this.vtm = vtm;
	    vtm.addTableModelListener(tml);
	}

	public int getColumnCount() {
	    return columnNames.length;
	}

	public int getRowCount() {
	    return vtm.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
	    return column >= 0 && column < columnNames.length ? columnNames[column]
		    : super.getColumnName(column);
	}

	@Override
	public Class getColumnClass(int column) {
	    switch (column) {
	    case 0:
		return java.lang.String.class;
	    case 1:
		return java.lang.Class.class;
	    case 2:
		return java.lang.String.class;
	    case 3:
		return java.lang.String.class;
	    }
	    return null;
	}

	public Object getValueAt(int row, int column) {
	    switch (column) {
	    case 0:
		return vtm.getColumnName(row);
	    case 1:
		return vtm.getColumnClass(row);
	    case 2:
		try {
		    VirtualCell vcell = (VirtualCell) vtm.getColumnList()
			    .getElementAt(row);
		    return vcell.getType();
		} catch (Exception ex) {
		}
		return "Unknown";
	    case 3:
		try {
		    VirtualCell vcell = (VirtualCell) vtm.getColumnList()
			    .getElementAt(row);
		    return vcell.getDescription();
		} catch (Exception ex) {
		}
		return "Unknown";
	    }
	    return null;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
	    switch (column) {
	    case 0:
		return true;
	    case 1:
		return false;
	    case 2:
		return false;
	    case 3:
		try {
		    VirtualCell vcell = (VirtualCell) vtm.getColumnList()
			    .getElementAt(row);
		    if (vcell instanceof TableModelFormula) {
			return true;
		    }
		} catch (Exception ex) {
		}
		return false;
	    }
	    return false;
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
	    switch (column) {
	    case 0:
		vtm.setColumnName(aValue != null ? aValue.toString() : null,
			column);
		break;
	    case 1:
		break;
	    case 2:
		break;
	    case 3:
		try {
		    VirtualCell vcell = (VirtualCell) vtm.getColumnList()
			    .getElementAt(row);
		    if (vcell instanceof TableModelFormula) {
			((TableModelFormula) vcell)
				.setFormula(aValue != null ? aValue.toString()
					: "");
			if (vtm != null && vtm instanceof AbstractTableModel) {
			    ((AbstractTableModel) vtm)
				    .fireTableChanged(new TableModelEvent(vtm,
					    0, vtm.getRowCount() - 1, row,
					    TableModelEvent.UPDATE));
			}
		    }
		} catch (Exception ex) {
		}
		break;
	    }
	}

	@Override
	protected void finalize() throws Throwable {
	    vtm.removeTableModelListener(tml);
	    super.finalize();
	}
    }

    boolean columnChangesAllowed = true;
    boolean rowChangesAllowed = false;
    JToolBar tb = new JToolBar();
    JButton addColBtn;
    JButton insColBtn;
    JButton delColBtn;
    JButton addRowBtn;
    JButton insRowBtn;
    JButton delRowBtn;

    VirtualTableModel vtm;
    JTable table = new JTable();
    ColumnTableModel ctm = null;
    JTable colTable = new JTable();
    DefaultListSelectionModel colLSM = new DefaultListSelectionModel();
    JScrollPane jsp;
    JScrollPane cjsp;
    JTextField tableName = new JTextField();
    JFrame helpFrame = null;
    JEditorPane editorPane = null;
    JPanel viewPanel = new JPanel(new BorderLayout());
    JPanel editPanel = new JPanel(new BorderLayout());
    JPanel colPanel = new JPanel(new BorderLayout());
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, colPanel,
	    viewPanel);

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
	    return new Integer(index + 1);
	    // return new Integer((indexMap != null ? indexMap.getSrc(index) :
	    // index) + 1);
	}
    }

    RowNumListModel rowNumLM = new RowNumListModel();
    JList rowNums = new JList(rowNumLM);
    TableModelListener tml = new TableModelListener() {
	public void tableChanged(TableModelEvent e) {
	    if (e.getSource() != null) {
		rowNumLM.setSize(((TableModel) e.getSource()).getRowCount());
	    }
	    repaint();
	}
    };

    MouseAdapter colSelector = new MouseAdapter() {
	@Override
	public void mouseClicked(MouseEvent e) {
	    int colIdx = table.getTableHeader().columnAtPoint(e.getPoint());
	    Rectangle rect = table.getTableHeader().getHeaderRect(colIdx);
	    if (colIdx >= 0) {
		TableColumnModel columnModel = table.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(e.getX());
		int column = table.convertColumnIndexToModel(viewColumn);
		if (e.getClickCount() == 1 && column != -1) {
		    if (e.isControlDown()) {
			if (colLSM.isSelectedIndex(column)) {
			    colLSM.removeSelectionInterval(column, column);
			} else {
			    colLSM.addSelectionInterval(column, column);
			}
		    } else {
			colLSM.setSelectionInterval(column, column);
		    }
		}
		table.getTableHeader().repaint();
		return;
	    }
	}
    };

    /**
     * Column Header renderer class that has SortArrow icons.
     */
    class ColumnRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {
	    int ci = table.convertColumnIndexToModel(column);
	    // setHorizontalTextPosition(JLabel.RIGHT);
	    // setHorizontalAlignment(JLabel.LEFT);
	    if (table != null) {
		JTableHeader header = table.getTableHeader();
		if (header != null) {
		    setForeground(header.getForeground());
		    setBackground(colLSM.isSelectedIndex(column) ? table
			    .getSelectionBackground() : header.getBackground());
		    setFont(header.getFont());
		}
	    }
	    setText((value == null) ? "" : value.toString());
	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    return this;
	}
    };

    private int getColumnCount() {
	return vtm != null ? vtm.getColumnCount() : 0;
    }

    public VirtualTableModelView(VirtualTableModel tableModel) {
	// this.vtm = tableModel;
	setVirtualTableModel(tableModel);
	ctm = new ColumnTableModel(vtm);
	table.setModel(vtm);
	colTable.setModel(ctm);
	colTable.setSelectionModel(colLSM);
	colTable.setDragEnabled(true);
	colTable.setColumnSelectionAllowed(true);
	setLayout(new BorderLayout());
	table.getTableHeader().setDefaultRenderer(new ColumnRenderer());
	table.getTableHeader().setReorderingAllowed(false);
	colLSM.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    table.getTableHeader().repaint();
		}
	    }
	});
	// Set the AbstractTableModel variable
	Icon addColIcon = null;
	Icon insColIcon = null;
	Icon delColIcon = null;
	Icon addRowIcon = null;
	Icon insRowIcon = null;
	Icon delRowIcon = null;

	try {
	    ClassLoader cl = this.getClass().getClassLoader();
	    // Java look and feel Graphics Repository: Table Toolbar Button
	    // Graphics
	    addColIcon = new ImageIcon(
		    cl
			    .getResource("edu/umn/genomics/table/Icons/ColumnInsertAfter24.gif"));
	    insColIcon = new ImageIcon(
		    cl
			    .getResource("edu/umn/genomics/table/Icons/ColumnInsertBefore24.gif"));
	    delColIcon = new ImageIcon(
		    cl
			    .getResource("edu/umn/genomics/table/Icons/ColumnDelete24.gif"));
	    addRowIcon = new ImageIcon(
		    cl
			    .getResource("edu/umn/genomics/table/Icons/RowInsertAfter24.gif"));
	    insRowIcon = new ImageIcon(
		    cl
			    .getResource("edu/umn/genomics/table/Icons/RowInsertBefore24.gif"));
	    delRowIcon = new ImageIcon(
		    cl
			    .getResource("edu/umn/genomics/table/Icons/RowDelete24.gif"));
	} catch (Exception ex) {
	    System.err.println("" + ex);
	}

	addColBtn = addColIcon != null ? new JButton(addColIcon) : new JButton(
		"Add Column");
	insColBtn = insColIcon != null ? new JButton(insColIcon) : new JButton(
		"Insert Column");
	delColBtn = delColIcon != null ? new JButton(delColIcon) : new JButton(
		"Delete Column");
	addRowBtn = addRowIcon != null ? new JButton(addRowIcon) : new JButton(
		"Add Row");
	insRowBtn = insRowIcon != null ? new JButton(insRowIcon) : new JButton(
		"Insert Row");
	delRowBtn = delRowIcon != null ? new JButton(delRowIcon) : new JButton(
		"Delete Row");

	addColBtn.setToolTipText("Add a new column after the current column.");
	insColBtn.setToolTipText("Add a new column before the current column.");
	delColBtn.setToolTipText("Remove the current column.");
	addRowBtn.setToolTipText("Add a new row after the current row.");
	insRowBtn.setToolTipText("Add a new row before the current row.");
	delRowBtn.setToolTipText("Remove the current row.");

	addColBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int colIndex = colLSM.getMaxSelectionIndex() + 1;
		newColumn(colIndex > 0 ? colIndex : vtm.getColumnCount());
	    }
	});
	insColBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int colIndex = colLSM.getMinSelectionIndex();
		newColumn(colIndex >= 0 ? colIndex : 0);
	    }
	});
	delColBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int min = colLSM.getMinSelectionIndex();
		if (min < 0) {
		} else {
		    int max = colLSM.getMaxSelectionIndex();
		    int[] colIndex = new int[max - min + 1];
		    int n = 0;
		    for (int i = min; i <= max; i++) {
			if (colLSM.isSelectedIndex(i)) {
			    colIndex[n++] = i;
			}
		    }
		    if (n < colIndex.length) {
			int[] tmp = colIndex;
			colIndex = new int[n];
			System.arraycopy(tmp, 0, colIndex, 0, n);
		    }
		    vtm.removeColumns(colIndex);
		    colLSM.clearSelection();
		}
	    }
	});
	addRowBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (vtm.getTableModel() != null
			&& vtm.getTableModel() instanceof DefaultTableModel) {
		    DefaultTableModel dtm = (DefaultTableModel) vtm
			    .getTableModel();
		    Object[] rowData = new Object[dtm.getColumnCount()];
		    dtm.addRow(rowData);
		}
	    }
	});
	insRowBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (vtm.getTableModel() != null
			&& vtm.getTableModel() instanceof DefaultTableModel) {
		    DefaultTableModel dtm = (DefaultTableModel) vtm
			    .getTableModel();
		    int rowIndex = rowNums.getSelectedIndex();
		    rowIndex = rowIndex >= 0 ? rowIndex : 0;
		    Object[] rowData = new Object[dtm.getColumnCount()];
		    dtm.insertRow(rowIndex, rowData);
		}
	    }
	});
	delRowBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (vtm.getTableModel() != null
			&& vtm.getTableModel() instanceof DefaultTableModel) {
		    DefaultTableModel dtm = (DefaultTableModel) vtm
			    .getTableModel();
		    int[] indices = rowNums.getSelectedIndices();
		    for (int i = indices.length - 1; i >= 0; i--) {
			dtm.removeRow(indices[i]);
		    }
		}
	    }
	});

	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	table.setRowSelectionAllowed(true);
	table.setCellSelectionEnabled(true);
	rowChangesAllowed = vtm.getTableModel() != null
		&& vtm.getTableModel() instanceof DefaultTableModel;
	setEditing(columnChangesAllowed, rowChangesAllowed);

	jsp = new JScrollPane(table);
	rowNums.setFixedCellHeight(table.getRowHeight());
	rowNums.setBackground(table.getTableHeader().getBackground());
	jsp.setRowHeaderView(rowNums);
	JLabel rowNumSortLbl = new JLabel("Row");
	rowNumSortLbl.setBackground(table.getTableHeader().getBackground());
	jsp.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, rowNumSortLbl);
	viewPanel.add(jsp);

	cjsp = new JScrollPane(colTable,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	colPanel.add(cjsp);
	split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cjsp, jsp);
	split.setOneTouchExpandable(true);
	split.resetToPreferredSizes();
	editPanel.add(split);
	if (vtm != null) {
	    tableName.setText(vtm.getName());
	}
	tableName.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    setTableName();
		} catch (Exception ex) {
		}
	    }
	});
	JPanel namePanel = new JPanel(new BorderLayout());
	namePanel.add(new JLabel("Table Name: "), BorderLayout.WEST);
	namePanel.add(tableName);

	add(namePanel, BorderLayout.NORTH);
	add(editPanel);
	split.setDividerLocation(-1);
    }

    public void setVirtualTableModel(VirtualTableModel vtm) {
	if (this.vtm != null) {
	    this.vtm.removeTableModelListener(tml);
	}
	this.vtm = vtm;
	ctm = new ColumnTableModel(vtm);
	table.setModel(vtm);
	colTable.setModel(ctm);
	colTable.setSelectionModel(colLSM);
	tableName.setText(vtm.getName());
	this.vtm.addTableModelListener(tml);
    }

    public VirtualTableModel getVirtualTableModel() {
	return vtm;
    }

    public void setEditing(boolean columnChangesAllowed,
	    boolean rowChangesAllowed) {
	if (columnChangesAllowed || rowChangesAllowed) {
	    tb.removeAll();
	    if (columnChangesAllowed) {
		tb.add(addColBtn);
		tb.add(insColBtn);
		tb.add(delColBtn);
		if (rowChangesAllowed) {
		    tb.addSeparator();
		}
		table.getTableHeader().addMouseListener(colSelector);
	    } else {
		table.getTableHeader().removeMouseListener(colSelector);
	    }
	    if (rowChangesAllowed) {
		tb.add(addRowBtn);
		tb.add(insRowBtn);
		tb.add(delRowBtn);
	    }
	    editPanel.add(tb, BorderLayout.NORTH);
	} else {
	    editPanel.remove(tb);
	}
    }

    private void showHelp(Class formulaClass) {
	if (helpFrame == null) {
	    helpFrame = new JFrame("Formula Help");
	    helpFrame.addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
		    try {
			helpFrame.dispose();
		    } catch (Exception ex) {
		    }
		    helpFrame = null;
		}

		@Override
		public void windowClosed(WindowEvent e) {
		    try {
			helpFrame.dispose();
		    } catch (Exception ex) {
		    }
		    helpFrame = null;
		}
	    });
	    editorPane = new JEditorPane();
	    editorPane.setEditable(false);
	    JScrollPane scrollpane = new JScrollPane(editorPane,
		    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scrollpane.setPreferredSize(new Dimension(600, 600));
	    JButton close = new JButton("close");
	    close.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			helpFrame.dispose();
		    } catch (Exception ex) {
		    }
		    helpFrame = null;
		}
	    });
	    JMenuBar mb = new JMenuBar();
	    mb.add(close);
	    helpFrame.getContentPane().add(mb, BorderLayout.NORTH);
	    helpFrame.getContentPane().add(scrollpane, BorderLayout.CENTER);
	    helpFrame.pack();
	}
	String help = "";
	try {
	    help = (String) formulaClass.getMethod("getHelpText", null).invoke(
		    null, null);
	    editorPane.setText(help);
	    helpFrame.setVisible(true);
	} catch (Exception ex) {
	    System.err.println(" help " + ex);
	}
    }

    // VirtualColumn
    // ColRef
    // TableModelFormula
    // VectorColumn

    public void newColumn(int columnIndex) {
	final int[] colIdx = new int[1];
	colIdx[0] = columnIndex;
	final JTextField columnName = new JTextField(20);
	final JComboBox columnType = new JComboBox(columnTypes);
	final JList tmCols = new JList(vtm.getDefaultColumnList());
	tmCols.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	tmCols.setVisibleRowCount(5);
	tmCols.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		Object obj = tmCols.getSelectedValue();
		if (obj != null) {
		    columnName.setText(obj.toString());
		}
	    }
	});
	final JScrollPane tjsp = new JScrollPane(tmCols,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	final JTextArea scriptArea = new JTextArea(10, 80);
	final JScrollPane sjsp = new JScrollPane(scriptArea,
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	final JButton helpBtn = new JButton("Help");
	helpBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Class selClass = columnTypeClass[columnType.getSelectedIndex()];
		if (edu.umn.genomics.table.TableModelFormula.class
			.isAssignableFrom(selClass)) {
		    showHelp(selClass);
		}
	    }
	});
	JPanel colPanel = new JPanel();
	colPanel.add(new JLabel("Data Type:"));
	colPanel.add(columnType);
	colPanel.add(helpBtn);
	colPanel.add(tjsp);
	colPanel.add(new JLabel("Column Name:"));
	colPanel.add(columnName);
	final Object[] options = { "Add", "Close" };
	final JPanel cPanel = new JPanel(new BorderLayout());
	cPanel.add(colPanel, BorderLayout.NORTH);
	cPanel.add(sjsp);
	Class selClass = columnTypeClass[columnType.getSelectedIndex()];
	tjsp.setVisible(edu.umn.genomics.table.ColRef.class
		.isAssignableFrom(selClass));
	sjsp.setVisible(edu.umn.genomics.table.TableModelFormula.class
		.isAssignableFrom(selClass));
	helpBtn.setVisible(edu.umn.genomics.table.TableModelFormula.class
		.isAssignableFrom(selClass));
	columnType.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    try {
			Class selClass = columnTypeClass[columnType
				.getSelectedIndex()];
			tjsp.setVisible(edu.umn.genomics.table.ColRef.class
				.isAssignableFrom(selClass));
			if (tjsp.isVisible()) {
			    Object obj = tmCols.getSelectedValue();
			    if (obj != null) {
				columnName.setText(obj.toString());
			    }
			}
			sjsp
				.setVisible(edu.umn.genomics.table.TableModelFormula.class
					.isAssignableFrom(selClass));
			helpBtn
				.setVisible(edu.umn.genomics.table.TableModelFormula.class
					.isAssignableFrom(selClass));
			if (sjsp.isVisible() && helpFrame != null) {
			    showHelp(selClass);
			}
			cPanel.validate();
		    } catch (Exception ex) {
		    }
		}
	    }
	});
	final JOptionPane optionPane = new JOptionPane(cPanel,
		JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
		options, options[0]);
	Component topLevel = this.getTopLevelAncestor();
	final JDialog dialog = topLevel instanceof Dialog ? new JDialog(
		(Dialog) this.getTopLevelAncestor(), "New Column", true)
		: new JDialog((Frame) this.getTopLevelAncestor(), "New Column",
			true);
	dialog.setContentPane(optionPane);
	dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	dialog.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent we) {
		// setLabel("Thwarted user attempt to close window.");
	    }
	});
	optionPane.addPropertyChangeListener(new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (dialog.isVisible() && (e.getSource() == optionPane)
			&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
		    Object optVal = optionPane.getValue();
		    if (optVal == options[0]) {

			Class selClass = columnTypeClass[columnType
				.getSelectedIndex()];
			String colName = columnName.getText();
			if (colName.length() > 0) {
			    if (edu.umn.genomics.table.ColRef.class
				    .isAssignableFrom(selClass)) {
				int idx = tmCols.getSelectedIndex();
				if (idx >= 0) {
				    newColumn(new ColRef(colName, vtm
					    .getTableModel(), idx), colIdx[0]);
				}
			    } else if (edu.umn.genomics.table.TableModelFormula.class
				    .isAssignableFrom(selClass)) {
				try {
				    Class argClass[] = new Class[3];
				    argClass[0] = String.class;
				    argClass[1] = TableModel.class;
				    argClass[2] = String.class;
				    Object args[] = new Object[3];
				    args[0] = colName;
				    args[1] = vtm;
				    args[2] = scriptArea.getText();
				    TableModelFormula formulaObj = (TableModelFormula) selClass
					    .getConstructor(argClass)
					    .newInstance(args);
				    newColumn(formulaObj, colIdx[0]);
				} catch (IllegalAccessException accex) {
				} catch (InvocationTargetException invex) {
				} catch (NoSuchMethodException nosmex) {
				} catch (InstantiationException instex) {
				}
			    } else {
				newColumn(new VectorColumn(colName, selClass),
					colIdx[0]);
			    }
			    colLSM.addSelectionInterval(colIdx[0], colIdx[0]);
			    colIdx[0]++;
			    columnName.setText("");
			}
			// reset value so we can enter a new column
			optionPane.setValue(null);
			return;
		    } else if (optVal == options[1]) {
			dialog.setVisible(false);
		    }
		}
	    }
	});
	dialog.pack();
	dialog.setLocationRelativeTo(this);
	dialog.setVisible(true);
    }

    public void newColumn(VirtualColumn column, int columnIndex) {
	vtm.addColumn(column, columnIndex);
    }

    public void newColumn(TableModelFormula column, int columnIndex) {
	vtm.addColumn(column, columnIndex);
    }

    private void setTableName() {
	String name = tableName.getText();
	if (vtm != null) {
	    vtm.setName(name);
	}
	Container c = getTopLevelAncestor();
	if (c instanceof Frame) {
	    ((Frame) c).setTitle("Edit " + name);
	}
    }

    public VirtualTableModel getTableModel() {
	return vtm;
    }

    public void setPreferredViewableRows(int rowNumber) {
	Dimension dim = table.getPreferredScrollableViewportSize();
	dim.height = rowNumber * table.getRowHeight();
	table.setPreferredScrollableViewportSize(dim);
    }

    /**
     * Display a VirtualTableModelView usage: java
     * edu.umn.genomics.table.VirtualTableModelView
     * 
     * @see FileTableModel
     */
    public static void main(String[] args) {
	JFrame frame = new JFrame("VirtualTableModelView");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	String[] columnNames = { "First Name", "Last Name", "Sport",
		"# of Years", "Vegetarian" };
	Object[][] data = {
		{ "Mary", "Campione", "Snowboarding", new Integer(5),
			new Boolean(false) },
		{ "Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
		{ "Kathy", "Walrath", "Knitting", new Integer(2),
			new Boolean(false) },
		{ "Sharon", "Zakhour", "Speed reading", new Integer(20),
			new Boolean(true) },
		{ "Philip", "Milne", "Pool", new Integer(10),
			new Boolean(false) } };
	DefaultTableModel dtm = new DefaultTableModel(data, columnNames);
	VirtualTableModel vtm = new VirtualTableModelProxy(dtm);
	VirtualTableModelView tableEdit = new VirtualTableModelView(vtm);
	frame.getContentPane().add(tableEdit);
	frame.pack();
	frame.setVisible(true);
    }
}
