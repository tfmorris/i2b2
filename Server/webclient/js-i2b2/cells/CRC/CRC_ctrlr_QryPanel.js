/**
 * @projectDescription	Event controller for Query Tool's three query panels. (GUI-only controller).
 * @inherits 	
 * @namespace	
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: CRC > ctrlr > QueryPanel');
console.time('execute time');


function i2b2_PanelController(parentCtrlr) {
	// this is the base class for the single panel controllers
	this.panelCurrentIndex = false;
	this.QTController = parentCtrlr;
	this.refTitle = undefined;
	this.refButtonExclude = undefined;
	this.refButtonDates = undefined;
	this.refButtonOccurs = undefined;
	this.refButtonOccursNum = undefined;
	this.refDispContents = undefined;
	this.refBalloon = undefined;

// ================================================================================================== //
	this.doRedraw = function() {
		if (this.panelCurrentIndex===false) { return true; }
		if (!i2b2.CRC.model.queryCurrent.panels) { i2b2.CRC.model.queryCurrent.panels = []}
		var dm = i2b2.CRC.model.queryCurrent;
		
		// retreve/initialize the display data
		var pd = dm.panels[this.panelCurrentIndex];
		this._redrawPanelStyle(pd);
		// protip: use null data to get other redraws to work
		if (undefined===pd) {
			pd = {};
			pd._nullRecord = true;
			pd.dateTo = false;
			pd.dateFrom = false;
			pd.exclude = false;
			pd.occurs = '0';
			pd.items = [];
		}
		// do redraw
		this._redrawTree(pd);
		this._redrawButtons(pd);
	}

// ================================================================================================== //
	this._redrawPanelStyle = function(pd) {
		if (undefined===pd) {
			// disable visual changes on hover
			Element.removeClassName(this.refButtonExclude,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonOccurs,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonDates,'queryPanelButtonHover');
			Element.removeClassName(this.refButtonExclude,'queryPanelButtonSelected');
			Element.removeClassName(this.refButtonOccurs,'queryPanelButtonSelected');
			Element.removeClassName(this.refButtonDates,'queryPanelButtonSelected');
			// is this panel one up from the max number of panels?
			if (this.panelCurrentIndex == i2b2.CRC.model.queryCurrent.panels.length) {
				this.isActive = 'Y';
				Element.removeClassName(this.refDispContents,'queryPanelHover');
				Element.removeClassName(this.refDispContents,'queryPanelDisabled');
				this.refBalloon.style.display = 'block';
				this.refBalloon.innerHTML = 'drop a<br />term<br />on here';
				this.refBalloon.style.background = '#FFFF99';
			} else {
				this.isActive = 'N';
				Element.addClassName(this.refDispContents,'queryPanelDisabled');
				this.refBalloon.style.display = 'none';
			}
		} else {
			// enable visual changes on hover
			Element.addClassName(this.refButtonExclude,'queryPanelButtonHover');
			Element.addClassName(this.refButtonOccurs,'queryPanelButtonHover');
			Element.addClassName(this.refButtonDates,'queryPanelButtonHover');
			this.isActive = 'Y';
			Element.removeClassName(this.refDispContents,'queryPanelHover');
			Element.removeClassName(this.refDispContents,'queryPanelDisabled');
			this.refBalloon.style.display = 'block';
		}
	}

// ================================================================================================== //
	this._redrawTree = function(pd) {
		if (undefined===pd.tvRootNode) {
			pd.tvRootNode = new YAHOO.widget.RootNode(this.yuiTree);
		}
		// reconnect the root node with the treeview
		YAHOO.widget.TreeView.attachRootNodeToTree(pd.tvRootNode, this.yuiTree);
		// cause the treeview to redraw
		this.yuiTree.draw();
		for (var i=0; i<pd.tvRootNode.children.length; i++) {
			// fix the folder icon for expanded folders
			var n = pd.tvRootNode.children[i];
			this._redrawTreeFix.call(this, n);
		}
	}

// ================================================================================================== //
	this._redrawTreeFix = function(tvNode) {
		// this is a recursive function used to fix all the folder images in a treeview after initial redraw
		if (!tvNode.tree.locked && (tvNode.hasChildren(true))) {
			if (tvNode.children.length > 0 && tvNode.expanded) {
				var imgs = $(tvNode.contentElId);
				if (imgs) { 
					imgs = imgs.select('img'); 
				} else {
					imgs = [];
				}
				if (imgs.length > 0) { 
					var isrc = imgs[0].getAttribute('src').replace('.gif','-exp.gif');
					imgs[0].setAttribute('src', isrc);
				}
			}
			if (!tvNode.isLeaf) {
				tvNode._dynLoad = true;
				// reattach the dynamic load event if it was lost
				this.yuiTree.setDynamicLoad(i2b2.CRC.ctrlr.QT._loadTreeDataForNode,1);
			}
			for (var i=0; i<tvNode.children.length; i++) {
				this._redrawTreeFix.call(this, tvNode.children[i]);
			}
		}
	}

// ================================================================================================== //
	this._redrawButtons = function(pd) {
		// set panel GUI according to data in the "pd" object
		if (undefined===pd) { pd = i2b2.CRC.model.queryCurrent.panels[this.panelCurrentIndex]; }
		if (pd.exclude) {
			Element.addClassName(this.refButtonExclude,'queryPanelButtonSelected');
			this.refBalloon.style.background = '#FF9999';
			this.refBalloon.innerHTML = 'none<br />of<br />these';
		} else {
			Element.removeClassName(this.refButtonExclude,'queryPanelButtonSelected');
			if (pd._nullRecord) {
				this.refBalloon.style.background = '#FFFF99';
				this.refBalloon.innerHTML = 'drop a<br />term<br />on here';
			} else {
				this.refBalloon.style.background = '#99EE99';
				this.refBalloon.innerHTML = 'one or<br />more of<br />these';
			}
		}
		if (pd.occurs > 0) {
			Element.addClassName(this.refButtonOccurs,'queryPanelButtonSelected');
		} else {
			Element.removeClassName(this.refButtonOccurs,'queryPanelButtonSelected');
		}
		this.refButtonOccursNum.innerHTML = pd.occurs;
		if (pd.dateTo || pd.dateFrom) {
			Element.addClassName(this.refButtonDates,'queryPanelButtonSelected');					
		} else {
			Element.removeClassName(this.refButtonDates,'queryPanelButtonSelected');					
		}
	}

// ================================================================================================== //
	this.showLabValues = function(key, extData) {
		i2b2.CRC.view.modalLabValues.show(this.panelCurrentIndex, this, key, extData);
	}

// ================================================================================================== //
	this.showOccurs = function(iMinCount) {
		if (i2b2.CRC.model.queryCurrent.panels.length==0) { return;}
		var dm = i2b2.CRC.model.queryCurrent.panels[this.panelCurrentIndex];
		if (undefined!==dm) {
			// load value
			$('constraintOccursInput').value = dm.occurs;
			// prep variables for JS closure
			var qpi = this.panelCurrentIndex;
			var cpc = this;
			// show occurs window
			if (!this.modalOccurs) {
				if (!Object.isUndefined(handleSubmit)) { delete handleSubmit; } 
				var handleSubmit = function(){
					var closure_qpi = qpi;
					var closure_cpc = cpc;
					// submit value(s)
					if (this.submit()) {
						var pd = i2b2.CRC.model.queryCurrent.panels[closure_qpi];
						pd.occurs = parseInt($('constraintOccursInput').value, 10);
						closure_cpc._redrawButtons(pd);
						i2b2.CRC.ctrlr.QT.doSetQueryName.call(this, '');
					}
				}
				var handleCancel = function(){
					this.cancel();
				}
				this.modalOccurs = new YAHOO.widget.SimpleDialog("constraintOccurs", {
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					modal: true,
					zindex: 700,
					buttons: [{
						text: "OK",
						handler: handleSubmit,
						isDefault: true
					}, {
						text: "Cancel",
						handler: handleCancel
					}]
				});
				$('constraintOccurs').show();
				this.modalOccurs.validate = function(){
					// now process the form data
					var t = parseInt($('constraintOccursInput').value, 10);
					if (isNaN(t)) {
						alert('The number you entered could not be understood.\nPlease make sure that you entered a valid number.');
						return false;
					}
					if (t > 19) {
						alert('The number you entered was too large.\nThe maximum number you can enter is 19.');
						return false;
					}
					if (t < 0) {
						alert('The number you entered was too small.\nThe minimum number you can enter is 0.');
						return false;
					}
					return true;
				};
				this.modalOccurs.render(document.body);
			}
			this.modalOccurs.show();
		}
	}

// ================================================================================================== //
	this.doExclude = function(bExclude) { 
		if (i2b2.CRC.model.queryCurrent.panels.length==0) { return;}
		var bVal;
		var dm = i2b2.CRC.model.queryCurrent.panels[this.panelCurrentIndex];
		if (undefined!==dm) {
			if (undefined!=bExclude) {
				bVal = bExclude;
			} else {
				bVal = !Boolean(dm.exclude);
			}
			dm.exclude = bVal;
			this._redrawButtons(dm);
		}
		// clear the query name and set the query as having dirty data
		var QT = i2b2.CRC.ctrlr.QT;
		QT.doSetQueryName.call(QT,'');
	}

// ================================================================================================== //
	this.doDrop = function(sdxConcept) {	// function to handle drag and drop
		// insert concept into our panel's items array;
		var dm = i2b2.CRC.model.queryCurrent;
		var repos = false;
		var targetPanelIndex = this.panelCurrentIndex;
		if (Object.isUndefined(dm.panels[targetPanelIndex])) { 
			this.QTController.panelAdd(this.yuiTree);
			repos = true;
		} 
		var il = dm.panels[targetPanelIndex].items;
		// check for duplicate data
		for (var i=0; i<il.length; i++) {
			if (il[i].sdxInfo.sdxKeyValue==sdxConcept.sdxInfo.sdxKeyValue) {return false; }					
		}
		// save data
		this._addConcept(sdxConcept,this.yuiTree.root, true);
		// reset the query name to be blank and flag as having dirty data
		i2b2.CRC.ctrlr.QT.doSetQueryName('');
		this.QTController._redrawAllPanels();
	}

// ================================================================================================== //
	this._addConcept = function (sdxConcept, tvParent, isDragged) {
		var tmpNode = this._addConceptVisuals.call(this, sdxConcept, tvParent, isDragged);
		// add concept to data model record for panel
		var panel = i2b2.CRC.model.queryCurrent.panels[this.panelCurrentIndex]
		panel.items[panel.items.length] = sdxConcept;
		return tmpNode;
	}

// ================================================================================================== //
	this._addConceptVisuals = function (sdxConcept, tvParent, isDragged) {
		var tvTree = tvParent.tree;
		var sdxDataNode = i2b2.sdx.Master.EncapsulateData('CONCPT',sdxConcept.origData);
		if (!sdxDataNode) { return false; }
		var renderOptions = {
			title: sdxConcept.origData.name,
			dblclick: "i2b2.CRC.ctrlr.QT.ToggleNode(this,'"+tvTree.id+"')",
			icon: {
				root: "sdx_ONT_CONCPT_root.gif",
				rootExp: "sdx_ONT_CONCPT_root-exp.gif",
				branch: "sdx_ONT_CONCPT_branch.gif",
				branchExp: "sdx_ONT_CONCPT_branch-exp.gif",
				leaf: "sdx_ONT_CONCPT_leaf.gif"
			}
		};
		var sdxRenderData = i2b2.sdx.Master.RenderHTML(tvTree.id, sdxDataNode, renderOptions);
		i2b2.sdx.Master.AppendTreeNode(tvTree, tvParent, sdxRenderData);
		return sdxRenderData;
	}	

// ================================================================================================== //
	this._deleteConcept = function(key) {
		var pd = i2b2.CRC.model.queryCurrent.panels[this.panelCurrentIndex];
		// remove the concept from panel
		for (var i=0; i< pd.items.length; i++) {
			if (pd.items[i].origData.key == key) {
				// found the concept to remove
				var rto = pd.items[i];
				break;
			}
		}
		if (undefined===rto) { return; }
		// remove the node in the treeview
		var tvChildren = pd.tvRootNode.children
		for (var i=0; i< tvChildren.length; i++) {
			if (tvChildren[i].data.i2b2_SDX.sdxInfo.sdxKeyValue===rto.origData.key) {
				this.yuiTree.removeNode(tvChildren[i],false);
				this._redrawTree.call(this, pd);
				break;
			}
		}
		// remove the concept from the data model
		pd.items.splice(i,1);
		// remove this panel if it's empty
		if (pd.items.length == 0) { this.doDelete(); }
		// clear the query name if it was set
		this.QTController.doSetQueryName.call(this,'');
	}

// ================================================================================================== //
	this.setPanelRecord = function (index) { 
		this.panelCurrentIndex = index; 
		this.doRedraw();
	}

// ================================================================================================== //
	this.doDelete = function() { 
		// function fired when the [X] icon for the GUI panel is clicked
		i2b2.CRC.ctrlr.QT.panelDelete(this.panelCurrentIndex);
		// redraw the panels 
		var idx = this.panelCurrentIndex - this.ctrlIndex;
		if (idx < 0) { idx = 0; }
		i2b2.CRC.ctrlr.QT.doShowFrom(idx);
	}	
}




console.timeEnd('execute time');
console.groupEnd();