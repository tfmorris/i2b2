//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.04.30 at 04:47:25 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for fact_primary_key_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fact_primary_key_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="event_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="patient_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="concept_cd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="observer_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="start_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="modifier_cd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fact_primary_key_Type", propOrder = {
    "eventId",
    "patientId",
    "conceptCd",
    "observerId",
    "startDate",
    "modifierCd"
})
public class FactPrimaryKeyType {

    @XmlElement(name = "event_id", required = true)
    protected String eventId;
    @XmlElement(name = "patient_id", required = true)
    protected String patientId;
    @XmlElement(name = "concept_cd", required = true)
    protected String conceptCd;
    @XmlElement(name = "observer_id", required = true)
    protected String observerId;
    @XmlElement(name = "start_date", required = true)
    protected XMLGregorianCalendar startDate;
    @XmlElement(name = "modifier_cd", required = true)
    protected String modifierCd;

    /**
     * Gets the value of the eventId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the value of the eventId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventId(String value) {
        this.eventId = value;
    }

    /**
     * Gets the value of the patientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Sets the value of the patientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientId(String value) {
        this.patientId = value;
    }

    /**
     * Gets the value of the conceptCd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConceptCd() {
        return conceptCd;
    }

    /**
     * Sets the value of the conceptCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConceptCd(String value) {
        this.conceptCd = value;
    }

    /**
     * Gets the value of the observerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObserverId() {
        return observerId;
    }

    /**
     * Sets the value of the observerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObserverId(String value) {
        this.observerId = value;
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
     * Gets the value of the modifierCd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModifierCd() {
        return modifierCd;
    }

    /**
     * Sets the value of the modifierCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModifierCd(String value) {
        this.modifierCd = value;
    }

}
