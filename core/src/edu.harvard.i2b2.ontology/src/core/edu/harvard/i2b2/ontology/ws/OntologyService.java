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

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.delegate.GetCategoriesHandler;
import edu.harvard.i2b2.ontology.delegate.GetChildrenHandler;
import edu.harvard.i2b2.ontology.delegate.GetCodeInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetNameInfoHandler;
import edu.harvard.i2b2.ontology.delegate.GetSchemesHandler;
import edu.harvard.i2b2.ontology.delegate.GetTermInfoHandler;

import org.apache.axiom.om.OMElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;


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

        if (getChildrenElement == null) {
            log.error("Incoming Ontology request is null");
            throw new I2B2Exception("Incoming Ontology request is null");
        }
        String requestElementString = getChildrenElement.toString();
        
        GetChildrenDataMessage childrenDataMsg = new GetChildrenDataMessage(requestElementString);
        long waitTime = 0;
        if(childrenDataMsg == null)
        	log.debug("childrenDataMsg is null");
        if (childrenDataMsg.getRequestMessageType() != null) {
            if (childrenDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = childrenDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.     
        ExecutorRunnable er = new ExecutorRunnable();        
        
        er.setRequestHandler(new GetChildrenHandler(childrenDataMsg));
        
        Thread t = new Thread(er);
        String ontologyDataResponse = null;

        synchronized (t) {
            t.start();

            try {
                if (waitTime > 0) {
                    t.wait(waitTime);
                } else {
                    t.wait();
                }

                ontologyDataResponse = er.getOutputString();

                if (ontologyDataResponse == null) {
                    if (er.getJobException() != null) {
                      //  throw er.getJobException();
                    	ontologyDataResponse = "";
                    	log.error("Error response is null");
                    	throw new I2B2Exception("Error response is null");
                    } 
                    else if (er.isJobCompleteFlag() == false) {
                        //<result_waittime_ms>5000</result_waittime_ms>
                        String timeOuterror = "Remote server timed out \n" +    		
                        "Result waittime = " +
                            waitTime +
                            " ms elapsed,\nPlease try again";
                        log.debug(timeOuterror);
                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
                                timeOuterror);
                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
                    } 
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new I2B2Exception("Thread error while running Ontology job " +
                    requestElementString, e);
            } finally {
                t.interrupt();
                er = null;
                t = null;
            }
        }

        try {
            returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        } catch (XMLStreamException e) {
           log.error(e.getMessage());
            log.error("Error creating OMElement from response string " +
            		ontologyDataResponse, e);
        }

        return returnElement;
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

        if (getCategoriesElement == null) {
            log.error("Incoming Ontology request is null");
            throw new I2B2Exception("Incoming Ontology request is null");
        }

        String requestElementString = getCategoriesElement.toString();

    //    log.info(requestElementString);
        GetCategoriesDataMessage categoriesDataMsg = new GetCategoriesDataMessage(requestElementString);
        long waitTime = 0;
        if(categoriesDataMsg == null)
        	log.debug("categoriesDataMsg is null");
        if (categoriesDataMsg.getRequestMessageType() != null) {
            if (categoriesDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = categoriesDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
        ExecutorRunnable er = new ExecutorRunnable();        

        er.setRequestHandler(new GetCategoriesHandler(categoriesDataMsg));
        
        Thread t = new Thread(er);
        String ontologyDataResponse = null;

        synchronized (t) {
            t.start();

            try {
                if (waitTime > 0) {
                    t.wait(waitTime);
                } else {
                    t.wait();
                }

                ontologyDataResponse = er.getOutputString();

                if (ontologyDataResponse == null) {
                    if (er.getJobException() != null) {
                      //  throw er.getJobException();
                    } else if (er.isJobCompleteFlag() == false) {
                        //<result_waittime_ms>5000</result_waittime_ms>
                        String timeOuterror = "Remote server timed out \n" +    		
                        "Result waittime = " +
                            waitTime +
                            " ms elapsed,\nPlease try again";
                        log.info(timeOuterror);

                        //throw new I2B2Exception("Timeout lapsed, try incresing it :  " + waitTime);
                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
                                timeOuterror);
                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
                    } else {
                        log.error("Error response is null");
                        throw new I2B2Exception("Error response is null");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new I2B2Exception("Thread error while running Ontology job " +
                    requestElementString, e);
            } finally {
                t.interrupt();
                er = null;
                t = null;
            }
        }

        try {
            returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        } catch (XMLStreamException e) {
            log.error("Error creating OMElement from response string " +
            		ontologyDataResponse, e);
        }

        return returnElement;
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

        if (getSchemesElement == null) {
            log.error("Incoming Ontology request is null");
            throw new I2B2Exception("Incoming Ontology request is null");
        }

        String requestElementString = getSchemesElement.toString();

   //     log.info(requestElementString);
        
        GetSchemesDataMessage schemesDataMsg = new GetSchemesDataMessage(requestElementString);
        long waitTime = 0;
        if(schemesDataMsg == null)
        	log.debug("schemesDataMsg is null");
        if (schemesDataMsg.getRequestMessageType() != null) {
            if (schemesDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = schemesDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
        ExecutorRunnable er = new ExecutorRunnable();        

        er.setRequestHandler(new GetSchemesHandler(schemesDataMsg));
        
        Thread t = new Thread(er);
        String ontologyDataResponse = null;

        synchronized (t) {
            t.start();

            try {
                if (waitTime > 0) {
                    t.wait(waitTime);
                } else {
                    t.wait();
                }

                ontologyDataResponse = er.getOutputString();

                if (ontologyDataResponse == null) {
                    if (er.getJobException() != null) {
                      //  throw er.getJobException();
                    } else if (er.isJobCompleteFlag() == false) {
                        //<result_waittime_ms>5000</result_waittime_ms>
                        String timeOuterror = "Remote server timed out \n" +    		
                        "Result waittime = " +
                            waitTime +
                            " ms elapsed,\nPlease try again";
                        log.debug(timeOuterror);
                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
                                timeOuterror);
                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
                    } else {
                        log.error("Error response is null");
                        throw new I2B2Exception("Error response is null");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new I2B2Exception("Thread error while running Ontology job " +
                    requestElementString, e);
            } finally {
                t.interrupt();
                er = null;
                t = null;
            }
        }

        try {
            returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            log.error("Error creating OMElement from response string " +
            		ontologyDataResponse, e);
        }

        return returnElement;
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

        if (getCodeInfoElement == null) {
            log.error("Incoming Ontology request is null");
            throw new I2B2Exception("Incoming Ontology request is null");
        }

        String requestElementString = getCodeInfoElement.toString();

    //    log.info(requestElementString);
        
        GetCodeInfoDataMessage codeInfoDataMsg = new GetCodeInfoDataMessage(requestElementString);
        long waitTime = 0;
        if(codeInfoDataMsg == null)
        	log.debug("categoriesDataMsg is null");
        if (codeInfoDataMsg.getRequestMessageType() != null) {
            if (codeInfoDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = codeInfoDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
        ExecutorRunnable er = new ExecutorRunnable();        
        
        er.setRequestHandler(new GetCodeInfoHandler(codeInfoDataMsg));
        
        Thread t = new Thread(er);
        String ontologyDataResponse = null;

        synchronized (t) {
            t.start();

            try {
                if (waitTime > 0) {
                    t.wait(waitTime);
                } else {
                    t.wait();
                }

                ontologyDataResponse = er.getOutputString();

                if (ontologyDataResponse == null) {
                    if (er.getJobException() != null) {
                      //  throw er.getJobException();
                    } else if (er.isJobCompleteFlag() == false) {
                        String timeOuterror = "Remote server timed out \n" +    		
                        "Result waittime = " +
                            waitTime +
                            " ms elapsed,\nPlease try again";
                        log.debug(timeOuterror);

                        //throw new I2B2Exception("Timeout lapsed, try incresing it :  " + waitTime);
                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
                                timeOuterror);
                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
                    } else {
                        log.error("Error response is null");
                        throw new I2B2Exception("Error response is null");
                    }
                }
            } catch (InterruptedException e) {
               log.error(e.getMessage());
                throw new I2B2Exception("Thread error while running Ontology job " +
                    requestElementString, e);
            } finally {
                t.interrupt();
                er = null;
                t = null;
            }
        }

        try {
            returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        } catch (XMLStreamException e) {
            e.printStackTrace();
            log.error("Error creating OMElement from response string " +
            		ontologyDataResponse, e);
        }

        return returnElement;
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

        if (getNameInfoElement == null) {
            log.error("Incoming Ontology request is null");
            throw new I2B2Exception("Incoming Ontology request is null");
        }

        String requestElementString = getNameInfoElement.toString();

    //    log.info(requestElementString);
        
        GetNameInfoDataMessage nameInfoDataMsg = new GetNameInfoDataMessage(requestElementString);
        long waitTime = 0;
        if(nameInfoDataMsg == null)
        	log.info("nameInfoDataMsg is null");
        if (nameInfoDataMsg.getRequestMessageType() != null) {
            if (nameInfoDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = nameInfoDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
        ExecutorRunnable er = new ExecutorRunnable();        
        
        er.setRequestHandler(new GetNameInfoHandler(nameInfoDataMsg));
        
        Thread t = new Thread(er);
        String ontologyDataResponse = null;

        synchronized (t) {
            t.start();

            try {
                if (waitTime > 0) {
                    t.wait(waitTime);
                } else {
                    t.wait();
                }

                ontologyDataResponse = er.getOutputString();

                if (ontologyDataResponse == null) {
                    if (er.getJobException() != null) {
                      //  throw er.getJobException();
                    } else if (er.isJobCompleteFlag() == false) {
                        //<result_waittime_ms>5000</result_waittime_ms>
                        String timeOuterror = "Remote server timed out \n" +    		
                        "Result waittime = " +
                            waitTime +
                            " ms elapsed,\nPlease try again";
                        log.debug(timeOuterror);

                        //throw new I2B2Exception("Timeout lapsed, try incresing it :  " + waitTime);
                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
                                timeOuterror);
                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
                    } else {
                        log.error("Error response is null");
                        throw new I2B2Exception("Error response is null");
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new I2B2Exception("Thread error while running Ontology job " +
                    requestElementString, e);
            } finally {
                t.interrupt();
                er = null;
                t = null;
            }
        }

        try {
            returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        } catch (XMLStreamException e) {
            log.error("Error creating OMElement from response string " +
            		ontologyDataResponse, e);
        }

        return returnElement;
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

        if (getTermInfoElement == null) {
            log.error("Incoming Ontology request is null");
            throw new I2B2Exception("Incoming Ontology request is null");
        }

        String requestElementString = getTermInfoElement.toString();

    //    log.info(requestElementString);
        
        GetTermInfoDataMessage termInfoDataMsg = new GetTermInfoDataMessage(requestElementString);
        long waitTime = 0;
        if(termInfoDataMsg == null)
        	log.debug("termInfoDataMsg is null");
        if (termInfoDataMsg.getRequestMessageType() != null) {
            if (termInfoDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = termInfoDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Ontology query processing inside thread, so that  
        // service could sends back message with timeout error.
 
        ExecutorRunnable er = new ExecutorRunnable();        
        
        er.setRequestHandler(new GetTermInfoHandler(termInfoDataMsg));
        
        Thread t = new Thread(er);
        String ontologyDataResponse = null;

        synchronized (t) {
            t.start();

            try {
                if (waitTime > 0) {
                    t.wait(waitTime);
                } else {
                    t.wait();
                }

                ontologyDataResponse = er.getOutputString();

                if (ontologyDataResponse == null) {
                    if (er.getJobException() != null) {
                      //  throw er.getJobException();
                    	ontologyDataResponse = "";
                    	log.error("Error response is null");
                    	throw new I2B2Exception("Error response is null");
                    } 
                    else if (er.isJobCompleteFlag() == false) {
                        //<result_waittime_ms>5000</result_waittime_ms>
                        String timeOuterror = "Remote server timed out \n" +    		
                        "Result waittime = " +
                            waitTime +
                            " ms elapsed,\nPlease try again";
                        log.debug(timeOuterror);

                        //throw new I2B2Exception("Timeout lapsed, try incresing it :  " + waitTime);
                        ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
                                timeOuterror);
                        ontologyDataResponse = MessageFactory.convertToXMLString(responseMsgType);
                    } 
                }
            } catch (InterruptedException e) {
               log.error(e.getMessage());
                throw new I2B2Exception("Thread error while running Ontology job " +
                    requestElementString, e);
            } finally {
                t.interrupt();
                er = null;
                t = null;
            }
        }

        try {
            returnElement = MessageFactory.createResponseOMElementFromString(ontologyDataResponse);
        } catch (XMLStreamException e) {
            log.error("Error creating OMElement from response string " +
            		ontologyDataResponse, e);
        }

        return returnElement;
    }
}
