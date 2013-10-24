/*
 * @(#) $RCSfile: ClutoOptionsPanel.java,v $ $Revision: 1.3 $ $Date: 2008/11/21 17:18:38 $ $Name: RELEASE_1_3_1_0001b $
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

package jcluto;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* RULES:
 IDF column model only applies to sparse matrices and non-correlation-coefficient similarities!
 Similarity based on correlation coefficient requires more than two dimensions!
 */

/**
 * Display a panel from which to select clustering parameter options for
 * <b>CLUTO</b>.
 * 
 * @author Shulan Tian
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/21 17:18:38 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see JClutoWrapper
 */
public class ClutoOptionsPanel extends JPanel implements ClutoParams {
    static ResourceBundle rsrc = ResourceBundle.getBundle("jcluto.Cluto");
    static boolean hasJSpinner = false;
    static {
	try {
	    Class.forName("javax.swing.JSpinner");
	    hasJSpinner = true;
	} catch (Exception ex) {
	}
    }
    int dbglvl = 0;

    protected JComponent cluster_k;
    protected JComponent colprune;
    protected JComponent ntrial;
    protected JComponent niter;
    protected JComponent seed;
    protected JComponent mincmp;
    protected JComponent nnbrs;
    protected JComponent agglofrom;

    protected SimMetricChooser simMetric = new SimMetricChooser();
    protected RowModelChooser rmodelMetric = new RowModelChooser();
    protected ColModelChooser cmodelMetric = new ColModelChooser();
    protected CsTypeMetricChooser csTypeMetric = new CsTypeMetricChooser();

    protected AggloBox aggloBox = new AggloBox("Combining Agglo", false);
    protected ClusterMethodChooser methodOption = new ClusterMethodChooser();
    protected KwayRefine kwayRefine = new KwayRefine("Globally Optimize", false);

    public JPanel jp;
    public JPanel row1;
    public JPanel row2;
    public JPanel row3;
    public Box crbox;
    public Box csbox;
    public Box acbox;
    public Box mcluster;

    protected JPanel box5;
    protected CrMetricChooser crMetric = new CrMetricChooser(
	    CrMetricChooser.agMetrics);
    protected CrMetricChooser aggloCrMetric = new CrMetricChooser(
	    CrMetricChooser.agMetrics);

