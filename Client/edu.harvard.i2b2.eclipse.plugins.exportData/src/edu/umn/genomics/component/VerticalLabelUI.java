/*
 * @(#) $RCSfile: VerticalLabelUI.java,v $ $Revision: 1.3 $ $Date: 2008/10/30 20:56:40 $ $Name: TableView1_0b1
 $
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

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.geom.*;

public class VerticalLabelUI extends BasicLabelUI {
    static {
	labelUI = new VerticalLabelUI(false);
    }

    protected boolean clockwise;

    public VerticalLabelUI(boolean clockwise) {
	super();
	this.clockwise = clockwise;
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
	Dimension dim = super.getPreferredSize(c);
	return new Dimension(dim.height, dim.width);
    }

    private static Rectangle paintIconR = new Rectangle();
    private static Rectangle paintTextR = new Rectangle();
    private static Rectangle paintViewR = new Rectangle();
    private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

    @Override
    public void paint(Graphics g, JComponent c) {

	JLabel label = (JLabel) c;
	String text = label.getText();
	Icon icon = (label.isEnabled()) ? label.getIcon() : label
		.getDisabledIcon();

	if ((icon == null) && (text == null)) {
	    return;
	}

	FontMetrics fm = g.getFontMetrics();
	paintViewInsets = c.getInsets(paintViewInsets);

	paintViewR.x = paintViewInsets.left;
	paintViewR.y = paintViewInsets.top;

	// Use inverted height & width
	paintViewR.height = c.getWidth()
		- (paintViewInsets.left + paintViewInsets.right);
	paintViewR.width = c.getHeight()
		- (paintViewInsets.top + paintViewInsets.bottom);

	paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
	paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

	String clippedText = layoutCL(label, fm, text, icon, paintViewR,
		paintIconR, paintTextR);

	Graphics2D g2 = (Graphics2D) g;
	AffineTransform tr = g2.getTransform();
	if (clockwise) {
	    g2.rotate(Math.PI / 2);
	    g2.translate(0, -c.getWidth());
	} else {
	    g2.rotate(-Math.PI / 2);
	    g2.translate(-c.getHeight(), 0);
	}

	if (icon != null) {
	    icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
	}

	if (text != null) {
	    int textX = paintTextR.x;
	    int textY = paintTextR.y + fm.getAscent();

	    if (label.isEnabled()) {
		paintEnabledText(label, g, clippedText, textX, textY);
	    } else {
		paintDisabledText(label, g, clippedText, textX, textY);
	    }
	}

	g2.setTransform(tr);
    }
}
