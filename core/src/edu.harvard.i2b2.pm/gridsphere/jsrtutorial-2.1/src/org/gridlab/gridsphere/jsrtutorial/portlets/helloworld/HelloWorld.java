/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: HelloWorld.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */
package org.gridlab.gridsphere.jsrtutorial.portlets.helloworld;


import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * a simple HelloWorld Portlet
 */
public class HelloWorld extends GenericPortlet {

    public void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h1>Hello World</h1>");
    }

}



