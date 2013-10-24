/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */

/*
 * QueryDisplayOptionsDialog.java
 * 
 *
 * Created on February 20, 2007, 10:14 AM
 */

package edu.harvard.i2b2.previousquery.ui;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.plugins.previousquery.views.PreviousQueryView;

public class QueryDisplayOptionsDialog extends javax.swing.JFrame {

	private PreviousQueryView previousQueryView_;
	private boolean isManager = false;
	private boolean hasProtectedAccess = false;

	/**
	 * Creates new form QueryDisplayOptionsDialog
	 */
	public QueryDisplayOptionsDialog(PreviousQueryView previousQueryView) {

		previousQueryView_ = previousQueryView;

		initComponents();

		setSize(320, 430);
		setLocation(400, 100);
		
		// set up default properties if not previously set
		if(System.getProperty("PQSortByTimeCheckBox")== null)
			System.setProperty("PQSortByTimeCheckBox", "true");
		jSortByTimeCheckBox.setSelected(Boolean.getBoolean("PQSortByTimeCheckBox"));

		if( (System.getProperty("QueryToolMaxQueryNumber") == null ))
			System.setProperty("QueryToolMaxQueryNumber", "20");
		jNumberOfQueryTextField.setText(System.getProperty("QueryToolMaxQueryNumber"));
		
		if( (System.getProperty("PQMaxPatientsNumber") == null ))
			System.setProperty("PQMaxPatientsNumber", "200");
		jNumberOfPatientsTextField.setText(System.getProperty("PQMaxPatientsNumber"));
		
		if(System.getProperty("PQSortByNameCheckBox")== null)
			System.setProperty("PQSortByNameCheckBox", "false");
		jSortByNameCheckBox.setSelected(Boolean.getBoolean("PQSortByNameCheckBox"));

		Boolean ascending = previousQueryView_.runTreePanel().ascending();
		System.setProperty("PQDescending",String.valueOf(!ascending));
		jDescendingRadioButton.setSelected(!ascending);
		System.setProperty("PQAscending",String.valueOf(ascending));
		jAscendingRadioButton.setSelected(ascending);

		ArrayList<String> roles = (ArrayList<String>) UserInfoBean
				.getInstance().getProjectRoles();

		for (String param : roles) {
			if (param.equalsIgnoreCase("manager")) {
				isManager = true;
				if(System.getProperty("PQDisplayGroup") == 	null)
					System.setProperty("PQDisplayGroup", "true");
				jDisplayGroupCheckBox.setSelected(Boolean.getBoolean("PQDisplayGroup"));
				break;
			}
		}

		for (String param : roles) {
			if (param.equalsIgnoreCase("protected_access")) {
				hasProtectedAccess = true;
				break;
			}
		}

		if (!isManager) {
			jDisplayGroupCheckBox.setEnabled(false);
		}

		if (hasProtectedAccess) {
			//Disable the show name button/feature for now as we have no names in our db...
				//	jShowNameRadioButton.setSelected(previousQueryView_.runTreePanel()
				//			.showName());
				// Be sure to activate jShowDemographicsRadioButtonActionPerformed method also
			jShowNameRadioButton.setEnabled(false);
			previousQueryView_.runTreePanel().showName(false);
			
			jShowDemographicsRadioButton.setSelected(!(previousQueryView_
					.runTreePanel().showName()));
		} else {
			jShowNameRadioButton.setSelected(false);
			jShowDemographicsRadioButton.setSelected(true);
			jShowNameRadioButton.setEnabled(false);
			jShowDemographicsRadioButton.setEnabled(false);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * 
	 */
	private void initComponents() {
		jPanel1 = new javax.swing.JPanel();
		jSortByTimeCheckBox = new javax.swing.JCheckBox();
		jSortByNameCheckBox = new javax.swing.JCheckBox();
		jOKButton = new javax.swing.JButton();
		jCancelButton = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jNumberOfQueryTextField = new javax.swing.JTextField();
		jNumberOfPatientsTextField = new javax.swing.JTextField();
		jAscendingRadioButton = new javax.swing.JRadioButton();
		jDescendingRadioButton = new javax.swing.JRadioButton();
		jDisplayGroupCheckBox = new javax.swing.JCheckBox();
		jPanel2 = new javax.swing.JPanel();
		jShowNameRadioButton = new javax.swing.JRadioButton();
		jShowDemographicsRadioButton = new javax.swing.JRadioButton();

		getContentPane().setLayout(null);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Previous Queries Options Dialog");
		java.awt.Image img = this.getToolkit().getImage(
				QueryDisplayOptionsDialog.class.getResource("core-cell.gif"));
		this.setIconImage(img);
		jPanel1.setLayout(null);

		jPanel1.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Order of display"));
		jSortByTimeCheckBox.setText("sort by time when the query was created");
		jSortByTimeCheckBox.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jSortByTimeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jSortByTimeCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jSortByTimeCheckBoxActionPerformed(evt);
					}
				});

