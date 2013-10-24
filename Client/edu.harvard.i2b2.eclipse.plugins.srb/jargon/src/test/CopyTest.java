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

import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBContainer;
import edu.sdsc.grid.io.srb.SRBException;
import edu.sdsc.grid.io.srb.SRBRandomAccessFile;
import edu.sdsc.grid.io.local.LocalFile;
import edu.sdsc.grid.io.local.LocalRandomAccessFile;

import edu.sdsc.grid.io.srb.*;
import edu.sdsc.grid.io.*;


import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Creates 5000+ files at the source location. Copy them to the remote
 * destination. Then copy them back to a new subdirectory in the source
 * location. Finally compare the md5 checksums to insure the transfer
 * was successful.
 */
public class CopyTest
{
	//following checks the md5sum
	//won't work without the md5sum proxy command
	static final String checksum = "md5sum";

	GeneralFile source, remoteDestination, returnDestination;


	/**
	 * Creates 5000+ files at the source location. Copy them to the remote
	 * destination. Then copy them back to a new subdirectory in the source
	 * location. Finally compare the md5 checksums to insure the transfer
	 * was successful.
	 */
	public CopyTest( GeneralFile source, GeneralFile remoteDestination )
		throws IOException
	{
		if (source.mkdir()) {
			GeneralRandomAccessFile raf = null;
			for (double i=0;i<5015;i++) {
				raf = FileFactory.newRandomAccessFile(
					FileFactory.newFile( source, "f"+i ), "rw" );
				raf.write(i+"\n");
				raf.write(new byte[(int)i]);
				raf.write(i+"\n");
				raf.write(new byte[(int)i]);
				raf.write(i+"\n");
				raf.close();
			}
			raf = FileFactory.newRandomAccessFile(
				FileFactory.newFile( source, "fbig1" ), "rw" );
			for (int i=0;i<10;i++) {
				raf.write(i+" ");
				raf.write( new byte[1000000] );
			}
			raf.close();
			raf = FileFactory.newRandomAccessFile(
				FileFactory.newFile( source, "fbig2" ), "rw" );
			for (int i=0;i<65;i++) {
				raf.write(i+" ");
				raf.write( new byte[1000000] );
			}
			raf.close();
		}

		this.source = source;
		this.remoteDestination = remoteDestination;
	}

	/**
	 * Uploads then downloads the files
	 */
	public void copy()
		throws IOException
	{
		double time = new Date().getTime();
		System.out.println("srb: "+remoteDestination+" time: "+time);
		((SRBFile)remoteDestination).copyFrom(source,true);
		System.out.println("upload time: "+(new Date().getTime()-time));

		returnDestination = FileFactory.newFile( source, "download" );
		((SRBFile)remoteDestination).copyTo(returnDestination,true,false);
		System.out.println("localFile: "+returnDestination);
		System.out.println("total copy time: "+(new Date().getTime()-time));
	}

	/**
	 * Compares the files.
	 *
	 * @return true, if and only if, all md5es match
	 */
	public boolean compare( )
		throws IOException
	{
		return compare( false );
	}

