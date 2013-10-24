package net.nbirn.srbclient.plugin;

import net.nbirn.srbclient.plugin.views.ClientFolderView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class PerspectiveFactory implements IPerspectiveFactory {

	public static String PERSPECTIVE_ID = "net.nbirn.srbclient.plugin.PerspectiveFactory";
	
	public void createInitialLayout(IPageLayout layout) {
		
		//layout.addView(WorkPlaceView.VIEW_ID,IPageLayout.TOP,0.5f,layout.getEditorArea());
		//layout.addView(SrbLogView.VIEW_ID,IPageLayout.TOP,0.5f,layout.getEditorArea()); //WorkPlaceView.VIEW_ID);

		layout.addView(ClientFolderView.VIEW_ID,IPageLayout.LEFT,0.5f,layout.getEditorArea());
		//layout.addView(SrbFolderView.VIEW_ID,IPageLayout.RIGHT,0.5f,layout.getEditorArea());
		//layout.addView(SrbLogView.VIEW_ID,IPageLayout.TOP,0.2f,WorkPlaceView.VIEW_ID);
		layout.setEditorAreaVisible(false);

	}

}