    /**
     * 
     * @param clusterMethod
     */
    public ClutoOptionsPanel(int clusterMethod) {

	// panel for clustering parameters
	jp = new JPanel(new GridLayout(0, 1));

	jp.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Clustering Methods and Parameters"));
	jp.setOpaque(false);
	add(jp);

	// row0 for clustering method

	JPanel row0 = new JPanel(new FlowLayout(FlowLayout.CENTER));

	methodOption.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setClusterMethod(methodOption.getSelectedIndex());
	    }
	});

	Box methodbox = Box.createVerticalBox();
	JLabel mlabel = new JLabel("Choose a Clustering Method");
	mlabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	methodbox.add(mlabel);
	methodbox.add(methodOption);

	row0.add(methodbox);
	jp.add(row0);

	// row1 for number of clusters, column prune, kwayrefine
	row1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
	cluster_k = getIntComponent(10, 1, 100, 1);
	cluster_k.setBorder(BorderFactory.createLoweredBevelBorder());
	cluster_k.setToolTipText("The desired number of clusters");
	row1.add(cluster_k);
	row1.add(new JLabel("number of clusters"));

	colprune = getFloatComponent(1.00f, 0.01f, 1.00f, .01f);
	colprune.setBorder(BorderFactory.createLoweredBevelBorder());
	colprune
		.setToolTipText("the factor by which the columns of the matrix will be pruned before clustering");
	row1.add(colprune);
	row1.add(new JLabel("column prune (0.0, 1.0]"));

	row1.add(kwayRefine);
	kwayRefine.setVisible(false);

	jp.add(row1);

	// row2 For VPClusterDirect, VPClusterRB, RB tree
	row2 = new JPanel(new FlowLayout(FlowLayout.CENTER));

	ntrial = getIntComponent(10, 1, 100, 1);
	ntrial.setBorder(BorderFactory.createLoweredBevelBorder());
	row2.add(ntrial);
	row2.add(new JLabel("number of trials"));

	niter = getIntComponent(10, 1, 100, 1);
	niter.setBorder(BorderFactory.createLoweredBevelBorder());
	row2.add(niter);
	row2.add(new JLabel("number of iterations"));

	seed = getIntComponent(10, 1, 100, 1);
	seed.setBorder(BorderFactory.createLoweredBevelBorder());
	row2.add(seed);
	row2.add(new JLabel("seed number"));

	aggloBox.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    acbox.setVisible(true);
		    mcluster.setVisible(true);
		} else {
		    acbox.setVisible(false);
		    mcluster.setVisible(false);
		}
	    }
	});
	row2.add(aggloBox);
	aggloBox.setVisible(true);

	// row3 for similiarity functions, row model, criterion function
	// cstype for RB
	// mcluster, agglo crfun for ClusterDirect, clusterRB

	row3 = new JPanel(new FlowLayout(FlowLayout.CENTER));

	// box for # of m clusters

	mcluster = Box.createVerticalBox();
	agglofrom = getIntComponent(20, 1, 100, 1);
	JLabel label = new JLabel("# of mcluster");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	mcluster.add(label);
	agglofrom.setToolTipText("First divide into number of clusters");
	mcluster.add(agglofrom);
	row3.add(mcluster);
	mcluster.setVisible(false);

	// box for agglo Cr Metrics
	aggloCrMetric.setToolTipText("Criterion function for agglo process");
	aggloCrMetric.setSelectedIndex(11);
	acbox = Box.createVerticalBox();
	label = new JLabel("Agglo CrMetric");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	acbox.add(label);
	acbox.add(aggloCrMetric);
	row3.add(acbox);
	acbox.setVisible(false);

	simMetric.setToolTipText("Similarity function");
	Box box1 = Box.createVerticalBox();
	label = new JLabel("Similarity Metric");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	box1.add(label);
	box1.add(simMetric);
	row3.add(box1);

	rmodelMetric
		.setToolTipText("the model to be used for scaling various columns of each row");
	Box box2 = Box.createVerticalBox();
	label = new JLabel("row model");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	box2.add(label);
	box2.add(rmodelMetric);
	row3.add(box2);

	crMetric.setToolTipText("Criterion function");
	crbox = Box.createVerticalBox();
	label = new JLabel("Criterion Metric");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	crbox.add(label);
	crMetric.setChoices(CrMetricChooser.paMetrics);
	crbox.add(crMetric);
	row3.add(crbox);

	csTypeMetric
		.setToolTipText("method for selecting the next cluster to be bisected");
	csbox = Box.createVerticalBox();
	label = new JLabel("Cluster Selection");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	csbox.add(label);
	csbox.add(csTypeMetric);

	row3.add(csbox);
	csbox.setVisible(false);

	jp.add(row1);
	jp.add(row2);
	jp.add(row3);

    }

    /**
     * Set the <b>CLUTO</b> clustering method and set clustering option
     * parameter choices to be consistent with this clustering method.
     * 
     * @param clusterMethod
     *            The <b>CLUTO</b> Cluster Method
     * @see ClutoParams#VA_Cluster
     * @see ClutoParams#VA_ClusterBiased
     * @see ClutoParams#VP_ClusterDirect
     * @see ClutoParams#VP_ClusterRB
     * @see ClutoParams#VP_ClusterRBTree
     */
    public void setClusterMethod(int clusterMethod) {
	if (methodOption.getSelectedIndex() != clusterMethod) {
	    methodOption.setValue(clusterMethod);
	}

	// Make sure the parameter options are set consistently for this
	// Clustering Method.

	switch (clusterMethod) {

	case ClutoParams.VP_ClusterDirect:
	    crMetric.setChoices(CrMetricChooser.paMetrics);
	    kwayRefine.setVisible(false);
	    aggloBox.setVisible(true);
	    if (aggloBox.isSelected()) {
		acbox.setVisible(true);
		mcluster.setVisible(true);
	    } else {
		acbox.setVisible(false);
		mcluster.setVisible(false);
	    }
	    csbox.setVisible(false);
	    row2.setVisible(true);

	    break;

	case ClutoParams.VP_ClusterRB:
	    crMetric.setChoices(CrMetricChooser.paMetrics);
	    kwayRefine.setVisible(true);
	    csbox.setVisible(true);
	    aggloBox.setVisible(true);
	    row2.setVisible(true);
	    if (aggloBox.isSelected()) {
		acbox.setVisible(true);
		mcluster.setVisible(true);
	    } else {
		acbox.setVisible(false);
		mcluster.setVisible(false);
	    }
	    break;

	case ClutoParams.VA_Cluster:
	    crMetric.setChoices(CrMetricChooser.agMetrics);
	    csbox.setVisible(false);
	    row2.setVisible(false);
	    kwayRefine.setVisible(false);
	    acbox.setVisible(false);
	    mcluster.setVisible(false);
	    break;

	case ClutoParams.VA_ClusterBiased:
	    crMetric.setChoices(CrMetricChooser.agMetrics);
	    csbox.setVisible(false);
	    row2.setVisible(false);
	    kwayRefine.setVisible(false);
	    acbox.setVisible(false);
	    mcluster.setVisible(false);
	    break;

	case ClutoParams.VP_ClusterRBTree:
	    crMetric.setChoices(CrMetricChooser.paMetrics);
	    crMetric.setSelectedIndex(1);
	    csbox.setVisible(false);
	    kwayRefine.setVisible(false);
	    aggloBox.setVisible(false);
	    row2.setVisible(true);
	    acbox.setVisible(false);
	    mcluster.setVisible(false);
	    break;

	default:
	    break;
	}
    }

    // ClutoParams Interface
    public void setValues(ClutoParams params) {
	setClusterMethod(params.getClusterMethod());
	simMetric.setValue(params.getSimFunc());
	crMetric.setValue(params.getCrFunc());
	csTypeMetric.setValue(params.getCsType());
	rmodelMetric.setValue(params.getRowModel());
	cmodelMetric.setValue(params.getColModel());
	setComponentValue(colprune, params.getColPrune());
	setComponentValue(ntrial, params.getNumTrials());
	setComponentValue(niter, params.getNumIter());
	setComponentValue(seed, params.getSeed());
	kwayRefine.setValue(params.getKwayRefine());
	aggloBox.setValue(params.showAgglomerativeParameters());
	setComponentValue(nnbrs, params.getNumNeighbors());
	setComponentValue(mincmp, params.getMinCmp());
	setComponentValue(cluster_k, params.getNumClusters());
    }

    // ClutoParams (default value)

    public void setDefaultValues(int clusterMethod) {

	setComponentValue(colprune, 1.0f);
	setComponentValue(ntrial, 10);
	setComponentValue(niter, 10);
	setComponentValue(seed, 10);
	kwayRefine.setValue(0);
	aggloBox.setValue(0);
	setComponentValue(cluster_k, 10);

	csTypeMetric.setSelectedIndex(0);

	simMetric.setSelectedIndex(0);
	rmodelMetric.setSelectedIndex(0);

	if (methodOption.getSelectedIndex() != clusterMethod) {
	    methodOption.setValue(clusterMethod);
	}

	switch (clusterMethod) {

	case ClutoParams.VP_ClusterDirect:
	case ClutoParams.VP_ClusterRB:
	case ClutoParams.VP_ClusterRBTree:

	    crMetric.setSelectedIndex(0);
	    break;

	case ClutoParams.VA_Cluster:
	case ClutoParams.VA_ClusterBiased:

	    crMetric.setSelectedIndex(11);
	    break;

	default:
	    break;

	}
    }

    public int getClusterMethod() {
	return methodOption.getValue();
    }

    public int getSimFunc() {
	return simMetric.getValue();
    }

    public int getCrFunc() {
	return crMetric.getValue();
    }

    public int getCsType() {
	return csTypeMetric.getValue();
    }

    public int getRowModel() {
	return rmodelMetric.getValue();
    }

    public int getColModel() {
	return cmodelMetric.getValue();
    }

    public float getColPrune() {
	return getComponentValue(colprune, 1f);
    }

    public int getNumTrials() {
	return getComponentValue(ntrial, 10);
    }

    public int getNumIter() {
	return getComponentValue(niter, 10);
    }

    public int getSeed() {
	return getComponentValue(seed, 999);
    }

    public int getKwayRefine() {
	return kwayRefine.getValue();
    }

    public int showAgglomerativeParameters() {
	return aggloBox.getValue();
    }

    public int getNumNeighbors() {
	return getComponentValue(nnbrs, 10);
    }

    public int getMinCmp() {
	return getComponentValue(mincmp, 10);
    }

    public int getNumClusters() {
	return getComponentValue(cluster_k, 10);
    }

    public int getAggloFrom() {
	return getComponentValue(agglofrom, getNumClusters() * 2);
    }

    public int getAggloCrFunc() {
	if (aggloCrMetric != null)
	    return aggloCrMetric.getValue();
	return getCrFunc();
    }

    public int getDbgLvl() {
	return dbglvl;
    }

    private void setIntComponent(JComponent comp, int value, int min, int max,
	    int step) {
	if (hasJSpinner) {
	}
    }

    private JComponent getIntComponent(int value, int min, int max, int step) {
	if (hasJSpinner) {
	    // return new JSpinner(new SpinnerNumberModel(value, min, max,
	    // step));
	    return DoSpinner.getComponent(value, min, max, step);
	}
	return new JTextField(Integer.toString(value), Integer.toString(max)
		.length());
    }

    private JComponent getFloatComponent(double value, double min, double max,
	    double step) {
	if (hasJSpinner) {
	    // return new JSpinner(new SpinnerNumberModel(value, min, max,
	    // step));
	    return DoSpinner.getComponent(value, min, max, step);
	}
	return new JTextField(Float.toString((float) value), Float.toString(
		(float) max + (float) step).length());
    }

    private String getComponentValue(JComponent comp) {
	try {
	    if (comp instanceof JTextField) {
		return ((JTextField) comp).getText();
	    } else {
		if (Class.forName("javax.swing.JSpinner").isAssignableFrom(
			comp.getClass())) {
		    return ((JSpinner) comp).getValue().toString();
		}
	    }
	} catch (Exception ex) {
	}
	return null;
    }

    private int getComponentValue(JComponent comp, int defaultValue) {
	try {
	    return Integer.parseInt(getComponentValue(comp));
	} catch (Exception ex) {
	}
	return defaultValue;
    }

    private float getComponentValue(JComponent comp, float defaultValue) {
	try {
	    return Float.parseFloat(getComponentValue(comp));
	} catch (Exception ex) {
	}
	return defaultValue;
    }

    private void setComponentValue(JComponent comp, int value) {
	try {
	    if (comp instanceof JTextField) {
		((JTextField) comp).setText("" + value);
	    } else {
		if (Class.forName("javax.swing.JSpinner").isAssignableFrom(
			comp.getClass())) {
		    ((JSpinner) comp).setValue(new Integer(value));
		}
	    }
	} catch (Exception ex) {
	}
    }

    private void setComponentValue(JComponent comp, float value) {
	try {
	    if (comp instanceof JTextField) {
		((JTextField) comp).setText("" + value);
	    } else {
		if (Class.forName("javax.swing.JSpinner").isAssignableFrom(
			comp.getClass())) {
		    ((JSpinner) comp).setValue(new Double(value));
		}
	    }
	} catch (Exception ex) {
	}
    }

    private void setComponentMaximum(JComponent comp, int value) {
	try {
	    if (getComponentValue(comp, value) > value) {
		setComponentValue(comp, value);
	    }
	    if (comp instanceof JTextField) {
	    } else {
		if (Class.forName("javax.swing.JSpinner").isAssignableFrom(
			comp.getClass())) {
		    ((SpinnerNumberModel) ((JSpinner) comp).getModel())
			    .setMaximum(new Integer(value));
		}
	    }
	} catch (Exception ex) {
	}
    }

    private void setComponentMaximum(JComponent comp, float value) {
	try {
	    if (getComponentValue(comp, value) > value) {
		setComponentValue(comp, value);
	    }
	    if (comp instanceof JTextField) {
	    } else {
		if (Class.forName("javax.swing.JSpinner").isAssignableFrom(
			comp.getClass())) {
		    ((SpinnerNumberModel) ((JSpinner) comp).getModel())
			    .setMaximum(new Float(value));
		}
	    }
	} catch (Exception ex) {
	}
    }

    public void setMatrixSize(int nrows, int ncols) {
	// Set Maximum sizes on cluster numbers
	setComponentMaximum(cluster_k, nrows - 1);
	setComponentMaximum(agglofrom, nrows - 1);
	// If ncols < 3, don't allow Pearson correlation
	// TODO
    }

    /**
     * Display this panel in a frame
     */
    public static void main(String[] args) {
	JFrame frame = new JFrame("Cluto Options");
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	ClutoOptionsPanel cop = new ClutoOptionsPanel(1);
	frame.getContentPane().add(cop, BorderLayout.CENTER);
	frame.setLocation(200, 200);
	frame.pack();
	frame.setVisible(true);
    }
}

