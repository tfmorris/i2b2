/*
 * @(#) $RCSfile: FileTableModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;
import java.io.*;
import java.text.*;
import javax.swing.table.*;
import edu.umn.genomics.bi.dbutil.VectorTree;
import edu.umn.genomics.file.OpenInputSource;

/**
 * FileTableModel presents a TableModel interface to a data file. FileTableModel
 * attempts to open the data source as a RandomAccessFile. If the data source is
 * a URL, it will attempt to copy the data to a temporary file that it can
 * access as a RandomAccessFile. FileTableModel wiil attempt to determine the
 * field separator, the presence of header lines, and the data Class of each
 * column (Character String, Number, or Date) by examining the first several
 * lines of data.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 */
@SuppressWarnings("serial")
public class FileTableModel extends AbstractTableModel implements
	TableColumnMap, Runnable {
    /*
     * // Eventually we want to present choices about converting this file to a
     * TableModel class TableStats { public int getColumnCount(); public Class[]
     * getColumnClass(); public int getHeaderRowCount(); public void
     * setHeaderRowCount(int headerRows); public Vector
     * getPossibleFieldSeparators(String[] lines); public String
     * getFieldSeparator(); public void setFieldSeparator(String fs); }
     */
    class ColStats {
	public String name = "";

	public boolean load = true;

	public int firstNonString = Integer.MAX_VALUE;

	public int firstNumDate = Integer.MAX_VALUE;

	public int lastString = 0;

	public int maxLen = 0;

	public int valCnt = 0;

	public int nullCnt = 0;

	public int numCnt = 0;

	public int dateCnt = 0;

	public double minNum = Double.NaN;

	public double maxNum = Double.NaN;

	public java.util.Date minDate = null;

	public java.util.Date maxDate = null;

	public String[] data;

	@Override
	public String toString() {
	    String brk = "\n";
	    StringBuffer sb = new StringBuffer();
	    sb.append("name = " + name);
	    sb.append(brk);
	    sb.append("firstNonString = " + firstNonString);
	    sb.append(brk);
	    sb.append("firstNumDate = " + firstNumDate);
	    sb.append(brk);
	    sb.append("lastString = " + lastString);
	    sb.append(brk);
	    sb.append("maxLen = " + maxLen);
	    sb.append(brk);
	    sb.append("values = " + valCnt);
	    sb.append(brk);
	    sb.append("nulls = " + nullCnt);
	    sb.append(brk);
	    sb
		    .append("numbers = "
			    + numCnt
			    + (numCnt > 0 ? (" [" + minNum + " - " + maxNum + "]")
				    : ""));
	    sb.append(brk);
	    sb.append("dates = "
		    + dateCnt
		    + (dateCnt > 0 ? (" [" + minDate + " - " + maxDate + "]")
			    : ""));
	    sb.append(brk);
	    return sb.toString();
	}
    }

    public boolean debug = false;

    public String source = null;

    private RandomAccessFile raf;

    public boolean canSplit; // can use regex in Java 1.4

    public String fs = "	"; // tab

    public String rs = null; // tab

    public int linesRead = 0;

    boolean linesIndexed = false;

    public long rowOffset[] = null;

    public int dataRowOffset = 0;

    public final int fetchSize = 200;

    public Class[] columnClass = null;

    public String[] columnName = null;

    public VectorTree vt = new VectorTree(fetchSize, 1, 1);

    // Column info
    CacheColumnMap colMap[] = null;

    class Indexer extends Thread {
	BufferedReader rdr = null;

	RandomAccessFile file = null;

	Indexer(BufferedReader rdr, RandomAccessFile file) {
	    this.rdr = rdr;
	    this.file = file;
	}

	@Override
	public void run() {
	    int cnt = 0;
	    if (rdr != null) {
		cnt = copy(rdr, file);
	    } else {
		cnt = getLineOffsets(file);
	    }
	    setIndexedCount();
	    // System.err.println((this.rdr != null ? "copied " : " indexed ") +
	    // cnt + " lines");

	    // close the file, wp
	    try {
		file.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    Indexer idxThread = null;

    class Reader extends Thread {
	Reader() {
	}

	@Override
	public void run() {
	    int rowIndex = 0;
	    int lastCnt = 0;
	    Vector rv = new Vector(getColumnCount());
	    while (true) {
		while (rowIndex < getRowCount()) {
		    try {
			rv = parseRow(rv, columnClass, readRow(rowIndex));

			// System.err.println(rowIndex + "\t" + rv);
			for (int c = 0; c < colMap.length; c++) {
			    colMap[c].setValueAt(rv.get(c), rowIndex);
			}
			rowIndex++;
			if (rowIndex - lastCnt > 1000) {
			    fireTableRowsUpdated(lastCnt, rowIndex - 1);
			    lastCnt = rowIndex;
			}
		    } catch (Exception ex) {
			System.err.println(ex);
			ex.printStackTrace();
		    }
		}
		if (!linesIndexed) {
		    try {
			Thread.sleep(100);
		    } catch (Exception ex) {
			System.err.println(ex);
		    }
		} else {
		    break;
		}
	    }

	    // close the file, wp
	    try {
		raf.close();
		// new File(source).deleteOnExit();
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    if (rowIndex > lastCnt) {
		fireTableRowsUpdated(lastCnt, rowIndex - 1);
		lastCnt = rowIndex;
	    }
	    CacheColumnMap[] cMap = colMap;
	    for (int c = 0; c < cMap.length; c++) {
		// cMap[c].setSortOrder(CellMap.NATURALSORT);
		cMap[c].setSortOrder(CellMap.ALPHANUMSORT);
		cMap[c].sortColumn();
		cMap[c].collectStats();
	    }
	    for (int c = 0; c < cMap.length; c++) {
		// System.err.println(c + " " + cMap[c].getName() + " " +
		// cMap[c].getState() + " " + cMap[c].getMin() + " to " +
		// cMap[c].getMax() );
		cMap[c].setState(CellMap.MAPPED);
	    }

	}
    }

    Reader rdrThread = null;

    private synchronized void setIndexedCount() {
	int cntWas = vt.getSize();
	vt.setSize(linesRead - dataRowOffset);
	if (vt.getSize() > cntWas) {
	    fireTableRowsInserted(cntWas, vt.getSize() - 1);
	}
	// System.err.println(" size was " + cntWas + " size is " +
	// vt.getSize());
    }

    /**
     * FileTableModel presents a text file as a TableModel.
     */
    public FileTableModel() {
    }

    /**
     * FileTableModel presents a text file as a TableModel.
     * 
     * @param source
     *            The path or URL to the data source.
     */
    public FileTableModel(String source) throws IOException {
	read(source);
    }

    /**
     * Initial varaibles before reading a new source.
     */
    private void init() {
	dataRowOffset = 0;
	columnClass = null;
	columnName = null;
    }

    /**
     * Read the source and try to present it as a TableModel.
     * 
     * @param source
     *            The path or URL to the data source.
     */
    public void read(String source) throws IOException {
	init();
	this.source = source;
	// RandomAccessFile
	this.raf = getRandomAccessFile(source);
	if (raf == null) {
	    throw new IOException("Unable to open " + source);
	}
	vt.setSize(0);
	// This wait is for indexing in a Thread
	while (!linesIndexed && linesRead < 100) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException ex) {
	    }
	}
	int lineSample = 100; // try to stay below pagination rate
	int numRead = linesRead;
	int lcnt = numRead < lineSample ? numRead : lineSample;
	// collect some lines to analyze
	String[] lines = new String[lcnt];
	for (int i = 0; i < lines.length; i++) {
	    lines[i] = readRow(i);
	}

	// guess the file separator
	fs = guessFieldSeparator(lines);

	// collect column stats
	Vector csv = collectColumnStats(0, lcnt);
	ColStats[] csa = (ColStats[]) csv.toArray(new ColStats[csv.size()]);
	// determine header rows
	int hd = 0;
	for (int c = 0; c < csa.length; c++) {
	    // if firstNonString == 0, assume no header
	    if (csa[c].firstNonString == 0) {
		hd = 0;
		break;
	    }
	    // if firstNumDate < lcnt && lastString < firstNumDate
	    if (csa[c].firstNumDate < lcnt
		    && csa[c].lastString < csa[c].firstNumDate) {
		if (hd < csa[c].firstNonString) {
		    hd = csa[c].firstNonString;
		    // System.err.println( c + " hd= " + hd);
		}
	    }
	}
	dataRowOffset = hd;
	if (dataRowOffset > 0) {
	    columnName = new String[csa.length];
	    for (int r = 0; r < dataRowOffset; r++) {
		Vector rv = parseRow(null, null, lines[r]);
		if (rv != null) {
		    for (int c = 0; c < csa.length; c++) {
			if (c < rv.size()) {
			    Object obj = rv.get(c);
			    if (obj != null) {
				if (columnName[c] == null) {
				    columnName[c] = obj.toString();
				} else {
				    columnName[c] += obj.toString();
				}
			    }
			}
		    }
		}
	    }
	}
	// determine column classes
	columnClass = new Class[csa.length];
	colMap = new CacheColumnMap[csa.length];
	for (int c = 0; c < csa.length; c++) {
	    if (csa[c].firstNumDate < lcnt
		    && csa[c].lastString <= csa[c].firstNumDate) {
		if (csa[c].numCnt > 0) {
		    columnClass[c] = java.lang.Number.class;
		} else if (csa[c].dateCnt > 0) {
		    columnClass[c] = java.util.Date.class;
		} else {
		    columnClass[c] = java.lang.String.class;
		}
	    } else {
		columnClass[c] = java.lang.String.class;
	    }
	    // System.err.println(c + "\t" + getColumnName(c) + colMap);
	    colMap[c] = new CacheColumnMap(this, null, getColumnName(c), c,
		    columnClass[c]);
	}
	setIndexedCount();
	try {
	    rdrThread = new Reader();
	    rdrThread.start();
	} catch (Exception ex) {
	    System.err.print(ex);
	} finally {
	    // close the file, wp
	    // raf.close();
	}
	fireTableStructureChanged();
    }

    /**
     * Collect the line offsets in the file.
     * 
     * @param file
     *            The file to index.
     */
    private int getLineOffsets(RandomAccessFile file) {
	linesRead = 0;
	linesIndexed = false;
	long offset = 0;
	try {
	    for (String line = file.readLine(); line != null; offset = file
		    .getFilePointer(), line = file.readLine(), linesRead++) {
		// Ignore blank lines
		while (line != null && line.length() == 0) {
		    offset = file.getFilePointer();
		    line = file.readLine();
		}
		if (line == null) {
		    break;
		}
		setRowOffset(linesRead, offset);
		if (linesRead == fetchSize) {
		    setIndexedCount();
		}
	    }
	} catch (IOException ioex) {
	    System.err.println(this.getClass().getName() + ": " + ioex);
	}
	linesIndexed = true;
	return linesRead;
    }

    /**
     * Copy a data source stream to a RandomAccessFile.
     * 
     * @param rdr
     *            The source data
     * @param file
     *            The local RandomAccessFile file to cache data.
     */
    private int copy(BufferedReader rdr, RandomAccessFile file) {
	linesRead = 0;
	linesIndexed = false;
	String nl = "\n";
	long offset = 0;
	try {
	    for (String line = rdr.readLine(); line != null; offset = writeLine(
		    file, line, nl, offset), line = rdr.readLine(), linesRead++) {
		// Ignore blank lines
		while (line != null && line.length() == 0) {
		    offset = file.getFilePointer();
		    line = file.readLine();
		}
		if (line == null) {
		    break;
		}
		setRowOffset(linesRead, offset);
		if (linesRead == fetchSize) {
		    setIndexedCount();
		}
	    }
	} catch (IOException ioex) {
	    System.err.println(this.getClass().getName() + ": " + ioex);
	}
	linesIndexed = true;
	return linesRead;
    }

    private synchronized long writeLine(RandomAccessFile file, String line,
	    String nl, long offset) throws IOException {
	file.seek(offset);
	file.writeBytes(line);
	file.writeBytes(nl);
	return file.getFilePointer();
    }

    private RandomAccessFile getRandomAccessFile(String source)
	    throws IOException {
	RandomAccessFile file = null;
	try {
	    file = new RandomAccessFile(source, "r");
	    // Open a separate RandomAccessFile for indexing
	    idxThread = new Indexer(null, new RandomAccessFile(source, "r"));
	    idxThread.start();
	    // getLineOffsets(file);
	} catch (SecurityException secex) {
	} catch (FileNotFoundException fnfex) {
	    try {
		File tmpFile = File.createTempFile("FileTableModel", "tmp");
		tmpFile.deleteOnExit();
		BufferedReader rdr = OpenInputSource.getBufferedReader(source);
		file = new RandomAccessFile(tmpFile, "rw");
		idxThread = new Indexer(rdr, file);
		idxThread.start();
		// copy(rdr,file);
	    } catch (IOException ex) {
		System.err.println(this + " : " + ex);
		throw ex;
	    }
	}
	return file;
    }

    private long getRowOffset(int rowIndex) {
	return rowOffset[rowIndex + dataRowOffset];
    }

    private void setRowOffset(int row, long offset) {
	// System.err.println("setRowOffset " + row + "\t" + offset);
	final int incr = 1000;
	if (rowOffset == null) {
	    rowOffset = new long[incr + row % incr];
	} else if (row >= rowOffset.length) {
	    long tmp[] = rowOffset;
	    rowOffset = new long[1000 + row / 1000 * 1000];
	    System.arraycopy(tmp, 0, rowOffset, 0, tmp.length);
	}
	rowOffset[row] = offset;
    }

    /**
     * Try to parse the given field as a Number.
     * 
     * @param field
     *            The value to parse.
     * @return the Number value of the field, or null if it couldn't be parsed
     *         as a Number.
     */
    public static Number getNumber(String field) {
	Number val = null;
	if (field != null) {
	    String ns = field.trim();
	    int len = ns.length();
	    if (len > 0) {
		if (Character.isDigit(ns.charAt(len - 1))) {
		    try {
			val = Double.valueOf(ns);
		    } catch (NumberFormatException nfex) {
			val = getFormattedNumber(ns);
		    }
		} else {
		    val = getFormattedNumber(ns);
		}
	    }
	}
	return val;
    }

    /**
     * Try to parse the given field as a Number using the DecimalFormat class.
     * 
     * @param field
     *            The value to parse.
     * @return the Number value of the field, or null if it couldn't be parsed
     *         as a Number.
     */
    public static Number getFormattedNumber(String field) {
	Number val = null;
	if (field != null) {
	    DecimalFormat df = new DecimalFormat();
	    ParsePosition pp = new ParsePosition(0);
	    try {
		// First try to parse this token as a Number
		pp.setIndex(0);
		// DecimalFormat needs uppercase E for exponent
		String ns = field.trim().replace('e', 'E');
		Number n = df.parse(ns, pp);
		// DecimalFormat doesn't accept E+2 it must be E2
		if (pp.getIndex() < ns.length()
			&& ns.regionMatches(pp.getIndex(), "E+", 0, 2)) {
		    ns = ns.substring(0, pp.getIndex() + 1)
			    + ns.substring(pp.getIndex() + 2);
		    pp.setIndex(0);
		    n = df.parse(ns, pp);
		}
		// Now check if entire token could be parsed as a Number
		if (pp.getIndex() >= ns.length()) {
		    val = n;
		}
	    } catch (Exception exn) {
	    }
	}
	return val;
    }

    static String dateFormats[] = { "EEE MMM d hh:mm:ss z yyyy", // Mon Sep
	    // 24
	    // 11:53:19
	    // CDT 2001
	    "yyyy/MM/dd kk:mm", // 2001/10/31 13:58 (yyyy/MM/dd kk:mm)
	    "yyyy.MM.dd hh:mm:ss", // YYYY.MM.DD hh:mm:ss
	    "yyyy-MM-dd hh:mm:ss", // YYYY-MM-DD hh:mm:ss
	    "yyyy.MM.dd", // YYYY.MM.DD
	    "yyyy/MM/dd", // YYYY/MM/DD
	    "dd-MMM-yyyy", // DD-Mon-yyyy
	    "ddMMMyyyy", // DDMonyyyy
	    "dd MMM yyyy", // DD Mon yyyy
	    "EEE, MMM d, yyyy", // Day, Mon day, yyyy
    };

    public static Date getDate(String field) {
	Date date = null;
	if (field != null) {
	    SimpleDateFormat sdf = new SimpleDateFormat();
	    sdf.setLenient(true);
	    for (int i = 0; i < dateFormats.length; i++) {
		sdf.applyPattern(dateFormats[i]);
		date = getDate(field, sdf);
		if (date != null) {
		    break;
		}
	    }
	}
	return date;
    }

    public static Date getDate(String field, DateFormat sdf[]) {
	Date date = null;
	if (field != null) {
	    for (int i = 0; i < sdf.length; i++) {
		date = getDate(field, sdf[i]);
		if (date != null) {
		    break;
		}
	    }
	}
	return date;
    }

    public static Date getDate(String field, DateFormat sdf) {
	if (field != null) {
	    ParsePosition pp = new ParsePosition(0);
	    try {
		Date date = sdf.parse(field, pp);
		if (date != null && pp.getIndex() >= field.length()) {
		    return date;
		}
	    } catch (Exception exn) {
	    }
	}
	return null;
    }

    private synchronized String readRow(int rowIndex) throws IOException {
	raf.seek(getRowOffset(rowIndex));
	return raf.readLine();
    }

    private Vector parseRow(Vector rv, Class[] colClass, String line) {
	int ncol = colClass != null ? colClass.length : 0;
	if (rv == null) {
	    rv = new Vector(ncol);
	} else {
	    rv.clear();
	}
	if (colClass != null) {
	    rv.setSize(colClass.length);
	}
	String vals[] = null;
	try {
	    String _fs = fs;
	    if (_fs.equals("|"))
		_fs = "\\" + _fs;
	    vals = line.split(_fs);
	    if (vals != null) {
		for (int c = 0; c < vals.length; c++) {
		    Object obj = vals[c];
		    if (vals[c] != null && colClass != null
			    && c < colClass.length) {
			if (java.lang.Number.class
				.isAssignableFrom(colClass[c])) {
			    obj = getNumber(vals[c]);
			} else if (java.util.Date.class
				.isAssignableFrom(colClass[c])) {
			    obj = getDate(vals[c]);
			}
		    }
		    if (c < rv.size()) {
			rv.set(c, obj);
		    } else {
			rv.add(obj);
		    }
		}
	    }
	} catch (NullPointerException npex) {
	} catch (Exception ex) {
	    if (ex instanceof java.util.regex.PatternSyntaxException) {
		System.err.println("pattern " + ex);
	    }
	} catch (Throwable t) {
	    // System.err.println("t " + t);
	    if (t instanceof NoSuchMethodError
		    || t instanceof NoSuchMethodException) { // pre J2SE1.4
		// do it the
		// hard way
		// System.err.println("noSuch " + t);
		int len = line.length();
		int c = 0; // column index
		String field = null;
		int fsl = fs.length();
		for (int i = 0; i < len; c++) {
		    int fi = line.indexOf(fs, i);
		    if (fi >= i) {
			field = line.substring(i, fi);
			i = fi + fsl;
		    } else {
			field = line.substring(i);
			i = len;
		    }
		    Object obj = field;
		    if (field != null && colClass != null
			    && c < colClass.length) {
			if (java.lang.Number.class
				.isAssignableFrom(colClass[c])) {
			    obj = getNumber(field);
			} else if (java.util.Date.class
				.isAssignableFrom(colClass[c])) {
			    obj = getDate(field);
			}
		    }
		    if (c < rv.size()) {
			rv.set(c, obj);
		    } else {
			rv.add(obj);
		    }
		}
	    }
	}
	return rv;
    }

    public static String guessFieldSeparator(List lines) {
	String[] la = new String[lines.size()];
	return guessFieldSeparator((String[]) lines.toArray(la));
    }

    public static String guessFieldSeparator(String[] lines) {
	String sep = "	"; // TAB
	Vector sepv = getPossibleFieldSeparators(lines);
	// System.err.println(sepv);
	if (sepv.size() > 0) {
	    if (sepv.size() > 1) {
		Vector v = new Vector(sepv);
		// System.err.println(sepv.size() + "/t" + v);
		for (int i = v.size() - 1; i >= 0; i--) {
		    if (Character
			    .isLetterOrDigit(((String) v.get(i)).charAt(0))) {
			v.remove(i);
		    }
		}
		for (int i = v.size() - 1; i > 0; i--) {
		    if (((String) v.get(i)).charAt(0) == '-') {
			v.remove(i);
		    }
		}
		for (int i = v.size() - 1; i > 0; i--) {
		    if (((String) v.get(i)).charAt(0) == '-') {
			v.remove(i);
		    }
		}
		// System.err.println(sepv.size() + "/t" + v);
		if (v.size() == 1) {
		    sep = (String) v.get(0);
		}
		// see if keys are adjacent
		// see if 1 is a
	    } else {
		sep = (String) sepv.get(0);
	    }
	}
	return sep;
    }

    public static Vector getPossibleFieldSeparators(List lines) {
	String[] la = new String[lines.size()];
	return getPossibleFieldSeparators((String[]) lines.toArray(la));
    }

    public static Vector getPossibleFieldSeparators(String[] lines) {
	// first line
	if (lines != null && lines.length > 0) {
	    Hashtable ht = new Hashtable();
	    for (int i = 0; i < lines[0].length(); i++) {
		String c = new String(lines[0].substring(i, i + 1));
		int[] cnt = (int[]) ht.get(c);
		if (cnt == null) {
		    cnt = new int[1];
		    ht.put(c, cnt);
		}
		cnt[0]++;
	    }
	    Set keys = ht.keySet();
	    // foreach each additional line get a count of each byte value
	    for (int l = 1; l < lines.length; l++) {
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
		    String key = (String) iter.next();
		    int cnt = ((int[]) ht.get(key))[0];
		    int n = 0;
		    for (int i = 0; i < lines[l].length(); i++) {
			i = lines[l].indexOf(key, i);
			if (i < 0) {
			    break;
			}
			n++;
		    }
		    if (n != cnt) {
			iter.remove();
		    }
		}
	    }
	    return new Vector(keys);
	}
	return new Vector(0);
    }

    private void getColumnStats(Vector rv, Vector csv, int rowIndex) {
	if (rv == null || csv == null)
	    return;
	for (int c = 0; c < rv.size(); c++) {
	    ColStats cs;
	    if (c < csv.size()) {
		cs = (ColStats) csv.elementAt(c);
	    } else {
		cs = new ColStats();
		csv.addElement(cs);
	    }
	    cs.valCnt++;
	    Object obj = rv.get(c);
	    if (obj == null || obj.toString().trim().length() < 1) {
		cs.nullCnt++;
		if (rowIndex < cs.firstNonString)
		    cs.firstNonString = rowIndex;
		continue;
	    } else if (!(obj instanceof Number || obj instanceof Date)) {
		String field = obj == null ? null : obj.toString();
		String trimField = field.trim();
		// Try parsing as a Number
		obj = getNumber(field);
		// Try parsing as a Date
		if (obj == null) {
		    obj = getDate(trimField);
		}
		// a String
		if (obj == null) {
		    obj = field;
		}
	    }
	    if (obj instanceof Number) {
		Number n = (Number) obj;
		cs.numCnt++;
		double dv = n.doubleValue();
		if (Double.isNaN(cs.minNum) || cs.minNum > dv)
		    cs.minNum = dv;
		if (Double.isNaN(cs.maxNum) || cs.maxNum < dv)
		    cs.maxNum = dv;
		if (rowIndex < cs.firstNonString)
		    cs.firstNonString = rowIndex;
		if (rowIndex < cs.firstNumDate)
		    cs.firstNumDate = rowIndex;
	    } else if (obj instanceof Date) {
		Date date = (Date) obj;
		cs.dateCnt++;
		if (cs.minDate == null || cs.minDate.after(date))
		    cs.minDate = date;
		if (cs.maxDate == null || cs.maxDate.before(date))
		    cs.maxDate = date;
		if (rowIndex < cs.firstNonString)
		    cs.firstNonString = rowIndex;
		if (rowIndex < cs.firstNumDate)
		    cs.firstNumDate = rowIndex;
	    } else {
		String field = obj.toString();
		int flen = field.length();
		if (flen > cs.maxLen)
		    cs.maxLen = flen;
		if (rowIndex > cs.lastString)
		    cs.lastString = rowIndex;
	    }
	}
	if (debug && rowIndex < 4) {
	    System.err.println(rowIndex + " getColumnStats: " + rv);
	    System.err.println(rowIndex + " getColumnStats: " + csv);
	}
    }

    private Vector collectColumnStats(int fromLineNum, int numLines) {
	Vector csv = new Vector();
	Vector rv = new Vector();
	String line;
	int ncols = 0;
	for (int i = 0, r = fromLineNum; i < numLines; i++, r++) {
	    try {
		line = readRow(r);
		if (line == null)
		    break;
		rv = parseRow(rv, null, line);
		getColumnStats(rv, csv, r);
	    } catch (IOException ioex) {
		System.err.println("collectColumnStats " + ioex);
	    }
	}
	return csv;
    }

    private synchronized Vector getRow(int rowIndex) throws IOException {
	/*
	 * This is synchronized because the size is set by parseRow before the
	 * values are filled in. This would permit another thread to access the
	 * row, between the time the size is set and the values inserted.
	 */
	Vector rv = vt.get(rowIndex);
	// System.err.println("getRow " + rowIndex + "\t" + rv.size());
	if (rv == null || rv.size() < 1) {
	    rv = parseRow(rv, columnClass, readRow(rowIndex));
	    // System.err.println("prsRow " + rowIndex + "\t" + rv.size()+
	    // "\n");
	}
	return rv;
    }

    // TableModel interface
    public int getRowCount() {
	int rowCnt = linesRead - dataRowOffset;
	return rowCnt < 0 ? 0 : rowCnt;
    }

    public int getColumnCount() {
	return columnClass != null ? columnClass.length : 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
	    throws ArrayIndexOutOfBoundsException {
	if (colMap != null && colMap.length > columnIndex) {
	    return colMap[columnIndex].getValueAt(rowIndex);
	}
	if (rowIndex >= vt.getSize()) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	try {
	    Vector rv = getRow(rowIndex);
	    if (rv != null) {
		try {
		    return rv.get(columnIndex);
		} catch (Exception ex) {
		    throw new ArrayIndexOutOfBoundsException();
		}
	    }
	} catch (IOException ioex) {
	    System.err.println(this + ".getValueAt(" + rowIndex + ","
		    + columnIndex + ") " + ioex);
	}
	return null;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
	ColumnMap cmap = getColumnMap(columnIndex);
	if (cmap != null) {
	    return cmap.getColumnClass();
	}
	return columnClass != null && columnIndex < columnClass.length ? columnClass[columnIndex]
		: java.lang.Object.class;
    }

    @Override
    public String getColumnName(int columnIndex) {
	if (columnName != null && columnIndex >= 0
		&& columnIndex < columnName.length) {
	    return columnName[columnIndex];
	}
	return super.getColumnName(columnIndex);
    }

    /*
     * TableModel interface: public boolean isCellEditable(int rowIndex, int
     * columnIndex); public Object getValueAt(int rowIndex, int columnIndex);
     * public void setValueAt(Object aValue, int rowIndex, int columnIndex);
     * public void addTableModelListener(TableModelListener l); public void
     * removeTableModelListener(TableModelListener l);
     */

    public void run() {
    }

    // interface TableColumnMap
    /**
     * Return a ColumnMap for the column in the TableModel at columnIndex.
     * 
     * @param columnIndex
     *            the index of the TableModel column.
     * @return a ColumnMap for the TableModel column at columnIndex.
     */
    public ColumnMap getColumnMap(int columnIndex) {
	CacheColumnMap cMap[] = colMap;
	// System.err.println("getColumnMap " + columnIndex);
	if (cMap != null && columnIndex >= 0 && columnIndex < cMap.length) {
	    return cMap[columnIndex];
	}
	// System.err.println("!getColumnMap " + columnIndex);
	return null;
    }

    private synchronized void mapColumns() {
    }

}
