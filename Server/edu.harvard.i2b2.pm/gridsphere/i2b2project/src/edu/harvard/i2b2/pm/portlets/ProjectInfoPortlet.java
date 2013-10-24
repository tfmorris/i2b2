package edu.harvard.i2b2.pm.portlets;


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
import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.services.GroupData;

import java.util.List;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import javax.servlet.UnavailableException;


public class ProjectInfoPortlet   extends ActionPortlet {

	LayoutManagerService layout = null;
	private GlobalDataService globaldataservice = null;

     public void init(PortletConfig config) throws UnavailableException {
		super.init(config);
		try {
			layout  =  (LayoutManagerService) config.getContext().getService(LayoutManagerService.class);
			globaldataservice = (GlobalDataService) config.getContext().getService(GlobalDataService.class);
		} catch (Exception e) {
			System.err.println("Unable to initialize GlobalDataService: ");
		}
		DEFAULT_VIEW_PAGE = "showList";
	}

	public void showList(FormEvent event) {

		PortletRequest request = event.getPortletRequest();

		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();

		//PortletTabbedPane tab2 = pg.getSelectedTab();


		GroupData pData = globaldataservice.getGroupDataByOid(tab.getSelectedTab().getTitle());

		TextBean oid = event.getTextBean("oid");
		TextFieldBean wiki2 = event.getTextFieldBean("wiki");
		TextFieldBean name = event.getTextFieldBean("name");

		oid.setValue(tab.getSelectedTab().getTitle());
		if (pData != null)
		{
			name.setValue(pData.getName());
			wiki2.setValue(pData.getWiki());
		}
		setNextState( request, "infodata/view.jsp");
	}


	public void addGroupData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();
		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();

		// get the values
		TextFieldBean wiki = event.getTextFieldBean("wiki");
		TextFieldBean name = event.getTextFieldBean("name");
		//ListBoxBean project = event.getListBoxBean("projectlist"); //.getTextFieldBean("value");

		// construct the address
		GroupData gParam = new GroupData();
		gParam.setWiki(wiki.getValue());
		gParam.setName(name.getValue());
		gParam.setOid(tab.getSelectedTab().getTitle());
		//gParam.setOid(project.getSelectedValue());

		// save the address
		globaldataservice.saveGroupData(gParam);
		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteGroupData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("infodatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			GroupData gData = globaldataservice.getGroupDataByOid(oid);
			// ... and delete the oid

			gData.setWiki("");
			gData.setName("");
			globaldataservice.saveGroupData(gData);
			//globaldataservice.deleteParamData(gData);
		}
		setNextState(request, "showList");
	}
}

