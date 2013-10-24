//	Copyright (c) 2005, Regents of the University of California
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
//	IRODSCommands.java	-  edu.sdsc.grid.io.irods.IRODSCommands
//
//  CLASS HIERARCHY
//	java.lang.Object
//     |
//     +-.IRODSCommands
//
//  PRINCIPAL AUTHOR
//	Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.irods;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.local.*;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Date;
import java.util.Vector;

import java.security.MessageDigest;
import java.security.GeneralSecurityException;


/**
 * Instances of this class support socket I/O to a iRODS server.
 *<P>
 * Handles socket level protocol for interacting with the iRODS.
 *
 * <P>
 * @author	Lucas Gilbert, San Diego Supercomputer Center
 * @since   JARGON2.0
 */
class IRODSCommands
{
  //TODO
  private long date;

	/**
	 * A positive debug value turns on debugging. Higher values turn on more, maybe.
	 */
	private static int DEBUG = 0;
	static {
		//Set the debug, default zero.
		try {
			DEBUG = new Integer( System.getProperty( "jargon.debug", "0")).intValue();
		} catch (java.lang.NumberFormatException e) {
			//in case they don't use an integer
			DEBUG = 0;
		}
	}
  
  
  
	/**
	 * 16 bit char
	 */
	public static final int CHAR_LENGTH = 2;

	/**
	 * 16 bit short
	 */
	public static final int SHORT_LENGTH = 2;

	/**
	 * 32 bit integer
	 */
	public static final int INT_LENGTH = 4;

	/**
	 * 64 bit long
	 */
	public static final int LONG_LENGTH = 8;


	/**
	 * Size of the socket send buffer
	 */
  static int OUTPUT_BUFFER_LENGTH = GeneralFile.BUFFER_MAX_SIZE;


  /**
   * 4 bytes at the front of the header, outside XML
   */
  static final int HEADER_INT_LENGTH = 4;

  /**
   * Maximum password length. Used in challenge response.
   */
  static final int MAX_PASSWORD_LENGTH = 50;
  /**
   * Standard challenge length. Used in challenge response.
   */
  static final int CHALLENGE_LENGTH = 64;

  /**
   * Max number of SQL attributes a query can return?
   */
  static final int MAX_SQL_ATTR = 50;
//TODO this class?



  // Various iRODS message types, in include/rodsDef.h
  static final String RODS_CONNECT    = "RODS_CONNECT";
  static final String RODS_VERSION    = "RODS_VERSION";
  static final String RODS_API_REQ    = "RODS_API_REQ";
  static final String RODS_DISCONNECT = "RODS_DISCONNECT";
  static final String RODS_REAUTH     = "RODS_REAUTH";
  static final String RODS_API_REPLY  = "RODS_API_REPLY";


  // Various iRODS message types, in include/api/dataObjInpOut.h
  // definition for oprType in dataObjInp_t, portalOpr_t and l1desc_t
  static final int DONE_OPR                = 9999;
  static final int PUT_OPR                 = 1;
  static final int GET_OPR                 = 2;
  static final int SAME_HOST_COPY_OPR      = 3;
  static final int COPY_TO_LOCAL_OPR       = 4;
  static final int COPY_TO_REM_OPR         = 5;
  static final int REPLICATE_OPR           = 6;
  static final int REPLICATE_DEST          = 7;
  static final int REPLICATE_SRC           = 8;
  static final int COPY_DEST               = 9;
  static final int COPY_SRC                = 10;
  static final int RENAME_DATA_OBJ         = 11;
  static final int RENAME_COLL             = 12;
  static final int MOVE_OPR                = 13;
  static final int RSYNC_OPR               = 14;
  static final int PHYMV_OPR               = 15;
  static final int PHYMV_SRC               = 16;
  static final int PHYMV_DEST              = 17;


  /* from apiNumber.h - header file for API number assignment */
  /* 500 - 599 - Internal File I/O API calls */
  static final int FILE_CREATE_AN                 = 500;
  static final int FILE_OPEN_AN                   = 501;
  static final int FILE_WRITE_AN                  = 502;
  static final int FILE_CLOSE_AN                  = 503;
  static final int FILE_LSEEK_AN                  = 504;
  static final int FILE_READ_AN                   = 505;
  static final int FILE_UNLINK_AN                 = 506;
  static final int FILE_MKDIR_AN                  = 507;
  static final int FILE_CHMOD_AN                  = 508;
  static final int FILE_RMDIR_AN                  = 509;
  static final int FILE_STAT_AN                   = 510;
  static final int FILE_FSTAT_AN                  = 511;
  static final int FILE_FSYNC_AN                  = 512;
  static final int FILE_STAGE_AN                  = 513;
  static final int FILE_GET_FS_FREE_SPACE_AN      = 514;
  static final int FILE_OPENDIR_AN                = 515;
  static final int FILE_CLOSEDIR_AN               = 516;
  static final int FILE_READDIR_AN                = 517;
  static final int FILE_PUT_AN                    = 518;
  static final int FILE_CHKSUM_AN                 = 520;
  static final int CHK_N_V_PATH_PERM_AN           = 521;
  static final int FILE_RENAME_AN                 = 522;

  /* 600 - 699 - Object File I/O API calls */
  static final int DATA_OBJ_CREATE_AN             = 601;
  static final int DATA_OBJ_OPEN_AN               = 602;
  static final int DATA_OBJ_READ_AN               = 603;
  static final int DATA_OBJ_WRITE_AN              = 604;
  static final int DATA_OBJ_CLOSE_AN              = 605;
  static final int DATA_OBJ_PUT_AN                = 606;
  static final int DATA_PUT_AN                    = 607;
  static final int DATA_OBJ_GET_AN                = 608;
  static final int DATA_GET_AN                    = 609;
  static final int DATA_OBJ_REPL_AN               = 610;
  static final int DATA_COPY_AN                   = 611;
  static final int DATA_OBJ_LSEEK_AN              = 612;
  static final int DATA_OBJ_COPY_AN               = 613;
  static final int SIMPLE_QUERY_AN                = 614;
  static final int DATA_OBJ_UNLINK_AN             = 615;
  static final int COLL_CREATE_AN                 = 616;
  static final int RM_COLL_AN                     = 617;
  static final int REG_COLL_AN                    = 618;
  static final int REG_DATA_OBJ_AN                = 619;
  static final int UNREG_DATA_OBJ_AN              = 620;
  static final int REG_REPLICA_AN                 = 621;
  static final int MOD_DATA_OBJ_META_AN           = 622;
  static final int RULE_EXEC_SUBMIT_AN            = 623;
  static final int RULE_EXEC_DEL_AN               = 624;
  static final int EXEC_MY_RULE_AN                = 625;
  static final int OPR_COMPLETE_AN                = 626;
  static final int DATA_OBJ_RENAME_AN             = 627;
  static final int DATA_OBJ_RSYNC_AN              = 628;
  static final int DATA_OBJ_CHKSUM_AN             = 629;
  static final int PHY_PATH_REG_AN                = 630;
  static final int DATA_OBJ_PHYMV_AN              = 631;
  static final int DATA_OBJ_TRIM_AN               = 632;
  static final int OBJ_STAT_AN                    = 633;

