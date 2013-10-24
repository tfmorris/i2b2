/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: TagBean.java,v 1.1 2007/08/24 17:24:49 mem61 Exp $
 */

package org.gridlab.gridsphere.provider.portletui.beans;

import java.util.Locale;

/**
 * The <code>TagBean</code> interface describes the base methods provided by all visual ui beans
 */
public interface TagBean {

    /**
     * Returns the bean identifier
     *
     * @return the bean identifier
     */
    public String getBeanId();

    public void addParam(String name, String value);

    public void removeParam(String name);

    /**
     * Sets the bean identifier
     *
     * @param beanId the bean identifier
     */
    public void setBeanId(String beanId);

    public void setLocale(Locale locale);

    /**
     * Returns the HTML representation of the bean
     *
     * @return html string representing the object
     */
    public String toStartString();

    /**
     * Returns the HTML representation of the bean
     *
     * @return html string representing the object
     */
    public String toEndString();

    /**
     * Stores this bean in the portlet request
     */
    //public void store();

}
