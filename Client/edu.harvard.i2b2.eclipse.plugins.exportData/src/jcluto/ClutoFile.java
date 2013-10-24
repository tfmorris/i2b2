/*
 * @(#) $RCSfile: ClutoFile.java,v $ $Revision: 1.2 $ $Date: 2008/11/18 21:42:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.StringTokenizer;
import java.io.*;
import java.net.*;

/**
 * ClutoFile reads and writes matrix files in the format used by the
 * <b>CLUTO</b> package exectuable: vcluster.
 * 
 * <p>
 * References:
 * <ul>
 * <li><a href="http://www-users.cs.umn.edu/~karypis/cluto/files/manual.pdf">
 * http://www-users.cs.umn.edu/~karypis/cluto/files/manual.pdf </a>
 * <li><a href="http://www-users.cs.umn.edu/~karypis/cluto/">
 * http://www-users.cs.umn.edu/~karypis/cluto/ </a>
 * <li><a href="http://www.msi.umn.edu/software/cluto/">
 * http:////www.msi.umn.edu/software/cluto/ </a>
 * </ul>
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/11/18 21:42:22 $ $Name: RELEASE_1_3_1_0001b $
 * @see javax.swing.table.AbstractTableModel
 * @see ClutoMatrix
 */
public class ClutoFile {
    public static final String ILLEGAL_HEADER = "Illegal Cluto File Header";
    /** character which is used as the field separator in <b>CLUTO</b> matrices. */
    protected String fs = " "; // space character

    /**
     * Attempt to find the absolute pathname for a Unix path that starts with a
     * tilde.
     * 
     * @param source
     *            A file pathname that starts with a tilde character.
     * @return The absolute path of the source pathname.
     */
    public static String tildeSubstitution(String source) {
	if (source.charAt(0) == '~') {
	    try {
		String args[] = new String[3];
		args[0] = "/usr/bin/csh";
		args[1] = "-c";
		args[2] = "echo " + source;
		Process p = Runtime.getRuntime().exec(args);
		BufferedReader br = new BufferedReader(new InputStreamReader(p
			.getInputStream()));
		String path = "";
		String so;
		while ((so = br.readLine()) != null) {
		    path += so;
		}
		return path;
	    } catch (Exception rte) {
		System.err.println(rte);
	    }
	}
	return null;
    }

    /**
     * Get a Reader for the file or URL source.
     * 
     * @param source
     *            The filepath or URL.
     * @return A Reader for the source.
     */
    public static Reader getReader(String source) throws IOException,
	    NullPointerException {
	if (source == null) {
	    throw new NullPointerException("No input source.");
	}
	// URL?
	try {
	    URL url = new URL(source);
	    try {
		InputStream is = url.openStream();
		BufferedReader in = new BufferedReader(
			new InputStreamReader(is));
		return in;
	    } catch (Exception se) {
		// System.err.println("URL.openStream(): " + se);
	    }
	} catch (Exception ue) {
	    // System.err.println("new URL: " + ue);
	}
	// local file?
	try {
	    BufferedReader in = new BufferedReader(new FileReader(source));
	    // System.err.println("local file: " + source);
	    return in;
	} catch (IOException e) {
	    if (source.length() > 0 && source.charAt(0) == '~') {
		String path = tildeSubstitution(source);
		if (path != null) {
		    BufferedReader in = new BufferedReader(new FileReader(
			    source));
		    return in;
		}
	    }
	    // System.err.println(e);
	    throw e;
	}
    }

    /**
     * Read a <b>CLUTO</b> data file.
     * 
     * @param source
     *            The filepath or URL that points to a <b>CLUTO</b> data file.
     * @return A matrix for the jcluto package.
     */
    public ClutoTableMatrix read(String source) throws IOException {
	return read(getReader(source));
    }

    /**
     * Write a <b>CLUTO</b> data file.
     * 
     * @param clutoMatrix
     *            A jcluto package matrix.
     * @param pathname
     *            The file pathname in which to write the <b>CLUTO</b> matrix
     *            file.
     */
    public void write(ClutoMatrix clutoMatrix, String pathname)
	    throws IOException {
	String path = pathname;
	if (pathname.charAt(0) == '~') {
	    path = tildeSubstitution(pathname);
	    if (path == null) {
		path = pathname;
	    }
	}
	write(clutoMatrix, new FileWriter(pathname));
    }

