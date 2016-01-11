/**
 * @projectDescription	View controller for ONT's "Find Terms" tab.
 * @inherits 	i2b2.ONT.view
 * @namespace	i2b2.ONT.view.find
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: ONT > view > Find');
console.time('execute time');


// create and save the view object
i2b2.ONT.view.find = new i2b2Base_cellViewController(i2b2.ONT, 'find');
i2b2.ONT.view.find.visible = false;
// redefine the option functions
// ================================================================================================== //
i2b2.ONT.view.find.showOptions = function(subScreen) {
	if (!this.modalOptions) {
		var handleSubmit = function() {
			// submit value(s)
			if(this.submit()) {
				i2b2.ONT.view['find'].params.max = parseInt($('ONTFINDMaxQryDisp').value,10);
				i2b2.ONT.view['find'].params.synonyms = $('ONTFINDshowSynonyms').checked;
				i2b2.ONT.view['find'].params.hiddens = $('ONTFINDshowHiddens').checked;
			}
		}
		var handleCancel = function() {
			this.cancel();
		}
		this.modalOptions = new YAHOO.widget.SimpleDialog("optionsOntFind",
		{ width : "400px", 
			fixedcenter : true,
			constraintoviewport : true,
			modal: true,
			zindex: 700,
			buttons : [ { text:"OK", handler:handleSubmit, isDefault:true }, 
				    { text:"Cancel", handler:handleCancel } ] 
		} ); 
		$('optionsOntFind').show();
		this.modalOptions.validate = function() {
			if (parseInt($('ONTFINDMaxQryDisp').value,10) <= 0) {
				alert('You must display at least one child!');
				return false;
			}
			return true;
		};
		this.modalOptions.render(document.body);
	}
	this.modalOptions.show();
	// load settings
	$('ONTFINDMaxQryDisp').value = this.params.max;
	$('ONTFINDshowSynonyms').checked = parseBoolean(this.params.synonyms);
	$('ONTFINDshowHiddens').checked = parseBoolean(this.params.hiddens);			
}


// ================================================================================================== //
i2b2.ONT.view.find.showView = function() {
	$('tabFind').addClassName('active');
	$('ontFindDisp').style.display = 'block';
}

// ================================================================================================== //
i2b2.ONT.view.find.hideView = function() {
	$('tabFind').removeClassName('active');
	$('ontFindDisp').style.display = 'none';
}

// ================================================================================================== //
i2b2.ONT.view.find.selectSubTab = function(tabCode) {
	// toggle between the Navigate and Find Terms tabs
	switch (tabCode) {
		case "names":
			this.currentSubTab = 'names';
			$('ontFindTabName').blur();
			$('ontFindTabName').className = 'findSubTabSelected';
			$('ontFindTabCode').className = 'findSubTab';
			$('ontFindFrameName').show();
			$('ontFindFrameCode').hide();
			$('ontSearchNamesResults').show();
			$('ontSearchCodesResults').hide();
		break;
		case "codes":
			this.currentTab = 'codes';
			$('ontFindTabCode').blur();
			$('ontFindTabName').className = 'findSubTab';
			$('ontFindTabCode').className = 'findSubTabSelected';
			$('ontFindFrameName').hide();
			$('ontFindFrameCode').show();
			$('ontSearchNamesResults').hide();
			$('ontSearchCodesResults').show();
		break;
	}
}

// ================================================================================================== //
i2b2.ONT.view.find.PopulateCategories = function() {		
	// insert the categories option list
	var tns = $('ontFindCategory');
	// clear the list first
	while( tns.hasChildNodes() ) { tns.removeChild( tns.lastChild ); }
	// add the default option
	var tno = document.createElement('OPTION');
	tno.setAttribute('value', '[[[ALL]]]');
	var tnt = document.createTextNode('Any Category');
	tno.appendChild(tnt);
	tns.appendChild(tno);		
	// populate the Categories from the data model
	for (var i=0; i<i2b2.ONT.model.Categories.length; i++) {
		var cat = i2b2.ONT.model.Categories[i];
		// ONT options dropdown
		var cid = cat.key;
		cid = /\\\\\w*\\/.exec(cid);
		cid = cid[0].replace(/\\/g,'');
		tno = document.createElement('OPTION');
		tno.setAttribute('value', cid);
		var tnt = document.createTextNode(cat.name);
		tno.appendChild(tnt);
		tns.appendChild(tno);
	}
}

// ================================================================================================== //
i2b2.ONT.view.find.PopulateSchemes = function() {
	// insert the Codings option list
	var tns = $('ontFindCoding');
	// clear the list first
	while( tns.hasChildNodes() ) { tns.removeChild( tns.lastChild ); }
	// add the default option
	var tno = document.createElement('OPTION');
	tno.setAttribute('value', '');
	var tnt = document.createTextNode('Select a Coding System');
	tno.appendChild(tnt);
	tns.appendChild(tno);		
	// populate the Codings from the data model
	for (var i=0; i<i2b2.ONT.model.Schemes.length; i++) {
		var sc = i2b2.ONT.model.Schemes[i];
		// ONT scheme (codes) dropdown
		tno = document.createElement('OPTION');
		tno.setAttribute('value', sc.key);
		var tnt = document.createTextNode(sc.name);
		tno.appendChild(tnt);
		tns.appendChild(tno);
	}
}

// ================================================================================================== //
i2b2.ONT.view.find.Resize = function(e) {
	// this function provides the resize functionality needed for this screen
	var viewObj = i2b2.ONT.view.main;
	var ds = document.viewport.getDimensions();
	var w = ds.width;
	var h = ds.height;
	if (w < 840) {w = 840;}
	if (h < 517) {h = 517;}
	switch(i2b2.hive.MasterView.getViewMode()) {
		case "Patients":
			if (i2b2.WORK && i2b2.WORK.isLoaded) {
				var z = parseInt((h - 321)/2)-57;
			} else {
				var z = h-362;
			}
			break;
		case "Analysis":
			if (i2b2.WORK && i2b2.WORK.isLoaded) {
				var z = parseInt((h - 321)/2)-57;
			} else {
				var z = h-362;
			}
			break;
		default:
			break;
	}
	if (z) {
		if (i2b2.ONT.view.main.isZoomed) { z = h-166; }
		$('ontSearchNamesResults').style.height = z;
		$('ontSearchCodesResults').style.height = z;
	}
	$('ontFindFrameName').style.height = 44; //h-355
	$('ontFindFrameCode').style.height = 44; //h-355
}

// ================================================================================================== //
console.info("SUBSCRIBED TO [window.resize]");
YAHOO.util.Event.addListener(window, "resize", i2b2.ONT.view.find.Resize, i2b2);


// This is done once the entire cell has been loaded
// ================================================================================================== //
console.info("SUBSCRIBED TO i2b2.events.afterCellInit");
i2b2.events.afterCellInit.subscribe(
	(function(en,co) {
		if (co[0].cellCode=='ONT') {
// ===================================================================
			var thisview = i2b2.ONT.view.find;
			// perform visual actions
			thisview.Resize();
			thisview.selectSubTab('names');
			// perform data actions
			if (!thisview.yuiTreeName) {
				thisview.yuiTreeName = new YAHOO.widget.TreeView("ontSearchNamesResults");
				thisview.yuiTreeName.setDynamicLoad(i2b2.sdx.Master.LoadChildrenFromTreeview,1);
				// register the treeview with the SDX subsystem to be a container for CONCPT objects
				i2b2.sdx.Master.AttachType("ontSearchNamesResults","CONCPT");
			}
			if (!thisview.yuiTreeCode) {
				thisview.yuiTreeCode = new YAHOO.widget.TreeView("ontSearchCodesResults");
				thisview.yuiTreeCode.setDynamicLoad(i2b2.sdx.Master.LoadChildrenFromTreeview,1);
				// register the treeview with the SDX subsystem to be a container for CONCPT objects
				i2b2.sdx.Master.AttachType("ontSearchCodesResults","CONCPT");
			}
			
			// ATTACH TO EVENTS (the target objects may not have been created when this file was loaded)
			// This is done when the cells data model has been changed
			console.info("SUBSCRIBED TO i2b2.ONT.ctrlr.gen.events.onDataUpdate");
			i2b2.ONT.ctrlr.gen.events.onDataUpdate.subscribe(
				(function(en,co) {
					console.group("[EVENT CAPTURED i2b2.ONT.ctrlr.gen.events.onDataUpdate]");
					console.dir(co[0]);
					console.groupEnd();
					var dm_loc = co[0].DataLocation;
					var dm_ptr = co[0].DataRef;
					if (dm_loc=='i2b2.ONT.model.Categories') {
						console.debug("Processing Category update");
						i2b2.ONT.view.find.PopulateCategories();
					}
					if (dm_loc=='i2b2.ONT.model.Schemes') {
						console.debug("Processing Schemes update");
						i2b2.ONT.view.find.PopulateSchemes();
					}
				})
			);
// ===================================================================
		}
	})
);


i2b2.events.changedViewMode.subscribe((function(eventTypeName, newMode) {
// -------------------------------------------------------
	newMode = newMode[0];
	this.viewMode = newMode;
	switch(newMode) {
		case "Patients":
		case "Analysis":
			var wlst = i2b2.hive.MasterView.getZoomWindows();
			if (wlst.indexOf("HISTORY")!=-1 || wlst.indexOf("WORK")!=-1) { return; }
			this.visible = true;
			this.Resize();
			break;
		default:
			this.visible = false;
			break;
	}
// -------------------------------------------------------
}),'',i2b2.ONT.view.find);


// ================================================================================================== //
i2b2.events.changedZoomWindows.subscribe((function(eventTypeName, zoomMsg) {
// -------------------------------------------------------
	newMode = zoomMsg[0];
	switch (newMode.window) {
		case "ONT":
		case "HISTORY":
		case "WORK":
			this.Resize();
	}
// -------------------------------------------------------
}),'',i2b2.ONT.view.find);


console.timeEnd('execute time');
console.groupEnd();