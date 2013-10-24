/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Wensong Pan, Lori Phillips - The LoginView class provides the header/banner for the
 *  i2b2 Eclipse framework
 *     Mike Mendis 
 */

package edu.harvard.i2b2.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import edu.harvard.i2b2.eclipse.login.*;

/**
 *
 */

public class LoginView extends ViewPart  {
	public static final String ID = "edu.harvard.i2b2.eclipse.loginView";
	public static String noteKey = null;

	public static final String PREFIX = "edu.harvard.i2b2.eclipse"; 
	public static final String LOGIN_VIEW_CONTEXT_ID = PREFIX + ".login_view_help_context";

	private static Log log = LogFactory.getLog(LoginView.class.getName());

	private Composite top;
	public String msTitle = ""; //i2b2 Workbench for Asthma Project";
	public String msUsername = "";
	public String msPassword = "";
	public static LoginView APP;
	private String APP_PROD = "PRODUCTION"; //Production";
	private String APP_TEST = "TEST";
	private String APP_CURRENT = APP_PROD;
	private String helpURL = ""; //http://www.i2b2.org";	
	private String logFileName = "i2b2log.html";
	private Color goColor;
	private Color backColor;
	private Composite banner;
	private CLabel titleLabel;
	private ToolBar titleToolBar;
	private Label authorizationLabel;
	private Label statusOvalLabel;
	private StatusLabelPaintListener statusLabelPaintListener;
	private Label statusLabel;
	private String OS = System.getProperty("os.name").toLowerCase();

	public static String BUTTON_TEXT_LOGIN =  "  Log in  ";

	public static String BUTTON_TEXT_LOGOUT = " Log out ";

	public Button loginButton;

	public String userLoginMode = "Login Mode";

	//host webservice for application stored in crcnavigator.properties file 
	public String webServiceName;

	// color and gui
	Color badColor;
	Color warningColor;
	Color grayColor;
	Color devColor;
	Color prodColor;
	Color testColor;


	// how much to offset the folder so tab text does not show
	// to do compute fontsize or use another way
	int tabFolderOffset = -20;

	public static UserInfoBean userInfoBean;

	// popup menu for toolbar
	public Menu menu;

	// tabfolder
	public TabFolder tabFolder;

	public TabItem tabLogin;

	public TabItem tabExp;

	public TabItem tabQuery;

	public TabItem tabOntology;

	public int tabFolderIndex;

	public static final LoginView getApp() {
		return APP;
	}

	public String getWebServiceName() {
		return webServiceName;
	}

	public void setWebServiceName(String webServiceName) {
		this.webServiceName = webServiceName;
	}

	public int getTabFolderIndex() {
		return tabFolder.getSelectionIndex();
	}

	public void setTabFolderIndex(int tabFolderIndex) {
		this.tabFolder.setSelection(tabFolderIndex);
	}


