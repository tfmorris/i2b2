//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:22:00 PM EDT 
//


package edu.harvard.i2b2.common.datavo.pdo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for modifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="modifierType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modifier_path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modifier_cd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name_char" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modifier_blob" type="{http://www.i2b2.org/xsd/hive/pdo/1.1/}blobType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.i2b2.org/xsd/hive/pdo/1.1/}techDataAttributeGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modifierType", propOrder = {
    "modifierPath",
    "modifierCd",
    "nameChar",
    "modifierBlob"
})
public class ModifierType {

    @XmlElement(name = "modifier_path", required = true)
    protected String modifierPath;
    @XmlElement(name = "modifier_cd", required = true)
    protected String modifierCd;
    @XmlElement(name = "name_char", required = true)
    protected String nameChar;
    @XmlElement(name = "modifier_blob")
    protected BlobType modifierBlob;
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
     * Gets the value of the modifierPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModifierPath() {
        return modifierPath;
    }

    /**
     * Sets the value of the modifierPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModifierPath(String value) {
        this.modifierPath = value;
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

    /**
     * Gets the value of the nameChar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameChar() {
        return nameChar;
    }

    /**
     * Sets the value of the nameChar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameChar(String value) {
        this.nameChar = value;
    }

    /**
     * Gets the value of the modifierBlob property.
     * 
     * @return
     *     possible object is
     *     {@link BlobType }
     *     
     */
    public BlobType getModifierBlob() {
        return modifierBlob;
    }

    /**
     * Sets the value of the modifierBlob property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlobType }
     *     
     */
    public void setModifierBlob(BlobType value) {
        this.modifierBlob = value;
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