    /**
     * Read a <b>CLUTO</b> data file.
     * 
     * @param reader
     *            The reader for a <b>CLUTO</b> matrix file.
     * @return A matrix for the jcluto package.
     */
    public ClutoTableMatrix read(Reader reader) throws IOException {
	int readAheadLimit = 100000;
	int nrow = 0;
	int ncol = 0;
	int nval = -1;
	int[] rowPtr = null;
	int[] rowInd = null;
	float[] rowVal = null;
	boolean isSparse = false;
	boolean isGraph = false;

	LineNumberReader rdr = new LineNumberReader(reader);
	// Read first line for: nrow ncol [nval]
	String line = rdr.readLine();
	rdr.mark(readAheadLimit);

	StringTokenizer st = new StringTokenizer(line);
	int tokCnt = st.countTokens();
	if (tokCnt < 1 || tokCnt > 3) {
	    // throw error
	    throw new IOException(ILLEGAL_HEADER);
	}
	int cnts[] = new int[tokCnt];
	try {
	    for (int i = 0; i < tokCnt; i++) {
		cnts[i] = Integer.parseInt(st.nextToken());
	    }
	} catch (NumberFormatException nfex) {
	    throw new IOException(ILLEGAL_HEADER);
	}

	switch (tokCnt) {
	case 1:
	    // Dense Graph
	    isGraph = true;
	    isSparse = false;
	    nrow = cnts[0];
	    ncol = nrow;
	    nval = nrow * ncol;
	    break;
	case 2:
	    // Dense Matrix is the default case
	    isGraph = false;
	    isSparse = false;
	    nrow = cnts[0];
	    ncol = cnts[1];
	    nval = nrow * ncol;
	    for (line = rdr.readLine(); line != null
		    && rdr.getLineNumber() < 100; line = rdr.readLine()) {
		st = new StringTokenizer(line);
		int dCnt = st.countTokens();
		if (dCnt % 2 != 0) {
		    if (dCnt == cnts[1]) {
			// Dense Matrix
			break;
		    } else {
			throw new IOException(ILLEGAL_HEADER);
		    }
		} else {
		    String[] tok = new String[dCnt];
		    for (int i = 0; i < dCnt; i++) {
			tok[i] = st.nextToken();
		    }
		    boolean canBeSparse = true;
		    for (int i = 0; canBeSparse && i < dCnt; i += 2) {
			try {
			    int idx = Integer.parseInt(tok[i]);
			    if (idx < 1 || idx > cnts[0]) {
				canBeSparse = false;
			    }
			} catch (NumberFormatException nfex) {
			    canBeSparse = false;
			}
		    }
		    if (dCnt != cnts[1] && canBeSparse) {
			// Sparse Graph
			isGraph = true;
			isSparse = true;
			nrow = cnts[0];
			ncol = nrow;
			nval = cnts[1];
			break;
		    } else if (dCnt == cnts[1] && !canBeSparse) {
			// Dense Matrix
			break;
		    }
		}
	    }
	    break;
	case 3:
	    // Sparse Matrix
	    // if even number of tokens and all odd tokens are ints > 0 && <=
	    // nvert
	    isGraph = false;
	    isSparse = true;
	    nrow = cnts[0];
	    ncol = cnts[1];
	    nval = cnts[2];
	    break;
	}

	System.err.println("isGraph=" + isGraph + " isSparse=" + isSparse
		+ " nrow=" + nrow + " lines=" + rdr.getLineNumber());

	try {
	    rdr.reset();
	} catch (IOException ioex) {
	    rdr = new LineNumberReader(reader);
	    line = rdr.readLine();
	}
	// read matrix data
	if (isSparse) {
	    rowPtr = new int[nrow + 1];
	    rowPtr[0] = 0;
	    rowInd = new int[nval];
	    rowVal = new float[nval];
	    for (int ri = 0, vi = 0; ri < nrow
		    && (line = rdr.readLine()) != null; ri++) {
		if (true) {
		    st = new StringTokenizer(line);
		    while (st.hasMoreTokens()) {
			// read columnIndex
			rowInd[vi] = Integer.parseInt(st.nextToken()) - 1;
			// read value
			rowVal[vi++] = Float.parseFloat(st.nextToken());
		    }
		} else {
		    for (int ci = 0, fi = 0, ni = line.indexOf(fs, fi); fi < line
			    .length()
			    && ci < ncol; ci++, fi = ni + 1, ni = line.indexOf(
			    fs, fi)) {

			// read columnIndex
			String s = ni < 0 ? line.substring(fi) : line
				.substring(fi, ni);
			rowInd[vi] = Integer.parseInt(s) - 1;
			// read value
			fi = ni + 1;
			ni = line.indexOf(fs, fi);
			s = ni < 0 ? line.substring(fi) : line
				.substring(fi, ni);
			rowVal[vi++] = Float.parseFloat(s);

		    }
		}
		rowPtr[ri + 1] = vi;
	    }
	} else {
	    rowVal = new float[nrow * ncol];
	    for (int ri = 0, vi = 0; ri < nrow
		    && (line = rdr.readLine()) != null; ri++) {
		if (true) {
		    st = new StringTokenizer(line);
		    for (int ci = 0; ci < ncol; ci++) {
			if (st.hasMoreTokens()) {
			    rowVal[vi++] = Float.parseFloat(st.nextToken());
			} else {
			    rowVal[vi++] = Float.NaN;
			}
		    }
		} else {
		    for (int ci = 0, fi = 0, ni = line.indexOf(fs, fi); ci < ncol; ci++, fi = ni + 1, ni = line
			    .indexOf(fs, fi)) {
			String s = ni < 0 ? line.substring(fi) : line
				.substring(fi, ni);
			rowVal[vi++] = Float.parseFloat(s);
		    }
		}
	    }
	}
	return new ClutoTableMatrix(nrow, ncol, rowPtr, rowInd, rowVal, isGraph);
    }

