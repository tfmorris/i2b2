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

public class QueryResultPatientGenderCountGenerator extends CRCDAO implements IResultGenerator {
	
	public static final String RESULT_NAME = "PATIENT_GENDER_COUNT_XML";

	public void generateResult(Map param) throws I2B2DAOException { 
		
	    SetFinderConnection sfConn = (SetFinderConnection)param.get("SetFinderConnection");
	    SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory)param.get("SetFinderDAOFactory");
	   // String patientSetId = (String)param.get("PatientSetId");
	    String queryInstanceId = (String)param.get("QueryInstanceId");
	    String TEMP_DX_TABLE = (String)param.get("TEMP_DX_TABLE");
	    String resultInstanceId = (String) param.get("ResultInstanceId");
	    this.setDbSchemaName(sfDAOFactory.getDataSourceLookup().getFullSchema());
	    
	    String demographics_count_sql = "select count(*) as demo_count from " + this.getDbSchemaName() + 
	    	"patient_dimension pd ," +  TEMP_DX_TABLE +" dx where pd.patient_num = dx.patient_num" + 
	    	" and pd.sex_cd=?";
	    String setSizeSql = "select count(1) as total_count from "+ TEMP_DX_TABLE +" " ; 
	    boolean errorFlag = false;
	    int totalCount = 0;
	    try { 
	    	
	    log.debug("Executing[ "+demographics_count_sql +" ]");
	    PreparedStatement stmt = sfConn.prepareStatement(demographics_count_sql);
	    stmt.setString(1, "M");
	    ResultSet resultSet = stmt.executeQuery();
	    resultSet.next();
        int maleCount = resultSet.getInt("demo_count");
        stmt.setString(1, "F");
        resultSet = stmt.executeQuery();
        resultSet.next();
        int femaleCount = resultSet.getInt("demo_count");
        log.debug("Executing[ "+setSizeSql+" ]");
        Statement countStmt = sfConn.createStatement();
        resultSet = countStmt.executeQuery(setSizeSql);
        resultSet.next();
        totalCount = resultSet.getInt("total_count");
        stmt.close();
        countStmt.close();
        int i = 0;
        ResultType resultType = new ResultType();
        resultType.setName("PATIENT_GENDER_COUNT_XML");
        DataType mdataType = new DataType();
        mdataType.setValue(String.valueOf(maleCount));
        mdataType.setColumn( "male_count");
        mdataType.setType("int");
        resultType.getData().add(mdataType);
        
        
        DataType fdataType = new DataType();
        fdataType.setValue(String.valueOf(femaleCount));
        fdataType.setColumn( "female_count");
        fdataType.setType("int");
        resultType.getData().add(fdataType);

        edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
        BodyType bodyType = new BodyType();
        bodyType.getAny().add(of.createResult(resultType));
        ResultEnvelopeType resultEnvelop = new  ResultEnvelopeType();
        resultEnvelop.setBody(bodyType);
        
       JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil(); 
     
       StringWriter strWriter = new StringWriter();
       
       jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelop), strWriter); 
        
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
	    		resultInstanceDao.updatePatientSet(resultInstanceId, QueryStatusTypeId.STATUSTYPE_ID_FINISHED, totalCount);
	    	}
	    }
	} 
}
