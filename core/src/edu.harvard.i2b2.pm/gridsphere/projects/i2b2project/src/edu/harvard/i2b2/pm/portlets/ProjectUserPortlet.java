package edu.harvard.i2b2.pm.portlets;


import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.layout.PortletPage;
import org.gridlab.gridsphere.layout.PortletTabbedPane;
import org.gridlab.gridsphere.provider.event.FormEvent;
import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.*;
import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;

import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.services.UserData;

import java.util.List;
import javax.servlet.UnavailableException;


public class ProjectUserPortlet   extends ActionPortlet {

	LayoutManagerService layout = null;
	private GlobalDataService globaldataservice = null;
	private UserManagerService userService = null;

	public void init(PortletConfig config) throws UnavailableException {
		super.init(config);
		try {
			layout  =  (LayoutManagerService) config.getContext().getService(LayoutManagerService.class);
			globaldataservice = (GlobalDataService) config.getContext().getService(GlobalDataService.class);
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


		List addresses = globaldataservice.getUserData(tab.getSelectedTab().getTitle());
		ListBoxBean addr = event.getListBoxBean("userdatalist");
		addr.clear();
		if (addresses!=null) {
			for (int i=0;i<addresses.size();i++) {


				UserData globaldata = (UserData)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(globaldata.getOid());
				item.setValue(globaldata.getUser() + ": " + globaldata.getName() +" =  "+globaldata.getValue());
				addr.addBean(item);
			}
		}
		
		

		try {
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
		
		setNextState( request, "userdata/view.jsp");
	}



	public void addUserData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();
		PortletPage pg = layout.getPortletPage(request);
		PortletTabbedPane tab = pg.getPortletTabbedPane();


		// get the values
		ListBoxBean user = event.getListBoxBean("userlist"); //.getTextFieldBean("value");
		TextFieldBean name = event.getTextFieldBean("name");
		//ListBoxBean project = event.getListBoxBean("projectlist"); //.getTextFieldBean("value");
		TextFieldBean value = event.getTextFieldBean("value");

		// construct the address
		UserData gParam = new UserData();
		gParam.setName(name.getValue());
		gParam.setUser(user.getSelectedName());
		gParam.setValue(value.getValue());
		gParam.setProject(tab.getSelectedTab().getTitle());

		// save the address
		globaldataservice.saveUserData(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteUserData(FormEvent event) {
		PortletRequest request = event.getPortletRequest();

		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("userdatalist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			UserData gData = globaldataservice.getUserDataByOid(oid);
			// ... and delete the oid
			if (gData != null)
				globaldataservice.deleteUserData(gData);
		}
		setNextState(request, "showList");
	}
}