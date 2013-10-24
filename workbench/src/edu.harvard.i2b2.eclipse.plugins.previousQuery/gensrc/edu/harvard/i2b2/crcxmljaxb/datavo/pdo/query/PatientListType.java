//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.04.30 at 04:47:29 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for patient_listType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="patient_listType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}rangeType">
 *       &lt;choice>
 *         &lt;element name="entire_patient_set" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *         &lt;element name="patient_set_coll_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="patient_id" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="index" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="patient_set_id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "patient_listType", propOrder = {
    "entirePatientSet",
    "patientSetCollId",
    "patientId"
})
public class PatientListType
    extends RangeType
{

    @XmlElement(name = "entire_patient_set")
    protected Object entirePatientSet;
    @XmlElement(name = "patient_set_coll_id")
    protected String patientSetCollId;
    @XmlElement(name = "patient_id")
    protected List<PatientListType.PatientId> patientId;

    /**
     * Gets the value of the entirePatientSet property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getEntirePatientSet() {
        return entirePatientSet;
    }

    /**
     * Sets the value of the entirePatientSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setEntirePatientSet(Object value) {
        this.entirePatientSet = value;
    }

    /**
     * Gets the value of the patientSetCollId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatientSetCollId() {
        return patientSetCollId;
    }

    /**
     * Sets the value of the patientSetCollId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatientSetCollId(String value) {
        this.patientSetCollId = value;
    }

    /**
     * Gets the value of the patientId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PatientListType.PatientId }
     * 
     * 
     */
    public List<PatientListType.PatientId> getPatientId() {
        if (patientId == null) {
            patientId = new ArrayList<PatientListType.PatientId>();
        }
        return this.patientId;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="index" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="patient_set_id" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class PatientId {

        @XmlValue
        protected String value;
        @XmlAttribute(required = true)
        protected int index;
        @XmlAttribute(name = "patient_set_id")
        protected String patientSetId;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the index property.
         * 
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the value of the index property.
         * 
         */
        public void setIndex(int value) {
            this.index = value;
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

    }

}
