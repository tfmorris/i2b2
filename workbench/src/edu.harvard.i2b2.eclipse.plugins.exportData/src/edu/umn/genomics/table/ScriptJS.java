/*
 * @(#) $RCSfile: ScriptJS.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.*;
import org.mozilla.javascript.tools.ToolErrorReporter;

/**
 * ScriptJS provides JavaScript formulas from tables.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see Cells
 */
public class ScriptJS implements ScriptInterpreter {
    static Scriptable sharedScope = null;
    Scriptable myScope = null;

    private Scriptable getSharedScope() {
	if (sharedScope == null) {
	    Context cx = Context.enter();
	    try {
		cx.setCompileFunctionsWithDynamicScope(true);
		sharedScope = cx.initStandardObjects(null);
		ImporterTopLevel itl = new ImporterTopLevel(cx);
		NativeJavaPackage[] njp = new NativeJavaPackage[pkgs.length];
		for (int i = 0; i < ScriptInterpreter.packageList.size(); i++) {
		    njp[i] = new NativeJavaPackage(
			    (String) ScriptInterpreter.packageList.get(i));
		}
		itl.importPackage(cx, sharedScope, njp, null);
		try {
		    ScriptableObject.defineClass(sharedScope, this.getClass());
		} catch (Exception ex) {
		    System.err.println(ex);
		}
	    } finally {
		Context.exit();
	    }
	}
	return sharedScope;
    }

    InputStream in;
    PrintStream out;
    PrintStream err;
    Map vars;

    public ScriptJS(InputStream in, PrintStream out, PrintStream err, Map vars) {
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

    public ScriptJS() {
	this(System.in, System.out, System.err, null);
    }

    public void run() {
	Context cx = Context.enter(new Context());
	Global global = Main.getGlobal();
	global.setIn(in); // InputStream
	global.setOut(out); // PrintStream
	global.setErr(err); // PrintStream
	cx.setErrorReporter(new ToolErrorReporter(false, global.getErr()));
	if (vars != null) {
	    for (Iterator iter = vars.keySet().iterator(); iter.hasNext();) {
		String key = iter.next().toString();
		Object obj = vars.get(key);
		global.put(key, global, obj);
	    }
	}
	Main.processSource(cx, null);
    }

    public static void main(String[] args) {
	ScriptJS js = new ScriptJS();
	new Thread(js).start();
	// js.run();
    }

}
