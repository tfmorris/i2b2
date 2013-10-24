//	Copyright (c) 2006, Regents of the University of California
//	All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//	modification, are permitted provided that the following conditions are
//	met:
//
//	  * Redistributions of source code must retain the above copyright notice,
//	this list of conditions and the following disclaimer.
//	  * Redistributions in binary form must reproduce the above copyright
//	notice, this list of conditions and the following disclaimer in the
//	documentation and/or other materials provided with the distribution.
//	  * Neither the name of the University of California, San Diego (UCSD) nor
//	the names of its contributors may be used to endorse or promote products
//	derived from this software without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//	IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//	THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//	PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//	EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//	PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//	LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//	NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//  FILE
//	IRODSFile.java	-  edu.sdsc.grid.io.irods.IRODSFile
//
//  CLASS HIERARCHY
//	java.lang.Object
//	    |
//	    +-.GeneralFile
//	            |
//	            +-.RemoteFile
//                 |
//                 +-.irods.IRODSFile
//
//  PRINCIPAL AUTHOR
//	Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.irods;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.sdsc.grid.io.local.*;
import edu.sdsc.grid.io.*;



/**
 * An abstract representation of file and directory pathnames on a
 * iRODS server.
 *<P>
 * Shares many similarities with the java.io.File class:
 * User interfaces and operating systems use system-dependent pathname
 * strings to name files and directories. This class presents an abstract,
 * system-independent view of hierarchical pathnames. An abstract pathname
 * has two components:
 *<P>
 * Instances of the IRODSFile class are immutable; that is, once created,
 * the abstract pathname represented by a IRODSFile object will never change.
 *<P>
 * @author	Lucas Gilbert, San Diego Supercomputer Center
 * @see	java.io.File
 * @see	edu.sdsc.grid.io.GeneralFile
 */
public class IRODSFile extends RemoteFile
{
//----------------------------------------------------------------------
//  Constants
//----------------------------------------------------------------------
	/**
	 * Standard iRODS path separator character represented as a string for
	 * convenience. This string contains a single character, namely
   * <code>{@link #PATH_SEPARATOR_CHAR}</code>.
   */
	public static final String PATH_SEPARATOR = "/";

	/**
	 * The iRODS path separator character, '/'.
	 */
	public static final char PATH_SEPARATOR_CHAR = '/';


  /**
   * Whether this abstract pathname is a file or directory is
   * undetermined.
   */
  static final int PATH_IS_UNKNOWN = 0;

  /**
   * This abstract pathname is a file.
   */
  static final int PATH_IS_FILE = 1;

  /**
   * This abstract pathname is a directory.
   */
  static final int PATH_IS_DIRECTORY = 2;
  
  
//----------------------------------------------------------------------
//  Fields
//----------------------------------------------------------------------
  /**
   * Connection to the iRODS server.
   */
  IRODSFileSystem iRODSFileSystem; 


	/**
	 * The storage resource name.
	 * A physical iRODS resource is a system that is capable of storing data sets
	 * and is accessible to the iRODS. It is registered in iRODS with its physical
	 * characteristics such as its physical location, resource type, latency,
	 * and maximum file size. For example, HPSS can be a resource, as can a
	 * Unix file system.
	 */
	String resource;


	/**
	 * The data type of the file. The default value is "generic".
	 */
	String dataType = "generic";

	/**
	 * If delete on exit gets set.
	 */
	boolean deleteOnExit = false;


  /**
   * Whether this abstract pathname is actually a directory or a file.
   * Reduces the number of network calls.
   */
  int pathNameType = PATH_IS_UNKNOWN;

  /**
   * If true, the cached value for the isDirectory or isFile methods
   * will always be used. The cache can be refresh by calling the
   * <code>isFile(true)</code> or <code>isDirectory(true)</code>.
   * When false, inside any particular SRBFile method isFile(true) or
   * isDirectory(true) is called only once. However if you know the status as
   * a normal file will not change and want to perform a lot of actions on
   * that file, then setting this value to false could save a number of
   * network calls. Care should be taken though, as most methods don't have a
   * refresh cache option, so not updating at the beginning of each method could
   * cause errors.
   */
  boolean useCache = false;

  

//----------------------------------------------------------------------
//  Constructors and Destructors
//----------------------------------------------------------------------
	/**
	 * Creates a new IRODSFile instance by converting the given pathname string
	 * into an abstract pathname.
	 *<P>
	 * @param fileSystem	The connection to the iRODS server
	 * @param filePath	A pathname string
	 */
	public IRODSFile( IRODSFileSystem fileSystem, String filePath )
//		throws IOException
	{
		this( fileSystem, "", filePath );
	}

	/**
	 * Creates a new iRODSFile instance from a parent pathname string and
	 * a child pathname string.
	 *<P>
	 * If parent is null then the new IRODSFile instance is created as if by
	 * invoking the single-argument IRODSFile constructor on the given child
	 * pathname string.
	 *<P>
	 * Otherwise the parent pathname string is taken to denote a directory,
	 * and the child pathname string is taken to denote either a directory
	 * or a file. If the child pathname string is absolute then it is
	 * converted into a relative pathname in a system-dependent way.
	 * If parent is the empty string then the new RemoteFile instance is created
	 * by converting child into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 *<P>
	 * @param fileSystem	The connection to the iRODS server
	 * @param parent	The parent pathname string
	 * @param child		The child pathname string
	 */
	public IRODSFile( IRODSFileSystem fileSystem, String parent, String child )
//		throws IOException
	{
		super( fileSystem, parent, child );
		resource = iRODSFileSystem.getDefaultStorageResource();
    
		makePathCanonical( parent );
	}

