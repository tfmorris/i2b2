/*
 * @(#) $RCSfile: DataMapJ3D.java,v $ $Revision: 1.4 $ $Date: 2008/11/05 19:32:04 $ $Name: RELEASE_1_3_1_0001b $
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
import java.math.*;
import java.util.Enumeration;
import java.awt.Color;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.JOptionPane;

import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.picking.*;
import javax.swing.ListSelectionModel;
import edu.umn.genomics.table.dv.*;
import edu.umn.genomics.table.SetOperator;

/**
 * This class extends DataMap to maps columns from a TableModel to displayable
 * attributes in java3D. It creates a single Java3d BranchGroup that can be
 * added to a scene to represent the data values of the TableModel. It overides
 * the TableModelListener method tableChanged so that it can modify the
 * BranchGroup in response to data changes.
 * 
 * @author James E Johnson
 * @version $Revision: 1.4 $ $Date: 2008/11/05 19:32:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since dv1.0
 */
public class DataMapJ3D extends DataMap implements Serializable {
    BranchGroup dsGroup;
    Switch repSwitch = new Switch();
    int whichChild = 0;
    BranchGroup gGroup = null;
    BranchGroup pGroup = null;
    BranchGroup sGroup = null;
    BranchGroup gRep;
    BranchGroup pRep;
    Appearance pointAppearance;
    Appearance selPointAppearance;
    PointAttributes pointAttributes;
    PointAttributes selPointAttributes;
    IndexedPointArray pa = null;
    Selection selector = null;
    BranchGroup selGroup = null;

    String defaultPrimName = (String) GlyphJ3D.getGlyphTypes().elementAt(0);

    /**
     * Create a mapping object for a TableModel.
     * 
     * @param name
     *            A name to identify this data mapping.
     * @param tableModel
     *            The table to map to displayable attributes.
     */
    DataMapJ3D(String name, TableModel tableModel) {
	this(name, tableModel, null);
    }

    /**
     * Create a mapping object for a TableModel.
     * 
     * @param name
     *            A name to identify this data mapping.
     * @param tableModel
     *            The table to map to displayable attributes.
     * @param lsm
     *            The ListSelectionModel for selecting elements of the table.
     */
    DataMapJ3D(String name, TableModel tableModel, ListSelectionModel lsm) {
	// Let the base class initialize.
	super(name, tableModel, lsm);

	pRep = new BranchGroup();
	// Assign the capabilities we need.
	pRep.setCapability(BranchGroup.ALLOW_DETACH);
	pRep.setCapability(Node.ALLOW_BOUNDS_READ);
	pRep.setCapability(Group.ALLOW_CHILDREN_READ);
	pRep.setCapability(Group.ALLOW_CHILDREN_WRITE);
	pRep.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	gRep = new BranchGroup();
	// Assign the capabilities we need.
	gRep.setCapability(BranchGroup.ALLOW_DETACH);
	gRep.setCapability(Node.ALLOW_BOUNDS_READ);
	gRep.setCapability(Group.ALLOW_CHILDREN_READ);
	gRep.setCapability(Group.ALLOW_CHILDREN_WRITE);
	gRep.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	pointAppearance = new Appearance();
	pointAttributes = new PointAttributes(1.f, true);
	pointAttributes.setCapability(PointAttributes.ALLOW_SIZE_READ);
	pointAttributes.setCapability(PointAttributes.ALLOW_SIZE_WRITE);
	pointAppearance.setPointAttributes(pointAttributes);

	selPointAppearance = new Appearance();
	selPointAttributes = new PointAttributes(2.f, true);
	selPointAttributes.setCapability(PointAttributes.ALLOW_SIZE_READ);
	selPointAttributes.setCapability(PointAttributes.ALLOW_SIZE_WRITE);
	selPointAppearance.setPointAttributes(selPointAttributes);

	// Create all the descendant nodes.
	repSwitch = new Switch(whichChild);
	repSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
	repSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	repSwitch.addChild(pRep);
	repSwitch.addChild(gRep);

	// Create the BranchGroup root node.
	dsGroup = new BranchGroup();
	dsGroup.setCapability(BranchGroup.ALLOW_DETACH);
	dsGroup.setCapability(Node.ALLOW_BOUNDS_READ);
	// dsGroup.setBoundsAutoCompute(true);
	dsGroup.addChild(repSwitch);
	addAll();
    }

