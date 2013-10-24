package net.nbirn.srbclient.data;

import java.io.File;
import java.io.IOException;
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
import net.nbirn.srbclient.utils.SrbDirectoryTable;

import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.local.LocalFile;
import edu.sdsc.grid.io.irods.*;

public class IrodsDirWorker implements DirWorker {

	private static IrodsDirWorker instance = null;

	private String homeDirectory = null;
	private String mdasDomainHome = null;
	private String defaultStorageResource = null;

	private String host = null;
	private String user = null;
	private String pass = null;
	private int port = 0;

	private IRODSFileSystem conn = null;
	private IRODSFile actFileList = null;
	private boolean overwrite = true;
	private boolean isChanceled = false;
	private Table statusTable = null;
	private String currentDir = null;
	private TransferThread thread  = null;

	private static final Log log = LogFactory.getLog(IrodsDirWorker.class);
	private IFolderListener folderListener = null;

	//private IrodsDirWorker() 
	//{
	//	super();
	//}
	public  IrodsDirWorker getFtpDirWorker()
	{
		if ( instance == null )
			instance = new IrodsDirWorker();
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

			IRODSAccount IRODSAccount = new IRODSAccount(
					host, Integer.valueOf( port ), user, pass,
					homeDirectory, mdasDomainHome, defaultStorageResource) ;

			log.info("Connecting to " + host);
			conn = new IRODSFileSystem( IRODSAccount );


			currentDir = conn.getHomeDirectory();
			actFileList = new IRODSFile(conn,conn.getHomeDirectory());

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
				actFileList = new IRODSFile(conn,conn.getHomeDirectory());
				currentDir = conn.getHomeDirectory();
			}
			if ( actFileList == null )
				actFileList = new IRODSFile(conn,currentDir);
		} catch (Exception e) {			
			log.error(e.getMessage());
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

			actFileList = new IRODSFile(conn,	actFileList.getAbsolutePath() + actFileList.getPathSeparator() + "..");
			currentDir = actFileList.getAbsolutePath();
			/*
            IRODSFile IRODSFile = new IRODSFile(conn, currentS);
            GeneralFile parentDir = IRODSFile.getParentFile();
            LogMsg("***** parentDir.getName is " + parentDir.getName());
            String parentAbsPath = parentDir.getAbsolutePath();

            if ((parentAbsPath.startsWith(getIRODSUserHomePrefix()) || parentAbsPath.startsWith(getTrashDirPrefix())) && (new IRODSFile(IRODSFS, parentAbsPath).list() != null))
            {
                IRODSFileBrowser = getIRODSFileBrowserByDirPath(IRODSFS, parentAbsPath);
                LogMsg("***** parentDir IF absolute path starts with /home or /trash and readable " + parentAbsPath);
            } else {
                IRODSFileBrowser = getIRODSFileBrowserByDirPath(IRODSFS, getHomeDirectory(IRODSFS));
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
				actFileList = new IRODSFile(conn, name);
			else
				actFileList = new IRODSFile(conn,currentDir + "/" + name);
			currentDir =  actFileList.getAbsolutePath();

			log.debug("Changes in Directory : " + actFileList.getAbsolutePath());
		} catch (Exception e) {
			log.error(ErrorTxt.ERROR_CHANGING_DIRECTORY + name);
			e.printStackTrace();
		}
	}
	public boolean isDir(String name)
	{
		IRODSFile IRODSTempFile = new IRODSFile(conn, currentDir + "/" + name);
		if (IRODSTempFile.isDirectory())
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
				IRODSFile IRODSTempFile = new IRODSFile(conn, currentDir + "/" + name);
				boolean result = IRODSTempFile.mkdir();
				if (result)
					log.debug("Irods : name  " + name + " created");
				else
					log.error("Irods : name  " + name + " was not created");
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
				IRODSFile IRODSTempFile = new IRODSFile(conn, currentDir + "/" + name);
			return 	IRODSTempFile.length();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public IRODSFile getFile(String name)
	{
		//if ( isOnline() )
		//{
			try {
				log.debug("Ftp : name " + name + " is provided");
				return  new IRODSFile(conn, currentDir + "/" + name);

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
			IRODSFile IRODSTempFile = new IRODSFile(conn, name);
			if ( (dirList = IRODSTempFile.listFiles()) != null )
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
			IRODSFile IRODSTempFile = new IRODSFile(conn, name);

			GeneralFile[] files = IRODSTempFile.listFiles();
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
			IRODSFile IRODSTempFile = new IRODSFile(conn, name);

			return IRODSTempFile.isDirectory();

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
					IRODSFile IRODSTempFile = new IRODSFile(conn, currentDir + "/" + names[i]);

					IRODSTempFile.delete();
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
			actFileList = new IRODSFile(conn, currentDir);
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
	public void downloadFiles(String folder,SrbDirectoryTable name, Table txfStatusTable) //throws Exception
	{
		isChanceled = false;
		//Display.getDefault().asyncExec(new DownloadThread(actFileList.getAbsolutePath(),name, txfStatusTable));
		//	DownloadThread thread = new DownloadThread(actFileList.getAbsolutePath(),name, txfStatusTable);
//		thread.run();
		DownloadThread(actFileList.getAbsolutePath(),name, txfStatusTable);
		try {
		run();
		} catch (Exception e)
		{
			log.error(e.getMessage());
		}

	}

	public String getCurrentDir()
	{
		if (actFileList != null)
			return actFileList.getAbsolutePath() ;
		return null;
	}

	public String getURI(String f)
	{

		//srb:// [ userName . domainHome [ : password ] @ ] host [ : port ][ / path ][ ? query ]

		StringBuffer sb = new StringBuffer();
		sb.append("irod://");
		sb.append(getUser());
		sb.append(".");
		sb.append(getMdasDomainHome());
		sb.append("@");
		sb.append(getHost());
		sb.append(":");
		sb.append(getPort());
		
		if (f.startsWith(conn.getHomeDirectory()))

			{
			sb.append(f);
			}
		else if (f.startsWith("/"))
			{
			sb.append(conn.getHomeDirectory());
			sb.append(f);
			}
		else
		{
			sb.append(conn.getHomeDirectory());
			sb.append("/");
			sb.append(f);
		}
			
		return sb.toString();

	}
	
	

	public String downloadFile(String uriLoc, String destDir, String storageResource, String password)
 {
		// Convert to filesystem URL does not seem to work

		
		String username = uriLoc.substring(uriLoc.indexOf('/') + 2, uriLoc.indexOf('.'));
		String mdas = uriLoc.substring(uriLoc.indexOf('.') + 1, uriLoc.indexOf('@'));
		String host = uriLoc.substring(uriLoc.indexOf('@') + 1, uriLoc.indexOf(':',5));
		int port = Integer.parseInt( uriLoc.substring(uriLoc.indexOf(':',5) + 1, uriLoc.indexOf('/', 7)));
		String file = uriLoc.substring(uriLoc.indexOf('/', 7));

		

		
		IRODSAccount irodsAccount = new IRODSAccount(
				host, port, username, password,
				"/", mdas, storageResource) ;


			
			
		GeneralFile source = null;
		try {
			IRODSFileSystem irodsFileSystem = new IRODSFileSystem(irodsAccount);

			//URI uri = new URI( uriLoc ); 
			//source = FileFactory.newFile( uri, password);
			source = new IRODSFile(irodsFileSystem, file);
			if (destDir == null)
				source.copyTo( new LocalFile(source.getName() ), true );
			else
				source.copyTo( new LocalFile(destDir + java.io.File.separator +source.getName() ), true );
			irodsFileSystem.close();
		} catch (IOException ioe) {
			log.error("========== IOException from SrbService: downloadTheFile " + ioe.getMessage());
			return null;
		}
		if (source == null)
			return null;
		if (destDir == null)
			return source.getName();
		else
			return destDir + java.io.File.separator + source.getName();
	}

	public void uploadFile(String uri, File f)
	{
		//actFileList = new IRODSFile(conn, currentDir);

		GeneralFile source = null;

		//FileFactory.newFile(arg0)


		log.debug("File " + f.getName() + " uploaded");
		try {
			//monitor.beginTask(f.getName() + " on the server side ",(int) f.length());
			//actFileList.copyTo( new LocalFile(f.getAbsoluteFile()), true );

			String file = uri.substring(uri.indexOf('/', 7));

			source = new LocalFile(f.getAbsoluteFile());
			source.copyTo(new IRODSFile(conn,file), overwrite);
			
			//source.copyTo( FileFactory.newFile( new URI( uri ) , getPass()));

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
	
	public void uploadFile(File f)
	{



		actFileList = new IRODSFile(conn, currentDir);

		GeneralFile source = null;



		log.debug("File " + f.getName() + " uploaded");
		try {
			//monitor.beginTask(f.getName() + " on the server side ",(int) f.length());
			//actFileList.copyTo( new LocalFile(f.getAbsoluteFile()), true );


			source = new LocalFile(f.getAbsoluteFile());
			source.copyTo( actFileList, overwrite);

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
	public char getPathChar(){
		return '/';
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
		private IRODSFile theFile = null;
		private boolean done = false;

		public TransferThread(IRODSFile file)
		{
			theFile = file;
		}

		@Override
		public void run()
		{
			try {
				LocalFile lf =  new LocalFile(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath()) ;
				theFile.copyTo(lf, true );
				//theFile = null;
				done = true;
			} catch (IOException e) {
				log.error(e.getMessage());
				// TODO Auto-generated catch block
				done = true;
			}
			done = true;
		}

		public boolean isDone() {
			return done;
		}

	}

//	class DownloadThread //extends Thread 
//	{
	private IrodsDirWorker ftp = null;
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
	public void run() throws Exception
	{
//		ftp = IrodsDirWorker.getFtpDirWorker();
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

	public void downloadFiles(String folder,String[] name, TableItem[] items) throws Exception
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

			IRODSFile ftpFile = null;
			
			if (folder == null)
				ftpFile = new IRODSFile(conn, actFileName);
			else
				ftpFile = new IRODSFile(conn, folder + "/" + actFileName);


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
					/*
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
					 */
					

					//Image Up/Download setup
					Label  txtStatus = new Label (statusTable, SWT.NONE);
					txtStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor1 = new TableEditor(statusTable);
					editor1.grabHorizontal = editor1.grabVertical = true;
					editor1.setEditor(txtStatus, item, 1);
					txtStatus.setText("Working");	
					
					
					Label  txtOperation = new Label (statusTable, SWT.NONE);
					txtOperation.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor2 = new TableEditor(statusTable);
					editor2.grabHorizontal = editor2.grabVertical = true;
					editor2.setEditor(txtOperation, item, 2);
					txtOperation.setText("Download");	
					
					//Final Size Setup
					Label  txtSize = new Label (statusTable, SWT.NONE);
					txtSize.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor5 = new TableEditor(statusTable);
					editor5.grabHorizontal = editor5.grabVertical = true;
					editor5.setEditor(txtSize, item, 3);
					txtSize.setText(getSize(ftpFile.length()));

					//Time Left Setup
					Label  txtTime = new Label (statusTable, SWT.NONE);
					txtTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor6 = new TableEditor(statusTable);
					editor6.grabHorizontal = editor6.grabVertical = true;
					editor6.setEditor(txtTime, item, 4);
					
					

					if (ftpFile.canRead()) {
					
					LocalFile lf =  new LocalFile(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath()) ;
					ftpFile.copyTo(lf );

					File file = lf.getFile();
				log.debug(file.getAbsolutePath());
					}
			        //file = FileFactory.newFile( fileSystem, TEST_DIR );
			        //localFile = new LocalFile( fakeName(LOCAL_TEST_FILE) );
			        //file.copyTo( localFile );
					/*
					thread = new TransferThread(ftpFile);
					thread.start();	
					long oldlength = 0;
					//long origsize = ftpFile.length();

					long oldTime = java.lang.System.currentTimeMillis(); 

					while (thread.isDone() == false && thread.isAlive())
					{
						Thread.sleep(250);
						//File file = new File(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath() + java.io.File.separatorChar + actFileName);
						// Get the number of bytes in the file
						//log.debug("here: " + counter++ + thread.isAlive());
						//if (file.exists())
						//{

							//bar.setSelection(size);
							
						int origsize = -1;
						int length = -1;
						String tLeft = timeLeft(System.currentTimeMillis(), oldTime,
								length, (int) oldlength, origsize);
						//bar.setSelection(size);
						//txtTransferDone.setText(getSize(length));
						txtTime.setText(tLeft);

						

					}
				*/
					txtStatus.setText("Finished");
					//File file = new File(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath() + java.io.File.separatorChar + actFileName);
					//txtSize.setText(getSize(file.length()));
					//	bar.setSelection(100);
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
	public void rename(String from, String to) {
		// TODO Auto-generated method stub
		
	}


}