	/**
	 * Creates a new IRODSFile instance from a parent abstract pathname
	 * and a child pathname string.
	 *<P>
	 * If parent is null then the new IRODSFile instance is created as if
	 * by invoking the single-argument IRODSFile constructor on the given
	 * child pathname string.
	 *<P>
	 * Otherwise the parent abstract pathname is taken to denote a directory,
	 * and the child pathname string is taken to denote either a directory or
	 * a file. If the child pathname string is absolute then it is converted
	 * into a relative pathname in a system-dependent way. If parent is the
	 * empty abstract pathname then the new RemoteFile instance is created by
	 * converting child into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 *<P>
	 * @param parent	The parent abstract pathname
	 * @param child		The child pathname string
	 */
	public IRODSFile( IRODSFile parent, String child )
//		throws IOException
	{
		this( (IRODSFileSystem) parent.getFileSystem(), 
      parent.getAbsolutePath(), child );
	}



	/**
	 * Creates a new IRODSFile instance by converting the given file: URI
	 * into an abstract pathname.
	 *<P>
   *  iRODS URI protocol:<br>
   *  irods:// [ userName [ : password ] @ ] host [ : port ][ / path ]
   *<P>
   * example:<br>
   * irods://irods@irods.sdsc.edu:21/pub/testfile.txt 
	 *
	 * @param uri An absolute, hierarchical URI using a supported scheme.
	 * @throws NullPointerException if <code>uri</code> is <code>null</code>.
	 * @throws IllegalArgumentException If the preconditions on the parameter
	 *		do not hold.
	 */
  public IRODSFile( URI uri )
		throws IOException, URISyntaxException
	{
		super( uri );
    
    if (uri.getScheme().equals( "irods" ))
    {
      String userInfo = uri.getUserInfo();

      String host = uri.getHost();
      int port = uri.getPort();
      String userName = null, password = "", zone = "", homeDirectory = null;
      int index = -1;
      
      if ((userInfo == null) || (userInfo == "")) {
        //anon. login
//TODO
throw new IllegalArgumentException("URI must have user info");        
//			userName = IRODSAccount.PUBLIC_USERNAME;
//			zone = IRODSAccount.PUBLIC_ZONENAME;
//			password = IRODSAccount.PUBLIC_PASSWORD;
//			homeDirectory = IRODSAccount.PUBLIC_HOME_DIRECTORY;
      }
      else {
        index = userInfo.indexOf(":");        
        if (index >= 0) {
          password = userInfo.substring(index+1); //password 
          userInfo = userInfo.substring(0, index);
        }
        
        index = userInfo.indexOf(".");
        if (index >= 0) {          
          userName = userInfo.substring(0, index);
          zone = userInfo.substring(index+1); 
          homeDirectory = PATH_SEPARATOR + zone + PATH_SEPARATOR + userName;
        }
        else {
          userName = userInfo;
          homeDirectory = uri.getPath();
        }
        
        setFileSystem( new IRODSFileSystem( new IRODSAccount( 
          uri.getHost(), 
          uri.getPort(), 
          userName, 
          password, 
          homeDirectory,
          zone,
          "" )) //default resource  TODO query
        );
      }
      
      setFileName( uri.getPath() );
    }
    else {
      throw new URISyntaxException(uri.toString(), "Wrong URI scheme");
    }    
	}
  


	/**
	 * Finalizes the object by explicitly letting go of each of
	 * its internally held values.
	 */
	protected void finalize( )
		throws Throwable
	{
		if (deleteOnExit)
			delete();

		super.finalize();

		if (resource != null)
			resource = null;

		if (dataType != null)
			dataType = null;
	}

//----------------------------------------------------------------------
// Setters and Getters
//----------------------------------------------------------------------
	/**
	 * Sets the file system used of this GeneralFile object. The file
	 * system object must be a subclass of the GeneralFileSystem matching
	 * this file object. eg. XYZFile requires XYZFileSystem.
	 *
	 * @param fileSystem The file system to be used.
	 * @throws IllegalArgumentException - if the argument is null.
	 * @throws ClassCastException -
	 * 		if the argument is not an object of the approriate subclass.
	 */
	protected void setFileSystem( GeneralFileSystem fileSystem )
		throws IllegalArgumentException, ClassCastException
	{
		if ( fileSystem == null )
			throw new IllegalArgumentException("Illegal fileSystem, cannot be null");

		this.fileSystem = fileSystem;
    iRODSFileSystem = (IRODSFileSystem) fileSystem;
	}
  
  /**
	 * Set the file name.
	 * @param fleName The file name or fileName plus some or all of the
	 * directory path.
	 */
	protected void setFileName( String filePath )
	{
		//used when parsing the filepath
		int index;

		//in case they used the local pathSeperator
		//in the fileName instead of the iRODS PATH_SEPARATOR.
		String localSeparator = System.getProperty( "file.separator" );

		if ( filePath == null ) {
			throw new NullPointerException( "The file name cannot be null" );
		}

		//replace local separators with iRODS separators.
		if (!localSeparator.equals(PATH_SEPARATOR)) {
			index = filePath.lastIndexOf( localSeparator );
			while ((index >= 0) && ((filePath.substring( index + 1 ).length()) > 0)) {
				filePath = filePath.substring( 0, index ) + PATH_SEPARATOR +
					filePath.substring( index + 1 );
				index = filePath.lastIndexOf( localSeparator );
			}
		}
		fileName = filePath;

		if (fileName.length() > 1) { //add to allow path = root "/"
			index = fileName.lastIndexOf( PATH_SEPARATOR );
			while ((index == fileName.length()-1) && (index >= 0)) {
				//remove '/' at end of filename, if exists
				fileName =  fileName.substring( 0, index );
				index = fileName.lastIndexOf( PATH_SEPARATOR );
			}

			//seperate directory and file
			if ((index >= 0) &&
				((fileName.substring( index + 1 ).length()) > 0)) {
				//have to run setDirectory(...) again
				//because they put filepath info in the filename
				setDirectory( fileName.substring( 0, index + 1 ) );
				fileName =  fileName.substring( index + 1 );
			}
		}
	}

