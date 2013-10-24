package edu.harvard.i2b2.pm.portlets;


import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridlab.gridsphere.portlet.PortletGroup;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;

import edu.harvard.i2b2.pm.services.ParamData;
import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.ws.DatabaseManagerService;


import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import java.util.Iterator;
import java.util.List;


public class ProjectParamPortlet extends ActionPortlet {

	private edu.harvard.i2b2.pm.services.GlobalDataService globaldataservice = null;
	private PortletServiceFactory  factory = null;
	// private edu.harvard.i2b2.pm.ws.DatabaseManagerService dbManagerService = null;
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		try {
			 factory = SportletServiceFactory.getInstance();
//			addressservice = (AddressService) factory.createPortletService(AddressService.class, null, true);


			globaldataservice = (edu.harvard.i2b2.pm.services.GlobalDataService)factory.createPortletService(edu.harvard.i2b2.pm.services.GlobalDataService.class, null, true);
//			globaldataservice = (GlobalDataService)createPortletService(GlobalDataService.class);

//			dbManagerService =  (edu.harvard.i2b2.pm.ws.DatabaseManagerService) createPortletService(edu.harvard.i2b2.pm.ws.DatabaseManagerService.class); //, null, true);
//			dbManagerService.setCounter(110);

		} catch (PortletServiceException e) {
			System.err.println("Unable to initialize GlobalDataService: ");
		}
		DEFAULT_VIEW_PAGE = "showList";
	}

	public void showList(RenderFormEvent event) {
		PortletRequest request = event.getRenderRequest();

		List addresses = globaldataservice.getParamData();
		ListBoxBean addr = event.getListBoxBean("paramdatalist");
		addr.clear();

		if (addresses!=null) {
			for (int i=0;i<addresses.size();i++) {
				ParamData globaldata = (ParamData)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(globaldata.getOid());
				item.setValue("(" + globaldata.getProject() + ") " + globaldata.getName() +" =  "+globaldata.getValue());
				addr.addBean(item);
			}
		}

		
		// get project
		
		GroupManagerService groupService = null;
		
		try {
		groupService = (GroupManagerService) factory.createPortletService(GroupManagerService.class, null, true);
		//UserManagerService userManagerService =  (UserManagerService) factory.createPortletService(UserManagerService.class, null, true);
		//User user = userManagerService.getUserByUserName(rmt.getUsername());
		List groups = groupService.getGroups(); //.getGroups(user);
		//Iterator itsg = groups.iterator();

		
		ListBoxBean prj = event.getListBoxBean("projectlist");
		prj.clear();

		if (groups!=null) {
			for (int i=0;i<groups.size();i++) {
				PortletGroup g = (PortletGroup) groups.get(i);
				if (g.getLabel().toUpperCase().equals("GRIDSPHERE"))
					continue;
				
				
				//ParamData globaldata = (ParamData)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(g.getLabel());
				item.setValue(g.getName() );
				prj.addBean(item);
			}
		}
		prj.sortByValue();
		} catch (Exception e){ e.printStackTrace();}
		
		// let's sort it
		addr.sortByValue();
		
		setNextState(request, "paramdata/view.jsp");
	}

	public void addParamData(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		// get the values
		TextFieldBean name = event.getTextFieldBean("name");
		ListBoxBean project = event.getListBoxBean("projectlist"); //.getTextFieldBean("value");
		TextFieldBean value = event.getTextFieldBean("value");

		// construct the address
		ParamData gParam = new ParamData();
		gParam.setName(name.getValue());
		gParam.setValue(value.getValue());
		gParam.setProject(project.getSelectedValue());
		
		// save the address
		globaldataservice.saveParamData(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteParamData(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("paramdatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			ParamData gData = globaldataservice.getParamDataByOid(oid);
			// ... and delete the oid
			globaldataservice.deleteParamData(gData);
		}
		setNextState(request, "showList");
	}
}

