/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.ws;

import javax.xml.stream.XMLStreamException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.delegate.GetCategoriesHandler;
import edu.harvard.i2b2.ontology.delegate.GetChildrenHandler;
import edu.harvard.i2b2.ontology.delegate.GetCodeInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetNameInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetSchemesHandler;
import edu.harvard.i2b2.ontology.delegate.GetTermInfoHandler;
import edu.harvard.i2b2.ontology.delegate.RequestHandler;
import edu.harvard.i2b2.ontology.ws.MessageFactory;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is webservice skeleton class. It parses incoming Ontology service requests
 * and  generates responses in the Vocab Data Object XML format.
 *
 */
public class OntologyService {
    private static Log log = LogFactory.getLog(OntologyService.class);

    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Ontology
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap vocab data object. Vocab data object will
     * have all the results returned by the query.
     *
     *
     * @param getChildren
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getChildren(OMElement getChildrenElement)
        throws I2B2Exception {
    	
        OMElement returnElement = null;
        String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
		"You may wish to retry your last action";
    	
        if (getChildrenElement == null) {
            log.error("Incoming Ontology request is null");
    		ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
    				unknownErrorMessage);
    		ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        }
        String requestElementString = getChildrenElement.toString();
        GetChildrenDataMessage childrenDataMsg = new GetChildrenDataMessage(requestElementString);
        long waitTime = 0;
        if (childrenDataMsg.getRequestMessageType() != null) {
            if (childrenDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = childrenDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.     
   //     ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetChildrenHandler(childrenDataMsg), waitTime);
        
    }
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Ontology
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap vocab data object. Vocab data object will
     * have all the results returned by the query.
     *
     *
     * @param OMElement getCategoriesElement
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getCategories(OMElement getCategoriesElement)
        throws I2B2Exception {

    	OMElement returnElement = null;
    	String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	

        if (getCategoriesElement == null) {
            log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        }

        String requestElementString = getCategoriesElement.toString();
        GetCategoriesDataMessage categoriesDataMsg = new GetCategoriesDataMessage(requestElementString);
        long waitTime = 0;
        if (categoriesDataMsg.getRequestMessageType() != null) {
            if (categoriesDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = categoriesDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
  //      ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetCategoriesHandler(categoriesDataMsg), waitTime);
    }
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Ontology
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap vocab data object. Vocab data object will
     * have all the results returned by the query.
     *
     *
     * @param OMElement geSchemesElement
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getSchemes(OMElement getSchemesElement)
        throws I2B2Exception {
    	OMElement returnElement = null;
    	String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	
        if (getSchemesElement == null) {
            log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        }

        String requestElementString = getSchemesElement.toString();
        GetSchemesDataMessage schemesDataMsg = new GetSchemesDataMessage(requestElementString);

        long waitTime = 0;
        if (schemesDataMsg.getRequestMessageType() != null) {
            if (schemesDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = schemesDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 //       ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetSchemesHandler(schemesDataMsg), waitTime);
    }
    
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Ontology
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap vocab data object. Vocab data object will
     * have all the results returned by the query.
     *
     *
     * @param OMElement getCodeInfoElement
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getCodeInfo(OMElement getCodeInfoElement)
        throws I2B2Exception {
    	OMElement returnElement = null;
    	String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    	"You may wish to retry your last action";

        if (getCodeInfoElement == null) {
            log.error("Incoming Ontology request is null");
    		ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
    				unknownErrorMessage);
    		ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        }

        String requestElementString = getCodeInfoElement.toString();
        GetCodeInfoDataMessage codeInfoDataMsg = new GetCodeInfoDataMessage(requestElementString);

        long waitTime = 0;
        if (codeInfoDataMsg.getRequestMessageType() != null) {
            if (codeInfoDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = codeInfoDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
   //     ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetCodeInfoHandler(codeInfoDataMsg), waitTime);
    }
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Ontology
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap vocab data object. Vocab data object will
     * have all the results returned by the query.
     *
     *
     * @param OMElement getCodeInfoElement
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getNameInfo(OMElement getNameInfoElement)
        throws I2B2Exception {
    	OMElement returnElement = null;
    	String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
        if (getNameInfoElement == null) {
            log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        }

        String requestElementString = getNameInfoElement.toString();
        GetNameInfoDataMessage nameInfoDataMsg = new GetNameInfoDataMessage(requestElementString);

        long waitTime = 0;
        if (nameInfoDataMsg.getRequestMessageType() != null) {
            if (nameInfoDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = nameInfoDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
   //     ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetNameInfoHandler(nameInfoDataMsg), waitTime);
    }
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Ontology
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap vocab data object. Vocab data object will
     * have all the results returned by the query.
     *
     *
     * @param getChildren
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getTermInfo(OMElement getTermInfoElement)
        throws I2B2Exception {

        OMElement returnElement = null;
    	String ontologyDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	

        if (getTermInfoElement == null) {
            log.error("Incoming Ontology request is null");
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(ontologyDataResponse);

        }

        String requestElementString = getTermInfoElement.toString();  
        GetTermInfoDataMessage termInfoDataMsg = new GetTermInfoDataMessage(requestElementString);

        long waitTime = 0;
        if (termInfoDataMsg.getRequestMessageType() != null) {
            if (termInfoDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = termInfoDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
    //    ExecutorRunnable er = new ExecutorRunnable();        
        return execute(new GetTermInfoHandler(termInfoDataMsg), waitTime);
    }
    
    private OMElement execute(RequestHandler handler, long waitTime)throws I2B2Exception{
    //do Ontology query processing inside thread, so that  
    // service could sends back message with timeout error.  
    	OMElement returnElement = null;
    	
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
		"You may wish to retry your last action";
    	
    	
    	ExecutorRunnable er = new ExecutorRunnable();        

    	er.setRequestHandler(handler);

    	Thread t = new Thread(er);
    	String ontologyDataResponse = null;

    	synchronized (t) {
    		t.start();

//    		try {
//    			if (waitTime > 0) {
//    				t.wait(waitTime);
//    			} else {
//    				t.wait();
//    			}
    			
    		try {
    			long startTime = System.currentTimeMillis();
    			long deltaTime = -1;
    			while((er.isJobCompleteFlag() == false)&& (deltaTime < waitTime)){
    				if (waitTime > 0) {
    					t.wait(waitTime - deltaTime);
    					deltaTime = System.currentTimeMillis() - startTime;
    				} else {
    					t.wait();
    				}
    			}

        		ontologyDataResponse = er.getOutputString();

        		if (ontologyDataResponse == null) {
        			if (er.getJobException() != null) {
        				log.error("er.jobException is " + 
        						er.getJobException().getMessage());
        		    	
        		    	log.info("waitTime is " + waitTime);
        		    	
        				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        						unknownErrorMessage);
        				ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);

        			} else if (er.isJobCompleteFlag() == false) {
        				//<result_waittime_ms>5000</result_waittime_ms>
        				String timeOuterror = "Remote server timed out \n" +    		
        				"Result waittime = " +
        				waitTime +
        				" ms elapsed,\nPlease try again";
        				log.error(timeOuterror);
        				
        				log.debug("ontology waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName() );
        				
        				ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        						timeOuterror);
        				ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);

        			} else {
        				log.error("ontology data response is null");
        			   	log.info("waitTime is " + waitTime);
        				log.debug("ontology waited " + deltaTime + "ms for " + er.getRequestHandler().getClass().getName() );
        			   	ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
        						unknownErrorMessage);
        				ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
        			}
        		}
    		} catch (InterruptedException e) {
    			log.error(e.getMessage());
    			throw new I2B2Exception("Thread error while running Ontology job ");
    		} finally {
    			t.interrupt();
    			er = null;
    			t = null;
    		}
    	}
    	returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);

    	return returnElement;
    }
    
}
