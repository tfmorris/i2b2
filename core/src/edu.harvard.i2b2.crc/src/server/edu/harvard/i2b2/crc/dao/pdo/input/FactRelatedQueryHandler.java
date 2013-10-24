/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.input;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.pdo.I2B2PdoFactory;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.RPDRPdoFactory;
import edu.harvard.i2b2.crc.dao.pdo.filter.ConceptFilter;
import edu.harvard.i2b2.crc.dao.pdo.filter.DimensionFilter;
import edu.harvard.i2b2.crc.dao.pdo.filter.ProviderFilter;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sql.RowSet;
import javax.sql.rowset.JdbcRowSet;


/**
 * Observation fact handler class for pdo request.
 * This class uses given pdo request to generate pdo sql and
 * build observation fact, unique list of fact's patient,concept code, visit and provider
 * list
 * <p>This class handles fact related queries for both plain and table pdo
 * $Id: FactRelatedQueryHandler.java,v 1.17 2007/10/18 19:48:49 rk903 Exp $
 * @author rkuttan
 * @see VisitFactRelated
 * @see ProviderFactRelated
 * @see PatientFactRelated
 * @see ObservationFactRelated
 */
public class FactRelatedQueryHandler extends CRCDAO {
    /** Input option list from pdo request**/
    private InputOptionListType inputList = null;

    /** filter list from pdo request **/
    private FilterListType filterList = null;

    /** helper class for visit/event  in pdo **/
    private VisitFactRelated visitFactRelated = null;

    /** helper class for observer/provider in pdo **/
    private ProviderFactRelated providerFactRelated = null;

    /** helper class for patient in pdo **/
    private PatientFactRelated patientFactRelated = null;

    /**  helper class for concepts in pdo **/
    private ConceptFactRelated conceptFactRelated = null;

    /** helper class for observation fact in pdo **/
    private ObservationFactFactRelated obsFactFactRelated = null;
    /** to store unique patient number list  present in fact**/
    List<String> patientFactList = new Vector<String>();
    /** to store unique concept code list present in fact**/
    List<String> conceptFactList = new Vector<String>();
    /** to store unique encounter number present in fact **/
    List<String> visitFactList = new Vector<String>();
    /** to store unique provider/observer id present in fact **/
    List<String> providerFactList = new Vector<String>();

    /** Handler interface for input list, i.e Patient list or visit list **/
    private IInputOptionListHandler inputOptionListHandler = null;

    /** flag to see if concept filter is set, used in observation fact set element **/
    private boolean checkFilter = false;

    /** field to keep track number of prepared statment parameters in the genereated pdo query**/
    private int queryParameterCount = 0;

    /**
     * Constructor with parameter
     * @param inputList
     * @param filterList
     * @param outputOptionList
     */
    public FactRelatedQueryHandler(InputOptionListType inputList,
        FilterListType filterList, OutputOptionListType outputOptionList) {
        this.inputList = inputList;
        this.filterList = filterList;

        visitFactRelated = new VisitFactRelated(outputOptionList.getEventSet());
        providerFactRelated = new ProviderFactRelated(outputOptionList.getObserverSetUsingFilterList());
        patientFactRelated = new PatientFactRelated(outputOptionList.getPatientSet());
        conceptFactRelated = new ConceptFactRelated(outputOptionList.getConceptSetUsingFilterList());
        obsFactFactRelated = new ObservationFactFactRelated(outputOptionList.getObservationSet());

        // check if concept filter present
        if ((filterList != null) && (filterList.getPanel() != null) &&
                (filterList.getPanel().size() > 0)) {
            checkFilter = true;
        }
    }

