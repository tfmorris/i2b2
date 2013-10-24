package net.nbirn.srbclient.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

public  class VerifyDataVisitDimension implements VerifyData {

	private Date correctDate = null;
	private final SimpleDateFormat correctFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	private boolean allClear = true;
	private int numErrors = 0;
	private Font boldFont = null;
	private ArrayList headerName = new ArrayList();
	private int[] headerLocation = new int[]
	                                       {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};

	private String badColumn = "";

	public int getDataLocation(String type)
	{
		if (headerName.indexOf(type) == -1)
			return -1;
		else
			return headerLocation[headerName.indexOf(type) ];
	}
	public int getHeaderLength()
	{
		return headerLocation.length;

	}
	public void init()
	{
		badColumn = "";
		allClear = true;
		numErrors = 0;

		FontData[] data = Display.getCurrent().getSystemFont().getFontData();
		FontData data0 = data[0];
		//     Set the font style to italic
		data0.setStyle(SWT.BOLD);
		//     Create a new font
		boldFont = new Font(Display.getCurrent(), data0);		
		headerName.add("ENCO"); headerName.add("PATI"); headerName.add("INOU"); headerName.add("LOCA");
		headerName.add("PATH"); headerName.add("STAR"); headerName.add("ENDD");  
		headerName.add("BLOB"); headerName.add("UPDA");	headerName.add("DOWN"); headerName.add("IMPO");
		headerName.add("SOUR");
	}

	public String[] getPKColumns()
	{
		return new String[]  {
				"Encounter_Num",
				"Patient_Source",
				"Patient_Num"

		};
	}
	public String[] getColumns()
	{
		return new String[]  {
				"Download_Date",
				"Encounter_Num",
				"End_Date",
				"Import_Date",
				"Inout_Cd",
				"Location_Cd",
				"Location_Path",
				"Patient_Source",
				"Patient_Num",
				"Sourcesystem_Cd",
				"Start_Date",
				"Update_Date",
				"Visit_Blob"};

	}

	public boolean isValidHeader()
	{
		boolean isValid = true;

		for (int i=0; i < headerLocation.length; i++)
			if (headerLocation[i] == -1)
			{
				isValid = false;
			}
		return isValid;
	}
	public void setHeaderLocation(int origLoc, int newValue)
	{
		for (int i=0; i <headerLocation.length; i++)
		{
			if (headerLocation[i] == origLoc)
				headerLocation[origLoc] = newValue;
		}
	}

	public void setHeaderLocation(String name, int count)
	{
		name = name.trim();
		name = name.toUpperCase();
		name = name.replaceAll(" ", "");
		name = name.replaceAll("_", "");

		if (name.contains("PATH"))
			name = "PATH";
		else if (name.contains("BLOB"))
			name = "BLOB";

		int loc = headerName.indexOf(name.substring(0, 4));
		if (loc > -1)
			headerLocation[loc] = count;
		else
			badColumn += name + "\n";

	}
	public void validateData(TableItem abc, boolean editable, boolean hasStatus, Vector<String> csv, String[] headers)
	{
		FontData[] data = Display.getCurrent().getSystemFont().getFontData();
		FontData data0 = data[0];
		//     Set the font style to italic
		data0.setStyle(SWT.BOLD);
		//     Create a new font
		Font boldFont = new Font(Display.getCurrent(), data0);

		if (hasStatus) {
			int loc = -1;

			//PK can not be null
			loc = headerLocation[headerName.indexOf("ENCO") ];
			isNotNull(abc,loc,csv.elementAt(loc));

			loc = headerLocation[headerName.indexOf("PATI") ];
			isNotNull(abc,loc,csv.elementAt(loc));

			/*
		//First 6 can not be null
		for (int i=0; i < 2; i++)
		{
			loc = headerLocation[i];
			if ((csv[loc].trim()).length() == 0)
			{
				//abc.setText(i,"{blank}");
				abc.setBackground(loc+1,red);
				//abc.setFont(i,boldFont);
				//if (allClear)
				//	table.setSelection(abc);
				allClear = false;
				abc.setText(0, "Error");
				abc.setForeground(0,red);
				numErrors++;

			}
		}
			 */


			//Verify Number is a number
			loc = headerLocation[headerName.indexOf("PATI") ];
			isNumber(abc,loc,csv.elementAt(loc));

			//Verify Number is a number
			loc = headerLocation[headerName.indexOf("ENCO") ];
			isNumber(abc,loc,csv.elementAt(loc));

			// Fill all fields with date
			//for (int i=0; i < headerLocation.length; i++)
			for (int i=0; i < csv.size(); i++)
				abc.setText((hasStatus?i+1:i), csv.elementAt(i));

			//Verify Date
			loc = headerLocation[headerName.indexOf("STAR") ];
			if (csv.size() > loc)
				isDate(abc,loc,csv.elementAt(loc));
			loc = headerLocation[headerName.indexOf("ENDD") ];
			if (csv.size() > loc)
				isDate(abc,loc,csv.elementAt(loc));
			loc = headerLocation[headerName.indexOf("UPDA") ];
			if (csv.size() > loc)
				isDate(abc,loc,csv.elementAt(loc));
			loc = headerLocation[headerName.indexOf("DOWN") ];
			if (csv.size() > loc)
				isDate(abc,loc,csv.elementAt(loc));
			loc = headerLocation[headerName.indexOf("IMPO") ];
			if (csv.size() > loc)
				isDate(abc,loc,csv.elementAt(loc));
		} else {
			int count = 0;
			for (int i=0; i < csv.size(); i++)

				if (!editable)
					abc.setText(i, csv.elementAt(i));
				else if ((headers == null) || (!headers[i].equals("Ignore"))) 
					abc.setText(count++, csv.elementAt(i));
		}

		abc.setChecked(true);

	}

