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
//  IRODSRodsFileSystem.java	-  edu.sdsc.grid.io.irods.IRODSFileSystem
//
//  CLASS HIERARCHY
//	java.lang.Object
//			|
//			+-edu.sdsc.grid.io.GeneralFileSystem
//						|
//						+-edu.sdsc.grid.io.RemoteFileSystem
//									|
//									+-edu.sdsc.grid.io.irods.IRODSFileSystem
//
//  PRINCIPAL AUTHOR
//	Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.irods;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.local.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Vector;



/**
 * The IRODSFileSystem class is the class for connection implementations
 * to iRods servers. It provides the framework to support a wide range
 * of iRODS semantics. Specifically, the functions needed to interact
 * with a iRODS server.
 *<P>
 *
 * @author	Lucas Gilbert, San Diego Supercomputer Center
 * @since   JARGON2.0
 * @see		edu.sdsc.grid.io.rods.RodsCommands
 */
public class IRODSFileSystem extends RemoteFileSystem
{
//----------------------------------------------------------------------
//  Constants
//----------------------------------------------------------------------
	/**
	 * The iRODS like Unix only has one root, "/".
	 */
	public static final String IRODS_ROOT = "/";

  static int BUFFER_SIZE = 65535;

  static int DEBUG = GeneralFileSystem.DEBUG;


//----------------------------------------------------------------------
//  Fields
//----------------------------------------------------------------------
  /**
	 * Use this account object instead of the parent class's
	 * GeneralAccount object.
   * Just so you don't have to recast it all the time.
	 */
	private IRODSAccount iRODSAccount;

  /**
   * All the socket and protocol methods to communicate with the irods server
   * use this object.
   */
  IRODSCommands commands;


//----------------------------------------------------------------------
//  Constructors and Destructors
//----------------------------------------------------------------------
	/**
	 * Opens a socket connection to read from and write to.
	 * Loads the default iRODS user account information from their home directory.
	 * The account information stored in this object cannot be changed once
	 * instantiated.
	 *<P>
	 * This constructor is provided for convenience however,
	 * it is recommended that all necessary data be sent
	 * to the constructor and not left to the defaults.
	 *
	 * @throws FileNotFoundException if the user data file cannot be found.
	 * @throws IOException if an IOException occurs.
	 */
  public IRODSFileSystem()
		throws IOException
  {
		this( new IRODSAccount() );
  }


	/**
	 * Opens a socket connection to read from and write to. Opens the account
	 * held in the IRODSAccount object. The account information stored in this
	 * object cannot be changed once constructed.
	 *
	 * @param iRODSAccount The iRODS account information object.
	 * @throws NullPointerException if IRODSAccount is null.
	 * @throws IOException if an IOException occurs.
	 */
	public IRODSFileSystem( IRODSAccount iRODSAccount )
		throws IOException, NullPointerException
	{
		setAccount( iRODSAccount );

		commands = new IRODSCommands(	);
    commands.connect( iRODSAccount );
	}


	/**
	 * Finalizes the object by explicitly letting go of each of
	 * its internally held values.
	 */
	protected void finalize( )
		throws Throwable
	{
    close();

		if (account != null)
			account = null;
		if (commands != null)
      commands = null;

    super.finalize();
	}


//----------------------------------------------------------------------
// Setters and Getters
//----------------------------------------------------------------------
//General
	/**
	 * Loads the account information for this file system.
	 */
	protected void setAccount( GeneralAccount account )
		throws IOException
	{
		if ( account == null )
			account = new IRODSAccount();

		 iRODSAccount = (IRODSAccount) account.clone();
		this.account = iRODSAccount;
	}

	/**
	 * Returns the account used by this IRODSFileSystem.
	 */
	public GeneralAccount getAccount( )
		throws NullPointerException
	{
		if ( iRODSAccount != null )
			return (IRODSAccount) iRODSAccount.clone();

		throw new NullPointerException();
	}


	/**
	 * Returns the root directories of the iRODS file system.
	 */
	public String[] getRootDirectories( )
	{
		String[] root = { IRODS_ROOT };

		return root;
	}


//Rods
	/**
	 * Only used by the IRODSFile( uri ) constructor.
	 */
	void setDefaultStorageResource( String resource )
	{
    iRODSAccount.setDefaultStorageResource( resource );
	}


