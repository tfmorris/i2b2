/*
 * @(#) $RCSfile: DBUserPanel.java,v $ $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
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

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import java.awt.event.*;
import edu.umn.genomics.file.ExtensionFileFilter;

/**
 * An entry form in which to enter parameters required for a JDBC connection to
 * a data base.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/04 20:14:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DBUserPanel implements DBConnectParams {
    ComboBoxModel dbModel = new DBComboBoxModel();
    JLabel dbUsersLabel;
    JComboBox dbUsers;
    JLabel dbNameLabel;
    JTextField dbNameField;
    JLabel userNameLabel;
    JTextField userNameField;
    JLabel passwordLabel;
    JPasswordField passwordField;
    JLabel serverLabel;
    JTextField serverField;
    JLabel driverLabel;
    JTextField driverField;
    JPanel connectionPanel;
    JLabel status;
    JFileChooser fileChooser;
    JPanel importPanel;
    JTextField importTextField;
    DBConnectParams dbuser = null;
    String exts[] = { "xml" };
    javax.swing.filechooser.FileFilter xmlFileFilter = new ExtensionFileFilter(
	    Arrays.asList(exts), ".xml files");

    /**
     * Construct a form for entering database connection parameters with all
     * blank entry fields.
     */
    public DBUserPanel() {
	init();
    }

    /**
     * Construct a form for entering database connection parameters from an
     * existing set of parameters.
     * 
     * @param params
     *            Database account connection parameters.
     */
    public DBUserPanel(DBConnectParams params) {
	dbuser = new DBUser(params);
	init();
    }

    /**
     * Construct a form for entering database connection parameters from a
     * ComboBoxModel.
     * 
     * @param comboBoxModel
     *            a ComboBoxModel containing a list of DBConnectParams.
     */
    public DBUserPanel(DefaultComboBoxModel comboBoxModel) {
	dbModel = comboBoxModel;
	init();
    }

    private JPanel getImportPanel() {
	if (importPanel == null) {
	    importPanel = new JPanel();
	    importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.X_AXIS));
	    JLabel label = new JLabel("File or URL:");
	    importTextField = new JTextField(40);
	    JButton fcBtn = new JButton("Browse");
	    fcBtn.setToolTipText("Browse files for preferences");
	    fcBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			if (fileChooser == null) {
			    fileChooser = new JFileChooser();
			    fileChooser.addChoosableFileFilter(xmlFileFilter);
			}
			int returnVal = fileChooser
				.showOpenDialog(((JComponent) e.getSource())
					.getTopLevelAncestor());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fileChooser.getSelectedFile();
			    importTextField.setText(file.getAbsolutePath());
			} else {
			}
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(((JComponent) e
				.getSource()).getTopLevelAncestor(), ex,
				"Data base user import failed",
				JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	    importPanel.add(label);
	    importPanel.add(importTextField);
	    importPanel.add(fcBtn);
	}
	return importPanel;
    }

    /**
     * Initialize the entry panel components.
     */
    private void init() {
	dbUsers = new JComboBox(dbModel);
	if (dbuser == null) {
	    if (dbModel.getSize() > 0) {
		try {
		    dbuser = (DBConnectParams) dbModel.getSelectedItem();
		    if (dbuser == null) {
			dbuser = (DBConnectParams) dbModel.getElementAt(0);
		    }
		} catch (Exception ex) {
		}
	    }
	}
	if (dbuser == null) {
	    dbuser = new DBUser("", "", "", "");
	}
	dbUsers.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    setDBUser((DBConnectParams) e.getItem());
		}
	    }
	});
	// Create the labels and text fields.
	dbUsersLabel = new JLabel("Select Account: ", SwingConstants.RIGHT);
	// 
	dbNameLabel = new JLabel("Connection name: ", SwingConstants.RIGHT);
	dbNameField = new JTextField(dbuser.getName());
	// 
	userNameLabel = new JLabel("User name: ", SwingConstants.RIGHT);
	userNameField = new JTextField(dbuser.getUser());
	// 
	passwordLabel = new JLabel("Password: ", SwingConstants.RIGHT);
	passwordField = new JPasswordField(dbuser.getPassword());
	// 
	serverLabel = new JLabel("Database URL: ", SwingConstants.RIGHT);
	serverField = new JTextField(dbuser.getURL());
	// 
	driverLabel = new JLabel("Driver: ", SwingConstants.RIGHT);
	driverField = new JTextField(dbuser.getDriverName());
	// 
	connectionPanel = new JPanel(new BorderLayout());
	// 
	JPanel editPanel = new JPanel();
	editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.X_AXIS));
	JPanel namePanel = new JPanel();
	namePanel.setLayout(new GridLayout(0, 1));
	namePanel.add(dbUsersLabel);
	namePanel.add(dbNameLabel);
	namePanel.add(userNameLabel);
	namePanel.add(passwordLabel);
	namePanel.add(serverLabel);
	namePanel.add(driverLabel);
	JPanel fieldPanel = new JPanel(false);
	fieldPanel.setLayout(new GridLayout(0, 1));
	fieldPanel.add(dbUsers);
	fieldPanel.add(dbNameField);
	fieldPanel.add(userNameField);
	fieldPanel.add(passwordField);
	fieldPanel.add(serverField);
	fieldPanel.add(driverField);
	editPanel.add(namePanel);
	editPanel.add(fieldPanel);
	JPanel btnPanel = new JPanel();
	btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
	// Add
	JButton addBtn = new JButton("Add");
	addBtn.setToolTipText("Add or update this user profile");
	addBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    String name = dbNameField.getText();
		    String usr = userNameField.getText();
		    String pw = new String(passwordField.getPassword());
		    String url = serverField.getText();
		    String driver = driverField.getText();
		    DBUserList.getSharedInstance().addElement(
			    new DBUser(name, usr, pw, url, driver));
		} catch (Exception ex) {
		}
	    }
	});
	btnPanel.add(addBtn);
	// Remove
	JButton rmvBtn = new JButton("Remove");
	rmvBtn.setToolTipText("Remove this user profile");
	rmvBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    DBUserList.getSharedInstance().removeElement(
			    dbUsers.getSelectedItem());
		} catch (Exception ex) {
		}
	    }
	});
	btnPanel.add(rmvBtn);
	// Test
	JButton testBtn = new JButton("Test");
	testBtn.setToolTipText("Test connection to this database");
	testBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		String name = dbNameField.getText();
		String usr = userNameField.getText();
		String pw = new String(passwordField.getPassword());
		String url = serverField.getText();
		String driver = driverField.getText();
		try {
		    DBTestConnection.testConnection(usr, pw, url, driver);
		    JOptionPane
			    .showMessageDialog(((JComponent) e.getSource())
				    .getTopLevelAncestor(),
				    "Able to connect to " + url);
		} catch (Exception ex) {
		    JOptionPane.showMessageDialog(((JComponent) e.getSource())
			    .getTopLevelAncestor(), ex,
			    "Data base connection failed",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	});
	btnPanel.add(testBtn);
	try {
	    Class.forName("java.util.prefs.Preferences");
	    // Import
	    JButton impBtn = new JButton("Import");
	    impBtn.setToolTipText("Import Database User Account Parameters");
	    impBtn.addActionListener(new ActionListener() {
		String[] optionNames = { "Import", "Cancel" };

		public void actionPerformed(ActionEvent e) {
		    try {
			int choice = JOptionPane.showOptionDialog(
				((JComponent) e.getSource())
					.getTopLevelAncestor(),
				getImportPanel(),
				"Import Database User Parameters",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null,
				optionNames, optionNames[0]);
			switch (choice) {
			case 0:
			    DBUserList.getSharedInstance().importDBUsers(
				    importTextField.getText());
			    break;
			}
		    } catch (Exception ex) {
		    }
		}
	    });
	    btnPanel.add(impBtn);
	    // Export this
	    JButton expBtn = new JButton("Export");
	    expBtn
		    .setToolTipText("Export this Database User Account Parameters");
	    expBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			if (fileChooser == null) {
			    fileChooser = new JFileChooser();
			    fileChooser.addChoosableFileFilter(xmlFileFilter);
			}
			int returnVal = fileChooser
				.showOpenDialog(((JComponent) e.getSource())
					.getTopLevelAncestor());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fileChooser.getSelectedFile();
			    String accnt = dbUsers.getSelectedItem().toString();
			    DBUserList.getSharedInstance().exportDBUsers(
				    file.getAbsolutePath(), accnt);
			} else {
			}
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(((JComponent) e
				.getSource()).getTopLevelAncestor(), ex,
				"Data base user export failed",
				JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	    btnPanel.add(expBtn);
	    // Export this
	    JButton expAllBtn = new JButton("Export All");
	    expAllBtn
		    .setToolTipText("Export ALL Database User Account Parameters");
	    expAllBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			if (fileChooser == null) {
			    fileChooser = new JFileChooser();
			    fileChooser.addChoosableFileFilter(xmlFileFilter);
			}
			int returnVal = fileChooser
				.showOpenDialog(((JComponent) e.getSource())
					.getTopLevelAncestor());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fileChooser.getSelectedFile();
			    DBUserList.getSharedInstance().exportDBUsers(
				    file.getAbsolutePath(), null);
			} else {
			}
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(((JComponent) e
				.getSource()).getTopLevelAncestor(), ex,
				"Data base user export failed",
				JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	    btnPanel.add(expAllBtn);
	} catch (ClassNotFoundException cnfex) {
	}
	connectionPanel.add(editPanel);
	connectionPanel.add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Set this to return the given connection parameters.
     * 
     * @param newdbuser
     *            the database connection parameters.
     */
    private void setDBUser(DBConnectParams newdbuser) {
	dbuser = newdbuser;
	dbNameField.setText(dbuser.getName());
	userNameField.setText(dbuser.getUser());
	passwordField.setText(dbuser.getPassword());
	serverField.setText(dbuser.getURL());
	driverField.setText(dbuser.getDriverName());
    }

    /**
     * Display the JDBC parameters entry form.
     * 
     * @param parentComponent
     *            the parent for this entry form panel.
     */
    public void show(Component parentComponent) {
	String[] ConnectOptionNames = { "OK", "Cancel" };
	String ConnectTitle = "Data Base Account Information";
	connectionPanel.validate();
	int choice = JOptionPane.showOptionDialog(parentComponent,
		connectionPanel, ConnectTitle, JOptionPane.OK_CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE, null, ConnectOptionNames,
		ConnectOptionNames[0]);

	String name = dbNameField.getText();
	String usr = userNameField.getText();
	String pw = new String(passwordField.getPassword());
	String url = serverField.getText();
	String driver = driverField.getText();
	switch (choice) {
	case 0:
	    try {
		DBTestConnection.testConnection(usr, pw, url, driver);
		dbuser = new DBUser(name, usr, pw, url, driver);
		DBUserList.getSharedInstance().addElement(dbuser);
	    } catch (Exception e) {
		JOptionPane.showMessageDialog(parentComponent, e,
			"Data base connection failed",
			JOptionPane.ERROR_MESSAGE);
		System.err.println("DB connection failed " + e);
	    }
	    break;
	default:
	    break;
	}

    }

    /**
     * Return the name for this connection.
     * 
     * @return the for this connection.
     */
    public String getName() {
	return dbuser.getName();
    }

    /**
     * Return the user account name.
     * 
     * @return the user account name.
     */
    public String getUser() {
	return dbuser.getUser();
    }

    /**
     * Return the user password.
     * 
     * @return the user password.
     */
    public String getPassword() {
	return dbuser.getPassword();
    }

    /**
     * Return the data base URL.
     * 
     * @return the data base URL.
     */
    public String getURL() {
	return dbuser.getURL();
    }

    /**
     * Return the class name for the JDBC Driver Class.
     * 
     * @return the class name for the JDBC Driver Class.
     */
    public String getDriverName() {
	return dbuser.getDriverName();
    }

    /**
     * Returns whether this DBConnectParams represnets the same database user
     * account as the given dbConnectParams. The name given to the
     * DBConnectParams instances are ignored for this comparison.
     * 
     * @param dbConnectParams
     * @return whether these represent the same database user.
     */
    public boolean userEquals(DBConnectParams dbConnectParams) {
	return DBUser.userEquals(this, dbConnectParams);
    }

    /**
     * Open a dialog with database user account connection parameters.
     * 
     * @param args
     *            No arguments are used.
     */
    public static void main(String[] args) {
	DBUserPanel dbup = new DBUserPanel();
	dbup.show(new JFrame(""));
	System.exit(0);
    }
}
