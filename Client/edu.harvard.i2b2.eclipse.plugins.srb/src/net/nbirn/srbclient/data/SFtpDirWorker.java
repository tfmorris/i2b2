package net.nbirn.srbclient.data;


import java.io.File;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;


import net.nbirn.srbclient.plugin.PluginPlugin;
import net.nbirn.srbclient.plugin.views.ProgressMonitor;
import net.nbirn.srbclient.utils.ErrorTxt;
import net.nbirn.srbclient.utils.IFolderListener;
import net.nbirn.srbclient.utils.SrbDirectoryTable;


public class SFtpDirWorker implements DirWorker {

	private static SFtpDirWorker instance = null;

	private String homeDirectory = null;
	//private String mdasDomainHome = null;
	//private String defaultStorageResource = null;

	private String host = null;
	private String user = null;
	private String pass = null;
	private char dirSeperator = '/';
	private int port = 0;
	private ProgressMonitor monitor = null;

	private Session session = null;
	private ChannelSftp conn = null;
	private Vector actFileList = null;
	private boolean overwrite = true;
	private boolean isChanceled = false;
	private Table statusTable = null;
	private String currentDir = null;
	//private TransferThread thread  = null;

	private static final Log log = LogFactory.getLog(SFtpDirWorker.class);
	private IFolderListener folderListener = null;

	//private FtpDirWorker() 
	//{
	//	super();
	//}
	public  SFtpDirWorker getFtpDirWorker()
	{
		if ( instance == null )
			instance = new SFtpDirWorker();
		return instance;
	}
	public void connect()
	{
		try {
			JSch jsch = new JSch();
			session=jsch.getSession(user, host, port);




			session.setUserInfo(new MyUserInfo(pass));


			//enable compression
			java.util.Properties config=new java.util.Properties();
			//config.put("StrictHostKeyChecking", "no");
			config.put("compression.s2c", "zlib,none");
			config.put("compression.c2s", "zlib,none");
			config.put("PreferredAuthentications","password");
			session.setConfig(config);     

			session.connect();


			Channel channel=session.openChannel("sftp");
			channel.connect();
			conn=(ChannelSftp)channel;

			currentDir = getCurrentDir(); //.getHomeDirectory();

			/*
			if (conn..system().toUpperCase().startsWith("WINDOWS"))
				dirSeperator = '\\';
			else
				dirSeperator = '/';
			 */
			//dirSeperator = conn.actFileList.getPathSeparatorChar(); //conn.getHomeDirectory().charAt(0);
			if ( isOnline() )
			{
				log.info("Online with " + host);
				notifyUpdateFileList();
			}

		} catch (Exception e) {
			//resetConnection();
			log.error("Error Connecting to " + host);
			log.debug(e.getMessage());
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
			conn.quit();

			conn = null;
			//actFileList = null;
			isChanceled = false;
		}


	}

	private  void isConnected() {
		if (( conn == null ) || (conn.isConnected() == false))
			connect();
		
	}

	public Object[] dir()
	{
		isConnected();
		//TODO need to impletment by own one
		//FTPFile[] listO = null;
		try {
			//listO =  conn.dirDetails(conn.pwd());   //actFileList.getFTPClient().list("/");//actFileList.getPath());
			actFileList = conn.ls(conn.pwd());
		}catch (Exception e)
		{
			log.error("Error getting directory ");
			log.debug(e.getMessage());
		}


		return actFileList.toArray();  //actFileList.listFiles();
	}


	public void DirBack()
	{		isConnected();

	try {


		conn.cd("..");
		actFileList = conn.ls(conn.pwd());
		notifyUpdateFileList();
	} catch (Exception e) {
		log.error("Error changing directory back (cd..) ");
		log.debug(e.getMessage());
	}
	}

