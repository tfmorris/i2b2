package org.gridlab.gridsphere.jsrtutorial.portlets.helloworld;

import org.gridlab.gridsphere.provider.portletui.beans.CheckBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextBean;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;

import javax.servlet.UnavailableException;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;

/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: UiHelloWorld.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */

public class UiHelloWorld extends ActionPortlet {

    private static final String DISPLAY_PAGE = "helloworld/uihelloworld.jsp";

    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        DEFAULT_VIEW_PAGE = "prepare";
    }

    public void showName(ActionFormEvent event) throws PortletException {
        TextFieldBean name = event.getTextFieldBean("nameTF");
        event.getActionResponse().setRenderParameter("helloname", name.getValue());
        /*CheckBoxBean bold = event.getCheckBoxBean("bold");
        if (bold.isSelected()) {
	    TextBean helloname = event.getTextBean("nameTB");
            helloname.setStyle(TextBean.MSG_BOLD);
        } */
        setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
    }

    public void prepare(RenderFormEvent event) throws PortletException {
        String name = event.getRenderRequest().getParameter("helloname");
        TextBean helloname = event.getTextBean("nameTB");
        if (name == null) {
            helloname.setValue("unknown");
        } else {
            helloname.setValue(name);
        }
        setNextState(event.getRenderRequest(), DISPLAY_PAGE);
    }
}
