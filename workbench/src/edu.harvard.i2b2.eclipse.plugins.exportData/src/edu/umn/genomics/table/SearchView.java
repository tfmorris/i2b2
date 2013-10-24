/*
 * @(#) $RCSfile: SearchView.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table;

import java.io.Serializable;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * SearchView provides common TableModel View elements.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see ColumnMap
 * @see TableContext
 * @see javax.swing.table.TableModel
 * @see javax.swing.ListSelectionModel
 */
public class SearchView extends AbstractTableModelView implements Serializable,
	TableModelView, CleanUp, TableModelListener, ListSelectionListener {
    SetIcon sReplace = new SetIcon(SetOperator.REPLACE);
    SetIcon sUnion = new SetIcon(SetOperator.UNION);
    SetIcon sIntersect = new SetIcon(SetOperator.INTERSECTION);
    SetIcon sDiff = new SetIcon(SetOperator.DIFFERENCE);
    SetIcon sXOR = new SetIcon(SetOperator.XOR);

    class IconRenderer extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, int row,
		int column) {

	    setText(null);
	    setIcon((Icon) value);
	    return this;
	}
    }

    class ColSelTableModel extends AbstractTableModel {
	Vector regex = new Vector();

	public int getRowCount() {
	    return tm != null ? tm.getColumnCount() : 0;
	}

	public int getColumnCount() {
	    return 7;
	}

	@Override
	public String getColumnName(int col) {
	    if (tm != null) {
		switch (col) {
		case 0:
		    return "Column";
		case 1:
		    return "Regular Expression";
		case 2:
		    return "Replace";
		case 3:
		    return "Union";
		case 4:
		    return "Intersect";
		case 5:
		    return "Diff";
		case 6:
		    return "XOR";
		}
	    }
	    return null;
	}

	@Override
	public Class getColumnClass(int col) {
	    if (tm != null) {
		switch (col) {
		case 0:
		    return java.lang.String.class;
		case 1:
		    return java.lang.String.class;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		    return SetIcon.class;
		}
	    }
	    return java.lang.Object.class;
	}

	public Object getValueAt(int row, int col) {
	    if (row >= 0 && col >= 0 && tm != null) {
		switch (col) {
		case 0:
		    return tm.getColumnName(row);
		case 1:
		    if (regex.size() < tm.getColumnCount()) {
			regex.setSize(tm.getColumnCount());
		    }
		    Object obj = row < regex.size() ? regex.get(row) : null;
		    return obj != null ? obj : "";
		case 2:
		    return sReplace;
		case 3:
		    return sUnion;
		case 4:
		    return sIntersect;
		case 5:
		    return sDiff;
		case 6:
		    return sXOR;
		}
	    }
	    return null;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
	    if (col >= 1 && col < 2) {
		return true;
	    }
	    return false;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
	    if (row >= 0 && col == 1) {
		if (regex.size() < tm.getColumnCount()) {
		    regex.setSize(tm.getColumnCount());
		}
		if (row < regex.size()) {
		    regex.set(row, value);
		}
	    }
	}
    }

    ColSelTableModel cstm = new ColSelTableModel();
    JTable jt = new JTable(cstm);
    IconRenderer iconRenderer = new IconRenderer();

    /**
     * Constructs a view display. Nothing will be displayed until a data model
     * is set.
     * 
     * @see #setTableModel(TableModel tableModel)
     */
    public SearchView() {
	super();
	jt.getTableHeader().setReorderingAllowed(false);
	jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	jt.setCellSelectionEnabled(true);
	jt.setDefaultRenderer(sReplace.getClass(), iconRenderer);
	jt.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mousePressed(MouseEvent e) {
		int row = jt.rowAtPoint(e.getPoint());
		int col = jt.columnAtPoint(e.getPoint());
		Object val = jt.getValueAt(row, col);
		if (val != null && val instanceof SetIcon) {
		    Object obj = jt.getValueAt(row, 1);
		    if (obj != null && obj instanceof String
			    && obj.toString().length() > 0) {
			doSelect(row, obj.toString(), ((SetIcon) val)
				.getSetOperator());
		    } else {
			jt.getColumnModel().getSelectionModel()
				.setSelectionInterval(1, 1);
			JOptionPane.showMessageDialog(getTopLevelAncestor(),
				"No Regular Expression Matching pattern given",
				"Search Failed", JOptionPane.ERROR_MESSAGE);
		    }
		}
	    }
	});
	JScrollPane jsp = new JScrollPane(jt);
	setLayout(new BorderLayout());
	add(jsp);
    }

    public void doSelect(int tableColumn, String pattern, int setOp) {
	try {
	    switch (setOp) {
	    case SetOperator.REPLACE:
		Search.select(getTableModel(), tableColumn,
			getSelectionModel(), pattern);
		return;
	    case SetOperator.UNION:
	    case SetOperator.DIFFERENCE:
	    case SetOperator.INTERSECTION:
	    case SetOperator.XOR:
		ListSelectionModel dlsm = new DefaultListSelectionModel();
		Search.select(getTableModel(), tableColumn, dlsm, pattern);
		Search.select(getSelectionModel(), dlsm, setOp);
		return;
	    }
	} catch (Exception ex) {
	    String s = pattern == null ? "" : (" for " + pattern);
	    JOptionPane.showMessageDialog(getTopLevelAncestor(), ex,
		    "Search Failed" + s, JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and a default selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     */
    public SearchView(TableModel tableModel) {
	this();
	setTableModel(tableModel);
    }

    /**
     * Constructs a view display which is initialized with tableModel as the
     * data model, and the given selection model.
     * 
     * @param tableModel
     *            the data model for the parallel coordinate display
     * @param lsm
     *            the ListSelectionModel for the parallel coordinate display
     */
    public SearchView(TableModel tableModel, ListSelectionModel lsm) {
	this();
	setSelectionModel(lsm);
	setTableModel(tableModel);
    }

    /**
     * Sets tableModel as the data model for the view.
     * 
     * @param tableModel
     *            the data model for the view
     */
    @Override
    public void setTableModel(TableModel tableModel) {
	super.setTableModel(tableModel);
	cstm.fireTableStructureChanged();
    }

    /**
     * Called whenever the TableModel structure or data changes, this method
     * does nothing and should be overridden by extending classes that wish to
     * receive these events. The TableModelEvent should be constructed in the
     * coordinate system of the model.
     * 
     * @param e
     *            the change to the data model
     */
    @Override
    public void tableChanged(TableModelEvent e) {
	if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
	    cstm.fireTableStructureChanged();
	    return;
	}
    }

}