/*
 * The following are private classes used only within ClutoOptionsPanel. They
 * are declared outside the scope of ClutoOptionsPanel so that they can have
 * static declarations.
 */

class IntMetricChooser extends JComboBox {
    Hashtable param = new Hashtable();

    public IntMetricChooser(String[] metrics) {
	super(metrics);
    }

    public void setChoices(String[] metrics) {
	Object selval = getSelectedItem();
	DefaultComboBoxModel cbm = new DefaultComboBoxModel(metrics);
	setModel(cbm);
	if (cbm.getIndexOf(selval) >= 0) {
	    setSelectedItem(selval);
	}
    }

    public int getValue() {
	return ((Integer) param.get(getSelectedItem())).intValue();
    }

    public void setValue(int value) {
	Integer v = new Integer(value);
	for (Iterator i = param.keySet().iterator(); i.hasNext();) {
	    Object key = i.next();
	    if (v.equals(param.get(key))) {
		setSelectedItem(key);
		break;
	    }
	}
    }
}

class ClusterMethodChooser extends IntMetricChooser {
    // static final String metrics[] = {"VP_ClusterDirect","VP_ClusterRB",
    // "VA_Cluster", "VA_ClusterBiased", "VP_ClusterRBTree"};
    static final String metrics[] = {
	    ClutoResource.getName("CLUTO_VP_ClusterDirect"),
	    ClutoResource.getName("CLUTO_VP_ClusterRB"),
	    ClutoResource.getName("CLUTO_VA_Cluster"),
	    ClutoResource.getName("CLUTO_VA_ClusterBiased"),
	    ClutoResource.getName("CLUTO_VP_ClusterRBTree") };

