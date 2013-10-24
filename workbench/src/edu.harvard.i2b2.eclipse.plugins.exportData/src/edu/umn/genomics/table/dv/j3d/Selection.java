/*
 * @(#) $RCSfile: Selection.java,v $ $Revision: 1.4 $ $Date: 2008/11/05 18:46:35 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.dv.j3d;

/*
 * Selection.java 
 */

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.picking.*;
import javax.swing.ListSelectionModel;
import edu.umn.genomics.table.SetOperator;

/**
 * Processes mouse events for picking indices of an IndexedPointSet and sets the
 * cooresponding indices in a ListSelectionModel. The user drags out a circular
 * region that has a diameter from the mouse pressed point to the mouse release
 * point. The set of selected points are applied to current selected set using
 * the set operator from the SetOperator.
 * 
 * This class relies on a PickCanvas that is available in Java3D1.2.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/11/05 18:46:35 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see edu.umn.genomics.table.SetOperator
 * @see com.sun.j3d.utils.picking.PickCanvas
 */
public class Selection extends Behavior implements Serializable {
    float size;
    PickCanvas pickCanvas;
    PickResult[] pickResult;
    ListSelectionModel lsm = null;
    int setOp = SetOperator.REPLACE;
    SetOperator setOperator = null;
    int prevSetOp = -1;
    Point lastPoint = null;

    /**
     * Create Selection object for the given branchGroup displayed on the given
     * canvas.
     * 
     * @param canvas3D
     *            The canvas on which the selection will be performed.
     * @param branchGroup
     *            The BranchGroup that contains the geometry to be picked.
     * @param size
     *            The default minimum radius in pixels for picking points.
     * @param lsm
     *            The model in which to record the point indices picked.
     * @param setOperator
     *            provides the set operator
     */
    public Selection(Canvas3D canvas3D, BranchGroup branchGroup, float size,
	    ListSelectionModel lsm, SetOperator setOperator) {
	pickCanvas = new PickCanvas(canvas3D, branchGroup);
	pickCanvas.setTolerance(5.0f);
	pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
	this.size = size;
	this.lsm = lsm;
	this.setOperator = setOperator;
    }

    /**
     * Return the canvas this operates on.
     * 
     * @return the canvas this operates on.
     */
    public Canvas3D getCanvas3D() {
	return pickCanvas.getCanvas();
    }

    /**
     * Return the SetOperator that specifies the current set operation.
     * 
     * @return the SetOperator in use.
     */
    public SetOperator getSetOperator() {
	return setOperator;
    }

    /**
     * Initialize is called by the Java 3D behavior scheduler.
     */
    @Override
    public void initialize() {
	wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
    }

