/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *     Wensong Pan
 *    
 *     
 */
package edu.harvard.i2b2.exportData.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader; // import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent; //import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.dnd.DndType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.common.datavo.pdo.EventSet;
import edu.harvard.i2b2.common.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType;
import edu.harvard.i2b2.common.datavo.pdo.PatientSet;
import edu.harvard.i2b2.common.datavo.pdo.PatientType;
import edu.harvard.i2b2.common.datavo.pdo.PidSet;
import edu.harvard.i2b2.common.datavo.pdo.PidType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.InstanceResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ItemType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.MasterResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.PanelType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryDefinitionType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryInstanceType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryResultInstanceType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.RequestXmlType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ResultResponseType;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.exportData.data.PDORequestMessageFactory;
import edu.harvard.i2b2.exportData.data.PDOResponseMessageFactory;
import edu.harvard.i2b2.exportData.data.PatientSetData;
import edu.harvard.i2b2.exportData.data.PatientTableRow;
import edu.harvard.i2b2.exportData.data.QueryInstanceData;
import edu.harvard.i2b2.exportData.data.QueryMasterData;
import edu.harvard.i2b2.exportData.dataModel.ConceptKTableModel;
import edu.harvard.i2b2.exportData.dataModel.ConceptTableRow; //import edu.harvard.i2b2.exportData.dataModel.ImageFactory;
import edu.harvard.i2b2.exportData.dataModel.KTable;
import edu.harvard.i2b2.exportData.dataModel.KTableCellResizeListener;
import edu.harvard.i2b2.exportData.dataModel.KTableCellSelectionListener;
import edu.harvard.i2b2.exportData.dataModel.PatientKTableModel;
import edu.harvard.i2b2.exportData.dataModel.QueryConceptData;
import edu.harvard.i2b2.exportData.dataModel.TimelineRow;
import edu.harvard.i2b2.exportData.datavo.ImageExplorerJAXBUtil;
import edu.harvard.i2b2.exportData.ui.DataExporter;
import edu.harvard.i2b2.exportData.ui.PDOQueryClient;
import edu.harvard.i2b2.exportData.ui.WaitPanel;

public class DataExporter extends Composite {
    public static String noteKey = null;

    private static final Log log = LogFactory.getLog(DataExporter.class);

    public String msTitle = "I2B2 CRC Navigator in";

    public String msUsername = "";

    public String msPassword = "";

    public boolean bWantStatusLine = false;

    private Composite oTheParent;

    private StatusLineManager slm = new StatusLineManager();

    private Table patientSetTable;

    private KTable patientTable;

    public KTable conceptTable;

    // private boolean bDebug = false;
    private Text patientMinNumText;

    private Text patientMaxNumText;

    private Text mrnlistText;

    private boolean bDisplayAllData = false;

    private boolean bDisplayDemographics = true;

    private WaitPanel p = null;

    private TabFolder tabFolder = null;

    private Thread visualizationQueryThread = null;

    private Connection oConnection = null;

    private LinkedHashMap<String, String> values = null;

    private String valueType = null;

    private int curRowNumber = 0;

    public ArrayList<ArrayList<ConceptTableRow>> rowData = null;

    public ArrayList<PatientTableRow> patientRowData = null;

    private java.awt.Frame oAwtContainer;

    private Text patientSetText;

    public boolean isAll = true;

    public boolean isSelectedPatients = false;

    public boolean isSelectedPatientIds = false;

    public int currentSelectedIndex = -1;

    private Combo queryCombo;

    private String patientRefId = null;

    public void patientRefId(String str) {
	patientRefId = new String(str);
    }

    private int patientSetSize = 0;

    private boolean drawLeft = true;

    public boolean drawLeft() {
	return drawLeft;
    }

    private String lastRequestMessage;

    public void lastRequestMessage(String str) {
	lastRequestMessage = new String(str);
    }

    public String lastRequestMessage() {
	return lastRequestMessage;
    }

    private String lastResponseMessage;

    public void lastResponseMessage(String str) {
	lastResponseMessage = new String(str);
    }

    public String lastResponseMessage() {
	return lastResponseMessage;
    }

    private ArrayList<PatientSetData> patientSets;

    public ArrayList<String> selectedPatientids;

    public ArrayList<String> selectedPatients;

    private EventSet eventSet;

    public ArrayList<ObservationType> observationList = null;

    private DataExporter explorer = this;

    private TableViewComposite tableviewComposite;

    private int currentTabIndex = 0;

    public DataExporter(Composite parent) {
	super(parent, SWT.FLAT);// |SWT.BORDER);
	// addStatusLine();
	// this.setSize(800,60);
	oTheParent = parent;
	values = new LinkedHashMap<String, String>();
	rowData = new ArrayList<ArrayList<ConceptTableRow>>();
	patientRowData = new ArrayList<PatientTableRow>();
	patientSets = new ArrayList<PatientSetData>();
	selectedPatientids = new ArrayList<String>();
	selectedPatients = new ArrayList<String>();

	createContents(parent);
    }

    public DataExporter(Composite parent, boolean drawleft) {
	super(parent, SWT.FLAT);// |SWT.BORDER);
	// addStatusLine();
	// this.setSize(800,60);
	oTheParent = parent;
	values = new LinkedHashMap<String, String>();
	rowData = new ArrayList<ArrayList<ConceptTableRow>>();
	patientRowData = new ArrayList<PatientTableRow>();
	patientSets = new ArrayList<PatientSetData>();
	selectedPatientids = new ArrayList<String>();
	selectedPatients = new ArrayList<String>();

	this.drawLeft = drawleft;
	createContents(parent);
    }

