package net.nbirn.srbclient.utils;

import java.util.Date;
import java.util.Vector;

import org.eclipse.swt.widgets.TableItem;

public interface VerifyData {


	public int getDataLocation(String type);
	public int getHeaderLength();
	public void init();
	public boolean isValidHeader();
	public String[] getPKColumns();
	public String[] getColumns();
	public void setHeaderLocation(String name, int count);
	public void validateData(TableItem abc,boolean editable,  boolean hasStatus, Vector<String> csv, String[] headers);
	public void setHeaderLocation(int origLoc, int newValue);

	public String getValidDate(String origdate);
	public Date getDate(String origdate);

	public boolean isAllClear() ;
	public int getNumErrors();


	public String getBadColumn() ;


	
}
