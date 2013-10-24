//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.10.27 at 11:21:39 AM EDT 
//


package jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for request_typeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="request_typeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CRC_QRY_getQueryMasterList_fromUserId"/>
 *     &lt;enumeration value="CRC_QRY_getQueryMaster_fromQueryMasterId"/>
 *     &lt;enumeration value="CRC_QRY_getQueryMaster_fromQueryInstanceId"/>
 *     &lt;enumeration value="CRC_QRY_getQueryMaster_fromResultInstanceId"/>
 *     &lt;enumeration value="CRC_QRY_saveQueryMaster_fromQueryDefinition"/>
 *     &lt;enumeration value="CRC_QRY_getQueryInstanceList_fromQueryMasterId"/>
 *     &lt;enumeration value="CRC_QRY_getQueryInstance_fromQueryInstanceId"/>
 *     &lt;enumeration value="CRC_QRY_getQueryInstance_fromResultInstanceId"/>
 *     &lt;enumeration value="CRC_QRY_runQueryInstance_fromQueryMasterId"/>
 *     &lt;enumeration value="CRC_QRY_runQueryInstance_fromQueryDefinition"/>
 *     &lt;enumeration value="CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId"/>
 *     &lt;enumeration value="CRC_QRY_getQueryResultInstance_fromResultInstanceId"/>
 *     &lt;enumeration value="CRC_QRY_getRequestXml_fromQueryMasterId"/>
 *     &lt;enumeration value="CRC_QRY_getPatientSet_fromResultInstanceId"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum RequestTypeType {

    @XmlEnumValue("CRC_QRY_getQueryMasterList_fromUserId")
    CRC_QRY_GET_QUERY_MASTER_LIST_FROM_USER_ID("CRC_QRY_getQueryMasterList_fromUserId"),
    @XmlEnumValue("CRC_QRY_getQueryMaster_fromQueryMasterId")
    CRC_QRY_GET_QUERY_MASTER_FROM_QUERY_MASTER_ID("CRC_QRY_getQueryMaster_fromQueryMasterId"),
    @XmlEnumValue("CRC_QRY_getQueryMaster_fromQueryInstanceId")
    CRC_QRY_GET_QUERY_MASTER_FROM_QUERY_INSTANCE_ID("CRC_QRY_getQueryMaster_fromQueryInstanceId"),
    @XmlEnumValue("CRC_QRY_getQueryMaster_fromResultInstanceId")
    CRC_QRY_GET_QUERY_MASTER_FROM_RESULT_INSTANCE_ID("CRC_QRY_getQueryMaster_fromResultInstanceId"),
    @XmlEnumValue("CRC_QRY_saveQueryMaster_fromQueryDefinition")
    CRC_QRY_SAVE_QUERY_MASTER_FROM_QUERY_DEFINITION("CRC_QRY_saveQueryMaster_fromQueryDefinition"),
    @XmlEnumValue("CRC_QRY_getQueryInstanceList_fromQueryMasterId")
    CRC_QRY_GET_QUERY_INSTANCE_LIST_FROM_QUERY_MASTER_ID("CRC_QRY_getQueryInstanceList_fromQueryMasterId"),
    @XmlEnumValue("CRC_QRY_getQueryInstance_fromQueryInstanceId")
    CRC_QRY_GET_QUERY_INSTANCE_FROM_QUERY_INSTANCE_ID("CRC_QRY_getQueryInstance_fromQueryInstanceId"),
    @XmlEnumValue("CRC_QRY_getQueryInstance_fromResultInstanceId")
    CRC_QRY_GET_QUERY_INSTANCE_FROM_RESULT_INSTANCE_ID("CRC_QRY_getQueryInstance_fromResultInstanceId"),
    @XmlEnumValue("CRC_QRY_runQueryInstance_fromQueryMasterId")
    CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_MASTER_ID("CRC_QRY_runQueryInstance_fromQueryMasterId"),
    @XmlEnumValue("CRC_QRY_runQueryInstance_fromQueryDefinition")
    CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION("CRC_QRY_runQueryInstance_fromQueryDefinition"),
    @XmlEnumValue("CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId")
    CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID("CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId"),
    @XmlEnumValue("CRC_QRY_getQueryResultInstance_fromResultInstanceId")
    CRC_QRY_GET_QUERY_RESULT_INSTANCE_FROM_RESULT_INSTANCE_ID("CRC_QRY_getQueryResultInstance_fromResultInstanceId"),
    @XmlEnumValue("CRC_QRY_getRequestXml_fromQueryMasterId")
    CRC_QRY_GET_REQUEST_XML_FROM_QUERY_MASTER_ID("CRC_QRY_getRequestXml_fromQueryMasterId"),
    @XmlEnumValue("CRC_QRY_getPatientSet_fromResultInstanceId")
    CRC_QRY_GET_PATIENT_SET_FROM_RESULT_INSTANCE_ID("CRC_QRY_getPatientSet_fromResultInstanceId");
    private final String value;

    RequestTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RequestTypeType fromValue(String v) {
        for (RequestTypeType c: RequestTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
