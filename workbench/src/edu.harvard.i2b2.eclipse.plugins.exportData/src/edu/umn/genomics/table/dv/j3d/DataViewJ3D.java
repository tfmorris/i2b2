/*
 * @(#) $RCSfile: DataViewJ3D.java,v $ $Revision: 1.4 $ $Date: 2008/11/07 21:03:30 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table.dv.j3d; //DataViewer

import java.io.Serializable;
import javax.swing.table.*;
import javax.swing.ListSelectionModel;
import java.util.Hashtable;
import java.util.BitSet;
import java.util.Observer;
import java.util.Enumeration;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Cursor;
import java.awt.Component;
import edu.umn.genomics.table.dv.*;
import edu.umn.genomics.j3d.CaptureCanvas3D;
import edu.umn.genomics.graph.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;

/**
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/11/07 21:03:30 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DataViewJ3D extends DataView implements Serializable {
    /** map the Branchgroup to the DataMap it comes from. */
    private Hashtable bgToDMHash = new Hashtable();

    /*
     * debug routine private void showSwitch(String s) { int i = 0; for
     * (Enumeration e = dataMaps.getAllChildren(); e.hasMoreElements() ;i++) {
     * DataMap dm = (DataMap)bgToDMHash.get((Group)e.nextElement());
     * System.out.println(s + i + " " + dm.getName()); } BitSet childmask =
     * dataMaps.getChildMask(); System.err.println(s + "mask " +
     * childmask.toString() + " of " + dataMaps.numChildren()); }
     */

    /**
     * Find the index for the DataMap in the Switch node.
     * 
     * @param the
     *            DataMap to find
     * @return the index in the Switch Node.
     */
    private int getDataMapIndex(DataMap target) {
	int i = 0;
	for (Enumeration e = dataMaps.getAllChildren(); e.hasMoreElements(); i++) {
	    DataMap dm = (DataMap) bgToDMHash.get(e.nextElement());
	    if (dm == target)
		return i;
	}
	return -1;
    }

    /**
     * Create a DataMap for the TableModel, give it the name, and display it.
     * 
     * @param name
     *            The name by which the data will be listed.
     * @param tableModel
     *            to display
     * @return the DataMap created for the tableModel
     */
    @Override
    public DataMap addDataMap(String name, TableModel tableModel) {
	return addDataMap(name, tableModel, null);
    }

    /**
     * Create a DataMap for the TableModel, give it the name, and display it.
     * 
     * @param name
     *            The name by which the data will be listed.
     * @param tableModel
     *            to display
     * @param lsm
     *            to display
     * @return the DataMap created for the tableModel
     */
    @Override
    public DataMap addDataMap(String name, TableModel tableModel,
	    ListSelectionModel lsm) {
	DataMapJ3D dm = new DataMapJ3D(name, tableModel, lsm);
	DataMapJ3D prev = (DataMapJ3D) dataMapName.put(name, dm);
	// System.err.println("DV3D addDataMap " + dm.getName());
	// check if it already exists
	if (prev != null) {
	    // System.err.println("remove old dataMap " + name );
	} else {
	    // System.err.println("add new  dataMap " + name );
	}
	bgToDMHash.put(dm.getBranchGroup(), dm);

	// make branchgroups
	// add to scenegraph
	// super.addDataMap(name,tableModel);
	// add entry to hashtable

	BitSet saveMask = dataMaps.getChildMask();

	dataMaps.addChild(dm.getBranchGroup());
	int nodeIndex = getDataMapIndex(dm);

	BitSet childmask = new BitSet(dataMaps.numChildren());
	childmask.or(saveMask);
	childmask.set(nodeIndex);
	for (int i = nodeIndex + 1; i < saveMask.length(); i++) {
	    if (saveMask.get(i))
		childmask.set(i + 1);
	    else
		childmask.clear(i + 1);
	}

	dataMaps.setChildMask(childmask);

	BitSet newmask = dataMaps.getChildMask();
	saveMask.xor(newmask);
	setViewpoint();
	try {
	    BoundingBox bbox = new BoundingBox(dataGroup.getBounds());
	    if (!bbox.isEmpty()) {
		Point3d l = new Point3d(), u = new Point3d();
		bbox.getLower(l);
		bbox.getUpper(u);
		// System.err.println("[" + l.x + "," + l.y + "," + l.z + "] ["
		// + u.x + "," + u.y + "," + u.z + "]" + " " + bbox.isEmpty());
		setAxes(l.x, l.y, l.z, u.x, u.y, u.z);
	    }
	} catch (Exception e) {
	    System.err.println("bbox: " + e);
	}
	// System.err.println("isLive: " + dm.getBranchGroup().isLive());
	dm.addObserver(this);
	// System.err.println("DV3D addDataMap " + dm.getName());
	dataMapList.addElement(dm);
	int index = dataMapList.indexOf(dm);
	dataMapJList.addSelectionInterval(index, index);
	dm
		.doSelection(getViewAction() == NAVIGATE ? null : canvas,
			setOperator);
	return dm;
    }

    @Override
    public void deleteDataMap(DataMap dataMap) {
	int nodeIndex = getDataMapIndex(dataMap);
	if (nodeIndex >= 0) {
	    BitSet saveMask = dataMaps.getChildMask();
	    dataMaps.removeChild(nodeIndex);
	    BitSet childmask = new BitSet(dataMaps.numChildren());
	    for (int i = 0; i < nodeIndex; i++) {
		if (saveMask.get(i)) {
		    childmask.set(i);
		} else {
		    childmask.clear(i);
		}
	    }
	    for (int i = nodeIndex; i < saveMask.length() - 1; i++) {
		if (saveMask.get(i + 1)) {
		    childmask.set(i);
		} else {
		    childmask.clear(i);
		}
	    }
	    dataMaps.setChildMask(childmask);
	}
	bgToDMHash.remove(((DataMapJ3D) dataMap).getBranchGroup());
	// System.err.println("DV3D deleteDataMap " + dataMap.getName());
    }

    @Override
    public void showDataMap(DataMap dataMap, boolean flag) {
	int nodeIndex = getDataMapIndex(dataMap);
	if (nodeIndex >= 0) {
	    BitSet childmask = dataMaps.getChildMask();
	    if (flag) {
		childmask.set(nodeIndex);
	    } else {
		childmask.clear(nodeIndex);
	    }
	    dataMaps.setChildMask(childmask);
	}
	// System.err.println("DV3D showDataMap " + dataMap.getName() + " " +
	// flag);
    }

    CaptureCanvas3D canvas;
    SimpleUniverse universe;
    TransformGroup vpTransGroup;
    View view;
    BranchGroup sceneRoot;
    TransformGroup examineGroup;
    BranchGroup sceneGroup;
    BranchGroup axesGroup;
    BranchGroup dataGroup;
    BranchGroup navGroup;

    BoundingSphere sceneBounds;
    DirectionalLight headLight;
    AmbientLight ambLight;
    LinearFog fog;
    Cursor waitCursor;
    Cursor handCursor;
    MouseRotate mr;
    MouseTranslate mt;
    MouseZoom mz;

    BitSet dataMapMask = new BitSet();
    Switch dataMaps;

    private static GraphicsConfiguration getGraphicsConfig() {
	GraphicsEnvironment e = GraphicsEnvironment
		.getLocalGraphicsEnvironment();
	GraphicsDevice d = e.getDefaultScreenDevice();
	GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
	GraphicsConfiguration c = d.getBestConfiguration(template);
	return c;
    }

    public DataViewJ3D() {
	super();
	// System.err.println("Before Canvas3D");
	canvas = new CaptureCanvas3D(getGraphicsConfig());
	// System.err.println("After Canvas3D");

	add(canvas);

	waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	handCursor = new Cursor(Cursor.HAND_CURSOR);

	universe = new SimpleUniverse(canvas);
	ViewingPlatform viewingPlatform = universe.getViewingPlatform();
	viewingPlatform.setNominalViewingTransform();
	vpTransGroup = viewingPlatform.getViewPlatformTransform();
	Viewer viewer = universe.getViewer();
	view = viewer.getView();

	// view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
	// view.setProjectionPolicy(View.PARALLEL_PROJECTION);

	setupBehavior();
	createScene();

    }

    /**
     * Set the viewpoint so that the entire geometry is displayed.
     */
    @Override
    public void setViewpoint() {
	sceneBounds = (BoundingSphere) dataGroup.getBounds();
	if (sceneBounds.isEmpty()) {
	    return;
	}
	Transform3D viewTrans = new Transform3D();
	Transform3D eyeTrans = new Transform3D();

	// point the view at the center of the object
	Point3d center = new Point3d();
	sceneBounds.getCenter(center);
	double radius = Math.abs(sceneBounds.getRadius());
	// System.err.println("setViewpoint center: " + center.toString() +
	// "radius: " + radius + " " + sceneBounds.isEmpty());
	Vector3d temp = new Vector3d(center);
	viewTrans.set(temp);
	// System.err.println("setViewpoint \n" + viewTrans.toString());

	// JJ set clipping planes
	view.setBackClipDistance(radius * 40);

	// pull the eye back far enough to see the whole object
	double eyeDist = radius / Math.tan(view.getFieldOfView() / 2.0);
	// eyeDist += Math.abs(eyeDist * .4);
	temp.x = 0.0;
	temp.y = 0.0;
	temp.z = eyeDist;
	eyeTrans.set(temp);
	viewTrans.mul(eyeTrans);
	// System.err.println("setViewpoint \n" + viewTrans.toString());

	// set the view transform
	vpTransGroup.setTransform(viewTrans);
    }

    /**
     * Set viewing behavior. Mouse Button1 rotates the geometry. Mouse Button1
     * zooms the geometry. Mouse Button1 translates the geometry.
     */
    private void setupBehavior() {
	sceneRoot = new BranchGroup();
	sceneRoot.setCapability(Group.ALLOW_CHILDREN_READ);
	sceneRoot.setCapability(Group.ALLOW_CHILDREN_WRITE);
	sceneRoot.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	examineGroup = new TransformGroup();
	examineGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	examineGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	examineGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	examineGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	examineGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	sceneRoot.addChild(examineGroup);

	BoundingSphere behaviorBounds = new BoundingSphere(new Point3d(),
		Double.MAX_VALUE);

	// JJ try scaling
	Transform3D transform = new Transform3D();
	examineGroup.getTransform(transform);
	// transform.setScale(10.);
	examineGroup.setTransform(transform);

	navGroup = new BranchGroup();
	navGroup.setCapability(BranchGroup.ALLOW_DETACH);

	mr = new MouseRotate();
	mr.setFactor(mr.getXFactor() * .2, mr.getYFactor() * .02);
	mr.setTransformGroup(examineGroup);
	mr.setSchedulingBounds(behaviorBounds);
	navGroup.addChild(mr);

	mt = new MouseTranslate();
	mt.setTransformGroup(examineGroup);
	mt.setSchedulingBounds(behaviorBounds);
	navGroup.addChild(mt);

	mz = new MouseZoom();
	mz.setTransformGroup(examineGroup);
	mz.setSchedulingBounds(behaviorBounds);
	navGroup.addChild(mz);
	sceneRoot.addChild(navGroup);

	BoundingSphere lightBounds = new BoundingSphere(new Point3d(),
		Double.MAX_VALUE);
	ambLight = new AmbientLight(true, new Color3f(1.0f, 1.0f, 1.0f));
	ambLight.setInfluencingBounds(lightBounds);
	ambLight.setCapability(Light.ALLOW_STATE_WRITE);
	sceneRoot.addChild(ambLight);
	headLight = new DirectionalLight();
	headLight.setCapability(Light.ALLOW_STATE_WRITE);
	headLight.setInfluencingBounds(lightBounds);
	sceneRoot.addChild(headLight);

	universe.addBranchGraph(sceneRoot);
    }

    private void doNavigationBehavior(boolean flag) {
	if (flag) {
	    sceneRoot.addChild(navGroup);
	} else {
	    navGroup.detach();
	}
    }

    /**
     * Create a scene graph containing geometry objects for the alignment.
     */
    void createScene() {
	canvas.setCursor(waitCursor);

	if (sceneGroup != null) {
	    sceneGroup.detach();
	}
	// get the scene group
	sceneGroup = new BranchGroup();
	sceneGroup.setCapability(BranchGroup.ALLOW_DETACH);
	sceneGroup.setCapability(Node.ALLOW_BOUNDS_READ);
	sceneGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	sceneGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	sceneGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	// Add coordinate axes
	setAxes();

	// Add the alignment data
	setData();

	// add the scene group to the scene
	examineGroup.addChild(sceneGroup);

	// now that the scene group is "live" we can inquire the bounds
	sceneBounds = (BoundingSphere) sceneGroup.getBounds();

	if (!true) { // JJ Add fog (does fog only affect Material coloring?)
	    BranchGroup fogGrp = new BranchGroup();
	    fog = new LinearFog();
	    fog.setBackDistance(sceneBounds.getRadius());
	    fog.addScope(dataGroup);
	    fogGrp.addChild(fog);
	    dataGroup.addChild(fogGrp);
	}

	// set up a viewpoint to include the bounds
	setViewpoint();

	canvas.setCursor(handCursor);
    }

    /**
     * Creates default axes.
     */
    public void setAxes() {
	double xdl = 0.;
	double ydl = 0.;
	double zdl = 0.;
	double xdh = 10.;
	double ydh = 10.;
	double zdh = 10.;
	setAxes(xdl, ydl, zdl, xdh, ydh, zdh);
    }

    /**
     * Creates the axes.
     */
    public void setAxes(double xdl, double ydl, double zdl, double xdh,
	    double ydh, double zdh) {

	if (displayAxes) {
	    // data dimensions
	    double dx = xdh - xdl; // width
	    double dy = ydh - ydl; // height
	    double dz = zdh - zdl; // height

	    double maxdim = dx > dy ? (dx > dz ? dx : dz) : (dy > dz ? dy : dz);
	    mt.setFactor(maxdim * .01); // scale mouse translation
	    mz.setFactor(maxdim * .01); // scale mouse zoom

	    if (axesGroup != null) {
		axesGroup.detach();
	    }
	    axesGroup = new BranchGroup();
	    axesGroup.setCapability(BranchGroup.ALLOW_DETACH);

	    Color3f axisColor = new Color3f(1.f, 1.f, 1.f);
	    Color3f tickColor = new Color3f(5.f, 5.f, 1.f);

	    // 
	    if (true) {

		Point3d lbb = new Point3d(xdl, ydl, zdl);
		Point3d rbb = new Point3d(xdh, ydl, zdl);
		Point3d ltb = new Point3d(xdl, ydh, zdl);
		Point3d rtb = new Point3d(xdh, ydh, zdl);

		Point3d lbf = new Point3d(xdl, ydl, zdh);
		Point3d rbf = new Point3d(xdh, ydl, zdh);
		Point3d ltf = new Point3d(xdl, ydh, zdh);
		Point3d rtf = new Point3d(xdh, ydh, zdh);

		Color3f red = new Color3f(1f, 0f, 0f);
		Color3f green = new Color3f(0f, 1f, 0f);
		Color3f blue = new Color3f(0f, 0f, 1f);
		Color3f white = new Color3f(1f, 1f, 1f);

		if (true) {
		    Point3d coord[] = { lbb, rbb, // xaxis
			    lbb, ltb, // yaxis
			    lbb, lbf // zaxis
		    };

		    Color3f color[] = { red, white, // xaxis
			    green, white, // yaxis
			    blue, white // zaxis
		    };

		    LineArray la = new LineArray(coord.length,
			    GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		    la.setCoordinates(0, coord);
		    la.setColors(0, color);
		    Appearance appearance = new Appearance();
		    LineAttributes lineAttributes = new LineAttributes(2.f,
			    LineAttributes.PATTERN_SOLID, true);
		    appearance.setLineAttributes(lineAttributes);
		    Shape3D shape = new Shape3D(la, appearance);
		    axesGroup.addChild(shape);

		}

		if (true) {
		    Point3d coord[] = { ltb, rtb, rtb, rtf, rtf, ltf, ltf, ltb,
			    lbf, ltf, rbf, rtf, rbb, rtb, lbf, rbf, rbf, rbb };

		    LineArray la = new LineArray(coord.length,
			    GeometryArray.COORDINATES | GeometryArray.COLOR_3);
		    la.setCoordinates(0, coord);
		    for (int i = 0; i < coord.length; i++)
			la.setColor(i, white);
		    Appearance appearance = new Appearance();
		    Shape3D shape = new Shape3D(la, appearance);
		    axesGroup.addChild(shape);
		}

		if (true) {
		    LinearAxis xAxis = new LinearAxis();
		    xAxis.setSize((int) Math.ceil(dx));
		    xAxis.setMin(xdl);
		    xAxis.setMax(xdh);
		    double xticks[] = xAxis.getTicks();
		    LinearAxis yAxis = new LinearAxis();
		    yAxis.setSize((int) Math.ceil(dy));
		    yAxis.setMin(ydl);
		    yAxis.setMax(ydh);
		    double yticks[] = yAxis.getTicks();
		    LinearAxis zAxis = new LinearAxis();
		    zAxis.setSize((int) Math.ceil(dz));
		    zAxis.setMin(zdl);
		    zAxis.setMax(zdh);
		    double zticks[] = zAxis.getTicks();
		    int tcnt = (xticks.length + yticks.length + zticks.length) / 2;
		    double coord[] = new double[tcnt * 3];
		    int ci = 0;
		    for (int i = 0; i < xticks.length; i += 2) {
			coord[ci++] = xdl + xticks[i];
			coord[ci++] = ydl;
			coord[ci++] = zdl;
		    }
		    for (int i = 0; i < yticks.length; i += 2) {
			coord[ci++] = xdl;
			coord[ci++] = ydl + yticks[i];
			coord[ci++] = zdl;
		    }
		    for (int i = 0; i < zticks.length; i += 2) {
			coord[ci++] = xdl;
			coord[ci++] = ydl;
			coord[ci++] = zdl + zticks[i];
		    }
		    PointArray pa = new PointArray(tcnt,
			    GeometryArray.COORDINATES);
		    pa.setCoordinates(0, coord);
		    Appearance appearance = new Appearance();
		    PointAttributes pointAttributes = new PointAttributes(3.f,
			    true);
		    appearance.setPointAttributes(pointAttributes);
		    Shape3D shape = new Shape3D(pa, appearance);
		    axesGroup.addChild(shape);
		}
		// Base
		if (true) {
		    Point3d coord[] = { lbb, lbf, rbf, rbb // around base
		    };
		    QuadArray qa = new QuadArray(coord.length,
			    GeometryArray.COORDINATES | GeometryArray.NORMALS);
		    qa.setCoordinates(0, coord);
		    Vector3f normal = new Vector3f(0f, 1f, 0f);
		    for (int i = 0; i < coord.length; i++)
			qa.setNormal(i, normal);
		    Appearance appearance = new Appearance();
		    appearance
			    .setTransparencyAttributes(new TransparencyAttributes(
				    TransparencyAttributes.FASTEST, .8f));
		    Shape3D shape = new Shape3D(qa, appearance);
		    axesGroup.addChild(shape);
		}

	    }

	    if (!true) { // x axis
		LinearAxis xAxis = new LinearAxis();
		xAxis.setSize((int) Math.ceil(dx));
		xAxis.setMin(xdl);
		xAxis.setMax(xdh);
		double xticks[] = xAxis.getTicks();
		int vcnt = 2 + xticks.length;
		int count = vcnt * 3;
		double coordinates[] = new double[count];
		coordinates[0] = xdl;
		coordinates[1] = ydl; // 0;
		coordinates[2] = zdl; // 0;
		coordinates[3] = xdh;
		coordinates[4] = ydl; // 0;
		coordinates[5] = zdl; // 0;
		int ti = 0;
		for (int i = 6; i < count; i += 6) { // tick marks
		    coordinates[i] = xdl + xticks[ti];
		    coordinates[i + 1] = ydl; // 0;
		    coordinates[i + 2] = zdl; // 0;
		    coordinates[i + 3] = xdl + xticks[ti];
		    coordinates[i + 4] = ydl + 1; // 1;
		    coordinates[i + 5] = zdl; // 0;
		    ti += 2;
		}

		LineArray xla = new LineArray(vcnt, GeometryArray.COORDINATES
			| GeometryArray.COLOR_3);
		xla.setCoordinates(0, coordinates);
		for (int i = 0; i < 2; i++)
		    xla.setColor(i, axisColor);
		for (int i = 2; i < vcnt; i++)
		    xla.setColor(i, tickColor);
		Appearance appearance = new Appearance();
		Material material = new Material();
		// material.setEmissiveColor(1.f, 0.f, 0.f);
		// appearance.setMaterial(material);
		// LineAttributes lineAttributes = new LineAttributes(2.f,
		// LineAttributes.PATTERN_SOLID, true);
		// appearance.setLineAttributes(lineAttributes);
		appearance.setColoringAttributes(new ColoringAttributes(1.f,
			0.f, 0.f, ColoringAttributes.SHADE_FLAT));
		Shape3D shape = new Shape3D(xla, appearance);
		axesGroup.addChild(shape);
	    }

	    if (!true) { // y axis
		LinearAxis yAxis = new LinearAxis();
		yAxis.setSize((int) Math.ceil(dy));
		yAxis.setMin(ydl);
		yAxis.setMax(ydh);
		double yticks[] = yAxis.getTicks();
		int vcnt = 2 + yticks.length;
		int count = vcnt * 3;
		double coordinates[] = new double[count];
		coordinates[0] = xdl; // 0;
		coordinates[1] = ydl;
		coordinates[2] = zdl; // 0;
		coordinates[3] = xdl; // 0;
		coordinates[4] = ydh;
		coordinates[5] = zdl; // 0;
		int ti = 0;
		for (int i = 6; i < count; i += 6) { // tick marks
		    coordinates[i] = xdl; // 0;
		    coordinates[i + 1] = ydl + yticks[ti];
		    coordinates[i + 2] = zdl; // 0;
		    coordinates[i + 3] = xdl + 1;
		    coordinates[i + 4] = ydl + yticks[ti];
		    coordinates[i + 5] = zdl; // 0;
		    ti += 2;
		}
		LineArray yla = new LineArray(vcnt, GeometryArray.COORDINATES
			| GeometryArray.COLOR_3);
		yla.setCoordinates(0, coordinates);
		for (int i = 0; i < 2; i++)
		    yla.setColor(i, axisColor);
		for (int i = 2; i < vcnt; i++)
		    yla.setColor(i, tickColor);
		Appearance appearance = new Appearance();
		appearance.setColoringAttributes(new ColoringAttributes(0.f,
			1.f, 0.f, ColoringAttributes.SHADE_FLAT));
		Shape3D shape = new Shape3D(yla, appearance);
		axesGroup.addChild(shape);
	    }

	    if (!true) { // z axis
		LinearAxis zAxis = new LinearAxis();
		zAxis.setSize((int) Math.ceil(dz));
		zAxis.setMin(zdl);
		zAxis.setMax(zdh);
		double zticks[] = zAxis.getTicks();
		int vcnt = 2 + zticks.length;
		int count = vcnt * 3;
		double coordinates[] = new double[count];
		coordinates[0] = xdl; // 0;
		coordinates[1] = ydl; // 0;
		coordinates[2] = zdl;
		coordinates[3] = xdl; // 0;
		coordinates[4] = ydl; // 0;
		coordinates[5] = zdh;
		int ti = 0;
		for (int i = 6; i < count; i += 6) { // tick marks
		    coordinates[i] = xdl; // 0;
		    coordinates[i + 1] = ydl; // 0;
		    coordinates[i + 2] = zdl + zticks[ti];
		    coordinates[i + 3] = xdl + 1; // 1;
		    coordinates[i + 4] = ydl; // 0;
		    coordinates[i + 5] = zdl + zticks[ti];
		    ti += 2;
		}
		LineArray zla = new LineArray(vcnt, GeometryArray.COORDINATES
			| GeometryArray.COLOR_3);
		zla.setCoordinates(0, coordinates);
		for (int i = 0; i < 2; i++)
		    zla.setColor(i, axisColor);
		for (int i = 2; i < vcnt; i++)
		    zla.setColor(i, tickColor);
		Appearance appearance = new Appearance();
		appearance.setColoringAttributes(new ColoringAttributes(0.f,
			0.f, 1.f, ColoringAttributes.SHADE_FLAT));
		Shape3D shape = new Shape3D(zla, appearance);
		axesGroup.addChild(shape);
	    }

	    // Planes
	    if (!true) {
		Vector3f normals[] = {
			// back
			new Vector3f(0f, 0f, -1f),
			new Vector3f(0f, 0f, -1f),
			new Vector3f(0f, 0f, -1f),
			new Vector3f(0f, 0f, -1f),
			// front
			new Vector3f(0f, 0f, 1f),
			new Vector3f(0f, 0f, 1f),
			new Vector3f(0f, 0f, 1f),
			new Vector3f(0f, 0f, 1f),
			// top
			new Vector3f(0f, -1f, 0f),
			new Vector3f(0f, -1f, 0f),
			new Vector3f(0f, -1f, 0f),
			new Vector3f(0f, -1f, 0f),
			// bottom
			new Vector3f(0f, 1f, 0f), new Vector3f(0f, 1f, 0f),
			new Vector3f(0f, 1f, 0f),
			new Vector3f(0f, 1f, 0f),
			// left
			new Vector3f(1f, 0f, 0f), new Vector3f(1f, 0f, 0f),
			new Vector3f(1f, 0f, 0f), new Vector3f(1f, 0f, 0f),
			// right
			new Vector3f(-1f, 0f, 0f), new Vector3f(-1f, 0f, 0f),
			new Vector3f(-1f, 0f, 0f), new Vector3f(-1f, 0f, 0f) };

		Point3d coords[] = {
			// back
			new Point3d(xdl, ydl, zdl),
			new Point3d(xdh, ydl, zdl),
			new Point3d(xdh, ydh, zdl),
			new Point3d(xdl, ydh, zdl),
			// front
			new Point3d(xdh, ydl, zdh),
			new Point3d(xdl, ydl, zdh),
			new Point3d(xdl, ydh, zdh),
			new Point3d(xdh, ydh, zdh),
			// top
			new Point3d(xdl, ydh, zdl),
			new Point3d(xdh, ydh, zdl),
			new Point3d(xdh, ydh, zdh),
			new Point3d(xdl, ydh, zdh),
			// bottom
			new Point3d(xdl, ydl, zdh), new Point3d(xdh, ydl, zdh),
			new Point3d(xdh, ydl, zdl),
			new Point3d(xdl, ydl, zdl),
			// left
			new Point3d(xdl, ydh, zdl), new Point3d(xdl, ydh, zdh),
			new Point3d(xdl, ydl, zdh), new Point3d(xdl, ydl, zdl),
			// right
			new Point3d(xdh, ydl, zdl), new Point3d(xdh, ydl, zdh),
			new Point3d(xdh, ydh, zdh), new Point3d(xdh, ydh, zdl) };
		float colors[] = {
		// back
		// front
		// top
		// bottom
		// left
		// right
		};
		QuadArray qa = new QuadArray(coords.length,
			GeometryArray.COORDINATES | GeometryArray.NORMALS);
		qa.setCoordinates(0, coords);
		qa.setNormals(0, normals);
		Appearance appearance = new Appearance();
		appearance
			.setTransparencyAttributes(new TransparencyAttributes(
				TransparencyAttributes.FASTEST, .99f));
		Shape3D shape = new Shape3D(qa, appearance);
		axesGroup.addChild(shape);
	    }
	    sceneGroup.addChild(axesGroup);
	}
    }

    @Override
    public void showAxes(boolean show) {
	super.showAxes(show);
	if (show) {
	    setAxes();
	} else {
	    if (axesGroup != null) {
		axesGroup.detach();
	    }
	}
    }

    @Override
    public void setViewAction(int viewAction) {
	super.setViewAction(viewAction);
	switch (viewAction) {
	case NAVIGATE:
	    for (Enumeration e = dataMapName.elements(); e.hasMoreElements();) {
		((DataMapJ3D) e.nextElement()).doSelection(null, setOperator);
	    }
	    doNavigationBehavior(true);
	    break;
	case SELECT_RECT:
	    for (Enumeration e = dataMapName.elements(); e.hasMoreElements();) {
		((DataMapJ3D) e.nextElement()).doSelection(canvas, setOperator);
	    }
	    doNavigationBehavior(false);
	    break;
	}
    }

    /**
     * Creates the geometry representing the alignment result.
     */
    public void setData() {
	dataMaps = new Switch(Switch.CHILD_MASK);
	dataMaps.setCapability(BranchGroup.ALLOW_DETACH);
	dataMaps.setCapability(Switch.ALLOW_SWITCH_READ);
	dataMaps.setCapability(Switch.ALLOW_SWITCH_WRITE);
	dataMaps.setCapability(Group.ALLOW_CHILDREN_READ);
	dataMaps.setCapability(Group.ALLOW_CHILDREN_WRITE);
	dataMaps.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	dataGroup = new BranchGroup();
	dataGroup.setCapability(BranchGroup.ALLOW_DETACH);
	dataGroup.setCapability(Node.ALLOW_BOUNDS_READ);
	dataGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	dataGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	dataGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	dataGroup.addChild(dataMaps);
	sceneGroup.addChild(dataGroup);
    }

    @Override
    public void grabImage(Observer observer) {
	canvas.captureImage(observer);
    }

    /**
     * Get the component displaying the table data in this view.
     * 
     * @return the component displaying the table data.
     */
    @Override
    public Component getCanvas() {
	return canvas;
    }
}
