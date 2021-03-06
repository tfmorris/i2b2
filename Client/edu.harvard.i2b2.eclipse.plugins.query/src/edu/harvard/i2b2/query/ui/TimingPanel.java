/*
 * Copyright (c) 2006-2012 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v2.1 
 * which accompanies this distribution.
 * 
 * Contributors: 
 * 
 *     Wensong Pan
 *     
 */

/*
 * AnalysisPanel.java
 *
 * Created on March 3, 2009, 12:06 PM
 */

package edu.harvard.i2b2.query.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; //import java.util.Iterator;

import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBElement;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.MessageTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.PsmQryHeaderType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.PsmRequestTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryResultTypeType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ResultTypeResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.UserType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.StatusType.Condition;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.plugins.query.utils.Messages;
import edu.harvard.i2b2.query.datavo.QueryJAXBUtil;
import edu.harvard.i2b2.query.serviceClient.QueryRequestClient;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener; //import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox; //import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class TimingPanel extends javax.swing.JPanel {

	private HashMap<String, String> requestMap = new HashMap<String, String>();
	private ArrayList<QueryResultTypeType> analyses;
	// private String cellStatus = "";

	private boolean hasGraphicAnalysis = false;

	public boolean hasGraphicAnalysis() {
		return hasGraphicAnalysis;
	}

	public boolean hasMissingInfo = false;
	public ArrayList<String> missingTypes;

	private MainPanelModel model_;
	private JRadioButton jAnytimeRadioButton;
	private JRadioButton jSameVisitRadioButton;
	private JRadioButton jSameInstanceRadioButton;

	/** Creates new form AnalysisPanel */
	@SuppressWarnings("unchecked")
	public TimingPanel(MainPanelModel model) {

		analyses = new ArrayList<QueryResultTypeType>();
		model_ = model;

		String requestStr = writeResultTypeRequestXML();
		System.out.println("Generated result type request: " + requestStr);

		String response = QueryRequestClient.sendQueryRequestREST(requestStr);
		System.out.println("Result type response: " + response);
		boolean celldown = false;
		if (response.equalsIgnoreCase("CellDown")) {
			// cellStatus = new String("CellDown");
			celldown = true;
		} else {

			JAXBUtil jaxbUtil = QueryJAXBUtil.getJAXBUtil();
			try {
				JAXBElement jaxbElement = jaxbUtil
						.unMashallFromString(response);
				ResponseMessageType messageType = (ResponseMessageType) jaxbElement
						.getValue();
				String version = messageType.getMessageHeader()
						.getSendingApplication().getApplicationVersion();
				double vernum = Double.parseDouble(version);
				if (vernum < 1.4) {
					QueryResultTypeType qtype = new QueryResultTypeType();
					qtype.setDescription("Same Patient");
					qtype.setName("PATIENTSET");
					qtype.setVisualAttributeType("LA");
					qtype.setDisplayType("LIST");
					analyses.add(qtype);

					// qtype = new QueryResultTypeType();
					// qtype.setDescription("Number of patients");
					// qtype.setName("PATIENT_COUNT_XML");
					// qtype.setVisualAttributeType("LA");
					// qtype.setDisplayType("CATNUM");
					// analyses.add(qtype);

					// qtype = new QueryResultTypeType();
					// qtype.setDescription("Patient set");
					// qtype.setName("PATIENTSET");
					// qtype.setVisualAttributeType("LA");
					// qtype.setDisplayType("LIST");
					// analyses.add(qtype);
				} else {
					BodyType bt = messageType.getMessageBody();

					ResultTypeResponseType resultTypes = (ResultTypeResponseType) new JAXBUnWrapHelper()
							.getObjectByClass(bt.getAny(),
									ResultTypeResponseType.class);
					for (Condition status : resultTypes.getStatus()
							.getCondition()) {
						if (status.getType().equals("ERROR")) {
							// cellStatus = new String("CellDown");
							celldown = true;
							break;
						}
					}

					if (!celldown) {
						System.out.println("Result type size: "
								+ resultTypes.getQueryResultType().size());
						for (int i = 0; i < resultTypes.getQueryResultType()
								.size(); i++) {
							QueryResultTypeType queryResultType = resultTypes
									.getQueryResultType().get(i);
							String va = queryResultType
									.getVisualAttributeType();

							if (va != null && !va.toLowerCase().endsWith("h")) {
								analyses.add(queryResultType);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				celldown = true;
			}
		}

		initComponents();
		populateStringMap();

		jAnalysisTable.setTableHeader(null);
		jAnalysisTable.getColumnModel().getColumn(0).setPreferredWidth(290);
		jAnalysisTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

		if (celldown) {
			jAnalysisTable.setForeground(Color.RED);
			// jAnalysisTable.getModel().setValueAt(false, 0, 0);
			// jAnalysisTable.getModel().setValueAt("CRC Cell Down", 0, 1);
		} else {
			// DefaultTableModel model = (DefaultTableModel) jAnalysisTable
			// .getModel();

			// JRadioButton any = new
			// JRadioButton("Anytime in Patient History");
			// JRadioButton same = new JRadioButton("In Same Patient Visit");
			// ButtonGroup group2 = new ButtonGroup();
			// group2.add(any);
			// group2.add(same);
			// jAnalysisTable.getModel().setValueAt(any, 0, 0);
			// jAnalysisTable.getModel().setValueAt(same , 1, 0);
			// any.setSelected(true);
			// ((JRadioButton) jAnalysisTable.getValueAt(0,
			// 0)).setBackground(Color.WHITE);
			// ((JRadioButton) jAnalysisTable.getValueAt(1,
			// 0)).setBackground(Color.WHITE);
			// ButtonGroup group2 = new ButtonGroup();
			// group2.add((JRadioButton) jAnalysisTable.getValueAt(0, 0));
			// group2.add((JRadioButton) jAnalysisTable.getValueAt(1, 0));

		}
		jSameInstanceRadioButton.setEnabled(false);
	}
	
	public void enableSameInstanceRadioButton(boolean b) {
		jSameInstanceRadioButton.setEnabled(b);
	}

	public boolean isTimelineSelected() {
		DefaultTableModel model = (DefaultTableModel) jAnalysisTable.getModel();
		boolean select = false;
		// model.setRowCount(analyses.size());
		for (int i = 0; i < model.getRowCount(); i++) {
			String str = (String) model.getValueAt(i, 1);// analyses.get(i).getDescription();
			if (str == null) {
				continue;
			}

			if (str.equalsIgnoreCase("Timeline")) {
				select = ((Boolean) model.getValueAt(i, 0)).booleanValue();
			}
		}
		return select;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	private void initComponents() {
		jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1.setBorder(javax.swing.BorderFactory
				.createLineBorder(new java.awt.Color(0, 0, 0)));

		jAnytimeRadioButton = new JRadioButton("Treat all groups independently");
		jAnytimeRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jAnytimeRadioButtonActionPerformed(evt);
					}
				});

		jSameVisitRadioButton = new JRadioButton(
				"Selected groups occur in the same financial encounter");
		jSameVisitRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jSameVisitRadioButtonActionPerformed(evt);
					}
				});
		
		///adding same instance
		jSameInstanceRadioButton = new JRadioButton(
			"Items instance will be same");
		jSameInstanceRadioButton
		.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jSameInstanceRadioButtonActionPerformed(evt);
			}
		});

		DefaultTableModel dm = new DefaultTableModel();
		dm.setDataVector(new Object[][] { { jAnytimeRadioButton },
				{ jSameVisitRadioButton }, { jSameInstanceRadioButton }, { null }, { null } },
				new String[] { "Name" });

		jAnalysisTable = new JTable(dm) {
			public void tableChanged(TableModelEvent e) {
				super.tableChanged(e);
				repaint();
			}
		};
		ButtonGroup group2 = new ButtonGroup();
		group2.add((JRadioButton) dm.getValueAt(0, 0));
		group2.add((JRadioButton) dm.getValueAt(1, 0));
		group2.add((JRadioButton) dm.getValueAt(2, 0));
		((JRadioButton) dm.getValueAt(0, 0)).setBackground(Color.WHITE);
		((JRadioButton) dm.getValueAt(0, 0)).setSelected(true);
		((JRadioButton) dm.getValueAt(1, 0)).setBackground(Color.WHITE);
		((JRadioButton) dm.getValueAt(2, 0)).setBackground(Color.WHITE);

		setLayout(new java.awt.BorderLayout());

		jAnalysisTable
				.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
					public void mouseMoved(java.awt.event.MouseEvent evt) {
						jAnalysisTableMouseMoved(evt);
					}
				});

		/*
		 * jAnalysisTable.setModel(new javax.swing.table.DefaultTableModel( new
		 * Object[][] { // {true, "Timeline"}, // {true, "Patient count"}, //
		 * {null, "Gender"}, // {null, "Age"}, // {null, "Vital"}, // {null,
		 * "Race"}, // {null, "Ethnicity"}, // {null, "Hospital"}, // {null,
		 * "Status"}, { null }, { null }, { null }, { null}, { null}, { null },
		 * { null }, { null }, { null }, { null }, { null }, { null }, { null },
		 * { null }, { null }, { null }, { null }, { null }, { null } }, new
		 * String[] { "Name" }) {
		 * 
		 * @SuppressWarnings("unchecked") Class[] types = new Class[] {
		 * java.lang.Boolean.class, java.lang.String.class };
		 * 
		 * @SuppressWarnings("unchecked") public Class getColumnClass(int
		 * columnIndex) { return types[columnIndex]; } });
		 */

		jAnalysisTable.getColumn("Name").setCellRenderer(
				new RadioButtonRenderer());
		jAnalysisTable.getColumn("Name").setCellEditor(
				new RadioButtonEditor(new JCheckBox()));

		jScrollPane1.setViewportView(jAnalysisTable);
		jScrollPane1.getViewport().setBackground(Color.WHITE);

		add(jScrollPane1, java.awt.BorderLayout.CENTER);

	}

	public ArrayList<String> getSelectedAnalysis() {
		missingTypes = new ArrayList<String>();
		hasGraphicAnalysis = false;
		hasMissingInfo = false;
		ArrayList<String> strs = new ArrayList<String>();
		for (int i = 0; i < jAnalysisTable.getRowCount(); i++) {
			if (jAnalysisTable.getValueAt(i, 0) != null
					&& ((Boolean) jAnalysisTable.getValueAt(i, 0))
							.booleanValue()) {
				// String str = new String((String)jAnalysisTable.getValueAt(i,
				// 1));
				// strs.add(requestMap.get(str)==null?str:requestMap.get(str));
				String str = analyses.get(i).getName();
				if (!strs.contains(str)) {
					strs.add(str);
				}
				if (analyses.get(i).getDisplayType() == null) {
					hasMissingInfo = true;
					missingTypes.add(analyses.get(i).getDescription());
				} else {
					if (analyses.get(i).getDisplayType().equalsIgnoreCase(
							"CATNUM")) {
						hasGraphicAnalysis = true;
					}
				}
			}
		}
		return strs;
	}

	private void jSameVisitRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		System.out.println("same clicked");
		for (int i = 0; i < model_.getCurrentPanelCount(); i++) {
			GroupPanel panel = model_.getTreePanel(i);
			panel.setTimingComboText("Occurs in Same Encounter");
			panel.setTimingCombo(1);
			panel.enableTimingCombo(true);
		}
	}
	
	private void jSameInstanceRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		System.out.println("same clicked");
		for (int i = 0; i < model_.getCurrentPanelCount(); i++) {
			GroupPanel panel = model_.getTreePanel(i);
			panel.setTimingComboText("Items instance will be same");
			panel.setTimingCombo(2);
			panel.enableTimingCombo(true);
		}
	}

	private void jAnytimeRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		System.out.println("any clicked");
		for (int i = 0; i < model_.getCurrentPanelCount(); i++) {
			GroupPanel panel = model_.getTreePanel(i);
			panel.setTimingCombo(0);
			panel.enableTimingCombo(false);
		}
	}

	public void reset() {
		jAnytimeRadioButton.doClick();
	}

	public void setSame(int index) {
		if(index == 1) {
			jSameVisitRadioButton.doClick();
		}
		else {
			jSameInstanceRadioButton.doClick();
		}
	}

	// mapping the display string to the request option string
	private void populateStringMap() {

		requestMap.put("Timeline", "PATIENTSET");

		requestMap.put("Patient count", "PATIENT_COUNT_XML");

		requestMap.put("Gender", "PATIENT_GENDER_COUNT_XML");

		requestMap.put("Age", "PATIENT_AGE_COUNT_XML");

		requestMap.put("Vital", "PATIENT_VITALSTATUS_COUNT_XML");

		requestMap.put("Race", "PATIENT_RACE_COUNT_XML");
	}

	/*
	 * private String getDisplayString(String type) { Iterator<String> it =
	 * requestMap.keySet().iterator(); while (it.hasNext()) { String str =
	 * it.next(); if (type.equalsIgnoreCase(requestMap.get(str))) { return str;
	 * } } return type; }
	 */

	public String writeResultTypeRequestXML() {
		// create header
		PsmQryHeaderType headerType = new PsmQryHeaderType();
		UserType userType = new UserType();
		userType.setLogin(UserInfoBean.getInstance().getUserName());
		userType.setValue(UserInfoBean.getInstance().getUserName());
		headerType.setUser(userType);
		headerType.setRequestType(PsmRequestTypeType.CRC_QRY_GET_RESULT_TYPE);

		// ResultRequestType resultRequestType = new ResultRequestType();
		// resultRequestType.setQueryResultInstanceId(queryId);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);

		BodyType bodyType = new BodyType();
		edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory psmOf = new edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory();
		bodyType.getAny().add(psmOf.createPsmheader(headerType));
		// bodyType.getAny().add(psmOf.createRequest(resultRequestType));

		MessageHeaderType messageHeader = getMessageHeader();
		RequestMessageType requestMessageType = new RequestMessageType();
		requestMessageType.setMessageBody(bodyType);
		requestMessageType.setMessageHeader(messageHeader);
		requestMessageType.setRequestHeader(requestHeader);

		JAXBUtil jaxbUtil = QueryJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try {
			edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory of = new edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ObjectFactory();
			jaxbUtil
					.marshaller(of.createRequest(requestMessageType), strWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("Generated XML document request: " +
		// strWriter.toString());
		return strWriter.toString();
	}

	protected MessageHeaderType getMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();

		messageHeader.setI2B2VersionCompatible(new BigDecimal(Messages
				.getString("QueryData.i2b2VersionCompatible"))); //$NON-NLS-1$

		ApplicationType appType = new ApplicationType();
		appType.setApplicationName(Messages
				.getString("QueryData.SendingApplicationName")); //$NON-NLS-1$
		appType.setApplicationVersion(Messages
				.getString("QueryData.SendingApplicationVersion")); //$NON-NLS-1$
		messageHeader.setSendingApplication(appType);

		messageHeader.setAcceptAcknowledgementType(new String("messageId"));

		MessageTypeType messageTypeType = new MessageTypeType();
		messageTypeType.setEventType(Messages.getString("QueryData.EventType"));
		messageTypeType.setMessageCode(Messages
				.getString("QueryData.MessageCode"));
		messageHeader.setMessageType(messageTypeType);

		FacilityType facility = new FacilityType();
		facility.setFacilityName(Messages
				.getString("QueryData.SendingFacilityName")); //$NON-NLS-1$
		messageHeader.setSendingFacility(facility);

		ApplicationType appType2 = new ApplicationType();
		appType2.setApplicationVersion(Messages
				.getString("QueryData.ReceivingApplicationVersion")); //$NON-NLS-1$
		appType2.setApplicationName(Messages
				.getString("QueryData.ReceivingApplicationName")); //$NON-NLS-1$
		messageHeader.setReceivingApplication(appType2);

		FacilityType facility2 = new FacilityType();
		facility2.setFacilityName(Messages
				.getString("QueryData.ReceivingFacilityName")); //$NON-NLS-1$
		messageHeader.setReceivingFacility(facility2);

		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory
				.getXMLGregorianCalendar(currentDate.getTime()));

		SecurityType secType = new SecurityType();
		secType.setDomain(UserInfoBean.getInstance().getUserDomain());
		secType.setUsername(UserInfoBean.getInstance().getUserName());
		PasswordType ptype = new PasswordType();
		ptype.setIsToken(UserInfoBean.getInstance().getUserPasswordIsToken());
		ptype.setTokenMsTimeout(UserInfoBean.getInstance()
				.getUserPasswordTimeout());
		ptype.setValue(UserInfoBean.getInstance().getUserPassword());

		secType.setPassword(ptype);
		messageHeader.setSecurity(secType);

		MessageControlIdType mcIdType = new MessageControlIdType();
		mcIdType.setInstanceNum(0);
		mcIdType.setMessageNum(generateMessageId());
		messageHeader.setMessageControlId(mcIdType);

		ProcessingIdType proc = new ProcessingIdType();
		proc.setProcessingId(Messages.getString("QueryData.ProcessingId")); //$NON-NLS-1$
		proc.setProcessingMode(Messages.getString("QueryData.ProcessingMode")); //$NON-NLS-1$
		messageHeader.setProcessingId(proc);

		messageHeader.setAcceptAcknowledgementType(Messages
				.getString("QueryData.AcceptAcknowledgementType")); //$NON-NLS-1$
		messageHeader.setApplicationAcknowledgementType(Messages
				.getString("QueryData.ApplicationAcknowledgementType")); //$NON-NLS-1$
		messageHeader.setCountryCode(Messages
				.getString("QueryData.CountryCode")); //$NON-NLS-1$
		messageHeader.setProjectId(UserInfoBean.getInstance().getProjectId());
		return messageHeader;
	}

	protected String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for (int i = 0; i < 20; i++) {
			int num = getValidAcsiiValue();
			// System.out.println("Generated number: " + num +
			// " char: "+(char)num);
			strWriter.append((char) num);
		}
		return strWriter.toString();
	}

	private int getValidAcsiiValue() {
		int number = 48;
		while (true) {
			number = 48 + (int) Math.round(Math.random() * 74);
			if ((number > 47 && number < 58) || (number > 64 && number < 91)
					|| (number > 96 && number < 123)) {
				break;
			}
		}
		return number;

	}

	private void jAnalysisTableMouseMoved(java.awt.event.MouseEvent evt) {
		for (int i = 0; i < jAnalysisTable.getRowCount(); i++) {
			Rectangle rect = jAnalysisTable.getCellRect(i, 1, false);
			if (rect.contains(evt.getPoint())) {
				jAnalysisTable.setToolTipText(analyses.get(i).getDescription());
				break;
			}
		}

		// jAnalysisTable.setToolTipText("moved: "+ evt.getX()+","+evt.getY());
	}

	public boolean isSameVisit() {
		return ((JRadioButton) jAnalysisTable.getValueAt(1, 0)).isSelected();
	}
	
	public int selectedTimingIndex() {
		if(((JRadioButton) jAnalysisTable.getValueAt(0, 0)).isSelected()) {
			return 1;
		}
		else if(((JRadioButton) jAnalysisTable.getValueAt(1, 0)).isSelected()) {
			return 2;
		} else {
			return 3;
		}
	}

	// Variables declaration
	private javax.swing.JTable jAnalysisTable;
	private javax.swing.JScrollPane jScrollPane1;
	// End of variables declaration

}

class RadioButtonRenderer implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value == null)
			return null;
		return (Component) value;
	}
}

class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
	private JRadioButton button;
	private JTable table;

	public RadioButtonEditor(JCheckBox checkBox) {
		super(checkBox);
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (value == null)
			return null;
		button = (JRadioButton) value;
		button.addItemListener(this);
		return (Component) value;
	}

	public Object getCellEditorValue() {
		button.removeItemListener(this);
		return button;
	}

	public void itemStateChanged(ItemEvent e) {
		super.fireEditingStopped();
		System.out.println("radio button clicked");
	}
}
