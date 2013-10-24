/*
 * @(#) $RCSfile: ListSelectionView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * ListSelectionView displays the values of a ListSelectionModel that are
 * selected. Each selected value is displayed as a horizontal line drawn at
 * vertical position of index / listSize down from the top of the display area.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see javax.swing.ListSelectionModel
 */
public class ListSelectionView extends JComponent implements Serializable,
	ListSelectionListener {
    ListSelectionModel lsm = null;
    int listSize = 0;
    double viewWin[] = null;

    /**
     * @param selectionModel
     *            the the selection model to display.
     * @param listSize
     *            the number of elements in the list.
     */
    public ListSelectionView(ListSelectionModel selectionModel, int listSize) {
	setSelectionModel(selectionModel);
	setListSize(listSize);
    }

    /**
     * Sets the row selection model to newModel and registers with for listener
     * notifications from the new selection model.
     * 
     * @param newModel
     *            the new selection model
     */
    public void setSelectionModel(ListSelectionModel newModel) {
	if (lsm != null) {
	    lsm.removeListSelectionListener(this);
	}
	lsm = newModel;
	if (lsm != null) {
	    lsm.addListSelectionListener(this);
	}
    }

    /**
     * Returns the ListSelectionModel being displayed.
     * 
     * @return the object that provides row selection state.
     */
    public ListSelectionModel getSelectionModel() {
	return lsm;
    }

    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e
     *            the event that characterizes the change in selection.
     */
    public void valueChanged(ListSelectionEvent e) {
	if (!e.getValueIsAdjusting()) {
	    repaint();
	}
    }

    /**
     * Set the size of the list that the ListSelectionModel operates on.
     * 
     * @param listSize
     *            the number of elements in the list.
     */
    public void setListSize(int listSize) {
	this.listSize = listSize;
	repaint();
    }

    /**
     * Return the size of the list that the ListSelectionModel operates on.
     * 
     * @return the number of elements in the list.
     */
    public int getListSize() {
	return listSize;
    }

    public void setWindow(double from, double to) {
	viewWin = new double[2];
	viewWin[0] = from;
	viewWin[1] = to;
	repaint();
    }

    /**
     * Draw the display.
     * 
     * @param g
     *            the graphics context.
     */
    @Override
    public void paintComponent(Graphics g) {
	if (lsm == null)
	    return;
	Dimension dim = getSize();
	Insets inset = getInsets();
	int w = dim.width - inset.left - inset.right;
	int h = dim.height - inset.top - inset.bottom - 1;
	int minSel = lsm.getMinSelectionIndex();
	int maxSel = lsm.getMaxSelectionIndex();
	g.setColor(getBackground());
	g.fillRect(0, 0, dim.width, dim.height);
	int yoff = inset.top;
	int y = -1;
	int ly = -1;
	int x = inset.left + 2;
	int ls = listSize > 1 ? listSize - 1 : 1;
	double s = (double) h / (double) ls;
	g.setColor(Color.blue);
	for (int i = minSel; i <= maxSel; i++) {
	    if (lsm.isSelectedIndex(i)) {
		y = yoff + (int) Math.round(i * s);
		if (y != ly) {
		    g.drawLine(x, y, w, y);
		    ly = y;
		}
	    }
	}
	if (viewWin != null) {
	    g.setColor(Color.black);
	    int y0 = yoff + (int) (viewWin[0] * h);
	    int y1 = yoff + (int) (viewWin[1] * h);
	    x = inset.left;
	    g.drawLine(x, y0, x, y1);
	    x++;
	    g.drawLine(x, y0, x, y1);
	}
    }
}
