/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *     Wensong Pan
 *     
 */

package edu.harvard.i2b2.exportData.ui;

import javax.swing.JTable;
import javax.swing.UIManager;

/*
 * ObservationListJPanel.java
 * 
 * Created on January 7, 2008, 11:31 AM
 */

public class ObservationListJPanel extends javax.swing.JPanel {

	/** Creates new form ObservationListJPanel */
	public ObservationListJPanel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}

		initComponents();
		jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);
		jTable1.getColumnModel().getColumn(0).setMinWidth(50);
		jTable1.getColumnModel().getColumn(0).setMaxWidth(50);
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		jTable1.setValueAt("1", 0, 0);
		jTable1.setValueAt("image1", 0, 1);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	private void initComponents() {
		jScrollPane1 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();

		setLayout(new java.awt.BorderLayout());

		jTable1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		jTable1.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null }, { null, null }, { null, null },
						{ null, null } }, new String[] { "Index", "Name" }) {
			Class[] types = new Class[] { java.lang.String.class,
					java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false };

			@Override
			public Class getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		jTable1.setDragEnabled(true);
		jScrollPane1.setViewportView(jTable1);

		add(jScrollPane1, java.awt.BorderLayout.CENTER);

	}

	// Variables declaration
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTable jTable1;
	// End of variables declaration

}
