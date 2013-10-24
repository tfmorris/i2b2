/*
 * @(#) $RCSfile: GlyphJ3D.java,v $ $Revision: 1.4 $ $Date: 2008/11/07 21:03:30 $ $Name: RELEASE_1_3_1_0001b $
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
import java.util.BitSet;
import java.util.Vector;
import java.awt.Color;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import edu.umn.genomics.table.dv.*;

/**
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/11/07 21:03:30 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class GlyphJ3D extends BranchGroup implements Serializable, Glyph {
    /** map names to data representations. */
    private static Vector dataReps = new Vector();
    static {
	dataReps.add("sphere");
	dataReps.add("cube");
	dataReps.add("cylinder");
	dataReps.add("cone");
    }

    static Sphere spherePrim = null;
    static Box boxPrim = null;
    static Cylinder cylinderPrim = null;
    static Cone conePrim = null;

    private static int SHAPEBIT = 0;
    private static int LABELBIT = 1;
    Transform3D transform = new Transform3D();
    TransformGroup transformGroup = new TransformGroup(transform);
    BitSet childmask = new BitSet(2);
    Switch rep = new Switch(Switch.CHILD_MASK, childmask);
    BranchGroup shapeBG;
    BranchGroup labelBG;
    Appearance appearance = new Appearance();
    Material material = new Material();
    Primitive primitive;
    // Shape3D shape = new Shape3D();
    float radius = .2f;
    float height = .2f;

    GlyphJ3D() {
	initGlyph3D();
	Primitive defaultPrimitive = new Sphere(radius,
		Primitive.GENERATE_NORMALS | Primitive.ENABLE_APPEARANCE_MODIFY
			| Primitive.ENABLE_GEOMETRY_PICKING, appearance);
	setPrimitive(defaultPrimitive);
    }

    GlyphJ3D(Primitive primitive) {
	initGlyph3D();
	setPrimitive(primitive);
    }

    GlyphJ3D(String glyphType) {
	initGlyph3D();
	setPrimitive(glyphType);
    }

    public static Vector getGlyphTypes() {
	return dataReps;
    }

    private void initGlyph3D() {

	rep.setCapability(Switch.ALLOW_SWITCH_READ);
	rep.setCapability(Switch.ALLOW_SWITCH_WRITE);
	rep.setCapability(Group.ALLOW_CHILDREN_READ);
	rep.setCapability(Group.ALLOW_CHILDREN_WRITE);
	rep.setCapability(Group.ALLOW_CHILDREN_EXTEND);

	childmask.set(SHAPEBIT);
	rep.setChildMask(childmask);

	this.setCapability(BranchGroup.ALLOW_DETACH);

	transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

	transformGroup.setTransform(transform);

	transformGroup.addChild(rep);
	this.addChild(transformGroup);

	appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
	appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
	material.setCapability(Material.ALLOW_COMPONENT_WRITE);
	material.setCapability(Material.ALLOW_COMPONENT_READ);
	appearance.setMaterial(material);

	shapeBG = new BranchGroup();
	labelBG = new BranchGroup();
	shapeBG.setCapability(BranchGroup.ALLOW_DETACH);
	labelBG.setCapability(BranchGroup.ALLOW_DETACH);
	rep.addChild(shapeBG);
	rep.addChild(labelBG);
    }

    public void setLabel(BranchGroup label) {
	if (rep.numChildren() < 2)
	    rep.addChild(label);
	else
	    rep.setChild(label, LABELBIT);
    }

    public void setPrimitive(String primName) {
	Primitive prim = null;
	switch (dataReps.indexOf(primName)) {
	case 0: // sphere
	    prim = new Sphere(radius, Primitive.GENERATE_NORMALS
		    | Primitive.ENABLE_APPEARANCE_MODIFY
		    | Primitive.ENABLE_GEOMETRY_PICKING, appearance);
	    break;
	case 1: // cube
	    prim = new Box(radius, radius, radius, Primitive.GENERATE_NORMALS
		    | Primitive.ENABLE_APPEARANCE_MODIFY
		    | Primitive.ENABLE_GEOMETRY_PICKING, appearance);
	    break;
	case 2: // cylynder
	    prim = new Cylinder(radius, 2 * height, Primitive.GENERATE_NORMALS
		    | Primitive.ENABLE_APPEARANCE_MODIFY
		    | Primitive.ENABLE_GEOMETRY_PICKING, appearance);
	    break;
	case 3: // cone
	    prim = new Cone(radius, 2 * height, Primitive.GENERATE_NORMALS
		    | Primitive.ENABLE_APPEARANCE_MODIFY
		    | Primitive.ENABLE_GEOMETRY_PICKING, appearance);
	    break;
	default:
	}
	if (prim != null) {
	    setPrimitive(prim);
	}
    }

    public void setPrimitive(Primitive primitive) {
	if (primitive != null) {
	    this.primitive = primitive;
	    primitive.setCapability(Primitive.ENABLE_APPEARANCE_MODIFY
		    | Primitive.ENABLE_GEOMETRY_PICKING);
	    primitive.setAppearance(appearance);
	    shapeBG = new BranchGroup();
	    shapeBG.setCapability(BranchGroup.ALLOW_DETACH);
	    shapeBG.addChild(primitive);
	    rep.setChild(shapeBG, SHAPEBIT);
	}
    }

    public void setColor(Color color) {
	try {
	    Color3f c = new Color3f(color.getRGBColorComponents(null));
	    Appearance a = primitive.getAppearance();
	    if (a == null) {
		a = new Appearance();
		a.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		a.setCapability(Appearance.ALLOW_MATERIAL_READ);
		primitive.setAppearance(a);
	    }
	    Material m = a.getMaterial();
	    if (m == null) {
		m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setCapability(Material.ALLOW_COMPONENT_READ);
		a.setMaterial(m);
	    }
	    m.setDiffuseColor(c);
	    // m.setEmissiveColor(c);
	} catch (Exception e) {
	    System.err.println("GlyphJ3D.setColor() " + e);
	}
    }

    public void showLabel(boolean show) {
	if (show)
	    childmask.set(LABELBIT);
	else
	    childmask.clear(LABELBIT);
	rep.setChildMask(childmask);
    }

    public void showShape(boolean show) {
	if (show)
	    childmask.set(SHAPEBIT);
	else
	    childmask.clear(SHAPEBIT);
	rep.setChildMask(childmask);
    }

    public void setTranslation(double translation[]) {
	if (translation == null)
	    return;
	Vector3d trans = new Vector3d();
	transformGroup.getTransform(transform);
	transform.get(trans);
	switch (translation.length) {
	default:
	case 3:
	    trans.z = translation[2];
	case 2:
	    trans.y = translation[1];
	case 1:
	    trans.x = translation[0];
	case 0:
	    break;
	}
	transform.setTranslation(trans);
	transformGroup.setTransform(transform);
    }

    public void setScale(double scale) {
	transformGroup.getTransform(transform);
	if (scale == 0.) // This causes an exception
	    scale = .000000001; // An arbitarily small number
	transform.setScale(scale);
	try {
	    transformGroup.setTransform(transform);
	} catch (Exception e) {
	    System.err.println("setScale " + e);
	}
    }

    public void scale(double scale) {
	transformGroup.getTransform(transform);
	Vector3d scaleVec = new Vector3d();
	transform.getScale(scaleVec);
	if (scale == 0.) // This causes an exception
	    scale = .000000001; // An arbitarily small number
	scaleVec.scale(scale);
	transform.setScale(scaleVec);
	try {
	    transformGroup.setTransform(transform);
	} catch (Exception e) {
	    System.err.println("setScale " + e);
	}
    }

}
