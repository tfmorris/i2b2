/*
 * @(#) $RCSfile: BshFormula.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.table.*;
import bsh.Interpreter;

/**
 * BshFormula provides beanshell formulas from tables. BshFormula uses embedded
 * beanshell from the BeanShell package: http://www.beanshell.org/
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
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see Cells
 */
public class BshFormula extends AbstractColumnFormula {
    Interpreter interp = new Interpreter();

    public BshFormula() {
	this("", null, "");
    }

    public BshFormula(TableModel tableModel) {
	this("", tableModel, "");
    }

    public BshFormula(String name, TableModel tableModel) {
	this(name, tableModel, "");
    }

    public BshFormula(String name, TableModel tableModel, String formula) {
	super(name, tableModel, formula);
	init();
    }

    private void init() {
	List list = getPackageList();
	if (list != null) {
	    for (int i = 0; i < list.size(); i++) {
		try {
		    interp.eval("import " + list.get(i) + ".*;");
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	list = getClassList();
	if (list != null) {
	    for (int i = 0; i < list.size(); i++) {
		try {
		    interp.eval("import " + list.get(i) + ";");
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
    }

    @Override
    protected Object calculateValueAt(int rowIndex, int columnIndex) {
	Object result = null;
	try {
	    interp.set("table", tm);
	    interp.set("row", rowIndex);
	    interp.set("col", columnIndex);
	    interp.set("Cells", new Cells());
	    result = interp.eval(script);
	} catch (Exception ex) {
	    result = ex;
	    // System.err.println("BshFormula " + ex.getClass() + " " + ex);
	    ex.printStackTrace();
	} finally {
	}
	return result;
    }

    protected static String interpreterInfo = ""
	    + " A formula is interpreted as java code by the beanshell Interpreter. \n"
	    + " For more information see:  http://www.beanshell.org/ \n" + "\n";

    protected static String exampleInfo = ""
	    + "\n"
	    + " Example formulas: \n"
	    + "\n"
	    + "//Example 1\n"
	    + "   // return column 1 - column 0 \n"
	    + "   table.getValueAt(row, 1) - table.getValueAt(row, 0); \n"
	    + "\n"
	    + "//Example 2\n"
	    + "   // return column 1 - column 0 with max number of decimal places \n"
	    + "   double dp = 100; // set number of decimal places \n"
	    + "   Object v = table.getValueAt(row,col);\n"
	    + "   return v instanceof Number ? Math.round(((Number)v).doubleValue() * dp) / dp : null; \n"
	    + "  Example 3\n"
	    + "\n"
	    + "   // return the sin of column 0 \n"
	    + "   Math.sin(((Number)(table.getValueAt(row,col) + 1.)).doubleValue());\n"
	    + "\n"
	    + "//Example 4\n"
	    + "   // average the values in a range of rows \n"
	    + "   Cells.average(Cells.getValuesFrom(table,row-1,col-1,row+1,col-1)); \n"
	    + "\n" + "//Example 5\n"
	    + "  // return the sum of the preceeding columns \n"
	    + "  double sum = 0.; \n" + "  for (int c = 0; c < col; c++) { \n"
	    + "    Object v = table.getValueAt(row,c); \n"
	    + "    if (v instanceof Number) { \n"
	    + "      sum += ((Number)v).doubleValue(); \n" + "    } \n"
	    + "  } \n" + "  return sum; \n" + "\n";

    public static String getHelpText() {
	return interpreterInfo + variableInfo + exampleInfo;
    }

    /**
     * Get a type name for this cell.
     * 
     * @return The type name for this cell.
     */
    @Override
    public String getType() {
	return "BeanShell";
    }

}
