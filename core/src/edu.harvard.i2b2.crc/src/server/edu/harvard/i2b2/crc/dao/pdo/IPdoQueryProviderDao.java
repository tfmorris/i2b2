package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface IPdoQueryProviderDao {

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
	public ObserverSet getProviderById(List<String> providerIdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

}