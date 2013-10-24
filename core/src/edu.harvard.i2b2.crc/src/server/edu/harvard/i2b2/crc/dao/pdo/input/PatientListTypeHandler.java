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

import java.util.ArrayList;
import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;

/**
 * Handler class for patient list type to generate "where" clause for pdo
 * request $Id: PatientListTypeHandler.java,v 1.8 2008/06/10 14:59:04 rk903 Exp
 * $
 * 
 * @author rkuttan
 */
public class PatientListTypeHandler extends CRCDAO implements
		IInputOptionListHandler {
	private PatientListType patientListType = null;
	private int minIndex = 0;
	private int maxIndex = 0;
	private String patientSetCollId = "";
	private List<String> patientNumList = null;
	private DataSourceLookup dataSourceLookup = null;

	/**
	 * Constructor accepts {@link PatientListType}
	 * 
	 * @param patientListType
	 * @throws I2B2DAOException
	 */
	public PatientListTypeHandler(DataSourceLookup dataSourceLookup,
			PatientListType patientListType) throws I2B2DAOException {
		if (patientListType == null) {
			throw new I2B2DAOException("Patient List Type is null");
		}
		this.dataSourceLookup = dataSourceLookup;
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.patientListType = patientListType;

		if (patientListType.getMin() != null) {
			minIndex = patientListType.getMin();
		}

		if (patientListType.getMax() != null) {
			maxIndex = patientListType.getMax();
		}
	}

	public int getMinIndex() {
		return minIndex;
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	/**
	 * Function to generate "where" clause for patient list
	 */
	public String generateWhereClauseSql() {
		String sqlString = null;

		if (patientListType.getPatientSetCollId() != null) {
			// set patient set coll id
			this.patientSetCollId = patientListType.getPatientSetCollId();

			// set sql string
			sqlString = "select pset.patient_num from "
					+ this.getDbSchemaName()
					+ "qt_patient_set_collection pset where pset.result_instance_id =  ?  ";

			if (minIndex <= maxIndex) {
				sqlString += (" and pset.set_index between " + minIndex
						+ " and " + maxIndex);
			}
		} else if ((patientListType.getPatientId() != null)
				&& (patientListType.getPatientId().size() > 0)) {
			ArrayList<String> patientNumArrayList = new ArrayList<String>(
					patientListType.getPatientId().size());

			for (PatientListType.PatientId patientNum : patientListType
					.getPatientId()) {
				// TODO see if we can use index value from patientNum
				patientNumArrayList.add(patientNum.getValue());

			}

			if (maxIndex > patientListType.getPatientId().size()) {
				maxIndex = patientListType.getPatientId().size();
			}

			// set int List
			if (minIndex < maxIndex) {
				this.patientNumList = patientNumArrayList.subList(minIndex,
						maxIndex);
			} else if (minIndex == maxIndex && minIndex > 0) {
				// check if maxIndex is equal to last index
				if (maxIndex == patientListType.getPatientId().size() - 1) {
					this.patientNumList = new ArrayList();
					this.patientNumList.add(patientNumArrayList.get(maxIndex));
				} else {
					this.patientNumList = patientNumArrayList.subList(minIndex,
							maxIndex);
				}

			} else {
				maxIndex = patientNumArrayList.size();
				this.patientNumList = patientNumArrayList.subList(minIndex,
						maxIndex);
			}

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				// set sql string
				sqlString = " SELECT * FROM TABLE (cast (? as QT_PDO_QRY_STRING_ARRAY)) ";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {

				sqlString = "  select distinct input_id from ( "
						+ " select input_id,row_number() over(order by input_id) as rnum from "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ " as tpdo  ) as tpdo1 ";
				if (minIndex <= maxIndex) {
					sqlString += " where tpdo1.rnum between  " + minIndex
							+ " and " + maxIndex;
				}
			}
		} else if (patientListType.getEntirePatientSet() != null) {
			// by default get first 100 rows
			if ((minIndex == 0) && (maxIndex == 0)) {
				minIndex = 0;
				maxIndex = 100;
			}

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				// do nothing
				sqlString = " select patient_num from (select p.*, ROWNUM rnum from ( select patient_num from "
						+ this.getDbSchemaName()
						+ "patient_dimension  order by patient_num) p "
						+ "	where ROWNUM<="
						+ maxIndex
						+ " ) where  rnum>="
						+ minIndex;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				sqlString = "	select patient_num from (select *, ROW_number() over (order by patient_num asc) as  rnum "
						+ " from "
						+ this.getDbSchemaName()
						+ "patient_dimension p) as p1  where rnum between  "
						+ minIndex + "  and  " + maxIndex;
			}
		}

		return sqlString;
	}

	public List<String> getIntListFromPatientNumList() {
		return this.patientNumList;
	}

	public String getCollectionId() {
		return patientSetCollId;
	}

	public List<String> getEnumerationList() {
		return this.patientNumList;

	}

	public boolean isCollectionId() {
		if (patientListType.getPatientSetCollId() != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEntireSet() {
		if (patientListType.getEntirePatientSet() != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEnumerationSet() {
		if ((patientListType.getPatientId() != null)
				&& (patientListType.getPatientId().size() > 0)) {
			return true;
		} else {
			return false;
		}
	}
}