    public void setPatientSetText(final String str) {
	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		patientSetText.setText(str);
	    }
	});
    }

    public void selectTab(final int index) {
	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		tabFolder.setSelection(index);
	    }
	});
    }

    public void setPatientMinNumText(final String str) {
	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		patientMinNumText.setText(str);
	    }
	});
    }

    public void setPatientSetSize(final String str) {
	patientSetSize = new Integer(str).intValue();
	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		int maxPatientNum = new Integer(patientMaxNumText.getText())
			.intValue();
		if (patientSetSize > maxPatientNum) {
		    // rightArrowButton.setEnabled(true);
		    patientMaxNumText.setText("10");
		} else {
		    // rightArrowButton.setEnabled(false);
		    // if(patientSetSize>0) {
		    // patientMaxNumText.setText(setSize);
		    // }
		}
	    }
	});
    }

    public void populatePatientSetTable() {
	patientSetTable.removeAll();

	for (int i = 0; i < patientSets.size(); i++) {
	    PatientSetData setdata = patientSets.get(i);
	    TableItem item = new TableItem(patientSetTable, SWT.NULL);
	    item
		    .setText(new String[] { setdata.setNumber(),
			    setdata.setName() });
	}
    }

    /**
     * @param args
     */
    protected Control createContents(Composite parent) {
	log.info("Data exporter plugin version 1.3.0");
	GridLayout topGridLayout = new GridLayout(1, false);
	topGridLayout.numColumns = 1;
	topGridLayout.marginWidth = 2;
	topGridLayout.marginHeight = 2;
	setLayout(topGridLayout);

	Composite oTreeComposite = new Composite(this, SWT.NONE);
	oTreeComposite.setLayout(new FillLayout(SWT.VERTICAL));
	GridData gridData2 = new GridData();
	gridData2.horizontalAlignment = GridData.FILL;
	gridData2.verticalAlignment = GridData.FILL;
	gridData2.grabExcessHorizontalSpace = true;
	gridData2.grabExcessVerticalSpace = true;
	oTreeComposite.setLayoutData(gridData2);

	// the horizontal sash form
	SashForm horizontalForm = new SashForm(oTreeComposite, SWT.HORIZONTAL);
	horizontalForm.setOrientation(SWT.HORIZONTAL);
	horizontalForm.setLayout(new GridLayout());

	if (drawLeft) {
	    // left sash form
	    SashForm leftVerticalForm = new SashForm(horizontalForm,
		    SWT.VERTICAL);
	    leftVerticalForm.setOrientation(SWT.VERTICAL);
	    leftVerticalForm.setLayout(new GridLayout());

	    if (bWantStatusLine) {
		slm.createControl(this, SWT.NULL);
	    }
	    slm.setMessage("i2b2 Explorer Version 2.0");
	    slm.update(true);

	    // Create the tab folder
	    final TabFolder oTabFolder = new TabFolder(leftVerticalForm,
		    SWT.NONE);

	    // Create each tab and set its text, tool tip text,
	    // image, and control
	    TabItem oTreeTab = new TabItem(oTabFolder, SWT.NONE);
	    oTreeTab.setText("Concept trees");
	    oTreeTab
		    .setToolTipText("Hierarchically organized patient characteristics");
	    oTreeTab.setControl(getConceptTreeTabControl(oTabFolder));

	    // TabItem oFindTab = new TabItem(oTabFolder, SWT.NONE);
	    // oFindTab.setText("Find");
	    // oFindTab.setToolTipText("Free-form find tool for patient
	    // characteristics");
	    // FindTool find = new FindTool(slm);
	    // FindTool find = new FindTool(slm);

	    // oFindTab.setControl(find.getFindTabControl(oTabFolder));

	    // Select the first tab (index is zero-based)
	    oTabFolder.setSelection(0);

	    // Create the tab folder
	    final TabFolder queryRunFolder = new TabFolder(leftVerticalForm,
		    SWT.NONE);

	    TabItem previousRunTab = new TabItem(queryRunFolder, SWT.NONE);
	    previousRunTab.setText("Patient Sets and Previous Queries");
	    previousRunTab.setToolTipText("Patient Sets & Previous Queries");
	    final Composite runComposite = new Composite(queryRunFolder,
		    SWT.EMBEDDED);
	    previousRunTab.setControl(runComposite);

	    /*
	     * Create and setting up frame/ Frame runFrame =
	     * SWT_AWT.new_Frame(runComposite); Panel runPanel = new Panel(new
	     * BorderLayout()); try { UIManager.setLookAndFeel(UIManager
	     * .getSystemLookAndFeelClassName()); } catch (Exception e) {
	     * System.out.println("Error setting native LAF: " + e); }
	     * 
	     * runFrame.add(runPanel); JRootPane runRoot = new JRootPane();
	     * runPanel.add(runRoot); oAwtContainer_left =
	     * runRoot.getContentPane();
	     */
	    // runTreePanel = new QueryPreviousRunsPanel(null, this);
	    // oAwtContainer_left.add(runTreePanel);
	    // Select the first tab (index is zero-based)
	    queryRunFolder.setSelection(0);
	}

	// SashForm verticalForm = new SashForm(horizontalForm, SWT.VERTICAL);
	// verticalForm.setOrientation(SWT.VERTICAL);
	// verticalForm.setLayout(new GridLayout());

	// put a tab folder in it...
	tabFolder = new TabFolder(horizontalForm, SWT.NONE);

	addListener(SWT.Resize, new Listener() {
	    public void handleEvent(Event event) {
		int w = getBounds().width;
		patientSetText.setBounds(160, 5, w - 190, 27);
	    }
	});

	// Item 1: a Text Table
	TabItem item1 = new TabItem(tabFolder, SWT.NONE);
	item1.setText("Select Patients");
	Composite oModelComposite1 = new Composite(tabFolder, SWT.NONE);
	item1.setControl(oModelComposite1);
	GridLayout gridLayout1 = new GridLayout(1, false);
	gridLayout1.marginTop = 2;
	gridLayout1.marginLeft = 2;
	gridLayout1.marginBottom = 2;
	gridLayout1.verticalSpacing = 1;
	gridLayout1.horizontalSpacing = 1;
	oModelComposite1.setLayout(gridLayout1);
	// oModelComposite.setBackground(oTheParent.getDisplay().getSystemColor(
	// SWT.COLOR_WHITE));

	patientSetTable = new Table(oModelComposite1, SWT.V_SCROLL
		| SWT.H_SCROLL | SWT.BORDER);
	patientSetTable.setFocus();
	patientSetTable.setBackground(oTheParent.getDisplay().getSystemColor(
		SWT.COLOR_WHITE));
	GridData pSetTableGridData = new GridData(
		GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
	pSetTableGridData.grabExcessHorizontalSpace = true;
	pSetTableGridData.grabExcessVerticalSpace = false;
	pSetTableGridData.verticalIndent = 5;
	pSetTableGridData.heightHint = 80;
	patientSetTable.setLayoutData(pSetTableGridData);

	patientSetTable.setHeaderVisible(true);
	patientSetTable.setLinesVisible(true);

	TableColumn id = new TableColumn(patientSetTable, SWT.CENTER);
	id.setText("Set #");
	id.setWidth(40);
	TableColumn info = new TableColumn(patientSetTable, SWT.CENTER);
	info.setText("Patient Set Name");
	info.setWidth(700);

	// put a table in tabItem1...
	patientTable = new KTable(oModelComposite1, SWT.V_SCROLL | SWT.H_SCROLL
		| SWT.BORDER);
	patientTable.setFocus();
	patientTable.setBackground(oTheParent.getDisplay().getSystemColor(
		SWT.COLOR_WHITE));
	GridData tableGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
		| GridData.VERTICAL_ALIGN_FILL);
	tableGridData.grabExcessHorizontalSpace = true;
	tableGridData.grabExcessVerticalSpace = true;
	tableGridData.verticalIndent = 5;
	patientTable.setLayoutData(tableGridData);
	patientTable.setRowSelectionMode(true);
	patientTable.setMultiSelectionMode(true);
	patientTable.setModel(new PatientKTableModel());

	patientTable
		.addCellSelectionListener(new KTableCellSelectionListener() {
		    public void cellSelected(int col, int row, int statemask) {
			System.out.println("Cell [" + col + ";" + row
				+ "] selected.");
			patientTable.selectedRow = row;
			patientTable.selectedColumn = col;

			if (isAll) {
			    return;
			}

			// set patient ids from patient table
			int[] rows = patientTable.getRowSelection();
			selectedPatientids.clear();
			selectedPatients.clear();
			for (int i = 0; i < rows.length; i++) {
			    String pid = patientRowData.get(rows[i] - 1).patientID;
			    String rid = new Integer(patientRowData
				    .get(rows[i] - 1).rowNumber).toString();
			    selectedPatientids.add(pid);
			    selectedPatients.add(rid);
			}

			Collections.sort(selectedPatients,
				new Comparator<String>() {
				    public int compare(String d1, String d2) {
					return (Integer.parseInt(d1) < Integer
						.parseInt(d2)) ? -1 : 1;
				    }
				});

			String pidStr = selectedPatients.get(0);
			for (int j = 1; j < selectedPatients.size(); j++) {
			    pidStr += "," + selectedPatients.get(j);
			}
			patientSetText.setText(pidStr);
		    }

		    public void fixedCellSelected(int col, int row,
			    int statemask) {
			System.out.println("Header [" + col + ";" + row
				+ "] selected.");
		    }

		});

	Composite oModelAddDelButtonComposite1 = new Composite(
		oModelComposite1, SWT.NONE);
	GridLayout gL1 = new GridLayout(25, false);
	oModelAddDelButtonComposite1.setLayout(gL1);
	GridData oModelAddDelButtonGridData1 = new GridData(
		GridData.FILL_HORIZONTAL);
	oModelAddDelButtonGridData1.grabExcessHorizontalSpace = false;
	oModelAddDelButtonGridData1.horizontalSpan = 2;
	oModelAddDelButtonComposite1.setLayoutData(oModelAddDelButtonGridData1);

	GridData gdDel = new GridData(GridData.FILL_HORIZONTAL);

	Button deleteArrowButton1 = new Button(oModelAddDelButtonComposite1,
		SWT.PUSH);
	gdDel.horizontalSpan = 4;
	deleteArrowButton1.setLayoutData(gdDel);
	deleteArrowButton1.setText("Delete From List");
	deleteArrowButton1.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {
		curRowNumber = 0;
		PatientKTableModel m_Model = (PatientKTableModel) patientTable
			.getModel();
		int[] selectedRow = patientTable.getRowSelection();
		ArrayList<Integer> selectedRowArr = new ArrayList<Integer>();
		for (int i = 0; i < selectedRow.length; i++) {
		    selectedRowArr.add(new Integer(selectedRow[i]));
		}
		Collections.sort(selectedRowArr, new Comparator<Integer>() {
		    public int compare(Integer d1, Integer d2) {
			return (d1.intValue() < d2.intValue()) ? -1 : -1;
		    }
		});
		m_Model.fillDataFromTable(patientRowData);

		if ((selectedRow != null) && (selectedRow.length > 0)) {
		    for (int i = selectedRowArr.size() - 1; i >= 0; i--) {
			int index = selectedRowArr.get(i).intValue();
			int rowNumber = new Integer((String) (m_Model
				.getContentAt(0, index))).intValue();
			patientRowData.remove(rowNumber - 1);
		    }

		    // m_Model.deleteRow(selectedRow[0]);
		    ((PatientKTableModel) patientTable.getModel())
			    .deleteAllRows();
		    ((PatientKTableModel) patientTable.getModel())
			    .populateTable(patientRowData);

		    patientTable.redraw();
		    if (isAll) {
			patientSetText.setText("1 - " + patientRowData.size());
		    }

		    m_Model.fillDataFromTable(patientRowData);
		}
	    }
	});

	Button deleteAllButton1 = new Button(oModelAddDelButtonComposite1,
		SWT.PUSH);
	gdDel = new GridData(GridData.FILL_HORIZONTAL);
	gdDel.horizontalSpan = 4;
	deleteAllButton1.setLayoutData(gdDel);
	deleteAllButton1.setText("Delete All ");
	deleteAllButton1.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {
		PatientKTableModel m_Model = (PatientKTableModel) patientTable
			.getModel();
		m_Model.deleteAllRows();
		curRowNumber = 0;
		patientRowData.clear();
		patientTable.clearSelection();
		patientTable.redraw();

		patientSets.clear();
		patientSetTable.removeAll();
		patientSetText.setText("");
	    }
	});

	Button upArrowButton1 = new Button(oModelAddDelButtonComposite1,
		SWT.PUSH);
	upArrowButton1.setText("Move Up");
	upArrowButton1.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {

		PatientKTableModel m_Model = (PatientKTableModel) patientTable
			.getModel();
		int[] selectedRow = patientTable.getRowSelection();
		curRowNumber = 0;
		// KTableI2B2Model m_Model = (KTableI2B2Model)
		// patientTable.getModel();
		// int[] selectedRow = patientTable.getRowSelection();
		m_Model.fillDataFromTable(patientRowData);
		int index = new Integer((String) (m_Model.getContentAt(0,
			selectedRow[0]))).intValue() - 1;
		if (index < 1) {
		    return;
		}
		if ((selectedRow != null) && (selectedRow.length > 0)) {
		    // m_Model.moveRow(selectedRow[0], selectedRow[0] -1);
		    PatientTableRow row = patientRowData.get(index);
		    patientRowData.remove(index);
		    patientRowData.add(index - 1, row);
		    resetRowNumber();
		    m_Model.populateTable(patientRowData);
		}
		patientTable.setSelection(0, selectedRow[0] - 1, true);
		patientTable.redraw();

		m_Model.fillDataFromTable(patientRowData);
	    }
	});

	Button downArrowButton1 = new Button(oModelAddDelButtonComposite1,
		SWT.PUSH);
	downArrowButton1.setText("Move Down");
	downArrowButton1.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {

		PatientKTableModel m_Model = (PatientKTableModel) patientTable
			.getModel();
		int[] selectedRow = patientTable.getRowSelection();
		curRowNumber = 0;
		// KTableI2B2Model m_Model = (KTableI2B2Model)
		// patientTable.getModel();
		// int[] selectedRow = patientTable.getRowSelection();
		m_Model.fillDataFromTable(patientRowData);
		int index = new Integer((String) (m_Model.getContentAt(0,
			selectedRow[0]))).intValue() - 1;
		if (index == (patientRowData.size() - 1)) {
		    return;
		}
		if ((selectedRow != null) && (selectedRow.length > 0)) {
		    // m_Model.moveRow(selectedRow[0], selectedRow[0] -1);
		    PatientTableRow row = patientRowData.get(index);
		    patientRowData.remove(index);
		    patientRowData.add(index + 1, row);
		    resetRowNumber();
		    m_Model.populateTable(patientRowData);
		}
		patientTable.setSelection(0, selectedRow[0] + 1, true);
		patientTable.redraw();
	    }
	});

	Composite patientNumsComposite = new Composite(oModelComposite1,
		SWT.NONE);
	GridData patientNumData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	patientNumData.grabExcessHorizontalSpace = true;
	patientNumsComposite.setLayoutData(patientNumData);
	patientNumsComposite.setLayout(null);

	// Label patientset = new Label(patientNumsComposite, SWT.NONE);
	// patientset.setText("Patient Set: ");
	// patientset.setBounds(5, 9, 60, 22);

	queryCombo = new Combo(patientNumsComposite, SWT.READ_ONLY);
	queryCombo.add("Select All");
	queryCombo.add("Selected patients");
	queryCombo.add("Selected patient ids");
	queryCombo.setBounds(5, 5, 150, 35);
	queryCombo.select(0);
	queryCombo
		.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
		    public void widgetSelected(
			    org.eclipse.swt.events.SelectionEvent e) {
			int index = queryCombo.getSelectionIndex();
			// System.setProperty("QueryName: ",
			// queryCombo.getText());
			if (index == 1) {
			    isAll = false;
			    isSelectedPatients = true;
			    isSelectedPatientIds = false;
			    patientSetText.setEditable(false);
			    patientSetText.setText("");
			    // set patient ids from patient table
			    int[] rows = patientTable.getRowSelection();
			    // selectedPatientids.clear();
			    selectedPatients.clear();
			    for (int i = 0; i < rows.length; i++) {
				// String pid =
				// patientRowData.get(rows[i]-1).patientID;
				String rid = new Integer(patientRowData
					.get(rows[i] - 1).rowNumber).toString();
				// selectedPatientids.add(pid);
				selectedPatients.add(rid);
			    }

			    String pidStr = selectedPatients.get(0);
			    for (int j = 1; j < selectedPatients.size(); j++) {
				pidStr += "," + selectedPatients.get(j);
			    }
			    patientSetText.setText(pidStr);
			    // leftArrowButton.setEnabled(false);
			    // rightArrowButton.setEnabled(false);
			} else if (index == 0) {
			    isAll = true;
			    isSelectedPatients = false;
			    isSelectedPatientIds = false;

			    // queryNamemrnlistText.setText("");
			    if (patientRowData.size() > 0) {
				patientSetText.setText("1 - "
					+ patientRowData.size());
			    } else {
				patientSetText.setText("");
			    }
			    patientSetText.setEditable(false);
			    // patientSetSize = 0;
			    // leftArrowButton.setEnabled(false);
			    // rightArrowButton.setEnabled(true);
			    // patientMinNumText.setText("1");
			    // patientMaxNumText.setText("10");
			}
		    }

		    public void widgetDefaultSelected(
			    org.eclipse.swt.events.SelectionEvent e) {
		    }
		});

	patientSetText = new Text(patientNumsComposite, SWT.SINGLE | SWT.BORDER);
	patientSetText.setText("");
	patientSetText.setEditable(false);
	patientSetText.setBounds(165, 5, 240, 37);

	Transfer[] types = new Transfer[] { TextTransfer.getInstance() };

	DropTarget target = new DropTarget(patientTable, DND.DROP_COPY);
	target.setTransfer(types);
	target.addDropListener(new DropTargetAdapter() {
	    public void drop(DropTargetEvent event) {
		if (event.data == null) {
		    event.detail = DND.DROP_NONE;
		    return;
		}

		String dragStr = (String) event.data;
		String[] strs = dragStr.split(":");
		if (strs[0].equalsIgnoreCase("logicquery")) {
		    MessageBox mBox = new MessageBox(conceptTable.getShell(),
			    SWT.ICON_INFORMATION | SWT.OK);
		    mBox.setText("Please Note ...");
		    mBox
			    .setMessage("You can not drop this item here. It accepts a patient set only.");
		    mBox.open();
		    event.detail = DND.DROP_NONE;
		    return;
		}

		JAXBUtil jaxbUtil = ImageExplorerJAXBUtil.getJAXBUtil();

		try {
		    JAXBElement jaxbElement = jaxbUtil
			    .unMashallFromString(dragStr);
		    DndType dndType = (DndType) jaxbElement.getValue();
		    // JAXBElement element = (JAXBElement)
		    // dndType.getAny().get(0);
		    // BodyType bodyType = messageType.getMessageBody();
		    QueryResultInstanceType queryResultInstanceType = (QueryResultInstanceType) new JAXBUnWrapHelper()
			    .getObjectByClass(dndType.getAny(),
				    QueryResultInstanceType.class);
		    edu.harvard.i2b2.crcxmljaxb.datavo.dnd.PatientSet dragPatientSet = (edu.harvard.i2b2.crcxmljaxb.datavo.dnd.PatientSet) new JAXBUnWrapHelper()
			    .getObjectByClass(
				    dndType.getAny(),
				    edu.harvard.i2b2.crcxmljaxb.datavo.dnd.PatientSet.class);

		    if ((queryResultInstanceType == null || !queryResultInstanceType
			    .getQueryResultType().getName().equalsIgnoreCase(
				    "PATIENTSET"))
			    && dragPatientSet == null) {
			MessageBox mBox = new MessageBox(conceptTable
				.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mBox.setText("Please Note ...");
			mBox
				.setMessage("You can not drop this item here. It accepts a patient or patient set.");
			mBox.open();
			event.detail = DND.DROP_NONE;
			return;
		    }

		    String setId = null;
		    Integer setSize = null;
		    String patientId = null;
		    String pateintSetName = null;
		    PatientSetData patientsetdata = null;

		    if (queryResultInstanceType != null) {
			setId = queryResultInstanceType.getResultInstanceId();
			for (int i = 0; i < patientSets.size(); i++) {
			    if (setId.equalsIgnoreCase(patientSets.get(i)
				    .setId())) {
				MessageBox mBox = new MessageBox(conceptTable
					.getShell(), SWT.ICON_INFORMATION
					| SWT.OK);
				mBox.setText("Please Note ...");
				mBox
					.setMessage("The patient set is already in the table.");
				mBox.open();
				event.detail = DND.DROP_NONE;
				return;
			    }
			}
			setSize = new Integer(queryResultInstanceType
				.getSetSize());
			pateintSetName = queryResultInstanceType
				.getQueryInstanceId();

			patientsetdata = new PatientSetData();
			patientsetdata.setId(setId);
			patientsetdata.setName(pateintSetName);
			patientsetdata.setNumber("" + (patientSets.size() + 1));

			patientSets.add(patientsetdata);
		    } else {
			patientId = dragPatientSet.getPatient().get(0)
				.getPatientId();
			setId = dragPatientSet.getPatientSetId();
			pateintSetName = dragPatientSet.getPatientSetName();

			boolean knownSet = false;
			for (int i = 0; i < patientSets.size(); i++) {
			    if (setId.equalsIgnoreCase(patientSets.get(i)
				    .setId())) {
				knownSet = true;
				patientsetdata = patientSets.get(i);
				break;
			    }
			}

			if (!knownSet) {
			    patientsetdata = new PatientSetData();
			    patientsetdata.setId(setId);
			    patientsetdata.setName(pateintSetName);
			    patientsetdata.setNumber(""
				    + (patientSets.size() + 1));

			    patientSets.add(patientsetdata);
			}
		    }

		    try {
			PDORequestMessageFactory pdoFactory = new PDORequestMessageFactory();
			// requestXml =
			// pdoFactory.requestXmlMessage(
			// "zzp___050206101533684227.xml",
			// new Integer(0), new Integer(5), false);
			String requestXml = null;
			if (queryResultInstanceType != null) {
			    requestXml = pdoFactory.requestXmlMessage(setId,
				    new Integer(1), setSize, false);
			} else {
			    requestXml = pdoFactory.requestXmlMessage(setId,
				    patientId, false);
			}
			lastRequestMessage = requestXml;
			System.out.println(requestXml);

			String xmlResponse = null;
			if (System.getProperty("webServiceMethod").equals(
				"SOAP")) {
			    xmlResponse = PDOQueryClient
				    .sendPDOQueryRequestSOAP(requestXml);
			} else {
			    xmlResponse = PDOQueryClient
				    .sendPDOQueryRequestREST(requestXml);
			}
			if (xmlResponse.equalsIgnoreCase("CellDown")) {
			    // final JPanel parent = this;
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    null,
						    "Trouble with connection to the remote server, "
							    + "this is often a network error, please try again",
						    "Network Error",
						    JOptionPane.INFORMATION_MESSAGE);
				}
			    });
			    return;
			}
			lastResponseMessage = xmlResponse;

			// check response status here ......

			// System.out.println("Response: "+xmlResponse);
			PatientSet patientSet = new PDOResponseMessageFactory()
				.getPatientSetFromResponseXML(xmlResponse);
			List<PatientType> patients = patientSet.getPatient();
			System.out.println("Patient set size: "
				+ patients.size());

			PidSet pidSet = new PDOResponseMessageFactory()
				.getPidSetFromResponseXML(xmlResponse);
			List<PidType> pids = null;
			if (pidSet != null) {
			    pids = pidSet.getPid();
			    System.out.println("Pid set size: " + pids.size());
			}

			// patientRowData.clear();
			for (int i = 0; i < patients.size(); i++) {
			    PatientType patient = patients.get(i);

			    // check patient ids
			    boolean intable = false;
			    for (int j = 0; j < patientRowData.size(); j++) {
				PatientTableRow rowdata = patientRowData.get(j);
				if (patient.getPatientId().getValue()
					.equalsIgnoreCase(rowdata.patientID)) {
				    intable = true;
				    rowdata.addKnownSet(patientsetdata
					    .setNumber());
				    rowdata.patientSetNumber += "+"
					    + patientsetdata.setNumber();
				}
			    }

			    if (intable) {
				continue;
			    }

			    // populate the patient table
			    PatientTableRow row = new PatientTableRow();
			    row.rowNumber = i + 1;
			    row.patientSetNumber = patientsetdata.setNumber();
			    row.patientID = patient.getPatientId().getValue();
			    row.setParamData(patient.getParam());
			    if (pids != null) {
				for (int j = 0; j < pids.size(); j++) {
				    PidType pidType = pids.get(j);
				    if (pidType.getPatientId().getValue()
					    .equalsIgnoreCase(
						    patient.getPatientId()
							    .getValue())) {
					row.setMRNs(pidType.getPatientMapId());
					break;
				    }
				}
			    }
			    patientRowData.add(row);
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }

		    // set up the table
		    populatePatientTable(null);

		    System.out.println("Dropped set of: " + setSize
			    + " patients" + " with refId: " + setId);

		    // if(queryResultInstanceType != null) {
		    populatePatientSetTable();
		    // }

		    // leftArrowButton.setEnabled(true);
		    // rightArrowButton.setEnabled(true);

		    if (isAll) {
			patientSetText
				.setText(patientRowData.size() > 1 ? "1 - "
					+ patientRowData.size() : ""
					+ patientRowData.size());
		    }
		    event.detail = DND.DROP_NONE;
		} catch (Exception e) {
		    System.err.println(e.getMessage());
		    event.detail = DND.DROP_NONE;
		    e.printStackTrace();
		    return;
		}
	    }

	    public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	    }
	});

	patientTable.addCellResizeListener(new KTableCellResizeListener() {
	    public void columnResized(int col, int newWidth) {
		System.out.println("Column " + col + " resized to " + newWidth);
	    }

	    public void rowResized(int newHeight) {
		System.out.println("Rows resized to " + newHeight);
	    }

	});

	patientTable.addListener(SWT.Resize, new Listener() {
	    public void handleEvent(Event event) {
		int tableWidth = patientTable.getBounds().width;
		if (tableWidth > 800) {
		    patientTable.getModel().setColumnWidth(9, tableWidth - 655);
		}
	    }
	});
	// /
	// Item 2: Step 2
	TabItem item2 = new TabItem(tabFolder, SWT.NONE);
	item2.setText("Select Concepts");
	Composite oModelComposite = new Composite(tabFolder, SWT.NONE);
	item2.setControl(oModelComposite);
	GridLayout gridLayout = new GridLayout(2, false);
	gridLayout.marginTop = 2;
	gridLayout.marginLeft = 2;
	gridLayout.marginBottom = 2;
	gridLayout.verticalSpacing = 1;
	gridLayout.horizontalSpacing = 1;
	oModelComposite.setLayout(gridLayout);
	// put a table in tabItem1...
	conceptTable = new KTable(oModelComposite, SWT.V_SCROLL | SWT.H_SCROLL
		| SWT.BORDER);
	conceptTable.setFocus();
	conceptTable.setBackground(oTheParent.getDisplay().getSystemColor(
		SWT.COLOR_WHITE));
	GridData tableGridData2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL
		| GridData.VERTICAL_ALIGN_FILL);
	tableGridData2.grabExcessHorizontalSpace = true;
	tableGridData2.grabExcessVerticalSpace = true;
	tableGridData2.verticalIndent = 5;
	conceptTable.setLayoutData(tableGridData2);
	conceptTable.setRowSelectionMode(true);
	// conceptTable.setMultiSelectionMode(true);
	// conceptTable.setModel(new KTableForModel());
	conceptTable.setModel(new ConceptKTableModel());
	// conceptTable.getModel().setColumnWidth(0,
	// oModelComposite.getBounds().width - 35);
	conceptTable
		.addCellSelectionListener(new KTableCellSelectionListener() {
		    public void cellSelected(int col, int row, int statemask) {
			System.out.println("Cell [" + col + ";" + row
				+ "] selected.");
			conceptTable.selectedRow = row;
			conceptTable.selectedColumn = col;
		    }

		    public void fixedCellSelected(int col, int row,
			    int statemask) {
			System.out.println("Header [" + col + ";" + row
				+ "] selected.");
		    }

		});

	conceptTable.addMouseTrackListener(new MouseTrackListener() {

	    public void mouseEnter(MouseEvent arg0) {

	    }

	    public void mouseExit(MouseEvent arg0) {

	    }

	    public void mouseHover(MouseEvent arg0) {
		MouseEvent evt = arg0;

		Rectangle rect = conceptTable.getCellRect(3, getRowNumber());
		Rectangle rect1 = conceptTable.getCellRect(2, 1);
		Rectangle rect2 = conceptTable.getCellRect(3, 1);

		//System.out.println("rect X and width: "+rect.x+","+rect.width)
		// ;
		// System.out.println("mouse X and Y: "+evt.x+","+evt.y);
		if (evt.y < rect.y && evt.x > rect1.x
			&& evt.x < rect1.x + rect1.width) {
		    conceptTable
			    .setToolTipText("Double click the cell to set date constraint.");
		} else if (evt.y < rect.y && evt.x > rect2.x
			&& evt.x < rect2.x + rect2.width) {
		    conceptTable
			    .setToolTipText("Double click the cell to set value constraint.");
		} else {
		    conceptTable.setToolTipText("");
		}
	    }

	});

	Composite oModelAddDelButtonComposite = new Composite(oModelComposite,
		SWT.NONE);
	GridLayout gL = new GridLayout(25, false);
	oModelAddDelButtonComposite.setLayout(gL);
	GridData oModelAddDelButtonGridData = new GridData(
		GridData.FILL_HORIZONTAL);// HORIZONTAL_ALIGN_FILL);// |
	// GridData.VERTICAL_ALIGN_FILL);
	oModelAddDelButtonGridData.grabExcessHorizontalSpace = false;
	oModelAddDelButtonGridData.horizontalSpan = 2;
	oModelAddDelButtonComposite.setLayoutData(oModelAddDelButtonGridData);

	Button deleteArrowButton = new Button(oModelAddDelButtonComposite,
		SWT.PUSH);
	gdDel.horizontalSpan = 4;
	deleteArrowButton.setLayoutData(gdDel);
	deleteArrowButton.setText("Delete From List");
	deleteArrowButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {
		curRowNumber = 0;
		ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
			.getModel();
		int[] selectedRow = conceptTable.getRowSelection();
		m_Model.fillDataFromTable(rowData);

		if ((selectedRow != null) && (selectedRow.length > 0)) {
		    String conceptName = (String) m_Model.getContentAt(1,
			    selectedRow[0]);
		    if (conceptName.equals("Encounter Range Line")) {
		    } else if (conceptName.equals("Vital Status Line")) {
		    }

		    int rowNumber = new Integer((String) (m_Model.getContentAt(
			    0, selectedRow[0]))).intValue();
		    int rid = selectedRow[0];
		    ArrayList list = (ArrayList) rowData.get(rowNumber - 1);
		    for (int i = 0; i < list.size(); i++) {
			ConceptTableRow tr = (ConceptTableRow) list.get(i);
			if (tr.rowId == rid) {
			    list.remove(i);
			    break;
			}
		    }
		    if (list.size() == 0) {
			rowData.remove(rowNumber - 1);
		    }
		    curRowNumber = rowData.size();
		    resetRowNumber();
		    // m_Model.deleteRow(selectedRow[0]);
		    ((ConceptKTableModel) conceptTable.getModel())
			    .deleteAllRows();
		    ((ConceptKTableModel) conceptTable.getModel())
			    .populateTable(rowData);

		    conceptTable.redraw();
		}
	    }
	});

	Button deleteAllButton = new Button(oModelAddDelButtonComposite,
		SWT.PUSH);
	gdDel = new GridData(GridData.FILL_HORIZONTAL);
	gdDel.horizontalSpan = 4;
	deleteAllButton.setLayoutData(gdDel);
	deleteAllButton.setText("Delete All ");
	deleteAllButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {
		ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
			.getModel();
		m_Model.deleteAllRows();
		curRowNumber = 0;
		rowData.clear();
		conceptTable.redraw();
	    }
	});

	Button putInOrderButton = new Button(oModelAddDelButtonComposite,
		SWT.PUSH);
	gdDel = new GridData(GridData.FILL_HORIZONTAL);
	gdDel.horizontalSpan = 4;
	putInOrderButton.setLayoutData(gdDel);
	putInOrderButton.setText("Put In Order ");
	putInOrderButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {

		ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
			.getModel();
		curRowNumber = 0;
		m_Model.fillDataFromTable(rowData);

		Collections.sort(rowData, new Comparator<Object>() {
		    public int compare(Object o1, Object o2) {
			int i1 = ((ConceptTableRow) ((ArrayList) o1).get(0)).rowNumber;
			int i2 = ((ConceptTableRow) ((ArrayList) o2).get(0)).rowNumber;
			if (i1 > i2) {
			    return 1;
			} else if (i1 < i2) {
			    return -1;
			} else {
			    return 0;
			}
		    }
		});
		m_Model.deleteAllRows();
		m_Model.populateTable(rowData);
		conceptTable.redraw();
	    }
	});

	Button upArrowButton = new Button(oModelAddDelButtonComposite, SWT.PUSH);
	upArrowButton.setText("Move Up");
	upArrowButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {

		ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
			.getModel();
		int[] selectedRow = conceptTable.getRowSelection();
		curRowNumber = 0;
		// KTableI2B2Model m_Model = (KTableI2B2Model)
		// conceptTable.getModel();
		// int[] selectedRow = conceptTable.getRowSelection();
		m_Model.fillDataFromTable(rowData);
		int index = new Integer((String) (m_Model.getContentAt(0,
			selectedRow[0]))).intValue() - 1;
		if (index < 1) {
		    return;
		}
		if ((selectedRow != null) && (selectedRow.length > 0)) {
		    // m_Model.moveRow(selectedRow[0], selectedRow[0] -1);
		    ArrayList<ConceptTableRow> list = rowData.get(index);
		    rowData.remove(index);
		    rowData.add(index - 1, list);
		    resetRowNumber();
		    m_Model.populateTable(rowData);
		}
		conceptTable.setSelection(0, selectedRow[0] - 1, true);
		conceptTable.redraw();
	    }
	});

	Button downArrowButton = new Button(oModelAddDelButtonComposite,
		SWT.PUSH);
	downArrowButton.setText("Move Down");
	downArrowButton.addSelectionListener(new SelectionAdapter() {
	    public void widgetSelected(SelectionEvent event) {

		ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
			.getModel();
		int[] selectedRow = conceptTable.getRowSelection();
		curRowNumber = 0;
		// KTableI2B2Model m_Model = (KTableI2B2Model)
		// conceptTable.getModel();
		// int[] selectedRow = conceptTable.getRowSelection();
		m_Model.fillDataFromTable(rowData);
		int index = new Integer((String) (m_Model.getContentAt(0,
			selectedRow[0]))).intValue() - 1;
		if (index == (rowData.size() - 1)) {
		    return;
		}
		if ((selectedRow != null) && (selectedRow.length > 0)) {
		    // m_Model.moveRow(selectedRow[0], selectedRow[0] -1);
		    ArrayList<ConceptTableRow> list = rowData.get(index);
		    rowData.remove(index);
		    rowData.add(index + 1, list);
		    resetRowNumber();
		    m_Model.populateTable(rowData);
		}
		conceptTable.setSelection(0, selectedRow[0] + 1, true);
		conceptTable.redraw();
	    }
	});

	if (UserInfoBean.getInstance().getCellDataUrl("identity") != null) {
	    Composite oPatientSetComposite = new Composite(oModelComposite,
		    SWT.NONE);
	    GridData patientSetData = new GridData(
		    GridData.HORIZONTAL_ALIGN_FILL);
	    patientSetData.grabExcessHorizontalSpace = true;
	    oPatientSetComposite.setLayoutData(patientSetData);
	    oPatientSetComposite.setLayout(null);

	    Label mrnlabel = new Label(oPatientSetComposite, SWT.NONE);
	    mrnlabel.setText("MRN site:");
	    mrnlabel.setBounds(5, 9, 50, 20);

	    final Combo siteCombo = new Combo(oPatientSetComposite, SWT.NULL);
	    siteCombo.add("BWH");
	    siteCombo.add("MGH");
	    siteCombo.setBounds(57, 5, 60, 20);
	    siteCombo.select(1);

	    Label mrnNumber = new Label(oPatientSetComposite, SWT.NONE);
	    mrnNumber.setText("number:");
	    mrnNumber.setBounds(121, 9, 40, 20);

	    mrnlistText = new Text(oPatientSetComposite, SWT.SINGLE
		    | SWT.BORDER);
	    mrnlistText.setBounds(164, 5, 150, 20);
	    mrnlistText.setText("");

	    Button runButton = new Button(oPatientSetComposite, SWT.PUSH);
	    runButton.setText("Search By MRN");
	    runButton.setBounds(315, 5, 85, 23);
	    runButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
		    String mrns = mrnlistText.getText();
		    if (mrns.equals("")) {
			return;
		    }

		}
	    });
	}

	DropTarget target2 = new DropTarget(conceptTable, DND.DROP_COPY);
	target2.setTransfer(types);
	target2.addDropListener(new DropTargetAdapter() {
	    public void drop(DropTargetEvent event) {
		if (event.data == null) {
		    event.detail = DND.DROP_NONE;
		    return;
		}

		try {
		    SAXBuilder parser = new SAXBuilder();
		    String xmlContent = (String) event.data;
		    java.io.StringReader xmlStringReader = new java.io.StringReader(
			    xmlContent);
		    org.jdom.Document tableDoc = parser.build(xmlStringReader);
		    org.jdom.Element tableXml = tableDoc
			    .getRootElement()
			    .getChild(
				    "concepts",
				    Namespace
					    .getNamespace("http://www.i2b2.org/xsd/cell/ont/1.1/"));

		    boolean isQuery = false;
		    if (tableXml == null) {
			tableXml = tableDoc
				.getRootElement()
				.getChild(
					"query_master",
					Namespace
						.getNamespace("http://www.i2b2.org/xsd/cell/crc/psm/1.1/"));

			if (tableXml != null) {
			    isQuery = true;
			} else {
			    ArrayList<QueryConceptData> nodeXmls = new ArrayList<QueryConceptData>();
			    org.jdom.Element panelXml = tableDoc
				    .getRootElement()
				    .getChild(
					    "panel",
					    Namespace
						    .getNamespace("http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"));

			    if (panelXml == null) {
				MessageBox mBox = new MessageBox(conceptTable
					.getShell(), SWT.ICON_INFORMATION
					| SWT.OK);
				mBox.setText("Please Note ...");
				mBox
					.setMessage("You can not drop this item here.");
				mBox.open();
				event.detail = DND.DROP_NONE;
				return;
			    } else {
				String domString = (new XMLOutputter())
					.outputString(panelXml);
				JAXBContext jc1 = JAXBContext
					.newInstance(edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory.class);
				Unmarshaller unMarshaller = jc1
					.createUnmarshaller();
				JAXBElement panelJaxbElement = (JAXBElement) unMarshaller
					.unmarshal(new StringReader(domString));

				PanelType panelType = (PanelType) panelJaxbElement
					.getValue();
				for (int j = 0; j < panelType.getItem().size(); j++) {
				    ItemType itemType = panelType.getItem()
					    .get(j);
				    QueryConceptData nodedata = new QueryConceptData();

				    nodedata.name(itemType.getItemName());
				    // nodedata.visualAttribute("FA");
				    nodedata.visualAttribute(itemType
					    .getItemIcon());
				    nodedata.tooltip(itemType.getTooltip());
				    nodedata.fullname(itemType.getItemKey());
				    nodedata.hlevel(new Integer(itemType
					    .getHlevel()).toString());
				    nodedata.constrainByValue(itemType
					    .getConstrainByValue());
				    // Handle ConstrainByDates
				    for (int u = 0; u < itemType
					    .getConstrainByDate().size(); u++) {
					nodedata.writeTimeConstrain(itemType
						.getConstrainByDate().get(u)
						.getDateFrom(), itemType
						.getConstrainByDate().get(u)
						.getDateTo());
				    }

				    // Process Constrain by Value

				    if (itemType.getConstrainByValue().size() > 0) {
					nodedata.setValueConstrains(itemType
						.getConstrainByValue());

					if (itemType.getConstrainByValue()
						.size() > 0) {
					    nodedata
						    .setValueConstrains(itemType
							    .getConstrainByValue());
					    if (nodedata.valuePropertyData()
						    .hasEnumValue()) {
						if (nodedata
							.valuePropertyData()
							.useTextValue()) {
						    ArrayList<String> results = new ArrayList<String>();
						    results
							    .toArray(nodedata
								    .valuePropertyData()
								    .value()
								    .split(","));
						    nodedata
							    .valuePropertyData().selectedValues = results;
						}

					    }
					}

				    }

				    // End Process Constrain by value

				    String status = nodedata.setXmlContent();

				    if (status.equalsIgnoreCase("error")) {
					MessageBox mBox = new MessageBox(
						conceptTable.getShell(),
						SWT.ICON_INFORMATION | SWT.OK);
					mBox.setText("Please Note ...");
					mBox
						.setMessage("Response delivered from the remote server could not be understood,\n"
							+ "you may wish to retry your last action.");
					mBox.open();
					event.detail = DND.DROP_NONE;

					return;
				    }
				    //nodedata.valueModel(nodedata.xmlContent())
				    // ;
				    nodeXmls.add(nodedata); // .xmlContent());
				}
				event.detail = DND.DROP_NONE;
			    }

			    populateTable(nodeXmls);
			    return;
			}
		    }

		    if (tableXml == null) {
			MessageBox mBox = new MessageBox(conceptTable
				.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mBox.setText("Please Note ...");
			mBox.setMessage("You can not drop this item here.");
			mBox.open();
			event.detail = DND.DROP_NONE;
			return;
		    }

		    if (isQuery) {
			ArrayList<QueryConceptData> nodeXmls = new ArrayList<QueryConceptData>();
			try {
			    JAXBUtil jaxbUtil = ImageExplorerJAXBUtil
				    .getJAXBUtil();
			    QueryMasterData ndata = new QueryMasterData();
			    ndata.name(tableXml.getChildText("name"));
			    // queryNamemrnlistText.setText(ndata.name());
			    ndata.xmlContent(null);
			    ndata.id(tableXml
				    .getChildTextTrim("query_master_id"));
			    ndata.userId(tableXml.getChildTextTrim("user_id"));
			    // queryCombo.select(0);

			    String xmlcontent = null;
			    String xmlrequest = null;

			    xmlrequest = ndata.writeDefinitionQueryXML();
			    lastRequestMessage(xmlrequest);

			    if (System.getProperty("webServiceMethod").equals(
				    "SOAP")) {
				xmlcontent = PDOQueryClient
					.sendPDQQueryRequestSOAP(xmlrequest);
			    } else {
				xmlcontent = PDOQueryClient
					.sendPDQQueryRequestREST(xmlrequest);
			    }

			    lastResponseMessage(xmlcontent);

			    if (xmlcontent == null) {

				return;
			    } else {
				System.out.println("Query content response: "
					+ xmlcontent);
				ndata.xmlContent(xmlcontent);
			    }

			    JAXBElement jaxbElement = jaxbUtil
				    .unMashallFromString(ndata.xmlContent());
			    ResponseMessageType messageType = (ResponseMessageType) jaxbElement
				    .getValue();

			    BodyType bt = messageType.getMessageBody();
			    MasterResponseType masterResponseType = (MasterResponseType) new JAXBUnWrapHelper()
				    .getObjectByClass(bt.getAny(),
					    MasterResponseType.class);
			    RequestXmlType requestXmlType = masterResponseType
				    .getQueryMaster().get(0).getRequestXml();

			    org.w3c.dom.Element element = (org.w3c.dom.Element) requestXmlType
				    .getContent().get(0);
			    String domString = edu.harvard.i2b2.common.util.xml.XMLUtil
				    .convertDOMElementToString(element);

			    JAXBContext jc1 = JAXBContext
				    .newInstance(edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.ObjectFactory.class);
			    Unmarshaller unMarshaller = jc1
				    .createUnmarshaller();
			    JAXBElement queryDefinitionJaxbElement = (JAXBElement) unMarshaller
				    .unmarshal(new StringReader(domString));

			    // String strRequest = (String)
			    // requestXmlType.getContent().get(0);

			    // jaxbElement =
			    // jaxbUtil.unMashallFromString(strRequest);
			    // RequestMessageType requestMessageType =
			    // (RequestMessageType)jaxbElement.getValue();
			    // bt = requestMessageType.getMessageBody();
			    // QueryDefinitionRequestType
			    // queryDefinitionRequestType =
			    // (QueryDefinitionRequestType) new
			    // JAXBUnWrapHelper().getObjectByClass(bt.getAny(),
			    // QueryDefinitionRequestType.class);
			    // QueryDefinitionType queryDefinitionType =
			    // queryDefinitionRequestType.getQueryDefinition();

			    QueryDefinitionType queryDefinitionType = (QueryDefinitionType) queryDefinitionJaxbElement
				    .getValue();

			    int numOfPanels = queryDefinitionType.getPanel()
				    .size();
			    for (int i = 0; i < numOfPanels; i++) {
				PanelType panelType = queryDefinitionType
					.getPanel().get(i);

				for (int j = 0; j < panelType.getItem().size(); j++) {
				    ItemType itemType = panelType.getItem()
					    .get(j);
				    QueryConceptData nodedata = new QueryConceptData();

				    nodedata.name(itemType.getItemName());
				    nodedata.visualAttribute("FA");
				    nodedata.tooltip(itemType.getTooltip());
				    nodedata.fullname(itemType.getItemKey());
				    //nodedata.lookuptable(itemType.getItemTable
				    // ());
				    nodedata.hlevel(new Integer(itemType
					    .getHlevel()).toString());
				    // nodedata.lookupdb("metadata");
				    //nodedata.selectservice(System.getProperty(
				    // "selectservice"));
				    // get the xml content from select service
				    // then set it as node data
				    String status = nodedata.setXmlContent();
				    if (status.equalsIgnoreCase("error")) {
					MessageBox mBox = new MessageBox(
						conceptTable.getShell(),
						SWT.ICON_INFORMATION | SWT.OK);
					mBox.setText("Please Note ...");
					mBox
						.setMessage("Response delivered from the remote server could not be understood,\n"
							+ "you may wish to retry your last action.");
					mBox.open();
					event.detail = DND.DROP_NONE;

					return;
				    }
				    nodeXmls.add(nodedata);// .xmlContent());
				}
			    }
			    populateTable(nodeXmls);

			    // get query instance
			    String xmlRequest = ndata.writeContentQueryXML();
			    lastRequestMessage = xmlRequest;
			    // System.out.println(xmlRequest);

			    // String xmlResponse;
			    // if(System.getProperty("webServiceMethod").equals(
			    // "SOAP"))
			    // {
			    // xmlResponse =
			    //PDOQueryClient.sendPDQQueryRequestSOAP(xmlrequest)
			    // ;
			    // }
			    // else {
			    // xmlResponse =
			    //PDOQueryClient.sendPDQQueryRequestREST(xmlrequest)
			    // ;
			    // }
			    String xmlResponse = PDOQueryClient
				    .sendPDQQueryRequestREST(xmlRequest);
			    lastResponseMessage = xmlResponse;

			    jaxbElement = jaxbUtil
				    .unMashallFromString(xmlResponse);
			    messageType = (ResponseMessageType) jaxbElement
				    .getValue();
			    bt = messageType.getMessageBody();
			    InstanceResponseType instanceResponseType = (InstanceResponseType) new JAXBUnWrapHelper()
				    .getObjectByClass(bt.getAny(),
					    InstanceResponseType.class);

			    QueryInstanceData instanceData = null;
			    XMLGregorianCalendar startDate = null;
			    for (QueryInstanceType queryInstanceType : instanceResponseType
				    .getQueryInstance()) {
				QueryInstanceData runData = new QueryInstanceData();

				runData.visualAttribute("FA");
				runData.tooltip("The results of the query run");
				runData.id(new Integer(queryInstanceType
					.getQueryInstanceId()).toString());
				XMLGregorianCalendar cldr = queryInstanceType
					.getStartDate();
				runData.name("Results of " + "["
					+ cldr.getMonth() + "-" + cldr.getDay()
					+ "-" + cldr.getYear() + " "
					+ cldr.getHour() + ":"
					+ cldr.getMinute() + ":"
					+ cldr.getSecond() + "]");

				if (instanceData == null) {
				    startDate = cldr;
				    instanceData = runData;
				} else {
				    if (cldr.toGregorianCalendar().compareTo(
					    startDate.toGregorianCalendar()) > 0) {
					startDate = cldr;
					instanceData = runData;
				    }
				}
			    }
			    // get patient set
			    if (instanceData == null) {
				event.detail = DND.DROP_NONE;
				return;
			    }
			    System.out.println("Got query instance: "
				    + instanceData.name());

			    xmlRequest = instanceData.writeContentQueryXML();
			    lastRequestMessage = xmlRequest;

			    xmlResponse = PDOQueryClient
				    .sendPDQQueryRequestREST(xmlRequest);
			    lastResponseMessage = xmlResponse;

			    jaxbElement = jaxbUtil
				    .unMashallFromString(xmlResponse);
			    messageType = (ResponseMessageType) jaxbElement
				    .getValue();
			    bt = messageType.getMessageBody();
			    ResultResponseType resultResponseType = (ResultResponseType) new JAXBUnWrapHelper()
				    .getObjectByClass(bt.getAny(),
					    ResultResponseType.class);

			    for (QueryResultInstanceType queryResultInstanceType : resultResponseType
				    .getQueryResultInstance()) {
				if (!(queryResultInstanceType
					.getQueryResultType().getName()
					.equalsIgnoreCase("PATIENTSET"))) {
				    continue;
				}

				String status = queryResultInstanceType
					.getQueryStatusType().getName();
				// resultData.patientRefId(new
				// Integer(queryResultInstanceType.
				// getResultInstanceId
				// ()).toString());//data.patientRefId());
				// resultData.patientCount(new
				//Integer(queryResultInstanceType.getSetSize()).
				// toString());//data.patientCount());
				if (status.equalsIgnoreCase("FINISHED")) {
				    // resultData.name("Patient Set -
				    // "+resultData.patientCount()+" Patients");
				    String setId = new Integer(
					    queryResultInstanceType
						    .getResultInstanceId())
					    .toString();
				    String setSize = new Integer(
					    queryResultInstanceType
						    .getSetSize()).toString();
				    // patientSetText.setText("Patient Set:
				    // "+setSize+" patients");//strs[0]);
				    patientRefId = new String(setId);//strs[1]);
				    // patientMinNumText.setText("1");
				    // leftArrowButton.setEnabled(false);

				    int maxPatientNum = 10;// new
				    // Integer(patientMaxNumText.getText()).
				    // intValue();
				    patientSetSize = queryResultInstanceType
					    .getSetSize();
				    if (patientSetSize > maxPatientNum) {
					// rightArrowButton.setEnabled(true);
					// patientMaxNumText.setText("10");
				    } else {
					// rightArrowButton.setEnabled(false);
					if (patientSetSize > 0) {
					    //patientMaxNumText.setText(setSize)
					    // ;
					}
				    }

				    System.out.println("Dropped set of: "
					    + setSize + " patients"/*
								    * strs[ 0]
								    */
					    + " with refId: " + setId/*
								      * strs[ 1
								      * ]
								      */);
				} else {
				    // message
				}
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			    return;
			}
		    } else {
			List conceptChildren = tableXml.getChildren();
			parseDropConcepts(conceptChildren, event);
			// System.setProperty("XMLfrommodel",(String)
			// event.data);
			conceptTable.redraw();
		    }

		    event.detail = DND.DROP_NONE;
		} catch (JDOMException e) {
		    System.err.println(e.getMessage());
		    MessageBox mBox = new MessageBox(conceptTable.getShell(),
			    SWT.ICON_INFORMATION | SWT.OK);
		    mBox.setText("Please Note ...");
		    mBox.setMessage("You can not drop this item here.");
		    mBox.open();
		    event.detail = DND.DROP_NONE;
		    e.printStackTrace();
		    return;
		} catch (Exception e) {
		    System.err.println(e.getMessage());
		    event.detail = DND.DROP_NONE;
		    e.printStackTrace();
		    return;
		}
	    }

	    public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	    }
	});

	conceptTable.addCellResizeListener(new KTableCellResizeListener() {
	    public void columnResized(int col, int newWidth) {
		System.out.println("Column " + col + " resized to " + newWidth);
	    }

	    public void rowResized(int newHeight) {
		System.out.println("Rows resized to " + newHeight);
	    }

	});

	conceptTable.addListener(SWT.Resize, new Listener() {
	    public void handleEvent(Event event) {
		int tableWidth = conceptTable.getBounds().width;
		conceptTable.getModel().setColumnWidth(1, tableWidth - 425);
	    }
	});

	// Item 3: Step 3 (render tables)
	TabItem item4 = new TabItem(tabFolder, SWT.NONE);
	item4.setText("Render Tables");

	tableviewComposite = new TableViewComposite(tabFolder, SWT.NONE);
	tableviewComposite.imageExplorerC(this);
	item4.setControl(tableviewComposite);

	tabFolder.addSelectionListener(new SelectionListener() {
	    public void widgetSelected(SelectionEvent e) {
		if (tabFolder.getSelectionIndex() == 0) {
		    if (currentTabIndex == 2) {
			MessageDialog mBox = new MessageDialog(
				getShell(),
				"Please Note ...",
				null,
				"Changing this option will lose your work if it's not saved. Do you want to change?",
				MessageDialog.QUESTION, new String[] { "Yes",
					"No" }, 0);

			int answer = mBox.open();

			if (answer != 0) {
			    oTheParent.getDisplay().syncExec(new Runnable() {
				public void run() {
				    tabFolder.setSelection(2);
				}
			    });
			    return;
			}
		    }

		    DestroyMiniVisualization(oAwtContainer);
		    populatePatientTable(null);
		    currentTabIndex = 0;

		} else if (tabFolder.getSelectionIndex() == 2) {
		    DestroyMiniVisualization(oAwtContainer);

		    if (rowData.size() == 0) {
			oTheParent.getDisplay().syncExec(new Runnable() {
			    public void run() {
				tabFolder.setSelection(0);
			    }
			});
			MessageBox mBox = new MessageBox(conceptTable
				.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mBox.setText("Please Note ...");
			mBox.setMessage("The concept table is empty.");
			mBox.open();
			return;
		    }
		    
		    if (patientRowData.size() == 0) {
			oTheParent.getDisplay().syncExec(new Runnable() {
			    public void run() {
				tabFolder.setSelection(0);
			    }
			});
			MessageBox mBox = new MessageBox(conceptTable
				.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mBox.setText("Please Note ...");
			mBox.setMessage("The patient table is empty.");
			mBox.open();
			return;
		    }

		    // reset patient data from table
		    PatientKTableModel pkmodel = (PatientKTableModel) patientTable
			    .getModel();
		    pkmodel.fillDataFromTable(patientRowData);

		    int startindex = 0;
		    int endindex = patientRowData.size();

		    tableviewComposite.setComboText(2);

		    // handle selected patients and patient ids
		    if (isAll) {
			// result = getPDOResult(startindex, endindex);

			tableviewComposite.renderTables(startindex, endindex,
				"UnD");
		    } else if (isSelectedPatients) {
			// imageComposite.currentIndextext.setText(
			// selectedPatients.get(0));
			// currentSelectedIndex = 0;
			// startindex =
			// Integer.parseInt(selectedPatients.get(
			// currentSelectedIndex))-1;

			// result =
			// getPDOResultFromPatientIndex(selectedPatients);

			tableviewComposite
				.renderTables(selectedPatients, "UnD");
		    }

		    currentTabIndex = 2;
		} else {
		    if (currentTabIndex == 2) {
			MessageDialog mBox = new MessageDialog(
				getShell(),
				"Please Note ...",
				null,
				"Changing this option will lose your work if it's not saved. Do you want to change?",
				MessageDialog.QUESTION, new String[] { "Yes",
					"No" }, 0);

			int answer = mBox.open();

			if (answer != 0) {
			    oTheParent.getDisplay().syncExec(new Runnable() {
				public void run() {
				    tabFolder.setSelection(2);
				}
			    });
			    return;
			}
		    }

		    DestroyMiniVisualization(oAwtContainer);
		    currentTabIndex = 1;
		}
	    }

	    public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	    }
	});

	if (drawLeft) {
	    horizontalForm.setWeights(new int[] { 30, 70 });
	}

	return parent;
    }

    public void redrawImage(int index) {
	// DestroyMiniVisualization(oAwtContainer);

	// handle selected patients
	// PatientTableRow row = patientRowData.get(index);
	// imageComposite.setupDecision(row);
	// String result = getPDOResult(index);
	// getObservationset(result);
	// imageComposite.setupListTable();

	// renderImage(imagePanel, observationList.get(index), false, 0);
    }

    public void redrawImage(ObservationType obs) {
	// DestroyMiniVisualization(oAwtContainer);

	// handle selected patients
	// PatientTableRow row = patientRowData.get(index);
	// imageComposite.setupDecision(row);
	// String result = getPDOResult(index);
	// getObservationset(result);
	// imageComposite.setupListTable();

	// renderImage(imagePanel, obs, false, 0);
    }

    public void redrawImageView(int index) {
	DestroyMiniVisualization(oAwtContainer);
	// imagePanel = new ImagePanel(explorer);

	// handle selected patient ids, id --> index
	PatientTableRow row = patientRowData.get(index);
	// imageComposite.setupDecision(row);
	String result = getPDOResult(index, index + 1, "All");
	getObservationList(result);
	// imageComposite.setupListTable(observationList);
	// imageComposite.setupEventTree(observationList, eventSet);
	// renderImage(imagePanel, observationList.get(0), true, 0);
    }

    // add for selected patients
    public String getPDOResultFromPatientIndex(ArrayList<String> indexes,
	    String decision) {
	try {
	    ConceptKTableModel i2b2Model = (ConceptKTableModel) conceptTable
		    .getModel();
	    // String xmlContent = i2b2Model.getContentXml();
	    i2b2Model.fillDataFromTable(rowData);

	    // handle selected patients and patient ids
	    ArrayList<String> patientids = new ArrayList<String>();
	    for (int i = 0; i < indexes.size(); i++) {
		PatientTableRow row = patientRowData.get(i);
		if (decision.equalsIgnoreCase("All")
			|| row.decision.equalsIgnoreCase(decision)) {
		    String id = row.patientID;
		    patientids.add(id);
		}
	    }

	    ArrayList<TimelineRow> tlrows = i2b2Model.getTimelineRows(rowData);
	    String result = PDOQueryClient.getPDOResponseString(tlrows,
		    patientids, explorer);

	    if (result != null) {
		if (result.equalsIgnoreCase("memory error")) {
		    java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
			    JOptionPane
				    .showMessageDialog(
					    oAwtContainer,
					    "Running out of memory while loading "
						    +
						    /* ids.size() + */" one patients."
						    + "\nPlease try it again with a smaller number of patients.");
			}
		    });

		    bNoError = false;
		} else if (result.equalsIgnoreCase("error")) {
		    java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
			    JOptionPane
				    .showMessageDialog(
					    oAwtContainer,
					    "Response delivered from the remote server could not be "
						    + "understood, you may wish to retry your last action");
			}
		    });

		    bNoError = false;
		}

		return result;
	    } else {
		// JOptionPane.showMessageDialog(oAwtContainer, "Response
		// delivered from the remote server could not be understood, you
		// may wish to retry your last action");
		bNoError = false;
		return null;
	    }
	} catch (Exception e) {
	    log.error(e.getMessage());
	    e.printStackTrace();
	    bNoError = false;
	    return null;
	}
    }

    // add for selected patient ids
    @SuppressWarnings("unused")
    public String getPDOResultFromPatientids(ArrayList<String> ids,
	    String decision) {
	return null;
    }

    public String getPDOResult(int startIndex, int endIndex, String decision) {
	try {
	    ConceptKTableModel i2b2Model = (ConceptKTableModel) conceptTable
		    .getModel();
	    // String xmlContent = i2b2Model.getContentXml();
	    i2b2Model.fillDataFromTable(rowData);

	    // handle selected patients and patient ids
	    ArrayList<String> patientids = new ArrayList<String>();
	    for (int i = startIndex; i < endIndex; i++) {
		PatientTableRow row = patientRowData.get(i);
		if (decision.equalsIgnoreCase("All")
			|| row.decision.equalsIgnoreCase(decision)) {
		    String id = row.patientID;
		    patientids.add(id);
		}
	    }

	    ArrayList<TimelineRow> tlrows = i2b2Model.getTimelineRows(rowData);
	    String result = PDOQueryClient.getPDOResponseString(tlrows,
		    patientids, explorer);

	    if (result != null) {
		if (result.equalsIgnoreCase("memory error")) {
		    java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
			    JOptionPane
				    .showMessageDialog(
					    oAwtContainer,
					    "Running out of memory while loading "
						    +
						    /* ids.size() + */" one patients."
						    + "\nPlease try it again with a smaller number of patients.");
			}
		    });

		    bNoError = false;
		} else if (result.equalsIgnoreCase("error")) {
		    java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
			    JOptionPane
				    .showMessageDialog(
					    oAwtContainer,
					    "Response delivered from the remote server could not be "
						    + "understood, you may wish to retry your last action");
			}
		    });

		    bNoError = false;
		}

		return result;
	    } else {
		// JOptionPane.showMessageDialog(oAwtContainer, "Response
		// delivered from the remote server could not be understood, you
		// may wish to retry your last action");
		bNoError = false;
		return null;
	    }
	} catch (Exception e) {
	    log.error(e.getMessage());
	    e.printStackTrace();
	    bNoError = false;
	    return null;
	}
    }

    private String getObservationList(String result) {
	try {
	    PDOResponseMessageFactory pdoresponsefactory = new PDOResponseMessageFactory();
	    StatusType statusType = pdoresponsefactory
		    .getStatusFromResponseXML(result);
	    if (!statusType.getType().equalsIgnoreCase("DONE")) {
		return "error";
	    }

	    PatientSet patientDimensionSet = pdoresponsefactory
		    .getPatientSetFromResponseXML(result);
	    if (patientDimensionSet != null) {
		System.out.println("Total patient: "
			+ patientDimensionSet.getPatient().size());
		final String id = patientDimensionSet.getPatient().get(0)
			.getPatientId().getValue();

		oTheParent.getDisplay().syncExec(new Runnable() {
		    public void run() {
			// patientIDText.setText(id);
			// imageComposite.subjectLabel.setText(id);

		    }
		});

		// for(int i=0;
		// i<patientDimensionSet.getPatientDimension().size();i++) {
		// PatientDimensionType patientType =
		// patientDimensionSet.getPatientDimension().get(i);
		// System.out.println("PatientNum: " +
		// patientType.getPatientNum());
		// }
	    } else {
		return "error";
	    }

	    // / testing the visit set
	    // PatientDataType.VisitDimensionSet visitSet =
	    // pdoresponsefactory.getVisitSetFromResponseXML(result);
	    // System.out.println("Total visits:
	    // "+visitSet.getVisitDimension().size());

	    // oset.clear();
	    // oset = null;
	    // oset= new
	    // ArrayList<ObservationSet>(pdoresponsefactory.
	    // getFactSetsFromResponseXML(result));

	    // //fill the observation list and remove the oset
	    if (observationList == null) {
		observationList = new ArrayList<ObservationType>();
	    }
	    observationList.clear();

	    List<ObservationSet> list = pdoresponsefactory
		    .getFactSetsFromResponseXML(result);
	    for (int i = 0; i < list.size(); i++) {
		ObservationSet set = list.get(i);
		for (int j = 0; j < set.getObservation().size(); j++) {
		    // check valtype and duplications here later
		    ObservationType obs = set.getObservation().get(j);
		    observationList.add(obs);
		}
	    }

	    eventSet = null;
	    eventSet = pdoresponsefactory.getEventSetFromResponseXML(result);

	    // System.out.println("\nThe lld file: \n"+resultFile.toString());
	    return "Done";
	} catch (org.apache.axis2.AxisFault e) {
	    e.printStackTrace();
	    return null;
	} catch (Exception e) {
	    e.printStackTrace();
	    return "error";
	}
    }

    public void imageSelected(int index) {
	// int index = imageComb.getSelectionIndex();
	System.out.println("image selection: " + index);

	// imagePanel.removeImage();
	// renderImage(imagePanel, observationList.get(0), false, 0);

    }

    private void parseListOfConcepts(QueryConceptData panel) {
	try {
	    SAXBuilder parser = new SAXBuilder();
	    java.io.StringReader xmlStringReader = new java.io.StringReader(
		    panel.xmlContent());
	    org.jdom.Document tableDoc = parser.build(xmlStringReader);
	    org.jdom.Element tableXml = tableDoc
		    .getRootElement()
		    .getChild(
			    "concepts",
			    Namespace
				    .getNamespace("http://www.i2b2.org/xsd/cell/ont/1.1/"));
	    List conceptChildren = tableXml.getChildren();

	    // parseListOfConcepts(conceptChildren, xmlContent);

	    for (Iterator itr = conceptChildren.iterator(); itr.hasNext();) {
		Element conceptXml = (org.jdom.Element) itr.next();
		Element conTableXml = (Element) conceptXml;//.getChildren().get(
							   // 0);

		Element metadataAttribs = conTableXml.getChild("metadataxml");
		Element valuedataAttribs = null;
		if (metadataAttribs != null) {
		    valuedataAttribs = metadataAttribs
			    .getChild("ValueMetadata");
		}

		values.clear();
		valueType = null;

		org.jdom.Element nameXml = conTableXml.getChild("name");
		String cname = nameXml.getText();
		if (cname.toLowerCase().startsWith("zz")) {
		    cname = cname.substring(2).trim();
		}

		curRowNumber = rowData.size() + 1;
		ConceptTableRow row = null;
		ArrayList<ConceptTableRow> alist = new ArrayList<ConceptTableRow>();
		XMLOutputter outputter = new XMLOutputter();
		String xmlOutput = outputter.outputString(conceptXml);
		ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
			.getModel();

		if (values.size() == 0) {
		    row = new ConceptTableRow();
		    row.rowNumber = curRowNumber;
		    row.conceptName = panel.name();// new String(cname);
		    row.valueType = "N/A";
		    row.valueText = "N/A";
		    row.height = "Medium";
		    row.color = new RGB(0, 0, 128);
		    row.conceptXml = new String(xmlOutput);

		    /*
		     * if (m_Model..getConstrainByValue().size() > 0) {
		     * node.setValueConstrains(itemType.getConstrainByValue());
		     * 
		     * if (itemType.getConstrainByValue().size() > 0) {
		     * node.setValueConstrains(itemType.getConstrainByValue());
		     * if (node.valuePropertyData().hasEnumValue()) { if
		     * (node.valuePropertyData().useTextValue()) {
		     * ArrayList<String> results = new ArrayList<String>();
		     * results
		     * .toArray(node.valuePropertyData().value().split(","));
		     * node.valuePropertyData().selectedValues = results; }
		     * 
		     * } }
		     * 
		     * }
		     */
		    row.data(m_Model.valueMode(xmlOutput));

		    if ((panel.hasValue())
			    && (cname.length() < panel.name().length()))// &&
									// (panel
									// .
									// name(
									// ).
									// indexOf
									// ('=')
									// >
									// -1))
		    {
			// Anything after the concept name is the valuetext
			row.valueText = panel.name().substring(cname.length());

			// Set the rows valuePropertyData

			// row.data.valuePropertyData().value(panel.
			// valuePropertyData().value());
			// //..getConstrainByValue());
			row.data.setValueConstrains(panel.constrainByValue);

			if (row.data.valuePropertyData().hasEnumValue()) {
			    row.data.valuePropertyData().useTextValue(
				    panel.valuePropertyData().useTextValue());
			    if (row.data.valuePropertyData().useTextValue()) {
				String pValue[] = panel.valuePropertyData()
					.value().split(",");
				ArrayList<String> results = new ArrayList<String>();
				for (String data : pValue)
				    results.add(data);

				row.data.valuePropertyData().selectedValues = results;
			    }

			}

		    }
		    // Set Date
		    String constrainDate = "";
		    if (panel.startTime() != -1) {
			row.data.startYear(panel.startYear());
			row.data.startMonth(panel.startMonth());
			row.data.startDay(panel.startDay());
			row.data.startTime(panel.startTime());
			// startTime(from.getTime().
			constrainDate = (row.data.startMonth() + 1) + "/"
				+ row.data.startDay() + "/"
				+ (row.data.startYear() + 1900) + " -> ";

		    }
		    if (panel.endTime() != -1) {
			row.data.endYear(panel.endYear());
			row.data.endMonth(panel.endMonth());
			row.data.endDay(panel.endDay());
			row.data.endTime(panel.endTime());
			if (constrainDate.equals(""))
			    constrainDate = " -> ";
			constrainDate += (row.data.endMonth() + 1) + "/"
				+ row.data.endDay() + "/"
				+ (row.data.endYear() + 1900);

		    }
		    row.valueType = constrainDate;
		    alist.add(row);
		} else {
		    Set s = values.keySet();
		    String op = null;
		    Object[] strs = s.toArray();

		    for (int n = 0; n < values.size(); n++) {
			row = new ConceptTableRow();
			row.rowNumber = curRowNumber;
			row.conceptName = new String(cname);
			row.valueType = new String(valueType);

			op = (String) strs[n];
			if (op.equalsIgnoreCase("LL")) {
			    row.valueText = new String(" < "
				    + values.get(strs[n]));
			    row.height = "Very Low";
			    row.color = new RGB(255, 0, 0);
			} else if (op.equalsIgnoreCase("HL")) {
			    row.valueText = new String(" between "
				    + values.get(strs[n - 1]) + " and "
				    + values.get(strs[n]));
			    row.height = "Low";
			    row.color = new RGB(255, 215, 0);
			} else if (op.equalsIgnoreCase("NM")) {
			    row.valueText = new String(" between "
				    + values.get(strs[n - 1]) + " and "
				    + values.get(strs[n]));
			    row.height = "Medium";
			    row.color = new RGB(0, 255, 0);
			} else if (op.equalsIgnoreCase("LH")) {
			    row.valueText = new String(" between "
				    + values.get(strs[n]) + " and "
				    + values.get(strs[n + 1]));
			    row.height = "Tall";
			    row.color = new RGB(255, 215, 0);
			} else if (op.equalsIgnoreCase("HH")) {
			    row.valueText = new String(" > "
				    + values.get(strs[n]));
			    row.height = "Very Tall";
			    row.color = new RGB(255, 0, 0);
			}
			row.conceptXml = new String(xmlOutput);
			row.data = m_Model.valueMode(xmlOutput);

			alist.add(row);
		    }
		}
		rowData.add(alist);
	    }
	} catch (JDOMException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    private void setupTableOLD(QueryConceptData xmlContent) {
	try {
	    SAXBuilder parser = new SAXBuilder();
	    java.io.StringReader xmlStringReader = new java.io.StringReader(
		    xmlContent.xmlContent());
	    org.jdom.Document tableDoc = parser.build(xmlStringReader);
	    org.jdom.Element tableXml = tableDoc
		    .getRootElement()
		    .getChild(
			    "concepts",
			    Namespace
				    .getNamespace("http://www.i2b2.org/xsd/cell/ont/1.1/"));
	    List conceptChildren = tableXml.getChildren();
	    // parseListOfConcepts(conceptChildren, xmlContent);
	} catch (JDOMException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    public void populatePatientTable(ArrayList<String> xmlContents) {
	PatientKTableModel m_Model = (PatientKTableModel) patientTable
		.getModel();
	m_Model.deleteAllRows();
	curRowNumber = 0;

	m_Model.populateTable(patientRowData);

	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		patientTable.redraw();
	    }
	});
    }

    public void populateTable(ArrayList<QueryConceptData> xmlContents) {
	ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
		.getModel();
	m_Model.deleteAllRows();
	curRowNumber = 0;
	rowData.clear();

	for (int i = 0; i < xmlContents.size(); i++) {
	    // setupTable(xmlContents.get(i));
	    parseListOfConcepts(xmlContents.get(i));
	}
	m_Model.populateTable(rowData);

	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		conceptTable.redraw();
	    }
	});
    }

    private void performVisualization() {
	if (tabFolder.getSelectionIndex() == 1) {
	    DestroyMiniVisualization(oAwtContainer);
	} else if (tabFolder.getSelectionIndex() == 0) {
	    tabFolder.setSelection(1);
	}
	String patientSetStr = patientSetText.getText();

	if (patientSetStr.equalsIgnoreCase("All")) {
	    int minPatient = 0;
	    try {
		String minText = patientMinNumText.getText();
		minPatient = Integer.parseInt(minText);
	    } catch (Exception e1) {
		minPatient = -1;
	    }

	    int maxPatient = 0;
	    try {
		maxPatient = Integer.parseInt(patientMaxNumText.getText());
	    } catch (Exception e2) {
		maxPatient = -1;
	    }

	    PerformVisualizationQuery(oAwtContainer, "All", minPatient,
		    maxPatient, bDisplayAllData);
	} else {

	    int min = Integer.parseInt(patientMinNumText.getText());
	    int max = Integer.parseInt(patientMaxNumText.getText());

	    PerformVisualizationQuery(oAwtContainer, patientRefId, min, max,
		    bDisplayAllData);
	}
    }

    public void generateTimeLine() {
	oTheParent.getDisplay().syncExec(new Runnable() {
	    public void run() {
		performVisualization();
	    }
	});
    }

    private void resetRowNumber() {
	for (int i = 0; i < rowData.size(); i++) {
	    ArrayList list = (ArrayList) rowData.get(i);
	    for (int j = 0; j < list.size(); j++) {
		ConceptTableRow row = (ConceptTableRow) list.get(j);
		row.rowNumber = i + 1;
	    }
	}
    }

    private int getRowNumber() {
	int n = 1;
	for (int i = 0; i < rowData.size(); i++) {
	    ArrayList list = (ArrayList) rowData.get(i);
	    for (int j = 0; j < list.size(); j++) {
		n++;
	    }
	}
	return n;
    }

    private void parseDropConcepts(List conceptChildren, DropTargetEvent event) {
	for (Iterator itr = conceptChildren.iterator(); itr.hasNext();) {
	    Element conceptXml = (org.jdom.Element) itr.next();
	    String conceptText = conceptXml.getText().trim();
	    if (conceptText.equals("null")) // this is root level node
	    {
		MessageBox mBox = new MessageBox(conceptTable.getShell(),
			SWT.ICON_INFORMATION | SWT.OK);
		mBox.setText("Please Note ...");
		mBox
			.setMessage("You can not use this item in a query, it is only used for organizing the lists.");
		mBox.open();
		event.detail = DND.DROP_NONE;
		return;
	    }
	    Element conTableXml = (Element) conceptXml;// .getChildren().get(0);

	    Element visualAttribs = conTableXml.getChild("visualattributes");
	    String sVisualAttribs = visualAttribs.getText().trim();
	    if (sVisualAttribs.toUpperCase().startsWith("C")) {
		MessageBox mBox = new MessageBox(conceptTable.getShell(),
			SWT.ICON_INFORMATION | SWT.OK);
		mBox.setText("Please Note ...");
		mBox
			.setMessage("You can not use this item in a query, it is only used for organizing the lists.");
		mBox.open();
		event.detail = DND.DROP_NONE;
		return;
	    }

	    Element metadataAttribs = conTableXml.getChild("metadataxml");
	    Element valuedataAttribs = null;
	    if (metadataAttribs != null) {
		valuedataAttribs = metadataAttribs.getChild("ValueMetadata");
	    }

	    String val = null;
	    values.clear();
	    valueType = null;
	    String tmp = null;
	    double ll = -1.0;
	    double hl = -1.0;
	    double lh = -1.0;
	    double hh = -1.0;

	    /*
	     * if (valuedataAttribs != null) { if ((tmp =
	     * valuedataAttribs.getChildTextTrim("Oktousevalues")) != null) { if
	     * (tmp.equalsIgnoreCase("Y")) { valueType = "NVAL_NUM"; } else if
	     * (tmp.equalsIgnoreCase("N")) { valueType = "TVAL_CHAR"; } } if
	     * ((val = valuedataAttribs.getChildTextTrim("LowofLowValue")) !=
	     * null && !(val.equals(""))) { ll = new Double(val).doubleValue();
	     * val = null; } if ((val =
	     * valuedataAttribs.getChildTextTrim("HighofLowValue")) != null &&
	     * !(val.equals(""))) { hl = new Double(val).doubleValue(); val =
	     * null; } if ((val =
	     * valuedataAttribs.getChildTextTrim("LowofHighValue")) != null &&
	     * !(val.equals(""))) { lh = new Double(val).doubleValue(); val =
	     * null; } if ((val =
	     * valuedataAttribs.getChildTextTrim("HighofHighValue")) != null &&
	     * !(val.equals(""))) { hh = new Double(val).doubleValue(); val =
	     * null; } }
	     * 
	     * if (ll >= 0) { values.put("LL", new Double(ll).toString()); } if
	     * (hl > 0 && hl >= ll) { values.put("HL", new
	     * Double(hl).toString()); } if (lh > 0 && lh >= hl) {
	     * values.put("NM", new Double(lh).toString()); values.put("LH", new
	     * Double(lh).toString()); } if (hh > 0 && hh >= lh) {
	     * values.put("HH", new Double(hh).toString()); }
	     * 
	     * System.out.println("Got values: " + values.size()); for (int i =
	     * 0; i < values.size(); i++) { System.out.println("Got value: " +
	     * values.get(values.keySet().toArray()[i])); }
	     */

	    org.jdom.Element nameXml = conTableXml.getChild("name");
	    String cname = nameXml.getText();
	    if (cname.toLowerCase().startsWith("zz")) {
		cname = cname.substring(2).trim();
	    }

	    // org.jdom.Element fullnameXml = conTableXml.getChild("key");
	    // String cfullname = fullnameXml.getText();

	    curRowNumber = rowData.size();
	    ConceptKTableModel m_Model = (ConceptKTableModel) conceptTable
		    .getModel();
	    m_Model.fillDataFromTable(rowData);

	    curRowNumber = rowData.size() + 1;
	    ConceptTableRow row = null;
	    ArrayList<ConceptTableRow> alist = new ArrayList<ConceptTableRow>();
	    XMLOutputter outputter = new XMLOutputter();
	    String xmlOutput = outputter.outputString(conceptXml);

	    if (values.size() == 0) {
		row = new ConceptTableRow();
		row.rowNumber = curRowNumber;
		row.conceptName = new String(cname);
		row.valueType = "N/A";
		row.valueText = "N/A";
		row.height = "Medium";
		row.color = new RGB(0, 0, 128);
		row.conceptXml = new String(xmlOutput);
		row.data(m_Model.valueMode(xmlOutput));
		alist.add(row);
	    } else {
		Set s = values.keySet();
		String op = null;
		Object[] strs = s.toArray();

		for (int n = 0; n < values.size(); n++) {
		    row = new ConceptTableRow();
		    row.rowNumber = curRowNumber;
		    row.conceptName = new String(cname);
		    row.valueType = new String(valueType);

		    op = (String) strs[n];
		    if (op.equalsIgnoreCase("LL")) {
			row.valueText = new String(" < " + values.get(strs[n]));
			row.height = "Very Low";
			row.color = new RGB(255, 0, 0);
		    } else if (op.equalsIgnoreCase("HL")) {
			row.valueText = new String(" between "
				+ values.get(strs[n - 1]) + " and "
				+ values.get(strs[n]));
			row.height = "Low";
			row.color = new RGB(255, 215, 0);
		    } else if (op.equalsIgnoreCase("NM")) {
			row.valueText = new String(" between "
				+ values.get(strs[n - 1]) + " and "
				+ values.get(strs[n]));
			row.height = "Medium";
			row.color = new RGB(0, 255, 0);
		    } else if (op.equalsIgnoreCase("LH")) {
			row.valueText = new String(" between "
				+ values.get(strs[n]) + " and "
				+ values.get(strs[n + 1]));
			row.height = "Tall";
			row.color = new RGB(255, 215, 0);
		    } else if (op.equalsIgnoreCase("HH")) {
			row.valueText = new String(" > " + values.get(strs[n]));
			row.height = "Very Tall";
			row.color = new RGB(255, 0, 0);
		    }
		    row.conceptXml = new String(xmlOutput);
		    row.data = m_Model.valueMode(xmlOutput);

		    alist.add(row);
		}
	    }
	    rowData.add(alist);

	    m_Model.deleteAllRows();
	    m_Model.populateTable(rowData);
	}
    }

    protected Control getConceptTreeTabControl(TabFolder tabFolder) {
	Composite compositeQueryTree = new Composite(tabFolder, SWT.NULL);
	GridLayout gridLayout = new GridLayout();
	gridLayout.numColumns = 1;
	gridLayout.horizontalSpacing = 1;
	gridLayout.verticalSpacing = 1;
	gridLayout.marginHeight = 0;
	gridLayout.marginWidth = 0;
	compositeQueryTree.setLayout(gridLayout);

	// *Group compositeQueryTreeTop = new Group(compositeQueryTree,
	// SWT.NULL);
	// *compositeQueryTreeTop.setText("Query Items");
	GridLayout gridLayoutTree = new GridLayout(1, false);
	gridLayoutTree.numColumns = 1;
	gridLayoutTree.marginHeight = 0;
	// *compositeQueryTreeTop.setLayout(gridLayoutTree);
	GridData fromTreeGridData = new GridData(GridData.FILL_BOTH);
	fromTreeGridData.widthHint = 300;
	// *compositeQueryTreeTop.setLayoutData(fromTreeGridData);
	compositeQueryTree.setLayoutData(fromTreeGridData);

	// TreeComposite dragTree = new TreeComposite(compositeQueryTree, 1,
	// slm);
	// TreeComposite dragTree = new TreeComposite(compositeQueryTree,
	// 1,slm);
	// dragTree.setLayoutData(new GridData (GridData.FILL_BOTH));
	// dragTree.setLayout(gridLayout);

	return compositeQueryTree;
    }

    public static void main(String[] args) {
	final String ssFakeApplicationConfigurationXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
		+ "<contents>\r\n"
		+ "    <table>\r\n"
		+ "        <name>Demographics</name>\r\n"
		+ "        <tableName>Demographics</tableName>\r\n"
		+ "        <status/>\r\n"
		+ "        <description/>\r\n"
		+ "        <lookupDB>metadata</lookupDB>\r\n"
		+ "        <webserviceName>http://phsi2b2appprod1.mgh.harvard.edu:8080/i2b2/services/Select</webserviceName>\r\n"
		+ "    </table>\r\n"
		+ "    <table>\r\n"
		+ "        <name>Diagnoses</name>\r\n"
		+ "        <tableName>Diagnoses</tableName>\r\n"
		+ "        <status/>\r\n"
		+ "        <description/>\r\n"
		+ "        <lookupDB>metadata</lookupDB>\r\n"
		+ "        <webserviceName>http://phsi2b2appprod1.mgh.harvard.edu:8080/i2b2/services/Select</webserviceName>\r\n"
		+ "    </table>\r\n"
		+ "    <table>\r\n"
		+ "        <name>Medications</name>\r\n"
		+ "        <tableName>Medications</tableName>\r\n"
		+ "        <status/>\r\n"
		+ "        <description/>\r\n"
		+ "        <lookupDB>metadata</lookupDB>\r\n"
		+ "        <webserviceName>http://phsi2b2appprod1.mgh.harvard.edu:8080/i2b2/services/Select</webserviceName>\r\n"
		+ "    </table>\r\n"
		+ "    <table>\r\n"
		+ "        <name>I2B2</name>\r\n"
		+ "        <tableName>i2b2</tableName>\r\n"
		+ "        <status/>\r\n"
		+ "        <description/>\r\n"
		+ "        <lookupDB>metadata</lookupDB>\r\n"
		+ "        <webserviceName>http://phsi2b2appprod1.mgh.harvard.edu:8080/i2b2/services/Select</webserviceName>\r\n"
		+ "    </table>\r\n" + "</contents>";
	System.setProperty("ApplicationConfigurationXML",
		ssFakeApplicationConfigurationXML);
	Display display = new Display();
	Shell shell = new Shell(display);
	shell.setLayout(new FillLayout(SWT.HORIZONTAL));
	shell.setText("ExplorerC Test");
	shell.setSize(1000, 800);
	// ExplorerC oExplorerC = new ExplorerC(shell);
	// shell.pack();
	shell.open();
	// oExplorerC.run();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
	display.dispose();
    }

    boolean bStillPerformingVisualizationQuery;

    boolean bNoError;

    private void removelldFile() {
	try {
	    String datafile = "i2b2xml.lld";
	    String appDirectory = System.getProperty("user.dir").toString();
	    datafile = appDirectory + File.separator + datafile;
	    File f = new File(datafile);
	    if (f.exists()) {
		f.delete();
	    }
	    System.out.println(datafile);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public boolean PerformVisualizationQuery(
	    final java.awt.Container oAwtContainer, final int[] patientIds,
	    final boolean bDisplayAll) {
	bStillPerformingVisualizationQuery = true;
	bNoError = true;

	p = new WaitPanel((int) (oAwtContainer.getWidth() * 0.40),
		(int) (oAwtContainer.getHeight() * 0.40), patientIds.length);
	oAwtContainer.add(p);
	p.setBounds(0, 0, p.getParent().getWidth(), p.getParent().getHeight());
	p.init((int) (p.getParent().getWidth() * 0.40), (int) (p.getParent()
		.getHeight() * 0.40));
	p.go();
	p.setVisible(true);

	visualizationQueryThread = new Thread() {
	    public void run() {
		log.info("before getResultSetAsi2b2XML: " + new Date());
		try {

		    p.stop();
		    p.setVisible(false);
		} catch (Exception e) {
		    log.error(e.getMessage());
		    bNoError = false;
		}
		log.info("after getImage: " + new Date());
	    }
	};

	try {
	    visualizationQueryThread.start();
	} catch (Exception e) {
	    log.error(e.getMessage());
	}

	return bNoError;
    }

    public boolean PerformVisualizationQuery(
	    final java.awt.Container oAwtContainer,
	    final ArrayList<String> ids, final boolean bDisplayAll) {
	bStillPerformingVisualizationQuery = true;
	bNoError = true;

	p = new WaitPanel((int) (oAwtContainer.getWidth() * 0.40),
		(int) (oAwtContainer.getHeight() * 0.40), ids.size());
	oAwtContainer.add(p);
	p.setBounds(0, 0, p.getParent().getWidth(), p.getParent().getHeight());
	p.init((int) (p.getParent().getWidth() * 0.40), (int) (p.getParent()
		.getHeight() * 0.40));
	p.go();
	p.setVisible(true);

	removelldFile();

	final DataExporter explorer = this;
	visualizationQueryThread = new Thread() {
	    public void run() {
		log.info("before getResultSetAsi2b2XML: " + new Date());
		try {
		    ConceptKTableModel i2b2Model = (ConceptKTableModel) conceptTable
			    .getModel();
		    // String xmlContent = i2b2Model.getContentXml();
		    i2b2Model.fillDataFromTable(rowData);

		    Properties properties = new Properties();
		    String writeFileStr = "";
		    String filename = "i2b2workbench.properties";
		    try {
			properties.load(new FileInputStream(filename));
			writeFileStr = properties
				.getProperty("writeTimelineFile");
			System.out.println("Properties writeFile: ="
				+ writeFileStr);
		    } catch (IOException e) {
			log.error(e.getMessage());
		    }

		    boolean writeFile = false;
		    if ((writeFileStr != null)
			    && (writeFileStr.equalsIgnoreCase("yes"))) {
			writeFile = true;
		    }

		    // ArrayList<String> patientids = new ArrayList<String>();
		    // for(int i=minPatient-1; i<minPatient+maxPatient; i++) {
		    // String id = patientRowData.get(i).patientID;
		    // patientids.add(id);
		    // }

		    ArrayList<TimelineRow> tlrows = i2b2Model
			    .getTimelineRows(rowData);
		    String result = PDOQueryClient.getPDOResponseString(tlrows,
			    ids, explorer);

		    if (result != null) {
			if (result.equalsIgnoreCase("memory error")) {
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    oAwtContainer,
						    "Running out of memory while loading "
							    + ids.size()
							    + " patients."
							    + "\nPlease try it again with a smaller number of patients.");
				}
			    });

			    bNoError = false;
			} else if (result.equalsIgnoreCase("error")) {
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    oAwtContainer,
						    "Response delivered from the remote server could not be "
							    + "understood, you may wish to retry your last action");
				}
			    });

			    bNoError = false;
			} else {
			    PerformMiniVisualization(oAwtContainer, result,
				    writeFile);
			}
		    } else {
			// JOptionPane.showMessageDialog(oAwtContainer,
			// "Response delivered from the remote server could not
			// be understood, you may wish to retry your last
			// action");
			bNoError = false;
		    }

		    p.stop();
		    p.setVisible(false);
		    if (result == null
			    || result.equalsIgnoreCase("memory error")
			    || result.equalsIgnoreCase("error")) {
			oTheParent.getDisplay().syncExec(new Runnable() {
			    public void run() {
				tabFolder.setSelection(0);
			    }
			});
		    }
		} catch (Exception e) {
		    log.error(e.getMessage());
		    bNoError = false;
		}
		log.info("after getResultSetAsi2b2XML: " + new Date());
	    }
	};

	try {
	    visualizationQueryThread.start();
	} catch (Exception e) {
	    log.error(e.getMessage());
	}

	return bNoError;

    }

    public boolean PerformVisualizationQuery(
	    final java.awt.Container oAwtContainer, final int minPatient,
	    final int maxPatient, final boolean bDisplayAll) {
	bStillPerformingVisualizationQuery = true;
	bNoError = true;

	p = new WaitPanel((int) (oAwtContainer.getWidth() * 0.40),
		(int) (oAwtContainer.getHeight() * 0.40),
		(maxPatient - minPatient));
	oAwtContainer.add(p);
	p.setBounds(0, 0, p.getParent().getWidth(), p.getParent().getHeight());
	p.init((int) (p.getParent().getWidth() * 0.40), (int) (p.getParent()
		.getHeight() * 0.40));
	p.go();
	p.setVisible(true);

	removelldFile();

	final DataExporter explorer = this;
	visualizationQueryThread = new Thread() {
	    public void run() {
		log.info("before getResultSetAsi2b2XML: " + new Date());
		try {
		    ConceptKTableModel i2b2Model = (ConceptKTableModel) conceptTable
			    .getModel();
		    // String xmlContent = i2b2Model.getContentXml();
		    i2b2Model.fillDataFromTable(rowData);

		    Properties properties = new Properties();
		    String writeFileStr = "";
		    String filename = "i2b2workbench.properties";
		    try {
			properties.load(new FileInputStream(filename));
			writeFileStr = properties
				.getProperty("writeTimelineFile");
			System.out.println("Properties writeFile: ="
				+ writeFileStr);
		    } catch (IOException e) {
			log.error(e.getMessage());
		    }

		    boolean writeFile = false;
		    if ((writeFileStr != null)
			    && (writeFileStr.equalsIgnoreCase("yes"))) {
			writeFile = true;
		    }

		    ArrayList<String> patientids = new ArrayList<String>();
		    for (int i = minPatient - 1; i < minPatient + maxPatient; i++) {
			String id = patientRowData.get(i).patientID;
			patientids.add(id);
		    }

		    ArrayList<TimelineRow> tlrows = i2b2Model
			    .getTimelineRows(rowData);
		    String result = PDOQueryClient.getPDOResponseString(tlrows,
			    patientids, explorer);

		    if (result != null) {
			if (result.equalsIgnoreCase("memory error")) {
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    oAwtContainer,
						    "Running out of memory while loading "
							    + (maxPatient - minPatient)
							    + " patients."
							    + "\nPlease try it again with a smaller number of patients.");
				}
			    });

			    bNoError = false;
			} else if (result.equalsIgnoreCase("error")) {
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    oAwtContainer,
						    "Response delivered from the remote server could not be "
							    + "understood, you may wish to retry your last action");
				}
			    });

			    bNoError = false;
			} else {
			    PerformMiniVisualization(oAwtContainer, result,
				    writeFile);
			}
		    } else {
			// JOptionPane.showMessageDialog(oAwtContainer,
			// "Response delivered from the remote server could not
			// be understood, you may wish to retry your last
			// action");
			bNoError = false;
		    }

		    p.stop();
		    p.setVisible(false);
		    if (result == null
			    || result.equalsIgnoreCase("memory error")
			    || result.equalsIgnoreCase("error")) {
			oTheParent.getDisplay().syncExec(new Runnable() {
			    public void run() {
				tabFolder.setSelection(0);
			    }
			});
		    }
		} catch (Exception e) {
		    log.error(e.getMessage());
		    bNoError = false;
		}
		log.info("after getResultSetAsi2b2XML: " + new Date());
	    }
	};

	try {
	    visualizationQueryThread.start();
	} catch (Exception e) {
	    log.error(e.getMessage());
	}

	return bNoError;

    }

    public void getConceptRows() {
    }

    public void getPatientList() {
    }

    public boolean PerformVisualizationQuery(
	    final java.awt.Container oAwtContainer, final String patientRefId,
	    final int minPatient, final int maxPatient,
	    final boolean bDisplayAll) {
	bStillPerformingVisualizationQuery = true;
	bNoError = true;

	p = new WaitPanel((int) (oAwtContainer.getWidth() * 0.40),
		(int) (oAwtContainer.getHeight() * 0.40),
		(maxPatient - minPatient));
	oAwtContainer.add(p);
	p.setBounds(0, 0, p.getParent().getWidth(), p.getParent().getHeight());
	p.init((int) (p.getParent().getWidth() * 0.40), (int) (p.getParent()
		.getHeight() * 0.40));
	p.go();
	p.setVisible(true);

	removelldFile();

	final DataExporter explorer = this;
	visualizationQueryThread = new Thread() {
	    public void run() {
		log.info("before getResultSetAsi2b2XML: " + new Date());
		try {
		    ConceptKTableModel i2b2Model = (ConceptKTableModel) conceptTable
			    .getModel();
		    // String xmlContent = i2b2Model.getContentXml();
		    i2b2Model.fillDataFromTable(rowData);

		    Properties properties = new Properties();
		    String writeFileStr = "";
		    String filename = "i2b2workbench.properties";
		    try {
			properties.load(new FileInputStream(filename));
			writeFileStr = properties
				.getProperty("writeTimelineFile");
			System.out.println("Properties writeFile: ="
				+ writeFileStr);
		    } catch (IOException e) {
			log.error(e.getMessage());
		    }

		    boolean writeFile = false;
		    if ((writeFileStr != null)
			    && (writeFileStr.equalsIgnoreCase("yes"))) {
			writeFile = true;
		    }

		    ArrayList<TimelineRow> tlrows = i2b2Model
			    .getTimelineRows(rowData);
		    String result = PDOQueryClient.getlldString(tlrows,
			    patientRefId, minPatient, minPatient + maxPatient,
			    bDisplayAll, writeFile, bDisplayDemographics,
			    explorer);

		    if (result != null) {
			if (result.equalsIgnoreCase("memory error")) {
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    oAwtContainer,
						    "Running out of memory while loading "
							    + (maxPatient - minPatient)
							    + " patients."
							    + "\nPlease try it again with a smaller number of patients.");
				}
			    });

			    bNoError = false;
			} else if (result.equalsIgnoreCase("error")) {
			    java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
				    JOptionPane
					    .showMessageDialog(
						    oAwtContainer,
						    "Response delivered from the remote server could not be "
							    + "understood, you may wish to retry your last action");
				}
			    });

			    bNoError = false;
			} else {
			    PerformMiniVisualization(oAwtContainer, result,
				    writeFile);
			}
		    } else {
			// JOptionPane.showMessageDialog(oAwtContainer,
			// "Response delivered from the remote server could not
			// be understood, you may wish to retry your last
			// action");
			bNoError = false;
		    }

		    p.stop();
		    p.setVisible(false);
		    if (result == null
			    || result.equalsIgnoreCase("memory error")
			    || result.equalsIgnoreCase("error")) {
			oTheParent.getDisplay().syncExec(new Runnable() {
			    public void run() {
				tabFolder.setSelection(0);
			    }
			});
		    }
		} catch (Exception e) {
		    log.error(e.getMessage());
		    bNoError = false;
		}
		log.info("after getResultSetAsi2b2XML: " + new Date());
	    }
	};

	try {
	    visualizationQueryThread.start();
	} catch (Exception e) {
	    log.error(e.getMessage());
	}

	return bNoError;

    }

    public void PerformMiniVisualization(java.awt.Container poAwtContainer,
	    String result, boolean writeFile) {
	try {
	    poAwtContainer.removeAll();

	    log.info("Got to PerformMiniVisualization");
	    // record record1 = new record();
	    // poAwtContainer.add(record1);
	    // record1.start();
	    // if(writeFile) {
	    // record1.init();
	    // }
	    // else {
	    // record1.init(result);
	    // }
	    // theRecord = record1;
	} catch (Exception e) {
	    log.error("done");
	}
    }

    @SuppressWarnings("deprecation")
    public void DestroyMiniVisualization(java.awt.Container poAwtContainer) {
	try {
	    if (p != null) {
		p.stop();
		p.setVisible(false);
		p = null;
	    }
	    if (visualizationQueryThread != null) {
		visualizationQueryThread.stop();
		visualizationQueryThread = null;
	    }
	    if (oConnection != null) {
		// DBLib.closeConnection(oConnection);
		oConnection = null;
	    }
	    System.out.println("got to destroy");
	    // theRecord.removeAll();
	    // theRecord = null;
	    poAwtContainer.removeAll();
	} catch (Exception e) {
	    // log.error("done");
	}
    }
}