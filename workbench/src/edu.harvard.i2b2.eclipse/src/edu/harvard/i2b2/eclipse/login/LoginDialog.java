/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 
 * 	Wensong Pan
 */


package edu.harvard.i2b2.eclipse.login;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.bind.JAXBElement;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.*;
import org.eclipse.swt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.util.Messages;
import edu.harvard.i2b2.eclipse.util.ProjectManagementJAXBUtil;

//import edu.harvard.i2b2.common.pm.UserInfoBean;

public class LoginDialog extends Dialog {

	private static final Log log = LogFactory.getLog(LoginDialog.class);

	public static final String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

	private static LoginDialog instance = null;

	//private static final String LOGIN_FAILED_MSG = "";

	private String title; // dialog title

	private String input; // return from dialog

	private UserInfoBean userInfo ; //return from dialog

	private String userid; 

	private String password; 

	private String loginStatus; // message on login status

	private String appName = null;

	//private String defaultProject = null;
	private Project currentPrj = null;

	private ArrayList<Project> projects = new ArrayList<Project>();
	//private Project defaultProject; 
	//private Hashtable pmAddresses = null;

	//private String pmAddress = null;

	public String getLoginStatus() {
		return loginStatus;
	}

	public void setStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public void setUserInfoBean(UserInfoBean info){
		this.userInfo = info;
	}

