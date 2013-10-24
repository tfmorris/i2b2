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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JRootPane;
import javax.swing.UIManager;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.FillLayout;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.SWT;

import edu.harvard.i2b2.exportData.dataModel.ConceptKTableModel;
import edu.harvard.i2b2.exportData.dataModel.TableFactory;
import edu.harvard.i2b2.exportData.dataModel.TimelineRow;
import edu.umn.genomics.table.TableView;
import edu.umn.genomics.table.VirtualTableModel;

public class TableViewComposite extends org.eclipse.swt.widgets.Composite {

	private Composite topComposite;

	private CLabel cpatientLabel;

	private Composite tablecomposite;

	private Combo patientCombo;

	private Composite bottomComposite;

	public Frame frame1;

	private java.awt.Container oAwtContainer;

	private DataExporter imageExplorerC;

	public void imageExplorerC(DataExporter ic) {
		imageExplorerC = ic;
	}

	private int comboIndex = 0;

	/**
	 * Auto-generated main method to display this
	 * org.eclipse.swt.widgets.Composite inside a new Shell.
	 */
	public static void main(String[] args) {
		showGUI();
	}

	/**
	 * Auto-generated method to display this org.eclipse.swt.widgets.Composite
	 * inside a new Shell.
	 */
	public static void showGUI() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		TableViewComposite inst = new TableViewComposite(shell, SWT.NULL);
		Point size = inst.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if (size.x == 0 && size.y == 0) {
			inst.pack();
			shell.pack();
		} else {
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			shell.setSize(shellBounds.width, shellBounds.height);
		}
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public TableViewComposite(org.eclipse.swt.widgets.Composite parent,
			int style) {
		super(parent, style);
		initGUI();
	}