	/**
	 * The constructor.
	 */
	public LoginView() {

	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {

		parent.getShell();
		userInfoBean = UserInfoBean.getInstance();
		if(userInfoBean == null){
			log.debug("user info bean is null");
			return;
		}

		// local variable to get system fonts and colors
		Display display = parent.getDisplay();
		final Font headerFont = new Font(display, "Tahoma", 12, SWT.BOLD);
		final Font normalFont = new Font(display, "Tahoma", 12, SWT.NORMAL);
		final Font buttonFont = new Font(display, "Tahoma", 9, SWT.NORMAL);

		String environment = userInfoBean.getEnvironment();
		if (!userInfoBean.getHelpURL().equals(""))
			helpURL=userInfoBean.getHelpURL();
		// set banner color
		// if environment not specified defaults to development (dark gray)
		APP_CURRENT = environment.toUpperCase();
		if (APP_CURRENT.equals(APP_PROD)) {
			backColor = display.getSystemColor(
					SWT.COLOR_WHITE);
		} else if (APP_CURRENT.equals(APP_TEST)) {
			backColor = display.getSystemColor(
					SWT.COLOR_GRAY);
		} else {
			// default to development
			backColor = display.getSystemColor(
					SWT.COLOR_DARK_GRAY);
		}

		log.info("Currently running in: " + APP_CURRENT);
		final Color foreColor = display.getSystemColor(SWT.COLOR_BLACK);
		warningColor = display.getSystemColor(SWT.COLOR_YELLOW);
		
		goColor = display.getSystemColor(SWT.COLOR_GREEN);
		badColor = display.getSystemColor(SWT.COLOR_RED);

		// create top composite
		top = new Composite(parent, SWT.NONE);

		FormLayout topCompositeLayout = new FormLayout();
		top.setLayout(topCompositeLayout);

		// BannerC composite
		banner = new Composite(top, SWT.NONE);

		FormData bannerData = new FormData();
		bannerData.left = new FormAttachment(0);
		bannerData.right = new FormAttachment(100);
		banner.setLayoutData(bannerData);

		// The Banner itself is configured and layout is set
		FormLayout bannerLayout = new FormLayout();
		bannerLayout.marginWidth = 2;
		if (OS.startsWith("windows"))
			bannerLayout.marginHeight = 8;
		else
			bannerLayout.marginHeight = 18;
		bannerLayout.spacing = 5;
		banner.setLayout(bannerLayout);

		banner.setBackground(backColor);
		banner.setForeground(foreColor);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(banner, LOGIN_VIEW_CONTEXT_ID);

		// add banner components and then configure layout
		// the label on the left is added
		titleLabel = new CLabel(banner, SWT.NO_FOCUS);
		titleLabel.setBackground(backColor);
		msTitle = System.getProperty("applicationName") + " Workbench for "+ System.getProperty("projectName") + " Project";
		titleLabel.setText(msTitle);
		titleLabel.setFont(headerFont);
		titleLabel.setForeground(foreColor);
		titleLabel.setImage(new Image(display, LoginView.class.getResourceAsStream("big-hive.gif")));

		// the general application area toolbar is added
		titleToolBar = new ToolBar(banner, SWT.FLAT);
		titleToolBar.setBackground(backColor);
		titleToolBar.setFont(headerFont);

		menu = new Menu(banner.getShell(), SWT.POP_UP);

		// Authorization label is made
		authorizationLabel = new Label(banner, SWT.NO_FOCUS);
		authorizationLabel.setBackground(backColor);
		authorizationLabel.setText(userInfoBean.getUserFullName());
		authorizationLabel.setAlignment(SWT.RIGHT);
		authorizationLabel.setFont(normalFont);
		authorizationLabel.setForeground(foreColor);
		ArrayList<String> roles = (ArrayList<String>) userInfoBean.getProjectRoles(System.getProperty("projectName"));

		String rolesStr = "";
		if (roles != null)
		{
			for (String param :roles)
				rolesStr += param + "\n";
			if (rolesStr.length() > 1)
				rolesStr = rolesStr.substring(0, rolesStr.length()-1);
		}
		authorizationLabel.setToolTipText(rolesStr);

		// the staus indicator is shown
		statusLabel = new Label(banner, SWT.NO_FOCUS);
		statusLabel.setBackground(backColor);
		statusLabel.setText("Status:");
		statusLabel.setAlignment(SWT.RIGHT);
		statusLabel.setFont(normalFont);
		statusLabel.setForeground(foreColor);

		statusOvalLabel = new Label(banner, SWT.NO_FOCUS);
		statusOvalLabel.setBackground(backColor);

		statusOvalLabel.setSize(20,20);
		statusOvalLabel.setForeground(foreColor);
		statusOvalLabel.redraw();

		statusOvalLabel.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event arg0) {
				statusOvalLabel.setSize(20,20);
				statusOvalLabel.redraw();
			}
		});

		// add selection listener so that clicking on status oval label shows error log
		// dialog
		statusOvalLabel.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event arg0) {
				//log.info(getNow() + "Status Listener Clicked");
				Display display = statusOvalLabel.getDisplay();
				final Shell shell = statusOvalLabel.getShell();
				// run asyncExec so that other pending ui events finished first
				display.asyncExec(new Runnable() {

					public void run() {
						File file = new File(logFileName);
						URL url = null;
						// Convert the file object to a URL with an absolute path
						try {
							url = file.toURL();
						} catch (MalformedURLException e) {
							log.error(e.getMessage());
						}
						final URL myurl = url;
						new HelpBrowser().run(myurl.toString(),shell);		
					}
				});

			}
		});

		// add status label paint listener so that it changes color
		statusLabelPaintListener = new StatusLabelPaintListener();
		statusOvalLabel.addPaintListener(statusLabelPaintListener);

		String cellStatus = getCellStatus();
		if (cellStatus == null)
		{
			statusLabelPaintListener.setOvalColor(goColor);
		}
		else
		{
			statusOvalLabel.setToolTipText("Cells Unavailable:\n" + cellStatus);
			statusLabelPaintListener.setOvalColor(warningColor);
		}

		statusOvalLabel.setSize(20,20);
		statusOvalLabel.redraw();

		// Help button is made
		final Button rightButton = new Button(banner, SWT.PUSH | SWT.LEFT);
		rightButton.setFont(buttonFont);
		rightButton.setText(" Wiki ");

		rightButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				final Button myButton = (Button) event.widget;
				Display display=myButton.getDisplay();
				final Shell myShell=myButton.getShell();
				display.asyncExec(new Runnable() {

					public void run() {
						new HelpBrowser().run(helpURL,myShell);
					}
				});	
			}
		});

		// attach titlelabel to left and align vertically with tool bar
		FormData titleLabelFormData = new FormData();

		titleLabelFormData.top = new FormAttachment(titleToolBar, 0, SWT.CENTER);
		titleLabelFormData.left = new FormAttachment(0, 10);
		titleLabel.setLayoutData(titleLabelFormData);

		// attach left of tool bar to title label, attach top to banner
		// attach right to authorization label so that it will resize and remain
		// visible when tool bar text changes
		FormData titleToolBarFormData = new FormData();
		titleToolBarFormData.left = new FormAttachment(titleLabel);
		titleToolBarFormData.top = new FormAttachment(0);
		titleToolBarFormData.right = new FormAttachment(authorizationLabel, 0, 0);

		titleToolBar.setLayoutData(titleToolBarFormData);

		// attach authorization label on right to status label and center
		// vertically

		FormData authorizationLabelFormData = new FormData();
		authorizationLabelFormData.right = new FormAttachment(statusLabel, -10);
		authorizationLabelFormData.top = new FormAttachment(statusLabel, 0,
				SWT.CENTER);
		authorizationLabel.setLayoutData(authorizationLabelFormData);

		FormData statusLabelFormData = new FormData();
		// statusLabelFormData.right = new FormAttachment(rightButton,0);
		statusLabelFormData.right = new FormAttachment(statusOvalLabel, 0);
		statusLabelFormData.top = new FormAttachment(statusOvalLabel, 0, SWT.CENTER);
		statusLabel.setLayoutData(statusLabelFormData);

		// attach status label on right to loginbutton and center vertically

		FormData statusOvalLabelFormData = new FormData();
		//add offset 
		statusOvalLabelFormData.right = new FormAttachment(rightButton, -25);
		statusOvalLabelFormData.top = new FormAttachment(rightButton, 0, SWT.CENTER);
		statusOvalLabel.setLayoutData(statusOvalLabelFormData);

		// attach right button to right of banner and center vertically on
		// toolbar
		FormData rightButtonFormData = new FormData();
		rightButtonFormData.right = new FormAttachment(100, -10);
		rightButtonFormData.top = new FormAttachment(titleToolBar, 0, SWT.CENTER);
		rightButton.setLayoutData(rightButtonFormData);
	}

	private String getCellStatus()
	{
		StringBuffer result = new StringBuffer();
		for (String cellID: userInfoBean.getCellList())
		{
			try {
			    URL url = new URL(userInfoBean.getCellDataUrl(cellID));
			    URLConnection connection = url.openConnection();
			    connection.connect();
			} catch (MalformedURLException e) {     // new URL() failed
			    log.debug(e.getMessage());
			    result.append(userInfoBean.getCellName(cellID));
			    result.append("\n");
			} catch (IOException e) {               // openConnection() failed
			    log.debug(e.getMessage());
			    result.append(userInfoBean.getCellName(cellID));
			    result.append("\n");
			}
		}
		if (result.length() == 0)
			return null;
		else
			return result.toString();
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		banner.setFocus();
	}

	/**
	 * opens login dialog
	 * 
	 * @param parentShell
	 */
	public void showLoginD(Shell parentShell) {
		LoginDialog loginDNew = new LoginDialog(parentShell);
		loginDNew.open();
	}


	/**
	 * opens help broswer in another display and separate thread
	 * 
	 * @param button-
	 *            the control that calls this method
	 * @returns a thread that creates a SWT browser control
	 */
	public Thread showHelpBrowser(Button button) {
		final Shell shell=button.getShell();

		return new Thread() {
			@Override
			public void run() {
				new HelpBrowser().run(helpURL,shell);
			}
		};
	}

	/**
	 * opens logger browser in another display and separate thread
	 * 
	 * @param shell-
	 *            the control that calls this method
	 * 
	 * @return new thread to show browser with logger html file 
	 */
	public Thread showLoggerBrowser(Shell shell) {
		final Shell myShell=shell;
		File file = new File(logFileName);
		URL url = null;
		// Convert the file object to a URL with an absolute path
		try {
			url = file.toURL();
		} catch (MalformedURLException e) {
			log.debug(e.getMessage());
		}

		final URL myurl = url;
		return new Thread() {
			@Override
			public void run() {
				new HelpBrowser().run(myurl.toString(),myShell);
			}
		};
	}

	/**
	 * sets background color of banner composite
	 * 
	 * @param bc
	 *            backcolor
	 */
	public void setBannerBackColor(Color bc) {
		banner.setBackground(bc);
		titleLabel.setBackground(bc);
		titleToolBar.setBackground(bc);
		statusLabel.setBackground(bc);
		authorizationLabel.setBackground(bc);
		statusOvalLabel.setBackground(bc);
	}

	//	 inner class for statusLabel paint listener to enable it to be redrawn
	private class StatusLabelPaintListener implements PaintListener {
		private Color ovalColor=null;
		public Color getOvalColor() {
			return ovalColor;
		}

		public void setOvalColor(Color ovalColor) {
			this.ovalColor = ovalColor;
		}

		public StatusLabelPaintListener() {
		}

		public void paintControl(PaintEvent e) {
			if(ovalColor != null) {
				e.gc.setBackground(ovalColor);
			}
			e.gc.fillOval(0, 0, 16, 16);
		}
	}

	/**
	 * get current date as string used for logi
	 * 
	 * @return
	 */
	public static String getNow() {
		return DateFormat.getDateTimeInstance().format(new Date());
	}

	public static UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

}