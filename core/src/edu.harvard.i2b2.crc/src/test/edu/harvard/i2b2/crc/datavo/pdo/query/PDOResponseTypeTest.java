package edu.harvard.i2b2.crc.datavo.pdo.query;

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2result.DataType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultEnvelopeType;
import edu.harvard.i2b2.crc.datavo.i2b2result.ResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.XmlValueType;

public class PDOResponseTypeTest {

	@Test
	public void testPDOMarshall() throws Exception {
		PatientDataType patientData = new PatientDataType();
		ObservationType observation = new ObservationType();
		//observation.setPatientId("patientid");
		//observation.setPatientIdSource("source");
		PatientIdType patientIdType = new PatientIdType();
		//patientIdType.setSource("soruce"); 
		patientIdType.setValue("patient_id");
		observation.setPatientId(patientIdType);
		ObservationSet observationSet = new ObservationSet();
		observationSet.getObservation().add(observation);
		patientData.getObservationSet().add(observationSet);

		try {
			JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil();
			edu.harvard.i2b2.crc.datavo.pdo.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.pdo.ObjectFactory();

			StringWriter strWriter = new StringWriter();
			jaxbUtil.marshaller(of.createPatientData(patientData), strWriter);
			System.out.print(strWriter.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testResult() throws Exception { 
		DataType dataType = new DataType();
        dataType.setValue("100");
        dataType.setColumn( "count");
        dataType.setType("int");
       
        ResultType resultType = new ResultType();
        resultType.setName("PATIENT_DEMOGRAPHICS_COUNT");
        resultType.getData().add(dataType);
        edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory of = new edu.harvard.i2b2.crc.datavo.i2b2result.ObjectFactory();
        
        edu.harvard.i2b2.crc.datavo.i2b2result.BodyType bodyType = new edu.harvard.i2b2.crc.datavo.i2b2result.BodyType();
        bodyType.getAny().add(of.createResult(resultType));
        ResultEnvelopeType resultEnvelopeType = new ResultEnvelopeType();
        resultEnvelopeType.setBody(bodyType);
        
       
        
        StringWriter strWriter = new StringWriter();
        JAXBUtil jaxbUtil = CRCJAXBUtil.getJAXBUtil() ;
        jaxbUtil.marshaller(of.createI2B2ResultEnvelope(resultEnvelopeType),strWriter);
        
        System.out.println("Results marshalled" + strWriter.toString());
        JAXBElement jaxbElement = jaxbUtil.unMashallFromString(strWriter.toString());
        ResultEnvelopeType resultEnvelopeType1 = (ResultEnvelopeType)jaxbElement.getValue();
        JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();
        ResultType umResultType = (ResultType)helper.getObjectByClass(resultEnvelopeType1.getBody().getAny(), ResultType.class);
        
        XmlValueType xmlValueType = new XmlValueType();
        xmlValueType.getContent().add(resultEnvelopeType);
        
        ResultEnvelopeType resultEnvelopeType2 = (ResultEnvelopeType)helper.getObjectByClass(xmlValueType.getContent(), ResultEnvelopeType.class);
        if (resultEnvelopeType2 == null) { 
        	System.out.println("null");
        }
        
	}
}
