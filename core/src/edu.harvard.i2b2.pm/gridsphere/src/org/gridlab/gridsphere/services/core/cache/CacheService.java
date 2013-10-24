/*
 * @author <a href="mailto:kisg@mailbox.hu">Gergely Kis</a>
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: CacheService.java,v 1.1 2007/08/24 17:24:58 mem61 Exp $
 */
package org.gridlab.gridsphere.services.core.cache;



/**
 * Simple Cache service. Caches java objects with a String key and a timeout.
 */
public interface CacheService {

    public static final String NO_CACHE = "org.gridlab.gridsphere.services.core.cache.CacheService.NO_CACHE";

    public void cache(String key, Object object, long timeout);

    public void removeCached(String key);

    public Object getCached(String key);
}
