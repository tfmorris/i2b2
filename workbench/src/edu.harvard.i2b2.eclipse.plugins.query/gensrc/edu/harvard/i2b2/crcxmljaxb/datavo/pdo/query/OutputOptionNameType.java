//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.04.30 at 04:47:25 PM EDT 
//


package edu.harvard.i2b2.crcxmljaxb.datavo.pdo.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for outputOptionNameType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="outputOptionNameType">
 *   &lt;restriction base="{http://www.i2b2.org/xsd/cell/crc/pdo/1.1/}tokenType">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="asattributes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "outputOptionNameType")
@XmlEnum
public enum OutputOptionNameType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("asattributes")
    ASATTRIBUTES("asattributes");
    private final String value;

    OutputOptionNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OutputOptionNameType fromValue(String v) {
        for (OutputOptionNameType c: OutputOptionNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
