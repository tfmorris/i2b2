/*
 * @(#) $RCSfile: IndexMap.java,v $ $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * Provides a mapping between two sets of indices.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface IndexMap {
    /**
     * Return true if this is a one to one mapping.
     * 
     * @return true if this is a one to one mapping, else efalse.
     */
    public boolean isOneToOne();

    /**
     * Return the source index for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source index mapped from the destination index.
     */
    public int getSrc(int dstIndex);

    /**
     * Return the destination index for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination index mapped from the source index.
     */
    public int getDst(int srcIndex);

    /**
     * Return the source indices for the given destination index.
     * 
     * @param dstIndex
     *            the index into the destination list.
     * @return the source indices mapped from the destination index.
     */
    public int[] getSrcs(int dstIndex);

    /**
     * Return the destination indices for the given source index.
     * 
     * @param srcIndex
     *            the index into the source list.
     * @return the destination indices mapped from the source index.
     */
    public int[] getDsts(int srcIndex);

    /**
     * Return the size of the source list.
     * 
     * @return the size of the source list.
     */
    public int getSrcSize();

    /**
     * Return the size of the destination list.
     * 
     * @return the size of the destination list.
     */
    public int getDstSize();
}
