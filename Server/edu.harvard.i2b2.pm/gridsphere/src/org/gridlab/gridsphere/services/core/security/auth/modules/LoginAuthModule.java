package org.gridlab.gridsphere.services.core.security.auth.modules;

import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.services.core.security.auth.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Locale;

/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: LoginAuthModule.java,v 1.1 2007/08/24 17:24:58 mem61 Exp $
 */
public interface LoginAuthModule extends Comparable {

    public void setHttpServletRequest(HttpServletRequest request);

    public void setAttributes(Map attributes);

    public String getAttribute(String name);

    public Map getAttributes();

    public String getModuleName();

    public String getModuleDescription(Locale locale);

    public int getModulePriority();

    public boolean isModuleActive();

    public void setModulePriority(int priority);

    public void setModuleActive(boolean isActive);

    public String getModuleError(String key, Locale locale);

    public abstract void checkAuthentication(User user, String password) throws AuthenticationException;

}
