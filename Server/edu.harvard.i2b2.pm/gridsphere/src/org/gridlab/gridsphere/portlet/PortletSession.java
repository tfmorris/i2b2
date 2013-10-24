/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: PortletSession.java,v 1.1 2007/08/24 17:24:51 mem61 Exp $
 */
package org.gridlab.gridsphere.portlet;

import javax.servlet.http.HttpSession;


/**
 * The <code>PortletSession</code> holds the user-specific data that the portlet
 * needs to personalize the one global portlet instance. Together with the
 * portlet, the portlet session constitutes the concrete portlet instance.
 */
public interface PortletSession extends HttpSession {

}
