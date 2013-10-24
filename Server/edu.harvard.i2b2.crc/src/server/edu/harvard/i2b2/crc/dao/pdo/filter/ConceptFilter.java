/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.filter;

import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;


/**
 * Class builds "from" and "where" clause of pdo query
 * based on given provider filter
 * $Id: ConceptFilter.java,v 1.8 2007/10/01 19:23:33 rk903 Exp $
 * @author rkuttan
 */
public class ConceptFilter {
    private FilterListType filterListType = null;

    /**
     * Parameter constructor
     * @param filterListType
     */
    public ConceptFilter(FilterListType filterListType) {
        this.filterListType = filterListType;
    }

    /**
      * Function generates "from" clause of PDO query, by 
      * iterating filter list 
      * @return sql string
      */
    /*
    public String getFromSqlString() {
        String conceptFromString = "";

        if ((filterListType.getPanel() != null) &&
                (filterListType.getPanel().size() > 0)) {
            conceptFromString = " ( ";

            int i = 0;

            for (PanelType panelType  : filterListType.getPanel()) {
                if (i != 0) {
                    conceptFromString += " UNION ";
                } else {
                    i = 1;
                }

                String conceptPathValue = null;
                String conceptPathFilterName = null;
                if (panelType.getItem() != null && panelType.getItem().get(0) != null) { 
                	String itemKey  =  panelType.getItem().get(0).getItemKey();
                	if (itemKey != null) { 
                		conceptPathValue = ItemKeyUtil.getItemPath(itemKey);
                	}
                }
                if (conceptPathValue != null) {
                    conceptPathValue = JDBCUtil.escapeSingleQuote(conceptPathValue);
                    conceptPathFilterName = JDBCUtil.escapeSingleQuote(panelType.getName());
                }

                conceptFromString += ("SELECT concept_cd, '" +
                conceptPathValue + "' c_path, '" + conceptPathFilterName +
                "'  name_char FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH LIKE " +
                "'" + conceptPathValue + "%'\n");
            }
            //check if it is exactly one concept, then add group by clause, other wise union will take care of 
            //removing duplicate concept_cd
            if (filterListType.getPanel().size() == 1) { 
            	conceptFromString += " group by concept_cd ";
            }

            conceptFromString += "    ) concept\n";
        }

        return conceptFromString;
    }
    */

    /**
     * Function returns "where" clause of PDO query, 
     * by iterating filter list
     * @return String where clause
     */
    /*
    public String getWhereString() {
        String conceptJoinString = "";

        if ((filterListType.getPanel() != null) &&
                (filterListType.getPanel().get(0) != null)) {
            conceptJoinString = " obs.concept_cd IN ( \n";

            int i = 0;

            for (ConceptListType.ConceptPath conceptPath : filterListType.getConceptList()
                                                                         .getConceptPath()) {
                if (i != 0) {
                    conceptJoinString += " UNION ";
                } else {
                    i = 1;
                }

                String conceptPathValue = "";

                if (conceptPath != null) {
                    conceptPathValue = JDBCUtil.escapeSingleQuote(conceptPath.getValue());
                }

                conceptJoinString += ("SELECT CONCEPT_CD FROM CONCEPT_DIMENSION WHERE CONCEPT_PATH LIKE " +
                "'" + conceptPathValue + "%'\n");
            }

            conceptJoinString += "    ) \n";
        }

        return conceptJoinString;
    }*/
    
}
