/*
 * @(#) $RCSfile: SetOperatorPanel.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * The SetOperatorPanel displays a set of RadioButtons to choose the Set
 * operator.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 */
public class SetOperatorPanel extends JPanel implements Serializable,
	ChangeListener {
    // WeakHashMap setOps = new WeakHashMap();
    HashMap setOps = new HashMap();
    JRadioButton sBrushOver = null;
    JRadioButton sReplace = null;
    JRadioButton sUnion = null;
    JRadioButton sIntersect = null;
    JRadioButton sDiff = null;
    JRadioButton sXOR = null;

    /**
     * Construct a panel of RadioButtons that will use setOperator as the model
     * for maintaining the Set operator choice.
     */
    public SetOperatorPanel() {
	setLayout(new GridLayout(1, 0, 0, 0));
	setBackground(Color.yellow);
	int curOp = SetOperator.REPLACE;
	ButtonGroup group = new ButtonGroup();
	sBrushOver = new JRadioButton(new SetIcon(Color.gray, Color.white,
		SetOperator.BRUSHOVER));
	sBrushOver.setMargin(new Insets(1, 1, 1, 1));
	sBrushOver.setSelectedIcon(new SetIcon(SetOperator.BRUSHOVER));
	sBrushOver.setSelected(curOp == SetOperator.BRUSHOVER);
	sBrushOver.setToolTipText("Select by brushover");
	sBrushOver.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setSetOperators(SetOperator.BRUSHOVER);
	    }
	});
	group.add(sBrushOver);
	add(sBrushOver);
	sReplace = new JRadioButton(new SetIcon(Color.gray, Color.white,
		SetOperator.REPLACE));
	sReplace.setMargin(new Insets(1, 1, 1, 1));
	sReplace.setSelectedIcon(new SetIcon(SetOperator.REPLACE));
	sReplace.setSelected(curOp == SetOperator.REPLACE);
	sReplace
		.setToolTipText("Replace previous selection with the new selection");
	sReplace.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setSetOperators(SetOperator.REPLACE);
	    }
	});
	group.add(sReplace);
	add(sReplace);
	sUnion = new JRadioButton(new SetIcon(Color.gray, Color.white,
		SetOperator.UNION));
	sUnion.setMargin(new Insets(1, 1, 1, 1));
	sUnion.setSelectedIcon(new SetIcon(SetOperator.UNION));
	sUnion.setSelected(curOp == SetOperator.UNION);
	// sUnion.setPressedIcon(new SetIcon(SetOperator.UNION));
	sUnion
		.setToolTipText("Union previous selection with the new selection");
	sUnion.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setSetOperators(SetOperator.UNION);
	    }
	});
	group.add(sUnion);
	add(sUnion);
	sIntersect = new JRadioButton(new SetIcon(Color.gray, Color.white,
		SetOperator.INTERSECTION));
	sIntersect.setMargin(new Insets(1, 1, 1, 1));
	sIntersect.setSelectedIcon(new SetIcon(SetOperator.INTERSECTION));
	sIntersect.setSelected(curOp == SetOperator.INTERSECTION);
	sIntersect
		.setToolTipText("Intersect previous selection with the new selection");
	sIntersect.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setSetOperators(SetOperator.INTERSECTION);
	    }
	});
	group.add(sIntersect);
	add(sIntersect);
	sDiff = new JRadioButton(new SetIcon(Color.gray, Color.white,
		SetOperator.DIFFERENCE));
	sDiff.setMargin(new Insets(1, 1, 1, 1));
	sDiff.setSelectedIcon(new SetIcon(SetOperator.DIFFERENCE));
	sDiff.setSelected(curOp == SetOperator.DIFFERENCE);
	sDiff
		.setToolTipText("Delete the new selection from the previous selection");
	sDiff.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setSetOperators(SetOperator.DIFFERENCE);
	    }
	});
	group.add(sDiff);
	add(sDiff);
	sXOR = new JRadioButton(new SetIcon(Color.gray, Color.white,
		SetOperator.XOR));
	sXOR.setMargin(new Insets(1, 1, 1, 1));
	sXOR.setSelectedIcon(new SetIcon(SetOperator.XOR));
	sXOR.setSelected(curOp == SetOperator.XOR);
	sXOR
		.setToolTipText("Exclusive OR of previous selection with new selection");
	sXOR.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		setSetOperators(SetOperator.XOR);
	    }
	});
	group.add(sXOR);
	add(sXOR);
    }

    /**
     * Construct a panel of RadioButtons that will use setOperator as the model
     * for maintaining the Set operator choice.
     * 
     * @param setOperator
     *            the object that will maintain the set operator choice.
     */
    public SetOperatorPanel(SetOperator setOperator) {
	this();
	addSetOperator(setOperator);
    }

    public void addSetOperator(SetOperator setOperator) {
	setOperator.addChangeListener(this);
	setOps.put(setOperator, null);
    }

    public void removeSetOperator(SetOperator setOperator) {
	setOperator.removeChangeListener(this);
	setOps.remove(setOperator);
    }

    public void setSetOperators(int setOperand) {
	for (Iterator iter = setOps.keySet().iterator(); iter.hasNext();) {
	    Object o = iter.next();
	    if (o != null && o instanceof SetOperator) {
		SetOperator setOp = (SetOperator) o;
		if (setOp.getSetOperator() != setOperand) {
		    setOp.setSetOperator(setOperand);
		}
	    }
	}
    }

    /**
     * The callback for the SetOperator when the operator has changed.
     * 
     * @param e
     *            the notification that the SetOperator has changed.
     */
    public void stateChanged(ChangeEvent e) {
	Object src = e.getSource();
	if (src instanceof SetOperator) {
	    switch (((SetOperator) src).getSetOperator()) {
	    case SetOperator.REPLACE:
		sReplace.doClick();
		break;
	    case SetOperator.BRUSHOVER:
		sBrushOver.doClick();
		break;
	    case SetOperator.UNION:
		sUnion.doClick();
		break;
	    case SetOperator.INTERSECTION:
		sIntersect.doClick();
		break;
	    case SetOperator.DIFFERENCE:
		sDiff.doClick();
		break;
	    case SetOperator.XOR:
		sXOR.doClick();
		break;
	    }
	}
    }
}
