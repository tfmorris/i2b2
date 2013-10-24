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
//	IRODSAccount.java	-  edu.sdsc.grid.io.IRODSAccount
//
//  CLASS HIERARCHY
//	java.lang.Object
//	    |
//	    +-edu.sdsc.grid.io.GeneralAccount
//	 			   |
//	 			   +-.RemoteAccount
//	 						   |
//	 						   +-.irods.IRODSAccount
//
//  PRINCIPAL AUTHOR
//	Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.irods;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.local.*;
import java.io.*;
import java.util.*;

/**
 * This class extends the RemoteAccount class, adding those values necessary
 * to open a connection to a iRODS server. This class does not actually connect
 * to a filesystem. It only hold user connection information. Setting
 * or getting this information only refers to the contents of the object.
 *<P>
 * @author	Lucas Gilbert, San Diego Supercomputer Center
 * @since   JARGON2.0
 * @see		edu.sdsc.grid.io.irods.RodsFileSystem
 */
public class IRODSAccount extends RemoteAccount
{
//----------------------------------------------------------------------
//  Constants
//----------------------------------------------------------------------
	/**
	 * iRODS version 1.0
	 */
	public static final String IRODS_VERSION_0_9 = "rods0.9jargon2.0";
  
	/**
	 * iRODS version 1.0
	 */
	public static final String IRODS_VERSION_1_0 = "rods1.0jargon2.0";
  
	/**
	 * iRODS API version "b"
	 */
	public static final String IRODS_API_VERSION = "b";
  


//----------------------------------------------------------------------
//  Fields
//----------------------------------------------------------------------
	/**
	 * The default storage resource.
	 */
	protected String defaultStorageResource;


	/**
	 * The iRODS authorization scheme.
	 */
	protected String authenticationScheme = "PASSWORD";
  
	/**
	 * The iRODS Server DN string.
	 */
	protected String serverDN;
  
  
	/**
	 * The iRODS zone.
	 */
	protected String zone;
  

	/**
	 * The iRODS version.
	 */
	protected static String version = IRODS_VERSION_0_9;

	/**
	 * The iRODS API version.
	 */
  static String apiVersion = IRODS_API_VERSION;

  
	/**
	 * working with strings got annoying
	 */
	static HashMap versionNumber = new HashMap( 10, 1 );
	static {
		versionNumber.put( IRODS_VERSION_0_9, new Float( .9 ) );
		versionNumber.put( IRODS_VERSION_1_0, new Float( 1 ) );

//    internalSetVersion( null );
  }  
  
  
//----------------------------------------------------------------------
//  Constructors and Destructors
//----------------------------------------------------------------------
	/**
	 * This constructor uses the default info found in the iRODS environment files 
   * in the user's local home directory.
	 *
	 * @throws FileNotFoundException if the user info cannot be found.
	 * @throws	IOException if the user info exists but cannot be opened or
	 *			created for any other reason.
	 */
	public IRODSAccount( )
		throws FileNotFoundException, IOException
	{
		//Can't actually do anything until the .iRODS files have been read.
		super( "", 0, "", "", "" );

		LocalFile info = new LocalFile(System.getProperty("user.home")+"/.irods/");
		if (!info.exists()) {
			//Windows Scommands doesn't setup as "."
			info = new LocalFile(System.getProperty("user.home")+"/irods/");
		}

		if (!info.exists())
			throw new FileNotFoundException(
				"Cannot find default iRODS account info" );

		setUserInfo( info );
	}


