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
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;


/**
 * Class to support provider section of plain pdo query
 * $Id: PdoQueryProviderDao.java,v 1.8 2007/08/31 14:40:23 rk903 Exp $
 * @author rkuttan
 */
public class PdoQueryProviderDao extends CRCDAO {
	
	/**
	 * Function to return provider/observer section of plain pdo 
	 * for the given id list 
	 * @param providerIdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return PatientDataType.ProviderDimensionSet 
	 * @throws I2B2DAOException
	 */
	public ObserverSet getProviderById(
			List<String> providerIdList,boolean detailFlag, boolean blobFlag, boolean statusFlag) throws I2B2DAOException  {
		ObserverSet providerDimensionSet = new ObserverSet();
		log.debug("provider list size " + providerIdList.size());
		Connection conn = null;
		PreparedStatement query = null;
		try {
			 conn =  getConnection();
			ProviderFactRelated providerRelated = new ProviderFactRelated(buildOutputOptionType(detailFlag, blobFlag, statusFlag));
			String selectClause = providerRelated.getSelectClause();
			
			oracle.jdbc.driver.OracleConnection conn1 = 		(oracle.jdbc.driver.OracleConnection)((WrappedConnection)conn).getUnderlyingConnection();
			String finalSql = "SELECT " + selectClause + " FROM provider_dimension provider WHERE provider.provider_id IN (SELECT * FROM TABLE (?))";
			log.debug("Executing sql["+ finalSql + "]");
			query = conn1.prepareStatement(finalSql);

			ArrayDescriptor desc = ArrayDescriptor.createDescriptor(
					"QT_PDO_QRY_STRING_ARRAY", conn1);
			oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
					providerIdList.toArray(new String[] {}));
			query.setArray(1, paramArray);
			ResultSet resultSet = query.executeQuery();
			I2B2PdoFactory.ProviderBuilder providerBuilder  = new I2B2PdoFactory().new ProviderBuilder(detailFlag,blobFlag,statusFlag);
			while (resultSet.next()) {
				ObserverType providerDimensionType = providerBuilder.buildObserverSet(resultSet);
				providerDimensionSet.getObserver().add(providerDimensionType);
			}

		} catch (SQLException sqlEx) {
			log.error("",sqlEx);
			throw new I2B2DAOException("sql exception",sqlEx);
		}catch (IOException ioex) {
			log.error("",ioex);
			throw new I2B2DAOException("io exception",ioex);
		}finally { 
			try { 
				JDBCUtil.closeJdbcResource(null, query, conn);
			} catch (SQLException sqlEx) { 
				sqlEx.printStackTrace();
			}
		}
		return providerDimensionSet;
	}

	
	
	
	
	
}
