package edu.harvard.i2b2.pm.services;

import org.gridlab.gridsphere.portlet.service.PortletService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface GlobalDataService extends   PortletService {

    void deleteGlobalData(GlobalData gParam);
    void saveGlobalData(GlobalData gParam);
    GlobalData getGlobalDataByOid(String oid);
    List getGlobalData();
    
    void deleteRegisteredCell(RegisteredCell gParam);
    void saveRegisteredCell(RegisteredCell gParam, Set cellparms);
    RegisteredCell getRegisteredCellByOid(String oid);
    List getRegisteredCell();
    
    void deleteVariableData(VariableData gParam);
    void saveVariableData(VariableData gParam);
    VariableData getVariableDataByOid(String oid);
    List getVariableData(String project);

    void deleteUserData(UserData gParam);
    void saveUserData(UserData gParam);
    UserData getUserDataByOid(String oid);
    List getUserData(String project);
    List getUserData(String project, String user);

    
    void deleteGroupData(GroupData gParam);
    void saveGroupData(GroupData gParam);
    GroupData getGroupDataByOid(String oid);
    List getGroupData();
    
    void deleteEnvironmentData(EnvironmentData gParam);
    void saveEnvironmentData(EnvironmentData gParam);
    EnvironmentData getEnvironmentData();
    //ist getEnvironmentData();    
    
    void deleteRoleData(RoleData gParam);
    void saveRoleData(RoleData gParam);
    RoleData getRoleDataByOid(String oid);
    List getRoleData(String project);
    List getRoleData(String project, String user);
    
        
}
