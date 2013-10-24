/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *  rk903
 *     
 */
package edu.harvard.i2b2.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * This class has the functions to convert an exception StackTrace into a string.
 * @author rk903
 */

public class StackTraceUtil {
	
    /**
     * Takes an Exception argument and returns the stacktrace.
     * 
     * @param exception
     * @return The stacktrace string
     */
    public static String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        return sw.toString();
    }
}

