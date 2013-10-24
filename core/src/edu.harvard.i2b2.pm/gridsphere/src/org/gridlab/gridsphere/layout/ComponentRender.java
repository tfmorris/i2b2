/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: ComponentRender.java,v 1.1 2007/08/24 17:24:53 mem61 Exp $
 */
package org.gridlab.gridsphere.layout;

import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;

/**
 * The <code>ComponentRender</code> interface required by all PortletComponets to be displayed
 */
public interface ComponentRender extends Cloneable {

    /**
     * Renders the portlet component
     *
     * @param event a gridsphere event
     */
    public void doRender(GridSphereEvent event);

    public Object clone() throws CloneNotSupportedException;
}