  /* 700 - 799 - Metadata API calls */
  static final int GET_MISC_SVR_INFO_AN           = 700;
  static final int GENERAL_ADMIN_AN               = 701;
  static final int GEN_QUERY_AN                   = 702;
  static final int AUTH_REQUEST_AN                = 703;
  static final int AUTH_RESPONSE_AN               = 704;
  static final int AUTH_CHECK_AN                  = 705;
  static final int MOD_AVU_METADATA_AN            = 706;
  static final int MOD_ACCESS_CONTROL_AN          = 707;
  static final int RULE_EXEC_MOD_AN               = 708;

  //iRODS communication types
  //Simple tags
  static TreeMap tagList = new TreeMap();

  //typical header tags
  static final String type = "type";
  static final String msgLen = "msgLen";
  static final String errorLen = "errorLen";
  static final String bsLen = "bsLen";
  static final String intInfo = "intInfo";

  //leaf tags
  static final String irodsProt = "irodsProt";
  static final String connectCnt = "connectCnt";
  static final String proxyUser = "proxyUser";
  static final String proxyRcatZone = "proxyRcatZone";
  static final String clientUser = "clientUser";
  static final String clientRcatZone = "clientRcatZone";
  static final String relVersion = "relVersion";
  static final String apiVersion = "apiVersion";
  static final String option = "option";
  static final String status = "status";
  static final String challenge = "challenge";
  static final String response = "response";
  static final String username = "username";
  static final String objPath = "objPath";
  static final String createMode = "createMode";
  static final String openFlags = "openFlags";
  static final String offset = "offset";
  static final String dataSize = "dataSize";
  static final String numThreads = "numThreads";
  static final String oprType = "oprType";
  static final String ssLen = "ssLen";
  static final String objSize = "objSize";
  static final String objType = "objType";
  static final String numCopies = "numCopies";
  static final String dataId = "dataId";
  static final String chksum = "chksum";
  static final String ownerName = "ownerName";
  static final String ownerZone = "ownerZone";
  static final String createTime = "createTime";
  static final String modifyTime = "modifyTime";
  static final String inx = "inx";
  static final String maxRows = "maxRows";
  static final String continueInx = "continueInx";
  static final String ivalue = "ivalue";
  static final String svalue = "svalue";
  static final String iiLen = "iiLen";
  static final String isLen = "isLen";
  static final String keyWord = "keyWord";
  static final String rowCnt = "rowCnt";
  static final String attriCnt = "attriCnt";
  static final String attriInx = "attriInx";
  static final String reslen = "reslen";
  static final String queryValue = "value";
  static final String collName = "collName";
  static final String recursiveFlag = "recursiveFlag";
  static final String accessLevel = "accessLevel";
  static final String userName = "userName";
  static final String zone = "zone";
  static final String path = "path";
  static final String l1descInx = "l1descInx";
  static final String len = "len";
  static final String fileInx = "fileInx";
  static final String whence = "whence";
  static final String dataObjInx = "dataObjInx";
  static final String bytesWritten = "bytesWritten";
  static final String msg = "msg";


  //Complex tags
  static final String MsgHeader_PI = "MsgHeader_PI";
  static final String StartupPack_PI = "StartupPack_PI";
  static final String Version_PI = "Version_PI";

  static final String authRequestOut_PI = "authRequestOut_PI";
  static final String authResponseInp_PI = "authResponseInp_PI";

  static final String DataObjInp_PI = "DataObjInp_PI";
  static final String GenQueryInp_PI = "GenQueryInp_PI";
  static final String InxIvalPair_PI = "InxIvalPair_PI";
  static final String InxValPair_PI = "InxValPair_PI";
  static final String KeyValPair_PI = "KeyValPair_PI";
  static final String RodsObjStat_PI = "RodsObjStat_PI";
  static final String SqlResult_PI = "SqlResult_PI";
  static final String CollInp_PI = "CollInp_PI";
  static final String DataObjCopyInp_PI = "DataObjCopyInp_PI";
  static final String modAccessControlInp_PI = "modAccessControlInp_PI";
  static final String dataObjReadInp_PI = "dataObjReadInp_PI";
  static final String dataObjWriteInp_PI = "dataObjWriteInp_PI";
  static final String fileLseekInp_PI = "fileLseekInp_PI";
  static final String dataObjCloseInp_PI = "dataObjCloseInp_PI";
  static final String RErrMsg_PI = "RErrMsg_PI";






/*
  static {
    tagList.put(MsgHeader_PI, new String[]
      {type, msgLen, errorLen, bsLen, intInfo} );
    tagList.put(StartupPack_PI, new String[]
      {irodsProt, connectCnt, proxyUser, proxyRcatZone,clientUser,
       clientRcatZone, relVersion, apiVersion, option} );
    tagList.put(Version_PI, new String[] {status, relVersion, apiVersion} );
    tagList.put(authRequestOut_PI, new String[] {challenge} );
    tagList.put(authResponseInp_PI, new String[] {response, username} );


    tagList.put(DataObjInp_PI, new String[]
        {objPath, createMode, openFlags, offset, dataSize, numThreads, oprType,
         KeyValPair_PI} );
    tagList.put(KeyValPair_PI, new String[] {ssLen} );

  }
*/

//----------------------------------------------------------------------
//  Fields
//----------------------------------------------------------------------
	/**
	 * The iRODS socket connection through which
	 * all socket activity is directed.
	 */
	private Socket connection;

	/**
	 * The input stream of the iRODS socket connection.
	 */
	private InputStream in = null;

	/**
	 * The output stream of the iRODS socket connection.
	 */
	private OutputStream out = null;

	/**
	 * Buffer output to the socket.
	 */
	private byte outputBuffer[] = new byte[OUTPUT_BUFFER_LENGTH];


	/**
	 * Holds the offset into the outputBuffer array for adding new data.
	 */
	private int outputOffset = 0;

	/**
	 * Hold the iRODS account info
	 */
  private IRODSAccount account;



//----------------------------------------------------------------------
//  Constructors and Destructors
//----------------------------------------------------------------------
  /**
   * Creates a new instance of IRODSCommands
   */
  IRODSCommands()
  {


  }


