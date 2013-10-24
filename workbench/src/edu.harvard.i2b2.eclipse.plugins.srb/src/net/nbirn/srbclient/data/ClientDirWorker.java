package net.nbirn.srbclient.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import net.nbirn.srbclient.plugin.PluginPlugin;
import net.nbirn.srbclient.utils.ErrorTxt;
import net.nbirn.srbclient.utils.IFolderListener;


public class ClientDirWorker 
{
	private IFolderListener folderListener = null;
	//private ILogger ftpLogger = null;
	private static ClientDirWorker instance = null;
	private String seperator = File.separator;
	private int ret = 0;
	//private ProgressMonitor monitor = null;
	private boolean isChanceled = false;
	TransferThread thread = null;
	private Table statusTable = null;
	private static final Log log = LogFactory.getLog(ClientDirWorker.class);

	File actDirectory = null;

	public void setTable(Table txfStatusTable) {
		// TODO Auto-generated method stub
		statusTable = txfStatusTable;
	}
	private String getSize(long length)
	{
			String result = "";
		if (length <  1048576)
			result = length / 1024 + "KB";
		else if (length <  1073741824)
			result = length / 1048576 + "MB";
		else //if (list[i].length() <  1048576)
			result = length / 1073741824 + "GB";
		return result;
	}
	private String timeLeft(long newTime, long oldTime, int currentCopiedBytes, int oldCopiedBytes, int size)
	{
		int timeDiff = (int) (newTime - oldTime);
		String format = "";
		try
		{
		  int timeRemaining = timeDiff; //*bytesCopied/bytesRemaining;
		  int sec = (timeRemaining/1000) % 60;
		  int min = (timeRemaining/(1000*60)) % 60;
		  if (sec != 0)
		     format = sec + " seconds";
		  if (min != 0)
		     format = min + " minutes " + format; 
		  //ect
		}
		catch(ArithmeticException e)
		{
		  //we did not recieve any bytes durring the last transfer
		  format = "";
		} 
		return format;
	}

	private ClientDirWorker()
	{
		actDirectory = new File(System.getProperty("user.home"));
		seperator = System.getProperty("file.separator");
	}
	public String getFileSeparator()
	{
		return seperator;
	}
	static public ClientDirWorker getClientDir()
	{
		if ( instance == null )
			instance = new ClientDirWorker();
		return instance;
	}
	
