//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 09:18:56 AM EST 
//


package org.i2b2.xsd.cell.crc.psm.analysisdefinition._1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.i2b2.xsd.cell.crc.psm.analysisdefinition._1 package. 
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

    private final static QName _AnalysisDefinition_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/", "analysis_definition");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.i2b2.xsd.cell.crc.psm.analysisdefinition._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AnalysisResultOptionType }
     * 
     */
    public AnalysisResultOptionType createAnalysisResultOptionType() {
        return new AnalysisResultOptionType();
    }

    /**
     * Create an instance of {@link AnalysisResultOptionListType }
     * 
     */
    public AnalysisResultOptionListType createAnalysisResultOptionListType() {
        return new AnalysisResultOptionListType();
    }

    /**
     * Create an instance of {@link CrcAnalysisInputParamType }
     * 
     */
    public CrcAnalysisInputParamType createCrcAnalysisInputParamType() {
        return new CrcAnalysisInputParamType();
    }

    /**
     * Create an instance of {@link AnalysisDefinitionType }
     * 
     */
    public AnalysisDefinitionType createAnalysisDefinitionType() {
        return new AnalysisDefinitionType();
    }

    /**
     * Create an instance of {@link AnalysisParamType }
     * 
     */
    public AnalysisParamType createAnalysisParamType() {
        return new AnalysisParamType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AnalysisDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/psm/analysisdefinition/1.1/", name = "analysis_definition")
    public JAXBElement<AnalysisDefinitionType> createAnalysisDefinition(AnalysisDefinitionType value) {
        return new JAXBElement<AnalysisDefinitionType>(_AnalysisDefinition_QNAME, AnalysisDefinitionType.class, null, value);
    }

}
