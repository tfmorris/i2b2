/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 */

package edu.harvard.i2b2.eclipse.plugins.pft.views;

import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.dnd.DndType;
import edu.harvard.i2b2.pftclient.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.eclipse.plugins.pft.ws.PFTJAXBUtil;
import edu.harvard.i2b2.eclipse.plugins.pft.ws.PFTServiceDriver2;
import edu.harvard.i2b2.eclipse.plugins.pft.views.PatientDataMessage;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The PFT View class provides the PFT UI View to the
 *  Eclipse framework
 * @author Lori Phillips   
 */


public class PftView extends ViewPart{

	public static final String ID = "edu.harvard.i2b2.eclipse.plugins.pft.views.pftView";
	public static final String THIS_CLASS_NAME = PftView.class.getName();

	public static final String PREFIX = "edu.harvard.i2b2.eclipse.plugins.pft"; 
	public static final String PFT_VIEW_CONTEXT_ID = PREFIX + ".pft_view_help_context";
	public static final String OS = System.getProperty("os.name").toLowerCase();

	private Log log = LogFactory.getLog(THIS_CLASS_NAME);

	private Text text1;

	/**
	 * The constructor
	 */
	public PftView() {
		//getPftProperties();
	}

	public String getNoteFromPDO(String xmlstr)
	{
		String note = null;
		try {
			JAXBUtil jaxbUtil = PFTJAXBUtil.getJAXBUtil();
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlstr);
			DndType dndType = (DndType)jaxbElement.getValue();

			if(dndType == null)
				log.info("dndType is null");
			
			PatientDataType patientDataType = (PatientDataType) new JAXBUnWrapHelper().getObjectByClass(dndType.getAny(),
					PatientDataType.class);

			if(patientDataType == null)
				log.info("patientDataType is null");
			
			note = patientDataType.getObservationFactSet().get(0).getObservationFact().get(0).getObservationBlob().toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Error marshalling Explorer drag text");
		}
		return note;

	}
	/**
	 * This is a callback that will allow us
	 * to create the tabbed viewers and initialize them.
	 * 
	 * @param parent   Composite the PFT UI is contained within
	 */
	public void createPartControl(Composite parent) {
		Font textFont = null;

		if (OS.startsWith("mac"))
			textFont = new Font(parent.getDisplay(), "Monaco", 12, SWT.NORMAL);
		else
			textFont = new Font(parent.getDisplay(), "Courier New", 10, SWT.NORMAL);

		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);

		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new FillLayout(SWT.VERTICAL));

		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.verticalAlignment = GridData.FILL;
		top.setLayoutData(layoutData);

		// Create the tab folder
		final TabFolder tabFolder = new TabFolder(top, SWT.NONE);
		TabItem one = new TabItem(tabFolder, SWT.BOTTOM);
		one.setText("Report");

		Composite reportComp = new Composite(tabFolder, SWT.NONE);
		reportComp.setLayout(new GridLayout(1, false));
		reportComp.setLayoutData(layoutData);

		text1 = new Text(reportComp, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		text1.setLayoutData(layoutData);
		text1.setFont(textFont);

		// Setup help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(text1, PFT_VIEW_CONTEXT_ID);

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };      
		// Set up drop target 
		DropTarget target = new DropTarget(text1, DND.DROP_COPY);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				text1.setText(getNoteFromPDO((String) event.data));
			}

			public void dragEnter(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}
		});


		Composite buttonComp = new Composite(reportComp, SWT.NONE);
		buttonComp.setLayout(new GridLayout(2, false));
		GridData buttonGridData = new GridData (GridData.FILL_HORIZONTAL);
		buttonComp.setLayoutData(buttonGridData);


		final TabItem two = new TabItem(tabFolder, SWT.BOTTOM);
		two.setText("Results");

		ResultsTab.setInstance(tabFolder, textFont);
		two.setControl(ResultsTab.getInstance().getTable());

		TabItem three = new TabItem(tabFolder, SWT.BOTTOM);
		three.setText("Request XML");

		final Text text3 = new Text(tabFolder, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		three.setControl(text3);
		text3.setFont(textFont);


		TabItem four = new TabItem(tabFolder, SWT.BOTTOM);
		four.setText("Response XML");	

		ResponseTab.setInstance(tabFolder, textFont);
		four.setControl(ResponseTab.getInstance().getText());

		Button runPulButton = new Button(buttonComp, SWT.PUSH);
		runPulButton.setText("Get Pulmonary Data");
		runPulButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				try {
					String input = text1.getText();
					text3.setText("Processing.....");
					log.info("Calling PFT web service");
					ResponseTab.getInstance().setText("Calling PFT Web Service");
					PatientDataMessage requestMsg = new PatientDataMessage();
					String requestString = requestMsg.doBuildXML(input);
					text3.setText(requestString);	
					getPft(requestString).start();	
					ResultsTab.getInstance().setItem("Processing results", null, null);
					tabFolder.setSelection(two);
				}	catch (Exception e1) {
					log.error(e1.getMessage());
				}	
			}
		});	

		Button clearButton = new Button(buttonComp, SWT.PUSH);
		clearButton.setText("Clear Text");
		clearButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				// clear text1 textArea
				text1.setText("");
				text1.clearSelection();
				ResultsTab.getInstance().clear();
				text3.setText("");
				ResponseTab.getInstance().clear();
			}
		});


		final Combo codeCombo = new Combo(buttonComp,SWT.READ_ONLY);
		codeCombo.add("BWH PFT Report");
		// set default category
		codeCombo.setText("BWH PFT Report");
		codeCombo.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				// Item in list has been selected
				String label = codeCombo.getItem(codeCombo.getSelectionIndex());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				// this is not an option (text cant be entered)
			}
		});


		Button sampleButton = new Button(buttonComp, SWT.PUSH);
		sampleButton.setText("Load Sample");
		sampleButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				// clear text1 textArea
				text1.clearSelection();
				String sample = getSampleText();
				text1.setText(sample);
				//    		reportCode = "LCS-I2B2:pul";
				codeCombo.setText("BWH PFT Report");
			}
		});

		one.setControl(reportComp);
	}
	/**
	 * Create a new thread to send the message to the PFT Web Service
	 *   and display the response
	 *   
	 *   @param  requestPdo    String containing the requestPdo message
	 */
	public  Thread getPft(String requestPdo) {

		final String theRequestPdo = requestPdo;
		final Display theDisplay = Display.getCurrent();
		return new Thread() {
			String theResult = "test";
			public void run(){
				try {
					// make PFT Web service call
					theResult = PFTServiceDriver2.callPft(theRequestPdo);	
					if (theResult == null)
						theResult = "No PFT response generated";
				} catch (Exception e) {
					// Exceptions are for AxisFault or OMElement generation
					//log.error(e.getMessage());
					theResult = "PFTService: " + e.getMessage();
				}
				theDisplay.syncExec(new Runnable() {
					public void run() {
						// Display the PFT web service response 
						try {
							ResponseTab.getInstance().setText(theResult);
							PatientDataMessage msg = new PatientDataMessage();
							ResultsTab.getInstance().removeLastLine();
							msg.doReadConceptCode(theResult);	
						} catch (Exception e) {
							// Log exception
							//log.error(e.getMessage());
						}
					}
				});
			}
		};
	}

	private String getSampleText() {
		String sample = "Date:  01/xx/xx \r\n" +
		"\r\n" +
		"PT: DOE, JANE                                              DATE: 01/xx/xx  \r\n" +
		"PT#: 12345678         AGE: 67   SEX: F    HT: 63.0 in      WT: 105.0 lb    \r\n" +
		"PHYSICIAN: SMITH      TECH: ABC                \r\n" +       
		"DIAGNOSIS: DYSPNEA              \r\n" +
		"SMK HX: NEVER                           \r\n" +   
		
		"                                     Pre-Drug*  \r\n" +                                
        " Spirometry          Predicted     Actual    %Pred    Actual    %Pred    %Change\r\n" +
        "FVC         (L)        2.58          2.12     82\r\n" +
        "FEV1        (L)        1.97          1.51     76\r\n" +
        "FEV1/FVC    (%)       77            71        92\r\n" +
        "FEF25-75%   (L/S)      1.79          1.09     61\r\n" +
        "FEFmax      (L/S)      5.16          2.70     52\r\n" +
        "TET         (SEC)                    9.66\r\n" +
		"                                     Pre-Drug*  \r\n" +                                
		"______________________________TREND REPORT________________________________  \r\n" +
		"     DATE      TIME        FVC         FEV1        FEV1/FVC    FEF25-75%     \r\n" +
		"                           (L)         (L)         (%)         (L/S)        \r\n" +
		"                          (PRE)       (PRE)       (PRE)       (PRE)       \r\n" +
		"-------------------------------------------------------------------------- \r\n" +
		"   01/xx/xx  08:25:40      2.12        1.51       71.36        1.09       \r\n" +
		"__________________________________________________________________________ \r\n" ; 
		return sample;
	}


	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		text1.setFocus();
	}
}
