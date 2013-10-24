/*
 * @(#) $RCSfile: AbstractGraphItem.java,v $ $Revision: 1.4 $ $Date: 2008/10/14 21:42:57 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;
import javax.swing.event.*;

/**
 * AbstractGraphItem provides a skeletal implementation of the GraphItem
 * interface, to minimize the effort required to implement this interface. It
 * provides common methods to set the data values for the item to be displayed.
 * The data values are interpreted by a DataModel that will attempt to convert
 * any array into an array of double values that can be positioned by the axes
 * of the graph.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/14 21:42:57 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see DataModel
 */
public abstract class AbstractGraphItem implements GraphItem {
    protected EventListenerList listenerList = new EventListenerList();
    protected Color color;
    protected IndexedColor indexedColor;
    protected String label;
    protected DataModel dataModel;
    // Previous draw settings
    Rectangle rect;
    Axis axes[] = new Axis[2];
    ChangeListener dataListener = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	    fireChangeEvent();
	}
    };

    /**
     * Set the color of the graph item.
     * 
     * @param color
     *            for this graph item.
     */
    public void setColor(Color color) {
	this.color = color;
	fireChangeEvent();
    }

    /**
     * Return the color of the graph item.
     * 
     * @return color of the graph item.
     */
    public Color getColor() {
	return color;
    }

    /**
     * Set the color for each drawable of the graph item.
     * 
     * @param indexedColor
     *            the color for each drawable of this graph item.
     */
    public void setIndexedColor(IndexedColor indexedColor) {
	this.indexedColor = indexedColor;
	fireChangeEvent();
    }

    /**
     * Return the color of the graph item.
     * 
     * @return color of the graph item.
     */
    public IndexedColor getIndexedColor() {
	return indexedColor;
    }

    /**
     * Set the label of the graph item.
     * 
     * @param label
     *            for this graph item.
     */
    public void setLabel(String label) {
	this.label = label;
	fireChangeEvent();
    }

    /**
     * Return the label of the graph item.
     * 
     * @return label of the graph item.
     */
    public String getLabel() {
	return label;
    }

    /**
     * Set the data values to be displayed by on the graph.
     * 
     * @param rawData
     *            an array of x and y values
     * @param format
     *            a String indicating the position of X and Y values in the
     *            array.
     * @see GraphDataModel
     * @see GraphDataModel#FORMAT_Y
     * @see GraphDataModel#FORMAT_XY
     */
    public void setData(Object rawData, String format)
	    throws IllegalArgumentException {
	GraphDataModel dm = dataModel != null
		&& dataModel instanceof GraphDataModel ? (GraphDataModel) dataModel
		: new GraphDataModel();
	dm.setData(rawData, format);
	setData(dm);
    }

    /**
     * Set the data values to be displayed by on the graph.
     * 
     * @param rawData
     *            an array of x and y values
     * @see GraphDataModel
     */
    public void setData(Object rawData) throws IllegalArgumentException {
	setData(rawData, GraphDataModel.FORMAT_Y);
    }

    /**
     * Set the data model that provides the x and y values for the graphed item.
     * 
     * @param dataModel
     *            provides the x and y values for the graphed item.
     * @see GraphDataModel
     */
    public void setData(DataModel dataModel) {
	if (this.dataModel != null
		&& this.dataModel instanceof MutableDataModel) {
	    ((MutableDataModel) this.dataModel)
		    .removeChangeListener(dataListener);
	}
	this.dataModel = dataModel;
	if (dataModel != null && dataModel instanceof MutableDataModel) {
	    ((MutableDataModel) dataModel).addChangeListener(dataListener);
	}
	rect = null;
	axes[0] = null;
	axes[1] = null;
	fireChangeEvent();
    }

    /**
     * Get the data model that provides the x and y values for the graphed item.
     * 
     * @return a dataModel that provides the x and y values for the graphed
     *         item.
     * @see GraphDataModel
     */
    public DataModel getData() {
	return dataModel;
    }

    /**
     * Draw the graph item on the graph display.
     * 
     * @param c
     *            the component upon which to draw.
     * @param g
     *            the graphics context.
     * @param r
     *            the area of the graph within the component.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     */
    public abstract void draw(Component c, Graphics g, Rectangle r, Axis xAxis,
	    Axis yAxis);

    /**
     * Returns whether this graph item intersects the given rectangle.
     * 
     * @param r
     *            the area to test for intersection.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     * @return true if the item intersects the given rectangle, else false.
     */
    public abstract boolean intersects(Rectangle r, Axis xAxis, Axis yAxis);

    /**
     * Returns whether this graph item intersects the given point.
     * 
     * @param p
     *            the point to test for intersection.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     * @return true if the item intersects the given point, else false.
     */
    public abstract boolean intersects(Point p, Axis xAxis, Axis yAxis);

    /**
     * Returns whether this graph item intersects the given point.
     * 
     * @param p
     *            the point to test for intersection.
     * @param xAxis
     *            The X axis of the graph.
     * @param yAxis
     *            The Y axis of the graph.
     * @return indices of datapoints that intersects the given point, else null.
     */
    public abstract int[] getIndicesAt(Point p, Axis xAxis, Axis yAxis);

    /**
     * Add a Listener to be notified of changes.
     * 
     * @param l
     *            the listener to be added.
     */
    public void addGraphItemListener(GraphItemListener l) {
	listenerList.add(GraphItemListener.class, l);
    }

    /**
     * Remove the listener from the notification list.
     * 
     * @param l
     *            the listener to be removed.
     */
    public void removeGraphItemListener(GraphItemListener l) {
	listenerList.remove(GraphItemListener.class, l);
    }

    /**
     * Notify Listeners of change.
     */
    protected void fireChangeEvent() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	GraphItemEvent graphItemEvent = null;
	for (int i = listeners.length - 2; i >= 0; i -= 2) {
	    if (listeners[i] == GraphItemListener.class) {
		// Lazily create the event:
		if (graphItemEvent == null)
		    graphItemEvent = new GraphItemEvent(this);
		((GraphItemListener) listeners[i + 1])
			.graphItemChanged(graphItemEvent);
	    }
	}
    }

}
