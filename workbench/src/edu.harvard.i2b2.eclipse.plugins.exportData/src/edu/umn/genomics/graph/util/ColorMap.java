/*
 * @(#) $RCSfile: ColorMap.java,v $ $Revision: 1.3 $ $Date: 2008/10/30 19:11:58 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.graph.util;

import java.awt.Color;

/**
 * Maps Color values to number values using HSB interpolation.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/30 19:11:58 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class ColorMap {
    double min = 0.;
    double max = 1.;
    Color table[] = { Color.blue, Color.cyan, Color.green, Color.yellow,
	    Color.red };

    public Color getColor(double v) {
	if (v <= min || max - min == 0)
	    return table[0];
	if (v >= max)
	    return table[table.length - 1];
	double p = (v - min) / (max - min) * (table.length - 1);
	double pc = Math.ceil(p);
	double pf = Math.floor(p);
	if (pc == pf)
	    return table[(int) pf];
	Color f = table[(int) pf];
	Color c = table[(int) pc];
	float p1 = (float) (pc - p);
	float p2 = (float) (p - pf);
	float hsb1[] = new float[3];
	float hsb2[] = new float[3];
	Color.RGBtoHSB(f.getRed(), f.getGreen(), f.getBlue(), hsb1);
	Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb2);
	// System.err.println("getColor  " + v + " " + pf + " " + p + " " + pc);
	// System.err.println("          " + hsb1[0] + " " + hsb1[1] + " " +
	// hsb1[2]);
	// System.err.println("          " + hsb2[0] + " " + hsb2[1] + " " +
	// hsb2[2]);
	hsb1[0] = hsb1[0] * p1 + hsb2[0] * p2;
	hsb1[1] = hsb1[1] * p1 + hsb2[1] * p2;
	hsb1[2] = hsb1[2] * p1 + hsb2[2] * p2;
	// System.err.println("          " + hsb1[0] + " " + hsb1[1] + " " +
	// hsb1[2]);

	return Color.getHSBColor(hsb1[0], hsb1[1], hsb1[2]);
    }

    public Color[] getColor(double[] values) {
	Color colors[] = new Color[values.length];
	for (int i = 0; i < values.length; i++)
	    colors[i] = getColor(values[i]);
	return colors;
    }

    public Color getHSVColor(double value) {
	return Color.getHSBColor((float) value, 1f, 1f);
    }

    public static void main(String[] args) {

    }
}
