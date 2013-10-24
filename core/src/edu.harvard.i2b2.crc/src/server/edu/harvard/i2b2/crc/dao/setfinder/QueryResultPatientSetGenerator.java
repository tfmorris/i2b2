package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;

public class QueryResultPatientSetGenerator extends CRCDAO implements IResultGenerator  {
	
	

	public void generateResult(Map param) throws I2B2DAOException { 
		
	    SetFinderConnection sfConn = (SetFinderConnection)param.get("SetFinderConnection");
	    SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory)param.get("SetFinderDAOFactory");
	    //String patientSetId = (String)param.get("PatientSetId");
	    String queryInstanceId = (String)param.get("QueryInstanceId");
	    String TEMP_DX_TABLE = (String)param.get("TEMP_DX_TABLE");
	    String resultInstanceId = (String) param.get("ResultInstanceId");
	    
	    boolean errorFlag = false;
	    Exception exception = null;
	    int loadCount = 0;
	    try { 
        
        int i = 0;
        IPatientSetCollectionDao patientSetCollectionDao = sfDAOFactory.getPatientSetCollectionDAO();
        patientSetCollectionDao.createPatientSetCollection(resultInstanceId);
       
        
        String patientIdSql = " select patient_num from " + TEMP_DX_TABLE +
        " order by patient_num ";
        Statement readQueryStmt = sfConn.createStatement();
        ResultSet resultSet = readQueryStmt.executeQuery(patientIdSql);
        
        
        
        while (resultSet.next()) {
            long patientNum = resultSet.getLong("patient_num");
            patientSetCollectionDao.addPatient(patientNum);
            i++;
            loadCount++;

            if ((i % 500) == 0) {
                log.debug("Loading [" + loadCount + "] patients" +
                    " for query instanse = " + queryInstanceId);
            }
        }
        readQueryStmt.close();

        log.debug("Total patients loaded for query instance =" +
            queryInstanceId + " is [" + loadCount + "]");
        patientSetCollectionDao.flush();
	    } catch (SQLException sqlEx) { 
	    	exception = sqlEx;
	    	log.error("QueryResultPatientSetGenerator.generateResult:"+sqlEx.getMessage(),sqlEx);
	    	throw new I2B2DAOException("QueryResultPatientSetGenerator.generateResult:"+sqlEx.getMessage(),sqlEx);
	    } finally {
	    	IQueryResultInstanceDao resultInstanceDao = sfDAOFactory.getPatientSetResultDAO();
	    
	    	if (errorFlag) { 
	    		resultInstanceDao.updatePatientSet(resultInstanceId, QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
	    	} else { 
	    		resultInstanceDao.updatePatientSet(resultInstanceId, QueryStatusTypeId.STATUSTYPE_ID_FINISHED, loadCount);
	    	}
	    }
	}
}
