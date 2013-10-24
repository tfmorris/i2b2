/*
 * @(#) $RCSfile: DatabaseAccountEditor.java,v $ $Revision: 1.3 $ $Date: 2008/09/03 18:02:45 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.prefs.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Provides a panel in which to edit the database account user preferences.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/09/03 18:02:45 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see edu.umn.genomics.bi.dbutil.DBAccountListModel
 * @see java.util.prefs.Preferences
 * @see java.sql.DriverManager
 */
public class DatabaseAccountEditor extends JPanel {
    DBAccountListModel dblm;
    boolean exitOnClose = false;
    JComboBox acctChooser;
    JTextField nameField;
    JComboBox driverChooser;
    JComboBox urlChooser;
    JComboBox userChooser;
    JPasswordField passwordChooser;
    JFileChooser fileChooser;
    JTextField importTextField;
    JPanel importPanel;
    Box btnPanel;
    JButton closeBtn = new JButton("Close");
    // Listen for changes to the Selected account
    ItemListener itemListener = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		String acctName = (String) e.getItem();
		if (acctName != null && acctName.length() > 0) {
		    try {
			setFields(acctName);
		    } catch (Exception ex) {
			System.err.println("Error getting preferences for "
				+ acctName + " " + ex);
		    }
		}
	    }
	}
    };
    // Listen for changes to the account list
    ListDataListener listListener = new ListDataListener() {
	public void intervalAdded(ListDataEvent e) {
	    setFields();
	}

	public void intervalRemoved(ListDataEvent e) {
	    setFields();

	}

	public void contentsChanged(ListDataEvent e) {
	    if (acctChooser.getSelectedIndex() == e.getIndex0()) {
		setFields();
	    }
	}
    };

    /**
     * Construct a database account user preferences editing panel.
     */
    public DatabaseAccountEditor() throws BackingStoreException {
	this(new DBAccountListModel());
    }

    /**
     * Construct a database account user preferences editing panel.
     * 
     * @param dbListModel
     *            the list model that manages the user preferences.
     */
    public DatabaseAccountEditor(DBAccountListModel dbListModel)
	    throws BackingStoreException {
	dblm = dbListModel;
	acctChooser = new JComboBox(dblm);
	nameField = new JTextField(30);
	nameField.setEditable(false);
	driverChooser = new JComboBox();
	driverChooser.setEditable(true);
	urlChooser = new JComboBox();
	urlChooser.setEditable(true);
	userChooser = new JComboBox();
	userChooser.setEditable(true);
	passwordChooser = new JPasswordField();

	JPanel labels = new JPanel(new GridLayout(0, 1));
	JPanel fields = new JPanel(new GridLayout(0, 1));
	JLabel label;

	// Account Chooser
	label = new JLabel("Select Account: ", SwingConstants.RIGHT);
	label.setToolTipText("Select Account to Edit");
	labels.add(label);
	fields.add(acctChooser);

	// Account Name
	label = new JLabel("Account Name: ", SwingConstants.RIGHT);
	label.setToolTipText("Account Name");
	labels.add(label);
	fields.add(nameField);

	// JDBC Driver
	label = new JLabel("JDBC Driver: ", SwingConstants.RIGHT);
	label.setToolTipText("Java Class name for the JDBC Driver");
	labels.add(label);
	fields.add(driverChooser);

	// JDBC URL
	label = new JLabel("JDBC URL: ", SwingConstants.RIGHT);
	label.setToolTipText("The URL for the Database Server");
	labels.add(label);
	fields.add(urlChooser);

	// Account Username
	label = new JLabel("Username: ", SwingConstants.RIGHT);
	label.setToolTipText("The User Name for the Database Account");
	labels.add(label);
	fields.add(userChooser);

	// Account Password
	label = new JLabel("Password: ", SwingConstants.RIGHT);
	label.setToolTipText("The User Password for the Database Account");
	labels.add(label);
	fields.add(passwordChooser);

	JPanel entry = new JPanel(new BorderLayout());
	entry.add(labels, BorderLayout.WEST);
	entry.add(fields, BorderLayout.CENTER);

	JButton btn;
	// ToolBar Buttons
	JToolBar tb = new JToolBar();

	URL imgURL;

	// New
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/New24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("New");
	}
	btn.setToolTipText("Add New Account");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		newAccount();
	    }
	});
	tb.add(btn);

	// Duplicate
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/Copy24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Duplicate");
	}
	btn.setToolTipText("Duplicate this Account");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		duplicateAccount();
	    }
	});
	tb.add(btn);
	tb.addSeparator();

	// Test
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/development/WebComponent24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Test");
	}
	btn.setToolTipText("Test Connection to Database Server");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    testConnection();
		    JOptionPane.showMessageDialog(((JComponent) e.getSource())
			    .getTopLevelAncestor(), "Able to connect");
		} catch (Exception ex) {
		    JOptionPane.showMessageDialog(((JComponent) e.getSource())
			    .getTopLevelAncestor(), ex,
			    "Data base connection failed",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	});
	tb.add(btn);

	// Apply
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/Save24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Apply");
	}
	btn.setToolTipText("Apply Changes to this Account");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		applyChanges();
	    }
	});
	tb.add(btn);
	tb.addSeparator();

	// Remove
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/Delete24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Remove");
	}
	btn.setToolTipText("Remove this Account");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		removeAccount();
	    }
	});
	tb.add(btn);
	tb.addSeparator();

	// Import
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/Import24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Import");
	}
	btn.setToolTipText("Import Accounts");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		importAccounts();
	    }
	});
	tb.add(btn);

	// Export
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/Export24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Export");
	}
	btn.setToolTipText("Export this Account");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		exportAccount();
	    }
	});
	tb.add(btn);

	// ExportAll
	imgURL = this.getClass().getClassLoader().getResource(
		"toolbarButtonGraphics/general/Export24.gif");
	if (imgURL != null) {
	    btn = new JButton(new ImageIcon(imgURL));
	} else {
	    btn = new JButton("Export All");
	}
	btn.setToolTipText("Export All Accounts");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		exportAllAccounts();
	    }
	});
	tb.add(btn);

	// Apply Buttons
	btnPanel = new Box(BoxLayout.X_AXIS);

	/*
	 * 
	 * // Test btn = new JButton("Test");
	 * btn.setToolTipText("Test Connection to Database Server");
	 * btn.addActionListener( new ActionListener() { public void
	 * actionPerformed(ActionEvent e) { try { testConnection();
	 * JOptionPane.showMessageDialog(
	 * ((JComponent)e.getSource()).getTopLevelAncestor(),
	 * "Able to connect"); } catch (Exception ex) {
	 * JOptionPane.showMessageDialog(
	 * ((JComponent)e.getSource()).getTopLevelAncestor(), ex,
	 * "Data base connection failed", JOptionPane.ERROR_MESSAGE); } } } );
	 * btnPanel.add(btn);
	 * 
	 * // Apply btn = new JButton("Apply");
	 * btn.setToolTipText("Apply Changes to this Account");
	 * btn.addActionListener( new ActionListener() { public void
	 * actionPerformed(ActionEvent e) { applyChanges(); } } );
	 * btnPanel.add(btn);
	 */

	setLayout(new BorderLayout());
	add(entry);
	add(tb, BorderLayout.NORTH);
	Box btnBox = new Box(BoxLayout.Y_AXIS);
	btnBox.setAlignmentX(Component.CENTER_ALIGNMENT);
	btnBox.add(btnPanel);
	add(btnBox, BorderLayout.SOUTH);
	// start with the first account selected
	if (dblm.getSize() > 0) {
	    dblm.setSelectedItem(dblm.getElementAt(0));
	    setFields();
	}
	// add listeners
	acctChooser.addItemListener(itemListener);
	dblm.addListDataListener(listListener);
    }

    private void addCloseButton() {
	// Close
	closeBtn.setToolTipText("Close");
	closeBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		cancel();
	    }
	});
	btnPanel.add(closeBtn);
    }

    /**
     * Create a new account.
     */
    private void newAccount() {
	String message = "Please enter a new accountName";
	while (true) {
	    String inputValue = JOptionPane
		    .showInputDialog("Please enter a new accountName");
	    if (inputValue == null || inputValue.length() < 1) {
		break;
	    } else {
		try {
		    setFields(inputValue);
		    break;
		} catch (Exception ex) {
		    JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
			    ex, "Creating new account failed",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }

    /**
     * Duplicate the selected account.
     */
    private void duplicateAccount() {
	String drvr = (String) driverChooser.getSelectedItem();
	String url = (String) urlChooser.getSelectedItem();
	String usr = (String) userChooser.getSelectedItem();
	String pwd = new String(passwordChooser.getPassword());

	newAccount();

	driverChooser.setSelectedItem(drvr);
	urlChooser.setSelectedItem(url);
	userChooser.setSelectedItem(usr);
	passwordChooser.setText(pwd);

    }

    /**
     * Remove the selected account.
     */
    private void removeAccount() {
	int i = acctChooser.getSelectedIndex();
	try {
	    String name = nameField.getText();
	    if (name != null && name.length() > 0) {
		dblm.removeAccount(nameField.getText());
		if (i > 0) {
		    acctChooser.setSelectedIndex(i - 1);
		} else if (i < dblm.getSize()) {
		    acctChooser.setSelectedIndex(i);
		}
	    }
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), ex,
		    "Removing the account failed", JOptionPane.ERROR_MESSAGE);
	}
    }

    private void importAccounts() {
	String[] optionNames = { "Import", "Cancel" };
	try {
	    int choice = JOptionPane.showOptionDialog(this
		    .getTopLevelAncestor(), getImportPanel(),
		    "Import Database User Parameters",
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
		    null, optionNames, optionNames[0]);
	    switch (choice) {
	    case 0:
		dblm.importPreferences(importTextField.getText());
		break;
	    }
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), ex,
		    "Importing accounts failed", JOptionPane.ERROR_MESSAGE);
	}
    }

    private void exportAccount() {
	String accnt = (String) dblm.getSelectedItem();
	exportAccount(accnt);
    }

    private void exportAccount(String accnt) {
	try {
	    if (fileChooser == null) {
		fileChooser = new JFileChooser();
	    }
	    int returnVal = fileChooser.showOpenDialog(this
		    .getTopLevelAncestor());
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		File file = fileChooser.getSelectedFile();
		dblm.exportPreferences(file.getAbsolutePath(), accnt);
	    } else {
	    }
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), ex,
		    "Data base user export failed", JOptionPane.ERROR_MESSAGE);
	}
    }

    private void exportAllAccounts() {
	exportAccount(null);
    }

    private void applyChanges() {
	try {
	    String name = nameField.getText();
	    String drvr = (String) driverChooser.getSelectedItem();
	    String url = (String) urlChooser.getSelectedItem();
	    String usr = (String) userChooser.getSelectedItem();
	    String pwd = new String(passwordChooser.getPassword());
	    dblm.addAccount(name);
	    dblm.setDriver(name, drvr);
	    dblm.setURL(name, url);
	    dblm.setUser(name, usr);
	    dblm.setPassword(name, pwd);
	    dblm.setSelectedItem(name);
	    setFields(name);
	} catch (Exception ex) {
	    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), ex,
		    "Applying account changes failed",
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    private boolean testConnection() throws SQLException {
	String name = nameField.getText();
	String drvr = (String) driverChooser.getSelectedItem();
	String url = (String) urlChooser.getSelectedItem();
	String usr = (String) userChooser.getSelectedItem();
	String pwd = new String(passwordChooser.getPassword());
	try {
	    Class.forName(drvr);
	} catch (ClassNotFoundException cnfex) {
	}
	Connection tc = DriverManager.getConnection(url, usr, pwd);
	tc.close();
	return true;
    }

    private void cancel() {
	if (exitOnClose) {
	    System.exit(0);
	} else {
	    Container c = getTopLevelAncestor();
	    if (c instanceof Window) {
		((Window) c).dispose();
	    }
	}
    }

    private synchronized void setFields(String accountName)
	    throws BackingStoreException {
	nameField.setText(accountName);
	driverChooser
		.setModel(new DefaultComboBoxModel(dblm.getKnownDrivers()));
	driverChooser.setSelectedItem(dblm.getDriver(accountName));
	urlChooser.setModel(new DefaultComboBoxModel(dblm.getKnownURLs()));
	urlChooser.setSelectedItem(dblm.getURL(accountName));
	userChooser.setModel(new DefaultComboBoxModel(dblm.getKnownUsers()));
	userChooser.setSelectedItem(dblm.getUser(accountName));
	passwordChooser.setText(dblm.getPassword(accountName));
    }

    private void setFields() {
	String acctName = (String) acctChooser.getSelectedItem();
	if (acctName != null && acctName.length() > 0) {
	    try {
		setFields(acctName);
	    } catch (Exception ex) {
		JOptionPane.showMessageDialog(this.getTopLevelAncestor(), ex,
			"Unable to retrieve account preferences",
			JOptionPane.ERROR_MESSAGE);
	    }
	}
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
     * Import accounts from the preferences urls given.
     * 
     * @param args
     *            urls from which to import account preference information
     */
    public void importAccounts(String[] args) {
	if (args != null) {
	    for (int i = 0; i < args.length; i++) {
		try {
		    dblm.importPreferences(args[i]);
		} catch (Exception ex) {
		    System.err
			    .println("Error importing Database Account preferences: "
				    + ex);
		}
	    }
	}
    }

    /**
     * Display the DatabaseAccountEditor as a popup Dialog.
     * 
     * @param parentComponent
     *            the parent for this entry form panel.
     */
    public void show(Component parentComponent) {
	Component parent = (parentComponent != null ? parentComponent
		: getTopLevelAncestor());
	String[] optionNames = { "Close" };
	String connectTitle = "Edit Database Account Preferences";
	this.validate();
	int choice = JOptionPane.showOptionDialog(parent, this, connectTitle,
		JOptionPane.CLOSED_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		optionNames, optionNames[0]);
    }

    /**
     * Open a frame with database user account connection parameters.
     * 
     * @param args
     *            urls from which to import account preference information
     */
    public static void main(String[] args) {
	DatabaseAccountEditor dbacct = null;
	JFrame frame = new JFrame("Edit Database Account Preferences");
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	try {
	    dbacct = new DatabaseAccountEditor();
	    dbacct.addCloseButton();
	    dbacct.exitOnClose = true;
	    dbacct.importAccounts(args);
	} catch (Exception ex) {
	    System.err.println("DatabaseAccountEditor " + ex);
	}
	frame.getContentPane().add(dbacct);
	frame.pack();
	frame.setVisible(true);
    }
}
