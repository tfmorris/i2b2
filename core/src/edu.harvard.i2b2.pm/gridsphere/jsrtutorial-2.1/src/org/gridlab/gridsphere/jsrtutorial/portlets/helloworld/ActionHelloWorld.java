/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: ActionHelloWorld.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */
package org.gridlab.gridsphere.jsrtutorial.portlets.helloworld;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A classic helloWorld portlet with some action
 *
 */
public class ActionHelloWorld extends GenericPortlet {

    public void processAction(ActionRequest req, ActionResponse res) throws PortletException, IOException {

        String name = req.getParameter("name");

        // must be passed into render parameters
        if (name != null) res.setRenderParameter("name", name);

    }

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        // retrieve the name
        String name = request.getParameter("name");
        if (name != null) request.setAttribute("name", name);

        // create action url
        PortletURL url = response.createActionURL();
        request.setAttribute("url", url.toString());

        // create action url
        PortletURL rurl = response.createRenderURL();
        rurl.setPortletMode(PortletMode.EDIT);
        request.setAttribute("rurl", rurl.toString());

        getPortletConfig().getPortletContext().getRequestDispatcher("/jsp/helloworld/actionhello.jsp").include(request, response);
    }

}
 


