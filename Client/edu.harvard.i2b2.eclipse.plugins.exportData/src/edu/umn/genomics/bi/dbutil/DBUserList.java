/*
 * @(#) $RCSfile: DBUserList.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.bi.dbutil;

import java.util.*;
import javax.swing.*;

/**
 * Provides a list of DBConnectParams that can be shared among a number
 * components in order that all componets can display a consistent list of
 * accounts. If java.util.prefs.Preferences are available, this list will
 * reflect the user's stored preferences.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBUserList extends DefaultListModel {
    static DBUserList sharedList = null;

    /**
     * Create a list model of Database user account connection parameters,
     * reading the user's preferences if possible.
     */
    public DBUserList() {
	super();
	if (System.getProperty("java.specification.version").compareTo("1.4") >= 0) {
	    try {
		DBConnectParams[] dbs = DBPreferences.getDBAccounts();
		for (int i = 0; i < dbs.length; i++) {
		    addElement(dbs[i]);
		}
	    } catch (Throwable t) {
		System.err.println(t);
	    }
	}
    }

    /**
     * Sets the component at the specified <code>index</code> of this list to be
     * the specified object. The previous component at that position is
     * discarded.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is
     * invalid. <blockquote> <b>Note:</b> Although this method is not
     * deprecated, the preferred method to use is <code>set(int,Object)</code>,
     * which implements the <code>List</code> interface defined in the 1.2
     * Collections framework. </blockquote>
     * 
     * @param obj
     *            what the component is to be set to
     * @param index
     *            the specified index
     * @see #set(int,Object)
     * @see Vector#setElementAt(Object,int)
     */
    @Override
    public void setElementAt(Object obj, int index) {
	if (obj instanceof DBConnectParams) {
	    try {
		DBPreferences
			.deleteDBAccount((DBConnectParams) getElementAt(index));
		DBPreferences.saveDBAccount((DBConnectParams) obj);
	    } catch (Throwable t) {
	    }
	    super.setElementAt(obj, index);
	}
    }

    /**
     * Inserts the specified object as a component in this list at the specified
     * <code>index</code>.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is
     * invalid. <blockquote> <b>Note:</b> Although this method is not
     * deprecated, the preferred method to use is <code>add(int,Object)</code>,
     * which implements the <code>List</code> interface defined in the 1.2
     * Collections framework. </blockquote>
     * 
     * @param obj
     *            the component to insert
     * @param index
     *            where to insert the new component
     * @exception ArrayIndexOutOfBoundsException
     *                if the index was invalid
     * @see #add(int,Object)
     * @see Vector#insertElementAt(Object,int)
     */
    @Override
    public void insertElementAt(Object obj, int index) {
	if (obj instanceof DBConnectParams) {
	    try {
		DBPreferences.saveDBAccount((DBConnectParams) obj);
	    } catch (Throwable t) {
	    }
	    super.insertElementAt(obj, index);
	}
    }

    /**
     * Adds the specified component to the end of this list.
     * 
     * @param obj
     *            the component to be added
     * @see Vector#addElement(Object)
     */
    @Override
    public void addElement(Object obj) {
	if (obj instanceof DBConnectParams) {
	    try {
		DBPreferences.saveDBAccount((DBConnectParams) obj);
	    } catch (Throwable t) {
	    }
	    super.addElement(obj);
	}
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
     * of range (<code>index &lt; 0 || index &gt;= size()</code>).
     * 
     * @param index
     *            index of element to replace
     * @param element
     *            element to be stored at the specified position
     * @return the element previously at the specified position
     */
    @Override
    public Object set(int index, Object element) {
	if (element instanceof DBConnectParams) {
	    try {
		DBPreferences
			.deleteDBAccount((DBConnectParams) getElementAt(index));
		DBPreferences.saveDBAccount((DBConnectParams) element);
	    } catch (Throwable t) {
	    }
	    return super.set(index, element);
	}
	return null;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
     * of range (<code>index &lt; 0 || index &gt; size()</code>).
     * 
     * @param index
     *            index at which the specified element is to be inserted
     * @param element
     *            element to be inserted
     */
    @Override
    public void add(int index, Object element) {
	if (element instanceof DBConnectParams) {
	    try {
		DBPreferences.saveDBAccount((DBConnectParams) element);
	    } catch (Throwable t) {
	    }
	    super.add(index, element);
	}
    }

    /**
     * Deletes the component at the specified index.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is
     * invalid. <blockquote> <b>Note:</b> Although this method is not
     * deprecated, the preferred method to use is <code>remove(int)</code>,
     * which implements the <code>List</code> interface defined in the 1.2
     * Collections framework. </blockquote>
     * 
     * @param index
     *            the index of the object to remove
     * @see #remove(int)
     * @see Vector#removeElementAt(int)
     */
    @Override
    public void removeElementAt(int index) {
	try {
	    DBPreferences
		    .deleteDBAccount((DBConnectParams) getElementAt(index));
	} catch (Throwable t) {
	}
	super.removeElementAt(index);
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument from this
     * list.
     * 
     * @param obj
     *            the component to be removed
     * @return <code>true</code> if the argument was a component of this list;
     *         <code>false</code> otherwise
     * @see Vector#removeElement(Object)
     */
    @Override
    public boolean removeElement(Object obj) {
	if (obj instanceof DBConnectParams) {
	    try {
		DBPreferences.deleteDBAccount((DBConnectParams) obj);
	    } catch (Throwable t) {
	    }
	    return super.removeElement(obj);
	}
	return false;
    }

    /**
     * Removes all components from this list and sets its size to zero.
     * <blockquote> <b>Note:</b> Although this method is not deprecated, the
     * preferred method to use is <code>clear</code>, which implements the
     * <code>List</code> interface defined in the 1.2 Collections framework.
     * </blockquote>
     * 
     * @see #clear()
     * @see Vector#removeAllElements()
     */
    @Override
    public void removeAllElements() {
	for (int i = 0; i < getSize(); i++) {
	    try {
		DBPreferences
			.deleteDBAccount((DBConnectParams) getElementAt(i));
	    } catch (Throwable t) {
	    }
	}
	super.removeAllElements();
    }

    /**
     * Removes the element at the specified position in this list. Returns the
     * element that was removed from the list.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
     * of range (<code>index &lt; 0 || index &gt;= size()</code>).
     * 
     * @param index
     *            the index of the element to removed
     */
    @Override
    public Object remove(int index) {
	try {
	    DBPreferences
		    .deleteDBAccount((DBConnectParams) getElementAt(index));
	} catch (Throwable t) {
	}
	return super.remove(index);
    }

    /**
     * Deletes the components at the specified range of indexes. The removal is
     * inclusive, so specifying a range of (1,5) removes the component at index
     * 1 and the component at index 5, as well as all components in between.
     * <p>
     * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index was
     * invalid. Throws an <code>IllegalArgumentException</code> if
     * <code>fromIndex &gt; toIndex</code>.
     * 
     * @param fromIndex
     *            the index of the lower end of the range
     * @param toIndex
     *            the index of the upper end of the range
     * @see #remove(int)
     */
    @Override
    public void removeRange(int fromIndex, int toIndex) {
	int fi = fromIndex < toIndex ? fromIndex : toIndex;
	int ti = fromIndex < toIndex ? toIndex : fromIndex;
	for (int i = fi; i <= ti; i++) {
	    try {
		DBPreferences
			.deleteDBAccount((DBConnectParams) getElementAt(i));
	    } catch (Throwable t) {
	    }
	}
	super.removeRange(fromIndex, toIndex);
    }

    /**
     * Import Data Base account connection parameters from the the given source.
     * 
     * @param source
     *            a URL or file pathname to a preference file.
     */
    public void importDBUsers(String source) throws Exception {
	if (System.getProperty("java.specification.version").compareTo("1.4") >= 0) {
	    try {
		DBPreferences.importPreferences(source);
		DBConnectParams[] dbs = DBPreferences.getDBAccounts();
		for (int i = 0; i < dbs.length; i++) {
		    if (!contains(dbs[i])) {
			addElement(dbs[i]);
		    }
		}
	    } catch (Throwable t) {
		System.err.println(t);
	    }
	} else {
	}
    }

    /**
     * Export Data Base account connection parameters to the given file path.
     * 
     * @param path
     *            file pathname to write the preferences file.
     * @param accountName
     *            If not null, only export the connection parameters for this
     *            account, otherwise export all account parameters.
     */
    public void exportDBUsers(String path, String accountName) throws Exception {
	if (System.getProperty("java.specification.version").compareTo("1.4") >= 0) {
	    DBPreferences.exportPreferences(path, accountName);
	} else {
	}
    }

    /**
     * Get a Dasebase Accounts list that will be shared. return a shared
     * Dasebase Accounts list model.
     */
    public static DBUserList getSharedInstance() {
	if (sharedList == null) {
	    sharedList = new DBUserList();
	}
	return sharedList;
    }

}