		jPanel1.add(jSortByTimeCheckBox);
		jSortByTimeCheckBox.setBounds(20, 30, 230, 15);

		jSortByNameCheckBox.setText("sort by query name");
		jSortByNameCheckBox.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jSortByNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jSortByNameCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jSortByNameCheckBoxActionPerformed(evt);
					}
				});

		jPanel1.add(jSortByNameCheckBox);
		jSortByNameCheckBox.setBounds(20, 60, 220, 15);

		jAscendingRadioButton.setText("Ascending");
		jAscendingRadioButton.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jAscendingRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jAscendingRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jAscendingRadioButtonActionPerformed(evt);
					}
				});

		jPanel1.add(jAscendingRadioButton);
		jAscendingRadioButton.setBounds(20, 90, 80, 16);

		jDescendingRadioButton.setText("Descending");
		jDescendingRadioButton.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jDescendingRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jDescendingRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jDescendingRadioButtonActionPerformed(evt);
					}
				});

		jPanel1.add(jDescendingRadioButton);
		jDescendingRadioButton.setBounds(150, 90, 90, 16);

		getContentPane().add(jPanel1);
		jPanel1.setBounds(10, 60, 285, 130);

		jOKButton.setText("OK");
		jOKButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jOKButtonActionPerformed(evt);
			}
		});

		getContentPane().add(jOKButton);
		jOKButton.setBounds(50, 360, 60, 23);

		jCancelButton.setText("Close");
		jCancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCancelButtonActionPerformed(evt);
			}
		});

		getContentPane().add(jCancelButton);
		jCancelButton.setBounds(190, 360, 80, 23);

		jLabel1.setText("Maximum number of queries to be displayed: ");	
		getContentPane().add(jLabel1);
		jLabel1.setBounds(18, 10, 228, 20);
		
		getContentPane().add(jNumberOfQueryTextField);
		jNumberOfQueryTextField.setBounds(248, 10, 45, 20);
		
		jLabel2.setText("Maximum number of patients to be displayed: ");
		getContentPane().add(jLabel2);
		jLabel2.setBounds(18, 30, 228, 20);
		
		getContentPane().add(jNumberOfPatientsTextField);
		jNumberOfPatientsTextField.setBounds(248, 30, 45, 20);
				
		jDisplayGroupCheckBox.setText("Get all queries in your group");
		jDisplayGroupCheckBox.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jDisplayGroupCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jDisplayGroupCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jDisplayGroupCheckBoxActionPerformed(evt);
					}
				});

		getContentPane().add(jDisplayGroupCheckBox);
		jDisplayGroupCheckBox.setBounds(20, 315, 270, 16);

		jPanel2.setLayout(null);

		jPanel2.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Patient labels"));
		jShowNameRadioButton
				.setText("Show Names (Protected health information)");
		jShowNameRadioButton.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jShowNameRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jShowNameRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jShowNameRadioButtonActionPerformed(evt);
					}
				});

		jPanel2.add(jShowNameRadioButton);
		jShowNameRadioButton.setBounds(20, 30, 260, 16);

		jShowDemographicsRadioButton
				.setText("Show Demographics (De-identified data)");
		jShowDemographicsRadioButton.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		jShowDemographicsRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jShowDemographicsRadioButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jShowDemographicsRadioButtonActionPerformed(evt);
					}
				});

		jPanel2.add(jShowDemographicsRadioButton);
		jShowDemographicsRadioButton.setBounds(20, 60, 250, 16);

		getContentPane().add(jPanel2);
		jPanel2.setBounds(10, 205, 285, 100);

		pack();
	}

	private void jDisplayGroupCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		Boolean isSelected = jDisplayGroupCheckBox.isSelected();
		System.setProperty("PQDisplayGroup", String.valueOf(isSelected));
		/*
		 * String num = jNumberOfQueryTextField.getText();
		 * System.setProperty("QueryToolMaxQueryNumber", num); String status =
		 * previousQueryView_.runTreePanel().loadPreviousQueries(jDisplayGroupCheckBox.isSelected());
		 * 
		 * if(status.equalsIgnoreCase("")) {
		 * previousQueryView_.runTreePanel().reset(new Integer(num).intValue(),
		 * jSortByNameCheckBox.isSelected()); } else
		 * if(status.equalsIgnoreCase("CellDown")){ final JFrame parent = this;
		 * java.awt.EventQueue.invokeLater(new Runnable() { public void run() {
		 * JOptionPane.showMessageDialog(parent, "Trouble with connection to the
		 * remote server, " + "this is often a network error, please try again",
		 * "Network Error", JOptionPane.INFORMATION_MESSAGE); } }); }
		 */
	}

	private void jShowDemographicsRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		// enable this when show name button is enabled.
		/*
		if (jShowDemographicsRadioButton.isSelected()) {
			jShowNameRadioButton.setSelected(false);
		} else {
			jShowNameRadioButton.setSelected(true);
		}
		*/
	}

	private void jShowNameRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (jShowNameRadioButton.isSelected()) {
			jShowDemographicsRadioButton.setSelected(false);
		} else {
			jShowDemographicsRadioButton.setSelected(true);
		}
	}

	private void jDescendingRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		
		Boolean isSelected = jDescendingRadioButton.isSelected();
		System.setProperty("PQDescending",String.valueOf(isSelected));
		jAscendingRadioButton.setSelected(!isSelected);
		previousQueryView_.runTreePanel().ascending(!isSelected);

		// String num = jNumberOfQueryTextField.getText();
		// previousQueryView_.runTreePanel().reset(new Integer(num).intValue(),
		// jSortByNameCheckBox.isSelected());
	}

	private void jAscendingRadioButtonActionPerformed(
			java.awt.event.ActionEvent evt) {
		
		Boolean isSelected = jAscendingRadioButton.isSelected();
		System.setProperty("PQAscending",String.valueOf(isSelected));
		jDescendingRadioButton.setSelected(!isSelected);
		previousQueryView_.runTreePanel().ascending(isSelected);
		

		// String num = jNumberOfQueryTextField.getText();
		// previousQueryView_.runTreePanel().reset(new Integer(num).intValue(),
		// jSortByNameCheckBox.isSelected());
	}

	private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		setVisible(false);
	}

	private void jOKButtonActionPerformed(java.awt.event.ActionEvent evt) {
		previousQueryView_.runTreePanel().showName(
				jShowNameRadioButton.isSelected());
		System.out.println("Show Name: "
				+ (previousQueryView_.runTreePanel().showName() ? "true"
						: "false"));

		String num = jNumberOfQueryTextField.getText();
		System.setProperty("QueryToolMaxQueryNumber", num);
		
		String numPat = jNumberOfPatientsTextField.getText();
		System.setProperty("PQMaxPatientsNumber", numPat);

		String status = previousQueryView_.runTreePanel().loadPreviousQueries(
				jDisplayGroupCheckBox.isSelected());
		if (status.equalsIgnoreCase("")) {
			previousQueryView_.runTreePanel().reset(
					new Integer(num).intValue(),
					jSortByNameCheckBox.isSelected());

			setVisible(false);
		} else if (status.equalsIgnoreCase("CellDown")) {
			final JFrame parent = this;
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									parent,
									"Trouble with connection to the remote server, "
											+ "this is often a network error, please try again",
									"Network Error",
									JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
	}

	private void jSortByNameCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {		
		Boolean isSelected = jSortByNameCheckBox.isSelected();
		System.setProperty("PQSortByNameCheckBox",String.valueOf(isSelected));
		System.setProperty("PQSortByTimeCheckBox",String.valueOf(!isSelected));
		jSortByTimeCheckBox.setSelected(!isSelected);
		
		// previousQueryView_.runTreePanel().reset(new Integer(num).intValue(),
		// true);
	}

	private void jSortByTimeCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		Boolean isSelected = jSortByTimeCheckBox.isSelected();
		System.setProperty("PQSortByTimeCheckBox",String.valueOf(isSelected));
		System.setProperty("PQSortByNameCheckBox",String.valueOf(!isSelected));	
		jSortByNameCheckBox.setSelected(!isSelected);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new QueryDisplayOptionsDialog(null).setVisible(true);
			}
		});
	}

	// Variables declaration
	private javax.swing.JButton jCancelButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JTextField jNumberOfQueryTextField;
	private javax.swing.JTextField jNumberOfPatientsTextField;
	private javax.swing.JButton jOKButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JCheckBox jSortByNameCheckBox;
	private javax.swing.JCheckBox jSortByTimeCheckBox;
	private javax.swing.JCheckBox jDisplayGroupCheckBox;
	private javax.swing.JRadioButton jAscendingRadioButton;
	private javax.swing.JRadioButton jDescendingRadioButton;
	private javax.swing.JRadioButton jShowDemographicsRadioButton;
	private javax.swing.JRadioButton jShowNameRadioButton;
	// End of variables declaration
}
