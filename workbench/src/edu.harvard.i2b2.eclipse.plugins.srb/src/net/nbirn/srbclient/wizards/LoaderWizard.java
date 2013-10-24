package net.nbirn.srbclient.wizards;

import java.io.File;

import net.nbirn.srbclient.data.BatchWorker;
import net.nbirn.srbclient.utils.Messages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.layout.RowData;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

/**
 * 
 */


class LoaderData {

	boolean delimited = true;
	int startimport = 0;
	char delimiters;
	String eolQualifer;
	char textQualifer;
	String[] locations = null;
}

public class LoaderWizard extends Wizard {
	private static final Log log = LogFactory.getLog(LoaderWizard.class);

	static final Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	// the model object. 
	public static LoaderData lData = new LoaderData();
	static  String file;
	static String fileType;
	private BatchWorker batchWorker = new BatchWorker();

	public String getFileType()
	{
		return fileType;
	}
	public BatchWorker getBatchWorker()
	{
		return batchWorker;
	}
	public LoaderWizard(String lFile) {
		file = lFile;
		setWindowTitle(Messages.getString("LoaderWizard.WindowTitle")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
		//setDefaultPageImageDescriptor(ImageDescriptor.createFromFile(null, "icons/import.gif")); //$NON-NLS-1$

		DialogSettings dialogSettings = new DialogSettings("userInfo"); //$NON-NLS-1$

		setDialogSettings(dialogSettings);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(new FrontPage());
		addPage(new DataTypePage());
		addPage(new DelimiterInfoPage());
		addPage(new DataMappingPage());
		addPage(new VerifyDataPage());

	}


	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof FrontPage) {
			FrontPage frontPage = (FrontPage) page;
			frontPage.getFileType();
		}

