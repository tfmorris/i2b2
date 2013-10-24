/*
 * @(#) $RCSfile: AbstractColumnFormula.java,v $ $Revision: 1.5 $ $Date: 2008/10/29 20:58:46 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.Serializable;
import java.util.*;
import javax.swing.table.*;

/**
 * AbstractColumnFormula provides the common methods for caching interpretted
 * cell values and preventing recursion while calculating cell values.
 * 
 * Extending classes need to implement the method: calculateValueAt(int
 * rowIndex, int columnIndex)
 * 
 * A formula is interpreted as java code. Four variables are provided to the
 * formula:
 * <UL>
 * <LI><b> table </b> - the TableModel on which this formula operates</LI>
 * <LI><b> row </b> - the row of the cell in the table (row starts from 0)</LI>
 * <LI><b> col </b> - the column of the cell in the table (col starts from 0)</LI>
 * <LI><b> Cells </b> - the Cells class that has a number of useful static
 * methods</LI>
 * </UL>
 * The formula needs to return a value. Example formulas:
 * <UL>
 * <LI>
 * 
 * <PRE>
 * &lt;CODE&gt;
 *    // return column 1 - column 0
 *    table.getValueAt(row, 1) - table.getValueAt(row, 0);
 * &lt;/CODE&gt;
 * </PRE>
 * 
 * </LI>
 * <LI>
 * 
 * <PRE>
 * &lt;CODE&gt;
 *    // return column 1 - column 0 with max number of decimal places
 *    double dp = 100; // set number of decimal places
 *    Math.round(dp*(table.getValueAt(row,1)-table.getValueAt(row,0)))/dp;
 * &lt;/CODE&gt;
 * </PRE>
 * 
 * </LI>
 * <LI>
 * 
 * <PRE>
 * &lt;CODE&gt;
 *    // return the sin of column 0
 *    Math.sin(((Number)table.getValueAt(row, 0)).doubleValue());
 * &lt;/CODE&gt;
 * </PRE>
 * 
 * </LI>
 * <LI>
 * 
 * <PRE>
 * &lt;CODE&gt;
 *    // return the sum of the preceeding columns
 *    double sum = 0.;
 *    for (int c = 0; c &lt; col; c++) {
 *      Object v = table.getValueAt(row,c);
 *      if (v instanceof Number) {
 *        sum += ((Number)v).doubleValue();
 *      }
 *    }
 *    return sum;
 * &lt;/CODE&gt;
 * </PRE>
 * 
 * </LI>
 * </UL>
 * 
 * @author J Johnson
 * @version $Revision: 1.5 $ $Date: 2008/10/29 20:58:46 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see Cells
 */
