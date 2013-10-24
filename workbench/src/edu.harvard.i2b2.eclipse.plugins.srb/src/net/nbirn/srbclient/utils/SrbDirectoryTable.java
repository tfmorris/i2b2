package net.nbirn.srbclient.utils;

import java.text.DateFormat;
import java.util.Date;

import net.nbirn.srbclient.data.DirWorker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.enterprisedt.net.ftp.FTPFile;
import com.jcraft.jsch.ChannelSftp.LsEntry;

//import com.enterprisedt.net.ftp.FTPFile;

import edu.sdsc.grid.io.GeneralFile;

public class SrbDirectoryTable extends Composite implements MouseListener/*,IFolderListener*/{

	private Table DirTable;
	private  		DirWorker dir = null;

	public SrbDirectoryTable(Composite parent, int style, DirWorker dirWorker) {
		super(parent, style);
		dir = dirWorker;
		DirTable = new Table(this, SWT.NONE | SWT.FULL_SELECTION | SWT.MULTI);
		DirTable.setHeaderVisible(true);
		TableColumn colFileName = new TableColumn(DirTable, SWT.LEFT);
		colFileName.setText(Messages.getString("SrbDirectoryTable.File")); //$NON-NLS-1$

		TableColumn colFileSize = new TableColumn(DirTable, SWT.RIGHT);
		colFileSize.setText(Messages.getString("SrbDirectoryTable.Size")); //$NON-NLS-1$

		TableColumn colFileDate = new TableColumn(DirTable, SWT.RIGHT);
		colFileDate.setText(Messages.getString("SrbDirectoryTable.Date")); //$NON-NLS-1$

		TableItem item = new TableItem(DirTable, SWT.NULL);
		item.setText(new String[] { ".." }); //$NON-NLS-1$

		colFileName.setWidth(100);
		colFileSize.setWidth(100);
		colFileDate.setWidth(100);
		DirTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		DirTable.pack();
		DirTable.addMouseListener(this);
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
	/*
	public void updateTable(FileInfo[] list)
	{
		DirTable.removeAll();
		TableItem rootitem = new TableItem(DirTable, SWT.NULL);
		rootitem.setText(new String[] { ".." });
		if ( list == null )
			return;
		//sortFileList(list);
		for (int i = 0; i < list.length; i++) {
			TableItem item = new TableItem(DirTable, SWT.NULL);
			item.setText(0, list[i].getName());
			if (list[i].isDirectory())
				item.setText(1, "Directory");
			else {
				if (list[i].getSize() <  1048576)
					item.setText(1, list[i].getSize() / 1024 + "KB");
				else if (list[i].getSize() <  1073741824)
					item.setText(1, list[i].getSize() / 1048576 + "MB");
				else //if (list[i].length() <  1048576)
					item.setText(1, list[i].getSize() / 1073741824 + "GB");
			}
			item.setText(2,list[i].getDate());
		}
	}
	 */

	public void updateTable(Object[] list)
	{
		DirTable.removeAll();
		TableItem rootitem = new TableItem(DirTable, SWT.NULL);
		rootitem.setText(new String[] { ".." }); //$NON-NLS-1$
		if ( list == null )
			return;
		FTPFile fii = null;
		LsEntry lsen = null;
		GeneralFile gl = null;
		//sortFileList(list);
		for (int i = 0; i < list.length; i++) {

			if (list[i] instanceof GeneralFile) {
				TableItem item = new TableItem(DirTable, SWT.NULL);
				gl = (GeneralFile) list[i];

				item.setText(0, gl.getName());
				if (gl.isDirectory())
					item.setText(1, Messages.getString("SrbDirectoryTable.Directory")); //$NON-NLS-1$
				else {
					if (gl.length() <  1048576)
						item.setText(1, gl.length() / 1024 + Messages.getString("SrbDirectoryTable.KB")); //$NON-NLS-1$
					else if (gl.length() <  1073741824)
						item.setText(1, gl.length() / 1048576 + Messages.getString("SrbDirectoryTable.MB")); //$NON-NLS-1$
					else //if (list[i].length() <  1048576)
						item.setText(1, gl.length() / 1073741824 + Messages.getString("SrbDirectoryTable.GB")); //$NON-NLS-1$
				}
				item.setText(2,new Date(gl.lastModified()).toString());
			} else if (list[i] instanceof FTPFile)
			{
				TableItem item = new TableItem(DirTable, SWT.NULL);

				fii = (FTPFile) list[i];
				item.setText(0, fii.getName());
				if (fii.isDir())
					item.setText(1, Messages.getString("SrbDirectoryTable.Directory")); //$NON-NLS-1$
				else {
					if (fii.size() <  1048576)
						item.setText(1, fii.size() / 1024 + Messages.getString("SrbDirectoryTable.KB")); //$NON-NLS-1$
					else if (fii.size() <  1073741824)
						item.setText(1, fii.size() / 1048576 + Messages.getString("SrbDirectoryTable.MB")); //$NON-NLS-1$
					else //if (list[i].length() <  1048576)
						item.setText(1, fii.size() / 1073741824 + Messages.getString("SrbDirectoryTable.GB")); //$NON-NLS-1$
				}
				item.setText(2, DateFormat.getDateInstance().format(fii.lastModified()));				
			} else if (list[i] instanceof LsEntry)
			{
				lsen = (LsEntry) list[i];
				if (!lsen.getFilename().equals(".") && !lsen.getFilename().equals("..")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					TableItem item = new TableItem(DirTable, SWT.NULL);

				item.setText(0, lsen.getFilename());
				if (lsen.getAttrs().isDir())
					item.setText(1, Messages.getString("SrbDirectoryTable.Directory")); //$NON-NLS-1$
				else {
					if (lsen.getAttrs().getSize() <  1048576)
						item.setText(1, lsen.getAttrs().getSize() / 1024 + Messages.getString("SrbDirectoryTable.KB")); //$NON-NLS-1$
					else if (lsen.getAttrs().getSize() <  1073741824)
						item.setText(1, lsen.getAttrs().getSize() / 1048576 + Messages.getString("SrbDirectoryTable.MB")); //$NON-NLS-1$
					else //if (list[i].length() <  1048576)
						item.setText(1, lsen.getAttrs().getSize() / 1073741824 + Messages.getString("SrbDirectoryTable.GB")); //$NON-NLS-1$
				}
				item.setText(2, lsen.getAttrs().getAtimeString());				
				}
			}
		}
	}

	public void mouseDoubleClick(MouseEvent e) {
		TableItem[] item = DirTable.getSelection();
		String name = item[0].getText(0);
		dir.notifyActionOnFile(name);
		/*
		if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("SRB"))
		{
			dir.notifyActionOnFile(name);
		} else if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("IRODS"))
		{
			dir.notifyActionOnFile(name);
		} else if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("FTP"))
		{
			dir.notifyActionOnFile(name);
		} else if (UserInfoBean.getInstance().getSelectedProjectParam("FRMethod").equals("SFTP"))
		{
			dir.notifyActionOnFile(name);
		}
		*/


	}

	public void mouseDown(MouseEvent e) {


	}

	public void mouseUp(MouseEvent e) {

	}	


}
