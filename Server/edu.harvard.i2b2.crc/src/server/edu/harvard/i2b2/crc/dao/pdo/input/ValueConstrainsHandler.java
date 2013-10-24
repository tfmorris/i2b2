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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.pdo.query.ItemType;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

/**
 * Class to handle value constrains. Generates sql where clause based on the
 * list of value constrains.
 * 
 * @author rkuttan
 */
public class ValueConstrainsHandler {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	public String constructValueConstainClause(
			List<ItemType.ConstrainByValue> valueConstrainList)
			throws I2B2Exception {
		String fullConstrainSql = "";

		for (ItemType.ConstrainByValue valueConstrain : valueConstrainList) {
			ConstrainValueType valueType = valueConstrain.getValueType();
			ConstrainOperatorType operatorType = valueConstrain
					.getValueOperator();
			String value = valueConstrain.getValueConstraint();

			String constrainSql = null;
			// check if value type is not null
			if (valueType == null) {
				continue;
			}
			if (valueType.equals(ConstrainValueType.TEXT)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LIKE.value())) {
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char LIKE '"
							+ value.replaceAll("'", "''") + "%'";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   IN ("
							+ value + ")";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					value = SqlClauseUtil.buildBetweenClause(value);
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   BETWEEEN "
							+ value;
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = " obs.valtype_cd = 'T' AND obs.tval_char   <> '"
							+ value.replaceAll("'", "''") + "' ";
				} else {
					throw new I2B2Exception(
							"Error TEXT value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.NUMBER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				value.replaceAll("'", "''");
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GT.value())) {
					constrainSql = " ((obs.valtype_cd = 'N' AND obs.nval_num > "
							+ value
							+ " AND obs.tval_char IN ('E','GE')) OR (obs.valtype_cd = 'N' AND obs.nval_num >= "
							+ value + " AND obs.tval_char = 'G' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GE.value())) {
					constrainSql = " obs.valtype_cd = 'N' AND obs.nval_num >= "
							+ value + " AND obs.tval_char IN ('E','GE','G') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LT.value())) {
					constrainSql = " ((obs.valtype_cd = 'N' AND obs.nval_num < "
							+ value
							+ " AND obs.tval_char IN ('E','LE')) OR (obs.valtype_cd = 'N' AND obs.nval_num <= "
							+ value + " AND obs.tval_char = 'L' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LE.value())) {
					constrainSql = " obs.valtype_cd = 'N' AND obs.nval_num <= "
							+ value + " AND obs.tval_char IN ('E','LE','L') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = " obs.valtype_cd = 'N' AND obs.nval_num = "
							+ value + " AND obs.tval_char='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					value = SqlClauseUtil.buildBetweenClause(value);
					constrainSql = " obs.valtype_cd = 'N' AND obs.nval_num BETWEEN  "
							+ value + " AND obs.tval_char ='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = " ((obs.valtype_cd = 'N' AND obs.nval_num <> "
							+ value
							+ " AND obs.tval_char <> 'NE') OR (obs.valtype_cd = 'N' AND obs.nval_num = "
							+ value + " AND obs.tval_char ='NE' )) ";
				} else {
					throw new I2B2Exception(
							"Error NUMBER value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.FLAG)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = " obs.valueflag_cd = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = "  obs.valueflag_cd <> '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constrainSql = " obs.valueflag_cd IN " + value;
				} else {
					throw new I2B2Exception(
							"Error FLAG value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.MODIFIER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				if (value != null) {
					if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.EQ.value())) {
						constrainSql = " obs.valtype_cd = 'M' and obs.tval_char = '"
								+ value.replaceAll("'", "''") + "' ";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.NE.value())) {
						constrainSql = " obs.valtype_cd = 'M' and obs.tval_char <> '"
								+ value.replaceAll("'", "''") + "' ";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.IN.value())) {
						value = SqlClauseUtil.buildINClause(value, true);
						constrainSql = " obs.valtype_cd = 'M' and obs.tval_char IN "
								+ value + " ";
					}

				}

			} else {
				throw new I2B2Exception(
						"Error value constrain, invalid value type ("
								+ valueType.toString() + ")");
			}

			if (constrainSql != null) {
				if (fullConstrainSql.length() > 0) {
					fullConstrainSql += " AND ";
				}

				fullConstrainSql += constrainSql;
			}
		}

		return fullConstrainSql;
	}

}
