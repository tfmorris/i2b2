/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */


package edu.harvard.i2b2.eclipse.login;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;

import edu.harvard.i2b2.eclipse.UserInfoBean;

//import edu.harvard.i2b2.common.pm.UserInfoBean;

public class LoginDialog extends Dialog {
	
	private static final Log log = LogFactory.getLog(LoginDialog.class);

	public static final String OS = System.getProperty("os.name").toLowerCase();
	
	private static LoginDialog instance = null;

	//private static final String LOGIN_FAILED_MSG = "";

	private String title; // dialog title

	private String input; // return from dialog
	
	private UserInfoBean userInfo ; //return from dialog

	private String userid; 

	private String password; 

	private String loginStatus; // message on login status
	
	private String appName = null;
	
	private String defaultProject = null;
	
	private Hashtable pmAddresses = null;

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
		
		pmAddresses = new Hashtable();
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
		
		setText("Login to "+ System.getProperty("applicationName"));
		setTitle("Log in");
		setUserid(System.getProperty("user.name"));
		setPassword("");
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
		final Label labelMsg = new Label(shell, SWT.SHADOW_NONE | SWT.CENTER);
		labelMsg.setText("Enter UserID and Password");
		if (OS.startsWith("mac"))
			labelMsg.setBounds(new Rectangle(48, 5, 183, 35));
		else
			labelMsg.setBounds(new Rectangle(70, 5, 143, 30));

		// row for project label/prompt
		Label projectLabel = new Label(shell, SWT.NULL);
		projectLabel.setText("Project: ");
		projectLabel.setBounds(new Rectangle(18, 42, 49, 18));
		
		String filename="i2b2workbench.properties";
		Properties properties = new Properties();
		//Boolean demoFlag = false;
		String[] projectNames = null;
		
	    try {
	        properties.load(new FileInputStream(filename));
	        appName = properties.getProperty("applicationName");
	        System.setProperty("applicationName", appName);
	        
	        String projects = properties.getProperty("projects");
	        projectNames = projects.split(",");
	    } 
	    catch (final IOException e) {
					MessageBox messageBox =
						   new MessageBox(shell,
						    SWT.OK|
						    SWT.ICON_ERROR);
						 messageBox.setMessage(e.getMessage());
						 messageBox.open();
							System.exit(0);
	    }
	        
		final Combo projectCombo = new Combo(shell, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
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
		projectCombo.setText(projectCombo.getItem(0));
		System.setProperty("projectName", projectCombo.getText());
		
		projectCombo.setBounds(new Rectangle(88, 38, 185, 21));
		projectCombo.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				//String selected = projectCombo.getText();
				int index = projectCombo.getSelectionIndex(); 
				statusMsg.setText((String) pmAddresses.get(projectCombo.getText()));   //index));
				System.setProperty("projectName", projectCombo.getText());
			}
			
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		
		// row for user label/prompt
		Label labelUser = new Label(shell, SWT.NULL);
		labelUser.setText("User name: ");
		labelUser.setBounds(new Rectangle(18, 68, 62, 13));

		textUser = new Text(shell, SWT.SINGLE | SWT.BORDER);
		textUser.setText(userid);
		if (OS.startsWith("mac"))
			textUser.setBounds(new Rectangle(87, 65, 185, 25));
		else
			textUser.setBounds(new Rectangle(87, 65, 185, 19));

		// row for password label/prompt
		Label labelPassword = new Label(shell, SWT.NULL);
		labelPassword.setText("Password: ");
		labelPassword.setBounds(new Rectangle(18, 92, 59, 16));
		
		textPassword = new Text(shell, SWT.SINGLE | SWT.BORDER);
		if (OS.startsWith("mac"))		
			textPassword.setBounds(new Rectangle(87, 91, 185, 25));
		else
			textPassword.setBounds(new Rectangle(87, 91, 185, 19));

		textPassword.setText(password);
		textPassword.setEchoChar('*');
		textPassword.setFocus();
		
		demoOnly = new Button(shell, SWT.CHECK);
		demoOnly.setText("Start as demonstration only");
		if (OS.startsWith("mac"))	
			demoOnly.setBounds(new Rectangle(18, 118, 210, 19));
		else
			demoOnly.setBounds(new Rectangle(18, 118, 180, 19));

