//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 09:18:50 AM EST 
//


package edu.harvard.i2b2.common.datavo.pdo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for pidType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pidType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="patient_id">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.i2b2.org/xsd/hive/pdo/1.1/>patientIdType">
 *                 &lt;attGroup ref="{http://www.i2b2.org/xsd/hive/pdo/1.1/}techDataAttributeGroup"/>
 *                 &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="patient_map_id" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attGroup ref="{http://www.i2b2.org/xsd/hive/pdo/1.1/}techDataAttributeGroup"/>
 *                 &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}string" default="HIVE" />
 *                 &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pidType", propOrder = {
    "patientId",
    "patientMapId"
})
public class PidType {

    @XmlElement(name = "patient_id", required = true)
    protected PidType.PatientId patientId;
    @XmlElement(name = "patient_map_id")
    protected List<PidType.PatientMapId> patientMapId;

    /**
     * Gets the value of the patientId property.
     * 
     * @return
     *     possible object is
     *     {@link PidType.PatientId }
     *     
     */
    public PidType.PatientId getPatientId() {
        return patientId;
    }

    /**
     * Sets the value of the patientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link PidType.PatientId }
     *     
     */
    public void setPatientId(PidType.PatientId value) {
        this.patientId = value;
    }

    /**
     * Gets the value of the patientMapId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the patientMapId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPatientMapId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PidType.PatientMapId }
     * 
     * 
     */
    public List<PidType.PatientMapId> getPatientMapId() {
        if (patientMapId == null) {
            patientMapId = new ArrayList<PidType.PatientMapId>();
        }
        return this.patientMapId;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.i2b2.org/xsd/hive/pdo/1.1/>patientIdType">
     *       &lt;attGroup ref="{http://www.i2b2.org/xsd/hive/pdo/1.1/}techDataAttributeGroup"/>
     *       &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PatientId
        extends PatientIdType
    {

        @XmlAttribute
        protected String status;
        @XmlAttribute(name = "update_date")
        protected XMLGregorianCalendar updateDate;
        @XmlAttribute(name = "download_date")
        protected XMLGregorianCalendar downloadDate;
        @XmlAttribute(name = "import_date")
        protected XMLGregorianCalendar importDate;
        @XmlAttribute(name = "sourcesystem_cd")
        protected String sourcesystemCd;
        @XmlAttribute(name = "upload_id")
        protected String uploadId;

        /**
         * Gets the value of the status property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStatus(String value) {
            this.status = value;
        }

        /**
         * Gets the value of the updateDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getUpdateDate() {
            return updateDate;
        }

        /**
         * Sets the value of the updateDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setUpdateDate(XMLGregorianCalendar value) {
            this.updateDate = value;
        }

        /**
         * Gets the value of the downloadDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getDownloadDate() {
            return downloadDate;
        }

        /**
         * Sets the value of the downloadDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setDownloadDate(XMLGregorianCalendar value) {
            this.downloadDate = value;
        }

        /**
         * Gets the value of the importDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getImportDate() {
            return importDate;
        }

        /**
         * Sets the value of the importDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setImportDate(XMLGregorianCalendar value) {
            this.importDate = value;
        }

        /**
         * Gets the value of the sourcesystemCd property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSourcesystemCd() {
            return sourcesystemCd;
        }

        /**
         * Sets the value of the sourcesystemCd property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSourcesystemCd(String value) {
            this.sourcesystemCd = value;
        }

        /**
         * Gets the value of the uploadId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUploadId() {
            return uploadId;
        }

        /**
         * Sets the value of the uploadId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUploadId(String value) {
            this.uploadId = value;
        }

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
     *       &lt;attGroup ref="{http://www.i2b2.org/xsd/hive/pdo/1.1/}techDataAttributeGroup"/>
     *       &lt;attribute name="source" type="{http://www.w3.org/2001/XMLSchema}string" default="HIVE" />
     *       &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}string" />
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
    public static class PatientMapId {

        @XmlValue
        protected String value;
        @XmlAttribute
        protected String source;
        @XmlAttribute
        protected String status;
        @XmlAttribute(name = "update_date")
        protected XMLGregorianCalendar updateDate;
        @XmlAttribute(name = "download_date")
        protected XMLGregorianCalendar downloadDate;
        @XmlAttribute(name = "import_date")
        protected XMLGregorianCalendar importDate;
        @XmlAttribute(name = "sourcesystem_cd")
        protected String sourcesystemCd;
        @XmlAttribute(name = "upload_id")
        protected String uploadId;

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
         * Gets the value of the source property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSource() {
            if (source == null) {
                return "HIVE";
            } else {
                return source;
            }
        }

        /**
         * Sets the value of the source property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSource(String value) {
            this.source = value;
        }

        /**
         * Gets the value of the status property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStatus(String value) {
            this.status = value;
        }

        /**
         * Gets the value of the updateDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getUpdateDate() {
            return updateDate;
        }

        /**
         * Sets the value of the updateDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setUpdateDate(XMLGregorianCalendar value) {
            this.updateDate = value;
        }

        /**
         * Gets the value of the downloadDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getDownloadDate() {
            return downloadDate;
        }

        /**
         * Sets the value of the downloadDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setDownloadDate(XMLGregorianCalendar value) {
            this.downloadDate = value;
        }

        /**
         * Gets the value of the importDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getImportDate() {
            return importDate;
        }

        /**
         * Sets the value of the importDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setImportDate(XMLGregorianCalendar value) {
            this.importDate = value;
        }

        /**
         * Gets the value of the sourcesystemCd property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSourcesystemCd() {
            return sourcesystemCd;
        }

        /**
         * Sets the value of the sourcesystemCd property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSourcesystemCd(String value) {
            this.sourcesystemCd = value;
        }

        /**
         * Gets the value of the uploadId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUploadId() {
            return uploadId;
        }

        /**
         * Sets the value of the uploadId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUploadId(String value) {
            this.uploadId = value;
        }

    }

}
