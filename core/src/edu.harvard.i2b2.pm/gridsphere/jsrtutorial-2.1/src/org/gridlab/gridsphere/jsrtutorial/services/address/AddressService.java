package org.gridlab.gridsphere.jsrtutorial.services.address;

import org.gridlab.gridsphere.portlet.service.PortletService;
import org.gridlab.gridsphere.jsrtutorial.services.address.Address;

import java.util.List;

/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: AddressService.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */

public interface AddressService extends PortletService {

    /**
     * Returns the a list of the address matching the request
     * @param lastname
     * @return list of address with lastname
     */
    List getAddressByLastName(String lastname);

    /**
     * Delete the address
     * @param address address to be deleted
     */
    void deleteAddress(Address address);

    /**
     * Saves the given address.
     * @param address address to be saved
     */
    void saveAddress(Address address);


    /**
     * Returns the address with the given oid or null if not exists
     * @param oid oid of the address object
     * @return addressobject
     */
    Address getAddressByOid(String oid);

    /**
     * Returns all addresses.
     * @return  List of all addresses.
     */
    List getAddresses();
}