    public ClusterMethodChooser() {
	super(metrics);
	param.put(metrics[0], new Integer(ClutoParams.VP_ClusterDirect));
	param.put(metrics[1], new Integer(ClutoParams.VP_ClusterRB));
	param.put(metrics[2], new Integer(ClutoParams.VA_Cluster));
	param.put(metrics[3], new Integer(ClutoParams.VA_ClusterBiased));
	param.put(metrics[4], new Integer(ClutoParams.VP_ClusterRBTree));
    }
}

class ColModelChooser extends IntMetricChooser {
    // static final String metrics[] = {"None","Idf"};
    static final String metrics[] = {
	    ClutoResource.getName("CLUTO_COLMODEL_NONE"),
	    ClutoResource.getName("CLUTO_COLMODEL_IDF") };

    public ColModelChooser() {
	super(metrics);
	param.put(metrics[0], new Integer(JClutoWrapper.CLUTO_COLMODEL_NONE));
	param.put(metrics[1], new Integer(JClutoWrapper.CLUTO_COLMODEL_IDF));
    }
}

class CrMetricChooser extends IntMetricChooser {
    static final String agMetrics[] = {
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_I1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_I2.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_E1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_G1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_G1P.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_H1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_H2.name"),

	    ClutoResource.getName("CLUTO_CLFUN_SLINK"),
	    ClutoResource.getName("CLUTO_CLFUN_SLINK_W"),
	    ClutoResource.getName("CLUTO_CLFUN_CLINK"),
	    ClutoResource.getName("CLUTO_CLFUN_CLINK_W"),
	    ClutoResource.getName("CLUTO_CLFUN_UPGMA"),
	    ClutoResource.getName("CLUTO_CLFUN_UPGMA_W"), };

