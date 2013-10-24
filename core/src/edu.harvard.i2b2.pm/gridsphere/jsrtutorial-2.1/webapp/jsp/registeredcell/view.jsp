<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>


<style type="text/css">
#param_new {
	width: 500px;
	float:left;
	visibility:hidden; 
	border-left:1px solid #ccc;
	margin-left:10px;
	padding-left:10px;
}
#param_new3 {
	padding-top: 6px;
	visibility:hidden; 
}
.line {
	line-height:4px; 
	margin-bottom: 8px; 
	border-bottom: 1px dotted #585858;
	height: 5px;
}
.missing {
	font-clolor: #e83e00;
}

select {
	width: 300px;
}
</style>



<script type="text/javascript">

function enableDiv() {

overl3 = document.forms[0].ui_lb_registeredcelllist_;
overl3.style.color = 'black';
overl3.disabled = false;
overl3 = document.getElementById('delete');
overl3.style.color = 'black';
overl3.disabled = false;
overl3 = document.getElementById('add');
overl3.style.color = 'black';
overl3.disabled = false;
overl3 = document.getElementById('edit');
overl3.style.color = 'black';
overl3.disabled = false;
		overl3 = document.forms[0].ui_tf_id_;
		overl3.style.color = 'black';
		overl3.disabled = false;
		document.forms[0].ui_tf_id_.value = '';
		document.forms[0].ui_tf_name_.value = '';
		document.forms[0].ui_tf_url_.value = '';

}

function disableDiv(elm) {

overl3 = document.forms[0].ui_lb_registeredcelllist_;
overl3.style.color = 'gray';
overl3.disabled = true;
overl3 = document.getElementById('delete');
overl3.style.color = 'gray';
overl3.disabled = true;
overl3 = document.getElementById('add');
overl3.style.color = 'gray';
overl3.disabled = true;
overl3 = document.getElementById('edit');
overl3.style.color = 'gray';
overl3.disabled = true;

}

 

function ShowHide2(szDivID, iState)
{

    if(document.layers)	   //NN4+
    {
       document.layers[szDivID].visibility = iState ? "show" : "hide";
    }
    else if(document.getElementById)	  //gecko(NN6) + IE 5+
    {
        var obj = document.getElementById(szDivID);
        obj.style.visibility = iState ? "visible" : "hidden";
    }
    else if(document.all)	// IE 4
    {
        document.all[szDivID].style.visibility = iState ? "visible" : "hidden";
    }

}


function clearNameValueError()
{
  		document.getElementById('rightname2').style.visibility = "hidden";
  		document.getElementById('rightname2').style.borderTop = "";
  		document.getElementById('rightname2').style.borderBottom = "";
    		document.getElementById('rightname2').style.borderRight= ""
  		document.getElementById('leftname2').style.borderTop = "";
  		document.getElementById('leftname2').style.borderBottom = "";
    		document.getElementById('leftname2').style.borderLeft= "";
    		document.getElementById('centername2').style.borderTop = "";
  		document.getElementById('centername2').style.borderBottom = "";
  		
  		document.getElementById('rightvalue').style.visibility = "hidden";
  		document.getElementById('rightvalue').style.borderTop = "";
  		document.getElementById('rightvalue').style.borderBottom = "";
    		document.getElementById('rightvalue').style.borderRight= "";
  		document.getElementById('leftvalue').style.borderTop = "";
  		document.getElementById('leftvalue').style.borderBottom = "";
    		document.getElementById('leftvalue').style.borderLeft= "";  
    		document.getElementById('centervalue').style.borderTop = "";
  		document.getElementById('centervalue').style.borderBottom = "";  		
}

function insertOptionBefore(num, szDivID)
{
  var errors = false;

  clearNameValueError();
  if (document.getElementById('Name1').value == "")  	
  	{
  		document.getElementById('rightname2').style.visibility = "visible";
  		document.getElementById('rightname2').style.borderTop = "1px solid #e83e00";
  		document.getElementById('rightname2').style.borderBottom = "1px solid #e83e00";
    		document.getElementById('rightname2').style.borderRight= "1px solid #e83e00";
  		document.getElementById('leftname2').style.borderTop = "1px solid #e83e00";
  		document.getElementById('leftname2').style.borderBottom = "1px solid #e83e00";
    		document.getElementById('leftname2').style.borderLeft= "1px solid #e83e00";  
    		document.getElementById('centername2').style.borderTop = "1px solid #e83e00";
  		document.getElementById('centername2').style.borderBottom = "1px solid #e83e00";
  		errors = true;
  	}
  if (document.getElementById('Value1').value == "")  	
  	{
  		document.getElementById('rightvalue').style.visibility = "visible";
  		document.getElementById('rightvalue').style.borderTop = "1px solid #e83e00";
  		document.getElementById('rightvalue').style.borderBottom = "1px solid #e83e00";
    		document.getElementById('rightvalue').style.borderRight= "1px solid #e83e00";
  		document.getElementById('leftvalue').style.borderTop = "1px solid #e83e00";
  		document.getElementById('leftvalue').style.borderBottom = "1px solid #e83e00";
    		document.getElementById('leftvalue').style.borderLeft= "1px solid #e83e00";  
    		document.getElementById('centervalue').style.borderTop = "1px solid #e83e00";
  		document.getElementById('centervalue').style.borderBottom = "1px solid #e83e00";
  		errors = true;
  	}

  if (errors == false) {
	ShowHide2(szDivID, 0)
	  var elSel = document.getElementById('selectX');
	    var elOptNew = document.createElement('option');
	    elOptNew.text = document.getElementById("Name1").value + " = " + document.getElementById("Value1").value; //'Ins333ert' + num;
	    elOptNew.value = 'insert' + num;
	    var elOptOld = elSel.options[0];   
	    try {
	      elSel.add(elOptNew, elOptOld); // standards compliant; doesn't work in IE
	    }
	    catch(ex) {
	      elSel.add(elOptNew, elSel.selectedIndex); // IE only
	    }
  }
}

