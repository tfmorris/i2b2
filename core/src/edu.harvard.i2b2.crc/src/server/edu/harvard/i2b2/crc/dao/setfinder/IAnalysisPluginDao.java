package edu.harvard.i2b2.crc.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.db.QtAnalysisPlugin;

public interface IAnalysisPluginDao {

	public QtAnalysisPlugin lookupAnalysisPluginByNameVersionProject(
			String analysisName, String version, String projectId)
			throws I2B2DAOException;

	public QtAnalysisPlugin getAnalysisPluginById(int analysisId)
			throws I2B2DAOException;

}