    static final String paMetrics[] = {
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_I1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_I2.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_E1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_G1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_G1P.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_H1.name"),
	    ClutoOptionsPanel.rsrc.getString("CLUTO_CLFUN_H2.name"), };

    public CrMetricChooser(String[] metrics) {
	super(metrics);
	setMaximumRowCount(agMetrics.length);
	param.put(agMetrics[0], new Integer(JClutoWrapper.CLUTO_CLFUN_I1));
	param.put(agMetrics[1], new Integer(JClutoWrapper.CLUTO_CLFUN_I2));
	param.put(agMetrics[2], new Integer(JClutoWrapper.CLUTO_CLFUN_E1));
	param.put(agMetrics[3], new Integer(JClutoWrapper.CLUTO_CLFUN_G1));
	param.put(agMetrics[4], new Integer(JClutoWrapper.CLUTO_CLFUN_G1P));
	param.put(agMetrics[5], new Integer(JClutoWrapper.CLUTO_CLFUN_H1));
	param.put(agMetrics[6], new Integer(JClutoWrapper.CLUTO_CLFUN_H2));
	param.put(agMetrics[7], new Integer(JClutoWrapper.CLUTO_CLFUN_SLINK));
	param.put(agMetrics[8], new Integer(JClutoWrapper.CLUTO_CLFUN_SLINK_W));
	param.put(agMetrics[9], new Integer(JClutoWrapper.CLUTO_CLFUN_CLINK));
	param
		.put(agMetrics[10], new Integer(
			JClutoWrapper.CLUTO_CLFUN_CLINK_W));
	param.put(agMetrics[11], new Integer(JClutoWrapper.CLUTO_CLFUN_UPGMA));
	param
		.put(agMetrics[12], new Integer(
			JClutoWrapper.CLUTO_CLFUN_UPGMA_W));
	setValue(JClutoWrapper.CLUTO_CLFUN_I2);
    }
}

