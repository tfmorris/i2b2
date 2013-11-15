//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:21:51 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for fact_output_optionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fact_output_optionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}output_optionType">
 *       &lt;attribute name="before" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="after" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="withmodifiers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fact_output_optionType")
public class FactOutputOptionType
    extends OutputOptionType
{

    @XmlAttribute
    protected XMLGregorianCalendar before;
    @XmlAttribute
    protected XMLGregorianCalendar after;
    @XmlAttribute
    protected Boolean withmodifiers;

    /**
     * Gets the value of the before property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBefore() {
        return before;
    }

    /**
     * Sets the value of the before property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBefore(XMLGregorianCalendar value) {
        this.before = value;
    }

    /**
     * Gets the value of the after property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAfter() {
        return after;
    }

    /**
     * Sets the value of the after property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAfter(XMLGregorianCalendar value) {
        this.after = value;
    }

    /**
     * Gets the value of the withmodifiers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isWithmodifiers() {
        if (withmodifiers == null) {
            return true;
        } else {
            return withmodifiers;
        }
    }

    /**
     * Sets the value of the withmodifiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWithmodifiers(Boolean value) {
        this.withmodifiers = value;
    }

}
