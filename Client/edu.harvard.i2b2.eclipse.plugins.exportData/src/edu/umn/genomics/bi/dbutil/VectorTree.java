/*
 * @(#) $RCSfile: VectorTree.java,v $ $Revision: 1.2 $ $Date: 2008/09/05 14:53:58 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.bi.dbutil;

import java.util.*;
import java.lang.ref.*;

/**
 * VectorTree provides a hierarchical tree structure of Vectors that can be used
 * for data caching. Branches in tree can be designated to be SoftReferences to
 * Vectors thus providing a cache that can be dumped when memory limits occur.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/09/05 14:53:58 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class VectorTree {
    /*
     * rowTree for branchSize = 2, size = 8 pow 2 1 0 data vector --- - - -
     * ----------- 0 0 0 -> row vector 1 -> row vector 1 0 -> row vector 1 ->
     * row vector 1 0 0 -> row vector 1 -> row vector 1 0 -> row vector 1 -> row
     * vector
     */
    static boolean canRef = false;
    static {
	try {
	    Class.forName("java.lang.ref.SoftReference");
	    canRef = true;
	} catch (ClassNotFoundException cnfe) {
	}
    }
    int minRef;
    int maxRef;
    int branchCount;
    int size = 0;
    Vector root;

    /**
     * Create VectorTree with that will use softreferences.
     */
    public VectorTree() {
	this(200, 1, 1);
    }

    /**
     * Create VectorTree with branchCount nummber of children at each node.
     * 
     * @param branchCount
     *            The number of children for each branch of the tree.
     */
    public VectorTree(int branchCount) {
	this(branchCount, 1, 1);
    }

    /**
     * Create VectorTree with branchCount nummber of children at each node, and
     * designate which levels from the leaf should be SoftReferences. This
     * affects the ganularity of volatile cache.
     * 
     * @param branchCount
     *            The number of children for each branch of the tree.
     * @param minLevelRef
     *            The lowest level from the leaf that should be a SoftReference
     * @param maxLevelRef
     *            The highest level from the leaf that should be a SoftReference
     */
    public VectorTree(int branchCount, int minLevelRef, int maxLevelRef) {
	this.branchCount = branchCount > 1 ? branchCount : 2;
	this.minRef = minLevelRef;
	this.maxRef = maxLevelRef;
    }

    /**
     * Return the depth of the tree required for the given size.
     * 
     * @param size
     *            The number of leaf entries.
     * @return The tree depth required for the size.
     */
    private int getTreeDepth(int size) {
	if (size < 2)
	    return 1;
	return Math.max(1, (int) Math.ceil(Math.log(size)
		/ Math.log(branchCount)));
    }

    /**
     * Get a node for the Tree.
     * 
     * @param v
     *            A node to be repackaged and returned.
     * @param vsize
     *            The size for the Vector of this node.
     * @param ref
     *            Whether to use a SoftReference for this node.
     * @return A Vector or a SoftReference to a Vector.
     */
    private Object getNode(Vector v, int vsize, boolean ref) {
	if (v == null)
	    v = new Vector(vsize);
	if (v.size() != vsize)
	    v.setSize(vsize);
	Object o = v;
	if (canRef && ref) {
	    o = new SoftReference(o);
	}
	return o;
    }

    /**
     * Return the Vector for this node.
     * 
     * @param o
     *            The Object representing this node, either a Vector or a
     *            SoftReference.
     * @return The Vector for this node.
     */
    private Vector getNodeVector(Object o) {
	if (o instanceof Vector) {
	    return (Vector) o;
	} else if (canRef && o instanceof SoftReference) {
	    Vector v = (Vector) ((SoftReference) o).get();
	    return v;
	}
	return null;
    }

    /**
     * Return the number of elements that can be stored in this data structure.
     * 
     * @return The number of elements that can be stored.
     */
    public int getSize() {
	return size;
    }

    /**
     * Set the number of elements that can be stored in this data structure.
     * 
     * @param size
     *            The number of elements that can be stored.
     */
    synchronized public void setSize(int size) {
	if (this.size == size)
	    return;
	int oldDepth = getTreeDepth(this.size);
	int newDepth = getTreeDepth(size);
	// Adjust hierarchy level
	if (newDepth > oldDepth) { // Move current root to a lower level branch
	    Vector oldroot = root;
	    root = (Vector) getNode(null, branchCount, false);
	    Vector v = root;
	    for (int p = newDepth - 1; p > oldDepth; p--) {
		Object o = getNode(null, branchCount, p >= minRef
			&& p <= maxRef);
		v.setElementAt(o, 0);
		v = getNodeVector(o);
	    }
	    int d = newDepth - oldDepth;
	    v.setElementAt(getNode(oldroot, branchCount, d >= minRef
		    && d <= maxRef), 0);
	} else if (newDepth < oldDepth) { // Move a branch up to root
	    Vector v = root;
	    for (int p = oldDepth; v != null && p > newDepth; p--) {
		Object o = v.elementAt(0);
		v = getNodeVector(o);
	    }
	    if (v != null) {
		root = v;
	    } else {
		root = (Vector) getNode(null, branchCount, false);
	    }
	}
	if (root == null) {
	    root = (Vector) getNode(null, branchCount, false);
	}
	// Setnew size
	this.size = size;
    }

    /**
     * Get the Vector element for the given index. The index ranges from 0 to
     * the size - 1.
     * 
     * @param index
     *            The element Vector to return.
     * @return The Vector for the given index, it will be empty if this is the
     *         first time it was accessed or if the cache had been flushed.
     */
    synchronized public Vector get(int index) {
	if (index >= size)
	    return null;
	int pow = getTreeDepth(size);
	Vector v = root;
	int idx = index;
	for (int p = pow - 1; p >= 0; p--) {
	    int div = (int) Math.pow(branchCount, p);
	    int i = idx / div;
	    Vector nv = null;
	    if (v.size() <= i) {
		v.setSize(branchCount);
	    }
	    Object o = v.elementAt(i);
	    if (getNodeVector(o) == null) {
		// if (o instanceof SoftReference) System.err.println(
		// " cache miss " + i + " for " + index);
		o = getNode(null, p > 1 ? branchCount : 0, p >= minRef
			&& p <= maxRef);
		v.setElementAt(o, i);
	    }
	    v = getNodeVector(o);
	    idx = idx % div;
	}
	return v;
    }

    private void printNode(Object o, int level, int depth) {
	if (o == null)
	    return;
	String s = "";
	for (int sp = 0; sp < level; sp++)
	    s += "\t";
	System.err.print(s);
	if (level >= depth) {
	    System.err.println("  " + o);
	    return;
	}
	if (o instanceof Vector)
	    System.err.println(" [");
	else
	    System.err.println("-[");
	Vector v = getNodeVector(o);
	if (v != null) {
	    for (int i = 0; i < v.size(); i++) {
		printNode(v.elementAt(i), level + 1, depth);
	    }
	}
	System.err.print(s);
	if (o instanceof Vector)
	    System.err.println(" ]");
	else
	    System.err.println("-]");
    }

    public void dump(int depth) {
	printNode(root, 0, depth);
    }

    public void dump() {
	dump(getTreeDepth(getSize()));
    }
}