		//WizardPage nextPage = super.getNextPage(page);
		try {
			if (page instanceof DataTypePage) {
				if (file == null)
				{
					FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
					fd.setText(Messages.getString("ClientFolderView.Step1ButtonOpenText")); //$NON-NLS-1$
					String[] filterExt = { "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					fd.setFilterExtensions(filterExt);
					file = fd.open();
				}
				DataTypePage dataType = (DataTypePage) page;
				try {
					lData.startimport = Integer.parseInt(dataType.getStartImport());


				} catch (Exception e)
				{

				}
			}
			if (page instanceof DataMappingPage) {
				//VerifyDataPage dataType = (VerifyDataPage) page;
				//lData.textQualifer = dataType.getTextQualifer();
				//lData.delimiters = dataType.getDelimiters();


				if (lData.delimiters != '\0') {
					DataMappingPage dmPage = (DataMappingPage) getPage(DataMappingPage.PAGE_NAME);

					try {
						VerifyDataPage vdPage = (VerifyDataPage) getPage(VerifyDataPage.PAGE_NAME);
						//Table table = dmPage.getTable(false);
						//TableItem titems = table.getItem(0);

						if (dmPage.getBatchWorker() == null)
						{
							dmPage.setBatchWorker(batchWorker);
							dmPage.hideTable();
						} else
						{
							dmPage.showTable();
							batchWorker = dmPage.getBatchWorker();
							batchWorker.setStartAt(lData.startimport);
							batchWorker.setFileType(fileType);
							batchWorker.setDelimiter(lData.delimiters);
							batchWorker.setEOLQualifer(lData.eolQualifer);
							if (lData.textQualifer != '\0') 
								batchWorker.setTextQualifer(lData.textQualifer);
							batchWorker.setFilename(file);
							dmPage.setBatchWorker(batchWorker);
							
							if (batchWorker.getValidHeadersDuplicate().size() > 0 ||
									batchWorker.getValidHeadersPK().size() > 0)
							{
								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < batchWorker.getValidHeadersPK().size(); i++) {
									if (i == 0)
										sb.append(Messages.getString("LoaderWizard.ErrorMissingPrimaryHeader")); //$NON-NLS-1$
									sb.append("\t"); //$NON-NLS-1$
									sb.append(batchWorker.getValidHeadersPK().get(i));
									sb.append("\n"); //$NON-NLS-1$
								}
								if (sb.length() > 0)
									sb.append("\n");
								for (int i = 0; i < batchWorker.getValidHeadersDuplicate().size(); i++) {
									if (i == 0)
										sb.append(Messages.getString("LoaderWizard.ErrorDuplicateHeaders")); //$NON-NLS-1$
									sb.append("\t"); //$NON-NLS-1$
									sb.append(batchWorker.getValidHeadersDuplicate().get(i));
									sb.append("\n"); //$NON-NLS-1$
								}

								MessageDialog.openError(getShell(), Messages.getString("LoaderWizard.ErrorMissingMapping"), sb.toString()); //$NON-NLS-1$
								return page;
							}
							

							batchWorker.loadFileDataMapping(vdPage.getTable(true), true);
							vdPage.refreshTable();
						}
						switch (dmPage.getFileType())
						{
						case 1: fileType = "OBS"; break;
						case 2: fileType = "PTM"; break;
						case 3: fileType = "ETM"; break;
						}


						/*
					lData.locations = new String[table.getColumnCount()];
					for (int a =0; a < table.getColumnCount(); a++)
					{
						lData.locations[a] = titems.getText(a);
					}

					batchWorker.setStartAt(lData.startimport);
					batchWorker.setFileType(fileType);
					batchWorker.setDelimiter(lData.delimiters);
					batchWorker.setTextQualifer(lData.textQualifer);
					if (batchWorker.getHeaders().length != lData.locations.length)
						batchWorker.setHeaders(lData.locations);
					batchWorker.setFilename(file);
						 */


						vdPage.setPageComplete(true);
					} catch (Exception e)
					{
						e.printStackTrace();
					}


					//BatchWorker batchWorker = new BatchWorker();
					///batchWorker.loadFileDataMapping(file, dmPage.getTable(), LoaderWizard.lData.startimport, fileType, LoaderWizard.lData.delimiters, LoaderWizard.lData.textQualifer);
				}
			}

			if (page instanceof DelimiterInfoPage) {
				DelimiterInfoPage dataType = (DelimiterInfoPage) page;
				lData.textQualifer = dataType.getTextQualifer();
				lData.delimiters = dataType.getDelimiters();
				lData.eolQualifer = dataType.getEOLQualifer();

//				if (lData.delimiters != '\0' && dataType.getIsDirty()) {
//					DataMappingPage dmPage = (DataMappingPage) getPage(DataMappingPage.PAGE_NAME);

					batchWorker.setStartAt(lData.startimport);
					batchWorker.setFileType(fileType);
					batchWorker.setDelimiter(lData.delimiters);
					batchWorker.setEOLQualifer(lData.eolQualifer);
					if (lData.textQualifer != '\0') 
						batchWorker.setTextQualifer(lData.textQualifer);
					batchWorker.setFilename(file);
//					batchWorker.loadFileDataMapping( dmPage.getTable(true), false);
	//				dmPage.refreshTable();
	//				dataType.setIsDirty(false);
		//		}
			}
		} catch (Exception e)
		{
			log.error(e);
		}
		return super.getNextPage(page);		


	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if(getDialogSettings() != null) {
		}

		try {
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		MessageBox mb = new MessageBox(getShell(),SWT.ICON_QUESTION|SWT.YES|SWT.NO);
		mb.setMessage(Messages.getString("LoaderWizard.CancelPopupText"));
		mb.setText(Messages.getString("LoaderWizard.CancelPopupTitle"));

		//boolean ans = MessageDialog.openConfirm(getShell(), Messages.getString("LoaderWizard.CancelPopupTitle"), Messages.getString("LoaderWizard.CancelPopupText")); //$NON-NLS-1$ //$NON-NLS-2$
		int ans =  mb.open();
		if(ans == SWT.YES)
			return true;
		else
			return false;
	}  
}

class DelimiterInfoPage extends WizardPage {

	public DelimiterInfoPage() {
		super("DelimiterInfo"); //$NON-NLS-1$
		setTitle(Messages.getString("LoaderWizard.Page3")); //$NON-NLS-1$
		//setDescription("Please enter your credit card details");
		setPageComplete(false);
	}
	private Combo combo;
	private boolean isDirty = true;
	private Text eolQualifier;
	private Button tabButton, semicolonButton, commaButton, spaceButton, pipeButton;
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 4;
		composite.setLayout(gridLayout_1);

		setControl(composite);

