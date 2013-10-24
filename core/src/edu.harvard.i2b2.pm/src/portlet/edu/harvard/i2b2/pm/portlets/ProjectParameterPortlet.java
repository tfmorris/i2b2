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
import edu.harvard.i2b2.pm.services.ParamData;

import java.util.List;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import javax.servlet.UnavailableException;


public class ProjectParameterPortlet   extends ActionPortlet {

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


				List addresses = globaldataservice.getParamData(tab.getSelectedTab().getTitle());
				ListBoxBean addr = event.getListBoxBean("parameterdatalist");
				addr.clear();

				if (addresses!=null) {
					for (int i=0;i<addresses.size();i++) {
						ParamData globaldata = (ParamData)addresses.get(i);
						ListBoxItemBean item = new ListBoxItemBean();
						item.setName(globaldata.getOid());
						item.setValue(globaldata.getName() +" =  "+globaldata.getValue());
						addr.addBean(item);
					}
				}
				setNextState( request, "globaldata/view.jsp");
	}



	public void addParameterData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();
		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();


		// get the values
		TextFieldBean name = event.getTextFieldBean("name");
		//ListBoxBean project = event.getListBoxBean("projectlist"); //.getTextFieldBean("value");
		TextFieldBean value = event.getTextFieldBean("value");

		// construct the address
		ParamData gParam = new ParamData();
		gParam.setName(name.getValue());
		gParam.setValue(value.getValue());
		gParam.setProject(tab.getSelectedTab().getTitle());

		// save the address
		globaldataservice.saveParamData(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteParameterData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("parameterdatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			ParamData gData = globaldataservice.getParamDataByOid(oid);
			// ... and delete the oid
			if (gData != null)
				globaldataservice.deleteParamData(gData);
		}
		setNextState(request, "showList");
	}
}