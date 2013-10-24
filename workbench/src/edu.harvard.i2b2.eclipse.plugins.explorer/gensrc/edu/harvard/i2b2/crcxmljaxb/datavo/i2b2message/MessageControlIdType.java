//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.04.30 at 04:47:18 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for message_control_idType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="message_control_idType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="session_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="message_num" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="instance_num" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message_control_idType", propOrder = {
    "sessionId",
    "messageNum",
    "instanceNum"
})
public class MessageControlIdType {

    @XmlElement(name = "session_id", required = true)
    protected String sessionId;
    @XmlElement(name = "message_num", required = true)
    protected String messageNum;
    @XmlElement(name = "instance_num")
    protected int instanceNum;

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the messageNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageNum() {
        return messageNum;
    }

    /**
     * Sets the value of the messageNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageNum(String value) {
        this.messageNum = value;
    }

    /**
     * Gets the value of the instanceNum property.
     * 
     */
    public int getInstanceNum() {
        return instanceNum;
    }

    /**
     * Sets the value of the instanceNum property.
     * 
     */
    public void setInstanceNum(int value) {
        this.instanceNum = value;
    }

}