	/**
	 * Finalizes the object by explicitly letting go of each of
	 * its internally held values.
	 *<P>
	 * @throws IOException If can't close socket.
	 */
	protected void finalize( )
		throws IOException
	{
		close();
		if (out != null) {
			out = null;
		}
		if (in != null) {
			in = null;
		}
		if (connection != null) {
			connection = null;
		}
	}



//----------------------------------------------------------------------
// Connection methods
//----------------------------------------------------------------------
  /**
   * Handles connection protocol.
   *
   * @throws	IOException	if the host cannot be opened or	created.
   */
  void connect( IRODSAccount account )
    throws IOException
  {
    Tag message;
    String out;

    this.account = (IRODSAccount) account.clone();

		if (DEBUG > 1) {
      date = new Date().getTime();
			System.err.println("Connecting to server, "+account.getHost()+
        ":"+account.getPort()+" running version: "+account.version+
        " as username: "+account.getUserName()+"\ntime: "+date );
    }

    //
    // Initial connection to irods server
    //
    openSocket( account.getHost(), account.getPort() );

    //
    // Send the user info
    //
    message = sendStartupPacket(account);
    status(message);
    //TODO check the version in message

    //
    //Request for authorization challenge
    //
    send( createHeader(RODS_API_REQ,0,0,0,AUTH_REQUEST_AN) );
		flush( );
    message = readMessage();

    //
    //Create and send the response
    //
    String response = challengeResponse(
      message.getTag(challenge).getStringValue(), account.getPassword());
    message = new Tag(authResponseInp_PI, new Tag[]{
        new Tag(this.response, response),
        new Tag(this.username, account.getUserName()),
    } );

    try {
      //should be a header with no body if successful
      message = irodsFunction( RODS_API_REQ, message, AUTH_RESPONSE_AN );
    } catch (IRODSException e) {
      if (e.getType() == IRODSException.CAT_INVALID_AUTHENTICATION) {
        SecurityException se = new SecurityException( "Invalid authentication" );
        se.initCause(e);
        throw se;
      }
      else {
        throw e;
      }
    }


    //TODO if (success) account.setPassword( null );
  }

	/**
	 * Close the connection to the server. This method has been sycnhronized
	 * so the socket will not be blocked when the socket.close() call is made.
	 *
	 * @throws IOException	Socket error
	 */
	synchronized void close( )
		throws IOException
	{
		if (isConnected()) {
      send( createHeader(RODS_DISCONNECT,0,0,0,0) );
      flush();
			out.close();
			in.close();
			connection.close();
		}
	}


	/**
	 * Returns the closed state of the socket.
	 *
	 * @return true if the socket has been closed,
   *  or is not connected
	 */
	synchronized boolean isClosed( )
		throws IOException
	{
    //So if null, maybe it just isn't connected yet, but I think for safety...
    if(connection == null)
      return true;
    if(connection.isClosed())
      return true;
    if(out == null)
      return true;
    if(in == null)
      return true;

    return false;
	}


//----------------------------------------------------------------------
// Initial Handshake Methods
//----------------------------------------------------------------------
	/**
	 * Handles sending the userinfo connection protocol.
	 * First, sends initial handshake with IRODS.
	 * <P>
	 * @throws	IOException	if the host cannot be opened or	created.
	 */
  private Tag sendStartupPacket( IRODSAccount account )
		throws IOException
  {
/*    
    String out2 = "<StartupPack_PI>" +
      "<irodsProt>"+"1"+"</irodsProt>" +
      "<connectCnt>"+"0"+"</connectCnt>" +
      "<proxyUser>" +account.getUserName()+ "</proxyUser>" +
      "<proxyRcatZone>" +account.getZone()+ "</proxyRcatZone>" +
      "<clientUser>" +account.getUserName()+ "</clientUser>" +
      "<clientRcatZone>" +account.getZone()+ "</clientRcatZone>" +
      "<relVersion>" +account.getVersion()+ "</relVersion>" +
      "<apiVersion>" +account.getAPIVersion()+ "</apiVersion>" +
      "<option>" +account.getOption()+ "</option>" +
      "</StartupPack_PI>";
*/
    Tag startupPacket = new Tag(StartupPack_PI, new Tag[]{
      new Tag(irodsProt, "1"),
      new Tag(connectCnt, "0"),
      new Tag(proxyUser, account.getUserName()),
      new Tag(proxyRcatZone, account.getZone()),
      new Tag(clientUser, account.getUserName()),
      new Tag(clientRcatZone, account.getZone()),
      new Tag(relVersion, account.getVersion()),
      new Tag(apiVersion, account.getAPIVersion()),
      new Tag(option, account.getOption()),
    } );
    String out = startupPacket.parseTag();
    send(createHeader(RODS_CONNECT,out.length(),0,0,0));
    send(out);
    flush( );
    
    return readMessage();
  }

  /**
   * Add the password to the end of the challenge string,
   * pad to the correct length, and take the md5 of that.
   */
  private String challengeResponse( String challenge, String password )
    throws SecurityException, IOException
  {
    // Convert base64 string to a byte array
    byte[] chal = null;
    byte[] temp = Base64.fromString(challenge);
      //new sun.misc.BASE64Decoder().decodeBuffer(challenge);

    if (password.length() < MAX_PASSWORD_LENGTH) {
      //pad the end with zeros to MAX_PASSWORD_LENGTH
      chal = new byte[CHALLENGE_LENGTH+MAX_PASSWORD_LENGTH];
    }
    else {
      throw new IllegalArgumentException( "Password is too long" );
    }

    //add the password to the end
    System.arraycopy(temp, 0, chal, 0, temp.length);
    temp = password.getBytes();
    System.arraycopy(temp, 0, chal, CHALLENGE_LENGTH, temp.length);

    //get the md5 of the challenge+password
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      chal = digest.digest(chal);
    } catch ( GeneralSecurityException e ) {
      SecurityException se = new SecurityException();
      se.initCause(e);
      throw se;
    }

    //after md5 turn any 0 into 1
    for (int i=0;i<chal.length;i++)
    {
      if (chal[i] == 0)
        chal[i] = 1;
    }

    //return to Base64
    return Base64.toString(chal);
      //new sun.misc.BASE64Encoder().encode( chal );
  }