    /**
     * Return the BranchGroup node for this data map.
     * 
     * @return the BranchGroup node for this data map.
     */
    public BranchGroup getBranchGroup() {
	return dsGroup;
    }

    /**
     * Modify the BranchGroup when the TablModel changes.
     * 
     * @param tableModelEvent
     *            describes changes to the TableModel.
     */
    @Override
    public void tableChanged(TableModelEvent tableModelEvent) {
	if (tableModelEvent == null) {
	    return;
	}
	if (tableModelEvent.getFirstRow() == TableModelEvent.HEADER_ROW) {
	    if (gGroup != null) {
		gGroup.detach();
		gGroup = null;
	    }
	    if (pGroup != null) {
		pGroup.detach();
		pGroup = null;
	    }
	    if (sGroup != null) {
		sGroup.detach();
		sGroup = null;
	    }
	    if (((TableModel) tableModelEvent.getSource()).getRowCount() > 0) {
		autoMap();
		addAll();
	    }
	    return;
	}
	int first = tableModelEvent.getFirstRow();
	int last = tableModelEvent.getLastRow();
	// System.err.println(name + " table changed " + first + " " + last);
	switch (tableModelEvent.getType()) {
	case TableModelEvent.UPDATE:
	    if (gGroup == null && pGroup == null) {
		autoMap();
		addAll();
	    } else {
		for (int i = first; i <= last; i++) {
		    setDataPoint(i);
		}
	    }
	    break;
	case TableModelEvent.INSERT:
	    if (gGroup == null && pGroup == null) {
		autoMap();
		addAll();
	    } else {
		for (int i = first; i <= last; i++) {
		    addDataPoint(i);
		}
	    }
	    break;
	case TableModelEvent.DELETE:
	    for (int i = first; i <= last; i++) {
		deleteDataPoint(i);
	    }
	    break;
	}
    }

    /**
     * Set the scale value (size) of the glyphs representing data points.
     * 
     * @param scale
     *            the scaling factor for the data point glyphs.
     */
    public void setScale(double scale) {
	if (whichChild == 0) {
	    pointAttributes.setPointSize((float) scale);
	    selPointAttributes.setPointSize((float) scale);
	} else if (whichChild == 1) {
	    for (Enumeration e = gGroup.getAllChildren(); e.hasMoreElements();) {
		((GlyphJ3D) e.nextElement()).setScale(scale);
	    }
	}
    }

    /**
     * Scale the current size of the glyphs representing data points.
     * 
     * @param scale
     *            the relative scaling factor for the data point glyphs.
     */
    @Override
    public void scale(double scale) {
	if (whichChild == 0) {
	    pointAttributes.setPointSize((float) scale
		    * pointAttributes.getPointSize());
	    selPointAttributes.setPointSize((float) scale
		    * selPointAttributes.getPointSize());
	} else if (whichChild == 1) {
	    for (Enumeration e = gGroup.getAllChildren(); e.hasMoreElements();) {
		((GlyphJ3D) e.nextElement()).scale(scale);
	    }
	}
    }

