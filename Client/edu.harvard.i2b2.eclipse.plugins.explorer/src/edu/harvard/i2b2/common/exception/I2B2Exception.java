/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     
 */
package edu.harvard.i2b2.common.exception;

public class I2B2Exception extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2212839315128709511L;

	/**
     * Default Constructor.
     */
    public I2B2Exception() {
    }

    /**
     * Constructor that takes message and the exception as inputs.
     * @param message
     * @param e
     */
    public I2B2Exception(String message, Exception e) {
        super(message, e);
    }

    

    /**
     * Constructor that takes message as input.
     * @param message
     */
    public I2B2Exception(String message) {
        super(message);
    }

    /**
     * Returns the known, i.e., not-null, root cause of this exception.
     *  
     * @return
     */
    public Throwable getKnownRootCause() {
    	Throwable root = this.getCause();
    	if (root == null) {
    		return this;
    	}
    	
    	while (root != null && root.getCause() != null) {
    		root = root.getCause();
    	}
    	
    	return root;
    }
}
