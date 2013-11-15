//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:22:02 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.vdo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for get_ont_process_statusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="get_ont_process_statusType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="process_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="process_type_cd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="process_status_cd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="process_start_date">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="start_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="end_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="process_end_date">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="start_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                   &lt;element name="end_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="max_return_records" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "get_ont_process_statusType", propOrder = {
    "processId",
    "processTypeCd",
    "processStatusCd",
    "processStartDate",
    "processEndDate"
})
public class GetOntProcessStatusType {

    @XmlElement(name = "process_id", required = true)
    protected String processId;
    @XmlElement(name = "process_type_cd", required = true)
    protected String processTypeCd;
    @XmlElement(name = "process_status_cd", required = true)
    protected String processStatusCd;
    @XmlElement(name = "process_start_date", required = true)
    protected GetOntProcessStatusType.ProcessStartDate processStartDate;
    @XmlElement(name = "process_end_date", required = true)
    protected GetOntProcessStatusType.ProcessEndDate processEndDate;
    @XmlAttribute(name = "max_return_records")
    protected Integer maxReturnRecords;

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessId(String value) {
        this.processId = value;
    }

    /**
     * Gets the value of the processTypeCd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessTypeCd() {
        return processTypeCd;
    }

    /**
     * Sets the value of the processTypeCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessTypeCd(String value) {
        this.processTypeCd = value;
    }

    /**
     * Gets the value of the processStatusCd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessStatusCd() {
        return processStatusCd;
    }

    /**
     * Sets the value of the processStatusCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessStatusCd(String value) {
        this.processStatusCd = value;
    }

    /**
     * Gets the value of the processStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link GetOntProcessStatusType.ProcessStartDate }
     *     
     */
    public GetOntProcessStatusType.ProcessStartDate getProcessStartDate() {
        return processStartDate;
    }

    /**
     * Sets the value of the processStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetOntProcessStatusType.ProcessStartDate }
     *     
     */
    public void setProcessStartDate(GetOntProcessStatusType.ProcessStartDate value) {
        this.processStartDate = value;
    }

    /**
     * Gets the value of the processEndDate property.
     * 
     * @return
     *     possible object is
     *     {@link GetOntProcessStatusType.ProcessEndDate }
     *     
     */
    public GetOntProcessStatusType.ProcessEndDate getProcessEndDate() {
        return processEndDate;
    }

    /**
     * Sets the value of the processEndDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetOntProcessStatusType.ProcessEndDate }
     *     
     */
    public void setProcessEndDate(GetOntProcessStatusType.ProcessEndDate value) {
        this.processEndDate = value;
    }

    /**
     * Gets the value of the maxReturnRecords property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getMaxReturnRecords() {
        if (maxReturnRecords == null) {
            return  0;
        } else {
            return maxReturnRecords;
        }
    }

    /**
     * Sets the value of the maxReturnRecords property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxReturnRecords(Integer value) {
        this.maxReturnRecords = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="start_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="end_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "startTime",
        "endTime"
    })
    public static class ProcessEndDate {

        @XmlElement(name = "start_time", required = true)
        protected XMLGregorianCalendar startTime;
        @XmlElement(name = "end_time", required = true)
        protected XMLGregorianCalendar endTime;

        /**
         * Gets the value of the startTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getStartTime() {
            return startTime;
        }

        /**
         * Sets the value of the startTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setStartTime(XMLGregorianCalendar value) {
            this.startTime = value;
        }

        /**
         * Gets the value of the endTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEndTime() {
            return endTime;
        }

        /**
         * Sets the value of the endTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEndTime(XMLGregorianCalendar value) {
            this.endTime = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="start_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *         &lt;element name="end_time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "startTime",
        "endTime"
    })
    public static class ProcessStartDate {

        @XmlElement(name = "start_time", required = true)
        protected XMLGregorianCalendar startTime;
        @XmlElement(name = "end_time", required = true)
        protected XMLGregorianCalendar endTime;

        /**
         * Gets the value of the startTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getStartTime() {
            return startTime;
        }

        /**
         * Sets the value of the startTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setStartTime(XMLGregorianCalendar value) {
            this.startTime = value;
        }

        /**
         * Gets the value of the endTime property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getEndTime() {
            return endTime;
        }

        /**
         * Sets the value of the endTime property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setEndTime(XMLGregorianCalendar value) {
            this.endTime = value;
        }

    }

}