    /**
   *
   */
    public void doSelection(Canvas3D canvas, SetOperator setOperator) {
	if (selGroup != null) {
	    selGroup.detach();
	    selGroup = null;
	    selector = null;
	}
	if (canvas != null) {
	    try {
		if (Class.forName("com.sun.j3d.utils.picking.PickTool") != null) {
		    selector = new Selection(canvas, pGroup, 0.05f, lsm,
			    setOperator);
		    BoundingSphere behaviorBounds = new BoundingSphere(
			    new Point3d(), Double.MAX_VALUE);
		    selector.setSchedulingBounds(behaviorBounds);
		    selGroup = new BranchGroup();
		    selGroup.setCapability(BranchGroup.ALLOW_DETACH);
		    selGroup.addChild(selector);
		    if (whichChild == 0)
			pGroup.addChild(selGroup);
		    else
			gGroup.addChild(selGroup);
		}
	    } catch (ClassNotFoundException cnfe) {
		JOptionPane.showMessageDialog(null,
			"Selection requires Java3D v1.2", "alert",
			JOptionPane.ERROR_MESSAGE);
	    }

	}
    }

    /**
     * Add all rows of the table to the BranchGroup.
     */
    private void addAll() {
	if (whichChild == 0) {
	    if (pGroup != null) {
		pGroup.detach();
		pGroup = null;
	    }
	    pGroup = new BranchGroup();
	    // Assign the capabilities we need.
	    pGroup.setCapability(BranchGroup.ALLOW_DETACH);
	    pGroup.setCapability(Node.ALLOW_BOUNDS_READ);
	    pGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	    pGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	    pGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	    makePoints();
	    pRep.addChild(pGroup);
	    if (selector != null) {
		doSelection(selector.getCanvas3D(), selector.getSetOperator());
	    }
	} else if (whichChild == 1) {
	    if (gGroup != null) {
		gGroup.detach();
		gGroup = null;
	    }
	    gGroup = new BranchGroup();
	    // Assign the capabilities we need.
	    gGroup.setCapability(BranchGroup.ALLOW_DETACH);
	    gGroup.setCapability(Node.ALLOW_BOUNDS_READ);
	    gGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	    gGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	    gGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	    int rows = tableModel.getRowCount();
	    for (int i = 0; i < rows; i++) {
		addDataPoint(i);
	    }
	    gRep.addChild(gGroup);
	    if (selector != null) {
		doSelection(selector.getCanvas3D(), selector.getSetOperator());
	    }
	}
    }

    /**
     * Set the Java3D attributes for a data point.
     * 
     * @param i
     *            The row index in the TableModel for this data point.
     */
    private void setDataPoint(int i) {
	if (gGroup != null && i < gGroup.numChildren()) {
	    GlyphJ3D glyph = (GlyphJ3D) gGroup.getChild(i);
	    double[] trans = getPointTranslation(i);
	    glyph.setTranslation(getPointTranslation(i));
	    if (colormapIndex >= 0 && colorMap != null) {
		try {
		    double dval = Double.NaN;
		    Object colObj = tableModel.getValueAt(i, colormapIndex);
		    if (colObj instanceof Double || colObj instanceof Integer) {
			dval = ((Double) colObj).doubleValue();
		    } else if (colObj instanceof BigDecimal) {
			dval = ((BigDecimal) colObj).doubleValue();
		    } else if (colObj instanceof BigInteger) {
			dval = ((BigInteger) colObj).doubleValue();
		    } else if (colObj instanceof String) {
			dval = Double.parseDouble((String) colObj);
		    }
		    if (dval != Double.NaN)
			glyph.setColor(colorMap.getColor(dval));
		} catch (Exception e) {
		    System.err.println(e);
		    System.err.println(tableModel.getValueAt(i, colormapIndex));
		}
	    }
	}
    }

    /**
     * Add the Java3D objects for a row in the column.
     * 
     * @param i
     *            The row index in the TableModel
     */
    private void addDataPoint(int i) {
	if (gGroup != null) {
	    GlyphJ3D glyph = new GlyphJ3D(defaultPrimName);
	    glyph.setTranslation(getPointTranslation(i));
	    if (i < gGroup.numChildren()) {
		gGroup.insertChild(glyph, i);
	    } else {
		gGroup.addChild(glyph);
	    }
	    setDataPoint(i);
	}
	if (pGroup != null) {
	    pGroup.detach();
	    pGroup = null;
	    if (whichChild == 0) {
		addAll();
	    }
	}
    }

