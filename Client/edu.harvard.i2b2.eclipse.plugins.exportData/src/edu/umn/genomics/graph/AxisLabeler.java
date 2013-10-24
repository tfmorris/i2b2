/*
 * @(#) $RCSfile: AxisLabeler.java,v $ $Revision: 1.3 $ $Date: 2008/10/17 19:00:57 $ $Name: RELEASE_1_3_1_0001b $
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

/**
 * An AxisLabeler provides a formatted label for positions along an axis.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/17 19:00:57 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public interface AxisLabeler {
    /**
     * Return a label for the given value along an axis.
     * 
     * @param value
     *            the value on the axis.
     * @return a formatted label to display for the given value.
     */
    public String getLabel(double value);
}