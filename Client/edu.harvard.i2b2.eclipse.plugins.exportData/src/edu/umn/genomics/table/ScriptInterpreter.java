/*
 * @(#) $RCSfile: ScriptInterpreter.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * ScriptInterpreter is the interface for script interpreters allowing the
 * connection to input, output, and error streams and variables mapped to java
 * Objects.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface ScriptInterpreter extends Runnable {
    static String[] pkgs = { "edu.umn.genomics.table",
	    "edu.umn.genomics.table.loaders", "edu.umn.genomics.table.cluster",
	    "edu.umn.genomics.table.cluster.cluto",
	    "edu.umn.genomics.table.dv", "edu.umn.genomics.table.dv.j3d",
	    "edu.umn.genomics.graph", "edu.umn.genomics.graph.swing",
	    "edu.umn.genomics.graph.util", "edu.umn.genomics.bi.dbutil",
	    "edu.umn.genomics.file", "edu.umn.genomics.component",
	    "edu.umn.genomics.layout", "java.lang", "java.util",
	    "java.util.regex", "java.text", "java.io", "java.nio", "java.net",
	    "java.lang.reflect", "java.beans", "java.sql", "javax.sql",
	    "java.math", "javax.swing", "javax.swing.event",
	    "javax.swing.table", "javax.swing.tree", "javax.swing.text",
	    "java.awt", "java.awt.event", };
    public static List packageList = Arrays.asList(pkgs);

    /**
     * Set the streams for input to and output from the script interpreter, and
     * initialize global variables.
     * 
     * @param in
     *            The input stream for the script interpreter.
     * @param out
     *            The output stream from the script interpreter.
     * @param err
     *            The error stream for the script interpreter.
     * @param vars
     *            A map of global variable names to the Objects they represent.
     */
    public void initialize(InputStream in, PrintStream out, PrintStream err,
	    Map vars);
}
