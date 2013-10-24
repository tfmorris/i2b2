/*
 * @(#) $RCSfile: ClutoView.java,v $ $Revision: 1.3 $ $Date: 2008/11/12 20:38:05 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import edu.umn.genomics.table.*;
import jcluto.*;

/**
 * ClutoView displays a hierarchical clustering of rows from a table. The
 * clustering is displayed as a Dendogram which is drawn as line segments on a
 * graph widget. The axes of the graph are zoomable. The row selection of the
 * table is displayed on the dendogram. The user can trace out a rectangle on
 * the dendogram to edit the row selection set for the table.
 * 
 * @author J Johnson
 * @author Shulan Tian
 * @version $Revision: 1.3 $ $Date: 2008/11/12 20:38:05 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */

public class ClutoView extends AbstractTableModelView implements Serializable {
    /**
     * Define a class that will execute Cluto Cluster Method and display the
     * Cluto Solution.
     */
    class SolutionPanel extends JPanel implements Runnable {
	ClutoMatrix cMat;
	ClutoParams cParam;
	TransParams tParam;
	ClutoSolution clutoSolution = null;
	int[] listIndex = colList.getSelectedIndices();
	Thread runThread = null;

	public SolutionPanel(ClutoMatrix cMat, TransParams tParam,
		ClutoParams cParam, int[] listIndex) {
	    String methodNames[] = { "VP_ClusterDirect", "VP_ClusterRB",
		    "VA_Cluster", "VA_ClusterBiased", "VP_ClusterRBTree" };
	    this.cMat = cMat;
	    this.tParam = tParam;
	    this.cParam = cParam;
	    this.listIndex = listIndex;

	    setName(methodNames[cParam.getClusterMethod()] + " "
		    + cParam.getNumClusters());
	    // Use BorderLayout.CENTER to use all available space
	    setLayout(new BorderLayout());
	    // initialize panel to an initialization label
	    setComponent(new JLabel("Clustering not started"));
	}

	public ClutoSolution getSolution() {
	    return clutoSolution;
	}

	public ClutoParams getClutoParams() {
	    return cParam;
	}

	public TransParams getTransParams() {
	    return tParam;
	}

	public ClutoMatrix getClutoMatrix() {
	    return cMat;
	}

	public int[] getColList() {
	    return listIndex;
	}

	/**
	 * Replace the component displayed
	 */
	private void setComponent(JComponent comp) {
	    removeAll();
	    add(comp, BorderLayout.CENTER);
	    validate();
	}

	/**
	 * Execute the clustering method
	 */
	public void run() {
	    runThread = Thread.currentThread();
	    // Make sure all data is available
	    for (int i = 0;; i++) {
		boolean allMapped = true;
		for (int c = 0; c < colMap.length; c++) {
		    ColumnMap cmap = getTableContext().getColumnMap(
			    getTableModel(), colMap[c]);
		    if (cmap.getState() != CellMap.MAPPED) {
			allMapped = false;
			break;
		    }
		}
		if (allMapped) {
		    break;
		} else {
		    // replace Message
		    if (i == 0) {
			setComponent(new JLabel("Waiting for table data"));
		    }
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException iex) {
		    }
		}
	    }
	    // If the matrix is not dense and tParam is not null
	    if (tParam != null) {
		setComponent(new JLabel("Processing Matrix"));
		ProcessMatrix process = new ProcessMatrix(cMat, tParam);
		cMat = process.getTransformedMatrix();
	    }

	    // replace Message
	    setComponent(new JLabel("Clustering running"));
	    // get the clustering solution
	    try {
		clutoSolution = getClutoSolution(cMat, cParam);
		// get the available views for the clustering solution
		if (runThread == Thread.currentThread()) {
		    setComponent(getSolutionViews(clutoSolution, tParam,
			    listIndex));
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
		setComponent(new JLabel(ex.toString()));
		// display error message
	    }
	    runThread = null;
	}

	public void cleanUp() {
	    if (runThread != null) {
		// runThread.interrupt();
		runThread = null;
	    }
	}
    } // end of solution panel

    /**
     * Allows view component to be dragged into separate frames.
     */
    class TearOff extends JToolBar implements CleanUp {
	public TearOff(String title) {
	    super(title);
	}

