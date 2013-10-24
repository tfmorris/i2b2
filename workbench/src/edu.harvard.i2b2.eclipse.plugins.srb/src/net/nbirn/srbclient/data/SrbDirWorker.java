package net.nbirn.srbclient.data;

import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import net.nbirn.srbclient.plugin.PluginPlugin;
import net.nbirn.srbclient.utils.ErrorTxt;
import net.nbirn.srbclient.utils.IFolderListener;
import net.nbirn.srbclient.utils.SrbDirectoryTable;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.local.LocalFile;
import edu.sdsc.grid.io.srb.*;

public class SrbDirWorker {

	private static SrbDirWorker instance = null;

	private String homeDirectory = null;
	private String mdasDomainHome = null;
	private String defaultStorageResource = null;

	private String host = null;
	private String user = null;
	private String pass = null;
	private int port = 0;

	private SRBFileSystem conn = null;
	private SRBFile actFileList = null;
	private boolean isChanceled = false;
	private Table statusTable = null;
	private String currentDir = null;
	private TransferThread thread  = null;

	private static final Log log = LogFactory.getLog(SrbDirWorker.class);
	private IFolderListener folderListener = null;
	//private ArrayList<ILogger> ftpLogger = null;

//	private ILogger ftpLogger = null;
	private SrbDirWorker() 
	{
		super();
	}
	public static SrbDirWorker getFtpDirWorker()
	{
		if ( instance == null )
			instance = new SrbDirWorker();
		return instance;
	}
	public void connect()
	{
		try {
			/*
			conn = new FTPClient();

			if ( fTPMessageListener != null )
				conn.setMessageListener(fTPMessageListener);

			conn.setRemoteHost(host);
			LogMsg("Verbindung zu " + host + "wird aufgebaut");
			conn.connect();
			LogMsg("Login zu " + host + "wird ausgeführt");
			conn.login(user,pass);
			LogMsg("Login zu " + host + "ausgeführt");
			UnixFileParser parser = new UnixFileParser();
			parser.setLocale(Locale.ENGLISH);
			conn.setFTPFileFactory(new FTPFileFactory(parser));
			conn.setParserLocale(Locale.ENGLISH);
			conn.setType(FTPTransferType.BINARY);
			 */

			SRBAccount SRBAccount = new SRBAccount(
					host, Integer.valueOf( port ), user, pass,
					homeDirectory, mdasDomainHome, defaultStorageResource) ;

			log.info("Connecting to " + host);
			conn = new SRBFileSystem( SRBAccount );


			currentDir = conn.getHomeDirectory();
			if ( isOnline() )
			{
				log.info("Online with " + host);
				notifyUpdateFileList();
			}
		} catch (IOException e) {
			resetConnection();
			e.printStackTrace();
		}		
	}

	private void resetConnection()
	{
		if ( conn != null )
		{

			try {
				conn.close();
			} catch (IOException e) {

			}
			finally
			{
				conn = null;
				actFileList = null;
				//monitor = null;
				isChanceled = false;
			}
		}


	}

