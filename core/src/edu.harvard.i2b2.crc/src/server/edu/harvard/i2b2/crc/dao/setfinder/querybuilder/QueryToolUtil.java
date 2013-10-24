/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

import org.jdom.Element;
import org.jdom.JDOMException;

import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.stream.StreamSource;


/**
 * Main class to generate setfinder sql from  
 * query definition xml.  
 * $Id: QueryToolUtil.java,v 1.11 2007/10/18 20:04:47 rk903 Exp $
 * @author chris,rkuttan
 */
public class QueryToolUtil extends CRCDAO {
    DatabaseType dbType = DatabaseType.Oracle;
    Connection conn = null;

    //DATABASE VARIABLES
    private String FACT_TABLE = "dw_f_conc_noval";
    private String PATIENT_TABLE = "dw_dim_patient";
    private String ENCOUNTER_TABLE = "dw_dim_enct";
    private String CONCEPT_TABLE = "dw_dim_concept";
    private String PROVIDER_TABLE = "dw_dim_provider";
    private String PATIENTLISTS_TABLE = "dw_patientlists";
    private String ENCOUNTER_SHORTCUT_TABLE = "dw_dim_patient_enct";
    private String ENCOUNTER_DIM_ID = "encounter_id_e";
    private String ENCOUNTER_INOUT_COL = "inout_cd";
    private String ENCOUNTER_COMPANY_COL = "company_cd";
    private String ENCOUNTER_START_DATE = "start_date";
    private String ENCOUNTER_PATIENT_ID = "patient_id_e";
    private String CONCEPT_DIM_ID = "c_basecode";
    private String CONCEPT_DIM_PATH = "c_fullname";
    private String PROVIDER_DIM_ID = "c_basecode";
    private String PROVIDER_DIM_PATH = "c_fullname";
    private String PATIENTLISTS_DIM_ID = "patient_id_e";
    private String PATIENTLISTS_DIM_PATH = "filename";
    private String PATIENT_DIM_ID = "patient_id_e";
    private String FACT_VAL_TYPE = "valtype";
    private String FACT_TEXT_VAL = "tval";
    private String FACT_NUM_VAL = "nval";
    private String FACT_FLAG_VAL = "valueflag";
    private String FACT_CONCEPT_RANK = "principal_concept";
    private String FACT_START_DATE = "start_date";
    private String FACT_ENCOUNTER_ID = "encounter_id_e";
    private String FACT_PATIENT_ID = "patient_id_e";
    private String FACT_CONCEPT_ID = "concept_id";
    private String FACT_PROVIDER_ID = "practitioner_id";
    private String TEMP_TABLE = "#t";
    private String TEMP_TABLE_PATIENT_ID = "patient_num";
    private String TEMP_TABLE_PATIENT_DATATYPE = "varchar(100)";
    private String TEMP_TABLE_ENCOUNTER_ID = "encounter_num";
    private String TEMP_TABLE_ENCOUNTER_DATATYPE = "varchar(100)";
    private String TEMP_PANELCOUNT_DATATYPE = "tinyint";
    private String TEMP_RETURN_TABLE = "#DX";
    private String METADATA_DATABASE = "MetaData_8086";
    private String METADATA_COLUMNNAME = "c_column_name";
    private String METADATA_TABLENAME = "c_table_name";
    private String METADATA_DIMCODE = "c_dim_code";
    private String METADATA_OPERATOR = "c_operator";
    private String METADATA_FULLNAME = "c_fullname";

    public QueryToolUtil() {
        SetQueryDatabaseConstants(dbType);
    }

    public String generateSQL(Connection conn, String queryXML)
        throws I2B2DAOException {
        this.conn = conn;

        org.jdom.Document controlDoc = getDocument(queryXML);
        String dataRequested = "";
        Integer iteration = new Integer(0);
        String sql = ProcessControlFileI2B2("", controlDoc, dataRequested,
                iteration);

        return sql;
    }

    

    public org.jdom.Document getDocument(String queryXML)
        throws I2B2DAOException {
        SAXBuilder parser = new SAXBuilder();
        parser.setFeature("http://apache.org/xml/features/standard-uri-conformant",
            false);

        StringReader strReader = new StringReader(queryXML);
        StreamSource s = new StreamSource(strReader);
        org.jdom.Document controlDoc = null;

        try {
            controlDoc = parser.build(strReader);
        } catch (JDOMException e) {
            log.error("", e);
            throw new I2B2DAOException("JDOMException", e);
        } catch (IOException e) {
            log.error("IOException ", e);
            throw new I2B2DAOException("IOException", e);
        }

        //controlDoc = parser.build(s.getInputStream());
        parser = null;

        return controlDoc;
    }

    protected void SetQueryDatabaseConstants(DatabaseType dbType) {
        if (dbType == DatabaseType.SqlServer) {
            FACT_TABLE = "dw_f_conc_noval";
            PATIENT_TABLE = "dw_dim_patient";
            ENCOUNTER_TABLE = "dw_dim_enct";
            CONCEPT_TABLE = "dw_dim_concept";
            PROVIDER_TABLE = "dw_dim_provider";
            PATIENTLISTS_TABLE = "dw_patientlists";
            ENCOUNTER_SHORTCUT_TABLE = "dw_dim_patient_enct";

            ENCOUNTER_DIM_ID = "encounter_id_e";
            ENCOUNTER_INOUT_COL = "inout_cd";
            ENCOUNTER_COMPANY_COL = "company_cd";
            ENCOUNTER_START_DATE = "start_date";
            ENCOUNTER_PATIENT_ID = "patient_id_e";

            CONCEPT_DIM_ID = "c_basecode";
            CONCEPT_DIM_PATH = "c_fullname";

            PROVIDER_DIM_ID = "c_basecode";
            PROVIDER_DIM_PATH = "c_fullname";

            PATIENT_DIM_ID = "patient_id_e";

            PATIENTLISTS_DIM_ID = "patient_id_e";
            PATIENTLISTS_DIM_PATH = "filename";

            FACT_VAL_TYPE = "valtype";
            FACT_TEXT_VAL = "tval";
            FACT_NUM_VAL = "nval";
            FACT_FLAG_VAL = "valueflag";
            FACT_CONCEPT_RANK = "principal_concept";
            FACT_START_DATE = "start_date";
            FACT_ENCOUNTER_ID = "encounter_id_e";
            FACT_PATIENT_ID = "patient_id_e";
            FACT_CONCEPT_ID = "concept_id";
            FACT_PROVIDER_ID = "practitioner_id";

            TEMP_TABLE = "#t";
            TEMP_TABLE_PATIENT_ID = "patient_id_e";
            TEMP_TABLE_PATIENT_DATATYPE = "varchar(100)";
            TEMP_TABLE_ENCOUNTER_ID = "encounter_id_e";
            TEMP_TABLE_ENCOUNTER_DATATYPE = "varchar(100)";
            TEMP_PANELCOUNT_DATATYPE = "tinyint";
            TEMP_RETURN_TABLE = "#DX";

            METADATA_DATABASE = "";
            METADATA_COLUMNNAME = "c_column_name";
            METADATA_TABLENAME = "c_table_name";
            METADATA_DIMCODE = "c_dim_code";
            METADATA_OPERATOR = "c_operator";
            METADATA_FULLNAME = "c_fullname";
        } else if (dbType == DatabaseType.Oracle) {
            FACT_TABLE = "observation_fact";
            PATIENT_TABLE = "patient_dimension";
            ENCOUNTER_TABLE = "visit_dimension";
            CONCEPT_TABLE = "concept_dimension";
            PROVIDER_TABLE = "provider_dimension";
            PATIENTLISTS_TABLE = "dw_patientlists"; //not sure on this yet
            ENCOUNTER_SHORTCUT_TABLE = "visit_dimension";

            ENCOUNTER_DIM_ID = "encounter_num";
            ENCOUNTER_INOUT_COL = "inout_cd";
            ENCOUNTER_COMPANY_COL = "location_cd";
            ENCOUNTER_START_DATE = "start_date";
            ENCOUNTER_PATIENT_ID = "patient_num";

            CONCEPT_DIM_ID = "concept_cd";
            CONCEPT_DIM_PATH = "concept_path";

            PROVIDER_DIM_ID = "provider_id";
            PROVIDER_DIM_PATH = "provider_path";

            PATIENT_DIM_ID = "patient_num";

            PATIENTLISTS_DIM_ID = "patient_id_e"; //??
            PATIENTLISTS_DIM_PATH = "filename"; //??

            FACT_VAL_TYPE = "valtype_cd";
            FACT_TEXT_VAL = "tval_char";
            FACT_NUM_VAL = "nval_num";
            FACT_FLAG_VAL = "valueflag_cd";
            FACT_CONCEPT_RANK = "modifier_cd";
            FACT_START_DATE = "start_date";
            FACT_ENCOUNTER_ID = "encounter_num";
            FACT_PATIENT_ID = "patient_num";
            FACT_CONCEPT_ID = "concept_cd";
            FACT_PROVIDER_ID = "provider_id";

            TEMP_TABLE = "QUERY_GLOBAL_TEMP";
            TEMP_TABLE_PATIENT_ID = "patient_num";
            TEMP_TABLE_PATIENT_DATATYPE = "number(22,0)";
            TEMP_TABLE_ENCOUNTER_ID = "encounter_num";
            TEMP_TABLE_ENCOUNTER_DATATYPE = "number(22,0)";
            TEMP_PANELCOUNT_DATATYPE = "number(5)";
            TEMP_RETURN_TABLE = "DX";

        	//METADATA SCHEMA NAME READING FROM CONFIG FILE
			try { 
				METADATA_DATABASE = QueryProcessorUtil.getInstance().getMetaDataSchemaName();
			} catch (Exception e) {
				//DEFAULT VALUE
				METADATA_DATABASE = "MetaData";
			}
			log.debug("METADATA SCHEMA NAME "+METADATA_DATABASE + "......");
		
            //METADATA_DATABASE = "MetaData";
            //METADATA_DATABASE = "MetaData_8086";
            METADATA_COLUMNNAME = "c_columnname";
            METADATA_TABLENAME = "c_tablename";
            METADATA_DIMCODE = "c_dimcode";
            METADATA_OPERATOR = "c_operator";
            METADATA_FULLNAME = "c_fullname";
        }
    }

