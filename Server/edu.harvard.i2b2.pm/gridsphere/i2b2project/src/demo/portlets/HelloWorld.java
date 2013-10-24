package demo.portlets;


import org.gridlab.gridsphere.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

import org.gridlab.gridsphere.layout.PortletPage;
import org.gridlab.gridsphere.layout.PortletTab;
import org.gridlab.gridsphere.layout.PortletTabbedPane;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.provider.event.FormEvent;
import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.*;
import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;
import org.gridlab.gridsphere.layout.*;


import java.util.List;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import javax.servlet.UnavailableException;


public class HelloWorld   extends ActionPortlet {

	LayoutManagerService layout = null;

     public void init(PortletConfig config) throws UnavailableException {
		super.init(config);
		try {
			PortletServiceFactory factory = SportletServiceFactory.getInstance();
			layout  =  (LayoutManagerService) config.getContext().getService(LayoutManagerService.class);
			//layout = (LayoutManagerService)factory.createPortletService(LayoutManagerService.class, null, true);
		} catch (Exception e) {
			System.err.println("Unable to initialize GlobalDataService: ");
		}
		DEFAULT_VIEW_PAGE = "showList";
	}


    //public void doView(PortletRequest request) throws PortletException, IOException {

	public void addGlobalData(FormEvent event) {


	PortletRequest request = event.getPortletRequest();
//	PrintWriter  out = event.getPortletResponse().getWriter();


		try {
//PortletRequest request = event.getPortletRequest();
PortletPage pg = layout.getPortletPage(request);
PortletTabbedPane tab = pg.getPortletTabbedPane();
//out.println("My page is " + pg.getTitle());

//out.println("My page2 is " + tab); //.getLayoutDescriptor());
System.out.println("My page3 is " + tab.getLastPortletTab().getTitle()); //.getLayoutDescriptor());
//out.println("My page4 is " + tab.getPortletTabs() ); //.getLayoutDescriptor());




} catch (Exception e)
{
			System.err.println(e.getMessage()	 + "Unable to initialize GlobalDataService: ");
}
		setNextState( request, "globaldata/view.jsp");

	}


	public void showList(FormEvent event) {

PortletRequest request = event.getPortletRequest();		/*
		try {
PrintWriter  out = event.getPortletResponse().getWriter();
	PortletRequest request = event.getPortletRequest();
out.println("My tab is :" + layout); //.getSelectedTab.getTitle());
out.println("My tab is :" + layout.getUserTabbedPane(request)); //.getSelectedTab.getTitle());


PortletPage pg = layout.getPortletPage(request);
PortletTabbedPane tab = pg.getPortletTabbedPane();
out.println("My page is " + pg.getTitle());

//out.println("My page2 is " + tab); //.getLayoutDescriptor());
out.println("My page3 is " + tab.getLastPortletTab().getTitle()); //.getLayoutDescriptor());
out.println("My page4 is " + tab.getPortletTabs() ); //.getLayoutDescriptor());




} catch (Exception e)
{
			System.err.println(e.getMessage()	 + "Unable to initialize GlobalDataService: ");
}
*/
//		PortletTabbedPane pane = layout.getUserTabbedPane(request);

//		out.println("here :" + pane.getSelectedTab());
//			out.println("<h1>Hello Worldssssss</h1>");

//out.println("<ui:form>");


		setNextState( request, "globaldata/view.jsp");


}
}