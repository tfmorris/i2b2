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

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverType;

import oracle.sql.ArrayDescriptor;

import org.jboss.resource.adapter.jdbc.WrappedConnection;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;


/**
  * Class to support observer section of table pdo query
  * $Id: TablePdoQueryProviderDao.java,v 1.8 2007/08/31 14:40:23 rk903 Exp $
  * @author rkuttan
  */
public class TablePdoQueryProviderDao extends CRCDAO {
    /**
     * Returns observerset for the given list of provider id
     * @param providerIdList
     * @param detailFlag
     * @param blobFlag
     * @param statusFlag
     * @return ObserverSet
     * @throws I2B2DAOException
     */
    public ObserverSet getProviderById(List<String> providerIdList,
        boolean detailFlag, boolean blobFlag, boolean statusFlag)
        throws I2B2DAOException {
        log.debug("input encounter list size " + providerIdList.size());

        ObserverSet observerSet = new ObserverSet();
        RPDRPdoFactory.ProviderBuilder providerBuilder = new RPDRPdoFactory.ProviderBuilder(detailFlag,
                blobFlag, statusFlag);
        Connection conn = null;
        PreparedStatement query = null;

        try {
            conn = getConnection();

            String selectClause = getSelectClause(detailFlag, blobFlag,
                    statusFlag);

            oracle.jdbc.driver.OracleConnection conn1 = (oracle.jdbc.driver.OracleConnection) ((WrappedConnection) conn).getUnderlyingConnection();
            query = conn1.prepareStatement("SELECT " + selectClause +
                    " FROM provider_dimension provider WHERE provider.provider_id IN (SELECT * FROM TABLE (?))");

            ArrayDescriptor desc = ArrayDescriptor.createDescriptor("QT_PDO_QRY_STRING_ARRAY",
                    conn1);

            oracle.sql.ARRAY paramArray = new oracle.sql.ARRAY(desc, conn1,
                    providerIdList.toArray(new String[] {  }));
            query.setArray(1, paramArray);

            ResultSet resultSet = query.executeQuery();

            //JdbcRowSet rowSet = new JdbcRowSetImpl(resultSet);
            while (resultSet.next()) {
                ObserverType observer = providerBuilder.buildObserverSet(resultSet);
                observerSet.getObserver().add(observer);
            }
        } catch (SQLException sqlEx) {
            log.error("", sqlEx);
            throw new I2B2DAOException("sql exception", sqlEx);
        } catch (IOException ioEx) {
            log.error("", ioEx);
            throw new I2B2DAOException("IO exception", ioEx);
        } finally {
            try {
                JDBCUtil.closeJdbcResource(null, query, conn);
            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
            }
        }

        return observerSet;
    }

    /**
     * Function returns select clause based on given flag
     * @param detailFlag
     * @param blobFlag
     * @param statusFlag
     * @return
     */
    private String getSelectClause(boolean detailFlag, boolean blobFlag,
        boolean statusFlag) {
        String selectClause = "";

        selectClause = " provider.provider_id provider_provider_id, provider.provider_path provider_provider_path ";

        if (detailFlag) {
            selectClause += ", provider.name_char provider_name_char ";
        }

        if (blobFlag) {
            selectClause += ", provider.provider_blob provider_provider_blob ";
        }

        if (statusFlag) {
            selectClause += " , provider.update_date provider_update_date, provider.download_date provider_download_date, provider.import_date provider_import_date, provider.sourcesystem_cd provider_sourcesystem_cd, provider.upload_id provider_upload_id ";
        }

        return selectClause;
    }
}
