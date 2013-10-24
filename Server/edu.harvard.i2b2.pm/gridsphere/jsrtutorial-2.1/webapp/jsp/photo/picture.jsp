<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<ui:form>
    <ui:text value="Picture selected is:"/>

    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:image beanId="urlphoto"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>
    <ui:actionsubmit action="showSelection" value="Show Selection"/>
</ui:form>




