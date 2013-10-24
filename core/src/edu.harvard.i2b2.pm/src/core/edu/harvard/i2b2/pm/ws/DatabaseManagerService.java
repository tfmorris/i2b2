package edu.harvard.i2b2.pm.ws;

import org.gridlab.gridsphere.portlet.service.PortletService;



public interface DatabaseManagerService extends PortletService {

    java.util.List 	restoreList(java.lang.String query) throws Exception;
    
    int  getCounter();
    void setCounter(int i);
    
    void connect(String path);
}


