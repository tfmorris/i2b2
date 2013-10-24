package net.nbirn.srbclient.plugin.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import net.nbirn.srbclient.data.BatchWorker;
import net.nbirn.srbclient.data.ClientDirWorker;
import net.nbirn.srbclient.data.CreatePDO;
import net.nbirn.srbclient.data.DirWorker;
import net.nbirn.srbclient.data.FtpDirWorker;
import net.nbirn.srbclient.data.IrodsDirWorker;
import net.nbirn.srbclient.data.SFtpDirWorker;
import net.nbirn.srbclient.data.StatusWorker;
import net.nbirn.srbclient.data.XMLtoPDO;
import net.nbirn.srbclient.utils.DirectoryTable;
import net.nbirn.srbclient.utils.IFolderListener;
import net.nbirn.srbclient.utils.Messages;
import net.nbirn.srbclient.utils.SrbDirectoryTable;
import net.nbirn.srbclient.wizards.LoaderWizard;
import net.nbirn.srbclient.wizards.NewSrbConnectionWizard;
import net.nbirn.srbclient.wizards.NewSrbConnectionwizardDlg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.ViewPart;

import com.swtdesigner.SWTResourceManager;

import edu.harvard.i2b2.eclipse.UserInfoBean;

public class ClientFolderView extends ViewPart implements IWindowListener,
IFolderListener  {

	private Table table_2;
	public static String VIEW_ID = "net.nbirn.srbclient.plugin.views.ClientFolderView"; //$NON-NLS-1$
	private static final Log log = LogFactory.getLog(ClientFolderView.class);

	private Composite compositeFile;
	private String key = null;

	static Text patientId;
	static Text numberPerBatch;
	static Text visitId;
	static Text startDate;
	static Text conceptCd;
	static Combo localCombo;
	static Combo remoteCombo;
	private BatchWorker batchWorker = new BatchWorker();
	static DirectoryTable directoryTable;
	static SrbDirectoryTable remoteTable;
	private Table txfStatusTable;
	private  Button startButton;
	private Button encryptMrnButton;
	private Button encryptTextButton;
	private Button encryptFileButton;
	private Button missingPatientNumberButton;
	private Button missingVisitNumberButton;
	//static Table localTable;
	//static Table remoteTable;
	private Hashtable filename = new Hashtable();
	private DirWorker dirWorker = null;
	private Table mrnMappingTable;
	private Table eventMappingTable;
	private Table obserFactTable;
	private Label uploadStatus;
	//PatientDataType PDO;
	private Group step3Group;
	private TabFolder tabFolder_1 ;
	private ProgressBar progressBar;
	private Display display;
	private Shell shell;
	XMLtoPDO xmltopdo= new XMLtoPDO();
	public ClientFolderView() {
		super();
	}


	private String getKey()
	{
		GetKeyDialog getkey = new GetKeyDialog(getSite().getShell());
		key = getkey.open();

		if(key == null) {
			return null;
		}

		return key;
	}



	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout());
		log.info(Messages.getString("ClientFolderView.PluginVersion")); //$NON-NLS-1$
		GridLayout gridLayout;
		//gridLayout.makeColumnsEqualWidth = true;
		GridData gridData;
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gridData.verticalSpan = 2;


		if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod") != null) //$NON-NLS-1$
			if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("SRB")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				dirWorker = new IrodsDirWorker();
				getSite().getWorkbenchWindow().getWorkbench().addWindowListener(this);
				dirWorker.getFtpDirWorker().addClientFolderListener(this);
			}
			else if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("IRODS")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				dirWorker = new IrodsDirWorker();
				getSite().getWorkbenchWindow().getWorkbench().addWindowListener(this);
				dirWorker.getFtpDirWorker().addClientFolderListener(this);
			}
			else if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("FTP")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				dirWorker = new FtpDirWorker();
				getSite().getWorkbenchWindow().getWorkbench().addWindowListener(this);
				dirWorker.getFtpDirWorker().addClientFolderListener(this);
			}
			else if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("SFTP")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				dirWorker = new SFtpDirWorker();
				getSite().getWorkbenchWindow().getWorkbench().addWindowListener(this);
				dirWorker.getFtpDirWorker().addClientFolderListener(this);
			}
		//final Table remoteTable = new Table(remoteSystemGroup, SWT.BORDER);


		compositeFile = new Composite(parent, SWT.NONE); //SWT.SHELL_TRIM | SWT.H_SCROLL);
		compositeFile.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compositeFile.setLayout(new GridLayout());

		//	compositeFile.setSize(200, 400);


		final TabFolder tabFolder = new TabFolder(compositeFile, SWT.NONE
		);
		final GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_tabFolder.widthHint = 714;
		tabFolder.setLayoutData(gd_tabFolder);


		if (dirWorker != null) {
			connectFtp();
		}
		//			if (dirWorker.isOnline()) {

		final TabItem batchTabItem = new TabItem(tabFolder, SWT.NONE);
		batchTabItem.setText(Messages.getString("ClientFolderView.TabBatch")); //$NON-NLS-1$

		final Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		composite_2.setLayout(new GridLayout());
		batchTabItem.setControl(composite_2);





		final Group step2Group = new Group(composite_2, SWT.NONE);
		final GridData gd_step2Group = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_step2Group.heightHint = 58;
		step2Group.setLayoutData(gd_step2Group);
		final FillLayout fillLayout = new FillLayout();
		fillLayout.spacing = 10;
		step2Group.setLayout(fillLayout);
		step2Group.setFont(SWTResourceManager.getFont("", 10, SWT.BOLD)); //$NON-NLS-1$
		//step2Group.setFont(SWTResourceManager.getFont("", 10, SWT.BOLD));
		step2Group.setText(Messages.getString("ClientFolderView.GroupStep1")); //$NON-NLS-1$


		//Display.getDefault().getActiveShell().setBackgroundMode(SWT.INHERIT_DEFAULT);
		//fileLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		//fileLabel.setBackgroundMode(SWT.INHERIT_DEFAULT);		


		final Group eitherGroup = new Group(step2Group, SWT.NONE);
		eitherGroup.setLayout(new GridLayout());
		eitherGroup.setText("Either");

		final Button openFileButton = new Button(eitherGroup, SWT.NONE);
		openFileButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		openFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (!(UserInfoBean.getInstance().isRoleInProject("ADMIN")) &&
						!(UserInfoBean.getInstance().isRoleInProject("MANAGER")))
						{
					MessageDialog.openError(getSite().getShell(),Messages.getString("ClientFolderView.DropFileErrorPopupTitle"),Messages.getString("ClientFolderView.MinRoleNeeded")); //$NON-NLS-1$ //$NON-NLS-2$
					return;
						}



				try {
					//BufferedReader reader = new BufferedReader (new FileReader (selected));
					//String text = reader.readLine().toUpperCase();
					//reader.close();
					//if (text == null)
					//	throw new Exception ("File does not contain header rpw");
					populateFileList( null ); 

					//TODO FIX
					/*
					if (tabFolder_1.getSelectionIndex() == 0) //text.startsWith("PAT"))
					{
						populateFileList(patientDimTable, "PAT", selected); //$NON-NLS-1$
						//tabFolder_1.setSelection(0);
					}
					else if (tabFolder_1.getSelectionIndex() == 1) //(text.startsWith("OBS"))
					{
						populateFileList(obserFactTable, "OBS", selected); //$NON-NLS-1$
						//tabFolder_1.setSelection(1);
					}
					else if (tabFolder_1.getSelectionIndex() == 2) // (text.startsWith("PTM"))
					{
						populateFileList(mrnMappingTable, "PTM", selected); //$NON-NLS-1$
						//tabFolder_1.setSelection(2);
					}
					else if (tabFolder_1.getSelectionIndex() == 3) ////(text.startsWith("PRO"))
					{
						populateFileList(providerDimTable, "PRO",  selected); //$NON-NLS-1$
						//tabFolder_1.setSelection(3);
					}
					else if (tabFolder_1.getSelectionIndex() == 4) //(text.startsWith("VIS"))
					{
						populateFileList(visitDimTable, "VIS", selected); //$NON-NLS-1$
						//tabFolder_1.setSelection(4);
					}
					else
					{
						throw new Exception (Messages.getString("ClientFolderView.InvalidHeader")); //$NON-NLS-1$
					}
					 */

					//MM REMOVED cancelFileButton.setEnabled(true);


				} catch (Exception eFile)
				{
					MessageBox messageBox = 
						new MessageBox(Display.getCurrent().getActiveShell(), SWT.OK|SWT.ICON_ERROR);
					messageBox.setMessage(eFile.getMessage());
					messageBox.open();

				}
			}
		});
		openFileButton.setText(Messages.getString("ClientFolderView.Step1ButtonOpen")); //$NON-NLS-1$

		final Group orGroup = new Group(step2Group, SWT.NONE);
		orGroup.setLayout(new GridLayout());
		orGroup.setText("Or");

		final Label dragPatientDataLabel = new Label(orGroup, SWT.NONE);
		dragPatientDataLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		dragPatientDataLabel.setAlignment(SWT.CENTER);
		dragPatientDataLabel.setText("Drag Patient Data to this box");

		DropTarget target = new DropTarget(orGroup, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);

		// Receive data in Text or File format
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] {fileTransfer, textTransfer};
		target.setTransfer(types);

		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				// will accept text but prefer to have files dropped
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (fileTransfer.isSupportedType(event.dataTypes[i])){
						event.currentDataType = event.dataTypes[i];
						// files should only be copied
						if (event.detail != DND.DROP_COPY) {
							event.detail = DND.DROP_NONE;
						}
						break;
					}
				}
			}
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
				if (textTransfer.isSupportedType(event.currentDataType)) {
					// NOTE: on unsupported platforms this will return null
					Object o = textTransfer.nativeToJava(event.currentDataType);
					String t = (String)o;
					if (t != null) System.out.println(t);
				}
			}
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				// allow text to be moved but files should only be copied
				if (fileTransfer.isSupportedType(event.currentDataType)){
					if (event.detail != DND.DROP_COPY) {
						event.detail = DND.DROP_NONE;
					}
				}
			}
			public void dragLeave(DropTargetEvent event) {
			}
			public void dropAccept(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				if (!(UserInfoBean.getInstance().isRoleInProject("ADMIN")) &&
						!(UserInfoBean.getInstance().isRoleInProject("MANAGER")))
				{
					MessageDialog.openError(getSite().getShell(),Messages.getString("ClientFolderView.DropFileErrorPopupTitle"),Messages.getString("ClientFolderView.MinRoleNeeded")); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}


				if (textTransfer.isSupportedType(event.currentDataType)) {
					//String text = (String)event.data;
					populatePDO((String)event.data);

				}
				if (fileTransfer.isSupportedType(event.currentDataType)){
					String[] files = (String[])event.data;
					if ( files.length > 1 )
					{
						MessageDialog.openError(getSite().getShell(),Messages.getString("ClientFolderView.DropFileErrorPopupTitle"),Messages.getString("ClientFolderView.DropFileErrorPopupText")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else
					{
						//If first part of file is <xml> than process in PDO either startup wizard
						if (loadFile(files[0], true).startsWith("<?xml version="))
						{
							populatePDO(loadFile(files[0], false));
						}
						else
						{
							populateFileList( files[0]);
						}
					}
					//for (int i = 0; i < files.length; i++) {
					//TableItem item = new TableItem(dropTable, SWT.NONE);
					//item.setText(files[i]);
					//	int  a =1;
					//}
				}
			}
		});



		step3Group = new Group(composite_2, SWT.NONE);
		final GridData gd_step3Group = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_step3Group.heightHint = 310;
		step3Group.setLayoutData(gd_step3Group);
		step3Group.setLayout(new GridLayout());
		step3Group.setFont(SWTResourceManager.getFont("", 10, SWT.BOLD)); //$NON-NLS-1$
		//step3Group.setFont(SWTResourceManager.getFont("", 10, SWT.BOLD));
		step3Group.setText(Messages.getString("ClientFolderView.GroupStep2")); //$NON-NLS-1$



		tabFolder_1 = new TabFolder(step3Group, SWT.NONE);
		final GridData gd_tabFolder_1 = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_tabFolder_1.widthHint = 678;
		tabFolder_1.setLayoutData(gd_tabFolder_1);

		final TabItem obserFactTab = new TabItem(tabFolder_1, SWT.NONE);
		obserFactTab.setText(Messages.getString("ClientFolderView.Step1TabObervationFact")); //$NON-NLS-1$

		final TabItem mrnMappingTab = new TabItem(tabFolder_1, SWT.NONE);
		mrnMappingTab.setText(Messages.getString("ClientFolderView.Step1TabMRNMapping")); //$NON-NLS-1$

		final TabItem eventMappingTab = new TabItem(tabFolder_1, SWT.NONE);
		eventMappingTab.setText(Messages.getString("ClientFolderView.Step1TabVisitMapping")); //$NON-NLS-1$


		//	final TabItem patientDimTab = new TabItem(tabFolder_1, SWT.NONE);
		//	patientDimTab.setText(Messages.getString("ClientFolderView.Step1TabPatient")); //$NON-NLS-1$

		//	final TabItem visitDimTab = new TabItem(tabFolder_1, SWT.NONE);
		//	visitDimTab.setText(Messages.getString("ClientFolderView.Step1TabVisit")); //$NON-NLS-1$


		//mm Turned off provider



		// Tables within Step 1
		//MRN Mapping
		eventMappingTable = new Table(tabFolder_1, SWT.NONE);
		eventMappingTable.setLinesVisible(true);
		eventMappingTable.setHeaderVisible(true);
		//eventMappingTable.setEnabled(false);
		eventMappingTab.setControl(eventMappingTable);


		TableColumn tc0 = new TableColumn(eventMappingTable,SWT.NONE); 
		tc0.setWidth(50);
		tc0.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable1")); //$NON-NLS-1$


		TableColumn tc2 = new TableColumn(eventMappingTable,SWT.NONE); 
		tc2.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable2"));  //$NON-NLS-1$
		tc2.setWidth(100);

		TableColumn tc3 = new TableColumn(eventMappingTable,SWT.NONE); 
		tc3.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable3"));  //$NON-NLS-1$
		tc3.setWidth(100);

		TableColumn newColumnTableColumn_20 = new TableColumn(eventMappingTable, SWT.NONE);
		newColumnTableColumn_20.setWidth(100);
		newColumnTableColumn_20.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable4")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_19 = new TableColumn(eventMappingTable, SWT.NONE);
		newColumnTableColumn_19.setWidth(100);
		newColumnTableColumn_19.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable5")); //$NON-NLS-1$


		TableColumn newColumnTableColumn_18 = new TableColumn(eventMappingTable, SWT.NONE);
		newColumnTableColumn_18.setWidth(100);
		newColumnTableColumn_18.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable6")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_17 = new TableColumn(eventMappingTable, SWT.NONE);
		newColumnTableColumn_17.setWidth(100);
		newColumnTableColumn_17.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable7"));		 //$NON-NLS-1$

		//MRN Mapping
		mrnMappingTable = new Table(tabFolder_1, SWT.NONE);
		mrnMappingTable.setLinesVisible(true);
		mrnMappingTable.setHeaderVisible(true);
		//mrnMappingTable.setEnabled(false);
		mrnMappingTab.setControl(mrnMappingTable);


		tc0 = new TableColumn(mrnMappingTable,SWT.NONE); 
		tc0.setWidth(50);
		tc0.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable1")); //$NON-NLS-1$


		tc2 = new TableColumn(mrnMappingTable,SWT.NONE); 
		tc2.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable2"));  //$NON-NLS-1$
		tc2.setWidth(100);

		tc3 = new TableColumn(mrnMappingTable,SWT.NONE); 
		tc3.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable3"));  //$NON-NLS-1$
		tc3.setWidth(100);

		newColumnTableColumn_20 = new TableColumn(mrnMappingTable, SWT.NONE);
		newColumnTableColumn_20.setWidth(100);
		newColumnTableColumn_20.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable4")); //$NON-NLS-1$

		newColumnTableColumn_19 = new TableColumn(mrnMappingTable, SWT.NONE);
		newColumnTableColumn_19.setWidth(100);
		newColumnTableColumn_19.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable5")); //$NON-NLS-1$


		newColumnTableColumn_18 = new TableColumn(mrnMappingTable, SWT.NONE);
		newColumnTableColumn_18.setWidth(100);
		newColumnTableColumn_18.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable6")); //$NON-NLS-1$

		newColumnTableColumn_17 = new TableColumn(mrnMappingTable, SWT.NONE);
		newColumnTableColumn_17.setWidth(100);
		newColumnTableColumn_17.setText(Messages.getString("ClientFolderView.Step1MRNMappingTable7"));		 //$NON-NLS-1$

		//Observation Mapping
		obserFactTable = new Table(tabFolder_1, SWT.NONE);
		obserFactTable.setLinesVisible(true);
		obserFactTable.setHeaderVisible(true);
		//obserFactTable.setEnabled(false);
		obserFactTab.setControl(obserFactTable);

		tc0 = new TableColumn(obserFactTable,SWT.NONE); 
		tc0.setWidth(50);
		tc0.setText(Messages.getString("ClientFolderView.Step1ObserFactTable1")); //$NON-NLS-1$

		TableColumn tc1 = new TableColumn(obserFactTable,SWT.NONE); 
		tc1.setText(Messages.getString("ClientFolderView.Step1ObserFactTable2"));  //$NON-NLS-1$
		tc1.setWidth(100);

		tc2 = new TableColumn(obserFactTable,SWT.NONE); 
		tc2.setText(Messages.getString("ClientFolderView.Step1ObserFactTable3"));  //$NON-NLS-1$
		tc2.setWidth(100);

		newColumnTableColumn_20 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_20.setWidth(100);
		newColumnTableColumn_20.setText(Messages.getString("ClientFolderView.Step1ObserFactTable4")); //$NON-NLS-1$

		newColumnTableColumn_19 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_19.setWidth(100);
		newColumnTableColumn_19.setText(Messages.getString("ClientFolderView.Step1ObserFactTable5")); //$NON-NLS-1$


		newColumnTableColumn_18 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_18.setWidth(100);
		newColumnTableColumn_18.setText(Messages.getString("ClientFolderView.Step1ObserFactTable6")); //$NON-NLS-1$

		newColumnTableColumn_17 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_17.setWidth(100);
		newColumnTableColumn_17.setText(Messages.getString("ClientFolderView.Step1ObserFactTable7")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_16 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_16.setWidth(100);
		newColumnTableColumn_16.setText(Messages.getString("ClientFolderView.Step1ObserFactTable8")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_7 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_7.setWidth(100);
		newColumnTableColumn_7.setText(Messages.getString("ClientFolderView.Step1ObserFactTable9")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_10 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_10.setWidth(100);
		newColumnTableColumn_10.setText(Messages.getString("ClientFolderView.Step1ObserFactTable10")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_9 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_9.setWidth(100);
		newColumnTableColumn_9.setText(Messages.getString("ClientFolderView.Step1ObserFactTable11")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_8 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_8.setWidth(100);
		newColumnTableColumn_8.setText(Messages.getString("ClientFolderView.Step1ObserFactTable12")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_21 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_21.setWidth(100);
		newColumnTableColumn_21.setText(Messages.getString("ClientFolderView.Step1ObserFactTable13")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_22 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_22.setWidth(100);
		newColumnTableColumn_22.setText(Messages.getString("ClientFolderView.Step1ObserFactTable14")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_23 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_23.setWidth(100);
		newColumnTableColumn_23.setText(Messages.getString("ClientFolderView.Step1ObserFactTable15")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_24 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_24.setWidth(100);
		newColumnTableColumn_24.setText(Messages.getString("ClientFolderView.Step1ObserFactTable16")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_25 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_25.setWidth(100);
		newColumnTableColumn_25.setText(Messages.getString("ClientFolderView.Step1ObserFactTable17")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_26 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_26.setWidth(100);
		newColumnTableColumn_26.setText(Messages.getString("ClientFolderView.Step1ObserFactTable18")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_27 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_27.setWidth(100);
		newColumnTableColumn_27.setText(Messages.getString("ClientFolderView.Step1ObserFactTable19")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_28 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_28.setWidth(100);
		newColumnTableColumn_28.setText(Messages.getString("ClientFolderView.Step1ObserFactTable20")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_29 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_29.setWidth(100);
		newColumnTableColumn_29.setText(Messages.getString("ClientFolderView.Step1ObserFactTable21")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_30 = new TableColumn(obserFactTable, SWT.NONE);
		newColumnTableColumn_30.setWidth(100);
		newColumnTableColumn_30.setText(Messages.getString("ClientFolderView.Step1ObserFactTable22")); //$NON-NLS-1$



		//Provider Dimension Mapping
		/*
		final Table providerDimTable = new Table(tabFolder_1, SWT.NONE);
		providerDimTable.setLinesVisible(true);
		providerDimTable.setHeaderVisible(true);
		providerDimTable.setEnabled(false);
		providerDimTab.setControl(providerDimTable);

		TableColumn status = new TableColumn(providerDimTable,SWT.NONE); 
		status.setWidth(50);
		status.setText(Messages.getString("ClientFolderView.Step1ProviderTable1")); //$NON-NLS-1$


		tc0 = new TableColumn(providerDimTable,SWT.NONE); 
		tc0.setWidth(50);
		tc0.setText(Messages.getString("ClientFolderView.Step1ProviderTable2")); //$NON-NLS-1$

		tc1 = new TableColumn(providerDimTable,SWT.NONE); 
		tc1.setText(Messages.getString("ClientFolderView.Step1ProviderTable3"));  //$NON-NLS-1$
		tc1.setWidth(100);

		tc2 = new TableColumn(providerDimTable,SWT.NONE); 
		tc2.setText(Messages.getString("ClientFolderView.Step1ProviderTable4"));  //$NON-NLS-1$
		tc2.setWidth(100);

		tc3 = new TableColumn(providerDimTable,SWT.NONE); 
		tc3.setText(Messages.getString("ClientFolderView.Step1ProviderTable5"));  //$NON-NLS-1$
		tc3.setWidth(100);

		newColumnTableColumn_27 = new TableColumn(providerDimTable, SWT.NONE);
		newColumnTableColumn_27.setWidth(100);
		newColumnTableColumn_27.setText(Messages.getString("ClientFolderView.Step1ProviderTable6")); //$NON-NLS-1$

		newColumnTableColumn_28 = new TableColumn(providerDimTable, SWT.NONE);
		newColumnTableColumn_28.setWidth(100);
		newColumnTableColumn_28.setText(Messages.getString("ClientFolderView.Step1ProviderTable7")); //$NON-NLS-1$

		newColumnTableColumn_29 = new TableColumn(providerDimTable, SWT.NONE);
		newColumnTableColumn_29.setWidth(100);
		newColumnTableColumn_29.setText(Messages.getString("ClientFolderView.Step1ProviderTable8")); //$NON-NLS-1$

		newColumnTableColumn_30 = new TableColumn(providerDimTable, SWT.NONE);
		newColumnTableColumn_30.setWidth(100);
		newColumnTableColumn_30.setText(Messages.getString("ClientFolderView.Step1ProviderTable9")); //$NON-NLS-1$
		 */

		//Provider Dimension Mapping
		final Table visitDimTable = new Table(tabFolder_1, SWT.NONE);
		visitDimTable.setLinesVisible(true);
		visitDimTable.setHeaderVisible(true);
		//visitDimTable.setEnabled(false);
		//mm turned off
		//visitDimTab.setControl(visitDimTable);

		TableColumn status = new TableColumn(visitDimTable,SWT.NONE); 
		status.setWidth(50);
		status.setText(Messages.getString("ClientFolderView.Step1VisitTable1")); //$NON-NLS-1$

		tc0 = new TableColumn(visitDimTable,SWT.NONE); 
		tc0.setWidth(50);
		tc0.setText(Messages.getString("ClientFolderView.Step1VisitTable2")); //$NON-NLS-1$

		tc1 = new TableColumn(visitDimTable,SWT.NONE); 
		tc1.setText(Messages.getString("ClientFolderView.Step1VisitTable3"));  //$NON-NLS-1$
		tc1.setWidth(100);

		tc2 = new TableColumn(visitDimTable,SWT.NONE); 
		tc2.setText(Messages.getString("ClientFolderView.Step1VisitTable4"));  //$NON-NLS-1$
		tc2.setWidth(100);


		newColumnTableColumn_20 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_20.setWidth(100);
		newColumnTableColumn_20.setText(Messages.getString("ClientFolderView.Step1VisitTable5")); //$NON-NLS-1$

		newColumnTableColumn_19 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_19.setWidth(100);
		newColumnTableColumn_19.setText(Messages.getString("ClientFolderView.Step1VisitTable6")); //$NON-NLS-1$


		newColumnTableColumn_18 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_18.setWidth(100);
		newColumnTableColumn_18.setText(Messages.getString("ClientFolderView.Step1VisitTable7")); //$NON-NLS-1$

		newColumnTableColumn_17 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_17.setWidth(100);
		newColumnTableColumn_17.setText(Messages.getString("ClientFolderView.Step1VisitTable8")); //$NON-NLS-1$

		newColumnTableColumn_16 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_16.setWidth(100);
		newColumnTableColumn_16.setText(Messages.getString("ClientFolderView.Step1VisitTable9")); //$NON-NLS-1$

		newColumnTableColumn_27 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_27.setWidth(100);
		newColumnTableColumn_27.setText(Messages.getString("ClientFolderView.Step1VisitTable10")); //$NON-NLS-1$

		newColumnTableColumn_28 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_28.setWidth(100);
		newColumnTableColumn_28.setText(Messages.getString("ClientFolderView.Step1VisitTable11")); //$NON-NLS-1$

		newColumnTableColumn_29 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_29.setWidth(100);
		newColumnTableColumn_29.setText(Messages.getString("ClientFolderView.Step1VisitTable12")); //$NON-NLS-1$

		newColumnTableColumn_30 = new TableColumn(visitDimTable, SWT.NONE);
		newColumnTableColumn_30.setWidth(100);
		newColumnTableColumn_30.setText(Messages.getString("ClientFolderView.Step1VisitTable13")); //$NON-NLS-1$


		// Patient Dim Mapping
		final Table patientDimTable = new Table(tabFolder_1, SWT.NONE);
		patientDimTable.setLinesVisible(true);
		patientDimTable.setHeaderVisible(true);
		//patientDimTable.setEnabled(false);
		//mm turned off 
		//patientDimTab.setControl(patientDimTable);

		tc0 = new TableColumn(patientDimTable,SWT.NONE); 
		tc0.setWidth(50);
		tc0.setText(Messages.getString("ClientFolderView.Step1PatientTable1")); //$NON-NLS-1$
		tc0.setAlignment(SWT.LEFT);

		tc1 = new TableColumn(patientDimTable,SWT.NONE); 
		tc1.setText(Messages.getString("ClientFolderView.Step1PatientTable2"));  //$NON-NLS-1$
		tc1.setWidth(100);

		tc2 = new TableColumn(patientDimTable,SWT.NONE); 
		tc2.setText(Messages.getString("ClientFolderView.Step1PatientTable3"));  //$NON-NLS-1$
		tc2.setWidth(100);


		newColumnTableColumn_20 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_20.setWidth(100);
		newColumnTableColumn_20.setText(Messages.getString("ClientFolderView.Step1PatientTable4")); //$NON-NLS-1$

		newColumnTableColumn_19 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_19.setWidth(100);
		newColumnTableColumn_19.setText(Messages.getString("ClientFolderView.Step1PatientTable5")); //$NON-NLS-1$


		newColumnTableColumn_18 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_18.setWidth(100);
		newColumnTableColumn_18.setText(Messages.getString("ClientFolderView.Step1PatientTable6")); //$NON-NLS-1$

		newColumnTableColumn_17 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_17.setWidth(100);
		newColumnTableColumn_17.setText(Messages.getString("ClientFolderView.Step1PatientTable7")); //$NON-NLS-1$

		newColumnTableColumn_16 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_16.setWidth(100);
		newColumnTableColumn_16.setText(Messages.getString("ClientFolderView.Step1PatientTable8")); //$NON-NLS-1$

		newColumnTableColumn_7 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_7.setWidth(100);
		newColumnTableColumn_7.setText(Messages.getString("ClientFolderView.Step1PatientTable9")); //$NON-NLS-1$

		newColumnTableColumn_10 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_10.setWidth(100);
		newColumnTableColumn_10.setText(Messages.getString("ClientFolderView.Step1PatientTable10")); //$NON-NLS-1$

		newColumnTableColumn_9 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_9.setWidth(100);
		newColumnTableColumn_9.setText(Messages.getString("ClientFolderView.Step1PatientTable11")); //$NON-NLS-1$

		newColumnTableColumn_8 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_8.setWidth(100);
		newColumnTableColumn_8.setText(Messages.getString("ClientFolderView.Step1PatientTable12")); //$NON-NLS-1$

		newColumnTableColumn_21 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_21.setWidth(100);
		newColumnTableColumn_21.setText(Messages.getString("ClientFolderView.Step1PatientTable13")); //$NON-NLS-1$

		newColumnTableColumn_22 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_22.setWidth(100);
		newColumnTableColumn_22.setText(Messages.getString("ClientFolderView.Step1PatientTable14")); //$NON-NLS-1$

		newColumnTableColumn_23 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_23.setWidth(100);
		newColumnTableColumn_23.setText(Messages.getString("ClientFolderView.Step1PatientTable15")); //$NON-NLS-1$

		newColumnTableColumn_24 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_24.setWidth(100);
		newColumnTableColumn_24.setText(Messages.getString("ClientFolderView.Step1PatientTable16")); //$NON-NLS-1$

		newColumnTableColumn_25 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_25.setWidth(100);
		newColumnTableColumn_25.setText(Messages.getString("ClientFolderView.Step1PatientTable17")); //$NON-NLS-1$

		newColumnTableColumn_26 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_26.setWidth(100);
		newColumnTableColumn_26.setText(Messages.getString("ClientFolderView.Step1PatientTable18")); //$NON-NLS-1$

		newColumnTableColumn_27 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_27.setWidth(100);
		newColumnTableColumn_27.setText(Messages.getString("ClientFolderView.Step1PatientTable19")); //$NON-NLS-1$

		newColumnTableColumn_28 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_28.setWidth(100);
		newColumnTableColumn_28.setText(Messages.getString("ClientFolderView.Step1PatientTable20")); //$NON-NLS-1$

		newColumnTableColumn_29 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_29.setWidth(100);
		newColumnTableColumn_29.setText(Messages.getString("ClientFolderView.Step1PatientTable21")); //$NON-NLS-1$

		newColumnTableColumn_30 = new TableColumn(patientDimTable, SWT.NONE);
		newColumnTableColumn_30.setWidth(100);
		newColumnTableColumn_30.setText(Messages.getString("ClientFolderView.Step1PatientTable22")); //$NON-NLS-1$


		patientDimTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.detail == SWT.CHECK)
				{

					boolean allclear = true;


					for (int i=0; i < patientDimTable.getItemCount(); i++)
					{
						TableItem data = patientDimTable.getItem(i);
						if (data.getChecked() && data.getText() != null)
						{

							if (data.getText(0).toUpperCase().equals("ERROR")) //$NON-NLS-1$
								allclear = false;
						}
						//data.get
					}
					if (allclear)
					{
						startButton.setEnabled(true);
						//MM removed table_3.setEnabled(true);
					} else
					{
						startButton.setEnabled(false);
						//MM removed  table_3.setEnabled(false);						
					}
				}
			}
		});

		final Button button = new Button(step3Group, SWT.NONE);
		button.setText(Messages.getString("ClientFolderView.ButtonClear"));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				clearTable();
				startButton.setEnabled(false);
				progressBar.setSelection(0);
				uploadStatus.setText(""); 
			}
		});

		
		
		final Group group = new Group(composite_2, SWT.NONE);
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		group.setLayout(gridLayout_3);
		group.setFont(SWTResourceManager.getFont("", 10, SWT.BOLD)); //$NON-NLS-1$

		group.setText(Messages.getString("ClientFolderView.GroupStep3")); //$NON-NLS-1$

		final GridData gd_group = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_group.heightHint = 77;
		group.setLayoutData(gd_group);

		final Label uploadCsvFileLabel = new Label(group, SWT.NONE);
		uploadCsvFileLabel.setLayoutData(new GridData());
		uploadCsvFileLabel.setText(Messages.getString("ClientFolderView.Step1Text")); //$NON-NLS-1$
		new Label(group, SWT.NONE);

		progressBar = new ProgressBar(group, SWT.NONE);
		final GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true, false);
		progressBar.setLayoutData(gd_progressBar);
		new Label(group, SWT.NONE);

		uploadStatus = new Label(group, SWT.NONE);
		final GridData gd_uploadStatus = new GridData(SWT.FILL, SWT.CENTER, true, false);
		uploadStatus.setLayoutData(gd_uploadStatus);
		uploadStatus.setText("");

		startButton = new Button(group, SWT.NONE);
		startButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, true));
		startButton.setEnabled(false);
		startButton.setText(Messages.getString("ClientFolderView.Step2ButtonUpload")); //$NON-NLS-1$

		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				populateRunList(filename);
			}
		});



		//Import Status Tab
		final TabItem statusTabItem = new TabItem(tabFolder, SWT.NONE);
		statusTabItem.setText(Messages.getString("ClientFolderView.TabStatus")); //$NON-NLS-1$


		tabFolder.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (tabFolder.getSelectionIndex() == 1) {
					StatusWorker sWorker = new StatusWorker();
					sWorker.updateLoadStatusDataRequest(Display.getCurrent(), table_2);
				}
			}
		});


		final Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		composite_1.setLayout(new GridLayout());
		statusTabItem.setControl(composite_1);

		final Group step3Group_1 = new Group(composite_1, SWT.NONE);
		step3Group_1.setLayout(new GridLayout());
		step3Group_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		step3Group_1.setText(Messages.getString("ClientFolderView.GroupProcess")); //$NON-NLS-1$

		final TableViewer tableViewer_1 = new TableViewer(step3Group_1, SWT.BORDER);
		table_2 = tableViewer_1.getTable();
		table_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table_2.setLayout(new GridLayout());
		table_2.setLinesVisible(true);
		table_2.setHeaderVisible(true);

		final TableColumn newColumnTableColumn_11_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_11_1.setWidth(59);
		newColumnTableColumn_11_1.setText(Messages.getString("ClientFolderView.ProcessTableID")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_12_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_12_1.setWidth(100);
		newColumnTableColumn_12_1.setText(Messages.getString("ClientFolderView.ProcessTableStart")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_4 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_4.setWidth(71);
		newColumnTableColumn_4.setText(Messages.getString("ClientFolderView.ProcessTableStatus")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_14_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_14_1.setWidth(100);
		newColumnTableColumn_14_1.setText(Messages.getString("ClientFolderView.ProcessTableObsLoaded")); //$NON-NLS-1$

		TableColumn newColumnTableColumn_15_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_15_1.setWidth(100);
		newColumnTableColumn_15_1.setText(Messages.getString("ClientFolderView.ProcessTableObsValid")); //$NON-NLS-1$

		newColumnTableColumn_14_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_14_1.setWidth(100);
		newColumnTableColumn_14_1.setText(Messages.getString("ClientFolderView.ProcessTableEidLoaded")); //$NON-NLS-1$

		newColumnTableColumn_15_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_15_1.setWidth(100);
		newColumnTableColumn_15_1.setText(Messages.getString("ClientFolderView.ProcessTableEidValid")); //$NON-NLS-1$	

		newColumnTableColumn_14_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_14_1.setWidth(100);
		newColumnTableColumn_14_1.setText(Messages.getString("ClientFolderView.ProcessTablePidLoaded")); //$NON-NLS-1$

		newColumnTableColumn_15_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_15_1.setWidth(100);
		newColumnTableColumn_15_1.setText(Messages.getString("ClientFolderView.ProcessTablePidValid")); //$NON-NLS-1$		


		final TableColumn newColumnTableColumn_13_1 = new TableColumn(table_2, SWT.NONE);
		newColumnTableColumn_13_1.setWidth(52);
		newColumnTableColumn_13_1.setText(Messages.getString("ClientFolderView.ProcessTableEnd")); //$NON-NLS-1$

		final Button refreshButton = new Button(composite_1, SWT.NONE);
		refreshButton.setLayoutData(new GridData());
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				StatusWorker sWorker = new StatusWorker();
				sWorker.updateLoadStatusDataRequest(Display.getCurrent(), table_2);
			}
		});
		refreshButton.setText(Messages.getString("ClientFolderView.ProcessButtonRefresh")); //$NON-NLS-1$



		//Options Tab
		final TabItem optionTab = new TabItem(tabFolder, SWT.NONE);
		optionTab.setText(Messages.getString("ClientFolderView.Step1TabOption")); //$NON-NLS-1$



		final Group group_1 = new Group(tabFolder, SWT.NONE);
		final GridLayout gridLayout_4 = new GridLayout();
		gridLayout_4.numColumns = 1;
		group_1.setLayout(gridLayout_4);
		optionTab.setControl(group_1);

		encryptMrnButton = new Button(group_1, SWT.CHECK);
		encryptMrnButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (key == null || key.length() == 0)
					getKey();
				if (key == null || key.length() == 0)
					encryptMrnButton.setSelection(false);
			}
		});
		encryptMrnButton.setText(Messages.getString("ClientFolderView.Step1EncryptMRNs")); //$NON-NLS-1$

		encryptTextButton = new Button(group_1, SWT.CHECK);
		encryptTextButton.setText(Messages.getString("ClientFolderView.Step1EncryptTexts")); //$NON-NLS-1$
		encryptTextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (key == null || key.length() == 0)
					getKey();
				if (key == null || key.length() == 0)
					encryptTextButton.setSelection(false);
			}
		});
		encryptFileButton = new Button(group_1, SWT.CHECK);
		encryptFileButton.setText(Messages.getString("ClientFolderView.Step1EncryptFiles")); //$NON-NLS-1$
		encryptFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (key == null || key.length() == 0)
					getKey();
				if (key == null || key.length() == 0)
					encryptFileButton.setSelection(false);
			}
		});
		missingPatientNumberButton = new Button(group_1, SWT.CHECK);
		missingPatientNumberButton.setText(Messages.getString("ClientFolderView.Step1MissingPatientNumber")); //$NON-NLS-1$
		missingPatientNumberButton.setEnabled(false);

		missingVisitNumberButton = new Button(group_1, SWT.CHECK);
		missingVisitNumberButton.setText(Messages.getString("ClientFolderView.Step1MissingVisitNumber")); //$NON-NLS-1$
		new Label(group_1, SWT.NONE);

		final Label label_1 = new Label(group_1, SWT.NONE);
		label_1.setLayoutData(new GridData());
		label_1.setText(Messages.getString("ClientFolderView.Step1ProcessPerCall")); //$NON-NLS-1$

		numberPerBatch = new Text(group_1, SWT.BORDER);
		numberPerBatch.setText("10000");
		numberPerBatch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));





		//Direct File Access Tab
		if ((dirWorker != null) && (dirWorker.isOnline()))
		{
			final TabItem viewerTabItem = new TabItem(tabFolder, SWT.NONE);
			viewerTabItem.setText(Messages.getString("ClientFolderView.TabClient")); //$NON-NLS-1$

			final Composite composite = new Composite(tabFolder, SWT.NONE);
			composite.setLayout(new RowLayout());
			viewerTabItem.setControl(composite);

			final Group localSystemGroup = new Group(composite, SWT.NONE);
			final RowData rd_localSystemGroup = new RowData();
			rd_localSystemGroup.height = 242;
			localSystemGroup.setLayoutData(rd_localSystemGroup);
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.numColumns = 2;
			localSystemGroup.setLayout(gridLayout_1);
			localSystemGroup.setText(Messages.getString("ClientFolderView.GroupLocalSystem")); //$NON-NLS-1$

			localCombo = new Combo(localSystemGroup, SWT.NONE);
			final GridData gd_localCombo = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
			gd_localCombo.widthHint = 241;
			localCombo.setLayoutData(gd_localCombo);

			//final Table table = new Table(localSystemGroup, SWT.BORDER);
			directoryTable = new DirectoryTable(localSystemGroup, SWT.BORDER);
			directoryTable.getDirTable().addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					updateDirectoryList();
				}
			});
			directoryTable.getDirTable().setBounds(0, 0,209, 202);
			final GridData gd_directoryTable = new GridData(SWT.LEFT, SWT.TOP, false, false);
			gd_directoryTable.widthHint = 209;
			directoryTable.setLayoutData(gd_directoryTable);
			//table.setLayout(new FillLayout());
			Table table = directoryTable.getDirTable();
			final GridData localTable = new GridData(SWT.FILL, SWT.TOP, false, false);
			localTable.heightHint = 88;
			localTable.widthHint = 200;
			table.setLayoutData(localTable);
			//table.setLinesVisible(true);
			table.setHeaderVisible(true);

			final ToolBar toolBar = new ToolBar(localSystemGroup, SWT.VERTICAL);
			toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
			toolBar.setData(Messages.getString("ClientFolderView.12"), Messages.getString("ClientFolderView.13")); //$NON-NLS-1$ //$NON-NLS-2$
			toolBar.setData(Messages.getString("ClientFolderView.14"), Messages.getString("ClientFolderView.15")); //$NON-NLS-1$ //$NON-NLS-2$

			final ToolItem newItemToolItem = new ToolItem(toolBar, SWT.PUSH);
			newItemToolItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					InputDialog dlg = new InputDialog(getSite().getShell(),Messages.getString("ClientFolderView.NewFolderPopupTitle"),Messages.getString("ClientFolderView.NewFolderPopupText"),"Directory", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							new IInputValidator(){

						public String isValid(String newText) {
							if ( newText.length() > 0 )
								return null;
							else
								return Messages.getString("ClientFolderView.NewFolderInvalidName"); //$NON-NLS-1$
						}});
					dlg.open();
					ClientDirWorker.getClientDir().createDir(dlg.getValue());
				}
			});
			newItemToolItem.setText(Messages.getString("ClientFolderView.ButtonNewFolder")); //$NON-NLS-1$



			final ToolItem newItemToolItem_1 = new ToolItem(toolBar, SWT.PUSH);
			newItemToolItem_1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ClientDirWorker.getClientDir().deleteDirs( directoryTable.getSelectionFiles()); //new String[]{
					updateDirectoryList();
				}
			});
			newItemToolItem_1.setText(Messages.getString("ClientFolderView.ButtonDelete")); //$NON-NLS-1$

			final ToolItem newItemToolItem_6 = new ToolItem(toolBar, SWT.PUSH);
			newItemToolItem_6.setText(Messages.getString("ClientFolderView.ButtonRename")); //$NON-NLS-1$
			newItemToolItem_6.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					if (directoryTable.getSelectionFiles().length ==0)
					{
					}
					else if (directoryTable.getSelectionFiles().length >1)
					{
						MessageBox messageBox =
							new MessageBox(getSite().getShell(),
									SWT.OK);
						messageBox.setMessage(Messages.getString("ClientFolderView.RenameErrorSelectedMultiple")); //$NON-NLS-1$
						messageBox.open();


					} else {

						InputDialog dlg = new InputDialog(getSite().getShell(),Messages.getString("ClientFolderView.RenamePopupTitle"),Messages.getString("ClientFolderView.RenamePopupText"),remoteTable.getSelectionFiles()[0], //$NON-NLS-1$ //$NON-NLS-2$
								new IInputValidator(){

							public String isValid(String newText) {
								if ( newText.length() > 0 )
									return null;
								else
									return Messages.getString("ClientFolderView.RenamePopupInvalidName"); //$NON-NLS-1$
							}});
						dlg.open();
						ClientDirWorker.getClientDir().rename(directoryTable.getSelectionFiles()[0], dlg.getValue());
					}
				}
			});
			final ToolItem newItemToolItem_8 = new ToolItem(toolBar, SWT.PUSH);
			newItemToolItem_8.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateDirectoryList();
				}
			});
			newItemToolItem_8.setText(Messages.getString("ClientFolderView.ButtonRefresh")); //$NON-NLS-1$

			final Composite transferComp = new Composite(composite, SWT.NONE);
			final RowData rd_transferComp = new RowData();
			rd_transferComp.height = 223;
			transferComp.setLayoutData(rd_transferComp);
			final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
			rowLayout.marginTop = 100;
			transferComp.setLayout(rowLayout);


			Button two = new Button(transferComp, SWT.NONE);
			two.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					upload();
				}
			});		
			two.setText("-->"); //$NON-NLS-1$
			Button three = new Button(transferComp, SWT.NONE);
			three.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					download();
				}
			});
			three.setText("<--"); //$NON-NLS-1$


			final Group remoteSystemGroup = new Group(composite, SWT.NONE);
			final RowData rd_remoteSystemGroup = new RowData();
			rd_remoteSystemGroup.height = 242;
			remoteSystemGroup.setLayoutData(rd_remoteSystemGroup);
			remoteSystemGroup.setText(Messages.getString("ClientFolderView.GroupRemoteSystem")); //$NON-NLS-1$
			final GridLayout gridLayout_2 = new GridLayout();
			gridLayout_2.numColumns = 2;
			remoteSystemGroup.setLayout(gridLayout_2);

			remoteCombo = new Combo(remoteSystemGroup, SWT.NONE);
			/*
				remoteCombo.addModifyListener(new ModifyListener() {
					public void modifyText(final ModifyEvent e) {

						//DirWorker ftp = dirWorker.getFtpDirWorker();
						//		if (!dirWorker.getCurrentDir().equals(remoteCombo.getText()))
						//			dirWorker.changeDir(remoteCombo.getText());
						//		updateDirectoryListRemote();

					}
				});
			 */

			final GridData gd_combo_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
			gd_combo_1.widthHint = 223;
			remoteCombo.setLayoutData(gd_combo_1);
			remoteTable = new SrbDirectoryTable(remoteSystemGroup, SWT.BORDER, dirWorker);
			remoteTable.getDirTable().setBounds(0, 0,210, 201);
			final GridData gd_directoryTables = new GridData(SWT.LEFT, SWT.TOP, false, false);
			gd_directoryTables.heightHint = 201;
			//gd_directoryTables.heightHint = 86;
			//gd_directoryTables.heightHint = 90;
			gd_directoryTables.widthHint = 209;
			remoteTable.setLayoutData(gd_directoryTables);
			//table.setLayout(new FillLayout());

			//Table rmtTable = remoteTable.getDirTable();
			remoteTable.getDirTable().addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					updateDirectoryListRemote();
				}
			});



			final ToolBar toolBar_1 = new ToolBar(remoteSystemGroup, SWT.VERTICAL);
			toolBar_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			final ToolItem newItemToolItem_3 = new ToolItem(toolBar_1, SWT.PUSH);
			newItemToolItem_3.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					InputDialog dlg = new InputDialog(getSite().getShell(),Messages.getString("ClientFolderView.NewFolderPopupTitle"),Messages.getString("ClientFolderView.NewFolderPopupText"),"Directory", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							new IInputValidator(){

						public String isValid(String newText) {
							if ( newText.length() > 0 )
								return null;
							else
								return Messages.getString("ClientFolderView.NewFolderInvalidName"); //$NON-NLS-1$
						}});
					dlg.open();

					dirWorker.createDir(remoteCombo.getText() + dirWorker.getPathChar() + dlg.getValue());
					updateDirectoryListRemote();
				}
			});
			newItemToolItem_3.setText(Messages.getString("ClientFolderView.ButtonNewFolder")); //$NON-NLS-1$

			final ToolItem newItemToolItem_4 = new ToolItem(toolBar_1, SWT.PUSH);
			newItemToolItem_4.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {

					dirWorker.deleteDirs( remoteTable.getSelectionFiles());
					updateDirectoryListRemote();
				}
			});
			newItemToolItem_4.setText(Messages.getString("ClientFolderView.ButtonDelete")); //$NON-NLS-1$

			final ToolItem newItemToolItem_7 = new ToolItem(toolBar_1, SWT.PUSH);
			newItemToolItem_7.setText(Messages.getString("ClientFolderView.ButtonRename")); //$NON-NLS-1$
			newItemToolItem_7.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					if (remoteTable.getSelectionFiles().length ==0)
					{
					}
					else if (remoteTable.getSelectionFiles().length >1)
					{
						MessageBox messageBox =
							new MessageBox(getSite().getShell(),
									SWT.OK);
						messageBox.setMessage(Messages.getString("ClientFolderView.RenameErrorSelectedMultiple")); //$NON-NLS-1$
						messageBox.open();


					} else {

						InputDialog dlg = new InputDialog(getSite().getShell(),Messages.getString("ClientFolderView.ButtonRename"),Messages.getString("ClientFolderView.NewFolderPopupText"),remoteTable.getSelectionFiles()[0], //$NON-NLS-1$ //$NON-NLS-2$
								new IInputValidator(){

							public String isValid(String newText) {
								if ( newText.length() > 0 )
									return null;
								else
									return Messages.getString("ClientFolderView.NewFolderInvalidName"); //$NON-NLS-1$
							}});
						dlg.open();
						dirWorker.rename(remoteTable.getSelectionFiles()[0], dlg.getValue());
					}
					updateDirectoryListRemote();

				}
			});


			final ToolItem newItemToolItem_9 = new ToolItem(toolBar_1, SWT.PUSH);
			newItemToolItem_9.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					updateDirectoryListRemote();
				}
			});
			newItemToolItem_9.setText(Messages.getString("ClientFolderView.ButtonRefresh")); //$NON-NLS-1$
			new Label(remoteSystemGroup, SWT.NONE);

			final Group logGroup = new Group(composite, SWT.NONE);
			logGroup.setText(Messages.getString("ClientFolderView.GroupLog")); //$NON-NLS-1$
			final RowData rd_logGroup = new RowData();
			rd_logGroup.width = 625;
			logGroup.setLayoutData(rd_logGroup);
			logGroup.setLayout(new FormLayout());

			txfStatusTable = new Table(logGroup, SWT.BORDER);
			final FormData fd_txfStatusTable = new FormData();
			fd_txfStatusTable.right = new FormAttachment(100, -5);
			fd_txfStatusTable.bottom = new FormAttachment(0, 101);
			fd_txfStatusTable.top = new FormAttachment(0, 3);
			fd_txfStatusTable.left = new FormAttachment(0, 1);
			txfStatusTable.setLayoutData(fd_txfStatusTable);
			txfStatusTable.setLinesVisible(true);
			txfStatusTable.setHeaderVisible(true);


			final TableColumn newColumnTableColumn = new TableColumn(txfStatusTable, SWT.NONE);
			newColumnTableColumn.setWidth(324);
			newColumnTableColumn.setText(Messages.getString("ClientFolderView.LogTableSource")); //$NON-NLS-1$

			final TableColumn newColumnTableColumn_1 = new TableColumn(txfStatusTable, SWT.NONE);
			newColumnTableColumn_1.setWidth(61);
			newColumnTableColumn_1.setText(Messages.getString("ClientFolderView.LogTableStatus")); //$NON-NLS-1$

			final TableColumn newColumnTableColumn_2 = new TableColumn(txfStatusTable, SWT.NONE);
			newColumnTableColumn_2.setWidth(65);
			newColumnTableColumn_2.setText(Messages.getString("ClientFolderView.LogOperation")); //$NON-NLS-1$

			final TableColumn newColumnTableColumn_5 = new TableColumn(txfStatusTable, SWT.NONE);
			newColumnTableColumn_5.setWidth(62);
			newColumnTableColumn_5.setText(Messages.getString("ClientFolderView.LogTableSize")); //$NON-NLS-1$

			final TableColumn newColumnTableColumn_3 = new TableColumn(txfStatusTable, SWT.NONE);
			newColumnTableColumn_3.setWidth(100);
			newColumnTableColumn_3.setText(Messages.getString("ClientFolderView.LogElaspedTime")); //$NON-NLS-1$

			updateDirectoryListRemote();
			updateDirectoryList();
			initializeToolBar();
		}
		// Setup help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IMPORTDATA_VIEW_CONTEXT_ID);
		addHelpButtonToToolBar();
	}
	//}

	//}

	//add help button
	private void addHelpButtonToToolBar() {
		final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
		Action helpAction = new Action(){
			public void run() {
				helpSystem.displayHelpResource("/net.nbirn.srbclient.plugin/html/i2b2_import_index.htm");
		}
		};
		helpAction.setImageDescriptor(ImageDescriptor.createFromFile(ClientFolderView.class, "/icons/help.png"));
		getViewSite().getActionBars().getToolBarManager().add(helpAction);
	}

	private void clearTable()
	{
		//if (tabFolder_1.getSelectionIndex() == 0) //(text.startsWith("OBS"))
		//{
			obserFactTable.removeAll();
			if (filename.containsKey("OBS"))
				filename.remove("OBS");
			mrnMappingTable.removeAll();
			if (filename.containsKey("PTM"))
				filename.remove("PTM");
			eventMappingTable.removeAll();
			if (filename.containsKey("ETM"))
				filename.remove("ETM");
			xmltopdo= new XMLtoPDO();

	}
	private void populateFileList( String selected)
	{
		//	File theFile = new File(selected);
		//	if (!theFile.isFile())
		//		return;

		try {
			progressBar.setSelection(0);
			uploadStatus.setText(""); 

			LoaderWizard wizard = new LoaderWizard( selected );

			//WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			//dialog.setBlockOnOpen(true);

			Shell shell = new Shell(Display.getCurrent(), SWT.NONE);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			//dialog.setBlockOnOpen(true);
			int returnCode = dialog.open();
			if(returnCode == Window.OK)
			{

				startButton.setEnabled(false);
				batchWorker = wizard.getBatchWorker();
				//MM removed table_3.setEnabled(false);


				if (wizard.getFileType() != null) {
					Table table = null;
					if (wizard.getFileType().equals("OBS"))
					{
						tabFolder_1.setSelection(0);
						table = obserFactTable;
					}
					else if (wizard.getFileType().equals("PTM"))
					{
						tabFolder_1.setSelection(1);
						table = mrnMappingTable;
					}
					else if (wizard.getFileType().equals("ETM"))
					{
						tabFolder_1.setSelection(2);
						table = eventMappingTable;
					}

					FontData[] data = Display.getCurrent().getSystemFont().getFontData();
					FontData data0 = data[0];
					data0.setStyle(SWT.BOLD);
					//     Create a new font
					Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

					//table.setEnabled(true);
					table.removeAll();

					//MM removed fileLabel.setText(Messages.getString("ClientFolderView.164")+ selected); //$NON-NLS-1$

					StyleRange style1 = new StyleRange();
					style1.start = 6;
					//	style1.length = selected.length();
					style1.fontStyle = SWT.BOLD;
					//MM removed fileLabel.setStyleRange(style1);

					//batchWorker = wizard.getBatchWorker();
					//batchWorker.setFilename(selected);
					for(;table.getColumns().length>0;)
					{
						table.getColumns()[0].dispose();	
					}
					table.clearAll();
					batchWorker.loadFileDataMapping(table, true);

					if (filename.containsKey(wizard.getFileType()))
						filename.remove(wizard.getFileType());
					filename.put(wizard.getFileType(), batchWorker);


					xmltopdo.setPDO(null);

					if (batchWorker.isAllClear())
					{
						startButton.setEnabled(true);
						//MM removed table_3.setEnabled(true);
						//txtErrorsFound.setText("");
					} 
					step3Group.layout(true, true);
					tabFolder_1.layout(true, true);
					table.layout(true, true);
				}
			}
			else
			{
				log.debug(Messages.getString("ClientFolderView.165")); //$NON-NLS-1$
			}




		}catch (Exception e) {
			e.printStackTrace();
			//log.error(Messages.getString("ClientFolderView.ErrorReadingFile")+ selected); //$NON-NLS-1$
			return;

		}
	}






	protected void upload() 
	{
		String[] filesToUpload;
		if ( (filesToUpload = directoryTable.getSelectionFiles()).length == 0 )
		{
			MessageDialog.openError(getSite().getShell(),Messages.getString("ClientFolderView.UploadErrorPopupTitle"),Messages.getString("ClientFolderView.UploadErrorPopupText")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			//ProgressDialog dlg = new ProgressDialog(getSite().getShell());
			//SrbDirWorker.getFtpDirWorker().setMonitor(dlg.getProgressMonitor());
			//for (int i=0; i < filesToUpload.length; i++)
			//{
			//	File f = new File(File.separator + filesToUpload[i]);
			//SrbDirWorker.getFtpDirWorker().uploadFile(f, dlg.getProgressMonitor());
			//	}
			//ClientDirWorker.getClientDir().setMonitor(dlg.getProgressMonitor());
			ClientDirWorker.getClientDir().setTable(txfStatusTable);

			String currentRmtDir = remoteCombo.getText();
			try {
				ClientDirWorker.getClientDir().uploadFiles(dirWorker, null, filesToUpload);
			} catch (InterruptedException e) {
				log.error (e.getMessage());
			}
			//.downloadFiles(null,filesToDownLoad);
			//dlg.getProgressMonitor().done();
			remoteCombo.setText(currentRmtDir);
			updateDirectoryList();
			//updateDirectoryListRemote();

		}

	}

	protected void download()
	{

		String[] filesToDownLoad = remoteTable.getSelectionFiles();
		if ( filesToDownLoad.length == 0 )
		{
			MessageDialog.openError(getSite().getShell(),Messages.getString("ClientFolderView.DownloadErrorPopupTitle"),Messages.getString("ClientFolderView.DownloadErrorPopupText")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			//ProgressDialog dlg = new ProgressDialog(getSite().getShell());


			dirWorker.setTable(txfStatusTable);
			try {
				dirWorker.downloadFiles(null,remoteTable, txfStatusTable);
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			//dlg.getProgressMonitor().done();
			updateDirectoryList();
			updateDirectoryListRemote();

		}

	}


	protected void newFolder() {
		InputDialog dlg = new InputDialog(getSite().getShell(),
				Messages.getString("ClientFolderView.DialogTitleCreateNewDir"), //$NON-NLS-1$
				Messages.getString("ClientFolderView.NameOfDir"), Messages.getString("ClientFolderView.NameOfDirInTextField"), //$NON-NLS-1$ //$NON-NLS-2$
				new IInputValidator() {

			public String isValid(String newText) {
				if (newText.length() > 0)
					return null;
				else
					return Messages.getString("ClientFolderView.TextForValidatorNewDirDialog"); //$NON-NLS-1$
			}
		});
		dlg.open();
		dirWorker.createDir(dlg.getValue());


	}
	//setup context help
	public static final String PREFIX = "edu.harvard.i2b2.eclipse.plugins.srb";
	public static final String IMPORTDATA_VIEW_CONTEXT_ID = PREFIX + ".importdata_view_help_context";
	@Override
	public void setFocus() {
		compositeFile.setFocus();
	}

	//public void updateDirectoryList() {
	//	table.setRootDir(ClientDirWorker.getClientDir().getActDirectory());

	//}

	public int notifyRemovingNoEmtyDir(String name) {

		if (MessageDialog
				.openConfirm(
						getSite().getShell(),
						Messages.getString("ClientFolderView.DialogTitleConfirmDelete"), //$NON-NLS-1$
						Messages.getString("ClientFolderView.DialogTextDirIsNotEmpty_1") //$NON-NLS-1$
						+ name
						+ Messages.getString("ClientFolderView.DialogTextDirIsNotEmpty_2")) == true) { //$NON-NLS-1$
			return REMOVE_DIR;
		}
		return DO_NOT_REMOVE_DIR;
	}

	public int notifyOveridingExistingDir(String name) {
		if ((MessageDialog.openConfirm(getSite().getShell(),Messages.getString("ClientFolderView.DialogTitleDirExists"),Messages.getString("ClientFolderView.QuestionIsThisDirOveride_1") + name + Messages.getString("ClientFolderView.QuestionIsThisDirOveride_2"))) == true) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			return OVERRIDE_DIR;
		}
		return DO_NOT_OVERRIDE_DIR;
	}

	public int notifyOverridingExistingFile(String name) {

		if ((MessageDialog.openConfirm(getSite().getShell(),Messages.getString("ClientFolderView.DialogTitleFileExist"),Messages.getString("ClientFolderView.IsThisFileOveridable_1") + name + Messages.getString("ClientFolderView.IsThisFileOverideAble_2"))) == true) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			return OVERRIDE_FILE;
		}
		return DO_NOT_OVERRIDE_FILE;
	}

	public void windowActivated(IWorkbenchWindow window) {
	}

	public void windowClosed(IWorkbenchWindow window) {
	}

	public void windowDeactivated(IWorkbenchWindow window) {
	}

	public void windowOpened(IWorkbenchWindow window) {
	}



	protected void newFtp() {
		NewSrbConnectionWizard wizard = new NewSrbConnectionWizard();
		wizard.init(PlatformUI.getWorkbench());
		NewSrbConnectionwizardDlg dlg = new NewSrbConnectionwizardDlg(getSite().getShell(),wizard);
		dlg.open();

	}
	protected void connectFtp() {
		try {
			dirWorker.setUser(UserInfoBean.getInstance().getUserName());
			dirWorker.setPass(UserInfoBean.getInstance().getUserPassword());
			dirWorker.setHost(UserInfoBean.getInstance().getSelectedProjectParam("FRHost")); //$NON-NLS-1$
			dirWorker.setHomeDirectory(UserInfoBean.getInstance().getSelectedProjectParam("FRHomeDirectory")); //$NON-NLS-1$
			dirWorker.setDefaultStorageResource(UserInfoBean.getInstance().getSelectedProjectParam("SRBDefaultStorageResource")); //$NON-NLS-1$
			dirWorker.setMdasDomainHome(UserInfoBean.getInstance().getSelectedProjectParam("SRBMdasDomainHome")); //$NON-NLS-1$
			dirWorker.setPort(UserInfoBean.getInstance().getSelectedProjectParam("FRPort")); //$NON-NLS-1$
			dirWorker.connect();	
		}
		catch (Exception e)
		{
			log.error(e.getMessage());

		}

	}

	public void updateDirectoryListRemote() {


		//IrodsDirWorker ftp = IrodsDirWorker.getFtpDirWorker();
		Object[] list = dirWorker.dir();
		String pwd = dirWorker.getCurrentDir();

		//ileInfo[] fl = dirWorker.dirFileInfo();
		/*if (remoteCombo.indexOf(pwd)> 0)
		{
			remoteCombo.remove(pwd);
		}*/
		//if (remoteCombo.indexOf(pwd) == -1)
		//{
		if (pwd != null) {
			try {
				remoteCombo.add(pwd); //list[0].getAbsolutePath());
				//remoteCombo.setText(pwd);
				if (remoteCombo.getItemCount() > 50)
					remoteCombo.remove(50);
			} catch (Exception e)
			{
				log.error(e.getMessage());
			}
			remoteCombo.setText(pwd); //list[0].getAbsolutePath());
		}
		//}

		//	else
		//{
		//	remoteCombo.remove(ftp.getCurrentDir());
		//}
		remoteTable.updateTable(list);

	}

	public void updateDirectoryList() {

		localCombo.add(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath());
		if (localCombo.getItemCount() > 50)
			localCombo.remove(50);
		localCombo.setText(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath());
		directoryTable.setRootDir(ClientDirWorker.getClientDir().getActDirectory());
	}
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
	}

	public void populatePDO(String pdo)
	{
		progressBar.setSelection(0);

		xmltopdo.setPDO(pdo);
		if (xmltopdo.updateTableObservationSet(obserFactTable, xmltopdo.getPDO().getObservationSet())) 
		{
			//obserFactTable.setEnabled(true);
			startButton.setEnabled(true);
		}
		if (xmltopdo.updateTablePidSet(mrnMappingTable,  xmltopdo.getPDO().getPidSet())) 
		{
			//mrnMappingTable.setEnabled(true);
			startButton.setEnabled(true);
		}
		if (xmltopdo.updateTableEidSet(eventMappingTable,  xmltopdo.getPDO().getEidSet())) 
		{
			//eventMappingTable.setEnabled(true);
			startButton.setEnabled(true);
		}
	}
	public File writeTempFile(String pdo)
	{
		try {
			// Create temp file.
			File temp = File.createTempFile("temp", ".xml");

			// Delete temp file when program exits.
			temp.deleteOnExit();

			// Write to temp file
			BufferedWriter out = new BufferedWriter(new FileWriter(temp));
			out.write(pdo);
			out.close();
			return temp;
		} catch (IOException e) {
			log.error(e);
		}
		return null;

	}
	public  String loadFile(String fileName, boolean onlyoneline)
	{
		if ((fileName == null) || (fileName == ""))
			throw new IllegalArgumentException();

		String line ;
		StringBuffer sb = new StringBuffer();
		try
		{    
			BufferedReader in = new BufferedReader(new FileReader(fileName));

			if (!in.ready())
				throw new IOException();

			while ((line = in.readLine()) != null)
			{
				sb.append(line);
				if (onlyoneline)
					break;

			}

			in.close();
		}
		catch (IOException e)
		{
			log.equals(e);
			return null;
		}

		return sb.toString();
	}
	/*

	public static Thread populateRunList(Button button, final Hashtable impFileList,
			final ProgressBar progressBar, final Label uploadStatus, final DirWorker dirWorker, 
			final String key, final boolean  encryptMrnButton,final  boolean encryptTextButton,final boolean encryptFileButton
	) {
		final Button theButton = button;
		return new Thread() {
			public void run() {

				try {

					//			if (encryptMrnButton.)
					// If checked then execute this code
					CreatePDO createpdo = new CreatePDO();



					//String results;
					createpdo.process(impFileList, progressBar, uploadStatus, dirWorker, key, 
							encryptMrnButton.getSelection(),
							encryptTextButton.getSelection(), encryptFileButton.getSelection());


					//table.pack();


				}catch (Exception e) {
					MessageBox mBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
					mBox.setText("Please Note ...");
					mBox.setMessage("Server reports: " + e.getMessage());
					mBox.open();
				}

				display.asyncExec(new Runnable() {
					public void run() {
						theButton.setText("done");
					}
				});
			}
		};
	}

	 */


	private void populateRunList( Hashtable impFileList)
	{
		try {
			uploadStatus.setText(Messages.getString("ClientFolderView.UploadStatusStarted"));

			//			if (encryptMrnButton.)
			// If checked then execute this code
			CreatePDO createpdo = new CreatePDO();

			//Create Tempfile
			if (xmltopdo.getPDO() != null)
				filename.put("PDO", writeTempFile(xmltopdo.toString(key, encryptMrnButton.getSelection(),
						encryptTextButton.getSelection(), missingPatientNumberButton.getSelection(), missingVisitNumberButton.getSelection())));



			//String results;
			int numPerBatch = Integer.parseInt(numberPerBatch.getText());
			createpdo.process(impFileList, startButton, progressBar, uploadStatus, dirWorker, key, numPerBatch, encryptMrnButton.getSelection(),
					encryptTextButton.getSelection(), encryptFileButton.getSelection(), missingPatientNumberButton.getSelection(), missingVisitNumberButton.getSelection());


			//table.pack();

			//uploadStatus.setText(Messages.getString("ClientFolderView.UploadStatusFinished"));

		}catch (Exception e) {
			MessageBox mBox = new MessageBox(getSite().getShell(), SWT.ICON_INFORMATION | SWT.OK);
			mBox.setText("Please Note ...");
			mBox.setMessage("Server reports: " + e.getMessage());
			mBox.open();
			uploadStatus.setText(Messages.getString("ClientFolderView.UploadStatusError"));
		}
		progressBar.setSelection(progressBar.getMaximum());
	}


}
