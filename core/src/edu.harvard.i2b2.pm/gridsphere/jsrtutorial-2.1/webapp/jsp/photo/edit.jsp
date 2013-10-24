<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<ui:form>
    <ui:text value="Enter data for new picture:"/>

    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Url"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="url"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Description"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="desc"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>
    <ui:actionsubmit action="addPicture" value="add Picture"/>
    <ui:actionsubmit action="showSelection" value="Cancel"/>
</ui:form>





