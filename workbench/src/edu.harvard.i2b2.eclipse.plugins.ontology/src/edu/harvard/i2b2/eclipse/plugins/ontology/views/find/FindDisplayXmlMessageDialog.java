/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *      Wensong Pan
 */
package edu.harvard.i2b2.eclipse.plugins.ontology.views.find;

public class FindDisplayXmlMessageDialog extends javax.swing.JFrame {
	
    /** Creates new form QueryDisplayXmlMessageDialog */
    public FindDisplayXmlMessageDialog(String msg) {
   	
        initComponents();
        setSize(800, 400);
        jMessageTextArea.setText(msg);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        jScrollPane = new javax.swing.JScrollPane();
        jMessageTextArea = new javax.swing.JTextArea();
        jMenuBar = new javax.swing.JMenuBar();
        jFileMenu = new javax.swing.JMenu();
        jCloseMenuItem = new javax.swing.JMenuItem();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.awt.Image img = this.getToolkit().getImage(FindDisplayOptionsDialog.class.getResource("core-cell.gif"));
        this.setIconImage(img);
        
        jMessageTextArea.setColumns(20);
        jMessageTextArea.setEditable(false);
        jMessageTextArea.setRows(5);
        jScrollPane.setViewportView(jMessageTextArea);

        getContentPane().add(jScrollPane);

        jFileMenu.setText("File");
        jCloseMenuItem.setText("Close");
        jCloseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseMenuItemActionPerformed(evt);
            }
        });

        jFileMenu.add(jCloseMenuItem);

        jMenuBar.add(jFileMenu);

        setJMenuBar(jMenuBar);

        pack();
    }

    private void jCloseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
    	setVisible(false);
    	dispose();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FindDisplayXmlMessageDialog(null).setVisible(true);
            }
        });
    }
    
    // Variables declaration
    private javax.swing.JMenuItem jCloseMenuItem;
    private javax.swing.JMenu jFileMenu;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JTextArea jMessageTextArea;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration
    

}
