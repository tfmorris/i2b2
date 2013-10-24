/*
 * @(#) $RCSfile: GraphDataModel.java,v $ $Revision: 1.4 $ $Date: 2008/10/27 20:18:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.lang.reflect.*;
import java.text.DecimalFormat;
import java.text.ParsePosition;

/**
 * GraphDataModel attempts to convert any array into an array of double values
 * that can be positioned by the axes of the graph. format "X" format "XY"
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/27 20:18:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class GraphDataModel extends AbstractDataModel {
    /**
     * The data array is a series of Y values, the x is implied to be the index.
     */
    public static final String FORMAT_Y = "Y";
    /**
     * The data array is a series of X Y values.
     */
    public static final String FORMAT_XY = "XY";
    double dataset[] = new double[0];
    String format;

    /**
     * Create a GraphDataModel without any data.
     */
    public GraphDataModel() {
    }

    /**
     * Create a GraphDataModel with the given xy data.
     * 
     * @param dataArray
     *            an array of data values.
     */
    public GraphDataModel(Object dataArray) {
	setData(dataArray);
    }

    /**
     * Create a GraphDataModel with the given xy data.
     * 
     * @param data
     *            an array of data values.
     */
    public GraphDataModel(double data[]) {
	setData(data);
    }

    /**
     * Create a GraphDataModel with the given data.
     * 
     * @param dataArray
     *            an array of data values.
     * @param format
     *            a String indicating the position of X and Y values in the
     *            array.
     * @see GraphDataModel#FORMAT_Y
     * @see GraphDataModel#FORMAT_XY
     */
    public GraphDataModel(Object dataArray, String format) {
	setData(dataArray);
	this.format = format;
    }

    /**
     * Create a GraphDataModel with the given data.
     * 
     * @param data
     *            an array of data values.
     * @param format
     *            a String indicating the position of X and Y values in the
     *            array.
     * @see GraphDataModel#FORMAT_Y
     * @see GraphDataModel#FORMAT_XY
     */
    public GraphDataModel(double data[], String format) {
	setData(data);
	this.format = format;
    }

    /**
     * Set the data values to be displayed by on the graph.
     * 
     * @param dataArray
     *            an array of data values
     * @param format
     *            a String indicating the position of X and Y values in the
     *            array.
     * @see GraphDataModel#FORMAT_Y
     * @see GraphDataModel#FORMAT_XY
     */
    public void setData(Object dataArray, String format) {
	double[] da = doubleArrayFromObject(dataArray);
	if (da != null)
	    dataset = da;
	this.format = format;
    }

    /**
     * Set the data values to be displayed by on the graph.
     * 
     * @param dataArray
     *            an array of x and y values
     * @see GraphDataModel#FORMAT_Y
     */
    public void setData(Object dataArray) throws IllegalArgumentException {
	double[] da = doubleArrayFromObject(dataArray);
	if (da != null)
	    dataset = da;
	this.format = FORMAT_Y;
    }

    /**
     * Transform an array of any primitive number type, any Number instances, or
     * String instances parseable as numbers into an array of double.
     * 
     * @param dataArray
     *            an array of values
     * @return a corresponding array of double values, or null if the array
     *         couldn't be interpreted as numbers.
     */
    public static double[] doubleArrayFromObject(Object dataArray) {
	double ds[] = null;
	try {
	    if (dataArray.getClass().isArray()) {
		int len = Array.getLength(dataArray);
		ds = new double[len];
		Class compClass = dataArray.getClass().getComponentType();
		if (dataArray.getClass().getComponentType().isPrimitive()) {
		    if (compClass.equals(Double.TYPE)) {
			double da[] = (double[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = da[i];
		    } else if (compClass.equals(Float.TYPE)) {
			float fa[] = (float[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = fa[i];
		    } else if (compClass.equals(Integer.TYPE)) {
			int ia[] = (int[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = ia[i];
		    } else if (compClass.equals(Long.TYPE)) {
			long la[] = (long[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = la[i];
		    } else if (compClass.equals(Short.TYPE)) {
			short sa[] = (short[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = sa[i];
		    } else if (compClass.equals(Byte.TYPE)) {
			byte ba[] = (byte[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = ba[i];
		    } else if (compClass.equals(Character.TYPE)) {
			char ca[] = (char[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = ca[i];
		    } else if (compClass.equals(Boolean.TYPE)) {
			boolean ta[] = (boolean[]) dataArray;
			for (int i = 0; i < len; i++)
			    ds[i] = ta[i] ? 1. : 0.;
		    } else {
			System.err.println("GraphDataModel.setData type?");
		    }
		} else if (java.lang.Number.class.isAssignableFrom(compClass)) {
		    Number na[] = (Number[]) dataArray;
		    for (int i = 0; i < len; i++)
			ds[i] = na[i].doubleValue();
		} else if (Class.forName("java.lang.String").isAssignableFrom(
			compClass)) {
		    String sa[] = (String[]) dataArray;
		    DecimalFormat df = new DecimalFormat();
		    ParsePosition pp = new ParsePosition(0);
		    for (int i = 0; i < len; i++) {
			// First try to parse this token as a Number
			pp.setIndex(0);
			// DecimalFormat needs uppercase E for exponent
			String ns = sa[i].replace('e', 'E');
			Number n = df.parse(ns, pp);
			// DecimalFormat doesn't accept E+2 it must be E2
			if (pp.getIndex() < ns.length()
				&& ns.regionMatches(pp.getIndex(), "E+", 0, 2)) {
			    ns = ns.substring(0, pp.getIndex() + 1)
				    + ns.substring(pp.getIndex() + 2);
			    pp.setIndex(0);
			    n = df.parse(ns, pp);
			}
			// Now check if entire token could be parsed as a Number
			if (pp.getIndex() < ns.length()) {
			    throw new IllegalArgumentException(
				    "GraphDataModel.setData(dataArray) "
					    + "unable to parse element " + i
					    + " :  " + sa[i] + "  as a number");
			}
			ds[i] = n.doubleValue();
		    }
		} else {
		    throw new IllegalArgumentException(
			    "GraphDataModel.setData(dataArray) "
				    + "dataArray must be an array of Number "
				    + "or a primitive number type.");
		}
	    } else if (dataArray instanceof Number) {
		ds = new double[1];
		ds[0] = ((Number) dataArray).doubleValue();
	    } else if (dataArray instanceof String) {
		String sa[] = new String[1];
		sa[0] = (String) dataArray;
		return doubleArrayFromObject(sa);
	    } else {
		throw new IllegalArgumentException(
			"GraphDataModel.setData(dataArray) "
				+ "dataArray must be an array of Number "
				+ "or a primitive number type.");
	    }
	} catch (Exception ex) {
	    System.err.println("GraphDataModel.setData " + ex);
	}
	return ds;
    }

    /**
     * Return any y values at the given x index.
     * 
     * @param xi
     *            the x index into the array.
     * @return the y values at the given x index.
     */
    @Override
    public double[] getYValues(int xi) {
	double yv[];
	if (FORMAT_Y.equalsIgnoreCase(format)) {
	    yv = new double[1];
	    yv[0] = dataset[xi];
	} else if (FORMAT_XY.equalsIgnoreCase(format)) {
	    yv = new double[1];
	    yv[0] = dataset[xi * 2 + 1];
	} else {
	    String fmt = format.toUpperCase();
	    int dims = 0;
	    int xdim = 0;
	    int ydim = 0;
	    for (int c = 0; c < fmt.length(); c++) {
		char ch = fmt.charAt(c);
		switch (ch) {
		case 'X':
		    dims++;
		    xdim++;
		    break;
		case 'Y':
		    ydim++;
		    dims++;
		    break;
		default:
		    break;
		}
	    }
	    yv = new double[ydim];
	    for (int c = 0, d = 0; c < fmt.length(); c++) {
		char ch = fmt.charAt(c);
		switch (ch) {
		case 'Y':
		    yv[d++] = dataset[xi * dims + c];
		    break;
		default:
		    break;
		}
	    }
	}
	return yv;
    }

    /**
     * @param x
     *            the x pixel offset
     * @param y
     *            the y pixel offset
     * @param axes
     *            the axes that transform the datapoints to the pixel area
     * @param points
     *            the array of points: xpoints, ypoints return the array of
     *            points: xpoints, ypoints
     */
    @Override
    public int[][] getPoints(int x, int y, Axis axes[], int points[][]) {
	int pnts[][] = points;
	int w = axes[0].getSize();
	int h = axes[1].getSize();
	int stride = format.length();
	int np = dataset.length / stride;
	if (pnts == null || pnts.length < 2) {
	    pnts = new int[2][];
	}
	if (pnts[0] == null || pnts[0].length != np) {
	    pnts[0] = new int[np];
	}
	if (pnts[1] == null || pnts[1].length != np) {
	    pnts[1] = new int[np];
	}
	int yb = y + h;
	if (FORMAT_Y.equalsIgnoreCase(format)) {
	    for (int i = 0; i < np; i++) {
		pnts[0][i] = x + axes[0].getIntPosition(i);
		pnts[1][i] = yb - axes[1].getIntPosition(dataset[i]);
	    }
	} else if (FORMAT_XY.equalsIgnoreCase(format)) {
	    for (int i = 0, di = 0; i < np; i++, di += stride) {
		pnts[0][i] = x + axes[0].getIntPosition(dataset[di]);
		pnts[1][i] = yb - axes[1].getIntPosition(dataset[di + 1]);
	    }
	} else {
	    String fmt = format.toUpperCase();
	    int dims = 0;

	    for (int c = 0; c < fmt.length(); c++) {
		char ch = fmt.charAt(c);
		switch (ch) {
		case 'X':
		case 'Y':
		    dims++;
		    break;
		default:
		    break;
		}
	    }

	    pnts = new int[dims][np];
	    for (int i = 0, di = 0; i < np; i++, di += stride) {
		for (int c = 0; c < fmt.length(); c++) {
		    int d = 0;
		    char ch = fmt.charAt(c);
		    switch (ch) {
		    case 'X':
			// System.err.println("X" + i + " " + d + " " + c + " "
			// + di + " : " + dataset[di+c] + " " +
			// axes[0].getIntPosition(dataset[di+c]) + "   " + ( x +
			// axes[0].getIntPosition(dataset[di+c])) + " @ " + x);
			pnts[d++][i] = x
				+ axes[0].getIntPosition(dataset[di + c]);
			break;
		    case 'Y':
			// System.err.println("Y" + i + " " + d + " " + c + " "
			// + di + " : " + dataset[di+c] + " " +
			// axes[1].getIntPosition(dataset[di+c]) + "   " + (yb -
			// axes[1].getIntPosition(dataset[di+c])) + " @ " + y +
			// " " + yb);
			pnts[d++][i] = yb
				- axes[1].getIntPosition(dataset[di + c]);
			break;
		    default:
			break;
		    }
		}
	    }
	}
	return pnts;
    }
}
