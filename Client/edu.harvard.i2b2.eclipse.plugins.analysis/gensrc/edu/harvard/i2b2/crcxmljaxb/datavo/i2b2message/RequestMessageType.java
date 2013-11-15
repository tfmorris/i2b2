//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:22:02 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for request_messageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="request_messageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message_header" type="{http://www.i2b2.org/xsd/hive/msg/1.1/}message_headerType"/>
 *         &lt;element name="request_header" type="{http://www.i2b2.org/xsd/hive/msg/1.1/}request_headerType"/>
 *         &lt;element name="message_body" type="{http://www.i2b2.org/xsd/hive/msg/1.1/}bodyType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "request_messageType", propOrder = {
    "messageHeader",
    "requestHeader",
    "messageBody"
})
public class RequestMessageType {

    @XmlElement(name = "message_header", required = true)
    protected MessageHeaderType messageHeader;
    @XmlElement(name = "request_header", required = true)
    protected RequestHeaderType requestHeader;
    @XmlElement(name = "message_body", required = true)
    protected BodyType messageBody;

    /**
     * Gets the value of the messageHeader property.
     * 
     * @return
     *     possible object is
     *     {@link MessageHeaderType }
     *     
     */
    public MessageHeaderType getMessageHeader() {
        return messageHeader;
    }

    /**
     * Sets the value of the messageHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageHeaderType }
     *     
     */
    public void setMessageHeader(MessageHeaderType value) {
        this.messageHeader = value;
    }

    /**
     * Gets the value of the requestHeader property.
     * 
     * @return
     *     possible object is
     *     {@link RequestHeaderType }
     *     
     */
    public RequestHeaderType getRequestHeader() {
        return requestHeader;
    }

    /**
     * Sets the value of the requestHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestHeaderType }
     *     
     */
    public void setRequestHeader(RequestHeaderType value) {
        this.requestHeader = value;
    }

    /**
     * Gets the value of the messageBody property.
     * 
     * @return
     *     possible object is
     *     {@link BodyType }
     *     
     */
    public BodyType getMessageBody() {
        return messageBody;
    }

    /**
     * Sets the value of the messageBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link BodyType }
     *     
     */
    public void setMessageBody(BodyType value) {
        this.messageBody = value;
    }

}
