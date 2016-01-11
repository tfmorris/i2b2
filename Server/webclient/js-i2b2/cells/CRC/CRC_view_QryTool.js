/**
 * @projectDescription	View controller for CRC Query Tool window.
 * @inherits 	i2b2.CRC.view
 * @namespace	i2b2.CRC.view.QT
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > view > Main');
console.time('execute time');


// create and save the screen objects
i2b2.CRC.view['QT'] = new i2b2Base_cellViewController(i2b2.CRC, 'QT');;
// define the option functions
// ================================================================================================== //
i2b2.CRC.view.QT.showOptions = function(subScreen) {
	if (!this.modalOptions) {
		var handleSubmit = function() {
			// submit value(s)
			if(this.submit()) {
				var tmpValue = parseInt($('QryTimeout').value,10);
				i2b2.CRC.view['QT'].params.queryTimeout = tmpValue;
	//			var tmpValue = parseInt($('MaxChldDisp').value,10);
	//			i2b2.CRC.view['QT'].params.maxChildren = tmpValue;
			}
		}
		var handleCancel = function() {
			this.cancel();
		}
		this.modalOptions = new YAHOO.widget.SimpleDialog("optionsQT",
		{ width : "400px", 
			fixedcenter : true, 
			constraintoviewport : true, 
			modal: true,
			zindex: 700,
			buttons : [ { text:"OK", handler:handleSubmit, isDefault:true }, 
				    { text:"Cancel", handler:handleCancel } ] 
		} ); 
		$('optionsQT').show();
		this.modalOptions.validate = function() {
			// now process the form data
			var msgError = '';
	//		var tmpValue = parseInt($('MaxChldDisp').value,10);
	//		if (!isNaN(tmpValue) && tmpValue <= 0) {
	//			msgError += "The max number of Children to display must be a whole number larger then zero.\n";
	//		}
			var tmpValue = parseInt($('QryTimeout').value,10);
			if (!isNaN(tmpValue) && tmpValue <= 0) {
				msgError += "The the query timeout period must be a whole number larger then zero.\n";
			}
			if (msgError) {
				alert(msgError);
				return false;
			}
			return true;
		};
		this.modalOptions.render(document.body);
	}
	this.modalOptions.show();
	// load settings
//	$('MaxChldDisp').value = this.params.maxChildren;
	$('QryTimeout').value = this.params.queryTimeout;
}

// ================================================================================================== //
i2b2.CRC.view.QT.ContextMenuPreprocess = function(p_oEvent) {
	var clickId = false;
	var clickPanel = false;
	var isDone = false;
	var currentNode = this.contextEventTarget;
	var doNotShow = false;
	
	while (!isDone) {
		// save the first DOM node found with an ID
		if (currentNode.id && !clickId)  {
			clickId = currentNode.id;
		}
		// save and exit when we find the linkback to the panel controller
		if (currentNode.linkbackPanelController) {
			// we are at the tree root... 
			var clickPanel = currentNode.linkbackPanelController;
			isDone = true;
		}
		if (currentNode.parentNode) {
			currentNode = currentNode.parentNode;
		} else {
			// we have recursed up the tree to the window/document DOM...
			isDone = true;
		}
	}
	if (!clickId || !clickPanel) {
		// something is missing, exit
		this.cancel();
		return;
	}
	// see if the ID maps back to a treenode with SDX data
	var tvNode = clickPanel.yuiTree.getNodeByProperty('nodeid', clickId);
	if (tvNode) {
		if (!Object.isUndefined(tvNode.data.i2b2_SDX)) {
			// Make sure the clicked node is at the root level
			if (tvNode.parent == clickPanel.yuiTree.getRoot()) {
				if (p_oEvent == "beforeShow") {
					i2b2.CRC.view.QT.contextRecord = tvNode.data.i2b2_SDX;
					i2b2.CRC.view.QT.contextPanelCtrlr = clickPanel;
					// custom build the context menu according to the concept that was clicked
					var mil = [];
					var op = i2b2.CRC.view.QT;
					// all nodes can be deleted
					mil.push( { text: "Delete", onclick: { fn: op.ContextMenuRouter, obj: 'delete' }} );
					// For lab tests...
					var lvMetaDatas = i2b2.h.XPath(i2b2.CRC.view.QT.contextRecord.origData.xmlOrig, 'metadataxml/ValueMetadata[string-length(Loinc)>0]');
					if (lvMetaDatas.length > 0) {
						mil.push( { text: "Set Value...", onclick: { fn: op.ContextMenuRouter, obj: 'labvalues' }} );
					}
					i2b2.CRC.view.QT.ContextMenu.clearContent();
					i2b2.CRC.view.QT.ContextMenu.addItems(mil);
					i2b2.CRC.view.QT.ContextMenu.render();
				}
			} else {
				// not root level node
				doNotShow = true;
			}
		} else {
			// no SDX data
			doNotShow = true;
		}
	} else {
		// not a treenode
		doNotShow = true;
	}
	if (doNotShow) {
		if (p_oEvent == "beforeShow") { i2b2.CRC.view.QT.ContextMenu.clearContent(); }
		if (p_oEvent == "triggerContextMenu") { this.cancel(); }
	}
}

// ================================================================================================== //
i2b2.CRC.view.QT.ContextMenuRouter = function(a, b, actionName) {
	// this is used to route the event to the correct handler
	var op = i2b2.CRC.view.QT;  // object path
	var cdat = { // context node data
		data: op.contextRecord,
		ctrlr: op.contextPanelCtrlr
	};
	// route accordingly
	switch(actionName) {
		case "delete":
			// delete item from the panel
			cdat.ctrlr._deleteConcept(cdat.data.sdxInfo.sdxKeyValue, cdat.data);
			break;
		case "labvalues":
			cdat.ctrlr.showLabValues(cdat.data.sdxInfo.sdxKeyValue, cdat.data);
			break;
		default:
			alert('context event was not found for event "'+actionName+'"');
	}
 }


// ================================================================================================== //
i2b2.CRC.view.QT.ZoomView = function() {
	i2b2.hive.MasterView.toggleZoomWindow("QT");
}

// ================================================================================================== //
i2b2.CRC.view.QT.Resize = function(e) {
	var ds = document.viewport.getDimensions();
	var w = ds.width;
	var h = ds.height;
	if (w < 840) {w = 840;}
	if (h < 517) {h = 517;}
	// resize our visual components
	$('crcQueryToolBox').style.left = w-550;
	if (i2b2.WORK && i2b2.WORK.isLoaded) {
		var z = h - 392 + 44;
		if (i2b2.CRC.view.QT.isZoomed) { z += 196 - 44; }	
	} else {
		var z = h - 392;
		if (i2b2.CRC.view.QT.isZoomed) { z += 196; }
	}
	// display the topic selector bar if we are in SHRINE-mode
 	if (i2b2.h.isSHRINE()) {
		$('queryTopicPanel').show();
		z = z - 28;
	}
			
	$('QPD1').style.height = z;
	$('QPD2').style.height = z;
	$('QPD3').style.height = z;	
}
YAHOO.util.Event.addListener(window, "resize", i2b2.CRC.view.QT.Resize, i2b2.CRC.view.QT);


// This is done once the entire cell has been loaded
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
i2b2.events.afterCellInit.subscribe(
	(function(en,co) {
		if (co[0].cellCode=='CRC') {
// ================================================================================================== //
			console.debug('[EVENT CAPTURED i2b2.events.afterCellInit]');
			// register the query panels as valid DragDrop targets for Ontology Concepts (CONCPT) and query master (QM) objects
			var op_trgt = {dropTarget:true};
			i2b2.sdx.Master.AttachType('QPD1', 'CONCPT', op_trgt);
			i2b2.sdx.Master.AttachType('QPD2', 'CONCPT', op_trgt);
			i2b2.sdx.Master.AttachType('QPD3', 'CONCPT', op_trgt);
			i2b2.sdx.Master.AttachType('QPD1', 'QM', op_trgt);
			i2b2.sdx.Master.AttachType('QPD2', 'QM', op_trgt);
			i2b2.sdx.Master.AttachType('QPD3', 'QM', op_trgt);
			i2b2.sdx.Master.AttachType('queryName', 'QM', op_trgt);
			
			//======================= <Define Hover Handlers> =======================
			var funcHovOverQM = function(e, id, ddProxy) {
				var el = $(id);
				 // apply DragDrop targeting CCS
				var targets = YAHOO.util.DDM.getRelated(ddProxy, true);
				for (var i=0; i<targets.length; i++) {
					Element.addClassName(targets[i]._domRef,"ddQMTarget");
				} 
			}
			var funcHovOutQM = function(e, id, ddProxy) {
				var el = $(id);
				 // apply DragDrop targeting CCS
				var targets = YAHOO.util.DDM.getRelated(ddProxy, true);
				for (var i=0; i<targets.length; i++) {
					Element.removeClassName(targets[i]._domRef,"ddQMTarget");
				} 
			}
			var funcHovOverCONCPT = function(e, id, ddProxy) {
				var el = $(id);
				if (Object.isUndefined(el.linkbackPanelController)) { return false;}
				var panelController = el.linkbackPanelController;
				// see if the panel controller is enabled
				if (panelController.isActive == 'Y') {										
					Element.addClassName(panelController.refDispContents,'ddCONCPTTarget');
				}
			}
			var funcHovOutCONCPT = function(e, id, ddProxy) {
				var el = $(id);
				if (Object.isUndefined(el.linkbackPanelController)) { return false;}
				var panelController = el.linkbackPanelController;
				// see if the panel controller is enabled
				if (panelController.isActive == 'Y') {
					Element.removeClassName(panelController.refDispContents,'ddCONCPTTarget');
				}
			}
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'onHoverOut', funcHovOutQM);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'onHoverOut', funcHovOutQM);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'onHoverOut', funcHovOutQM);
			i2b2.sdx.Master.setHandlerCustom('queryName', 'QM', 'onHoverOut', funcHovOutQM);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'onHoverOver', funcHovOverQM);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'onHoverOver', funcHovOverQM);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'onHoverOver', funcHovOverQM);
			i2b2.sdx.Master.setHandlerCustom('queryName', 'QM', 'onHoverOver', funcHovOverQM);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'onHoverOut', funcHovOutCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'onHoverOver', funcHovOverCONCPT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'onHoverOver', funcHovOverCONCPT);
			//======================= <Define Drop Handlers> =======================

			//======================= <Define Drop Handlers> =======================
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[0];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[1];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'DropHandler', (function(sdxData) { 
				sdxData = sdxData[0];	// only interested in first record
				var t = i2b2.CRC.ctrlr.QT.panelControllers[2];
				if (t.isActive=="Y") { t.doDrop(sdxData); }
			}));
						
			var funcATN = function(yuiTree, yuiParentNode, sdxDataPack, callbackLoader) { 
				var myobj = { html: sdxDataPack.renderData.html, nodeid: sdxDataPack.renderData.htmlID}
				// if the treenode we are appending to is the root node then do not show the [+] infront
				if (yuiTree.getRoot() == yuiParentNode) {
					var tmpNode = new YAHOO.widget.HTMLNode(myobj, yuiParentNode, false, false);
				} else {
					var tmpNode = new YAHOO.widget.HTMLNode(myobj, yuiParentNode, false, true);
				}
				if (sdxDataPack.renderData.iconType != 'CONCPT_item' && !Object.isUndefined(callbackLoader)) {
					// add the callback to load child nodes
					sdxDataPack.sdxInfo.sdxLoadChildren = callbackLoader;
				}
				tmpNode.data.i2b2_SDX= sdxDataPack;
				tmpNode.toggle = function() {
					if (!this.tree.locked && ( this.hasChildren(true) ) ) {
							var data = this.data.i2b2_SDX.renderData;
							var img = this.getContentEl();
							img = Element.select(img,'img')[0];
							if (this.expanded) { 
								img.src = data.icon;
								this.collapse(); 
							} else { 
								img.src = data.iconExp;
								this.expand(); 
							}
						}
				};
				if (sdxDataPack.renderData.iconType == 'CONCPT_leaf' || !sdxDataPack.renderData.canExpand) { tmpNode.dynamicLoadComplete = true; }
			}
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'AppendTreeNode', funcATN);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'AppendTreeNode', funcATN);

			var funcQMDH = function(sdxData) {
				sdxData = sdxData[0];	// only interested in first record
				// pass the QM ID to be loaded
				var qm_id = sdxData.sdxInfo.sdxKeyValue;
				i2b2.CRC.ctrlr.QT.doQueryLoad(qm_id)
			};
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'QM', 'DropHandler', funcQMDH);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'QM', 'DropHandler', funcQMDH);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'QM', 'DropHandler', funcQMDH);
			i2b2.sdx.Master.setHandlerCustom('queryName', 'QM', 'DropHandler', funcQMDH);
			//======================= </Define Drop Handlers> =======================
			
			
			// ========= Override default LoadChildrenFromTreeview handler (we need this so that we can properly capture the XML request/response messages) ========= 
			var funcLCFT = function(node, onCompleteCallback) {
				var scopedCallback = new i2b2_scopedCallback();
				scopedCallback.scope = node.data.i2b2_SDX;
				scopedCallback.callback = function(results) {
					var cl_node = node;
					var cl_onCompleteCB = onCompleteCallback;
					var cl_options = options;
					// THIS function is used to process the AJAX results of the getChild call
					//		results data object contains the following attributes:
					//			refXML: xmlDomObject <--- for data processing
					//			msgRequest: xml (string)
					//			msgResponse: xml (string)
					//			error: boolean
					//			errorStatus: string [only with error=true]
					//			errorMsg: string [only with error=true]
						
					
		// <THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE Query Tool CONTROL!>
					i2b2.CRC.view.QT.queryResponse = results.msgResponse;
					i2b2.CRC.view.QT.queryRequest = results.msgRequest;
					i2b2.CRC.view.QT.queryUrl = results.msgUrl;
		// </THIS IS WHY WE ARE CREATING CUSTOMER HANDLERS FOR THE QueryTool CONTROL!>					

					// clear the drop-lock so the node can be requeried if anything bad happens below
					node.data.i2b2_dropLock = false;

					
					// handle any errors
					if (results.error) {
						// process the specific error
						var errorCode = results.refXML.getElementsByTagName('status')[0].firstChild.nodeValue;
						if (errorCode == "MAX_EXCEEDED") {
							var eaction = confirm("The number of children in this node exceeds the maximum number you specified in options.\n Displaying all children may take a long time to do.");
						}
						else {
							alert("The following error has occurred:\n" + errorCode);
						}
						// re-fire the call with no max limit if the user requested so
						if (eaction) {
							var mod_options = Object.clone(cl_options);
							delete mod_options.ont_max_records;
							i2b2.ONT.ajax.GetChildConcepts("CRC:QueryTool", mod_options, scopedCallback);
							return true;
						}
						// ROLLBACK the tree changes
						cl_onCompleteCB();
						// reset dynamic load state for the node (total hack of YUI Treeview)
						node.collapse();
						node.dynamicLoadComplete = false;
						node.expanded = false;
						node.childrenRendered = false;
						node._dynLoad = true;
						// uber-elite code (fix the style settings)
						var tc = node.getToggleEl().className;
						tc = tc.substring(0, tc.length - 1) + 'p';
						node.getToggleEl().className = tc;
						// fix the icon image
						var img = node.getContentEl();
						img = Element.select(img, 'img')[0];
						img.src = node.data.i2b2_SDX.sdxInfo.icon;
						return false;
					}
					
					var c = results.refXML.getElementsByTagName('concept');
					for(var i=0; i<1*c.length; i++) {
						var o = new Object;
						o.xmlOrig = c[i];
						o.name = i2b2.h.getXNodeVal(c[i],'name');
						o.hasChildren = i2b2.h.getXNodeVal(c[i],'visualattributes').substring(0,2);
						o.level = i2b2.h.getXNodeVal(c[i],'level');
						o.key = i2b2.h.getXNodeVal(c[i],'key');
						o.tooltip = i2b2.h.getXNodeVal(c[i],'tooltip');
						o.icd9 = '';
						o.table_name = i2b2.h.getXNodeVal(c[i],'tablename');
						o.column_name = i2b2.h.getXNodeVal(c[i],'columnname');
						o.operator = i2b2.h.getXNodeVal(c[i],'operator');
						o.dim_code = i2b2.h.getXNodeVal(c[i],'dimcode');
						// append the data node
						var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',o);
						var renderOptions = {
							title: o.name,
							dblclick: "i2b2.ONT.view.nav.ToggleNode(this,'"+cl_node.tree.id+"')",
							icon: {
								root: "sdx_ONT_CONCPT_root.gif",
								rootExp: "sdx_ONT_CONCPT_root-exp.gif",
								branch: "sdx_ONT_CONCPT_branch.gif",
								branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
								leaf: "sdx_ONT_CONCPT_leaf.gif"
							}
						};
						var sdxRenderData = i2b2.sdx.Master.RenderHTML(cl_node.tree.id, sdxDataNode, renderOptions);
						i2b2.sdx.Master.AppendTreeNode(cl_node.tree, cl_node, sdxRenderData);
					}
					// handle the YUI treeview	
					cl_onCompleteCB();
				}
				
				// fix double loading error via node level dropping-lock
				if (node.data.i2b2_dropLock) { return true; }
				node.data.i2b2_dropLock = true;
				
				var options = {};
				options.ont_max_records = "max='" +i2b2.CRC.cfg.params.maxChildren + "'";
				options.result_wait_time= i2b2.CRC.cfg.params.queryTimeout;
				options.ont_synonym_records = i2b2.ONT.cfg.params.synonyms;
				options.ont_hidden_records = i2b2.ONT.cfg.params.hiddens;
				// parent key
				options.concept_key_value = node.data.i2b2_SDX.sdxInfo.sdxKeyValue;
				i2b2.ONT.ajax.GetChildConcepts("CRC:QueryTool", options, scopedCallback);
			}
			i2b2.sdx.Master.setHandlerCustom('QPD1', 'CONCPT', 'LoadChildrenFromTreeview', funcLCFT);
			i2b2.sdx.Master.setHandlerCustom('QPD2', 'CONCPT', 'LoadChildrenFromTreeview', funcLCFT);
			i2b2.sdx.Master.setHandlerCustom('QPD3', 'CONCPT', 'LoadChildrenFromTreeview', funcLCFT);
			// ========= END Override default LoadChildrenFromTreeview handler (we need this so that we can properly capture the XML request/response messages)  END ========= 

			

			
			

			//======================= <Initialization> =======================
			// Connect the panel controllers to the DOM nodes in the document
			var t = i2b2.CRC.ctrlr.QT;
			for (var i=0; i<3; i++) {
				t.panelControllers[i].ctrlIndex = i;
				t.panelControllers[i].refTitle = $("queryPanelTitle"+(i+1));
				t.panelControllers[i].refButtonExclude = $("queryPanelExcludeB"+(i+1));
				t.panelControllers[i].refButtonDates = $("queryPanelDatesB"+(i+1));
				t.panelControllers[i].refButtonOccurs = $("queryPanelOccursB"+(i+1));
				t.panelControllers[i].refButtonOccursNum = $("QP"+(i+1)+"Occurs");
				t.panelControllers[i].refBalloon = $("queryBalloon"+(i+1));
				t.panelControllers[i].refDispContents = $("QPD"+(i+1));
				// create a instance of YUI Treeview
				if (!t.panelControllers[i].yuiTree) {
					t.panelControllers[i].yuiTree = new YAHOO.widget.TreeView("QPD"+(i+1));
					t.panelControllers[i].yuiTree.setDynamicLoad(t.panelControllers[i]._loadTreeDataForNode,1);
					// forward reference from DOM Node to tree obj
					$("QPD"+(i+1)).tree = t.panelControllers[i].yuiTree;
					// linkback on the treeview to allow it to find its PanelController
					t.panelControllers[i].refDispContents.linkbackPanelController = t.panelControllers[i];
				}
			}
			// display the panels
			t.doScrollFirst();
			t._redrawPanelCount();
			i2b2.CRC.ctrlr.QT.doShowFrom(0);
			i2b2.CRC.ctrlr.history.Refresh();
			//======================= </Initialization> =======================


			// attach the context controller to all panel controllers objects
			var op = i2b2.CRC.view.QT; // object path 
			i2b2.CRC.view.QT.ContextMenu = new YAHOO.widget.ContextMenu( 
					"divContextMenu-QT",  
					{ lazyload: true,
					trigger: [$('QPD1'), $('QPD2'), $('QPD3')],
					itemdata: [
						{ text: "Delete", 		onclick: { fn: op.ContextMenuRouter, obj: 'delete' } },
						{ text: "Lab Values", 	onclick: { fn: op.ContextMenuRouter, obj: 'labvalues' } }
					] }  
			); 
			i2b2.CRC.view.QT.ContextMenu.subscribe("triggerContextMenu", i2b2.CRC.view.QT.ContextMenuPreprocess); 
			i2b2.CRC.view.QT.ContextMenu.subscribe("beforeShow", i2b2.CRC.view.QT.ContextMenuPreprocess); 
// ================================================================================================== //
		}
	})
);


// QueryTool Helper Balloons
// ================================================================================================== //
i2b2.CRC.view.QT.hballoon = {
	canShowQueryBalloons: true,
	delayQueryBalloons: false,
	hideBalloons: function() {
		var thisObj = i2b2.CRC.view.QT.hballoon;
		thisObj.canShowQueryBalloons = false;
		clearTimeout(thisObj.delayQueryBalloons);
		$('queryBalloonBox').hide();
		YAHOO.util.Event.removeListener(document, "mousemove", thisObj.showBalloons);
		YAHOO.util.Event.addListener(document, "mousemove", thisObj.showBalloons);
	},
	showBalloons: function(e) {
		var thisObj = i2b2.CRC.view.QT.hballoon;
		var x = YAHOO.util.Event.getPageX(e);
		var y = YAHOO.util.Event.getPageY(e);
		var elX = parseInt($('crcQueryToolBox').style.left);
		if (isNaN(elX)) {elX = 241;}
		var elY = $('crcQueryToolBox').getHeight();
		if (isNaN(elY)) {elY = 280;}
		elY = elY + 76 - 135;
		if ( (x < elX-5) || (x > elX+524+5) || (y < elY-15) || (y > elY+110) ) {
			if (!thisObj.canShowQueryBalloons) {
				thisObj.canShowQueryBalloons = true;
				thisObj.delayQueryBalloons = setTimeout("i2b2.CRC.view.QT.hballoon._showQueryBalloons()",200);
			}
		} else {
			thisObj.canShowQueryBalloons = false;
			clearTimeout(thisObj.delayQueryBalloons);
		}
	},
	_showQueryBalloons: function() {
		var thisObj = i2b2.CRC.view.QT.hballoon;
		if (thisObj.canShowQueryBalloons) {
			$('queryBalloonBox').show();
			YAHOO.util.Event.removeListener(document, "mousemove", thisObj.showBalloons);
		}
	}
};


// ================================================================================================== //
i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	this.viewMode = newMode;
	switch(newMode) {
		case "Patients":
			this.visible = true;
			$('crcQueryToolBox').show();
			this.Resize();
			break;
		default:
			this.visible = false;
			$('crcQueryToolBox').hide();
			break;
	}
// -------------------------------------------------------
}),'', i2b2.CRC.view.QT);


// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
	newMode = zoomMsg[0];
	if (!newMode.action) { return; }
	if (newMode.action == "ADD") {
		switch (newMode.window) {
			case "QT":
				this.isZoomed = true;
				this.visible = true;
				break;
		}
	} else {
		switch (newMode.window) {
			case "QT":
				this.isZoomed = false;
				this.visible = true;
		}
	}
	this.Resize();
}),'',i2b2.CRC.view.QT);




console.timeEnd('execute time');
console.groupEnd();