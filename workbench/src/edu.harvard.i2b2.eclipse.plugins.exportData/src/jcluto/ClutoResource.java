/*
 * @(#) $RCSfile: ClutoResource.java,v $ $Revision: 1.2 $ $Date: 2008/11/21 17:18:38 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.*;
import java.lang.reflect.*;

/**
 * Provides a mapping of <b>CLUTO</b> parameters to names for those parameters.
 * {@link ClutoOptionsPanel} uses the names from the resource bundle as names
 * displayed as parameter choices. This mapping uses the field names as returned
 * from class reflection as the key values for mapping.
 * 
 * @author J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/11/21 17:18:38 $ $Name: RELEASE_1_3_1_0001b $
 * @see ClutoOptionsPanel
 * @see ClutoParams
 * @see JClutoWrapper
 */
public class ClutoResource {
    private static ResourceBundle rsrc = ResourceBundle
	    .getBundle("jcluto.Cluto");

    public static void setLocale(Locale locale) {
	rsrc = ResourceBundle.getBundle("jcluto.Cluto", locale);
    }

    /**
     * Return the display name for a parameter choice.
     * 
     * @param param
     *            The class field name of the parameter.
     * @return the display name for the give parameter
     */
    public static String getName(String param) {
	return rsrc.getString(param + ".name");
    }

    /**
     * Returns a short description of the given parameter choice.
     * 
     * @param param
     *            The parameter field name.
     * @return a short description for the give parameter
     */
    public static String getDescription(String param) {
	return rsrc.getString(param + ".name");
    }

    /**
     * Return the display name for a specific parameter value given the
     * parameter name and the field value of that parameter.
     * 
     * @param param
     *            The parameter name.
     * @param value
     *            The parameter value
     * @return the display name for the give parameter
     */
    public static String getParamValueName(String param, int value) {
	if (param.startsWith("CLUTO_CLUSTER_METHOD")) {
	    switch (value) {
	    case 0:
		return getName("CLUTO_VP_ClusterDirect");
	    case 1:
		return getName("CLUTO_VP_ClusterRB");
	    case 2:
		return getName("CLUTO_VA_Cluster");
	    case 3:
		return getName("CLUTO_VA_ClusterBiased");
	    case 4:
		return getName("CLUTO_VP_ClusterRBTree");
	    }
	} else if (param.startsWith("MISSING_VALUE_ESTIMATION")) {
	    switch (value) {
	    case 0:
		return getName("MISSING_VALUE_ESTIMATION_NONE");
	    case 1:
		return getName("MISSING_VALUE_ESTIMATION_KNN");
	    case 2:
		return getName("MISSING_VALUE_ESTIMATION_ROW_AVG");
	    case 3:
		return getName("MISSING_VALUE_ESTIMATION_COL_AVG");
	    case 4:
		return getName("MISSING_VALUE_ESTIMATION_ZERO");
	    }
	} else if (param.startsWith("MATRIX_TRANSFORM")) {
	    switch (value) {
	    case 0:
		return getName("MATRIX_TRANSFORM_LOG");
	    case 1:
		return getName("MATRIX_TRANSFORM_MEAN_CENTERED");
	    case 2:
		return getName("MATRIX_TRANSFORM_MEDIAN_CENTERED");
	    }
	} else { // The rest of the params are from JClutoWrapper
	    Field[] jf = JClutoWrapper.class.getFields();
	    for (int i = 0; i < jf.length; i++) {
		if (jf[i].getName().startsWith(param)) {
		    try {
			if (jf[i].getInt(null) == value) {
			    return getName(jf[i].getName());
			}
		    } catch (Exception ex) {
			System.err.println("ClutoResource " + ex);
		    }
		}
	    }
	    return param + "_" + value;
	}
	return "";
    }
}
