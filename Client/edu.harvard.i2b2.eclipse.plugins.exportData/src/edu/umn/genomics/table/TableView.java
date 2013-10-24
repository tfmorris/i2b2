/*
 * @(#) $RCSfile: TableView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.prefs.*; // import java.awt.print.*; //PrintJob
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import edu.umn.genomics.bi.dbutil.*;
import edu.umn.genomics.file.OpenInputSource;

// interfaces:
// CellMap.java: public interface CellMap {
// SelectableCellMap.java: public interface SelectableCellMap extends CellMap {
// ColumnMap.java: public interface ColumnMap extends SelectableCellMap {
// VirtualCell.java: public interface VirtualCell {
// VirtualColumn.java: public interface VirtualColumn extends VirtualCell {
// TableModelFormula.java: public interface TableModelFormula extends
// VirtualCell {
// IndexMap.java: public interface IndexMap {
// SetOperator.java: public interface SetOperator {
// TableContext.java: public interface TableContext {
// TableModelView.java: public interface TableModelView {
// TableSourceMap.java: public interface TableSourceMap {
// VirtualTableModel.java: public interface VirtualTableModel extends TableModel
// {
// possible redesign interfaces:
// ColorArray.java: public interface ColorArray {
// CoordinateArray.java: public interface CoordinateArray {
// CoordinateMap.java: public interface CoordinateMap {

/**
 * TableView displays the values of a table in several complementary views. The
 * views provide a means to select a set of rows of the table that are of
 * interest. The data tables may be read from a file or queried from a data
 * base. The TableModel and the ListSelectionModel are shared by all the views.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */

@SuppressWarnings("serial")
public class TableView extends JPanel implements Serializable {

    public static final String _revisionId = "$Id: TableView.java,v 1.3 2008/10/31 17:43:22 wp066 Exp $";

    // are we running with Java2:
    boolean j2available = System.getProperty("java.specification.version")
	    .compareTo("1.2") >= 0;

    DefaultTableContext ctx = new DefaultTableContext();

    TreeModel tree = ctx.getTreeModel();

    JTree jtr = new JTree(tree);

