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

import edu.harvard.i2b2.pm.services.ProjectData;
import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.ws.DatabaseManagerService;

import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextBean;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import java.util.Iterator;
import java.util.List;


public class ProjectSecurityPortlet extends ActionPortlet {

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
		ProjectData pData = globaldataservice.getProjectDataByOid("asthma");
		
		TextFieldBean key = event.getTextFieldBean("key");

		if (pData != null)
			key.setValue(pData.getKey());
		
		/*
		ListBoxBean addr = event.getListBoxBean("infodatalist");
		addr.clear();

		if (addresses!=null) {
			for (int i=0;i<addresses.size();i++) {
				ProjectData globaldata = (ProjectData)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(globaldata.getName());
				item.setValue("(" + globaldata.getOid() + ") " + globaldata.getWiki());
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
		 */

		setNextState(request, "securitydata/view.jsp");
	}

	public void addProjectData(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		// get the values
		TextFieldBean key = event.getTextFieldBean("key");
		//ListBoxBean project = event.getListBoxBean("projectlist"); //.getTextFieldBean("value");

		// construct the address
		ProjectData gParam = new ProjectData();
		gParam.setKey(key.getValue());
		gParam.setName("asthma");
		//gParam.setOid(project.getSelectedValue());
		
		// save the address
		globaldataservice.saveProjectData(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}
	/*
	public void deleteProjectData(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("infodatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			ProjectData gData = globaldataservice.getProjectDataByOid(oid);
			// ... and delete the oid
			
			gData.setKey("");
			globaldataservice.saveProjectData(gData);
			//globaldataservice.deleteParamData(gData);
		}
		setNextState(request, "showList");
	}
	*/
}

