package net.nbirn.srbclient.plugin.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

// dimport net.nbirn.srbclient.plugin.views.FRDisplayOptionsDialog;

/**
 * @author wp066
 *
 */
public class ViewOptionToolbarActionDelegate implements IViewActionDelegate {
	
	private FRView workView;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		workView = (FRView) view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		FRDisplayOptionsDialog dlg = new FRDisplayOptionsDialog(Display.getCurrent().getActiveShell());
	  dlg.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		

	}

}
