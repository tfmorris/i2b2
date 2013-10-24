/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: AuthModuleCollection.java,v 1.1 2007/08/24 17:24:51 mem61 Exp $
 */
package org.gridlab.gridsphere.services.core.security.auth.modules.impl.descriptor;

import java.util.List;
import java.util.Vector;

/**
 * The <code>AuthModuleCollection</code> provides a list of
 * <code>AuthModuleDefinition</code> entries.
 */
public class AuthModuleCollection {

    private List modulesList = new Vector();

    /**
     * Sets the list of auth module definitions
     *
     * @param modulesList a <code>Vector</code> containing
     *                     auth module definitions
     * @see AuthModuleDefinition
     */
    public void setAuthModulesList(List modulesList) {
        this.modulesList = modulesList;
    }

    /**
     * Returns the list of auth module definitions
     *
     * @return a list containing the auth module definitions
     * @see AuthModuleDefinition
     */
    public List getAuthModulesList() {
        return modulesList;
    }

}
