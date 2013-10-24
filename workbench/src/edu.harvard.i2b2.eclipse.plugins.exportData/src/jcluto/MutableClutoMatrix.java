/*
 * @(#) $RCSfile: MutableClutoMatrix.java,v $ $Revision: 1.3 $ $Date: 2008/11/24 22:06:46 $ $Name: RELEASE_1_3_1_0001b $
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

package jcluto;

/**
 * MutableClutoMatrix represents a Cluto Matrix that allows values to be set.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/24 22:06:46 $ $Name: RELEASE_1_3_1_0001b $
 * @see ClutoMatrix
 */
public interface MutableClutoMatrix extends ClutoMatrix {

    /**
     * Sets whether this matrix represents a the edge weights of a connected
     * graph.
     * 
     * @param isGraph
     *            whether this matrix represents a graph
     */
    public void setIsGraph(boolean isGraph);

    /**
     * Set the value of a cell in the matrix. This may change the matrix from a
     * sparse to dense matrix or vice versa.
     * 
     * @param value
     *            The value for the matrix cell.
     * @param rowIndex
     *            the row index of the matrix cell.
     * @param columnIndex
     *            the column index of the matrix cell.
     */
    public void setValue(float value, int rowIndex, int columnIndex);
}
