/*
 * @(#) $RCSfile: SetIcon.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2003. The Regents of the University of Minnesota  
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

import java.awt.*;
import javax.swing.*;

/**
 * SetIcon provides set operation icons.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see SetOperator
 */
public class SetIcon implements Icon {
    int w = 19;
    int h = 19;
    Color c1 = Color.red.darker();
    Color c2 = Color.cyan;
    int op = SetOperator.UNION;

    SetIcon(int op) {
	this.op = op;
    }

    SetIcon(Color c1, Color c2, int op) {
	this.c1 = c1;
	this.c2 = c2;
	this.op = op;
    }

    public int getSetOperator() {
	return op;
    }

    public int getIconWidth() {
	return w;
    }

    public int getIconHeight() {
	return h;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
	int d = (int) (w * .6);
	int x1 = (int) (w * .333) - d / 2;
	int x2 = (int) (w * .667) - d / 2;
	int y1 = (h - d) / 2;

	switch (op) {
	case SetOperator.BRUSHOVER:
	    g.setColor(c1);
	    g.drawLine(x + w / 2 - d / 2, y + h / 2, x + w / 2 + d / 2, y + h
		    / 2);
	    g.drawLine(x + w / 2, y + y1, x + w / 2, y + y1 + d);
	    g.setColor(c2);
	    g.drawLine(x + w / 2 - d / 2, y + h / 2 + 1, x + w / 2 + d / 2, y
		    + h / 2 + 1);
	    g.drawLine(x + w / 2 + 1, y + y1, x + w / 2 + 1, y + y1 + d);
	    break;
	case SetOperator.REPLACE:
	    g.setColor(c1);
	    g.fillOval(x + x1, y + y1, d, d);

	    g.setColor(c2);
	    g.fillOval(x + x2, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x2, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x1, y + y1, d, d);

	    break;
	case SetOperator.UNION:
	    g.setColor(c2);
	    g.fillOval(x + x1, y + y1, d, d);

	    g.setColor(c2);
	    g.fillOval(x + x2, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x2, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x1, y + y1, d, d);

	    break;

	case SetOperator.INTERSECTION:
	    g.setColor(c1);
	    g.fillOval(x + x1, y + y1, d, d);

	    g.setColor(c1);
	    g.fillOval(x + x2, y + y1, d, d);

	    g.setColor(c2);
	    g.fillArc(x + x1, y + y1, d, d, 45, -90);

	    g.setColor(c2);
	    g.fillArc(x + x2, y + y1, d, d, 135, 90);

	    g.setColor(c1.darker());
	    g.drawOval(x + x1, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x2, y + y1, d, d);
	    break;

	case SetOperator.DIFFERENCE:
	    g.setColor(c2);
	    g.fillOval(x + x1, y + y1, d, d);

	    g.setColor(c1);
	    g.fillOval(x + x2, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x2, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x1, y + y1, d, d);

	    break;

	case SetOperator.XOR:
	    g.setColor(c2);
	    g.fillOval(x + x1, y + y1, d, d);

	    g.setColor(c2);
	    g.fillOval(x + x2, y + y1, d, d);

	    g.setColor(c1);
	    g.fillArc(x + x1, y + y1, d, d, 45, -90);

	    g.setColor(c1);
	    g.fillArc(x + x2, y + y1, d, d, 135, 90);

	    g.setColor(c1.darker());
	    g.drawOval(x + x1, y + y1, d, d);

	    g.setColor(c1.darker());
	    g.drawOval(x + x2, y + y1, d, d);

	    break;

	}
    }
}