    protected ItemMetaData GetItemMetaData(Connection conn, String tableName,
        String itemKey) {
        ItemMetaData itemMeta = new ItemMetaData();

        if (tableName.equals(PATIENTLISTS_TABLE)) {
            itemMeta.QueryTable = tableName;
            itemMeta.QueryColumn = PATIENTLISTS_DIM_PATH;
            itemMeta.QueryOp = "=";
            itemMeta.QueryCode = itemKey;
        } else {
            String sql = "select " + METADATA_TABLENAME + ", " +
                METADATA_COLUMNNAME + ", " + METADATA_OPERATOR + ", " +
                METADATA_DIMCODE + " " + "from " + METADATA_DATABASE + "." +
                tableName + " " + "where " + METADATA_FULLNAME + " = '" +
                itemKey.replaceAll("'", "''") + "'";

            try {
                java.sql.Statement st1 = conn.createStatement();
                ResultSet rs = st1.executeQuery(sql);

                if (rs.next()) {
                    itemMeta.QueryTable = rs.getString(METADATA_TABLENAME);
                    itemMeta.QueryColumn = rs.getString(METADATA_COLUMNNAME);
                    itemMeta.QueryOp = rs.getString(METADATA_OPERATOR);
                    itemMeta.QueryCode = rs.getString(METADATA_DIMCODE);

                    if ((itemMeta.QueryOp != null) &&
                            (itemMeta.QueryOp.toUpperCase().equals("LIKE"))) {
                        itemMeta.QueryCode = itemMeta.QueryCode + "\\%";
                    }
                } else {
                    log.debug("Could not find " + itemKey);
                }

                rs.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return itemMeta;
    }

    protected long GetEstimatedSize(Connection conn, String theTableName,
        String theColumnName, String theOperator, String theData,
        long DBNumPatients) {
        long EstSize = 0;
        String sql = "";

        try {
            if (theTableName.equals(CONCEPT_TABLE)) {
                sql = "select sum(n.patient_count) n " +
                    "from rpdrconceptlookup n " + "where table_name = '" +
                    FACT_TABLE + "' " + "and column_name = '" +
                    FACT_CONCEPT_ID + "'" + "and concept_t_value in " +
                    "(select " + CONCEPT_DIM_ID + " from " + "" +
                    CONCEPT_TABLE + " c " + "where " + CONCEPT_DIM_PATH + " " +
                    theOperator + " " + theData + ")";

                java.sql.Statement st1 = conn.createStatement();
                ResultSet rs = st1.executeQuery(sql);

                if (rs.next()) {
                    EstSize = rs.getLong("n");
                }

                rs.close();
            } else if (theTableName.equals(PROVIDER_TABLE)) {
                sql = "select sum(n.patient_count) n " +
                    "from rpdrconceptlookup n " + "where table_name = '" +
                    FACT_TABLE + "' " + "and column_name = '" +
                    FACT_PROVIDER_ID + "'" + "and concept_t_value in " +
                    "(select " + PROVIDER_DIM_ID + " " + "from " +
                    PROVIDER_TABLE + " c " + "where " + PROVIDER_DIM_PATH +
                    " " + theOperator + " " + theData + ")";

                java.sql.Statement st1 = conn.createStatement();
                ResultSet rs = st1.executeQuery(sql);

                if (rs.next()) {
                    EstSize = rs.getLong("n");
                }

                rs.close();
            } else if (theTableName.equals(ENCOUNTER_TABLE)) {
                sql = "select sum(n.patient_count) n " +
                    "from rpdrconceptlookup n " + "where table_name = '" +
                    ENCOUNTER_TABLE + "' " + "and column_name = '" +
                    theColumnName + "' " + "and concept_t_value " +
                    theOperator + " " + theData;

                java.sql.Statement st1 = conn.createStatement();
                ResultSet rs = st1.executeQuery(sql);

                if (rs.next()) {
                    EstSize = rs.getLong("n");
                }

                rs.close();
            } else if (theTableName.equals(PATIENT_TABLE)) {
                if (theColumnName.equals("age_in_years_num")) {
                    sql = "select sum(n.patient_count) n " +
                        "from rpdrconceptlookup n " + "where table_name = '" +
                        PATIENT_TABLE + "' " + "and column_name = '" +
                        theColumnName + "' " + "and concept_n_value " +
                        theOperator + " " + theData;
                } else {
                    sql = "select sum(n.patient_count) n " +
                        "from rpdrconceptlookup n " + "where table_name = '" +
                        PATIENT_TABLE + "' " + "and column_name = '" +
                        theColumnName + "' " + "and concept_t_value " +
                        theOperator + " " + theData;
                }

                java.sql.Statement st1 = conn.createStatement();
                ResultSet rs = st1.executeQuery(sql);

                if (rs.next()) {
                    EstSize = rs.getLong("n");
                }

                rs.close();
            } else {
                EstSize = 1;
            }
        } catch (Exception e) {
            log.error("Unable to get Estimated Size: " + e.getMessage());
        }

        return EstSize;
    }

    /**
     * Function reads table part of full path item key and
     * finds metadata table by looking up in table_lookup table
     * @param itemKey
     * @return
     * @throws SQLException
     */
    private String getItemTableFromKey(String itemKey)
        throws SQLException, I2B2DAOException  {
    	
        String itemTableNameFromLookup = null;
        if (itemKey == null) { 
        	throw new I2B2DAOException("Item key is null");
        }
        if (itemKey.indexOf("\\\\") != 0) {
        	log.error("Item key [" + itemKey + "] is not in the correct format");
        	throw new I2B2DAOException("Item key [" + itemKey + "] is not in the correct format");
        }
        
        int end = itemKey.indexOf("\\", 3);
        String itemTableFromKey = itemKey.substring(2, end).trim();
        log.debug("ITEM TABLE FROM KEY [" + itemTableFromKey + "]");

        String lookupSql = "select c_table_name from table_lookup where c_table_cd = ?";
        log.debug("Executing sql[" + lookupSql + "]");

        PreparedStatement preparedStmt = conn.prepareStatement(lookupSql);
        preparedStmt.setString(1, itemTableFromKey);

        ResultSet resultSet = preparedStmt.executeQuery();

        if (resultSet.next()) {
            itemTableNameFromLookup = resultSet.getString("c_table_name");
        }
        
        if (itemTableNameFromLookup == null) {
        	log.error("Table name["+itemTableFromKey + "] not found in lookup table");
        	throw new I2B2DAOException("Table name["+itemTableFromKey + "] not found in lookup table");
        }

        return itemTableNameFromLookup;
        
        
        	
        
        
        
    }

    /**
     *
     * @param fullPath
     * @return
     */
    private String getItemKeyFromFullPath(String fullPath) throws I2B2DAOException {
    	 if (fullPath == null) { 
         	throw new I2B2DAOException("Item key path is null");
         }
         if (fullPath.indexOf("\\\\") != 0) {
         	log.error("Item key full path [" + fullPath + "] is not in the correct format");
         	throw new I2B2DAOException("Item key full path [" + fullPath + "] is not in the correct format");
         }
        int end = fullPath.indexOf("\\", 3);
        return fullPath.substring(end).trim();
    }

    protected String ProcessControlFileI2B2(String controlFilePath, //only needed for query in query, path to folder that contains controlfiles
        org.jdom.Document controlDoc, //main control document
        String dataRequested, //type of data requested: either "P" for patient or "PE" for patient and encounter
        Integer iteration //number of calls into ProcessControlFile, used by query in query to uniquely identify temp tables
    ) throws I2B2DAOException {
        try {
            String querySQL = "";
            String tableSuffix = "";

            if (iteration.intValue() > 0) {
                tableSuffix = iteration.toString();
            }

            boolean sameVisit = false;

           
            org.jdom.Element i2b2Xml = controlDoc.getRootElement(); //i2b2:i2b2
            org.jdom.Element bodyXml = i2b2Xml.getChild("message_body");
            List child = bodyXml.getChildren();

            org.jdom.Element querySetXml = (org.jdom.Element) child.get(1);

            org.jdom.Element controlXml = querySetXml.getChild("query_definition",
                    null);
            String qTiming = controlXml.getChildText("query_timing");

            if ((qTiming != null) && (qTiming.equals("SAME"))) {
                sameVisit = true;
            } else {
                sameVisit = false;
            }

            String theQueryDateFrom = controlXml.getChildText("query_date_from");

            if (theQueryDateFrom == null) {
                theQueryDateFrom = "";
            }

            String theQueryDateTo = controlXml.getChildText("query_date_to");

            if (theQueryDateTo == null) {
                theQueryDateTo = "";
            }

            String sSpec = controlXml.getChildText("specificity_scale");
            int specificity = 0;

            if ((sSpec != null) && (sSpec.trim().length() > 0)) {
                specificity = Integer.parseInt(sSpec);
                specificity++;
            }

            List panelList = controlXml.getChildren("panel");
            long EstSize = 0;
            long EstPanelSize = 0;
            long EstQuerySize = 1;

            //long DBNumPatients = 0;
            long DBNumPatients = 3638280;
            int i = 0;
            int j = 0;
            int origIteration = iteration.intValue();
            boolean doInvert = false;
            ArrayList t = new ArrayList();
            ArrayList p = new ArrayList();

            for (Iterator itr = panelList.iterator(); itr.hasNext();) {
                i++;

                Element panelXml = (org.jdom.Element) itr.next();

                String invertString = panelXml.getChildText("invert");

                
                if ((invertString != null) && (invertString.equals("1"))) {
                    doInvert = true;
                } else {
                    doInvert = false;
                }

                String thePanelDateFrom = panelXml.getChildText(
                        "panel_date_from");

                if (thePanelDateFrom == null) {
                    thePanelDateFrom = "";
                }

                String thePanelDateTo = panelXml.getChildText("panel_date_to");

                if (thePanelDateTo == null) {
                    thePanelDateTo = "";
                }

                String totalItemOccuranceStr = null;
                int totalItemOccurance = 0;
                totalItemOccuranceStr = panelXml.getChildText(
                        "total_item_occurrences");

                if (totalItemOccuranceStr != null) {
                    totalItemOccurance = Integer.parseInt(totalItemOccuranceStr);
                }

                List itemList = panelXml.getChildren("item");
                String sql0 = "";
                String sql1 = "";
                EstPanelSize = 0;

                for (Iterator itItem = itemList.iterator(); itItem.hasNext();) {
                    j++;

                    Element itemXml = (org.jdom.Element) itItem.next();
                    String fullItemKey = itemXml.getChildText("item_key");
                    String itemKey = getItemKeyFromFullPath(fullItemKey);
                    log.debug("ITEM KEY [" + itemKey + "]");

                    //call function to get item table  
                    String itemTable = getItemTableFromKey(fullItemKey);
                    log.debug("ITEM TABLE [" + itemTable + "]");

                    //String itemTable = itemXml.getChildText("item_table");
                    String itemClass = itemXml.getChildText("class");

                    if ((dbType == DatabaseType.SqlServer) &&
                            (itemKey.length() > 2)) {
                        //the following is a hack to get the query code to
                        //work with the c_fullname from the RPDR.  fullname in
                        //i2b2 contains the table name...in the rpdr it does not
                        int pos1 = itemKey.indexOf("\\", 2);

                        if (pos1 > 2) {
                            itemKey = itemKey.substring(pos1 + 1);
                        }
                    }

                    ItemMetaData itemMeta = GetItemMetaData(conn, itemTable,
                            itemKey);
                    String theTable = itemMeta.QueryTable;

                    if (theTable != null) {
                        theTable = theTable.toLowerCase();
                    } else {
                        theTable = "";
                    }

                    String theColumn = itemMeta.QueryColumn;

                    if (theColumn != null) {
                        theColumn.toLowerCase();
                    } else {
                        theColumn = "";
                    }

                    theColumn = TranslateColumnName(DatabaseType.SqlServer,
                            theTable, theColumn);
                    theTable = TranslateTableName(DatabaseType.SqlServer,
                            theTable);

                    String theOperator = itemMeta.QueryOp;

                    if (theOperator == null) {
                        theOperator = "";
                    }

                    String theData = itemMeta.QueryCode;

                    if (theData == null) {
                        theData = "";
                    }

                    EstSize = 0;
                    sql0 = "";
                    sql1 = "";

                    if (theOperator.toUpperCase().equals("IN")) {
                        theData = "(" + theData + ")";
                    } else {
                        theData = "'" + theData.replaceAll("'", "''") + "'";
                    }

                    String theDateFrom = "";
                    String theDateTo = "";
                    Element consDate = itemXml.getChild("constrain_by_date");

                    if (consDate != null) {
                        theDateFrom = consDate.getChildText("date_from");

                        if (theDateFrom == null) {
                            theDateFrom = "";
                        }

                        theDateTo = consDate.getChildText("date_to");

                        if (theDateTo == null) {
                            theDateTo = "";
                        }
                    }

                    if (theTable.toLowerCase().equals(CONCEPT_TABLE)) {
                        EstSize = GetEstimatedSize(conn, theTable, theColumn,
                                theOperator, theData, DBNumPatients);
                        sql0 = FACT_CONCEPT_ID + " IN (select " +
                            CONCEPT_DIM_ID + " from " + CONCEPT_TABLE +
                            " c where " + CONCEPT_DIM_PATH + " " + theOperator +
                            " " + theData + ")";

                        StringBuilder theFilter = new StringBuilder();

                        if ((theDateFrom.trim().length() > 0) &&
                                (theDateTo.trim().length() > 0)) {
                            theFilter.append(" AND " + FACT_START_DATE +
                                " between to_date('" +
                                theDateFrom.substring(0,
                                    theDateFrom.length() - 1) +
                                "','YYYY-MM-DD') AND " + "to_date('" +
                                theDateTo.substring(0, theDateTo.length() - 1) +
                                "','YYYY-MM-DD') ");
                        } else {
                            if (theDateFrom.trim().length() > 0) {
                                theFilter.append(" AND " + FACT_START_DATE +
                                    " >= to_date('" +
                                    theDateFrom.substring(0,
                                        theDateFrom.length() - 1) +
                                    "','YYYY-MM-DD')");
                            }

                            if (theDateTo.trim().length() > 0) {
                                theFilter.append(" AND " + FACT_START_DATE +
                                    " <= to_date('" +
                                    theDateTo.substring(0,
                                        theDateTo.length() - 1) +
                                    "','YYYY-MM-DD')");
                            }
                        }

                        if ((thePanelDateFrom.trim().length() > 0) &&
                                (thePanelDateTo.trim().length() > 0)) {
                            theFilter.append(" AND " + FACT_START_DATE +
                                " between to_date('" +
                                thePanelDateFrom.substring(0,
                                    thePanelDateFrom.length() - 1) +
                                "','YYYY-MM-DD') AND " + " to_date('" +
                                thePanelDateTo.substring(0,
                                    thePanelDateTo.length() - 1) +
                                "','YYYY-MM-DD')");
                        } else {
                            if (thePanelDateFrom.trim().length() > 0) {
                                theFilter.append(" AND " + FACT_START_DATE +
                                    " >= to_date('" +
                                    thePanelDateFrom.substring(0,
                                        thePanelDateFrom.length() - 1) +
                                    "','YYYY-MM-DD')");
                            }

                            if (thePanelDateTo.trim().length() > 0) {
                                theFilter.append(" AND " + FACT_START_DATE +
                                    " <= to_date('" +
                                    thePanelDateTo.substring(0,
                                        thePanelDateTo.length() - 1) +
                                    "','YYYY-MM-DD')");
                            }
                        }

                        if ((theQueryDateFrom.trim().length() > 0) &&
                                (theQueryDateTo.trim().length() > 0)) {
                            theFilter.append(" AND " + FACT_START_DATE +
                                " between to_date('" +
                                theQueryDateFrom.substring(0,
                                    theQueryDateFrom.length() - 1) +
                                "','YYYY-MM-DD') AND " + " to_date('" +
                                theQueryDateTo.substring(0,
                                    theQueryDateTo.length() - 1) +
                                "','YYYY-MM-DD')");
                        } else {
                            if (theQueryDateFrom.trim().length() > 0) {
                                theFilter.append(" AND " + FACT_START_DATE +
                                    " >= to_date('" +
                                    theQueryDateFrom.substring(0,
                                        theQueryDateFrom.length() - 1) +
                                    "','YYYY-MM-DD')");
                            }

                            if (theQueryDateTo.trim().length() > 0) {
                                theFilter.append(" AND " + FACT_START_DATE +
                                    " <= to_date('" +
                                    theQueryDateTo.substring(0,
                                        theQueryDateTo.length() - 1) +
                                    "','YYYY-MM-DD')");
                            }
                        }

                        Element consMod = itemXml.getChild(
                                "constrain_by_modifier");

                        if (consMod != null) {
                            String theModifier = itemXml.getChildText(
                                    "modifier_key");

                            if (theModifier != null) {
                                if (theModifier.equals("1")) {
                                    theFilter.append(" AND " +
                                        FACT_CONCEPT_RANK + " = '1'");
                                } else if (theModifier.equals("2")) {
                                    theFilter.append(" AND " +
                                        FACT_CONCEPT_RANK + " = '2'");
                                } else if (theModifier.equals("3")) {
                                    theFilter.append(" AND " +
                                        FACT_CONCEPT_RANK + " IN ('1','2')");
                                } else if (theModifier.equals("4")) {
                                    theFilter.append(" AND " +
                                        FACT_CONCEPT_RANK + " = '0'");
                                } else if (theModifier.equals("5")) {
                                    theFilter.append(" AND " +
                                        FACT_CONCEPT_RANK + " IN ('0','1')");
                                } else if (theModifier.equals("6")) {
                                    theFilter.append(" AND " +
                                        FACT_CONCEPT_RANK + " IN ('0','2')");
                                }
                            }
                        }

                        List constraintList = itemXml.getChildren(
                                "constrain_by_value");

                        if (constraintList != null) {
                            for (Iterator itConstraint = constraintList.iterator();
                                    itConstraint.hasNext();) {
                                Element constraintXml = (org.jdom.Element) itConstraint.next();

                                String theValueType = constraintXml.getChildText(
                                        "value_type");

                                if (theValueType == null) {
                                    theValueType = "";
                                }

                                String theValueOp = constraintXml.getChildText(
                                        "value_operator");

                                if (theValueOp == null) {
                                    theValueOp = "";
                                }

                                String theValueCons = constraintXml.getChildText(
                                        "value_constraint");

                                if (theValueCons == null) {
                                    theValueCons = "";
                                }

                                if (theValueType.equals("T")) {
                                    theFilter.append(" AND " + FACT_VAL_TYPE +
                                        " = 'T' and " + FACT_TEXT_VAL + " = '" +
                                        theValueCons.replaceAll("'", "''") +
                                        "'");
                                } else if (theValueType.equals("F")) {
                                    theFilter.append(" AND " + FACT_FLAG_VAL +
                                        " IN (" + theValueCons + ")");
                                } else if (theValueType.equals("N")) {
                                    theFilter.append(" AND " + FACT_VAL_TYPE +
                                        " = 'N'");

                                    if (theValueOp.equals("=")) {
                                        theFilter.append(" AND " +
                                            FACT_TEXT_VAL + " IN (" +
                                            theValueCons + ")");
                                    } else if (theValueOp.equals("GE")) {
                                        theFilter.append(" AND " +
                                            FACT_TEXT_VAL +
                                            " IN ('G','E') AND " +
                                            FACT_NUM_VAL + " >= '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "'");
                                    } else if (theValueOp.equals("G")) {
                                        theFilter.append(" AND ((" +
                                            FACT_TEXT_VAL + " = 'E' AND " +
                                            FACT_NUM_VAL + " > '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "')");
                                        theFilter.append(" OR (" +
                                            FACT_TEXT_VAL + " = 'G' AND " +
                                            FACT_NUM_VAL + " >= '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "'))");
                                    } else if (theValueOp.equals("E")) {
                                        theFilter.append(" AND " +
                                            FACT_TEXT_VAL + " = 'E' AND " +
                                            FACT_NUM_VAL + " = '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "'");
                                    } else if (theValueOp.equals("L")) {
                                        theFilter.append(" AND ((" +
                                            FACT_TEXT_VAL + " = 'E' AND " +
                                            FACT_NUM_VAL + " < '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "')");
                                        theFilter.append(" OR (" +
                                            FACT_TEXT_VAL + " = 'L' AND " +
                                            FACT_NUM_VAL + " <= '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "'))");
                                    } else if (theValueOp.equals("LE")) {
                                        theFilter.append(" AND " +
                                            FACT_TEXT_VAL +
                                            " IN ('L','E') AND " +
                                            FACT_NUM_VAL + " <= '" +
                                            theValueCons.replaceAll("'", "''") +
                                            "'");
                                    }
                                }
                            }
                        }

                        if (theFilter.length() > 0) {
                            if (sql0.trim().length() > 0) {
                                sql0 = "((" + sql0 + ")" +
                                    theFilter.toString() + ")";
                            } else {
                                sql0 = "(" + theFilter.toString().substring(4) +
                                    ")";
                            }
                        }
                    } else if (theTable.equals(PROVIDER_TABLE)) {
                        EstSize = GetEstimatedSize(conn, theTable, theColumn,
                                theOperator, theData, DBNumPatients);
                        sql0 = FACT_PROVIDER_ID + " IN (SELECT " +
                            PROVIDER_DIM_ID + " FROM " + PROVIDER_TABLE +
                            " c where " + PROVIDER_DIM_PATH + " " +
                            theOperator + " " + theData + ")";

                        if (theDateFrom.trim().length() > 0) {
                            sql0 = sql0 + " AND " + FACT_START_DATE +
                                " >= to_date('" +
                                theDateFrom.substring(0,
                                    theDateFrom.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (theDateTo.trim().length() > 0) {
                            sql0 = sql0 + " AND " + FACT_START_DATE +
                                " <= to_date('" +
                                theDateTo.substring(0, theDateTo.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (thePanelDateFrom.trim().length() > 0) {
                            sql0 = sql0 + " AND " + FACT_START_DATE +
                                " >= to_date('" +
                                thePanelDateFrom.substring(0,
                                    thePanelDateFrom.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (thePanelDateTo.trim().length() > 0) {
                            sql0 = sql0 + " AND " + FACT_START_DATE +
                                " <= to_date('" +
                                thePanelDateTo.substring(0,
                                    thePanelDateTo.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (theQueryDateFrom.trim().length() > 0) {
                            sql0 = sql0 + " AND " + FACT_START_DATE +
                                " >= to_date('" +
                                theQueryDateFrom.substring(0,
                                    theQueryDateFrom.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (theQueryDateTo.trim().length() > 0) {
                            sql0 = sql0 + " AND " + FACT_START_DATE +
                                " <= to_date('" +
                                theQueryDateTo.substring(0,
                                    theQueryDateTo.length() - 1) +
                                "','YYYY-MM-DD')";
                        }
                    } else if (theTable.equals(PATIENT_TABLE)) {
                        EstSize = GetEstimatedSize(conn, theTable, theColumn,
                                theOperator, theData, DBNumPatients);
                        sql0 = theColumn.toLowerCase() + " " + theOperator +
                            " " + theData;
                        sql1 = "select " + PATIENT_DIM_ID + " FROM " +
                            PATIENT_TABLE + " where (" + sql0;
                    } else if ((theTable.equals(PATIENTLISTS_TABLE)) ||
                            (itemClass.equals("POP"))) {
                        if ((theTable == null) ||
                                (theTable.trim().length() == 0)) {
                            theTable = PATIENTLISTS_TABLE;
                        }

                        EstSize = 0;
                        //GetEstimatedSize(conn, itemTable, PATIENTLISTS_DIM_ID, theOperator, theData, DBNumPatients);
                        sql0 = " " + PATIENTLISTS_DIM_PATH + " = '" + itemKey +
                            "'";
                        sql1 = "select " + PATIENTLISTS_DIM_ID + " FROM " +
                            theTable + " where (" + sql0;
                    } else if (theTable.equals(ENCOUNTER_TABLE)) {
                        EstSize = GetEstimatedSize(conn, theTable, theColumn,
                                theOperator, theData, DBNumPatients);
                        sql0 = theColumn.toLowerCase() + " " + theOperator +
                            " " + theData;

                        if (theDateFrom.trim().length() > 0) {
                            sql0 = sql0 + " AND " + ENCOUNTER_START_DATE +
                                " >= to_date('" +
                                theDateFrom.substring(0,
                                    theDateFrom.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (theDateTo.trim().length() > 0) {
                            sql0 = sql0 + " AND " + ENCOUNTER_START_DATE +
                                " <= to_date('" +
                                theDateTo.substring(0, theDateTo.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (thePanelDateFrom.trim().length() > 0) {
                            sql0 = sql0 + " AND " + ENCOUNTER_START_DATE +
                                " >= to_date('" +
                                thePanelDateFrom.substring(0,
                                    thePanelDateFrom.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (thePanelDateTo.trim().length() > 0) {
                            sql0 = sql0 + " AND " + ENCOUNTER_START_DATE +
                                " <= to_date('" +
                                thePanelDateTo.substring(0,
                                    thePanelDateTo.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (theQueryDateFrom.trim().length() > 0) {
                            sql0 = sql0 + " AND " + ENCOUNTER_START_DATE +
                                " >= to_date('" +
                                theQueryDateFrom.substring(0,
                                    theQueryDateFrom.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (theQueryDateTo.trim().length() > 0) {
                            sql0 = sql0 + " AND " + ENCOUNTER_START_DATE +
                                " <= to_date('" +
                                theQueryDateTo.substring(0,
                                    theQueryDateTo.length() - 1) +
                                "','YYYY-MM-DD')";
                        }

                        if (sameVisit) {
                            sql1 = FACT_ENCOUNTER_ID + " in (select " +
                                ENCOUNTER_DIM_ID + " FROM " + ENCOUNTER_TABLE +
                                " where " + sql0 + ")";
                        } else {
                            if (((theDateFrom.trim().length() > 0) ||
                                    (theDateTo.trim().length() > 0)) ||
                                    ((thePanelDateFrom.trim().length() > 0) ||
                                    (thePanelDateTo.trim().length() > 0)) ||
                                    ((theQueryDateFrom.trim().length() > 0) ||
                                    (theQueryDateTo.trim().length() > 0)) ||
                                    ((!theColumn.equals(ENCOUNTER_INOUT_COL)) &&
                                    (!theColumn.equals(ENCOUNTER_COMPANY_COL)))) {
                                sql1 = "select " + ENCOUNTER_PATIENT_ID +
                                    " FROM " + ENCOUNTER_TABLE + " where (" +
                                    sql0;
                            } else {
                                theTable = ENCOUNTER_SHORTCUT_TABLE;
                                sql1 = "select " + ENCOUNTER_PATIENT_ID +
                                    " FROM " + ENCOUNTER_SHORTCUT_TABLE +
                                    " where (" + sql0;
                            }
                        }
                    } else if ((itemClass.equals("FIL")) && (itemKey != null)) {
                        theTable = TEMP_TABLE + iteration.toString();
                        theData = theData.replace("'", "");

                        String docPath = controlFilePath;
                        String path = docPath.substring(0,
                                docPath.lastIndexOf("\\") + 1);

                        //String panelSql = ProcessControlFile(path + theData, path, XmlFormat.I2B2, dataRequested, iteration);
                        String panelSql = null;
                        int index = panelSql.indexOf("<|>");

                        if (index > 0) {
                            sql1 = panelSql.substring(0, index);
                        } else {
                            sql1 = panelSql;
                        }

                        if (index > 0) {
                            sql0 = panelSql.substring(index + 3);
                        } else {
                            sql0 = null;
                        }
                    }

                    EstPanelSize = EstPanelSize + EstSize;

                    if (!itItem.hasNext()) {
                        if (doInvert) {
                            EstQuerySize = EstQuerySize + (1 - EstPanelSize);

                            //EstQuerySize = EstQuerySize * (1 - EstPanelSize);
                            PanelEntry panel = new PanelEntry();
                            panel.Panel = i;
                            panel.Invert = 1;
                            panel.EstPanelSize = (1 - EstPanelSize);
                            panel.Items = 0;
                            panel.AllShort = 0;
                            panel.ForInsert = 0;
                            panel.FirstPanel = 0;
                            panel.totalItemOccurrences = totalItemOccurance;
                            p.add(panel);
                        } else {
                            EstQuerySize = EstQuerySize + EstPanelSize;

                            PanelEntry panel = new PanelEntry();
                            panel.Panel = i;
                            panel.Invert = 0;
                            panel.EstPanelSize = EstPanelSize;
                            panel.Items = 0;
                            panel.AllShort = 0;
                            panel.ForInsert = 0;
                            panel.FirstPanel = 0;
                            panel.totalItemOccurrences = totalItemOccurance;
                            p.add(panel);
                        }
                    }

                    if (sql0.trim().length() > 0) {
                        ItemEntry item = new ItemEntry();
                        item.Panel = i;
                        item.TableName = theTable;
                        item.FieldName = theColumn;
                        item.EstItemSize = EstSize;
                        item.SqlX = 0;
                        item.Sql0 = sql0;
                        item.Sql1 = sql1;
                        t.add(item);
                    }
                }
            }

            String[] panelSQL = new String[100];
            String[] panelTables = new String[100];
            String[] shortcutSQL = new String[100];
            boolean[] panelInvert = new boolean[100];
            int[] totalItemOccurance = new int[100];

            for (int ii = 0; ii < 100; ii++) {
                panelSQL[ii] = "";
                panelTables[ii] = "";
                shortcutSQL[ii] = "";
                panelInvert[ii] = false;
                totalItemOccurance[ii] = 0;
            }

            int numPanels = -1;

            //java.sql.Statement st1 = conn.createStatement();
            //ResultSet rs = st1.executeQuery(itemSql.toString());
            ResultEntry[] results = OrderPanels(p, t, sameVisit, specificity);
            String old_panel = "";
            String prevTable = "";
            EstQuerySize = 1;

            boolean newPanel = false;

            if ((results != null) && (results.length > 0)) {
                for (int r = 0; r < results.length; r++) {
                    ResultEntry re = results[r];

                    String new_panel = re.Panel.toString();

                    if (!new_panel.equals(old_panel)) {
                        newPanel = true;
                        numPanels++;
                        old_panel = new_panel;

                        if (re.Invert == 1) {
                            panelInvert[numPanels] = true;
                            EstQuerySize = Math.round(EstQuerySize * (1 -
                                    (1.0 * re.EstPanelSize)));
                        } else {
                            EstQuerySize = Math.round(EstQuerySize * (1.0 * re.EstPanelSize));
                        }
                    } else {
                        newPanel = false;
                    }

                    totalItemOccurance[numPanels] = re.totalItemOccurrences;

                    String theTable = re.TableName.toLowerCase();
                    String theColumn = re.FieldName.toLowerCase();
                    String theTable2 = theTable;

                    if ((newPanel) || (!theTable.equals(prevTable)) ||
                            (theTable.equals(CONCEPT_TABLE) ||
                            theTable.equals(PROVIDER_TABLE) ||
                            theTable.startsWith(TEMP_TABLE))) {
                        if (!panelSQL[numPanels].equals("")) {
                            panelSQL[numPanels] = panelSQL[numPanels] + ")<|>";
                        }

                        String numFactsCheck = "";

                        if (theTable.equals(CONCEPT_TABLE) ||
                                (theTable.equals(PROVIDER_TABLE))) {
                            theTable2 = FACT_TABLE;
                        }

                        String is_fact = "";

                        if (specificity > 1) {
                            if (theTable.equals(CONCEPT_TABLE)) {
                                is_fact = ", 1 is_fact";
                            } else {
                                is_fact = ", 0 is_fact";
                            }
                        }

                        if (sameVisit) {
                            if (theTable.equals(PATIENT_TABLE)) {
                                panelSQL[numPanels] = panelSQL[numPanels] +
                                    "SELECT e." + ENCOUNTER_DIM_ID + ", e." +
                                    ENCOUNTER_PATIENT_ID + " " + is_fact + " " +
                                    "FROM " + ENCOUNTER_TABLE + " e, " +
                                    PATIENT_TABLE + " p " + "WHERE e." +
                                    ENCOUNTER_PATIENT_ID + " = p." +
                                    PATIENT_DIM_ID + " AND (";

                                //p.num_facts > 0 AND (";
                            } else if ((theTable.equals(PATIENTLISTS_TABLE))) {
                                panelSQL[numPanels] = panelSQL[numPanels] +
                                    "SELECT e." + ENCOUNTER_DIM_ID + ", e." +
                                    ENCOUNTER_PATIENT_ID + " " + is_fact + " " +
                                    "FROM " + ENCOUNTER_TABLE + " e, " +
                                    PATIENTLISTS_TABLE + " p " + "WHERE e." +
                                    ENCOUNTER_PATIENT_ID + " = p." +
                                    PATIENT_DIM_ID + " AND (";

                                //p.num_facts > 0 AND (";
                            } else if ((theTable.startsWith(TEMP_TABLE))) {
                                if (re.Sql0 != null) {
                                    panelSQL[numPanels] = panelSQL[numPanels] +
                                        "(" + re.Sql0;
                                } else {
                                    panelSQL[numPanels] = panelSQL[numPanels] +
                                        " (SELECT " + FACT_ENCOUNTER_ID + ", " +
                                        FACT_PATIENT_ID + " " + is_fact + " " +
                                        "FROM " + theTable2 + " ";
                                }
                            } else {
                                panelSQL[numPanels] = panelSQL[numPanels] +
                                    "SELECT " + FACT_ENCOUNTER_ID + ", " +
                                    FACT_PATIENT_ID + " " + is_fact + " " +
                                    "FROM " + theTable2 + " WHERE " +
                                    numFactsCheck + "(";
                            }
                        } else {
                            if (theTable.equals(PATIENT_TABLE)) {
                                panelSQL[numPanels] = panelSQL[numPanels] +
                                    "SELECT " + PATIENT_DIM_ID + " " + is_fact +
                                    " " + "FROM " + theTable2 + " " + "WHERE " +
                                    numFactsCheck + "(";
                            } else if ((theTable.equals(
                                        ENCOUNTER_SHORTCUT_TABLE)) &&
                                    ((theColumn.equals(ENCOUNTER_INOUT_COL)) ||
                                    (theColumn.equals(ENCOUNTER_COMPANY_COL)))) {
                                panelSQL[numPanels] = panelSQL[numPanels] +
                                    "SELECT " + ENCOUNTER_PATIENT_ID + " " +
                                    is_fact + " " + "FROM " +
                                    ENCOUNTER_SHORTCUT_TABLE + " " + "WHERE " +
                                    numFactsCheck + "(";
                            } else if (theTable.startsWith(TEMP_TABLE)) {
                                if (re.Sql0 != null) {
                                    panelSQL[numPanels] = panelSQL[numPanels] +
                                        " (" + re.Sql0;
                                } else {
                                    panelSQL[numPanels] = panelSQL[numPanels] +
                                        " (" + "SELECT " + FACT_PATIENT_ID +
                                        " " + is_fact + " " + "FROM " +
                                        theTable2 + " ";
                                }
                            } else {
                            	String queryHint = " ";
                            	if (theTable.equals(PROVIDER_TABLE)) {
                            		System.out.println("PROVIDER TABLE");
                            		queryHint = "/*+ index(observation_fact observation_fact_pk) */";
                            	}
                            	else { 
                            		System.out.println("CONCEPT TABLE");
                            		queryHint = "/*+ index(observation_fact fact_cnpt_pat_enct_idx) */";
                            	}
                                panelSQL[numPanels] = panelSQL[numPanels] +
                                    //RAJESH CHANGE (ADDED INDEX NAME) fact_cnpt_pat_enct_idx observation_fact_pk
                                    "SELECT " +  queryHint + 
                                    FACT_PATIENT_ID + " " + is_fact + " " +
                                    "FROM " + theTable2 + " " + "WHERE " +
                                    numFactsCheck + "(";
                            }
                        }

                        if (theTable.equals(CONCEPT_TABLE)) {
                            panelTables[numPanels] = panelTables[numPanels] +
                                "1|";
                        } else if (theTable.startsWith(TEMP_TABLE)) {
                            panelTables[numPanels] = panelTables[numPanels] +
                                "2|";
                        } else {
                            panelTables[numPanels] = panelTables[numPanels] +
                                "0|";
                        }

                        prevTable = theTable;

                        if (theTable.startsWith(TEMP_TABLE)) {
                            shortcutSQL[numPanels] = shortcutSQL[numPanels] +
                                re.Sql1;
                        } else {
                            panelSQL[numPanels] = panelSQL[numPanels] + " OR " +
                                re.Sql0;
                        }
                    } else if ((theTable.equals(prevTable)) && (!newPanel)) {
                        panelSQL[numPanels] = panelSQL[numPanels] + " OR " +
                            re.Sql0;
                    } else {
                        panelSQL[numPanels] = panelSQL[numPanels] + " " +
                            re.Sql1;
                    }
                }
            }

            if (numPanels >= 0) {
                for (i = 0; i <= numPanels; i++) {
                    if (!panelSQL[i].equals("")) {
                        String pSql = panelSQL[i] + ")";
                        pSql = pSql.replaceAll("\\( OR ", "(");
                        panelSQL[i] = pSql + "\r\n";
                    }
                }

                int panelCount = 1;

                boolean firstFilter = true;

                int oldPanelCount = 0;
                int newPanelCount = 0;

                boolean continueQuery = false;

                for (i = 0; i <= numPanels; i++) {
                    continueQuery = false;

                    String[] panelItemsTable = panelTables[i].split("\\|");

                    if (i == 0) {
                        //querySQL = panelSQL[0].replaceAll("<\\|>", "\r\n" + "UNION ALL" + "\r\n");

                        //RAJESH CHANGE BEGIN
                        if (totalItemOccurance[i] == 0) {
                            querySQL = panelSQL[0].replaceAll("<\\|>",
                                    "\r\n" + "UNION ALL" + "\r\n");
                        } else {
                        	if (sameVisit) { 
                        		querySQL = panelSQL[0].replaceAll("<\\|>",
                                        " group by encounter_num,patient_num having count(*) >=" +
                                        totalItemOccurance[i] + "\r\n" +
                                        "UNION ALL" + "\r\n");
                        	}
                            else {
                            	querySQL = panelSQL[0].replaceAll("<\\|>",
                                        " group by patient_num having count(*) >=" +
                                        totalItemOccurance[i] + "\r\n" +
                                        "UNION ALL" + "\r\n");
                            	
                            }
                        	
                        }

                        //RAJESH CHANGE END	
                        if (panelInvert[i]) {
                            continueQuery = true;

                            if (sameVisit) {
                                querySQL = "INSERT INTO " + TEMP_TABLE +
                                    tableSuffix + " " + "(" +
                                    TEMP_TABLE_PATIENT_ID + ", " +
                                    TEMP_TABLE_ENCOUNTER_ID +
                                    ", panel_count) \r\n" + "SELECT " +
                                    ENCOUNTER_PATIENT_ID + ", " +
                                    ENCOUNTER_DIM_ID + ", " + panelCount +
                                    " FROM ( " + "\r\nSELECT " +
                                    ENCOUNTER_PATIENT_ID + ", " +
                                    ENCOUNTER_DIM_ID + " FROM " +
                                    ENCOUNTER_TABLE + ") t ";
                            } else {
                                querySQL = "INSERT INTO " + TEMP_TABLE +
                                    tableSuffix + " " + "\r\n" + "(" +
                                    TEMP_TABLE_PATIENT_ID +
                                    ", panel_count) \r\n" + "SELECT " +
                                    PATIENT_DIM_ID + ", " + panelCount +
                                    " FROM ( " + "\r\nSELECT " +
                                    PATIENT_DIM_ID + " FROM " + PATIENT_TABLE +
                                    ") t ";
                            }
                        } else if (specificity > 1) {
                            if (sameVisit) {
                                querySQL = "INSERT INTO " + TEMP_TABLE +
                                    tableSuffix + " " + "(" +
                                    TEMP_TABLE_PATIENT_ID + ", " +
                                    TEMP_TABLE_ENCOUNTER_ID +
                                    ", panel_count, fact_count, fact_panels) \r\n" +
                                    "SELECT " + TEMP_TABLE_ENCOUNTER_ID + ", " +
                                    TEMP_TABLE_PATIENT_ID + ", " + panelCount +
                                    ", (case min(is_fact) when 0 then 1 else sum(is_fact) end), min(is_fact) FROM ( " +
                                    "\r\n" + querySQL + ") t GROUP BY " +
                                    TEMP_TABLE_ENCOUNTER_ID + ", " +
                                    TEMP_TABLE_PATIENT_ID + " ";
                            } else {
                                querySQL = "INSERT INTO " + TEMP_TABLE +
                                    tableSuffix + " " + "(" +
                                    TEMP_TABLE_PATIENT_ID +
                                    ", panel_count, fact_count, fact_panels) \r\n" +
                                    "SELECT " + TEMP_TABLE_PATIENT_ID + ", " +
                                    panelCount +
                                    ", (case min(is_fact) when 0 then 1 else sum(is_fact) end), min(is_fact) FROM ( " +
                                    "\r\n" + querySQL + ") t GROUP BY " +
                                    TEMP_TABLE_PATIENT_ID + " ";
                            }
                        } else if (sameVisit) {
                            querySQL = "INSERT INTO " + TEMP_TABLE +
                                tableSuffix + " " + "(" +
                                TEMP_TABLE_PATIENT_ID + ", " +
                                TEMP_TABLE_ENCOUNTER_ID +
                                ", panel_count) \r\n" + "SELECT " +
                                TEMP_TABLE_ENCOUNTER_ID + ", " +
                                TEMP_TABLE_PATIENT_ID + ", " + panelCount +
                                " FROM ( " + "\r\n" + querySQL + ") t ";
                        } else {
                            String occuranceSql = " ";

                            if (totalItemOccurance[i] > 0) {
                            	if (sameVisit) { 
                            		occuranceSql = " group by encounter_num,patient_num having count(*) >=" +
                                    totalItemOccurance[i];
                            	}
                            	else { 
                            		occuranceSql = " group by patient_num having count(*) >=" +
                                    totalItemOccurance[i];	
                            	}
                                
                            }

                            querySQL = "INSERT INTO " + TEMP_TABLE +
                                tableSuffix + " " + "(" +
                                TEMP_TABLE_PATIENT_ID + ", panel_count) \r\n" +
                                "SELECT " + TEMP_TABLE_PATIENT_ID + ", " +
                                panelCount + " FROM ( " + "\r\n" + querySQL +
                                // RAJESH CHANGE BEGIN
                                occuranceSql + //RAJESH CHANGE END
                                ") t ";
                        }

                        String specCount = "";

                        if (specificity > 1) {
                            specCount = ", fact_count int, fact_panels " +
                                TEMP_PANELCOUNT_DATATYPE + "";
                        }

                        if (panelItemsTable[0].equals("2")) {
                            querySQL = shortcutSQL[0] + querySQL;
                        }

                        if (dbType == DatabaseType.SqlServer) {
                            if (sameVisit) {
                                querySQL = "CREATE TABLE " + TEMP_TABLE +
                                    tableSuffix + " (" +
                                    TEMP_TABLE_ENCOUNTER_ID + " " +
                                    TEMP_TABLE_ENCOUNTER_DATATYPE + ", " +
                                    TEMP_TABLE_PATIENT_ID + " " +
                                    TEMP_TABLE_PATIENT_DATATYPE +
                                    ", panel_count " +
                                    TEMP_PANELCOUNT_DATATYPE + "" + specCount +
                                    ") " + "\r\n\r\n" + querySQL;
                            } else {
                                querySQL = "CREATE TABLE " + TEMP_TABLE +
                                    tableSuffix + " (" + TEMP_TABLE_PATIENT_ID +
                                    " " + TEMP_TABLE_PATIENT_DATATYPE +
                                    ", panel_count " +
                                    TEMP_PANELCOUNT_DATATYPE + "" + specCount +
                                    ") " + "\r\n\r\n" + querySQL;
                            }

                            ;
                            querySQL = querySQL + "\r\n\r\n";
                        } else if (dbType == DatabaseType.Oracle) {
                            //querySQL = "begin \r\n" + querySQL + ";";
                            querySQL = querySQL + "\r\n<*>\r\n";
                        }
                    }

                    if ((i > 0) || (continueQuery)) {
                        if (panelInvert[i]) {
                            oldPanelCount = panelCount;
                            newPanelCount = 0;
                        } else {
                            oldPanelCount = panelCount;
                            newPanelCount = panelCount + 1;
                            panelCount = panelCount + 1;
                        }

                        if ((!shortcutSQL[i].equals("")) &&
                                (!panelTables[i].contains("2|"))) {
                            querySQL = querySQL +
                                "UPDATE t SET t.panel_count = " +
                                newPanelCount + " FROM " + TEMP_TABLE +
                                tableSuffix + " t " + "\r\n" + "WHERE " +
                                shortcutSQL[i];

                            if (firstFilter) {
                                firstFilter = false;
                            } else {
                                querySQL = querySQL + "WHERE t.panel_count = " +
                                    oldPanelCount + "\r\n";
                            }

                            if (dbType == DatabaseType.Oracle) {
                                querySQL = querySQL + "<*>";
                            }

                            //querySQL = querySQL + ";";
                            querySQL = querySQL + "\r\n\r\n";
                        }

                        if (!panelSQL[i].equals("")) {
                            String[] panelItemsSQL = panelSQL[i].split("<\\|>");

                            for (j = 0; j < panelItemsSQL.length; j++) {
                                if ((!shortcutSQL[i].equals("")) &&
                                        (panelItemsTable[j].equals("2"))) {
                                    querySQL = querySQL + shortcutSQL[i];
                                }

                                if (dbType == DatabaseType.SqlServer) {
                                    if ((specificity > 1) &&
                                            (panelItemsTable[j].equals("1"))) {
                                        querySQL = querySQL +
                                            "UPDATE t SET t.panel_count = " +
                                            newPanelCount +
                                            ", t.fact_count = t.fact_count * v.fact_count, t.fact_panels = t.fact_panels + v.fact_panels FROM " +
                                            TEMP_TABLE + tableSuffix +
                                            " t INNER JOIN ( " + "\r\n";

                                        if (sameVisit) {
                                            querySQL = querySQL + "SELECT " +
                                                TEMP_TABLE_PATIENT_ID + ", " +
                                                TEMP_TABLE_ENCOUNTER_ID +
                                                ", (case min(is_fact) when 0 then 1 else sum(is_fact) end) fact_count, min(is_fact) fact_panels FROM ( " +
                                                "\r\n";
                                        } else {
                                            querySQL = querySQL + "SELECT " +
                                                TEMP_TABLE_PATIENT_ID +
                                                ", (case min(is_fact) when 0 then 1 else sum(is_fact) end) fact_count, min(is_fact) fact_panels FROM ( " +
                                                "\r\n";
                                        }

                                        querySQL = querySQL + panelItemsSQL[j] +
                                            ") w GROUP BY " +
                                            TEMP_TABLE_PATIENT_ID + "";

                                        if (sameVisit) {
                                            querySQL = querySQL + "," +
                                                TEMP_TABLE_ENCOUNTER_ID + "";
                                        }

                                        querySQL = querySQL + ") v " + "\r\n" +
                                            "ON t." + TEMP_TABLE_PATIENT_ID +
                                            " = v." + TEMP_TABLE_PATIENT_ID +
                                            " ";

                                        if (sameVisit) {
                                            querySQL = querySQL + "AND t." +
                                                TEMP_TABLE_ENCOUNTER_ID +
                                                " = v." +
                                                TEMP_TABLE_ENCOUNTER_ID + " ";
                                        }
                                    } else {
                                        querySQL = querySQL +
                                            "UPDATE t SET t.panel_count = " +
                                            newPanelCount + " FROM " +
                                            TEMP_TABLE + tableSuffix +
                                            " t INNER JOIN ( " + "\r\n" +
                                            panelItemsSQL[j] + ") v " + "\r\n" +
                                            "ON t." + TEMP_TABLE_PATIENT_ID +
                                            " = v." + TEMP_TABLE_PATIENT_ID +
                                            " ";

                                        if (sameVisit) {
                                            querySQL = querySQL + "AND t." +
                                                TEMP_TABLE_ENCOUNTER_ID +
                                                " = v." +
                                                TEMP_TABLE_ENCOUNTER_ID + " ";
                                        }
                                    }

                                    if (firstFilter) {
                                        firstFilter = false;
                                    } else {
                                        querySQL = querySQL +
                                            "\r\nWHERE t.panel_count = " +
                                            oldPanelCount + "\r\n";
                                    }

                                    querySQL = querySQL + "\r\n";
                                } else if (dbType == DatabaseType.Oracle) {
                                    if ((specificity > 1) &&
                                            (panelItemsTable[j].equals("1"))) {
                                        querySQL = querySQL + "UPDATE " +
                                            TEMP_TABLE + " " +
                                            "SET (panel_count, fact_count, fact_panels) = " +
                                            "(SELECT " + newPanelCount + ", " +
                                            TEMP_TABLE +
                                            ".fact_count * v.fact_count, " +
                                            TEMP_TABLE +
                                            ".fact_panels + v.fact_panels " +
                                            "FROM (";

                                        String subQuery = "";

                                        if (sameVisit) {
                                            subQuery = subQuery + "SELECT " +
                                                TEMP_TABLE_PATIENT_ID + ", " +
                                                TEMP_TABLE_ENCOUNTER_ID +
                                                ", (case min(is_fact) when 0 then 1 else sum(is_fact) end) fact_count, min(is_fact) fact_panels FROM ( " +
                                                "\r\n";
                                        } else {
                                            subQuery = subQuery + "SELECT " +
                                                TEMP_TABLE_PATIENT_ID +
                                                ", (case min(is_fact) when 0 then 1 else sum(is_fact) end) fact_count, min(is_fact) fact_panels FROM ( " +
                                                "\r\n";
                                        }

                                        subQuery = subQuery + panelItemsSQL[j] +
                                            ") GROUP BY " +
                                            TEMP_TABLE_PATIENT_ID + "";

                                        if (sameVisit) {
                                            subQuery = subQuery + "," +
                                                TEMP_TABLE_ENCOUNTER_ID + "";
                                        }

                                        subQuery = subQuery + ") v " + "\r\n" +
                                            "WHERE " + TEMP_TABLE + "." +
                                            TEMP_TABLE_PATIENT_ID + " = v." +
                                            TEMP_TABLE_PATIENT_ID + " ";

                                        if (sameVisit) {
                                            subQuery = subQuery + "AND " +
                                                TEMP_TABLE + "." +
                                                TEMP_TABLE_ENCOUNTER_ID +
                                                " = v." +
                                                TEMP_TABLE_ENCOUNTER_ID + " ";
                                        }

                                        querySQL = querySQL + subQuery + ")" +
                                            "\r\n" +
                                            " WHERE EXISTS ( SELECT 1 FROM ( " +
                                            "\r\n" + subQuery + ")";
                                    } else {
                                        String occuranceSql = " ";

                                        if (totalItemOccurance[i] > 0) {
                                        	if (sameVisit) { 
                                        		occuranceSql = " group by encounter_num,patient_num having count(*) >=" +
                                                totalItemOccurance[i];
                                        	}
                                        	else { 
                                        		occuranceSql = " group by patient_num having count(*) >=" +
                                                totalItemOccurance[i];	
                                        	}
                                            
                                        }

                                        querySQL = querySQL + "UPDATE " +
                                            TEMP_TABLE + " SET panel_count = " +
                                            newPanelCount +
                                            " WHERE EXISTS ( SELECT 1 FROM ( " +
                                            "\r\n" + panelItemsSQL[j] +
                                            //RAJ OCCURANCE CHANGE BEGIN
                                            occuranceSql + //RAJ OCCURANCE CHANGE END
                                            ") v " + " WHERE " + TEMP_TABLE +
                                            "." + TEMP_TABLE_PATIENT_ID +
                                            " = v." + TEMP_TABLE_PATIENT_ID +
                                            " ";

                                        if (sameVisit) {
                                            querySQL = querySQL + "AND " +
                                                TEMP_TABLE + "." +
                                                TEMP_TABLE_ENCOUNTER_ID +
                                                " = v." +
                                                TEMP_TABLE_ENCOUNTER_ID + " ";
                                        }

                                        querySQL = querySQL + ")";
                                    }

                                    if (firstFilter) {
                                        firstFilter = false;
                                    } else {
                                        querySQL = querySQL +
                                            "\r\nAND panel_count = " +
                                            oldPanelCount + "\r\n";
                                    }

                                    querySQL = querySQL + "\r\n<*>";
                                }

                                //querySQL = querySQL + ";";
                                querySQL = querySQL + "\r\n";
                            }
                        }
                    }
                }

                String querySQLTemp = "SELECT 0 " + TEMP_TABLE_PATIENT_ID +
                    " WHERE 1=0";

                if (specificity > 1) {
                    if (sameVisit) {
                        querySQLTemp = "SELECT " + TEMP_TABLE_PATIENT_ID + " ";

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + "," +
                                TEMP_TABLE_ENCOUNTER_ID + " ";
                        }

                        querySQLTemp = querySQLTemp + "FROM (" + "\r\n";
                        querySQLTemp = querySQLTemp + "SELECT " +
                            TEMP_TABLE_PATIENT_ID;

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + " ," +
                                TEMP_TABLE_ENCOUNTER_ID + "";
                        }

                        querySQLTemp = querySQLTemp +
                            ", cast(sum(fact_count) as bigint) fact_count, fact_panels " +
                            "FROM " + TEMP_TABLE + tableSuffix + " " +
                            "WHERE panel_count = " + panelCount + " " +
                            "GROUP BY " + TEMP_TABLE_PATIENT_ID;

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + " ," +
                                TEMP_TABLE_ENCOUNTER_ID + "";
                        }

                        querySQLTemp = querySQLTemp + ", fact_panels" + "\r\n";
                        querySQLTemp = querySQLTemp + ") t GROUP BY " +
                            TEMP_TABLE_PATIENT_ID;

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + " ," +
                                TEMP_TABLE_ENCOUNTER_ID + "";
                        }

                        querySQLTemp = querySQLTemp +
                            " HAVING sum(fact_count * power(" + specificity +
                            "," + numPanels + "-fact_panels)) >= " +
                            (Math.pow(specificity, numPanels)) + "\r\n";
                    } else {
                        querySQLTemp = "SELECT t." + TEMP_TABLE_PATIENT_ID +
                            " ";

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + " , e." +
                                TEMP_TABLE_ENCOUNTER_ID + " ";
                        }

                        querySQLTemp = querySQLTemp + "FROM " + TEMP_TABLE +
                            tableSuffix + " t ";

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + ", " +
                                ENCOUNTER_TABLE + " e" + " ";
                        }

                        querySQLTemp = querySQLTemp + "WHERE t.panel_count = " +
                            panelCount + " AND t.fact_count >= power(" +
                            specificity + ", t.fact_panels)" + "\r\n";

                        if (dataRequested.equals("PE")) {
                            querySQLTemp = querySQLTemp + " AND e." +
                                ENCOUNTER_PATIENT_ID + " = t." +
                                TEMP_TABLE_PATIENT_ID + "\r\n";
                        }
                    }
                } else {
                    querySQLTemp = "SELECT DISTINCT t." +
                        TEMP_TABLE_PATIENT_ID + " ";

                    if (dataRequested.equals("PE")) {
                        if (sameVisit) {
                            querySQLTemp = querySQLTemp + ", t." +
                                TEMP_TABLE_ENCOUNTER_ID + " ";
                        } else {
                            querySQLTemp = querySQLTemp + ", e." +
                                TEMP_TABLE_ENCOUNTER_ID + " ";
                        }
                    }

                    querySQLTemp = querySQLTemp + "FROM " + TEMP_TABLE +
                        tableSuffix + " t ";

                    if ((dataRequested.equals("PE")) && (!sameVisit)) {
                        querySQLTemp = querySQLTemp + ", " + ENCOUNTER_TABLE +
                            " e" + " ";
                    }

                    querySQLTemp = querySQLTemp + "WHERE panel_count = " +
                        panelCount + "\r\n";

                    if ((dataRequested.equals("PE")) && (!sameVisit)) {
                        querySQLTemp = querySQLTemp + " AND e." +
                            ENCOUNTER_PATIENT_ID + " = t." +
                            TEMP_TABLE_PATIENT_ID + "\r\n";
                    }
                }

                if (origIteration == 0) {
                    if (dbType == DatabaseType.SqlServer) {
                        querySQL = querySQL + "SELECT * INTO " +
                            TEMP_RETURN_TABLE + tableSuffix + " FROM (" +
                            "\r\n" + querySQLTemp + ") q" + "\r\n";
                    } else {
                        querySQL = querySQL + "INSERT INTO " +
                            TEMP_RETURN_TABLE + tableSuffix + " ";

                        if (dataRequested.equals("PE")) {
                            querySQL = querySQL + "(" + TEMP_TABLE_PATIENT_ID +
                                ", " + TEMP_TABLE_ENCOUNTER_ID + ")";
                        } else {
                            querySQL = querySQL + "(" + TEMP_TABLE_PATIENT_ID +
                                ")";
                        }

                        querySQL = querySQL + " SELECT * FROM (" + "\r\n" +
                            querySQLTemp + ") q";

                        //querySQL = querySQL + ";\r\n\r\n";
                        //querySQL = querySQL + "\r\n<*>\r\n";
                        querySQL = querySQL + "\r\n";
                    }

                    if (dbType == DatabaseType.SqlServer) {
                        for (int it = iteration.intValue(); it >= 0; it--)
                            if (it > 0) {
                                querySQL = querySQL + "DROP TABLE " +
                                    TEMP_TABLE + (new Integer(it)).toString() +
                                    " " + "\r\n";
                            } else {
                                querySQL = querySQL + "DROP TABLE " +
                                    TEMP_TABLE + " " + "\r\n";
                            }
                    }
                }
            }

            return querySQL;
        } catch (Exception e) {
        	log.error(e.getMessage(),e);
        	throw new I2B2DAOException(e.getMessage(),e);
		}
    }

    public String TranslateTableName(DatabaseType controlFileDbType,
        String tableName) {
        if ((controlFileDbType == dbType) || (tableName == null) ||
                (tableName.trim().length() == 0)) {
            return tableName;
        }

        String newTableName = tableName.toLowerCase();

        if (controlFileDbType == DatabaseType.SqlServer) {
            if (newTableName.equals("dw_dim_concept")) {
                newTableName = CONCEPT_TABLE;
            } else if (newTableName.equals("dw_f_conc_noval")) {
                newTableName = FACT_TABLE;
            } else if (newTableName.equals("dw_dim_patient")) {
                newTableName = PATIENT_TABLE;
            } else if (newTableName.equals("dw_dim_provider")) {
                newTableName = PROVIDER_TABLE;
            } else if (newTableName.equals("dw_dim_enct")) {
                newTableName = ENCOUNTER_TABLE;
            } else if (newTableName.equals("dw_dim_patient_enct")) {
                newTableName = ENCOUNTER_SHORTCUT_TABLE;
            } else if (newTableName.equals("dw_patientlists")) {
                newTableName = PATIENTLISTS_TABLE;
            }
        } else if (controlFileDbType == DatabaseType.Oracle) {
            if (newTableName.equals("concept_dimension")) {
                newTableName = CONCEPT_TABLE;
            } else if (newTableName.equals("observation_fact")) {
                newTableName = FACT_TABLE;
            } else if (newTableName.equals("patient_dimension")) {
                newTableName = PATIENT_TABLE;
            } else if (newTableName.equals("provider_dimension")) {
                newTableName = PROVIDER_TABLE;
            } else if (newTableName.equals("visit_dimension")) {
                newTableName = ENCOUNTER_TABLE;
            } else if (newTableName.equals("dw_patientlists")) { //??
                newTableName = PATIENTLISTS_TABLE;
            }
        }

        return newTableName;
    }

    protected String TranslateColumnName(DatabaseType controlFileDbType,
        String tableName, String columnName) {
        if ((controlFileDbType == dbType) || (tableName == null) ||
                (tableName.trim().length() == 0) || (columnName == null) ||
                (columnName.trim().length() == 0)) {
            return columnName;
        }

        String newTableName = tableName.toLowerCase();
        String newColumnName = columnName.toLowerCase();

        if (controlFileDbType == DatabaseType.SqlServer) {
            if (newTableName.equals("dw_dim_concept")) {
                if (dbType == DatabaseType.Oracle) {
                    if (newColumnName.equals("c_basecode")) {
                        newColumnName = CONCEPT_DIM_ID;
                    } else if (newColumnName.equals("c_fullname")) {
                        newColumnName = CONCEPT_DIM_PATH;
                    }
                }
            } else if (newTableName.equals("dw_f_conc_noval")) {
                if (dbType == DatabaseType.Oracle) {
                    if (newColumnName.equals("encounter_id_e")) {
                        newColumnName = FACT_ENCOUNTER_ID;
                    } else if (newColumnName.equals("concept_id")) {
                        newColumnName = FACT_CONCEPT_ID;
                    } else if (newColumnName.equals("patient_id_e")) {
                        newColumnName = FACT_PATIENT_ID;
                    } else if (newColumnName.equals("start_date")) {
                        newColumnName = FACT_START_DATE;
                    } else if (newColumnName.equals("practitioner_id")) {
                        newColumnName = FACT_PROVIDER_ID;
                    } else if (newColumnName.equals("principal_concept")) {
                        newColumnName = FACT_CONCEPT_RANK;
                    } else if (newColumnName.equals("valtype")) {
                        newColumnName = FACT_VAL_TYPE;
                    } else if (newColumnName.equals("tval")) {
                        newColumnName = FACT_TEXT_VAL;
                    } else if (newColumnName.equals("nval")) {
                        newColumnName = FACT_NUM_VAL;
                    } else if (newColumnName.equals("valueflag")) {
                        newColumnName = FACT_FLAG_VAL;
                    } else if (newColumnName.equals("sourcesystem")) {
                        newColumnName = "sourcesystem_cd";
                    }
                }
            } else if (newTableName.equals("dw_dim_patient")) {
                if (dbType == DatabaseType.Oracle) {
                    if (newColumnName.equals("patient_id_e")) {
                        newColumnName = PATIENT_DIM_ID;
                    } else if (newColumnName.equals("langauge_cd")) {
                        newColumnName = "language_cd";
                    } else if (newColumnName.equals("statecityzip_cd")) {
                        newColumnName = "statecityzip_path";
                    } else if (newColumnName.equals("ss_text")) {
                        newColumnName = "patient_blob";
                    } else if (newColumnName.equals("date_of_birth")) {
                        newColumnName = "birth_date";
                    } else if (newColumnName.equals("date_of_death")) {
                        newColumnName = "death_date";
                    }
                }
            } else if (newTableName.equals("dw_dim_provider")) {
                if (dbType == DatabaseType.Oracle) {
                    if (newColumnName.equals("c_basecode")) {
                        newColumnName = PROVIDER_DIM_ID;
                    } else if (newColumnName.equals("c_fullname")) {
                        newColumnName = PROVIDER_DIM_PATH;
                    }
                }
            } else if (newTableName.equals("dw_dim_enct")) {
                if (dbType == DatabaseType.Oracle) {
                    if (newColumnName.equals("encounter_id_e")) {
                        newColumnName = ENCOUNTER_DIM_ID;
                    } else if (newColumnName.equals("inout_cd")) {
                        newColumnName = ENCOUNTER_INOUT_COL;
                    } else if (newColumnName.equals("company_cd")) {
                        newColumnName = ENCOUNTER_COMPANY_COL;
                    } else if (newColumnName.equals("start_date")) {
                        newColumnName = ENCOUNTER_START_DATE;
                    } else if (newColumnName.equals("patient_id_e")) {
                        newColumnName = ENCOUNTER_PATIENT_ID;
                    } else if (newColumnName.equals("clinicpath_cd")) {
                        newColumnName = "location_path";
                    } else if (newColumnName.equals("hl7_text")) {
                        newColumnName = "visit_blob";
                    } else if (newColumnName.equals("sourcesystem")) {
                        newColumnName = "sourcesystem_cd";
                    }
                }
            }
        } else if (controlFileDbType == DatabaseType.Oracle) {
            if (newTableName.equals("concept_dimension")) {
                if (dbType == DatabaseType.SqlServer) {
                    if (newColumnName.equals("concept_id")) {
                        newColumnName = CONCEPT_DIM_ID;
                    } else if (newColumnName.equals("concept_path")) {
                        newColumnName = CONCEPT_DIM_PATH;
                    }
                }
            } else if (newTableName.equals("dw_f_conc_noval")) {
                if (dbType == DatabaseType.SqlServer) {
                    if (newColumnName.equals("encounter_num")) {
                        newColumnName = FACT_ENCOUNTER_ID;
                    } else if (newColumnName.equals("concept_id")) {
                        newColumnName = FACT_CONCEPT_ID;
                    } else if (newColumnName.equals("patient_num")) {
                        newColumnName = FACT_PATIENT_ID;
                    } else if (newColumnName.equals("start_date")) {
                        newColumnName = FACT_START_DATE;
                    } else if (newColumnName.equals("provider_id")) {
                        newColumnName = FACT_PROVIDER_ID;
                    } else if (newColumnName.equals("modifier_cd")) {
                        newColumnName = FACT_CONCEPT_RANK;
                    } else if (newColumnName.equals("valtype_cd")) {
                        newColumnName = FACT_VAL_TYPE;
                    } else if (newColumnName.equals("tval_char")) {
                        newColumnName = FACT_TEXT_VAL;
                    } else if (newColumnName.equals("nval_num")) {
                        newColumnName = FACT_NUM_VAL;
                    } else if (newColumnName.equals("valueflag_cd")) {
                        newColumnName = FACT_FLAG_VAL;
                    } else if (newColumnName.equals("sourcesystem_cd")) {
                        newColumnName = "sourcesystem_cd";
                    }
                }
            } else if (newTableName.equals("dw_dim_patient")) {
                if (dbType == DatabaseType.SqlServer) {
                    if (newColumnName.equals("patient_num")) {
                        newColumnName = PATIENT_DIM_ID;
                    } else if (newColumnName.equals("language_cd")) {
                        newColumnName = "langauge_cd";
                    } else if (newColumnName.equals("statecityzip_path")) {
                        newColumnName = "statecityzip_cd";
                    } else if (newColumnName.equals("patient_blob")) {
                        newColumnName = "ss_text";
                    } else if (newColumnName.equals("birth_date")) {
                        newColumnName = "date_of_birth";
                    } else if (newColumnName.equals("death_date")) {
                        newColumnName = "date_of_death";
                    }
                }
            } else if (newTableName.equals("dw_dim_provider")) {
                if (dbType == DatabaseType.SqlServer) {
                    if (newColumnName.equals("provider_id")) {
                        newColumnName = PROVIDER_DIM_ID;
                    } else if (newColumnName.equals("provider_path")) {
                        newColumnName = PROVIDER_DIM_PATH;
                    }
                }
            } else if (newTableName.equals("dw_dim_enct")) {
                if (dbType == DatabaseType.SqlServer) {
                    if (newColumnName.equals("encounter_num")) {
                        newColumnName = ENCOUNTER_DIM_ID;
                    } else if (newColumnName.equals("inout_cd")) {
                        newColumnName = ENCOUNTER_INOUT_COL;
                    } else if (newColumnName.equals("location_cd")) {
                        newColumnName = ENCOUNTER_COMPANY_COL;
                    } else if (newColumnName.equals("start_date")) {
                        newColumnName = ENCOUNTER_START_DATE;
                    } else if (newColumnName.equals("patient_num")) {
                        newColumnName = ENCOUNTER_PATIENT_ID;
                    } else if (newColumnName.equals("location_path")) {
                        newColumnName = "clinicpath_cd";
                    } else if (newColumnName.equals("visit_blob")) {
                        newColumnName = "hl7_text";
                    } else if (newColumnName.equals("sourcesystem_cd")) {
                        newColumnName = "sourcesystem";
                    }
                }
            }
        }

        return newColumnName;
    }

    protected ResultEntry[] OrderPanels(ArrayList panelEntries,
        ArrayList itemEntries, boolean sameVisit, int specificity) {
        try {
            Integer firstPanel = -1;

            //first set all inverted panels to one panel
            ArrayList invertPanels = new ArrayList();
            Integer minInvert = -1;

            for (int i = 0; i < panelEntries.size(); i++) {
                PanelEntry p = (PanelEntry) panelEntries.get(i);

                if (p.Invert == 1) {
                    invertPanels.add(p.Panel);

                    if (minInvert < 0) {
                        minInvert = p.Panel;
                    } else if (minInvert > p.Panel) {
                        minInvert = p.Panel;
                    }
                }
            }

            /*if (minInvert>=0)
            {
                    for (int i=0; i<invertPanels.size(); i++)
                            ((PanelEntry) panelEntries.get((Integer) invertPanels.get(i))).Panel = minInvert;
            }*/

            //now, get the distinct items and put them into v hashtable
            Hashtable v = new Hashtable();

            for (int i = 0; i < itemEntries.size(); i++) {
                ItemEntry t = (ItemEntry) itemEntries.get(i);

                if ((minInvert >= 0) && (invertPanels.contains(t.Panel)) &&
                        (t.Panel != minInvert)) {
                    t.Panel = minInvert;
                }

                if (!v.containsKey(t.Panel)) {
                    ArrayList vItems = new ArrayList();
                    vItems.add(t);
                    v.put(t.Panel, vItems);
                } else {
                    ArrayList vItems = (ArrayList) v.get(t.Panel);

                    if (!vItems.contains(t)) {
                        vItems.add(t);
                    }
                }
            }

            //update panelEntries with the count of items from v
            for (int p = 0; p < panelEntries.size(); p++) {
                PanelEntry panel = (PanelEntry) panelEntries.get(p);
                ArrayList items = (ArrayList) v.get(panel.Panel);

                if (items != null) {
                    panel.Items = items.size();
                } else {
                    panel.Items = 0;
                }
            }

            //now, find first Panel
            Comparator comp = new PanelEntryComparator();
            Collections.sort(panelEntries, comp);
            ((PanelEntry) panelEntries.get(0)).FirstPanel = 1;
            firstPanel = ((PanelEntry) panelEntries.get(0)).Panel;

            int e = 0;

            if (sameVisit) {
                //find count of encounter and concept panels
                for (Enumeration i = v.keys(); i.hasMoreElements();) {
                    ArrayList vItems = (ArrayList) v.get(i.nextElement());

                    for (int vi = 0; vi < vItems.size(); vi++) {
                        ItemEntry t = (ItemEntry) vItems.get(vi);

                        if ((t.TableName.equals(CONCEPT_TABLE)) ||
                                (t.TableName.equals(ENCOUNTER_TABLE))) {
                            e++;
                            vi = vItems.size();
                        }
                    }
                }
            }

            //since we're no longer ordering ids by these columns this update isn't valid
            /*itemSql.append("update @v set sqlx = 1 " +
                            "where panel <> @f " +
                            "and lower(thetable) = '" + PATIENT_TABLE + "' " +
                            "and lower(thefield) in " +
                                    "('age_in_years_num','vital_status_cd','sex_cd','race_cd','vip_cd') \r\n");*/
            if (e > 1) {
                for (Enumeration i = v.keys(); i.hasMoreElements();) {
                    Integer panel = (Integer) i.nextElement();

                    if (!panel.equals(firstPanel)) {
                        ArrayList vItems = (ArrayList) v.get(panel);

                        for (int vi = 0; vi < vItems.size(); vi++) {
                            ItemEntry t = (ItemEntry) vItems.get(vi);

                            if (t.TableName.equals(ENCOUNTER_TABLE)) {
                                t.SqlX = 1;
                            }
                        }
                    }
                }
            }

            //set all short values
            //all shorts stands for shortcut values...we can't use shortcuts with
            //the current database structure
            /*for (int i=0; i<panelEntries.size(); i++)
            {
                    PanelEntry p = (PanelEntry) panelEntries.get(i);
                    if (!p.equals(firstPanel))
                    {
                            ArrayList vItems = (ArrayList) v.get(p.Panel);
                            int xitems = 0;
                            for (int vi=0; vi<vItems.size(); vi++)
                                    if (((ItemEntry) vItems.get(vi)).SqlX > 0)
                                            xitems++;
            
                            if (p.Items==xitems)
                                    p.AllShort = 1;
                    }
            
                    if ((p.AllShort==1)||(p.FirstPanel==1))
                                    p.ForInsert = 1;
            }*/
            comp = new QPanelEntryComparator();
            Collections.sort(panelEntries, comp);

            ArrayList q = new ArrayList();

            for (int i = 0; i < panelEntries.size(); i++) {
                PanelEntry p = (PanelEntry) panelEntries.get(i);

                if (p.Items > 0) {
                    IdentityPanelEntry ip = new IdentityPanelEntry(p);
                    q.add(ip);
                }
            }

            ArrayList resultArray = new ArrayList();

            for (int i = 0; i < q.size(); i++) {
                IdentityPanelEntry p = (IdentityPanelEntry) q.get(i);
                ArrayList vItems = (ArrayList) v.get(p.OldPanel);

                for (int vi = 0; vi < vItems.size(); vi++)
                    resultArray.add(new ResultEntry(e, p,
                            ((ItemEntry) vItems.get(vi))));
            }

            if (specificity > 1) {
                comp = new ResultEntryComparator(PATIENT_TABLE,
                        ENCOUNTER_TABLE, PROVIDER_TABLE);
            } else {
                comp = new ResultEntryComparator(PATIENT_TABLE,
                        ENCOUNTER_TABLE, CONCEPT_TABLE);
            }

            Collections.sort(resultArray, comp);

            ResultEntry[] rs = new ResultEntry[resultArray.size()];

            return (ResultEntry[]) resultArray.toArray(rs);
        } catch (Exception e) {
            log.error(e.getMessage());

            return null;
        }
    }

    public class ItemMetaData {
        public String QueryTable;
        public String QueryColumn;
        public String QueryOp;
        public String QueryCode;
    }
    public enum DatabaseType {SqlServer,
        Oracle;
    }
    public enum XmlFormat {RPDR,
        I2B2;
    }
}