	public UserInfoBean getUserInfoBean(){
		return userInfo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the parent
	 */
	public LoginDialog(Shell parent) {

		// Pass the default styles here
		this(parent, SWT.DIALOG_TRIM | SWT.ON_TOP);
		if (instance == null)
			instance = this;	

		//pmAddresses = new Hashtable();
	}

	/**
	 * constructor
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */
	public LoginDialog(Shell parent, int style) {
		// Let caller override the default styles
		super(parent, style);
		if (instance == null) {
			instance = this;
		}

		setText(Messages.getString("LoginDialog.Text")+ System.getProperty("applicationName")); //$NON-NLS-1$ //$NON-NLS-2$
		setTitle(Messages.getString("LoginDialog.Title")); //$NON-NLS-1$
		setUserid(System.getProperty("user.name")); //$NON-NLS-1$
		setPassword(""); //$NON-NLS-1$


	}

	public static LoginDialog getInstance() {
		return instance;
	}

	/**
	 * Opens dialog and returns the input in a UserInfo object
	 * 
	 * @return  UserInfo object
	 */
	public UserInfoBean open() {
		// Create the dialog window
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setSize(new Point(295, 238));
		shell.setLocation(400, 200);	
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		// Return the entered value or null
		return userInfo;
	}

	/**
	 * Creates the dialog's contents
	 * 
	 * @param shell
	 *            the dialog window
	 */
	private void createContents(final Shell shell) {
		shell.setLayout(null);

		// row for login message
		final Label labelMsg = new Label(shell, SWT.CENTER | SWT.SHADOW_NONE | SWT.WRAP);
		labelMsg.setText(Messages.getString("HiveLoginDialog.EnterCredential")); //$NON-NLS-1$
		if (OS.startsWith("mac")) //$NON-NLS-1$
			labelMsg.setBounds(new Rectangle(18, 5, 267, 35));
		else
			labelMsg.setBounds(new Rectangle(18, 5, 260, 30));

		// row for project label/prompt
		Label projectLabel = new Label(shell, SWT.NULL);
		projectLabel.setText(Messages.getString("LoginDialog.8")); //$NON-NLS-1$
		projectLabel.setBounds(new Rectangle(18, 42, 85, 18));

		String filename=Messages.getString("Application.PropertiesFile"); //$NON-NLS-1$
		Properties properties = new Properties();
		//Boolean demoFlag = false;
		//String[] projectNames = null;
		//ArrayList<Project> projectName = new ArrayList<Project>();
		try {
			properties.load(new FileInputStream(filename));
			appName = properties.getProperty("applicationName"); //$NON-NLS-1$

			if(appName == null || appName.equals("")  ) //$NON-NLS-1$
			{
				MessageBox messageBox =
					new MessageBox(shell,
							SWT.OK|
							SWT.ICON_ERROR);
				messageBox.setMessage(Messages.getString("LoginDialog.12")); //$NON-NLS-1$
				messageBox.open();
				log.info(Messages.getString("LoginDialog.13")); //$NON-NLS-1$
				System.exit(0);
			}

			System.setProperty("applicationName", appName); //$NON-NLS-1$

			String demoUserFlag = properties.getProperty("demoUser"); //$NON-NLS-1$
			if (demoUserFlag == null)				
				System.setProperty("demoUser", "no"); //$NON-NLS-1$ //$NON-NLS-2$
			else
				System.setProperty("demoUser", demoUserFlag); //$NON-NLS-1$

			//String projects = properties.propertyNames(); //.getProperty(appName + ".1"); //"projects");

			Enumeration propertyNames = properties.propertyNames();

			while (propertyNames.hasMoreElements()) {

				String propertyName = (String)propertyNames.nextElement();

				if (propertyName.toUpperCase().startsWith(appName.toUpperCase()))
				{
					try {
						String[] propertyValue = properties.getProperty(propertyName).split(","); //$NON-NLS-1$
						if (propertyValue.length > 2){
							Project prj = new Project();
							prj.setId(propertyName);
							prj.setName(propertyValue[0]);
							prj.setMethod(propertyValue[1]);
							prj.setUrl(propertyValue[2]);
							projects.add(prj);
							if (currentPrj == null)
								currentPrj = projects.get(0);

						}
						else {
							MessageBox messageBox =
								new MessageBox(shell,
										SWT.OK|
										SWT.ICON_ERROR);
							messageBox.setMessage(Messages.getString("LoginDialog.PMLocation1") + propertyName + Messages.getString("LoginDialog.PMLocation2")); //$NON-NLS-1$ //$NON-NLS-2$
							messageBox.open();
							log.info("PM Target location " + propertyName + " not specified properly"); //$NON-NLS-1$ //$NON-NLS-2$
							System.exit(0);
						}
					} catch (Exception ee)
					{
						ee.printStackTrace();

					}
				}

			}	        
			if(currentPrj == null) {
				MessageBox messageBox =
					new MessageBox(shell,
							SWT.OK|
							SWT.ICON_ERROR);
				messageBox.setMessage(Messages.getString("LoginDialog.NOPMProvided") + appName); //$NON-NLS-1$
				messageBox.open();
				log.info("No PM target locations were provided that have prefix of " + appName); //$NON-NLS-1$
				System.exit(0);
			}
		} 
		catch (IOException e) {
			MessageBox messageBox =
				new MessageBox(shell,
						SWT.OK|
						SWT.ICON_ERROR);
			messageBox.setMessage(e.getMessage());
			messageBox.open();
			log.error(e.getMessage());
			System.exit(0);
		}

		final Combo projectCombo = new Combo(shell, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		for (Project project: projects)
		{
			projectCombo.add(project.getName());
			//projectC


		}
		/*
		for(int i=0; i<projectNames.length; i++) {
			if(projectNames[i].toUpperCase().indexOf(appName.toUpperCase()) >= 0) {
				if (defaultProject == null)
					defaultProject = projectNames[i].substring(projectNames[i].indexOf(" ")+1, 
							projectNames[i].lastIndexOf(" "));

				projectCombo.add(projectNames[i].substring(projectNames[i].indexOf(" ")+1, 
						projectNames[i].lastIndexOf(" ")));
				pmAddresses.put(projectNames[i].substring(projectNames[i].indexOf(" ")+1, 
						projectNames[i].lastIndexOf(" ")), 
						projectNames[i].substring(projectNames[i].indexOf("[")+1, projectNames[i].indexOf("]")));
			}
		}
		 */
		projectCombo.setText(projectCombo.getItem(0));
		System.setProperty("projectName", projectCombo.getText()); //$NON-NLS-1$
		//if(projectCombo.getItem(0).equalsIgnoreCase("rpdr")) {

		//	}

		projectCombo.setBounds(new Rectangle(104, 38, 170, 21));
		projectCombo.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				//String selected = projectCombo.getText();
				int index = projectCombo.getSelectionIndex(); 
				currentPrj = getProject(index);

				statusMsg.setText(currentPrj.getUrl());

				//		(String) pmAddresses.get(projectCombo.getText()));   //index));
				System.setProperty("projectName", projectCombo.getText()); //$NON-NLS-1$

				if(projectCombo.getItem(0).equalsIgnoreCase("rpdr")) { //$NON-NLS-1$
					textUser.setText("partners\\"+userid); //$NON-NLS-1$
				}
			}

			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});