//----------------------------------------------------------------------
// Socket Methods
//----------------------------------------------------------------------
	/**
	 * Open a connection to the server.
	 *
	 * @param host		Name of the host to connect to
	 * @param port		Port on that host
	 * @throws ConnectException	if the connection cannot be made
	 * @throws SocketException	A socket error occured
	 * @throws IOException if a IOException occurs
	 */
	private void openSocket( String host, int port )
		throws IOException
	{
		try {
			connection = new Socket( host, port );
			in = connection.getInputStream();
			out = connection.getOutputStream();
		}
		catch (ConnectException e) {
			ConnectException connException = new ConnectException(
				"Connection cannot be made to: "+ host + " at port: "+ port);
			connException.initCause(e);
			throw connException;
		}
		catch ( SocketException e ) {
			SocketException socketException = new SocketException(
				"A socket error occured when connecting to: "+ host +
				" at port: "+ port);
			socketException.initCause(e);
			throw socketException;
		}
	}


	/**
	 * Checks if the socket is connected.
	 */
	boolean isConnected()
	{
    try {
      if (connection != null) {
        if (connection.isConnected()) {
          if (!connection.isClosed()) {
            //can be both connected and closed, but that isn't what I meant.
            return true;
          }
        }
      }
    } catch (Throwable e) {
      if (DEBUG > 0) e.printStackTrace();
    }
		return false;
	}


	/**
	 * Writes value.length bytes to this output stream.
	 *
	 * @param value		value to be sent
	 * @throws NullPointerException	Send buffer is empty
	 * @throws IOException	If an IOException occurs
	 */
	private void send( byte[] value )
		throws IOException
	{
    if (( value.length + outputOffset ) >= OUTPUT_BUFFER_LENGTH) {
			//in cases where OUTPUT_BUFFER_LENGTH isn't big enough
			out.write( outputBuffer, 0, outputOffset );
			out.write( value );
			out.flush();
			outputOffset = 0;
		}
    else {
			//the message sent isn't longer than OUTPUT_BUFFER_LENGTH
			System.arraycopy( value, 0, outputBuffer, outputOffset, value.length);
			outputOffset += value.length;
		}
	}

	/**
	 * Writes a certain length of bytes at some offset
	 * in the value array to the output stream,
	 * by converting the value to a byte array and calling
	 * send( byte[] value ).
	 *
	 * @param value		value to be sent
	 * @param offset	offset into array
	 * @param length	number of bytes to read
	 * @throws IOException	If an IOException occurs
	 */
	private void send( byte[] value, int offset, int length )
		throws IOException
	{
		byte temp[] = new byte[length];

		System.arraycopy( value, offset, temp, 0, length);

		send( temp );
	}


	/**
	 * Writes value.length bytes to this output stream.
	 *
	 * @param value		value to be sent
	 * @throws IOException	If an IOException occurs
	 */
	private void send( String value )
		throws IOException
	{
		send( value.getBytes() );
	}

	/**
	 * Writes an int to the output stream as four bytes, low byte first.
	 *
	 * @param value		value to be sent
	 * @throws IOException	If an IOException occurs
	 */
	private void send( int value )
		throws IOException
	{
		byte bytes[] = new byte[INT_LENGTH];

		Host.copyInt( value, bytes );
		Host.swap( bytes, INT_LENGTH );

		send( bytes );
	}

	/**
	 * Writes an long to the output stream as eight bytes, low byte first.
	 *
	 * @param value		value to be sent
	 * @throws IOException	If an IOException occurs
	 */
	private void send( long value )
		throws IOException
	{
		byte bytes[] = new byte[LONG_LENGTH];

		Host.copyLong( value, bytes );
		Host.swap( bytes, LONG_LENGTH );

		send( bytes );
	}


	/**
	 * Writes an long to the output stream as eight bytes, low byte first.
	 *
	 * @param value		value to be sent
	 * @throws IOException	If an IOException occurs
	 */
	private void send( InputStream source, long length )
		throws IOException
	{
    byte[] temp = new byte[Math.min(IRODSFileSystem.BUFFER_SIZE,(int)length)];
    while (length > 0) {
      if (temp.length > length) {
        temp = new byte[(int)length];
      }
      length -= source.read(temp, 0, temp.length);
      send(temp);
    }
	}


	/**
	 * Flushes all data in the output stream and sends it to the server.
	 *
	 * @throws NullPointerException	Send buffer empty
	 * @throws IOException	If an IOException occurs
	 */
	void flush( )
		throws IOException
	{
		if ( connection.isClosed() ) {
      //hopefully this isn't too slow to check.
			throw new ClosedChannelException();
		}
    out.write( outputBuffer, 0, outputOffset );

		outputOffset = 0;
	}






	//
	//Input
	//
	/**
	 * Reads a byte from the server.
	 *
	 * @throws IOException	If an IOException occurs
	 */
	private byte read()
		throws IOException
	{
		return (byte) in.read();
	}


  private int read( byte[] value )
		throws ClosedChannelException, InterruptedIOException, IOException
  {
    return read(value, 0, value.length);
  }


  /**
   * read length bytes from the server socket connection and write them to
   * destination
   */
  private void read( OutputStream destination, long length )
		throws IOException
  {
    byte[] temp = new byte[Math.min(IRODSFileSystem.BUFFER_SIZE,(int)length)];
    while (length > 0) {
      if (temp.length > length) {
        temp = new byte[(int)length];
      }
      length -= read(temp, 0, temp.length);
      destination.write(temp);
    }
  }

	/**
	 * Reads a byte array from the server.
	 *
	 * @param length	length of byte array to be read
	 * @return byte[]	bytes read from the server
	 * @throws OutOfMemoryError	Read buffer overflow
	 * @throws ClosedChannelException if the connection is closed
	 * @throws NullPointerException	Read buffer empty
	 * @throws IOException	If an IOException occurs
	 */
	private int read( byte[] value, int offset, int length )
		throws ClosedChannelException, InterruptedIOException, IOException
	{
    int n = 0;
		if (length <= 0) {
			return 0;
		}
    else if (offset >= value.length) {
      return 0;
    }
    else if (length + offset > value.length) {
throw new IllegalArgumentException("debuging protocol read");
    }


		try {
			//Can only read 1448 bytes in each loop
			int maxReadSize = 1448;
			int temp = 0;
			if (length > maxReadSize) {
				while ((length > (n + maxReadSize - 1)) && (n >= 0)) {
					n += in.read( value, n, maxReadSize );
				}
				while (((length - n - 1) > 0) && (n >= 0)) {
					n +=	in.read( value, n, (length - n) );
				}
			}
			else {
				while (((length - n) > 0) && (n >= 0)) {
					n +=	in.read( value, n, (length - n) );
				}
			}

//apparently I still need the 1448.
//      n = in.read(value,offset,length);

      if (n < 0) {
        throw new SocketException("iRODS socket connection is closed.");
      }
		}
		catch ( IOException e ) {
			IOException ioException = new IOException(
				"read() -- couldn't read complete packet");
			ioException.initCause(e);
			throw ioException;
		}
/*
		if (DEBUG > 5) {
      System.err.print("Read: "+new String(value));
      if (DEBUG > 6) {
        for (int i=0;i<value.length;i++) {
          System.err.print(value[i]+" ");
        }
      }
    }
*/
		return n;
	}

  /**
   * Just a simple message to check if there was an error.
   */
  void status( Tag message )
    throws IOException
  {
    Tag s = message.getTag("status");
    if ((s != null) && (s.getIntValue() < 0))
      throw new IRODSException(""+s.getIntValue());
  }

