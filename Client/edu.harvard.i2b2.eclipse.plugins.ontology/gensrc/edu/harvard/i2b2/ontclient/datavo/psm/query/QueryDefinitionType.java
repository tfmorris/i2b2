//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:21:49 PM EDT 
//


package edu.harvard.i2b2.ontclient.datavo.psm.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for query_definitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="query_definitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="query_name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="query_description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="query_timing" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="specificity_scale" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="panel" type="{http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/}panelType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "query_definitionType", namespace = "http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/", propOrder = {
    "queryName",
    "queryDescription",
    "queryTiming",
    "specificityScale",
    "panel"
})
public class QueryDefinitionType {

    @XmlElement(name = "query_name", required = true)
    protected String queryName;
    @XmlElement(name = "query_description", required = true)
    protected String queryDescription;
    @XmlElement(name = "query_timing", required = true, defaultValue = "ANY")
    protected String queryTiming;
    @XmlElement(name = "specificity_scale")
    protected int specificityScale;
    @XmlElement(required = true)
    protected List<PanelType> panel;

    /**
     * Gets the value of the queryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Sets the value of the queryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryName(String value) {
        this.queryName = value;
    }

    /**
     * Gets the value of the queryDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryDescription() {
        return queryDescription;
    }

    /**
     * Sets the value of the queryDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryDescription(String value) {
        this.queryDescription = value;
    }

    /**
     * Gets the value of the queryTiming property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryTiming() {
        return queryTiming;
    }

    /**
     * Sets the value of the queryTiming property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryTiming(String value) {
        this.queryTiming = value;
    }

    /**
     * Gets the value of the specificityScale property.
     * 
     */
    public int getSpecificityScale() {
        return specificityScale;
    }

    /**
     * Sets the value of the specificityScale property.
     * 
     */
    public void setSpecificityScale(int value) {
        this.specificityScale = value;
    }

    /**
     * Gets the value of the panel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the panel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPanel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PanelType }
     * 
     * 
     */
    public List<PanelType> getPanel() {
        if (panel == null) {
            panel = new ArrayList<PanelType>();
        }
        return this.panel;
    }

}