	private void initGUI() {
		try {
			GridLayout thisLayout = new GridLayout();
			thisLayout.makeColumnsEqualWidth = true;
			this.setLayout(thisLayout);
			this.setSize(491, 161);
			{
				topComposite = new Composite(this, SWT.NONE);
				GridData composite1LData = new GridData();
				composite1LData.widthHint = 449;
				composite1LData.heightHint = 25;
				topComposite.setLayoutData(composite1LData);
				topComposite.setLayout(null);
				{
					patientCombo = new Combo(topComposite, SWT.READ_ONLY);
					patientCombo.add("Yes");
					patientCombo.add("No");
					patientCombo.add("UnD");
					patientCombo.select(0);
					patientCombo.setBounds(167, 2, 130, 30);
					//patientCombo.addMouseListener(new MouseAdapter() {
					//	public void mouseDown(MouseEvent evt) {
					//		patientComboMouseDown(evt);
					//	}
					//});

					patientCombo.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent evt) {
							patientComboWidgetSelected(evt);
						}
					});
				}
				{
					cpatientLabel = new CLabel(topComposite, SWT.NONE);
					cpatientLabel.setText("Compiling all patients of type");
					cpatientLabel.setBounds(4, 4, 164, 22);
				}
			}
			{
				GridData composite2LData = new GridData();
				composite2LData.grabExcessHorizontalSpace = true;
				composite2LData.grabExcessVerticalSpace = true;
				composite2LData.horizontalAlignment = GridData.FILL;
				composite2LData.verticalAlignment = GridData.FILL;
				bottomComposite = new Composite(this, SWT.NONE);
				GridLayout composite2Layout = new GridLayout();
				composite2Layout.numColumns = 1;
				bottomComposite.setLayout(composite2Layout);
				bottomComposite.setLayoutData(composite2LData);
				{
					// composite1 = new Composite(bottomComposite, SWT.NONE);
					// GridLayout composite1Layout = new GridLayout();
					// composite1Layout.makeColumnsEqualWidth = true;
					// GridData composite1LData = new GridData();
					// composite1LData.widthHint = 85;
					// composite1LData.verticalAlignment = GridData.FILL;
					// composite1LData.grabExcessVerticalSpace = true;
					// composite1.setLayoutData(composite1LData);
					// composite1.setLayout(composite1Layout);
				}
				{
					GridData tablecompositeLData = new GridData();
					tablecompositeLData.verticalAlignment = GridData.FILL;
					tablecompositeLData.horizontalAlignment = GridData.FILL;
					tablecompositeLData.grabExcessHorizontalSpace = true;
					tablecompositeLData.grabExcessVerticalSpace = true;
					tablecomposite = new Composite(bottomComposite,
							SWT.EMBEDDED | SWT.BORDER);
					GridLayout tablecompositeLayout = new GridLayout();
					tablecompositeLayout.makeColumnsEqualWidth = true;
					tablecomposite.setLayout(tablecompositeLayout);
					tablecomposite.setLayoutData(tablecompositeLData);

					frame1 = SWT_AWT.new_Frame(tablecomposite);
					// frame1.setLayout(new BorderLayout());
					{
						Panel panel = new Panel(new BorderLayout());

						try {
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						} catch (Exception e) {
							System.out.println("Error setting native LAF: " + e);
						}

						frame1.add(panel);
						JRootPane root = new JRootPane();
						panel.add(root);
						oAwtContainer = root.getContentPane();
					}
				}
			}
			this.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void patientComboWidgetSelected(SelectionEvent evt) {

		MessageDialog mBox = new MessageDialog(
				getShell(),
				"Please Note ...",
				null,
				"Changing this option will lose your work if it's not saved. Do you want to change?",
				MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);

		int answer = mBox.open();

		if (answer == 0) {
			if(imageExplorerC.isAll) {
				renderTables(0, imageExplorerC.patientRowData.size(), patientCombo.getText());
			} else if(imageExplorerC.isSelectedPatients) {
				renderTables(imageExplorerC.selectedPatients, patientCombo.getText());
			}
			comboIndex = patientCombo.getSelectionIndex();
		} else {
			patientCombo.select(comboIndex);
		}
	}

	public void setComboText(int index) {

		patientCombo.select(index);
	}

	public void renderTables(final ArrayList<String> ids, final String decision) {
		Thread visualizationQueryThread = new Thread() {
			@Override
			public void run() {

				oAwtContainer.removeAll();

				WaitPanel p = new WaitPanel(
						(int) (oAwtContainer.getWidth() * 0.40),
						(int) (oAwtContainer.getHeight() * 0.40), 5);
				oAwtContainer.add(p);
				p.setBounds(0, 0, p.getParent().getWidth(), p.getParent()
						.getHeight());
				p.init((int) (p.getParent().getWidth() * 0.40), (int) (p
						.getParent().getHeight() * 0.40));
				p.go();
				p.setVisible(true);

				removeTmpTableFiles();
				String result = imageExplorerC.getPDOResultFromPatientIndex(ids, decision);

				ConceptKTableModel i2b2Model = (ConceptKTableModel) imageExplorerC.conceptTable
						.getModel();
				i2b2Model.fillDataFromTable(imageExplorerC.rowData);

				ArrayList<TimelineRow> tlrows = i2b2Model
						.getTimelineRows(imageExplorerC.rowData);
				String status = TableFactory.writeTableFiles(result, tlrows, true);

				// java.awt.EventQueue.invokeLater(new Runnable() {
				// public void run() {
				// oAwtContainer.removeAll();
				
				if(!status.equalsIgnoreCase("Error")) {
					TableView tableview = new TableView();
	
					VirtualTableModel model = tableview.setFile(System
							.getProperty("user.dir")
							+ '/' + "i2b2tmptablefiles/patienttable.txt",
							"PatientDemographics");
					tableview.displayTableModelView("Histogram", model);
					tableview.displayTableModelView("Histograms", model);
					tableview.displayTableModelView("Table", model);
					tableview.displayTableModelView("Histogram3D", model);
	
					for (int i = 0; i < tlrows.size(); i++) {
						model = tableview.setFile(
								System.getProperty("user.dir") + '/'
										+ "i2b2tmptablefiles/facttable_" + i
										+ ".txt", tlrows.get(i).displayName);
						tableview.displayTableModelView("Histogram", model);
						tableview.displayTableModelView("Histograms", model);
						tableview.displayTableModelView("Table", model);
						tableview.displayTableModelView("Histogram3D", model);
					}
					
					model = tableview.setFile(System
							.getProperty("user.dir")
							+ '/' + "i2b2tmptablefiles/eventtable.txt",
							"Events");
					tableview.displayTableModelView("Histogram", model);
					tableview.displayTableModelView("Histograms", model);
					tableview.displayTableModelView("Table", model);
					tableview.displayTableModelView("Histogram3D", model);
	
					oAwtContainer.add(tableview, BorderLayout.CENTER);
					tableview.setBounds(0, 0, tableview.getParent().getWidth(),
							tableview.getParent().getHeight());
	
					tableview.setVisible(false);
					tableview.setVisible(true);
				}
				// }
				// });

				p.stop();
				p.setVisible(false);
			}
		};

		try {
			visualizationQueryThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void renderTables(final int start, final int end,
			final String decision) {
		Thread visualizationQueryThread = new Thread() {
			@Override
			public void run() {

				oAwtContainer.removeAll();

				WaitPanel p = new WaitPanel(
						(int) (oAwtContainer.getWidth() * 0.40),
						(int) (oAwtContainer.getHeight() * 0.40), 5);
				oAwtContainer.add(p);
				p.setBounds(0, 0, p.getParent().getWidth(), p.getParent()
						.getHeight());
				p.init((int) (p.getParent().getWidth() * 0.40), (int) (p
						.getParent().getHeight() * 0.40));
				p.go();
				p.setVisible(true);

				removeTmpTableFiles();
				String result = imageExplorerC.getPDOResult(start, end,
						decision);

				ConceptKTableModel i2b2Model = (ConceptKTableModel) imageExplorerC.conceptTable
						.getModel();
				i2b2Model.fillDataFromTable(imageExplorerC.rowData);

				ArrayList<TimelineRow> tlrows = i2b2Model
						.getTimelineRows(imageExplorerC.rowData);
				String status = TableFactory.writeTableFiles(result, tlrows, true);
				
				// java.awt.EventQueue.invokeLater(new Runnable() {
				// public void run() {
				// oAwtContainer.removeAll();
				if(!status.equalsIgnoreCase("Error")) {
					TableView tableview = new TableView();
	
					VirtualTableModel model = tableview.setFile(System
							.getProperty("user.dir")
							+ '/' + "i2b2tmptablefiles/patienttable.txt",
							"PatientDemographics");
					tableview.displayTableModelView("Histogram", model);
					tableview.displayTableModelView("Histograms", model);
					tableview.displayTableModelView("Table", model);
					tableview.displayTableModelView("Histogram3D", model);
	
					for (int i = 0; i < tlrows.size(); i++) {
						model = tableview.setFile(
								System.getProperty("user.dir") + '/'
										+ "i2b2tmptablefiles/facttable_" + i
										+ ".txt", tlrows.get(i).displayName);
						tableview.displayTableModelView("Histogram", model);
						tableview.displayTableModelView("Histograms", model);
						tableview.displayTableModelView("Table", model);
						tableview.displayTableModelView("Histogram3D", model);
					}
					
					model = tableview.setFile(System
							.getProperty("user.dir")
							+ '/' + "i2b2tmptablefiles/eventtable.txt",
							"Events");
					tableview.displayTableModelView("Histogram", model);
					tableview.displayTableModelView("Histograms", model);
					tableview.displayTableModelView("Table", model);
					tableview.displayTableModelView("Histogram3D", model);
	
					oAwtContainer.add(tableview, BorderLayout.CENTER);
					tableview.setBounds(0, 0, tableview.getParent().getWidth(),
							tableview.getParent().getHeight());
	
					tableview.setVisible(false);
					tableview.setVisible(true);
				}
				// }
				// });

				p.stop();
				p.setVisible(false);
			}
		};

		try {
			visualizationQueryThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeTmpTableFiles() {
		boolean deleteSuccess = false;
		try {
			String filedir = System.getProperty("user.dir") + '/'
					+ "i2b2tmptablefiles";
			File tmpdir = new File(filedir);
			if (tmpdir.exists()) {
				File[] files = tmpdir.listFiles();
				for (int i = 0; i < files.length; i++) {
					File oDelete = files[i];
					deleteSuccess = oDelete.delete();

					System.out.println("Delete " + oDelete.getName() + " "
							+ deleteSuccess);
					deleteSuccess = false;
				}
			} else {
				tmpdir.mkdir();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
