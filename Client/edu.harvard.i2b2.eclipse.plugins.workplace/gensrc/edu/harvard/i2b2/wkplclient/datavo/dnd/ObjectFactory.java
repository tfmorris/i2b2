//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.16 at 02:21:56 PM EDT 
//


package edu.harvard.i2b2.wkplclient.datavo.dnd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.harvard.i2b2.wkplclient.datavo.dnd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PatientType_QNAME = new QName("http://www.i2b2.org/xsd/hive/plugin/", "patient_type");
    private final static QName _PluginDragDrop_QNAME = new QName("http://www.i2b2.org/xsd/hive/plugin/", "plugin_drag_drop");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.harvard.i2b2.wkplclient.datavo.dnd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PatientSet }
     * 
     */
    public PatientSet createPatientSet() {
        return new PatientSet();
    }

    /**
     * Create an instance of {@link PatientType }
     * 
     */
    public PatientType createPatientType() {
        return new PatientType();
    }

    /**
     * Create an instance of {@link DndType }
     * 
     */
    public DndType createDndType() {
        return new DndType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PatientType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/hive/plugin/", name = "patient_type")
    public JAXBElement<PatientType> createPatientType(PatientType value) {
        return new JAXBElement<PatientType>(_PatientType_QNAME, PatientType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DndType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/hive/plugin/", name = "plugin_drag_drop")
    public JAXBElement<DndType> createPluginDragDrop(DndType value) {
        return new JAXBElement<DndType>(_PluginDragDrop_QNAME, DndType.class, null, value);
    }

}
