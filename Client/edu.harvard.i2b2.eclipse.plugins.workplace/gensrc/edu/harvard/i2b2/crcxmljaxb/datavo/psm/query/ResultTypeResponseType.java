//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:21:51 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.psm.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for result_type_responseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="result_type_responseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}responseType">
 *       &lt;sequence>
 *         &lt;element name="query_result_type" type="{http://www.i2b2.org/xsd/cell/crc/psm/1.1/}query_result_typeType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "result_type_responseType", propOrder = {
    "queryResultType"
})
public class ResultTypeResponseType
    extends ResponseType
{

    @XmlElement(name = "query_result_type")
    protected List<QueryResultTypeType> queryResultType;

    /**
     * Gets the value of the queryResultType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the queryResultType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQueryResultType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryResultTypeType }
     * 
     * 
     */
    public List<QueryResultTypeType> getQueryResultType() {
        if (queryResultType == null) {
            queryResultType = new ArrayList<QueryResultTypeType>();
        }
        return this.queryResultType;
    }

}
