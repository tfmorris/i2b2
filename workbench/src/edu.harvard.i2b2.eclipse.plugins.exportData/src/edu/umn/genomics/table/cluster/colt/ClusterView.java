/*
 * @(#) $RCSfile: ClusterView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster.colt;

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import edu.umn.genomics.table.*;
import edu.umn.genomics.table.cluster.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;

/**
 * ClusterView displays a hierarchical clustering of rows from a table. The
 * clustering is displayed as a Dendogram which is drawn as line segments on a
 * graph widget. The axes of the graph are zoomable. The row selection of the
 * table is displayed on the dendogram. The user can trace out a rectangle on
 * the dendogram to edit the row selection set for the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 18:06:26 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClusterView extends AbstractTableModelView implements Serializable {

    // Select columns for clustering
    // Select cluster method
    // Select distance method
    int[] colMap = null;
    DefaultListModel colLM = new DefaultListModel();
    JList colList = new JList(colLM);
    String clusterMethods[] = { "Average", "Single-Link", "Complete-Link" };
    JComboBox clusterChoice = new JComboBox(clusterMethods);
    JComboBox distFuncChoice = new JComboBox(AbstractSimilarity
	    .getDistanceFunctions());
    DefaultComboBoxModel labelModel = new DefaultComboBoxModel();
    JComboBox labelChoice = new JComboBox(labelModel);
    JProgressBar progressBar = new JProgressBar();
    SimpleGraph graph;
    GraphSegments gs;
    IndexSelectColor idxSelColor;
    Point start = null;
    Point current = null;
    boolean selecting = false;
    TreeNode[] nodemap;
    TreeNode rootNode;
    /** map of row index to leaf node index */
    OneToOneIndexMap idxMap = new OneToOneIndexMap();
    /** Number od segments per node */
    int segOffset = 2;
    int prevSetOp = -1;
    int labelColumn = 0;

    class ClusterThread extends Thread {
	@Override
	public void run() {
	    showCluster();
	}
    };

    ClusterThread clusterThread = null;
    ListSelectionListener selListener = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    mapSelection();
	}
    };
    AxisLabeler labeler = new AxisLabeler() {
	public String getLabel(double value) {
	    return getLeafLabel(value);
	}
    };

    /**
     * Return a label for the given point on the graph axis
     * 
     * @param value
     *            the value on the graph
     * @return the label for the given value
     */
    private String getLeafLabel(double value) {
	String label = Double.toString(value);
	try {
	    int v = (int) value;
	    int r = idxMap.getSrc(v);
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

    /**
     * Make selections on the graph panel. User draws out a rectangle on the
     * graph. Any nodes intersected by the rect will be selected. Note that this
     * sets the ListSelection model for the table rows, but it is the listener
     * of the TableModel row ListSelectionModel that will actually set the
     * selection of the line segments for display in the graph.
     */
    private MouseAdapter ma = new MouseAdapter() {
	@Override
	public void mousePressed(MouseEvent e) {
	    if (ctx != null) {
		// save the current selection set operator
		prevSetOp = ctx.getSetOperator(tm).getSetOperator();
		// set the selection set operator
		ctx.getSetOperator(tm).setFromInputEventMask(e.getModifiers());
	    }
	    start = e.getPoint();
	    current = e.getPoint();
	    selecting = true;
	    repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    current = e.getPoint();
	    // intersect with graph
	    Rectangle selrect = new Rectangle(start.x, start.y, current.x
		    - start.x, current.y - start.y);
	    int[] gi = gs.getIndicesAt(selrect, graph.getXAxis(), graph
		    .getYAxis());
	    DefaultListSelectionModel rsm = new DefaultListSelectionModel();
	    if (gi != null) {
		rsm.setValueIsAdjusting(true);
		for (int j = 0; j < gi.length; j++) {
		    // find node and select segs for node and all descendents
		    int nodeidx = gi[j] / 2;
		    TreeNode tn = nodemap[nodeidx];
		    selectTraverse(tn, rsm);
		}
		rsm.setValueIsAdjusting(false);
	    }
	    if (ctx != null) {
		// Merge this selection with the table selection list
		// using the current set selection operator
		ColumnMap cmap = ctx.getColumnMap(tm, 0);
		if (cmap != null) {
		    cmap.selectValues(rsm);
		}
	    }
	    if (ctx != null) {
		// restore the original selection set operator
		ctx.getSetOperator(tm).setSetOperator(prevSetOp);
	    }
	    repaint();
	}
    };

    /**
     * Constructs a ClusterView display. Nothing will be displayed until a data
     * model is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ClusterView() {
	super();
	try {
	    init();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Constructs a ClusterView diaplay which is initialized with tableModel as
     * the data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ClusterView(TableModel tableModel) {
	super(tableModel);
	init();
    }

    /**
     * Constructs a ClusterView diaplay which is initialized with tableModel as
     * the data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public ClusterView(TableModel tableModel, ListSelectionModel lsm) {
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
	if (tm != tableModel) {
	    super.setTableModel(tableModel);
	    mapColumns();
	}
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
	lsm.addListSelectionListener(selListener);
    }

    /**
     * initialize the components in the view panel
     */
    private void init() {
	setLayout(new BorderLayout());
	JPanel top = new JPanel(new BorderLayout());
	JToolBar btnP = new JToolBar();
	add(btnP, BorderLayout.NORTH);
	progressBar.setVisible(false);
	top.add(progressBar, BorderLayout.NORTH);
	progressBar.setStringPainted(true);
	JButton clusterBtn = new JButton("cluster");
	clusterBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		// Generate the cluster in a separate Thread
		if (clusterThread != null) {
		    try {
			clusterThread.interrupt();
		    } catch (Exception ex) {
		    } finally {
		    }
		}
		clusterThread = new ClusterThread();
		clusterThread.start();
	    }
	});
	labelChoice.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    labelColumn = 0;
		    String ln = (String) e.getItem();
		    if (ln != null) {
			for (int c = 0; c < tm.getColumnCount(); c++) {
			    if (ln.equals(tm.getColumnName(c))) {
				labelColumn = c;
				break;
			    }
			}
		    }
		    repaint();
		}
	    }
	});
	clusterChoice.setToolTipText("Clustering Linkage");
	distFuncChoice.setToolTipText("Distance Function");
	labelChoice.setToolTipText("Label using");
	clusterBtn.setToolTipText("Start Clustering");
	progressBar.setToolTipText("Clustering progress");

	btnP.add(clusterChoice);
	btnP.add(distFuncChoice);
	btnP.add(clusterBtn);
	btnP.add(labelChoice);

	graph = new SimpleGraph();
	graph.getGraphDisplay().setOpaque(true);
	graph.getGraphDisplay().setBackground(Color.white);
	graph.getGraphDisplay().setGridColor(new Color(220, 220, 220));
	graph.showGrid(true);
	graph.showAxis(BorderLayout.WEST, false);
	graph.showAxis(BorderLayout.EAST, true);
	graph.getAxisDisplay(BorderLayout.EAST).setZoomable(true);
	graph.getAxisDisplay(BorderLayout.EAST).setAxisLabeler(labeler);
	((LinearAxis) graph.getYAxis()).setTickIncrement(-1.);
	graph.getAxisDisplay(BorderLayout.SOUTH).setZoomable(true);
	gs = new GraphSegments();
	gs.setColor(Color.blue);
	idxSelColor = new IndexSelectColor(Color.cyan, null,
		new DefaultListSelectionModel());
	gs.setIndexedColor(idxSelColor);
	graph.addGraphItem(gs);
	graph.getGraphDisplay().addMouseListener(ma);

	colList
		.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	colList.setToolTipText("Select columns to cluster on");
	mapColumns();

	JScrollPane jsp = new JScrollPane(colList,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
		jsp, graph);
	split.setOneTouchExpandable(true);
	top.add(split);
	add(top);
	if (lsm != null) {
	    lsm.addListSelectionListener(selListener);
	}
    }

    /**
     * Generate the hierarchical cluster and display it as a denogram in the
     * graph
     */
    private void showCluster() {
	// Get a list of columns to use for clustering rows
	if (tm != null) {
	    int[] selcols = colList.getSelectedIndices();
	    for (int i = 0; i < selcols.length; i++) {
		selcols[i] = colMap[selcols[i]];
	    }
	    if (selcols != null && selcols.length > 0) {
		final AbstractSimilarity av;
		// Get the user choice for clustering method
		switch (clusterChoice.getSelectedIndex()) {
		case 0:
		default:
		    av = new AverageSimilarity();
		    break;
		case 1:
		    av = new SingleSimilarity();
		    break;
		case 2:
		    av = new CompleteSimilarity();
		    break;
		}
		// Get the user choice for distance function
		av.setDistanceFunction(distFuncChoice.getSelectedItem());
		// av.debug = 1;
		progressBar.setVisible(true);
		progressBar.setMinimum(1);
		progressBar.setMaximum(tm.getRowCount());
		progressBar.setValue(1);
		TimerTask task = new TimerTask() {
		    @Override
		    public void run() {
			try {
			    if (!clusterThread.isAlive()) {
				System.err.println("clusterThread died");
				progressBar.setVisible(false);
				this.cancel();
			    }
			} catch (Exception ex) {
			    System.err.println("clusterThread timer " + ex);
			}
			progressBar.setValue(progressBar.getMaximum()
				- av.getRemainingCount());
		    }
		};
		java.util.Timer timer = new java.util.Timer();
		timer.schedule(task, 1000, 1000);
		rootNode = av.cluster(ctx, tm, selcols);
		if (rootNode != null) {
		    display(rootNode);
		    mapSelection();
		}
		timer.cancel();
		progressBar.setVisible(false);
		validate();
	    }
	}
    }

    /**
     * Display the tree rooted at node tn in the graph as a dendogram.
     * 
     * @param tn
     *            the root node of the tree to display
     */
    private void display(TreeNode tn) {
	double[] segs = dendogram(tn);
	double distance = 10.;
	if (tn instanceof Cluster) {
	    distance = ((Cluster) tn).getSimilarity();
	} else {
	    distance = ((DefaultMutableTreeNode) tn).getDepth();
	}
	gs.setData(segs, GraphDataModel.FORMAT_XY);
	graph.getXAxis().setMin(distance);
	graph.getXAxis().setMax(0.);
	graph.getYAxis().setMin(tm.getRowCount());
	graph.getYAxis().setMax(0.);
    }

    /**
     * Return the number of leaf nodes that descend from the given node.
     * 
     * @param node
     *            A node in the Treemodel.
     * @return The number of leaf nodes that descend from the given node.
     */
    private static int getLeafCount(TreeNode tn) {
	int lc = 0;
	if (!tn.isLeaf()) {
	    for (int i = 0; i < tn.getChildCount(); i++) {
		TreeNode cn = tn.getChildAt(i);
		lc += getLeafCount(cn);
	    }
	} else {
	    lc = 1;
	}
	return lc;
    }

    /**
     * Return a count of this node and all its descendents.
     * 
     * @param node
     *            A node in the Treemodel.
     * @return The number of nodes in this branch of the Treemodel.
     */
    private static int getNodeCount(TreeNode tn) {
	int nc = 1; // this node
	if (!tn.isLeaf()) {
	    for (int i = 0; i < tn.getChildCount(); i++) {
		TreeNode cn = tn.getChildAt(i);
		nc += getNodeCount(cn);
	    }
	}
	return nc;
    }

    /**
     * Start the traversal of the tree for row selection.
     */
    private void mapSelection() {
	if (rootNode != null && idxSelColor != null && lsm != null) {
	    ListSelectionModel gsm = idxSelColor.getSelectionModel();
	    int[] nodeidx = new int[1];
	    nodeidx[0] = 0;
	    selTraverse(rootNode, nodeidx, gsm, lsm);
	    repaint();
	}
    }

    /**
     * Recursive traverse of tree to determine selections A leaf is selected if
     * rsm is selected. Nonleaf nodes are selected if all children are selected.
     * 
     * @param tn
     *            node in the tree for which to determine selection
     * @param nodeidx
     *            the ordinal postion in the segments array
     * @param gsm
     *            the graph segments selection model
     * @param rsm
     *            the table row selection model
     * @return true if given node tn is selected, else false
     */
    private boolean selTraverse(TreeNode tn, int[] nodeidx,
	    ListSelectionModel gsm, ListSelectionModel rsm) {
	boolean selected = true;
	if (!tn.isLeaf()) {
	    // A nonleaf node is selected if all its children are selected.
	    for (int i = 0; i < tn.getChildCount(); i++) {
		TreeNode cn = tn.getChildAt(i);
		selected &= selTraverse(cn, nodeidx, gsm, rsm);
	    }
	} else {
	    if (tn instanceof RowCluster) {
		// get the row index of the leaf node
		int ri = ((RowCluster) tn).getIndex();
		// A leaf is selected if its row is selected in the row
		// selection rsm.
		selected = rsm.isSelectedIndex(ri);
	    }
	}
	// Get the offset into the segments array
	int idx = nodeidx[0] * segOffset;
	if (selected) {
	    gsm.addSelectionInterval(idx, idx + (segOffset - 1));
	} else {
	    gsm.removeSelectionInterval(idx, idx + (segOffset - 1));
	}
	// Increment the nodeidx in the tree
	nodeidx[0]++;
	return selected;
    }

    /**
     * Traververse the tree selecting rows coresponding to leaf nodes of the
     * given node tn
     * 
     * @param tn
     *            the node from which to start traversing
     * @param rsm
     *            the selection model in which to mark selected rows
     */
    private void selectTraverse(TreeNode tn, ListSelectionModel rsm) {
	if (!tn.isLeaf()) {
	    for (int i = 0; i < tn.getChildCount(); i++) {
		TreeNode cn = tn.getChildAt(i);
		selectTraverse(cn, rsm);
	    }
	} else {
	    if (tn instanceof RowCluster) {
		int ri = ((RowCluster) tn).getIndex();
		rsm.addSelectionInterval(ri, ri);
	    }
	}
    }

    /**
     * Traverse the tree creating dendogram graph line segments, a nodemap, and
     * a leafmap. Traversal is depth first. return child position currentleafcnt
     * 
     * @param tn
     *            the tree node to traverse
     * @param leafcnt
     *            the leafcount prior to this node
     * @param parentDistance
     *            the distance of the parent node
     * @param nodeidx
     *            the current node index, incremented after each node is
     *            traversed
     * @param nodemap
     *            an ordered list of nodes traversed
     * @param leafmap
     *            an order list of row indices to the leafnode traversed
     * @param segs
     *            line segments comprising the dendogram
     * @param childpos
     *            position of child node returned to parent
     * @return the leafcount after traversing this node
     */
    private int traverse(TreeNode tn, int leafcnt, double parentDistance,
	    int[] nodeidx, TreeNode[] nodemap, int[] leafmap, double[] segs,
	    double childpos[]) {
	int lc = leafcnt;
	double distance = 0.;
	double height = 0.;
	double minChildx = Double.NaN;
	double maxChildx = Double.NaN;
	double minChildy = Double.NaN;
	double maxChildy = Double.NaN;
	if (tn instanceof Cluster) {
	    distance = ((Cluster) tn).getSimilarity();
	} else {
	    distance = ((DefaultMutableTreeNode) tn).getDepth();
	}
	if (!tn.isLeaf()) {
	    for (int i = 0; i < tn.getChildCount(); i++) {
		TreeNode cn = tn.getChildAt(i);
		lc = traverse(cn, lc, distance, nodeidx, nodemap, leafmap,
			segs, childpos);
		if (Double.isNaN(minChildx) || childpos[0] < minChildx) {
		    minChildx = childpos[0];
		}
		if (Double.isNaN(maxChildx) || childpos[0] > maxChildx) {
		    maxChildx = childpos[0];
		}
		if (Double.isNaN(minChildy) || childpos[1] < minChildy) {
		    minChildy = childpos[1];
		}
		if (Double.isNaN(maxChildy) || childpos[1] > maxChildy) {
		    maxChildy = childpos[1];
		}
	    }
	} else {
	    if (tn instanceof RowCluster) {
		leafmap[lc] = ((RowCluster) tn).getIndex();
	    }
	    minChildx = distance;
	    maxChildx = distance;
	    minChildy = lc;
	    maxChildy = lc;
	    lc++;
	}
	// offset into segs
	int offset = nodeidx[0] * segOffset * 4;
	nodemap[nodeidx[0]] = tn;
	nodeidx[0]++;

	if (segs.length < offset + 8) {
	    double tmp[] = segs;
	    segs = new double[offset + 8];
	    System.arraycopy(tmp, 0, segs, 0, tmp.length);
	}

	// segment from minchild to maxchild
	segs[offset++] = distance; // X
	segs[offset++] = minChildy; // Y
	segs[offset++] = distance; // X
	segs[offset++] = maxChildy; // Y
	// segment from node to parent of length distance
	// postision half way between first and last child
	double y = minChildy + (maxChildy - minChildy) / 2.;
	segs[offset++] = distance; // X
	segs[offset++] = y; // Y
	segs[offset++] = parentDistance; // X
	segs[offset++] = y; // Y
	/*
	 * System.err.println(tn.toString() + "       \tmlc " + leafcnt + " lc "
	 * + lc + " pd " + parentDistance + " d " + distance); for (int i = 8, j
	 * = leafcnt 8; i < 8; i++,j++) { System.err.print("\t" + segs[j]); }
	 * System.err.println("");
	 */

	// Update return values
	childpos[0] = distance;
	childpos[1] = y;
	return lc;
    }

    /**
     * Generate a list of line segments that construct a dendogram of the tree
     * rooted at node tn.
     * 
     * @param tn
     *            the root of the tree
     * @return the segements comprising the dendogram
     */
    private double[] dendogram(TreeNode tn) {
	double distance = 0.;
	nodemap = new TreeNode[getNodeCount(tn)];
	int[] leafmap = new int[getLeafCount(tn)]; // record order of rows
	int[] nodeidx = new int[1];
	double[] segs = null;
	double[] childpos = new double[2];

	nodeidx[0] = 0;
	if (tn instanceof Cluster) {
	    distance = ((Cluster) tn).getSimilarity();
	} else {
	    distance = ((DefaultMutableTreeNode) tn).getDepth();
	}
	segs = new double[getNodeCount(tn) * 8];
	// start recursive depth first traversal
	traverse(tn, 0, distance, nodeidx, nodemap, leafmap, segs, childpos);
	// set map of row index to leafnode ordinal
	idxMap.setIndex(leafmap);
	return segs;
    }

    /**
     * Map the data in the table to the display.
     */
    private void mapColumns() {
	if (tm != null) {
	    int ncols = tm.getColumnCount();
	    int nrows = tm.getRowCount();
	    colLM.clear();
	    labelModel.removeAllElements();
	    colMap = new int[ncols];
	    for (int c = 0, i = 0; c < ncols; c++) {
		String cn = tm.getColumnName(c);
		labelModel.addElement(cn);
		if (Number.class.isAssignableFrom(tm.getColumnClass(c))) {
		    colMap[i++] = c;
		    colLM.addElement(cn);
		}
	    }
	    colList.validate();
	    colList.setSelectionInterval(0, colList.getModel().getSize() - 1);
	    // labelModel.addElement("row index");
	    gs.setData(new double[0]);
	    graph.getXAxis().setMin(1.);
	    idxMap = new OneToOneIndexMap();
	    validate();
	    repaint();
	}
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
	    colList.setSelectedIndices(columns);
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
	if (tm == null)
	    return;
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

    @Override
    protected void finalize() throws Throwable {
	cleanUp();
	super.finalize();
    }

    @Override
    public void cleanUp() {
	if (lsm != null) {
	    lsm.removeListSelectionListener(selListener);
	}
	super.cleanUp();
    }

    public static void main(String[] args) {
	DefaultTableContext ctx = new DefaultTableContext();
	try {
	    FileTableModel ftm = new FileTableModel(args[0]);
	    ClusterView cv = new ClusterView();
	    ctx.addTableModel(ftm);
	    cv.setTableContext(ctx);
	    cv.setTableModel(ftm);
	    cv.setSelectionModel(ctx.getRowSelectionModel(ftm));
	    JFrame jf = ctx.getViewFrame("ClusterView", cv);
	    jf.pack();
	    jf.setVisible(true);
	} catch (Exception ex) {
	    System.err.println("" + ex);
	}
    }
}
