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

package edu.harvard.i2b2.previousquery;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.harvard.i2b2.eclipse.plugins.previousquery.views.PreviousQueryView;

/**
 *
 * @author  wp066
 */
public class QueryDisplayOptionsDialog extends javax.swing.JFrame {
    
	private PreviousQueryView previousQueryView_;
	
    /**
     * Creates new form QueryDisplayOptionsDialog
     */
    public QueryDisplayOptionsDialog(PreviousQueryView previousQueryView) {
    	
    	previousQueryView_ = previousQueryView;
    	
        initComponents();
        
        setSize(320, 280);
        jSortByTimeCheckBox.setSelected(true);
        jDescendingRadioButton.setSelected(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * 
     */
    private void initComponents() {
    	jPanel1 = new javax.swing.JPanel();
        jSortByTimeCheckBox = new javax.swing.JCheckBox();
        jSortByNameCheckBox = new javax.swing.JCheckBox();
        jOKButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jNumberOfQueryTextField = new javax.swing.JTextField();
        jAscendingRadioButton = new javax.swing.JRadioButton();
        jDescendingRadioButton = new javax.swing.JRadioButton();
        jDisplayGroupCheckBox = new javax.swing.JCheckBox();

        getContentPane().setLayout(null);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Previous Queries Options Dialog");
        java.awt.Image img = this.getToolkit().getImage(QueryDisplayOptionsDialog.class.getResource("core-cell.gif"));
        this.setIconImage(img);
        jPanel1.setLayout(null);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Order of display"));
        jSortByTimeCheckBox.setText("sort by time when the query was created");
        jSortByTimeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSortByTimeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jSortByTimeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSortByTimeCheckBoxActionPerformed(evt);
            }
        });
        
        jPanel1.add(jSortByTimeCheckBox);
        jSortByTimeCheckBox.setBounds(20, 30, 230, 15);

        jSortByNameCheckBox.setText("sort by query name");
        jSortByNameCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jSortByNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jSortByNameCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSortByNameCheckBoxActionPerformed(evt);
            }
        });
        
        jPanel1.add(jSortByNameCheckBox);
        jSortByNameCheckBox.setBounds(20, 60, 220, 15);
        
        jAscendingRadioButton.setText("Ascending");
        jAscendingRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jAscendingRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jAscendingRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAscendingRadioButtonActionPerformed(evt);
            }
        });

        jPanel1.add(jAscendingRadioButton);
        jAscendingRadioButton.setBounds(20, 90, 67, 15);

        jDescendingRadioButton.setText("Descending");
        jDescendingRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jDescendingRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jDescendingRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDescendingRadioButtonActionPerformed(evt);
            }
        });

        jPanel1.add(jDescendingRadioButton);
        jDescendingRadioButton.setBounds(150, 90, 73, 15);

        getContentPane().add(jPanel1);
        jPanel1.setBounds(10, 40, 285, 130);

        jOKButton.setText("OK");
        jOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOKButtonActionPerformed(evt);
            }
        });

        getContentPane().add(jOKButton);
        jOKButton.setBounds(50, 215, 50, 23);

        jCancelButton.setText("Close");
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelButtonActionPerformed(evt);
            }
        });

        getContentPane().add(jCancelButton);
        jCancelButton.setBounds(190, 215, 70, 23);

        jLabel1.setText("Maximum number of queries to be displayed: ");
        jNumberOfQueryTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jNumberOfQueryTextFieldActionPerformed(evt);
            }
        });
        
        getContentPane().add(jLabel1);
        jLabel1.setBounds(18, 10, 220, 20);

        jNumberOfQueryTextField.setText("20");
        getContentPane().add(jNumberOfQueryTextField);
        jNumberOfQueryTextField.setBounds(240, 10, 50, 20);
        
        jDisplayGroupCheckBox.setText("Get all queries in your group");
        jDisplayGroupCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jDisplayGroupCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jDisplayGroupCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisplayGroupCheckBoxActionPerformed(evt);
            }
        });

        getContentPane().add(jDisplayGroupCheckBox);
        jDisplayGroupCheckBox.setBounds(20, 180, 270, 15);

        pack();
    }
    
    private void jDisplayGroupCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
    	String num = jNumberOfQueryTextField.getText();
    	System.setProperty("QueryToolMaxQueryNumber", num);
    	String status = previousQueryView_.runTreePanel().loadPreviousQueries(jDisplayGroupCheckBox.isSelected());
    	
    	if(status.equalsIgnoreCase("")) {
    		previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), 
        			jSortByNameCheckBox.isSelected());
		}
		else if(status.equalsIgnoreCase("CellDown")){
			final JFrame parent = this;
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {					
					JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
	         			"this is often a network error, please try again", 
	         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
				}
			});		
		}	
    }
    
    private void jDescendingRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	jAscendingRadioButton.setSelected(false);
    	previousQueryView_.runTreePanel().ascending(false);
    	
    	String num = jNumberOfQueryTextField.getText();
    	previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), 
    			jSortByNameCheckBox.isSelected());
    }

    private void jAscendingRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	jDescendingRadioButton.setSelected(false);
    	previousQueryView_.runTreePanel().ascending(true);
    	
    	String num = jNumberOfQueryTextField.getText();
    	previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), 
    			jSortByNameCheckBox.isSelected());
    }

    private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	setVisible(false);
    }

    private void jOKButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	String num = jNumberOfQueryTextField.getText();
    	System.setProperty("QueryToolMaxQueryNumber", num);
    	
    	String status = previousQueryView_.runTreePanel().loadPreviousQueries(jDisplayGroupCheckBox.isSelected());
    	if(status.equalsIgnoreCase("")) {
    		previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), 
        			jSortByNameCheckBox.isSelected());
    		
    		setVisible(false);
		}
		else if(status.equalsIgnoreCase("CellDown")){
			final JFrame parent = this;
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {					
					JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
	         			"this is often a network error, please try again", 
	         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
				}
			});		
		}	
    }
    
    private void jNumberOfQueryTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
    	String num = jNumberOfQueryTextField.getText();
    	System.setProperty("QueryToolMaxQueryNumber", num);
    	String status = previousQueryView_.runTreePanel().loadPreviousQueries(jDisplayGroupCheckBox.isSelected());
    	if(status.equalsIgnoreCase("")) {
    		previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), 
        			jSortByNameCheckBox.isSelected());
		}
		else if(status.equalsIgnoreCase("CellDown")){
			final JFrame parent = this;
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {					
					JOptionPane.showMessageDialog(parent, "Trouble with connection to the remote server, " +
	         			"this is often a network error, please try again", 
	         			"Network Error", JOptionPane.INFORMATION_MESSAGE);
				}
			});		
		}	    	
    }
    
    private void jSortByNameCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
    	jSortByTimeCheckBox.setSelected(false);
    	String num = jNumberOfQueryTextField.getText();
    	previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), true);
    }

    private void jSortByTimeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
    	jSortByNameCheckBox.setSelected(false);
    	String num = jNumberOfQueryTextField.getText();
    	previousQueryView_.runTreePanel().reset(new Integer(num).intValue(), false);
    }
    
    /**
     * @param args the command line arguments
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
    private javax.swing.JTextField jNumberOfQueryTextField;
    private javax.swing.JButton jOKButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox jSortByNameCheckBox;
    private javax.swing.JCheckBox jSortByTimeCheckBox;
    private javax.swing.JCheckBox jDisplayGroupCheckBox;
    private javax.swing.JRadioButton jAscendingRadioButton;
    private javax.swing.JRadioButton jDescendingRadioButton;
    // End of variables declaration
    
}
