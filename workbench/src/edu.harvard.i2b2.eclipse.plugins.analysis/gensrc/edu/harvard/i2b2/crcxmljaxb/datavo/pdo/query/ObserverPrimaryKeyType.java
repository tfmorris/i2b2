//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.16 at 02:22:06 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for observer_primary_key_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="observer_primary_key_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="observer_path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="observer_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "observer_primary_key_Type", propOrder = {
    "observerPath",
    "observerId"
})
public class ObserverPrimaryKeyType {

    @XmlElement(name = "observer_path", required = true)
    protected String observerPath;
    @XmlElement(name = "observer_id", required = true)
    protected String observerId;

    /**
     * Gets the value of the observerPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObserverPath() {
        return observerPath;
    }

    /**
     * Sets the value of the observerPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObserverPath(String value) {
        this.observerPath = value;
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

}