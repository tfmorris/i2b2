<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<ui:form>

    <ui:text value="AddressBook"/>

    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:listbox beanId="addresslist" size="5"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>

    <ui:actionsubmit action="deleteAddress" value="Delete Address"/>

    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Last Name:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="lastname"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="First Name:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="firstname"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>

    <ui:actionsubmit action="addAddress" value="New Address"/>
</ui:form>
