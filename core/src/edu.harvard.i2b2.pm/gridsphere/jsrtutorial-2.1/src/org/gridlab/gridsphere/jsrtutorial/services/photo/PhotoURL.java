/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: PhotoURL.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */
package org.gridlab.gridsphere.jsrtutorial.services.photo;

/**
 *
 */
public class PhotoURL {

    public String url = new String();
    public String desc = new String();

    public PhotoURL(String url, String desc) {
        this.url = url;
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}



