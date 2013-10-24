package net.nbirn.srbclient.utils;

public interface IFolderListener 
{
	public static final int REMOVE_DIR = 0;
	public static final int REMOVE_DIR_AND_SAVE_ANSWER = 1;
	public static final int OVERRIDE_DIR = 2;
	public static final int OVERRIDE_DIR_AND_SAVE_ANSWER = 3;
	public static final int OVERRIDE_FILE = 4;
	public static final int OVERRIDE_FILE_AND_SAVE_ANSWER = 5;
	public static final int DO_NOT_REMOVE_DIR = -1;
	public static final int DO_NOT_REMOVE_DIR_AND_SAVE_ANSWER = -2;
	public static final int DO_NOT_OVERRIDE_DIR = -3;
	public static final int DO_NOT_OVERRIDE_DIR_AND_SAVE_ANSWER = -4;
	public static final int DO_NOT_OVERRIDE_FILE = -5;
	public static final int DO_NOT_OVERRIDE_FILE_AND_SAVE_ANSWER = -6;
	
	
	public void updateDirectoryList();
	public int notifyRemovingNoEmtyDir(String name);
	public int notifyOveridingExistingDir(String name);
	public int notifyOverridingExistingFile(String name);
}