//----------------------------------------------------------------------
// Struct Methods
//----------------------------------------------------------------------
  /**
   * Create the iRODS header packet
   */
  private byte[] createHeader( String type, int messageLength,
    int errorLength, long byteStringLength, int intInfo )
  {
    String header = "<MsgHeader_PI>" +
      "<type>" +type+ "</type>" +
      "<msgLen>" +messageLength+ "</msgLen>" +
      "<errorLen>" +errorLength+ "</errorLen>" +
      "<bsLen>" +byteStringLength+ "</bsLen>" +
      "<intInfo>"+intInfo+"</intInfo>" +
      "</MsgHeader_PI>";
    byte[] length = new byte[4];
    byte[] full = new byte[4+header.length()];
    Host.copyInt( header.length(), length );
    System.arraycopy(length,0,full,0,4);
    System.arraycopy(header.getBytes(),0,full,4,header.length());
    return full;
  }


  private Tag readMessage( )
    throws IOException
  {
    Tag header = readHeader();
    Tag message = null;

    //TODO probably shouldn't be so hardcoded...
    String type       = header.tags[0].getStringValue();
    if (true) {//type.equals()) {
//TODO which types?
      int messageLength = header.tags[1].getIntValue();
      int errorLength   = header.tags[2].getIntValue();
      int bytesLength   = header.tags[3].getIntValue();
      int info          = header.tags[4].getIntValue();


      //Reports iRODS errors, throw exception if appropriate
      if (info < 0) {
        //if nothing else, read the returned bytes and throw them away
        if (messageLength > 0)
          read(new byte[messageLength], 0, messageLength);

        if (info == IRODSException.CAT_NO_ROWS_FOUND ) {
          //query with no results
          return null;
        }
        else if (info == IRODSException.OVERWITE_WITHOUT_FORCE_FLAG ) {
          //TODO keep?
          throw new IRODSException(
            "Attempt to overwrite file without force flag. ", info);
        }
        else {
          if (errorLength != 0) {
            //TODO only occur if info is < 0?
            byte[] errorMessage = new byte[errorLength];
            read(errorMessage, 0, errorLength);
            Tag errorTag = readNextTag(errorMessage);        
            
            throw new IRODSException("IRODS error occured "+errorTag.getTag(RErrMsg_PI).getTag(msg), info);
          }
          throw new IRODSException("IRODS error occured "+info, info);
        }
      }

      if (messageLength > 0) {
        message = readMessageBody(messageLength);
      }
      if (bytesLength != 0 || info > 0) {
        if (message == null) {
          message = new Tag(MsgHeader_PI);
        }

        //lets the bytes get read later,
        //instead of passing a 32MB buffer around.
        message.addTag(header);
      }
    }

    return message;
  }

  /**
   * Going to read the header somewhat differently
   */
  private Tag readHeader( )
    throws IOException
  {
    int length = readHeaderLength();
    byte[] header = new byte[length];
    read(header, 0, length);

    return readNextTag(header);
  }

  private int readHeaderLength( )
    throws IOException
  {
    byte[] headerInt = new byte[HEADER_INT_LENGTH];
    read(headerInt, 0, HEADER_INT_LENGTH);
    return Host.castToInt(headerInt);
  }

  private Tag readMessageBody( int length )
    throws IOException
  {
    byte[] body = new byte[length];
    read(body, 0, length);
    return readNextTag(body);
  }


//TODO maybe can make this just one function instead of two...
  /**
   * Read the data buffer to discover the first tag. Fill the values of that tag
   * according to the above defined static final values.
   */
  private Tag readNextTag( byte[] data )
  {
    if (data == null) return null;

    //TODO see how String.getBytes works, if this is efficient
    String d = new String(data);
    int start = d.indexOf(OPEN_START_TAG),
        end = d.indexOf(CLOSE_START_TAG,start);
    if (start < 0) return null;

    String tagName = d.substring(start+1,end);
    end = d.lastIndexOf(OPEN_END_TAG+tagName+CLOSE_END_TAG);

    Tag tag = new Tag(tagName);
    String value = d.substring(start+tagName.length()+2,end);
    if (value.indexOf(OPEN_START_TAG) > 2) {
      tag.setValue(value);
    }
    else {
      while (value.indexOf(OPEN_START_TAG) >= 0 && start >= 0 &&
        start < value.length())
      {
        //send the rest of the bytes read
        value = value.substring(start);
        start = readSubTag( tag, value.getBytes() );
      }
    }
    //maybe a final '\n' left. just ignore it.
    //if (value.length > 1) TODO?

    return tag;
  }

  private int readSubTag( Tag tag, byte[] data )
  {
    if (data == null)
    {
      //Protocol problem?
      return Integer.MAX_VALUE;
    }
    int m,n;
    //TODO see how String.getBytes works, if this is efficient
    String d = new String(data);
    int start = d.indexOf(OPEN_START_TAG),
        end = d.indexOf(CLOSE_START_TAG,start);
    if ((start < 0) && d.length() == 0) {
      return 1;
    }
    else if (start < 0 || end < 0) {
      return end;  //TODO
    }

    String tagName = d.substring(start+1,end);
    end = d.indexOf(OPEN_END_TAG+tagName+CLOSE_END_TAG);
    if (end < 0) {
      //TODO used to fix the missing of the closing tag of subtags.
      //a bit inefficient
      return d.indexOf(CLOSE_START_TAG,start)+1;
    }
    Tag subTag = new Tag(tagName);
    tag.addTag(subTag);

    String value = d.substring(start+tagName.length()+2,end);
//    if (value.indexOf(OPEN_START_TAG) > 2) {
//TODO need the second part?
    m = value.indexOf(OPEN_START_TAG);
    n = value.indexOf(OPEN_END_TAG);
    if (m < 0)
    {
      subTag.setValue(value);
      end+=tagName.length()+4; //endtag, 4, </>\n
    }
    else if (m == n)
    {
      //TODO no longer needed?
      //means a complex tag, because a new tag came up before this one closed
      subTag.setValue(value.substring(0,n));
      //tell it to pass over
      end= start+tagName.length()+2+  //start tag
        n+                            //value
        tagName.length()+4;           //end tag, 4, </>\n
    }
    else {
      //continue reading all the sub tags and adding their values
      while (value.indexOf(OPEN_START_TAG) >= 0 && start >= 0 &&
        start < value.length())
      {
        //send the rest of the bytes read
        value = value.substring(start);
        start = readSubTag( subTag, value.getBytes() );
      }
    }
    return end;
  }



  /**
   * Creates the KeyValPair_PI tag.
   */
  Tag createKeyValueTag( String keyword, String value )
  {
    return createKeyValueTag( new String[][]{{keyword,value}} );
  }


  /**
   * Creates the KeyValPair_PI tag.
   */
  Tag createKeyValueTag( String[][] keyValue )
  {
    /*
      Must be like the following:
      <KeyValPair_PI>
      <ssLen>3</ssLen>
      <keyWord>dataType</keyWord>
      <keyWord>destRescName</keyWord>
      <keyWord>dataIncluded</keyWord>
      <svalue>generic</svalue>
      <svalue>resourceB</svalue>
      <svalue></svalue>
      </KeyValPair_PI>
    */

    Tag pair = new Tag(KeyValPair_PI, new Tag(ssLen, 0));
    int i = 0;

    //return the empty Tag
    if (keyValue == null) return pair;

    for ( ;i<keyValue.length;i++) {
      pair.addTag( keyWord, keyValue[i][0] );
    }

    //just use index zero because they have to be in order...
    pair.tags[0].setValue(i);
    if (i == 0) return pair;

    for (i=0;i<keyValue.length;i++) {
      pair.addTag( svalue, keyValue[i][1] );
    }

    return pair;
  }



