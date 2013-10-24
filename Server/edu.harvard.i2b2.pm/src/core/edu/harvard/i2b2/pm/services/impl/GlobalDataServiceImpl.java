package edu.harvard.i2b2.pm.services.impl;


import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;

import edu.harvard.i2b2.pm.services.EnvironmentData;
import edu.harvard.i2b2.pm.services.GlobalData;
import edu.harvard.i2b2.pm.services.GlobalDataService;
import edu.harvard.i2b2.pm.services.RegisteredCellParam;
import edu.harvard.i2b2.pm.services.UserData;
import edu.harvard.i2b2.pm.services.VariableData;
import edu.harvard.i2b2.pm.services.GroupData;
import edu.harvard.i2b2.pm.services.RegisteredCell;
import edu.harvard.i2b2.pm.services.RoleData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GlobalDataServiceImpl implements PortletServiceProvider, GlobalDataService {

	// logging
	private static PortletLog log = SportletLog.getInstance(GlobalDataServiceImpl.class);

	// our persistencemanager
	private PersistenceManagerRdbms pm = null;


	/**
	 * Init the service
	 * @param config
	 * @throws PortletServiceUnavailableException
	 */
	public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
		// create the persistencemanager
		this.pm = PersistenceManagerFactory.createPersistenceManagerRdbms("default");
	}

	/**
	 * Destroy the service and free ressources.
	 */
	public void destroy() {
		try {
			pm.destroy();
		} catch (PersistenceManagerException e) {
			log.debug("Problems shutting down GlobalParamService.");
		}
	}

	private List queryDB(String name, String condition) {
		List result = null;
		try {
			// try to get the address
			///GlobalData.class.getName() 
			result = pm.restoreList("from " + name + " as address "+condition);
			// result = pm.restoreList("from Address as address "+condition);
			if (result.size() == 0)
				result = null;
		} catch (PersistenceManagerException e) {
			log.error("Could not retrieve address(es) :"+e);
		}

		return result;

	}

	public void deleteGlobalData(GlobalData address) {
		try {
			pm.delete(address);
		} catch (PersistenceManagerException e) {
			log.error("Error deleting global param object."+e);
		}
	}

	public void saveGlobalData(GlobalData address) {
		try {
			// if we already have an oid the it is an existing object
			if (address.getOid() != null) {
				pm.update(address);
			} else {
				// otherwise create a  new object
				pm.create(address);
			}
		} catch (PersistenceManagerException e) {
			log.error("Error creating/updating the global param object!"+e);
		}
	}

	public GlobalData getGlobalDataByOid(String oid) {
		List result = queryDB(GlobalData.class.getName(), "where gsoid='" + oid + "'");
		if (result!=null) {
			return (GlobalData)result.get(0);
		} else {
			return null;
		}
	}

	public List getGlobalData() {
		return queryDB(GlobalData.class.getName(), "");
	}

	public void deleteRegisteredCell(RegisteredCell gParam) {
		try {
			//Delete all the params first
	    	List params = queryDB(RegisteredCellParam.class.getName(), "where cellid = '" + gParam.getId() + "'");

			if (params!=null) {
				for (int i=0;i<params.size();i++) {
					RegisteredCellParam globaldata = (RegisteredCellParam)params.get(i);
					pm.delete(globaldata);
				}
			}
			
			//delete the cell
			pm.delete(gParam);
			
		} catch (PersistenceManagerException e) {
			log.error("Error deleting global param object."+e);
		}		
	}

	public List getRegisteredCell() {
		List lst =  queryDB(RegisteredCell.class.getName(), "");
		if (lst != null)
		{
			List withParams = new ArrayList();
		    Iterator it = lst.iterator();
		    while (it.hasNext()) {
		        // Get element
		    	RegisteredCell element = (RegisteredCell) it.next();
		    	List params = queryDB(RegisteredCellParam.class.getName(), "where cellid = '" + element.getId() + "'");
		    	if (params != null) {
		    		element.getParams().addAll(params);
		    	}
	    		withParams.add(element);
		    }
			return withParams;
		}
		else
		{
			return lst;
		}
	}

	public RegisteredCell getRegisteredCellByOid(String oid) {
		List result = queryDB(RegisteredCell.class.getName(), "where id='" + oid + "'");
		if (result!=null) {
			RegisteredCell regCell = (RegisteredCell)result.get(0);
	    	List params = queryDB(RegisteredCellParam.class.getName(), "where cellid = '" + regCell.getId() + "'");
	    	if (params != null)
	    		regCell.getParams().addAll(params);

	    	return regCell;
			
		} else {
			return null;
		}
	}

	public void saveRegisteredCell(RegisteredCell regCell, Set regCellParams) {
		try {
			deleteRegisteredCell(regCell);
			pm.create(regCell);
			pm.commitTransaction();

			if (regCellParams != null) 
			{
			    Iterator it = regCellParams.iterator();
			    while (it.hasNext()) {
			        // Get element
			    	RegisteredCellParam element = (RegisteredCellParam) it.next();
			    	element.setCellid(regCell.getId());

			    	pm.create(element);
			    }
			}
			
		} catch (PersistenceManagerException e2) {

				log.error("Error creating/updating the project data object!"+e2);
		}

	}



	public void deleteVariableData(VariableData gParam) {
		try {
			pm.delete(gParam);
		} catch (PersistenceManagerException e) {
			log.error("Error deleting global param object."+e);
		}		
	}

	public List getVariableData(String project) {
		return queryDB(VariableData.class.getName(),  "where project='" + project + "'");
	}

	public VariableData getVariableDataByOid(String oid) {
		List result = queryDB(VariableData.class.getName(), "where gsoid='" + oid + "'");
		if (result!=null) {
			return (VariableData)result.get(0);
		} else {
			return null;
		}
	}

	public void saveVariableData(VariableData gParam) {
		try {
			// if we already have an oid the it is an existing object
			if (gParam.getOid() != null) {
				pm.update(gParam);
			} else {
				// otherwise create a  new object
				pm.create(gParam);
			}
		} catch (PersistenceManagerException e) {
			log.error("Error creating/updating the param cell object!"+e);
		}
	}










	public void deleteUserData(UserData gParam) {
		try {
			pm.delete(gParam);
		} catch (PersistenceManagerException e) {
			log.error("Error deleting global param object."+e);
		}		
	}

	public List getUserData(String project) {
		return queryDB(UserData.class.getName(),  "where project='" + project + "'");
	}

	public List getUserData(String project, String user) {
		return queryDB(UserData.class.getName(),  "where project='" + project + "' and user='" + user + "'");
	}
	
	public UserData getUserDataByOid(String oid) {
		List result = queryDB(UserData.class.getName(), "where gsoid='" + oid + "'");
		if (result!=null) {
			return (UserData)result.get(0);
		} else {
			return null;
		}
	}

	public void saveUserData(UserData gParam) {
		try {
			// if we already have an oid the it is an existing object
			if (gParam.getOid() != null) {
				pm.update(gParam);
			} else {
				// otherwise create a  new object
				pm.create(gParam);
			}
		} catch (PersistenceManagerException e) {
			log.error("Error creating/updating the param cell object!"+e);
		}
	}



	public void deleteGroupData(GroupData gParam) {
		try {
			pm.delete(gParam);
		} catch (PersistenceManagerException e) {
			log.error("Error deleting global param object."+e);
		}		
	}

	public List getGroupData() {
		return queryDB(GroupData.class.getName(), "");
	}

	public GroupData getGroupDataByOid(String oid) {
		List result = queryDB(GroupData.class.getName(), "where gsoid='" + oid + "'");
		if (result!=null) {
			return (GroupData)result.get(0);
		} else {
			return null;
		}
	}

	public void saveGroupData(GroupData gParam) {
		try {
			pm.create(gParam);
		} catch (PersistenceManagerException e) {
			try {
				pm.update(gParam);
			} catch (PersistenceManagerException e2) {
				log.error("Error creating/updating the project data object!"+e2);
			}
			log.error("Error creating/updating the project data object!"+e);

		}
	}

	public void deleteRoleData(RoleData gParam) {
		try {
			pm.delete(gParam);
		} catch (PersistenceManagerException e) {
			log.error("Error deleting security object."+e);
		}		
	}

	public List getRoleData(String project) {
		return queryDB(RoleData.class.getName(),  "where project='" + project + "'");
	}

	public List getRoleData(String project, String user) {
		return queryDB(RoleData.class.getName(),  "where project='" + project + "' and user='" + user + "'");
	}
	
	public RoleData getRoleDataByOid(String oid) {
		List result = queryDB(RoleData.class.getName(), "where gsoid='" + oid + "'");
		if (result!=null) {
			return (RoleData)result.get(0);
		} else {
			return null;
		}
	}

	public void saveRoleData(RoleData gParam) {
		try {
			// if we already have an oid the it is an existing object
			if (gParam.getOid() != null) {
				pm.update(gParam);
			} else {
				// otherwise create a  new object
				pm.create(gParam);
			}
		} catch (PersistenceManagerException e) {
			log.error("Error creating/updating the role object!"+e);
		}
	}



	public void deleteEnvironmentData(EnvironmentData gParam) {
		try {
			pm.delete(gParam);
		} catch (PersistenceManagerException e) {
			log.error("Error deleting global param object."+e);
		}		
	}

	//public List getEnvironmentData() {
	//	return queryDB(EnvironmentData.class.getName(), "");
	//}

	public EnvironmentData getEnvironmentData() {
		
		List result = queryDB(EnvironmentData.class.getName(), "where gsoid='hive'");
		if (result!=null) {
			return (EnvironmentData)result.get(0);
		} else {
			return null;
		}
	}

	public void saveEnvironmentData(EnvironmentData gParam) {
		try {
			pm.create(gParam);
		} catch (PersistenceManagerException e) {
			try {
				pm.update(gParam);
			} catch (PersistenceManagerException e2) {
				log.error("Error creating/updating the project data object!"+e2);
			}
			log.error("Error creating/updating the project data object!"+e);
		}
	}
}