	/**
	 * Set the directory.
	 * @param dir	The directory path, need not be absolute.
	 */
//though everything will be converted to a canonical path	to avoid errors
	protected void setDirectory( String dir )
	{
		if (directory == null) {
			directory = new Vector();
		}
		if (dir == null) {
			return;
		}

		//in case they used the local pathSeperator
		//in the fileName instead of the iRODS PATH_SEPARATOR.
		String localSeparator = System.getProperty( "file.separator" );
		int index = dir.lastIndexOf( localSeparator );
		if ((index >= 0) && ((dir.substring( index + 1 ).length()) > 0)) {
			dir = dir.substring( 0, index ) + PATH_SEPARATOR +
				dir.substring( index + 1 );
			index = dir.lastIndexOf( localSeparator );
		}

		while ((directory.size() > 0) && //only if this is the dir cut from fileName
						dir.startsWith(PATH_SEPARATOR))// &&  //strip these
//						(dir.length() > 1)) //but not if they only wanted
		{
			dir = dir.substring(1);
//problems if dir passed from filename starts with PATH_SEPARATOR
		}

		//create directory vector
		index = dir.indexOf( PATH_SEPARATOR );

		if (index >= 0) {
			do {
				directory.add( dir.substring( 0, index ) );
				do {
					dir = dir.substring( index + 1 );
					index = dir.indexOf( PATH_SEPARATOR );
				} while (index == 0);
			} while (index >= 0);
		}
		//add the last path item
		if ((!dir.equals("")) && (dir != null)) {
			directory.add( dir );
		}
	}

	/**
	 * Helper for setting the directory to an absolute path
	 * @param dir Used to determine if the path is absolute.
	 */
//Yes, this whole business is the most horrible thing you have ever seen.
//
//using "fileName&COPY=#" in the constructor should stay valid.
	void makePathCanonical( String dir )
	{
		int i = 0; //where to insert into the Vector
		boolean absolutePath = false;
		String canonicalTest = null;

		if (dir == null) {
			dir = "";
		}

		//In case this abstract path is supposed to be root
		if ((fileName.equals(IRODSFileSystem.IRODS_ROOT)) && (dir == "")) {
			return;
		}

		//In case this abstract path is supposed to be the home directory
		if (fileName.equals("") && dir.equals("")) {
			String home = iRODSFileSystem.getHomeDirectory();
			int index = home.lastIndexOf( separator );
			setDirectory( home.substring( 0, index ) );
			setFileName( home.substring( index+1 ) );
			return;
		}


		//if dir not absolute
		if (dir.startsWith(iRODSFileSystem.IRODS_ROOT))
			absolutePath = true;

		//if directory not already absolute
		if (directory.size() > 0) {
			if (directory.get(0).toString().length() == 0) {
				//The /'s were all striped when the vector was created
				//so if the first element of the vector is null
				//but the vector isn't null, then the first element
				//is really a /.
				absolutePath = true;
			}
		}
		if (!absolutePath) {
			String home = iRODSFileSystem.getHomeDirectory();
			int index = home.indexOf( separator );
			//allow the first index to = 0,
			//because otherwise separator won't get added in front.
			if (index >= 0) {
				do {
					directory.add( i, home.substring( 0, index ) );
					home = home.substring( index + 1 );
					index = home.indexOf( separator );
					i++;
				} while (index > 0);
			}
			if ((!home.equals("")) && (home != null)) {
				directory.add( i, home );
			}
		}


		//first, made absolute, then canonical
		for (i=0; i<directory.size(); i++) {
			canonicalTest = directory.get(i).toString();
			if (canonicalTest.equals( "." )) {
				directory.remove( i );
				i--;
			}
			else if ((canonicalTest.equals( ".." )) && (i >= 2)) {
				directory.remove( i );
				directory.remove( i-1 );
				i--;
				if (i > 0)
					i--;
			}
			else if (canonicalTest.equals( ".." )) {
				//at root, just remove the ..
				directory.remove( i );
				i--;
			}
			else if (canonicalTest.startsWith( separator )) {
				//if somebody put filepath as /foo//bar or /foo////bar
				do {
					canonicalTest = canonicalTest.substring( 1 );
				} while (canonicalTest.startsWith( separator ));
				directory.remove( i );
				directory.add( i, canonicalTest );
			}
		}
		//also must check fileName
		if (fileName.equals( "." )) {
			fileName = directory.get(directory.size()-1).toString();
			directory.remove( directory.size()-1 );
		}
		else if (fileName.equals( ".." )) {
			if (directory.size() > 1) {
				fileName = directory.get(directory.size()-2).toString();
				directory.remove( directory.size()-1 );
				directory.remove( directory.size()-1 );
			}
			else {
				//at root
				fileName = separator;
				directory.remove( directory.size()-1 );
			}
		}
	}


	/**
	 * This abstract method gets the path separator as defined by the subclass.
	 */
	public String getPathSeparator( )
	{
    return PATH_SEPARATOR;
	}

