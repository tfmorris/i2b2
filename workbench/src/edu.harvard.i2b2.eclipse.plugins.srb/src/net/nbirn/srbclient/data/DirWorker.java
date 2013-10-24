package net.nbirn.srbclient.data;

import java.io.File;
import net.nbirn.srbclient.utils.IFolderListener;
import net.nbirn.srbclient.utils.SrbDirectoryTable;

import org.eclipse.swt.widgets.Table;

public interface  DirWorker 
{
	DirWorker getFtpDirWorker();
	void connect();
	//void resetConnection();
	Object[] dir();
	//Object[] dirFileInfo();
	void DirBack();
	void changeDir(String name);
	boolean isDir(String name);
	void notifyActionOnFile(String name);
	void createDir(String name);
	void rename(String from, String to);
	long getFileSize(String name);
	//IRODSFile getFile(String name);
	boolean checkDir(String name);
	Object[] checkDirEmpty(String name);
	boolean checkFile(String name);
	void deleteDirs(String[] names);
	//void updateFileList();
	boolean isOnline();
	String downloadFile(String uriLoc, String destDir, String storageResource, String password);

	//void downloadFiles(String folder,SrbDirectoryTable name, Table txfStatusTable);
	String getCurrentDir();
	void uploadFile(File f);
	void uploadFile(String uri, File f);
	void stop();
	void addClientFolderListener(IFolderListener listener);
	void notifyUpdateFileList();
	void disconnect();
	void setTable(Table txfStatusTable);
	void downloadFiles(String folder,SrbDirectoryTable name, Table txfStatusTable);

	String getURI(String f);
	char getPathChar();

	String getDefaultStorageResource();
	void setDefaultStorageResource(String defaultStorageResource);
	String getHomeDirectory();

	void setHomeDirectory(String homeDirectory);		
	String getMdasDomainHome();
	void setMdasDomainHome(String mdasDomainHome);
	int getPort();
	void setPort(String port);
	String getHost();
	void setHost(String host);
	String getPass();
	void setPass(String pass);
	String getUser();
	void setUser(String user);	 


}