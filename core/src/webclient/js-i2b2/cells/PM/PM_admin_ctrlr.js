/**
 * @projectDescription	PM Administration Module
 * @inherits			i2b2
 * @namespace			i2b2.PM
 * @author			Nick Benik, Mike Mendis, Griffin Weber MD PhD
 * @version			1.0
 */

console.group('Load & Execute component file: cells > PM > Admin');
i2b2.PM.admin = {};
i2b2.PM.model.admin = {};




// create view controller [i2b2.PM.view.admin]
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.view.admin = new i2b2Base_cellViewController(i2b2.PM, 'admin');
i2b2.PM.view.admin.visible = false;
i2b2.PM.view.admin.yuiControls = {};
i2b2.PM.view.admin.yuiControls.primaryGrid = {};
i2b2.PM.view.admin.yuiControls.secondaryGrid = {};
i2b2.PM.view.admin.yuiTreeNodePROJECTS = false;
i2b2.PM.view.admin.Resize = function(e){
//	var t = $('pmNavTreeview');
	var ds = document.viewport.getDimensions();
	var w = ds.width;
	var h = ds.height-50;
	//if (w < 840) { w = 840; }
	if (h < 170) { h = 170; }	
	$('pmNavTreeview').style.height = h - 47;
	$('pmAdminMainView').style.height = h - 44;
	$('pmNav').style.height = h;
	$('pmMain').style.left = 200;
	$('pmMain').style.height = h;
	$('pmMain').style.width = w - 225;
	$('pmAdminMainView').style.width = w - 240;
}

// attach resize events
YAHOO.util.Event.addListener(window, "resize", i2b2.PM.view.admin.Resize, i2b2.PM.view.admin);

// capture view mode changes (via EVENT CAPTURE)
// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode){
	newMode = newMode[0];
	this.viewMode = newMode;
	if (newMode=="Admin") {
		i2b2.PM.view.admin.parentID = false;
		i2b2.PM.view.admin.configScreen = false;
		var pu = $('pmNav');
		pu.show();
		pu = pu.style;
		pu.width = 170;
		pu.height = 144;
		if (!i2b2.PM.view.admin.yuiControls.pmNavTreeview) {
			var tree = new YAHOO.widget.TreeView("pmNavTreeview");
			i2b2.PM.view.admin.yuiControls.pmNavTreeview = tree;
			var root = tree.getRoot(); 
			var tmpNode = new YAHOO.widget.TextNode({label: "Hive", expanded: false}, root);
			tmpNode.data.i2b2NodeType = "HIVE";
			var tmpNode2 = new YAHOO.widget.TextNode({label: "Domains", expanded: false}, tmpNode);
			tmpNode2.data.i2b2NodeType = "HIVEDOMAINS";
			var tmpNode2 = new YAHOO.widget.TextNode({label: "Cells", expanded: false}, tmpNode);
			tmpNode2.data.i2b2NodeType = "HIVECELLS";
			var tmpNode2 = new YAHOO.widget.TextNode({label: "Global Params", expanded: false}, tmpNode);
			tmpNode2.data.i2b2NodeType = "HIVEGLOBALS";
			// save this for later use (refresh project list)
			i2b2.PM.view.admin.yuiTreeNodePROJECTS = new YAHOO.widget.TextNode({label: "Projects", expanded: false}, root);
			i2b2.PM.view.admin.yuiTreeNodePROJECTS.data.i2b2NodeType = "PROJECTS";
			i2b2.PM.view.admin.yuiTreeNodePROJECTS.setDynamicLoad(i2b2.PM.admin.refreshProjects);
			var tmpNode = new YAHOO.widget.TextNode({label: "Manage Users", expanded: false}, root);
			tmpNode.data.i2b2NodeType = "USERS";
			tree.render(); 
			tree.subscribe('clickEvent', i2b2.PM.view.admin.treeClick);
		}
		i2b2.PM.view.admin.configScreenDispay(0);
		i2b2.PM.view.admin.showInfoPanel("LOADED");
		$('pmMain').show();
		$('pmNav').show();
	} else {
		$('pmMain').hide();
		$('pmNav').hide();		
		this.visible = false;
	}
	this.Resize();	
}),'',i2b2.PM.view.admin);





