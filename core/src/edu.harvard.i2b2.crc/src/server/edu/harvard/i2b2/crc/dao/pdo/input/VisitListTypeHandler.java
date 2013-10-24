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
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;

/**
 * Handler class for visit/event list type to generate "where" clause for pdo
 * request $Id: VisitListTypeHandler.java,v 1.11 2008/11/05 16:11:45 rk903 Exp $
 * 
 * @author rkuttan
 */
public class VisitListTypeHandler extends CRCDAO implements
		IInputOptionListHandler {
	private EventListType visitListType = null;
	private int minIndex = 0;
	private int maxIndex = 0;
	private String encounterSetCollId = "";
	private List<String> encounterNumList = null;
	private DataSourceLookup dataSourceLookup = null;

	/**
	 * Constructor accepts {@link EventListType}
	 * 
	 * @param visitListType
	 * @throws I2B2DAOException
	 */
	public VisitListTypeHandler(DataSourceLookup dataSourceLookup,
			EventListType visitListType) throws I2B2DAOException {
		if (visitListType == null) {
			throw new I2B2DAOException("Visit List Type is null");
		}

		this.dataSourceLookup = dataSourceLookup;
		setDbSchemaName(dataSourceLookup.getFullSchema());
		this.visitListType = visitListType;

		if (visitListType.getMin() != null) {
			minIndex = visitListType.getMin();
		}

		if (visitListType.getMax() != null) {
			maxIndex = visitListType.getMax();
		}
	}

	public int getMinIndex() {
		return minIndex;
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	public boolean isCollectionId() {
		if (visitListType.getPatientEventCollId() != null) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEnumerationSet() {
		if ((visitListType.getEventId() != null)
				&& (visitListType.getEventId().size() > 0)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isEntireSet() {
		if (visitListType.getEntireEventSet() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Function to generate "where" clause for visit/event list
	 */
	public String generateWhereClauseSql() {
		String sqlString = null;

		if (visitListType.getPatientEventCollId() != null) {
			// set patient set coll id
			this.encounterSetCollId = visitListType.getPatientEventCollId();

			// set sql string
			sqlString = "select eset.encounter_num from "
					+ this.getDbSchemaName()
					+ "qt_patient_enc_collection eset where eset.result_instance_id = ? ";

			if (minIndex <= maxIndex) {
				sqlString += (" and eset.set_index between " + minIndex
						+ " and " + maxIndex);
			}
		} else if ((visitListType.getEventId() != null)
				&& (visitListType.getEventId().size() > 0)) {
			ArrayList<String> encounterNumArrayList = new ArrayList<String>();

			for (EventListType.EventId encounterNum : visitListType
					.getEventId()) {
				// TODO see if we can use index value from encounterNum
				encounterNumArrayList.add(encounterNum.getValue());
			}

			if (maxIndex > visitListType.getEventId().size()) {
				// log.warn("max size is more than list size");
				maxIndex = visitListType.getEventId().size();
			}

			// set int List
			if (minIndex < maxIndex) {
				this.encounterNumList = encounterNumArrayList.subList(minIndex,
						maxIndex);
			} else if (minIndex == maxIndex && minIndex > 0) {
				// check if maxIndex is equal to last index
				if (maxIndex == visitListType.getEventId().size() - 1) {
					this.encounterNumList = new ArrayList();
					this.encounterNumList.add(encounterNumArrayList
							.get(maxIndex));
				} else {
					this.encounterNumList = encounterNumArrayList.subList(
							minIndex, maxIndex);
				}
			} else {
				maxIndex = encounterNumArrayList.size();
				this.encounterNumList = encounterNumArrayList.subList(minIndex,
						maxIndex);
			}

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				// set sql string
				sqlString = " SELECT * FROM TABLE (?) ";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				sqlString = " select encounter_num from ( select encounter_num,row_number() over(order by encounter_num) as rnum  from "
						+ SQLServerFactRelatedQueryHandler.TEMP_PDO_INPUTLIST_TABLE
						+ " ) as v ";
				if (minIndex <= maxIndex) {
					sqlString += " where rnum between " + minIndex + " and "
							+ maxIndex;
				}
			}
		} else if (visitListType.getEntireEventSet() != null) {
			// by default get first 100 rows
			if ((minIndex == 0) && (maxIndex == 0)) {
				minIndex = 0;
				maxIndex = 100;
			}
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				// do nothing
				sqlString = " select encounter_num from (select a.*, ROWNUM rnum from ( select encounter_num from "
						+ this.getDbSchemaName()
						+ "visit_dimension  order by encounter_num) a "
						+ "	where ROWNUM<="
						+ maxIndex
						+ " ) where  rnum>="
						+ minIndex;
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				sqlString = " select encounter_num from ( select encounter_num,row_number() over(order by encounter_num) as rnum  from "
						+ this.getDbSchemaName()
						+ "visit_dimension ) as v "
						+ " where rnum between "
						+ minIndex
						+ " and "
						+ maxIndex;
			}
		}

		return sqlString;
	}

	public String generatePatentSql() {
		String sqlString = null;

		if (visitListType.getPatientEventCollId() != null) {
			// set patient set coll id
			this.encounterSetCollId = visitListType.getPatientEventCollId();

			// set sql string
			sqlString = "select eset.patient_num from "
					+ getDbSchemaName()
					+ "qt_patient_enc_collection eset where eset.result_instance_id = ? ";

			if (minIndex < maxIndex) {
				sqlString += (" and eset.set_index betweeen " + minIndex
						+ " and " + maxIndex);
			}
		}

		return sqlString;
	}

	public List<String> getEnumerationList() {
		return this.encounterNumList;
	}

	public String getCollectionId() {
		return encounterSetCollId;
	}
}
