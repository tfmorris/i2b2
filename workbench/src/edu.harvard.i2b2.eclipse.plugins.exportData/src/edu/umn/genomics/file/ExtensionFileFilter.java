/*
 * @(#) $RCSfile: ExtensionFileFilter.java,v $ $Revision: 1.4 $ $Date: 2008/11/04 19:33:00 $ $Name: RELEASE_1_3_1_0001b $
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

import java.io.File;
import java.util.*;
import javax.swing.filechooser.FileFilter;

/**
 * Filter files based on filename extensions.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/11/04 19:33:00 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.1
 */
public class ExtensionFileFilter extends FileFilter {
    String description = "All files";
    List extensions = null;

    /**
     * Construct a filter for files based on filename extensions.
     * 
     * @param extensions
     *            The list of filename extensions to recognize.
     * @param description
     *            A text description for this filter.
     */
    public ExtensionFileFilter(List extensions, String description) {
	setDescription(description);
	setExtensions(extensions);
    }

    /**
     * Set the description for this filter.
     * 
     * @param description
     *            A text description for this filter.
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * Get the description for this filter.
     * 
     * @return A text description for this filter.
     */
    @Override
    public String getDescription() {
	return description;
    }

    /**
     * Set the list of filename extensions to recognize.
     * 
     * @param extensions
     *            The list of filename extensions to recognize.
     */
    public void setExtensions(List extensions) {
	this.extensions = new Vector();
	for (ListIterator i = extensions.listIterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o != null) {
		this.extensions.add(o.toString().toLowerCase());
	    }
	}
    }

    /**
     * Get the list of filename extensions to recognize.
     * 
     * @return The list of filename extensions to recognize.
     */
    public List getExtensions() {
	return extensions;
    }

    // Inherit javadoc
    @Override
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	if (extensions == null) {
	    return true;
	}
	String extension = getExtension(f);
	if (extension != null) {
	    return extensions.contains(extension);
	}
	return false;
    }

    /**
     * Get the final filename extension for this file.
     * 
     * @param f
     *            The file from which to retrieve the extension.
     * @return The extension or null if none was found.
     */
    public static String getExtension(File f) {
	String s = f.getName();
	return getExtension(s);
    }

    /**
     * Get the final filename extension for this filename.
     * 
     * @param s
     *            The filename from which to retrieve the extension.
     * @return The extension or null if none was found.
     */
    public static String getExtension(String s) {
	String ext = null;
	int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
	    ext = s.substring(i + 1).toLowerCase();
	}
	return ext;
    }

}