function doSubmit(form)
{
  var errors = false;
  if (document.getElementById('param_new').style.visibility == "visible")
  {
	  if (form.ui_tf_id_.value == "")  	
		{
			document.getElementById('rightid').style.visibility = "visible";
			document.getElementById('rightid').style.borderTop = "1px solid #e83e00";
			document.getElementById('rightid').style.borderBottom = "1px solid #e83e00";
			document.getElementById('rightid').style.borderRight= "1px solid #e83e00";
			document.getElementById('leftid').style.borderTop = "1px solid #e83e00";
			document.getElementById('leftid').style.borderBottom = "1px solid #e83e00";
			document.getElementById('leftid').style.borderLeft= "1px solid #e83e00";  
			document.getElementById('centerid').style.borderTop = "1px solid #e83e00";
			document.getElementById('centerid').style.borderBottom = "1px solid #e83e00";
			errors = true;
		}
	  if (form.ui_tf_name_.value == "")  	
		{
			document.getElementById('rightname').style.visibility = "visible";
			document.getElementById('rightname').style.borderTop = "1px solid #e83e00";
			document.getElementById('rightname').style.borderBottom = "1px solid #e83e00";
			document.getElementById('rightname').style.borderRight= "1px solid #e83e00";
			document.getElementById('leftname').style.borderTop = "1px solid #e83e00";
			document.getElementById('leftname').style.borderBottom = "1px solid #e83e00";
			document.getElementById('leftname').style.borderLeft= "1px solid #e83e00";  
			document.getElementById('centername').style.borderTop = "1px solid #e83e00";
			document.getElementById('centername').style.borderBottom = "1px solid #e83e00";
			errors = true;
		}
	  if (form.ui_tf_url_.value == "")  	
		{
			document.getElementById('righturl').style.visibility = "visible";
			document.getElementById('righturl').style.borderTop = "1px solid #e83e00";
			document.getElementById('righturl').style.borderBottom = "1px solid #e83e00";
			document.getElementById('righturl').style.borderRight= "1px solid #e83e00";
			document.getElementById('lefturl').style.borderTop = "1px solid #e83e00";
			document.getElementById('lefturl').style.borderBottom = "1px solid #e83e00";
			document.getElementById('lefturl').style.borderLeft= "1px solid #e83e00";  
			document.getElementById('centerurl').style.borderTop = "1px solid #e83e00";
			document.getElementById('centerurl').style.borderBottom = "1px solid #e83e00";
			errors = true;
		}
	if (errors == false) {
		  var elSel = document.getElementById('selectX');
		  var i;
		  var count = 0;
		  var str = "";
		  var addHidden = document.getElementById('param_new3');
		  for (i = elSel.length - 1; i>=0; i--) {
			if (elSel.options[i].text.indexOf('=') !=-1)
			{
				var col_array=elSel.options[i].text.split(" = ");
				addHidden.innerHTML += '<INPUT TYPE="hidden" NAME="ui_tf_RealName' + count+ '_" VALUE="' + col_array[0] + '">';  	
				addHidden.innerHTML += '<INPUT TYPE="hidden" NAME="ui_tf_RealValue' + count+ '_" VALUE="' + col_array[1] + '">';  	
				count++;
			}
		  }
	}
  }
  if (errors == false) 
  {
  	return true;
  }
  else {
  	return false;
  }
}



var count1 = 0;
var count2 = 0;



function removeOptionSelected()
{
  var elSel = document.getElementById('selectX');
  var i;
  for (i = elSel.length - 1; i>=0; i--) {
    if (elSel.options[i].selected) {
      elSel.remove(i);
    }
  }
}
</script>


