package net.nbirn.srbclient.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import net.nbirn.srbclient.utils.VerifyData;
import net.nbirn.srbclient.utils.VerifyDataEventMapping;
import net.nbirn.srbclient.utils.VerifyDataPatientMapping;
import net.nbirn.srbclient.utils.VerifyDataObservationFact;
import net.nbirn.srbclient.utils.VerifyDataPatientDimension;
import net.nbirn.srbclient.utils.VerifyDataProviderDimension;
import net.nbirn.srbclient.utils.VerifyDataVisitDimension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


import edu.harvard.i2b2.frclient.security.CryptUtil;


public class BatchWorker {
	//private DTOFactory dtoFactory = new DTOFactory();
	private  final Log log = LogFactory.getLog(BatchWorker.class);
	private  VerifyData vData = null;
	//private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	//private File f = null;
	//private DirWorker dirWorker;
	CryptUtil crypt;
	private int maxCount = 499;
	//private Display theDisplay;

	private String filename;
	private int startAt;
	private String fileType;
	private char delimiter;
	private String eolQualifer;
	private char textQualifer;
	private String[] isHeaders = new String[1];
	private ArrayList headersMissingPK = new ArrayList();
	private ArrayList headersDuplicate = new ArrayList();

	public BatchWorker() 
	{
		//vData.init();
	}

	public int getNumErrors()
	{
		return vData.getNumErrors();
	}

	public boolean isAllClear()
	{
		return vData.isAllClear();
	}




	public void loadFileDataMapping(Table table,boolean useHeaders) throws Exception
	{
		boolean header = false;

		//BufferedReader reader = new BufferedReader (new FileReader (filename));

		CSVFileReader reader = new CSVFileReader(filename, delimiter, textQualifer, eolQualifer);
		Vector<String> csv;
		if (useHeaders == false) {
			if (fileType.toUpperCase().startsWith("PAT"))
				vData = new VerifyDataPatientDimension();
			else if (fileType.toUpperCase().startsWith("PRO"))
				vData = new VerifyDataProviderDimension();
			else if (fileType.toUpperCase().startsWith("OBS"))
				vData = new VerifyDataObservationFact();
			else if (fileType.toUpperCase().startsWith("VIS"))
				vData = new VerifyDataVisitDimension();
			else if (fileType.toUpperCase().startsWith("PTM"))
				vData = new VerifyDataPatientMapping();
			else if (fileType.toUpperCase().startsWith("ETM"))
				vData = new VerifyDataEventMapping();
			else
				throw new Exception(filename + " is missing first row with file type.");

			vData.init();
		}
		int count = 0;

		final GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd_table);
		while ((csv=reader.readFields(startAt)) != null)
		{
			if (csv.size() < 1)
				continue;



			if (csv.size() < 1)
			{
				continue;
			}

			if (startAt-1 <= count)
			{
				if (header == false)
				{

					if (useHeaders)
					{
						for (int i=0; i < isHeaders.length; i++)				
						{
							if (!isHeaders[i].equals("Ignore")) {
								vData.setHeaderLocation(isHeaders[i], i);

								TableColumn column = new TableColumn(table, SWT.CENTER);
								column.setText(isHeaders[i]);
								column.setWidth(125);
								column.setAlignment(SWT.LEFT);
								//column.pack();
							} else
							{
								vData.setHeaderLocation(i, -1);
							}

						}


					} else {
						// Create five columns
						isHeaders = new String[csv.size()];
						for (int i = 0; i < csv.size(); i++) {
							vData.setHeaderLocation(csv.elementAt(i), i);

							TableColumn column = new TableColumn(table, SWT.CENTER);
							column.setText(csv.elementAt(i));
							column.setAlignment(SWT.LEFT);
							column.setWidth(125);
							//column.pack();
						}	    
						// Create five table editors for color
						final TableEditor[] colorEditors = new TableEditor[csv.size()];





						// Create the row
						final TableItem item = new TableItem(table, SWT.NONE);

						for (int i = 0; i < csv.size(); i++) {
							final CCombo combo = new CCombo(table, SWT.READ_ONLY);
							final int current = i; 
							combo.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent event) {
									setHeaders(combo.getText(), current);

								}
							});
							for (int ii = 0, n = vData.getColumns().length; ii < n; ii++) {
								//setHeaders(combo.getText(), current);
								combo.add(vData.getColumns()[ii]);
								if ((csv.elementAt(i).length() > 4) && csv.elementAt(i).substring(0, 4).toUpperCase().equals(vData.getColumns()[ii].substring(0, 4).toUpperCase()))
								{
									combo.select(ii);
									item.setText(current, combo.getText());

								}
							}
							if (combo.getSelectionIndex() == -1)
							{
								combo.select(0);
								item.setText(current, combo.getText());
								//setHeaders(combo.getText(), current);
							}
							setHeaders(combo.getText(), current);
							// Create the editor and button
							colorEditors[i] = new TableEditor(table);
							colorEditors[i].grabHorizontal = true;
							colorEditors[i].grabVertical = true;
							//colorEditors[i].minimumWidth = 100;
							//colorEditors[i].minimumHeight = 25;

							colorEditors[i].setEditor(combo, item, i);
						}

					}

