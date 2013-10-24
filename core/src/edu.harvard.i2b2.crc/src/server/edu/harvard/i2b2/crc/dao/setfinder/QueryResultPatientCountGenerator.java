package edu.harvard.i2b2.crc.dao.setfinder;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2result.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;

public class QueryResultPatientCountGenerator extends CRCDAO implements IResultGenerator {
	
	

	public void generateResult(Map param) throws I2B2DAOException { 
		
	    SetFinderConnection sfConn = (SetFinderConnection)param.get("SetFinderConnection");
	    SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory)param.get("SetFinderDAOFactory");
	    //String patientSetId = (String)param.get("PatientSetId");
	    String queryInstanceId = (String)param.get("QueryInstanceId");
	    String TEMP_DX_TABLE = (String)param.get("TEMP_DX_TABLE");
	    String resultInstanceId = (String) param.get("ResultInstanceId");
	    this.setDbSchemaName(sfDAOFactory.getDataSourceLookup().getFullSchema());
	    
	    String demographics_count_sql = "select count(*) as patient_count from " + 
	    	TEMP_DX_TABLE; 
	    boolean errorFlag = false;
	    int patientCount = 0;
	    try { 
	    System.out.println(demographics_count_sql);
	    Statement stmt = sfConn.createStatement();
	    ResultSet resultSet = stmt.executeQuery(demographics_count_sql);
	    resultSet.next();
        patientCount = resultSet.getInt("patient_count");
        stmt.close();
        int i = 0;
        ResultType resultType = new ResultType();
        resultType.setName("PATIENT_COUNT_XML");
        DataType mdataType = new DataType();
        mdataType.setValue(String.valueOf(patientCount));
        mdataType.setColumn( "patient_count");
        mdataType.setType("int");
        resultType.getData().add(mdataType);
        
        edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
        BodyType bodyType = new BodyType();
        bodyType.getAny().add(of.createResult(resultType));
        ResultEnvelopeType resultEnvelope = new  ResultEnvelopeType();
        resultEnvelope.setBody(bodyType);
        
       JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil(); 
     
       StringWriter strWriter = new StringWriter();
       
       jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelope), strWriter); 
        
       IXmlResultDao xmlResultDao = sfDAOFactory.getXmlResultDao();
       xmlResultDao.createQueryXmlResult(resultInstanceId, strWriter.toString());
       
        } catch (Exception sqlEx) { 
	    	log.error("QueryResultPatientSetGenerator.generateResult:"+sqlEx.getMessage(),sqlEx);
	    	throw new I2B2DAOException("QueryResultPatientSetGenerator.generateResult:"+sqlEx.getMessage(),sqlEx);
	    }  finally {
	    	IQueryResultInstanceDao resultInstanceDao = sfDAOFactory.getPatientSetResultDAO();
	    
	    	if (errorFlag) { 
	    		resultInstanceDao.updatePatientSet(resultInstanceId, QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
	    	} else { 
	    		resultInstanceDao.updatePatientSet(resultInstanceId, QueryStatusTypeId.STATUSTYPE_ID_FINISHED, patientCount);
	    	}
	    }
	} 
}
