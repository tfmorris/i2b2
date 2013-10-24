/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 */
package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.ontology.delegate.RequestHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements thread runnable interface, to do Ontology
 * processing using thread.
 */
public class ExecutorRunnable implements Runnable {
    private static Log log = LogFactory.getLog(ExecutorRunnable.class);
    private String inputString = null;
    private String outputString = null;
    private RequestHandler reqHandler = null;
    private Exception ex = null;
    private boolean jobCompleteFlag = false;

    public Exception getJobException() {
        return ex;
    }

    public boolean isJobCompleteFlag() {
        return jobCompleteFlag;
    }

    public void setJobCompleteFlag(boolean jobCompleteFlag) {
        this.jobCompleteFlag = jobCompleteFlag;
    }

    public void setJobException(Exception ex) {
        this.ex = ex;
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }
    
    public void setRequestHandler(RequestHandler handler) {
        this.reqHandler = handler;
    }

    public RequestHandler getRequestHandler() {
        return this.reqHandler;
    }
    
    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public void run() {
        try {
            outputString = reqHandler.execute();
            setJobCompleteFlag(true);
        } catch (Exception e) {
            setJobException(e);
        }

        //notify();
    }
}
