/*
 * @(#) $RCSfile: IndexFractionSelected.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.ListSelectionModel;
import javax.swing.event.*;

/**
 * Determines the fraction of values in list, or a array indexed subset of a
 * list, that are selected.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class IndexFractionSelected implements ListSelectionListener {
    /**
     * The list of ChangeListeners for this model. Subclasses may store their
     * own listeners here.
     */
    protected EventListenerList listenerList = new EventListenerList();
    /** The fraction of row indices selected. */
    double fractionSelected = 0.;
    int listLength = 0;
    /** The array of row indices. */
    int[] rowMap = null;

    /**
     * Determines the fraction of a sublist of a list that is selected.
     * 
     * @param rowMap
     *            an index of rows in the list that comprise this sublist.
     * @param lsm
     *            if not null, the fraction is set using the current state of
     *            this ListSelectionModel.
     */
    public IndexFractionSelected(int[] rowMap, ListSelectionModel lsm) {
	this.rowMap = rowMap;
	this.listLength = rowMap.length;
	if (lsm != null) {
	    ListSelectionEvent e = new ListSelectionEvent(lsm, lsm
		    .getMinSelectionIndex(), lsm.getMaxSelectionIndex(), lsm
		    .getValueIsAdjusting());
	    valueChanged(e);
	}
    }

    /**
     * Determines the fraction of a list of length listLength that is selected.
     * 
     * @param listLength
     *            length of the list.
     * @param lsm
     *            if not null, the fraction is set using the current state of
     *            this ListSelectionModel.
     */
    public IndexFractionSelected(int listLength, ListSelectionModel lsm) {
	this.listLength = listLength;
	if (lsm != null) {
	    ListSelectionEvent e = new ListSelectionEvent(lsm, lsm
		    .getMinSelectionIndex(), lsm.getMaxSelectionIndex(), lsm
		    .getValueIsAdjusting());
	    valueChanged(e);
	}
    }

    /**
     * Adds a ChangeListener to the listener list. The ChangeListeners must be
     * notified when the fractionSelected value changes.
     * 
     * @param listener
     *            the ChangeListener to add
     * @see #removeChangeListener
     */
    public void addChangeListener(ChangeListener listener) {
	listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes a ChangeListener from the model's listener list.
     * 
     * @param listener
     *            the ChangeListener to remove
     * @see #addChangeListener
     */

    public void removeChangeListener(ChangeListener listener) {
	listenerList.remove(ChangeListener.class, listener);
    }

    /**
     * Returns an array of all the <code>ChangeListener</code>s added with
     * addChangeListener().
     * 
     * @return all of the <code>ChangeListener</code>s added or an empty array
     *         if no listeners have been added
     * @see #addChangeListener
     */
    public ChangeListener[] getChangeListeners() {
	return listenerList.getListeners(ChangeListener.class);
    }

    /**
     * Run each ChangeListeners stateChanged() method.
     * 
     * @see EventListenerList
     */
    protected void fireStateChanged() {
	ChangeEvent changeEvent = null;
	Object[] listeners = listenerList.getListenerList();
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ChangeListener.class) {
		if (changeEvent == null) {
		    changeEvent = new ChangeEvent(this);
		}
		((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
	    }
	}
    }

    /**
     * Set the fractionSelected value, and fire a change event if this is a
     * change in the value.
     * 
     * @param fraction
     *            the fraction selected value.
     */
    private void setFraction(double fraction) {
	if (fractionSelected != fraction) {
	    fractionSelected = fraction;
	    fireStateChanged();
	}
    }

    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e
     *            the selection event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
	ListSelectionModel lsm = (ListSelectionModel) e.getSource();
	if (!e.getValueIsAdjusting() && listLength > 0) {
	    int cnt = 0;
	    if (rowMap != null) {
		for (int i = 0; i < rowMap.length; i++) {
		    if (lsm.isSelectedIndex(rowMap[i])) {
			cnt++;
		    }
		}
	    } else {
		int min = lsm.getMinSelectionIndex();
		if (min >= 0) {
		    for (int i = min; i <= lsm.getMaxSelectionIndex(); i++) {
			if (lsm.isSelectedIndex(i)) {
			    cnt++;
			}
		    }
		}
	    }
	    setFraction((double) cnt / listLength);
	}
    }

    /**
     * Returns a value between 0 and 1 which represents the fraction of the
     * indices that are selected.
     * 
     * @return the fraction (between 0. and 1.) of the indices selected.
     */
    public double getFraction() {
	return fractionSelected;
    }

}
