/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Lori Phillips - initial API and implementation
 *     Wensong Pan
 *     Mike Mendis
 */

package edu.harvard.i2b2.eclipse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.harvard.i2b2.eclipse.login.LoginDialog;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IPlatformRunnable {

	private static Log log = LogFactory.getLog(Application.class.getName());
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		Display display = PlatformUI.createDisplay();

		log.debug("STARTING APP");
		try {
			if(loginAction(true) == false) {
				return IPlatformRunnable.EXIT_OK;
			}

			int returnCode = PlatformUI.createAndRunWorkbench(display, 
					new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IPlatformRunnable.EXIT_RESTART;
			}

			return IPlatformRunnable.EXIT_OK;
		} finally {
			display.dispose();
		}
	}
	/**
	 * Method to read in crcnavigator properties file
	 * 
	 * @return  webservicename
	 * 
	 */
	private String getCRCNavigatorProperties() {
		Properties properties = new Properties();
		String webServiceName="";
		String filename="i2b2workbench.properties";
		try {
			properties.load(new FileInputStream(filename));
			webServiceName=properties.getProperty("webservicename");
			System.setProperty("applicationName", properties.getProperty("applicationName"));

		} catch (IOException e) {
			log.error(e.getMessage());
			webServiceName="";
		}
		log.debug("webservicename="+webServiceName);
		return webServiceName;
	}

	/**
	 * this method populates controls on login/logout using UserInfoBean
	 * 
	 * @param login-
	 *            true if action is login, false if action is logout
	 * @return Boolean true if user logged in
	 * 					false if user cancelled login session
	 */
	private boolean loginAction(boolean login) {
		UserInfoBean userInfoBean = null;
		// if login action true open dialog and wait for return
		Shell activeShell = new Shell();		

		if (login) {
			getCRCNavigatorProperties();
			LoginDialog loginDialog = new LoginDialog(activeShell);	
			userInfoBean = loginDialog.open();			
		}	

		// userInfoBean null means user pressed cancel- logout and close pages
		if (userInfoBean == null) {
			log.debug( " Login cancel");
			return false;
		} else {
			// login successful
			log.debug("Login Successful");
			return true;
		}
	}



}
