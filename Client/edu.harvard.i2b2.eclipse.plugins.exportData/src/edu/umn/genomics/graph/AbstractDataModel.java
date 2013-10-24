/*
 * @(#) $RCSfile: AbstractDataModel.java,v $ $Revision: 1.3 $ $Date: 2008/10/14 21:24:06 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.graph;

import javax.swing.event.*;

/**
 * Return arrays of the x pixel location and the y pixel location for the data
 * values given the axes transformations.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/14 21:24:06 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public abstract class AbstractDataModel implements MutableDataModel {
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Return arrays of the x pixel location and the y pixel location.
     * 
     * @param x
     *            the x pixel offset
     * @param y
     *            the y pixel offset
     * @param axes
     *            the axes that transform the datapoints to the pixel area
     * @param points
     *            the array of points: xpoints, ypoints
     * @return the array of points: xpoints, ypoints
     */
    public abstract int[][] getPoints(int x, int y, Axis axes[], int points[][]);

    /**
     * Return any y values at the given x index.
     * 
     * @param xi
     *            the x index into the array.
     * @return the y values at the given x index.
     */
    public abstract double[] getYValues(int xi);

    /**
     * Add a Listener to be notified of changes.
     * 
     * @param l
     *            the listener to be added.
     */
    public void addChangeListener(ChangeListener l) {
	listenerList.add(ChangeListener.class, l);
    }

    /**
     * Remove the listener from the notification list.
     * 
     * @param l
     *            the listener to be removed.
     */
    public void removeChangeListener(ChangeListener l) {
	listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Notify Listeners of change.
     */
    protected void fireChangeEvent() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	ChangeEvent changeEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == ChangeListener.class) {
		// Lazily create the event:
		if (changeEvent == null)
		    changeEvent = new ChangeEvent(this);
		((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
	    }
	}
    }

}