		// row for user label/prompt
		Label labelUser = new Label(shell, SWT.NULL);
		labelUser.setText(Messages.getString("LoginDialog.UserName")); //$NON-NLS-1$
		labelUser.setBounds(new Rectangle(18, 68, 62, 13));

		textUser = new Text(shell, SWT.SINGLE | SWT.BORDER);
		textUser.setText(userid);
		if(projectCombo.getItem(0).equalsIgnoreCase("rpdr")) { //$NON-NLS-1$
			textUser.setText("partners\\"+userid); //$NON-NLS-1$
		}
		if (OS.startsWith("mac")) //$NON-NLS-1$
			textUser.setBounds(new Rectangle(104, 65, 170, 27));
		else
			textUser.setBounds(new Rectangle(104, 65, 170, 21));

		// row for password label/prompt
		Label labelPassword = new Label(shell, SWT.NULL);
		labelPassword.setText(Messages.getString("LoginDialog.Password")); //$NON-NLS-1$
		labelPassword.setBounds(new Rectangle(18, 92, 59, 16));

		textPassword = new Text(shell, SWT.SINGLE | SWT.BORDER);
		if (OS.startsWith("mac"))		 //$NON-NLS-1$
			textPassword.setBounds(new Rectangle(104, 93, 170, 25));
		else
			textPassword.setBounds(new Rectangle(104, 91, 170, 21));

		textPassword.setText(password);
		textPassword.setEchoChar('*');
		textPassword.setFocus();

		demoOnly = new Button(shell, SWT.CHECK);
		demoOnly.setText(Messages.getString("LoginDialog.StartInDemo")); //$NON-NLS-1$
		if (OS.startsWith("mac"))	 //$NON-NLS-1$
			demoOnly.setBounds(new Rectangle(18, 118, 210, 19));
		else
			demoOnly.setBounds(new Rectangle(18, 118, 180, 19));

		if (System.getProperty("demoUser").equals("no")){ //$NON-NLS-1$ //$NON-NLS-2$
			demoOnly.setEnabled(false);
		}

