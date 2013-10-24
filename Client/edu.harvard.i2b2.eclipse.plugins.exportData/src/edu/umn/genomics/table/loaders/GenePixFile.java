/*
 * @(#) $RCSfile: GenePixFile.java,v $ $Revision: 1.3 $ $Date: 2008/11/05 18:46:10 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.loaders;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import edu.umn.genomics.table.*;
import edu.umn.genomics.file.*;

/**
 * A Graphical User Interface for loading a data table from a GenePix file or
 * URL, and providing a TableModel interface to the datasource. The GenePix file
 * should have the Axon Text File format:
 * 
 * <code>
 * First header record    Format: ATF (all caps), Version number
 * Second header record   Number of optional header records n, Number of data columns (fields) m
 * 1st optional record    ...
 * 2nd optional record    ...
 * nth optional record    ...
 * (n+3)th record         Required record containing m fields, Each field contains a column title.
 * DATA RECORDS           Arranged in m columns (fields) of data.
 * </code>
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/05 18:46:10 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class GenePixFile extends AbstractTableSource implements OpenTableSource {
    TableModel tm = null;
    JFileChooser fc;
    JTextField path;
    JTextArea header;
    JTable jtable = new JTable();
    String exts[] = { "gpr", "gal" };
    javax.swing.filechooser.FileFilter gpFileFilter = new ExtensionFileFilter(
	    Arrays.asList(exts), "*.gpr and *.gal files");

    /**
     * Creates a Component for selecting a table from a GenePix Axon Text File
     * from file or URL.
     */
    public GenePixFile() {
	// file
	String fs = "\t";
	int colHeadersRows = -1;

	fc = new JFileChooser();
	fc.addChoosableFileFilter(gpFileFilter);

	JPanel flp = new JPanel(new GridLayout(0, 1));
	JPanel fip = new JPanel(new GridLayout(0, 1));

	JLabel pthLbl = new JLabel("File or URL:", SwingConstants.RIGHT);
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
	openBtn.setToolTipText("Open a file chooser");
	openBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		openTableSource();
	    }
	});

	JPanel pathp = new JPanel(new BorderLayout());
	pathp.add(path, BorderLayout.CENTER);
	pathp.add(browseBtn, BorderLayout.WEST);
	pathp.add(openBtn, BorderLayout.EAST);

	flp.add(pthLbl);
	fip.add(pathp);

	JPanel fileLoc = new JPanel(new BorderLayout());
	fileLoc.add(flp, BorderLayout.WEST);
	fileLoc.add(fip, BorderLayout.CENTER);

	JPanel hdrPanel = new JPanel(new BorderLayout());
	hdrPanel.add(fileLoc, BorderLayout.NORTH);

	header = new JTextArea();
	header.setRows(10);
	hdrPanel.add(new JScrollPane(header), BorderLayout.SOUTH);

	setLayout(new BorderLayout());
	add(hdrPanel, BorderLayout.NORTH);
	jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	JScrollPane jsp = new JScrollPane(jtable);
	add(jsp);
    }

    /**
     * Open the data source and create a TableModel.
     */
    private void openTableSource() {
	try {
	    openTableSource(path.getText());
	} catch (Exception ex) {
	    System.err.println(" openTableSource " + ex);
	    // ex.printStackTrace();
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex,
		    "Unable to open table", JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Open the data source and create a TableModel.
     * 
     * @param tableSource
     *            The URL or file path for the table data.
     */
    public void openTableSource(String tableSource) throws IOException {
	String lineSep = System.getProperty("line.separator");
	StringBuffer sb = new StringBuffer();
	LineNumberReader lnr = new LineNumberReader(OpenInputSource
		.getBufferedReader(tableSource));
	// Read the ID and version line
	String line = lnr.readLine();
	if (line == null || !line.startsWith("ATF")) {
	    throw new IOException(
		    "Not in GenePix Axon Text Format: missing ATF field in first line");
	}
	sb.append(line).append(lineSep);
	// Read the header length and column number line
	line = lnr.readLine();
	if (line == null) {
	    throw new IOException(
		    "Not in GenePix Axon Text Format: missing counts in second line");
	}
	StringTokenizer st = new StringTokenizer(line);
	int headerRecords;
	int dataFields;
	try {
	    headerRecords = Integer.parseInt(st.nextToken());
	} catch (Exception nex) {
	    throw new IOException(
		    "Not in GenePix Axon Text Format: missing option header record count in second line");
	}
	try {
	    dataFields = Integer.parseInt(st.nextToken());
	} catch (Exception nex) {
	    throw new IOException(
		    "Not in GenePix Axon Text Format: missing data column count in second line");
	}
	sb.append(line).append(lineSep);
	// Read the rest of the header lines
	for (int i = headerRecords; i > 0; i--) {
	    line = lnr.readLine();
	    sb.append(line).append(lineSep);
	}
	header.setText(sb.toString());
	header.setRows(headerRecords > 8 ? 10 : headerRecords + 2);
	// Now write the rest to a temp file
	File tmp = File.createTempFile("gpr_", ".tbl");
	FileWriter wtr = new FileWriter(tmp);
	for (line = lnr.readLine(); line != null; line = lnr.readLine()) {
	    wtr.write(line);
	    wtr.write(lineSep);
	}
	wtr.flush();
	wtr.close();
	// Now read the data into a FileTableModel
	FileTableModel ftm = new FileTableModel(tmp.getAbsolutePath());
	jtable.setModel(ftm);
	setTableSource(ftm, tableSource);
    }
}