//----------------------------------------------------------------------
// Basic irodsFunction format
//----------------------------------------------------------------------
  /**
   * Create a typical iRODS api call Tag
   */
  Tag irodsFunction( String type, Tag message, int intInfo )
  	throws IOException
  {
    return irodsFunction( type, message, 0, null, 0, null, intInfo );
  }

  /**
   * Create an iRODS message Tag, including header.
   * Send the bytes of the byte array, no error stream.
   */
  Tag irodsFunction( String type, Tag message,
    byte[] errorStream, int errorOffset, int errorLength,
    byte[] bytes, int byteOffset, int byteStringLength,
    int intInfo )
  	throws IOException
  {
    String out = message.parseTag();
    send( createHeader( RODS_API_REQ, out.length(), errorLength,
      byteStringLength, intInfo ) );
    send( out );
    if (byteStringLength > 0)
      send(bytes, byteOffset, byteStringLength);
    flush( );
    return readMessage();
  }

  /**
   * Create an iRODS message Tag, including header.
   */
  Tag irodsFunction( String type, Tag message,
    int errorLength, InputStream errorStream,
    long byteStringLength, InputStream byteStream,
    int intInfo )
  	throws IOException
  {
    String out = message.parseTag();
    send( createHeader( RODS_API_REQ, out.length(), errorLength,
      byteStringLength, intInfo ) );
    send( out );
    if (errorLength > 0)
      send(errorStream, errorLength);
    if (byteStringLength > 0)
      send(byteStream, byteStringLength);
    flush( );
    return readMessage();
  }



