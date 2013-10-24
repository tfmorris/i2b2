<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<ui:form>
    <ui:text value="Please select a picture:"/>

    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:listbox beanId="photolist"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>
    <ui:actionsubmit action="showPicture" value="Show Picture"/>
</ui:form>