		final Label thisScreenLetsLabel = new Label(composite, SWT.NONE);
		thisScreenLetsLabel.setText(Messages.getString("LoaderWizard.Page3Text")); //$NON-NLS-1$
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		final Group delimitersGroup = new Group(composite, SWT.NONE);
		delimitersGroup.setLayoutData(new GridData());
		delimitersGroup.setText(Messages.getString("LoaderWizard.GroupDelimiters")); //$NON-NLS-1$
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 15;
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginTop = 5;
		gridLayout.marginRight = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginBottom = 5;
		gridLayout.numColumns = 3;
		delimitersGroup.setLayout(gridLayout);

		tabButton = new Button(delimitersGroup, SWT.RADIO);
		tabButton.setText(Messages.getString("LoaderWizard.DelimiterTab"));	 //$NON-NLS-1$
		tabButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (tabButton.getSelection() || semicolonButton.getSelection() || commaButton.getSelection()
						|| spaceButton.getSelection() || pipeButton.getSelection())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		});		

		semicolonButton = new Button(delimitersGroup, SWT.RADIO);
		semicolonButton.setText(Messages.getString("LoaderWizard.DelimiterSeliColon")); //$NON-NLS-1$
		semicolonButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				isDirty = true;
				if (tabButton.getSelection() || semicolonButton.getSelection() || commaButton.getSelection()
						|| spaceButton.getSelection() || pipeButton.getSelection())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		});
		commaButton = new Button(delimitersGroup, SWT.RADIO);
		commaButton.setText(Messages.getString("LoaderWizard.DelimterComma")); //$NON-NLS-1$
		commaButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				isDirty = true;
				if (tabButton.getSelection() || semicolonButton.getSelection() || commaButton.getSelection()
						|| spaceButton.getSelection() || pipeButton.getSelection())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		});
		spaceButton = new Button(delimitersGroup, SWT.RADIO);
		spaceButton.setText(Messages.getString("LoaderWizard.DelimiterSpace")); //$NON-NLS-1$
		spaceButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				isDirty = true;
				if (tabButton.getSelection() || semicolonButton.getSelection() || commaButton.getSelection()
						|| spaceButton.getSelection() || pipeButton.getSelection())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		});
		pipeButton = new Button(delimitersGroup, SWT.RADIO);
		pipeButton.setText(Messages.getString("LoaderWizard.DelimiterPipe")); //$NON-NLS-1$
		pipeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				isDirty = true;
				if (tabButton.getSelection() || semicolonButton.getSelection() || commaButton.getSelection()
						|| spaceButton.getSelection() || pipeButton.getSelection())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		});	

		new Label(delimitersGroup, SWT.NONE);
		new Label(composite, SWT.NONE);

		final Label textQualifiersLabel = new Label(composite, SWT.NONE);
		textQualifiersLabel.setLayoutData(new GridData());
		textQualifiersLabel.setText(Messages.getString("LoaderWizard.DelimiterTextQualifier")); //$NON-NLS-1$


		combo = new Combo(composite, SWT.READ_ONLY);
		combo.setLayoutData(new GridData());
		combo.add("{none}"); //$NON-NLS-1$
		combo.add("\""); //$NON-NLS-1$
		combo.add("'"); //$NON-NLS-1$
		combo.pack();
		new Label(composite, SWT.NONE);

		final Label textEOLQualifiersLabel = new Label(composite, SWT.NONE);
		textEOLQualifiersLabel.setLayoutData(new GridData());
		textEOLQualifiersLabel.setText(Messages.getString("LoaderWizard.DelimiterEOLQualifier")); //$NON-NLS-1$

		eolQualifier = new Text(composite, SWT.BORDER);
		eolQualifier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	}
	public String getEOLQualifer()
	{
		return eolQualifier.getText().replaceAll("<CR>","\n");
	}
	public char getTextQualifer() {
		if ((combo.getSelectionIndex() == -1 || combo.getSelectionIndex() == 0))
			return '\0';
		else
			return combo.getItem(combo.getSelectionIndex()).charAt(0);
	}
	public boolean getIsDirty()
	{
		return isDirty;
	}
	public void setIsDirty(boolean dirty)
	{
		isDirty = dirty;
	}	
	public char getDelimiters() {
		//ArrayList list = new ArrayList();
		if (commaButton.getSelection())
			return ',';
		if (tabButton.getSelection())
			return '\t';
		if (semicolonButton.getSelection())
			return ';';
		if (spaceButton.getSelection())
			return ' ';
		if (pipeButton.getSelection())
			return '|';

		return '\0';
		//return  (String []) list.toArray (new String [0]);
	}

}

