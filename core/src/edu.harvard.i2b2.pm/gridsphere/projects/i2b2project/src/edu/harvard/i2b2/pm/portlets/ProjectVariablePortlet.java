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
import edu.harvard.i2b2.pm.services.VariableData;

import java.util.List;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import javax.servlet.UnavailableException;


public class ProjectVariablePortlet   extends ActionPortlet {

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


				List addresses = globaldataservice.getVariableData(tab.getSelectedTab().getTitle());
					System.out.println("My address" + addresses);
				ListBoxBean addr = event.getListBoxBean("variabledatalist");
				addr.clear();
System.out.println("I AM IN VARPORT2 :"  + tab.getSelectedTab().getTitle());
				if (addresses!=null) {
					System.out.println("My address" + addresses.size());
					for (int i=0;i<addresses.size();i++) {


						VariableData globaldata = (VariableData)addresses.get(i);
System.out.println("I AM IN VARPORT name= " +globaldata.getName());
						ListBoxItemBean item = new ListBoxItemBean();
						item.setName(globaldata.getOid());
						item.setValue(globaldata.getName() +" =  "+globaldata.getValue());
						addr.addBean(item);
					}
				}
				setNextState( request, "variabledata/view.jsp");
	}



	public void addVariableData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();
		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();


		// get the values
		TextFieldBean name = event.getTextFieldBean("name");
		//ListBoxBean project = event.getListBoxBean("projectlist"); //.getTextFieldBean("value");
		TextFieldBean value = event.getTextFieldBean("value");

		// construct the address
		VariableData gParam = new VariableData();
		gParam.setName(name.getValue());
		gParam.setValue(value.getValue());
		gParam.setProject(tab.getSelectedTab().getTitle());

		// save the address
		globaldataservice.saveVariableData(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteVariableData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("variabledatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			VariableData gData = globaldataservice.getVariableDataByOid(oid);
			// ... and delete the oid
			if (gData != null)
				globaldataservice.deleteVariableData(gData);
		}
		setNextState(request, "showList");
	}
}