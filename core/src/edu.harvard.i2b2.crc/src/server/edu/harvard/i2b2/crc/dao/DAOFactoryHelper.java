package edu.harvard.i2b2.crc.dao;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public  class DAOFactoryHelper {

	public static final String ORACLE = "ORACLE";
	public static final String SQLSERVER = "SQLSERVER";
	DataSourceLookup dataSourceLookup = null;

	public DAOFactoryHelper(String hiveId, String projectId, String ownerId) throws I2B2DAOException {
	    try { 
			DataSourceLookupHelper dsHelper = new DataSourceLookupHelper();
			dataSourceLookup = dsHelper.matchDataSource(hiveId, projectId, ownerId);
		} catch(I2B2Exception i2b2Ex) { 
	    	throw new I2B2DAOException("DataSource lookup error" +i2b2Ex.getMessage(),i2b2Ex);
	    }
	}

	public DAOFactoryHelper(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = dataSourceLookup;
	}

	public IDAOFactory getDAOFactory() throws I2B2DAOException {
		String dataSourceName = dataSourceLookup.getServerType();
		if (dataSourceName.equalsIgnoreCase(ORACLE)) {
			return new OracleDAOFactory(dataSourceLookup);
		} else if (dataSourceName.equalsIgnoreCase(SQLSERVER)) {
			return new OracleDAOFactory(dataSourceLookup);
		} else {
			return null;
		}
	}

}