class VerifyDataPage extends WizardPage {
	public static final String PAGE_NAME = "VerifyInfo"; //$NON-NLS-1$
	public VerifyDataPage() {
		super(PAGE_NAME);
		setTitle(Messages.getString("LoaderWizard.Page5")); //$NON-NLS-1$
		//setDescription("Please enter your credit card details");
		setPageComplete(false);
	}
	public Group delimitersGroup;

	private Table table;
	public void refreshTable()
	{
		delimitersGroup.layout(true, true);
		//delimitersGroup.redraw();
		//table.redraw();
		table.layout(true, true);
	}
	public Table getTable(boolean newTable)
	{
		if (newTable)
		{
			table.dispose();

			table = new Table(delimitersGroup, SWT.SINGLE | SWT.BORDER |
					SWT.FULL_SELECTION);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
		}
		return table;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout_1 = new GridLayout(1, false);
		gridLayout_1.marginWidth = 15;
		gridLayout_1.marginTop = 5;
		gridLayout_1.marginRight = 5;
		gridLayout_1.marginLeft = 5;
		gridLayout_1.marginBottom = 5;
		composite.setLayout(gridLayout_1);

		setControl(composite);

		final Label thisScreenLetsLabel = new Label(composite, SWT.NONE);
		thisScreenLetsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		thisScreenLetsLabel.setText(Messages.getString("LoaderWizard.Page5Text1")); //$NON-NLS-1$

		final Label firstColumnIsLabel = new Label(composite, SWT.NONE);
		firstColumnIsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		firstColumnIsLabel.setText(Messages.getString("LoaderWizard.Page5Text2")); //$NON-NLS-1$

		final Label useTheSecondLabel = new Label(composite, SWT.NONE);
		useTheSecondLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		useTheSecondLabel.setText(Messages.getString("LoaderWizard.Page5Text3")); //$NON-NLS-1$

		delimitersGroup = new Group(composite, SWT.NONE);
		delimitersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		delimitersGroup.setText(Messages.getString("LoaderWizard.GroupReview")); //$NON-NLS-1$
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 15;
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginTop = 5;
		gridLayout.marginRight = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginBottom = 5;
		delimitersGroup.setLayout(gridLayout);



		table = new Table(delimitersGroup, SWT.BORDER | SWT.MULTI);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		//final GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		//table.setLayoutData(gd_table);



		/*
		int NUM = 10;
		// Create five columns
		TableColumn statusColumn = new TableColumn(table, SWT.CENTER);
		statusColumn.setText("Status");
		statusColumn.setWidth(50);
		statusColumn.pack();

		for (int ii = 1, n = LoaderWizard.lData.locations.length; ii < n; ii++) {
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(LoaderWizard.lData.locations[ii]);
			column.setWidth(100);
			column.pack();
		}	    

		for (int i = 0; i < NUM; i++) {
			final TableItem items = new TableItem(table, SWT.NONE);
			items.setText(0, "Error");
			items.setForeground(0,LoaderWizard.red);
			items.setText(1, "AAA");
			items.setText(2, "AAA");
			items.setText(3, "AAA");
			items.setChecked(true);
		}
		 */
	}
}

class DataMappingPage extends WizardPage {
	public static final String PAGE_NAME = "MappingInfo"; //$NON-NLS-1$

	private  Group delimitersGroup;
	private Table table;
	private Combo fileType;
	private BatchWorker batchWorker = null;
	public void showTable()
	{
		table.setVisible(true);
	}

	public void hideTable()
	{
		table.setVisible(false);
	}

	public void setBatchWorker(BatchWorker bw)
	{
		batchWorker = bw;
	}
	public BatchWorker getBatchWorker()
	{
		return batchWorker;
	}

	public int getFileType() {
		return  fileType.getSelectionIndex();
	}