    /**
     * Function to build and execute pdo sql and build plain pdo's observation fact
     * @return
     * @throws Exception
     */
    public List<ObservationSet> getPdoObservationFact()
        throws I2B2DAOException {
        ResultSet resultSet = null;
        Connection conn = null;
        List<ObservationSet> observationFactSetList = new ArrayList<ObservationSet>();

        try {
            conn = getConnection();

            int sqlParamCount = 1;

            if (filterList.getPanel().size() == 0) {
                //        	 		generate sql
                String querySql = buildQuery(null,
                        PdoQueryHandler.PLAIN_PDO_TYPE);
                log.debug("Executing sql[" + querySql + "]");
                // 	execute fullsql
                resultSet = executeQuery(conn, querySql, sqlParamCount);
                // 	build facts
                observationFactSetList.add(buildPDOFact(resultSet, ""));
            } else {
                for (PanelType panel : filterList.getPanel()) {
                    // 	generate sql
                    String querySql = buildQuery(panel,
                            PdoQueryHandler.PLAIN_PDO_TYPE);
                    log.debug("Executing sql[" + querySql + "]");
                    // 	execute fullsql
                    sqlParamCount = panel.getItem().size();
                    resultSet = executeQuery(conn, querySql, sqlParamCount);
                    // 	build facts
                    observationFactSetList.add(buildPDOFact(resultSet,
                            panel.getName()));
                }
            }
        } catch (SQLException sqlEx) {
            throw new I2B2DAOException("", sqlEx);
        } catch (IOException ioEx) {
            throw new I2B2DAOException("", ioEx);
        } finally {
            // close connection
            try {
                JDBCUtil.closeJdbcResource(null, null, conn);
            } catch (SQLException e) {
                log.error("Error trying to close connection", e);
            }
        }

        return observationFactSetList;
    }

    /**
     * Function to build and execute pdo sql and build table pdo's observation fact
     * @return ObservationSet list
     * @throws I2B2DAOException
     */
    public List<ObservationSet> getTablePdoObservationFact()
        throws I2B2DAOException {
        Connection conn = null;
        List<ObservationSet> observationSetList = new ArrayList<ObservationSet>();
        ResultSet resultSet = null;

        try {
            conn = getConnection();

            int sqlParamCount = 1;

            if (filterList.getPanel().size() == 0) {
                //        	 		generate sql
                String querySql = buildQuery(null,
                        PdoQueryHandler.PLAIN_PDO_TYPE);
                log.debug("Executing sql[" + querySql + "]");
                // 	execute fullsql
                resultSet = executeQuery(conn, querySql, sqlParamCount);
                // 	build facts
                observationSetList.add(buildPDOFact(resultSet, ""));
            } else {
                for (PanelType panel : filterList.getPanel()) {
                    // generate sql
                    String querySql = buildQuery(panel,
                            PdoQueryHandler.TABLE_PDO_TYPE);
                    log.debug("Executing sql[" + querySql + "]");
                    sqlParamCount = panel.getItem().size();
                    // execute fullsql
                    resultSet = executeQuery(conn, querySql, sqlParamCount);
                    // build facts
                    observationSetList.add(buildTablePDOFact(resultSet,
                            panel.getName()));
                }
            }
        } catch (SQLException sqlEx) {
            throw new I2B2DAOException("", sqlEx);
        } catch (IOException ioEx) {
            throw new I2B2DAOException("", ioEx);
        } finally {
            // close connection
            try {
                JDBCUtil.closeJdbcResource(null, null, conn);
            } catch (SQLException e) {
                log.error("Error trying to close connection", e);
            }
        }

        return observationSetList;
    }

    /**
     * Returns provider id, belong to the facts
     * @return list of provider/observer id
     */
    public List<String> getProviderFactList() {
        return providerFactList;
    }

    /**
     * Returns concept code belong to the facts
     * @return
     */
    public List<String> getConceptFactList() {
        return conceptFactList;
    }

    /**
     * Returns patient number belong to the facts
     * @return
     */
    public List<String> getPatientFactList() {
        return patientFactList;
    }

    /**
     * Returns encounter number belong to the facts
     * @return list of encounter number
     */
    public List<String> getVisitFactList() {
        return visitFactList;
    }

    //-----------------------------------------
    //private helper functions start from here
    //-----------------------------------------

