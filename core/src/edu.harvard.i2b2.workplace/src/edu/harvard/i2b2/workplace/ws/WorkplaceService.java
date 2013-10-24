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
package edu.harvard.i2b2.workplace.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.delegate.AddChildHandler;
import edu.harvard.i2b2.workplace.delegate.AnnotateChildHandler;
import edu.harvard.i2b2.workplace.delegate.DeleteChildHandler;
import edu.harvard.i2b2.workplace.delegate.GetFoldersByProjectHandler;
import edu.harvard.i2b2.workplace.delegate.GetFoldersByUserIdHandler;
import edu.harvard.i2b2.workplace.delegate.GetChildrenHandler;
import edu.harvard.i2b2.workplace.delegate.RenameChildHandler;
import edu.harvard.i2b2.workplace.delegate.MoveChildHandler;

import org.apache.axiom.om.OMElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import javax.xml.stream.XMLStreamException;


/**
 * This is webservice skeleton class. It parses incoming Workplace service requests
 * and  generates responses in the Work Data Object XML format.
 *
 */
public class WorkplaceService {
    private static Log log = LogFactory.getLog(WorkplaceService.class);

    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps a Workplace
     * query inside a vocab query request object. The response is also will be in i2b2
     * message format, which will wrap work data object. Work data object will
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
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";

    	if (getChildrenElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
        
        GetChildrenDataMessage childrenDataMsg = new GetChildrenDataMessage();
        String requestElementString = getChildrenElement.toString();
        childrenDataMsg.setRequestMessageType(requestElementString);
   
        long waitTime = 0;
        if (childrenDataMsg.getRequestMessageType() != null) {
            if (childrenDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = childrenDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }
        
        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new GetChildrenHandler(childrenDataMsg), waitTime);
        
    }
    
    /**
     * This function is main webservice interface to get vocab data
     * for a query. It uses AXIOM elements(OMElement) to conveniently parse
     * xml messages.
     *
     * It excepts incoming request in i2b2 message format, which wraps an Workplace
     * query inside a work query request object. The response is also will be in i2b2
     * message format, which will wrap work data object. Work data object will
     * have all the results returned by the query.
     *
     *
     * @param OMElement getFoldersElement
     * @return OMElement in i2b2message format
     * @throws Exception
     */
    public OMElement getFoldersByProject(OMElement getFoldersElement)
        throws Exception {
    	
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";

    	if (getFoldersElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
        
    	GetFoldersDataMessage foldersDataMsg = new GetFoldersDataMessage();
        String requestElementString = getFoldersElement.toString();
        //    log.info(requestElementString);
        foldersDataMsg.setRequestMessageType(requestElementString);

        long waitTime = 0;
        if (foldersDataMsg.getRequestMessageType() != null) {
            if (foldersDataMsg.getRequestMessageType().getRequestHeader() != null) {
                waitTime = foldersDataMsg.getRequestMessageType()
                                         .getRequestHeader()
                                         .getResultWaittimeMs();
            }
        }

        //do Workplace query processing inside thread, so that  
        // service could sends back message with timeout error.
  
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new GetFoldersByProjectHandler(foldersDataMsg), waitTime);
        
    }

    
    public OMElement getFoldersByUserId(OMElement getFoldersElement) 
    throws Exception {
    	
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    	"You may wish to retry your last action";

    	if (getFoldersElement == null) {
    		log.error("Incoming Workplace request is null");

    		ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
    				unknownErrorMessage);
    		workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}

    	GetFoldersDataMessage foldersDataMsg = new GetFoldersDataMessage();
    	String requestElementString = getFoldersElement.toString();
//  	log.info(requestElementString);
    	foldersDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (foldersDataMsg.getRequestMessageType() != null) {
    		if (foldersDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = foldersDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new GetFoldersByUserIdHandler(foldersDataMsg), waitTime);
        
    }
    
    public OMElement deleteChild(OMElement deleteNodeElement)throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (deleteNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	DeleteChildDataMessage deleteDataMsg = new DeleteChildDataMessage();
    	String requestElementString = deleteNodeElement.toString();
//  	log.info(requestElementString);
    	deleteDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (deleteDataMsg.getRequestMessageType() != null) {
    		if (deleteDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = deleteDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new DeleteChildHandler(deleteDataMsg), waitTime);
        
    }    
    
    
    public OMElement moveChild(OMElement nodeElement)throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (nodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	MoveChildDataMessage moveDataMsg = new MoveChildDataMessage();
    	String requestElementString = nodeElement.toString();
//  	log.info(requestElementString);
    	moveDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (moveDataMsg.getRequestMessageType() != null) {
    		if (moveDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = moveDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

        //do Workplace query processing inside thread, so that  
        // service could send back message with timeout error.     
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new MoveChildHandler(moveDataMsg), waitTime);
    }
    
    
    public OMElement renameChild(OMElement renameNodeElement) throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (renameNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	RenameChildDataMessage renameDataMsg = new RenameChildDataMessage();
    	String requestElementString = renameNodeElement.toString();
//  	log.info(requestElementString);
    	renameDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (renameDataMsg.getRequestMessageType() != null) {
    		if (renameDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = renameDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new RenameChildHandler(renameDataMsg), waitTime);
        
    }    
    
    public OMElement annotateChild(OMElement annotateNodeElement) throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (annotateNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	AnnotateChildDataMessage annotateDataMsg = new AnnotateChildDataMessage();
    	String requestElementString = annotateNodeElement.toString();
//  	log.info(requestElementString);
    	annotateDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (annotateDataMsg.getRequestMessageType() != null) {
    		if (annotateDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = annotateDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new AnnotateChildHandler(annotateDataMsg), waitTime);
        
    }    
    
    public OMElement addChild(OMElement addNodeElement) throws Exception {
    	OMElement returnElement = null;
    	String workplaceDataResponse = null;
    	String unknownErrorMessage = "Error message delivered from the remote server \n" +  
    			"You may wish to retry your last action";
    	
    	if (addNodeElement == null) {
    		log.error("Incoming Workplace request is null");
    		
			ResponseMessageType responseMsgType = MessageFactory.doBuildErrorResponse(null,
					unknownErrorMessage);
			workplaceDataResponse = MessageFactory.convertToXMLString(responseMsgType);
    		return MessageFactory.createResponseOMElementFromString(workplaceDataResponse);
    	}
    	
    	AddChildDataMessage addDataMsg = new AddChildDataMessage();
    	String requestElementString = addNodeElement.toString();
//  	log.info(requestElementString);
    	addDataMsg.setRequestMessageType(requestElementString);
    	
    	long waitTime = 0;
    	if (addDataMsg.getRequestMessageType() != null) {
    		if (addDataMsg.getRequestMessageType().getRequestHeader() != null) {
    			waitTime = addDataMsg.getRequestMessageType()
    			.getRequestHeader()
    			.getResultWaittimeMs();
    		}
    	}

    	//do Workplace query processing inside thread, so that  
    	// service could send back message with timeout error. 
        ExecutorRunnable er = new ExecutorRunnable();        
        return er.execute(new AddChildHandler(addDataMsg), waitTime);
        
    }    
    
    
}
