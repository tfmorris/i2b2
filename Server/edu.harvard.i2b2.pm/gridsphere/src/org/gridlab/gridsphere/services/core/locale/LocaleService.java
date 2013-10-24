package org.gridlab.gridsphere.services.core.locale;

import org.gridlab.gridsphere.portlet.service.PortletService;

import java.util.Locale;

/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: LocaleService.java,v 1.1 2007/08/24 17:24:52 mem61 Exp $
 */
public interface LocaleService extends PortletService {

    public Locale[] getSupportedLocales();

}
