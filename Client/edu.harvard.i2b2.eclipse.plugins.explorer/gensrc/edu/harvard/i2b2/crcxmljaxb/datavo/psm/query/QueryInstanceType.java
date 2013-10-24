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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for query_instanceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="query_instanceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="query_instance_id" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}query_instance_idType"/>
 *         &lt;element name="query_master_id" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}query_master_idType"/>
 *         &lt;element name="user_id" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}user_idType"/>
 *         &lt;element name="group_id" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}group_idType"/>
 *         &lt;element name="batch_mode" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}batch_modeType"/>
 *         &lt;element name="start_date" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}start_dateType"/>
 *         &lt;element name="end_date" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}end_dateType"/>
 *         &lt;element name="name" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}nameType"/>
 *         &lt;element name="message" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}xml_valueType"/>
 *         &lt;element name="query_status_type" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}query_status_typeType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "query_instanceType", propOrder = {
    "queryInstanceId",
    "queryMasterId",
    "userId",
    "groupId",
    "batchMode",
    "startDate",
    "endDate",
    "name",
    "message",
    "queryStatusType"
})
public class QueryInstanceType {

    @XmlElement(name = "query_instance_id", required = true)
    protected String queryInstanceId;
    @XmlElement(name = "query_master_id", required = true)
    protected String queryMasterId;
    @XmlElement(name = "user_id", required = true)
    protected String userId;
    @XmlElement(name = "group_id", required = true)
    protected String groupId;
    @XmlElement(name = "batch_mode", required = true)
    protected String batchMode;
    @XmlElement(name = "start_date", required = true)
    protected XMLGregorianCalendar startDate;
    @XmlElement(name = "end_date", required = true)
    protected XMLGregorianCalendar endDate;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected XmlValueType message;
    @XmlElement(name = "query_status_type", required = true)
    protected QueryStatusTypeType queryStatusType;

    /**
     * Gets the value of the queryInstanceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryInstanceId() {
        return queryInstanceId;
    }

    /**
     * Sets the value of the queryInstanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryInstanceId(String value) {
        this.queryInstanceId = value;
    }

    /**
     * Gets the value of the queryMasterId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryMasterId() {
        return queryMasterId;
    }

    /**
     * Sets the value of the queryMasterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryMasterId(String value) {
        this.queryMasterId = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Gets the value of the groupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

    /**
     * Gets the value of the batchMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatchMode() {
        return batchMode;
    }

    /**
     * Sets the value of the batchMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatchMode(String value) {
        this.batchMode = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDate(XMLGregorianCalendar value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link XmlValueType }
     *     
     */
    public XmlValueType getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlValueType }
     *     
     */
    public void setMessage(XmlValueType value) {
        this.message = value;
    }

    /**
     * Gets the value of the queryStatusType property.
     * 
     * @return
     *     possible object is
     *     {@link QueryStatusTypeType }
     *     
     */
    public QueryStatusTypeType getQueryStatusType() {
        return queryStatusType;
    }

    /**
     * Sets the value of the queryStatusType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryStatusTypeType }
     *     
     */
    public void setQueryStatusType(QueryStatusTypeType value) {
        this.queryStatusType = value;
    }

}