	/**
	 * This abstract method gets the path separator char as defined by
	 * the subclass.
	 */
	public char getPathSeparatorChar( )
	{
		return PATH_SEPARATOR_CHAR;
	}
  
  
//----------------------------------------------------------------------
// GeneralFile Methods
//----------------------------------------------------------------------  
	/**
	 * Copies this file to another file. This object is the source file.
	 * The destination file is given as the argument.
	 * If the destination file, does not exist a new one will be created.
	 * Otherwise the source file will be appended to the destination file.
	 * Directories will be copied recursively.
	 *
	 * @param file	The file to receive the data.
	 * @throws  NullPointerException If file is null.
	 * @throws IOException If an IOException occurs.
	 */
	public void copyTo( GeneralFile file, boolean forceOverwrite )
		throws IOException
	{
		if (file == null) {
			throw new NullPointerException();
		}

		if (isDirectory()) {
			//recursive copy
			GeneralFile[] fileList = listFiles();

			file.mkdir();
			if (fileList != null) {
				for (int i=0;i<fileList.length;i++) {
					fileList[i].copyTo(
						FileFactory.newFile( file.getFileSystem(), file.getAbsolutePath(),
              fileList[i].getName()), forceOverwrite );
				}
			}
		}
		else {
			if (file.isDirectory()) {
				//change the destination from a directory to a file
				file = FileFactory.newFile( file, getName() );
			}
      try {   
        if (file instanceof LocalFile) {
          iRODSFileSystem.commands.get( 
            this, (LocalFileOutputStream)FileFactory.newFileOutputStream(file),
            forceOverwrite
          );
        }
        else if (file instanceof IRODSFile) {          
          iRODSFileSystem.commands.copy( this, (IRODSFile)file, forceOverwrite );
        }
        else {
          super.copyTo( file, forceOverwrite );
        }
      } catch (IRODSException e) {
        IOException io = new IOException();
        io.initCause(e);
        throw io;
      }
		}    
  }
  

	/**
	 * Copies this file to another file. This object is the source file.
	 * The destination file is given as the argument.
	 * If the destination file, does not exist a new one will be created.
	 * Otherwise the source file will be appended to the destination file.
	 * Directories will be copied recursively.
	 *
	 * @param file	The file to receive the data.
	 * @throws  NullPointerException If file is null.
	 * @throws IOException If an IOException occurs.
	 */
	public void copyFrom( GeneralFile file, boolean forceOverwrite )
		throws IOException
	{
		if (file == null) {
			throw new NullPointerException();
		}

		if (file.isDirectory()) {
			//recursive copy
			GeneralFile[] fileList = file.listFiles();

			mkdir();
			if (fileList != null) {
				for (int i=0;i<fileList.length;i++) {
					FileFactory.newFile( this, fileList[i].getName() ).copyFrom(
						fileList[i], forceOverwrite );
				}
			}
		}
		else {
			if (isDirectory()) {
				//change the destination from a directory to a file
				GeneralFile subFile = FileFactory.newFile( this, file.getName() );
				subFile.copyFrom( file );
				return;
			}
      try {
        if (file instanceof LocalFile) {
          iRODSFileSystem.commands.put( 
            FileFactory.newFileInputStream(file), file.length(), forceOverwrite,
            this );
        }
        else if (file instanceof IRODSFile) {
          iRODSFileSystem.commands.copy( (IRODSFile)file, this, forceOverwrite );
        }
        else {
          super.copyTo( file, forceOverwrite ); 
        }
      } catch (IRODSException e) {
        IOException io = new IOException();
        io.initCause(e);
        throw io;
      }
		}
	}

  
  
//----------------------------------------------------------------------
// RemoteFile Methods
//----------------------------------------------------------------------  
	/**
	 * Sets the physical resource this IRODSFile object will be stored on.
   *
	 * @param resource The name of resource to be used.
	 * @throws NullPointerException If resourceName is null.
	 * @throws IllegalArgumentException If resourceName is not a valid resource.
	 * @throws IOException If an IOException occurs during the system change.
	 */
	public void setResource( String resourceName )
		throws IOException, NullPointerException, IllegalArgumentException
	{
		if ( resourceName != null ) {
			resource = resourceName;
    }
    else
			throw new NullPointerException();
  }
  
  
	/**
	 * @return resource the physical resource where this file is stored.
	 *		Will not query the server if this abstract pathname is a directory.
	 * 		Returns null if the file is a directory or does not exist.
	 *
	 * @throws IOException If an IOException occurs during the system query.
	 */
	public String getResource( )
		throws IOException
	{
		if (isDirectory()) {
			return null;
		}

    if (resource != null) {
      return resource;
    }
//TODO
		return null;//firstQueryResult( SRBMetaDataSet.PHYSICAL_RESOURCE_NAME );
	}
  

