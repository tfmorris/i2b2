/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */


        /**
        * PMSoapServiceMessageReceiverInOut.java
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: 1.1 Nov 13, 2006 (07:31:44 LKT)
        */
        package edu.harvard.i2b2.pm.ws.wsdl;

        /**
        *  PMSoapServiceMessageReceiverInOut message receiver
        */

        public class PMSoapServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        PMSoapServiceSkeleton skel = (PMSoapServiceSkeleton)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if(op.getName() != null & (methodName = op.getName().getLocalPart()) != null){

        

            if("getServices".equals(methodName)){

            
            edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse param3 = null;
                    
                            //doc style
                            edu.harvard.i2b2.pm.ws.wsdl.GetServices wrappedParam =
                                                 (edu.harvard.i2b2.pm.ws.wsdl.GetServices)fromOM(
                        msgContext.getEnvelope().getBody().getFirstElement(),
                        edu.harvard.i2b2.pm.ws.wsdl.GetServices.class,
                        getEnvelopeNamespaces(msgContext.getEnvelope()));
                                    
                                   param3 =
                                             skel.getServices(wrappedParam) ;
                                        
                                    envelope = toEnvelope(getSOAPFactory(msgContext), param3, false);
                                

            }
        

        newMsgContext.setEnvelope(envelope);
        }
        }
        catch (Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(edu.harvard.i2b2.pm.ws.wsdl.GetServices param, boolean optimizeContent){
            
                     return param.getOMElement(edu.harvard.i2b2.pm.ws.wsdl.GetServices.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse param, boolean optimizeContent){
            
                     return param.getOMElement(edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse param, boolean optimizeContent){
                      org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                       
                                emptyEnvelope.getBody().addChild(param.getOMElement(edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse.MY_QNAME,factory));
                            

                     return emptyEnvelope;
                    }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces){

        try {
        
                if (edu.harvard.i2b2.pm.ws.wsdl.GetServices.class.equals(type)){
                
                           return edu.harvard.i2b2.pm.ws.wsdl.GetServices.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse.class.equals(type)){
                
                           return edu.harvard.i2b2.pm.ws.wsdl.GetServicesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (Exception e) {
        throw new RuntimeException(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    