<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<p>

<ui:form>
    Hello, <ui:text beanId="nameTB"/> !
    <ui:textfield size="20" beanId="nameTF"/>
    <ui:actionsubmit action="showName" value="Say Hello!"/>
</ui:form>