    class JTRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree, Object value,
		boolean sel, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
	    super.getTreeCellRendererComponent(tree, value, sel, expanded,
		    leaf, row, hasFocus);
	    if (value != null && value instanceof DefaultMutableTreeNode) {
		setIcon(ctx.getViewIcon((DefaultMutableTreeNode) value));
	    }
	    return this;
	}
    };

    TreeSelectionListener tsl = new TreeSelectionListener() {
	public void valueChanged(TreeSelectionEvent e) {
	    try {
		final Object o = ((DefaultMutableTreeNode) e.getPath()
			.getLastPathComponent()).getUserObject();
		java.awt.EventQueue.invokeLater(new Runnable() {
		    public void run() {
			treeSelect(o);
		    }
		});
	    } catch (Exception ex) {
	    }
	}
    };

    TreeModelListener tml = new TreeModelListener() {
	public void treeNodesChanged(TreeModelEvent e) {
	}

	public synchronized void treeNodesInserted(TreeModelEvent e) {
	    // Expand tree and set selection
	    // Use SwingUtilities.invokeLater to do this in the GUI thread.
	    final TreePath tp = e.getTreePath().pathByAddingChild(
		    e.getChildren()[0]);
	    Runnable selectNewNode = new Runnable() {
		TreePath treePath = tp;

		public void run() {
		    // jtr.setSelectionPath(null);
		    // jtr.setSelectionPath(treePath);
		    jtr.scrollPathToVisible(treePath);
		    // jtr.repaint();
		}
	    };
	    SwingUtilities.invokeLater(selectNewNode);
	}

	public synchronized void treeNodesRemoved(TreeModelEvent e) {
	    // Select Item nearest to the deleted item
	    // Use SwingUtilities.invokeLater to do this in the GUI thread.
	    final TreePath tp = e.getTreePath();
	    final int[] ci = e.getChildIndices();
	    Runnable selectNextNode = new Runnable() {
		TreePath treePath = tp;

		int[] idx = ci;

		public void run() {
		    TreeNode pn = (TreeNode) treePath.getLastPathComponent();
		    if (idx != null && idx.length > 0 && pn != null
			    && pn.getChildCount() > 0) {
			pn.getChildCount();
			if (idx[0] > 0) {
			    jtr.setSelectionPath(treePath.pathByAddingChild(pn
				    .getChildAt(idx[0] - 1)));
			} else {
			    jtr.setSelectionPath(treePath.pathByAddingChild(pn
				    .getChildAt(0)));
			}
		    } else {
			jtr.setSelectionPath(treePath);
			jtr.scrollPathToVisible(treePath);
		    }
		    jtr.repaint();
		}
	    };
	    SwingUtilities.invokeLater(selectNextNode);
	}

	public synchronized void treeStructureChanged(TreeModelEvent e) {
	    jtr.repaint();
	}
    };

    // Entry panels
    final JFileChooser fc = new JFileChooser();

    LoadTable loadTable = null;

    JTextArea queryEntry = null;

    JTextArea selectedText = null;

    JSPanel scriptPanel = null;

    JFrame scriptFrame = null;

    BshPanel bshScriptPanel = null;

    JFrame bshScriptFrame = null;

    // Views of the tablemodel

    SetOperatorPanel setOpPanel = new SetOperatorPanel();

    // Main panel Buttons
    JButton jColBtn = new JButton("Columns");

    // Query panel buttons
    JButton dbconfigBtn = new JButton("Configure Connection");

    JButton submitBtn = new JButton("Submit Query");

    JButton cancelBtn = new JButton("Cancel");

    JButton closeBtn = new JButton("Close");

    // Threads
    Thread queryThread;

    Thread theThread;

    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    displayTableModelView(e.getActionCommand());
	}
    };

    private void treeSelect(Object o) {
	if (o instanceof JFrame) {
	    JFrame jf = (JFrame) o;
	    jf.setExtendedState(jf.getExtendedState() & (~Frame.ICONIFIED));
	    jf.setVisible(false);
	    jf.setVisible(true);
	    jf.toFront();
	} else if (o instanceof TableModel) {
	    // System.err.println("Select " + o.toString());
	}
    }

    @SuppressWarnings("unchecked")
    private Map getScriptVars() {
	Hashtable vars = new Hashtable();
	vars.put("tableView", this);
	vars.put("tv", this);
	vars.put("tableContext", this.getTableContext());
	vars.put("ctx", this.getTableContext());
	vars.put("Cells", new Cells());
	return vars;
    }

    /*
     * private JSPanel getScriptPanel() throws IOException { Hashtable vars =
     * new Hashtable(); vars.put("tableView", this); vars.put("tv", this);
     * vars.put("tableContext", this.getTableContext()); vars.put("ctx",
     * this.getTableContext()); vars.put("Cells", new Cells()); JSPanel jsPnl =
     * new JSPanel(vars); return jsPnl; }
     */

    public void showJavaScriptWindow() {
	if (scriptFrame == null) {
	    try {
		scriptPanel = new JSPanel(getScriptVars());
		scriptFrame = new JFrame("JavaScript");
		scriptFrame.getContentPane().add(scriptPanel);
		scriptFrame.pack();
		scriptFrame.setVisible(true);
	    } catch (Exception ex) {
		System.err.println("ScriptPanel " + ex);
	    }
	}
	scriptFrame.setVisible(true);
    }

    public void showBshScriptWindow() {
	if (bshScriptFrame == null) {
	    try {
		bshScriptPanel = new BshPanel(getScriptVars());
		bshScriptFrame = new JFrame("BeanShell");
		bshScriptFrame.getContentPane().add(bshScriptPanel);
		bshScriptFrame.pack();
		bshScriptFrame.setVisible(true);
	    } catch (Exception ex) {
		System.err.println("ScriptPanel " + ex);
	    }
	}
	bshScriptFrame.setVisible(true);
    }

    /**
     * Create a TableView panel with a menubar for selecting data and views.
     */
    public TableView() {
	jtr.setCellRenderer(new JTRenderer());
	jtr.addTreeSelectionListener(tsl);
	jtr.getModel().addTreeModelListener(tml);
	jtr.setEditable(false);
	jtr.setDragEnabled(true);
	jtr.setShowsRootHandles(true);
	jtr.setExpandsSelectedPaths(true);
	jtr.setVisibleRowCount(10);

	JMenuBar mb = getJMenuBar();

	setLayout(new BorderLayout());

	add(mb, BorderLayout.NORTH);

	JPanel ppnl = this;
	JPanel pnl;
	JToolBar tb;

	if (true) {
	    pnl = new JPanel(new BorderLayout());
	    tb = getEditToolBar();
	    pnl.add(tb, BorderLayout.NORTH);
	    ppnl.add(pnl);
	    ppnl = pnl;
	}

	if (true) {
	    pnl = new JPanel(new BorderLayout());
	    tb = getViewToolBar();
	    pnl.add(tb, BorderLayout.NORTH);
	    ppnl.add(pnl);
	    ppnl = pnl;
	}

	JPanel setPanel = new JPanel(new BorderLayout());
	getSetToolBar();
	// setPanel.add(stb, BorderLayout.NORTH);
	ppnl.add(setPanel);

	JScrollPane jtrsp = new JScrollPane(jtr);
	setPanel.add(jtrsp, BorderLayout.CENTER);

    }

    /**
     * Create a TableView panel for the given TableModel.
     * 
     * @param tableModel
     *            the data model to view.
     */
    public TableView(TableModel tableModel) {
	this();
	setTableModel(tableModel, " ");
    }

    /**
     * Create a TableView panel for the given file containing a table of data.
     * 
     * @param filename
     *            the data table file to view.
     */
    public TableView(String filename) {
	this();
	setFile(filename);
    }

    /*
     * This also requires remaking menus and icons and posibbly adding new views
     * to the context. Defer this one for now.
     * 
     * public void setTableContext(TableContext context) { ctx = context; tree =
     * ctx.getTreeModel(); jtr.setModel(tree); }
     */

    /**
     * Return the TableContext from which an application can retrieve associated
     * the ListSelectionModel for a TableModel.
     * 
     * @return the TableContext.
     */
    public TableContext getTableContext() {
	return ctx;
    }

    private LoadTable getLoadTable() {
	if (loadTable == null) {
	    loadTable = new LoadTable();
	}
	return loadTable;
    }

    private JMenuBar getJMenuBar() {
	JMenuBar mb = new JMenuBar();
	JMenuItem mi;
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic('f');

	mi = (JMenuItem) fileMenu.add(new JMenuItem("load table"));
	mi.setMnemonic('l');
	mi.getAccessibleContext().setAccessibleDescription("load table");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {

		// TableView tableView = new TableView();
		// java.awt.EventQueue.invokeLater(new Runnable() {
		// public void run() {

		// VirtualTableModel model = setFile(
		// "C:\\tableview\\data\\patienttable.txt", "PatientData");
		// displayTableModelView("Histogram", model);
		// displayTableModelView("Histograms", model);
		// displayTableModelView("Table", model);
		// displayTableModelView("Histogram3D", model);
		// }
		// });

		// java.awt.EventQueue.invokeLater(new Runnable() {
		// public void run() {
		// model = setFile("C:\\tableview\\data\\dd.data", "ddData");
		// displayTableModelView("Histogram", model);
		// displayTableModelView("Histograms", model);
		// displayTableModelView("Table", model);
		// displayTableModelView("Histogram3D", model);
		// }
		// });

		LoadTable lt = getLoadTable();
		TableModel newtm = lt
			.openLoadTableDialog((Frame) getTopLevelAncestor());
		if (newtm != null) {
		    setTableModel(newtm, lt.getTableSource());
		}

	    }
	});

	mi = (JMenuItem) fileMenu.add(new JMenuItem("save table"));
	mi.setMnemonic('t');
	mi.getAccessibleContext().setAccessibleDescription("save table");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int returnVal = fc
			.showSaveDialog((Window) getTopLevelAncestor());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    try {
			// file.createNewFile();
			writeTable(new FileOutputStream(file));
		    } catch (Exception ex) {
			System.err.println("Unable to Save: "
				+ file.getAbsolutePath() + " " + ex);
		    }
		} else {
		    System.err.println("Save command cancelled by user.");
		}
	    }
	});

	mi = (JMenuItem) fileMenu.add(new JMenuItem("save selection"));
	mi.setMnemonic('s');
	mi.getAccessibleContext().setAccessibleDescription("save selection");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int returnVal = fc
			.showSaveDialog((Window) getTopLevelAncestor());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    try {
			// file.createNewFile();
			writeSelection(new FileOutputStream(file));
		    } catch (Exception ex) {
			System.err.println("Unable to Save: "
				+ file.getAbsolutePath() + " " + ex);
		    }
		} else {
		    System.err.println("Save command cancelled by user.");
		}
	    }
	});

	mi = (JMenuItem) fileMenu.add(new JMenuItem("output selection"));
	mi.setMnemonic('o');
	mi.getAccessibleContext().setAccessibleDescription("output selectiont");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		writeSelection(System.out);
	    }
	});
	/*
	 * //PrintJob if (j2available) { mi = (JMenuItem)fileMenu.add(new
	 * JMenuItem("print")); mi.addActionListener(new ActionListener() {
	 * public void actionPerformed(ActionEvent ae) { PrinterJob pj =
	 * PrinterJob.getPrinterJob(); pj.setPrintable(TableView.this);
	 * pj.pageDialog(pj.defaultPage()); if (pj.printDialog()) { try {
	 * pj.print(); } catch (PrinterException pe) { System.out.println(pe); }
	 * } } }); }
	 */

	/****
	 * mi = (JMenuItem) fileMenu.add(new JMenuItem("Exit"));
	 * mi.setMnemonic('x');
	 * mi.getAccessibleContext().setAccessibleDescription("Exit");
	 * mi.addActionListener(new ActionListener() { public void
	 * actionPerformed(ActionEvent e) { try { Container c =
	 * getTopLevelAncestor(); if (c instanceof Window) { ((Window)
	 * c).dispose(); } if (c instanceof JFrame && ((JFrame)
	 * c).getDefaultCloseOperation() == JFrame.EXIT_ON_CLOSE) {
	 * System.exit(0); } else { ctx.removeAllTableModels(); } } catch
	 * (Exception ex) { System.exit(0); } } });
	 ****/

	mb.add(fileMenu);
	JMenu editMenu = new JMenu("Edit");
	editMenu.setMnemonic('e');

	mi = (JMenuItem) editMenu.add(new JMenuItem("Edit Table Columns"));
	mi.setMnemonic('c');
	mi.getAccessibleContext()
		.setAccessibleDescription("Edit Table Columns");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		jColBtn.doClick();
	    }
	});

	JMenu defsort = new JMenu("Set Default Mapping Order");
	defsort.setMnemonic('o');
	ButtonGroup sortGrp = new ButtonGroup();
	ActionListener sortBtnL = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s.startsWith("N"))
		    DefaultColumnMap.defaultsortby = ColumnMap.NATURALSORT;
		else if (s.startsWith("A"))
		    DefaultColumnMap.defaultsortby = ColumnMap.ALPHANUMSORT;
		else if (s.startsWith("T"))
		    DefaultColumnMap.defaultsortby = ColumnMap.ROWORDERSORT;
	    }
	};
	mi = (JMenuItem) defsort.add(new JRadioButtonMenuItem("Natural Order"));
	mi.setMnemonic('n');
	mi.getAccessibleContext().setAccessibleDescription("Natural Order");
	mi.addActionListener(sortBtnL);
	mi.setSelected(DefaultColumnMap.defaultsortby == ColumnMap.NATURALSORT);
	sortGrp.add(mi);
	mi = (JMenuItem) defsort.add(new JRadioButtonMenuItem(
		"AlphaNumeric Order"));
	mi.setMnemonic('a');
	mi.getAccessibleContext()
		.setAccessibleDescription("AlphaNumeric Order");
	mi.addActionListener(sortBtnL);
	mi
		.setSelected(DefaultColumnMap.defaultsortby == ColumnMap.ALPHANUMSORT);
	sortGrp.add(mi);
	mi = (JMenuItem) defsort
		.add(new JRadioButtonMenuItem("Table Row Order"));
	mi.setMnemonic('t');
	mi.getAccessibleContext().setAccessibleDescription("Table Row Order");
	mi.addActionListener(sortBtnL);
	mi
		.setSelected(DefaultColumnMap.defaultsortby == ColumnMap.ROWORDERSORT);
	sortGrp.add(mi);
	editMenu.add(defsort);

	mi = (JMenuItem) editMenu.add(new JMenuItem(
		"Create Table from Selected Rows"));
	mi.setMnemonic('s');
	mi.getAccessibleContext().setAccessibleDescription(
		"Create Table from Selected Rows");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		selectedSubsetTable();
	    }
	});

	mi = (JMenuItem) editMenu.add(new JMenuItem("Delete item"));
	mi.setMnemonic('d');
	mi.getAccessibleContext().setAccessibleDescription("Delete");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		deleteNode();
	    }
	});

	mi = (JMenuItem) editMenu.add(new JMenuItem("JavaScript Window"));
	mi.setMnemonic('j');
	mi.getAccessibleContext().setAccessibleDescription("JavaScript");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		showJavaScriptWindow();
	    }
	});

	mi = (JMenuItem) editMenu.add(new JMenuItem("BeanShell Window"));
	mi.setMnemonic('j');
	mi.getAccessibleContext().setAccessibleDescription("BeanShell");
	mi.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		showBshScriptWindow();
	    }
	});

	mb.add(editMenu);
	JMenu viewMenu = new JMenu("View");
	viewMenu.setMnemonic('v');
	String vn[] = ctx.getViewNames();
	for (int i = 0; i < vn.length; i++) {
	    mi = (JMenuItem) viewMenu.add(new JMenuItem(vn[i]));
	    mi.setActionCommand(vn[i]);
	    // mi.setMnemonic('t');
	    // mi.getAccessibleContext().setAccessibleDescription("Table");
	    mi.addActionListener(al);
	}

	mb.add(viewMenu);
	return mb;
    }

    /**
     * Return a toolbar with selection set operator choices
     */
    private JToolBar getSetToolBar() {
	JToolBar tb = new JToolBar();
	JButton sClear = new JButton("Clear");
	sClear.setMargin(new Insets(1, 1, 1, 1));
	sClear.setBackground(Color.red);
	sClear.setToolTipText("Clear selection");
	sClear.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		TableModel tm = getTableModel();
		if (tm != null) {
		    ListSelectionModel lsm = ctx.getRowSelectionModel(tm);
		    if (lsm != null) {
			lsm.clearSelection();
		    }
		}
	    }
	});

	JButton sAll = new JButton("All");
	sAll.setMargin(new Insets(1, 1, 1, 1));
	sAll.setToolTipText("Select All");
	sAll.setBackground(Color.cyan);
	sAll.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		TableModel tm = getTableModel();
		if (tm != null) {
		    ListSelectionModel lsm = ctx.getRowSelectionModel(tm);
		    if (lsm != null) {
			int end = tm.getRowCount() - 1;
			lsm.setSelectionInterval(0, end);
		    }
		}
	    }
	});

	JButton sInvrt = new JButton("Invert");
	sInvrt.setMargin(new Insets(1, 1, 1, 1));
	sInvrt.setToolTipText("Invert Selection");
	sInvrt.setBackground(Color.cyan);
	sInvrt.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		TableModel tm = getTableModel();
		if (tm != null) {
		    ListSelectionModel lsm = ctx.getRowSelectionModel(tm);
		    if (lsm != null) {
			int end = tm.getRowCount() - 1;
			if (lsm.getMinSelectionIndex() < 0) {
			    lsm.setSelectionInterval(0, end);
			} else {
			    int min = lsm.getMinSelectionIndex();
			    int max = lsm.getMaxSelectionIndex();
			    lsm.setValueIsAdjusting(true);
			    if (min > 0) {
				lsm.addSelectionInterval(0, min - 1);
			    }
			    if (max < end) {
				lsm.addSelectionInterval(max + 1, end);
			    }
			    for (int i = min; i <= max; i++) {
				if (lsm.isSelectedIndex(i)) {
				    lsm.removeSelectionInterval(i, i);
				} else {
				    lsm.addSelectionInterval(i, i);
				}
			    }
			    lsm.setValueIsAdjusting(false);
			}
		    }
		}
	    }
	});

	tb.add(sClear);
	tb.add(sAll);
	tb.add(sInvrt);
	Component[] c = setOpPanel.getComponents();
	for (int i = 0; i < c.length; i++) {
	    tb.add(c[i]);
	}
	return tb;
    }

    private void openScratchPad() {
	JTextArea text = new JTextArea();
	JScrollPane jsp = new JScrollPane(text);
	JFrame frame = ctx.getViewFrame("Scratch Pad", jsp);
	DefaultTableContext.setViewToolBar(frame, text);
	frame.setVisible(true);
    }

    /**
     * Open a view on the current TableModel.
     * 
     * @param viewName
     *            The name of the view as it is known to the TableContext.
     * @return The frame that is displaying the view.
     * @see #getTableModel
     * @see #getTableContext
     */
    public JFrame displayTableModelView(String viewName) {
	TableModel tm = getTableModel();
	if (tm != null) {
	    return ctx.getTableModelView(tm, viewName);
	} else {
	    JOptionPane.showMessageDialog(this, "There are no tables to view.");
	}
	return null;
    };

    /**
     * Open a view on the current TableModel.
     * 
     * @param viewName
     *            The name of the view as it is known to the TableContext.
     * @param tm
     *            The table model for the parent tree node.
     * 
     * @return The frame that is displaying the view.
     * @see #getTableModel
     * @see #getTableContext
     */
    public JFrame displayTableModelView(String viewName, TableModel tm) {

	if (tm != null) {
	    return ctx.getTableModelView(tm, viewName);
	} else {
	    JOptionPane.showMessageDialog(this, "There are no tables to view.");
	}
	return null;
    };

    private JToolBar getEditToolBar() {
	JToolBar tb = new JToolBar();
	JButton btn;
	ClassLoader cl = this.getClass().getClassLoader();

	// ImageIcon icon = null;
	ImageIcon icon = null;
	String buttonName = "";

	icon = new ImageIcon(cl
		.getResource("edu/umn/genomics/table/Icons/Import24.gif"));
	buttonName = icon == null ? "Load" : null;
	btn = new JButton(buttonName, icon);
	btn.setMargin(new Insets(0, 0, 0, 0));
	btn.setMnemonic('l');
	btn.getAccessibleContext().setAccessibleDescription("Load a table");
	btn.setToolTipText("Load a table");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		LoadTable lt = getLoadTable();
		TableModel newtm = lt
			.openLoadTableDialog((Frame) getTopLevelAncestor());
		if (newtm != null) {
		    setTableModel(newtm, lt.getTableSource());
		}
	    }
	});
	tb.add(btn);

	icon = new ImageIcon(cl
		.getResource("edu/umn/genomics/table/Icons/SaveAs24.gif"));
	buttonName = icon == null ? "Save" : null;
	btn = new JButton(buttonName, icon);
	btn.setMargin(new Insets(0, 0, 0, 0));
	btn.setMnemonic('s');
	btn.getAccessibleContext().setAccessibleDescription("Save a table");
	btn.setToolTipText("Save a table");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int returnVal = fc
			.showSaveDialog((Window) getTopLevelAncestor());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    try {
			// file.createNewFile();
			writeSelection(new FileOutputStream(file));
		    } catch (Exception ex) {
			System.err.println("Unable to Save: "
				+ file.getAbsolutePath() + " " + ex);
		    }
		} else {
		    System.err.println("Save command cancelled by user.");
		}
	    }
	});
	tb.add(btn);

	tb.addSeparator();
	icon = new ImageIcon(cl
		.getResource("edu/umn/genomics/table/Icons/subtable24.gif"));
	buttonName = icon == null ? "Sel" : null;
	btn = new JButton(buttonName, icon);
	btn.setMargin(new Insets(0, 0, 0, 0));
	btn.setMnemonic('s');
	btn.getAccessibleContext().setAccessibleDescription(
		"Create Table from Selected Rows");
	btn.setToolTipText("Create Table from Selected Rows");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		selectedSubsetTable();
	    }
	});
	tb.add(btn);

	icon = new ImageIcon(cl
		.getResource("edu/umn/genomics/table/Icons/TableEdit24.gif"));
	buttonName = icon == null ? "Edit" : null;
	btn = new JButton(buttonName, icon);
	btn.setMargin(new Insets(0, 0, 0, 0));
	btn.setMnemonic('e');
	btn.getAccessibleContext().setAccessibleDescription(
		"Edit Table Columns");
	btn.setToolTipText("Edit Table Columns");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		jColBtn.doClick();
	    }
	});
	tb.add(btn);

	tb.addSeparator();
	icon = new ImageIcon(cl
		.getResource("edu/umn/genomics/table/Icons/postit24.gif"));
	buttonName = icon == null ? "Scratch Pad" : null;
	btn = new JButton(buttonName, icon);
	btn.setMargin(new Insets(0, 0, 0, 0));
	btn.setMnemonic('n');
	btn.getAccessibleContext().setAccessibleDescription(
		"Open Scratch Pad Window");
	btn.setToolTipText("Open Scratch Pad Window");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		openScratchPad();
	    }
	});
	tb.add(btn);

	tb.addSeparator();
	icon = null;
	icon = new ImageIcon(cl
		.getResource("edu/umn/genomics/table/Icons/Remove24.gif"));
	buttonName = icon == null ? "Del" : null;
	btn = new JButton(buttonName, icon);
	btn.setMargin(new Insets(0, 0, 0, 0));
	btn.setMnemonic('d');
	btn.getAccessibleContext().setAccessibleDescription("Delete");
	btn.setToolTipText("Delete the selected item");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		deleteNode();
	    }
	});
	tb.add(btn);

	return tb;
    }

    private JToolBar getViewToolBar() {
	JToolBar tb = new JToolBar();
	jColBtn = new JButton("Columns");
	jColBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		TableModel tm = getTableModel();
		if (tm != null) {
		    ctx.getEditorFrame(tm);
		}
	    }
	});

	if (true) {
	    String vn[] = ctx.getViewNames();
	    for (int i = 0; i < vn.length; i++) {
		Icon icon = (Icon) ctx.getViewIcon(vn[i]);
		JButton jBtn = new JButton(icon);
		jBtn.setMargin(new Insets(0, 0, 0, 0));
		jBtn.setToolTipText(vn[i]);
		jBtn.setActionCommand(vn[i]);
		jBtn.addActionListener(al);
		tb.add(jBtn);
	    }
	}
	return tb;
    }

    /**
     * Enter a data base selection query to generate a TableModel.
     * 
     * @param dbuser
     *            the database account to query
     * @param query
     *            the query statement to produce the TableModel
     * @return the VirtualTableModel created for this data source
     */
    public VirtualTableModel setQuery(DBConnectParams dbuser, String query) {
	return setQuery(dbuser, query, null);
    }

    /**
     * Enter a data base selection query to generate a TableModel.
     * 
     * @param dbuser
     *            the database account to query
     * @param query
     *            the query statement to produce the TableModel
     * @param alias
     *            if not null, the display name for this table
     * @return the VirtualTableModel created for this data source
     */
    public VirtualTableModel setQuery(DBConnectParams dbuser, String query,
	    String alias) {
	VirtualTableModel vtm = null;
	try {
	    if (dbuser == null) {
		throw new Exception("No data base account for query");
	    }
	    if (query == null) {
		throw new NullPointerException("No query given.");
	    }
	    JDBCTableModel jdbctm = new JDBCTableModel(dbuser);
	    vtm = setTableModel(jdbctm, alias != null ? alias : query);
	    StringTokenizer st = new StringTokenizer(query);
	    if (st.countTokens() >= 4
		    && query.toUpperCase().indexOf("SELECT") >= 0) { // select
		// *
		// from
		// table
		jdbctm.setQuery(query);
	    } else {
		BufferedReader rd = OpenInputSource.getBufferedReader(query);
		StringBuffer sb = new StringBuffer();
		for (String line = rd.readLine(); line != null; line = rd
			.readLine()) {
		    sb.append(line).append("\n");
		}
		jdbctm.setQuery(sb.toString());
	    }
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(this, ex, "Unable to execute query "
		    + query, JOptionPane.ERROR_MESSAGE);
	}
	return vtm;
    }

    /**
     * Enter a filename that contains a table of data.
     * 
     * @param file
     *            the name of the file to load
     * @return the VirtualTableModel created for this data source
     */
    public VirtualTableModel setFile(String file) {
	return setFile(file, null);
    }

    /**
     * Enter a filename that contains a table of data.
     * 
     * @param file
     *            the name of the file to load
     * @param alias
     *            if not null, the display name for this table
     * @return the VirtualTableModel created for this data source
     */
    public VirtualTableModel setFile(String file, String alias) {
	VirtualTableModel vtm = null;
	try {
	    FileTableModel ftm = new FileTableModel(file);
	    vtm = setTableModel(ftm, alias != null ? alias : file);
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(this, ex, "Unable to load " + file,
		    JOptionPane.ERROR_MESSAGE);
	}
	return vtm;
    }

    /**
     * Load the table data from the tableSource using the table loader.
     * 
     * @param tableLoader
     *            The the table loader instance that can read the data format of
     *            the tableSource.
     * @param tableSource
     *            The file or URL that is the source of table data.
     * @return The TableModel that accesses the tableSource.
     */
    public TableModel loadSource(TableSource tableLoader, String tableSource,
	    String alias) throws IOException {
	VirtualTableModel vtm = null;
	if (tableLoader == null) {
	    return setFile(tableSource, alias);
	} else if (tableLoader instanceof OpenTableSource) {
	    ((OpenTableSource) tableLoader).openTableSource(tableSource);
	    vtm = setTableModel(tableLoader.getTableModel(),
		    alias != null ? alias : tableLoader.getTableSource());
	}
	return vtm;
    }

    /**
     * Load the table data from the tableSource using the named table loader.
     * 
     * @param tableLoaderName
     *            The name of the table loader that can read the data format of
     *            the tableSource.
     * @param tableSource
     *            The file or URL that is the source of table data.
     * @return The TableModel that accesses the tableSource.
     */
    public TableModel loadSource(String tableLoaderName, String tableSource,
	    String alias) throws IOException, NullPointerException {
	if (tableLoaderName == null) {
	    return setFile(tableSource, alias);
	}
	TableSource ts = getLoadTable().getTableLoader(tableLoaderName);
	if (ts == null) {
	    throw new NullPointerException("No Table Loader by the name "
		    + tableLoaderName);
	}
	return loadSource(ts, tableSource, alias);
    }

    /**
     * Create a MergeTableModel from the list of TableModels.
     * 
     * @param tableModels
     *            The TableModels to merge.
     * @return The MergeTableModel.
     * @see MergeTableModel
     */
    @SuppressWarnings("unchecked")
    public MergeTableModel mergeTables(java.util.List tableModels)
	    throws IOException {
	return mergeTables(tableModels, null);
    }

    /**
     * Create a MergeTableModel from the list of TableModels.
     * 
     * @param tableModels
     *            The TableModels to merge.
     * @param displayName
     *            The display name for the resulting MergeTableModel.
     * @return The MergeTableModel.
     * @see MergeTableModel
     */
    @SuppressWarnings("unchecked")
    public MergeTableModel mergeTables(java.util.List tableModels,
	    String displayName) throws IOException {
	MergeTableModel mtm = new MergeTableModel();
	String name = "";
	int n = 0;
	for (ListIterator i = tableModels.listIterator(); i.hasNext();) {
	    Object obj = i.next();
	    if (obj instanceof TableModel) {
		VirtualTableModel vtm = null;
		try {
		    // Get the VirtualTableModel from the TableContext so that
		    // it records the dependence on tableModel.
		    vtm = ctx.getVirtualTableModel((TableModel) obj);
		    mtm.addTableModel(vtm);
		    name += (n++ > 0 ? ", " : "") + vtm.toString();
		} catch (Exception ex) {
		    System.err.println("mergeTables " + ex);
		    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex,
			    "Unable to merge table "
				    + (vtm != null ? vtm.toString() : ""),
			    JOptionPane.ERROR_MESSAGE);
		}
	    } else {
	    }
	}
	setTableModel(mtm, displayName != null ? displayName : name);
	return mtm;
    }

    /**
     * Sets tableModel as the data model.
     * 
     * @param tableModel
     *            the data model for the table.
     * @param name
     *            the name identifying the table.
     * @return the VirtualTableModel that references the given TableModel in the
     *         TableContext.
     */
    public VirtualTableModel setTableModel(TableModel tableModel, String name) {
	if (tableModel == null) {
	    return null;
	}
	// Get the VirtualTableModel from the TableContext so that it records
	// the dependence on tableModel.
	VirtualTableModel vtm = ctx.getVirtualTableModel(tableModel);
	vtm.setName(name);
	ctx.addTableModel(vtm);
	setOpPanel.addSetOperator(ctx.getSetOperator(vtm));
	return vtm;
    }

    /**
     * Return the table model being displayed.
     * 
     * @return the table being displayed.
     */
    @SuppressWarnings("unchecked")
    public TableModel getTableModel() {
	DefaultMutableTreeNode tn = (DefaultMutableTreeNode) jtr
		.getLastSelectedPathComponent();
	for (DefaultMutableTreeNode n = tn; n != null; n = (DefaultMutableTreeNode) n
		.getParent()) {
	    Object o = n.getUserObject();
	    if (o instanceof TableModel) {
		return (TableModel) o;
	    }
	}
	for (Enumeration e = ((DefaultMutableTreeNode) jtr.getModel().getRoot())
		.breadthFirstEnumeration(); e.hasMoreElements();) {
	    DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
	    Object o = n.getUserObject();
	    if (o instanceof TableModel) {
		jtr.getSelectionModel().setSelectionPath(
			new TreePath(n.getPath()));
		return (TableModel) o;
	    }
	}
	return null;
    }

    /**
     * Return the selected view.
     * 
     * @return the view that is selected, or null if the selection is not a
     *         view.
     */
    public TableModelView getSelectedView() {
	return ctx.getTableModelView((DefaultMutableTreeNode) jtr
		.getLastSelectedPathComponent());
    }

    /**
     * Select the given TableModel as the focus for subsequent actions.
     * 
     * @param tm
     *            the TableModel to select.
     * @return the table being displayed.
     */
    @SuppressWarnings("unchecked")
    public void selectTableModel(TableModel tm) {
	for (Enumeration e = ((DefaultMutableTreeNode) jtr.getModel().getRoot())
		.breadthFirstEnumeration(); e.hasMoreElements();) {
	    DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
	    Object o = n.getUserObject();
	    if (o instanceof TableModel && o == tm) {
		jtr.getSelectionModel().setSelectionPath(
			new TreePath(n.getPath()));
		return;
	    }
	}
    }

    /**
     * Select the given table source as the focus for subsequent actions.
     * 
     * @param tableSource
     *            The source name for the table to select.
     * @return the TableModel for this table source, or null if it wasn't found.
     */
    public TableModel selectTableSource(String tableSource) {
	TableModel tm = getTableModelForSource(tableSource);
	selectTableModel(tm);
	return tm;
    }

    /**
     * Return the TableModel associated with the given table source.
     * 
     * @param tableSource
     *            The source name for the table.
     * @return the TableModel for this table source, or null if it wasn't found.
     */
    public TableModel getTableModelForSource(String tableSource) {
	if (tableSource != null) {
	    TableModel[] tm = ctx.getTableModels();
	    for (int i = 0; i < tm.length; i++) {
		if (tableSource.equals(tm[i].toString())) {
		    return tm[i];
		}
	    }
	    // Compare with Unix Shell tilde expanded file names.
	    if (tableSource.startsWith("~")) {
		String tsName = tableSource.substring(1);
		for (int i = 0; i < tm.length; i++) {
		    if (tm[i].toString().endsWith(tsName)) {
			return tm[i];
		    }
		}
	    }
	}
	return null;
    }

    private void selectedSubsetTable() {
	TableModel ctm = getTableModel();
	if (ctm == null) {
	    JOptionPane.showMessageDialog(this, "No table selected",
		    "Unable to create subtable", JOptionPane.ERROR_MESSAGE);
	} else {
	    if (ctx.getRowSelectionModel(ctm).isSelectionEmpty()) {
		JOptionPane.showMessageDialog(this,
			"No table rows are selected",
			"Unable to create subtable", JOptionPane.ERROR_MESSAGE);
	    } else {
		/* TableModel stm = */ctx.getTableModel(ctm, ctx
			.getRowSelectionModel(ctm));
	    }
	}
    }

    private void deleteNode() {
	deleteNode((DefaultMutableTreeNode) jtr.getLastSelectedPathComponent());
    }

    private void deleteNode(DefaultMutableTreeNode tn) {
	if (tn == null)
	    return;
	Object obj = tn.getUserObject();
	if (obj != null) {
	    if (obj instanceof JFrame) {
		((JFrame) obj).dispose();
	    } else if (obj instanceof TableModel) {
		ctx.removeTableModel((TableModel) obj);
	    }
	}
    }

    // save table method
    private void writeTable(OutputStream out) {
	TableModel tm = getTableModel();
	if (tm == null) {
	    return;
	}

	ListSelectionModel lsm = ctx.getRowSelectionModel(tm);
	PrintWriter prStr = new PrintWriter(out, true);
	int ncol = tm.getColumnCount();
	for (int c = 0; c < ncol; c++) {
	    // String name = "\"" + tm.getColumnName(c) + "\"";
	    String name = tm.getColumnName(c);
	    prStr.print((name != null && name.trim().length() > 0 ? name
		    : "column" + c)
		    + "\t");
	}
	prStr.print("\n");
	if (lsm != null) {
	    int min = 0;
	    int max = tm.getRowCount() - 1;
	    if (min >= 0) {
		for (int r = min; r <= max; r++) {
		    // if (lsm.isSelectedIndex(r)) {
		    for (int c = 0; c < ncol; c++) {
			Object o = tm.getValueAt(r, c);
			String s = o != null ? o.toString() : null;
			// if (s != null &&
			// (s.indexOf(' ') >= 0 || // space char
			// s.indexOf(' ') >= 0)) { // tab char
			// s = "\"" + s + "\"";
			// }
			prStr.print(s + "\t");
		    }
		    prStr.print("\n");
		    // }
		}
	    }
	}
	prStr.flush();
    }

    private void writeSelection(OutputStream out) {
	TableModel tm = getTableModel();
	if (tm == null) {
	    return;
	}

	ListSelectionModel lsm = ctx.getRowSelectionModel(tm);
	PrintWriter prStr = new PrintWriter(out, true);
	int ncol = tm.getColumnCount();
	for (int c = 0; c < ncol; c++) {
	    // String name = "\"" + tm.getColumnName(c) + "\"";
	    String name = tm.getColumnName(c);
	    prStr.print((name != null && name.trim().length() > 0 ? name
		    : "column" + c)
		    + "\t");
	}
	prStr.print("\n");
	if (lsm != null) {
	    int min = lsm.getMinSelectionIndex();
	    int max = lsm.getMaxSelectionIndex();
	    if (min >= 0) {
		for (int r = min; r <= max; r++) {
		    if (lsm.isSelectedIndex(r)) {
			for (int c = 0; c < ncol; c++) {
			    Object o = tm.getValueAt(r, c);
			    String s = o != null ? o.toString() : null;
			    // if (s != null &&
			    // (s.indexOf(' ') >= 0 || // space char
			    // s.indexOf(' ') >= 0)) { // tab char
			    // s = "\"" + s + "\"";
			    // }
			    prStr.print(s + "\t");
			}
			prStr.print("\n");
		    }
		}
	    }
	}
	prStr.flush();
    }

    /*
     * //PrintJob public int print(Graphics g, PageFormat pf, int pageIndex) {
     * if (pageIndex != 0) return NO_SUCH_PAGE; Graphics2D g2 = (Graphics2D)g;
     * g2.translate(pf.getImageableX(), pf.getImageableY()); paint(g2); return
     * PAGE_EXISTS; }
     */

    /**
     * Parse geometry strings and sets the frame bounds accordingly.
     * widthxheight+xoffset+yoffset Acceptable geometry strings: 500x400+100+100
     * 500x400-100+100 500x400-100-100 500x400 +100+100
     */
    private static void setFrameBounds(JFrame jf, String geom) {
	if (jf == null || geom == null || geom.length() < 1) {
	    return;
	}
	try {
	    Rectangle bnds = jf.getBounds();
	    int ti = geom.indexOf('x');
	    int[] sep = new int[2];
	    sep[0] = -1;
	    sep[1] = -1;
	    for (int i = 0, j = 0; i < geom.length() && j < 2; i++) {
		if (geom.charAt(i) == '+' || geom.charAt(i) == '-') {
		    sep[j++] = i;
		}
	    }
	    int xi = sep[0];
	    int yi = sep[1];
	    if (ti > 0) {
		String ws = geom.substring(0, ti);
		int w = Integer.parseInt(ws);
		bnds.width = w;
	    }
	    if (ti >= 0) {
		String hs = xi < 0 ? geom.substring(ti + 1) : geom.substring(
			ti + 1, xi);
		int h = Integer.parseInt(hs);
		bnds.height = h;
	    }
	    if (xi >= 0) {
		String xs = yi > xi ? geom.substring(xi + 1, yi) : geom
			.substring(xi + 1);
		int x = Integer.parseInt(xs);
		bnds.x = geom.charAt(xi) == '+' ? x : Toolkit
			.getDefaultToolkit().getScreenSize().width
			- x;
	    }
	    if (yi > xi) {
		String ys = geom.substring(yi + 1);
		int y = Integer.parseInt(ys);
		bnds.y = y;
		bnds.y = geom.charAt(yi) == '+' ? y : Toolkit
			.getDefaultToolkit().getScreenSize().height
			- y;
	    }
	    jf.setBounds(bnds);
	} catch (Exception ex) {
	    System.err.println("Error parsing screen location for "
		    + jf.getTitle() + " " + ex);
	}
    }

    private void setViewColumns(TableModelView view, String[] cols) {
	try {
	    int[] colIdx = new int[cols.length];
	    for (int i = 0; i < cols.length; i++) {
		colIdx[i] = Integer.parseInt(cols[i]);
	    }
	    view.setColumns(colIdx);
	} catch (Exception ex) {
	    view.setColumns(cols);
	}
    }

    /**
     * Open a view on a table as described in the viewDescription:
     * viewName(tableSource)@geometry where '(tableSource)' and '@geometry' are
     * optional in the description.
     * 
     * @param viewDescription
     *            The view description.
     * @return the view Component.
     */
    @SuppressWarnings("unchecked")
    public JComponent displayView(String viewDescription) {
	JComponent jc = null;
	String viewDesc = viewDescription.trim();
	String colList = null;
	// Is there a column list
	int cbi = viewDesc.indexOf('[');
	int cei = viewDesc.indexOf(']');
	if (cbi > 0 && cei > cbi) {
	    colList = viewDesc.substring(cbi + 1, cei).trim();
	    viewDesc = viewDesc.substring(0, cbi)
		    + (cei + 1 < viewDesc.length() ? viewDesc
			    .substring(cei + 1) : "");
	}

	// Is there a data source String
	cbi = viewDesc.indexOf('(');
	cei = viewDesc.indexOf(')');
	if (cbi > 0 && cei > cbi) {
	    String dataSource = viewDesc.substring(cbi + 1, cei).trim();
	    viewDesc = viewDesc.substring(0, cbi)
		    + (cei + 1 < viewDesc.length() ? viewDesc
			    .substring(cei + 1) : "");
	    TableModel tm = selectTableSource(dataSource);
	    if (tm == null) {
		System.err.println(dataSource + " was not found in:");
		TableModel[] tma = ctx.getTableModels();
		for (int i = 0; i < tma.length; i++) {
		    System.err.println("  " + tma[i].toString());
		}
	    }
	}
	int atIdx = viewDesc.indexOf('@');
	String viewName = atIdx < 0 ? viewDesc.trim() : viewDesc.substring(0,
		atIdx).trim();
	TableModel tm = getTableModel();
	if (tm != null) {
	    jc = ctx.getView(tm, viewName);
	    if (jc != null) {
		if (colList != null && jc instanceof TableModelView) {
		    // String[] cols = colList.split(",");
		    String[] cols;
		    if (true) { // Use until we no longer support version prior
			// to j2se1.4
			Vector cv = new Vector();
			for (StringTokenizer st = new StringTokenizer(colList,
				","); st.hasMoreTokens();) {
			    cv.add(st.nextToken().trim());
			}
			cols = (String[]) cv.toArray(new String[cv.size()]);
		    }
		    if (tm.getColumnCount() > 0) {
			setViewColumns((TableModelView) jc, cols);
		    } else {
			final TableModelView f_tmv = (TableModelView) jc;
			final String[] f_colArray = cols;
			TableModelListener tml = new TableModelListener() {
			    final TableModelView tmv = f_tmv;

			    final String[] colArray = f_colArray;

			    public void tableChanged(TableModelEvent e) {
				TableModel tm = (TableModel) e.getSource();
				if (tm.getColumnCount() > 0) {
				    setViewColumns(tmv, colArray);
				    tm.removeTableModelListener(this);
				}
			    }
			};
			tm.addTableModelListener(tml);
		    }
		}
		JFrame jf = ctx.addView(tm, viewName, jc);
		if (jf == null) {
		    String[] vn = getTableContext().getViewNames();
		    Vector vnames = new Vector(vn.length);
		    for (int j = 0; j < vn.length; j++) {
			vnames.add(vn[j]);
		    }
		    System.err.println(viewName + " not in availables views: "
			    + vnames);
		} else if (atIdx >= 0) {
		    setFrameBounds(jf, viewDesc.substring(atIdx + 1).trim());
		}
	    }
	}
	return jc;
    }

    @SuppressWarnings("static-access")
    public static void setPreferences(InputStream is)
	    throws NullPointerException, SecurityException,
	    InvalidPreferencesFormatException, ClassNotFoundException,
	    IOException {
	Preferences.userNodeForPackage(edu.umn.genomics.table.TableView.class)
		.importPreferences(is);
	//
	// If pre j2se1.4
	// Introspection allows us to compile with pre j2se1.4
	// try {
	// Class pref = Class.forName("java.util.prefs.Preferences");
	// Class[] paramClass = new Class[1];
	// paramClass[0] = java.lang.Class.class;
	// Object[] args = new Object[1];
	// args[0] = edu.umn.genomics.table.TableView.class;
	// Object upref =
	// pref.getMethod("userNodeForPackage",paramClass).invoke(null,args);
	// paramClass[0] = java.io.InputStream.class;
	// args[0] = is;
	//upref.getClass().getMethod("importPreferences",paramClass).invoke(upref
	// ,args);
	// } catch (NoSuchMethodException ex) {
	// } catch (InvocationTargetException ex) {
	// } catch (IllegalAccessException ex) {
	// }
    }

    /**
     * Set user preferences from the named source. This will irrevocately
     * overwrite any existing preferences with the same name.
     * 
     * @param source
     *            The URL or file pathname containing the preferences in xml
     *            format.
     */
    public static void setPreferences(String source)
	    throws NullPointerException, SecurityException,
	    // , InvalidPreferencesFormatException
	    ClassNotFoundException, IOException, Exception {
	setPreferences(OpenInputSource.getInputStream(source));
    }

    /**
     * Read table load commands from the source.
     * 
     * <pre>
     *       Example command file:
     * &lt;code&gt;
     *         # Comment lines start with a # character
     *         # each line consists of a name value pair
     *         # the name and value fields are separated by = or :
     *         # the following name fields are recognized:
     *         #
     *         dbname=MYDATABASE
     *         dbdriver=com.mysql.jdbc.Driver
     *         dburl=jdbc:mysql://hostname/dbname
     *         dbusr=my_db_accountname
     *         dbpasswrd=my_db_password
     *         dbquery=select * from mytable
     *         view=viewname
     *         loader=GenePix File
     *         file=/home/me/mydata.tdf
     *         commands=/home/me/more_tableview_commands
     * &lt;/code&gt;
     * </pre>
     * 
     * @param source
     *            a file or URL to a list of name=values parameters.
     */
    public void readCommands(String source) {
	try {
	    readCommands(source, OpenInputSource.getBufferedReader(source));
	} catch (IOException ex) {
	    System.err.println("Unable to open " + source + "  " + ex);
	}
    }

    /**
     * Read table load commands from the source.
     * 
     * @param source
     *            the name of the file or URL source.
     * @param in
     *            the Reader for the command stream.
     */
    @SuppressWarnings("unchecked")
    public void readCommands(String source, Reader in) {
	readCommands(source, in, new Vector());
    }

    /**
     * Read table load commands from the source.
     * 
     * @param source
     *            the name of the file or URL source.
     * @param in
     *            the Reader for the command stream.
     * @param sources
     *            a List of sources already loaded to prevent an infinite
     *            loading loop
     */
    @SuppressWarnings("unchecked")
    private void readCommands(String source, Reader in, Vector sources) {
	if (in == null)
	    return;
	// prevent a loop on command files
	if (sources != null && source != null && sources.contains(source))
	    return;
	Vector srcs = sources != null ? new Vector(sources) : new Vector();
	if (source != null)
	    srcs.add(source);
	// Parse the command file
	try {
	    LineNumberReader rd = new LineNumberReader(in);
	    DBAccountListModel dbs = null;
	    try {
		dbs = new DBAccountListModel();
	    } catch (Exception ex) {
		System.err
			.println("Unable to retrieve Database account preferences: "
				+ ex);
	    }
	    String dbname = null;
	    String dburl = null;
	    String dbusr = null;
	    String dbpwd = null;
	    String dbdvr = null;
	    String dbquery = null;
	    String tableLoader = null;
	    Vector lines = new Vector();
	    for (String line = rd.readLine(); line != null; line = rd
		    .readLine()) {
		lines.add(line);
	    }
	    for (int i = 0; i < lines.size(); i++) {
		String line = (String) lines.get(i);
		String l = line.trim();
		if (l.startsWith("#") || l.length() < 1) {
		    // ignore comment lines
		} else {
		    if (l.startsWith("-")) {
			l = l.substring(1);
		    }
		    // find the name/value split location
		    int si = -1;
		    for (si = 0; si < l.length(); si++) {
			char c = l.charAt(si);
			if (c == '=' || c == ':' || Character.isWhitespace(c))
			    break;
		    }
		    String name = null;
		    String value = null;
		    if (si > 0 && si < l.length() - 1) {
			name = l.substring(0, si);
			value = l.substring(si + 1);
		    } else {
			continue;
		    }

		    if (name.equals("dbname")) {
			dbname = value;
			if (dbs != null) {
			    dbs.setSelectedItem(dbname);
			}
		    } else if (name.equals("dburl")) {
			dburl = value;
		    } else if (name.equals("dbdriver")) {
			dbdvr = value;
			try {
			    Class.forName(dbdvr);
			} catch (Exception ex) {
			    System.err.println(dbdvr + " " + ex);
			}
		    } else if (name.equals("dbusr")) {
			dbusr = value;
		    } else if (name.equals("dbpasswrd")) {
			dbpwd = value;
		    } else if (name.equals("dbquery")) {

			dbquery = value;
			DBUser usr = null;
			if (dbs != null && dbname != null
				&& dbs.getSelectedItem() != null
				&& dbname.equals(dbs.getSelectedItem())) {
			    try {
				usr = new DBUser(dbname, dbs.getUser(dbname),
					dbs.getPassword(dbname), dbs
						.getURL(dbname), dbs
						.getDriver(dbname));
			    } catch (Exception ex) {
				System.err
					.println("Unable to retrieve Database account preferences: "
						+ ex);
			    }
			} else if (dburl != null && dbusr != null
				&& dbpwd != null) {
			    usr = new DBUser(dbname, dbusr, dbpwd, dburl, dbdvr);
			}
			String alias = null;
			String nextLine = i + 1 < lines.size() ? ((String) lines
				.get(i + 1)).trim()
				: null;
			if (nextLine != null
				&& (nextLine.startsWith("alias") || nextLine
					.startsWith("-alias"))) {
			    int ii = -1;
			    for (ii = 0; ii < l.length(); ii++) {
				char c = l.charAt(ii);
				if (c == '=' || c == ':'
					|| Character.isWhitespace(c))
				    break;
			    }
			    if (ii > 0 && ii < l.length() - 1) {
				alias = l.substring(ii + 1).trim();
			    }
			}
			setQuery(usr, dbquery, alias);
		    } else if (name.equals("preferences")) {
			String prefs = value;
			try {
			    setPreferences(prefs);
			} catch (Exception ex) {
			    System.err
				    .println("Unable to set preferences from "
					    + prefs + "  " + ex);
			}
		    } else if (name.equals("view")) {
			displayView(value);
		    } else if (name.equals("loader")) {
			tableLoader = value;
		    } else if (name.equals("file")) {
			String src = value;
			String alias = null;
			String nextLine = i + 1 < lines.size() ? ((String) lines
				.get(i + 1)).trim()
				: null;
			if (nextLine != null
				&& (nextLine.startsWith("alias") || nextLine
					.startsWith("-alias"))) {
			    int ii = -1;
			    for (ii = 0; ii < l.length(); ii++) {
				char c = l.charAt(ii);
				if (c == '=' || c == ':'
					|| Character.isWhitespace(c))
				    break;
			    }
			    if (ii > 0 && ii < l.length() - 1) {
				alias = l.substring(ii + 1).trim();
			    }
			}
			try {
			    loadSource(tableLoader, src, alias);
			} catch (IOException ioex) {
			    System.err.println("Unable to load table from "
				    + src + " : " + ioex);
			}

		    } else if (name.equals("merge")) {
			String alias = null;
			String nextLine = i + 1 < lines.size() ? ((String) lines
				.get(i + 1)).trim()
				: null;
			if (nextLine != null
				&& (nextLine.startsWith("alias") || nextLine
					.startsWith("-alias"))) {
			    int ii = -1;
			    for (ii = 0; ii < l.length(); ii++) {
				char c = l.charAt(ii);
				if (c == '=' || c == ':'
					|| Character.isWhitespace(c))
				    break;
			    }
			    if (ii > 0 && ii < l.length() - 1) {
				alias = l.substring(ii + 1).trim();
			    }
			}
			Vector tmv = new Vector();
			for (StringTokenizer st = new StringTokenizer(value,
				","); st.hasMoreTokens();) {
			    String src = st.nextToken().trim();
			    try {
				TableModel tm = getTableModelForSource(src);
				if (tm != null) {
				    tmv.add(tm);
				} else {
				    throw new Exception("Table " + src
					    + " is not in context");
				}
			    } catch (Exception ex) {
				System.err.println("Unable to merge table "
					+ src + " : " + ex);
				JOptionPane.showMessageDialog(
					getTopLevelAncestor(), ex,
					"Unable to merge table " + src,
					JOptionPane.ERROR_MESSAGE);
			    }
			}
			if (tmv.size() > 0) {
			    try {
				mergeTables(tmv, alias);
			    } catch (IOException ioex) {
				System.err.println("Unable to merge tables "
					+ ioex);
			    }
			}

		    } else if (name.equals("commands")) {
			readCommands(value, OpenInputSource
				.getBufferedReader(value), srcs);
		    } else if (name.equals("alias")) {
			System.err.println("alias " + value
				+ "  was not associated with any table source");
		    } else {
			System.err.println(source + " Unknown command at line "
				+ i + ":\n" + line);
		    }
		}
	    }
	} catch (Exception ex) {
	    System.err.println(source + "\t" + ex);
	}
    }

    /** The paraameters recognized by this application. */
    static String usage = " usage:"
	    + "        java edu.umn.genomics.table.TableView\n"
	    + "          [-help]\n"
	    + "          [-geometry window_size_and_location]\n"
	    + "          [-commands filename|URL]\n"
	    + "          [-preferences filename|URL]\n"
	    + "          [-dblist]\n"
	    + "          [-dbname dbname]\n"
	    + "          [-dburl dburl] [-dbusr dbusr] [-dbpasswrd password]\n"
	    + "          [-dbdriver drivername]\n"
	    + "          [-dbquery query|filename|URL [-alias aliasname]]\n"
	    + "          [-loader tableLoaderName]\n"
	    + "          [-file filename|URL [-alias aliasname]]\n"
	    + "          [-merge [-alias aliasname] aliasname [aliasname ...]]\n"
	    + "          [-view viewname]\n"
	    + "          [-bsh 'beanshell command;[...;]']\n"
	    + "          [-bshsource filename|URL]\n"
	    + "          [-js 'javascript command;[...;]']\n"
	    + "          [-jssource filename|URL]\n"
	    + "          [filename|URL]\n"
	    + "\n"
	    + " DESCRIPTION \n\n"
	    + "   -help\n"
	    + "    Print this help information.\n"
	    + "\n"
	    + "   -geometry window_size_and_location\n"
	    + "    The size and location for the main window.\n"
	    + "    Acceptable formats: \n"
	    + "      WidthxHeight+x+y e.g. 500x300+200+100\n"
	    + "      WidthxHeight-x-y (minuses indicate offset from the right or bottom of the screen)\n"
	    + "      WidthxHeight e.g. 500x300 (set the size, but leave the default location)\n"
	    + "      +x+y e.g. +200+100 (set the location, but leave the default size)\n"
	    + "\n"
	    + "   -commands file \n"
	    + "    Read commands for loading tables from file.\n"
	    + "    Example command file:\n"
	    + "      # Comment lines start with a # character\n"
	    + "      # each line consists of a name value pair\n"
	    + "      # the name and value fields are separated by = or : \n"
	    + "      # (NOTE: URLs are accepted wherever filepaths are accepted) \n"
	    + "      # the following name fields are recognized:\n"
	    + "      #\n"
	    + "      preferences=/home/me/DBAccts.xml\n"
	    + "      dbname=MYDATABASE\n"
	    + "      dbdriver=com.mysql.jdbc.Driver\n"
	    + "      dburl=jdbc:mysql://hostname/dbname\n"
	    + "      dbusr=my_db_accountname\n"
	    + "      dbpasswrd=my_db_password\n"
	    + "      dbquery=select * from mytable\n"
	    + "      alias=mytable\n"
	    + "      file=/home/me/mydata.tdf\n"
	    + "      loader=GenePix File\n"
	    + "      file=/home/me/myslide1.gpr\n"
	    + "      alias=slide1\n"
	    + "      file=/home/me/myslide2.gpr\n"
	    + "      alias=slide2\n"
	    + "      merge=slide1,slide2\n"
	    + "      alias=merged table of slide1 and slide2\n"
	    + "      view=Table\n"
	    + "      view=Compare Rows(/home/me/mydata.tdf)@500x300-600+100\n"
	    + "      view=ScatterPlot(slide1)[0,2]@500x600+100-650\n"
	    + "      commands=/home/me/more_tableview_commands\n"
	    + "\n"
	    + "   -preferences filename|URL\n"
	    + "    A xml file containing user preferences.\n"
	    + "\n"
	    + "   -dblist\n"
	    + "    List databases stored in your preferences.\n"
	    + "\n"
	    + "   -dbname dbname\n"
	    + "    Connect to this database using the usr information in your preferences.\n"
	    + "\n"
	    + "   -dburl dburl -dbusr dbusr -dbpasswrd password\n"
	    + "    Connect to database  dburl  with user  dbusr  and password  password\n"
	    + "      Example URLS:\n"
	    + "        jdbc:oracle:thin:@hostname:1521:dbname\n"
	    + "        jdbc:mysql://hostname/dbname\n"
	    + "        jdbc:postgresql://hostname/dbname\n"
	    + "\n"
	    + "   -dbdriver drivername\n"
	    + "    The database JDBC Driver Class name \n"
	    + "     (e.g. oracle.jdbc.driver.OracleDriver, com.mysql.jdbc.Driver,  org.postgresql.Driver, etc.)\n"
	    + "\n"
	    + "   -dbquery query|filename|URL [-alias aliasname]\n"
	    + "    The query (or a path or URL to a file containing the query) to execute. \n"
	    + "    A reference name may be included with the -alias option. \n"
	    + "\n"
	    + "   -loader tableLoaderName\n"
	    + "    The name of a table loader that should be used to read the next input source. \n"
	    + "\n"
	    + "   -file filename|URL [-alias aliasname]\n"
	    + "    A file containing a table of data in ASCII text. \n"
	    + "     reference name may be included with the -alias option.\n"
	    + "\n"
	    + "   -merge [-alias aliasname] aliasname|filename|URL ... \n"
	    + "    Create a merged table of the tables listed by aliasname or filename|URL\n"
	    + "    A reference name may be included with the -alias option.\n"
	    + "\n"
	    + "   -view viewname(tablesource)[column,...]@geometry\n"
	    + "    Open a window for the named view. '(tablesource)', '[column,...]',  and '@geometry' are optional.\n"
	    + "    If a tablesource is given, set the selection to that table source.\n"
	    + "    If columns given, the view will be requested to display the listed columns,\n"
	    + "    columns may either be column names or column indices (indices start at 0). \n"
	    + "    If a geometry is given, set the size and location of the view window.\n"
	    + "\n"
	    + "   -bsh 'beanshell command;[...;]'\n"
	    + "    Execute the beanshell commands. \n"
	    + "\n"
	    + "   -bshsource filename|URL\n"
	    + "    Execute the beanshell script.\n"
	    + "\n"
	    + "   -js 'javascript command;[...;]'\n"
	    + "    Execute the javascript commands.\n"
	    + "\n"
	    + "   -jssource filename|URL\n"
	    + "    Execute the javascript script.\n"
	    + "\n"
	    + "   Scripts have access to the following variables:\n"
	    + "     tv (or tableView): the TableView instance\n"
	    + "     ctx (or tableContext): the TableView tableContext \n"
	    + "     Cells: the utility class\n"
	    + "   Examples: \n"
	    + "     -bsh 'ctx.getRowSelectionModel(tv.getTableModel()).addSelectionInterval(5,10);'   \n"
	    + "     -bsh 'tv.displayView(\"Table\"); tv.displayView(\"Histograms[1,2]\");'   \n"
	    + "     -js 'var m = tv.getClass().getDeclaredMethods(); "
	    + "for(var i = 0; i < m.length; i++) { print(i + \"\t\" + m[i]);}'   \n"
	    + "\n" + "   filename|URL\n"
	    + "    A file containing a table of data in ASCII text. \n" + "\n";

    /**
     * Print a list of the available views to System.out.
     */
    public void printViewList() {
	String[] vn = getTableContext().getViewNames();
	if (vn != null) {
	    System.out.println("Available Views:");
	    for (int j = 0; j < vn.length; j++) {
		System.out.println("\t" + vn[j]);
	    }
	}
    }

    /**
     * Print a list of the available loaders to System.out.
     */
    public void printLoaderList() {
	String[] ln = getLoadTable().getTableLoaderNames();
	if (ln != null) {
	    System.out.println("Available Loaders:");
	    for (int j = 0; j < ln.length; j++) {
		System.out.println("\t" + ln[j]);
	    }
	}
    }

    /**
     * Returns a list of view names available for viewing tables. This is a
     * shortcut for Arrays.asList(getTableContext().getViewNames())
     * 
     * @return a list of view names available for viewing tables.
     */
    @SuppressWarnings("unchecked")
    public java.util.List getViewList() {
	String[] vn = getTableContext().getViewNames();
	if (vn != null) {
	    return new Vector(Arrays.asList(vn));
	}
	return null;
    }

    /**
     * Returns a list of names of loaders available for loading table files.
     * This is a shortcut for
     * Arrays.asList(getLoadTable().getTableLoaderNames())
     * 
     * @return a list of names of loaders available for loading table files.
     */
    @SuppressWarnings("unchecked")
    public java.util.List getLoaderList() {
	String[] ln = getLoadTable().getTableLoaderNames();
	if (ln != null) {
	    return new Vector(Arrays.asList(ln));
	}
	return null;
    }

    public void runScript(ScriptInterpreter interpreter, String script) {
	ByteArrayInputStream in = new ByteArrayInputStream(script.getBytes());
	runScript(interpreter, in);
    }

    @SuppressWarnings("unchecked")
    public void runScript(ScriptInterpreter interpreter, InputStream in) {
	Map vars = getScriptVars();
	interpreter.initialize(in, System.out, System.err, vars);
	new Thread(interpreter).start();
    }

    /**
     * Creates a TableView that can display a variety of views of a table. An
     * argument can specify a source for a table. If the argument contains
     * spaces, it is assumed to be a data base query, otherwise it is assumed to
     * be a filename.
     * 
     * <pre>
     * &lt;code&gt;
     *    &lt;B&gt;NAME&lt;/B&gt;
     *      TableView
     *   
     *    &lt;B&gt;SYNOPSIS&lt;/B&gt;
     *           &lt;B&gt;java edu.umn.genomics.table.TableView&lt;/B&gt; 
     *             [&lt;B&gt;-help&lt;/B&gt;] 
     *             [&lt;B&gt;-geometry&lt;/B&gt; window_size_and_location]
     *             [&lt;B&gt;-commands&lt;/B&gt; filename|URL]
     *             [&lt;B&gt;-preferences&lt;/B&gt; filename|URL]
     *             [&lt;B&gt;-dblist&lt;/B&gt;] 
     *             [&lt;B&gt;-commands&lt;/B&gt; file] 
     *             [&lt;B&gt;-dbname&lt;/B&gt; dbname] 
     *             [&lt;B&gt;-dburl&lt;/B&gt; dburl] [&lt;B&gt;-dbusr&lt;/B&gt; dbusr] [&lt;B&gt;-dbpasswrd&lt;/B&gt; password] 
     *             [&lt;B&gt;-dbdriver&lt;/B&gt; drivername] 
     *             [&lt;B&gt;-dbquery&lt;/B&gt; query|filename|URL [&lt;B&gt;-alias&lt;/B&gt; aliasname]] 
     *             [&lt;B&gt;-loader&lt;/B&gt; tableLoaderName]
     *             [&lt;B&gt;-file &lt;/B&gt; filename|URL [&lt;B&gt;-alias&lt;/B&gt; aliasname]]
     *             [&lt;B&gt;-merge&lt;/B&gt; [&lt;B&gt;-alias&lt;/B&gt; aliasname] aliasname ...]
     *             [&lt;B&gt;-view&lt;/B&gt; viewName[(tableSource)][[column,...]][@widthxheight+x+y]]
     *             [&lt;B&gt;-bsh&lt;/B&gt; beanshell_commands]
     *             [&lt;B&gt;-bshsource&lt;/B&gt; beanshell_script]
     *             [&lt;B&gt;-js&lt;/B&gt; javascript_commands]
     *             [&lt;B&gt;-jssource&lt;/B&gt; javascript_script]
     *             [filename|URL ...]
     *   
     *    &lt;B&gt;DESCRIPTION &lt;/B&gt;
     *      &lt;B&gt;-help&lt;/B&gt;
     *       Print this help information.
     *   
     *      &lt;B&gt;-views&lt;/B&gt;
     *       List the available views.
     *   
     *      &lt;B&gt;-loaders&lt;/B&gt;
     *       List the available loaders.
     *   
     *   
     *      &lt;B&gt;-commands&lt;/B&gt; file 
     *       Read commands for loading tables from file.
     *       Example command file:
     *         # Comment lines start with a # character
     *         # each line consists of a name value pair
     *         # the name and value fields are separated by = or : 
     *         # the following name fields are recognized:
     *         #
     *         preferences=/home/me/DBAccts.xml
     *         dbname=MYDATABASE
     *         dbdriver=com.mysql.jdbc.Driver
     *         dburl=jdbc:mysql://hostname/dbname
     *         dbusr=my_db_accountname
     *         dbpasswrd=my_db_password
     *         dbquery=select * from mytable
     *         alias=mytable
     *         file=/home/me/mydata.tdf
     *         loader=GenePix File
     *         file=/home/me/myslide1.gpr
     *         alias=slide1
     *         file=/home/me/myslide2.gpr
     *         alias=slide2
     *         merge=slide1,slide2
     *         alias=merged table of slide1 and slide2
     *         view=Table
     *         view=Compare Rows(/home/me/mydata.tdf)@500x300-600+100
     *         view=ScatterPlot(slide1)[0,2]@500x600+100-650
     *         commands=/home/me/more_tableview_commands
     *   
     *      &lt;B&gt;-dblist&lt;/B&gt;
     *       List databases stored in your preferences.
     *   
     *      &lt;B&gt;-dbname&lt;/B&gt; dbname
     *       Connect to this database using the usr information in your preferences.
     *   
     *      &lt;B&gt;-dburl&lt;/B&gt; dburl &lt;B&gt;-dbusr&lt;/B&gt; dbusr &lt;B&gt;-dbpasswrd&lt;/B&gt; password
     *       Connect to database  dburl  with user  dbusr  and password  password
     *         Example URLS:
     *           jdbc:oracle:thin:@hostname:1521:dbname
     *           jdbc:mysql://hostname/dbname
     *           jdbc:postgresql://hostname/dbname
     *   
     *      &lt;B&gt;-dbdriver&lt;/B&gt; drivername
     *       The database JDBC Driver Class name 
     *        (e.g. oracle.jdbc.driver.OracleDriver, com.mysql.jdbc.Driver,  org.postgresql.Driver, etc.)
     *   
     *      &lt;B&gt;-dbquery&lt;/B&gt; query|filename|URL [&lt;B&gt;-alias&lt;/B&gt; aliasname]
     *       The query to execute, or a path or URL to a file containing a query. 
     *       The &lt;B&gt;-alias&lt;/B&gt; option gives a name to the table for display and reference.
     *   
     *      &lt;B&gt;-loader&lt;/B&gt; tableLoaderName
     *       The name of a table loader that should be used to read the next input source. 
     *   
     *      &lt;B&gt;-file&lt;/B&gt; filename|URL [&lt;B&gt;-alias&lt;/B&gt; aliasname]
     *       A path or URL to a file containing a table of data in ASCII text. 
     *       The &lt;B&gt;-alias&lt;/B&gt; option gives a name to the table for display and reference.
     *   
     *      &lt;B&gt;-bsh&lt;/B&gt; beanshell_commands
     *       Execute beanshell script commands.
     *   
     *      &lt;B&gt;-bshsource&lt;/B&gt; beanshell_script
     *       Execute the beanshell script commands in the file or URL.
     *   
     *      &lt;B&gt;-js&lt;/B&gt; javascript_commands
     *       Execute the javascript commands.
     *   
     *      &lt;B&gt;-jssource&lt;/B&gt; javascript_script
     *       Execute the javascript commands in the file or URL.
     *   
     *      filename|URL
     *       A path or URL to a file containing a table of data in ASCII text. 
     * &lt;/code&gt;
     * </pre>
     * 
     * @param args
     *            source of the data table
     * @see FileTableModel
     */
    public static void main(String args[]) {
	/*
	 * DBAccountListModel dbs = null; try { dbs = new DBAccountListModel();
	 * } catch (Exception ex) { System.err .println("Unable to retrieve
	 * Database account preferences: " + ex); } String dbname = null; String
	 * dburl = null; String dbusr = null; String dbpwd = null; String dbdvr
	 * = null; String dbquery = null; String geometry = null; String
	 * tableLoader = null;
	 */
	// try {
	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	// } catch(Exception e) {
	// System.out.println("Error setting native LAF: " + e);
	// }
	JFrame frame = new JFrame("TableView");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	TableView tv = new TableView();
	VirtualTableModel model = tv.setFile(
		"C:\\tableview\\data\\patienttable.txt", "PatientData");
	tv.displayTableModelView("Histogram", model);
	tv.displayTableModelView("Histograms", model);
	tv.displayTableModelView("Table", model);
	tv.displayTableModelView("Histogram3D", model);

	model = tv.setFile("C:\\tableview\\data\\dd.data", "ddData");
	tv.displayTableModelView("Histogram", model);
	tv.displayTableModelView("Histograms", model);
	tv.displayTableModelView("Table", model);
	tv.displayTableModelView("Histogram3D", model);

	/*
	 * for (int i = 0; i < args.length; i++) { if (args[i].startsWith("-"))
	 * { if (args[i].equals("-help")) { System.err.println(usage);
	 * tv.printViewList(); tv.printLoaderList(); if (args.length == 1) {
	 * System.exit(0); } } else if (args[i].equals("-views")) {
	 * tv.printViewList(); } else if (args[i].equals("-loaders")) {
	 * tv.printLoaderList(); } else if (args[i].equals("-dblist")) { if (dbs
	 * != null) { try { String[] accts = dbs.getAccountNames(); for (int j =
	 * 0; j < accts.length; j++) { System.out.println(" " + accts[j]);
	 * System.out.println(" -dbname " + accts[j]); System.out.println("
	 * -dbdriver " + dbs.getDriver(accts[j])); System.out.println(" -dburl "
	 * + dbs.getURL(accts[j])); System.out.println(" -dbusr " +
	 * dbs.getUser(accts[j])); StringBuffer sb = new
	 * StringBuffer("********"); String pw = dbs.getPassword(accts[j]); if
	 * (pw != null) { sb.setLength(0); for (int k = pw.length(); k > 0; k--)
	 * sb.append("*"); } System.out.println(" -dbpasswrd " + sb); } } catch
	 * (Exception ex) { System.err .println("Unable to retrieve Database
	 * account preferences: " + ex); } } } else if
	 * (args[i].equals("-preferences")) { String prefs = args[++i]; try {
	 * tv.setPreferences(prefs); } catch (Exception ex) {
	 * System.err.println("Unable to set preferences from " + prefs + " " +
	 * ex); } } else if (args[i].equals("-geometry")) { geometry =
	 * args[++i]; } else if (args[i].equals("-commands")) {
	 * tv.readCommands(args[++i]); } else if (args[i].equals("-dbname")) {
	 * dbname = args[++i]; if (dbs != null) { dbs.setSelectedItem(dbname); }
	 * } else if (args[i].equals("-dburl")) { dburl = args[++i]; } else if
	 * (args[i].equals("-dbdriver")) { dbdvr = args[++i]; try {
	 * Class.forName(dbdvr); } catch (Exception ex) {
	 * System.err.println(dbdvr + " " + ex); } } else if
	 * (args[i].equals("-dbusr")) { dbusr = args[++i]; } else if
	 * (args[i].equals("-dbpasswrd")) { dbpwd = args[++i]; } else if
	 * (args[i].equals("-dbquery")) { dbquery = args[++i]; DBUser usr =
	 * null; if (dbs != null && dbname != null && dbs.getSelectedItem() !=
	 * null && dbname.equals(dbs.getSelectedItem())) { try { usr = new
	 * DBUser(dbname, dbs.getUser(dbname), dbs .getPassword(dbname),
	 * dbs.getURL(dbname), dbs.getDriver(dbname)); } catch (Exception ex) {
	 * System.err
	 * .println("Unable to retrieve Database account preferences: " + ex); }
	 * } else if (dburl != null && dbusr != null && dbpwd != null) { usr =
	 * new DBUser(dbname, dbusr, dbpwd, dburl, dbdvr); } String alias = (i +
	 * 2 < args.length && args[i + 1].equals("-alias") && i++ < args.length)
	 * ? args[++i] : null; tv.setQuery(usr, dbquery, alias); } else if
	 * (args[i].equals("-loader")) { tableLoader = args[++i]; } else if
	 * (args[i].equals("-merge")) { String mergeAlias = (i + 2 < args.length
	 * && args[i + 1].equals("-alias") && i++ < args.length) ? args[++i] :
	 * null; Vector tmv = new Vector(); while (i + 1 < args.length &&
	 * !args[i + 1].startsWith("-")) { String src = args[++i]; String alias
	 * = (i + 2 < args.length && args[i + 1].equals("-alias") && i++ <
	 * args.length) ? args[++i] : null; try { TableModel tm =
	 * tv.getTableModelForSource(src); if (tm == null) { tm =
	 * tv.loadSource(tableLoader, src, alias); } if (tm != null) {
	 * tmv.add(tm); } } catch (IOException ioex) {
	 * System.err.println("Unable to load table from " + src + " : " +
	 * ioex); } } if (tmv.size() > 0) { try { tv.mergeTables(tmv,
	 * mergeAlias); } catch (IOException ioex) { System.err .println("Unable
	 * to merge tables " + ioex); } } } else if (args[i].equals("-file")) {
	 * String src = args[++i]; String alias = (i + 2 < args.length && args[i
	 * + 1].equals("-alias") && i++ < args.length) ? args[++i] : null; try {
	 * tv.loadSource(tableLoader, src, alias); } catch (IOException ioex) {
	 * System.err.println("Unable to load table from " + src + " : " +
	 * ioex); } } else if (args[i].equals("-view")) { i++; } else if
	 * (args[i].equals("-alias")) { String alias = args[++i];
	 * System.err.println("-alias " + alias + " was not associated with any
	 * table source"); } else if (args[i].equals("-js")) { String script =
	 * args[++i]; tv.runScript(new ScriptJS(), script); } else if
	 * (args[i].equals("-jssource")) { String source = args[++i]; try {
	 * tv.runScript(new ScriptJS(), OpenInputSource
	 * .getInputStream(source)); } catch (Exception ex) {
	 * ex.printStackTrace(); } } else if (args[i].equals("-bsh")) { String
	 * script = args[++i]; tv.runScript(new ScriptBsh(), script); } else if
	 * (args[i].equals("-bshsource")) { String source = args[++i]; try {
	 * tv.runScript(new ScriptBsh(), OpenInputSource
	 * .getInputStream(source)); } catch (Exception ex) {
	 * ex.printStackTrace(); } } else { System.err.println("Unknown
	 * argument: " + args[i]); System.err.println(usage); System.exit(1); }
	 * } else { String src = args[i]; String alias = (i + 2 < args.length &&
	 * args[i + 1].equals("-alias") && i++ < args.length) ? args[++i] :
	 * null; try { tv.loadSource(tableLoader, src, alias); } catch
	 * (IOException ioex) { System.err.println("Unable to load table from "
	 * + src + " : " + ioex); } } }
	 */
	frame.getContentPane().add(tv, BorderLayout.CENTER);
	frame.setLocation(100, 100);
	frame.pack();
	// if (geometry != null) {
	// setFrameBounds(frame, geometry);
	// }
	frame.setVisible(true);
	/*
	 * for (int i = 0; i < args.length; i++) { if (args[i].startsWith("-"))
	 * { if (args[i].equals("-view")) { tv.displayView(args[++i]); } } }
	 */
    }
}
