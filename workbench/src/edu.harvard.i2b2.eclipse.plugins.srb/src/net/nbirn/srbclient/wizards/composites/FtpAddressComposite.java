package net.nbirn.srbclient.wizards.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FtpAddressComposite extends Composite {

	private Label label = null;
	//private Text txtServerAddress = null;
	private Label label1 = null;
	//private Text txtAccountName = null;
	private Label label2 = null;
	//private Text txtPassword = null;
	public FtpAddressComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        label = new Label(this, SWT.NONE);
        label.setText("Server Address");
        //txtServerAddress = new Text(this, SWT.BORDER);
        label1 = new Label(this, SWT.NONE);
        label1.setText("User Name");
       // txtAccountName = new Text(this, SWT.BORDER);
        label2 = new Label(this, SWT.NONE);
        label2.setText("Password");
      //  txtPassword = new Text(this, SWT.BORDER | SWT.PASSWORD);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        this.setLayout(gridLayout);
        this.setSize(new org.eclipse.swt.graphics.Point(173,78));
			
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