    /**
     * This is the main function to build query for plain and table pdo request
     * @param pdoType
     * @return String
     * @throws I2B2DAOException
     */
    private String buildQuery(PanelType panel, String pdoType)
        throws I2B2DAOException {
        String obsFactSelectClause = null;

        if (obsFactFactRelated != null) {
            obsFactSelectClause = obsFactFactRelated.getSelectClause();

            if (obsFactSelectClause.length() <= 0) {
                obsFactSelectClause = obsFactFactRelated.getDefaultSelectClause();
            }
        }

        String tableLookupJoinClause = " ";

        if (pdoType.equals(PdoQueryHandler.TABLE_PDO_TYPE)) {
            tableLookupJoinClause = getLookupJoinClause(obsFactFactRelated.isSelectDetail(),
                    obsFactFactRelated.isSelectBlob(),
                    obsFactFactRelated.isSelectStatus());
            obsFactSelectClause += " , concept_lookup.name_char concept_name, provider_lookup.name_char provider_name, modifier_lookup.name_char modifier_name,location_lookup.name_char location_name ";
        }

        String fullWhereClause = "";

        // fullWhereClause = patientSetWhereBuilder.getWhereClause();
        if (inputList.getPatientList() != null) {
            inputOptionListHandler = new PatientListTypeHandler(inputList.getPatientList());
            fullWhereClause = " obs.patient_num IN  ";
        } else if (inputList.getEventList() != null) {
            inputOptionListHandler = new VisitListTypeHandler(inputList.getEventList());
            fullWhereClause = " obs.encounter_num IN  \n";
        } else {
            throw new I2B2DAOException(
                "Input option list does not contain visit or patient list");
        }

        fullWhereClause += (" ( " +
        inputOptionListHandler.generateWhereClauseSql() + " ) \n");

        boolean conceptFilterListFlag = false;
        boolean providerFilterListFlag = false;
        String factByConceptSql = "";
        String factByProviderSql = "";
        String factWithoutFilterSql = "";

        String mainQuerySql = "SELECT * FROM ( \n";

        try {
            if (panel != null) {
                factByConceptSql = factQueryWithDimensionFilter(obsFactSelectClause,
                        tableLookupJoinClause, fullWhereClause, panel);
                mainQuerySql += factByConceptSql;
            } else {
                factWithoutFilterSql = factQueryWithoutFilter(obsFactSelectClause,
                        tableLookupJoinClause, fullWhereClause);
                mainQuerySql += factWithoutFilterSql;
            }
        } catch (I2B2Exception i2b2Ex) {
            throw new I2B2DAOException(i2b2Ex.getMessage(), i2b2Ex);
        }

        /*
        if ((filterList != null) &&
                ((filterList.getConceptList() != null) ||
                (filterList.getObserverList() != null))) {
            if (filterList.getConceptList() != null && filterList.getConceptList().getConceptPath() != null && filterList.getConceptList().getConceptPath().size()>0) {
                factByConceptSql = factQueryWithConceptFilter(obsFactSelectClause,
                        tableLookupJoinClause, fullWhereClause);
                log.debug("Fact By ConceptSql[" + factByConceptSql + "]");
                conceptFilterListFlag = true;
            }
        
            if (filterList.getObserverList() != null) {
                factByProviderSql = factQueryWithProviderFilter(obsFactSelectClause,
                        tableLookupJoinClause, fullWhereClause);
                log.debug("Fact By ProviderSql[" + factByProviderSql + "]");
                providerFilterListFlag = true;
            }
        } else {
            factWithoutFilterSql = factQueryWithoutFilter(obsFactSelectClause,
                    tableLookupJoinClause, fullWhereClause);
            log.debug("Fact without FilterSql[" + factWithoutFilterSql + "]");
        }
        
        String mainQuerySql = "SELECT * FROM ( \n";
        
        if ((providerFilterListFlag == false) &&
                (conceptFilterListFlag == false)) {
            mainQuerySql += factWithoutFilterSql;
            queryParameterCount = 1;
        }
        
        if (conceptFilterListFlag == true) {
            mainQuerySql += factByConceptSql;
            queryParameterCount = 1;
        }
        
        if (providerFilterListFlag == true) {
            if (conceptFilterListFlag == true) {
                mainQuerySql += "\n UNION ALL \n";
                queryParameterCount = 2;
            } else {
                queryParameterCount = 1;
            }
        
            mainQuerySql += factByProviderSql;
        }
        
                */
        mainQuerySql += " WHERE ROWNUM <= 100000  ) where rnum >= 0 ";

        return mainQuerySql;
    }