	public void addNotify() {
	    super.addNotify();
	    Container c = getTopLevelAncestor();
	    if (c instanceof Dialog) {
		((Dialog) c).setResizable(true);
		DefaultTableContext.setViewToolBar((Window) c, this
			.getComponent(0));
	    } else {
	    }
	}

	public void cleanUp() {
	    Container c = getTopLevelAncestor();
	    if (c instanceof Dialog) {
		Dialog d = (Dialog) c;
		WindowEvent we = new WindowEvent(d, WindowEvent.WINDOW_CLOSING);
		WindowListener[] wl = d.getWindowListeners();
		if (wl != null) {
		    for (int i = 0; i < wl.length; i++) {
			wl[i].windowClosing(we);
		    }
		}
		d.dispose();
	    }
	}
    }

    // The columns to show when the table is mapped
    int[] pendingCols = null;
    // A mapping of selectable column indices to tablemodel column indices
    int[] colMap = null;

    DefaultListModel colLM = new DefaultListModel();
    final JList colList = new JList(colLM);

    // Panel to select clustering options
    final ClutoOptionsPanel clutoOptions = new ClutoOptionsPanel(
	    ClutoParams.VP_ClusterRB);

    // Panel to select clustering options
    TransMatrixPanel transOptions = new TransMatrixPanel(TransParams.KNN);

    // A JTabbedPane that has a tab for each clustering we have performed
    JTabbedPane solTP = new JTabbedPane();

    // Hashtable listeners // solution : List [ ims, listener ]
    Hashtable csListenHt = new Hashtable(); // key ClutoSolution element Vector
					    // (IndexMapSelection )

    private Vector getSolutionListenerList(ClutoSolution clutoSolution) {
	if (clutoSolution != null) {
	    Vector v = (Vector) csListenHt.get(clutoSolution);
	    if (v == null) {
		v = new Vector();
		csListenHt.put(clutoSolution, v);
	    }
	    return v;
	}
	return null;
    }

    private void addSolutionListener(ClutoSolution clutoSolution, CleanUp obj) {
	if (clutoSolution != null) {
	    getSolutionListenerList(clutoSolution).add(obj);
	}
    }

    private void addSolutionListener(ClutoSolution clutoSolution,
	    ListSelectionListener obj) {
	if (clutoSolution != null) {
	    getSolutionListenerList(clutoSolution).add(obj);
	}
    }

    // removeSolution
    // remove listeners
    private void removeSolution(ClutoSolution clutoSolution) {
	if (clutoSolution != null) {
	    Vector v = getSolutionListenerList(clutoSolution);
	    for (Enumeration e = v.elements(); e.hasMoreElements();) {
		Object obj = e.nextElement();
		if (obj instanceof CleanUp) {
		    ((CleanUp) obj).cleanUp();
		} else if (obj instanceof ListSelectionListener) {
		    getSelectionModel().removeListSelectionListener(
			    (ListSelectionListener) obj);
		}
	    }
	    csListenHt.remove(clutoSolution);
	}
    }

    // removeAllListeners() {
    public void cleanUp() {
	super.cleanUp();
	// for each key removeSolution
	for (Enumeration e = csListenHt.keys(); e.hasMoreElements();) {
	    removeSolution((ClutoSolution) e.nextElement());
	}
    }

    /**
     * Get the current values for cLUTO clustering parameters.
     * 
     * @return The Cluto Clustering parameter settings.
     */
    private ClutoParams getClutoParams() {
	return new ClutoOptions(clutoOptions);
    }

    private TransParams getTransParams() {
	return new TransOptions(transOptions);
    }

    /**
     * Return a ClutoMatrix of the selected columns
     * 
     * @return a ClutoMatrix of the selected columns
     */
    private ClutoColumnMapMatrix getClutoMatrix() {
	int[] selcols = colList.getSelectedIndices();
	for (int i = 0; i < selcols.length; i++) {
	    selcols[i] = colMap[selcols[i]];
	}
	if (selcols != null && selcols.length > 0) {
	    ColumnMap cmap[] = new ColumnMap[selcols.length];
	    for (int c = 0; c < selcols.length; c++) {
		cmap[c] = getTableContext().getColumnMap(getTableModel(),
			selcols[c]);
	    }
	    return new ClutoColumnMapMatrix(cmap);
	}
	return null;
    }

