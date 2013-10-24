package net.nbirn.srbclient.plugin.views;

import net.nbirn.srbclient.utils.WorkPlaceTree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class FRView extends ViewPart {

	public static String VIEW_ID = "net.nbirn.srbclient.plugin.views.WorkPlaceView";
	
	private WorkPlaceTree tree;
	
	public FRView() {
		super();
		
	}

	@Override
	public void createPartControl(Composite parent) {
		tree = new WorkPlaceTree(parent,SWT.NONE);
		tree.setLayout(new FillLayout());

	}
	@Override
	public void setFocus() {
		
	}


}
