/**
 * @projectDescription	PM Administration Module
 * @inherits			i2b2
 * @namespace			i2b2.PM
 * @author			Nick Benik, Mike Mendis, Griffin Weber MD PhD
 * @version			1.0
 */

i2b2.PM.model.helpMSGS = {};
i2b2.PM.model.helpMSGS.LOADED = "Welcome to the i2b2 Project Management Interface<br />Use the tree on the left to load a configuration screen.";
i2b2.PM.model.helpMSGS.HIVE = "Hive Configuration<br />Describe what these functions are for.";
i2b2.PM.model.helpMSGS.PROJECT = "<form><p>Click on \"Project\" in the navigation bar to refresh the list of projects.<br />Please select a project on the left to edit it's properties</p>"+
	'<div id="AddNewProjBtnDIV"><input type="BUTTON" value="Add New Project" onclick="$(\'AddNewProjBtnDIV\').hide(); $(\'AddNewProjDIV\').show();"></div>'+
	'<div id="AddNewProjDIV" style="display:none">'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Project Id:</b></td><td><input type="TEXT" id="pmAdmin-projID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Wiki:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projWiki" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Key:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projKey" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Description:</b></td><td><textarea maxlength="2000" id="pmAdmin-projDesc" style="width:250px; height:126px"/></textarea></td></tr>'+
	'<tr><td valign="middle"><b>Project Path:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projPath" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Save" onclick="i2b2.PM.admin.saveProject();"/> <input type="BUTTON" value="Cancel" onclick="$(\'AddNewProjBtnDIV\').show(); $(\'AddNewProjDIV\').hide();"/></td></tr>'+
	'</table></div></form>';
i2b2.PM.model.helpMSGS.PROJECTREC = "<form><p>Please select which project configuration screen you want to access.</p>"+
	'<div>'+
	'<table border="0"><tbody><tr><td valign="middle"><b>Project Id:</b></td><td><input type="TEXT" id="pmAdmin-projID" maxlength="50" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Name:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projName" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Wiki:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projWiki" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Key:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projKey" style="width:250px"/></td></tr>'+
	'<tr><td valign="middle"><b>Project Description:</b></td><td><textarea maxlength="2000" id="pmAdmin-projDesc" style="width:250px; height:126px"/></textarea></td></tr>'+
	'<tr><td valign="middle"><b>Project Path:</b></td><td><input type="TEXT" maxlength="255" id="pmAdmin-projPath" style="width:250px"/></td></tr>'+
	'<tr><td></td><td align="right"><input type="HIDDEN" value="" id="pmAdmin-projOrigKey"/><input type="BUTTON" value="Delete" onclick="i2b2.PM.admin.deleteProject();"/> <input type="BUTTON" value="Save Updates" onclick="i2b2.PM.admin.saveProject();"/> <input type="BUTTON" value="Cancel" onclick="i2b2.PM.view.admin.refreshScreen();"/></td></tr>'+
	'</table></div></form>';