	/**
	 * @return the default storage resource.
	 */
	public String getDefaultStorageResource( )
	{
		return iRODSAccount.getDefaultStorageResource();
	}

	/**
	 * @return the options
	 */
	public String getAuthenticationScheme( )
	{
		return iRODSAccount.getAuthenticationScheme();
	}

	/**
	 * @return the domain name used by the client.
	 * Only different from the proxyDomainName for ticketed users.
	 */
	public String getServerDN( )
	{
		return iRODSAccount.getServerDN();
	}

	/**
	 * @return the iRODS version
	 */
	public String getVersion( )
	{
		return iRODSAccount.version;
	}

  /**
	 * @return the version number
	 */
	public float getVersionNumber( )
	{
		return ((Float) iRODSAccount.versionNumber.get(iRODSAccount.version)).floatValue();
	}


//----------------------------------------------------------------------
// GeneralFileSystem methods
//----------------------------------------------------------------------
	/**
	 * Tests this filesystem object for equality with the given object.
	 * Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and both are filesystem objects connected to the
	 * same filesystem using the same account information.
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

			IRODSFileSystem temp = (IRODSFileSystem) obj;

			if (getAccount().equals(temp.getAccount())) {
				if (isConnected() == temp.isConnected()) {
					return true;
				}
			}
		} catch (ClassCastException e) {
			return false;
		}
		return false;
	}


	/**
	 * Checks if the socket is connected.
	 */
	public boolean isConnected( )
	{
		return commands.isConnected();
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



	/**
	 * Closes the connection to the SRB file system. The filesystem
	 * cannot be reconnected after this method is called. If this object,
	 * or another object which uses this filesystem, tries to send a
	 * command to the server a ClosedChannelException will be thrown.
	 */
	public void close( ) throws IOException
	{
		commands.close();
	}


	/**
	 * Returns if the connection to the SRB has been closed or not.
	 *
	 * @return true if the connection has been closed
	 */
	public boolean isClosed( ) throws IOException
	{
		return commands.isClosed();
	}


  /**
	 *
	 */
	public MetaDataRecordList[] query(
  	MetaDataCondition[] conditions, MetaDataSelect[] selects )
  	throws IOException
  {
    return query( conditions, selects,
      GeneralFileSystem.DEFAULT_RECORDS_WANTED );
  }

	/**
	 *
	 */
	public MetaDataRecordList[] query( MetaDataCondition[] conditions,
		MetaDataSelect[] selects, int numberOfRecordsWanted )
  	throws IOException
  {
    //TODO Duplicates? maybe they are && conditions
    conditions = (MetaDataCondition[]) cleanNulls(conditions);
    selects = (MetaDataSelect[]) cleanNulls(selects);
    return commands.query( conditions, selects, numberOfRecordsWanted );
  }


//----------------------------------------------------------------------
//
//----------------------------------------------------------------------
	/**
	 * Removes null values from an array.
	 */
	static final Object[] cleanNulls( Object[] obj )
	{
		Vector temp = new Vector(obj.length);
		boolean add = false;
		int i=0,j=0;

		for (i=0;i<obj.length;i++) {
			if (obj[i] != null) {
				temp.add(obj[i]);
        if (!add) add = true;
			}
		}
    if (!add) return null;

    //needs its own check
    if ((obj.length == 1) && (obj[0] == null)) {
  		return null;
		}

    return temp.toArray((Object[]) Array.newInstance(temp.get(0).getClass(), 0));
	}

	/**
	 * Removes null and duplicate values from an array.
	 */
	static final Object[] cleanNullsAndDuplicates( Object[] obj )
	{
    if (obj == null) return null;

		Vector temp = new Vector(obj.length);
		boolean anyAdd = false;
		int i=0,j=0;

		for (i=0;i<obj.length;i++) {
			if (obj[i] != null) {
        //need to keep them in original order
        //keep the first, remove the rest.
				for (j=i+1;j<obj.length;j++) {
					if (obj[i].equals(obj[j])){
            obj[j] = null;
						j = obj.length;
					}
				}

				if (obj[i] != null) {
					temp.add(obj[i]);
          if (!anyAdd) anyAdd = true;
				}
			}
		}
    if (!anyAdd) return null;

    //needs its own check
    if ((obj.length == 1) && (obj[0] == null)) {
  		return null;
		}

    return temp.toArray((Object[]) Array.newInstance(temp.get(0).getClass(), 0));
	}

}
