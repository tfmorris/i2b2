/*
 * @(#) $RCSfile: DefaultSetOperator.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.Vector;
import java.util.Enumeration;
import java.awt.event.InputEvent;
import javax.swing.event.*;

/**
 * Maintains the state of the SetOperator.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DefaultSetOperator implements Serializable, SetOperator {
    int setOperator = SetOperator.REPLACE;
    private EventListenerList listenerList = new EventListenerList();
    protected transient ChangeEvent changeEvent = null;
    private static int modifierMask = InputEvent.BUTTON1_MASK
	    | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK
	    | InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK
	    | InputEvent.ALT_MASK | InputEvent.META_MASK;
    static Vector defaultMasks = new Vector(6);
    private static int[] REPLACE_MASKS = {
    // InputEvent.BUTTON1_MASK
    0 // use the use the previously set value for MouseButton1
    };
    private static int[] UNION_MASKS = { InputEvent.BUTTON2_MASK,
	    InputEvent.SHIFT_MASK | InputEvent.BUTTON1_MASK };
    private static int[] INTERSECTION_MASKS = { InputEvent.CTRL_MASK
	    | InputEvent.BUTTON1_MASK };
    private static int[] DIFFERENCE_MASKS = { InputEvent.BUTTON3_MASK,
	    InputEvent.ALT_MASK | InputEvent.BUTTON1_MASK };
    private static int[] XOR_MASKS = { InputEvent.META_MASK
	    | InputEvent.BUTTON1_MASK };
    static {
	// REPLACE
	defaultMasks.addElement(REPLACE_MASKS);
	// BRUSHOVER
	defaultMasks.addElement(REPLACE_MASKS);
	// UNION
	defaultMasks.addElement(UNION_MASKS);
	// INTERSECTION
	defaultMasks.addElement(INTERSECTION_MASKS);
	// DIFFERENCE
	defaultMasks.addElement(DIFFERENCE_MASKS);
	// XOR
	defaultMasks.addElement(XOR_MASKS);
    }
    Vector masks = (Vector) defaultMasks.clone();

    /**
     * Contruct an empty DefaultSetOperator.
     */
    public DefaultSetOperator() {
    }

    /**
     * Set the selection set operator to use.
     * 
     * @param setOperator
     *            the set operation to apply to selections.
     * @see SetOperator#REPLACE
     * @see SetOperator#BRUSHOVER
     * @see SetOperator#UNION
     * @see SetOperator#INTERSECTION
     * @see SetOperator#DIFFERENCE
     * @see SetOperator#XOR
     */
    public void setSetOperator(int setOperator) {
	if (setOperator < 0 || setOperator > SetOperator.XOR)
	    return;
	if (this.setOperator != setOperator) {
	    this.setOperator = setOperator;
	    setOperatorChanged();
	}
    }

    /**
     * Return the selection set operator being used.
     * 
     * @return the selection set operator used to create selection sets.
     */
    public int getSetOperator() {
	return setOperator;
    }

    public void setFromInputEventMask(int mask) {
	setSetOperator(getFromInputEventMask(mask));
    }

    public int getFromInputEventMask(int mask) {
	int idx = 0;
	for (Enumeration e = masks.elements(); e.hasMoreElements(); idx++) {
	    int m[] = (int[]) e.nextElement();
	    if (m == null) {
		continue;
	    }
	    for (int i = 0; i < m.length; i++) {
		if ((mask & modifierMask) == m[i]) {
		    return idx;
		}
	    }
	}
	return -1;
    }

    public void setInputEventMasks(int setOperator, int masks[]) {
	try {
	    this.masks.setElementAt(masks, setOperator);
	} catch (ArrayIndexOutOfBoundsException ae) {
	}
    }

    public int[] getInputEventMasks(int setOperator) {
	return (int[]) masks.elementAt(setOperator);
    }

    public void addChangeListener(ChangeListener listener) {
	listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
	listenerList.remove(ChangeListener.class, listener);
    }

    protected void setOperatorChanged() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ChangeListener.class) {
		// Lazily create the event:
		// if (changeEvent == null)
		changeEvent = new ChangeEvent(this);
		((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
	    }
	}
    }
}
