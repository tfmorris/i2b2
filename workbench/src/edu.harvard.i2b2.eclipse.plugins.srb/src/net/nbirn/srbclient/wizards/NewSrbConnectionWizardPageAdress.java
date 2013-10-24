package net.nbirn.srbclient.wizards;



import net.nbirn.srbclient.plugin.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class NewSrbConnectionWizardPageAdress extends WizardPage implements VerifyListener{

	private Text txtServerAddress;
	private Text txtAccountName;
	private Text txtPassword;
	private Text txtPort;
	private Text txtHomeDirectory;
	private Text txtMdasDomainHome;
	private Text txtDefaultStorageResource;
	private IPreferenceStore preferenceStore;
	
	public NewSrbConnectionWizardPageAdress(String pageName) {
		super(pageName);
	}

	public NewSrbConnectionWizardPageAdress(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		 //IPreferenceStore preferenceStore = getPreferenceStore();
		
		Composite main = new Composite(parent,SWT.NULL);
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
		main.setLayout(gridLayout);	
		Label label = new Label(main, SWT.NONE);
        label.setText("Server Address");
        txtServerAddress = new Text(main, SWT.BORDER);
        txtServerAddress.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_ADDRESS));
        Label label1 = new Label(main, SWT.NONE);
        label1.setText("User Name");
        txtAccountName = new Text(main, SWT.BORDER);
        txtAccountName.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_ACCOUNT_NAME));
        Label label2 = new Label(main, SWT.NONE);
        label2.setText("Password");
        txtPassword = new Text(main, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_PASSWORD));
        
        Label label3 = new Label(main, SWT.NONE);
        label3.setText("Port");
        txtPort = new Text(main, SWT.BORDER);
        txtPort.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_PORT));

        Label label4 = new Label(main, SWT.NONE);
        label4.setText("Home Directory");
        txtHomeDirectory = new Text(main, SWT.BORDER);
        txtHomeDirectory.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_HOMEDIRECTORY));

        Label label5 = new Label(main, SWT.NONE);
        label5.setText("MdasDomainHome");
        txtMdasDomainHome = new Text(main, SWT.BORDER);
        txtMdasDomainHome.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_MDASDOMAINHOME));

        Label label6 = new Label(main, SWT.NONE);
        label6.setText("DefaultStorageResource");
        txtDefaultStorageResource = new Text(main, SWT.BORDER);
        txtDefaultStorageResource.setText(
        		preferenceStore.getString(PreferenceConstants.SRB_SERVER_DEFAULTSTORAGERESOURCE));
        
        
        txtServerAddress.addVerifyListener(this);
        txtAccountName.addVerifyListener(this);
        txtPassword.addVerifyListener(this);
        txtPort.addVerifyListener(this);
        txtHomeDirectory.addVerifyListener(this);
        txtMdasDomainHome.addVerifyListener(this);
        txtDefaultStorageResource.addVerifyListener(this);
        
        setControl(main);
        setPageComplete(false);      
	}

	public void verifyText(VerifyEvent e) {
		if ( 	txtServerAddress.getText().length() > 0 && 
				txtAccountName.getText().length() > 0 && 
				txtPort.getText().length() > 0 && 
				txtHomeDirectory.getText().length() > 0 && 
				txtMdasDomainHome.getText().length() > 0 && 
				txtDefaultStorageResource.getText().length() > 0 && 
				txtPassword.getText().length() > 0
			)
		{
			setPageComplete(true);
		}
		else
			setPageComplete(false);
		
	}

	public void retrieveFromWorkSpace(IPreferenceStore preferenceStr) {
		preferenceStore = preferenceStr;
	
	}
	
	public void saveToWorkSpace(IPreferenceStore preferenceStore) {
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_ADDRESS,txtServerAddress.getText());
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_ACCOUNT_NAME,txtAccountName.getText());
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_PASSWORD,txtPassword.getText());
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_PORT,txtPort.getText());
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_HOMEDIRECTORY,txtHomeDirectory.getText());
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_MDASDOMAINHOME,txtMdasDomainHome.getText());
		preferenceStore.putValue(PreferenceConstants.SRB_SERVER_DEFAULTSTORAGERESOURCE,txtDefaultStorageResource.getText());
		
	}

}
