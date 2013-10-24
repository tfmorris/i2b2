/*
 * @(#) $RCSfile: JSPanel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 * @see Cells
 */
public class JSPanel extends ScriptPanel {

    public JSPanel() throws IOException {
	this(null);
    }

    public JSPanel(Map vars) throws IOException {
	super(new ScriptJS(), vars);
    }

    public static void main(String[] args) {
	try {
	    Map vars = new Hashtable();
	    vars.put("me", vars);
	    JSPanel jsPnl = new JSPanel(vars);
	    JFrame frame = new JFrame("JavaScript");
	    frame.addWindowListener(new WindowAdapter() {
		private void doClose(WindowEvent e) {
		    System.exit(0);
		}

		@Override
		public void windowClosing(WindowEvent e) {
		    doClose(e);
		}

		@Override
		public void windowClosed(WindowEvent e) {
		    doClose(e);
		}
	    });
	    frame.getContentPane().add(jsPnl, BorderLayout.CENTER);
	    frame.pack();
	    frame.setVisible(true);
	} catch (Exception ex) {
	    System.err.println(ex.toString());
	}
    }
}
