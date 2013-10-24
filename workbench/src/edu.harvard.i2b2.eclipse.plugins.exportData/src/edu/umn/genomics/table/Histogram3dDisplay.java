/*
 * @(#) $RCSfile: Histogram3dDisplay.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.*;

public class Histogram3dDisplay extends JComponent {
    SetOperator setOperator = new DefaultSetOperator();
    int prevSetOp = -1;
    HistogramModel hgm;
    // Selection
    boolean selecting = false;
    Point start = null;
    Point current = null;
    // Transformation matrices
    public Matrix4d mvm = null; // model view matrix
    public Matrix4d pjm = null; // projection matrix
    public double rotation = .25; // -1 <-> 1
    public double tilt = .1; // 0 <-> 1
    public double eyeX = 0.;
    public double eyeY = 0.;
    public double eyeZ = 5.;
    public double ctrX = 0.;
    public double ctrY = 0.;
    public double ctrZ = 0.;
    // Bar width
    double barWidth = .5;
    // Whether to only display Selected Portions of bars or all
    public boolean selectBarsOnly = false;
    // An array of facets of bars
    public Polygon[] bars;
    // Position of facets of bars in the Polygon array
    static final int FRONT_FACET = 0;
    static final int RIGHT_FACET = 1;
    static final int LEFT_FACET = 2;
    static final int BACK_FACET = 3;
    static final int TOP_FACET = 4;
    static final int FRONT_SEL_FACET = 5;
    static final int RIGHT_SEL_FACET = 6;
    static final int LEFT_SEL_FACET = 7;
    static final int BACK_SEL_FACET = 8;
    static final int TOP_SEL_FACET = 9;
    // POLYS_PER_BAR
    static final int POLYS_PER_BAR = 10;

    public boolean updateAxesNeeded = true;
    public boolean updateBarsNeeded = true;

    public void setBarWidth(double width) {
	barWidth = width < 0. ? .1 : width > 1. ? 1. : width;
	updateBarsNeeded = true;
	repaint();
    }

    public double getBarWidth() {
	return barWidth;
    }

    public void setSelectBarsOnly(boolean showSelectedOnly) {
	selectBarsOnly = showSelectedOnly;
	updateBarsNeeded = true;
	repaint();
    }

    public boolean getSelectBarsOnly() {
	return selectBarsOnly;
    }

    public void setRotation(double rotation) {
	this.rotation = rotation;
	setView();
    }

    public double getRotation() {
	return rotation;
    }

    public void setTilt(double tilt) {
	this.tilt = tilt;
	setView();
    }

    public double getTilt() {
	return tilt;
    }

    HistogramListener hdl = new HistogramListener() {
	public void histogramChanged(HistogramEvent e) {
	    if (!e.isAdjusting()) {
		if (e.binModelsChanged()) { // Update axes and labels
		    updateAxesNeeded = true;
		}
		updateBarsNeeded = true;
		repaint();
	    }
	}
    };
    private MouseInputAdapter ma = new MouseInputAdapter() {
	@Override
	public void mousePressed(MouseEvent e) {
	    start = e.getPoint();
	    current = e.getPoint();
	    selecting = true;
	    prevSetOp = getSetOperator().getSetOperator();
	    getSetOperator().setFromInputEventMask(e.getModifiers());
	    repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	    current = e.getPoint();
	    repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    selecting = false;
	    current = e.getPoint();
	    select();
	    getSetOperator().setSetOperator(prevSetOp);
	    repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	    start = e.getPoint();
	    current = e.getPoint();
	    prevSetOp = getSetOperator().getSetOperator();
	    getSetOperator().setFromInputEventMask(e.getModifiers());
	    select(current);
	    getSetOperator().setSetOperator(prevSetOp);
	    repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
    };

    public void select() {
	int x = Math.min(start.x, current.x);
	int y = Math.min(start.y, current.y);
	int w = Math.abs(start.x - current.x);
	int h = Math.abs(start.y - current.y);
	Rectangle r = new Rectangle(x, y, w, h);
	select(r);
    }

    public void select(Rectangle r) {
	if (bars == null) {
	    return;
	}
	int selCnt = 0;
	int[] indices = new int[bars.length / POLYS_PER_BAR];
	if (bars != null) {
	    for (int i = 0, pos = 0; pos < bars.length; i++, pos += POLYS_PER_BAR) {
		for (int j = 0; j < POLYS_PER_BAR; j++) {
		    if (bars[pos + j] != null) {
			if (bars[pos + j].intersects(r)) {
			    indices[selCnt++] = i;
			    break;
			}
		    }
		}
	    }
	}
	if (selCnt < indices.length) {
	    int[] tmp = indices;
	    indices = new int[selCnt];
	    System.arraycopy(tmp, 0, indices, 0, selCnt);
	}
	select(indices);
    }

    public void select(Point p) {
	if (bars == null) {
	    return;
	}
	int selCnt = 0;
	int[] indices = new int[bars.length / POLYS_PER_BAR];
	if (bars != null) {
	    for (int i = 0, pos = 0; pos < bars.length; i++, pos += POLYS_PER_BAR) {
		for (int j = 0; j < POLYS_PER_BAR; j++) {
		    if (bars[pos + j] != null) {
			if (bars[pos + j].contains(p)) {
			    indices[selCnt++] = i;
			    break;
			}
		    }
		}
	    }
	}
	if (selCnt < indices.length) {
	    int[] tmp = indices;
	    indices = new int[selCnt];
	    System.arraycopy(tmp, 0, indices, 0, selCnt);
	}
	select(indices);
    }

    public void select(int[] indices) {
	if (indices != null) {
	    hgm.selectBins(indices, hgm.getListSelectionModel(),
		    getSetOperator().getSetOperator());
	} else {
	}
    }

    public void setSetOperator(SetOperator setOperator) {
	this.setOperator = setOperator;
    }

    public SetOperator getSetOperator() {
	return setOperator;
    }

    public Histogram3dDisplay(HistogramModel model, SetOperator setOperator) {
	this(model);
	setSetOperator(setOperator);
    }

    public Histogram3dDisplay(HistogramModel model) {
	setOpaque(true);
	mvm = new Matrix4d(); // model view matrix
	pjm = new Matrix4d(); // projection matrix
	setFrustum(pjm, -1, 1, -1, 1, 4, 10000);
	mvm.setIdentity();
	setView();
	// lookAt(mvm, .5, 1, -5, 0, 0, 0, 0, 1, 0);
	this.hgm = model;
	hgm.addHistogramListener(hdl);
	addMouseListener(ma);
	addMouseMotionListener(ma);
    }

    public int getDecimalPlaces(double num) {
	return num == 0. ? 1 : 1 + (int) Math.floor(Math.log(Math.abs(num))
		/ Math.log(10.));
    }

    public int getPowOfTen(int max) {
	if (max == 0) {
	    return 1;
	}
	int pow = (int) Math.floor(Math.log(Math.abs(max)) / Math.log(10.));
	int n = (int) Math.pow(10., pow);
	return n;
    }

    public int getDisplayMax(int max) {
	if (max == 0) {
	    return 1;
	}
	int pow = (int) Math.floor(Math.log(Math.abs(max)) / Math.log(10.));
	int n = (int) Math.pow(10., pow);
	return (max / n + 1) * n;
    }

    /*
     * // Determine the increments for counts // log10 [ 1/Math.log(10.) ] to
     * get number of places. d = d < 1 ? 1 : d; double scale =
     * Math.pow(10.,d-1); int vmax = (int)Math.ceil((max+1) / scale);
     * vaxis[ci].getAxis().setMax(vmaxscale); if (vmax > 5) { double[] ticks =
     * new double[vmax/2+1]; for (int i = 0; i < ticks.length; i++) { ticks[i] =
     * (double)i 2 scale; } vaxis[ci].getAxis().setTicks(ticks); } else {
     * double[] ticks = new double[vmax+1]; for (int i = 0; i < ticks.length;
     * i++) { ticks[i] = (double)i scale; } vaxis[ci].getAxis().setTicks(ticks);
     * }
     */

    public void setView() {
	setView(rotation, tilt);
    }

    public Vector3d trans = new Vector3d(0., 0., 0.);

    public void setView(double rot, double tilt) {
	AxisAngle4d axisAngle;
	Matrix4d mat;
	mvm.setIdentity();
	// mvm.setTranslation(tran);

	// view platform
	lookAt(mvm, eyeX, eyeY, eyeZ, ctrX, ctrX, ctrY, 0, 1, 0);

	// set tilt;
	axisAngle = new AxisAngle4d(1., 0., 0., tilt * Math.PI);
	mat = new Matrix4d();
	mat.setIdentity();
	mat.setRotation(axisAngle);
	mvm.mul(mat);

	// set rotation;
	axisAngle = new AxisAngle4d(0., 1., 0., rot * Math.PI);
	mat = new Matrix4d();
	mat.setIdentity();
	mat.setRotation(axisAngle);
	mvm.mul(mat);

	// translation
	mat = new Matrix4d();
	mat.setIdentity();
	mat.setTranslation(trans);
	mvm.mul(mat);

	updateAxesNeeded = true;
	updateBarsNeeded = true;
	repaint();
    }

    public void lookAt(Matrix4d mat, double eyeX, double eyeY, double eyeZ,
	    double centerX, double centerY, double centerZ, double upX,
	    double upY, double upZ) {
	Vector3d f = new Vector3d(centerX - eyeX, centerY - eyeY, centerZ
		- eyeZ);
	Vector3d up = new Vector3d(upX, upY, upZ);
	f.normalize();
	up.normalize();
	Vector3d s = new Vector3d();
	Vector3d u = new Vector3d();
	s.cross(f, up);
	u.cross(s, f);
	Matrix4d m = new Matrix4d(s.x, s.y, s.z, 0, u.x, u.y, u.z, 0, -f.x,
		-f.y, -f.z, 0, 0, 0, 0, 1);
	mat.mul(m);
	// m.mul(mat);
	// mat.set(m);
	m.setIdentity();
	// m.setTranslation(new Vector3d(-eyeX, -eyeY, -eyeZ));
	// mat.mul(m);
	mat.setTranslation(new Vector3d(-eyeX, -eyeY, -eyeZ));
	// m.mul(mat);
	// mat.set(m);
    }

    public void setFrustum(Matrix4d mat, double l, double r, double b,
	    double t, double n, double f) {
	mat.m00 = 2 * n / (r - l);
	mat.m01 = 0;
	mat.m02 = (r + l) / (r - l);
	mat.m03 = 0;

	mat.m10 = 0;
	mat.m11 = 2 * n / (t - b);
	mat.m12 = (t + b) / (t - b);
	mat.m13 = 0;

	mat.m20 = 0;
	mat.m21 = 0;
	mat.m22 = -1 * (f + n) / (f - n);
	mat.m23 = -2 * f * n / (f - n);

	mat.m30 = 0;
	mat.m31 = 0;
	mat.m32 = -1;
	mat.m33 = 0;
    }

    public void setOrtho(Matrix4d mat, double l, double r, double b, double t,
	    double n, double f) {
	mat.m00 = 2 / (r - l);
	mat.m01 = 0;
	mat.m02 = 0;
	mat.m03 = -1 * (r + l) / (r - l);

	mat.m10 = 0;
	mat.m11 = 2 / (t - b);
	mat.m12 = 0;
	mat.m13 = -1 * (t + b) / (t - b);

	mat.m20 = 0;
	mat.m21 = 0;
	mat.m22 = -2 / (f - n);
	mat.m23 = -1 * (f + n) / (f - n);

	mat.m30 = 0;
	mat.m31 = 0;
	mat.m32 = 0;
	mat.m33 = 1;
    }

    public Point[] worldToScreen(Point4d[] coords, Matrix4d mvm, Matrix4d pjm,
	    Dimension viewPort) {
	Point[] pnts = new Point[coords.length];
	Point4d pp = new Point4d();
	for (int i = 0; i < coords.length; i++) {
	    // ModelView Matrix Transformtion
	    mvm.transform(coords[i]);
	    // Projection Matrix Transformtion
	    pjm.transform(coords[i]);
	    // Perspective Division
	    pp.project(coords[i]);
	    // ViewPort Transformation
	    double scale = Math.min(viewPort.width, viewPort.height) * .5;
	    pnts[i] = new Point((int) (viewPort.width * .5 + (pp.x * scale)),
		    (int) (viewPort.height * .4 - (pp.y * scale)));
	    coords[i].set(pp);
	    // pnts[i] = new Point((int)((pp.x)*(viewPort.width/2.)),
	    // viewPort.height - (int)((pp.y) * (viewPort.height/2.)));
	}
	return pnts;
    }

    static int[][] facets = {

	    { TOP_FACET, FRONT_FACET, LEFT_FACET },
	    { TOP_FACET, FRONT_FACET, RIGHT_FACET },
	    { TOP_FACET, RIGHT_FACET, BACK_FACET },
	    { TOP_FACET, BACK_FACET, LEFT_FACET },

	    { FRONT_FACET, LEFT_FACET },
	    { FRONT_FACET, RIGHT_FACET },
	    { RIGHT_FACET, BACK_FACET },
	    { BACK_FACET, LEFT_FACET },

	    { TOP_SEL_FACET, FRONT_SEL_FACET, LEFT_SEL_FACET },
	    { TOP_SEL_FACET, FRONT_SEL_FACET, RIGHT_SEL_FACET },
	    { TOP_SEL_FACET, RIGHT_SEL_FACET, BACK_SEL_FACET },
	    { TOP_SEL_FACET, BACK_SEL_FACET, LEFT_SEL_FACET },

	    { FRONT_FACET, LEFT_FACET, FRONT_SEL_FACET, LEFT_SEL_FACET },
	    { FRONT_FACET, RIGHT_FACET, FRONT_SEL_FACET, RIGHT_SEL_FACET },
	    { RIGHT_FACET, BACK_FACET, RIGHT_SEL_FACET, BACK_SEL_FACET },
	    { BACK_FACET, LEFT_FACET, BACK_SEL_FACET, LEFT_SEL_FACET },

	    { TOP_FACET, FRONT_FACET, LEFT_FACET, FRONT_SEL_FACET,
		    LEFT_SEL_FACET },
	    { TOP_FACET, FRONT_FACET, RIGHT_FACET, FRONT_SEL_FACET,
		    RIGHT_SEL_FACET },
	    { TOP_FACET, RIGHT_FACET, BACK_FACET, RIGHT_SEL_FACET,
		    BACK_SEL_FACET },
	    { TOP_FACET, BACK_FACET, LEFT_FACET, BACK_SEL_FACET, LEFT_SEL_FACET },

	    { TOP_FACET }, {}, };

    public int closestPointZ(Point4d[] coords) {
	if (coords == null || coords.length < 1) {
	    return -1;
	}
	int n = 0;
	for (int i = 1; i < coords.length; i++) {
	    if (coords[i].z < coords[n].z) {
		n = i;
	    }
	}
	return n;
    }

    public int[] visibleFacets(Point4d[] coords, int binCnt, int selCnt,
	    boolean selectOnly) {
	/*
	 * // selectBarsOnly selCnt == 0 // 4 points else // 8 points else
	 * binCnt == 0 // 4 points selCnt == binCnt // 8 points selCnt > 0) { //
	 * 12 points selCnt == 0 // 8 points
	 */
	if (coords == null) {
	    return facets[21];
	} else if (coords.length <= 4) {
	    return facets[20];
	} else {
	    // Find the minumum z value;
	    switch (closestPointZ(coords)) {
	    case 0:
		return coords.length == 12 ? facets[16] : selectBarsOnly
			|| selCnt == binCnt ? facets[8] : facets[0];
	    case 1:
		return coords.length == 12 ? facets[17] : selectBarsOnly
			|| selCnt == binCnt ? facets[9] : facets[1];
	    case 2:
		return coords.length == 12 ? facets[18] : selectBarsOnly
			|| selCnt == binCnt ? facets[10] : facets[2];
	    case 3:
		return coords.length == 12 ? facets[19] : selectBarsOnly
			|| selCnt == binCnt ? facets[11] : facets[3];
	    case 4:
		return coords.length == 12 ? facets[16] : selectBarsOnly
			|| selCnt == binCnt ? facets[8] : facets[0];
	    case 5:
		return coords.length == 12 ? facets[17] : selectBarsOnly
			|| selCnt == binCnt ? facets[9] : facets[1];
	    case 6:
		return coords.length == 12 ? facets[18] : selectBarsOnly
			|| selCnt == binCnt ? facets[10] : facets[2];
	    case 7:
		return coords.length == 12 ? facets[19] : selectBarsOnly
			|| selCnt == binCnt ? facets[11] : facets[3];
	    }
	}
	return facets[0];
    }

    public Polygon getPolygon(Point[] pnts, int facet) {
	Polygon poly = new Polygon();
	switch (facet) {
	case TOP_FACET:
	    if (pnts.length > 4) {
		poly.addPoint(pnts[4].x, pnts[4].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
	    } else {
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[3].x, pnts[3].y);
	    }
	    break;
	case FRONT_FACET:
	    // left
	    if (pnts.length > 8) {
		poly.addPoint(pnts[8].x, pnts[8].y);
		poly.addPoint(pnts[9].x, pnts[9].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
		poly.addPoint(pnts[4].x, pnts[4].y);
	    } else {
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
		poly.addPoint(pnts[4].x, pnts[4].y);
	    }
	    break;
	case RIGHT_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[9].x, pnts[9].y);
		poly.addPoint(pnts[10].x, pnts[10].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
	    } else {
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
	    }
	    break;
	case LEFT_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[11].x, pnts[11].y);
		poly.addPoint(pnts[8].x, pnts[8].y);
		poly.addPoint(pnts[4].x, pnts[4].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
	    } else {
		poly.addPoint(pnts[3].x, pnts[3].y);
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[4].x, pnts[4].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
	    }
	    break;
	case BACK_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[10].x, pnts[10].y);
		poly.addPoint(pnts[11].x, pnts[11].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
	    } else {
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[3].x, pnts[3].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
	    }
	    break;
	case TOP_SEL_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[8].x, pnts[8].y);
		poly.addPoint(pnts[9].x, pnts[9].y);
		poly.addPoint(pnts[10].x, pnts[10].y);
		poly.addPoint(pnts[11].x, pnts[11].y);
	    } else if (pnts.length > 4) {
		poly.addPoint(pnts[4].x, pnts[4].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
	    } else {
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[3].x, pnts[3].y);
	    }
	    break;
	case FRONT_SEL_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[9].x, pnts[9].y);
		poly.addPoint(pnts[8].x, pnts[8].y);
	    } else {
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
		poly.addPoint(pnts[4].x, pnts[4].y);
	    }
	    break;
	case RIGHT_SEL_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[10].x, pnts[10].y);
		poly.addPoint(pnts[9].x, pnts[9].y);
	    } else {
		poly.addPoint(pnts[1].x, pnts[1].y);
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
		poly.addPoint(pnts[5].x, pnts[5].y);
	    }
	    break;
	case LEFT_SEL_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[3].x, pnts[3].y);
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[8].x, pnts[8].y);
		poly.addPoint(pnts[11].x, pnts[11].y);
	    } else {
		poly.addPoint(pnts[3].x, pnts[3].y);
		poly.addPoint(pnts[0].x, pnts[0].y);
		poly.addPoint(pnts[4].x, pnts[4].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
	    }
	    break;
	case BACK_SEL_FACET:
	    if (pnts.length > 8) {
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[3].x, pnts[3].y);
		poly.addPoint(pnts[11].x, pnts[11].y);
		poly.addPoint(pnts[10].x, pnts[10].y);
	    } else {
		poly.addPoint(pnts[2].x, pnts[2].y);
		poly.addPoint(pnts[3].x, pnts[3].y);
		poly.addPoint(pnts[7].x, pnts[7].y);
		poly.addPoint(pnts[6].x, pnts[6].y);
	    }
	    break;
	}
	return poly;
    }

    public Dimension getViewPort() {
	Insets insets = getInsets();
	int vw = (getWidth() - insets.left - insets.right);
	int vh = (getHeight() - insets.top - insets.bottom);
	return new Dimension(vw, vh);
    }

    public Point4d[] getPoint4dArray(int len) {
	Point4d[] coord = new Point4d[len];
	for (int i = 0; i < len; i++) {
	    coord[i] = new Point4d();
	}
	return coord;
    }

    //	
    // 7 6
    // +---+
    // / /|
    // 4 +---+5| <- nonselected portion
    // | | |
    // |11 | + 10
    // | |/|
    // 8 +---+9| <- selected portion
    // | 3 | + 2
    // | |/
    // +---+
    // 0 1
    //	
    // / \
    // / \
    // |\ /|
    // | \ / | <- nonselected portion
    // | | |
    // |\ | /|
    // | \|/ | <- selected portion
    // \ | /
    // \|/
    //	
    // +---+ TOP
    // /| /| FRONT
    // +---+ | LEFT
    // | | | | RIGHT
    // | +-|-+ BACK
    // |/ |/ BOTTOM
    // +---+
    //	

    Dimension vp = null;
    int[] modelDim = null;
    int binCount = 0;
    int binMax = 0;
    int displayMax = 0;

    double xscale = 1.;
    double xoff = 1.;
    double zscale = 1.;
    double zoff = 1.;
    double hscale = 1.;

    double xbase = -.5;
    double zbase = .5;
    double hbase = -1;

    /**
     * 
     * Six Polygons per bar in this order: select left face select right face
     * select top face unselect left face unselect right face unselect top face
     */
    public Polygon[] makeBars() {
	if (hgm == null) {
	    return null;
	}
	int nMod = hgm.getModelCount();
	modelDim = hgm.getDimensions();
	binCount = hgm.getBinCount();
	binMax = hgm.getMaxBinSize();
	displayMax = getDisplayMax(binMax);
	if (nMod != 2 || modelDim == null || modelDim.length != 2) {
	    return null;
	}
	int[] indices = new int[modelDim.length];
	vp = getViewPort();

	Polygon[] polygon = new Polygon[binCount * POLYS_PER_BAR];
	Polygon poly = null;

	Point4d[] pnts4 = null;
	Point4d[] pnts8 = null;
	Point4d[] pnts12 = null;
	xscale = 1. / modelDim[0];
	xoff = xscale * (barWidth * .5);
	zscale = 1. / modelDim[1];
	zoff = zscale * (barWidth * .5);
	hscale = displayMax > 0 ? 1. / displayMax : 1;
	for (int i = 0, pos = 0; i < binCount; i++, pos += POLYS_PER_BAR) {
	    int binCnt = hgm.getBinCount(indices);
	    int selCnt = hgm.getBinSelectCount(indices);
	    if (selectBarsOnly) {
		if (selCnt == 0) { // 4 points

		    if (pnts4 == null) {
			pnts4 = getPoint4dArray(4);
		    }
		    // top
		    pnts4[0].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts4[0].y = hbase + selCnt * hscale;
		    pnts4[0].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts4[0].w = 1;
		    pnts4[1].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts4[1].y = hbase + selCnt * hscale;
		    pnts4[1].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts4[1].w = 1;
		    pnts4[2].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts4[2].y = hbase + selCnt * hscale;
		    pnts4[2].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts4[2].w = 1;
		    pnts4[3].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts4[3].y = hbase + selCnt * hscale;
		    pnts4[3].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts4[3].w = 1;
		    Point[] pnts = worldToScreen(pnts4, mvm, pjm, vp);
		    int[] facet = visibleFacets(pnts4, binCnt, selCnt,
			    selectBarsOnly);
		    for (int j = 0; j < facet.length; j++) {
			poly = getPolygon(pnts, facet[j]);
			polygon[pos + facet[j]] = poly;
		    }
		} else { // 8 points

		    if (pnts8 == null) {
			pnts8 = getPoint4dArray(8);
		    }
		    // base
		    pnts8[0].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[0].y = hbase + 0;
		    pnts8[0].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[0].w = 1;
		    pnts8[1].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[1].y = hbase + 0;
		    pnts8[1].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[1].w = 1;
		    pnts8[2].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[2].y = hbase + 0;
		    pnts8[2].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[2].w = 1;
		    pnts8[3].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[3].y = hbase + 0;
		    pnts8[3].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[3].w = 1;
		    // top
		    pnts8[4].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[4].y = hbase + selCnt * hscale;
		    pnts8[4].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[4].w = 1;
		    pnts8[5].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[5].y = hbase + selCnt * hscale;
		    pnts8[5].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[5].w = 1;
		    pnts8[6].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[6].y = hbase + selCnt * hscale;
		    pnts8[6].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[6].w = 1;
		    pnts8[7].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[7].y = hbase + selCnt * hscale;
		    pnts8[7].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[7].w = 1;
		    Point[] pnts = worldToScreen(pnts8, mvm, pjm, vp);
		    int[] facet = visibleFacets(pnts8, binCnt, selCnt,
			    selectBarsOnly);
		    for (int j = 0; j < facet.length; j++) {
			poly = getPolygon(pnts, facet[j]);
			polygon[pos + facet[j]] = poly;
		    }
		}
	    } else {
		if (binCnt == 0) { // 4 points
		    if (pnts4 == null) {
			pnts4 = getPoint4dArray(4);
		    }
		    // top
		    pnts4[0].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts4[0].y = hbase + 0;
		    pnts4[0].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts4[0].w = 1;
		    pnts4[1].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts4[1].y = hbase + 0;
		    pnts4[1].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts4[1].w = 1;
		    pnts4[2].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts4[2].y = hbase + 0;
		    pnts4[2].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts4[2].w = 1;
		    pnts4[3].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts4[3].y = hbase + 0;
		    pnts4[3].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts4[3].w = 1;
		    Point[] pnts = worldToScreen(pnts4, mvm, pjm, vp);
		    int[] facet = visibleFacets(pnts4, binCnt, selCnt,
			    selectBarsOnly);
		    for (int j = 0; j < facet.length; j++) {
			poly = getPolygon(pnts, facet[j]);
			polygon[pos + facet[j]] = poly;
		    }
		} else if (selCnt == binCnt) { // 8 points
		    if (pnts8 == null) {
			pnts8 = getPoint4dArray(8);
		    }
		    // base
		    pnts8[0].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[0].y = hbase + 0;
		    pnts8[0].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[0].w = 1;
		    pnts8[1].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[1].y = hbase + 0;
		    pnts8[1].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[1].w = 1;
		    pnts8[2].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[2].y = hbase + 0;
		    pnts8[2].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[2].w = 1;
		    pnts8[3].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[3].y = hbase + 0;
		    pnts8[3].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[3].w = 1;
		    // top
		    pnts8[4].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[4].y = hbase + selCnt * hscale;
		    pnts8[4].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[4].w = 1;
		    pnts8[5].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[5].y = hbase + selCnt * hscale;
		    pnts8[5].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[5].w = 1;
		    pnts8[6].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[6].y = hbase + selCnt * hscale;
		    pnts8[6].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[6].w = 1;
		    pnts8[7].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[7].y = hbase + selCnt * hscale;
		    pnts8[7].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[7].w = 1;
		    Point[] pnts = worldToScreen(pnts8, mvm, pjm, vp);
		    int[] facet = visibleFacets(pnts8, binCnt, selCnt,
			    selectBarsOnly);
		    for (int j = 0; j < facet.length; j++) {
			poly = getPolygon(pnts, facet[j]);
			polygon[pos + facet[j]] = poly;
		    }
		} else if (selCnt > 0) { // 12 points
		    if (pnts12 == null) {
			pnts12 = getPoint4dArray(12);
		    }
		    // base
		    pnts12[0].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts12[0].y = hbase + 0;
		    pnts12[0].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts12[0].w = 1;
		    pnts12[1].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts12[1].y = hbase + 0;
		    pnts12[1].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts12[1].w = 1;
		    pnts12[2].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts12[2].y = hbase + 0;
		    pnts12[2].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts12[2].w = 1;
		    pnts12[3].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts12[3].y = hbase + 0;
		    pnts12[3].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts12[3].w = 1;

		    // top
		    pnts12[4].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts12[4].y = hbase + binCnt * hscale;
		    pnts12[4].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts12[4].w = 1;
		    pnts12[5].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts12[5].y = hbase + binCnt * hscale;
		    pnts12[5].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts12[5].w = 1;
		    pnts12[6].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts12[6].y = hbase + binCnt * hscale;
		    pnts12[6].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts12[6].w = 1;
		    pnts12[7].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts12[7].y = hbase + binCnt * hscale;
		    pnts12[7].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts12[7].w = 1;

		    // select top
		    pnts12[8].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts12[8].y = hbase + selCnt * hscale;
		    pnts12[8].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts12[8].w = 1;
		    pnts12[9].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts12[9].y = hbase + selCnt * hscale;
		    pnts12[9].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts12[9].w = 1;
		    pnts12[10].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts12[10].y = hbase + selCnt * hscale;
		    pnts12[10].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts12[10].w = 1;
		    pnts12[11].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts12[11].y = hbase + selCnt * hscale;
		    pnts12[11].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts12[11].w = 1;

		    Point[] pnts = worldToScreen(pnts12, mvm, pjm, vp);
		    int[] facet = visibleFacets(pnts12, binCnt, selCnt,
			    selectBarsOnly);
		    for (int j = 0; j < facet.length; j++) {
			poly = getPolygon(pnts, facet[j]);
			polygon[pos + facet[j]] = poly;
		    }
		} else { // selCnt == 0 8 points
		    if (pnts8 == null) {
			pnts8 = getPoint4dArray(8);
		    }
		    // base
		    pnts8[0].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[0].y = hbase + 0;
		    pnts8[0].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[0].w = 1;
		    pnts8[1].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[1].y = hbase + 0;
		    pnts8[1].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[1].w = 1;
		    pnts8[2].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[2].y = hbase + 0;
		    pnts8[2].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[2].w = 1;
		    pnts8[3].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[3].y = hbase + 0;
		    pnts8[3].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[3].w = 1;
		    // top
		    pnts8[4].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[4].y = hbase + binCnt * hscale;
		    pnts8[4].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[4].w = 1;
		    pnts8[5].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[5].y = hbase + binCnt * hscale;
		    pnts8[5].z = zbase - (.5 + indices[1]) * zscale - zoff;
		    pnts8[5].w = 1;
		    pnts8[6].x = xbase + (.5 + indices[0]) * xscale - xoff;
		    pnts8[6].y = hbase + binCnt * hscale;
		    pnts8[6].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[6].w = 1;
		    pnts8[7].x = xbase + (.5 + indices[0]) * xscale + xoff;
		    pnts8[7].y = hbase + binCnt * hscale;
		    pnts8[7].z = zbase - (.5 + indices[1]) * zscale + zoff;
		    pnts8[7].w = 1;
		    Point[] pnts = worldToScreen(pnts8, mvm, pjm, vp);
		    int[] facet = visibleFacets(pnts8, binCnt, selCnt,
			    selectBarsOnly);
		    for (int j = 0; j < facet.length; j++) {
			poly = getPolygon(pnts, facet[j]);
			polygon[pos + facet[j]] = poly;
		    }
		}
	    }
	    MultiDimIntArray.incrIndex(indices, modelDim);
	}
	return polygon;
    }

    public void setPoint4dArray(Point4d[] pnts, double x, double z,
	    double xoff, double zoff, double h1, double h2) {
	for (int i = 0; i < pnts.length; i++) {
	    /*
	     * cube[i].x = xbase + (i + 1) % 4 / 2; cube[i].y = hbase + i / 4;
	     * cube[i].z = zbase - i % 4 / 2; cube[i].w = 1.;
	     */
	}
    }

    public boolean isFrontFacing(Polygon p) {
	double a = 0;
	for (int i = 0, n = p.npoints; i < n; i++) {
	    a += p.xpoints[i] * p.ypoints[(i + 1) % n] - p.xpoints[(i + 1) % n]
		    * p.ypoints[i];
	}
	return a >= 0;
    }

    public Color getBarFacetColor(int facet) {
	switch (facet % POLYS_PER_BAR) {
	case TOP_FACET:
	    return Color.BLUE;
	case FRONT_FACET:
	case BACK_FACET:
	    return Color.BLUE.darker();
	case RIGHT_FACET:
	case LEFT_FACET:
	    return Color.BLUE.darker().darker();
	case TOP_SEL_FACET:
	    return Color.CYAN;
	case FRONT_SEL_FACET:
	case BACK_SEL_FACET:
	    return Color.CYAN.darker();
	case RIGHT_SEL_FACET:
	case LEFT_SEL_FACET:
	    return Color.CYAN.darker().darker();
	default:
	}
	return Color.BLACK;
    }

    Polygon[] box;
    Line2D[] hLines;
    Line2D[] hTicks;
    Line2D[] xTicks;
    Line2D[] yTicks;

    // Shape[] display;

    // 
    // For simplicity,
    // cube -1,1
    @Override
    public void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	Dimension vp = getViewPort();

	if (updateAxesNeeded) {
	}
	if (updateBarsNeeded) {
	    bars = makeBars();
	}
	// draw background
	if (isOpaque()) {
	    g2.setColor(getBackground());
	    g2.fillRect(0, 0, getWidth(), getHeight());
	}
	// draw background cube faces
	Point4d[] cube = getPoint4dArray(8);
	for (int i = 0; i < 8; i++) {
	    cube[i].x = xbase + (i + 1) % 4 / 2;
	    cube[i].y = hbase + i / 4;
	    cube[i].z = zbase - i % 4 / 2;
	    cube[i].w = 1.;
	}
	Point[] pnts = worldToScreen(cube, mvm, pjm, vp);
	Polygon poly;
	int closestCorner = closestPointZ(cube);
	switch (closestCorner) {
	case 0: // back right top
	case 1: // back left top
	case 2: // front left top
	case 3: // front right top
	case 4: // back right bottom
	case 5: // back left bottom
	case 6: // front left bottom
	case 7: // front right bottom
	}
	// System.err.println("closestCorner " + closestCorner);

	for (int i = 0; i < 8; i++) {
	    cube[i].x = xbase + (i + 1) % 4 / 2;
	    cube[i].y = hbase + i / 4;
	    cube[i].z = zbase - i % 4 / 2;
	    cube[i].w = 1.;
	}

	FontMetrics fm = g2.getFontMetrics();
	int hIncr = getPowOfTen(binMax);
	if (closestCorner % 4 < 2) {
	    // draw back
	    poly = new Polygon();
	    poly.addPoint(pnts[3].x, pnts[3].y);
	    poly.addPoint(pnts[2].x, pnts[2].y);
	    poly.addPoint(pnts[6].x, pnts[6].y);
	    poly.addPoint(pnts[7].x, pnts[7].y);
	    g2.setColor(new Color(240, 240, 240));
	    g2.fill(poly);
	    g2.setColor(Color.BLACK);
	    g2.draw(poly);
	    // draw height grid lines
	    Point4d[] gPnts4d = getPoint4dArray(2);
	    for (int i = 0; i <= displayMax; i += hIncr) {
		gPnts4d[0].x = xbase + 0.;
		gPnts4d[0].y = hbase + hscale * i;
		gPnts4d[0].z = zbase - 1.;
		gPnts4d[0].w = 1.;
		gPnts4d[1].x = xbase + 1.;
		gPnts4d[1].y = hbase + hscale * i;
		gPnts4d[1].z = zbase - 1;
		gPnts4d[1].w = 1.;
		Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
		g2.drawLine(gPnts[0].x, gPnts[0].y, gPnts[1].x, gPnts[1].y);
		String s = "" + i;
		if (closestCorner % 4 == 0) {
		    int lw = fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[0].x - lw - 10, gPnts[0].y
			    + fm.getAscent() * i / displayMax);
		} else {
		    int lw = fm.stringWidth("" + displayMax)
			    - fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[1].x + lw + 10, gPnts[1].y
			    + fm.getAscent() * i / displayMax);
		}
	    }
	}
	if (closestCorner % 4 == 1 || closestCorner % 4 == 2) {
	    // draw left
	    poly = new Polygon();
	    poly.addPoint(pnts[0].x, pnts[0].y);
	    poly.addPoint(pnts[3].x, pnts[3].y);
	    poly.addPoint(pnts[7].x, pnts[7].y);
	    poly.addPoint(pnts[4].x, pnts[4].y);
	    g2.setColor(new Color(220, 220, 220));
	    g2.fill(poly);
	    g2.setColor(Color.BLACK);
	    g2.draw(poly);
	    // draw height grid lines
	    Point4d[] gPnts4d = getPoint4dArray(2);
	    for (int i = 0; i <= displayMax; i += hIncr) {
		gPnts4d[0].x = xbase + 0.;
		gPnts4d[0].y = hbase + hscale * i;
		gPnts4d[0].z = zbase - 1.;
		gPnts4d[0].w = 1.;
		gPnts4d[1].x = xbase + 0.;
		gPnts4d[1].y = hbase + hscale * i;
		gPnts4d[1].z = zbase - 0;
		gPnts4d[1].w = 1.;
		Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
		g2.drawLine(gPnts[0].x, gPnts[0].y, gPnts[1].x, gPnts[1].y);
		String s = "" + i;
		if ((closestCorner - 1) % 4 == 0) {
		    int lw = fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[1].x - lw - 10, gPnts[1].y
			    + fm.getAscent() * i / displayMax);
		} else {
		    int lw = fm.stringWidth("" + displayMax)
			    - fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[0].x + lw + 10, gPnts[0].y
			    + fm.getAscent() * i / displayMax);
		}
	    }
	}

	if (closestCorner % 4 >= 2) {
	    // draw front
	    poly = new Polygon();
	    poly.addPoint(pnts[1].x, pnts[1].y);
	    poly.addPoint(pnts[0].x, pnts[0].y);
	    poly.addPoint(pnts[4].x, pnts[4].y);
	    poly.addPoint(pnts[5].x, pnts[5].y);
	    g2.setColor(new Color(240, 240, 240));
	    g2.fill(poly);
	    g2.setColor(Color.BLACK);
	    g2.draw(poly);
	    // draw height grid lines
	    Point4d[] gPnts4d = getPoint4dArray(2);
	    for (int i = 0; i <= displayMax; i += hIncr) {
		gPnts4d[0].x = xbase + 1.;
		gPnts4d[0].y = hbase + hscale * i;
		gPnts4d[0].z = zbase - 0.;
		gPnts4d[0].w = 1.;
		gPnts4d[1].x = xbase + 0.;
		gPnts4d[1].y = hbase + hscale * i;
		gPnts4d[1].z = zbase - 0.;
		gPnts4d[1].w = 1.;
		Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
		g2.drawLine(gPnts[0].x, gPnts[0].y, gPnts[1].x, gPnts[1].y);
		String s = "" + i;
		if (closestCorner % 4 == 2) {
		    int lw = fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[0].x - lw - 10, gPnts[0].y
			    + fm.getAscent() * i / displayMax);
		} else {
		    int lw = fm.stringWidth("" + displayMax)
			    - fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[1].x + lw + 10, gPnts[1].y
			    + fm.getAscent() * i / displayMax);
		}
	    }
	}
	if (closestCorner % 4 == 0 || closestCorner % 4 == 3) {
	    // draw right
	    poly = new Polygon();
	    poly.addPoint(pnts[2].x, pnts[2].y);
	    poly.addPoint(pnts[1].x, pnts[1].y);
	    poly.addPoint(pnts[5].x, pnts[5].y);
	    poly.addPoint(pnts[6].x, pnts[6].y);
	    g2.setColor(new Color(220, 220, 220));
	    g2.fill(poly);
	    g2.setColor(Color.BLACK);
	    g2.draw(poly);
	    // draw height grid lines
	    Point4d[] gPnts4d = getPoint4dArray(2);
	    for (int i = 0; i <= displayMax; i += hIncr) {
		gPnts4d[0].x = xbase + 1.;
		gPnts4d[0].y = hbase + hscale * i;
		gPnts4d[0].z = zbase - 0.;
		gPnts4d[0].w = 1.;
		gPnts4d[1].x = xbase + 1.;
		gPnts4d[1].y = hbase + hscale * i;
		gPnts4d[1].z = zbase - 1.;
		gPnts4d[1].w = 1.;
		Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
		g2.drawLine(gPnts[0].x, gPnts[0].y, gPnts[1].x, gPnts[1].y);
		String s = "" + i;
		if (closestCorner % 4 == 3) {
		    int lw = fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[1].x - lw - 10, gPnts[1].y
			    + fm.getAscent() * i / displayMax);
		} else {
		    int lw = fm.stringWidth("" + displayMax)
			    - fm.stringWidth("" + i);
		    g2.drawString(s, gPnts[0].x + lw + 10, gPnts[0].y
			    + fm.getAscent() * i / displayMax);
		}
	    }

	}
	// Control tilt and rotation so bottom is always in view
	if (true || closestCorner >= 4) {
	    // draw bottom
	    poly = new Polygon();
	    poly.addPoint(pnts[0].x, pnts[0].y);
	    poly.addPoint(pnts[1].x, pnts[1].y);
	    poly.addPoint(pnts[2].x, pnts[2].y);
	    poly.addPoint(pnts[3].x, pnts[3].y);
	    g2.setColor(new Color(250, 250, 250));
	    g2.fill(poly);
	    g2.setColor(Color.BLACK);
	    g2.draw(poly);
	    // draw bin labels
	    BinModel bm;
	    BinLabeler bl;
	    Point4d[] gPnts4d = getPoint4dArray(1);
	    if (hgm != null && hgm.getModelCount() == 2) {
		if (closestCorner % 4 == 0) {
		    double dx = pnts[1].x - pnts[0].x;
		    double dy = pnts[0].y - pnts[1].y;
		    double slopeRight = dx != 0. ? dy / dx : 0.;
		    double rotLeft = Math.atan2(-dy, dx); // perpendicular
		    dx = pnts[0].x - pnts[3].x;
		    dy = pnts[3].y - pnts[0].y;
		    double slopeLeft = dx != 0. ? dy / dx : 0.;
		    double rotRight = Math.atan2(-dy, dx); // perpendicular
		    // right
		    bm = hgm.getBinModel(0);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase + (.5 + i) * xscale;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase + .05;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			// g2.drawString(s,gPnts[0].x, gPnts[0].y +
			// fm.getAscent()/2);
			AffineTransform xform = g2.getTransform();
			g2.rotate(rotRight, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		    // left
		    bm = hgm.getBinModel(1);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase - .05;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase - (.5 + i) * zscale;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			int lw = fm.stringWidth(s);
			AffineTransform xform = g2.getTransform();
			// int xl = gPnts[0].x - slopeLeft * lw;
			// int yl = gPnts[0].y +
			g2.rotate(rotLeft, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x - lw, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		} else if (closestCorner % 4 == 1) {
		    double dx = pnts[2].x - pnts[1].x;
		    double dy = pnts[1].y - pnts[2].y;
		    double slopeRight = dx != 0. ? dy / dx : 0.;
		    double rotLeft = Math.atan2(-dy, dx); // perpendicular
		    dx = pnts[1].x - pnts[0].x;
		    dy = pnts[0].y - pnts[1].y;
		    double slopeLeft = dx != 0. ? dy / dx : 0.;
		    double rotRight = Math.atan2(-dy, dx); // perpendicular
		    // right
		    bm = hgm.getBinModel(1);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase + 1.05;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase - (.5 + i) * zscale;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			// g2.drawString(s,gPnts[0].x, gPnts[0].y +
			// fm.getAscent()/2);
			AffineTransform xform = g2.getTransform();
			g2.rotate(rotRight, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		    // left
		    bm = hgm.getBinModel(0);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase + (.5 + i) * xscale;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase + .05;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			int lw = fm.stringWidth(s);
			AffineTransform xform = g2.getTransform();
			// int xl = gPnts[0].x - slopeLeft * lw;
			// int yl = gPnts[0].y +
			g2.rotate(rotLeft, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x - lw, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		} else if (closestCorner % 4 == 2) {
		    double dx = pnts[3].x - pnts[2].x;
		    double dy = pnts[2].y - pnts[3].y;
		    double slopeRight = dx != 0. ? dy / dx : 0.;
		    double rotLeft = Math.atan2(-dy, dx); // perpendicular
		    dx = pnts[2].x - pnts[1].x;
		    dy = pnts[1].y - pnts[2].y;
		    double slopeLeft = dx != 0. ? dy / dx : 0.;
		    double rotRight = Math.atan2(-dy, dx); // perpendicular
		    // right
		    bm = hgm.getBinModel(0);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase + (.5 + i) * xscale;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase - 1.05;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			// g2.drawString(s,gPnts[0].x, gPnts[0].y +
			// fm.getAscent()/2);
			AffineTransform xform = g2.getTransform();
			g2.rotate(rotRight, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		    // left
		    bm = hgm.getBinModel(1);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase + 1.05;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase - (.5 + i) * zscale;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			int lw = fm.stringWidth(s);
			AffineTransform xform = g2.getTransform();
			// int xl = gPnts[0].x - slopeLeft * lw;
			// int yl = gPnts[0].y +
			g2.rotate(rotLeft, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x - lw, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		} else if (closestCorner % 4 == 3) {
		    double dx = pnts[0].x - pnts[3].x;
		    double dy = pnts[3].y - pnts[0].y;
		    double slopeRight = dx != 0. ? dy / dx : 0.;
		    double rotLeft = Math.atan2(-dy, dx); // perpendicular
		    dx = pnts[3].x - pnts[2].x;
		    dy = pnts[2].y - pnts[3].y;
		    double slopeLeft = dx != 0. ? dy / dx : 0.;
		    double rotRight = Math.atan2(-dy, dx); // perpendicular
		    // System.err.println("slopes\t" + slopeLeft + "\t" +
		    // rotLeft + "\t\t" + slopeRight + "\t" + rotRight);
		    // right
		    bm = hgm.getBinModel(1);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase - .05;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase - (.5 + i) * zscale;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			// g2.drawString(s,gPnts[0].x, gPnts[0].y +
			// fm.getAscent()/2);
			AffineTransform xform = g2.getTransform();
			g2.rotate(rotRight, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		    // left
		    bm = hgm.getBinModel(0);
		    bl = bm.getBinLabeler();
		    for (int i = 0; i < bm.getBinCount(); i++) {
			gPnts4d[0].x = xbase + (.5 + i) * xscale;
			gPnts4d[0].y = hbase;
			gPnts4d[0].z = zbase - 1.05;
			gPnts4d[0].w = 1.;
			Point[] gPnts = worldToScreen(gPnts4d, mvm, pjm, vp);
			String s = bl.getLabel(i);
			int lw = fm.stringWidth(s);
			AffineTransform xform = g2.getTransform();
			// int xl = gPnts[0].x - slopeLeft * lw;
			// int yl = gPnts[0].y +
			g2.rotate(rotLeft, (gPnts[0].x), (gPnts[0].y + fm
				.getAscent() / 2));
			g2.drawString(s, gPnts[0].x - lw, gPnts[0].y
				+ fm.getAscent() / 2);
			g2.setTransform(xform);
		    }
		}
	    }
	}
	/*
	 * // Tilt is controlled so that top doesn't need to be draw if
	 * (closestCorner < 4) { // draw top poly = new Polygon();
	 * poly.addPoint(pnts[7].x,pnts[7].y);
	 * poly.addPoint(pnts[6].x,pnts[6].y);
	 * poly.addPoint(pnts[5].x,pnts[5].y);
	 * poly.addPoint(pnts[4].x,pnts[4].y); g2.setColor(new
	 * Color(250,250,250)); g2.fill(poly); g2.setColor(Color.BLACK);
	 * g2.draw(poly); }
	 */
	if (bars != null) {
	    // draw bars back to front
	    if (closestCorner % 4 == 0) {
		for (int pos = bars.length - POLYS_PER_BAR; pos >= 0; pos -= POLYS_PER_BAR) {
		    for (int j = 0; j < POLYS_PER_BAR; j++) {
			if (bars[pos + j] != null) {
			    g2.setColor(getBarFacetColor(pos + j));
			    g2.fill(bars[pos + j]);
			}
		    }
		}
	    } else if (closestCorner % 4 == 2) {
		for (int pos = 0; pos < bars.length; pos += POLYS_PER_BAR) {
		    for (int j = 0; j < POLYS_PER_BAR; j++) {
			if (bars[pos + j] != null) {
			    g2.setColor(getBarFacetColor(pos + j));
			    g2.fill(bars[pos + j]);
			}
		    }
		}
	    } else if (closestCorner % 4 == 1) {

		int[] dim = hgm.getDimensions();
		int[] indices = new int[dim.length];
		int cnt = hgm.getBinCount();

		for (indices[0] = 0; indices[0] < dim[0]; indices[0]++) {
		    for (indices[1] = dim[1] - 1; indices[1] >= 0; indices[1]--) {
			int pos = MultiDimIntArray.getIndex(indices, dim)
				* POLYS_PER_BAR;
			for (int j = 0; j < POLYS_PER_BAR; j++) {
			    if (bars[pos + j] != null) {
				g2.setColor(getBarFacetColor(pos + j));
				g2.fill(bars[pos + j]);
			    }
			}
		    }
		}

	    } else if (closestCorner % 4 == 3) {

		int[] dim = hgm.getDimensions();
		int[] indices = new int[dim.length];
		int cnt = hgm.getBinCount();

		for (indices[0] = dim[0] - 1; indices[0] >= 0; indices[0]--) {
		    for (indices[1] = 0; indices[1] < dim[1]; indices[1]++) {
			int pos = MultiDimIntArray.getIndex(indices, dim)
				* POLYS_PER_BAR;
			for (int j = 0; j < POLYS_PER_BAR; j++) {
			    if (bars[pos + j] != null) {
				g2.setColor(getBarFacetColor(pos + j));
				g2.fill(bars[pos + j]);
			    }
			}
		    }
		}
	    }
	}
	if (selecting) {
	    g2.setColor(Color.black);
	    g2.drawRect(Math.min(current.x, start.x), Math.min(current.y,
		    start.y), Math.abs(current.x - start.x), Math.abs(current.y
		    - start.y));
	}
    }
}
