package edu.harvard.i2b2.crc.dao.pdo;

import java.util.List;

import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.common.exception.I2B2DAOException;

public interface IPdoQueryConceptDao {

	/**
	 * Get concepts detail from concept code list
	 * @param conceptCdList
	 * @param detailFlag
	 * @param blobFlag
	 * @param statusFlag
	 * @return {@link PatientDataType.ConceptDimensionSet}
	 * @throws I2B2DAOException
	 */
	public ConceptSet getConceptByConceptCd(List<String> conceptCdList,
			boolean detailFlag, boolean blobFlag, boolean statusFlag)
			throws I2B2DAOException;

}