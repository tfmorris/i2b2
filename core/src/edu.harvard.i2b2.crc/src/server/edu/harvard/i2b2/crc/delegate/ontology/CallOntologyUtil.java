package edu.harvard.i2b2.crc.delegate.ontology;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptsType;
import edu.harvard.i2b2.crc.datavo.ontology.GetTermInfoType;
import edu.harvard.i2b2.crc.datavo.ontology.ObjectFactory;
import edu.harvard.i2b2.crc.delegate.pm.PMServiceClient;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class CallOntologyUtil {
	
	private SecurityType securityType = null; 
	private String projectId = null;
	private String ontologyUrl = null;
	
	public CallOntologyUtil(String requestXml) throws JAXBUtilException, I2B2Exception { 
		 JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(requestXml);
		 RequestMessageType request = (RequestMessageType) responseJaxb.getValue();
		 this.securityType = request.getMessageHeader().getSecurity();
		 this.projectId = request.getMessageHeader().getProjectId();
		 this.ontologyUrl = QueryProcessorUtil.getInstance().getOntologyUrl();
		 
	}
	
	public CallOntologyUtil(SecurityType securityType, String projectId) throws I2B2Exception { 
			this.securityType = securityType;
			this.projectId = projectId;
			this.ontologyUrl = QueryProcessorUtil.getInstance().getOntologyUrl();
			System.out.println(ontologyUrl);
	}
	
	public CallOntologyUtil(String ontologyUrl,SecurityType securityType, String projectId) throws I2B2Exception { 
		this(securityType, projectId);
		this.ontologyUrl = ontologyUrl;
	}
	
	
	public ConceptType callOntology(String itemKey) throws XMLStreamException, JAXBUtilException, AxisFault { 
		RequestMessageType requestMessageType = getI2B2RequestMessage(itemKey);
		OMElement requestElement = buildOMElement(requestMessageType);
		System.out.println(requestElement);
		OMElement response = getServiceClient().sendReceive(requestElement);
		ConceptType conceptType = getConceptFromResponse(response);
		return conceptType;
	}
	
	private ConceptType getConceptFromResponse(OMElement response) throws JAXBUtilException { 
		 JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil().unMashallFromString(response.toString());
		 ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		 System.out.println(response);

		 JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		 ConceptsType conceptsType = (ConceptsType) helper.getObjectByClass(r.getMessageBody().getAny(),ConceptsType.class);
		 return conceptsType.getConcept().get(0);
	}
	
	private OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException { 
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(hiveof.createRequest(requestMessageType), strWriter);
    	//getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();
		
		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement request = builder.getDocumentElement();
		return request;
	}
	
	
	private RequestMessageType getI2B2RequestMessage(String conceptPath) { 
		QueryProcessorUtil queryUtil =QueryProcessorUtil.getInstance();
    	MessageHeaderType messageHeaderType = (MessageHeaderType)queryUtil.getSpringBeanFactory().getBean("message_header");
    	messageHeaderType.setSecurity(securityType);
    	messageHeaderType.setProjectId(projectId);
    	
    	messageHeaderType.setReceivingApplication(messageHeaderType.getSendingApplication());
       FacilityType facilityType = new FacilityType();
       facilityType.setFacilityName("sample");
       messageHeaderType.setSendingFacility(facilityType);
       messageHeaderType.setReceivingFacility(facilityType);	
    	//build message body
    	GetTermInfoType getTermInfo = new GetTermInfoType();
    	getTermInfo.setSelf(conceptPath);
    	//max="300" hiddens="false" synonyms="false" type="core" blob="true"
    	getTermInfo.setMax(300);
    	getTermInfo.setHiddens(false);
    	getTermInfo.setSynonyms(false);
    	getTermInfo.setType("core");
    	getTermInfo.setBlob(true);
    	
   
    	
    	RequestMessageType requestMessageType = new RequestMessageType();
     	ObjectFactory of = new ObjectFactory();
    	BodyType bodyType = new BodyType();
    	bodyType.getAny().add(of.createGetTermInfo(getTermInfo));
    	requestMessageType.setMessageBody(bodyType);
    	
    	requestMessageType.setMessageHeader(messageHeaderType);
    	
    	RequestHeaderType requestHeader = new RequestHeaderType();
    	requestHeader.setResultWaittimeMs(180000);
    	requestMessageType.setRequestHeader(requestHeader);
    	
    	return requestMessageType;
		
	}
	
	private ServiceClient getServiceClient() { 
		//call
    	ServiceClient serviceClient = OntologyServiceClient.getServiceClient();
    	
    	Options options = new Options();
		options.setTo( new EndpointReference(ontologyUrl));
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(Constants.Configuration.ENABLE_REST,
				Constants.VALUE_TRUE);
	 	options.setTimeOutInMilliSeconds(50000);
		serviceClient.setOptions(options);
		return serviceClient;
		
	}
	
	
	  
	  
	  public static void main(String args[]) throws Exception { 
		  SecurityType securityType = new SecurityType();
	    	securityType.setDomain("Demo");
	    	securityType.setUsername("lcp");
	    	securityType.setPassword("lcpuser");
	    	String projectId ="asthma";
		CallOntologyUtil ontologyUtil = new CallOntologyUtil(securityType,projectId);
		ConceptType concept = ontologyUtil.callOntology("\\\\i2b2\\i2b2");
		System.out.println(concept.getTablename());
;	  }
}
