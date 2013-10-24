/*
 * @(#) $RCSfile: TableModelFormula.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.table.TableModel;

/**
 * TableModelFormula
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public interface TableModelFormula extends VirtualCell {
    static String[] classes = { "edu.umn.genomics.table.Cells", };
    static String[] pkgs = { "edu.umn.genomics.table", "java.lang",
	    "java.math", "java.util", "java.util.regex", "java.text",
	    "java.lang.reflect", "java.beans", "javax.swing.table", "java.sql",
	    "javax.sql", "java.io", "java.nio", "java.net", };
    /** A List of java packages the Intrepreter should know about. */
    public static List packageList = Arrays.asList(pkgs);
    /** A List of java classes the Intrepreter should know about. */
    public static List classList = Arrays.asList(classes);

    /**
     * Force the model to remove any cached cell values and and recalculate
     * them.
     */
    public void recalculate();

    /**
     * Get the formula used to generate cell values.
     * 
     * @return The formula used to generate cell values.
     */
    public String getFormula();

    /**
     * Set the formula used to generate cell values.
     * 
     * @param formula
     *            The formula used to generate cell values.
     */
    public void setFormula(String formula);

    /**
     * Set the TableModel that the formulas will evaluate.
     * 
     * @param tableModel
     *            The TableModel that the formulas will evaluate.
     */
    public void setTableModel(TableModel tableModel);

    /**
     * Get the TableModel that the formulas will evaluate.
     * 
     * @return The TableModel that the formulas will evaluate.
     */
    public TableModel getTableModel();
}
