//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.04.12 at 03:21:57 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for constrainOperatorType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="constrainOperatorType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EQ"/>
 *     &lt;enumeration value="NE"/>
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="GE"/>
 *     &lt;enumeration value="LE"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="LIKE"/>
 *     &lt;enumeration value="LIKE[begin]"/>
 *     &lt;enumeration value="LIKE[end]"/>
 *     &lt;enumeration value="LIKE[contains]"/>
 *     &lt;enumeration value="LIKE[exact]"/>
 *     &lt;enumeration value="BETWEEN"/>
 *     &lt;enumeration value="CONTAINS"/>
 *     &lt;enumeration value="CONTAINS[database]"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "constrainOperatorType")
@XmlEnum
public enum ConstrainOperatorType {

    EQ("EQ"),
    NE("NE"),
    GT("GT"),
    LT("LT"),
    GE("GE"),
    LE("LE"),
    IN("IN"),
    LIKE("LIKE"),
    @XmlEnumValue("LIKE[begin]")
    LIKE_BEGIN("LIKE[begin]"),
    @XmlEnumValue("LIKE[end]")
    LIKE_END("LIKE[end]"),
    @XmlEnumValue("LIKE[contains]")
    LIKE_CONTAINS("LIKE[contains]"),
    @XmlEnumValue("LIKE[exact]")
    LIKE_EXACT("LIKE[exact]"),
    BETWEEN("BETWEEN"),
    CONTAINS("CONTAINS"),
    @XmlEnumValue("CONTAINS[database]")
    CONTAINS_DATABASE("CONTAINS[database]");
    private final String value;

    ConstrainOperatorType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConstrainOperatorType fromValue(String v) {
        for (ConstrainOperatorType c: ConstrainOperatorType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
