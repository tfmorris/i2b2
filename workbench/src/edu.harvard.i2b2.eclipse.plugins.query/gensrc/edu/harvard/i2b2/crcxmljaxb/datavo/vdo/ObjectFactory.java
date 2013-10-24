//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 09:18:52 AM EST 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.vdo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.harvard.i2b2.crcxmljaxb.datavo.vdo package. 
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

    private final static QName _GetNameInfo_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "get_name_info");
    private final static QName _GetSchemes_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "get_schemes");
    private final static QName _GetCategories_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "get_categories");
    private final static QName _Concepts_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "concepts");
    private final static QName _GetTermInfo_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "get_term_info");
    private final static QName _GetChildren_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "get_children");
    private final static QName _GetCodeInfo_QNAME = new QName("http://www.i2b2.org/xsd/cell/ont/1.1/", "get_code_info");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.harvard.i2b2.crcxmljaxb.datavo.vdo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetTermInfoType }
     * 
     */
    public GetTermInfoType createGetTermInfoType() {
        return new GetTermInfoType();
    }

    /**
     * Create an instance of {@link GetChildrenType }
     * 
     */
    public GetChildrenType createGetChildrenType() {
        return new GetChildrenType();
    }

    /**
     * Create an instance of {@link MatchDateType }
     * 
     */
    public MatchDateType createMatchDateType() {
        return new MatchDateType();
    }

    /**
     * Create an instance of {@link MatchStrType }
     * 
     */
    public MatchStrType createMatchStrType() {
        return new MatchStrType();
    }

    /**
     * Create an instance of {@link GetReturnType }
     * 
     */
    public GetReturnType createGetReturnType() {
        return new GetReturnType();
    }

    /**
     * Create an instance of {@link XmlValueType }
     * 
     */
    public XmlValueType createXmlValueType() {
        return new XmlValueType();
    }

    /**
     * Create an instance of {@link VocabRequestType }
     * 
     */
    public VocabRequestType createVocabRequestType() {
        return new VocabRequestType();
    }

    /**
     * Create an instance of {@link ConceptsType }
     * 
     */
    public ConceptsType createConceptsType() {
        return new ConceptsType();
    }

    /**
     * Create an instance of {@link MatchIntType }
     * 
     */
    public MatchIntType createMatchIntType() {
        return new MatchIntType();
    }

    /**
     * Create an instance of {@link ConceptType }
     * 
     */
    public ConceptType createConceptType() {
        return new ConceptType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VocabRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "get_name_info")
    public JAXBElement<VocabRequestType> createGetNameInfo(VocabRequestType value) {
        return new JAXBElement<VocabRequestType>(_GetNameInfo_QNAME, VocabRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReturnType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "get_schemes")
    public JAXBElement<GetReturnType> createGetSchemes(GetReturnType value) {
        return new JAXBElement<GetReturnType>(_GetSchemes_QNAME, GetReturnType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReturnType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "get_categories")
    public JAXBElement<GetReturnType> createGetCategories(GetReturnType value) {
        return new JAXBElement<GetReturnType>(_GetCategories_QNAME, GetReturnType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConceptsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "concepts")
    public JAXBElement<ConceptsType> createConcepts(ConceptsType value) {
        return new JAXBElement<ConceptsType>(_Concepts_QNAME, ConceptsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetTermInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "get_term_info")
    public JAXBElement<GetTermInfoType> createGetTermInfo(GetTermInfoType value) {
        return new JAXBElement<GetTermInfoType>(_GetTermInfo_QNAME, GetTermInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetChildrenType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "get_children")
    public JAXBElement<GetChildrenType> createGetChildren(GetChildrenType value) {
        return new JAXBElement<GetChildrenType>(_GetChildren_QNAME, GetChildrenType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VocabRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/ont/1.1/", name = "get_code_info")
    public JAXBElement<VocabRequestType> createGetCodeInfo(VocabRequestType value) {
        return new JAXBElement<VocabRequestType>(_GetCodeInfo_QNAME, VocabRequestType.class, null, value);
    }

}