    /**
     * Function to uses given select, join and where clause to build core pdo query,
     * related to provider filter
     * @param obsFactSelectClause
     * @param tableLookupJoinClause
     * @param fullWhereClause
     * @return
     */
    private String factQueryWithDimensionFilter(String obsFactSelectClause,
        String tableLookupJoinClause, String fullWhereClause, PanelType panel)
        throws I2B2Exception {
        String factByProviderSql = "";
        int i = 0;
        String panelName = null;
        if (panel.getName() != null) { 
        	panelName = JDBCUtil.escapeSingleQuote(panel.getName());
        }
        

        factByProviderSql += "SELECT /*+ FIRST_ROWS(200000) */ a.*, ROWNUM rnum FROM (\n";
        obsFactSelectClause += (", '" + panelName + "' panel_name ");
        
        for (ItemType item : panel.getItem()) {
            //read the first item
            //ItemType item = panel.getItem().get(0);
            if (item.getDimTablename().trim()
                        .equalsIgnoreCase("concept_dimension")) {
                item.setDimColumnname("concept_path");
                item.setFacttablecolumn("concept_cd");
            } else if (item.getDimTablename().trim()
                               .equalsIgnoreCase("provider_dimension")) {
                item.setDimColumnname("provider_path");
                item.setFacttablecolumn("provider_id");
            }

            if (i == 0) {
                i = 1;
            } else {
                factByProviderSql += "UNION \n";
            }

            
            factByProviderSql += (" SELECT /*+ index (obs OBFACT_PATCON_SDED_NVTV_IDX)*/ " + obsFactSelectClause + " FROM \n");

            DimensionFilter providerFilter = new DimensionFilter(item);
            factByProviderSql += (" " + providerFilter.getFromSqlString() +
            "  \n");
            factByProviderSql += ",observation_FACT obs \n";

            fullWhereClause += (" AND obs." + item.getFacttablecolumn() +
            " = dimension." + item.getFacttablecolumn());

            factByProviderSql += tableLookupJoinClause;

            factByProviderSql += (" WHERE \n" + fullWhereClause + "\n");
        }

        //factByProviderSql += "  ORDER BY obs.patient_num,obs.start_date,obs.concept_cd,obs.rowid) a \n";
        factByProviderSql += "  ORDER BY 2,5,3) a \n";

        return factByProviderSql;
    }

    /**
     * Function to uses given select, join and where clause to build core pdo query,
     * related to provider filter
     * @param obsFactSelectClause
     * @param tableLookupJoinClause
     * @param fullWhereClause
     * @return
     */

    //    private String factQueryWithProviderFilter(String obsFactSelectClause,
    //        String tableLookupJoinClause, String fullWhereClause) {
    //        String factByProviderSql = "SELECT /*+ FIRST_ROWS(200000) */ a.*, ROWNUM rnum FROM (\n";
    //        obsFactSelectClause += ",provider.c_path, provider.name_char, 'provider_dimension' table_name ";
    //        factByProviderSql += (" SELECT /*+ index (obs OBFACT_PATCON_SDED_NVTV_IDX)*/ " +
    //        obsFactSelectClause + " FROM \n");
    //
    //        ProviderFilter providerFilter = new ProviderFilter(filterList);
    //        factByProviderSql += (" " + providerFilter.getFromSqlString() + "  \n");
    //        factByProviderSql += ",observation_FACT obs \n";
    //        fullWhereClause += " AND obs.provider_id = provider.provider_id ";
    //
    //        factByProviderSql += tableLookupJoinClause;
    //
    //        factByProviderSql += (" WHERE \n" + fullWhereClause + "\n");
    //        factByProviderSql += "  ORDER BY provider.c_path,obs.patient_num,obs.start_date,obs.concept_cd,obs.rowid) a \n";
    //
    //        return factByProviderSql;
    //    }