	private void isNotNull (TableItem abc, int loc, String value)
	{

		if ((value.trim()).length() == 0)
		{
			//abc.setText(i,"{blank}");
			abc.setBackground(loc+1,red);
			//abc.setFont(i,boldFont);
			//if (allClear)
			//	table.setSelection(abc);
			allClear = false;
			abc.setText(0, "Error");
			abc.setForeground(0,red);
			numErrors++;
		}
		else
		{
			abc.setText(loc+1, value);
		}
	}

	private void isDate(TableItem abc, int loc, String value)
	{
		//Verify Date
		String newDate = getValidDate(value.trim());

		if (newDate == null)
		{
			abc.setForeground(loc+1,red);
			abc.setFont(loc+1,boldFont);
			allClear = false;
			abc.setText(0, "Error");
			abc.setForeground(0,red);
			numErrors++;
		}
		else
		{
			abc.setText(loc+1, newDate);
		}


	}
	private void isNumber(TableItem abc, int loc, String value)
	{
		if (value.length() > 0) 
			try
		{
				int tester = Integer.parseInt(value); //encounter
		} catch (Exception e)
		{



			abc.setForeground(loc+1,red);
			abc.setFont(loc+1,boldFont);
			allClear = false;
			abc.setText(0, "Error");
			abc.setForeground(0,red);
			numErrors++;
		}
	}
	public String getValidDate(String origdate)
	{

		return correctFormat.format(getDate(origdate));
	}
	public Date getDate(String origdate)
	{
		//	getValidDate(date, "MM/dd/yyyy");
		//	if (correctDate != null)
		//		return correctDate;
		String date = origdate.replace(':', '-');
		date = date.replace('/', '-');
		date = date.replace('.', '-');

		correctDate = null;
		getValidDate(date, "dd-MMM-yyyy hh-mm a");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "dd-MMM-yyyy HH-mm");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "dd-MMM-yy hh-mm a");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "dd-MMM-yy HH-mm");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "dd-MMM-yy");
		if (correctDate != null)
			return correctDate;		
		getValidDate(date, "MM-dd-yyyy hh-mm a");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "MM-dd-yyyy HH-mm");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "MM-dd-yy hh-mm a");
		if (correctDate != null)
			return correctDate;
		getValidDate(date, "MM-dd-yy HH-mm");
		if (correctDate != null)
			return correctDate;


		getValidDate(origdate, DateFormat.SHORT);
		if (correctDate != null)
			return correctDate;
		getValidDate(origdate, DateFormat.FULL);
		if (correctDate != null)
			return correctDate;
		getValidDate(origdate, DateFormat.LONG);
		if (correctDate != null)
			return correctDate;
		getValidDate(origdate, DateFormat.DEFAULT);
		if (correctDate != null)
			return correctDate;
		getValidDate(origdate, DateFormat.MEDIUM);
		if (correctDate != null)
			return correctDate;

		return correctDate;
	}

	private void getValidDate(String date, String format)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		//Date testDate = null;

		// we will now try to parse the string into date form
		try
		{
			correctDate = sdf.parse(date);
		}

		// if the format of the string provided doesn't match the format we
		// declared in SimpleDateFormat() we will get an exception

		catch (ParseException e)
		{
			correctDate = null;
			return ;
		}
		//return testDate;
		//correctDate = correctFormat.format(testDate);

	} // end isValidDate

	private void getValidDate(String date, int format)
	{
		DateFormat testFormat =	DateFormat.getDateInstance(format);

		// declare and initialize testDate variable, this is what will hold
		// our converted string

		//Date testDate = null;

		// we will now try to parse the string into date form
		try
		{
			correctDate = testFormat.parse(date);
		}

		// if the format of the string provided doesn't match the format we
		// declared in SimpleDateFormat() we will get an exception

		catch (ParseException e)
		{
			correctDate = null;
			return ;
		}

		//correctDate = correctFormat.format(testDate);
	} // end isValidDate
	public boolean isAllClear() {
		return allClear;
	}
	public int getNumErrors() {
		return numErrors;
	}


	public String getBadColumn() {
		return badColumn;
	}


}
