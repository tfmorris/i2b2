package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface ITablePdoQueryConceptDao {

	/**
	 * Function returns concepts based on list of concept codes
	 * @param conceptCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return ConceptSet
	 * @throws I2B2DAOException
	 */
	public ConceptSet getConceptByConceptCd(List<String> conceptCdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

}