	/**
	 * Creates an object to hold iRODS account information.
	 * <P>
	 * @param	userInfoDirectory directory holding the .iRODS files
	 * @throws FileNotFoundException if the user info cannot be found.
	 * @throws	IOException if the user info exists but cannot be opened or
	 *			created for any other reason.
	 */
	public IRODSAccount( GeneralFile userInfoDirectory )
		throws FileNotFoundException, IOException
	{
		//Can't actually do anything until the .Mdas files have been read.
		super( "", 0, "", "", "" );

		if ( userInfoDirectory.equals(null) )
			throw new NullPointerException("UserInfoDirectory cannot be null");

		setUserInfo( userInfoDirectory );
	}
  
  
	/**
	 * Creates an object to hold iRODS account information.
	 * <P>
	 * @param	envFile Location of the ".irodsEnv" file.
	 * @param	authFile Location of the ".irodsAuth" file.
	 * @throws FileNotFoundException if the user info cannot be found.
	 * @throws	IOException if the user info exists but cannot be opened or
	 *			created for any other reason.
	 */
	public IRODSAccount( GeneralFile envFile, GeneralFile authFile )
		throws FileNotFoundException, IOException
	{
		//Can't actually do anything until the .iRODS files have been read.
		super( "", 0, "", "", "" );

		if ( envFile.equals(null) || authFile.equals(null)  )
			throw new NullPointerException("iRODS files cannot be null");

		setUserInfo( envFile );
	}



	/**
	 * Creates an object to hold iRODS account information.
	 * This constructor does not use any default info.
	 * <P>
	 * @param	host the iRODS server domain name
	 * @param	port the port on the iRODS server
	 * @param	userName the user name
	 * @param	password the password
	 * @param	homeDirectory home directory on the iRODS
	 * @param	defaultStorageResource default storage resource
	 */
/*  public IRODSAccount( String host, int port, String userName, String password,
		String homeDirectory, String defaultStorageResource )
	{
		super( host, port, userName, password, homeDirectory );

		setUserName( userName );
		setDefaultStorageResource( defaultStorageResource );
	}
*/  
  
	/**
	 * Creates an object to hold iRODS account information.
	 * This constructor does not use any default info.
	 * <P>
	 * @param	host the iRODS server domain name
	 * @param	port the port on the iRODS server
	 * @param	userName the user name
	 * @param	password the password
	 * @param	homeDirectory home directory on the iRODS
	 * @param	zone the IRODS zone
	 * @param	defaultStorageResource default storage resource
	 */
  public IRODSAccount( String host, int port, String userName, String password,
		String homeDirectory, String zone, String defaultStorageResource )
	{
		super( host, port, userName, password, homeDirectory );

		setUserName( userName );
		setZone( zone );
		setDefaultStorageResource( defaultStorageResource );
	}
  


	/**
	 * Finalizes the object by explicitly letting go of each of
	 * its internally held values.
	 * <P>
	 */
	protected void finalize( )
	{
		super.finalize();
    
	}



//----------------------------------------------------------------------
// Setters and Getters
//----------------------------------------------------------------------
	/**
	 * Sets the port of this IRODSeAccount. Port numbers can not be negative.
	 */
	public void setPort( int port )
	{
		if (port > 0)
			this.port = port;
		else {
			this.port = 1247;
		}
	}
  
  
	/**
	 * Sets the home directory of this RemoteAccount.
	 *
	 * @throws	NullPointerException	if homeDirectory is null.
	 */
	public void setHomeDirectory( String homeDirectory )
	{
		if ( homeDirectory == null )
			throw new NullPointerException(
				"The home directory string cannot be null");

		this.homeDirectory = homeDirectory;
	}
  
	/**
	 * Sets the default storage resource.
	 *
	 * @throws	NullPointerException	if defaultStorageResource is null.
	 */
	public void setDefaultStorageResource( String defaultStorageResource )
	{
		if ( defaultStorageResource == null ) {
			throw new NullPointerException(
				"The default storage resource cannot be null");
		}

		this.defaultStorageResource = defaultStorageResource;
	}

	/**
	 * Set the type of authentication used.
	 */
	public void setAuthenticationScheme( String scheme )
	{
		authenticationScheme = scheme;
	}

  public void setZone( String zone )
  {
    this.zone = zone;
  }
  
	/**
	 * Set the version of the iRODS server this client should use when connecting.
	 */
	void setVersion( String version )
	{
		this.version = version;
	}
  
  
	/**
	 * Gets the default storage resource.
	 *
	 * @return defaultStorageResource
	 */
	public String getDefaultStorageResource( )
	{
		return defaultStorageResource;
	}

