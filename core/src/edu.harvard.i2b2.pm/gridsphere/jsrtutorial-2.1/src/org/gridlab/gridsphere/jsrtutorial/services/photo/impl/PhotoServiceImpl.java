/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: PhotoServiceImpl.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */
package org.gridlab.gridsphere.jsrtutorial.services.photo.impl;

import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.jsrtutorial.services.photo.PhotoService;
import org.gridlab.gridsphere.jsrtutorial.services.photo.PhotoURL;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PhotoServiceImpl implements PortletServiceProvider, PhotoService {
    private List photolist = new ArrayList();

    public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
    }

    public void destroy() {
    }

    public void add(String url, String desc) {
        PhotoURL photo = new PhotoURL(url, desc);
        photolist.add(photo);
    }

    public List getPictures() {
        return photolist;
    }
}



