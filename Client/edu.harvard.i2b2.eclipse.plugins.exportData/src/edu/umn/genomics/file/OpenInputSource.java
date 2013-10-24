/*
 * @(#) $RCSfile: OpenInputSource.java,v $ $Revision: 1.3 $ $Date: 2008/11/04 19:33:00 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * see: http://www.gnu.org/copyleft/gpl.html
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 */

package edu.umn.genomics.file;

import java.io.*;
import java.net.*;

/**
 * OpenInputSource provides methods to open a named source that is either a URL
 * or a File.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/11/04 19:33:00 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class OpenInputSource {
    /**
     * Open the given URL or file pathname for reading.
     * 
     * @param source
     *            the URL or pathname to open.
     * @return an input stream opened on the source.
     */
    public static InputStream getInputStream(String source) throws IOException {
	// URL?
	try {
	    URL url = new URL(source);
	    try {
		InputStream is = url.openStream();
		return is;
	    } catch (Exception se) {
	    }
	} catch (Exception ue) {
	}
	// local file?
	try {
	    InputStream is = new FileInputStream(source);
	    return is;
	} catch (IOException e) {
	    if (source.charAt(0) == '~') {
		// Try a Unix shell tilde expansion
		try {
		    String shell = "/usr/bin/csh";
		    if ((new File(shell)).exists()) {
			String args[] = new String[3];
			args[0] = shell;
			args[1] = "-c";
			args[2] = "echo " + source;
			Process p = Runtime.getRuntime().exec(args);
			BufferedReader br = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
			String path = "";
			String so;
			while ((so = br.readLine()) != null) {
			    path += so;
			}
			InputStream is = new FileInputStream(path);
			return is;
		    }
		} catch (Exception rte) {
		    System.err.println(rte);
		}
	    }
	    throw e;
	}
    }

    /**
     * Open the given URL or file pathname for reading.
     * 
     * @param source
     *            the URL or pathname to open.
     * @return a Reader opened on the source.
     */
    public static BufferedReader getBufferedReader(String source)
	    throws IOException {
	return new BufferedReader(new InputStreamReader(getInputStream(source)));
    }
}