	public DataMappingPage() {
		super(PAGE_NAME);
		setTitle(Messages.getString("LoaderWizard.Page4")); //$NON-NLS-1$
		setPageComplete(true);
	}
	public void refreshTable()
	{
		delimitersGroup.layout(true, true);
		//delimitersGroup.redraw();
		//table.redraw();
		table.layout(true, true);
	}
	public Table getTable(boolean newTable)
	{
		if (newTable)
		{
			table.dispose();

			table = new Table(delimitersGroup, SWT.SINGLE | SWT.BORDER |
					SWT.FULL_SELECTION);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			table.redraw();
		}
		table.redraw();

		return table;
	}
	public String getFileType(int i)
	{
		String ft = null;
		switch (i)
		{
		case 1: ft = "OBS"; break;
		case 2: ft = "PTM"; break;
		case 3: ft = "ETM"; break;
		}
		return ft;
	}
	private void populateFileList(String selected)
	{
		if (selected == null)
			return;
		File theFile = new File(selected);
		if (!theFile.isFile())
			return;
		try {
			FontData[] data = Display.getCurrent().getSystemFont().getFontData();
			FontData data0 = data[0];
			data0.setStyle(SWT.BOLD);
			//     Create a new font
			Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);


		}catch (Exception e) {
			e.printStackTrace();
			//log.error("Unable to read from csv file "+ selected);
			return;

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());

		setControl(composite);

		final Label thisScreenLetsLabel = new Label(composite, SWT.NONE);
		thisScreenLetsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		thisScreenLetsLabel.setText(Messages.getString("LoaderWizard.Page4Text1")); //$NON-NLS-1$

		final Label firstColumnIsLabel = new Label(composite, SWT.NONE);
		firstColumnIsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		firstColumnIsLabel.setText(Messages.getString("LoaderWizard.Page4Text2")); //$NON-NLS-1$

		final Label useTheSecondLabel = new Label(composite, SWT.NONE);
		useTheSecondLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		useTheSecondLabel.setText(Messages.getString("LoaderWizard.Page4Text3")); //$NON-NLS-1$

