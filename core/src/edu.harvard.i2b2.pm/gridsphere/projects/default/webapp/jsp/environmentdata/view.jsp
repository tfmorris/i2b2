<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>


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

	<img src="/default/html/images/hive-small.gif"><B>Manage Environment Data</b>
	<div class="line">&nbsp;</div>
    <ui:text value="Environment specific data which can be used by this hive."/>
	<br />


    <ui:frame>
        <ui:tablerow>
	    <ui:tablecell valign="top">
	        <ui:text value="Environment:"/>
	    </ui:tablecell>
	    <ui:tablecell>
	        <ui:listbox beanId="environmentlist" size="1"/>
	     </ui:tablecell>
        </ui:tablerow>        
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Domain:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="domain"/>
            </ui:tablecell>
        </ui:tablerow>
        <ui:tablerow>
            <ui:tablecell>
                <ui:text value="Help URL:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="url"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>
    <ui:actionsubmit action="addEnvironmentData" value="Save Environment Data"/>

</ui:form>
