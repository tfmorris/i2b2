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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class RegisteredCellPortlet extends ActionPortlet {

	private edu.harvard.i2b2.pm.services.GlobalDataService globaldataservice = null;
	// private edu.harvard.i2b2.pm.ws.DatabaseManagerService dbManagerService = null;
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		try {
			PortletServiceFactory factory = SportletServiceFactory.getInstance();
			globaldataservice = (edu.harvard.i2b2.pm.services.GlobalDataService)factory.createPortletService(edu.harvard.i2b2.pm.services.GlobalDataService.class, null, true);
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
				item.setName(globaldata.getId());
				item.setValue(globaldata.getName()); // +", "+globaldata.getUrl());
				addr.addBean(item);
			}
		}

		//Add soap list
		   String[] list = new String[] {"SOAP", "REST"};

		   
		   ListBoxBean lb = event.getListBoxBean("webservicelist");
		   if (lb == null)
		   {
			   lb.clear();
			   
			   for (int i = 0; i < list.length; i++) {
			               ListBoxItemBean item = new ListBoxItemBean();
			               item.setValue(list[i]);
			               item.setName(list[i]);
			               lb.addBean(item);
			   }		
		   }
		/*
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
		*/
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
				TextFieldBean name = event.getTextFieldBean("name");
				name.setValue(gData.getName());
				ListBoxBean webservice = event.getListBoxBean("webservicelist"); //.getTextFieldBean("value");

				   String[] list = new String[] {"SOAP", "REST"};

				   for (int i = 0; i < list.length; i++) {
				               ListBoxItemBean item = new ListBoxItemBean();
				               item.setValue(list[i]);
				               item.setName(list[i]);
				               if (list[i].equals(gData.getWebservice()))
				            	   item.setSelected(true);
				               webservice.addBean(item);
				   }				
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
				
				//Get cellparams
			    Iterator it = gData.getParams().iterator();
				   ListBoxBean lb = event.getListBoxBean("registeredcellparamslist");
				   lb.clear();
				   int i=0;
				   
				 System.out.println("  My count " + it.hasNext()); 
			    while (it.hasNext()) {
			    	System.out.println("My parmas");
			        // Get element
			    	RegisteredCellParam element = (RegisteredCellParam) it.next();
			    	
	               ListBoxItemBean item = new ListBoxItemBean();
	               item.setName("count" + i);
	               item.setValue(element.getName() + " = " + element.getValue());
	               lb.addBean(item);
	               i++;
	               /*
					TextFieldBean realname = event.getTextFieldBean("RealName" + count);
					realname.setValue(element.getName());
					TextFieldBean realvalue = event.getTextFieldBean("RealValue" + count);
					realvalue.setValue(element.getValue());
					count++;
				   */
			    }			
				
			}
		}
		setNextState(request, "showList");
	}
		
	public void addRegisteredCell(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();
		/* */
		// get the values
		TextFieldBean edit = event.getTextFieldBean("edit");
		edit.setValue("false");
		
		TextFieldBean name = event.getTextFieldBean("name");
		ListBoxBean webservice = event.getListBoxBean("webservicelist"); //.getTextFieldBean("value");
		TextFieldBean id = event.getTextFieldBean("id");
		TextFieldBean value = event.getTextFieldBean("url");
		// Deal with Name/Value Pairs

		// construct the address
		RegisteredCell gParam = new RegisteredCell();
		gParam.setName(name.getValue());
		gParam.setUrl(value.getValue());
		gParam.setId(id.getValue());
		gParam.setWebservice(webservice.getSelectedValue());
		
		/* */
		int count = 0;
		Set cellP = new HashSet();
		//ArrayList cellP = new ArrayList();
		while (event.getTextFieldBean("RealName" + count).getValue() != null)
		{
			RegisteredCellParam cellParam = new RegisteredCellParam();
			cellParam.setName(event.getTextFieldBean("RealName" + count).getValue());
			cellParam.setValue(event.getTextFieldBean("RealValue" + count).getValue());
			//gParam.getParams().add(cellParam);
			cellP.add(cellParam);
			count++;
			if (count==100)
				break;
		}
		//gParam.setParams(cellP);
		// save the address
		globaldataservice.saveRegisteredCell(gParam, cellP);

		// show the mainpage again
		setNextState(request, "showList");
	}

	public void deleteRegisteredCell(ActionFormEvent event) {
		PortletRequest request = event.getActionRequest();

		TextFieldBean edit = event.getTextFieldBean("edit");
		edit.setValue("false");

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
