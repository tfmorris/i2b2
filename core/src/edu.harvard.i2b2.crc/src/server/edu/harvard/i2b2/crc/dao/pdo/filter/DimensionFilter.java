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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;


/**
 * Class builds "from" and "where" clause of pdo query
 * based on given provider filter
 * $Id: DimensionFilter.java,v 1.1 2007/10/01 19:23:33 rk903 Exp $
 * @author rkuttan
 */
public class DimensionFilter {
    private ItemType item = null;
    private String dimensionColumnName = null; 
    private String factTableColumn = null;

    /**
     * Parameter constructor
     * @param filterListType
     */
    public DimensionFilter(ItemType item) {
        this.item = item;
       
        
    }

    /**
      * Function generates "from" clause of PDO query, by 
      * iterating filter list 
      * @return sql string
      */
    public String getFromSqlString() throws I2B2Exception {
        String conceptFromString = "";

        if (item != null) {
            conceptFromString = " ( ";

            int i = 0;

            
                String conceptPathValue = null;
                String conceptPathFilterName = null;
                 
                String dimCode = item.getDimDimcode();
                	
                
                if (dimCode != null) {
                    dimCode = JDBCUtil.escapeSingleQuote(dimCode);
                }

                conceptFromString += ("SELECT " +  item.getFacttablecolumn() +   
                " FROM "+  item.getDimTablename() + " WHERE " + item.getDimColumnname() + " LIKE " +
                "'" + dimCode + "%'\n");
            
            //check if it is exactly one concept, then add group by clause, other wise union will take care of 
            //removing duplicate concept_cd
            conceptFromString += " group by " + item.getFacttablecolumn() ;
            conceptFromString += "    ) dimension \n";
        }

        return conceptFromString;
    }

    
}
