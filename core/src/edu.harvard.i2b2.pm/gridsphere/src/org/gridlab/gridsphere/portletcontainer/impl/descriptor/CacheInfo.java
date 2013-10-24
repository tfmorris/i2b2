/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: CacheInfo.java,v 1.1 2007/08/24 17:24:50 mem61 Exp $
 */
package org.gridlab.gridsphere.portletcontainer.impl.descriptor;

public class CacheInfo {

    // default is to not cache
    private long expires = 0;
    private boolean shared = false;

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean getShared() {
        return shared;
    }
}
