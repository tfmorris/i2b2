/*
 * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: PhotoPortlet.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */

package org.gridlab.gridsphere.jsrtutorial.portlets.photo;

import org.gridlab.gridsphere.jsrtutorial.services.photo.PhotoService;
import org.gridlab.gridsphere.jsrtutorial.services.photo.PhotoURL;
import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
import org.gridlab.gridsphere.provider.event.jsr.FormEvent;
import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
import org.gridlab.gridsphere.provider.portlet.jsr.ActionPortlet;
import org.gridlab.gridsphere.provider.portletui.beans.ImageBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxBean;
import org.gridlab.gridsphere.provider.portletui.beans.ListBoxItemBean;
import org.gridlab.gridsphere.provider.portletui.beans.TextFieldBean;
import org.gridlab.gridsphere.portlet.service.PortletServiceException;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoPortlet extends ActionPortlet {

    private PhotoService photoService = null;
    private List list = new ArrayList();

    private String pic1 = "/jsrtutorial/html/images/empirestate.jpg";
    private String pic2 = "/jsrtutorial/html/images/tokyo.jpg";
    private String pic3 = "/jsrtutorial/html/images/vienna.jpg";
    private String desc1 = "New York";
    private String desc2 = "Tokyo";
    private String desc3 = "Vienna";


    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        // DEFAULT_EDIT_PAGE = "photo/edit.jsp";
        DEFAULT_VIEW_PAGE = "displaySelection";

        // uncomment this for using a service
         /*
          try {
              photoService = (PhotoService) this.createPortletService(PhotoService.class);
          } catch (PortletServiceException e) {
              log.error("Unable to initialize PhotoService", e);
          }

          photoService.add(pic1, desc1);
          photoService.add(pic2, desc2);
          photoService.add(pic3, desc3);
         */

        list.add(new PhotoURL(pic1, desc1));
        list.add(new PhotoURL(pic2, desc2));
        list.add(new PhotoURL(pic3, desc3));
    }

    private ListBoxItemBean makeItem(String name, String value) {
        ListBoxItemBean item = new ListBoxItemBean();
        item.setName(value);
        item.setValue(name);
        return item;
    }

    public void displaySelection(RenderFormEvent event) throws PortletException {
        RenderRequest request = event.getRenderRequest();
        System.err.println("context path= " + request.getContextPath());
        setListBox(event);

        setNextState(request, "photo/view.jsp");
    }

    public void showSelection(ActionFormEvent event) throws IOException, PortletException {
        setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
    }

    protected void setListBox(FormEvent event) { 
        ListBoxBean lb = event.getListBoxBean("photolist");
        lb.setSize(1);
        lb.setMultipleSelection(false);
        // uncomment this for using a service

        // List list = photoService.getPictures();

        for (int i = 0; i < list.size(); i++) {
            PhotoURL photo = (PhotoURL) list.get(i);
            lb.addBean(makeItem(photo.getDesc(), photo.getUrl()));
        }
    }

    public void showPicture(ActionFormEvent event) throws PortletException {
        ActionRequest request = event.getActionRequest();

        ListBoxBean lb = event.getListBoxBean("photolist");
        String url = lb.getSelectedValue();

        ImageBean image = event.getImageBean("urlphoto");
        image.setSrc(url);

        setNextState(request, "photo/picture.jsp");
    }

    // uncomment this for using the editmode

     public void addPicture(ActionFormEvent event) throws PortletException {
         ActionRequest request = event.getActionRequest();
         ActionResponse response = event.getActionResponse();
         TextFieldBean url = event.getTextFieldBean("url");
         TextFieldBean desc = event.getTextFieldBean("desc");
         photoService.add(url.getValue(), desc.getValue());
         response.setPortletMode(PortletMode.VIEW);
         setNextState(request, "displaySelection");
     }

}
