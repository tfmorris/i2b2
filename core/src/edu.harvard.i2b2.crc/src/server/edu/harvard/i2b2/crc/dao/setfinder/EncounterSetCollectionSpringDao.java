/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtPatientEncCollection;
import edu.harvard.i2b2.crc.datavo.db.QtPatientSetCollection;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;

/**
 * Class to support batch inserts of patients to PatientSetCollection This class
 * uses single Session to persist all the patients to collection. To have
 * limited memory usage, it will flush and clear the session manually for every
 * 1000 inserts. $Id: PatientSetCollectionDao.java,v 1.4 2007/08/31 14:44:26
 * rk903 Exp $
 * 
 * @author rkuttan
 * @see QtPatientSetCollection
 */
public class EncounterSetCollectionSpringDao extends CRCDAO implements
		IEncounterSetCollectionDao {
	/** patient set collection index * */
	private long setIndex = 0;
	private int batchDataIndex = 0;
	private JdbcTemplate jdbcTemplate = null;
	private String insert_sql = "";

	QtPatientEncCollection[] patientEncColl = null;
	/** master table for patient set collection * */
	QtQueryResultInstance resultInstance = null;
	String resultInstanceId = null;
	private static final int INITIAL_ARRAY_SIZE = 1100;
	private DataSourceLookup dataSourceLookup = null;

	private SQLServerSequenceDAO sqlServerSequenceDao = null;

	/**
	 * Construc with patientset id Initialize hibernate session and creates
	 * Query reuslt instance class
	 * 
	 * @param patientSetId
	 */
	public EncounterSetCollectionSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.ORACLE)) {
			insert_sql = "insert into "
					+ getDbSchemaName()
					+ "qt_patient_enc_collection(patient_enc_coll_id,result_instance_id,set_index,patient_num,encounter_num) values ("
					+ getDbSchemaName() + "QT_SQ_QPER_PECID.nextval,?,?,?,?)";
		} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			insert_sql = "insert into "
					+ getDbSchemaName()
					+ "qt_patient_enc_collection(result_instance_id,set_index,patient_num,encounter_num) values (?,?,?,?)";
		}
		sqlServerSequenceDao = new SQLServerSequenceDAO(dataSource,
				dataSourceLookup);
		resultInstance = new QtQueryResultInstance();
		patientEncColl = new QtPatientEncCollection[INITIAL_ARRAY_SIZE];

	}

	public void createPatientEncCollection(String resultInstanceId) {
		resultInstance = new QtQueryResultInstance();
		resultInstance.setResultInstanceId(resultInstanceId);

	}

	public String getResultInstanceId() {
		return resultInstance.getResultInstanceId();
	}

	/**
	 * function to add patient to patient set without out creating new db
	 * session
	 * 
	 * @param patientId
	 */
	public void addEncounter(long encounterId, long patientId) {
		setIndex++;

		QtPatientEncCollection collElement = new QtPatientEncCollection();
		int patientSetCollId = 0;
		collElement.setPatientId(patientId);
		collElement.setEncounterId(encounterId);
		collElement.setQtQueryResultInstance(resultInstance);
		collElement.setSetIndex(setIndex);

		patientEncColl[batchDataIndex++] = collElement;

		if ((setIndex % 1000) == 0) {
			InsertStatementSetter batchSetter = new InsertStatementSetter(
					patientEncColl, batchDataIndex);
			jdbcTemplate.batchUpdate(insert_sql, batchSetter);

			Arrays.fill(patientEncColl, null);
			batchDataIndex = 0;
		}
	}

	/**
	 * Call this function at the end. i.e. after loading all patient with
	 * addPatient function, finally call this function to clear session
	 */
	public void flush() {
		InsertStatementSetter batchSetter = new InsertStatementSetter(
				patientEncColl, batchDataIndex);
		jdbcTemplate.batchUpdate(insert_sql, batchSetter);
		Arrays.fill(patientEncColl, null);
		batchDataIndex = 0;
		setIndex = 0;
	}

	class InsertStatementSetter implements BatchPreparedStatementSetter {

		private QtPatientEncCollection[] data;
		private int batchSize = 0;

		public InsertStatementSetter(QtPatientEncCollection[] data,
				int batchSize) {
			this.data = data;
			this.batchSize = batchSize;
		}

		public int getBatchSize() {
			return batchSize;
		}

		// this is called for each row
		public void setValues(PreparedStatement ps, int i) throws SQLException {

			// ps.setLong(1, data[i].getPatientSetCollId()); // set first value
			ps.setInt(1, Integer.parseInt(data[i].getQtQueryResultInstance()
					.getResultInstanceId()));
			ps.setLong(2, data[i].getSetIndex());
			ps.setLong(3, data[i].getPatientId());
			ps.setLong(4, data[i].getEncounterId());

		}

	}

}