		final Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("LoaderWizard.FileTypeText")); //$NON-NLS-1$

		fileType = new Combo(composite, SWT.READ_ONLY);
		fileType.add(Messages.getString("LoaderWizard.FileTypeCombo0")); //$NON-NLS-1$
		fileType.add(Messages.getString("LoaderWizard.FileTypeCombo1")); //$NON-NLS-1$
		fileType.add(Messages.getString("LoaderWizard.FileTypeCombo2")); //$NON-NLS-1$
		fileType.add(Messages.getString("LoaderWizard.FileTypeCombo3")); //$NON-NLS-1$
		fileType.pack();		
		fileType.select(0);
		fileType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					if (fileType.getSelectionIndex() == 0)
					{
						hideTable();
					} else
					{
						showTable();
					batchWorker.setFileType(getFileType(fileType.getSelectionIndex()));
					batchWorker.loadFileDataMapping( getTable(true), false);

					//batchWorker.loadFileDataMapping(getTable(true), true);
					refreshTable();
					}
				} catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
			});
		delimitersGroup = new Group(composite, SWT.NONE);
		delimitersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		delimitersGroup.setText(Messages.getString("LoaderWizard.GroupReview")); //$NON-NLS-1$
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 15;
		gridLayout.horizontalSpacing = 15;
		gridLayout.marginTop = 5;
		gridLayout.marginRight = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginBottom = 5;
		delimitersGroup.setLayout(gridLayout);


		table = new Table(delimitersGroup, SWT.SINGLE | SWT.BORDER |
				SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		//table.setSize(268, 560);





		populateFileList( LoaderWizard.file);
		/*
		for (int i = 0; i < NUM; i++) {
			final TableItem items = new TableItem(table, SWT.NONE);
			items.setText(0, "AAA");
			items.setText(1, "AAA");
			items.setText(2, "AAA");
			items.setText(3, "AAA");
		}
		 */
		}
	}
	/**
	 *  
	 */
	class DataTypePage extends WizardPage {
		public static final String PAGE_NAME = "DataInfo"; //$NON-NLS-1$
		//	private Combo fileType;

		public DataTypePage() {
			super(PAGE_NAME);
			setTitle(Messages.getString("LoaderWizard.Page2")); //$NON-NLS-1$

			setPageComplete(true);
		}

		private  Text dfsaText ;
		private  Button delimitedCharactersButton;
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */


		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
			rowLayout.marginLeft = 5;
			rowLayout.marginRight = 5;
			rowLayout.marginTop = 5;
			rowLayout.spacing = 15;
			rowLayout.marginBottom = 5;
			composite.setLayout(rowLayout);

			new Label(composite, SWT.NULL).setText(Messages.getString("LoaderWizard.Page2Text1")); //$NON-NLS-1$


			final Label ifThisIsLabel = new Label(composite, SWT.NULL);
			ifThisIsLabel.setText(Messages.getString("LoaderWizard.Page2Text2")); //$NON-NLS-1$


			if (getDialogSettings() != null && validDialogSettings()) {

			}

			setControl(composite);

			final Group originalDataTypeGroup = new Group(composite, SWT.NONE);
			final RowData rd_originalDataTypeGroup = new RowData();
			rd_originalDataTypeGroup.width = 440;
			originalDataTypeGroup.setLayoutData(rd_originalDataTypeGroup);
			originalDataTypeGroup.setText(Messages.getString("LoaderWizard.GroupOriginalData")); //$NON-NLS-1$
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.marginBottom = 5;
			gridLayout_1.marginTop = 5;
			gridLayout_1.verticalSpacing = 15;
			originalDataTypeGroup.setLayout(gridLayout_1);

			final Label chooseTheFileLabel = new Label(originalDataTypeGroup, SWT.NONE);
			chooseTheFileLabel.setText(Messages.getString("LoaderWizard.OriginalDataText1")); //$NON-NLS-1$

			delimitedCharactersButton = new Button(originalDataTypeGroup, SWT.RADIO);
			delimitedCharactersButton.setSelection(true);
			delimitedCharactersButton.setText(Messages.getString("LoaderWizard.OriginalDataText2")); //$NON-NLS-1$

			delimitedCharactersButton = new Button(originalDataTypeGroup, SWT.RADIO);
			delimitedCharactersButton.setEnabled(false);
			delimitedCharactersButton.setText(Messages.getString("LoaderWizard.OriginalDataText3")); //$NON-NLS-1$

			final Label startImportAtLabel = new Label(composite, SWT.NONE);
			startImportAtLabel.setText(Messages.getString("LoaderWizard.StartImportingAt")); //$NON-NLS-1$

			dfsaText = new Text(composite, SWT.BORDER);
			dfsaText.setTextLimit(2);
			final RowData rd_dfsaText = new RowData();
			rd_dfsaText.width = 14;
			dfsaText.setLayoutData(rd_dfsaText);
			dfsaText.setText("1"); //$NON-NLS-1$



		}

		private boolean validDialogSettings() {

			return true;
		}

		public boolean getDelimiter() {
			return delimitedCharactersButton.getSelection();
		}

		public String getStartImport() {
			return dfsaText.getText();
		}

	}

	/**
	 * 
	 */
	class FrontPage extends WizardPage {

		public static final String PAGE_NAME = "FrontPage"; //$NON-NLS-1$

		FrontPage() {
			super(PAGE_NAME);
			setTitle(Messages.getString("LoaderWizard.Page1")); //$NON-NLS-1$
			//  setDescription("Select the type of room and your arrival date & departure date");
		}

		private Button csvFileButton;
		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setEnabled(false);
			GridLayout gridLayout = new GridLayout(2, false);
			composite.setLayout(gridLayout);

			new Label(composite, SWT.NULL).setText(Messages.getString("LoaderWizard.Page1Text")); //$NON-NLS-1$
			new Label(composite, SWT.NONE);

			csvFileButton = new Button(composite, SWT.RADIO);
			csvFileButton.setSelection(true);
			csvFileButton.setText(Messages.getString("LoaderWizard.TextFileType")); //$NON-NLS-1$

			final Label selectThisFileLabel = new Label(composite, SWT.NONE);
			selectThisFileLabel.setText(Messages.getString("LoaderWizard.TextFileDesc")); //$NON-NLS-1$

			// draws a line. 

			csvFileButton = new Button(composite, SWT.RADIO);
			csvFileButton.setEnabled(false);
			csvFileButton.setText(Messages.getString("LoaderWizard.XMLFileType")); //$NON-NLS-1$

			final Label selectThisFileLabel2 = new Label(composite, SWT.NONE);
			selectThisFileLabel2.setText(Messages.getString("LoaderWizard.XMLFileDesc")); //$NON-NLS-1$



			csvFileButton = new Button(composite, SWT.RADIO);
			csvFileButton.setEnabled(false);
			csvFileButton.setText(Messages.getString("LoaderWizard.DBFileType")); //$NON-NLS-1$

			final Label selectThisFileLabel3 = new Label(composite, SWT.NONE);
			selectThisFileLabel3.setText(Messages.getString("LoaderWizard.DBFileDesc")); //$NON-NLS-1$

			new Label(composite, SWT.NONE);





			setControl(composite);
		}
		public Button getFileType() {
			return csvFileButton;
		}

	}