    /**
     * processStimulus is called by the Java 3D behavior scheduler.
     * 
     * @param criteria
     *            an enumeration of triggered wakeup criteria for this behavior.
     */
    @Override
    public void processStimulus(Enumeration criteria) {
	WakeupCriterion wakeup;
	AWTEvent[] event;
	int id;

	while (criteria.hasMoreElements()) {
	    wakeup = (WakeupCriterion) criteria.nextElement();
	    if (wakeup instanceof WakeupOnAWTEvent) {
		event = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
		for (int i = 0; i < event.length; i++) {
		    id = event[i].getID();
		    if (id == MouseEvent.MOUSE_PRESSED) {
			lastPoint = ((MouseEvent) event[i]).getPoint();
			if (setOperator != null) {
			    prevSetOp = setOperator.getSetOperator();
			    setOperator
				    .setFromInputEventMask(((MouseEvent) event[i])
					    .getModifiers());
			}
			wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED));
		    } else if (id == MouseEvent.MOUSE_RELEASED) {
			int cx = ((MouseEvent) event[i]).getX();
			int cy = ((MouseEvent) event[i]).getY();
			int lx = lastPoint.x;
			int ly = lastPoint.y;
			double r = Math.sqrt(((cx - lx) * (cx - lx))
				+ ((cy - ly) * (cy - ly))) * .5;
			float t = (float) (r > size ? r : size);
			pickCanvas.setTolerance(t);
			int x, y;
			x = lx + (cx - lx) / 2;
			y = ly + (cy - ly) / 2;
			pickCanvas.setShapeLocation(x, y);

			Point3d eyePos = pickCanvas.getStartPosition();
			pickResult = pickCanvas.pickAllSorted();
			// Use this to do picking benchmarks
			/*
			 * long start = System.currentTimeMillis(); for (int
			 * l=0;l<3;l++) { if (l == 0) System.out.print
			 * ("BOUNDS: "); if (l == 1) System.out.print
			 * ("GEOMETRY: "); if (l == 2) System.out.print
			 * ("GEOMETRY_INTERSECT_INFO: ");
			 * 
			 * for (int k=0;k<1000;k++) { if (l == 0) {
			 * pickCanvas.setMode(PickTool.BOUNDS); pickResult =
			 * pickCanvas.pickAllSorted(); } if (l == 1) {
			 * pickCanvas.setMode(PickTool.GEOMETRY); pickResult =
			 * pickCanvas.pickAllSorted(); } if (l == 2) {
			 * pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
			 * pickResult = pickCanvas.pickAllSorted(); } } long
			 * delta = System.currentTimeMillis() - start;
			 * System.out.println ("\t"+delta+" ms / 1000 picks"); }
			 */
			if (pickResult != null) {
			    for (int j = 0; j < 1; j++) {
				// Get node info
				Node curNode = pickResult[j].getObject();
				Geometry curGeom = ((Shape3D) curNode)
					.getGeometry();
				GeometryArray curGeomArray = (GeometryArray) curGeom;

				if (false) {
				    // Get closest intersection results
				    PickIntersection pi = pickResult[j]
					    .getClosestIntersection(eyePos);
				    // Position sphere at intersection point
				    Vector3d v = new Vector3d();
				    Point3d intPt = pi.getPointCoordinatesVW();
				    // Position sphere at closest vertex
				    Point3d closestVert = pi
					    .getClosestVertexCoordinatesVW();

				    Point3d[] ptw = pi
					    .getPrimitiveCoordinatesVW();
				    Point3d[] pt = pi.getPrimitiveCoordinates();
				    int[] coordidx = pi
					    .getPrimitiveCoordinateIndices();
				    Point3d ptcoord = new Point3d();

				    // Get interpolated color (if available)
				    Color4f iColor4 = null;
				    Color3f iColor = null;
				    Vector3f iNormal = null;

				    if (curGeomArray != null) {
					int vf = curGeomArray.getVertexFormat();

					if (((vf & (GeometryArray.COLOR_3 | GeometryArray.COLOR_4)) != 0)
						&& (null != (iColor4 = pi
							.getPointColor()))) {
					    iColor = new Color3f(iColor4.x,
						    iColor4.y, iColor4.z);

					}

					if (((vf & GeometryArray.NORMALS) != 0)
						&& (null != (iNormal = pi
							.getPointNormal()))) {
					    System.out
						    .println("Interpolated normal: "
							    + iNormal);
					}
				    }

				    System.out.println("=============");
				    System.out
					    .println("Coordinates of intersection pt:"
						    + intPt);
				    System.out
					    .println("Coordinates of vertices: ");
				    for (int k = 0; k < pt.length; k++) {
					System.out.println(k + ":" + ptw[k].x
						+ " " + ptw[k].y + " "
						+ ptw[k].z);
				    }
				    System.out.println("Closest vertex: "
					    + closestVert);
				    if (coordidx != null && coordidx.length > 0) {
					if (lsm != null)
					    lsm.setValueIsAdjusting(true);
					System.out.print("Indices: "
						+ (lsm != null));
					for (int k = 0; k < coordidx.length; k++) {
					    if (lsm != null)
						lsm.addSelectionInterval(
							coordidx[k],
							coordidx[k]);
					    if (k % 6 == 0)
						System.out.print("\n\t");
					    System.out.print(coordidx[k] + " ");
					}
					if (lsm != null)
					    lsm.setValueIsAdjusting(false);
					System.out.println("");
				    }

				    if (iColor != null) {
					System.out
						.println("Interpolated color: "
							+ iColor);
				    }
				    if (iNormal != null) {
					System.out
						.println("Interpolated normal: "
							+ iNormal);
				    }
				}

				if (lsm != null) {
				    lsm.setValueIsAdjusting(true);
				    int nhit = pickResult[j].numIntersections();
				    if (setOperator != null) {
					setOp = setOperator.getSetOperator();
				    }
				    switch (setOp) {
				    case SetOperator.REPLACE:
					lsm.clearSelection();
				    case SetOperator.UNION:
					for (int k = 0; k < nhit; k++) {
					    PickIntersection pi = pickResult[j]
						    .getIntersection(k);
					    int[] coordidx = pi
						    .getPrimitiveCoordinateIndices();
					    if (coordidx != null
						    && coordidx.length > 0) {
						for (int l = 0; l < coordidx.length; l++) {
						    lsm.addSelectionInterval(
							    coordidx[l],
							    coordidx[l]);
						}
					    }
					}
					break;

				    case SetOperator.DIFFERENCE:
					for (int k = 0; k < nhit; k++) {
					    PickIntersection pi = pickResult[j]
						    .getIntersection(k);
					    int[] coordidx = pi
						    .getPrimitiveCoordinateIndices();
					    if (coordidx != null
						    && coordidx.length > 0) {
						for (int l = 0; l < coordidx.length; l++) {
						    lsm
							    .removeSelectionInterval(
								    coordidx[l],
								    coordidx[l]);
						}
					    }
					}
					break;
				    case SetOperator.INTERSECTION:
				    case SetOperator.XOR:
				    case SetOperator.BRUSHOVER:
					break;
				    }
				    lsm.setValueIsAdjusting(false);
				}
			    }
			}
			if (setOperator != null) {
			    setOperator.setSetOperator(prevSetOp);
			}
			wakeupOn(new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
		    }
		}
	    }
	}
    }
}