	public File getActDirectory()
	{
		return actDirectory;
	}
	public void setActDirectory(File f)
	{
		actDirectory = f;
		notifyUpdateFileList();
	}
	public File DirectoryBackWard()
	{
		File newDirectory = actDirectory.getParentFile();
		if ( newDirectory != null)
			actDirectory = newDirectory;
		return actDirectory;
	}
	public File DirectoryForward(String name)
	{
		String newPath = actDirectory.getAbsolutePath()+seperator+name;
		File newDirectory = new File(newPath);
		if ( newDirectory.isDirectory() && newDirectory.exists())
			actDirectory = newDirectory;
		return actDirectory;
	}
	public FileSystemView getFileSystemView()
	{
		return FileSystemView.getFileSystemView();
	}
	public void addClientFolderListener(IFolderListener listener)
	{
		this.folderListener = listener;
	}
	public void deleteDirs(String[] files) 
	{
		int ret = IFolderListener.DO_NOT_REMOVE_DIR;
		for (int i = 0; i < files.length; i++) {
			File fileToRemove = new File(actDirectory.getAbsolutePath()+seperator+files[i]);
			if ( fileToRemove.isDirectory() )
			{
				if ( fileToRemove.listFiles().length > 0 )
				{
					ret =  notifyRemovingNoEmtyDir(fileToRemove.getName());
					if ( ret == IFolderListener.REMOVE_DIR )
					{
						deleteSubDirsAndFiles(fileToRemove.listFiles());
						fileToRemove.delete();
					}
				}
				else
				{
					fileToRemove.delete();
				}
			}
			else
			{
				if ( fileToRemove.delete() ) 
					log.debug("File :" + actDirectory.getAbsolutePath()+seperator+files[i]+" is deletes");
				else
					log.debug("File :" + actDirectory.getAbsolutePath()+seperator+files[i]+" could not be deleted");
				
			}
			
		}
		notifyUpdateFileList();
	}
	/**
	 *  Provides a file on the Client site.
	 *  Informed with the presence of the file the FolderListener and a return which expects
	 *  will happen is 
	 * @see #folderListener
	 * @param folders directory in that the file provide will is
	 * @param name the name of the file to be provided is
	 * @return The Stream of the file if these to be provided 
	 * the Stream of the file if these available were and to be overwritten may 
	 * zero knew if the file not be provided could 
	 * zero if the file were already missing and are not overwritten 
	 */
	public OutputStream createOrOverideNewFile(String folder,String name)
	{
		File f = new File(actDirectory+"/"+folder,name);
		log.debug("file :" + f.toString() + " is provided by the client");
		if ( f.exists() && (!f.isDirectory())) 
		{
			if (  notifyOveridingFile(name) == IFolderListener.OVERRIDE_FILE )  //Überschreiben
			{
				try {
					FileOutputStream out = new FileOutputStream(f);
					return out;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}
			}
			else
			{
				return null; 
			}
		}
		else
		{
			try {
				FileOutputStream out = new FileOutputStream(f);
				return out;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	/**
	 * Provides a reuse directory on the Client side. Is directory already available 
	 * will more folderListener and must examine whether in directory be created 
	 * 
	 * @param name name of directory
	 * @return true if directory one provided and to be written may
	 * 		 true if do not directory was already present and to be written may 
	 *  	 false directory available and to be written may 
	 *   	 false errors with that provide the listing 
	 */
	public boolean createDir(String name)
	{
		File dirToCreate = new File(actDirectory,name);
		
		if ( checkDir(dirToCreate) == true)  
		{
			int retVal = IFolderListener.DO_NOT_OVERRIDE_DIR;
			retVal = notifyOveridingDir(name);
			if ( retVal == IFolderListener.OVERRIDE_DIR || retVal == IFolderListener.OVERRIDE_DIR_AND_SAVE_ANSWER)
			{
				log.debug("Directory :" + dirToCreate.getAbsolutePath() + " is overwritten");
				return true; 
			}
			else 
			{
				log.debug("Directory :" + dirToCreate.getAbsolutePath() + " is not overwritten");
				return false; 
			}
		}
		else 
		{
			log.debug("Directory :" + dirToCreate.getAbsolutePath() + " provided");
			return dirToCreate.mkdir();
		}
	}
	
	
	public boolean rename(String origloc, String newloc)
	{
		try {
		File from = new File(actDirectory,origloc);
		File to = new File(actDirectory,newloc);
		from.renameTo(to);
		} catch (Exception e)
		{
			log.error("Error on renaming directory: " + e.getMessage());
			return false;
		}
		/*
		if ( checkDir(dirToCreate) == true)  
		{
			int retVal = IFolderListener.DO_NOT_OVERRIDE_DIR;
			retVal = notifyOveridingDir(name);
			if ( retVal == IFolderListener.OVERRIDE_DIR || retVal == IFolderListener.OVERRIDE_DIR_AND_SAVE_ANSWER)
			{
				log.debug("Directory :" + dirToCreate.getAbsolutePath() + " is overwritten");
				return true; 
			}
			else 
			{
				log.debug("Directory :" + dirToCreate.getAbsolutePath() + " is not overwritten");
				return false; 
			}
		}
		else 
		{
			log.debug("Directory :" + dirToCreate.getAbsolutePath() + " provided");
			return dirToCreate.mkdir();
		}
		*/
		return true;
	}

	
	/**
	 * Examines whether directory is already present  
	 * @param D name of the listing 
	 * @return true if directory available false if not 
	 */
	public boolean checkDir(File d)
	{
		if ( d.exists() && d.isDirectory())
			return true;
		else
			return false;
	}
	private void deleteSubDirsAndFiles(File[] list)
	{
		for (int i = 0; i < list.length; i++) {
			if ( list[i].isDirectory() )
			{
				if ( list[i].listFiles().length > 0 )
				{
					deleteSubDirsAndFiles(list[i].listFiles());
					list[i].delete();
				}
				else
				{
					list[i].delete();
				}
				
			}
			else // No Dir
			{
				if ( list[i].delete() ) 
					log.debug("File :" + actDirectory.getAbsolutePath()+seperator+list[i]+" is deleted");
				else
					log.debug("File :" + actDirectory.getAbsolutePath()+seperator+list[i]+" could not be deleted");
			}
		}
	}
	public void uploadFiles(DirWorker dirWorker, String dir,String[] files) throws InterruptedException
	{
	//	if ( thread == null || thread.isAlive() == false)
	//	{
	//		isChanceled = false;
	//		this.thread = new UploadThread(dir,files);
	//		thread.start();
	//	}
		UploadThread(dirWorker, dir,files);
		run();
	}
	public InputStream getFileStream(File f)
	{
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {	
			log.error(ErrorTxt.ERROR_CREATING_FILE_ON_CLIENT);
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	protected void notifyUpdateFileList()
	{
		if ( folderListener  != null )
		{
			PluginPlugin.getDefault().getWorkbench().getDisplay().syncExec(new Runnable(){

				public void run() {
					folderListener.updateDirectoryList();
				}});
				
	
		}
	}
	private int notifyRemovingNoEmtyDir(final String name)
	{
		if ( folderListener  != null )
		{
			
			PluginPlugin.getDefault().getWorkbench().getDisplay().syncExec(new Runnable(){

				public void run() {
					ret = folderListener.notifyRemovingNoEmtyDir(name);
				}});
		}
		return ret;
	}
	private int notifyOveridingDir(final String name)
	{
		if ( folderListener  != null )
		{
			PluginPlugin.getDefault().getWorkbench().getDisplay().syncExec(new Runnable(){

				public void run() {
					ret = folderListener.notifyOveridingExistingDir(name);
				}});
			
		
		}
		return ret;
		
	}
	private int notifyOveridingFile(final String name)
	{
		//final int ret= IFolderListener.DO_NOT_OVERRIDE_DIR;
		if ( folderListener  != null )
		{
			PluginPlugin.getDefault().getWorkbench().getDisplay().syncExec(new Runnable(){

				public void run() {
					ret = folderListener.notifyOverridingExistingFile(name);
				}});
			
		}
		return ret;
	}
	public void stop()
	{
		isChanceled = true;
	}
	
//	class UploadThread extends Thread
//	{
		private String dir = null;
		private String[] files = null;
		private DirWorker ftp = null;

		public void UploadThread(DirWorker dirWorker, String dir,String[] files)
		{
			this.dir = dir;
			this.ftp = dirWorker;
			this.files = files;
		}
		public void run() throws InterruptedException
		{
		
		   upload(dir,files);
		}
		public void upload(String dir,String[] files) throws InterruptedException
		{
			boolean hasCdUp = false;
			if ( dir == null )
			{
				dir = actDirectory.getAbsolutePath();
				log.debug("Absolute path of client = "+ dir);
			}
			for (int i = 0; i < files.length; i++) {
				if ( isChanceled == true )
				{
	//				interrupt();
					
					return;
				}
				File actFile = new File(dir+seperator+files[i]);
				log.debug("Current file/diretory of client is "+ actFile.getAbsolutePath());
				if ( actFile.isDirectory())
				{
					log.debug("File " + actFile + " is directory");
					ftp.createDir(actFile.getName());
					String[] list = actFile.list();
					if ( list.length > 0  )
					{
						ftp.changeDir(actFile.getName());
						hasCdUp = true;
						this.upload(dir+seperator+files[i],list);
					}
				}
				else
				{
					log.debug(actFile + "Starting");


					TableItem item = new TableItem(statusTable, SWT.NONE);
					
					//Filename Setup
					Label  txtFilename = new Label (statusTable, SWT.NONE);
					txtFilename.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor0 = new TableEditor(statusTable);
					editor0.grabHorizontal = editor0.grabVertical = true;
					editor0.setEditor(txtFilename, item, 0);
					txtFilename.setText(dir + seperator + files[i]);
					
					//Progress Bar Setup
					//ProgressBar bar = new ProgressBar(statusTable, SWT.NONE);
					//bar.setSelection(1);
					//TableEditor editor = new TableEditor(statusTable);
					//editor.grabHorizontal = editor.grabVertical = true;
					//editor.setEditor(bar, item, 1);
					
					//Image Up/Download setup
					Label  txtStatus = new Label (statusTable, SWT.NONE);
					txtStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor1 = new TableEditor(statusTable);
					editor1.grabHorizontal = editor1.grabVertical = true;
					editor1.setEditor(txtStatus, item, 1);
					txtStatus.setText("Upload");	
					
					
					Label  txtOperation = new Label (statusTable, SWT.NONE);
					txtOperation.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor2 = new TableEditor(statusTable);
					editor2.grabHorizontal = editor2.grabVertical = true;
					editor2.setEditor(txtOperation, item, 2);
					txtOperation.setText("Working");	
					//Image img =  new Image(Display.getDefault(), "icons/download.gif");
					//item.setImage(2,img); 

					/*
					//Amount Transfer Setup
					Label  txtTransferDone = new Label (statusTable, SWT.NONE);
					txtTransferDone.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor3 = new TableEditor(statusTable);
					editor3.grabHorizontal = editor3.grabVertical = true;
					editor3.setEditor(txtTransferDone, item, 3);
					
					//Amount Transfer Rate Setup
					Label  txtTransferRate = new Label (statusTable, SWT.NONE);
					txtTransferRate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor4 = new TableEditor(statusTable);
					editor4.grabHorizontal = editor4.grabVertical = true;
					editor4.setEditor(txtTransferRate, item, 3);
					*/
					
					//Final Size Setup
					Label  txtSize = new Label (statusTable, SWT.NONE);
					txtSize.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor5 = new TableEditor(statusTable);
					editor5.grabHorizontal = editor5.grabVertical = true;
					editor5.setEditor(txtSize, item, 3);
					txtSize.setText(getSize(actFile.length()));

					//Time Left Setup
					
					Label  txtTime = new Label (statusTable, SWT.NONE);
					txtTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor6 = new TableEditor(statusTable);
					editor6.grabHorizontal = editor6.grabVertical = true;
					editor6.setEditor(txtTime, item, 4);
					
					
					
					
					//TODO Add to background
					ftp.uploadFile(actFile);


					/*
					thread = new TransferThread(actFile, ftp);
					thread.start();	
					long oldlength = 0;
					long origsize = actFile.length();

					long oldTime = java.lang.System.currentTimeMillis(); 
					//IrodsDirWorker ftp = IrodsDirWorker.getFtpDirWorker();
				//	IRODSFile  file = ftp.getFile(files[i]);
						while (thread.isDone() == false)
					{
						Thread.sleep(250);
						//File file = new File(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath() + java.io.File.separatorChar + files[i]);
						//long length = ftp.getFileSize(files[i]);
				///
						
						long length = -1;
						// Get the number of bytes in the file
						//log.debug("here: " + counter++ + ", " + length);
					//	if (length > 0)
					//	{
							//aint size = (int) (((float)length / (float) origsize) * 100);
							String tLeft = timeLeft(System.currentTimeMillis(), oldTime,
									(int)length, (int) oldlength, (int) origsize);
							//bar.setSelection(size);
							//txtTransferDone.setText(getSize(length));
							txtTime.setText(tLeft);
					//	}

					}
						txtOperation.setText("Finished");
						//ftp = null;
					//	file = null;
					thread = null;
				//	ftp.uploadFile(actFile);
				  */
					log.debug(actFile + "Finished");
				}
				
			}
			if ( hasCdUp)
				ftp.DirBack();
			
		}
	}


class TransferThread extends Thread
{
	private File theFile = null;
	private boolean done = false;
	private DirWorker theFtp = null;


	public TransferThread(File  file, DirWorker ftp)
	{
		theFtp = ftp;
		theFile = file;
	}

	@Override
	public void run()
	{


		try {
			theFtp.uploadFile(theFile);
			//theFile.copyTo( new LocalFile(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath() ), true );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally 
		{
			done = true;
			theFile = null;
			theFtp = null;
		}
	}

	public boolean isDone() {
		return done;
	}

}
//}