	/**
	 * @return dataType  The dataType string of this file.
	 *		Will not query the server if this abstract pathname is a directory.
	 * 		Returns null if the file does not exist.
	 *
	 * @throws IOException If an IOException occurs during the system query.
	 */
	public String getDataType( )
		throws IOException
	{
		if (isDirectory()) {
			return dataType;
		}
//TODO
		return "generic";//firstQueryResult( SRBMetaDataSet.FILE_TYPE_NAME );
	}
  
  
  public void replicate( String newResource )
		throws IOException
  {
    iRODSFileSystem.commands.replicate(this, newResource);
  }
  
  
//----------------------------------------------------------------------
// IRODSFile Methods
//----------------------------------------------------------------------  
	/**
	 * Change the permissions for this IRODSFile.
	 * <P>
	 * @param permission "w" - write;"r" - read;"own" or "all" - owner;"n" - null;
	 * @param newUserName The permissions are changed for this user,
	 * @param userMdasDomain at this Mdas domain.
   * @param recursive Changes this and all subdirectories
	 * @throws IOException If an IOException occurs.
	 */
	public void changePermissions( String permission, String newUserName,
    boolean recursive )
		throws IOException
	{
		if (permission == null) {
			permission = "";
		}

		permission = permission.toLowerCase();

		if (permission.equals("n") || permission.equals("null")) //or "" or null
    {
			permission = "";
		}
		else if (permission.equals("r") || permission.equals("read")) {
			permission = "read";
		}
		else if (permission.equals("w") || permission.equals("write")) {
			permission = "write";
		}
		else if (permission.equals("all") || permission.equals("ownership") ||
             permission.equals("own") || permission.equals("o")) 
    {
			permission = "own";
		}
		else {
			//permission = "";
			throw new IllegalArgumentException(
				"Permission type not valid: "+permission );
		}
    iRODSFileSystem.commands.chmod(this, permission, newUserName, recursive);
	}
  
  
//----------------------------------------------------------------------
// java.io.File Methods
//----------------------------------------------------------------------  	
  /**
	 * Tests whether the application can read the file denoted by
	 * this abstract pathname.
	 *
	 * @return  <code>true</code> if and only if the file specified by this
	 * 	abstract pathname exists <em>and</em> can be read; otherwise
	 *  <code>false</code>.
	 */
	public boolean canRead( )
	{
//TODO    
//    String result = firstQueryResult(IRODSMetaDataSet.);
		return true;
	}
  
  
	/**
	 * Tests whether the application can modify to the file denoted by
	 * this abstract pathname.
	 *
	 * @return  <code>true</code> if and only if the file system actually
	 * 	contains a file denoted by this abstract pathname <em>and</em>
	 * 	the application is allowed to write to the file; otherwise
	 * <code>false</code>.
	 */
	public boolean canWrite( )
	{
//TODO
//    String result = firstQueryResult(IRODSMetaDataSet.);
    return true;
  }
   
  
	/**
	 * Atomically creates a new, empty file named by this abstract pathname if
	 * and only if a file with this name does not yet exist.  The check for the
	 * existence of the file and the creation of the file if it does not exist
	 * are a single operation that is atomic with respect to all other
	 * filesystem activities that might affect the file.
	 * <P>
	 * Note: this method should <i>not</i> be used for file-locking, as
	 * the resulting protocol cannot be made to work reliably.
	 *
	 * @return  <code>true</code> if the named file does not exist and was
	 *          successfully created; <code>false</code> if the named file
	 *          already exists
	 *
	 * @throws  IOException If an I/O error occurred
	 */
	public boolean createNewFile() throws IOException
	{
		try {
			if (!isFile()) {
				getParentFile().mkdirs();

				int fd = iRODSFileSystem.commands.fileCreate( this );

				//Be sure to close files after a create() or open().
				iRODSFileSystem.commands.fileClose( fd );
				return true;
			}
		} catch (IRODSException e) {
      if (IRODSFileSystem.DEBUG > 0) e.printStackTrace();
			//catch already exists and just return false
//TODO equivalent
//      if (e.getType() != -3210)
//				throw e;
		}

		return false;
	}

	/**
	 * <p> Creates a new empty file in the specified directory, using the
	 * given prefix and suffix strings to generate its name.  If this method
	 * returns successfully then it is guaranteed that:
	 *
	 * <ol>
	 * <li> The file denoted by the returned abstract pathname did not exist
	 *      before this method was invoked, and
	 * <li> Neither this method nor any of its variants will return the same
	 *      abstract pathname again in the current invocation of the virtual
	 *      machine.
	 * </ol>
	 *
	 * This method provides only part of a temporary-file facility.  To arrange
	 * for a file created by this method to be deleted automatically, use the
	 * <code>{@link #deleteOnExit}</code> method.
	 *
	 * <p> The <code>prefix</code> argument must be at least three characters
	 * long.  It is recommended that the prefix be a short, meaningful string
	 * such as <code>"hjb"</code> or <code>"mail"</code>.  The
	 * <code>suffix</code> argument may be <code>null</code>, in which case the
	 * suffix <code>".tmp"</code> will be used.
	 *
	 * <p> To create the new file, the prefix and the suffix may first be
	 * adjusted to fit the limitations of the underlying platform.  If the
	 * prefix is too long then it will be truncated, but its first three
	 * characters will always be preserved.  If the suffix is too long then it
	 * too will be truncated, but if it begins with a period character
	 * (<code>'.'</code>) then the period and the first three characters
	 * following it will always be preserved.  Once these adjustments have been
	 * made the name of the new file will be generated by concatenating the
	 * prefix, five or more internally-generated characters, and the suffix.
	 *
	 * <p> If the <code>directory</code> argument is <code>null</code> then the
	 * default temporary-file directory will be used. Since the SRB does not
	 * have a standard temporary directory, files will be placed in a temp/
	 * directory in the user's SRB home directory.
	 * There are certain difficulties creating a static connection to the SRB.
	 * For this static method to connect to the SRB, .Mdas files must be
	 * available in the local home directory/.srb. That is the information that
	 * will be used when storing the temporary file. This comprimise is
	 * necessary to maintain the designs unity with the java.io.File class.
	 *
	 * @param  prefix     The prefix string to be used in generating the file's
	 *                    name; must be at least three characters long
	 *
	 * @param  suffix     The suffix string to be used in generating the file's
	 *                    name; may be <code>null</code>, in which case the
	 *                    suffix <code>".tmp"</code> will be used
	 *
	 * @param  directory  The directory in which the file is to be created, or
	 *                    <code>null</code> if the default temporary-file
	 *                    directory is to be used
	 *
	 * @return  An abstract pathname denoting a newly-created empty file
	 *
	 * @throws  IllegalArgumentException
	 *          If the <code>prefix</code> argument contains fewer than three
	 *          characters
	 *
	 * @throws  IOException  If a file could not be created
	 */
	public static GeneralFile createTempFile(
		String prefix, String suffix, GeneralFile directory)
		throws IOException, IllegalArgumentException
	{
		String randomChars = "";
		for (int i=0;i<8;i++)
			randomChars += ((char) (65 + Math.random() * 25));

		if (prefix == null)
			throw new NullPointerException();
		if (prefix.length() < 3)
			throw new IllegalArgumentException("Prefix string too short");

		if (suffix == null)
			suffix = ".tmp";

		if (directory == null) {
			IRODSFileSystem fs = new IRODSFileSystem();
			directory = FileFactory.newFile( fs, fs.getHomeDirectory(), "temp" );
			directory.mkdir();
		}


		GeneralFile temp = FileFactory.newFile( directory,
			prefix+randomChars+suffix );

		if ( temp.createNewFile() )
			return temp;
		else {
			throw new IOException("The temp file already exists.");
		}
	}