	/**
	 * Gets the iRODS options.
	 *
	 * @return options
	 */
	public String getAuthenticationScheme()
	{
		return authenticationScheme;
	}

	/**
	 * Gets the iRODS version.
	 *
	 * @return version
	 */
	public static String getVersion( )
	{
		return version;
	}

	/**
	 * Gets the iRODS version.
	 *
	 * @return version
	 */
	static float getVersionNumber( )
	{
		return ((Float) versionNumber.get( version )).floatValue();
	}
  

	/**
	 * Gets the iRODS API version.
	 *
	 * @return version
	 */
	static String getAPIVersion( )
	{
		return apiVersion;
	}
  
	/**
	 * Gets the iRODS option. (Not sure what that is...)
	 *
	 * @return version
	 */
  static int getOption( ) 
  {
    return 0;
  }

  
	/**
	 * @return the Server DN string used by the client.
	 */
	public String getServerDN( )
	{
		return serverDN;
	}

  
	/**
	 * @return the Server DN string used by the client.
	 */
	public String getZone( )
	{
		return zone;
	}


//----------------------------------------------------------------------
// Object Methods
//----------------------------------------------------------------------
	/**
	 * Tests this local file system account object for equality with the
	 * given object.
	 * Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and both are account objects for the same
	 * filesystem.
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

			IRODSAccount temp = (IRODSAccount) obj;

			if (!getHost().equals(temp.getHost()))
				return false;
			if (getPort() != temp.getPort())
				return false;
			if (!getUserName().equals(temp.getUserName()))
				return false;
			if (!getPassword().equals(temp.getPassword()))
				return false;

//TODO correct? everything included?

      //else //everything is equal
        return true;
		} catch (ClassCastException e) {
			return false;
		}
	}


 	/**
	 * Returns a string representation of this file system object.
	 * The string is formated according to the SRB URI model.
	 * Note: the user password will not be included in the URI.
	 */
	public String toString( )
	{
		return new String( "irods://"+getUserName()+"@"+getHost()+":"+getPort() );
	}



//----------------------------------------------------------------------
// UserInfo Methods
//----------------------------------------------------------------------
//TODO
  /**
	 * Reads the iRODS enviroment files to set the user info.
	 *
	 * @param userInfo The path to the user info file
	 */
	public void setUserInfo( GeneralFile userInfo )
		throws FileNotFoundException, IOException
	{
		GeneralFile env = FileFactory.newFile( userInfo, ".irodsEnv" );
		if (!env.exists()) {
			env = FileFactory.newFile( userInfo, "irodsEnv" );
		}

		int index = 0;
		GeneralFileInputStream envReader = null;
		byte envContents[] = new byte[(int) env.length()];

		//The values are inside 'single quotes',
		//so java.util.Properties gets them wrong.
		try {
			envReader = FileFactory.newFileInputStream( env );
		} catch ( FileNotFoundException e ) {
  		throw e;
		}

		envReader.read(envContents);
		String rcatEnv = new String(envContents);

    
		//Remove comments
		while (index >=0) {
      index = rcatEnv.indexOf("#", index);
			while (index >= 0) {
				rcatEnv = rcatEnv.substring( 0, index ) +
					rcatEnv.substring( rcatEnv.indexOf('\n', index+1), rcatEnv.length() );
        index = rcatEnv.indexOf("#", index);
			}
    
      //Sometimes has "" sometimes not. remove them
      index = rcatEnv.indexOf("\"", index);
			while (index >= 0) {
				rcatEnv = rcatEnv.substring( 0, index ) +
					rcatEnv.substring( index+1, rcatEnv.length() );
        index = rcatEnv.indexOf("\"", index);
			}
    
      //Sometimes has '' sometimes not. remove them
      index = rcatEnv.indexOf("'", index);
			while (index >= 0) {
				rcatEnv = rcatEnv.substring( 0, index ) +
					rcatEnv.substring( index+1, rcatEnv.length() );
        index = rcatEnv.indexOf("'", index);
			}
    
      //Sometimes has = sometimes not. remove them
      index = rcatEnv.indexOf("=", index);
			while (index >= 0) {
				rcatEnv = rcatEnv.substring( 0, index ) + " " +
					rcatEnv.substring( index+1, rcatEnv.length() );
        index = rcatEnv.indexOf("=", index);
			}
    
      //Sometimes has \r from windows. remove them.
      index = rcatEnv.indexOf("\r", index);
			while (index >= 0) {
				rcatEnv = rcatEnv.substring( 0, index ) +
					rcatEnv.substring( rcatEnv.indexOf('\n', index+1), rcatEnv.length() );
        index = rcatEnv.indexOf("\r", index);
			}
		}
    rcatEnv = rcatEnv + "\n";
    

		//host
		index = rcatEnv.indexOf("irodsHost");
		if (index < 0) {
			throw new NullPointerException( "No host name found in env file.");
		}
		index = rcatEnv.indexOf(' ', index)+1;
		setHost( rcatEnv.substring( index, rcatEnv.indexOf('\n', index)) );
    

		//port
		index = rcatEnv.indexOf("irodsPort");
		if (index < 0) {
			setPort( 1247 );
		}
		else {
			index = rcatEnv.indexOf(' ', index)+1;    
			setPort( Integer.parseInt( 
        rcatEnv.substring( index, rcatEnv.indexOf('\n', index))) );
		}


		//userName
		index = rcatEnv.indexOf("irodsUserName");
		if (index < 0) {
			throw new NullPointerException( "No user name found in env file.");
		}
		index = rcatEnv.indexOf(' ', index)+1;
		setUserName( rcatEnv.substring( index, rcatEnv.indexOf('\n', index)) );


		//defaultStorageResource
		index = rcatEnv.indexOf("irodsDefResource");
		if (index < 0) {
			throw new NullPointerException( "No default resource found in env file.");
		}
		index = rcatEnv.indexOf(' ', index)+1;
		setDefaultStorageResource( rcatEnv.substring(
			index,rcatEnv.indexOf('\n', index)) );

    
		//homeDirectory
		index = rcatEnv.indexOf("irodsHome");
		if (index >= 0) {
			index = rcatEnv.indexOf(' ', index)+1;
			setHomeDirectory( rcatEnv.substring(
				index, rcatEnv.indexOf('\n', index)) );
		}
    
		//zone
		index = rcatEnv.indexOf("irodsZone");
		if (index >= 0) {
			index = rcatEnv.indexOf(' ', index)+1;
			setZone( rcatEnv.substring(
				index, rcatEnv.indexOf('\n', index)) );
		}
/*
		//rodsVersion (for JARGON only)
		index = rcatEnv.indexOf("rodsVersion");
		if (index > 0) {
			index = rcatEnv.indexOf(' ', index)+1;
			setVersion( rcatEnv.substring( index, rcatEnv.indexOf('\n', index)) );
		}
*/
    //set the password
		env = FileFactory.newFile( userInfo, ".irodsA" );
		if (!env.exists()) {
			env = FileFactory.newFile( userInfo, "irodsA" );
		}
    readAuth(env);
	}
  
  
	/**
	 * Retrieve the Mdas authorization user password
	 *
	 * @param mdasAuthFile	The file which contains the Mdas authorization
	 */
	public void readAuth( GeneralFile authFile )
		throws FileNotFoundException, IOException
	{
		int index = 0;
		GeneralFileInputStream authReader =
      FileFactory.newFileInputStream( authFile );

		byte authContents[] = new byte[(int) authFile.length()];
		authReader.read(authContents);

		String auth = new String(authContents);

		StringTokenizer authTokens = new StringTokenizer(
			auth, System.getProperty( "line.separator" )+"\n");
		String token;
		while (authTokens.hasMoreTokens()) {
			token = authTokens.nextToken();

			if ( token.startsWith("#") ) {
				//ignore comments
			}
			else {
				index = token.indexOf(System.getProperty( "line.separator" )) +
					token.indexOf("\n") + 1;

				if (index >= 0)
					auth = token.substring( 0, index );
				else
					auth = token;
			}
		}

		setPassword( auth );
	}
}

