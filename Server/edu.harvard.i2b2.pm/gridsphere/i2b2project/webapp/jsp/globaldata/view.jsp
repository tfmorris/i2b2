<%@ page import="org.gridlab.gridsphere.portlet.PortletGroup" %>
<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>

<portletAPI:init/>

<portlet:defineObjects/>

<script type="text/javascript">
function ShowHide1(szDivID, iState){

    if(document.layers)	   //NN4+    {       document.layers[szDivID].visibility = iState ? "show" : "hide";    }    else if(document.getElementById)	  //gecko(NN6) + IE 5+    {        var obj = document.getElementById(szDivID);        obj.style.visibility = iState ? "visible" : "hidden";    }    else if(document.all)	// IE 4    {        document.all[szDivID].style.visibility = iState ? "visible" : "hidden";    }}
</script>

<ui:form>

 <ui:text value="Data which can be used by any this project."/>
	<div id="top_new" style=" visibility:visible; name="top_new">
	   <ui:frame>
     	   <ui:tablerow>
       	       <ui:tablecell>
<input type=button value="Add New Parameter Data" onclick="ShowHide1('addnew', 1)">
               </ui:tablecell>
	   </ui:tablerow>
	   <ui:tablerow>
              <ui:tablecell>
                <ui:listbox beanId="parameterdatalist" size="1" >
		    <ui:listboxitem selected="true" value="Select or Create New"/>		</ui:listbox>
             </ui:tablecell>
       	       <ui:tablecell>
		 <ui:actionsubmit action="deleteParameterData" value="Delete"/>
               </ui:tablecell>
	   </ui:tablerow>
	</ui:frame>
	</div>


	<div id="addnew" style=" visibility:hidden; name="addnew">

    <ui:frame>
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
                <ui:text value="Value:"/>
            </ui:tablecell>
            <ui:tablecell>
                <ui:textfield beanId="value"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>

    <ui:actionsubmit action="addParameterData" value="OK"/>
    <input type=button value="Cancel" onclick="ShowHide1('addnew', 0)">
</div>
<%-- OLD 
    <hr>
    <ui:text value="Parameter Data current in database"/>

    <ui:frame>
        <ui:tablerow>
            <ui:tablecell>
                <ui:listbox beanId="parameterdatalist" size="5"/>
            </ui:tablecell>
        </ui:tablerow>
    </ui:frame>

    <ui:actionsubmit action="deleteParameterData" value="Delete Parameter Data"/>
--%>
</ui:form>
