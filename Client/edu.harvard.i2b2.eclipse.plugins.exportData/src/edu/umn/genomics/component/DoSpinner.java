/*
 * @(#) $RCSfile: DoSpinner.java,v $ $Revision: 1.2 $ $Date: 2008/10/30 20:56:40 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.component;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Hide all references to JSpinner and SpinnerNumberModel, and don't call this
 * class unless we're j2se1.4 or later.
 * 
 * try { // Check for JSpinner class Class.forName("javax.swing.JSpinner"); //
 * Found it so we can reference DoSpinner which will // cause the ClassLoader to
 * load javax.swing.JSpinner JComponent comp = DoSpinner.getComponent(1,1,10,1);
 * ChangeListener cl = new ChangeListener() { public void
 * stateChanged(ChangeEvent e) { DoSpinner.getValue(e.getSOurce()); } };
 * DoSpinner.addChangeListener(comp,cl); } catch (ClassNotFoundException e) { }
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/30 20:56:40 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.JSpinner
 */
public class DoSpinner {

    /**
   *
   */
    public static JComponent getComponent(int value, int min, int max, int step) {
	return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    public static JComponent getComponent(double value, double min, double max,
	    double step) {
	return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    public static void addChangeListener(JComponent comp,
	    ChangeListener listener) {
	((JSpinner) comp).addChangeListener(listener);
    }

    public static Object getValue(Object comp) {
	if (comp instanceof JSpinner) {
	    return ((JSpinner) comp).getValue();
	}
	return null;
    }

    public static void setValue(JComponent comp, Object value) {
	if (comp instanceof JSpinner) {
	    ((JSpinner) comp).setValue(value);
	}
    }

}