	/**
	 * Deletes the file or directory denoted by this abstract pathname.  If
	 * this pathname denotes a directory, then the directory must be empty in
	 * order to be deleted.
	 *
	 * @return  <code>true</code> if and only if the file or directory is
	 *          successfully deleted; <code>false</code> otherwise
	 */
	public boolean delete( )
	{
		try {
      if (isDirectory()) {
        iRODSFileSystem.commands.deleteDirectory( this );
      }
      else if (isFile(false)) {
        iRODSFileSystem.commands.deleteFile( this );
      }
      return true;
		} catch( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
			return false;
		}
	}
  
  
	/**
	 * Requests that the file or directory denoted by this abstract
	 * pathname be deleted when the virtual machine terminates.
	 * Deletion will be attempted only for normal termination of the
	 * virtual machine, as defined by the Java Language Specification.
	 *
	 * <p> Once deletion has been requested, it is not possible to cancel the
	 * request.  This method should therefore be used with care.
	 *
	 * <P>
	 * Note: this method should <i>not</i> be used for file-locking, as
	 * the resulting protocol cannot be made to work reliably.
	 */
	public void deleteOnExit( )
	{
		deleteOnExit = true;
	}
  

	/**
	 * @return This abstract pathname as a pathname string.
	 */
	public String getPath( )
	{
		return getAbsolutePath();
	}
  
  
	/**
	 * Tests this abstract pathname for equality with the given object.
	 * Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and is an abstract pathname that denotes the same file
	 * or directory as this abstract pathname.
	 *
	 * @param   obj   The object to be compared with this abstract pathname
	 *
	 * @return  <code>true</code> if and only if the objects are the same;
	 *          <code>false</code> otherwise
	 */
	public boolean equals( Object obj )
	{
		try {
  		if (obj == null)
  			return false;

      if (obj instanceof IRODSFile) {
  			IRODSFile temp = (IRODSFile) obj;
        
        if (temp.iRODSFileSystem.getHost().equals(iRODSFileSystem.getHost())) {
          if (temp.iRODSFileSystem.getPort() == iRODSFileSystem.getPort()) 
          {
        		return getAbsolutePath().equals(temp.getAbsolutePath());
          }
        }
      }
		} catch (ClassCastException e) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
		}
    return false;
	}
  

	/**
	 * Tests whether the file denoted by this abstract pathname exists.
	 *
	 * @return  <code>true</code> if and only if the file denoted by this
	 * 	abstract pathname exists; <code>false</code> otherwise
	 */
	public boolean exists( )
	{
		try {
			MetaDataRecordList[] rl = null;
			int operator = MetaDataCondition.EQUAL;

			//if it is a file
			MetaDataCondition conditions[] = null;

/*TODO?      
			if (getReplicaNumber() >= 0) {
				conditions = new MetaDataCondition[2];
				conditions[0] = MetaDataSet.newCondition(
					GeneralMetaData.DIRECTORY_NAME, operator, getParent() );
				conditions[1] = MetaDataSet.newCondition(
					GeneralMetaData.FILE_NAME, operator, getName() );
				conditions[1] = MetaDataSet.newCondition(
					SRBMetaDataSet.FILE_REPLICATION_ENUM, operator, replicaNumber );
			}
			else {
*/        
				conditions = new MetaDataCondition[2];
				conditions[0] = MetaDataSet.newCondition(
					GeneralMetaData.DIRECTORY_NAME, operator, getParent() );
				conditions[1] = MetaDataSet.newCondition(
					GeneralMetaData.FILE_NAME, operator, getName() );
//			}

			MetaDataSelect selects[] = {
				MetaDataSet.newSelection( GeneralMetaData.FILE_NAME )
			};

			rl = fileSystem.query( conditions, selects, 3 );

			if (rl != null)
				return true;


			//if it is a directory
			conditions = new MetaDataCondition[1];
			conditions[0] =
				MetaDataSet.newCondition(
					GeneralMetaData.DIRECTORY_NAME, operator, getAbsolutePath() );
			selects[0] =
				MetaDataSet.newSelection( GeneralMetaData.DIRECTORY_NAME );
			rl = fileSystem.query( conditions, selects, 3 );

			if (rl != null)
				return true;

		} catch ( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
		}

		return false;
  }
  

	/**
	 * Returns the canonical pathname string of this abstract pathname.
	 *
	 * @return  The canonical pathname string denoting the same file or
	 *          directory as this abstract pathname
	 *
	 * @throws  IOException
	 *          If an I/O error occurs, which is possible because the
	 *          construction of the canonical pathname may require
	 *          filesystem queries
	 */
	public String getCanonicalPath( )
		throws IOException
	{
		if (( directory != null ) && (!directory.isEmpty())) {
			int size = directory.size();
			String path = (String) directory.firstElement();
			int i = 1;

			while (i < size ) {
				path += separator + directory.get( i );
				i++;
			}

			return path + separator + fileName;
		}

		return fileName;
	}

  
	/**
	 * Computes a hash code for this abstract pathname. The hash code of
	 * an abstract pathname is equal to the exclusive <em>or</em> of its
	 * pathname string and the decimal value <code>1234321</code>.
	 *
	 * @return  A hash code for this abstract pathname
	 */
	public int hashCode( )
	{
		return getAbsolutePath().toLowerCase().hashCode() ^ 1234321;
	}


	/**
	 * Tests whether this abstract pathname is absolute. A pathname is
	 * absolute if its prefix is <code>"/"</code>.
	 *
	 * @return  <code>true</code> if this abstract pathname is absolute,
	 *          <code>false</code> otherwise
	 */
	public boolean isAbsolute( )
	{
		//all path names are made absolute at construction.
		return true;
	}


	/**
	 * Tests whether the file denoted by this abstract pathname is a directory.
	 * Also known on the SRB as a collection.
	 *<P>
	 * A SRB collection is a logical name given to a set of data sets. All data
	 * sets stored in SRB/MCAT are stored in some collection. A collection can
	 * have sub-collections, and hence provides a hierarchical structure. A
	 * collection in SRB/MCAT can be equated to a directory in a Unix file
	 * system. But unlike a file system, a collection is not limited to a
	 * single device (or partition). A collection is logical but the datsets
	 * grouped under a collection can be stored in heterogeneous storage
	 * devices. There is one obvious restriction, the name given to a data set
	 * in a collection or sub-collection should be unique in that collection.
	 *
	 * @return <code>true</code> if and only if the file denoted by this
	 *          abstract pathname exists <em>and</em> is a directory;
	 *          <code>false</code> otherwise
	 */
	public boolean isDirectory( )
	{
    if ( useCache ) {
      return isDirectory( false );
    }
    else {
      return isDirectory( true );
    }
	}
  
  
	/**
	 * Tests whether the file denoted by this abstract pathname is a directory.
	 * Also known on the SRB as a collection.
	 *<P>
	 * @param update If true, send a new query to the SRB to determine if
	 *    this abstract pathname refers to a directory. If false, this
	 *    method will return a previously stored value. Also queries the SRB
	 *    if the value is not already stored with this object.
	 * @return <code>true</code> if and only if the file denoted by this
	 *          abstract pathname exists <em>and</em> is a directory;
	 *          <code>false</code> otherwise
	 */
	boolean isDirectory( boolean update )
	{
    if (update || (pathNameType == PATH_IS_UNKNOWN)) {
      //run the code below
    }
    else if (pathNameType == PATH_IS_FILE) {
      return false;
    }
    else if (pathNameType == PATH_IS_DIRECTORY) {
      return true;
    }

		MetaDataRecordList[] rl = null;
		MetaDataCondition[] conditions = {
			MetaDataSet.newCondition( GeneralMetaData.DIRECTORY_NAME,
				MetaDataCondition.EQUAL, getAbsolutePath() ) };
		MetaDataSelect[] selects = {
			MetaDataSet.newSelection( GeneralMetaData.DIRECTORY_NAME ) };

		try {
			rl = fileSystem.query( conditions, selects, 3 );

			if ( rl != null && rl.length > 0) {
        pathNameType = PATH_IS_DIRECTORY;
				return true;
      }

		} catch ( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
		}

		return false;
	}


	/**
	 * Tests whether the file denoted by this abstract pathname is a normal
	 * file. A file is <em>normal</em> if it is not a directory or a container.
	 * Any non-directory or other subclass of SRBFile, such as a SRBContainer,
	 * file created by a Java application is guaranteed to be a normal file.
	 *<P>
	 * In the terminology of SRB, files are known as data sets. A data set is
	 * a "stream-of-bytes" entity that can be uniquely identified. For example,
	 * a file in HPSS or Unix is a data set, or a LOB stored in a SRB Vault
	 * database is a data set. Importantly, note that a data set is not a
	 * set of data objects/files. Each data set in SRB is given a unique
	 * internal identifier by SRB. A dataset is associated with a collection.
	 *
	 * @return  <code>true</code> if and only if the file denoted by this
	 *          abstract pathname exists <em>and</em> is a normal file;
	 *          <code>false</code> otherwise
	 */
	public boolean isFile( )
	{
    if ( useCache ) {
      return isFile( false );
    }
    else {
      return isFile( true );
    }
	}

	/**
	 * Tests whether the file denoted by this abstract pathname is a file.
	 * Also known on the SRB as a dataset.
	 *<P>
	 * @param update If true, send a new query to the SRB to determine if
	 *    this abstract pathname refers to a file. If false, this
	 *    method will return a previously stored value. Also queries the SRB
	 *    if the value is not already stored with this object.
	 * @return <code>true</code> if and only if the file denoted by this
	 *          abstract pathname exists <em>and</em> is a directory;
	 *          <code>false</code> otherwise
	 */
	public boolean isFile( boolean update )
	{
    if ((pathNameType == PATH_IS_UNKNOWN) || update) {
      //run the code below
    }
    else if (pathNameType == PATH_IS_FILE) {
      return true;
    }
    else if (pathNameType == PATH_IS_DIRECTORY) {
      return false;
    }

    MetaDataRecordList[] rl = null;
    MetaDataCondition[] conditions = {
      MetaDataSet.newCondition( GeneralMetaData.DIRECTORY_NAME,
        MetaDataCondition.EQUAL, getParent() ),
      MetaDataSet.newCondition( GeneralMetaData.FILE_NAME,
        MetaDataCondition.EQUAL, getName() ) };
    MetaDataSelect[] selects = {
      MetaDataSet.newSelection( GeneralMetaData.FILE_NAME ) };

    try {
      rl = fileSystem.query( conditions, selects, 3 );

      if( rl != null ) {
        pathNameType = PATH_IS_FILE;
        return true;
      }

    } catch ( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
    }

    return false;
	}


	/**
	 * Tests whether the file named by this abstract pathname is a hidden file.
	 *
	 * @return  <code>true</code> if and only if the file denoted by this
	 *          abstract pathname is hidden.
	 */
	public boolean isHidden( )
	{
		return false; //TODO has hidden?
	}



  /**
   * Returns the time that the file denoted by this abstract pathname
   * was last modified.
   *
   * @return  A <code>long</code> value representing the time the file was
   *          last modified, measured in system-dependent way.
   */
  public long lastModified( )
  {
		long lastModified = 0;
    String result = null;
		try {
      result = firstQueryResult( GeneralMetaData.MODIFICATION_DATE );
      if (result != null) {
        lastModified = Long.parseLong( result );
      }
		} catch ( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
			return 0;
		}
		return lastModified;
  }
  

	/**
	 * Returns an array of strings naming the files and directories in
	 * the directory denoted by this abstract pathname.
	 *<P>
	 * There is no guarantee that the name strings in the resulting array
	 * will appear in any specific order; they are not, in particular,
	 * guaranteed to appear in alphabetical order.
	 *<P>
	 * If this IRODSFile object denotes a file, the directory containing
	 * that file will be listed instead.
	 *<P>
	 * This method will return all the files in the directory. Listing
	 * directories with a large number of files may take a very long time.
	 * The more generic IRODSFile.query() method could be used to iterate
	 * through the file list piecewise.
   *
	 * @return  An array of strings naming the files and directories in the
	 *          directory denoted by this abstract pathname.
	 */
	public String[] list( )
	{
    return list( null );
	}
  
	public String[] list( MetaDataCondition[] conditions )
	{    
		MetaDataSelect selects[] = {
			MetaDataSet.newSelection( GeneralMetaData.FILE_NAME ),
			MetaDataSet.newSelection( GeneralMetaData.DIRECTORY_NAME ),
    };
		MetaDataRecordList[] rl1, rl2, temp;
		Vector list = null;
		String path, parent;
    
    MetaDataCondition con[] = null;
    if (conditions == null) {
		 con = new MetaDataCondition[1];      
    }
    else {
		 con = new MetaDataCondition[conditions.length+1];
     System.arraycopy(conditions,0,con,1,conditions.length);
    }

		try {
			//Have to do two queries, one for files and one for directories.
			if (isDirectory()) {
				path = getAbsolutePath();
			}
			else {
				path = getParent();
			}

			//get all the files
			con[0] = MetaDataSet.newCondition(
				GeneralMetaData.DIRECTORY_NAME, MetaDataCondition.EQUAL, path );
			rl1 = fileSystem.query( con, selects );
      /*TODO
			if (completeDirectoryList) {
				rl1 = MetaDataRecordList.getAllResults( rl1 );
			}
       */
			//get all the sub-directories
			selects[0] = null;
			con[0] = MetaDataSet.newCondition(
				IRODSMetaDataSet.PARENT_DIRECTORY_NAME, MetaDataCondition.EQUAL, path );
			rl2 = iRODSFileSystem.query( con, selects );
/*TODO			if (completeDirectoryList) {
				rl2 = MetaDataRecordList.getAllResults( rl2 );
			}
*/
			//change to relative path
			if (rl2 != null) {
				String absolutePath = null;
				String relativePath = null;
				for (int i=0;i<rl2.length;i++) {
					//only one record per rl
					absolutePath = rl2[i].getStringValue(0);
					relativePath = absolutePath.substring(
						absolutePath.lastIndexOf( "/" )+1 );
					rl2[i].setValue( 0, relativePath );
				}
			}
		} catch ( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
			return null;
		}


		if (( rl1 != null ) && (rl2 != null)) {
			//length of previous query + (new query - table and attribute names)
			temp = new MetaDataRecordList[rl1.length+rl2.length];
			//copy files
			System.arraycopy( rl1, 0, temp, 0, rl1.length );
			System.arraycopy( rl2, 0, temp, rl1.length, rl2.length );
		}
		else if (rl1 != null) {
			temp = rl1;
		}
		else if (rl2 != null) {
			temp = rl2;
		}
		else {
			return new String[0];
		}

		list = new Vector();
		for (int i=0;i<temp.length;i++) {
			if (temp[i].getStringValue(0) != null) {
				//only one record per rl
				list.add(temp[i].getStringValue(0));
			}
		}

		return (String[]) list.toArray(new String[0]);
	}
  
	/**
	 * Creates the directory named by this abstract pathname.
	 */
	public boolean mkdir( )
	{
		try {
			if (!isDirectory()) {
        iRODSFileSystem.commands.mkdir( this );
        return true;
			}
		} catch ( IOException e ) {
      if (iRODSFileSystem.DEBUG > 0) e.printStackTrace();
		}
		return false;
	}
  
    
  
	/**
	 * Renames the file denoted by this abstract pathname.
	 *<P>
	 * Whether or not this method can move a file from one filesystem to
	 * another is platform-dependent. The return value should always be
	 * checked to make sure that the rename operation was successful.
	 *
	 * @param  dest  The new abstract pathname for the named file
	 *
	 * @throws  IllegalArgumentException
	 *          If parameter <code>dest</code> is not a <code>GeneralFile</code>.
	 * @throws NullPointerException - If dest is null
	 */
	public boolean renameTo( GeneralFile dest )
		throws IllegalArgumentException, NullPointerException
	{
    try {
      if (dest instanceof IRODSFile) {
        try {
          iRODSFileSystem.commands.rename(this, (IRODSFile)dest);
        } catch (IOException e) {
          //irods don't share the same rcat? try again
          if (!dest.exists()) {
            copyTo( dest );
            delete();
          }
          else
            return false;             
        }
      }
      else {
        if (!dest.exists()) {
          copyTo( dest );
          delete();
        }
        else
          return false;      
      }
    } catch( IOException e ) {
      return false;
    }
    
    return true;
	}
  

	/**
	 * Returns a string representation of this file object.
	 * The string is formated according to the SRB URI model.
	 * Note: the user password will not be included in the URI.
	 */
	public String toString( )
	{
		return new String( "irods://"+iRODSFileSystem.getUserName()+
			"@"+iRODSFileSystem.getHost()+":"+
			iRODSFileSystem.getPort() + getAbsolutePath() );
	}
}
