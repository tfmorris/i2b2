//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.16 at 02:21:58 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for pdo_qry_headerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pdo_qry_headerType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}headerType">
 *       &lt;sequence>
 *         &lt;element name="request_type" type="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}pdoRequest_typeType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pdo_qry_headerType", propOrder = {
    "requestType"
})
public class PdoQryHeaderType
    extends HeaderType
{

    @XmlElement(name = "request_type", required = true)
    protected PdoRequestTypeType requestType;

    /**
     * Gets the value of the requestType property.
     * 
     * @return
     *     possible object is
     *     {@link PdoRequestTypeType }
     *     
     */
    public PdoRequestTypeType getRequestType() {
        return requestType;
    }

    /**
     * Sets the value of the requestType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PdoRequestTypeType }
     *     
     */
    public void setRequestType(PdoRequestTypeType value) {
        this.requestType = value;
    }

}
