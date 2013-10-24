//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.16 at 02:21:55 PM EDT 
//


package edu.harvard.i2b2.ontclient.datavo.dnd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="patient" type="{http://www.i2b2.org/xsd/hive/plugin/}patientType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="patient_set_id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="patient_set_name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "patient"
})
@XmlRootElement(name = "patient_set")
public class PatientSet {

    @XmlElement(required = true)
    protected List<PatientType> patient;
    @XmlAttribute(name = "patient_set_id")
    protected String patientSetId;
    @XmlAttribute(name = "patient_set_name")
    protected String patientSetName;

    /**
     * Gets the value of the patient property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patient property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatient().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientType }
     * 
     * 
     */
    public List<PatientType> getPatient() {
        if (patient == null) {
            patient = new ArrayList<PatientType>();
        }
        return this.patient;
    }

    /**
     * Gets the value of the patientSetId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientSetId() {
        return patientSetId;
    }

    /**
     * Sets the value of the patientSetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientSetId(String value) {
        this.patientSetId = value;
    }

    /**
     * Gets the value of the patientSetName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientSetName() {
        return patientSetName;
    }

    /**
     * Sets the value of the patientSetName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientSetName(String value) {
        this.patientSetName = value;
    }

}