i2b2.PM.model.adminButtonsPrimary = {};
i2b2.PM.model.adminButtonsPrimary["HIVEDOMAINS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["HIVECELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["HIVEGLOBALS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["PROJECTREC-PARAMS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["PROJECTREC-CELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsPrimary["PROJECTREC-USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(1, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(1,'NEW');\"/></form>";


i2b2.PM.model.adminColumnDef = {};
i2b2.PM.model.adminColumnDef["HIVEDOMAINS"] = [
	{key:"domain_id",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"active",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["Active"],disableBtns:true})}, 
	{key:"environment", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["DEVELOPMENT","PRODUCTION", "TEST"],disableBtns:true})}, 
	{key:"domain_name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"helpURL",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["HIVECELLS"] = [
	{key:"id", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"method",sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:["SOAP","REST","OTHER"],disableBtns:true})}, 
	{key:"url",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["HIVEGLOBALS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
        {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["USERPARAMS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
       {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["PROJPARAMS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
       {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];
i2b2.PM.model.adminColumnDef["HIVECELLPARAMS"] = [
	{key:"name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
       {key:"datatype", sortable:true, resizeable:true, editor: new YAHOO.widget.RadioCellEditor({radioOptions:[{label:"Text", value:"T"}, {label:"Reference Text", value:"M"}, {label:"Reference Binary", value:"C"},{label:"Numeric", value:"N"},{label:"Date", value:"D"}, {label:"Integer", value:"I"}, {label:"Boolean", value:"B"}, {label:"RTF", value:"RTF"}, {label:"Excel", value:"XLS"}, {label:"XML", value:"XML"}, {label:"Word", value:"Doc"}],disableBtns:true})},
	{key:"value",sortable:true, resizeable:true,editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}
];

i2b2.PM.model.adminColumnDef["USERS"] = [
	{key:"full_name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"user_name",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"email",sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})}, 
	{key:"password",resizeable:true, editor: new YAHOO.widget.PasswordCellEditor({disableBtns:false}), formatter: DataTableUtils.PasswordFormatter}
];

// build the columndef for project-user dynamically
var t = i2b2.PM.cfg.config.authRoles;
var l = t.length;
var codes = [];
for (var i=0; i<l; i++) {
	codes.push(t[i].code);
}
i2b2.PM.model.adminColumnDef["PROJECTREC-USERS"] = [
	{key:"user_name", sortable:true, resizeable:true, editor: new YAHOO.widget.TextboxCellEditor({disableBtns:true})},
	{key:"roles", sortable:false, resizeable:true, editor: new YAHOO.widget.CheckboxCellEditor({checkboxOptions:codes, disableBtns:false})}
];


i2b2.PM.model.adminButtonsSecondary = {};
i2b2.PM.model.adminButtonsSecondary["USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["HIVECELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["HIVEDOMAINS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["PROJECTREC-CELLS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";
i2b2.PM.model.adminButtonsSecondary["PROJECTREC-USERS"] = "<form><input type='button' value='Save Updates' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'UPDATE');\"/><input type='button' value='Delete' onclick=\"i2b2.PM.admin.clickActionBtn(2, 'DELETE');\"/><input type='button' value='Add New' onclick=\"i2b2.PM.admin.clickActionBtn(2,'NEW');\"/></form>";








// visual screen configuration
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.view.admin.showInfoPanel = function(infoID) {
	if (!infoID) {
		$('pmAdminHelp').hide();
	} else {
		if (i2b2.PM.model.helpMSGS[infoID]) {
			$('pmAdminHelp').innerHTML = i2b2.PM.model.helpMSGS[infoID];
		} else {
			$('pmAdminHelp').innerHTML = "Could not find help message ["+infoID+"]";
		}
		$('pmAdminHelp').show();
	}
};

i2b2.PM.view.admin.configScreenDispay = function(dispLevel) {
	var configScreen = i2b2.PM.view.admin.configScreen;
	switch(dispLevel) {
		case 0:
			// no config
			Element.hide('pmAdminMainTableview');
			Element.hide('pmAdminTableviewButtons');
			Element.hide('pmAdminParamTableview');
			Element.hide('pmAdminParamTableviewButtons');
			break;
		case 1:
			// only the main grid
			$('pmAdminMainTableview').show();
			// display action buttons for main grid
			if (i2b2.PM.model.adminButtonsPrimary[configScreen]) {
				$('pmAdminTableviewButtons').innerHTML = i2b2.PM.model.adminButtonsPrimary[configScreen];
				Element.show('pmAdminTableviewButtons');
			} else {
				Element.hide('pmAdminTableviewButtons');
			}
			Element.hide('pmAdminParamTableview');
			Element.hide('pmAdminParamTableviewButtons');
			break;
		case 2:
			// main grid and parameters grid
			Element.show('pmAdminMainTableview');
			// display action buttons for main grid
			if (i2b2.PM.model.adminButtonsPrimary[configScreen]) {
				$('pmAdminTableviewButtons').innerHTML = i2b2.PM.model.adminButtonsPrimary[configScreen];
				Element.show('pmAdminTableviewButtons');
			} else {
				Element.hide('pmAdminTableviewButtons');
			}
			Element.show('pmAdminParamTableview');
			if (i2b2.PM.model.adminButtonsSecondary[configScreen]) {
				$('pmAdminParamTableviewButtons').innerHTML = i2b2.PM.model.adminButtonsSecondary[configScreen];
				Element.show('pmAdminParamTableviewButtons');
			} else {
				Element.hide('pmAdminParamTableviewButtons');
			}
			break;
	}
};




// data retreval and display

i2b2.PM.view.admin.showProjectUsers = function() {
	var proj_data = i2b2.PM.view.admin.currentProject;
	$('pmMainTitle').innerHTML = 'Project &gt; "'+proj_data.label+'" &gt; Users';
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllRole("PM:Admin", {id: proj_data.i2b2NodeKey, proj_path:"/"+proj_data.i2b2NodeKey});
	// custom parse functionality
	var tmpRoles = {};
	var c = i2b2.h.XPath(recList.refXML, "//role[user_name and role]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var name = i2b2.h.XPath(c[i], "descendant-or-self::role/user_name/text()")[0].nodeValue;
			if (!tmpRoles[name]) { tmpRoles[name] = []; }
			tmpRoles[name].push(i2b2.h.XPath(c[i], "descendant-or-self::role/role/text()")[0].nodeValue);
		} catch(e) {}
	}
	var dSrc = [];
	for (var un in tmpRoles) {
		dSrc.push({user_name: un, roles: tmpRoles[un]});
	}
	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(dSrc);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = { fields: ["user_name","roles"] };
	delete recList;
	// create grid 
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef["PROJECTREC-USERS"];
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showUserParams = function(usrname) {
	// show the secondary grid
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"user", id_xml:"<user_id>"+usrname+"</user_id>"});
	recList.parse(usrname);
	var tmp = recList.model;
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.USERPARAMS;
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, {});
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showCellParams = function(cellName, path) {
	if (undefined == path) { path = "/"; }
	// show the secondary grid
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"cell", param_xml: " id='"+cellName+"' ", id_xml:"<project_path>"+path+"</project_path>"});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLPARAMS;
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, {});
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showDomainParams = function(DomainID) {
	// show the secondary grid
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"hive", id_xml:DomainID});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLPARAMS;
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, {});
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showProjectCells = function() {
	var proj_data = i2b2.PM.view.admin.currentProject;
	$('pmMainTitle').innerHTML = 'Project &gt; "'+proj_data.label+'" &gt; Cells';
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// create the filtered DataSource
	i2b2.PM.admin.refreshCellListData(); 
	var tmp = [];
	for (var idx in i2b2.PM.model.admin.CellList) {
		if (i2b2.PM.model.admin.CellList[idx].project_path == "/"+proj_data.i2b2NodeKey) {
			tmp.push(i2b2.PM.model.admin.CellList[idx]);
		}
	}
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {
		fields: ["id","method","name","url"]
	};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLS;
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showProjectParams = function() {
	var proj_data = i2b2.PM.view.admin.currentProject;
	$('pmMainTitle').innerHTML = 'Project &gt; "'+proj_data.label+'" &gt; Params';
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:'project', id_xml:proj_data.i2b2NodeKey});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["name","datatype","value"]};
	delete recList;
	// create grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.PROJPARAMS;
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};


i2b2.PM.view.admin.showUsers = function() {
	$('pmMainTitle').innerHTML = "Manage Users";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get a list of user info
	var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
	usrList.parse();
	var tmp = [];
	var l = usrList.model.length;
	for (var i=0; i<l; i++) {
		tmp.push(usrList.model[i]);
	}
	delete usrList.model;
	delete usrList;		
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["user_name","full_name","password", "email"]};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.USERS;
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showHiveDomains = function() {
	$('pmMainTitle').innerHTML = "Hive &gt; Domains";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllHive("PM:Admin", {});
	recList.parse();
	var tmp = recList.model;
	var l = tmp.length;
	for (var i=0;i<l;i++) {
		if (Boolean.parseTo(tmp[i].active)) {
			tmp[i].active = "Active";
		} else {
			tmp[i].active = "Inactive";
		}
	}

	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {
		fields: ["domain_id","domain_name","environment","helpURL","active"]
	};
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVEDOMAINS;
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showHiveCells = function() {
	$('pmMainTitle').innerHTML = "Hive &gt; Cells";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// refresh data
	i2b2.PM.admin.refreshCellListData();
	// create the filtered DataSource
	var tmp = [];
	for (var idx in i2b2.PM.model.admin.CellList) {
		if (i2b2.PM.model.admin.CellList[idx].project_path == "/") {
			tmp.push(i2b2.PM.model.admin.CellList[idx]);
		}
	}
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {
		fields: ["id","method","name","url"]
	};
	// create the grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVECELLS;
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showGlobals = function() {
	$('pmMainTitle').innerHTML = "Hive &gt; Global Parameters";
	i2b2.PM.view.admin.showInfoPanel(false);
	i2b2.PM.view.admin.configScreenDispay(1);
	// get data
	var recList = i2b2.PM.ajax.getAllGlobal("PM:Admin", {});i2b2.PM.model.admin.CellList
	recList.parse();
	var tmp = recList.model;
	// create datasource
	i2b2.PM.admin.dsPrimary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsPrimary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsPrimary.responseType.responseSchema = {fields: ["name","value"]};
	delete recList.model;
	delete recList;
	// create grid
	i2b2.PM.admin.grdPrimaryColumnDefs = i2b2.PM.model.adminColumnDef.HIVEGLOBALS;
	var t = new YAHOO.widget.DataTable("pmAdminMainTableview", i2b2.PM.admin.grdPrimaryColumnDefs, i2b2.PM.admin.dsPrimary, {});
	i2b2.PM.view.admin.yuiControls.primaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
};

i2b2.PM.view.admin.showUserProjParams = function(username, project) {
	i2b2.PM.view.admin.configScreenDispay(2);
	// get data
	var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"project_user", param_xml:' id="'+project+'"', id_xml:"<user_name>"+username+"</user_name>"});
	// custom parse functionality
	var tmp = [];
	var c = i2b2.h.XPath(recList.refXML, "//param[@name and @id]");
	var l = c.length;
	for (var i=0; i<l; i++) {
		try {
			var tmpRec = {};
			tmpRec.name = i2b2.h.XPath(c[i], "attribute::name")[0].nodeValue;
			tmpRec.id = i2b2.h.XPath(c[i], "attribute::id")[0].nodeValue;
			tmpRec.datatype = i2b2.h.XPath(c[i], "attribute::datatype")[0].nodeValue;
			tmpRec.value = i2b2.h.XPath(c[i], "text()")[0].nodeValue;
			tmp.push(tmpRec);
		} catch(e) {}
	}
	// create datasource
	i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
	i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
	i2b2.PM.admin.dsSecondary.responseType.responseSchema = { fields: ["name","datatype","value"] };
	delete recList;
	// create grid
	i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.USERPARAMS;
	var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, {});
	i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
	t.isDirty = false;
	t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
	t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
	t.subscribe("cellClickEvent", i2b2.PM.view.admin.paramgridClickHandler);
	t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
	
};