    /**
     * Function to uses given select, join and where clause to build core pdo query,
     * related to concept filter
     * @param obsFactSelectClause
     * @param tableLookupJoinClause
     * @param fullWhereClause
     * @return
     */

    //    private String factQueryWithConceptFilter(String obsFactSelectClause,
    //        String tableLookupJoinClause, String fullWhereClause) {
    //        String factByConceptSql = "SELECT /*+ FIRST_ROWS(200000)  b.*, ROWNUM rnum FROM (\n";
    //        obsFactSelectClause += ",concept.c_path, concept.name_char, 'concept_dimension' table_name ";
    //        factByConceptSql += (" SELECT /*+ index (obs OBFACT_PATCON_SDED_NVTV_IDX)*/ " +
    //        obsFactSelectClause + " FROM \n");
    //
    //        ConceptFilter conceptFilter = new ConceptFilter(filterList);
    //        factByConceptSql += (" " + conceptFilter.getFromSqlString() + "  \n");
    //        factByConceptSql += ", observation_FACT obs \n";
    //        fullWhereClause += " AND obs.concept_cd = concept.concept_cd ";
    //
    //        factByConceptSql += tableLookupJoinClause;
    //
    //        factByConceptSql += (" WHERE \n" + fullWhereClause + "\n");
    //        factByConceptSql += "  ORDER BY concept.c_path,obs.patient_num,obs.start_date,obs.concept_cd,obs.rowid) b \n";
    //
    //        return factByConceptSql;
    //    }

    /**
     * Function to uses given select, join and where clause to build core pdo query,
     * without any filter (concept and provider)
     * @param obsFactSelectClause
     * @param tableLookupJoinClause
     * @param fullWhereClause
     * @return
     */
    private String factQueryWithoutFilter(String obsFactSelectClause,
        String tableLookupJoinClause, String fullWhereClause) {
        String factSql = "SELECT /*+ FIRST_ROWS(200000) */ b.*, ROWNUM rnum FROM (\n";
        factSql += (" SELECT /*+ index (obs OBFACT_PATCON_SDED_NVTV_IDX)*/ " +
        obsFactSelectClause + " FROM observation_FACT obs\n");

        factSql += tableLookupJoinClause;

        factSql += (" WHERE \n" + fullWhereClause + "\n");
        factSql += " ORDER BY obs.patient_num,obs.start_date,obs.concept_cd,obs.rowid) b \n";

        return factSql;
    }

    /**
     * Generate fact's join clause for table pdo
     * @param detailFlag
     * @param blobFlag
     * @param statusFlag
     * @return
     */
    private String getLookupJoinClause(boolean detailFlag, boolean blobFlag,
        boolean statusFlag) {
        String joinClause = " ";

        if (detailFlag) {
            joinClause = " left JOIN fact_lookup modifier_lookup \n" +
                " ON (obs.modifier_cd = modifier_lookup.concept_Cd AND modifier_lookup.dim_column = 'MODIFIER_CD') \n" +
                " left JOIN concept_dimension concept_lookup \n" +
                " ON (obs.concept_cd = concept_lookup.concept_Cd) \n" +
                " left JOIN provider_dimension provider_lookup \n" +
                " ON (obs.provider_id = provider_lookup.provider_id) \n" +
                " left JOIN visit_dim_lookup location_lookup \n" +
                " ON (obs.location_Cd = location_lookup.concept_Cd AND location_lookup.dim_column = 'LOCATION_CD') \n";
        }

        return joinClause;
    }

    /**
     * Helper function to build unique visit, patient, concept list from observation fact
     * @param encounterNum
     * @param patientNum
     * @param providerId
     * @param conceptCd
     */
    private void addToDistinctList(String encounterNum, String patientNum,
        String providerId, String conceptCd) {
        if (visitFactRelated.isSelected()) {
            if (!visitFactList.contains(encounterNum)) {
                visitFactList.add(encounterNum);
            }
        }

        if (patientFactRelated.isSelected()) {
            if (!patientFactList.contains(patientNum)) {
                patientFactList.add(patientNum);
            }
        }

        if (providerFactRelated.isSelected()) {
            if (!providerFactList.contains(providerId)) {
                providerFactList.add(providerId);
            }
        }

        if (conceptFactRelated.isSelected()) {
            if (!conceptFactList.contains(conceptCd)) {
                conceptFactList.add(conceptCd);
            }
        }
    }