		demoOnly.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {				
				if(demoOnly.getSelection() == true) {
					textUser.setEditable(false);
					textUser.setText(Messages.getString("LoginDialog.DemoUserText")); //$NON-NLS-1$
					textPassword.setEditable(false);
					textPassword.setText(""); //$NON-NLS-1$
				}
				else {
					textUser.setEditable(true);
					textUser.setText(System.getProperty("user.name")); //$NON-NLS-1$
					textPassword.setEditable(true);
				}
			}
		});

		final Button help = new Button(shell, SWT.PUSH);
		if (OS.startsWith("mac"))	 //$NON-NLS-1$
			help.setBounds(new Rectangle(230, 117, 40, 25));
		else
			help.setBounds(new Rectangle(230, 117, 21, 18));

		help.setText("?"); //$NON-NLS-1$
		help.setFont(new Font(Display.getDefault(), "Tahoma", 10, SWT.BOLD)); //$NON-NLS-1$
		help.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {				
				MessageBox mBox = new MessageBox(help.getShell(), SWT.ICON_INFORMATION | SWT.OK);
				mBox.setText(Messages.getString("LoginDialog.HelpPopup")); //$NON-NLS-1$
				mBox.setMessage(Messages.getString("LoginDialog.HelpPopupText"));  //$NON-NLS-1$
				mBox.open();
			}
		});

		// create ok button and add handler
		// pressing it will set userid and start login query
		final Button ok = new Button(shell, SWT.PUSH);
		ok.setText(Messages.getString("LoginDialog.ButtonLogin")); //$NON-NLS-1$
		if (OS.startsWith("mac"))	 //$NON-NLS-1$
			ok.setBounds(new Rectangle(87, 144, 94, 30));
		else
			ok.setBounds(new Rectangle(147, 144, 54, 23));

		// add selection handler
		ok.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				labelMsg.setText(Messages.getString("LoginDialog.LogginIn")); //$NON-NLS-1$
				currentPrj = getProject(projectCombo.getSelectionIndex());
				//pmAddress = getProject(projectCombo.getText()).getUrl();
				System.setProperty("webServiceMethod", currentPrj.getMethod()); //$NON-NLS-1$
				//(String) pmAddresses.get(projectCombo.getText());
				if(demoOnly.getSelection() == true) {

					//labelMsg.setText("Logging in ...");
					LoginThread loginThread = new LoginThread(textUser.getText()
							.trim(), textPassword.getText(),
							currentPrj.getUrl(),
							// (String) pmAddresses.get(projectCombo.getText()),
							projectCombo.getText(), true);
					// shows busy caret, spawns thread and blocks until return
					BusyIndicator.showWhile(ok.getDisplay(), loginThread);
					System.setProperty("projectName", loginThread.getUserBean().getUserDomain()); //$NON-NLS-1$
					String userName = loginThread.getUserBean().getUserName();
					log.debug("Login name for userId="+textUser.getText() //$NON-NLS-1$
							.trim() + ", userName=" +userName );				 //$NON-NLS-1$
					// if login fails, set message text and return to dialog
					if (userName == null) {
						log.debug("Login Fail for userid="+textUser.getText().trim()); //$NON-NLS-1$
						//log.info("Login Fail for userid="+textUser.getText().trim());
						labelMsg.setForeground(labelMsg.getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
						labelMsg.setText(loginThread.getMsg());
						//LOGIN_FAILED_MSG + " " +System.getProperty("statusMessage"));
						textUser.setText(textUser.getText().trim());

					} else {
						//login succeeded, return userInfoBean and close dialog
						labelMsg.setText(Messages.getString("LoginDialog.LoginOK") + userName); //$NON-NLS-1$
						textUser.setText(textUser.getText().trim());
						//return UserInfo object
						userInfo=loginThread.getUserBean();
						shell.close();
					}					
				}
				else {
					String workbenchversion = getWorkbenchMessageVersion();
					String hiveversion = getHiveMessageVersion();

					if (workbenchversion == null) {
						MessageBox messageBox =
							new MessageBox(shell,
									SWT.OK|
									SWT.ICON_ERROR);
						messageBox.setMessage(Messages.getString("LoginDialog.messageVersionNotInProperty")); //$NON-NLS-1$
						messageBox.open();
						log.info("messageversion is missing from properties file"); //$NON-NLS-1$
						System.exit(0);

					}

					if (hiveversion == null) {
						
						labelMsg.setForeground(labelMsg.getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));

						labelMsg.setText(Messages.getString("LoginDialog.PMServerError"));

						//labelMsg.setText("Unable to connect to server.");
						//LOGIN_FAILED_MSG + " " +System.getProperty("statusMessage"));
						textUser.setText(textUser.getText().trim());
						
						return;
						
						//						labelMsg.setForeground(labelMsg.getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));

						//						labelMsg.setText("PM Cell's getVersion operation is not responding");
						//						log.info("PM Cell's getVersion operation is not responding");
						//						return;
					} else 	if(!workbenchversion.equalsIgnoreCase(hiveversion)) {
						Shell activeShell = shell;

						MessageBox mBox = new MessageBox(activeShell, SWT.ICON_INFORMATION | SWT.ON_TOP | SWT.RETRY | SWT.CANCEL);
						mBox.setText(Messages.getString("LoginDialog.VersionConflictPopup")); //$NON-NLS-1$

						mBox.setMessage(Messages.getString("LoginDialog.VersionConflictPopupText1") + appName + Messages.getString("LoginDialog.VersionConflictPopupText2") + workbenchversion //$NON-NLS-1$ //$NON-NLS-2$
								+Messages.getString("LoginDialog.VersionConflictPopupText3") + appName + Messages.getString("LoginDialog.VersionConflictPopupText4")); //$NON-NLS-1$ //$NON-NLS-2$

						int returnVal = mBox.open();		
						//		log.info(returnVal);

						log.info("Workbench message version " + workbenchversion + " does not match hive message version " + hiveversion); //$NON-NLS-1$ //$NON-NLS-2$
						if (returnVal == 256){
							System.exit(0);
						}
					} 


					//create thread for web call - populates UserInfo object
					//labelMsg.setText("Logging in ...");
					LoginThread loginThread = new LoginThread(textUser.getText()
							.trim(), textPassword.getText(),
							currentPrj.getUrl(),
							//(String) pmAddresses.get(projectCombo.getText()), 
							projectCombo.getText(), false);
					// shows busy caret, spawns thread and blocks until return
					BusyIndicator.showWhile(ok.getDisplay(), loginThread);
					String userName = loginThread.getUserBean().getUserName();
					log.debug("Login name for userId="+textUser.getText() //$NON-NLS-1$
							.trim() + ", userName=" +userName );				 //$NON-NLS-1$
					// if login fails, set message text and return to dialog
					if (userName == null) {
						log.debug("Login Fail for userid="+textUser.getText().trim()); //$NON-NLS-1$
						//log.info("Login Fail for userid="+textUser.getText().trim());
						labelMsg.setForeground(labelMsg.getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));

						if (loginThread.getMsg() != null)
							labelMsg.setText(loginThread.getMsg());

						//labelMsg.setText("Unable to connect to server.");
						//LOGIN_FAILED_MSG + " " +System.getProperty("statusMessage"));
						textUser.setText(textUser.getText().trim());

					} else {
						//login succeeded, return userInfoBean and close dialog
						labelMsg.setText(Messages.getString("LoginDialog.LoginOK") + userName); //$NON-NLS-1$
						textUser.setText(textUser.getText().trim());
						//return UserInfo object
						userInfo=loginThread.getUserBean();
						shell.close();					
					}
				}
			}
		});

		// Create the cancel button and add a handler
		// so that pressing it will set input to null and close window
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel"); //$NON-NLS-1$
		if (OS.startsWith("mac"))	 //$NON-NLS-1$
			cancel.setBounds(new Rectangle(187, 144, 90, 30));
		else
			cancel.setBounds(new Rectangle(217, 144, 50, 23));
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// do something here- return null
				userInfo = null;
				shell.close();
			}
		});

		statusMsg = new Label(shell, SWT.BORDER | SWT.LEFT | SWT.WRAP);
		statusMsg.setText(currentPrj.getUrl());
		//(String) pmAddresses.get(defaultProject.getName()));
		Font font = statusMsg.getFont();
		//font.size = 10; //only for mac
		statusMsg.setFont(font);
		statusMsg.setBounds(new Rectangle(4, 182, 281, 27));

		// Set the OK button as the default
		shell.setDefaultButton(ok);	
	}

	/**
	 * Method to get the message version of the workbench.
	 * 
	 * @return  version
	 * 
	 */
	private String getWorkbenchMessageVersion() {
		Properties properties = new Properties();
		String version=""; //$NON-NLS-1$
		String filename=Messages.getString("Application.PropertiesFile"); //$NON-NLS-1$
		try {
			properties.load(new FileInputStream(filename));
			version=properties.getProperty("messageversion"); //$NON-NLS-1$
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		log.info("workbench message version="+version); //$NON-NLS-1$
		return version;
	}

	/*
	private Project getProject(String id)
	{
		for (Project project: projects)
		{
			if (id.equalsIgnoreCase(project.getId()))
				return project;
		}
		return null;
	}
	 */

	private Project getProject(int id)
	{
		if (projects != null)
			return projects.get(id);
		return null;
	}

	/**
	 * Method to get the message version of the hive.
	 * 
	 * @return  version
	 * 
	 */
	private String getHiveMessageVersion() {
		StringWriter strWriter = null;
		try {
			strWriter = new StringWriter();

			edu.harvard.i2b2.pm.datavo.pm.version.RequestMessageType reqMessageType = new edu.harvard.i2b2.pm.datavo.pm.version.RequestMessageType();
			edu.harvard.i2b2.pm.datavo.pm.version.RequestMessageType.MessageBody body = new edu.harvard.i2b2.pm.datavo.pm.version.RequestMessageType.MessageBody();
			body.setGetMessageVersion(""); //$NON-NLS-1$
			reqMessageType.setMessageBody(body);

			edu.harvard.i2b2.pm.datavo.pm.version.ObjectFactory of = new edu.harvard.i2b2.pm.datavo.pm.version.ObjectFactory();
			ProjectManagementJAXBUtil.getJAXBUtil().marshaller(of.createRequest(reqMessageType), strWriter);
			//		log.info("get version request: "+strWriter.toString());

			String response = ""; //$NON-NLS-1$


			if(System.getProperty("webServiceMethod").equals("SOAP")) { //$NON-NLS-1$ //$NON-NLS-2$
				response = LoginHelper.sendSOAP(new EndpointReference(currentPrj.getUrl()), strWriter.toString(), "http://rpdr.partners.org/GetVersion", "GetVersion"); //$NON-NLS-1$ //$NON-NLS-2$
				log.info(currentPrj.getUrl());
				log.info("version response: "+response); //$NON-NLS-1$
			}
			else {
				response = LoginHelper.sendREST(new EndpointReference(
						//pmAddress + "getVersion"), 
						currentPrj.getUrl() + "getVersion"),  //$NON-NLS-1$
						strWriter.toString());
				log.info(currentPrj.getUrl() + "getVersion"); //$NON-NLS-1$
				log.info("version response: "+response); //$NON-NLS-1$
			}

			JAXBElement element = ProjectManagementJAXBUtil.getJAXBUtil().unMashallFromString(response);
			edu.harvard.i2b2.pm.datavo.pm.version.ResponseMessageType responseMessageType = 
				(edu.harvard.i2b2.pm.datavo.pm.version.ResponseMessageType) element.getValue();

			return responseMessageType.getMessageBody().getI2B2MessageVersion();
		} 
		catch (JAXBUtilException e) {
			log.error("Error marshalling get version request message"); //$NON-NLS-1$
			//e.printStackTrace();
			return null;
		} 
		catch (AxisFault e) {
			//e.printStackTrace();
			return null;
		} 
		catch (Exception e) {
			//e.printStackTrace();
			return null;
		} 
	}

	/**
	 * @param userID
	 * @param password
	 *            
	 * @return
	 *
	public String getUserLogins(String userID, String password, String project, String projectID) {
		LoginThread loginThread = new LoginThread(userID, password, project, projectID,  false);
		return "Login Thread Complete";
	}
	 */

	private Label statusMsg;
	private Button demoOnly;
	private Text textPassword;
	private Text textUser;
}