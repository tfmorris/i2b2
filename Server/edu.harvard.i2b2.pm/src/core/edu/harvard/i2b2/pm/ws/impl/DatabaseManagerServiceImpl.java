package edu.harvard.i2b2.pm.ws.impl;

import java.util.List;

import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;

import edu.harvard.i2b2.pm.ws.DatabaseManagerService;

public class DatabaseManagerServiceImpl implements PortletServiceProvider, DatabaseManagerService {


	private static PortletLog log = SportletLog.getInstance(DatabaseManagerServiceImpl.class);
	private static PersistenceManagerRdbms pm = null;

	private static int count = 0;
	public void destroy() {
		log.debug("Calling destroy()");
	}

	public void init(PortletServiceConfig arg0) throws PortletServiceUnavailableException {
	}

	public List restoreList(String query) throws Exception {
		if (pm == null)
			throw new Exception ("Attempt to call restoreList without connecting to a database");
		return pm.restoreList(query);
	}

	public void connect(String path) {
		pm = PersistenceManagerFactory.createPersistenceManagerRdbms(path); 
	}

	public int getCounter() {
		// TODO Auto-generated method stub
		return count;
	}

	public void setCounter(int i) {
		// TODO Auto-generated method stub
		count = i;
	}


}