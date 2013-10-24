/**
 * @projectDescription	Event controller for CRC's Query Tool.
 * @inherits 	i2b2.CRC.ctrlr
 * @namespace	i2b2.CRC.ctrlr.QT
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > ctrlr > QueryTool');
console.time('execute time');


i2b2.CRC.ctrlr.QT = new QueryToolController();
function QueryToolController() {
	i2b2.CRC.model.queryCurrent = {};
	this.queryIsDirty = true;
	this.queryIsRunning = false;
	this.queryNamePrompt = false;
	this.queryNameDefault = 'New Query';
	this.queryStatusDefaultText = 'Drag query items to one or more groups then click Run Query.';
	this.panelControllers = [];
	this.panelControllers[0] = new i2b2_PanelController(this);
	this.panelControllers[1] = new i2b2_PanelController(this);
	this.panelControllers[2] = new i2b2_PanelController(this);
// ================================================================================================== //
	this.doSetQueryName = function(inName) {
		this.queryIsDirty = true;
		$('queryName').innerHTML = inName;
		i2b2.CRC.model.queryCurrent.name = inName;
	}

// ================================================================================================== //
	this.doQueryClear = function() {
		// function to clear query from memory
		delete i2b2.CRC.model.queryCurrent;
		i2b2.CRC.model.queryCurrent = {};
		var dm = i2b2.CRC.model.queryCurrent;
		dm.panels = [];
		this.doSetQueryName.call(this,'');
		this.doShowFrom(0);
		this._redrawPanelCount();
		this.queryNamePrompt = false;
		this.queryIsDirty = true;
	}

// ================================================================================================== //
	this.doQueryLoad = function(qm_id) {  // function to load query from history
		// clear existing query
		i2b2.CRC.ctrlr.QT.doQueryClear();
		// show on GUI that work is being done
		i2b2.h.LoadingMask.show();

		// callback processor
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = this;
		scopedCallback.callback = function(results) {
			var cl_queryMasterId = qm_id;
			// THIS function is used to process the AJAX results of the getChild call
			//		results data object contains the following attributes:
			//			refXML: xmlDomObject <--- for data processing
			//			msgRequest: xml (string)
			//			msgResponse: xml (string)
			//			error: boolean
			//			errorStatus: string [only with error=true]
			//			errorMsg: string [only with error=true]
			i2b2.CRC.view.QT.queryRequest = results.msgRequest;
			i2b2.CRC.view.QT.queryResponse = results.msgResponse;
			// switch to status tab
			i2b2.CRC.view.status.showDisplay();
			// did we get a valid query definition back? 
			var qd = i2b2.h.XPath(results.refXML, 'descendant::query_name/..');
			if (qd.length != 0) {
				i2b2.CRC.ctrlr.QT.doQueryClear();
				var dObj = {};
				dObj.name = i2b2.h.getXNodeVal(qd[0],'query_name');
				$('queryName').innerHTML = dObj.name;
				dObj.specificity = i2b2.h.getXNodeVal(qd[0],'specificity_scale');
				dObj.panels = [];
				var qp = i2b2.h.XPath(qd[0], 'descendant::panel');
				var total_panels = qp.length;
				for (var i1=0; i1<total_panels; i1++) {
					// extract the data for each panel
					var po = {};
					po.panel_num = i2b2.h.getXNodeVal(qp[i1],'panel_number');
					var t = i2b2.h.getXNodeVal(qp[i1],'invert');
					po.exclude = (t=="1");
					var t = i2b2.h.getXNodeVal(qp[i1],'total_item_occurrences');
					po.occurs = (1*t)-1;
					var t = i2b2.h.getXNodeVal(qp[i1],'panel_date_from');
					if (t) {
						t = t.replace('Z','');
						t = t.split('-');
						po.dateFrom = {};
						po.dateFrom.Year = t[0];
						po.dateFrom.Month = t[1];
						po.dateFrom.Day = t[2];
					} else {
						po.dateFrom = false;
					}
					var t = i2b2.h.getXNodeVal(qp[i1],'panel_date_to');
					if (t) {
						t = t.replace('Z','');
						t = t.split('-');
						po.dateTo = {};
						po.dateTo.Year = t[0];
						po.dateTo.Month = t[1];
						po.dateTo.Day = t[2];
					} else {
						po.dateTo = false;
					}
					po.items = [];
					var pi = i2b2.h.XPath(qp[i1], 'descendant::item[item_key]');
					for (i2=0; i2<pi.length; i2++) {
						var item = {};
						// get the item's details from the ONT Cell
						var ckey = i2b2.h.getXNodeVal(pi[i2],'item_key');
						// WE MUST QUERY THE ONT CELL TO BE ABLE TO DISPLAY THE TREE STRUCTURE CORRECTLY
						var cdetails = i2b2.ONT.ajax.GetTermInfo("CRC:QueryTool", {concept_key_value:ckey, ont_synonym_records: true, ont_hidden_records: true} );
						
						// this is what comes out of the old AJAX call
						var c = i2b2.h.XPath(cdetails.refXML, 'descendant::concept');
						if (c.length > 0) {
							c = c[0];
							var o = new Object;
							o.xmlOrig = c;
							o.table_name = i2b2.h.getXNodeVal(c,'tablename');
							o.column_name = i2b2.h.getXNodeVal(c,'columnname');
							o.operator = i2b2.h.getXNodeVal(c,'operator');
							o.icd9 = i2b2.h.getXNodeVal(c,'basecode');
							o.level = i2b2.h.getXNodeVal(c,'level');
							o.name = i2b2.h.getXNodeVal(c,'name');
							o.key = i2b2.h.getXNodeVal(c,'key');
							o.tooltip = i2b2.h.getXNodeVal(c,'tooltip');
							o.dim_code = i2b2.h.getXNodeVal(c,'dimcode');
							o.visual_attribs = i2b2.h.getXNodeVal(c,'visualattributes');
							o.hasChildren = i2b2.h.getXNodeVal(c,'visualattributes').substring(0,2);
							// these are not needed?
							o.fact_table_column = i2b2.h.getXNodeVal(c,'facttablecolumn');
							o.column_name_datatype = i2b2.h.getXNodeVal(c,'columndatatype');
							o.synonym_cd = i2b2.h.getXNodeVal(c,'synonym_cd');
							o.totalnum = i2b2.h.getXNodeVal(c,'totalnum');
							// Lab Values processing
							var lvd = i2b2.h.XPath(pi[i2], 'descendant::constrain_by_value');
							if (lvd.length>0){
								lvd = lvd[0];
								// pull the LabValue definition for concept
								var lvdef = i2b2.h.XPath(c, "descendant::metadataxml/ValueMetadata[Loinc]");
								if (lvdef.length > 0) {
									lvdef = lvdef[0];
								} else {
									lvdef = false;
								}
								// extract & translate
								var t = i2b2.h.getXNodeVal(lvd,"value_constraint");
								o.LabValues = {};
								o.LabValues.NumericOp = i2b2.h.getXNodeVal(lvd,"value_operator");
								o.LabValues.GeneralValueType = i2b2.h.getXNodeVal(lvd,"value_type");								
								switch(o.LabValues.GeneralValueType) {
									case "NUMBER":
										o.LabValues.MatchBy = "VALUE";
										if (t.indexOf(' and ')!=-1) {
											// extract high and low values
											t = t.split(' and ');
											o.LabValues.ValueLow = t[0];
											o.LabValues.ValueHigh = t[1];
										} else {
											o.LabValues.Value = t;
										}
										break;
									case "STRING":
										o.LabValues.MatchBy = "VALUE";
										o.LabValues.ValueString = t;
										break;
									case "TEXT":	// this means Enum?
										o.LabValues.MatchBy = "VALUE";
										try {
											o.LabValues.ValueEnum = eval("(Array"+t+")");
										} catch(e) {
											console.error("Conversion Failed: Lab Value data = "+t);
										}
										break;
									case "FLAG":
										o.LabValues.MatchBy = "FLAG";
										o.LabValues.ValueFlag = t
										break;		
									default:
										o.LabValues.Value = t;
								}		
							}
							// sdx encapsulate
							var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',o);
							if (o.LabValues) {
								// We do want 2 copies of the Lab Values: one is original from server while the other one is for user manipulation
								sdxDataNode.LabValues = o.LabValues;
							}
							po.items.push(sdxDataNode);
						} else {
							console.error("CRC's ONT Handler could not get term details about '"+ckey+"'!");
						}
					}
					dObj.panels[po.panel_num] = po;
				}
				// reindex the panels index (panel [1,3,5] should be [0,1,2])
				dObj.panels = dObj.panels.compact();
				i2b2.CRC.model.queryCurrent = dObj;
				// populate the panels yuiTrees
				var qpc = i2b2.CRC.ctrlr.QT.panelControllers[0];
				var dm = i2b2.CRC.model.queryCurrent;
				for (var pi=0; pi<dm.panels.length; pi++) {
					// create a treeview root node and connect it to the treeview controller
					dm.panels[pi].tvRootNode = new YAHOO.widget.RootNode(qpc.yuiTree);
					qpc.yuiTree.root = dm.panels[pi].tvRootNode;
					dm.panels[pi].tvRootNode.tree = qpc.yuiTree;
					qpc.yuiTree.setDynamicLoad(i2b2.CRC.ctrlr.QT._loadTreeDataForNode,1);						
					// load the treeview with the data
					var tvRoot = qpc.yuiTree.getRoot();
					for (var pii=0; pii<dm.panels[pi].items.length; pii++) {
						var withRenderData = qpc._addConceptVisuals(dm.panels[pi].items[pii], tvRoot, true);
						dm.panels[pi].items[pii] = withRenderData;
					}
				}
				// redraw the Query Tool GUI
				i2b2.CRC.ctrlr.QT._redrawPanelCount();
				i2b2.CRC.ctrlr.QT.doScrollFirst();
				// hide the loading mask
				i2b2.h.LoadingMask.hide();
			}
		}
		// AJAX CALL
		i2b2.CRC.ajax.getRequestXml_fromQueryMasterId("CRC:QueryTool", { qm_key_value: qm_id }, scopedCallback);		
	}

// ================================================================================================== //
	this.doQueryRun = function() {
		// function to build and run query 
		if (this.queryIsRunning) { 
			alert('A query is already running.\n Please wait until the currently running query has finished.');
			return void(0);
		}
		
		if (i2b2.CRC.model.queryCurrent.panels.length < 1) {
			alert('You must enter at least one concept to run a query.');
			return void(0);
		}
		
		// make sure a shrine topic has been selected
		if (i2b2.PM.model.shrine_domain) {
			var topicSELECT = $('queryTopicSelect');
			if (topicSELECT.selectedIndex == null || topicSELECT.selectedIndex == 0) {
				alert('You must select a Topic to run a SHRINE query.');
				return void(0);
			}
			var topicid = topicSELECT.options[topicSELECT.selectedIndex].value;
		}

		// callback for dialog submission
		var handleSubmit = function() {
			// submit value(s)
			if(this.submit()) {
				// run the query
				var t = $('dialogQryRun');
				var queryNameInput = t.select('INPUT.inputQueryName')[0];
				var options = {};
				var t2 = t.select('INPUT.chkQueryType');
				for (var i=0;i<t2.length; i++) {
					options['chk_'+t2[i].value] = t2[i].checked;
				}
				i2b2.CRC.ctrlr.QT._queryRun(queryNameInput.value, options);
			}
		}
		// display the query name input dialog
		this._queryPromptRun(handleSubmit);
		// autogenerate query name
		var myDate=new Date();
		var ds = myDate.toUTCString();
		var ts = ds.substring(ds.length-4,ds.length-12);
		var defQuery = this._getQueryXML.call(this);
		var qn = defQuery.queryAutoName+'@'+ts;
		// display name
		var queryNameInput = $('dialogQryRun').select('INPUT.inputQueryName')[0];
		queryNameInput.value = qn;
	}

// ================================================================================================== //
	this._queryRun = function(inQueryName, options) {
		// make sure name is not blank
		if (inQueryName.blank()) { 
			alert('Cannot run query with without providing a name!');
			return;
		}
		if(!options.chk_PRS && !options.chk_PRC) {
			alert('You must select at least one query result type to return!');
			return;
		}
		
		// Query Parameters
		var query_definition = this._getQueryXML(inQueryName);
		var params = {
			result_wait_time: i2b2.CRC.view.QT.params.queryTimeout,
			psm_query_definition: query_definition.queryXML
		}
		// SHRINE topic if we are running SHRINE query
		if (i2b2.h.isSHRINE()) {
			var topicSELECT = $('queryTopicSelect');
			if (topicSELECT.selectedIndex == null || topicSELECT.selectedIndex == 0) {
				alert("Please select a Topic to run the query.");
				return false;
			}
			params.shrine_topic = "<shrine><queryTopicID>"+topicSELECT.options[topicSELECT.selectedIndex].value+"</queryTopicID></shrine>";
		}
		
		// generate the result_output_list (for 1.3 backend)
		var result_output = "";
		var i=0;
		if (options.chk_PRS) {
			i++;
			result_output += '<result_output priority_index="'+i+'" name="patientset"/>';
		}
		if (options.chk_PRC) {
			i++;
			result_output += '<result_output priority_index="'+i+'" name="patient_count_xml"/>';
		}
		params.psm_result_output = '<result_output_list>'+result_output+'</result_output_list>';
		
		// create query object
		i2b2.CRC.ctrlr.currentQueryStatus = new i2b2.CRC.ctrlr.QueryStatus($('infoQueryStatusText'));
		i2b2.CRC.ctrlr.currentQueryStatus.startQuery(inQueryName, params);		
	}


// ================================================================================================== //
	this._queryRunningTime = function() {
		if (i2b2.CRC.ctrlr.QT.queryIsRunning) {
			var d = new Date();
			var t = Math.floor((d.getTime() - queryStartTime)/100)/10;
			var el = $('numSeconds');
			if (el) {
				var s = t.toString();
				if (s.indexOf('.') < 0) {
					s += '.0';
				}
				el.innerHTML = s;
				window.setTimeout('i2b2.CRC.ctrlr.QT._queryRunningTime()',100);
			}
		}
	}


// ================================================================================================== //
	this._queryPromptRun = function(handleSubmit) {
		if (!i2b2.CRC.view.dialogQryRun) {
			var handleCancel = function() {
				this.cancel();
			};
			var loopBackSubmit = function() {
				i2b2.CRC.view.dialogQryRun.submitterFunction();
			};
			i2b2.CRC.view.dialogQryRun = new YAHOO.widget.SimpleDialog("dialogQryRun", {
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					modal: true,
					zindex: 700,
					buttons: [{
						text: "OK",
						handler: loopBackSubmit,
						isDefault: true
					}, {
						text: "Cancel",
						handler: handleCancel
					}]
				});
			$('dialogQryRun').show();
			i2b2.CRC.view.dialogQryRun.validate = function(){
				// now process the form data
				var msgError = '';
				var queryNameInput = $('dialogQryRun').select('INPUT.inputQueryName')[0];
				if (!queryNameInput || queryNameInput.value.blank()) {
					alert('Please enter a name for this query.');
					return false;
				}
				return true;
			};
			i2b2.CRC.view.dialogQryRun.render(document.body);
		}
		// manage the event handler for submit
		delete i2b2.CRC.view.dialogQryRun.submitterFunction;
		i2b2.CRC.view.dialogQryRun.submitterFunction = handleSubmit;
		// display the dialoge
		i2b2.CRC.view.dialogQryRun.center();
		i2b2.CRC.view.dialogQryRun.show();
	}


// ================================================================================================== //
	this._queryPromptName = function(handleSubmit) {
		if (!i2b2.CRC.view.dialogQmName) {
			var handleCancel = function() {
				this.cancel();
			};
			var loopBackSubmit = function() {
				i2b2.CRC.view.dialogQmName.submitterFunction();
			};
			i2b2.CRC.view.dialogQmName = new YAHOO.widget.SimpleDialog("dialogQmName", {
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					modal: true,
					zindex: 700,
					buttons: [{
						text: "OK",
						handler: loopBackSubmit,
						isDefault: true
					}, {
						text: "Cancel",
						handler: handleCancel
					}]
				});
			$('dialogQmName').show();
			i2b2.CRC.view.dialogQmName.validate = function(){
				// now process the form data
				var msgError = '';
				var queryNameInput = $('inputQueryName');
				if (!queryNameInput || queryNameInput.value.blank()) {
					alert('Please enter a name for this query.');
					return false;
				}
				return true;
			};
			i2b2.CRC.view.dialogQmName.render(document.body);
		}
		// manage the event handler for submit
		delete i2b2.CRC.view.dialogQmName.submitterFunction;
		i2b2.CRC.view.dialogQmName.submitterFunction = handleSubmit;
		// display the dialoge
		i2b2.CRC.view.dialogQmName.center();
		i2b2.CRC.view.dialogQmName.show();
	}

// ================================================================================================== //
	this._getQueryXML = function(queryName) {
		var i;
		var el;
		var concept;
		var panel_list = i2b2.CRC.model.queryCurrent.panels
		var panel_cnt = panel_list.length;
		var auto_query_name_len = 15;
		var auto_query_name = '';
		if (panel_cnt > 0) {
			auto_query_name_len = Math.floor(15/panel_cnt);
			if (auto_query_name_len < 1) {auto_query_name_len = 1;}
		}
		// build Query XML
		var s = '<query_definition>\n';
		s += '\t<query_name>' + i2b2.h.Escape(queryName) + '</query_name>\n';
		s += '\t<specificity_scale>0</specificity_scale>\n';
		if (i2b2.PM.model.shrine_domain) { s += '\t<use_shrine>1</use_shrine>\n'; }
		for (var p = 0; p < panel_cnt; p++) {
			s += '\t<panel>\n';
			s += '\t\t<panel_number>' + (p+1) + '</panel_number>\n';
			// date range constraints
			if (panel_list[p].dateFrom) {
				s += '\t\t<panel_date_from>'+panel_list[p].dateFrom.Year+'-'+padNumber(panel_list[p].dateFrom.Month,2)+'-'+padNumber(panel_list[p].dateFrom.Day,2)+'Z</panel_date_from>\n';
			}
			if (panel_list[p].dateTo) {
				s += '\t\t<panel_date_to>'+panel_list[p].dateTo.Year+'-'+padNumber(panel_list[p].dateTo.Month,2)+'-'+padNumber(panel_list[p].dateTo.Day,2)+'Z</panel_date_to>\n';
			}
			// Exclude constraint (invert flag)
			if (panel_list[p].exclude) {
				s += '\t\t<invert>1</invert>\n';
			} else {
				s += '\t\t<invert>0</invert>\n';
			}
			// Occurs constraint
			s += '\t\t<total_item_occurrences>'+((panel_list[p].occurs*1)+1)+'</total_item_occurrences>\n';
			// Concepts
			for (i=0; i < panel_list[p].items.length; i++) {
				var sdxData = panel_list[p].items[i];
				s += '\t\t<item>\n';
				s += '\t\t\t<hlevel>' + sdxData.origData.level + '</hlevel>\n';
				s += '\t\t\t<item_name>' + sdxData.origData.name + '</item_name>\n';
				s += '\t\t\t<item_key>' + sdxData.origData.key + '</item_key>\n';
				s += '\t\t\t<tooltip>' + sdxData.origData.tooltip + '</tooltip>\n';
				s += '\t\t\t<class>ENC</class>\n';
				s += '\t\t\t<constrain_by_date>\n';
				if (panel_list[p].dateFrom) {
					s += '\t\t<date_from>'+panel_list[p].dateFrom.Year+'-'+padNumber(panel_list[p].dateFrom.Month,2)+'-'+padNumber(panel_list[p].dateFrom.Day,2)+'Z</date_from>\n';
				}
				if (panel_list[p].dateTo) {
					s += '\t\t<date_to>'+panel_list[p].dateTo.Year+'-'+padNumber(panel_list[p].dateTo.Month,2)+'-'+padNumber(panel_list[p].dateTo.Day,2)+'Z</date_to>\n';
				}
				s += '\t\t\t</constrain_by_date>\n';
				s += '\t\t\t<item_icon>'+sdxData.origData.hasChildren+'</item_icon>\n';
				try {
					var t = i2b2.h.XPath(sdxData.origData.xmlOrig,'descendant::synonym_cd/text()');
					t = (t[0].nodeValue=="Y");
				} catch(e) {
					var t = "false";
				}
				s += '\t\t\t<item_is_synonym>'+t+'</item_is_synonym>\n';
				if (sdxData.LabValues) {
					s += '\t\t\t<constrain_by_value>\n';
					var lvd = sdxData.LabValues;
					switch(lvd.MatchBy) {
						case "FLAG":
							s += '\t\t\t\t<value_type>FLAG</value_type>\n';
							s += '\t\t\t\t<value_operator>EQ</value_operator>\n';
							s += '\t\t\t\t<value_constraint>'+i2b2.h.Escape(lvd.ValueFlag)+'</value_constraint>\n';
							break;
						case "VALUE":
							s += '\t\t\t\t<value_type>'+lvd.GeneralValueType+'</value_type>\n';
							if (lvd.GeneralValueType=="ENUM") {
								var sEnum = [];
								for (var i2=0;i2<lvd.ValueEnum.length;i2++) {
									sEnum.push(i2b2.h.Escape(lvd.ValueEnum[i2]));
								}
								sEnum = sEnum.join("\", \"");
								sEnum = '("'+sEnum+'")';
								s += '\t\t\t\t<value_type>TEXT</value_type>\n';
								s += '\t\t\t\t<value_constraint>'+sEnum+'</value_constraint>\n';
								s += '\t\t\t\t<value_operator>IN</value_operator>\n';								
							} else {
								s += '\t\t\t\t<value_unit_of_measure>'+lvd.UnitsCtrl+'</value_unit_of_measure>\n';
								s += '\t\t\t\t<value_operator>'+lvd.NumericOp+'</value_operator>\n';
								if (lvd.NumericOp == 'BETWEEN') {
									s += '\t\t\t\t<value_constraint>'+i2b2.h.Escape(lvd.ValueLow)+' and '+i2b2.h.Escape(lvd.ValueHigh)+'</value_constraint>\n';
								} else {
									s += '\t\t\t\t<value_constraint>'+i2b2.h.Escape(lvd.Value)+'</value_constraint>\n';
								}
							}
							break;
						case "":
							break;
					}
					s += '\t\t\t</constrain_by_value>\n';
				}
				s += '\t\t</item>\n';
				if (i==0) {
					auto_query_name += sdxData.origData.name.substring(0,auto_query_name_len);
					if (p < panel_cnt-1) {auto_query_name += '-';}
				}
			}
			s += '\t</panel>\n';
		}
		s += '</query_definition>\n';
		this.queryMsg = {};
		this.queryMsg.queryAutoName = auto_query_name;
		if (undefined===queryName) {
			this.queryMsg.queryName = this.queryNameDefault;
		} else {
			this.queryMsg.queryName = queryName;				
		}
		this.queryMsg.queryXML = s;
		return(this.queryMsg);
	}

// ================================================================================================== //
	this.panelAdd = function(yuiTree) {
		// this function is used to create a new panel, it initializes the data structure in the 
		if (!i2b2.CRC.model.queryCurrent.panels) { i2b2.CRC.model.queryCurrent.panels = []}
		var dm = i2b2.CRC.model.queryCurrent;
		var pi = dm.panels.length;
		// setup the data model for this panel
		dm.panels[pi] = {};
		dm.panels[pi].dateTo = false;
		dm.panels[pi].dateFrom = false;
		dm.panels[pi].exclude = false;
		dm.panels[pi].occurs = '0';
		dm.panels[pi].items = [];
		// create a treeview root node and connect it to the treeview controller
		dm.panels[pi].tvRootNode = new YAHOO.widget.RootNode(this.yuiTree);
		yuiTree.root = dm.panels[pi].tvRootNode;
		dm.panels[pi].tvRootNode.tree = yuiTree;
		yuiTree.setDynamicLoad(i2b2.CRC.ctrlr.QT._loadTreeDataForNode,1);
		// update the count on the GUI
		this._redrawPanelCount();
		// return a reference to the new panel object
		this.doSetQueryName.call(this,'');
		return dm.panels[pi];
	}

// ================================================================================================== //
	this._loadTreeDataForNode = function(node, onCompleteCallback) {
		i2b2.sdx.Master.LoadChildrenFromTreeview(node, onCompleteCallback);
	}

// ================================================================================================== //
	this.ToggleNode = function(divTarg, divTreeID) {
		// get the i2b2 data from the yuiTree node
		var tvTree = YAHOO.widget.TreeView.findTreeByChildDiv(divTarg);  // this is a custom extention found in "hive_helpers.js"
		var tvNode = tvTree.getNodeByProperty('nodeid', divTarg.id);
		tvNode.toggle();
	}

// ================================================================================================== //
	this.panelDelete = function(index) {
		// alter the data model's panel elements
		var dm = i2b2.CRC.model.queryCurrent;
		if(index <0 || index>=dm.panels.length) { return false;}
		dm.panels.splice(index,1);
		// redraw the panels
		this.doShowFrom(this.panelControllers[0].panelCurrentIndex);
		// BUG FIX: force the panels to fully reattach the yuiRootNode to the controllers
		for (var i=0; i<this.panelControllers.length; i++) {
			this.panelControllers[i].doRedraw();
		}		
		this._redrawPanelCount();
		this.doSetQueryName.call(this,'');
	}

// ================================================================================================== //
	this.doShowFrom = function(index_offset) {
		// have all panel controllers redraw using new index offest
		if (index_offset===false) { return true; }
		if (index_offset < 0) { index_offset = 0; }
		for (var i=0; i<3; i++) {
			this.panelControllers[i].refTitle.innerHTML = "Group "+(index_offset+i+1);
			this.panelControllers[i].setPanelRecord(index_offset+i);
			if (i > 0) {
				if (index_offset+i <= i2b2.CRC.model.queryCurrent.panels.length) {
					$('queryBalloonAnd'+(i)).style.display = 'block';
				} else {
					$('queryBalloonAnd'+(i)).style.display = 'none';
				}
			}
		}
		this._redrawScrollBtns();
	}

// ================================================================================================== //
	this._redrawAllPanels = function() {
		for (var i=0; i<3; i++) {
			this.panelControllers[i].doRedraw();
			if (i > 0) {
				if (this.panelControllers[i].panelCurrentIndex-1 < i2b2.CRC.model.queryCurrent.panels.length) {
					$('queryBalloonAnd'+(i)).style.display = 'block';
				} else {
					$('queryBalloonAnd'+(i)).style.display = 'none';
				}
			}
		}
	}

// ================================================================================================== //
	this._redrawPanelCount = function() {
		var c = i2b2.CRC.model.queryCurrent.panels.length; 
		if (c == 1) {
			var s = '1 Group';
		} else {
			var s = c + ' Groups';
		}
		$('groupCount').innerHTML = s;
	}

// ================================================================================================== //
	this.doScrollFirst = function() {
		this.doShowFrom(0);
	}

// ================================================================================================== //
	this.doScrollPrev = function() {
		var i = this.panelControllers[0].panelCurrentIndex - 1;
		if (i<0) { i=0; }
		this.doShowFrom(i);
	}

// ================================================================================================== //
	this.doScrollNext = function() {
		var i = this.panelControllers[0].panelCurrentIndex + 1;
		var dm = i2b2.CRC.model.queryCurrent;
		if (i > (dm.panels.length-3)) { i=dm.panels.length-3; }
		this.doShowFrom(i);
	}

// ================================================================================================== //
	this.doScrollLast = function() {
		var i = i2b2.CRC.model.queryCurrent.panels.length - 3;
		if (i<0) { i = 0; }
		this.doShowFrom(i);
	}

// ================================================================================================== //
	this.doScrollNew = function() {
		var i = i2b2.CRC.model.queryCurrent.panels.length - 2;
		if (i<0) { i = 0; }
		this.doShowFrom(i);
	}

// ================================================================================================== //
	this._redrawScrollBtns = function() {
		// enable & disable scroll buttons (at least the look of the buttons)
		var dir = i2b2.hive.cfg.urlFramework + 'cells/CRC/assets/';
		if (i2b2.CRC.ctrlr.QT.panelControllers[0].panelCurrentIndex == 0) {
			$('panelScrollFirst').src = dir+"QryTool_b_first_hide.gif";
			$('panelScrollPrev').src = dir+"QryTool_b_prev_hide.gif";
		} else {
			$('panelScrollFirst').src = dir+"QryTool_b_first.gif";
			$('panelScrollPrev').src = dir+"QryTool_b_prev.gif";
		}
		if ((i2b2.CRC.model.queryCurrent.panels.length - i2b2.CRC.ctrlr.QT.panelControllers[0].panelCurrentIndex) > 3) {
			$('panelScrollNext').src = dir+"QryTool_b_next.gif";
			$('panelScrollLast').src = dir+"QryTool_b_last.gif";
		} else {
			$('panelScrollNext').src = dir+"QryTool_b_next_hide.gif";
			$('panelScrollLast').src = dir+"QryTool_b_last_hide.gif";
		}
	}
}

console.timeEnd('execute time');
console.groupEnd();