					//	int i=0;

					header = true;
					continue;
				}
				TableItem abc = new TableItem(table,SWT.NONE);
				vData.validateData(abc, useHeaders, false, csv, isHeaders);
			}
			count++;
			if (count > maxCount)
			{

				break;
			}
		}
		
		reader.close();
		table.pack();

	}

	public char getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String[] getHeaders() {
		return isHeaders;
	}

	public boolean getHeaders(String name)
	{
		for (int i=0; i < isHeaders.length; i++)
		{
			if (isHeaders[i].equals(name))
				return true;
		}
		return false;

	}
	public void setHeaders(String header, int loc) {
		this.isHeaders[loc] = header;

		headersMissingPK = new ArrayList();
		headersDuplicate = new ArrayList();
		
		Hashtable testHeaders = new Hashtable();
		for (int ii = 0; ii < isHeaders.length;  ii++) {
			if (isHeaders[ii] != null && !isHeaders[ii].equals("Ignore"))
			{
				if (testHeaders.containsKey(isHeaders[ii]))
				{
					headersDuplicate.add(isHeaders[ii]);
					log.debug("InValid Headers is " + isHeaders[ii] + " for " + header);

					//return;
				}
				else
				{
					testHeaders.put(isHeaders[ii], "");
				}
			}
		}
		try {
			for (int ii = 0; ii < vData.getPKColumns().length;  ii++) {

				if (!getHeaders(vData.getPKColumns()[ii]))
				{
					headersMissingPK.add(vData.getPKColumns()[ii]);
					log.debug("InValid Headers is " + vData.getPKColumns()[ii] + " for " + header);

					//return;
				}
			}
		} catch (Exception e)
		{
			//validHeaders = false;
			log.debug("Got a expection in checking headers: " + e.getMessage());
			//return;
		}
		log.debug("Valid Headers is valid for " + header);
	}

	public void setHeaders(String[] isHeaders) {
		this.isHeaders = isHeaders;
	}


	public int getStartAt() {
		return startAt;
	}

	public void setStartAt(int startAt) {
		this.startAt = startAt;
	}
	public String getEOLQualifer() {
		return eolQualifer;
	}

	public void setEOLQualifer(String eolQualifer) {
		this.eolQualifer = eolQualifer;
	}

	public char getTextQualifer() {
		return textQualifer;
	}

	public void setTextQualifer(char textQualifer) {
		this.textQualifer = textQualifer;
	}
	public String getFilename () {
		return filename;
	}

	public VerifyData getVerifyData () {
		return vData;
	}
	
	public ArrayList getValidHeadersDuplicate() {
		return headersDuplicate;
	}


	public ArrayList getValidHeadersPK() {
		return headersMissingPK;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
