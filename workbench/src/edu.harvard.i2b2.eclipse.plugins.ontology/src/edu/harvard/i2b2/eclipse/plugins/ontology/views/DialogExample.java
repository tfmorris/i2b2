package edu.harvard.i2b2.eclipse.plugins.ontology.views;


import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class DialogExample {
	  public void run() {
		    Display display = new Display();
		    Shell shell = new Shell(display);
		    createContents(shell);
		    shell.pack();
		    shell.open();
		    while (!shell.isDisposed()) {
		      if (!display.readAndDispatch()) {
		        display.sleep();
		      }
		    }
		    display.dispose();
		  }

		  private void createContents(final Shell parent) {
		    parent.setLayout(new FillLayout(SWT.VERTICAL));

		    final Label label = new Label(parent, SWT.NONE);

		    Button button = new Button(parent, SWT.PUSH);
		    button.setText("Push Me");
		    button.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        // Create and display the InputDialog
		    	  OntologyDisplayOptionsDialog dlg = new OntologyDisplayOptionsDialog(parent);
		    	  dlg.open();
//		        if (input != null) {
//		          // User clicked OK; set the text into the label
//		          label.setText(input);
//		          label.getParent().pack();
//		        }
		      }
		    });
		  }

		  public static void main(String[] args) {
		    new DialogExample().run();
		  }
}
