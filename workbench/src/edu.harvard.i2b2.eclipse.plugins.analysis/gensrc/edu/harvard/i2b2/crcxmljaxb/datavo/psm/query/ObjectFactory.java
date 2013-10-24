//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 09:18:59 AM EST 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.psm.query;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.harvard.i2b2.crcxmljaxb.datavo.psm.query package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Sql_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "sql");
    private final static QName _Psmheader_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "psmheader");
    private final static QName _QueryResultInstance_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "query_result_instance");
    private final static QName _QueryInstance_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "query_instance");
    private final static QName _Response_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "response");
    private final static QName _QueryDefinition_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/", "query_definition");
    private final static QName _Panel_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/", "panel");
    private final static QName _QueryMaster_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "query_master");
    private final static QName _Request_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/1.1/", "request");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.harvard.i2b2.crcxmljaxb.datavo.psm.query
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResultRequestType }
     * 
     */
    public ResultRequestType createResultRequestType() {
        return new ResultRequestType();
    }

    /**
     * Create an instance of {@link RequestXmlResponseType }
     * 
     */
    public RequestXmlResponseType createRequestXmlResponseType() {
        return new RequestXmlResponseType();
    }

    /**
     * Create an instance of {@link PatientSetType }
     * 
     */
    public PatientSetType createPatientSetType() {
        return new PatientSetType();
    }

    /**
     * Create an instance of {@link ResultOutputOptionListType }
     * 
     */
    public ResultOutputOptionListType createResultOutputOptionListType() {
        return new ResultOutputOptionListType();
    }

    /**
     * Create an instance of {@link PatientSetCollectionType }
     * 
     */
    public PatientSetCollectionType createPatientSetCollectionType() {
        return new PatientSetCollectionType();
    }

    /**
     * Create an instance of {@link ResultTypeRequestType }
     * 
     */
    public ResultTypeRequestType createResultTypeRequestType() {
        return new ResultTypeRequestType();
    }

    /**
     * Create an instance of {@link QueryResultInstanceType }
     * 
     */
    public QueryResultInstanceType createQueryResultInstanceType() {
        return new QueryResultInstanceType();
    }

    /**
     * Create an instance of {@link ConstrainDateType }
     * 
     */
    public ConstrainDateType createConstrainDateType() {
        return new ConstrainDateType();
    }

    /**
     * Create an instance of {@link HeaderType }
     * 
     */
    public HeaderType createHeaderType() {
        return new HeaderType();
    }

    /**
     * Create an instance of {@link PatientSetType.CohortPatient }
     * 
     */
    public PatientSetType.CohortPatient createPatientSetTypeCohortPatient() {
        return new PatientSetType.CohortPatient();
    }

    /**
     * Create an instance of {@link InstanceResultResponseType }
     * 
     */
    public InstanceResultResponseType createInstanceResultResponseType() {
        return new InstanceResultResponseType();
    }

    /**
     * Create an instance of {@link UserRequestType }
     * 
     */
    public UserRequestType createUserRequestType() {
        return new UserRequestType();
    }

    /**
     * Create an instance of {@link UserType }
     * 
     */
    public UserType createUserType() {
        return new UserType();
    }

    /**
     * Create an instance of {@link InstanceRequestType }
     * 
     */
    public InstanceRequestType createInstanceRequestType() {
        return new InstanceRequestType();
    }

    /**
     * Create an instance of {@link MasterInstanceResultResponseType }
     * 
     */
    public MasterInstanceResultResponseType createMasterInstanceResultResponseType() {
        return new MasterInstanceResultResponseType();
    }

    /**
     * Create an instance of {@link DatatypesExample }
     * 
     */
    public DatatypesExample createDatatypesExample() {
        return new DatatypesExample();
    }

    /**
     * Create an instance of {@link MasterRequestType }
     * 
     */
    public MasterRequestType createMasterRequestType() {
        return new MasterRequestType();
    }

    /**
     * Create an instance of {@link ItemType.ConstrainByDate }
     * 
     */
    public ItemType.ConstrainByDate createItemTypeConstrainByDate() {
        return new ItemType.ConstrainByDate();
    }

    /**
     * Create an instance of {@link QueryInstanceType }
     * 
     */
    public QueryInstanceType createQueryInstanceType() {
        return new QueryInstanceType();
    }

    /**
     * Create an instance of {@link XmlValueType }
     * 
     */
    public XmlValueType createXmlValueType() {
        return new XmlValueType();
    }

    /**
     * Create an instance of {@link PsmQryHeaderType }
     * 
     */
    public PsmQryHeaderType createPsmQryHeaderType() {
        return new PsmQryHeaderType();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link MasterDeleteRequestType }
     * 
     */
    public MasterDeleteRequestType createMasterDeleteRequestType() {
        return new MasterDeleteRequestType();
    }

    /**
     * Create an instance of {@link QueryDefinitionType }
     * 
     */
    public QueryDefinitionType createQueryDefinitionType() {
        return new QueryDefinitionType();
    }

    /**
     * Create an instance of {@link QueryStatusTypeType }
     * 
     */
    public QueryStatusTypeType createQueryStatusTypeType() {
        return new QueryStatusTypeType();
    }

    /**
     * Create an instance of {@link RequestXmlType }
     * 
     */
    public RequestXmlType createRequestXmlType() {
        return new RequestXmlType();
    }

    /**
     * Create an instance of {@link ItemType.ConstrainByValue }
     * 
     */
    public ItemType.ConstrainByValue createItemTypeConstrainByValue() {
        return new ItemType.ConstrainByValue();
    }

    /**
     * Create an instance of {@link QueryMasterType }
     * 
     */
    public QueryMasterType createQueryMasterType() {
        return new QueryMasterType();
    }

    /**
     * Create an instance of {@link QueryResultTypeType }
     * 
     */
    public QueryResultTypeType createQueryResultTypeType() {
        return new QueryResultTypeType();
    }

    /**
     * Create an instance of {@link MasterRenameRequestType }
     * 
     */
    public MasterRenameRequestType createMasterRenameRequestType() {
        return new MasterRenameRequestType();
    }

    /**
     * Create an instance of {@link ResultResponseType }
     * 
     */
    public ResultResponseType createResultResponseType() {
        return new ResultResponseType();
    }

    /**
     * Create an instance of {@link StatusType.Condition }
     * 
     */
    public StatusType.Condition createStatusTypeCondition() {
        return new StatusType.Condition();
    }

    /**
     * Create an instance of {@link PanelType }
     * 
     */
    public PanelType createPanelType() {
        return new PanelType();
    }

    /**
     * Create an instance of {@link PatientEncCollectionType }
     * 
     */
    public PatientEncCollectionType createPatientEncCollectionType() {
        return new PatientEncCollectionType();
    }

    /**
     * Create an instance of {@link ResultOutputOptionType }
     * 
     */
    public ResultOutputOptionType createResultOutputOptionType() {
        return new ResultOutputOptionType();
    }

    /**
     * Create an instance of {@link XmlResultType }
     * 
     */
    public XmlResultType createXmlResultType() {
        return new XmlResultType();
    }

    /**
     * Create an instance of {@link PanelType.TotalItemOccurrences }
     * 
     */
    public PanelType.TotalItemOccurrences createPanelTypeTotalItemOccurrences() {
        return new PanelType.TotalItemOccurrences();
    }

    /**
     * Create an instance of {@link QueryDefinitionRequestType }
     * 
     */
    public QueryDefinitionRequestType createQueryDefinitionRequestType() {
        return new QueryDefinitionRequestType();
    }

    /**
     * Create an instance of {@link PatientSetResponseType }
     * 
     */
    public PatientSetResponseType createPatientSetResponseType() {
        return new PatientSetResponseType();
    }

    /**
     * Create an instance of {@link CrcXmlResultResponseType }
     * 
     */
    public CrcXmlResultResponseType createCrcXmlResultResponseType() {
        return new CrcXmlResultResponseType();
    }

    /**
     * Create an instance of {@link AnalysisDefinitionRequestType }
     * 
     */
    public AnalysisDefinitionRequestType createAnalysisDefinitionRequestType() {
        return new AnalysisDefinitionRequestType();
    }

    /**
     * Create an instance of {@link ResultTypeResponseType }
     * 
     */
    public ResultTypeResponseType createResultTypeResponseType() {
        return new ResultTypeResponseType();
    }

    /**
     * Create an instance of {@link MasterResponseType }
     * 
     */
    public MasterResponseType createMasterResponseType() {
        return new MasterResponseType();
    }

    /**
     * Create an instance of {@link InstanceResponseType }
     * 
     */
    public InstanceResponseType createInstanceResponseType() {
        return new InstanceResponseType();
    }

    /**
     * Create an instance of {@link ItemType }
     * 
     */
    public ItemType createItemType() {
        return new ItemType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "sql")
    public JAXBElement<String> createSql(String value) {
        return new JAXBElement<String>(_Sql_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PsmQryHeaderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "psmheader")
    public JAXBElement<PsmQryHeaderType> createPsmheader(PsmQryHeaderType value) {
        return new JAXBElement<PsmQryHeaderType>(_Psmheader_QNAME, PsmQryHeaderType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryResultInstanceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "query_result_instance")
    public JAXBElement<QueryResultInstanceType> createQueryResultInstance(QueryResultInstanceType value) {
        return new JAXBElement<QueryResultInstanceType>(_QueryResultInstance_QNAME, QueryResultInstanceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryInstanceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "query_instance")
    public JAXBElement<QueryInstanceType> createQueryInstance(QueryInstanceType value) {
        return new JAXBElement<QueryInstanceType>(_QueryInstance_QNAME, QueryInstanceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "response")
    public JAXBElement<ResponseType> createResponse(ResponseType value) {
        return new JAXBElement<ResponseType>(_Response_QNAME, ResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/", name = "query_definition")
    public JAXBElement<QueryDefinitionType> createQueryDefinition(QueryDefinitionType value) {
        return new JAXBElement<QueryDefinitionType>(_QueryDefinition_QNAME, QueryDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PanelType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/", name = "panel")
    public JAXBElement<PanelType> createPanel(PanelType value) {
        return new JAXBElement<PanelType>(_Panel_QNAME, PanelType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryMasterType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "query_master")
    public JAXBElement<QueryMasterType> createQueryMaster(QueryMasterType value) {
        return new JAXBElement<QueryMasterType>(_QueryMaster_QNAME, QueryMasterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/1.1/", name = "request")
    public JAXBElement<RequestType> createRequest(RequestType value) {
        return new JAXBElement<RequestType>(_Request_QNAME, RequestType.class, null, value);
    }

}
