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
import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;

import org.gridlab.gridsphere.layout.*;


import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.services.RoleData;


import java.util.List;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import javax.servlet.UnavailableException;


public class ProjectRolePortlet   extends ActionPortlet {

	LayoutManagerService layout = null;
	private GlobalDataService globaldataservice = null;
		GroupManagerService groupService = null;
		RoleManagerService roleService = null;
		UserManagerService userService = null;

     public void init(PortletConfig config) throws UnavailableException {
		super.init(config);
		try {
			layout  =  (LayoutManagerService) config.getContext().getService(LayoutManagerService.class);
			globaldataservice = (GlobalDataService) config.getContext().getService(GlobalDataService.class);
			groupService = (GroupManagerService) config.getContext().getService(GroupManagerService.class);
			roleService = (RoleManagerService) config.getContext().getService(RoleManagerService.class);
			userService = (UserManagerService) config.getContext().getService(UserManagerService.class);

		} catch (Exception e) {
			System.err.println("Unable to initialize GlobalDataService: ");
		}
		DEFAULT_VIEW_PAGE = "showList";
	}

	public void showList(FormEvent event) {

		PortletRequest request = event.getPortletRequest();
		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();



		List addresses = globaldataservice.getRoleData(tab.getSelectedTab().getTitle());
		ListBoxBean addr = event.getListBoxBean("roledatalist");
		addr.clear();

		if (addresses!=null) {
			for (int i=0;i<addresses.size();i++) {
				RoleData globaldata = (RoleData)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(globaldata.getOid());
				item.setValue(globaldata.getUser() + " =  "+globaldata.getRole());
				addr.addBean(item);
			}
		}

		// get project


		try {
			List groups = groupService.getGroups(); //.getGroups(user);
			//Iterator itsg = groups.iterator();

			//Get Roels
			List roles = roleService.getRoles();
			ListBoxBean rle = event.getListBoxBean("rolelist");
			rle.clear();

			if (roles!=null) {
				for (int i=0;i<roles.size();i++) {
					PortletRole g = (PortletRole) roles.get(i);

					//ParamData globaldata = (ParamData)addresses.get(i);
					ListBoxItemBean item = new ListBoxItemBean();
					item.setName(g.getName());
					item.setValue(g.getName() );
					rle.addBean(item);
				}
			}
			rle.sortByValue();


			//Get User
			List users = userService.getUsers();
			ListBoxBean usr = event.getListBoxBean("userlist");
			usr.clear();

			if (users!=null) {
				for (int i=0;i<users.size();i++) {
					User g = (User) users.get(i);

					//ParamData globaldata = (ParamData)addresses.get(i);
					ListBoxItemBean item = new ListBoxItemBean();
					item.setValue(g.getFullName());
					item.setName(g.getUserName() );
					usr.addBean(item);
				}
			}
			usr.sortByValue();
		} catch (Exception e){ e.printStackTrace();}

		// let's sort it
		addr.sortByValue();

		setNextState( request, "roledata/view.jsp");
	}


	public void addRoleData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();
		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();

		// get the values
		//TextFieldBean project = event.getTextFieldBean("project");
		ListBoxBean role = event.getListBoxBean("rolelist");
		ListBoxBean user = event.getListBoxBean("userlist");

		// construct the address
		RoleData gParam = new RoleData();
		//gParam.setProject(project.getValue());
		gParam.setProject(tab.getSelectedTab().getTitle());
		gParam.setRole(role.getSelectedValue());
		gParam.setUser(user.getSelectedValue());

		// save the address
		if (!gParam.getProject().equals("") &&
				!gParam.getRole().equals("") &&
				!gParam.getUser().equals(""))
		globaldataservice.saveRoleData(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteRoleData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("roledatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			RoleData gData = globaldataservice.getRoleDataByOid(oid);
			// ... and delete the oid
			if (gData != null)
				globaldataservice.deleteRoleData(gData);

		}
		setNextState(request, "showList");
	}
}