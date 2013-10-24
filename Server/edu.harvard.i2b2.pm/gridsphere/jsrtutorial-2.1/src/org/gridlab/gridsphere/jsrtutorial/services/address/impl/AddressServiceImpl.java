package org.gridlab.gridsphere.jsrtutorial.services.address.impl;

import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.jsrtutorial.services.address.AddressService;
import org.gridlab.gridsphere.jsrtutorial.services.address.Address;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;

import java.util.List;

/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: AddressServiceImpl.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */

public class AddressServiceImpl implements PortletServiceProvider, AddressService {

    // logging
    private static PortletLog log = SportletLog.getInstance(AddressServiceImpl.class);

    // our persistencemanager
    private PersistenceManagerRdbms pm = null;


    /**
     * Init the service
     * @param config
     * @throws PortletServiceUnavailableException
     */
    public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
        // create the persistencemanager
        this.pm = PersistenceManagerFactory.createPersistenceManagerRdbms("jsrtutorial");
    }

    /**
     * Destroy the service and free ressources.
     */
    public void destroy() {
        try {
            pm.destroy();
        } catch (PersistenceManagerException e) {
            log.info("Problems shutting down AddressService.");
        }
    }

    private List queryDB(String condition) {
        List result = null;
        try {
            // try to get the address
            result = pm.restoreList("from " + Address.class.getName() + " as address "+condition);
        } catch (PersistenceManagerException e) {
            log.error("Could not retrieve address(es) :"+e);
        }

        return result;

    }


    public List getAddressByLastName(String lastname) {
        return queryDB("where address.lastname='" + lastname + "'");
    }

    public void deleteAddress(Address address) {
        try {
            pm.delete(address);
        } catch (PersistenceManagerException e) {
            log.error("Error deleting address object."+e);
        }
    }

    public void saveAddress(Address address) {
        try {
            // if we already have an oid the it is an existing object
            if (address.getOid() != null) {
                pm.update(address);
            } else {
                // otherwise create a  new object
                pm.create(address);
            }
        } catch (PersistenceManagerException e) {
            log.error("Error creating/updating the address object!"+e);
        }
    }

    public Address getAddressByOid(String oid) {
        List result = queryDB("where address.oid='" + oid + "'");
        if (result!=null) {
            return (Address)result.get(0);
        } else {
            return null;
        }
    }

    public List getAddresses() {
        return queryDB("");
    }
}