    /**
     * Executive the given query
     * @param conn
     * @param querySql
     * @return
     * @throws SQLException
     */
    private ResultSet executeQuery(Connection conn, String querySql,
        int sqlParamCount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(querySql);

        if (inputOptionListHandler.isCollectionId()) {
            for (int i = 1; i <= sqlParamCount; i++) {
                stmt.setInt(i,
                    Integer.parseInt(inputOptionListHandler.getCollectionId()));
            }
        } else if (inputOptionListHandler.isEnumerationSet()) {
            oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn).getUnderlyingConnection();
            List<String> enumList = inputOptionListHandler.getEnumerationList();
            ArrayDescriptor desc = ArrayDescriptor.createDescriptor("QT_PDO_QRY_INT_ARRAY",
                    conn1);

            oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
                    enumList.toArray(new String[] {  }));

            for (int i = 1; i <= sqlParamCount; i++) {
                stmt.setArray(1, paramArray);
            }

            //if (queryParameterCount == 2) {
            //    stmt.setArray(2, paramArray);
            //}
        }

        ResultSet resultSet = stmt.executeQuery();

        return resultSet;

        //JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
        //return rowSet;
    }

    /**
     * Build plain pdo's observation fact
     * @param rowSet
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private ObservationSet buildPDOFact(ResultSet rowSet, String panelName)
        throws SQLException, IOException {
        ObservationSet currentObsFactSetType = new ObservationSet();
        currentObsFactSetType.setPanelName(panelName);

        I2B2PdoFactory.ObservationFactBuilder observationFactBuilder = new I2B2PdoFactory().new ObservationFactBuilder(obsFactFactRelated.isSelectDetail(),
                obsFactFactRelated.isSelectBlob(),
                obsFactFactRelated.isSelectStatus());

        while (rowSet.next()) {
            ObservationType obsFactType = null;

            obsFactType = observationFactBuilder.buildObservationSet(rowSet);

            if (obsFactFactRelated.isSelected()) {
                currentObsFactSetType.getObservation().add(obsFactType);
            }

            addToDistinctList(obsFactType.getEventId().getValue(),
                obsFactType.getPatientId(),
                obsFactType.getObserverCd().getValue(),
                obsFactType.getConceptCd().getValue());
        }

        return currentObsFactSetType;
    }

    /**
     * Build table pdo observaton fact
     * @param rowSet
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private ObservationSet buildTablePDOFact(ResultSet rowSet, String panelName)
        throws SQLException, IOException {
        //obsFactSetTypeList = new Vector<PatientDataType.ObservationFactSet>();
        boolean detailFlag = obsFactFactRelated.isSelectDetail();
        boolean booleanFlag = obsFactFactRelated.isSelectBlob();
        boolean statusFlag = obsFactFactRelated.isSelectStatus();
        ObservationSet currentObservationSet = new ObservationSet();
        currentObservationSet.setPanelName(panelName);

        RPDRPdoFactory.ObservationFactBuilder observationFactBuilder = new RPDRPdoFactory.ObservationFactBuilder(detailFlag,
                booleanFlag, statusFlag);

        while (rowSet.next()) {
            ObservationType observation = null;
            observation = observationFactBuilder.buildObservationSet(rowSet,
                    "i2b2");

            if (obsFactFactRelated.isSelected()) {
                currentObservationSet.getObservation().add(observation);
            }

            String encounterNum = observation.getEventId().getValue();
            String patientNum = observation.getPatientId();
            String providerId = observation.getObserverCd().getValue();
            String conceptCd = observation.getConceptCd().getValue();
            addToDistinctList(encounterNum, patientNum, providerId, conceptCd);
        }

        return currentObservationSet;
    }
}
