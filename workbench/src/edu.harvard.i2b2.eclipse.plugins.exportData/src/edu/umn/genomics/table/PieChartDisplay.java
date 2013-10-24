/*
 * @(#) $RCSfile: PieChartDisplay.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class PieChartDisplay extends JComponent {
    SetOperator setOperator = new DefaultSetOperator();
    int prevSetOp = -1;
    HistogramModel hgm;
    // Selection
    boolean selecting = false;
    Point start = null;
    Point current = null;
    int[] modelDim;
    int modelCount;
    int binCount;
    int binMax;
    Arc2D.Double[][] arcs = null;
    HistogramListener hdl = new HistogramListener() {
	public void histogramChanged(HistogramEvent e) {
	    if (!e.isAdjusting()) {
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
	BitSet bs = new BitSet();
	if (arcs != null) {
	    for (int j = 0; j < arcs.length; j++) {
		int[] indices = new int[j + 1];
		for (int i = 0; i < arcs[j].length; i++) {
		    if (arcs[j][i] != null) {
			if (arcs[j][i].intersects(r.getX(), r.getY(), r
				.getWidth(), r.getHeight())) {
			    int[] ia = MultiDimIntArray.getIndexArray(indices,
				    modelDim);
			    for (int k = 0; k < ia.length; k++) {
				bs.set(ia[k]);
			    }
			}
		    }
		    MultiDimIntArray.incrIndex(indices, modelDim);
		}
	    }
	}
	int[] selIdx = new int[bs.cardinality()];
	for (int i = 0, n = 0; i < bs.length(); i++) {
	    if (bs.get(i)) {
		selIdx[n++] = i;
	    }
	}
	select(selIdx);
    }

    public void select(Point p) {
	BitSet bs = new BitSet();
	if (arcs != null) {
	    for (int j = 0; j < arcs.length; j++) {
		int[] indices = new int[j + 1];
		for (int i = 0; i < arcs[j].length; i++) {
		    if (arcs[j][i] != null) {
			if (arcs[j][i].contains(p.getX(), p.getY())) {
			    int[] ia = MultiDimIntArray.getIndexArray(indices,
				    modelDim);
			    for (int k = 0; k < ia.length; k++) {
				bs.set(ia[k]);
			    }
			}
		    }
		    MultiDimIntArray.incrIndex(indices, modelDim);
		}
	    }
	}
	int[] selIdx = new int[bs.cardinality()];
	for (int i = 0, n = 0; i < bs.length(); i++) {
	    if (bs.get(i)) {
		selIdx[n++] = i;
	    }
	}
	select(selIdx);
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

    public PieChartDisplay(HistogramModel model, SetOperator setOperator) {
	this(model);
	setSetOperator(setOperator);
    }

    public PieChartDisplay(HistogramModel model) {
	hgm = model;
	setOpaque(true);
	hgm.addHistogramListener(hdl);
	addMouseListener(ma);
	addMouseMotionListener(ma);
    }

    public Dimension getViewPort() {
	Insets insets = getInsets();
	int vw = (getWidth() - insets.left - insets.right);
	int vh = (getHeight() - insets.top - insets.bottom);
	return new Dimension(vw, vh);
    }

    public Arc2D.Double[][] getArcs() {
	if (hgm == null) {
	    return null;
	}
	int nMod = hgm.getModelCount();
	modelDim = hgm.getDimensions();
	binCount = hgm.getBinCount();
	binMax = hgm.getMaxBinSize();
	if (modelDim == null || modelDim.length < 1) {
	    return null;
	}
	// allocate arc arrays
	Arc2D.Double[][] arca = new Arc2D.Double[modelDim.length][];
	for (int i = 0, cum = 1; i < modelDim.length; i++) {
	    cum *= modelDim[i];
	    arca[i] = new Arc2D.Double[cum];
	}
	int cx = getWidth() / 2;
	int cy = getHeight() / 2;
	int rw = Math.min(cx, cy) / 2;
	double scale = 360. / hgm.getItemCount();
	double angle = 0.;

	getArcs(modelDim, null, arca, cx, cy, rw, angle, scale);
	return arca;
    }

    public String arrayString(int[] arr) {
	String s = "[";
	if (arr != null && arr.length > 0) {
	    s += arr[0];
	    for (int i = 1; i < arr.length; i++) {
		s += "," + arr[i];
	    }
	}
	return s + "]";
    }

    public double getArcs(int[] dims, int[] indices, Arc2D[][] arca, double cx,
	    double cy, double rw, double angle, double scale) {
	if (indices == null) {
	    return getArcs(dims, new int[0], arca, cx, cy, rw, angle, scale);
	} else if (indices.length < dims.length) {
	    double startAngle = angle;
	    int[] indexes = new int[indices.length + 1];
	    System.arraycopy(indices, 0, indexes, 0, indices.length);
	    for (int i = 0; i < dims[indices.length]; i++) {
		indexes[indices.length] = i;
		startAngle = getArcs(dims, indexes, arca, cx, cy, rw,
			startAngle, scale);
	    }
	    if (indices.length > 0) {
		double rr = rw * indices.length / dims.length;
		int idx = indices != null ? MultiDimIntArray.getIndex(indices,
			dims) : 0;
		Arc2D.Double arc = new Arc2D.Double();
		arc.setArcByCenter(cx, cy, rr, angle, startAngle - angle,
			Arc2D.PIE);
		arca[indices.length - 1][idx] = arc;
		// arca[indices.length-1][idx] = new
		// Arc2D.Double(bx,by,bw,bh,angle,startAngle - angle,Arc2D.PIE);
	    }
	    return startAngle;
	} else {
	    int binCnt = hgm.getBinCount(indices);
	    int selCnt = hgm.getBinSelectCount(indices);
	    double extent = binCnt * scale;
	    int idx = indices != null ? MultiDimIntArray
		    .getIndex(indices, dims) : 0;
	    Arc2D.Double arc = new Arc2D.Double();
	    arc.setArcByCenter(cx, cy, rw, angle, extent, Arc2D.PIE);
	    arca[indices.length - 1][idx] = arc;
	    // arca[indices.length -1][idx] = new
	    // Arc2D.Double(bx,by,bw,bh,angle,extent,Arc2D.PIE);
	    return angle + extent;
	}
    }

    public Color getColor(Arc2D arc) {
	float hue = ((int) (arc.getAngleStart() + arc.getAngleExtent() / 2) % 360) / 360f;
	return new Color(Color.HSBtoRGB(hue, 1f, 1f));
    }

    @Override
    public void paintComponent(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	// draw background
	if (isOpaque()) {
	    g2.setColor(getBackground());
	    g2.fillRect(0, 0, getWidth(), getHeight());
	}
	FontMetrics fm = g2.getFontMetrics();
	Arc2D lblArc = new Arc2D.Double();
	Arc2D.Double arc;
	arcs = getArcs();
	if (arcs != null) {

	    for (int j = arcs.length - 1; j >= 0; j--) {
		// System.err.println("arcs " + j + " " + arcs[j].length + "  "
		// + arrayString(hgm.getBinCounts()));
		for (int i = 0; i < arcs[j].length; i++) {
		    arc = arcs[j][i];
		    if (arc != null) {
			g2.setColor(getColor(arc));
			g2.fill(arc);
			g2.setColor(Color.black);
			g2.draw(arc);
		    }
		}
	    }

	    for (int j = arcs.length - 1; j >= 0; j--) {
		BinLabeler bl = hgm.getBinModel(j).getBinLabeler();
		int binSize = hgm.getBinModel(j).getBinCount();
		int winSize = Math.min(getWidth(), getHeight());
		double radius = winSize / 4 + winSize / arcs.length * .2 * j
			+ 5;
		if (bl != null) {
		    for (int i = 0; i < arcs[j].length; i++) {
			arc = arcs[j][i];
			if (arc != null) {
			    String lbl = bl.getLabel(i % binSize);
			    int sw = fm.stringWidth(lbl);
			    lblArc.setArcByCenter(arc.getCenterX(), arc
				    .getCenterY(), arc.getWidth() * .4, arc
				    .getAngleStart()
				    + arc.getAngleExtent() / 2, 0., Arc2D.PIE);
			    Point2D p1 = lblArc.getStartPoint();
			    lblArc.setArcByCenter(arc.getCenterX(), arc
				    .getCenterY(), radius, arc.getAngleStart()
				    + arc.getAngleExtent() / 2, 0., Arc2D.PIE);
			    Point2D p2 = lblArc.getStartPoint();
			    g2.setColor(Color.darkGray);
			    g2.draw(new Line2D.Double(p1, p2));
			    g2.setColor(Color.black);
			    double cos = Math.cos(lblArc.getAngleStart()
				    * Math.PI / 180);
			    double sin = Math.sin(lblArc.getAngleStart()
				    * Math.PI / 180);
			    int lx = (int) (p2.getX() + (sw * (cos - 1) * .5) + fm
				    .getMaxAdvance()
				    * cos);
			    int ly = (int) (p2.getY() - fm.getAscent() * sin);
			    // g2.drawString(lbl, lx, ly);
			    g2.translate(lx, ly);
			    g2.setColor(getBackground());
			    g2.fill(fm.getStringBounds(lbl, g2));
			    g2.setColor(getForeground());
			    g2.drawString(lbl, 0, 0);
			    g2.translate(-lx, -ly);
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
