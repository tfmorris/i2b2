package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface ITablePdoQueryProviderDao {

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
			throws I2B2DAOException;

}