	/**
	 * Compares the files.
	 *
	 * @param remoteMd5 if true compare the source to the remote destination,
	 *  	otherwise compare source to files once again downloaded into
	 *  	source subdirectory.
	 * @return true, if and only if, all md5es match
	 */
	public boolean compare( boolean remoteMd5 )
		throws IOException
	{
		String srbChk = null;
		String localChk = null;
		Process proc = null;
		InputStream localIn = null, remoteIn = null;
		byte mds[] = new byte[33];
		boolean error = false;

		GeneralFile tempFile = null;

		if (remoteMd5) {
			if (!remoteDestination.exists()) return false;

			//compare source files md5es to md5es on the SRB
			MetaDataCondition conditions[] = {
				MetaDataSet.newCondition( SRBMetaDataSet.DIRECTORY_NAME,
					MetaDataCondition.EQUAL, remoteDestination.getAbsolutePath() )
			};

			String[] selectFieldNames = {
				SRBMetaDataSet.FILE_NAME,
				SRBMetaDataSet.PATH_NAME,
				SRBMetaDataSet.SIZE,
			};
			MetaDataSelect selects[] =
				MetaDataSet.newSelection( selectFieldNames );

			//TODO need more remote systems...
			MetaDataRecordList[] rl = ((SRBFileSystem) //(RemoteFileSystem)
				remoteDestination.getFileSystem()).query( conditions, selects );
			rl = MetaDataRecordList.getAllResults(rl);

			for (int i=0;i<rl.length;i++) {
				if(rl[i].getIntValue(2) > 0) {
					//TODO need more remote systems...
					remoteIn = ((SRBFileSystem) //(RemoteFileSystem)
						remoteDestination.getFileSystem()).executeProxyCommand(
							checksum, rl[i].getValue(SRBMetaDataSet.PATH_NAME).toString() );
					int result = remoteIn.read();
					while (result != -1) {
						srbChk += ""+(char)result;
						result = remoteIn.read();
					}
					srbChk += ""+(char)result;
          remoteIn.close();

					tempFile = FileFactory.newFile( source, rl[i].getValue(
							SRBMetaDataSet.FILE_NAME ).toString());
					proc = Runtime.getRuntime().exec("md5sum "+
						tempFile.getAbsolutePath());
					localIn = proc.getInputStream();
					localIn.read(mds);
					localChk = new String(mds);
          localIn.close();

//System.out.println("srb md5 "+srbChk);
//System.out.println("local md5 "+localChk);
					if (srbChk.indexOf(localChk) < 0) {
						System.out.println( "error "+tempFile );
						error = true;
					}
					srbChk = "";
				}
				System.out.print(i+" ");
			}
		}
		else {
			if (!returnDestination.exists()) return false;

			//compare source files md5es to local md5es transfered from the SRB
			String[] list = source.list();
			for (int i=0;i<list.length;i++) {
				tempFile = FileFactory.newFile( source, list[i] );
				if (tempFile.isFile()) {
					//find the original md5sum
					proc = Runtime.getRuntime().exec("md5sum "+
						tempFile.getAbsolutePath());
					localIn = proc.getInputStream();
					localIn.read(mds);
					localChk = new String(mds);

					//find the transfered md5sum
					tempFile = FileFactory.newFile(
						returnDestination, tempFile.getName() );
					proc = Runtime.getRuntime().exec("md5sum "+
						tempFile.getAbsolutePath());
					localIn = proc.getInputStream();
					localIn.read(mds);
					srbChk = new String(mds);

          localIn.close();
//System.out.println("srb md5 "+srbChk);
//System.out.println("local md5 "+localChk);
					if (!srbChk.equals(localChk)) {
						System.out.println("error "+tempFile);
						error = true;
					}
				}
			}
		}
		return error;
	}

	public static void main(String args[])
	{
		try{
			String filePath = "file:/scratch/slocal/iktome/sourcedata";
			String relativeFilePath = "mySRBCopyTest";
			LocalFile loc = null;
			SRBFile srb = null;

			if ((args != null) && (args.length > 0)) {
				filePath = args[0];
			}

			try {
				loc = new LocalFile( new URI( filePath ) );
				//test to see if loc is a valid filePath
				if (loc.createNewFile()) {
					loc.delete();
				}
			} catch (Throwable e) {
				loc = new LocalFile( relativeFilePath );
			}

			if (args.length > 1) {
				srb = new SRBFile( new URI( args[1] ) );
			}
			else {
				SRBFileSystem srbFileSystem = new SRBFileSystem( );
				int num=0;
				do {
					srb = new SRBFile( srbFileSystem, "copyTest"+num );
					num++;
				} while (srb.exists());
			}

			CopyTest copyTest = new CopyTest( loc, srb );
			copyTest.copy();
			copyTest.compare();
		}
		catch ( Throwable e ) {
			System.out.println( "\nJava Error Message: "+ e.toString() );
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}
}
