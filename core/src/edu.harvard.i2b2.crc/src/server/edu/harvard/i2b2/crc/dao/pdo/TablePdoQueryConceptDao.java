/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptType;

/**
 * Class to support concept section of table pdo query
 * $Id: TablePdoQueryConceptDao.java,v 1.8 2007/08/31 14:40:23 rk903 Exp $
 * @author rkuttan
 */
public class TablePdoQueryConceptDao extends CRCDAO {
	
	
	/**
	 * Function returns concepts based on list of concept codes
	 * @param conceptCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return ConceptSet
	 * @throws I2B2DAOException
	 */
	public ConceptSet getConceptByConceptCd(
			List<String> conceptCdList, boolean detailFlag, boolean blobFlag, boolean statusFlag) throws I2B2DAOException {
		ConceptSet conceptSet = new ConceptSet();
		RPDRPdoFactory.ConceptBuilder conceptBuilder = new RPDRPdoFactory.ConceptBuilder(detailFlag, blobFlag, statusFlag);
		log.debug("input concept cd list size " + conceptCdList.size());
		Connection conn = null;
		PreparedStatement query = null;
		try {
			 conn = getConnection();
			ConceptFactRelated conceptFactRelated = new ConceptFactRelated(buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = conceptFactRelated.getSelectClause();
			oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
			String finalSql = "SELECT " + selectClause + "  FROM concept_dimension concept WHERE concept.concept_cd IN (SELECT * FROM TABLE (?))";
			log.debug("Executing sql[" + finalSql + "]");
			 query = conn1.prepareStatement(finalSql);

			ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
					"QT_PDO_QRY_STRING_ARRAY", conn1);

			oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
					conceptCdList.toArray(new String[] {}));
			query.setArray(1, paramArray);

			ResultSet resultSet = query.executeQuery();
			while (resultSet.next()) {
				ConceptType concept =  conceptBuilder.buildConceptSet(resultSet);
				conceptSet.getConcept().add(concept);
			}

		} catch (SQLException sqlEx) {
			log.error("",sqlEx);
			throw new I2B2DAOException("sql exception",sqlEx);
		}catch (IOException ioEx) {
			log.error("",ioEx);
			throw new I2B2DAOException("IO exception",ioEx);
		}finally { 
			try { 
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) { 
				sqlEx.printStackTrace();
			}
		}
		return conceptSet;
	}
}
