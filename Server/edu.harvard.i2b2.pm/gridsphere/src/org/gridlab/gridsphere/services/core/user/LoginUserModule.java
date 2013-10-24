/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: LoginUserModule.java,v 1.1 2007/08/24 17:24:53 mem61 Exp $
 */
package org.gridlab.gridsphere.services.core.user;

import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.service.PortletService;

public interface LoginUserModule extends PortletService {

    public User getLoggedInUser(String loginName);

}