		demoOnly.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {				
				if(demoOnly.getSelection() == true) {
					textUser.setEditable(false);
					textUser.setText("Demo User");
					textPassword.setEditable(false);
					textPassword.setText("");
				}
				else {
					textUser.setEditable(true);
					textUser.setText(System.getProperty("user.name"));
					textPassword.setEditable(true);
				}
			}
		});
			
		final Button help = new Button(shell, SWT.PUSH);
		if (OS.startsWith("mac"))	
			help.setBounds(new Rectangle(230, 117, 40, 25));
		else
			help.setBounds(new Rectangle(230, 117, 21, 18));

		help.setText("?");
		help.setFont(new Font(Display.getDefault(), "Tahoma", 10, SWT.BOLD));
		help.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {				
				MessageBox mBox = new MessageBox(help.getShell(), SWT.ICON_INFORMATION | SWT.OK);
        		mBox.setText("Help Message");
        		mBox.setMessage("Selecting \"Start as demonstration only\" and " +
        				"clicking \"Login\" button\n" +
        				"will start the application in demonstration mode");
        		mBox.open();
			}
		});
		
		// create ok button and add handler
		// pressing it will set userid and start login query
		final Button ok = new Button(shell, SWT.PUSH);
		ok.setText(" Login ");
		if (OS.startsWith("mac"))	
			ok.setBounds(new Rectangle(87, 144, 94, 33));
		else
			ok.setBounds(new Rectangle(147, 144, 54, 23));

		// add selection handler
		ok.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				if(demoOnly.getSelection() == true) {
					
					labelMsg.setText("Logging in ...");
					LoginThread loginThread = new LoginThread(textUser.getText()
							.trim(), textPassword.getText(),
							 (String) pmAddresses.get(projectCombo.getText()), projectCombo.getText(), true);
					// shows busy caret, spawns thread and blocks until return
					BusyIndicator.showWhile(ok.getDisplay(), loginThread);
					System.setProperty("projectName", loginThread.getUserBean().getUserDomain());
					String userName = loginThread.getUserBean().getUserName();
					log.debug("Login name for userId="+textUser.getText()
							.trim() + ", userName=" +userName );				
					// if login fails, set message text and return to dialog
					if (userName == null) {
						log.debug("Login Fail for userid="+textUser.getText().trim());
						//log.info("Login Fail for userid="+textUser.getText().trim());
						labelMsg.setForeground(labelMsg.getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
						labelMsg.setText(loginThread.getMsg());
								//LOGIN_FAILED_MSG + " " +System.getProperty("statusMessage"));
						textUser.setText(textUser.getText().trim());
						
					} else {
						//login succeeded, return userInfoBean and close dialog
						labelMsg.setText("Login OK. " + userName);
						textUser.setText(textUser.getText().trim());
						//return UserInfo object
						userInfo=loginThread.getUserBean();
						shell.close();
					}					
				}
				else {
					//create thread for web call - populates UserInfo object
					labelMsg.setText("Logging in ...");
					LoginThread loginThread = new LoginThread(textUser.getText()
							.trim(), textPassword.getText(), (String) pmAddresses.get(projectCombo.getText()), projectCombo.getText(), false);
					// shows busy caret, spawns thread and blocks until return
					BusyIndicator.showWhile(ok.getDisplay(), loginThread);
					String userName = loginThread.getUserBean().getUserName();
					log.debug("Login name for userId="+textUser.getText()
							.trim() + ", userName=" +userName );				
					// if login fails, set message text and return to dialog
					if (userName == null) {
						log.debug("Login Fail for userid="+textUser.getText().trim());
						//log.info("Login Fail for userid="+textUser.getText().trim());
						labelMsg.setForeground(labelMsg.getParent().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
						
						labelMsg.setText(loginThread.getMsg());
						
						//labelMsg.setText("Unable to connect to server.");
						//LOGIN_FAILED_MSG + " " +System.getProperty("statusMessage"));
						textUser.setText(textUser.getText().trim());
						
					} else {
						//login succeeded, return userInfoBean and close dialog
						labelMsg.setText("Login OK. " + userName);
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
		cancel.setText("Cancel");
		if (OS.startsWith("mac"))	
			cancel.setBounds(new Rectangle(187, 144, 90, 33));
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
		statusMsg.setText((String) pmAddresses.get(defaultProject));
		Font font = statusMsg.getFont();
		//font.size = 10; //only for mac
		statusMsg.setFont(font);
		statusMsg.setBounds(new Rectangle(4, 182, 281, 27));

		// Set the OK button as the default
		shell.setDefaultButton(ok);	
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