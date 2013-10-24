/*
 * @author <a href="mailto:kisg@mailbox.hu">Gergely Kis</a>
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: PortalConfigService.java,v 1.1 2007/08/24 17:24:58 mem61 Exp $
 */
package org.gridlab.gridsphere.services.core.portal;



/**
 * Portal configuration service is used to manage portal administrative settings
 */
public interface PortalConfigService {

    public void savePortalConfigSettings(PortalConfigSettings configSettings);

    public PortalConfigSettings getPortalConfigSettings();

}
