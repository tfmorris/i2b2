package edu.harvard.i2b2.ontology.delegate.crc;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.DataFormatType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.DataListType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.GetUploadInfoRequestType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.InputOptionListType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadDataResponseType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadOptionType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.LoadType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.OutputOptionListType;
import edu.harvard.i2b2.ontology.datavo.crcloader.query.PublishDataRequestType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

public class CallCRCUtil {

	private SecurityType securityType = null;
	private String projectId = null;
	private String crcUrl = null;
	OntologyUtil ontologyUtil = OntologyUtil.getInstance();

	private static Log log = LogFactory.getLog(CallCRCUtil.class);

	public CallCRCUtil(SecurityType securityType, String projectId)
			throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.crcUrl = ontologyUtil.getCRCUrl();
		log.debug("CRC Ontology call url" + crcUrl);
	}

	public CallCRCUtil(String crcUrl, SecurityType securityType,
			String projectId) throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.crcUrl = crcUrl;
	}

	public LoadDataResponseType callCRCUpload(String conceptFileName,
			String loadLabel, boolean synchronizeAllFlag) throws I2B2Exception {
		RequestMessageType requestMessageType = buildUploaderRequestMessage(
				conceptFileName, loadLabel, synchronizeAllFlag);
		LoadDataResponseType loadDataResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			System.out.println("CRC Ontology call's request xml "
					+ requestElement);
			OMElement response = getServiceClient("/publishDataRequest")
					.sendReceive(requestElement);
			System.out.println("CRC Ontology call's reponse xml " + response);
			loadDataResponseType = getGetPublishDataResponseMessage(response
					.toString());
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (AxisFault e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return loadDataResponseType;
	}

	public LoadDataResponseType callCRCUploadStatus(String uploadId)
			throws I2B2Exception {
		RequestMessageType requestMessageType = buildUploaderRequestMessage(uploadId);
		LoadDataResponseType loadDataResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			System.out.println("CRC Ontology call's request xml "
					+ requestElement);
			OMElement response = getServiceClient("/getLoadDataStatusRequest")
					.sendReceive(requestElement);
			System.out.println("CRC Ontology call's request xml " + response);
			loadDataResponseType = getGetPublishDataResponseMessage(response
					.toString());

		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (AxisFault e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return loadDataResponseType;
	}

	public LoadDataResponseType pollUploadStatus(String uploadId)
			throws I2B2Exception {
		UploadStatusRunner uploadStatusRunner = new UploadStatusRunner();
		uploadStatusRunner.setCRCUtil(this);
		uploadStatusRunner.setUploadId(uploadId);
		Thread t = new Thread(uploadStatusRunner);
		t.start();
		while (uploadStatusRunner.isNotDone()) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String exceptionMsg = uploadStatusRunner.getExceptionMsg();
		if (exceptionMsg != null) {
			throw new I2B2Exception("Error while getting status of upload ["
					+ exceptionMsg + "]");
		}
		LoadDataResponseType loadDataResponse = uploadStatusRunner
				.getLodDataResponseType();

		return loadDataResponse;
	}

	public RequestMessageType buildUploaderRequestMessage(
			String conceptFileName, String loadLabel, boolean synchronizeAllFlag) {
		PublishDataRequestType publishDataRequest = new PublishDataRequestType();
		DataListType dataListType = new DataListType();
		DataListType.LocationUri locationUri = new DataListType.LocationUri();
		locationUri.setValue(conceptFileName);
		locationUri.setProtocolName("FR");
		dataListType.setLocationUri(locationUri);

		dataListType.setDataFormatType(DataFormatType.PDO);
		InputOptionListType inputListType = new InputOptionListType();
		inputListType.setDataFile(dataListType);
		dataListType.setLoadLabel(loadLabel);
		dataListType.setSourceSystemCd("edu.harvard.i2b2.ontology.sync");
		publishDataRequest.setInputList(inputListType);

		LoadType loadType = new LoadType();
		loadType.setClearTempLoadTables(true);
		LoadOptionType loadOption = new LoadOptionType();
		loadOption.setIgnoreBadData(true);
		if (synchronizeAllFlag) {
			loadOption.setDeleteExistingData(true);
		}
		loadType.setLoadConceptSet(loadOption);
		loadType.setLoadObserverSet(loadOption);
		publishDataRequest.setLoadList(loadType);
		OutputOptionListType outOption = new OutputOptionListType();
		publishDataRequest.setOutputList(outOption);

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createPublishDataRequest(publishDataRequest));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	public RequestMessageType buildUploaderRequestMessage(String uploadId) {
		GetUploadInfoRequestType getUploadInfo = new GetUploadInfoRequestType();
		getUploadInfo.setLoadId(uploadId);
		getUploadInfo.setUserId(securityType.getUsername());

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory of = new edu.harvard.i2b2.ontology.datavo.crcloader.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetUploadInfoRequest(getUploadInfo));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	private LoadDataResponseType getGetPublishDataResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		LoadDataResponseType loadDataResponseType = (LoadDataResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						LoadDataResponseType.class);

		return loadDataResponseType;
	}

	private OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory();
		OntologyJAXBUtil.getJAXBUtil().marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement request = builder.getDocumentElement();
		return request;
	}

	private ServiceClient getServiceClient(String operationName) {
		// call
		ServiceClient serviceClient = CRCServiceClient.getServiceClient();

		Options options = new Options();
		options.setTo(new EndpointReference(crcUrl + operationName));
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(Constants.Configuration.ENABLE_REST,
				Constants.VALUE_TRUE);
		options.setTimeOutInMilliSeconds(0);
		serviceClient.setOptions(options);
		return serviceClient;

	}

}
