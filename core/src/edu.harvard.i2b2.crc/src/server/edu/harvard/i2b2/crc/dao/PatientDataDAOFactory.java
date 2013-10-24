package edu.harvard.i2b2.crc.dao;

import edu.harvard.i2b2.crc.dao.pdo.IObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.IPdoQueryVisitDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.ITablePdoQueryVisitDao;
import edu.harvard.i2b2.crc.dao.pdo.ObservationFactDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.PdoQueryVisitDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryConceptDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryPatientDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryProviderDao;
import edu.harvard.i2b2.crc.dao.pdo.TablePdoQueryVisitDao;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;

public interface PatientDataDAOFactory {
	public IObservationFactDao getObservationFactDAO();
	public IPdoQueryConceptDao getPdoQueryConceptDAO();
	public IPdoQueryPatientDao getPdoQueryPatientDAO();
	public IPdoQueryProviderDao getPdoQueryProviderDAO();
	public IPdoQueryVisitDao getPdoQueryVisitDAO();
	public ITablePdoQueryConceptDao getTablePdoQueryConceptDAO();
	public ITablePdoQueryPatientDao getTablePdoQueryPatientDAO();
	public ITablePdoQueryProviderDao getTablePdoQueryProviderDAO();
	public ITablePdoQueryVisitDao getTablePdoQueryVisitDAO();
	public DataSourceLookup getDataSourceLookup() ;
}
