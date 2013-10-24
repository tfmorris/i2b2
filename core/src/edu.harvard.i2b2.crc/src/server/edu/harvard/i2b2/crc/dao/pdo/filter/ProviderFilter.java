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
import edu.harvard.i2b2.crc.datavo.pdo.query.ObserverListType;


/**
 * Class builds "from" and "where" clause of pdo query
 * based on given provider filter
 * $Id: ProviderFilter.java,v 1.5 2007/10/01 19:23:33 rk903 Exp $
 * @author rkuttan
 */
public class ProviderFilter {
    private FilterListType filterListType = null;

    public ProviderFilter(FilterListType filterListType) {
        this.filterListType = filterListType;
    }

    /**
     * Function generates "from" clause of PDO query, by 
     * iterating filter list 
     * @return string
     */
    /*
    public String getFromSqlString() {
        String providerFromString = "";

        if ((filterListType.getObserverList() != null) &&
                (filterListType.getObserverList().getObserverPath() != null)) {
            providerFromString = " ( ";

            int i = 0;

            for (ObserverListType.ObserverPath providerPath : filterListType.getObserverList()
                                                                            .getObserverPath()) {
                if (i != 0) {
                    providerFromString += " UNION ";
                } else {
                    i = 1;
                }

                String providerPathValue = "";

                if (providerPath != null) {
                    providerPathValue = JDBCUtil.escapeSingleQuote(providerPath.getValue());
                }
                
                String providerPathFilterName = "";

                if (providerPath.getFilterName() != null) {
                    providerPathFilterName = JDBCUtil.escapeSingleQuote(providerPath.getFilterName());
                }

                

                providerFromString += ("SELECT PROVIDER_ID, '" +
                providerPathValue +
                "' c_path, '" + providerPathFilterName + "' name_char FROM PROVIDER_DIMENSION WHERE PROVIDER_PATH LIKE " +
                "'" + providerPathValue + "%'\n");
            }
            //check if it is exactly one concept, then add group by clause, other wise union will take care of 
            //removing duplicate provider_id
            if (filterListType.getObserverList().getObserverPath().size() == 1) { 
            	providerFromString += " group by PROVIDER_ID ";
            }

            providerFromString += "    ) provider\n";
        }

        return providerFromString;
    }*/

    /**
     * Function returns "where" clause of PDO query, 
     * by iterating filter list
     * @return
     */
    /*
    public String getWhereString() {
        String providerJoinString = "";

        if ((filterListType.getObserverList() != null) &&
                (filterListType.getObserverList().getObserverPath() != null)) {
            providerJoinString = " obs.provider_id IN ( \n";

            int i = 0;

            for (ObserverListType.ObserverPath providerPath : filterListType.getObserverList()
                                                                            .getObserverPath()) {
                if (i != 0) {
                    providerJoinString += " UNION ";
                } else {
                    i = 1;
                }

                String providerPathValue = "";

                if (providerPath != null) {
                    providerPathValue = JDBCUtil.escapeSingleQuote(providerPath.getValue());
                }

                providerJoinString += ("SELECT provider_id FROM PROVIDER_DIMENSION WHERE PROVIDER_PATH LIKE " +
                "'" + providerPathValue + "%'\n");
            }

            providerJoinString += "    ) \n";
        }

        return providerJoinString;
    }*/
}