    /**
     * Delete the Java3D objects for a row in the column.
     * 
     * @param i
     *            The row index in the TableModel
     */
    private void deleteDataPoint(int i) {
	if (gGroup != null && i < gGroup.numChildren()) {
	    gGroup.removeChild(i);
	}
	if (pGroup != null) {
	    pGroup.detach();
	    pGroup = null;
	    if (whichChild == 0) {
		addAll();
	    }
	}
    }

    /**
     * Set the selection status of a data point.
     * 
     * @param i
     *            The row index in the TableModel for this data point.
     * @param isSelected
     *            The selection status of this data point.
     */
    @Override
    public void selectDataPoint(int i, boolean isSelected) {
	if (gGroup != null && i < gGroup.numChildren()) {
	    GlyphJ3D glyph = (GlyphJ3D) gGroup.getChild(i);
	    glyph.setColor(isSelected ? selectColor : getPointColor(i));
	    // glyph.setScale(isSelected ? 2. : 1.);
	}
	if (pGroup != null) {
	    /*
	     * Color col = isSelected ? selectColor : getPointColor(i); Color3f
	     * c3f = new Color3f(col.getColorComponents((float[])null));
	     * pa.setColor(i,c3f);
	     */
	}
    }

    /**
     * Called whenever the value of the selection changes.
     * 
     * @param e
     *            the event that characterizes the change in selection.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
	if (pGroup != null) {
	    makeSelectPoints(e);
	} else {
	    int min = e.getFirstIndex();
	    int max = e.getLastIndex();
	    for (int i = min; i < max; i++) {
		selectDataPoint(i, lsm.isSelectedIndex(i));
	    }
	}
    }

    @Override
    public void setDataRepresentation(String rep) {
	if (rep.equals("points")) {
	    whichChild = 0;
	    if (pGroup == null || pGroup.numChildren() < 1) {
		addAll();
	    }
	    repSwitch.setWhichChild(whichChild);
	} else if (GlyphJ3D.getGlyphTypes().contains(rep)) {
	    whichChild = 1;
	    if (gGroup == null || gGroup.numChildren() < 1) {
		addAll();
	    }
	    repSwitch.setWhichChild(whichChild);
	    defaultPrimName = rep;
	    for (Enumeration e = gGroup.getAllChildren(); e.hasMoreElements();) {
		((GlyphJ3D) e.nextElement()).setPrimitive(defaultPrimName);
	    }
	}
    }

    private void makePoints() {
	int rows = tableModel.getRowCount();
	if (rows > 0) {
	    boolean useColor = colormapIndex >= 0 && colorMap != null;
	    int vertexFormat = GeometryArray.COORDINATES
		    | GeometryArray.COLOR_3;
	    pa = new IndexedPointArray(rows, vertexFormat, rows);
	    pa.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
	    pa.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
	    pa.setCapability(GeometryArray.ALLOW_COLOR_READ);
	    pa.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
	    pa.setCapability(GeometryArray.ALLOW_NORMAL_READ);
	    pa.setCapability(GeometryArray.ALLOW_NORMAL_WRITE);
	    pa.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
	    pa.setCapability(GeometryArray.ALLOW_TEXCOORD_WRITE);
	    pa.setCapability(GeometryArray.ALLOW_COUNT_READ);
	    pa.setCapability(GeometryArray.ALLOW_COUNT_WRITE);
	    pa.setCapability(GeometryArray.ALLOW_FORMAT_READ);
	    pa.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
	    pa.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
	    pa.setCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ);
	    pa.setCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_WRITE);
	    pa.setCapability(IndexedGeometryArray.ALLOW_COLOR_INDEX_READ);
	    pa.setCapability(IndexedGeometryArray.ALLOW_COLOR_INDEX_WRITE);
	    pa.setCapability(IndexedGeometryArray.ALLOW_NORMAL_INDEX_READ);
	    pa.setCapability(IndexedGeometryArray.ALLOW_NORMAL_INDEX_WRITE);
	    pa.setCapability(IndexedGeometryArray.ALLOW_TEXCOORD_INDEX_READ);
	    pa.setCapability(IndexedGeometryArray.ALLOW_TEXCOORD_INDEX_WRITE);
	    pa.setCapability(IndexedGeometryArray.ALLOW_NORMAL_INDEX_READ);
	    pa.setCapability(IndexedGeometryArray.ALLOW_NORMAL_INDEX_READ);
	    // float p[] = new float[rows*3];
	    // getPointTranslation(0, rows, p, 0, 3);
	    pa.setCoordinates(0, getPointTranslation(0, rows, (float[]) null,
		    0, 3));
	    pa.setColors(0, getPointColor(0, rows, (float[]) null, 0, 3));
	    for (int i = 0; i < rows; i++) {
		// pa.setCoordinates(i, getPointTranslation(i));
		pa.setCoordinateIndex(i, i);
		// Color c = getPointColor(i);
		// pa.setColor(i, c.getComponents(null));
		pa.setColorIndex(i, i);
	    }
	    Shape3D s3d = new Shape3D(pa, pointAppearance);
	    try {
		if (Class.forName("com.sun.j3d.utils.picking.PickTool") != null) {
		    PickTool.setCapabilities(s3d, PickTool.INTERSECT_FULL);
		}
	    } catch (ClassNotFoundException cnfe) {
	    }
	    pGroup.addChild(s3d);
	}
    }

    private void makeSelectPoints(ListSelectionEvent e) {
	if (e.getValueIsAdjusting()) {
	    return;
	}
	int rows = tableModel.getRowCount();
	pa.setColors(0, getPointColor(0, rows, (float[]) null, 0, 3));
    }

    private void _makeSelectPoints(ListSelectionEvent e) {
	if (e.getValueIsAdjusting()) {
	    return;
	}
	int rows = tableModel.getRowCount();
	int min = e.getFirstIndex();
	int max = e.getLastIndex();
	if (max >= rows)
	    max = rows - 1;
	rows = max - min;

	if (sGroup != null) {
	    sGroup.detach();
	    sGroup = null;
	}
	if (rows > 0) {
	    // count actual number of rows selected
	    rows = 0;
	    for (int i = min; i <= max; i++) {
		if (lsm.isSelectedIndex(i)) {
		    rows++;
		}
	    }
	    if (rows < 1)
		return;
	    boolean useColor = colormapIndex >= 0 && colorMap != null;
	    int vertexFormat = GeometryArray.COORDINATES
		    | GeometryArray.COLOR_4;
	    PointArray pa = new PointArray(rows, vertexFormat);
	    int size = 0;
	    Color c = Color.yellow;
	    for (int i = min; i <= max; i++) {
		if (lsm.isSelectedIndex(i)) {
		    pa.setCoordinates(size, getPointTranslation(i));
		    // Color c = getPointColor(i);
		    pa.setColor(size, c.getComponents(null));
		    size++;
		}
	    }
	    sGroup = new BranchGroup();
	    // Assign the capabilities we need.
	    sGroup.setCapability(BranchGroup.ALLOW_DETACH);
	    sGroup.setCapability(Node.ALLOW_BOUNDS_READ);
	    sGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	    sGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	    sGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	    sGroup.addChild(new Shape3D(pa, selPointAppearance));
	    pRep.addChild(sGroup);
	} else {
	}
    }

    /**
     * Called when the column to axis mapping has been changed.
     */
    @Override
    protected void reMapAxes() {
	addAll();
    }

    /*
     * public void setPositionSource(int column[]) {
     * super.setPositionSource(column); } public void setColorSource(int column)
     * { } public void setTransparencySource(int column) { } public void
     * setScaleSource(int column) { } public void setScaleSource(int column[]) {
     * } public void setColorMap() { } public void points() { }
     */
}
