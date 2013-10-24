/*
 * @(#) $RCSfile: ScriptBsh.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.*;
import java.util.*;
import bsh.Interpreter;

/**
 * ScriptBsh provides JavaScript formulas from tables.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see Cells
 */
public class ScriptBsh implements ScriptInterpreter {
    InputStream in;
    PrintStream out;
    PrintStream err;
    Map vars;
    Interpreter interp;

    public ScriptBsh(InputStream in, PrintStream out, PrintStream err, Map vars) {
	initialize(in, out, err, vars);
    }

    /**
     * Set the streams for input to and output from the script interpretter, and
     * initialize global variables.
     * 
     * @param in
     *            The input stream for the script interpretter.
     * @param out
     *            The output stream from the script interpretter.
     * @param err
     *            The error stream for the script interpretter.
     * @param vars
     *            A map of global variable names to the Objects they represent.
     */
    public void initialize(InputStream in, PrintStream out, PrintStream err,
	    Map vars) {
	this.in = in;
	this.out = out;
	this.err = err;
	this.vars = vars;
    }

    public ScriptBsh() {
	this(System.in, System.out, System.err, null);
    }

    public void run() {
	interp = new Interpreter(new InputStreamReader(in), out, err, true);
	interp.setExitOnEOF(false);
	for (int i = 0; i < ScriptInterpreter.packageList.size(); i++) {
	    try {
		interp.eval("import " + ScriptInterpreter.packageList.get(i)
			+ ".*;");
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	if (vars != null) {
	    for (Iterator i = vars.keySet().iterator(); i.hasNext();) {
		String key = (String) i.next();
		try {
		    interp.set(key, vars.get(key));
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	interp.run();
    }

    public static void main(String[] args) {
	ScriptBsh bsh = new ScriptBsh();
	new Thread(bsh).start();
    }

}
