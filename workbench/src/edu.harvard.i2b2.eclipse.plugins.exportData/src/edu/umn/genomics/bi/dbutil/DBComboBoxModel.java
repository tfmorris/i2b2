/*
 * @(#) $RCSfile: DBComboBoxModel.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 18:00:21 $ $Name: RELEASE_1_3_1_0001b $
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

import javax.swing.*;
import javax.swing.event.*;

/**
 * Provides a ComboBoxModel that uses a shared DBUserList.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 18:00:21 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see edu.umn.genomics.bi.dbutil.DBUserList
 * @see javax.swing.DefaultComboBoxModel
 */
public class DBComboBoxModel extends DefaultComboBoxModel {
    ListDataListener ldl = new ListDataListener() {
	public void intervalAdded(ListDataEvent e) {
	    DBUserList list = (DBUserList) e.getSource();
	    for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
		insertElementAt(list.getElementAt(i), i);
	    }
	}

	public void intervalRemoved(ListDataEvent e) {
	    DBUserList list = (DBUserList) e.getSource();
	    for (int i = e.getIndex1(); i >= e.getIndex0(); i--) {
		removeElementAt(i);
	    }
	}

	public void contentsChanged(ListDataEvent e) {
	    DBUserList list = (DBUserList) e.getSource();
	    removeAllElements();
	    for (int i = 0; i < list.getSize(); i++) {
		addElement(list.getElementAt(i));
	    }
	}
    };

    public DBComboBoxModel() {
	super();
	DBUserList.getSharedInstance().addListDataListener(ldl);
	ldl.contentsChanged(new ListDataEvent(DBUserList.getSharedInstance(),
		ListDataEvent.CONTENTS_CHANGED, 0, DBUserList
			.getSharedInstance().getSize() - 1));
    }

    @Override
    protected void finalize() {
	DBUserList.getSharedInstance().removeListDataListener(ldl);
    }

}