	public void changeDir(String name)
	{
		isConnected();

		try {
			conn.cd(name);
			actFileList = conn.ls(conn.pwd());
		} catch (Exception e) {
			log.error(ErrorTxt.ERROR_CHANGING_DIRECTORY + name);
			log.debug(e.getMessage());
		}
	}
	public boolean isDir(String name)
	{
		
		for ( int i = 0 ; i < actFileList.size() ; i++)
		{
			LsEntry lsen = (LsEntry) actFileList.elementAt(i);

			
			if ( lsen.getFilename().equals(name))
				return lsen.getAttrs().isDir();
		}
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


	public void createDir(String name)
	{
		isConnected();

		if ( isOnline() && name != null && name.length() > 0)
		{

			try {
				if (name.charAt(0) != (dirSeperator))
				{
					name = conn.pwd() + dirSeperator + name;
				}
				log.debug("Ftp : name " + name + " is provided");
				conn.mkdir(name);
				//currentDir = name;
				updateFileList();
				notifyUpdateFileList();
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		else
		{
			log.error("Currently not logged on");
		}
	}

	public long getFileSize(String name)
	{
		isConnected();

		if ( isOnline() )
		{
			try {
				log.debug("Ftp : name " + name + " is provided");
				//FTPFile FTPTempFile = conn.fileDetails(name);
				//return 	FTPTempFile.size();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/*
	public FTPFile getFile(String name)
	{
		try {
			log.debug("Ftp : name " + name + " is provided");
			return  new FTPFile(currentDir + dirSeperator + name);

		} catch (Exception e) {
			e.printStackTrace();
		}
		//}
		return null;
	}

	public boolean checkDir(String name)
	{
		FTPFile[] dirList = null;
		try {
			FTPFile FTPTempFile = new FTPFile(name);
			if ( (dirList = FTPTempFile.listFiles()) != null )
			{
				for (int i = 0; i < dirList.length; i++) {
					if ( dirList[i].isDir())
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
		} catch (Exception e) {
			log.error(ErrorTxt.IO_EXCEPTION);
			e.printStackTrace();
		}
		return false;  

	}
	 */

	/*
	public FTPFile[] checkDirEmpty(String name)
	{

		try {
			FTPFile[] files = conn.dirDetails(name);
			if ( files.length == 0 )
			{
				return null;
			}
			else
				return files;
			//TODO Fehlerausgabe
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;  //Sollte nicht vorkommen

	}
	 */
	public boolean checkFile(String name)
	{
		int i = 0;
		try {

			return conn.lstat(name).isDir();

		} catch (Exception e) {
			log.error("Error in check file " + name + " at " +ErrorTxt.IO_EXCEPTION);
			e.printStackTrace();
		}
		return false;  //Sollte nicht vorkommen
	}


	public void deleteDirs(String[] names)
	{

		isConnected();
		int retVal = -1;
		if ( isOnline() )
		{
			for (int i = 0; i < names.length; i++) {
				try {
					if ( isDir(names[i]))
					{
						//TODO
						/*
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
						 */
						conn.rm(names[i]);

					}
					else
					{
						conn.rm(names[i]);
					}

				} catch (Exception e) {
					log.error("Error in delete file " + names[i] + ". " + ErrorTxt.IO_EXCEPTION);
				} 
			}
			/*
			for (int i = 0; i < names.length; i++) {
				try {
					conn.delete(names[i]);
					//FTPFile FTPTempFile = new FTPFile(conn, currentDir + dirSeperator + names[i]);

					//FTPTempFile.delete();

				} catch (Exception e) {
					log.error("Error in delete file " + names[i] + ". " + ErrorTxt.IO_EXCEPTION);
					e.printStackTrace();
				}
			}
			 */
			updateFileList();
			notifyUpdateFileList();

		}
	}

	/*
	private void deleteDirs(FTPFile[] names)
	{
		if ( isOnline() )
		{
			for (int i = 0; i < names.length; i++) {
				try {
					if ( isDir(names[i].getName()))
					{

						FTPFile[] files = checkDirEmpty(names[i].getName());
						if ( files != null )
						{
							changeDir(names[i].getName());
							deleteDirs(files);
							DirBack();
						}
						conn.rmdir(names[i].getName());
					}
					else
					{
						conn.delete(names[i].getName());
					}

				} catch (IOException e) {
					log.error("Error in delete file " + names[i] + ". " + ErrorTxt.IO_EXCEPTION);
				} 
			}
			updateFileList();
			notifyUpdateFileList();

		}
	}
	 */

	private void updateFileList()
	{
		try {
			actFileList = conn.ls(conn.pwd());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isOnline()
	{
		if ( conn == null )
		{
			log.error("Not connected.");
			return false;
		}
		return conn.isConnected();
	}

	public void downloadFiles(String folder,SrbDirectoryTable name, Table txfStatusTable) //throws Exception
	{
		isChanceled = false;
		try {
			DownloadThread(folder,name, txfStatusTable);
			run();
		} catch (Exception e)
		{
			log.error(e.getMessage());
		}

	}

	public String getCurrentDir()
	{
		isConnected();
		try {
			return conn.pwd() ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		try {
			if (f.startsWith(conn.pwd()))//.getHomeDirectory()))

			{
				sb.append(f);
			}
			else if (f.startsWith("/"))
			{
				sb.append(conn.pwd());//.getHomeDirectory());
				sb.append(f);
			}
			else
			{
				sb.append(conn.pwd());//.getHomeDirectory());
				sb.append("/");
				sb.append(f);
			}
		} catch (Exception e)
		{
			log.equals(e.getMessage());
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

		try {

			JSch jsch = new JSch();
			Session lsession=jsch.getSession(username, host, port);


			lsession.setPassword(password);

			lsession.connect();

			Channel channel=lsession.openChannel("sftp");
			channel.connect();
			ChannelSftp connsftp=(ChannelSftp)channel;

			connsftp.get(destDir, file);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return destDir + java.io.File.separator + file;		


	}

	public void uploadFile(String uri, File f)
	{


		log.debug("File " + f.getName() + " uploaded");
		try {
			//monitor.beginTask(f.getName() + " on the server side ",(int) f.length());
			//actFileList.copyTo( new LocalFile(f.getAbsoluteFile()), true );

			String file = uri.substring(uri.indexOf('/', 7));
			conn.put(f.getAbsolutePath() + File.separator + f.getName(), file);


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

		String actDirOnClient = currentDir;


		//try {
		//	connect();
		//	actFileList = conn.fileDetails(actDirOnClient + dirSeperator + f.getName());
		//} catch (Exception e1) {
		// TODO Auto-generated catch block
		//	e1.printStackTrace();
		//}

		//GeneralFile source = null;



		log.info("File " + f.getName() + " uploaded");
		try {
			//monitor.beginTask(f.getName() + " on the server side ",(int) f.length());
			//actFileList.copyTo( new LocalFile(f.getAbsoluteFile()), true );

			//String file = uri.substring(uri.indexOf('/', 7));
			//conn.put(f.getAbsolutePath() + File.separator + f.getName(),  f.getName());
			//conn.setProgressMonitor(monitor)
			conn.put(ClientDirWorker.getClientDir().getFileStream(f),f.getName());


			//source = new LocalFile(f.getAbsoluteFile());
			//source.copyTo( actFileList, overwrite);

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
		return null;
	}
	public void setDefaultStorageResource(String defaultStorageResource) {
		//this.defaultStorageResource = defaultStorageResource;
	}
	public String getHomeDirectory() {
		return homeDirectory;
	}
	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}
	public String getMdasDomainHome() {
		return null;
	}
	public void setMdasDomainHome(String mdasDomainHome) {
		//this.mdasDomainHome = mdasDomainHome;
	}
	public int getPort() {
		return port;
	}
	public void setPort(String port) {
		try {
			this.port = Integer.parseInt(port);
		} catch (Exception e)
		{
			this.port = 22;

		}
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
				conn.quit();
				log.info("Connection was terminated");

			} catch (Exception e) {
				log.error(ErrorTxt.IO_EXCEPTION);
				log.error(ErrorTxt.ERROR_BY_DISCONNECTING);
				e.printStackTrace();
			}
	}


	protected SftpATTRS getFtpFile(String name)
	{
		try {
			return conn.lstat(name);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	class DownloadThread extends Thread 
	{
		private SFtpDirWorker ftp = null;
		private String theFirstFolder = null;
		private String[] theFirstFiles = null;



		public DownloadThread(SFtpDirWorker patentftp, String folder,String[] name)
		{
			theFirstFolder = folder;
			theFirstFiles = name;
			ftp = patentftp;
		}
		@Override
		public void run()
		{
			//ftp = getFtpDirWorker();//FtpDirWorker.getFtpDirWorker();
			downloadFiles(theFirstFolder,theFirstFiles);
			log.debug("Ende run");

		}

		public void downloadFiles(String folder,Object[] name)
		{
			String actDirOnClient = folder;

			if ( actDirOnClient == null )
				actDirOnClient = ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath();

			for (String actFileName : (String[])name) {
				if ( isChanceled == true )
				{
					interrupt();
					return;
				}
				SftpATTRS ftpFile = ftp.getFtpFile(actFileName);
				if ( ftpFile == null )
				{
					SFtpDirWorker.log.error(ErrorTxt.ERROR_DOWNLOADING_FILE_OR_DIR + actFileName);
					return;
				}
				else
				{
					if ( ftpFile.isDir())
					{
						if ( ClientDirWorker.getClientDir().createDir(actDirOnClient + dirSeperator + actFileName))
						{
							try {
								ftp.changeDir(actFileName);
								if ( ftp.actFileList != null )
								{
									this.downloadFiles(actDirOnClient + dirSeperator + actFileName,conn.ls(conn.pwd()).toArray());
									DirBack();
								}
							} catch (Exception e) {
								SFtpDirWorker.log.error(ErrorTxt.IO_EXCEPTION);
								SFtpDirWorker.log.error(ErrorTxt.ERROR_LISTING_DIR_BY_FTP);
								e.printStackTrace();
							}
						}
					}
					else
					{
						if ( monitor != null )
						{
							//conn.setProgressMonitor(monitor);
							monitor.beginTask("Download of file" + actFileName
									+ ". This size is " + ftpFile.getSize() / 1000 
									+ "Kb.",(int) ftpFile.getSize());
						}
						//	OutputStream stream = ClientDirWorker.getClientDir().createOrOverideNewFile(actDirOnClient,ftpFile.getName());
						//	if ( stream != null )
						try {
							conn.get(actFileName, actDirOnClient);
						} catch (Exception e) {
							log.error(ErrorTxt.IO_EXCEPTION);
							log.error(ErrorTxt.ERROR_DOWNLOADING_FILE_OR_DIR);
							e.printStackTrace();
						}
						log.info("Finish download of :" + actDirOnClient);

					}
				}
			}
			if ( actDirOnClient == "" )
			{
				if (monitor != null)
					monitor.done();
				ClientDirWorker.getClientDir().notifyUpdateFileList();
			}

		}
	}

	public ProgressMonitor getMonitor() {
		return monitor;
	}
	public void setMonitor(ProgressMonitor monitor) {
		this.monitor = monitor;
	}
	/*
	class TransferThread extends Thread
	{
		private FTPFile theFile = null;
		private boolean done = false;
		private FTPClient theConn = null;
		private String theLocalDir = null;
		public TransferThread(FTPFile file, FTPClient conn, String localDir)
		{
			theLocalDir = localDir;
			theConn = conn;
			theFile = file;
		}

		public void run()
		{
			try {
				theConn.get(theFile.getName(), theLocalDir);
				//LocalFile lf =  new LocalFile(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath()) ;
				//theFile.copyTo(lf, true );
				//theFile = null;
				done = true;
			} catch (Exception e) {
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
	 */
//	class DownloadThread //extends Thread 
//	{
	private SFtpDirWorker ftp = null;
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
//		ftp = FtpDirWorker.getFtpDirWorker();
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


	public void downloadFiles(String folder,String[] name, TableItem[] items)
	{



		TableItem item = new TableItem(statusTable, SWT.NONE);

		//Filename Setup
		Label  txtFilename = new Label (statusTable, SWT.NONE);
		txtFilename.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		TableEditor editor0 = new TableEditor(statusTable);
		editor0.grabHorizontal = editor0.grabVertical = true;
		editor0.setEditor(txtFilename, item, 0);
		txtFilename.setText(folder + dirSeperator + items[0].getText(0));

		//Progress Bar Setup
		ProgressBar bar = new ProgressBar(statusTable, SWT.SMOOTH);

		//Image Up/Download setup
		//Label  txtStatus = new Label (statusTable, SWT.NONE);
		//txtStatus.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		TableEditor editor1 = new TableEditor(statusTable);
		editor1.grabHorizontal = editor1.grabVertical = true;
		editor1.setEditor(bar, item, 1);
		//txtStatus.setText("Working");	


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
		txtSize.setText("0"); //getSize(ftpFile.size()));

		//Time Left Setup
		Label  txtTime = new Label (statusTable, SWT.NONE);
		txtTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		TableEditor editor6 = new TableEditor(statusTable);
		editor6.grabHorizontal = editor6.grabVertical = true;
		editor6.setEditor(txtTime, item, 4);

		monitor = new ProgressMonitor(bar, txtOperation, statusTable.getShell());

		isChanceled = false;
		DownloadThread thread = new DownloadThread(this, folder,name);
		thread.start();
	}

	/*
	public void downloadFiles_ORIG(String folder,String[] name, TableItem[] items) throws Exception
	{
		//?ORIGINAL
		String actDirOnClient = folder;

		if ( actDirOnClient == null )
			actDirOnClient = "";

		for (String actFileName : name) {
			if ( isChanceled == true )
			{
				//interrupt();
				return;
			}

			try {
				if (( conn == null ) || (conn.isConnected() == false))
				{
					connect();
					//actFileList = new FTPFile(conn,conn.getHomeDirectory());
					//currentDir = conn.getHomeDirectory();
				} 
			//	if ( actFileList == null )
			//		actFileList = new FTPFile(conn.pwd());
			} catch (Exception e) {			
				log.debug(e.getMessage());
			}


			//conn.d

			SftpATTRS ftpFile = null;

			if (folder == null)
			{
				ftpFile = conn.stat(conn.pwd() + dirSeperator + actFileName);
			}
			else
			{
				ftpFile = conn.stat(folder + dirSeperator + actFileName);
			}

			if ( ftpFile == null )
			{
				log.error(ErrorTxt.ERROR_DOWNLOADING_FILE_OR_DIR + actFileName);
				return;
			}
			else
			{
				if ( ftpFile.isDir())
				{
					if ( ClientDirWorker.getClientDir().createDir(actDirOnClient + dirSeperator + actFileName))
					{
						try {
							ftp.changeDir(actFileName);
							if ( ftp.actFileList != null )
							{
								this.downloadFiles(actDirOnClient + dirSeperator + actFileName,conn.ls(actDirOnClient),theItems);
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
					txtFilename.setText(folder + dirSeperator + items[0].getText(0));

					//Progress Bar Setup


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
					txtSize.setText(getSize(ftpFile.getSize()));

					//Time Left Setup
					Label  txtTime = new Label (statusTable, SWT.NONE);
					txtTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
					TableEditor editor6 = new TableEditor(statusTable);
					editor6.grabHorizontal = editor6.grabVertical = true;
					editor6.setEditor(txtTime, item, 4);


					conn.get(ClientDirWorker.getClientDir().getActDirectory().getAbsolutePath(),actFileName);
					txtStatus.setText("Finished");
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
	 */
	public void setTable(Table txfStatusTable) {
		// TODO Auto-generated method stub
		statusTable = txfStatusTable;
	}
	public boolean checkDir(String name) {
		// TODO Auto-generated method stub
		return false;
	}
	public Object[] checkDirEmpty(String name) {
		// TODO Auto-generated method stub
		return null;
	}




	public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {


		public MyUserInfo(String pass) {
			// TODO Auto-generated constructor stub
			passwd = pass;
		}
		public String getPassword(){ return passwd; }


		String passwd;

		public String getPassphrase(){ return passwd; }
		public boolean promptPassphrase(String message){ return true; }
		public boolean promptPassword(String message){
			//passwd=message;
			return true;
		}
		public void showMessage(String message){

		}
		public boolean promptYesNo(String arg0) {
			// TODO Auto-generated method stub
			return true;
		}

		public String[] promptKeyboardInteractive(
				String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo){
			if(prompt.length!=1 || echo[0]!=false || this.passwd==null){
				return null;
			}
			String[] response=new String[1];
			response[0]=this.passwd;
			return response;
		}
	}

	public void rename(String from, String to) {
		isConnected();
		try {
			conn.rename(from, to);
			actFileList = conn.ls(conn.pwd());
		} catch (Exception e) {
			log.error(ErrorTxt.ERROR_RENAMING_DIRECTORY + from);
			log.debug(e.getMessage());
		}
		updateFileList();
		notifyUpdateFileList();

	}
}
