<%@ page import="org.gridlab.gridsphere.portlet.PortletGroup" %>
<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>

<portletAPI:init/>

<portlet:defineObjects/>

<style type="text/css">
.line {
	line-height:4px; 
	margin-bottom: 8px; 
	border-bottom: 1px dotted #585858;
	height: 5px;
}
</style>

<ui:form>

	<img src="/jsrtutorial/html/images/hive-small.gif"><B>Manage Environment Data</b>
	<div class="line">&nbsp;</div>
    <ui:text value="Project specific data which can be used by this project."/>
	<br />


    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="ID:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:text beanId="oid"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Name:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="name"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Wiki:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="wiki"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>
    <ui:actionsubmit action="addGroupData" value="New Group Data"/>

</ui:form>