// Click handlers for action buttons
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.clickActionBtn = function(btnLevel, btnCommand) {
	var errAlertMissing = function() {alert("Unable to process. Required information is missing from the record.") };
	// identify targeted grid
	if (btnLevel==1) {
		var trgtGrid = i2b2.PM.view.admin.yuiControls.primaryGrid;
		var trgtColDefs = i2b2.PM.admin.grdPrimaryColumnDefs;
	} else {
		var trgtGrid = i2b2.PM.view.admin.yuiControls.secondaryGrid;
		var trgtColDefs = i2b2.PM.admin.grdSecondaryColumnDefs;
	}
	
	// get the selected rows (if any)
	var trgtRows = trgtGrid.getSelectedRows();
	switch(btnCommand) {
		case "DELETE":
			if (trgtRows.length == 0) {
				alert("Please select a record to delete.");
				return;
			}
			var deleteRow = trgtGrid.getRecord(trgtRows[0]).getData();
			switch (i2b2.PM.view.admin.configScreen) {
				case "HIVEDOMAINS":
					if (btnLevel==1) {
						// DELETE A HIVE DOMAIN
						if (!Object.isUndefined(deleteRow.domain_id)) {
							i2b2.PM.ajax.deleteHive("PM:Admin",  {domain_id:deleteRow.domain_id}, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						// DELETE A HIVE DOMAIN PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(deleteRow.id)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"hive", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showDomainParams(un.domain_id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;
				case "HIVECELLS":
				case "PROJECTREC-CELLS":
					if (btnLevel==1) {
						// DELETE HIVE CELL
						if (!Object.isUndefined(deleteRow.id, deleteRow.project_path)) {
							i2b2.PM.ajax.deleteCell("PM:Admin",  {id:deleteRow.id, project_path:deleteRow.project_path }, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						//DELETE HIVE CELL PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(deleteRow.id)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"cell", msg_xml:deleteRow.id}, (function(result) {
									if (i2b2.PM.view.admin.currentProject) {
										i2b2.PM.view.admin.showCellParams(selectedRec.id, "/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey);
									} else {
										i2b2.PM.view.admin.showCellParams(un.id);
									}
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}						
					}
					break;
				case "HIVEGLOBALS":
					if (!Object.isUndefined(deleteRow.id)) {
						i2b2.PM.ajax.deleteGlobal("PM:Admin",  {param_id:deleteRow.id}, i2b2.PM.view.admin.refreshScreen);
					} else {
						errAlertMissing();
					}
					break;
				case "PROJECTREC-PARAMS":
					// DELETE PROJECT PARAMETER
					if (!Object.isUndefined(i2b2.PM.view.admin.currentProject.i2b2NodeKey, deleteRow.id)) {
						// get the required project ID 
						var ma = ' id="'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'" ';
						i2b2.PM.ajax.deleteParam("PM:Admin",  {table:"project", msg_attrib: ma, msg_xml: deleteRow.id}, (function() {
							i2b2.PM.view.admin.showProjectParams();
						}));
						i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
						i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
						i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;	
					} else {
						errAlertMissing();
					}
					break;								
				case "PROJECTREC-USERS":
					if (btnLevel==1) {
						// DELETE PROJECT USER
						if (!confirm('Are you sure you want to delete username "'+deleteRow.user_name+'" from the project?')) { return false; }
						// VERIFY THAT USERNAME IS VALID!
						var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
						var c = i2b2.h.XPath(usrList.refXML, '//user/user_name[text() = "'+deleteRow.user_name+'"]');
						if (c.length == 0) {
							alert('The username "'+deleteRow.user_name+'" was not found!');
							return;
						}
						// GET THE USER'S EXISTING ROLES
						var roleList = i2b2.PM.ajax.getAllRole("PM:Admin", {id: i2b2.PM.view.admin.currentProject.i2b2NodeKey, proj_path:"/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey});
						var c = i2b2.h.XPath(roleList.refXML, '//user_name[text() = "'+deleteRow.user_name+'"]/../role/text()');
						var l = c.length;
						var actions = {};
						for (var i=0; i<l; i++) {
							var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: deleteRow.user_name, user_role: c[i].nodeValue, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
						}
						i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
						i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
						i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
						i2b2.PM.view.admin.showProjectUsers();
					} else {
						// DELETE PROJECT USER PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(un.user_name, deleteRow.id)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"project_user", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showUserProjParams(un.user_name, i2b2.PM.view.admin.currentProject.i2b2NodeKey);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;
				case "USERS":
					if (btnLevel==1) {
						// DELETE USER
						if (!Object.isUndefined(deleteRow.user_name)) {
							i2b2.PM.ajax.deleteUser("PM:Admin", {user_name:deleteRow.user_name}, i2b2.PM.view.admin.refreshScreen);
						} else {
							errAlertMissing();
						}
					} else {
						// DELETE USER PARAMETER
						try {
							// get the required username from the selected row in the primary grid
							var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
							var un = pgrd.getSelectedRows();
							un = pgrd.getRecord(un[0]).getData();
							if (!Object.isUndefined(un.user_name, deleteRow.name, deleteRow.value)) {
								i2b2.PM.ajax.deleteParam("PM:Admin", {table:"user", msg_xml:deleteRow.id}, (function(result) {
									i2b2.PM.view.admin.showUserParams(un.user_name);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} catch(e) {
							var s="Failed to delete the record";
							console.error(s);
							console.dir(e);
							alert(s);
						}
					}
					break;
				}

			break;
		case "UPDATE":
			if (trgtRows.length == 0) {
				alert("Please select a record to update.");
				return;
			}
			if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
				var updateRow = trgtGrid.getRecord(trgtRows[0]).getData();
				switch (i2b2.PM.view.admin.configScreen) {
					case "HIVEDOMAINS":
						if (btnLevel==1) {
							// UPDATE THE HIVE DOMAIN
							if (!Object.isUndefined(updateRow.domain_id, updateRow.domain_name, updateRow.environment, updateRow.helpURL)) {
								i2b2.PM.ajax.setHive("PM:Admin", {domain_id:updateRow.domain_id, domain_name:updateRow.domain_name, environment:updateRow.environment, helpURL:updateRow.helpURL}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE A HIVE DOMAIN PARAM
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = 'id="'+un.domain_id+'"';
								var ma = '';
								if (updateRow.id) {
									var mx = '<project_path>' +un.path +'</project_path><domain_id>'+un.domain_id+'</domain_id><param datatype="'+updateRow.datatype+'" id="'+updateRow.id+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<project_path>' +un.path +'</project_path><domain_id>'+un.domain_id+'</domain_id><param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"hive", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showDomainParams(un.domain_id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}							
						}
						break;
					case "HIVECELLS":
						if (btnLevel==1) {
							// UDATE HIVE CELL
							if (!Object.isUndefined(updateRow.id, updateRow.name, updateRow.method, updateRow.url)) {
								i2b2.PM.ajax.setCell("PM:Admin",  {cell_id:updateRow.id, project_path:"/", name:updateRow.name, method:updateRow.method, can_override:true, url:updateRow.url}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE HIVE CELL PARAMETER
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = ' id="'+un.id+'" ';
								if (updateRow.id) {
									var mx = '<project_path>/</project_path><param datatype="'+updateRow.datatype+'" id="'+updateRow.id+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<project_path>/</project_path><param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"cell", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showCellParams(un.id);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						}
						break;
					case "HIVEGLOBALS":
						// UPDATE GLOBALS
						if (!Object.isUndefined(updateRow.name, updateRow.value)) {
							if (updateRow.id) {
								var t = ' id="'+updateRow.id+'" ';
							} else {
								var t = '';
							}
							i2b2.PM.ajax.setGlobal("PM:Admin", {param_name:updateRow.name, param_datatype:updateRow.datatype, param_value:updateRow.value, param_id_attrib:t, can_override:"Y"});
							i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
							i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
						} else {
							errAlertMissing();
						}
						break;
					case "PROJECTREC-CELLS":
						if (btnLevel==1) {
							// UPDATE THE PROJECT CELL
							if (!Object.isUndefined(updateRow.id, updateRow.name, updateRow.method, updateRow.url)) {
								i2b2.PM.ajax.setCell("PM:Admin",  {cell_id:updateRow.id, project_path:"/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey, name:updateRow.name, method:updateRow.method, can_override:true, url:updateRow.url}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE THE PROJECT CELL PARAMETER
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = ' id="'+un.id+'" ';
								if (updateRow.id) {
									var mx = '<project_path>/'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'</project_path><param datatype="'+updateRow.datatype+'" id="'+updateRow.id+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<project_path>/'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'</project_path><param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"cell", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showCellParams(selectedRec.id, "/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						}
						break;
					case "PROJECTREC-PARAMS":
						// UPDATE PROJECT PARAMETER
						if (!Object.isUndefined(updateRow.name, updateRow.value)) {
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required project ID 
								var ma = ' id="'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'" ';
								if (updateRow.id) {
									var mx = '<param id="'+updateRow.id+'" datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									var mx = '<param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"project", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showProjectParams();
								}));
								i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						} else {
							errAlertMissing();
						}	
						break;
					case "PROJECTREC-USERS":
						if (btnLevel==1) {
							// VERIFY THAT USERNAME IS VALID!
							var usrList = i2b2.PM.ajax.getAllUser("PM:Admin", {});
							var c = i2b2.h.XPath(usrList.refXML, '//user/user_name[text() = "'+updateRow.user_name+'"]');
							if (c.length == 0) {
								alert('The username "'+updateRow.user_name+'" was not found.\nPlease check the spelling, verify that the user is active, or add the username to the hive before adding project permissions.');
								return;
							}
							// VERIFY THAT AT LEAST ONE ROLE HAS BEEN SET
							if (undefined==updateRow.roles || updateRow.roles.length == 0) {
								alert('The username "'+updateRow.user_name+'" has no roles selected.\nPlease select one or more roles or use the delete button to remove the user from the project.');
								return;
							}
							// GET THE USER'S EXISTING ROLES
							//var roleList = i2b2.PM.ajax.getAllRole("PM:Admin", {id: i2b2.PM.view.admin.currentProject.i2b2NodeKey, proj_path:"/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey});
							//var c = i2b2.h.XPath(roleList.refXML, '//user_name[text() = "i2b2"]/../role/text()');
							//var l = c.length;
							//var actions = {};
							//for (var i=0; i<l; i++) {
							//	actions[c[i].nodeValue] = "DELETE";
							//}
							// CREATE AN ALTER PLAN USING THE NEW ROLE LIST
							var c = updateRow.roles;
							//var l = c.length;
							//for (var i=0; i<l; i++) {
							//	if (actions[c[i]]) {
									// the role already exists, keep it
							//		actions[c[i]] = "ADD";
							//	} else {
									// the role does not exist, add it
							//		actions[c[i]] = "ADD";
							//	}
							//}
							// UPDATE THE USERNAME's PERMISSIONS USING THE ALTER PLAN
							//var deletedRoles = false;
							// FIRST REMOVE ALL ROLES THAN ADD THEM BACK
							var t = i2b2.PM.cfg.config.authRoles;
							for (var i=0; i<t.length; i++) {
								var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: updateRow.user_name, user_role: t[i].code, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
							}

							//for (var roleCode in c) {
								//switch(actions[roleCode]) {
									//case "DELETE":
									//	var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: updateRow.user_name, user_role: roleCode, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
									//	break;
									//case "ADD":
                                                           //                     if (deletedRoles == false)
                                                             //                   {
							//				var t = i2b2.PM.cfg.config.authRoles;
							//				for (var i=0; i<t.length; i++) {
							//					var result = i2b2.PM.ajax.deleteRole("PM:Admin", {user_id: updateRow.user_name, user_role: t[i].code, project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
							//				}
							//				deletedRoles = true;
                                                          //                      }
							for (var i=0; i<c.length; i++) { 
										var result = i2b2.PM.ajax.setRole("PM:Admin", {user_id: updateRow.user_name, user_role: c[i], project_id:i2b2.PM.view.admin.currentProject.i2b2NodeKey});
								//		break;
								//}
							}
							i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
							i2b2.PM.view.admin.yuiControls.primaryGrid.unselectAllRows();
							i2b2.PM.view.admin.showProjectUsers();
						} else {
							// UPDATE USER-PROJECT PARAM
							if (!Object.isUndefined(updateRow.name, updateRow.value)) {
								// get the required cell ID from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								var ma = 'id="'+i2b2.PM.view.admin.currentProject.i2b2NodeKey+'"';
								var mx = '<user_name>'+un.user_name+'</user_name>';
								if (updateRow.id) {
									mx = mx + '<param id="'+updateRow.id+'" datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								} else {
									mx = mx + '<param datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+"</param>";
								}
								i2b2.PM.ajax.setParam("PM:Admin",  {table:"project_user", msg_attrib: ma, msg_xml: mx}, (function() {
									i2b2.PM.view.admin.showUserProjParams(un.user_name, i2b2.PM.view.admin.currentProject.i2b2NodeKey);
								}));
								i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
								i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
							} else {
								errAlertMissing();
							}
						}
						break;
					case "USERS":
						if (btnLevel==1) {
							// UPDATE USER
							if (!Object.isUndefined(updateRow.full_name, updateRow.user_name)) {
								if (updateRow.password != "") {
									updateRow.password = "<password>"+updateRow.password+"</password>";
								}
								i2b2.PM.ajax.setUser("PM:Admin", {user_name:updateRow.user_name, full_name:updateRow.full_name, email:updateRow.email, password:updateRow.password}, i2b2.PM.view.admin.refreshScreen);
							} else {
								errAlertMissing();
							}
						} else {
							// UPDATE USER PARAMETER
							try {
								// get the required username from the selected row in the primary grid
								var pgrd = i2b2.PM.view.admin.yuiControls.primaryGrid;
								var un = pgrd.getSelectedRows();
								un = pgrd.getRecord(un[0]).getData();
								if (!Object.isUndefined(un.user_name, updateRow.name, updateRow.value)) {
									if (updateRow.id) {
										var t = 'id="'+updateRow.id+'"';
									} else {
										var t = "";
									}
									var vals = '<user_name>'+un.user_name+'</user_name><param '+t+' datatype="'+updateRow.datatype+'" name="'+updateRow.name+'">'+updateRow.value+'</param>';
									i2b2.PM.ajax.setParam("PM:Admin", {table:"user", msg_xml:vals}, (function(result) {
										i2b2.PM.view.admin.showUserParams(un.user_name);
									}));
									i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
									i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
								} else {
									errAlertMissing();
								}
							} catch(e) {
								var s="Failed to update the record";
								console.error(s);
								console.dir(e);
								alert(s);
							}
						}	
						break;
				}
			}
			break;
		case "NEW":
			// abandon dirty data?
			try {
				if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
					if (confirm("Abandon Changes?")) {
						// clear previously added/edited row in primary data
						i2b2.PM.view.admin.treeClick(false,true);
					}
					return;
				} 
			} catch(e) {}
			// create blank record
			var t = {};
			t[trgtColDefs[0].key] = "";
			trgtGrid.unselectAllRows();
			trgtGrid.set("sortedBy", null);
			trgtGrid.addRow(t,0);
			trgtGrid.selectRow(0);
			trgtGrid.isDirty = true;
			if (btnLevel == 1) {
				// hide the secondary grid
				$('pmAdminParamTableview').hide();
				$('pmAdminParamTableviewButtons').hide();
			}
	}		
};



i2b2.PM.view.admin.refreshScreen = function() {
	if (i2b2.PM.view.admin.yuiControls.primaryGrid) {
		i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
	}
	if (i2b2.PM.view.admin.yuiControls.secondaryGrid) {
		i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
	}
	i2b2.PM.view.admin.treeClick(null,true);
};

i2b2.PM.admin.deleteProject = function() {
	var proj_id = $('pmAdmin-projID').value; 
	var proj_path = $('pmAdmin-projPath').value;
	if (proj_id=="" || proj_path=="") {
		alert('Project ID and project path are required!');
		return false;
	}
	if (confirm("Are you sure you want to delete this project?")) {
		i2b2.PM.ajax.deleteProject("PM:Admin", {project_id:proj_id, project_path:proj_path}, (function(result) {
			// restore screen
			$('pmMainTitle').innerHTML = "Project List";
			i2b2.PM.view.admin.showInfoPanel("PROJECT");
			i2b2.PM.view.admin.configScreenDispay(0);
			// refresh the project listings
			var evt = {node: i2b2.PM.view.admin.yuiTreeNodePROJECTS};
			i2b2.PM.view.admin.treeClick(evt, false);
		}));
	}
};

i2b2.PM.admin.saveProject = function() {
	var projData = {};
	// verify all required info is presented
	var errstr = "";
	var t = $('pmAdmin-projID').value; 
	if (t=="") {
		errstr = errstr + '\n Project ID is a required field';
	} else {
		projData.id = t;
	}
	var t = $('pmAdmin-projName').value;
	if (t=="") {
		errstr = errstr + '\n Project Name is a required field';
	} else {
		projData.name = t;
	}
	if (errstr != "") {
		alert("The following errors have occured:\n"+errstr);
		return;
	}
	// send data
	projData.wiki = $('pmAdmin-projID').value;
	projData.key = $('pmAdmin-projKey').value;
	if (projData.key == "") {
		projData.key = $('pmAdmin-projOrigKey').value;
	} else {
		var t = hex_md5(projData.key);
		projData.key = t.substr(0,3);
	}
	projData.description = $('pmAdmin-projDesc').value;
	projData.path = $('pmAdmin-projPath').value;
	i2b2.PM.ajax.setProject("PM:Admin", projData, i2b2.PM.admin.refreshProjects);
	// restore screen
	$('pmMainTitle').innerHTML = "Project List";
	i2b2.PM.view.admin.showInfoPanel("PROJECT");
	i2b2.PM.view.admin.configScreenDispay(0); 
};





// refresh treeview lists
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshProjects = function(tvNode, onCompleteCallback) {
	i2b2.PM.admin.refreshProjectListData();
	for (var idx in i2b2.PM.model.admin.ProjectList) {
		var d = i2b2.PM.model.admin.ProjectList[idx];
		var tmpNode = new YAHOO.widget.TextNode({label: d.name, expanded: false}, tvNode);
		tmpNode.data.i2b2NodeType = "PROJECTREC";
		tmpNode.data.i2b2NodeKey = d.id;
		tmpNode.data.i2b2NodePath = d.path;
		var tmpNode2 = new YAHOO.widget.TextNode({label: "Cells", expanded: false}, tmpNode);
		tmpNode2.data.i2b2NodeType = "PROJECTREC-CELLS";
		var tmpNode2 = new YAHOO.widget.TextNode({label: "Params", expanded: false}, tmpNode);
		tmpNode2.data.i2b2NodeType = "PROJECTREC-PARAMS";
		var tmpNode2 = new YAHOO.widget.TextNode({label: "Users", expanded: false}, tmpNode);
		tmpNode2.data.i2b2NodeType = "PROJECTREC-USERS";
	}
	if (onCompleteCallback) { onCompleteCallback(); }
};


// treeview click handler & action router
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.view.admin.treeClick = function(tvEvent, override) {
	if (i2b2.PM.view.admin.checkDirtyFlags()) { return; }
	if (override) {
		var info = i2b2.PM.view.admin.clickedTreeNode;
	} else {
		var info = tvEvent.node.data;
		i2b2.PM.view.admin.clickedTreeNode = info;
	}
	console.debug("treeview node clicked: "+info.i2b2NodeType);
	i2b2.PM.view.admin.configScreen = info.i2b2NodeType;
	switch(i2b2.PM.view.admin.configScreen) {
		case "HIVE":
			delete i2b2.PM.view.admin.currentProject;
			$('pmMainTitle').innerHTML = "Hive Overview";
			i2b2.PM.view.admin.showInfoPanel("HIVE");
			i2b2.PM.view.admin.configScreenDispay(0);
			break;
		case "HIVEDOMAINS":
			delete i2b2.PM.view.admin.currentProject;
			i2b2.PM.view.admin.showHiveDomains();
			break;
		case "HIVECELLS":
			delete i2b2.PM.view.admin.currentProject;
			i2b2.PM.view.admin.showHiveCells();
			break;
		case "HIVEGLOBALS":
			delete i2b2.PM.view.admin.currentProject;
			i2b2.PM.view.admin.showGlobals();
			break;
		case "PROJECTS":
			var proj_data = tvEvent.node.data;
			i2b2.PM.view.admin.currentProject = proj_data;
			$('pmMainTitle').innerHTML = "Project List";
			i2b2.PM.view.admin.showInfoPanel("PROJECT");
			i2b2.PM.view.admin.configScreenDispay(0);
			tvEvent.node.tree.removeChildren(tvEvent.node);
			break;
		case "PROJECTREC":
			try {
				$('pmMainTitle').innerHTML = 'Project &gt; "'+info.label+'"';
				i2b2.PM.view.admin.showInfoPanel("PROJECTREC");
				var response = i2b2.PM.ajax.getProject("PM:Admin", {proj_code:info.i2b2NodeKey, proj_path:info.i2b2NodePath});
				response.parse();
				var data = response.model[0];
				if (data.id) { $('pmAdmin-projID').value = data.id; }
				if (data.name) { $('pmAdmin-projName').value = data.name; }
				if (data.wiki) { $('pmAdmin-projWiki').value = data.wiki; }
				if (data.key) { 
					$('pmAdmin-projOrigKey').value = data.key; 
				}
				$('pmAdmin-projKey').value = ""; 
				if (data.description) { $('pmAdmin-projDesc').value = data.description; }
				if (data.path) {
					$('pmAdmin-projPath').value = data.path; 
				} else {
					$('pmAdmin-projPath').value = '/';
				}
				$('pmAdmin-projStatus').value = 'A';
			} catch (e) {}
			i2b2.PM.view.admin.configScreenDispay(0);
			break;
		case "PROJECTREC-USERS":
			// get parent record information
			i2b2.PM.view.admin.currentProject = tvEvent.node.parent.data;
			i2b2.PM.view.admin.showProjectUsers();
			break;
		case "PROJECTREC-CELLS":
			// get parent record information
			i2b2.PM.view.admin.currentProject = tvEvent.node.parent.data;
			i2b2.PM.view.admin.showProjectCells();
			break;
		case "PROJECTREC-PARAMS":
			// get parent record information
			i2b2.PM.view.admin.currentProject = tvEvent.node.parent.data;
			i2b2.PM.view.admin.showProjectParams();
			break;
		case "USERS":
			delete i2b2.PM.view.admin.currentProject;
			i2b2.PM.view.admin.showUsers();
			break;
		default:
			delete i2b2.PM.view.admin.currentProject;
			alert(i2b2.PM.view.admin.configScreen);
	}
};

i2b2.PM.view.admin.editorSaved = function(editObj) {
	if (editObj.newData != editObj.oldData) {
		editObj.editor._oDataTable.isDirty = true;
	}
};


i2b2.PM.view.admin.gridClickHandler = function(evtData) {
	if (this.isSelected(evtData.target)==true) {
		if (this.isDirty) {
			this.onEventShowCellEditor(evtData);
		} else {
			// block editing of some cells
			var srcn = i2b2.PM.view.admin.configScreen;
			var column = this.getColumn(evtData.target).field;
			if (!((srcn == "HIVEDOMAINS" && column == "domain_id") || (srcn == "PROJECTREC-USERS" && column == "user_name"))) {
				this.onEventShowCellEditor(evtData);
			}
		}
	} else {
		// abandon dirty data?
		try {
			if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
				if (confirm("Abandon Changes?")) {
					// clear previously added/edited row in primary data
					i2b2.PM.view.admin.treeClick(false,true);
				}
				return;
			}
		} catch(e) {}

		// deal with params grid
		try {
			i2b2.PM.view.admin.yuiControls.secondaryGrid.unselectAllRows();
		} catch (e) {}

		// unselect all rows
		this.unselectAllRows();
		this.selectRow(evtData.target);
		var configScreen = i2b2.PM.view.admin.configScreen;
		var selectedRec = this.getRecord(evtData.target)._oData;		
		switch(configScreen) {
			case "HIVEDOMAINS":
				i2b2.PM.view.admin.showDomainParams(selectedRec.domain_id);
				break;
			case "HIVECELLS":
				i2b2.PM.view.admin.showCellParams(selectedRec.id);
				break;
			case "":
				// load the HiveParams in the secondary grid
				i2b2.PM.view.admin.configScreenDispay(2);
				// get data
				var recList = i2b2.PM.ajax.getAllParam("PM:Admin", {table:"hive", id_xml:selectedRec.domain_id});
				recList.parse();
				var tmp = recList.model;
				// create datasource
				i2b2.PM.admin.dsSecondary = new YAHOO.util.DataSource(tmp);
				i2b2.PM.admin.dsSecondary.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
				i2b2.PM.admin.dsSecondary.responseType.responseSchema = {
					fields: ["name","value"]
				};
				delete recList.model;
				delete recList;
				// create grid
				i2b2.PM.admin.grdSecondaryColumnDefs = i2b2.PM.model.adminColumnDef.USERPARAMS;
				var t = new YAHOO.widget.DataTable("pmAdminParamTableview", i2b2.PM.admin.grdSecondaryColumnDefs, i2b2.PM.admin.dsSecondary, {});
				i2b2.PM.view.admin.yuiControls.secondaryGrid = t;
				t.isDirty = false;
				t.subscribe("rowMouseoverEvent", t.onEventHighlightRow);
				t.subscribe("rowMouseoutEvent", t.onEventUnhighlightRow);
				t.subscribe("cellClickEvent", i2b2.PM.view.admin.gridClickHandler);
				t.subscribe("editorSaveEvent", i2b2.PM.view.admin.editorSaved);
				break;
			case "PROJECTREC-CELLS":
				i2b2.PM.view.admin.showCellParams(selectedRec.user_name, "/"+i2b2.PM.view.admin.currentProject.i2b2NodeKey);
				break;
			case "PROJECTREC-USERS":
				i2b2.PM.view.admin.showUserProjParams(selectedRec.user_name, i2b2.PM.view.admin.currentProject.i2b2NodeKey);
				break;
			case "USERS":
				i2b2.PM.view.admin.showUserParams(selectedRec.user_name);
				break;
		}
	}
};





i2b2.PM.view.admin.paramgridClickHandler = function(evtData) {
	if (this.isSelected(evtData.target)==true) {
		if (this.isDirty) {
			this.onEventShowCellEditor(evtData);
		} else {
			// block editing of some cells
			var srcn = i2b2.PM.view.admin.configScreen;
			var column = this.getColumn(evtData.target).field;
			if (!((srcn == "HIVEDOMAINS" && column == "domain_id") || (srcn == "PROJECTREC-USERS" && column == "name"))) {
				this.onEventShowCellEditor(evtData);
			}
		}
	} else {
		// abandon dirty data?
		try {
			if (i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
				if (confirm("Abandon Changes?")) {
					// clear previously added/edited row in secondary data grid
					this.deleteRow(0);
					this.isDirty = false;
				}
				return;
			}
		} catch(e) {}
		// unselect all rows
		this.unselectAllRows();
		this.selectRow(evtData.target);
	}
};









i2b2.PM.view.admin.checkDirtyFlags = function() {
	try {
		if (i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty || i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty) {
			if (confirm("Abandon Changes?")) {
				// clear previously added/edited row in primary data
				i2b2.PM.view.admin.yuiControls.primaryGrid.isDirty = false;
				i2b2.PM.view.admin.yuiControls.secondaryGrid.isDirty = false;
				i2b2.PM.view.admin.yuiControls.primaryGrid.isNew = false;
				i2b2.PM.view.admin.yuiControls.secondaryGrid.isNew = false;
				i2b2.PM.view.admin.treeClick(false,true);
				return false;	
			}
			return true;
		} else {
			return false;
		}
	} catch(e) {}
}


// data layer stuff
// --------------------------------------------------------------------------------------------------------------------
i2b2.PM.admin.refreshCellListData = function() {
	var cellList = i2b2.PM.ajax.getAllCell("PM:Admin", {});
	cellList.parse();
	var tmp = {};
	var l = cellList.model.length;
	for (var i=0; i<l; i++) {
		tmp[cellList.model[i].id+cellList.model[i].project_path] = cellList.model[i];
	}
	delete cellList;
	i2b2.PM.model.admin.CellList = tmp;
};
i2b2.PM.admin.refreshProjectListData = function() {
	var projList = i2b2.PM.ajax.getAllProject("PM:Admin", {});
	projList.parse();
	var tmp = {};
	var l = projList.model.length;
	for (var i=0; i<l; i++) {
		tmp[projList.model[i].id] = projList.model[i];
	}
	delete projList;
	i2b2.PM.model.admin.ProjectList = tmp;
};



console.timeEnd('execute time');
console.groupEnd();
