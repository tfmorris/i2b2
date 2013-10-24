/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: SportletCollection.java,v 1.1 2007/08/24 17:24:50 mem61 Exp $
 */
package org.gridlab.gridsphere.portletcontainer.impl.descriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>SportletCollection</code> is a container for
 * a collection of <code>SportletDefinition</code>s.
 */
public class SportletCollection {

    private List portletDefList = new ArrayList();

    /**
     * Constructs an instance of SportletCollection
     */
    public SportletCollection() {
    }

    /**
     * Returns the list of portlet definitions
     *
     * @return a list of portlet definitions
     */
    public List getPortletDefinitionList() {
        return portletDefList;
    }

    /**
     * Sets the list of portlet definitions
     *
     * @param portletDefList an <code>ArrayList</code> containing the portlet definitions
     */
    public void setPortletDefinitionList(ArrayList portletDefList) {
        this.portletDefList = portletDefList;
    }

}

