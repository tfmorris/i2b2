/*
 * @(#) $RCSfile: TransMatrixPanel.java,v $ $Revision: 1.3 $ $Date: 2008/11/26 14:59:01 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.event.*;
import javax.swing.*;

/**
 * Display a panel from which to select a method for missing values estimation
 * and for matrix transformations.
 * 
 * @author Shulan Tian
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/26 14:59:01 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ProcessMatrix
 */
public class TransMatrixPanel extends JPanel implements TransParams {
    static boolean hasJSpinner = false;
    static {
	try {
	    Class.forName("javax.swing.JSpinner");
	    hasJSpinner = true;
	} catch (Exception ex) {
	}
    }
    int dbglvl = 0;
    protected JComponent numNeighbors;
    protected EstMethodChooser estMethod = new EstMethodChooser();
    protected JLabel numlabel;
    protected JCheckBox mean = new JCheckBox("Mean centering", false);
    protected JCheckBox median = new JCheckBox("Median centering", false);
    protected JCheckBox log = new JCheckBox("Log transformation", false);

    /**
     * constructor for TransMatrixPanel
     * 
     * @param method
     *            The initial estimating Method for missing value
     * @see TransParams#Do_Nothing
     * @see TransParams#KNN
     * @see TransParams#Row_Average
     * @see TransParams#Column_Average
     * @see TransParams#Input_With_Zero
     */
    public TransMatrixPanel(int method) {

	// panel for clustering parameters
	Box jp = Box.createVerticalBox();

	// jp.setBorder(BorderFactory.createTitledBorder(BorderFactory.
	// createEtchedBorder(),
	// "Transform Matrix"));
	// jp.setBorder(BorderFactory.createEtchedBorder());
	jp.setOpaque(false);
	add(jp);

	estMethod.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setEstMethod(estMethod.getSelectedIndex());
	    }
	});

	numlabel = new JLabel("#neighbors");
	numNeighbors = getIntComponent(15, 1, 100, 1);
	numNeighbors.setBorder(BorderFactory.createLoweredBevelBorder());
	numNeighbors.setToolTipText("The deserved # of neighbors");
	numNeighbors.setVisible(false);
	numlabel.setVisible(false);

	Box estBox = Box.createHorizontalBox();
	estBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Estimate Missing Values"));
	estBox.add(estMethod);
	estBox.add(numNeighbors);
	estBox.add(numlabel);

	Box xformBox = Box.createVerticalBox();
	xformBox.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Transform Matrix"));
	Box logBox = Box.createHorizontalBox();
	logBox.add(log);
	logBox.add(Box.createHorizontalGlue());
	xformBox.add(logBox);

	Box cntrBox = Box.createHorizontalBox();
	cntrBox.add(mean);
	cntrBox.add(median);

	xformBox.add(cntrBox);

	jp.add(estBox);
	jp.add(xformBox);

	setEstMethod(method);

    }

    /**
     * Set the missing value method
     * 
     * @param method
     *            The <b>CLUTO</b> missing value estimate method
     * @see TransParams#Do_Nothing
     * @see TransParams#KNN
     * @see TransParams#Row_Average
     * @see TransParams#Column_Average
     * @see TransParams#Input_With_Zero
     */
    public void setEstMethod(int method) {
	if (estMethod.getSelectedIndex() != method) {
	    estMethod.setValue(method);
	}

	switch (method) {
	case Do_Nothing:
	    numlabel.setVisible(false);
	    numNeighbors.setVisible(false);
	    break;
	case KNN:
	    numlabel.setVisible(true);
	    numNeighbors.setVisible(true);
	    break;
	case Row_Average:
	    numlabel.setVisible(false);
	    numNeighbors.setVisible(false);
	    break;
	case Column_Average:
	    numNeighbors.setVisible(false);
	    numlabel.setVisible(false);
	    break;
	case Input_With_Zero:
	    numNeighbors.setVisible(false);
	    numlabel.setVisible(false);
	    break;

	default:
	    break;
	}
    }

    // TransParams interface
    /**
     * Set the matrix transformation values to be the same as the given params.
     * 
     * @param params
     *            parameters associated with TransMatrixPanel
     */
    public void setValues(TransParams params) {
	setEstMethod(params.getEstMethod());
	setComponentValue(numNeighbors, params.getNumNeighbors());
	mean.setSelected(params.getMeanCenter());
	median.setSelected(params.getMedianCenter());
	log.setSelected(params.getLogTrans());
    }

    /**
     * this function sets the default values for the chosen method
     * 
     * @param method
     *            The missing value estimation method
     */
    public void setDefaultValues(int method) {
	setEstMethod(method);
	setComponentValue(numNeighbors, 15);
	mean.setSelected(false);
	median.setSelected(false);
	log.setSelected(false);

    }

    /**
     * Get the missing value estimating method.
     * 
     * @return the integer associated with method
     */
    public int getEstMethod() {
	return estMethod.getValue();
    }

    /**
     * Get the number of neighbors used when estimating missing values with the
     * K nearest neighbors.
     * 
     * @return number of neighbors
     */
    public int getNumNeighbors() {
	return getComponentValue(numNeighbors, 15);
    }

    /**
     * Return whether mean centering should be performed on the matrix values.
     * 
     * @return whether the mean centering option is selected.
     */
    public boolean getMeanCenter() {
	return mean.isSelected();
    }

    /**
     * Return whether median centering should be performed on the matrix values.
     * 
     * @return whether the median centering option is selected.
     */
    public boolean getMedianCenter() {
	return median.isSelected();
    }

    /**
     * Return whether a log transformation should be performed on the matrix
     * values. get log transformation option
     * 
     * @return whether the log transformation option is selected
     */
    public boolean getLogTrans() {
	return log.isSelected();
    }

    /**
     * get degugging option
     * 
     * @return 1 if debug, else 0
     */
    public int getDbgLvl() {
	return dbglvl;
    }

    /**
     * this function constructs a widget for integer JSpinner
     * 
     * @param value
     *            Need_Comment
     * @param min
     *            the minimum integer
     * @param max
     *            the maximum integer
     * @param step
     *            number for increase at one change
     * @return a integer JSpinner component
     */
    private JComponent getIntComponent(int value, int min, int max, int step) {
	if (hasJSpinner) {
	    // return new JSpinner(new SpinnerNumberModel(value, min, max,
	    // step));
	    return DoSpinner.getComponent(value, min, max, step);
	}
	return new JTextField(Integer.toString(value), Integer.toString(max)
		.length());
    }

    /**
     * this function constructs a widget for float JSpinner
     * 
     * @param value
     *            Need_Comment
     * @param min
     *            the minimum integer
     * @param max
     *            the maximum integer
     * @param step
     *            number for increase at one change
     * @return a float JSpinner component
     */
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

    /**
     * this function get the value supplied by users in string format
     * 
     * @param comp
     *            JComponent
     * @return a value in string format
     */
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

    /**
     * this function gets the integer value from JComponent
     * 
     * @param comp
     *            JComponent
     * @param defaultValue
     *            the default value
     * @return an integer from JComponent
     */
    private int getComponentValue(JComponent comp, int defaultValue) {
	try {
	    return Integer.parseInt(getComponentValue(comp));
	} catch (Exception ex) {
	}
	return defaultValue;
    }

    /**
     * this function gets the float value from JComponent
     * 
     * @param comp
     *            JComponent
     * @param defaultValue
     *            the default value
     * @return a float value from JComponent
     */
    private float getComponentValue(JComponent comp, float defaultValue) {
	try {
	    return Float.parseFloat(getComponentValue(comp));
	} catch (Exception ex) {
	}
	return defaultValue;
    }

    /**
     * this function sets an integer value for JComponent
     * 
     * @param comp
     *            JComponent
     * @param value
     *            the value to be set
     */
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

    /**
     * this function sets a float value for JComponent
     * 
     * @param comp
     *            JComponent
     * @param value
     *            the value to be set
     */
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
}

/**
 * this function constructs a JComoBox for choosing missing value estimate
 * method it is a private class used only within TransMatrixPanel. it is
 * declared outside the scope of TransMatrixPanel so that it can have static
 * declarations.
 */

class EstMethodChooser extends IntMetricChooser {
    /** available estimating methods */
    static final String metrics[] = { "Do_Nothing", "KNN", "Row_Average",
	    "Column_Average", "Input_With_Zero" };

    /**
     * assign integer representation to each method
     */
    public EstMethodChooser() {
	super(metrics);
	param.put(metrics[0], new Integer(TransParams.Do_Nothing));
	param.put(metrics[1], new Integer(TransParams.KNN));
	param.put(metrics[2], new Integer(TransParams.Row_Average));
	param.put(metrics[3], new Integer(TransParams.Column_Average));
	param.put(metrics[4], new Integer(TransParams.Input_With_Zero));
    }
}
