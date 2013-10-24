package org.gridlab.gridsphere.jsrtutorial.portlets.globaldata;


import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridlab.gridsphere.portlet.PortletRole;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;

import edu.harvard.i2b2.pm.services.RegisteredCell;
import edu.harvard.i2b2.pm.services.RegisteredCellParam;
import edu.harvard.i2b2.pm.services.GlobalDataService;


import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import java.util.List;


public class RegisteredCellPortlet extends ActionPortlet {

	private edu.harvard.i2b2.pm.services.GlobalDataService globaldataservice = null;
	// private edu.harvard.i2b2.pm.ws.DatabaseManagerService dbManagerService = null;
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		try {
			PortletServiceFactory factory = SportletServiceFactory.getInstance();
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

		/* */
		List addresses = globaldataservice.getRegisteredCell();

		ListBoxBean addr = event.getListBoxBean("registeredcelllist");
		addr.clear();

		if (addresses!=null) {
			for (int i=0;i<addresses.size();i++) {
				RegisteredCell globaldata = (RegisteredCell)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(globaldata.getOid());
				item.setValue(globaldata.getName()); // +", "+globaldata.getUrl());
				addr.addBean(item);
			}
		}

		//Add soap list
		ListBoxBean web = event.getListBoxBean("webservicelist");
		web.clear();

		ListBoxItemBean item = new ListBoxItemBean();
		item.setName("SOAP");
		item.setValue("SOAP" );
		web.addBean(item);

		ListBoxItemBean item2 = new ListBoxItemBean();
		item2.setName("REST");
		item2.setValue("REST" );
		web.addBean(item2);
		web.sortByValue();

		// let's sort it
		addr.sortByValue();


		setNextState(request, "registeredcell/view.jsp");
	}

	public void editRegisteredCell(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		ListBoxBean gDatalist = event.getListBoxBean("registeredcelllist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {

			TextFieldBean edit = event.getTextFieldBean("edit");
			edit.setValue("true");

			// now get the address by oid
			RegisteredCell gData = globaldataservice.getRegisteredCellByOid(oid);

			if (gData != null) {
System.out.println("I AM HERE");
			TextFieldBean name = event.getTextFieldBean("name");
			name.setValue(gData.getName());
			System.out.println("Name is "  + gData.getName());
			ListBoxBean webservice = event.getListBoxBean("webservicelist"); //.getTextFieldBean("value");
			/*
			for (int i=0; i < webservice.getSize(); i++)
			{
				if (webservice.getValue().equals(gData.getWebservice()))
					webservice.setSelected(true);
			}
			*/
			TextFieldBean id = event.getTextFieldBean("id");
			id.setValue(gData.getId());
			TextFieldBean value = event.getTextFieldBean("url");
			value.setValue(gData.getUrl());
			}
		}
		setNextState(request, "showList");
	}
		
	public void addRegisteredCell(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();
		/* */
		// get the values
		TextFieldBean name = event.getTextFieldBean("name");
		ListBoxBean webservice = event.getListBoxBean("webservicelist"); //.getTextFieldBean("value");
		TextFieldBean id = event.getTextFieldBean("id");
		TextFieldBean value = event.getTextFieldBean("url");
		// Deal with Name/Value Pairs
		/*
		System.out.println("Name 1: " + event.getTextFieldBean("Name1").getValue());
		System.out.println("Value 1: " + event.getTextFieldBean("Value1").getValue());
		System.out.println("Name 2: " + event.getTextFieldBean("Name2").getValue());
		System.out.println("Value 2: " + event.getTextFieldBean("Value2").getValue());
		System.out.println("Name 3: " + event.getTextFieldBean("Name3").getValue());
		System.out.println("Value 3: " + event.getTextFieldBean("Value3").getValue());
		*/



		// construct the address
		RegisteredCell gParam = new RegisteredCell();
		gParam.setName(name.getValue());
		gParam.setUrl(value.getValue());
		gParam.setId(id.getValue());
		gParam.setWebservice(webservice.getSelectedValue());

		/* */
		int count = 0;
		while (event.getTextFieldBean("ReadName" + count).getValue() != null)
		{
			System.out.println("Value " + count + ": " + event.getTextFieldBean("RealValue" + count).getValue());
			RegisteredCellParam cellParam = new RegisteredCellParam();
			cellParam.setName(event.getTextFieldBean("RealName" + count).getValue());
			cellParam.setValue(event.getTextFieldBean("RealValue" + count).getValue());
			System.out.println("Name n: " + event.getTextFieldBean("RealName" + count).getValue());
			gParam.getParams().add(cellParam);
			count++;
			if (count==100)
				break;
		}
		// save the address
		globaldataservice.saveRegisteredCell(gParam);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteRegisteredCell(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		/* */
		// get the listbox
		ListBoxBean gDatalist = event.getListBoxBean("registeredcelllist");

		// get the oid
		String oid = gDatalist.getSelectedName();
		if (oid!=null) {
			// now get the address by oid
			RegisteredCell gData = globaldataservice.getRegisteredCellByOid(oid);
			// ... and delete the oid
			globaldataservice.deleteRegisteredCell(gData);
		}
		setNextState(request, "showList");
	}
}