class CsTypeMetricChooser extends IntMetricChooser {
    static final String metrics[] = {
	    ClutoResource.getName("CLUTO_CSTYPE_LARGEFIRST"),
	    ClutoResource.getName("CLUTO_CSTYPE_BESTFIRST"), };

    public CsTypeMetricChooser() {
	super(metrics);
	param.put(metrics[0],
		new Integer(JClutoWrapper.CLUTO_CSTYPE_LARGEFIRST));
	param
		.put(metrics[1], new Integer(
			JClutoWrapper.CLUTO_CSTYPE_BESTFIRST));
    }
}

class RowModelChooser extends IntMetricChooser {
    static final String metrics[] = {
	    ClutoResource.getName("CLUTO_ROWMODEL_NONE"),
	    ClutoResource.getName("CLUTO_ROWMODEL_MAXTF"),
	    ClutoResource.getName("CLUTO_ROWMODEL_SQRT"),
	    ClutoResource.getName("CLUTO_ROWMODEL_LOG"), };

    public RowModelChooser() {
	super(metrics);
	param.put(metrics[0], new Integer(JClutoWrapper.CLUTO_ROWMODEL_NONE));
	param.put(metrics[1], new Integer(JClutoWrapper.CLUTO_ROWMODEL_MAXTF));
	param.put(metrics[2], new Integer(JClutoWrapper.CLUTO_ROWMODEL_SQRT));
	param.put(metrics[3], new Integer(JClutoWrapper.CLUTO_ROWMODEL_LOG));
    }
}

// A dropdown menu for getting similiarity function for non-graph-based
// algorithms

class SimMetricChooser extends IntMetricChooser {
    static final String metrics[] = {
	    ClutoResource.getName("CLUTO_SIM_COSINE"),
	    ClutoResource.getName("CLUTO_SIM_CORRCOEF"),
	    ClutoResource.getName("CLUTO_SIM_EDISTANCE"),
	    ClutoResource.getName("CLUTO_SIM_EJACCARD"), };
    static final String vmetrics[] = { metrics[0], metrics[1] };

    public SimMetricChooser() {
	super(vmetrics);
	param.put(metrics[0], new Integer(JClutoWrapper.CLUTO_SIM_COSINE));
	param.put(metrics[1], new Integer(JClutoWrapper.CLUTO_SIM_CORRCOEF));
	param.put(metrics[2], new Integer(JClutoWrapper.CLUTO_SIM_EDISTANCE));
	param.put(metrics[3], new Integer(JClutoWrapper.CLUTO_SIM_EJACCARD));
    }
}

class KwayRefine extends JCheckBox {
    public KwayRefine(String text, boolean selected) {
	super(text, selected);
    }

    public int getValue() {
	return isSelected() ? 1 : 0;
    }

    public void setValue(int value) {
	setSelected(value == 1);
    }
}

class AggloBox extends JCheckBox {
    public AggloBox(String text, boolean selected) {
	super(text, selected);
    }

    public int getValue() {
	return isSelected() ? 1 : 0;
    }

    public void setValue(int value) {
	setSelected(value == 1);
    }
}

/*
 * Hide all references to JSpinner and SpinnerNumberModel here, and don't call
 * this class unless we're j2se1.4 or later.
 */
class DoSpinner {
    static JComponent getComponent(int value, int min, int max, int step) {
	return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    static JComponent getComponent(double value, double min, double max,
	    double step) {
	return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    static void setComponent(JComponent comp, int value, int min, int max,
	    int step) {
	if (comp instanceof JSpinner) {
	    try {
		SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) comp)
			.getModel();
		model.setMinimum(new Integer(min));
		model.setMaximum(new Integer(max));
		model.setStepSize(new Integer(step));
		model.setValue(new Integer(value));
	    } catch (Exception ex) {
	    }
	}
    }
}