public abstract class AbstractColumnFormula implements Serializable,
	TableModelFormula {
    String name = null;
    TableModel tm;
    String script = "";
    /**
     * This holds a list of cells visited during a getValueAt() call it is used
     * to prevent infinite recursion by self reference. No attempt has been made
     * to make this Thread safe.
     */
    protected Vector path = new Vector();
    protected Vector values = new Vector();
    protected BitSet valueSet = new BitSet();

    public AbstractColumnFormula() {
	this("", null, "");
    }

    public AbstractColumnFormula(String name, TableModel tableModel,
	    String formula) {
	setName(name);
	setTableModel(tableModel);
	setFormula(formula);
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name != null ? name : script != null ? script : "";
    }

    public void setTableModel(TableModel tableModel) {
	this.tm = tableModel;
    }

    public TableModel getTableModel() {
	return tm;
    }

    public String getFormula() {
	return script;
    }

    public void setFormula(String formula) {
	this.script = formula;
	recalculate();
    }

    /**
     * Return a List of java package names the Intrepreter should import, e.g.
     * "java.util".
     * 
     * @return a List of java package names the Intrepreter should import.
     */
    protected List getPackageList() {
	return TableModelFormula.packageList;
    }

    /**
     * Return a List of java class names the Intrepreter should import, e.g.
     * "edu.umn.genomics.table.Cells".
     * 
     * @return a List of java class names the Intrepreter should import.
     */
    protected List getClassList() {
	return TableModelFormula.classList;
    }

    public void recalculate() {
	valueSet.clear();
	values.clear();
    }

    /**
     * Returns the value for the table cell at row and column indices. This is
     * synchronized to prevent clashes in detecting recursion while evauting
     * formulas.
     * 
     * @param rowIndex
     *            The table row of the cell.
     * @param columnIndex
     *            The table column of the cell.
     * @return The table cell value.
     */
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
	Object obj = null;
	if (values != null && values.size() > rowIndex
		&& valueSet.get(rowIndex)) {
	    obj = values.get(rowIndex);
	} else {
	    obj = retrieveValueAt(rowIndex, columnIndex);
	    setValueAt(obj, rowIndex);
	}
	return obj;
    }

    /**
     * This method keeps track of the cells traversed in order to prevent
     * recursion.
     * 
     * @param rowIndex
     *            The table row of the cell to evaluate.
     * @param columnIndex
     *            The table column of the cell to evaluate.
     * @return The cell value result from interpretting the formula.
     */
    protected Object retrieveValueAt(int rowIndex, int columnIndex) {
	Object result = null;
	try {
	    String cell = "r" + rowIndex + "c" + columnIndex;
	    if (path.contains(cell)) {
		result = new Exception("Recursion evaluating cell "
			+ path.get(0) + " at " + cell); // recursion
	    } else {
		path.addElement(cell);
		result = calculateValueAt(rowIndex, columnIndex);
		path.removeElement(cell);
	    }
	} catch (Exception ex) {
	    result = ex;
	    // System.err.println("AbstractColumnFormula " + ex.getClass() + " "
	    // + ex);
	    ex.printStackTrace();
	} finally {
	}
	return result;
    }

    /**
     * This evalutes the cell value by interpretting the formula.
     * 
     * @param rowIndex
     *            The table row of the cell to evaluate.
     * @param columnIndex
     *            The table column of the cell to evaluate.
     * @return The cell value result from interpretting the formula.
     */
    protected abstract Object calculateValueAt(int rowIndex, int columnIndex);

    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }

    /**
     * Set the cell value in the cache.
     * 
     * @param aValue
     *            The value for the table cell.
     * @param rowIndex
     *            The table row of the cell to evaluate.
     */
    protected void setValueAt(Object aValue, int rowIndex) {
	// ensure size
	// values.ensureCapacity(rowIndex+1);
	if (values.size() <= rowIndex) {
	    values.setSize(rowIndex + 1);
	}
	// set value
	values.set(rowIndex, aValue);
	// mark bit
	valueSet.set(rowIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	if (isCellEditable(rowIndex, columnIndex)) {
	    setValueAt(aValue, rowIndex);
	}
    }

    public static int getColumnByName(TableModel tm, String name) {
	if (tm != null && name != null) {
	    int cc = tm.getColumnCount();
	    for (int i = 0; i < cc; i++) {
		if (name.equals(tm.getColumnName(i)))
		    return i;
	    }
	    for (int i = 0; i < cc; i++) {
		if (name.equalsIgnoreCase(tm.getColumnName(i)))
		    return i;
	    }
	}
	return -1;
    }

    @Override
    public String toString() {
	return getName();
    }

    protected static String interpreterInfo = ""
	    + " A formula is interpreted as java code by Script Interpreter. \n"
	    + "\n";

    protected static String variableInfo = ""
	    + "\n"
	    + " Four variables are provided to the formula: \n"
	    + "   table - the javax.swing.table.TableModel on which this formula operates \n"
	    + "   row - the row of the cell in the table (row starts from 0) \n"
	    + "   col - the column of the cell in the table (col starts from 0) \n"
	    + "   Cells - the Cells class that has a number of useful static methods.\n"
	    + "\n"
	    + " The formula should return a value. \n"
	    + "\n"
	    + "\n"
	    + " The Cells class provides a means to get a collection of table cells. "
	    + " (Cells uses java.util.Collection interface for Java2, "
	    + "  for JDK1.1 it uses java.util.Vector only.) \n"
	    + " Cells provides the following static methods:\n"
	    + "  Vector getValuesFrom(TableModel tableModel, int from_row, int from_col, int to_row, int to_col)\n"
	    + "  \tReturn a list of cells from a rectangular portion of the table.\n"
	    + "  Object min(Collection cells)\n"
	    + "  \tReturn the cell with minimum value of the cells in the list.\n"
	    + "  Object max(Collection cells)\n"
	    + "  \tReturn the cell with maximum value of the cells in the list.\n"
	    + "  Object median(Collection cells)\n"
	    + "  \tReturn the median value of all Number-typed cells in the list.\n"
	    + "  double sum(Collection cells)\n"
	    + "  \tReturn the sum of all Number-typed cells in the list of cells.\n"
	    + "  double average(Collection cells)\n"
	    + "  \tReturn the average mean value of all Number-typed cells in the list.\n"
	    + "  double variance(Collection cells)\n"
	    + "  \tReturn the variance of all Number-typed cells in the list.\n"
	    + "  double stddev(Collection cells)\n"
	    + "  \tReturn the standard deviation of all Number-typed cells in the list.\n"
	    + "  int count(Collection cells)\n"
	    + "  \tReturn the count of cells in the list.\n"
	    + "  int count(Collection cells, Object obj)\n"
	    + "  \tReturn the number of times the given obj appears among the cells in the list.\n"
	    + "  int count(Collection cells, Class javaClass)\n"
	    + "  \tReturn the number of cells in the list are of the given java class.\n"
	    + "  Set distinct(Collection cells)\n"
	    + "  \tReturn the set of distinct cells in the list with duplicates removed.\n"
	    + "  List sort(Collection cells)\n"
	    + "  \tSort the cells in place.  \n"
	    + "  List reverse(Collection cells)\n"
	    + "  \tReverse the order of the cells in place.  \n"
	    + "  List fill(Collection cells, Object obj)\n"
	    + "  \tFill the list with the given obj.  \n"
	    + "  List getCommonClasses(Collection collection)\n"
	    + "  \tReturn a list of java Classes that all cells in the list belong to.\n"
	    + "  Class getCommonClass(Collection collection)\n"
	    + "  \tReturn the most specific java Class that all cells in the list belong to.\n"
	    + "  Set getCommonInterfaces(Collection collection)\n"
	    + "  \tReturn a list of java interfaces that all cells in the list implement.\n"
	    + "\n";

    protected static String exampleInfo = "" + "\n" + " Example formulas: \n"
	    + "\n" + "//Example 1\n" + "   // return column 1 - column 0 \n"
	    + "   table.getValueAt(row, 1) - table.getValueAt(row, 0); \n"
	    + "\n";

    public static String getHelpText() {
	return interpreterInfo + variableInfo + exampleInfo;
    }

    /**
     * Get a type name for this cell.
     * 
     * @return The type name for this cell.
     */
    public String getType() {
	return "Formula";
    }

    /**
     * Get a description of this VirtualCell.
     * 
     * @return a description of this VirtualCell.
     */
    public String getDescription() {
	return getFormula();
    }

}