<ui:form name="mike" onSubmit="return doSubmit(this);">
<ui:hiddenfield beanId="edit"/>
<table>
<tr><td valign="top">
<div id="col">
	<img src="/jsrtutorial/html/images/hive-small.gif"><B>Manage Cells</b>
	<div class="line">&nbsp;</div>
	<ui:text value="Register new cells or modify existing ones:"/>
	<br />


	<div id="dyn_table" name="dyn_table" class="channel_fields">
	        <ui:listbox id="mike" beanId="registeredcelllist"  size="5" >
		</ui:listbox>

		<br clear="all"/>
		<input id="edit" class="portlet-form-button" type="submit" value="Edit Cell Info" name="gs_action=editRegisteredCell"/> 
		<%-- <ui:actionsubmit action="deleteRegisteredCell" name="deleteRegisteredData" value="Remove Selected"/>  --%>
		<input id="delete" class="portlet-form-button" type="submit" value="Remove Selected" name="gs_action=deleteRegisteredCell"/> 
		<br clear="all"/>
		<div class="line">&nbsp;</div>

		<input type=button name="add" id="add" value="Add New Cell -->" onclick="disableDiv(); ShowHide2('param_new', 1)">
	</div>
</div>
</td>
<td valign="top">
 <div id="param_new" name="parm_new">
		<img src="/jsrtutorial/html/images/cell-small.gif"><B>Add New Cell</B>
		<div class="line">&nbsp;</div>
		<div class="steps"><b>Step 1:</b> Fill in required Cell Information</div>
		<br clear="all"/>
		<table>
			
		<tr name="chkId" class="col1">
		<td id="leftid" class="idleft"><ui:text value="ID:"/></td>
		<td id="centerid" class="idcenter"><ui:textfield beanId="id"/></td>
		<td id="rightid" style="visibility:hidden; ">
			<img src="/jsrtutorial/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
		</td>
		</tr>
		<tr name="chkName" class="col1">
		<td id="leftname"><ui:text value="Name:"/></td>
	        <td id="centername"><ui:textfield beanId="name"/></td>
		<td id="rightname" style="visibility:hidden; ">
			<img src="/jsrtutorial/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
		</td>
		</tr>
		<tr name="chkUrl" class="col1">
		<td id="lefturl"><ui:text value="URL:"/></td>
		<td id="centerurl"><ui:textfield beanId="url"/></td>
		<td id="righturl" style="visibility:hidden; ">
			<img src="/jsrtutorial/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
		</td>
		</tr>
		<tr>
		<td> <ui:text value="Web Service:"/></td>
		        <td>
		        <select style="width: 100px" size="0" name="ui_lb_webservicelist_">
			<option value="REST">REST</option>
			<option value="SOAP">SOAP</option>
			</select>
		<td>
		</td>
		</tr>
		</table>


		<br clear="all"/><br clear="all"/><br clear="all"/>
		<div class="steps"><b>Step 2:</b> <i>(optional)</i>Add cell variables</div>
		<br clear="all"/>
		<table>
			<tr valign="top">
			<td width="100">
			              	<ui:text value="Cell Variables:"/>
			</td>
			<td>
			<select id="selectX"  size="5" multiple="multiple">
<%--			<option value="original1" selected="selected">Orig1</option> --%>
			</select>
			<br clear="all"/>
			<input type=button value="New Param" onclick="ShowHide2('param_new3', 1)">
			<input type="button" value="Remove" onclick="removeOptionSelected();" />
			<br clear="all"/>

			<div id="param_new3" name="parm_new3">
				<table>
					<tr>
					<td id="leftname2" width=75" >Name</td>
					<td id="centername2"><input type="text" id="Name1" name="Name1"/></td>
					<td id="rightname2" style="visibility:hidden; ">
						<img src="/jsrtutorial/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
					</td>
					</tr>
					<tr>
					<td id="leftvalue" width="75">Value</td>
					<td id="centervalue"><input type="text" id="Value1" name="Value1"/></td>
					<td id="rightvalue" style="visibility:hidden; ">
						<img src="/jsrtutorial/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
					</td>
					</tr>
					<tr>
					<td colspan="2"><input type="button" value="Add" onclick="insertOptionBefore(count1++, 'param_new3');" />
				<input type=button value="Cancel" onclick="ShowHide2('param_new3', 0)">
					</td>
					</tr>
				</table>
			</div>
			</td>
			</tr></table>
		<br clear="all"/>

		<div class="line">&nbsp;</div>
	 		<ui:actionsubmit  action="addRegisteredCell" value="Save"/> 
	 		<%-- <input class="portlet-form-button" type="submit" value="Save" name="gs_action=addRegisteredCell"/> --%>
 			<input type=button value="Cancel" onclick="enableDiv(); ShowHide2('param_new', 0)">
		</div>
	 
</div>	 
	 
</td></tr>
</table>
	 
   
</ui:form>

<script type="text/javascript">
	var editable = document.forms[0].ui_hf_edit_;

	if (editable.value == 'true')
	{
		disableDiv(); 
		ShowHide2('param_new', 1);
		var turnoff = document.forms[0].ui_tf_id_;
		turnoff.style.color = 'gray';
		turnoff.disabled = true;

	}

</script>