    /**
     * Write a <b>CLUTO</b> data file.
     * 
     * @param clutoMatrix
     *            A jcluto package matrix.
     * @param writer
     *            The writer for the <b>CLUTO</b> matrix file.
     */
    public void write(ClutoMatrix clutoMatrix, Writer writer)
	    throws IOException {
	PrintWriter wtr = new PrintWriter(writer, true);
	int nrow = clutoMatrix.getRowCount();
	int ncol = clutoMatrix.getColumnCount();
	int[] rowPtr = clutoMatrix.getRowPtr();
	int[] rowInd = clutoMatrix.getRowInd();
	float[] rowVal = clutoMatrix.getRowVal();
	boolean isSparse = clutoMatrix.getMissingValueCount() > 0;
	boolean isGraph = clutoMatrix.getIsGraph();
	// Header row
	if (isGraph) {
	    if (isSparse) {
		wtr.println(nrow + " " + clutoMatrix.getValueCount());
	    } else {
		wtr.println(nrow);
	    }
	} else {
	    if (isSparse) {
		wtr.println(nrow + " " + ncol + " "
			+ clutoMatrix.getValueCount());
	    } else {
		wtr.println(nrow + " " + ncol);
	    }
	}
	// matrix rows
	if (isSparse) {
	    for (int ri = 0, vi = 0; ri < nrow; ri++) {
		for (int ci = rowPtr[ri]; ci < rowPtr[ri + 1]; ci++) {
		    if (ci > rowPtr[ri])
			wtr.print(fs);
		    wtr.print(rowInd[ci] + 1);
		    wtr.print(fs);
		    wtr.print(rowVal[ci]);
		}
		wtr.println("");
	    }
	} else {
	    for (int ri = 0, vi = 0; ri < nrow; ri++) {
		for (int ci = 0; ci < ncol; ci++) {
		    if (ci > 0)
			wtr.print(fs);
		    wtr.print(rowVal[vi++]);
		}
		wtr.println("");
	    }
	}
    }

}