//----------------------------------------------------------------------
// irods functions
//----------------------------------------------------------------------
  void chmod( IRODSFile file, String permission, String user,
    boolean recursive )
    throws IOException
  {
    Tag message = new Tag(modAccessControlInp_PI, new Tag[]{
        new Tag(recursiveFlag, recursive? 1:0),
        new Tag(accessLevel, permission),
        new Tag(userName, user),
        new Tag(zone, ""),
        new Tag(path, file.getAbsolutePath()),
    } );

    irodsFunction( RODS_API_REQ, message, MOD_ACCESS_CONTROL_AN );
  }


  void copy( IRODSFile source, IRODSFile destination, boolean overwriteFlag )
    throws IOException
  {
    String[][] keyword = null;
    if (overwriteFlag) {
      keyword = new String[][] {
        { IRODSMetaDataSet.FORCE_FLAG_KW, "" },
        { IRODSMetaDataSet.DEST_RESC_NAME_KW, destination.getResource() },
      };
    }
    else {
      keyword = new String[][] {
        { IRODSMetaDataSet.DEST_RESC_NAME_KW, destination.getResource() },
      };
    }

    Tag message = new Tag(DataObjCopyInp_PI, new Tag[]{
      //define the source
      new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, source.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, source.length()),
        new Tag(numThreads, 0),
        new Tag(oprType, COPY_SRC),
        createKeyValueTag( null ),
      } ),
      //define the destination
      new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, destination.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, COPY_DEST),
        createKeyValueTag( keyword ),
      } ),
    } );

    irodsFunction( RODS_API_REQ, message, DATA_OBJ_COPY_AN );
  }

  void deleteDirectory( IRODSFile file )
    throws IOException
  {
    Tag message = new Tag(CollInp_PI, new Tag[]{
        new Tag(collName, file.getAbsolutePath()),
        createKeyValueTag( IRODSMetaDataSet.RECURSIVE_OPR__KW, "" ),
    } );

    irodsFunction( RODS_API_REQ, message, RM_COLL_AN );
  }


  void deleteFile( IRODSFile file )
    throws IOException
  {
    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, file.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, 0),
        createKeyValueTag( null ),
    } );

    irodsFunction( RODS_API_REQ, message, DATA_OBJ_UNLINK_AN );
  }

  //POSIX commands
  int fileCreate( IRODSFile file )
    throws IOException
  {
    String[][] keyword = new String[][] {
      { IRODSMetaDataSet.DATA_TYPE_KW, file.getDataType() },
      { IRODSMetaDataSet.DEST_RESC_NAME_KW, file.getResource() },
    };
    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, file.getAbsolutePath()),
        new Tag(createMode, 488), //octal for 750  owner has rwx, group? has r+x
        new Tag(openFlags, 1),
        new Tag(offset, 0),
        new Tag(dataSize, -1),
        new Tag(numThreads, 0),
        new Tag(oprType, 0),
        createKeyValueTag( keyword ),
    } );

    message = irodsFunction( RODS_API_REQ, message, DATA_OBJ_CREATE_AN );
    return message.getTag(MsgHeader_PI).getTag(intInfo).getIntValue();
  }


  void fileClose( int fd )
    throws IOException
  {
    Tag message = new Tag(dataObjCloseInp_PI, new Tag[]{
        new Tag(l1descInx, fd),
        new Tag(bytesWritten, 0),
    } );

    irodsFunction( RODS_API_REQ, message, DATA_OBJ_CLOSE_AN );
  }


  int fileOpen( IRODSFile file )
    throws IOException
  {
    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, file.getAbsolutePath()),
        new Tag(createMode, 0), //can ignore on open
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, 0),
        createKeyValueTag( null ),
    } );

    message = irodsFunction( RODS_API_REQ, message, DATA_OBJ_OPEN_AN );
    return message.getTag(MsgHeader_PI).getTag(intInfo).getIntValue();
  }

  /**
   * Read a file to the given stream.
   */
  int fileRead( int fd, OutputStream destination, long length )
    throws IOException
  {
    Tag message = new Tag(dataObjReadInp_PI, new Tag[]{
        new Tag(l1descInx, fd),
        new Tag(len, length),
    } );

    message = irodsFunction( RODS_API_REQ, message, DATA_OBJ_READ_AN );
    //Need the total dataSize
    length = message.getTag(MsgHeader_PI).getTag(bsLen).getIntValue();

    //read the message byte stream into the local file
    read( destination, length );

    return message.getTag(MsgHeader_PI).getTag(intInfo).getIntValue();
  }

  /**
   * Read a file into the given byte array.
   */
  int fileRead( int fd, byte buffer[], int offset, int length )
    throws IOException
  {
    Tag message = new Tag(dataObjReadInp_PI, new Tag[]{
        new Tag(l1descInx, fd),
        new Tag(len, length),
    } );

    message = irodsFunction( RODS_API_REQ, message, DATA_OBJ_READ_AN );
    //Need the total dataSize
    length = message.getTag(MsgHeader_PI).getTag(bsLen).getIntValue();

    //read the message byte stream into the local file
    int read = read( buffer, offset, length );

    if (read == message.getTag(MsgHeader_PI).getTag(intInfo).getIntValue()) {
      return read;
    }
    else {
      //TODO exception ok?
      throw new ProtocolException("Bytes read mismatch");
    }
  }


  int fileSeek( int fd, long seek, int whence )
    throws IOException
  {
    Tag message = new Tag(fileLseekInp_PI, new Tag[]{
        new Tag(fileInx, fd),
        new Tag(offset, seek),
        new Tag(this.whence, whence),
    } );

    message = irodsFunction( RODS_API_REQ, message, DATA_OBJ_LSEEK_AN );
    return message.getTag(offset).getIntValue();
  }


  /**
   * Write a file into the given InputStream.
   */
  int fileWrite( int fd, InputStream source, long length )
    throws IOException
  {
    Tag message = new Tag(dataObjWriteInp_PI, new Tag[]{
        new Tag(dataObjInx, fd),
        new Tag(len, length),
    } );

    message = irodsFunction( RODS_API_REQ, message, 0, null, length, source,
      DATA_OBJ_WRITE_AN );
    return message.getTag(MsgHeader_PI).getTag(intInfo).getIntValue();
  }

  /**
   * Write a file into the given byte array.
   */
  int fileWrite( int fd, byte buffer[], int offset, int length )
    throws IOException
  {
    Tag message = new Tag(dataObjWriteInp_PI, new Tag[]{
        new Tag(dataObjInx, fd),
        new Tag(len, length),
    } );

    message = irodsFunction( RODS_API_REQ, message, null, 0, 0,
      buffer, offset, length,
      DATA_OBJ_WRITE_AN );
    return message.getTag(MsgHeader_PI).getTag(intInfo).getIntValue();

  }



  void get( IRODSFile source, LocalFileOutputStream destination,
    boolean overwriteFlag )
    throws IOException
  {
    String[][] keyword = null;
    if (overwriteFlag) {
      keyword = new String[][] {
        { IRODSMetaDataSet.FORCE_FLAG_KW, "" },
      };
    }
    long a = source.length();

    int length = 0;
    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, source.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, GET_OPR),
        createKeyValueTag( keyword ),
    } );

    message = irodsFunction( RODS_API_REQ, message, DATA_OBJ_GET_AN );

    //Need the total dataSize
    Tag tt =  message.getTag(MsgHeader_PI);
    length = message.getTag(MsgHeader_PI).getTag(bsLen).getIntValue();

    //length = Integer.MAX_VALUE;
    //read the message byte stream into the local file
    //long test = source.length();
   // GeneralFile gf = source.getAbsoluteFile();
 //   gf.length()
    read( destination, a );
  }



  /**
   *
   */
  void mkdir( IRODSFile directory )
    throws IOException
  {
    if (directory == null) {
      throw new NullPointerException("Directory path cannot be null");
    }
    Tag message = new Tag(CollInp_PI, new Tag[]{
        new Tag(collName, directory.getAbsolutePath()),
        createKeyValueTag( null ),
    } );

    irodsFunction( RODS_API_REQ, message, COLL_CREATE_AN );
  }



  void put( GeneralFileInputStream source, long length, boolean overwriteFlag,
    IRODSFile destination )
    throws IOException
  {
    String[][] keyword = null;
    if (overwriteFlag) {
      keyword = new String[][] {
        { IRODSMetaDataSet.DATA_TYPE_KW, destination.getDataType() },
        { IRODSMetaDataSet.FORCE_FLAG_KW, "" },
        { IRODSMetaDataSet.DEST_RESC_NAME_KW, destination.getResource() },
        { IRODSMetaDataSet.DATA_INCLUDED_KW, "" }, //blank it seems
      };
    }
    else {
      keyword = new String[][] {
        { IRODSMetaDataSet.DATA_TYPE_KW, destination.getDataType() },
        { IRODSMetaDataSet.DEST_RESC_NAME_KW, destination.getResource() },
        { IRODSMetaDataSet.DATA_INCLUDED_KW, "" }, //blank it seems
      };
    }

    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, destination.getAbsolutePath()),
        new Tag(createMode, 448), //octal for 700  owner has rw
        new Tag(openFlags, 1),
        new Tag(offset, 0),
        new Tag(dataSize, length),
        new Tag(numThreads, 0),
        new Tag(oprType, PUT_OPR),
        createKeyValueTag( keyword ),
    } );

    //send the message, no result expected.
    //exception thrown on error.
    irodsFunction( RODS_API_REQ, message, 0, null,
      length, source, DATA_OBJ_PUT_AN );
  }



  /**
   * Send a query to iRODS
   */
	MetaDataRecordList[] query( MetaDataCondition[] conditions,
		MetaDataSelect[] selects, int numberOfRecordsWanted )
  	throws IOException
  {
    Tag message = new Tag( GenQueryInp_PI, new Tag[] {
      new Tag( maxRows, numberOfRecordsWanted ),
      new Tag( continueInx, 0 ), //new query
      createKeyValueTag( null ),
    } );
    Tag[] subTags = null;
    int j=1;

    //package the selects
    subTags = new Tag[selects.length*2+1];
    subTags[0] = new Tag( iiLen, selects.length );
    for (int i=0;i<selects.length;i++) {
      subTags[j] = new Tag( inx,
        IRODSMetaDataSet.getID( selects[i].getFieldName() ) );
      j++;
    }
    for (int i=0;i<selects.length;i++) {
      //New for loop because they have to be in a certain order...
      subTags[j] = new Tag( ivalue, selects[i].getOperation() );
      j++;
    }
    message.addTag( new Tag( InxIvalPair_PI, subTags ) );

    //package the conditions
    subTags = new Tag[conditions.length*2+1];
    subTags[0] = new Tag( isLen, conditions.length );
    j=1;
    for (int i=0;i<conditions.length;i++) {
      subTags[j] = new Tag( inx,
        IRODSMetaDataSet.getID( conditions[i].getFieldName() ) );
      j++;
    }
    for (int i=0;i<conditions.length;i++) {
      //New for loop because they have to be in a certain order...
      subTags[j] = new Tag( svalue, " "+conditions[i].getOperatorString()+
        " '"+conditions[i].getStringValue()+"'");
      j++;
    }
    message.addTag( new Tag( InxValPair_PI, subTags ) );

    message = irodsFunction( RODS_API_REQ, message, GEN_QUERY_AN );

    if (message == null) {
      //query had no results
      return null;
    }

    int rows = message.getTag(rowCnt).getIntValue();
    int attributes = message.getTag(attriCnt).getIntValue();
    int continuation = message.getTag(continueInx).getIntValue();

    String[] results = new String[attributes];
    MetaDataField[] fields = new MetaDataField[attributes];
    MetaDataRecordList[] rl = new MetaDataRecordList[rows];
    for (int i=0;i<attributes;i++) {
      //TODO just hard-coded the first "SqlResult_PI" result location...
      //definately TODO
      fields[i] = IRODSMetaDataSet.getField(
        message.tags[4+i].getTag(attriInx).getStringValue());
    }
    for (int i=0;i<rows;i++) {
      for (j=0;j<attributes;j++) {
        //TODO just hard-coded the first "SqlResult_PI" result location...
        results[j] = message.tags[4+j].tags[2+i].getStringValue();
      }
      rl[i] = new IRODSMetaDataRecordList( fields, results, continuation );
    }

    return rl;
  }


  void rename( IRODSFile source, IRODSFile destination )
    throws IOException
  {
    Tag message = new Tag(DataObjCopyInp_PI, new Tag[]{
      //define the source
      new Tag(DataObjInp_PI, new Tag[]{
        new Tag(collName, source.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, RENAME_DATA_OBJ),
        createKeyValueTag( null ),
      } ),
      //define the destination
      new Tag(DataObjInp_PI, new Tag[]{
        new Tag(collName, destination.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, RENAME_DATA_OBJ),
        createKeyValueTag( null ),
      } ),
    } );

    irodsFunction( RODS_API_REQ, message, DATA_OBJ_RENAME_AN );
  }

  void replicate( IRODSFile file, String newResource )
    throws IOException
  {
    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, file.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, REPLICATE_OPR),
        createKeyValueTag( IRODSMetaDataSet.DEST_RESC_NAME_KW, newResource ),
    } );

    irodsFunction( RODS_API_REQ, message, DATA_OBJ_REPL_AN );
  }

  String[] stat( IRODSFile file )
    throws IOException
  {
    String[] data;
    Tag message = new Tag(DataObjInp_PI, new Tag[]{
        new Tag(objPath, file.getAbsolutePath()),
        new Tag(createMode, 0),
        new Tag(openFlags, 0),
        new Tag(offset, 0),
        new Tag(dataSize, 0),
        new Tag(numThreads, 0),
        new Tag(oprType, 0),
        createKeyValueTag( null ),
    } );

    irodsFunction( RODS_API_REQ, message, OBJ_STAT_AN );

//TODO convert result
/*
<RodsObjStat_PI>
<objSize>5249</objSize>
<objType>1</objType>
<numCopies>1</numCopies>
<dataId>10014</dataId>
<chksum></chksum>
<ownerName>iktome</ownerName>
<ownerZone>tempZone</ownerZone>
<createTime>2007-04-20-17.27.19</createTime>
<modifyTime>2007-04-20-17.27.19</modifyTime>
</RodsObjStat_PI>
*/
    data = null;

    return data;
  }



