package net.nbirn.srbclient.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import net.nbirn.srbclient.data.ClientDirWorker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class DirectoryTable extends Composite implements MouseListener
		{

	private Table DirTable;

	public DirectoryTable(Composite parent, int style) {
		super(parent, style);

		DirTable = new Table(this, SWT.NONE | SWT.FULL_SELECTION | SWT.MULTI);
		DirTable.setHeaderVisible(true);
		TableColumn colFileName = new TableColumn(DirTable, SWT.LEFT);
		colFileName.setText("File");

		TableColumn colFileSize = new TableColumn(DirTable, SWT.RIGHT);
		colFileSize.setText("Size");

		TableColumn colFileDate = new TableColumn(DirTable, SWT.RIGHT);
		colFileDate.setText("Date");

		TableItem item = new TableItem(DirTable, SWT.NULL);
		item.setText(new String[] { ".." });

		colFileName.setWidth(66);
		colFileSize.setWidth(66);
		colFileDate.setWidth(66);
		DirTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		DirTable.pack();
		DirTable.addMouseListener(this);
		
	}

	/**
	 * Set the Parent directory for the table Provides 
	 * the two points over back to click lists listing
	 * contents in the table 
	 * @param File root
	 */
	public void setRootDir(File root) {
		if (root.exists() && root.isDirectory()) {
			if ( DirTable.getItemCount() > 0 )
				DirTable.removeAll();
			TableItem rootitem = new TableItem(DirTable, SWT.NULL);
			rootitem.setText(new String[] { ".." });
			File[] files = root.listFiles();
			if ( files == null )
				return;
			sortFileList(files);
			for (int i = 0; i < files.length; i++) {
				TableItem item = new TableItem(DirTable, SWT.NULL);
				item.setText(0, files[i].getName());
				if (files[i].isDirectory())
					item.setText(1, "Directory");
				else {
					if (files[i].length() <  1048576)
						item.setText(1, files[i].length() / 1024 + "KB");
					else if (files[i].length() <  1073741824)
						item.setText(1, files[i].length() / 1048576 + "MB");
					else //if (list[i].length() <  1048576)
						item.setText(1, files[i].length() / 1073741824 + "GB");
					setFileProgrammImage(item, files[i]);
				}

				item.setText(2, new Date(files[i].lastModified()).toString());
			}

		}
	}
	

	public void setFileProgrammImage(TableItem item, File f) {
		String fullname = f.getName();

		int pos = fullname.indexOf(".");

		if (pos > 0) {
			String type = fullname.substring(pos + 1);
			if ( type == null )
				return;
			Program program = Program.findProgram(type);
			if (program != null) {
				ImageData imgData = program.getImageData();
				if ( imgData == null )
					return;
				Image img = new Image(Display.getCurrent(),imgData);
				if (img != null)
					item.setImage(img);
			}
		}

	}
	/**
	 * Sorts the handed over array of the type file according to 
	 * listings and files the sequence 
	 * is listings --> alphabetical --> files alphabetical
	 * @param files
	 */
	public void sortFileList(File[] files)
	{
		//Count dirs :
		int cnt = 0;
		for ( int i = 0 ; i < files.length ; i++ )
		{
			if ( files[i].isDirectory())
				cnt++;
		}
		File[] dirs = new File[cnt];
		File[] filesArray = new File[files.length-cnt];
		cnt = 0;
		int cnt2 = 0;
		for ( int i = 0 ; i < files.length ; i++)
		{
			if ( files[i].isDirectory())
			{
				dirs[cnt++] = files[i];
			}
			else
			{
				filesArray[cnt2++] = files[i];
			}
		}
		Arrays.sort(dirs);
		Arrays.sort(filesArray);
		//Copy the Files Back in the Orginal Array
		for ( int i = 0 ; i < dirs.length ; i++)
		{
			files[i] = dirs[i];
		}
		for ( int i = 0 ; i < filesArray.length ; i++ )
		{
			if ( dirs.length != 0 )
				files[i+dirs.length] = filesArray[i];
			else
				files[i+dirs.length] = filesArray[i];
		}
	}

	public void mouseDoubleClick(MouseEvent e) {
		TableItem[] item = DirTable.getSelection();
		String name = item[0].getText(0);
		if (name.equals("..")) {
			setRootDir(ClientDirWorker.getClientDir().DirectoryBackWard());
		} else if (!name.equals(""))
			setRootDir(ClientDirWorker.getClientDir().DirectoryForward(name));
	}

	public void mouseDown(MouseEvent e) {
	}

	public void mouseUp(MouseEvent e) {	
	}

	public Table getDirTable()
	{
		return DirTable;		
	}
	public String[] getSelectionFiles()
	{
		TableItem[] items = DirTable.getSelection();
		String[] filenames = null;
		if ( items != null )
		{	
			filenames = new String[items.length];
			for (int i = 0; i < items.length; i++) {
				filenames[i] = items[i].getText();
			}
		}
		return filenames;
	}

	

}
