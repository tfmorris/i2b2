/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: PhotoService.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */
package org.gridlab.gridsphere.jsrtutorial.services.photo;

import org.gridlab.gridsphere.portlet.service.PortletService;

import java.util.List;


public interface PhotoService extends PortletService {

    public void add(String url, String desc);

    public List getPictures();

}
