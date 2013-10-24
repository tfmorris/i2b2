package org.gridlab.gridsphere.jsrtutorial.portlets.address;

import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;
import org.gridlab.gridsphere.jsrtutorial.services.address.AddressService;
import org.gridlab.gridsphere.jsrtutorial.services.address.Address;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;


import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import java.util.List;

/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: AddressPortlet.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */

public class AddressPortlet extends ActionPortlet {

    private AddressService addressservice = null;
  // private edu.harvard.i2b2.pm.ws.DatabaseManagerService dbManagerService = null;
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        try {
//	 PortletServiceFactory factory = SportletServiceFactory.getInstance();
// addressservice = (AddressService) factory.createPortletService(AddressService.class, null, true);


            addressservice = (AddressService)createPortletService(AddressService.class);
	
//	    dbManagerService =  (edu.harvard.i2b2.pm.ws.DatabaseManagerService) createPortletService(edu.harvard.i2b2.pm.ws.DatabaseManagerService.class); //, null, true);
//dbManagerService.setCounter(110);

        } catch (PortletServiceException e) {
            System.err.println("Unable to initialize AddressService: ");
        }
        DEFAULT_VIEW_PAGE = "showList";
    }

    public void showList(RenderFormEvent event) {
        PortletRequest request = event.getRenderRequest();
//try {
//	PortletServiceFactory factory = SportletServiceFactory.getInstance();
//	edu.harvard.i2b2.pm.ws.DatabaseManagerService dbManagerService =  (edu.harvard.i2b2.pm.ws.DatabaseManagerService) factory.createPortletService(edu.harvard.i2b2.pm.ws.DatabaseManagerService.class, null, true);

//dbManagerService.setCounter(10);
//} catch (Exception e)
//{
//e.printStackTrace();
//System.out.println("ERROR inf actory");
//}
        List addresses = addressservice.getAddresses();

        ListBoxBean addr = event.getListBoxBean("addresslist");
        addr.clear();
        
        if (addresses!=null) {
            for (int i=0;i<addresses.size();i++) {
                Address address = (Address)addresses.get(i);
                ListBoxItemBean item = new ListBoxItemBean();
                item.setName(address.getOid());
                item.setValue(address.getLastname()+", "+address.getFirstname());
                addr.addBean(item);
            }
        }

        // let's sort it
        addr.sortByValue();

        setNextState(request, "address/view.jsp");
    }

    public void addAddress(ActionFormEvent event) {
        PortletRequest request = event.getActionRequest();

        // get the values
        TextFieldBean lastname = event.getTextFieldBean("lastname");
        TextFieldBean firstname = event.getTextFieldBean("firstname");

        // construct the address
        Address address = new Address();
        address.setFirstname(firstname.getValue());
        address.setLastname(lastname.getValue());

        // save the address
        addressservice.saveAddress(address);

        // show the mainpage again
        setNextState(request, "showList");
    }

    public void deleteAddress(ActionFormEvent event) {
        PortletRequest request = event.getActionRequest();

        // get the listbox
        ListBoxBean addresslist = event.getListBoxBean("addresslist");

        // get the oid
        String oid = addresslist.getSelectedName();
        if (oid!=null) {
            // now get the address by oid
            Address address = addressservice.getAddressByOid(oid);
            // ... and delete the oid
            addressservice.deleteAddress(address);
        }
        setNextState(request, "showList");
    }
}
