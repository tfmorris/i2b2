package edu.harvard.i2b2.crc.dao.setfinder;

import java.util.List;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultInstance;

public interface IQueryResultInstanceDao {

	/**
	 * Function to create result instance for given
	 * query instance id. The result instance status is set to
	 * running. Use updatePatientSet function to change the status to completed or error
	 * @param queryInstanceId
	 * @return
	 */
	public String createPatientSet(String queryInstanceId,String resultName);

	/**
	 * Function used to update result instance
	 * Particularly its status and size
	 * @param resultInstanceId
	 * @param statusTypeId
	 * @param setSize
	 */
	public void updatePatientSet(String resultInstanceId, int statusTypeId,
			int setSize);


	/**
	 * Return list of query result instance by query instance id
	 * @param queryInstanceId
	 * @return  List<QtQueryResultInstance>
	 */
	@SuppressWarnings("unchecked")
	 public List<QtQueryResultInstance> getResultInstanceList(
		        String queryInstanceId);
	/**
	 * Return list of query result instance by query result id
	 * 
	 * @param queryResultId
	 * @return QtQueryResultInstance
	 */
	public QtQueryResultInstance getResultInstanceById(
			String queryResultId) throws I2B2DAOException;
	
	/**
	 * Return list of query result instance by query instance id and result name
	 * 
	 * @param queryInstanceId
	 * @param resultName
	 * @return QtQueryResultInstance
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultInstance getResultInstanceByQueryInstanceIdAndName(
			String queryInstanceId,String resultName) ;
}