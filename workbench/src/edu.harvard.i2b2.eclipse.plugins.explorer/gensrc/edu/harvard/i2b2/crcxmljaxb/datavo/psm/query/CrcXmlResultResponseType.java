//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.16 at 02:21:58 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.psm.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for crc_xml_result_responseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="crc_xml_result_responseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}responseType">
 *       &lt;sequence>
 *         &lt;element name="query_result_instance" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}query_result_instanceType"/>
 *         &lt;element name="crc_xml_result" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}xml_resultType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "crc_xml_result_responseType", propOrder = {
    "queryResultInstance",
    "crcXmlResult"
})
public class CrcXmlResultResponseType
    extends ResponseType
{

    @XmlElement(name = "query_result_instance", required = true)
    protected QueryResultInstanceType queryResultInstance;
    @XmlElement(name = "crc_xml_result", required = true)
    protected XmlResultType crcXmlResult;

    /**
     * Gets the value of the queryResultInstance property.
     * 
     * @return
     *     possible object is
     *     {@link QueryResultInstanceType }
     *     
     */
    public QueryResultInstanceType getQueryResultInstance() {
        return queryResultInstance;
    }

    /**
     * Sets the value of the queryResultInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryResultInstanceType }
     *     
     */
    public void setQueryResultInstance(QueryResultInstanceType value) {
        this.queryResultInstance = value;
    }

    /**
     * Gets the value of the crcXmlResult property.
     * 
     * @return
     *     possible object is
     *     {@link XmlResultType }
     *     
     */
    public XmlResultType getCrcXmlResult() {
        return crcXmlResult;
    }

    /**
     * Sets the value of the crcXmlResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlResultType }
     *     
     */
    public void setCrcXmlResult(XmlResultType value) {
        this.crcXmlResult = value;
    }

}