//----------------------------------------------------------------------
// Tag Class
//----------------------------------------------------------------------
  static final String OPEN_START_TAG = "<";
  static final String CLOSE_START_TAG = ">";
  static final String OPEN_END_TAG = "</";
  static final String CLOSE_END_TAG = ">";
  /**
   * Eh, seemed easier. The other java-XML parsers were troublesome.
   * Though I'm bound to have a few bugs so probably end up about the same.
   */
  class Tag implements Cloneable
  {
    /**
     * iRODS name of the tag
     */
    String tagName;

    /**
     * all the sub tags
     */
    Tag[] tags;

    /**
     * probably a string...
     */
    String value;


    Tag( String tagName )
    {
      this.tagName = tagName;
    }

    Tag( String tagName, int value )
    {
        this.tagName = tagName;
        this.value = ""+value;
    }

    Tag( String tagName, long value )
    {
        this.tagName = tagName;
        this.value = ""+value;
    }

    Tag( String tagName, String value )
    {
        this.tagName = tagName;
        this.value = value;
    }

    Tag( String tagName, Tag tag )
    {
        this(tagName, new Tag[]{tag});
    }
    Tag( String tagName, Tag[] tags )
    {
      this.tagName = tagName;
      this.tags = tags;
    }

    void setValue( int value )
    {
      this.value = ""+value;
    }

    void setValue( long value )
    {
      this.value = ""+value;
    }

    void setValue( String value )
    {
      this.value = value;
    }


    void setTagValues( Object[] values )
    {
      if (values instanceof Tag[]) {
        tags = (Tag[]) values;
      }
      else if (tags.length != values.length) {
        throw new IllegalArgumentException("Schema mismatch "+this+
          " does not have these "+values+"   "+values.length+" values.");
      }
      else {
        for (int i=0;i<tags.length;i++) {
          if (values[i] instanceof String) {
            //just set this leaf value
            tags[i].setValue((String)values[i]);
          }
          else if (values[i] instanceof Tag) {
            tags[i] = (Tag) values[i];
          }
          else if (values[i] instanceof Object[]) {
            tags[i].setTagValues((Object[])values[i]);
          }
          else {
            throw new IllegalArgumentException("Protocol error: This "+
              values[i]+" shouldn't be here");
          }
        }
      }
    }


    Object getValue( )
    {
  //TODO clone so it can't over write when set value is called?
      if (tags != null)
        return tags.clone();
      else
        return value;
    }

    int getIntValue( )
    {
      return Integer.parseInt(value);
    }

    String getStringValue( )
    {
      return value;
    }

    String getName( )
    {
      return tagName;
    }

    int getLength( ) {
      return tags.length;
    }

    Tag getTag( String tagName )
    {
      if (tags == null) return null;

      //see if tagName exists in first level
      //if it isn't the toplevel, just leave it.
      for (int i=0;i<tags.length;i++) {
        if (tags[i].getName().equals(tagName))
          return tags[i];
      }
      return null;
    }

    Tag[] getTags( )
    {
      //clone so it can't over write when set value is called?
      if (tags != null)
        return tags;
      else
        return null;
    }

    /**
     * Returns the values of this tags subtags. Which are probably more
     * tags unless we've finally reached a leaf.
     */
    Object[] getTagValues()
    {
        if (tags == null) return null;

        Object[] val = new Object[tags.length];
        for (int i=0;i<tags.length;i++) {
            val[i] = tags[i].getValue();
        }
        return val;
    }

    void addTag( String name, String val )
    {
      addTag( new Tag( name, val ) );
    }

    void addTag( Tag add )
    {
        if (tags != null) {
            Tag[] temp = tags;
            tags = new Tag[temp.length+1];
            System.arraycopy(temp, 0, tags, 0, temp.length);
            tags[temp.length] = add;
        }
        else {
            tags = new Tag[]{add};
        }
    }

    void addTags( Tag[] add )
    {
        if (tags != null) {
            Tag[] temp = tags;
            tags = new Tag[temp.length+add.length];
            System.arraycopy(temp, 0, tags, 0, temp.length);
            System.arraycopy(add, 0, tags, temp.length, add.length);
        }
        else {
            tags = add;
        }
    }

    public Object clone()
      throws CloneNotSupportedException
    {
      return super.clone();
    }

    public boolean equals( Object obj )
    {
      if (obj instanceof Tag) {
        Tag newTag = (Tag) obj;
        if (newTag.getName().equals(tagName)) {
          if (newTag.getValue().equals(value)) {
            if (newTag.getTags().equals(tags)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    public String toString()
    {
      return tagName;
    }

    /**
     * Outputs a string to send communications (function calls) to the
     * iRODS server. All values are strings
     */
    String parseTag( )
    {
//TODO though if something isn't a string and you try to send a
//non-printable character this way, it will get all messed up.
//maybe just send a '?' instead.
      StringBuffer parsed =
        new StringBuffer(OPEN_START_TAG + tagName + CLOSE_START_TAG);
      if (tags != null) {
        for (int i=0;i<tags.length;i++) {
          parsed.append(tags[i].parseTag());
        }
      }
      else {
        parsed.append(value);
      }
      parsed.append(OPEN_END_TAG + tagName + CLOSE_END_TAG + "\n");

      return parsed.toString();
    }
/*
    boolean isLeaf( )
    {
      if (tags != null)
        return false;
      else
        return true;
    }
 */
  }
}
