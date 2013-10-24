package net.nbirn.srbclient.utils;

public interface ErrorTxt {
	public final String ERROR_CREATING_FILE_ON_CLIENT = "Errors with the listing provided by the client";
	public final String IO_EXCEPTION = "IO Exception: Errors and Messages";
	public final String FTP_EXCEPTION = "SRP Exception: Errors and Messages";
	public final String FTP_PARSE_EXCEPTION = "SRB Exception wrong type, Unix or Windows date format";
	public final String ERROR_CHANGING_DIRECTORY = "Error with changing directory";
	public final String ERROR_RENAMING_DIRECTORY = "Error with renaming directory";
	public final String ERROR_UPLOADING_FILE = "Error with uploading file";
	public final String ERROR_BY_DISCONNECTING = "Error with disconnecting";
	public final String ERROR_DOWNLOADING_FILE_OR_DIR = "Error with downloading or getting the listing";
	public final String ERROR_LISTING_DIR_BY_FTP = "Error with getting the listing";
	
}