    /**
     * Add a ClutoSolution to the JTabbedPanel
     */
    private void addClutoSolution() {
	// Check for valid cluto parameters, lest we suffer an ugly death
	ClutoColumnMapMatrix cMat = getClutoMatrix();
	try {
	    if (!cMat.isMapped()) {
		throw new Exception("Matrix still being read");
	    }
	    ValidateClutoParams.isValid(cMat, getTransParams(),
		    getClutoParams());
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex.toString(),
		    "Invalid Clustering Parameters", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	// create new SolutionPanel solPanel
	SolutionPanel solPanel = new SolutionPanel(cMat, getTransParams(),
		getClutoParams(), colList.getSelectedIndices());
	// add it to solTP
	solTP.add(solPanel);
	solTP.setSelectedComponent(solPanel);
	// exec:
	new Thread(solPanel).start();
    }

    /**
     * Replace the ClutoSolution in the JTabbedPanel
     */
    private void replaceClutoSolution() {
	// Check for valid cluto parameters, lest we suffer an ugly death
	ClutoColumnMapMatrix cMat = getClutoMatrix();
	try {
	    if (!cMat.isMapped()) {
		throw new Exception("Matrix still being read");
	    }
	    ValidateClutoParams.isValid(cMat, getTransParams(),
		    getClutoParams());
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex.toString(),
		    "Invalid Clustering Parameters", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	// create new SolutionPanel solPanel
	SolutionPanel solPanel = new SolutionPanel(cMat, getTransParams(),
		getClutoParams(), colList.getSelectedIndices());
	// Stop clustering
	((SolutionPanel) solTP.getSelectedComponent()).cleanUp();
	// remove listeners
	removeSolution(((SolutionPanel) solTP.getSelectedComponent())
		.getSolution());
	// get SolutionPanel solPanel
	int Index = solTP.getSelectedIndex();
	// remove the selected (SolutionPanel) tab from the solTP
	solTP.removeTabAt(Index);
	// add new solution to the selected
	solTP.add(solPanel, Index);
	// exec:
	new Thread(solPanel).start();
    }

    /**
     * Delete the ClutoSolution from the JTabbedPanel
     */
    private void deleteClutoSolution() {
	// Stop clustering
	((SolutionPanel) solTP.getSelectedComponent()).cleanUp();
	// remove listeners
	removeSolution(((SolutionPanel) solTP.getSelectedComponent())
		.getSolution());
	// remove the selected (SolutionPanel) tab from the solTP
	int Index = solTP.getSelectedIndex();
	solTP.removeTabAt(Index);
    }

    /**
     * Clear the ClutoOptionPanel to give the default value for selected method
     */
    private void clearClutoSolution() {
	clutoOptions.setDefaultValues(getClutoParams().getClusterMethod());
	transOptions.setDefaultValues(getTransParams().getEstMethod());
	colList.addSelectionInterval(0, colList.getModel().getSize() - 1);
	clutoOptions.setMatrixSize(tm.getRowCount(), colList
		.getSelectedValues().length);
    }

    /**
   *  
   */
    private ClutoSolution getClutoSolution(ClutoMatrix cMat, ClutoParams cParam) {
	ClutoCluster clutoCluster = new ClutoCluster(cMat, cParam);
	clutoCluster.execute();
	return clutoCluster;
    }

    /**
     * Get the available view displays for Cluto Solutions
     */
    private JComponent getSolutionViews(ClutoSolution clutoSolution,
	    TransParams tParams, int[] listIndex) {
	PartitionIndexMap partMap = new PartitionIndexMap(clutoSolution
		.getParts());
	JTabbedPane viewTabs = new JTabbedPane();
	// Matrix
	viewTabs.add("Matrix", getFloatingComponent(clutoSolution,
		getAfMatrix(clutoSolution), "Matrix"));
	// Cluster Summary
	// JTextArea with params, Solution Quality, and Cluster Stats
	viewTabs.add("Summary", getFloatingComponent(clutoSolution,
		getSolutionSummary(clutoSolution, tParams, listIndex),
		"Summary"));

	// Cluster Stats
	viewTabs.add("Cluster Stats", getFloatingComponent(clutoSolution,
		getClusterStatsTable(clutoSolution, partMap), "Cluster Stats"));
	// Cluster FeatureS
	viewTabs.add("Cluster Features", getFloatingComponent(clutoSolution,
		getClusterFeaturesTable(clutoSolution, partMap),
		"Cluster Features"));

	viewTabs.add("Cluster Graphs", getFloatingComponent(clutoSolution,
		getClusterGraphs(clutoSolution, partMap), "Cluster Graphs"));
	// Full Table
	// Tree
	viewTabs.add("Tree", getFloatingComponent(clutoSolution,
		getFullTreeView(clutoSolution), "Tree"));
	// Matrix
	viewTabs.add("LogRatio Tree Matrix", getFloatingComponent(
		clutoSolution, getLogRatioView(clutoSolution),
		"LogRatio Tree Matrix"));
	// Matrix
	viewTabs.add("Tree Matrix", getFloatingComponent(clutoSolution,
		getMatrixView(clutoSolution), "Tree Matrix"));
	viewTabs.add("Normalized Matrix", getFloatingComponent(clutoSolution,
		getIntMatrix(clutoSolution), "Normalized Matrix"));

	// Tree and Matrix
	// Mountains
	return viewTabs;
    }

    /*
     * Future buttons to add: SaveTable, SaveTableAs ClutoMatrix
     */

    /**
     * Get a component that can be dragged into a separate frame.
     * 
     * @param clutoSolution
     *            The cluto solution that is represents
     * @param comp
     *            The component
     * @param title
     *            The title for the dialog frame.
     */
    private JComponent getFloatingComponent(ClutoSolution clutoSolution,
	    JComponent comp, String title) {
	if (!true)
	    return comp;
	TearOff tb = new TearOff(title);
	tb.add(comp);
	addSolutionListener(clutoSolution, tb);
	JPanel pnl = new JPanel(new GridLayout(1, 1));
	// JLayeredPane pnl = new JLayeredPane();
	pnl.add(tb);
	return pnl;
    }

    /**
     * A view of the ClutoSolution that displays graphs of Cluto Clusters
     */
    private JComponent getSolutionSummary(ClutoSolution clutoSolution,
	    TransParams tParams, int[] listIndex) {
	DecimalFormat df = new DecimalFormat("#.###");
	ClutoMatrix cm = clutoSolution.getMatrix();
	ClutoParams cp = clutoSolution.getParams();
	JTextArea text = new JTextArea();
	StringBuffer sb = new StringBuffer();
	sb.append(JClutoWrapper.getCopyright()).append("\n");
	sb.append("\n");
	sb.append("Matrix Information").append("\n");
	sb.append("  #Rows: ").append(cm.getRowCount());
	sb.append("  #Columns: ").append(cm.getColumnCount());
	sb.append("  #NonZeros: ").append(cm.getValueCount()).append("\n");
	if (listIndex != null) {
	    String lbl = "  Columns: ";
	    sb.append(lbl).append("\t");
	    for (int i = 0, l = lbl.length(); i < listIndex.length; i++) {
		String name = "" + colLM.get(listIndex[i]);
		if (i > 0) {
		    sb.append(", ");
		    l += 2;
		}
		l += name.length();
		if (l + name.length() > 80) {
		    sb.append("\n").append("\t");
		    l = lbl.length();
		}
		sb.append(name);
	    }
	    sb.append("\n");
	}
	sb.append("\n");

	if (tParams != null) {
	    sb.append("Matrix Transformation").append("\n");
	    sb.append("  Missing_Value_Estimation: ").append(
		    ClutoResource.getParamValueName("MISSING_VALUE_ESTIMATION",
			    tParams.getEstMethod()));
	    if (tParams.getEstMethod() == TransParams.KNN) {
		sb.append("  #Neighbors: ").append(tParams.getNumNeighbors());
	    }
	    if (tParams.getLogTrans()) {
		sb.append(",  ").append(
			ClutoResource.getName("MATRIX_TRANSFORM_LOG"));
	    }
	    if (tParams.getMeanCenter()) {
		sb
			.append(",  ")
			.append(
				ClutoResource
					.getName("MATRIX_TRANSFORM_MEAN_CENTERED"));
	    }
	    if (tParams.getMedianCenter()) {
		sb.append(",  ").append(
			ClutoResource
				.getName("MATRIX_TRANSFORM_MEDIAN_CENTERED"));
	    }
	    sb.append("\n\n");
	}
	/*
	 * This is how the Cluto Options are reported in the commandline
	 * application vcluster CLMethod=RB, CRfun=I2, SimFun=Cosine, #Clusters:
	 * 10 RowModel=None, ColModel=IDF, GrModel=SY-DIR, NNbrs=40
	 * Colprune=1.00, EdgePrune=-1.00, VtxPrune=-1.00, MinComponent=5
	 * CSType=Best, AggloFrom=0, AggloCRFun=I2, NTrials=10, NIter=10
	 */
	sb.append("Options").append("\n");
	sb.append("  CLMethod=").append(
		ClutoResource.getParamValueName("CLUTO_CLUSTER_METHOD", cp
			.getClusterMethod())).append(",");
	sb.append("  CRfun=").append(
		ClutoResource.getParamValueName("CLUTO_CLFUN", cp.getCrFunc()))
		.append(",");
	sb.append("  SimFun=").append(
		ClutoResource.getParamValueName("CLUTO_SIM", cp.getSimFunc()))
		.append(",");
	sb.append("  #Clusters: ").append(cp.getNumClusters()).append("\n");
	sb.append("  RowModel=").append(
		ClutoResource.getParamValueName("CLUTO_ROWMODEL", cp
			.getRowModel())).append(",");
	sb.append("  ColModel=").append(
		ClutoResource.getParamValueName("CLUTO_COLMODEL", cp
			.getColModel())).append(",");
	sb.append("  Colprune=").append(cp.getColPrune()).append("\n");
	sb.append("  CSType=")
		.append(
			ClutoResource.getParamValueName("CLUTO_CSTYPE", cp
				.getCsType())).append(",");
	sb.append("  AggloFrom=").append(cp.getAggloFrom()).append(",");
	sb.append("  AggloCRFun=").append(
		ClutoResource.getParamValueName("CLUTO_CLFUN", cp
			.getAggloCrFunc())).append(",");
	sb.append("  NTrials=").append(cp.getNumTrials()).append(",");
	sb.append("  NIter=").append(cp.getNumIter()).append("\n");
	sb.append("\n");

	sb.append("Solution").append("\n");
	sb.append(" ").append(cp.getNumClusters()).append("-way clustering: [");
	sb.append(" ").append("=").append(clutoSolution.getSolutionQuality())
		.append("]").append("\n");
	sb.append("\n");
	ClusterStatsTable stm = new ClusterStatsTable(clutoSolution);
	String colNames[] = { "cid", "size", "ISim", "ISdev", "ESim", "ESdev" };
	for (int c = 0; c < stm.getColumnCount(); c++) {
	    // sb.append("\t").append(stm.getColumnName(c));
	    sb.append("\t").append(colNames[c]);
	}
	sb.append("\n");
	for (int r = 0; r < stm.getRowCount(); r++) {
	    for (int c = 0; c < stm.getColumnCount(); c++) {
		sb.append("\t")
			.append(
				df.format(((Number) stm.getValueAt(r, c))
					.floatValue()));
	    }
	    sb.append("\n");
	}
	sb.append("\n");

	if (clutoSolution instanceof ClutoCluster) {
	    sb.append("Timing").append("\n");
	    sb.append("\tclustering\t").append(
		    ((ClutoCluster) clutoSolution).getClusterTime()).append(
		    " secs\n");
	}

	text.append(sb.toString());
	JScrollPane jsp = new JScrollPane(text);
	return jsp;
    }

    /**
     * A view of the ClutoSolution that displays graphs of Cluto Clusters
     */
    private JComponent getClusterGraphs(ClutoSolution clutoSolution,
	    PartitionIndexMap partMap) {
	ClutoGraphView cgv = new ClutoGraphView(clutoSolution, partMap,
		getSelectionModel());
	addSolutionListener(clutoSolution, cgv);
	return cgv;
    }

    /**
     * A view of the ClutoSolution that displays the Cluster Statisitics
     */
    private JComponent getClusterStatsTable(ClutoSolution clutoSolution,
	    PartitionIndexMap partMap) {
	// If we select a cluster row from this table, it should select the
	// related rows in our full tableview table
	// It should also listen to the tableview row selection and
	// select cluster row when all releated tableview rows are selected
	//
	ClusterStatsTable tm = new ClusterStatsTable(clutoSolution);
	JTable jt = new JTable(tm); // We could also use JTableView if we want
				    // sortable columns
	IndexMapSelection ims = new IndexMapSelection(getSelectionModel(), jt
		.getSelectionModel(), partMap);
	// Register this so it eventually get reomoved
	addSolutionListener(clutoSolution, ims);
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	JScrollPane jsp = new JScrollPane(jt);
	return jsp;
    }

    /**
     * A view of the ClutoSolution that displays the Cluster features
     */
    private JComponent getClusterFeaturesTable(ClutoSolution clutoSolution,
	    PartitionIndexMap partMap) {
	// Use nfeatures=5 as default
	// Create a ClusterFeatureTable from
	// clutoSolution.getClusterFeatures(featureCount)
	ClusterFeatureTable cft = new ClusterFeatureTable(clutoSolution
		.getClusterFeatures(5));
	// create a JTable
	JTable jt = new JTable(cft);
	// Add Cell Renderer for column number to Columnname
	// link up the ListSelectionModels with an IndexMapSelection
	IndexMapSelection ims = new IndexMapSelection(getSelectionModel(), jt
		.getSelectionModel(), partMap);
	// Register this so it eventually get reomoved
	addSolutionListener(clutoSolution, ims);
	jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	// put the JTable into a JScrollPane
	JScrollPane jsp = new JScrollPane(jt);
	return jsp;
    }

    /**
     * A view of the ClutoSolution that displays the Original data table with a
     * added column that shows the cluster assignment
     */
    private JComponent getFullTable(ClutoSolution clutoSolution) {
	// create an ArrayRefColumn partCol with the part array from the
	// clutoSolution
	// create a new VirtualTableModelProxy with getTableModel()
	// insert the partCol into it using: addColumn(VirtualColumn column, int
	// index)
	// Either
	// create a JTable
	// link up the ListSelectionModels with an IndexMapSelection
	// put the JTable into a JScrollPane
	// return the JScrollPane
	// Or
	// Can we just use a JTableView?
	//
	// Until implemented just return a JLabel:
	return new JLabel("FullTable not yet implemented");
    }

    // shulan test
    private JComponent getAfMatrix(ClutoSolution clutoSolution) {
	ClutoTableMatrix cMat = new ClutoTableMatrix(clutoSolution.getMatrix());
	VirtualTableModelProxy vMat = new VirtualTableModelProxy(cMat);
	ArrayRefColumn part = new ArrayRefColumn(clutoSolution.getParts());
	part.setName("Cluster ID");
	vMat.addColumn(part, 0);
	JTableView jt = new JTableView(vMat, getSelectionModel());
	// jt.setTableModel(cMat);
	// jt.setSelectionModel(getSelectionModel());
	addSolutionListener(clutoSolution, (CleanUp) jt);
	return jt;
    }

    private JComponent getFullTreeView(ClutoSolution clutoSolution) {
	ClutoTree ct = new ClutoTree(clutoSolution, ctx, tm);
	addSolutionListener(clutoSolution, ct);
	return ct;
    }

    private JComponent getLogRatioView(ClutoSolution clutoSolution) {
	ClutoTableMatrix cMat = new ClutoTableMatrix(clutoSolution.getMatrix());
	FontMetrics fm = null;
	try {
	    fm = this.getGraphics().getFontMetrics();
	} catch (Exception ex) {
	}
	ClutoMatrixView cmv = new ClutoMatrixView(clutoSolution, cMat, ctx, tm,
		fm, true, .33f);
	addSolutionListener(clutoSolution, cmv);
	return cmv;
    }

    private JComponent getMatrixView(ClutoSolution clutoSolution) {
	ClutoTableMatrix cMat = new ClutoTableMatrix(clutoSolution
		.getInternalMatrix());
	FontMetrics fm = null;
	try {
	    fm = this.getGraphics().getFontMetrics();
	} catch (Exception ex) {
	}
	ClutoMatrixView cmv = new ClutoMatrixView(clutoSolution, cMat, ctx, tm,
		fm);
	addSolutionListener(clutoSolution, cmv);
	return cmv;
    }

    private JComponent getIntMatrix(ClutoSolution clutoSolution) {
	ClutoTableMatrix cMat = new ClutoTableMatrix(clutoSolution
		.getInternalMatrix());
	FontMetrics fm = null;
	try {
	    fm = this.getGraphics().getFontMetrics();
	} catch (Exception ex) {
	}
	ClutoMatrixView cmv = new ClutoMatrixView(clutoSolution, cMat, ctx, tm,
		fm, false, 1.f);
	addSolutionListener(clutoSolution, cmv);
	return cmv;
    }

    /**
     * Constructs a ClutoView display. Nothing will be displayed until a data
     * model is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public ClutoView() {
	super();
	try {
	    init();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Constructs a ClutoView diaplay which is initialized with tableModel as
     * the data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public ClutoView(TableModel tableModel) {
	super(tableModel);
	init();
    }

    /**
     * Constructs a ClutoView diaplay which is initialized with tableModel as
     * the data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public ClutoView(TableModel tableModel, ListSelectionModel lsm) {
	super(tableModel, lsm);
	init();
    }

    /**
     * Sets tableModel as the data model for the view.
     * 
     * @param tableModel
     *            the data model for the view
     */
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
    public void setSelectionModel(ListSelectionModel newModel) {
	super.setSelectionModel(newModel);
    }

    /**
     * initialize the components in the view panel
     */
    private void init() {
	setLayout(new BorderLayout());
	solTP.setPreferredSize(new Dimension(550, 580));
	// Listen for which solution panel is selected
	ChangeListener solTpListener = new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		// get the selected (SolutionPanel) component
		SolutionPanel solP = (SolutionPanel) solTP
			.getSelectedComponent();
		// set the values in the ClutoOptionsPanel
		if (solP != null) {
		    clutoOptions.setValues(solP.getClutoParams());
		    int[] si = solP.getColList();
		    colList.clearSelection();
		    for (int i = 0; i < si.length; i++) {
			colList.addSelectionInterval(si[i], si[i]);
		    }
		    transOptions.setValues(solP.getTransParams());
		}
	    }
	};
	solTP.addChangeListener(solTpListener);

	colList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    clutoOptions.setMatrixSize(tm.getRowCount(), colList
			    .getSelectedValues().length);
		}
	    }
	});

	// Panel for Cluster Buttons: Add, Replace, Delete
	JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER));
	// Buttons to add to the Panel
	final JButton addBtn = new JButton("Add");
	addBtn.setToolTipText("Add a new solution");
	final JButton replaceBtn = new JButton("Replace");
	replaceBtn.setToolTipText("Replace a new solution");
	final JButton deleteBtn = new JButton("Delete");
	deleteBtn.setToolTipText("Delete a new solution");
	final JButton clearBtn = new JButton("Clear");
	clearBtn.setToolTipText("Default setting for the selected method");

	btnP.add(addBtn);
	btnP.add(replaceBtn);
	replaceBtn.setEnabled(false);
	btnP.add(deleteBtn);
	deleteBtn.setEnabled(false);
	btnP.add(clearBtn);
	clearBtn.setEnabled(true);

	// "Add" Action: addClutoSolution
	addBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		addClutoSolution();
		replaceBtn.setEnabled(true);
		deleteBtn.setEnabled(true);
	    }
	});

	// "Replace" Action: replaceClutoSolution
	replaceBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		replaceClutoSolution();
	    }
	});

	// "Delete" Action: deleteClutoSolution
	deleteBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		deleteClutoSolution();
		int numPane = solTP.getTabCount();
		if (numPane == 0) {
		    replaceBtn.setEnabled(false);
		    deleteBtn.setEnabled(false);
		}
	    }
	});

	// Action: clearClutoSolution
	clearBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		clearClutoSolution();
	    }
	});

	JPanel left = new JPanel(new GridLayout(0, 1));
	left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
	JPanel right = new JPanel(new GridLayout(0, 1));
	right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
	JScrollPane listPane = new JScrollPane(colList);
	listPane.setToolTipText("Select columns to be clustered");
	listPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Select Columns"));
	left.add(listPane);
	left.add(transOptions);
	right.add(clutoOptions);
	right.add(btnP);

	// JPanel top = new JPanel();
	// GridBagLayout gb = new GridBagLayout();
	// GridBagConstraints c = new GridBagConstraints();
	// top.setLayout(gb);
	// top.add(left);
	// top.add(right);

	JToolBar tb = new JToolBar();
	// tb.add(top);
	tb.add(left);
	tb.add(right);

	JPanel cPnl = new JPanel(new BorderLayout());
	// cPnl.add(btnP,BorderLayout.NORTH);
	solTP.setMinimumSize(new Dimension(250, 250));
	cPnl.add(solTP, BorderLayout.CENTER);

	// NORTH All components for Generating a Cluto Solution
	add(tb, BorderLayout.NORTH);
	// CENTER the Solutions Panel
	add(cPnl, BorderLayout.CENTER);
    }

    private void mapColumns() {
	if (tm != null) {
	    int ncols = tm.getColumnCount();
	    colLM.clear();
	    colMap = new int[ncols];
	    for (int c = 0, i = 0; c < ncols; c++) {
		String cn = tm.getColumnName(c);
		if (Number.class.isAssignableFrom(tm.getColumnClass(c))) {
		    colMap[i++] = c;
		    colLM.addElement(cn);
		}
	    }
	    colList.validate();
	    if (pendingCols != null) {
		selectColumns(pendingCols);
	    } else {
		colList.setSelectionInterval(0,
			colList.getModel().getSize() - 1);
	    }
	    // Set ClutoOptions to initial values
	    // Remove existing Solutions
	    validate();
	    repaint();
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
	    clutoOptions.setMatrixSize(tm.getRowCount(), tm.getColumnCount());
	    lsm.clearSelection();
	    repaint();
	    return;
	}
	if (e.getType() == TableModelEvent.DELETE) {
	    clutoOptions.setMatrixSize(tm.getRowCount(), tm.getColumnCount());
	    lsm.clearSelection();
	    repaint();
	    return;
	}
	if (e.getType() == TableModelEvent.UPDATE) {
	    lsm.clearSelection();
	    repaint();
	    return;
	}
	repaint();
    }

    /**
     * Translate the table column indices to the number list indices.
     */
    private void selectColumns(int[] columns) {
	if (columns != null) {
	    if (colMap != null) {
		BitSet selbits = new BitSet(colMap.length);
		for (int c = 0; c < colMap.length; c++) {
		    for (int i = 0; i < columns.length; i++) {
			if (colMap[c] == columns[i]) {
			    selbits.set(c);
			    break;
			}
		    }
		}
		int sel[] = new int[selbits.cardinality()];
		for (int i = 0, n = 0; i < selbits.length(); i++) {
		    if (selbits.get(i)) {
			sel[n++] = i;
		    }
		}
		colList.setSelectedIndices(sel);
	    }
	}
    }

    /**
     * Set the view to display the columns at the TableModel columns indices
     * (numbered from 0 to number of columns - 1).
     * 
     * @param columns
     *            the indices of the columns to display.
     */
    public void setColumns(int[] columns) {
	pendingCols = columns;
	if (columns != null) {
	    selectColumns(columns);
	} else {
	    colList.clearSelection();
	}
    }

    protected void finalize() throws Throwable {
	super.finalize();
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    public Component getCanvas() {
	SolutionPanel pnl = (SolutionPanel) solTP.getSelectedComponent();
	Component comp = pnl.getComponent(0);
	if (comp instanceof JTabbedPane) {
	    Component c = ((JTabbedPane) comp).getSelectedComponent();
	    if (c != null) {
		return c;
	    }
	}
	return this;
    }

    public static void main(String[] args) {
	DefaultTableContext ctx = new DefaultTableContext();
	try {
	    FileTableModel ftm = new FileTableModel(args[0]);
	    ClutoView cv = new ClutoView();
	    ctx.addTableModel(ftm);
	    cv.setTableContext(ctx);
	    cv.setTableModel(ftm);
	    cv.setSelectionModel(ctx.getRowSelectionModel(ftm));
	    JFrame jf = ctx.getViewFrame("ClutoView", cv);
	    jf.pack();
	    jf.setVisible(true);
	} catch (Exception ex) {
	    System.err.println("" + ex);
	}
    }
}
