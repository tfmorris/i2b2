package org.gridlab.gridsphere.jsrtutorial.portlets.globaldata;


import edu.harvard.i2b2.pm.services.GlobalData;
import edu.harvard.i2b2.pm.services.GlobalDataService;


/*
import org.gridlab.gridsphere.layout.PortletPage;
import org.gridlab.gridsphere.layout.PortletTab;
import org.gridlab.gridsphere.layout.PortletTabbedPane;
import org.gridlab.gridsphere.portlet.*;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.provider.event.FormEvent;
import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.*;
import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
import org.gridlab.gridsphere.services.core.user.UserManagerService;

import javax.servlet.UnavailableException;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


//import javax.portlet.PortletConfig;
//import javax.portlet.PortletException;
//import javax.portlet.PortletRequest;
import java.util.List;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 */


import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
import org.gridlab.gridsphere.layout.PortletPage;
import org.gridlab.gridsphere.layout.PortletTab;
import org.gridlab.gridsphere.layout.PortletTabbedPane;

import org.gridlab.gridsphere.portlet.*;
import edu.harvard.i2b2.pm.services.GlobalData;
import edu.harvard.i2b2.pm.services.GlobalDataService;

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

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;


//import org.gridlab.gridsphere.portlet.PortletRequest;

public class GlobalDataPortlet extends ActionPortlet {

	private edu.harvard.i2b2.pm.services.GlobalDataService globaldataservice = null;
	public void init(PortletConfig config) throws PortletException { // UnavailableException {
		super.init(config);
		try {
			PortletServiceFactory factory = SportletServiceFactory.getInstance();
			globaldataservice = (edu.harvard.i2b2.pm.services.GlobalDataService)factory.createPortletService(edu.harvard.i2b2.pm.services.GlobalDataService.class, null, true);

		} catch (Exception e) {
			System.err.println("Unable to initialize GlobalDataService: ");
		}
		DEFAULT_VIEW_PAGE = "showList";
	}




	public void showList(RenderFormEvent event) {
		PortletRequest request = event.getRenderRequest();
		List addresses = globaldataservice.getGlobalData();

		ListBoxBean addr = event.getListBoxBean("globaldatalist");
		addr.clear();

		if (addresses!=null) {
			for (int i=0;i<addresses.size();i++) {
				GlobalData globaldata = (GlobalData)addresses.get(i);
				ListBoxItemBean item = new ListBoxItemBean();
				item.setName(globaldata.getOid());
				item.setValue(globaldata.getName() + " = " + globaldata.getValue());
				addr.addBean(item);
			}
		}

		// let's sort it
		addr.sortByValue();

		setNextState( request, "globaldata/view.jsp");
	}

    public void addGlobalData(ActionFormEvent event) {
        PortletRequest request = event.getActionRequest();

        TextFieldBean name = event.getTextFieldBean("name");
        TextFieldBean value = event.getTextFieldBean("value");

        // construct the address
        GlobalData gParam = new GlobalData();
        gParam.setName(name.getValue());
        gParam.setValue(value.getValue());

        // save the address
        globaldataservice.saveGlobalData(gParam);

        // show the mainpage again
        setNextState(request, "showList");
    }

    public void deleteGlobalData(ActionFormEvent event) {
        PortletRequest request = event.getActionRequest(); //getActionRequest();

        // get the listbox
        ListBoxBean gDatalist = event.getListBoxBean("globaldatalist");

        // get the oid
        String oid = gDatalist.getSelectedName();
        if (oid!=null) {
            // now get the address by oid
        	GlobalData gData = globaldataservice.getGlobalDataByOid(oid);
            // ... and delete the oid
            if (gData != null)
	        	globaldataservice.deleteGlobalData(gData);
        }
        setNextState(request, "showList");
    }


}
