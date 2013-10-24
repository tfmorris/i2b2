/*
 * @(#) $RCSfile: SetOperator.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.event.ChangeListener;

/**
 * The interface for objects to specify a Set operator. Methods are included to
 * allow easy mapping from InputEvent modifier masks and a Set operator.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface SetOperator {
    /**
     * Selection set operator: Replace the previous selection with the new
     * selection.
     */
    public static final int REPLACE = 0;
    /**
     * Selection set operator: Moving the cursor over the point where a value is
     * represented on a coordinate line will replace the selection with all rows
     * that have the value.
     */
    public static final int BRUSHOVER = 1;
    /**
     * Selection set operator: Add the new selection to the previous selection.
     */
    public static final int UNION = 2;
    /**
     * Selection set operator: Select rows that are in both the previous
     * selection and the new selection.
     */
    public static final int INTERSECTION = 3;
    /**
     * Selection set operator: Remove rows of the new selection from the
     * previous selection.
     */
    public static final int DIFFERENCE = 4;
    /**
     * Selection set operator: Select the rows that are in either the previous
     * selection or the new selection, but not in both selections.
     */
    public static final int XOR = 5;

    /**
     * Set the selection set operator to use.
     * 
     * @see #REPLACE
     * @see #BRUSHOVER
     * @see #UNION
     * @see #INTERSECTION
     * @see #DIFFERENCE
     * @see #XOR
     */
    public void setSetOperator(int setOperator);

    /**
     * Return the selection set operator being used.
     * 
     * @return the current selection set operator being used
     */
    public int getSetOperator();

    /**
     * Set the Set Operator that is mapped to the InputEvent modifiers.
     * 
     * @param modifiers
     *            the InputEvent modifiers return by InputEvent.getModifiers().
     * @see java.awt.event.InputEvent#getModifiers()
     */
    public void setFromInputEventMask(int modifiers);

    /**
     * Return the Set Operator that is mapped to the InputEvent modifiers.
     * 
     * @param modifiers
     *            the InputEvent modifiers return by InputEvent.getModifiers().
     * @return the Set Operator that is mapped to the InputEvent modifiers.
     * @see java.awt.event.InputEvent#getModifiers()
     */
    public int getFromInputEventMask(int modifiers);

    /**
     * Map InputEvent modifiers to a Set Operator.
     * 
     * @param setOperator
     *            the Set Operator to map.
     * @param modifiers
     *            the InputEvent modifiers to be mapped to the Set Operator.
     * @see java.awt.event.InputEvent#getModifiers()
     */
    public void setInputEventMasks(int setOperator, int modifiers[]);

    /**
     * Return the InputEvent modifiers mapped to a Set Operator.
     * 
     * @param setOperator
     *            the Set Operator.
     * @return the InputEvent modifiers mapped to the Set Operator.
     * @see java.awt.event.InputEvent#getModifiers()
     */
    public int[] getInputEventMasks(int setOperator);

    /**
     * Adds the listener to the list of objects notified of changes in the
     * model.
     * 
     * @param listener
     *            the ChangeListener to add
     */
    public void addChangeListener(ChangeListener listener);

    /**
     * Removes the listener from the notification list.
     * 
     * @param listener
     *            the ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener listener);
}
