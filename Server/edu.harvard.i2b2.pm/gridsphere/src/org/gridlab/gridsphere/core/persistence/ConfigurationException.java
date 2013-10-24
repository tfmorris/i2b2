/*
 * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
 * @team sonicteam
 * @version $Id: ConfigurationException.java,v 1.1 2007/08/24 17:24:54 mem61 Exp $
 *
 * Is thrown when the needed settings for create/restore/update/delete such as connectionURL
 * are not set
 */

package org.gridlab.gridsphere.core.persistence;

public class ConfigurationException extends PersistenceManagerException {

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String msg) {
        super(msg);
    }

}

