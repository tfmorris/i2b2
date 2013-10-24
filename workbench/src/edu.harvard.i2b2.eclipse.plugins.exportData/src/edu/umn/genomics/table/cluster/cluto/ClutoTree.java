/*
 * @(#) $RCSfile: ClutoTree.java,v $ $Revision: 1.3 $ $Date: 2008/11/12 20:38:05 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.cluster.cluto;

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import jcluto.*;
import edu.umn.genomics.table.*;
import edu.umn.genomics.table.cluster.*;
import edu.umn.genomics.graph.*;
import edu.umn.genomics.graph.swing.*;

/**
 * ClutoTree displays a hierarchical clustering of rows from a table. The
 * clustering is displayed as a Dendogram which is drawn as line segments on a
 * graph widget. The axes of the graph are zoomable. The row selection of the
 * table is displayed on the dendogram. The user can trace out a rectangle on
 * the dendogram to edit the row selection set for the table.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/12 20:38:05 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClutoTree extends JPanel implements CleanUp, Serializable {
    JToolBar btnP = null;
    TableContext ctx = null;
    TableModel tm = null;
    ListSelectionModel lsm = null;
    DefaultComboBoxModel labelModel = new DefaultComboBoxModel();
    JComboBox labelChoice = new JComboBox(labelModel);
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

    ListSelectionListener selListener = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (!e.getValueIsAdjusting()) {
		mapSelection();
	    }
	}
    };

    AxisLabeler labeler = new AxisLabeler() {
	public String getLabel(double value) {
	    return getLeafLabel(value);
	}
    };

    public IndexMap getIndexMap() {
	return idxMap;
    }

    public int[] getLeafMap() {
	return idxMap.getIndex();
    }

    public void setToolbar(boolean showToolbar) {
	if (showToolbar) {
	    add(btnP, BorderLayout.NORTH);
	} else {
	    remove(btnP);
	}
    }

    public void setShowLabels(boolean showLabels) {
	graph.showAxis(BorderLayout.EAST, showLabels);
    }

    public void setShowScale(boolean showScale) {
	graph.showAxis(BorderLayout.SOUTH, showScale);
    }

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
     * Constructs a ClutoTree diaplay which is initialized with tableModel as
     * the data model, and the given selection model.
     * 
     * @param clutoSolution
     *            The clustering solution
     * @param tableContext
     *            The context which manages views and selections.
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ClutoTree(ClutoSolution clutoSolution, TableContext tableContext,
	    TableModel tableModel) {
	ctx = tableContext;
	tm = tableModel;
	lsm = ctx.getRowSelectionModel(tm);

	// labelModel
	int ncol = tm.getColumnCount();
	for (int i = 0; i < ncol; i++) {
	    labelModel.addElement(tm.getColumnName(i));
	}

	setLayout(new BorderLayout());
	btnP = new JToolBar();
	add(btnP, BorderLayout.NORTH);
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
	btnP.add(labelChoice);

	graph = new SimpleGraph();
	graph.getGraphDisplay().setOpaque(true);
	graph.getGraphDisplay().setBackground(Color.white);
	graph.getGraphDisplay().setGridColor(new Color(220, 220, 220));
	graph.showGrid(false);
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

	add(graph);
	if (lsm != null) {
	    lsm.addListSelectionListener(selListener);
	}
	display(makeTree(clutoSolution));
    }

    /**
     * Generate the hierarchical cluster and display it as a denogram in the
     * graph
     */
    public TreeNode makeTree(ClutoSolution cs) {
	int[] tsize = cs.getTreeCounts();
	int[][] ftree = cs.getForwardTree();
	int nnrows = tsize.length;
	int nrows = cs.getMatrix().getRowCount();

	// for (int i = 0; i < nnrows-1; i++) {
	// String s = "ftree" + "\t" + i + "\t" + ftree[i][0] + "\t" +
	// ftree[i][1] + "\t" + tsize[i];
	// System.out.println(s);
	// }

	Cluster[] ca = new Cluster[nnrows];
	for (int i = 0; i < nnrows - 1; i++) {

	    if (!true) {
		String s = "ftree" + "\t" + i + "\t" + ftree[i][0] + "\t"
			+ ftree[i][1] + "\t" + tsize[i];
		System.out.println(s);
	    }

	    Cluster cn = i < nrows ? (Cluster) new RowCluster(tm, i, null)
		    : new CompositeCluster();
	    cn.setSimilarity(Math.abs(tsize[i]));
	    ca[i] = cn;
	    if (ftree[i][0] > -1) {
		cn.add(ca[ftree[i][0]]);
	    }
	    if (ftree[i][0] > -1) {
		cn.add(ca[ftree[i][1]]);
	    }
	    rootNode = cn;
	}
	return rootNode;
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
	graph.getYAxis().setMin(tm.getRowCount() - .5);
	graph.getYAxis().setMax(-.5);
	repaint();
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

    @Override
    protected void finalize() throws Throwable {
	cleanUp();
	super.finalize();
    }

    public void cleanUp() {
	if (lsm != null) {
	    lsm.removeListSelectionListener(selListener);
	}
    }

}