	public GeneralFile[] dir()
	{
		try {
			if ( conn == null )
			{
				connect();
				actFileList = new SRBFile(conn,conn.getHomeDirectory());
				currentDir = conn.getHomeDirectory();
			}
			if ( actFileList == null )
				actFileList = new SRBFile(conn,currentDir);
		} catch (Exception e) {			
			log.debug(e.getMessage());
		}
		return actFileList.listFiles();
	}
	/**
	 * Wechselt eine Verzeichnissebene höher
	 *
	 */
	public void DirBack()
	{
		try {
			/*
			 * todo Fix
			 */

			actFileList = new SRBFile(conn,	actFileList.getAbsolutePath() + actFileList.getPathSeparator() + "..");
			currentDir = actFileList.getAbsolutePath();
			/*
            SRBFile SRBFile = new SRBFile(conn, currentS);
            GeneralFile parentDir = SRBFile.getParentFile();
            LogMsg("***** parentDir.getName is " + parentDir.getName());
            String parentAbsPath = parentDir.getAbsolutePath();

            if ((parentAbsPath.startsWith(getSRBUserHomePrefix()) || parentAbsPath.startsWith(getTrashDirPrefix())) && (new SRBFile(SRBFS, parentAbsPath).list() != null))
            {
                SRBFileBrowser = getSRBFileBrowserByDirPath(SRBFS, parentAbsPath);
                LogMsg("***** parentDir IF absolute path starts with /home or /trash and readable " + parentAbsPath);
            } else {
                SRBFileBrowser = getSRBFileBrowserByDirPath(SRBFS, getHomeDirectory(SRBFS));
                LogMsg("***** parentDir ELSE absolute path does not start with /home or /trash and readable " + parentAbsPath);
            }

			conn.cdup();
			actFileList = conn.dirDetails(.pwd());
			 */
			notifyUpdateFileList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Changes in a directory
	 * @param name is the directory to be changed
	 */
	public void changeDir(String name)
	{
		try {
			if (name.startsWith("/"))
				actFileList = new SRBFile(conn, name);
			else
				actFileList = new SRBFile(conn,currentDir + "/" + name);
			currentDir =  actFileList.getAbsolutePath();

			log.debug("Changes in Directory : " + actFileList.getAbsolutePath());
		} catch (Exception e) {
			log.error(ErrorTxt.ERROR_CHANGING_DIRECTORY + name);
			e.printStackTrace();
		}
	}
	public boolean isDir(String name)
	{
		SRBFile SRBTempFile = new SRBFile(conn, currentDir + "/" + name);
		if (SRBTempFile.isDirectory())
			return true;
		else
			return false;
	}

	public void notifyActionOnFile(String name)
	{
		if ( name.equals(".."))
			DirBack();
		else
			if ( isDir(name) )
			{
				changeDir(name);
			}
		notifyUpdateFileList();

	}
	/**
	 * Creates a new directory on the irods
	 * @param name Name is the directory is be created
	 */
	public void createDir(String name)
	{
		if ( isOnline() )
		{
			try {
				log.debug("Ftp : name " + name + " is provided");
				SRBFile SRBTempFile = new SRBFile(conn, currentDir + "/" + name);
				SRBTempFile.mkdir();
				log.debug("Ftp : name  " + name + " created");
				//currentDir = name;
				updateFileList();
				notifyUpdateFileList();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			log.error("Verzeichniss kann nicht erstellt werden da die Verbindung nicht besteht");
		}
	}
	
	public long getFileSize(String name)
	{
		if ( isOnline() )
		{
			try {
				log.debug("Ftp : name " + name + " is provided");
				SRBFile SRBTempFile = new SRBFile(conn, currentDir + "/" + name);
			return 	SRBTempFile.length();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public SRBFile getFile(String name)
	{
		//if ( isOnline() )
		//{
			try {
				log.debug("Ftp : name " + name + " is provided");
				return  new SRBFile(conn, currentDir + "/" + name);

			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
		return null;
	}
	
	/**
	 * Checks whether a directory exists. 
	 * 
	 * @param name Name is the directory to check
	 * @return true if it exists
	 * 		   false if it does not exist
	 */
	public boolean checkDir(String name)
	{
		GeneralFile[] dirList = null;
		try {
			SRBFile SRBTempFile = new SRBFile(conn, name);
			if ( (dirList = SRBTempFile.listFiles()) != null )
			{
				for (int i = 0; i < dirList.length; i++) {
					if ( dirList[i].isDirectory())
					{
						if ( dirList[i].getName().equals(name))
							return true; //Verzeichniss vorhanden
					}
				}
				return false;	//Verzeichniss nicht vorhanden
			}
			else
			{
				return false;
			}
		} catch (Exception e) {
			log.error(ErrorTxt.IO_EXCEPTION);
			e.printStackTrace();
		}
		return false;  //Sollte nicht vorkommen

	}
	/**
	 * Check to see if dirctory is empty
	 * 
	 * @param name Name is the directory to check
	 * @return null if it does not have any content
	 * 		   List of files in directory
	 */
	public GeneralFile[] checkDirEmpty(String name)
	{

		try {
			SRBFile SRBTempFile = new SRBFile(conn, name);

			GeneralFile[] files = SRBTempFile.listFiles();
			if ( files.length == 0 )
			{
				return null;
			}
			else
				return files;
			//TODO Fehlerausgabe
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;  //Sollte nicht vorkommen



	}

	public boolean checkFile(String name)
	{
		int i = 0;
		GeneralFile[] dirList = null;
		try {
			SRBFile SRBTempFile = new SRBFile(conn, name);

			return SRBTempFile.isDirectory();

			/*(
			if ( (dirList = conn.dirDetails(conn.pwd())) != null )
			{
				for (i = 0; i < dirList.length; i++) {
					if ( dirList[i].isDir() == false)
					{
						if ( dirList[i].getName().equals(name))
							return true; 
					}
				}
				return false;	
			}
			else
			{
				return false;
			}
			 */
		} catch (Exception e) {
			log.error("Error in check file " + dirList[i] + " at " +ErrorTxt.IO_EXCEPTION);
			e.printStackTrace();
		}
		return false;  //Sollte nicht vorkommen
	}

	/**
	 * Delete a directory 
	 * 
	 * @param name Name of top level to delete
	 */
	public void deleteDirs(String[] names)
	{
		if ( isOnline() )
		{

			for (int i = 0; i < names.length; i++) {
				try {
					SRBFile SRBTempFile = new SRBFile(conn, currentDir + "/" + names[i]);

					SRBTempFile.delete();
					/*
					if ( isDir(names[i]))
					{
						FTPFile[] files = checkDirEmpty(names[i]);
						if ( files != null )
						{
							retVal = folderListener.notifyRemovingNoEmtyDir(names[i]);
							if ( retVal == folderListener.REMOVE_DIR || retVal == folderListener.REMOVE_DIR_AND_SAVE_ANSWER )
							{
								changeDir(names[i]);
								deleteDirs(files);
								DirBack();
							}
					    }
						conn.rmdir(names[i]);

					}
					else
					{
						conn.delete(names[i]);
					}
					 */
				} catch (Exception e) {
					log.error("Error in delete file " + names[i] + ". " + ErrorTxt.IO_EXCEPTION);
					e.printStackTrace();
				}
			}
			updateFileList();
			notifyUpdateFileList();

		}
	}


	private void updateFileList()
	{
		try {
			actFileList = new SRBFile(conn, currentDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Check if a connect is alive
	 * @return true if connected
	 * @return false if not connected
	 */
	public boolean isOnline()
	{
		if ( conn == null )
		{
			log.error("Not connected.");
			return false;
		}
		return conn.isConnected();
	}
	/**
	 * Download a list of files
	 * @param folder folder of path
	 * @param name the filelist
	 * @param txfStatusTable That status of transfer
	 * @throws InterruptedException 
	 * 	
	 */
	public void downloadFiles(String folder,SrbDirectoryTable name, Table txfStatusTable) throws InterruptedException
	{
		isChanceled = false;
		//Display.getDefault().asyncExec(new DownloadThread(actFileList.getAbsolutePath(),name, txfStatusTable));
		//	DownloadThread thread = new DownloadThread(actFileList.getAbsolutePath(),name, txfStatusTable);
//		thread.run();
		DownloadThread(actFileList.getAbsolutePath(),name, txfStatusTable);
		run();

	}

	public String getCurrentDir()
	{
		return actFileList.getAbsolutePath() ;
	}


	public void uploadFile(File f)
	{



		actFileList = new SRBFile(conn, currentDir);

		GeneralFile source = null;



		log.debug("File " + f.getName() + " uploaded");
		try {
			//monitor.beginTask(f.getName() + " on the server side ",(int) f.length());
			//actFileList.copyTo( new LocalFile(f.getAbsoluteFile()), true );


			source = new LocalFile(f.getAbsoluteFile());
			source.copyTo( actFileList, true);

			//TODO add upload
			//conn.setProgressMonitor(monitor);
			//conn.put(ClientDirWorker.getClientDir().getFileStream(f),f.getName());
			updateFileList();
			notifyUpdateFileList();

		} catch (Exception e) {
			log.error(ErrorTxt.ERROR_UPLOADING_FILE + f.getName());
			e.printStackTrace();
		}
	}
	public void stop()
	{
		//TODO: conn.cancelTransfer();
		isChanceled = true;
	}


	public void addClientFolderListener(IFolderListener listener)
	{
		folderListener = listener;
	}

	public void notifyUpdateFileList()
	{
		log.debug("notifyUpdateFileList");
		if ( folderListener  != null )
		{
			PluginPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable(){

				public void run() {
					//TODO Causes local list to refresh, deterine better way
					//folderListener.updateDirectoryList();
				}});
		}

	}


	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}
	public void setDefaultStorageResource(String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}
	public String getHomeDirectory() {
		return homeDirectory;
	}
	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}
	public String getMdasDomainHome() {
		return mdasDomainHome;
	}
	public void setMdasDomainHome(String mdasDomainHome) {
		this.mdasDomainHome = mdasDomainHome;
	}
	public int getPort() {
		return port;
	}
	public void setPort(String port) {
		try {
			this.port = Integer.parseInt(port);
		} catch (Exception e)
		{}
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	public void disconnect() {
		if ( conn != null )
			try {
				log.info("Connection being terminated");
				conn.close();
				log.info("Connection was terminated");

			} catch (IOException e) {
				log.error(ErrorTxt.IO_EXCEPTION);
				log.error(ErrorTxt.ERROR_BY_DISCONNECTING);
				e.printStackTrace();
			}
	}


	class TransferThread extends Thread
	{
		private SRBFile theFile = null;
		private boolean done = false;

		public TransferThread(SRBFile file)
		{
			theFile = file;
		}

		@Override
		public void run()
		{
			try {
				theFile.copyTo( new LocalFile(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath() ), true );
				done = true;
				theFile = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public boolean isDone() {
			return done;
		}

	}

//	class DownloadThread //extends Thread 
//	{
	private SrbDirWorker ftp = null;
	private String theFirstFolder = null;
	private String[] theFirstFiles = null;
	private TableItem[] theItems = null;
	//private Table statusTable = null;
	//private SrbDirectoryTable theDirectoryTable = null;

	public void  DownloadThread(String folder,SrbDirectoryTable srbDirectoryTable, Table txfStatusTable)
	{
		log.debug("New download thread...");
		theFirstFolder = folder;
		//theDirectoryTable = srbDirectoryTable;

		theFirstFiles = srbDirectoryTable.getSelectionFiles();
		theItems = srbDirectoryTable.getDirTable().getSelection();
		//statusTable = txfStatusTable;
	}
	public void run() throws InterruptedException
	{
		ftp = SrbDirWorker.getFtpDirWorker();
		log.debug("Starting download");
		downloadFiles(theFirstFolder,theFirstFiles, theItems);
		log.debug("Finished download");

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
		int bytesCopied = currentCopiedBytes - oldCopiedBytes;
		int bytesRemaining = size - currentCopiedBytes;
		String format = "";
		try
		{
			int timeRemaining = timeDiff*bytesCopied/bytesRemaining;
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
			format = "DONE";
		} 
		return format;
	}

	public void downloadFiles(String folder,String[] name, TableItem[] items) throws InterruptedException
	{
		String actDirOnClient = folder;

		if ( actDirOnClient == null )
			actDirOnClient = "";

		for (String actFileName : name) {
			if ( isChanceled == true )
			{
				//interrupt();
				return;
			}

			SRBFile ftpFile = new SRBFile(conn, actFileName);

			if ( ftpFile == null )
			{
				log.error(ErrorTxt.ERROR_DOWNLOADING_FILE_OR_DIR + actFileName);
				return;
			}
			else
			{
				if ( ftpFile.isDirectory())
				{
					if ( ClientDirWorker.getClientDir().createDir(actDirOnClient + "/" + ftpFile.getName()))
					{
						try {
							ftp.changeDir(ftpFile.getName());
							if ( ftp.actFileList != null )
							{
								this.downloadFiles(actDirOnClient + "/" + ftpFile.getName(),ftpFile.list(),theItems);
								DirBack();
							}
						} catch (Exception e) {
							log.error(ErrorTxt.IO_EXCEPTION);
							log.error(ErrorTxt.ERROR_LISTING_DIR_BY_FTP);
							e.printStackTrace();
						}
					}
				}
				else
				{

					TableItem item = new TableItem(statusTable, SWT.NONE);

					//Filename Setup
					Label  txtFilename = new Label (statusTable, SWT.NONE);
					txtFilename.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor0 = new TableEditor(statusTable);
					editor0.grabHorizontal = editor0.grabVertical = true;
					editor0.setEditor(txtFilename, item, 0);
					txtFilename.setText(folder + "/" + items[0].getText(0));

					//Progress Bar Setup
					ProgressBar bar = new ProgressBar(statusTable, SWT.NONE);
					bar.setSelection(1);
					TableEditor editor = new TableEditor(statusTable);
					editor.grabHorizontal = editor.grabVertical = true;
					editor.setEditor(bar, item, 1);

					//Image Up/Download setup
					Image img =  new Image(Display.getDefault(), "icons/upload.gif");
					item.setImage(2,img); 

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

					//Final Size Setup
					Label  txtSize = new Label (statusTable, SWT.NONE);
					txtSize.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor5 = new TableEditor(statusTable);
					editor5.grabHorizontal = editor5.grabVertical = true;
					editor5.setEditor(txtSize, item, 5);
					txtSize.setText(getSize(ftpFile.length()));

					//Time Left Setup
					Label  txtTimeLeft = new Label (statusTable, SWT.NONE);
					txtTimeLeft.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor6 = new TableEditor(statusTable);
					editor6.grabHorizontal = editor6.grabVertical = true;
					editor6.setEditor(txtTimeLeft, item, 6);



					thread = new TransferThread(ftpFile);
					thread.start();	
					int counter = 0;
					long oldlength = 0;
					long origsize = ftpFile.length();

					long oldTime = java.lang.System.currentTimeMillis(); 

					while (thread.isDone() == false)
					{
						Thread.sleep(250);
						File file = new File(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath() + java.io.File.separatorChar + actFileName);
						// Get the number of bytes in the file
						log.debug("here: " + counter++);
						if (file.exists())
						{
							long length = file.length();

							int size = (int) (((float)length / (float) origsize) * 100);
							String tLeft = timeLeft(System.currentTimeMillis(), oldTime,
									(int)length, (int) oldlength, (int) origsize);
							bar.setSelection(size);
							txtTransferDone.setText(getSize(length));
							txtTimeLeft.setText(tLeft);
						}

					}
					bar.setSelection(100);
					thread = null;
				}
			}
		}
		if ( actDirOnClient == "" )
		{
			//monitor.done();
			ClientDirWorker.getClientDir().notifyUpdateFileList();
		}

	}
	public void setTable(Table txfStatusTable) {
		// TODO Auto-generated method stub
		statusTable = txfStatusTable;
	}

}
