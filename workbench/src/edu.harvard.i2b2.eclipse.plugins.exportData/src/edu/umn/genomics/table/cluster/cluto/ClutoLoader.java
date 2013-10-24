/*
 * @(#) $RCSfile: ClutoLoader.java,v $ $Revision: 1.3 $ $Date: 2008/11/11 19:05:37 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import edu.umn.genomics.table.*;
import edu.umn.genomics.file.OpenInputSource;
import edu.umn.genomics.layout.SpringUtilities;
import jcluto.*;

/**
 * A Graphical User Interface for selecting a table from a file or URL, and
 * providing a TableModel interface to the datasource.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/11 19:05:37 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class ClutoLoader extends AbstractTableSource implements OpenTableSource {
    TableModel tm = null;
    JFileChooser fc;
    JTextField path;
    JTable jtable = new JTable();
    // Need field for Row Labels
    JTextField rowLabelPath;
    JTextField colLabelPath;

    // Need field for Column Labels
    // Need custom ColumnMap
    /**
     * Creates a ClutoLoader Component for selecting a table from a file or URL.
     */
    public ClutoLoader() {
	// file
	String fs = "\t";
	int colHeadersRows = -1;

	fc = new JFileChooser();

	JPanel clutoFileLoc = new JPanel(new SpringLayout());

	JLabel pthLbl = new JLabel("Matrix File or URL:", SwingConstants.RIGHT);
	path = new JTextField(40);
	path.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    openTableSource();
		} catch (Exception ex) {
		}
	    }
	});
	JButton browseBtn = new JButton("browse");
	browseBtn.setToolTipText("Open a file chooser");
	browseBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int returnVal = fc.showOpenDialog(path);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    try {
			path.setText(file.getAbsolutePath());
			openTableSource();
		    } catch (Exception ex) {
		    }
		} else {
		    System.err.println("Open command cancelled by user.");
		}
	    }
	});
	JButton openBtn = new JButton("open");
	openBtn.setToolTipText("Read the Matrix Source");
	openBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		openTableSource();
	    }
	});

	clutoFileLoc.add(pthLbl);
	clutoFileLoc.add(browseBtn);
	clutoFileLoc.add(path);
	clutoFileLoc.add(openBtn);

	pthLbl = new JLabel("Row Label    File or URL:", SwingConstants.RIGHT);
	rowLabelPath = new JTextField(40);
	rowLabelPath.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    addRowLabels();
		} catch (Exception ex) {
		}
	    }
	});
	browseBtn = new JButton("browse");
	browseBtn.setToolTipText("Open a file chooser");
	browseBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int returnVal = fc.showOpenDialog(path);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    try {
			rowLabelPath.setText(file.getAbsolutePath());
			addRowLabels();
		    } catch (Exception ex) {
		    }
		} else {
		    System.err.println("Open command cancelled by user.");
		}
	    }
	});
	openBtn = new JButton("open");
	openBtn.setToolTipText("Read the Row Labels Source for the Matrix");
	openBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		addRowLabels();
	    }
	});

	clutoFileLoc.add(pthLbl);
	clutoFileLoc.add(browseBtn);
	clutoFileLoc.add(rowLabelPath);
	clutoFileLoc.add(openBtn);

	pthLbl = new JLabel("Column Label File or URL:", SwingConstants.RIGHT);
	colLabelPath = new JTextField(40);
	colLabelPath.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    addColLabels();
		} catch (Exception ex) {
		}
	    }
	});
	browseBtn = new JButton("browse");
	browseBtn.setToolTipText("Open a file chooser");
	browseBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int returnVal = fc.showOpenDialog(path);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    try {
			colLabelPath.setText(file.getAbsolutePath());
			addColLabels();
		    } catch (Exception ex) {
		    }
		} else {
		    System.err.println("Open command cancelled by user.");
		}
	    }
	});
	openBtn = new JButton("open");
	openBtn.setToolTipText("Read the Column Labels Source for the Matrix");
	openBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		addColLabels();
	    }
	});

	clutoFileLoc.add(pthLbl);
	clutoFileLoc.add(browseBtn);
	clutoFileLoc.add(colLabelPath);
	clutoFileLoc.add(openBtn);

	SpringUtilities.makeCompactGrid(clutoFileLoc, // parent
		3, 4, // rows, cols,
		3, 3, // initX, initY
		1, 3); // xPad, yPad

	setLayout(new BorderLayout());
	add(clutoFileLoc, BorderLayout.NORTH);
	jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	JScrollPane jsp = new JScrollPane(jtable);
	add(jsp);
    }

    private Vector openLabelSource(String source) throws IOException,
	    NullPointerException {
	if (source == null) {
	    throw new NullPointerException("No input source location given.");
	}
	Vector v = new Vector();
	BufferedReader rdr = OpenInputSource.getBufferedReader(source);
	for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
	    v.add(line);
	}
	return v;
    }

    static String missingMatrixMsg = "No Matrix Source was entered";

    private void addRowLabels() {
	try {
	    if (getTableModel() == null) {
		if (path.getText() != null && path.getText().length() > 0) {
		    openTableSource();
		}
	    }
	    if (getTableModel() != null) {
		addRowLabels(rowLabelPath.getText());
	    } else {
		JOptionPane.showMessageDialog(getTopLevelAncestor(),
			missingMatrixMsg, missingMatrixMsg,
			JOptionPane.ERROR_MESSAGE);
	    }
	} catch (IOException ex) {
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex,
		    "Unable to open row labels: " + rowLabelPath.getText(),
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    private void addColLabels() {
	try {
	    if (getTableModel() == null) {
		if (path.getText() != null && path.getText().length() > 0) {
		    openTableSource();
		}
	    }
	    if (getTableModel() != null) {
		addColLabels(colLabelPath.getText());
	    } else {
		JOptionPane.showMessageDialog(getTopLevelAncestor(),
			missingMatrixMsg, missingMatrixMsg,
			JOptionPane.ERROR_MESSAGE);
	    }
	} catch (IOException ex) {
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex,
		    "Unable to open column labels: " + colLabelPath.getText(),
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    public void addRowLabels(String source) throws IOException {
	String rowColName = "Names";
	TableModel tm = getTableModel();
	if (tm != null) {
	    VirtualTableModel vtm = tm instanceof VirtualTableModel ? (VirtualTableModel) tm
		    : null;
	    String[] labelArray = null;
	    if (source != null && source.length() > 0) {
		Vector labels = openLabelSource(source);
		labelArray = new String[labels.size()];
		labelArray = (String[]) labels.toArray(labelArray);
	    }
	    if (vtm != null && vtm.getColumnCount() > 0
		    && vtm.getColumnClass(0) == java.lang.String.class
		    && vtm.getColumnName(0).equals(rowColName)) {
		vtm.removeColumn(0);
	    }
	    if (labelArray != null) {
		if (vtm == null) {
		    vtm = new VirtualTableModelProxy(tm);
		}
		ArrayRefColumn col = new ArrayRefColumn(labelArray, rowColName);
		vtm.addColumn(col, 0);
		setTableSource(vtm, getTableSource());
	    }
	    jtable.setModel(getTableModel());
	}
    }

    public void addColLabels(String source) throws IOException {
	TableModel tm = getTableModel();
	if (tm != null) {
	    Vector labels = null;
	    if (source != null && source.length() > 0) {
		labels = openLabelSource(source);
	    }
	    if (tm instanceof VirtualTableModel) {
		tm = ((VirtualTableModel) tm).getTableModel();
	    }
	    if (tm instanceof ClutoTableMatrix) {
		ClutoTableMatrix ctm = (ClutoTableMatrix) tm;
		for (int i = 0; i < ctm.getColumnCount(); i++) {
		    ctm
			    .setColumnName(i, labels != null
				    && i < labels.size() ? (String) labels
				    .get(i) : null);
		}
		jtable.setModel(new DefaultTableModel()); // Need to do this so
							  // table column header
							  // will update
		jtable.setModel(getTableModel());
	    }
	}
    }

    private void openTableSource() {
	try {
	    openTableSource(path.getText());
	} catch (Exception ex) {
	    System.err.println(" openTableSource " + ex);
	    ex.printStackTrace();
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex,
		    "Unable to open matrix file", JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Open the data source and create a TableModel.
     * 
     * @param tableSource
     *            The URL or file path for the table data.
     */
    public void openTableSource(String tableSource) throws IOException {
	ClutoFile cmf = new ClutoFile();
	MappedClutoMatrix ctm = new MappedClutoMatrix(cmf.read(tableSource));
	setTableSource(new VirtualTableModelProxy(ctm), tableSource);
	addColLabels();
	addRowLabels();
	jtable.setModel(getTableModel());
    }

}
