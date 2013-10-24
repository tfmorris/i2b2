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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.sql.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.xml.XMLOperatorLookup;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.pdo.I2B2PdoFactory;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.RPDRPdoFactory;
import edu.harvard.i2b2.crc.dao.pdo.filter.DimensionFilter;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainDateTimeType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainDateType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InclusiveType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType.ConstrainByDate;
import edu.harvard.i2b2.crc.datavo.pdo.query.PanelType.TotalItemOccurrences;

/**
 * Observation fact handler class for pdo request. This class uses given pdo
 * request to generate pdo sql and build observation fact, unique list of fact's
 * patient,concept code, visit and provider list
 * <p>
 * This class handles fact related queries for both plain and table pdo $Id:
 * SQLServerFactRelatedQueryHandler.java,v 1.3 2008/06/10 14:59:05 rk903 Exp $
 * 
 * @author rkuttan
 * @see VisitFactRelated
 * @see ProviderFactRelated
 * @see PatientFactRelated
 * @see ObservationFactRelated
 */
public class SQLServerFactRelatedQueryHandler extends CRCDAO implements
		IFactRelatedQueryHandler {
	/** Input option list from pdo request* */
	private InputOptionListType inputList = null;

	/** filter list from pdo request * */
	private FilterListType filterList = null;

	/** helper class for visit/event in pdo * */
	private VisitFactRelated visitFactRelated = null;

	/** helper class for observer/provider in pdo * */
	private ProviderFactRelated providerFactRelated = null;

	/** helper class for patient in pdo * */
	private PatientFactRelated patientFactRelated = null;

	/** helper class for concepts in pdo * */
	private ConceptFactRelated conceptFactRelated = null;

	/** helper class for observation fact in pdo * */
	private ObservationFactFactRelated obsFactFactRelated = null;
	/** to store unique patient number list present in fact* */
	List<String> patientFactList = new Vector<String>();
	/** to store unique concept code list present in fact* */
	List<String> conceptFactList = new Vector<String>();
	/** to store unique encounter number present in fact * */
	List<String> visitFactList = new Vector<String>();
	/** to store unique provider/observer id present in fact * */
	List<String> providerFactList = new Vector<String>();

	/** Handler interface for input list, i.e Patient list or visit list * */
	private IInputOptionListHandler inputOptionListHandler = null;

	/**
	 * flag to see if concept filter is set, used in observation fact set
	 * element *
	 */
	private boolean checkFilter = false;

	/**
	 * field to keep track number of prepared statment parameters in the
	 * genereated pdo query*
	 */
	private int queryParameterCount = 0;

	private DataSourceLookup dataSourceLookup = null;

	public static final String TEMP_PDO_OBSFACT_TABLE = "#TEMP_PDO_OBSFACT";
	public static final String TEMP_PDO_INPUTLIST_TABLE = "#TEMP_PDO_INPUTLIST";

	/**
	 * Constructor with parameter
	 * 
	 * @param inputList
	 * @param filterList
	 * @param outputOptionList
	 */
	public SQLServerFactRelatedQueryHandler(DataSourceLookup dataSourceLookup,
			InputOptionListType inputList, FilterListType filterList,
			OutputOptionListType outputOptionList) {
		this.dataSourceLookup = dataSourceLookup;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.inputList = inputList;
		this.filterList = filterList;

		visitFactRelated = new VisitFactRelated(outputOptionList.getEventSet());
		providerFactRelated = new ProviderFactRelated(outputOptionList
				.getObserverSetUsingFilterList());
		patientFactRelated = new PatientFactRelated(outputOptionList
				.getPatientSet());
		conceptFactRelated = new ConceptFactRelated(outputOptionList
				.getConceptSetUsingFilterList());
		obsFactFactRelated = new ObservationFactFactRelated(outputOptionList
				.getObservationSet());

		// check if concept filter present
		if ((filterList != null) && (filterList.getPanel() != null)
				&& (filterList.getPanel().size() > 0)) {
			checkFilter = true;
		}
	}

	/**
	 * Function to build and execute pdo sql and build plain pdo's observation
	 * fact
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ObservationSet> getPdoObservationFact() throws I2B2DAOException {
		ResultSet resultSet = null;
		Connection conn = null;
		List<ObservationSet> observationFactSetList = new ArrayList<ObservationSet>();

		try {
			conn = this.getApplicationDataSource(
					dataSourceLookup.getDataSource()).getConnection();

			int sqlParamCount = 1;

			boolean createTempTable = true;
			if (filterList.getPanel().size() == 0) {
				// generate sql
				String querySql = buildQuery(null,
						PdoQueryHandler.PLAIN_PDO_TYPE);
				log.debug("Executing sql[" + querySql + "]");
				// execute fullsql
				resultSet = executeQuery(conn, querySql, sqlParamCount,
						createTempTable);
				// build facts
				observationFactSetList.add(buildPDOFact(resultSet, ""));
			} else {
				for (PanelType panel : filterList.getPanel()) {
					// generate sql
					String querySql = buildQuery(panel,
							PdoQueryHandler.PLAIN_PDO_TYPE);
					log.debug("Executing sql[" + querySql + "]");
					// execute fullsql
					sqlParamCount = panel.getItem().size();
					resultSet = executeQuery(conn, querySql, sqlParamCount,
							createTempTable);
					// build facts
					observationFactSetList.add(buildPDOFact(resultSet, panel
							.getName()));
				}
			}
		} catch (SQLException sqlEx) {
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			throw new I2B2DAOException("", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
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
	 * Function to build and execute pdo sql and build table pdo's observation
	 * fact
	 * 
	 * @return ObservationSet list
	 * @throws I2B2DAOException
	 */
	public List<ObservationSet> getTablePdoObservationFact()
			throws I2B2DAOException {
		Connection conn = null;
		List<ObservationSet> observationSetList = new ArrayList<ObservationSet>();
		ResultSet resultSet = null;

		try {
			conn = this.getApplicationDataSource(
					dataSourceLookup.getDataSource()).getConnection();

			int sqlParamCount = 1;

			//
			boolean createTempTable = true;
			if (filterList.getPanel().size() == 0) {
				// generate sql
				String querySql = buildQuery(null,
						PdoQueryHandler.PLAIN_PDO_TYPE);
				log.debug("Executing sql[" + querySql + "]");
				// execute fullsql
				resultSet = executeQuery(conn, querySql, sqlParamCount,
						createTempTable);
				// build facts
				observationSetList.add(buildPDOFact(resultSet, ""));
			} else {
				for (PanelType panel : filterList.getPanel()) {
					// generate sql
					String querySql = buildQuery(panel,
							PdoQueryHandler.TABLE_PDO_TYPE);
					log.debug("Executing sql[" + querySql + "]");
					sqlParamCount = panel.getItem().size();
					// execute fullsql
					resultSet = executeQuery(conn, querySql, sqlParamCount,
							createTempTable);
					// build facts
					observationSetList.add(buildTablePDOFact(resultSet, panel
							.getName()));
					createTempTable = false;
				}
			}
		} catch (SQLException sqlEx) {
			throw new I2B2DAOException("", sqlEx);
		} catch (IOException ioEx) {
			throw new I2B2DAOException("", ioEx);
		} finally {
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				deleteTempTable(conn);
			}
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
	 * 
	 * @return list of provider/observer id
	 */
	public List<String> getProviderFactList() {
		return providerFactList;
	}

	/**
	 * Returns concept code belong to the facts
	 * 
	 * @return
	 */
	public List<String> getConceptFactList() {
		return conceptFactList;
	}

	/**
	 * Returns patient number belong to the facts
	 * 
	 * @return
	 */
	public List<String> getPatientFactList() {
		return patientFactList;
	}

	/**
	 * Returns encounter number belong to the facts
	 * 
	 * @return list of encounter number
	 */
	public List<String> getVisitFactList() {
		return visitFactList;
	}

	/**
	 * This is the main function to build query for plain and table pdo request
	 * 
	 * @param pdoType
	 * @return String
	 * @throws I2B2DAOException
	 */
	public String buildQuery(PanelType panel, String pdoType)
			throws I2B2DAOException {
		String obsFactSelectClause = null;

		if (obsFactFactRelated != null) {
			obsFactSelectClause = obsFactFactRelated.getSelectClause();

			if (obsFactSelectClause.length() <= 0) {
				obsFactSelectClause = obsFactFactRelated
						.getDefaultSelectClause();
			}
		}

		String tableLookupJoinClause = " ";

		if (pdoType.equals(PdoQueryHandler.TABLE_PDO_TYPE)) {
			tableLookupJoinClause = getLookupJoinClause(obsFactFactRelated
					.isSelectDetail(), obsFactFactRelated.isSelectBlob(),
					obsFactFactRelated.isSelectStatus());
			obsFactSelectClause += " , concept_lookup.name_char concept_name, provider_lookup.name_char provider_name, modifier_lookup.name_char modifier_name,location_lookup.name_char location_name ";
		}

		String fullWhereClause = "";

		// fullWhereClause = patientSetWhereBuilder.getWhereClause();
		if (inputList.getPatientList() != null) {
			inputOptionListHandler = new PatientListTypeHandler(
					dataSourceLookup, inputList.getPatientList());
			fullWhereClause = " obs.patient_num IN  ";
		} else if (inputList.getEventList() != null) {
			inputOptionListHandler = new VisitListTypeHandler(dataSourceLookup,
					inputList.getEventList());
			fullWhereClause = " obs.encounter_num IN  \n";
		} else {
			throw new I2B2DAOException(
					"Input option list does not contain visit or patient list");
		}

		fullWhereClause += (" ( "
				+ inputOptionListHandler.generateWhereClauseSql() + " ) \n");

		String factByConceptSql = "";
		String factWithoutFilterSql = "";

		String mainQuerySql = "SELECT * FROM ( \n";

		try {
			if (panel != null) {
				factByConceptSql = factQueryWithDimensionFilter(
						obsFactSelectClause, tableLookupJoinClause,
						fullWhereClause, panel);
				if (panel.getTotalItemOccurrences() != null
						&& panel.getTotalItemOccurrences().getValue() > 0) {
					mainQuerySql += " select *, rank() over (partition by obs_encounter_num,obs_patient_num,obs_start_date,obs_concept_cd order by rnum) as seqNumber from ( "
							+ factByConceptSql + " ) as t";
				} else {
					mainQuerySql += factByConceptSql;
				}

			} else {
				factWithoutFilterSql = factQueryWithoutFilter(
						obsFactSelectClause, tableLookupJoinClause,
						fullWhereClause);
				mainQuerySql += factWithoutFilterSql;
			}

		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2DAOException(i2b2Ex.getMessage(), i2b2Ex);
		}

		mainQuerySql += "   ) as final where rnum >= 0 ";

		TotalItemOccurrences totOccurance = panel.getTotalItemOccurrences();
		if (totOccurance != null && totOccurance.getValue() > 1) {
			int totOcurranceValue = totOccurance.getValue();
			String totOccuranceOperator = ">=";
			if (totOccurance.getOperator() != null) {
				String totOccuranceOperatorValue = totOccurance.getOperator()
						.value();
				totOccuranceOperator = XMLOperatorLookup
						.getComparisonOperatorFromAcronum(totOccuranceOperatorValue);
				if (totOccuranceOperator == null) {
					totOccuranceOperator = ">=";
				}
			}

			mainQuerySql += " AND seqNumber " + totOccuranceOperator
					+ totOcurranceValue;
		}
		mainQuerySql += " ORDER BY obs_patient_num,obs_start_date,obs_concept_cd,rnum";
		return mainQuerySql;
	}

	// -----------------------------------------
	// private helper functions start from here
	// -----------------------------------------

	/**
	 * Function to uses given select, join and where clause to build core pdo
	 * query, related to provider filter
	 * 
	 * @param obsFactSelectClause
	 * @param tableLookupJoinClause
	 * @param fullWhereClause
	 * @return
	 */
	private String factQueryWithDimensionFilter(String obsFactSelectClause,
			String tableLookupJoinClause, String fullWhereClause,
			PanelType panel) throws I2B2Exception {
		String factByProviderSql = "";
		int i = 0;
		String panelName = null;
		DateConstrainHandler dateConstrainHandler = new DateConstrainHandler(
				dataSourceLookup);

		if (panel.getName() != null) {
			panelName = JDBCUtil.escapeSingleQuote(panel.getName());
		}

		obsFactSelectClause += (", '" + panelName + "' panel_name ");

		int totalItemOccurance = 0;
		if (panel.getTotalItemOccurrences() != null) {
			totalItemOccurance = panel.getTotalItemOccurrences().getValue();
		}
		String panelDateConstrain = null;
		// generate panel date constrain
		if (panel.getPanelDateFrom() != null || panel.getPanelDateTo() != null) {
			panelDateConstrain = generatePanelDateConstrain(
					dateConstrainHandler, panel.getPanelDateFrom(), panel
							.getPanelDateTo());
		}

		for (ItemType item : panel.getItem()) {
			// read the first item
			// ItemType item = panel.getItem().get(0);
			if (item.getDimTablename().trim().equalsIgnoreCase(
					"concept_dimension")) {
				item.setDimColumnname("concept_path");
				item.setFacttablecolumn("concept_cd");
			} else if (item.getDimTablename().trim().equalsIgnoreCase(
					"provider_dimension")) {
				item.setDimColumnname("provider_path");
				item.setFacttablecolumn("provider_id");
			}

			if (i == 0) {
				i = 1;
			} else {
				if (totalItemOccurance > 1) {
					factByProviderSql += "UNION ALL \n";
				} else {
					factByProviderSql += "UNION \n";
				}
			}

			factByProviderSql += (" SELECT /*+ index (obs OBFACT_PATCON_SDED_NVTV_IDX)*/ "
					+ obsFactSelectClause + " FROM \n");

			DimensionFilter providerFilter = new DimensionFilter(item, this
					.getDbSchemaName());
			factByProviderSql += (" " + providerFilter.getFromSqlString() + "  \n");
			factByProviderSql += ", " + getDbSchemaName()
					+ "observation_FACT obs \n";

			String fullWhereClause1 = fullWhereClause
					+ (" AND obs." + item.getFacttablecolumn()
							+ " = dimension." + item.getFacttablecolumn());

			factByProviderSql += tableLookupJoinClause;

			factByProviderSql += (" WHERE \n" + fullWhereClause1 + "\n");

			// if value constrain is given, generate value constrain sql
			if ((item.getConstrainByValue() != null)
					&& (item.getConstrainByValue().size() > 0)) {
				ValueConstrainsHandler vh = new ValueConstrainsHandler();
				String valueConstrainSql = vh.constructValueConstainClause(item
						.getConstrainByValue());

				if ((valueConstrainSql != null)
						&& (valueConstrainSql.length() > 0)) {
					factByProviderSql += (" AND " + valueConstrainSql + "\n");
				}
			}

			// add start and end date constrains

			List<ConstrainByDate> constrainByDateList = item
					.getConstrainByDate();
			for (ConstrainByDate constrainByDate : constrainByDateList) {

				ConstrainDateType dateFrom = constrainByDate.getDateFrom();
				ConstrainDateType dateTo = constrainByDate.getDateTo();

				String dateFromColumn = null, dateToColumn = null;
				InclusiveType dateFromInclusive = null, dateToInclusive = null;
				XMLGregorianCalendar dateFromValue = null, dateToValue = null;
				String dateConstrainSql = null;

				// obs.start_date, obs.end_date
				if (dateFrom != null || dateTo != null) {

					if (dateFrom != null) {
						dateFromInclusive = dateFrom.getInclusive();
						dateFromValue = dateFrom.getValue();
						if (dateFrom.getTime() != null
								&& dateFrom.getTime().name() != null
								&& dateFrom.getTime().name().equalsIgnoreCase(
										ConstrainDateTimeType.END_DATE.name())) {
							dateFromColumn = "obs.end_date";
						} else {
							dateFromColumn = "obs.start_date";
						}

					}

					if (dateTo != null) {
						dateToInclusive = dateTo.getInclusive();
						dateToValue = dateTo.getValue();
						if (dateTo.getTime() != null
								&& dateTo.getTime().name() != null
								&& dateTo.getTime().name().equals(
										ConstrainDateTimeType.END_DATE.name())) {
							dateToColumn = "obs.end_date";
						} else {
							dateToColumn = "obs.start_date";
						}
					}

					dateConstrainSql = dateConstrainHandler
							.constructDateConstrainClause(dateFromColumn,
									dateToColumn, dateFromInclusive,
									dateToInclusive, dateFromValue, dateToValue);
					if (dateConstrainSql != null) {
						factByProviderSql += (" AND " + dateConstrainSql + "\n");
					}

					// item.getConstrainByModifier().get(0).getModifierName()
				}
			}
			// generate panel date constrain
			if (panelDateConstrain != null) {
				factByProviderSql += (" AND " + panelDateConstrain + "\n");
			}
		}

		int invert = panel.getInvert();

		if (invert == 1) {
			String invertSql = ("( SELECT " + obsFactSelectClause + " FROM \n");
			invertSql += " " + this.getDbSchemaName()
					+ "observation_FACT obs \n";
			invertSql += tableLookupJoinClause;
			invertSql += (" WHERE \n" + fullWhereClause + ")\n");
			factByProviderSql = invertSql + " EXCEPT \n " + "("
					+ factByProviderSql + ")";
		}

		if (totalItemOccurance > 1) {
			factByProviderSql = "SELECT /*+ FIRST_ROWS(200000) */ a.*, row_number() over(order by obs_patient_num) as rnum FROM (\n"
					+ factByProviderSql;
		} else {
			factByProviderSql = "SELECT /*+ FIRST_ROWS(200000) */ a.*, row_number() over(order by obs_patient_num) as rnum FROM (\n"
					+ factByProviderSql;
		}

		factByProviderSql += "  ) a \n";
		// factByProviderSql += " ORDER BY 2,5,3) a \n";

		return factByProviderSql;
	}

	/**
	 * Function to uses given select, join and where clause to build core pdo
	 * query, without any filter (concept and provider)
	 * 
	 * @param obsFactSelectClause
	 * @param tableLookupJoinClause
	 * @param fullWhereClause
	 * @return
	 */
	private String factQueryWithoutFilter(String obsFactSelectClause,
			String tableLookupJoinClause, String fullWhereClause) {
		String factSql = "SELECT /*+ FIRST_ROWS(200000) */ b.*, ROWNUM rnum FROM (\n";
		factSql += (" SELECT /*+ index (obs OBFACT_PATCON_SDED_NVTV_IDX)*/ "
				+ obsFactSelectClause + " FROM " + this.getDbSchemaName() + "observation_FACT obs\n");

		factSql += tableLookupJoinClause;

		factSql += (" WHERE \n" + fullWhereClause + "\n");
		factSql += " ORDER BY obs.patient_num,obs.start_date,obs.concept_cd,obs.rowid) b \n";

		return factSql;
	}

	/**
	 * Generate fact's join clause for table pdo
	 * 
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return
	 */
	private String getLookupJoinClause(boolean detailFlag, boolean blobFlag,
			boolean statusFlag) {
		String joinClause = " ";

		if (detailFlag) {
			joinClause = " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup modifier_lookup \n"
					+ " ON (obs.modifier_cd = modifier_lookup.code_Cd AND modifier_lookup.column_cd = 'MODIFIER_CD') \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "concept_dimension concept_lookup \n"
					+ " ON (obs.concept_cd = concept_lookup.concept_Cd) \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "provider_dimension provider_lookup \n"
					+ " ON (obs.provider_id = provider_lookup.provider_id) \n"
					+ " left JOIN "
					+ this.getDbSchemaName()
					+ "code_lookup location_lookup \n"
					+ " ON (obs.location_Cd = location_lookup.code_Cd AND location_lookup.column_cd = 'LOCATION_CD') \n";
		}

		return joinClause;
	}

	private void upLoadTempTable(Connection conn) throws SQLException {
		List<String> enumList = inputOptionListHandler.getEnumerationList();
		// ArrayDescriptor desc =
		// ArrayDescriptor.createDescriptor("QT_PDO_QRY_INT_ARRAY",
		// conn1);

		// oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
		// enumList.toArray(new String[] { }));

		// create temp table
		java.sql.Statement tempStmt = conn.createStatement();
		String createTempInputListTable = "create table "
				+ TEMP_PDO_INPUTLIST_TABLE + " ( input_id varchar(100) )";
		tempStmt.executeUpdate(createTempInputListTable);
		// load to temp table
		// TempInputListInsert inputListInserter = new
		// TempInputListInsert(dataSource,TEMP_PDO_INPUTLIST_TABLE);
		// inputListInserter.setBatchSize(100);
		int i = 0;
		for (String singleValue : enumList) {
			tempStmt.addBatch("insert into " + TEMP_PDO_INPUTLIST_TABLE
					+ " values ('" + singleValue + "' )");
			i++;
			if (i % 100 == 0) {
				tempStmt.executeBatch();

			}
		}
		tempStmt.executeBatch();
	}

	private void deleteTempTable(Connection conn) {

		Statement deleteStmt = null;
		try {
			deleteStmt = conn.createStatement();
			conn
					.createStatement()
					.executeUpdate(
							"drop table "
									+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE);
		} catch (SQLException sqle) {
			;
		} finally {
			try {
				deleteStmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Helper function to build unique visit, patient, concept list from
	 * observation fact
	 * 
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
	 * 
	 * @param conn
	 * @param querySql
	 * @return
	 * @throws SQLException
	 */
	private ResultSet executeQuery(Connection conn, String querySql,
			int sqlParamCount, boolean createTempTable) throws SQLException {

		PreparedStatement stmt = conn.prepareStatement(querySql);

		if (inputOptionListHandler.isCollectionId()) {
			for (int i = 1; i <= sqlParamCount; i++) {
				stmt.setInt(i, Integer.parseInt(inputOptionListHandler
						.getCollectionId()));
			}
		} else if (inputOptionListHandler.isEnumerationSet()) {
			if (createTempTable) {
				upLoadTempTable(conn);
			}
		}

		ResultSet resultSet = stmt.executeQuery();

		return resultSet;

		// JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
		// return rowSet;
	}

	/**
	 * Build plain pdo's observation fact
	 * 
	 * @param rowSet
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private ObservationSet buildPDOFact(ResultSet rowSet, String panelName)
			throws SQLException, IOException {
		ObservationSet currentObsFactSetType = new ObservationSet();
		currentObsFactSetType.setPanelName(panelName);

		I2B2PdoFactory.ObservationFactBuilder observationFactBuilder = new I2B2PdoFactory().new ObservationFactBuilder(
				obsFactFactRelated.isSelectDetail(), obsFactFactRelated
						.isSelectBlob(), obsFactFactRelated.isSelectStatus());

		while (rowSet.next()) {
			ObservationType obsFactType = null;

			obsFactType = observationFactBuilder.buildObservationSet(rowSet);

			if (obsFactFactRelated.isSelected()) {
				currentObsFactSetType.getObservation().add(obsFactType);
			}

			addToDistinctList(obsFactType.getEventId().getValue(), obsFactType
					.getPatientId().getValue(), obsFactType.getObserverCd()
					.getValue(), obsFactType.getConceptCd().getValue());
		}

		return currentObsFactSetType;
	}

	/**
	 * Build table pdo observaton fact
	 * 
	 * @param rowSet
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private ObservationSet buildTablePDOFact(ResultSet rowSet, String panelName)
			throws SQLException, IOException {
		// obsFactSetTypeList = new
		// Vector<PatientDataType.ObservationFactSet>();
		boolean detailFlag = obsFactFactRelated.isSelectDetail();
		boolean booleanFlag = obsFactFactRelated.isSelectBlob();
		boolean statusFlag = obsFactFactRelated.isSelectStatus();
		ObservationSet currentObservationSet = new ObservationSet();
		currentObservationSet.setPanelName(panelName);

		RPDRPdoFactory.ObservationFactBuilder observationFactBuilder = new RPDRPdoFactory.ObservationFactBuilder(
				detailFlag, booleanFlag, statusFlag);

		while (rowSet.next()) {
			ObservationType observation = null;
			observation = observationFactBuilder.buildObservationSet(rowSet,
					"i2b2");

			if (obsFactFactRelated.isSelected()) {
				currentObservationSet.getObservation().add(observation);
			}

			String encounterNum = observation.getEventId().getValue();
			String patientNum = observation.getPatientId().getValue();
			String providerId = observation.getObserverCd().getValue();
			String conceptCd = observation.getConceptCd().getValue();
			addToDistinctList(encounterNum, patientNum, providerId, conceptCd);
		}

		return currentObservationSet;
	}

	/**
	 * Patient_Mapping insert code.
	 */
	protected class TempInputListInsert extends BatchSqlUpdate {

		/**
		 * Create a new instance of Patient_MappingInsert.
		 * 
		 * @param ds
		 *            the DataSource to use for the insert
		 */
		protected TempInputListInsert(DataSource ds, String tempTableName) {

			super(ds, "INSERT INTO " + tempTableName + "  (" + "INPUT_ID" + ")"
					+ " VALUES(?)");
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected void insert(String id) {

			// do self insert
			Object[] objs = new Object[] { id };
			super.update(objs);
		}

	}

	private String generatePanelDateConstrain(
			DateConstrainHandler dateConstrainHandler,
			ConstrainDateType dateFrom, ConstrainDateType dateTo)
			throws I2B2Exception {

		String dateFromColumn = null, dateToColumn = null;
		InclusiveType dateFromInclusive = null, dateToInclusive = null;
		XMLGregorianCalendar dateFromValue = null, dateToValue = null;
		String dateConstrainSql = null;

		if (dateFrom != null || dateTo != null) {

			if (dateFrom != null) {
				dateFromInclusive = dateFrom.getInclusive();
				dateFromValue = dateFrom.getValue();

				if (dateFrom.getTime() != null
						&& dateFrom.getTime().name() != null
						&& dateFrom.getTime().name().equalsIgnoreCase(
								dateFrom.getTime().END_DATE.name())) {
					dateFromColumn = "obs.end_date";
				} else {

					dateFromColumn = "obs.start_date";
				}

			}

			if (dateTo != null) {
				dateToInclusive = dateTo.getInclusive();
				dateToValue = dateTo.getValue();

				if (dateTo.getTime() != null
						&& dateTo.getTime().name() != null
						&& dateTo.getTime().name().equalsIgnoreCase(
								dateTo.getTime().END_DATE.name())) {
					dateToColumn = "obs.end_date";
				} else {

					dateToColumn = "obs.start_date";
				}

			}

			dateConstrainSql = dateConstrainHandler
					.constructDateConstrainClause(dateFromColumn, dateToColumn,
							dateFromInclusive, dateToInclusive, dateFromValue,
							dateToValue);
		}
		return dateConstrainSql;
	}
}
