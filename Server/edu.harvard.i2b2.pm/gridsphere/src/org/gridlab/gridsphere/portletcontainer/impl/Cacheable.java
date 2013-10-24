/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: Cacheable.java,v 1.1 2007/08/24 17:24:58 mem61 Exp $
 */
package org.gridlab.gridsphere.portletcontainer.impl;

public class Cacheable {

    private boolean shared;
    private long seconds;

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean getShared() {
        return shared;
    }

    public void setExpiration(int seconds) {
        this.seconds = seconds;
    }

    public long getExpiration() {
        return seconds;
    }
}
