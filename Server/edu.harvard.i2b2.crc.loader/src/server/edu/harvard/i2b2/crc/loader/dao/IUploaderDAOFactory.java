package edu.harvard.i2b2.crc.loader.dao;

import javax.sql.DataSource;

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public interface IUploaderDAOFactory {
	public DataSourceLookup getDataSourceLookup();

	public IConceptDAO getConceptDAO();

	public IPatientDAO getPatientDAO();

	public IPidDAO getPidDAO();

	public IEidDAO getEidDAO();

	public IObservationFactDAO getObservationDAO();

	public UploadStatusDAOI getUploadStatusDAO();

	public IProviderDAO getProviderDAO();

	public IVisitDAO getVisitDAO();

	public DataSource getDataSource();

	public void setDataSource(DataSource dataSource);

}
